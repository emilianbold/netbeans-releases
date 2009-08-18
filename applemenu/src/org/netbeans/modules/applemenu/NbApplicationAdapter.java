/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.applemenu;

import com.apple.eawt.*;

import java.beans.Beans;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
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
        FileObject fo = FileUtil.getConfigFile(key);
        
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find (fo);
                InstanceCookie ic = dob.getCookie(InstanceCookie.class);
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
