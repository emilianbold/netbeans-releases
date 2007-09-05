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



package org.netbeans.modules.uml.ui.controls.newdialog;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import org.netbeans.modules.uml.core.UMLSettings;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.util.NbBundle;

/**
 * @author sumitabhk
 *
 */
public class NewDialogUtilities
{
   private static Vector<IElement> m_Elements = new Vector<IElement>();
   
   private static final String BUNDLE_NAME ="org.netbeans.modules.uml.ui.controls.newdialog.Bundle"; // NOI18N
   
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
   
   /**
    *
    */
   public NewDialogUtilities()
   {
      super();
   }
   
   /**
    * Loads the namespace combo box
    *
    * @param pWnd [in] The combo box that is to be populated with namespaces.
    * @param pNamespace [in] The default namespace, can be NULL, specified by the caller.
    */
   public static void loadNamespace(JComboBox box, INamespace pNamespace)
   {
      resetElements();
      INamespace defaultNS = pNamespace;
      // Get the name of the namespace
      if (pNamespace != null)
      {
         addToNamespaceList(box, pNamespace, true);
      }
      
      else
      {
         // Get the current project as the default namespace
         IProject curProj = getProject();
         if (curProj != null)
         {
            addToNamespaceList(box, curProj, false);
         }
      }
      
      // cvc - CR#6269238
      // the namespace was loaded with all available UML projects but we
      //  only desire to have the active/selected project and the package
      //  namespaces that are relative to where the dialog was launched
      //  so the "populate with open projects" code is disabled
      // Now populate with open projects
//		IApplication pApp = ProductHelper.getApplication();
//		if (pApp != null)
//		{
//			ETList<IProject> projs = pApp.getProjects();
//			if (projs != null)
//			{
//				for (int i=0; i<projs.size(); i++)
//				{
//					IProject proj = projs.get(i);
//					addToNamespaceList(box, proj, false);
//				}
//			}
//		}
      
      // Now load all the selected namespaces in the project tree.
      IProjectTreeControl projTree = ProductHelper.getProjectTree();
      if (projTree != null)
      {
         IProjectTreeItem[] items = projTree.getSelected();
         
         if (items != null)
         {
            int count = items.length;
            // Add all the namespaces of each item, if it's a
            //diagram then add the namespace of the diagram
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = items[i];
               boolean isDiagram = false;
               INamespace eleNamespace =
                       getItemNamespace(item, isDiagram, true);
               
               if (eleNamespace != null)
                  addToNamespaceList(box, eleNamespace, true);
            }
            
            // If all the items are from the same namespace add the
            // namespace of the parent and make it the default do
            // this only if >1 item is selected.
            if (count > 1)
            {
               boolean sameNS = true;
               INamespace parentNS = null;
               for (int i=0; i<count; i++)
               {
                  IProjectTreeItem item = items[i];
                  IElement pElement = item.getModelElement();
                  if (pElement != null)
                  {
                     IElement owner = pElement.getOwner();
                     if (parentNS != null && owner != null)
                     {
                        boolean isSame = false;
                        isSame = owner.isSame(parentNS);
                        if (!isSame)
                        {
                           sameNS = false;
                           break;
                        }
                     }
                     else
                     {
                        parentNS = (INamespace) owner;
                     }
                  }
               }
               
               // Add the namespace of the parent
               if (sameNS && parentNS != null)
               {
                  addToNamespaceList(box, parentNS, false);
                  defaultNS = parentNS;
               }
            }
            
            // Now, if there is no current default, calculate the default
            if (defaultNS == null)
            {
               defaultNS = calculateDefaultNamespace(items);
            }
         }
      }
      
      // If the namespace has a name then add it and select
      if (defaultNS != null)
      {
         String qName = defaultNS.getQualifiedName();
         if (qName != null && qName.length() > 0)
         {
            // Make absolutely sure this item is on the list
            addToNamespaceList(box, defaultNS, false);
            box.setSelectedItem(qName);
         }
      }
   }
   
   
   /**
    * Returns the project from our new dialog context
    *
    * @param pProject [out,retval] The project
    */
   private static IProject getProject()
   {
      IProductProjectManager mgr = ProductHelper.getProductProjectManager();
      if (mgr != null)
      {
         return mgr.getCurrentProject();
      }
      return null;
   }
   
   /**
    * Returns the namespace from a combo box
    *
    * @param pWnd [in] The combo box where the namespace text resides
    * @param pSelectedNamespace [out,retval] The namespace this string represents.
    */
   public static INamespace getNamespace(String name)
   {
      INamespace retObj = null;
      retObj = getNamespaceFromList(name);
      return retObj;
   }
   
   /**
    * Gets an element from the list
    *
    * @param sNamespaceName [in] The name of the namespace we're looking for
    * @param pFoundElement [out,retval] The found namespace that matches the namespace name.
    */
   private static INamespace getNamespaceFromList(String name)
   {
      INamespace retSpace = null;
      if (name != null && name.length() > 0 && m_Elements != null)
      {
         int count = m_Elements.size();
         for (int i=0; i<count; i++)
         {
            IElement pEle = m_Elements.elementAt(i);
            if (pEle instanceof INamespace)
            {
               INamespace pSpace = (INamespace)pEle;
               String qName = pSpace.getQualifiedName();
               if (name.equals(qName))
               {
                  retSpace = pSpace;
                  break;
               }
            }
         }
      }
      return retSpace;
   }
   
   /**
    * Returns the location of the current workspace
    *
    * @return The directory location of the workspace.
    */
   public static String getWorkspaceLocation()
   {
      String retLocation = null;
      IWorkspace space = getWorkspace();
      if (space != null)
      {
         try
         {
            String location = space.getLocation();
            if (location != null && location.length() > 0)
            {
               retLocation = StringUtilities.getPath(location);
            }
         }
         catch (WorkspaceManagementException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return retLocation;
   }
   
   /**
    * Returns the workspace this result processor should use
    *
    * @param pWorkspace [out,retval] The workspace
    */
   private static IWorkspace getWorkspace()
   {
      IWorkspace retSpace = null;
      INewDialogContext context = new NewDialogContext();
      retSpace = context.getWorkspace();
      return retSpace;
   }
   
   /**
    * Returns the home location
    *
    * @return The location of our install location.
    */
   public static String getHomeLocation()
   {
      String homeStr = null;
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         IConfigManager manager = prod.getConfigManager();
         if (manager != null)
         {
            homeStr = manager.getHomeLocation();
         }
      }
      return homeStr;
   }
   
   /**
    * Adds an element to our list of elements in the combo box
    *
    * @param pElement [in] The element to add to our list of known namespaces
    */
   private static void addToNamespaceList(JComboBox box, INamespace space, boolean addParent)
   {
      if (space != null)
      {
         String fsn = space.getQualifiedName();
         int count = box.getItemCount();
         m_Elements.add(space);
         
         boolean foundInList = false;
         for (int i=0; i<count; i++)
         {
            String str = (String)box.getItemAt(i);
            if (fsn.equals(str))
            {
               foundInList = true;
               break;
            }
         }
         
         if (!foundInList)
         {
            box.addItem(fsn);
         }
         
         //m_Elements.add(space);
         
         if (addParent)
         {
            INamespace parent = space.getNamespace();
            if (parent != null)
            {
               // If we wanted to make sure that the entire "tree" is
               // part of the picklist, we would keep passing "true" in
               // for addParent here.
               addToNamespaceList(box, parent, true);
            }
         }
      }
   }
   
   /**
    *
    * Calculates the default namespace to appear in the picklist control
    *
    * @param items[in] The list of items selected.
    * @param pDefaultNamespace[out] The default namespace
    *
    * @return
    *
    */
   private static INamespace calculateDefaultNamespace(IProjectTreeItem[] items)
   {
      INamespace space = null;
      if (items != null)
      {
         // First, if there is only one item on the list, select that item's
         // parent as the default
         int count = items.length;
         int idx = 0;
         if (count == 1)
         {
            IProjectTreeItem item = items[idx];
            idx++;
            boolean isDiagram = false;
            space = getItemNamespace(item, isDiagram, true);
         }
         else if (count > 1)
         {
            // we want to find the deapest common namespace.
            
            // first, for the first element, create a complete list of namespaces.
            Vector<INamespace> firstList = new Vector<INamespace>();
            INamespace pOneAndOnlyOne = null;
            
            IProjectTreeItem item = items[0];
            boolean isDiagram = false;
            INamespace itemNamespace = getItemNamespace(item, isDiagram, false);
            if (itemNamespace != null)
            {
               firstList.add(itemNamespace);
               pOneAndOnlyOne = itemNamespace;
               
               // now go up
               INamespace parent = itemNamespace.getNamespace();
               while (parent != null)
               {
                  firstList.add(parent);
                  parent = parent.getNamespace();
               }
            }
            
            // now, for each item after this, we are going to create a common list,
            // and keep doing it until the common list contains only 1 item, or until
            // we run out of items.
            Vector<INamespace> commonList = firstList;
            int commonCount = commonList.size();
            int index=1;
            while (index < count && commonCount > 1)
            {
               IProjectTreeItem pItem = items[index];
               index++;
               
               // Create a NewCommon list
               Vector<INamespace> newCommonList = new Vector<INamespace>();
               boolean isDiag = false;
               INamespace pItemNamespace = getItemNamespace(pItem, isDiag, false);
               while (pItemNamespace != null)
               {
                  // look for this namespace in the current common list
                  boolean foundIt = false;
                  int commonIdx = 0;
                  while (!foundIt && commonIdx < commonCount)
                  {
                     INamespace pCommonItem = (INamespace)commonList.elementAt(commonIdx);
                     commonIdx++;
                     if (pCommonItem != null)
                     {
                        boolean isSame = false;
                        isSame = pCommonItem.isSame(pItemNamespace);
                        if (isSame)
                        {
                           foundIt = true;
                           // add this one to the new common list
                           newCommonList.add(pCommonItem);
                        }
                     }
                  }
                  
                  // now go up
                  INamespace parent = pItemNamespace.getNamespace();
                  pItemNamespace = parent;
               }
               
               // Ok, we have created a new common list
               commonList = newCommonList;
               commonCount = commonList.size();
            }
            
            // Now, because of the way that we build the common list, the first item
            // on the list should be the deepest.
            
            // we could use commonCount, but why make the code that spaghetti?
            boolean useOneAndOnly = true;
            if (commonList != null)
            {
               commonCount = commonList.size();
               if (commonCount > 0)
               {
                  INamespace firstCommon = (INamespace)commonList.elementAt(0);
                  useOneAndOnly = false;
                  space = firstCommon;
               }
            }
            
            // if all else failed, use the first namespace we ever found
            if (useOneAndOnly)
            {
               space = pOneAndOnlyOne;
            }
         }
      }
      return space;
   }
   
   /**
    *
    * Get the namespace of the project tree item.
    * If the item is a diagram, the namespace of the diaram is returned.
    * If the item is an element, the namespace returned is the namespace
    * of the element, unless the element is a namespace AND the flag
    * itemAsNamespace was set to true, meaning the user wanted the element
    * itself, not its namespace.
    *
    * @param pItem[in]
    * @param pNamespace[out]
    * @param itemIsDiagram[out]
    * @param itemAsNamespace[in]
    *
    * @return
    *
    */
   private static INamespace getItemNamespace(IProjectTreeItem item,
           boolean isDiagram,
           boolean itemAsNamespace)
   {
      INamespace retSpace = null;
      if (item != null)
      {
         boolean isDiag = false;
         boolean isWS = false;
         IElement pEle = item.getModelElement();
         isDiag = item.isDiagram();
         isWS = item.isWorkspace();
         
         if (isDiag)
         {
            // the item is a diagram. Get its namespace
            IProxyDiagram dia = item.getDiagram();
            if (dia != null)
            {
               retSpace = dia.getNamespace();
            }
            isDiagram = true;
         }
         else if (pEle != null || isWS)
         {
            if (pEle == null)
            {
               // Get the current project as the default namespace
               IProject curProj = getProject();
               if (curProj != null)
               {
                  pEle = curProj;
               }
            }
            
            if (itemAsNamespace)
            {
               // if the element is a namespace, the user wants that one.
               if (pEle instanceof INamespace)
               {
                  retSpace = (INamespace)pEle;
               }
               else if (pEle instanceof INamedElement)
               {
                  // We want this element's namespace
                  retSpace = ((INamedElement)pEle).getNamespace();
               }
            }
            else
            {
               if (pEle instanceof INamedElement)
               {
                  // We want this element's namespace
                  retSpace = ((INamedElement)pEle).getNamespace();
               }
            }
         }
      }
      return retSpace;
   }
   
   /**
    * Indicates the application product is "in round trip mode"
    */
   public static boolean isProductRoundTripOn()
   {
      boolean rtOn = true;
      ICoreProductManager man = CoreProductManager.instance();
      if (man != null)
      {
         ICoreProduct prod = man.getCoreProduct();
         if (prod != null)
         {
            IRoundTripController rtControl = prod.getRoundTripController();
            if (rtControl != null)
            {
               int mode = RTMode.RTM_LIVE;
               mode = rtControl.getMode();
               rtOn = (mode != RTMode.RTM_OFF);
            }
         }
      }
      return rtOn;
   }
   
   public static String getDefaultDiagramName()
   {
      //kris richards - "DefaultDiagramName" pref expunged. Set to "New Diagram".
      return NbBundle.getMessage (NewDialogUtilities.class, "NEW_DIAGRAM");
   }

   
    public static String getDefaultDiagramBaseName(int kind)
    {
        String key = "NEW_DIAGRAM"; // NOI18N
        
        switch (kind)
        {
        case IDiagramKind.DK_ACTIVITY_DIAGRAM:
            key = "PSK_ACTIVITY_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_CLASS_DIAGRAM:
            key = "PSK_CLASS_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_COLLABORATION_DIAGRAM:
            key = "PSK_COLLABORATION_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_COMPONENT_DIAGRAM:
            key = "PSK_COMPONENT_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_DEPLOYMENT_DIAGRAM:
            key = "PSK_DEPLOYMENT_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_SEQUENCE_DIAGRAM:
            key = "PSK_SEQUENCE_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_STATE_DIAGRAM:
            key = "PSK_STATE_DIAGRAM"; // NOI18N
            break;

        case IDiagramKind.DK_USECASE_DIAGRAM:
            key = "PSK_USE_CASE_DIAGRAM"; // NOI18N
            break;
        }
        
        return NbBundle.getMessage (NewDialogUtilities.class, key);
    }
    
    
    public static String getDefaultDiagramName(int kind)
    {
        return getDefaultDiagramBaseName(kind) + " " + getNextDiagramCounter(kind); // NOI18N
        
//        return getDefaultDiagramBaseName(kind) + " " + // NOI18N
//            UMLSettings.getDefault().getNewDiagramCount();
    }
     
    public static int getNextDiagramCounter(int kind)
    {
        if (getProject() == null)
        {
            // this means that a new project is being created
            // so all diagrams are reset to 1
            return 1;
        }
        
        else
        {
            ETList<IProxyDiagram> diagrams = ProxyDiagramManager.instance()
                .getDiagramsInProject(getProject());

            String baseName = getDefaultDiagramBaseName(kind);
            int baseLength = baseName.length();
            int maxNumber = 0;

            for (IProxyDiagram diagram: diagrams)
            {
                if (diagram.getDiagramKind() == kind)
                {
                    String dname = diagram.getName();
                    if (dname.length() > baseLength && dname.startsWith(baseName))
                    {
                        String ending = dname.substring(baseLength).trim();

                        try
                        {
                            Integer number = Integer.valueOf(ending);
                            if (number > maxNumber)
                                maxNumber = number;
                        }

                        catch (NumberFormatException ex)
                        {
                            // silently ignore this; means it wasn't a number
                        }
                    }
                } // for
            }

            return ++maxNumber;
        }
    }
   
   public static String getDefaultElementName()
   {
      String defName = null;
      // If there's no default name then get it from the preference accessor
      IPreferenceAccessor pPref = PreferenceAccessor.instance();
      if (pPref != null)
      {
         defName = pPref.getDefaultElementName();
      }
      return defName;
   }
   
   public static String getDefaultProjectName()
   {
      //kris richards - "DefaultProjectName" pref expunged. Set to "New Project".
      return NbBundle.getMessage(NewDialogUtilities.class, "NEW_PROJECT");
   }
   
   public static String getDefaultWorkspaceLocation()
   {
      String defName = null;
      // If there's no default name then get it from the preference accessor
      IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
      if (conMan != null)
      {
         defName = conMan.getDefaultConfigLocation();
         defName += "\\";
         defName += "Workspaces\\";
      }
      return defName;
   }
   
   public static String getDefaultPackageName()
   {
      String defName = null;
      defName = getDefaultElementName();
      return defName;
   }
   
   public static void loadDiagramTypes(JComboBox box)
   {
      //ConfigStringTranslator translator = new ConfigStringTranslator();
//		box.addItem("Activity Diagram");
//		box.addItem("Class Diagram");
//		box.addItem("Collaboration Diagram");
//		box.addItem("Component Diagram");
//		box.addItem("Deployment Diagram");
//		box.addItem("Sequence Diagram");
//		box.addItem("State Diagram");
//		box.addItem("Use Case Diagram");
      box.addItem(RESOURCE_BUNDLE.getString("PSK_ACTIVITY_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_CLASS_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_COLLABORATION_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_COMPONENT_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_DEPLOYMENT_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_SEQUENCE_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_STATE_DIAGRAM"));
      box.addItem(RESOURCE_BUNDLE.getString("PSK_USE_CASE_DIAGRAM"));
   }
   
   public static void loadModes(JComboBox box)
   {
      String defaultMode = "Implementation";
      //VERIFY(tempString.LoadString(IDS_ANALYSIS));
      box.addItem("Analysis");
      
      //VERIFY(tempString.LoadString(IDS_DESIGN));
      box.addItem("Design");
      
      //VERIFY(tempString.LoadString(IDS_IMPLEMENTATION));
      box.addItem("Implementation");
      
      //now set the default
      box.setSelectedItem(defaultMode);
   }
   
   public static void loadLanguages(JComboBox box)
   {
      String defaultLanguage = "Java";
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         ILanguageManager langMan = prod.getLanguageManager();
         if (langMan != null)
         {
            IStrings langs = langMan.getSupportedLanguages();
            if (langs != null)
            {
               long count = langs.getCount();
               for (int i=0; i<count; i++)
               {
                  String lang = langs.item(i);
                  box.addItem(lang);
               }
               
               //now set the default
               box.setSelectedItem(defaultLanguage);
            }
         }
      }
   }
   
   public static Icon getIconForResource(String elemType)
   {
      ImageIcon icon = null;
      String location = null;//RESOURCE_BUNDLE.getString(elemType);
      if (location == null || location.length() == 0)
      {
         String elem = StringUtilities.replaceAllSubstrings(elemType, " ", "");
         location = RESOURCE_BUNDLE.getString(elem);
      }
      
      if (location != null)
      {
         URL url = NewDialogUtilities.class.getResource(location);
         //File file = new File(location);
         if (url != null)
            icon = new ImageIcon(url);
      }
      return icon;
   }
   
   /**
    * If its a new project,we must clear this elements list
    */
   public static void resetElements()
   {
      m_Elements.clear();
   }
   
   public static String diagramKindToName(int nDiagramKind)
   {
      
      String diagramName = "";
      switch (nDiagramKind)
      {
         case IDiagramKind.DK_ACTIVITY_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_ACTIVITY_DIAGRAM");
            break;
         case IDiagramKind.DK_CLASS_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_CLASS_DIAGRAM");
            break;
         case IDiagramKind.DK_COLLABORATION_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_COLLABORATION_DIAGRAM");
            break;
         case IDiagramKind.DK_COMPONENT_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_COMPONENT_DIAGRAM");
            break;
         case IDiagramKind.DK_DEPLOYMENT_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_DEPLOYMENT_DIAGRAM");
            break;
         case IDiagramKind.DK_SEQUENCE_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_SEQUENCE_DIAGRAM");
            break;
         case IDiagramKind.DK_STATE_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_STATE_DIAGRAM");
            break;
         case IDiagramKind.DK_USECASE_DIAGRAM :
            diagramName = NewDialogResources.getString("PSK_USECASE_DIAGRAM");
            break;
      }
      
      return diagramName;
   }
   
   public static int diagramNameToKind(String diagramName)
   {
      
      int diagramKind = IDiagramKind.DK_UNKNOWN;
      if (diagramName == null || diagramName.length() == 0)
          return diagramKind;
      
      if (diagramName.equals(NewDialogResources.getString(
              "PSK_SEQUENCE_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_SEQUENCE_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_ACTIVITY_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_ACTIVITY_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_CLASS_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_CLASS_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_COLLABORATION_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_COMPONENT_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_COMPONENT_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_DEPLOYMENT_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_DEPLOYMENT_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_STATE_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_STATE_DIAGRAM;
      }
      else if (diagramName.equals(NewDialogResources
              .getString("PSK_USE_CASE_DIAGRAM"))) // NOI18N
      {
         diagramKind = IDiagramKind.DK_USECASE_DIAGRAM;
      }
      
      return diagramKind;
   }
}



