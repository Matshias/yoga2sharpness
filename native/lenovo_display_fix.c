#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "drm/drm.h"
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <jni.h>

const char drm_dev_name[] = "/dev/dri/card0";


char out_str[8192];
char *out_ptr = &out_str[0];

int set_pfit_noroot(int fd, __u32 prop_id, __u32 conn_id)
{
	struct drm_mode_connector_set_property drm_prop_set;
	int res = -1;

	drm_prop_set.value = 0; // disable PFIT
	drm_prop_set.prop_id = prop_id;
	drm_prop_set.connector_id = conn_id;

	res = ioctl(fd, DRM_IOCTL_MODE_SETPROPERTY, &drm_prop_set);
	if (res == -1)
	{	
		out_ptr += sprintf(out_ptr, "ERROR: setting pfit via IOCTL\n");
		return res;
	}

	usleep(1000000);

	drm_prop_set.value = 1; // reenable PFIT, has no effect but if we don't do it we won't be able to set to 0 in the next display on/off cycle
	drm_prop_set.prop_id = prop_id;
	drm_prop_set.connector_id = conn_id;

	if (ioctl(fd, DRM_IOCTL_MODE_SETPROPERTY, &drm_prop_set) == -1)
	{
		res = -1;
	}

	return res;
}

void disable_i915_pfit()
{
	struct drm_mode_card_res drm_res;
	struct drm_mode_obj_get_properties drm_conn;
	struct drm_mode_get_property drm_prop;
	
	int fd = open(drm_dev_name, O_RDWR);
	int res;

	if (fd < 0)
	{
		out_ptr += sprintf(out_ptr, "ERROR: cannot open %s!\n", drm_dev_name);
		return;
	}

	if (1)
	{
		out_ptr += sprintf(out_ptr, "Try quick setting of panel fitting property ...\n");
		res = set_pfit_noroot(fd, 11, 10);
		if (res > -1)
		{
			out_ptr += sprintf(out_ptr, "SUCCESS: successfully set pfit=0\n");
			close(fd);
			return;
		}
		else if (res == -2)
		{
			close(fd);
			return;
		}
		else
		{
			out_ptr += sprintf(out_ptr, "WARNING: quick set failed, try long way\n");
		
		}	
	}

	// find connectors
	drm_res.fb_id_ptr = 0;
	drm_res.fb_id_ptr = 0;
	drm_res.crtc_id_ptr = 0;
	drm_res.connector_id_ptr = (__u64)malloc(4 * sizeof(unsigned long));
	drm_res.encoder_id_ptr;
	drm_res.count_fbs = 0;
	drm_res.count_crtcs = 0;
	drm_res.count_connectors = 4;
	drm_res.count_encoders = 0;

	drm_conn.props_ptr = malloc(256 * sizeof(__u32));
	drm_conn.prop_values_ptr = malloc(256 * sizeof(__u64));
	drm_conn.count_props = 256;
	drm_conn.obj_id = 0; // filled later
	drm_conn.obj_type = 0xc0c0c0c0; // DRM_MODE_OBJECT_CONNECTOR

	drm_prop.values_ptr = malloc(32 * sizeof(__u64));
	drm_prop.enum_blob_ptr = malloc(32 * sizeof(__u64));
	
	res = ioctl(fd, DRM_IOCTL_MODE_GETRESOURCES, &drm_res);
	if (res == -1)
	{
		out_ptr += sprintf(out_ptr, "ERROR: ioctl failed with error %d\n", errno);
		
	}
	else
	{
		int i;
		unsigned long *ids = (unsigned long*)drm_res.connector_id_ptr;
		out_ptr += sprintf(out_ptr, "found %u connectors\n", drm_res.count_connectors);
		for (i = 0; i < drm_res.count_connectors; i++)
		{
			drm_conn.count_props = 256;
			drm_conn.obj_id = (__u32)ids[i]; 
			out_ptr += sprintf(out_ptr, "Connector ID: %lu\n", ids[i]);

			// retrieve properties for connector
			res = ioctl(fd, DRM_IOCTL_MODE_OBJ_GETPROPERTIES, &drm_conn);
			if (res != -1)
			{
				int j;
				__u32 *prop_ids = (__u32*)drm_conn.props_ptr;
				__u64 *prop_vals = (__u64*)drm_conn.prop_values_ptr;

				out_ptr += sprintf(out_ptr, "found %d properties\n", drm_conn.count_props);
				for (j = 0; j < drm_conn.count_props; j++)
				{
					drm_prop.prop_id = prop_ids[j];
					drm_prop.count_values = 32;
					drm_prop.count_enum_blobs = 32;
					drm_prop.flags = 0;
					
					out_ptr += sprintf(out_ptr, " property id: %u, value %lu\n", prop_ids[j], (unsigned long)prop_vals[j]);
				
					// retrieve property information
					res = ioctl(fd, DRM_IOCTL_MODE_GETPROPERTY, &drm_prop);
					if (res != -1)
					{
						out_ptr += sprintf(out_ptr, " flags: %u, name: %s, values %u, enums %d\n", drm_prop.flags, drm_prop.name, drm_prop.count_values, drm_prop.count_enum_blobs);
						if (strcmp(drm_prop.name, "pfit") == 0)
						{
							if (prop_vals[j] == 1)
							{
								// set value for property pfit
								res = set_pfit_noroot(fd, prop_ids[j], (__u32)ids[i]);
								if (res > -1)
								{
									out_ptr += sprintf(out_ptr, "SUCCESS: set pfit=0, prop_id = %u, connector_id = %lu\n", prop_ids[j], ids[i]);
									return;
								}
							}
							else 
							{
								out_ptr += sprintf(out_ptr, "PFIT already disabled\n");
							}
						}
					}
				}
			}
		}
	}

	close(fd);
	free((void*)drm_res.connector_id_ptr);
	free((void*)drm_conn.props_ptr);
	free((void*)drm_conn.prop_values_ptr);
	free((void*)drm_prop.values_ptr);
	free((void*)drm_prop.enum_blob_ptr);
}

jstring
Java_com_matshias_yoga2sharpnessfix_BroadcastIntentService_setPFIT( JNIEnv* env,
                                                  jobject thiz )
{
	out_ptr = &out_str[0];

	disable_i915_pfit();

	return (*env)->NewStringUTF(env, out_str);
}


