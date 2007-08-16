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


package org.netbeans.modules.uml.ui.products.ad.diagramengines;


import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSEWindowTool;
import com.tomsawyer.editor.ui.TSEDefaultNodeUI;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSFindChildParent;
import com.tomsawyer.graph.TSGraph;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.util.TSObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IGraphic;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.MetaLayerRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Relationship;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IProcedureOccurrence;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.AutoRoutingAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper;
import org.netbeans.modules.uml.ui.controls.drawingarea.IAutoRoutingAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.SimpleAction;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
import org.netbeans.modules.uml.ui.products.ad.compartments.IStereotypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IADInterfaceEventManager;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IPackageDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IPortDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.QuadrantKindEnum;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETUIFactory;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.IAutoRoutingActionKind;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.DiagramKeyboardAccessProvider;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypeDetails;
import org.netbeans.modules.uml.ui.support.applicationmanager.TSGraphObjectKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSorter;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontChooser;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
import org.netbeans.modules.uml.ui.support.relationshipVerification.INodeVerification;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.UIResources;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaConstants;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.BaseActionWrapper;
import org.netbeans.modules.uml.ui.swing.drawingarea.DataVerificationResults;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.DrawingAreaAddEdgeEventsSinkAdapter;
import org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.ISimplePresentationAction;
import org.netbeans.modules.uml.ui.swing.drawingarea.SimplePresentationAction;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADAddAssociationClassEdgeTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateNodeState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADDiagramAddCommentTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADDiagramAddInterfaceTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADDiagramCreateMessageTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADInteractiveZoomState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADLinkNavigationState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADPanState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADZoomState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.CrossDiagramPasteTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.DiagramAddAssemblyConnectorTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.DiagramAddPartFacadeTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.NodeDecoratorTool;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * 
 * @author Trey Spiva
 */
public class ADCoreEngine extends DiagramEngine 
    implements IADCoreEngine, ActionListener, IETContextMenuHandler, 
        IAcceleratorListener, IDrawingAreaReconnectEdgeEventsSink
{
	Point m_AcceleratorOffset = new Point();
	public static String BOUNDARY_STEREO = DiagramEngineResources.getString("ADCoreEngine.boundary_1"); 
	public static String CONTROL_STEREO = DiagramEngineResources.getString("ADCoreEngine.controller_2"); 
	public static String ENTITY_STEREO = DiagramEngineResources.getString("ADCoreEngine.entity_3"); 
	public static int INT_MAX = 999;
        public static int FIND_DEPTH_ALL = TSFindChildParent.FIND_DEPTH_ALL;
	private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.Bundle"; 
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private IMenuManager m_ContextMenuManager = null;


	/// Variables for the graphical containment
	boolean m_bAllNodesInSameContainer = true;
	INodePresentation m_cpPreContainer = null;        
        private HashMap<IADContainerDrawEngine, ETList<IPresentationElement>> containedElements = 
                new HashMap < IADContainerDrawEngine,ETList <  IPresentationElement > >();

	//**************************************************
	// IADCoreEngine Implementaion
	//**************************************************

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.diagramegines.IADCoreEngine#IsCreateAttributeAccelerator(int)
	 */
	public boolean getIsCreateAttributeAccelerator(String accelerator)
	{
		return accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ATTRIBUTE);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.diagramegines.IADCoreEngine#IsCreateOperationAccelerator(int)
	 */
	public boolean getIsCreateOperationAccelerator(String accelerator)
	{
		return accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_OPERATION);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.diagramegines.IADCoreEngine#IsCreateParameterAccelerator(int)
	 */
	public boolean getIsCreateParameterAccelerator(String accelerator)
	{
		return accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_PARAMETER);
	}

	//**************************************************
	// ICoreDiagramEngine Implementation
	//**************************************************
	private DispatchHelper m_DispatchHelper = new DispatchHelper();
	private NodeEventListener m_NodeListener = new NodeEventListener();
	private EdgeEventListener m_EdgeListener = new EdgeEventListener();
	private ChangeNotificiationListener m_ChangeListener = new ChangeNotificiationListener();

	protected DispatchHelper getDispatchHelper() {
		return m_DispatchHelper;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#attach(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
	 */
	public void attach(IDrawingAreaControl pParentControl) {
		super.attach(pParentControl);

		// Initialize All our tools

		// Initialize Event Sinks
		m_DispatchHelper.registerDrawingAreaAddNodeEvents(m_NodeListener);
		m_DispatchHelper.registerDrawingAreaAddEdgeEvents(m_EdgeListener);
		m_DispatchHelper.registerDrawingAreaReconnectEdgeEvents( this );
		m_DispatchHelper.registerChangeNotificationTranslatorEvents(m_ChangeListener);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#detach()
	 */
	public void detach() {
		super.detach();

		// Deinitialize all of the tools

		// Revoke the event sinks
		m_DispatchHelper.revokeDrawingAreaAddNodeSink(m_NodeListener);
		m_DispatchHelper.revokeDrawingAreaAddEdgeSink(m_EdgeListener);
		m_DispatchHelper.revokeDrawingAreaReconnectEdgeSink( this );
		m_DispatchHelper.revokeChangeNotificationTranslatorSink(m_ChangeListener);
	}

   /**
    *
    * Helper for getting a preference
    * Returns the cached diagram preference
    *
    * @param sLocID[in] The path to the preference.
    * @param sID[in] The name of the preference.
    *
    * @return The preference value as a string.
    *
    */
   protected String getPreferenceValue( String sLocID, String sID)
   {
      String strValue = "";

      // Get the preference off the drawing area which can cache it up.
      IDrawingAreaControl control = getDrawingArea();
      if ( control != null )
      {
         strValue = control.getPreferenceValue( sLocID, sID);
      }

      return strValue;
   }
   
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#getContextMenuSorter()
	 */
	public IProductContextMenuSorter getContextMenuSorter() {
		return super.getContextMenuSorter();
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#enterMode(int)
    */
    public void enterMode(int nDrawingToolKind)
    {
        IDrawingAreaControl control = getDrawingArea();
        if (control != null)
        {
            TSEGraphWindow graph = control.getGraphWindow();
            if (graph != null)
            {
                switch (nDrawingToolKind)
                {
                    case IDrawingToolKind.DTK_SELECTION :
                        setGraphState(graph, graph.getDefaultState());
                        break;
                    case IDrawingToolKind.DTK_PAN :
                        setGraphState(graph, new ADPanState());
                        break;
                    case IDrawingToolKind.DTK_ACCORDION :
                        if (control.getReadOnly() == false)
                        {
                        }
                        break;
                    case IDrawingToolKind.DTK_ZOOM :
                        setGraphState(graph, new ADZoomState());
                        break;
                    case IDrawingToolKind.DTK_MOUSE_ZOOM :
                        setGraphState(graph, new ADInteractiveZoomState());
                        break;
                    case IDrawingToolKind.DTK_EDGENAV_MOUSE :
                        setGraphState(graph, new ADLinkNavigationState());
                        break;
                    default :
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#enterMode2(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void enterMode2(String sMode, String sFullInitString, String sTSViewString, String sGraphObjectObjectInitString)
    {
        IDiagram diagram = null;

        if (getDrawingArea() != null)
        {
            diagram = getDrawingArea().getDiagram();

            if (diagram != null)
            {
                if (sMode.equals("NODE_DECORATOR") == true)
                { 
                    if ((isParentDiagramReadOnly() == false) && (sGraphObjectObjectInitString.length() > 0))
                    {
                        IDrawingAreaControl control = getDrawingArea();
                        TSEGraphWindow graph = control.getGraphWindow();

                        setGraphState(graph, new NodeDecoratorTool(sGraphObjectObjectInitString));
                    }
                }
                else if (sMode.equals("NODE_RESIZE") == true)
                { 
                }
                else if (sMode.equals("ADD_NODE") == true)
                { 
                    addNodeUI(sGraphObjectObjectInitString);
                }
                else if (sMode.equals("ADD_EDGE") == true)
                { 
                    addEdgeUI(sGraphObjectObjectInitString);
                }

            }
        }
    }

   /*
    * Allows engines to override the tool that gets created.
    *
    * @param sButtonID The button string in the presentation types file.  A
    *                  lookup is performed to figure out the TS init string.
    * @return Set the true to tell the drawing area that we've handled
    *         the event
    */
    public boolean enterModeFromButton(String sButtonID)
    {
        boolean retVal = super.enterModeFromButton(sButtonID);
        
        if ((retVal == false) && (getDrawingArea() != null))
        {
            if ((sButtonID.equals("ID_VIEWNODE_UML_COMMENTLINK") == true) || 
                (sButtonID.equals("ID_VIEWNODE_UML_COMMENT") == true))
            {  //$NON-NLS-2$
                // Entering this tool allows user to create a comment and its
                // link at the same time
                enterAddCommentMode();
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_INTERFACE") == true))
            { 
                // This is used during the lollypop.  It creates a lollypop
                // interface and the associated edge.
                enterAddInterfaceTool();
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_ASSEMBLYCONNECTOR_INITIALEDGE") == true))
            { 
                // This is used on the component diagram when creating the assembly
                // connector.  It creates the assembly connector interface and edge.
                enterAddAssemblyConnectorTool();
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_PARTFACADELINK") == true) || 
                (sButtonID.equals("ID_VIEWNODE_UML_PARTFACADE") == true))
            {  //$NON-NLS-2$
                // Allows the user to single click to create the classifier
                // partfacade or drag out an edgeand create an edge and the
                // partfacade at the same time.
                enterAddPartFacadeMode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Classifier", 
                    "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade"); 
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_PARTFACADE_INTERFACE") == true))
            { 
                // Same as above, except for interfaces
                enterAddPartFacadeMode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Interface", "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade");  //$NON-NLS-2$
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_PARTFACADE_USECASE") == true))
            { 
                // Same as above, except for UseCase
                enterAddPartFacadeMode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade UseCase", "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade");  //$NON-NLS-2$
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_PARTFACADE_ACTOR") == true))
            { 
                // Same as above, except for Actor
                enterAddPartFacadeMode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Actor", "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade");  //$NON-NLS-2$
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_PARTFACADE_CLASS") == true))
            { 
                // Same as above, except for Class
                enterAddPartFacadeMode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Class", "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade");  //$NON-NLS-2$
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_MESSAGE_CREATE") == true))
            { 
                enterAddEdgeAndNodeMode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class", 
                    "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message create", 
                    "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class", 
                    false, 60, 20);
                retVal = true;
            }
            else if ((sButtonID.equals("ID_VIEWNODE_UML_ASSOCIATIONCLASS") == true))
            { 
                enterAddAssociationClassMode();
                retVal = true;
            }
        }
        
        return retVal;
    }

	/**
	 * Copy is about to happen.  Set bHandled to true to cancel normal handling
	 *
	 * @return Set to <code>true</code> to cancel the event
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preCopy()
	 */
	public boolean preCopy() {
		boolean retVal = false;

		if (getDrawingArea() != null) {
			ETList < IPresentationElement > elements = getDrawingArea().getSelected();
			if (elements != null) {
				Iterator < IPresentationElement > iter = elements.iterator();
				while (iter.hasNext()) {
					IPresentationElement element = iter.next();
					if (element instanceof IProductGraphPresentation) {
						IProductGraphPresentation graphP = (IProductGraphPresentation) element;
						graphP.selectAllLabels(true);
					}
				}
			}
		}

		return retVal; // Don't cancel.
	}

	/** 
	 * Right now our prelayout mechanism goes through the layouts and makes sure 
	 * none of them will resize the nodes from their prelayout sizes.
	 *
	 * @param nLayoutStyle The new layout style for this graph.
	 * @return Return <code>true</code> to cancel the layout.
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preDoLayout(int)
	 */
	public boolean preDoLayout(int nLayoutStyle) {
		boolean retVal = false;

		if (getDrawingArea() != null) {
			TSEGraph graph = getDrawingArea().getCurrentGraph();

			if (graph != null) {
				// We try to keep node sizes no matter what the layout mechanism.
				if (nLayoutStyle == ILayoutKind.LK_HIERARCHICAL_LAYOUT) {
                                    /* jyothi
					TSBooleanLayoutProperty property = new TSBooleanLayoutProperty(TSTailorProperties.HIERARCHICAL_CALCULATED_SIZES);
					property.setCurrentValue(false);                                 
					graph.setTailorProperty(property);
                                     */
				} else if (nLayoutStyle == ILayoutKind.LK_CIRCULAR_LAYOUT) {
				} else if (nLayoutStyle == ILayoutKind.LK_SYMMETRIC_LAYOUT) {
				} else if (nLayoutStyle == ILayoutKind.LK_TREE_LAYOUT) {
				} else if (nLayoutStyle == ILayoutKind.LK_ORTHOGONAL_LAYOUT) {
                                    /* jyothi
					TSBooleanLayoutProperty property = new TSBooleanLayoutProperty(TSTailorProperties.ORTHOGONAL_KEEP_NODE_SIZES);
					property.setCurrentValue(true);
					graph.setTailorProperty(property);
                                     */
				}
			}
		}

		return retVal;
	}

	/** 
	 * Goes through the list of accelerator types and registers each one found
	 * in the list
	 *
	 * @param accelsToRegister The accelerators this diagram engine 
	 *                         should register
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#registerAccelerators()
	 */
	public void registerAccelerators() {
		// TODO Auto-generated method stub
		super.registerAccelerators();
	}

	/**
	 * Register for the accelerators that the drawing area may be interested in.
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#revokeAccelerators()
	 */
	public void revokeAccelerators() {
		// TODO Auto-generated method stub
		super.revokeAccelerators();
	}


   /**
    * Called after an element has been added to the diagram, this routine specifically changes the 
    * dropped namespace or region as necessary.
    *
    * @param pPE [in] The presentation element to begin containment on
    */
   public void postAddObjectHandleContainment(IPresentationElement pPE)
   {
      if (isContainmentOK())
      {
         INodePresentation cpContainer = TypeConversions.getGraphicalContainer(pPE);

         // We dropped onto a containing object so tell the container about it.
         IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(cpContainer);

         IADContainerDrawEngine pActualContainer = (pDrawEngine instanceof IADContainerDrawEngine) ? (IADContainerDrawEngine)pDrawEngine : null;

         if (pActualContainer != null)
         {
            INodePresentation pNodePE = (pPE instanceof INodePresentation) ? (INodePresentation)pPE : null;

            if (pNodePE != null)
            {
               pActualContainer.beginContainment(null, pNodePE);
            }
         }
      }
   }

	/** 
	 * Called after an element has been added to the diagram.
	 * The default implementation is to determine if the new object is a node 
	 * being added on top of a container node.  If this is the case then behave 
	 * as if the node had been moved onto the container node.
	 *
	 * @param pGraphObject The object that was just created.
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postAddObject(com.tomsawyer.graph.TSGraphObject)
	 */
   public void postAddObject(TSGraphObject pGraphObject)
   {
      INodePresentation pPE = TypeConversions.getNodePresentation(pGraphObject);

      if (pPE != null)
      {
         postAddObjectHandleContainment(pPE);
	}
   }


   /**
    * Allows engine to handle delayed actions
    * Currently handles: SPAK_MOVEBEHINDCONTAINED
    */
   public boolean handleDelayedAction( IDelayedAction action )
   {
      if( null == action ) throw new IllegalArgumentException();
      
      boolean bHandled = false;

      if ( getDrawingArea() != null )
      {
         if (action instanceof ISimplePresentationAction)
         {
            ISimplePresentationAction simplePresentationAction = (ISimplePresentationAction)action;
            
            if ( DiagramAreaEnumerations.SPAK_MOVEBEHINDCONTAINED == simplePresentationAction.getKind() )
            {
               ETList< IPresentationElement > presentationElements = 
                  simplePresentationAction.getPresentationElements();
               if ( presentationElements != null )
               {
                  for (Iterator iter = presentationElements.iterator(); iter.hasNext();)
                  {
                     IPresentationElement presentationElement = (IPresentationElement)iter.next();
                     
                     IDrawEngine drawEngine = TypeConversions.getDrawEngine( presentationElement );
                     if (drawEngine instanceof IADContainerDrawEngine)
                     {
                        IADContainerDrawEngine engine = (IADContainerDrawEngine)drawEngine;
                        
                        ETList < IPresentationElement > containedPEs = engine.getContained();
                        if( containedPEs != null )
                        {
                            getDrawingArea().executeStackingCommand( containedPEs, IDrawingAreaControl.SOK_MOVETOFRONT, true );
                        }
                        else
                        {
                           // This case happens when we are creating an SQD, and
                           // the user wants the interaction boundary to be displayed.
                            getDrawingArea().executeStackingCommand( presentationElement, IDrawingAreaControl.SOK_MOVETOBACK, true );
                        }

                        getDrawingArea().setIsDirty(true);
                        getDrawingArea().refresh( false );
                     }
                  }
               }

               bHandled = true;  // We tried to handle it
            }
         }
      }
      
      return bHandled;
   }


	/**
	 * Preresize event from the drawing area
	 * 
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPreResizeObjects(com.tomsawyer.graph.TSGraphObject)
	 */
	public boolean onPreResizeObjects(TSGraphObject graphObject) {
		// TODO Auto-generated method stub
		return super.onPreResizeObjects(graphObject);
	}

	/**
	 * Fired before the control is scrolled and/or zoomed
	 * 
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPreScrollZoom(double, double, double)
	 */
	public boolean onPreScrollZoom(double pageCenterX, double pageCenterY, double zoomLevel) {
		m_AcceleratorOffset.x = 0;
		m_AcceleratorOffset.y = 0;

		return true;
	}

	/** 
	 * Called after objects have been dropped onto the diagram perform 
	 * relationship discovery.
	 *
	 * @param pMEs The elements that just got dropped.
	 * @param bAutoRouteEdges Should we autoroute the edges that get created during relationship 
	 *                        discovery
	 * 
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postOnDrop(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], boolean)
	 */
	public void postOnDrop(ETList<IElement> pMEs, boolean bAutoRouteEdges) {
		if ((getDrawingArea() != null) && (pMEs != null)) {
			ETList < IElement > elementsOnDiagram = getDrawingArea().getAllItems3();
			
			ICoreRelationshipDiscovery pDiscovery = getRelationshipDiscovery();
			if (pDiscovery != null) {
				if (elementsOnDiagram != null && elementsOnDiagram.size() > 0){
					pDiscovery.discoverCommonRelations(bAutoRouteEdges, pMEs, elementsOnDiagram);
				}					
				else{			
					pDiscovery.discoverCommonRelations(bAutoRouteEdges, pMEs);
				}				
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#getRelationshipDiscovery()
	 */
	public ICoreRelationshipDiscovery getRelationshipDiscovery() {
		ADRelationshipDiscovery retVal = new ADRelationshipDiscovery();
		retVal.setParentDrawingArea(getDrawingArea());
		return retVal;
	}

	public final static String DELETE_ACTION = "DELETE_SELECTED"; 

	public void setQuickKeys(TSEGraphWindow pGraphEditor) {
		//pGraphEditor.registerKeyboardAction(this, DELETE_ACTION, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void actionPerformed(ActionEvent e) {
//		if (e.getActionCommand().equals(DELETE_ACTION)) {
//			getDrawingArea().deleteSelected(true);
//		}
	}

    /**
     * Test if the user wants to delete the data behind the presenation.
     *
     * @return <code>true</code> if the user wants to delete the data associted
     *         with the presentation elements.
     */
    public DataVerificationResults verifyDataDeletion(
        ETList<TSENode> selectedNodes,
        ETList<TSEEdge> selectedEdges,
        ETList<TSENodeLabel> selectedNodeLabels,
        ETList<TSEEdgeLabel> selectedEdgeLabels)
    {
        DataVerificationResults retVal = new DataVerificationResults();
        
        // Do not display the questionDialog when deleting labels
        if ((selectedNodes != null && selectedNodes.size() > 0) || 
            (selectedEdges != null && selectedEdges.size() > 0))
        {
            IQuestionDialog questionDialog = UIFactory.createQuestionDialog();
            
            String title = DiagramEngineResources.getString(
                "ADCoreEngine.DELETE_QUESTIONDIALOGTITLE"); // NO18N
            
            String question = DiagramEngineResources.getString(
                "ADCoreEngine.DELETE_GRAPH_OBJECTS_MESSAGE"); // NO18N
            
            String checkQuestion = DiagramEngineResources.getString(
                "ADCoreEngine.DELETE_ELEMENTS_QUESTION"); // NO18N
            
            QuestionResponse result =
                questionDialog.displaySimpleQuestionDialogWithCheckbox(
                MessageDialogKindEnum.SQDK_YESNO,
                MessageIconKindEnum.EDIK_ICONWARNING,
                question,
                checkQuestion,
                title,
                MessageResultKindEnum.SQDRK_RESULT_NO,
                false);
            
            retVal.setCancelAction(result.getResult() ==
                MessageResultKindEnum.SQDRK_RESULT_NO ||
                result.getResult() == 
                    MessageResultKindEnum.SQDRK_RESULT_CANCEL);
            
            if (retVal.isCancelAction() == false)
            {
                // cvc - 6311930
                // for some reason, the param was hardcode as "false"
                // I reverted back to passing the actual value of the checkbox
                retVal.setAffectModelElement(result.isChecked());
            }
            
            else
                retVal.setAffectModelElement(false);
        }
        
        else
            retVal.setAffectModelElement(false);
        
        return retVal;
    }
   
	//**************************************************
	// Helper Methods
	//**************************************************

	/**
	 * @param sGraphObjectObjectInitString
	 */
	protected void addEdgeUI(String edgeUI) {
		ETGenericEdgeUI ui = null;
		String type = edgeUI;

		IDrawingAreaControl control = getDrawingArea();
		TSEGraphWindow graph = control.getGraphWindow();

		if ((control != null) && (graph != null)) {
			IPresentationTypesMgr mgr = control.getPresentationTypesMgr();
			PresentationTypeDetails details = mgr.getInitStringDetails(edgeUI, control.getDiagramKind());

			int delimiter = edgeUI.indexOf(' ');
			String edgeUIClass = edgeUI.substring(0, delimiter);
			String drawEngineClass = details.getEngineName();

			if ((edgeUIClass != null) && (edgeUIClass.length() > 0) && (drawEngineClass != null) && (drawEngineClass.length() > 0)) {
				try {
					ui = ETUIFactory.createEdgeUI(edgeUIClass, edgeUI, drawEngineClass, control);
//					ui.setDrawEngineClass(drawEngineClass);
//					ui.setDrawingArea(control);
				} catch (ETException e) {
					//            this.showErrorMessage(e.getMessage());
				}

				// add user specified properties
				this.setUIAttributes(type, ui);

				if (ui != null) {
					graph.setCurrentEdgeUI(ui);
				}

				graph.setEdgeCreatedWithLabel("true".equals(getDrawingArea().getResources().getStringResource(type + ".labeled")));  //$NON-NLS-2$
			}

			String edgeType = getDrawingArea().getResources().getStringResource("edgeType." + type.substring(type.indexOf('.') + 1)); 

			int intType = TSEdge.TYPE;

			if (edgeType != null) {
				try {
					intType = Integer.parseInt(edgeType);
				} catch (Exception e) {
					intType = TSEdge.TYPE;
				}
			}

			// switch to create edge state
			if (intType == TSEdge.TYPE) {
				setGraphState(graph, new ADCreateEdgeState());
			} else {
				setGraphState(graph, new ADCreateEdgeState(intType));
			}
		}

	}

	/**
	 * @param sGraphObjectObjectInitString
	 */
	protected void addNodeUI(String nodeUIName) {
		ETGenericNodeUI ui = null;
		String type = nodeUIName;

		IDrawingAreaControl control = getDrawingArea();
		TSEGraphWindow graph = control.getGraphWindow();

		if ((control != null) && (graph != null)) {
			IPresentationTypesMgr mgr = control.getPresentationTypesMgr();
			PresentationTypeDetails details = mgr.getInitStringDetails(nodeUIName, control.getDiagramKind());

			int delimiter = type.indexOf(' ');
			String nodeUIClass = type.substring(0, delimiter);
			String drawEngineClass = details.getEngineName();

			if ((nodeUIClass != null) && (nodeUIClass.length() > 0) && (drawEngineClass != null) && (drawEngineClass.length() > 0)) {
				try {
					ui = ETUIFactory.createNodeUI(nodeUIClass, nodeUIName, drawEngineClass, control);
				} catch (ETException e) {
					//this.showErrorMessage(e.getMessage());
				}

				//         Cursor oldCursor = getDrawingArea().getCursor();

				try {
					// add user specified properties
					setUIAttributes(type, ui);

				} catch (Throwable t) {
					t.printStackTrace();
					//            this.setCursor(oldCursor);
				}

				if (ui != null) {
					graph.setCurrentNodeUI(ui);
				}
			} else {
				TSENodeUI currentNodeUI = graph.getCurrentNodeUI();

				if (currentNodeUI instanceof TSEDefaultNodeUI) {
					addNodeUI(ADDrawingAreaConstants.DEFAULT_NODE_UI);
				}
			}

			setGraphState(graph, new ADCreateNodeState());
		}
	}

	/**
	 * 
	 */
	protected void enterAddAssociationClassMode() {
		try {
			IDrawingAreaControl control = getDrawingArea();

			if (control != null) {
				TSEGraphWindow pGraphEditor = control.getGraphWindow();
				if (pGraphEditor != null) {
					control.setEdgeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationClassInitialEdge");
					ADAddAssociationClassEdgeTool pTool = new ADAddAssociationClassEdgeTool(pGraphEditor);
					if (pTool != null && control.getDiagram() != null) {
						pTool.setParentDiagram(control.getDiagram());
						pTool.setViewDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationClassInitialEdge");

						pTool.setCreateBends(false);
						setGraphState(pGraphEditor, pTool);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param sNodeDescription
	 * @param sEdgeDescription
	 * @param sSingleClickNodeDescription
	 * @param bDrawEllipseEndNode
	 * @param nEndNodeWidth
	 * @param nEndNodeHeight
	 */
	protected void enterAddEdgeAndNodeMode(String sNodeDescription, String sEdgeDescription, String sSingleClickNodeDescription, boolean bDrawEllipseEndNode, int nEndNodeWidth, int nEndNodeHeight) {

		if (sNodeDescription == null || sEdgeDescription == null)
			return;

		IDrawingAreaControl control = getDrawingArea();

		if ((control != null) && (control.getGraphWindow() != null)) {
			TSEGraphWindow window = control.getGraphWindow();

			control.setNodeDescription(sNodeDescription);
			control.setEdgeDescription(sEdgeDescription);

			ADDiagramCreateMessageTool pDiagramAddCommentTool = new ADDiagramCreateMessageTool();
			pDiagramAddCommentTool.setSingleClickNodeDescription(sSingleClickNodeDescription);

			//         if (nEndNodeWidth > 0 && nEndNodeHeight > 0)
			//         {
			//            if (bDrawEllipseEndNode)
			//               pDiagramAddCommentTool.drawEndNodeAsEllipse(nEndNodeWidth, nEndNodeHeight);
			//            else
			//               pDiagramAddCommentTool.drawEndNodeAsRectangle(nEndNodeWidth, nEndNodeHeight);
			//         }

			//window.switchState(pDiagramAddCommentTool);
                        window.switchTool(pDiagramAddCommentTool);
		}
	}

	/**
	 * @param string
	 * @param string2
	 */
	protected void enterAddPartFacadeMode(String string, String string2) {
		IDrawingAreaControl control = getDrawingArea();

		if ((control != null) && (control.getGraphWindow() != null)) {
			TSEGraphWindow window = control.getGraphWindow();

			control.setNodeDescription(string);
			control.setEdgeDescription(string2);

			DiagramAddPartFacadeTool pDiagramAddPartFacadeTool = new DiagramAddPartFacadeTool();
			pDiagramAddPartFacadeTool.setSingleClickNodeDescription(string);
			//pDiagramAddPartFacadeTool.drawEndNodeAsRectangle(24, 16);

			//window.switchState(pDiagramAddPartFacadeTool);
                        window.switchTool(pDiagramAddPartFacadeTool);
		}

	}

	/**
	 * 
	 */
	protected void enterAddAssemblyConnectorTool() {
		IDrawingAreaControl control = getDrawingArea();

		if ((control != null) && (control.getGraphWindow() != null)) {
			TSEGraphWindow window = control.getGraphWindow();

			control.setNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation");
			control.setEdgeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Usage");

			DiagramAddAssemblyConnectorTool pDiagramAddAssemblyConnectorTool = new DiagramAddAssemblyConnectorTool();
			pDiagramAddAssemblyConnectorTool.setSingleClickNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation");
			//			pDiagramAddInterfaceTool.drawEndNodeAsEllipse(16, 16);

			//window.switchState(pDiagramAddAssemblyConnectorTool);
                        window.switchTool(pDiagramAddAssemblyConnectorTool);
		}
	}

	/**
	 * 
	 */
	protected void enterAddInterfaceTool() {
		IDrawingAreaControl control = getDrawingArea();

		if ((control != null) && (control.getGraphWindow() != null)) {
			TSEGraphWindow window = control.getGraphWindow();

			control.setNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation");
			control.setEdgeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation");

			ADDiagramAddInterfaceTool pDiagramAddInterfaceTool = new ADDiagramAddInterfaceTool();
			pDiagramAddInterfaceTool.setSingleClickNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation");
			//			pDiagramAddInterfaceTool.drawEndNodeAsEllipse(16, 16);

			//window.switchState(pDiagramAddInterfaceTool);
                        window.switchTool(pDiagramAddInterfaceTool);
		}
	}

	/**
	 * Enters us into the CADDiagramAddCommentTool mode.  Helper Function for 
	 * EnterModeFromButton.
	 */
	protected void enterAddCommentMode() {
		IDrawingAreaControl control = getDrawingArea();

		if ((control != null) && (control.getGraphWindow() != null)) {
			TSEGraphWindow window = control.getGraphWindow();

			control.setNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Comment"); 
			control.setEdgeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge CommentEdge"); 

			ADDiagramAddCommentTool pDiagramAddCommentTool = new ADDiagramAddCommentTool();
			pDiagramAddCommentTool.setSingleClickNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Comment"); 
			pDiagramAddCommentTool.drawEndNodeAsRectangle(24, 16);

			//window.switchState(pDiagramAddCommentTool);
                        window.switchTool(pDiagramAddCommentTool);
		}
	}

	/**
	 * This method sets all user specified properties for the
	 * node or edge UI specified by the user.
	 */
	protected void setUIAttributes(String type, TSEObjectUI ui) 
	{
/*
 
		The following code is not in the C++.  Also in the 
		for (int attr = 1;; ++attr) {
			String name = getDrawingArea().getResources().getStringResource(type + DiagramEngineResources.getString("ADCoreEngine..property._47") + attr + ".name");  //$NON-NLS-2$
			String value = getDrawingArea().getResources().getStringResource(type + DiagramEngineResources.getString("ADCoreEngine..property._49") + attr + ".value");  //$NON-NLS-2$
			if (name == null || value == null) {
				return;
			}
			ui.setProperty(new TSProperty(name, value));
		}
*/		
	}

	/**
	 * @param graph
	 * @param m_CreateEdgeState
	 */
//	protected void setGraphState(TSEGraphWindow graph, TSEWindowState state) {
	protected void setGraphState(TSEGraphWindow graph, TSEWindowTool tool) {
		if (graph != null)
			//graph.switchState(state);
                    graph.switchTool(tool);
	}

	/**
	 * Handles the finishing of Nested Links
	 * 
	 * @param context The edge finish context
	 */
	protected void handleNestedLinkFinish(IEdgeFinishContext context) {
		if (context != null) {
			INamedElement startingElement = null;
			if (context.getStartNodeModelElement() instanceof INamedElement) {
				startingElement = (INamedElement) context.getStartNodeModelElement();
			}

			INamespace finishNamespace = null;
			if (context.getFinishNodeModelElement() instanceof INamespace) {
				finishNamespace = (INamespace) context.getFinishNodeModelElement();
			}

			if (startingElement != null & finishNamespace != null) {
				// Special case for the nested link symbol
				String viewDescription = context.getViewDescription();
				if (viewDescription.equals("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge NestedLink")) { 
					boolean cancel = true;
					try {
						// Make sure we don't nest a reflexive node
						if (context.getStartNode() != context.getFinishNode() && !startingElement.isSame(finishNamespace))
						{
							if(Util.hasNameCollision(finishNamespace, startingElement.getName(), 
									startingElement.getElementType(), startingElement))
							{
							   DialogDisplayer.getDefault().notify(
									new NotifyDescriptor.Message(NbBundle.getMessage(
										DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
							   return;
							}
							startingElement.setNamespace(finishNamespace);

							// Verify that the change took place.  If the change took 
							// place then do not cancel the event.
							INamespace curNamespace = startingElement.getNamespace();
							if ((curNamespace != null) && (curNamespace.isSame(finishNamespace) == true)) {
							
								getDrawingArea().setModelElement(finishNamespace);
	/*							
								TSEGraphWindow graphWindow = getDrawingArea().getGraphWindow();
								if (graphWindow.getCurrentState() instanceof ADCreateEdgeState)
								{
									ADCreateEdgeState edgeState = (ADCreateEdgeState)graphWindow.getCurrentState();
									edgeState.
								}
	*/							
								cancel = false;
							}
						}
 					} catch (Exception e) {
						// If an exception occurs I just want to cancel the process.
						cancel = true;
					} finally {
						context.setCancel(cancel);
					}
				}
			}
		}
	}
   
	protected void handlePartFacadeFinish(IEdgeFinishContext context) {
		if (context != null) {
			INamedElement startingElement = null;
			if (context.getStartNodeModelElement() instanceof INamedElement) {
				startingElement = (INamedElement) context.getStartNodeModelElement();
			}

			INamespace finishNamespace = null;
			if (context.getFinishNodeModelElement() instanceof INamespace) {
				finishNamespace = (INamespace) context.getFinishNodeModelElement();
			}

			if ((startingElement != null) && (finishNamespace != null)) {
				// Special case for the nested link symbol
				String viewDescription = context.getViewDescription();
				if (viewDescription.equals("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade")) { 
					boolean cancel = true;

					ICollaboration collaboration = null;
					if (startingElement instanceof ICollaboration) {
						collaboration = (ICollaboration) startingElement;
					}

					IPartFacade partFacade = null;
					if (finishNamespace instanceof IPartFacade) {
						partFacade = (IPartFacade) finishNamespace;
					}

					if ((collaboration != null) && (partFacade != null)) {
						try {
							partFacade.setNamespace(collaboration);

						} catch (Exception e) {
							// If an exception occurs I just want to cancel the process.
							cancel = true;
						} finally {
							// Verify that the change took place.  If the change took 
							// place then do not cancel the event.
							INamespace curNamespace = partFacade.getNamespace();
							if ((curNamespace != null) && (curNamespace.isSame(collaboration) == true)) {
								cancel = false;
							}

							context.setCancel(cancel);
						}
					}
				}
			}
		}
	}

	/**
	 * Notifies the parent as well as the child of the change
	 *
	 * @param pTargets Information about the event.  Add presentation 
	 *                 elements to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pNamespace The namespace that was modified
	 */
	protected void respondToElementAddedToNamespace(INotificationTargets pTargets, IDiagram diagram, INamespace namespace) {
		if ((namespace != null) && (pTargets != null)) {
			pTargets.addElementsPresentationElements(diagram, namespace);

			IElement secondaryElement = pTargets.getSecondaryChangedModelElement();
			if (secondaryElement != null) {
				ETList < IPresentationElement > secondaryPEs = getDrawingArea().getAllItems2(secondaryElement);
				if (secondaryPEs != null) {
					pTargets.addNotifiedElements(secondaryPEs);
				}

				for (Iterator < IPresentationElement > iter = secondaryPEs.iterator(); iter.hasNext();) {
					IPresentationElement element = iter.next();
					if (element instanceof INodePresentation) {
						INodePresentation nodePE = (INodePresentation) element;
						ETList < IPresentationElement > edgePEs = nodePE.getEdgesWithEndPoint(true, true, nodePE);

						if (edgePEs != null) {
							pTargets.addNotifiedElements(edgePEs);
						}
					}
				}
			}
		}
	}

	/**
	 * Get all the elements of this namespace and update those presentation 
	 * elements
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pNamespace The namespace that was modified
	 */
	protected void respondToNameModified(INotificationTargets pTargets, IDiagram diagram, INamespace namespace) {
		if ((namespace != null) && (pTargets != null)) {
			ETList < INamedElement > namedElements = namespace.getOwnedElements();
			if (namedElements != null) {
				for (Iterator < INamedElement > iter = namedElements.iterator(); iter.hasNext();) {
					pTargets.addElementsPresentationElements(diagram, iter.next());
				}
			}
		}
	}

	/**
	 * Notifies the lifelines on collaboration and sequence diagrams
	 *
	 * @param pTargets Information about the event.  Add presentation 
	 *                 elements to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pChangedME The element that was modified
	 */
	protected void notifyLifelines(INotificationTargets pTargets, IDiagram diagram, IElement changedME) {
		if ((changedME != null) && (pTargets != null)) {
			IClassifier classifier = null;
			if ((changedME instanceof IClassifier) || (changedME instanceof IPart)) {
				ETList < IPresentationElement > elements = getDrawingArea().getAllByType(DiagramEngineResources.getString("ADCoreEngine.Lifeline_53")); 
				pTargets.addNotifiedElements(elements);
			}
		}
	}

	/**
	 * Notifies the design patterns that are connected to the role that changed
	 *
	 * @param pTargets[in] Information about the event.  Add presentation elements to be notified through
	 * this interface.
	 * @param pDiagram[in] The diagram we're associated with
	 * @param pConnectableElement [in] The role that was modified
	 */
	protected void notifyDesignPatterns(INotificationTargets pTargets, IDiagram diagram, IConnectableElement connect) {
		if ((connect != null) && (pTargets != null)) {
			// Find all the roles this guy plays a part in and notify the
			// contexts - these contexts should be the collaborations.
			ETList < IStructuredClassifier > classifiers = connect.getRoleContexts();
			if (classifiers != null) {
				for (Iterator < IStructuredClassifier > iter = classifiers.iterator(); iter.hasNext();) {
					IStructuredClassifier curClassifier = iter.next();
					if (curClassifier != null) {
						pTargets.addElementsPresentationElements(diagram, curClassifier);
					}
				}
			}
		}
	}

	/**
	 * Notifies the parameterable elements owner (ie the template class)
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pParameterableElement The IParameterableElement that was modified.
	 */
	protected void notifyParameterableElementOwner(INotificationTargets pTargets, IDiagram diagram, IParameterableElement parameter) {
		if ((pTargets != null) && (parameter != null)) {
			// Get the owner of the parameterable element and see if that guy 
			// pParameterableElement in its list of parameterable elements.
			IElement changedME = pTargets.getChangedModelElement();

			// Get the owner of the tagged value and see if that has presentation 
			// elements to update
			IElement owner = changedME.getOwner();

			if (owner instanceof IClassifier) {
				IClassifier classifier = (IClassifier) owner;
				ETList < IParameterableElement > elements = classifier.getTemplateParameters();
				if (elements != null) {
					if (elements.contains(parameter) == true) {
						pTargets.addElementsPresentationElements(diagram, classifier);
					}
				}
			}
		}
	}

	/**
	 * @param pTargets
	 * @param diagram
	 * @param transition
	 */
	protected void notifyTransitionSourceAndTarget(INotificationTargets pTargets, IDiagram diagram, ITransition transition) {
		if ((pTargets != null) && (transition != null)) {
			if (transition.getSource() != null) {
				pTargets.addElementsPresentationElements(diagram, transition.getSource());
			}

			if (transition.getTarget() != null) {
				pTargets.addElementsPresentationElements(diagram, transition.getTarget());
			}
		}
	}

	/**
	 * Notifies the owning interation.  Used on the sequence diagram
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pInteraction The IInteraction that was modified
	 */
	protected void notifyInteration(INotificationTargets pTargets, IDiagram diagram, IInteraction interaction) {
		if ((pTargets != null) && (interaction != null)) {
			ETList < IPresentationElement > elements = getDrawingArea().getAllByType("InteractionOccurrence"); 
			if (elements != null) {
				pTargets.addNotifiedElements(elements);
			}
		}
	}

	/**
	 * Notifies the owning expression.  Used on the sequence diagram
	 *
	 * @param pTargets Information about the event.  Add presentation 
	 *                 elements to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pInteractionConstraint The IInteractionConstraint that was modified
	 */
	protected void notifyExpressions(INotificationTargets pTargets, IDiagram diagram, IInteractionConstraint constraint) {
		if ((pTargets != null) && (constraint != null)) {
			IValueSpecification spec = constraint.getSpecification();
			if (spec instanceof IExpression) {
				IExpression expression = (IExpression) spec;

				// Must call GetAllItems, not get the presentation elements off the 
				// ME because the PE's could be across diagrams.
				pTargets.addElementsPresentationElements(diagram, expression);
			}
		}
	}

	/**
	 * @param pTargets
	 * @param diagram
	 * @param procedure
	 */
	protected void notifyState(INotificationTargets pTargets, IDiagram diagram, IProcedure procedure) {
		if ((pTargets != null) && (procedure != null)) {

			IState parent = OwnerRetriever.getOwnerByType(procedure, IState.class);
			pTargets.addElementsPresentationElements(diagram, parent);
		}
	}

	/**
	 * @param pTargets
	 * @param diagram
	 * @param occurence
	 */
	protected void notifyMessages(INotificationTargets pTargets, IDiagram diagram, IProcedureOccurrence occurence) {
		if ((pTargets != null) && (occurence != null)) {
			IEventOccurrence eventOccurence = occurence.getFinish();
			if (eventOccurence != null) {
				IMessage message = eventOccurence.getReceiveMessage();
				if (message != null) {
					// Must call GetAllItems, not get the presentation elements off 
					// the ME because the PE's could be across diagrams.
					pTargets.addElementsPresentationElements(diagram, message);
				}
			}
		}
	}

	/**
	 * Get the parent multiplicity and notify any presentation elements
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pChangedRange The multiplicity range that was modified
	 */
	protected void notifyParentMultiplicity(INotificationTargets pTargets, IDiagram diagram, IMultiplicityRange range) {
		if ((pTargets != null) && (range != null)) {
			IMultiplicity multiplicity = range.getParentMultiplicity();
			if (multiplicity != null) {
				IElement element = multiplicity.getOwner();
				if (element instanceof IFeature) {
					// owner's a feature, run through this again
					// recurse using the owner as the changed modelelement
					IFeature feature = (IFeature) element;
					//element = pTargets.getChangedModelElement();
					pTargets.setChangedModelElement(element);
					getNotificationTargets(pTargets);
				} else if (element instanceof IParameter) {
					IParameter parameter = (IParameter) element;

					// Backup to the operation and notify that guy  
					IElement paramOwner = parameter.getOwner();
					if (paramOwner != null) {
						pTargets.setChangedModelElement(paramOwner);
						getNotificationTargets(pTargets);
					}
				} else {
					// Must call GetAllItems, not get the presentation elements off 
					// the ME because the PE's could be across diagrams.
					pTargets.addElementsPresentationElements(diagram, element);
				}
			}
		}
	}

	/**
	 * Go up the tree and notify the owner.
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pElement The element that has changed.  Need to notify the owner
	 */
	protected void notifyOwner(INotificationTargets pTargets, IDiagram diagram, IElement element)
        {
            if ((pTargets != null) && (element != null))
            {
                IElement owner = element.getOwner();
                if (owner instanceof IFeature)
                {
                    IFeature feature = (IFeature) owner;
                    
                    // owner's a feature, run through this again
                    // recurse using the owner as the changed modelelement
                    pTargets.setChangedModelElement(feature);
                    getNotificationTargets(pTargets);
                }
                else if (owner instanceof IParameter)
                {
                    IParameter parameter = (IParameter) owner;
                    
                    if (parameter.getOwner() != null)
                    {
                        // Backup to the operation and notify that guy
                        pTargets.setChangedModelElement(parameter.getOwner());
                        getNotificationTargets(pTargets);
                    }
                }
                
//                        // Since this is for the owner, I do
//                        else if(owner instanceof IDerivation)
//                        {
//                            pTargets.addElementsPresentationElements(diagram, element);
//                        }
                else
                {
                    pTargets.addElementsPresentationElements(diagram, owner);
                }
            }
        }

	/**
	 * When an enumeration literal changes we backup and notify the enumeration 
	 * that owns it
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pEnumerationLiteral The enumeration literal that has changed.  
	 *                            Need to notify the enumeration
	 */
	protected void notifyEnumerationLiteralsEnumeration(INotificationTargets pTargets, IDiagram diagram, IEnumerationLiteral literal) {
		if ((pTargets != null) && (literal != null)) {
			IEnumeration enumeration = literal.getEnumeration();
			if (enumeration != null) {
				// Must call GetAllItems, not get the presentation elements off the 
				// ME because the PE's could be across diagrams.
				pTargets.addElementsPresentationElements(diagram, enumeration);

				// The changed element is an enumeration literal.  We need to 
				// switch the enum literal and model element in the targets, so 
				// that the model element is the enumeration and the secondary 
				// element is this enumeration literal.
				pTargets.setChangedModelElement(enumeration);
				pTargets.setSecondaryChangedModelElement(literal);
			}
		}
	}

	/**
	 * When a feature changes we backup and notify the feature's classifier or 
	 * association end
	 *
	 * @param pTargets Information about the event.  Add presentation 
	 *                 elements to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pFeature The feature end that has changed.
	 */
	protected void notifyFeaturesClassifierOrAssocEnd(INotificationTargets pTargets, IDiagram diagram, IFeature feature) {
		if ((pTargets != null) && (feature != null)) {
			// ModelElement is a feature, get its classifier
			IClassifier classifier = feature.getFeaturingClassifier();
			if (classifier != null) {
				// Must call GetAllItems, not get the presentation elements off the ME because
				// the PE's could be across diagrams.
				pTargets.addElementsPresentationElements(diagram, classifier);

				// The changed element is a feature.  We need to switch the feature and
				// model element in the targets, so that the model element is the classifier
				// and the feature is this feature.
				pTargets.setChangedModelElement(classifier);
				pTargets.setSecondaryChangedModelElement(feature);
			} else {
				if (feature instanceof IAttribute) {
					IAttribute attribute = (IAttribute) feature;
					IAssociationEnd end = attribute.getAssociationEnd();
					if (end != null) {
						// Must call GetAllItems, not get the presentation elements off the ME because
						// the PE's could be across diagrams.
						pTargets.addElementsPresentationElements(diagram, end);

						// The changed element is a feature.  We need to switch the feature and
						// model element in the targets, so that the model element is the classifier
						// and the feature is this feature.
						pTargets.setChangedModelElement(end);
						pTargets.setSecondaryChangedModelElement(feature);
					}
				}
			}
		}
	}

	/**
	 * When an association end changes this routine find the association and 
	 * makes sure that association is notified.
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pEnd The association end that has changed.  Need to notify the 
	 *             association
	 */
	protected void notifyAssocations(INotificationTargets pTargets, IDiagram diagram, IAssociationEnd end) {
		if ((pTargets != null) && (end != null)) {
			IAssociation assoc = end.getAssociation();
			if (assoc != null) {
				pTargets.addElementsPresentationElements(diagram, end);
			}
		}
	}

	/**
	 * Go up the tree and notify the first owner we find that has a presentation 
	 * element.  Used for tagged values especially.
	 *
	 * @param pTargets Information about the event.  Add presentation 
	 *                 elements to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 * @param pElement The element we're associated with
	 */
   protected void notifyFirstOwnerWithPresentationElements(INotificationTargets pTargets, IDiagram diagram, IElement element)
   {
      if (pTargets != null)
      {
         IElement changedME = pTargets.getChangedModelElement();
         if (changedME != null)
         {
            // Get the owner of the tagged value and see if that has 
            // presentation elements to update
            IElement owner = changedME.getOwner();

            // Tagged values can be associated with lots of things, here we try 
            // to find the owner that has a presentation element.
            while (owner != null)
            {
               ETList < IPresentationElement > elements = getDrawingArea().getAllItems2( owner );
               if (elements != null)
               {
                  if (elements.size() > 0)
                  {
                     break;
                  }
               }

               owner = owner.getOwner();
            }

            pTargets.addElementsPresentationElements(diagram, owner);
         }
      }
   }
   
   /**
    * Returns true if this listener is attached to the currently active diagram
    */
   protected boolean weAreActiveDiagram()
   {
      boolean bWeAreActive = false;

      IDiagram currentDiagram = null;
      {
         IProductDiagramManager diagramManager = ProductHelper.getProductDiagramManager();
         if ( diagramManager != null )
         {
            currentDiagram = diagramManager.getCurrentDiagram();
         }
      }

      IDrawingAreaControl control = getDrawingArea();
      if ( (control != null) &&
           (currentDiagram != null) )
      {
         boolean bIsSame = false;

         bWeAreActive = control.isSame( currentDiagram );
      }

      return bWeAreActive;
   }

	/**
	 * Populate the targets with presentation elements that should get notified 
	 * of the deletion
	 *
	 * @param pTargets Information about the event.  Add presentation elements 
	 *                 to be notified through this interface.
	 * @param pDiagram The diagram we're associated with
	 */
   protected void populateElementDeletedTargets(INotificationTargets pTargets, IDiagram pDiagram)
   {
      if (pTargets != null)
      {
         IElement changedME = pTargets.getChangedModelElement();
         IElement secondaryChangeME = pTargets.getSecondaryChangedModelElement();

         if (changedME instanceof IFeature)
         {
            IFeature feature = (IFeature)changedME;

            // We may have an attribute of a qualifier, in which case the 
            // parent is not a featuring classifier, it's an IAssociationEnd.  
            // So rather then calling get_FeaturingClassifer we get the owner 
            // and use that.
            IElement owner = feature.getOwner();
            if (owner != null)
            {
               pTargets.addElementsPresentationElements(pDiagram, owner);

               // Fix J1427:  This was coded incorrectly from the C++ version
               //             I (BDB) had to add the not operator in the if statement's expression.

               // at this point if we have a classifier, feature and some 
               // presentation elements swap the variables around so the call
               // to ModelElementDeleted() below gets the proper params in the 
               //proper order
               if (!(secondaryChangeME instanceof IFeature))
               {
                  pTargets.setChangedModelElement(owner);
                  pTargets.setSecondaryChangedModelElement(feature);
               }
            }
         }
         else if (changedME instanceof IEnumerationLiteral)
         {
            // ModelElement is an enumeration literal, get its enumeration 
            // classifier
            IEnumerationLiteral literal = (IEnumerationLiteral)changedME;
            IEnumeration enumeration = literal.getEnumeration();
            if (enumeration != null)
            {
               // Must call GetAllItems, not get the presentation elements off 
               // the ME because the PE's could be across diagrams.
               pTargets.addElementsPresentationElements(pDiagram, enumeration);

               // The changed element is an enumeration literal.  We need to 
               // switch the enum literal and model element in the targets, so 
               // that the model element is the enumeration and the secondary 
               // element is this enumeration literal.
               pTargets.setChangedModelElement(enumeration);
               pTargets.setSecondaryChangedModelElement(literal);
            }
         }
         else if (changedME instanceof ITaggedValue)
         {
            ITaggedValue taggedValue = (ITaggedValue)changedME;

            // The model element is a tagged value, get its owner as long as 
            // it's not a hidden tagged value
            if (taggedValue.isHidden() == false)
            {
               IElement owner = changedME.getOwner();
               if (owner != null)
               {
                  notifyFirstOwnerWithPresentationElements(pTargets, pDiagram, taggedValue);
                  pTargets.setChangedModelElement(taggedValue);
               }
            }
         }
         else if ( (changedME instanceof IActivityPartition) ||
                   (changedME instanceof IInteractionOperand) ||
                   (changedME instanceof IRegion) )
         {
            IElement owner = changedME.getOwner();
            if (owner != null)
            {
               pTargets.addElementsPresentationElements(pDiagram, owner);
            }
         }
         else if (changedME instanceof IConnectableElement)
         {
            // Find all the roles this guy plays a part in and notify the
            // contexts - these contexts should be the collaborations.
            notifyDesignPatterns(pTargets, pDiagram, (IConnectableElement)changedME);
         }
         else
         {
            // Fix W3055:  Need to find the feature's presentation elements
            // This special case is when an operation is dragged out of the class,
            // which is the representing classifier of the lifeline.
            // See also MessageEdgeDrawEngineImpl::ModelElementDeleted()
            if (secondaryChangeME instanceof IFeature)
            {
               pTargets.addElementsPresentationElements(pDiagram, (IFeature)secondaryChangeME);
            }
         }
      }
   }

	protected void getNotificationTargets(INotificationTargets pTargets) {
		IDiagram diagram = null;
		int diagramKind = IDiagramKind.DK_DIAGRAM;
		if (getDrawingArea() != null) {
			diagram = getDrawingArea().getDiagram();
			diagramKind = getDrawingArea().getDiagramKind();
		}

		if (pTargets.getChangedModelElement() != null) {
			IElement changedME = pTargets.getChangedModelElement();
			pTargets.addElementsPresentationElements(diagram, changedME);
			int kind = pTargets.getKind();

			if (kind == ModelElementChangedKind.MECK_ELEMENTDELETED) {
				populateElementDeletedTargets(pTargets, diagram);
			} else if (changedME instanceof ITaggedValue) {
				notifyFirstOwnerWithPresentationElements(pTargets, diagram, changedME);
			} else if (changedME instanceof IActivityPartition) {
				notifyFirstOwnerWithPresentationElements(pTargets, diagram, changedME);
			} else if (changedME instanceof IRegion) {
				notifyFirstOwnerWithPresentationElements(pTargets, diagram, changedME);
			} else if (changedME instanceof IAssociationEnd) {
				IAssociationEnd end = (IAssociationEnd) changedME;
				notifyAssocations(pTargets, diagram, end);
                        // 78868, the logic to handle other IFeature cannot be applied to PartFacade
			} else if (changedME instanceof IFeature && !(changedME instanceof IConnectableElement)) {
				IFeature feature = (IFeature) changedME;
				notifyFeaturesClassifierOrAssocEnd(pTargets, diagram, feature);
			} else if (changedME instanceof IEnumerationLiteral) {
				IEnumerationLiteral literal = (IEnumerationLiteral) changedME;
				notifyEnumerationLiteralsEnumeration(pTargets, diagram, literal);
			} else if (changedME instanceof IUMLBinding) {
				IUMLBinding binding = (IUMLBinding) changedME;
				notifyOwner(pTargets, diagram, binding);
			} else if (changedME instanceof ITypedElement) {
				ITypedElement typedElement = (ITypedElement) changedME;
				notifyOwner(pTargets, diagram, typedElement);
			} else if (changedME instanceof IMultiplicity) {
				IMultiplicity multiplicity = (IMultiplicity) changedME;
				notifyOwner(pTargets, diagram, multiplicity);
			} else if (changedME instanceof IMultiplicityRange) {
				IMultiplicityRange range = (IMultiplicityRange) changedME;
				notifyParentMultiplicity(pTargets, diagram, range);
			} else if (changedME instanceof IProcedureOccurrence) {
				IProcedureOccurrence occurence = (IProcedureOccurrence) changedME;
				notifyMessages(pTargets, diagram, occurence);
			} else if (changedME instanceof IProcedure) {
				IProcedure procedure = (IProcedure) changedME;
				notifyState(pTargets, diagram, procedure);
			} else if (changedME instanceof IInteractionConstraint) {
				IInteractionConstraint constraint = (IInteractionConstraint) changedME;
				notifyExpressions(pTargets, diagram, constraint);
			} else if (changedME instanceof IInteraction) {
				IInteraction interaction = (IInteraction) changedME;
				if (diagramKind == IDiagramKind.DK_SEQUENCE_DIAGRAM) {
					notifyInteration(pTargets, diagram, interaction);
				}
			} else if (changedME instanceof ITransition) {
				ITransition transition = (ITransition) changedME;
				notifyTransitionSourceAndTarget(pTargets, diagram, transition);
			} else if (changedME instanceof IParameterableElement) {
				IParameterableElement parameter = (IParameterableElement) changedME;
				notifyParameterableElementOwner(pTargets, diagram, parameter);
			}

			// If we have a partfacade we need to see if we're playing in one
			// or more design pattherns (collaborations).  If so those design 
			// patterns need to update their template parameters compartment
			if (changedME instanceof IConnectableElement) {
				// Find all the roles this guy plays a part in and notify the
				// contexts - these contexts should be the collaborations.
				IConnectableElement connect = (IConnectableElement) changedME;
				notifyDesignPatterns(pTargets, diagram, connect);
			}

			// Only valid on the behavior diagrams
			if ((diagramKind == IDiagramKind.DK_SEQUENCE_DIAGRAM) || (diagramKind == IDiagramKind.DK_COLLABORATION_DIAGRAM)) {
				notifyLifelines(pTargets, diagram, changedME);
			}

			// If this is a namespace we need to alert the classes that are 
			// showing the package import compartment.
			if (changedME instanceof INamespace) {
				INamespace namespace = (INamespace) changedME;
				if (kind == ModelElementChangedKind.MECK_NAMEMODIFIED) {
					respondToNameModified(pTargets, diagram, namespace);
				} else if (kind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE) {
					respondToElementAddedToNamespace(pTargets, diagram, namespace);
				} else if ((kind == ModelElementChangedKind.MECK_PROJECT_MODEMODIFIED) || (kind == ModelElementChangedKind.MECK_PROJECT_LANGUAGEMODIFIED)) {
					getDrawingArea().validateDiagram(false, null);
				} else if (kind == ModelElementChangedKind.MECK_ELEMENTMODIFIED) {
					//                     // Fix W3510:  Make sure any lifline moves are handled, before determining sizes.
					//                     if(diagram == IDiagramKind.DK_SEQUENCE_DIAGRAM)
					//                     {
					//                        getDrawingArea().refresh(true);
					//                     }
				}
			}
		}
	}

	/**
	 * Adds the menu items the AxDrawingArea by default wants on the context menu
	 *
	 * @param pContextMenu [in] The parent context menu about to be displayed
	 */
    public void onContextMenu(IMenuManager manager)
    {
        // TODO Auto-generated method stub
        m_ContextMenuManager = manager;
        IDrawingAreaControl control = getDrawingArea();
        if (control != null)
        {
            TSEGraphWindow window = control.getGraphWindow();

            if (window != null)
            {
                ETGraph graph = (ETGraph)window.getGraph();

                ETList<TSGraphObject> selected =
                    graph.getSelectedObjects(false, false);

                IProxyUserInterface pProxy =
                    ProductHelper.getProxyUserInterface();

                boolean bIsProperyEditorVisible = true;

                if (pProxy != null)
                    bIsProperyEditorVisible = pProxy.getPropertyEditorVisible();

                TSGraph pTSGraph = null;
                TSNode pTSNode = null;
                TSEdge pTSEdge = null;
                TSLabel pTSLabel = null;
                TSGraphObject graphObj = null;

                if (selected == null)
                {
                    // Check it again. Linux has troubles (J2144)
                    Object targetObj = manager.getContextObject();

                    graphObj =
                        (targetObj instanceof TSGraphObject)
                        ? (TSGraphObject)targetObj
                        : null;

                    if (graphObj == null)
                    {
                        pTSGraph = graph;
                    }

                    else
                    {
                        TSEObject tseObject =
                            graphObj instanceof TSEObject
                            ? (TSEObject) graphObj
                            : null;

                        getDrawingArea().getGraphWindow()
                        .selectObject(tseObject, true);
                    }
                }

                else if (selected.size() > 0)
                {
                    graphObj = selected.get(0);
                }

                if (graphObj != null || pTSGraph != null)
                {
                    if (graphObj instanceof TSGraph)
                        pTSGraph = (TSGraph) graphObj;

                    // ignore path nodes for now
                    if (graphObj instanceof ETNode)
                        pTSNode = (TSNode) graphObj;

                    if (graphObj instanceof TSEdge)
                        pTSEdge = (TSEdge) graphObj;

                    if (graphObj instanceof TSLabel)
                        pTSLabel = (TSLabel) graphObj;

                    IGraphic pGraphic = null;
                    IComment pComment = null;
                    IClassifier pClassifier = null;
                    IETGraphObject etElem =
                        TypeConversions.getETGraphObject(graphObj);

                    if (etElem != null)
                    {
                        // Get the element and we do some special stuff for
                        // graphics and classifiers so get that guy as well
                        IElement pElement = TypeConversions.getElement(etElem);

                        if (pElement != null)
                        {
                            if (pElement instanceof IGraphic)
                                pGraphic = (IGraphic) pElement;

                            if (pElement instanceof IComment)
                                pComment = (IComment) pElement;

                            if (pElement instanceof IClassifier)
                                pClassifier = (IClassifier) pElement;
                        }
                    }

                    //now start loading the menus
                    if (pTSGraph != null)
                    {
                        createLayoutPullright(manager);
                        createEditPullright(graphObj, manager);
                        createBackgroundPullright(manager);
                        addSeparatorMenuItem(manager);
                        createZoomMenu(manager);

                        addSeparatorMenuItem(manager);

                        createSequenceDiagramButtons(manager);
                        createCollaborationDiagramButtons(manager);
                        createActivityDiagramButtons(manager);

                        addSeparatorMenuItem(manager);

                        manager.add(createMenuAction(loadString(
                            "IDS_SYNCH_ELEMENT_WITH_DATA"), // NOI18N
                            "MBK_SYNCH_ELEMENT_WITH_DATA")); // NOI18N

                        // J905:Since this isn't really functional in 6.1.4,
                        // remove this menu item from elements.
                        //manager.add(createMenuAction(loadString(
                        //	"IDS_RESET_PRESENTATION_COLORS_AND_FONTS"),
                        //  "MBK_RESET_PRESENTATION_COLORS_AND_FONTS"));

                        addSeparatorMenuItem(manager);
                        // add the 'Select in Model' action
                        createNodeNavigationPullright(manager);

                        manager.add(createMenuAction(loadString(
                            "IDS_POPUPMENU_GRAPHPROPERTIES"), // NOI18N
                            "MBK_GRAPHPROPERTIES")); // NOI18N
                    }

                    else if (pTSLabel != null)
                    {
                        // Create the label menu
                        addDrawEngineColorMenuItems(manager, pTSLabel, true);
                    }

                    else if (pTSEdge != null)
                    {
                        createEditPullright(graphObj, manager);
                        createEdgeNavigationPullright(manager, pTSEdge);

                        // See if this edge has a label manager
                        IETGraphObject etEle =
                            TypeConversions.getETGraphObject(pTSEdge);

                        if (etEle != null)
                        {
                            IDrawEngine engine = etEle.getEngine();

                            if (engine != null)
                            {
                                ILabelManager labelMgr =
                                    engine.getLabelManager();

                                if (labelMgr != null)
                                {
                                    addResetLabelsButton(manager);
                                }
                            }
                        }

                        manager.add(createMenuAction(loadString(
                            "IDS_SYNCH_ELEMENT_WITH_DATA"), // NOI18N
                            "MBK_SYNCH_ELEMENT_WITH_DATA")); // NOI18N

                        if (!parentIsDiagramKind(
                            IDiagramKind.DK_SEQUENCE_DIAGRAM))
                        {
                            boolean hasBends = ((ETEdge) pTSEdge).hasBends();

                            if (hasBends) // Should never happen
                            {
                                manager.add(createMenuAction(loadString(
                                    "IDS_REMOVE_ALL_BENDS"), // NOI18N
                                    "MBK_REMOVE_ALL_BENDS")); // NOI18N
                            }

                            // See JUML1643 & JUML200
                            // manager.add(createMenuAction(loadString(
                            //	"IDS_AUTOROUTEEDGES"), "MBK_AUTOROUTE_EDGES"));
                            // manager.add(createMenuAction(loadString(
                            //	"IDS_TOGGLE_EDGE_ORTHOGONALITY"),
                            //	"MBK_TOGGLE_EDGE_ORTHOGONALITY"));
                        }

                        // Don't show the properties menu button if the
                        // properties pane is already showing
                        // if (!bIsProperyEditorVisible) {
                        //	manager.add(createMenuAction(loadString(
                        //		"IDS_EDGE_PROPERTIES"),
                        //		"MBK_EDGEPROPERTIES"));
                        // }

                        // cvc - causes IllegalStateException because
                        //  PropertiesAction is a NetBeans shared class action
                        //  Can't "new" it, you must "get" it by its class
                        // BaseActionWrapper action =
                        //		new BaseActionWrapper(new PropertiesAction());
                        createNodeNavigationPullright(manager);
                        BaseActionWrapper action = new BaseActionWrapper(
                            SystemAction.get(PropertiesAction.class));

                        action.setId("MBK_EDGEPROPERTIES");
                        manager.add(action);

                        addDrawEngineColorMenuItems(manager, pTSEdge, false);
                    }

                    else if (pTSNode != null)
                    {
                        IETGraphObject etObj =
                            TypeConversions.getETGraphObject(pTSNode);

                        createEditPullright(graphObj, manager);

                        if (pClassifier != null)
                        {
                            createTransformPullright(manager, pTSNode);
                        }

                        addSeparatorMenuItem(manager);
                        createShowAndHidePullright(
                            manager, (IETGraphObject) pTSNode);

                        addSeparatorMenuItem(manager);

                        // Distribute port interfaces
                        addEdgeDistributionButton(manager, pTSNode);

                        if (!parentIsDiagramKind(
                            IDiagramKind.DK_SEQUENCE_DIAGRAM) &&
                            pGraphic == null)
                        {
                            manager.add(createMenuAction(loadString(
                                "IDS_RESIZE_ELEMENT_TO_CONTEXT"), // NOI18N
                                "MBK_RESIZE_ELEMENT_TO_CONTEXT")); // NOI18N
                        }

                        manager.add(createMenuAction(loadString(
                            "IDS_SYNCH_ELEMENT_WITH_DATA"), // NOI18N
                            "MBK_SYNCH_ELEMENT_WITH_DATA")); // NOI18N

                        addSeparatorMenuItem(manager);

                        if (atLeastTwoSelectedNodes(control.getSelected4()))
                        {
                            createNodeAlignmentPullright(manager);
                            addSeparatorMenuItem(manager);
                        }
                        
                        // disabled - feature to be added with Meteora
                        // createNodeDistributionPullright(manager);
                        //  addSeparatorMenuItem(manager);
                        

                        if (pGraphic == null)
                            createNodeNavigationPullright(manager);

                        // Don't show the properties menu button if the
                        // properties pane is already showing
                        // if (!bIsProperyEditorVisible) {
                        //	manager.add(createMenuAction(loadString(
                        //	"IDS_NODE_PROPERTIES"),
                        //	"MBK_NODEPROPERTIES"));
                        // }

                        // cvc - causes IllegalStateException because
                        //  PropertiesAction is a NetBeans shared class action
                        //  Can't "new" it, you must "get" it by its class
                        // BaseActionWrapper action =
                        //		new BaseActionWrapper(new PropertiesAction());
                        BaseActionWrapper action = new BaseActionWrapper(
                            SystemAction.get(PropertiesAction.class));

                        action.setId("MBK_NODEPROPERTIES"); // NOI18N
                        manager.add(action);

                        createShowAsIconicButton(manager, pTSNode);
                        // Change port locations
                        createPortLocationPullright(manager, pTSNode);
                        addDrawEngineColorMenuItems(manager, pTSNode, true);
                    }
                }
            }
        }
    }
    
    
    private boolean atLeastTwoSelectedNodes(ETList<IElement> elements)
    {
        int i = 0;

        for (IElement element: elements)
        {
            if (!(element instanceof Relationship))
                i++;
            
            if (i > 1)
                return true;
        }
        
        return false;
    }

	/**
		* Tests to determine if the associated parent diagram is a specified kind
		*
		* @param kind [in] The kind of diagram to query
		* @return true if the diagram this button handler is associated with is nKind
		*/
	private boolean parentIsDiagramKind(int nKind) {
		boolean isKind = false;
		IDrawingAreaControl control = getDrawingArea();
		if (control != null) {
			int diaKind = control.getDiagramKind();
			if (diaKind == nKind) {
				isKind = true;
			}
		}
		return isKind;
	}
        
	/**
		* Create the layout pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createLayoutPullright(IMenuManager manager) {
		// Don't show these buttons on the activity or state diagrams.
		if (!parentIsDiagramKind(IDiagramKind.DK_ACTIVITY_DIAGRAM) && !parentIsDiagramKind(IDiagramKind.DK_STATE_DIAGRAM)) {
			IMenuManager insertMenu = manager.createSubMenu(loadString("IDS_LAYOUT_POPUP_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");  //$NON-NLS-2$
			if (insertMenu != null) {
				if (parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM)) {
					insertMenu.add(createMenuAction(loadString("IDS_POPUP_LAYOUT_SEQUENCEDIAGRAM"), "MBK_POPUP_LAYOUT_SEQUENCEDIAGRAM", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
					//addSeparatorMenuItem(insertMenu);
					//insertMenu.add(createMenuAction(loadString("IDS_LAYOUT_RELAYOUT"), "MBK_POPUP_LAYOUT_RELAYOUT", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
				} else {
					//insertMenu.add(createMenuAction(loadString("IDS_POPUP_LAYOUT_CIRCULAR"), "MBK_POPUP_LAYOUT_CIRCULAR", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
					
					insertMenu.add(createMenuAction(loadString("IDS_POPUP_LAYOUT_HIERARCHICAL"), "MBK_POPUP_LAYOUT_HIERARCHICAL", BaseAction.AS_CHECK_BOX, IDrawingAreaAcceleratorKind.DAVK_LAYOUT_HIERARCHICAL_STYLE));  //$NON-NLS-2$
					insertMenu.add(createMenuAction(loadString("IDS_POPUP_LAYOUT_ORTHOGONAL"), "MBK_POPUP_LAYOUT_ORTHOGONAL", BaseAction.AS_CHECK_BOX, IDrawingAreaAcceleratorKind.DAVK_LAYOUT_ORTHOGONAL_STYLE));  //$NON-NLS-2$
					insertMenu.add(createMenuAction(loadString("IDS_POPUP_LAYOUT_SYMMETRIC"), "MBK_POPUP_LAYOUT_SYMMETRIC", BaseAction.AS_CHECK_BOX, IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SYMMETRIC_STYLE));  //$NON-NLS-2$
					//insertMenu.add(createMenuAction(loadString("IDS_POPUP_LAYOUT_TREE"), "MBK_POPUP_LAYOUT_TREE", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
//					addSeparatorMenuItem(insertMenu);
					//insertMenu.add(createMenuAction(loadString("IDS_LAYOUT_RELAYOUT"), "MBK_POPUP_LAYOUT_RELAYOUT", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
					addSeparatorMenuItem(insertMenu);
                                        insertMenu.add(createMenuAction(loadString("IDS_LAYOUT_INCREMENTAL"), "MBK_POPUP_LAYOUT_INCREMENTAL", BaseAction.AS_CHECK_BOX, IDrawingAreaAcceleratorKind.DAVK_LAYOUT_INCREMENTAL_LAYOUT));  //$NON-NLS-2$
					addSeparatorMenuItem(insertMenu);
					insertMenu.add(createMenuAction(loadString("IDS_LAYOUT_PROPERTIES"), "MBK_POPUP_LAYOUT_PROPERTIES"));  //$NON-NLS-2$
				}
			}
			manager.add(insertMenu);
		}
	}

    /**
     * Create the edit pullright
     *
     * @param pItemClickedOn [in] The graph object that was clicked on
     * @param pContextMenu [in] The parent context menu about to be displayed
     * @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
     */
    private void createEditPullright(Object pItemClickedOn, IMenuManager manager)
    {
        IMenuManager editMenu = manager.createSubMenu(loadString(
            "IDS_EDIT_POPUP_TITLE"), // NOI18N
            "org.netbeans.modules.uml.view.drawingarea.edit.popup"); // NOI18N
        
        if (editMenu != null)
        {
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_COPY"), "MBK_POPUP_COPY",  // NOI18N
                IDrawingAreaAcceleratorKind.DAVK_COPY ));
            
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_CUT"), "MBK_POPUP_CUT", 
                IDrawingAreaAcceleratorKind.DAVK_CUT)); // NOI18N
            
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_DELETE"), "MBK_POPUP_DELETE", 
                IDrawingAreaAcceleratorKind.DAVK_DELETE)); // NOI18N
            
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_PASTE"), "MBK_POPUP_PASTE",  // NOI18N
                IDrawingAreaAcceleratorKind.DAVK_PASTE));
            
            if (pItemClickedOn instanceof TSNode)
            {
                addSeparatorMenuItem(editMenu);
                editMenu.add(createMenuAction(loadString(
                    "IDS_LOCK_EDIT"), "MBK_TOGGLE_LOCK_EDIT",  // NOI18N
                    BaseAction.AS_CHECK_BOX));
            }
            
            addSeparatorMenuItem(editMenu);
            
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_SELECTALL"), "MBK_POPUP_SELECTALL", // NOI18N
                IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL));
            
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_SELECTALL_SIMILAR"), "MBK_POPUP_SELECTALL_SIMILAR", // NOI18N
                IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL_SIMILAR));
            
            addSeparatorMenuItem(editMenu);
            
            editMenu.add(createMenuAction(loadString(
                "IDS_POPUP_INVERTSELECTION"), "MBK_POPUP_INVERTSELECTION")); // NOI18N

            // add dimensional resize action (Set Dimensions)
//            if (!parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM))
//            {
                addSeparatorMenuItem(editMenu);

                editMenu.add(createMenuAction(loadString(
                    "IDS_DIMENSIONAL_RESIZE_ELEMENT"), // NOI18N
                    "MBK_DIMENSIONAL_RESIZE_ELEMENT")); // NOI18N
//            }
        }
        
        manager.add(editMenu);
    }

	/**
		* Create the background pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createBackgroundPullright(IMenuManager manager) {
		//J1914
//		IMenuManager backMenu = manager.createSubMenu(loadString("IDS_BACKGROUND_POPUP_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
//		if (backMenu != null) {
//			backMenu.add(createMenuAction(loadString("IDS_POPUP_COLOR"), "MBK_POPUP_COLOR"));  //$NON-NLS-2$
//			// J891
//			//backMenu.add(createMenuAction(loadString("IDS_POPUP_BROWSE_PICTURES"), "MBK_POPUP_BROWSE_PICTURES"));  //$NON-NLS-2$
//			//backMenu.add(createMenuAction(loadString("IDS_POPUP_CLEAR_PICTURE"), "MBK_POPUP_CLEAR_PICTURE"));  //$NON-NLS-2$
//			//addSeparatorMenuItem(backMenu);
//			//backMenu.add(createMenuAction(loadString("IDS_POPUP_PICTURE_TILE"), "MBK_POPUP_PICTURE_TILE"));  //$NON-NLS-2$
//		}
//		manager.add(backMenu);
	}

	/**
		* Create the edge Navigation pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		* @param pTSEdge [in] The TS edge
		*/
	private void createEdgeNavigationPullright(IMenuManager manager, TSEdge pTSEdge) {
		IMenuManager navMenu = manager.createSubMenu(loadString("IDS_POPUP_NAVIGATION_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (navMenu != null) {
			IDrawEngine engine = TypeConversions.getDrawEngine(pTSEdge);
			if (engine != null) {
				String id = engine.getDrawEngineID();
				if (id != null && !id.equals("NestedLinkDrawEngine")) { 
//					navMenu.add(createMenuAction(loadString("IDS_POPUP_FIND_IN_PROJECTTREE"), "MBK_POPUP_FIND_IN_PROJECTTREE"));  //$NON-NLS-2$
				}
			}
			navMenu.add(createMenuAction(loadString("IDS_POPUP_FIND_FROM_NODE"), "MBK_FIND_FROM_NODE"));  //$NON-NLS-2$
			navMenu.add(createMenuAction(loadString("IDS_POPUP_FIND_TO_NODE"), "MBK_FIND_TO_NODE"));  //$NON-NLS-2$
		}
		manager.add(navMenu);
	}

	/**
		* Create the node Navigation pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		*/
	private void createNodeNavigationPullright(IMenuManager manager) {
//		manager.add(createMenuAction(loadString("IDS_POPUP_FIND_IN_PROJECTTREE2"), "MBK_POPUP_FIND_IN_PROJECTTREE"));  //$NON-NLS-2$
		
		// use the SelectInModel action defined in NB Integration moduel, since it
		// has dependency on the core module, we have to use Default File System to access
		// the action class
		
		addNBAction(manager, 
				"Actions/Window/SelectDocumentNode/org-netbeans-modules-uml-integration-ide-actions-SelectInModel.instance"); // NOI18N
//		FileSystem system = Repository.getDefault().getDefaultFileSystem();
//
//		if (system != null)
//		{
//			try
//			{
//				FileObject fo = system.findResource("Actions/Window/SelectDocumentNode/org-netbeans-modules-uml-integration-ide-actions-SelectInModel.instance");
//				DataObject actionObjects = DataObject.find(fo);
//				if (actionObjects!=null)
//				{
//					InstanceCookie ic = (InstanceCookie) actionObjects.getCookie(InstanceCookie.class);
//					if (ic != null) 
//					{
//						Object instance=null;
//						try {
//							instance = ic.instanceCreate();
//						} catch (IOException e) {
//							// ignore
//							e.printStackTrace();
//
//						} catch (ClassNotFoundException e) {
//							// ignore
//							e.printStackTrace();
//						}
//						if (instance instanceof Action)
//						{
//							manager.add(new BaseActionWrapper((Action)instance));
//						}
//					}
//				}
//			}catch (Exception e)
//			{
//				// ignore
//				e.printStackTrace();
//			}
//		}						
	}

   /**
    * Create the Hiding pullright
    *
    * @param pContextMenu [in] The parent context menu about to be displayed
    * @param pMenuItems [in] The menu items where these buttons are created.  
    *                           The other sibling buttons.
    * @param pTSGraphObject [in] The graph object that was clicked on.
    */
    private void createShowAndHidePullright(
        IMenuManager manager, IETGraphObject graphObj)
    {
        IDrawEngine engine = TypeConversions.getDrawEngine(graphObj);
        IElement itemEle = TypeConversions.getElement(graphObj);
        IGraphic pGraphic = null;
        IComment pComment = null;
        
        if (itemEle != null && itemEle instanceof IGraphic)
            pGraphic = (IGraphic) itemEle;
        
        if (itemEle != null && itemEle instanceof IComment)
            pComment = (IComment) itemEle;
        
        IMenuManager showMenu = manager.createSubMenu(loadString(
            "IDS_POPUP_SHOWING_TITLE"), 
            "org.netbeans.modules.uml.view.drawingarea.edit.popup");
        
        IMenuManager hideMenu = manager.createSubMenu(loadString(
            "IDS_POPUP_HIDING_TITLE"), 
            "org.netbeans.modules.uml.view.drawingarea.edit.popup");

        if (showMenu != null && hideMenu != null)
        {
            if (!parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM) && 
                !parentIsDiagramKind(IDiagramKind.DK_ACTIVITY_DIAGRAM) && 
                pGraphic == null && 
                pComment == null)
            {
                createHideChildrenPullright(hideMenu);
                createHideParentsPullright(hideMenu);

                createUnhideChildrenPullright(showMenu);
                createUnhideParentsPullright(showMenu);
            }

            if (engine != null)
            {
                String id = engine.getDrawEngineID();
                if (id != null)
                {
                    // Add buttons for switching back and forth 
                    // between interface lollypop notations.
                    if (itemEle instanceof IInterface)
                    {
                        if (id.equals("ClassDrawEngine"))
                        {
                            showMenu.add(createMenuAction(loadString(
                                "IDS_DISPLAY_AS_ICON"), "MBK_DISPLAY_AS_ICON"));
                        }
                        
                        else if (id.equals("InterfaceDrawEngine"))
                        {
                            showMenu.add(createMenuAction(loadString(
                                "IDS_DISPLAY_AS_CLASS"), "MBK_DISPLAY_AS_CLASS"));
                        }
                    }

                    // Add buttons for moving the name from the 
                    // center to the tab of a package.
                    if (id.equals("PackageDrawEngine"))
                    { 
                        if (engine instanceof IPackageDrawEngine)
                        {
                            IPackageDrawEngine packEngine = 
                                (IPackageDrawEngine)engine;
                            
                            boolean hasContained = packEngine.hasContained();
                            
                            if (!hasContained)
                            {
                                boolean nameInTab = packEngine.getNameInTab();
                            
                                if (nameInTab)
                                {
                                    showMenu.add(createMenuAction(loadString(
                                        "IDS_PACKAGE_NAME_IN_CENTER"), 
                                        "MBK_PACKAGE_NAME_TO_CENTER"));
                                }

                                else
                                {
                                    showMenu.add(createMenuAction(loadString(
                                        "IDS_PACKAGE_NAME_IN_TAB"), 
                                        "MBK_PACKAGE_NAME_TO_TAB"));
                                }
                            }
                        }
                    }
                }

                // Add buttons for showing and hiding stereotype icons
                ICompartment pCompartment = 
                    engine.findCompartmentByCompartmentID(
                        "StereotypeCompartment");
                
                if (pCompartment != null)
                {
                    boolean show = false;
                    if (pCompartment instanceof IStereotypeCompartment)
                    {
                        show = ((IStereotypeCompartment)pCompartment)
                            .getShowStereotypeIcons();
                    }
                    
                    if (show)
                    {
                        hideMenu.add(createMenuAction(loadString(
                            "IDS_HIDE_STEREOTYPE_ICONS"), 
                            "MBK_HIDE_STEREOTYPE_ICONS"));
                    }

                    else
                    {
                        showMenu.add(createMenuAction(loadString(
                            "IDS_SHOW_STEREOTYPE_ICONS"), 
                            "MBK_SHOW_STEREOTYPE_ICONS"));
                    }
                }
            }

            Object[] showItems = showMenu.getItems();
            Object[] hideItems = hideMenu.getItems();

            if (showItems != null && showItems.length > 0)
                manager.add(showMenu);
            
            if (hideItems != null && hideItems.length > 0)
                manager.add(hideMenu);
        }
    }

   /**
    * Create the Hide Children pullright
    *
    * @param pContextMenu [in] The parent context menu about to be displayed
    * @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
    */
    private void createHideChildrenPullright(IMenuManager manager)
    {
        //fix for #5105272. This bug, boils down to org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper.
        //The method hasChildren() calls TSNode.findChildren() and returns true for a Child node.
        //The method hasParents() calls TSNode.findParents() and returns true for a Parent node.
        //hence we reverse the label "IDS_POPUP_HIDE_CHILDREN_TITLE" to "IDS_POPUP_HIDE_PARENTS_TITLE" and vice-versa.
        //IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_HIDE_CHILDREN_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
        IMenuManager hideMenu = manager.createSubMenu(loadString(
            "IDS_POPUP_HIDE_PARENTS_TITLE"), 
            "org.netbeans.modules.uml.view.drawingarea.edit.popup");

        if (hideMenu != null)
        {
            hideMenu.add(createMenuAction(loadString(
                "IDS_POPUP_NODE_ONE_LEVEL"), 
                "MBK_NODE_HIDE_CHILDREN_ONE_LEVEL")); 
            
            hideMenu.add(createMenuAction(loadString(
                "IDS_POPUP_NODE_ALL_LEVELS"), 
                "MBK_NODE_HIDE_CHILDREN_ALL_LEVELS"));

            // hideMenu.add(createMenuAction(loadString(
            //     "IDS_POPUP_NODE_N_LEVELS"), 
            //     "MBK_NODE_HIDE_CHILDREN_N_LEVELS"));

            manager.add(hideMenu);
        }
    }

    /**
     * Create the Hide Parents pullright
     *
     * @param pContextMenu [in] The parent context menu about to be displayed
     * @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
     */
    private void createHideParentsPullright(IMenuManager manager)
    {
        //fix for #5105272. This bug, boils down to org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper.
        //The method hasChildren() calls TSNode.findChildren() and returns true for a Child node.
        //The method hasParents() calls TSNode.findParents() and returns true for a Parent node.
        //hence we reverse the label "IDS_POPUP_HIDE_CHILDREN_TITLE" to "IDS_POPUP_HIDE_PARENTS_TITLE" and vice-versa.
        //IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_HIDE_PARENTS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
        IMenuManager hideMenu = manager.createSubMenu(loadString(
            "IDS_POPUP_HIDE_CHILDREN_TITLE"), 
            "org.netbeans.modules.uml.view.drawingarea.edit.popup");
        
        if (hideMenu != null)
        {
            hideMenu.add(createMenuAction(loadString(
                "IDS_POPUP_NODE_HIDE_PARENTS_ONE_LEVEL"), 
                "MBK_NODE_HIDE_PARENTS_ONE_LEVEL"));
            
            hideMenu.add(createMenuAction(loadString(
                "IDS_POPUP_NODE_HIDE_PARENTS_ALL_LEVELS"), 
                "MBK_NODE_HIDE_PARENTS_ALL_LEVELS"));
            
            // hideMenu.add(createMenuAction(loadString(
            //     "IDS_POPUP_NODE_HIDE_PARENTS_N_LEVELS"), 
            //     "MBK_NODE_HIDE_PARENTS_N_LEVELS"));
            
            manager.add(hideMenu);
        }
    }

	/**
		* Create the Unhide Children pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createUnhideChildrenPullright(IMenuManager manager) {
                //fix for #5105272. This bug, boils down to org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper.
                //The method hasChildren() calls TSNode.findChildren() and returns true for a Child node.
                //The method hasParents() calls TSNode.findParents() and returns true for a Parent node.
                //hence we reverse the label "IDS_POPUP_HIDE_CHILDREN_TITLE" to "IDS_POPUP_HIDE_PARENTS_TITLE" and vice-versa.
		//IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_UNHIDE_CHILDREN_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_UNHIDE_PARENTS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (hideMenu != null) {
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNHIDE_CHILDREN_ONE_LEVEL"), "MBK_NODE_UNHIDE_CHILDREN_ONE_LEVEL"));  //$NON-NLS-2$
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNHIDE_CHILDREN_ALL_LEVELS"), "MBK_NODE_UNHIDE_CHILDREN_ALL_LEVELS"));  //$NON-NLS-2$
			//hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNHIDE_CHILDREN_N_LEVELS"), "MBK_NODE_UNHIDE_CHILDREN_N_LEVELS"));  //$NON-NLS-2$

			manager.add(hideMenu);
		}
	}

	/**
		* Create the Unhide Parents pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createUnhideParentsPullright(IMenuManager manager) {
                //fix for #5105272. This bug, boils down to org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper.
                //The method hasChildren() calls TSNode.findChildren() and returns true for a Child node.
                //The method hasParents() calls TSNode.findParents() and returns true for a Parent node.
                //hence we reverse the label "IDS_POPUP_HIDE_CHILDREN_TITLE" to "IDS_POPUP_HIDE_PARENTS_TITLE" and vice-versa.
		//IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_UNHIDE_PARENTS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_UNHIDE_CHILDREN_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (hideMenu != null) {
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNHIDE_PARENTS_ONE_LEVEL"), "MBK_NODE_UNHIDE_PARENTS_ONE_LEVEL"));  //$NON-NLS-2$
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNHIDE_PARENTS_ALL_LEVELS"), "MBK_NODE_UNHIDE_PARENTS_ALL_LEVELS"));  //$NON-NLS-2$
			//hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNHIDE_PARENTS_N_LEVELS"), "MBK_NODE_UNHIDE_PARENTS_N_LEVELS"));  //$NON-NLS-2$

			manager.add(hideMenu);
		}
	}

	/**
		* Create the Folding pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createFoldingPullright(IMenuManager manager) {
		IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_FOLDING_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (hideMenu != null) {
			createFoldChildrenPullright(hideMenu);
			createFoldParentsPullright(hideMenu);
			addSeparatorMenuItem(hideMenu);
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_UNFOLD"), "MBK_NODE_UNFOLD"));  //$NON-NLS-2$

			manager.add(hideMenu);
		}
	}

	/**
		* Create the Fold Children pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createFoldChildrenPullright(IMenuManager manager) {
		IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_FOLD_CHILDREN_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (hideMenu != null) {
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_FOLD_CHILDREN_ONE_LEVEL"), "MBK_NODE_FOLD_CHILDREN_ONE_LEVEL"));  //$NON-NLS-2$
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_FOLD_CHILDREN_ALL_LEVELS"), "MBK_NODE_FOLD_CHILDREN_ALL_LEVELS"));  //$NON-NLS-2$
			//hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_FOLD_CHILDREN_N_LEVELS"), "MBK_NODE_FOLD_CHILDREN_N_LEVELS"));  //$NON-NLS-2$

			manager.add(hideMenu);
		}
	}

	/**
		* Create the Fold Parents pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createFoldParentsPullright(IMenuManager manager) {
		IMenuManager hideMenu = manager.createSubMenu(loadString("IDS_POPUP_FOLD_PARENTS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (hideMenu != null) {
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_FOLD_PARENTS_ONE_LEVEL"), "MBK_NODE_FOLD_PARENTS_ONE_LEVEL"));  //$NON-NLS-2$
			hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_FOLD_PARENTS_ALL_LEVELS"), "MBK_NODE_FOLD_PARENTS_ALL_LEVELS"));  //$NON-NLS-2$
			//hideMenu.add(createMenuAction(loadString("IDS_POPUP_NODE_FOLD_PARENTS_N_LEVELS"), "MBK_NODE_FOLD_PARENTS_N_LEVELS"));  //$NON-NLS-2$

			manager.add(hideMenu);
		}
	}

	/**
		* Create the zoom pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createZoomMenu(IMenuManager manager) {
		manager.add(createMenuAction(loadString("IDS_POPUP_ZOOM_IN"), "MBK_ZOOM_IN"));  //$NON-NLS-2$
		manager.add(createMenuAction(loadString("IDS_POPUP_ZOOM_OUT"), "MBK_ZOOM_OUT"));  //$NON-NLS-2$
		manager.add(createMenuAction(loadString("IDS_POPUP_ZOOM_CUSTOM_ZOOM"), "MBK_ZOOM_CUSTOM_ZOOM"));  //$NON-NLS-2$
	}

	/**
		* Creates the buttons for a sequence diagra, if this is a sequence diagram
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createSequenceDiagramButtons(IMenuManager manager) {
		if (parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM)) {
			manager.add(createMenuAction(loadString("IDS_SQD_SHOW_ALL_RETURN_MESSAGES"), "MBK_SQD_SHOW_ALL_RETURN_MESSAGES", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
			manager.add(createMenuAction(loadString("IDS_SQD_SHOW_MESSAGE_NUMBERS"), "MBK_SQD_SHOW_MESSAGE_NUMBERS", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
			manager.add(createMenuAction(loadString("IDS_SQD_SHOW_INTERACTION_BOUNDARY"), "MBK_SQD_SHOW_INTERACTION_BOUNDARY", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
		}
	}

	/**
		* Creates the buttons for a collaboration diagram, if this is a collaboration diagram
		*/
	private void createCollaborationDiagramButtons(IMenuManager manager) {
		if (parentIsDiagramKind(IDiagramKind.DK_COLLABORATION_DIAGRAM)) {
			manager.add(createMenuAction(loadString("IDS_SQD_SHOW_MESSAGE_NUMBERS"), "MBK_SQD_SHOW_MESSAGE_NUMBERS", BaseAction.AS_CHECK_BOX));  //$NON-NLS-2$
		}
	}

	/**
		* Creates the buttons for an activity diagram, if this is an activity diagram
		*/
	private void createActivityDiagramButtons(IMenuManager manager) {
		//nothing is done in C++ right now.
	}

       /**
        * Create the transform pullright
        *
        * @param pContextMenu [in] The parent context menu about to be displayed
        * @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
        */
	private void createTransformPullright(IMenuManager manager, TSNode pTSNode) {
		IElement pEle = TypeConversions.getElement(pTSNode);
		if (pEle != null) {
			String elemType = pEle.getElementType();

			// Get the element type.  Tranform pullright is only available 
			// for Class, Interface, DataType, Enumeration and Actor
			if (elemType.equals("Class") || elemType.equals("Interface") || elemType.equals("Actor") || elemType.equals("DataType") || elemType.equals("Enumeration")) {  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				IMenuManager transMenu = manager.createSubMenu(loadString("IDS_TRANSFORM_POPUP_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
				if (transMenu != null) {
					// Add our transform buttons
					transMenu.add(createMenuAction(loadString("IDS_TRANSFORM_TO_ACTOR"), "MBK_TRANSFORM_TO_ACTOR"));  //$NON-NLS-2$
					transMenu.add(createMenuAction(loadString("IDS_TRANSFORM_TO_CLASS"), "MBK_TRANSFORM_TO_CLASS"));  //$NON-NLS-2$
					transMenu.add(createMenuAction(loadString("IDS_TRANSFORM_TO_INTERFACE"), "MBK_TRANSFORM_TO_INTERFACE"));  //$NON-NLS-2$
					transMenu.add(createMenuAction(loadString("IDS_TRANSFORM_TO_DATATYPE"), "MBK_TRANSFORM_TO_DATATYPE"));  //$NON-NLS-2$
					transMenu.add(createMenuAction(loadString("IDS_TRANSFORM_TO_ENUMERATION"), "MBK_TRANSFORM_TO_ENUMERATION"));  //$NON-NLS-2$

					manager.add(transMenu);
				}
			}
		}
	}

        /**
        * Adds the color menu buttons.
        *
        * @param pContextMenu[in] The context menu about to be displayed
        * @param bAddFontMenuItems [in] true if we should add the font menu items
        */
	private void addDrawEngineColorMenuItems(IMenuManager manager, TSGraphObject obj, boolean addFontMenus) {
            String eleType = null;
            String format = null;
            
            IElement pEle = TypeConversions.getElement(obj);
            if (pEle != null)
            {
                String expandedType = pEle.getExpandedElementType();
                if (pEle instanceof IAssociation) 
                {
                    format = expandedType.replace('_', ' ');
                }
                else 
                {
                    eleType = pEle.getElementType();
                    if (eleType != null && eleType.length() > 0) 
                    {
                        format = eleType;
                        if (expandedType != null) 
                        {
                            String captionKey = "IDS_" + expandedType.toUpperCase();
                            String caption = loadString(captionKey);

                            if (!caption.equals("!" + captionKey + "!")) 
                            {
                                format = caption;
                            }
                        }
                    }
                }
            }

            if (format != null && format.length() > 0) {
                IMenuManager subMenu = manager.createOrGetSubMenu(format, "org.netbeans.modules.uml.view.drawingarea.edit.popup"); 
                if (subMenu != null) {
                    if (addFontMenus) {
                        // Edges don't have fill colors or fonts
                        subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_FONTS"), "MBK_CHANGE_SIMILAR_FONT"));  //$NON-NLS-2$
                        subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_FONTCOLOR"), "MBK_CHANGE_SIMILAR_FONT_COLOR"));  //$NON-NLS-2$
                        subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_BKCOLOR"), "MBK_CHANGE_SIMILAR_FILL_COLOR"));  //$NON-NLS-2$
                    }
                    subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_BDCOLOR"), "MBK_CHANGE_SIMILAR_BORDER_COLOR"));  //$NON-NLS-2$
                    //manager.add(subMenu);
                }
            }
	}

	/**
        * Adds the reset labels button handler
        *
        * @param pContextMenu [in] The parent context menu
        * @param pMenuItems [in] The menu items where we should add these buttons
        */
	private void addResetLabelsButton(IMenuManager manager) {
		IMenuManager subMenu = manager.createSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
		if (subMenu != null) {
			subMenu.add(createMenuAction(loadString("IDS_RESETLABELS"), "MBK_RESET_LABELS"));  //$NON-NLS-2$
			manager.add(subMenu);
		}
	}

	/**
		* Adds buttons to distribute Port interfaces or reroute edges
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void addEdgeDistributionButton(IMenuManager manager, TSNode obj) {
		if (hasPorts(obj) || isAPort(obj)) {
			manager.add(createMenuAction(loadString("IDS_DISTRIBUTE_PORT_INTERFACES"), "MBK_DISTRIBUTE_PORT_INTERFACES"));  //$NON-NLS-2$
		} else {
			manager.add(createMenuAction(loadString("IDS_RESET_EDGES"), "MBK_RESET_EDGES"));  //$NON-NLS-2$
		}
	}

	/**
		* Does the item we've clicked on have ports
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		*/
	private boolean hasPorts(TSNode node) {
		boolean found = false;
		IDrawEngine engine = TypeConversions.getDrawEngine(node);
		if (engine != null) {
			if (engine instanceof IComponentDrawEngine) {
				ETList < IElement > ports = ((IComponentDrawEngine) engine).getPorts2();
				if (ports != null && ports.size() > 0) {
					found = true;
				}
			}
		}
		return found;
	}

	/**
		* Is the item we've clicked on a port
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		*/
	private boolean isAPort(TSNode node) {
		boolean isPort = false;
		IDrawEngine engine = TypeConversions.getDrawEngine(node);
		if (engine != null) {
			String id = engine.getDrawEngineID();

			if (id != null && id.endsWith("PortDrawEngine")) { 
				isPort = true;
			}
		}
		return isPort;
	}

	/**
		* Create the classes Show As Iconic pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createShowAsIconicButton(IMenuManager manager, TSNode node)
        {
            IElement pEle = TypeConversions.getElement(node);
            IDrawEngine engine = TypeConversions.getDrawEngine(node);
            if (pEle != null && pEle instanceof IClass && engine != null)
            {
                IClass pClass = (IClass) pEle;
                String id = engine.getDrawEngineID();
                Object obj = pClass.retrieveAppliedStereotype(BOUNDARY_STEREO);
                if (obj == null)
                {
                    obj = pClass.retrieveAppliedStereotype(CONTROL_STEREO);
                    if (obj == null)
                    {
                        obj = pClass.retrieveAppliedStereotype(ENTITY_STEREO);
                    }
                }
                if (obj!=null)
                {
                    if (id != null && id.equals("ClassDrawEngine")) // NOI18N
                        manager.add(createMenuAction(loadString("IDS_SHOW_AS_ICONIC"), "MBK_SHOW_AS_ICONIC"));  // NOI18N
                    else
                        manager.add(createMenuAction(loadString("IDS_SHOW_AS_NONICONIC"), "MBK_SHOW_AS_NONICONIC"));  // NOI18N
                }
            }
        }

	private boolean hasPorts(IMenuManager pContextMenu) {
		boolean bHasPorts = false;

		Object pItemClickedOn = pContextMenu.getContextObject();
		if (pItemClickedOn != null && pItemClickedOn instanceof TSNode) {
			TSNode pTSNode = (TSNode) pItemClickedOn;
			IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pTSNode);
			if (pDrawEngine != null) {
				String sDrawEngineID = pDrawEngine.getDrawEngineID();
				if (pDrawEngine instanceof IComponentDrawEngine) {
					IComponentDrawEngine pComponentDrawEngine = (IComponentDrawEngine) pDrawEngine;
					ETList < IElement > pPorts = pComponentDrawEngine.getPorts2();
					if (pPorts != null && pPorts.size() > 0) {
						bHasPorts = true;
					}
				}
			}
		}
		return bHasPorts;
	}
	/**
		* Create the port location pullright
		*
		* @param pContextMenu [in] The parent context menu about to be displayed
		* @param pMenuItems [in] The menu items where these buttons are created.  The other sibling buttons.
		*/
	private void createPortLocationPullright(IMenuManager manager, TSNode obj) {
		if (hasPorts(manager) || isAPort(obj)) {
			IMenuManager subMenu = manager.createSubMenu(loadString("IDS_PORT_LOCATIONS"), "org.netbeans.modules.uml.view.drawingarea.edit.popup");  //$NON-NLS-2$
			if (subMenu != null) {
				subMenu.add(createMenuAction(loadString("IDS_TOP"), "MBK_PORTS_TO_TOP"));  //$NON-NLS-2$
				subMenu.add(createMenuAction(loadString("IDS_BOTTOM"), "MBK_PORTS_TO_BOTTOM"));  //$NON-NLS-2$
				subMenu.add(createMenuAction(loadString("IDS_LEFT"), "MBK_PORTS_TO_LEFT"));  //$NON-NLS-2$
				subMenu.add(createMenuAction(loadString("IDS_RIGHT"), "MBK_PORTS_TO_RIGHT"));  //$NON-NLS-2$

				manager.add(subMenu);
			}
		}
	}

    private void createNodeAlignmentPullright(IMenuManager manager)
    {
        IMenuManager alignMenu = manager.createSubMenu(loadString(
            "IDS_ALIGNMENT_POPUP_TITLE"), 
            "org.netbeans.modules.uml.view.drawingarea.edit.popup");
        
        if (alignMenu != null)
        {
            // Add horizontal alignment menu actions
            alignMenu.add(createMenuAction(
                loadString("IDS_ALIGN_LEFT"), "MBK_ALIGN_LEFT"));
            alignMenu.add(createMenuAction(
                loadString("IDS_ALIGN_HCENTER"), "MBK_ALIGN_HCENTER"));
            alignMenu.add(createMenuAction(
                loadString("IDS_ALIGN_RIGHT"), "MBK_ALIGN_RIGHT"));
            
            addSeparatorMenuItem(alignMenu);
            
            // Add vertical alignment menu actions
            alignMenu.add(createMenuAction(
                loadString("IDS_ALIGN_TOP"), "MBK_ALIGN_TOP"));
            alignMenu.add(createMenuAction(
                loadString("IDS_ALIGN_VCENTER"), "MBK_ALIGN_VCENTER"));
            alignMenu.add(createMenuAction(
                loadString("IDS_ALIGN_BOTTOM"), "MBK_ALIGN_BOTTOM"));

            manager.add(alignMenu);
        }
    }

    
// disabled - feature to be added with Meteora
//    private void createNodeDistributionPullright(IMenuManager manager)
//    {
//        IMenuManager distributeMenu = manager.createSubMenu(loadString(
//            "IDS_DISTRIBUTE_POPUP_TITLE"), 
//            "org.netbeans.modules.uml.view.drawingarea.edit.popup");
//        
//        if (distributeMenu != null)
//        {
//            // Add horizontal distribute menu actions
//            distributeMenu.add(createMenuAction(
//                loadString("IDS_DISTRIBUTE_LEFT_EDGE"),
//                "MBK_DISTRIBUTE_LEFT_EDGE"));
//            
//            distributeMenu.add(createMenuAction(
//                loadString("IDS_DISTRIBUTE_HORIZONTAL_CENTER"),
//                "MBK_DISTRIBUTE_HCENTER"));
//            
//            distributeMenu.add(createMenuAction(
//                loadString("IDS_DISTRIBUTE_RIGHT_EDGE"),
//                "MBK_DISTRIBUTE_RIGHT_EDGE"));
//            
//            distributeMenu.add(createMenuAction(
//                loadString("IDS_DISTRIBUTE_TOP_EDGE"),
//                "MBK_DISTRIBUTE_TOP_EDGE"));
//            
//            distributeMenu.add(createMenuAction(
//                loadString("IDS_DISTRIBUTE_VERTICAL_CENTER"), 
//                "MBK_DISTRIBUTE_VCENTER"));
//
//            distributeMenu.add(createMenuAction(
//                loadString("IDS_DISTRIBUTE_BOTTOM_EDGE"),
//                "MBK_DISTRIBUTE_BOTTOM_EDGE"));
//            
//            manager.add(distributeMenu);
//        }
//    }
        
	private String loadString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	private void addSeparatorMenuItem(IMenuManager manager) {
		manager.add(new Separator());
	}

	//**************************************************
	// Event Casses
	//**************************************************

	public class NodeEventListener implements IDrawingAreaAddNodeEventsSink {

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink#onDrawingAreaCreateNode(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaCreateNode(IDiagram pParentDiagram, ICreateNodeContext pContext, IResultCell cell) {
			if ((pContext != null) && (isParent(pParentDiagram) == true)) {
				if ((pContext.getLogicalPoint() != null) && (pContext.getCancel() == false) && (getDiagram() != null)) {
					IETPoint point = pContext.getLogicalPoint();

					point.getX();
					point.getY();

					INodeVerification verification = CreationFactoryHelper.getNodeVerification();

					if (verification != null) {
						if (verification.verifyCreationLocation(getDiagram(), point) == true) {
							pContext.setLogicalPoint(point);
						}

						pContext.setCancel(false);
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink#onDrawingAreaDraggingNode(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaDraggingNode(IDiagram pParentDiagram, IDraggingNodeContext pContext, IResultCell cell) {
			if ((pContext != null) && (isParent(pParentDiagram) == true)) {
				if ((pContext.getLogicalPoint() != null) && (pContext.getMovingNode() != null) && (getDiagram() != null)) {
					IETPoint point = pContext.getLogicalPoint();

					point.getX();
					point.getY();

					INodeVerification verification = CreationFactoryHelper.getNodeVerification();

					if ((verification != null) && (pContext.getMovingNode() instanceof ETNode)) {
						verification.verifyDragDuringCreation(getDiagram(), (ETNode) pContext.getMovingNode(), point);
					}
				}
			}
		}
	}

	public class EdgeEventListener extends DrawingAreaAddEdgeEventsSinkAdapter {

		/** (non-Javadoc)
		 * This event is received when an edge is about to begin.  The 
		 * IEdgeCreateContext contains the details.
		 * 
		 * @param pContext [in] The context about the edge that is beginning
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaStartingEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaStartingEdge(IDiagram pParentDiagram, IEdgeCreateContext pContext, IResultCell cell) {
			if ((pContext.getCancel() == false) && 
             (getDrawingArea() != null) && 
             (isParent(pParentDiagram) == true)) {
				String edgeViewDescription = pContext.getViewDescription();
				if ((edgeViewDescription.length() > 0) && (pContext.getNodeModelElement() != null)) {
					IPresentationTypesMgr mgr = getDrawingArea().getPresentationTypesMgr();
					if (mgr != null) {
						PresentationTypeDetails details = mgr.getInitStringDetails(edgeViewDescription, getDrawingArea().getDiagramKind());
						IEdgeVerification verification = CreationFactoryHelper.getEdgeVerification();
						if (verification != null) {
							boolean valid = verification.verifyStartNode(pContext.getNodeModelElement(), details.getMetaType());
							pContext.setCancel(!valid);
						} else {
							pContext.setCancel(true);
						}
					} else {
						pContext.setCancel(true);
					}
				} else {
					pContext.setCancel(true);
				}
			}
		}

		/** (non-Javadoc)
		 * This event is received when an edge is being moved around.  The 
		 * IEdgeMouseMoveContext contains the details.
		 *
		 * @param pContext [in] The details about the new edge that is being moved.
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaEdgeMouseMove(IDiagram pParentDiagram, IEdgeMouseMoveContext pContext, IResultCell cell) {
			if ((pContext != null) && (isParent(pParentDiagram) == true)){
				IElement startNode = pContext.getStartNodeModelElement();
				IElement nodeUnderMouse = pContext.getNodeUnderMouseModelElement();
				String edgeViewDescription = pContext.getViewDescription();

				if ((startNode != null) && (nodeUnderMouse != null) && (edgeViewDescription != null) && (edgeViewDescription.length() > 0)) {
					IPresentationTypesMgr mgr = getDrawingArea().getPresentationTypesMgr();
					if (mgr != null) {
						PresentationTypeDetails details = mgr.getInitStringDetails(edgeViewDescription, getDrawingArea().getDiagramKind());
						IEdgeVerification verification = CreationFactoryHelper.getEdgeVerification();
						if (verification != null) {
							boolean valid = verification.verifyFinishNode(startNode, nodeUnderMouse, details.getMetaType());
							pContext.setValid(valid);
						}
					}
				}
			}
		}

		/**
		 * This event is received when an edge is about to begin.  The IEdgeCreateContext contains the details.
		 *
		 * @param pContext [in] The details about the new edge that is to be created.
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaFinishEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaFinishEdge(IDiagram pParentDiagram, IEdgeFinishContext pContext, IResultCell cell) {
			if ((pContext != null) && (isParent(pParentDiagram) == true)) {
				handleNestedLinkFinish(pContext);
				handlePartFacadeFinish(pContext);

				// Verify that the above handlers did not already cancel the process.
				if ((pContext.getCancel() == false) && (getDrawingArea() != null)) {
					IElement startNode = pContext.getStartNodeModelElement();
					IElement finishNode = pContext.getFinishNodeModelElement();
					String edgeViewDescription = pContext.getViewDescription();

					if ((startNode != null) && (finishNode != null) && (edgeViewDescription != null) && (edgeViewDescription.length() > 0)) {
						IPresentationTypesMgr mgr = getDrawingArea().getPresentationTypesMgr();
						if (mgr != null) {
							PresentationTypeDetails details = mgr.getInitStringDetails(edgeViewDescription, getDrawingArea().getDiagramKind());
							IEdgeVerification verification = CreationFactoryHelper.getEdgeVerification();
							if (verification != null) {
								boolean valid = verification.verifyFinishNode(startNode, finishNode, details.getMetaType());
								pContext.setCancel(!valid);
							} else {
								pContext.setCancel(true);
							}
						} else {
							pContext.setCancel(true);
						}
					} else {
						pContext.setCancel(true);
					}
				}
			}
		}
	}

		/**
		 * Fired when an edge reconnect is about to occur.
		 *
		 * @param pContext [in] The details about the reconnect
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink#onDrawingAreaReconnectEdgeStart(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaReconnectEdgeStart(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IResultCell cell) {
			if ((pContext != null)&& (isParent(pParentDiagram) == true)) {
				boolean valid = true;
				IEdgeVerification verfication = CreationFactoryHelper.getEdgeVerification();
				if (verfication != null) {
					IETEdge edge = pContext.getEdge();
					IETNode preConnectNode = pContext.getPreConnectNode();

					if ((edge != null) && (preConnectNode != null)) {
						if (edge instanceof ETEdge) {
							ETEdge etEdge = (ETEdge) edge;

							String edgeElementType = ""; 
							if (etEdge.getPresentationElement() != null) {
								IElement element = etEdge.getPresentationElement().getFirstSubject();
								edgeElementType = element.getElementType();
							}

							if (preConnectNode instanceof ETNode) {
								ETNode etPreConnectNode = (ETNode) preConnectNode;
								IPresentationElement presElement = etPreConnectNode.getPresentationElement();

								if ((presElement != null) && (presElement.getFirstSubject() != null) && (edgeElementType.length() > 0)) {
									valid = verfication.verifyReconnectStart(presElement.getFirstSubject(), edgeElementType);
								}
							}
						}
					}

					pContext.setCancel(!valid);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink#onDrawingAreaReconnectEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaReconnectEdgeMouseMove(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IResultCell cell) {
			if ((pContext != null)&& (isParent(pParentDiagram) == true)) {
				boolean valid = true;
				IEdgeVerification verifcation = CreationFactoryHelper.getEdgeVerification();
				if (verifcation != null) {
					IETEdge edge = pContext.getEdge();
					IETNode proposedEndNode = pContext.getProposedEndNode();
					IETNode anchoredNode = pContext.getAnchoredNode();

					if ((edge != null) && (anchoredNode != null)) {
						IElement edgeElement = TypeConversions.getElement(edge);
						IElement anchoredElement = TypeConversions.getElement(anchoredNode);
						IElement proposedEndElement = TypeConversions.getElement(proposedEndNode);

						String edgeElementType = edgeElement != null ? edgeElement.getElementType() : ""; 

						if (edgeElementType.length() > 0 && anchoredElement != null && proposedEndElement != null) 
						{
							if (pContext.getReconnectTarget())
								valid = verifcation.verifyFinishNode(anchoredElement, proposedEndElement, edgeElementType);
							else
								valid = verifcation.verifyFinishNode(proposedEndElement, anchoredElement, edgeElementType);
						}
					}
				}

				pContext.setCancel(!valid);
			}
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink#onDrawingAreaReconnectEdgeFinish(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onDrawingAreaReconnectEdgeFinish(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IResultCell cell) {
			if (pContext != null&& (isParent(pParentDiagram) == true)) {
				IETEdge edge = pContext.getEdge();
				IEdgePresentation edgePres = TypeConversions.getEdgePresentation(edge);
				IETNode start=pContext.getAnchoredNode();
                IETNode finish=pContext.getProposedEndNode();
                if (start.equals(finish))
                { 
                    pContext.setCancel(true);
                    return;
                }
				boolean valid = false;
				if (edgePres != null) {
					valid = edgePres.reconnectLink(pContext);
				}

				pContext.setCancel(!valid);
			}
		}

	public class ChangeNotificiationListener implements IChangeNotificationTranslatorSink {

		/**
		 * This routine takes the change and type of change that was made and
		 * tries to figure out what presentation elements need to know about 
		 * the event.
		 *
		 * @param pTargets[in] Information about the event.  Add presentation elements to be notified through
		 * this interface.
		 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink#onGetNotificationTargets(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onGetNotificationTargets(IDiagram pDiagram, INotificationTargets pTargets, IResultCell cell) {
			if(isParent(pDiagram) == true)
         {
            getNotificationTargets(pTargets);
         }
		}
	}

	/**
		* Tests to determine if the associated parent diagram is readonly
		*
		* @return true if the parent diagram is readonly
		*/
	protected boolean isParentDiagramReadOnly() {
		boolean readOnly = true;
		IDrawingAreaControl control = getDrawingArea();
		if (control != null) {
			readOnly = control.getReadOnly();
		}
		return readOnly;
	}

	/**
	 * Does this engine control this diagram?
	 *
	 * @param diagram The diagram that we're questioning the control of
	 * @return true if this engine is controlling the argument diagram.
	 */
	public boolean isParent(IDiagram diagram) {
		boolean retVal = false;

		IDrawingAreaControl ctrl = getDrawingArea();
		if ((diagram != null) && (ctrl != null)) {
			retVal = ctrl.isSame(diagram);
		}

		return retVal;
	}

    public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
    {
        boolean retVal = false;
        IDrawingAreaControl control = getDrawingArea();
        ADGraphWindow window = control.getGraphWindow();
        TSEGraph graph = window.getGraph();
        Object obj = m_ContextMenuManager.getContextObject();
        IETGraphObject pETElement = null;
        IDrawEngine pDrawEngine = null;
        IPresentationElement pPresEle = null;
        IElement pElement = null;
        TSNode pProductNode = null;
        TSEdge pProductEdge = null;
        
        if (obj instanceof TSNode)
        {
            pProductNode = (TSNode) obj;
            pETElement = TypeConversions.getETGraphObject(pProductNode);
            pDrawEngine = TypeConversions.getDrawEngine(pProductNode);
            pElement = TypeConversions.getElement(pProductNode);
            pPresEle = TypeConversions.getPresentationElement(pProductNode);
        }
        
        else if (obj instanceof TSEdge)
        {
            pProductEdge = (TSEdge) obj;
            pETElement = TypeConversions.getETGraphObject(pProductEdge);
            pDrawEngine = TypeConversions.getDrawEngine(pProductEdge);
            pElement = TypeConversions.getElement(pProductEdge);
            pPresEle = TypeConversions.getPresentationElement(pProductEdge);
        }
        
        String elemType = ""; 
        
        if (pElement != null)
        {
            elemType = pElement.getElementType();
        }

        // First set the check state
        int layoutKind = 0;
        if (control != null)
        {
            layoutKind = control.getLayoutStyle();
            if (id.equals("MBK_POPUP_LAYOUT_CIRCULAR") && layoutKind == ILayoutKind.LK_CIRCULAR_LAYOUT)
            { 
                pClass.setChecked(true);
            }
            else if (id.equals("MBK_POPUP_LAYOUT_HIERARCHICAL") && layoutKind == ILayoutKind.LK_HIERARCHICAL_LAYOUT)
            { 
                pClass.setChecked(true);
            }
            else if (id.equals("MBK_POPUP_LAYOUT_ORTHOGONAL") && layoutKind == ILayoutKind.LK_ORTHOGONAL_LAYOUT)
            { 
                pClass.setChecked(true);
            }
            else if (id.equals("MBK_POPUP_LAYOUT_SYMMETRIC") && layoutKind == ILayoutKind.LK_SYMMETRIC_LAYOUT)
            { 
                pClass.setChecked(true);
            }
            else if (id.equals("MBK_POPUP_LAYOUT_TREE") && layoutKind == ILayoutKind.LK_TREE_LAYOUT)
            { 
                pClass.setChecked(true);
            }
            else if (id.equals("MBK_POPUP_LAYOUT_SEQUENCEDIAGRAM") && layoutKind == ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT)
            { 
                pClass.setChecked(true);
            }
            else if (id.equals("MBK_POPUP_PICTURE_TILE"))
            { 
                if (control != null)
                {
                    //to implement
                    pClass.setChecked(true);
                }
            }
        }

        boolean bFlag = false;
        boolean bReadOnly = isParentDiagramReadOnly();
        // Now set the sensitivity
        if (id.equals("MBK_GOTO_PARENT_GRAPH"))
        { 
            bFlag = control.getHasParentGraph();
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_FIT_TO_WINDOW") 
        || id.equals("MBK_ZOOM_25") 
        || id.equals("MBK_ZOOM_50") 
        || id.equals("MBK_ZOOM_ACTUAL_SIZE") 
        || id.equals("MBK_ZOOM_200") 
        || id.equals("MBK_ZOOM_400") 
        || id.equals("MBK_ZOOM_CUSTOM_ZOOM") 
        || id.equals("MBK_ZOOM_IN") 
        || id.equals("MBK_ZOOM_OUT"))
        { 
            // These guys are always sensitive
            retVal = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_CIRCULAR") 
        || id.equals("MBK_POPUP_LAYOUT_HIERARCHICAL") 
        || id.equals("MBK_POPUP_LAYOUT_ORTHOGONAL") 
        || id.equals("MBK_POPUP_LAYOUT_SYMMETRIC") 
        || id.equals("MBK_POPUP_LAYOUT_TREE") 
        || id.equals("MBK_POPUP_LAYOUT_INCREMENTAL"))
        { 
            if (!parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_SEQUENCEDIAGRAM"))
        { 
            if (parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_RELAYOUT") || id.equals("MBK_POPUP_LAYOUT_PROPERTIES"))
        {  //$NON-NLS-2$
            retVal = bReadOnly ? false : true;
        }
        else if (id.equals("MBK_POPUP_COPY"))
        { 
            retVal = control.getHasSelectedNodes(true);
        }
        else if (id.equals("MBK_POPUP_CUT"))
        { 
            bFlag = control.getHasSelectedNodes(true);
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_POPUP_DELETE"))
        { 
            bFlag = control.getHasSelected(true);
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_POPUP_PASTE"))
        { 
            if (!parentIsDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM))
            {
                bFlag = control.itemsOnClipboard();
                retVal = bReadOnly ? false : bFlag;
            }
            else
            {
                retVal = false;
            }
        }
        else if (id.equals("MBK_TOGGLE_LOCK_EDIT"))
        { 
            if (pPresEle != null && pPresEle instanceof INodePresentation)
            {
                // Get the current status of the lock flag
                boolean curLockSts = ((INodePresentation) pPresEle).getLockEdit();
                //need to set it checked - do it while building menu item.
                pClass.setChecked(curLockSts);
            }
            retVal = true;
        }
        else if (id.equals("MBK_POPUP_SELECTALL") || id.equals("MBK_POPUP_INVERTSELECTION"))
        {  //$NON-NLS-2$
            retVal = true;
        }
        else if (id.equals("MBK_POPUP_SELECTALL_SIMILAR"))
        { 
            retVal = control.getHasSelected(true);
        }
        else if (id.equals("MBK_POPUP_CLEAR_PICTURE") || id.equals("MBK_POPUP_PICTURE_TILE"))
        {  //$NON-NLS-2$
            // to do
        }
        else if (id.equals("MBK_GOTO_CHILD_GRAPH"))
        { 
            if (pProductNode != null)
            {
                bFlag = control.getHasChildGraph(pProductNode);
                retVal = bReadOnly ? false : bFlag;
            }
        }
        else if (id.equals("MBK_NODE_EXPAND"))
        { 
            if (pProductNode != null)
            {
                //To do
            }
        }
        else if (id.equals("MBK_NODE_COLLAPSE"))
        { 
            if (pProductNode != null)
            {
                //To do
            }
        }
        else if (id.equals("MBK_NODE_CREATE_CHILD_GRAPH"))
        { 
            if (pProductNode != null)
            {
                //To do
            }
        }
        else if (id.equals("MBK_NODE_DELETE_CHILD_GRAPH"))
        { 
            if (pProductNode != null)
            {
                //To do
            }
        }
        else if (id.equals("MBK_NODE_HIDE_CHILDREN_ONE_LEVEL") 
        || id.equals("MBK_NODE_HIDE_CHILDREN_N_LEVELS") 
        || id.equals("MBK_NODE_HIDE_CHILDREN_ALL_LEVELS") 
        || id.equals("MBK_NODE_FOLD_CHILDREN_ONE_LEVEL") 
        || id.equals("MBK_NODE_FOLD_CHILDREN_N_LEVELS") 
        || id.equals("MBK_NODE_FOLD_CHILDREN_ALL_LEVELS"))
        { 
            if (pProductNode != null && GetHelper.hasChildren(window, pProductNode))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_NODE_HIDE_PARENTS_ONE_LEVEL") 
        || id.equals("MBK_NODE_HIDE_PARENTS_N_LEVELS") 
        || id.equals("MBK_NODE_HIDE_PARENTS_ALL_LEVELS") 
        || id.equals("MBK_NODE_FOLD_PARENTS_ONE_LEVEL") 
        || id.equals("MBK_NODE_FOLD_PARENTS_N_LEVELS") 
        || id.equals("MBK_NODE_FOLD_PARENTS_ALL_LEVELS"))
        { 
            if (pProductNode != null && GetHelper.hasParents(window, pProductNode))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_NODE_UNHIDE_CHILDREN_ONE_LEVEL") || id.equals("MBK_NODE_UNHIDE_CHILDREN_N_LEVELS") || id.equals("MBK_NODE_UNHIDE_CHILDREN_ALL_LEVELS"))
        {  //$NON-NLS-2$ //$NON-NLS-3$
            if (pProductNode != null && GetHelper.hasHiddenChildren(window, pProductNode))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_NODE_UNHIDE_PARENTS_ONE_LEVEL") || id.equals("MBK_NODE_UNHIDE_PARENTS_N_LEVELS") || id.equals("MBK_NODE_UNHIDE_PARENTS_ALL_LEVELS"))
        {  //$NON-NLS-2$ //$NON-NLS-3$
            if (pProductNode != null && GetHelper.hasHiddenParents(window, pProductNode))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_NODE_UNFOLD"))
        { 
            if (pProductNode != null && GetHelper.hasFoldedItems(window, pProductNode))
            {
                bFlag = true;
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_POPUP_FIND_IN_PROJECTTREE"))
        { 
//			if (pElement !=null)
            bFlag=true;

//			IProjectTreeControl pTree = ProductHelper.getProjectTree();
//
//			if (pElement instanceof IGraphic) {
//				// Don't sensitize the button if its a graphic shape.
//			} else {
//				if (pTree != null && pETElement != null) {
//					bFlag = true;
//				}
//			}
            retVal = bFlag;
        }
        else if (id.equals("MBK_TRANSFORM_TO_CLASS") 
        || id.equals("MBK_TRANSFORM_TO_INTERFACE") 
        || id.equals("MBK_TRANSFORM_TO_ACTOR") 
        || id.equals("MBK_TRANSFORM_TO_DATATYPE") 
        || id.equals("MBK_TRANSFORM_TO_ENUMERATION"))
        { 
            if (id.equals("MBK_TRANSFORM_TO_CLASS") && !elemType.equals("Class"))
            {  //$NON-NLS-2$
                // It's not a class so allow transformation to one of the other types
                bFlag = true;
            }
            else if (id.equals("MBK_TRANSFORM_TO_INTERFACE") && !elemType.equals("Interface"))
            {  //$NON-NLS-2$
                // It's not an interface so allow transformation to one of the other types
                bFlag = true;
            }
            else if (id.equals("MBK_TRANSFORM_TO_ACTOR") && !elemType.equals("Actor"))
            {  //$NON-NLS-2$
                // It's not an actor so allow transformation to one of the other types
                bFlag = true;
            }
            else if (id.equals("MBK_TRANSFORM_TO_DATATYPE") && !elemType.equals("DataType"))
            {  //$NON-NLS-2$
                // It's not a datatype so allow transformation to one of the other types
                bFlag = true;
            }
            else if (id.equals("MBK_TRANSFORM_TO_ENUMERATION") && !elemType.equals("Enumeration"))
            {  //$NON-NLS-2$
                // It's not an enumeration so allow transformation to one of the other types
                bFlag = true;
            }

            retVal = bReadOnly ? false : bFlag;
        }
        
        else if (id.equals("MBK_RESIZE_ELEMENT_TO_CONTEXT"))
        {
            if (pElement instanceof IGraphic)
            {
                // Don't sensitize the button if its a graphic shape.
                bFlag = false;
            }
            
            else
            {
                // This is just a workaround as getHasSelected() 
                //  method always returns false.
		// bFlag = control.getHasSelected(true);

                bFlag = true;
            }

            retVal = bReadOnly ? false : bFlag;
        }

        else if (id.equals("MBK_DIMENSIONAL_RESIZE_ELEMENT"))
        {
            retVal = bReadOnly ? false : graph.hasSelectedNodes();
        }

        else if (id.equals("MBK_SYNCH_ELEMENT_WITH_DATA"))
        { 

            // This is just a workaround as getHasSelected() 
            //  method always returns false.
            // bFlag = control.getHasSelected(true);

            bFlag = true;
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_RESET_PRESENTATION_COLORS_AND_FONTS"))
        { 
            retVal = bReadOnly ? false : true;
        }
        else if (id.equals("MBK_REMOVE_ALL_BENDS") || 
            id.equals("MBK_AUTOROUTE_EDGES") || 
            id.equals("MBK_TOGGLE_EDGE_ORTHOGONALITY"))
        {  //$NON-NLS-2$ //$NON-NLS-3$
            bFlag = control.getHasSelected(true);
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_RESET_LABELS"))
        { 
            if (pETElement != null)
            {
                IDrawEngine pEngine = pETElement.getEngine();
                if (pEngine != null)
                {
                    ILabelManager labelMgr = pEngine.getLabelManager();
                    if (labelMgr != null)
                    {
                        bFlag = true;
                    }
                }
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_DISPLAY_AS_ICON"))
        { 
            if (pElement != null && pElement instanceof IInterface && pDrawEngine != null)
            {
                String engineId = pDrawEngine.getDrawEngineID();
                if (engineId != null && engineId.equals("ClassDrawEngine"))
                { 
                    bFlag = true;
                }
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_DISPLAY_AS_CLASS"))
        { 
            if (pElement != null && pElement instanceof IInterface && pDrawEngine != null)
            {
                String engineId = pDrawEngine.getDrawEngineID();
                if (engineId != null && engineId.equals("InterfaceDrawEngine"))
                { 
                    bFlag = true;
                }
            }
            retVal = bReadOnly ? false : bFlag;


        }
        else if (id.equals("MBK_SHOW_STEREOTYPE_ICONS") || 
            id.equals("MBK_HIDE_STEREOTYPE_ICONS"))
        {  //$NON-NLS-2$
            retVal = bReadOnly ? false : true;
        }
        else if (id.equals("MBK_RESET_EDGES"))
        { 
            if (pDrawEngine != null)
            {
                IEventManager eventMgr = pDrawEngine.getEventManager();
                if (eventMgr != null)
                {
                    // See if we have edges to reset
                    bFlag = eventMgr.hasEdgesToReset();
                }
            }
            retVal = bReadOnly ? false : bFlag;
        }
        else if (id.equals("MBK_EDGEPROPERTIES") || 
            id.equals("MBK_NODEPROPERTIES"))
        {  //$NON-NLS-2$
            IProxyUserInterface pProxy = ProductHelper.getProxyUserInterface();
            if (pProxy != null)
            {
                boolean isVisible = pProxy.getPropertyEditorVisible();
                if (!isVisible)
                {
                    bFlag = true;
                }
            }
            retVal = bFlag;
        }
        else if (id.equals("MBK_CHANGE_SIMILAR_FONT") 
        || id.equals("MBK_CHANGE_SIMILAR_FILL_COLOR") 
        || id.equals("MBK_CHANGE_SIMILAR_BORDER_COLOR") 
        || id.equals("MBK_CHANGE_SIMILAR_FONT_COLOR") 
        || id.equals("MBK_CURRENT_COLOR_AS_DEFAULT"))
        { 
            retVal = bReadOnly ? false : true;
        }
        else
        {
            retVal = bReadOnly ? false : true;
        }

        return retVal;
    }

    public boolean onHandleButton(ActionEvent e, String id)
    {
        boolean bHandled = false;
        IDrawingAreaControl control = getDrawingArea();
        ADGraphWindow window = control.getGraphWindow();
        TSEGraph graph = window.getGraph();
        Object itemClickedOn = m_ContextMenuManager.getContextObject();
        IPresentationElement pPresEle = null;
        IETGraphObject pETElement = null;
        IDrawEngine pDrawEngine = null;
        
        if (itemClickedOn != null && itemClickedOn instanceof TSObject)
        {
            pETElement = TypeConversions.getETGraphObject((TSObject) itemClickedOn);
            pPresEle = TypeConversions.getPresentationElement((TSObject) itemClickedOn);
        }
        if (itemClickedOn != null && itemClickedOn instanceof IETGraphObject)
        {
            pDrawEngine = TypeConversions.getDrawEngine((IETGraphObject) itemClickedOn);
        }
        
        if (id.equals("MBK_GOTO_PARENT_GRAPH"))
        { 
            control.goToParentGraph();
            bHandled = true;
        }
        else if (id.equals("MBK_FIT_TO_WINDOW"))
        { 
            control.fitInWindow();
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_25"))
        { 
            control.zoom(0.25f);
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_50"))
        { 
            control.zoom(0.5f);
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_ACTUAL_SIZE"))
        { 
            control.zoom(1.0f);
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_200"))
        { 
            control.zoom(2.0f);
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_400"))
        { 
            control.zoom(4.0f);
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_CUSTOM_ZOOM"))
        { 
            control.onCustomZoom();
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_IN"))
        { 
            control.zoomIn();
            bHandled = true;
        }
        else if (id.equals("MBK_ZOOM_OUT"))
        { 
            control.zoomOut();
            bHandled = true;
            //} else if (id.equals("MBK_POPUP_LAYOUT_CIRCULAR")) { 
            //	control.setLayoutStyle(ILayoutKind.LK_CIRCULAR_LAYOUT);
            //	bHandled = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_HIERARCHICAL"))
        { 
            control.setLayoutStyle(ILayoutKind.LK_HIERARCHICAL_LAYOUT);
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_ORTHOGONAL"))
        { 
            control.setLayoutStyle(ILayoutKind.LK_ORTHOGONAL_LAYOUT);
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_SYMMETRIC"))
        { 
            control.setLayoutStyle(ILayoutKind.LK_SYMMETRIC_LAYOUT);
            bHandled = true;
            //} else if (id.equals("MBK_POPUP_LAYOUT_TREE")) { 
            //	control.setLayoutStyle(ILayoutKind.LK_TREE_LAYOUT);
            //	bHandled = true;
            //} else if (id.equals("MBK_POPUP_LAYOUT_RELAYOUT")) { 
            //	control.setLayoutStyle(ILayoutKind.LK_GLOBAL_LAYOUT);
            //	bHandled = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_INCREMENTAL"))
        { 
            control.setLayoutStyle(ILayoutKind.LK_INCREMENTAL_LAYOUT);
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_SEQUENCEDIAGRAM"))
        { 
            control.setLayoutStyle(ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT);
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_LAYOUT_PROPERTIES"))
        { 
            boolean isOpen = control.getIsLayoutPropertiesDialogOpen();
            if (isOpen)
            {
                control.layoutPropertiesDialog(false);
            }
            else
            {
                control.layoutPropertiesDialog(true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_COPY"))
        { 
            control.copy();
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_CUT"))
        { 
            control.cut();
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_DELETE"))
        { 
            // Post an action to delete the selected elements.  We can't do this
            // right away 'cause TS crashes if we keep and instance of the objects
            // we're about to delete.  So we have to wait until the menu goes away
            // (it's holding onto the one underneigh the pointer).
            ISimpleAction action = new SimpleAction();
            action.setKind(DiagramAreaEnumerations.SAK_DELETE_SELECTED);
            control.postDelayedAction(action);
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_PASTE"))
        { 
            control.paste();
            bHandled = true;
        }
        else if (id.equals("MBK_TOGGLE_LOCK_EDIT"))
        { 
            toggleLockEditingOnAllSelectedNodes(pPresEle);
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_SELECTALL"))
        { 
            control.selectAll(true);
        }
        else if (id.equals("MBK_POPUP_SELECTALL_SIMILAR"))
        { 
            control.selectAllSimilar();
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_INVERTSELECTION"))
        { 
            control.invertSelection();
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_BROWSE_PICTURES"))
        { 
            //To do implement
        }
        else if (id.equals("MBK_POPUP_CLEAR_PICTURE"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_POPUP_PICTURE_TILE"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_SYNCH_ELEMENT_WITH_DATA"))
        { 
            control.syncElements(true);
            bHandled = true;
        }
        else if (id.equals("MBK_RESET_PRESENTATION_COLORS_AND_FONTS"))
        { 
            //control.resetColorsAndFonts();
        }
        else if (id.equals("MBK_REMOVE_ALL_BENDS"))
        { 
            createActionForAllSelectedEdges(pPresEle, DiagramAreaEnumerations.SPAK_DISCARDALLBENDS);
            bHandled = true;
        }
        else if (id.equals("MBK_AUTOROUTE_EDGES") || id.equals("MBK_TOGGLE_EDGE_ORTHOGONALITY"))
        {  //$NON-NLS-2$
            IAutoRoutingAction action = new AutoRoutingAction();
            ETList < IPresentationElement > selElems = control.getSelected();
            boolean bFoundEdge = false;
            if (selElems != null)
            {
                int count = selElems.size();
                for (int i = 0; i < count; i++)
                {
                    IPresentationElement thisItem = selElems.get(i);
                    if (thisItem instanceof IEdgePresentation)
                    {
                        action.add((IEdgePresentation) thisItem);
                        bFoundEdge = true;
                    }
                }
            }
            
            if (bFoundEdge)
            {
                if (id.equals("MBK_AUTOROUTE_EDGES"))
                { 
                    action.setKind(IAutoRoutingActionKind.ARAK_AUTOMATIC);
                }
                else if (id.equals("MBK_TOGGLE_EDGE_ORTHOGONALITY"))
                { 
                    action.setKind(IAutoRoutingActionKind.ARAK_TOGGLE_ORTHOGONALITY);
                }
                
                control.postDelayedAction(action);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_GRAPHPROPERTIES"))
        { 
            boolean isOpen = control.getIsGraphPreferencesDialogOpen();
            control.graphPreferencesDialog(isOpen ? false : true);
            bHandled = true;
        }
        else if (id.equals("MBK_EDGEPROPERTIES") || id.equals("MBK_NODEPROPERTIES"))
        {  //$NON-NLS-2$
            IProxyUserInterface pProxy = ProductHelper.getProxyUserInterface();
            if (pProxy != null)
            {
                pProxy.setPropertyEditorVisible(true);
                IPropertyEditor pEditor = ProductHelper.getPropertyEditor();
                if (pEditor != null && pPresEle != null)
                {
                    IElement pEle = pPresEle.getFirstSubject();
                    if (pEle != null)
                    {
                        pEditor.loadElement(pEle);
                    }
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_FIND_FROM_NODE"))
        { 
            if (itemClickedOn instanceof TSEdge)
            {
                GetHelper.gotoNodeEnd(window, (TSEdge) itemClickedOn, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_FIND_TO_NODE"))
        { 
            if (itemClickedOn instanceof TSEdge)
            {
                GetHelper.gotoNodeEnd(window, (TSEdge) itemClickedOn, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_GOTO_CHILD_GRAPH"))
        { 
            control.goToChildGraph((TSNode) itemClickedOn);
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_EXPAND"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_NODE_COLLAPSE"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_NODE_CREATE_CHILD_GRAPH"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_NODE_DELETE_CHILD_GRAPH"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_NODE_HIDE_CHILDREN_ONE_LEVEL"))
        { 
            if (pPresEle != null)
            {
                control.hide(pPresEle, 1, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_HIDE_CHILDREN_N_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.hide(pPresEle, -1, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_HIDE_CHILDREN_ALL_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.hide(pPresEle, INT_MAX, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_HIDE_PARENTS_ONE_LEVEL"))
        { 
            if (pPresEle != null)
            {
                control.hide(pPresEle, 1, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_HIDE_PARENTS_N_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.hide(pPresEle, -1, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_HIDE_PARENTS_ALL_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.hide(pPresEle, this.INT_MAX, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNHIDE_CHILDREN_ONE_LEVEL"))
        { 
            if (pPresEle != null)
            {
                control.unhide(pPresEle, 1, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNHIDE_CHILDREN_N_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.unhide(pPresEle, -1, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNHIDE_CHILDREN_ALL_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.unhide(pPresEle, FIND_DEPTH_ALL, true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNHIDE_PARENTS_ONE_LEVEL"))
        { 
            if (pPresEle != null)
            {
                control.unhide(pPresEle, 1, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNHIDE_PARENTS_N_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.unhide(pPresEle, -1, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNHIDE_PARENTS_ALL_LEVELS"))
        { 
            if (pPresEle != null)
            {
                control.unhide(pPresEle, FIND_DEPTH_ALL, false);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_FOLD_CHILDREN_ONE_LEVEL"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                GetHelper.fold(window, (TSNode) itemClickedOn, 1, true);
                control.setIsDirty(true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_FOLD_CHILDREN_N_LEVELS"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                //to do implement
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_FOLD_CHILDREN_ALL_LEVELS"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                GetHelper.fold(window, (TSNode) itemClickedOn, INT_MAX, true);
                control.setIsDirty(true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_FOLD_PARENTS_ONE_LEVEL"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                GetHelper.fold(window, (TSNode) itemClickedOn, 1, false);
                control.setIsDirty(true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_FOLD_PARENTS_N_LEVELS"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                //to do implement
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_FOLD_PARENTS_ALL_LEVELS"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                GetHelper.fold(window, (TSNode) itemClickedOn, INT_MAX, false);
                control.setIsDirty(true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_NODE_UNFOLD"))
        { 
            if (itemClickedOn instanceof TSNode)
            {
                control.unfoldNode((TSNode) itemClickedOn);
                control.setIsDirty(true);
            }
            bHandled = true;
        }
        else if (id.equals("MBK_POPUP_COLOR"))
        { 
            //to do implement
        }
        else if (id.equals("MBK_POPUP_FIND_IN_PROJECTTREE"))
        { 
            // this should not happen now as we are using SelectInModel action
            
//			if (itemClickedOn instanceof TSGraphObject) {
//				IElement modEle = TypeConversions.getElement((TSGraphObject) itemClickedOn);
//				if (modEle != null) {
//					bHandled = ProjectUtil.findElementInProjectTree(modEle);
//				}
//				else
//				{
//					IDiagram diagram = getDrawingArea().getDiagram();
//					bHandled = ProjectUtil.findElementInProjectTree(diagram);
//				}
            
//			}
        }
        else if (id.equals("MBK_TRANSFORM_TO_CLASS") ||
            id.equals("MBK_TRANSFORM_TO_INTERFACE") ||
            id.equals("MBK_TRANSFORM_TO_ACTOR") ||
            id.equals("MBK_TRANSFORM_TO_DATATYPE") ||
            id.equals("MBK_TRANSFORM_TO_ENUMERATION"))
        {
            if (pETElement != null)
            {
                if (id.equals("MBK_TRANSFORM_TO_CLASS"))
                { 
                    control.transform(pETElement, "Class"); 
                }
                else if (id.equals("MBK_TRANSFORM_TO_INTERFACE"))
                { 
                    control.transform(pETElement, "Interface"); 
                }
                else if (id.equals("MBK_TRANSFORM_TO_ACTOR"))
                { 
                    control.transform(pETElement, "Actor"); 
                }
                else if (id.equals("MBK_TRANSFORM_TO_DATATYPE"))
                { 
                    control.transform(pETElement, "DataType"); 
                }
                else if (id.equals("MBK_TRANSFORM_TO_ENUMERATION"))
                { 
                    control.transform(pETElement, "Enumeration"); 
                }
            }
            bHandled = true;
        }
        
        else if (id.equals("MBK_RESIZE_ELEMENT_TO_CONTEXT"))
        {
            control.sizeToContents(true);
            control.setIsDirty(true);
            bHandled = true;
        }
        
        else if (id.equals("MBK_DIMENSIONAL_RESIZE_ELEMENT"))
        {
            if (control.resizeDimensions())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
        else if (id.equals("MBK_ALIGN_LEFT"))
        {
            if (control.alignLeft())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
        else if (id.equals("MBK_ALIGN_HCENTER"))
        {
            if (control.alignHorizontalCenter())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
        else if (id.equals("MBK_ALIGN_RIGHT"))
        {
            if (control.alignRight())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
        else if (id.equals("MBK_ALIGN_TOP"))
        {
            if (control.alignTop())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
        else if (id.equals("MBK_ALIGN_VCENTER"))
        {
            if (control.alignVerticalCenter())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
        else if (id.equals("MBK_ALIGN_BOTTOM"))
        {
            if (control.alignBottom())
                control.setIsDirty(true);
            
            bHandled = true;
        }
        
// disabled - feature to be added with Meteora
//        else if (id.equals("MBK_DISTRIBUTE_LEFT_EDGE"))
//        {
//            if (control.distributeLeftEdge())
//                control.setIsDirty(true);
//            
//            bHandled = true;
//        }
//        
//        else if (id.equals("MBK_DISTRIBUTE_HCENTER"))
//        {
//            if (control.distributeHorizontalCenter())
//                control.setIsDirty(true);
//            
//            bHandled = true;
//        }
//        
//        else if (id.equals("MBK_DISTRIBUTE_RIGHT_EDGE"))
//        {
//            if (control.distributeRightEdge())
//                control.setIsDirty(true);
//            
//            bHandled = true;
//        }
//        
//        else if (id.equals("MBK_DISTRIBUTE_TOP_EDGE"))
//        {
//            if (control.distributeTopEdge())
//                control.setIsDirty(true);
//            
//            bHandled = true;
//        }
//        
//        else if (id.equals("MBK_DISTRIBUTE_VCENTER"))
//        {
//            if (control.distributeVerticalCenter())
//                control.setIsDirty(true);
//            
//            bHandled = true;
//        }
//        
//        else if (id.equals("MBK_DISTRIBUTE_BOTTOM_EDGE"))
//        {
//            if (control.distributeBottomEdge())
//                control.setIsDirty(true);
//            
//            bHandled = true;
//        }
        
        else if (id.equals("MBK_RESET_LABELS"))
        { 
            if (pETElement != null && pDrawEngine != null)
            {
                ILabelManager labelMgr = pDrawEngine.getLabelManager();
                if (labelMgr != null)
                {
                    labelMgr.resetLabels();
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_DISPLAY_AS_ICON"))
        { 
            if (pETElement != null)
            {
                control.resetDrawEngine(pETElement, "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation"); 
            }
            bHandled = true;
        }
        else if (id.equals("MBK_DISPLAY_AS_CLASS"))
        { 
            if (pETElement != null)
            {
                control.resetDrawEngine(pETElement, "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation"); 
            }
            bHandled = true;
        }
        else if (id.equals("MBK_PACKAGE_NAME_TO_CENTER") || id.equals("MBK_PACKAGE_NAME_TO_TAB"))
        {  //$NON-NLS-2$
            if (pETElement != null && pDrawEngine != null)
            {
                if (pDrawEngine instanceof IPackageDrawEngine)
                {
                    IPackageDrawEngine packDE = (IPackageDrawEngine) pDrawEngine;
                    packDE.invalidate();
                    if (id.equals("MBK_PACKAGE_NAME_TO_CENTER"))
                    { 
                        packDE.setNameInTab(false);
                    }
                    else
                    {
                        packDE.setNameInTab(true);
                    }
                    pDrawEngine.invalidate();
                    getDrawingArea().refreshRect(pDrawEngine.getBoundingRect());
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_SHOW_STEREOTYPE_ICONS") || id.equals("MBK_HIDE_STEREOTYPE_ICONS"))
        {  //$NON-NLS-2$
            if (pETElement != null && pDrawEngine != null)
            {
                ICompartment pComp = pDrawEngine.findCompartmentByCompartmentID("StereotypeCompartment"); 
                if (pComp != null && pComp instanceof IStereotypeCompartment)
                {
                    IStereotypeCompartment pStereoComp = (IStereotypeCompartment) pComp;
                    if (id.equals("MBK_SHOW_STEREOTYPE_ICONS"))
                    { 
                        pStereoComp.setShowStereotypeIcons(true);
                    }
                    else
                    {
                        pStereoComp.setShowStereotypeIcons(false);
                    }
                    pDrawEngine.invalidate();
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_RESET_EDGES"))
        { 
            if (pETElement != null && pDrawEngine != null)
            {
                IEventManager pEventMgr = pDrawEngine.getEventManager();
                if (pEventMgr != null)
                {
                    pEventMgr.resetEdges();
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_DISTRIBUTE_PORT_INTERFACES"))
        { 
            if (pETElement != null && pDrawEngine != null)
            {
                String drawEngineId = pDrawEngine.getDrawEngineID();
                if (drawEngineId.equals("ComponentDrawEngine"))
                { 
                    if (pDrawEngine instanceof IComponentDrawEngine)
                    {
                        ((IComponentDrawEngine) pDrawEngine).distributeInterfacesOnAllPorts(true);
                    }
                }
                else if (drawEngineId.equals("PortDrawEngine"))
                { 
                    IEventManager eventMgr = pDrawEngine.getEventManager();
                    if (eventMgr != null && eventMgr instanceof IADInterfaceEventManager)
                    {
                        ((IADInterfaceEventManager) eventMgr).distributeAttachedInterfaces(true);
                    }
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_PORTS_TO_TOP") ||
            id.equals("MBK_PORTS_TO_BOTTOM") ||
            id.equals("MBK_PORTS_TO_LEFT") ||
            id.equals("MBK_PORTS_TO_RIGHT"))
        {  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (pETElement != null && pDrawEngine != null)
            {
                int portPosition = QuadrantKindEnum.QK_RIGHT;
                if (id.equals("MBK_PORTS_TO_TOP"))
                {
                    portPosition = QuadrantKindEnum.QK_TOP;
                }
                else if (id.equals("MBK_PORTS_TO_BOTTOM"))
                {
                    portPosition = QuadrantKindEnum.QK_BOTTOM;
                }
                else if (id.equals("MBK_PORTS_TO_LEFT"))
                {
                    portPosition = QuadrantKindEnum.QK_LEFT;
                }
                
                String drawEngineId = pDrawEngine.getDrawEngineID();
                if (drawEngineId.equals("ComponentDrawEngine"))
                {
                    if (pDrawEngine instanceof IComponentDrawEngine)
                    {
                        ((IComponentDrawEngine) pDrawEngine).movePortsToSide(portPosition);
                    }
                }
                else if (drawEngineId.equals("PortDrawEngine") && pPresEle != null)
                {
                    ETList<IPresentationElement> selectedPorts = new ETArrayList<IPresentationElement>();
                    selectedPorts.add(pPresEle);
                    ((IPortDrawEngine) pDrawEngine).movePortsToSide(portPosition, selectedPorts);
                }
            }
            bHandled = true;
        }
        else if (id.equals("MBK_SHOW_AS_ICONIC"))
        { 
            if (pETElement != null)
            {
                control.resetDrawEngine(pETElement, "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI BoundaryControllerOrEntity"); 
            }
            bHandled = true;
        }
        else if (id.equals("MBK_SHOW_AS_NONICONIC"))
        {
            if (pETElement != null)
            {
                // display classes with entity/control/boundary stereotype in non-iconic view using class draw engine just like utility class
                control.resetDrawEngine(pETElement, "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UtilityClass");
            }
            bHandled = true;
        }
        else if (id.equals("MBK_CHANGE_SIMILAR_FONT"))
        {
            if (pDrawEngine != null)
            {
                Font oldFont = null;
                IDrawingPropertyProvider pDrawingPropertyProvider = (IDrawingPropertyProvider) pDrawEngine;
                ETList < IDrawingProperty > pDrawingProperties = pDrawingPropertyProvider.getDrawingProperties();
                String resourceName = pDrawEngine.getResourceName(UIResources.CK_FONT);
                for (int i = 0; i < pDrawingProperties.size(); i++)
                {
                    IDrawingProperty pDrawingProperty = pDrawingProperties.get(i);
                    if (pDrawingProperty instanceof IFontProperty && pDrawingProperty.getResourceName().equals(resourceName))
                    {
                        IFontProperty pFontProperty = (IFontProperty)pDrawingProperty;
                        int style = Font.PLAIN;
                        if (pFontProperty.getItalic())
                        {
                            style |= Font.ITALIC;
                        }
                        if (pFontProperty.getWeight() >= 700)
                        {
                            style |= Font.BOLD;
                        }
                        oldFont = new Font(pFontProperty.getFaceName(), style, pFontProperty.getSize());
                    }
                }
                
                Font font = null;
                if (oldFont != null)
                {
                    font = FontChooser.selectFont(oldFont);
                }
                else
                {
                    font = FontChooser.selectFont();
                }
                
                /*Added by Smitha- Fix for bug # 6266911*/
                String engineID = pDrawEngine.getDrawEngineID();
                ETList < IPresentationElement > elements = getDrawingArea().getSelected();
                if (elements != null)
                {
                    Iterator < IPresentationElement > iter = elements.iterator();
                    while (iter.hasNext())
                    {
                        IPresentationElement element = iter.next();
                        IDrawEngine pCurDrawEngine = TypeConversions.getDrawEngine(element);
                        if (font != null && pCurDrawEngine != null)
                        {
                            pCurDrawEngine.setFontResource(UIResources.CK_FONT, font);
                        }
                    }
                }
                /*Added by Smitha*/
            }
            bHandled = true;
        }
        else if (id.equals("MBK_CHANGE_SIMILAR_FILL_COLOR") || id.equals("MBK_CHANGE_SIMILAR_BORDER_COLOR") || id.equals("MBK_CHANGE_SIMILAR_FONT_COLOR"))
        {  //$NON-NLS-2$ //$NON-NLS-3$
            if (pDrawEngine != null /*&& color != null*/)
            {
                IDrawingPropertyProvider pDrawingPropertyProvider = (IDrawingPropertyProvider) pDrawEngine;
                int resourceKind;
                String title;
                
                if (id.equals("MBK_CHANGE_SIMILAR_FONT_COLOR"))
                {
                    resourceKind = UIResources.CK_TEXTCOLOR;                    
                }
                else if (id.equals("MBK_CHANGE_SIMILAR_FILL_COLOR"))
                {
                    resourceKind = UIResources.CK_FILLCOLOR;
                }
                else
                {  // case of id.equals("MBK_CHANGE_SIMILAR_BORDER_COLOR")
                    resourceKind = UIResources.CK_BORDERCOLOR;
                }
                title = NbBundle.getMessage(ADCoreEngine.class, id);
                String resourceName = pDrawEngine.getResourceName(resourceKind);
                
                Color oldColor = null;
                ETList < IDrawingProperty > pDrawingProperties = pDrawingPropertyProvider.getDrawingProperties();
                for (int i = 0; i < pDrawingProperties.size(); i++)
                {
                    IDrawingProperty pDrawingProperty = pDrawingProperties.get(i);
                    if (pDrawingProperty.getResourceType().equals("color") && pDrawingProperty.getResourceName().equals(resourceName))
                    {
                        oldColor = new Color(((IColorProperty)pDrawingProperty).getColor());
                        break;
                    }
                }
                Color color = JColorChooser.showDialog(null, title, oldColor);
                if(color != null)
                {
                    String engineID = pDrawEngine.getDrawEngineID();
                    ETList < IPresentationElement > elements = getDrawingArea().getSelected();
                    if (elements != null)
                    {
                        Iterator < IPresentationElement > iter = elements.iterator();
                        while (iter.hasNext())
                        {
                            IPresentationElement element = iter.next();
                            IDrawEngine pCurDrawEngine = TypeConversions.getDrawEngine(element);
                            if (pCurDrawEngine != null)
                            {
                                pCurDrawEngine.setColorResource(resourceKind, color);
                            }
                        }
                    }
                }
            }
            bHandled = true;
        }
        
        return bHandled;
    }

	/**
	 * Tells all nodes to lock their editing
	 */
	private void toggleLockEditingOnAllSelectedNodes(IPresentationElement pPresEle) {
		
		IDrawingAreaControl control = getDrawingArea();
		
		if (pPresEle instanceof INodePresentation && control != null) {
		
			INodePresentation pNodePE = (INodePresentation) pPresEle;
			boolean lockEdit = pNodePE.getLockEdit();

			// Get all the nodes and set their lock edit
			ETList < IPresentationElement > allSelected = control.getSelected();
			
			if (allSelected != null) {
				int count = allSelected.size();
				
				for (int i = 0; i < count; i++) {
					IPresentationElement thisItem = allSelected.get(i);
					if (thisItem instanceof INodePresentation) {
						((INodePresentation) thisItem).setLockEdit(lockEdit ? false : true);
					}
				}
			}
		}
	}

	/**
	 * Posts a delayed action for all selected edges
	 */
	private void createActionForAllSelectedEdges(IPresentationElement pPresEle, int nKind) {
		try {
			IDrawingAreaControl control = getDrawingArea();
			ISimplePresentationAction cpAction = new SimplePresentationAction();
			if (cpAction != null && control != null) {
				IEdgePresentation pEdgePE = pPresEle instanceof IEdgePresentation ? (IEdgePresentation) pPresEle : null;

				// Prepare the delayed action
				cpAction.setKind(nKind);

				// Get all the edges and add them to the delayed action
				ETList < IPresentationElement > pAllSelected = control.getSelected();
				long count = pAllSelected != null ? pAllSelected.getCount() : 0;

				// Add the selected edge first so that if we act on multiple edges the
				// item right clicked on is the first in the list.
				if (pEdgePE != null) {
					cpAction.add(pEdgePE);
				}

				for (int i = 0; i < count; i++) {
					IPresentationElement pThisItem = pAllSelected.item(i);

					IEdgePresentation pEdge = pThisItem instanceof IEdgePresentation ? (IEdgePresentation) pThisItem : null;
					if (pEdge != null && pEdge != pEdgePE) {
						// Add this edge to our list
						cpAction.add(pEdge);
					}
				}

				// Post the delayed action
				control.postDelayedAction(cpAction);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Makes sure that if the element being dropped is not in the same project as the element
	 * that owns this diagram, and element or package import is created.
         * 
	 *
	 * @param pElementBeingDropped[in]     The element being dropped
	 * @param pChangedElement[in]          ignored
	 * @param bCancelThisElement[in]       
	 *
	 * @return HRESULT
	 *
	 */
	public ETPairT<Boolean, IElement> processOnDropElement(IElement pElementBeingDropped)
        {
            boolean cancelThisEle = false;
            IElement pChangedElement = pElementBeingDropped;
            
            IElement owner = getOwner();
            if (owner != null)
            {

		// The below check has been replaced by check for the same project. We are interested in the topmost owner 
		//	i.e project rather than just the owner of an element.	
				
		//  boolean isSame = owner.isSame(pElementBeingDropped);
                    boolean isSame = owner.inSameProject(pElementBeingDropped);
                if (!isSame)
                {
                    // Only AutonomousElements can be imported across Projects
                    if (pElementBeingDropped instanceof IAutonomousElement)
                    {
                        IDirectedRelationship rel = MetaLayerRelationFactory.instance().establishImportIfNeeded(owner, pElementBeingDropped);
                    }
                    else
                    {
                        cancelThisEle = true;
                    }
                }
                    
            }
            return new ETPairT < Boolean, IElement > (new Boolean(cancelThisEle), pChangedElement);
        }

	public ContextMenuActionClass createMenuAction(String text, String menuID) {
		return new ContextMenuActionClass(this, text, menuID);
	}

	public ContextMenuActionClass createMenuAction(String text, String menuID, int style )
	{
	   ContextMenuActionClass menu = new ContextMenuActionClass(this, text, menuID);
	   if( menu != null )
	   {
		  menu.setStyle( style );
	   }
      
	   return menu;
	}

	
	public ContextMenuActionClass createMenuAction(String text, String menuID, String shortcut) {
		ContextMenuActionClass menu = new ContextMenuActionClass(this, text, menuID);
		menu.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));
		return menu;
	}
	
	
	public ContextMenuActionClass createMenuAction(String text, String menuID, int style, String shortcut)
	{
	   ContextMenuActionClass menu = new ContextMenuActionClass(this, text, menuID);
	   if( menu != null )
	   {
		  menu.setStyle( style );
		  menu.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));
	   }
      
	   return menu;
	}
	
	
	public void addNormalAccelerators(ETList<String> accelsToRegister, boolean bAddLayoutAccelerators)
	{
//		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_DOWN);
//		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_UP);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_TAB);
                
                String copy = IDrawingAreaAcceleratorKind.DAVK_COPY;
                
                accelsToRegister.add(copy );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CUT );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_PASTE );
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_SAVE );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL_SIMILAR );

		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_RESIZE_TO_CONTENTS );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_SHOW_FRIENDLY_NAMES );
                
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_EXPORT_AS_IMAGE );
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_FIT_TO_WINDOW );
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_PRINT_PREVIEW );

                addStackingActionAccelerators (accelsToRegister) ;
                        
		if (bAddLayoutAccelerators)
		{
			// Add layout accelerators
			addLayoutAccelerators(accelsToRegister);
		}
                
                //add other toolbar accelerators
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_RELATIONSHIP_DISCOVERY );
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_SELECT_MODE );
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_PAN_MODE );
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_NAVIGATE_LINK_MODE) ;
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_ZOOM_INTERACTIVELY_MODE) ;
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_ZOOM_WITH_MARQUEE_MODE) ;
                accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_DIAGRAM_SYNC) ;

		// Add the nodes and edges that are available to all normal diagrams
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_DEPENDENCY);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_USECASE);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_INTERFACE);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_PACKAGE);

		// Add parameters
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_PARAMETER);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_OPERATION); 
	}
	
        public void addStackingActionAccelerators (ETList<String> accelsToRegister) {
            accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_MOVE_BACKWARD) ;
            accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_MOVE_FORWARD) ;
            accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_MOVE_TO_BACK) ;
            accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_MOVE_TO_FRONT) ;
        }
        
	public void addLayoutAccelerators(ETList<String> accelsToRegister)
	{
		//accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_CIRCULAR_STYLE );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_HIERARCHICAL_STYLE );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_INCREMENTAL_LAYOUT );
//		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_RELAYOUT );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_ORTHOGONAL_STYLE );
//		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_TREE_STYLE );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SYMMETRIC_STYLE );
	}
	
	public void registerAcceleratorsByType(ETList<String> accelsToRegister)
	{
		IAcceleratorManager pManager = null;
		JComponent hwnd = null;
		IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
		if (pDrawingAreaControl != null && pDrawingAreaControl instanceof JComponent)
		{
			hwnd = (JComponent)pDrawingAreaControl;
		}
	
		pManager = ProductHelper.getAcceleratorManager();
		if(pManager != null && hwnd != null)
		{
			ETList<String> pAcceleratorsOnlyWhenHaveFocus = new ETArrayList<String>();
			ETList<String> pAcceleratorsToReceiveAlways = new ETArrayList<String>();
	
			for (int i = 0; i < accelsToRegister.size(); i++)
			{
				String nKind = (String)accelsToRegister.get(i);
				boolean bFoundAccel = true;
	
				// register accels when we have focus
				if (
//                                      nKind.equals(IDrawingAreaAcceleratorKind.DAVK_DOWN) ||
//					nKind.equals(IDrawingAreaAcceleratorKind.DAVK_UP) ||
					nKind.equals(IDrawingAreaAcceleratorKind.DAVK_TAB) ||
					nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ATTRIBUTE) ||
					nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_OPERATION) ||
					nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_PARAMETER) ||
                                        
                                        //model element creation
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_AGGREGATION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_COMPOSITION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_IMPLEMENTATION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ASSOCIATION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_SYNCHRONUS_MESSAGE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_DEPENDENCY) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTIVITYEDGE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_GENERALIZATION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_STATETRANSITION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ABSTRACTION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_INVOCATION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTOR) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_DECISION) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_CLASS) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_SIMPLESTATE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ERENTITY) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_PACKAGE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_INTERFACE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_USECASE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_MESSAGE_AFTER) ||
                                        
                                         // toolbars - diagram layout
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_CIRCULAR_STYLE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_HIERARCHICAL_STYLE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_INCREMENTAL_LAYOUT) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_RELAYOUT) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_ORTHOGONAL_STYLE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SEQUENCE_DIAGRAM_LAYOUT) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_TREE_STYLE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SYMMETRIC_STYLE) ||
                                        
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL_SIMILAR) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_RESIZE_TO_CONTENTS) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_SHOW_FRIENDLY_NAMES) ||
                                        
                                        // Toolbar shortcuts
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_EXPORT_AS_IMAGE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_FIT_TO_WINDOW) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_PRINT_PREVIEW) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_BACKWARD) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_FORWARD) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_TO_BACK) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_TO_FRONT) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_SELECT_MODE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_PAN_MODE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_NAVIGATE_LINK_MODE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_ZOOM_INTERACTIVELY_MODE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_ZOOM_WITH_MARQUEE_MODE) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_DIAGRAM_SYNC) ||
                                        nKind.equals(IDrawingAreaAcceleratorKind.DAVK_RELATIONSHIP_DISCOVERY)
                                    )
				{
					pAcceleratorsOnlyWhenHaveFocus.add(nKind);
				}
				else
				{
					bFoundAccel = false;
				}
	
				if (!bFoundAccel)
				{
					bFoundAccel = true;
	
					// register accels for all the time, even if we don't have focus
					if (nKind.equals(IDrawingAreaAcceleratorKind.DAVK_COPY) ||
                                            nKind.equals(IDrawingAreaAcceleratorKind.DAVK_CUT) ||
                                            nKind.equals(IDrawingAreaAcceleratorKind.DAVK_PASTE) ||
                                            nKind.equals(IDrawingAreaAcceleratorKind.DAVK_SAVE)
                                            )
					{
						pAcceleratorsToReceiveAlways.add(nKind);
					}
					else
					{
						bFoundAccel = false;
					}
				}
			}
	
			// Now register the accelerators
			pManager.register(hwnd, this, pAcceleratorsOnlyWhenHaveFocus, false);
			pManager.register(hwnd, this, pAcceleratorsToReceiveAlways, true);
                        
                        // Register Ctrl-Enter (creating new node action)
                        pManager.register(hwnd, this, KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK, true);
		}
	}
	
	public String getInitStringForButton(String sButtonID)
	{
		String returnString = "";
		IDrawingAreaControl pDrawingAreaControl = getDrawingArea();

		if (sButtonID !=null && sButtonID.length() > 0 && pDrawingAreaControl != null)
		{
			// Get the current diagram kind
			int nDiagramKind = pDrawingAreaControl.getDiagramKind();
			IPresentationTypesMgr pPresentationTypesMgr = ProductHelper.getPresentationTypesMgr();

			if (pPresentationTypesMgr != null)
			{
				// Make sure the file is valid
				boolean bIsValid = pPresentationTypesMgr.validateFile();
				if (bIsValid)
				{
					String initString = pPresentationTypesMgr.getButtonInitString(sButtonID, nDiagramKind);
					PresentationTypeDetails nObjectKind = pPresentationTypesMgr.getInitStringDetails(initString, nDiagramKind);

					if (nObjectKind.getObjectKind() != TSGraphObjectKind.TSGOK_INVALID && initString != null && initString.length() > 0 )
					{
						returnString = initString;
					}
				}
			}
		}

		return returnString;
	}
	
	public IETPoint getLogicalCenter()
	{
		IETPoint pPoint = null;
		IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
   
		if (pDrawingAreaControl != null)
		{
			ADGraphWindow pADGraphWindow = pDrawingAreaControl.getGraphWindow();
			Rectangle rectClient = pADGraphWindow.bounds();
			pPoint = new ETPoint((int)rectClient.getCenterX(), (int)rectClient.getCenterY());
			pPoint = pDrawingAreaControl.deviceToLogicalPoint(pPoint);
		}
		return pPoint;
	}
        
        public Point getAcceleratorOffset ()
        {
            return m_AcceleratorOffset;
        }
        
        public void setAcceleratorOffset (Point newPoint)
        {
            this.m_AcceleratorOffset = newPoint;
        }
	
	public boolean handleRelationAccelerator(String accelerator) {
            boolean retval = false;
            int count = 0;
            IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
            ETList<IPresentationElement> pGraphics = pDrawingAreaControl.getSelected();
            ETList<IPresentationElement> pNodeGraphics = new ETArrayList<IPresentationElement>();
            if ( pGraphics != null && pNodeGraphics != null) {
                count = pGraphics.size();
                for (int idx = 0 ; idx < count; idx++) {
                    IPresentationElement pItem = pGraphics.get(idx);
                    if ( pItem != null && pItem instanceof INodePresentation) {
                        pNodeGraphics.add(pItem);
                    }
                }
            }
            
            count = pNodeGraphics.size();
            if ( count == 2 ) {
                IPresentationElement pGraphic1 = pNodeGraphics.get(0);
                IPresentationElement pGraphic2 = pNodeGraphics.get(1);
                
                if (pGraphic1 != null && pGraphic2 != null) {
                    IPresentationElement pFromGraphic = null;
                    IPresentationElement pToGraphic = null;
                    IElement pElement1 = pGraphic1.getFirstSubject();
                    IElement pElement2 = pGraphic2.getFirstSubject();
                    
                    if (pElement1 != null && pElement2 != null) {
                        String eltype1 = pElement1.getElementType();
                        String eltype2 = pElement2.getElementType();
                        
                        boolean proceed = true;
                        
                        String resultingInitString = "";
                        String sLinkType = "";
                        
                        IProject pProject = pElement1.getProject();
                        
                        
                        if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ABSTRACTION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_ABSTRACTION");
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                            sLinkType ="Abstraction";
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_GENERALIZATION)) {
                            // Make sure both are classifiers
                            proceed = false;
                            if (pElement1 instanceof IClassifier && pElement1 instanceof IClassifier) {
                                IClassifier pFromClassifier = null;
                                if(pElement1 instanceof IClassifier) {
                                    pFromClassifier = (IClassifier)pElement1;
                                }
                                IClassifier pToClassifier = null;
                                if(pElement2 instanceof IClassifier) {
                                    pToClassifier = (IClassifier)pElement2;
                                }
                                
                                if((pFromClassifier != null) && (pToClassifier != null)) {
                                    boolean bIsSame = pFromClassifier.isSame(pToClassifier);
                                    if (!bIsSame) {
                                        resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_GENERALIZATION");
                                        sLinkType ="Generalization";
                                        proceed = true;
                                        pFromGraphic = pGraphic1;
                                        pToGraphic = pGraphic2;
                                    }
                                }
                            }
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_IMPLEMENTATION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_IMPLEMENTATION");
                            sLinkType ="Implementation";
                            if (eltype1.equals("Class") && eltype2.equals("Interface")) {
                                proceed = true;
                                pFromGraphic = pGraphic1;
                                pToGraphic = pGraphic2;
                            } else if (eltype1.equals("Interface") && eltype2.equals("Class")) {
                                proceed = true;
                                pFromGraphic = pGraphic2;
                                pToGraphic = pGraphic1;
                            } else {
                                proceed = false;
                            }
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ASSOCIATION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_ASSOCIATION_NN_NN");
                            sLinkType ="Association";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_DEPENDENCY)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_DEPENDENCY");
                            sLinkType ="Dependency";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_REALIZATION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_REALIZE");
                            sLinkType ="Realization";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_AGGREGATION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_AGGREGATION_AG_NN");
                            sLinkType ="Aggregation";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_COMPOSITION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_AGGREGATION_CO_NN");
                            sLinkType ="Composition";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTIVITYEDGE)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_ACTIVITYEDGE");
                            sLinkType ="ActivityEdge";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        } else if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_STATETRANSITION)) {
                            resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_STATE_TRANSITION");
                            sLinkType ="Transition";
                            proceed = true;
                            pFromGraphic = pGraphic1;
                            pToGraphic = pGraphic2;
                        }
                        
                        if ( proceed &&
                                resultingInitString.length() > 0 &&
                                pFromGraphic != null &&
                                pToGraphic != null  ) {
                            TSEdge pNewEdge = null;
                            TSNode pFromNode = TypeConversions.getOwnerNode(pFromGraphic);
                            TSNode pToNode = TypeConversions.getOwnerNode(pToGraphic);
                            
                            if ( pFromNode != null && pToNode != null ) {
                                try {
                                    pNewEdge = pDrawingAreaControl.addEdge( resultingInitString,
                                            pFromNode,
                                            pToNode,
                                            true,
                                            true);
                                } catch(Exception e) {
                                    pNewEdge = null;
                                }
                                
                                if (pNewEdge != null) {
                                    retval = true;
                                    
                                    IEdgePresentation pEdgePresentation = TypeConversions.getEdgePresentation(pNewEdge);
                                    if (pEdgePresentation != null) {
                                        pEdgePresentation.autoRoute(true);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // More than 2 nodes. Is this a generalization or implementation?
                // If so, try to resolve a tree creation
                
                if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_GENERALIZATION) ||
                        accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_IMPLEMENTATION)) {
                    int idx = 0;
                    int numClasses = 0;
                    int numInterfaces = 0;
                    int numOthers = 0;
                    
                    IPresentationElement pFirstClass = null;
                    IPresentationElement pFirstInterface = null;
                    
                    ETList<IPresentationElement> pClasses = new ETArrayList<IPresentationElement>();
                    ETList<IPresentationElement> pInterfaces = new ETArrayList<IPresentationElement>();
                    
                    if ( pClasses != null && pInterfaces != null ) {
                        while ( idx < count && numOthers == 0 ) {
                            IPresentationElement pItem = pNodeGraphics.get(idx++);
                            if ( pItem != null) {
                                IElement pElement = pItem.getFirstSubject();
                                
                                if ( pElement != null) {
                                    String eltype = pElement.getElementType();
                                    
                                    if ( eltype.equals("Class") ) {
                                        numClasses++;
                                        if ( numClasses == 1 ) {
                                            pFirstClass = pItem;
                                        } else {
                                            pClasses.add(pItem);
                                        }
                                    } else if ( eltype.equals("Interface") ) {
                                        numInterfaces++;
                                        if ( numInterfaces == 1 ) {
                                            pFirstInterface = pItem;
                                        } else {
                                            pInterfaces.add( pItem );
                                        }
                                    } else {
                                        numOthers++;
                                    }
                                }
                            }
                        }
                        
                        if ( numOthers == 0 ) {
                            if (accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_GENERALIZATION)) {
                                
                                String resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_GENERALIZATION");
                                
                                ETList<IPresentationElement> pTheList = null;
                                IPresentationElement pTheFirst = null;
                                
                                if ( numInterfaces == 0 && pFirstClass != null ) {
                                    // create generalizations from all the others to the first
                                    pTheFirst = pFirstClass;
                                    pTheList = pClasses;
                                } else if ( numClasses == 0 && pFirstInterface != null) {
                                    // create generalizations from all the others to the first
                                    pTheFirst = pFirstInterface;
                                    pTheList = pInterfaces;
                                }
                                
                                if ( pTheFirst != null && pTheList != null ) {
                                    TSNode pToNode = TypeConversions.getOwnerNode( pTheFirst );
                                    int idx1 = 0;
                                    int count2 = pTheList.size();
                                    while ( idx1 < count2 ) {
                                        IPresentationElement pItem = pTheList.get(idx1++);
                                        if ( pItem != null ) {
                                            TSEdge pNewEdge = null;
                                            TSNode pFromNode = null;
                                            
                                            pFromNode = TypeConversions.getOwnerNode( pItem );
                                            
                                            if ( pFromNode != null && pToNode != null ) {
                                                try {
                                                    pNewEdge = pDrawingAreaControl.addEdge( resultingInitString,
                                                            pFromNode,
                                                            pToNode,
                                                            true,
                                                            true);
                                                } catch(Exception e) {
                                                    pNewEdge = null;
                                                }
                                                retval = true;
                                            }
                                        }
                                    }
                                }
                            } else if ( accelerator.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_IMPLEMENTATION )) {
                                if ( numClasses == 1 && numInterfaces > 0 ) {
                                    // Create implementations from the only class to all interfaces
                                    String resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_IMPLEMENTATION");
                                    
                                    if ( pFirstClass != null && pInterfaces != null ) {
                                        TSNode pFromNode = null;
                                        pFromNode = TypeConversions.getOwnerNode( pFirstClass);
                                        
                                        // Make sure to handle the first on first
                                        
                                        TSNode pToNode = null;
                                        
                                        if ( pFirstInterface != null) {
                                            pToNode = TypeConversions.getOwnerNode( pFirstInterface);
                                            if ( pFromNode != null && pToNode != null ) {
                                                try {
                                                    TSEdge pNewEdge = pDrawingAreaControl.addEdge( resultingInitString,
                                                            pFromNode,
                                                            pToNode,
                                                            true,
                                                            true );
                                                } catch(Exception e) {
                                                }
                                                retval = true;
                                            }
                                        }
                                        
                                        // Now handle the rest
                                        
                                        int count2 = pInterfaces.size();
                                        int idx2 = 0;
                                        while ( idx2 < count2 ) {
                                            IPresentationElement pItem = pInterfaces.get(idx2++);
                                            if ( pItem != null) {
                                                TSEdge pNewEdge = null;
                                                
                                                pToNode = TypeConversions.getOwnerNode( pItem );
                                                
                                                if ( pFromNode != null && pToNode != null) {
                                                    try {
                                                        pNewEdge = pDrawingAreaControl.addEdge( resultingInitString,
                                                                pFromNode,
                                                                pToNode,
                                                                true,
                                                                true);
                                                    } catch(Exception e) {
                                                        pNewEdge = null;
                                                    }
                                                    retval = true;
                                                }
                                            }
                                        }
                                    }
                                } else if ( numInterfaces == 1 && numClasses > 0 ) {
                                    // Create implementations from all classes to the only interface
                                    // Create implementations from the only class to all interfaces
                                    String resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_IMPLEMENTATION");
                                    
                                    if ( pFirstInterface != null && pClasses != null ) {
                                        TSNode pToNode = TypeConversions.getOwnerNode( pFirstInterface);
                                        
                                        // Make sure to handle the first on first
                                        
                                        TSNode pFromNode = null;
                                        
                                        if ( pFirstClass != null ) {
                                            pFromNode = TypeConversions.getOwnerNode( pFirstClass);
                                            if ( pFromNode != null && pToNode != null ) {
                                                try {
                                                    TSEdge pNewEdge = pDrawingAreaControl.addEdge( resultingInitString,
                                                            pFromNode,
                                                            pToNode,
                                                            true,
                                                            true );
                                                } catch(Exception e) {
                                                }
                                                retval = true;
                                            }
                                        }
                                        
                                        // Now handle the rest
                                        
                                        int  count2 = pClasses.size();
                                        int idx3 = 0;
                                        while ( idx3 < count2 ) {
                                            IPresentationElement pItem = pClasses.get(idx3++);
                                            if ( pItem != null) {
                                                TSEdge pNewEdge = null;
                                                
                                                pFromNode = TypeConversions.getOwnerNode( pItem );
                                                
                                                if ( pFromNode != null && pToNode != null ) {
                                                    try {
                                                        pNewEdge = pDrawingAreaControl.addEdge( resultingInitString,
                                                                pFromNode,
                                                                pToNode,
                                                                true,
                                                                true);
                                                    } catch(Exception e) {
                                                        pNewEdge = null;
                                                    }
                                                    retval = true;
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
            return retval;
        }
        
        
	public boolean onAcceleratorInvoke(String keyCode)
	{
		ETSystem.out.println("ADCoreEngine.onAcceleratorInvoke-" + keyCode);
		boolean bHandled = false;
		boolean bReadOnly = false;
		IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
		
		int diagramKind = IDiagramKind.DK_UNKNOWN;
		IDiagram pDiagram = pDrawingAreaControl.getDiagram();
		if (pDiagram != null)
		{
			diagramKind = pDiagram.getDiagramKind();
		}
		
//		ADGraphWindow pGraphEditor = null;

		if (pDrawingAreaControl != null)
		{
			bReadOnly = pDrawingAreaControl.getReadOnly();
//			pGraphEditor = pDrawingAreaControl.getGraphWindow();
		}

		// since Ax control container eat certain navigation keys, we trap them here as accelerators
		if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_CLASS) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_SIMPLESTATE) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_PACKAGE) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_INTERFACE) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_USECASE) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_INVOCATION) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTOR) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_DECISION) ||
			keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ERENTITY))
		{
			if (!bReadOnly)
			{
				String resultingInitString = "";
				// commented out the diagram kind check for issue 2929.
				// this code did not exist in 6.1.4 or 6.2
				if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_CLASS) && (diagramKind == IDiagramKind.DK_CLASS_DIAGRAM || diagramKind == IDiagramKind.DK_COLLABORATION_DIAGRAM || diagramKind == IDiagramKind.DK_COMPONENT_DIAGRAM) )
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_CLASS");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_SIMPLESTATE) && diagramKind == IDiagramKind.DK_STATE_DIAGRAM)
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_SIMPLE_STATE");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_PACKAGE))
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_PACKAGE");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_INTERFACE))
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_INTERFACE_AS_CLASS");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_USECASE))
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_USECASE");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_INVOCATION))
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_INVOCATIONNODE");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTOR))
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_ACTOR");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_DECISION) && diagramKind == IDiagramKind.DK_ACTIVITY_DIAGRAM)
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_UML_DECISIONNODE");
				}
				else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ERENTITY) && diagramKind == IDiagramKind.DK_ENTITY_DIAGRAM)
				{
					resultingInitString = getInitStringForButton("ID_VIEWNODE_ER_ENTITY");
				}

				if (resultingInitString != null && resultingInitString.length() > 0)
				{
					IETPoint pCenterPoint = getLogicalCenter();
					if (pCenterPoint != null)
					{
						int x = pCenterPoint.getX();
						int y = pCenterPoint.getY();
						
						ETList<IPresentationElement> pPEs = pDrawingAreaControl.getSelected();

						// Refresh the deselected objects
//						pDrawingAreaControl.selectAll(false);
//						pDrawingAreaControl.RefreshRect2(pPEs);

						// There's a problem (perhaps in TS) dealing with WM_PAINTs.  Even
						// though the refreshrect2 call above invalidates the graph manager
						// I never get a MW_PAINT blasting their offscreen bitmap to the
						// HWND.  The result is that the objects above appear to never deselect.
						// TS sent me this message 12-4-2003 :
						//
						// The graph manager's invalidate doesn't update until the next WM_PAINT 
						// message so this might explain your problem. By passing TRUE for the  
						// immediateUpdate parameter on TSEGraphDisplay's invalidateLogicalRect,  
						// the system should be forced to send a WM_PAINT which should redraw your  
						// screen properly. 
						//
						// Sure enough forcing the redraw works, but I don't want to change a core
						// function and have everything else slow down with forced refreshes.  So
						// instead I've posed my own WM_PAINT here and this seems to work.  Strange.

						pDrawingAreaControl.refresh(true);

						pCenterPoint.setPoints(x + m_AcceleratorOffset.x, y + m_AcceleratorOffset.y);

						try
						{
							TSNode pCreatedNode = pDrawingAreaControl.addNode(resultingInitString, pCenterPoint, true, true);
						}
						catch(Exception e)
						{
						}

						// Increment the accelerator offset in case the user hits the accel key again
						m_AcceleratorOffset.x += 20;
						m_AcceleratorOffset.y -= 20;
					}
				}
			}
			bHandled = true;
		}
		/*else*/ if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ABSTRACTION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_GENERALIZATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_IMPLEMENTATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ASSOCIATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_DEPENDENCY) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_REALIZATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTIVITYEDGE) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_AGGREGATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_COMPOSITION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_AGGREGATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_STATETRANSITION))
		{
			if (!bReadOnly)
			{
				// if we have two things selected, try to draw said relationship
				// KSL: pump keyboard messages before handing off control, if a model dialog
				// is created during processing it could protentially receive keyboard messages
				// sent to the top level window (JBuilder potentially), thus entering a deadly
				// embrace (see COccManager::IsDialogMessage())
				bHandled = handleRelationAccelerator (keyCode);
			}
			else
			{
				bHandled = true;
			}
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_SYNCHRONUS_MESSAGE))
		{
			if( pDrawingAreaControl != null )
			{
				pDrawingAreaControl.enterModeFromButton("ID_VIEWNODE_UML_MESSAGE");
				bHandled = true;
			}
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_ATTRIBUTE) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_OPERATION) ||
				keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_PARAMETER))
		{
			if (!bReadOnly)
			{      
				// if we have just one object selected
				// pass the message to that object
				// KSL: pump keyboard messages before handing off control, if a model dialog
				// is created during processing it could protentially receive keyboard messages
				// sent to the top level window (JBuilder potentially), thus entering a deadly
				// embrace (see COccManager::IsDialogMessage())
				bHandled = GetHelper.handleAccelerator(pDrawingAreaControl, keyCode, true );
			}
			else
			{
				bHandled = true;
			}
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_CIRCULAR_STYLE))
		{
//			if (!bReadOnly)
//			{
//				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_CIRCULAR_LAYOUT);
//			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_HIERARCHICAL_STYLE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_HIERARCHICAL_LAYOUT);
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_INCREMENTAL_LAYOUT))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_INCREMENTAL_LAYOUT);
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_RELAYOUT))
		{
//			if (!bReadOnly)
//			{
//				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_GLOBAL_LAYOUT);
//			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_ORTHOGONAL_STYLE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_ORTHOGONAL_LAYOUT);
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SEQUENCE_DIAGRAM_LAYOUT))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_SYMMETRIC_LAYOUT);
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_TREE_STYLE))
		{
//			if (!bReadOnly)
//			{
//				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_TREE_LAYOUT);
//			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SYMMETRIC_STYLE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.setLayoutStyle(ILayoutKind.LK_SYMMETRIC_LAYOUT);
			}
			bHandled = true;
		}
                
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_EXPORT_AS_IMAGE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.showImageDialog();
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_FIT_TO_WINDOW))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.fitInWindow();
			}
			bHandled = true;
		}
                //the stackable actions from the toolbar
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_TO_FRONT))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.onMoveToFront() ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_FORWARD))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.onMoveForward() ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_BACKWARD))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.onMoveBackward() ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_MOVE_TO_BACK))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.onMoveToBack() ;
			}
			bHandled = true;
		}
                
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_RELATIONSHIP_DISCOVERY))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.executeRelationshipDiscovery() ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_SELECT_MODE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.enterMode(IDrawingToolKind.DTK_SELECTION) ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_PAN_MODE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.enterMode(IDrawingToolKind.DTK_PAN) ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_ZOOM_WITH_MARQUEE_MODE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.enterMode(IDrawingToolKind.DTK_ZOOM) ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_ZOOM_INTERACTIVELY_MODE))
		{
			if (!bReadOnly)
			{
                                pDrawingAreaControl.enterMode(IDrawingToolKind.DTK_MOUSE_ZOOM) ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_NAVIGATE_LINK_MODE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.enterMode(IDrawingToolKind.DTK_EDGENAV_MOUSE) ;
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_DIAGRAM_SYNC))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.validateDiagram(false,null);
			}
			bHandled = true;
		}
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_PRINT_PREVIEW))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.printPreview(pDrawingAreaControl.getName(), false);
			}
			bHandled = true;
		}
                
		// Select all
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL))
		{
			pDrawingAreaControl.selectAll(true);
			bHandled = true;
		}
		// Select all similar
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_SELECT_ALL_SIMILAR))
		{
			pDrawingAreaControl.selectAllSimilar();
			bHandled = true;
		}
                // Save
                else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_SAVE))
		{
			if (!bReadOnly)
			{                           
				pDrawingAreaControl.save();
                                pDrawingAreaControl.setIsDirty(false);                                
			}
			bHandled = true;
		}
		// Copy/Paste/Cut
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_COPY))
		{
			boolean bHasSelectedNodes = pDrawingAreaControl.getHasSelectedNodes(true);
			if (bHasSelectedNodes)
			{
				pDrawingAreaControl.copy();
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_PASTE))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.paste();
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CUT))
		{
			if (!bReadOnly)
			{
				boolean bHasSelectedNodes = pDrawingAreaControl.getHasSelectedNodes(true);
				if (bHasSelectedNodes)
				{
					pDrawingAreaControl.cut();
				}
			}
			bHandled = true;
		}
		// Resize element to contents
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_RESIZE_TO_CONTENTS))
		{
			if (!bReadOnly)
			{
				pDrawingAreaControl.sizeToContents(true);
			}
			bHandled = true;
		}
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY))
		{
			if (!bReadOnly)
			{
				ETList<IPresentationElement> pPEs = pDrawingAreaControl.getSelected();
				int count = 0;
				if (pPEs != null)
				{
					count = pPEs.size();
					if (count > 0)
					{
//						pDrawingAreaControl.autoRouteEdges(BaseRoutingAction.RSK_TOGGLE_ORTHOGONALITY, pPEs);
						pDrawingAreaControl.refresh(true);
					}
				}
			}
			bHandled = true;
		}
		// Show friendly names
		else if (keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_SHOW_FRIENDLY_NAMES))
		{
			// Toggle the friendly names preference
			ProductHelper.toggleShowAliasedNames();
			bHandled = true;
		}

		if (bHandled && pDrawingAreaControl != null)
		{
			pDrawingAreaControl.setFocus();
		}

		if(bHandled == true && !bReadOnly )
		{
//			  SHORT nShift = 0;
//			  if( HIBYTE(::GetKeyState( VK_SHIFT )) )
//				 nShift = 1;
//     
//			  short tempKeyCode = (short)(*nKeycode);
//			pDrawingAreaControl->HandleKeyDown( &tempKeyCode, nShift, true );
		}
		return bHandled;
	}
	
        
        public boolean onCreateNewNodeByKeyboard()
        {
           boolean retVal = false;
            IDrawingAreaControl drawingDiagram  = getDrawingArea();
            if ( drawingDiagram == null)
            {
                return retVal;
            }
            DiagramKeyboardAccessProvider kbAccessProvider = 
                DiagramKeyboardAccessProvider.getInstance(drawingDiagram);
            
            if ( kbAccessProvider != null) {
                retVal = kbAccessProvider.onCreateNewNodeByKeyboard();
            }
            return retVal;
        }
        
   /**
    * Delayed Postmove event from the drawing area
    */
   public void delayedPostMoveObjects(ETList<IPresentationElement> pPEs, int nDeltaX, int nDeltaY)
   {
       if (m_bAllNodesInSameContainer &&
           (pPEs != null) &&
           (determineContainedPEs(pPEs) > 0) )
       {
           
           ETPairT<Boolean, INodePresentation> result = getGraphicalContainerforPEs(pPEs);
           boolean inSameContainer = ((Boolean)result.getParamOne()).booleanValue();
           INodePresentation cpPostMoveContainer = result.getParamTwo();
           
           if (result != null)
           {
               // We used to make sure that the containers were different, but
               // we need to account for changes within containers like
               // partitions, and states.
               
               IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(cpPostMoveContainer);
               
               IADContainerDrawEngine pActualContainer = (pDrawEngine instanceof IADContainerDrawEngine)? (IADContainerDrawEngine)pDrawEngine:null;
               
               if (pActualContainer != null)
               {
                   // Tell the container to begin containment
                   pActualContainer.beginContainment(m_cpPreContainer, pPEs);
               }
               else if (m_cpPreContainer != null)
               {
                   pDrawEngine = TypeConversions.getDrawEngine(m_cpPreContainer);
                   
                   pActualContainer = (pDrawEngine instanceof IADContainerDrawEngine)? (IADContainerDrawEngine)pDrawEngine:null;
                   
                   if (pActualContainer != null)
                   {
                       // Tell the container to end containment
                       pActualContainer.endContainment(pPEs);
                   }
               }
               
               if(containedElements.size() > 0)
               {
                   for(Map.Entry<IADContainerDrawEngine, ETList<IPresentationElement>> entry :
                       containedElements.entrySet())
                   {
                       IADContainerDrawEngine engine = entry.getKey();
                       ETList <  IPresentationElement > preContained = entry.getValue();

                       ETList < IPresentationElement > elements = engine.getContained();
                       ETList < IPresentationElement > newlyContained = new ETArrayList < IPresentationElement >();
                       for(IPresentationElement curElement : elements)
                       {
                           if(preContained.contains(curElement) == false)
                           {
                               newlyContained.add(curElement);
                               
                               IDrawEngine nodeEngine = TypeConversions.getDrawEngine(curElement);
                               nodeEngine.resetGraphicalContainer();
                           }
                       }

                       engine.beginContainment(m_cpPreContainer, newlyContained);
                   }
               }
           }

      // Reset the container for the pre operation
      m_cpPreContainer = null;
      containedElements.clear();

      }
   }
   
	/**
	 * Returns only the presentation elements that need to change their containment
	 */
	public int determineContainedPEs( ETList<IPresentationElement> pPEs ) 
	{
		if (pPEs == null){
			throw new IllegalArgumentException();
		}

		// Remove any presentation element that are graphically contained
		// within other presentation elements with this list
		int lCnt =  pPEs.size();

		if( lCnt > 1 )
		{
			ETList <IPresentationElement> cpAllContainedPEs = new ETArrayList <IPresentationElement> ();
			
				for( int lIndx=0; lIndx<lCnt; lIndx++ )
				{
					IPresentationElement cpPE = (IPresentationElement)pPEs.get(lIndx);

					if( cpPE != null )
					{
						IDrawEngine cpDrawEngine = TypeConversions.getDrawEngine(cpPE);
						
						IADContainerDrawEngine cpEngine = ( cpDrawEngine instanceof IADContainerDrawEngine)? (IADContainerDrawEngine)cpDrawEngine:null;
						
						if( cpEngine != null )
						{
							ETList <IPresentationElement> cpContainedPEs = cpEngine.getContained();
							
							if( cpContainedPEs != null )
							{
								cpAllContainedPEs.addThese( cpContainedPEs );
							}
						}
					}
				}

				pPEs.removeThese(cpAllContainedPEs);

			lCnt = pPEs.size();
		}

		return lCnt;
	}  
   
   
   /**
    * Premove event from the drawing area
    */
   public void onPreMoveObjects(ETList<IETGraphObject> affectedObjects, int dx, int dy)  
   {
       // Reset the accelerator offset with any change on the drawing area
       m_AcceleratorOffset.x = 0;
       m_AcceleratorOffset.y = 0;
       
       m_cpPreContainer = null;
       
       if (isContainmentOK())
       {
           // Determine if all the nodes being moved are in the same graphical container
           ETPairT<Boolean, INodePresentation> result = getGraphicalContainerforNodes(affectedObjects);;
           boolean m_bAllNodesInSameContainer = ((Boolean)result.getParamOne()).booleanValue();
           m_cpPreContainer = result.getParamTwo();
           
           for(IETGraphObject obj : affectedObjects)
           {
               IDrawEngine engine = TypeConversions.getDrawEngine(obj);
               if(engine instanceof IADContainerDrawEngine)
               {
//                   movedContainer = (IADContainerDrawEngine)engine;
//                   containedElements = movedContainer.getContained();
                   
                   IADContainerDrawEngine container = (IADContainerDrawEngine) engine;
                   ETList < IPresentationElement > contained = container.getContained();
                   
                   containedElements.put(container, contained);
               }
           }
       }
       else
       {
           m_bAllNodesInSameContainer = false;
       }
   
   }
	

	/**
	 * Determine the graphical container for all the nodes in the list
	 * It is valid for the container to be NULL, this implies the diagram is the container.
	 */
	protected ETPairT<Boolean, INodePresentation> getGraphicalContainerforNodes( ETList<IETGraphObject> affectedObjects )
	{
		if( affectedObjects == null )
		{
			throw new IllegalArgumentException();
		}

		ETList <IPresentationElement> cpPEs = GetHelper.createPEList(affectedObjects);

		return getGraphicalContainerforPEs( cpPEs);
	}

   /**
    * Determine the graphical container for all the presentation elements
    * It is valid for the container to be NULL, this implies the diagram is the container.
    */
   protected ETPairT<Boolean, INodePresentation> getGraphicalContainerforPEs(ETList<IPresentationElement> pPEs)
   {
      if (pPEs == null)
      {
         throw new IllegalArgumentException();
      }

		INodePresentation nodePres = null;		
      boolean bAllNodesInSameContainer = true;
      
		ETPairT<Boolean, INodePresentation> retVal = new ETPairT<Boolean, INodePresentation>();


      INodePresentation cpAllContainer = null;

      if (pPEs != null)
      {
         int lCnt = pPEs.size();

         for (int lIndx = 0; lIndx < lCnt; lIndx++)
         {
            IPresentationElement cpPE = pPEs.get(lIndx);
 
            if (cpPE != null)
            {
               INodePresentation cpContainer = TypeConversions.getGraphicalContainer(cpPE);
               
               if (cpContainer != null)
               {
                  if (cpAllContainer == null)
                  {
                     cpAllContainer = cpContainer;
                  }
                  else if (cpAllContainer != cpContainer)
                  {
                     bAllNodesInSameContainer = false;
                     break;
                  }
               }
               else if (cpAllContainer != null)
               {
                  bAllNodesInSameContainer = false;
                  break;
               }
            }
         }

			if( bAllNodesInSameContainer && (cpAllContainer != null) )
			{
				nodePres = cpAllContainer;
			}

      }

		retVal.setParamOne(Boolean.valueOf(bAllNodesInSameContainer));
		retVal.setParamTwo(nodePres);

      return retVal;
   }
	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#beginCrossDiagramPaste()
    */
   public boolean beginCrossDiagramPaste()
   {
      enterCrossDiagramPasteMode();
      return false;
   }
   
   protected void enterCrossDiagramPasteMode()
   {
        IDrawingAreaControl control = getDrawingArea();
        if(control == null)
            return;

        TSEGraphWindow window = control.getGraphWindow();
        if(window == null)
            return;

        CrossDiagramPasteTool pasteTool = new CrossDiagramPasteTool();

        IDiagram diagram = control.getDiagram();
        pasteTool.setParentDiagram(diagram);

        //window.switchState(pasteTool);
        window.switchTool(pasteTool);
   }
   
   
   // util to add an action defined in NB file system
   private void addNBAction(IMenuManager manager, String action)
   {
	   FileSystem system = Repository.getDefault().getDefaultFileSystem();

		if (system != null)
		{
			try
			{
				FileObject fo = system.findResource(action);
				DataObject actionObjects = DataObject.find(fo);
				if (actionObjects!=null)
				{
					InstanceCookie ic = (InstanceCookie) actionObjects.getCookie(InstanceCookie.class);
					if (ic != null) 
					{
						Object instance=null;
						try {
							instance = ic.instanceCreate();
						} catch (IOException e) {
							// ignore
							e.printStackTrace();

						} catch (ClassNotFoundException e) {
							// ignore
							e.printStackTrace();
						}
						if (instance instanceof Action)
						{
							manager.add(new BaseActionWrapper((Action)instance));
						}
					}
				}
			}catch (Exception e)
			{
				// ignore
				e.printStackTrace();
			}
		}					
   }
}
