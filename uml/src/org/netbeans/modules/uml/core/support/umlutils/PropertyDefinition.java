/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
//import org.netbeans.modules.uml.core.metamodel.structure.IProject;
//import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PropertyDefinition implements IPropertyDefinition{

  private String m_ID = null;
  private String m_name = null;
  private String m_displayName = null;
  private String m_PropertyEditorShowName = null;
  private String m_controlType = null;
  private String m_helpDescription = null;
  private String m_validValues = null;
  private String m_validValues2 = null;
  private String m_defaultValue = null;
  private String m_getMethod = null;
  private String m_setMethod = null;
  private String m_insertMethod = null;
  private String m_deleteMethod = null;
  private String m_validateMethod = null;
  private String m_createMethod = null;
  private String m_image = null;
  private String m_progID = null;

  private boolean m_required = false;
  private boolean m_defaultExist = false;
  private boolean m_onDemand = true;
  private boolean m_modified = false;

  private long m_multiplicity = 1;
  
  private boolean m_ForceRefresh = false;

  private Hashtable<String, String> m_map = new Hashtable<String, String>();

  //vector for IPropertyDefinition[]
  private HashMap<String, IPropertyDefinition> m_subDefinitions = new HashMap<String, IPropertyDefinition>();
  private Vector<IPropertyDefinition> m_VecSubDefs = new Vector<IPropertyDefinition>();
  
  private IPropertyDefinition m_parent = null;


  public PropertyDefinition() {
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public boolean isRequired() {
    return m_required;
  }

  public void setRequired(boolean val) {
    m_required = val;
  }

  public void setForceRefersh(boolean val)
  {
      m_ForceRefresh = val;
  }
  
  public boolean isForceRefresh()
  {
      return m_ForceRefresh;
  }
  
  public long getMultiplicity() {
    return m_multiplicity;
  }

  public void setMultiplicity(long val) {
    m_multiplicity = val;
  }

  public String getHelpDescription() {
	// due to localization issues, we have now stored some help text as a resource strings
	// because of this, we will need to translate from the "special" string to the text
	// if the string is not "specially" marked, it will not be translated, just returned
	IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
	String retHelp = m_helpDescription;
	if (translator != null)
	{
		retHelp = translator.translate(this, m_helpDescription);
	}
    return retHelp;
  }

  public void setHelpDescription(String val) {
    m_helpDescription = val;
  }

  public Vector<IPropertyDefinition> getSubDefinitions() {
     return m_VecSubDefs;
  	//return (new Vector<IPropertyDefinition>(m_subDefinitions.values()));
    //return m_subDefinitions;
  }

  public HashMap getHashedSubDefinitions() {
	return m_subDefinitions;
	//return m_subDefinitions;
  }

  public void setSubDefinitions(Vector val) {
  	m_subDefinitions.clear();
   m_VecSubDefs.clear();
   m_VecSubDefs = val;
  	if (val != null)
  	{
  		int count = val.size();
  		for (int i=0; i<count; i++)
  		{
  			IPropertyDefinition def = (IPropertyDefinition)val.elementAt(i);
  			m_subDefinitions.put(def.getName(), def);
  		}
  	}
    //m_subDefinitions = val;
  }

  public void addSubDefinition(IPropertyDefinition def) {
    if (def != null)
    {
      //m_subDefinitions.addElement(def);
      def.setParent(this);
	  m_subDefinitions.put(def.getName(), def);
     m_VecSubDefs.add(def);
    }

  }

  public IPropertyDefinition getSubDefinition(int index) 
  {
  	IPropertyDefinition retDef = null;
    if (index >= 0)
    {
    	Collection col = m_subDefinitions.values();
    	if (!col.isEmpty())
    	{
    		Iterator iter = col.iterator();
    		Object obj = iter.next();
    		int count = 0;
    		while (obj != null)
    		{
				count++;
    			if (count > index)
    			{
    				break;
    			}
    			obj = iter.next();
    		}
    		if (obj != null && obj instanceof IPropertyDefinition)
    		{
    			retDef = (IPropertyDefinition)obj;
    		}
    	}
    }
    return retDef;
  }
  
  public IPropertyDefinition getSubDefinition(String name) {
	if (name != null)
	{
		IPropertyDefinition def = m_subDefinitions.get(name);
		return def;
	}
	return null;
  }
  

  public String getDisplayName() 
  {
     String retVal = "";
     
     ConfigStringHelper helper = ConfigStringHelper.instance();
     IConfigStringTranslator translator = helper.getTranslator();
  
     if(translator != null)
     {
        retVal = translator.translate(this, m_displayName);
     }
    return retVal;
  }

  public void setDisplayName(String val) 
  {    
     m_displayName = val;
  }
  
  public void setPropertyEditorShowName(String val)
  {
  	m_PropertyEditorShowName = val;
  }

  public String getPropertyEditorShowName()
  {
  	if (m_PropertyEditorShowName != null && m_PropertyEditorShowName.length() > 0)
  	{
  		return m_PropertyEditorShowName;
  	}
  	else
  	{
  		return getDisplayName();
  	}
  }

  public IPropertyDefinition getParent() {
    return m_parent;
  }

  public void setParent(IPropertyDefinition def) {
    m_parent = def;
  }

  public String getControlType() {
    return m_controlType;
  }

  public void setControlType(String str) {
    m_controlType = str;
  }

  public String getGetMethod() {
    return m_getMethod;
  }

  public void setGetMethod(String val) {
    m_getMethod = val;
  }

  public String getSetMethod() {
    return m_setMethod;
  }

  public void setSetMethod(String val) {
    m_setMethod = val;
  }

  public String getID() {
    return m_ID;
  }

  public void setID(String val) {
    m_ID = val;
  }

  public String getInsertMethod() {
    return m_insertMethod;
  }

  public void setInsertMethod(String val) {
    m_insertMethod = val;
  }

  public String getDeleteMethod() {
    return m_deleteMethod;
  }

  public void setDeleteMethod(String val) {
    m_deleteMethod = val;
  }

  public String getValidValues() {
    return m_validValues;
  }
  public void setValidValues(String val) {
    m_validValues = val;
  }

  public String getValidValues2() {
    return m_validValues2;
  }
  public void setValidValues2(String val) {
    m_validValues2 = val;
  }

  private String m_EnumValues = null;
  
  /**
   * Some list are actually enumeration list.  The enum values are used to
   * determine the correct value that should be set and retreived.
   */
  public void setEnumValues(String values)
  {
      m_EnumValues = values;
  }
  
  /**
   * Some list are actually enumeration list.  The enum values are used to
   * determine the correct value that should be set and retreived.
   *
   * @return The string will be the list of enumeration values seperated
   *         by "|" characters.
   */
  public String getEnumValues()
  {
      return m_EnumValues;
  }
  
  /**
   * Some list are actually enumeration list.  The enum values are used to
   * determine the correct value that should be set and retreived.
   *
   * @return The list of enumeration values.
   */
  public String[] getEnumValueList()
  {
     String[] retVal = null;
     
     if(m_EnumValues != null)
     {
         StringTokenizer tokenizer = new StringTokenizer(m_EnumValues, "|");
         int tokens = tokenizer.countTokens();
         retVal = new String[tokens];
         for(int index = 0; index < tokens; index++)
         {
             retVal[index] = tokenizer.nextToken();
         }    
     }
     return retVal;
  }
  
  public void addToAttrMap(String name, String value) {
    if ((name != null) && (value != null))
      m_map.put(name, value);
  }

  public String getFromAttrMap(String name) {
    String value = null;
    if (name != null)
    {
      value = m_map.get(name);
    }
    return value;
  }

  public boolean isOnDemand() {
    return m_onDemand;
  }
  public void setOnDemand(boolean val) {
    m_onDemand = val;
  }

  public String getCreateMethod() {
    return m_createMethod;
  }

  public void setCreateMethod(String val) {
    m_createMethod = val;
  }

  public String getImage() {
    return m_image;
  }

  public void setImage(String val) {
    m_image = val;
  }

  public String getProgID() {
    return m_progID;
  }
  public void setProgID(String val) {
    m_progID = val;
  }

 public void save() {
 }

  public boolean isModified() {
    return m_modified;
  }
  public void setModified(boolean val) {
    m_modified = val;
  }

  public void remove() {
  }

  /**
   * Builds a "|" delimited string of representing the path to this definition.  Gets each
   * of its parent definition names and appends to the string
   *
   * @param pVal[out] The path
   *
   * @return HRESULT
   */
  public String getPath() {
    String returnStr = getName();
    IPropertyDefinition parent = getParent();
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

public String getDefaultValue() {
    return m_defaultValue;
  }
public void setDefaultValue(String val) {
    m_defaultValue = val;
  }

public boolean isDefaultExisting() {
    return m_defaultExist;
  }
public void setDefaultExists(boolean val) {
    m_defaultExist = val;
  }

public String getValidateMethod() {
    return m_validateMethod;
  }
public void setValidateMethod(String val) {
    m_validateMethod = val;
  }

public long getAttrMapCount() {
    return m_map.size();
  }

  /**
   * Retrieves a particular xml attribute and value from the
   * already built map based on its position
   *
   * @param pos		- the position of the attribute
   * @param name		- the name in the position
   * @param value	- the value in the position
   */
public void getFromAttrMap(long pos, String name, String value) {
    if ((name != null) && (pos >= 0))
    {
      Enumeration enumVal = m_map.keys();
      int i = 0;
      while (enumVal.hasMoreElements())
      {
        String str = (String)enumVal.nextElement();
        if (str.equalsIgnoreCase(name) && (i == pos))
        {
          value = (String)m_map.get(name);
        }
        i++;
      }
    }
  }

  /**
   * Some of the picklists used by the property editor are common and are built
   * from xpath queries.  This routine executes the xpath query and then gets the
   * name from each element and stores it in a list buffer.
   *
   *
   * @param pValues[out, retval]
   *
   * @return HRESULT
   */
  /**
   * Gets a string that represents the data to be inserted into the
   * control for this definition.
   *
   * @param pVal[out]
   */
public IStrings getValidValue(IPropertyElement elem){
    IStrings retVal = null;
    String str = getValidValues();
    if (str != null && str.indexOf("|") >= 0)
    {
    	StringTokenizer tokenizer = new StringTokenizer(str, "|");
    	while (tokenizer.hasMoreTokens())
    	{
    		String token = tokenizer.nextToken();
    		
    		//should translate this value if needed
    		IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
    		token = trans.translate(elem.getPropertyDefinition(), token);
    		if (retVal == null)
    		{
    			retVal = new Strings();
    		}
    		retVal.add(token);
    	}
//      String[] strs = str.split("|");
//      if (strs != null && strs.length > 0) {
//        for (int i = 0; i < strs.length; i++) {
//          String addStr = strs[i];
//          retVal.add(addStr);
//        }
//      }
    }
    else if (str != null && str.indexOf("#DataTypeList") >= 0)
    {
      IProject proj = getProject(elem);
      if (proj != null)
      {
        //try to get the picklist
        ITypeManager pTypeMgr = proj.getTypeManager();
        if (pTypeMgr != null)
        {
        	IPickListManager pPickMgr = pTypeMgr.getPickListManager();
        	if (pPickMgr != null)
        	{
                        retVal = new Strings();
        		String filter = parseDataTypeList();
                        StringTokenizer tokenizer = new StringTokenizer(filter, " ");
                        while (tokenizer.hasMoreTokens())
                        {
                            IStrings list;
                            String token = tokenizer.nextToken().trim();
                            if (token.equals("ParameterableElement"))
                            {
                                INamespace space = null;
                                Object mobj = getModelElement(elem);
                                if (mobj instanceof INamespace) 
                                {
                                    space = (INamespace)mobj;
                                }
                                else if (mobj instanceof INamedElement)
                                {
                                    space = ((INamedElement)mobj).getNamespace();
                                }
                                list = (pPickMgr.getTypeNamesWithStringFilterNamespaceVisible
                                       (token, false, space));
                            } 
                            else 
                            {
                                if (isFullyQualified())
                                {
                                    list = pPickMgr.getFullyQualifiedTypeNamesWithStringFilter(token);
                                }
                                else
                                {
                                    list = pPickMgr.getTypeNamesWithStringFilter(token);
                                }
                            }
                            retVal.append(list);
                        }
        	}
        }
      }
    }
    else if (str != null && str.startsWith("#Call") == true)
    {
        try
        {
            int end = str.lastIndexOf(')');
            String call = str.substring(6, end);

            // When in a property editor that is creating a new model element
            // element my be null.  So, in this situation the call has to be 
            // made on the parent.
            Object element = getModelElement(elem);

            Method m = element.getClass().getMethod(call);
            if(m != null)
            {
                Object returnValue = m.invoke(element);
                if(returnValue instanceof String)
                {
                    String strValue = (String)returnValue;
                    StringTokenizer tokenizer = new StringTokenizer(strValue, "|");
                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();
                        
                        //should translate this value if needed
                        IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
                        token = trans.translate(elem.getPropertyDefinition(), token);
                        if (retVal == null)
                        {
                            retVal = new Strings();
                        }
                        retVal.add(token);
                    }
                }
                else if(returnValue instanceof Collection)
                {
                    for(Object curValue : (Collection)returnValue)
                    {
                        if(retVal == null)
                        {
                            retVal = new Strings();
                        }
                        
                        retVal.add(curValue.toString());
                    }
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
        catch (IllegalArgumentException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
        catch (InvocationTargetException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
        catch (NoSuchMethodException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
        catch (SecurityException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }

    }
    
    return retVal;
  }

  /**
    * Method to navigate up the property element chain to retrieve the first model element in the chain
    *
    * @param[in] pEle       The property element in which to get the model element
    * @param[out] pModEle   The model element
    *
    * @return HRESULT
    *
    */
   private Object getModelElement(IPropertyElement pEle)
   {
      Object obj = pEle.getElement();
      if (obj == null)
      {
         IPropertyElement elem = pEle.getParent();
         if (elem != null)
         {
            obj = getModelElement(elem);
         }
      }
      return obj;
   }

  /**
   *
   * Determines whether or not pick lists should be shown using fully qualified names
   *
   * @return HRESULT
   *
   */
	private boolean isFullyQualified()
	{
		//kris richards - "DisplayTypeFSN" pref removed. Set to true
		return true;
	}
	
	/**
	 * @return
	 */
	private String parseDataTypeList()
	{
		String retStr = "";
		
		String values = getValidValues();
		if (values != null && values.length() > 0)
		{
			// has it been tagged as an xpath query
			if (values.indexOf("#DataTypeList") >= 0)
			{
				int pos = values.indexOf("(");
				if (pos >= 0)
				{
					// get the string between the ( )'s, this is what needs to be passed back
					retStr = values.substring(pos+1, values.length()-1);
				}
				else
				{
                                    System.out.println("PropertyDefinition");
					retStr = "DataType Class Interface Enumeration ParameterableElement DerivationClassifier";
				}
			}
		}
		return retStr;
	}

protected IProject getProject(IPropertyElement elem) {
    IProject proj = null;
    IPropertyElement elem1 = elem.getParent();
    if (elem1 != null)
    {
      Object obj = elem.getElement();
      if (obj == null)
      {
      	obj = elem1.getElement();
      }
      
      if (obj == null)
      {
		proj = getProject(elem1);
      }
      else
      {
      	if (obj instanceof IElement)
      	{
			IElement pElement = (IElement) obj;
			Object proj1 = pElement.getProject();
			if (proj1 == null)
			  proj = getProject(elem1);
			else if (proj1 instanceof IProject)
			{
				proj = (IProject)proj1;
			}
//			else if (Dispatch.isType(proj1, IProject.GUID))
//			{
//				proj = (IProject)(new IProjectProxy((Dispatch)proj1));
//			}
      	}
      	else
      	{
      		proj = getProject(elem1);
      	}
      }
    }
    return proj;
  }

}
