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
package org.netbeans.modules.javascript.nodejs.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.ui.actions.NodeJsActionProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

//@ProjectServiceProvider(service = NodeJsSupport.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
public final class NodeJsSupport {

    private static final Logger LOGGER = Logger.getLogger(NodeJsSupport.class.getName());

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final Project project;
    private final ActionProvider actionProvider;
    private final NodeJsSourceRoots sourceRoots;
    private final NodeJsPreferences preferences;


    public NodeJsSupport(Project project, Lookup lookup) {
        assert project != null;
        this.project = project;
        actionProvider = new NodeJsActionProvider(project);
        sourceRoots = new NodeJsSourceRoots(project);
        preferences = new NodeJsPreferences(this, project);
    }

    public static NodeJsSupport forProject(Project project) {
        NodeJsSupport support = project.getLookup().lookup(NodeJsSupport.class);
        assert support != null : "NodeJsSupport should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return support;
    }

    public NodeJsPreferences getPreferences() {
        return preferences;
    }

    public ActionProvider getActionProvider() {
        return actionProvider;
    }

    public List<URL> getSourceRoots() {
        return sourceRoots.getSourceRoots();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(project, propertyName, oldValue, newValue));
    }

}
