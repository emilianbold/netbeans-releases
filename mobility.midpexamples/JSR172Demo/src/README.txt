--------------------------------------------------------------------------------
              J2ME Web Services (JSR 172 API) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    JSR172Demo shows how to access a web service from a MIDlet. The web service 
    is already running on an Internet server. If you are behind a firewall, 
    you must configure the emulator's proxy server settings. Choose Tools > Java Platforms, 
    select WTK emulator. On Tools&Extensions tab, choose Open Preferences 
    from the  menu, then select Network Configuration. Fill in the proxy 
    server address file and the port number.

2. Usage

    - Run the example.
    - You can browse through simulated news headlines, all of which are retrieved 
      from the web service.
    - To see what is going on behind the scenes, use the network monitor.

3. Required APIs
    
    JSR 139 - Connected Limited Device Configuration (CLDC) 1.1
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 172 - J2ME Web Services Specification


Note:
------
    You may use a "Java ME Web Service Client" before the build to regenerate the connection
    package sources.
