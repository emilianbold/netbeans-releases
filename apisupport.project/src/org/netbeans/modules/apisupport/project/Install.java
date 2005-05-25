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
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Mutex;

/**
 * Registers the default platform.
 * @author Jesse Glick
 */
public final class Install extends ModuleInstall {

    public void restored() {
        // XXX probably a better way to do this with InstalledFileLocator... assuming that clusters are ${netbeans.home}/../*
        String nbhome = System.getProperty("netbeans.home");
        if (nbhome != null) {
            final File install = FileUtil.normalizeFile(new File(nbhome).getParentFile());
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties p = PropertyUtils.getGlobalProperties();
                    String installS = install.getAbsolutePath();
                    p.setProperty("nbplatform.default.netbeans.dest.dir", installS); // NOI18N
                    String suffix = File.separatorChar + "nbbuild" + File.separatorChar + "netbeans"; // NOI18N
                    if (installS.endsWith(suffix)) {
                        // We're running from a build; also set source location, for convenience.
                        p.setProperty("nbplatform.default.sources", installS.substring(0, installS.length() - suffix.length())); // NOI18N
                    }
                    p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness"); // NOI18N
                    final File apidocsZip = InstalledFileLocator.getDefault().locate("docs/NetBeansAPIs.zip", "org.netbeans.modules.apisupport.apidocs", true); // NOI18N
                    if (apidocsZip != null) {
                        // XXX OK to overwrite any existing config? not sure...
                        p.setProperty("nbplatform.default.javadoc", FileUtil.normalizeFile(apidocsZip).getAbsolutePath()); // NOI18N
                    } else {
                        // XXX remove any existing binding?
                    }
                    {// XXX temporary, to clean up userdirs from old revs
                        p.remove("netbeans.dest.dir"); // NOI18N
                        p.remove("harness.dir"); // NOI18N
                        p.remove("netbeans.sources"); // NOI18N
                        p.remove("netbeans.javadoc"); // NOI18N
                        p.remove("nbplatform.default.netbeans.sources"); // NOI18N
                        p.remove("nbplatform.default.netbeans.javadoc"); // NOI18N
                    }
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
