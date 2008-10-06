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

package org.netbeans.modules.glassfish.spi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser that invokes a user defined node reader(s) on a list of xpath
 * designated nodes.
 * 
 * @author Peter Williams
 */
public final class TreeParser extends DefaultHandler {

    private static Logger LOGGER = Logger.getLogger("glassfish");
    private static final boolean isFinestLoggable = LOGGER.isLoggable(Level.FINEST);
    private static final boolean isFinerLoggable = LOGGER.isLoggable(Level.FINER);

    public static boolean readXml(File xmlFile, List<Path> pathList) throws IllegalStateException {
        boolean result = false;
        InputStream is = null;
        try {
            // !PW FIXME what to do about entity resolvers?  Timed out when
            // looking up doctype for sun-resources.xml earlier today (Jul 10)
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // !PW If namespace-aware is enabled, make sure localpart and
            // qname are treated correctly in the handler code.
            //                
            factory.setNamespaceAware(false);
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new TreeParser(pathList);
            is = new BufferedInputStream(new FileInputStream(xmlFile));
            saxParser.parse(new InputSource(is), handler);
            result = true;
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (SAXException ex) {
            throw new IllegalStateException(ex);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return result;
    }
    
    // Parser internal state
    private final Node root;
    private Node rover;
    
    // For skipping node blocks
    private String skipping;
    private int depth;
    private NodeReader childNodeReader;

    
    private TreeParser(List<Path> pathList) {
        root = buildTree(pathList);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(childNodeReader != null) {
            childNodeReader.readCData(skipping, ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
        if(skipping != null) {
            depth++;
            if(childNodeReader != null) {
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Skip: reading " + qname);
                childNodeReader.readChildren(qname, attributes);
            }
            if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: descend, depth is " + depth + ", qn is " + qname);
        } else {
            Node child = rover.findChild(qname);
            if(child != null) {
                rover = child;
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover descend to " + rover);
                
                NodeReader reader = rover.getReader();
                if(reader != null) {
                    if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover enter & read node " + qname);
                    reader.readAttributes(qname, attributes);
                }
            } else {
                skipping = qname;
                depth = 1;
                childNodeReader = rover.getReader();
                if(childNodeReader != null) {
                    if(isFinerLoggable) LOGGER.log(Level.FINER, "Skip: reading " + qname);
                    childNodeReader.readChildren(qname, attributes);
                }
                if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: start, depth is " + depth + ", qn is " + qname);
            }
        }
    }

    @Override
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if(skipping != null) {
            if(--depth == 0) {
                if(!skipping.equals(qname)) {
                    LOGGER.log(Level.WARNING, "Skip: " + skipping + " does not match " + qname + " at depth " + depth);
                }
                if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: ascend, depth is " + depth);
                skipping = null;
                childNodeReader = null;
            } else {
                if(isFinestLoggable) LOGGER.log(Level.FINEST, "Skip: ascend, depth is " + depth);
            }
        } else {
            NodeReader reader = rover.getReader();
            if(reader != null) {
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover exit & read node " + qname);
                reader.endNode(qname);
            }
            rover = rover.getParent();
            if(isFinerLoggable) LOGGER.log(Level.FINER, "Rover ascend to " + rover);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        rover = root;
        skipping = null;
        depth = 0;
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        LOGGER.log(Level.INFO, "Requested Entity: public id = " + publicId + ", system id = " + systemId);

        // We only expect a few entries here so use linear search directly.  If
        // this changes, considering caching using HashMap<String, String>
        //
        InputSource source = null;
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = sfs.findResource("DTDs/GlassFish");
        if(folder != null) {
            for(FileObject fo: folder.getChildren()) {
                Object attr;
                if((attr = fo.getAttribute("publicId")) instanceof String && attr.equals(publicId)) {
                    source = new InputSource(fo.getInputStream());
                    break;
                } else if((attr = fo.getAttribute("systemId")) instanceof String && attr.equals(systemId)) {
                    source = new InputSource(fo.getInputStream());
                    break;
                }
            }
        }

        return source;
    }

    public static abstract class NodeReader {

        public void readAttributes(String qname, Attributes attributes) throws SAXException {
        }

        public void readChildren(String qname, Attributes attributes) throws SAXException {
        }
        
        public void readCData(String qname, char [] ch, int start, int length) throws SAXException {
        }
        
        public void endNode(String qname) throws SAXException {
        }

    }
    
    public static class Path {
        
        private final String path;
        private final NodeReader reader;
        
        public Path(String path) {
            this(path, null);
        }
        
        public Path(String path, NodeReader reader) {
            this.path = path;
            this.reader = reader;
        }
        
        public String getPath() {
            return path;
        }
        
        public NodeReader getReader() {
            return reader;
        }
        
        @Override
        public String toString() {
            return path;
        }
        
    }

    private static Node buildTree(List<Path> paths) {
        Node root = null;
        for(Path path: paths) {
            String [] parts = path.getPath().split("/");
            if(parts == null || parts.length == 0) {
                LOGGER.log(Level.WARNING, "Invalid entry, no parts, skipping: " + path);
                continue;
            }
            if(parts[0] == null) {
                LOGGER.log(Level.WARNING, "Invalid entry, null root, skipping: " + path);
                continue;
            }
            if(root == null) {
                if(isFinerLoggable) LOGGER.log(Level.FINER, "Root node created: " + parts[0]);
                root = new Node(parts[0]);
            }
            Node rover = root;
            for(int i = 1; i < parts.length; i++) {
                if(parts[i] != null && parts[i].length() > 0) {
                    Node existing = rover.findChild(parts[i]);
                    if(existing != null) {
                        if(isFinerLoggable) LOGGER.log(Level.FINER, "Existing node " + parts[i] + " at level " + i);
                        rover = existing;
                    } else {
                        if(isFinerLoggable) LOGGER.log(Level.FINER, "Adding node " + parts[i] + " at level " + i);
                        rover = rover.addChild(parts[i]);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Broken parts found in " + path + " at level " + i);
                }
            }
            if(rover != null) {
                rover.setReader(path.getReader());
            }
        }
        return root;
    }
    
    private static class Node implements Comparable<Node> {
        
        private final String element;
        private final Map<String, Node> children;
        private Node parent;
        private NodeReader reader;
        
        public Node(String element) {
            this(element, null);
        }
        
        private Node(String element, Node parent) {
            this.element = element;
            this.children = new HashMap<String, Node>();
            this.parent = parent;
        }

        public Node addChild(String tag) {
            Node child = new Node(tag, this);
            children.put(tag, child);
            return child;
        }
        
        public Node findChild(String tag) {
            return children.get(tag);
        }
        
        public Node getParent() {
            return parent;
        }
        
        public NodeReader getReader() {
            return reader;
        }
        
        public void setReader(NodeReader reader) {
            this.reader = reader;
        }
        
        public int compareTo(Node o) {
            return element.compareTo(o.element);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.element != other.element && 
                    (this.element == null || !this.element.equals(other.element))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.element != null ? this.element.hashCode() : 0);
            return hash;            
        }

        @Override
        public String toString() {
            boolean comma = false;
            StringBuilder buf = new StringBuilder(500);
            buf.append("{ ");
            if(element != null && element.length() > 0) {
                buf.append(element);
                comma = true;
            }
            if(parent == null) {
                if(comma) {
                    buf.append(", ");
                }
                buf.append("root");
                comma = true;
            }
            if(children.size() > 0) {
                if(comma) {
                    buf.append(", ");
                }
                buf.append(children.size());
                buf.append(" sub(s)");
            }
            buf.append(" }");
            return buf.toString();
        }
        
    }
}
