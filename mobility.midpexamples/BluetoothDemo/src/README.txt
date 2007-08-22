--------------------------------------------------------------------------------
                Bluetooth API (JSR 82 API) Demostration
--------------------------------------------------------------------------------
1. Introduction

    Bluetooth Demo - allows to exchange pictures between phones.
    It's intended to be run on two or more phones.


2. Bluetooth Demo

   2.1 The demo consist in 2 parts - client and server. You may run several
       clients or servers if you want.

   2.2 If you run the server, the bluetooth notifier is created (so you may be
       asked with security question) and it is accepting the clients
       already. The corresponding service record contains the attribute with
       the published images information.

       By default no images are published!

       Use "Publish image" and "Remove image" commands in MIDlet menu to
       add/remove the corresponding images information to/from service record.

       The published images are highlighted in the selection list.

   2.3 If you run the client and bluetooth system is initialized successfully,
       you see the "Ready for images search!" message. Select "Find" command
       to start a search.

       In fact, such a search includes both device and service
       discovering. The device search is done for PREKNOWN and CACHED devices
       too.

       You may cancel the device/service search - then you get the main client
       display.

       Select the image you want to download and choose "Load" command. You
       may cancel the image download. After you review the image, go back to
       found images list to choose another image to be download or to return
       back to new images each.

   2.4 The server is created with the URL, that indicates each connected
       client should be AUTHORIZED.

       I.e. when client is downloading the image, the corresponding server
       phone shows user the security dialog with remote device friendly name
       (if it is available) and bluetooth address.

   2.5 If several servers published the same images, the "union" list is shown
       on the client as a search result. This application hides the
       information from user what server (bluetooth device) publish these
       images.

       Still, all of the available sources (servers) for the image are stored
       in the client application. When user chooses to download image, the
       first source is attempted. In case of I/O error, next source is used
       and so on.

       After the images search and before the image download the server may
       shutdown or just "Remove image" from published list. In this case the
       client fails to download image from this server.
       

3. Required APIs
    
    JSR 30 - Connected Limited Device Configuration (CLDC) 1.0
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 82 - Java APIs for Bluetooth

