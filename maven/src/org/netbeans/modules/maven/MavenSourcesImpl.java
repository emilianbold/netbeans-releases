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

package org.netbeans.modules.maven;

import org.netbeans.modules.maven.api.FileUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Implementation of Sources interface for maven projects.
 * generic and java are necessary for proper workings of the project, the rest is custom thing..
 * IMHO at least..
 * @author  Milos Kleint
 */
public class MavenSourcesImpl implements Sources {
    public static final String TYPE_OTHER = "Resources"; //NOI18N
    public static final String TYPE_TEST_OTHER = "TestResources"; //NOI18N
    public static final String TYPE_GEN_SOURCES = "GeneratedSources"; //NOI18N
    public static final String NAME_PROJECTROOT = "ProjectRoot"; //NOI18N
    public static final String NAME_XDOCS = "XDocs"; //NOI18N
    public static final String NAME_SOURCE = "1SourceRoot"; //NOI18N
    public static final String NAME_TESTSOURCE = "2TestSourceRoot"; //NOI18N
    public static final String NAME_GENERATED_SOURCE = "6GeneratedSourceRoot"; //NOI18N
    
    private NbMavenProjectImpl project;
    private final List<ChangeListener> listeners;
    
    private Map<String, SourceGroup> javaGroup;
    private SourceGroup genSrcGroup;
    
    private final Object lock = new Object();
    
    
    /** Creates a new instance of MavenSourcesImpl */
    public MavenSourcesImpl(NbMavenProjectImpl proj) {
        project = proj;
        listeners = new ArrayList<ChangeListener>();
        javaGroup = new TreeMap<String, SourceGroup>();
        NbMavenProject.addPropertyChangeListener(project, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(event.getPropertyName())) {
                    checkChanges(true);
                }
            }
        });
    }
    
    private void checkChanges(boolean synchronous) {
        boolean changed = false;
        synchronized (lock) {
            MavenProject mp = project.getOriginalMavenProject();
            if (mp != null) {
                FileObject folder = FileUtilities.convertStringToFileObject(mp.getBuild().getSourceDirectory());
                changed = changed | checkJavaGroupCache(folder, NAME_SOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Sources"));
                folder = FileUtilities.convertStringToFileObject(mp.getBuild().getTestSourceDirectory());
                changed = changed | checkJavaGroupCache(folder, NAME_TESTSOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Test_Sources"));
                URI[] uris = project.getGeneratedSourceRoots();
                if (uris.length > 0) {
                    try {
                        folder = URLMapper.findFileObject(uris[0].toURL());
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        folder = null;
                    }
                } else {
                    folder = null;
                }
                changed = changed | checkGeneratedGroupCache(folder);
            } else {
                changed = true;
                checkJavaGroupCache(null, NAME_SOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Sources"));
                checkJavaGroupCache(null, NAME_TESTSOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Test_Sources"));
                checkGeneratedGroupCache(null);
            }
        }
        if (changed) {
            if (synchronous) {
                fireChange();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        fireChange();
                    }
                });
            }
        }
    }
    
    private void fireChange() {
        List<ChangeListener> currList;
        synchronized (listeners) {
            currList = new ArrayList<ChangeListener>(listeners);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener list : currList) {
            list.stateChanged(event);
        }
    }
    
    public void addChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.add(changeListener);
        }
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.remove(changeListener);
        }
    }
    
    public SourceGroup[] getSourceGroups(String str) {
        if (Sources.TYPE_GENERIC.equals(str)) {
            return new SourceGroup[] { GenericSources.group(project, project.getProjectDirectory(), NAME_PROJECTROOT, 
                    project.getLookup().lookup(ProjectInformation.class).getDisplayName(), null, null) };
        }
        if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(str)) {
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            synchronized (lock) {
                // don't fire event synchronously..
                checkChanges(false);
                toReturn.addAll(javaGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_GEN_SOURCES.equals(str)) {
            try {
                URI[] uris = project.getGeneratedSourceRoots();
                if (uris.length > 0) {
                    //TODO just assuming 1 generated source root is BAD... should be more like the java source roots..
                    FileObject folder = URLMapper.findFileObject(uris[0].toURL());
                    SourceGroup grp = null;
                    synchronized (lock) {
                        checkGeneratedGroupCache(folder);
                        grp = genSrcGroup;
                    }
                    if (grp != null) {
                        return new SourceGroup[] {grp};
                    } else {
                        return new SourceGroup[0];
                    }
                }
            } catch (MalformedURLException exc) {
                return new SourceGroup[0];
            }
        }
        if (TYPE_OTHER.equals(str) || TYPE_TEST_OTHER.equals(str)) {
            // TODO not all these are probably resources.. maybe need to split in 2 groups..
            boolean test = TYPE_TEST_OTHER.equals(str);
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            File[] roots = project.getOtherRoots(test);
            for (File f : roots) {
                FileObject folder = FileUtil.toFileObject(f);
                if (folder != null && folder.isFolder()) { //#146753
                    toReturn.add(new OtherGroup(project, folder, "Resource" + (test ? "Test":"Main") + folder.getNameExt(), folder.getName())); //NOI18N
                }
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(str)) {
            URI[] uris = project.getResources(false);
            if (uris.length > 0) {
                List<URI> virtuals = new ArrayList<URI>();
                List<SourceGroup> existing = new ArrayList<SourceGroup>();
                for (URI u : uris) {
                    FileObject fo = FileUtilities.convertURItoFileObject(u);
                    if (fo == null) {
                        virtuals.add(u);
                    } else {
                        existing.add(GenericSources.group(project, fo, "resources",  //NOI18N
                            NbBundle.getMessage(MavenSourcesImpl.class, "SG_Project_Resources"), null, null));
                    }
                }
                if (existing.size() == 0) {
                    File root = new File(virtuals.get(0));
                    FileObject fo=null;
                    try {
                        fo = FileUtil.createFolder(root);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    existing.add(GenericSources.group(project, fo, "resources",  //NOI18N
                        NbBundle.getMessage(MavenSourcesImpl.class, "SG_Project_Resources"), null, null));
                }
                //TODO we should probably add includes/excludes to source groups.
                return existing.toArray(new SourceGroup[0]);
            }
        }
//        logger.warn("unknown source type=" + str);
        return new SourceGroup[0];
    }
    
    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    private boolean checkJavaGroupCache(FileObject root, String name, String displayName) {
        SourceGroup group = javaGroup.get(name);
        if (root == null && group != null) {
            javaGroup.remove(name);
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (group == null) {
            group = GenericSources.group(project, root, name, displayName, null, null);
            javaGroup.put(name, group);
            changed = true;
        } else {
            if (!group.getRootFolder().equals(root)) {
                group = GenericSources.group(project, root, name, displayName, null, null);
                javaGroup.put(name, group);
                changed = true;
            }
        }
        return changed;
    }
    
    
    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    private boolean checkGeneratedGroupCache(FileObject root) {
        if (root == null && genSrcGroup != null) {
            genSrcGroup = null;
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (genSrcGroup == null || !genSrcGroup.getRootFolder().isValid() || !genSrcGroup.getRootFolder().equals(root)) {
            genSrcGroup = new GeneratedGroup(project, root, NAME_GENERATED_SOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Generated_Sources"));
            changed = true;
        }
        return changed;
    }
    
    
    public static final class OtherGroup implements SourceGroup {
        
        private final FileObject rootFolder;
        private File rootFile;
        private final String name;
        private final String displayName;
        private final Icon icon = null;
        private final Icon openedIcon = null;
        private NbMavenProjectImpl project;
        
        OtherGroup(NbMavenProjectImpl p, FileObject rootFold, String nm, String displayNm/*,
                Icon icn, Icon opened*/) {
            project = p;
            rootFolder = rootFold;
            rootFile = FileUtil.toFile(rootFolder);
            name = nm;
            displayName = displayNm != null ? displayNm : NbBundle.getMessage(MavenSourcesImpl.class, "SG_Root_not_defined");
//            icon = icn;
//            openedIcon = opened;
        }
        
        public FileObject getRootFolder() {
            return rootFolder;
        }
        
        public File getRootFolderFile() {
            return rootFile;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        public boolean contains(FileObject file)  {
             if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                throw new IllegalArgumentException();
            }
            if (project != null) {
                if (file.isFolder() && file != project.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                    // #67450: avoid actually loading the nested project.
                    return false;
                }
                if (FileOwnerQuery.getOwner(file) != project) {
                    return false;
                }
            }
            File f = FileUtil.toFile(file);
            if (f != null) {
                // MIXED, UNKNOWN, and SHARABLE -> include it
                return SharabilityQuery.getSharability(f) != SharabilityQuery.NOT_SHARABLE;
            } else {
                // Not on disk, include it.
                return true;
            }

        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            // XXX should react to ProjectInformation changes
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            // XXX
        }
        
    }
    
    /**
     * MEVENIDE-536 - cannot use default implementation of SourceGroup because it
     * won't include non-shareable folders..
     */ 
    public static final class GeneratedGroup implements SourceGroup {
        
        private final FileObject rootFolder;
        private File rootFile;
        private final String name;
        private final String displayName;
        private final Icon icon = null;
        private final Icon openedIcon = null;
        private NbMavenProjectImpl project;
        
        GeneratedGroup(NbMavenProjectImpl p, FileObject rootFold, String nm, String displayNm/*,
                Icon icn, Icon opened*/) {
            project = p;
            rootFolder = rootFold;
            rootFile = FileUtil.toFile(rootFolder);
            name = nm;
            displayName = displayNm != null ? displayNm : NbBundle.getMessage(MavenSourcesImpl.class, "SG_Root_not_defined");
//            icon = icn;
//            openedIcon = opened;
        }
        
        public FileObject getRootFolder() {
            return rootFolder;
        }
        
        public File getRootFolderFile() {
            return rootFile;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        public boolean contains(FileObject file)  {
             if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                throw new IllegalArgumentException();
            }
            if (project != null) {
                if (file.isFolder() && file != project.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                    // #67450: avoid actually loading the nested project.
                    return false;
                }
                if (FileOwnerQuery.getOwner(file) != project) {
                    return false;
                }
            }
            return true;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            // XXX should react to ProjectInformation changes
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            // XXX
        }
        
    }
    
}
