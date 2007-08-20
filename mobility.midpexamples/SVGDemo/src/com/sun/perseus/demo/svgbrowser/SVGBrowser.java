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
package com.sun.perseus.demo.svgbrowser;

import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableImage;
import javax.microedition.midlet.MIDlet;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGSVGElement;


/**
 * This demo implements a simple file browser which allows opening SVG images
 * and animations.
 */
public final class SVGBrowser extends MIDlet implements CommandListener, SVGEventListener {
    /*
     * Animation state constants.
     */

    /** State representing stopped animation. */
    private static final int STATE_STOPPED = 0;

    /** State representing paused animation. */
    private static final int STATE_PAUSED = 1;

    /** State representing running animation. */
    private static final int STATE_PLAYING = 2;

    /** Command for exiting the MIDlet. */
    private final Command exitCommand;

    /** Command for returning from a displayed SVG image. */
    private final Command backCommand;

    /** Command for entering directory or opening SVG file. */
    private final Command openCommand;

    /** Command for starting a SVG animation. */
    private final Command playCommand;

    /** Command for pausing a SVG animation. */
    private final Command pauseCommand;

    /** Command for stopping a SVG animation. */
    private final Command stopCommand;

    /** Screen with directory / SVG file listing. */
    private final List browserList;

    /** The loaded SVG image. */
    private SVGImage svgImage;

    /** The root <svg> element of the image. */
    private SVGSVGElement svgRoot;

    /** The animator created for the image. */
    private SVGAnimator svgAnimator;

    /** The canvas from the animator. */
    private Canvas svgCanvas;

    /** The current animation state. */
    private int animationState = STATE_STOPPED;

    /** The next animation state. */
    private int animationNextState = STATE_STOPPED;

    /** The display assigned for the MIDlet. */
    private final Display display;

    /** The parent path of the current path. */
    private String parentPath;

    /** The browsing depth. Value 0 means a virtual root of all filesystems. */
    private int browsingDepth;

    /** The current path. */
    private String currentPath;

    /** Creates a new instance of SVGBrowser. */
    public SVGBrowser() {
        browserList = new List(null, Choice.IMPLICIT);

        exitCommand = new Command("Exit", Command.EXIT, 1);
        backCommand = new Command("Back", Command.BACK, 1);
        openCommand = new Command("Open", Command.OK, 1);

        playCommand = new Command("Play", Command.SCREEN, 1);
        pauseCommand = new Command("Pause", Command.SCREEN, 1);
        stopCommand = new Command("Stop", Command.SCREEN, 2);

        browserList.addCommand(exitCommand);
        browserList.addCommand(openCommand);
        browserList.setSelectCommand(openCommand);
        browserList.setCommandListener(this);

        display = Display.getDisplay(this);
        new BrowseAction("file:///", 0, null).start();
    }

    protected void startApp() {
    }

    protected void pauseApp() {
    }

    protected synchronized void destroyApp(boolean unconditional) {
        if ((svgAnimator != null) && (animationState != STATE_STOPPED)) {
            svgAnimator.stop();
            animationState = STATE_STOPPED;
            animationNextState = STATE_STOPPED;
        }
    }

    public synchronized void commandAction(Command c, Displayable d) {
        if ((c == exitCommand) || (c == Alert.DISMISS_COMMAND)) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == openCommand) {
            int selectedIdx = browserList.getSelectedIndex();

            if ((selectedIdx == 0) && (browsingDepth > 0)) {
                new BrowseAction(parentPath, 
                                 browsingDepth - 1, 
                                 browserList).start();
            } else {
                String selectedString = browserList.getString(selectedIdx);

                if (selectedString.endsWith("/")) {
                    // open the selected directory
                    new BrowseAction(currentPath + selectedString,
                                     browsingDepth + 1, 
                                     browserList).start();
                } else {
                    // open the selected svg file
                    new OpenAction(currentPath + selectedString, 
                                   browserList).start();
                }
            }
        } else if (c == backCommand) {
            display.setCurrent(browserList);
        } else if (c == playCommand) {
            play();
        } else if (c == pauseCommand) {
            pause();
        } else if (c == stopCommand) {
            stop();
        }
    }

    /**
     * Opens browser for the given path. If an error happens while reading
     * the directory content, an alert with the error message is displayed.
     * After timeout this alert is dismissed automatically and the
     * prevDisplayable is shown. If prevDisplayable is null and an error happens
     * the MIDlet is ended after the alert timeouts.
     *
     * @param newPath the path to browse
     * @param newDepth the browsing depth, 0 means "root"     
     * @param prevDisplayable the Displayable to be shown in the case of error
     */
    private void browse(String newPath, int newDepth, 
                        Displayable prevDisplayable) {
        try {
            String newParentPath;
            Vector directories = new Vector();
            Vector svgFiles = new Vector();

            if (newDepth == 0) {
                newParentPath = null;
                
                // get filesystems listing
                Enumeration roots = FileSystemRegistry.listRoots();
                if (!roots.hasMoreElements()) {
                    // this is fatal, no filesystems enumerated
                    handleError("No filesystems found", prevDisplayable);
                    return;            
                }
                
                do {
                    directories.addElement(roots.nextElement());
                } while (roots.hasMoreElements());
                
            } else {
                int slashIndex = newPath.lastIndexOf('/', newPath.length() - 2);
                // slashIndex != -1
                newParentPath = newPath.substring(0, slashIndex + 1);

                // get directory listing
                FileConnection fc = 
                        (FileConnection)Connector.open(newPath, Connector.READ);
                try {
                    Enumeration files = fc.list();

                    while (files.hasMoreElements()) {
                        String fileName = (String)files.nextElement();

                        if (fileName.endsWith("/")) {
                            directories.addElement(fileName);

                            continue;
                        }

                        int dotIndex = fileName.lastIndexOf('.');

                        if (dotIndex != -1) {
                            String extension = fileName.substring(dotIndex + 1);

                            if ("svg".equalsIgnoreCase(extension)) {
                                svgFiles.addElement(fileName);
                            }
                        }
                    }
                } finally {
                    fc.close();
                }
            }

            synchronized (this) {
                browserList.setTitle(newPath);
                browserList.deleteAll();

                if (newDepth > 0) {
                    browserList.append("..", null);
                }

                Enumeration e;
                // add directories
                e = directories.elements();

                while (e.hasMoreElements()) {
                    browserList.append((String)e.nextElement(), null);
                }

                // add svg files
                e = svgFiles.elements();

                while (e.hasMoreElements()) {
                    browserList.append((String)e.nextElement(), null);
                }

                parentPath = newParentPath;
                browsingDepth = newDepth;
                currentPath = newPath;

                display.setCurrent(browserList);
            }
        } catch (IOException e) {
            handleError("Failed to open " + newPath, prevDisplayable);
        }
    }

    /**
     * Opens an SVG file and shows on the screen. If an error happens while
     * opening the file, an alert with the error message is displayed.
     * After timeout this alert is dismissed automatically and the
     * prevDisplayable is shown. If prevDisplayable is null and an error happens
     * the MIDlet is ended after the alert timeouts.
     *
     * @param svgPath the path to the SVG file
     * @param prevDisplayable the Displayable to be shown in the case of error
     */
    private void open(String svgPath, Displayable prevDisplayable) {
        try {
            SVGImage newSVGImage;
            FileConnection fc = (FileConnection)Connector.open(svgPath,
                                                               Connector.READ);

            try {
                InputStream is = fc.openInputStream();

                try {
                    newSVGImage = (SVGImage)ScalableImage.createImage(is, null);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            } finally {
                fc.close();
            }

            Document newSVGDocument = newSVGImage.getDocument();
            SVGSVGElement newSVGRoot = (SVGSVGElement)newSVGDocument.getDocumentElement();
            SVGAnimator newSVGAnimator = SVGAnimator.createAnimator(newSVGImage);

            synchronized (this) {
                if ((svgAnimator != null) && (animationState != STATE_STOPPED)) {
                    svgAnimator.stop();
                }

                svgImage = newSVGImage;
                svgRoot = newSVGRoot;

                svgAnimator = newSVGAnimator;
                // Set to 10 fps (frames per second)
                svgAnimator.setTimeIncrement(0.01f);
                svgAnimator.setSVGEventListener(this);

                svgCanvas = (Canvas)svgAnimator.getTargetComponent();
                svgCanvas.setTitle(svgPath);
                svgCanvas.addCommand(backCommand);
                svgCanvas.setCommandListener(this);

                animationState = STATE_STOPPED;
                animationNextState = STATE_PLAYING;

                display.setCurrent(svgCanvas);
            }
        } catch (IOException e) {
            handleError("Failed to open " + svgPath, prevDisplayable);
        }
    }

    /**
     * Handles error conditions. It shows an Alert with the error message.
     * After the Alert timeouts the behaviour depends on the value of
     * prevDisplayable. If prevDisplayable is not null it is displayed,
     * otherwise the MIDlet exits.
     *
     * @param message the error message
     * @param prevDisplayable the Displayable to be shown after the timeout
     */
    private void handleError(String message, Displayable prevDisplayable) {
        if (prevDisplayable == null) {
            errorExit(message);
        } else {
            warningMessage(message, prevDisplayable);
        }
    }

    /**
     * Displays an alert with the given error message. When the alert timeouts
     * the current MIDlet exits.
     *
     * @param message the error message
     */
    private void errorExit(String message) {
        Alert errorAlert = new Alert("Fatal error", message, null, 
                                     AlertType.ERROR);
        errorAlert.setCommandListener(this);

        synchronized (this) {
            display.setCurrent(errorAlert);
        }
    }

    /**
     * Displays an alert with the given warning message. When the alert timeouts
     * the given Displayable is displayed.
     *
     * @param message the warning message
     * @param nextDisplayable the next Displayable to display
     */
    private void warningMessage(String message, Displayable nextDisplayable) {
        Alert warningAlert = new Alert("Warning", message, null, AlertType.WARNING);

        synchronized (this) {
            display.setCurrent(warningAlert, nextDisplayable);
        }
    }

    public void keyPressed(int keyCode) {
    }

    public void keyReleased(int keyCode) {
    }

    public void pointerPressed(int x, int y) {
    }

    public void pointerReleased(int x, int y) {
    }

    public synchronized void hideNotify() {
        if (animationState == STATE_PLAYING) {
            svgAnimator.pause();
            animationState = STATE_PAUSED;
            animationNextState = STATE_PLAYING;
        }
    }

    public synchronized void showNotify() {
        svgImage.setViewportWidth(svgCanvas.getWidth());
        svgImage.setViewportHeight(svgCanvas.getHeight());

        if (animationState != animationNextState) {
            switch (animationNextState) {
            case STATE_PLAYING:
                svgAnimator.play();

                break;

            case STATE_PAUSED:
                svgAnimator.pause();

                break;

            case STATE_STOPPED:
                svgAnimator.stop();

                break;
            }

            animationState = animationNextState;
            updateAnimatorCommands();
        }
    }

    /**
     * Updates the SVG image viewport size when the canvas size changes.
     */
    public synchronized void sizeChanged(int width, int height) {
        svgImage.setViewportWidth(width);
        svgImage.setViewportHeight(height);
    }

    /**
     * Updates the available commands in the menu according to the current
     * animation state.
     */
    private void updateAnimatorCommands() {
        svgCanvas.removeCommand(playCommand);
        svgCanvas.removeCommand(pauseCommand);
        svgCanvas.removeCommand(stopCommand);

        switch (animationState) {
        case STATE_PLAYING:
            svgCanvas.addCommand(pauseCommand);
            svgCanvas.addCommand(stopCommand);

            break;

        case STATE_PAUSED:
            svgCanvas.addCommand(playCommand);
            svgCanvas.addCommand(stopCommand);

            break;

        case STATE_STOPPED:
            svgCanvas.addCommand(playCommand);

            break;
        }
    }

    /**
     * This methods starts the current animation.
     */
    private void play() {
        if ((animationState == animationNextState) && (animationState != STATE_PLAYING)) {
            svgAnimator.play();
            animationState = STATE_PLAYING;
            updateAnimatorCommands();
        }

        animationNextState = STATE_PLAYING;
    }

    /**
     * This methods pauses the current animation.
     */
    private void pause() {
        if ((animationState == animationNextState) && (animationState != STATE_PAUSED)) {
            svgAnimator.pause();
            animationState = STATE_PAUSED;
            updateAnimatorCommands();
        }

        animationNextState = STATE_PAUSED;
    }

    /**
     * This methods stops the current animation.
     */
    private void stop() {
        if ((animationState == animationNextState) && (animationState != STATE_STOPPED)) {
            svgAnimator.stop();
            svgRoot.setCurrentTime(0);
            animationState = STATE_STOPPED;
            updateAnimatorCommands();
        }

        animationNextState = STATE_STOPPED;
    }

    private final class BrowseAction extends Thread {
        private final String directory;
        private final int depth;
        private final Displayable prevDisplayable;

        public BrowseAction(String directory, int depth, 
                            Displayable prevDisplayable) {
            this.directory = directory;
            this.depth = depth;
            this.prevDisplayable = prevDisplayable;
        }

        public void run() {
            browse(directory, depth, prevDisplayable);
        }
    }

    private final class OpenAction extends Thread {
        private final String svgFile;
        private final Displayable prevDisplayable;

        public OpenAction(String svgFile, Displayable prevDisplayable) {
            this.svgFile = svgFile;
            this.prevDisplayable = prevDisplayable;
        }

        public void run() {
            open(svgFile, prevDisplayable);
        }
    }
}
