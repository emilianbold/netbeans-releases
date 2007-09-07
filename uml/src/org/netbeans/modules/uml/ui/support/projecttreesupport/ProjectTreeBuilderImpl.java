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


/*
 *
 * Created on Jun 12, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.projecttreesupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;

/**
 * 
 * @author Trey Spiva
 */
public class ProjectTreeBuilderImpl implements IProjectTreeBuilder
{
   private HashMap < String, Integer > m_SortMap = new HashMap < String, Integer >();
   //private HashMap m_SortMap = new HashMap();
   private IPropertyDefinitionFactory m_DefFactory = null;
   
   private ProjectTreeNodeFactory m_NodeFactory = null;
   
   private IProjectTreeBuilderFilter m_TreeFilter = null;
   private static String DEPENDENCIES_GROUP = 
           "Abstraction,Usage,Permission,Delegate,RoleBinding,Derivation,Dependency";
   
   public ProjectTreeBuilderImpl(ProjectTreeNodeFactory factory)
   {
      buildSortMap();    
      setNodeFactory(factory);  
   }
   

   /**
    * Based on Wolverine rules, we needed a way to get a list of elements that 
    * should be placed as child nodes in our tree structures.  We used to ask
    * for owned elements, but that does not map directly to the way that we 
    * want to display the tree.
    * 
    * For instance, a project has classes and associations as owned elements,
    * but we do not want to show associations at the top level, so this will
    * return only those owned elements that we want displayed at the top level
    * for a project (not associations).  If you pass this routine a class, its
    * out elements will include associations.
    *
    * @param pEle The element in which to retrieve its children
    * @return A collection of tree items (including elements, diagrams, relationships)
    */
   public ITreeItem[] retrieveChildItems(Object pEle)
   {
      ITreeItem[] retVal = null;
      
      if (pEle instanceof IElement)
      {
         retVal = retrieveChildItemsForElement(pEle, (IElement)pEle);
      }
      else if(pEle instanceof ITreeFolder)
      {
         ITreeFolder treeElement = (ITreeFolder)pEle;
         retVal = retrieveChildItemsForFolder(treeElement);
      }
      else if (pEle instanceof ITreeRelElement)
      {
      }
      else if (pEle instanceof ITreeElement)
      {
         ITreeElement treeElement = (ITreeElement)pEle;
         retVal = retrieveChildItemsForElement(pEle, treeElement.getElement());
      }
      else if (pEle instanceof IWorkspace)
      {
         retVal = retrieveChildItemsForWorkspace((IWorkspace)pEle);         
      }
      else if (pEle instanceof ITreeWorkspace)
      {
         ITreeWorkspace treeWorkspace = (ITreeWorkspace)pEle;
         retVal = retrieveChildItemsForWorkspace(treeWorkspace.getWorkspace()); 
      }
      else if (pEle instanceof ITreeWSProject)
      {
         ITreeWSProject treeProject = (ITreeWSProject)pEle;
         retVal = retrieveChildItemsForElement(pEle, treeProject.getRelatedProject());
      }
      
      return retVal;
   }

   /**
    * Based on the passed-in IElement, return a list of child
    * elements that should appear as child nodes.
    * 
    * @param pDisp[in]     The item in which to get its children (stored as a dispatch 
    *                      because if it is a tree element
    *                      in some paths, we were losing the parent)
    * @param pElement[in]  The element in which to get its children
    * @return              A collection of tree items (including elements, diagrams,
    *                      relationships)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#retrieveChildItemsForElement(java.lang.Object, com.embarcadero.describe.foundation.IElement)
    */
   public ITreeItem[] retrieveChildItemsForElement(Object pDisp, IElement pEle)
   {
      //ETList < ITreeItem > retList = new ETArrayList();
	  ArrayList < ITreeItem > retList = new ArrayList < ITreeItem >();
      //ArrayList retList = new ArrayList(); 
      ITreeItem[] retVal = null;     
      
      try
      {
         // The diagram go on the list first.
         retrieveDiagramsForElement(pDisp, pEle, retList);

         buildChildItemsForElementBasedOnDefinitions(pDisp, pEle, retList);

         removeCommonItems(retList);
      }
      catch (Exception e)
      {
         // HAVE TODO: Determine what to do about exceptions.
         e.printStackTrace();
      }
      finally
      {
         retVal = new ITreeItem[retList.size()];
         retList.toArray(retVal);
      }
      return retVal;
   }   

   /**
    * Get any predefined child elements of the passed in folder.
    * 
    *
    * @param pFolder Folder in which to retrieve its children
    * @param eleemtn The element that ownes the folder.
    * @return        A collection of tree items (elements, diagrams, 
    *                relationships)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#retrieveChildItemsForFolder(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder)
    */
   public ITreeItem[] retrieveChildItemsForFolder(ITreeFolder pFolder)
   {
      ITreeItem[] retVal = null;
      
      if((pFolder != null) && (pFolder.getElement() != null))
      {
         if((pFolder.getGetMethod() != null) && 
            (pFolder.getGetMethod().length() > 0))
         {
            //objToActOn = castUsingID(pFolder.getElement(), pFolder.getID());
            Object objToActOn = executeGetMethod(pFolder.getElement(),
                                                 pFolder.getID(), 
                                                 pFolder.getGetMethod());
                                          
            retVal = buildFolderChildrenFromObject(objToActOn, pFolder);
         } 
         else
         {
            IElement element = pFolder.getElement();
            IPropertyDefinition def = getDefinition(element);
            
            Debug.assertNotNull(def, "Unable to locate the property definition for " + pFolder.getName());
               
            if(def != null)
            {
               IPropertyDefinition subDef = def.getSubDefinition(pFolder.getName());
               if(subDef != null)
               {
                  ArrayList < ITreeItem > list = new ArrayList < ITreeItem >();
                  buildChildItemsForElementBasedOnDefinitions(null, 
                                                              subDef, 
                                                              element,
                                                              list);
                                                              
                  retVal = new ITreeItem[list.size()];                                            
                  for (int index = 0; index < list.size(); index++)
                  {
                  	 ITreeItem ti = list.get(index);
                  	 if (ti != null)
                  	 {
                             ti.setParentItem(pFolder);
	                     retVal[index] = ti;
                  	 }
                  }
                                                                       
               }
            }     
         }
         
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#getItems(java.lang.Object)
    */
   public ITreeItem[] getItems(Object parent)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#getFolders(java.lang.Object)
    */
   public String[] getFolders(Object parent)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#retrieveChildItemsForWorkspace(com.embarcadero.describe.workspacemanagement.IWorkspace)
    */
   public ITreeItem[] retrieveChildItemsForWorkspace(IWorkspace pWork)
   {
      ITreeItem[] retVal = null;
      try {
		if(pWork != null)
		{
		  ETList<IWSProject> wsProjects = pWork.getWSProjects();
        
		  int count = wsProjects.size();
		  retVal = new ITreeItem[count];
		  for(int index = 0; index < count; index++)
		  {
			 retVal[index] = createChildTreeProject(null, wsProjects.get(index));  
		  }
		}
      }
      catch(Exception e)
      {
      }
     
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#createChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, java.lang.Object)
    */
   public ITreeItem createChild(ITreeItem pParent, Object pDisp)
   {
      ITreeItem retVal = null;
      
      if(pDisp != null)
      {
         if(pDisp instanceof IElement)
         {
            retVal = createChildTreeElement(pParent, (IElement)pDisp);
         }
         else if(pDisp instanceof IProxyDiagram)
         {
            retVal = createChildTreeDiagram(pParent, (IProxyDiagram)pDisp);
         }
         else if(pDisp instanceof IWorkspace)
         {
            retVal = createChildTreeWorkspace(pParent, (IWorkspace)pDisp);
         }
         else if(pDisp instanceof IWSProject)
         {
            retVal = createChildTreeProject(pParent, (IWSProject)pDisp);
         }
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#sort(java.lang.Object, org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem[])
    */
   public ITreeItem[] sort(Object pParent, ITreeItem[] pIn)
   {
      ITreeItem[] retVal = null;
      
      if (pIn != null)
      {
		retVal = new ITreeItem[pIn.length];
      
      for (int index = 0; index < pIn.length; index++)
      {
         retVal[index] = pIn[index];
      }
    
      Comparator c = new ProjectTreeComparable();  
      Arrays.sort(retVal, c);
      
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#retrieveChildItemsSorted(java.lang.Object)
    */
   public ITreeItem[] retrieveChildItemsSorted(Object pParent)
   {
//      ITreeItem[] items = null;
//      if(pParent != null)
//      {  
//         if (pParent instanceof IElement)
//         {
//            IElement element = (IElement)pParent;
//            items = retrieveChildItemsForElement(pParent, element);
//         }
//         else if (Dispatch.isType(pParent, IElement.GUID) == true)
//         {
//            IElement element = new IElementProxy((Dispatch)pParent);
//            items = retrieveChildItemsForElement(pParent, element);
//         }
//         else if (pParent instanceof ITreeFolder)
//         {
//            items = retrieveChildItemsForFolder((ITreeFolder)pParent);
//         }
//         else if (pParent instanceof ITreeElement)
//         {
//            IElement element = ((ITreeElement)pParent).getElement();
//            items = retrieveChildItemsForElement(pParent, element);
//         }
//         else if (pParent instanceof ITreeRelElement)
//         {
//            
//         }
//      }
      
      ITreeItem[] items = retrieveChildItems(pParent);
      return sort(pParent, items);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder#getSortPriority(java.lang.String)
    */
   public long getSortPriority(String sType)
   {
      long retVal = 0;
      
      if(m_SortMap.containsKey(sType) == true)
      {
         Integer value = m_SortMap.get(sType);
         retVal = value.longValue();
      }
      return retVal;
   }

   /**
    * Retrieves the model elements that are affected by the changed element.
    * 
    * @param element The element that changed.
    * @param strs This is a an array of "|" delimited strings that state
    *             where in the xml definition file the element can live
    * @param items The model elements that are affected.
    */
   public void getInfoForRefresh(IElement             element, 
                                 ETList < String >    paths, 
                                 ETList < ITreeItem > items)
   {
      if((paths != null) && (items != null) && (element != null))
      {
         getPathsWhereElementTypeLives(element, paths);
         if (paths.size() > 0)
         {
         	ArrayList < ITreeItem > tempItems = new ArrayList < ITreeItem >();
            buildChildItemsForElementBasedOnDefinitions(null, element, tempItems);
            
            items.addAll(tempItems);
         }
      }
   }
   
   /**
    * Based on the xml definitions, get a list of paths where this element type
    * can reside in the tree.
    *
    * @param pElement The element in question
    * @param pStrs [IN/OUT] This is a an array of "|" delimited strings that state
    *              where in the xml definition file the element can live
    */
   protected void getPathsWhereElementTypeLives(IElement          element, 
                                                ETList < String > paths)
   {
       String query = null;
      if((paths != null) && (element != null))
      {
         try
         {
            String typeName = element.getElementType();
            if(typeName.length() > 0)
            {
               IPropertyDefinitionFactory factory = getFactory();
               Document doc = factory.getXMLDocument();
               
               // special case where type is a comma separated list of types
               if ( DEPENDENCIES_GROUP.indexOf(typeName) >= 0)
               {
                    query = "//*[@type=\'" + DEPENDENCIES_GROUP + "\']"; 
               } else {
                    query = "//*[@type=\'" + typeName + "\']";
               }
               List domNodes = doc.selectNodes(query);
               
               for (Iterator iter = domNodes.iterator(); iter.hasNext();)
               {
                  Node curNode = (Node)iter.next();
                  //paths.add(getNodePath(curNode));
                  
                  if (curNode instanceof Element)
                  {
                     paths.add(getNodePath(curNode));
                  }
               }
            }
         }
         catch(NullPointerException e)
         {
            // Ignore the null pointer.
         }
      }
   }
   
   protected String getNodePath(Node node)
   {
      String retVal = "";
      
      if (node instanceof Element)
      {
         Element element = (Element)node;
         String name = element.attributeValue("name"); 
         
         if(node.getParent() != null)
         {
            retVal = getNodePath(node.getParent());
            if(retVal.length() > 0)
            {
               retVal += "|";
            }
            retVal += name;
         }
      }
      
      return retVal;
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   protected void buildSortMap()
   {
      // Add our element type to icon index
      int sortIndex = 1;

      // The diagrams
      m_SortMap.put("Diagram", new Integer(sortIndex++));
//      m_SortMap.put("Diagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("Diagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("ActivityDiagram", new Integer(sortIndex++));
//      m_SortMap.put("ActivityDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("ActivityDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("ClassDiagram", new Integer(sortIndex++));
//      m_SortMap.put("ClassDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("ClassDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("CollaborationDiagram", new Integer(sortIndex++));
//      m_SortMap.put("CollaborationDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("CollaborationDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("ComponentDiagram", new Integer(sortIndex++));
//      m_SortMap.put("ComponentDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("ComponentDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("DeploymentDiagram", new Integer(sortIndex++));
//      m_SortMap.put("DeploymentDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("DeploymentDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("ImplementationDiagram", new Integer(sortIndex++));
//      m_SortMap.put("ImplementationDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("ImplementationDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("RobustnessDiagram", new Integer(sortIndex++));
//      m_SortMap.put("RobustnessDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("RobustnessDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("SequenceDiagram", new Integer(sortIndex++));
//      m_SortMap.put("SequenceDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("SequenceDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("StateDiagram", new Integer(sortIndex++));
//      m_SortMap.put("StateDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("StateDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("SummaryDiagram", new Integer(sortIndex++));
//      m_SortMap.put("SummaryDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("SummaryDiagram_CLOSED", new Integer(sortIndex++));
      m_SortMap.put("UseCaseDiagram", new Integer(sortIndex++));
//      m_SortMap.put("UseCaseDiagram_BROKEN", new Integer(sortIndex++));
//      m_SortMap.put("UseCaseDiagram_CLOSED", new Integer(sortIndex++));

      // Attributes,Operations,EnumerationLiterals and ActivityPartitions folders appear after the diagrams
      // The single single type and the folder type needs to have the same
      // sort index.
      m_SortMap.put("Attributes", new Integer(sortIndex));
      m_SortMap.put("Attribute", new Integer(sortIndex++));
      m_SortMap.put("Operations", new Integer(sortIndex));
      m_SortMap.put("Operation", new Integer(sortIndex++));
      
      m_SortMap.put("EnumerationLiterals", new Integer(sortIndex++));
      m_SortMap.put("ActivityPartitions", new Integer(sortIndex++));

      // Float these guys to the top so they appear before other elements
      m_SortMap.put("Activity", new Integer(sortIndex++));
      m_SortMap.put("Interaction", new Integer(sortIndex++));
      m_SortMap.put("StateMachine", new Integer(sortIndex++));
      m_SortMap.put("Package", new Integer(sortIndex++));
      m_SortMap.put("Class", new Integer(sortIndex++));
      m_SortMap.put("Interface", new Integer(sortIndex++));
      m_SortMap.put("AliasedType", new Integer(sortIndex++));
      m_SortMap.put("DataType", new Integer(sortIndex++));
      m_SortMap.put("Actor", new Integer(sortIndex++));
      m_SortMap.put("UseCase", new Integer(sortIndex++));
      m_SortMap.put("CombinedFragment", new Integer(sortIndex++));
      m_SortMap.put("Lifeline", new Integer(sortIndex++));
      m_SortMap.put("Message", new Integer(sortIndex++));

      m_SortMap.put("AbortedFinalState", new Integer(sortIndex++));
      m_SortMap.put("Abstraction", new Integer(sortIndex++));
      m_SortMap.put("ActionSequence", new Integer(sortIndex++));
      m_SortMap.put("ActivityFinalNode", new Integer(sortIndex++));
      m_SortMap.put("ActivityInvocation", new Integer(sortIndex++));
      m_SortMap.put("ActivityPartition", new Integer(sortIndex++));
      m_SortMap.put("Aggregation", new Integer(sortIndex++));
      m_SortMap.put("Argument", new Integer(sortIndex++));
      m_SortMap.put("Artifact", new Integer(sortIndex++));
      m_SortMap.put("AssemblyConnector", new Integer(sortIndex++));
      m_SortMap.put("AssignmentAction", new Integer(sortIndex++));
      m_SortMap.put("Association", new Integer(sortIndex++));
      m_SortMap.put("AssociationClass", new Integer(sortIndex++));
      m_SortMap.put("AssociationEnd", new Integer(sortIndex++));
      m_SortMap.put("AtomicFragment", new Integer(sortIndex++));
      //m_SortMap.put("Attribute", new Integer(sortIndex++));
      m_SortMap.put("CallAction", new Integer(sortIndex++));
      m_SortMap.put("CallEvent", new Integer(sortIndex++));
      m_SortMap.put("ChangeEvent", new Integer(sortIndex++));
      m_SortMap.put("ChangeSignal", new Integer(sortIndex++));
      m_SortMap.put("Collaboration", new Integer(sortIndex++));
      m_SortMap.put("CollaborationOccurrence", new Integer(sortIndex++));
      m_SortMap.put("Comment", new Integer(sortIndex++));
      m_SortMap.put("ComplexActivityGroup", new Integer(sortIndex++));
      m_SortMap.put("Component", new Integer(sortIndex++));
      m_SortMap.put("CompositeState", new Integer(sortIndex++));
      m_SortMap.put("Connector", new Integer(sortIndex++));
      m_SortMap.put("ConnectorEnd", new Integer(sortIndex++));
      m_SortMap.put("Constraint", new Integer(sortIndex++));
      m_SortMap.put("Container", new Integer(sortIndex++));
      m_SortMap.put("ControlFlow", new Integer(sortIndex++));
      m_SortMap.put("CreateAction", new Integer(sortIndex++));
      m_SortMap.put("DecisionNode", new Integer(sortIndex++));
      m_SortMap.put("DecisionMergeNode", new Integer(sortIndex++));
      m_SortMap.put("DataStoreNode", new Integer(sortIndex++));
      m_SortMap.put("Delegation", new Integer(sortIndex++));
      m_SortMap.put("DelegationConnector", new Integer(sortIndex++));
      m_SortMap.put("Dependency", new Integer(sortIndex++));
      m_SortMap.put("Deployment", new Integer(sortIndex++));
      m_SortMap.put("DeploymentSpecification", new Integer(sortIndex++));
      m_SortMap.put("Derivation", new Integer(sortIndex++));
      m_SortMap.put("DerivationClassifier", new Integer(sortIndex++));
      m_SortMap.put("DestroyAction", new Integer(sortIndex++));
      m_SortMap.put("Enumeration", new Integer(sortIndex++));
      m_SortMap.put("EnumerationLiteral", new Integer(sortIndex++));
      m_SortMap.put("Exception", new Integer(sortIndex++));
      m_SortMap.put("Expression", new Integer(sortIndex++));
      m_SortMap.put("Extend", new Integer(sortIndex++));
      m_SortMap.put("ExtensionPoint", new Integer(sortIndex++));
      m_SortMap.put("FinalState", new Integer(sortIndex++));
      m_SortMap.put("Flow", new Integer(sortIndex++));
      m_SortMap.put("FlowFinalNode", new Integer(sortIndex++));
      m_SortMap.put("ForkNode", new Integer(sortIndex++));
      m_SortMap.put("Gate", new Integer(sortIndex++));
      m_SortMap.put("Generalization", new Integer(sortIndex++));
      m_SortMap.put("Implementation", new Integer(sortIndex++));
      m_SortMap.put("Include", new Integer(sortIndex++));
      m_SortMap.put("Increment", new Integer(sortIndex++));
      m_SortMap.put("InitialNode", new Integer(sortIndex++));
      m_SortMap.put("InitialState", new Integer(sortIndex++));
      m_SortMap.put("InteractionConstraint", new Integer(sortIndex++));
      m_SortMap.put("InteractionOccurrence", new Integer(sortIndex++));
      m_SortMap.put("InteractionOperand", new Integer(sortIndex++));
      m_SortMap.put("InterGateConnector", new Integer(sortIndex++));
      m_SortMap.put("InterLifelineConnector", new Integer(sortIndex++));
      m_SortMap.put("InterruptibleActivityRegion", new Integer(sortIndex++));
      m_SortMap.put("InvocationNode", new Integer(sortIndex++));
      m_SortMap.put("ImportedElements", new Integer(sortIndex++));
      m_SortMap.put("ImportedPackages", new Integer(sortIndex++));
      m_SortMap.put("JoinNode", new Integer(sortIndex++));
      m_SortMap.put("JoinState", new Integer(sortIndex++));
      m_SortMap.put("MergeNode", new Integer(sortIndex++));
      m_SortMap.put("Model", new Integer(sortIndex++));
      m_SortMap.put("MultiFlow", new Integer(sortIndex++));
      m_SortMap.put("Multiplicity", new Integer(sortIndex++));
      m_SortMap.put("MultiplicityRange", new Integer(sortIndex++));
      m_SortMap.put("NavigableEnd", new Integer(sortIndex++));
      m_SortMap.put("Node", new Integer(sortIndex++));
      m_SortMap.put("ObjectFlow", new Integer(sortIndex++));
      m_SortMap.put("ObjectNode", new Integer(sortIndex++));
      //m_SortMap.put("Operation", new Integer(sortIndex++));
      m_SortMap.put("Parameter", new Integer(sortIndex++));
      m_SortMap.put("ParameterUsageNode", new Integer(sortIndex++));
      m_SortMap.put("Part", new Integer(sortIndex++));
      m_SortMap.put("PartDecomposition", new Integer(sortIndex++));
      m_SortMap.put("PartFacade", new Integer(sortIndex++));
      m_SortMap.put("Permission", new Integer(sortIndex++));
      m_SortMap.put("Port", new Integer(sortIndex++));
      m_SortMap.put("Ports", new Integer(sortIndex++));
      m_SortMap.put("PrimitiveType", new Integer(sortIndex++));
      m_SortMap.put("Procedure", new Integer(sortIndex++));
      m_SortMap.put("Profile", new Integer(sortIndex++));
      m_SortMap.put("ProtocolConformance", new Integer(sortIndex++));
      m_SortMap.put("ProtocolTransition", new Integer(sortIndex++));
      m_SortMap.put("PseudoState", new Integer(sortIndex++));
      m_SortMap.put("Realization", new Integer(sortIndex++));
      m_SortMap.put("Reception", new Integer(sortIndex++));
      m_SortMap.put("Region", new Integer(sortIndex++));
      m_SortMap.put("Regions", new Integer(sortIndex++));
      m_SortMap.put("ReturnAction", new Integer(sortIndex++));
      m_SortMap.put("RoleBinding", new Integer(sortIndex++));
      m_SortMap.put("SendAction", new Integer(sortIndex++));
      m_SortMap.put("Signal", new Integer(sortIndex++));
      m_SortMap.put("SignalNode", new Integer(sortIndex++));
      m_SortMap.put("SignalEvent", new Integer(sortIndex++));
      m_SortMap.put("SimpleState", new Integer(sortIndex++));
      m_SortMap.put("SourceFileArtifact", new Integer(sortIndex++));
      m_SortMap.put("State", new Integer(sortIndex++));
      m_SortMap.put("StateGroup", new Integer(sortIndex++));
      m_SortMap.put("Stereotype", new Integer(sortIndex++));
      m_SortMap.put("SubmachineState", new Integer(sortIndex++));
      m_SortMap.put("Subsystem", new Integer(sortIndex++));
      m_SortMap.put("TaggedValue", new Integer(sortIndex++));
      m_SortMap.put("TemplateArgument", new Integer(sortIndex++));
      m_SortMap.put("TemplateBinding", new Integer(sortIndex++));
      m_SortMap.put("TemplateParameter", new Integer(sortIndex++));
      m_SortMap.put("TerminateAction", new Integer(sortIndex++));
      m_SortMap.put("TimeEvent", new Integer(sortIndex++));
      m_SortMap.put("TimeSignal", new Integer(sortIndex++));
      m_SortMap.put("Transition", new Integer(sortIndex++));
      m_SortMap.put("UMLConnectionPoint", new Integer(sortIndex++));
      m_SortMap.put("UninterpretedAction", new Integer(sortIndex++));
      m_SortMap.put("Usage", new Integer(sortIndex++));
      m_SortMap.put("Delegate", new Integer(sortIndex++));
      m_SortMap.put("UseCaseDetail", new Integer(sortIndex++));
      m_SortMap.put("UseCaseDetails", new Integer(sortIndex++));

      m_SortMap.put("JoinForkNode", new Integer(sortIndex++));

      // Show relationships and other folders at the bottom
      m_SortMap.put("Messages", new Integer(sortIndex++));
      m_SortMap.put("Relationships", new Integer(sortIndex++));
   }
   
   /**
    * Removes items from the passed-in list that have the same IElement
    * as any of its parents.
    *
    * @param pItems The list of items to check to see if they need to be removed
    */
   protected void removeCommonItems(ArrayList < ITreeItem > itemList)
   {
      if(itemList.size() > 0)
      {
         // A place to start from.
         ITreeItem curItem = (ITreeItem)itemList.get(0);
         ITreeItem curParent = curItem.getParentItem(); 
         while(curParent != null)
         {
            IProjectTreeItem data = curParent.getData();
            if(curParent instanceof ITreeElement)
            {
               IElement curElement = data.getModelElement();
               
               // need special processing for a recursive association
               boolean continueFlag = true;
               if (curElement instanceof IAssociation)
               {
                  IAssociation assoc = (IAssociation) curElement;
                  if(assoc.getIsReflexive() == true)
                  {
                     continueFlag = false;
                     curParent = null;
                  }
               }
               
               if(continueFlag == true)
               {
                  if(itemList.contains(curParent) == true)
                  {
                     itemList.remove(curParent);
                  }
               }
               
            }  
            
            if(curParent != null)
            {
               curParent = curParent.getParentItem();          
            }
         }
      }
   }

   /**
    * Get any predefined child elements of the passed in element based on
    * the property definitions in a config xml file.
    *
    * @param pDisp[in]   The item in which to get its children (stored as a
    *                    dispatch because if it is a tree element
    *                    in some paths, we were losing the parent)
    * @param pEle[in]    Element in which to retrieve its children
    * @return A collection of tree items (elements, diagrams, relationships)
    */
   protected void buildChildItemsForElementBasedOnDefinitions(Object    pDisp, 
                                                              IElement  pEle,
                                                              ArrayList < ITreeItem > items)
   {
      IPropertyDefinition def = getDefinition(pEle);
      if(def != null)
      {
         buildChildItemsForElementBasedOnDefinitions(pDisp, def, pEle, items);
      }
   }

   /**
    * Get any predefined child elements of the passed in element based on the 
    * property definitions in a config xml file.
    *
    * @param pDisp[in]     The item in which to get its children (stored as a
    *                      dispatch because if it is a tree element in some 
    *                      paths, we were losing the parent)
    * @param pDef[in]      The definition that defines what child elements should 
    *                      be included in this element's list
    * @param pEle[in]      Element in which to retrieve its children
    * @return              A collection of tree items (elements, diagrams, 
    *                      relationships)
    */
   private void buildChildItemsForElementBasedOnDefinitions(Object               pDisp, 
                                                            IPropertyDefinition  def, 
                                                            IElement             pEle,
                                                            ArrayList < ITreeItem > items)
   {
      Vector subDefs = def.getSubDefinitions();
      if(subDefs != null)
      {
         if(pEle instanceof INamespace)
         {
            addNamespaceElements(pDisp, def, (INamespace)pEle, items);
         } 
         
         buildSubElementsForSubDefinition(pDisp, pEle, subDefs, items);
      }
   }


   private void buildSubElementsForSubDefinition( Object               pDisp,
                                                  IElement             pEle,
                                                  Vector               subDefs,
                                                  ArrayList < ITreeItem > items)
   {
      for (Iterator iter = subDefs.iterator(); iter.hasNext();)
      {
         IPropertyDefinition curDef = (IPropertyDefinition)iter.next();
         
         // don't process the exclude definition, because we already handled it
         if(curDef.getName().equals("Exclude") == false)
         {
            // See if this element type has been filtered out
            if(isHidden(curDef.getName()) == false)
            {
               // build the tree item based on the definition
               buildItem(pDisp, curDef, pEle, items); 
            }
            else
            {
               // if we come back from building a tree item and don't have 
               // one, this is because some of the rule processing that was
               // in the xml definition for this node (not enough items to 
               // create a folder, etc) so there may be some other 
               // processing that needs to take place.
               buildChildItemsForElementBasedOnDefinitions(pDisp, curDef, pEle, items);
            }
         }
      }
   }


   /**
    * The actual creation of an ITreeItem based on the passed-in information.
    *
    * @param pDisp The item in which to get its children (stored as a dispatch
    *              because if it is a tree element
    *              in some paths, we were losing the parent)
    * @param pDef  The property definition
    * @param pEle  The element that we are building the child items for
    * @return      The tree item that is a child item of the element
    */
   private void buildItem(Object               pDisp, 
                          IPropertyDefinition  def, 
                          IElement             pEle,
                          List < ITreeItem > items)
   {
      if((def != null) && (pEle != null) && (items != null))
      {
         String id      = def.getID();
         String defPath = def.getPath();
         
         if((def.getGetMethod() != null) && (def.getGetMethod().length() > 0))
         {            
            Object returnVal = executeGetMethod(def, pEle);
            if(returnVal != null)
            {
               // now we are assuming that the result of the get is either 
               // an object or a collection of objects.  So check if the
               // object is an instance of Collection.  If the object is 
               // not an instance then we have an object.
               if (returnVal instanceof Collection)
               {
                  buildCollection(pDisp, def, pEle, items, returnVal);
               }               
               else               
               {
                  Method countMethod = null;
                  try
                  {
                     // TODO: The reflection must be removed after the bindings are no longer used.
                     countMethod = returnVal.getClass().getMethod("getCount");
                  }
                  catch (SecurityException e)
                  {                     
                  }
                  catch (NoSuchMethodException e)
                  {
                  }
                  
                  if(countMethod != null)
                  {
                     buildBindingCollection(countMethod, pDisp, returnVal, def, pEle, items);
                  }
                  else
                  {
                     buildObjectItem(pDisp, def, returnVal, items);
                  }
               }
            }
         }
         else
         {
            buildFolder(pDisp, def, pEle, items);
         }
      }
   }


   /**
    * @param countMethod
    * @param pDisp
    * @param returnVal
    */
   protected void buildBindingCollection(Method               countMethod, 
                                         Object               parent, 
                                         Object               instance,
                                         IPropertyDefinition  def,
                                         IElement             pEle,
                                         List < ITreeItem >   items)
   {
      if(countMethod != null)
      {
         try
         {
            Object result = countMethod.invoke(instance);
            if((result != null) && (result instanceof Integer))
            {
               int count = ((Integer)result).intValue();
               if(count > 0)
               {
                  //ITreeFolder newFolder = new ProjectTreeFolderNode();
                  if(getNodeFactory() != null)
                  {
                     ITreeFolder newFolder = getNodeFactory().createFolderNode();
                     newFolder.setID(def.getID());
                     newFolder.setName(def.getName());
                     newFolder.setElement(pEle);
                     newFolder.setDisplayName(def.getDisplayName());
                     newFolder.setGetMethod(def.getGetMethod());    
                     newFolder.setSortPriority(getSortPriority(def.getName()));  
                     newFolder.setPathAsString(def.getPath());            
                     setTreeItemParent(newFolder, parent);
                  
                     String minSize = def.getFromAttrMap("minimum");
                     if((minSize != null) && (count >= Integer.parseInt(minSize)))
                     {
                        items.add(newFolder);
                     }
                     else
                     {
                        ITreeItem[] childItems = retrieveChildItemsForFolder(newFolder);
                        for (int index = 0; index < childItems.length; index++)
                        {
                           items.add(childItems[index]);
                        }
                     }
                  }
               }
            }
         }
         catch (NumberFormatException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (IllegalArgumentException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (IllegalAccessException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (InvocationTargetException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }      
   }

   /**
    * @param objToActOn
    * @return
    */
   protected ITreeItem[] buildFolderChildrenFromObject(Object instance, ITreeFolder folder)
   {
      ITreeItem[] retVal = null;
      
      IElement pElement = folder.getElement();
	  IPropertyDefinition pDef = getDefinition(pElement); 
	  IPropertyDefinition subDef = null;
	  if (pDef != null)
	  {
                String name = folder.getName();
                subDef = pDef.getSubDefinition(name);
	  }
      // TODO: The reflection must be removed after the bindings are no longer used.
      try
      {
         IElement curElement = null;
         if ( instance == null )
         {
             return retVal;
         }
         
         // Try not to use refletion if we know the instance is of type collection
         if (instance instanceof List)
         {   
             Object obj = null;
             List listItems = (List) instance;
             int itemCount = listItems.size();
             retVal = new ITreeItem[itemCount];
             for (int i = 0; i < itemCount; i++)
             {
                 obj = listItems.get(i);
                 if(obj != null && obj instanceof IElement)
                 {
                    curElement = (IElement) obj;
                    retVal[i] = createChildTreeElement(folder, curElement, subDef);
                 }
             }
         }
         else 
         {
             Class clazz = instance.getClass();

             Method countMethod = clazz.getMethod("getCount");     
             if(countMethod != null)
             {

                Object result = countMethod.invoke(instance);
                if((result != null) && (result instanceof Integer))
                {
                   Class[] paramType = {int.class};
                   Method itemMethod = instance.getClass().getMethod("item", paramType);
                   if(itemMethod != null)
                   {
                      int count = ((Integer)result).intValue();
                      retVal = new ITreeItem[count];
                      for(int index = 0; index < count; index++)
                      {
                         Object[] params =  { new Integer(index) };
                         curElement = (IElement)itemMethod.invoke(instance, params);
                         if(curElement != null)
                         {
                            retVal[index] = createChildTreeElement(folder, curElement, subDef);
                         }
                      }
                   }
                }
             }
         }
      }
      catch (NumberFormatException e)
      {
         // TODO Stop using when bindings are removed.
         e.printStackTrace();
      }
      catch (IllegalArgumentException e)
      {
         // TODO Stop using when bindings are removed.
         e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
         // TODO Stop using when bindings are removed.
         e.printStackTrace();
      }
      catch (InvocationTargetException e)
      {
         // TODO Stop using when bindings are removed.
         e.printStackTrace();
      }
      catch (NoSuchMethodException e)
      {
         // TODO Stop using when bindings are removed.
         e.printStackTrace();
      }  
      
      return retVal;
   }

   protected void buildFolder(Object               pDisp,
                              IPropertyDefinition  def,
                              IElement             pEle,
                              List < ITreeItem >   items)
   {
      // There is no get method on the property definition, but all 
      // definitions are there for a reason so create a folder node
      // currently, this is only for the relationship node
      String showStr = def.getFromAttrMap("show");
      if((showStr != null) && (showStr.toLowerCase().equals("true")))
      {
         //ProjectTreeFolderNode folder = new ProjectTreeFolderNode();
         if(getNodeFactory() != null)
         {
            ITreeFolder folder = getNodeFactory().createFolderNode();
            folder.setID(def.getID());
            
            folder.setName(def.getName());
            folder.setElement(pEle);
            folder.setDisplayName(def.getDisplayName(), false);
            folder.setSortPriority(getSortPriority(def.getName()));
            folder.setPathAsString((def.getPath()));
            setTreeItemParent(folder, pDisp);
            
            // only add the folder if there are children.
            ITreeItem[] fItems = retrieveChildItemsForFolder(folder);
            if((fItems != null) && (fItems.length > 0))
            {
               items.add(folder);
            }
         }
      }
   }


   protected void buildObjectItem(Object             pDisp, 
				IPropertyDefinition  def,
                                Object             item, 
                                List < ITreeItem > items)
   {
      // the result was not a collection, therefore it must be an  
      // object (like a Participant on a Association node) This 
      // will be a leaf in our tree, so make it a special kind of
      // tree element
      if(getNodeFactory() != null)
      {
         ITreeRelElement relElement = getNodeFactory().createRelationshipNode();
         if(item instanceof IElement)
         {
            IElement itemElement = (IElement)item;
            String name = "";
            if (item instanceof INamedElement)
            {
               INamedElement namedE = (INamedElement)item;
               name = namedE.getNameWithAlias();
            }
            
            if(name.length() <= 0)
            {
               name = itemElement.getElementType();
            }
            
            relElement.setName(name);
            relElement.setElement(itemElement);
            relElement.setPathAsString(def.getPath());
            relElement.setSortPriority(getSortPriority(itemElement.getElementType()));
            
            // setTreeItemParent calls setParentItem on relElement. 
            // setParentItem in TreeRelationshipNode will set the 
            // path of the element.
            setTreeItemParent(relElement, pDisp);
            items.add(relElement);
         }
      }
   }


   protected void buildCollection(Object               pDisp,
                                  IPropertyDefinition  def,
                                  IElement             pEle,
                                  List < ITreeItem >   items,
                                  Object               collection)
   {
      Collection getCollection = (Collection)collection;
      
      // the Object is a collection, so we want to see if the
      // collection is empty or not and if it is not, build
      // a folder node
      if(getCollection.size() > 0)
      {
         //ITreeFolder folder = new ProjectTreeFolderNode();
         if(getNodeFactory() != null)
         {
            ITreeFolder folder = getNodeFactory().createFolderNode();
            folder.setID(def.getID());
            folder.setName(def.getName());
            folder.setElement(pEle);
            folder.setDisplayName(def.getDisplayName());
            folder.setGetMethod(def.getGetMethod());   
            folder.setSortPriority(getSortPriority(def.getName())); 
            folder.setPathAsString(def.getPath());     
            setTreeItemParent(folder, pDisp);
            
            String value = def.getFromAttrMap("minimum");
            int min = -1;
            if(value != null)
            {
               min = Integer.parseInt(value);
            } 
            
            if( getCollection.size() >= min)
            {
               items.add(folder); 
            }
            else
            {
               ITreeItem[] childItems = retrieveChildItemsForFolder(folder);
               for (int index = 0; index < childItems.length; index++)
               {
                  items.add(childItems[index]);
               }
            }
         }
      }
   }


   /**
    * Sets the parent information on a tree item.
    *
    * @param pItem[in]	The tree item whose parent information needs to be set
    * @param pDisp  The parent (in IDispatch form)
    */
   private void setTreeItemParent(ITreeItem pItem, Object pDisp)
   {
	  // only set the parent if it is a ITreeItem
	  if (pItem != null && pDisp != null)
	  {
	  	if (pDisp instanceof ITreeItem)
	  	{
			ITreeItem pTreeItem = (ITreeItem)pDisp;
			pItem.setParentItem(pTreeItem);
	  	}
	  }
   }

   protected Object executeGetMethod(IElement object, String id, String name)
   {
      Object retVal = null;
      
      IElement curE = object;
//      if((id != null) && (id.length() > 0))
//      {
//         // TODO: The reflection must be removed after the bindings are no longer used.
//         {
//            Class classType;
//            try
//            {
//               classType = Class.forName(id);
//      
//      
//               Class[] params = null;//{com.embarcadero.com.Dispatch.class};
//               Constructor constructor = classType.getConstructor(params);      
//         
//               Object[] paramInstances = {curE};
//               curE = (IElement)constructor.newInstance(paramInstances);
//            }
//            catch (ClassNotFoundException e1)
//            {
//               // TODO Auto-generated catch block
//               e1.printStackTrace();
//            } catch (SecurityException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            } catch (NoSuchMethodException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            } catch (IllegalArgumentException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            } catch (InstantiationException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            } catch (IllegalAccessException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            } catch (InvocationTargetException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            }      
//         }
//      }

      Class elementClass = curE.getClass();
      try
      {         
         Method getMethod = elementClass.getMethod(name);
         retVal = getMethod.invoke(curE);
      }
      catch (SecurityException e)
      {
         // HAVE TODO: Determine what to do about excpetions.
         e.printStackTrace();
      }
      catch (NoSuchMethodException e)
      {
         // HAVE TODO: Determine what to do about excpetions.
         e.printStackTrace();
      } catch (IllegalArgumentException e)
      {
         // HAVE TODO: Determine what to do about excpetions.
         e.printStackTrace();
      } catch (IllegalAccessException e)
      {
         // HAVE TODO: Determine what to do about excpetions.
         e.printStackTrace();
      } catch (InvocationTargetException e)
      {
         // HAVE TODO: Determine what to do about excpetions.
         e.printStackTrace();
      }

      return retVal;
   }
   
   protected Object executeGetMethod(IPropertyDefinition def, IElement pEle)
   {
      return executeGetMethod(pEle, def.getID(), def.getGetMethod());
   }


	/**
	 * Keeps us in sync with the project tree engine so we don't load element types that have
	 * been filtered.
	 */
   protected boolean isHidden(String sName)
   {
   	boolean bHidden = false;
		if (m_TreeFilter != null && sName != null && sName.length() > 0)
		{
			bHidden = m_TreeFilter.isHidden(sName);
		}
      return bHidden;
   }


   protected void addNamespaceElements(Object               pDisp,
                                       IPropertyDefinition  def,
                                       INamespace           space,
                                       List < ITreeItem > items)
   {   
      // if this is a namespace element, we should have had an exclude 
      // definition in the file telling us which of the "owned elements"
      // to not include in the list
      IPropertyDefinition excludeDef = def.getSubDefinition("Exclude");
      if(excludeDef != null)
      {
		   // get the project's owned elements as our starting point
         ETList<INamedElement> namedElements = space.getOwnedElements();
         if(namedElements != null)
         {
            long count = namedElements.size();
            
            for(int index = 0; index < count; index++)
            {
               IElement curElement = namedElements.get(index);
               if(curElement != null)
               {
                  // based on wolverine rules, we do not want to show 
                  // relationships at the top level of the project, 
                  // but some of them are returned as owned elements 
                  // of the project, so we need to filter them out here.
                  if(isExcluded(curElement, excludeDef) == false)
                  {
                     ITreeItem newItem = createChildTreeElement(pDisp, 
                                                                curElement, 
                                                                def);
                                                                
                     items.add(newItem);
                  }
               }
            }
         }
      }
   }


   /**
    * Common method to create a ITreeElement.
    * 
    *
    * @param pParent The parent in which to add the new tree element to
    * @param element The element who represents the new tree element
    * @return        The newly created tree item
    */
   private ITreeItem createChildTreeElement(Object              pDisp, 
                                            IElement            element)
   {
      return createChildTreeElement(pDisp, element, null);
   }

   /**
    * Common method to create a ITreeElement.
    * 
    *
    * @param pParent The parent in which to add the new tree element to
    * @param pEle    The element who represents the new tree element
    * @param pDef    The definition to use when creating the new tree element
    * @return        The newly created tree item
    */
   private ITreeItem createChildTreeElement(Object              parent, 
                                            IElement            element, 
                                            IPropertyDefinition def)
   {
      ITreeItem retVal = null;
    
      if(element != null)
      {
         String name = "";
         if(def != null)
         {
            name = def.getName();
         }
         
          
         // special case processing for participants of an association
         // because they are members of a collection, tree elements were being created
         // we want tree rel elements to be created so that they are dead end nodes
         if(name.equals("Participants") == true)
         {
            //ITreeRelElement relNode = new TreeRelationshipNode();
            if(getNodeFactory() != null)
            {
               ITreeRelElement relNode = getNodeFactory().createRelationshipNode();
               fillTreeElement(parent, element, def, relNode);
               retVal = relNode;
            }
         }
         else
         {
            if(getNodeFactory() != null)
            {
               ITreeElement treeElement = getNodeFactory().createElementNode();
               
               fillTreeElement(parent, element, def, treeElement);
               retVal = treeElement;
            }
         }
      }
      
      return retVal;
   }


   /**
    * Common method to populate the tree element.
    * 
    *
    * @param pParent   The parent of the tree item
    * @param pEle      The element representing the tree item
    * @param pDef      The xml definition representing the tree item
    * @param pItem     The tree item in which to populate
    */
   private void fillTreeElement(Object parent, 
                                IElement element, 
                                IPropertyDefinition def, 
                                ITreeElement treeElement)
   {
      String elementType = element.getElementType();
      
      String name = "";
      String displayName = "";
      if((def != null) && (def.getValidValues() != null))
      {
         String validValues = def.getValidValues();
         if(validValues.equals("FormatString") == true)
         {
            IDataFormatter formatter = ProductHelper.getDataFormatter();
            if(formatter != null)
            {
               name = formatter.formatElement(element);
            }
         }
      }
      
      if( 
      		( (name == null) || (name.length() <= 0)) && 
      		(element instanceof INamedElement)
      	)
      {
         name = ((INamedElement)element).getNameWithAlias();
      }
      
      if((name == null) || (name.length() <= 0))
      {
         name = elementType;
         treeElement.setTranslateName(true);
      }
      
      treeElement.setName(name);
      
      treeElement.setElement(element);
      
      treeElement.setSortPriority(getSortPriority(elementType));
      
      if (parent instanceof ITreeItem)
      {
         ITreeItem pTreeItem = (ITreeItem)parent;
   		String path = pTreeItem.getPathAsString();
   		if (path != null && path.length() > 0)
   		{
   			path += "|";
   		}
   		path += elementType;
   		treeElement.setPathAsString(path);
      }
      else
      {
   		treeElement.setPathAsString(elementType);
      }
	   setTreeItemParent(treeElement, parent);
   }
   
   public boolean isExcluded(IElement testElement)
   {
       boolean retVal = false;
       
       IElement parent = testElement.getOwner();
       
       if(parent != null)
       {
           IPropertyDefinition def = getDefinition(parent);        
           
           if(def != null)
           {
            IPropertyDefinition excludeDef = def.getSubDefinition("Exclude");
            retVal = isExcluded(testElement, excludeDef);
           }
       }
       
       return retVal;
   }
   
   /**
    * Method that eliminates certain items from the list of owned elements of a project.
    * 
    * @param pElement The element in question
    * @param pDef     The associated property definition
    * @return         Whether or not it should be included
    */
   private boolean isExcluded(IElement            curElement, 
                              IPropertyDefinition excludeDef)
   {
      boolean retVal = false;
      
      try
      {
         if(excludeDef.getSubDefinitions().size()> 0)
         {
            String type = curElement.getElementType();
            if(excludeDef.getSubDefinition(type) != null)
            {
               retVal = true;
            }
         }
      }
      catch (NullPointerException e)
      {
         // HAVE TODO: Determine what to do about excpetions. 
      }
      
      return retVal;
   }


   /**
    * Get any diagrams owned by the passed-in element.
    * 
    * @param parent  The item in which to get its children (stored as a 
    *                dispatch because if it is a tree element in some paths,
    *                we were losing the parent)
    * @param pEle    Element in which to retrieve its owned diagrams
    * @param retList A collection of tree items (diagrams)
    */
   public void retrieveDiagramsForElement(Object    parent, 
                                             IElement  pEle, 
                                             ArrayList < ITreeItem > retList)
   {
      // only namespace elements can own diagrams
      if (pEle instanceof INamespace)
      {
         INamespace space = (INamespace)pEle;
         IProxyDiagramManager manager = ProxyDiagramManager.instance();
         if(manager != null)
         {
            // get the xmi id of the passed in element
            // get the diagrams owned by this namespace and add them to
            // the out array
            ETList<IProxyDiagram> diagrams = manager.getDiagramsInNamespace(space);
            if((diagrams != null) && diagrams != null)
            {
               for (int index = 0; index < diagrams.size(); index++)
               {
                  //ITreeDiagram newItem = new ProjectTreeDiagramNode(diagrams.get(index));
                  if(getNodeFactory() != null)
                  {
                     ITreeDiagram newItem = getNodeFactory().createDiagramNode(diagrams.get(index));
                     newItem.setDisplayedName(diagrams.get(index).getNameWithAlias());
					 // cvc - CR#6265213   
					 // the tree node's and the diagram's name was constantly 
					 //  being reset to the diagram type name ???
					 // newItem.setName(diagrams.get(index).getDiagramKindName());
                     newItem.setName(diagrams.get(index).getNameWithAlias());
                     newItem.setSortPriority(getSortPriority(diagrams.get(index).getDiagramKindName()));
                     setTreeItemParent(newItem, parent);
                     retList.add(newItem);
                  }
               }
            }
         }         
      }
   }
   
   
   protected IPropertyDefinitionFactory getFactory()
   {
      if( m_DefFactory == null)
      {
         String file = getDefinitionFile();
         m_DefFactory = new PropertyDefinitionFactory();
         m_DefFactory.setDefinitionFile(file);
         m_DefFactory.buildDefinitionsUsingFile();
      }
      
      return m_DefFactory;   
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
         buffer.append("ProjectTreeDefinitions.etc");
         retVal = buffer.toString();
      }
      
      return retVal;
   }
   
   protected IPropertyDefinition getDefinition(IElement element)
   {
      IPropertyDefinition retVal = null;
      
      if(element != null)
      {
         retVal = getDefinition(element.getElementType());
      }
      
      return retVal;
   }
   
   protected IPropertyDefinition getDefinition(String elementType)
   {
      IPropertyDefinition retVal = null;
   
      IPropertyDefinitionFactory factory = getFactory();
      if(factory != null)
      {
         StringTokenizer types = new StringTokenizer(elementType, "|");
         
         if(types.hasMoreTokens() == true)
         {
            String topType = types.nextToken();
            IPropertyDefinition def = factory.getPropertyDefinitionForElement(topType, null);
            if(def != null)
            {
               IPropertyDefinition curDef = def;
               
               // have the top level element, so keep looping through the sub elements
               // finding each one as a sub element of the previous
               while(types.hasMoreTokens() == true)
               {
                  IPropertyDefinition subDef = curDef.getSubDefinition(types.nextToken());
                  curDef = subDef;                  
               }
               
               if(curDef != null)
               {
                  retVal = curDef;
               }
            }
         }
      }
   
      return retVal;
   }
   
   /**
    * @param pParent
    * @param project
    */
   private ITreeItem createChildTreeProject(ITreeItem pParent, IWSProject project)
   {
      ITreeItem retVal = null;
      
      if(project != null)
      {
         String name = project.getName();
         if(name.length() > 0)
         {
            if(getNodeFactory() != null)
            {
               ITreeItem newNode = getNodeFactory().createProjectNode();
               newNode.setName(name);
               newNode.getData().setDescription(IProjectTreeControl.PROJECT_DESCRIPTION);    
               
               if(pParent != null)
               {
                  setTreeItemParent(newNode, pParent);        
               }
               retVal = newNode;
            }
         }
      }
      
      return retVal;
   }


   /**
    * @param pParent
    * @param workspace
    */
   private ITreeItem createChildTreeWorkspace(ITreeItem  pParent, 
                                              IWorkspace workspace)
   {
      ITreeItem retVal = null;
      if (workspace != null)
      {
      	String name = workspace.getName();
      	if (name != null && name.length() > 0)
      	{
   			//ITreeWorkspace newNode = new TreeWorkspaceNode();
            if(getNodeFactory() != null)
            {   
               ITreeWorkspace newNode = getNodeFactory().createWorkspaceNode();
      			newNode.setName(name);
      			
      			newNode.setWorkspace(workspace);    
                  
      			if(pParent != null)
      			{
      			   setTreeItemParent(newNode, pParent);        
      			}
      			retVal = newNode;
            }
      	}
      }
      return retVal;
   
   }


   /**
    * @param pParent
    * @param diagram
    */
   private ITreeItem createChildTreeDiagram(ITreeItem     pParent, 
                                            IProxyDiagram diagram)
   {
      ITreeItem retVal = null;
      return retVal;
   
   }
   
   /**
    * Retrieves the node factory used to create project tree items.
    * 
    * @return The factory to use.
    */
   protected ProjectTreeNodeFactory getNodeFactory()
   {
      return m_NodeFactory;
   }

   /**
    * Sets the node factory to use when creating project tree items
    * 
    * @param factory The new factory.
    */
   public void setNodeFactory(ProjectTreeNodeFactory factory)
   {
      m_NodeFactory = factory;
   }

	/**
	 * Manages what elements are filtered so we don't do extra processing
	 *
	 * @param pFilter [out,retval] The filter provided to us
	 */
	public IProjectTreeBuilderFilter getProjectTreeBuilderFilter()
	{
		return m_TreeFilter;
	}

	/**
	 * Manages what elements are filtered so we don't do extra processing
	 *
	 * @param pFilter [in] The filter provided to us
	 */
	public void setProjectTreeBuilderFilter(IProjectTreeBuilderFilter filter)
	{
		m_TreeFilter = filter;
	}

}
