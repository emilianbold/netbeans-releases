--------------------------------------------------------------------------------
      Advanced Multimedia Supplements (JSR 234 API) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    This MIDlet suite demonstrates the power of JSR 234 Advanced Multimedia 
    Supplements (AMMS). It consists of the following MIDlets:

    1.1 Image Effects 
        Shows standard image processing operations. Choose an effect from 
        the menu. The processed image is shown following the source image. 
        Note that some items, Set Transforms, for example, can perform several 
        operations. Click the Done soft button to apply every effect.

    1.2 Radio Tuner 
        Simulates a radio tuner by reading audio files from the project 
        directory via the toolkit's built-in HTTP server.

    1.3 Camera 
        Shows how the Advanced Multimedia Supplements provide control 
        of a device's camera. The screen shows the viewfinder of the camera 
        (simulated with a movie). You can use commands in the menu to change 
        the camera settings and take and manage snapshots.

    1.4 Moving Helicopter 
        Simulates a helicopter flying around a stationary observer. 
        Use headphones for best results. You can control many of the parameters 
        of the simulation, including the size of the helicopter, whether 
        the Doppler effect is used, and the spectator orientation (for example, 
        standing straight or lying face down).

    1.5 Music Effects 
        Shows off the advanced audio capabilities of the Advanced Multimedia 
        Supplements. As an audio file loops continuously, you can adjust 
        the volume, pan, equalizer settings, reverberation, and chorus settings. 


2. Required APIs
    
    JSR 139 - Connected Limited Device Configuration (CLDC) 1.1
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 135 - Mobile Media API (MMAPI)
    JSR 234 - Advanced Multimedia Supplements
