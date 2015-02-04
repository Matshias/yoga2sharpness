Yoga Tablet 2 sharpness fix
===========================

On Lenovo's Yoga 2 Tablet there are (probably not wittingly) two filters applied to the display contents.

* Blur filter by the Intel GPU (panel fitting, pfit)

* Sharpen filter by the panel in conjunction with CABC (content adaptive backlight control)

Both can be disabled but not permanently. Every time the device goes to sleep and wakes up again the default settings are restored. 

This app can disable both filters after screen on. 

Technical details
-----------------

1) Blur filter

The blur filter can be switched of by using IOCTLs on the GPU driver (i915). The device file /dev/dri/card0 can be read and 
written without super user priviledges. The IOCTL is DRM_IOCTL_MODE_SETPROPERTY. The property and connector id can be detected 
by the app if are different from the predefined values.

2) Sharpen filter
The sharpen filter can onbly be disabled having super user priviledges by writing the '0' character to the sysfs file 
/sys/lcd_panel/cabc_onoff. This will also be disable the CABC feature of the panel. Since the specs of the panel 
(AUO B101UAN01.E) are not public it is hard to check if the filter could be disabled without CABC




