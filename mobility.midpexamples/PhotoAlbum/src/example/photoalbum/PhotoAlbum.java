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
package example.photoalbum;

import java.io.DataInputStream;
import java.io.IOException;

import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;


/**
 * The PhotoAlbum MIDlet provides the commands and screens
 * that implement a simple photograph and animation album.
 * The images and animations to be displayed are configured
 * in the descriptor file with attributes.
 * <p>
 * Options are provided to to vary the speed of display
 * and the frame style.
 *
 */
public class PhotoAlbum extends MIDlet implements CommandListener, ItemStateListener, Runnable {
    /** The Command object for the Exit command */
    private Command exitCommand;

    /** The Command object for the Ok command */
    private Command okCommand;

    /** The Command object for the Options command */
    private Command optionsCommand;

    /** The Command object for the Back command */
    private Command backCommand;

    /** The Command object for the Cancel command */
    private Command cancelCommand;

    /** The Form object for the Progress form */
    private Form progressForm;

    /** The Gauge object for the Progress gauge */
    private Gauge progressGauge;

    /** The Form object for the Options command */
    private Form optionsForm;

    /** Set of choices for the border styles */
    private ChoiceGroup borderChoice;

    /** Set of choices for the speeds */
    private ChoiceGroup speedChoice;

    /** The current display for this MIDlet */
    private Display display;

    /** The PhotoFrame that displays images */
    private PhotoFrame frame;

    /** The Alert for messages */
    private Alert alert;

    /** Contains Strings with the image names */
    private Vector imageNames;

    /** List of Image titles for user to select */
    private List imageList;

    /** Name of current image, may be null */
    private String imageName;

    /** Current thread loading images, may be null */
    private Thread thread;

    /** Name of persistent storage */
    private final String optionsName = "PhotoAlbum";

    /** Persistent storage for options */
    private RecordStore optionsStore;
    private boolean firstTime = true;

    /**
     * Construct a new PhotoAlbum MIDlet and initialize the base options
     * and main PhotoFrame to be used when the MIDlet is started.
     */
    public PhotoAlbum() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        optionsCommand = new Command("Options", Command.SCREEN, 1);
        okCommand = new Command("Ok", Command.OK, 3);
        backCommand = new Command("Back", Command.BACK, 3);
        cancelCommand = new Command("Cancel", Command.CANCEL, 1);

        frame = new PhotoFrame();
        frame.setStyle(2);
        frame.setSpeed(2);
        frame.addCommand(optionsCommand);
        frame.addCommand(backCommand);
        frame.setCommandListener(this);
        alert = new Alert("Warning");
        setupImageList();
        firstTime = true;
    }

    /**
     * Start up the Hello MIDlet by setting the PhotoFrame
     * and loading the initial images.
     */
    protected void startApp() {
        if (firstTime) {
            if (imageList.size() > 0) {
                display.setCurrent(imageList);
                openOptions();
                restoreOptions();
            } else {
                alert.setString("No images configured.");
                display.setCurrent(alert, imageList);
            }

            firstTime = false;
        }

        openOptions();
        restoreOptions();
    }

    /**
     * Pause is used to release the memory used by Image.
     * When restarted the images will be re-created.
     * Save the options for the next restart.
     */
    protected void pauseApp() {
        saveOptions();
    }

    /**
     * Destroy must cleanup everything not handled by the garbage collector.
     * In this case there is nothing to cleanup.
     * Save the options for the next restart.
     * @param unconditional true if this MIDlet should always cleanup
     */
    protected void destroyApp(boolean unconditional) {
        saveOptions();
        frame.reset(); // Discard images cached in the frame.
        saveOptions();
        closeOptions();
    }

    /**
     * Respond to commands. Commands are added to each screen as
     * they are created.  Each screen uses the PhotoAlbum MIDlet as the
     * CommandListener. All commands are handled here:
     * <UL>
     * <LI>Select on Image List - display the progress form and start the thread
     *  to read in the images.
     * <LI>Options - display the options form.
     * <LI>Ok on the Options form - returns to the PhotoFrame.
     * <LI>Back - display the Image List, deactivating the PhotoFrame.
     * <LI>Cancel - display the image List and stop the thread loading images.
     * <LI>Exit - images are released and notification is given that the MIDlet
     *  has exited.
     * </UL>
     * @param c the command that triggered this callback
     * @param s the screen that contained the command
     */
    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            // Cleanup and notify that the MIDlet has exited
            destroyApp(false);
            notifyDestroyed();
        } else if (c == optionsCommand) {
            // Display the options form
            display.setCurrent(genOptions());
        } else if ((c == okCommand) && (s == optionsForm)) {
            // Return to the PhotoFrame, the option values have already
            // been saved by the item state listener
            display.setCurrent(frame);
        } else if (c == List.SELECT_COMMAND) {
            // Display the progress screen and
            // start the thread to read the images
            int i = imageList.getSelectedIndex();
            imageName = (String)imageNames.elementAt(i);
            display.setCurrent(genProgress(imageList.getString(i)));
            thread = new Thread(this);
            thread.start();
        } else if (c == backCommand) {
            // Display the list of images.
            display.setCurrent(imageList);
        } else if (c == cancelCommand) {
            // Signal thread to stop and put an alert.
            thread = null;
            alert.setString("Loading images cancelled.");
            display.setCurrent(alert, imageList);
        } 
    }

    /**
     * Listener for changes to options.
     * The new values are set in the PhotoFrame.
     * @param item - the item whose value has changed.
     */
    public void itemStateChanged(Item item) {
        if (item == borderChoice) {
            frame.setStyle(borderChoice.getSelectedIndex());
        } else if (item == speedChoice) {
            frame.setSpeed(speedChoice.getSelectedIndex());
        }
    }

    /**
     * Generate the options form with speed and style choices.
     * Speed choices are stop, slow, medium, and fast.
     * Style choices for borders are none, plain, fancy.
     * @return the generated options Screen
     */
    private Screen genOptions() {
        if (optionsForm == null) {
            optionsForm = new Form("Options");
            optionsForm.addCommand(okCommand);
            optionsForm.setCommandListener(this);
            optionsForm.setItemStateListener(this);

            speedChoice = new ChoiceGroup("Speed", Choice.EXCLUSIVE);
            speedChoice.append("Stop", null);
            speedChoice.append("Slow", null);
            speedChoice.append("Medium", null);
            speedChoice.append("Fast", null);
            speedChoice.append("Unlimited", null);
            speedChoice.setSelectedIndex(frame.getSpeed(), true);
            optionsForm.append(speedChoice);

            borderChoice = new ChoiceGroup("Borders", Choice.EXCLUSIVE);
            borderChoice.append("None", null);
            borderChoice.append("Plain", null);
            borderChoice.append("Fancy", null);
            borderChoice.setSelectedIndex(frame.getStyle(), true);
            optionsForm.append(borderChoice);
        }

        return optionsForm;
    }

    /**
     * Generate the options form with image title and progress gauge.
     * @param name the title of the Image to be loaded.
     * @return the generated progress screen
     */
    private Screen genProgress(String name) {
        if (progressForm == null) {
            progressForm = new Form(name);
            progressForm.addCommand(cancelCommand);
            progressForm.setCommandListener(this);

            progressGauge = new javax.microedition.lcdui.Gauge("Loading images...", false, 9, 0);

            progressForm.append(progressGauge);
        } else {
            progressGauge.setValue(0);
            progressForm.setTitle(name);
        }

        return progressForm;
    }

    /**
     * Check the attributes in the descriptor that identify
     * images and titles and initialize the lists of imageNames
     * and imageList.
     * <P>
     * The attributes are named "PhotoTitle-n" and "PhotoImage-n".
     * The value "n" must start at "1" and increment by 1.
     */
    private void setupImageList() {
        imageNames = new Vector();
        imageList = new List("Images", List.IMPLICIT);
        imageList.addCommand(exitCommand);
        imageList.setCommandListener(this);

        for (int n = 1; n < 100; n++) {
            String nthImage = "PhotoImage-" + n;
            String image = getAppProperty(nthImage);

            if ((image == null) || (image.length() == 0)) {
                break;
            }

            String nthTitle = "PhotoTitle-" + n;
            String title = getAppProperty(nthTitle);

            if ((title == null) || (title.length() == 0)) {
                title = image;
            }

            imageNames.addElement(image);
            imageList.append(title, null);
        }

        imageNames.addElement("testchart:");
        imageList.append("Test Chart", null);
    }

    /**
     * The Run method is used to load the images.
     * A form is used to report the progress of loading images
     * and when the loading is complete they are displayed.
     * Any errors that occur are reported using an Alert.
     * Images previously loaded into the PhotoFrame are discarded
     * before loading.
     * <P>
     * Load images from resource files using <code>Image.createImage</code>.
     * Images may be in resource files or accessed using http:
     * The first image is loaded to determine whether it is a
     * single image or a sequence of images and to make sure it exists.
     * If the name given is the complete name of the image then
     * it is a singleton.
     * Otherwise it is assumed to be a sequence of images
     * with the name as a prefix.  Sequence numbers (n) are
     * 0, 1, 2, 3, ....  The full resource name is the concatenation
     * of name + n + ".png".
     * <p>
     * If an OutOfMemoryError occurs the sequence of images is truncated
     * and an alert is used to inform the user. The images loaded are
     * displayed.
     * @see createImage
     */
    public void run() {
        Thread mythread = Thread.currentThread();
        Vector images = new Vector(5);
        /* Free images and resources used by current frame. */
        frame.reset();

        try { // Catch OutOfMemory Errors

            try {
                if (imageName.startsWith("testchart:")) {
                    TestChart t = new TestChart(frame.getWidth(), frame.getHeight());
                    images = t.generateImages();
                } else {
                    // Try the name supplied for the single image case.
                    images.addElement(createImage(imageName));
                }
            } catch (IOException ex) {
                try {
                    int namelen = imageName.length();
                    StringBuffer buf = new StringBuffer(namelen + 8);
                    buf.append(imageName);

                    Runtime rt = Runtime.getRuntime();

                    // Try for a sequence of images.
                    for (int i = 0;; i++) {
                        progressGauge.setValue(i % 10);

                        // If cancelled, discard images and return immediately
                        if (thread != mythread) {
                            break;
                        }

                        // locate the next in the series of images.
                        buf.setLength(namelen);
                        buf.append(i);
                        buf.append(".png");

                        String name = buf.toString();
                        images.addElement(createImage(name));
                    }
                } catch (IOException io_ex) {
                }
            } catch (SecurityException se_ex) {
                // no-retry, just put up the alert
            }

            // If cancelled, discard images and return immediately
            if (thread != mythread) {
                return;
            }

            // If any images, setup the images and display them.
            if (images.size() > 0) {
                frame.setImages(images);
                display.setCurrent(frame);
            } else {
                // Put up an alert saying image cannot be loaded
                alert.setString("Images could not be loaded.");
                display.setCurrent(alert, imageList);
            }
        } catch (OutOfMemoryError err) {
            int size = images.size();

            if (size > 0) {
                images.setSize(size - 1);
            }

            // If cancelled, discard images and return immediately
            if (thread != mythread) {
                return;
            }

            alert.setString("Not enough memory for all images.");

            // If no images are loaded, Alert and return to the list
            // Otherwise, Alert and display the ones that were loaded.
            if (images.size() <= 0) {
                display.setCurrent(alert, imageList);
            } else {
                frame.setImages(images);
                display.setCurrent(alert, frame);
            }
        }
    }

    /**
     * Fetch the image.  If the name begins with "http:"
     * fetch it with connector.open and http.
     * If it starts with "/" then load it from the
     * resource file.
     * @param name of the image to load
     * @return image created
     * @exception IOException if errors occur during image loading
     */
    private Image createImage(String name) throws IOException {
        if (name.startsWith("/")) {
            // Load as a resource with Image.createImage
            return Image.createImage(name);
        } else if (name.startsWith("http:")) {
            // Load from a ContentConnection
            HttpConnection c = null;
            DataInputStream is = null;

            try {
                c = (HttpConnection)Connector.open(name);

                int status = c.getResponseCode();

                if (status != 200) {
                    throw new IOException("HTTP Response Code = " + status);
                }

                int len = (int)c.getLength();
                String type = c.getType();

                if (!type.equals("image/png") && !type.equals("image/jpeg")) {
                    throw new IOException("Expecting image, received " + type);
                }

                if (len > 0) {
                    is = c.openDataInputStream();

                    byte[] data = new byte[len];
                    is.readFully(data);

                    return Image.createImage(data, 0, len);
                } else {
                    throw new IOException("Content length is missing");
                }
            } finally {
                if (is != null) {
                    is.close();
                }

                if (c != null) {
                    c.close();
                }
            }
        } else {
            throw new IOException("Unsupported media");
        }
    }

    /**
     * Open the store that holds the saved options.
     * If an error occurs, put up an Alert.
     */
    void openOptions() {
        try {
            optionsStore = RecordStore.openRecordStore(optionsName, true);
        } catch (RecordStoreException ex) {
            alert.setString("Could not access options storage");
            display.setCurrent(alert);
            optionsStore = null;
        }
    }

    /**
     * Save the options to persistent storage.
     * The options are retrieved ChoiceGroups and stored
     * in Record 1 of the store which is reserved for it.
     * The two options are stored in bytes 0 and 1 of the record.
     */
    void saveOptions() {
        if (optionsStore != null) {
            byte[] options = new byte[2];
            options[0] = (byte)frame.getStyle();
            options[1] = (byte)frame.getSpeed();

            try {
                optionsStore.setRecord(1, options, 0, options.length);
            } catch (InvalidRecordIDException ridex) {
                // Record 1 did not exist, create a new record (Should be 1)
                try {
                    int rec = optionsStore.addRecord(options, 0, options.length);
                } catch (RecordStoreException ex) {
                    alert.setString("Could not add options record");
                    display.setCurrent(alert);
                }
            } catch (RecordStoreException ex) {
                alert.setString("Could not save options");
                display.setCurrent(alert);
            }
        }
    }

    /**
     * Restore the options from persistent storage.
     * The options are read from record 1 and set in
     * the frame and if the optionsForm has been created
     * in the respective ChoiceGroups.
     */
    void restoreOptions() {
        if (optionsStore != null) {
            try {
                byte[] options = optionsStore.getRecord(1);

                if (options.length == 2) {
                    frame.setStyle(options[0]);
                    frame.setSpeed(options[1]);

                    if (optionsForm != null) {
                        borderChoice.setSelectedIndex(options[0], true);
                        speedChoice.setSelectedIndex(options[1], true);
                    }

                    return; // Return all set
                }
            } catch (RecordStoreException ex) {
                // Ignore, use normal defaults
            }
        }
    }

    /**
     * Close the options store.
     */
    void closeOptions() {
        if (optionsStore != null) {
            try {
                optionsStore.closeRecordStore();
                optionsStore = null;
            } catch (RecordStoreException ex) {
                alert.setString("Could not close options storage");
                display.setCurrent(alert);
            }
        }
    }
}
