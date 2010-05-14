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

package org.netbeans.modules.soa.jca.base.inbound;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

/**
 *
 * @author echou
 */
public class JavaCollabNodeFactory implements NodeFactory {

    public NodeList<?> createNodes(Project project) {
        return new JavaCollabNodeList(project);
    }

    private class JavaCollabNodeList implements NodeList<String> {

        private static final String KEY = "javaCollab";

        private Project project;

        public JavaCollabNodeList(Project project) {
            this.project = project;
        }

        public List<String> keys() {
            return Collections.<String>singletonList(KEY);
        }

        public void addChangeListener(ChangeListener arg0) {
        }

        public void removeChangeListener(ChangeListener arg0) {
        }

        public Node node(String key) {
            try {
                if (key.equals(KEY)) {
                    return new JavaCollabNode(project);
                } else {
                    return null;
                }
            } catch (Exception e) {
                NotifyDescriptor d = new NotifyDescriptor.Exception(e);
                DialogDisplayer.getDefault().notifyLater(d);
                return null;
            }
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }

    }

}
