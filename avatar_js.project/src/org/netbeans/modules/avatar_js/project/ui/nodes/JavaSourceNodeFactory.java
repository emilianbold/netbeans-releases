/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project.ui.nodes;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.avatar_js.project.AvatarJSProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
@NodeFactory.Registration(projectType=AvatarJSProject.ID, position=200)
public class JavaSourceNodeFactory implements NodeFactory {
    
    NodeFactory delegate =
            new org.netbeans.modules.java.api.common.project.ui.JavaSourceNodeFactory();
    
    @Override
    public NodeList<?> createNodes(Project p) {
        AvatarJSProject ap = p.getLookup().lookup(AvatarJSProject.class);
        assert ap != null;
        return new JavaSourceNodeList(ap, delegate.createNodes(p));
    }
    
    private static final class JavaSourceNodeList implements NodeList<Object> {
        
        private final AvatarJSProject project;
        private final NodeList<Object> delegate;
        private final ChangeSupport chSupport = new ChangeSupport(this);
        
        JavaSourceNodeList(AvatarJSProject project, NodeList<? extends Object> delegate) {
            this.project = project;
            this.delegate = (NodeList<Object>) delegate;
        }

        @Override
        public List<Object> keys() {
            return delegate.keys();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            delegate.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            chSupport.removeChangeListener(l);
        }

        @Override
        public Node node(Object key) {
            Node n = delegate.node(key);
            return new JavaSourceNode(n);
        }

        @Override
        public void addNotify() {
            delegate.addNotify();
        }

        @Override
        public void removeNotify() {
            delegate.removeNotify();
        }
        
    }
    
    @NbBundle.Messages("LBL_JavaSources=Java Sources")
    private static final class JavaSourceNode extends FilterNode {
        
        JavaSourceNode(Node delegate) {
            super(delegate);
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_JavaSources();
        }

    }
    
}
