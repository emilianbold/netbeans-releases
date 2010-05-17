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

package org.netbeans.modules.uml.core.support.umlutils;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PropertyElement implements IPropertyElement{

  private String m_name = null;
  private String m_value = null;
  private String m_origValue = null;

  private IPropertyElement m_parent = null;
  private IPropertyDefinition m_definition = null;
  private IPropertyElementManager m_elementManager = null;
  private Object m_Element = null;

  private boolean m_modified = false;
  private boolean m_onDemand = false;

  //< IPropertyElement[] >
  private Vector<IPropertyElement> m_subElements = new Vector<IPropertyElement>();
  private HashMap<String, IPropertyElement> m_subElementsHashed = new HashMap<String, IPropertyElement>();

  public PropertyElement() {
  }

  public String getName() {
    return m_name;
  }

  public void setName( String value ) {
    m_name = value;
  }

  public String getValue() 
  {
    return m_value;
//      String retVal = m_value;
//      
//      IPropertyDefinition def = getPropertyDefinition();
//      String enumValues = def.getEnumValues();
//      if(enumValues != null)
//      {
//          StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
//          int tokens = tokenizer.countTokens();
//          for(int index = 0; index < tokens; index++)
//          {
//              String curToken = tokenizer.nextToken();
//              if(curToken.equals(m_value) == true)
//              {
//                  retVal = Integer.toString(index);
//                  break;
//              }    
//          }   
//      }
//      return retVal;
  }

  public void setValue( String value ) 
  {
//      // Now make sure that the value is not an enumeration value.
//      IPropertyDefinition def = getPropertyDefinition();
//      String[] enumValues = def.getEnumValueList();
//      if(enumValues != null)
//      {
//          // Make sure that we actually have a valid value.
//          if((value != null) && (value.length() > 0))
//          {
//              try
//              {        
//                  int enumValueIndex = Integer.parseInt(value);
//                  if(enumValueIndex < enumValues.length)
//                  {
//                      m_value = enumValues[enumValueIndex];
//                  }   
//              }
//              catch(NumberFormatException e)
//              {
//                  // Just use the value that was passed into the method.
//              }
//          }
//      }
//      else
//      {
          m_value = value;
//      }
  }

  public Vector<IPropertyElement> getSubElements() {
    return m_subElements;
  }
  
  public HashMap<String, IPropertyElement> getHashedSubElements()
  {
  	return m_subElementsHashed;
  }

  public void setSubElements( Vector<IPropertyElement> value ) 
  {
  	m_subElements.clear();
  	m_subElementsHashed.clear();
    m_subElements = value;
    if (value != null)
    {
    	int count = value.size();
    	for (int i=0; i<count; i++)
    	{
    		IPropertyElement ele = value.get(i);
    		m_subElementsHashed.put(ele.getName(), ele);
    	}
    }
  }

  public void addSubElement( IPropertyElement element ) 
  {
  	if (element != null)
  	{
		element.setParent(this);
		m_subElements.addElement(element);
		m_subElementsHashed.put(element.getName(), element);
  	}
  }

  public IPropertyElement getSubElement( int index, IPropertyElement element ) 
  {
    int pos = m_subElements.indexOf(element, index);
    if (pos >= 0)
      return element;
    return null;
  }
  
  public IPropertyElement getSubElement(String name, IPropertyElement element)
  {
  	IPropertyElement retEle = null;
  	if (name != null)
  	{
  		retEle = m_subElementsHashed.get(name);
  	}
  	return retEle;
  }

  public IPropertyDefinition getPropertyDefinition() {
    return m_definition;
  }

  public void setPropertyDefinition( IPropertyDefinition value ) {
    m_definition = value;
  }

  public Object getElement() {
	if (m_Element != null)
	{
	   return m_Element;
	}

    return null;
  }

  public void setElement( Object value ) {
	m_Element = value;
  }

  public IPropertyElement getParent() {
    return m_parent;
  }

  public void setParent( IPropertyElement value ) {
    m_parent = value;
  }

  public boolean getModified() {
    return m_modified;
  }

  public void setModified( boolean value ) {
    m_modified = value;
  }

  public boolean getOnDemand() {
    return m_onDemand;
  }

  public void setOnDemand( boolean value ) {
    m_onDemand = value;
  }

  public boolean save() {
  	boolean saved = false;
    if (m_elementManager != null)
    {
      Object modelElem = this.getElement();
      if (modelElem == null)
      {
        modelElem = m_elementManager.createData(modelElem, m_definition, this);
        m_elementManager.insertData(m_parent.getElement(), m_definition, this);
      }
      m_elementManager.setData(modelElem, m_definition, this);
      saved = true;
    }
    return saved;
  }

  public String getOrigValue() {
    return m_origValue;
  }

  public void setOrigValue( String value ) {
    m_origValue = value;
  }

  public void remove() {
    if( m_elementManager != null)
    {
      IPropertyElement parent = getParent();
      if (parent != null)
        m_elementManager.deleteData(getParent(), this);
    }
  }

  /**
   * Builds a "|" delimited string of representing the path to this element.  Gets each
   * of its parent element names and appends to the string
   *
   * @param pVal[out] The path
   *
   * @return HRESULT
   */
  public String getPath() {
    String returnStr = getName();
    IPropertyElement parent = getParent();
    if (parent != null) {
      do {
        String name = parent.getName();
        name = name + "|" + returnStr;
        returnStr = name;
        parent = parent.getParent();
      } while (parent != null);
    }
    return returnStr;
  }

  public String getTranslatedValue() {
    String transStr = null;
    transStr = m_value;
    //use ConfigStringHelper to translate.
    IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
    if (translator != null)
    {
    	transStr = translator.translate(m_definition, m_value);
    }
    /*	CComPtr < IConfigStringTranslator > pTranslator;
   _VH(CConfigStringHelper::Instance()->GetTranslator(&pTranslator));
        if (pTranslator)
        {
                CComBSTR outVal;
                pTranslator->Translate(m_Definition, m_Value, &outVal);
                outVal.CopyTo(pVal);
        }
*/
    return transStr;
  }

  public IPropertyElementManager getPropertyElementManager() {
    return m_elementManager;
  }

  public void setPropertyElementManager( IPropertyElementManager value ) {
    m_elementManager = value;
  }

  public IProject getProject() {
    IProject proj = null;
    return proj;
  }

	public String toString()
	{
		if (m_definition != null)
		{
			return m_definition.getPropertyEditorShowName();
		}
		return getName();
	}
}
