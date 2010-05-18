/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.project.jsf.ui;

import org.netbeans.modules.visualweb.complib.api.ComplibService;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.services.ThemeNodeService;
import org.netbeans.modules.visualweb.project.jsf.services.DataSourceService;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** Visual Web framework extra nodes factory.
 *
 * @author Po-Ting Wu
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=900)
public class JSFNodeFactory implements NodeFactory {
    /** Creates a new instance of JSFNodeFactory */
    public JSFNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        return new JSFNodeList(p);
    }

    private static class JSFNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String THEMES_FOLDER = "themesFolder"; //NOI18N
        private static final String COMPONENT_LIBS = "compLib"; //NOI18N
        private static final String DATASOURCE_REFS = "dataSource"; //NOI18N

        private final Project project;
        private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

        JSFNodeList(Project proj) {
            project = proj;
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            if (JsfProjectUtils.isJsfProject(project)) {
                result.add(THEMES_FOLDER);
                result.add(COMPONENT_LIBS);
                result.add(DATASOURCE_REFS);
            }
            return result;
        }

        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            for (ChangeListener elem : listeners) {
                elem.stateChanged(new ChangeEvent( this ));
            }
        }

        public Node node(String key) {
            if (key == THEMES_FOLDER) {
                Lookup.Result<ThemeNodeService> themeNodeServices = Lookup.getDefault().lookup(new Lookup.Template<ThemeNodeService>(ThemeNodeService.class));
                for (ThemeNodeService service: themeNodeServices.allInstances()) {
                    // We can return only one node depending on the project
                    Node themeNode = service.getThemeNode(project);
                    if (themeNode != null) {
                        return themeNode;
                    }
                } 

                return null;
            } else if (key == COMPONENT_LIBS) {
                ComplibService complibService =  Lookup.getDefault().lookup(ComplibService.class);
                if (complibService != null) {
                    return complibService.getComplibsRootNode(project);
                } else {
                    return null;
                }
            } else if (key == DATASOURCE_REFS) {
                DataSourceService dss = (DataSourceService) Lookup.getDefault().lookup(DataSourceService.class);
                if (dss != null) {
                    return dss.getDataSourceReferenceNode(project); 
                } else {
                    return null;
                }
            }
            assert false: "No node for key: " + key;
            return null;
        }

        public void addNotify() {
            JsfProjectUtils.addJsfFrameworkChangeListener(project, this);
        }

        public void removeNotify() {
            JsfProjectUtils.removeJsfFrameworkChangeListener(project, this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        }
        
    }
}
