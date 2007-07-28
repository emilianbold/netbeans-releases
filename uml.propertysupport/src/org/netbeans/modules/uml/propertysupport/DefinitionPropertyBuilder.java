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

package org.netbeans.modules.uml.propertysupport;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.PseudoState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFilter;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFilter;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElementManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionBooleanProperty;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionColorProperty;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionColorsAndFontsProperty;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionCustomProperty;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionFontProperty;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionListProperty;
import org.netbeans.modules.uml.propertysupport.nodes.DefinitionTextProperty;

import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

import org.netbeans.modules.uml.core.support.umlsupport.ICustomValidator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import java.util.Map;

/**
 * 
 * @author Trey Spiva
 */
public final class DefinitionPropertyBuilder
{
   private IPropertyDefinitionFactory mDefFactory = null;
   private IPropertyElementManager   mPropEleMgr = null;
   private static DefinitionPropertyBuilder mBuilderInstance = new DefinitionPropertyBuilder();
   private IPropertyDefinitionFilter mFilter = new PropertyDefinitionFilter();
   ConfigStringTranslator mTranslator = new ConfigStringTranslator();
   
   public final static int CONTROL_UNKNOWN = - 1;
   public final static int CONTROL_EDIT = 1;
   public final static int CONTROL_BOOLEAN = 2;
   public final static int CONTROL_COMBO = 3;
   public final static int CONTROL_LIST = 4;
   public final static int CONTROL_FONT = 5;
   public final static int CONTROL_COLOR = 6;
   public final static int CONTROL_FONTLIST = 7;
   public final static int CONTROL_COLORLIST = 8;
   public final static int CONTROL_COLORS_AND_FONTS = 9;
   public final static int CONTROL_CUSTOM = 10;
   public final static int CONTROL_MULTIEDIT = 11;
   public final static int CONTROL_SHEET = 12;  
   
   
   /**
    * 
    */
   private DefinitionPropertyBuilder()
   {
   }
   
   public static DefinitionPropertyBuilder instance()
   {
      return mBuilderInstance;
   }
            
   /**
    * Builds the property set for a specified model elements.
    *
    * @param elementType
    * @param element The model element.  This type can be null
    * @return The PropertySet structure for the model element.
    */
   public Node.PropertySet[] retreiveProperties(String elementType, Object element)
	{
		Node.PropertySet[] retVal = null;	
      
      IPropertyDefinitionFactory factory = getFactory();
      IPropertyDefinition def = factory.getPropertyDefinitionForElement(elementType, element);
      if(def != null)
      {
         IPropertyElementManager manager = getPropertyElementManager();
         IPropertyElement propEle = manager.buildElement(element, def, null);

         if(propEle != null)
         {
            mFilter.filterPropertyElement(propEle);
            
            retVal = buildPropertySets(def, propEle);
         }
      }
		
		return retVal;
	}
   
   public IPropertyElement retrievePropertyElement(IPropertyDefinition def,
                                                   IPropertyElement    element)
   {
      IPropertyElement retVal = null;
      
      if(def != null)
      {
         IPropertyElementManager manager = getPropertyElementManager();
         //retVal = manager.buildElement(null, def, element);
         retVal = manager.buildTopPropertyElement(def);
         retVal.setParent(element);
      }
      
      return retVal;
   }
   /**
    * Builds the property set for a specified property elements.  Bascially
    * builds the bridge between the UML property element constructs and the
    * NetBeans PropertySet constructs.
    *
    * @param def The property defintions that defines the property element
    *            Structure.
    * @param element The property element that needs to be converted into
    *                the PropertySet structure.
    * @return The PropertySet structure that wraps the property elements.
    */
   public Node.PropertySet[] buildPropertySets(IPropertyDefinition def,
                                               IPropertyElement    element)
   {
      return buildPropertySets(def, element, true);
   }
   
   public Node.PropertySet[] buildPropertySets(IPropertyDefinition def,
                                               IPropertyElement    element,
                                               boolean             autoCommit)                                            
   {
      Node.PropertySet[] retVal = null;

      
      //retVal = new Node.PropertySet[1];
      ArrayList < Node.PropertySet > propertySets = new ArrayList < Node.PropertySet >();

      Sheet.Set properties = new Sheet.Set();
      
      StringBuffer buffer = new StringBuffer("ModelElement Properties [");
      buffer.append(element.getName());
      buffer.append("]");
      properties.setName(buffer.toString());
      properties.setDisplayName(def.getDisplayName()); 
	  // #6266944, display expanded type for PseudoState element
	  if (element.getElement() instanceof PseudoState)
	  {
		  properties.setDisplayName(
				  ((PseudoState)element.getElement()).getExpandedElementType());
	  }  
      propertySets.add(properties);       

      properties.put(buildProperties(element, autoCommit));      
      buildAdditionalPropertySheets(element, propertySets, autoCommit);
      
      retVal = new Node.PropertySet[propertySets.size()];      
      propertySets.toArray(retVal);

      return retVal;
   }
   
   public Node.Property[] buildProperties(IPropertyElement element,
                                          boolean          autoCommit)
   {
      ArrayList < Node.Property > retVal = new ArrayList < Node.Property >();
      Vector < IPropertyElement > elements = element.getSubElements();
      if(elements != null)
      { 
         for (Iterator < IPropertyElement > iter = elements.iterator(); iter.hasNext();)
         {
            IPropertyElement curElement = iter.next();
            IPropertyDefinition curDef = curElement.getPropertyDefinition();
            if(curDef.isOnDemand() == true)
            {
               loadOnDemandProperties(curElement);
               curDef = curElement.getPropertyDefinition(); 
            }
            
            IPropertyElementManager manager = getPropertyElementManager();
            manager.interpretElementValue(curElement);
            
            boolean writeable = !isReadonly(curDef, curElement);
            if(curDef.getMultiplicity() <= 1)
            {          
                boolean canAdd = true;
                if(writeable == false)
                {    
                    String value = curElement.getValue();                    
                    if((value == null) || (value.length() <= 0))
                    {
//                        IPropertyElementManager manager = getPropertyElementManager();
//                        manager.interpretElementValue(curElement);
//                        
//                        String value2 = curElement.getValue();  
//                        if((value2 == null) || (value2.length() <= 0))
                        {
                            canAdd = false;
                        }
                    }
                }
                
                if(canAdd == true)
                {
                    Node.Property newProperty = getPropertyForDefinition(curDef, 
                                                                         curElement, 
                                                                         writeable,
                                                                         autoCommit);  
                    if(newProperty != null)
                    {
                       retVal.add(newProperty);
                    }   
                }
            }     
            else
            {
               if("custom".equals(curDef.getControlType()) == true)
               {
                  Node.Property newProperty = new DefinitionCustomProperty(curDef, curElement, writeable, autoCommit);
                  retVal.add(newProperty);
               }
            }
         }
      }
      
      Node.Property[] properties = new Node.Property[retVal.size()];
      retVal.toArray(properties);
      return properties;
   }
   
   protected void buildAdditionalPropertySheets(IPropertyElement element,
                                                ArrayList < Node.PropertySet > propertySets,
                                                boolean autoCommit)
   {
       Vector < IPropertyElement > elements = element.getSubElements();
       if(elements != null)
       {           
           for (Iterator < IPropertyElement > iter = elements.iterator(); iter.hasNext();)
           {
               IPropertyElement curElement = iter.next();
               IPropertyDefinition curDef = curElement.getPropertyDefinition();
               
               if("sheet".equals(curDef.getControlType()) == true)
               {                   
                   Vector < IPropertyElement > initialData = curElement.getSubElements();
                   buildPropertySheets(initialData, curDef, propertySets, autoCommit);                   
               }
               else if("read-only-sheet".equals(curDef.getControlType()) == true)
               {
                   Vector < IPropertyElement > initialData = curElement.getSubElements();
                   if((initialData != null) && (initialData.size() > 0))
                   {
                       Vector < IPropertyElement > sheets = new Vector < IPropertyElement >();
                       sheets.add(curElement);
                       buildPropertySheets(sheets, curDef, propertySets, autoCommit);  
                   }
               }
               else if("named-sheet".equals(curDef.getControlType()) == true)
               {
                   Vector < IPropertyElement > initialData = curElement.getSubElements();
                   buildPropertySheets(initialData, curDef, propertySets, autoCommit);  
                   
                   if(initialData.size() > 0)
                   {
                       INamedElement modelElement = (INamedElement)initialData.get(0).getElement();
                       if(modelElement != null)
                       {
                           Node.PropertySet set = propertySets.get(propertySets.size() - 1);

                           StringBuffer buffer = new StringBuffer(curDef.getDisplayName());
                           buffer.append(" - ");
                           buffer.append(modelElement.getNameWithAlias());
                           set.setDisplayName(buffer.toString());
                           set.setName(buffer.toString());
                       }
                   }
               }
           }
       }
   }
   
   protected void buildPropertySheets(Vector < IPropertyElement > initialData,
                                      IPropertyDefinition curDef,
                                      ArrayList < Node.PropertySet > propertySets,
                                      boolean autoCommit)
   {
       if(initialData != null)
       {
           int index = 1;
           for(IPropertyElement curType : initialData)
           {
               IPropertyDefinition def = curType.getPropertyDefinition();
               if(def.isOnDemand() == true)
               {
                   loadOnDemandProperties(curType);
                   def = curType.getPropertyDefinition(); 
               }

               Sheet.Set properties = new Sheet.Set(); 
               properties.setName("ModelElement Properties - Sheet " + index++);
               if(curDef.getMultiplicity() <= 1)
               {
                   properties.setDisplayName(curDef.getDisplayName());
               }
               else
               {
                   String name = def.getDisplayName();
                   if((name == null) || (name.length() <= 0))
                   {
                       name = curType.getName();
                   }
                   
                   // In the future this should not be hard coded. We should
                   // put this functionallity in the config file.
                   if(curType.getElement() instanceof IUMLBinding)
                   {
                       IUMLBinding binding = (IUMLBinding)curType.getElement();
                       name += ": " + binding.getFormal().getName(); // NOI18N
                   }
                   properties.setDisplayName(name);
               }

               properties.put(buildProperties(curType, autoCommit));
               propertySets.add(properties);
           }
       }
   }
   
   public void loadOnDemandProperties(IPropertyElement element)
   {
      Object data = element.getElement();
//      IPropertyDefinition pDef = mDefFactory.getPropertyDefinitionByName(element.getName());
      IPropertyDefinition pDef = mDefFactory.getPropertyDefinitionByName(((IElement)element.getElement()).getElementType());
      if (pDef != null)
      {
         mPropEleMgr.reloadElement(data, pDef, element);
         mFilter.filterPropertyElement(element);
         
         element.setPropertyDefinition(pDef);
      }
   }
   
   public IPropertyDefinition loadOnDemandDefintion(IPropertyDefinition def)
   {
      return mDefFactory.getPropertyDefinitionByName(def.getName());
   }
   
   public ValidValues retrieveValidValues(IPropertyDefinition def, 
                                          IPropertyElement  element)
   {  
      ValidValues retVal = null;

      if ((def != null) && (element != null))
      {
         String values = def.getValidValues();

         if ((values != null) && (values.length() > 0))
         {
            if (values.equals("#DataTypeList") == true)
            {
               IStrings validValues = def.getValidValue(element);
               retVal = convertValues(validValues, def);
            }
            else if (values != null && values.indexOf("#") >= 0)
            {  
               //values = mDefinition.getValidValues2();
               IStrings validValues = def.getValidValue(element);
               if ((validValues != null) && (validValues.getCount() > 0))
               {
                  retVal = convertValues(validValues, def);
               }
               else
               {                     
                  retVal = retreiveValueValue2(def);
               }
            }
            else
            {
               retVal = retreiveValueValue2(def);
            }
         }
      }

      //      Debug.out.println("Property [" + mDefinition.getDisplayName() + "] = " + retVal);
      return retVal;
   }
   
   public Node.Property getPropertyForDefinition(IPropertyDefinition def, 
                                                 IPropertyElement    element, 
                                                 boolean             writeable,
                                                 boolean             autoCommit)
   {
      Node.Property retVal = null;
      
      switch(getControlType(def))
      {                  
         case CONTROL_EDIT:
            retVal = new DefinitionTextProperty(def, element, writeable, autoCommit);
            retVal.setValue("suppressCustomEditor",Boolean.TRUE);
            break;
         case CONTROL_MULTIEDIT:
            retVal = new DefinitionTextProperty(def, element, writeable, autoCommit);
            break;
          case CONTROL_BOOLEAN:
            retVal = new DefinitionBooleanProperty(def, element, writeable, autoCommit);
            break;
         case CONTROL_COMBO:        
            retVal = new DefinitionListProperty(def, element, writeable, autoCommit, true);
            break;
         case CONTROL_LIST:
            retVal = new DefinitionListProperty(def, element, writeable, autoCommit);
            break;
         case CONTROL_FONT:
            retVal = new DefinitionFontProperty(def, element, writeable, autoCommit);
            break;
         case CONTROL_COLOR:
            retVal = new DefinitionColorProperty(def, element, writeable, autoCommit);
            break;
         case CONTROL_FONTLIST:
            retVal = new DefinitionFontProperty(def, element, writeable, autoCommit);
            break;
         case CONTROL_COLORLIST:
            retVal = new DefinitionColorProperty(def, element, writeable, autoCommit);
            break;
         case CONTROL_COLORS_AND_FONTS:
            retVal = new DefinitionColorsAndFontsProperty(def, element);
            break;
         case CONTROL_CUSTOM:
            retVal = new DefinitionCustomProperty(def, element, writeable, autoCommit);
            break;
      }
      
      return retVal;
   }
   
   ////////////////////////////////////////////////////////////////////////////
	// Helper Methods
	////////////////////////////////////////////////////////////////////////////
	
   /**
	 * Gets the property element manager for the node.
	 */
	protected IPropertyElementManager getPropertyElementManager()
	{
		if (mPropEleMgr == null)
		{
			mPropEleMgr = new PropertyElementManager();
			mPropEleMgr.setPDFactory(getFactory());
		}
		return mPropEleMgr;
	}
   
	/**
	 * Gets the property definition factory for the node.  The property
	 * definitions factory uses a configration file to generate property
	 * defintions.  The propety defintion is basically an abstract description
	 * of the properties to be displayed for the model elements.
	 *
	 * @return The property definition factory.
	 */
	protected IPropertyDefinitionFactory getFactory()
	{
		if( mDefFactory == null)
		{
			String file = getDefinitionFile();
			mDefFactory = new PropertyDefinitionFactory();
			mDefFactory.setDefinitionFile(file);
			mDefFactory.buildDefinitionsUsingFile();
		}
		
		return mDefFactory;
	}
   
   /**
	 * Retrieve the file that defines the property definitions for the
	 * project tree builder.
	 *
	 * @return  The definition file
	 */
	protected String getDefinitionFile()
	{
		String retVal = "";
		
		IConfigManager configMgr = ProductHelper.getConfigManager();
		if(configMgr != null)
		{
			// I am using a StringBuffer because it is suppose to be
			// faster when doing string concatnation.
			StringBuffer buffer = new StringBuffer(configMgr.getDefaultConfigLocation());
			//buffer.append("PropertyDefinitions.etc");
			buffer.append("PropertyDefinitions.etc");
			retVal = buffer.toString();
		}
		
		return retVal;
	}
   
   public int getControlType(IPropertyDefinition def)
   {
      int retVal = CONTROL_UNKNOWN;

      if(def != null)
      {
         String ctrlType = def.getControlType();
         
         if((ctrlType == null ) ||
            (ctrlType.equals("edit") == true) ||
            (ctrlType.equals("read-only") == true))
         {
            retVal = CONTROL_EDIT;
            if(isBoolean(def) == true)
            {
                retVal = CONTROL_BOOLEAN;
            }
         }
         else if(ctrlType.equals("multiedit") ==  true)
         {
            retVal = CONTROL_MULTIEDIT;
            String classID = def.getProgID();
            if((classID != null) && (classID.length() > 0))
            {
               try
               {
                  if(classID.equals("JavaDocEditorAddIn.JavadocEditor"))
                  {
                     retVal = CONTROL_EDIT;
                  }
                  else if(classID.equals("PreferenceDialog.DescribeFontDialog"))
                  {
                     retVal = CONTROL_FONTLIST;
                  }
                  else if (classID.equals("PreferenceDialog.DescribeColorDialog"))
                  {
                     retVal = CONTROL_COLORLIST;
                  }
                  else if (classID.equals("DrawingProps.ColorsAndFontsDialog"))
                  {
                     retVal = CONTROL_COLORS_AND_FONTS;
                  }
                  else
                  {
                     Class c = Class.forName(classID);
                     if(c != null)
                     {
                        retVal = CONTROL_CUSTOM;
                     }
                  }
               }
               catch(ClassNotFoundException e)
               {
                  retVal = CONTROL_UNKNOWN;
               }
            }
         }
         else if(ctrlType.equals("combo") == true)
         {
            retVal = CONTROL_COMBO;
         }
         else if(ctrlType.equals("list") == true)
         {
            retVal = CONTROL_LIST;
            if(isBoolean(def) == true)
            {
                retVal = CONTROL_BOOLEAN;
            }
            

         }
         else if(ctrlType.equals("sheet") == true)
         {
             retVal = CONTROL_SHEET;
         }
         else if (ctrlType.equals("custom"))
         {
             retVal = CONTROL_CUSTOM;
         }
      }
      return retVal;
   }
   
   private boolean isBoolean(IPropertyDefinition def)
   {
       boolean retVal = false;
       String validValues = def.getValidValues();
       if(validValues != null)
       {
           if((validValues.equals("PSK_FALSE|PSK_TRUE") == true) ||
              (validValues.equals("PSK_TRUE|PSK_FALSE") == true))
           {
              retVal = true;
           }
       }
       return retVal;
   }
   
   private ValidValues convertValues(IStrings validValues, IPropertyDefinition def)
   {
      String[] retVal;
	  if (validValues==null)
	  {
		  return null;
	  }
      retVal = new String[validValues.getCount()];
      
      HashMap < String, String > validValuesMap = new HashMap < String, String >();
      for (int index = 0; index < retVal.length; index++)
      {
         String validValue = validValues.item(index);
         String transVal = mTranslator.translate(def, validValue);
         retVal[index] = transVal;
         validValuesMap.put(transVal, validValue);
      }
      return new ValidValues(retVal, validValuesMap, false);
   }
   
   public ValidValues retreiveValueValue2(IPropertyDefinition def)
   {
      ValidValues retVal = null;
      String values = def.getValidValues2();
      if(values != null)
      {
         StringTokenizer tokenizer = new StringTokenizer(values, "|");

         String[] validValues = new String[tokenizer.countTokens()];
         HashMap < String, String > validValuesMap = new HashMap < String, String >();

         for (int index = 0; tokenizer.hasMoreTokens() == true; index++)
         {
            String preTransValue = tokenizer.nextToken();
            String transVal = mTranslator.translate(def, preTransValue);
            validValues[index] = transVal;
            validValuesMap.put(transVal, preTransValue);

         }
         
         retVal = new ValidValues(validValues, validValuesMap, true);
      }
      return retVal;
   }
   
   protected boolean isReadonly(IPropertyDefinition def, 
                                IPropertyElement element)
   {
      boolean retVal = false;
      
      if(def != null)
      {
         String controlType = def.getControlType();
         long mult = def.getMultiplicity();
         
         if (mult > 1)
         {
            // collections are read only
            if (controlType != null && controlType.equals("read-only"))
            {
               retVal = true;
            }
         }
         else
         {
            // property definitions that have not told us what their type is or that they are read only
            // should be marked as read only
            if (controlType == null || controlType.length() == 0 || controlType.equals("read-only"))
            {
               retVal = true;
            }
            else
            {
               boolean bEdit = isElementEditable(element, def);
               if (!bEdit)
               {
                  retVal = true;
               }
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Determines whether or not this property element should be editable.  There are some cases
    * that we know going in that it is not editable (ie. versioned file name).  There are other
    * cases that we want it to not be editable if it is a certain value.  In these cases, we
    * need to mark the definition, so that we know who to ask.
    *
    *
    * @param pEle[in]      The property element in question
    * @param pDef[in]      The property definition of the property element in question
    * @return Whether or not this property element is editable
    */
   protected boolean isElementEditable(IPropertyElement pEle,
            IPropertyDefinition pDef)
   {
      boolean bEdit = true;
      String value = pEle.getValue();
      if (value != null && value.length() > 0)
      {
         String cType = pDef.getControlType();
         if (cType != null && cType.equals("read-only"))
         {
            String pdName = pDef.getName();
            String parentName = "";
            IPropertyDefinition parentDef = pDef.getParent();
            if (parentDef != null)
            {
               parentName = parentDef.getName();
            }
            if ((pdName.equals("ReferredElement") == true) ||
                     (pdName.equals("ReferencingElement") == true))
            {
            }
            else if ((parentName.equals("AssociatedDiagrams") == true) ||
                     (parentName.equals("AssociatedElements") == true))
            {
            }
            else
            {
               bEdit = false;
            }
         }
         else
         {
            // get the validate information from the definition
            String validM = pDef.getFromAttrMap("validate");
            if (validM != null && validM.length() > 0)
            {
               // if the string in the validate is not a progID (Foundation.Project)
               // then check to see if it is a GUID ({123-456})
               // there are two ways to invoke the validate, if it is a progID
               // then we cocreate it, cast it to a ICustomValidator, and call Validate
               // if it is a GUID, we cast the IDispatch that we have on the property element
               // to a ICustomValidator, and call Validate
               try
               {
                  Class progIDClass = Class.forName(validM);
                  Object progIDObj = progIDClass.newInstance();
                  if (progIDObj instanceof ICustomValidator)
                  {
                     ICustomValidator pValidator = (ICustomValidator)progIDObj;
                     String name = pDef.getName();
                     bEdit = pValidator.validate(pEle, name, value);
                  }
               }
               catch (ClassNotFoundException cExp)
               {
                  Object obj = pEle.getElement();
                  if (obj != null)
                  {
                     if (obj instanceof ICustomValidator)
                     {
                        ICustomValidator pValidator = (ICustomValidator)obj;
                        String name = pDef.getName();
                        bEdit = pValidator.validate(pEle, name, value);
                     }
                  }
               }
               catch (Exception e)
               {
               }
            }
         }
      }
      return bEdit;
   }
   
   
   public class ValidValues
   {
      private String[] mTranslatedValues = null;
      private HashMap < String, String> mTransValueMap = null;
      private HashMap < String, String> mValueToTransValueMap = null;
      private boolean mTranslateAsArray = false;
      
      public ValidValues(String[] transValue,
               HashMap < String, String > transValueMap,
               boolean asArray)
      {
         mTranslateAsArray = asArray;
         mTranslatedValues = transValue;
         mTransValueMap = transValueMap;
         
         mValueToTransValueMap = new HashMap<String, String>();
         for (Iterator iter = mTransValueMap.entrySet().iterator(); iter.hasNext(); ) {
             Map.Entry entry = (Map.Entry) iter.next();
             mValueToTransValueMap.put((String)entry.getValue(), (String)entry.getKey());
         }
      }
      
      public String[] getValidValues()
      {
         return mTranslatedValues;
      }
      
      public String translateValue(String value)
      {
         String retVal = "";
         
         if(value != null)
         {
            if(mTranslateAsArray == true)
            {
               try
               {
                  String [] transValue = getValidValues();
                  int index = Integer.parseInt(value);
                  if(index < transValue.length)
                  {
                     retVal = transValue[index];
                  }
                  else
                  {
                  }
               }
               catch(NumberFormatException e)
               {
                  retVal = mValueToTransValueMap.get(value);
                  if (retVal == null)
                      retVal = value;
               }
               
            }
            else
            {
               if (retVal != null)
               {
                  //retVal = mTranslator.translate(mDefinition, value);
                  retVal = mTransValueMap.get(value);
               }
               else
               {
                  retVal = "";
               }
            }
         }
         
         return retVal;
      }
      
      public String translateValueBack(String value)
      {
         String retVal = value;
         
         retVal = (String)value;
         if(mTranslateAsArray == true)
         {
            for(int index = 0; index < mTranslatedValues.length; index++)
            {
               String curVal = mTranslatedValues[index];
               if(value.equals(curVal) == true)
               {
                  retVal = Integer.toString(index);
                  break;
               }
            }
         }
         else
         {
            String strValue = (String)value;
            retVal = (String)mTransValueMap.get(strValue);
         }
         
         return retVal;
      }
      
      public String translateValueBackToPSK(String value)
      {         
         return (String)mTransValueMap.get(value);
      }
   }

}
