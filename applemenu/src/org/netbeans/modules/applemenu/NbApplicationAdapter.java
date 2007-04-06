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

package org.netbeans.modules.applemenu;

import com.apple.eawt.*;

import java.beans.Beans;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.Action;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;

/** Adapter class which intercepts action events and passes them to the
 * correct action instance as defined in the system filesystem.
 *
 * @author  Tim Boudreau
 */

class NbApplicationAdapter implements ApplicationListener {
    
    private static final String OPTIONS_ACTION = 
        "Actions/Window/org-netbeans-modules-options-OptionsWindowAction.instance"; //NOI18N
    private static final String ABOUT_ACTION = 
        "Actions/Help/org-netbeans-core-actions-AboutAction.instance"; //NOI18N
    private static final String OPENFILE_ACTION = 
        "Menu/File/org-netbeans-modules-openfile-OpenFileAction"; //NOI18N
    private static final String EXIT_ACTION = 
        "Actions/System/org-netbeans-core-actions-SystemExit.instance"; //NOI18N
    
    private static ApplicationListener al = null;
    
    private NbApplicationAdapter() {
    }

    static void install() {
        //Thanks to Scott Kovatch from Apple for this fix - enabling the preferences menu
        //requires that Beans.isDesignTime() be false
        boolean wasDesignTime = Beans.isDesignTime();
        
        try {
            Beans.setDesignTime (false);

            al = new NbApplicationAdapter();
            Application.getApplication().addApplicationListener(al);
            Application.getApplication().setEnabledAboutMenu(true);
            Application.getApplication().setEnabledPreferencesMenu(true);
        } finally {
            Beans.setDesignTime(wasDesignTime);
        }
    }

    static void uninstall() {
        if (al != null) {
            Application.getApplication().removeApplicationListener(al);
            al = null;
        }
    }
    
    public void handleAbout(ApplicationEvent e) {
        e.setHandled (performAction (ABOUT_ACTION));
    }
    
    public void handleOpenApplication (ApplicationEvent e) {
    }
    
    public void handleOpenFile (ApplicationEvent e) {
        e.setHandled(performAction (OPENFILE_ACTION, e.getFilename()));
    }
    
    public void handlePreferences (ApplicationEvent e) {
        e.setHandled(performAction(OPTIONS_ACTION));
    }
    
    public void handlePrintFile (ApplicationEvent e) {
        //do nothing - what invokes this?
    }
    
    public void handleQuit (ApplicationEvent e) {
        //Set it to false to abort the quit, our code will handle shutdown
        e.setHandled (!performAction (EXIT_ACTION));
    }
    
    public void handleReOpenApplication (ApplicationEvent e) {
    }
    
    private boolean performAction (String key) {
        return performAction (key, null);
    }
    
    private boolean performAction (String key, String command) {
        Action a = findAction (key);
        if (a == null) {
            return false;
        }
        if (command == null) {
            command = "foo"; //XXX ???
        }
        ActionEvent ae = new ActionEvent (this, ActionEvent.ACTION_PERFORMED, 
            command);
        try {
            a.actionPerformed(ae);
            return true;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            return false;
        }
    }
    
    private Action findAction (String key) {
        FileObject fo = 
            Repository.getDefault().getDefaultFileSystem().findResource(key);
        
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find (fo);
                InstanceCookie ic = 
                    (InstanceCookie) dob.getCookie(InstanceCookie.class);
                
                if (ic != null) {
                    Object instance = ic.instanceCreate();
                    if (instance instanceof Action) {
                        return (Action) instance;
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                return null;
            }
        }
        return null;
    }
}
