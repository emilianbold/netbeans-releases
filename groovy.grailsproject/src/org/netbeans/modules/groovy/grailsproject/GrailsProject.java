/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.grailsproject;

import org.netbeans.modules.groovy.grails.api.GrailsServerState;
import org.netbeans.modules.groovy.grailsproject.ui.GrailsLogicalViewProvider;
import org.netbeans.modules.groovy.grailsproject.ui.GrailsProjectCustomizerProvider;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grailsproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.groovy.grailsproject.classpath.SourceRoots;
import org.netbeans.modules.groovy.grailsproject.queries.GrailsProjectEncodingQueryImpl;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Martin Adamek
 */
public final class GrailsProject implements Project {

    private final FileObject projectDir;
    private final ProjectState projectState;
    private final LogicalViewProvider logicalView;
    private final ClassPathProviderImpl cpProvider;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;

    private Lookup lookup;
    private final Logger LOG = Logger.getLogger(GrailsProject.class.getName());

    public GrailsProject(FileObject projectDir, ProjectState projectState) {
        this.projectDir = projectDir;
        this.projectState = projectState;
        this.logicalView = new GrailsLogicalViewProvider(this);
        this.cpProvider = new ClassPathProviderImpl(getSourceRoots(), getTestSourceRoots(), FileUtil.toFile(projectDir));
    }

    public FileObject getProjectDirectory() {
        return projectDir;
    }

    public Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(
                this,  //project spec requires a project be in its own lookup
                projectState, //allow outside code to mark the project as needing saving
                new Info(), //Project information implementation
                new GrailsSources(projectDir),
                new GrailsServerState(this, getProjectDirectory().getName()),
                new GrailsProjectCustomizerProvider(this),
                new GrailsProjectDeleteImplementation(this),
                new GrailsProjectEncodingQueryImpl(),
                getSearchInfo(projectDir),
                // new TemplatesImpl(),
                logicalView, //Logical view of project implementation
                cpProvider
            );
        }
        return lookup;
    }
    
    SearchInfo getSearchInfo(FileObject projectDir) {
        
        // LOG.setLevel(Level.FINEST);
        
        assert projectDir != null;
        
        String[] dirlist =  {   "test",
                                "src",
                                "grails-app",
                                "scripts" } ;
        
        List<FileObject> foList = new ArrayList<FileObject>();
        
        String basedir = FileUtil.getFileDisplayName(projectDir) + File.separatorChar;
        LOG.log(Level.FINEST, "basedir = " + basedir);    
        
        for (String dir : dirlist) {
            LOG.log(Level.FINEST, "dir = " + dir);
            File f = new File(basedir + dir);
            if (f != null) {
                if (f.isDirectory()) {
                    foList.add(FileUtil.toFileObject(f));
                }

            } else {
                LOG.log(Level.FINEST, "Problem creating file = " + basedir + dir);
            }
        }
        
        LOG.log(Level.FINEST, "foList: " + foList);
        
        FileObject[] folder = foList.toArray(new FileObject[foList.size()]);
        
        assert folder.length == foList.size();
        
        return SearchInfoFactory.createSearchInfo(folder, true, null);                                         
    }

    public synchronized SourceRoots getSourceRoots() {        
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = new SourceRoots(projectDir); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = new SourceRoots(projectDir); //NOI18N
        }
        return this.testRoots;
    }

    private final class Info implements ProjectInformation {

        public Icon getIcon() {
            Image image = Utilities.loadImage("org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png");
            return new ImageIcon(image);
        }

        public String getName() {
            return getProjectDirectory().getName();
        }

        public String getDisplayName() {
            return getName();
        }

        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        public Project getProject() {
            return GrailsProject.this;
        }
    }

}
