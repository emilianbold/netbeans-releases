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
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * Provide general infrastructure for performing miscellaneous operations upon
 * {@link NbModuleProject}'s files, such as <em>manifest.mf</em>,
 * <em>bundle.properties</em>, <em>layer.xml</em>, <em>project.xml</em> easily.
 * See javadoc to individual methods below. After creating a
 * <code>CreatedModifiedFiles</code> instance client may create {@link
 * CreatedModifiedFiles.Operation} which then may be added to the
 * <code>CreatedModifiedFiles</code> instance or just used itself. Both
 * <code>CreatedModifiedFiles</code> and <code>Operation</code> provide methods
 * to get sets of relative (to a project's base directory) paths which are
 * going to be created and/or modified. These sets may be obtained
 * <b>before</b> added operation are run so they can be e.g. shown by wizard
 * before any files are actually created.
 *
 * @author Martin Krauskopf
 */
public class CreatedModifiedFiles {
    
    private SortedSet/*<String>*/ createdFiles;
    private SortedSet/*<String>*/ modifiedFiles;
    
    /** {@link NbModuleProject} this instance manage. */
    private NbModuleProject project;
    private List/*<CreatedModifiedFiles.Operation>*/ operations;
    
    /**
     * Create instance for managing given {@link NbModuleProject}'s files.
     * @param project project this instance will operate upon
     */
    public CreatedModifiedFiles(NbModuleProject project) {
        this.project = project;
    }
    
    /**
     * Adds given {@link Operation} to a list of operations that will be run
     * after calling {@link #run()}. Operations are run in the order in which
     * they have been added. Also files which would be created by a given
     * operation are added to lists of paths returned by {@link
     * #getModifiedPaths()} or {@link #getCreatedPaths()} immediately. @param
     * operation operation to be added
     */
    public void add(Operation operation) {
        if (operations == null) {
            // first operation
            operations = new ArrayList();
            createdFiles = new TreeSet();
            modifiedFiles = new TreeSet();
        }
        operations.add(operation);
        createdFiles.addAll(Arrays.asList(operation.getCreatedPaths()));
        modifiedFiles.addAll(Arrays.asList(operation.getModifiedPaths()));
    }
    
    /**
     * Performs in turn {@link Operation#run()} on all operations added to this
     * instance in order in which operations have been added.
     */
    public void run() throws IOException {
        if (operations != null) {
            for (Iterator it = operations.iterator(); it.hasNext(); ) {
                Operation op = (Operation) it.next();
                op.run();
            }
        }
    }
    
//    private void addFiles(SortedSet/*<File>*/ fileSet, String[] relPaths) {
//        for (int i = 0; i < relPaths.length; i++) {
//            fileSet.add(FileUtil.toFile(
//                    project.getProjectDirectory().getFileObject(relPaths[i])));
//        }
//    }
    
    /**
     * Returns a sorted set of path which are going to created after this
     * {@link CreatedModifiedFiles} instance is run. Paths are relative to the
     * project's base directory. It contains all paths that of all added (not
     * necessarily run) operations.
     */
    public SortedSet/*<String>*/ getCreatedPaths() {
        return Collections.unmodifiableSortedSet(
                createdFiles == null ? new TreeSet() : createdFiles);
    }
    
    /**
     * Returns a sorted set of path which are going to modified after this
     * {@link CreatedModifiedFiles} instance is run. Paths are relative to the
     * project's base directory. It contains all paths that of all added (not
     * necessarily run) operations.
     */
    public SortedSet/*<String>*/ getModifiedPaths() {
        return Collections.unmodifiableSortedSet(
                modifiedFiles == null ? new TreeSet() : modifiedFiles);
    }
    
    /**
     * Operation that may be added to a <code>CreatedModifiedFiles</code>
     * instance or can just be used alone. See {@link CreatedModifiedFiles} for
     * more information.
     */
    public interface Operation {
        
        /**
         * Returns sorted array of path which are going to modified after this
         * {@link CreatedModifiedFiles} instance is run. Paths are relative to
         * the project's base directory. It is available immediately after an
         * operation instance is created.
         */
        String[] getModifiedPaths();
        
        /**
         * Returns sorted array of path which are going to created after this
         * {@link CreatedModifiedFiles} instance is run. Paths are relative to
         * the project's base directory. It is available immediately after an
         * operation instance is created.
         */
        String[] getCreatedPaths();
        
        /** Perform this operation. */
        void run() throws IOException;
    }
    
    public Operation createFile(String path, URL content) {
        return null;
    }
    
    public Operation createFileWithSubstitutions(String path,
            URL content, Map/*<String,String>*/ tokens) {
        return null;
    }
    
    /**
     * Provides {@link Operation} that will add given <code>value</code> under
     * a specified <code>key</code> into the custom <em>bundle</em> which is
     * specified by the <code>bundlePath</code> parameter.
     */
    public Operation bundleKey(String bundlePath, String key, String value) {
        return new BundleKey(bundlePath, key, value);
    }
    
    /**
     * Provides {@link Operation} that will add given <code>value</code> under
     * a specified <code>key</code> into the project's default <em>localized
     * bundle</em> which is specified in the project's <em>manifest</em>.
     */
    public Operation bundleKeyDefaultBundle(String key, String value) {
        ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
        String srcDir = project.getProjectDirectoryPath();
        return new BundleKey(srcDir + "/" + mm.getLocalizingBundle(), // NOI18N
                key, value);
    }
    
    /**
     * Provides {@link Operation} that will create a new section in the
     * project's <em>manifest</em> registering a given
     * <code>dataLoaderClass</code>.
     *
     * <pre>
     *   Name: org/netbeans/modules/myprops/MyPropsLoader.class
     *   OpenIDE-Module-Class: Loader
     * </pre>
     *
     * @param dataLoaderClass e.g. org/netbeans/modules/myprops/MyPropsLoader
     *                        (<b>without</b> .class extension)
     */
    public Operation addLoaderSection(String dataLoaderClass) {
        return new AddLoaderSection(dataLoaderClass);
    }
    
    /**
     * Provides {@link Operation} that will register an <code>implClass</code>
     * implementation of <code>interfaceClass</code> interface in the lookup.
     * If a file representing <code>interfaceClass</code> service already
     * exists in <em>src/META-INF/services</em> directory
     * <code>implClass</code> will be appended to the end of the list of
     * implementations. If it doesn't exist a new file will be created.
     *
     * @param interfaceClass e.g. org.example.spi.somemodule.ProvideMe
     * @param implClass e.g. org.example.module1.ProvideMeImpl
     */
    public Operation addLookupRegistration(String interfaceClass, String implClass) {
        return new AddLookupRegistration(interfaceClass, implClass);
    }
    
    public Operation addModuleDependency(String codeNameBase, int
            releaseVersion, SpecificationVersion version, boolean useInCompiler) {
        return null;
    }
    
    /**
     * <em>Converts</em> a given {@link LayerCallback} into an {@link
     * Operation} so it may be run within a {@link CreatedModifiedFiles}
     * instance. Also methods for obtaining created and modified paths may be
     * used on the returned instance.
     *
     * @param callback <code>LayerCallback</code> instance you want to wrap
     * @return operation wrapping a given callback
     */
    public Operation layerOperation(LayerCallback callback) {
        return new LayerOperation(callback);
    }
    
    /**
     * Performs a project's layer related operation. It may also modify and/or
     * create other files as well. See also {@link
     * CreatedModifiedFiles#layerOperation(LayerCallback)}.
     */
    public interface LayerCallback {
        /** Perform this instance. */
        void run() throws IOException;
    }
    
    /**
     * Creates an entry (<em>file</em> element) in the project's layer. Also
     * may create and/or modify other files as it is needed.
     *
     * @param layerPath path in a project's layer. Folders which don't exist
     *        yet will be created. (e.g.
     *        <em>Menu/Tools/org-example-module1-BeepAction.instance</em>).
     * @param contentResourcePath represents an <em>url</em> attribute of entry
     *        being created
     * @param content <b>not used yet</b>
     * @param localizedDisplayName <b>not used yet</b>
     * @param substitutionTokens <b>not used yet</b>
     * @return see {@link LayerCallback}
     */
    public LayerCallback createLayerEntry(String layerPath, String
            contentResourcePath, URL content, String localizedDisplayName,
            Map/*<String,String>*/ substitutionTokens) {
        return new LayerEntry(layerPath, contentResourcePath);
    }
    
    public LayerCallback orderLayerEntry(String layerPath, String
            precedingItemName, String followingItemName) {
        return null;
    }
    
    
    private abstract class OperationBase implements Operation {
        
        private SortedSet/*<String>*/ createdPaths;
        private SortedSet/*<String>*/ modifiedPaths;
        
        public String[] getModifiedPaths() {
            String[] s = new String[getModifiedPathsSet().size()];
            return (String[]) getModifiedPathsSet().toArray(s);
        }
        
        public String[] getCreatedPaths() {
            String[] s = new String[getCreatedPathsSet().size()];
            return (String[]) getCreatedPathsSet().toArray(s);
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
        
    }
    
    
    private final class BundleKey extends OperationBase {
        
        protected String bundlePath;
        protected String key;
        protected String value;
        
        public BundleKey(String bundlePath, String key, String value) {
            this.bundlePath = bundlePath;
            this.key = key;
            this.value = value;
            getModifiedPathsSet().add(bundlePath);
        }
        
        public void run() throws IOException {
            FileObject prjDir = project.getProjectDirectory();
            FileObject bundleFO = prjDir.getFileObject(bundlePath);
            if (bundleFO == null) {
                bundleFO = FileUtil.createData(prjDir, bundlePath);
            }
            EditableProperties ep = Util.loadProperties(bundleFO);
            ep.setProperty(key, value);
            Util.storeProperties(bundleFO, ep);
        }
        
    }
    
    private final class AddLoaderSection extends OperationBase {
        
        private FileObject mfFO;
        
        private String dataLoaderClass;
        
        public AddLoaderSection(String dataLoaderClass) {
            this.dataLoaderClass = dataLoaderClass + ".class"; // NOI18N
            this.mfFO = project.getManifestFile();
            addModifiedFileObject(mfFO);
        }
        
        public void run() throws IOException {
            EditableManifest em = Util.loadManifest(mfFO);
            em.addSection(dataLoaderClass);
            em.setAttribute("OpenIDE-Module-Class", "Loader", dataLoaderClass); // NOI18N
            Util.storeManifest(mfFO, em);
        }
        
    }
    
    private final class AddLookupRegistration extends OperationBase {
        
        private String interfaceClassPath;
        private String interfaceClass;
        private String implClass;
        
        public AddLookupRegistration(String interfaceClass, String implClass) {
            this.interfaceClass = interfaceClass;
            this.implClass = implClass;
            this.interfaceClassPath = project.getProjectDirectoryPath() +
                    "/META-INF/services/" + interfaceClass; // NOI18N
            if (project.getProjectDirectory().getFileObject(interfaceClassPath) == null) {
                getCreatedPathsSet().add(interfaceClassPath);
            } else {
                getModifiedPathsSet().add(interfaceClassPath);
            }
        }
        
        public void run() throws IOException {
            FileObject service = FileUtil.createData(
                    project.getProjectDirectory(),interfaceClassPath);
            
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
                    pw.println(interfaceClass);
                    pw.println();
                } finally {
                    pw.close();
                }
            } finally {
                lock.releaseLock();
            }
            
        }
    }
    
    private final class LayerEntry implements LayerCallback {
        
        private String layerPath;
        private String contentResourcePath;
        
        public LayerEntry(String layerPath, String contentResourcePath) {
            this.layerPath = layerPath;
            this.contentResourcePath = contentResourcePath;
        }
        
        public void run() throws IOException {
            ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
            String srcDir = project.getProjectDirectoryPath();
            String layerFile = srcDir + "/" + mm.getLayer(); // NOI18N
            LayerUtil.createFile(project.getProjectDirectory(), layerFile,
                    layerPath, contentResourcePath);
        }
    }
    
    private final class LayerOperation extends OperationBase {
        
        private LayerCallback callback;
        
        public LayerOperation(LayerCallback callback) {
            ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
            String srcDir = project.getProjectDirectoryPath();
            getModifiedPathsSet().add(srcDir + "/" + mm.getLayer());
            this.callback = callback;
        }
        
        public void run() throws IOException {
            callback.run();
        }
    }
    
    /**
     * Doesn't check given arguments. Be sure they are valid as supposed by
     * {@link PropertyUtils#relativizeFile(File, File)} method.
     */
    private String getProjectPath(FileObject file) {
        return PropertyUtils.relativizeFile(
                FileUtil.toFile(project.getProjectDirectory()),
                FileUtil.normalizeFile(FileUtil.toFile(file)));
    }
    
}

