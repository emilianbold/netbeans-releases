/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * MergeDiff.java
 *
 * Created on February 2, 2006, 3:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.diff.DiffEvent.NodeType;
import org.netbeans.modules.xml.xdm.diff.DiffEvent.Type;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.visitor.FindVisitor;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Owner
 */
public class MergeDiff {

	/** Creates a new instance of MergeDiff */
	public MergeDiff(ElementIdentity eID) {	
		this.eID = eID;
	}

	public void merge(XDMModel model, List<DiffEvent> deList) {
		this.model = model;
		HashMap<Node, List<DiffEvent>> deMap = new HashMap<Node, List<DiffEvent>>();
		for ( DiffEvent de: deList ) {
			Node parent = de.getOldNodeParent();
			List<DiffEvent> childDeList = deMap.get(parent);
			if ( childDeList == null ) {
				childDeList = new ArrayList<DiffEvent>();
				deMap.put( parent, childDeList );
			}
			childDeList.add( de );
		}
		
		Iterator it = deMap.keySet().iterator();
		while ( it.hasNext() ) {
			Node parent = (Node) it.next();
			List<DiffEvent> childDeList = deMap.get(parent);
			Node cloneParent = parent;
			//Apply element changes first, then text changes
			cloneParent = applyDiffEvents ( childDeList, cloneParent );
		}
	}

	/*private Node applyDiffEvents(final List<DiffEvent> childDeList, Node parent) throws DOMException {
		Node cloneParent = parent;
				
		//Apply deletes
		cloneParent = applyDeleteEvents ( filterDeList( childDeList, DiffEvent.Type.DELETE ), cloneParent );
		
		//Apply adds
		cloneParent = applyAddEvents ( filterDeList( childDeList, DiffEvent.Type.ADD ), cloneParent );
		
		//Apply changes
		cloneParent = applyChangeEvents ( filterDeList( childDeList, DiffEvent.Type.CHANGE ), cloneParent );
				
		return cloneParent;
	}

	private List<DiffEvent> filterDeList(List<DiffEvent> childDeList, DiffEvent.Type filterType) {
		List<DiffEvent> returnDeList = new ArrayList<DiffEvent>();
		for ( int i=0; i < childDeList.size(); i++ ) {
			DiffEvent de = childDeList.get(i);
			DiffEvent.Type type = de.getType();
			if ( type == filterType )
				returnDeList.add( de );
		}
		return returnDeList;
	}*/
	
	private Node applyDiffEvents(final List<DiffEvent> childDeList, Node parent) throws DOMException {
		Node cloneParent = parent;
		for ( int i=0; i < childDeList.size(); i++ ) {
			DiffEvent de = childDeList.get(i);
			DiffEvent.Type type = de.getType();
			if ( type == DiffEvent.Type.ADD )
				cloneParent = applyAddEvent( cloneParent, de );
			else if ( type == DiffEvent.Type.DELETE )
				cloneParent = applyDeleteEvent( de );
			else if ( type == DiffEvent.Type.CHANGE )
				cloneParent = applyChangeEvent( cloneParent, de );
		}
		return cloneParent;
	}	
	
	private Node applyChangeEvents(final List<DiffEvent> changeDeList, Node parent) throws DOMException {
		Node cloneParent = parent;
		for ( int i=0; i < changeDeList.size(); i++ ) {
			DiffEvent de = changeDeList.get(i);
			cloneParent = applyChangeEvent(cloneParent, de);
		}
		return cloneParent;
	}	

	private Node applyChangeEvent(Node parent, final DiffEvent de) {
		Node cloneParent = parent;
		DiffEvent.Type type = de.getType();
		
		assert type == DiffEvent.Type.CHANGE ;

		Node oldNode = de.getOldNode();//de.getOldPathToRoot().get(0);
		Node currNode = de.getNewNode();//de.getNewPathToRoot().get(0);

		assert oldNode.getLocalName().equals(currNode.getLocalName());
		/*if( oldNode.getNamespaceURI() != null ) {	
			assert oldNode.getNamespaceURI().equals(currNode.getNamespaceURI());
		}*/
		Node tmpNode = applyTokenChange( oldNode, currNode, de );
		if ( oldNode != null ) //non null if element has token change
			oldNode = tmpNode;
		
		int px1 = de.getOldNodePosition();
		int px2 = de.getNewNodePosition();		
		if ( px1 != px2 ) {
			//check for element position change
			//if pos change, then Delete and Add to change position
			Node saveNode = createClone(oldNode);
			cloneParent = delete( oldNode, de );
			
			//Add
			cloneParent = add( cloneParent, saveNode, px2, de );
		}
		return cloneParent;
	}
	
	private Node applyDeleteEvents(final List<DiffEvent> deleteDeList, Node parent) throws DOMException {
		Node cloneParent = parent;
		for ( int i=0; i < deleteDeList.size(); i++ ) {
			DiffEvent de = deleteDeList.get(i);
			cloneParent = applyDeleteEvent( de );
		}
		return cloneParent;
	}

	private Node applyDeleteEvent(final DiffEvent de) {		
		DiffEvent.Type type = de.getType();		
		assert type == DiffEvent.Type.DELETE;		
		int px1 = de.getOldNodePosition();
		int px2 = de.getNewNodePosition();
		Node delNode = de.getOldPathToRoot().get(0);
		return delete( delNode, de );
	}
		
	/*private Node applyAddEvents(final List<DiffEvent> addDeList, Node parent) throws DOMException {
		Node cloneParent = parent;
		HashMap<Node, List<DiffEvent>> proximityMap = new HashMap<Node, List<DiffEvent>>();
		for ( int i=0; i < addDeList.size(); i++ ) {
			DiffEvent de = addDeList.get(i);
			DiffEvent.Type type = de.getType();
			assert type == DiffEvent.Type.ADD;
			
			Node siblingBefore = de.getSiblingBefore();
			List<DiffEvent> prxList = proximityMap.get( siblingBefore );
			if ( prxList == null ) {
				prxList = new ArrayList<DiffEvent>();
				proximityMap.put ( siblingBefore, prxList );
			}
			
			boolean added = false;
			for ( int j=0; j<prxList.size(); j++ ) {
				if ( de.getNewNodePosition() < prxList.get(j).getNewNodePosition()) {
					prxList.add( j, de );
					added = true;
					break;
				}						
			}
			if( !added )
				prxList.add( de );
		}
		
		NodeList childNodes = cloneParent.getChildNodes();
		List<Node> childList = new ArrayList<Node>();		
		HashMap<Integer, Integer> posMap = new HashMap<Integer, Integer>();
		for ( int i = 0; i < childNodes.getLength() ; i++ ) {
			childList.add( (Node) childNodes.item(i) );
			posMap.put( new Integer(((Node) childNodes.item(i)).getId()), new Integer(i) );
		}
		
		//sort siblingBefore nodes
		List<Node> siblingBeforeList = new ArrayList<Node>();
		Iterator it = proximityMap.keySet().iterator();
		while ( it.hasNext() ) {
			Node prxAnchor = (Node) it.next();
			int pos = cloneParent.getIndexOfChild( prxAnchor );
			boolean inserted = false;
			for ( int i=0; i < siblingBeforeList.size(); i++ )
				if ( pos < cloneParent.getIndexOfChild( siblingBeforeList.get( i ) ) ) {
					siblingBeforeList.add( i, prxAnchor );
					break;
				}
			if ( !inserted )
				siblingBeforeList.add( prxAnchor );
		}
		
		for ( int i = siblingBeforeList.size()-1 ; i >= 0 ; i-- ) { //descending order
			Node prxAnchor = siblingBeforeList.get( i );
			int anchorIndex = 0;
			if( prxAnchor!=null && posMap.get(prxAnchor.getId()) != null ) 
				anchorIndex = posMap.get(prxAnchor.getId()).intValue();			
			List<DiffEvent> prxList = proximityMap.get( prxAnchor );
			//System.out.println(" applyAddEvents: ");
			//DiffFinder.printDeList( prxList );
			for ( int j=0; j < prxList.size(); j++ ) {
				DiffEvent de = prxList.get(j);
				int px1 = de.getOldNodePosition();
				int px2 = de.getNewNodePosition();
				Node currNode = de.getNewPathToRoot().get(0);
				NodeImpl newNode=createCopy(currNode);
				int index = 0;
				if( prxAnchor != null ) {
					int relativeIndex = de.getPositionFromSiblingBefore();
					if ( relativeIndex > 0 )
						index = anchorIndex + relativeIndex;
					//System.out.println(" anchorIndex: " +anchorIndex);
					//System.out.println(" relativeIndex: " +relativeIndex);					
				}
				else
					index = de.getNewNodePosition();	
				//System.out.println(" index: " +index);
				//System.out.println(" children size: " +cloneParent.getChildNodes().getLength());
				assert index <= cloneParent.getChildNodes().getLength();
				cloneParent = add( cloneParent, newNode,  index, de );
			}
		}
		return cloneParent;
	}*/		
	
	private Node applyAddEvent(Node parent, DiffEvent de) {
		Node currNode = de.getNewNode();//de.getNewPathToRoot().get(0);
		NodeImpl newNode = createCopy(currNode);
		int index = de.getNewNodePosition();	
		assert index <= parent.getChildNodes().getLength();
		return add( parent, newNode,  index, de );
	}	
	
	private Node applyTokenChange(Node oldNode, Node currNode, DiffEvent de) {	
		Node cloneNode = oldNode;
		
		//Apply attr change
		if( oldNode instanceof Element ) {
			cloneNode = applyAttrTokenChange( (Element) oldNode, (Element) currNode, de );
		}
		
		//Apply token change
		if ( DiffFinder.checkChange ( cloneNode, currNode ) ) {
			NodeImpl newNode = createClone(cloneNode);
			newNode.copyTokens ( currNode );
			modify ( cloneNode, newNode, de );
		}
		return cloneNode;
	}
	
	private Node applyAttrTokenChange(Element oldNode, Element currNode, DiffEvent de) {	
		Element cloneNode = oldNode;
		NamedNodeMap nm1 = oldNode.getAttributes();
		NamedNodeMap nm2 = currNode.getAttributes();		
		List<String> allAttrNames = new ArrayList<String>();
		HashMap<Node, Integer> posMap = new HashMap<Node, Integer>();
		for ( int i=0; i < nm1.getLength(); i++ ) {	
			Node oldAttr = (Node) nm1.item(i);
			String name = oldAttr.getNodeName();
			posMap.put( oldAttr, new Integer( i ) );
			if ( !allAttrNames.contains( name ) )
				allAttrNames.add( name );
		}
		for ( int i=0; i < nm2.getLength(); i++ ) {	
			Node newAttr = (Node) nm2.item(i);
			String name = newAttr.getNodeName();
			posMap.put( newAttr, new Integer( i ) );
			if ( !allAttrNames.contains( name ) )
				allAttrNames.add( name );
		}			
		for ( int i=0; i < allAttrNames.size(); i++ ) {
			String attrName = allAttrNames.get( i );
			Node oldAttr = (Node) nm1.getNamedItem(attrName);
			Node currAttr = (Node) nm2.getNamedItem(attrName);
			if ( oldAttr != null ) {
				if ( currAttr == null ) {
					List<Node> pair = new ArrayList<Node>( 2 );
					pair.add( oldAttr ); pair.add( null );
					de.addAttrChanges( pair );
				}
				else
					if ( DiffFinder.checkChange ( oldAttr, currAttr )) {
						List<Node> pair = new ArrayList<Node>( 2 );
						pair.add( oldAttr ); pair.add( currAttr );
						de.addAttrChanges( pair );
					}
			}
			else if ( currAttr != null ) {
				List<Node> pair = new ArrayList<Node>( 2 );
				pair.add( null ); pair.add( currAttr );
				de.addAttrChanges( pair );
			}
		}
		
		List<List<Node>> attrChanges = de.getAttrChanges();		
		//do add, delete, modify
		for ( int i=0; i < attrChanges.size(); i++ ) {
			List<Node> pair = attrChanges.get( i );
			Node oldAttr = (Node) pair.get( 0 );
			Node currAttr = (Node) pair.get( 1 );
			if ( oldAttr != null ) {
				if ( currAttr == null )
					cloneNode = (Element) delete( oldAttr );
				else {
					NodeImpl newAttr = createClone(oldAttr);
					newAttr.copyTokens ( currAttr );
					cloneNode = (Element) modify(oldAttr, newAttr);
					pair.remove( 1 );
					pair.add( 1, newAttr );
				}
			}
			else if ( currAttr != null ) {
				NodeImpl newAttr = createCopy(currAttr);
				cloneNode = (Element) add(cloneNode, newAttr, posMap.get(currAttr).intValue());
				pair.remove( 1 );
				pair.add( 1, newAttr );
			}	
		}		
		return cloneNode;
	}	

	private NodeImpl createCopy(final Node currNode) {
		NodeImpl newNode = (NodeImpl) ((NodeImpl)currNode).copy();
		return newNode;
	}
	
	private NodeImpl createClone(final Node oldNode) {
		NodeImpl newNode = (NodeImpl) ((NodeImpl)oldNode).clone( false, false, false );
		return newNode;
	}	
	
	private Node add(Node parent, Node newNode, int pos) {
		Node newParent = DiffFinder.getPathToRoot(parent).get(0);
		Node cloneParent = model.add ( newParent, newNode, pos );
		return cloneParent;
	}
		
	private Node add(Node parent, Node newNode, int pos, DiffEvent de) {
		Node cloneParent = add ( parent, newNode, pos );
		//de.setNewPathToRoot( DiffFinder.getPathToRoot( newNode ) );
		de.setNewNode( newNode );
		return cloneParent;
	}	
	
	private Node delete(Node oldNode) {
		Node cloneParent = model.delete ( oldNode );
		return cloneParent;
	}
	
	private Node delete(Node oldNode, DiffEvent de) {
		//de.setOldPathToRoot( DiffFinder.getPathToRoot( oldNode ) );
		de.setOldNode( oldNode );
		Node cloneParent = delete ( oldNode );
		return cloneParent;
	}
	
	private Node modify(Node oldNode, Node newNode) {
		Node cloneParent = model.modify ( oldNode, newNode );
		return cloneParent;		
	}
	
	private Node modify(Node oldNode, Node newNode, DiffEvent de) {
		//de.setOldPathToRoot( DiffFinder.getPathToRoot( oldNode ) );
		de.setOldNode( oldNode );
		Node cloneParent = modify ( oldNode, newNode );
		//de.setNewPathToRoot( DiffFinder.getPathToRoot( newNode ) );
		de.setNewNode( newNode );
		return cloneParent;
	}
	
	private Node findNode(Node root, Node node) {
		FindVisitor fv = new FindVisitor();
		return fv.find( (Document) root, node.getId() );		
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////////////////////////////

	private ElementIdentity eID;
	
	private XDMModel model;		
}
