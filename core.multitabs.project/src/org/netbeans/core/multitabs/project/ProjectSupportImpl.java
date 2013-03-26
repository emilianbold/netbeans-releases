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
package org.netbeans.core.multitabs.project;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.multitabs.impl.ProjectSupport;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author stan
 */
@ServiceProvider(service = ProjectSupport.class)
public class ProjectSupportImpl extends ProjectSupport {

    private static final Map<TabData, Project> tab2project = new WeakHashMap<TabData, Project>(50);
    
    public ProjectSupportImpl() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        OpenProjects.getDefault().addPropertyChangeListener( l );
    }

    @Override
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        OpenProjects.getDefault().removePropertyChangeListener( l );
    }

    @Override
    public ProjectProxy[] getOpenProjects() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        ProjectProxy[] res = new ProjectProxy[projects.length];
        for( int i=0; i<projects.length; i++ ) {
            Project p = projects[i];
            ProjectProxy proxy = createProxy( p );
            res[i] = proxy;
        }
        return res;
    }

    @Override
    public ProjectProxy getProjectForTab( TabData tab ) {
        synchronized( tab2project ) {
            Project p = tab2project.get( tab );
            if( null == p ) {
                if( tab.getComponent() instanceof TopComponent ) {
                    TopComponent tc = ( TopComponent ) tab.getComponent();
                    DataObject dob = tc.getLookup().lookup( DataObject.class );
                    if( null != dob ) {
                        FileObject fo = dob.getPrimaryFile();
                        if( fo.isData() ) {
                            p = FileOwnerQuery.getOwner( fo );
                            if( null != p ) {
                                tab2project.put( tab, p );
                            }
                        }
                    }
                }
            }
            return null == p ? null : createProxy( p );
        }
    }

    private static ProjectProxy createProxy( Project p ) {
        ProjectInformation info = ProjectUtils.getInformation( p );
        FileObject projectDir = p.getProjectDirectory();
        return new ProjectProxy( p, info.getDisplayName(), projectDir.getPath() );
    }
}
