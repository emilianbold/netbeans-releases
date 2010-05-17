/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class RelationshipEventsHelper {

	private IRelationValidatorEventDispatcher m_Help = null;
	private IElement m_Relation = null;
	private IRelationProxy m_RelProxy = null;
	private boolean m_Proceed = true;
	private boolean m_FireEvents = true;

	/**
	 * 
	 */
	private RelationshipEventsHelper() {
		super();
	}
	
	public RelationshipEventsHelper(String relType)
	{
		prepareDispatcher();
		initialize(relType);
	}

	public RelationshipEventsHelper(IElement elem)
	{
		m_Relation = elem;
		prepareDispatcher();
		initialize(null);
	}

	/**
	 *
	 * Establishes the RelationProxy
	 *
	 * @param connectionType[in] The type of the connection. Default is ""
	 *
	 * @return HRESULT
	 *
	 */
	private void initialize(String connectionType)
	{
		m_RelProxy = new RelationProxy();
		if (connectionType != null)
		{
			m_RelProxy.setConnectionElementType(connectionType);
		}
		else
		{
			m_RelProxy.setConnection(m_Relation);
		}
	}

	/**
	 *
	 * Called when one of the ends of a relation is being added to the relation.
	 *
	 * @param fromEnd[in] If not 0, this is the element being added to the relation.
	 * @param toEnd[in] If not 0, this is the element being added to the relation.
	 *
	 * @return - true if the post event should be dispatched, else
	 *         - false if the post event should not be dispatched
	 *
	 */
	public boolean firePreEndAdd(IElement fromEnd, IElement toEnd)
	{
		m_Proceed = true;
		if (m_RelProxy != null && m_Help != null)
		{
			IEventPayload payload = preparePayload("PreRelationEndAdded", fromEnd, toEnd);
			m_Proceed = m_Help.firePreRelationEndAdded(m_RelProxy, payload);
		}
		return m_Proceed;
	}

	/**
	 *
	 * Called when one of the ends of a relation has been added to the relation.
	 *
	 * @return HRESULT
	 *
	 */
	public void fireEndAdded()
	{
		if (m_Proceed && m_Help != null)
		{
			IEventPayload payload = m_Help.createPayload("RelationEndAdded");
			m_Help.fireRelationEndAdded(m_RelProxy, payload);
		}
	}
	
	/**
	 *
	 * Called when one of the ends of a relation is being modified. This is most
	 * commonly called when a single end of a relationship such as Generalization is
	 * being changed from one element to another.
	 *
	 * @param query[in] The EndQuery object used to find the xml attribute name to query
	 * @param fromEnd[in] If not 0, this is the element being set on this relation.
	 * @param toEnd[in] If not 0, this is the element being set on this relation.
	 *
	 * @return - true if the post event should be dispatched, else
	 *         - false if the post event should not be dispatched
	 *
	 */
	public boolean firePreEndModified(String query, IElement fromEnd, IElement toEnd)
	{
		m_Proceed = true;
		if (m_RelProxy != null && m_Help != null)
		{
			// Check to see if we really need to fire this event at all. We don't want
			// to fire these events if the value found in attrName is "" ( this is certainly
			// a new element that is currently being populated ) or if the value of attrName
			// is the XMI.ID of the from or to end, whichever is not 0.
			if (proceed(query, fromEnd, toEnd))
			{
				IEventPayload payload = preparePayload("PreRelationEndModified", fromEnd, toEnd);
				m_Proceed = m_Help.firePreRelationEndModified(m_RelProxy, payload);
			}
		}
		return m_Proceed;
	}
	
	/**
	 *
	 * Called when one of the ends of a relation is being modified. This is most
	 * commonly called when a single end of a relationship such as Generalization is
	 * being changed from one element to another.
	 *
	 * @return HRESULT
	 *
	 */
	public void fireEndModified()
	{
		if (m_RelProxy != null && m_Help != null && m_Proceed && m_FireEvents)
		{
			IEventPayload payload = m_Help.createPayload("RelationEndModified");
			m_Help.fireRelationEndModified(m_RelProxy, payload);
		}
	}
	
	/**
	 *
	 * Called when one of the ends of a relation is being removed. 
	 *
	 * @param fromEnd[in] If not 0, this is the element being removed from this relation.
	 * @param toEnd[in] If not 0, this is the element being removed from this relation.
	 *
	 * @return - true if the post event should be dispatched, else
	 *         - false if the post event should not be dispatched
	 *
	 */
	public boolean firePreEndRemoved(IElement fromEnd, IElement toEnd)
	{
		m_Proceed = true;
		if (m_RelProxy != null && m_Help != null)
		{
			IEventPayload payload = preparePayload("PreRelationEndRemoved", fromEnd, toEnd);
			m_Proceed = m_Help.firePreRelationEndRemoved(m_RelProxy, payload);
		}
		return m_Proceed;
	}
	
	/**
	 *
	 * Called when one of the ends of a relation is being removed. 
	 *
	 * @return HRESULT
	 *
	 */
	public void fireEndRemoved()
	{
		if (m_Proceed && m_Help != null)
		{
			IEventPayload payload = m_Help.createPayload("RelationEndRemoved");
			m_Help.fireRelationEndRemoved(m_RelProxy, payload);
		}
	}

	/**
	 *
	 * Creates the payload to be included in the dispatched event, as well as sets the to and from
	 * elements of the RelationProxy
	 *
	 * @param triggerName[in] The name of the trigger to use for payload creation
	 * @param fromEnd[in] If not 0, this is the element being removed / added / or modified.
	 * @param toEnd[in] If not 0, this is the element being removed / added / or modified.
	 * @param payload[out] The new payload
	 *
	 * @return HRESULT
	 *
	 */
	protected IEventPayload preparePayload(String triggerName, IElement fromEnd, IElement toEnd)
	{
		IEventPayload payload = null;
		if( m_RelProxy != null && m_Help != null)
		{
		   m_RelProxy.setFrom(fromEnd);
		   m_RelProxy.setTo( toEnd );
		   payload = m_Help.createPayload(triggerName);
		}
		return payload;
	}
	
	/**
	 *
	 * Determines whether or not a modify event should go out or not.
	 * There are a couple of assumptions being made:
	 * 1) The pre/ post modify calls on this helper object are called
	 *    for single end relationships, such as Generalization.
	 * 2) The attribute name passed in resolves to an XMI id value
	 *    that can be matched against the ID of one of the elements
	 *    coming in.
	 *
	 * @param query[in] The EndQuery object used to find the xml attribute name to query
	 * @param fromEnd[in] If not 0, this is the element being modified.
	 * @param toEnd[in] If not 0, this is the element being modified.
	 *
	 * @return - true if it is ok to proceed with event dispatch, else
	 *         - false if it is no ok. This will only occur if the 
	 *           current value of the attrName xml attribute is "" or if it
	 *           matches the id of one of the ends coming in.
	 *
	 */
	protected boolean proceed(String query, IElement fromEnd, IElement toEnd)
	{
		m_FireEvents = true;
		if (m_Relation != null)
		{
			Node node = getCurNode();
			if (node != null)
			{
				IElement end = (fromEnd != null) ? fromEnd : toEnd;
				if (end != null )
				{
					String attrVal = XMLManip.getAttributeValue(node, query);
					if(attrVal != null && attrVal.trim().length() > 0)
					{
						m_FireEvents = proceed( node, query, end );
					}
					else
					{					
					org.dom4j.Node element = XMLManip.selectSingleNode(node, query);
					if (element != null)
					{
						m_FireEvents = proceed( element, "xmi.id", end );
					}
               else
               {
                  m_FireEvents = false;
               }
				}
				}
				else
				{
					m_FireEvents = false;
				}
			}
		}
		return m_FireEvents;
	}
	
	/**
	 *
	 * Determines whether or not the attribute name on the passed
	 * in node contains a value that matches the xmi.id of end.
	 *
	 * @param node[in] The node to check
	 * @param attrName[in] The xml attribute value to check
	 * @param end[in] The Element whose xmi.id is to be matched
	 *
	 * @return - true if ok to proceed, else false
	 *
	 */
//	protected boolean proceed( Node node, String attrName, IElement end )
//	{
//		boolean proceed = true;
//		if (node != null && end != null)
//		{
//			String value = XMLManip.getAttributeValue(node, attrName);
//			if (value != null && value.length() > 0)
//			{
//				String endId = end.getXmiId();
//				if (endId.equals(value))
//				{
//					proceed = false;
//				}
//			}
//			else
//			{
//				proceed = false;
//			}
//		}
//		return proceed;
//	}
	
	protected boolean proceed( org.dom4j.Node node, String attrName, IElement end )
	{
		boolean proceed = true;
		if (node != null && end != null)
		{
			String value = XMLManip.getAttributeValue(node, attrName);
			if (value != null && value.length() > 0)
			{
				String endId = end.getXMIID();
				if (endId.equals(value))
				{
					proceed = false;
				}
			}
			else
			{
				proceed = false;
			}
		}
		return proceed;
	}
	
	/**
	 *
	 * Retrieves the node of te Relationship. This is virtual, so sub-classes may override this 
	 * behavior.
	 *
	 * @param node[out] The retrieved node
	 *
	 * @return HRESULT
	 *
	 */
	protected Node getCurNode()
	{
		Node n = null;
		if (m_Relation != null)
		{
			n = m_Relation.getNode();
		}
		return n;
	}
	
	/**
	 *
	 * Prepares the internal dispatcher for use.
	 *
	 * @return HRESULT
	 *
	 */
	private void prepareDispatcher()
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IRelationValidatorEventDispatcher disp = (IRelationValidatorEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.relation());
		m_Help = disp;
	}
	
	public boolean firePreRelationCreated( IElement fromEnd, IElement toEnd )
	{
		m_Proceed = true;
		if (m_RelProxy != null && m_Help != null)
		{
			m_RelProxy.setConnection(m_Relation);
			IEventPayload payload = preparePayload("PreRelationCreated", fromEnd, toEnd);
			m_Proceed = m_Help.firePreRelationCreated(m_RelProxy, payload);
		}
		return m_Proceed;
	}
	
	public void fireRelationCreated()
	{
		if (m_Proceed && m_RelProxy != null && m_Help != null)
		{
			m_RelProxy.setConnection(m_Relation);
			IEventPayload payload = m_Help.createPayload("RelationCreated");
			m_Help.fireRelationCreated(m_RelProxy, payload);
		}
	}
	public void fireRelationCreated(IElement element)
	{
		if (m_RelProxy != null)
		{
			m_Relation = element;
			fireRelationCreated();
		}
	}
	
	public boolean firePreRelationDeleted()
	{
		m_Proceed = true;
		if (m_RelProxy != null && m_Help != null)
		{
			IEventPayload payload = preparePayload("PreRelationDeleted", null, null);
			m_Proceed = m_Help.firePreRelationDeleted(m_RelProxy, payload);
		}
		return m_Proceed;
	}
	
	public void fireRelationDeleted()
	{
		if (m_Proceed && m_Help != null)
		{
			IEventPayload payload = m_Help.createPayload("RelationDeleted");
			m_Help.fireRelationDeleted(m_RelProxy, payload);
		}
	}
	
}



