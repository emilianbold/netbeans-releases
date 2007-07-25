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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.uihandler.api;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.modules.uihandler.Installer;
import org.netbeans.modules.uihandler.UIHandler;

/** Class that allows other modules to control the behaviour of the UI
 * Gestures submit process.
 *
 * @author Jaroslav Tulach
 */
public final class Controller {
    private static final Controller INSTANCE = new Controller();

    private Controller() {
    }
    
    /** @return the controller instance */
    public static Controller getDefault() {
        return INSTANCE;
    }

    /** Controls exception reporting. Either enables or disables it.
     * @param enable enable or disable.
     * @since 2.0
     */
    public void setEnableExceptionHandler(boolean enable) {
        UIHandler.registerExceptionHandler(enable);
    }
    
    /** Getter for the number of collected log records
     * @return the number of currently
     * @since 2.0
     */
    public int getLogRecordsCount() {
        return Installer.getLogsSize();
    }
    
    /** Are logs automatically send to server when the local buffer gets full?
     * @return true if automatic submit is enabled
     */
    public boolean isAutomaticSubmit() {
        return Installer.isHintsMode();
    }
    
    /** If the automatic mode is on, this method returns the URL that has been
     * returned when data were transmitted to the server last time.
     * @return null or URL with "hints"
     */
    public URL getHintsURL() {
        return Installer.hintsURL();
    }
    
    /**
     * Adds listener for various properties
     * @param l 
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        UIHandler.SUPPORT.addPropertyChangeListener(l);
    }
    
    /**
     * Removes property change listener. 
     *
     * @param l 
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        UIHandler.SUPPORT.removePropertyChangeListener(l);
    }
    
    /** Explicitly invoke the submit data procedure. The method returns
     * immediatelly, then it opens a dialog and asks the user whether he
     * wants to submit the data.
     * @since 2.0
     */
    public void submit() {
        new ExplicitSubmit();
    }

    private static class ExplicitSubmit implements Runnable {
        public ExplicitSubmit() {
            Installer.RP.post(this);
        }
        
        public void run() {
            Installer.displaySummary("WELCOME_URL", true, false, true); // NOI18N
        }
    }
}
