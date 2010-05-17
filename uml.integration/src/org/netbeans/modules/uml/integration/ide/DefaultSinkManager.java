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
 * File         : DefaultSinkManager.java
 * Version      : 1.0
 * Description  : Manages the sinks (listeners) attached to the running
 *                Describe instance.
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumLiteralEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IUMLMessagingEventDispatcher;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;

/**
 *  Manages the sinks (listeners) attached to the running Describe instance.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-22  Darshan     Added support for source navigation.
 *
 * @author  Trey Spiva
 * @version 1.0
 */
public class DefaultSinkManager extends SourceNavigable {
    public static final String RT_LANGUAGE = "Java";
    ElementLifeTimeEventDispatcher d;
    private IRoundTripClassEventsSink mRTClassSink;
    private RoundTripPackageEventsSink mRTPackageSink;
    private IRoundTripRelationEventsSink mRTRelationSink;
    DispatchHelper helper = null;
    
    public DefaultSinkManager() {
         helper = new DispatchHelper();
    }

    public DefaultSinkManager(IEventDispatchController controller) {
        setEventDispatchController(controller);
    }

    public void setEventDispatchController(IEventDispatchController controller) {
        mController = controller;
    }

//    public void registerForAllWorkspaceEvents() {
//        registerForWorkspaceEvents();
//        registerForWSProjectEvents();
//    }

    public void initializeAll() {
        Log.out("initializeAll() - Registering for all events");
        registerForRoundTripEvents();
//        registerForAllWorkspaceEvents();
//        registerForMessagesEvents();
//        registerForDrawingAreaEvents();
//        registerForAllTreeEvents();
//        registerForSCMEvents();
//        registerForPreferenceEvents();
//        // Was commented out during the C++ days
//        registerForElementLifeTimeEvents();
    }

//    private void registerForPreferenceEvents() 
//    {
//        if(helper == null) 
//        	helper = new DispatchHelper();
//       
//        helper.registerForPreferenceManagerEvents(
//                new DefaultPreferenceManagerEventSink());
//    }

    /**
     * Connects a sink to the Workspace dispatcher.
//     */
//    public void registerForWorkspaceEvents() {
//        if (mController != null) {
//            establishWorkspaceDispatcher();
//            if (mWDispatcher != null) {
//                IWorkspaceEventsSink sink = getWorkspaceEventsSink();
//                if (sink == null)
//                    setWorkspaceEventsSink(sink = new WorkspaceEventsSink());
//                mWDispatcher.registerForWorkspaceEvents(sink);
//                Log.out(
//                    "In registerForWorkspaceEvents() : Registered for Workspace Events");
//                GDProSupport
//                    .getGDProSupport()
//                    .getSinkManager()
//                    .registerForRoundTripEvents();
//                Log.out(
//                    "In registerForWorkspaceEvents() : Registered for RoundTrip Events");
//            }
//        }
//    }

    /**
     * Connects a sink to the Workspace Dispatcher
     */
//    public void registerForWSProjectEvents() {
//        if (mController != null) {
//            establishWorkspaceDispatcher();
//            if (mWDispatcher != null) {
//                IWSProjectEventsSink sink = getProjectEventsSink();
//                if (sink == null)
//                    setProjectEventsSink(sink = new WSProjectEventsSink());
//                mWDispatcher.registerForWSProjectEvents(sink);
//                Log.out(
//                    "In registerForWorkspaceEvents() : Registered for WSProject Events");
//            }
//        }
//    }

    /**
     * Connects sinks to all the ProjectTree event sinks
     */
//    public void registerForAllTreeEvents() {
//        registerForTreeMenuEvents();
//        registerForProjectTreeEvents();
//    }

    /**
     * Connects a sink to the context menu events coming off the Project tree
     */
//    public void registerForTreeMenuEvents() {
//        if (mController != null) {
//            establishTreeDispatcher();
//
//            IProjectTreeContextMenuEventsSink sink =
//                getProjectTreeContextMenuEventsSink();
//            if (sink == null)
//                setProjectTreeContextMenuEventsSink(
//                    sink = new ProjectTreeContextMenuEventsSink());
//
//            mTreeDispatcher.registerProjectTreeContextMenuEvents(sink);
//        }
//    }

    /**
     * Connects a sink to the tree events, such as selection and double clicks
     */
//    public void registerForProjectTreeEvents() {
//        if (mController != null) {
//            establishTreeDispatcher();
//            DefaultTreeEventsSink tree = getProjectTreeEventsSink();
//            if (tree == null)
//                setProjectTreeEventsSink(tree = new DefaultTreeEventsSink());
//            tree.setSourceNavigator(navigator);
//            mTreeDispatcher.registerProjectTreeEvents(tree);
//        }
//    }

    public void registerForProcessorEvents() {
        IRoundTripController rt =
            UMLSupport
                .getUMLSupport()
                .getProduct()
                .getRoundTripController();

        if (rt != null) {
            // Need to get the dispatch controller off the RoundTrip controller
            // to listen for the OnInitialized()

            IRoundTripEventDispatcher dispatcher = rt.getRoundTripDispatcher();

            if (dispatcher != null) {
                Log.out("Regsitering for init events on request processor");
                dispatcher.registerForRequestProcessorInitEvents(
                        new RequestProcessorInitEventsSink());
            }
            else {
                Log.out("RT Dispatcher null");
            }
        }
        else {
            Log.out("RT null");
        }
    }

    public void registerForRoundTripEvents() {
        Log.out("registering for round trip events");
        IRoundTripController rt =
            UMLSupport
                .getUMLSupport()
                .getProduct()
                .getRoundTripController();

        // Need to get the dispatch controller off the RoundTrip controller
        // to listen for the OnInitialized()

        IRoundTripEventDispatcher dispatcher = rt.getRoundTripDispatcher();

        if (mRTAttrSink != null)
            dispatcher.revokeRoundTripAttributeSink(mRTAttrSink);
        dispatcher.registerForRoundTripAttributeEvents(
                    mRTAttrSink = new RoundTripAttributeEventsSink(),
                    RT_LANGUAGE);
        
        if (mRTOperSink != null)
            dispatcher.revokeRoundTripOperationSink(mRTOperSink);
        dispatcher.registerForRoundTripOperationEvents(
                mRTOperSink = new RoundTripOperationEventsSink(),
                RT_LANGUAGE);
        if (mRTClassSink != null) {
            dispatcher.revokeRoundTripClassSink(mRTClassSink);
            dispatcher.revokeRoundTripEnumSink((IRoundTripEnumEventsSink)mRTClassSink);
        }
        dispatcher.registerForRoundTripClassEvents(
                mRTClassSink = new RoundTripClassEventsSink(),
                RT_LANGUAGE);
        dispatcher.registerForRoundTripEnumEvents(
                (IRoundTripEnumEventsSink)mRTClassSink,
                RT_LANGUAGE);
        
        if (mRTPackageSink != null)
            dispatcher.revokeRoundTripPackageSink(mRTPackageSink);
        dispatcher.registerForRoundTripPackageEvents(
                mRTPackageSink = new RoundTripPackageEventsSink(),
                RT_LANGUAGE);
        if (mRTRelationSink != null)
            dispatcher.revokeRoundTripRelationSink(mRTRelationSink);
        dispatcher.registerForRoundTripRelationEvents(
                mRTRelationSink = new RoundTripRelationEventsSink(),
                RT_LANGUAGE);
        
        if (mRTEnumLiteralSink != null)
            dispatcher.revokeRoundTripEnumLiteralSink(mRTEnumLiteralSink);
        dispatcher.registerForRoundTripEnumLiteralEvents(
                mRTEnumLiteralSink = new RoundTripEnumLiteralEventsSink(),
                RT_LANGUAGE);

        if (eventFrameworkSink != null)
            dispatcher.revokeEventFrameworkSink(eventFrameworkSink);
        dispatcher.registerForEventFrameworkEvents(
                eventFrameworkSink = new EventFrameworkSink());
        eventFrameworkSink.setSourceNavigator(navigator);

        Log.out("registered for round trip events ..");
    }

    /**
     * Pulls the WorkspaceEventDispatcher off the internal EventDispatchController
     */
//    protected void establishWorkspaceDispatcher() {
//        if (mController != null && (mWDispatcher == null)) {
//            IEventDispatcher disp =
//                mController.retrieveDispatcher("WorkspaceDispatcher");
//
//            mWDispatcher = (IWorkspaceEventDispatcher)  disp;
//        }
//    }

    /**
     * Pulls the ProjectTreeEventDispatcher off the internal EventDispatchController
     */
//    protected void establishTreeDispatcher() {
//    	 if (mController != null && mTreeDispatcher == null) {
//            if(helper == null) 
//                helper = new DispatchHelper();
//        	mTreeDispatcher = helper.getProjectTreeDispatcher();
//        }
//    }

    /**
     * Connects a sink to the Drawing Area
     */
//    public void registerForDrawingAreaEvents() {
//        Log.out("Registering for drawing area events");
//        if (mController != null) {
//            establishDrawingAreaDispatcher();
//            DefaultDrawingAreaSelectionEventsSink selSink =
//                getDrawingSelectionEventsSink();
//            if (selSink == null)
//                setDrawingSelectionEventsSink(
//                    selSink = new DefaultDrawingAreaSelectionEventsSink());
//            selSink.setSourceNavigator(navigator);
//            mDrawingAreaDispatcher.registerDrawingAreaSelectionEvents(selSink);
//
//            DefaultCompartmentEventsSink compSink =
//                getDrawingCompartmentEventsSink();
//            if (compSink == null)
//                setDrawingCompartmentEventsSink(
//                    compSink = new DefaultCompartmentEventsSink());
//            compSink.setSourceNavigator(navigator);
//
//            mDrawingAreaDispatcher.registerDrawingAreaCompartmentEvents(
//                compSink);
//            DefaultDrawingAreaEventsSink eventSink = getDrawingAreaEventsSink();
//
//            if (eventSink == null)
//                setDrawingAreaEventsSink(
//                    eventSink = new DefaultDrawingAreaEventsSink());
//
//            mDrawingAreaDispatcher.registerDrawingAreaEvents(eventSink);
//        }
//        else
//            Log.out("mController is null!");
//    }

    /**
     * Connects a sink to the Describe messages
     */
//    public void registerForMessagesEvents() {
//        Log.out("Registering for messages events");
//        if (mController != null) {
//            establishMessagesDispatcher();
//            DefaultMessagesEventsSink messSink = getMessagesEventsSink();
//            if (messSink == null)
//                setMessagesEventsSink(
//                    messSink = new DefaultMessagesEventsSink());
//
//            Log.out("DefaultSinkManager.registerForMessagesEvents: " +
//                "Registering preference watcher");
//            Preferences.addPreferenceWatcher(
//                Preferences.LOG_DESCRIBE_MESSAGES,
//                new Preferences.PreferenceWatcher() {
//                    private IMessengerEventsSink mMessagingSink;
//
//                    public void preferenceChanged(String preference,
//                                                  String oldValue,
//                                                  String newValue) {
//                        if (Preferences.PSK_NO.equals(newValue)) {
//                            if (mMessagingSink != null) {
//                                Log.out(
//                                    "DefaultSinkManager." +
//                                    "registerForMessagesEvents: " +
//                                    "Revoking messaging sink");
//
//                                mMessagesDispatcher.revokeMessengerSink(
//                                    mMessagingSink);
//                                mMessagingSink = null;
//                            }
//                        }
//                        else {
//                            if (mMessagingSink == null) {
//                                Log.out(
//                                    "DefaultSinkManager." +
//                                    "registerForMessagesEvents: " +
//                                    "Registering messaging sink");
//                                mMessagesDispatcher
//                                        .registerMessengerEvents(
//                                            mMessagingSink = 
//                                                getMessagesEventsSink());
//                            }
//                        }
//                    }
//                },
//                true
//            );
//        }
//        else
//            Log.out("mController is null!");
//    }

    /**
     * Connects a sink to the SCM events
     */
//    public void registerForSCMEvents() {
//        Log.out("Registering for SCM events");
//        if (mController != null) {
//            establishSCMDispatcher();
//            DefaultSCMEventsSink scmSink = getSCMEventsSink();
//            if (scmSink == null)
//                setSCMEventsSink(scmSink = new DefaultSCMEventsSink());
//            // TODO
////            mSCMDispatcher.registerForSCMEvents(scmSink);
//        }
//        else
//            Log.out("mController is null!");
//    }


    /**
     * Pulls the ElementLifeTimeEventsDispatcher off the internal EventDispatchController
     */
//    protected void establishElementLifeTimeEventsDispatcher() {
//        if (mController != null && mLifeTimeDispatcher == null) {
//            if(helper == null) 
//                helper = new DispatchHelper();
//            mLifeTimeDispatcher = helper.getLifeTimeDispatcher();
//            Log.out("establishElementLifeTimeDispatcher() : " + mLifeTimeDispatcher.getClass().getName());
//        }
//    }

//    public void registerForElementLifeTimeEvents() {
//        Log.out("Registering for ElementLifeTime events");
//        if (mController != null) {
//            establishElementLifeTimeEventsDispatcher();
//            DefaultElementLifeTimeEventsSink lifeTimeSink = getElementLifeTimeEventsSink();
//            if (lifeTimeSink == null)
//                setElementLifeTimeEventsSink(lifeTimeSink = new DefaultElementLifeTimeEventsSink());
//            Log.out("registerForElementLifeTimeEvents() - " + lifeTimeSink);
//            mLifeTimeDispatcher.registerForLifeTimeEvents(lifeTimeSink);
//        }
//        else
//            Log.out("mController is null!");
//    }


    /**
     * Pulls the DrawingAreaEventDispatcher off the internal
     * EventDispatchController
     */
//    protected void establishDrawingAreaDispatcher() {
//        if (mController != null && mDrawingAreaDispatcher == null) {
//            if(helper == null) 
//                helper = new DispatchHelper();
//            mDrawingAreaDispatcher = helper.getDrawingAreaDispatcher();
//        }
//    }

//    protected void establishMessagesDispatcher() {
//        if (mController != null && (mMessagesDispatcher == null)) {
//            IEventDispatcher disp =
//                mController.retrieveDispatcher("UMLMessagingDispatcher");
//            mMessagesDispatcher =
//                (IUMLMessagingEventDispatcher)  disp;
//        }
//    }

//    protected void establishSCMDispatcher() {
//        if (mController != null /* && TODO (mSCMDispatcher == null) */) {
//            //       IEventDispatcher disp = mController.retrieveDispatcher("SCM");
//            IEventDispatcher disp =
//                GDProSupport.getGDProSupport().getProduct().getEventDispatcher(
//                    "SCM");
//            // TODO
////            mSCMDispatcher = (ISCMEventDispatcher)  disp;
//        }
//    }

    public void purge() {
        mWDispatcher = null;
        mTreeDispatcher = null;
        // TODO
//        mSCMDispatcher = null;
        mController = null;
        mDrawingAreaDispatcher = null;
    }

//    public IWorkspaceEventsSink getWorkspaceEventsSink() {
//        return workspaceEventsSink;
//    }

//    public void setWorkspaceEventsSink(IWorkspaceEventsSink workspaceEventsSink) {
//        this.workspaceEventsSink = workspaceEventsSink;
//    }

//    public IWSProjectEventsSink getProjectEventsSink() {
//        return projectEventsSink;
//    }

    public void setProjectEventsSink(IWSProjectEventsSink projectEventsSink) {
        this.projectEventsSink = projectEventsSink;
    }

//    public DefaultTreeEventsSink getProjectTreeEventsSink() {
//        return projectTreeEventsSink;
//    }

//    public void setProjectTreeEventsSink(DefaultTreeEventsSink projectTreeEventsSink) {
//        this.projectTreeEventsSink = projectTreeEventsSink;
//    }

//    public IProjectTreeContextMenuEventsSink getProjectTreeContextMenuEventsSink() {
//        return projectTreeContextMenuEventsSink;
//    }

//    public void setProjectTreeContextMenuEventsSink(IProjectTreeContextMenuEventsSink sink) {
//        this.projectTreeContextMenuEventsSink = sink;
//    }
//
//    public DefaultDrawingAreaSelectionEventsSink getDrawingSelectionEventsSink() {
//        return drawingSelectionEventsSink;
//    }
//
//    public DefaultElementLifeTimeEventsSink getElementLifeTimeEventsSink() {
//        return lifeTimeEventsSink;
//    }
//
//
//    public void setDrawingSelectionEventsSink(DefaultDrawingAreaSelectionEventsSink sink) {
//        this.drawingSelectionEventsSink = sink;
//    }
//
//    public DefaultMessagesEventsSink getMessagesEventsSink() {
//        return messagesEventsSink;
//    }
//
//    public DefaultSCMEventsSink getSCMEventsSink() {
//        return scmEventsSink;
//    }
//
//    public void setMessagesEventsSink(DefaultMessagesEventsSink sink) {
//        this.messagesEventsSink = sink;
//    }
//
//    public void setSCMEventsSink(DefaultSCMEventsSink sink) {
//        this.scmEventsSink = sink;
//    }
//
//    public void setElementLifeTimeEventsSink(DefaultElementLifeTimeEventsSink sink) {
//        this.lifeTimeEventsSink = sink;
//    }
//
//
//    public DefaultCompartmentEventsSink getDrawingCompartmentEventsSink() {
//        return drawingCompartmentEventsSink;
//    }
//
//    public void setDrawingCompartmentEventsSink(DefaultCompartmentEventsSink sink) {
//        this.drawingCompartmentEventsSink = sink;
//    }
//
//    public DefaultDrawingAreaEventsSink getDrawingAreaEventsSink() {
//        return drawingAreaEventsSink;
//    }
//
//    public void setDrawingAreaEventsSink(DefaultDrawingAreaEventsSink sink) {
//        this.drawingAreaEventsSink = sink;
//    }
//
    public EventFrameworkSink getEventFrameworkSink() {
        return eventFrameworkSink;
    }

    protected IWorkspaceEventDispatcher mWDispatcher;
    protected IProjectTreeEventDispatcher mTreeDispatcher;
    protected IEventDispatchController mController;
    protected IDrawingAreaEventDispatcher mDrawingAreaDispatcher;
    protected IUMLMessagingEventDispatcher mMessagesDispatcher;
//    protected ISCMEventDispatcher mSCMDispatcher;
    private IElementLifeTimeEventDispatcher mLifeTimeDispatcher;

    protected IWorkspaceEventsSink workspaceEventsSink;
    protected IWSProjectEventsSink projectEventsSink;
//    protected DefaultTreeEventsSink projectTreeEventsSink;
    protected IProjectTreeContextMenuEventsSink projectTreeContextMenuEventsSink;
//    protected DefaultDrawingAreaSelectionEventsSink drawingSelectionEventsSink;
//    protected DefaultMessagesEventsSink messagesEventsSink;
//    protected DefaultSCMEventsSink scmEventsSink;
//    protected DefaultElementLifeTimeEventsSink lifeTimeEventsSink;
//    protected DefaultCompartmentEventsSink drawingCompartmentEventsSink;
//    protected DefaultDrawingAreaEventsSink drawingAreaEventsSink;
    protected EventFrameworkSink eventFrameworkSink;
    
    protected IRoundTripAttributeEventsSink mRTAttrSink;
    protected RoundTripOperationEventsSink mRTOperSink;
    protected IRoundTripEnumLiteralEventsSink mRTEnumLiteralSink;
    
}
