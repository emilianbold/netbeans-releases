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

package org.netbeans.modules.php.project.ui.actions.tests;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpProjectUtils;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.phpunit.PhpUnit;
import org.netbeans.spi.gototest.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Support for PHP Unit.
 * @author Tomas Mysik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.gototest.TestLocator.class)
public class GoToTest implements TestLocator {
    private static final Logger LOGGER = Logger.getLogger(GoToTest.class.getName());

    public GoToTest() {
    }

    @Override
    public boolean appliesTo(FileObject fo) {
        return FileUtils.isPhpFile(fo);
    }

    @Override
    public boolean asynchronous() {
        return false;
    }

    @Override
    public void findOpposite(FileObject fo, int caretOffset, LocationListener callback) {
        throw new UnsupportedOperationException("GotoTest is synchronous");
    }

    @Override
    public LocationResult findOpposite(FileObject fo, int caretOffset) {
        PhpProject project = findPhpProject(fo);
        if (project == null) {
            // XXX what to do now??
            LOGGER.log(Level.INFO, "PHP project was not found for file {0}", fo);
            return null;
        }

        if (CommandUtils.isUnderTests(project, fo, false)) {
            return findSource(project, fo);
        } else if (CommandUtils.isUnderSources(project, fo)) {
            return findTest(project, fo);
        }
        return null;
    }

    @Override
    public FileType getFileType(FileObject fo) {
        PhpProject project = findPhpProject(fo);
        if (project == null) {
            LOGGER.log(Level.INFO, "PHP project was not found for file {0}", fo);
            return FileType.NEITHER;
        }

        if (CommandUtils.isUnderTests(project, fo, false)) {
            String name = fo.getNameExt();
            if (!name.equals(PhpUnit.TEST_FILE_SUFFIX) && name.endsWith(PhpUnit.TEST_FILE_SUFFIX)) {
                return FileType.TEST;
            }
        } else if (CommandUtils.isUnderSources(project, fo)) {
            return FileType.TESTED;
        }
        return FileType.NEITHER;
    }

    private LocationResult findSource(PhpProject project, FileObject testFo) {
        FileObject sources = getSources(project);
        assert sources != null : "Project sources must be found";
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";
        Collection<PhpClass> classes = editorSupport.getClasses(testFo);
        for (PhpClass phpClass : classes) {
            String clsFQName = phpClass.getFullyQualifiedName();
            if (clsFQName.endsWith(PhpUnit.TEST_CLASS_SUFFIX)) {
                int lastIndexOf = phpClass.getName().lastIndexOf(PhpUnit.TEST_CLASS_SUFFIX);
                assert lastIndexOf != -1;
                String srcClassName = phpClass.getName().substring(0, lastIndexOf);
                lastIndexOf = clsFQName.lastIndexOf(PhpUnit.TEST_CLASS_SUFFIX);
                String srcClassFQName = clsFQName.substring(0, lastIndexOf);
                Collection<FileObject> files = editorSupport.filesForClass(sources, new PhpClass(srcClassName, srcClassFQName, -1));
                for (FileObject fileObject : files) {
                    if (FileUtils.isPhpFile(fileObject)
                            && FileUtil.isParentOf(sources, fileObject)) {
                        return new LocationResult(fileObject, -1);
                    }
                }
            }
        }
        return new LocationResult(NbBundle.getMessage(GoToTest.class, "MSG_SrcNotFound", testFo.getNameExt()));
    }

    public static LocationResult findTest(PhpProject project, FileObject srcFo) {
        FileObject tests = getTests(project);
        if (tests != null) {
            EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
            assert editorSupport != null : "Editor support must exist";
            Collection<PhpClass> classes = editorSupport.getClasses(srcFo);
            for (PhpClass phpClass : classes) {
                String testClsName = phpClass.getName() + PhpUnit.TEST_CLASS_SUFFIX;
                String testClsFQName = phpClass.getFullyQualifiedName() + PhpUnit.TEST_CLASS_SUFFIX;
                Collection<FileObject> files = editorSupport.filesForClass(tests, new PhpClass(testClsName, testClsFQName, -1));
                for (FileObject fileObject : files) {
                    if (FileUtils.isPhpFile(fileObject)
                            && FileUtil.isParentOf(tests, fileObject)) {
                        return new LocationResult(fileObject, -1);
                    }
                }
            }
        }
        return new LocationResult(NbBundle.getMessage(GoToTest.class, "MSG_TestNotFound", srcFo.getNameExt()));
    }

    private PhpProject findPhpProject(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null
                || !PhpProjectUtils.isPhpProject(project)) {
            return null;
        }
        return (PhpProject) project;
    }

    public static FileObject getSources(PhpProject project) {
        return ProjectPropertiesSupport.getSourcesDirectory(project);
    }

    public static FileObject getTests(PhpProject project) {
        return ProjectPropertiesSupport.getTestDirectory(project, false);
    }
}
