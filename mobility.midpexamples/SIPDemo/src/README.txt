--------------------------------------------------------------------------------
                SIP API for J2ME (JSR 180 API) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    This application is a very simple example of using SIP (JSR 180) to communicate 
    directly between two devices. Usually devices will use SIP with a proxy server 
    to set up direct communications of some kind. For a more complete example involving 
    a proxy, take a look at GoSip.
    
2. Usage

    - To see how SIPDemo works, run two instances of the emulator. In the first, 
      choose Receive message. You can use the default port, 5070, and choose Receive. 
      The first emulator is now listening for incoming messages.
    - In the second emulator, choose Send message. Fill in values for the recipient, 
      port number, subject, and message, or accept the defaults, and choose Send. 
      Your message will be displayed in the first emulator. The first emulator's 
      response is displayed in the second emulator.
    - Try it again with the network monitor turned on. You can see the communication 
      between the emulators in the network monitor SIP tab. 


3. Required APIs
    
    JSR 139 - Connected Limited Device Configuration (CLDC) 1.1
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 180 - SIP API for J2ME
