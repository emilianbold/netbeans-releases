--------------------------------------------------------------------------------
                    The JSR 82 IrDA Obex Demonstration
--------------------------------------------------------------------------------

Content:

1. Demo organization
2. Demo scenarios
   2.1 The base scenario
   2.2 Choose from several obex devices
   2.3 Device discovery timeout
   2.4 Maximum packet length
3. Required APIs
--------------------------------------------------------------------------------

1. Demo organization

   Obex Demo is placed in package example.obex.demo and contains 5 classes:
   
   ObexDemoMIDlet - the main class which creates a choice list
                    to run client or server part of the Demo;

   ObexImageSender - it's an OBEX client, which sends the selected
                     image to the OBEX server;

   ObexImageReceiver - it's an OBEX server, which receives the image 
                        from the OBEX client;

   GUIImageSender & GUIImageReceiver - the GUI parts of 
   ObexImageSender & ObexImageReceiver (may be missed on early access
   version of the demo).


2. Demo scenarios

   There are 4 scenarios of demo given below:

   2.1 The base scenario
      - Run two emulators and launch ObexDemo in each;
      - In the first emulator -  select a ImageReceiver;
      - In the second emulator - select a ImageSender;
      - In new list which you can see on the second emulator
        screen - select any picture name;
      - In the first emulator - answer "yes" to accept 
        and download incoming image;
      - After you looked on downloaded image, press "back"
        to exit to the main menu of the demo.

   2.2 Choose from several obex devices
      - Run tree emulators and launch ObexDemo in each
      - In the second and third emulators - select a ImageSender,
        choose an image to send and initiate the sending
      - No later then in 5 seconds, in the first emulator -  
        select a ImageReceiver;
      - In the first emulator - answer "yes" to accept 
        and download incoming image;
      - You'll see the server resumes a work of those
        client (second or third emulator) which was connecting
        first (before another client), and there is no way
        for server to understand which client connects him
        (for such simple applications)

   2.3   Device discovery timeout
      - Run two emulators and launch ObexDemo in each;
      - In the first emulator - select a ImageSender,
        choose an image and start a sending. You'll
        see the "Connecting..." title above the progress.
        After some time (10 seconds by default) you'll
        see "Receiver isn't ready to download image" response.
      - In the first emulator start a sending again.
      - Very fast in the second emulator -  select a ImageReceiver - 
        you'll see the first emulator has discovered the second one
        and proceed downloading.
      - Choose Tools > Java Platforms, select WTK emulator. On Tools&Extensions tab,
        choose Open Preferences. Change the "Device discovery timeout" in Preferences 
        window: Edit->Preferences->IrDA OBEX->Discovery Timeout
        to see it does affect the time the device tries to discover
        another devices
        
   2.4  Maximum packet length
      - process the demo scenario 2.1 (base scenario) - use
        the cdcrus.png image (the largest one in the list) - 
        remember the time the image data was uploaded,
        exit these emulator
      - in the IrDA OBEX tab in Preferences window reduce the 
        Maximum packet length value to 128 bytes (for example);
      - process the demo scenario 2.1 with the largest image - 
        you'll see the image uploading time has been increased;
      - exit one of emulators, keeps another one alive,
        set Maximum packet length value to 65536 bytes (64Kb)
        and run a new emulator, then process the demo scenario 2.1 - 
        you'll see the download speed is not changed - the 
        IrDA devices negotiates about the packet size to use
        while connecting, so the smallest packet size has an effect;


3. Required APIs
    
    JSR 30 - Connected Limited Device Configuration (CLDC) 1.0
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 82 - Java APIs for Bluetooth
 
