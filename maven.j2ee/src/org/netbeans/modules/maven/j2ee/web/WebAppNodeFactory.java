/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
public class WebAppNodeFactory implements NodeFactory {
    private static final String KEY_WEBAPP = "webapp"; //NOI18N
    
    /** Creates a new instance of SiteDocsNodeFactory */
    public WebAppNodeFactory() {
    }
    
    public NodeList createNodes(Project project) {
        Project prj = project.getLookup().lookup(Project.class);
        return new NList(prj);
    }
    
    
    private static class NList extends AbstractMavenNodeList<String> implements PropertyChangeListener{
        private Project project;
        private NbMavenProject mavenproject;
        private String currentWebAppKey;
        
        private NList(Project prj) {
            project = prj;
            mavenproject = project.getLookup().lookup(NbMavenProject.class);
        }
        
        public List<String> keys() {
            URI webapp = mavenproject.getWebAppDirectory();
            if (webapp != null) {
                currentWebAppKey = KEY_WEBAPP + webapp.toString();
                return Collections.singletonList(currentWebAppKey);
            }
            return Collections.emptyList();
        }
        
        public Node node(String key) {
            return createWebAppNode();
        }
        
        private Node createWebAppNode() {
            Node n =  null;
            try {
                FileObject fo = URLMapper.findFileObject(mavenproject.getWebAppDirectory().toURL());
                if (fo != null) {
                    DataFolder fold = DataFolder.findFolder(fo);
                    File fil = FileUtil.toFile(fo);
                    if (fold != null) {
                        n = new WebAppFilterNode(project, fold.getNodeDelegate().cloneNode(), fil);
                    }
                }
            } catch (MalformedURLException exc) {
                n = null;
            }
            return n;
        }
        
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                fireChange();
            }
        }
        
        @Override
        public void addNotify() {
            NbMavenProject.addPropertyChangeListener(project, this);
        }
        
        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
        }
        
    }
}
