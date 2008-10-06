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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.ui.trackbar.JTrackBar;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;

/**
 * use external provider for mopst actions and made adjustments cf specific
 * @author sp153251
 */
public class CombinedFragmentMoveProvider  implements MoveProvider {

                private AlignWithMoveStrategyProvider provider;
                private JTrackBar tb;
                
                public CombinedFragmentMoveProvider(AlignWithMoveStrategyProvider baseProvider)
                {
                    provider=baseProvider;
                }
                
                public void movementStarted(Widget widget) {
                    provider.movementStarted(widget);
                    if(tb==null)
                    {
                        DesignerScene scene=(DesignerScene) widget.getScene();
                        tb=((SQDDiagramTopComponent) scene.getEngine().getTopComponent()).getTrackBar();
                    }
                }

                public void movementFinished(final Widget widget) {
                    provider.movementFinished(widget);
                    //containment is checked at final step only
                    //
                    CombinedFragmentWidget cfW=(CombinedFragmentWidget) widget;
                    final ContainerWidget finalcontainer=cfW.getContainer();
                    processAllVerifications(widget,finalcontainer);
                    widget.getScene().validate();
                    tb=null;
                }

                public Point getOriginalLocation(Widget widget) {
                    return provider.getOriginalLocation(widget);
                }

                public void setNewLocation(final Widget widget, Point location) {
                    provider.setNewLocation(widget, location);
                    if(provider.isMovementInitialized())processAllVerifications(widget,null);
                    widget.getScene().validate();
                 }
                
                private void verifyCFContentLifelines(Widget widget)
                {
                    CombinedFragmentWidget cfW=(CombinedFragmentWidget) widget;
                    if(cfW.getContainer()==null)return;
                    DesignerScene scene=((DesignerScene) widget.getScene());
                    //
                    Point contPoint60=cfW.getContainer().convertSceneToLocal(new Point(0,60));
                    boolean changed=false;
                    for(Widget child:cfW.getContainer().getChildren())
                    {
                        if(child instanceof LifelineWidget)
                        {
                            LifelineWidget l=(LifelineWidget)child;
                            Point loc=l.getPreferredLocation();
                            Rectangle rec=l.getPreferredBounds();
                            rec.x+=l.getBorder().getInsets().left;
                            rec.y+=l.getBorder().getInsets().top;
                            rec.width-=l.getBorder().getInsets().left+l.getBorder().getInsets().right;
                            rec.height-=l.getBorder().getInsets().top+l.getBorder().getInsets().bottom;
                             //all simple lifelines(not created) should be on top, TBD check fo created
                            int y_border_shift=0;//l.getBorder().getInsets().top+l.getBounds().y;
                            if(!l.isCreated())
                            {
                                if(loc.y!=(contPoint60.y-y_border_shift) && !l.isActorLifeline())
                                {
                                    changed=true;
                                    loc.y=contPoint60.y-y_border_shift;//TBD where to store this number, may be it's not necessary to use layout at all (used also in creation when link is drawn)
                                }
                                else if(loc.y!=(contPoint60.y-40-y_border_shift) && l.isActorLifeline())
                                {
                                    changed=true;
                                   loc.y=contPoint60.y-40-y_border_shift;//TBD where to store this number, may be it's not necessary to use layout at all (used also in creation when link is drawn)
                                }
                            }
                            else
                            {
                                //correct created lifelines in separate handler
                            }
                            if(changed)l.setPreferredLocation(loc);
                            IPresentationElement el=(IPresentationElement) scene.findObject(l);
                            if(tb!=null)tb.moveObject(el);
                        }
                        else if(child instanceof CombinedFragmentWidget)
                        {
                            verifyCFContentLifelines(child);//TBD??? need to do it after validation?
                        }
                    }
                    //
                    scene.validate();
                }
                private void verifyCFContentPins(Widget widget)
                {
                    CombinedFragmentWidget cfW=(CombinedFragmentWidget) widget;
                    if(cfW.getContainer()==null)return;
                    DesignerScene scene=((DesignerScene) widget.getScene());
                    //
                    for(Widget child:cfW.getContainer().getChildren())
                    {
                        if(child instanceof CombinedFragmentWidget)
                        {
                            verifyCFContentPins(child);//TBD??? need to do it after validation?
                        }
                    }
                    //need to adjust pins position, we consider we have now only horizontal messages
                    for(Widget child:cfW.getMainWidget().getChildren())
                    {
                        if(child instanceof MessagePinWidget)
                        {
                            MessagePinWidget cfPin=(MessagePinWidget) child;
                            MessageWidget message=(MessageWidget) cfPin.getConnection(0);//no we have 1pin to 1 message relation
                            MessagePinWidget pinOpposite= (MessagePinWidget) (message.getSourceAnchor().getRelatedWidget()==cfPin ? message.getTargetAnchor().getRelatedWidget() : message.getSourceAnchor().getRelatedWidget());
                            Point sceneLocation=pinOpposite.getParentWidget().convertLocalToScene(pinOpposite.getLocation());
                            Point needCfPinLocation=cfW.getMainWidget().convertSceneToLocal(sceneLocation);
                            needCfPinLocation.x=cfPin.getPreferredLocation().x;
                            if(needCfPinLocation.y<0)needCfPinLocation.y=0;
                            cfPin.setPreferredLocation(needCfPinLocation);
                        }
                    }
                    //
                    scene.validate();
                }
                private void verifyCreatedLifelines(Widget widget)
                {
                    CombinedFragmentWidget cfW=(CombinedFragmentWidget) widget;
                    if(cfW.getContainer()==null)return;
                    DesignerScene scene=((DesignerScene) widget.getScene());
                    //
                    Point contPoint60=cfW.getContainer().convertSceneToLocal(new Point(0,60));
                    boolean changed=false;
                    for(Widget child:cfW.getContainer().getChildren())
                    {
                        if(child instanceof LifelineWidget)
                        {
                            LifelineWidget l=(LifelineWidget)child;
                            Point loc=l.getPreferredLocation();
                            if(l.isCreated())
                            {
                                MessagePinWidget pinIn=null;
                                for(Widget w:l.getBox().getChildren())
                                {
                                    if(w instanceof MessagePinWidget)
                                    {
                                        pinIn=(MessagePinWidget) w;
                                        break;
                                    }
                                }
                                MessageWidget msgW=(MessageWidget) pinIn.getConnection(0);
                                MessagePinWidget pinOut=(MessagePinWidget) msgW.getSourceAnchor().getRelatedWidget();
                                Point inLoc=msgW.getTargetAnchor().compute(msgW.getTargetAnchorEntry()).getAnchorSceneLocation();
                                Point outLoc=pinOut.getParentWidget().convertLocalToScene(pinOut.getPreferredLocation());
                                int dy=outLoc.y-inLoc.y;
                                if(dy!=0)
                                {
                                    loc.y+=dy;
                                    changed=true;
                                }
                            }
                            if(changed)l.setPreferredLocation(loc);
                            IPresentationElement el=(IPresentationElement) scene.findObject(l);
                            if(tb!=null)tb.moveObject(el);
                        }
                        else if(child instanceof CombinedFragmentWidget)
                        {
                            verifyCreatedLifelines(child);//TBD??? need to do it after validation?
                        }
                    }
                    //
                    scene.validate();
                }

                private void processAllVerifications(final Widget widget,final ContainerWidget finalcontainer)
                {
                        new AfterValidationExecutor(new ActionProvider(){
                            public void perfomeAction() {
                                verifyCFContentLifelines(widget);
                                widget.revalidate();
                                new AfterValidationExecutor(new ActionProvider(){
                                    public void perfomeAction() {
                                        verifyCFContentPins(widget);
                                        widget.revalidate();
                                        new AfterValidationExecutor(new ActionProvider(){
                                            public void perfomeAction() {
                                                verifyCreatedLifelines(widget);
                                                widget.revalidate();
                                                if(finalcontainer!=null)new AfterValidationExecutor(new ActionProvider(){
                                                    public void perfomeAction() {
                                                        finalcontainer.calculateChildren(true);
                                                        widget.revalidate();
                                                        widget.getScene().validate();
                                                    }
                                                },widget.getScene());
                                                widget.revalidate();
                                                widget.getScene().validate();
                                           }
                                        },widget.getScene());
                                        widget.revalidate();
                                        widget.getScene().validate();
                                    }
                                },widget.getScene());
                                widget.revalidate();
                                widget.getScene().validate();
                            }
                        },widget.getScene());
                        widget.revalidate();
                        widget.getScene().validate();
                }

}
