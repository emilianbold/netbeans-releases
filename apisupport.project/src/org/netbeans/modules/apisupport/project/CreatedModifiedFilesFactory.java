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

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.PlainDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
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
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;

/**
 * See javadoc in {@link CreatedModifiedFiles} for what this class and its
 * methods is supposed to do.
 */
public final class CreatedModifiedFilesFactory {

    private CreatedModifiedFilesFactory() {}

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
            String path, FileObject content) {
        return new CreateFile(project, path, content);
    }
    
    static CreatedModifiedFiles.Operation createFileWithSubstitutions(Project project,
            String path, FileObject content, Map<String,String> tokens) {
        return new CreateFile(project, path, content, tokens);
    }
    
    static CreatedModifiedFiles.Operation layerModifications(Project project, CreatedModifiedFiles.LayerOperation op, Set<String> externalFiles, CreatedModifiedFiles cmf) {
        return new LayerModifications(project, op, externalFiles, cmf);
    }
    
    static CreatedModifiedFiles.Operation createLayerEntry(CreatedModifiedFiles cmf, Project project,
            String layerPath, FileObject content,
            Map<String,String> substitutionTokens, String localizedDisplayName, Map<String,Object> attrs) {
        return new CreateLayerEntry(cmf, project, layerPath, content,
                substitutionTokens, localizedDisplayName, attrs);
    }
    
    static CreatedModifiedFiles.Operation manifestModification(Project project, String section,
            Map<String,String> attributes) {
        CreatedModifiedFilesFactory.ModifyManifest retval =
                new CreatedModifiedFilesFactory.ModifyManifest(project);
        for (Map.Entry<String,String> entry : attributes.entrySet()) {
            retval.setAttribute(entry.getKey(), entry.getValue(), section);
        }
        return retval;
    }
    
    static CreatedModifiedFiles.Operation propertiesModification(Project project,
            String propertyPath, Map<String,String> properties) {
        CreatedModifiedFilesFactory.ModifyProperties retval =
                new CreatedModifiedFilesFactory.ModifyProperties(project, propertyPath);
        for (Map.Entry<String,String> entry : properties.entrySet()) {
            retval.setProperty(entry.getKey(), entry.getValue());
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
        private FileObject content;
        private Map<String,String> tokens;
        
        public CreateFile(Project project, String path, FileObject content) {
            this(project, path, content, null);
        }
        
        public CreateFile(Project project, String path, FileObject content, Map<String,String> tokens) {
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
            FileObject target = FileUtil.createData(getProject().getProjectDirectory(), path);
            if (tokens == null) {
                copyByteAfterByte(content, target);
            } else {
                copyAndSubstituteTokens(content, target, tokens);
            }
            // #129446: form editor doesn't work sanely unless you do this:
            if (target.hasExt("form")) { // NOI18N
                FileObject java = FileUtil.findBrother(target, "java"); // NOI18N
                if (java != null) {
                    java.setAttribute("justCreatedByNewWizard", true); // NOI18N
                }
            } else if (target.hasExt("java") && FileUtil.findBrother(target, "form") != null) { // NOI18N
                target.setAttribute("justCreatedByNewWizard", true); // NOI18N
            }
        }
        
    }
    
    private static void copyByteAfterByte(FileObject content, FileObject target) throws IOException {
        OutputStream os = target.getOutputStream();
        try {
            InputStream is = content.getInputStream();
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }
    
    private static void copyAndSubstituteTokens(FileObject content, FileObject target, Map<String,String> tokens) throws IOException {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("freemarker");
        assert engine != null : scriptEngineManager.getEngineFactories();
        Map<String,Object> bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        String basename = target.getName();
        for (CreateFromTemplateAttributesProvider provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
            Map<String,?> map = provider.attributesFor(DataObject.find(content), DataFolder.findFolder(target.getParent()), basename);
            if (map != null) {
                bindings.putAll(map);
            }
        }
        bindings.put("name", basename.replaceFirst("\\.[^./]+$", "")); // NOI18N
        bindings.put("user", System.getProperty("user.name")); // NOI18N
        Date d = new Date();
        bindings.put("date", DateFormat.getDateInstance().format(d)); // NOI18N
        bindings.put("time", DateFormat.getTimeInstance().format(d)); // NOI18N
        bindings.put("nameAndExt", target.getNameExt()); // NOI18N
        bindings.putAll(tokens);
        Charset targetEnc = FileEncodingQuery.getEncoding(target);
        Charset sourceEnc = FileEncodingQuery.getEncoding(content);
        bindings.put("encoding", targetEnc.name());
        Writer w = new OutputStreamWriter(target.getOutputStream(), targetEnc);
        try {
            IndentEngine format = IndentEngine.find(content.getMIMEType());
            if (format != null) {
                PlainDocument doc = new PlainDocument();
                doc.putProperty(PlainDocument.StreamDescriptionProperty, content);
                w = format.createWriter(doc, 0, w);
            }
            engine.getContext().setWriter(w);
            engine.getContext().setAttribute(FileObject.class.getName(), content, ScriptContext.ENGINE_SCOPE);
            engine.getContext().setAttribute(ScriptEngine.FILENAME, content.getNameExt(), ScriptContext.ENGINE_SCOPE);
            Reader is = new InputStreamReader(content.getInputStream(), sourceEnc);
            try {
                engine.eval(is);
            } catch (ScriptException x) {
                throw (IOException) new IOException(x.toString()).initCause(x);
            } finally {
                is.close();
            }
        } finally {
            w.close();
        }
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
        
        private final CreatedModifiedFiles.Operation createBundleKey;
        private final CreatedModifiedFiles.Operation layerOp;
        
        public CreateLayerEntry(final CreatedModifiedFiles cmf, final Project project, final String layerPath,
                final FileObject content,
                final Map<String,String> tokens, final String localizedDisplayName, final Map<String,Object> attrs) {
            
            super(project);
            final String locBundleKey = (localizedDisplayName != null ? LayerUtils.generateBundleKeyForFile(layerPath) : null);

            CreatedModifiedFiles.LayerOperation op = new CreatedModifiedFiles.LayerOperation() {
                public void run(FileSystem layer) throws IOException {
                    FileObject targetFO = FileUtil.createData(layer.getRoot(), layerPath);
                    if (content != null) {
                        if (tokens == null) {
                            copyByteAfterByte(content, targetFO);
                        } else {
                            copyAndSubstituteTokens(content, targetFO, tokens);
                        }
                    }
                    if (localizedDisplayName != null) {
                        String bundlePath = ManifestManager.getInstance(Util.getManifest(getModuleInfo().getManifestFile()), false).getLocalizingBundle();
                        String suffix = ".properties"; // NOI18N
                        if (bundlePath != null && bundlePath.endsWith(suffix)) {
                            String name = bundlePath.substring(0, bundlePath.length() - suffix.length()).replace('/', '.');
                            targetFO.setAttribute("displayName", "bundlevalue:" + name + "#" + locBundleKey); // NOI18N
                        } else {
                            // XXX what?
                        }
                    }
                    if (attrs != null) {
                        for (Map.Entry<String,Object> entry : attrs.entrySet()) {
                            targetFO.setAttribute(entry.getKey(), entry.getValue());
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
            FileSystem layer = cmf.getLayerHandle().layer(false);
            if (layer != null && layer.findResource(layerPath) != null) {
                layerOp = new CreatedModifiedFiles.Operation() {
                    public void run() throws IOException {
                        throw new IOException("cannot overwrite " + layerPath); // NOI18N
                    }
                    public String[] getModifiedPaths() {
                        return new String[0];
                    }
                    public String[] getCreatedPaths() {
                        return new String[0];
                    }
                    public String[] getInvalidPaths() {
                        // #85138: make sure we do not overwrite an existing entry.
                        return new String[] {layerPath};
                    }
                };
            } else {
                layerOp = new LayerModifications(project, op, externalFiles, cmf);
            }
            addPaths(layerOp);
            if (localizedDisplayName != null) {
                this.createBundleKey = new BundleKey(getProject(), locBundleKey, localizedDisplayName);
                addPaths(this.createBundleKey);
            } else {
                createBundleKey = null;
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
            EditableProperties p = getEditableProperties();
            p.putAll(getProperties());
            Util.storeProperties(getPropertyFile(),p);
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

