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

package org.netbeans.modules.j2ee.ejbjarproject.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 * NodeFactory to create EJB nodes.
 * 
 * @author gpatil
 */
public class EjbsNodeFactory implements NodeFactory {

    public EjbsNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        EjbJarProject project = p.getLookup().lookup(EjbJarProject.class);
        assert project != null;
        return new EjbNodeList(project);
    }
    
    private static class EjbNodeList implements NodeList<String> {
        private static final String KEY_EJBS = "ejbKey"; //NOI18N

        private final EjbJarProject project;

        EjbNodeList(EjbJarProject proj) {
            this.project = proj;
        }
        
        public List<String> keys() {
            return Collections.singletonList(KEY_EJBS);
        }

        public void addChangeListener(ChangeListener l) {
            // Ignore, will not generate any change event.
        }

        public void removeChangeListener(ChangeListener l) {
            // Ignore, will not generate any change event.
        }

        public Node node(String key) {
            if (KEY_EJBS.equals(key)) {
                EjbJar ejbModule = project.getAPIEjbJar(); 
                return J2eeProjectView.createEjbsView(ejbModule, project);
            }
            assert false: "No node for key: " + key; //NO18N
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }
    }           
}
