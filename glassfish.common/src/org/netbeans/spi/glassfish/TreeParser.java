/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.spi.glassfish;

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
    
    public static boolean readXml(File xmlFile, List<Path> pathList) throws IllegalStateException {
        boolean result = false;
        InputStream is = null;
        try {
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
    public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
        if(skipping != null) {
            depth++;
            if(childNodeReader != null) {
                LOGGER.log(Level.FINER, "Skip: reading " + qname);
                childNodeReader.readChildren(qname, attributes);
            }
            LOGGER.log(Level.FINEST, "Skip: descend, depth is " + depth + ", qn is " + qname);
        } else {
            Node child = rover.findChild(qname);
            if(child != null) {
                rover = child;
                LOGGER.log(Level.FINER, "Rover descend to " + rover);
                
                NodeReader reader = rover.getReader();
                if(reader != null) {
                    reader.readAttributes(attributes);
                }
            } else {
                skipping = qname;
                depth = 1;
                childNodeReader = rover.getReader();
                if(childNodeReader != null) {
                    LOGGER.log(Level.FINER, "Skip: reading " + qname);
                    childNodeReader.readChildren(qname, attributes);
                }
                LOGGER.log(Level.FINEST, "Skip: start, depth is " + depth + ", qn is " + qname);
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
                if(childNodeReader != null) {
                    childNodeReader.endNode(qname);
                }
                LOGGER.log(Level.FINEST, "Skip: ascend, depth is " + depth);
                skipping = null;
                childNodeReader = null;
            } else {
                LOGGER.log(Level.FINEST, "Skip: ascend, depth is " + depth);
            }
        } else {
            rover = rover.getParent();
            LOGGER.log(Level.FINER, "Rover ascend to " + rover);
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
    
    public static abstract class NodeReader {

        public void readAttributes(Attributes attributes) throws SAXException {
        }

        public void readChildren(String qname, Attributes attributes) throws SAXException {
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
                LOGGER.log(Level.FINER, "Root node created: " + parts[0]);
                root = new Node(parts[0]);
            }
            Node rover = root;
            for(int i = 1; i < parts.length; i++) {
                if(parts[i] != null && parts[i].length() > 0) {
                    Node existing = rover.findChild(parts[i]);
                    if(existing != null) {
                        LOGGER.log(Level.FINER, "Existing node " + parts[i] + " at level " + i);
                        rover = existing;
                    } else {
                        LOGGER.log(Level.FINER, "Adding node " + parts[i] + " at level " + i);
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
            StringBuffer buf = new StringBuffer(500);
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
