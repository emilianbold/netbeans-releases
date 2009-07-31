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

package org.netbeans.modules.php.project.ui.logicalview;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author Radek Matous
 */
public class PhpSourcesFilter implements  ChangeListener, ChangeableDataFilter {
        private static final long serialVersionUID = -74397897465486955L;

        private final PhpProject project;
        private final FileObject rootFolder;
        private final File nbProject;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public PhpSourcesFilter(PhpProject project) {
            this(project, null);
        }
        public PhpSourcesFilter(PhpProject project, FileObject rootFolder) {
            assert project != null;

            this.project = project;
            this.rootFolder = rootFolder;

            nbProject = project.getHelper().resolveFile(AntProjectHelper.PROJECT_XML_PATH).getParentFile();
            assert nbProject != null : "NB metadata folder was not found for project: " + project;

            VisibilityQuery visibilityQuery = VisibilityQuery.getDefault();
            visibilityQuery.addChangeListener(WeakListeners.change(this, visibilityQuery));
        }

        public boolean acceptDataObject(DataObject object) {
            return !isProjectFile(object)
                    && !isTestDirectory(object)
                    && !isSeleniumDirectory(object)
                    && VisibilityQuery.getDefault().isVisible(object.getPrimaryFile());
        }

        private boolean isProjectFile(DataObject object) {
            File f = FileUtil.toFile(object.getPrimaryFile());
            return nbProject.equals(f);
        }

        private boolean isTestDirectory(DataObject object) {
            return isDirectory(object, ProjectPropertiesSupport.getTestDirectory(project, false));
        }

        private boolean isSeleniumDirectory(DataObject object) {
            return isDirectory(object, ProjectPropertiesSupport.getSeleniumDirectory(project, false));
        }

        private boolean isDirectory(DataObject object, FileObject directory) {
            if (rootFolder == null || directory == null) {
                return false;
            }
            if (!directory.equals(rootFolder)) {
                // in sources or similar (but not in 'directory' definitely)
                return directory.equals(object.getPrimaryFile());
            }
            return false;
        }

        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
    }
