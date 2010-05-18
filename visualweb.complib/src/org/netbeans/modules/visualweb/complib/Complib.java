/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.complib;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.visualweb.complib.api.ComplibException;

/**
 * Represents a component library consisting of a collection of components. This is either a
 * built-in or an extension component library provided by a third party.
 * 
 * 
 * @author Edwin Goei
 */
public abstract class Complib implements Comparable<Complib> {
    /**
     * Represents an identifier for a Component Library.
     * 
     * @author Edwin Goei
     */
    public static class Identifier {
        private URI uri;

        private Version version;

        public Identifier(URI uri, int major, int minor, int micro) {
            if (uri == null) {
                throw new IllegalArgumentException("URI must not be null");
            }
            this.uri = uri;
            this.version = new Version(major, minor, micro);
        }

        /**
         * @param libraryUri
         * @param libraryVersion
         */
        public Identifier(String uriString, String versionString) {
            try {
                this.uri = new URI(uriString);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid library URI", ex);
            }

            try {
                this.version = new Version(versionString);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid library version", e);
            }
        }

        /**
         * Return Namespace URI
         * 
         * @return namespace URI
         */
        public URI getNamespaceUri() {
            return uri;
        }

        /**
         * Return version
         * 
         * @return version
         */
        public Version getVersion() {
            return version;
        }

        /**
         * @return The fully escaped version of the URI
         */
        public String getNamespaceUriString() {
            // Expose this method because of URI API bug where toString() does
            // not escape non-ASCII chars.
            return uri.toASCIIString();
        }

        /**
         * @return Version info as a String
         */
        public String getVersionString() {
            return version.toString();
        }

        /*
         * For debugging, not necessarily used for persistence so this can change in the future if
         * desired.
         */
        public String toString() {
            return asString();
        }

        /**
         * Both this URI and version must be non-null and correspondingly equal to that contained in
         * formal argument.
         */
        public boolean equals(Object anObject) {
            if (this == anObject) {
                return true;
            }
            if (anObject instanceof Identifier) {
                Identifier anotherId = (Identifier) anObject;
                if (uri.equals(anotherId.uri)
                        && getVersionString().equals(anotherId.getVersionString())) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return uri.hashCode() + version.hashCode();
        }

        /**
         * @return String representation of this identifier object used for persistence
         */
        public String asString() {
            return "(uri=" + getNamespaceUriString() + ", version=" + getVersionString() + ")";
        }
    }

    /**
     * For initial palette, represents a container that can contain InitialPaletteItem children.
     * 
     * @author Edwin Goei
     */
    private static abstract class AbstractParent {
        private ArrayList<InitialPaletteItem> children = new ArrayList<InitialPaletteItem>();

        public List<InitialPaletteItem> getChildren() {
            return children;
        }

        public void appendChild(InitialPaletteItem child) {
            children.add(child);
        }

    }

    /**
     * For initial palette, represents a container that can contain either InitialPaletteItem-s or
     * other InitialPaletteFolder-s. Possibly empty. Note: 2005-03-10 restricted to only contain
     * InitialPaletteItem-s.
     * 
     * @author Edwin Goei
     */
    public static class InitialPaletteFolder extends AbstractParent {
        private String name;

        /**
         * @param name
         *            Localized display name or resource key if not found
         */
        InitialPaletteFolder(String name) {
            this.name = name;
        }

        /**
         * Returns the localized display name of this initial folder or the key if resource is not
         * found
         * 
         * @return
         */
        public String getName() {
            return name;
        }

    }

    /**
     * For initial palette, represents a container that can only contain other InitialPaletteItem-s.
     * Possibly empty. InitialPaletteItems-s containing other InitialPaletteItems-s may be used to
     * organize the palette hierarchically.
     * 
     * @author Edwin Goei
     */
    public static class InitialPaletteItem extends AbstractParent {
        private String className;

        InitialPaletteItem(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    /** Complib configuration info */
    private ComplibManifest compLibManifest;

    private List<File> runtimePath;

    private List<File> designTimePath;

    private List<File> javadocPath;

    private List<File> sourcePath;

    private List<File> webResourcePath;

    private List<File> helpPath;

    /**
     * Note: to fully initialize a Complib subclass requires these steps and occur in the following
     * order: 1) calling the constructor 2) initCompLibManifest() 3) initPaths().
     * 
     * TODO Figure out how to clean this up
     */
    public Complib() {
    }

    /**
     * This method should be called in the constructor of a Complib subclass.
     * 
     * @param compLibManifest
     */
    protected void initCompLibManifest(ComplibManifest compLibManifest) {
        this.compLibManifest = compLibManifest;
    }

    protected ComplibManifest getCompLibManifest() {
        return compLibManifest;
    }

    /**
     * This method converts relative paths from the compLibConfig and turns them into absolute file
     * paths in the expanded complib form.
     * 
     * @throws ComplibException
     */
    protected void initPaths() throws ComplibException {
        this.runtimePath = convertConfigPathToFileList(compLibManifest.getRuntimePath());
        this.designTimePath = convertConfigPathToFileList(compLibManifest
                .getDeclaredDesignTimePath());
        this.javadocPath = convertConfigPathToFileList(compLibManifest.getJavadocPath());
        this.sourcePath = convertConfigPathToFileList(compLibManifest.getSourcePath());
        this.webResourcePath = convertConfigPathToFileList(compLibManifest.getWebResourcePath());
        this.helpPath = convertConfigPathToFileList(compLibManifest.getHelpPath());
    }

    /**
     * Convert from a ComponentLibraryConfiguration path containing relative File-s, ie. a List<String>
     * each representing a path element within a component library into a List<File> with absolute
     * File-s.
     * 
     * @param path
     *            List<String> each String representing a path element. Possibly empty, may not be
     *            null.
     * @return
     * @throws ComplibException
     *             if not valid
     */
    protected abstract List<File> convertConfigPathToFileList(List<String> path)
            throws ComplibException;

    /**
     * Returns the identifier for this component library
     * 
     * @return
     */
    public Complib.Identifier getIdentifier() {
        return compLibManifest.getIdentifier();
    }

    /**
     * Returns the localized Title if it has been localized
     * 
     * @return
     */
    public String getTitle() {
        return compLibManifest.getTitle();
    }

    /**
     * Returns the localized Title if it has been localized appended with a version
     * 
     * @return
     */
    public String getVersionedTitle() {
        return getTitle() + " (" + getIdentifier().getVersionString() + ")";
    }

    /**
     * Returns the runtime path
     * 
     * @return List of absolute File-s
     */
    public List<File> getRuntimePath() {
        return runtimePath;
    }

    /**
     * Returns the design-time path
     * 
     * @return List<File> of absolute File-s
     */
    public List<File> getDesignTimePath() {
        return designTimePath;
    }

    /**
     * Returns the javadoc path
     * 
     * @return List<File> of absolute File-s
     */
    public List<File> getJavadocPath() {
        return javadocPath;
    }

    /**
     * Returns the java source path
     * 
     * @return List<File> of absolute File-s
     */
    public List<File> getSourcePath() {
        return sourcePath;
    }

    /**
     * Returns the web resource path
     * 
     * @return List<File> of absolute File-s
     */
    public List<File> getWebResourcePath() {
        return webResourcePath;
    }

    /**
     * Returns the help path
     * 
     * @return List<File> of absolute File-s
     */
    public List<File> getHelpPath() {
        return helpPath;
    }

    /**
     * Return the help set file. Null means none found. "/" separated path to HelpSet file relative
     * to helpPath. eg. "help/my-help.hs"
     * 
     * @return the help set file. Null means none found.
     */
    public String getHelpSetFile() {
        return compLibManifest.getHelpSetFile();
    }

    /**
     * Return the help prefix. Null means no prefix attribute found.
     * 
     * @return the help prefix. Null means no prefix attribute found.
     */
    public String getHelpPrefix() {
        // TODO Not used. Remove.
        return compLibManifest.getHelpPrefix();
    }

    /**
     * Method does not throw a checked exception. Used by UI.
     * 
     * @return
     */
    public List<InitialPaletteFolder> getInitialPaletteFolders() {
        return compLibManifest.getInitialPalette();
    }

    /**
     * Main entry point to get list of components organized into InitialPaletteFolder-s for this
     * complib.
     * 
     * @return
     * @throws ComplibException
     */
    public List<InitialPaletteFolder> getComponentItemsInFolders() throws ComplibException {
        return compLibManifest.getInitialPalette();
    }

    /**
     * Return the BeanInfo given a class name
     * 
     * @param className
     * @return
     * @throws ClassNotFoundException
     * @throws IntrospectionException
     */
    abstract BeanInfo getBeanInfo(String className) throws ClassNotFoundException,
            IntrospectionException;

    /**
     * Returns true iff this bean should be hidden
     * 
     * @param itemClassName
     * @return
     */
    boolean isHidden(String itemClassName) {
        BeanInfo beanInfo;
        try {
            beanInfo = getBeanInfo(itemClassName);
        } catch (Exception e) {
            IdeUtil.logWarning(e);
            return false;
        }
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        return beanDescriptor.isHidden();
    }

    /**
     * Returns a ClassLoader that can be used to obtain design-time metadata for a component
     * library. Typically, both the runtime and design-time classes are accessible as well as
     * JavaHelp jars.
     * 
     * @return ClassLoader
     */
    public abstract ClassLoader getClassLoader();

    @Override
    public boolean equals(Object anObject) {
        // Two complibs are equal iff their identifiers are
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Complib) {
            Complib aComplib = (Complib) anObject;
            if (getIdentifier().equals(aComplib.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    public int compareTo(Complib aComplib) {
        URI myNamespace = getIdentifier().getNamespaceUri();
        URI otherNamespace = aComplib.getIdentifier().getNamespaceUri();
        if (!myNamespace.equals(otherNamespace)) {
            return myNamespace.compareTo(otherNamespace);
        }

        Version myVersion = getIdentifier().getVersion();
        Version otherVersion = aComplib.getIdentifier().getVersion();
        return myVersion.compareTo(otherVersion);
    }
}
