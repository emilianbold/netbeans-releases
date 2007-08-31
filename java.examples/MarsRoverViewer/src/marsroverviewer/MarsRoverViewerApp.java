/*
 * Copyright (c) 2007, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 

/*
 * MarsRoverViewerApp.java
 */

package marsroverviewer;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * This example highlights the importance of background tasks by
 * downloading some very large Mars rover images from JPL's
 * photojournal web site.  There are about a dozen images, most with
 * 10-15M pixels.  Clicking the next/prev buttons (or control-N,P)
 * cancels the current download and starts loading a new image.  The
 * stop button also cancels the current download.  The list of images
 * is defined in the startup() method.  The first image is shown by
 * the application's ready() method.
 * <p>
 * More images of Mars can be found here: 
 * <a href="http://photojournal.jpl.nasa.gov/target/Mars">
 * http://photojournal.jpl.nasa.gov/target/Mars</a>.  Some of the
 * MER images are quite large (like this 22348x4487 whopper,
 * http://photojournal.jpl.nasa.gov/jpeg/PIA06917.jpg) and can't
 * be loaded without reconfiguring the Java heap parameters.
 * <p>
 * This file contains the main class of the application. It extends the 
 * Swing Application Framework's {@code SingleFrameApplication} class 
 * and therefore takes care or simplifies things like 
 * loading resources and saving the session state.
 *
 * This class calls the {@code MarsRoverViewerView} class, which 
 * contains the code for constructing the user interface and 
 * much of the application logic.
 * 
 */
public class MarsRoverViewerApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        // create URLs for a set of selected images
        String imageDir = "http://photojournal.jpl.nasa.gov/jpeg/";
	String[] imageNames = {
	    "PIA03171", "PIA02652", "PIA05108", "PIA02696",
	    "PIA05049", "PIA05460", "PIA07327", "PIA05117", 
	    "PIA05199", "PIA05990", "PIA03623"
	};
	List<URL> imageLocations = new ArrayList<URL>(imageNames.length);
	for(String imageName : imageNames) {
            String path = imageDir + imageName + ".jpg";
            try {
                URL url = new URL(path);
                imageLocations.add(url);
            }
            catch (MalformedURLException e) {
                Logger.getLogger(MarsRoverViewerApp.class.getName()).log(Level.WARNING, "bad image URL " + path, e);
            }
        }
        // create and show the application's main window
        show(new MarsRoverViewerView(this, imageLocations));
    }

    /**
     * Runs after the startup has completed and the GUI is up and ready.
     * We show the first image here, rather than initializing it at startup
     * time, so loading the first image doesn't impede getting the 
     * GUI visible.
     */
    @Override protected void ready() {
        Action refreshAction = getContext().getActionMap(
                MarsRoverViewerView.class, getMainView())
                .get("refreshImage");
        refreshAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of MarsRoverViewerApp
     */
    public static MarsRoverViewerApp getApplication() {
        return Application.getInstance(MarsRoverViewerApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(MarsRoverViewerApp.class, args);
    }
}
