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

import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.diagrams.anchors.ArrowWithCrossedCircleAnchorShape;
import org.netbeans.modules.uml.diagrams.anchors.TargetMessageAnchor;
import org.netbeans.modules.uml.diagrams.nodes.CommentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineLineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.ExConnectProvider;
import org.netbeans.modules.uml.drawingarea.actions.ExConnectWithLocationProvider;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;

/**
 * redefined to use starting point
 * TBD consider extend of common action for messages and other links
 */
public class ConnectAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private ConnectDecorator decorator;
    private Widget interractionLayer;
    private ExConnectProvider provider;

    private ConnectionWidget connectionWidget = null;
    private Widget sourceWidget = null;
    private Widget targetWidget = null;
    private Point startingPoint = null;
    
    private int type;//type of created message (?if message?), see BaseElement for keys
    private boolean targetExistButNotValid=false;
    private AnchorShape standartShape=AnchorShapeFactory.createArrowAnchorShape(45, 12);
    private AnchorShape notValidTargetShape=new ArrowWithCrossedCircleAnchorShape(12,20,0);
    /**
     * 
     * @param decorator
     * @param interractionLayer
     * @param provider
     * @param type
     */
    public ConnectAction (ConnectDecorator decorator, Widget interractionLayer, ExConnectProvider provider,int type) {
        this.decorator = decorator;
        this.interractionLayer = interractionLayer;
        this.provider = provider;
        this.type=type;
    }

    @Override
    public State drop(Widget widget, WidgetDropTargetDropEvent event) {
        return State.CONSUMED;
    }

    protected boolean isLocked () {
        return sourceWidget != null;
    }

    @Override
    public WidgetAction.State mousePressed (Widget widget, WidgetAction.WidgetMouseEvent event) 
    {
        if (isLocked ())
            return WidgetAction.State.createLocked (widget, this);
        return mousePressedCore (widget, event);
    }
    
    protected State mousePressedCore (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
            boolean isSource=provider instanceof ExConnectWithLocationProvider ? ((ExConnectWithLocationProvider)provider).isSourceWidget(widget, widget.convertLocalToScene(event.getPoint ())) : provider.isSourceWidget (widget);
            if (isSource) {
                sourceWidget = widget;
                targetWidget = null;
                startingPoint = new Point (event.getPoint ());

                connectionWidget = decorator.createConnectionWidget (interractionLayer.getScene ());
                connectionWidget.setTargetAnchorShape (standartShape);
                assert connectionWidget != null;
                if(type==BaseElement.MK_SYNCHRONOUS || type==BaseElement.MK_ASYNCHRONOUS || type==BaseElement.MK_CREATE)
                {
                    Point tmp=widget.convertLocalToScene (event.getPoint ());
                    if(widget instanceof LifelineWidget)
                    {
                        Point widgetSceneLocation=widget.getParentWidget().convertLocalToScene(widget.getLocation());
                        tmp.x=widgetSceneLocation.x+widget.getBounds().x+widget.getBounds().width/2;//lifeline center
                    }
                    else if(widget instanceof CombinedFragmentWidget)
                    {
                        Rectangle rec=widget.getBounds();
                        rec.x+=widget.getBorder().getInsets().left;
                        rec.width-=widget.getBorder().getInsets().left+widget.getBorder().getInsets().right;
                        rec=widget.convertLocalToScene(rec);
                        tmp.x=tmp.x<(rec.x+rec.width/2) ? rec.x : (rec.x+rec.width);
                    }
                    else
                    {
                        throw new UnsupportedOperationException("Unknown case");
                    }
                    connectionWidget.setSourceAnchor (decorator.createFloatAnchor(tmp));
                }
                else connectionWidget.setSourceAnchor (decorator.createSourceAnchor (widget));
                interractionLayer.addChild (connectionWidget);

                return WidgetAction.State.createLocked(widget, this);
            }
            else
            {
                ((DesignerScene)(widget.getScene())).getContextPaletteManager().selectionChanged(widget.convertLocalToScene(event.getPoint()));
                ((DesignerScene)(widget.getScene())).setActiveTool(DesignerTools.SELECT);//??TBD is it always select before usage?, may it be restored in selectionChanged?
            }
        }
        else if(event.getButton () == MouseEvent.BUTTON3)
        {
            if(isLocked ())cancel ();//TBD check why don't work correctly
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    @Override
    public WidgetAction.State mouseReleased (Widget widget, WidgetAction.WidgetMouseEvent event) {
        if(connectionWidget==null)
        {
            //press was blocked
            cancel();
            return State.CONSUMED;
        }
        
        Point point = event.getPoint();
        
        boolean state = move (widget, point);
        final Point finishPnt= widget.convertLocalToScene(point);
        final Point startingPnt= sourceWidget.convertLocalToScene(startingPoint);
        if ((state) && (event.getButton () == MouseEvent.BUTTON1))
        {
            if (targetWidget != null)
            {
                if (Math.abs (startingPoint.x - point.x) >= MIN_DIFFERENCE  ||  Math.abs (startingPoint.y - point.y) >= MIN_DIFFERENCE)
                {
                    if(provider instanceof ExConnectWithLocationProvider)
                    {
                        ArrayList<ConnectionWidget> messageWs=(ArrayList<ConnectionWidget>) ((ExConnectWithLocationProvider)provider).createConnection(sourceWidget, targetWidget, startingPnt,finishPnt);
                    }
                    else provider.createConnection (sourceWidget, targetWidget);
                }
                cancel ();
            }
            else if(targetExistButNotValid)
            {
                cancel ();
            }
            else if(provider.hasTargetWidgetCreator() == true)
            {
                if(provider instanceof ExConnectWithLocationProvider)
                {
                    targetWidget = ((ExConnectWithLocationProvider)provider).createTargetWidget(interractionLayer.getScene(),finishPnt);
                }
                else targetWidget = provider.createTargetWidget(interractionLayer.getScene(),
                                                                sourceWidget, point);
                
                if(targetWidget != null)
                {
                    //need to wait for scene validation, so complex objects can define borders, sizes etc
                    new AfterValidationExecutor(
                            new ActionProvider() {
                                public void perfomeAction() {
                                    Point tmp=targetWidget.getPreferredLocation();
                                    tmp.x=finishPnt.x;
                                    targetWidget.setPreferredLocation(tmp);//set proper x location for new widget
                                    if(provider instanceof ExConnectWithLocationProvider)
                                    {
                                        ((ExConnectWithLocationProvider)provider).createConnection(sourceWidget, targetWidget, startingPnt,finishPnt);
                                    }
                                    else provider.createConnection (sourceWidget, targetWidget);
                                   cancel ();
                                }
                            }
                            ,
                            widget.getScene()
                            
                            );
                            widget.getScene().revalidate();
                            widget.getScene().validate();
               }
            }
            else
            {
                cancel ();
            }
        }
        else
        {
            cancel();
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    private void cancel () {
        sourceWidget = null;
        targetWidget = null;
        startingPoint = null;
        type=0;
        if(connectionWidget!=null)
        {
            connectionWidget.setSourceAnchor (null);
        
            connectionWidget.setTargetAnchor (null);
            interractionLayer.removeChild (connectionWidget);
        }
        connectionWidget = null;
    }

    @Override
    public WidgetAction.State mouseDragged (Widget widget, WidgetAction.WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point point) {
        targetExistButNotValid=false;
        if (sourceWidget != widget || connectionWidget==null)
            return false;
        connectionWidget.setForeground(Color.BLACK);

        Point targetSceneLocation = widget.convertLocalToScene (point);
        targetWidget = resolveTargetWidgetCore (interractionLayer.getScene (), targetSceneLocation);
        //
        if((targetWidget==null || targetWidget instanceof Scene) && targetExistButNotValid)
        {
            connectionWidget.setTargetAnchorShape(notValidTargetShape);
        }
        else
        {
            connectionWidget.setTargetAnchorShape(standartShape);
        }
        //
        if(type==BaseElement.MK_SYNCHRONOUS || type==BaseElement.MK_ASYNCHRONOUS || type==BaseElement.MK_CREATE)
        {
            Anchor sourceAnchor = null;
            Point scenePnt=widget.convertLocalToScene(point);
            Point tmp=new Point(scenePnt);
            Point setStarting=new Point(startingPoint);
            if(sourceWidget instanceof LifelineWidget)
            {
                LifelineLineWidget lllW=((LifelineWidget)sourceWidget).getLine();
                Rectangle bnd=lllW.convertLocalToScene(lllW.getBounds());
                tmp.x=bnd.x+bnd.width/2;//lifeline center
                if(tmp.y<bnd.y)tmp.y=bnd.y;
                else if(tmp.y>(bnd.y+bnd.height))tmp.y=bnd.y+bnd.height;
                setStarting=sourceWidget.convertSceneToLocal(tmp);
            }
            else if(sourceWidget instanceof CombinedFragmentWidget)
            {
                Rectangle bnd=sourceWidget.convertLocalToScene(sourceWidget.getBounds());
                if(tmp.y<bnd.y)tmp.y=bnd.y;
                else if(tmp.y>(bnd.y+bnd.height))tmp.y=bnd.y+bnd.height;
                setStarting.y=sourceWidget.convertSceneToLocal(tmp).y;
            }
            boolean isSource=provider instanceof ExConnectWithLocationProvider ? ((ExConnectWithLocationProvider)provider).isSourceWidget(sourceWidget, sourceWidget.convertLocalToScene(setStarting)) : provider.isSourceWidget (widget);
            if(isSource)
            {
                startingPoint=setStarting;
                sourceAnchor=decorator.createFloatAnchor(sourceWidget.convertLocalToScene(startingPoint));
                connectionWidget.setSourceAnchor(sourceAnchor);
            }
       }
        Anchor targetAnchor = null;
        if (targetWidget != null)
        {
            targetAnchor = decorator.createTargetAnchor (targetWidget);
            if(targetWidget instanceof CombinedFragmentWidget)
            {
                TargetMessageAnchor ta=(TargetMessageAnchor) targetAnchor;
                Point cfPoint=targetWidget.convertSceneToLocal(targetSceneLocation);
                boolean left=cfPoint.x<(targetWidget.getBounds().x+targetWidget.getBounds().width/2);
                ta.combinedFragmentToLeft(left);
            }
        }

        if (targetAnchor == null)
        {
            targetAnchor = decorator.createFloatAnchor (targetSceneLocation);
        }
        connectionWidget.setTargetAnchor (targetAnchor);
        //
        //
        return true;
    }

    private Widget resolveTargetWidgetCore (Scene scene, Point sceneLocation) {
        if (provider != null)
            if (provider.hasCustomTargetWidgetResolver (scene))
                return provider.resolveTargetWidget (scene, sceneLocation);
        targetExistButNotValid=false;
        Point sceneOrigin = scene.getLocation ();
        sceneLocation = new Point (sceneLocation.x + sceneOrigin.x, sceneLocation.y + sceneOrigin.y);
        Widget[] result = new Widget[]{null};
        resolveTargetWidgetCoreDive (result, scene, sceneLocation);
        return result[0];
    }

    private boolean resolveTargetWidgetCoreDive (Widget[] result, Widget widget, Point parentLocation) {
        if (interractionLayer.equals (widget))
            return false;
        Point widgetLocation = widget.getLocation ();
        Point location = new Point (parentLocation.x - widgetLocation.x, parentLocation.y - widgetLocation.y);

        if (! widget.getBounds ().contains (location))
        {
            return false;
        }

        java.util.List<Widget> children = widget.getChildren ();
        for (int i = children.size () - 1; i >= 0; i --) {
            
            if (resolveTargetWidgetCoreDive (result, children.get (i), location))
                return true;
        }
        
        if (! widget.isHitAt (location))
        {
            return false;
        }

        ConnectorState state = null;
        if(provider instanceof ExConnectWithLocationProvider)state=((ExConnectWithLocationProvider)provider).isTargetWidget (sourceWidget, widget,sourceWidget.convertLocalToScene(startingPoint),widget.convertLocalToScene(location));
        else state=provider.isTargetWidget (sourceWidget, widget);
        connectionWidget.setTargetAnchorShape(standartShape);
        if (state == ConnectorState.REJECT)
        {
            if(widget instanceof CommentWidget)
            {
                targetExistButNotValid=true;
            }
            else if(widget instanceof LifelineWidget)
            {
                targetExistButNotValid=true;
            }
            return false;
        }
        if (state == ConnectorState.ACCEPT)
        {
            result[0] = widget;
        }
        return true;
    }
    

    @Override
    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        if (isLocked ()  &&  event.getKeyCode () == KeyEvent.VK_ESCAPE) {
            cancel ();
            return State.CONSUMED;
        }
        return super.keyPressed (widget, event);
    }

}
