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
