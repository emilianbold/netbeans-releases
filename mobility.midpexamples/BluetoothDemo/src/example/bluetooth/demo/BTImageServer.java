/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * @(#)BTImageServer.java	1.1 04/04/24
 */
package example.bluetooth.demo;

// jsr082 API
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.ServiceRegistrationException;
import javax.bluetooth.UUID;

// midp/cldc API
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.Hashtable;

/**
 * Established the BT service, accepts connections
 * and send the requested image silently.
 *
 * @author Vladimir K. Beliaev
 * @version 1.1, 04/24/04
 */
final class BTImageServer implements Runnable {

    /** Describes this server - note, it's a "short" UUID. */
    private static final UUID PICTURES_SERVER_UUID = new UUID(0x12345);

    /** The attribute id of the record item with images names. */
    private static final int IMAGES_NAMES_ATTRIBUTE_ID = 0x4321;

    /** Keeps the local device reference. */
    private LocalDevice localDevice;

    /** Accepts new connections. */
    private StreamConnectionNotifier notifier;

    /** Keeps the information about this server. */
    private ServiceRecord record;

    /** Keeps the parent reference to process specific actions. */
    private ServerMIDlet parent;

    /** Becomes 'true' when this component is finilized. */
    private boolean isClosed;

    /** Creates notifier and accepts clients to be processed. */
    private Thread accepterThread;

    /** Process the particular client from queue. */
    private ClientProcessor processor;

    /** Optimization: keeps the table of data elements to be published. */
    private final Hashtable dataElements = new Hashtable();

    /**
     * Constructs the bluetooth server, but it is initialized
     * in the different thread to "avoid dead lock".
     */
    BTImageServer(ServerMIDlet parent) {
        this.parent = parent;

        // we have to initialize a system in different thread...
        accepterThread = new Thread(this);
        accepterThread.start();
    }

    /**
     * Accepts a new client and send him/her a requested image.
     */
    public void run() {
        boolean isBTReady = false;

        try {

            // create/get a local device
            localDevice = LocalDevice.getLocalDevice();

            // set we are discoverable
            if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
                throw new IOException("Can't set discoverable mode...");
            }

            // prepare a URL to create a notifier
            StringBuffer url = new StringBuffer("btspp://");

            // indicate this is a server
            url.append("localhost").append(':');

            // add the UUID to identify this service
            url.append(PICTURES_SERVER_UUID.toString());

            // add the name for our service
            url.append(";name=Picture Server");

            // request all of the client to be authorized
            url.append(";authorize=true");

            // create notifier now
            notifier = (StreamConnectionNotifier) Connector.open(
                    url.toString());

            // and remember the service record for the later updates
            record = localDevice.getRecord(notifier);

            // create a special attribute with images names
            DataElement base = new DataElement(DataElement.DATSEQ);
            record.setAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID, base);

            // remember we've reached this point.
            isBTReady = true;
        } catch (Exception e) {
            System.err.println("Can't initialize bluetooth: " + e);
        }
        parent.completeInitialization(isBTReady);

        // nothing to do if no bluetooth available
        if (!isBTReady) {
            return;
        }

        // ok, start processor now
        processor = new ClientProcessor();

        // ok, start accepting connections then
        while (!isClosed) {
            StreamConnection conn = null;

            try {
                conn = notifier.acceptAndOpen();
            } catch (IOException e) {

                // wrong client or interrupted - continue anyway
                continue;
            }
            processor.addConnection(conn);
        }
    }

    /**
     * Updates the service record with the information
     * about the published images availability.
     * <p>
     * This method is invoked after the caller has cheched
     * already that the real action should be done.
     *
     * @return true if record was updated successfully, false otherwise.
     */
    boolean changeImageInfo(String name, boolean isPublished) {

        // ok, get the record from service
        DataElement base = record.getAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID);

        // check the corresponding DataElement object is created already
        DataElement de = (DataElement) dataElements.get(name);

        // if no, then create a new DataElement that describes this image
        if (de == null) {
            de = new DataElement(DataElement.STRING, name);
            dataElements.put(name, de);
        }

        // we know this data element has DATSEQ type
        if (isPublished) {
            base.addElement(de);
        } else {
            if (!base.removeElement(de)) {
                System.err.println("Error: item was not removed for: " + name);
                return false;
            }
        }
        record.setAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID, base);

        try {
            localDevice.updateRecord(record);
        } catch (ServiceRegistrationException e) {
            System.err.println("Can't update record now for: " + name);
            return false;
        }
        return true;
    }

    /**
     * Destroy a work with bluetooth - exits the accepting
     * thread and close notifier.
     */
    void destroy() {
        isClosed = true;

        // finilize notifier work
        if (notifier != null) {
            try {
                notifier.close();
            } catch (IOException e) {} // ignore
        }

        // wait for acceptor thread is done
        try {
            accepterThread.join();
        } catch (InterruptedException e) {} // ignore

        // finilize processor
        if (processor != null) {
            processor.destroy(true);
        }
        processor = null;
    }

    /**
     * Reads the image name from the specified connection
     * and sends this image through this connection, then
     * close it after all.
     */
    private void processConnection(StreamConnection conn) {

        // read the image name first
        String imgName = readImageName(conn);

        // check this image is published and get the image file name
        imgName = parent.getImageFileName(imgName);

        // load image data into buffer to be send
        byte[] imgData = getImageData(imgName);

        // send image data now
        sendImageData(imgData, conn);

        // close connection and good-bye
        try {
            conn.close();
        } catch (IOException e) {} // ignore
    }

    /** Send image data. */
    private void sendImageData(byte[] imgData, StreamConnection conn) {
        if (imgData == null) {
            return;
        }
        OutputStream out = null;

        try {
            out = conn.openOutputStream();
            out.write(imgData.length >> 8);
            out.write(imgData.length & 0xff);
            out.write(imgData);
        } catch (IOException e) {
            System.err.println("Can't send image data: " + e);
        }

        // close output stream anyway
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {} // ignore
        }
    }

    /** Reads image name from specified connection. */
    private String readImageName(StreamConnection conn) {
        String imgName = null;
        InputStream in = null;

        try {
            in = conn.openInputStream();
            int length = in.read(); // 'name' length is 1 byte

            if (length <= 0) {
                throw new IOException("Can't read name length");
            }
            byte[] nameData = new byte[length];
            length = 0;

            while (length != nameData.length) {
                int n = in.read(nameData, length, nameData.length - length);

                if (n == -1) {
                    throw new IOException("Can't read name data");
                }
                length += n;
            }
            imgName = new String(nameData);
        } catch (IOException e) {
            System.err.println(e);
        }

        // close input stream anyway
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {} // ignore
        }
        return imgName;
    }

    /** Reads images data from MIDlet archive to array. */
    private byte[] getImageData(String imgName) {
        if (imgName == null) {
            return null;
        }
        InputStream in = getClass().getResourceAsStream(imgName);

        // read image data and create a byte array
        byte[] buff = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

        try {
            while (true) {
                int length = in.read(buff);

                if (length == -1) {
                    break;
                }
                baos.write(buff, 0, length);
            }
        } catch (IOException e) {
            System.err.println("Can't get image data: imgName=" + imgName + " :"
                    + e);
            return null;
        }
        return baos.toByteArray();
    }

    /**
     * Orginizes the queue of clients to be processed,
     * processes the clients one by one until destroyed.
     */
    private class ClientProcessor implements Runnable {
        private Thread processorThread;
        private Vector queue = new Vector();
        private boolean isOk = true;

        ClientProcessor() {
            processorThread = new Thread(this);
            processorThread.start();
        }

        public void run() {
            while (!isClosed) {

                // wait for new task to be processed
                synchronized (this) {
                    if (queue.size() == 0) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            System.err.println("Unexpected exception: " + e);
                            destroy(false);
                            return;
                        }
                    }
                }

                // send the image to specified connection
                StreamConnection conn;

                synchronized (this) {

                    // may be awaked by "destroy" method.
                    if (isClosed) {
                        return;
                    }
                    conn = (StreamConnection) queue.firstElement();
                    queue.removeElementAt(0);
                    processConnection(conn);
                }
            }
        }

        /** Adds the connection to queue and notifys the thread. */
        void addConnection(StreamConnection conn) {
            synchronized (this) {
                queue.addElement(conn);
                notify();
            }
        }

        /** Closes the connections and . */
        void destroy(boolean needJoin) {
            StreamConnection conn;

            synchronized (this) {
                notify();

                while (queue.size() != 0) {
                    conn = (StreamConnection) queue.firstElement();
                    queue.removeElementAt(0);

                    try {
                        conn.close();
                    } catch (IOException e) {} // ignore
                }
            }

            // wait until dispatching thread is done
            try {
                processorThread.join();
            } catch (InterruptedException e) {} // ignore
        }
    }
} // end of class 'BTImageServer' definition
