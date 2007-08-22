--------------------------------------------------------------------------------
           Wireless Messaging API (WMA) 2.0 (JSR 205) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    This application shows how to send and receive SMS, CBS, and MMS messages. 
    The Sun JavaTM Wireless Toolkit for CLDC offers a flexible emulation environment 
    to support messaging. Messages can be exchanged between emulator instances 
    and can be generated or received using the WMA console utility.
    
    
2. WMADemo

    Because this example makes use of the push registry, you can't see all of its 
    features just by using the Run button. Use the Run via OTA feature to install 
    the application into the emulator in a process that mirrors how applications are 
    installed on real devices.
    To exercise the push registry, use the WMA console to send the emulator a message. 
    Launch the console by choosing Choose Tools > Java Platforms, 
    select WTK emulator. On Tools&Extensions tab, choose Open Utilities. 
    Click on the Open Console button in the WMA box to launch the WMA console.
    Click on the Send SMS... button in the WMA console window. Choose the number 
    that corresponds to the emulator, probably +5550000. If you're not sure what 
    number the emulator is using, look in its title bar. Choose the number in 
    the SMS message window, then fill in a port number of 50000. Type your text 
    message in the Message field and click on Send. 


3. Required APIs
    
    JSR 30 - Connected Limited Device Configuration (CLDC) 1.0
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 135 - Mobile Media API (MMAPI)
    JSR 205 - Wireless Messaging API (WMA) 2.0
