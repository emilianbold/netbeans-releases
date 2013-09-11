/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.filesystems.FileObject;

/**
 * Model for the stored resources mappings (outputScripts and outputStylesheets)
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ResourcesMappingModel extends JsfPageModel {

    // index - keys
    static final String STATIC_RESOURCES_KEY = "static_resources";  //NOI18N

    // index - other
    private static final char RESOURCES_SEPARATOR = ';';            //NOI18N
    private static final char NAME_SEPARATOR = '@';                 //NOI18N
    private static final char LIB_SEPARATOR = ':';                  //NOI18N

    private final FileObject file;
    private final List<Resource> staticResources;

    public ResourcesMappingModel(FileObject file, List<Resource> resources) {
        this.file = file;
        this.staticResources = resources;
    }

    @Override
    public String storeToIndex(IndexDocument document) {
        // store resources
        StringBuilder resString = new StringBuilder();
        for (Iterator<Resource> it = staticResources.iterator(); it.hasNext();) {
            Resource resource = it.next();
            resString.append(resource.type);
            resString.append(NAME_SEPARATOR);
            resString.append(resource.name);
            resString.append(LIB_SEPARATOR);
            resString.append(resource.library);
            if (it.hasNext()) {
                resString.append(RESOURCES_SEPARATOR);
            }
        }
        document.addPair(STATIC_RESOURCES_KEY, resString.toString(), true, true);

        return ""; // the return value looks to be used nowhere
    }

    public static Collection<? extends Resource> parseResourcesFromString(String resString) {
        List<Resource> resources = new ArrayList<>();
        // parse static resources
        for (String resource : resString.split("[" + RESOURCES_SEPARATOR + "]")) {
            int nameSepIndex = resource.indexOf(NAME_SEPARATOR);
            int libSepIndex = resource.indexOf(LIB_SEPARATOR);
            if (nameSepIndex != -1 && libSepIndex != -1) {
                resources.add(new Resource(
                        ResourceType.fromString(resource.substring(nameSepIndex)),
                        resource.substring(nameSepIndex + 1, libSepIndex),
                        resource.substring(libSepIndex + 1)));
            }
        }
        return resources;
    }

    public static class Factory extends JsfPageModelFactory {

        private static final String OUTPUT_STYLESHEET_TAG_NAME = "outputStylesheet";    //NOI18N
        private static final String OUTPUT_SCRIPT_TAG_NAME = "outputScript";            //NOI18N
        private static final String LINK_TAG_NAME = "link";                       //NOI18N
        private static final String SCRIPT_TAG_NAME = "script";                         //NOI18N

        @Override
        public JsfPageModel getModel(HtmlParserResult result) {
            List<Resource> resources = new ArrayList<>();
            FileObject file = result.getSnapshot().getSource().getFileObject();
            resources.addAll(getResourcesDefinedByJsfComponents(result));
            resources.addAll(getResourcesDefinedByHtmlTags(result));
            return new ResourcesMappingModel(file, resources);
        }

        @Override
        public JsfPageModel loadFromIndex(IndexResult result) {
            List<Resource> resources = new ArrayList<>();
            String resString = result.getValue(STATIC_RESOURCES_KEY);
            resources.addAll(parseResourcesFromString(resString));
            return new ResourcesMappingModel(result.getFile(), resources);
        }

        private Collection<Resource> getResourcesDefinedByJsfComponents(HtmlParserResult result) {
            final List<Resource> resources = new ArrayList<>();
            Node node = result.root(DefaultLibraryInfo.HTML.getNamespace());
            if (node == null || node.children().isEmpty()) {
                node = result.root(DefaultLibraryInfo.HTML.getLegacyNamespace());
            }
            if (node == null || node.children().isEmpty()) {
                return resources; //no HTML Basic component in the page
            }

            // looks for all h:outputStylesheet and h:outputScript elements
            ElementUtils.visitChildren(node, new ElementVisitor() {
                @Override
                public void visit(Element node) {
                    switch(node.type()) {
                        case OPEN_TAG:
                            OpenTag openTag = (OpenTag) node;
                            if (LexerUtils.equals(OUTPUT_STYLESHEET_TAG_NAME, openTag.unqualifiedName(), false, true)
                                    || LexerUtils.equals(OUTPUT_SCRIPT_TAG_NAME, openTag.unqualifiedName(), false, true)) {
                                Attribute name = openTag.getAttribute("name");          //NOI18N
                                Attribute library = openTag.getAttribute("library");    //NOI18N
                                if (name == null) {
                                    break;
                                }
                                Resource resource = new Resource(LexerUtils.equals(OUTPUT_SCRIPT_TAG_NAME, openTag.unqualifiedName(), false, true) ?
                                        ResourceType.SCRIPT : ResourceType.STYLESHEET,
                                        name.unquotedValue().toString(),
                                        library == null ? "" : library.unquotedValue().toString()); //NOI18N
                                resources.add(resource);
                            }
                            break;
                        default:
                            break;
                    }
                }
            });

            return resources;
        }

        private Collection<Resource> getResourcesDefinedByHtmlTags(HtmlParserResult result) {
            final List<Resource> resources = new ArrayList<>();
            Node node = result.root();
            if (node == null || node.children().isEmpty()) {
                return resources; //no tags in the file
            }

            // looks for all <script>, <style> elements
            ElementUtils.visitChildren(node, new ElementVisitor() {
                @Override
                public void visit(Element node) {
                    switch(node.type()) {
                        case OPEN_TAG:
                            OpenTag openTag = (OpenTag) node;
                            if (LexerUtils.equals(LINK_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                Attribute rel = openTag.getAttribute("rel");            //NOI18N
                                Attribute type = openTag.getAttribute("type");          //NOI18N
                                Attribute href = openTag.getAttribute("href");          //NOI18N
                                if (rel == null || type == null || href == null) {
                                    break;
                                }
                                Resource resource = new Resource(ResourceType.STYLESHEET,
                                        href.unquotedValue().toString(),
                                        href.unquotedValue().toString());
                                resources.add(resource);
                            } else if (LexerUtils.equals(SCRIPT_TAG_NAME, openTag.unqualifiedName(), true, true)) {
                                Attribute type = openTag.getAttribute("type");          //NOI18N
                                Attribute src = openTag.getAttribute("src");            //NOI18N
                                if (type == null || src == null) {
                                    break;
                                }
                                Resource resource = new Resource(ResourceType.SCRIPT,
                                        src.unquotedValue().toString(),
                                        src.unquotedValue().toString());
                                resources.add(resource);
                            }
                            break;
                        default:
                            break;
                    }
                }
            });

            return resources;
        }
    }

    public static class Resource {

        private final ResourceType type;
        private final String name;
        private final String library;

        public Resource(ResourceType type, String name, String library) {
            this.type = type;
            this.name = name;
            this.library = library;
        }

        public ResourceType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getLibrary() {
            return library;
        }

    }

    public static enum ResourceType {
        SCRIPT("script"),
        STYLESHEET("stylesheet");

        private final String value;

        private ResourceType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static ResourceType fromString(String string) {
            for (ResourceType resourceType : ResourceType.values()) {
                if (resourceType.value.equals(string)) {
                    return resourceType;
                }
            }
            return null;
        }

    }

}
