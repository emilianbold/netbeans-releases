/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles.Operation;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * See javadoc in {@link CreatedModifiedFiles} for what this class and its
 * methods is supposed to do.
 */
final class CreatedModifiedFilesFactory {
    
    static CreatedModifiedFiles.Operation addLoaderSection(
            NbModuleProject project, String dataLoaderClass, String installBefore) {
        return new AddLoaderSection(project, dataLoaderClass, installBefore);
    }
    
    static CreatedModifiedFiles.Operation addLookupRegistration(
            NbModuleProject project, String interfaceClass, String implClass) {
        return new AddLookupRegistration(project, interfaceClass, implClass);
    }
    
    static CreatedModifiedFiles.Operation addModuleDependency(NbModuleProject project,
            String codeNameBase, int releaseVersion, SpecificationVersion version, boolean useInCompiler) {
        return new AddModuleDependency(project, codeNameBase, releaseVersion, version, useInCompiler);
    }
    
    static CreatedModifiedFiles.Operation bundleKey(NbModuleProject project,
            String key, String value, String bundlePath) {
        return new BundleKey(project, key, value, bundlePath);
    }
    
    static CreatedModifiedFiles.Operation bundleKeyDefaultBundle(
            NbModuleProject project, String key, String value) {
        return new BundleKey(project, key, value);
    }
    
    static CreatedModifiedFiles.Operation createFile(NbModuleProject project,
            String path, URL content) {
        return new CreateFile(project, path, content);
    }
    
    static CreatedModifiedFiles.Operation createFileWithSubstitutions(NbModuleProject project,
            String path, URL content, Map/*<String,String>*/ tokens) {
        return new CreateFile(project, path, content, tokens);
    }
    
    static CreatedModifiedFiles.Operation createLayerEntry(NbModuleProject project,
            String layerPath, String contentResourcePath, URL content, String generatedPath,
            Map/*<String,String>*/ substitutionTokens, String localizedDisplayName, Map attrs) {
        return new CreateLayerEntry(project, layerPath, contentResourcePath, content,
                generatedPath, substitutionTokens, localizedDisplayName, attrs);
    }
    
    static CreatedModifiedFiles.Operation createLayerSubtree(NbModuleProject project,
            String layerPath, String content, boolean includeRootElement) {
        return new CreateLayerSubtree(project, layerPath, content, includeRootElement);
    }
    
    static CreatedModifiedFiles.Operation orderLayerEntry(NbModuleProject project,
            String layerPath, String precedingItemName, String followingItemName) {
        return new OrderLayerEntry(project, layerPath, precedingItemName, followingItemName);
    }
    
    static CreatedModifiedFiles.Operation createLayerAttribute(NbModuleProject project,
            String parentPath, String attrName, String secondAttrName, String secondAttrValue) {
        return new CreateLayerAttribute(project, parentPath, attrName, secondAttrName, secondAttrValue);
    }
    
    private static abstract class OperationBase implements Operation {
        
        private NbModuleProject project;
        private SortedSet/*<String>*/ createdPaths;
        private SortedSet/*<String>*/ modifiedPaths;

        private String layerFile;
        
        protected OperationBase(NbModuleProject project) {
            this.project = project;
        }
        
        protected NbModuleProject getProject() {
            return project;
        }
        
        public String[] getModifiedPaths() {
            String[] s = new String[getModifiedPathsSet().size()];
            return (String[]) getModifiedPathsSet().toArray(s);
        }
        
        public String[] getCreatedPaths() {
            String[] s = new String[getCreatedPathsSet().size()];
            return (String[]) getCreatedPathsSet().toArray(s);
        }
        
        protected void addCreatedOrModifiedPath(String relPath) {
            if (getProject().getProjectDirectory().getFileObject(relPath) == null) {
                getCreatedPathsSet().add(relPath);
            } else {
                getModifiedPathsSet().add(relPath);
            }
        }
        
        protected void addPaths(Operation o) {
            getCreatedPathsSet().addAll(Arrays.asList(o.getCreatedPaths()));
            getModifiedPathsSet().addAll(Arrays.asList(o.getModifiedPaths()));
        }
        
        protected SortedSet/*<String>*/ getCreatedPathsSet() {
            if (createdPaths == null) {
                createdPaths = new TreeSet();
            }
            return createdPaths;
        }
        
        protected SortedSet/*<String>*/ getModifiedPathsSet() {
            if (modifiedPaths == null) {
                modifiedPaths = new TreeSet();
            }
            return modifiedPaths;
        }
        
        protected boolean addCreatedFileObject(FileObject fo) {
            return getCreatedPathsSet().add(getProjectPath(fo));
        }
        
        protected boolean addModifiedFileObject(FileObject fo) {
            return getModifiedPathsSet().add(getProjectPath(fo));
        }
        
        protected String getLayerFile() {
            if (layerFile == null) {
                ManifestManager mm = ManifestManager.getInstance(getProject().getManifest(), false);
                String srcDir = getProject().getSourceDirectoryPath();
                layerFile = srcDir + "/" + mm.getLayer(); // NOI18N
            }
            return layerFile;
        }
        
        /**
         * Doesn't check given arguments. Be sure they are valid as supposed by
         * {@link PropertyUtils#relativizeFile(File, File)} method.
         */
        private String getProjectPath(FileObject file) {
            return PropertyUtils.relativizeFile(
                    FileUtil.toFile(getProject().getProjectDirectory()),
                    FileUtil.normalizeFile(FileUtil.toFile(file)));
        }
        
    }
    
    private static final class CreateFile extends OperationBase {
        
        private String path;
        private URL content;
        private Map/*<String,String>*/ tokens;
        
        public CreateFile(NbModuleProject project, String path, URL content) {
            this(project, path, content, null);
        }
        
        public CreateFile(NbModuleProject project, String path, URL content, Map/*<String,String>*/ tokens) {
            super(project);
            this.path = path;
            this.content = content;
            this.tokens = tokens;
            addCreatedOrModifiedPath(path);
        }
        
        public void run() throws IOException {
            FileObject targetFO = FileUtil.createData(getProject().getProjectDirectory(), path);
            FileLock lock = targetFO.lock();
            try {
                if (tokens == null) {
                    copyByteAfterByte(lock, targetFO);
                } else {
                    copyAndSubstituteTokens(lock, targetFO);
                }
            } finally {
                lock.releaseLock();
            }
        }

        private void copyByteAfterByte(final FileLock lock, final FileObject targetFO) throws IOException {
            OutputStream os = targetFO.getOutputStream(lock);
            InputStream is = content.openStream();
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
                os.close();
            }
        }
        
        private void copyAndSubstituteTokens(final FileLock lock, final FileObject targetFO) throws IOException {
            PrintWriter pw = new PrintWriter(targetFO.getOutputStream(lock));
            BufferedReader br = new BufferedReader(new InputStreamReader(content.openStream()));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    pw.println(tokens == null ? line : replaceTokens(line));
                }
            } finally {
                br.close();
                pw.close();
            }
        }
        
        private String replaceTokens(String line) {
            for (Iterator it = tokens.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                line = line.replaceAll((String) entry.getKey(), (String) entry.getValue());
            }
            return line;
        }
        
    }
    
    private static final class BundleKey extends OperationBase {
        
        protected String bundlePath;
        protected String key;
        protected String value;
        
        public BundleKey(NbModuleProject project, String key, String value) {
            this(project, key, value, null);
        }
        
        public BundleKey(NbModuleProject project, String key, String value, String bundlePath) {
            super(project);
            this.key = key;
            this.value = value;
            if (bundlePath == null) {
                ManifestManager mm = ManifestManager.getInstance(getProject().getManifest(), false);
                String srcDir = getProject().getSourceDirectoryPath();
                this.bundlePath = srcDir + "/" + mm.getLocalizingBundle(); // NOI18N
            } else {
                this.bundlePath = bundlePath;
            }
            addCreatedOrModifiedPath(this.bundlePath);
        }
        
        public void run() throws IOException {
            FileObject prjDir = getProject().getProjectDirectory();
            FileObject bundleFO = FileUtil.createData(prjDir, bundlePath);
            EditableProperties ep = Util.loadProperties(bundleFO);
            ep.setProperty(key, value);
            Util.storeProperties(bundleFO, ep);
        }
        
    }
    
    private static final class AddLoaderSection extends OperationBase {
        
        private FileObject mfFO;
        
        private String dataLoaderClass;
        private String installBefore;
        
        public AddLoaderSection(NbModuleProject project, String dataLoaderClass, String installBefore) {
            super(project);
            this.dataLoaderClass = dataLoaderClass + ".class"; // NOI18N
            this.installBefore = installBefore;
            this.mfFO = getProject().getManifestFile();
            addModifiedFileObject(mfFO);
        }
        
        public void run() throws IOException {
            EditableManifest em = Util.loadManifest(mfFO);
            em.addSection(dataLoaderClass);
            em.setAttribute("OpenIDE-Module-Class", "Loader", dataLoaderClass); // NOI18N
            if (installBefore != null) {
                em.setAttribute("Install-Before", installBefore, dataLoaderClass); //NOI18N
            }
            Util.storeManifest(mfFO, em);
        }
        
    }
    
    private static final class AddModuleDependency extends OperationBase {

        private String codeNameBase;
        private int releaseVersion;
        private SpecificationVersion version;
        private boolean useInCompiler;
        
        public AddModuleDependency(NbModuleProject project, String codeNameBase,
                int releaseVersion, SpecificationVersion version, boolean useInCompiler) {
            super(project);
            this.codeNameBase = codeNameBase;
            this.releaseVersion = releaseVersion;
            this.version = version;
            this.useInCompiler = useInCompiler;
            getModifiedPathsSet().add("nbproject/project.xml"); // NOI18N
        }
        
        public void run() throws IOException {
            ModuleEntry me = getProject().getModuleList().getEntry(codeNameBase);
            assert me != null : "Cannot find module with the given codeNameBase (" + // NOI18N
                    codeNameBase + ") in the project's universe"; // NOI18N
            
            ProjectXMLManager pxm = new ProjectXMLManager(getProject().getHelper());
            
            // firstly check if the dependency is already not there
            Set currentDeps = pxm.getDirectDependencies(getProject().getPlatform());
            for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
                ModuleDependency md = (ModuleDependency) it.next();
                if (codeNameBase.equals(md.getModuleEntry().getCodeNameBase())) {
                    Util.err.log(ErrorManager.INFORMATIONAL, codeNameBase + " already added"); // NOI18N
                    return;
                }
            }
            
            ModuleDependency md = new ModuleDependency(me,
                    releaseVersion == -1 ? me.getReleaseVersion() : String.valueOf(releaseVersion),
                    version == null ? me.getSpecificationVersion() : version.toString(),
                    useInCompiler, false);
            pxm.addDependency(md);
            // XXX consider this carefully
            ProjectManager.getDefault().saveProject(getProject());
        }
        
    }
    
    private static final class AddLookupRegistration extends OperationBase {
        
        private String interfaceClassPath;
        private String implClass;
        
        public AddLookupRegistration(NbModuleProject project, String interfaceClass, String implClass) {
            super(project);
            this.implClass = implClass;
            this.interfaceClassPath = getProject().getSourceDirectoryPath() +
                    "/META-INF/services/" + interfaceClass; // NOI18N
            addCreatedOrModifiedPath(interfaceClassPath);
        }
        
        public void run() throws IOException {
            FileObject service = FileUtil.createData(
                    getProject().getProjectDirectory(),interfaceClassPath);
            
            String line = null;
            List lines = new ArrayList();
            InputStream serviceIS = service.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(serviceIS));
            try {
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                serviceIS.close();
            }
            
            FileLock lock = service.lock();
            try {
                PrintWriter pw = new PrintWriter(service.getOutputStream(lock));
                try {
                    for (int i = 0; i < lines.size(); i++) {
                        line = (String) lines.get(i);
                        if (i != lines.size() - 1 || !line.trim().equals("")) {
                            pw.println(line);
                        }
                    }
                    pw.println(implClass);
                    pw.println();
                } finally {
                    pw.close();
                }
            } finally {
                lock.releaseLock();
            }
            
        }
    }
    
    private static final class CreateLayerEntry extends OperationBase {
        
        private String layerPath;
        private String contentResourcePath;
        private String generatedPath;
        private Map/*<String,String>*/ tokens;
        private Map/*<String, Object>*/ fileAttributes;
        
        private Operation createBundleKey;
        private Operation createContentResource;
        
        public CreateLayerEntry(NbModuleProject project, String layerPath,
                String contentResourcePath, URL content, String generatedPath,
                Map/*<String,String>*/ substitutionTokens, String localizedDisplayName, Map attrs) {
            
            super(project);
            this.layerPath = layerPath;
            this.contentResourcePath = contentResourcePath;
            this.generatedPath = generatedPath;
            this.tokens = substitutionTokens;
            this.fileAttributes = attrs;
            addCreatedOrModifiedPath(getLayerFile());
            
            if (content != null) {
                this.createContentResource = new CreateFile(getProject(), generatedPath, content, tokens);
                addPaths(this.createContentResource);
            }
            if (localizedDisplayName != null) {
                this.createBundleKey = new BundleKey(getProject(), layerPath, localizedDisplayName);
                addPaths(this.createBundleKey);
            }
        }
        
        public void run() throws IOException{
            if (createContentResource != null) {
                createContentResource.run();
                if (contentResourcePath == null) {
                    String layer = getLayerFile();
                    String layerParent = layer.substring(0, layer.lastIndexOf("/"));
                    if (generatedPath.startsWith(layerParent)) {
                        contentResourcePath = generatedPath.substring(layerParent.length());
                        if (contentResourcePath.startsWith("/")) {
                            contentResourcePath = contentResourcePath.substring(1);
                        }
                    }
                }
            }
            String lbDotted = null;
            if (createBundleKey != null) {
                createBundleKey.run();
                ManifestManager mm = ManifestManager.getInstance(getProject().getManifest(), false);
                lbDotted = mm.getLocalizingBundle().replace('/', '.');
                if (lbDotted.endsWith(".properties")) { // NOI18N
                    lbDotted = lbDotted.substring(0, lbDotted.length() - 11);
                }
            }
            
            LayerUtil.createFile(getProject().getProjectDirectory(), getLayerFile(),
                    layerPath, contentResourcePath, lbDotted, fileAttributes);
        }
    }
    
    private static final class CreateLayerSubtree extends OperationBase {
        
        private String layerPath;
        private String subtreecontent;
        private boolean includeRootElement;
        
        public CreateLayerSubtree(NbModuleProject project, String layerPath,
                                  String subtreecontent, boolean includeRootElement) {
            
            super(project);
            this.layerPath = layerPath;
            this.subtreecontent = subtreecontent;
            this.includeRootElement = includeRootElement;
            addCreatedOrModifiedPath(getLayerFile());
            
        }
        
        public void run() throws IOException{
            LayerUtil.createSubTree(getProject().getProjectDirectory(), getLayerFile(),
                    layerPath, subtreecontent, includeRootElement);
        }
    }    

    private static final class OrderLayerEntry extends OperationBase {
        
        private String layerPath;
        private String precedingItemName;
        private String followingItemName;
        
        public OrderLayerEntry(NbModuleProject project, String layerPath,
                String precedingItemName, String followingItemName) {
            
            super(project);
            this.layerPath = layerPath;
            this.precedingItemName = precedingItemName;
            this.followingItemName = followingItemName;
            addCreatedOrModifiedPath(getLayerFile());
        }
        
        public void run() throws IOException{
            LayerUtil.orderEntry(getProject().getProjectDirectory(), getLayerFile(),
                    layerPath, precedingItemName, followingItemName);
        }
    }
    
    private static final class CreateLayerAttribute extends OperationBase {
        
        private String parentPath;
        private String attrName;
        private String secondAttrName;
        private String secondAttrValue;
        
        public CreateLayerAttribute(NbModuleProject project, String parentPath,
                String attrName, String secondAttrName, String secondAttrValue) {
            
            super(project);
            this.parentPath = parentPath;
            this.attrName = attrName;
            this.secondAttrName = secondAttrName;
            this.secondAttrValue = secondAttrValue;
            addCreatedOrModifiedPath(getLayerFile());
        }
        
        public void run() throws IOException{
            LayerUtil.createAttribute(getProject().getProjectDirectory(), getLayerFile(),
                    parentPath, attrName, secondAttrName, secondAttrValue);
        }
    }
    
}

