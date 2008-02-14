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



package org.netbeans.modules.uml.ui.support;



import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocumentFactory;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ProjectEventsAdapter;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * 
 * @author Trey Spiva
 */
public class UserSettings
{
	private String m_SystemSettingsFile = "";
	
   /** Specifies that the navigation list view was selected last by the user. */
   public static int LIST_VIEW = 0;
   
   /** Specifies that the navigation report view was selected last by the user. */
   public static int REPORT_VIEW = 1;
   
   /** Specifies that the navigation list view was selected last by the user. */
   public static int OPEN_OPTION = 0;

   /** Specifies that the navigation report view was selected last by the user. */
   public static int PIN_OPTION = 1;
   
   private static HashMap < String, Document > m_DocumentMap = new HashMap < String, Document >();
   //private static HashMap         m_DocumentMap = new HashMap();
   private static DispatchHelper  m_Helper      = new DispatchHelper();
   
   static
   {
      m_Helper.registerForProjectEvents(new UserSettings.ProjectListener());
   }
   
   //**************************************************
   // Project Based Settings
   //**************************************************
   
   public boolean isOnlyShowNavigateWhenShift(IElement element)
   {
      boolean retVal = false;
      
      Element settingsElement = getProjectSettingsElement(element);
      if(settingsElement != null)
      {
         String value = settingsElement.attributeValue("isOnlyShowNavigateWhenShift");
         
         if(value != null)
         {
            retVal = value.equalsIgnoreCase("true");
         } 
      }
      
      return retVal;
   }

   public void setIsOnlyShowNavigateWhenShift(IElement element, boolean value)
   {
        if(element != null)
        {
            Element setting = createProjectSettingsElement(element);

            if(setting != null)
            {
                setting.addAttribute("isOnlyShowNavigateWhenShift", 
                     Boolean.toString(value));
                XMLManip.save(setting.getDocument(), getProjectFileLocation(element));
            }
        }
   }
   
   public void setDefaultDiagram(IElement element, String value)
   {
          if(element != null)
          {
                 Element setting = createProjectSettingsElement(element);

                 if(setting != null)
                 {
                        setting.addAttribute("associatedXMID", value);
                        setting.addAttribute("isAssociatedDiagram", "true");
                        XMLManip.save(setting.getDocument(), getProjectFileLocation(element));
                 }
          }
		
   }
   
   public void setDefaultPE(IElement element, String value)
   {
          if(element != null)
          {
                 Element setting = createProjectSettingsElement(element);

                 if(setting != null)
                 {
                        setting.addAttribute("associatedXMID", value);
                        setting.addAttribute("isAssociatedDiagram", "false");
                        XMLManip.save(setting.getDocument(), getProjectFileLocation(element));
                 }
          }
   }
   
   public String getDefaultNavigationTargetXMID(IElement associatedElement)
   {
      String retVal = "";
      
      if(associatedElement != null)
      {
         Element settings = getProjectSettingsElement(associatedElement);
         if(settings != null)
         {
            retVal = settings.attributeValue("associatedXMID");
         }
      }
      
      return retVal;
   }
   
   public boolean hasDefaultNavigationTarget(IElement associatedElement)
   {
      String value = getDefaultNavigationTargetXMID(associatedElement);
      
      boolean retVal = false;
      if((value != null) && (value.length() > 0))
      {
         retVal = true;
      }
      return retVal;
   }
   
   public boolean isDefaultNavigationTargetADiagram(IElement associatedElement)
   {
      boolean retVal = false;
      
      if(associatedElement != null)
      {
         Element settings = getProjectSettingsElement(associatedElement);
         if(settings != null)
         {
            String value = settings.attributeValue("isAssociatedDiagram");
            if(value != null)
            {
               retVal = value.equalsIgnoreCase("true");
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * @param parentModelElement
    */
   public void clearDefaultTarget(IElement associatedElement)
   {
      if(associatedElement != null)
      {
         Element settings = getProjectSettingsElement(associatedElement);
         if(settings != null)
         {
            Attribute xmiAttr = settings.attribute("associatedXMID");
            if (xmiAttr != null)
            {
                    settings.remove(xmiAttr);
            }
            Attribute diaAttr = settings.attribute("isAssociatedDiagram");
            if (diaAttr != null)
            {
                    settings.remove(settings.attribute("isAssociatedDiagram"));
            }
            XMLManip.save(settings.getDocument(), getProjectFileLocation(associatedElement));
         }
      }
   }
   
   //**************************************************
   // System Based Settings.   
   //**************************************************
   
   public int getDefaultPinButton(int defaultID)
   {
      int retVal = defaultID;
   
      Element settings = getSystemSettingsElement();
      if(settings != null)
      {
         String value = settings.attributeValue("PresNav_DefaultButton");
         if(value != null && value.equalsIgnoreCase("open") == true)
         {
            retVal = LIST_VIEW;
         }
         else
         {
            retVal = REPORT_VIEW;
         }
      }

      return retVal;
   }
      
   public int getDefaultListOrReportButton(int defaultID)
   {
      int retVal = defaultID;

      Element settings = getSystemSettingsElement();
      if(settings != null)
      {
         String value = settings.attributeValue("PresNav_DefaultListOrReport");
         if (value != null && value.equalsIgnoreCase("list") == true)
         {
            retVal = LIST_VIEW;
         }
         else
         {
            retVal = REPORT_VIEW;
         }
      }

      return retVal;
   }
   
	public String getFilterDialogModelElement(String name)
	{
		return getFilterDialogItem("ModelElements", name);
	}
	public String getFilterDialogDiagram(String name)
	{
		return getFilterDialogItem("Diagrams", name);
	}
	public void addFilterDialogModelElement(String name)
	{
		addFilterDialogItem("ModelElements", name);
	}
	public void addFilterDialogDiagram(String name)
	{
		addFilterDialogItem("Diagrams", name);
	}
	public void removeFilterDialogModelElement(String name)
	{
		removeFilterDialogItem("ModelElements", name);
	}
	public void removeFilterDialogDiagram(String name)
	{
		removeFilterDialogItem("Diagrams", name);
	}  
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * @param element
    */
   protected Document getSystemSettingsDocument(boolean forceCreate) 
      throws IOException
   {
      Document retVal = null;
   
		IConfigManager manager = ProductHelper.getConfigManager();
		String settingsPath = manager.getDefaultConfigLocation();
		m_SystemSettingsFile = settingsPath + ".Settings";
      
         
      retVal = m_DocumentMap.get("SystemSettings");
   
      if(retVal == null)
      {
         File settingsFile = new File(m_SystemSettingsFile);
         if(settingsFile.exists() == true)
         {
            retVal = XMLManip.getDOMDocument(m_SystemSettingsFile);
         }
      
         if((retVal == null) && (forceCreate == true))
         {
			retVal = DOMDocumentFactory.getInstance().createDocument();
			Element rootEle = DOMDocumentFactory.getInstance().createElement("Settings");
			if (rootEle != null){
				retVal.setRootElement(rootEle);
			}
				
            //retVal.add(DocumentHelper.createElement("Settings"));
            XMLManip.save(retVal, m_SystemSettingsFile);
         }
      
         if(retVal != null)
         {
            m_DocumentMap.put("SystemSettings", retVal);
         }
      }
   
      return retVal;

   }
   
   /**
    * @param element
    */
   protected Document getProjectSettingsDocument(IElement element, 
                                                 boolean  forceCreate) 
      throws IOException
   {
      Document retVal = null;
      
      if(element != null)
      {
         IProject project = element.getProject();
         if(project != null)
         {
            String settingsPath = getProjectFileName(project);
            
            retVal = m_DocumentMap.get(settingsPath);
            
            if(retVal == null)
            {
               File settingsFile = new File(settingsPath);
               if(settingsFile.exists() == true)
               {
                  retVal = XMLManip.getDOMDocument(settingsPath);
               }
               
               if((retVal == null) && (forceCreate == true))
               {
					retVal = DOMDocumentFactory.getInstance().createDocument();
					Element rootEle = DOMDocumentFactory.getInstance().createElement("Settings");
					if (rootEle != null){
						retVal.setRootElement(rootEle);
					}
                  XMLManip.save(retVal, settingsPath);
               }
               
               if(retVal != null)
               {
                  m_DocumentMap.put(settingsPath, retVal);
               }
            }
         }
      }
      
      return retVal;
   
   }
   
	private String getProjectFileLocation(IElement element)
	{
		String str = "";
		if(element != null)
		{
		   IProject project = element.getProject();
		   if(project != null)
		   {
		   		str = getProjectFileName(project);
		   }
		}
		return str;
	}

   private static String getProjectFileName(IProject project)
   {
//      String settingsPath = project.getBaseDirectory() + File.separator +
//                            ".ProjectSettings";
      
      String retVal = project.getBaseDirectory();
      
      if((retVal.endsWith(File.separator) == true) || 
         (retVal.endsWith("/") == true))
      {
          retVal += ".Settings";
      }
      else
      {
          retVal += File.separator + ".Settings";
      }
      return retVal;
   }
   
   /**
   * @param element
   */
  protected Element getSystemSettingsElement()
  {
     Element retVal = null;
     
     try
     {
        Document doc = getSystemSettingsDocument( true );
        if(doc != null)
        {
           retVal = doc.getRootElement();
        }
     }
     catch (IOException e)
     {
        retVal = null;
     }
   
     return retVal;
  }
  
  
   /**
    * @param element
    */
   protected Element getProjectSettingsElement(IElement element)
   {
      Element retVal = null;
      
      if(element != null)
      {
         try
         {
            Document doc = getProjectSettingsDocument(element, false);
            if(doc != null)
            {
				Element top = doc.getRootElement();
				if (top != null)
				{
					String xpath = "./Element[@xmi.id='" + element.getXMIID() + "']";
					retVal = (Element)top.selectSingleNode(xpath);
				}
            }
         }
         catch (IOException e)
         {
            retVal = null;
         }
      }
      
      return retVal;
   }
   
   protected Element createProjectSettingsElement(IElement element)
   {
      Element retVal = getProjectSettingsElement(element);
      
      try
      {
         Document doc = getProjectSettingsDocument(element, true);
         if((retVal == null) && (doc != null))
         {  
            retVal = XMLManip.createElement(doc, 
                                            "Element");
            retVal.addAttribute("xmi.id", element.getXMIID());
            
//            if(doc.getRootElement() != null)
//            {
//               doc.getRootElement().add(retVal);
//            } 
         }
      }
      catch (IOException e)
      {
         retVal = null;
      }
      
      return retVal;
   }
   
   public static class ProjectListener extends ProjectEventsAdapter
   {
      public void onProjectClosed(IProject project, IResultCell cell)
      {
         m_DocumentMap.remove(getProjectFileName(project));
      }
   }   
   
   public String getSettingValue(String heading, String setting)
   {
		String value = "";
		Element topSetting = getSystemSettingsElement();
		if(topSetting != null)
		{
			Node pNode = topSetting.selectSingleNode(heading);
			if (pNode != null)
			{
				Node pNode2 = pNode.selectSingleNode(setting);
				if (pNode2 != null)
				{
					value = pNode2.getText();
				}
			}
		}
	    return value;
   }
   public void setSettingValue(String heading, String setting, String value)
   {
		Element topSetting = getSystemSettingsElement();
		if(topSetting != null)
		{
			Document doc = topSetting.getDocument();
			if(doc != null)
			{  
				Node pNode = topSetting.selectSingleNode(heading);
				if (pNode == null)
				{
					pNode = createElement(topSetting, heading);
				}
				if (pNode != null)
				{
					Element pElement2 = (Element)pNode;
					Node pNode2 = pNode.selectSingleNode(setting);
					if (pNode2 == null)
					{
						pNode2 = createElement(pElement2, setting);
					}
					if (pNode2 != null)
					{
						pNode2.setText(value);
					}
				}
				XMLManip.save(doc, m_SystemSettingsFile);
			}
		}
   }
   protected Element createElement(Element parentElement, String nodeName)
   {
   	  return XMLManip.createElement(parentElement, nodeName);
   }
   
	private String getFilterDialogItem(String subHeading, String name)
	{
		String retVal = null;
		Element topSetting = getSystemSettingsElement();
		if(topSetting != null)
		{
			Node pNode = topSetting.selectSingleNode("ProjectTreeFilterDialog");
			if (pNode != null)
			{
				Node pNode2 = pNode.selectSingleNode(subHeading);
				if (pNode2 != null)
				{
					Node pNode3 = pNode2.selectSingleNode(name);
					if (pNode3 != null)
					{
						retVal = pNode3.getText();
					}
				}
			}
		}
		return retVal;
	}   
	private void addFilterDialogItem(String subHeading, String name)
	{
		Element topSetting = getSystemSettingsElement();
		if(topSetting != null)
		{
			Node pNode = topSetting.selectSingleNode("ProjectTreeFilterDialog");
			if (pNode == null)
			{
				pNode = createElement(topSetting, "ProjectTreeFilterDialog");
			}
			if (pNode != null)
			{
				Node pNode2 = pNode.selectSingleNode(subHeading);
				if (pNode2 == null)
				{
					pNode2 = createElement((Element)pNode, subHeading);
				}
				if (pNode2 != null)
				{
					Node pNode3 = pNode2.selectSingleNode(name);
					if (pNode3 == null)
					{
						pNode3 = createElement((Element)pNode2, name);
					}
				}
			}
			Document doc = topSetting.getDocument();
			if(doc != null)
			{
                            XMLManip.save(doc, m_SystemSettingsFile);
				
			}  
		}
	}
	public void removeFilterDialogItem(String subHeading, String name)
	{
		Element topSetting = getSystemSettingsElement();
		if(topSetting != null)
		{
			Node pNode = topSetting.selectSingleNode("ProjectTreeFilterDialog");
			if (pNode != null)
			{
				Node pNode2 = pNode.selectSingleNode(subHeading);
				if (pNode2 != null)
				{
					Node pNode3 = pNode2.selectSingleNode(name);
					if (pNode3 != null)
					{
						pNode3.detach();
					}
				}
			}
			Document doc = topSetting.getDocument();
			if(doc != null)
			{
                            XMLManip.save(doc, m_SystemSettingsFile);
				
			}  
		}
	}
   
}
