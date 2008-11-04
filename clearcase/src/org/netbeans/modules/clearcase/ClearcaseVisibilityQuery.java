/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.clearcase;

import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.event.ChangeListener;
import java.util.regex.Pattern;
import java.io.File;

/**
 * Hides files that are known to clearcase and should not be visible.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class)
public class ClearcaseVisibilityQuery implements VisibilityQueryImplementation {

    private static final Pattern unloadedPattern = Pattern.compile(".*\\.unloaded(\\.\\d+)?");    
    private static final Pattern updtPattern = Pattern.compile("update\\..*?\\.updt");

    /**
     * Hides these files:
     * 
     * *.keep       - files that store local backups
     * view.dat     - administrative metadata
     * update*.updt - update logs
     * lost+found   - administrative folder (is it desired to show it in the IDE?) 
     * 
     * @param file a file to test
     * @return visibility of the file
     */
    public boolean isVisible(FileObject file) {
        File f = FileUtil.toFile(file);
        if (f == null || !isManagedByClearcase(f)) return true;
        String name = file.getNameExt();
        if (file.isFolder()) {
            return !name.equals("lost+found");
        } else {
            return !name.equals("view.dat") &&                 
                   // WARNING: 
                   // *.mkelem are temporary files created by the mkelem command
                   // 1.) it looks like keeping them visible causes exception - e.g. 
                   //     java.lang.IllegalStateException: The data object .../blah/blah.mkelem is invalid
                   //     as they are created and deleted outside of the IDE                     
                   // 2.) there is no known need to show them at all 
                   !name.endsWith(".mkelem") &&                                         
                   !updtPattern.matcher(name).matches() && 
                   !unloadedPattern.matcher(name).matches();
        }
    }

    private boolean isManagedByClearcase(File file) {
        return VersioningSupport.getOwner(file) instanceof ClearcaseVCS;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
}
