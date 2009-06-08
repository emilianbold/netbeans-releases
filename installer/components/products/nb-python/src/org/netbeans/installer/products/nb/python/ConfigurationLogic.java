/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.products.nb.python;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String PYTHON_CLUSTER =
            "{python-cluster}"; // NOI18N
    private static final String ID =
            "PY"; // NOI18N

    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            PYTHON_CLUSTER}, ID);
    }

    @Override
    public void uninstall(Progress progress) throws UninstallationException {
        super.uninstall(progress);
        FilesList installedFiles = getProduct().getInstalledFiles();
        File location = new File(getProduct().getInstallationLocation(), PYTHON_CLUSTER);
        File [] jythonDirs = location.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && 
                        pathname.getName().startsWith("jython");
            }
        });
        
        try {
            if(jythonDirs!=null) {
                for(File dir : jythonDirs) {
                    List <File> list = new ArrayList <File>();
                    findFiles(new File(dir, "cachedir/packages"), ".*.pkc$", list);
                    findFiles(new File(dir, "Lib"), ".*\\$py.class$", list);
                    for(File f : list) {
                        LogManager.log("... adding jython temp file for removal : " + f);
                    }
                    installedFiles.add(list);
                }
            }
            
        } catch (IOException e) {
            LogManager.log(e);
        }        
    }
    
    private void findFiles(
            final File file,
            final String pattern, List <File> list) {
        if (file.getName().matches(pattern)) {
            list.add(file);
            return;
        }
        if(file.isFile()) {
            return;
        }
        final File[] children = file.listFiles();
        if (children != null) {
            for (File child: children) {
                findFiles(child, pattern,list);                
            }
        }
        
        return;
    }
    
}
