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

package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator;

import java.awt.Dimension;
import java.awt.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.UIDiagram;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.ExConnectWithLocationProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.ThermProgress;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.CombinedFragment;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
/**
 * @author sumitabhk
 *
 */
public class SequenceDiagramGenerator implements ISequenceDiagramGenerator
{
    /// map of the lifeline draw engines representing the Lifelines for this interaction
     private Hashtable < ILifeline, UMLNodeWidget > m_MapLifelineToEngine = new Hashtable < ILifeline, UMLNodeWidget > ();
    
    // Original namespace for the interaction
    private INamespace m_Namespace = null;
    private DesignerScene scene;
    // Interaction the sequence diagram is being created from
    private IInteraction m_Interaction = null;
    //The diagram that will contain the sequence interaction
    private IDiagram m_Diagram = null;
    private int m_InitialMessageY = 120;
    private HashMap<IMessage,ConnectionWidget> mapMessages=new HashMap<IMessage,ConnectionWidget>();
    private HashMap<Integer,ICombinedFragment> emptyCFs=new HashMap<Integer,ICombinedFragment>();
    private HashMap<Integer,ICombinedFragment> notEmptyCFs=new HashMap<Integer,ICombinedFragment>();
    private HashMap<Integer,ICombinedFragment> allCFs=new HashMap<Integer,ICombinedFragment>();
    private HashMap<ICombinedFragment,Widget> mapCFToW=new HashMap<ICombinedFragment,Widget>();
    private HashMap<IMessage,ArrayList<Widget>> messageTEmptyCF=new HashMap<IMessage,ArrayList<Widget>>();
    private ArrayList<ICombinedFragment> emptyCFList=new ArrayList<ICombinedFragment>();
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
        scene=null;
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
            //
            if(pExistingDiagram instanceof UIDiagram)
            {
                scene=((UIDiagram)pExistingDiagram).getScene();
            }
        }
        else
        {
            // Create it
            createSequenceDiagram();
        }
        
        
        // Make sure the interaction is named
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
            
            // Make sure the interaction (or project, see below) is set as Dirty
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
        
        public SQDGenerateRunnable( MessageHelper msgHelper, ThermProgress progress,int count,int index)
        {
            m_msgHelper = msgHelper;
            m_progress = progress;
            m_count = count;
            m_index = index;
        }
        
        public void run()
        {
            boolean origBlock = EventBlocker.startBlocking();
            
            try
            {
                m_progress.updateProgressControl(loadString("IDS_SQD_CREATE_MESSAGES"), ++m_index, m_count);
                m_verticalOffset = createMessages( m_msgHelper );
                
                m_progress.updateProgressControl(loadString("IDS_SQD_UPDATE_LIFELINES"), ++m_index, m_count);
                
                m_progress.updateProgressControl(loadString("IDS_PERFORMING_LAYOUT"), ++m_index, m_count);
 
                
                // Create the comments associated with the lifelines
                m_progress.updateProgressControl(loadString("IDS_SQD_COMMENTS"), ++m_index, m_count);
                createComments();
                
                // Fix W6744:  Show the interaction boundary, preference based
//                if (m_showInteractionBoundary)
                {
//                    m_progress.updateProgressControl(loadString("IDS_SQD_SHOW_BOUNDARY"), ++m_index, m_count);
//                    IDiagramEngine diaEngine = TypeConversions.getDiagramEngine(m_Diagram);
//                    if (diaEngine != null && diaEngine instanceof IADSequenceDiagEngine)
//                    {
//                        ((IADSequenceDiagEngine)diaEngine).showInteractionBoundary(true);
//                    }
                }
                
                // Fix W7315:  Create the combined fragments after showing the interaction boundary
                //             Perform a layout of the diagram first
                m_progress.updateProgressControl(loadString("IDS_PERFORMING_LAYOUT"), ++m_index, m_count);
                
                // The pumpmessages is necessary to get the above layout to properly align the create messages.
                pumpMessages();
                normalizeLifelines();
                
                m_progress.updateProgressControl(loadString("IDS_SQD_COMBINED_FRAGMENTS"), ++m_index, m_count);
                createCombinedFragments();
                
                // UPDATE:  These should have been discovered when they were created?
                // Discover the UML::Comment relationships
                m_progress.updateProgressControl(loadString("IDS_SQD_RELATIONSHIPS"), ++m_index, m_count);
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
            
            int count = 8 + 1;
            int index = 0;
            progress.updateProgressControl(loadString("IDS_SQD_VALIDATE_INTERACTION"), index, count);
            MessageHelper msgHelper = new MessageHelper( m_Interaction );
            msgHelper.validateInteraction();
            
            progress.updateProgressControl(loadString("IDS_SQD_CREATE_LIFELINES"), ++index, count);
            if (createLifelines() > 0)
            {
                SwingUtilities.invokeLater(new SQDGenerateRunnable( msgHelper, progress, count, index ));
            }
            else
            {
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
    
    
    /**
     * Creates the lifelines, and performs diagram layout
     */
    private int createLifelines()
    {
        int numCreated = 0;
        if (m_Interaction != null && m_Diagram != null && scene!=null)
        {
            DiagramEngine engine=scene.getEngine();
            ETList < ILifeline > lifelines = m_Interaction.getLifelines();
            if (lifelines != null)
            {
                int count = lifelines.size();
                if (count > 0)
                {
                    {
                        Point point = new Point(30, 60);
                        if (point != null)
                        {
                            int horizontalOffset = 30;
                            for (int i = 0; i < count; i++, horizontalOffset += 20)
                            {
                                point.x=horizontalOffset;
                                ILifeline lifeline = lifelines.get(i);
                                IPresentationElement pEle = createPresentationElement(lifeline, engine);
                                Widget addedW=engine.addWidget(pEle, point);
                                if(addedW instanceof UMLNodeWidget)
                                {
                                    ((UMLNodeWidget)addedW).initializeNode(pEle);
                                    saveDrawEngineIntoMap(lifeline, pEle);
                                }
                                else
                                {
                                    UMLLogger.logMessage("attempt to add a lifeline cause addition of "+addedW,Level.WARNING);
                                }
                                scene.validate();
                                horizontalOffset=addedW.getPreferredLocation().x+addedW.getPreferredBounds().x+addedW.getPreferredBounds().width;
                                numCreated++;
                            }
                        }
                    }
                }
            }
        }
        return numCreated;
    }
    
    private void normalizeLifelines() {
        Collection<UMLNodeWidget> lifelines=m_MapLifelineToEngine.values();
        scene.validate();
        int maxY=0;
        for(UMLNodeWidget llW:lifelines)
        {
            if((llW.getBounds().y+llW.getBounds().height+llW.getLocation().y)>maxY)
            {
                maxY=(llW.getBounds().y+llW.getBounds().height+llW.getLocation().y);
            }
        }
        for(Widget llW:mapCFToW.values())
        {
            if((llW.getBounds().y+llW.getBounds().height+llW.getLocation().y+20)>maxY)
            {
                maxY=(llW.getBounds().y+llW.getBounds().height+llW.getLocation().y+20);
            }
        }
        for(UMLNodeWidget llW:lifelines)
        {
            Dimension minSize=llW.getMinimumSize();
            minSize.height=maxY-llW.getLocation().y;
            llW.setMinimumSize(minSize);
        }
    }
    
    /**
     * Converts the presentation element into a lifeline draw engine and saves it into out map
     *
     * @param pLifeline[in] Lifeline to associate to the draw engine
     * @param pPE[in] Presentation element to convert to a lifeline draw engine
     */
    private UMLNodeWidget saveDrawEngineIntoMap(ILifeline pLifeline, IPresentationElement pPE)
    {
        UMLNodeWidget retObj = null;
        if (pLifeline != null && pPE != null)
        {
            Widget drawEng = (Widget) scene.findWidget(pPE);
            if (drawEng != null && drawEng instanceof UMLNodeWidget)
            {
                retObj = (UMLNodeWidget)drawEng;
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
        int verticalOffset = 120;
        if (m_Interaction != null && scene!=null)
        {
            findCombinedFragments();
            ETList < IMessage > messages = m_Interaction.getMessages();
            if (messages != null && messages.size()>0)
            {
                int prevKind = IMessageKind.MK_UNKNOWN;
                final int count = msgHelper.getMessageCnt();
                                
                // Ensure that the zoom is not too small.  This avoids the crash problem.
                double zoom = scene.getZoomFactor();
                if (zoom < 0.05)
                {
                    scene.setZoomFactor(0.05);
                }
                
                // The initial vertical starting point for the messages
                // is determined in createLifelines().
                verticalOffset = m_InitialMessageY;
                ExConnectWithLocationProvider messageConnectProvider=null;
                ArrayList <IMessage> resultMessages=new ArrayList <IMessage>();
                mapMessages=new HashMap<IMessage,ConnectionWidget>();//be careful if later it will not be first place where messages are cteated
                IInteractionOperand lastOperand=messages.get(0).getInteractionOperand();
                int prevLineNumber=-1;
                for (int i = 0; i < count; i++)
                {
                    IMessage message = messages.get(i);
                    int thisLineNumber=message.getLineNumber();
                    ILifeline sendLifeline = message.getSendingLifeline();
                    ILifeline recLifeline = message.getReceivingLifeline();
                    //TBD check cases for absent sendingLifeline or/and receivingLifeline
                    if(message.getKind()==IMessageKind.MK_SYNCHRONOUS)
                    {
                        //let's find return message instead and pass it to connector
                        for(int j=i+1;j<count;j++)
                        {
                            IMessage tmp=messages.get(j);
                            
                            if(tmp.getKind()==IMessageKind.MK_RESULT)
                            {
                                ILifeline sendL = tmp.getSendingLifeline();
                                ILifeline recL = tmp.getReceivingLifeline();
                                //if(sendLifeline==recL && recLifeline==sendL)
                                if(tmp.getSendingMessage()==message)
                                {
                                    message=tmp;
                                    sendLifeline=sendL;
                                    recLifeline=recL;
                                    break;
                                }
                            }
                        }
                    }
                    else if(message.getKind()==IMessageKind.MK_RESULT)
                    {
                        resultMessages.remove(message);
                        verticalOffset += 50;
                        if(sendLifeline==recLifeline)verticalOffset += 35;//message to self
                        continue;
                    }
                    SQDDiagramEngineExtension engine=(SQDDiagramEngineExtension) scene.getEngine();
                    messageConnectProvider=(ExConnectWithLocationProvider) engine.getConnectProvider(message,null);
                    
                    IInteractionOperand thisOperand=message.getInteractionOperand();
                    if(lastOperand!=thisOperand)
                    {
                        //if operand is changed between messaage and previous message need to add space for separator
                        if(lastOperand==null)
                        {
                            //most likely jump into combined fragment border
                            //verticalOffset+=25;
                            for(IElement owner=thisOperand.getOwner();owner!=null && !(owner instanceof IProject);owner=owner.getOwner())
                            {
                                if(owner instanceof ICombinedFragment)
                                {
                                    verticalOffset+=25;//each combined fragment need operand constraint and line space
                                }
                            }
                        }
                        else if(thisOperand==null)
                        {
                            //jump out of combined fragments
                           for(IElement owner=lastOperand.getOwner();owner!=null && !(owner instanceof IProject);owner=owner.getOwner())
                            {
                                if(owner instanceof ICombinedFragment)
                                {
                                    verticalOffset+=10;//each combined fragment need line space
                                }
                            }
                        }
                        else if(thisOperand.getOwner()==lastOperand.getOwner())
                        {
                            //both in one cf
                            verticalOffset+=25;
                        }
                        else if(lastOperand.getOwner()==thisOperand.getOwner().getOwner().getOwner())
                        {
                            //just jump into one level of cf
                            verticalOffset+=25;
                        }
                        else if(thisOperand.getOwner()==lastOperand.getOwner().getOwner().getOwner())
                        {
                            //just jump out of one level of cf
                            verticalOffset+=10;
                        }
                        else
                        {
                            //in different owners more complex cases, good to fins number of lines between, but use simple logic for now
                            verticalOffset+=40;
                        }
                        
                    }
                    lastOperand=thisOperand;
                    //
                    for(Integer ecfLN:emptyCFs.keySet())
                    {
                        if(ecfLN>prevLineNumber && ecfLN<thisLineNumber)
                        {
                            verticalOffset+=50*emptyCFs.get(ecfLN).getOperands().size();
                        }
                    }
                    //
                    prevLineNumber=thisLineNumber;
                    if (sendLifeline == null || recLifeline == null)
                    {
                        // ignore these for now, probably have gates
                        verticalOffset += 55;//anyway shift, it may help later with addition
                    }
                    else
                    {
                        UMLNodeWidget sendLL=m_MapLifelineToEngine.get(sendLifeline);
                        UMLNodeWidget receiveLL=m_MapLifelineToEngine.get(recLifeline);
                        //in some rare cases, not reproducible consistently nodes are missed, need to skip this message
                        //it may be better to skip even if it's error somewhere to avoid fail in diagram creation with more worst result.
                        if(sendLL==null || receiveLL==null)
                        {
                            verticalOffset += 15;
                            UMLLogger.logMessage("source or target lifeline is missed, can't create message",Level.WARNING);
                            continue;
                        }
                        //
                        Point startingPoint=new Point(0,verticalOffset);
                        Point finishingPoint=new Point(0,verticalOffset);
                        //check if need to bump down result message
                        if(resultMessages.size()>0)
                        {
                             //consider the only most inner may affect smth
                             IMessage lastResMessage=resultMessages.get(resultMessages.size()-1);
                             {
                                ConnectionWidget resultW=mapMessages.get(lastResMessage);
                                int location=resultW.getSourceAnchor().getRelatedSceneLocation().y;
                                if(location<(verticalOffset+40+30*(sendLL==receiveLL ? 1:0)))
                                {
                                    //need to bump
                                    engine.bumpMessage(resultW, (verticalOffset+40+30*(sendLL==receiveLL ? 1:0))-location);
                                    scene.validate();
                                }
                            }
                        }
                        //
                        ArrayList<ConnectionWidget> result=messageConnectProvider.createConnection(sendLL, receiveLL, startingPoint, finishingPoint);
                        scene.validate();
                        if(result.size()==1)
                        {
                            mapMessages.put(message,result.get(0));
                        }
                        else if (result.size()==2)
                        {
                            //synchronous message
                            mapMessages.put(message.getSendingMessage(),result.get(0));
                            mapMessages.put(message,result.get(1));
                            resultMessages.add(message);
                        }
                        // Determine the message kind here, for debugging
                        // It is used later to determine if the message pump should be run (for create messages)
                        int kind = message.getKind();

                        switch (kind)
                        {
                            case IMessageKind.MK_CREATE :
                            verticalOffset += 55;
                                break;

                            case IMessageKind.MK_RESULT :
                            verticalOffset += 40;
                            break;

                            case IMessageKind.MK_SYNCHRONOUS :
                            verticalOffset += 55;
                            break;

                            case IMessageKind.MK_ASYNCHRONOUS :
                            verticalOffset += 55;
                            break;

                            default :
                            break;
                        }
                        if(sendLifeline==recLifeline)verticalOffset += 25;//message to self
                        
                        prevKind = kind;
                    }
                }
                //go and sow labe;s
                for(IMessage k:mapMessages.keySet())
                {
                    ConnectionWidget w=mapMessages.get(k);
                    if(w instanceof UMLEdgeWidget)
                    {
                        LabelManager lm= getLabelManager((UMLEdgeWidget)w);
                        if(lm!=null)
                        {
                            if(k.getOperationInvoked()!=null && k.getKind()!=IMessageKind.MK_RESULT)//if it's result do not show operation, its enouth to show on call only
                            {
                                lm.showLabel(OPERATION);
                            }
                            else if(k.getName()!=null && k.getName().length()>0)
                            {
                                lm.showLabel(NAME);
                            }
                        }
                    }
                }
                layoutSequenceDiagram();//we have lifelines and messages now, may want to relayout lifelines based on message labels sizes
            }
        }
        return verticalOffset;
    }
    
    
    /**
     * Create the comments associated with the lifelines
     */
    private void createComments()
    {
       int x=10,y=10;
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
                            Point point = new Point(x, y);
                            y+=60;
                            if(y%1210==0)
                            {
                                y=10;
                                x+=100;
                            }
                            createComment((IComment)elem,point);
                        }
                    }
                    
                    // Make sure the comments get moved properly
                    //layoutSequenceDiagram();
                }
            }
        }
    }
    
    /**
     * TBD made combned fragments arrangment here
     * also bump messages to label width (before cf)
     * also move comments to some side of commented element, may be try to stack if one element have several comments
     * Layout the sequence diagram
     */
    private void layoutSequenceDiagram()
    {
        if (m_Diagram != null)
        {
            // Deselect everything
            m_Diagram.selectAll(false);
            //m_Diagram.setLayoutStyleSilently(ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT);
            ((SQDDiagramEngineExtension) scene.getEngine()).layout(false);
            // Force everything to draw
            //m_Diagram.fitInWindow();
        }
    }
    
    /**
     * Creates the presentation element for a commend associated with a lifeline
     */
    private IPresentationElement createComment(IComment pComment,Point point)
    {
         IPresentationElement retEle = null;
        if (m_Diagram != null && pComment != null && scene!=null)
        {
            //ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(m_Diagram);
            DiagramEngine engine=scene.getEngine();
            if (engine != null)
            {
                if (point != null)
                {
                    IPresentationElement pEle = createPresentationElement(pComment, engine);
                    Widget addedW=engine.addWidget(pEle, point);
                    if(addedW instanceof UMLNodeWidget)
                    {
                        ((UMLNodeWidget)addedW).initializeNode(pEle);
                    }
                    //retEle = relDisco.createNodePresentationElement(pComment, point);
                }
            }
        }
        return retEle;
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
     * Creates the combined fragments
     */
    private void createCombinedFragments()
    {
        if (m_Interaction != null)
        {
            IElementLocator locator = new ElementLocator();
            
            // Search for all the combined fragments, including grandchildren, etc.
            final ETList < IElement > pElements = locator.findElementsByQuery(m_Interaction, ".//UML:CombinedFragment");
            //may need to revert and move empty to be created first
            pElements.removeAll(emptyCFs.values());
            for(int i=emptyCFList.size()-1;i>=0;i--)pElements.add(emptyCFList.get(i));
            //
            if (pElements != null)
            {
                Point point=new Point(10,10);
                createCombinedFragments(pElements,pElements.size()-1,point);
                new AfterValidationExecutor(new ActionProvider() {
                   public void perfomeAction() {
                        //start containment handling after last validation with creation of cf
                        //TBD move to populate containers in DiagCreatorAddin
                            handleConteinmentForCombinedFragments(pElements,pElements.size()-1);//need to handle containment after all expansions/resizing after creation
                     }
                }, scene);
                scene.revalidate();
                scene.validate();
            }
            else
            {
                revalidateAndSave();
            }
        }
    }
    /**
     * Creates the combined fragments
     */
    private void findCombinedFragments()
    {
        if (m_Interaction != null)
        {
            IElementLocator locator = new ElementLocator();
            
            // Search for all the combined fragments, including grandchildren, etc.
            final ETList < IElement > pElements = locator.findElementsByQuery(m_Interaction, ".//UML:CombinedFragment");
            if (pElements != null)
            {
                for(IElement cfE:pElements)
                {
                    boolean empty=true;
                    ICombinedFragment cf=(ICombinedFragment) cfE;
                    empty=empty && cf.getCoveredLifelines().size()==0;
                    for(IInteractionOperand io:cf.getOperands())
                    {
                        empty=empty&&io.getCoveredMessages().size()==0;
                        empty=empty&&io.getCoveredLifelines().size()==0;
                        empty=empty&&locator.findElementsByQuery(io, ".//UML:CombinedFragment").size()==0;
                    }
                    if(empty)
                    {
                        emptyCFs.put(cf.getOperands().get(0).getLineNumber(), cf);
                        emptyCFList.add(cf);
                    }
                    allCFs.put(cf.getOperands().get(0).getLineNumber(), cf);
                }
           }

        }
    }
    
    /**
     * Creates the combined fragments, handle validation between each cf creation
     * do it in reverse order
     */
    private void createCombinedFragments(final ETList < IElement > pElements,final int index,final Point point)
    {
        if(index>=0)
        {
            IElement elem = pElements.get(index);
            if (elem instanceof ICombinedFragment)
            {
                createCombinedFragment((ICombinedFragment)elem,point);
                new AfterValidationExecutor(new ActionProvider() {

                    public void perfomeAction() {
                            createCombinedFragments(pElements,index-1,point);
                    }
                }, scene);
                scene.validate();
            }
            else 
            {
                createCombinedFragments(pElements,index-1,point);
            }
        }
        else
        {
            normalizeLifelines();
        }
    }
    /**
     * Creates the combined fragments, handle validation between each cf creation
     * do it in reverse order
     */
    private void handleConteinmentForCombinedFragments(final ETList < IElement > pElements,final int index)
    {
        if(index>=0)
        {
            IElement elem = pElements.get(index);
            if (elem instanceof ICombinedFragment)
            {
                handleConteinmentForCombinedFragment((ICombinedFragment)elem);
                new AfterValidationExecutor(new ActionProvider() {

                    public void perfomeAction() {
                        handleConteinmentForCombinedFragments(pElements,index-1);
                    }
                }, scene);
                scene.validate();
            }
            else handleConteinmentForCombinedFragments(pElements,index-1);
        }
        else
        {
            revalidateAndSave();
        }
    }
    
    
    /**
     * Creates, and sizes the presentation element for the combined fragment.
     *
     * @param pCombinedFragment[in] The combined fragment to create on the diagram
     *
     * @return HRESULT
     */
    private void createCombinedFragment(ICombinedFragment pCombinedFragment,Point point)
    {
         if (m_Diagram != null && pCombinedFragment != null)
        {
            //ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(m_Diagram);
            DiagramEngine engine=scene.getEngine();
            
            // Add the combined fragment to the diagram
            IPresentationElement presEle = null;
            Widget addedW=null;
            if (engine != null)
            {
                presEle = createPresentationElement(pCombinedFragment,engine);
            }
            
            addedW=engine.addWidget(presEle, point);
            mapCFToW.put(pCombinedFragment, addedW);
            //now find message before cf and after cf, it may be the only information for cf positionin/sizing if cf hve no covered messages
                int cf_line=pCombinedFragment.getOperands().get(0).getLineNumber();
                //find messages:
                IMessage msgBefore=null,msgAfter=null;
                int msgBeforeLN=-1,msgAfterLN=Integer.MAX_VALUE;
                for(IMessage msg:mapMessages.keySet())
                {
                    if(msg.getLineNumber()<=cf_line && (msg.getLineNumber()>msgBeforeLN || (msg.getLineNumber()==msgBeforeLN && msg.getKind()==BaseElement.MK_RESULT)))//if it's on the sameline most likely message create smth used in cf
                    {
                        msgBeforeLN=msg.getLineNumber();
                        msgBefore=msg;
                    }
                    else if(msg.getLineNumber()<msgAfterLN && msg.getLineNumber()>cf_line)
                    {
                        msgAfterLN=msg.getLineNumber();
                        msgAfter=msg;
                    }
                }
                ICombinedFragment cfBefore=null,cfAfter=null;
                int cfBeforeLN=-1,cfAfterLN=Integer.MAX_VALUE;
                for(ICombinedFragment cf:allCFs.values())
                {
                    if(cf.getOperands().get(0).getLineNumber()>cfBeforeLN && cf.getOperands().get(0).getLineNumber()<cf_line)
                    {
                        cfBeforeLN=cf.getOperands().get(0).getLineNumber();
                        cfBefore=cf;
                    }
                    else if(cf.getOperands().get(0).getLineNumber()<cfAfterLN && cf.getOperands().get(0).getLineNumber()>cf_line)
                    {
                        cfAfterLN=cf.getOperands().get(0).getLineNumber();
                        cfAfter=cf;
                    }
                }
           
            CombinedFragment cf=(CombinedFragment) addedW;//I expect combined fragment widget here, but may be check is good also, later
             // Determine where to place the combined fragment
            if (presEle != null)
            {
                if (addedW != null)
                {
                    cf.setMessageBefore(msgBefore, mapMessages.get(msgBefore));
                    cf.setMessageAfter(msgAfter, mapMessages.get(msgAfter));
                    cf.setCombinedFragmentBefore(cfBefore,mapCFToW.get(cfBefore));
                    cf.setCombinedFragmentAfter(cfAfter,mapCFToW.get(cfAfter));
                    cf.resizeToModelContent();
                    cf.showLabels();
                 }
            }
            //
            point.y+=100;
        }
    }
   private void handleConteinmentForCombinedFragment(ICombinedFragment pCombinedFragment) {
        if (m_Diagram != null && pCombinedFragment != null)
        {
            for(IPresentationElement pe:pCombinedFragment.getPresentationElements())
            {
                Widget w=scene.findWidget(pe);
                if(w instanceof CombinedFragment)
                {
                   ContainerWidget cont=((CombinedFragment)w).getContainer();
                   if(cont!=null)cont.calculateChildren(true);
                }
            }
        }
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
//        IDrawingAreaControl control = getDrawingAreaControl();
//        if (control != null)
//        {
//            control.pumpMessages(false);
//        }
    }
    
    /**
     * Creates and posts the input simple action kind
     */
    private void postSimpleAction(int /*SimpleActionKind*/
            kind)
    {
//        if (m_Diagram != null)
//        {
//            ISimpleAction action = new SimpleAction();
//            action.setKind(kind);
//            m_Diagram.postDelayedAction(action);
//        }
    }
    
    private IPresentationElement createPresentationElement(INamedElement element,DiagramEngine engine)
    {
        element=engine.processDrop(element);

        IPresentationElement presentation = Util.createNodePresentationElement();
        presentation.addSubject(element);
        
        return presentation;
    }
    
    
    private LabelManager getLabelManager(UMLEdgeWidget edgeW)
    {
        LabelManager manager = null;
        Lookup lookup = edgeW.getLookup();
        if (lookup != null)
        {
            manager = lookup.lookup(LabelManager.class);
            if (manager != null)
            {
                return manager;
            }
        }
        return manager;
    }
    
    private void revalidateAndSave()
    {
        scene.revalidate();
        new AfterValidationExecutor(new ActionProvider() {
            public void perfomeAction() {
                try {
                    scene.getDiagram().setDirty(true);
                    scene.getDiagram().save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, scene);
        scene.validate();
    }
    
    public static final String NAME = "Name"; //NOI18N
    public static final String OPERATION = "Operation"; //NOI18N
    public static final String NUMBER = "Number"; //NOI18N
}


