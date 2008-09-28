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
package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.diagrams.actions.ClassifierSelectAction;
import org.netbeans.modules.uml.diagrams.actions.sqd.ArrangeMoveWithBumping;
import org.netbeans.modules.uml.diagrams.actions.sqd.OperandsMoveProvider;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.layouts.sqd.CombinedFragmentBodyLayout;
import org.netbeans.modules.uml.diagrams.layouts.sqd.OperandsLayout;
import org.netbeans.modules.uml.diagrams.nodes.ContainerNode;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContext;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContextFactory;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import java.util.TreeSet;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.nodes.LabeledWidget;
import org.netbeans.modules.uml.diagrams.nodes.MovableLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.widgets.CombinedFragment;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeReader;

/**
 *
 * @author sp153251
 */
public class CombinedFragmentWidget extends ContainerNode implements PropertyChangeListener,CombinedFragment {

    private InteractionOperatorWidget operator;
    private Widget body;
    private Widget operandsContainer;
    protected ContainerWidget childContainer;
    //
    private HashMap<IInteractionOperand, InteractionOperandWidget> operands = new HashMap<IInteractionOperand, InteractionOperandWidget>();
    private boolean isShowWidget;
    private IMessage messageBefore;
    private MessageWidget messageBeforeW;
    private IMessage messageAfter;
    private MessageWidget messageAfterW;
    private ICombinedFragment cfBefore;
    private CombinedFragmentWidget cfBeforeW;
    private ICombinedFragment cfAfter;
    private CombinedFragmentWidget cfAfterW;

    public CombinedFragmentWidget(Scene scene) {
        this(scene, "assert");
    }

    public CombinedFragmentWidget(Scene scene, String kind) {
        super(scene,true);
        body = new Widget(getScene());
        body.setForeground(null);
        operator = new InteractionOperatorWidget(getScene(), "");
        operator.setForeground(null);
        operator.setBackground(null);
        body.setBorder(BorderFactory.createLineBorder(1, getForeground()));
        operator.setPreferredLocation(new Point(0, 0));
        body.addChild(operator);
        operandsContainer = new Widget(getScene());
        operandsContainer.setForeground(null);
        operandsContainer.setBackground(null);
        operandsContainer.setPreferredLocation(new Point(0, 0));
        body.addChild(operandsContainer);
        //
        body.setMinimumSize(new Dimension(60, 20));
        setPreferredBounds(new Rectangle(new Dimension(120, 100)));

        body.setPreferredLocation(new Point(0, 0));
        childContainer=new ContainerWidget(getScene());
        childContainer.setPreferredLocation(new Point(0, 0));
        body.addChild(childContainer);

        setCurrentView(body);
        ((Customizable) body.getParentWidget()).setCustomizableResourceTypes(new org.netbeans.modules.uml.drawingarea.view.ResourceType[]{org.netbeans.modules.uml.drawingarea.view.ResourceType.FONT,org.netbeans.modules.uml.drawingarea.view.ResourceType.FOREGROUND});
 
        operandsContainer.setLayout(new OperandsLayout());
        body.setLayout(new CombinedFragmentBodyLayout());
        //
        addToLookup(initializeContextPalette());
        addToLookup(new CombinedFragmentWidgetContext());
    }


    public Widget getMainWidget() {
        return body;
    }

    protected void setOperator(String value)
    {
        operator.setLabel(value);
    }

    @Override
    public Dimension getResizingMinimumSize() {
        int height=24;
        int width=60;
        width=Math.max(operator.getBounds().width+10, width);
        if(operandsContainer.getChildren().size()==0)
        {
            //nothing to count on more
        }
        else
        {
            Widget lastChild=operandsContainer.getChildren().get(operandsContainer.getChildren().size()-1);
            height=Math.max(height, lastChild.getLocation().y+10);
        }
        return new Dimension(width,height);
    }
    
    @Override
    public void initializeNode(IPresentationElement presentation) {
        // Since the interaction boundary widget extends the the CombinedFragmentWidge
        // we have to make sure the element type is not an interaction object.
        if(presentation.getFirstSubject() instanceof ICombinedFragment)
        {
            ICombinedFragment src = (ICombinedFragment) presentation.getFirstSubject();
            String name = ((BaseElement)src).getAttributeValue("interactionOperator");
            if (name == null || name.length() == 0) {
                name = "assert";
            }
            setOperator(name);
            //add all necessary operands

            for (IInteractionOperand i : src.getOperands()) {
                //i.createGuard();
                InteractionOperandWidget w=addOperand(i);
                getScene().validate();
            }
        }
        super.initializeNode(presentation);
    }

    @Override
    public void initializeNode(IPresentationElement presentation, boolean show) {
        isShowWidget=show;
        if(isShowWidget && presentation.getFirstSubject() instanceof ICombinedFragment)
        {
            ICombinedFragment src = (ICombinedFragment) presentation.getFirstSubject();
            src.createOperand();
        }
        super.initializeNode(presentation, show);
    }

    
    
    @Override
    public String toString() {
        ICombinedFragment cf= null;
        if(getObject()!=null && getObject().getFirstSubject() instanceof ICombinedFragment)
        {
            cf=(ICombinedFragment) getObject().getFirstSubject();
            return "CombinedFragmentWidget: operator: "+ cf.getOperator()+"; name: "+cf.getName()+"; num operands: "+cf.getOperands().size()+"; bounds: "+getBounds()+"; ////" + super.toString();
        }
        else return super.toString();
    }

    public String getKind() {
        return "CombinedFragment";
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        Object value = evt.getNewValue();
        Object old = evt.getOldValue();
        Object source = evt.getSource();
        ICombinedFragment src = null;
        if (source instanceof ICombinedFragment) {
            src = (ICombinedFragment) source;
            if (name.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString())) {
                String nm = ((BaseElement)src).getAttributeValue("interactionOperator");
                if (nm == null || nm.length() == 0) {
                    nm = "assert";
                }
                operator.setLabel(nm);
            //changes in operands are handled directly (the same as in previous release, and because event do not provide enouth information and it reqires a lot of calculation to handle common event type)
                //TBD: good to intriduce(or find) appropriate events in core
            } else if (name.equals(ModelElementChangedKind.STEREOTYPE.toString())) {
            //boxWidget.setStereotype(src.getAppliedStereotypesAsString(false));//TBD based on aliasing settings
            }
        }
    }
    //
    @Override
    public void remove() {
        IPresentationElement node=getObject();
        DesignerScene scene=(DesignerScene) getScene();
        TreeSet<MessagePinWidget> pins=new TreeSet<MessagePinWidget>();//pins to remove
        for (IPresentationElement edge : (Collection<IPresentationElement>) scene.findNodeEdges(node, true, true))//output edges
        if (scene.isEdge (edge))
        {
            if(edge.getFirstSubject() instanceof IMessage)
            {
                //messages, need to check kind and existens of nested  messages on opposite side and exe specs too (on this side will be removed by default)
                MessageWidget mW=(MessageWidget) scene.findWidget(edge);
                //at least pins need to be deleted too
                MessagePinWidget sourcePin=(MessagePinWidget) mW.getSourceAnchor().getRelatedWidget();
                MessagePinWidget targetPin=(MessagePinWidget) mW.getTargetAnchor().getRelatedWidget();
                //
                pins.add(sourcePin);
                pins.add(targetPin);
                //
                scene.removeEdge (edge);//all "bounds" found, may remove now
                //
                Widget targetParent=targetPin.getParentWidget();
                IPresentationElement targetPresentationElement=(IPresentationElement) scene.findObject(targetParent);
                //can now remove target pin
                //targetParent.removeChild(targetPin);
                if(targetPin.getKind()==MessagePinWidget.PINKIND.CREATE_CALL_IN)
                {
                    //need to adjust lifelien position
                   IPresentationElement pe=(IPresentationElement) scene.findObject(targetPin);
                   LifelineWidget llW=(LifelineWidget) scene.findWidget(pe);
                   ILifeline llE=(ILifeline) pe.getFirstSubject();
                    //move lifeline up and all messages on it down
                    Point loc=llW.getPreferredLocation();
                    int dy=loc.y-20-40*(llE.getIsActorLifeline()?0:1);
                    if(dy!=0)
                    {
                        ArrangeMoveWithBumping.correctPindOnWidget(llW.getLine(), dy);
                        loc.y=20+40*(llE.getIsActorLifeline()?0:1);
                        llW.setPreferredLocation(loc);
                    }
                }
            }
            else
            {
                //comment or other simple links
                scene.removeEdge (edge);
            }
        }
        //now remove pins and handle possible ex spec relocation
        for(MessagePinWidget pin:pins)
        {
            Widget par=pin.getParentWidget();
            if(par!=null)
            {
                par.removeChild(pin);
                if(par instanceof ExecutionSpecificationThinWidget)
                {
                    Widget parpar=par.getParentWidget();
                    if(parpar!=null)
                    {
                        if(par.getChildren().size()==0)
                        {
                            parpar.removeChild(par);//remove empty execution specification
                        }
                    }
                }
                else
                {
                   //just remove pin and do nothing else for former createdlifeline 
                    //or for combined fragments
                }
            }
            //relocation of parent spec: tbd
            //
        }
        //now handle containment, do not remove content
        ContainerWidget container = getContainer();
        if(container!=null)
        {
//            Widget parent=scene.getMainLayer();
//            for(Widget child : container.getChildren())
//            {
//                Point location = container.convertLocalToScene(child.getLocation());
//                child.setPreferredLocation(location);
//
//                container.removeChild(child);
//                parent.addChild(child);
//            }
//            scene.validate();
            container.removeAllChildren(scene);
        }
        //
        scene.removeNode (node);
    }

    private DefaultContextPaletteModel initializeContextPalette() {
        DefaultContextPaletteModel paletteModel = 
                new DefaultContextPaletteModel(this, ContextPaletteModel.FOLLOWMODE.VERTICAL_AND_HORIZONTAL);
        
        paletteModel.initialize("UML/context-palette/CombinedFragment");
        return paletteModel;
    }
    /**
     * additioon of operand to the widget
     * 
     * 
     * @param op
     */

    public InteractionOperandWidget addOperand(IInteractionOperand op) {
        InteractionOperandWidget ret=null;
        
        if (operands.get(op) != null) {
            //it was already added, log to track possile perfomance
//            System.out.println("***WARNING: "+"Operand is already added, op:"+op.getConstraintsAsString()+"; ");
        } else {
            InteractionOperandWidget opW = new InteractionOperandWidget(getScene());
            ret=opW;
            opW.initialize(op);
            opW.createActions(DesignerTools.SELECT).addAction(ActionFactory.createSelectAction(new ClassifierSelectAction()));
            operands.put(op, opW);
            int prevIndex = operandsContainer.getChildren().size() - 1;//TBD let it be in vertical position order at first iteraction
            InteractionOperandWidget opWPrev = null;
            if (prevIndex >= 0) {
                opWPrev = (InteractionOperandWidget) operandsContainer.getChildren().get(prevIndex);
            }


            Rectangle aaBounds = getBounds();
            if (aaBounds == null) {
                aaBounds = new Rectangle(0, 0, 0, 0);
                if(opWPrev!=null)aaBounds.height=Math.max(20,opWPrev.getPreferredLocation().y+10);
            }
            Point prefPosition = new Point(0, 0);//first operands located at top level
            int vertical_shift = 0;
            if (opWPrev != null) {
                /*ETList<IMessage> messages=opWPrev.getOperand().getCoveredMessages();
                if(messages!=null && messages.size()>0)
                {
                    //TBD, better addition logic, but for now the same as in oprevios release
                }*/
                vertical_shift = aaBounds.height;
            }
            prefPosition.translate(0, vertical_shift);
            opW.setPreferredLocation(prefPosition);
            operandsContainer.addChild(opW);
            getScene().validate();
            //
            if (operandsContainer.getChildren().size() > 1)//don't need to move 1st(default) operand
            {
            //
                WidgetAction.Chain selectTool = opW.createActions(DesignerTools.SELECT);
                OperandsMoveProvider provider = new OperandsMoveProvider();
                WidgetAction operandMoveAction = ActionFactory.createMoveAction(provider, provider);
                selectTool.addAction(operandMoveAction);
                switch(getResizeMode())
                {
                    case MINIMUMSIZE:
                    Dimension minS=getMinimumSize();
                    if(minS!=null)
                    {
                        minS.height+=100;
                        setMinimumSize(minS);
                    }
                    break;
                    case PREFERREDBOUNDS:
                        Rectangle prefBnd=null;
                        if(isPreferredBoundsSet())prefBnd=getPreferredBounds();
                        if(prefBnd!=null)
                        {
                            prefBnd.height+=100;
                            setPreferredBounds(prefBnd);
                        }
                        break;
                        //other unsupported
                }
            }
        }
        return ret;
    }

    public InteractionOperandWidget addOperand(IInteractionOperand op, IInteractionOperand beforeOperand) {
        throw new UnsupportedOperationException("It's not supported yet to insert operand to any position");
    }

    public boolean removeOperand(IInteractionOperand op) {
        if(operands.get(op)!=null)
        {
            InteractionOperandWidget opW = operands.get(op);
            if(opW.getLocation().y>0)
            {
                MovableLabelWidget labelWidget=opW.getLabel();
                DesignerScene scene=(DesignerScene) getScene();
                IPresentationElement lblPE=(IPresentationElement) scene.findObject(labelWidget);
                if(lblPE!=null)opW.getOperand().getGuard().getSpecification().removePresentationElement(lblPE);
                if(labelWidget!=null)labelWidget.removeFromParent();
                IPresentationElement ioPE=(IPresentationElement) scene.findObject(opW);
                operandsContainer.removeChild(opW);
                if(ioPE!=null)opW.getOperand().removePresentationElement(ioPE);
                operands.remove(op);
                scene.validate();
                return true;
            }
        }
        return false;
    }
    
    
    private class CombinedFragmentWidgetContext implements WidgetContextFactory
    {
        public WidgetContext findWidgetContext(Point localLocation)
        {
            WidgetContext retVal = null;
            if(operator!=null)
            {
                Point scenePnt=convertLocalToScene(localLocation);
                if(operator.isHitAt(operator.convertSceneToLocal(scenePnt)) && (operandsContainer.getChildren().size()>0))
                {
                    retVal=new DefaultWidgetContext("Operator",(InteractionOperandWidget) operandsContainer.getChildren().get(0));//operator is located in forst operand area
                }
                else if(operandsContainer.getChildren().size()>0)
                {
                    Point operandsPoint=operandsContainer.convertSceneToLocal(scenePnt);
                    InteractionOperandWidget operandW=null;
                    for(Widget w:operandsContainer.getChildren())
                    {
                        int y_bot=w.getPreferredLocation().y;
                        if(y_bot<=0)y_bot=-10;//possible border
                        if(operandsPoint.y>y_bot)
                        {
                            if(operandW==null)operandW=(InteractionOperandWidget) w;
                            else if (operandW.getPreferredLocation().y<w.getPreferredLocation().y)operandW=(InteractionOperandWidget) w;
                        }
                    }
                    retVal=new DefaultWidgetContext("Operand", operandW);
                }
            }
            if(retVal==null)
            {
                retVal=new DefaultWidgetContext("CombinedFragment",CombinedFragmentWidget.this);
            }
            return retVal;
        }
    }

    public String getWidgetID() {
        return UMLWidgetIDString.COMBINEDFRAGMENTWIDGET.toString();
    }
    
    @Override
    public void refresh(boolean resizetocontent)
    {
        IPresentationElement pe = getObject();
        if (pe != null && pe.getFirstSubject() != null && !pe.getFirstSubject().isDeleted())
        {
            //need to update smth
        } else
        {
            remove();
        }
        getScene().validate();
    }

    @Override
    public ContainerWidget getContainer() {
        return childContainer;
    }

    public void resizeToModelContent() {
        //
        ObjectScene scene=(ObjectScene) getScene();
        IPresentationElement pe=(IPresentationElement) scene.findObject(this);
        org.netbeans.modules.uml.core.metamodel.dynamics.CombinedFragment cf=(org.netbeans.modules.uml.core.metamodel.dynamics.CombinedFragment) pe.getFirstSubject();
        //
        Rectangle bounds=null;
        //
        ETList<ILifeline> coveredLifelines=cf.getCoveredLifelines();
        if(coveredLifelines.size()>0)
        {
            for(ILifeline ll:coveredLifelines)
            {
                for(IPresentationElement llPe:ll.getPresentationElements())
                {
                    Widget llW=scene.findWidget(llPe);
                    Rectangle bndTmp=llW.convertLocalToScene(llW.getBounds());
                    if(bounds==null)
                    {
                        bounds=new Rectangle(bndTmp);
                    }
                    else
                    {
                        bounds.add(bndTmp);
                    }
                }
            }
        }

        ETList<IInteractionOperand> operandInCf=cf.getOperands();
        HashMap<IInteractionOperand,Rectangle> operandsBounds=new HashMap<IInteractionOperand,Rectangle>();
        //
        for(IInteractionOperand io:operandInCf)
        {
            Rectangle opRectngle=null;
            ETList<IInteractionFragment> fragments=io.getFragments();
            if(fragments.size()>0)
            {
                for(INamedElement ll:fragments)
                {
                    for(IPresentationElement llPe:ll.getPresentationElements())
                    {
                        Widget llW=scene.findWidget(llPe);
                        Rectangle bndTmp=llW.convertLocalToScene(llW.getBounds());
                        if(opRectngle==null)
                        {
                            opRectngle=new Rectangle(bndTmp);
                        }
                        else
                        {
                            opRectngle.add(bndTmp);
                        }
                    }
                }
            }
            //
            coveredLifelines=io.getCoveredLifelines();
            if(coveredLifelines.size()>0)
            {
                for(ILifeline ll:coveredLifelines)
                {
                    for(IPresentationElement llPe:ll.getPresentationElements())
                    {
                        Widget llW=scene.findWidget(llPe);
                        Rectangle bndTmp=llW.convertLocalToScene(llW.getBounds());
                        if(opRectngle==null)
                        {
                            opRectngle=new Rectangle(bndTmp);
                        }
                        else
                        {
                            opRectngle.add(bndTmp);
                        }
                    }
                }
            }
            //
            ETList<IMessage> opMessages=io.getCoveredMessages();
            if(opMessages.size()>0)
            {
                for(IMessage msg:opMessages)
                {
                    if(msg.getPresentationElementCount()>1)throw new UnsupportedOperationException("supported only 1-1 message to presentation element relation");
                    IPresentationElement msgPE=msg.getPresentationElements().get(0);
                    MessageWidget msgW=(MessageWidget) scene.findWidget(msgPE);
                    Point p1=msgW.getSourceAnchor().getRelatedSceneLocation();
                    if(opRectngle==null)
                    {
                        opRectngle=new Rectangle(p1.x,p1.y-10,0,20);
                    }
                    else
                    {
                        p1.y-=24;
                        opRectngle.add(p1);
                        p1.y+=34;
                        opRectngle.add(p1);
                    }
                    Point p2=msgW.getTargetAnchor().getRelatedSceneLocation();
                    p2.y-=24;
                    opRectngle.add(p2);
                    p2.y+=34;
                    opRectngle.add(p2);
                    if(msg.getKind()==BaseElement.MK_CREATE)
                    {
                        //if message is create message need to add box also, it's HARDCODED relation between pin and box here
                        Widget targetBox=msgW.getTargetAnchor().getRelatedWidget().getParentWidget();
                        Rectangle targetBoxBounds=targetBox.convertLocalToScene(targetBox.getBounds());
                        targetBoxBounds.x-=5;
                        targetBoxBounds.y-=5;
                        targetBoxBounds.height+=10;
                        targetBoxBounds.width+=10;
                        opRectngle.add(targetBoxBounds);
                    }
                    else if(msgW.getTargetAnchor().getRelatedWidget().getParentWidget()==msgW.getSourceAnchor().getRelatedWidget().getParentWidget().getParentWidget() || msgW.getTargetAnchor().getRelatedWidget().getParentWidget().getParentWidget()==msgW.getSourceAnchor().getRelatedWidget().getParentWidget())
                    {
                        //message to self, need to add some space right
                        p1.x+=40;//may be good to add space to width of lifeline
                        p2.x+=40;
                        opRectngle.add(p2);
                        opRectngle.add(p1);
                    }
                    LabelManager lm=msgW.getLookup().lookup(LabelManager.class);
                    Collection<Widget> labelsW=lm.getLabelMap().values();
                    for(Widget lW:labelsW)
                    {
                        if(lW.isVisible())
                        {
                            opRectngle.add(lW.convertLocalToScene(lW.getBounds()));
                        }
                     }
                }
            }
            // Search for all the combined fragments, including grandchildren, etc.
            IElementLocator locator = new ElementLocator();
            ETList < IElement > childCfs = locator.findElementsByQuery(io, ".//UML:CombinedFragment");
            if(childCfs.size()>0)
            {
                for(IElement chCF:childCfs)
                {
                    for(IPresentationElement cfPe:chCF.getPresentationElements())
                    {
                        Widget cfW=scene.findWidget(cfPe);
                        Rectangle bndTmp=cfW.convertLocalToScene(cfW.getBounds());
                        if(opRectngle==null)
                        {
                            opRectngle=new Rectangle(bndTmp);
                        }
                        else
                        {
                            opRectngle.add(bndTmp);
                        }
                    }
                }
            }
            if(opRectngle!=null)
            {
                if(bounds==null)
                {
                    bounds=new Rectangle(opRectngle);
                }
                else
                {
                    bounds.add(opRectngle);
                }
            }
            operandsBounds.put(io, opRectngle);
        }
        //look for all missed operands (i.e. not handled, need at least 0 size even if it will not look good)
        //this way there will be no npe(in normalization logic) and broken diagram creation other cases should remain as was.
        if(bounds!=null)
        {
            //find if there any space to expand if necessary
            int y_min=40;
            if(messageBeforeW!=null)
            {
                y_min=messageBeforeW.getSourceAnchor().getRelatedSceneLocation().y+10;
            }
            if(cfBeforeW!=null)
            {
                Point loc=cfBeforeW.getParentWidget().convertLocalToScene(cfBeforeW.getPreferredLocation());
                int prevheight=0;
                if(cfBeforeW.isPreferredBoundsSet())prevheight=cfBeforeW.getPreferredBounds().height;//we use bounds for resizing now, same should be in save-load etc
                else if(cfBeforeW.getPreferredSize()!=null)prevheight=cfBeforeW.getPreferredSize().height;//but in case of any problem try also prefsize
                else if(cfBeforeW.getMinimumSize()!=null)prevheight=cfBeforeW.getMinimumSize().height;//and min size
                y_min=Math.max(y_min, loc.y+prevheight);
            }
            int count_null_before=0;
            //first handle unhandled operands befor first handled (will use space above first with bounds)
            for(IInteractionOperand io:operandInCf)
            {
                Rectangle recCur=operandsBounds.get(io);
                if(recCur==null)
                {
                    count_null_before++;
                }
                else
                {
                    break;
                }
            }
            if(count_null_before>0)
            {
                int height_before=40*count_null_before;
                height_before=Math.min(height_before, bounds.y-y_min);
                height_before=Math.max(count_null_before*10,height_before);
                //
                bounds.y-=height_before;
                bounds.height+=height_before;
                //fill missed operands before first not null
                int cnt=0;
                for(IInteractionOperand io:operandInCf)
                {
                    Rectangle recCur=operandsBounds.get(io);
                    if(recCur==null)
                    {
                        cnt++;
                        operandsBounds.put(io, new Rectangle(bounds.x,bounds.y+cnt*height_before/count_null_before,bounds.width,height_before/count_null_before));
                    }
                    else
                    {
                        break;
                    }
                }
            }
            //now handle operands between operands with bounds, need to use space between.
            //and case where no handler operands after, use some default size.
            Rectangle prevBnd=new Rectangle(bounds.x,bounds.y,bounds.width,0);//just to be sure not null
            Rectangle nxtBnd=null;
            for(int i=0;i<operandInCf.size();i++)
            {
                IInteractionOperand io=operandInCf.get(i);
                Rectangle recCur=operandsBounds.get(io);
                if(recCur==null)
                {
                    int cnt=0;
                    for(int j=i;j<operandInCf.size();j++)
                    {
                        IInteractionOperand io2=operandInCf.get(j);
                        Rectangle recCur2=operandsBounds.get(io2);
                        if(recCur2==null)
                        {
                            cnt++;
                        }
                        else
                        {
                            nxtBnd=recCur2;
                            break;
                        }
                    }
                    int height=40*cnt;
                    if(nxtBnd!=null)
                    {
                        height=nxtBnd.y-prevBnd.y-prevBnd.height;
                    }
                    else
                    {
                        bounds.height+=height;
                    }
                    if(height<0)
                    {
                        height=cnt;//will get at least 1px for each
                    }
                    for(int j=i;j<operandInCf.size();j++)
                    {
                        IInteractionOperand io2=operandInCf.get(j);
                        if(operandsBounds.get(io2)==null)operandsBounds.put(io2, new Rectangle(bounds.x,prevBnd.y+prevBnd.height+(j-i)*height/cnt,bounds.width,height/cnt));
                        else break;
                    }
                    i+=cnt-1;//skip handled part -1, this eway in next iteraction not null prevBnd should be set
                }
                prevBnd=recCur;
            }
        }
        //case where no operands was handlled by previous section so entire cf need to be handled by boundary limits
        if(bounds==null)
        {
            //have no children, will look for neighbors
            int y=Integer.MIN_VALUE;
            int x=40;//???
            if(messageBeforeW!=null)
            {
                y=messageBeforeW.getSourceAnchor().getRelatedSceneLocation().y+10;
            }
            if(cfBeforeW!=null)
            {
                Point loc=cfBeforeW.getParentWidget().convertLocalToScene(cfBeforeW.getPreferredLocation());
                int prevheight=0;
                if(cfBeforeW.isPreferredBoundsSet())prevheight=cfBeforeW.getPreferredBounds().height;//we use bounds for resizing now, same should be in save-load etc
                else if(cfBeforeW.getPreferredSize()!=null)prevheight=cfBeforeW.getPreferredSize().height;//but in case of any problem try also prefsize
                else if(cfBeforeW.getMinimumSize()!=null)prevheight=cfBeforeW.getMinimumSize().height;//and min size
                y=Math.max(y, loc.y+prevheight);
            }
            y+=20;
            if(y<100)y=100;
            int width=150;
            int height=operandsContainer.getChildren().size()*40;
            if(height<50)height=50;
            bounds=new Rectangle(x,y,width,height);
            int ioHeight=height/operandInCf.size();
            for(IInteractionOperand io:operandInCf)
            {
                operandsBounds.put(io,new Rectangle(x,y,width,ioHeight));
                y+=ioHeight;
            }
        }
        //TBD: need to handle all expressions too at least for width, but better for operands too
        //
        if(bounds!=null)
        {
            //expand to have some border around minimum sizes
            bounds.width+=80;
            bounds.x-=60;
            bounds.y-=15;
            bounds.height+=20;
            //correct upper side
            if(bounds.y<=5)bounds.y=5;//may be good to decrease height also,need to set better upper limit amy be
            //
            bounds= getParentWidget().convertSceneToLocal(bounds);
            setPreferredLocation(bounds.getLocation());
            //setMinimumSize(bounds.getSize());
            setPreferredBounds(new Rectangle(bounds.getSize()));
            //need to set proper position for operands
            IInteractionOperand prevIO=null;
            for(IInteractionOperand io:operandInCf)
            {
                if(prevIO!=null)
                {
                    //normalize gaps between operands
                    Rectangle prevR=operandsBounds.get(prevIO);
                    Rectangle curR=operandsBounds.get(io);
                    int yPrev=prevR.y+prevR.height;
                    int yCur=curR.y;
                    int middle=(yPrev+yCur)/2;
                    if(yCur>yPrev)
                    {
                        //we have some additional space, use most(3/4) for next, to have more space for expressions and operation labels
                        middle=yPrev+(yCur-yPrev)/4;
                    }
                    else
                    {
                        //we have not enouth space, let it be more equally substracted from both operands
                        middle=yPrev+2*(yCur-yPrev)/3;
                    }
                    int dyPrev=middle-yPrev;
                    int dyCur=yCur-middle;
                    prevR.height+=dyPrev;
                    curR.y-=dyCur;
                    curR.height+=dyCur;
                    operandsBounds.put(prevIO, prevR);
                    operandsBounds.put(io, curR);
                    //positioning
                    Widget operandW=operands.get(io);
                    //
                    Rectangle bnd=operandsContainer.convertSceneToLocal(curR);
                    int dyCorrection=getPreferredLocation().y-getLocation().y;//required because validation will not happens in next several steps, store old pref location here
                    operandW.setPreferredLocation(new Point(0,bnd.y-dyCorrection));
                }
                prevIO=io;
            }
        }
        else
        {
            revalidate();
        }
        new AfterValidationExecutor(new ActionProvider() {
            public void perfomeAction() {
                getContainer().calculateChildren(true);;
            }
        }
        , scene);
        scene.validate();
        //if size changed especially but anyway also need to recalculate graphically contained children
        //getContainer().calculateChildren(true);
        //need to do it after all sizes determination if set of containers are positioned, so call outside
    }

    /**
     * show all main labe;s if appropriate 
     * TBD: switch to usage of label manager if will be appropriate
     */
    public void showLabels() {
        for(InteractionOperandWidget ioW:operands.values())
        {
            if(ioW.getOperand().getGuard()!=null && ioW.getOperand().getGuard().getExpression()!=null && ioW.getOperand().getGuard().getExpression().length()>0 && !"[<expression>]".equals(ioW.getOperand().getGuard().getExpression()) && !"<expression>".equals(ioW.getOperand().getGuard().getExpression()))
            ioW.show(LabeledWidget.TYPE.BODY);
        }
    }

    public void setMessageBefore(IMessage msg, Widget msgW) {
        this.messageBefore=msg;
        this.messageBeforeW=(MessageWidget) msgW;
    }

    public void setMessageAfter(IMessage msg, Widget msgW) {
        this.messageAfter=msg;
        this.messageAfterW=(MessageWidget) msgW;
    }

    public void setCombinedFragmentBefore(ICombinedFragment cf, Widget cfW) {
        this.cfBefore=cf;
        this.cfBeforeW=(CombinedFragmentWidget) cfW;
    }

    public void setCombinedFragmentAfter(ICombinedFragment cf, Widget cfW) {
        this.cfAfter=cf;
        this.cfAfterW=(CombinedFragmentWidget) cfW;
    }

     IElementLocator locator = new ElementLocator();
    @Override
    public void load(NodeInfo nodeReader)
    {
        IElement elt = nodeReader.getModelElement();
        if (elt == null)
        {
            elt = locator.findByID(nodeReader.getProject(), nodeReader.getMEID());
        }            
        if (elt != null && (elt instanceof ICombinedFragment || elt instanceof IInteraction))
        {            
            super.load(nodeReader); 
            setPreferredSize(nodeReader.getSize());
        } 
        else if (elt != null && elt instanceof IInteractionOperand)
        {
            //find the proper operand,and set its size and other properties
            InteractionOperandWidget iow = operands.get(elt);
            if (iow != null && iow instanceof DiagramNodeReader)
            {
                IPresentationElement pElt = PersistenceUtil.getPresentationElement(iow);
                nodeReader.setPresentationElement(pElt);
                ((DiagramNodeReader)iow).load(nodeReader);
            }
        }
    }
    
    @Override
    public void loadDependencies(NodeInfo nodeReader) {
        Collection nodeLabels = nodeReader.getLabels();
        //do we have any node labels here? guess not..
//        System.out.println(" NodeLabels = "+nodeLabels.toString());
    }
    
}
