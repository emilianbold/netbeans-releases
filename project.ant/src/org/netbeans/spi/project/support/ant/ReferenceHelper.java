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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.Util;
import org.netbeans.spi.project.AuxiliaryConfiguration;
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
 * and {@link #removeReference(String,String)}; the lower-level methods such as {@link #addRawReference}
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
    
    /** Set of property names which values can be used as additional base
     * directories. */
    private Set/*<String>*/ extraBaseDirectories = new HashSet();
    
    private final AntProjectHelper h;
    final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;

    /**
     * Create a new reference helper.
     * It needs an {@link AntProjectHelper} object in order to update references
     * in <code>project.xml</code>,
     * as well as set project or private properties referring to the locations
     * of foreign projects on disk.
     * <p>
     * The property evaluator may be used in {@link #getForeignFileReferenceAsArtifact},
     * {@link ReferenceHelper.RawReference#toAntArtifact}, or
     * {@link #createSubprojectProvider}. Typically this would
     * be {@link AntProjectHelper#getStandardPropertyEvaluator}. You can substitute
     * a custom evaluator but be warned that this helper class assumes that
     * {@link AntProjectHelper#PROJECT_PROPERTIES_PATH} and {@link AntProjectHelper#PRIVATE_PROPERTIES_PATH}
     * have their customary meanings; specifically that they are both used when evaluating
     * properties (such as the location of a foreign project) and that private properties
     * can override public properties.
     * @param helper an Ant project helper object representing this project's configuration
     * @param aux an auxiliary configuration provider needed to store references
     * @param eval a property evaluator
     */
    public ReferenceHelper(AntProjectHelper helper, AuxiliaryConfiguration aux, PropertyEvaluator eval) {
        h = helper;
        this.aux = aux;
        this.eval = eval;
    }

    /**
     * Load <references> from project.xml.
     * @param create if true, create an empty element if it was missing, else leave as null
     */
    private Element loadReferences(boolean create) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        Element references = aux.getConfigurationFragment(REFS_NAME, REFS_NS, true);
        if (references == null && create) {
            references = XMLUtil.createDocument("ignore", null, null, null).createElementNS(REFS_NS, REFS_NAME); // NOI18N
        }
        return references;
    }

    /**
     * Store <references> to project.xml (i.e. to memory and mark project modified).
     */
    private void storeReferences(Element references) {
        assert ProjectManager.mutex().isWriteAccess();
        assert references != null && references.getLocalName().equals(REFS_NAME) && REFS_NS.equals(references.getNamespaceURI());
        aux.putConfigurationFragment(references, true);
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
                File forProjDir = FileUtil.toFile(forProj.getProjectDirectory());
                assert forProjDir != null : forProj.getProjectDirectory();
                String projName = getUsableReferenceID(ProjectUtils.getInformation(forProj).getName());
                String forProjName = findReferenceID(projName, "project.", forProjDir.getAbsolutePath());
                if (forProjName == null) {
                    forProjName = generateUniqueID(projName, "project.", forProjDir.getAbsolutePath());
                }
                File scriptFile = artifact.getScriptLocation();
                URI scriptLocation;
                String rel = PropertyUtils.relativizeFile(forProjDir, scriptFile);
                try {
                    scriptLocation = new URI(null, null, rel, null);
                } catch (URISyntaxException ex) {
                    scriptLocation = forProjDir.toURI().relativize(scriptFile.toURI());
                }
                RawReference ref = new RawReference(forProjName, artifact.getType(), scriptLocation, artifact.getTargetName(), artifact.getCleanTargetName(), artifact.getID());
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
                FileObject myProjDirFO = AntBasedProjectFactorySingleton.getProjectFor(h).getProjectDirectory();
                File myProjDir = FileUtil.toFile(myProjDirFO);
                String forProjPath;
                String propertiesFile;
                if (CollocationQuery.areCollocated(myProjDir, forProjDir)) {
                    // Fine, using a relative path to subproject.
                    forProjPath = PropertyUtils.relativizeFile(myProjDir, forProjDir);
                    assert forProjPath != null : "These dirs are not really collocated: " + myProjDir + " & " + forProjDir;
                    propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                } else {
                    forProjPath = relativizeFileToExtraBaseFolders(forProjDir);
                    if (forProjPath != null) {
                        propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                    } else {
                        // Use an absolute path.
                        forProjPath = forProjDir.getAbsolutePath();
                        propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
                    }
                }
                EditableProperties props = h.getProperties(propertiesFile);
                String forProjPathProp = "project." + forProjName; // NOI18N
                if (!forProjPath.equals(props.getProperty(forProjPathProp))) {
                    props.put(forProjPathProp, forProjPath);
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
                    refPath = "${" + forProjPathProp + "}/" + artFile.getPath(); // NOI18N
                    propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                }
                props = h.getProperties(propertiesFile);
                String refPathProp = "reference." + forProjName + '.' + getUsableReferenceID(artifact.getID()); // NOI18N
                if (!refPath.equals(props.getProperty(refPathProp))) {
                    props.put(refPathProp, refPath);
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
                if (testRef.getID().compareTo(ref.getID()) > 0) {
                    // again, gone too far, go back
                    nextRefEl = testRefEl;
                    break;
                }
                if (testRef.getID().equals(ref.getID())) {
                    // Key match, check if it needs to be updated.
                    if (testRef.getArtifactType().equals(ref.getArtifactType()) &&
                            testRef.getScriptLocation().equals(ref.getScriptLocation()) &&
                            testRef.getTargetName().equals(ref.getTargetName()) &&
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
     * @param id the ID of the build artifact (usually build target name)
     * @return true if a reference or some property was actually removed,
     *         false if the reference was not there and no property was removed
     */
    public boolean removeReference(final String foreignProjectName, final String id) {
        return removeReference(foreignProjectName, id, false);
    }
    
    private boolean removeReference(final String foreignProjectName, final String id, final boolean escaped) {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(true);
                boolean success;
                try {
                    success = removeRawReference(foreignProjectName, id, references, escaped);
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
                String refProp = "reference." + foreignProjectName + '.' + getUsableReferenceID(id); // NOI18N
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
     * Remove reference to a file.
     * <p>
     * If the reference does not exist, nothing is done.
     * <p>
     * Acquires write access.
     * @param fileReference file reference as created by 
     *    {@link #createForeignFileReference(File, String)}
     * @return true if the reference was actually removed; otherwise false
     */
    public boolean removeReference(final String fileReference) {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                boolean success = false;
                // Note: try to delete obsoleted properties from both project.properties
                // and private.properties, just in case.
                String[] PROPS_PATHS = {
                    AntProjectHelper.PROJECT_PROPERTIES_PATH,
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                };
                String refProp = fileReference;
                if (refProp.startsWith("${") && refProp.endsWith("}")) {
                    refProp = refProp.substring(2, refProp.length()-1);
                }
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
     * @param id the ID of the build artifact (usually build target name)
     * @return true if a reference was actually removed, false if it was not there
     */
    public boolean removeRawReference(final String foreignProjectName, final String id) {
        return ((Boolean)ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(true);
                boolean success;
                try {
                    success = removeRawReference(foreignProjectName, id, references, false);
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
    
    private static boolean removeRawReference(String foreignProjectName, String id, Element references, boolean escaped) throws IllegalArgumentException {
        // As with addRawReference, do a linear search through.
        List/*<Element>*/subEls = Util.findSubElements(references);
        Iterator it = subEls.iterator();
        while (it.hasNext()) {
            Element testRefEl = (Element)it.next();
            RawReference testRef = RawReference.create(testRefEl);
            String refID = testRef.getID();
            String refName = testRef.getForeignProjectName();
            if (escaped) {
                refID = getUsableReferenceID(testRef.getID());
                refName = getUsableReferenceID(testRef.getForeignProjectName());
            }
            if (refName.compareTo(foreignProjectName) > 0) {
                // searched past it
                return false;
            }
            if (refName.equals(foreignProjectName)) {
                if (refID.compareTo(id) > 0) {
                    // again, searched past it
                    return false;
                }
                if (refID.equals(id)) {
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
     * @param id the ID of the build artifact (usually the build target name)
     * @return the specified raw reference from this project,
     *         or null if none such could be found
     */
    public RawReference getRawReference(final String foreignProjectName, final String id) {
        return getRawReference(foreignProjectName, id, false);
    }
    
    // not private only to allow unit testing
    RawReference getRawReference(final String foreignProjectName, final String id, final boolean escaped) {
        return (RawReference)ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element references = loadReferences(false);
                if (references != null) {
                    try {
                        return getRawReference(foreignProjectName, id, references, escaped);
                    } catch (IllegalArgumentException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                return null;
            }
        });
    }
    
    private static RawReference getRawReference(String foreignProjectName, String id, Element references, boolean escaped) throws IllegalArgumentException {
        List/*<Element>*/subEls = Util.findSubElements(references);
        Iterator it = subEls.iterator();
        while (it.hasNext()) {
            RawReference ref = RawReference.create((Element)it.next());
            String refID = ref.getID();
            String refName = ref.getForeignProjectName();
            if (escaped) {
                refID = getUsableReferenceID(ref.getID());
                refName = getUsableReferenceID(ref.getForeignProjectName());
            }
            if (refName.equals(foreignProjectName) && refID.equals(id)) {
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
     * Otherwise, a reference for the file is created. The file path will
     * be relative in case {@link CollocationQuery#areCollocated} says that
     * the file is collocated with this project's main directory, else it
     * will be an absolute path.
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
                    String propertiesFile;
                    String path;
                    File myProjDir = FileUtil.toFile(AntBasedProjectFactorySingleton.getProjectFor(h).getProjectDirectory());
                    if (CollocationQuery.areCollocated(myProjDir, file)) {
                        propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                        path = PropertyUtils.relativizeFile(myProjDir, file);
                        assert path != null : "expected relative path from " + myProjDir + " to " + file;
                    } else {
                        path = relativizeFileToExtraBaseFolders(file);
                        if (path != null) {
                            propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                        } else {
                            propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
                            path = file.getAbsolutePath();
                        }
                    }
                    EditableProperties props = h.getProperties(propertiesFile);
                    String fileID = file.getName();
                    // if the file is folder then add to ID string also parent folder name,
                    // i.e. if external source folder name is "src" the ID will
                    // be a bit more selfdescribing, e.g. project-src in case
                    // of ID for ant/project/src directory.
                    if (file.isDirectory() && file.getParentFile() != null) {
                        fileID = file.getParentFile().getName()+"-"+file.getName();
                    }
                    fileID = PropertyUtils.getUsablePropertyName(fileID);
                    String prop = findReferenceID(fileID, "file.reference.", file.getAbsolutePath()); // NOI18N
                    if (prop == null) {
                        prop = generateUniqueID(fileID, "file.reference.", file.getAbsolutePath()); // NOI18N
                    }
                    if (!path.equals(props.getProperty("file.reference." + prop))) { // NOI18N
                        props.put("file.reference." + prop, path); // NOI18N
                        h.putProperties(propertiesFile, props);
                    }
                    return "${file.reference." + prop + '}'; // NOI18N
                }
            }
        });
    }
    
    /**
     * Test whether file does not lie under an extra base folder and if it does
     * then return string in form of "${extra.base}/remaining/path"; or null.
     */
    private String relativizeFileToExtraBaseFolders(File f) {
        File base = FileUtil.toFile(h.getProjectDirectory());
        String fileToRelativize = f.getAbsolutePath();
        Iterator it = extraBaseDirectories.iterator();
        while (it.hasNext()) {
            String prop = (String)it.next();
            String path = eval.getProperty(prop);
            File extraBase = PropertyUtils.resolveFile(base, path);
            path = extraBase.getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            if (fileToRelativize.startsWith(path)) {
                return "${"+prop+"}/"+fileToRelativize.substring(path.length()).replace('\\', '/'); // NOI18N
            }
        }
        return null;
    }

    /**
     * Add extra folder which can be used as base directory (in addition to
     * project base folder) for creating references. Duplicate property names
     * are not allowed. Any newly created reference to a file lying under an
     * extra base directory will be based on that property and will be stored in
     * shared project properties.
     * <p>Acquires write access.
     * @param propertyName property name which value is path to folder which
     *  can be used as alternative project's base directory; cannot be null;
     *  property must exist
     * @throws IllegalArgumentException if propertyName is null or such a 
     *   property does not exist
     * @since 1.4
     */
    public void addExtraBaseDirectory(final String propertyName) {
        if (propertyName == null || eval.getProperty(propertyName) == null) {
            throw new IllegalArgumentException("propertyName is null or such a property does not exist: "+propertyName); // NOI18N
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    if (!extraBaseDirectories.add(propertyName)) {
                        throw new IllegalArgumentException("Already extra base directory property: "+propertyName); // NOI18N
                    }
                }
            });
    }
    
    /**
     * Remove extra base directory. The base directory property had to be added
     * by {@link #addExtraBaseDirectory} method call. At the time when this
     * method is called the property must still exist and must be valid. This
     * method will replace all references of the extra base directory property
     * with its current value and if needed it may move such a property from
     * shared project properties into the private properties.
     * <p>Acquires write access.
     * @param propertyName property name which was added by 
     * {@link #addExtraBaseDirectory} method.
     * @throws IllegalArgumentException if given property is not extra base 
     *   directory
     * @since 1.4
     */
    public void removeExtraBaseDirectory(final String propertyName) {
        ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    if (!extraBaseDirectories.remove(propertyName)) {
                        throw new IllegalArgumentException("Non-existing extra base directory property: "+propertyName); // NOI18N
                    }
                    // substitute all references of removed extra base folder property with its value
                    String tag = "${"+propertyName+"}"; // NOI18N
                    // was extra base property defined in shared file or not:
                    boolean shared = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).containsKey(propertyName);
                    String value = eval.getProperty(propertyName);
                    EditableProperties propProj = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties propPriv = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    boolean modifiedProj = false;
                    boolean modifiedPriv = false;
                    Iterator it = propProj.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry)it.next();
                        String val = (String)entry.getValue();
                        int index;
                        if ((index = val.indexOf(tag)) != -1) {
                            val = val.substring(0, index) +value + val.substring(index+tag.length());
                            if (shared) {
                                // substitute extra base folder property with its value
                                entry.setValue(val);
                                modifiedProj = true;
                            } else {
                                // move property to private properties file
                                it.remove();
                                propPriv.put(entry.getKey(), val);
                                modifiedPriv = true;
                                modifiedProj = true;
                            }
                        }
                    }
                    if (modifiedProj) {
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, propProj);
                    }
                    if (modifiedPriv) {
                        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, propPriv);
                    }
                }
            });
    }
    
    /**
     * Find reference ID (e.g. something you can then pass to RawReference 
     * as foreignProjectName) for the given property base name, prefix and path.
     * @param property project name or jar filename
     * @param prefix prefix used for reference, i.e. "project." for project 
     *    reference or "file.reference." for file reference
     * @param path absolute filename the reference points to
     * @return found reference ID or null
     */
    private String findReferenceID(String property, String prefix, String path) {
        Map m = h.getStandardPropertyEvaluator().getProperties();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            if (key.startsWith(prefix+property)) {
                String v = h.resolvePath((String)m.get(key));
                if (path.equals(v)) {
                    return key.substring(prefix.length());
                }
            }
        }
        return null;
    }
    
    /**
     * Find reference ID for the given AntArtifact. See also 
     * {@link #findReferenceID(String, String, String)}.
     */
    private String findReferenceID(AntArtifact artifact) {
        Project proj = artifact.getProject();
        if (proj == null) {
            throw new IllegalArgumentException("No project associated with " + artifact); // NOI18N
        }
        File projDir = FileUtil.toFile(proj.getProjectDirectory());
        assert projDir != null : proj.getProjectDirectory();
        String usableID = getUsableReferenceID(ProjectUtils.getInformation(proj).getName());
        String path = projDir.getAbsolutePath();
        String id = findReferenceID(usableID, "project.", path); // NOI18N
        assert id != null : "Did not have a ref ID for " + artifact + " with usable ID " + usableID + " and path " + path + " among " + h.getStandardPropertyEvaluator().getProperties();
        return id;
    }

    /**
     * Generate unique reference ID for the given property base name, prefix 
     * and path. See also {@link #findReferenceID(String, String, String)}.
     * @param property project name or jar filename
     * @param prefix prefix used for reference, i.e. "project." for project 
     *    reference or "file.reference." for file reference
     * @param path absolute filename the reference points to
     * @return generated unique reference ID
     */
    private String generateUniqueID(String property, String prefix, String value) {
        PropertyEvaluator pev = h.getStandardPropertyEvaluator();
        if (pev.getProperty(prefix+property) == null) {
            return property;
        }
        int i = 1;
        while (pev.getProperty(prefix+property+"-"+i) != null) {
            i++;
        }
        return property+"-"+i;
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
        String projID = findReferenceID(artifact);
        return "${reference." + projID + '.' + getUsableReferenceID(artifact.getID()) + '}'; // NOI18N
    }

    /**
     * Project reference ID cannot contain dot character.
     * File reference can.
     */
    private static String getUsableReferenceID(String ID) {
        return PropertyUtils.getUsablePropertyName(ID).replace('.', '_');
    }
    
    
    private static final Pattern FOREIGN_FILE_REFERENCE = Pattern.compile("\\$\\{reference\\.([^.${}]+)\\.([^.${}]+)\\}"); // NOI18N
    private static final Pattern FOREIGN_PLAIN_FILE_REFERENCE = Pattern.compile("\\$\\{file\\.reference\\.([^${}]+)\\}"); // NOI18N
    
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
                    RawReference ref = getRawReference(m.group(1), m.group(2), true);
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
     * a known inter-project reference created by 
     * {@link #createForeignFileReference(AntArtifact)} or file reference created by
     * {@link #createForeignFileReference(File, String)}, that reference is removed using
     * {@link #removeReference(String, String)} or {@link #removeReference(String)}.
     * Since this would break any other identical foreign
     * file references present in the project, you should first confirm that this
     * reference was the last one of its kind (by string match).
     * <p>
     * If the passed string is anything else (i.e. a plain file path, relative or
     * absolute), nothing is done.
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
            String id = m.group(2);
            removeReference(forProjName, id, true);
            return;
        }
        m = FOREIGN_PLAIN_FILE_REFERENCE.matcher(reference);
        if (m.matches()) {
            removeReference(reference);
            return;
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
        private final String artifactID;
        
        /**
         * Create a raw reference descriptor.
         * As this is basically just a struct, does no real work.
         * @param foreignProjectName the name of the foreign project (usually its code name)
         * @param artifactType the {@link AntArtifact#getType type} of the build artifact
         * @param scriptLocation the relative URI to the build script from the project directory
         * @param targetName the Ant target name
         * @param cleanTargetName the Ant clean target name
         * @param artifactID the {@link AntArtifact#getID ID} of the build artifact
         * @throws IllegalArgumentException if the script location is given an absolute URI
         */
        public RawReference(String foreignProjectName, String artifactType, URI scriptLocation, String targetName, String cleanTargetName, String artifactID) throws IllegalArgumentException {
            this.foreignProjectName = foreignProjectName;
            this.artifactType = artifactType;
            if (scriptLocation.isAbsolute()) {
                throw new IllegalArgumentException("Cannot use an absolute URI " + scriptLocation + " for script location"); // NOI18N
            }
            this.scriptLocation = scriptLocation;
            this.targetName = targetName;
            this.cleanTargetName = cleanTargetName;
            this.artifactID = artifactID;
        }
        
        private static final List/*<String>*/ SUB_ELEMENT_NAMES = Arrays.asList(new String[] {
            "foreign-project", // NOI18N
            "artifact-type", // NOI18N
            "script", // NOI18N
            "target", // NOI18N
            "clean-target", // NOI18N
            "id", // NOI18N
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
            if (nl.getLength() != 6) {
                throw new IllegalArgumentException("missing or extra data: " + xml); // NOI18N
            }
            String[] values = new String[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
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
            return new RawReference(values[0], values[1], scriptLocation, values[3], values[4], values[5]);
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
                artifactID,
            };
            for (int i = 0; i < 6; i++) {
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
         * For example, <a href="@JAVA/PROJECT@/org/netbeans/api/java/project/JavaProjectConstants.html#ARTIFACT_TYPE_JAR"><code>JavaProjectConstants.ARTIFACT_TYPE_JAR</code></a>.
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
         * Get the ID of the foreign project's build artifact.
         * See also {@link AntArtifact#getID}.
         * @return the artifact identifier
         */
        public String getID() {
            return artifactID;
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
                    String path = helper.eval.getProperty("project." + foreignProjectName); // NOI18N
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
                    return AntArtifactQuery.findArtifactByID(p, artifactID);
                }
            });
        }
        
        public String toString() {
            return "ReferenceHelper.RawReference<" + foreignProjectName + "," + artifactType + "," + scriptLocation + "," + targetName + "," + cleanTargetName + "," + artifactID + ">"; // NOI18N
        }
        
    }
    
}
