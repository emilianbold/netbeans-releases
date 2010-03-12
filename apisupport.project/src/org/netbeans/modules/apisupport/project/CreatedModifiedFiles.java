/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
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
        
        /**
         * returns paths that are already existing but the operaton expects to create it.
         * Is an error condition and should be shown in UI.
         *
         */
        String[] getInvalidPaths();
        
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
    
    private final SortedSet<String> createdPaths = new TreeSet<String>();
    private final SortedSet<String> modifiedPaths = new TreeSet<String>();
    private final SortedSet<String> invalidPaths = new TreeSet<String>();
    
    /** {@link Project} this instance manage. */
    private final Project project;
    private final List<CreatedModifiedFiles.Operation> operations = new ArrayList<CreatedModifiedFiles.Operation>();
    
    // For use from CreatedModifiedFilesFactory.LayerModifications; XXX would be better to have an operation context or similar
    // (so that multiple operations could group pre- and post-actions)
    private LayerHandle layerHandle;
    LayerHandle getLayerHandle() {
        if (layerHandle == null) {
            layerHandle = LayerHandle.forProject(project);
        }
        return layerHandle;
    }
    
    /**
     * Create instance for managing given {@link NbModuleProject}'s files.
     * @param project project this instance will operate upon
     */
    public CreatedModifiedFiles(Project project) {
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
        invalidPaths.addAll(Arrays.asList(operation.getInvalidPaths()));
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
            for (Operation op : operations) {
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
            return createdPaths.toArray(s);
        }
    }
    
    public String[] getModifiedPaths() {
        if (modifiedPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[modifiedPaths.size()];
            return modifiedPaths.toArray(s);
        }
    }
    
    public String[] getInvalidPaths() {
        if (invalidPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[invalidPaths.size()];
            return invalidPaths.toArray(s);
        }
    }

    /**
     * Convenience method to load a file template from the standard location.
     * @param name a simple filename
     * @return that file from the <code>Templates/NetBeansModuleDevelopment-files</code> layer folder
     */
    public static FileObject getTemplate(String name) {
        FileObject f = FileUtil.getConfigFile("Templates/NetBeansModuleDevelopment-files/" + name);
        assert f != null : name;
        return f;
    }

    /**
     * Returns {@link Operation} for creating custom file in the project file
     * hierarchy.
     * @param path relative to a project directory where a file to be created
     * @param content content for the file being created. Content may address
     *        either text or binary data.
     */
    public Operation createFile(String path, FileObject content) {
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
     * @param tokens properties with values to be passed to FreeMarker
     *               (in addition to: name, nameAndExt, user, date, time, and project.license)
     */
    public Operation createFileWithSubstitutions(String path,
            FileObject content, Map<String,String> tokens) {
        if (tokens == null) {
            throw new NullPointerException();
        }
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
     * exists in <em>META-INF/services</em> directory
     * <code>implClass</code> will be appended to the end of the list of
     * implementations. If it doesn't exist a new file will be created.
     * <p><strong>Note:</strong> this style of registration should not be used
     * for any new APIs. Use {@link ServiceProvider} instead.
     *
     * @param interfaceClass e.g. org.example.spi.somemodule.ProvideMe
     * @param implClass e.g. org.example.module1.ProvideMeImpl
     * @param inTests if true, add to test/unit/src/META-INF/services/, else to src/META-INF/services/
     */
    public Operation addLookupRegistration(String interfaceClass, String implClass, boolean inTests) {
        return CreatedModifiedFilesFactory.addLookupRegistration(
                project, interfaceClass, implClass, inTests);
    }
    
    /**
     * Add a dependency to a list of module dependencies of this project. This
     * means editing of project's <em>nbproject/project.xml</em>. All
     * parameters refers to a module this module will depend on. If a project
     * already has a given dependency it will not be added.
     *
     * @param codeNameBase codename base
     * @param releaseVersion release version, if <code>null</code> will be taken from the
     *        entry found in platform
     * @param version specification version (see {@link SpecificationVersion}),
     *        if null will be taken from the entry found in platform
     * @param useInCompiler do this module needs a module beeing added at a
     *        compile time?
     */
    public Operation addModuleDependency(String codeNameBase, String
            releaseVersion, SpecificationVersion version, boolean useInCompiler) {
        return CreatedModifiedFilesFactory.addModuleDependency(project, codeNameBase,
                releaseVersion, version, useInCompiler);
    }
    
    /**
     * Delegates to {@link #addModuleDependency(String, String,
     * SpecificationVersion, boolean)} passing a given code name base,
     * <code>null</code> as release version, <code>null</code> as version and
     * <code>true</code> as useInCompiler arguments.
     */
    public CreatedModifiedFiles.Operation addModuleDependency(String codeNameBase) {
        return addModuleDependency(codeNameBase, null, null, true);
    }
    
    /**
     * Creates an entry (<em>file</em> element) in the project's layer. Also
     * may create and/or modify other files as it is needed.
     *
     * @param layerPath path in a project's layer. Folders which don't exist
     *        yet will be created. (e.g.
     *        <em>Menu/Tools/org-example-module1-BeepAction.instance</em>).
     * @param content became content of a file, or null
     * @param substitutionTokens see {@link #createFileWithSubstitutions} for details;
     *                           may be <code>null</code> to not use FreeMarker
     * @param localizedDisplayName if it is not a <code>null</code>
     *        <em>displayName</em> attribute will be
     *        created with the bundlevalue to a new entry in default bundle (from manifest).
     *        The entry will also be added into the bundle.
     * @param fileAttributes &lt;String,Object&gt; map. key in the map is the
     *        name of the file attribute value is the actual value, currently
     *        supported types are Boolean and String Generates
     *        <pre>
     *          &lt;attr name="KEY" stringvalue="VALUE"/&gt; or &lt;attr name="KEY" booleanvalue="VALUE"/&gt;
     *        </pre>
     * @return see {@link Operation}
     */
    public Operation createLayerEntry(
            String layerPath,
            FileObject content,
            Map<String,String> substitutionTokens,
            String localizedDisplayName,
            Map<String,Object> fileAttributes) {
        return CreatedModifiedFilesFactory.createLayerEntry(this, project, layerPath,
                content, substitutionTokens,
                localizedDisplayName, fileAttributes);
    }
    
    /**
     * Adds new attributes into manifest file.
     * @param section the name of the section or <code>null</code> for the main section.
     * @param attributes attribute names and values
     * @return see {@link Operation}
     */
    public Operation manifestModification(String section, Map<String,String> attributes) {
        return CreatedModifiedFilesFactory.manifestModification(project, section, attributes);
    }

    /**
     * Adds a token to a list given by a manifest header (creating it if it does not already exist).
     * @param header a header such as {@link ManifestManager#OPENIDE_MODULE_REQUIRES} (in the main section)
     * @param token a token to add
     * @return see {@link Operation}
     */
    public Operation addManifestToken(final String header, final String token) {
        return new CreatedModifiedFilesFactory.ModifyManifest(project) {
            {
                setAttribute(header, token, null);
            }
            protected @Override void performModification(EditableManifest em, String name, String value, String section) throws IllegalArgumentException {
                String originalValue = em.getAttribute(name, section);
                if (originalValue != null) {
                    em.setAttribute(name, originalValue + ", " + value, section);
                } else {
                    super.performModification(em, name, value, section);
                }
            }
        };
    }
    
    /**
     * Adds new properties into property file.
     * @param propertyPath path representing properties file relative to a project directory where all
     * properties will be put in. If such a file does not exist it is created.
     * @param properties &lt;String,String&gt; map mapping properties names and values.
     * @return see {@link Operation}
     */
    public Operation propertiesModification(String propertyPath,
            Map<String,String> properties) {
        return CreatedModifiedFilesFactory.propertiesModification(project, propertyPath, properties);
    }
    
    /**
     * Creates a new arbitrary <em>&lt;attr&gt;</em> element.
     *
     * @param parentPath path to a <em>file</em> or a <em>folder</em> in a
     *        project's layer. It <strong>must</strong> exist.
     * @param attrName value of the name attribute of the <em>&lt;attr&gt;</em>
     *        element.
     * @param attrValue value of the attribute (may specially be a string prefixed with "newvalue:", "bundlevalue:" or "methodvalue:")
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
        }, Collections.<String>emptySet());
    }
    
    /**
     * Order a new entry in a project layer between two others.
     *
     * @param layerPath folder path in a project's layer. (e.g. <em>Loaders/text/x-java/Actions</em>).
     * @param precedingItemName item to be before <em>newItemName</em> (may be null)
     * @param newItemName the new item (must already exist!)
     * @param followingItemName item to be after <em>newItemName</em> (may be null)
     */
    public Operation orderLayerEntry(final String layerPath, final String precedingItemName, final String newItemName,
            final String followingItemName) {
        return layerModifications(new LayerOperation() {
            public void run(FileSystem layer) throws IOException {
                FileObject f = layer.findResource(layerPath);
                if (f == null) {
                    throw new IOException("No such folder " + layerPath);
                }
                FileObject merged = LayerUtils.getEffectiveSystemFilesystem(project).findResource(layerPath);
                assert merged != null : layerPath;
                Integer beforePos = getPosition(merged, precedingItemName);
                Integer afterPos = getPosition(merged, followingItemName);
                if (beforePos != null && afterPos != null) {
                    // won't work well if afterPos == beforePos + 1, but oh well
                    f.getFileObject(newItemName).setAttribute("position", (beforePos + afterPos) / 2); // NOI18N
                } else if (beforePos != null) {
                    f.getFileObject(newItemName).setAttribute("position", beforePos + 100); // NOI18N
                } else if (afterPos != null) {
                    f.getFileObject(newItemName).setAttribute("position", afterPos - 100); // NOI18N
                } else {
                    // Fallback esp. for old platforms.
                    if (precedingItemName != null) {
                        f.setAttribute(precedingItemName + '/' + newItemName, true);
                    }
                    if (followingItemName != null) {
                        f.setAttribute(newItemName + '/' + followingItemName, true);
                    }
                }
            }
            private Integer getPosition(FileObject folder, String name) {
                if (name == null) {
                    return null;
                }
                FileObject f = folder.getFileObject(name);
                if (f == null) {
                    return null;
                }
                Object pos = f.getAttribute("position"); // NOI18N
                // ignore floats for now...
                return pos instanceof Integer ? (Integer) pos : null;
            }
        }, Collections.<String>emptySet());
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
    public Operation layerModifications(final LayerOperation op, final Set<String> externalFiles) {
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
    
    /**
     * Provides {@link Operation} that will create a {@code package-info.java} if needed
     * and optionally add some annotations to the package.
     * Each annotation is of the form FQN -> {key -> val}.
     */
    public Operation packageInfo(String packageName, Map<String,Map<String,Object>> annotations) {
        return CreatedModifiedFilesFactory.packageInfo(project, packageName, annotations);
    }

}
