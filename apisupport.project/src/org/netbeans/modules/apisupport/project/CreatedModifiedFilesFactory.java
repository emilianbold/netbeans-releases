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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * See javadoc in {@link CreatedModifiedFiles} for what this class and its
 * methods is supposed to do.
 */
public final class CreatedModifiedFilesFactory {
    
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
    
    static CreatedModifiedFiles.Operation layerModifications(NbModuleProject project, CreatedModifiedFiles.LayerOperation op, Set/*<String>*/ externalFiles, CreatedModifiedFiles cmf) {
        return new LayerModifications(project, op, externalFiles, cmf);
    }
    
    static CreatedModifiedFiles.Operation createLayerEntry(CreatedModifiedFiles cmf, NbModuleProject project,
            String layerPath, URL content,
            Map/*<String,String>*/ substitutionTokens, String localizedDisplayName, Map attrs) {
        return new CreateLayerEntry(cmf, project, layerPath, content,
                substitutionTokens, localizedDisplayName, attrs);
    }
    
    public static abstract class OperationBase implements CreatedModifiedFiles.Operation {
        
        private NbModuleProject project;
        private SortedSet/*<String>*/ createdPaths;
        private SortedSet/*<String>*/ modifiedPaths;
        
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
            // XXX this is probably wrong, since it might be created by an earlier op:
            if (getProject().getProjectDirectory().getFileObject(relPath) == null) {
                getCreatedPathsSet().add(relPath);
            } else {
                getModifiedPathsSet().add(relPath);
            }
        }
        
        protected void addPaths(CreatedModifiedFiles.Operation o) {
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
                    copyByteAfterByte(content, lock, targetFO);
                } else {
                    copyAndSubstituteTokens(content, lock, targetFO, tokens);
                }
            } finally {
                lock.releaseLock();
            }
        }
        
    }
    
    private static void copyByteAfterByte(URL content, FileLock lock, FileObject targetFO) throws IOException {
        OutputStream os = targetFO.getOutputStream(lock);
        InputStream is = content.openStream();
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
            os.close();
        }
    }
    
    private static void copyAndSubstituteTokens(URL content, FileLock lock, FileObject targetFO, Map/*<String,String>*/ tokens) throws IOException {
        PrintWriter pw = new PrintWriter(targetFO.getOutputStream(lock));
        BufferedReader br = new BufferedReader(new InputStreamReader(content.openStream()));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(tokens == null ? line : replaceTokens(tokens, line));
            }
        } finally {
            br.close();
            pw.close();
        }
    }
    
    private static String replaceTokens(Map/*<String,String>*/ tokens, String line) {
        for (Iterator it = tokens.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            line = line.replaceAll((String) entry.getKey(), (String) entry.getValue());
        }
        return line;
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
        
        private CreatedModifiedFiles.Operation createBundleKey;
        private CreatedModifiedFiles.Operation layerOp;
        
        public CreateLayerEntry(CreatedModifiedFiles cmf, NbModuleProject project, final String layerPath,
                final URL content,
                final Map/*<String,String>*/ tokens, final String localizedDisplayName, final Map attrs) {
            
            super(project);
            CreatedModifiedFiles.LayerOperation op = new CreatedModifiedFiles.LayerOperation() {
                public void run(FileSystem layer) throws IOException {
                    FileObject targetFO = FileUtil.createData(layer.getRoot(), layerPath);
                    if (content != null) {
                        FileLock lock = targetFO.lock();
                        try {
                            if (tokens == null) {
                                copyByteAfterByte(content, lock, targetFO);
                            } else {
                                copyAndSubstituteTokens(content, lock, targetFO, tokens);
                            }
                        } finally {
                            lock.releaseLock();
                        }
                    }
                    if (localizedDisplayName != null) {
                        String bundlePath = ManifestManager.getInstance(getProject().getManifest(), false).getLocalizingBundle();
                        String suffix = ".properties"; // NOI18N
                        if (bundlePath.endsWith(suffix)) {
                            String name = bundlePath.substring(0, bundlePath.length() - suffix.length()).replace('/', '.');
                            targetFO.setAttribute("SystemFileSystem.localizingBundle", name); // NOI18N
                        } else {
                            // XXX what?
                        }
                    }
                    if (attrs != null) {
                        Iterator it = attrs.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            targetFO.setAttribute((String) entry.getKey(), entry.getValue());
                        }
                    }
                }
            };
            Set/*<String>*/ externalFiles;
            if (content != null) {
                FileObject xml = LayerUtils.layerForProject(project).getLayerFile();
                FileObject parent = xml != null ? xml.getParent() : null;
                // XXX this is not fully accurate since if two ops would both create the same file,
                // really the second one would automatically generate a uniquified name... but close enough!
                externalFiles = Collections.singleton(LayerUtils.findGeneratedName(parent, layerPath));
            } else {
                externalFiles = Collections.EMPTY_SET;
            }
            layerOp = new LayerModifications(project, op, externalFiles, cmf);
            addPaths(layerOp);
            if (localizedDisplayName != null) {
                this.createBundleKey = new BundleKey(getProject(), layerPath, localizedDisplayName);
                addPaths(this.createBundleKey);
            }
        }
        
        public void run() throws IOException{
            layerOp.run();
            if (createBundleKey != null) {
                createBundleKey.run();
            }
        }
    }
    
    private static final class LayerModifications implements CreatedModifiedFiles.Operation {

        private final NbModuleProject project;
        private final CreatedModifiedFiles.LayerOperation op;
        private final Set/*<String>*/ externalFiles;
        private final CreatedModifiedFiles cmf;
        
        public LayerModifications(NbModuleProject project, CreatedModifiedFiles.LayerOperation op, Set/*<String>*/ externalFiles, CreatedModifiedFiles cmf) {
            this.project = project;
            this.op = op;
            this.externalFiles = externalFiles;
            this.cmf = cmf;
        }
        
        public void run() throws IOException {
            op.run(cmf.getLayerHandle().layer(true));
        }
        
        private String layerPrefix() {
            FileObject layer = cmf.getLayerHandle().getLayerFile();
            if (layer == null) {
                return null;
            }
            return FileUtil.getRelativePath(project.getProjectDirectory(), layer);
        }
        
        public String[] getModifiedPaths() {
            String layerPath = layerPrefix();
            if (layerPath == null) {
                return new String[0];
            }
            return new String[] {layerPath};
        }
        
        public String[] getCreatedPaths() {
            String layerPath = layerPrefix();
            if (layerPath == null) {
                return new String[0];
            }
            int slash = layerPath.lastIndexOf('/');
            String prefix = layerPath.substring(0, slash + 1);
            SortedSet s = new TreeSet();
            Iterator it = externalFiles.iterator();
            while (it.hasNext()) {
                s.add(prefix + (String) it.next());
            }
            return (String[]) s.toArray(new String[s.size()]);
        }
        
    }
    
}

