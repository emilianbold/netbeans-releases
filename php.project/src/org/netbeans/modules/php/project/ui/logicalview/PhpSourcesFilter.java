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
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataObject;

/**
 *
 * @author Radek Matous
 */
public class PhpSourcesFilter implements  ChangeListener, ChangeableDataFilter {
        private static final long serialVersionUID = -7439706583318056955L;
        private File projectXML;
        private final EventListenerList ell = new EventListenerList();

        public PhpSourcesFilter(PhpProject project) {
            projectXML = project.getHelper().resolveFile(AntProjectHelper.PROJECT_XML_PATH);
            VisibilityQuery.getDefault().addChangeListener(this);
        }

        public boolean acceptDataObject(DataObject object) {
            return isNotProjectFile(object) && VisibilityQuery.getDefault().isVisible(object.getPrimaryFile());
        }

        private boolean isNotProjectFile(DataObject object) {
            try {
                if (projectXML != null) {
                    File f = FileUtil.toFile(object.getPrimaryFile()).getCanonicalFile();
                    File nbProject = projectXML.getParentFile().getCanonicalFile();
                    return nbProject != null && !nbProject.equals(f);
                } else {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }

        public void stateChanged(ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
        }

        public void addChangeListener(ChangeListener listener) {
            ell.add(ChangeListener.class, listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            ell.remove(ChangeListener.class, listener);
        }
    }
