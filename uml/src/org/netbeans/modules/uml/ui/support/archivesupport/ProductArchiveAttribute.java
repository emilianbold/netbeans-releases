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



package org.netbeans.modules.uml.ui.support.archivesupport;

import org.dom4j.Node;

/**
 * @author sumitabhk
 *
 *
 */
public class ProductArchiveAttribute implements IProductArchiveAttribute
{
	protected Node m_Node = null;

	/**
	 *
	 */
	public ProductArchiveAttribute()
	{
		super();
	}

	/**
	 * Gets the name of this attribute
	 */
	public String getName()
	{		
		return m_Node != null ? m_Node.getName() : null;
	}

	/**
	 * Gets the value of this attribute.
	 *
	 * @param pVal
	 */
	public String getValue()
	{		
		return m_Node != null ? m_Node.getStringValue() : null;
	}

	/**
	 * Sets the value of this attribute.
	 *
	 * @param newVal
	 */
	public void setValue(String value)
	{
		if (m_Node != null)
		{
			if (value != null && value.length() > 0)
			{
				m_Node.setText(value);
			}
			else
			{
				m_Node.setText("");
			}
		}
	}

	public Node getDOMNode()
	{
		return m_Node;
	}

	public void setDOMNode(Node value)
	{
		m_Node = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute#getLongValue()
	 */
	public long getLongValue()
	{
		Node node = getDOMNode();
      if(node != null)
      {
         String value = node.getText();
         if ((value != null) && (value.length() > 0))
         {
            try
            {
               return Long.parseLong(value);
            }
            catch(NumberFormatException e)
            {
            }
         }
      }
      
		return 0L;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute#getStringValue()
	 */
	public String getStringValue()
	{
      Node node = getDOMNode();
      if(node != null)
      {
         String value = node.getText();
         if (value != null)
         {
            return value;
         }
      }
            
      return "";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute#getBoolValue()
	 */
	public boolean getBoolValue()
	{
      Node node = getDOMNode();
      if(node != null)
      {
         String value = node.getText();
         if((value != null) && (value.length() > 0))
         {
            return Boolean.valueOf(value).booleanValue();
         }
      }
            
      return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute#getDoubleValue()
	 */
	public double getDoubleValue()
	{
      Node node = getDOMNode();
      if(node != null)
      {
         String value = node.getText();
         if((value != null) && (value.length() > 0))
         {
            try
            {
               return Double.parseDouble(value);
            }
            catch(NumberFormatException e)
            {
            }
         }
      }
            
      return 0;
	}

}


