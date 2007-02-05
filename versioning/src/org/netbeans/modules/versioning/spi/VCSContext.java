/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.FlatFolder;
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
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

import java.io.File;
import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

/**
 * This encapsulates a context, typically set of selected files or nodes. Context is passed to VCSAnnotators when
 * asked for actions available on a given context or to annotate a name (label) representing a context.
 * 
 * @author Maros Sandor
 */
public final class VCSContext {
    
    public static final VCSContext Empty = new VCSContext(new Node[0], emptySet(), emptySet() );

    /**
     * Caching of current context for performance reasons, also see #72006.
     */
    private static VCSContext  contextCached;    
    private static Reference<Node[]> contextNodesCached = new WeakReference<Node []>(null); 

    private final Node []   nodes;
    
    private final Set<File> rootFiles;
    private final Set<File> exclusions;

    public static VCSContext forLookup(Lookup lookup) {
        Lookup.Result<Node> result = lookup.lookup(new Lookup.Template<Node>(Node.class));
        Collection<? extends Node> nodes = result.allInstances();
        return VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
    }
    
    public static VCSContext forFiles(Set<File> rootFiles) {
        return new VCSContext(null, Collections.unmodifiableSet(new HashSet<File>(rootFiles)), emptySet());
    }

    /**
     * Initializes the context from array of nodes (typically currently activated nodes).
     * 
     * @param nodes array of Nodes
     */
    public synchronized static VCSContext forNodes(Node[] nodes) {
        if (Arrays.equals(contextNodesCached.get(), nodes)) return contextCached;
        Set<File> files = new HashSet<File>(nodes.length);
        Set<File> rootFiles = new HashSet<File>(nodes.length);
        Set<File> rootFileExclusions = new HashSet<File>(5);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            File aFile = node.getLookup().lookup(File.class);
            if (aFile != null) {
                files.add(aFile);
                rootFiles.add(aFile);
                continue;
            }
            Project project =  node.getLookup().lookup(Project.class);
            if (project != null) {
                addProjectFiles(rootFiles, rootFileExclusions, project);
                continue;
            }
            addFileObjects(node, files, rootFiles);
        }
        
        contextCached = new VCSContext(nodes, rootFiles, rootFileExclusions);
        contextNodesCached = new WeakReference<Node []>(nodes);
        return contextCached;
    }
    
    private static void addProjectFiles(Collection<File> rootFiles, Collection<File> rootFilesExclusions, Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            rootFiles.add(rootFile);
            FileObject [] rootChildren = srcRootFo.getChildren();
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                File child = FileUtil.toFile(rootChildFo);
                // TODO: #60516 deep scan is required here but not performed due to performace reasons 
                if (!sourceGroup.contains(rootChildFo) && SharabilityQuery.getSharability(child) != SharabilityQuery.NOT_SHARABLE) {
                    rootFilesExclusions.add(child);
                }
            }
        }
    }
    
    private static void addFileObjects(Node node, Set<File> files, Set<File> rootFiles) {
        Collection<NonRecursiveFolder> folders = node.getLookup().lookup(new Lookup.Template(NonRecursiveFolder.class)).allInstances();
        List<File> nodeFiles = new ArrayList<File>();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                nodeFiles.add(new FlatFolder(FileUtil.toFile(nonRecursiveFolder.getFolder()).getAbsolutePath()));
            }
        } else {
            Collection<FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template(FileObject.class)).allInstances();
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
        files.addAll(nodeFiles);
        rootFiles.addAll(nodeFiles);
    }
    
    private static Collection<File> toFileCollection(Collection<FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            files.add(FileUtil.toFile(fo));
        }
        files.remove(null);
        return files;
    }    

    private VCSContext(Node [] nodes, Set<File> rootFiles, Set<File> exclusions) {
        this.nodes = nodes; // TODO: construct artificial nodes
        this.rootFiles = Collections.unmodifiableSet(new HashSet<File>(rootFiles));
        this.exclusions = Collections.unmodifiableSet(new HashSet<File>(exclusions));
    }

    public Node[] getNodes() {
        return nodes;
    }
    
    public Set<File> getRootFiles() {
        return rootFiles;
    }

    public Set<File> getExclusions() {
        return exclusions;
    }

    public boolean contains(File file) {
        outter : for (File root : rootFiles) {
            if (Utils.isParentOrEqual(root, file)) {
                for (File excluded : exclusions) {
                    if (Utils.isParentOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    private static final Set<File> emptySet() {
        return Collections.emptySet();
    }
}
