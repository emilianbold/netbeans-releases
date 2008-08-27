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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.maven.MavenSourcesImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.VisibilityQueryDataFilter;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;

/**
 *
 * @author  Milos Kleint
 */
class OthersRootChildren extends Children.Keys {
    
    private NbMavenProjectImpl project;
    private PropertyChangeListener changeListener;
    private boolean test;
    public OthersRootChildren(NbMavenProjectImpl prj, boolean testResource) {
        this.project = prj;
        test = testResource;
        changeListener  = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                    regenerateKeys();
                    refresh();
                }
            }
        };
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        NbMavenProject.addPropertyChangeListener(project, changeListener);
        regenerateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        NbMavenProject.removePropertyChangeListener(project, changeListener);
        super.removeNotify();
        
    }
    
    private void regenerateKeys() {
        List<SourceGroup> list = new ArrayList<SourceGroup>();
        Sources srcs = project.getLookup().lookup(Sources.class);
        if (srcs == null) {
            throw new IllegalStateException("need Sources instance in lookup"); //NOI18N
        }
        SourceGroup[] resgroup = srcs.getSourceGroups(test ? MavenSourcesImpl.TYPE_TEST_OTHER  
                                                           : MavenSourcesImpl.TYPE_OTHER);
        Set<FileObject> files = new HashSet<FileObject>();
        for (int i = 0; i < resgroup.length; i++) {
            list.add(resgroup[i]);
            files.add(resgroup[i].getRootFolder());
            //TODO all direct subfolders that are contained in the SG?
        }
        setKeys(list);
        ((OthersRootNode)getNode()).setFiles(files);
    }
    
    
    protected Node[] createNodes(Object key) {
        SourceGroup grp = (SourceGroup)key;
        Node[] toReturn = new Node[1];
        DataFolder dobj = DataFolder.findFolder(grp.getRootFolder());
        Children childs = dobj.createNodeChildren(VisibilityQueryDataFilter.VISIBILITY_QUERY_FILTER);
        toReturn[0] = new FilterNode(dobj.getNodeDelegate().cloneNode(), childs);
        return toReturn;
    }
    

   
}
