/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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

/*
 * BaseNode.java
 *
 * Created on August 11, 2005, 10:40 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.nodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.spi.dom.NamedNodeMapImpl;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.w3c.dom.*;

/**
 * This class provides base implementation of Node Interface.
 * @author Ajit
 */
public abstract class NodeImpl implements Node, Cloneable {
    
    /* flag indicating if in tree */
    private boolean inTree;
    
    /* The model to which the node belongs */
    private XDMModel model;
    
    /* id of this node */
    private int id;
    
    /* tokens */
    private List<Token> tokens;
    
    /* child nodes */
    private List<Node> children;
    
    /* attributes */
    private List<Attribute> attributes = null;
    
    /** Creates a new instance of BaseNode. sets id during creation */
    NodeImpl() {
        model = null;
        inTree = false;
        id = -1;
    }
    
    /**
     * Returns the id of this node
     * @return id - the id of this node
     */
    public final int getId() {
        return id;
    }
    
    /**
     * sets the id of this node
     * @param id - the id of this node
     */
    private void setId(int nodeId) {
        id = nodeId;
    }
    
    @Override
            public int hashCode() {
        return (int) getId();
    }
    
    /**
     * Determines if the node is any tree
     * @return Returns true is in tree, false otherwise
     */
    public final boolean isInTree() {
        return inTree && getModel()!=null;
    }
    
    /**
     * Marks the node and all its children added to a tree.
     */
    public void addedToTree(XDMModel model) {
        if (!isInTree()) {
            inTree = true;
            if(getModel() != model) {
                setModel(model);
                setId(model.getNextNodeId());
            } else {
                if(getId() == -1)
                    setId(model.getNextNodeId());
            }
            for (Node n: getChildren()) {
                n.addedToTree(model);
            }
            for (Node n: getAttributesForRead()) {
                n.addedToTree(model);
            }
        }
    }
    
    private XDMModel getModel() {
        return model;
    }
    
    private void setModel(XDMModel xdmModel) {
        assert xdmModel != null;
        model = xdmModel;
    }
    
    /**
     * @return true the passed node has same id and belongs to same model.
     * @param node Node to compare
     */
    public boolean isEquivalentNode(Node node){
        return (this==node) || getClass().isInstance(node) &&
                getModel()!=null && getModel()==((NodeImpl)node).getModel() &&
                getId() != -1  && getId()==node.getId();
    }
    
    /**
     * Validation whether a node is in a tree
     * @throws IllegalStateException if a node has already been added to a tree.
     */
    final void checkNotInTree() {
        if (isInTree()) {
            throw new IllegalStateException("mutations cannot occur on nodes already added to a tree");
        }
    }
    
    // DOM Node impl
    public boolean isSupported(String feature, String version) {
        return "1.0".equals(version);
    }
    
    /**
     * This api clones the node object and returns the clone. A node object has
     * content, attributes and children. The api will allow or disallow
     * modification of this underlying data based on the input.
     * @param cloneContent If true the content of clone can be modified.
     * @param cloneAttributes If true the attributes of the clone can be modified.
     * @param cloneChildren If true the children of the clone can be modified.
     * @return returns the clone of this node
     */
    public Node clone(boolean cloneContent, boolean cloneAttributes, boolean cloneChildren) {
        try {
            NodeImpl clone = (NodeImpl)super.clone();
            clone.inTree = false;
            if(cloneContent) {
                clone.setTokens(new ArrayList<Token>(getTokens()));
            } else {
                clone.setTokens(getTokens());
            }
            if(cloneAttributes) {
                clone.setAttributes(new ArrayList<Attribute>(getAttributesForRead()));
            } else {
                clone.setAttributes(getAttributesForRead());
            }
            if(cloneChildren) {
                clone.setChildren(new ArrayList<Node>(getChildren()));
            } else {
                clone.setChildren(getChildren());
            }
            return clone;
        } catch (CloneNotSupportedException cne) {
            throw new RuntimeException(cne);
        }
    }
    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy constructor for nodes.
     * @param deep - If true, recursively clone the subtree under the specified node;
     *               if false, clone only the node itself
     * @return the clone
     */
    //TODO revisit this
    public Node cloneNode(boolean deep) {
        try {
            NodeImpl clone = (NodeImpl)super.clone();
            clone.inTree = false;
            clone.setTokens(new ArrayList<Token>(getTokens()));
            if(deep) {
                if(hasChildNodes()) {
                    List<Node> cloneChildren = new ArrayList<Node>(getChildren().size());
                    for (Node child:getChildren()) {
                        cloneChildren.add((Node)child.cloneNode(deep));
                    }
                    clone.setChildren(cloneChildren);
                }
                if(hasAttributes()) {
                    List<Attribute> cloneAttributes = new ArrayList<Attribute>(getAttributesForRead().size());
                    for (Attribute attribute:getAttributesForRead()) {
                        cloneAttributes.add((Attribute)attribute.cloneNode(deep));
                    }
                    clone.setAttributes(cloneAttributes);
                }
            } else {
                if(hasChildNodes()) clone.setChildren(new ArrayList<Node>(getChildren()));
                 if(hasAttributes()) clone.setAttributes(new ArrayList<Attribute>(getAttributesForRead()));
            }
            return clone;
        } catch (CloneNotSupportedException cne) {
            throw new RuntimeException(cne);
        }
    }
    
    /**
     * Returns whether this node has any children.
     * @return Returns true if this node has any children, false otherwise.
     */
    public boolean hasChildNodes() {
        return !getChildren().isEmpty();
    }
    
    /**
     * A NodeList that contains all children of this node.
     * @return Returns nodelist containing children
     */
    public NodeList getChildNodes() {
        if (!hasChildNodes()) return NodeListImpl.EMPTY;
        return new NodeListImpl(getChildren());
    }
    
    /**
     * The first child of this node. If there is no such node, this returns null.
     * @return first child
     */
    public Node getFirstChild() {
        if (!hasChildNodes()) return null;
        return getChildren().get(0);
    }
    
    /**
     * The last child of this node. If there is no such node, this returns null.
     * @return last child
     */
    public Node getLastChild() {
        if (!hasChildNodes()) return null;
        return getChildren().get(getChildren().size()-1);
    }
    
    public int getIndexOfChild(Node n) {
        if (n == null) return -1;
        for (int i = 0; i < getChildren().size(); i++) {
            if (getChildren().get(i).getId() == n.getId()) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Adds the node newChild to the end of the list of children of this node.
     * Since the model is immutable checks if current node and node being added
     * are not already in tree.
     * @param newChild - The node to add.
     * @return The node added.
     */
    public Node appendChild(org.w3c.dom.Node node) {
        checkNotInTree();
        if(node instanceof Node) {
            NodeImpl nodeImpl = (NodeImpl) node;
            nodeImpl.checkNotInTree();
            getChildrenForWrite().add(nodeImpl);
            return nodeImpl;
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }
    
    /**
     * Repalces the node oldNode with newNode.
     * Since the model is immutable checks if current node
     * and the node being put, are not already in tree.
     * @param newChild - The new node to put in the child list.
     * @param oldChild - The node being replaced in the list.
     * @return The node replaced.
     */
    public Node replaceChild(org.w3c.dom.Node newNode, org.w3c.dom.Node oldNode) {
        checkNotInTree();
        if(newNode instanceof Node && oldNode instanceof Node) {
            NodeImpl newNodeImpl = (NodeImpl) newNode;
            NodeImpl oldNodeImpl = (NodeImpl) oldNode;
            newNodeImpl.checkNotInTree();
            int oldIndex = getIndexOfChild(oldNodeImpl);
            if(oldIndex!=-1) {
                return getChildrenForWrite().set(oldIndex, newNodeImpl);
            } else {
                throw new DOMException(DOMException.NOT_FOUND_ERR,null);
            }
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }
    
    /**
     * Removes the node from children list.
     * Since the model is immutable checks if current node is not already in tree.
     * @param node - The node being removed from the list.
     * @return The node removed.
     */
    public Node removeChild(org.w3c.dom.Node node) {
        checkNotInTree();
        if(node instanceof Attribute) {
            if(getAttributesForWrite().remove(node)) {
                return (Node) node;
            }
        } else if(node instanceof Node) {
            if(getChildrenForWrite().remove(node)) {
                return (Node)node;
            }
        } 
        throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
    }
    
    /**
     * Inserts the node newChild before the existing child node refChild.
     * If refChild is null, insert newChild at the end of the list of children.
     * Since the model is immutable checks if current node
     * and node being inserted are not already in tree.
     * @param newChild - The node to insert.
     * @param refChild - The reference node, i.e., the node before which the new node must be inserted.
     * @return The node being inserted.
     */
    public Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException {
        if(refChild == null)
            return appendChild(newChild);
        checkNotInTree();
        if(newChild instanceof Node && refChild instanceof Node) {
            NodeImpl newChildImpl = (NodeImpl) newChild;
            newChildImpl.checkNotInTree();
            int index = getIndexOfChild((NodeImpl)refChild);
            if(index <0)
                throw new DOMException(DOMException.NOT_FOUND_ERR, null);
            getChildrenForWrite().add(index,newChildImpl);
            return newChildImpl;
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }
    
    /**
     * Returns whether this node has any attributes.
     * @return Returns true if this node has any attributes, false otherwise.
     */
    public boolean hasAttributes() {
        return !getAttributesForRead().isEmpty();
    }
    
    /**
     * A NamedNodeMap that contains all attributes of this node.
     * @return Returns NamedNodeMap containing attributes
     */
    public NamedNodeMap getAttributes() {
        if(getAttributesForRead().isEmpty()) return NamedNodeMapImpl.EMPTY;
        Map<String,Node> attributeMap = new LinkedHashMap<String,Node>();
        for(Attribute attr: getAttributesForRead()) {
            attributeMap.put(attr.getName(),attr);
        }
        return new NamedNodeMapImpl(attributeMap);
    }
    
    /**
     * The Document object associated with this node.
     * @return the document object
     */
    public org.w3c.dom.Document getOwnerDocument() {
        return getModel().getDocument();
    }
    
    public Node getParentNode() {
        if (!isInTree()) return null;
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        List<Node> path = pfrv.findPath(getModel().getDocument(),this);
        if(path == null || path.size()<2) return null;
        return path.get(1);
    }
    
    public Node getNextSibling() {
        if (!isInTree()) return null;
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        List<Node> path = pfrv.findPath(getModel().getDocument(),this);
        if(path == null || path.size()<2) return null;
        NodeImpl parent = (NodeImpl)path.get(1);
        NodeImpl node = (NodeImpl)path.get(0);
        int nextIndex = parent.getIndexOfChild(node)+1;
        if(nextIndex>=parent.getChildren().size()) return null;
        return parent.getChildren().get(nextIndex);
    }
    
    public Node getPreviousSibling() {
        if (!isInTree()) return null;
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        List<Node> path = pfrv.findPath(getModel().getDocument(),this);
        if(path == null || path.size()<2) return null;
        NodeImpl parent = (NodeImpl)path.get(1);
        NodeImpl node = (NodeImpl)path.get(0);
        int prevIndex = parent.getIndexOfChild(node)-1;
        if(prevIndex<0) return null;
        return parent.getChildren().get(prevIndex);
    }
    
    /*
     * A code representing the type of the underlying object
     * abstract and to be implemented in subclasses
     */
    public abstract short getNodeType();
    
    /*
     * The name of this node, depending on its type
     * abstract and to be implemented in subclasses
     */
    public abstract String getNodeName();
    
    public String getNodeValue() throws DOMException {
        return null;
    }
    
    public void setNodeValue(String str) throws DOMException {
    }
    
    public String getLocalName() {
        return null;
    }
    
    public String getNamespaceURI() {
        if(isInTree()) {
            return getModel().getNamespaceURI(this);
        }
        return lookupNamespaceURI(getPrefix());
    }
    
    public String lookupNamespaceURI(String prefix) {
        if(prefix == null) prefix = "";
        if(hasAttributes()) {
            for (Attribute attribute:getAttributesForRead()) {
                if("xmlns".equals(attribute.getPrefix()) || "xmlns".equals(attribute.getName())) {
                    String key = attribute.getPrefix()==null?"":attribute.getLocalName();
                    if(key.equals(prefix)) return attribute.getValue();
                }
            }
        }
        if(isInTree()) {
            PathFromRootVisitor pfrv = new PathFromRootVisitor();
            List<Node> path = pfrv.findPath(getModel().getDocument(),this);
            if(path == null || path.size()<2) return null;
            path.remove(0);
            for(Node ancestor:path) {
                NodeImpl ancestorImpl = (NodeImpl)ancestor;
                if(ancestorImpl.hasAttributes()) {
                    for (Attribute attribute:ancestorImpl.getAttributesForRead()) {
                        if("xmlns".equals(attribute.getPrefix()) || "xmlns".equals(attribute.getName())) {
                            String key = attribute.getPrefix()==null?"":attribute.getLocalName();
                            if(key.equals(prefix)) return attribute.getValue();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private String findPrefixLocally(String uri) {
        if(hasAttributes()) {
            String defaultNamespace = null;
            for (Attribute attribute:getAttributesForRead()) {
                String attrName = attribute.getName();
                if (attrName.startsWith("xmlns")) {
                    if (attrName.length() == 5) {
                        defaultNamespace = attribute.getValue();
                    } else if (attrName.charAt(5) == ':' && uri.equals(attribute.getValue())) {
                        return attrName.substring(6);
                    }
                }
            }
            if (uri.equals(defaultNamespace)) {
                return "";
            }
        }
        return null;
    }
    public String lookupPrefix(String uri) {
        if(uri == null) return null;
        if(isInTree()) {
            PathFromRootVisitor pfrv = new PathFromRootVisitor();
            List<Node> path = pfrv.findPath(getModel().getDocument(),this);
            assert path != null && path.size() > 1;
            for(Node node : path) {
                NodeImpl n = (NodeImpl) node;
                String prefix = n.findPrefixLocally(uri);
                if (prefix != null) {
                    return prefix;
                }
            }
            return null;
        } else {
            return findPrefixLocally(uri);
        }
    }
    
    public String getPrefix() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }
    
    public void setPrefix(String str) throws DOMException {
    }
    
    public void normalize() {
    }
    // DOM level 3
    public short compareDocumentPosition(org.w3c.dom.Node a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    
    public String getBaseURI() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public Object getFeature(String a, String b) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public String getTextContent() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public Object getUserData(String a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public boolean isDefaultNamespace(String a)  {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public boolean isEqualNode(org.w3c.dom.Node a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public boolean isSameNode(org.w3c.dom.Node a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public void setTextContent(String a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public Object setUserData(String a, Object b, UserDataHandler c) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    
    /*
     * Used by DiffMerger to merge token changes
     *
     * @param newNode
     */
    public void copyTokens(Node newNode) {
        checkNotInTree();
        setTokens(((NodeImpl)newNode).getTokens());
    }
    
    /**
     * Returns a List of all children of this node.
     * @return Returns a unmodifiable List of all children of this node.
     */
    private List<Node> getChildren() {
        List<Node> readChildren;
        if (children == null) {
            readChildren = Collections.emptyList();
        } else {
            readChildren = Collections.unmodifiableList(children);
        }
        return readChildren;
    }
    
    /**
     * Returns a List of all children of this node for updates.
     * @return Returns a modifiable List of all children of this node.
     */
    private List<Node> getChildrenForWrite() {
        checkNotInTree();
        if (children == null) {
            children = new ArrayList<Node>(0);
        }
        return children;
    }
    
    /**
     * Sets the children of this node
     * @param newChildren - The list of children.
     */
    private void setChildren(List<Node> newChildren) {
        checkNotInTree();
        children = newChildren;
    }

    /**
     * Returns a readonly List of all attributes of this node.
     * @return Returns a unmodifiable List of all attributes of this node.
     */
    protected List<Attribute> getAttributesForRead() {
        List<Attribute> readAttributes;
        if (attributes == null) {
            readAttributes = Collections.emptyList();
        } else {
            readAttributes = Collections.unmodifiableList(attributes);
        }
        return readAttributes;
    }
    
    /**
     * Returns a modifiable List of all attributes of this node for updates.
     * @return Returns a modifiable List of all attributes of this node.
     */
    protected List<Attribute> getAttributesForWrite() {
        checkNotInTree();
        if (attributes == null) {
            attributes = new ArrayList<Attribute>(0);
        }
        return attributes;
    }
    
    /**
     * Sets the attributes of this node
     * @param newAttributes - The list of attributes.
     */
    private void setAttributes(List<Attribute> newAttributes) {
        checkNotInTree();
        attributes = newAttributes;
    }

    /**
     * Returns the readonly lexical tokens associated with this node.
     * @return The unmodifiable list of lexical tokens.
     */
    public List<Token> getTokens() {
        List<Token> readTokens;
        if (tokens == null) {
            readTokens = Collections.emptyList();
        } else {
            readTokens = Collections.unmodifiableList(tokens);
        }
        return readTokens;
    }
    
    /**
     * Returns the lexical tokens associated with this node for updates.
     * @return The modifiable list of lexical tokens.
     */
    List<Token> getTokensForWrite() {
        checkNotInTree();
        if (tokens == null) {
            tokens = new ArrayList<Token>(0);
        }
        return tokens;
    }
    
    /**
     * Sets the lexical tokens associated with this node
     * @param newTokens - The list of lexical tokens.
     */
    void setTokens(List<Token> newTokens) {
        tokens = newTokens;
    }
	
    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy constructor for nodes.
	 * Used during Copy/Paste, Cut/Paste operation
     * @return the clone
     */
    public Node copy() {
        try {
            NodeImpl clone = (NodeImpl)super.clone();
            clone.inTree = false;
			clone.model = null;			
            clone.setTokens(new ArrayList<Token>(getTokens()));
			if(hasChildNodes()) {
				List<Node> cloneChildren = new ArrayList<Node>(getChildren().size());
				for (Node child:getChildren()) {
					cloneChildren.add(((NodeImpl)child).copy());
				}
				clone.setChildren(cloneChildren);
			}
			if(hasAttributes()) {
 				//clone.setAttributes(new ArrayList<Attribute>(getAttributesForRead()));
 				List<Attribute> cloneAttributes = new ArrayList<Attribute>(getAttributesForRead().size());
 				for (Attribute attr:getAttributesForRead()){					
 					cloneAttributes.add((Attribute)((NodeImpl)attr).copy());
 				}				
 				clone.setAttributes(cloneAttributes);
			}
            return clone;
        } catch (CloneNotSupportedException cne) {
            throw new RuntimeException(cne);
        }
    }	
}
