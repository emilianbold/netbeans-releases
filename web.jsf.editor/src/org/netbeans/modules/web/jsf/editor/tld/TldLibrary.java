/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.tld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.xml.services.UserCatalog;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */

public class TldLibrary {

    private FileObject definitionFile;
    private String prefix;
    private String uri;
    private String displayName;
    private Map<String, Tag> tags = new HashMap<String, Tag>();

    static TldLibrary create(FileObject definitionFile) throws TldLibraryException {
        return new TldLibrary(definitionFile);
    }

    static TldLibrary create(InputStream content) throws TldLibraryException {
        return new TldLibrary(content);
    }

    private TldLibrary(FileObject definitionFile) throws TldLibraryException {
        this.definitionFile = definitionFile;
        parseLibrary();
    }

    private TldLibrary(InputStream content) throws TldLibraryException {
        this.definitionFile = null;
        parseLibrary(content);
    }

    @Override
    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(getDefinitionFile() != null ? getDefinitionFile().getFileSystem().getRoot().getURL().toString() + ";" + getDefinitionFile().getPath() : ""); //NOI18N
            sb.append("; defaultPrefix = " + getDefaultPrefix() + "; uri = " + getURI() + "; tags={"); //NOI18N
            for(Tag t : getTags().values()) {
                sb.append(t.toString());
            }
            sb.append("}]"); //NOI18N
            return sb.toString();
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }

    FileObject getDefinitionFile() {
        return definitionFile;
    }

    public String getURI() {
        return uri;
    }

    public String getDefaultPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Map<String, Tag> getTags() {
        return tags;
    }

    //--------------- private -------------------
    private void parseLibrary() throws TldLibraryException {
        try {
            parseLibrary(getDefinitionFile().getInputStream());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    private void parseLibrary(InputStream content) throws TldLibraryException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource(content); //default encoding?!?!
            is.setEncoding("UTF-8");
            docBuilder.setEntityResolver(UserCatalog.getDefault().getEntityResolver()); //we count on TaglibCatalog from web.core module
            Document doc = docBuilder.parse(is);

            //usually the default taglib prefix
            Node tagLib = getNodeByName(doc, "taglib"); //NOI18N

            prefix = getTextContent(tagLib, "short-name"); //NOI18N
            if(prefix == null) {
                //no default prefix
                prefix = "";
            }

            uri = getTextContent(tagLib, "uri"); //NOI18N
            if(uri == null) {
                throw new TldLibraryException("Missing uri entry in " + getDefinitionFile().getPath() + " library.", null);
            }

            displayName = getTextContent(tagLib, "display-name"); //NOI18N
            if(displayName == null) {
                //no display-name specified in the TLD, lets try to get the displayname from names registry
                displayName = TldUtils.getLibraryDisplayName(uri);
                if(displayName == null) {
                    //no entry even here, use TLD file name
                    displayName = getDefinitionFile().getNameExt();
                }
            }

            //scan the <tag> nodes content - the tag descriptions
            NodeList tagNodes = doc.getElementsByTagName("tag"); //NOI18N
            if (tagNodes != null) {
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    Node tag = tagNodes.item(i);
                    String tagName = getTextContent(tag, "name"); //NOI18N
                    String tagDescription = getTextContent(tag, "description"); //NOI18N

                    Collection<Attribute> attrs = new ArrayList<Attribute>();
                    //find attributes
                    for(Node attrNode : getNodesByName(tag, "attribute")) { //NOI18N
                        String aName = getTextContent(attrNode, "name"); //NOI18N
                        String aDescription = getTextContent(attrNode, "description"); //NOI18N
                        boolean aRequired = Boolean.parseBoolean(getTextContent(attrNode, "required")); //NOI18N

                        attrs.add(new Attribute(aName, aDescription, aRequired));
                    }

                    tags.put(tagName, new Tag(tagName, tagDescription, attrs));

                }
            }

        } catch (ParserConfigurationException ex) {
            throw new TldLibraryException("Error parsing TLD library: ", ex); //NOI18N
        } catch (SAXException ex) {
            throw new TldLibraryException("Error parsing TLD library: ", ex); //NOI18N
        } catch (IOException ex) {
            throw new TldLibraryException("Error parsing TLD library: ", ex); //NOI18N
        }


    }

    private static String getTextContent(Node parent, String childName) {
        Node found = getNodeByName(parent, childName);
        return found == null ? null : found.getTextContent().trim();
    }

    private static Node getNodeByName(Node parent, String childName) {
        Collection<Node> found = getNodesByName(parent, childName);
        if(!found.isEmpty()) {
            return found.iterator().next();
        } else {
            return null;
        }
    }

    private static Collection<Node> getNodesByName(Node parent, String childName) {
        Collection<Node> nodes = new ArrayList<Node>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(childName)) {
                nodes.add(n);
            }
        }
        return nodes;
    }

    public static class Tag {

        private String name;
        private String description;
        private Collection<Attribute> attrs;

        Tag(String name, String description, Collection<Attribute> attrs) {
            this.name = name;
            this.description = description;
            this.attrs = attrs;
        }

        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }

        public Collection<Attribute> getAttributes() {
            return attrs;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Tag[name=" + getName() + /*", description=" + getDescription() +*/ ", attributes={"); //NOI18N
            for(Attribute attr : getAttributes()) {
                sb.append(attr.toString() + ",");
            }
            sb.append("}]");
            return sb.toString();
        }

    }

    public static class Attribute {
        
        private String name;
        private String description;
        private boolean required;

        Attribute(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public String toString() {
            return "Attribute[name=" + getName() + /*", description=" + getDescription() + */ ", required=" + isRequired() + "]"; //NOI18N
        }



    }
}
