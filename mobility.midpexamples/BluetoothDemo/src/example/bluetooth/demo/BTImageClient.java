/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.bluetooth.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

// jsr082 API
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

// midp/cldc API
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Image;


/**
 * Initialize BT device, search for BT services,
 * presents them to user and picks his/her choice,
 * finally download the choosen image and present
 * it to user.
 *
 * @version ,
 */
final class BTImageClient implements Runnable, DiscoveryListener {
    /** Describes this server */
    private static final UUID PICTURES_SERVER_UUID =
        new UUID("F0E0D0C0B0A000908070605040302010", false);

    /** The attribute id of the record item with images names. */
    private static final int IMAGES_NAMES_ATTRIBUTE_ID = 0x4321;

    /** Shows the engine is ready to work. */
    private static final int READY = 0;

    /** Shows the engine is searching bluetooth devices. */
    private static final int DEVICE_SEARCH = 1;

    /** Shows the engine is searching bluetooth services. */
    private static final int SERVICE_SEARCH = 2;

    /** Keeps the current state of engine. */
    private int state = READY;

    /** Keeps the discovery agent reference. */
    private DiscoveryAgent discoveryAgent;

    /** Keeps the parent reference to process specific actions. */
    private GUIImageClient parent;

    /** Becomes 'true' when this component is finalized. */
    private boolean isClosed;

    /** Process the search/download requests. */
    private Thread processorThread;

    /** Collects the remote devices found during a search. */
    private Vector /* RemoteDevice */ devices = new Vector();

    /** Collects the services found during a search. */
    private Vector /* ServiceRecord */ records = new Vector();

    /** Keeps the device discovery return code. */
    private int discType;

    /** Keeps the services search IDs (just to be able to cancel them). */
    private int[] searchIDs;

    /** Keeps the image name to be load. */
    private String imageNameToLoad;

    /** Keeps the table of {name, Service} to process the user choice. */
    private Hashtable base = new Hashtable();

    /** Informs the thread the download should be canceled. */
    private boolean isDownloadCanceled;

    /** Optimization: keeps service search pattern. */
    private UUID[] uuidSet;

    /** Optimization: keeps attributes list to be retrieved. */
    private int[] attrSet;

    /**
     * Constructs the bluetooth server, but it is initialized
     * in the different thread to "avoid dead lock".
     */
    BTImageClient(GUIImageClient parent) {
        this.parent = parent;

        // we have to initialize a system in different thread...
        processorThread = new Thread(this);
        processorThread.start();
    }

    /**
     * Process the search/download requests.
     */
    public void run() {
        // initialize bluetooth first
        boolean isBTReady = false;

        try {
            // create/get a local device and discovery agent
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            discoveryAgent = localDevice.getDiscoveryAgent();

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

        // initialize some optimization variables
        uuidSet = new UUID[2];

        // ok, we are interesting in btspp services only
        uuidSet[0] = new UUID(0x1101);

        // and only known ones, that allows pictures
        uuidSet[1] = PICTURES_SERVER_UUID;

        // we need an only service attribute actually
        attrSet = new int[1];

        // it's "images names" one
        attrSet[0] = IMAGES_NAMES_ATTRIBUTE_ID;

        // start processing the images search/download
        processImagesSearchDownload();
    }

    /**
     * Invoked by system when a new remote device is found -
     * remember the found device.
     */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        // same device may found several times during single search
        if (devices.indexOf(btDevice) == -1) {
            devices.addElement(btDevice);
        }
    }

    /**
     * Invoked by system when device discovery is done.
     * <p>
     * Remember the discType
     * and process its evaluation in another thread.
     */
    public void inquiryCompleted(int discType) {
        this.discType = discType;

        synchronized (this) {
            notify();
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            records.addElement(servRecord[i]);
        }
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        // first, find the service search transaction index
        int index = -1;

        for (int i = 0; i < searchIDs.length; i++) {
            if (searchIDs[i] == transID) {
                index = i;

                break;
            }
        }

        // error - unexpected transaction index
        if (index == -1) {
            System.err.println("Unexpected transaction index: " + transID);
            // process the error case here
        } else {
            searchIDs[index] = -1;
        }

        /*
         * Actually, we do not care about the response code -
         * if device is not reachable or no records, etc.
         */

        // make sure it was the last transaction
        for (int i = 0; i < searchIDs.length; i++) {
            if (searchIDs[i] != -1) {
                return;
            }
        }

        // ok, all of the transactions are completed
        synchronized (this) {
            notify();
        }
    }

    /** Sets the request to search the devices/services. */
    void requestSearch() {
        synchronized (this) {
            notify();
        }
    }

    /** Cancel's the devices/services search. */
    void cancelSearch() {
        synchronized (this) {
            if (state == DEVICE_SEARCH) {
                discoveryAgent.cancelInquiry(this);
            } else if (state == SERVICE_SEARCH) {
                for (int i = 0; i < searchIDs.length; i++) {
                    discoveryAgent.cancelServiceSearch(searchIDs[i]);
                }
            }
        }
    }

    /** Sets the request to load the specified image. */
    void requestLoad(String name) {
        synchronized (this) {
            imageNameToLoad = name;
            notify();
        }
    }

    /** Cancel's the image download. */
    void cancelLoad() {
        /*
         * The image download process is done by
         * this class's thread (not by a system one),
         * so no need to wake up the current thread -
         * it's running already.
         */
        isDownloadCanceled = true;
    }

    /**
     * Destroy a work with bluetooth - exits the accepting
     * thread and close notifier.
     */
    void destroy() {
        synchronized (this) {
            isClosed = true;
            isDownloadCanceled = true;
            notify();
        }

        // wait for acceptor thread is done
        try {
            processorThread.join();
        } catch (InterruptedException e) {
        } // ignore
    }

    /**
     * Processes images search/download until component is closed
     * or system error has happen.
     */
    private synchronized void processImagesSearchDownload() {
        while (!isClosed) {
            // wait for new search request from user
            state = READY;

            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Unexpected interruption: " + e);

                return;
            }

            // check the component is destroyed
            if (isClosed) {
                return;
            }

            // search for devices
            if (!searchDevices()) {
                return;
            } else if (devices.size() == 0) {
                continue;
            }

            // search for services now
            if (!searchServices()) {
                return;
            } else if (records.size() == 0) {
                continue;
            }

            // ok, something was found - present the result to user now
            if (!presentUserSearchResults()) {
                // services are found, but no names there
                continue;
            }

            // the several download requests may be processed
            while (true) {
                // this download is not canceled, right?
                isDownloadCanceled = false;

                // ok, wait for download or need to wait for next search
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Unexpected interruption: " + e);

                    return;
                }

                // check the component is destroyed
                if (isClosed) {
                    return;
                }

                // this means "go to the beginning"
                if (imageNameToLoad == null) {
                    break;
                }

                // load selected image data
                Image img = loadImage();

                // this should never happen - monitor is taken...
                if (isClosed) {
                    return;
                }

                if (isDownloadCanceled) {
                    continue; // may be next image to be download
                }

                if (img == null) {
                    parent.informLoadError("Can't load image: " + imageNameToLoad);

                    continue; // may be next image to be download
                }

                // ok, show image to user
                parent.showImage(img, imageNameToLoad);

                // may be next image to be download
                continue;
            }
        }
    }

    /**
     * Search for bluetooth devices.
     *
     * @return false if should end the component work.
     */
    private boolean searchDevices() {
        // ok, start a new search then
        state = DEVICE_SEARCH;
        devices.removeAllElements();

        try {
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
        } catch (BluetoothStateException e) {
            System.err.println("Can't start inquiry now: " + e);
            parent.informSearchError("Can't start device search");

            return true;
        }

        try {
            wait(); // until devices are found
        } catch (InterruptedException e) {
            System.err.println("Unexpected interruption: " + e);

            return false;
        }

        // this "wake up" may be caused by 'destroy' call
        if (isClosed) {
            return false;
        }

        // no?, ok, let's check the return code then
        switch (discType) {
        case INQUIRY_ERROR:
            parent.informSearchError("Device discovering error...");

        // fall through
        case INQUIRY_TERMINATED:
            // make sure no garbage in found devices list
            devices.removeAllElements();

            // nothing to report - go to next request
            break;

        case INQUIRY_COMPLETED:

            if (devices.size() == 0) {
                parent.informSearchError("No devices in range");
            }

            // go to service search now
            break;

        default:
            // what kind of system you are?... :(
            System.err.println("system error:" + " unexpected device discovery code: " + discType);
            destroy();

            return false;
        }

        return true;
    }

    /**
     * Search for proper service.
     *
     * @return false if should end the component work.
     */
    private boolean searchServices() {
        state = SERVICE_SEARCH;
        records.removeAllElements();
        searchIDs = new int[devices.size()];

        boolean isSearchStarted = false;

        for (int i = 0; i < devices.size(); i++) {
            RemoteDevice rd = (RemoteDevice)devices.elementAt(i);

            try {
                searchIDs[i] = discoveryAgent.searchServices(attrSet, uuidSet, rd, this);
            } catch (BluetoothStateException e) {
                System.err.println("Can't search services for: " + rd.getBluetoothAddress() +
                    " due to " + e);
                searchIDs[i] = -1;

                continue;
            }

            isSearchStarted = true;
        }

        // at least one of the services search should be found
        if (!isSearchStarted) {
            parent.informSearchError("Can't search services.");

            return true;
        }

        try {
            wait(); // until services are found
        } catch (InterruptedException e) {
            System.err.println("Unexpected interruption: " + e);

            return false;
        }

        // this "wake up" may be caused by 'destroy' call
        if (isClosed) {
            return false;
        }

        // actually, no services were found
        if (records.size() == 0) {
            parent.informSearchError("No proper services were found");
        }

        return true;
    }

    /**
     * Gets the collection of the images titles (names)
     * from the services, prepares a hashtable to match
     * the image name to a services list, presents the images names
     * to user finally.
     *
     * @return false if no names in found services.
     */
    private boolean presentUserSearchResults() {
        base.clear();

        for (int i = 0; i < records.size(); i++) {
            ServiceRecord sr = (ServiceRecord)records.elementAt(i);

            // get the attribute with images names
            DataElement de = sr.getAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID);

            if (de == null) {
                System.err.println("Unexpected service - missed attribute");

                continue;
            }

            // get the images names from this attribute
            Enumeration deEnum = (Enumeration)de.getValue();

            while (deEnum.hasMoreElements()) {
                de = (DataElement)deEnum.nextElement();

                String name = (String)de.getValue();

                // name may be stored already
                Object obj = base.get(name);

                // that's either the ServiceRecord or Vector
                if (obj != null) {
                    Vector v;

                    if (obj instanceof ServiceRecord) {
                        v = new Vector();
                        v.addElement(obj);
                    } else {
                        v = (Vector)obj;
                    }

                    v.addElement(sr);
                    obj = v;
                } else {
                    obj = sr;
                }

                base.put(name, obj);
            }
        }

        return parent.showImagesNames(base);
    }

    /**
     * Loads selected image data.
     */
    private Image loadImage() {
        if (imageNameToLoad == null) {
            System.err.println("Error: imageNameToLoad=null");

            return null;
        }

        // ok, get the list of service records
        ServiceRecord[] sr = null;
        Object obj = base.get(imageNameToLoad);

        if (obj == null) {
            System.err.println("Error: no record for: " + imageNameToLoad);

            return null;
        } else if (obj instanceof ServiceRecord) {
            sr = new ServiceRecord[] { (ServiceRecord)obj };
        } else {
            Vector v = (Vector)obj;
            sr = new ServiceRecord[v.size()];

            for (int i = 0; i < v.size(); i++) {
                sr[i] = (ServiceRecord)v.elementAt(i);
            }
        }

        // now try to load the image from each services one by one
        for (int i = 0; i < sr.length; i++) {
            StreamConnection conn = null;
            String url = null;

            // the process may be canceled
            if (isDownloadCanceled) {
                return null;
            }

            // first - connect
            try {
                url = sr[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                conn = (StreamConnection)Connector.open(url);
            } catch (IOException e) {
                System.err.println("Note: can't connect to: " + url);

                // ignore
                continue;
            }

            // then open a steam and write a name
            try {
                OutputStream out = conn.openOutputStream();
                out.write(imageNameToLoad.length()); // length is 1 byte
                out.write(imageNameToLoad.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                System.err.println("Can't write to server for: " + url);

                // close stream connection
                try {
                    conn.close();
                } catch (IOException ee) {
                } // ignore

                continue;
            }

            // then open a steam and read an image
            byte[] imgData = null;

            try {
                InputStream in = conn.openInputStream();

                // read a length first
                int length = in.read() << 8;
                length |= in.read();

                if (length <= 0) {
                    throw new IOException("Can't read a length");
                }

                // read the image now
                imgData = new byte[length];
                length = 0;

                while (length != imgData.length) {
                    int n = in.read(imgData, length, imgData.length - length);

                    if (n == -1) {
                        throw new IOException("Can't read a image data");
                    }

                    length += n;
                }

                in.close();
            } catch (IOException e) {
                System.err.println("Can't read from server for: " + url);

                continue;
            } finally {
                // close stream connection anyway
                try {
                    conn.close();
                } catch (IOException e) {
                } // ignore
            }

            // ok, may it's a chance
            Image img = null;

            try {
                img = Image.createImage(imgData, 0, imgData.length);
            } catch (Exception e) {
                // may be next time
                System.err.println("Error: wrong image data from: " + url);

                continue;
            }

            return img;
        }

        return null;
    }
} // end of class 'BTImageClient' definition
