/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.Util;
import org.netbeans.spi.project.ExtensibleMetadataProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// XXX need a method to update non-key data in references e.g. during projectOpened()

/**
 * Helps manage inter-project references.
 * Normally you would create an instance of this object and keep it in your
 * project object in order to support {@link SubprojectProvider} and various
 * operations that change settings which might refer to build artifacts from
 * other projects: e.g. when changing the classpath for a Java-based project
 * you would want to use this helper to scan potential classpath entries for
 * JARs coming from other projects that you would like to be able to build
 * as dependencies before your project is built.
 * <p>
 * You probably only need the higher-level methods such as {@link #addReference}
 * and {@link #removeReference}; the lower-level methods such as {@link #addRawReference}
 * are provided for completeness, but typical client code should not need them.
 * <p>
 * Only deals with references needed to support build artifacts coming from
 * foreign projects. If for some reason you wish to store other kinds of
 * references to foreign projects, you do not need this class; just store
 * them however you wish, and be sure to create an appropriate {@link SubprojectProvider}.
 * <p>
 * Modification methods (add, remove) mark the project as modified but do not save it.
 * @author Jesse Glick
 */
public final class ReferenceHelper {
    
    /**
     * XML element name used to store references in <code>project.xml</code>.
     */
    static final String REFS_NAME = "references"; // NOI18N
    
    /**
     * XML element name used to store one reference in <code>project.xml</code>.
     */
    static final String REF_NAME = "reference"; // NOI18N
    
    /**
     * XML namespace used to store references in <code>project.xml</code>.
     */
    static final String REFS_NS = "http://www.netbeans.org/ns/ant-project-references/1"; // NOI18N
    
    private final AntProjectHelper h;
    private final ExtensibleMetadataProvider emp;

    /**
     * Create a new reference helper.
     * It needs an {@link AntProjectHelper} object in order to update references
     * in <code>project.xml</code>,
     * as well as set project or private properties referring to the locations
     * of foreign projects on disk.
     * @param helper an Ant project helper object representing this project's configuration
     * @param emp an extensible metadata provider needed to store references
     */
    public ReferenceHelper(AntProjectHelper helper, ExtensibleMetadataProvider emp) {
        h = helper;
        this.emp = emp;
    }

    /**
     * Load <references> from project.xml.
     * @param create if true, create an empty element if it was missing, else leave as null
     */
    private Element loadReferences(boolean create) {
        //assert ProjectManager.mutex().canRead();
        Element references = emp.getConfigurationFragment(REFS_NAME, REFS_NS, true);
        if (references == null && create) {
            references = XMLUtil.createDocument("ignore", null, null, null).createElementNS(REFS_NS, REFS_NAME); // NOI18N
        }
        return references;
    }

    /**
     * Store <references> to project.xml (i.e. to memory and mark project modified).
     */
    private void storeReferences(Element references) {
        //assert ProjectManager.mutex().canWrite();
        assert references != null && references.getLocalName().equals(REFS_NAME) && REFS_NS.equals(references.getNamespaceURI());
        emp.putConfigurationFragment(references, true);
    }
    
    /**
     * Add a reference to an artifact coming from a foreign project.
     * <p>
     * Records the name of the foreign project.
     * Normally the foreign project name is that project's code name,
     * but it may be uniquified if that name is already taken to refer
     * to a different project with the same code name.
     * <p>
     * Adds a project property if necessary to refer to its location of the foreign
     * project - a shared property if the foreign project
     * is {@link CollocationQuery collocated} with this one, else a private property.
     * This property is named <samp>project.<i>foreignProjectName</i></samp>.
     * Example: <samp>project.mylib=../mylib</samp>
     * <p>
     * Adds a project property to refer to the location of the artifact itself.
     * This property is named <samp>reference.<i>foreignProjectName</i>.<i>targetName</i></samp>
     * and will use <samp>${project.<i>foreignProjectName</i>}</samp> and be a shared
     * property - unless the artifact location is an absolute URI, in which case the property
     * will also be private.
     * Example: <samp>reference.mylib.jar=${project.mylib}/dist/mylib.jar</samp>
     * <p>
     * Also records the artifact type, (relative) script path, and build and
     * clean target names.
     * <p>
     * If the reference already exists (keyed by foreign project object
     * and target name), nothing is done, unless some other field (script location,
     * clean target name, or artifact type) needed to be updated, in which case
     * the new information replaces the old. Similarly, the artifact location
     * property is updated if necessary.
     * <p>
     * Acquires write access.
     * @param artifact the artifact to add
     * @return true if a reference or some property was actually added or modified,
     *         false if everything already existed and was not modified
     * @throws IllegalArgumentException if the artifact is not associated with a project
     */
    public boolean addReference(final AntArtifact artifact) throws IllegalArgumentException {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Project forProj = artifact.getProject();
                if (forProj == null) {
                    throw new IllegalArgumentException("No project associated with " + artifact); // NOI18N
                }
                // Set up the raw reference.
                String forProjName = forProj.getName();
                // XXX need to uniquify it! If there is already a reference using that name,
                // but it refers to a different project, choose a new name.
                File forProjDir = FileUtil.toFile(forProj.getProjectDirectory());
                assert forProjDir != null : forProj.getProjectDirectory();
                File scriptFile = artifact.getScriptLocation();
                URI scriptLocation = forProjDir.toURI().relativize(scriptFile.toURI());
                RawReference ref = new RawReference(forProjName, artifact.getType(), scriptLocation, artifact.getTargetName(), artifact.getCleanTargetName());
                Element references = loadReferences(true);
                boolean success;
                try {
                    success = addRawReference(ref, references);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return Boolean.FALSE;
                }
                if (success) {
                    storeReferences(references);
                }
                // Set up ${project.whatever}.
                File myProjDir = FileUtil.toFile(AntBasedProjectFactorySingleton.getProjectFor(h).getProjectDirectory());
                String forProjPath;
                String propertiesFile;
                if (CollocationQuery.areCollocated(myProjDir, forProjDir)) {
                    // Fine, using a relative path to subproject.
                    forProjPath = PropertyUtils.relativizeFile(myProjDir, forProjDir);
                    assert forProjPath != null : "These dirs are not really collocated: " + myProjDir + " & " + forProjDir;
                    propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                } else {
                    // Use an absolute path.
                    forProjPath = forProjDir.getAbsolutePath();
                    propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
                }
                EditableProperties props = h.getProperties(propertiesFile);
                String forProjPathProp = "project." + forProjName; // NOI18N
                if (!forProjPath.equals(props.getProperty(forProjPathProp))) {
                    props.setProperty(forProjPathProp, forProjPath);
                    h.putProperties(propertiesFile, props);
                    success = true;
                }
                // Set up ${reference.whatever.whatever}.
                URI artFile = artifact.getArtifactLocation();
                String refPath;
                if (artFile.isAbsolute()) {
                    refPath = new File(artFile).getAbsolutePath();
                    propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
                } else {
                    refPath = "${" + forProjPathProp + "}/" + artFile; // NOI18N
                    propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                }
                props = h.getProperties(propertiesFile);
                String refPathProp = "reference." + forProjName + '.' + artifact.getTargetName(); // NOI18N
                if (!refPath.equals(props.getProperty(refPathProp))) {
                    props.setProperty(refPathProp, refPath);
                    h.putProperties(propertiesFile, props);
                    success = true;
                }
                return Boolean.valueOf(success);
            }
        })).booleanValue();
    }

    /**
     * Add a raw reference to a foreign project artifact.
     * Does not check if such a project already exists; does not create a project
     * property to refer to it; does not do any backreference usage notifications.
     * <p>
     * If the reference already exists (keyed by foreign project name and target name),
     * nothing is done, unless some other field (script location, clean target name,
     * or artifact type) needed to be updated, in which case the new information
     * replaces the old.
     * <p>
     * Note that since {@link RawReference} is just a descriptor, it is not guaranteed
     * that after adding one {@link #getRawReferences} or {@link #getRawReference}
     * would return the identical object.
     * <p>
     * Acquires write access.
     * @param ref a raw reference descriptor
     * @return true if a reference was actually added or modified,
     *         false if it already existed and was not modified
     */
    public boolean addRawReference(final RawReference ref) {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(true);
                boolean success;
                try {
                    success = addRawReference(ref, references);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return Boolean.FALSE;
                }
                if (success) {
                    storeReferences(references);
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        })).booleanValue();
    }
    
    private static boolean addRawReference(RawReference ref, Element references) throws IllegalArgumentException {
        // Linear search; always keeping references sorted first by foreign project
        // name, then by target name.
        Element nextRefEl = null;
        List/*<Element>*/subEls = Util.findSubElements(references);
        Iterator it = subEls.iterator();
        while (it.hasNext()) {
            Element testRefEl = (Element)it.next();
            RawReference testRef = RawReference.create(testRefEl);
            if (testRef.getForeignProjectName().compareTo(ref.getForeignProjectName()) > 0) {
                // gone too far, go back
                nextRefEl = testRefEl;
                break;
            }
            if (testRef.getForeignProjectName().equals(ref.getForeignProjectName())) {
                if (testRef.getTargetName().compareTo(ref.getTargetName()) > 0) {
                    // again, gone too far, go back
                    nextRefEl = testRefEl;
                    break;
                }
                if (testRef.getTargetName().equals(ref.getTargetName())) {
                    // Key match, check if it needs to be updated.
                    if (testRef.getArtifactType().equals(ref.getArtifactType()) &&
                            testRef.getScriptLocation().equals(ref.getScriptLocation()) &&
                            testRef.getCleanTargetName().equals(ref.getCleanTargetName())) {
                        // Match on other fields. Return without changing anything.
                        return false;
                    }
                    // Something needs updating.
                    // Delete the old ref and set nextRef to the next item in line.
                    references.removeChild(testRefEl);
                    if (it.hasNext()) {
                        nextRefEl = (Element)it.next();
                    } else {
                        nextRefEl = null;
                    }
                    break;
                }
            }
        }
        // Need to insert a new record before nextRef.
        Element newRefEl = ref.toXml(references.getOwnerDocument());
        // Note: OK if nextRefEl == null, that means insert as last child.
        references.insertBefore(newRefEl, nextRefEl);
        return true;
    }
    
    /**
     * Remove a reference to an artifact coming from a foreign project.
     * <p>
     * The property giving the location of the artifact is removed if it existed.
     * <p>
     * If this was the last reference to the foreign project, its location
     * property is removed as well.
     * <p>
     * If the reference does not exist, nothing is done.
     * <p>
     * Acquires write access.
     * @param foreignProjectName the local name of the foreign project
     *                           (usually its code name)
     * @param targetName the name of the Ant target corresponding to the build artifact
     * @return true if a reference or some property was actually removed,
     *         false if the reference was not there and no property was removed
     */
    public boolean removeReference(final String foreignProjectName, final String targetName) {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(true);
                boolean success;
                try {
                    success = removeRawReference(foreignProjectName, targetName, references);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return Boolean.FALSE;
                }
                if (success) {
                    storeReferences(references);
                }
                // Note: try to delete obsoleted properties from both project.properties
                // and private.properties, just in case.
                String[] PROPS_PATHS = {
                    AntProjectHelper.PROJECT_PROPERTIES_PATH,
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                };
                // Check whether there are any other references using foreignProjectName.
                // If not, we can delete ${project.foreignProjectName}.
                RawReference[] refs = getRawReferences(references);
                boolean deleteProjProp = true;
                for (int i = 0; i < refs.length; i++) {
                    if (refs[i].getForeignProjectName().equals(foreignProjectName)) {
                        deleteProjProp = false;
                        break;
                    }
                }
                if (deleteProjProp) {
                    String projProp = "project." + foreignProjectName; // NOI18N
                    for (int i = 0; i < PROPS_PATHS.length; i++) {
                        EditableProperties props = h.getProperties(PROPS_PATHS[i]);
                        if (props.containsKey(projProp)) {
                            props.remove(projProp);
                            h.putProperties(PROPS_PATHS[i], props);
                            success = true;
                        }
                    }
                }
                String refProp = "reference." + foreignProjectName + '.' + targetName; // NOI18N
                for (int i = 0; i < PROPS_PATHS.length; i++) {
                    EditableProperties props = h.getProperties(PROPS_PATHS[i]);
                    if (props.containsKey(refProp)) {
                        props.remove(refProp);
                        h.putProperties(PROPS_PATHS[i], props);
                        success = true;
                    }
                }
                return Boolean.valueOf(success);
            }
        })).booleanValue();
    }
    
    /**
     * Remove a raw reference to an artifact coming from a foreign project.
     * Does not attempt to manipulate backreferences in the foreign project
     * nor project properties.
     * <p>
     * If the reference does not exist, nothing is done.
     * <p>
     * Acquires write access.
     * @param foreignProjectName the local name of the foreign project
     *                           (usually its code name)
     * @param targetName the name of the Ant target corresponding to the build artifact
     * @return true if a reference was actually removed, false if it was not there
     */
    public boolean removeRawReference(final String foreignProjectName, final String targetName) {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(true);
                boolean success;
                try {
                    success = removeRawReference(foreignProjectName, targetName, references);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return Boolean.FALSE;
                }
                if (success) {
                    storeReferences(references);
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        })).booleanValue();
    }
    
    private static boolean removeRawReference(String foreignProjectName, String targetName, Element references) throws IllegalArgumentException {
        // As with addRawReference, do a linear search through.
        List/*<Element>*/subEls = Util.findSubElements(references);
        Iterator it = subEls.iterator();
        while (it.hasNext()) {
            Element testRefEl = (Element)it.next();
            RawReference testRef = RawReference.create(testRefEl);
            if (testRef.getForeignProjectName().compareTo(foreignProjectName) > 0) {
                // searched past it
                return false;
            }
            if (testRef.getForeignProjectName().equals(foreignProjectName)) {
                if (testRef.getTargetName().compareTo(targetName) > 0) {
                    // again, searched past it
                    return false;
                }
                if (testRef.getTargetName().equals(targetName)) {
                    // Key match, remove it.
                    references.removeChild(testRefEl);
                    return true;
                }
            }
        }
        // Searched through to the end and did not find it.
        return false;
    }

    /**
     * Get a list of raw references from this project to others.
     * If necessary, you may use {@link RawReference#toAntArtifact} to get
     * live information from each reference, such as its associated project.
     * <p>
     * Acquires read access.
     * @return a (possibly empty) list of raw references from this project
     */
    public RawReference[] getRawReferences() {
        return (RawReference[])ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(false);
                if (references != null) {
                    try {
                        return getRawReferences(references);
                    } catch (IllegalArgumentException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                return new RawReference[0];
            }
        });
    }
    
    private static RawReference[] getRawReferences(Element references) throws IllegalArgumentException {
        List/*<Element>*/subEls = Util.findSubElements(references);
        List/*<RawReference>*/ refs = new ArrayList(subEls.size());
        Iterator it = subEls.iterator();
        while (it.hasNext()) {
            refs.add(RawReference.create((Element)it.next()));
        }
        return (RawReference[])refs.toArray(new RawReference[refs.size()]);
    }
    
    /**
     * Get a particular raw reference from this project to another.
     * If necessary, you may use {@link RawReference#toAntArtifact} to get
     * live information from each reference, such as its associated project.
     * <p>
     * Acquires read access.
     * @param foreignProjectName the local name of the foreign project
     *                           (usually its code name)
     * @param targetName the name of the Ant target corresponding to the build artifact
     * @return the specified raw reference from this project,
     *         or null if none such could be found
     */
    public RawReference getRawReference(final String foreignProjectName, final String targetName) {
        return (RawReference)ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(false);
                if (references != null) {
                    try {
                        return getRawReference(foreignProjectName, targetName, references);
                    } catch (IllegalArgumentException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                return null;
            }
        });
    }
    
    private static RawReference getRawReference(String foreignProjectName, String targetName, Element references) throws IllegalArgumentException {
        List/*<Element>*/subEls = Util.findSubElements(references);
        Iterator it = subEls.iterator();
        while (it.hasNext()) {
            RawReference ref = RawReference.create((Element)it.next());
            if (ref.getForeignProjectName().equals(foreignProjectName) &&
                    ref.getTargetName().equals(targetName)) {
                return ref;
            }
        }
        return null;
    }
    
    /**
     * Create an Ant-interpretable string referring to a file on disk.
     * If the file refers to a known Ant artifact according to
     * {@link AntArtifactQuery#findArtifactFromFile}, of the expected type
     * and associated with a particular project,
     * the behavior is identical to {@link #createForeignFileReference(AntArtifact)}.
     * <p>
     * Otherwise, a simple path to the foreign file is created; it will
     * be relative in case {@link CollocationQuery#areCollocated} says that
     * the file is collocated with this project's main directory, else it
     * will be an absolute path.
     * (XXX if not collocated, should perhaps create a private.properties
     * entry for the absolute path and refer to that instead?)
     * <p>
     * Acquires write access.
     * @param file a file to refer to (need not currently exist)
     * @param expectedArtifactType the required {@link AntArtifact#getType}
     * @return a string which can refer to that file somehow
     */
    public String createForeignFileReference(final File file, final String expectedArtifactType) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        return (String)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                AntArtifact art = AntArtifactQuery.findArtifactFromFile(file);
                if (art != null && art.getType().equals(expectedArtifactType) && art.getProject() != null) {
                    try {
                        return createForeignFileReference(art);
                    } catch (IllegalArgumentException iae) {
                        throw new AssertionError(iae);
                    }
                } else {
                    File myProjDir = FileUtil.toFile(AntBasedProjectFactorySingleton.getProjectFor(h).getProjectDirectory());
                    if (CollocationQuery.areCollocated(myProjDir, file)) {
                        String path = PropertyUtils.relativizeFile(myProjDir, file);
                        assert path != null : "expected relative path from " + myProjDir + " to " + file;
                        return path;
                    } else {
                        // XXX should perhaps update private.properties with a placeholder property
                        // and return a reference to that, for maximum portability
                        return file.getAbsolutePath();
                    }
                }
            }
        });
    }
    
    /**
     * Create an Ant-interpretable string referring to a known build artifact file.
     * Simply calls {@link #addReference} and returns an Ant string which will
     * refer to that artifact correctly.
     * <p>
     * Acquires write access.
     * @param artifact a known build artifact to refer to
     * @return a string which can refer to that artifact file somehow
     * @throws IllegalArgumentException if the artifact is not associated with a project
     */
    public String createForeignFileReference(AntArtifact artifact) throws IllegalArgumentException {
        addReference(artifact);
        // XXX need to take uniquified foreign project name from addReference somehow
        String forProjName = artifact.getProject().getName();
        return "${reference." + forProjName + '.' + artifact.getTargetName() + '}'; // NOI18N
    }
    
    private static final Pattern FOREIGN_FILE_REFERENCE = Pattern.compile("\\$\\{reference\\.([^.${}]+)\\.([^.${}]+)\\}"); // NOI18N
    
    /**
     * Try to find an <code>AntArtifact</code> object corresponding to a given
     * foreign file reference.
     * If the supplied string is not a recognized reference to a build
     * artifact, returns null.
     * <p>Acquires read access.
     * @param reference a reference string as present in an Ant property
     * @return a corresponding Ant artifact object if there is one, else null
     */
    public AntArtifact getForeignFileReferenceAsArtifact(final String reference) {
        return (AntArtifact)ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Matcher m = FOREIGN_FILE_REFERENCE.matcher(reference);
                if (m.matches()) {
                    RawReference ref = getRawReference(m.group(1), m.group(2));
                    if (ref != null) {
                        return ref.toAntArtifact(ReferenceHelper.this);
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * Remove a reference to a foreign file from the project.
     * If the passed string consists of an Ant property reference corresponding to
     * a known inter-project reference, that reference is removed using
     * {@link #removeReference}. Since this would break any other identical foreign
     * file references present in the project, you should first confirm that this
     * reference was the last one of its kind (by string match).
     * <p>
     * If the passed string is anything else (i.e. a plain file path, relative or
     * absolute), nothing is done.
     * (XXX if dealing with absolute plain file paths specially using private.properties,
     * deal with that here too)
     * <p>
     * Acquires write access.
     * @param reference an Ant-interpretable foreign file reference as created e.g.
     *                  by {@link #createForeignFileReference(File,String)} or
     *                  by {@link #createForeignFileReference(AntArtifact)}
     */
    public void destroyForeignFileReference(String reference) {
        Matcher m = FOREIGN_FILE_REFERENCE.matcher(reference);
        if (m.matches()) {
            String forProjName = m.group(1);
            String targetName = m.group(2);
            removeReference(forProjName, targetName);
        }
    }
    
    /**
     * Create an object permitting this project to represent subprojects.
     * Would be placed into the project's lookup.
     * @return a subproject provider object suitable for the project lookup
     * @see Project#getLookup
     */
    public SubprojectProvider createSubprojectProvider() {
        return new SubprojectProviderImpl(this);
    }
    
    /**
     * Access from SubprojectProviderImpl.
     */
    AntProjectHelper getAntProjectHelper() {
        return h;
    }
    
    /**
     * A raw reference descriptor representing a link to a foreign project
     * and some build artifact used from it.
     * This class corresponds directly to what it stored in <code>project.xml</code>
     * to refer to a target in a foreign project.
     * See {@link AntArtifact} for the precise meaning of several of the fields in this class.
     */
    public static final class RawReference {
        
        private final String foreignProjectName;
        private final String artifactType;
        private final URI scriptLocation;
        private final String targetName;
        private final String cleanTargetName;
        
        /**
         * Create a raw reference descriptor.
         * As this is basically just a struct, does no real work.
         * @param foreignProjectName the name of the foreign project (usually its code name)
         * @param artifactType the {@link AntArtifact#getType type} of the build artifact
         * @param scriptLocation the relative URI to the build script from the project directory
         * @param targetName the Ant target name
         * @param cleanTargetName the Ant clean target name
         * @throws IllegalArgumentException if the script location is given an absolute URI
         */
        public RawReference(String foreignProjectName, String artifactType, URI scriptLocation, String targetName, String cleanTargetName) throws IllegalArgumentException {
            this.foreignProjectName = foreignProjectName;
            this.artifactType = artifactType;
            if (scriptLocation.isAbsolute()) {
                throw new IllegalArgumentException("Cannot use an absolute URI " + scriptLocation + " for script location"); // NOI18N
            }
            this.scriptLocation = scriptLocation;
            this.targetName = targetName;
            this.cleanTargetName = cleanTargetName;
        }
        
        private static final List/*<String>*/ SUB_ELEMENT_NAMES = Arrays.asList(new String[] {
            "foreign-project", // NOI18N
            "artifact-type", // NOI18N
            "script", // NOI18N
            "target", // NOI18N
            "clean-target", // NOI18N
        });
        
        /**
         * Create a RawReference by parsing an XML &lt;reference&gt; fragment.
         * @throws IllegalArgumentException if anything is missing or duplicated or malformed etc.
         */
        static RawReference create(Element xml) throws IllegalArgumentException {
            if (!REF_NAME.equals(xml.getLocalName()) || !REFS_NS.equals(xml.getNamespaceURI())) {
                throw new IllegalArgumentException("bad element name: " + xml); // NOI18N
            }
            NodeList nl = xml.getElementsByTagNameNS("*", "*"); // NOI18N
            if (nl.getLength() != 5) {
                throw new IllegalArgumentException("missing or extra data: " + xml); // NOI18N
            }
            String[] values = new String[5];
            for (int i = 0; i < 5; i++) {
                Element el = (Element)nl.item(i);
                if (!REFS_NS.equals(el.getNamespaceURI())) {
                    throw new IllegalArgumentException("bad subelement ns: " + el); // NOI18N
                }
                String elName = el.getLocalName();
                int idx = SUB_ELEMENT_NAMES.indexOf(elName);
                if (idx == -1) {
                    throw new IllegalArgumentException("bad subelement name: " + elName); // NOI18N
                }
                String val = Util.findText(el);
                if (val == null) {
                    throw new IllegalArgumentException("empty subelement: " + el); // NOI18N
                }
                if (values[idx] != null) {
                    throw new IllegalArgumentException("duplicate " + elName + ": " + values[idx] + " and " + val); // NOI18N
                }
                values[idx] = val;
            }
            assert !Arrays.asList(values).contains(null);
            URI scriptLocation = URI.create(values[2]); // throws IllegalArgumentException
            return new RawReference(values[0], values[1], scriptLocation, values[3], values[4]);
        }
        
        /**
         * Write a RawReference as an XML &lt;reference&gt; fragment.
         */
        Element toXml(Document ownerDocument) {
            Element el = ownerDocument.createElementNS(REFS_NS, REF_NAME);
            String[] values = {
                foreignProjectName,
                artifactType,
                scriptLocation.toString(),
                targetName,
                cleanTargetName,
            };
            for (int i = 0; i < 5; i++) {
                Element subel = ownerDocument.createElementNS(REFS_NS, (String)SUB_ELEMENT_NAMES.get(i));
                subel.appendChild(ownerDocument.createTextNode(values[i]));
                el.appendChild(subel);
            }
            return el;
        }
        
        /**
         * Get the name of the foreign project as referred to from this project.
         * Usually this will be the code name of the foreign project, but it may
         * instead be a uniquified name.
         * The name can be used in project properties and the build script to refer
         * to the foreign project from among subprojects.
         * @return the foreign project name
         */
        public String getForeignProjectName() {
            return foreignProjectName;
        }
        
        /**
         * Get the type of the foreign project's build artifact.
         * For example, {@link AntArtifact#TYPE_JAR}.
         * @return the artifact type
         */
        public String getArtifactType() {
            return artifactType;
        }
        
        /**
         * Get the location of the foreign project's build script relative to the
         * project directory.
         * This is the script which would be called to build the desired artifact.
         * @return the script location
         */
        public URI getScriptLocation() {
            return scriptLocation;
        }
        
        /**
         * Get the Ant target name to build the artifact.
         * @return the target name
         */
        public String getTargetName() {
            return targetName;
        }
        
        /**
         * Get the Ant target name to clean the artifact.
         * @return the clean target name
         */
        public String getCleanTargetName() {
            return cleanTargetName;
        }
        
        /**
         * Attempt to convert this reference to a live artifact object.
         * This involves finding the referenced foreign project on disk
         * (among standard project and private properties) and asking it
         * for the artifact named by the given target.
         * Given that object, you can find important further information
         * such as the location of the actual artifact on disk.
         * <p>
         * Note that non-key attributes of the returned artifact (i.e.
         * type, script location, and clean target name) might not match
         * those in this raw reference.
         * <p>
         * Acquires read access.
         * @param helper an associated reference helper used to resolve the foreign
         *               project location
         * @return the actual Ant artifact object, or null if it could not be located
         */
        public AntArtifact toAntArtifact(final ReferenceHelper helper) {
            return (AntArtifact)ProjectManager.mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    AntProjectHelper h = helper.h;
                    String path = h.evaluate("project." + foreignProjectName); // NOI18N
                    if (path == null) {
                        // Undefined foreign project.
                        return null;
                    }
                    FileObject foreignProjectDir = h.resolveFileObject(path);
                    if (foreignProjectDir == null) {
                        // Nonexistent foreign project dir.
                        return null;
                    }
                    Project p;
                    try {
                        p = ProjectManager.getDefault().findProject(foreignProjectDir);
                    } catch (IOException e) {
                        // Could not load it.
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        return null;
                    }
                    if (p == null) {
                        // Was not a project dir.
                        return null;
                    }
                    return AntArtifactQuery.findArtifactByTarget(p, targetName);
                }
            });
        }
        
        public String toString() {
            return "ReferenceHelper.RawReference<" + foreignProjectName + "," + artifactType + "," + scriptLocation + "," + targetName + "," + cleanTargetName + ">"; // NOI18N
        }
        
    }
    
}
