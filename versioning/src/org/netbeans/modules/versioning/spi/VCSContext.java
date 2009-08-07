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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.Utils;
import org.netbeans.modules.versioning.FlatFolder;
import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.modules.versioning.Accessor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This encapsulates a context, typically set of selected files or nodes. Context is passed to VCSAnnotators when
 * asked for actions available on a given context or to annotate a name (label) representing a context.
 * 
 * @author Maros Sandor
 */
public final class VCSContext {
    
    /**
     * VCSContext that contains no files.
     */
    public static final VCSContext EMPTY = new VCSContext((Node[]) null, emptySet(), emptySet() );

    /**
     * Caching of current context for performance reasons, also see #72006.
     */
    private static Reference<VCSContext>  contextCached = new WeakReference<VCSContext>(null);    
    private static Reference<Node[]> contextNodesCached = new WeakReference<Node []>(null); 

    private final Lookup    elements;
    
    private final Set<File> unfilteredRootFiles;
    private final Set<File> rootFiles;
    private final Set<File> exclusions;

    private Set<File>       computedFilesCached;
    private FileFilter      fileFilterCached;

    static {
        Accessor.VCSContextAccessor = new AccessorImpl();
    }
    
    /**
     * Constructs a VCSContext out of a set of files. These files are later available via getRootFiles().
     * 
     * @param rootFiles set of Files
     * @param originalFiles set of original files for which the context shall be created
     * @return VCSContext a context representing supplied set of Files
     */ 
    static VCSContext forFiles(Set<File> rootFiles, Set<? extends FileObject> originalFiles) {
        return new VCSContext(originalFiles, rootFiles, emptySet());
    }

    /**
     * Initializes the context from array of nodes (typically currently activated nodes).
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become root files of this context and
     * exclusions list is constructed using sourceRoot.contains() queries.
     * 
     * Nodes' lookups are examined in the following way (the first applied rule wins):
     * - if there's a File, the File is added to set of root files
     * - if there's a Project, project's source roots of type Sources.TYPE_GENERIC are added to set of root files and
     *   all direct children that do not belong to the project (sg.contains() == false) are added to set of exclusions
     * - if there's a FileObject, it is added to set of root files
     * - if there's a DataObject, all dao.files() are added to set of root files 
     * 
     * @param nodes array of Nodes
     * @return VCSContext containing nodes and corresponding files they represent
     */
    public synchronized static VCSContext forNodes(Node[] nodes) {
        if (Arrays.equals(contextNodesCached.get(), nodes)) {
            VCSContext ctx = contextCached.get();
            if (ctx != null) return ctx;
        }
        Set<File> rootFiles = new HashSet<File>(nodes.length);
        Set<File> rootFileExclusions = new HashSet<File>(5);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            File aFile = node.getLookup().lookup(File.class);
            if (aFile != null) {
                rootFiles.add(aFile);
                continue;
            }
            Project project =  node.getLookup().lookup(Project.class);
            if (project != null) {
                addProjectFiles(rootFiles, rootFileExclusions, project);
                continue;
            }
            addFileObjects(node, rootFiles);
        }

        List<File> unversionedFiles = new ArrayList<File>(rootFiles.size());
        Set<VersioningSystem> projectOwners = new HashSet<VersioningSystem>(2);
        for (File root : rootFiles) {
            VersioningSystem owner = VersioningManager.getInstance().getOwner(root);
            if (owner == null) {
                unversionedFiles.add(root);
            } else {
                projectOwners.add(owner);
            }
        }
        if(projectOwners.size() == 0) {
            // all roots are unversioned -> keep them
        } else if(projectOwners.size() == 1) {
            // context contais one owner -> remove unversioned files
            for (File unversionedFile : unversionedFiles) {
                for (Iterator<File> i = rootFileExclusions.iterator(); i.hasNext(); ) {
                    File exclusion = i.next();
                    if (Utils.isAncestorOrEqual(unversionedFile, exclusion)) {
                        i.remove();
                    }
                }
            }
            rootFiles.removeAll(unversionedFiles);
        } else {
            // more than one owner -> return empty context
            rootFileExclusions.clear();
            rootFiles.clear();
        }

        VCSContext ctx = new VCSContext(nodes, rootFiles, rootFileExclusions);
        contextCached = new WeakReference<VCSContext>(ctx);
        contextNodesCached = new WeakReference<Node []>(nodes);
        return ctx;
    }
        
    /**
     * Returns the smallest possible set of all files that lie under Root files and are NOT 
     * under some Excluded file. 
     * Technically, for every file in the returned set all of the following is true:
     * 
     * - the file itself or at least one of its ancestors is a root file/folder
     * - neither the file itself nor any of its ancestors is an exluded file/folder
     * - the file passed through the supplied FileFilter
     *  
     * @param filter custom file filter
     * @return filtered set of files that must pass through the filter
     */
    public synchronized Set<File> computeFiles(FileFilter filter) {
        if (computedFilesCached == null || filter != fileFilterCached) {
            computedFilesCached = substract(rootFiles, exclusions, filter);
            fileFilterCached = filter;
        }
        return computedFilesCached;
    }
    
    /**
     * Retrieves elements that make up this VCS context. The returned lookup may be empty
     * or may contain any number of the following elements:
     * - instances of Node that were originally used to construct this context object
     *
     * @return Lookup lookup of this VCSContext
     */ 
    public Lookup getElements() {
        return elements;
    }

    /**
     * Retrieves set of files/folders that represent this context.
     * This set contains all files the user selected, unfiltered.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that DO manage folders, such as Clearcase. 
     * 
     * @return Set<File> set of Files this context represents
     * @see #getRootFiles() 
     * @since 1.6
     */ 
    public Set<File> getFiles() {
        return unfilteredRootFiles;
    }

    /**
     * Retrieves set of root files/folders that represent this context.
     * This set only contains context roots, not files/folders that are contained within these roots.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that do not manage folders, such as CVS. 
     * 
     * @return Set<File> set of Files this context represents
     * @see #getFiles() 
     */ 
    public Set<File> getRootFiles() {
        return rootFiles;
    }

    /**
     * Retrieves set of files/folders that are excluded from this context. Exclusions are files or folders that
     * are descendants of a root folder and should NOT be a part of a versioning operation. For example, an CVS/Update command
     * run on a project that contains a subproject should not touch any files in the subproject. Therefore the VCSContext for
     * the action would contain one root file (the project's root) and one exclusion (subproject root).
     * 
     * @return Set<File> set of files and folders that are not part of (are excluded from) this context. 
     * All their descendands are excluded too.
     */ 
    public Set<File> getExclusions() {
        return exclusions;
    }

    /**
     * Determines whether the supplied File is contained in this context. In other words, the file must be either a root file/folder
     * or be a descendant of a root folder and also must NOT be an excluded file/folder or be a descendant of an excluded folder. 
     * 
     * @param file a File to test
     * @return true if this context contains the supplied file, false otherwise 
     */ 
    public boolean contains(File file) {
        outter : for (File root : rootFiles) {
            if (Utils.isAncestorOrEqual(root, file)) {
                for (File excluded : exclusions) {
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }
        
    private static void addProjectFiles(Collection<File> rootFiles, Collection<File> rootFilesExclusions, Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            if (rootFile == null) continue;
            rootFiles.add(rootFile);
            FileObject [] rootChildren = srcRootFo.getChildren();
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                File child = FileUtil.toFile(rootChildFo);
                // TODO: #60516 deep scan is required here but not performed due to performace reasons
                try {
                    if (child != null && rootChildFo.isValid() && !sourceGroup.contains(rootChildFo) && SharabilityQuery.getSharability(child) != SharabilityQuery.NOT_SHARABLE) {
                        rootFilesExclusions.add(child);
                    }
                } catch (IllegalArgumentException ex) {
                    // #161904
                    Logger logger = Logger.getLogger(VCSContext.class.getName());
                    logger.log(Level.WARNING, "addProjectFiles: IAE");
                    logger.log(Level.WARNING, "rootFO: " + srcRootFo);
                    if (srcRootFo != sourceGroup.getRootFolder()) {
                        logger.log(Level.WARNING, "root FO has changed");
                    }
                    String children = "[";
                    for (FileObject fo : rootChildren) {
                        children += "\"" + fo.getPath() + "\", ";
                    }
                    children += "]";
                    logger.log(Level.WARNING, "srcRootFo.getChildren(): " + children);
                    if (!rootChildFo.isValid()) {
                        logger.log(Level.WARNING, rootChildFo + " does not exist ");
                    }
                    if (!FileUtil.isParentOf(srcRootFo, rootChildFo)) {
                        logger.log(Level.WARNING, rootChildFo + " is not under " + srcRootFo);
                    }
                    logger.log(Level.WARNING, null, ex);
                }
            }
        }
    }
    
    private static void addFileObjects(Node node, Set<File> rootFiles) {
        Collection<? extends NonRecursiveFolder> folders = node.getLookup().lookup(new Lookup.Template<NonRecursiveFolder>(NonRecursiveFolder.class)).allInstances();
        List<File> nodeFiles = new ArrayList<File>();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                File file = FileUtil.toFile(nonRecursiveFolder.getFolder());
                if (file != null) {
                    nodeFiles.add(new FlatFolder(file.getAbsolutePath()));
                }
            }
        } else {
            Collection<? extends FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template<FileObject>(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                nodeFiles.addAll(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    Collection<File> doFiles = toFileCollection(dataObject.files());
                    nodeFiles.addAll(doFiles);
                }
            }
        }
        rootFiles.addAll(nodeFiles);
    }
    
    private static Collection<File> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            files.add(FileUtil.toFile(fo));
        }
        files.remove(null);
        return files;
    }    

    private VCSContext(Set<File> rootFiles, Set<File> exclusions, Object... elements) {
        Set<File> tempRootFiles = new HashSet<File>(rootFiles);
        Set<File> tempExclusions = new HashSet<File>(exclusions);
        this.unfilteredRootFiles = Collections.unmodifiableSet(new HashSet<File>(tempRootFiles));
        while (normalize(tempRootFiles, tempExclusions));
        this.rootFiles = Collections.unmodifiableSet(tempRootFiles);
        this.exclusions = Collections.unmodifiableSet(tempExclusions);
        this.elements = Lookups.fixed(elements);
    }

    private VCSContext(Node [] nodes, Set<File> rootFiles, Set<File> exclusions) {
        this(rootFiles, exclusions, nodes != null ? (Object[]) nodes : new Node[0]);
    }

    private VCSContext(Set<? extends FileObject> elements, Set<File> rootFiles, Set<File> exclusions) {
        this(rootFiles, exclusions, elements != null ? elements : Collections.EMPTY_SET);
    }

    private boolean normalize(Set<File> rootFiles, Set<File> exclusions) {
        for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            for (Iterator j = exclusions.iterator(); j.hasNext();) {
                File exclusion = (File) j.next();
                if (Utils.isAncestorOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusions, exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }
    
    private void exclusionRemoved(Set<File> exclusions, File exclusion, File root) {
        File [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            File child = exclusionChildren[i];
            if (!Utils.isAncestorOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    private static Set<File> substract(Set<File> roots, Set<File> exclusions, FileFilter filter) {
        Set<File> files = new HashSet<File>(roots);
        Set<File> checkedFiles = new HashSet<File>();
        for (File exclusion : exclusions) {
            assert exclusion != null;
            for (;;) {
                File parent = exclusion.getParentFile();
                /**
                 * only if the parent has not been checked yet - #158221
                 * otherwise skip adding of the siblings - they have been already added
                 */
                if (!checkedFiles.contains(exclusion.getParentFile())) {
                    addSiblings(files, exclusion, filter);
                    checkedFiles.add(parent);
                }
                exclusion = parent;
                files.remove(exclusion);
                if (roots.contains(exclusion)) break;
            }
        }
        files.removeAll(exclusions);
        return files;
    }

    private static void addSiblings(Set<File> files, File exclusion, FileFilter filter) {
        if (exclusion.getParentFile() == null) return;  // roots have no siblings
        File [] siblings = exclusion.getParentFile().listFiles();
        for (File sibling : siblings) {
            if (filter.accept(sibling)) files.add(sibling);
        }
        files.remove(exclusion);
    }

    private static final Set<File> emptySet() {
        return Collections.emptySet();
    }

    private void removeDuplicates(Set<File> files) {
        List<File> newFiles = new ArrayList<File>();
        outter: for (Iterator<File> i = files.iterator(); i.hasNext();) {
            File file = i.next();
            for (Iterator<File> j = newFiles.iterator(); j.hasNext();) {
                File includedFile = j.next();
                if (Utils.isAncestorOrEqual(includedFile, file) && (file.isFile() || !VersioningSupport.isFlat(includedFile))) continue outter;
                if (Utils.isAncestorOrEqual(file, includedFile) && (includedFile.isFile() || !VersioningSupport.isFlat(file))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
}
