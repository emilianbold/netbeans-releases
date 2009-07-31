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

import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleVisibilityExtender;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Tomas Mysik
 */
@ServiceProvider(service = VisibilityQueryImplementation.class)
public final class PhpVisibilityQuery implements VisibilityQueryImplementation {

    // @GuardedBy(this)
    private final Set<PhpProject> watchedProjects = new WeakSet<PhpProject>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ChangeListener ignoredFoldersListener = new IgnoredFoldersListener();

    public boolean isVisible(FileObject file) {
        PhpProject phpProject = PhpProjectUtils.getPhpProject(file);
        if (phpProject == null) {
            return true;
        }

        if (isIgnoredByProject(phpProject, file)) {
            return false;
        } else if (isIgnoredByFramework(phpProject, file)) {
            return false;
        }
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    private boolean isIgnoredByProject(PhpProject project, FileObject file) {
        checkIgnoredFoldersListener(project);
        return ProjectPropertiesSupport.getIgnoredFolders(project).contains(file);
    }

    private boolean isIgnoredByFramework(PhpProject project, FileObject file) {
        PhpModule phpModule = project.getLookup().lookup(PhpModule.class);
        assert phpModule != null : "php module must be found for " + project.getProjectDirectory();
        for (PhpFrameworkProvider framework : ProjectPropertiesSupport.getFrameworks(project)) {
            PhpModuleVisibilityExtender visibilityExtender = framework.createVisibilityExtender(phpModule);
            if (visibilityExtender != null) {
                if (!visibilityExtender.isVisible(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    private synchronized void checkIgnoredFoldersListener(PhpProject project) {
        if (!watchedProjects.contains(project)) {
            ProjectPropertiesSupport.addWeakIgnoredFoldersListener(project, ignoredFoldersListener);
            watchedProjects.add(project);
        }
    }

    private final class IgnoredFoldersListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            fireChange();
        }
    }
}
