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

package org.netbeans.modules.web.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
public final class SetupDirNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public SetupDirNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = (WebProject) p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new SetupDirNodeList(project);
    }

    private static class SetupDirNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String SETUP_DIR = "setupDir"; //NOI18N

        private final WebProject project;
        private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

        SetupDirNodeList(WebProject proj) {
            project = proj;
            WebLogicalViewProvider logView = (WebLogicalViewProvider) project.getLookup().lookup(WebLogicalViewProvider.class);
            assert logView != null;
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(SETUP_DIR);
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
            if (key == SETUP_DIR) {
                return J2eeProjectView.createServerResourcesNode(project);
            }
            assert false: "No node for key: " + key;
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
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
