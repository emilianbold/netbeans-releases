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

import java.awt.Image;
import org.netbeans.modules.maven.api.FileUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Implementation of Sources interface for maven projects.
 * generic and java are necessary for proper workings of the project, the rest is custom thing..
 * IMHO at least..
 * @author  Milos Kleint
 */
public class MavenSourcesImpl implements Sources, SourceGroupModifierImplementation {
    public static final String TYPE_OTHER = "Resources"; //NOI18N
    public static final String TYPE_TEST_OTHER = "TestResources"; //NOI18N
    public static final String TYPE_GEN_SOURCES = "GeneratedSources"; //NOI18N
    public static final String TYPE_GROOVY = "groovy"; //NOI18N
    public static final String TYPE_SCALA = "scala"; //NOI18N

    public static final String NAME_GROOVYSOURCE = "81GroovySourceRoot"; //NOI18N
    public static final String NAME_GROOVYTESTSOURCE = "82GroovyTestSourceRoot"; //NOI18N
    public static final String NAME_SCALASOURCE = "91ScalaSourceRoot"; //NOI18N
    public static final String NAME_SCALATESTSOURCE = "92ScalaTestSourceRoot"; //NOI18N
    public static final String NAME_PROJECTROOT = "ProjectRoot"; //NOI18N
    public static final String NAME_XDOCS = "XDocs"; //NOI18N
    public static final String NAME_SOURCE = "1SourceRoot"; //NOI18N
    public static final String NAME_TESTSOURCE = "2TestSourceRoot"; //NOI18N
    public static final String NAME_GENERATED_SOURCE = "6GeneratedSourceRoot"; //NOI18N
    
    private final NbMavenProjectImpl project;
    private final List<ChangeListener> listeners;
    
    private Map<String, SourceGroup> javaGroup;
    private Map<File, SourceGroup> genSrcGroup;
    private Map<File, OtherGroup> otherMainGroups;
    private Map<File, OtherGroup> otherTestGroups;
    private Map<String, SourceGroup> groovyGroup;
    private Map<String, SourceGroup> scalaGroup;

    
    private final Object lock = new Object();
    
    
    /** Creates a new instance of MavenSourcesImpl */
    public MavenSourcesImpl(NbMavenProjectImpl proj) {
        project = proj;
        listeners = new ArrayList<ChangeListener>();
        javaGroup = new TreeMap<String, SourceGroup>();
        genSrcGroup = new TreeMap<File, SourceGroup>();
        otherMainGroups = new TreeMap<File, OtherGroup>();
        otherTestGroups = new TreeMap<File, OtherGroup>();
        groovyGroup = new TreeMap<String, SourceGroup>();
        scalaGroup = new TreeMap<String, SourceGroup>();

        NbMavenProject.addPropertyChangeListener(project, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(event.getPropertyName())) {
                    checkChanges(true, true);
                }
            }
        });
    }
    
    private void checkChanges(boolean fireChanges, boolean checkAlsoNonJavaStuff) {
        boolean changed = false;
        synchronized (lock) {
            MavenProject mp = project.getOriginalMavenProject();
            FileObject folder = FileUtilities.convertStringToFileObject(mp.getBuild().getSourceDirectory());
            changed = changed | checkSourceGroupCache(folder, NAME_SOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Sources"), javaGroup);
            folder = FileUtilities.convertStringToFileObject(mp.getBuild().getTestSourceDirectory());
            changed = changed | checkSourceGroupCache(folder, NAME_TESTSOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Test_Sources"), javaGroup);
            changed = changed | checkGeneratedGroupsCache(project.getGeneratedSourceRoots());
            //groovy
            folder = FileUtilities.convertURItoFileObject(project.getGroovyDirectory(false));
            changed = changed | checkSourceGroupCache(folder, NAME_GROOVYSOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_GroovySources"), groovyGroup);
            folder = FileUtilities.convertURItoFileObject(project.getGroovyDirectory(true));
            changed = changed | checkSourceGroupCache(folder, NAME_GROOVYTESTSOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Test_GroovySources"), groovyGroup);
            //scala
            folder = FileUtilities.convertURItoFileObject(project.getScalaDirectory(false));
            changed = changed | checkSourceGroupCache(folder, NAME_SCALASOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_ScalaSources"), scalaGroup);
            folder = FileUtilities.convertURItoFileObject(project.getScalaDirectory(true));
            changed = changed | checkSourceGroupCache(folder, NAME_SCALATESTSOURCE, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Test_ScalaSources"), scalaGroup);

            if (checkAlsoNonJavaStuff) {
                changed = changed | checkOtherGroupsCache(project.getOtherRoots(false), false);
                changed = changed | checkOtherGroupsCache(project.getOtherRoots(true), true);
            }
        }
        if (changed) {
            if (fireChanges) {
                fireChange();
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
                // don't fire event at all..
                checkChanges(false, false);
                toReturn.addAll(javaGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_GEN_SOURCES.equals(str)) {
            URI[] uris = project.getGeneratedSourceRoots();
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            synchronized (lock) {
                checkGeneratedGroupsCache(uris);
                toReturn.addAll(genSrcGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_OTHER.equals(str) || TYPE_TEST_OTHER.equals(str)) {
            // TODO not all these are probably resources.. maybe need to split in 2 groups..
            boolean test = TYPE_TEST_OTHER.equals(str);
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            File[] roots = project.getOtherRoots(test);
            synchronized (lock) {
                // don't fire event synchronously..
                checkOtherGroupsCache(roots, test);
                if (test && !otherTestGroups.isEmpty()) {
                    toReturn.addAll(otherTestGroups.values());
                } else if (!test && !otherMainGroups.isEmpty()) {
                    toReturn.addAll(otherMainGroups.values());
                }
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_GROOVY.equals(str)) {
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            synchronized (lock) {
                // don't fire event synchronously..
                checkChanges(false, false);
                toReturn.addAll(groovyGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_SCALA.equals(str)) {
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            synchronized (lock) {
                // don't fire event synchronously..
                checkChanges(false, false);
                toReturn.addAll(scalaGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }

        if (JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(str)) {
            return getOrCreateResourceSourceGroup(false);
        }
//        logger.warn("unknown source type=" + str);
        return new SourceGroup[0];
    }

    private SourceGroup[] getOrCreateResourceSourceGroup(boolean test) {
        URI[] uris = project.getResources(test);
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
        } else {
            //TODO add <Resources> element to pom??
        }
        return new SourceGroup[0];
    }
    

    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    private boolean checkSourceGroupCache(FileObject root, String name, String displayName, Map<String, SourceGroup> groups) {
        SourceGroup group = groups.get(name);
        if (root == null && group != null) {
            groups.remove(name);
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (group == null) {
            group = GenericSources.group(project, root, name, displayName, null, null);
            groups.put(name, group);
            changed = true;
        } else {
            if (!group.getRootFolder().equals(root)) {
                group = GenericSources.group(project, root, name, displayName, null, null);
                groups.put(name, group);
                changed = true;
            }
        }
        return changed;
    }


    private boolean checkGeneratedGroupsCache(URI[] uris) {
        boolean changed = false;
        List<File> checked = new ArrayList<File>();
        for (URI u : uris) {
            File file = FileUtil.normalizeFile(new File(u));
            FileObject folder = FileUtil.toFileObject(file);
            changed = changed |checkGeneratedGroupCache(folder, file, file.getName());
            checked.add(file);
        }
        Set<File> currs = new HashSet<File>();
        currs.addAll(genSrcGroup.keySet());
        for (File curr : currs) {
            if (!checked.contains(curr)) {
                genSrcGroup.remove(curr);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    private boolean checkGeneratedGroupCache(FileObject root, File rootFile, String nameSuffix) {
        SourceGroup group = genSrcGroup.get(rootFile);
        if (root == null && group != null) {
            genSrcGroup.remove(rootFile);
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (group == null) {
            group = new GeneratedGroup(project, root, NAME_GENERATED_SOURCE + nameSuffix, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Generated_Sources", nameSuffix));
            genSrcGroup.put(rootFile, group);
            changed = true;
        } else {
            if (!group.getRootFolder().isValid() || !group.getRootFolder().equals(root)) {
                group = new GeneratedGroup(project, root, NAME_GENERATED_SOURCE + nameSuffix, NbBundle.getMessage(MavenSourcesImpl.class, "SG_Generated_Sources", nameSuffix));
                genSrcGroup.put(rootFile, group);
                changed = true;
            }
        }
        return changed;
    }

    private boolean checkOtherGroupsCache(File[] roots, boolean test) {
        boolean ch = false;
        Set<File> toRemove = new HashSet<File>(test ? otherTestGroups.keySet() : otherMainGroups.keySet());
        toRemove.removeAll(Arrays.asList(roots));

        URI[] res = project.getResources(test);
        Set<File> resources = new HashSet<File>();
        for (URI ur : res) {
            resources.add(new File(ur));
        }

        for (File f : roots) {
            ch = ch | checkOtherGroup(f, resources, test);
        }
        for (File f : toRemove) {
            //now this shall remove the nonexisting ones and even mark the change..
            ch = ch | checkOtherGroup(f, resources, test);
        }
        return ch;
    }

    private boolean checkOtherGroup(File rootFile, Set<File> resourceRoots, boolean test) {
        FileObject root = FileUtil.toFileObject(rootFile);
        if (root != null && !root.isFolder()) {
            root = null;
        }
        Map<File, OtherGroup> map = test ? otherTestGroups : otherMainGroups;
        OtherGroup grp = map.get(rootFile);
        boolean isResourceNow = resourceRoots.contains(rootFile);
        boolean wasResourceBefore = grp != null && grp.getResource() != null;
        if ((root == null && grp != null) ||  (root != null && grp != null && wasResourceBefore && !isResourceNow)) {
            map.remove(rootFile);
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (grp == null || !grp.getRootFolder().isValid() || !grp.getRootFolder().equals(root) ||
                isResourceNow != wasResourceBefore) {
            grp = new OtherGroup(project, root, "Resource" + (test ? "Test":"Main") + root.getNameExt(), root.getName(), test); //NOI18N
            map.put(rootFile, grp);
            changed = true;
        }
        return changed;
    }

    public SourceGroup createSourceGroup(String type, String hint) {
        assert type != null;
        MavenProject mp = project.getOriginalMavenProject();
        File folder = null;
        if (JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(type)) {
            boolean main = JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint);
            SourceGroup[] grps =  getOrCreateResourceSourceGroup(!main);
            if (grps.length > 0) {
                return grps[0];
            }
            return null;
        }
        if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type)) {
            if (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint)) {
                folder = FileUtilities.convertStringToFile(mp.getBuild().getSourceDirectory());
            }
            if (JavaProjectConstants.SOURCES_HINT_TEST.equals(hint)) {
                folder = FileUtilities.convertStringToFile(mp.getBuild().getTestSourceDirectory());
            }
        }
        if (MavenSourcesImpl.TYPE_GROOVY.equals(type)) {
            if (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint)) {
                folder = new File(project.getGroovyDirectory(false));
            }
            if (JavaProjectConstants.SOURCES_HINT_TEST.equals(hint)) {
                folder = new File(project.getGroovyDirectory(true));
            }
        }
        if (MavenSourcesImpl.TYPE_SCALA.equals(type)) {
            if (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint)) {
                folder = new File(project.getScalaDirectory(false));
            }
            if (JavaProjectConstants.SOURCES_HINT_TEST.equals(hint)) {
                folder = new File(project.getScalaDirectory(true));
            }
        }
        if (folder != null) {
            folder.mkdirs();
            FileUtil.refreshFor(folder);
            checkChanges(false, true);
            FileObject fo = FileUtil.toFileObject(folder);
            assert fo != null;
            SourceGroup[] grps = getSourceGroups(type);
            for (SourceGroup sg : grps) {
                if (fo.equals(sg.getRootFolder())) {
                    return sg;
                }
            }
            //shall we somehow report it?
        }

        return null;
    }

    public boolean canCreateSourceGroup(String type, String hint) {
        if ((JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(type) || JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type))
                && (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint) || JavaProjectConstants.SOURCES_HINT_TEST.equals(hint))) {
            return true;
        }
        return false;
    }
    
    
    public static final class OtherGroup implements SourceGroup {
        
        private final FileObject rootFolder;
        private File rootFile;
        private final String name;
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        private NbMavenProjectImpl project;
        private Resource resource;
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        
        @SuppressWarnings("unchecked")
        OtherGroup(NbMavenProjectImpl p, FileObject rootFold, String nm, String displayNm, boolean test) {
            project = p;
            rootFolder = rootFold;
            rootFile = FileUtil.toFile(rootFolder);
            resource = checkResource(rootFold, 
                    test ? project.getOriginalMavenProject().getTestResources() :
                           project.getOriginalMavenProject().getResources());
            if (resource != null) {
                Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/others-badge.png", true); //NOI18N
//                ImageUtilities.addToolTipToImage(badge, "Resource root as defined in POM.");
                icon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(false), badge, 8, 8));
                openedIcon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(true), badge, 8, 8));
                name = FileUtilities.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), FileUtilities.convertStringToFile(resource.getDirectory()));
                displayName = name;
            } else {
                icon = ImageUtilities.image2Icon(NodeUtils.getTreeFolderIcon(false));
                openedIcon = ImageUtilities.image2Icon(NodeUtils.getTreeFolderIcon(true));
                name = nm;
                displayName = displayNm != null ? displayNm : NbBundle.getMessage(MavenSourcesImpl.class, "SG_Root_not_defined");
            }
        }
        
        public FileObject getRootFolder() {
            return rootFolder;
        }
        
        public File getRootFolderFile() {
            return rootFile;
        }

        public Resource getResource() {
            return resource;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            if (resource != null && resource.getTargetPath() != null) {
                return displayName + " -> " + resource.getTargetPath();
            }
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
            support.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.removePropertyChangeListener(l);
        }

        private Resource checkResource(FileObject rootFold, List<Resource> list) {
            for (Resource elem : list) {
                URI uri = FileUtilities.getDirURI(project.getProjectDirectory(), elem.getDirectory());
                FileObject fo = FileUtilities.convertURItoFileObject(uri);
                if (fo != null && fo.equals(rootFold)) {
                    return elem;
                }
            }
            return null;
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
