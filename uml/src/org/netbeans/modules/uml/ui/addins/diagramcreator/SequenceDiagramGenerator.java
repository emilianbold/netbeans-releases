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

package org.netbeans.modules.uml.ui.addins.diagramcreator;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramEnums;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IActionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.SimpleAction;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.ThermProgress;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProgressCtrl;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

/**
 * @author sumitabhk
 *
 */
public class SequenceDiagramGenerator implements ISequenceDiagramGenerator
{
    /// map of the lifeline draw engines representing the Lifelines for this interaction
    private Hashtable < ILifeline, ILifelineDrawEngine > m_MapLifelineToEngine = new Hashtable < ILifeline, ILifelineDrawEngine > ();
    //typedef std::map< CComPtr< IElement >, CComPtr< IDrawEngine > > MapElementToEngine;
    
    // Original namespace for the interaction
    private INamespace m_Namespace = null;
    // Interaction the sequence diagram is being created from
    private IInteraction m_Interaction = null;
    //The diagram that will contain the sequence interaction
    private IDiagram m_Diagram = null;
    private int m_InitialMessageY = 0;
    
    /**
     *
     */
    public SequenceDiagramGenerator()
    {
        super();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.addins.diagramcreator.ISequenceDiagramGenerator#generate(org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction, org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
         */
    public boolean generate(IInteraction pInteraction, IDiagram pExistingDiagram)
    {
        boolean handled = false;
        m_Interaction = pInteraction;
        if (pInteraction instanceof INamespace)
        {
            m_Namespace = (INamespace)pInteraction;
        }
        
        // Make sure we have a valid diagram
        if (pExistingDiagram != null)
        {
            int diaKind = IDiagramKind.DK_DIAGRAM;
            diaKind = pExistingDiagram.getDiagramKind();
            if (diaKind == IDiagramKind.DK_SEQUENCE_DIAGRAM)
            {
                m_Diagram = pExistingDiagram;
                handled = true;
            }
        }
        else
        {
            // Create it
            createSequenceDiagram();
        }
        
        // We were seing problems when the message were being created during SQD CDFS.
        // This was especially evident when a create message was in the interaction.
        // Sequence diagram CDFS depends on draw information, therefore
        // we need to ensure that redraw is allowed for the diagram.
        if( m_Diagram != null )
        {
            m_Diagram.setAllowRedraw( true );
        }
        
        // Fix W6872:  Make sure the interaction is named
        if (m_Interaction != null)
        {
            String name = m_Interaction.getName();
            if (name == null || name.length() == 0)
            {
                String diaName = m_Diagram.getName();
                m_Interaction.setName(diaName);
            }
        }
        
        if (m_Diagram != null)
        {
            generateSequenceDiagramFromInteraction();
            
            // Fix W9969:  Make sure the interaction (or project, see below) is set as Dirty
            m_Interaction.setDirty(true);
            
            // If the project is version controlled the above call works
            // If not the Dirty flag for the element will be false, and
            // the project needs to be set to dirty.
            boolean isDirty = m_Interaction.isDirty();
            if (!isDirty)
            {
                IProject proj = m_Interaction.getProject();
                if (proj != null)
                {
                    proj.setDirty(true);
                }
            }
        }
        
        return handled;
    }
    
    /**
     * Creates the sequence diagram under the given interaction.
     */
    private void createSequenceDiagram()
    {
        if (m_Interaction != null)
        {
            IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
            if (diaMgr != null)
            {
                // Get the name of the interaction where this diagram is to be created
                String name = m_Interaction.getName();
                
                // Go through the new diagram dialog so it restricts the fact that interactions can
                // only have a single sqd diagram
                m_Diagram = diaMgr.newDiagramDialog(m_Namespace, IDiagramKind.DK_SEQUENCE_DIAGRAM, IDiagramKind.DK_SEQUENCE_DIAGRAM | IDiagramKind.DK_COLLABORATION_DIAGRAM, null);
            }
        }
    }
    
    private class SQDGenerateRunnable implements Runnable
    {
        private MessageHelper m_msgHelper = null;
        private ThermProgress m_progress = null;
        private int m_count = 0;
        private int m_index = 0;
        private int m_verticalOffset = 0;
        private boolean m_showInteractionBoundary = false;
        
        public SQDGenerateRunnable( MessageHelper msgHelper, ThermProgress progress,int count,int index, boolean showInteractionBoundary)
        {
            m_msgHelper = msgHelper;
            m_progress = progress;
            m_count = count;
            m_index = index;
            m_showInteractionBoundary = showInteractionBoundary;
        }
        
        public void run()
        {
            boolean origBlock = EventBlocker.startBlocking();
            
            try
            {
                m_progress.updateProgressControl(loadString("IDS_SQD_CREATE_MESSAGES"), ++m_index, m_count);
                m_verticalOffset = createMessages( m_msgHelper );
                pumpMessages();
                
                m_progress.updateProgressControl(loadString("IDS_SQD_UPDATE_LIFELINES"), ++m_index, m_count);
                updateLifelineLengths(m_verticalOffset - 80 );
                pumpMessages();
                
                if (!inDescribeIDE())
                {
                    m_progress.updateProgressControl(loadString("IDS_PERFORMING_LAYOUT"), ++m_index, m_count);
                    layoutSequenceDiagram();
                }
                
                m_progress.updateProgressControl(loadString("IDS_SQD_DESTROY_ACTIONS"), ++m_index, m_count);
                createDestroyActions();
                
                // Create the comments associated with the lifelines
                m_progress.updateProgressControl(loadString("IDS_SQD_COMMENTS"), ++m_index, m_count);
                createComments();
                
                // Fix W6744:  Show the interaction boundary, preference based
                if (m_showInteractionBoundary)
                {
                    m_progress.updateProgressControl(loadString("IDS_SQD_SHOW_BOUNDARY"), ++m_index, m_count);
                    IDiagramEngine diaEngine = TypeConversions.getDiagramEngine(m_Diagram);
                    if (diaEngine != null && diaEngine instanceof IADSequenceDiagEngine)
                    {
                        ((IADSequenceDiagEngine)diaEngine).showInteractionBoundary(true);
                    }
                }
                
                // Fix W7315:  Create the combined fragments after showing the interaction boundary
                //             Perform a layout of the diagram first
                m_progress.updateProgressControl(loadString("IDS_PERFORMING_LAYOUT"), ++m_index, m_count);
                layoutSequenceDiagram();
                
                // The pumpmessages is necessary to get the above layout to properly align the create messages.
                pumpMessages();
                
                m_progress.updateProgressControl(loadString("IDS_SQD_COMBINED_FRAGMENTS"), ++m_index, m_count);
                createCombinedFragments();
                
                // UPDATE:  These should have been discovered when they were created?
                // Discover the UML::Comment relationships
                m_progress.updateProgressControl(loadString("IDS_SQD_RELATIONSHIPS"), ++m_index, m_count);
                
                // Get the relationship discovery object so we can create presentation elements
                ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(m_Diagram);
                if (relDisco != null)
                {
                    ETList < IElement > elemsToDoRelDiscoOn = m_Diagram.getAllItems3();
                    relDisco.discoverCommonRelations(false, elemsToDoRelDiscoOn);
                }
                
                pumpMessages();
            }
            finally
            {
                EventBlocker.stopBlocking(origBlock);
            }
        }
    }
    /**
     * Generates all the elements on the sequence diagram.
     */
    private void generateSequenceDiagramFromInteraction()
    {
        // Make sure no message dialog boxes come up during processing
        //DisableMessaging disable;
        boolean origBlock = EventBlocker.startBlocking();
        try
        {
            ThermProgress progress = new ThermProgress();
            
            boolean bShowInteractionBoundary = (m_Diagram != null && showInteractionBoundary());
            int count = 8 + (inDescribeIDE() ? 0 : 1) + (bShowInteractionBoundary ? 1 : 0);
            int index = 0;
            progress.updateProgressControl(loadString("IDS_SQD_VALIDATE_INTERACTION"), index, count);
            MessageHelper msgHelper = new MessageHelper( m_Interaction );
            msgHelper.validateInteraction();
            
            progress.updateProgressControl(loadString("IDS_SQD_CREATE_LIFELINES"), ++index, count);
            if (createLifelines() > 0)
            {
                pumpMessages();
                
                SwingUtilities.invokeLater(new SQDGenerateRunnable( msgHelper, progress, count, index, bShowInteractionBoundary ));
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            EventBlocker.stopBlocking(origBlock);
        }
    }
    
    private boolean inDescribeIDE()
    {
        return false; //(CProductHelper::AIT_DESCRIBE == CProductHelper::Instance()->GetApplicationIDEType());
    }
    
    /**
     * Looks in the preference to see if we should show the interaction boundary
     *
     * @return true if the preference indicates that we should show the interaction boundary
     */
    private boolean showInteractionBoundary()
    {
        boolean showIntBound = false;
        IPreferenceManager2 prefMgr = ProductHelper.getPreferenceManager();
        if (prefMgr != null)
        {
            String prefVal = prefMgr.getPreferenceValue("Default", "Diagrams|SequenceDiagram", "ShowInteractionBoundary");
            
            if (prefVal != null && prefVal.equals("PSK_YES"))
            {
                showIntBound = true;
            }
        }
        return showIntBound;
    }
    
    /**
     * Creates the lifelines, and performs diagram layout
     */
    private int createLifelines()
    {
        int numCreated = 0;
        if (m_Interaction != null && m_Diagram != null)
        {
            ETList < ILifeline > lifelines = m_Interaction.getLifelines();
            if (lifelines != null)
            {
                int count = lifelines.size();
                if (count > 0)
                {
                    ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(m_Diagram);
                    if (relDisco != null)
                    {
                        IETPoint point = PointConversions.newETPoint(new Point(0, 0));
                        if (point != null)
                        {
                            int horizontalOffset = 0;
                            for (int i = 0; i < count; i++, horizontalOffset += 20)
                            {
                                point.setX(horizontalOffset);
                                ILifeline lifeline = lifelines.get(i);
                                IPresentationElement pEle = relDisco.createNodePresentationElement(lifeline, point);
                                
                                ILifelineDrawEngine engine = saveDrawEngineIntoMap(lifeline, pEle);
                                if (engine != null)
                                {
                                    engine.sizeToContents();
                                    IETRect rectBound = TypeConversions.getLogicalBoundingRect(engine);
                                    horizontalOffset = rectBound.getRight();
                                }
                                numCreated++;
                            }
                        }
                    }
                    
                    // Fix W7425:  This layout fixes problems in SunOne IDE
                    //if( CProductHelper::AIT_DESCRIBE != CProductHelper::Instance()->GetApplicationIDEType() )
                    {
                        layoutSequenceDiagram();
                    }
                    
                    // Determine the initial vertical location for starting the messages
                    ILifeline lifeline = lifelines.get(0);
                    
                    // Determine the initial vertical starting point for the messages
                    ILifelineDrawEngine cpEngine = m_MapLifelineToEngine.get(lifeline);
                    if (cpEngine != null)
                    {
                        IETRect rectBounding = TypeConversions.getLogicalBoundingRect(cpEngine);
                        m_InitialMessageY = rectBounding.getIntY() - 50;
                    }
                }
            }
        }
        
        return numCreated;
    }
    
    /**
     * Converts the presentation element into a lifeline draw engine and saves it into out map
     *
     * @param pLifeline[in] Lifeline to associate to the draw engine
     * @param pPE[in] Presentation element to convert to a lifeline draw engine
     */
    private ILifelineDrawEngine saveDrawEngineIntoMap(ILifeline pLifeline, IPresentationElement pPE)
    {
        ILifelineDrawEngine retObj = null;
        if (pLifeline != null && pPE != null)
        {
            IDrawEngine drawEng = TypeConversions.getDrawEngine(pPE);
            if (drawEng != null && drawEng instanceof ILifelineDrawEngine)
            {
                retObj = (ILifelineDrawEngine)drawEng;
                m_MapLifelineToEngine.put(pLifeline, retObj);
            }
        }
        return retObj;
    }
    
    /**
     * Creates the messages, attaching them to their lifelines.
     *
     * @return the vertical location of the last message
     */
    private int createMessages( MessageHelper msgHelper )
    {
        int verticalOffset = -100;
        if (m_Interaction != null && m_MapLifelineToEngine.size() > 0)
        {
            ETList < IMessage > messages = m_Interaction.getMessages();
            if (messages != null)
            {
                int prevKind = IMessageKind.MK_UNKNOWN;
                final int count = msgHelper.getMessageCnt();
                
                // Fix W10056:  Need to make the lifelines longer than 55
                updateLifelineLengths((count + 1) * 60);
                m_Diagram.fitInWindow();
                
                // Ensure that the zoom is not too small.  This avoids the crash problem.
                double zoom = m_Diagram.getCurrentZoom();
                if (zoom < 0.05)
                {
                    m_Diagram.zoom(0.05);
                }
                
                // The initial vertical starting point for the messages
                // is determined in createLifelines().
                verticalOffset = m_InitialMessageY;
                
                for (int i = 0; i < count; i++)
                {
                    IMessage message = msgHelper.getMessages().get(i);
                    ILifeline sendLifeline = message.getSendingLifeline();
                    ILifeline recLifeline = message.getReceivingLifeline();
                    if (sendLifeline == null || recLifeline == null)
                    {
                        // ignore these for now, probably have gates
                    }
                    else
                    {
                        // Determine the message kind here, for debugging
                        // It is used later to determine if the message pump should be run (for create messages)
                        int kind = message.getKind();
                        ILifelineDrawEngine sendEngine = m_MapLifelineToEngine.get(sendLifeline);
                        ILifelineDrawEngine recEngine = m_MapLifelineToEngine.get(recLifeline);
                        if (sendEngine != null && recEngine != null)
                        {
                            // Determine the delta offset for the vertical location
                            // ILifelineDrawEngine::CreateMessage() also changes this height
                            switch (prevKind)
                            {
                                case IMessageKind.MK_CREATE :
                                {
                                    verticalOffset -= 40;
                                }
                                break;
                                
                                case IMessageKind.MK_RESULT :
                                {
                                    if (kind == IMessageKind.MK_RESULT)
                                    {
                                        verticalOffset -= 15;
                                    }
                                    else
                                    {
                                        verticalOffset -= (!sendEngine.equals(recEngine)) ? 60 : 35;
                                    }
                                }
                                break;
                                
                                case IMessageKind.MK_SYNCHRONOUS :
                                {
                                    verticalOffset -= 5;
                                }
                                break;
                                
                                case IMessageKind.MK_ASYNCHRONOUS :
                                {
                                    // It is a very tight area to get two asynch messages next to each other
                                    verticalOffset -= (IMessageKind.MK_ASYNCHRONOUS == kind) ? 50 : 20;
                                }
                                break;
                                
                                default :
                                {
                                    verticalOffset -= 20;
                                }
                                break;
                            }
                            
                            ETPairT<IMessageEdgeDrawEngine,Integer> messageData = sendEngine.createMessage(message,
                                    recEngine, verticalOffset);
                            
                            IMessageEdgeDrawEngine meDrawEngine = messageData.getParamOne();
                            verticalOffset = messageData.getParamTwo().intValue();
                        }
                        
                        pumpMessages();
                        prevKind = kind;
                    }
                }
            }
        }
        return verticalOffset;
    }
    
    /**
     * Makes sure all the lifeline lengths are not too long, or short
     */
    private void updateLifelineLengths(int verticalOffset)
    {
        if (m_Diagram != null)
        {
            ETList < IPresentationElement > pPEs = m_Diagram.getAllByType("Lifeline");
            if (pPEs != null)
            {
                int count = pPEs.size();
                for (int i = 0; i < count; i++)
                {
                    IPresentationElement pEle = pPEs.get(i);
                    IETRect rectBound = (IETRect)TypeConversions.getLogicalBoundingRect(pEle).clone();
                    
                    if (verticalOffset != (rectBound.getBottom()))
                    {
                        rectBound.setBottom(verticalOffset);
                        if (pEle instanceof INodePresentation)
                        {
                            INodePresentation nodePE = (INodePresentation)pEle;
                            nodePE.resize(rectBound.getIntWidth(), rectBound.getIntHeight(), true);
                            
                            // update the activation bars, which merges them where possible
                            // and update the reflexive messages
                            TSENode node = nodePE.getTSNode();
                            if (node != null)
                            {
                                IADLifelineCompartment compartment =
                                        (IADLifelineCompartment)TypeConversions.getCompartment(node,IADLifelineCompartment.class);
                                
                                if (compartment != null)
                                {
                                    compartment.cleanUpActivationBars();
                                    compartment.updateReflexiveBends();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Creates any DestroyAction elements on the lifelines.
     */
    private void createDestroyActions()
    {
        if (m_Interaction != null && m_Diagram != null)
        {
            // Search all the events on each lifeline for any DestroyActions
            ETList < ILifeline > lifelines = m_Interaction.getLifelines();
            if (lifelines != null)
            {
                int count = lifelines.size();
                for (int i = 0; i < count; i++)
                {
                    ILifeline lifeline = lifelines.get(i);
                    createDestroyAction(lifeline);
                }
            }
        }
    }
    
    /**
     * Creates a DestroyAction on the draw engine associated with the lifeline, if necessary.
     *
     * @param pLifeline[in] The lifeline
     */
    private void createDestroyAction(ILifeline pLifeline)
    {
        if (pLifeline != null)
        {
            ETList < IEventOccurrence > events = pLifeline.getEvents();
            if (events != null)
            {
                int count = events.size();
                
                // Save the previous message, to determine where the destroy should be located
                IMessage prevMessage = null;
                for (int i = 0; i < count; i++)
                {
                    IEventOccurrence event = events.get(i);
                    if (containsDestroyAction(event))
                    {
                        int lY = destroyLifelineAfterMessage(pLifeline, prevMessage);
                        
                        // Move the next message down some
                        IMessageEdgeDrawEngine messageDE = findFirstMessageBelow(lY);
                        if (messageDE != null)
                        {
                            messageDE.move(lY - 30, true);
                        }
                        break; //only allow one DestroyAction
                    }
                    
                    IMessage eventMessage = determineEventsMessage(event);
                    if (eventMessage != null)
                    {
                        prevMessage = eventMessage;
                    }
                }
            }
        }
    }
    
    /**
     * Create the comments associated with the lifelines
     */
    private void createComments()
    {
        if (m_Interaction != null)
        {
            IElementLocator locator = new ElementLocator();
            
            // Search for all the comments, including grandchildren, etc.
            ETList < IElement > elems = locator.findElementsByQuery(m_Interaction, ".//UML:Comment");
            if (elems != null)
            {
                int count = elems.size();
                if (count > 0)
                {
                    for (int i = 0; i < count; i++)
                    {
                        IElement elem = elems.get(i);
                        if (elem instanceof IComment)
                        {
                            createComment((IComment)elem);
                        }
                    }
                    
                    // Make sure the comments get moved properly
                    layoutSequenceDiagram();
                }
            }
        }
    }
    
    /**
     * Layout the sequence diagram
     */
    private void layoutSequenceDiagram()
    {
        if (m_Diagram != null)
        {
            // Deselect everything
            m_Diagram.selectAll(false);
            m_Diagram.setLayoutStyleSilently(ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT);
            
            // Pump the all messages, forcing the lifelines to layout properly
            pumpMessages();
            
            // Force everything to draw
            m_Diagram.fitInWindow();
        }
    }
    
    /**
     * Creates the presentation element for a commend associated with a lifeline
     */
    private IPresentationElement createComment(IComment pComment)
    {
        IPresentationElement retEle = null;
        if (m_Diagram != null && pComment != null)
        {
            ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(m_Diagram);
            if (relDisco != null)
            {
                IETPoint point = PointConversions.newETPoint(new Point(0, 0));
                if (point != null)
                {
                    retEle = relDisco.createNodePresentationElement(pComment, point);
                }
            }
        }
        return retEle;
    }
    
    /**
     * Determines if the event contains a DestroyAction.
     *
     * @param pEventOccurrence[in] The EvencOccurrence to test
     *
     * @return True when the EventOccurrence contains a DestroyAction
     */
    private boolean containsDestroyAction(IEventOccurrence pEventOccurrence)
    {
        boolean contains = false;
        if (pEventOccurrence != null)
        {
            IExecutionOccurrence pExec = pEventOccurrence.getStartExec();
            if (pExec == null)
            {
                pExec = pEventOccurrence.getFinishExec();
            }
            
            if (pExec != null && pExec instanceof IActionOccurrence)
            {
                IActionOccurrence pActionOcc = (IActionOccurrence)pExec;
                IAction pAction = pActionOcc.getAction();
                if (pAction != null)
                {
                    String elemType = pAction.getElementType();
                    if (elemType != null && elemType.equals("DestroyAction"))
                    {
                        // Found a destroy action
                        contains = true;
                    }
                }
            }
        }
        return contains;
    }
    
    /**
     * Finds the EventOccurrence's associated message.
     *
     * @param pEventOccurrence[in] The EventOccurrence that may contain a message
     * @param ppMessage[in,out] The Message found in the EventOccurrence, or the original Message
     */
    private IMessage determineEventsMessage(IEventOccurrence pEventOccurrence)
    {
        IMessage pMessage = null;
        if (pEventOccurrence != null)
        {
            pMessage = pEventOccurrence.getSendMessage();
            if (pMessage == null)
            {
                pMessage = pEventOccurrence.getReceiveMessage();
            }
        }
        return pMessage;
    }
    
    /**
     * Places the DestroyAction symbol on the Lifeline's draw engine after the Message.
     *
     * @param pLifeline
     * @param pMessage
     */
    private int destroyLifelineAfterMessage(ILifeline pLifeline, IMessage pMessage /* Can be NULL */
            )
    {
        int lY = 0;
        if (m_Diagram != null && pLifeline != null)
        {
            ILifelineDrawEngine pEngine = m_MapLifelineToEngine.get(pLifeline);
            if (pEngine != null)
            {
                IETRect rect = null;
                if (pMessage != null)
                {
                    // Find the draw engine associated with this Message
                    rect = getElementBoundingRectangle(pMessage);
                }
                else
                {
                    rect = TypeConversions.getLogicalBoundingRect(pEngine);
                }
                
                // Fix W6622:  Make sure the graphical representation for the destroy action
                //             is below any activation bars.
                // Fix W3282:  Needed to move the destroy down a bit more because
                //             the destroy is below the head of a result message.
                // [Phil] Needed to increase once again, from 50 to 300, because a
                //        recursive msg as the bottom msg, requires more room.
                lY = rect.getIntY() + rect.getIntHeight() - 300;
                IETPoint point = PointConversions.newETPoint(new Point((int)rect.getCenterX(), lY));
                pEngine.addDecoration("destroy", point);
                
                lY -= 20;
            }
        }
        return lY;
    }
    
    /**
     * Retrieve the first message draw engine that is below the specified logical vertical location.
     *
     * @param ly[in] Logical vertical location to search below
     * @param ppMessageDE[out] First message draw engine that is below the specified logical vertical location
     */
    private IMessageEdgeDrawEngine findFirstMessageBelow(int lY)
    {
        IMessageEdgeDrawEngine retEng = null;
        if (m_Diagram != null)
        {
            ETList < IPresentationElement > pPEs = m_Diagram.getAllByType("Message");
            if (pPEs != null)
            {
                // Search all the message draw engines,
                // looking for the one that is below, but closest to the input vertical location
                int count = pPEs.size();
                int lDeltaY = Integer.MAX_VALUE;
                IDrawEngine foundEng = null;
                
                for (int i = 0; i < count; i++)
                {
                    IPresentationElement pEle = pPEs.get(i);
                    IDrawEngine pEngine = TypeConversions.getDrawEngine(pEle);
                    if (pEngine != null)
                    {
                        IETRect rect = TypeConversions.getLogicalBoundingRect(pEngine);
                        int testDeltaY = lY - (int)rect.getTop();
                        if (testDeltaY > 0 && testDeltaY < lDeltaY)
                        {
                            lDeltaY = testDeltaY;
                            foundEng = pEngine;
                        }
                    }
                }
                
                if (foundEng != null && foundEng instanceof IMessageEdgeDrawEngine)
                {
                    retEng = (IMessageEdgeDrawEngine)foundEng;
                }
            }
        }
        return retEng;
    }
    
    /**
     * Get the draw engine of the Element.
     *
     * @param pElement[in]
     * @param ppDrawEngine[in]
     */
    private IDrawEngine getDrawEngine(IElement pElement)
    {
        IDrawEngine retEng = null;
        if (pElement != null && m_Diagram != null)
        {
            ETList < IPresentationElement > pPEs = m_Diagram.getAllItems2(pElement);
            if (pPEs != null)
            {
                int count = pPEs.size();
                if (count > 0)
                {
                    // Assume the last presentation element for the Message is the lowest graphically
                    IPresentationElement pElem = pPEs.get(count - 1);
                    retEng = TypeConversions.getDrawEngine(pElem);
                }
            }
        }
        return retEng;
    }
    
    /**
     * Get the logical bounding rectangle of the Element.
     *
     * @param pElement[in] The Element whose logical bounding 3 will be determined
     * @param rrectBounding[out] The logical bounding rectangle of the Element
     */
    private IETRect getElementBoundingRectangle(IElement pElement)
    {
        IETRect retRect = null;
        if (pElement != null)
        {
            IDrawEngine pEngine = getDrawEngine(pElement);
            if (pEngine != null)
            {
                retRect = TypeConversions.getLogicalBoundingRect(pEngine);
            }
        }
        return retRect;
    }
    
    /**
     * Creates the combined fragments
     */
    private void createCombinedFragments()
    {
        if (m_Interaction != null)
        {
            IElementLocator locator = new ElementLocator();
            
            // Search for all the combined fragments, including grandchildren, etc.
            ETList < IElement > pElements = locator.findElementsByQuery(m_Interaction, ".//UML:CombinedFragment");
            if (pElements != null)
            {
                int count = pElements.size();
                
                // Loop in reverse order so "parent" combined fragment will 
                // graphically contian their "children"
                for (int i = count - 1; i >= 0; i--)
                {
                    IElement elem = pElements.get(i);
                    if (elem instanceof ICombinedFragment)
                    {
                        createCombinedFragment((ICombinedFragment)elem);
                    }
                }
                
                IDrawingAreaControl pControl = getDrawingAreaControl();
                if (pControl != null)
                {
                    // Fix W10014:  Loop through the Combined fragments from the top of the diagram down,
                    //              moving them to the top of the graphical stacking order.
                    for (int i = 0; i < count; i++)
                    {
                        IElement pEle = pElements.get(i);
                        ETList < IPresentationElement > pPEs = pControl.getAllItems2(pEle);
                        if (pPEs != null)
                        {
                            int lCnt = pPEs.size();
                            if (lCnt > 0)
                            {
                                IPresentationElement presEle = pPEs.get(0);
                                pControl.executeStackingCommand(presEle, DiagramEnums.SOK_MOVETOFRONT, false);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Creates, and sizes the presentation element for the combined fragment.
     *
     * @param pCombinedFragment[in] The combined fragment to create on the diagram
     *
     * @return HRESULT
     */
    private void createCombinedFragment(ICombinedFragment pCombinedFragment)
    {
        if (m_Diagram != null && pCombinedFragment != null)
        {
            ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(m_Diagram);
            
            // Add the combined fragment to the diagram
            IPresentationElement presEle = null;
            if (relDisco != null)
            {
                presEle = relDisco.createPresentationElement(pCombinedFragment);
            }
            
            // Determine where to place the combined fragment
            if (presEle != null)
            {
                if (presEle instanceof INodePresentation)
                {
                    ((INodePresentation)presEle).setSelected(false);
                }
                
                IDrawEngine pEngine = TypeConversions.getDrawEngine(presEle);
                if (pEngine != null)
                {
                    if (pEngine instanceof ICombinedFragmentDrawEngine)
                    {
                        ((ICombinedFragmentDrawEngine)pEngine).expandToIncludeInteractionOperands(true);
                    }
                    
                    // Make sure the combined fragment is drawn properly before creating the labels
                    pumpMessages();
                    
                    // Make sure the labels get created
                    ILabelManager labelMgr = pEngine.getLabelManager();
                    if (labelMgr != null)
                    {
                        labelMgr.createInitialLabels();
                    }
                }
            }
        }
    }
    
    /**
     * Retrieve the drawing area control associated with the diagram being created
     */
    private IDrawingAreaControl getDrawingAreaControl()
    {
        IDrawingAreaControl retObj = null;
        if (m_Diagram != null && m_Diagram instanceof IUIDiagram)
        {
            retObj = ((IUIDiagram)m_Diagram).getDrawingArea();
        }
        return retObj;
    }
    
    /**
     * @param string
     * @return
     */
    private String loadString(String key)
    {
        return DiagCreatorAddIn.loadString(key);
    }
    
    /**
     * Forces all the message on the windows message que to be processed.
     */
    private void pumpMessages()
    {
        IDrawingAreaControl control = getDrawingAreaControl();
        if (control != null)
        {
            control.pumpMessages(false);
        }
    }
    
    /**
     * Creates and posts the input simple action kind
     */
    private void postSimpleAction(int /*SimpleActionKind*/
            kind)
    {
        if (m_Diagram != null)
        {
            ISimpleAction action = new SimpleAction();
            action.setKind(kind);
            m_Diagram.postDelayedAction(action);
        }
    }
    
}


