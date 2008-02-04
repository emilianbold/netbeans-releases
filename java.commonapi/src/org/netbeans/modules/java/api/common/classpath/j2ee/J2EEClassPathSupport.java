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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.api.common.classpath.ClassPathItem;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

// will be moved to j2ee.common
/**
 *
 * @author Tomas Mysik
 * @since 1.21
 */
public final class J2EEClassPathSupport extends BaseClassPathSupport<J2EEClassPathSupport.Item> {

    private final String[] elementsOrder;

    // XXX javadoc
    public static J2EEClassPathSupport create(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper, String[] wellKnownPaths, String libraryPrefix, String librarySuffix,
            String antArtifactPrefix, String projectConfigurationNameSpace, String[] elementsOrder) {
        return new J2EEClassPathSupport(evaluator, referenceHelper, antProjectHelper, wellKnownPaths, libraryPrefix,
                librarySuffix, antArtifactPrefix, projectConfigurationNameSpace, elementsOrder);
    }

    private J2EEClassPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper, String[] wellKnownPaths, String libraryPrefix, String librarySuffix,
            String antArtifactPrefix, String projectConfigurationNameSpace, String[] elementsOrder) {
        super(evaluator, referenceHelper, antProjectHelper, wellKnownPaths, libraryPrefix, librarySuffix,
                antArtifactPrefix, projectConfigurationNameSpace);

        this.elementsOrder = elementsOrder;
    }

    @Override
    public List<Item> itemsList(String propertyValue, String libraryElementName) {
        List<org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item> items =
                delegate.itemsList(propertyValue);

        // get the list of items which are included in deployment
        List<String> includedItems = null;
        if (libraryElementName != null) {
            includedItems = getIncludedLibraries(antProjectHelper, libraryElementName);
        } else {
            includedItems = Collections.<String>emptyList();
        }

        List<Item> result = new ArrayList<Item>(items.size());
        for (org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item item : items) {
            result.add(createItem(item, includedItems));
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

                // add the item to the list of items included in deployment
                if (libraryElementName != null && item.isIncludedInDeployment()) {
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
            final List<String> includedItems) {
        Item item = null;
        switch (delegate.getType()) {
            case CLASSPATH:
                item = new Item(delegate, delegate.getReference(), false, null);
                break;
            case LIBRARY:
            case ARTIFACT:
            case JAR:
                String property = delegate.getReference();
                item = new Item(delegate, property, includedItems.contains(property), null);
                break;
            default:
                assert false : "Unknown classpath item type for " + delegate;
                break;
        }
        return item;
    }

    /**
     * Returns a list with the classpath items which are to be included
     * in deployment.
     */
    private List<String> getIncludedLibraries(AntProjectHelper antProjectHelper, String libraryElementName) {
        assert antProjectHelper != null;
        assert libraryElementName != null;

        Element data = antProjectHelper.getPrimaryConfigurationData(true);
        NodeList libs = data.getElementsByTagNameNS(projectConfigurationNameSpace, libraryElementName);
        List<String> libraries = new ArrayList<String>(libs.getLength());
        for (int i = 0; i < libs.getLength(); i++) {
            Element item = (Element) libs.item(i);
            libraries.add(findText(item));
        }
        return libraries;
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
        NodeList libs = data.getElementsByTagNameNS(projectConfigurationNameSpace, libraryElementName);
        while (libs.getLength() > 0) {
            Node n = libs.item(0);
            n.getParentNode().removeChild(n);
        }

        Document doc = data.getOwnerDocument();
        for (String libraryName : libraries) {
            // find a correcponding classpath item for the library
            for (Item item : classpath) {
                String libraryPropName = "${" + libraryName + "}"; // NOI18N
                if (libraryPropName.equals(item.getReference())) {
                    Element element = createLibraryElement(doc, libraryName, item, libraryElementName);
                    if (elementsOrder != null) {
                        appendChildElement(data, element, elementsOrder);
                    } else {
                        data.appendChild(element);
                    }
                }
            }
        }
        antProjectHelper.putPrimaryConfigurationData(data, true);
    }

    /**
     * Find all direct child elements of an element.
     * More useful than {@link Element#getElementsByTagNameNS} because it does
     * not recurse into recursive child elements.
     * Children which are all-whitespace text nodes are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     */
    private static List<Element> findSubElements(Element parent) {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text) n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text);
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // skip
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n);
            }
        }
        return elements;
    }

    /**
     * Append child element to the correct position according to given
     * order.
     * @param parent parent to which the child will be added
     * @param el element to be added
     * @param order order of the elements which must be followed
     */
    private static void appendChildElement(Element parent, Element el, String[] order) {
        Element insertBefore = null;
        List l = Arrays.asList(order);
        int index = l.indexOf(el.getLocalName());
        assert index != -1 : el.getLocalName() + " was not found in " + l;
        Iterator it = findSubElements(parent).iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            int index2 = l.indexOf(e.getLocalName());
            assert index2 != -1 : e.getLocalName() + " was not found in " + l;
            if (index2 > index) {
                insertBefore = e;
                break;
            }
        }
        parent.insertBefore(el, insertBefore);
    }

    private Element createLibraryElement(Document doc, String pathItem, Item item, String libraryElementName) {
        Element libraryElement = doc.createElementNS(projectConfigurationNameSpace, libraryElementName);
        List<File> files = new ArrayList<File>();
        List<File> dirs = new ArrayList<File>();
        getFilesForItem(item, files, dirs);
        if (files.size() > 0) {
            libraryElement.setAttribute(ATTR_FILES, "" + files.size()); // NOI18N
        }
        if (dirs.size() > 0) {
            libraryElement.setAttribute(ATTR_DIRS, "" + dirs.size()); // NOI18N
        }
        libraryElement.appendChild(doc.createTextNode(pathItem));
        return libraryElement;
    }

    public static final class Item extends BaseClassPathSupport.Item {
        private boolean includedInDeployment;

        Item(org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate, String property,
                boolean includedInDeployment, String raw) {
            super(delegate, property, raw);
            this.includedInDeployment = includedInDeployment;
        }

        public static Item create(Library library, String property, boolean included) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(library, property);
            String libraryName = library.getName();
            return new Item(delegate, property, included, "${libs." + libraryName + ".classpath}"); // NOI18N
        }

        public static Item create(AntArtifact artifact, URI artifactURI, String property, boolean included) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(artifact, artifactURI,
                    property);
            return new Item(delegate, property, included, null);
        }

        public static Item create(File file, String property, boolean included) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(file, property);
            return new Item(delegate, property, included, null);
        }

        public static Item create(String property, boolean included) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.create(property);
            return new Item(delegate, property, included, null);
        }

        public static Item createBroken(ClassPathItem.Type type, String property, boolean included) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.createBroken(type, property);
            return new Item(delegate, property, included, null);
        }

        public static Item createBroken(File file, String property, boolean included) {
            org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item delegate =
                    org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.createBroken(file, property);
            return new Item(delegate, property, included, null);
        }

        public boolean isIncludedInDeployment() {
//            boolean result = includedInDeployment;
//            if (getType() == TYPE_JAR) {
                // at the moment we can't include folders in deployment
//                FileObject fo = FileUtil.toFileObject(getFile());
//                if (fo == null || fo.isFolder())
//                    return false;
//            }
            return includedInDeployment;
        }

        public void setIncludedInDeployment(boolean includedInDeployment) {
            this.includedInDeployment = includedInDeployment;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(delegate.toString());
            sb.append(", raw = "); // NOI18N
            sb.append(getRaw());
            sb.append(", includedInDeployment = "); // NOI18N
            sb.append(includedInDeployment);
            return sb.toString();
        }
    }
}
