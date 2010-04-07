/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project;

import java.util.prefs.Preferences;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public class PhpModuleImpl extends PhpModule {
    private final PhpProject phpProject;

    public PhpModuleImpl(PhpProject phpProject) {
        assert phpProject != null;
        this.phpProject = phpProject;
    }

    @Override
    public String getName() {
        return phpProject.getLookup().lookup(ProjectInformation.class).getName();
    }

    @Override
    public String getDisplayName() {
        return phpProject.getLookup().lookup(ProjectInformation.class).getDisplayName();
    }

    @Override
    public FileObject getSourceDirectory() {
        return ProjectPropertiesSupport.getSourcesDirectory(phpProject);
    }

    @Override
    public FileObject getTestDirectory() {
        return ProjectPropertiesSupport.getTestDirectory(phpProject, false);
    }

    @Override
    public PhpModuleProperties getProperties() {
        PhpModuleProperties properties = new PhpModuleProperties()
                .setWebRoot(ProjectPropertiesSupport.getWebRootDirectory(phpProject));
        FileObject tests = ProjectPropertiesSupport.getTestDirectory(phpProject, false);
        if (tests != null) {
            properties = properties.setTests(tests);
        }
        String url = ProjectPropertiesSupport.getUrl(phpProject);
        if (url != null) {
            properties = properties.setUrl(url);
        }
        String indexFile = ProjectPropertiesSupport.getIndexFile(phpProject);
        if (indexFile != null) {
            FileObject index = getSourceDirectory().getFileObject(indexFile);
            if (index != null
                    && index.isData()
                    && index.isValid()) {
                properties = properties.setIndexFile(index);
            }
        }
        return properties;
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + FileUtil.getFileDisplayName(getSourceDirectory()) + ")"; // NOI18N
    }

    @Override
    public <T extends PhpFrameworkProvider> Preferences getPreferences(Class<T> clazz, boolean shared) {
        return ProjectUtils.getPreferences(phpProject, clazz, shared);
    }
}
