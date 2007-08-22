--------------------------------------------------------------------------------
     Mobile Information Device Profile (MIDP) 2.0 (JSR 118) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    This demo has two MIDlets: Socket Demo and Datagram Demo. Each demo requires 
    you to run two emulator instances so that you can emulate the server and 
    client relationship.

    
2. Network Demo

    2.1 Socket Demo
        - Run two instances of the emulator. One acts as the socket server, and 
          the other as the socket client.
        - In the first emulator, launch the application, then select the Server peer. 
          Choose Start. The emulator explains that the demo wants to send and receive 
          data over the network and asks, "Is it OK to use network?" Choose Yes. 
          The Socket Server displays a screen that indicates it is waiting for a connection.
        - In the second emulator, launch the application, select the Client peer, 
          then choose Start. The emulator explains that the demo wants to send and receive 
          data over the network and asks, "Is it OK to use network?" Choose Yes. 
          The Socket Client displays a screen that indicates it is connected to 
          the server. Use the down navigation arrow to highlight the Send box. 
          Type a message in the Send box, then choose the Send soft key.
        - On the emulator running the Socket Server, the Status reads: 
          Message received - Hello Server. You can use the down arrow to move to 
          the Send box and type a reply. For example, Hello Client, I heard you.
        - Back in the Socket Client, the status shows the message received from the server. 
          Until you send a new message, the Send box contains the previous message you sent.
          
    2.2 Datagram Demo
        - Run two instances of the emulator. One acts as the datagram server, and 
          the other as the datagram client.
        - In the first emulator, launch Datagram Demo, then select the Server peer. 
          Choose Start. The emulator explains that the demo wants to send and receive 
          data over the network and asks, "Is it OK to use network?" Choose Yes. 
          Initially, the Datagram Server status is Waiting for connection, and 
          the Send box is empty.
        - In the second emulator, launch Datagram Demo, select the Client peer, 
          then choose Start. The emulator explains that the demo wants to send 
          and receive data over the network and asks, "Is it OK to use network?" 
          Choose Yes. The Datagram Client status is: Connected to server. 
          Use the down navigation arrow to highlight the Send box. Type a message in 
          the Send box, then choose the Send soft key. For example, type 
          Hello datagram server.
        - On the emulator running the Datagram Server, the Status displays: 
          Message received - Hello datagram server. You can use the down arrow to 
          move to the Send box and type a reply to the client.
        - In the Datagram Client, the status field displays the message received 
          from the server. The Send box contains the last message you sent. 
        - For example, in the client, type Hello Server In the Send box. 
          Choose the Send soft key. The emulator activates a blue light during 
          the transmission. 


3. Required APIs
    
    JSR 30 - Connected Limited Device Configuration (CLDC) 1.0
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
