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
 * Created on Jun 10, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductInitEventsAdapter;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementLifeTimeEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementModifiedEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.MetaLayerRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamespaceModifiedEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AffectedElementEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AttributeEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ClassifierFeatureEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.OperationEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.TypedElementEventsAdapter;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.filter.ProjectTreeFilterDialogEventsAdapter;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEngine;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeEventsAdapter;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.modules.uml.ui.support.DiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.IDiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.BatchProcessRunnable;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.IProjectTreeBuilderFilter;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeRelElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeBuilderImpl;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.MetaModelHelper;
import org.netbeans.modules.uml.ui.swing.drawingarea.DrawingAreaEventsAdapter;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaPropertyKind;
import org.netbeans.modules.uml.ui.swing.projecttree.ISwingProjectTreeModel;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;
import org.netbeans.modules.uml.ui.swing.projecttree.ProjectTreeResources;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * The ADProjectTreeEngine is used to add Application Designer
 * items to the project tree.  The configuration file
 * ProjectTreeEngine.etc is used to specify the items that can be
 * added to the tree.
 *
 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEngine
 * @author Trey Spiva
 */
public class ADProjectTreeEngine
   implements IProjectTreeEngine, IProjectTreeBuilderFilter
{
   /** Allow Drag Movement with in the tree. */
   public final static int     MRK_ALLOW_MOVE                        = 0;

   /** Allow the element to be dropped onto the diagrams but onto the tree. */
   public final static int     MRK_ALLOW_MOVE_FOUND_RELATIONSHIP_END = 1;

   /** Disallow Drag Movement with in the tree. */
   public final static int     MRK_DISALLOW                          = 1;

   public final static String  ELEMENT_IMPORT = "ImportedElements";
   public final static String  PACKAGE_IMPORT = "ImportedPackages";
   public final static String  ELEMENT_IMPORT_ELEMENT_TYPE = "ElementImport";
   public final static String  PACKAGE_IMPORT_ELEMENT_TYPE = "ElementImport";

   private IProjectTreeModel   m_TreeModel      = null;
   private DispatchHelper      m_DispatchHelper = new DispatchHelper();
   private static IProjectTreeBuilder m_TreeBuilder    = null;

   private static FilteredItemManager m_FilteredItemManager = null;

   private int                 m_MovementRestrictionKind = MRK_DISALLOW;

   // Members used for Editable and Display Preferences.
   private ArrayList < String > m_EditableItems = new ArrayList < String >();
   private ArrayList < String > m_DeletableItems = new ArrayList < String >();
   private ArrayList < String > m_MoveableItems = new ArrayList < String >();
   private HashMap < String, String > m_GUIDMap = new HashMap < String, String >();
   private HashMap < String, String > m_DisplayedItems = new HashMap < String, String >();
   private HashMap < String, String > m_ValidDropTargets = new HashMap < String, String >();
   private HashMap < String, String > m_InvalidValidDropTargets = new HashMap < String, String >();

   private final int PTEAV_UNKNOWN = -1;
   private final int PTEAV_Y       = -2;
   private final int PTEAV_N       = -3;
   private final int PTEAV_X       = -4;

   private HashMap < String, WeakReference<ITreeItem >> m_IDLookup = new HashMap < String, WeakReference<ITreeItem >>();

   //**************************************************
   // Sinks
   //**************************************************
   private ProjectTreeListener m_ProjectTreeListener = new ProjectTreeListener();
   private EngineElementModifiedSink m_ElementModSink = new EngineElementModifiedSink();
   private EngineNamespaceModifiedSink m_NamespaceModSink = new EngineNamespaceModifiedSink();
   private EngineClassifierFeatureSink m_ClassFeatureSink = new EngineClassifierFeatureSink();
   private EngineClassifierTransformSink m_ClassTransSink = new EngineClassifierTransformSink();
   private EngineElementLifeTimeSink m_LifetimeSink = new EngineElementLifeTimeSink();
   private EngineAttributeEventsSink m_AttrSink = new EngineAttributeEventsSink();
   private EngineOperationEventsSink m_OprSink = new EngineOperationEventsSink();
   private EngineDrawingAreaSink m_DrawAreaSink = new EngineDrawingAreaSink();
   private EngineAffectedElementEventsSink m_AffectSink = new EngineAffectedElementEventsSink();
   private EngineRelationEventsSink m_RelSink = new EngineRelationEventsSink();
   private EngineTypedElementEventsSink m_TypeElemSink = new EngineTypedElementEventsSink();
   private EngineFilterSink m_FilterSink = new EngineFilterSink();
   /**
    * Initializes the context menu system and required requesters
    * the required events sinks.
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEngine#initialize(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel)
    */
   public void initialize(IProjectTreeModel model)
   {
   	setTreeModel(model);

        if (m_TreeBuilder == null)
        {
                m_TreeBuilder = new ProjectTreeBuilderImpl(model.getNodeFactory());
                if (this instanceof IProjectTreeBuilderFilter)
                {            
                        IProjectTreeBuilderFilter pFilter = (IProjectTreeBuilderFilter)this;
                        m_TreeBuilder.setProjectTreeBuilderFilter(pFilter);
                }
        }
      initializeSinks();
      initializeByPreferences();
      initializeFillEditableAndDisplayList();
      initializeFilteredManager();
   }

   /**
    * Test if it is OK to delete a tree item.
    * 
    * @param item The item to test.
    * @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if 
    *         it is not OK to delete the tree item.
    */
   public boolean canDelete(IProjectTreeItem item)
   {
      boolean retVal = false;
      
      if(item != null)
      {
         String elementType = item.getModelElementMetaType();
         
         if(elementType.length() > 0)
         {
            retVal = m_DeletableItems.contains(elementType);
         }
      }
      
      return retVal;
   }

   /**
	* Test if it is OK to edit a tree item.
	* 
	* @param item The item to test.
	* @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if 
	*         it is not OK to delete the tree item.
	*/
   public boolean canEdit(IProjectTreeItem item)
   {
	  boolean retVal = false;
      
	  if(item != null)
	  {
		 String elementType = item.getModelElementMetaType();
         
		 if(elementType.length() > 0)
		 {
			retVal = m_EditableItems.contains(elementType);
		 }
	  }
      
	  return retVal;
   }


   /**
    * Cleans up when the core product is about to Quit.
    */

   public void deInitialize()
   {
      deInitializeSinks();
      deInitializeFillEditableAndDisplayList();
      deInitializeFilteredManager();
      m_TreeModel = null;

   }

   /**
    * Initializes the sinks that are required to populate the tree.
    */
   protected void initializeSinks()
   {
      if(getTreeModel() != null)
      {
         m_DispatchHelper.registerProjectTreeEvents(m_ProjectTreeListener);
         m_DispatchHelper.registerForElementModifiedEvents(m_ElementModSink);
         m_DispatchHelper.registerForNamespaceModifiedEvents(m_NamespaceModSink);

         m_DispatchHelper.registerForClassifierFeatureEvents(m_ClassFeatureSink);
         m_DispatchHelper.registerForTransformEvents(m_ClassTransSink);
         m_DispatchHelper.registerForLifeTimeEvents(m_LifetimeSink);
         m_DispatchHelper.registerForAttributeEvents(m_AttrSink);
         m_DispatchHelper.registerForOperationEvents(m_OprSink);
         m_DispatchHelper.registerDrawingAreaEvents(m_DrawAreaSink);
         m_DispatchHelper.registerForAffectedElementEvents(m_AffectSink);
         m_DispatchHelper.registerForRelationEvents(m_RelSink);
         m_DispatchHelper.registerForTypedElementEvents(m_TypeElemSink);
         m_DispatchHelper.registerProjectTreeFilterDialogEvents(m_FilterSink);
      }

   }

   /**
    * Initializes the sinks that are required to populate the tree.
    */
   protected void deInitializeSinks()
   {
      m_DispatchHelper.revokeProjectTreeSink(m_ProjectTreeListener);
      m_DispatchHelper.revokeElementModifiedSink(m_ElementModSink);
      m_DispatchHelper.revokeNamespaceModifiedSink(m_NamespaceModSink);

      m_DispatchHelper.revokeClassifierFeatureSink(m_ClassFeatureSink);
      m_DispatchHelper.revokeTransformSink(m_ClassTransSink);
      m_DispatchHelper.revokeLifeTimeSink(m_LifetimeSink);
      m_DispatchHelper.revokeAttributeSink(m_AttrSink);
      m_DispatchHelper.revokeOperationSink(m_OprSink);
      m_DispatchHelper.revokeDrawingAreaSink(m_DrawAreaSink);
      m_DispatchHelper.revokeAffectedElementEvents(m_AffectSink);
      m_DispatchHelper.revokeRelationSink(m_RelSink);
      m_DispatchHelper.revokeTypedElementSink(m_TypeElemSink);
      m_DispatchHelper.revokeProjectTreeFilterDialogSink(m_FilterSink);

   }

   /**
    * Initalizes engine from the the preferences
    */
   protected void initializeByPreferences()
   {

   }

   /**
    * Initalizes the filter manager with the display information.
    */
   protected void initializeFilteredManager()
   {
   	if (m_FilteredItemManager == null)
   	{
			m_FilteredItemManager = new FilteredItemManager();
			m_FilteredItemManager.initialize(m_DisplayedItems);
   	}
   }

   /**
    * Cleans up the filter manager.
    */
   protected void deInitializeFilteredManager()
   {
   	m_FilteredItemManager = null;
   }

   /**
    * Inializes all of the editable and display list information.  The file
    * <i>ProjectTreeEngine.etc</i> is used to initalize the information.
    */
   protected void initializeFillEditableAndDisplayList()
   {
      IConfigManager manager = ProductHelper.getConfigManager();
      if(manager != null)
      {
         String location = manager.getDefaultConfigLocation();
         location += "ProjectTreeEngine.etc";

         Document doc = XMLManip.getDOMDocument(location);
         if(doc != null)
         {
            processVaildDropTargets(doc);
            processDisplayItems(doc);

         }
      }
   }

   /**
    * Clears all the members used to store the editable and display list
    * information.
    */
   protected void deInitializeFillEditableAndDisplayList()
   {
      m_GUIDMap.clear();
      m_GUIDMap = null;

      m_DisplayedItems.clear();
      m_DisplayedItems = null;

      m_EditableItems.clear();
      m_EditableItems = null;

      m_DeletableItems.clear();
      m_DeletableItems = null;

      m_MoveableItems.clear();
      m_MoveableItems = null;

      m_ValidDropTargets.clear();
      m_ValidDropTargets = null;

      m_InvalidValidDropTargets.clear();
      m_InvalidValidDropTargets = null;
   }

   public void addNewlyCreatedElement( INamedElement elementToAdd)
   {
      String elementType = elementToAdd.getElementType();
      if((elementType.equals("Association") == true) ||
			(elementType.equals("Aggregation") == true) ||
         (elementType.equals("Generalization") == true))
      {
         // Don't add relationships.
      }
      else if(elementType.equals("UseCaseDetail") == true)
      {
         // Use case details can appear in their own folder so we need to have
         // a more complicated update process similar to attributes and
         // operations which are in their own folder if their number is greater
         // then that specified in the project tree builder etc file.
         updateItems(elementToAdd);
      }
      else if(getTreeModel() != null)
      {
          IElement owner = elementToAdd.getOwner();
          if( m_TreeBuilder.isExcluded(elementToAdd) == false)
          {
          
              ETList < ITreeItem > ownerNodes = getOwnerNodes(elementToAdd);
              
              // Before we add the model element to the tree make sure that the model
              // element does not exist in the tree.
              removeElementFromTree(elementToAdd);
              
              if((ownerNodes != null) && (ownerNodes.size() > 0)) {
                  addToOwner(elementToAdd, ownerNodes);
              }
          }
      }
   }

   
   
   /**
    * Retrieve an elements project tree nodes.  If the owner is a IRegion it may
    * not be shown in the tree, so if the owner is an IRegion and it is not
    * found in the tree the grandparent will be returned instead.
    *
    * @param element The element used to retrieve the owner nodes.
    */
   protected ETList < ITreeItem >  getOwnerNodes(IElement element)
   {
      ETList < ITreeItem > retVal = null;

      if((element != null) && (element.getOwner() != null))
      {
         IElement owner = element.getOwner();
         retVal = getTreeModel().findNodes(owner);

         if((retVal == null) || ((retVal != null) && (retVal.size() <= 0)))
         {
            if (owner instanceof IRegion)
            {
               retVal = getTreeModel().findNodes(owner.getOwner());
            }
         }
      }

      return retVal;
   }

   //private UpdateItemsRunnable m_UpdateRunnable = new UpdateItemsRunnable();
   private UpdateItemsRunnable m_UpdateRunnable = null;
   
   /**
    * Verify that the items are up to date.
    *
    * @param pChangedItem The item that changed.  We need to verify this guy is
    *                     up to date.
    */
   protected void updateItems(IElement changedItem)
   {
      
      //if((m_UpdateRunnable.isRunning() == false) && (hasItems()))
      if(m_UpdateRunnable == null)
      {
         m_UpdateRunnable = new UpdateItemsRunnable();
         m_UpdateRunnable.addItem(changedItem);
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               Thread thread = new Thread(m_UpdateRunnable);
               thread.setName("Project Tree Updater"); //NOI18N - This is a program name.
               thread.setPriority(Thread.NORM_PRIORITY - 1);
               thread.run();
               m_UpdateRunnable = null;
               thread = null;
            }
         });
      }
      else
      {
         m_UpdateRunnable.addItem(changedItem);
      }
   }

   public class UpdateItemsRunnable extends BatchProcessRunnable < IElement >
   {
      /**
       * Actually perform the update operation.
       *
       * @param obj The item that changed.  We need to verify this guy is
       *            up to date.
       */
      protected void execute(IElement changedItem)
      {
          if((m_TreeBuilder != null) && (changedItem != null))
          {
              //IElement changedItem = (IElement)obj;
              ETArrayList < String > paths = new ETArrayList < String >();
              ETArrayList < ITreeItem > items = new ETArrayList < ITreeItem >();
              m_TreeBuilder.getInfoForRefresh(changedItem, paths, items);
              
              ETArrayList < IElement > elementsToRefresh = new ETArrayList < IElement >();
              retrieveElementsFromItems(items, elementsToRefresh);
              
              // Associations children are the ends.  We need the actual classes so
              // we have to special case these guys, for other relationships we call
              // GetRelationshipEnds on the helper.
              if (changedItem instanceof IAssociation)
              {
                  IAssociation curAssoc = (IAssociation)changedItem;
                  retrieveElementsFromElements(curAssoc.getAllParticipants(), elementsToRefresh);
              }
              else if (changedItem instanceof IMessage)
              {
                  IMessage curMsg = (IMessage)changedItem;
                  IInteraction interaction = curMsg.getInteraction();
                  if(interaction != null)
                  {
                      if(elementsToRefresh.contains(interaction) == false)
                      {
                          elementsToRefresh.add(interaction);
                      }
                  }
              }
              else
              {
                  // The C++ version first checks if changed Item is actually an
                  // IElement instance.  However, since INamedElement extends IElement
                  // I do not have to worry about that.
                  
                  MetaModelHelper helper = new MetaModelHelper();
                  MetaModelHelper.RelationEnds ends = helper.getRelationshipEnds(changedItem);
                  
                  if((ends.getStartElement() != null) &&
                          (elementsToRefresh.contains(ends.getStartElement()) == false))
                  {
                      elementsToRefresh.add(ends.getStartElement());
                  }
                  
                  if((ends.getEndElement() != null) &&
                          (elementsToRefresh.contains(ends.getEndElement()) == false))
                  {
                      elementsToRefresh.add(ends.getEndElement());
                  }

                  if((ends.getStartElement() == null) && (ends.getEndElement() == null))
                  {
                      // We have changed an item that isn't a relationship.  Add
                      // special handing for the attributes and operations and other
                      // element types just get the owner
                      if ((changedItem instanceof IAttribute) ||
                              (changedItem instanceof IOperation))
                      {
                          IFeature feature = (IFeature)changedItem;
                          IClassifier classifier = feature.getFeaturingClassifier();
                          
                          if((classifier != null) &&
                             (elementsToRefresh.contains(ends.getEndElement()) == false))
                          {
                              elementsToRefresh.add(classifier);
                          }
                      }
                      
                      else if (changedItem instanceof IEnumerationLiteral)
                      {
                          IEnumerationLiteral enumLit = 
                              (IEnumerationLiteral)changedItem;
                          
                          IEnumeration enumeration = enumLit.getEnumeration();
                          
                          if (enumeration != null &&
                              !elementsToRefresh.contains(ends.getEndElement()))
                          {
                              elementsToRefresh.add(enumeration);
                          }
                          
                      }
                      
                      else
                      {
                          if(changedItem.getOwner() != null)
                          {
                              elementsToRefresh.add(changedItem.getOwner());
                          }
                      }
                  }
              }
              
              // Go over the list of elements finding them in the tree and verifying
              // they are correctly displayingtheir children.
              updateTree(elementsToRefresh, paths);
          }
      }
   }

   /**
    * Go over the list of elements finding them in the tree and verifying
    * they are correctly displaying their children.
    *
    * @param elements The model elements to verify.
    * @param paths The paths of the desired items.
    */
   protected void updateTree(ETList < IElement > elements,
                             ETList < String > paths)
   {
      ETList < ITreeItem > affectedNodes = new ETArrayList < ITreeItem >();
      for (Iterator < IElement > iter = elements.iterator(); iter.hasNext();)
      {
         IElement element = iter.next();

         ETList < ITreeItem > nodes = getTreeModel().findNodes(element); 
         affectedNodes.addAll(nodes);
         for (Iterator < ITreeItem > iterator = nodes.iterator(); iterator.hasNext();)
         {
            ITreeItem item = iterator.next();
            if(item.isInitalized() == true)
            {
               // Make sure this guy is not an ITreeRelElement, if it is it's a leaf
               // and we shouldn't add any children.
               if( !(item instanceof ITreeRelElement) )
               {
                  // Modified the logic to fix IZ=110809 and the root cause of 
                  // bugster #6283081.
                  // We have a real tree element that may have children.  Lets
                  // make sure this guy has all the relevant children.  This
                  // is done by first calacuating what the children should be
                  // then add them to the tree. 
                  // - If a child item has already existed in the tree, addItem (parent, child)
                  // does not add a duplicate item.
                  // - if a child is a folder (of type ITreeFolder), search for the 
                  // the same folder from the parent node and recalculate its children.
                   
                  ITreeItem childItem = null; 
                  
                  // build children nodes that shoud be under the 'item' node
                  ITreeItem[] desiredChildren = retrieveChildItems(item); 
                  
                  for(int i=0; i<desiredChildren.length; i++)
                  {
                      // add a child node to the tree (duplicate node is not added to tree)
                      childItem = desiredChildren[i];
                      addItem(item, childItem, "");
                      
                      // if a child is a folder, search for the same folder from 
                      // the parent item, and recalcualte its children.
                      if (childItem instanceof ITreeFolder)
                      { 
                            Enumeration<ITreeItem> kids = item.getNodeChildren();
                            ITreeItem curChild = null;
        
                            while (kids.hasMoreElements())
                            {
                                curChild = kids.nextElement();
                                if (curChild == childItem || curChild.equals(childItem))
                                    curChild.sortChildren();
                            }
                      }
                  } 
                 
                  // The new node is now added directly to the parent node, so no
                  // need to compare the 'desiredChildren; with the actual children
                  // from the parent item; hence commented out this block of code.
                  
//                  if(verifyChildConsistency(paths, item, desiredChildren) == true)
//                  {
//                     getTreeModel().sortChildren(item);
//                  }
            }
         }
      }
   }
   }

   /**
    * This routine verifies that the project tree item has all the children that the builder
    * says it should have.  If not then we create/delete them as necessary.
    *
    * @return <b>true</b> if changes did occur, <b>false</b> if changes did not
    *         occur.
    */
   protected boolean verifyChildConsistency(ETList < String > paths,
                                            ITreeItem         parentItem,
                                            ITreeItem[]       desiredChildren)
   {
      boolean retVal = false;

      ETList < ITreeItem > shouldntBeInTree = new ETArrayList < ITreeItem >();
      ETList < ITreeItem > existingFolders = new ETArrayList < ITreeItem >();

      ETList < ETPairT < ITreeItem, ITreeItem > > missingItems = null;
      missingItems = new ETArrayList < ETPairT < ITreeItem, ITreeItem > >();

      // I want to make a copy of the children.  The compareChildList method
      // will remove elements from the children collection once it finds
      // a match.
      ETList < ITreeItem > children = new ETArrayList < ITreeItem >();
      Enumeration < ITreeItem > childNodes = parentItem.getNodeChildren();
      while(childNodes.hasMoreElements() == true)
      {
         children.add(childNodes.nextElement());
      }

      compareChildList(children,
                       desiredChildren,
                       paths,
                       shouldntBeInTree,
                       missingItems,
                       existingFolders);

      IProjectTreeModel model = getTreeModel();

      // This order is very important.  When the node is the only child node
      // if it is removed then the parent may be collapsed.  Therefore, add
      // any new nodes first then remove any required nodes.
      for (Iterator iter = shouldntBeInTree.iterator(); iter.hasNext();)
      {
         model.removeNodeFromParent((ITreeItem)iter.next());
         retVal = true;
      }

      for (Iterator folderIter = existingFolders.iterator(); folderIter.hasNext();)
      {
         ITreeItem folderItem = (ITreeItem)folderIter.next();
         if(folderItem.isInitalized() == true)
         {
            // We have a real tree element that may have children.  Lets make sure
            // this guy has all the relevant children.

            ITreeItem[] folderDesiredChildren = retrieveChildItems(folderItem);
            if(verifyChildConsistency(paths, folderItem, folderDesiredChildren) == true)
            {
               model.sortChildren(folderItem);
               retVal = true;
            }
         }
      }


      

      Iterator < ETPairT < ITreeItem, ITreeItem > > iterMissing = null;
      for (iterMissing = missingItems.iterator(); iterMissing.hasNext();)
      {
         //ITreeItem curItem = (ITreeItem)iterMissing.next();
         ETPairT < ITreeItem, ITreeItem > curItem = iterMissing.next();

         ITreeItem sibling = curItem.getParamOne();

         // The index is being set to -1 because the index will be incremented
         // later.  Se the comment below for more details.
         int index = -1;
         if(sibling != null)
         {
            index = model.getIndexOfChild(parentItem, sibling);
         }

         // The missing node must be inserted after the sibling.  Therefore,
         // the index must be incremented by one.
         ITreeItem insertItem = curItem.getParamTwo();
         if(insertItem(parentItem, insertItem, index + 1) == true)
         {
//            int childIndex = model.getIndexOfChild(parentItem, insertItem);
//            if((childIndex > 0) || ((childIndex == 0) && (parentItem.isInitalized() == true)))
//            {
//               int[] childIndices = { childIndex };
//               model.notifyOfAddedChildren(parentItem, childIndices);
//               
//               retVal = true;
//            }
         }
      }
      return retVal;
   }

   /**
    * Given a list of ITreeItems (pDesiredTreeItems) this routine checks the
    * list and determines what is in the list that shouldnt be
    * (<code>shouldntBeInList</code>) and whats in the desired list thats
    * missing(<code>missingItems</code>).  <code>existingFolders</code> are
    * folders that need to be checked to see if their children are represented
    * in the tree.
    *
    * For example, when adding a generalization we pass the IGeneralization to
    * the tree builder which returns the ITreeItem parents (ie Class A ,
    * Class B) as well as the folder strings for how that child will appear in
    * the tree (ie Class|Relationships|Generalizations and Class|Relationships|
    * Specializations). The <code>childList</code> will be compared against what
    * the project tree builder gave us (<code>desiredItems</code>) to determine
    * the missing or new items.  If a Relationships, Generalizations, or
    * Specializations folder is found that folder will be returned in the
    * existingFolders list.
    *
    * @param desiredItems These are the parent ITreeItems received from the
    *                     project tree builder.  We need to return information
    *                     to the caller of this routine in 3 forms - what
    *                     children are there that need to get removed, what
    *                     children the project tree builder is telling us should
    *                     appear, but do not. And, this is complicated, what
    *                     folders we may have to check for child consistency as
    *                     well.
    *
    * @param pStrs These strings represent the ways that the created element
    *              could have been added to the tree.  We use these strings to
    *              search for folders that may need to get updated as well and
    *              returned in existingFolders.
    *
    * @param shouldntBeInList [IN/OUT] These are IProjectTreeItems that the
    *                                  project tree builder says should not
    *                                  appear in the tree, but do.
    *
    * @param missingItems [IN/OUT] These are ITreeItems from the project tree
    *                              builder that should appear in the tree, but
    *                              do not.
    *
    * @param existingFolders [IN/OUT] These are a list of project tree items
    *                                 that are folders.  These folders were
    *                                 found in the strings list (pStrs) and may
    *                                 have to be updated as well.
    */
   protected void compareChildList(ETList < ITreeItem > childList,
                                   ITreeItem[]          desiredItems,
                                   ETList < String >    desiredPaths,
                                   ETList < ITreeItem > shouldntBeInList,
                                   ETList < ETPairT < ITreeItem, ITreeItem > > missingItems,
                                   ETList < ITreeItem > existingFolders)
   {
      if((childList != null)         &&
         (desiredItems != null)     &&
         (desiredPaths != null)    &&
         (shouldntBeInList != null) &&
         (missingItems != null)     &&
         (existingFolders != null))
      {
         retrieveDesiredItems( childList,
                               desiredItems,
                               desiredPaths,
                               missingItems,
                               existingFolders);
         // After the operation above what's left in the list are items that
         // should not appear.
         shouldntBeInList.addAll(childList);
      }
   }

   /**
    * Retrieves the information need to update a tree item after it has been
    * modified.  All tree items in the <code>desiredItems</code> collection are
    * removed from <code>childList</code> collection.  If a item in the
    * <code>desiredItems</code> collection is not present in the
    * <code>childList</code> collection, the item will be added to the
    * <code>missingItems</code> collection.  When retrieveDesiredItems returns
    * the <code>childList</code> collection will contain items that are not
    * in the <code>desiredItems</code> collection and the
    * <code>existingFolders</code> collection will contain all folders that
    * are in the <code>childList</code> collection.
    */
   protected void retrieveDesiredItems(ETList < ITreeItem > childList,
                                       ITreeItem[] desiredItems,
                                       ETList < String > desiredPaths,
                                       ETList < ETPairT < ITreeItem, ITreeItem > > missingItems,
                                       ETList < ITreeItem > existingFolders)
   {
      int numPaths = desiredPaths.size();
      for (int index = 0; index < desiredItems.length; index++)
      {
         boolean foundIt = false;

         for (Iterator iter = childList.iterator();
              (iter.hasNext() == true) && ( foundIt == false);)
         {
            ITreeItem curItem = (ITreeItem)iter.next();
            if((curItem != null) &&
               (curItem.equals(desiredItems[index]) == true))
            {
               // If it's a folder then check to see if the text appears in
               // the folder string list that got passed in.  That string
               // list could appear like Class|Attributes|Attribute. If the
               // Attributes folder is there and expanded then we need to
               // update the Attributes folder as well.

               if (curItem instanceof ITreeFolder)
               {
                  if(findFolderName("|" + curItem.getName(), desiredPaths) == true)
                  {
                     existingFolders.add(curItem);
                  }
               }

               //childList.remove(index);
               iter.remove();
               foundIt = true;
            }
         }

         if(foundIt == false)
         {
            ETPairT < ITreeItem, ITreeItem > missingItem = null;

            ITreeItem curItem = desiredItems[index];
            ITreeItem parent  = null;
            if((index > 0) && (index < desiredItems.length))
            {
               parent = desiredItems[index - 1];
            }

            missingItem = new ETPairT < ITreeItem, ITreeItem >(parent, curItem);
            missingItems.add(missingItem);
         }
      }
   }

   /**
    * Test if the specified name is in a list of paths.  The paths are a
    * '|' seperated list of names.
    *
    * @param toFind The name to find.
    * @param paths The collection of strings that specify the paths to search.
    * @return <code>true</code> if the name is in the paths, <code>false</code>
    *         if the name is not found.
    */
    protected boolean findFolderName(String toFind, ETList<String> paths)
    {
        boolean found = false;
        if (paths != null) {
            for (Iterator<String> iter = paths.iterator(); iter.hasNext() && !found;) {
                String value = iter.next();
                if (value.indexOf(toFind) >= 0) {
                    found = true;
                }
            }
        }

        return found;
    }

   /**
    * Pulls out all of the IElements in the collection of ITreeItems.  The
    * elementsToRefresh collection will not contain any duplicates.
    *
    * @param items The collection that specifies the elements.
    * @param elementsToRefresh The IElements found in the items collection.
    */
   protected void retrieveElementsFromItems(ETList < ITreeItem > items,
                                            ETList < IElement >  elementsToRefresh)
   {
      if ((items != null) && (elementsToRefresh != null))
      {
         for (Iterator < ITreeItem > iter = items.iterator(); iter.hasNext();)
         {
            ITreeItem curItem = iter.next();

            if (curItem instanceof ITreeElement)
            {
               ITreeElement curElement = (ITreeElement)curItem;
               IElement modelElement = curElement.getElement();

               if ((modelElement != null)
                  && (elementsToRefresh.contains(modelElement) == false))
               {
                  elementsToRefresh.add(modelElement);
               }
            }
         }
      }

   }

   /**
    * Pulls out all of the IElements in the collection of ITreeItems.  The
    * elementsToRefresh collection will not contain any duplicates.
    *
    * @param items The collection that specifies the elements.
    * @param elementsToRefresh The IElements found in the items collection.
    */
   private void retrieveElementsFromElements(ETList<IElement>           items,
                                          ETList < IElement > elementsToRefresh)
   {
      if((items != null) && (elementsToRefresh != null))
      {
         int count = items.size();
         for (int index = 0; index < count; index++)
         {
            IElement curItem = items.get(index);

            if((curItem != null) &&
               (elementsToRefresh.contains(curItem) == false))
            {
               elementsToRefresh.add(curItem);
            }
         }
      }
   }

   /**
    * Pulls out all of the IElements in the collection of ITreeItems.  The
    * elementsToRefresh collection will not contain any duplicates.
    *
    * @param items The collection that specifies the elements.
    * @param elementsToRefresh The IElements found in the items collection.
    */
//   private void retrieveElementsFromElements(ETList < IElement > items,
//                                             ETList < IElement > elementsToRefresh)
//   {
//      if((items != null) && (elementsToRefresh != null))
//      {
//         for (Iterator < IElement > iter = items.iterator(); iter.hasNext();)
//         {
//            IElement curItem = iter.next();
//
//            if((curItem != null) &&
//               (elementsToRefresh.contains(curItem) == false))
//            {
//               elementsToRefresh.add(curItem);
//            }
//         }
//      }
//   }

   /**
    * Retrieves the model elements that need to be updated.  The model elements
    * that need to be updated are the elements that contain ends of the
    * association.
    *
    * @param curAssoc The association used to retrieve the model elements.
    * @param elementsToRefresh the collection to update.
    */
   protected void retrieveElementsFromAssoc(IAssociation curAssoc,
                                            ETList < IElement > elementsToRefresh)
   {
      ETList<IElement> allParticipants = curAssoc.getAllParticipants();

      long count = 0;
      if(allParticipants != null)
      {
         //allParticipants.removeDuplicates();
         count = allParticipants.size();
      }

      for (int index = 0; index < count; index++)
      {
         IElement curElement = allParticipants.get(index);
         if((curElement != null) &&
            (elementsToRefresh.contains(curElement) == false))
         {
            elementsToRefresh.add(curElement);
         }
      }
   }

   /**
    * Add a model element feature to a collection of model elements.  The
    * feature is added only if it has a classifier and it has not been previously
    * added to the collection.
    *
    * @param element The element to add.
    * @param elementsToRefresh the collection to update.
    */
   protected void addFeature(INamedElement element,
                            ArrayList < IElement > elementsToRefresh)
   {
      IFeature feature = (IFeature)element;
      if(feature.getFeaturingClassifier() != null)
      {
         if(elementsToRefresh.contains(element) == false)
         {
            elementsToRefresh.add(element);
         }
      }
   }

   /**
    * Adds a model element to all tree items that own the model element.
    *
    * @param elementAdded The model element to be added.
    * @param ownerNodes The project tree items that should own the model element
    *                   tree item.
    */
   protected void addToOwner(INamedElement        elementAdded,
                             ETList < ITreeItem > ownerNodes)
   {
      for (Iterator < ITreeItem > iter = ownerNodes.iterator(); iter.hasNext();)
      {
         ITreeItem thisItem = iter.next();

         // If the tree item has been initialize then the node has been
         // expanded.
         if(thisItem.isInitalized() == true)
         {
            if(elementAdded != null)
            {
               // Add this item to the tree.  Since this is a named
               // element, we know that the tree builder will give us the
               // owner as the parent so just create the ITreeElement
               // here.
               m_TreeBuilder.setNodeFactory(getNodeFactory());
               ITreeItem createdItem = m_TreeBuilder.createChild(thisItem,
                                                                 elementAdded);
               if(createdItem != null)
               {
                  addToTree(createdItem, thisItem);
				  m_IDLookup.put(elementAdded.getXMIID(), new WeakReference(createdItem)); 
                  IProjectTreeModel model = getTreeModel();
//                  int[] childIndices = { model.getIndexOfChild(thisItem, createdItem) };
//                  model.notifyOfAddedChildren(thisItem, childIndices);

                  model.sortChildren(thisItem);
               }
            }
            else
            {
               // Probably an IProject
               String name = elementAdded.getNameWithAlias();
               
               ProjectTreeNodeFactory factory = getNodeFactory();
               if(factory != null)
               {
                  ITreeElement treeElement = factory.createElementNode();
                  treeElement.setElement(elementAdded);
                  treeElement.setName(name);
                  addToTree(treeElement, thisItem);
				  m_IDLookup.put(elementAdded.getXMIID(), new WeakReference(treeElement)); 
                  IProjectTreeModel model = getTreeModel();
                  int[] childIndices = { model.getIndexOfChild(thisItem, treeElement) };
                  //model.notifyOfAddedChildren(thisItem, childIndices);
   
                  model.sortChildren(thisItem);
               }
            }

         }
      }
   }

   /**
    * Removes a model element form the tree.  All instances of the model element
    * will be removed from the tree.
    *
    * @param element The model element to be removed.
    */
   protected void removeElementFromTree(INamedElement element)
   {
      if((element != null) && (getTreeModel() != null))
      {
         getTreeModel().removeAll(element);
      }
   }

	//**************************************************
   // Getter and Setters
   //**************************************************

   /**
    * Retreives the project tree mode associated with the
    * engine.
    *
    * @return The model.
    */
   public IProjectTreeModel getTreeModel()
   {
      return m_TreeModel;
   }

   /**
    * Sets the project tree mode associated with the
    * engine.
    *
    * @param model The model
    */
   public void setTreeModel(IProjectTreeModel model)
   {
      m_TreeModel = model;
   }

	//**************************************************
   // Helper Methods
   //**************************************************

   /**
    * Process the XML DOM document for all displayed items.  Displayed items
    * are specified by a DOM node with the name of <code>DisplayedItems</code>.
    * <br>
    * The <code>DisplayItems</code> attributes are:
    * <b>name</b> Then name of the item to be displayed in the tree.
    * <b>moveable</b> Specifies if the item can be moved <i>("y" or "Y")</i>.
    * <b>editable</b> Specifies if the item can be edited <i>("y" or "Y")</i>.
    * <b>deletable</b> Specifies if the item can be deleted <i>("y" or "Y")</i>.
    * <b>filteredByDefault</b> Specifies if the item is filtered
    *                         <i>("y" or "Y")</i>.
    * <b>sortType</b> Sort algorthm that is used to sort the item.
    * <b>validDrop</b> A comma seperated list of tree item names that this the
    *                  displayed item can be drop onto.
    * <b>invalidDropTargets</b> A comma seperated list of tree items names that
    *                           can not be dropped onto a displayed item.
    * <b>dragAndDropDiagrams</b> Specified if diagram can be dropped onto the
    *                            displayed item.  "ALL" means all diagrams can
    *                            be dropped onto the displayed item.  "NONE"
    *                            means that no diagrams can be dropped onto the
    *                            displayed item.
    *
    * @param doc
    */
   protected void processDisplayItems(Document doc)
   {
      List items = doc.selectNodes("//DisplayedItems");
      for (Iterator iter = items.iterator(); iter.hasNext();)
      {
         Node guidNode = (Node)iter.next();
         if (guidNode instanceof Element)
         {
            Element element = (Element)guidNode;

            String name = element.attributeValue("name");
            String displayed = element.attributeValue("displayed");
            String filteredByDefault = element.attributeValue("filteredByDefault");
			String expandedElementType = element.attributeValue("expandedElementType");

            boolean isDisplayed = false;
            if(getAttributeValue(displayed) == PTEAV_Y)
            {
               isDisplayed = true;
               if (expandedElementType != null && expandedElementType.length() > 0)
               {
					// Don't display the primary element in the filter dialog,
					// this can be overridden in the xml file if the user puts
					// the primary element as an expanded element as well (see
					// State for an example).
					//m_DisplayedItems.put(name, "N");
	
					// Go through the expanded element types and place those into 
					// the list, make the primary column (name) so that it
					// doesn't appear in the filter dialog.
					ETList < String > tokens = StringUtilities.splitOnDelimiter(expandedElementType, ",");
					if (tokens != null)
					{
						int cnt = tokens.size();
						for (int x = 0; x < cnt; x++)
						{
							String str = tokens.get(x);
							if (str != null && str.length() > 0)
							{
								m_DisplayedItems.put(str, "Y");
							}
						}
					}
               }
               else
               {
	               if(getAttributeValue(filteredByDefault) == PTEAV_Y)
	               {
	                  m_DisplayedItems.put(name, "Y");
	               }
	               else if(getAttributeValue(filteredByDefault) == PTEAV_N)
	               {
	                  m_DisplayedItems.put(name, "N");
	               }
               }
            }

            // Don't add this other stuff if displayed=="x", that means we don't
            // ever display this type.
            if(isDisplayed == true)
            {
               condiontialAddItem(element, "editable", name, m_EditableItems);
               condiontialAddItem(element, "deletable", name, m_DeletableItems);
               condiontialAddItem(element, "moveable", name, m_MoveableItems);
               addTargets(element, "validDropTargets", name, m_ValidDropTargets);
               addTargets(element, "invalidDropTargets", name, m_InvalidValidDropTargets);
            }
         }
      }
   }

   /**
    * Adds condional information to a specified collection.  The data is
    * retrieved from a XML DOM element.
    *
    * @param element The XML DOM element to retrieve the information.
    * @param attrName The name of the attribute that determines the target value.
    * @param name The name of the element.
    * @params collection The collection to receive the information.
    */
   protected void condiontialAddItem(Element              element,
                                     String               attrName,
                                     String               name,
                                     ArrayList < String > collection)
   {
      if(element != null)
      {
         if(getAttributeValue(element.attributeValue(attrName)) == PTEAV_Y)
         {
            collection.add(name);
         }
      }
   }

   /**
    * Adds target information to a specified collection.  The data is retrieved
    * from a XML DOM element.
    *
    * @param element The XML DOM element to retrieve the information.
    * @param attrName The name of the attribute that determines the target value.
    * @param name The name of the element.
    * @params collection The collection to receive the information.
    */
   protected void addTargets(Element                    element,
                             String                     attrName,
                             String                     name,
                             HashMap < String, String > collection)
   {
      if(element != null)
      {
         String value = element.attributeValue(attrName);
         if((value != null) && (value.length() > 0) && (value.equals(",,") != true))
         {
            collection.put(name, value);
         }
      }
   }

   /**
    * If the test string is <i>Y</i>, <i>y</i>, <i>t</i>, or <i>T</i>
    * then <code>PTEAV_Y</code> is returned.  If the test string is <i>F</i>,
    * <i>f</i>, <i>n</i>, or <i>N</i> then <code>PTEAV_N</code> is returned.  If
    * the test string is <i>X</i>, <i>x</i> then <code>PTEAV_N</code> is
    * returned.
    */
   protected int getAttributeValue(String testStr)
   {
      int retVal = PTEAV_UNKNOWN;

      String uCase = testStr.toUpperCase();

      if((uCase.equals("T") == true) ||
         (uCase.equals("Y") == true))
      {
         retVal = PTEAV_Y;
      }
      else if((uCase.equals("F") == true) ||
              (uCase.equals("N") == true))
      {
         retVal = PTEAV_N;
      }
      else if(uCase.equals("X") == true)
      {
         retVal = PTEAV_X;
      }

      return retVal;
   }

   /**
    * Get the guids that correspond to the names in the validDropTargets
    * An example is...
    * <code><GUIDItem type="IActor" guid="AF9A845E-4889-47CB-BCBD-F63362F8F8F0"/></code>
    *
    * @param doc The DOM document use to retrieve the drop targets.
    */
   protected void processVaildDropTargets(Document doc)
   {
      List guidItems = doc.selectNodes("//GUIDItem");
      for (Iterator iter = guidItems.iterator(); iter.hasNext();)
      {
         Node guidNode = (Node)iter.next();
         if (guidNode instanceof Element)
         {
            Element element = (Element)guidNode;
            m_GUIDMap.put(element.attributeValue("type"),
                          element.attributeValue("interfacename"));
         }
      }
   }

   /**
    * Retrieves the active workspace.
    *
    * @return The workspace.
    */
	protected IWorkspace getWorkspace()
	{
		return ProductHelper.getWorkspace();
	}

	/**
	 * Retrieves the filter manager.  The filter manager is used to filter out
    * items from the tree.
    *
    * @return The filter manager.
	 */
	public FilteredItemManager getFilterManager()
	{
		return m_FilteredItemManager;

	}

	/**
	 * Given a parent pTreeItem and pModelElement this routine searches through
	 * the pModelElement's owned elements and adding them to the tree as a
	 * child of pTreeItem.  If sAddOnlyThisType has non-zero length then only
	 * elements that have this elementType are added
	 *
	 * @param treeItem The parent item to recieve the owned elements.
	 * @param modelElement The model element for which treeItem represents
	 */
	public void addOwnedElements(ITreeItem owner, IElement modelElement)
	{
		addOwnedElements(owner, modelElement, "");

	}

	/**
	 * Given a parent pTreeItem and pModelElement this routine searches through
	 * the pModelElement's owned elements and adding them to the tree as a
	 * child of pTreeItem.  If sAddOnlyThisType has non-zero length then only
	 * elements that have this elementType are added
	 *
	 * @param owner The parent item to recieve the owned elements.
	 * @param modelElement The model element for which treeItem represents
	 * @param sAddOnlyThisType If this is set then only children with this
	 *                         element type are added.
	 */
	protected void addOwnedElements(ITreeItem owner,
                                   IElement  modelElement,
                                   String    addOnlyThisType)
	{
      if(owner != null)
      {
         ITreeItem[] treeItems = null;
         if(owner instanceof ITreeFolder)
         {
            treeItems = retrieveChildItems(owner);
         }
         else if(modelElement instanceof IProject)
         {
            // TODO: Figure out how I am going to be notified of DOCUMENT NOT FOUND errors.
//            treeItems = retrieveChildItemsForElement(null,
//                                                     modelElement);

            treeItems = retrieveChildItems(modelElement);

            // NOTE: If the document is not found I have to call handleLostElement
         }
         else if(owner != null)
         {
            treeItems = retrieveChildItems(owner);
         }

         if (treeItems != null)
         {
            for (int index = 0; index < treeItems.length; index++)
            {
               addItem(owner, treeItems[index], addOnlyThisType);
            }
         }
      }
	}

	/**
    * Add a new tree item to the project tree model.
    *
    * @param parentItem The tree item that will receive the new tree item.
    * @param item The new tree item.
    * @param addOnlyOneType The type to add.  If addOnlyThisType is an empty
    *                        empty string then all types are added.
    */
   protected boolean addItem(ITreeItem parentItem,
                             ITreeItem item,
                             String    addOnlyThisType)
   {
      boolean retVal = false;

      if(parentItem != null)
      {
         if (item instanceof ITreeElement)
         {
            ITreeElement curElement = (ITreeElement)item;
            retVal = addElement(parentItem, addOnlyThisType, curElement);
         }
         else if (item instanceof ITreeDiagram)
         {
            ITreeDiagram curDiagram = (ITreeDiagram)item;
            retVal = addDiagram(parentItem, curDiagram);
         }
         else if (item instanceof ITreeRelElement)
         {
            // TreeRelElements are the nodes shown as end of relationships.  We
            // don't want to recurse forever in the tree so we treat these as
            // dead end nodes.  By making the check that pTreeRelElement is 0
            //and continuing we skip these items for expansion purposes.
         }
         else if (item instanceof ITreeFolder)
         {
            ITreeFolder curFolder = (ITreeFolder)item;
            retVal = addFolder(parentItem, curFolder);
         }
      }

      return retVal;
   }

   /**
    * Inserts the item into the parent at the specified location in the
    * parents child list.
    *
    * @param parentItem The tree item that will receive the new tree item.
    * @param item The new tree item.
    * @param index The child list position.
    */
   protected boolean insertItem(ITreeItem parentItem,
                                ITreeItem item,
                                int       index)
   {
      boolean retVal = false;

      if(parentItem != null)
      {
         if (item instanceof ITreeElement)
         {
            ITreeElement curElement = (ITreeElement)item;
            retVal = insertElement(parentItem, curElement, "", index);
         }
         else if (item instanceof ITreeDiagram)
         {
            ITreeDiagram curDiagram = (ITreeDiagram)item;
            retVal = insertDiagram(parentItem, curDiagram, index);
         }
         else if (item instanceof ITreeRelElement)
         {
            ITreeRelElement curRelElement = (ITreeRelElement)item;

         }
         else if (item instanceof ITreeFolder)
         {
            ITreeFolder curFolder = (ITreeFolder)item;
            retVal = insertFolder(parentItem, curFolder, index);
         }
      }

      return retVal;
   }

   /**
    * Adds the diagram to the project tree model.
    *
    * @param parent The parent of the diagram tree item.
    * @param diagramItem The diagram tree item to be added to the parent.
    */
   protected boolean addDiagram(ITreeItem parent, ITreeDiagram diagramItem)
   {
      boolean retVal = false;
      String diagramType = "Diagram"; // NOI18N

      // Get the type of the closed diagram
      IProxyDiagramManager manager = ProxyDiagramManager.instance();
      IProxyDiagram proxy = diagramItem.getDiagram();

      if (proxy != null)
      {
         String location = proxy.getFilename();
         diagramType = proxy.getDiagramKindName();
         boolean isValid = manager.isValidDiagram(location);
         boolean isOpen  = proxy.isOpen();
         
         if (!isValid)
         	isValid = doubleCheckIsValidDiagram(location);

         String diagramTypeStr = getDiagramDisplayDetails(
             location, diagramType, isValid, isOpen);
         
         String diagramHidingStr = getDiagramDisplayHidingDetails(
             location, diagramType, isValid, isOpen);

         if (diagramHidingStr.length() <= 0)
            diagramHidingStr = "Diagram"; // NOI18N

         // cvc - 6302566 & 6302566
         // regression caused by CR fix below, so disabled it
         // cvc - 6299088
         // only allow the diagram node to be added once
         //  if (!m_IDLookup.containsKey(diagramItem.getDiagram().getFilename()))
         //  {
             if (!isHidden2(diagramHidingStr))
             {
                m_IDLookup.put(
                    diagramItem.getDiagram().getFilename(), new WeakReference(diagramItem));

                diagramItem.setDiagramType(diagramTypeStr);
                m_TreeModel.addItem(parent, diagramItem);
                retVal = true;
             }
         // }
      }

      return retVal;
   }

   // This could be a diagram that has not yet been saved.  It won't have a .etld
   // or .etlp file.  We need to look in the open diagrams
	private boolean doubleCheckIsValidDiagram(String location)
	{
		boolean retVal = false;
		IProduct product = ProductHelper.getProduct();
		if(product != null)
		{
		   IDiagram diagram = product.getDiagram(location);
		   if(diagram != null && diagram instanceof IUIDiagram) 
		   {
		   		IUIDiagram pDia = (IUIDiagram)diagram;
		   		IDrawingAreaControl control = pDia.getDrawingArea();
		   		if (control != null)
		   		{
		   			String drawKind = control.getDiagramKind2();
		   			if (drawKind != null && drawKind.length() > 0)
		   			{
		   				retVal = true;
		   			}
		   		}
		   }
		}
		return retVal;
	}

   /**
    * Adds the diagram to the project tree model at the specified location.
    *
    * @param parent The parent of the diagram tree item.
    * @param diagramItem The diagram tree item to be added to the parent.
    */
   protected boolean insertDiagram(ITreeItem parent,
                                   ITreeDiagram diagramItem,
                                   int index)
   {
      boolean retVal = false;

      String diagramType = "Diagram";

      // Get the type of the closed diagram
      IProxyDiagramManager manager = ProxyDiagramManager.instance();
      IProxyDiagram        proxy   = diagramItem.getDiagram();
      if(proxy != null)
      {
         String location = proxy.getFilename();
         diagramType = proxy.getDiagramKindName();
         boolean isValid = proxy.isValidDiagram();
         boolean isOpen  = proxy.isOpen();

		 if (!isValid)
		 {
		   isValid = doubleCheckIsValidDiagram(location);
		 }

         String diagramTypeStr = getDiagramDisplayDetails(location,
                                                          diagramType,
                                                          isValid,
                                                          isOpen);
         String diagramHidingStr = getDiagramDisplayHidingDetails(location,
                                                                  diagramType,
                                                                  isValid,
                                                                  isOpen);

         if(diagramHidingStr.length() <= 0)
         {
            diagramHidingStr = "Diagram";
         }

         // HAVE TODO: Check if duplicate node.

         if(isHidden2(diagramHidingStr) == false)
         {
            m_IDLookup.put(diagramItem.getDiagram().getFilename(), new WeakReference(diagramItem));

            diagramItem.setDiagramType(diagramTypeStr);
            //m_TreeModel.insertItem(parent, diagramItem, index);
            m_TreeModel.addItem(parent, diagramItem);

            retVal = true;
         }
      }

      return retVal;
   }

   /**
    * This routine returns the string used for hiding various diagram types.
    * If the diagram is invalid the assumption is made that it's an unsaved
    * diagram that's still open in the workspace - so we ask the product for
    * the diagram by name.  If found we query the DrawingAreaControl directly
    * for it's type.
    *
    * @param location
    * @param diagramType
    * @param isValid
    * @param isOpen
    * @return The name of the diagram.  If the diagram is not valid then
    *         _BROKEN will be appended to the diagram name.  If the diagram is
    *         closed then _CLOSED will be appended to the diagram name.
    */
   protected String getDiagramDisplayHidingDetails(String location,
                                                   String diagramType,
                                                   boolean isValid,
                                                   boolean isOpen)
   {
      String retVal = "Diagram";
      return getDiagramIcon(diagramType, isValid, isOpen, true);
   }

   /**
    * Returns the diagram icon that should be used for this diagram type,
    * open state and validity.
    *
    * @param diagramType The diagram type.
    * @param isValid Is the diagram valid.
    * @param isOpen Is the diagram open.
    * @param isHidden Are we getting strictly the hidden name.
    * @return Then daigarm icon name.
    */
   protected String getDiagramIcon(String  diagramType,
                                   boolean isValid,
                                   boolean isOpen,
                                   boolean isHidden)
   {
      String retVal = "Diagram";
      IDiagramTypesManager manager = DiagramTypesManager.instance();

      String displayName = manager.getDiagramTypeNameNoSpaces(diagramType);
      if(displayName != null && displayName.length() > 0)
      {
         if(isHidden == true)
         {
            retVal = displayName;
         }
         else
         {
            if((isValid == true) && (isOpen == true))
            {
               retVal = displayName;
            }
            else if((isValid == true) && (isOpen == false))
            {
               retVal = displayName; // + "_CLOSED";
            }
            else
            {
               retVal = displayName; // + "_BROKEN";
            }
         }
      }

      return retVal;
   }

   /**
    * This routine returns the diagram type string.  If the diagram is invalid
    * the assumption is made that it's an unsaved diagram that's still open in
    * the workspace so we ask the product for the diagram by name.  If found
    * we query the AxDrawingAreaControl directly for it's type.
    *
    * @param location The location of the diagram file.
    * @param diagramType The type of the diagram.
    * @param isValid <code>true</code> if the diagram is valid.
    * @param isOpen <code>true</code> if the diagram is open.
    * @return
    */
   protected String getDiagramDisplayDetails(String location,
                                             String diagramType,
                                             boolean isValid,
                                             boolean isOpen)
   {

      return getDiagramIcon(diagramType, isValid, isOpen, false);
   }

   /**
    * Adds a folder to the project tree model.
    *
    * @param parent The new parent of the folder.
    * @param folder The new folder to add.
    */
   protected boolean addFolder(ITreeItem parent, ITreeFolder folder)
   {
      return insertFolder(parent, folder, parent.getChildCount());
   }

   protected boolean insertFolder(ITreeItem parent,
                                  ITreeFolder folder,
                                  int       index)
   {
      boolean retVal = false;

      // Make sure this folder has children before adding
      ITreeItem[] children = retrieveChildItems(folder);
      if((children != null) &&  (children.length > 0))
      {
         boolean isHidden = false;

         String name = folder.getName();
         if((name.equals("ImportedPackages") == true) ||
            (name.equals("ImportedElements") == true))
         {
            // These guys can be hidden
            isHidden = isHidden2(name);
         }

         if(isHidden == false)
         {
            m_IDLookup.put(folder.getID(), new WeakReference(folder));
            //m_TreeModel.insertItem(parent, folder, index);
            m_TreeModel.addItem(parent, folder);
            retVal = true;
         }
      }

      return retVal;
   }

   /**
    * Adds a element tree item to the project tree model.
    *
    * @param parentItem The parent of the new tree element.
    * @param addOnlyThisType The type to add.  If addOnlyThisType is an empty
    *                        empty string then all types are added.
    * @param element The tree element to add to the parent.
    */
   protected boolean addElement( ITreeItem    parentItem,
                                 String       addOnlyThisType,
                                 ITreeElement element)
   {
      String type = element.getExpandedElementType();
      boolean retVal = false;
      if ( (isHidden2(type) == false) || (isDesignCenter()) )
      {
         m_IDLookup.put(element.getXMIID(), new WeakReference(element));
         if(addOnlyThisType.equals(type) == true)
         {
            addToTree(element, parentItem);
            retVal = true;
         }
         else if(addOnlyThisType.length() <= 0)
         {
            addToTree(element, parentItem);
            retVal = true;
         }
      }

      return retVal;
   }

   /**
    * Adds a element tree item to the project tree model.
    *
    * @param parentItem The parent of the new tree element.
    * @param addOnlyThisType The type to add.  If addOnlyThisType is an empty
    *                        empty string then all types are added.
    * @param element The tree element to add to the parent.
    */
   protected boolean insertElement( ITreeItem    parentItem,
                                    ITreeElement element,
                                    String       addOnlyThisType,
                                    int          index)
   {
      String type = element.getExpandedElementType();
      boolean retVal = false;

      if(isHidden2(type) == false)
      {
         m_IDLookup.put(element.getXMIID(), new WeakReference(element));
         if(addOnlyThisType.equals(type) == true)
         {
            addToTree(element, parentItem, index);
            retVal = true;
         }
         else if(addOnlyThisType.length() <= 0)
         {
            addToTree(element, parentItem, index);
            retVal = true;
         }
      }

      return retVal;
   }

   /**
    * Adds this particular element to the tree.
    *
    * @param pSupportTreeItem The ITreeElement we got from the project tree builder
    * @param pParentTreeItem The parent for the one we're about to add
    */
   protected void addToTree(ITreeItem pSupportTreeItem,
                            ITreeItem pParentTreeItem)
   {
      addToTree(pSupportTreeItem, pParentTreeItem, pParentTreeItem.getChildCount());

   }

   protected void addToTree(ITreeItem pSupportTreeItem,
           ITreeItem pParentTreeItem,
           int       index)
   {
       if((pSupportTreeItem != null) && (pParentTreeItem != null))
       {
           IProjectTreeItem data = pSupportTreeItem.getData();
           if(data != null)
           {
               IElement elementToAdd = data.getModelElement();
               if(elementToAdd != null)
               {
                   String typeName = elementToAdd.getElementType();
                   String exTypeName = elementToAdd.getExpandedElementType();
                   if ( (isHidden2(exTypeName) == false) || (isDesignCenter()) )
                   {
                       String name = pSupportTreeItem.getName();
//                       String name = pSupportTreeItem.getData().getItemText();
                       
                       if(typeName.equals("Operation") == true)
                       {
                           IOperation oper = (IOperation)elementToAdd;
                           name = getFormattedString(oper);
                       }
                       else if(typeName.equals("Attribute") == true)
                       {
                           IAttribute attr = (IAttribute)elementToAdd;
                           name = getFormattedString(attr);
                       }
                       else if (pSupportTreeItem instanceof ITreeFolder)
                       {
                           ITreeFolder folder = (ITreeFolder)pSupportTreeItem;
                           name = folder.getDisplayName();
                       }
                       
                       if(name == null || name.length() <= 0)
                       {
                           name = PreferenceAccessor.instance().getDefaultElementName();
                       }
                       
                       pSupportTreeItem.setDisplayedName(name, false);
                       
                       //m_TreeModel.insertItem(pParentTreeItem, pSupportTreeItem, index);
                       m_TreeModel.addItem(pParentTreeItem, pSupportTreeItem);
                   }
               }
           }
       }
   }

   /**
    * Interface into the tree builder
    */
   protected ITreeItem[] retrieveChildItems(Object object)
   {
      //ITreeItem[] retVal = m_TreeBuilder.retrieveChildItems(object);
      
      m_TreeBuilder.setNodeFactory(getNodeFactory());
      ITreeItem[] retVal = m_TreeBuilder.retrieveChildItemsSorted(object);

      if((retVal != null) && (retVal.length > 0))
      {
         ArrayList < ITreeItem > list = new ArrayList < ITreeItem >();
         for (int index = 0; index < retVal.length; index++)
         {
            list.add(retVal[index]);
         }
         if (!isDesignCenter())
         {
				handleHiddenPackage(list);
         }

         retVal = null;

         retVal = new ITreeItem[list.size()];
         list.toArray(retVal);
      }

      return retVal;

   }

   /**
    * Interface into the tree builder
    * @return The tree items to add.
    */
   protected ITreeItem[] retrieveChildItemsForElement(Object object,
                                                      IElement modelElement)
   {      
      m_TreeBuilder.setNodeFactory(getNodeFactory());
      ITreeItem[] retVal =  m_TreeBuilder.retrieveChildItemsForElement(object, modelElement);

      if((retVal != null) && (retVal.length > 0))
      {
         ArrayList < ITreeItem > list = new ArrayList < ITreeItem >();
         for (int index = 0; index < retVal.length; index++)
         {
            list.add(retVal[index]);
         }
			if (!isDesignCenter())
			{
				handleHiddenPackage(list);
			}

         retVal = null;

         retVal = new ITreeItem[list.size()];
         list.toArray(retVal);
      }

      return retVal;
   }

   /**
    * If packages are hidden this routine finds the package members
    * and adds them to our main list.
    *
    * @param items The items to handle.
    */
   protected void handleHiddenPackage(ArrayList < ITreeItem > items)
   {
      ArrayList < ITreeItem > foundItems = new ArrayList < ITreeItem >();

      if((items != null) && (isHidden2("Package") == true))
      {
         // Go into packages and return there child items
         for (Iterator iter = items.iterator(); iter.hasNext();)
         {
            ITreeItem curItem = null;
            try
            {
               curItem = (ITreeItem)iter.next();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
            IElement curElement = null;

            if (curItem instanceof ITreeElement)
            {
               ITreeElement curTreeElement = (ITreeElement)curItem;
               curElement = curTreeElement.getElement();
            }
            else if (curItem instanceof ITreeFolder)
            {
               ITreeFolder curTreeFolder = (ITreeFolder)curItem;
               curElement = curTreeFolder.getElement();
            }

            if (curElement instanceof IPackage)
            {
               IPackage curPackage = (IPackage)curElement;
               ITreeItem[] packageItems = retrieveChildItems(curItem);
               if(packageItems != null)
               {
                  for (int pIndex = 0; pIndex < packageItems.length; pIndex++)
                  {
                     //items.add(packageItems[pIndex]);
                     foundItems.add(packageItems[pIndex]);
                  }
               }
            }
         }
      }

      items.addAll(foundItems);
   }

   /**
    * Returns true if this type has been hidden.
    *
    * @param elementTypeName The element to determine whether or not its filtered.
    * @return <b>true</b> if the element type is hidden, <b>false</b> otherwise.
    */
   public boolean isHidden(String elementTypeName)
   {
   	boolean bHidden = false;
   	if (elementTypeName != null && elementTypeName.length() > 0)
   	{
   		if ((elementTypeName.equals(ELEMENT_IMPORT)) || (elementTypeName.equals(PACKAGE_IMPORT)))
   		{
				bHidden = !m_FilteredItemManager.isDisplayed(elementTypeName);
   		}
   	}
   	return bHidden;
   }

	private boolean isHidden2(String elementTypeName)
	{
		return !m_FilteredItemManager.isDisplayed(elementTypeName);
	}

   /**
    * Adds all the diagrams in the project.
    *
	 * @param treeItem The project tree item.
	 * @param project The project that is being updated.
	 */
   protected void addAllDiagramsInProject(ITreeItem parent, IProject project)
	{
		IProxyDiagramManager manager = ProxyDiagramManager.instance();
        ETList<IProxyDiagram> diagrams = manager.getDiagramsInProject(project);

      if(diagrams != null)
      {
         for (int index = 0; index < diagrams.size(); index++)
         {
            IProxyDiagram curDiagram = diagrams.get(index);
            String location = curDiagram.getFilename();

            ProjectTreeNodeFactory factory = getNodeFactory();
            if((location.length() > 0) && (factory != null))
            {
               //addDiagram(parent, new ProjectTreeDiagramNode(curDiagram));
               ITreeDiagram newItem = factory.createDiagramNode(curDiagram);
               newItem.setDisplayedName(diagrams.get(index).getNameWithAlias(), false);
               newItem.setName(diagrams.get(index).getNameWithAlias());
               newItem.setSortPriority(m_TreeBuilder.getSortPriority(curDiagram.getDiagramKindName()));
               addDiagram(parent, newItem);
            }
         }
      }
	}

   /**
    * Check if the engine is being used to support the project tree.  This should
    * be overridden if ADProjectTreeEngine is extended to support anything other
    * than the project tree.
    *
    * @param pParentControl The control that is being used.
    * @return <code>true</code> the we are processing the project tree.
    */
	protected boolean isProjectTree(IProjectTreeControl pParentControl)
	{
		boolean retVal = false;
		IProjectTreeModel thisModel = getTreeModel();
		//if (thisModel instanceof ProjectTreeSwingModel)
                if ("ProjectTree".equals(thisModel.getModelName()) == true)
		{
//			if (pParentControl instanceof JProjectTree)
//			{
//				ISwingProjectTreeModel model = ((JProjectTree)pParentControl).getProjectModel();
//				retVal = model.getProjectTreeName().equals(ProjectTreeResources.getString("ProjectTreeSwingModel.ProjectTree_Name"));
//				
//			}
                   // For now the only time the parent control will be null is when we are in the project
                   // tree.  In the future we will need to get rid of the whole parent control stuff.
                   if(pParentControl == null)
                   {
                      retVal = true;
                   }
		}
		return retVal;
	}

	protected boolean isDesignCenterTree(IProjectTreeControl pParentControl)
	{
		boolean retVal = false;
		IProjectTreeModel thisModel = getTreeModel();
      
      String modelName = thisModel.getModelName();
		//if (thisModel instanceof DesignCenterSwingModel)
      if(modelName.equals("DesignCenter") == true)
		{
			if (pParentControl instanceof JProjectTree)
			{
				ISwingProjectTreeModel model = ((JProjectTree)pParentControl).getProjectModel();
				retVal = model.getProjectTreeName().equals(ProjectTreeResources.getString("DesignCenterSwingModel.Design_Center_Description"));
			}
		}
	  return retVal;
	}
   /**
    * Called when the tree control begins a drag.  You can cancel the drag by calling put_Cancel with
    * <b>true</b> on the pVerify object.
    *
    * @param pParentControl The tree that caused the event
    * @param pItems The items being moved
    * @param pVerify Used to cancel the event
    */
   protected void handleBeginDrag(IProjectTreeControl    pParentControl,
                                  IProjectTreeItem[]     pItems,
                                  IProjectTreeDragVerify pVerify)
   {
      // First make sure that someone else has not canceled the event.
      if(pVerify.isCancel() == false)
      {
         for (int index = 0; index < pItems.length; index++)
         {
            m_MovementRestrictionKind = canMove(pItems[index]);
            if( m_MovementRestrictionKind == MRK_DISALLOW)
            {
               pVerify.setCancel(true);
            }
         }

         if(pVerify.isCancel() == false)
         {
            pVerify.setDropEffect(DnDConstants.ACTION_MOVE);
         }
      }
   }

   /**
    * Called when items have been dropped onto the tree.  OnDrop.
    *
    * @param pParentControl The tree that caused the event
    * @param transfer Data provided by the source about the drag and drop operation
    * @param pVerify Used to cancel the event
    */
   protected void handleEndDrag(IProjectTreeControl    pParentControl,
                                Transferable           transfer,
                                int                    action,
                                IProjectTreeDragVerify pVerify)
   {
      if (m_MovementRestrictionKind == MRK_ALLOW_MOVE)
      {

         try
         {
            if(transfer.isDataFlavorSupported(ADTransferable.ADDataFlavor) == true)
            {
               handleEndDrag(transfer,
                             action,
                             pParentControl,
                             pVerify);
            }
            else if(transfer.isDataFlavorSupported(DataFlavor.stringFlavor) == true)
            {
               handleStringEndDrag(transfer,
                                   action,
                                   pParentControl,
                                   pVerify);
            }
         }
         catch (UnsupportedFlavorException e)
         {
            // We should never be here because I am first checking if the
            // data flavor is supported.
         }
         catch (IOException e)
         {
            //e.printStackTrace();
         }
      }
   }

   /**
    * Handles the end drag event.  The data on the transfer data was a string.
    * It is assumed the data is an XML document.
    *
    * @param transfer The drops transerfable object.
    * @param action The drag action to perform.  The value is one of the
    *               DnDConstants.
    * @param pParentControl The control that was dropped onto.
    * @param pVerify The drag verify object.  To cancel the drop set the
    *                isCancel property to true.
    */
   protected void handleStringEndDrag(Transferable           transfer,
                                      int                    action,
                                      IProjectTreeControl    pParentControl,
                                      IProjectTreeDragVerify pVerify)
      throws UnsupportedFlavorException, IOException
   {
      String data = (String)transfer.getTransferData(DataFlavor.stringFlavor);

   }

   /**
    * Handles the end drag event.  The transfer type ADTransferable.ADTransferData
    * is Embarcadero's proprietary data flavor.
    *
    * @param transfer The drops transerfable object.
    * @param action The drag action to perform.  The value is one of the
    *               DnDConstants.
    * @param pParentControl The control that was dropped onto.
    * @param pVerify The drag verify object.  To cancel the drop set the
    *                isCancel property to true.
    * @see org.netbeans.modules.uml.ui.support.ADTransferable.ADTransferData
    */
   protected void handleEndDrag(Transferable           transfer,
                                int                    action,
                                IProjectTreeControl    pParentControl,
                                IProjectTreeDragVerify pVerify)
      throws UnsupportedFlavorException, IOException
   {
      ADTransferable.ADTransferData data = (ADTransferable.ADTransferData)transfer.getTransferData(ADTransferable.ADDataFlavor);
      IProjectTreeItem dropTarget = pVerify.getTargetNode();

      //ArrayList < ITreeItem > effectedObjects = new ArrayList < ITreeItem >();

      if(dropTarget != null)
      {
         ITreeItem[] dropPath = dropTarget.getPath();
         //effectedObjects.add(dropPath[dropPath.length - 1]);

         // TODO: Need to determine if we are in copy of move mode.
         boolean isCopy = (action == DnDConstants.ACTION_COPY);

         if(dropTarget.getModelElement() != null)
         {
            if((isCopy == true) && (data.isAllElementsFeatures() == false))
            {
               isCopy = false;
            }

            IElement dropTargetElement = dropTarget.getModelElement();

            ImportResult performImport    = new ImportResult();
            boolean      testInnerClasses = true;
            boolean      testImports      = true;

            ArrayList < IElement > modelElements = data.getModelElements();

            if ((modelElements != null) && (modelElements.size() > 0))
            {
               for (Iterator < IElement > iter = modelElements.iterator();
                    (iter.hasNext() == true)&&
                    (performImport.isImportCanceled() == false);
                  )
               {
                  IElement element = iter.next();
				  
                  if ((element != null) && (element != dropTargetElement))
                  {
                     if (isValidDropTarget(element, dropTargetElement) == true)
                     {
                        if ((element instanceof IAttribute) ||
                            (element instanceof IOperation))
                        {
                           if (dropTargetElement instanceof IClassifier)
                           {
                              IClassifier classifier = (IClassifier)dropTargetElement;
                              handleDragFeature(isCopy, element, classifier);
                           }
                        }
                        else
                        {
                           handleDragTopElement(isCopy,
                                                element,
                                                dropTargetElement,
                                                testInnerClasses,
                                                testImports,
                                                performImport);
                        }
						
						

                        if(isCopy == false)
                        {
//                           ITreeItem item = m_IDLookup.get(element.getXMIID());

//                           ITreeItem[] path = item.getData().getPath();

                           // The parent was effected.  So notify the parent
                           // of the moved node must be notified not the node
                           // itself.
                           //effectedObjects.add(path[path.length - 2]);
                        }
                     }
                  }
               }
            }

            // Reparent the diagram to this model element.  Make sure to update
            // the workspace data so that it has the new namespace id.
            ArrayList < String > locations = data.getDiagramLocations();
            if(locations != null)
            {
               for (Iterator <String > iter = locations.iterator(); iter.hasNext();)
               {
                  String curLoc = iter.next();
                  setDiagramNamespace(curLoc, dropTargetElement);
               }
            }
         }
         
         if(pParentControl != null)
         {
            pParentControl.refresh(false);
         }
      }
   }

   protected boolean setDiagramNamespace(String location, IElement parentNS)
   {
      boolean retVal = false;

      IProxyDiagramManager manager = ProxyDiagramManager.instance();
      IProxyDiagram proxyDiagram = manager.getDiagram(location);

      assert proxyDiagram != null : "Unable to find the diagram";

      if(proxyDiagram != null)
      {
         INamespace namespace = null;
         if (parentNS instanceof INamespace)
         {
            namespace = (INamespace)parentNS;
         }

         int        diagramKind  = proxyDiagram.getDiagramKind();
         INamespace oldNamespace = proxyDiagram.getNamespace();

         // If a sequence diagram is dropped onto the tree then the move is
         // ignored, but it's allowed to be dropped onto another sequence
         // diagram GET object - That will result in the creation of an
         // InteractionOccurrence
         if((namespace != null) && (diagramKind != org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram.DK_SEQUENCE_DIAGRAM))
         {
            String topLevelXMIID    = namespace.getTopLevelId();

            String oldTopLevelXMIID = "";
            if(oldNamespace != null)
            {
               oldTopLevelXMIID = oldNamespace.getTopLevelId();
            }

            // Make sure the namespaces share the same project
            if((oldTopLevelXMIID.length() == 0) ||
               (oldTopLevelXMIID.equals(topLevelXMIID) == true))
            {
               proxyDiagram.setNamespace(namespace);
            }
            else
            {
            }
         }
      }

      return retVal;
   }

   protected void handleDragFeature(boolean     isCopy,
                                    IElement    element,
                                    IClassifier dropTargetClassifier)
   {
      // Attributes and operations extends IFeature.
      IFeature feature = null;
      if (element != null)
      {
         feature = (IFeature)element;
      }

      if(feature != null)
      {
         if (isCopy == true)
         {
            feature.duplicateToClassifier(dropTargetClassifier);
         }
         else
         {
            feature.moveToClassifier(dropTargetClassifier);
         }
      }
   }

   protected void handleDragTopElement(boolean      isCopy,
                                       IElement     element,
                                       IElement     dropTargetElement,
                                       boolean      testInnerClasses,
                                       boolean      testImports,
                                       ImportResult performImport)
   {
      boolean doIt = true;
      boolean doInnerClasses = false;

      IClassifier thisClassifier = null;
      if(element instanceof IClassifier)
      {
         thisClassifier = (IClassifier)element;
      }

      IClassifier dropTargetClassifier = null;
      if (dropTargetElement instanceof IClassifier)
      {
         dropTargetClassifier = (IClassifier)dropTargetElement;
      }

      if((thisClassifier != null)&& (dropTargetClassifier != null))
      {
         if(testInnerClasses == true)
         {
            // The user might not really have meant to create inner
            // classes. But, make sure we ask only once.

            String pattern = DefaultEngineResource.getString("IDS_NESTED_CLASS_WARNING");
            String title = DefaultEngineResource.getString("IDS_NESTED_CLASS_WARNING_TITLE");
            String checkbox = DefaultEngineResource.getString("IDS_NESTED_CLASS_WARNING_CHECKBOX");

            Object[] params = {thisClassifier.getName(),
                              dropTargetClassifier.getName()};

            String msg = MessageFormat.format(pattern, params);
            IQuestionDialog dialog = UIFactory.createQuestionDialog();

            QuestionResponse result;
            result = dialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO,
                                                                    MessageIconKindEnum.EDIK_ICONQUESTION,
                                                                    msg,
                                                                    checkbox,
                                                                    title,
                                                                    MessageResultKindEnum.SQDRK_RESULT_YES,
                                                                    true);

            doIt = result.getResult() == MessageResultKindEnum.SQDRK_RESULT_YES;
            if(result.isChecked() == true)
            {
               testInnerClasses = false;
               doInnerClasses = doIt;
            }
         }
         else
         {
            doIt = doInnerClasses;
         }
      }

      if(doIt == true)
      {
         IElement targetNamespace = null;
         if (dropTargetElement instanceof INamespace)
         {
            targetNamespace = (INamespace)dropTargetElement;
         }
         else
         {
            targetNamespace = dropTargetElement;
         }
         
         INamedElement namedElement = null;
         if (element instanceof INamedElement)
         {
            namedElement = (INamedElement)element;
         }
         testImports = moveOrImportElement(targetNamespace,
                                           namedElement,
                                           testImports,

                                           performImport);
      }
   }

   /**
    * Handles the movement of one element into the namespace or ownership of another,
    * making sure to take cross project occurrences into account
    *
    * @param dropTargetElement The element that is being dropped on
    * @param namedElement The element being dropped onto pNamespaceDropTarget
    * @param testImports true if the import check needs to be performed for each element, else false
    * @param performImport true if the import should be done period.
    * @return whether or not imports should be checked.
    */
   protected boolean moveOrImportElement(IElement      dropTargetElement,
                                         INamedElement namedElement,
                                         boolean       testImports,
                                         ImportResult  performImport)
   {
      if(namedElement != null)
      {
         // We need to make one last check. If dropTargetElement
         // and namedElement are not in the same project, we need
         // to ask the user if they want to import namedElement into
         // dropTargetElement, or actually move it
         if(testImports )
         {
            boolean applyToAll = checkForPossibleImport(dropTargetElement,
                                                        namedElement,
                                                        performImport);

            if(applyToAll == true)
            {
               testImports = false;
            }
         }

         if(performImport.isPerformImport() == false)
         {
            boolean sameProject = dropTargetElement.inSameProject(namedElement);

            if (dropTargetElement instanceof INamespace)
            {
               INamespace spaceTarget = (INamespace)dropTargetElement;
			   if(Util.hasNameCollision(spaceTarget, namedElement.getName(), 
					   namedElement.getElementType(), namedElement))
			   {
				   DialogDisplayer.getDefault().notify(
						new NotifyDescriptor.Message(NbBundle.getMessage(
							DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
				   return testImports;

			   }
			   else
				   spaceTarget.addOwnedElement(namedElement);
            }
            else
            {
               dropTargetElement.addElement(namedElement);
            }

            INamespace curNamespace = namedElement.getNamespace();
            if((dropTargetElement != null) &&
               (curNamespace != null)      &&
               (sameProject == false))
            {
               // we have just moved namedElement into importingElement, which
               // is in a different project. To be safe, the namespace that
               // namedElement came from should now import namedElement.
               MetaLayerRelationFactory fact = MetaLayerRelationFactory.instance();
               boolean isNeeded = fact.isImportNeeded(curNamespace, namedElement);
               if (isNeeded)
               {
				fact.establishImportIfNeeded(curNamespace, namedElement);
               }
            }
         }
         else
         {
            if((performImport.isImportCanceled() == false) &&
               (performImport.isPerformImport() == true)   &&
               (dropTargetElement != null))
            {
               MetaLayerRelationFactory fact = MetaLayerRelationFactory.instance();
               boolean isNeeded = fact.isImportNeeded(dropTargetElement, namedElement);
               if (isNeeded)
               {
					fact.establishImportIfNeeded(dropTargetElement,
												 namedElement);
               }
            }
         }
      }

      return testImports;
   }

   /**
    * If importingElement and elementToImport are NOT in the same Project, then the user is asked
    * if they want to actually move elementToImport into the importingElement's project, or create
    * an element import relationship between the projects
    *
    * @param importingElement The element that is the target of the drop.
    * @param elementToImport  The elemnet being dropped.
    * @param performImport The import settings.  isPerformImport is set to
    *                      true if the user wants to import the element rather
    *                      than move it ( the default ).  isImportCanceled is
    *                      true if the user wants to cancel the entire operation
    *
    * @return true if apply to all was checked
    */
   protected boolean checkForPossibleImport(IElement      importingElement,
                                            INamedElement elementToImport,
                                            ImportResult  performImport)
   {
      boolean retVal = false;

      if((importingElement != null) && (elementToImport != null))
      {
         if(importingElement.inSameProject(elementToImport) == false)
         {
            // Now make sure that the elementToImport supports the
            // IAutonomousElement interface.  If it doesn't, import is not
            // allowed.
            if (elementToImport instanceof IAutonomousElement)
            {
               IAutonomousElement autoEl = (IAutonomousElement)elementToImport;
               IQuestionDialog dialog = UIFactory.createQuestionDialog();

               String title = DefaultEngineResource.getString("IDS_CROSS_PROJECT_LINKAGE_TITLE");
               String checkbox = DefaultEngineResource.getString("IDS_CROSS_PROJECT_LINKAGE_CHECKBOX");
               String message = DefaultEngineResource.getString("IDS_CROSS_PROJECT_LINKAGE_MESSAGE");

               QuestionResponse result;
                   result = dialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNOCANCEL,
                                                                       MessageIconKindEnum.EDIK_ICONQUESTION,
                                                                       message,
                                                                       checkbox,
                                                                       title,
                                                                       MessageResultKindEnum.SQDRK_RESULT_YES,
                                                                       true);

               if(result.getResult() == MessageResultKindEnum.SQDRK_RESULT_NO)
               {
                  performImport.setPerformImport(true);
               }
               else if(result.getResult() == MessageResultKindEnum.SQDRK_RESULT_CANCEL)
               {
                  performImport.setPerformImport(true);
                  performImport.setImportCanceled(true);
               }

               retVal = result.isChecked();
            }
            else
            {
               performImport.setPerformImport(true);
               performImport.setImportCanceled(true);
            }
         }
      }

      return retVal;
   }

   /**
    * Returns true if the projecttreeengine.etc says that sElement1 is a valid
    * drop target on sElement2.
    *
    * @param pMovingElement The element being moved
    * @param pDropTargetElement The element of the drop target
    * @return true if sMovingElementType is valid to move onto a
    *              sDropTargetElementType
    */
   protected boolean isValidDropTarget(IElement pMovingElement,
                                       IElement pDropTargetElement)
   {
      boolean retVal = false;

      if((pMovingElement != null) && (pDropTargetElement != null))
      {
         String targets = m_ValidDropTargets.get(pMovingElement.getElementType());
         if(targets != null)
         {
            StringTokenizer tokenizer = new StringTokenizer(targets, ",");
            while((tokenizer.hasMoreTokens() == true) && (retVal == false))
            {
               String token = tokenizer.nextToken();
               if(token.length() > 0)
               {
                  if(m_GUIDMap.containsKey(token) == true)
                  {
                     Class myClass = pDropTargetElement.getClass();

                     if(isImplemented(pDropTargetElement, m_GUIDMap.get(token)) == true)
                     {
                        retVal = true;
                     }
                  }
               }
            }
         }
      }

      return retVal;
   }

   protected boolean isImplemented(IElement object, String name)
   {
      boolean retVal = false;

      Class   myClass    = object.getClass();
      Class[] interfaces = myClass.getInterfaces();
      for (int index = 0; (index < interfaces.length) && (retVal == false); index++)
      {
         String fullName = interfaces[index].getName();
         retVal = fullName.equals(name);

         if(retVal == false)
         {
            Class classType;
            try
            {
               classType = Class.forName(name);
               //Field guidField = classType.getField("GUID");
               //String guidID = (String)guidField.get(null);

               //if(guidID.length() > 0)
               {
                  //retVal = object.getClass().getName().equals(name);//Dispatch.isType(object, guidID);
                  retVal = classType.isAssignableFrom(object.getClass());
               }


//               Class[] params = {com.embarcadero.com.Dispatch.class};
//               Constructor constructor = classType.getConstructor(params);
//
//               Object[] paramInstances = {object};
//               constructor.newInstance(paramInstances);
//               retVal = true;
            }
            catch(Throwable e)
            {
               e.printStackTrace();
               retVal = false;
            }
         }
      }

      return retVal;
   }

   /**
    * @param item
    * @return
    */
   protected int canMove(IProjectTreeItem item)
   {
      int retVal = MRK_DISALLOW;

      if(item != null)
      {
         String elementType = item.getModelElementMetaType();
         if(elementType.length() > 0)
         {
            // We have a model element.  So, See if it's in our moveable items
            // list that was created via the XML file when we constructed.
            if(m_MoveableItems.contains(elementType) == true)
            {
               // Make sure that the ITreeItem is not an ITreeRelElement.  If it
               // is that means that this guy is a child under a relationship.
               // We don't want users thinking they can alter relationships by
               // moving the ends - yet we want to allow it dropped onto the
               // diagram.  So if we have an ITreeRelElement allow DND, but
               // disable any dropping on this tree.
               if(item.getProjectTreeSupportTreeItem() instanceof ITreeRelElement)
               {
                  retVal = MRK_ALLOW_MOVE_FOUND_RELATIONSHIP_END;
               }
               else
               {
                  retVal = MRK_ALLOW_MOVE;
               }
            }
         }
         else
         {
            // Check if there is a proxy diagram.  If so All diagrams can be
            // moved.  If a sequence diagram is dropped onto the tree then the
            // move is ignored, but it's allowed to be dropped onto another
            // sequence diagram GET object - That will result in the creation
            // of an InteractionOccurrence
            retVal = MRK_ALLOW_MOVE;
         }
      }
      return retVal;
   }

   protected boolean handleModifiedAttribute(IElement modelElement, ITreeItem item)
   {
      boolean retVal = false;

      if (modelElement instanceof IAttribute)
      {
         IAttribute attr = (IAttribute)modelElement;
         String formatted = getFormattedString(attr);

         if(formatted != null && formatted.equals(item.getDisplayedName()) == false) 
         {
            item.setDisplayedName(formatted);
         }
			retVal = true;
      }

      return retVal;
   }

   protected boolean handleModifiedEnumerationLiteral(IElement modelElement, ITreeItem item)
   {
      boolean retVal = false;

      if (modelElement instanceof IEnumerationLiteral)
      {
         IEnumerationLiteral attr = (IEnumerationLiteral)modelElement;
         String formatted = getFormattedString(attr);

         if(formatted != null && formatted.equals(item.getDisplayedName()) == false) 
         {
            item.setDisplayedName(formatted);
         }
			retVal = true;
      }

      return retVal;
   }
   
   /**
    * @param attr
    * @return
    */
   private String getFormattedString(IElement element)
   {
      String retVal = "";

      if(element != null)
      {
         if (element instanceof IDiagram)
         {
            IDiagram diagram = (IDiagram)element;
            retVal = diagram.getNameWithAlias();
         }
         else
         {
            IDataFormatter formatter = ProductHelper.getDataFormatter();
            if(formatter != null)
            {
               retVal = formatter.formatElement(element);
            }
         }
      }

      return retVal;
   }

   protected boolean handleModifiedOperation(IElement modelElement, ITreeItem item)
   {
      boolean retVal = false;

      if (modelElement instanceof IOperation)
      {
         IOperation oper = (IOperation)modelElement;
         String formatted = getFormattedString(oper);

         if(formatted != null && formatted.equals(item.getDisplayedName()) == false)
         {
            item.setDisplayedName(formatted);
         }
			retVal = true;
      }

      return retVal;
   }

   protected boolean handleModifiedNamedElement(IElement  modelElement,
                                                ITreeItem item)
   {
      boolean retVal = false;

      if( !(item instanceof ITreeFolder) )
      {
         if (modelElement instanceof INamedElement)
         {
            INamedElement element = (INamedElement)modelElement;
            //String formatted = element.getNameWithAlias();
            String formatted = getFormattedString(element);

            String itemName = item.getDisplayedName();
            if (formatted != null && !formatted.equals(item.getDisplayedName()))
            {               
               item.setDisplayedName(formatted);
               // If the name is set then the model element is update.
               // Since this is from an issue I do not want to cause the
               // model element to be updated again.
               //item.setName(formatted);
               retVal = true;
            }
         }
      }
      return retVal;
   }

   /**
    * Removes an element node from the tree.
    *
    * @param element The element that has been removed.
    */
   protected void onElementDelete(IElement element)
   {
     if(element != null)
     {
         String elementType = element.getElementType();
         if((elementType.equals(ELEMENT_IMPORT) == true) ||
            (elementType.equals(PACKAGE_IMPORT) == true))
         {
         }
         else
         {
            getTreeModel().removeAll(element);
         }
      }
   }

   /**
    * Notifies all change listeners that an element node has changed.
    *
    * @param element The element that has changed.
    * @see IProjectTreeModel#notifyOfNodesChanged(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */
   protected void notifyElementChanged(final IElement element)
   {
      if(element != null)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
             public void run()
             {
                 String formattedValue = getFormattedString(element);
                 IProjectTreeModel model = getTreeModel();
                 if(model != null)
                 {
                    ETList < ITreeItem > items = model.findNodes(element);
                    notifyElementChanged(items, formattedValue);
                 }
             }
         });
      }
   }

   /**
    * Notifies all change listeners that a proxy diagram node has changed.
    *
    * @param element The proxy diagram that has changed.
    * @see IProjectTreeModel#notifyOfNodesChanged
    */
   protected void notifyElementChanged(IProxyDiagram element)
   {
      if(element != null)
      {
         String formattedValue = element.getNameWithAlias();
         IProjectTreeModel model = getTreeModel();
         if(model != null)
         {
            ETList < ITreeItem > items = model.findDiagramNodes(element.getFilename());
            notifyElementChanged(items, formattedValue);
         }
      }
   }

   protected void notifyElementChanged(ETList < ITreeItem > items,
                                       String               displayValue)
   {
      if(items != null)
      {
         IProjectTreeModel model = getTreeModel();
         for(int index = 0; index < items.size(); index++)
         {
            ITreeItem curItem = items.get(index);

            if(curItem != null)
            {
               curItem.setDisplayedName(displayValue);

               ITreeItem parent = curItem.getParentItem();
               int[]    indices = { model.getIndexOfChild(parent, curItem) };
               ITreeItem[] nodes  = { curItem };
               model.notifyOfNodesChanged(parent, indices, nodes);
            }
         }
      }
   }
   /**
    * Add a new diagram to the tree.  The diagram location is used to find
    * the proxy diagram and the namespace that contains the daigram.
    *
    * @param diagramLocation The location of the diagram.
    */
   public void addDiagramNode(String diagramLocation)
   {
      if ((diagramLocation != null) && (diagramLocation.length() > 0))
      {
         IProxyDiagramManager manager = ProxyDiagramManager.instance();
         IProxyDiagram proxy = manager.getDiagram(diagramLocation);
         
         ProjectTreeNodeFactory factory = getNodeFactory();
         if ((proxy != null) && (factory != null))
         {            
            //ITreeDiagram newItem = new ProjectTreeDiagramNode(proxy);
            ITreeDiagram newItem = factory.createDiagramNode(proxy);
            newItem.setDisplayedName(proxy.getNameWithAlias());
            // newItem.setName(proxy.getDiagramKindName());
            newItem.setName(proxy.getNameWithAlias());
            String namespaceXMID = proxy.getNamespaceXMIID();
            IElement namespace = proxy.getNamespace();
			
            if (namespace != null)
            {
               ETList<ITreeItem> items = getTreeModel().findNodes(namespace);
			   
               for (Iterator < ITreeItem > iter = items.iterator();
                    iter.hasNext();)
               {
                  ITreeItem curItem = iter.next();
                  int diagramKind = proxy.getDiagramKind();

                  if (curItem.isInitalized())
                  {
                     addDiagram(curItem, newItem);
                     getTreeModel().sortChildren(curItem);
                  }
               }
            }
         }
      }
   }
   
   /**
    * Retrieves the factory to use when creating project tree nodes.
    * 
    * @return The node factory.
    */
   protected ProjectTreeNodeFactory getNodeFactory()
   {
      ProjectTreeNodeFactory retVal = null;
      
      IProjectTreeModel model = getTreeModel();
      if(model != null)
      {
         retVal = model.getNodeFactory();
      }
      
      return retVal;
   }

   /**
	* Finds this diagram and sets the icon accordingly.
	*
	* @param pParentDiagram
	* @param sDiagramLocation
	* @param bIsOpen
	*/
   public void changeDiagramIcon(IDiagram pParentDiagram, String sDiagramLocation, boolean isOpen) 
   {
   		IProxyDiagramManager mgr = ProxyDiagramManager.instance();
   		if (pParentDiagram != null && m_TreeModel != null)
   		{
   			ETList<ITreeItem> items = m_TreeModel.findDiagramNodes(sDiagramLocation);
   			if (items != null)
   			{
   				int count = items.size();
   				if (count > 0)
   				{
   					String diaKind = pParentDiagram.getDiagramKind2();
   					String diaType = getDiagramIcon(diaKind, true, isOpen, false);
   					for (int i=0; i<count; i++)
   					{
   						ITreeItem item = items.get(i);
   						if (item != null)
   						{
   							setImage(item, diaType);
   							
//   							//now set the appropriate model element
//   							IProjectTreeItem projItem = item.getData();
//   							if (isOpen && projItem != null)
//   							{
//   								//set the diagram on projItem
//   								projItem.setModelElement(pParentDiagram);
//   							}
//   							else if (!isOpen && projItem != null)
//   							{
//   								//set the proxy diagram on projItem
//   								projItem.setModelElement(null);
//   							}
   						}
   					}
   				}
   			}
   		}
   }

   /**
	* Given a key string this looks the string up in the resource manager and returns
	* the icon that should be used for that type.
	*
	* @param pTreeItem[in] Contains the HTREEITEM that is to receive the icon
	* @param sIconType[in] The keystring that should be used to lookup the icon (usually the element name or folder name)
	*/
   private void setImage(ITreeItem pTreeItem, String sIconType)
   {
   		if (m_TreeModel != null)
   		{
			if (pTreeItem != null && pTreeItem instanceof ITreeDiagram)
			{
				ITreeDiagram diaItem = (ITreeDiagram)pTreeItem;
				diaItem.setDiagramType(sIconType);
			}
   		}
   }

   public void onNodeExpanding(IProjectTreeControl          pParentControl,
									    IProjectTreeExpandingContext pContext,
                               FilteredItemManager          manager)
   {
      if (isProjectTree(pParentControl))
      {
         if(manager == null)
         {
            handleItemExpanding(pParentControl, pContext);
         }
         else
         {
            handleItemExpanding(pParentControl, pContext, manager);
         }
      }
   }

   /**
	 * Handles the after edit event.
	 *
	 * @param pParentControl [in] The tree that caused the event
	 * @param pItem [in] The item being edited
	 * @param pVerify [in] The status after this connection point has finished
	 */
   protected void handleItemExpanding(IProjectTreeControl          pParentControl,
									           IProjectTreeExpandingContext pContext)
   {
      handleItemExpanding(pParentControl, pContext, getFilterManager());
   }
   
   protected void handleItemExpanding(IProjectTreeControl          pParentControl,
				      IProjectTreeExpandingContext pContext,
                                      FilteredItemManager          filterManager)
   {
       //IProjectTreeItem treeItem = null;
       ITreeItem treeItem = null;
       if(pContext != null)
       {
           treeItem = pContext.getTreeItem();
           if(treeItem != null)
           {
               treeItem.setIsInitalized(true);
           }
       }
       
       if((treeItem != null) && (getTreeModel() != null))
       {
           IProjectTreeItem item = treeItem.getData();
           
           if(item.getDescription().equals(IProjectTreeControl.PROJECT_DESCRIPTION) == true)
           {
               pContext.setCancel(handleProjectExpanding(item));
           }
           
           IElement modelElement = item.getModelElement();
           if(pContext.isCancel() == false)
           {
               // If we have a model element show its members.  Remember that
               // a model element could have been the IProject we just opened above.
               if(modelElement != null)
               {
                   // This is a special case where all the element types are
                   // hidden, we put the diagrams, no matter the namespace,
                   // under the IProject
                   // but only if we are not in the design center
                   if((modelElement instanceof IProject) &&
                           (filterManager.areAllModelElementsHidden() == true) &&
                           !isDesignCenter()
                           )
                   {
                       addAllDiagramsInProject(treeItem, (IProject)modelElement);
                   }
                   else
                   {
                       addOwnedElements(treeItem, modelElement);
                   }
               }
               else
               {
                   addOwnedElements(treeItem, modelElement);
               }
           }
       }
   }
   public void onNodeDoubleClick(IProjectTreeControl pParentControl,
							 IProjectTreeItem pItem,
							 boolean             isControl,
							 boolean             isShift,
							 boolean             isAlt,
							 boolean             isMeta)
   {
       if (isProjectTree(pParentControl))
       {
           handleDoubleClick(pParentControl, pItem, isControl, isShift, isAlt, isMeta);
       }
   }
   protected void handleDoubleClick(IProjectTreeControl pParentControl,
				    IProjectTreeItem pItem,
				    boolean             isControl,
				    boolean             isShift,
				    boolean             isAlt,
				    boolean             isMeta)
   {
	  if(pItem != null)
	  {
		 IElement modelElement = pItem.getModelElement();
		 IProduct product      = ProductHelper.getProduct();

		 boolean didNavigation = false;

		 if((modelElement != null) && (product != null))
		 {
			String type = modelElement.getElementType();
			if(type.equals("Artifact") == true)
			{
			   didNavigation = navigateToArtifact(modelElement);
			}

			if(didNavigation == false)
			{
			   IDiagramAndPresentationNavigator navigator = new DiagramAndPresentationNavigator();
			   navigator.handleNavigation(0, modelElement, isShift);
			}
			else
			{
			   //update the project tree.
			}
		 }
		 else if (product != null)
		 {
			 // Get the description and see if its a diagram
			 String desc = pItem.getDescription();
			 if (desc != null && desc.length() > 0)
			 {
				 // If we've got a .etld file then we've got a diagram
				 if (StringUtilities.hasExtension(desc, FileExtensions.DIAGRAM_LAYOUT_EXT))
				 {
					 IProductDiagramManager diaMgr = product.getDiagramManager();
					 if (diaMgr != null)
					 {
						 // See if the diagram is open
						 IDiagram dia = diaMgr.getOpenDiagram(desc);
            				
						 if (dia != null)
						 {
							 // Raise the diagram
							 diaMgr.raiseWindow(dia);
						 }
						 else
						 {
							 // Open the diagram
							 dia = diaMgr.openDiagram(desc, true, null);
						 }
					 }
				 }
			 }
		 }
	  }
   }
   protected boolean navigateToArtifact(IElement modelElement)
   {
	  boolean retVal = false;

	  IArtifact artifact = null;
	  if (modelElement instanceof IArtifact)
	  {
		 artifact = (IArtifact)modelElement;
	  }

	  String filename = artifact.getFileName();
	  if((filename != null) && (filename.length() > 0))
	  {
		 retVal = true;
		 String extension = "." + FileSysManip.getExtension(filename);
	  }

	  return retVal;
   }

   /**
	* Handles expanding a projects nodes.  The project is opened and its contents
	* are added to the projects nodes.  The contents of a project are build using
	* the project tree builder and the user filter.
	*
	* @param pContext
	* @param treeItem
	*/
   protected boolean handleProjectExpanding(IProjectTreeItem treeItem)
   {
	  boolean retVal = false;

	  IApplication app = ProductHelper.getApplication();

	  // See if we have a workspace project.
	  if(getWorkspace() != null)
	  {
		 String prjName = treeItem.getItemText();
		 IWorkspace space = getWorkspace();

		 //IWSProject wsProject = space.openWSProjectByName(prjName);
		 IProject project = app.openProject(getWorkspace(), prjName);
		 //IProject project = app.getProjectByName2(getWorkspace(), prjName);

		 if(project != null)
		 {
			// Since the model element has not been set by the time that the
			// item has been expanded.  (Reason: The IProject object is not
			// created until the project has been opened.) Do not set the project.
			treeItem.setModelElement(project);
		 }

		 if(project == null)
		 {
			 retVal = true;
		 }
	  }

	  return retVal;
   }

   /**
    * Called when the an element has been modified.
    *
    * @param element The element that was just modified
    */
   public void handleElementModified(IVersionableElement element)
   {
      if(element instanceof IElement)
      {
         IElement modelElement = (IElement)element;
         //IElement modelElement = new IElementProxy((Dispatch)element);
         String type = modelElement.getElementType();
         if((type.equals(ELEMENT_IMPORT_ELEMENT_TYPE) == true) &&
            (type.equals(PACKAGE_IMPORT_ELEMENT_TYPE) == true))
         {
            // A new element import relationship was created.  Refresh the tree
            //
            // NOTE : This refresh was causing HUGE performance problems with imported
            // libraries.  So we've commented this out for release.
            //_VH(m_ProjectTree->Refresh());
         }
         else
         {
            ArrayList affectedItems = new ArrayList();

            ETList < ITreeItem > items = getTreeModel().findNodes(modelElement);
            //for(int index = 0; index < items.length; index++)
            for (Iterator < ITreeItem > iter = items.iterator(); iter.hasNext();)
            {
               ITreeItem curItem = iter.next();

               boolean wasHandled = false;
               
               if(handleModifiedAttribute(modelElement, curItem) == true)
               {
                  wasHandled = true;
               }
               
               else if (handleModifiedOperation(modelElement, curItem) == true)
               {
                  wasHandled = true;
               }
               
               else if(handleModifiedEnumerationLiteral(modelElement, curItem) == true)
               {
                  wasHandled = true;
               }

               else if(handleModifiedNamedElement(modelElement, curItem) == true)
               {
                  wasHandled = true;
               }

               if(wasHandled == true)
               {
                  ITreeItem parent = curItem.getParentItem();
                  int[] childIndices = {getTreeModel().getIndexOfChild(parent, curItem)};
                  ITreeItem[] modifiedItems = { curItem };
                  getTreeModel().notifyOfNodesChanged(parent, childIndices, modifiedItems);
               }
            }
         }
      }
   }
   
   /**
    * Called when we receive several events from the ITypedElementEventsSink.  
    * This updates the attribute when a multiplicity range is modified
    */
   public void multiplicityModified(final ITypedElement     element,  
                                    IMultiplicity           mult,  
                                    IMultiplicityRange      range)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if(element instanceof IAttribute)   
            {
               handleElementModified((IAttribute)element);
            }
            else if(element instanceof IParameter)
            {
               IParameter param = (IParameter)element;
               handleElementModified(param);

               IElement owner = param.getOwner();
               if(owner instanceof IOperation)
               {
                  handleElementModified(owner);
               }
            }
         }
      });
   }
   
   protected boolean isDesignCenter()
   {
   	boolean bFlag = false;
      
      String modelName = m_TreeModel.getModelName();
      
      //if (m_TreeModel instanceof DesignCenterSwingModel)
      if(modelName.equals("DesignCenter") == true)         
		
		{
			bFlag = true;
		}
   	return bFlag;
   }
   
   /**
    * Determines whether or not the current EventContext is a
    * "PresentationAdded" or "DefaultImports" context.  If the current context 
    * is a NoEffectModification context then the context is returned.
    *
    * @return  The context that is a no effect context. If null is
    *          return, the context is not a No Effect Modification.
    *
    */
   protected IEventContext getNoEffectModification()
   {
      EventContextManager man = new EventContextManager();
      
      IEventContext retVal = null;
      if(man.isNoEffectModification() == true)
      {
         retVal = man.getNoEffectContext();
      }
      return retVal;
   }
   
   
	//**************************************************
   // Event Sinks Classes
   //**************************************************

   protected class ProjectTreeListener extends ProjectTreeEventsAdapter
	{
	   /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onAfterEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
      public void onAfterEdit(IProjectTreeControl pParentControl,
                              IProjectTreeItem pItem,
                              IProjectTreeEditVerify pVerify,
                              IResultCell cell)
      {
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeforeEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
      public void onBeforeEdit(IProjectTreeControl pParentControl,
                               IProjectTreeItem pItem,
                               IProjectTreeEditVerify pVerify,
                               IResultCell cell)
      {
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeginDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
      public void onBeginDrag(IProjectTreeControl pParentControl,
                              IProjectTreeItem[] pItem,
                              IProjectTreeDragVerify pVerify,
                              IResultCell cell)
      {
         if(isProjectTree(pParentControl) == true)
         {
            handleBeginDrag(pParentControl, pItem, pVerify);
         }
      }

      /**
       * Handles the double click event.  The double click event will be used
       * to navigate to a diagram that contains the model element.
       *
       * @param pParentControl [in] The tree that caused the event
       * @param pItem[in] The item that was double clicked on
       */
      public void onDoubleClick(IProjectTreeControl pParentControl,
                                IProjectTreeItem pItem,
                                boolean             isControl,
                                boolean             isShift,
                                boolean             isAlt,
                                boolean             isMeta,
                                IResultCell cell)
      {
		onNodeDoubleClick(pParentControl, pItem, isControl, isShift, isAlt, isMeta);
       }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onEndDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IDataObject, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
      public void onEndDrag(IProjectTreeControl    pParentControl,
                            Transferable           transfer,
                            int                    action,
                            IProjectTreeDragVerify pVerify,
                            IResultCell            cell)
      {
         if(isProjectTree(pParentControl) == true)
         {
            handleEndDrag(pParentControl, transfer, action, pVerify);
         }
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
      public void onItemExpanding(IProjectTreeControl          pParentControl,
                                  IProjectTreeExpandingContext pContext,
                                  IResultCell                  cell)
      {
      	// TODO: The C++ version is asking if the control is a project tree control
      	//       The problem is that every time that we add a new deriviative of project
      	//       tree we will have to ask another question.  Therefore, think of something
      	//       else.
         onNodeExpanding(pParentControl, pContext, null);
      }

      public void onItemExpandingWithFilter( IProjectTreeControl pParentControl,
                                             IProjectTreeExpandingContext pContext, 
                                             FilteredItemManager manager, 
                                             IResultCell cell )
      {
         onNodeExpanding(pParentControl, pContext, manager);
      }
      
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onMoveDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IDataObject, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
      public void onMoveDrag(IProjectTreeControl pParentControl,
                             Transferable pItem,
                             IProjectTreeDragVerify pVerify,
                             IResultCell cell)
      {

      }

	}

   public class EngineElementModifiedSink extends ElementModifiedEventsAdapter
   {
      /**
       * Called when the an element has been modified.
       *
       * @param element The element that was just modified
       */
      public void onElementModified(final IVersionableElement element, IResultCell cell)
      {
         IEventContext  context = getNoEffectModification();
         if( context == null)
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                   handleElementModified(element);
               }
            });
         }
      }
   }

   public class EngineNamespaceModifiedSink extends NamespaceModifiedEventsAdapter
   {
      public void onElementAddedToNamespace(INamespace space,
                                            final INamedElement elementAdded,
                                            IResultCell cell)
      {
         if(elementAdded != null)
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()               
               {
                  addNewlyCreatedElement(elementAdded);
               }
            });
         }
      }
   }

   public class EngineClassifierFeatureSink extends ClassifierFeatureEventsAdapter
   {
      public void onFeatureAdded(IClassifier classifier,
                                 IFeature    feature,
                                 IResultCell cell)
      {
         if((classifier != null) && (feature != null))
         {
            updateItems(feature);
         }
      }
      
      public void onFeatureMoved(IClassifier classifier,
                                 IFeature feature,
                                 IResultCell cell)
      {
          if((classifier != null) && (feature != null))
          {
              getTreeModel().removeAll(feature);
              updateItems(feature);
              
//              IClassifier owner = feature.getFeaturingClassifier();
//              ETList < ITreeItem > items = getTreeModel().findNodes(owner);
//              for(ITreeItem item : items)
//              {
//                  
//              }
          }
      }
      
      public void onFeatureDuplicatedToClassifier(IClassifier pOldClassifier,
                                                  IFeature pOldFeature,
                                                  IClassifier pNewClassifier,
                                                  IFeature pNewFeature,
                                                  IResultCell cell)
      {
          updateItems(pNewFeature);
      }
      
      public void onEnumerationLiteralAdded(
          IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
      {
         if ((classifier != null) && (enumLit != null))
         {
            updateItems(enumLit);
         }
      }
      
   }

   public class EngineClassifierTransformSink implements IClassifierTransformEventsSink
   {
      /**
       * Fired whenever a classifier is about to be transformed.
      */
      public void onPreTransform( IClassifier classifier,
                                  String      newForm,
                                  IResultCell cell )
      {
//         onElementDelete(classifier);
      }

      /**
       * Fired right after a classifier is transformed into a new one.
      */
      public void onTransformed( final IClassifier classifier,
                                 IResultCell cell )
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()               
            {
               addNewlyCreatedElement(classifier);
            }
         });
      }
   }

   public class EngineElementLifeTimeSink extends ElementLifeTimeEventsAdapter
   {
      public void onElementDeleted(IVersionableElement element,
                                   IResultCell cell)
      {
         if(element instanceof IElement)
         {
            onElementDelete((IElement)element);
         }

      }
   }

   public class EngineAttributeEventsSink extends AttributeEventsAdapter
   {
      public void onDefaultModified(IAttribute attr,
                                    IResultCell cell)
      {
         if(attr != null)
         {
            notifyElementChanged(attr);
         }
      }

      public void onDefaultBodyModified(IAttribute feature,
                                        IResultCell cell)
      {
         if(feature != null)
         {
            notifyElementChanged(feature);
         }
      }

      public void onDefaultLanguageModified(IAttribute feature,
                                            IResultCell cell)
      {
         if(feature != null)
         {
            notifyElementChanged(feature);
         }
      }
   }

   public class EngineOperationEventsSink extends OperationEventsAdapter
   {
      public void onConditionAdded(IOperation oper,
                                   IConstraint condition,
                                   boolean isPreCondition,
                                   IResultCell cell)
      {
         if(oper != null)
         {
            notifyElementChanged(oper);
         }
      }

      public void onConditionRemoved(IOperation oper,
                                     IConstraint condition,
                                     boolean isPreCondition,
                                     IResultCell cell)
      {
         if(oper != null)
         {
            notifyElementChanged(oper);
         }
      }

      public void onQueryModified(IOperation oper, IResultCell cell)
      {
         if(oper != null)
         {
            notifyElementChanged(oper);
         }
      }

      public void onRaisedExceptionAdded(IOperation oper,
                                         IClassifier pException,
                                         IResultCell cell)
      {
         if(oper != null)
         {
            notifyElementChanged(oper);
         }
      }

      public void onRaisedExceptionRemoved(IOperation oper,
                                           IClassifier pException,
                                           IResultCell cell)
      {
         if(oper != null)
         {
            notifyElementChanged(oper);
         }
      }
   }

   public class EngineDrawingAreaSink extends DrawingAreaEventsAdapter
   {
       public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl,
               IResultCell cell)
       {
           if(pDiagramControl != null)
           {
               final IDrawingAreaControl control = pDiagramControl;
               SwingUtilities.invokeLater(new Runnable()
               {
                   public void run()
                   {
                       addDiagramNode(control.getFilename());
                   }
               });
           }
       }

      // NOTE: I will have to determine what to do about the IDiagram Parameter
      //       once we start sending the events.
      public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
      {
      	if (pParentDiagram != null)
      	{
      		String filename = pParentDiagram.getFilename();
      		if (filename != null && filename.length() > 0)
      		{
      			changeDiagramIcon(pParentDiagram, filename, true);
      			notifyElementChanged(pParentDiagram);
      		}
      	}
      }

      //    NOTE: I will have to determine what to do about the IDiagram Parameter
      //          once we start sending the events.
      public void onDrawingAreaClosed(IDiagram pParentDiagram,
                                      boolean bDiagramIsDirty,
                                      IResultCell cell)
      {
		if (pParentDiagram != null)
		{
			String filename = pParentDiagram.getFilename();
			if (filename != null && filename.length() > 0)
			{
				changeDiagramIcon(pParentDiagram, filename, false);
				notifyElementChanged(pParentDiagram);
			}
		}
      }

      public void onDrawingAreaPostPropertyChange(final IProxyDiagram pProxyDiagram,
                                                  int nPropertyKindChanged,
                                                  IResultCell cell)
      {
//         if((nPropertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAMESPACE) ||
//            (nPropertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAME))
         if(nPropertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAME)
         {
            notifyElementChanged(pProxyDiagram);
         }
         else if(nPropertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAMESPACE)
         {
             IProjectTreeModel model = getTreeModel();
             ETList < ITreeItem > items = model.findDiagramNodes(pProxyDiagram.getFilename());    
             for(ITreeItem item : items)
             {
                getTreeModel().removeNodeFromParent(item);
             }
             
             SwingUtilities.invokeLater(new Runnable()
             {

                 public void run()
                 {
                     addDiagramNode(pProxyDiagram.getFilename());
                 }
             });
             
//             ETList < ITreeItem > owner = model.findNodes(pProxyDiagram.getNamespace()); 
//             if(owner != null)
//             {                 
//                 
////                 // This should never be more than one but you can never know.
////                 for(ITreeItem item : items)
////                 {
//////                    ITreeDiagram diagram = model.getNodeFactory().createDiagramNode(pProxyDiagram);
//////                    getTreeModel().addItem(item, diagram);
//////                    diagram.setParentItem(item);
////                     ITreeDiagram newItem = getNodeFactory().createDiagramNode(pProxyDiagram);
////                     newItem.setDisplayedName(pProxyDiagram.getNameWithAlias());
////					 // cvc - CR#6265213   
////					 // the tree node's and the diagram's name was constantly 
////					 //  being reset to the diagram type name ???
////					 // newItem.setName(diagrams.get(index).getDiagramKindName());
////                     newItem.setName(pProxyDiagram.getNameWithAlias());
////                     newItem.setSortPriority(m_TreeBuilder.getSortPriority(pProxyDiagram.getDiagramKindName()));
////                     //setTreeItemParent(newItem, parent);
////                     getTreeModel().addItem(item, newItem);
////                 }
//             }
         }
      }

      public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
      {
         IProjectTreeModel model = getTreeModel();
         if(model != null)
         {
            ETList < ITreeItem > diagramItems = model.findDiagramNodes(sFilename);

            if(diagramItems != null)
            {
               for (Iterator < ITreeItem > iter = diagramItems.iterator();
                    iter.hasNext();)
               {
                  model.removeNodeFromParent(iter.next());
               }
            }
         }
      }
   }

   public class EngineProductInitEventsSink extends CoreProductInitEventsAdapter
   {
      public void onCoreProductPreQuit(ICoreProduct product,
                                       IResultCell result)
      {
         deInitialize();
      }
   }

   public class EngineAffectedElementEventsSink extends AffectedElementEventsAdapter
   {
      public void onImpacted(IClassifier          classifier,
                             ETList<IVersionableElement> impacted,
                             IResultCell cell)
      {
         if (impacted != null)
         {
            ETList<IVersionableElement> impactedElements = impacted;

            int max = impactedElements.size();
            for(int index = 0; index < max; index++)
            {
               IVersionableElement curElement = impactedElements.get(index);
               if(curElement instanceof IAttribute)
               {
                  notifyElementChanged((IElement)curElement);
               }
               if(curElement instanceof IParameter)
               {
                  IParameter parameter = (IParameter)curElement;
                  notifyElementChanged(parameter.getOwner());
               }
            }
         }
      }
   }

   public class EngineRelationEventsSink extends RelationEventsAdapter
   {
      public void onRelationCreated(IRelationProxy proxy,
                                    IResultCell cell)
      {
         IElement element = proxy.getConnection();
         if(element != null)
         {
            updateItems(element);
         }
      }
   }
   
//   public class EngineWSElementEventsSink extends WSElementEventsAdapter
//   {
//      public long onWSElementNameChanged(IWSElement element,
//                                         com.embarcadero.describe.umlsupport.IResultCell cell)
//      {
//         WSTreeItemComparator comparator = new WSTreeItemComparator(element);
//         ETList < ITreeItem > items = getTreeModel().findNodes(comparator);
//         notifyElementChanged()items, element.getNameWithAlias());
//
//         return 0;
//      }
//
//      public long onWSElementAliasChanged(IWSElement element,
//                                          com.embarcadero.describe.umlsupport.IResultCell cell)
//      {
//         WSTreeItemComparator comparator = new WSTreeItemComparator(element);
//         ETList < ITreeItem > items = getTreeModel().findNodes(comparator);
//         notifyElementChanged()items, element.getNameWithAlias());
//
//         return 0;
//      }
//   }

   public class EngineTypedElementEventsSink extends TypedElementEventsAdapter
   {
      public void onPreOrderModified(ITypedElement element, IMultiplicity mult, 
                                     boolean proposedValue, IResultCell cell)
      {
         multiplicityModified(element, mult, null);
      }

      public void onRangeAdded(ITypedElement element, IMultiplicity mult, 
                               IMultiplicityRange range, IResultCell cell)
      {
         multiplicityModified(element, mult, range);
      }

      public void onRangeRemoved(ITypedElement element, IMultiplicity mult, 
                                 IMultiplicityRange range, IResultCell cell)
      {
         multiplicityModified(element, mult, range);
      }

      public void onUpperModified(ITypedElement element, IMultiplicity mult, 
                                  IMultiplicityRange range, IResultCell cell)
      {
         multiplicityModified(element, mult, range);
      }

      public void onPreLowerModified(ITypedElement element, IMultiplicity mult,
                                     IMultiplicityRange range, 
                                     String proposedValue, IResultCell cell)
      {
         multiplicityModified(element, mult, range);
      }

      public void onTypeModified(ITypedElement element,
                                 IResultCell cell)
      {
         if(element != null)
         {
            OwnerRetriever< IOperation > ownerRetriever = new OwnerRetriever< IOperation >( element );
            IOperation parentOperation = ownerRetriever.getOwnerByType(element, IOperation.class );            
            
            if (parentOperation != null)
            {
               notifyElementChanged(parentOperation);
            }
         }
      }
      
      
   }

   protected class ImportResult
   {
      boolean m_PerformImport  = false;
      boolean m_ImportCanceled = false;

      /**
       * @return
       */
      public boolean isImportCanceled()
      {
         return m_ImportCanceled;
      }

      /**
       * @return
       */
      public boolean isPerformImport()
      {
         return m_PerformImport;
      }

      /**
       * @param b
       */
      public void setImportCanceled(boolean b)
      {
         m_ImportCanceled = b;
      }

      /**
       * @param b
       */
      public void setPerformImport(boolean b)
      {
         m_PerformImport = b;
      }

   }
   
   protected class EngineFilterSink extends ProjectTreeFilterDialogEventsAdapter
   {
      public void onProjectTreeFilterDialogInit(IFilterDialog dialog,
                                                IResultCell cell)
      {
         if((m_TreeModel != null) && (dialog != null))
         {
            IProjectTreeModel model = dialog.getProjectTreeModel();
            if(model == m_TreeModel)
            {
               m_FilteredItemManager.onProjectTreeFilterDialogInit(dialog, cell);
            }
         }
      }
   }
}
