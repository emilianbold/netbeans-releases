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

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.diff.DiffEvent.NodeType;
import org.netbeans.modules.xml.xdm.diff.DiffEvent.Type;
import org.netbeans.modules.xml.xdm.diff.ElementIdentity.ELEM_ATTR_COMPARE;
import org.netbeans.modules.xml.xdm.diff.ElementIdentity.TEXT_COMPARE;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * This class can be used to find difference between 2 XML and returns 
 * List<DiffEvent>
 *
 * @author Ayub Khan
 */
public class DiffFinder 
{
	
	public DiffFinder()
	{
		throw new IllegalArgumentException(" use constructor with element id map as argument");
	}
	
	public DiffFinder(ElementIdentity eID)
	{	
		this.eID = eID;
	}
	
	/*
	 * Process document
	 *
	 * @param node - an XML document
	 */
	public List<DiffEvent> findDiff(Document d1, Document d2) 
	{
		this.oldDoc = d1;
		this.newDoc = d2;
		
		List<DiffEvent> deList = new ArrayList<DiffEvent>();
		compareChildren((Node)d1, (Node)d2, deList);
		
		//remove any (sequential) position change events
		if( deList.size() > 0 ) {
			//System.out.println("\n\nbefore optimize: "+deList.size());
			//printDeListPosition( deList );
			List<DiffEvent> optimizedList = optimize(deList);
			//System.out.println("\n\nafter optimize: "+optimizedList.size());
			//printDeListPosition( optimizedList );
			return optimizedList;
		}
		
		return deList;
	}

	public static void printDeList(final List<DiffEvent> deList) throws DOMException {
		for ( int i=0; i < deList.size(); i++ ) {
			System.out.print("\n\n========================================================");			
			System.out.print(i+":\n");
			printDe(deList.get( i ));
		}
	}

	public static void printDe(final DiffEvent de) throws DOMException {
		DiffEvent.NodeType nodeType = de.getNodeType();
		Node node = de.getOldPathToRoot().get(0);
		if ( de.getType() == DiffEvent.Type.ADD )
			node = de.getNewNode();//de.getNewPathToRoot().get(0);
		String id = "";
		if ( nodeType == DiffEvent.NodeType.ELEMENT && node.getAttributes().getLength() > 0 )
			id = node.getAttributes().item(0).getNodeValue();
		else if ( nodeType == DiffEvent.NodeType.TEXT )
			id = "["+node.getNodeValue()+"]";
		else if ( nodeType == DiffEvent.NodeType.WHITE_SPACE )
			id = "["+node.getNodeValue().replaceAll("\n", "~")+"]";
		System.out.print("\n\ntype: " + de.getType() + 
			" "+node.getNodeName()+": "+id);
		System.out.print( " (" + de.getOldNodePosition() + "," + 
			de.getNewNodePosition() + ":"+de.getPositionFromSiblingBefore()+")");
		
		id = "";
		Node siblingBefore = de.getSiblingBefore();
		if ( siblingBefore instanceof Element && siblingBefore.getAttributes().getLength() > 0 )
			id = siblingBefore.getAttributes().item(0).getNodeValue();
		else if ( siblingBefore instanceof Text )
			id = "["+siblingBefore.getNodeValue().replaceAll("\n", "~")+"]";
		System.out.println("\n\nsiblingBefore: " + (siblingBefore!=null?siblingBefore.getNodeName():"null")+": "+id+"\n");			
	}
	
	protected void compareChildren(Node parent1, Node parent2, List<DiffEvent> deList) {
		NodeList p1ChildNodes = parent1.getChildNodes();
		NodeList p2ChildNodes = parent2.getChildNodes();
		
		if ( p1ChildNodes == NodeListImpl.EMPTY && 
				p2ChildNodes == NodeListImpl.EMPTY )
			return;
		
		List<Node> p2ChildList = new ArrayList<Node>(p2ChildNodes.getLength());		
		HashMap<Node, Integer> posMap = new HashMap<Node, Integer>();//check initial size
		HashMap<Node, Node> nbrMap = new HashMap<Node, Node>();//"
		Node siblingBefore = null;
		for ( int i = 0; i < p2ChildNodes.getLength() ; i++ ) {
			Node child = (Node) p2ChildNodes.item(i);
			p2ChildList.add( child );
			posMap.put( child, new Integer(i) );
			siblingBeforeMap.put( child, siblingBefore );
			siblingBefore = child;
		}
		List<List<Integer>> pairList = new ArrayList<List<Integer>>();
		List<Node> foundList = new ArrayList<Node>();
		if ( p1ChildNodes != null ) {
			int length = p1ChildNodes.getLength();
			siblingBefore = null;
			for ( int i = 0; i < length; i++ ) {
				Node child = (Node) p1ChildNodes.item(i);
				siblingBeforeMap.put( child, siblingBefore );
				siblingBefore = child;
				Node foundNode = null;
				if ( child instanceof Element ) {
					foundNode = findMatch( (Element)child, p2ChildList, ElementIdentity.ELEM_ATTR_COMPARE.MATCH_ORDER );
				}
				else if ( child instanceof Text ) {
					foundNode = findMatch( (Text)child, p2ChildList, ElementIdentity.TEXT_COMPARE.MATCH );
				}
				if( foundNode == null ) {
					String value = getValue(parent1);
					markDelete( parent1, child, i, siblingBeforeMap.get( child ), deList );
				}
				else {
					compareNodeMap.put( foundNode, child);
					foundList.add( foundNode );
					int nextIndex = posMap.get(foundNode).intValue() + 1;
					p2ChildList.remove( foundNode );//use iterator
					List<Integer> pair = new ArrayList<Integer>(2);
					pair.add( new Integer(i) ); pair.add( posMap.get(foundNode) );
					pairList.add( pair );
				}
			}
			
			for ( int i = 0; i < p2ChildList.size() ; i++ ) {
				Node child = (Node) p2ChildList.get(i);	
				List<Node> siblingBeforeList = new ArrayList<Node>();
				int relativePos = getSiblingInfoFor( child, p2ChildNodes, foundList, siblingBeforeList );	
				assert siblingBeforeList.size() <= 1 ;
				Node originalSiblingBefore = null;				
				if ( !siblingBeforeList.isEmpty() ) {
					if ( compareNodeMap.get( siblingBeforeList.get( 0 ) ) != null )
						originalSiblingBefore = compareNodeMap.get(  siblingBeforeList.get( 0 ) );
				}

				int absolutePos = posMap.get( child );
				markAdd( parent1, child, absolutePos, relativePos, originalSiblingBefore, deList );
			}	
			
			//sort match nodes by position (toPos)
			//All deletes are removed and later inserted appropriately
			int p1Size = parent1.getChildNodes().getLength();
			for ( int i=0; i < pairList.size(); i++ ) {	
				int minPos = p1Size;
				int index = -1;
				for ( int j=i; j < pairList.size(); j++ ) {
					List<Integer> pair = pairList.get(j);
					int px2 = pair.get(1).intValue();
					if ( px2 < minPos ) {
						minPos = px2;
						index = j;
					}
				}
				if ( index != -1 && index > i ) {
					List<Integer> pl = pairList.remove( index );
					pairList.add( i, pl );
				}
			}

			for ( int i=0; i < pairList.size(); i++ ) {
				List<Integer> pair = pairList.get(i);
				int px1 = pair.get(0).intValue();
				int px2 = pair.get(1).intValue();
				Node p1 = (Node) parent1.getChildNodes().item(px1);
				Node p2 = (Node) parent2.getChildNodes().item(px2);
				if ( p1 instanceof Element/* && p2 instanceof Element*/ ) {
					boolean changed = checkChange( p1, p2, px1, px2 );
					if ( changed ) {
						List<Node> siblingBeforeList = new ArrayList<Node>();
						int relativePos = getSiblingInfoFor( p2, p2ChildNodes, foundList, siblingBeforeList );	

						Node originalSiblingBefore = null;				
						if ( !siblingBeforeList.isEmpty() ) {
							if ( compareNodeMap.get( siblingBeforeList.get( 0 ) ) != null )
								originalSiblingBefore = compareNodeMap.get(  siblingBeforeList.get( 0 ) );	
						}
						markChange( parent1, p1, p2, px1, px2, relativePos, originalSiblingBefore, deList );
					}
					//Since p1 and p2 are similar nodes, now compare their childrens 
					compareChildren( p1, p2, deList );
				}
			}
		}	
	}	

	private DiffEvent.NodeType getNodeType(final Node child) throws DOMException {
		DiffEvent.NodeType nodeType = DiffEvent.NodeType.ELEMENT;
		if ( child instanceof Text )
			if ( child.getNodeValue().trim().equals("") )
				nodeType = DiffEvent.NodeType.WHITE_SPACE;
			else
				nodeType = DiffEvent.NodeType.TEXT;
		return nodeType;
	}
	
	private int getSiblingInfoFor(Node child, NodeList p2ChildNodes, List<Node> foundList, List<Node> siblingBeforeList) {
		int relativePos = 0;
		for ( int j=0; j < p2ChildNodes.getLength(); j++ ) {
			Node node = (Node) p2ChildNodes.item(j);
			if ( node == child ) {
				if ( j-1 >= 0 ) {
					for ( int k=j-1; k >= 0 ; k-- ) {//go backwards and find a node that hasn't changed its pos
						if ( p2ChildNodes.item( k ) instanceof Element &&
								foundList.contains( p2ChildNodes.item( k ) ) ) {
							siblingBeforeList.add( (Node) p2ChildNodes.item( k ) );
							relativePos = j-k;
							break;
						}
					}
				}
				if ( !siblingBeforeList.isEmpty() )
					break;
			}
		}
		return relativePos;
	}

	private boolean checkChange(final Node p1, final Node p2, final int px1, final int px2) {		
		//Now check Element or its Attribute's tokens
		if ( px1 != px2 || 
				checkChange( p1, p2) ) {
			return true;
		}
		return false;
	}
	
	public static boolean checkChange(final Node p1, final Node p2) {		
		//Now check Element or its Attribute's tokens
		if ( !compareTokenEquals( p1, p2) ) {
			return true;
		}
		return false;
	}	

	private static boolean compareTokenEquals(Node p1, Node p2) {
		List<Token> t1List = ((NodeImpl)p1).getTokens();
		List<Token> t2List = ((NodeImpl)p2).getTokens();
		
		boolean status = compareTokenEquals ( t1List, t2List );
		if ( !status ) return false;
		
		if ( p1 instanceof Element ) {
			NamedNodeMap nm1 = p1.getAttributes();
			NamedNodeMap nm2 = p2.getAttributes();
			if( nm1.getLength() != nm2.getLength() )
				return false;

			int count = 0;
			for ( int i = 0; i < nm1.getLength(); i++ ) {
				t1List = ((NodeImpl)nm1.item(i)).getTokens();
				Node attr2 = (Node) nm2.getNamedItem(nm1.item(i).getNodeName());
				if ( attr2 == null ) return false;
				count++;
				t2List = ( (NodeImpl) attr2 ).getTokens();
				status = compareTokenEquals ( t1List, t2List );
				if ( !status ) return false;
			}
		}
		
		return true;
	}	
	
	private static boolean compareTokenEquals(List<Token> t1List, List<Token> t2List) {
		if( t1List.size() != t1List.size() )
			return false;
		
		//compare element tokens
		for ( int i=0; i<t1List.size(); i++ ) {
			Token t1 = t1List.get( i );
			Token t2 = t2List.get( i );
			if ( t1.getValue().intern() !=  t2.getValue().intern() )
				return false;
		}
		
		return true;
	}	
	
	public Node findMatch(Element child, List<Node> childNodes, ElementIdentity.ELEM_ATTR_COMPARE matchCriteria) {
		if ( childNodes != null ) {
			for ( int i=0; i<childNodes.size(); i++ ) {
				Node otherChild = (Node) childNodes.get(i);
				if ( otherChild instanceof Element ) {
					ElementIdentity.ELEM_ATTR_COMPARE status = eID.compareElement( child, (Element) otherChild );
					if ( status == matchCriteria )
						return otherChild;
				}
			}
		}
		return null;
	}
	
	public Node findMatch(Text child, List<Node> childNodes, ElementIdentity.TEXT_COMPARE matchCriteria) {
		if ( childNodes != null ) {
			for ( int i=0; i<childNodes.size(); i++ ) {
				Node otherChild = (Node) childNodes.get(i);
				if ( otherChild instanceof Text &&
						compareText( child, (Text) otherChild ) == matchCriteria )
					return otherChild;
			}
		}
		return null;
	}	
	
	private DiffEvent createAddEvent(Node parent1, Node n, int absolutePos, int posFromSibling, Node siblingBefore, List<DiffEvent> deList) {
		if ( n == null )
			throw new IllegalArgumentException("argument null");
		List<Node> parentPathToRoot = getPathToRoot( parent1 );
		return new DiffEvent( DiffEvent.Type.ADD, getNodeType( n ), 
				parent1, null, n, -1, absolutePos, posFromSibling, siblingBefore, 
				parentPathToRoot, this.oldDoc, this.newDoc );
	}
	
	private DiffEvent createDeleteEvent(Node parent1, Node n, int pos, Node siblingBefore, List<DiffEvent> deList) {
		if ( n == null )
			throw new IllegalArgumentException("argument null");
		List<Node> nodePathToRoot = getPathToRoot( n );	
		return new DiffEvent( DiffEvent.Type.DELETE, getNodeType( n ), 
				parent1, n, null, pos, -1, -1, siblingBefore, 
				nodePathToRoot, this.oldDoc, this.newDoc ) ;
	}
	
	private DiffEvent createChangeEvent(Node parent1, Node n1, Node n2, int n1Pos, int n2Pos, int posFromSibling, Node siblingBefore, List<DiffEvent> deList) {
		if ( n1 == null || n2 == null )
			throw new IllegalArgumentException("argument null");
		List<Node> nodePathToRoot = getPathToRoot( n1 );
		//System.out.println( " n1: [" + n1.getLocalName() + "] change to: [" + n2.getLocalName()+"]");
		assert n1.getLocalName().equals(n2.getLocalName());
		/*if( n1.getNamespaceURI() != null ) {
			//System.out.println( " ns: [" + n1.getLocalName() + "] ns2: [" + n2.getLocalName()+"]");		
			assert n1.getNamespaceURI().equals(n2.getNamespaceURI());
		}*/
		return new DiffEvent(DiffEvent.Type.CHANGE, getNodeType( n1 ), 
				parent1, n1, n2, n1Pos, n2Pos, posFromSibling, siblingBefore, 
				nodePathToRoot, this.oldDoc, this.newDoc );		
	}
	
	private void markAdd(Node parent1, Node n, int absolutePos, int posFromSibling, Node siblingBefore, List<DiffEvent> deList) {
		deList.add( createAddEvent( parent1, n, absolutePos, posFromSibling, siblingBefore, deList ) );
	}	

	private void markDelete(Node parent1, Node n, int pos, Node siblingBefore, List<DiffEvent> deList) {
		deList.add( createDeleteEvent( parent1, n, pos, siblingBefore, deList ) );
	}

	private void markChange(Node parent1, Node n1, Node n2, int n1Pos, int n2Pos, int posFromSibling, Node siblingBefore, List<DiffEvent> deList) {
		deList.add( createChangeEvent( parent1, n1, n2, n1Pos, n2Pos, posFromSibling, siblingBefore, deList ) );		
	}
	
	private String getValue(final Node n) throws DOMException {
		String value = "";
		if(n instanceof Element) {
			NamedNodeMap attrs = n.getAttributes();
			if(attrs != null && attrs.getLength() > 0)
				value = attrs.item(0).getNodeValue();
		}
		else if(n instanceof Text)
			value = n.getNodeValue();
		return value;
	}
	
    public static List<Node> getPathToRoot(Node node) {
		assert node.getOwnerDocument() != null;
        List<Node> pathToRoot = new PathFromRootVisitor().findPath(node.getOwnerDocument(), node);
        assert pathToRoot != null && pathToRoot.size() > 0;
        return pathToRoot;
    }
	
	public List<DiffEvent> optimize(List<DiffEvent> deList) {
		List<DiffEvent> optimizedList = new ArrayList<DiffEvent>();
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
			
			//Modify add/delete to change x, -y && -y, x 
			/*combineAddDeleteEvents ( parent, childDeList , deList );
			//System.out.println("\n\nafter combineAddDeleteEvents deList: "+deList.size());
			//printDeListPosition( deList );
			//System.out.println("\n\nafter combineAddDeleteEvents childDeList: "+childDeList.size());
			//printDeListPosition( childDeList );*/
			
			sortByPosition( parent, childDeList );
			Node cloneParent = null;
			for ( int i=0; i < childDeList.size(); i++ ) {
				DiffEvent de = childDeList.get(i);
				DiffEvent.Type type = de.getType();
				modifyPositionFromIndex( i+1, childDeList, de );				
			}
			
			//Now remove position change events
			for ( int i=0; i < childDeList.size(); i++ ) {
				DiffEvent de = childDeList.get(i);
				DiffEvent.Type type = de.getType();
				int px1 = de.getOldNodePosition();
				int px2 = de.getNewNodePosition();
				if ( !( px1 == px2 && //skip unchanged nodes
						compareTokenEquals( de.getOldNode(), de.getNewNode() /*de.getOldPathToRoot().get(0), de.getNewPathToRoot().get(0)*/ ) ) ) {
					optimizedList.add( de );
				}
			}
		}	
		return optimizedList;
	}
	
	private void sortByPosition(Node parent1, List<DiffEvent> deList) {
		//sort match nodes by position
		//System.out.println("\nsort before: ");
		//printDeListPosition(deList);
		int p1Size = parent1.getChildNodes().getLength();
		int size = deList.size();
		for ( int i=0; i < size; i++ ) {
			int minPos = p1Size;
			int index = -1;
			for ( int j=i; j < size; j++ ) {
				DiffEvent de = deList.get(j);
				int px2 = de.getNewNodePosition();
				if ( px2 < minPos ) {
					minPos = px2;
					index = j;
				}
			}
			if ( index != -1 && index > i ) {
				DiffEvent de = deList.remove( index );
				deList.add( i, de );
			}
		}
		//System.out.println("\nsort after2: ");
		//printDeListPosition(deList);		
	}

	public static void printDeListPosition(final List<DiffEvent> deList) {
		for ( int i=0; i < deList.size(); i++ ) {
			DiffEvent de = deList.get( i );
			Node n = de.getType()==DiffEvent.Type.ADD?
				de.getNewNode():de.getOldNode();//de.getNewPathToRoot().get(0):de.getOldPathToRoot().get(0);
			String name = n.getNodeName();
			if ( n instanceof Element  && n.getAttributes().getLength() > 0 )
				name = name + "("+n.getAttributes().item(0).getNodeValue()+")";
			System.out.print( name + ":("+ de.getOldNodePosition() + "," + de.getNewNodePosition() + ")");
		}
	}	

	private void modifyPositionFromIndex(int index, List<DiffEvent> childDeList, DiffEvent de) {
		//System.out.println("\nmp before: ");
		//printDeListPosition( childDeList );		
		int x = de.getOldNodePosition();
		int y = de.getNewNodePosition();
		DiffEvent.Type type = de.getType();
		for ( int i=index; i < childDeList.size(); i++ ) {
			DiffEvent cde = childDeList.get(i);
			int p1 = cde.getOldNodePosition();
			int p2 = cde.getNewNodePosition();
			
			if ( p1 == p2 ) continue;//skip no position change needed
			
			if( type == DiffEvent.Type.ADD && 
				x==-1 && y>=0 && y<=p1)
			{
				cde.setOldNodePosition(p1+1);
			}
			else if( type == DiffEvent.Type.DELETE && 
				x>=0 && y==-1 && x<=p1)
			{
				cde.setOldNodePosition(p1-1);
			}		
			else if( type == DiffEvent.Type.CHANGE && 
				x!=y && p1!=-1 && y<=p2)
			{
				cde.setOldNodePosition(p1+1);
			}
		}
		//System.out.println("\nmp after: ");
		//printDeListPosition( childDeList );		
	}	
	
	private void combineAddDeleteEvents(Node parent1, List<DiffEvent> childDeList, List<DiffEvent> deList) {
		//sort match nodes by position
		//System.out.println("\nsort before: ");
		//printDeListPosition( deList );
		List<DiffEvent> comparedList = new ArrayList<DiffEvent>();
		List<List<DiffEvent>> pairList = new ArrayList<List<DiffEvent>>();
		for ( int i=0; i < childDeList.size(); i++ ) {
			DiffEvent de1 = childDeList.get(i);
			if ( !( de1.getNodeType() == DiffEvent.NodeType.TEXT ||
					de1.getNodeType() == DiffEvent.NodeType.WHITE_SPACE ) )
				continue;
			Node p1 = de1.getOldPathToRoot().get(0);
			int x = de1.getOldNodePosition();
			int y = de1.getNewNodePosition();
			for ( int j=i+1; j < childDeList.size(); j++ ) {
				DiffEvent de2 = childDeList.get(j);
				if ( !( de2.getNodeType() == DiffEvent.NodeType.TEXT ||
						de2.getNodeType() == DiffEvent.NodeType.WHITE_SPACE ) )
					continue;
				Node p2 = de2.getOldPathToRoot().get(0);
				if ( comparedList.contains( de2 ) ) continue;
				int px1 = de2.getOldNodePosition();
				int px2 = de2.getNewNodePosition();		
				if ( x == px2 && y == px1 &&
						!compareTokenEquals (p1, p2 ) ) {
					comparedList.add( de2 );
					List<DiffEvent> l = new ArrayList<DiffEvent>();
					l.add( de1 );
					l.add( de2 );
					pairList.add( l );
					continue;
				}
			}
		}
		for ( List<DiffEvent> pair:pairList ) {
			DiffEvent de1 = pair.get( 0 );
			DiffEvent de2 = pair.get( 1 );
			if ( de1.getType() == DiffEvent.Type.ADD ) {
				DiffEvent tmp = de1;
				de1 = de2;
				de2 = tmp;
			}

			Node p1 = de1.getOldNode();//de1.getOldPathToRoot().get( 0 );
			Node p2 = de2.getNewNode();//de2.getNewPathToRoot().get( 0 );
			int px1 = de1.getOldNodePosition();
			int px2 = de2.getNewNodePosition();
			int posFromSiblingBefore = de2.getPositionFromSiblingBefore();
			Node siblingBefore = de2.getSiblingBefore();
			
			int grandPos = deList.indexOf( de1 );
			int pos = childDeList.indexOf( de1 );
			deList.remove( de1 );
			deList.remove( de2 );
			DiffEvent combinedEvent = createChangeEvent( parent1, p1, p2, px1, px2, posFromSiblingBefore, siblingBefore, deList );
			deList.add( grandPos, combinedEvent);
			
			childDeList.remove( de1 );
			childDeList.remove( de2 );
			childDeList.add( pos, combinedEvent );
		}
	}
	
	public TEXT_COMPARE compareText(Text n1, Text n2) {
		if( n1.getNodeValue().trim().equals("") && n2.getNodeValue().trim().equals("") )
			return compareWhiteSpaces( n1, n2 );
		else
			return compareTextByValue( n1, n2 );
	}

	private TEXT_COMPARE compareWhiteSpaces(Text n1, Text n2) {
		Node nodeBefore1 = siblingBeforeMap.get( n1 );
		Node nodeBefore2 = siblingBeforeMap.get( n2 );
		boolean siblingCompare = false;
		if( nodeBefore1 == null && nodeBefore2 == null )
			siblingCompare = true;
		else if ( nodeBefore1 instanceof Element && nodeBefore2 instanceof Element &&
				eID.compareElement( (Element) nodeBefore1, (Element) nodeBefore2) == ElementIdentity.ELEM_ATTR_COMPARE.MATCH_ORDER )
			siblingCompare = true;
		else if ( nodeBefore1 instanceof Text && nodeBefore2 instanceof Text &&
				nodeBefore1.getNodeValue().intern() == nodeBefore2.getNodeValue().intern() )
			siblingCompare = true;
		
		if ( siblingCompare )
			return compareTextByValue( n1, n2 );

		return TEXT_COMPARE.NO_MATCH;
	}

	private TEXT_COMPARE compareTextByValue(Text n1, Text n2) {
		if ( n1.getNodeValue().intern() == n2.getNodeValue().intern() )
			return TEXT_COMPARE.MATCH;
		else
			return TEXT_COMPARE.NO_MATCH;
	}
	
	public static void printChildren(final org.w3c.dom.Node parent) 
		throws DOMException 
	{
		NodeList childNodes=parent.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++)
		{
			NodeImpl child=(NodeImpl)childNodes.item(i);
			String aName="";
			if(child.getAttributes()!=null && 
					child.getAttributes().getLength()>0)
				aName=child.getAttributes().item(0).getNodeValue();
			System.out.println("child node: ("+child.getNodeName()+", "+
					aName+"): "+child.getId());
		}		
	}	
	
	////////////////////////////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////////////////////////////

	private ElementIdentity eID;
	
	private HashMap<Node, Node> compareNodeMap = new HashMap<Node, Node>();
	
	private HashMap<Node, Node> siblingBeforeMap = new HashMap<Node, Node>();

	private Document oldDoc;

	private Document newDoc;
}
