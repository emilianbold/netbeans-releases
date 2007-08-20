/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.project.jsf.ui;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.framework.JSFFrameworkProvider;
import org.netbeans.modules.visualweb.project.jsf.services.ThemeNodeService;
import org.netbeans.modules.visualweb.project.jsf.services.ComponentLibraryService;
import org.netbeans.modules.visualweb.project.jsf.services.DataSourceService;

import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.Action;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Visual Web framework extra nodes factory.
 *
 * @author Po-Ting Wu
 */
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
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }

        public Node node(String key) {
            if (key == THEMES_FOLDER) {
                ThemeNodeService themeService = (ThemeNodeService) Lookup.getDefault().lookup(ThemeNodeService.class);
                if (themeService != null) {
                    return themeService.getThemeNode(project);
                } else {
                    return null;
                }
            } else if (key == COMPONENT_LIBS) {
                ComponentLibraryService complibService = (ComponentLibraryService) Lookup.getDefault().lookup(ComponentLibraryService.class);
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
            JSFFrameworkProvider framework = getJSFFramework();
            if (framework != null) {
                framework.addPropertyChangeListener(project, this);
            }
        }

        public void removeNotify() {
            JSFFrameworkProvider framework = getJSFFramework();
            if (framework != null) {
                framework.removePropertyChangeListener(project, this);
            }
        }

        private JSFFrameworkProvider getJSFFramework() {
            List frameworks = WebFrameworkSupport.getFrameworkProviders();
            for (int i = 0; i < frameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
                String name = NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Name");
                if (framework.getName().equals(name)) {
                    return (JSFFrameworkProvider) framework;
                }
            }

            return null;
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
