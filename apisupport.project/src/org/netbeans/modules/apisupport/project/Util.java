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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility methods for the module.
 * @author Jesse Glick
 */
public class Util {
    
    private Util() {}
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project"); // NOI18N
    
    // COPIED FROM org.netbeans.modules.project.ant:
    // (except for namespace == null support in findElement)
    // (and support for comments in findSubElements)
    
    /**
     * Search for an XML element in the direct children of a parent.
     * DOM provides a similar method but it does a recursive search
     * which we do not want. It also gives a node list and we want
     * only one result.
     * @param parent a parent element
     * @param name the intended local name
     * @param namespace the intended namespace (or null)
     * @return the one child element with that name, or null if none or more than one
     */
    public static Element findElement(Element parent, String name, String namespace) {
        Element result = null;
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)l.item(i);
                if ((namespace == null && name.equals(el.getTagName())) ||
                    (namespace != null && name.equals(el.getLocalName()) &&
                                          namespace.equals(el.getNamespaceURI()))) {
                    if (result == null) {
                        result = el;
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    public static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    /**
     * Find all direct child elements of an element.
     * More useful than {@link Element#getElementsByTagNameNS} because it does
     * not recurse into recursive child elements.
     * Children which are all-whitespace text nodes or comments are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     */
    public static List/*<Element>*/ findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List/*<Element>*/ elements = new ArrayList(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // OK, ignore
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }
    
    // CANDIDATES FOR FileUtil (#59311):
    
    /**
     * Creates a URL for a directory on disk.
     * Works correctly even if the directory does not currently exist.
     */
    public static URL urlForDir(File dir) {
        try {
            URL u = FileUtil.normalizeFile(dir).toURI().toURL();
            String s = u.toExternalForm();
            if (s.endsWith("/")) { // NOI18N
                return u;
            } else {
                return new URL(s + "/"); // NOI18N
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Creates a URL for the root of a JAR on disk.
     */
    public static URL urlForJar(File jar) {
        try {
            return FileUtil.getArchiveRoot(FileUtil.normalizeFile(jar).toURI().toURL());
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Creates a URL for a directory on disk or the root of a JAR.
     * Works correctly whether or not the directory or JAR currently exists.
     * Detects whether the file is supposed to be a directory or a JAR.
     */
    public static URL urlForDirOrJar(File location) {
        try {
            URL u = FileUtil.normalizeFile(location).toURI().toURL();
            if (FileUtil.isArchiveFile(u)) {
                u = FileUtil.getArchiveRoot(u);
            } else {
                String us = u.toExternalForm();
                if (!us.endsWith("/")) { // NOI18N
                    u = new URL(us + "/"); // NOI18N
                }
            }
            return u;
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Tries to find {@link Project} in the given directory. If succeeds
     * delegates to {@link ProjectInformation#getDisplayName}. Returns {@link
     * FileUtil#getFileDisplayName} otherwise.
     */
    public static String getDisplayName(FileObject projectDir) {
        try {
            Project p = ProjectManager.getDefault().findProject(projectDir);
            return ProjectUtils.getInformation(p).getDisplayName();
        } catch (IOException e) {
            return FileUtil.getFileDisplayName(projectDir);
        }
    }
    
    /**
     * Normalizes the given value to a regular dotted code name base.
     * @param value to be normalized
     */
    public static String normalizeCNB(String value) {
        StringTokenizer tk = new StringTokenizer(value.toLowerCase(), ".", true); //NOI18N
        StringBuffer normalizedCNB = new StringBuffer();
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (!delimExpected) {
                if (namePart.equals(".")) { //NOI18N
                    continue;
                }
                for (int i = 0; i < namePart.length(); i++) {
                    char c = namePart.charAt(i);
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)) {
                            continue;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)) {
                            continue;
                        }
                    }
                    normalizedCNB.append(c);
                }
            } else {
                if (namePart.equals(".")) { //NOI18N
                    normalizedCNB.append(namePart);
                }
            }
            delimExpected = !delimExpected;
        }
        // also be sure there is no '.' left at the end of the cnb
        return normalizedCNB.toString().replaceAll("\\.$", ""); // NOI18N
    }
    
    /**
     * Search for an appropriate localized bundle (i.e.
     * OpenIDE-Module-Localizing-Bundle) entry in the given
     * <code>manifest</code> taking into account branding and localization
     * (using {@link NbBundle#getLocalizingSuffixes}) and returns an
     * appropriate <em>valid</em> {@link LocalizedBundleInfo} instance. By
     * <em>valid</em> it's meant that a found localized bundle contains at
     * least a display name. If <em>valid</em> bundle is not found
     * <code>null</code> is returned.
     * 
     * @param sourceDir source directory to be used for as a <em>searching
     *        path</em> for the bundle
     * @param manifest manifest the bundle's path should be extracted from
     * @return localized bundle info for the given project or <code>null</code>
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(FileObject sourceDir, Manifest manifest) {
        LocalizedBundleInfo locInfo = null;
        String locBundleResource =
                ManifestManager.getInstance(manifest, false).getLocalizingBundle();
        try {
            if (locBundleResource != null) {
                for (Iterator it = getPossibleResources(locBundleResource); it.hasNext(); ) {
                    String resource = (String) it.next();
                    FileObject bundleFO = sourceDir.getFileObject(resource);
                    if (bundleFO != null) {
                        LocalizedBundleInfo supposedLI;
                        supposedLI = LocalizedBundleInfo.load(bundleFO);
                        if (supposedLI.getDisplayName() != null) {
                            supposedLI.setPath(FileUtil.toFile(bundleFO).getAbsolutePath());
                            locInfo = supposedLI;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return locInfo;
    }
    
    /**
     * Actually deletages to {@link #findLocalizedBundleInfo(FileObject, Manifest)}.
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(File projectDir) {
        FileObject sourceDir = FileUtil.toFileObject(new File(projectDir, "src")); // NOI18N
        FileObject manifestFO = FileUtil.toFileObject(new File(projectDir, "manifest.mf")); // NOI18N
        
        LocalizedBundleInfo locInfo = null;
        Manifest mf = getManifest(manifestFO);
        if (sourceDir != null && mf != null) {
            locInfo = findLocalizedBundleInfo(sourceDir, mf);
        }
        return locInfo;
    }
    
    /**
     * The same as {@link #findLocalizedBundleInfo(FileObject, Manifest)} but
     * searching in the given JAR representing a NetBeans module.
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(JarFile binaryProject) {
        LocalizedBundleInfo locInfo = null;
        try {
            Manifest mf = binaryProject.getManifest();
            if (mf != null) {
                String locBundleResource =
                        ManifestManager.getInstance(mf, false).getLocalizingBundle();
                if (locBundleResource != null) {
                    for (Iterator it = getPossibleResources(locBundleResource); it.hasNext(); ) {
                        String resource = (String) it.next();
                        ZipEntry entry = binaryProject.getEntry(resource);
                        if (entry != null) {
                            InputStream bundleIS = binaryProject.getInputStream(entry);
                            try {
                                LocalizedBundleInfo supposedLI = LocalizedBundleInfo.load(bundleIS);
                                if (supposedLI.getDisplayName() != null) {
                                    locInfo = supposedLI;
                                    break;
                                }
                            } finally {
                                bundleIS.close();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return locInfo;
    }
    
    /**
     * Convenience method for loading {@link EditableProperties} from a {@link
     * FileObject}. New items will alphabetizied by key.
     *
     * @param propsFO file representing properties file
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream propsIS = propsFO.getInputStream();
        EditableProperties props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }
    
    /**
     * Convenience method for storing {@link EditableProperties} into a {@link
     * FileObject}.
     *
     * @param propsFO file representing where properties will be stored
     * @param props properties to be stored
     * @exception IOException if properties cannot be written to the file
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try {
            OutputStream os = propsFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Convenience method for loading {@link EditableManifest} from a {@link
     * FileObject}.
     *
     * @param manifestFO file representing manifest
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableManifest loadManifest(FileObject manifestFO) throws IOException {
        InputStream mfIS = manifestFO.getInputStream();
        try {
            EditableManifest mf = new EditableManifest(mfIS);
            return mf;
        } finally {
            mfIS.close();
        }
    }
    
    /**
     * Convenience method for storing {@link EditableManifest} into a {@link
     * FileObject}.
     * 
     * @param manifestFO file representing where manifest will be stored
     * @param em manifest to be stored
     * @exception IOException if manifest cannot be written to the file
     */
    public static void storeManifest(FileObject manifestFO, EditableManifest em) throws IOException {
        FileLock lock = manifestFO.lock();
        try {
            OutputStream os = manifestFO.getOutputStream(lock);
            try {
                em.write(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private static Iterator getPossibleResources(String locBundleResource) {
        String locBundleResourceBase, locBundleResourceExt;
        int idx = locBundleResource.lastIndexOf('.');
        if (idx != -1 && idx > locBundleResource.lastIndexOf('/')) {
            locBundleResourceBase = locBundleResource.substring(0, idx);
            locBundleResourceExt = locBundleResource.substring(idx);
        } else {
            locBundleResourceBase = locBundleResource;
            locBundleResourceExt = "";
        }
        Collection/*<String>*/ resources = new LinkedHashSet();
        for (Iterator it = NbBundle.getLocalizingSuffixes(); it.hasNext(); ) {
            String suffix = (String) it.next();
            String resource = locBundleResourceBase + suffix + locBundleResourceExt;
            resources.add(resource);
            resources.add(resource);
        }
        return resources.iterator();
    }
    
    private static Manifest getManifest(FileObject manifestFO) {
        if (manifestFO != null) {
            try {
                InputStream is = manifestFO.getInputStream();
                try {
                    return new Manifest(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
}
