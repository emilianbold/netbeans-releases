/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.classpath.j2ee;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.api.common.classpath.ClassPathItem;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// will be moved to j2ee.common
/**
 *
 * @author Tomas Mysik
 */
public final class WebClassPathSupport extends BaseClassPathSupport<WebClassPathSupport.Item> {

    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE_ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; // NOI18N

    private static final String TAG_PATH_IN_WAR = "path-in-war"; // NOI18N
    private static final String TAG_FILE = "file"; // NOI18N
    private static final String TAG_LIBRARY = "library"; // NOI18N

    private final String[] projectConfigurationNameSpaceList;

    // XXX javadoc
    public static WebClassPathSupport create(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper, String[] wellKnownPaths, String libraryPrefix, String librarySuffix,
            String antArtifactPrefix, String projectConfigurationNameSpace, String[] projectConfigurationNameSpaceList) {
        Parameters.notNull("projectConfigurationNameSpaceList", projectConfigurationNameSpaceList);

        return new WebClassPathSupport(evaluator, referenceHelper, antProjectHelper, wellKnownPaths, libraryPrefix,
                librarySuffix, antArtifactPrefix, projectConfigurationNameSpace, projectConfigurationNameSpaceList);
    }

    private WebClassPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper, String[] wellKnownPaths, String libraryPrefix, String librarySuffix,
            String antArtifactPrefix, String projectConfigurationNameSpace,
            String[] projectConfigurationNameSpaceList) {
        super(evaluator, referenceHelper, antProjectHelper, wellKnownPaths, libraryPrefix, librarySuffix,
                antArtifactPrefix, projectConfigurationNameSpace);
        assert projectConfigurationNameSpaceList != null;

        this.projectConfigurationNameSpaceList = projectConfigurationNameSpaceList;
    }

    @Override
    public List<Item> itemsList(String propertyValue, String libraryElementName) {
        List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item> items =
                delegate.itemsList(propertyValue);

        // get the list of items which are included in deployment
        Map<String, String> warMap = null;
        if (libraryElementName != null) {
            warMap = createWarIncludesMap(antProjectHelper, libraryElementName);
        } else {
            warMap = Collections.<String, String>emptyMap();
        }

        List<Item> result = new ArrayList<Item>(items.size());
        for (org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item item : items) {
            result.add(createItem(item, warMap));
        }
        return result;
    }

    @Override
    public String[] encodeToStrings(List<Item> items, String libraryElementName) {
        List<String> includedLibraries = new ArrayList<String>();

        List<String> result = new ArrayList<String>();
        for (Item item : items) {
            String reference = delegate.getReference(item.delegate);
            if (reference != null) {
                result.add(reference);
                item.setReference(reference);

                if (libraryElementName != null) {
                    includedLibraries.add(CommonProjectUtils.getAntPropertyName(reference));
                }
            }
        }

        if (libraryElementName != null) {
            List<Item> cp = new ArrayList<Item>(items);
            putIncludedLibraries(includedLibraries, cp, antProjectHelper, libraryElementName);
        }

        // XXX create util method in Strings (core j2ee utilities)
        String[] strings = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            if (i < result.size() - 1) {
                strings[i] = result.get(i) + ":"; //NOI18N
            } else  {
                strings[i] = result.get(i);
            }
        }
        return strings;
    }

    private Item createItem(final org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate,
            final Map<String, String> warMap) {
        Item item = null;
        switch (delegate.getType()) {
            case CLASSPATH:
                item = new Item(delegate, null, null, delegate.getReference(), null);
                break;
            case LIBRARY:
            case ARTIFACT:
            case JAR:
                String property = delegate.getReference();
                item = new Item(delegate, null, null, property, warMap.get(property));
                break;
            default:
                assert false : "Unknown classpath item type for " + delegate;
                break;
        }
        return item;
    }

    private Map<String, String> createWarIncludesMap(AntProjectHelper uh, String libraryElementName) {
        Map<String, String> warIncludesMap = new LinkedHashMap<String, String>();
        //try all supported namespaces starting with the newest one
        for (int idx = projectConfigurationNameSpaceList.length - 1; idx >= 0; idx--) {
            String ns = projectConfigurationNameSpaceList[idx];
            Element data = uh.createAuxiliaryConfiguration().getConfigurationFragment("data", ns, true); // NOI18N
            if (data != null) {
                Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, libraryElementName).item(0);
                if (webModuleLibs != null) {
                    NodeList ch = webModuleLibs.getChildNodes();
                    for (int i = 0; i < ch.getLength(); i++) {
                        if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element library = (Element) ch.item(i);
                            Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                            NodeList pathInWarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_WAR);
                            // remove ${ and } from the beginning and end
                            String webFileText = findText(webFile);
                            webFileText = webFileText.substring(2, webFileText.length() - 1);

                            //#86522
                            if (libraryElementName.equals(TAG_WEB_MODULE_ADDITIONAL_LIBRARIES)) {
                                String pathInWar = Item.PATH_IN_WAR_NONE;
                                if (pathInWarElements.getLength() > 0) {
                                    pathInWar = findText((Element) pathInWarElements.item(0));
                                    if (pathInWar == null) {
                                        pathInWar = Item.PATH_IN_WAR_APPLET;
                                    }
                                }
                                warIncludesMap.put(webFileText, pathInWar);
                            } else {
                                String value = null;
                                if (pathInWarElements.getLength() > 0) {
                                    value = findText(pathInWarElements.item(0));
                                } else {
                                    value = Item.PATH_IN_WAR_NONE;
                                }
                                warIncludesMap.put(webFileText, value);
                            }
                        }
                    }
                    return warIncludesMap;
                }
            }
        }
        if (warIncludesMap.isEmpty()) {
            return Collections.<String, String>emptyMap();
        }
        return warIncludesMap;
    }

    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private void putIncludedLibraries(List<String> libraries, List<Item> classpath,
            AntProjectHelper antProjectHelper, String libraryElementName) {
        assert libraries != null;
        assert antProjectHelper != null;
        assert libraryElementName != null;

        Element data = antProjectHelper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element webModuleLibs = (Element) data.getElementsByTagNameNS(
                projectConfigurationNameSpace, libraryElementName).item(0);
        if (webModuleLibs == null) {
            webModuleLibs = doc.createElementNS(projectConfigurationNameSpace, libraryElementName);
            data.appendChild(webModuleLibs);
        }
        while (webModuleLibs.hasChildNodes()) {
            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
        }

        Iterator<Item> cp = classpath.iterator();
        for (String library : libraries) {
            webModuleLibs.appendChild(createLibraryElement(doc, library, cp.next()));
        }
        antProjectHelper.putPrimaryConfigurationData(data, true);
    }

    private Element createLibraryElement(Document doc, String pathItem, Item item) {
        Element libraryElement = doc.createElementNS(projectConfigurationNameSpace, TAG_LIBRARY);
        List<File> files = new ArrayList<File>();
        List<File> dirs = new ArrayList<File>();
        getFilesForItem(item, files, dirs);
        if (files.size() > 0) {
            libraryElement.setAttribute(ATTR_FILES, "" + files.size());
        }
        if (dirs.size() > 0) {
            libraryElement.setAttribute(ATTR_DIRS, "" + dirs.size());
        }
        Element webFile = doc.createElementNS(projectConfigurationNameSpace, TAG_FILE);
        libraryElement.appendChild(webFile);
        webFile.appendChild(doc.createTextNode("${" + pathItem + "}")); // NOI18N
        if (item.getPathInWAR() != null) {
            Element pathInWar = doc.createElementNS(projectConfigurationNameSpace, TAG_PATH_IN_WAR);
            pathInWar.appendChild(doc.createTextNode(item.getPathInWAR()));
            libraryElement.appendChild(pathInWar);
        }
        return libraryElement;
    }

    public static final class Item extends BaseClassPathSupport.Item {
        public static final String PATH_IN_WAR_LIB = "WEB-INF/lib"; // NOI18N
        public static final String PATH_IN_WAR_DIR = "WEB-INF/classes"; // NOI18N
        public static final String PATH_IN_WAR_APPLET = ""; // NOI18N
        public static final String PATH_IN_WAR_NONE = null;

        private String pathInWar;
        private String eval;

        Item(org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate, String raw, String eval,
                String property, String pathInWar) {
            super(delegate, property, raw);
            this.pathInWar = pathInWar;
            this.eval = eval;
        }

        public static Item create(Library library, String property, String pathInWar) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(library, property);
            String libraryName = library.getName();
            return new Item(delegate, "${libs." + libraryName + ".classpath}", libraryName, property, pathInWar); // NOI18N
        }

        public static Item create(AntArtifact artifact, URI artifactURI, String property, String pathInWar) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(artifact, artifactURI, property);
            return new Item(delegate, null, artifact.getArtifactLocations()[0].toString(), property, pathInWar);
        }

        public static Item create(File file, String property, String pathInWar) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(file, property);
            return new Item(delegate, null, file.getPath(), property, pathInWar);
        }

        public static Item create(String property, String pathInWar) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(property);
            return new Item(delegate, null, null, property, pathInWar);
        }

        public static Item createBroken(ClassPathItem.Type type, String property, String pathInWar) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.createBroken(type, property);
            return new Item(delegate, null, null, property, pathInWar);
        }

        public static Item createBroken(File file, String property, String pathInWar) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.createBroken(file, property);
            return new Item(delegate, null, null, property, pathInWar);
        }

        public boolean canDelete() {
            return getType() != ClassPathItem.Type.CLASSPATH;
        }

        public String getPathInWAR() {
            return pathInWar;
        }

        public void setPathInWAR(String pathInWar) {
            this.pathInWar = pathInWar;
        }

        public String getEvaluated() {
            if (eval == null) {
                return getRaw();
            }
            return eval;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(delegate.toString());
            sb.append(", raw = "); // NOI18N
            sb.append(getRaw());
            sb.append(", eval = "); // NOI18N
            sb.append(eval);
            sb.append(", pathInWar = "); // NOI18N
            sb.append(pathInWar);
            return sb.toString();
        }
    }
}
