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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Mutex;

/**
 * Registers the default platform.
 * @author Jesse Glick
 */
public final class Install extends ModuleInstall {

    public void restored() {
        final File install = NbPlatform.defaultPlatformLocation();
        if (install != null) {
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties p = PropertyUtils.getGlobalProperties();
                    String installS = install.getAbsolutePath();
                    p.setProperty("nbplatform.default.netbeans.dest.dir", installS); // NOI18N
                    if (!p.containsKey("nbplatform.default.harness.dir")) { // NOI18N
                        p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness"); // NOI18N
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
