/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.util.Mutex;

/**
 * Registers the default platform.
 * @author Jesse Glick
 */
public final class Install extends ModuleInstall {

    public void restored() {
        String nbhome = System.getProperty("netbeans.home");
        if (nbhome != null) {
            final File install = FileUtil.normalizeFile(new File(nbhome).getParentFile());
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties p = PropertyUtils.getGlobalProperties();
                    p.setProperty("netbeans.dest.dir", install.getAbsolutePath()); // NOI18N
                    p.setProperty("harness.dir", "${netbeans.dest.dir}/harness"); // NOI18N
                    try {
                        PropertyUtils.putGlobalProperties(p);
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                    return null;
                }
            });
        }
    }
    
}
