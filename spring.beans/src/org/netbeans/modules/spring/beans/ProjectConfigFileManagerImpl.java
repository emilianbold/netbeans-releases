/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.spring.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;

/**
 *
 * @author Andrei Badea
 */
public class ProjectConfigFileManagerImpl implements ConfigFileManagerImplementation {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<ConfigFileGroup> groups;

    public ProjectConfigFileManagerImpl(Project project) {
        this.project = project;
    }

    /**
     * Returns the mutex which protectes the access to this ConfigFileManager.
     *
     * @return the mutex; never null.
     */
    public Mutex mutex() {
        return ProjectManager.mutex();
    }

    /**
     * Returns the list of config file groups in this manger. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<ConfigFileGroup> getConfigFileGroups() {
        return mutex().readAccess(new Action<List<ConfigFileGroup>>() {
            public List<ConfigFileGroup> run() {
                if (groups == null) {
                    readGroups();
                }
                List<ConfigFileGroup> result = new ArrayList<ConfigFileGroup>(groups.size());
                result.addAll(groups);
                return result;
            }
        });
    }

    /**
     * Returns the config file group (if any) which contains the given config file.
     *
     * @param  file a file; never null.
     * @return the config file group or null.
     */
    public ConfigFileGroup getConfigFileGroupFor(File file) {
        for (ConfigFileGroup group : getConfigFileGroups()) {
            if (group.containsFile(file)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Modifies the list of config file groups. This method needs to be called
     * under {@code mutex()} write access.
     *
     * @throws IllegalStateException if the called does not hold {@code mutex()}
     *         write access.
     */
    public void putConfigFileGroups(List<ConfigFileGroup> groups) {
        if (!mutex().isWriteAccess()) {
            throw new IllegalStateException("The setConfigFileGroups() method should be called under mutex() write access");
        }
        writeGroups(groups);
        changeSupport.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    private void readGroups() {
        assert mutex().isReadAccess();
    }

    private void writeGroups(List<ConfigFileGroup> groups) {
        assert mutex().isWriteAccess();
    }
}
