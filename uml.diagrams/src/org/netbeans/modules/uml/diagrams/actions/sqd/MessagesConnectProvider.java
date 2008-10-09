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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.diagrams.anchors.CreateMessageTargetAnchor;
import org.netbeans.modules.uml.diagrams.edges.factories.MessageFactory;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.engines.SequenceDiagramEngine;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.ExecutionSpecificationThinWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.InteractionOperandWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineBoxWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineLineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.SQDMessageConnectProvider;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;


/**
 *
 * @author psb
 */
public class MessagesConnectProvider implements SQDMessageConnectProvider
{

    private String edgeType;
    private String defaultTargetType;
    private int edgeKind;
    private RelationValidator validator = new RelationValidator();
    private RelationshipFactory factory = null;
    private IMessage message;
    private IMessage resultMessage;


    /** 
     * Creates a new instance of SceneConnectProvider.  The provider will create
     * an edge of type edgeType.  If the user drops the edge on the diagram as
     * opposed to a node, the targetType will be used to create a new node.  
     * 
     * @param edgeType the type of edge to create.
     * @param targetType the type of node to create if the user releases over 
     *                   the diagram.
     */
    public MessagesConnectProvider(String connectionType, int connectionKind, String defaultTargetType) {
        this.edgeType = connectionType;
        this.defaultTargetType = defaultTargetType;
        this.edgeKind=connectionKind;
        message=null;
        resultMessage=null;
    }

    /**
     * constructor used to draw messages for existent model elemnts
     * corrspodning conect method will not create new elements but will use specified in constructor elemnt
     * presentation elements will be created
     * @param message
     */
    public MessagesConnectProvider(IMessage message)
    {
        this(message,null);
    }
    
    public MessagesConnectProvider(IMessage call, IMessage result) {
        this.message=call;
        resultMessage=result;
        edgeKind=message.getKind();
    }
    
    public boolean isSourceWidget(Widget sourceWidget)
    {
        return sourceWidget instanceof UMLNodeWidget;
    }
    
    
    public boolean isSourceWidget(Widget sourceWidget, Point sourcePoint) {
        boolean ret=isSourceWidget(sourceWidget);
        if(ret && sourceWidget instanceof LifelineWidget)
        {
                LifelineLineWidget sourceLine=((LifelineWidget)sourceWidget).getLine();
                int y1=sourceLine.convertSceneToLocal(sourcePoint).y;//incoming corrdinates in LifelineWidget coordinates, we adds child to LifelineLineWidget
                if(y1<0 || y1>sourceLine.getBounds().height)
                {
                    //outside of line
                    ret=false;
                }
                else
                {
                    ExecutionSpecificationThinWidget sourcePreexistentSpec=getExSpecification(sourceLine, y1);
                    ExSpecData sourcePair=null;

                    if(sourcePreexistentSpec!=null)
                    {
                        int ys=sourcePreexistentSpec.convertSceneToLocal(sourcePoint).y;
                        sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys);
                        //first can't draw any message within source of synch message (lifeline wait returns)
                        //and also can't create/call multiple mthods at one atomic time
                        //TBD add support for multiple calls from one ex specification 
                        //TBD do not check in isTarget
                        if(sourcePair.getFirstWidget() instanceof MessagePinWidget)
                        {
                            MessagePinWidget.PINKIND firstKind=((MessagePinWidget)sourcePair.getFirstWidget()).getKind();
                            MessagePinWidget.PINKIND prevKind=firstKind;//by default if it above the very first pin, consider the same as first
                            if(sourcePair.getPrevWidget()!=null && sourcePair.getPrevWidget() instanceof MessagePinWidget)prevKind=((MessagePinWidget)sourcePair.getPrevWidget()).getKind();
                            if(firstKind==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT || prevKind==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT || firstKind==MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_OUT || firstKind==MessagePinWidget.PINKIND.CREATE_CALL_OUT)
                            {
                                //can't create from within of synchronous message source
                                //prohobite also other creations from source for now
                                ret= false;
                            }
                        }
                    }
                }
        }
        return ret;
    }

    /**
     * check target widget and consider target ppoint (checks if possible to draw message to this point)
     * @param sourceWidget - message from this widget
     * @param targetWidget - message to this widget
     * @param sourcePoint - message from this point on source point in scene coordinates
     * @param targetPoint - message to this point on target widget in scene coordinates, in general isn't used much and may be the same as source often
     * @return accept if connection is possible
     */
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget,Point sourcePoint,Point targetPoint)
    {   
        ConnectorState retVal = ConnectorState.REJECT;
        
        if(targetWidget instanceof InteractionOperandWidget)return retVal;
        
        ObjectScene scene = (ObjectScene)sourceWidget.getScene();
        // Verify that this relationship is ok
        RelationProxy relationshipProxy = new RelationProxy();
        IPresentationElement source = (IPresentationElement) scene.findObject(sourceWidget);
        IPresentationElement target = (IPresentationElement) scene.findObject(targetWidget);
        
        if(target != null)
        {
            if(scene.findWidget(target) != targetWidget)
            {
                target = null;
            }
        }


        //check if target is of valid type, it may be easier for now to check only comment element(invalid) but in case of more elements later 
        //I use check for valid elements
        if((source != null) && (target != null) && ((target.getFirstSubject() instanceof ILifeline) || (target.getFirstSubject() instanceof IInteraction) || (target.getFirstSubject() instanceof ICombinedFragment)))
        {
            relationshipProxy.setFrom(source.getFirstSubject());
            relationshipProxy.setTo(target.getFirstSubject());
            String connectorType="MessageConnector";//TBD, remove hardcoded type(if this provider will be used not for messages only)
            relationshipProxy.setConnectionElementType(connectorType);

            // Verify the relation
            validator.validateRelation(relationshipProxy);

            if(relationshipProxy.getRelationValidated() == true)
            {
                retVal = ConnectorState.ACCEPT;
            }
        }
        
        if(retVal==ConnectorState.ACCEPT)
        {
            if(targetWidget instanceof CombinedFragmentWidget)
            {
                Rectangle bnd=targetWidget.getBounds();
                Point locPnt=targetWidget.convertSceneToLocal(targetPoint);
                if(locPnt.x>(bnd.x+30) && locPnt.x<(bnd.x+bnd.width-30))retVal=ConnectorState.REJECT;
                else if(target==source)retVal=ConnectorState.REJECT;//disable message to self on combined fragments
            }
            if(retVal==ConnectorState.ACCEPT)
            {
                //additional checks is required sometimes
                if(edgeKind==BaseElement.MK_CREATE)
                {
                    targetWidget=scene.findWidget(target);
                    if(targetWidget instanceof LifelineWidget)
                    {
                        LifelineWidget tmp=(LifelineWidget) targetWidget;
                        if(tmp.isCreated() || targetWidget==sourceWidget)
                        {
                            //can't draw second create messsage or create message to self
                            retVal=ConnectorState.REJECT;
                        }
                        else
                        {
                            //TODO how to check if there any messages which prevent creation, should it be as in previous release when any message block creation?
                            Lifeline tmpE=(Lifeline) target.getFirstSubject();
                            if(tmpE.getEvents().size()>1)retVal=ConnectorState.REJECT;//TBD, not perfect, need separate check if 1 event is destroy or receive of asycnh message
                        }
                    }
                    else if(targetWidget instanceof Scene)
                    {
                        //all good
                    }
                    else
                    {
                        //default, for example can't create to combined fragment
                        retVal=ConnectorState.REJECT;
                    }
                }
                if(retVal==ConnectorState.ACCEPT)
                {
                    //checks existent position if still accept, case for lifeline-lifeline message
                    if(source!=null)sourceWidget=scene.findWidget(source);
                    if(target!=null)targetWidget=scene.findWidget(target);
                    if(retVal==ConnectorState.ACCEPT && sourceWidget instanceof LifelineWidget && targetWidget instanceof LifelineWidget)
                    {
                        LifelineLineWidget sourceLine=((LifelineWidget)sourceWidget).getLine();
                        LifelineLineWidget targetLine=((LifelineWidget)targetWidget).getLine();
                        int y1=sourceLine.convertSceneToLocal(sourcePoint).y;//incoming corrdinates in LifelineWidget coordinates, we adds child to LifelineLineWidget
                        int y2=targetLine.convertSceneToLocal(sourcePoint).y;//

                        ExecutionSpecificationThinWidget sourcePreexistentSpec=getExSpecification(sourceLine, y1);
                        ExecutionSpecificationThinWidget targetPreexistentSpec=getExSpecification(targetLine, y2);
                        ExSpecData sourcePair=null,targetPair=null;

                        if(sourcePreexistentSpec!=null)
                        {
                            int ys=sourcePreexistentSpec.convertSceneToLocal(sourcePoint).y;
                            sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys);
                        }
                        if(targetPreexistentSpec!=null)
                        {
                            int yt=targetPreexistentSpec.convertSceneToLocal(sourcePoint).y;
                            targetPair=getWidgetsAroundPoint(targetPreexistentSpec, yt);
                        }
                        //now filter out all unsupported cases (TBD do not check for them in creation later)

                    }
                }
            }
        }
       
        return retVal;
    }

    public boolean hasCustomTargetWidgetResolver(Scene scene)
    {
        return false;
    }

    public Widget resolveTargetWidget(Scene scene, Point sceneLocation)
    {
        return null;
    }
    
    
    /**
     * Method accept source/target widgets(Lifelines/Combined Fragments) and sraw message of kind specified during creation of connect provider
     * If kind of message synchronous or result both links call and result are created
     * if you need to adjust result message position after creation (point is used for call messages positioning) you have to adjust source and target related widgets positions for result
     * @param sourceWidget from widget
     * @param targetWidget to widget
     * @param startingPoint starting point in scene coordinates
     * @param finishPoint finish point in scene coordinated
     * @return ArrayList with all created connectionwidgets
     */
    @SuppressWarnings(value = "unchecked")
    public ArrayList<ConnectionWidget> createConnection(Widget sourceWidget, Widget targetWidget,Point startingPoint,Point finishPoint)
    {
        return handleCreateConnection(sourceWidget, targetWidget, startingPoint, finishPoint, null, null);
    }
    
    public ArrayList<ConnectionWidget> createSynchConnection(Widget sourceWidget, Widget targetWidget, Point callStartingPoint, Point callFinishPoint, Point resultStartingPoint, Point resultFinishPoint) {
        return handleCreateConnection(sourceWidget, targetWidget, callStartingPoint, callFinishPoint, resultStartingPoint, resultFinishPoint);
    }
    
    private ArrayList<ConnectionWidget> handleCreateConnection(Widget sourceWidget, Widget targetWidget, Point callStartingPoint, Point callFinishPoint, Point resultStartingPoint, Point resultFinishPoint)
    {
         ArrayList ret=new ArrayList();
        if((validator != null) && (sourceWidget.getScene() instanceof GraphScene))
        {
            GraphScene scene = (GraphScene)sourceWidget.getScene();
            IPresentationElement sourceElement = getElement(sourceWidget);
            IPresentationElement targetElement = getElement(targetWidget);
            IElement sourceNE=sourceElement.getFirstSubject();
            IElement targetNE=targetElement.getFirstSubject();

            IMessage call=null,result=null;
            if(message!=null)//put existent model message to sqd
            {
                if(edgeKind==BaseElement.MK_RESULT)
                {
                    result=message;
                    //find also call
                    call=result.getSendingMessage();
                    //TBD: verify if call was already created and throw smth?
                }
                else if (edgeKind==BaseElement.MK_SYNCHRONOUS)
                {
                    //throw new UnsupportedOperationException("draw of existent synch message is supported by use of result message only, do not use call message.");
                    call=message;
                    if(resultMessage!=null)
                    {
                        result=resultMessage;
                    }
                    else
                    {
                        IInteraction interaction=message.getInteraction();
                        ETList<IMessage> messages=interaction.getMessages();
                        //
                        for(IMessage msg:messages)
                        {
                            if(msg==call)
                            {
                                //additional verification, find foirst call to be sure result goes after call
                                result=call;
                            }
                            if(result==call)
                            {
                                //only if call found
                                if(msg.getSendingMessage()==result)
                                {
                                    result=msg;
                                    break;
                                }
                            }
                        }
                    }
                    //verify result was found
                    if(result==null || result==call)
                    {
                        throw new RuntimeException("Result message wasn't found within interaction after call message");
                    }
                }
                else
                {
                    call=message;
                }
                //also if it's existent message it is expected to get starting and finishingPoint as message location, not starting/finishing osint for creation which need to take it may not be posible to bump all up and will create ex specification starting from posin
                int fix=new MessagePinWidget(sourceWidget.getScene(), MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_OUT).getMarginBefore();
                callStartingPoint.y-=fix;//all call messages starts with the same shift after "creation point"
                callFinishPoint.y-=fix;
            }
            
            if(getRelationshipFactory() != null || message!=null)
            {
                IElement rel = null;
                if(message==null)rel=getRelationshipFactory().create(sourceElement.getFirstSubject(), 
                                                                        targetElement.getFirstSubject());
                if((getRelationshipFactory() instanceof MessageFactory) || message!=null)
                {
                    
                    IPresentationElement msgPE1=null,msgPE2=null;
                    MessageFactory mf=(MessageFactory) getRelationshipFactory();
                    IMessage nextMessage=null;
                    if(message==null)
                    {
                        //need to find next message if new one need to be created only
                        IInteraction fromOwner=(IInteraction) OwnerRetriever.getOwnerByType(sourceNE,IInteraction.class);
                        if(fromOwner==null && sourceNE instanceof IInteraction)fromOwner=(IInteraction) sourceNE;
                        nextMessage=mf.getMessageNextToPoint(scene,fromOwner, callStartingPoint.y);
                    }
                    IMessage message1=call;
                    if(mf!=null && message1==null)
                    {
                        message1=mf.createMessage(sourceNE,targetNE,edgeKind,nextMessage);
                    }
                    if(rel!=null)((IMessageConnector) rel).addMessage(message1);//TBD what for connector is created? if it possible to live without
                    UMLEdgeWidget msgW1=null;
                        msgPE1 = createPresentationElement(message1);
                        msgW1=(UMLEdgeWidget) scene.addEdge(msgPE1);
                        ret.add(msgW1);
                        scene.setEdgeSource(msgPE1, sourceElement);
                        scene.setEdgeTarget(msgPE1, targetElement);
                    //
                    if(edgeKind==BaseElement.MK_CREATE)
                    {
                        createCreateMessage(sourceWidget,(LifelineWidget)targetWidget,msgW1,callStartingPoint);
                    }
                    else
                    {
                        if(edgeKind==BaseElement.MK_SYNCHRONOUS)
                        {
                            //((IMessageConnector) rel)
                            IMessage message2=null;
                            UMLEdgeWidget msgW2=null;
                            if(result==null)
                            {
                                //create only if new
                                message2=mf.createMessage(targetNE,sourceNE,BaseElement.MK_RESULT,nextMessage);
                                message2.setSendingMessage(message1);
                                if(rel!=null)((IMessageConnector) rel).addMessage(message2);
                            }
                            else
                            {
                                message2=result;
                            }
                            msgPE2 = createPresentationElement(message2);
                            //
                            msgW2=(UMLEdgeWidget) scene.addEdge(msgPE2);
                            ret.add(msgW2);
                            scene.setEdgeSource(msgPE2, targetElement);
                            scene.setEdgeTarget(msgPE2, sourceElement);
                                     //
                            createSynchMessage(sourceWidget,targetWidget,msgW1,msgW2,callStartingPoint,callFinishPoint,resultStartingPoint,resultFinishPoint);
                        }
                        else if(edgeKind==BaseElement.MK_RESULT && message!=null)
                        {
                            //separate creation of result link is supported for existent messages only
                            IMessage message2=result;
                            msgPE2 = createPresentationElement(message2);
                            UMLEdgeWidget msgW2=(UMLEdgeWidget) scene.addEdge(msgPE2);
                            ret.add(msgW2);
                            scene.setEdgeSource(msgPE2, targetElement);
                            scene.setEdgeTarget(msgPE2, sourceElement);
                            createSynchMessage(targetWidget,sourceWidget,msgW1,msgW2,callStartingPoint,callFinishPoint,resultStartingPoint,resultFinishPoint);
                       }
                        else if(edgeKind==BaseElement.MK_ASYNCHRONOUS)
                        {
                             createASynchMessage(sourceWidget,targetWidget,msgW1,callStartingPoint,callFinishPoint);
                        }
                    }
                }
           }
        }
        return ret;       
    }

    public boolean hasTargetWidgetCreator()
    {
        boolean retVal = false;
        
        if((defaultTargetType != null) && (defaultTargetType.length() > 0))
        {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Create a new node widget to be used as the target widget for the connector.
     * This provider expects the scene to be a graph scene.
     * 
     * @param targetScene The scene that will contain the node.  The scene must be a
     *                    GraphScene.
     * @return The new node that was created.
     */
    @SuppressWarnings("unchecked")
    public Widget createTargetWidget(Scene targetScene, Point sourcePoint)
    {
        Widget retVal = null;
        
        if(targetScene instanceof DesignerScene)
        {
            DesignerScene scene = (DesignerScene)targetScene;
            Object value = FactoryRetriever.instance().createType(defaultTargetType, 
                                                                   null);

            if(value instanceof Lifeline)
            {
                Lifeline namedElement = (Lifeline)value;
                IPresentationElement element = createPresentationElement(namedElement);
                element.addSubject(namedElement);
                //retVal = scene.addNode(element);
                retVal = (scene).getEngine().addWidget(element, sourcePoint);
                IDiagram diagram = scene.getDiagram();
                if(diagram != null)
                {
                    INamespace space = diagram.getNamespaceForCreatedElements();
                    space.addOwnedElement(namedElement);
                }
            }
        }
        
        return retVal;
    }

    public RelationshipFactory getRelationshipFactory()
    {
        return factory;
    }

    public void setRelationshipFactory(RelationshipFactory factory)
    {
        this.factory = factory;
    }


    
    private IPresentationElement createPresentationElement(INamedElement element)
    {
        IPresentationElement retVal = null;
        
        ICreationFactory creationFactory = FactoryRetriever.instance().getCreationFactory();
        if(creationFactory != null)
        {
           Object presentationObj = creationFactory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;   
                  element.addPresentationElement(retVal);
           }
        }
        
        return retVal;
    }
    
    private IPresentationElement getElement(Widget widget)
    {
        IPresentationElement retVal = null;
        
        Scene widgetScene = widget.getScene();
        if (widgetScene instanceof ObjectScene)
        {
            ObjectScene objScene = (ObjectScene)widgetScene;
            Object value = objScene.findObject(widget);
            if (value instanceof IPresentationElement)
            {
                retVal = (IPresentationElement)value;
            }
        }
        
        return retVal;
    }
    
    private IPresentationElement createPresentationElement(IRelationship element)
    {
        IPresentationElement retVal = null;
        
        ICreationFactory creationFactory = FactoryRetriever.instance().getCreationFactory();
        if(creationFactory != null)
        {
           Object presentationObj = creationFactory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;    
                  element.addPresentationElement(retVal);
           }
        }
        
        return retVal;
    }

    
    /**
     * TBD, should it be separated for different cases to simplify logic
     * @param source
     * @param target
     * @param call
     * @param result
     * @param y
     * @return success state
     */
    private boolean createSynchMessage(Widget source, Widget target,UMLEdgeWidget call,UMLEdgeWidget result,Point startingPoint,Point finishPoint,Point resultStartingPoint,Point resultFinishPoint) {
            LifelineWidget sourceLifeline=null,targetLifeline=null;
            CombinedFragmentWidget sourceCF=null,targetCF=null;
            if(source instanceof LifelineWidget)sourceLifeline=(LifelineWidget) source;
            else sourceCF=(CombinedFragmentWidget) source;//do not check type here because any other type is unexpected
            if(target instanceof LifelineWidget)targetLifeline=(LifelineWidget) target;
            else targetCF=(CombinedFragmentWidget) target;//do not check type here because any other type is unexpected
        
            LifelineLineWidget sourceLine=null;
            if(sourceLifeline!=null)sourceLine=sourceLifeline.getLine();
            LifelineLineWidget targetLine=null;
            if(targetLifeline!=null)targetLine=targetLifeline.getLine();
            //
            Point scenePoint=startingPoint;
            int y1=0;
            if(sourceLine!=null)y1=convertSceneToLocal(sourceLine,scenePoint).y;//incoming corrdinates in LifelineWidget coordinates, we adds child to LifelineLineWidget
            else y1=sourceCF.getMainWidget().convertSceneToLocal(scenePoint).y;
            int y2=0;
            if(targetLine!=null)y2=convertSceneToLocal(targetLine,scenePoint).y;//
            else y2=targetCF.getMainWidget().convertSceneToLocal(scenePoint).y;
            
            GraphScene scene=(GraphScene) source.getScene();
           
            ExecutionSpecificationThinWidget sourcePreexistentSpec=getExSpecification(sourceLine, y1);
            ExSpecData sourcePair=null,targetPair=null;
            //
               boolean sourceNested=sourcePreexistentSpec!=null;
            if(sourceNested)
            {
                int ys=convertSceneToLocal(sourcePreexistentSpec,scenePoint).y;
                sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys);
                //correction if got position inside top margine
                if(sourcePair.getPrevWidget()==null)
                {
                    scenePoint.y+=10;
                    y2+=10;
                    sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys+10);
                }
                //
                y1=convertSceneToLocal(sourcePreexistentSpec,scenePoint).y;
            }
            ExecutionSpecificationThinWidget targetPreexistentSpec=getExSpecification(targetLine, y2+new MessagePinWidget(scene, MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT).getMarginBefore()-new MessagePinWidget(scene, MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN).getMarginBefore());//usually it mean shift for 10 px because target do not have margin before);
            boolean targetNested=targetPreexistentSpec!=null;
            if(targetNested)
            {
                int yt=convertSceneToLocal(targetPreexistentSpec,scenePoint).y+new MessagePinWidget(scene, MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT).getMarginBefore()-new MessagePinWidget(scene, MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN).getMarginBefore();//usually it mean shift for 10 px because target do not have margin before
                targetPair=getWidgetsAroundPoint(targetPreexistentSpec, yt);
                //correction if got position inside top margine
                if(targetPair.getPrevWidget()==null)
                {
                    scenePoint.y+=10;
                    y1+=10;
                    targetPair=getWidgetsAroundPoint(targetPreexistentSpec, yt+10);
                }
                y2=convertSceneToLocal(targetPreexistentSpec,scenePoint).y;
            }
            
            //
            ExecutionSpecificationThinWidget sourceExWidget=null,targetExWidget=null;
            MessagePinWidget sourceCallPin=null;
            MessagePinWidget targetCallPin=null;
                sourceCallPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT);
                targetCallPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN);
            MessagePinWidget sourceReturnPin=null;new MessagePinWidget(scene,MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_OUT);
            MessagePinWidget targetReturnPin=null;new MessagePinWidget(scene,MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_IN);
                sourceReturnPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_OUT);
                targetReturnPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_IN);
                
            
                handleFreeSpaceExpantion(scenePoint,source,sourceCallPin.getKind(),targetLine==sourceLine && sourceLine!=null,sourcePreexistentSpec,sourcePair);
                scene.validate();
                handleFreeSpaceExpantion(scenePoint,target,targetCallPin.getKind(),targetLine==sourceLine && sourceLine!=null,targetPreexistentSpec,targetPair);
                scene.validate();
            
            if(sourceLine==null)//combined fragment case
            {
                call.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceCallPin));
                result.setTargetAnchor(AnchorFactory.createCenterAnchor(targetReturnPin));
            }
            else if(!sourceNested)//from clear line
            {
                sourceExWidget=new ExecutionSpecificationThinWidget(scene);
                sourceExWidget.setPreferredLocation(new Point(0,0));
                sourceCallPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()));
                targetReturnPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()+30));
                call.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceCallPin,sourceExWidget.width/2));
                result.setTargetAnchor(AnchorFactory.createCircularAnchor(targetReturnPin,sourceExWidget.width/2));
            }
            else// from within existed spec
            {
                //from existent use existent execution specification
                sourceCallPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()));
                targetReturnPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()+30));
                call.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceCallPin,sourcePreexistentSpec.width/2));
                result.setTargetAnchor(AnchorFactory.createCircularAnchor(targetReturnPin,sourcePreexistentSpec.width/2));
            }
            
            if(targetLine==null)//combined fragment
            {
                result.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceReturnPin));
                call.setTargetAnchor(AnchorFactory.createCenterAnchor(targetCallPin));
            }
            else if(!targetNested)//to clear line
            {
                targetExWidget=new ExecutionSpecificationThinWidget(scene);
                targetExWidget.setPreferredLocation(new Point(0,0));//y2+10));
                targetCallPin.setPreferredLocation(new Point(0,y2+sourceCallPin.getMarginBefore()));
                //return message/hardcoded position for now
                sourceReturnPin.setPreferredLocation(new Point(0,y2+sourceCallPin.getMarginBefore()+30));
                result.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceReturnPin,targetExWidget.width/2));
                call.setTargetAnchor(AnchorFactory.createCircularAnchor(targetCallPin,targetExWidget.width/2));
            }
            else//to existend specification
            {
                //to existent need to create target execution specification
                targetExWidget=new ExecutionSpecificationThinWidget(scene);
                targetExWidget.setPreferredLocation(new Point(0,0));//y2+10));
                targetCallPin.setPreferredLocation(new Point(0,y2+sourceCallPin.getMarginBefore()));
                //return message/hardcoded position for now
                sourceReturnPin.setPreferredLocation(new Point(0,y2+sourceCallPin.getMarginBefore()+30));
                result.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceReturnPin,targetExWidget.width/2));
                call.setTargetAnchor(AnchorFactory.createCircularAnchor(targetCallPin,targetExWidget.width/2));
            }
            
            //actions
            //seems reasonable to remove control button after drawing
            //add actions to execution specifications
            //if(sourceExWidget!=null)sourceExWidget.createActions(DesignerTools.SELECT).addAction(ActionFactory.createMoveAction(new ThinExecutionSpecificationMoveStrategy(),new ThinExecutionSpecificationMoveProvider()));
            //if(targetExWidget!=null)targetExWidget.createActions(DesignerTools.SELECT).addAction(ActionFactory.createMoveAction(new ThinExecutionSpecificationMoveStrategy(),new ThinExecutionSpecificationMoveProvider()));
            //--
            //revalidate with
            if(sourceNested)
            {
                Widget prevW=sourcePair.getPrevWidget();
                int index=sourcePreexistentSpec.getChildren().indexOf(prevW);
                sourcePreexistentSpec.addChild(index+1,sourceCallPin);
                sourcePreexistentSpec.addChild(index+2,targetReturnPin);
            }
            else if(sourceLine!=null)
            {
                sourceExWidget.addChild(sourceCallPin);//this is new pins, so no need to find indexes
                sourceExWidget.addChild(targetReturnPin);
                //but need to find index for execution specifictions
                //usual development from top to bottom, so start from bottom, later half way may b implemented for search
                int index=findPrevPinOrESpecIndex(sourceLine, y1);
                //if(index>=(sourceLine.getChildren().size()-1))sourceLine.addChild(sourceExWidget);
                //else
                {
                    sourceLine.addChild(index+1,sourceExWidget);
                }
            }
            else
            {
                //from combinedfragment, do not order for now (at least before implementation of separate containers for left and right messages)
                Point start=sourceCF.getMainWidget().convertSceneToLocal(scenePoint);
                start.x=(start.x<(sourceCF.getMainWidget().getBounds().x+sourceCF.getMainWidget().getBounds().width/2)) ? (0) : (sourceCF.getMainWidget().getBounds().width);
                sourceCallPin.setPreferredLocation(new Point(start.x,start.y+10));
                targetReturnPin.setPreferredLocation(new Point(start.x,start.y+40));//TBD, hardcoded
                sourceCF.getMainWidget().addChild(sourceCallPin);
                sourceCF.getMainWidget().addChild(targetReturnPin);
            }
            //
            if(targetLine==sourceLine && sourceLine!=null)
            {
                //message to self
                 if(sourceNested)
                {
                   //sourceCallPin is before target ex spec
                    int index=sourcePreexistentSpec.getChildren().indexOf(sourceCallPin);
                    sourcePreexistentSpec.addChild(index+1,targetExWidget);
                }
                else
                {
                    //source is new and before only call pin-> index=1
                    sourceExWidget.addChild(1, targetExWidget);
                }
                targetExWidget.setPreferredLocation(new Point(7,0));
                targetCallPin.setPreferredLocation(new Point(0,y2+20));
                targetReturnPin.setPreferredLocation(new Point(0,y1+80));
                sourceReturnPin.setPreferredLocation(new Point(0,y2+70));
                //target spec for message to self isalways new, so no need to find index, but need to add in proper order
                targetExWidget.addChild(targetCallPin);
                targetExWidget.addChild(sourceReturnPin);
                //
                call.setRouter(new SelfMessageRouter());
                result.setRouter(new SelfMessageRouter());
            }
            else if(targetNested)
            {
                //no need to find indexes for pins, becaise new ex spec is crea6ted, keep order
                targetExWidget.addChild(targetCallPin);
                targetExWidget.addChild(sourceReturnPin);
                //for ex spec need to find index
                int index=targetPreexistentSpec.getChildren().indexOf(targetPair.getPrevWidget());
                targetPreexistentSpec.addChild(index+1, targetExWidget);
            }
            else if(targetLine!=null)
            {
                //no need to find indexes for pins, becaise new ex spec is crea6ted, keep order
                targetExWidget.addChild(targetCallPin);
                targetExWidget.addChild(sourceReturnPin);
                //need to find prev on target line
                int index=findPrevPinOrESpecIndex(targetLine, y2+sourceCallPin.getMarginBefore()-targetCallPin.getMarginBefore());
                targetLine.addChild(index+1,targetExWidget);
            }
            else
            {
                //to combined fragment
                targetCF.getMainWidget().addChild(targetCallPin);
                targetCF.getMainWidget().addChild(sourceReturnPin);
                Point finish=convertSceneToLocal(targetCF.getMainWidget(),finishPoint);
                Point start=targetCF.getMainWidget().convertSceneToLocal(scenePoint);
                finish.x=(finish.x<targetCF.getMainWidget().getBounds().width/2) ? (0) : (targetCF.getMainWidget().getBounds().width);
                targetCallPin.setPreferredLocation(new Point(finish.x,start.y+sourceCallPin.getMarginBefore()));//margin shift is get from source pin, the same as y position from staring point
                sourceReturnPin.setPreferredLocation(new Point(finish.x,start.y+sourceCallPin.getMarginBefore()+30));//TBD, hardcoded 40
            }
            //if result points are specified correct calculated points to specified
            if(resultFinishPoint!=null)
            {
                Point toSet=convertSceneToLocal(targetReturnPin.getParentWidget(),resultFinishPoint);
                //specified point shuld affect only y position, x should be the same as derived from call locations,
                //so we don't use x
                //from parameter.
                toSet.x=targetReturnPin.getPreferredLocation().x;
                targetReturnPin.setPreferredLocation(toSet);
            }
            if(resultStartingPoint!=null)
            {
                Point toSet=convertSceneToLocal(sourceReturnPin.getParentWidget(),resultStartingPoint);
                //specified point shuld affect only y position, x should be the same as derived from call locations,
                //so we don't use x
                //from parameter.
                toSet.x=sourceReturnPin.getPreferredLocation().x;
                sourceReturnPin.setPreferredLocation(toSet);
            }
            return true;
    }
    //TBD check if all methods can be easily replaced with one
    private void createASynchMessage(Widget source, Widget target, UMLEdgeWidget call,Point startingPoint,Point finishPoint) {
            LifelineWidget sourceLifeline=null,targetLifeline=null;
            CombinedFragmentWidget sourceCF=null,targetCF=null;
            if(source instanceof LifelineWidget)sourceLifeline=(LifelineWidget) source;
            else sourceCF=(CombinedFragmentWidget) source;//do not check type here because any other type is unexpected
            if(target instanceof LifelineWidget)targetLifeline=(LifelineWidget) target;
            else targetCF=(CombinedFragmentWidget) target;//do not check type here because any other type is unexpected
        
            LifelineLineWidget sourceLine=null;
            if(sourceLifeline!=null)sourceLine=sourceLifeline.getLine();
            LifelineLineWidget targetLine=null;
            if(targetLifeline!=null)targetLine=targetLifeline.getLine();
            Point scenePoint=startingPoint;
            int y1=0;
            if(sourceLine!=null)y1=convertSceneToLocal(sourceLine,scenePoint).y;//incoming corrdinates in LifelineWidget coordinates, we adds child to LifelineLineWidget
            else y1=sourceCF.getMainWidget().convertSceneToLocal(scenePoint).y;
            int y2=0;
            if(targetLine!=null)y2=convertSceneToLocal(targetLine,scenePoint).y;
            else y2=targetCF.getMainWidget().convertSceneToLocal(scenePoint).y;
            
            GraphScene scene=(GraphScene) source.getScene();
            ExecutionSpecificationThinWidget sourcePreexistentSpec=getExSpecification(sourceLine, y1);
            ExSpecData sourcePair=null,targetPair=null;
            
            
            boolean sourceNested=sourcePreexistentSpec!=null;
            if(sourceNested)
            {
                int ys=convertSceneToLocal(sourcePreexistentSpec,scenePoint).y;
                sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys);
                //correction if got position inside top margine
                if(sourcePair.getPrevWidget()==null)
                {
                    scenePoint.y+=10;
                    y2+=10;
                    sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys+10);
                }
                //
                y1=convertSceneToLocal(sourcePreexistentSpec,scenePoint).y;
            }
            ExecutionSpecificationThinWidget targetPreexistentSpec=getExSpecification(targetLine, y2+new MessagePinWidget(scene, MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_OUT).getMarginBefore()-new MessagePinWidget(scene, MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_IN).getMarginBefore());
            boolean targetNested=targetPreexistentSpec!=null;
            if(targetNested)
            {
                int yt=convertSceneToLocal(targetPreexistentSpec,scenePoint).y+new MessagePinWidget(scene, MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_OUT).getMarginBefore()-new MessagePinWidget(scene, MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_IN).getMarginBefore();
                targetPair=getWidgetsAroundPoint(targetPreexistentSpec, yt);
                //correction if got position inside top margine
                if(targetPair.getPrevWidget()==null)
                {
                    scenePoint.y+=10;
                    y1+=10;
                    targetPair=getWidgetsAroundPoint(targetPreexistentSpec, yt+10);
                }
                y2=convertSceneToLocal(targetPreexistentSpec,scenePoint).y;
            }

            
            ExecutionSpecificationThinWidget sourceExWidget=null,targetExWidget=null;
            MessagePinWidget sourceCallPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_OUT);
            MessagePinWidget targetCallPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_IN);

            handleFreeSpaceExpantion(scenePoint,source,sourceCallPin.getKind(),targetLine==sourceLine && sourceLine!=null,sourcePreexistentSpec,sourcePair);
            handleFreeSpaceExpantion(scenePoint,target,targetCallPin.getKind(),targetLine==sourceLine && sourceLine!=null,targetPreexistentSpec,targetPair);
            //
            if(sourceLine==null)//combined fragment
            {
                call.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceCallPin));
            }
            else if(!sourceNested)//clear source
            {
                sourceExWidget=new ExecutionSpecificationThinWidget(scene);
                sourceExWidget.setPreferredLocation(new Point(0,0));
                sourceCallPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()));
                call.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceCallPin,sourceExWidget.width/2));
            }
            else//from existent ex specification
            {
                //from existent use existent execution specification
                sourceCallPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()));
                call.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceCallPin,sourcePreexistentSpec.width/2));
            }
            
            if(targetLine==null)
            {
                call.setTargetAnchor(AnchorFactory.createCenterAnchor(targetCallPin));
            }
            else if(!targetNested)
            {
                targetExWidget=new ExecutionSpecificationThinWidget(scene);
                targetExWidget.setPreferredLocation(new Point(0,0));
                targetCallPin.setPreferredLocation(new Point(0,y2+sourceCallPin.getMarginBefore()));
                call.setTargetAnchor(AnchorFactory.createCircularAnchor(targetCallPin,targetExWidget.width/2));
            }
            else
            {
                //to existent need to create target execution specification
                targetExWidget=new ExecutionSpecificationThinWidget(scene);
                targetExWidget.setPreferredLocation(new Point(0,0));
                targetCallPin.setPreferredLocation(new Point(0,y2+sourceCallPin.getMarginBefore()));
                //return message/hardcoded position for now
                call.setTargetAnchor(AnchorFactory.createCircularAnchor(targetCallPin,targetExWidget.width/2));
            }

            //actions
            //seems reasonable to remove control button after drawing
            //add actions to execution specifications
            //if(sourceExWidget!=null)sourceExWidget.createActions(DesignerTools.SELECT).addAction(ActionFactory.createMoveAction(new ThinExecutionSpecificationMoveStrategy(),new ThinExecutionSpecificationMoveProvider()));
            //if(targetExWidget!=null)targetExWidget.createActions(DesignerTools.SELECT).addAction(ActionFactory.createMoveAction(new ThinExecutionSpecificationMoveStrategy(),new ThinExecutionSpecificationMoveProvider()));
            //--
            //revalidate with
            if(sourceNested)
            {
                Widget prevW=sourcePair.getPrevWidget();
                int index=sourcePreexistentSpec.getChildren().indexOf(prevW);
                sourcePreexistentSpec.addChild(index+1,sourceCallPin);
            }
            else if(sourceLine!=null)
            {
                sourceExWidget.addChild(sourceCallPin);//this is new ex spec, so index always 0
                //
                 int index=findPrevPinOrESpecIndex(sourceLine, y1);
               //
                sourceLine.addChild(index+1,sourceExWidget);
            }
            else
            {
                sourceCF.getMainWidget().addChild(sourceCallPin);
                Point start=sourceCF.getMainWidget().convertSceneToLocal(scenePoint);
                start.x=(start.x<(sourceCF.getMainWidget().getBounds().x+sourceCF.getMainWidget().getBounds().width/2)) ? (0) : (sourceCF.getMainWidget().getBounds().width);
                sourceCallPin.setPreferredLocation(new Point(start.x,start.y+sourceCallPin.getMarginBefore()));
            }
            //-------
            if(targetLine==sourceLine && sourceLine!=null)
            {
                //message to self
                if(sourceNested)
                {
                   //sourceCallPin is before target ex spec
                    int index=sourcePreexistentSpec.getChildren().indexOf(sourceCallPin);
                    sourcePreexistentSpec.addChild(index+1,targetExWidget);
                }
                else
                {
                    sourceExWidget.addChild(1, targetExWidget);//this is new source, so have at 0 position only one call pin
                }
                targetExWidget.setPreferredLocation(new Point(7,0));
                targetCallPin.setPreferredLocation(new Point(0,y2+20));
                targetExWidget.addChild(targetCallPin);
                //TBD change router for messages
                call.setRouter(new SelfMessageRouter());
            }
            else if(targetNested)
            {
                targetExWidget.addChild(targetCallPin);
                int index=targetPreexistentSpec.getChildren().indexOf(targetPair.getPrevWidget());
                targetPreexistentSpec.addChild(index+1, targetExWidget);
            }
            else if(targetLine!=null)
            {
                targetExWidget.addChild(targetCallPin);
                int index=findPrevPinOrESpecIndex(targetLine, y2+sourceCallPin.getMarginBefore()-targetCallPin.getMarginBefore());
                targetLine.addChild(index+1,targetExWidget);
            }
            else
            {
                targetCF.getMainWidget().addChild(targetCallPin);
                Point finish=convertSceneToLocal(targetCF.getMainWidget(),finishPoint);
                Point start=targetCF.getMainWidget().convertSceneToLocal(scenePoint);
                finish.x=(finish.x<targetCF.getMainWidget().getBounds().width/2) ? (0) : (targetCF.getMainWidget().getBounds().width);
                targetCallPin.setPreferredLocation(new Point(finish.x,start.y+sourceCallPin.getMarginBefore()));
            }
    }
    
    /**
     * create message on a diagram, create message always to lifelines
     * @param source
     * @param target
     * @param call
     * @param y
     */
    private void createCreateMessage(Widget source, LifelineWidget target, UMLEdgeWidget call,Point startingPoint) {
            //int y=startingPoint.y;
            LifelineWidget sourceLifeline=null;
            CombinedFragmentWidget sourceCF=null;
            if(source instanceof LifelineWidget)sourceLifeline=(LifelineWidget) source;
            else sourceCF=(CombinedFragmentWidget) source;//do not check type here because any other type is unexpected
            LifelineLineWidget sourceLine=null;
            if(sourceLifeline!=null)sourceLine=sourceLifeline.getLine();
            LifelineLineWidget targetLine=null;

            LifelineBoxWidget targetBox=target.getBox();
            int y1=0;
            if(sourceLine!=null)y1=convertSceneToLocal(sourceLine,startingPoint).y;//incoming corrdinates in LifelineWidget coordinates, we adds child to LifelineLineWidget
            else y1=convertSceneToLocal(sourceCF.getMainWidget(),startingPoint).y;
            //set position of target lifeline
            Point boxCenter=new Point(targetBox.getPreferredBounds().x+targetBox.getPreferredBounds().width/2,targetBox.getPreferredBounds().y+targetBox.getPreferredBounds().height/2);//x doen't matter, in box coordinates
            boxCenter=targetBox.convertLocalToScene(boxCenter);//in scene coordinats
            boxCenter=convertSceneToLocal(target,boxCenter);//in lifeline coordinates
            Point targetCurSceneLocation=target.getParentWidget().convertLocalToScene(target.getPreferredLocation());
            Point targetNewSceneLocation=new Point(targetCurSceneLocation.x,startingPoint.y-boxCenter.y);
            Point targetLoc=convertSceneToLocal(target.getParentWidget(),targetNewSceneLocation);
            //
            ExecutionSpecificationThinWidget sourcePreexistentSpec=getExSpecification(sourceLine, y1);
            ExSpecData sourcePair=null;
            boolean sourceNested=sourcePreexistentSpec!=null;
             if(sourceNested)
            {
                int ys=convertSceneToLocal(sourcePreexistentSpec,startingPoint).y;
                sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys);
                //correction if got position inside top margine
                if(sourcePair.getPrevWidget()==null)
                {
                    startingPoint.y+=10;
                    targetLoc.y+=10;
                    sourcePair=getWidgetsAroundPoint(sourcePreexistentSpec, ys+10);
                }
                //
                y1=convertSceneToLocal(sourcePreexistentSpec,startingPoint).y;
            }
            //
            final DesignerScene scene=(DesignerScene) source.getScene();
            //
            MessagePinWidget sourceCallPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.CREATE_CALL_OUT);
            MessagePinWidget targetCallPin=new MessagePinWidget(scene,MessagePinWidget.PINKIND.CREATE_CALL_IN);
            handleFreeSpaceExpantion(startingPoint,source,sourceCallPin.getKind(),targetLine==sourceLine && sourceLine!=null,sourcePreexistentSpec,sourcePair);
            //
            targetLoc.y+=sourceCallPin.getMarginBefore();//pin shift in source
            ExecutionSpecificationThinWidget sourceExWidget=null;
            if(sourceLine==null)//combined fragment
            {
                call.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceCallPin));
            }
            else if(!sourceNested)//free line
            {
                sourceExWidget=new ExecutionSpecificationThinWidget(scene);
                sourceExWidget.setPreferredLocation(new Point(0,0));
                sourceCallPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()));

                call.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceCallPin,sourceExWidget.width/2));
            }
            else
            {
                //from existent use existent execution specification
                sourceCallPin.setPreferredLocation(new Point(0,y1+sourceCallPin.getMarginBefore()));
                call.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceCallPin,sourcePreexistentSpec.width/2));
            }
            //
            targetBox.addChild(targetCallPin);
            //
            call.setTargetAnchor(new CreateMessageTargetAnchor(targetCallPin));
            //actions
            //seems reasonable to remove control button after drawing
            //add actions to execution specifications
            //if(sourceExWidget!=null)sourceExWidget.createActions(DesignerTools.SELECT).addAction(ActionFactory.createMoveAction(new ThinExecutionSpecificationMoveStrategy(),new ThinExecutionSpecificationMoveProvider()));
            //--
            //revalidate with
            if(sourceNested)
            {
                Widget prevW=sourcePair.getPrevWidget();
                int index=sourcePreexistentSpec.getChildren().indexOf(prevW);
                sourcePreexistentSpec.addChild(index+1,sourceCallPin);
            }
            else if(sourceLine!=null)
            {
                sourceExWidget.addChild(sourceCallPin);
                //
                 int index=findPrevPinOrESpecIndex(sourceLine, y1);
                 sourceLine.addChild(index+1,sourceExWidget);
            }
            else
            {
                sourceCF.getMainWidget().addChild(sourceCallPin);
                Point start=convertSceneToLocal(sourceCF.getMainWidget(),startingPoint);
                start.x=(start.x<(sourceCF.getMainWidget().getBounds().x+sourceCF.getMainWidget().getBounds().width/2)) ? (0) : (sourceCF.getMainWidget().getBounds().width);
                sourceCallPin.setPreferredLocation(new Point(start.x,start.y+sourceCallPin.getMarginBefore()));
            }
            target.setPreferredLocation(targetLoc);
            
            if(!PersistenceUtil.isDiagramLoading())
                new AfterValidationExecutor(new ActionProvider() {
                public void perfomeAction() {
                    SequenceDiagramEngine  engine=(SequenceDiagramEngine) scene.getEngine();
                    engine.normalizeLifelines(false, false, null);//align to minimum after create message
                }
                },scene);
    }
    
        
    /**
     * 
     * @param level
     * @param y
     * @return
     */
    private ExecutionSpecificationThinWidget getExSpecification(Widget level,int y)
    {
        Widget deep=null;
        if(level!=null)
        {
            List<Widget> children=level.getChildren();//don't need sorted here because n*log(n) is more then just n iteration here
            for(int i=0;i<children.size();i++)
            {
                Widget ch=children.get(i);
                if(ch instanceof ExecutionSpecificationThinWidget)
                {
                    if((ch.getLocation().y+ch.getBounds().y)<=y && (ch.getLocation().y+ch.getBounds().y+ch.getBounds().height)>=y)
                    {
                        deep=ch;
                        //try deeper
                        Widget deeper=getExSpecification(deep, y-deep.getLocation().y);
                        if(deeper!=null)deep=deeper;
                        break;
                    }
                }
            }
        }
        return (ExecutionSpecificationThinWidget) deep;
    }
    
    /**
     * 
     * 
     * @param exspec
     * @param y
     * @return pair of widgets enclosing point on execution specification and also first and last in the specification, usually pin widgets, but may be null and other execution specification
     */
    private ExSpecData getWidgetsAroundPoint(ExecutionSpecificationThinWidget exspec,int y)
    {
        Widget pre=null;
        Widget nxt=null;
        Widget first=null;
        Widget last=null;
        int maxYPre=Integer.MIN_VALUE;
        int minYnxt=Integer.MAX_VALUE;
        int minYFirst=Integer.MAX_VALUE;
        int maxYLast=Integer.MIN_VALUE;
        for(Widget w:exspec.getChildren())
        {
            int y0=w.getLocation().y+w.getBounds().y;
            int y1=y0+w.getBounds().height;
            //
            if(y1<=y)
            {
                //pre
                if(y1>maxYPre)
                {
                    maxYPre=y1;
                    pre=w;
                }
            }
            else if(y0>=y)
            {
                //after
                if(y0<minYnxt)
                {
                    minYnxt=y0;
                    nxt=w;
                }
            }
            else
            {
                //unexpected, for test purpose throw exception
                throw new RuntimeException("Unexpected Exception");
            }
            
            if(y0>maxYLast)
            {
                maxYLast=y0;
                last=w;
            }
            if(y0<minYFirst)
            {
              minYFirst=y0;
              first=w;
            }
        }
        return new ExSpecData(first,last,pre,nxt);
    }
            
    private class ExSpecData
    {
        Widget prev,next,first,last;
        public ExSpecData(Widget first,Widget last,Widget prev,Widget next)
        {
            this.prev=prev;
            this.next=next;
            this.first=first;
            this.last=last;
        }
        public Widget getPrevWidget()
        {
            return prev;
        }
        public Widget getNextWidget()
        {
            return next;
        }
        public Widget getFirstWidget()
        {
            return first;
        }
        public Widget getLastWidget()
        {
            return last;
        }
    }
    
    private class SelfMessageRouter implements Router
    {

        public List<Point> routeConnection(ConnectionWidget widget) {
            ArrayList<Point> ret=new ArrayList<Point>();
            MessagePinWidget source=(MessagePinWidget) widget.getSourceAnchor().getRelatedWidget();
            MessagePinWidget target=(MessagePinWidget) widget.getTargetAnchor().getRelatedWidget();
            //find line
            Widget line=source.getParentWidget();
            for(;line!=null;line=line.getParentWidget())
            {
                if(line instanceof LifelineLineWidget)break;
            }
            Point lineLocation=line.getPreferredLocation();
            if(lineLocation==null)lineLocation=line.getLocation();
            lineLocation=line.getParentWidget().convertLocalToScene(lineLocation);
            //
            Point start=widget.getSourceAnchor().getRelatedSceneLocation();
            Point finish=widget.getTargetAnchor().getRelatedSceneLocation();
            //
            boolean right=start.x>=lineLocation.x;
            //
            int semi_w=5;//consider all same, TBD get 10 somewhere
            //
            int max=Math.max(start.x,finish.x);
            int min=Math.min(start.x,finish.x);
            //
            if(right)
            {
                start.x+=semi_w;
                finish.x+=semi_w;
            }
            else
            {
                start.x-=semi_w;
                finish.x-=semi_w;
            }
            //
            ret.add(start);
            if(right)
            {
                ret.add(new Point(max+20,start.y));
                ret.add(new Point(max+25,(start.y+finish.y)/2));
                ret.add(new Point(max+20,finish.y));
            }
            else
            {
                ret.add(new Point(min-20,start.y));
                ret.add(new Point(min-25,(start.y+finish.y)/2));
                ret.add(new Point(min-20,finish.y));
            }
            ret.add(finish);
            //
            return ret;
        }
        
    }
    
    /**
     * Calculation consider widgets orderes, higher indexed widgets have higher y corrdinate
     * @param lineWidget - LifelineLineWidget or ExecutionSpec widget or any widget which may contain pins and ex specifications
     * @param y - ppint before which we are looking other widgets in lineWidget coordinate system
     * @return index of previous widget
     */
    private int findPrevPinOrESpecIndex(Widget lineWidget,int y)
    {
                        int index=-1;
                for(int i=lineWidget.getChildren().size()-1;i>=0;i--)
                {
                    Widget w=lineWidget.getChildren().get(i);
                    if(w instanceof ExecutionSpecificationThinWidget || w instanceof MessagePinWidget)
                    {
                        if((w.getPreferredLocation().y+w.getPreferredBounds().y+w.getPreferredBounds().height)<y)
                        {
                            index=i;
                            break;
                        }
                    }
                    //else we do not support direct connection without spec from line now
                }
          return index;      
    }
    
    /**
     * this method handle new message cases, i.e. dependent on new message ex spec sizes and isn't very good to reconnect action
     * TBD need to be updated later
     * @param y position in scene? coordinated
     * @param widget Lifeline/CF widget
     * @param kind create/asych,synch messages
     * @param preExistent if there preexistent spec in place
     */
    private void handleFreeSpaceExpantion(Point scenePoint,Widget widget,MessagePinWidget.PINKIND pinkind,boolean toself,ExecutionSpecificationThinWidget preExistent,ExSpecData pair)
    {
        if(widget instanceof LifelineWidget)
        {
            LifelineWidget lifeline=(LifelineWidget) widget;
            int yfree_req=20;//minimum free space?, for rcreate message, call out of asynch
            if(toself)
            {
                if(pinkind==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT)
                {
                    yfree_req=90;
                }
                else
                {
                    yfree_req=80;
                }
            }
            else
            {
                if(pinkind==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT || pinkind==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN)
                {
                    yfree_req=50;
                }
                else if(pinkind==MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_IN)
                {
                    yfree_req=40;
                }
            }
            yfree_req+=10;
            ////////////
            if(preExistent!=null)
            {
                Widget nxt=pair.getNextWidget();
                if(nxt instanceof MessagePinWidget)
                {
                    MessagePinWidget pin=(MessagePinWidget) nxt;
                    int dy=yfree_req-(pin.getParentWidget().convertLocalToScene(pin.getPreferredLocation()).y-scenePoint.y);
                    new ArrangeMoveWithBumping(null, null, null).moveDown(pin, dy);
                    widget.getScene().validate();
                }
                else if(nxt!=null)
                {
                    ExecutionSpecificationThinWidget exSpec=(ExecutionSpecificationThinWidget) nxt;
                    MessagePinWidget pin=(MessagePinWidget) exSpec.getChildren().get(0);
                    int dy=yfree_req-(pin.getParentWidget().convertLocalToScene(pin.getPreferredLocation()).y-scenePoint.y);
                    new ArrangeMoveWithBumping(null, null, null).moveDown(pin, dy);
                    widget.getScene().validate();
                }
                else
                {
                    
                }
            }
            else
            {
                LifelineLineWidget line=lifeline.getLine();
                ExecutionSpecificationThinWidget nxt=null;
                Point linePoint=convertSceneToLocal(line,scenePoint);
                for(Widget w:line.getChildren())
                {
                    if((w.getPreferredLocation().y+w.getPreferredBounds().y)>linePoint.y)
                    {
                        //if(nxt==null || ((nxt.getPreferredLocation().y+nxt.getPreferredBounds().y)>(w.getPreferredLocation().y+w.getPreferredBounds().y)))
                        {
                            nxt=(ExecutionSpecificationThinWidget) w;
                            break;//it's ordered
                        }
                    }
                }
                if(nxt!=null)
                {
                    Point nxtPoint=new Point(0,nxt.getPreferredLocation().y+nxt.getPreferredBounds().y);
                    int dy=yfree_req-(nxt.getParentWidget().convertLocalToScene(nxtPoint).y-scenePoint.y);
                    new ArrangeMoveWithBumping(null, null, null).moveDown((MessagePinWidget) nxt.getChildren().get(0), dy);
                    widget.getScene().validate();
                }
            }
        }
        else if(widget instanceof CombinedFragmentWidget)
        {
            
        }
    }
    
    /**
     * support reconnection of target of synch or asynch messages from to lifelines only
     */
    public void reconnectMessage(GraphScene scene,MessageFactory factory,IPresentationElement messagePE,IPresentationElement targetLifelinePE)
    {
        if(!processInvokedOperation(messagePE.getFirstSubject(),targetLifelinePE.getFirstSubject()))return;
        factory.reconnectTarget(messagePE.getFirstSubject(), targetLifelinePE.getFirstSubject());
        IMessage message=(IMessage) messagePE.getFirstSubject();
        IMessage resultMessage=null;
        ILifeline targetLifeline=(ILifeline) targetLifelinePE.getFirstSubject();
        IPresentationElement resultPE=null;
        LifelineWidget target=(LifelineWidget) scene.findWidget(targetLifelinePE);
        if(message.getKind()==BaseElement.MK_SYNCHRONOUS || message.getKind()==BaseElement.MK_ASYNCHRONOUS)
        {
            MessageWidget call=(MessageWidget) scene.findWidget(messagePE);
            
            MessageWidget result=null;
            //call section
            MessagePinWidget targetPin1=(MessagePinWidget) call.getTargetAnchor().getRelatedWidget();
            ExecutionSpecificationThinWidget targetSpec=(ExecutionSpecificationThinWidget) targetPin1.getParentWidget();
            //need to move return message also, and may be need to move all child messages
            MessagePinWidget returnPin=null;
            for(Widget w:targetSpec.getChildren())
            {
                if(w instanceof MessagePinWidget)
                {
                    MessagePinWidget pin=(MessagePinWidget) w;
                    if(pin.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_OUT)returnPin=pin;
                }
            }
            //
            if(returnPin!=null)
            {
                result=(MessageWidget) returnPin.getConnection(0);
                resultPE=(IPresentationElement) scene.findObject(result);
                resultMessage=(IMessage) resultPE.getFirstSubject();
                factory.reconnectSource(resultMessage, targetLifelinePE.getFirstSubject());
            }
            //position where it was
            Point localPoint=targetPin1.getPreferredLocation();//consider all specs are at zero position and only pins move
            Point scenePoint1=targetSpec.convertLocalToScene(localPoint);
            Point scenePoint2=null;
            if(returnPin!=null)
            {
                scenePoint2=targetSpec.convertLocalToScene(returnPin.getPreferredLocation());
            }
            targetSpec.getParentWidget().removeChild(targetSpec);
            //
            //+++++++++++++++++++++++++++++++++
            LifelineWidget targetLifeLine=(LifelineWidget) scene.findWidget(targetLifelinePE);
            LifelineLineWidget targetLine=targetLifeLine.getLine();
            int y2=targetLine.convertSceneToLocal(scenePoint1).y;//
            int yret=0;if(scenePoint2!=null)yret=targetLine.convertSceneToLocal(scenePoint2).y;
            ExecutionSpecificationThinWidget targetPreexistentSpec=getExSpecification(targetLine, y2);
            ExSpecData targetPair=null;
            
            
            boolean targetNested=targetPreexistentSpec!=null;
            if(targetNested)
            {
                y2=targetPreexistentSpec.convertSceneToLocal(scenePoint1).y;
                targetPair=getWidgetsAroundPoint(targetPreexistentSpec, y2);
                if(scenePoint2!=null)yret=targetPreexistentSpec.convertSceneToLocal(scenePoint2).y;
                //correction if got position inside top margine
                if(targetPair.getPrevWidget()==null)
                {
                    throw new UnsupportedOperationException("Can't reconnect, need code improvement");
                }
            }
            
            handleFreeSpaceExpantion(scenePoint1,target,targetPin1.getKind(),false,targetPreexistentSpec,targetPair);//do not support messages to self for now (as in 6.0, TBD disable start of reconnection)
            //--
            if(targetNested)
            {
                int index=targetPreexistentSpec.getChildren().indexOf(targetPair.getPrevWidget());
                targetPreexistentSpec.addChild(index+1, targetSpec);
                targetPin1.setPreferredLocation(new Point(0,y2));
                if(returnPin!=null)returnPin.setPreferredLocation(new Point(0,yret));
            }
            else if(targetLine!=null)
            {
                int index=findPrevPinOrESpecIndex(targetLine, y2);
                targetLine.addChild(index+1,targetSpec);
                targetPin1.setPreferredLocation(new Point(0,y2));
                if(returnPin!=null)returnPin.setPreferredLocation(new Point(0,yret));
             }
            //+++++++++++++++++++++++++++++++++
            Anchor tmp=call.getTargetAnchor();
            scene.setEdgeTarget(messagePE, targetLifelinePE);
            call.setTargetAnchor(tmp);//edge update change anchor also
            if(resultPE!=null)
            {
                tmp=result.getSourceAnchor();
                scene.setEdgeSource(resultPE, targetLifelinePE);
                result.setSourceAnchor(tmp);
            }
        }
    }
    /**
     * When a message has been moved from one lifeline to another, process its invoked operation
     *
     * @param edge    The TS edge containing the a message, which may have an invoked operation
     * @param endNode The node the edge is moving to
     */
    protected boolean processInvokedOperation( IElement edgeElement, IElement nodeElement )
    {       
        boolean bProcessInvokedOperation = true;
        
        if (edgeElement instanceof IMessage)
        {
            IMessage message = (IMessage)edgeElement;
            
            IOperation operation = message.getOperationInvoked();
            if( operation != null )
            {
                // Determine the new invoked operation, the default is null
                IOperation operationInvoked = null;
                
                if (nodeElement instanceof ILifeline)
                {
                    ILifeline lifeline = (ILifeline)nodeElement;
                    
                    IClassifier classifier = lifeline.getRepresentingClassifier();
                    if( classifier != null )
                    {
                        operationInvoked = classifier.findMatchingOperation( operation );
                        
                        if( null == operationInvoked )
                        {
                            // Determine if the user wants to copy, move or cancel
                            //kris richards - PROBLEM - need to remove this pref.
                            IPreferenceQuestionDialog questionDialog = new SwingPreferenceQuestionDialog();
                            if( questionDialog != null )
                            {
                                String strQuestion = RESOURCE_BUNDLE.getString( "IDS_Q_INVOKED_OPERATION" );
                                String strQuestionTitle = RESOURCE_BUNDLE.getString( "IDS_Q_INVOKED_OPERATION_TITLE" );
                                
                                int nResult =
                                        questionDialog.displayFromStrings( "Default",
                                        "Diagrams|SequenceDiagram",
                                        "UML_ShowMe_Move_Invoked_Operation",
                                        "PSK_ALWAYS",
                                        "PSK_NEVER",
                                        "PSK_ASK",
                                        strQuestion,
                                        SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                                        strQuestionTitle,
                                        SimpleQuestionDialogKind.SQDK_YESNOCANCEL,
                                        MessageIconKindEnum.EDIK_ICONQUESTION,
                                        null );
                                
                                switch( nResult )
                                {
                                    case SimpleQuestionDialogResultKind.SQDRK_RESULT_YES:
                                        operation.moveToClassifier( classifier );
                                        operationInvoked = operation;
                                        break;
                                        
                                    case SimpleQuestionDialogResultKind.SQDRK_RESULT_NO:
                                    {
                                        IFeature feature = operation.duplicateToClassifier( classifier );
                                        if (feature instanceof IOperation)
                                        {
                                            operationInvoked = (IOperation)feature;
                                        }
                                    }
                                    break;
                                    
                                    default:
                                        assert ( false );
                                    case SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL:
                                        bProcessInvokedOperation = false;
                                        break;
                                }
                            }
                        }
                    }
                }
                
                if( bProcessInvokedOperation )
                {
                    message.setOperationInvoked( operationInvoked );
                }
            }
        }
        
        return bProcessInvokedOperation;
    }
    
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        throw new UnsupportedOperationException("Messages can't be created without points specification.");
    }
    public void createConnection(Widget sourceWidget, Widget targetWidget) {
        throw new UnsupportedOperationException("Messages can't be created without points specification.");
    }

    private static final String BUNDLE_NAME = "org.netbeans.modules.uml.diagrams.engines.Bundle"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public Widget createTargetWidget(Scene scene, Widget soruceWidget, Point location) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Converts a location in the scene coordination system to the local coordination system.
     * coerting based on preferred widgets locations
     * @param sceneLocation the scene location
     * @return the local location
     */
    public Point convertSceneToLocal (Widget widget,Point sceneLocation) {
        Point localLocation = new Point (sceneLocation);
        while (widget != null) {
            if (widget == widget.getScene())
                break;
            Point location = widget.getPreferredLocation();
            if(location==null)location=widget.getLocation();
            localLocation.x -= location.x;
            localLocation.y -= location.y;
            widget = widget.getParentWidget ();
        }
        return localLocation;
    }

}
