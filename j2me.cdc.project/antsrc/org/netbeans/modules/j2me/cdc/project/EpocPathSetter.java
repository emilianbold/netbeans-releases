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

package org.netbeans.modules.j2me.cdc.project;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.File;
import org.apache.tools.ant.*;

/**
 * @author suchys
 */
public class EpocPathSetter extends Task {
    
    private File home;
    
    public void execute() throws BuildException {
        if (home == null || !home.exists())
            throw new BuildException("Home is invalid!");
        
        String absPath = home.getAbsolutePath();
        //do not depend on user setting, prepend this path before others, can broke by e.g. Nokia installation of S80 or 
        //other Symbian installation otherwise!
        getProject().setNewProperty("sdkdrive", String.valueOf(absPath.charAt(0))); //NOI18N
        getProject().setNewProperty("epocroot", absPath.substring(2) + "\\"); //NOI18N
        getProject().setNewProperty("epocpath", absPath + "\\epoc32\\gcc\\bin;" +  //NOI18N
                                                absPath + "\\epoc32\\tools;" +  //NOI18N
                                                absPath + "\\epoc32\\include;" + //NOI18N
                                                System.getProperty("java.library.path")); //NOI18N
        log("sdkdrive = " + String.valueOf(absPath.charAt(0)), Project.MSG_VERBOSE); //NOI18N
        log("epocroot = " + absPath.substring(2) + "\\", Project.MSG_VERBOSE); //NOI18N
        log("epocpath = " + absPath + "\\epoc32\\gcc\\bin;" +  //NOI18N
                                                absPath + "\\epoc32\\tools;" +  //NOI18N
                                                absPath + "\\epoc32\\include;" + //NOI18N                                                System.getProperty("java.library.path")); //NOI18N
                                                System.getProperty("java.library.path"), Project.MSG_VERBOSE); //NOI18N
    }

    public File getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home;
    }    
}
