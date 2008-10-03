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

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.generativeframework.IExpansionVariable;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.generativeframework.IVariableExpander;
import org.netbeans.modules.uml.core.generativeframework.IVariableFactory;
import org.netbeans.modules.uml.core.generativeframework.VariableExpander;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PropertyElementManager implements IPropertyElementManager
{
   
   private Object m_PresentationElement = null;
   private Object m_ModelElement = null;
   private IPropertyDefinitionFactory m_PDFactory = null;
   private boolean m_CreateSubs = true;
   private String m_ElementFile = null;
   private IDataFormatter m_Formatter = null;
   
   public PropertyElementManager()
   {
   }
   
   public Object getPresentationElement()
   {
      return m_PresentationElement;
   }
   
   public void setPresentationElement( Object value )
   {
      m_PresentationElement = value;
   }
   
   public Object getModelElement()
   {
      return m_ModelElement;
   }
   
   public void setModelElement( Object value )
   {
      m_ModelElement = value;
   }
   
   /**
    * Begin the process to create a property element based on the property definition.
    *
    * @param propDef    The property definition to base the building of the property element on
    * @param pVal       The property element that has been built
    *
    * @return HRESULT
    *
    */
   public IPropertyElement buildTopPropertyElement( IPropertyDefinition propDef )
   {
      IPropertyElement propEle = null;
      if (propDef != null)
      {
         propEle = new PropertyElement();
         propEle.setName(propDef.getName());
         propEle.setPropertyDefinition(propDef);
         propEle.setPropertyElementManager(this);
         Object pDisp = getModelElement();
         processSubElements(pDisp, propDef, propEle);
      }
      return propEle;
   }
   
   public IPropertyDefinitionFactory getPDFactory()
   {
      return m_PDFactory;
   }
   
   public void setPDFactory( IPropertyDefinitionFactory value )
   {
      m_PDFactory = value;
   }
   
   /**
    * Determines whether or not sub elements should be created
    */
   public boolean getCreateSubs()
   {
      return m_CreateSubs;
   }
   
   public void setCreateSubs( boolean value )
   {
      m_CreateSubs = value;
   }
   
   /**
    * Call the given "set" method from the property definition on the passed in IDispatch using the information
    * from the property element.
    *
    * @param pDisp     The IDispatch to call the set routine on
    * @param[in] pDef  The property definition to process
    * @param[in] pEle  The property element to get the information from
    *
    * @return HRESULT
    *
    */
   public void setData( Object pDisp1, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      try
      {
         if (pEle != null && pDef != null && pDisp1 != null)
         {
            String value = pEle.getValue();
            String getMeth = pDef.getGetMethod();
            String setMeth = pDef.getSetMethod();
            if (setMeth != null && setMeth.length() > 0)
            {
               //get the actual element from this proxy
               //        	Object pDisp = null;
               //        	String className = getClassNameFromID(pDef);
               //			Class clazz = Class.forName(className);
               //			Class[] params = null;//{com.embarcadero.com.Dispatch.class};
               //			Constructor constructor = clazz.getConstructor(params);
               //
               //			Object[] paramInstances = {pDisp1};
               //			pDisp = (IElement)constructor.newInstance(paramInstances);
               // before we actually do the set, we need to make sure that what we are setting
               // the item to is valid
               String str = pDef.getFromAttrMap("validate");
               Object obj = pEle.getElement();
               ICustomNameResolver resol = new CustomNameResolver();
               boolean valid = resol.validate(pEle, pDef.getName(), value);
               if (valid)
                  resol.whenValid(pDisp1);
               else
                  resol.whenInvalid(pDisp1);
               Class clazz1 = pDisp1.getClass();               
               Method getmethod = clazz1.getMethod(getMeth);
               Method setmethod = null;
               Method[] meths = clazz1.getMethods();
               if (meths != null)
               {
                  //ETSystem.out.println("Printing methods for class " + clazz1.getName());
                  for (int i = 0; i < meths.length; i++)
                  {
                     if (meths[i].getName().equals(setMeth))
                     {
                        setmethod = meths[i];                        
                        break;
                     }
                     //ETSystem.out.println(meths[i].getName());
                  }
               }
               if (setmethod != null)
               {
                  Class[] types = setmethod.getParameterTypes();
                  Object[] args = null;
                  if (types != null && types.length > 0)
                  {
                     //For getting the set method I need to pass in the arguments too.
                     //Method meth = clazz1.getMethod(setMeth, null);
                     args = new Object[1];
                     //need to take care of case where its one of the allowed values.
                     String val = pEle.getValue();
                     String validVals = pDef.getValidValues2();
                     if (validVals != null )
                     {
                        StringTokenizer tokenizer =
                        new StringTokenizer(validVals, "|");
                        String firstToken = tokenizer.nextToken();
						
						 if (validVals.startsWith("PSK") &&
						
                        ((firstToken.equals("PSK_TRUE")
                        || firstToken.equals("PSK_FALSE"))))
                        {
                           if ((("PSK_FALSE").equals(val)) || (("0").equals(val)))
                              args[0] = Boolean.FALSE;
                           else if ((("PSK_TRUE").equals(val)) || (("1").equals(val)))
                              args[0] = Boolean.TRUE;
                        }
//						 }
                        else
                        {
                           int i = 0;
                           boolean converted = true;
                           int convertedVal = -1;
                           try
                           {
                              convertedVal = new Integer(val).intValue();
                           }
                           catch (NumberFormatException e)
                           {
                              converted = false;
                           }
                           boolean found = false;
                           while (tokenizer.hasMoreTokens())
                           {
                              if ((firstToken).equals(val)
                              || (converted && (convertedVal == i)))
                              {
                                 found = true;
                                 break;
                              }
                              else
                              {
                                 firstToken = tokenizer.nextToken();
                                 i++;
                              }
                           }    
                       
                           if (!found) 
                           {
                               validVals = pDef.getValidValues();
                               if (validVals != null )
                               {
                                   tokenizer = new StringTokenizer(validVals, "|");
                                   int j = 0;
                                   while(tokenizer.hasMoreTokens()) 
                                   {
                                       String token = tokenizer.nextToken();
                                       if (token != null && token.equals(val)) 
                                       {
                                           i = j;
                                           break;
                                       }
                                       j++;
                                   }
                               }
                           }
                           
                           String enumVals = pDef.getFromAttrMap("enumValues");
                           if (enumVals != null)
                           {
                              //This is a special case for setting values for enums which do not start with 0
                              //for example diagram layout kind.
                              StringTokenizer enumTokenizer =
                              new StringTokenizer(enumVals, "|");
                              int j = 0;
                              String enumToken = "";
                              while (enumTokenizer.hasMoreTokens())
                              {
                                 enumToken = enumTokenizer.nextToken();
                                 if (j == i)
                                 {
                                    try
                                    {
                                       i = (new Integer(enumToken)).intValue();
                                       break;
                                    }
                                    catch (NumberFormatException e)
                                    {}
                                 }
                                 else
                                 {
                                    j++;
                                 }
                              }
                           }
                           if(!types[0].getName().equals("java.lang.String"))
                           {
                               args[0] = new Integer(i);
                           } else
                           {
                               args[0] = val;
                           }
                        }
                     }
                     else
                     {
                        args[0] = val;
                     }
                  }
                  Object retObj = setmethod.invoke(pDisp1, args);
                  if (getmethod != null)
                  {
                     // now that we did the set, some other process may have cancelled it, so we need
                     // to do a get of the data and refresh the property element
                     Object retObjGet = getmethod.invoke(pDisp1, (Object[])null);
                     Class returnClass = getmethod.getReturnType();
                     if (returnClass.getName().equals("java.lang.String"))
                     {
                        String str2 = (String)retObjGet;
                        if (str2 != null && str2.length() > 0)
                        {
                           processResult(retObjGet, pDef, pEle);
                        }
                     }
                     else
                     {
                        processResult(retObjGet, pDef, pEle);
                     }
                     IPropertyElementManager manager = pEle.getPropertyElementManager();
                     manager.interpretElementValue(pEle);
                     
                     pEle.setModified(false);
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
//         e.printStackTrace();
      }
   }
   
   /**
    * @param pDef
    * @return
    */
   private String getClassNameFromID(IPropertyDefinition pDef)
   {
      String retVal = null;
      if (pDef != null)
      {
         retVal = pDef.getID();
         if (retVal == null)
         {
            IPropertyDefinition parent = pDef.getParent();
            if (parent != null)
            {
               retVal = getClassNameFromID(parent);
            }
         }
      }
      return retVal;
   }
   
   /**
    * Call the given "create" method from the property definition on the passed-in IDispatch using the information
    * from the property element.
    *
    * @param pDisp     The IDispatch to use for the method calls - if there isn't one
    *							we will try and figure out one to use by going up the property element chain
    *							and finding the first propEle that has a IElement stored on it, and use that
    * @param[in] pDef  The property definition to process
    * @param[in] pEle  The property element to get the information from
    *
    * @return HRESULT
    *
    */
   public Object createData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      try
      {
         if (pEle != null && pDef != null)
         {
            String value = pEle.getValue();
            String setMeth = pDef.getCreateMethod();
            if (setMeth != null && setMeth.length() > 0)
            {
               // if a IDispatch hasn't been passed in, we are going to try and figure one out
               // we do that by going up the property element chain and finding the first one that
               // has a IElement on it
               if (pDisp == null)
               {
                  Object pDisp2 = getModelElement(pEle);
                  if (pDisp2 != null)
                  {
                     pDisp = pDisp2;
                  }
               }
               
               if (pDisp != null)
               {
                  // before we actually do the set, we need to make sure that what we are setting
                  // the item to is valid
                  String str = new String();
                  
                  Class clazz = pDisp.getClass();
                  Method[] meths = clazz.getMethods();
                  Method meth = null;
                  if (meths != null)
                  {
                     for (int i=0; i<meths.length; i++)
                     {
                        Method method = meths[i];
                        //ETSystem.out.println("got method = " + method.toString());
                        if (method.getName().equals(setMeth))
                        {
                           meth = method;
                           break;
                        }
                     }
                  }
                  
                  if (meth != null)
                  {
                     Class[] parms = meth.getParameterTypes();
                     
                     Object[] args = buildParameters(parms, pEle);
                     
                     Object retObj = meth.invoke(pDisp, args);
                     
                     Class returnClass = meth.getReturnType();
                     
                     // some of the create methods in Wolverine are a two step process
                     // we first create the IElement, then it needs to be added(inserted) into
                     // its proper parent (the second s
                     //
                     // if the method has returned a IDispatch, then the first step has been completed,
                     // and we need to set up the sub elements so that they can be used properly
                     if (retObj != null)
                     {
                        populateSubElementsAfterCreate(pEle, retObj);
                     }
                     else
                     {
                        // some of the create methods do not return the IDispatch that was created
                        // instead they take the IDispatch that should be created as its parameter
                        // our BuildParameter method has handled the creation of this parameter, but
                        // now we need to set up the subelements so that they can be used properly
                        if (args != null && args.length > 0)
                        {
                           Object obj = args[0];
                           if (obj != null)
                           {
                              populateSubElementsAfterCreate(pEle, obj);
                           }
                        }
                     }
                     pEle.setModified(false);
                  }
               }
            }
         }
      } catch (Exception e)
      {
         Log.stackTrace(e);
      }
      return pDisp;
   }
   
   /**
    * Special processing after a create of an IDispatch to set the subelements up to point to that
    * newly created IDispatch
    *
    *
    * @param pEle[in]			The current property element
    * @param pDisp[in]			The newly created IDispatch
    *
    * @return HRESULT
    *
    */
   private void populateSubElementsAfterCreate(IPropertyElement pEle, Object obj)
   {
       Class clazz = null;
       if (obj != null)
       {
            clazz = obj.getClass();
       }
        
      pEle.setElement(obj);
      Vector elems = pEle.getSubElements();
      if (elems != null && !elems.isEmpty())
      {
         for (int i=0; i<elems.size(); i++)
         {
            Object elemObj = elems.get(i);
             if (elemObj instanceof IPropertyElement)
             {
                 IPropertyElement ele = (IPropertyElement) elemObj;
                 ele.setElement(obj);
                 
                 if (clazz != null)
                 {  
                     IPropertyDefinition pDef = ele.getPropertyDefinition();
                     // check if the property definition is marked as a collection
                     long mult = pDef.getMultiplicity();
                     if (mult <= 1)
                     {
                         String getMethStr = pDef.getGetMethod();
                         try
                         {
                             java.lang.reflect.Method method =
                                     clazz.getMethod(getMethStr, (Class[]) null);
                             Object result = method.invoke(obj, (Object[]) null);
                             processResult(result, pDef, ele);

                         } catch (NoSuchMethodException ex)
                         {
                         // do nothing
                         } catch (Exception e)
                         {
                             e.printStackTrace();
                         }
                     } else
                     {
                         processCollectionWithSet(obj, pDef, ele);
                     }
                 }
             }
         }
         pEle.setModified(false);
      }
   }
   
   /**
    * Because a delete of an IElement has occurred, remove the corresponding property element from the
    * element structure
    *
    *
    * @param pDeleteEle[in]	The property element that has the delete information on it
    * @param pEle[in]			The property element that caused the delete
    *
    * @return HRESULT
    *
    */
   private void removeSubElementDueToDelete(IPropertyElement pDeleteEle, IPropertyElement pEle)
   {
      IPropertyElement delElem = null;
      if (pDeleteEle.equals(pEle))
         delElem = pDeleteEle.getParent();
      else
         delElem = pDeleteEle;
      
      if (delElem != null)
      {
         Vector<IPropertyElement> elems = delElem.getSubElements();
         if (elems != null && !elems.isEmpty())
         {
            for (int i=elems.size()-1; i>=0; i--)
            {
               Object obj = elems.get(i);
               if (obj instanceof IPropertyElement)
               {
                  IPropertyElement ele = (IPropertyElement)obj;
                  if (ele.equals(pEle))
                     elems.remove(i);
               }
            }
            delElem.setSubElements(elems);
         }
      }
   }
   
   private Object[] buildParameters(Class[] parms, IPropertyElement elem)
   {
      Object[] args = null;
      try
      {
         if (parms != null && parms.length > 0)
         {
            args = new Object[parms.length];
            Vector subElems = elem.getSubElements();
            if (subElems != null && !subElems.isEmpty())
            {
               for (int i = 0; i < parms.length; i++)
               {
                  Class parm = parms[i];
                  String clazzName = parm.getName();
                  
                  //all our classes will start with packagename.I, so search for .I
                  int pos = clazzName.indexOf(".I");
                  if (pos >= 0)
                  {
                     //get the actual class name to be created
                     String newclassName = clazzName.substring(pos + 2);
                     
                     //since we found a class as parameter, we need to create one.
                     FactoryRetriever ret = FactoryRetriever.instance();
                     Object obj = ret.createType(newclassName, null);
                     if (obj != null && parm.isAssignableFrom(obj.getClass()))
                     {
                        args[i] = obj;
                     }
                  }
                  else
                  {
                     //its a simple type, get the value from sub element and put in the args.
                     IPropertyElement subElem = (IPropertyElement) subElems.get(i);
                     String val = subElem.getValue();
                     if (val == null && clazzName.equals("java.lang.String"))
                     {
                        args[i] = "";
                     }
                     else
                     {
                        if (val.getClass().equals(parm))
                        {
                           args[i] = val;
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception e)
      {
         
      }
      return args;
   }
   
   /**
    * Call the given "delete" method on the passed-in delete property element using the other property
    * element as the parameter to the "delete".
    *
    *
    * @param pDeleteEle[in]		The property element containing the information about the model element
    *										that should be invoked in order to perform the delete.
    * @param pEle[in]				The property element containing the information about the model element
    *										that should be deleted.
    *
    * @return HRESULT
    *
    */
   public void deleteData( IPropertyElement pDeleteEle, IPropertyElement pEle )
   {
      try
      {
         if (pDeleteEle != null && pEle != null)
         {
            IPropertyDefinition pDef = pDeleteEle.getPropertyDefinition();
            if (pDef != null)
            {
               String delMeth = pDef.getDeleteMethod();
               Object propEle = pDeleteEle.getElement();
               Class clazz = propEle.getClass();
               
               //need to pass in the right set of parameters, otherwise the method will not be found.
               Method meth = null;//clazz.getMethod(delMeth, null);
               Method[] meths = clazz.getMethods();
               if (meths != null)
               {
                  for (int i=0; i<meths.length; i++)
                  {
                     Method method = meths[i];
                     //ETSystem.out.println("got method = " + method.toString());
                     if (method.getName().equals(delMeth))
                     {
                        meth = method;
                        break;
                     }
                  }
               }
               
               if (meth != null)
               {
                  Class[] parms = meth.getParameterTypes();
                  Object[] args = null;
                  if (parms != null && parms.length > 0)
                  {
                     args = new Object[parms.length];
                     args[0] = pEle.getElement();
                  }
                  meth.invoke(propEle, args);
                  removeSubElementDueToDelete(pDeleteEle, pEle);
               }
            }
         }
      } catch(Exception e)
      {}
   }
   
   /**
    * Do the actual create of the property element.
    *
    * @param[in] modEle       The IDispatch(model element) to get the information from
    * @param[in] parentDef    The property definition to base the building of the property element on
    * @param[in] parentEle    The owning property element to add the information to
    *
    * @return HRESULT
    *
    */
   public IPropertyElement buildElement( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      IPropertyElement propEle = null;
      try
      {
         propEle = new PropertyElement();
         String pdName = pDef.getName();
         propEle.setName(pdName);
         propEle.setElement(pDisp);
         propEle.setPropertyDefinition(pDef);
         propEle.setPropertyElementManager(this);
         if (pDisp != null)
         {
            String getStr = pDef.getGetMethod();
            if (getStr != null && getStr.length()>0)
            {
               // I am really special casing this because it is 5 days before the release and the
               // only attribute that is not working to our knowledge is IsFinal on a Classifier.
               // This is because Classifier is multiply derived from RedefinableElement, but RedefEle
               // is not a COM object, it is just an interface.  I tried writing generic code for this
               // and I think I have it, but it is too risky to check in at this time.  So this is my
               // hack.
               // begin special case
               String str = pDef.getFromAttrMap("typelibQuery");
               String str2 = pDef.getFromAttrMap("getObject");
               String countStr = pDef.getFromAttrMap("getCount");
               if (str != null && str.length() > 0)
               {
                  if (pDisp instanceof IRedefinableElement)
                  {
                     IRedefinableElement pRedef = (IRedefinableElement)pDisp;
                     boolean isFinal = pRedef.getIsFinal();
                     String buffer = "0";
                     if (isFinal)
                     {
                        buffer = "1";
                     }
                     propEle.setValue(buffer);
                     propEle.setOrigValue(buffer);
                  }
               }
               else if (str2 != null && str2.length() > 0)
               {
                  Object result = interpretGetObjectDefinition(propEle);
                  if (result != null)
                  {
                     processResult(result, pDef, propEle);
                  }
               }
               else
               {
                  // normal processing
                  // figure out the id of the "get" method as well as the proper type that the IDispatch should be
                  // this will try and find the method on the IDispatch and if it is not found, it will ask the
                  // definition if it contains an ID of a type that the IDispatch should be (because some IElements
                  // multiply inherit, so this screws up our chain)
                  //
                  // the result is the ID of the method and the proper IDispatch
                  //invoke the get method on this "name" object and then load the value and origValue.
                  Class clazz = pDisp.getClass();
                  try
                  {
                     Object result = null;
                     IElement curE = null;
                     java.lang.reflect.Method method = 
                         clazz.getMethod(getStr, (Class[])null);
                     
                     result = method.invoke(pDisp, (Object[])null);
                     
                     //now since we have the result of the method invoke, we want to process it.
                     processResult(result, pDef, propEle);
                     
                     // if what we are building is the name, store the result in the parent's name field too
                     if (pdName.equals("Name") && pEle != null)
                     {
                        if (result != null)
                        {
                           pEle.setValue(result.toString());
                        }
                     }
                  }
                  catch(NoSuchMethodException ex)
                  {
                     // need to check if it is our Multiplicity special case, if it is, we will continue
                     // and process the sub elements - this will happen if the getMethod is not found on
                     //on the class.
                     long mult = pDef.getMultiplicity();
                     if (mult > 1)
                     {
                        String setMeth = pDef.getSetMethod();
                        if (setMeth != null && setMeth.length() > 0)
                        {
                           processSubElements(pDisp, pDef, propEle);
                        }
                     }
                  }
                  catch(Exception e)
                  {
                     e.printStackTrace();
                  }
               }
            }
            else
            {
               processSubElements(pDisp, pDef, propEle);
            }
         }
         else
         {
            // didn't have a model element on it, but we still need to process any subs
            processSubElements(pDisp, pDef, propEle);
         }
         propEle.setPropertyDefinition(pDef);
      } catch (Exception e)
      {}
      return propEle;
   }
   
   /**
    * Method to determine how to handle the result of the invoke call
    *
    * @param[in] result       The CComVariant that was returned from the invoke call
    * @param[in] pDef         The property definition to base the building of the property element on
    * @param[in] pEle         The property element to use
    *
    * @return HRESULT
    *
    */
   private void processResult(Object result, IPropertyDefinition pDef, IPropertyElement pEle)
   {
      if (result != null)
      {
         Class clazz = result.getClass();
         if (clazz.equals(String.class))
         {
            pEle.setValue(result.toString());
            pEle.setOrigValue(result.toString());
         }
         else if (clazz.equals(Integer.class))
         {
            pEle.setValue(result.toString());
            pEle.setOrigValue(result.toString());
         }
         else if (clazz.equals(Boolean.class))
         {
            // the result was a boolean, so turn it into a string, and set the property element value to it
            String val = result.toString();
            String buffer = "";
            if (val.equals("true"))
            {
               buffer = "1";
            }
            else
            {
               buffer = "0";
            }
            pEle.setValue(buffer);
            pEle.setOrigValue(buffer);
         }
         else //if(result instanceof Dispatch)
         {
            processDispatchResult(result, pDef, pEle);
         }
      }
   }
   
   /**
    * Special processing of a IDispatch returned by the invoke call
    *
    * @param pDisp            The IDispatch(model element) to get the information from
    * @param[in] pDef         The property definition to base the building of the property element on
    * @param[in] pEle         The property element to use
    *
    * @return HRESULT
    *
    */
   private void processDispatchResult(Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle)
   {
      try
      {
         // if the property definition is a record (has a multiplicity > 1)
         // then the IDispatch that we got back is a collection
         long mult = pDef.getMultiplicity();
         if (mult > 1)
         {
            processCollectionResult(pDisp, pDef, pEle);
         }
         else
         {
            // we still got a IDispatch back, but it isn't a collection
            // we have stored what to do with this IDispatch in a map on the property definition
            // loop through them
            if (pDisp instanceof IElement)
            {
               IElement pModelElement = (IElement)pDisp;
               
               // find if we need to process this kind by looking at the attr map - the definition
               // may have had some DispatchInvoke subdefinitions that were processed as attributes
               // on the definition
               // For example, <DispatchInvoke name="{123-456}" get="Type">
               // This means that if the IDispatch that we just got back from the "get" call can be
               // cast to an object represented by the progID in name,
               // we want to take the IDispatch and call its Type method to get
               // the value to store in the property element
               String castID = pDef.getFromAttrMap("castID");
               String getStr = pDef.getFromAttrMap("castIDGet");
               if (getStr != null && getStr.length() > 0)
               {
                  //find the method with this name on the pDisp object
                  Method getMeth = pDisp.getClass().getMethod(getStr, (Class[])null);
                  if (getMeth != null)
                  {
                     Object result = getMeth.invoke(pDisp, (Object[])null);
                     if (result != null)
                     {
                        // process the result from the "get"
                        processResult(result, pDef, pEle);
                     }
                  }
               }
               else
               {
                  // we may still need to process sub elements
                  processSubElements(pDisp, pDef, pEle);
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Special processing of a IDispatch returned by the invoke call that is a collection
    *
    * @param pDisp            The IDispatch(model element) to get the information from
    * @param[in] pDef         The property definition to base the building of the property element on
    * @param[in] pEle         The property element to use
    *
    * @return HRESULT
    *
    */
   private void processCollectionResult(Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle)
   {
      try
      {
         // we are assuming that all collections have a count method on them
         Class clazz = pDisp.getClass();
         Method countMethod = clazz.getMethod("getCount", (Class[])null);
         
         if (countMethod != null)
         {
            //invoke this count method
            Object countResult = countMethod.invoke(pDisp, (Object[])null);
            if (countResult != null && countResult instanceof Integer)
            {
               int counter = ((Integer)countResult).intValue();
               if(counter > 0)
               {
                  //Now I want to get the item method so that I can invoke
                  //it on the collections object.
                  Class[] parms = new Class[1];
                  parms[0] = int.class;
                  Method itemMethod = clazz.getMethod("item", parms);
                  if (itemMethod != null)
                  {
                     for (int i=0; i<counter; i++)
                     {
                        Object[] itemCount = new Object[1];
                        itemCount[0] = new Integer(i);
                        Object itemResult = itemMethod.invoke(pDisp, itemCount);
                        
                        if (itemResult != null)
                        {
                           Class itemClass = itemResult.getClass();
                           if (itemClass.equals(String.class))
                           {
                              processSubElementsAsStrings(itemResult.toString(), pDef, pEle);
                           }
                           else //if (itemClass.equals(Object.class))
                           {
                              processSubElements(itemResult, pDef, pEle);
                           }
                        }
                     }
                  }
               }
            }
            
         }
         else
         {
            // this collection does not have a count method on it
            // this is a special case, right now only applies to IMultiplicity
            pEle.setElement(pDisp);
            processSubElements(pDisp, pDef, pEle);
         }
      }
      catch (NoSuchMethodException exe)
      {
         //This will come if there is no getCount method on this object
         // this collection does not have a count method on it
         // this is a special case, right now only applies to IMultiplicity
         pEle.setElement(pDisp);
         processSubElements(pDisp, pDef, pEle);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Process any sub elements based on sub property definitions for collections that are represented
    * by strings.  This is a special case that is happening when the get method is called and the
    * result is a collection of IStrings.  We go through the collection processing, but before we
    * were assuming objects as members of the collection.  So their sub elements were being built
    * based on the definitions for the member objects.  This case we have a string.  Since there is
    * nothing special about the sub element, we will just build a dummy one and set its value to what
    * is passed in.
    *
    * @param[in] sValue       The string that represents the value of the property element
    * @param[in] parentDef    The property definition to base the building of the property element on
    * @param[in] parentEle    The owning property element to which any subs will be added
    *
    * @return HRESULT
    *
    */
   private void processSubElementsAsStrings(String sValue,
   IPropertyDefinition parentDef,
   IPropertyElement parentEle)
   {
      // get the corresponding sub property definitions
      // still need to do this so that we have what the gui should display for it
      Vector<IPropertyDefinition> subDefs = parentDef.getSubDefinitions();
      if (subDefs != null)
      {
         // loop through to create a related property element
         int count = subDefs.size();
         for (int i=0; i<count; i++)
         {
            IPropertyDefinition pDef = subDefs.elementAt(i);
            
            // create the property element
            // passing in nothing, which will just create a dummy node with no subs
            // which is okay for this case
            IPropertyElement pEle = buildElement(null, pDef, parentEle);
            if (pEle != null)
            {
               pEle.setValue(sValue);
               
               // insert the newly created element as a child of the current element
               parentEle.addSubElement(pEle);
            }
         }
      }
   }
   
   /**
    * Method used to figure out what to do with the passed-in IDispatch.  Based on the property
    * definition and element, may want to do a create, or just set data.
    *
    * @param pDisp            The IDispatch(model element) to process
    * @param[in] pDef         The property definition to process
    * @param[in] pEle         The property element to use
    *
    * @return HRESULT
    *
    */
   public long processData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      if (pDef != null && pEle != null && pDisp != null)
      {
         String setMeth = pDef.getSetMethod();
         if (setMeth != null && !setMeth.equals(""))
         {
            long mult = pDef.getMultiplicity();
            if (mult == 1)
            {
               setData(pDisp, pDef, pEle);
            }
            else
            {
               processCollectionWithSet(pDisp, pDef, pEle);
            }
         }
         else
         {
            String createMeth = pDef.getCreateMethod();
            if (createMeth != null && !createMeth.equals(""))
            {
               createData(pDisp, pDef, pEle);
            }
            else
            {
               Vector elems = pEle.getSubElements();
               if (elems != null & !elems.isEmpty())
               {
                  for (int i=0; i<elems.size(); i++)
                  {
                     Object elem = elems.get(i);
                     if (elem instanceof IPropertyElement)
                     {
                        IPropertyElement subElem = (IPropertyElement)elems.get(i);
                        IPropertyDefinition subDef = subElem.getPropertyDefinition();
                        processData(pDisp, subDef, subElem);
                     }
                  }
               }
            }
         }
      }
      return 0;
   }
   
   /**
    *	Special processing for property definitions that have been marked as collections but have a "set" method
    * eg. IMultiplicity
    *
    * @param pDisp[in]		The current IDispatch
    * @param pDef[in]		The current property definition
    * @param pEle[in]		The current property element
    *
    * @return HRESULT
    *
    */
   private void processCollectionWithSet(Object pDisp, IPropertyDefinition pDef,
   IPropertyElement pEle)
   {
      try
      {
         String setMeth = pDef.getSetMethod();
         if (setMeth.length() > 0)
         {
            long mult = pDef.getMultiplicity();
            if (mult > 1)
            {
               String getMeth = pDef.getGetMethod();
               Class clazz = pDisp.getClass();
               Method[] meths = clazz.getMethods();
               Method meth = null;
               if (meths != null)
               {
                  for (int i=0; i<meths.length; i++)
                  {
                     Method method = meths[i];
                     if (method.getName().equals(getMeth))
                     {
                        meth = method;
                        break;
                     }
                  }
               }
               if (meth != null)
               {
                  Object obj = meth.invoke(pDisp, (Object[])null);
                  if (obj != null)
                  {
                     pEle.setElement(obj);
                     setDispatchOfAllSubElements(pEle, obj);
                  }
               }
            }
         }
      } catch (Exception e)
      {}
   }
   
   private void setDispatchOfAllSubElements(IPropertyElement pEle, Object obj)
   {
      try
      {
         if (pEle != null && obj != null)
         {
            String name = pEle.getName();
            pEle.setElement(obj);
            if (name.equals("Multiplicity"))
            {
               // multiplicity is a special case because the dispatch that is coming in is the element that
               // the multiplicity is on (attribute, parameter), but the sub elements of multiplicity and
               // the multiplicity object itself needs to have the IMultiplicity element on them
               IPropertyDefinition pDef = pEle.getPropertyDefinition();
               if (pDef != null)
               {
                  // get the parent of this property element (will be the attribute or parameter)
                  IPropertyElement pParent = pEle.getParent();
                  if (pParent != null)
                  {
                     // get its element (attribute or parameter)
                     Object pParentDisp = pParent.getElement();
                     if (pParentDisp != null)
                     {
                        // invoke the get_Multiplicity method on the IElement
                        String getMeth = pDef.getGetMethod();
                        Class clazz = pParentDisp.getClass();
                        Method[] meths = clazz.getMethods();
                        Method meth = null;
                        if (meths != null)
                        {
                           for (int i=0; i<meths.length; i++)
                           {
                              Method method = meths[i];
                              if (method.getName().equals(getMeth))
                              {
                                 meth = method;
                                 break;
                              }
                           }
                        }
                        if (meth != null)
                        {
                           Object object = meth.invoke(pParentDisp, (Object[])null);
                           if (object != null)
                           {
                              // now set the current property element's IElement to the IMultiplicity
                              pEle.setElement(object);
                              obj = object;
                           }
                        }
                     }
                  }
               }
            }
            // have the right element now as our pDisp
            Vector elems = pEle.getSubElements();
            if (elems != null && !elems.isEmpty())
            {
               for (int i=0; i<elems.size(); i++)
               {
                  Object subObj = elems.get(i);
                  if (subObj instanceof IPropertyElement)
                  {
                     IPropertyElement subElem = (IPropertyElement)subObj;
                     setDispatchOfAllSubElements(subElem, obj);
                  }
               }
            }
         }
      } catch (Exception e)
      {}
   }
   
   /**
    * Rebuild the information for the passed-in property element.  This first removes all
    * sub elements, then rebuilds them.
    *
    *
    * @param modEle[in]	Model element representing the element to be reloaded
    * @param pDef[in]		Definition of the element to be reloaded
    * @param pEle[in]		Element to be reloaded
    *
    * @return HRESULT
    *
    */
   public long reloadElement( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      if (pDisp != null && pDef != null && pEle != null)
      {
         Vector<IPropertyElement> elems = pEle.getSubElements();
         elems.clear();
         pEle.setSubElements(elems);
         processSubElements(pDisp, pDef, pEle);
         
         IPropertyElement parent = pEle.getParent();
         if(parent != null)
         {
             IPropertyElement newElement = buildElement(parent.getElement(), pDef, pEle);
             if(newElement != null)
             {
                 interpretElementValue(newElement);
                 pEle.setValue(newElement.getValue());
             }
         } 
      }
      return 0;
   }
   
   /**
    * Rebuild the information for the passed-in property element.  This first removes all
    * sub elements, then rebuilds only one sub element marked as a dummy node.
    *
    * This was needed for performance reasons in the property editor
    *
    *
    * @param modEle[in]	Model element representing the element to be reloaded
    * @param pDef[in]		Definition of the element to be reloaded
    * @param pEle[in]		Element to be reloaded
    *
    * @return HRESULT
    *
    */
   public long reloadElementWithDummy( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      if (pDef != null && pEle != null)
      {
         Vector<IPropertyElement> elems = pEle.getSubElements();
         elems.clear();
         pEle.setSubElements(elems);
         
         pEle.setName("dummy");
         pEle.setPropertyDefinition(pDef);
         Vector defs = pDef.getSubDefinitions();
         if (defs != null && !defs.isEmpty())
         {
            Object subPd = defs.get(0);
            if (subPd instanceof IPropertyDefinition)
            {
               IPropertyDefinition pd = (IPropertyDefinition)subPd;
               IPropertyElement subEle = null;
               if (pDisp != null)
                  subEle = buildElement(pDisp, pd, pEle);
               else
                  subEle = buildElement(m_ModelElement, pd, pEle);
               
               if (subEle != null)
                  pEle.addSubElement(subEle);
            }
         }
      }
      return 0;
   }
   
   public String getElementFile()
   {
      return m_ElementFile;
   }
   
   public void setElementFile( String value )
   {
      m_ElementFile = value;
   }
   
   /**
    * Begin the process to create a property element based on the property definition using an already
    * defined file.
    *
    * @param[in]	pDef   The property definition to base the building of the property element on
    * @param[out] pVal    The property elements that have been built
    *
    * @return HRESULT
    *
    */
   public IPropertyElement[] buildElementsUsingXMLFile( IPropertyDefinition pDef )
   {
      IPropertyElement[] pEles = null;
      try
      {
         //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         //dbf.setNamespaceAware(true);
         //DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = XMLManip.getDOMDocument(m_ElementFile);//db.parse(new File(m_ElementFile));
         
         // file has been loaded, now find all property elements with the same name as the passed
         // in property definition
         String pDefName = pDef.getName();
         
         String pattern = "//PropertyElement[@name=\'";
         pattern += pDefName;
         pattern += "\']";
         
         List list = XMLManip.selectNodeList(doc, pattern);
         if (list != null && list.size() > 0)
         {
            pEles = new PropertyElement[list.size()];
            for (int i=0; i<list.size(); i++)
            {
               Node node = (Node)list.get(i);
               IPropertyElementXML xmlElem = new PropertyElementXML();
               xmlElem.setName(pDefName);
               xmlElem.setPropertyDefinition(pDef);
               xmlElem.setPropertyElementManager(this);
               
               // set its information
               setAttributesXML(node, pDef, xmlElem);
               
               // process any sub elements
               processSubElementsUsingXMLFile(node, pDef, xmlElem);
               pEles[i] = xmlElem;
               
            }
         }
      } catch (Exception e)
      {
         
      }
      return pEles;
   }
   
   /**
    * Process any sub elements based on sub property definitions in a predetermined file
    *
    * @param[in] pNode        The XML node to get the information from
    * @param[in] parentDef    The property definition to base the building of the property element on
    * @param[in] parentEle    The owning property element to which any subs will be added
    *
    * @return HRESULT
    *
    */
   private void processSubElementsUsingXMLFile(Node node,
   IPropertyDefinition pDef,
   IPropertyElement pElem)
   {
      try
      {
/*      Vector elems = pDef.getSubDefinitions();
      if (elems != null && !elems.isEmpty()) {
        for (int i = 0; i < elems.size(); i++) {
          Object obj = elems.get(i);
          if (obj instanceof IPropertyDefinition) {
            IPropertyDefinition def = (IPropertyDefinition) obj;
            String subName = def.getName();
            String pattern = "aElement[@name=\'";
            pattern += subName;
            pattern += "\']";
            List list = XPathAPI.selectNodeList(node, pattern);
            if (list != null && list.getLength() > 0) {
              for (int j = 0; j < list.getLength(); j++) {
                Node newNode = list.item(j);
                IPropertyElementXML xmlElem = buildElementUsingXMLFile(def,
                    newNode);
                if (xmlElem != null) {
                  pElem.addSubElement(xmlElem);
                }
              }
            }
          }
        }
      }*/
         HashMap<String, IPropertyDefinition> elems = pDef.getHashedSubDefinitions();
         if (elems != null && node.getNodeType() == Node.ELEMENT_NODE)
         {
            org.dom4j.Element elem = (org.dom4j.Element)node;
            List list = elem.selectNodes("aElement");
            if (list != null)
            {
               int count = list.size();
               for (int i=0; i<count; i++)
               {
                  Node newNode = (Node)list.get(i);
                  if (newNode.getNodeType() == Node.ELEMENT_NODE)
                  {
                     org.dom4j.Element child = (org.dom4j.Element)newNode;
                     String name = child.attributeValue("name");
                     IPropertyDefinition def = (IPropertyDefinition)elems.get(name);
                     IPropertyElementXML xmlElem = buildElementUsingXMLFile(def,	newNode);
                     if (xmlElem != null)
                     {
                        pElem.addSubElement(xmlElem);
                     }
                  }
               }
            }
         }
      } catch (Exception e)
      {}
   }
   
   /**
    * Do the actual create of the property element based on a file
    *
    * @param[in] pDef    The property definition to base the building of the property element on
    * @param[in] pNode   The xml node to get the information from
    * @param[out] pEle   The property element that has been created
    *
    * @return HRESULT
    *
    */
   private IPropertyElementXML buildElementUsingXMLFile(IPropertyDefinition pDef, Node node)
   {
      IPropertyElementXML elem = new PropertyElementXML();
      setAttributesXML(node, pDef, elem);
      elem.setPropertyElementManager(this);
      processSubElementsUsingXMLFile(node, pDef, elem);
      return elem;
   }
   
   /**
    * Sets the information on the property element from the dom node in the given file
    *
    *
    * @param pNode[in]		The DOM Node
    * @param pDef[in]		The property definition representing the property element
    * @param pEle[in]		The property element to have its information set
    *
    * @return HRESULT
    *
    */
   private void setAttributesXML(Node node, IPropertyDefinition pDef, IPropertyElement elem)
   {
      if (node != null)
      {
         if (node instanceof org.dom4j.Element)
         {
            org.dom4j.Element ele = (org.dom4j.Element)node;
            
            Attribute nameNode = ele.attribute("name");
            String name = "";
            if (nameNode != null)
               name = nameNode.getValue();
            
            nameNode = ele.attribute("value");
            String value = "";
            if (nameNode != null)
               value = nameNode.getValue();
            
            if (value.equals(""))
               value = pDef.getValidValues2();
            
            elem.setName(name);
            elem.setValue(value);
            elem.setOrigValue(value);
            elem.setPropertyDefinition(pDef);
         }
         
      }
   }
   
   /**
    * Sometimes the information that is stored on the property element is not what
    * we want to display.  It may need to be interpreted, this is based on the information
    * on the property definition.
    *
    * @param pEle[in]	The property element
    *
    * @return HRESULT
    *
    */
   public void interpretElementValue( IPropertyElement pEle )
   {
      if (pEle != null)
      {
         IPropertyDefinition pDef = pEle.getPropertyDefinition();
         String vals = pDef.getValidValues();
         if (vals != null && vals.length() > 0)
         {
            // if the definition has a # in it, this is our way of saying that there is
            // an xpath query needed to be executed in order to determine the values
            // of the definition
            int pos = vals.indexOf("#");
            if (pos >= 0)
            {
               // this check didn't work for the edit control, who had a list of "signs"
               // representing public, protected, private, so do one more check
               // if the definition has a # in it and it is part of a list, then we need to
               // evaluate the list
               int pos2 = vals.indexOf("|#|");
               if (pos2 >= 0)
               {
                  processEnumeration(pEle);
               }
               else
               {
                  int pos4 = vals.indexOf("#Call");
                  if(pos4 >= 0)
                  {
                      processMethodCall(pEle, pDef);
                  }
                  else
                  {
                      int pos3 = vals.indexOf("");
                      if (pos3 >= 0)
                      {
                         processExpansionVar(pEle);
                      }
                      else
                      {
                         // validValues2 is used to store the results of whatever was in validValues
                         // if there is nothing in validValues2, then try to determine what should be there
                         String val2 = pDef.getValidValues2();
                         if (val2 == null || val2.length() == 0)
                         {
                            // last try to process is figure out if this is an xpath or not
                            String cType = pDef.getControlType();
                            if (cType != null && cType.equals("read-only"))
                               processXPath(pEle);
                         }
                      }
                  }
               }
            }
            else
            {
               pos = vals.indexOf("FormatString");
               if (pos >= 0)
               {
                  processFormatString(pEle);
               }
               else
               {
                  processEnumeration(pEle);
               }
            }
         }
         Vector<IPropertyElement> subElems = pEle.getSubElements();
         if (subElems != null) 
         {
             for(IPropertyElement sub : subElems) 
             {
                 interpretElementValue(sub);
             }
         }
      }
   }
   
   /**
    * The property definition states that the information to be displayed by the property element
    * should be a value from an xpath query.  Need to figure out what the value is.
    *
    * @param[in] pEle      The property element to be used
    *
    * @return HRESULT
    *
    */
   private void processXPath(IPropertyElement pEle)
   {
      try
      {
         IPropertyDefinition def = pEle.getPropertyDefinition();
         String values = def.getValidValues();
         if (values.length() > 0)
         {
            int pos = values.indexOf("#");
            if (pos >= 0)
            {
               // get the string without the "#", this is the xpath query
               String xpath = values.substring(pos + 1, values.length() - 1);
               Object obj = pEle.getElement();
               if (obj instanceof IElement)
               {
                  IElement elem = (IElement) obj;
                  Node node = elem.getNode();
                  if (node != null)
                  {
                     Node n = XMLManip.selectSingleNode(node, xpath);
                     if (n != null)
                     {
                        String str = n.getStringValue();
                        pEle.setValue(str);
                        pEle.setOrigValue(str);
                     }
                  }
               }
            }
         }
      } catch (Exception e)
      {}
   }
   /**
    * The property definition states that the information to be displayed by the property element
    * should be a format string.  Need to figure out what the format string is.
    *
    * @param[in] pEle      The property element to be used
    *
    * @return HRESULT
    *
    */
   private void processFormatString(IPropertyElement pElem)
   {
      if((m_Formatter == null) && (ProductRetriever.retrieveProduct() != null))
      {
         //m_Formatter = ProductHelper
         m_Formatter = ProductRetriever.retrieveProduct().getDataFormatter();
      }
      
      Object obj = pElem.getElement();
      if (obj != null)
      {
         if (obj instanceof IElement)
         {
            IElement elem = (IElement)obj;
            String value = m_Formatter.formatElement(elem);
            pElem.setValue(value);
            pElem.setOrigValue(value);
         }
      }
   }
   
   /**
    * The property definition states that the information to be displayed by the property element
    * may be something other than what is stored on the property editor.  For example, for visibility
    * a value of 0-3 is stored, but what we want to display is public, private, etc.
    *
    * @param[in] pEle      The property element to be used
    *
    * @return HRESULT
    *
    */
   public void processEnumeration(IPropertyElement pElem)
   {
      String val = pElem.getValue();
      try
      {
         if (val != null)
         {
            Integer i = new Integer(val);
            IPropertyDefinition def = pElem.getPropertyDefinition();
            if (def != null)
            {
               String values = def.getValidValues();
               String enums = def.getFromAttrMap("enumValues");
               if (values != null && values.length() > 0)
               {
                  IEnumTranslator trans = new EnumTranslator();
                  String value = trans.translateFromEnum(i.intValue(), values, enums);
                  pElem.setValue(value);
                  pElem.setOrigValue(value);
               }
            }
         }
      } catch (NumberFormatException e)
      {
         //the number conversion error must have happenned.
         pElem.setValue(val);
         pElem.setOrigValue(val); 
          /*
         String value = pElem.getTranslatedValue();
         pElem.setValue(value);
         pElem.setOrigValue(value);
           */
      }
   }
   
   /**
    * Call the given "insert" method from the property definition on the passed-in IDispatch using the information
    * from the property element.
    *
    * @param pDisp            The IDispatch to use for the method calls
    * @param[in] pDef         The property definition to process
    * @param[in] pEle         The property element to get the information from
    *
    * @return HRESULT
    *
    */
   public void insertData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle )
   {
      try
      {
         String insMeth = pDef.getInsertMethod();
         IPropertyDefinition tempDef = pDef;
         if (insMeth.length() == 0)
         {
            IPropertyElement elem = getInsertElement(pEle);
            if (elem != null)
            {
               IPropertyDefinition def = elem.getPropertyDefinition();
               if (def != null)
               {
                  insMeth = def.getInsertMethod();
                  tempDef = def;
               }
            }
         }
         
         if (insMeth.length() > 0)
         {
            // if a IDispatch hasn't been passed in, we are going to try and figure one out
            // we do that by going up the property element chain and finding the first one that
            // has a IElement on it
            if (pDisp == null)
            {
               Object newDisp = getModelElement(pEle);
               if (newDisp != null)
                  pDisp = newDisp;
            }
            if (pDisp != null)
            {
               Class clazz = pDisp.getClass();
               Method[] meths = clazz.getMethods();
               Method meth = null;
               if (meths != null)
               {
                  for (int i=0; i<meths.length; i++)
                  {
                     Method method = meths[i];
                     if (method.getName().equals(insMeth))
                     {
                        meth = method;
                        break;
                     }
                  }
               }
               
               if (meth != null)
               {
                  Class[] parms = meth.getParameterTypes();
                  Object[] args = new Object[parms.length];//buildParameters(parms, pEle);
                  args[0] = pEle.getElement();
                  Object obj = meth.invoke(pDisp, args);
                  pEle.setModified(false);
               }
            }
         }
      } catch (Exception e)
      {}
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
    * Method to navigate up the property element chain to retrieve the property element that represents
    * the one that should be inserted into (the one with an insert method)
    *
    * @param[in] pEle          The property element in which to get the insert element that it belongs to
    * @param[out] pInsertEle   The property element that is the element in which to perform the insert
    *
    * @return HRESULT
    *
    */
   private IPropertyElement getInsertElement(IPropertyElement pEle)
   {
      IPropertyElement elem = null;
      IPropertyDefinition def = pEle.getPropertyDefinition();
      if (def != null)
      {
         String insMeth = def.getInsertMethod();
         if (insMeth.length() == 0)
         {
            IPropertyElement parElem = pEle.getParent();
            if (parElem != null)
            {
               elem = getInsertElement(parElem);
            }
         }
         else
            elem = pEle;
      }
      return elem;
   }
   
   /**
    * Builds and returns an empty element structure based on the passed-in definition.
    *
    * @param[in] pDef			The property definition to base the building of the property element on
    * @param[out] pNewEle		The newly created empty property element
    *
    * @return HRESULT
    *
    */
   public IPropertyElementXML buildEmptyElementXML( IPropertyDefinition pDef )
   {
      IPropertyElementXML propEle = new PropertyElementXML();
      propEle.setName(pDef.getName());
      propEle.setPropertyDefinition(pDef);
      processEmptySubElementsXML(pDef, propEle);
      return propEle;
   }
   
   /**
    * Process any sub elements based on sub property definitions, creating them empty
    *
    * @param[in] parentDef    The property definition to base the building of the property element on
    * @param[in] parentEle    The owning property element to which any subs will be added
    *
    * @return HRESULT
    *
    */
   private void processEmptySubElementsXML(IPropertyDefinition pDef, IPropertyElement pEle)
   {
      Vector elems = pDef.getSubDefinitions();
      if (elems != null && !elems.isEmpty())
      {
         for (int i=0; i<elems.size(); i++)
         {
            Object obj = elems.get(i);
            if (obj instanceof IPropertyDefinition)
            {
               IPropertyDefinition def = (IPropertyDefinition)obj;
               IPropertyElement elem = buildEmptyElementXML(def);
               if (elem != null)
                  pEle.addSubElement(elem);
            }
         }
      }
   }
   
   /**
    * Save the passed in property elements to the given file.
    *
    * @param file[in]		The file to save the elements to
    * @param dtdFile[in]	The dtd file to use to validate the element
    * @param pEles[in]		The elements to save
    *
    * @return HRESULT
    */
   public void saveElementsToXMLFile( String file, String dtdFile, IPropertyElement[] pEles )
   {
      Document doc = getDOMDocumentForFile(file, dtdFile);
      if (doc != null)
      {
         if (pEles != null && pEles.length > 0)
         {
            for (int i=0; i<pEles.length; i++)
            {
               IPropertyElement elem = pEles[i];
               if (elem != null)
               {
                  if (elem instanceof IPropertyElementXML)
                  {
                     IPropertyElementXML xmlElem = (IPropertyElementXML)elem;
                     xmlElem.save(doc);
                  }
                  saveSubElementsToXMLFile(doc, elem);
               }
            }
         }
         //save the document to xml file here.
      }
   }
   
   private void saveSubElementsToXMLFile(Document doc, IPropertyElement ele)
   {
      Vector elems = ele.getSubElements();
      if (elems != null && !elems.isEmpty())
      {
         for (int i=0; i<elems.size(); i++)
         {
            Object obj = elems.get(i);
            if (obj instanceof IPropertyElementXML)
            {
               IPropertyElementXML elem = (IPropertyElementXML)obj;
               elem.save(doc);
            }
            if (obj instanceof IPropertyElement)
            {
               IPropertyElement elem = (IPropertyElement)obj;
               saveSubElementsToXMLFile(doc, elem);
            }
         }
      }
   }
   
   /**
    * Get a DOM Document for the passed-in file.
    *
    *
    * @param file[in]		The file that needs to be queried
    * @param pDoc[out]		The returned DOM document
    *
    * @return HRESULT
    *
    */
   private Document getDOMDocumentForFile(String fileName, String dtdFile)
   {
      Document doc = null;
      try
      {
         File file = new File(fileName);
         if (file.canWrite())
         {
            //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //DocumentBuilder db = dbf.newDocumentBuilder();
            if (file.exists())
            {
               doc = XMLManip.getDOMDocument(fileName);//db.parse(file);
            }
            else
            {
               boolean created = file.createNewFile();
               if (created )
               {
                  doc = XMLManip.getDOMDocument();//db.newDocument();
                  //put processing to create the doc with DTD declaration, propertyElem node etc.
                  //and save it.
                  
               }
            }
         }
      } catch (Exception e)
      {
         
      }
      return doc;
   }
   
   /**
    * Process any sub elements based on sub property definitions
    *
    * @param[in] modEle       The IDispatch(model element) to get the information from
    * @param[in] parentDef    The property definition to base the building of the property element on
    * @param[in] parentEle    The owning property element to which any subs will be added
    *
    * @return HRESULT
    *
    */
   private void processSubElements(Object modEle,
   IPropertyDefinition parentDef,
   IPropertyElement parentEle)
   {
      if (getCreateSubs())
      {
         Vector defs = parentDef.getSubDefinitions();
         if (defs != null && !defs.isEmpty())
         {
            for(int i=0; i<defs.size(); i++)
            {
               Object obj = defs.elementAt(i);
               if (obj instanceof IPropertyDefinition)
               {
                  IPropertyDefinition def = (IPropertyDefinition)obj;
                  IPropertyElement propEle = buildElement(modEle, def, parentEle);
                  if (propEle != null)
                     parentEle.addSubElement(propEle);
               }
            }
         }
      }
   }
   public Object interpretGetObjectDefinition(IPropertyElement pEle)
   {
      Object retVal = null;
      if (pEle != null)
      {
         IPropertyDefinition pDef = pEle.getPropertyDefinition();
         if (pDef != null)
         {
            String getObjStr = pDef.getFromAttrMap("getObject");
            if (getObjStr != null && getObjStr.length() > 0)
            {
               String getMethod = pDef.getGetMethod();
               if (getMethod != null && getMethod.length() > 0)
               {
                  String getParams = pDef.getFromAttrMap("getParameters");
                  if ( (getObjStr.length() > 0) && (getMethod.length() > 0))
                  {
                     // the definition has an object and a method stored in it that needs to be
                     // created and then invoked in order to have the value
                     // turn into a class id
                     try
                     {
                        Object actual = retrieveObject(getObjStr);
                        if (actual != null)
                        {
                           Class clazz = actual.getClass();
                           // the get method may not actually be a get_X, so try invoking a method with this name
                           // that is just a method.
                           // but first we will try and figure out the parameters needed by the method
                           if (getParams != null && getParams.length() > 0)
                           {
                              // invoke the method on this class
                              Class[] params = new Class[1];
                              params[0] = IElement.class;
                              java.lang.reflect.Method method = clazz.getMethod(getMethod, params);
                              // right now only going to figure out one, but someday we should parse this
                              // string for delimiters
                              Object pDispOnEle = pEle.getElement();
                              Object[] args = new Object[1];
                              args[0] = pDispOnEle;
                              retVal = method.invoke(actual, args);
                           }
                           else
                           {
                              // invoke the method on this class
                              java.lang.reflect.Method method = 
                                  clazz.getMethod(getMethod, (Class[])null);
                              
                              retVal = method.invoke(actual, (Object[])null);
                           }
                        }
                     }
                     catch(NoSuchMethodException ex)
                     {
                        int i = 0;
                     }
                     catch(Exception e)
                     {
                        int i = 0;
                     }
                     
                  }
               }
            }
         }
      }
      return retVal;
   }
   protected Object retrieveObject(String objName)
   throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      Object retVal = null;
      
      ICoreProduct product = ProductRetriever.retrieveProduct();
      if(product != null)
      {
         Class clazz = Class.forName(objName);
         retVal = clazz.newInstance();
      }
      
      return retVal;
   }
   
   private void processMethodCall(IPropertyElement pEle, IPropertyDefinition pDef)
   {
       if(pEle != null)
       {
           String values = pDef.getValidValues();
           if (values != null && values.length() > 0)
           {
               // has it been tagged as an xpath query
               if (values.indexOf("#Call") >= 0)
               {
                   int pos = values.indexOf("(");
                   if( pos >= 0 )
                   {
                       // get the string between the ( )'s, this is what needs to be passed back
                       String name = values.substring(pos + 1, values.length() - 1);
                       // now get the element on the property element
                       Object pDisp = pEle.getElement();
                       if ( pDisp != null )
                       {
                           Class clazz = pDisp.getClass();
                           try
                           {
                               Method getMethod = clazz.getMethod(name);
                               if(getMethod != null)
                               {
                                   Object value = getMethod.invoke(pDisp);
                                   if(value instanceof String)
                                   {
                                       String str = (String)value;
                                       pEle.setValue(str);
                                       pEle.setOrigValue(str);
                                   }
                               }
                           }
                           catch(NoSuchMethodException e)
                           {
                               
                           }
                           catch(IllegalAccessException ie)
                           {
                               
                           }
                           catch(InvocationTargetException te)
                           {
                               
                           }
                       }
                   }
               }
           }
       }
   }
   
   /**
    * The property definition states that the information to be displayed by the property element
    * should be a value from an expansion variable.  Need to figure out what the value is.
    *
    * @param[in] pEle      The property element to be used
    *
    * @return HRESULT
    *
    */
   private void processExpansionVar(IPropertyElement pEle)
   {
      if (pEle != null)
      {
         // get the definition
         IPropertyDefinition pDef = pEle.getPropertyDefinition();
         if (pDef != null)
         {
            // get its values
            String values = pDef.getValidValues();
            if (values != null && values.length() > 0)
            {
               // has it been tagged as an xpath query
               if (values.indexOf("#ExpansionVar") >= 0)
               {
                  int pos = values.indexOf("(");
                  if( pos >= 0 )
                  {
                     // get the string between the ( )'s, this is what needs to be passed back
                     String var = values.substring(pos + 1, values.length() - 1);
                     // now get the element on the property element
                     Object pDisp = pEle.getElement();
                     if ( pDisp instanceof IElement )
                     {
                        IElement pElement = (Element)pDisp;
                        // get this element's dom node
                        Node pNode = pElement.getNode();
                        if (pNode != null)
                        {
                           // get the core product
                           ICoreProduct pCoreProduct = ProductRetriever.retrieveProduct();
                           if (pCoreProduct != null)
                           {
                              ITemplateManager pTemplateMgr = pCoreProduct.getTemplateManager();
                              if (pTemplateMgr != null)
                              {
                                 IVariableFactory pFactory = pTemplateMgr.getFactory();
                                 if (pFactory != null)
                                 {
                                    String config = pTemplateMgr.getConfigLocation();
                                    IVariableExpander expander = new VariableExpander();
                                    expander.setConfigFile(config);
                                    expander.setManager( pTemplateMgr );
                                    pFactory.setExecutionContext( expander );
                                    IExpansionVariable pVar = pFactory.createVariableWithText(var);
                                    if (pVar != null)
                                    {
                                       String text = pVar.expand(pNode);
                                       pEle.setValue(text);
                                       pEle.setOrigValue(text);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
   
}
