/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * XDMModel.java
 *
 * Created on August 4, 2005, 6:41 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xdm.diff.XDMTreeDiff;
import org.netbeans.modules.xml.xdm.diff.DiffEvent;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.TokenType;
import org.netbeans.modules.xml.xdm.nodes.XMLSyntaxParser;
import org.netbeans.modules.xml.xdm.visitor.FindVisitor;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.visitor.FindNamespaceVisitor;
import org.netbeans.modules.xml.xdm.visitor.Utils;

/**
 */
public class XDMModel {
    
    private XDMModel(Document doc) {
        currentDocument = doc;
        status = Status.STABLE;
    }
    
    public XDMModel(javax.swing.text.Document initialVersion) {
        if (!(initialVersion instanceof BaseDocument)) {
            throw new IllegalArgumentException("invalid document passed");
        }
        docBuffer = (BaseDocument)initialVersion;
        if(!XMLKit.class.isAssignableFrom(docBuffer.getKitClass())) {
            throw new IllegalArgumentException("invalid document passed");
        }
        ues = new UndoableEditSupport(this);
        pcs = new PropertyChangeSupport(this);
        parser = new XMLSyntaxParser(docBuffer);
        setStatus(Status.UNPARSED);
    }
    
    /**
     * This api flushes the changes made to the model to the underlying document.
     */
    public synchronized void flush() {
        flushDocument(getDocument());
    }
    
    /**
     * This api syncs the model with the underlying swing document.
     * If its the first time sync is called, swing document is parsed and model
     * is initialized. Otherwise the changes made to swing document are applied
     * to the model using DiffMerger.
     */
    public synchronized void sync() throws IOException {
        Document oldDoc = currentDocument;
        setStatus(Status.PARSING);
        try {
            Document newDoc = parser.parse();
            if (oldDoc == null) {
                // Dont do the merge for sync when creating model
                newDoc.addedToTree(this);
                setDocument(newDoc);               
            } else {
				//Using In-line diff approach
				performDiff( newDoc, true );
            }
	        setStatus(Status.STABLE);
        } catch (BadLocationException ble) {
            IOException ioe = new IOException();
            ioe.initCause(ble);
            throw ioe;
        } catch (IllegalArgumentException iae) {
			iae.printStackTrace();
            IOException ioe = new IOException();
            ioe.initCause(iae);
            throw ioe;
        } finally {
            if(getStatus() != Status.STABLE) {
                setStatus(Status.BROKEN);
				setDocument(oldDoc);
            }
        }
    }

	private void fireDiffEvents(final List<DiffEvent> deList) {
		//dumpModel("Doc After mutate: ");
		for ( DiffEvent de:deList ) {
			DiffEvent.NodeType nodeType = de.getNodeType();
			if ( nodeType == DiffEvent.NodeType.WHITE_SPACE ) continue;//skip if WS
			DiffEvent.Type type = de.getType();
			if ( type == DiffEvent.Type.ADD ) {
				//Node newNode = de.getNewPathToRoot().get( 0 );
				//Node newNode = de.getNewNode();
				//assert newNode != null;				
				DiffEvent.NewInfo newInfo = de.getNewInfo();
				assert newInfo != null;
				pcs.firePropertyChange( PROP_ADDED, null, newInfo/*newNode*/ );
			}
			else if ( type == DiffEvent.Type.DELETE ) {
				//Node oldNode = de.getOldPathToRoot().get( 0 );
				//Node oldNode = de.getOldNode();
				//assert oldNode != null;
				DiffEvent.OldInfo oldInfo = de.getOldInfo();
				assert oldInfo != null;				
				pcs.firePropertyChange( PROP_DELETED, oldInfo/*oldNode*/, null );
			}
			else if ( type == DiffEvent.Type.CHANGE ) {
				//Node oldNode = de.getOldPathToRoot().get( 0 );
//				Node oldNode = de.getOldNode();
//				assert oldNode != null;
				DiffEvent.OldInfo oldInfo = de.getOldInfo();
				assert oldInfo != null;					
				
				//Node newNode = de.getNewPathToRoot().get( 0 );
//				Node newNode = de.getNewNode();
//				assert newNode != null;						
				DiffEvent.NewInfo newInfo = de.getNewInfo();
				assert newInfo != null;
				
				//fire attribute change events
				if ( de.getNodeType() == DiffEvent.NodeType.ELEMENT ) {
					List<List<Node>> attrChanges = de.getAttrChanges();
					for ( List<Node> pair:attrChanges ) {
						Node oldAttr = pair.get( 0 );
						Node newAttr = pair.get( 1 );
						if ( oldAttr == null )
							pcs.firePropertyChange( PROP_ADDED, null, 
									DiffEvent.createNewInfo( newAttr, -1, oldInfo.getNode(), newInfo.getDocument() ) );
						else {
							if ( newAttr == null )
								pcs.firePropertyChange( PROP_DELETED, 
									DiffEvent.createOldInfo( oldAttr, -1, oldInfo.getNode(), oldInfo.getDocument() ) , null );
							else
								pcs.firePropertyChange( PROP_MODIFIED, 
										DiffEvent.createOldInfo( oldAttr, -1, oldInfo.getNode(), oldInfo.getDocument() ), 
										DiffEvent.createNewInfo( newAttr, -1, oldInfo.getNode(), newInfo.getDocument() ) );
						}
					}
				}

				//fire delete and add events for position change of element/text
				if ( de.getOldNodePosition() != de.getNewNodePosition() ) {
					pcs.firePropertyChange( PROP_DELETED, oldInfo/*de.getOldNode()*/, null );
					pcs.firePropertyChange( PROP_ADDED, null, newInfo/*de.getNewNode()*/ );								
				}
				else if ( de.getNodeType() == DiffEvent.NodeType.TEXT ) //text change only
					pcs.firePropertyChange( PROP_MODIFIED, oldInfo, newInfo/*oldNode, newNode*/ );
			}
		}
	}
    
    /**
     * This api replaces given old node with given new node.
     * The old node passed must be in tree, the new node must not be in tree,
     * and new node must be clone of old node.
     * @param oldValue The old node to be replaced.
     * @param newValue The new node.
     * @return The new parent
     */
    public synchronized Node modify(Node oldValue, Node newValue) {
        checkStableOrParsingState();
        if (oldValue.getId() != newValue.getId()) {
            throw new IllegalArgumentException("newValue must be a clone of oldValue");
        }
        checkNodeInTree(newValue);
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, oldValue);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldNode = path.remove(0);
        Node oldParent = path.remove(0);
        Node newParent;
        if(oldParent instanceof Element && oldNode instanceof Attribute) {
            assert newValue instanceof Attribute;
            newParent = (Node)oldParent.clone(false,true,false);
            ((Element)newParent).replaceAttribute((Attribute)newValue,(Attribute)oldNode);
        } else {
            newParent = (Node)oldParent.clone(false,false,true);
            newParent.replaceChild(newValue, oldNode);
        }
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
		//dumpModel("After modify: "+oldValue.getNodeName());
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( oldValue, -1, oldParent, currentDocument );
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( newValue, -1, newParent, d );
			pcs.firePropertyChange(PROP_MODIFIED, oldInfo, newInfo);
            //pcs.firePropertyChange(PROP_MODIFIED, oldValue, newValue);
        }
        return newParent;
    }
    
    /**
     * This api adds given node to given parent at given index.
     * The added node will be part of childnodes of the parent,
     * and its index will be the given index. If the given index
     * is out of the parents childnodes range, the node will be
     * appended.
     * @param parent The parent node to which the node is to be added.
     * @param node The node which is to be added.
     * @param offset The index at which the node is to be added.
     * @return The parent node resulted by addition of this node
     */
    public synchronized Node add(Node parent, Node node, int offset) {
        if(offset<0) throw new IndexOutOfBoundsException();
        checkStableOrParsingState();
        checkNodeInTree(node);
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, parent);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldParent = path.remove(0);
        Node newParent;
        if(oldParent instanceof Element && node instanceof Attribute) {
            newParent = (Node)oldParent.clone(false,true,false);
            Element newElement = (Element)newParent;
            if (offset>newElement.getAttributes().getLength())
                throw new IndexOutOfBoundsException();
            newElement.addAttribute((Attribute)node,offset);
        } else {
            newParent = (Node)oldParent.clone(false,false,true);
            if (offset>newParent.getChildNodes().getLength())
                throw new IndexOutOfBoundsException();
            if(offset<newParent.getChildNodes().getLength()) {
                Node refChild = (Node)newParent.getChildNodes().item(offset);
                newParent.insertBefore(node,refChild);
                doPrettyPrint(newParent,node);
            } else {
                newParent.appendChild(node);
                doPrettyPrint(newParent);
            }
        }
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
		//dumpModel("After add: "+node.getNodeName());		
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( node, -1, newParent, d );
			pcs.firePropertyChange(PROP_ADDED, null, newInfo);
            //pcs.firePropertyChange(PROP_ADDED, null, node);
        }
        return newParent;
    }

	private void dumpModel(String message) {
		flush();
		try {
			System.out.println(message+"\n"+docBuffer.getText(0, docBuffer.getLength()));		
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}		
	}
    
    /**
     * This api adds given node to given parent before given ref node.
     * The inserted node will be part of childnodes of the parent,
     * and will appear before ref node.
     * @param parent The parent node to which the node is to be added.
     * @param node The node which is to be added.
     * @param refChild The ref node (child) of parent node,
     *                  before which the node is to be added.
     * @return The parent node resulted by inserion of this node.
     */
    public synchronized Node insertBefore(Node parent, Node node, Node refChild) {
        checkStableOrParsingState();
        checkNodeInTree(node);
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, parent);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldParent = path.remove(0);
        Node newParent = (Node)oldParent.clone(false,false,true);
        newParent.insertBefore(node,refChild);
        doPrettyPrint(newParent,node);
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( node, -1, newParent, d );
			pcs.firePropertyChange(PROP_ADDED, null, newInfo);
            //pcs.firePropertyChange(PROP_ADDED, null, node);
        }
        return newParent;
    }
    
    /**
     * This api adds given node to given parent at the end.
     * The added node will be part of childnodes of the parent,
     * and it will be the last node.
     * @param parent The parent node to which the node is to be appended.
     * @param node The node which is to be appended.
     * @return The parent node resulted by addition of this node
     */
    public synchronized Node append(Node parent, Node node) {
        checkStableOrParsingState();
        checkNodeInTree(node);
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, parent);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldParent = path.remove(0);
        Node newParent = (Node)oldParent.clone(false,false,true);
        newParent.appendChild(node);
        doPrettyPrint(newParent);
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( node, -1, newParent, d );
			pcs.firePropertyChange(PROP_ADDED, null, newInfo);
            //pcs.firePropertyChange(PROP_ADDED, null, node);
        }
        return newParent;
    }
    
    /**
     * This api deletes given node from a tree.
     * @param node The node  to be deleted.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized Node delete(Node n) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, n);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldNode = path.remove(0);
        Node oldParent = path.remove(0);
        boolean attributesWritable = n instanceof Attribute;
        Node newParent = (Node)oldParent.clone(false,attributesWritable,!attributesWritable);
        newParent.removeChild(oldNode);
        undoPrettyPrint(newParent, oldNode);
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
		//dumpModel("After delete: "+n.getNodeName());
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( n, -1, oldParent, currentDocument );
			pcs.firePropertyChange(PROP_DELETED, oldInfo, null);
            //pcs.firePropertyChange(PROP_DELETED, n, null);
        }
        return newParent;
    }
    
    /**
     * This api deletes given node from a given parent node.
     * @param parent The parent node from which the node is to be deleted.
     * @param child The node  to be deleted.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized Node remove(Node parent, Node child) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, child);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldNode = path.remove(0);
        Node oldParent = path.remove(0);
        assert parent.isEquivalentNode(oldParent);
        Node newParent = (Node)oldParent.clone(false,false,true);
        newParent.removeChild(oldNode);
        undoPrettyPrint(newParent, oldNode);
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( child, -1, oldParent, currentDocument );
			pcs.firePropertyChange(PROP_DELETED, oldInfo, null);
            //pcs.firePropertyChange(PROP_DELETED, child, null);
        }
        return newParent;
    }
    
    public synchronized Node replaceChild(Node parent, Node child, Node newChild) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, child);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldNode = path.remove(0);
        Node oldParent = path.remove(0);
        assert parent.isEquivalentNode(oldParent);
        Node newParent = (Node)oldParent.clone(false,false,true);
        newParent.replaceChild(newChild, oldNode);
        List<Node> newAncestors = updateAncestors(path, newParent, oldParent);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( child, -1, oldParent, currentDocument );
			pcs.firePropertyChange(PROP_DELETED, oldInfo, null);			
            //pcs.firePropertyChange(PROP_DELETED, child, null);
        }
        return newParent;
    }
    
    /**
     * This api sets an attribute given name and value of a given element node.
     * If an attribute with given name already present in element, it will only
     * set the value. Otherwise a new attribute node, with given name and value,
     * will be appended to the attibute list of the element node.
     * @param element The element of which the attribute to be set.
     * @param name The name of the attribute to be set.
     * @param value The value of the attribute to be set.
     * @return The element resulted by setting of attribute.
     */
    public synchronized Node setAttribute(Element element, String name, String value) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, element);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldElement = path.remove(0);
        assert oldElement instanceof Element;
        Element newElement = (Element)oldElement.clone(false,true,false);
        newElement.setAttribute(name,value);
        List<Node> newAncestors = updateAncestors(path, newElement, oldElement);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newElement);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			Node oldParent = path.get(0);
			Node newParent = pathVisitor.findPath(d, newElement).get(1);			
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( oldElement, -1, oldParent, currentDocument );
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( newElement, -1, newParent, d );
			pcs.firePropertyChange(PROP_MODIFIED, oldInfo, newInfo);			
            //pcs.firePropertyChange(PROP_MODIFIED, oldElement, newElement);
        }
        return newElement;
    }
    
    /**
     * This api removes an attribute given name and value of a given element node.
     * @param element The element of which the attribute to be removed.
     * @param name The name of the attribute to be removed.
     * @return The element resulted by removed of attribute.
     */
    public synchronized Node removeAttribute(Element element, String name) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, element);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldElement = path.remove(0);
        assert oldElement instanceof Element;
        Element newElement = (Element)oldElement.clone(false,true,false);
        newElement.removeAttribute(name);
        List<Node> newAncestors = updateAncestors(path, newElement, oldElement);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newElement);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			Node oldParent = path.get(0);
			Node newParent = pathVisitor.findPath(d, newElement).get(1);			
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( oldElement, -1, oldParent, currentDocument );
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( newElement, -1, newParent, d );
			pcs.firePropertyChange(PROP_MODIFIED, oldInfo, newInfo);		
            //pcs.firePropertyChange(PROP_MODIFIED, oldElement, newElement);
        }
        return newElement;
    }
    
    /**
     * This api sets given value in given node.
     * @param node The node of which the value to be set.
     * @param value The value to be set.
     * @return The node resulted by setting the value.
     */
    //Note: Node.setNodeValue is *not* implemented.
    public synchronized Node setValue(Node node, String value) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, node);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldNode = path.remove(0);
        Node newNode = (Node)oldNode.clone(true,false,false);
        newNode.setNodeValue(value);
        List<Node> newAncestors = updateAncestors(path, newNode, oldNode);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newNode);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			Node oldParent = path.get(0);
			Node newParent = pathVisitor.findPath(d, newNode).get(1);			
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( oldNode, -1, oldParent, currentDocument );
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( newNode, -1, newParent, d );
			pcs.firePropertyChange(PROP_MODIFIED, oldInfo, newInfo);			
            //pcs.firePropertyChange(PROP_MODIFIED, oldNode, newNode);
        }
        return newNode;
    }
    
    public synchronized Node setTextValue(Node node, String value) {
        checkStableOrParsingState();
        Document currentDocument = getDocument();
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(currentDocument, node);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        Node oldNode = path.remove(0);
        Node newNode = (Node)oldNode.clone(false,false,true);
        resetChildren(newNode);
        Node textNode = (Node) currentDocument.createTextNode(value);
        newNode.appendChild(textNode);
        List<Node> newAncestors = updateAncestors(path, newNode, oldNode);
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newNode);
        d.addedToTree(this);
        setDocument(d);
        if(getStatus() != Status.PARSING) {
            fireUndoableEditEvent(d, currentDocument);
			Node oldParent = path.get(0);
			Node newParent = pathVisitor.findPath(d, newNode).get(1);			
			DiffEvent.OldInfo oldInfo = 
				DiffEvent.createOldInfo( oldNode, -1, oldParent, currentDocument );
			DiffEvent.NewInfo newInfo = 
				DiffEvent.createNewInfo( newNode, -1, newParent, d );
			pcs.firePropertyChange(PROP_MODIFIED, oldInfo, newInfo);
            //pcs.firePropertyChange(PROP_MODIFIED, oldNode, newNode);
        }
        return newNode;
    }

    /**
     * This is utility method which updates all the ancestors in the given
     * ancestor list of given originalNode. The list returned represents
     * the ancestors of given modified node.
     * @param ancestors the list of ancestors starting from parent
     * @param modifiedNode The modified node for which the new list is to be created
     * @param originalNode The original node which ancestors are given
     * @return The list of new ancestors starting parent for the modified node
     */
    private List<Node> updateAncestors(List<Node> ancestors, Node modifiedNode, Node originalNode) {
        assert ancestors != null && modifiedNode != null && originalNode != null;
        List<Node> newAncestors = new ArrayList<Node>(ancestors.size());
        Node currentModifiedNode = modifiedNode;
        Node currentOrigNode = originalNode;
        for(Node parentNode: ancestors) {
            Node newParentNode = (Node)parentNode.clone(false,false,true);
            newParentNode.replaceChild(currentModifiedNode, currentOrigNode);
            newAncestors.add(newParentNode);
            currentOrigNode = parentNode;
            currentModifiedNode = newParentNode;
        }
        return newAncestors;
    }
    
    /**
     * This api returns the latest stable document in the model.
     * @return The latest stable document in the model.
     */
    public synchronized Document getDocument() {
        checkStableOrParsingState();
        return currentDocument;
    }
    
    /**
     * This api returns the current document in the model, regardless of the state.
     * @return The latest stable document in the model.
     */
    public synchronized Document getCurrentDocument() {
        return currentDocument;
    }
    
    /**
     * This method is used to restore a document and cause events to be fired.
     */
    synchronized void resetDocument(Document newDoc) throws IOException {
        try {
            fireUndoEvents = false;
            flushDocument(newDoc);
			//only perform diff and fire events
			performDiff( newDoc, false );
            setDocument(newDoc);
        } finally {
            fireUndoEvents = true;
        }
    }
    
    private void flushDocument(Document newDoc) {
        checkStableState();
        try {
            FlushVisitor flushvisitor = new FlushVisitor();
            String newXMLText = flushvisitor.flushModel(newDoc);
            Utils.replaceDocument(docBuffer, newXMLText);
        } catch (BadLocationException ble) {
            assert false;
        }
    }
    
    public void setDocument(Document newDoc) {
        currentDocument = newDoc;
    }
    
    /**
     * This returns the statuc of the model.
     * @return the status.
     * @see #Status
     */
    public synchronized Status getStatus() {
        return status;
    }
    
    /**
     * This api adds an undoable edit listener.
     * @param l The undoable edit listener to be added.
     */
    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        ues.addUndoableEditListener(l);
    }
    
    /**
     * This api removes an undoable edit listener.
     * @param l The undoable edit listener to be removed.
     */
    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        ues.addUndoableEditListener(l);
    }
    
    /**
     * This api adds a property change listener.
     * @param pcl The property change listener to be added.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    /**
     * This api removes a property change listener.
     * @param pcl The property change listener to be removed.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
    /**
     * Find the node with same id in the current tree.
     */
    private synchronized Node findNode(int id) {
        FindVisitor fv = new FindVisitor();
        return fv.find(getDocument(), id);
    }
    
    /**
     * This represents the status of the XDM Model.
     * Status STABLE means the latest attempt to parse was successful
     * Status BROKEN means that the latest attempt to parse was unsuccessful.
     * Status UNPARSED means the document has not been parsed yet.
     * Status PARSING means the document is being parsed.
     */
    //TODO Last Parsed status
    public enum Status {BROKEN, STABLE, UNPARSED, PARSING;}
    
    private void fireUndoableEditEvent(Document newDoc, Document oldDoc) {
        if (fireUndoEvents) {
            assert newDoc != oldDoc;
            UndoableEdit ee = new XDMModelUndoableEdit(oldDoc, newDoc, this);
            UndoableEditEvent ue = new UndoableEditEvent(this, ee);
            for (UndoableEditListener l:ues.getUndoableEditListeners()) {
                l.undoableEditHappened(ue);
            }
        }
    }
    
    private void checkNodeInTree(Node n) {
        if (n.isInTree()) {
            throw new IllegalArgumentException("newValue must not have been added to model"); // NOI18N
        }
    }
    
    private void checkStableState() {
        if (getStatus() != Status.STABLE ) {
            throw new IllegalStateException("flush can only be called from STABLE STATE"); //NOI18N
        }
    }
    
    private void checkStableOrParsingState() {
        if (getStatus() != Status.STABLE && getStatus() != Status.PARSING) {
            throw new IllegalStateException("The model is not initialized or is broken."); //NOI18N
        }
    }
    
    private void setStatus(Status s) {
        status = s;
    }
    
    /**
     * This api keeps track of the nodes created in this model.
     * @return the id of the next node to be created.
     */
    public int getNextNodeId() {
        int nodeId = nodeCount;
        nodeCount++;
        return nodeId;
    }
    
    private boolean isPretty() {
        return pretty;
    }
    
    public void setPretty(boolean print) {
        pretty = print;
    }
    
    private void doPrettyPrint(Node parent) {
        if ((getStatus() != Status.PARSING) && isPretty()) {
            Text txt = (Text)getDocument().createTextNode("\n");
            txt.setText("\n", TokenType.TOKEN_PRETTY_PRINT);
            parent.appendChild(txt);
        }
    }
    
    private void doPrettyPrint(Node parent, Node n) {
        if ((getStatus() != Status.PARSING) && isPretty()) {
            int index = ((NodeImpl)parent).getIndexOfChild(n);
            Node ref = (Node)parent.getChildNodes().item((index+1));
            Text txt = (Text)this.getDocument().createTextNode("\n");
            txt.setText("\n", TokenType.TOKEN_PRETTY_PRINT);
            parent.insertBefore(txt, ref);
        }
    }
    
    private void undoPrettyPrint(Node parent, Node n) {
        if ((getStatus() != Status.PARSING) && isPretty()) {
            int index = ((NodeImpl)parent).getIndexOfChild(n);
            Node txt = (Node)parent.getChildNodes().item(index+1);
            if (txt instanceof Text) {
                if ((((NodeImpl)txt).getTokens().size() == 1) &&
                        ((NodeImpl)txt).getTokens().get(0).getType() == TokenType.TOKEN_PRETTY_PRINT) {
                    parent.removeChild(txt);
                }
            }
        }
    }
    
    /**
     * This api returns the namespaceuri of specified node.
     * @param node The node which namespace to find.
     * @return The namespaceuri of given node.
     */
    public String getNamespaceURI(Node node) {
        return fnv.findNamespace(getDocument(),node);
    }	

    private static void resetChildren(Node parent) {
        while(parent.hasChildNodes()) {
            parent.removeChild(parent.getLastChild());
        }
    }

	private void performDiff(Document newDoc, boolean doMerge) {
		XDMTreeDiff treeDiff = new XDMTreeDiff();
		try {
			List<DiffEvent> deList = null;
			if ( doMerge ) {
				//create a XDM model for this new document
				//XDMModel newModel = new XDMModel( newDoc );
				//newDoc.addedToTree( newModel );						
				//dumpModel("Doc Before mutate: ");
				deList = treeDiff.performDiffAndMutate( this, newDoc );
				//dumpModel("Doc After mutate: ");
			}
			else
				deList = treeDiff.performDiff( this, newDoc );
			//fire events
			if ( deList != null && !deList.isEmpty() )
				fireDiffEvents(deList);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}		
	}
    
    /**
     * The xml syntax parser
     */
    private XMLSyntaxParser parser;
    
    /**
     * The current stable document represented by the model
     */
    private Document currentDocument;
    
    /**
     * Property change support
     */
    private PropertyChangeSupport pcs;
    
    /**
     * The underlying swing document
     */
    private BaseDocument docBuffer;
    
    /**
     * Current status of the model
     */
    private Status status;
    
    private boolean pretty = false;
    
    /**
     * Undoable edit support
     */
    private UndoableEditSupport ues;
    
    /**
     * whether to fire undo events
     */
    private boolean fireUndoEvents = true;
    
    /**
     * The names of property change events fired
     */
    /**
     * Indicates node modified
     */
    public static final String PROP_MODIFIED = "modified";
    /**
     * Indicates node deleted
     */
    public static final String PROP_DELETED = "deleted";
    /**
     * Indicates node added
     */
    public static final String PROP_ADDED = "added";
    
    /**
     * current node count
     */
    private int nodeCount = 0;
    
    /**
     * Namespace finder visitor
     */
    private FindNamespaceVisitor fnv = new FindNamespaceVisitor();

	private boolean isPSStable=false;
}
