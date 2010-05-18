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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.netbeans.modules.visualweb.complib.Complib.InitialPaletteFolder;
import org.netbeans.modules.visualweb.complib.Complib.InitialPaletteItem;
import org.netbeans.modules.visualweb.complib.api.ComplibException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents configuration information for a component library.
 * 
 * @author Edwin Goei
 */
class ComplibManifest {

    static final String MISSING = "Missing attribute: "; // NOI18N

    static final String PREFIX = "X-Rave-"; // NOI18N

    private static final String MISSING_RESOURCE = "Missing resource: "; // NOI18N

    private static final String API_COMPAT_VERSION = PREFIX + "API-Compatibility-Version"; // NOI18N

    private static final Version VERSION_1_0_0 = new Version(1, 0, 0);

    private static final Version VERSION_2_0_0 = new Version(2, 0, 0);

    private static final String CONFIGURATION = PREFIX + "Complib-Configuration"; // NOI18N

    private static final String ELM_ROOT = "complibConfiguration"; // NOI18N

    private static final String ELM_DESIGN_TIME_PATH = "designTimePath";

    private static final String ATT_RESOURCE_BUNDLE = "resourceBundleBaseName"; // NOI18N

    /** Used to look in META-INF/ as a fall back */
    private static final String RESOURCE_BUNDLE_PREFIX = "META-INF.";

    /** XPath constants begin with XP_ */

    private static final String XP_PREFIX = "/" + ELM_ROOT + "/"; // NOI18N

    private static final String XP_CONFIG_VERSION = XP_PREFIX + "@version"; // NOI18N

    private static final String XP_IDENTIFIER_URI = XP_PREFIX + "identifier/uri/text()"; // NOI18N

    private static final String XP_IDENTIFIER_VERSION = XP_PREFIX + "identifier/version/text()"; // NOI18N

    private static final String XP_TITLE_KEY = XP_PREFIX + "titleKey/text()"; // NOI18N

    private static final String XP_RUNTIME_PATH = XP_PREFIX + "runtimePath"; // NOI18N

    private static final String XP_DESIGN_TIME_PATH = XP_PREFIX + ELM_DESIGN_TIME_PATH; // NOI18N

    private static final String XP_JAVADOC_PATH = XP_PREFIX + "javadocPath"; // NOI18N

    private static final String XP_SOURCE_PATH = XP_PREFIX + "sourcePath"; // NOI18N

    private static final String XP_WEB_RESOURCE_PATH = XP_PREFIX + "webResourcePath"; // NOI18N

    private static final String XP_HELP_PATH = XP_PREFIX + "helpPath"; // NOI18N

    private static final String XP_HELP_PREFIX = XP_HELP_PATH + "/@helpPrefix"; // NOI18N

    /** @since NetBeans Visual Web 6 */
    private static final String XP_HELP_SET_FILE = XP_HELP_PATH + "/@helpSetFile"; // NOI18N

    private static final String XP_INITIAL_PALETTE_FOLDER = XP_PREFIX + "initialPalette/folder"; // NOI18N

    private static final String XP_PATH_ELEMENT = "pathElement"; // NOI18N

    private static final String XP_EE_SPEC_VERSION = XP_PREFIX + "eeSpecification/@version"; // NOI18N

    private static final String XP_SUN_FACES_CONFIG = XP_PREFIX + "sunFacesConfig/text()"; // NOI18N

    private static final String ATT_CLASS_NAME = "className"; // NOI18N

    private static final String ELM_ITEM = "item"; // NOI18N

    private static final String ATT_KEY = "key"; // NOI18N

    private static final String REQUIRES = "Complib configuration requires: "; // NOI18N

    private static final String BAD_VALUE = "Complib configuration has invalid value for: "; // NOI18N

    private static String complibConfigAttr;

    /** complibConfiguration XML document */
    private Document doc;

    /**
     * Resource bundle used to localize metadata for this complib package. Use
     * this.getResource(String) to get localized messages.
     */
    private ResourceBundle rb;

    private static final EmptyResourceBundle emptyResourceBundle = new EmptyResourceBundle();

    private ArrayList<InitialPaletteFolder> topFolders = new ArrayList<InitialPaletteFolder>();

    /** Identifier is unique among all installed libraries */
    protected Complib.Identifier identifier;

    /** Localized Title for this complib */
    protected String title;

    /** Runtime path, each String is a relative complib resource path */
    protected List<String> runtimePath;

    /** Design-time path, each String is a relative complib resource path */
    protected List<String> declaredDesignTimePath;

    /** True iff runtime path should be prepended to design-time path */
    protected boolean prependRuntimePath;

    /** Javadoc path, each String is a relative complib resource path */
    protected List<String> javadocPath;

    /** Java source path, each String is a relative complib resource path */
    protected List<String> sourcePath;

    /** Web resource path, each String is a relative complib resource path */
    private List<String> webResourcePath;

    /** Help path, each String is a relative complib resource path */
    private List<String> helpPath;

    /** Help prefix that is associated with the help path */
    private String helpPrefix;

    /**
     * "/" separated path to HelpSet file relative to helpPath. eg. "help/my-help.hs"
     */
    private String helpSetFile;

    /**
     * Relative complib resource path to a sun-faces-config.xml type code generator input file
     */
    private String sunFacesConfig;

    public enum EeSpecVersion {
        J2EE_1_4, JAVA_EE_5
    };

    /** Java EE spec version that this complib depends upon */
    private EeSpecVersion eeSpecVersion;

    private static XmlUtil configXmlUtil;

    private static final List<String> EMPTY_LIST = Collections.emptyList();

    /**
     * Represents legacy component library configuration info that was in Reef, AKA Rave version
     * 1.x.
     * 
     * @author Edwin Goei
     */
    static class LegacyManifest extends ComplibManifest {
        /** The ordered pair (Library-URI, Library-Version) must be unique */
        private static final String LIBRARY_URI = PREFIX + "Library-URI"; // NOI18N

        /** The ordered pair (Library-URI, Library-Version) must be unique */
        private static final String LIBRARY_VERSION = PREFIX + "Library-Version"; // NOI18N

        /** Library-Title used as a user-friendly name which may not be unique */
        private static final String LIBRARY_TITLE = PREFIX + "Library-Title"; // NOI18N

        private static final String RUNTIME_PATH = PREFIX + "Runtime-Path"; // NOI18N

        private static final String DESIGN_TIME_PATH = PREFIX + "Design-Time-Path"; // NOI18N

        private static final String JAVADOC_PATH = PREFIX + "Javadoc-Path"; // NOI18N

        private static final String SOURCE_PATH = PREFIX + "Source-Path"; // NOI18N

        /**
         * Parse legacy version 1.0 complib configuration
         * 
         * @param attrs
         *            complib manifest attributes
         * @throws ComplibException
         */
        public LegacyManifest(Attributes attrs) throws ComplibException {

            // Library URI is required
            String valLibraryUri = attrs.getValue(LIBRARY_URI);
            if (valLibraryUri == null) {
                throw new ManifestAttributeException(MISSING + LIBRARY_URI);
            }

            // Library Version is required
            String valLibraryVersion = attrs.getValue(LIBRARY_VERSION);
            if (valLibraryVersion == null) {
                throw new ManifestAttributeException(MISSING + LIBRARY_VERSION);
            }
            this.identifier = new Complib.Identifier(valLibraryUri, valLibraryVersion);

            String val;

            // Library Title is required
            val = attrs.getValue(LIBRARY_TITLE);
            if (val == null) {
                throw new ManifestAttributeException(MISSING + LIBRARY_TITLE);
            }
            this.title = val;

            // At least one Runtime jar is required
            val = attrs.getValue(RUNTIME_PATH);
            if (val == null || "".equals(val.trim())) {
                throw new ManifestAttributeException(MISSING + RUNTIME_PATH);
            }
            this.runtimePath = splitPath(val);

            // At least one Design-time jar is required
            val = attrs.getValue(DESIGN_TIME_PATH);
            if (val == null || "".equals(val.trim())) {
                throw new ManifestAttributeException(MISSING + DESIGN_TIME_PATH);
            }
            this.declaredDesignTimePath = splitPath(val);

            ArrayList<String> al = new ArrayList<String>(runtimePath);
            al.addAll(splitPath(val));

            // Javadoc will be added to library reference, if any
            this.javadocPath = splitPath(attrs.getValue(JAVADOC_PATH));

            // Source will be added to library reference, if any
            this.sourcePath = splitPath(attrs.getValue(SOURCE_PATH));
        }

        /**
         * Split a path string using whitespace as a delimiter
         * 
         * @param path
         *            whitespace separated path
         * @return List of String-s, possibly empty, never null
         */
        private List<String> splitPath(String path) {
            if (path == null) {
                return new ArrayList<String>(0);
            }
            String[] parts = path.split("\\s"); // NOI18N
            return Arrays.asList(parts);
        }
    }

    /**
     * When no resource bundle is used, use a fake empty resource bundle so we don't have to special
     * case for null later
     */
    private static class EmptyResourceBundle extends ListResourceBundle {

        private static final Object[][] contents = new Object[0][];

        protected Object[][] getContents() {
            return contents;
        }

    }

    /**
     * Parse the Jar Manifest and return a CompLibManifest. This can occur before a package is
     * expanded. This needs to be fast since a UI may depend on accessing some metadata about this
     * library. Only some info such as the library Title is accessible before a package is expanded.
     * 
     * @param manifest
     *            root jar Manifest which may refer to external resources
     * @param resourceClassLoader
     *            ClassLoader used to load resources from
     * @return
     * @throws ComplibException
     * @throws IOException
     */
    static ComplibManifest getInstance(Manifest manifest, ClassLoader resourceClassLoader)
            throws ComplibException, IOException {
        // API Version attribute is required
        Attributes attrs = manifest.getMainAttributes();

        String apiVersionString = attrs.getValue(API_COMPAT_VERSION);
        if (apiVersionString == null) {
            throw new ManifestAttributeException(MISSING + API_COMPAT_VERSION);
        }
        Version apiVersion = new Version(apiVersionString);

        ComplibManifest compLibConfig;
        if (VERSION_2_0_0.equals(apiVersion)) {
            complibConfigAttr = attrs.getValue(CONFIGURATION);
            if (complibConfigAttr == null) {
                throw new ManifestAttributeException(MISSING + CONFIGURATION);
            }
            URL complibConfUrl = resourceClassLoader.getResource(complibConfigAttr);
            if (complibConfUrl == null) {
                throw new ComplibException(MISSING_RESOURCE + complibConfigAttr);
            }

            configXmlUtil = new XmlUtil();
            Document configDoc = configXmlUtil.read(complibConfUrl);
            compLibConfig = new ComplibManifest(configDoc, resourceClassLoader);
        } else if (VERSION_1_0_0.equals(apiVersion)) {
            compLibConfig = new ComplibManifest.LegacyManifest(attrs);
        } else {
            throw new ManifestAttributeException(API_COMPAT_VERSION + " must be " + VERSION_2_0_0
                    + " or " + VERSION_1_0_0); // NOI18N
        }

        return compLibConfig;
    }

    static ComplibManifest getInstance(URL configUrl, ClassLoader resourceClassLoader)
            throws ComplibException, XmlException {
        // TODO what about closing the doc??
        Document configDoc = new XmlUtil().read(configUrl);
        return new ComplibManifest(configDoc, resourceClassLoader);
    }

    private ComplibManifest() {
        // No-op. This default constructor is to allow subclassing.
    }

    /**
     * @param doc
     *            DOM that represents complib configuration metadata
     * @param resourceClassLoader
     *            ClassLoader to load localized resources used for example for folder names
     * @throws XmlException
     * @throws ComplibException
     */
    private ComplibManifest(Document doc, ClassLoader resourceClassLoader) throws XmlException,
            ComplibException {
        this.doc = doc;

        Element root = doc.getDocumentElement();
        if (!ELM_ROOT.equals(root.getNodeName())) {
            throw new ComplibException("Complib configuration root element must be '" // NOI18N
                    + ELM_ROOT + "'"); // NOI18N
        }

        // Schema version of complib config file
        Attr versionAttr = (Attr) XmlUtil.selectSingleNode(doc, XP_CONFIG_VERSION);
        String versionString = versionAttr.getValue();
        int version;
        if ("1.0".equals(versionString)) {
            version = 1;
        } else if ("1.1".equals(versionString)) {
            // VWP 5.5, Shortfin
            version = 11;
        } else if ("1.2".equals(versionString)) {
            // NetBeans 6 Visual Web, Longfin
            version = 12;
        } else {
            throw new ComplibException(
                    "Complib configuration root element @version must be '1.0' or '1.1' or '1.2'"); // NOI18N
        }

        initResourceBundle(root, resourceClassLoader);

        // Library URI is required
        String libraryUri = getRequiredTextNode(XP_IDENTIFIER_URI);
        // Library Version is required
        String libraryVersion = getRequiredTextNode(XP_IDENTIFIER_VERSION);
        this.identifier = new Complib.Identifier(libraryUri, libraryVersion);

        // Library Title is required
        String titleKey = getRequiredTextNode(XP_TITLE_KEY);
        this.title = getResource(titleKey);

        // At least one Runtime jar is required
        this.runtimePath = getRequiredConfigPath(XP_RUNTIME_PATH);

        // Design-time jars, typically at least one, but no longer required
        this.declaredDesignTimePath = getConfigPath(XP_DESIGN_TIME_PATH);

        // Javadoc will be added to library reference, if any
        this.javadocPath = getConfigPath(XP_JAVADOC_PATH);

        // Source will be added to library reference, if any
        this.sourcePath = getConfigPath(XP_SOURCE_PATH);

        // Resources will be unpacked and merged with project web resources
        this.webResourcePath = getConfigPath(XP_WEB_RESOURCE_PATH);

        // Help will be merged with IDE help
        Attr attr = (Attr) XmlUtil.selectSingleNode(doc, XP_HELP_PREFIX);
        if (attr != null) {
            this.helpPrefix = attr.getValue();
        }
        attr = (Attr) XmlUtil.selectSingleNode(doc, XP_HELP_SET_FILE);
        if (attr != null) {
            this.helpSetFile = attr.getValue();
        }
        this.helpPath = getConfigPath(XP_HELP_PATH);

        // Init the optional initial palette structure data
        initPaletteData();

        // Default value is J2EE 1.4 for backward compatibility before VWP 5.5
        eeSpecVersion = EeSpecVersion.J2EE_1_4;
        // Value of null means not specified so use default
        attr = (Attr) XmlUtil.selectSingleNode(doc, XP_EE_SPEC_VERSION);
        String stringVal = attr == null ? null : attr.getValue();
        // Complibs before VWP 5.5, will not contain this attribute
        if (stringVal != null) {
            if ("5".equals(stringVal)) {
                eeSpecVersion = EeSpecVersion.JAVA_EE_5;
            } else if (version > 11 && !"1.4".equals(stringVal)) {
                // If VWP 5.5 and later, enforce fixed set of values
                throw new ComplibException(BAD_VALUE + XP_EE_SPEC_VERSION);
            }
        }
        // Value of null means not specified
        this.sunFacesConfig = getOptionalTextNode(XP_SUN_FACES_CONFIG, null);
    }

    /**
     * @return
     * @throws XmlException
     * @throws ComplibException
     *             if required text node does not exist
     */
    private String getRequiredTextNode(String xpath) throws XmlException, ComplibException {
        Text text = (Text) XmlUtil.selectSingleNode(doc, xpath);
        if (text == null) {
            throw new ComplibException(REQUIRES + xpath);
        }
        return text.getNodeValue().trim();
    }

    private String getOptionalTextNode(String xpath, String defaultVal) throws XmlException {
        Text text = (Text) XmlUtil.selectSingleNode(doc, xpath);
        if (text == null) {
            return defaultVal;
        }
        return text.getNodeValue().trim();
    }

    /**
     * Same as getConfigPath(String) but it requires at least one path element
     * 
     * @param xpath
     * @return
     * @throws XmlException
     * @throws ComplibException
     *             if no path elements are found
     */
    private List<String> getRequiredConfigPath(String xpath) throws XmlException, ComplibException {
        List<String> path = getConfigPath(xpath);
        if (path.isEmpty()) {
            throw new ComplibException(REQUIRES + xpath);
        }
        return path;
    }

    /**
     * Return a List of path elements within a path specified in the config
     * 
     * @param xpath
     *            parent element containing path elements
     * @return List of path elements which may be empty but never null
     * @throws XmlException
     */
    private List<String> getConfigPath(String xpath) throws XmlException {
        ArrayList<String> path = new ArrayList<String>();
        Element elm = (Element) XmlUtil.selectSingleNode(doc, xpath);
        if (elm == null) {
            // Empty path
            return path;
        }

        // Iterate through all XP_PATH_ELEMENT children
        NodeList nl = XmlUtil.selectNodeList(elm, XP_PATH_ELEMENT);
        for (int i = 0; i < nl.getLength(); i++) {
            Element pathElmElm = (Element) nl.item(i);
            Text pathElmText = (Text) XmlUtil.selectSingleNode(pathElmElm, "text()"); // NOI18N
            if (pathElmText != null) {
                String val = pathElmText.getNodeValue();
                if ("" != val.trim()) {
                    path.add(val);
                }
            }
        }
        return path;
    }

    private boolean getBoolean(String xpath, boolean defaultValue) throws XmlException {
        Attr attr = (Attr) XmlUtil.selectSingleNode(doc, xpath);
        if (attr == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(attr.getValue().trim());
    }

    private void initPaletteData() throws XmlException {
        // Iterate through top level folders
        NodeList nl = XmlUtil.selectNodeList(doc, XP_INITIAL_PALETTE_FOLDER);
        for (int i = 0; i < nl.getLength(); i++) {
            Element elm = (Element) nl.item(i);
            InitialPaletteFolder topFolder = createFolderRecurse(elm);
            topFolders.add(topFolder);
        }
    }

    /**
     * @param folderElement
     * @return
     * @throws XmlException
     */
    private InitialPaletteFolder createFolderRecurse(Element folderElement) throws XmlException {
        // Derive the localized name
        String key = folderElement.getAttribute(ATT_KEY);
        InitialPaletteFolder folder = new InitialPaletteFolder(getResource(key));

        // Iterate through all Element child nodes
        NodeList nl = XmlUtil.selectNodeList(folderElement, "*"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            Element elm = (Element) nl.item(i);
            String tagName = elm.getNodeName();
            if (ELM_ITEM.equals(tagName)) {
                InitialPaletteItem child = createItemRecurse(elm);
                folder.appendChild(child);
            } else {
                // This also disallows a folder from containing another folder
                IdeUtil.logWarning(new IllegalArgumentException(
                        "Skipping invalid element in initial palette: " // NOI18N
                                + tagName));
            }
        }
        return folder;
    }

    private InitialPaletteItem createItemRecurse(Element itemElement) throws XmlException {
        // Derive the localized name
        String className = itemElement.getAttribute(ATT_CLASS_NAME);
        InitialPaletteItem item = new InitialPaletteItem(className);

        // Iterate through all Element child nodes
        NodeList nl = XmlUtil.selectNodeList(itemElement, "*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element elm = (Element) nl.item(i);
            String tagName = elm.getNodeName();
            if (ELM_ITEM.equals(tagName)) {
                InitialPaletteItem child = createItemRecurse(elm);
                item.appendChild(child);
            } else {
                IdeUtil.logWarning(new IllegalArgumentException(
                        "Skipping invalid element in initial palette: " // NOI18N
                                + tagName));
            }
        }
        return item;
    }

    /**
     * Init ResourceBundle baseName which is optional
     * 
     * @param complibConfigurationElm
     * @param resourceClassLoader
     */
    private void initResourceBundle(Element complibConfigurationElm, ClassLoader resourceClassLoader) {
        String rbBaseName = complibConfigurationElm.getAttribute(ATT_RESOURCE_BUNDLE);
        if (rbBaseName.length() != 0) {
            // Attribute was specified or defaulted
            try {
                /*
                 * As a workaround for the NB ProxyClassLoader warning, look for the bundle under
                 * META-INF first and then go look starting at the top level as usual.
                 * 
                 * @since NetBeans 6 Visual Web
                 */
                if (rbBaseName.indexOf(".") == -1) {
                    try {
                        rb = ResourceBundle.getBundle(RESOURCE_BUNDLE_PREFIX + rbBaseName, Locale
                                .getDefault(), resourceClassLoader);
                    } catch (MissingResourceException mre) {
                        rb = ResourceBundle.getBundle(rbBaseName, Locale.getDefault(),
                                resourceClassLoader);
                    }
                } else {
                    rb = ResourceBundle.getBundle(rbBaseName, Locale.getDefault(),
                            resourceClassLoader);
                }
            } catch (RuntimeException e) {
                // Fallback: warn user and default to using no resource bundle
                IdeUtil.logWarning(e);
                rb = emptyResourceBundle;
            }
        } else {
            // Assume no localization was desired.
            rb = emptyResourceBundle;
        }
    }

    /**
     * Return a value from a resource bundle or the key itself if this is not possible.
     * 
     * @param key
     * @return value correspinding to key or key itself
     */
    private String getResource(String key) {
        try {
            String value = rb.getString(key);
            return value;
        } catch (RuntimeException e) {
            if (rb != emptyResourceBundle) {
                // Warn if we're using a user supplied resource bundle
                IdeUtil.logWarning(e);
            }
            // Use the key itself as the value
            return key;
        }
    }

    /**
     * Returns a List<InitialPaletteFolder> representing the initial palette structure for this
     * complib, if any. If the list is empty, then this complib does not have an initial palette
     * specified.
     * 
     * @return List<InitialPaletteFolder> which may be empty but never null.
     */
    List<InitialPaletteFolder> getInitialPalette() {
        return topFolders;
    }

    List<String> getDeclaredDesignTimePath() {
        return declaredDesignTimePath;
    }

    List<String> getHelpPath() {
        return helpPath == null ? EMPTY_LIST : helpPath;
    }

    String getHelpSetFile() {
        return helpSetFile;
    }

    /**
     * Return the help prefix. Null means no prefix attribute found.
     * 
     * @return
     */
    String getHelpPrefix() {
        return helpPrefix;
    }

    Complib.Identifier getIdentifier() {
        return identifier;
    }

    List<String> getJavadocPath() {
        return javadocPath == null ? EMPTY_LIST : javadocPath;
    }

    List<String> getRuntimePath() {
        return runtimePath;
    }

    List<String> getSourcePath() {
        return sourcePath == null ? EMPTY_LIST : sourcePath;
    }

    String getTitle() {
        return title;
    }

    List<String> getWebResourcePath() {
        return webResourcePath == null ? EMPTY_LIST : webResourcePath;
    }

    public EeSpecVersion getEeSpecVersion() {
        return eeSpecVersion;
    }

    /**
     * @return null if not specified
     */
    String getSunFacesConfig() {
        return sunFacesConfig;
    }

    /**
     * Append a single design-time jar element to the declared design-time path
     * 
     * @param dtJar
     * @throws XmlException
     */
    void addDeclaredDesignTimePath(File dtJar) throws XmlException {
        // TODO this just picks the last component of path
        String dtJarText = dtJar.getName();
        declaredDesignTimePath.add(dtJarText);

        // Persist it in DOM
        Element dtPathElm = (Element) XmlUtil.selectSingleNode(doc, XP_DESIGN_TIME_PATH);
        if (dtPathElm == null) {
            // No DT path element so create it and insert it

            dtPathElm = doc.createElement(ELM_DESIGN_TIME_PATH);
            Element rtPathElm = (Element) XmlUtil.selectSingleNode(doc, XP_RUNTIME_PATH);
            Element parentElm = (Element) rtPathElm.getParentNode();
            Node insertNode = rtPathElm.getNextSibling();
            parentElm.insertBefore(dtPathElm, insertNode);
        }

        Text textNode = doc.createTextNode(dtJarText);
        Element pathElementElm = doc.createElement(XP_PATH_ELEMENT);
        pathElementElm.appendChild(textNode);
        dtPathElm.appendChild(pathElementElm);

    }

    /**
     * @param absoluteLibDir
     * @throws XmlException
     */
    void saveTo(File absoluteLibDir) throws XmlException {
        File destConfig = new File(absoluteLibDir, complibConfigAttr);
        configXmlUtil.write(destConfig);
    }
}
