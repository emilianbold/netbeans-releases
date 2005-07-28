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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
    public static interface Operation {
        
        /** Perform this operation. */
        void run() throws IOException;
        
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
        
    }
    
    private SortedSet/*<String>*/ createdPaths;
    private SortedSet/*<String>*/ modifiedPaths;
    
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
            createdPaths = new TreeSet();
            modifiedPaths = new TreeSet();
        }
        operations.add(operation);
        createdPaths.addAll(Arrays.asList(operation.getCreatedPaths()));
        modifiedPaths.addAll(Arrays.asList(operation.getModifiedPaths()));
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
     * @param path where a file to be created
     * @param content content for the file being created. Content may address either
     * text or binary data.
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
     * @param path where a file to be created
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
     */
    public Operation addLoaderSection(String dataLoaderClass) {
        return CreatedModifiedFilesFactory.addLoaderSection(project, dataLoaderClass);
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
     * parameters refers to a module this module will depend on.
     *
     * @param codeNameBase codename base
     * @param releaseVersion release version
     * @param version specification version (see {@link SpecificationVersion})
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
     * @param contentResourcePath represents an <em>url</em> attribute of entry
     *        being created.
     * @param content became content of a file represented by the
     *        contentResourcePath
     * @param localizedDisplayName if it is not a <code>null</code>
     *        <em>SystemFileSystem.localizingBundle</em> attribute will be
     *        created with the stringvalue to a default bundle (from manifest).
     *        Also an appropriate entry will be added into the bundle.
     * @param substitutionTokens map of <em>token to be replaced</em> - <em>by
     *        what</em> pairs which will be applied on the stored
     *        <code>content</code> file. Both a key and a value have to be a
     *        valid regular expression. See {@link
     *        java.lang.String#replaceAll(String, String)} and follow links in
     *        its javadoc for more details. May be <code>null</code> (the same
     *        as an empty map).
     * @return see {@link Operation}
     */
    public Operation createLayerEntry(String layerPath, String
            contentResourcePath, URL content, String localizedDisplayName,
            Map/*<String,String>*/ substitutionTokens) {
        return CreatedModifiedFilesFactory.createLayerEntry(project, layerPath,
                contentResourcePath, content, localizedDisplayName, substitutionTokens);
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
    public Operation orderLayerEntry(String layerPath, String precedingItemName,
            String followingItemName) {
        return CreatedModifiedFilesFactory.orderLayerEntry(project, layerPath,
                precedingItemName, followingItemName);
    }
    
}

