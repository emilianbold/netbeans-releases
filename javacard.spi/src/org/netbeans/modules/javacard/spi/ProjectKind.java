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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import java.awt.Image;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enum of project subtypes.  Use toString() to get the value expected in
 * project.xml
 * <p>
 * Most differences between the various project subtypes are encoded here,
 * as they are largely ones of strings and templates, not logic.
 *
 * @author Tim Boudreau
 */
public enum ProjectKind {

    WEB("org.netbeans.modules.javacard.webproject", //NOI18N
    "org/netbeans/modules/javacard/spi/resources/webproject.png"), //NOI18N
    EXTENDED_APPLET("org.netbeans.modules.javacard.eapproject", //NOI18N
    "org/netbeans/modules/javacard/spi/resources/eapproject.png"), //NOI18N
    CLASSIC_APPLET("org.netbeans.modules.javacard.capproject", //NOI18N
    "org/netbeans/modules/javacard/spi/resources/capproject.png"), //NOI18N
    EXTENSION_LIBRARY("org.netbeans.modules.javacard.extlibproject", //NOI18N
    "org/netbeans/modules/javacard/spi/resources/extlibproject.png"), //NOI18N
    CLASSIC_LIBRARY("org.netbeans.modules.javacard.clslibproject", //NOI18N
    "org/netbeans/modules/javacard/spi/resources/clslibproject.png"); //NOI18N
    public static final String FO_ATTR_PROJECT_TYPE = "projectType"; //NOI18N
    private final String id;
    private final String iconPath;

    ProjectKind(String id, String iconPath) {
        this.id = id;
        this.iconPath = iconPath;
    }

    /**
     * Icon for this project type, to be shown in the projects window
     * @return The project type
     */
    public Image icon() {
        return ImageUtilities.loadImage(iconPath);
    }

    /**
     * String path to the icon file within the module JAR, for use with
     * DataNode.setIconBaseWithExtension().
     * @return
     */
    public String iconPath() {
        return iconPath;
    }

    @Override
    /**
     * Returns the ID of this project, as used in project.xml
     */
    public String toString() {
        return id;
    }

    /**
     * Determine if this project represents a classic applet or classic library
     * project
     * @return
     */
    public boolean isClassic() {
        return this == CLASSIC_APPLET || this == CLASSIC_LIBRARY;
    }

    /**
     * Get a human readable display name for this kind of project
     * @return A display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(ProjectKind.class, this.name());
    }

    /**
     * Return a prototype file name for new projects of this kind
     * @return A prototype file name such as "ClassicApplet"
     */
    public String prototypeProjectName() {
        switch (this) {
            case WEB:
                return "WebApplication"; //NOI18N
            case EXTENDED_APPLET:
                return "ExtendedApplet"; //NOI18N
            case CLASSIC_APPLET:
                return "ClassicApplet"; //NOI18N
            case EXTENSION_LIBRARY:
                return "ExtensionLibrary"; //NOI18N
            case CLASSIC_LIBRARY:
                return "ClassicLibrary"; //NOI18N
            default:
                throw new AssertionError();
        }
    }

    /**
     * Determine if this project is something which can be run
     * (applet or web project)
     * @return
     */
    public boolean isApplication() {
        switch (this) {
            case EXTENDED_APPLET:
            case CLASSIC_APPLET:
            case WEB:
                return true;
            case EXTENSION_LIBRARY:
            case CLASSIC_LIBRARY:
                return false;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Determine if this is an applet-type project
     * @return true if it is an applet
     */
    public boolean isApplet() {
        switch (this) {
            case EXTENDED_APPLET:
            case CLASSIC_APPLET:
                return true;
            case WEB:
            case EXTENSION_LIBRARY:
            case CLASSIC_LIBRARY:
                return false;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Determine if this is a library-type project
     * @return true if it is a library
     */
    public boolean isLibrary() {
        return !isApplet() && this != WEB;
    }

    /**
     * Get the path in the system filesystem where other modules could place
     * objects to include them in the projects' lookup
     * @return A path in the system filesystem
     */
    public String getLookupMergerPath() {
        return projectLayerPath() + "/Lookup";
    }

    private String projectLayerPath() {
        String val = "Projects/"; //NOI18N
        switch (this) {
            case WEB:
                return val + "org-netbeans-modules-javacard-webproject"; //NOI18N
            case CLASSIC_APPLET:
                return val + "org-netbeans-modules-javacard-capproject"; //NOI18N
            case EXTENDED_APPLET:
                return val + "org-netbeans-modules-javacard-eapproject"; //NOI18N
            case CLASSIC_LIBRARY:
                return val + "org-netbeans-modules-javacard-clslibproject"; //NOI18N
            case EXTENSION_LIBRARY:
                return val + "org-netbeans-modules-javacard-extlibproject"; //NOI18N
            default:
                throw new AssertionError();
        }
    }

    /**
     * System filesystem path for modules to place Node factories to include additional
     * nodes under this project
     * @return A path
     */
    public String nodeFactoryPath() {
        return projectLayerPath() + "/Nodes"; //NOI18N
    }

    /**
     * System fs path for other modules to add children to the Important Files subnode
     * @return A path
     */
    public String importantFilesPath() {
        return projectLayerPath() + "/ImportantFiles"; //NOI18N
    }

    /**
     * System fs path for other modules to add customizer panels
     * @return A path
     */
    public String customizerPath() {
        return projectLayerPath() + "/Customizer"; //NOI18N
    }
    
    /**
     * Determine what the project kind for a given template should be.  This should be
     * set via the attribute "projectType" on the fileobject and it should match one of
     * the names in this enum.
     * @param template The template
     * @return A ProjectKind
     * @throws IllegalArgumentException if the attribute is missing or unrecognized
     */
    public static ProjectKind kindForTemplate(FileObject template) {
        String attr = (String) template.getAttribute(FO_ATTR_PROJECT_TYPE); //NOI18N
        ProjectKind result = ProjectKind.valueOf(attr);
        if (result == null) {
            throw new IllegalArgumentException("Unknown template type " + //NOI18N
                    template.getPath());
        }
        return result;
    }

    /**
     * Look up a ProjectKind based on its toString() value as found in
     * project.xml (e.g. "org.netbeans.modules.javacard.webproject")
     * @param type The type as a string
     * @return a project kind, or null if unrecognized
     */
    public static ProjectKind forName(String type) {
        for (ProjectKind kind : ProjectKind.values()) {
            if (kind.toString().equals(type)) {
                return kind;
            }
        }
        return null;
    }

    public static ProjectKind kindForProject (FileObject projectXml) {
        try {
            if (projectXml != null) {
                InputStream in = new BufferedInputStream(projectXml.getInputStream());
                return fastKindForProject(in);
            }
        } catch (IOException ioe) {
            Logger.getLogger(ProjectKind.class.getName()).log(Level.INFO,
                    "Error parsing project.xml " + projectXml.getPath(), //NOI18N
                    ioe);
        }
        return null;

    }

    /**
     * Get the project kind based on an AntProjectHelper's data.  This may be
     * called before the project is fully initialized, so rather than fetch the
     * primary configuration data (which may be empty), this directly reads
     * the project.xml with a fast SAX parser.
     * <p>
     * Where there is an instance of the project in scope, it is preferable to
     * call JCProject.kind() rather than use this method
     *
     * @param helper An Ant project helper
     * @return A ProjectKind.
     */
    public static ProjectKind kindForProject(AntProjectHelper helper) {
        FileObject projectXml = helper.getProjectDirectory().getFileObject(
                AntProjectHelper.PROJECT_XML_PATH);
        return kindForProject (projectXml);
    }

    public static ProjectKind fastKindForProject(InputStream in) throws IOException {
        try {
            InputSource src = new InputSource(in);
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(new FastProjectKindDetector());
            reader.parse(src);
        } catch (SAXException e) {
            if (e instanceof FastProjectKindDetector.FoundProjectTypeException) {
                return forName(e.getMessage());
            } else {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
        } finally {
            in.close();
        }
        return null;
    }

    public static ProjectKind forManifestType (String manifestType) {
        for (ProjectKind k : values()) {
            if (manifestType.equals(k.getManifestApplicationType())) {
                return k;
            }
        }
        return null;
    }

    /**
     * Get the type of project as it should be referred to in the
     * manifest of projects of this type
     * @return
     */
    public String getManifestApplicationType() {
        switch (this) {
            case WEB:
                return "web"; //NOI18N
            case EXTENDED_APPLET:
                return "extended-applet"; //NOI18N
            case CLASSIC_APPLET:
                return "classic-applet"; //NOI18N
            case EXTENSION_LIBRARY:
                return "extension-lib"; //NOI18N
            case CLASSIC_LIBRARY:
                return "classic-lib"; //NOI18N
            default:
                throw new AssertionError();
        }
    }

    /**
     * Takes a comma-delimited string (may contain whitespace) of project
     * kinds as specified by the Java Card spec (<code>web, extended-applet,
     * classic-applet, extension-lib, classic-lib</code>), and returns a
     * set of corresponding ProjectKinds.
     *
     * @param s A string
     * @param returnAllIfEmpty Returns a set of all possible ProjectKinds if
     * the string is empty
     * @return
     */
    public static Set<ProjectKind> kindsFor(String s, boolean returnAllIfEmpty) {
        if (s == null || "".equals(s.trim())) { //NOI18N
            return returnAllIfEmpty ? new HashSet<ProjectKind>(Arrays.asList(ProjectKind.values())) :
                Collections.<ProjectKind>emptySet();
        }
        String[] els = s.split(","); //NOI18N
        Set<ProjectKind> result = new HashSet<ProjectKind>(5);
        boolean noMatch = true;
        for (String el : els) {
            el = el.trim();
            for (ProjectKind k : ProjectKind.values()) {
                if (k.getManifestApplicationType().equals(el)) {
                    result.add(k);
                    noMatch = false;
                }
            }
            if (noMatch) {
                Logger.getLogger(ProjectKind.class.getName()).log(Level.WARNING,
                        "Unrecognized project kind '" + el + "' in '" + s + "'",
                        new Exception());
            }
            noMatch = true;
        }
        return result;
    }

    public String getBundleFileExtension() {
        switch (this) {
            case WEB :
                return "war";
            case EXTENSION_LIBRARY:
            case EXTENDED_APPLET :
                return "eap";
            case CLASSIC_APPLET:
            case CLASSIC_LIBRARY:
                return "cap";
            default :
                throw new AssertionError();
        }
    }

    public static ProjectKind forJarFile (File jarFile) throws IOException {
        JarFile jf = new JarFile(jarFile);
        try {
            Manifest m = jf.getManifest();
            String appType = (String) m.getMainAttributes().get("Application-Type");
            if (appType == null) {
                return null;
            }
            return forManifestType(appType);
        } finally {
            jf.close();
        }
    }
}
