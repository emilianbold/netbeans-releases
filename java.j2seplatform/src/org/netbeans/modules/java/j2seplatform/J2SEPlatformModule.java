/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seplatform;

import java.io.IOException;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

import java.util.Iterator;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;


public class J2SEPlatformModule extends ModuleInstall {

    public void restored() {
        super.restored();
        // update default javac.source and javac.target in 
        // userdir/build.properties according to current active platform
        doUpdateSourceLevel();
    }

    // implemented as separate method for simpler unit testing
    static void doUpdateSourceLevel() {
        ProjectManager.mutex().postWriteRequest(
            new Runnable () {
                public void run () {
                    try {
                        EditableProperties ep = PropertyUtils.getGlobalProperties();
                        if (updateSourceLevel(ep)) {
                            PropertyUtils.putGlobalProperties (ep);
                        }
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify (ioe);
                    }
                }
            });
    }
    
    private static boolean updateSourceLevel(EditableProperties ep) {
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        String ver = platform.getSpecification().getVersion().toString();
        if (!ver.equals(ep.getProperty("default.javac.source"))) { //NOI18N
            ep.setProperty("default.javac.source", ver); //NOI18N
            ep.setProperty("default.javac.target", ver); //NOI18N
            return true;
        } else {
            return false;
        }
    }
    
}
