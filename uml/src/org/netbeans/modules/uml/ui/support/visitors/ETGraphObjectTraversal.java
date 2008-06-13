/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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



package org.netbeans.modules.uml.ui.support.visitors;

import java.util.Collections;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

import java.util.Iterator;
import java.util.List;

/**
 * @author KevinM
 * Traverses the graphs nodes, edges, nodeLabels and edgeLabels.
 * Please note you should only use this in non destructive operations. 
 */
public class ETGraphObjectTraversal
{
//	protected ETGraph m_graph;
	ETList<IETGraphObjectVisitor> listeners = new ETArrayList<IETGraphObjectVisitor>();
	
   /**
    * 
    */ //TODO
//   public ETGraphObjectTraversal(ETGraph graph)
//   {
//      super();
//		m_graph = graph;
//   }

	/*
	 * Returns true if all graph objects have been visited.
	 */ //TODO
	public boolean traverse()
	{
//		return m_graph != null && visitThese(m_graph.nodes()) && visitThese(m_graph.edges()) && 
//			visitThese(m_graph.nodeLabels()) && visitThese(m_graph.edgeLabels());
	return false;
        }
	
	/*
	 * Adds a visiter to the listener list.
	 */
	public void addVisitor(IETGraphObjectVisitor visiter)
	{
		listeners.add(visiter);
	}
	
	/*
	 * Removes a visiter from the visiter list.
	 */
	public void removeVisitor(IETGraphObjectVisitor visiter)
	{
		listeners.remove(visiter);
	}
	
	/*
	 * Returns true if we should continue with the traversal.
	 */ //TODO
//	protected boolean visit(IETGraphObject object)
//	{
//		Iterator<IETGraphObjectVisitor> iter = listeners.iterator();
//		while(iter.hasNext())
//		{
//			if (!iter.next().visit(object))
//			{
//				return false;
//			}
//		}
//		return true;
//	}
	
	/*
	 * Returns true if we should continue with the traversal.
	 */ //TODO
//	protected boolean visitThese(List objectList)
//	{
//		IteratorT<IETGraphObject> iter = new IteratorT<IETGraphObject>(objectList);
//		while (iter.hasNext())
//		{
//			if (!visit(iter.next()))
//			{
//				return false;
//			}			
//		}
//		return true;
//	}
    
    //TODO
    public boolean traverseInReverseOrder()
	{
//        List nodes = new ETArrayList(m_graph.nodes());
//        Collections.reverse(nodes);
//		return m_graph != null && visitThese(nodes) && visitThese(m_graph.edges()) && 
//			visitThese(m_graph.nodeLabels()) && visitThese(m_graph.edgeLabels());
        return false;
    }
}
