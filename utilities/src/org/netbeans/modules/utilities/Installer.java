/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.utilities;

import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.openfile.Server;
import org.netbeans.modules.openfile.Settings;
import org.netbeans.modules.pdf.LinkProcessor;

/** ModuleInstall class for Utilities module
*
* @author Jesse Glick, Petr Kuzel, Martin Ryzl
*/
public class Installer extends ModuleInstall {

    private final static long serialVersionUID = 1;

    private final org.netbeans.modules.search.Installer searchInstaller;

    public Installer() {
        searchInstaller = new org.netbeans.modules.search.Installer();
    }

    /** Module installed for the first time. */
    public void installed () {
        searchInstaller.installed();

        // Don't ask:
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              Settings.DEFAULT.isRunning ();
                                          }
                                      }, 60000);
        
        LinkProcessor.init ();

    }

    public void uninstalled () {
        // OpenFile:
        Server.shutdown ();

        searchInstaller.uninstalled();
    }

    public boolean closing () {
        // OpenFile:
        Server.shutdown ();

        return true;
    }

    public void restored () {
        //System.err.println("utilities.Installer.restored");
        searchInstaller.restored();

        // Don't ask:
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              Settings.DEFAULT.isRunning ();
                                          }
                                      }, 60000);

        LinkProcessor.init ();
                                      
    }

}
