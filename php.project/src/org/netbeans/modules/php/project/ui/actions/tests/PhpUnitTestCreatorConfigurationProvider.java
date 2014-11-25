/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfigurationProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service=TestCreatorConfigurationProvider.class, position=20)
public class PhpUnitTestCreatorConfigurationProvider extends TestCreatorConfigurationProvider {

    /**
     *
     * @param framework the value of framework
     * @return the boolean
     */
    @Override
    public boolean canHandleProject(String framework) {
        return framework.equals(TestCreatorProvider.FRAMEWORK_PHPUNIT);
    }

    @Override
    public void persistConfigurationPanel(Context context) {
    }

    @Override
    public String[] getSourceAndTestClassNames(FileObject fileObj, boolean isTestNG, boolean isSelenium) {
        String[] result = {"", ""};
        Project p = FileOwnerQuery.getOwner(fileObj);
        if (p != null) {
            Collection<? extends ClassPathProvider> providers = p.getLookup().lookupAll(ClassPathProvider.class);
            for (ClassPathProvider provider : providers) {
                ClassPath cp = provider.findClassPath(fileObj, PhpSourcePath.SOURCE_CP);
                if (cp != null) {
                    result[0] =  cp.getResourceName(fileObj, '.', false);
                    result[1] = result[0].concat(TestCreatorProvider.TEST_CLASS_SUFFIX);
                }
            }
        }
        return result;
    }

    

    @Override
    public Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fo) {
        ArrayList<Object> folders = new ArrayList<>();
        Project p = FileOwnerQuery.getOwner(fo);
        if (p != null && (p instanceof PhpProject)) {
            List<FileObject> testDirectories = ProjectPropertiesSupport.getTestDirectories((PhpProject)p, true);
            SourceGroup[] sourceGroups = PhpProjectUtils.getSourceGroups((PhpProject)p);
            for(SourceGroup sg : sourceGroups) {
                if(!sg.contains(fo)) {
                    if(testDirectories.contains(sg.getRootFolder())) {
                        folders.add(sg);
                    }
                }
            }
        }
        return folders.toArray();
    }
    
}
