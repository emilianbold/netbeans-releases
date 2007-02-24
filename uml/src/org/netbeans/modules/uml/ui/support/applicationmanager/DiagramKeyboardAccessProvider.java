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

package org.netbeans.modules.uml.ui.support.applicationmanager;

import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import java.awt.event.InputEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import com.tomsawyer.util.TSSystem;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.graph.TSEdge;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.ADCoreEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETUIFactory;
import org.netbeans.modules.uml.ui.support.relationshipVerification.AddEdgeEventDispatcher;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaActions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaConstants;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaResourceBundle;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADAddAssociationClassEdgeTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.DiagramAddAssemblyConnectorTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.DiagramAddPartFacadeTool;

/**
 *
 * @author Thuy
 */
public class DiagramKeyboardAccessProvider
{
    private static DiagramKeyboardAccessProvider kbAccessProvider;
    private IDrawingAreaControl drawingAreaCtrl;
    private AddEdgeEventDispatcher m_eventDispatcher = null;
    
    /**
     * Creates a new instance of DiagramKeyboardAccessProvider
     */
    private DiagramKeyboardAccessProvider()
    {
    }
    
    public static DiagramKeyboardAccessProvider getInstance(IDrawingAreaControl daCtrl) 
    {
        if (kbAccessProvider == null) 
        {
            kbAccessProvider = new DiagramKeyboardAccessProvider();
        }
        
        kbAccessProvider.setDiagramDrawingCtrl(daCtrl);
        return kbAccessProvider;
    }
    
    public IDrawingAreaControl getDiagramDrawingCtrl ()
    {
        return this.drawingAreaCtrl;
    }
    
    public void setDiagramDrawingCtrl (IDrawingAreaControl val)
    {
        this.drawingAreaCtrl = val;
    }
    
    /**
     * This method registers all the key actions to the related keys.
     */
    public void registerKeyCommands(JComponent component)
    {
        int i = 1;
        String keyCodeString;
        if (component == null)
        {
            return;
        }
        ADDrawingAreaResourceBundle bundle = drawingAreaCtrl.getResources();
        ADDrawingAreaActions drawingActions = ((ADDrawingAreaControl)drawingAreaCtrl).getActions();
        
        while ((keyCodeString = bundle.getStringResource("key." + i + ".keyCode")) != null)
        {
            int keyCode = Integer.valueOf(keyCodeString).intValue();
            int modifiers = Integer.valueOf(bundle.getStringResource("key." + i + ".modifiers")).intValue();
            String command = bundle.getStringResource("key." + i + ".command");
            int focus = Integer.valueOf(bundle.getStringResource("key." + i + ".focus")).intValue();
            boolean released = (bundle.getStringResource("key." + i + ".released") != null);
            
            component.registerKeyboardAction(drawingActions, command, 
                KeyStroke.getKeyStroke(keyCode, modifiers, released), focus);
            
            // In JDK1.4, key events while mouse is pressed are different
            // from key events where mouse is not pressed, so we need
            // to register these too.
            
            if (TSSystem.isJVM14orAbove())
            {
                int newModifiers = modifiers | InputEvent.BUTTON1_MASK;
                component.registerKeyboardAction(drawingActions, command, 
                    KeyStroke.getKeyStroke(keyCode, newModifiers, released), focus);
                
                newModifiers = modifiers | InputEvent.BUTTON2_MASK;
                component.registerKeyboardAction(drawingActions, command, 
                    KeyStroke.getKeyStroke(keyCode, newModifiers, released), focus);
                
                newModifiers = modifiers | InputEvent.BUTTON3_MASK;
                component.registerKeyboardAction(drawingActions, command, 
                    KeyStroke.getKeyStroke(keyCode, newModifiers, released), focus);
            }
            i++;
        }
    }
    
    /**
     * This method unregisters some keys, which are used to zoom and
     * scroll, when "auto fit in window" is on. It simply binds them
     * to a dummy command.
     */
    public void unregisterKeyCommands(JComponent component)
    {
        int i = 1;
        String keyCodeString;
        
        if (component == null)
        {
            return;
        }
        
        ADDrawingAreaResourceBundle bundle = drawingAreaCtrl.getResources();
        if (drawingAreaCtrl != null && drawingAreaCtrl instanceof JComponent)
        {
            component = (JComponent)drawingAreaCtrl;
        }
        
        ADDrawingAreaActions drawingActions = ((ADDrawingAreaControl)drawingAreaCtrl).getActions();
        
        while ((keyCodeString = bundle.getStringResource("key." + i + ".keyCode")) != null)
        {
            int keyCode = Integer.valueOf(keyCodeString).intValue();
            int modifiers = Integer.valueOf(bundle.getStringResource("key." + i + ".modifiers")).intValue();
            String command = bundle.getStringResource("key." + i + ".command");
            int focus = Integer.valueOf(bundle.getStringResource("key." + i + ".focus")).intValue();
            boolean released = (bundle.getStringResource("key." + i + ".released") != null);
            
            if ((command.indexOf("ZOOM") >= 0) || (command.indexOf("MOVE") >= 0) || (command.indexOf("SCROLL") >= 0))
            {
                component.registerKeyboardAction(drawingActions, 
                    ADDrawingAreaConstants.ACTION_ABORT, 
                    KeyStroke.getKeyStroke(keyCode, modifiers, released), focus);
            }
            
            i++;
        }
    }
    
    public boolean onCreateNewNodeByKeyboard()
    {
        boolean retVal = false;
        if ( drawingAreaCtrl == null)
        {
            return retVal;
            }
        ADCoreEngine coreEngine = null;
        String buttonID = drawingAreaCtrl.getSelectedPaletteButton();
        IDiagramEngine diagramEngine = drawingAreaCtrl.getDiagramEngine();
        if ( diagramEngine instanceof ADCoreEngine)
        {
            coreEngine = (ADCoreEngine)diagramEngine;
        }
        
        IPresentationTypesMgr mgr = drawingAreaCtrl.getPresentationTypesMgr();
        
        if (buttonID == null || buttonID.trim().length() == 0)
        {
            return retVal;
        }
        
        boolean bReadOnly = false;
        
        int diagramKind = IDiagramKind.DK_UNKNOWN;
        IDiagram pDiagram = drawingAreaCtrl.getDiagram();
        if (pDiagram != null)
        {
            diagramKind = pDiagram.getDiagramKind();
        }
        
        bReadOnly = drawingAreaCtrl.getReadOnly();
        if (!bReadOnly)
        {
            String initString = "";
            buttonID = redefineButtonID(buttonID);
            initString = mgr.getButtonInitString(buttonID, diagramKind);
            
            if (initString != null && initString.length() > 0)
            {
                PresentationTypeDetails details = mgr.getInitStringDetails(initString, diagramKind);
                int objectKind = details.getObjectKind();
                
                if (objectKind == TSGraphObjectKind.TSGOK_NODE ||
                    objectKind == TSGraphObjectKind.TSGOK_NODE_RESIZE) // Handle non-edge elements
                {
                    IETPoint pCenterPoint = coreEngine.getLogicalCenter();
                    if (pCenterPoint != null)
                    {
                        int x = pCenterPoint.getX();
                        int y = pCenterPoint.getY();
                        int xOffset = coreEngine.getAcceleratorOffset().x;
                        int yOffset = coreEngine.getAcceleratorOffset().y;
                        
                        drawingAreaCtrl.refresh(true);
                        
                        pCenterPoint.setPoints(x + xOffset, y + yOffset);
                        
                        try
                        {
                            TSNode pCreatedNode = drawingAreaCtrl.addNode(initString, pCenterPoint, true, true);
                            retVal = true;
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        
                        // Increment the accelerator offset in case the user hits the accel key again
                        coreEngine.getAcceleratorOffset().setLocation(xOffset+20,yOffset-20);
                    }
                }
                else if (objectKind == TSGraphObjectKind.TSGOK_EDGE) // Hanlde edge elements
                {
                    retVal = handleEdgeCreation (buttonID, initString);
                }
                else
                {
                    // Do Nothing or display a message.
                }
                
                // reset the cursor to default cursor
                drawingAreaCtrl.enterMode(IDrawingToolKind.DTK_SELECTION);
                drawingAreaCtrl.setFocus();
            }
        }
        return retVal;
    }
    
    private boolean handleEdgeCreation (String buttonID, String initStr)
    {
        boolean handled = false;
        ETList<IPresentationElement> selectedElems = drawingAreaCtrl.getSelected();
        //List selectedElems = ((ADDrawingAreaControl) drawingAreaCtrl).getSelectedNodesGroup();
        
        if (buttonID == null || initStr == null || initStr.trim().length() == 0)
        {
            return handled;
        }
        // if no element is selected or element count is not 2, no need to process further
        if ( selectedElems == null || selectedElems.size() != 2)  
        {
            return handled;
        }
        
        // exactly 2 elements are selected.  Now check if they are of type INodePresentation
        boolean validNode = true;
        ETList<IPresentationElement> selectedGraphics = new ETArrayList<IPresentationElement>();
        
        for (Object elem  : selectedElems)
        {
            if ((elem instanceof IPresentationElement ))
            {
                selectedGraphics.add( ((IPresentationElement)elem) ); 
            }
            else
            {
                validNode = false;
                break;
            }
        }
        
        if (!validNode)  // one of the selected elements is not a node
        {
            return handled;
        }
        
        boolean proceed = true;
        IPresentationElement graphic1 = (IPresentationElement) selectedGraphics.get(0);   
        IPresentationElement graphic2 = (IPresentationElement) selectedGraphics.get(1);
        

        IPresentationElement fromElem = graphic1;
        IPresentationElement toElem = graphic2;
        
        IElement elem1 = graphic1.getFirstSubject();
        IElement elem2 = graphic2.getFirstSubject();
        if (elem1 != null && elem2 != null) 
        {
            String elemType1 = elem1.getElementType();
            String elemType2 = elem2.getElementType();
            
            if ("ID_VIEWNODE_UML_IMPLEMENTATION".equals(buttonID))
            {
                //Make sure one is class and the other is interface
                if (elemType1.equals("Class") && elemType2.equals("Interface"))
                {
                    fromElem = graphic1;
                    toElem = graphic2;
                }
                else if (elemType1.equals("Interface") && elemType2.equals("Class"))
                {
                    fromElem = graphic2;
                    toElem = graphic1; 
                }
                else
                {
                    proceed = false;
                }
            }
            if ("ID_VIEWNODE_UML_ASSEMBLYCONNECTOR_INITIALEDGE".equals(buttonID))
            {
                //Make sure one is Component and the other is Interface
                if (elemType1.equals("Component") && elemType2.equals("Interface"))
                {
                    fromElem = graphic1;
                    toElem = graphic2;
                }
                else if (elemType1.equals("Interface") && elemType2.equals("Component"))
                {
                    fromElem = graphic2;    
                    toElem = graphic1; 
                }
                else
                {
                    proceed = false;
                }
            }
            else if ("ID_VIEWNODE_UML_DERIVATIONEDGE".equals(buttonID))
            {
                //Make sure one is class and the other is DerivationClassifier
                if (elemType1.equals("DerivationClassifier") && elemType2.equals("Class"))
                {
                    fromElem = graphic1;
                    toElem = graphic2;
                }
                else if (elemType1.equals("Class") && elemType2.equals("DerivationClassifier"))
                {
                    fromElem = graphic2;
                    toElem = graphic1; 
                }
                else
                {
                    proceed = false;
                }
                
            }
            else if ("ID_VIEWNODE_UML_COMMENTLINK".equals(buttonID))
            {
                String commentType = "Comment";
                //Make sure one is comment node
                if (elemType1.equals(commentType) && !elemType2.equals(commentType))
                {
                    fromElem = graphic2;
                    toElem = graphic1;
                }
                else if (!elemType1.equals(commentType) && elemType2.equals(commentType))
                {
                    fromElem = graphic1;
                    toElem = graphic2;
                }
                else
                {
                    proceed = false;
                }
                if (proceed) 
                {
                    drawingAreaCtrl.setModelElement(toElem.getFirstSubject());
                }
            }
        }
        
        if (proceed) 
        {
            ETEdge edge = null;
            
            TSNode fromNode = TypeConversions.getOwnerNode(fromElem);
            TSNode toNode = TypeConversions.getOwnerNode(toElem);
            
            fromNode = (TSNode) preConnectEdge(buttonID, (IETNode)fromNode);
            
            if ( fromNode != null && toNode != null )
            {
                try
                {   
                    // firing event to validate start node
                    TSConnector startConnector = connectStartingEdgeEvent(fromNode,
                                                                          initStr);
                    // create an edge
                    edge = (ETEdge) drawingAreaCtrl.getGraphWindow().getGraph().addEdge(fromNode, toNode);
                    if(startConnector != null)
                    {
                        edge.setSourceConnector(startConnector);
                    }
                    //firing event to validate end node
                    ETPairT < TSConnector, Boolean > value = canConnectEdge(fromNode,
                                                                            toNode,
                                                                            startConnector,
                                                                            initStr);
                    if(value.getParamTwo() == true)
                    {   
                        if (value.getParamOne() != null)
                        {
                            edge.setTargetConnector(value.getParamOne());
                        }
                        // do post connection
                        postConnectEdge(buttonID, (IETNode)fromNode, (IETNode)toNode, (IETEdge)edge);
                    }
                    else
                    {
                        edge.delete();
                    }
                }
                catch(Exception e)
                {
                    edge = null;
                    handled = false;
                }
                
                if (edge != null)
                {
                    handled = true;
                }
            }
        }
        return handled;
    }
    
    /**
     * Connects the edge to the node.  Sends out the onStartingEdgeEvent to
     * determine if the is able to be created.
     *
     * @param pt The location that the user started the edge.
     * @return <code>true</code> if the edge can be created.
     */
    protected TSConnector connectStartingEdgeEvent(TSNode fromNode,
                                                   String initStr)
    {
        TSConnector retVal = null;
        
        
        IAddEdgeEvents dispatcher = getEventDispatcher(initStr);
        if (dispatcher != null)
        {
            ETTripleT < TSConnector, Integer, IETPoint > data = 
                    dispatcher.fireStartingEdgeEvent((IETNode)fromNode, null);
            
            if (data != null)
            {
                Integer canceled = data.getParamTwo();
                if (canceled != null && canceled.longValue() == 0)
                {
                    retVal = data.getParamOne();
                }
            }
        }
        return retVal;
    }

    /**
     * Returns true if we can connect an edge at this logical window point, it 
     * only gets called after the user has clicked for the second time, durning 
     * interactive mouse moves onVerifyMouseMove is used
     */
    public ETPairT < TSConnector, Boolean > canConnectEdge(TSNode fromNode, 
                                                           TSNode toNode,
                                                           TSConnector startConnector,
                                                           String initString)
    {
        ETPairT < TSConnector, Boolean > retVal = new ETPairT < TSConnector, Boolean > ();
        if ((fromNode instanceof TSENode) && 
            (toNode instanceof TSENode))
        {
            TSENode targetNode = (TSENode)toNode;
            TSENode sourceNode = (TSENode)fromNode;
            
            // Now fire the connect Edge Event.
            ETPairT < TSConnector, Integer > retcode = fireFinishEdgeEvent((IETNode) sourceNode, 
                    (IETNode) targetNode,
                    startConnector,
                    initString);
            
            if (retcode != null)
            {
                retVal.setParamOne(retcode.getParamOne());
                retVal.setParamTwo(new Boolean(retcode.getParamTwo().intValue() == 0));
            }
        }
        return retVal;
    }

    private ETPairT < TSConnector, Integer > fireFinishEdgeEvent(IETNode sourceNode, 
                                        IETNode targetNode,
                                        TSConnector startConnector,
                                        String  initStringValue)
    {
        IAddEdgeEvents dispatcher = getEventDispatcher(initStringValue);
        
        return dispatcher.fireFinishEdgeEvent(sourceNode, 
                                              targetNode, 
                                              startConnector, null);
    }
    
    /**
     * Returns the AddEdge Event Dispatcher.
     */
    protected IAddEdgeEvents getEventDispatcher(String initStringValue)
    {
        if (m_eventDispatcher == null)
        {
            IDrawingAreaEventDispatcher dispatcher = drawingAreaCtrl.getDrawingAreaDispatcher();
            IDiagram diagram = drawingAreaCtrl.getDiagram();
            m_eventDispatcher = new AddEdgeEventDispatcher(dispatcher, 
                                                           diagram, 
                                                           initStringValue);
        }
        else
        {
            m_eventDispatcher.setViewDescription(initStringValue);
            m_eventDispatcher.setParentDiagram(drawingAreaCtrl.getDiagram());
        }
        
        return m_eventDispatcher;
    }
    
    private IETNode preConnectEdge(String buttonID, IETNode fromNode)
    {
        IETNode retNode = fromNode;
        if (drawingAreaCtrl != null)
        {
            if ("ID_VIEWNODE_UML_ASSEMBLYCONNECTOR_INITIALEDGE".equals(buttonID))
            {
                TSEGraphWindow pGraphEditor = drawingAreaCtrl.getGraphWindow();
                DiagramAddAssemblyConnectorTool pTool = new DiagramAddAssemblyConnectorTool(pGraphEditor);
                pGraphEditor.switchTool(pTool);
                retNode = pTool.createPort(fromNode);
                drawingAreaCtrl.switchToDefaultState();
            }
        }
        return retNode;
    }
    
    private void postConnectEdge(String buttonID, IETNode sourceNode, IETNode targetNode, IETEdge newEdge)
    {
        if (drawingAreaCtrl != null)
        {
            TSEGraphWindow pGraphEditor = drawingAreaCtrl.getGraphWindow();
            if ("ID_VIEWNODE_UML_ASSOCIATIONCLASS".equals(buttonID))
            {
                ADAddAssociationClassEdgeTool pTool = new ADAddAssociationClassEdgeTool(pGraphEditor);
                pTool.setParentDiagram(drawingAreaCtrl.getDiagram());
                pTool.setCreateBends(false);
                pGraphEditor.switchTool(pTool);
                pTool.postConnectEdge(newEdge);
                drawingAreaCtrl.switchToDefaultState();
                return;
            }
            else if ("ID_VIEWNODE_UML_PARTFACADELINK".equals(buttonID))
            {   
                DiagramAddPartFacadeTool pTool = new DiagramAddPartFacadeTool(pGraphEditor);
                pGraphEditor.switchTool(pTool);
                IPartFacade pPartFacade = pTool.getPartFacadeElement(targetNode, sourceNode);
                drawingAreaCtrl.setModelElement(pPartFacade);
                notifyDrawingAreaObjCreated((TSEEdge)newEdge);
                onPostDrawingAreaNotifyedObjCreated((TSEEdge)newEdge);
                pTool.doAnnotation(targetNode, sourceNode, newEdge);
                drawingAreaCtrl.setModelElement(null);
                drawingAreaCtrl.switchToDefaultState();
                return;
            }
            
            notifyDrawingAreaObjCreated((TSEEdge)newEdge);
            onPostDrawingAreaNotifyedObjCreated((TSEEdge)newEdge);
        }
    }
    
    
    protected void notifyDrawingAreaObjCreated(TSEdge newEdge)
    {
        if(newEdge instanceof TSEEdge)
        {
            TSEEdge edge = (TSEEdge)newEdge;
            TSEEdgeUI edgeUI = edge.getEdgeUI(); 

            if (edgeUI instanceof ETGenericEdgeUI)
            {
                drawingAreaCtrl.onInteractiveObjCreated(edgeUI);
                return;
            }
            else
            {
                try
                {
                    ADGraphWindow wnd = drawingAreaCtrl.getGraphWindow();
                    ETGenericEdgeUI ui = (ETGenericEdgeUI) wnd.getCurrentEdgeUI();
                    ETGenericEdgeUI newUI = ETUIFactory.createEdgeUI(ui.getClass().getName(), 
                                                                     ui.getInitStringValue(), 
                                                                     ui.getDrawEngine().getClass().getName(), 
                                                                     ui.getDrawingArea());

                    newUI.setDrawEngineClass(ui.getDrawEngine().getClass().getName());
                    edge.setUI(newUI);
                    
                    drawingAreaCtrl.onInteractiveObjCreated(newUI);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Make sure the only object selected it the new edge just created.
     * It gets called after the drawing area has been notified that an edge has
     * been created.
     */
    protected void onPostDrawingAreaNotifyedObjCreated(TSEdge newEdge)
    {
        ADGraphWindow diagramWindow = drawingAreaCtrl.getGraphWindow();
        if (diagramWindow != null && newEdge instanceof TSEEdge)
        {
            TSEEdge edge = (TSEEdge)newEdge;
            
            diagramWindow.deselectAll(false);
            edge.setSelected(true);
            
            // Repaint the window.
            diagramWindow.drawGraph();
            diagramWindow.fastRepaint();
        }
        
        drawingAreaCtrl.setModelElement(null);
    }	

    private String redefineButtonID(String buttonID)
    {
        String redefinedBtnId = buttonID;
        
        if (buttonID != null && buttonID.trim().length() > 0 )
        {
            if ("ID_VIEWNODE_UML_INTERFACE".equals(buttonID) )
            {
                // for now, we only create interface as class only, not as a lollipop
                redefinedBtnId = "ID_VIEWNODE_UML_INTERFACE_AS_CLASS";
            }
            else if ("ID_VIEWNODE_UML_COMMENT".equals(buttonID) )
            {
                // for now, we only create comment as a comment node, not as a comment link
                redefinedBtnId = "ID_VIEWNODE_UML_COMMENTNODE";
            }
        }
        return redefinedBtnId;
    } 
}