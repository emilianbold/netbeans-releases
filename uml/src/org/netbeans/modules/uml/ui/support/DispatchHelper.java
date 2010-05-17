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


/*
 *
 * Created on May 30, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

import java.util.HashMap;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductEventDispatcher;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUnknownClassifierEventsSink;
import org.netbeans.modules.uml.core.metamodel.dynamics.IDynamicsEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventDispatcher;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManagerEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessengerEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElementModifiedEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlEventDispatcher;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventDispatcher;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink;
import org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher;
import org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink;
import org.netbeans.modules.uml.ui.controls.filter.ProjectTreeFilterDialogEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeEventDispatcherImpl;
import org.netbeans.modules.uml.core.scm.ISCMEventDispatcher;
import org.netbeans.modules.uml.core.scm.ISCMEventsSink;
import org.netbeans.modules.uml.ui.support.diagramsupport.DrawingAreaEventDispatcherImpl;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventsSink;

/**
 * DispatcherHelper is used to register and revoke to various event dispatchers.
 *
 * @author Trey Spiva
 */
public class DispatchHelper
{

    private HashMap<Object, Integer> m_CookieMap = new HashMap<Object, Integer>();
    private IWorkspaceEventDispatcher m_WorkspaceEventDispatcher = null;
    private IStructureEventDispatcher m_StructureEventDispatcher = null;
    private IEventDispatchController m_EventController = null;
    private IClassifierEventDispatcher m_ClassifierEventDispatcher = null;
    private IDynamicsEventDispatcher m_DynamicsEventDispatcher = null;
    private IElementLifeTimeEventDispatcher m_LifeTimeEventDispatcher = null;
    private IRelationValidatorEventDispatcher m_RelationValidatorEventDispatcher = null;
    private static IProjectTreeEventDispatcher m_ProjectTreeEventDispatcher = null;
    private static IDrawingAreaEventDispatcher m_DrawingAreaDispatcher = null;
    private static IPreferenceManagerEventDispatcher m_PreferenceEventDispatcher = null;
    private static IProjectTreeFilterDialogEventDispatcher m_ProjectTreeFilterEventDispatcher = null;
    private IWorkspaceEventDispatcher m_WorkspaceEventDispatcherDP = null;
    private IActivityEventDispatcher m_ActivityEventDispatcher = null;
    //**************************************************
    // Workspace Event Sinks
    //**************************************************

    public void registerForWorkspaceEvents(IWorkspaceEventsSink sink)
    {
        if (sink != null)
        {
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcher();
            if (dispatcher != null)
            {
                dispatcher.registerForWorkspaceEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
    }

    public void revokeWorkspaceSink(IWorkspaceEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcher();
            dispatcher.revokeWorkspaceSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }

    public void registerForWSProjectEvents(IWSProjectEventsSink sink)
    {
        if (sink != null)
        {
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcher();
            if (dispatcher != null)
            {
                dispatcher.registerForWSProjectEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeWSProjectSink(IWSProjectEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcher();
            dispatcher.revokeWSProjectSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }

    public void registerForWSElementEvents(IWSElementEventsSink pSink)
    {
    }

    public void revokeWSElementSink(IWSElementEventsSink pSink)
    {
    }

    public void registerForWSElementModifiedEvents(IWSElementModifiedEventsSink pSink)
    {
    }

    public void revokeWSElementModifiedSink(IWSElementModifiedEventsSink pSink)
    {
    }
//		public void registerForWorkspaceEventsDP(IEventsSink pSink){}
//		public void revokeWorkspaceSinkDP(IEventsSink pSink){}
//		public void registerForWSProjectEventsDP(IEventsSink pSink){}
//		public void revokeWSProjectSinkDP(IEventsSink pSink){}

    //	**************************************************
    // Perference Manager Events
    //**************************************************
    public void registerForPreferenceManagerEvents(IPreferenceManagerEventsSink sink)
    {
        if (sink != null)
        {
            IPreferenceManagerEventDispatcher dispatcher = getPreferenceManagerDispatcher();
            if (dispatcher != null)
            {
                dispatcher.registerPreferenceManagerEvents(sink);
                //int cookie = dispatcher.registerPreferenceManagerEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokePreferenceManagerSink(IPreferenceManagerEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IPreferenceManagerEventDispatcher dispatcher = getPreferenceManagerDispatcher();
            dispatcher.revokePreferenceManagerSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }
    //**************************************************
    // Core product dispatcher
    //**************************************************

    public void registerForInitEvents(ICoreProductInitEventsSink sink)
    {
        if (sink != null)
        {
            ICoreProductEventDispatcher dispatcher = getCoreProductDispatcher();
            if (dispatcher != null)
            {
                dispatcher.registerForInitEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeInitSink(ICoreProductInitEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            ICoreProductEventDispatcher dispatcher = getCoreProductDispatcher();
            dispatcher.revokeInitSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }
    //**************************************************
    // Structure Event Dispatcher
    //**************************************************

    public void registerForArtifactEvents(IArtifactEventsSink pSink)
    {
    }

    public void revokeArtifactSink(IArtifactEventsSink pSink)
    {
    }

    public void registerForProjectEvents(IProjectEventsSink sink)
    {
        if (sink != null)
        {
            IStructureEventDispatcher dispatcher = getStructureDispatcher();
            if (dispatcher != null)
            {
//				TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForProjectEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeProjectSink(IProjectEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IStructureEventDispatcher dispatcher = getStructureDispatcher();
            dispatcher.revokeProjectSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }

    //**************************************************
    // Activities Event Dispatcher
    //**************************************************
    public void registerForActivityEdgeEvents(IActivityEdgeEventsSink pSink)
    {

        if (pSink != null)
        {
            IActivityEventDispatcher dispatcher = getActivitiesDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForActivityEdgeEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeActivityEdgeSink(IActivityEdgeEventsSink pSink)
    {

        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IActivityEventDispatcher dispatcher = getActivitiesDispatcher();
            dispatcher.revokeActivityEdgeSink(pSink);
        }
    }

    //**************************************************
    // Drawing Area Event Dispatcher
    //**************************************************
    public void registerDrawingAreaEvents(IDrawingAreaEventsSink sink)
    {
        if (sink != null)
        {
            IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                int cookie = dispatcher.registerDrawingAreaEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeDrawingAreaSink(IDrawingAreaEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
            dispatcher.revokeDrawingAreaSink(sink);
        }
    }
    
    // TODO: meteora
//	public void registerDrawingAreaSynchEvents(IDrawingAreaSynchEventsSink sink)
//   {
//      if(sink != null)
//      {
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         if(dispatcher != null)
//         {
//            // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//            dispatcher.registerDrawingAreaSynchEvents(sink);
//            //m_CookieMap.put(sink, new Integer(cookie));
//         }
//
//      }
//      else
//      {
//         // TODO: I need to notify the caller that the register failed.
//      }
//   }
//
//	public void revokeDrawingAreaSynchSink(IDrawingAreaSynchEventsSink sink)
//   {
//      if(sink != null)
//      {
//         //Integer cookie = (Integer)m_CookieMap.get(sink);
//         //int value = cookie.intValue();
//
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         dispatcher.revokeDrawingAreaSynchSink(sink);
//      }
//   }
//
//	public void registerDrawingAreaContextMenuEvents(IDrawingAreaContextMenuEventsSink sink)
//   {
//      if(sink != null)
//      {
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         if(dispatcher != null)
//         {
//            // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//            dispatcher.registerDrawingAreaContextMenuEvents(sink);
//            //m_CookieMap.put(sink, new Integer(cookie));
//         }
//
//      }
//      else
//      {
//         // TODO: I need to notify the caller that the register failed.
//      }
//   }
//
//	public void revokeDrawingAreaContextMenuSink(IDrawingAreaContextMenuEventsSink sink)
//   {
//      if(sink != null)
//      {
//         //Integer cookie = (Integer)m_CookieMap.get(sink);
//         //int value = cookie.intValue();
//
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         dispatcher.revokeDrawingAreaContextMenuSink(sink);
//      }
//   }
//
//	public void registerDrawingAreaSelectionEvents(IDrawingAreaSelectionEventsSink sink)
//   {
//      if(sink != null)
//       {
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          if(dispatcher != null)
//          {
//             // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//             dispatcher.registerDrawingAreaSelectionEvents(sink);
//             //m_CookieMap.put(sink, new Integer(cookie));
//          }
//
//       }
//       else
//       {
//          // TODO: I need to notify the caller that the register failed.
//       }
//   }
//
//	public void revokeDrawingAreaSelectionSink(IDrawingAreaSelectionEventsSink sink)
//   {
//      if(sink != null)
//      {
//         //Integer cookie = (Integer)m_CookieMap.get(sink);
//         //int value = cookie.intValue();
//
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         dispatcher.revokeDrawingAreaSelectionSink(sink);
//      }
//   }
//
//	public void registerDrawingAreaAddNodeEvents(IDrawingAreaAddNodeEventsSink sink)
//   {
//      if(sink != null)
//       {
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          if(dispatcher != null)
//          {
//             // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//             int cookie = dispatcher.registerDrawingAreaAddNodeEvents(sink);
//             //m_CookieMap.put(sink, new Integer(cookie));
//          }
//
//       }
//       else
//       {
//          // TODO: I need to notify the caller that the register failed.
//       }
//   }
//
//	public void revokeDrawingAreaAddNodeSink(IDrawingAreaAddNodeEventsSink sink)
//   {
//      if(sink != null)
//      {
//         //Integer cookie = (Integer)m_CookieMap.get(sink);
//         //int value = cookie.intValue();
//
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         dispatcher.revokeDrawingAreaAddNodeSink(sink);
//      }
//   }
//
//	public void registerDrawingAreaAddEdgeEvents(IDrawingAreaAddEdgeEventsSink sink)
//   {
//      if(sink != null)
//       {
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          if(dispatcher != null)
//          {
//             // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//             int cookie = dispatcher.registerDrawingAreaAddEdgeEvents(sink);
//             //m_CookieMap.put(sink, new Integer(cookie));
//          }
//
//       }
//       else
//       {
//          // TODO: I need to notify the caller that the register failed.
//       }
//   }
//
//	public void revokeDrawingAreaAddEdgeSink(IDrawingAreaAddEdgeEventsSink sink)
//   {
//      if(sink != null)
//      {
//         //Integer cookie = (Integer)m_CookieMap.get(sink);
//         //int value = cookie.intValue();
//
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         dispatcher.revokeDrawingAreaAddEdgeSink(sink);
//      }
//   }
//
//	public void registerDrawingAreaReconnectEdgeEvents(IDrawingAreaReconnectEdgeEventsSink sink)
//   {
//      if(sink != null)
//       {
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          if(dispatcher != null)
//          {
//             // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//             int cookie = dispatcher.registerDrawingAreaReconnectEdgeEvents(sink);
//             //m_CookieMap.put(sink, new Integer(cookie));
//          }
//
//       }
//       else
//       {
//          // TODO: I need to notify the caller that the register failed.
//       }
//   }
//
//	public void revokeDrawingAreaReconnectEdgeSink(IDrawingAreaReconnectEdgeEventsSink sink)
//   {
//      if(sink != null)
//      {
//         //Integer cookie = (Integer)m_CookieMap.get(sink);
//         //int value = cookie.intValue();
//
//         IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//         dispatcher.revokeDrawingAreaReconnectEdgeSink(sink);
//      }
//   }
//
//	public void registerDrawingAreaCompartmentEvents(ICompartmentEventsSink sink)
//   {
//      if(sink != null)
//       {
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          if(dispatcher != null)
//          {
//             // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//             int cookie = dispatcher.registerDrawingAreaCompartmentEvents(sink);
//            // m_CookieMap.put(sink, new Integer(cookie));
//          }
//
//       }
//       else
//       {
//          // TODO: I need to notify the caller that the register failed.
//       }
//   }
//
//	public void revokeDrawingAreaCompartmentSink(ICompartmentEventsSink sink)
//   {
//      if(sink != null)
//       {
//          //Integer cookie = (Integer)m_CookieMap.get(sink);
//          //int value = cookie.intValue();
//
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          dispatcher.revokeDrawingAreaCompartmentSink(sink);
//       }
//   }
//
//	public void registerChangeNotificationTranslatorEvents(IChangeNotificationTranslatorSink sink)
//   {
//      if(sink != null)
//       {
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          if(dispatcher != null)
//          {
//             // TODO: We do not have a JCWrapper from the ProjectEventsSink.
//             int cookie = dispatcher.registerChangeNotificationTranslatorEvents(sink);
//             //m_CookieMap.put(sink, new Integer(cookie));
//          }
//
//       }
//       else
//       {
//          // TODO: I need to notify the caller that the register failed.
//       }
//   }
//
//	public void revokeChangeNotificationTranslatorSink(IChangeNotificationTranslatorSink sink)
//   {
//      if(sink != null)
//       {
//          //Integer cookie = (Integer)m_CookieMap.get(sink);
//          //int value = cookie.intValue();
//
//          IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
//          dispatcher.revokeChangeNotificationTranslatorSink(sink);
//       }
//   }

    //**************************************************
    // Messenger Dispatcher
    //**************************************************
    public void registerMessengerEvents(IMessengerEventsSink pSink)
    {
    }

    public void revokeMessengerSink(IMessengerEventsSink pSink)
    {
    }
    //**************************************************
    // VBA Dispatcher
    //**************************************************
    //public void registerVBAProjectEvents(IVBAProjectEventsSink pSink){}
    //public void revokeVBAProjectSink(IVBAProjectEventsSink pSink){}

    //**************************************************
    // Project Tree event dispatcher
    //**************************************************
    public void registerProjectTreeEvents(IProjectTreeEventsSink sink)
    {
        if (sink != null)
        {
            IProjectTreeEventDispatcher dispatcher = getProjectTreeDispatcher();
            if (dispatcher != null)
            {
//				int cookie = dispatcher.registerProjectTreeEvents(sink);
//				m_CookieMap.put(sink, new Integer(cookie));
                dispatcher.registerProjectTreeEvents(sink);
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeProjectTreeSink(IProjectTreeEventsSink sink)
    {

//		Integer cookie = (Integer)m_CookieMap.get(sink);
//		int value = cookie.intValue();
//
        IProjectTreeEventDispatcher dispatcher = getProjectTreeDispatcher();
        dispatcher.revokeProjectTreeSink(sink);
    }

    public void registerProjectTreeContextMenuEvents(IProjectTreeContextMenuEventsSink pSink)
    {
        if (pSink != null)
        {
            IProjectTreeEventDispatcher dispatcher = getProjectTreeDispatcher();
            if (dispatcher != null)
            {
//						int cookie = dispatcher.registerProjectTreeEvents(sink);
//						m_CookieMap.put(sink, new Integer(cookie));
                dispatcher.registerProjectTreeContextMenuEvents(pSink);
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeProjectTreeContextMenuSink(IProjectTreeContextMenuEventsSink pSink)
    {
        IProjectTreeEventDispatcher dispatcher = getProjectTreeDispatcher();
        dispatcher.revokeProjectTreeContextMenuSink(pSink);
    }
    //**************************************************
    // Add In Dispatcher
    //**************************************************
//	public void registerAddInEvents(IAddInEventsSink pSink){}
//	public void revokeAddInSink(IAddInEventsSink pSink){}

    //**************************************************
    // Element Change Event Dispatcher
    //**************************************************
    public void registerForElementModifiedEvents(IElementModifiedEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForElementModifiedEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeElementModifiedSink(IElementModifiedEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeElementModifiedSink(sink);
        }
    }

    public void registerForMetaAttributeModifiedEvents(IMetaAttributeModifiedEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForMetaAttributeModifiedEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeMetaAttributeModifiedSink(IMetaAttributeModifiedEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeMetaAttributeModifiedSink(sink);
        }
    }

    public void registerForDocumentationModifiedEvents(IDocumentationModifiedEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForDocumentationModifiedEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeDocumentationModifiedSink(IDocumentationModifiedEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeDocumentationModifiedSink(sink);
        }
    }

    public void registerForNamespaceModifiedEvents(INamespaceModifiedEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForNamespaceModifiedEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeNamespaceModifiedSink(INamespaceModifiedEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeNamespaceModifiedSink(sink);
        }
    }

    public void registerForNamedElementEvents(INamedElementEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForNamedElementEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeNamedElementSink(INamedElementEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeNamedElementSink(sink);
        }
    }

    public void registerForExternalElementEventsSink(IExternalElementEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForExternalElementEventsSink(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeExternalElementEventsSink(IExternalElementEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeExternalElementEventsSink(sink);
        }
    }

    public void registerForStereotypeEventsSink(IStereotypeEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForStereotypeEventsSink(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeStereotypeEventsSink(IStereotypeEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeStereotypeEventsSink(sink);
        }
    }

    public void registerForRedefinableElementModifiedEvents(IRedefinableElementModifiedEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForRedefinableElementModifiedEvents(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeRedefinableElementModifiedEvents(IRedefinableElementModifiedEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokeRedefinableElementModifiedEvents(sink);
        }
    }

    public void registerForPackageEvents(IPackageEventsSink sink)
    {
        if (sink != null)
        {
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForPackageEventsSink(sink);
                //m_CookieMap.put(sink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokePackageEvents(IPackageEventsSink sink)
    {
        if (sink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(sink);
            //int value = cookie.intValue();
            IElementChangeEventDispatcher dispatcher = getElementChangeDispatcher();
            dispatcher.revokePackageEventsSink(sink);
        }
    }
    //**************************************************
    // Edit control dispatcher
    //**************************************************
    //public void registerEditCtrlEvents(IEditCtrlEventsSink pSink){}
    //public void revokeEditCtrlSink(IEditCtrlEventsSink pSink){}

    //**************************************************
    // Project Tree Filter Dialog Event Dispatcher
    //**************************************************
    public void registerProjectTreeFilterDialogEvents(IProjectTreeFilterDialogEventsSink pSink)
    {
        if (pSink != null)
        {
            IProjectTreeFilterDialogEventDispatcher dispatcher = null;
            dispatcher = getProjectTreeFilterDialogDispatcher();

            if (dispatcher != null)
            {
                dispatcher.registerProjectTreeFilterDialogEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeProjectTreeFilterDialogSink(IProjectTreeFilterDialogEventsSink pSink)
    {
//    Integer cookie = (Integer)m_CookieMap.get(sink);
//    int value = cookie.intValue();
//
        IProjectTreeFilterDialogEventDispatcher dispatcher = null;
        dispatcher = getProjectTreeFilterDialogDispatcher();
        dispatcher.revokeProjectTreeFilterDialogEvents(pSink);
    }

    //**************************************************
    // Relation validator dispatcher
    //**************************************************
    public void registerForRelationValidatorEvents(IRelationValidatorEventsSink pSink)
    {

        if (pSink != null)
        {
            IRelationValidatorEventDispatcher dispatcher = getRelationDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForRelationValidatorEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeRelationValidatorSink(IRelationValidatorEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IRelationValidatorEventDispatcher dispatcher = getRelationDispatcher();
            dispatcher.revokeRelationValidatorSink(pSink);
        }
    }

    public void registerForRelationEvents(IRelationEventsSink pSink)
    {
        if (pSink != null)
        {
            IRelationValidatorEventDispatcher dispatcher = getRelationDispatcher();
            if (dispatcher != null)
            {
                dispatcher.registerForRelationEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeRelationSink(IRelationEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IRelationValidatorEventDispatcher dispatcher = getRelationDispatcher();
            dispatcher.revokeRelationSink(pSink);
        }
    }

    //**************************************************
    // Lifetime dispatcher
    //**************************************************
    public void registerForLifeTimeEvents(IElementLifeTimeEventsSink pSink)
    {
        if (pSink != null)
        {
            IElementLifeTimeEventDispatcher dispatcher = getLifeTimeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForLifeTimeEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeLifeTimeSink(IElementLifeTimeEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IElementLifeTimeEventDispatcher dispatcher = getLifeTimeDispatcher();
            dispatcher.revokeLifeTimeSink(pSink);
        }
    }

    public void registerElementDisposalEvents(IElementDisposalEventsSink pSink)
    {
        if (pSink != null)
        {
            IElementLifeTimeEventDispatcher dispatcher = getLifeTimeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForDisposalEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeElementDisposalEventsSink(IElementDisposalEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IElementLifeTimeEventDispatcher dispatcher = getLifeTimeDispatcher();
            dispatcher.revokeDisposalSink(pSink);
        }
    }

    public void registerUnknownClassifierEvents(IUnknownClassifierEventsSink pSink)
    {
        if (pSink != null)
        {
            IElementLifeTimeEventDispatcher dispatcher = getLifeTimeDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForUnknownClassifierEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeUnknownClassifierEventsSink(IUnknownClassifierEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IElementLifeTimeEventDispatcher dispatcher = getLifeTimeDispatcher();
            dispatcher.revokeUnknownClassifierSink(pSink);
        }
    }

    //**************************************************
    // Classifier event dispatcher
    //**************************************************
    public void registerForClassifierFeatureEvents(IClassifierFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForClassifierFeatureEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeClassifierFeatureSink(IClassifierFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeClassifierFeatureSink(pSink);
        }
    }

    public void registerForDynamicsEvents(ILifelineModifiedEventsSink pSink)
    {
        if (pSink != null)
        {
            IDynamicsEventDispatcher dispatcher = getDynamicsDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForLifelineModifiedEvents(pSink);
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeDynamicsSink(ILifelineModifiedEventsSink pSink)
    {
        if (pSink != null)
        {
            IDynamicsEventDispatcher dispatcher = getDynamicsDispatcher();
            dispatcher.revokeLifelineModifiedSink(pSink);
        }
    }

    public void registerForTransformEvents(IClassifierTransformEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForTransformEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeTransformSink(IClassifierTransformEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeTransformSink(pSink);
        }
    }

    public void registerForFeatureEvents(IFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForFeatureEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeFeatureSink(IFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeFeatureSink(pSink);
        }
    }

    public void registerForStructuralFeatureEvents(IStructuralFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForStructuralFeatureEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeStructuralFeatureSink(IStructuralFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeStructuralFeatureSink(pSink);
        }
    }

    public void registerForBehavioralFeatureEvents(IBehavioralFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForBehavioralFeatureEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeBehavioralFeatureSink(IBehavioralFeatureEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeBehavioralFeatureSink(pSink);
        }
    }

    public void registerForParameterEvents(IParameterEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForParameterEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeParameterSink(IParameterEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeParameterSink(pSink);
        }
    }

    public void registerForTypedElementEvents(ITypedElementEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForTypedElementEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeTypedElementSink(ITypedElementEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeTypedElementSink(pSink);
        }
    }

    public void registerForAttributeEvents(IAttributeEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForAttributeEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeAttributeSink(IAttributeEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeAttributeSink(pSink);
        }
    }

    public void registerForOperationEvents(IOperationEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForOperationEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeOperationSink(IOperationEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeOperationSink(pSink);
        }
    }

    public void registerForAffectedElementEvents(IAffectedElementEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForAffectedElementEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeAffectedElementEvents(IAffectedElementEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeAffectedElementEvents(pSink);
        }
    }

    public void registerForAssociationEndEvents(IAssociationEndEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForAssociationEndEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeAssociationEndEvents(IAssociationEndEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeAssociationEndSink(pSink);
        }
    }

    public void registerForAssociationEndTransformEvents(IAssociationEndTransformEventsSink pSink)
    {
        if (pSink != null)
        {
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            if (dispatcher != null)
            {
                // TODO: We do not have a JCWrapper from the ProjectEventsSink.
                dispatcher.registerForAssociationEndTransformEvents(pSink);
                //m_CookieMap.put(pSink, new Integer(cookie));
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeAssociationEndTransformEvents(IAssociationEndTransformEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            IClassifierEventDispatcher dispatcher = getClassifierDispatcher();
            dispatcher.revokeAssociationEndTransformSink(pSink);
        }
    }
    
    //**************************************************
    // RoundTrip dispatcher
    //**************************************************
    public void registerForRoundTripOperationEvents(IRoundTripOperationEventsSink pSink, String language)
    {
    }

    public void revokeRoundTripOperationEvents(IRoundTripOperationEventsSink pSink)
    {
    }

    public void registerForRoundTripAttributeEvents(IRoundTripAttributeEventsSink pSink, String language)
    {
    }

    public void revokeRoundTripAttributeEvents(IRoundTripAttributeEventsSink pSink)
    {
    }

    public void registerForRoundTripClassEvents(IRoundTripClassEventsSink pSink, String language)
    {
    }

    public void revokeRoundTripClassEvents(IRoundTripClassEventsSink pSink)
    {
    }

    public void registerForRoundTripPackageEvents(IRoundTripPackageEventsSink pSink, String language)
    {
    }

    public void revokeRoundTripPackageEvents(IRoundTripPackageEventsSink pSink)
    {
    }

    public void registerForRoundTripRelationEvents(IRoundTripRelationEventsSink pSink, String language)
    {
    }

    public void revokeRoundTripRelationEvents(IRoundTripRelationEventsSink pSink)
    {
    }

    public void registerForRoundTripRequestProcessorInitEvents(IRequestProcessorInitEventsSink pSink)
    {
    }

    public void revokeRoundTripRequestProcessorInitEvents(IRequestProcessorInitEventsSink pSink)
    {
    }

    //**************************************************
    // SCM Dispatcher
    //**************************************************
    public void registerForSCMEvents(ISCMEventsSink pSink)
    {
//      if(pSink != null)
//      {
//         getSCMDispatcher();
//      }
        if (pSink != null)
        {
            ISCMEventDispatcher dispatcher = getSCMDispatcher();
            if (dispatcher != null)
            {
                dispatcher.registerForSCMEvents(pSink);
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeSCMSink(ISCMEventsSink pSink)
    {
        if (pSink != null)
        {
            //Integer cookie = (Integer)m_CookieMap.get(pSink);
            //int value = cookie.intValue();
            ISCMEventDispatcher dispatcher = getSCMDispatcher();
            if (dispatcher != null)
            {
                dispatcher.revokeSCMSink(pSink);
            }
        }
    }

    //**************************************************
    // Event Framework Dispatcher
    //**************************************************
    public void registerForEventFrameworkEvents(IEventFrameworkEventsSink pSink)
    {
    }

    public void revokeEventFrameworkSink(IEventFrameworkEventsSink pSink)
    {
    }
    //**************************************************
    // Dynamics Event Dispatcher
    //**************************************************
    //public void registerForDynamicsEvents(IDynamicsEventsSink pSink){}
    //public void revokeDynamicsSink(IDynamicsEventsSink pSink){}

    //**************************************************
    // Design Pattern dispatcher
    //**************************************************
    //public void registerForDesignPatternEvents(IDesignPatternEventsSink pSink){}
    //public void revokeDesignPatternSink(IDesignPatternEventsSink pSink){}
    //**************************************************
    // Dispatcher Retrieval Methods
    //**************************************************
    /**
     *
     */
    public final IWorkspaceEventDispatcher getWorkspaceDispatcher()
    {
        //return (IWorkspaceEventDispatcher)getDispatcher(EventDispatchNameKeeper.workspaceName());
        IWorkspaceEventDispatcher retVal = m_WorkspaceEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.workspaceName());
            if (dispatcher instanceof IWorkspaceEventDispatcher)
            {
                retVal = (IWorkspaceEventDispatcher) dispatcher;
            }
            m_WorkspaceEventDispatcher = retVal;
        }

        return retVal;
    }

    public final IWorkspaceEventDispatcher getWorkspaceDispatcherDP()
    {
        IWorkspaceEventDispatcher retVal = m_WorkspaceEventDispatcherDP;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.workspaceNameDP());
            if (dispatcher instanceof IWorkspaceEventDispatcher)
            {
                retVal = (IWorkspaceEventDispatcher) dispatcher;
            }
            m_WorkspaceEventDispatcherDP = retVal;
        }

        return retVal;
    }

    /**
     * @return
     */
    public IElementLifeTimeEventDispatcher getLifeTimeDispatcher()
    {
        //return (IClassifierEventDispatcher)getDispatcher(EventDispatchNameKeeper.classifier());
        IElementLifeTimeEventDispatcher retVal = m_LifeTimeEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.lifeTime());
            if (dispatcher instanceof IElementLifeTimeEventDispatcher)
            {
                retVal = (IElementLifeTimeEventDispatcher) dispatcher;
            }
            m_LifeTimeEventDispatcher = retVal;
        }

        return retVal;
    }

    /**
     *
     */
    public IActivityEventDispatcher getActivitiesDispatcher()
    {
        IActivityEventDispatcher retVal = m_ActivityEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.activities());
            if (dispatcher instanceof IActivityEventDispatcher)
            {
                retVal = (IActivityEventDispatcher) dispatcher;
            }
            m_ActivityEventDispatcher = retVal;
        }

        return retVal;
    }

    protected ISCMEventDispatcher getSCMDispatcher()
    {
        ISCMEventDispatcher retVal = null;

        IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.SCM());
        if (dispatcher instanceof ISCMEventDispatcher)
        {
            retVal = (ISCMEventDispatcher) dispatcher;
        }

        return retVal;
    }

    /**
     *
     */
    protected IClassifierEventDispatcher getClassifierDispatcher()
    {
        //return (IClassifierEventDispatcher)getDispatcher(EventDispatchNameKeeper.classifier());
        IClassifierEventDispatcher retVal = m_ClassifierEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.classifier());
            if (dispatcher instanceof IClassifierEventDispatcher)
            {
                retVal = (IClassifierEventDispatcher) dispatcher;
            }
            m_ClassifierEventDispatcher = retVal;
        }

        return retVal;
    }

    protected IDynamicsEventDispatcher getDynamicsDispatcher()
    {
        IDynamicsEventDispatcher retVal = m_DynamicsEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.dynamics());
            if (dispatcher instanceof IDynamicsEventDispatcher)
            {
                retVal = (IDynamicsEventDispatcher) dispatcher;
            }
            m_DynamicsEventDispatcher = retVal;
        }
        return retVal;
    }

    /**
     * @return
     */
    public final IPreferenceManagerEventDispatcher getPreferenceManagerDispatcher()
    {
        IPreferenceManagerEventDispatcher retVal = null;
        if (m_PreferenceEventDispatcher == null)
        {
            m_PreferenceEventDispatcher = (IPreferenceManagerEventDispatcher) getJavaDispatcher(EventDispatchNameKeeper.preferenceManager());
        }
        // Since we are not using the Java code 100% (Basically we are not using core at
        // all.  I will have to create the Dispatcher and add it to core if it has not
        // already been added.
        if (m_PreferenceEventDispatcher == null)
        {
            m_PreferenceEventDispatcher = new PreferenceManagerEventDispatcher();
            addJavaDispatcher(EventDispatchNameKeeper.preferenceManager(), m_PreferenceEventDispatcher);
        }

        return m_PreferenceEventDispatcher;
    }

    /**
     * @return
     */
    public ICoreProductEventDispatcher getCoreProductDispatcher()
    {
        //return (ICoreProductEventDispatcher)getDispatcher(EventDispatchNameKeeper.coreProduct());
        ICoreProductEventDispatcher retVal = null;

        IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.coreProduct());
        if (dispatcher instanceof ICoreProductEventDispatcher)
        {
            retVal = (ICoreProductEventDispatcher) dispatcher;
        }

        return retVal;
    }

    /**
     * @return
     */
    public IStructureEventDispatcher getStructureDispatcher()
    {
        //return (IStructureEventDispatcher)getDispatcher(EventDispatchNameKeeper.structure());
        IStructureEventDispatcher retVal = m_StructureEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.structure());
            if (dispatcher instanceof IStructureEventDispatcher)
            {
                retVal = (IStructureEventDispatcher) dispatcher;
            }
        }

        return retVal;
    }

    /**
     * Gets the project tree dispatcher.
     */
    public IProjectTreeEventDispatcher getProjectTreeDispatcher()
    {
        //return (IProjectTreeEventDispatcher)getJavaDispatcher(EventDispatchNameKeeper.projectTreeName());
        if (m_ProjectTreeEventDispatcher == null)
        {
            m_ProjectTreeEventDispatcher = (IProjectTreeEventDispatcher) getJavaDispatcher(EventDispatchNameKeeper.projectTreeName());
        }
        // Since we are not using the Java code 100% (Basically we are not using core at
        // all.  I will have to create the Dispatcher and add it to core if it has not
        // already been added.
        if (m_ProjectTreeEventDispatcher == null)
        {
            m_ProjectTreeEventDispatcher = new ProjectTreeEventDispatcherImpl();
            addJavaDispatcher(EventDispatchNameKeeper.projectTreeName(), m_ProjectTreeEventDispatcher);
        }

        return m_ProjectTreeEventDispatcher;
    }

    /**
     * Gets the project tree dispatcher.
     */
    public IProjectTreeFilterDialogEventDispatcher getProjectTreeFilterDialogDispatcher()
    {
        //return (IProjectTreeEventDispatcher)getJavaDispatcher(EventDispatchNameKeeper.projectTreeName());
        if (m_ProjectTreeFilterEventDispatcher == null)
        {
            m_ProjectTreeFilterEventDispatcher = (IProjectTreeFilterDialogEventDispatcher) getJavaDispatcher(EventDispatchNameKeeper.projectTreeFilterDialogName());
        }
        // Since we are not using the Java code 100% (Basically we are not using core at
        // all.  I will have to create the Dispatcher and add it to core if it has not
        // already been added.
        if (m_ProjectTreeFilterEventDispatcher == null)
        {
            m_ProjectTreeFilterEventDispatcher = new ProjectTreeFilterDialogEventDispatcher();
            addJavaDispatcher(EventDispatchNameKeeper.projectTreeFilterDialogName(), m_ProjectTreeFilterEventDispatcher);
        }

        return m_ProjectTreeFilterEventDispatcher;
    }

    /**
     * Gets the drawing area dispatcher.
     */
    // TODO: Must figure out a way to decouple this from the drawing area package.
    public IDrawingAreaEventDispatcher getDrawingAreaDispatcher()
    {
        //return (IDrawingAreaEventDispatcher)getJavaDispatcher(EventDispatchNameKeeper.drawingAreaName());
        if (m_DrawingAreaDispatcher == null)
        {
            m_DrawingAreaDispatcher = (IDrawingAreaEventDispatcher) getJavaDispatcher(EventDispatchNameKeeper.drawingAreaName());
        }
        // Since we are not using the Java code 100% (Basically we are not using core at
        // all.  I will have to create the Dispatcher and add it to core if it has not
        // already been added.
        if (m_DrawingAreaDispatcher == null)
        {
            m_DrawingAreaDispatcher = new DrawingAreaEventDispatcherImpl();
            addJavaDispatcher(EventDispatchNameKeeper.drawingAreaName(), m_DrawingAreaDispatcher);
        }

        return m_DrawingAreaDispatcher;
    }

    /**
     * @return
     */
    public IElementChangeEventDispatcher getElementChangeDispatcher()
    {
        //return (IElementChangeEventDispatcher)getDispatcher(EventDispatchNameKeeper.modifiedName());
        IElementChangeEventDispatcher retVal = null;

        IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.modifiedName());
        if (dispatcher instanceof IElementChangeEventDispatcher)
        {
            retVal = (IElementChangeEventDispatcher) dispatcher;
        }

        return retVal;
    }

    protected IRelationValidatorEventDispatcher getRelationDispatcher()
    {
        //return (IRelationValidatorEventDispatcher)getDispatcher(EventDispatchNameKeeper.relation());
        IRelationValidatorEventDispatcher retVal = m_RelationValidatorEventDispatcher;

        if (retVal == null)
        {
            IEventDispatcher dispatcher = getDispatcher(EventDispatchNameKeeper.relation());
            if (dispatcher instanceof IRelationValidatorEventDispatcher)
            {
                retVal = (IRelationValidatorEventDispatcher) dispatcher;
            }

            m_RelationValidatorEventDispatcher = retVal;
        }

        return retVal;
    }

    /**
     * This method is used to access the Java versions of Dispatch controllers.  Basically
     * all UI dispatchers will be Java versions of the Event Dispatchers.  MetaModel dispatcher
     * will be COM versions of the Dispatcher Controllers.
     *
     * @param name
     * @param newVal
     */
    protected void addJavaDispatcher(String name, org.netbeans.modules.uml.core.eventframework.IEventDispatcher newVal)
    {
        try
        {
            ICoreProduct product = ProductRetriever.retrieveProduct();
            if (product != null)
            {
                IEventDispatchController controller = product.getEventDispatchController();
                controller.addDispatcher(name, newVal);
            }
        }
        catch (NullPointerException e)
        {
            // HAVE TODO: Determine what to do about excpetions.
        }
    }

    /**
     * @param string
     * @return
     */
    protected org.netbeans.modules.uml.core.eventframework.IEventDispatcher getJavaDispatcher(String name)
    {
        IEventDispatcher retVal = null;

        if (name.length() > 0)
        {
            try
            {
                ICoreProduct product = ProductRetriever.retrieveProduct();
                if (product != null)
                {
                    IEventDispatchController controller = product.getEventDispatchController();
                    retVal = controller.retrieveDispatcher(name);
                }
            }
            catch (NullPointerException e)
            {
                // HAVE TODO: Determine what to do about excpetions.
            }
        }
        else
        {
            // HAVE TODO: notify that the name is invalid.
        }

        return retVal;
    }

    /**
     * @param string
     */
    protected final IEventDispatcher getDispatcher(String name)
    {
        IEventDispatcher retVal = null;

        if (name.length() > 0)
        {
            try
            {
                if (m_EventController == null)
                {
                    ICoreProduct product = ProductRetriever.retrieveProduct();
                    m_EventController = product.getEventDispatchController();
                }

                retVal = m_EventController.retrieveDispatcher(name);

//				TODO: Add Round Trip Dispatch Event Context
//				if(retVal == null)
//				{
//					IRoundTripController rtController = product.getRoundTripController();
//					IEventDispatchController rtEventController = rtController.getEventDispatchController();
//
//					retVal = rtEventController.retrieveDispatcher(name);
//				}
            }
            catch (NullPointerException e)
            {
                // HAVE TODO: Determine what to do about excpetions.
            }
        }
        else
        {
            // HAVE TODO: notify that the name is invalid.
        }

        return retVal;
    }

    /**
     * Gets the edit control dispatcher
     */
    public IEditControlEventDispatcher getEditControlDispatcher()
    {
        IEditControlEventDispatcher retDisp = null;
        retDisp = (IEditControlEventDispatcher) getJavaDispatcher(EventDispatchNameKeeper.editCtrlName());
        if (retDisp == null)
        {
            retDisp = new EditControlEventDispatcher();

            addJavaDispatcher(EventDispatchNameKeeper.editCtrlName(), retDisp);
        }

        return retDisp;
    }

    public void registerEditCtrlEvents(IEditControlEventSink pSink)
    {
        if (pSink != null)
        {
            IEditControlEventDispatcher tempDisp = getEditControlDispatcher();
            if (tempDisp != null)
            {
                tempDisp.registerEditCtrlEvents(pSink);
            }
        }
    }

    public void revokeEditCtrlSink(IEditControlEventSink pSink)
    {
        if (pSink != null)
        {
            IEditControlEventDispatcher tempDisp = getEditControlDispatcher();
            if (tempDisp != null)
            {
                tempDisp.revokeEditCtrlSink(pSink);
            }
        }
    }

    public void registerForWorkspaceEventsDP(IWorkspaceEventsSink sink)
    {
        if (sink != null)
        {
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcherDP();
            if (dispatcher != null)
            {
                dispatcher.registerForWorkspaceEvents(sink);
            }
        }
    }

    public void revokeWorkspaceSinkDP(IWorkspaceEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcherDP();
            dispatcher.revokeWorkspaceSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }

    public void registerForWSProjectEventsDP(IWSProjectEventsSink sink)
    {
        if (sink != null)
        {
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcherDP();
            if (dispatcher != null)
            {
                dispatcher.registerForWSProjectEvents(sink);
            }
        }
        else
        {
            // TODO: I need to notify the caller that the register failed.
        }
    }

    public void revokeWSProjectSinkDP(IWSProjectEventsSink sink) throws InvalidArguments
    {
        if (sink != null)
        {
            IWorkspaceEventDispatcher dispatcher = getWorkspaceDispatcherDP();
            dispatcher.revokeWSProjectSink(sink);
        }
        else
        {
            throw new InvalidArguments();
        }
    }
}
