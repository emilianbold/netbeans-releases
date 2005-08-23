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

import java.io.IOException;
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
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
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
 * <strong>before</strong> added operation are run so they can be e.g. shown by
 * wizard before any files are actually created.
 *
 * @author Martin Krauskopf
 */
public final class CreatedModifiedFiles {
    
    /**
     * Operation that may be added to a <code>CreatedModifiedFiles</code>
     * instance or can just be used alone. See {@link CreatedModifiedFiles} for
     * more information.
     */
    public interface Operation {
        
        /** Perform this operation. */
        void run() throws IOException;
        
        /**
         * Returns sorted array of path which are going to modified after this
         * {@link CreatedModifiedFiles} instance is run. Paths are relative to
         * the project's base directory. It is available immediately after an
         * operation instance is created.
         * XXX why is this sorted, and not a simple Set<String>?
         */
        String[] getModifiedPaths();
        
        /**
         * Returns sorted array of path which are going to created after this
         * {@link CreatedModifiedFiles} instance is run. Paths are relative to
         * the project's base directory. It is available immediately after an
         * operation instance is created.
         */
        String[] getCreatedPaths();
        
        /* XXX should perhaps also have:
        /**
         * True if the created or modified path is relevant to the user and should
         * be selected in the final wizard.
         * /
        boolean isRelevant(String path);
        /**
         * True if the created or modified path should be opened in the editor.
         * /
        boolean isForEditing(String path);
         */
        
    }
    
    private final SortedSet/*<String>*/ createdPaths = new TreeSet();
    private final SortedSet/*<String>*/ modifiedPaths = new TreeSet();
    
    /** {@link NbModuleProject} this instance manage. */
    private final NbModuleProject project;
    private final List/*<CreatedModifiedFiles.Operation>*/ operations = new ArrayList();
    
    // For use from CreatedModifiedFilesFactory.LayerModifications; XXX would be better to have an operation context or similar
    // (so that multiple operations could group pre- and post-actions)
    private LayerUtils.LayerHandle layerHandle;
    LayerUtils.LayerHandle getLayerHandle() {
        if (layerHandle == null) {
             layerHandle = LayerUtils.layerForProject(project);
        }
        return layerHandle;
    }
    
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
        operations.add(operation);
        // XXX should always show isForEditing files at the top of the list, acc. to Jano
        createdPaths.addAll(Arrays.asList(operation.getCreatedPaths()));
        modifiedPaths.addAll(Arrays.asList(operation.getModifiedPaths()));
    }
    
    /**
     * Performs in turn {@link Operation#run()} on all operations added to this
     * instance in order in which operations have been added.
     */
    public void run() throws IOException {
        boolean oldAutosave = false;
        if (layerHandle != null) {
            oldAutosave = layerHandle.isAutosave();
            layerHandle.setAutosave(false);
        }
        try {
            for (Iterator it = operations.iterator(); it.hasNext(); ) {
                Operation op = (Operation) it.next();
                op.run();
            }
            if (layerHandle != null) {
                // XXX clumsy, see above
                layerHandle.save();
            }
        } finally {
            if (layerHandle != null) {
                layerHandle.setAutosave(oldAutosave);
            }
        }
        // XXX should get EditCookie/OpenCookie for created/modified files for which isForEditing
        // XXX should return a Set<FileObject> of created/modified files for which isRelevant
    }
    
    public String[] getCreatedPaths() {
        if (createdPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[createdPaths.size()];
            return (String[]) createdPaths.toArray(s);
        }
    }
    
    public String[] getModifiedPaths() {
        if (modifiedPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[modifiedPaths.size()];
            return (String[]) modifiedPaths.toArray(s);
        }
    }
    
    /**
     * Returns {@link Operation} for creating custom file in the project file
     * hierarchy.
     * @param path relative to a project directory where a file to be created
     * @param content content for the file being created. Content may address
     *        either text or binary data.
     */
    public Operation createFile(String path, URL content) {
        return CreatedModifiedFilesFactory.createFile(project, path, content);
    }
    
    /**
     * Returns an {@link Operation} for creating custom file in the project
     * file hierarchy with an option to replace <em>token</em>s from a given
     * <code>content</code> with custom string. The result will be stored into
     * a file representing by a given <code>path</code>.
     *
     * @param path relative to a project directory where a file to be created
     * @param content content for the file being created
     * @param tokens map of <em>token to be replaced</em> - <em>by what</em>
     *        pairs which will be applied on the stored file. Both a key and a
     *        value have to be a valid regular expression. See {@link
     *        java.lang.String#replaceAll(String, String)} and follow links in
     *        its javadoc for more details. May be <code>null</code> (the same
     *        as an empty map).
     */
    public Operation createFileWithSubstitutions(String path,
            URL content, Map/*<String,String>*/ tokens) {
        return CreatedModifiedFilesFactory.createFileWithSubstitutions(project, path, content, tokens);
    }
    
    /**
     * Provides {@link Operation} that will add given <code>value</code> under
     * a specified <code>key</code> into the custom <em>bundle</em> which is
     * specified by the <code>bundlePath</code> parameter.
     */
    public Operation bundleKey(String bundlePath, String key, String value) {
        return CreatedModifiedFilesFactory.bundleKey(project, key, value, bundlePath);
    }
    
    /**
     * Provides {@link Operation} that will add given <code>value</code> under
     * a specified <code>key</code> into the project's default <em>localized
     * bundle</em> which is specified in the project's <em>manifest</em>.
     */
    public Operation bundleKeyDefaultBundle(String key, String value) {
        return CreatedModifiedFilesFactory.bundleKeyDefaultBundle(project, key, value);
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
     *        (<strong>without</strong> .class extension)
     * @param installBefore content of Install-Before attribute, or null if not
     *        specified
     */
    public Operation addLoaderSection(String dataLoaderClass, String installBefore) {
        return CreatedModifiedFilesFactory.addLoaderSection(project, dataLoaderClass, installBefore);
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
        return CreatedModifiedFilesFactory.addLookupRegistration(
                project, interfaceClass, implClass);
    }
    
    /**
     * Add a dependency to a list of module dependencies of this project. This
     * means editing of project's <em>nbproject/project.xml</em>. All
     * parameters refers to a module this module will depend on. If a project
     * already has a given dependency it will not be added.
     *
     * @param codeNameBase codename base
     * @param releaseVersion release version, if -1 will be taken from the
     *        entry found in platform
     * @param version specification version (see {@link SpecificationVersion}),
     *        if null will be taken from the entry found in platform
     * @param useInCompiler do this module needs a module beeing added at a
     *        compile time?
     */
    public Operation addModuleDependency(String codeNameBase, int
            releaseVersion, SpecificationVersion version, boolean useInCompiler) {
        return CreatedModifiedFilesFactory.addModuleDependency(project, codeNameBase,
                releaseVersion, version, useInCompiler);
    }
    
    /**
     * Creates an entry (<em>file</em> element) in the project's layer. Also
     * may create and/or modify other files as it is needed.
     *
     * @param layerPath path in a project's layer. Folders which don't exist
     *        yet will be created. (e.g.
     *        <em>Menu/Tools/org-example-module1-BeepAction.instance</em>).
     * @param content became content of a file, or null
     * @param substitutionTokens map of <em>token to be replaced</em> - <em>by
     *        what</em> pairs which will be applied on the stored
     *        <code>content</code> file. Both a key and a value have to be a
     *        valid regular expression. See {@link
     *        java.lang.String#replaceAll(String, String)} and follow links in
     *        its javadoc for more details. May be <code>null</code> (the same
     *        as an empty map).
     * @param localizedDisplayName if it is not a <code>null</code>
     *        <em>SystemFileSystem.localizingBundle</em> attribute will be
     *        created with the stringvalue to a default bundle (from manifest).
     *        Also an appropriate entry will be added into the bundle.
     * @param fileAttributes a map of <String,Object>s. key in the map is the
     *        name of the file attribute value is the actual value, currently
     *        supported types are Boolean and String Generates
     *        <pre>
     *          <attr name="KEY" stringvalue="VALUE"/> or <attr name="KEY" booleanvalue="VALUE"/>
     *        </pre>
     * @return see {@link Operation}
     */
    public Operation createLayerEntry(
            String layerPath,
            URL content,
            Map/*<String,String>*/ substitutionTokens,
            String localizedDisplayName,
            Map/*<String,Object>*/ fileAttributes) {
        return CreatedModifiedFilesFactory.createLayerEntry(this, project, layerPath,
                content, substitutionTokens,
                localizedDisplayName, fileAttributes);
    }
    
    /**
     * Creates a new arbitrary <em>&lt;attr&gt;</em> element.
     *
     * @param parentPath path to a <em>file</em> or a <em>folder</em> in a
     *        project's layer. It <strong>must</strong> exist.
     * @param attrName value of the name attribute of the <em>&lt;attr&gt;</em>
     *        element.
     * @param attrValue value of the attribute (may specially be a string prefixed with "newvalue:" or "methodvalue:")
     * @return see {@link Operation}
     */
    public CreatedModifiedFiles.Operation createLayerAttribute(final String parentPath,
            final String attrName, final Object attrValue) {
        return layerModifications(new LayerOperation() {
            public void run(FileSystem layer) throws IOException {
                FileObject f = layer.findResource(parentPath);
                if (f == null) {
                    // XXX sometimes this happens when it should not, during unit tests... why?
                    /*
                    try {
                        // For debugging:
                        getLayerHandle().save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                     */
                    throw new IOException(parentPath);
                }
                f.setAttribute(attrName, attrValue);
            }
        }, Collections.EMPTY_SET);
    }
    
    /**
     * Order two entries in a project layer. i.e. creates an ordering
     * <em>&lt;attr&gt;</em> element.
     *
     * @param layerPath folder path in a project's layer. Folders which don't
     *        exist yet will be created. (e.g. <em>Loaders/text/x-java/Actions</em>).
     * @param precedingItemName item to be before <em>followingItemName</em>
     * @param followingItemName item to be after <em>precedingItemName</em>
     */
    public Operation orderLayerEntry(final String layerPath, final String precedingItemName,
            final String followingItemName) {
        return layerModifications(new LayerOperation() {
            public void run(FileSystem layer) throws IOException {
                FileObject f = FileUtil.createFolder(layer.getRoot(), layerPath);
                f.setAttribute(precedingItemName + '/' + followingItemName, Boolean.TRUE);
            }
        }, Collections.EMPTY_SET);
    }
    
    /**
     * Make structural modifications to the project's XML layer.
     * The operations may be expressed as filesystem calls.
     * @param op a callback for the actual changes to make
     * @param externalFiles a list of <em>simple filenames</em> of new data files which
     *                      are to be created in the layer and which will therefore appear
     *                      on disk alongside the layer, usually with the same names (unless
     *                      they conflict with existing files); you still need to create them
     *                      yourself using e.g. {@link FileObject#createData} and {@link FileObject#getOutputStream}
     * @return the operation handle
     */
    public Operation layerModifications(final LayerOperation op, final Set/*<String>*/ externalFiles) {
        return CreatedModifiedFilesFactory.layerModifications(project, op, externalFiles, this);
    }
    
    /**
     * Callback for modifying the project's XML layer.
     * @see #layerModifications
     */
    public interface LayerOperation {
        
        /**
         * Actually change the layer.
         * @param layer the layer to make changes to using Filesystems API calls
         * @throws IOException if the changes fail somehow
         */
        void run(FileSystem layer) throws IOException;
        
    }
    
}
