/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.grammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import hidden.org.codehaus.plexus.util.IOUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;

/**
 * Module install that unzips the xml descriptors of known maven plugins.
 * @author mkleint
 */
public class ModInstall extends ModuleInstall {
    
    private static final String UPGRADE_PATH = "org" + File.separator + "codehaus" + File.separator + "mojo" + File.separator + "nbm-maven-plugin-2.6.2.xml"; //NOI18N
    /** Creates a new instance of ModInstall */
    public ModInstall() {
    }
    
    @Override
    public void restored() {
        super.restored();
        File expandedPath = InstalledFileLocator.getDefault().locate("maven2/maven-plugins-xml", null, false); //NOI18N
        File upgrade = expandedPath == null ? null : new File(expandedPath, UPGRADE_PATH);
        if (expandedPath == null || !expandedPath.exists() || (expandedPath != null && expandedPath.exists() && !upgrade.exists())) {
            File zipFile = InstalledFileLocator.getDefault().locate("maven2/maven-plugins-xml.zip", null, false); //NOI18N
            assert zipFile != null : "Wrong installation, maven2/maven-plugins-xml.zip missing"; //NOI18N
            //TODO place somewhere else to make sure it's writable by user?
            expandedPath = new File(zipFile.getParentFile(), "maven-plugins-xml"); //NOI18N
            
            InputStream in = null;
            try {
                FileObject fo = FileUtil.createFolder(expandedPath);
                in = new FileInputStream(zipFile);
                FileUtil.extractJar(fo, in);
            } catch (IOException exc) {
                Logger.getLogger(ModInstall.class.getName()).log(Level.FINE, "Cannot extract zip into " + expandedPath, exc); //NOI18N
            } finally {
                IOUtil.close(in);
            }
        }
    }
}
