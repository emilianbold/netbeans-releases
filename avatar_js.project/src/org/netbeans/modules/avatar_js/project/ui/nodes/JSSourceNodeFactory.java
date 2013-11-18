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

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.avatar_js.project.AvatarJSProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
@NodeFactory.Registration(projectType=AvatarJSProject.ID, position=100)
public class JSSourceNodeFactory implements NodeFactory {
    
    @Override
    public NodeList<?> createNodes(Project p) {
        AvatarJSProject ap = p.getLookup().lookup(AvatarJSProject.class);
        assert ap != null;
        return new JSSourceNodeList(ap);
    }
    
    private static final class JSSourceNodeList implements NodeList<FileObject> {
        
        private final AvatarJSProject project;
        private final ChangeSupport chSupport = new ChangeSupport(this);
        
        JSSourceNodeList(AvatarJSProject project) {
            this.project = project;
        }

        @Override
        public List<FileObject> keys() {
            FileObject projectDir = project.getProjectDirectory();
            FileObject jsDir = projectDir.getFileObject(AvatarJSProject.CONFIG_JS_SOURCE_PATH);
            if (jsDir == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(jsDir);
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            chSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            chSupport.removeChangeListener(l);
        }

        @Override
        public Node node(FileObject key) {
            DataFolder folder = DataFolder.findFolder(key);
            Children children = folder.createNodeChildren(DataFilter.ALL);
            return new JSSourceNode(key, children);
        }

        @Override
        public void addNotify() {
            
        }

        @Override
        public void removeNotify() {
            
        }
        
    }
    
    @NbBundle.Messages("LBL_JSSources=JavaScript Sources")
    private static final class JSSourceNode extends AbstractNode {
        
        private final FileObject root;
        private static final String JS_ICON_BASE = "org/netbeans/modules/avatar_js/project/ui/resources/javascript.png";  // NOI18N
        
        JSSourceNode(FileObject root, Children ch) {
            super(ch);
            this.root = root;
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_JSSources();
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(JS_ICON_BASE);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
    }
    
}
