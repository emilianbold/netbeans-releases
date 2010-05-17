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


package org.netbeans.modules.uml.ui.addins.eventlogger;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;

//import org.netbeans.modules.uml.core.addinframework.IAddIn;
//import org.netbeans.modules.uml.core.addinframework.IAddInEventsSink;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundtripEnums;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageData;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessengerEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.ISCMEnums;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.core.scm.ISCMEventsSink;
import org.netbeans.modules.uml.core.scm.ISCMItemGroup;
import org.netbeans.modules.uml.core.scm.ISCMOptions;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaPropertyKind;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/**
 * @author sumitabhk
 *
 */
public class AddinEventsSink implements 
//        IDrawingAreaEventsSink,
//										IDrawingAreaSynchEventsSink,
//										IDrawingAreaContextMenuEventsSink,
//										IDrawingAreaSelectionEventsSink,
//										IDrawingAreaAddNodeEventsSink,
//										IDrawingAreaAddEdgeEventsSink,
//										IDrawingAreaReconnectEdgeEventsSink,
//										ICompartmentEventsSink,
//										IChangeNotificationTranslatorSink,
										IProjectTreeEventsSink,
										IProjectTreeContextMenuEventsSink,
										IMessengerEventsSink,
										IWorkspaceEventsSink,
										IWSProjectEventsSink,
										IWSElementEventsSink,
//										IAddInEventsSink,
										IElementModifiedEventsSink,
										IMetaAttributeModifiedEventsSink,
										IDocumentationModifiedEventsSink,
										INamespaceModifiedEventsSink,
										INamedElementEventsSink,
										IEditControlEventSink,
										IProjectTreeFilterDialogEventsSink,
										IRelationValidatorEventsSink,
										IElementLifeTimeEventsSink,
										IClassifierFeatureEventsSink,
										IClassifierTransformEventsSink,
										IFeatureEventsSink,
										IStructuralFeatureEventsSink,
										IBehavioralFeatureEventsSink,
										IParameterEventsSink,
										ITypedElementEventsSink,
										IAttributeEventsSink,
										IOperationEventsSink,
										ICoreProductInitEventsSink,
										IRequestProcessorInitEventsSink,
										IRoundTripAttributeEventsSink,
										IRoundTripClassEventsSink,
										IRoundTripOperationEventsSink,
										IRoundTripPackageEventsSink,
										IRoundTripRelationEventsSink,
										IImportEventsSink,
										IElementDisposalEventsSink,
										IRelationEventsSink,
										IPreferenceManagerEventsSink,
										IExternalElementEventsSink,
										ISCMEventsSink,
										IStereotypeEventsSink,
										IAffectedElementEventsSink,
										IRedefinableElementModifiedEventsSink,
										ILifelineModifiedEventsSink,
										IActivityEdgeEventsSink,
										IProjectEventsSink,
										/*IProjectUpgradeEventsSink,*/
										IAssociationEndEventsSink,
										IArtifactEventsSink,
										/*IDesignPatternEventsSink,*/
										IPackageEventsSink
{
	private EventLoggingAddin m_Parent = null;

	/**
	 * 
	 */
	public AddinEventsSink()
	{
		super();
	}

	private void addMessage(String msg)
	{
		if (m_Parent != null)
		{
			EventsDialog dialog = m_Parent.getDialog();
			if (dialog != null)
			{
				dialog.addEntry(msg);
			}
		}
	}

	private void addChangeRequestMessage(String eventName, IChangeRequest event)
	{
		String msg = eventName;
		if (event != null)
		{
			int type = event.getState();
			switch (type)
			{
				case IRoundtripEnums.CT_MODIFY :
					msg += "[ MODIFY";
					break;

				case IRoundtripEnums.CT_DELETE :
					msg += "[ DELETE";
					break;

				case IRoundtripEnums.CT_CREATE :
					msg += "[ CREATE";
					break;

				default :
					msg += "[ NONE";
					break;
			}
			
			int elemType = event.getElementType();
			switch (elemType)
			{
				case IRoundtripEnums.RCT_CLASS :
					msg += "- Class";
					break;

				case IRoundtripEnums.RCT_ATTRIBUTE :
					msg += "- Attribute";
					break;

				case IRoundtripEnums.RCT_NAVIGABLE_END_ATTRIBUTE :
					msg += "- Navigable End Attribute";
					break;

				case IRoundtripEnums.RCT_OPERATION :
					msg += "- Operation";
					break;

				case IRoundtripEnums.RCT_PACKAGE :
					msg += "- Package";
					break;

				case IRoundtripEnums.RCT_RELATION :
					msg += "- Relationship";
					break;

				default :
					msg += "- None";
					break;
			}
			
			int reqKind = event.getRequestDetailType();
			switch( reqKind ) 
			{
			   case IRoundtripEnums.RDT_NONE:                                   msg += "RDT_NONE"; break;
			   case IRoundtripEnums.RDT_DOCUMENTATION_MODIFIED:                 msg += "RDT_DOCUMENTATION_MODIFIED"; break;
			   case IRoundtripEnums.RDT_ELEMENT_DELETED:                        msg += "RDT_ELEMENT_DELETED"; break;
			   case IRoundtripEnums.RDT_NAME_MODIFIED:                          msg += "RDT_NAME_MODIFIED"; break;
			   case IRoundtripEnums.RDT_VISIBILITY_MODIFIED:                    msg += "RDT_VISIBILITY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_ELEMENT_ADDED_TO_NAMESPACE:             msg += "RDT_ELEMENT_ADDED_TO_NAMESPACE"; break;
			   case IRoundtripEnums.RDT_RELATION_VALIDATE:                      msg += "RDT_RELATION_VALIDATE"; break;
			   case IRoundtripEnums.RDT_RELATION_MODIFIED:                      msg += "RDT_RELATION_MODIFIED"; break;
			   case IRoundtripEnums.RDT_RELATION_DELETED:                       msg += "RDT_RELATION_DELETED"; break;
			   case IRoundtripEnums.RDT_ATTRIBUTE_DEFAULT_MODIFIED:             msg += "RDT_ATTRIBUTE_DEFAULT_MODIFIED"; break;
			   case IRoundtripEnums.RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED:        msg += "RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED:    msg += "RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED"; break;
			   case IRoundtripEnums.RDT_CONCURRENCY_MODIFIED:                   msg += "RDT_CONCURRENCY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_SIGNAL_ADDED:                           msg += "RDT_SIGNAL_ADDED"; break;
			   case IRoundtripEnums.RDT_SIGNAL_REMOVED:                         msg += "RDT_SIGNAL_REMOVED"; break;
			   case IRoundtripEnums.RDT_PARAMETER_ADDED:                        msg += "RDT_PARAMETER_ADDED"; break;
			   case IRoundtripEnums.RDT_PARAMETER_REMOVED:                      msg += "RDT_PARAMETER_REMOVED"; break;
			   case IRoundtripEnums.RDT_ABSTRACT_MODIFIED:                      msg += "RDT_ABSTRACT_MODIFIED"; break;
			   case IRoundtripEnums.RDT_FEATURE_ADDED:                          msg += "RDT_FEATURE_ADDED"; break;
			   case IRoundtripEnums.RDT_FEATURE_REMOVED:                        msg += "RDT_FEATURE_REMOVED"; break; 
			   case IRoundtripEnums.RDT_STATIC_MODIFIED:                        msg += "RDT_STATIC_MODIFIED"; break;
			   case IRoundtripEnums.RDT_CONDITION_ADDED:                        msg += "RDT_CONDITION_ADDED"; break;
			   case IRoundtripEnums.RDT_CONDITION_REMOVED:                      msg += "RDT_CONDITION_REMOVED"; break; 
			   case IRoundtripEnums.RDT_QUERY_MODIFIED:                         msg += "RDT_QUERY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_PARAMETER_DEFAULT_MODIFIED:             msg += "RDT_PARAMETER_DEFAULT_MODIFIED"; break;
			   case IRoundtripEnums.RDT_PARAMETER_DEFAULT_BODY_MODIFIED:        msg += "RDT_PARAMETER_DEFAULT_BODY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED:    msg += "RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED"; break;
			   case IRoundtripEnums.RDT_PARAMETER_DIRECTION_MODIFIED:           msg += "RDT_PARAMETER_DIRECTION_MODIFIED"; break;
			   case IRoundtripEnums.RDT_CHANGEABILITY_MODIFIED:                 msg += "RDT_CHANGEABILITY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_MULTIPLICITY_MODIFIED:                  msg += "RDT_MULTIPLICITY_MODIFIED"; break;
			   case IRoundtripEnums.RDT_TYPE_MODIFIED:                          msg += "RDT_TYPE_MODIFIED"; break;
			   case IRoundtripEnums.RDT_LOWER_MODIFIED:                         msg += "RDT_LOWER_MODIFIED"; break;
			   case IRoundtripEnums.RDT_UPPER_MODIFIED:                         msg += "RDT_UPPER_MODIFIED"; break;
			   case IRoundtripEnums.RDT_RANGE_ADDED:                            msg += "RDT_RANGE_ADDED"; break;
			   case IRoundtripEnums.RDT_RANGE_REMOVED:                          msg += "RDT_RANGE_REMOVED"; break;
			   case IRoundtripEnums.RDT_ORDER_MODIFIED:                         msg += "RDT_ORDER_MODIFIED"; break;
			   case IRoundtripEnums.RDT_PACKAGE_NAME_MODIFIED:                  msg += "RDT_PACKAGE_NAME_MODIFIED"; break;
			   case IRoundtripEnums.RDT_TRANSIENT_MODIFIED:                     msg += "RDT_TRANSIENT_MODIFIED"; break;
			   case IRoundtripEnums.RDT_NATIVE_MODIFIED:                        msg += "RDT_NATIVE_MODIFIED"; break;
			   case IRoundtripEnums.RDT_VOLATILE_MODIFIED:                      msg += "RDT_VOLATILE_MODIFIED"; break;
			   case IRoundtripEnums.RDT_LEAF_MODIFIED:                          msg += "RDT_LEAF_MODIFIED"; break;
			   case IRoundtripEnums.RDT_RELATION_END_MODIFIED:                  msg += "RDT_RELATION_END_MODIFIED"; break;
			   case IRoundtripEnums.RDT_RELATION_END_ADDED:                     msg += "RDT_RELATION_END_ADDED"; break;
			   case IRoundtripEnums.RDT_RELATION_END_REMOVED:                   msg += "RDT_RELATION_END_REMOVED"; break;
			   case IRoundtripEnums.RDT_DEPENDENCY_ADDED:                       msg += "RDT_DEPENDENCY_ADDED"; break;
			   case IRoundtripEnums.RDT_DEPENDENCY_REMOVED:                     msg += "RDT_DEPENDENCY_REMOVED"; break;
			   case IRoundtripEnums.RDT_ASSOCIATION_END_ADDED:                  msg += "RDT_ASSOCIATION_END_ADDED"; break;
			   case IRoundtripEnums.RDT_ASSOCIATION_END_MODIFIED:               msg += "RDT_ASSOCIATION_END_MODIFIED"; break;
			   case IRoundtripEnums.RDT_ASSOCIATION_END_REMOVED:                msg += "RDT_ASSOCIATION_END_REMOVED"; break;
			   case IRoundtripEnums.RDT_RELATION_CREATED:                       msg += "RDT_RELATION_CREATED"; break;
			   case IRoundtripEnums.RDT_FEATURE_MOVED :                         msg += "RDT_FEATURE_MOVED"; break;
			   case IRoundtripEnums.RDT_FEATURE_DUPLICATED :                    msg += "RDT_FEATURE_DUPLICATED"; break;
			   case IRoundtripEnums.RDT_NAMESPACE_MODIFIED :                    msg += "RDT_NAMESPACE_MODIFIED"; break;
			   case IRoundtripEnums.RDT_CHANGED_NAMESPACE :                     msg += "RDT_CHANGED_NAMESPACE"; break;
			}
			
			msg += " ]";
		}
		addMessage(msg);
	}

	private String getElementType(IVersionableElement pEle)
	{
		String retStr = "";
		if (pEle != null && pEle instanceof IElement)
		{
			IElement elem = (IElement)pEle;
			String metaType = elem.getElementType();
			retStr = " (" + metaType + ")"; 
		}
		return retStr;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//		addMessage("OnDrawingAreaPreCreated");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//		addMessage("OnDrawingAreaPostCreated");
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
	{
		addMessage("OnDrawingAreaOpened");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
	{
		addMessage("OnDrawingAreaClosed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
		addMessage("OnDrawingAreaPreSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
		addMessage("OnDrawingAreaPostSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
	{
		addMessage("OnDrawingAreaKeyDown");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
		switch (nPropertyKindChanged)
		{
//		case IDrawingAreaPropertyKind.DAPK_NAMESPACE :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_NAMESPACE)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_NAME :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_NAME)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_DOCUMENTATION :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_DOCUMENTATION)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_LAYOUT :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_LAYOUT)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_DIRTYSTATE :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_DIRTYSTATE)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_ZOOM :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_ZOOM)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_READONLY :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_READONLY)");
//		   break;
//		case IDrawingAreaPropertyKind.DAPK_FRIENDLYNAMESCHANGE :
//		   addMessage( "OnDrawingAreaPrePropertyChange (DAPK_FRIENDLYNAMESCHANGE)");
//		   break;
		default :
		   addMessage( "OnDrawingAreaPrePropertyChange (Unknown)");
		   break;
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
		addMessage( "OnDrawingAreaPostPropertyChange");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
//	{
//		addMessage( "OnDrawingAreaTooltipPreDisplay");
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
	{
		if (pParentDiagram != null)
		{
			String filename = pParentDiagram.getFilename();
			addMessage( "Diagram Activated : " + filename);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		IPresentationElement pPE = null;
//		String msg = "OnDrawingAreaPreDrop";
//		if (pContext != null)
//		{
//			pPE = pContext.getPEDroppedOn();
//			msg += pPE.toString();
//		}
//		addMessage(msg);
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPostDrop"); 
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
	{
		addMessage("onDrawingAreaPreFileRemoved"); 
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
	{
		addMessage("onDrawingAreaFileRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink#onDrawingAreaPreRetrieveElementSynchState(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreRetrieveElementSynchState(IPresentationElementSyncState pPresentationElementSyncState, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPreRetrieveElementSynchState");
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink#onDrawingAreaPostRetrieveElementSynchState(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaPostRetrieveElementSynchState(IPresentationElementSyncState pPresentationElementSyncState, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPostRetrieveElementSynchState");
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink#onDrawingAreaPrePresentationElementPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPrePresentationElementPerformSync(IPresentationElementPerformSyncContext pPresentationElementSyncContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPrePresentationElementPerformSync");
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink#onDrawingAreaPostPresentationElementPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaPostPresentationElementPerformSync(IPresentationElementPerformSyncContext pPresentationElementSyncContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPostPresentationElementPerformSync");
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink#onDrawingAreaPreDiagramPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreDiagramPerformSync(IDiagramPerformSyncContext pDiagramSyncContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPreDiagramPerformSync");
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink#onDrawingAreaPostDiagramPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPostDiagramPerformSync(IDiagramPerformSyncContext pDiagramSyncContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaPostDiagramPerformSync");
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink#onDrawingAreaContextMenuPrepare(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaContextMenuPrepare(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		addMessage("onDrawingAreaContextMenuPrepare");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink#onDrawingAreaContextMenuPrepared(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaContextMenuPrepared(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		addMessage("onDrawingAreaContextMenuPrepared");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink#onDrawingAreaContextMenuHandleDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaContextMenuHandleDisplay(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		addMessage("onDrawingAreaContextMenuHandleDisplay");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink#onDrawingAreaContextMenuSelected(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaContextMenuSelected(IDiagram pParentDiagram, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IResultCell cell)
	{
		addMessage("onDrawingAreaContextMenuSelected");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onSelect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
//	public void onSelect(IDiagram pParentDiagram, ETList<IPresentationElement> selectedItems, ICompartment pCompartment, IResultCell cell )
//	{
//		int numSel = 0;
//		if (selectedItems != null)
//		{
//			numSel = selectedItems.size();
//		}
//		String message = "OnSelect numSelected = " + numSel;
//		if (numSel == 1)
//		{
//			IPresentationElement pElem = selectedItems.get(0);
//			if (pElem != null)
//			{
//				IElement ele = pElem.getFirstSubject();
//				if (ele != null)
//				{
//					String metaType = ele.getElementType();
//					String xmiid = ele.getXMIID();
//					if (metaType != null && xmiid != null)
//					{
//						message += "(" + metaType + "," + xmiid + ")";
//					}
//				
//					if (pCompartment != null)
//					{
//						message += " Compartment = " + pCompartment.toString();
//					}
//				}
//			}
//		}
//		addMessage(message);
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onUnselect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onUnselect(IDiagram pParentDiagram, IPresentationElement[] unselectedItems, IResultCell cell)
	{
		int numSel = 0;
		if (unselectedItems != null)
		{
			numSel = unselectedItems.length;
		}
		String message = "OnUnselect numSelected = " + numSel;
		if (numSel == 1)
		{
			IPresentationElement pElem = unselectedItems[0];
			IElement ele = pElem.getFirstSubject();
			if (ele != null)
			{
				String metaType = ele.getElementType();
				String xmiid = ele.getXMIID();
				if (metaType != null && xmiid != null)
				{
					message += "(" + metaType + "," + xmiid + ")";
				}
			}
		}
		addMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink#onDrawingAreaCreateNode(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaCreateNode(IDiagram pParentDiagram, ICreateNodeContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaCreateNode");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink#onDrawingAreaDraggingNode(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaDraggingNode(IDiagram pParentDiagram, IDraggingNodeContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaDraggingNode");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaStartingEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaStartingEdge(IDiagram pParentDiagram, IEdgeCreateContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaStartingEdge");
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeShouldCreateBend(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaEdgeShouldCreateBend(IDiagram pParentDiagram, IEdgeCreateBendContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaEdgeShouldCreateBend");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaEdgeMouseMove(IDiagram pParentDiagram, IEdgeMouseMoveContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaEdgeMouseMove");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaFinishEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaFinishEdge(IDiagram pParentDiagram, IEdgeFinishContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaFinishEdge");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink#onDrawingAreaReconnectEdgeStart(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaReconnectEdgeStart(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaReconnectEdgeStart");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink#onDrawingAreaReconnectEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaReconnectEdgeMouseMove(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaReconnectEdgeMouseMove");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink#onDrawingAreaReconnectEdgeFinish(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaReconnectEdgeFinish(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IResultCell cell)
//	{
//		addMessage("onDrawingAreaReconnectEdgeFinish");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentSelected(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onCompartmentSelected(ICompartment pCompartment, boolean bSelected, IResultCell cell)
//	{
//		addMessage("onCompartmentSelected");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed, IResultCell cell)
//	{
//		addMessage("onCompartmentCollapsed");
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink#onGetNotificationTargets(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onGetNotificationTargets(IDiagram pDiagram, INotificationTargets pTargets, IResultCell cell)
//	{
//		addMessage("onGetNotificationTargets");
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onItemExpanding(IProjectTreeControl pParentControl, IProjectTreeExpandingContext pContext, IResultCell cell)
	{
		addMessage("onItemExpanding");
	}
   
   public void onItemExpandingWithFilter(IProjectTreeControl pParentControl, 
                                          IProjectTreeExpandingContext pContext, 
                                          FilteredItemManager manager, IResultCell cell)
    {
       onItemExpanding(pParentControl, pContext, cell);
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeforeEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onBeforeEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
	{
		addMessage("onBeforeEdit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onAfterEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAfterEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
	{
		addMessage("onAfterEdit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onDoubleClick(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDoubleClick(IProjectTreeControl pParentControl, IProjectTreeItem pItem, boolean isControl, boolean isShift, boolean isAlt, boolean isMeta, IResultCell cell)
	{
		addMessage("onDoubleClick");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onSelChanged(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSelChanged(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IResultCell cell)
	{
		addMessage("onSelChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onRightButtonDown(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled, int, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRightButtonDown(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeHandled pHandled, int nScreenLocX, int nScreenLocY, IResultCell cell)
	{
		addMessage("onRightButtonDown");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeginDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onBeginDrag(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		addMessage("onBeginDrag");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onMoveDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMoveDrag(IProjectTreeControl pParentControl, Transferable pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		addMessage("onModeDrag");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onEndDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, int, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onEndDrag(IProjectTreeControl pParentControl, Transferable pItem, int action, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		addMessage("onEndDrag");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuPrepare(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectTreeContextMenuPrepare(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
	{
		addMessage("onProjectTreeContextMenuPrepare");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuPrepared(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectTreeContextMenuPrepared(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
	{
		addMessage("onProjectTreeContextMenuPrepared");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuHandleDisplay(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectTreeContextMenuHandleDisplay(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
	{
		addMessage("onProjectTreeContextMenuHandleDisplay");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectTreeContextMenuSelected(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IResultCell cell)
	{
		addMessage("onProjectTreeContextMenuSelected");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlmessagingcore.IMessengerEventsSink#onMessageAdded(org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMessageAdded(IMessageData pMessage, IResultCell cell)
	{
		addMessage("onMessageAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreCreate(IWorkspacePreCreateEventPayload pEvent, IResultCell cell)
	{
		addMessage("onWorkspacePreCreate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceCreated(IWorkspace space, IResultCell cell)
	{
		addMessage("onWorkspaceCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreOpen(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreOpen(String fileName, IResultCell cell)
	{
		addMessage("onWorkspacePreOpen");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceOpened(IWorkspace space, IResultCell cell)
	{
		addMessage("onWorkspaceOpened");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreSave(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreSave(String fileName, IResultCell cell)
	{
		addMessage("onWorkspacePreSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceSaved(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceSaved(IWorkspace space, IResultCell cell)
	{
		addMessage("onWorkspaceSaved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreClose(IWorkspace space, IResultCell cell)
	{
		addMessage("onWorkspacePreClose");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceClosed(IWorkspace space, IResultCell cell)
	{
		addMessage("onWorkspaceClosed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
	{
		addMessage("onWSProjectPreCreate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectCreated(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		addMessage("onWSProjectPreOpen");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectOpened(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectOpened");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectPreRemove");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRemoved(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
	{
		addMessage("onWSProjectPreInsert");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectInserted(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectInserted");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
	{
		addMessage("onWSProjectPreRename");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
	{
		addMessage("onWSProjectRenamed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreClose(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectPreClose");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectClosed(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectClosed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreSave(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectPreSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectSaved(IWSProject project, IResultCell cell)
	{
		addMessage("onWSProjectSaved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreCreate(IWSProject wsProject, String location, String Name, String data, IResultCell cell)
	{
		addMessage("onWSElementPreCreate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementCreated(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreSave(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementPreSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementSaved(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementSaved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreRemove(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementPreRemove");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementRemoved(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreNameChanged(IWSElement element, String proposedValue, IResultCell cell)
	{
		addMessage("onWSElementPreNameChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementNameChanged(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementNameChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreOwnerChange(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreOwnerChange(IWSElement element, IWSProject newOwner, IResultCell cell)
	{
		addMessage("onWSElementPreOwnerChange");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementOwnerChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementOwnerChanged(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementOwnerChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreLocationChanged(IWSElement element, String proposedLocation, IResultCell cell)
	{
		addMessage("onWSElementPreLocationChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementLocationChanged(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementLocationChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreDataChanged(IWSElement element, String newData, IResultCell cell)
	{
		addMessage("onWSElementPreDataChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementDataChanged(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementDataChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreDocChanged(IWSElement element, String doc, IResultCell cell)
	{
		addMessage("onWSElementPreDocChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementDocChanged(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementDocChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreAliasChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreAliasChanged(IWSElement element, String proposedValue, IResultCell cell)
	{
		addMessage("onWSElementPreAliasChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementAliasChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementAliasChanged(IWSElement element, IResultCell cell)
	{
		addMessage("onWSElementAliasChanged");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddInEventsSink#onAddInLoaded(org.netbeans.modules.uml.core.addinframework.IAddIn, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onAddInLoaded(IAddIn pAddIn, IResultCell cell)
//	{
//		addMessage("onAddInLoaded");
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddInEventsSink#onAddInUnLoaded(org.netbeans.modules.uml.core.addinframework.IAddIn, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onAddInUnLoaded(IAddIn pAddIn, IResultCell cell)
//	{
//		addMessage("onAddInUnLoaded");
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreModified(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementPreModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementModified(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementModified " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventsSink#onMetaAttributePreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMetaAttributePreModified(IMetaAttributeModifiedEventPayload Payload, IResultCell cell)
	{
		addMessage("onMetaAttributePreModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventsSink#onMetaAttributeModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMetaAttributeModified(IMetaAttributeModifiedEventPayload Payload, IResultCell cell)
	{
		addMessage("onMetaAttributeModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDocumentationPreModified(IElement element, String doc, IResultCell cell)
	{
		addMessage("onDocumentationPreModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDocumentationModified(IElement element, IResultCell cell)
	{
		addMessage("onDocumentationModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink#onPreElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreElementAddedToNamespace(INamespace space, INamedElement elementToAdd, IResultCell cell)
	{
		addMessage("onPreElementAddedToNamespace " + getElementType(elementToAdd));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink#onElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementAddedToNamespace(INamespace space, INamedElement elementAdded, IResultCell cell)
	{
		addMessage("onElementAddedToNamespace " + getElementType(elementAdded));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		addMessage("onPreNameModified " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNameModified(INamedElement element, IResultCell cell)
	{
		addMessage("onNameModified " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell)
	{
		addMessage("onPreVisibilityModified ");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onVisibilityModified(INamedElement element, IResultCell cell)
	{
		addMessage("onVisibilityModified ");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		addMessage("onPreAliasNameModified ");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAliasNameModified(INamedElement element, IResultCell cell)
	{
		addMessage("onAliasNameModified " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String)
	 */
	public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell)
	{
		addMessage("onPreNameCollision " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
	 */
	public void onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell )
	{
		addMessage("onNameCollision " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onPreInvalidData(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreInvalidData(String ErrorData, IResultCell cell)
	{
		addMessage("onPreInvalidData");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onInvalidData(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onInvalidData(String ErrorData, IResultCell cell)
	{
		addMessage("onInvalidData");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onPreOverstrike(boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreOverstrike(boolean bOverstrike, IResultCell cell)
	{
		addMessage("onPreOverstrike");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onOverstrike(boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onOverstrike(boolean bOverstrike, IResultCell cell)
	{
		addMessage("onOverstrike");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onPreActivate(org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreActivate(IEditControl pControl, IResultCell cell)
	{
		addMessage("onPreActivate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onActivate(org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onActivate(IEditControl pControl, IResultCell cell)
	{
		addMessage("onActivate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onDeactivate(org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDeactivate(IEditControl pControl, IResultCell cell)
	{
		addMessage("onDeactivate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#setEventOwner(int)
	 */
	public void setEventOwner(int pOwner)
	{
		addMessage("setEventOwner");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onPreCommit(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreCommit(IResultCell cell)
	{
		addMessage("onPreCommit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink#onPostCommit(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPostCommit(IResultCell cell)
	{
		addMessage("onPostCommit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogInit(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectTreeFilterDialogInit(IFilterDialog dialog, IResultCell cell)
	{
		addMessage("onProjectTreeFilterDialogInit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogOKActivated(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectTreeFilterDialogOKActivated(IFilterDialog dialog, IResultCell cell)
	{
		addMessage("onProjectTreeFilterDialogOKActivated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink#onPreRelationValidate(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationValidate(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onPreRelationValidate");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink#onRelationValidated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationValidated(IRelationProxy Payload, IResultCell cell)
	{		
		addMessage("onRelationValidated");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreCreate(String ElementType, IResultCell cell)
	{
		addMessage("onElementPreCreate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementCreated(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementCreated " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDelete(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementPreDelete");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDeleted(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementDeleted " + getElementType(element));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementPreDuplicated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDuplicated(IVersionableElement element, IResultCell cell)
	{
		addMessage("onElementDuplicated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeaturePreAdded(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("onFeaturePreAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeatureAdded(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("onFeatureAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeaturePreRemoved(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("Classifier onFeaturePreRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeatureRemoved(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("Classifier onFeatureRemoved");
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onEnumerationLiteralAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
	{
		addMessage("onEnumerationLiteralAdded");
	}
    
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onEnumerationLiteralPreAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
	{
		addMessage("onEnumerationLiteralPreAdded");
	}

    /* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeaturePreMoved(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("Classifier onFeaturePreMoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeatureMoved(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("Classifier onFeatureMoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeaturePreDuplicatedToClassifier(IClassifier classifier, IFeature feature, IResultCell cell)
	{
		addMessage("Classifier onFeaturePreDuplicatedToClassifier");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeatureDuplicatedToClassifier(IClassifier pOldClassifier, IFeature pOldFeature, IClassifier pNewClassifier, IFeature pNewFeature, IResultCell cell)
	{
		addMessage("Classifier onFeatureDuplicatedToClassifier");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAbstractModified(IClassifier feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Classifier onPreAbstractModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAbstractModified(IClassifier feature, IResultCell cell)
	{
		addMessage("Classifier onAbstractModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreLeafModified(IClassifier feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Classifier onPreLeafModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onLeafModified(IClassifier feature, IResultCell cell)
	{
		addMessage("Classifier onLeafModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreTransientModified(IClassifier feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Classifier onPreTransientModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onTransientModified(IClassifier feature, IResultCell cell)
	{
		addMessage("Classifier onTransientModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreTemplateParameterAdded(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
	{
		addMessage("Classifier onPreTemplateParameterAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onTemplateParameterAdded(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
	{
		addMessage("Classifier onTemplateParameterAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreTemplateParameterRemoved(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
	{
		addMessage("Classifier onPreTemplateParameterRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onTemplateParameterRemoved(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
	{
		addMessage("Classifier onTemplateParameterRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreTransform(IClassifier classifier, String newForm, IResultCell cell)
	{
		addMessage("onPreTransform");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onTransformed(IClassifier classifier, IResultCell cell)
	{
		addMessage("onTransformed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onPreStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreStaticModified(IFeature feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreStaticModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onStaticModified(IFeature feature, IResultCell cell)
	{
		addMessage("onStaticModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onPreNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNativeModified(IFeature feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreNativeModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNativeModified(IFeature feature, IResultCell cell)
	{
		addMessage("onNativeModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreChangeabilityModified(IStructuralFeature feature, int proposedValue, IResultCell cell)
	{
		addMessage("onPreChangeabilityModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onChangeabilityModified(IStructuralFeature feature, IResultCell cell)
	{
		addMessage("onChangeabilityModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreVolatileModified(IStructuralFeature feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Structural Feature onPreVolatileModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onVolatileModified(IStructuralFeature feature, IResultCell cell)
	{
		addMessage("Structural Feature onVolatileModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreTransientModified(IStructuralFeature feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Structural Feature onPreTransientModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onTransientModified(IStructuralFeature feature, IResultCell cell)
	{
		addMessage("Structural Feature onTransientModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onConcurrencyPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onConcurrencyPreModified(IBehavioralFeature feature, int proposedValue, IResultCell cell)
	{
		addMessage("onConcurrencyPreModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onConcurrencyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onConcurrencyModified(IBehavioralFeature feature, IResultCell cell)
	{
		addMessage("onConcurrencyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreHandledSignalAdded(IBehavioralFeature feature, ISignal proposedValue, IResultCell cell)
	{
		addMessage("onPreHandledSignalAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onHandledSignalAdded(IBehavioralFeature feature, IResultCell cell)
	{
		addMessage("onHandledSignalAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreHandledSignalRemoved(IBehavioralFeature feature, ISignal proposedValue, IResultCell cell)
	{
		addMessage("onPreHandledSignalRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onHandledSignalRemoved(IBehavioralFeature feature, IResultCell cell)
	{
		addMessage("onHandledSignalRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreParameterAdded(IBehavioralFeature feature, IParameter parm, IResultCell cell)
	{
		addMessage("onPreParameterAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onParameterAdded(IBehavioralFeature feature, IParameter parm, IResultCell cell)
	{
		addMessage("onParameterAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
	{
		addMessage("onPreParameterRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
	{
		addMessage("onParameterRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAbstractModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Behavior Feature onPreAbstractModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAbstractModified(IBehavioralFeature feature, IResultCell cell)
	{
		addMessage("Behavior Feature onAbstractModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreStrictFPModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("Behavior Feature onPreStrictFPModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onStrictFPModified(IBehavioralFeature feature, IResultCell cell)
	{
		addMessage("Behavior Feature onStrictFPModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultExpModified(IParameter feature, IExpression proposedValue, IResultCell cell)
	{
		addMessage("onPreDefaultExpModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultExpModified(IParameter feature, IResultCell cell)
	{
		addMessage("onDefaultExpModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultExpBodyModified(IParameter feature, String bodyValue, IResultCell cell)
	{
		addMessage("onPreDefaultExpBodyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultExpBodyModified(IParameter feature, IResultCell cell)
	{
		addMessage("onDefaultExpBodyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultExpLanguageModified(IParameter feature, String language, IResultCell cell)
	{
		addMessage("onPreDefaultExpLanguageModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultExpLanguageModified(IParameter feature, IResultCell cell)
	{
		addMessage("onDefaultExpLanguageModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDirectionModified(IParameter feature, int proposedValue, IResultCell cell)
	{
		addMessage("onPreDirectionModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDirectionModified(IParameter feature, IResultCell cell)
	{
		addMessage("onDirectionModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreMultiplicityModified(ITypedElement element, IMultiplicity proposedValue, IResultCell cell)
	{
		addMessage("onPreMultiplicityModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMultiplicityModified(ITypedElement element, IResultCell cell)
	{
		addMessage("onMultiplicityModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreTypeModified(ITypedElement element, IClassifier proposedValue, IResultCell cell)
	{
		addMessage("onPreTypeModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onTypeModified(ITypedElement element, IResultCell cell)
	{
		addMessage("onTypeModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreLowerModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell)
	{
		addMessage("onPreLowerModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onLowerModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
	{
		addMessage("onLowerModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell)
	{
		addMessage("onPreUpperModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
	{
		addMessage("onUpperModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRangeAdded(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
	{
		addMessage("onPreRangeAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRangeAdded(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
	{
		addMessage("onRangeAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRangeRemoved(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
	{
		addMessage("onPreRangeRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRangeRemoved(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
	{
		addMessage("onRangeRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreOrderModified(ITypedElement element, IMultiplicity mult, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreOrderModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onOrderModified(ITypedElement element, IMultiplicity mult, IResultCell cell)
	{
		addMessage("onOrderModified");
	}
        
        /**
         * Fired when the collection type property is changed on the passed in
         * range.
         * @param element The type that owned the multilicity element
         * @param mult The multiplicity
         * @param range The multiplicity range that changed
         * @param cell The event result.
         */
        public void onCollectionTypeModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
        {
            addMessage("onCollectionTypeModified");
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultPreModified(IAttribute attr, IExpression proposedValue, IResultCell cell)
	{
		addMessage("onDefaultPreModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultModified(IAttribute attr, IResultCell cell)
	{
		addMessage("onDefaultModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultBodyModified(IAttribute feature, String bodyValue, IResultCell cell)
	{
		addMessage("onPreDefaultBodyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
	{
		addMessage("onDefaultBodyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultLanguageModified(IAttribute feature, String language, IResultCell cell)
	{
		addMessage("onPreDefaultLanguageModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
	{
		addMessage("onDefaultLanguageModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDerivedModified(IAttribute feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreDerivedModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDerivedModified(IAttribute feature, IResultCell cell)
	{
		addMessage("onDerivedModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPrePrimaryKeyModified(IAttribute feature, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPrePrimaryKeyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
	{
		addMessage("onPrimaryKeyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onConditionPreAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
	{
		addMessage("onConditionPreAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onConditionAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
	{
		addMessage("onConditionAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onConditionPreRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
	{
		addMessage("onConditionPreRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onConditionRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
	{
		addMessage("onConditionRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onPreQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreQueryModified(IOperation oper, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreQueryModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onQueryModified(IOperation oper, IResultCell cell)
	{
		addMessage("onQueryModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRaisedExceptionPreAdded(IOperation oper, IClassifier pException, IResultCell cell)
	{
		addMessage("onRaisedExceptionPreAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRaisedExceptionAdded(IOperation oper, IClassifier pException, IResultCell cell)
	{
		addMessage("onRaisedExceptionAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRaisedExceptionPreRemoved(IOperation oper, IClassifier pException, IResultCell cell)
	{
		addMessage("onRaisedExceptionPreRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRaisedExceptionRemoved(IOperation oper, IClassifier pException, IResultCell cell)
	{
		addMessage("onRaisedExceptionRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onPreOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreOperationPropertyModified(IOperation oper, int nKind, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreOperationPropertyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onOperationPropertyModified(IOperation oper, int nKind, IResultCell cell)
	{
		addMessage("onOperationPropertyModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
	{
		addMessage("onCoreProductPreInit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
	{
		addMessage("onCoreProductInitialized");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
	{
		addMessage("onCoreProductPreQuit");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
	{
		addMessage("onCoreProductPreSaved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
	{
		addMessage("onCoreProductSaved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink#onPreInitialized(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreInitialized(String proc, IResultCell cell)
	{
		addMessage("Roundtrip processor onPreInitialized");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink#onInitialized(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onInitialized(IRequestProcessor proc, IResultCell cell)
	{
		addMessage("Roundtrip processor onInitialized");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink#onPreAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAttributeChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onPreAttributeChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink#onAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAttributeChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onAttributeChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink#onPreClassChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreClassChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onPreClassChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink#onClassChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onClassChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onClassChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink#onPreOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreOperationChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onPreOperationChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink#onOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onOperationChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onOperationChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink#onPrePackageChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPrePackageChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onPrePackageChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink#onPackageChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPackageChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onPackageChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink#onPreRelationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onPreRelationChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink#onRelationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationChangeRequest(IChangeRequest newVal, IResultCell cell)
	{
		addChangeRequestMessage("onRelationChangeRequest", newVal);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink#onPrePackageImport(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPrePackageImport(IPackage ImportingPackage, IPackage ImportedPackage, INamespace owner, IResultCell cell)
	{
		addMessage("OnPrePackageImport");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink#onPackageImported(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPackageImported(IPackageImport packImport, IResultCell cell)
	{
		addMessage("OnPackageImport");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink#onPreElementImport(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreElementImport(IPackage ImportingPackage, IElement ImportedElement, INamespace owner, IResultCell cell)
	{
		addMessage("onPreElementImport");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink#onElementImported(org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementImported(IElementImport elImport, IResultCell cell)
	{
		addMessage("onElementImport");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink#onPreDisposeElements()
	 */
	public void onPreDisposeElements( ETList<IVersionableElement> pElements, IResultCell cell )
	{
		addMessage("onPreDisposeElements");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink#onDisposedElements()
	 */
	public void onDisposedElements( ETList<IVersionableElement> pElements, IResultCell cell )
	{
		addMessage("onDisposedElements");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationEndModified(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onPreRelationEndModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationEndModified(IRelationProxy Payload, IResultCell cell)
	{
		addMessage("onPreRelationEndModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationEndAdded(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onPreRelationEndAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationEndAdded(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onRelationEndAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onPreRelationEndRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onRelationEndRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationCreated(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onPreRelationCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationCreated(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onRelationCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRelationDeleted(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onPreRelationDeleted");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRelationDeleted(IRelationProxy proxy, IResultCell cell)
	{
		addMessage("onRelationDeleted");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceChange(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceChange(String Name, IPropertyElement pElement, IResultCell cell)
	{
		addMessage("onPreferenceChange");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceAdd(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceAdd(String Name, IPropertyElement pElement, IResultCell cell)
	{
		addMessage("onPreferenceAdd");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceRemove(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceRemove(String Name, IPropertyElement pElement, IResultCell cell)
	{
		addMessage("onPreferenceRemove");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferencesChange(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferencesChange(IPropertyElement[] pElements, IResultCell cell)
	{
		addMessage("onPreferencesChange");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementPreLoaded(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onExternalElementPreLoaded(String uri, IResultCell cell)
	{
		addMessage("onExternalElementPreLoaded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementLoaded(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onExternalElementLoaded(IVersionableElement element, IResultCell cell)
	{
		addMessage("onExternalElementLoaded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onPreInitialExtraction(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreInitialExtraction(String fileName, IVersionableElement element, IResultCell cell)
	{
		addMessage("onPreInitialExtraction");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onInitialExtraction(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onInitialExtraction(IVersionableElement element, IResultCell cell)
	{
		addMessage("onInitialExtraction");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.scmintegration.ISCMEventsSink#onPreFeatureExecuted(int, org.netbeans.modules.uml.ui.support.scmintegration.ISCMItemGroup, org.netbeans.modules.uml.ui.support.scmintegration.ISCMOptions, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreFeatureExecuted(int kind, ISCMItemGroup Group, ISCMOptions pOptions, IResultCell cell)
	{
		addMessage("onPreFeatureExecuted " + translateSCMKind(kind));
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.scmintegration.ISCMEventsSink#onFeatureExecuted(int, org.netbeans.modules.uml.ui.support.scmintegration.ISCMItemGroup, org.netbeans.modules.uml.ui.support.scmintegration.ISCMOptions, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeatureExecuted(int kind, ISCMItemGroup Group, ISCMOptions pOptions, IResultCell cell)
	{
		addMessage("onFeatureExecuted " + translateSCMKind(kind));
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink#onPreStereotypeApplied(java.lang.Object, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreStereotypeApplied(Object pStereotype, IElement element, IResultCell cell)
	{
		addMessage("onPreStereotypeApplied");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink#onStereotypeApplied(java.lang.Object, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onStereotypeApplied(Object pStereotype, IElement element, IResultCell cell)
	{
		addMessage("onStereotypeApplied");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink#onPreStereotypeDeleted(java.lang.Object, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreStereotypeDeleted(Object pStereotype, IElement element, IResultCell cell)
	{
		addMessage("onPreStereotypeDeleted");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink#onStereotypeDeleted(java.lang.Object, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onStereotypeDeleted(Object pStereotype, IElement element, IResultCell cell)
	{
		addMessage("onStereotypeDeleted");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onPreImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
	 */
	public void onPreImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell )
	{
		addMessage("onPreImpacted");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
	 */
	public void onImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell )
	{
		addMessage("onImpacted");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreFinalModified(IRedefinableElement element, boolean proposedValue, IResultCell cell)
	{
		addMessage("onPreFinalModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFinalModified(IRedefinableElement element, IResultCell cell)
	{
		addMessage("onFinalModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefinedElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRedefinedElementAdded(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
	{
		addRedefinedMessage("onPreRedefinedElementAdded", redefiningElement, redefinedElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefinedElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRedefinedElementAdded(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
	{
		addRedefinedMessage("onRedefinedElementAdded", redefiningElement, redefinedElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefinedElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRedefinedElementRemoved(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
	{
		addRedefinedMessage("onPreRedefinedElementRemoved", redefiningElement, redefinedElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefinedElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRedefinedElementRemoved(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
	{
		addRedefinedMessage("onRedefinedElementRemoved", redefiningElement, redefinedElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefiningElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRedefiningElementAdded(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
	{
		addRedefiningMessage("onPreRedefiningElementAdded", redefinedElement, redefiningElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefiningElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRedefiningElementAdded(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
	{
		addRedefiningMessage("onRedefiningElementAdded", redefinedElement, redefiningElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefiningElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreRedefiningElementRemoved(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
	{
		addRedefiningMessage("onPreRedefiningElementRemoved", redefinedElement, redefiningElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefiningElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRedefiningElementRemoved(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
	{
		addRedefiningMessage("onRedefiningElementRemoved", redefinedElement, redefiningElement);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink#onPreChangeRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IResultCell cell)
	{
		addMessage("onPreChangeRepresentingClassifier");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink#onChangeRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IResultCell cell)
	{
		addMessage("onChangeRepresentingClassifier");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onPreWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreWeightModified(IActivityEdge pEdge, String newValue, IResultCell cell)
	{
		addMessage("onPreWeightModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWeightModified(IActivityEdge pEdge, IResultCell cell)
	{
		addMessage("onWeightModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onPreGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreGuardModified(IActivityEdge pEdge, String newValue, IResultCell cell)
	{
		addMessage("onPreGuardModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onGuardModified(IActivityEdge pEdge, IResultCell cell)
	{
		addMessage("onGuardModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
	{
		addMessage("onPreModeModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onModeModified(IProject pProject, IResultCell cell)
	{
		addMessage("onModeModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
	{
		addMessage("onPreDefaultLanguageModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
	{
		addMessage("onDefaultLanguageModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreCreate(IWorkspace space, IResultCell cell)
	{
		addMessage("onProjectPreCreate");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectCreated(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectCreated(IProject Project, IResultCell cell)
	{
		addMessage("onProjectCreated");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		addMessage("onProjectPreOpen");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectOpened(IProject Project, IResultCell cell)
	{
		addMessage("onProjectOpened");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreRename(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreRename(IProject Project, String newName, IResultCell cell)
	{
		addMessage("onProjectPreRename");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectRenamed(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectRenamed(IProject Project, String oldName, IResultCell cell)
	{
		addMessage("onProjectRenamed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreClose(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreClose(IProject Project, IResultCell cell)
	{
		addMessage("onProjectPreClose");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectClosed(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectClosed(IProject Project, IResultCell cell)
	{
		addMessage("onProjectClosed");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreSave(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreSave(IProject Project, IResultCell cell)
	{
		addMessage("onProjectPreSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectSaved(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectSaved(IProject Project, IResultCell cell)
	{
		addMessage("onProjectSaved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		addMessage("onPreReferencedLibraryAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		addMessage("onReferencedLibraryAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		addMessage("onPreReferencedLibraryRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		addMessage("onReferencedLibraryRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onPreQualifierAttributeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreQualifierAttributeAdded(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
	{
		addMessage("onPreQualifierAttributeAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onQualifierAttributeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onQualifierAttributeAdded(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
	{
		addMessage("onQualifierAttributeAdded");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onPreQualifierAttributeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
	{
		addMessage("onPreQualifierAttributeRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onQualifierAttributeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
	{
		addMessage("onQualifierAttributeRemoved");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onPreFileNameModified(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreFileNameModified(IArtifact pArtifact, String newFileName, IResultCell cell)
	{
		addMessage("onPreFileNameModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onFileNameModified(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFileNameModified(IArtifact pArtifact, String oldFileName, IResultCell cell)
	{
		addMessage("onFileNameModified");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onPreDirty(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDirty(IArtifact pArtifact, IResultCell cell)
	{
		addMessage("onPreDirty");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onDirty(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDirty(IArtifact pArtifact, IResultCell cell)
	{
		addMessage("onDirty");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onPreSave(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreSave(IArtifact pArtifact, String fileName, IResultCell cell)
	{
		addMessage("onPreSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onSave(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSave(IArtifact pArtifact, String fileName, IResultCell cell)
	{
		addMessage("onSave");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink#onPreSourceDirModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreSourceDirModified(IPackage element, String proposedSourceDir, IResultCell cell)
	{
		addMessage("onPreSourceDirModified");
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink#onSourceDirModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSourceDirModified(IPackage element, IResultCell cell)
	{
		addMessage("onSourceDirModified");
		
	}

	/**
	 * @param addin
	 */
	public void setParent(EventLoggingAddin addin)
	{
		m_Parent = addin;
	}

	private void addRedefinedMessage(String name, IRedefinableElement redefiningEle, IRedefinableElement redefinedEle)
	{
		INamedElement redefiningNamedEle = null;
		INamedElement redefinedNamedEle = null;
		String message = name + " redefining element = ";
		if (redefiningEle != null)
		{
			String redefiningName = redefiningEle.getName();
			IElement redefiningOwner = redefiningEle.getOwner();
			if (redefiningOwner != null && redefiningOwner instanceof INamedElement)
			{
				redefiningNamedEle = (INamedElement)redefiningOwner;
				message += redefiningNamedEle.getName() + "::";
			}
			message += redefiningName;
		}
		
		message += " redefined = ";
		if (redefinedEle != null)
		{
			String redefinedName = redefinedEle.getName();
			IElement redefinedOwner = redefinedEle.getOwner();
			if (redefinedOwner != null && redefinedOwner instanceof INamedElement)
			{
				redefinedNamedEle = (INamedElement)redefinedOwner;
				message += redefinedNamedEle.getName() + "::";
			}
			message += redefinedName;
		}
		addMessage(message);
	}

	private void addRedefiningMessage(String name, IRedefinableElement redefinedEle, IRedefinableElement redefiningEle)
	{
		INamedElement redefiningNamedEle = null;
		INamedElement redefinedNamedEle = null;
		String message = name + " redefining element = ";
		if (redefinedEle != null)
		{
			String redefinedName = redefinedEle.getName();
			IElement redefinedOwner = redefinedEle.getOwner();
			if (redefinedOwner != null && redefinedOwner instanceof INamedElement)
			{
				redefinedNamedEle = (INamedElement)redefinedOwner;
				message += redefinedNamedEle.getName() + "::";
			}
			message += redefinedName;
		}
		message += " redefining = ";
		if (redefiningEle != null)
		{
			String redefiningName = redefiningEle.getName();
			IElement redefiningOwner = redefiningEle.getOwner();
			if (redefiningOwner != null && redefiningOwner instanceof INamedElement)
			{
				redefiningNamedEle = (INamedElement)redefiningOwner;
				message += redefiningNamedEle.getName() + "::";
			}
			message += redefiningName;
		}
		addMessage(message);
	}
	
	/**
	 *
	 * Gets the translated names of the SCMFeatureKind enum
	 *
	 * @param kind[in] The kind
	 *
	 * @return The string version
	 *
	 */
	private String translateSCMKind(int kind)
	{
		String retStr = "";
		switch (kind)
		{
			case ISCMEnums.FK_GET_LATEST_VERSION :
				retStr = "Get Latest";
				break;

			case ISCMEnums.FK_CHECK_IN :
				retStr = "Check in";
				break;

			case ISCMEnums.FK_CHECK_OUT :
				retStr = "Check out";
				break;

			case ISCMEnums.FK_UNDO_CHECK_OUT :
				retStr = "Undo Check Out";
				break;

			case ISCMEnums.FK_SHOW_HISTORY :
				retStr = "Show History";
				break;

			case ISCMEnums.FK_SHOW_DIFF :
				retStr = "Show Diff";
				break;

			case ISCMEnums.FK_ADD_TO_SOURCE_CONTROL :
				retStr = "Add to Source Control";
				break;

			case ISCMEnums.FK_REMOVE_FROM_SOURCE_CONTROL :
				retStr = "Remove From Source Control";
				break;

			default :
				break;
		}
		return retStr;
	}
	
}



