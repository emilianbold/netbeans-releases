/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;

/**
 * See javadoc in {@link CreatedModifiedFiles} for what this class and its
 * methods is supposed to do.
 */
public final class CreatedModifiedFilesFactory {
    
    static CreatedModifiedFiles.Operation addLoaderSection(
            Project project, String dataLoaderClass, String installBefore) {
        return new AddLoaderSection(project, dataLoaderClass, installBefore);
    }
    
    static CreatedModifiedFiles.Operation addLookupRegistration(
            Project project, String interfaceClass, String implClass, boolean inTests) {
        return new AddLookupRegistration(project, interfaceClass, implClass, inTests);
    }
    
    static CreatedModifiedFiles.Operation addModuleDependency(Project project,
            String codeNameBase, String releaseVersion, SpecificationVersion version, boolean useInCompiler) {
        return new AddModuleDependency(project, codeNameBase, releaseVersion, version, useInCompiler);
    }
    
    static CreatedModifiedFiles.Operation bundleKey(Project project,
            String key, String value, String bundlePath) {
        return new BundleKey(project, key, value, bundlePath);
    }
    
    static CreatedModifiedFiles.Operation bundleKeyDefaultBundle(
            Project project, String key, String value) {
        return new BundleKey(project, key, value);
    }
    
    static CreatedModifiedFiles.Operation createFile(Project project,
            String path, URL content) {
        return new CreateFile(project, path, content);
    }
    
    static CreatedModifiedFiles.Operation createFileWithSubstitutions(Project project,
            String path, URL content, Map<String,String> tokens) {
        return new CreateFile(project, path, content, tokens);
    }
    
    static CreatedModifiedFiles.Operation layerModifications(Project project, CreatedModifiedFiles.LayerOperation op, Set<String> externalFiles, CreatedModifiedFiles cmf) {
        return new LayerModifications(project, op, externalFiles, cmf);
    }
    
    static CreatedModifiedFiles.Operation createLayerEntry(CreatedModifiedFiles cmf, Project project,
            String layerPath, URL content,
            Map<String,String> substitutionTokens, String localizedDisplayName, Map attrs) {
        return new CreateLayerEntry(cmf, project, layerPath, content,
                substitutionTokens, localizedDisplayName, attrs);
    }
    
    static CreatedModifiedFiles.Operation manifestModification(Project project, String section,
            Map<String,String> attributes) {
        CreatedModifiedFilesFactory.ModifyManifest retval =
                new CreatedModifiedFilesFactory.ModifyManifest(project);
        for (Iterator it = attributes.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            retval.setAttribute(name, value, section);
        }
        return retval;
    }
    
    static CreatedModifiedFiles.Operation propertiesModification(Project project,
            String propertyPath, Map<String,String> properties) {
        CreatedModifiedFilesFactory.ModifyProperties retval =
                new CreatedModifiedFilesFactory.ModifyProperties(project, propertyPath);
        for (Iterator it = properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            retval.setProperty(name, value);
        }
        return retval;
    }
    
    
    public static abstract class OperationBase implements CreatedModifiedFiles.Operation {
        
        private Project project;
        private SortedSet<String> createdPaths;
        private SortedSet<String> modifiedPaths;
        private SortedSet<String> invalidPaths;
        
        protected OperationBase(Project project) {
            this.project = project;
        }
        
        protected Project getProject() {
            return project;
        }
        
        protected NbModuleProvider getModuleInfo() {
            return getProject().getLookup().lookup(NbModuleProvider.class);
        }
        
        public String[] getModifiedPaths() {
            String[] s = new String[getModifiedPathsSet().size()];
            return getModifiedPathsSet().toArray(s);
        }
        
        public String[] getCreatedPaths() {
            String[] s = new String[getCreatedPathsSet().size()];
            return getCreatedPathsSet().toArray(s);
        }
        
        public String[] getInvalidPaths() {
            String[] s = new String[getInvalidPathsSet().size()];
            return getInvalidPathsSet().toArray(s);
            
        }
        
        protected void addCreatedOrModifiedPath(String relPath, boolean allowFileModification) {
            // XXX this is probably wrong, since it might be created by an earlier op:
            if (getProject().getProjectDirectory().getFileObject(relPath) == null) {
                getCreatedPathsSet().add(relPath);
            } else {
                if (allowFileModification) {
                    getModifiedPathsSet().add(relPath);
                } else {
                    getInvalidPathsSet().add(relPath);
                }
            }
        }
        
        protected void addPaths(CreatedModifiedFiles.Operation o) {
            getCreatedPathsSet().addAll(Arrays.asList(o.getCreatedPaths()));
            getModifiedPathsSet().addAll(Arrays.asList(o.getModifiedPaths()));
            getInvalidPathsSet().addAll(Arrays.asList(o.getInvalidPaths()));
        }
        
        protected SortedSet<String> getCreatedPathsSet() {
            if (createdPaths == null) {
                createdPaths = new TreeSet<String>();
            }
            return createdPaths;
        }
        
        protected SortedSet<String> getInvalidPathsSet() {
            if (invalidPaths == null) {
                invalidPaths = new TreeSet<String>();
            }
            return invalidPaths;
        }
        
        protected SortedSet<String> getModifiedPathsSet() {
            if (modifiedPaths == null) {
                modifiedPaths = new TreeSet<String>();
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
        private Map<String,String> tokens;
        
        public CreateFile(Project project, String path, URL content) {
            this(project, path, content, null);
        }
        
        public CreateFile(Project project, String path, URL content, Map<String,String> tokens) {
            super(project);
            this.path = path;
            if (content == null) {
                throw new NullPointerException();
            }
            this.content = content;
            this.tokens = tokens;
            addCreatedOrModifiedPath(path, false);
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
    
    private static void copyAndSubstituteTokens(URL content, FileLock lock, FileObject targetFO, Map<String,String> tokens) throws IOException {
        // #64023: at least XML files must always use UTF-8; but user probably expects *.java to use platform default?
        boolean useUTF8 = targetFO.hasExt("xml"); // NOI18N
        OutputStream os = targetFO.getOutputStream(lock);
        try {
            PrintWriter pw;
            if (useUTF8) {
                pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); // NOI18N
            } else {
                pw = new PrintWriter(os);
            }
            try {
                InputStream is = content.openStream();
                try {
                    Reader r;
                    if (useUTF8) {
                        r = new InputStreamReader(is, "UTF-8"); // NOI18N
                    } else {
                        r = new InputStreamReader(is);
                    }
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        pw.println(tokens == null ? line : replaceTokens(tokens, line));
                    }
                } finally {
                    is.close();
                }
            } finally {
                pw.close();
            }
        } finally {
            os.close();
        }
    }
    
    private static String replaceTokens(Map<String,String> tokens, String line) {
        for (Iterator it = tokens.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            line = line.replaceAll((String) entry.getKey(), (String) entry.getValue());
        }
        return line;
    }
    
    private static final class BundleKey extends OperationBase {
        
        private final String bundlePath;
        private final String key;
        private final String value;
        
        public BundleKey(Project project, String key, String value) {
            this(project, key, value, null);
        }
        
        public BundleKey(Project project, String key, String value, String bundlePath) {
            super(project);
            this.key = key;
            this.value = value;
            if (bundlePath == null) {
                
                ManifestManager mm = ManifestManager.getInstance(Util.getManifest(getModuleInfo().getManifestFile()), false);
                String srcDir = getModuleInfo().getResourceDirectoryPath(false);
                this.bundlePath = srcDir + "/" + mm.getLocalizingBundle(); // NOI18N
            } else {
                this.bundlePath = bundlePath;
            }
            addCreatedOrModifiedPath(this.bundlePath, true);
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
        
        public AddLoaderSection(Project project, String dataLoaderClass, String installBefore) {
            super(project);
            this.dataLoaderClass = dataLoaderClass + ".class"; // NOI18N
            this.installBefore = installBefore;
            this.mfFO = getModuleInfo().getManifestFile();
            addModifiedFileObject(mfFO);
        }
        
        public void run() throws IOException {
            //#65420 it can happen the manifest is currently being edited. save it
            // and cross fingers because it can be in inconsistent state
            try {
                DataObject dobj = DataObject.find(mfFO);
                SaveCookie safe = dobj.getCookie(SaveCookie.class);
                if (safe != null) {
                    safe.save();
                }
            } catch (DataObjectNotFoundException ex) {
                Util.err.notify(ErrorManager.WARNING, ex);
            }
            
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
        private String releaseVersion;
        private SpecificationVersion specVersion;
        private boolean useInCompiler;
        
        public AddModuleDependency(Project project, String codeNameBase,
                String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler) {
            super(project);
            this.codeNameBase = codeNameBase;
            this.releaseVersion = releaseVersion;
            this.specVersion = specVersion;
            this.useInCompiler = useInCompiler;
            getModifiedPathsSet().add(getModuleInfo().getProjectFilePath()); // NOI18N
        }
        
        public void run() throws IOException {
            getModuleInfo().addDependency(codeNameBase, releaseVersion, specVersion, useInCompiler);
            // XXX consider this carefully
            ProjectManager.getDefault().saveProject(getProject());
        }
        
    }
    
    private static final class AddLookupRegistration extends OperationBase {
        
        private String interfaceClassPath;
        private String implClass;
        
        public AddLookupRegistration(Project project, String interfaceClass, String implClass, boolean inTests) {
            super(project);
            this.implClass = implClass;
            this.interfaceClassPath = getModuleInfo().getResourceDirectoryPath(inTests) + // NOI18N
                    "/META-INF/services/" + interfaceClass; // NOI18N
            addCreatedOrModifiedPath(interfaceClassPath, true);
        }
        
        public void run() throws IOException {
            FileObject service = FileUtil.createData(
                    getProject().getProjectDirectory(),interfaceClassPath);
            
            List<String> lines = new ArrayList<String>();
            InputStream serviceIS = service.getInputStream();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(serviceIS, "UTF-8")); // NOI18N
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                serviceIS.close();
            }
            
            FileLock lock = service.lock();
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(service.getOutputStream(lock), "UTF-8")); // NOI18N
                try {
                    Iterator<String> it = lines.iterator();
                    while (it.hasNext()) {
                        String line = it.next();
                        if (it.hasNext() || !line.trim().equals("")) {
                            pw.println(line);
                        }
                    }
                    pw.println(implClass);
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
        
        public CreateLayerEntry(CreatedModifiedFiles cmf, Project project, final String layerPath,
                final URL content,
                final Map<String,String> tokens, final String localizedDisplayName, final Map attrs) {
            
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
                        String bundlePath = ManifestManager.getInstance(Util.getManifest(getModuleInfo().getManifestFile()), false).getLocalizingBundle();
                        String suffix = ".properties"; // NOI18N
                        if (bundlePath != null && bundlePath.endsWith(suffix)) {
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
            Set<String> externalFiles;
            if (content != null) {
                FileObject xml = LayerUtils.layerForProject(project).getLayerFile();
                FileObject parent = xml != null ? xml.getParent() : null;
                // XXX this is not fully accurate since if two ops would both create the same file,
                // really the second one would automatically generate a uniquified name... but close enough!
                externalFiles = Collections.singleton(LayerUtils.findGeneratedName(parent, layerPath));
            } else {
                externalFiles = Collections.emptySet();
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
        
        private final Project project;
        private final CreatedModifiedFiles.LayerOperation op;
        private final Set<String> externalFiles;
        private final CreatedModifiedFiles cmf;
        
        public LayerModifications(Project project, CreatedModifiedFiles.LayerOperation op, Set<String> externalFiles, CreatedModifiedFiles cmf) {
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
            SortedSet<String> s = new TreeSet<String>();
            for (String file : externalFiles) {
                s.add(prefix + file);
            }
            return s.toArray(new String[s.size()]);
        }
        
        public String[] getInvalidPaths() {
            //TODO applicable here?
            return new String[0];
        }
        
    }
    
    /**
     * Operation for making changes in manifest
     */
    public static class ModifyManifest extends CreatedModifiedFilesFactory.OperationBase {
        private FileObject manifestFile;
        private Map<String,Map<String,String>> attributesToAdd;
        
        /**
         * @param project
         */
        public ModifyManifest(final Project project) {
            super(project);
            this.attributesToAdd = new HashMap<String,Map<String,String>>();
            addModifiedFileObject(getManifestFile());
        }
        
        /**
         * Adds requirement for modifying attribute for the main section. How attribute
         * will be modified depends on implementation of method {@link performModification}.
         * @param name the attribute name
         * @param value the new attribute value
         */
        public final void setAttribute(final String name, final  String value) {
            setAttribute(name, value, "null");//NOI18N
        }
        
        /**
         * Adds requirement for modifying attribute. How attribute
         * will be modified depends on implementation of method {@link performModification}.
         * @param name the attribute name
         * @param value the new attribute value
         * @param section the name of the section or null for the main section
         */
        public final void setAttribute(final String name, final String value, final  String section) {
            Map<String,String> attribs = attributesToAdd.get(section);
            if (attribs == null) {
                attribs = new HashMap<String,String>();
                attributesToAdd.put(section, attribs);
            }
            attribs.put(name, value);
        }
        
        /**
         * Creates section if doesn't exists and set all attributes
         * @param em EditableManifest where attribute represented by other
         * parameters is going to be added
         * @param name the attribute name
         * @param value the new attribute value
         * @param section the name of the section to add it to, or null for the main section
         */
        protected void performModification(final EditableManifest em,final String name,final String value,
                final String section)  {
            if (section != null && em.getSectionNames().contains(section)) {
                em.addSection(section);
            }
            em.setAttribute(name, value, section);
        }
        
        public final void run() throws IOException {
            ensureSavingFirst();
            
            EditableManifest em = Util.loadManifest(getManifestFile());
            for (Map.Entry<String,Map<String,String>> entry : attributesToAdd.entrySet()) {
                String section = entry.getKey();
                for (Map.Entry<String,String> subentry : entry.getValue().entrySet()) {
                    performModification(em, subentry.getKey(), subentry.getValue(),
                            (("null".equals(section)) ? null : section)); // NOI18N
                }
            }
            
            Util.storeManifest(getManifestFile(), em);
        }
        
        
        private FileObject getManifestFile() {
            if (manifestFile == null) {
                manifestFile = getModuleInfo().getManifestFile();
            }
            return manifestFile;
        }
        
        private void ensureSavingFirst() throws IOException {
            //#65420 it can happen the manifest is currently being edited. save it
            // and cross fingers because it can be in inconsistent state
            try {
                DataObject dobj = DataObject.find(getManifestFile());
                SaveCookie safe = dobj.getCookie(SaveCookie.class);
                if (safe != null) {
                    safe.save();
                }
            } catch (DataObjectNotFoundException ex) {
                Util.err.notify(ErrorManager.WARNING, ex);
            }
        }
    }
    
    /**
     * Operation for making changes in properties
     */
    private  static class ModifyProperties extends CreatedModifiedFilesFactory.OperationBase {
        private Map<String,String> properties;
        private final String propertyPath;
        private EditableProperties ep;
        private FileObject propertiesFile;
        
        private ModifyProperties(final Project project, final String propertyPath) {
            super(project);
            this.propertyPath= propertyPath;
            addCreatedOrModifiedPath(propertyPath,true);
        }
        
        public void run() throws IOException {
            EditableProperties ep = getEditableProperties();
            ep.putAll(getProperties());
            Util.storeProperties(getPropertyFile(),ep);
        }
        
        public final void setProperty(final String name, final String value) {
            getProperties().put(name, value);
        }
        
        protected final FileObject getPropertyFile() throws IOException {
            if (propertiesFile == null) {
                FileObject projectDirectory = getProject().getProjectDirectory();
                propertiesFile = FileUtil.createData(projectDirectory, propertyPath);
            }
            return propertiesFile;
        }
        
        protected final EditableProperties getEditableProperties() throws IOException {
            if (ep == null) {
                ep = Util.loadProperties(getPropertyFile());
            }
            return ep;
        }
        
        protected final Map<String,String> getProperties() {
            if (properties == null) {
                this.properties = new HashMap<String,String>();
            }
            return properties;
        }
    }
}

