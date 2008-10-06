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

package org.netbeans.modules.maven.nodes;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.modules.maven.api.NbMavenProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
public class SiteDocsNodeFactory implements NodeFactory {
    private static final String KEY_SITE = "SITE"; //NOI18N
    private static final String SITE = "src/site"; //NOI18N
    
    /** Creates a new instance of SiteDocsNodeFactory */
    public SiteDocsNodeFactory() {
    }
    
    public NodeList createNodes(Project project) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        return new NList(prj);
    }
    
    
    private static class NList extends AbstractMavenNodeList<String> implements PropertyChangeListener {
        private NbMavenProjectImpl project;
        
        private NList(NbMavenProjectImpl prj) {
            project = prj;
        }
        
        public List<String> keys() {
            //TODO handle custom locations of sit docs
            if (project.getProjectDirectory().getFileObject(SITE) != null) {
                return Collections.singletonList(KEY_SITE);
            }
            return Collections.emptyList();
        }
        
        public Node node(String key) {
            return createSiteDocsNode();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                fireChange();
            }
            if (NbMavenProjectImpl.PROP_RESOURCE.equals(evt.getPropertyName()) &&
                    SITE.equals(evt.getNewValue())) {
                fireChange();
            }
        }
        
        @Override
        public void addNotify() {
            NbMavenProject.addPropertyChangeListener(project, this);
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            watcher.addWatchedPath(SITE);
        }
        
        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            watcher.removeWatchedPath(SITE);
        }
        
        private Node createSiteDocsNode() {
            Node n =  null;
            //TODO handle custom locations of sit docs
            FileObject fo = project.getProjectDirectory().getFileObject(SITE);
            if (fo != null) {
                DataFolder fold = DataFolder.findFolder(fo);
                if (fold != null) {
                    n = new SiteDocsNode(project, fold.getNodeDelegate().cloneNode());
                }
            }
            return n;
        }
        
        
    }
}
