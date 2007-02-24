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


package org.netbeans.modules.uml.core.eventframework;

import org.dom4j.Node;

public class EventContext implements IEventContext
{
  private IEventFilter m_eventFilter = null;
  private Object m_data = null;
  private String m_name = null;
  private Node m_node = null;
  private org.dom4j.Node m_4Jnode = null;

	public EventContext()
	{
	}


	/**
	 * Sets / Gets the EventFilter for this Context.
	*/
	public IEventFilter getFilter()
	{
          if (m_eventFilter == null)
          {
            m_eventFilter = new EventFilter();
          }
          return m_eventFilter;
	}

	/**
	 * Sets / Gets the EventFilter for this Context.
	*/
	public void setFilter( IEventFilter value )
	{
          if (value != null)
            m_eventFilter = value;
	}

	/**
	 * Sets / Gets the XML node this context represents.
	*/
	public Node getNode()
	{
          if (m_node == null)
          {
            //create it.
          }
          return m_node;
	}

	public org.dom4j.Node getDom4JNode()
	{
		  if (m_4Jnode == null)
		  {
			//create it.
		  }
		  return m_4Jnode;
	}

	/**
	 * Sets / Gets the XML node this context represents.
	*/
	public void setNode( Node value )
	{
          if (value != null)
            m_node = value;
	}

	public void setDom4JNode( org.dom4j.Node value )
	{
		  if (value != null)
			m_4Jnode = value;
	}

	/**
	 * Validates the trigger and payload about to be dispatched.
	*/
	public boolean validateEvent( String triggerName, Object payLoad )
	{
          if (triggerName != null && triggerName.length() > 0)
          {
            return getFilter().validateEvent(triggerName, payLoad);
          }
          return false;
	}

	/**
	 * The name of this EventContext.
	*/
	public String getName()
	{
          if (m_name.equals(""))
          {
            //set the name
          }
          return m_name;
	}

	/**
	 * The name of this EventContext.
	*/
	public void setName( String value )
	{
          m_name = value;
	}

	/**
	 * User-defined data associated with this Context.
	*/
	public Object getData()
	{
          if (m_data == null)
          {
            //create it.
          }
          return m_data;
	}

	/**
	 * User-defined data associated with this Context.
	*/
	public void setData( Object value )
	{
          if (value != null)
            m_data = value;
	}

	public static final String CLSID = "{C77F3863-A2D3-4832-B28B-A2E86CC32FEA}";

}
