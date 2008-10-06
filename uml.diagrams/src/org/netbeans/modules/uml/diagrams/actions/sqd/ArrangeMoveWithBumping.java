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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.ExecutionSpecificationThinWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineBoxWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineLineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.widgets.MessagePin;

/**
 * perfome bumping down and up for messages
 * imporatnt: all lifelineLine children/subchildren etc should be ordered in graphical order
 * all execution expecifications should have zero location and differes by border only and of cause index
 * @author sp153251
 */
public class ArrangeMoveWithBumping extends ArrangeMessagesProvider {

    private MessagePinWidget pin1,pin2;
    private Point originalPosition,newPosition;
    private int gap=5;
    
    private HashMap<MessagePinWidget,TreeSet<MessagePinWidget>> moveCollections;

    /**
     * 
     * @param exSpec1
     * @param exSpec2
     * @param originalPosition
     * @param newPosition
     */
    public ArrangeMoveWithBumping(MessageWidget message,Point originalPosition,Point newPosition) {
        if(message!=null)
        {
            pin1=(MessagePinWidget) message.getSourceAnchor().getRelatedWidget();
            pin2=(MessagePinWidget) message.getTargetAnchor().getRelatedWidget();
            this.originalPosition=originalPosition;
            this.newPosition=newPosition;
        }
        moveCollections=new HashMap<MessagePinWidget,TreeSet<MessagePinWidget>>();
    }

    public void perfomeAction() {
        if(newPosition.y<originalPosition.y)
        {
            moveUp(newPosition.y-originalPosition.y);
        }
        else if(newPosition.y>originalPosition.y)
        {
            moveDown(newPosition.y-originalPosition.y);
        }
    }

    public int moveUp(int shift)
    {
        //
        MessagePinWidget preferredPin=pin1;//try to choose pin with execution specification if possible
        if(preferredPin==null)preferredPin=pin2;
        else if(!(preferredPin.getParentWidget() instanceof ExecutionSpecificationThinWidget) && pin2!=null)preferredPin=pin2;
        return moveUp(preferredPin,shift);
    }
    /*
     * returns real number of pixels it can be moved up/and is moved
     * take into account move up means shift<0
     */
    public int moveUp(MessagePinWidget pin,int shift)
    {
        Widget wgt=pin.getParentWidget();
        if(shift>=0)return 0;//no shift is required for thid message from this processor and no farther processing down too, TBD may be it should be universal method for up and down, TBD later
        if((wgt instanceof ExecutionSpecificationThinWidget) || (wgt instanceof LifelineBoxWidget))
        {
            TreeSet<MessagePinWidget> pins=moveCollections.get(pin);
            if(pins==null)
            {
                pins=new TreeSet<MessagePinWidget>();
                collectPinsToMoveWidgets(pins,pin);
                moveCollections.put(pin, pins);
            }
            int dShift=0;
            MessagePinWidget toBump=null;
            for(MessagePinWidget w:pins)
            {
                int dFr=0;
                toBump=null;
                //need to check if this pin is first, if first check parent
                Widget parent=w.getParentWidget();
                int pinPos=parent.getChildren().indexOf(w);
                if(pinPos==0)
                {
                    if(parent instanceof CombinedFragmentWidget)
                    {
                        dFr=-w.getPreferredLocation().y;
                        //nothing to bump, let for now not to prevent move over
                    }
                    else if(parent.getParentWidget() instanceof LifelineLineWidget)
                    {
                        //find previous execution sppecification
                        int pos=parent.getParentWidget().getChildren().indexOf(parent);
                        if(pos==0)
                        {
                            dFr=-w.getPreferredLocation().y+w.getMarginBefore()+gap;
                        }
                        else if(pos>0)
                        {
                            ExecutionSpecificationThinWidget prevEx=(ExecutionSpecificationThinWidget) parent.getParentWidget().getChildren().get(pos-1);
                            toBump=(MessagePinWidget) prevEx.getChildren().get(0);//bump first pin, so ex spec is bumped as solid
                            Widget lastInPrevW=prevEx.getChildren().get(prevEx.getChildren().size()-1);
                            if(lastInPrevW instanceof MessagePinWidget)
                            {
                                MessagePinWidget lastInPrev=(MessagePinWidget) lastInPrevW;//but determine free space from last pin
                                dFr=lastInPrev.getPreferredLocation().y+lastInPrev.getMarginAfter()-(w.getPreferredLocation().y-w.getMarginBefore())+gap;
                            }
                            else if(lastInPrevW instanceof ExecutionSpecificationThinWidget)
                            {
                                //it currently may happens for asynch message to self only
                                ExecutionSpecificationThinWidget lastInPrev=(ExecutionSpecificationThinWidget)lastInPrevW;
                                dFr=lastInPrev.getPreferredLocation().y+lastInPrev.getBounds().y+lastInPrev.getBounds().height+10+-(w.getPreferredLocation().y-w.getMarginBefore())+gap;//TODO make 10 not hardcoded margin from inner specification
                            }
                        }
                        else
                        {
                            throw new RuntimeException("Got unexpected state");
                        }
                    }
                    else if(parent.getParentWidget() instanceof ExecutionSpecificationThinWidget)
                    {
                        int pos=parent.getParentWidget().getChildren().indexOf(parent);
                        if(pos==0)
                        {
                            dFr=-(parent.getPreferredLocation().y+parent.getClientArea().y)+gap;
                        }
                        else if(pos>0)
                        {
                            Widget prevW=parent.getParentWidget().getChildren().get(pos-1);
                            //toBump=prevEx;//no bump for inner up bounds, because it should be hard to implement logic now
                            dFr=prevW.getPreferredLocation().y+prevW.getClientArea().y+prevW.getClientArea().height-(parent.getPreferredLocation().y+parent.getClientArea().y)+gap;
                        }
                        else
                        {
                            throw new RuntimeException("Got unexpected state");
                        }
                    }
                    else if(parent instanceof LifelineBoxWidget)
                    {
                        dFr=shift;
                    }
                }
                else if(parent instanceof LifelineBoxWidget)
                {
                    dFr=shift;
                }
                else
                {
                    //find prev pin/spec
                    Widget prevW=parent.getChildren().get(pinPos-1);
                    if((pinPos-1)>0)
                    {
                                if(prevW instanceof ExecutionSpecificationThinWidget)
                                {
                                    toBump=(MessagePinWidget) prevW.getChildren().get(0);
                                }
                                else if(prevW instanceof MessagePinWidget)
                                {
                                    toBump=(MessagePinWidget) prevW;
                                }
                    }//no bump of first widget
                   //
                    dFr=prevW.getPreferredLocation().y+prevW.getClientArea().y+prevW.getClientArea().height-w.getPreferredLocation().y+gap;
                }

                if(dFr>shift)
                {
                    //check if bumped will cause move of current also, which may cause loop and stackoverflow
                    TreeSet<MessagePinWidget> tmp=moveCollections.get(toBump);
                    if(toBump!=null && tmp==null)
                    {
                        tmp=new TreeSet<MessagePinWidget>();
                        collectPinsToMoveWidgets(tmp,toBump);
                        moveCollections.put(toBump, tmp);
                    }
                    //need to bump, may be do it here?, may be check if it will be moved too?
                    if(toBump!=null && (tmp==null || !tmp.contains(w)))
                    {
                        int shifted=moveUp(toBump,shift-dFr);
                        dShift=Math.min(shift-dFr-shifted,dShift);
                    }
                    else
                    {
                        //
                        dShift=shift;
                    }
                }
            }
            //
            shift-=dShift;
            for(MessagePinWidget w:pins)
            {
                if(w.getParentWidget() instanceof LifelineBoxWidget)
                {
                    LifelineWidget ll=(LifelineWidget) Util.getParentByClass(w,LifelineWidget.class);
                    Point loc=ll.getPreferredLocation();
                    loc.y+=shift;
                    ll.setPreferredLocation(loc);
                    //correct created lifeline messages
                    correctPindOnWidget(ll.getLine(), -shift);
                    //now bump all messages 
                    //TBD
                    //
                }
                else
                {
                    Point loc=w.getPreferredLocation();
                    loc.y+=shift;
                    w.setPreferredLocation(loc);
                    w.getParentWidget().revalidate();
                }
            }
        }
        else
        {
            //onlu cf-cf case unsupported now
        }
        return shift;
    }
    
    /*
     * returns real number of pixels widget is moved down (not really necessary(because no bottom border) for now but may be later.., now = shift)
     * take into account move down means shift>0
     */
    public int moveDown(int shift)
    {
        MessagePinWidget preferredPin=pin1;//try to choose pin with execution specification if possible
        if(preferredPin==null)preferredPin=pin2;
        else if(!(preferredPin.getParentWidget() instanceof ExecutionSpecificationThinWidget) && pin2!=null)preferredPin=pin2;
        return moveDown(preferredPin,shift);
    }
    /*
     * returns real number of pixels widget is moved down (not really necessary(because no bottom border) for now but may be later.., now = shift)
     * take into account move down means shift>0
     */
    public int moveDown(MessagePinWidget pin,int shift)
    {
        Widget wgt=pin.getParentWidget();
        if(shift<=0)return 0;//no shift is required for thid message from this processor and no farther processing down too, TBD may be it should be universal method for up and down, TBD later
        if((wgt instanceof ExecutionSpecificationThinWidget) || (wgt instanceof LifelineBoxWidget))
        {
            TreeSet<MessagePinWidget> pins=moveCollections.get(pin);
            if(pins==null)
            {
                pins=new TreeSet<MessagePinWidget>();
                collectPinsToMoveWidgets(pins,pin);
                moveCollections.put(pin, pins);
            }
            int dShift=0;
            MessagePinWidget toBump=null;
            for(MessagePinWidget w:pins)
            {
                int dFr=0;
                int wY=w.getPreferredLocation()!=null ? w.getPreferredLocation().y : w.getLocation().y; 
                int currentYWithMargin=wY+w.getMarginAfter();
                //check if it's asynch message to self movent, so have unique margin after
                if(w.getKind()==MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_OUT)
                {
                    if(w.getNumbetOfConnections()>0)
                    {
                        MessagePinWidget opposite=(MessagePinWidget) w.getConnection(0).getTargetAnchor().getRelatedWidget();
                        if(opposite.getParentWidget().getParentWidget()==w.getParentWidget())
                        {
                            //message to self
                            currentYWithMargin+=45;
                        }
                    }
                }
                toBump=null;
                Widget parent=w.getParentWidget();
                if(parent instanceof ExecutionSpecificationThinWidget || parent instanceof LifelineBoxWidget || parent instanceof LifelineLineWidget)
                {
                    //need to check if this pin is first, if first check parent
                    int pinPos=parent.getChildren().indexOf(w);
                    if(pinPos==0)
                    {
                        if(parent instanceof CombinedFragmentWidget)
                        {
                            dFr=parent.getClientArea().y+parent.getClientArea().height-w.getPreferredLocation().y;
                            //nothing to bump, let for now not to prevent move over
                        }
                        else if(parent.getParentWidget() instanceof LifelineLineWidget)
                        {
                            //find next execution sppecification
                            int pos=parent.getParentWidget().getChildren().indexOf(parent);
                            if(pos==(parent.getParentWidget().getChildren().size()-1))
                            {
                                dFr=shift+gap;//free movement
                            }
                            else
                            {
                                ExecutionSpecificationThinWidget nxtEx=(ExecutionSpecificationThinWidget) parent.getParentWidget().getChildren().get(pos+1);//only execution specs
                                toBump=(MessagePinWidget) nxtEx.getChildren().get(0);
                                //
                                dFr=(toBump.getPreferredLocation().y-toBump.getMarginBefore())-currentYWithMargin;//(parent.getPreferredLocation().y+parent.getClientArea().y+parent.getClientArea().height)-gap;
                            }
                        }
                        else if(parent.getParentWidget() instanceof ExecutionSpecificationThinWidget)
                        {
                            int pos=parent.getParentWidget().getChildren().indexOf(parent);
                            if(pos==(parent.getParentWidget().getChildren().size()-1))
                            {
                                dFr=shift;//free movement
                            }
                            else
                            {
                                Widget nxtW=parent.getParentWidget().getChildren().get(pos+1);
                                //
                                if(nxtW instanceof ExecutionSpecificationThinWidget)
                                {
                                    toBump=(MessagePinWidget) nxtW.getChildren().get(0);
                                }
                                else if(nxtW instanceof MessagePinWidget)
                                {
                                    toBump= (MessagePinWidget) nxtW;
                                }
                                dFr=(toBump.getPreferredLocation().y-toBump.getMarginBefore())-currentYWithMargin;//(parent.getPreferredLocation().y+parent.getClientArea().y+parent.getClientArea().height)-gap;
                              }
                        }
                        else if(parent instanceof LifelineBoxWidget)
                        {
                            dFr=shift;
                        }
                    }
                    else if(parent instanceof LifelineBoxWidget)
                    {
                        dFr=shift;
                    }
                    else
                    {
                        //find if last
                        boolean last_pin=pinPos==(parent.getChildren().size()-1);
                        int y_up=Integer.MAX_VALUE;
                        //
                        if(last_pin)//if last it do not bump pins inside this execution specificatio, it may bump next execution specificatio
                        {
                            toBump=null;
                            if(parent instanceof ExecutionSpecificationThinWidget)
                            {
                                y_up=Integer.MAX_VALUE;
                                int y_par=currentYWithMargin;//parent.getPreferredLocation().y+parent.getClientArea().y+parent.getClientArea().height;
                                int parIndex=parent.getParentWidget().getChildren().indexOf(parent);
                                if(parIndex<(parent.getParentWidget().getChildren().size()-1))
                                {
                                    Widget nxtW=parent.getParentWidget().getChildren().get(parIndex+1);
                                     if(nxtW instanceof MessagePinWidget)toBump=(MessagePinWidget) nxtW;
                                    else toBump=(MessagePinWidget) nxtW.getChildren().get(0);//it can be only pin, or ex spec
                                    y_up=toBump.getPreferredLocation().y-toBump.getMarginBefore();
                               }
                                else
                                {
                                    //nothing to bump
                                }
                                dFr=y_up-y_par-gap;
                            }
                            else
                            {
                                //throw new RuntimeException("Unsupported case, expected ex specifications only here");
                            }
                        }
                        else
                        {
                            Widget nxtW=parent.getChildren().get(pinPos+1);
                            if(nxtW instanceof ExecutionSpecificationThinWidget)
                            {
                                toBump=(MessagePinWidget) nxtW.getChildren().get(0);
                            }
                            else
                            {
                                toBump=(MessagePinWidget) nxtW;
                            }
                            y_up=toBump.getPreferredLocation().y-toBump.getMarginBefore();
                            //
                            dFr=y_up-w.getPreferredLocation().y-gap;
                        }
                     }
                }
                else
                {
                    //only combinedfragment lefts, move down without limitation
                    dFr=shift;
                }
                if(toBump!=null && toBump.getParentWidget()==parent.getParentWidget() && toBump.getKind()==MessagePin.PINKIND.SYNCHRONOUS_RETURN_IN && w.getKind()==MessagePin.PINKIND.SYNCHRONOUS_RETURN_OUT)
                {
                    toBump=null;//to self and source of result try to bump target
                    dFr=shift;
                }
                if(dFr<shift)
                {
                    //need to bump
                    if(toBump!=null){
                        int shifted=moveDown(toBump,shift-dFr);
                        dShift=Math.max(shift-dFr-shifted,dShift);
                    }
                    else
                    {
                        //nothin to bump, freee movement
                        dShift=shift;
                    }
                }
            }
            //
            shift-=dShift;
            for(MessagePinWidget w:pins)
            {
                if(w.getParentWidget() instanceof LifelineBoxWidget)
                {
                    LifelineWidget ll=(LifelineWidget) Util.getParentByClass(w,LifelineWidget.class);
                    Point loc=ll.getPreferredLocation();
                    loc.y+=shift;
                    ll.setPreferredLocation(loc);
                    //correct created lifeline messages
                    correctPindOnWidget(ll.getLine(), -shift);
                    //now bump messages 
                    if(ll.getLine().getChildren().size()>0)
                    {
                        MessagePinWidget firstPin=(MessagePinWidget) ll.getLine().getChildren().get(0).getChildren().get(0);
                        int distance=firstPin.getPreferredLocation().y-firstPin.getMarginBefore();
                        if(distance<gap)
                        {
                            moveDown(firstPin, gap-distance);
                        }
                    }
                    //
                }
                else
                {
                    Point loc=w.getPreferredLocation();
                    loc.y+=shift;
                    w.setPreferredLocation(loc);
                    w.getParentWidget().revalidate();
                }
            }
        }
        else
        {
            //onlu cf-cf case unsupported now
        }
        return shift;
    }
        
    /**
     * return collection of all messages moved after class creation
     * or after class resetting
     * @return
     */
    public ArrayList<MessageWidget> getAllMovedMessages()
    {
        ArrayList<MessageWidget> movedMsgs=new ArrayList<MessageWidget>();
        for(TreeSet<MessagePinWidget> ts:moveCollections.values())
        {
            for(MessagePinWidget mp:ts)
            {
                if(mp.getNumbetOfConnections()>0 && mp.getConnection(0) instanceof MessageWidget && !movedMsgs.contains(mp.getConnection(0)))
                {
                    movedMsgs.add((MessageWidget) mp.getConnection(0));
                }
            }
        }
        return movedMsgs;
    }

    /**
     * starting from current widget goes by messages and fin all connected psecifications, lifeline boxes, combined fragments which should be moved together
     * all pins to move as solid, in real somtimes parent widget will be moved, sometimes pins itself and should be checked if it possible to move and how much
     * @param widgets store serarch result here, store widget and pin from which connection is cunted, store in array because several messages may go to the same widget
     * @param currentWgt start count from currentWidget
     * @param startingPin, start count from starting pin (for example if we start from retun message pin we will not include call pin in calculation because we will move only return message, if we'll start from first call pin we wil include all othe pins
     */
    public static void collectPinsToMoveWidgets(TreeSet<MessagePinWidget> widgets,MessagePinWidget startingPin)
    {
        if(widgets.contains(startingPin))return;//this widget was processed
        widgets.add(startingPin);//starting always moves, exclution for LifelineBox will be handled in move method, for execution specification and combimned fragment it works
        Widget currentWidget=startingPin.getParentWidget();
        if(currentWidget instanceof LifelineBoxWidget)
        {
            //no move of pins on this widget, but move of ins on connected widget
            //fins out connected widget
            MessageWidget mssg=(MessageWidget) startingPin.getConnection(0);
            MessagePinWidget nextPin=(MessagePinWidget) mssg.getSourceAnchor().getRelatedWidget();
           //
            collectPinsToMoveWidgets(widgets,nextPin);
        }
        else if(currentWidget instanceof CombinedFragmentWidget)
        {
            //this method is used for drag by exzecution spec or message(drag of cf will not change vertical position of messages), so only starting pin is moved
            //may need correction after testing
        }
        else if(currentWidget instanceof ExecutionSpecificationThinWidget)
        {
            if(startingPin.getParentWidget().getChildren().indexOf(startingPin)==0)
            {
                //this widget is starting, all widgets within this specification and child specification should be moved
                for(Widget child:currentWidget.getChildren())
                {
                    if(child instanceof MessagePinWidget)
                    {
                        widgets.add((MessagePinWidget)child);
                        //and opposite
                        MessageWidget ms=(MessageWidget) ((MessagePinWidget)child).getConnection(0);
                        MessagePinWidget tmp1=(MessagePinWidget) ms.getSourceAnchor().getRelatedWidget();
                        MessagePinWidget tmp2=(MessagePinWidget) ms.getTargetAnchor().getRelatedWidget();
                        if(tmp1==child)collectPinsToMoveWidgets(widgets,tmp2);
                        else collectPinsToMoveWidgets(widgets,tmp1);
                    }
                    else if (child instanceof ExecutionSpecificationThinWidget)
                    {
                        collectPinsToMoveWidgets(widgets,(MessagePinWidget) child.getChildren().get(0));//first alway should be pin
                    }
                }
            }
            else
            {
                MessageWidget ms=(MessageWidget) ((MessagePinWidget)startingPin).getConnection(0);
                MessagePinWidget tmp1=(MessagePinWidget) ms.getSourceAnchor().getRelatedWidget();
                MessagePinWidget tmp2=(MessagePinWidget) ms.getTargetAnchor().getRelatedWidget();
                if(tmp1==startingPin)collectPinsToMoveWidgets(widgets,tmp2);
                else collectPinsToMoveWidgets(widgets,tmp1);
            }
            //else if(startingPin.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN || startingPin.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT)
            {
                //need to find corresponding return pin and move too, and move also all between
            }
        }
    }
    
    /**
     * just moves all pins and therefore all messages/execution specification on specified widget with all children
     * @param root starting widget
     * @param dy correction value
     */
    public static void correctPindOnWidget(Widget root,int dy)
    {
        for(Widget w:root.getChildren())
        {
            if(w instanceof MessagePinWidget)
            {
                Point loc=w.getPreferredLocation();
                loc.y+=dy;
                w.setPreferredLocation(loc);
            }
            else
            {
                correctPindOnWidget(w, dy);
            }
        }
    }
}
