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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.behavior.IDestroyAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IActionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.diagrams.actions.sqd.ArrangeMoveWithBumping;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.nodes.ActorSymbolWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.ui.trackbar.JTrackBar;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import java.util.TreeSet;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.modules.uml.diagrams.actions.sqd.AdjustAfterBoxChangeProvider;
import org.netbeans.modules.uml.diagrams.actions.sqd.LifelineResizeProvider;
import org.netbeans.modules.uml.diagrams.engines.SequenceDiagramEngine;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.ResizeStrategyProvider;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;

/**
 *
 * @author sp153251
 */
public class LifelineWidget extends UMLNodeWidget implements PropertyChangeListener {

    private boolean selected=false;
    
    private LifelineBoxWidget boxWidget;
    private LifelineLineWidget lineWidget;
    private ActorSymbolWidget actorWidget;
    private Widget all;
    //
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);

    private JTrackBar tb;
    private boolean isActorLifeline;
    private Font prevFont;
    private ResourceType[] customizableResTypes = Customizable.DEFAULT_RESTYPES;
    
    private boolean isShowWidget;
    
    public LifelineWidget(Scene scene) {
       this(scene,"defNM","defCL");
    }
    public LifelineWidget(Scene scene,String name,String classifier) {
        super(scene,false);
        //setLayout(new LifelineLayout());
        //
        lookupContent.add(initializeContextPalette());
        lookupContent.add(new DefaultWidgetContext("Lifeline"));
        ResourceValue.initResources(getWidgetID(), this);
    }
    
    @Override
    public Lookup getLookup()
    {
        return lookup;
    }    
    
    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        //setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY,0));
        
        // The ActorSymbolWidget will override the display name.
        actorWidget=new ActorSymbolWidget(getScene(),
                                          getWidgetID()+".stickfigure", 
                                          "");
        
        actorWidget.setMinimumSize(new Dimension(10,SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y-SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y));
        actorWidget.setMaximumSize(new Dimension(50,SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y-SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y));
        actorWidget.setPreferredSize(new Dimension(40,SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y-SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y));
        boxWidget=new LifelineBoxWidget(getScene(),"","");
        boxWidget.setForeground(null);
        boxWidget.setBackground(null);
        boxWidget.setFont(null);
        lineWidget=new LifelineLineWidget(getScene());
        setBackground(null);
        setForeground(null);
        setFont(null);
        all=new Widget(getScene());
        all.setBackground(null);
        all.setForeground(null);
        all.setFont(null);
        all.addChild(actorWidget);
        all.addChild(boxWidget);
        all.addChild(lineWidget,new Integer(1));
        setCurrentView(all);
        all.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY,0));
        //
        all.getParentWidget().getResourceTable().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getNewValue()!=null)
                {
                    if(evt.getNewValue() instanceof Font)
                    {
                        if(evt.getOldValue()==null || ((Font)evt.getOldValue()).getSize()!=((Font)evt.getNewValue()).getSize())
                        {
                            //need to adjust messages on this lifeline
                            new AfterValidationExecutor(new AdjustAfterBoxChangeProvider(LifelineWidget.this), getScene());
                            //font size was changed, need to revalidate box sizes
                            boxWidget.revalidate();
                            all.revalidate();
                            boxWidget.updateLabel();
                            revalidate();
                            getScene().validate();
                            prevFont=((Font)evt.getNewValue());
                        }
                    }
                    else if(evt.getNewValue() instanceof Color)
                    {
                        
                    }
                }
            }
        });
        setMinimumSize(new Dimension(40,140));//initial size
        //
        setCheckClipping(true);
        lineWidget.setCheckClipping(true);
        //
        revalidate();
        
        Lifeline src=(Lifeline) presentation.getFirstSubject();
        String stereotype=src.getAppliedStereotypesAsString(false);//TBD need to be based on alias seting
        boxWidget.updateLabel(presentation);
        if(isShowWidget)
        {
            stereotype="<<sample stereotype>>";
            boxWidget.setClassifier("sampleClassifier");
        }
        boxWidget.setStereotype(stereotype);
        updateDestroyState(src);
        //
        setIsActorLifeline(src.getIsActorLifeline());
        //
        if(!isShowWidget)
        {
            DesignerScene scene=(DesignerScene) getScene();
            final SequenceDiagramEngine engine=(SequenceDiagramEngine) scene.getEngine();
            //
            new AfterValidationExecutor(new ActionProvider() {
                public void perfomeAction() {
                    engine.normalizeLifelines(true, false, null);
                }
            }, getScene());
        }
        
        super.initializeNode(presentation);
    }

    @Override
    public void initializeNode(IPresentationElement element, boolean show) {
        isShowWidget=show;
        if(show)
        {
            Lifeline lifeline=(Lifeline) element.getFirstSubject();
            lifeline.setName("sampleName");
            lifeline.setIsActorLifeline(true);
        }
        initializeNode(element);
    }
    
    
    /**
     * Called to whether a particular location in local coordination system is controlled (otionally also painted) by the widget.
     * @param localLocation the local location
     * @return true, if the location belong to the widget
     */
    @Override
    public boolean isHitAt (Point localLocation) {
        if(getState().isSelected())
        {
            return super.isHitAt(localLocation);// && !lineWidget.isHitChilds(lP);
        }
        else
        {
            Point bP=new Point(localLocation);
            bP.translate(-boxWidget.getLocation().x,-boxWidget.getLocation().y);
            Point lP=new Point(localLocation);
            lP.translate(-lineWidget.getLocation().x,-lineWidget.getLocation().y);
            return boxWidget.isHitAt(bP) || lineWidget.isHitAt(lP);
        }
    }
    
    @Override
    public String toString()
    {
        return "LifelineWidget, some name etc"+super.toString();
    }

    @Override
    protected void paintWidget() {
        super.paintWidget();
    }

    public String getKind() {
        return "Lifeline";
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name=evt.getPropertyName();
        Object value=evt.getNewValue();
        Object old=evt.getOldValue();
        Object source=evt.getSource();
        ILifeline src=null;
        if(source instanceof Lifeline)
        {
            src=(Lifeline) source;
            boolean update=false;
            if(name.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
            {
                boxWidget.updateLabel();
                revalidate();
                update=true;
            }
            else if(name.equals(ModelElementChangedKind.REPRESENTING_CLASSIFIER_CHANGED.toString()))
            {
                boxWidget.updateLabel();
                revalidate();
                update=true;
            }
            else if(name.equals(ModelElementChangedKind.STEREOTYPE.toString()))
            {
                 //need to adjust messages on this lifeline
                 new AfterValidationExecutor(new AdjustAfterBoxChangeProvider(LifelineWidget.this), getScene());
                 boxWidget.setStereotype(src.getAppliedStereotypesAsString(false));//TBD based on aliasing settings
                 update=true;
                 revalidate();
                 //TBD fix bug with messages after stereotype add/remove
            }
            else if(name.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
            {
                updateDestroyState(src);
            }
            if(update)
            {
                new AfterValidationExecutor(new ActionProvider() {
                    public void perfomeAction() {
                       DesignerScene scene=((DesignerScene) getScene());
                        if(tb==null)
                        {
                            tb=((SQDDiagramTopComponent) scene.getEngine().getTopComponent()).getTrackBar();
                        }
                        IPresentationElement el=(IPresentationElement) scene.findObject(LifelineWidget.this);
                        tb.updateName(el);
                        tb.moveObject(el);
                    }
                },getScene());
            }
        }
    }
    
    public boolean isDestroyed()
    {
        return getLine().isDestroyed();
    }
    
    private void updateDestroyState(ILifeline src)
    {
                boolean isdestr=false;
                for(int i=src.getEvents().size()-1;i>=0;i--)
                {
                    IEventOccurrence eo=src.getEvents().get(i);
                    IExecutionOccurrence exO=eo.getStartExec();
                    if(exO instanceof IActionOccurrence && ((IActionOccurrence)exO).getAction() instanceof IDestroyAction )
                    {
                        isdestr=true;
                        break;
                    }
                }
                if(isdestr!=isDestroyed())
                {
                    lineWidget.setDestroyEvent(isdestr);
                    getScene().validate();
                }
    }
    
    //
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = 
                new DefaultContextPaletteModel(this, ContextPaletteModel.FOLLOWMODE.VERTICAL_ONLY);
        paletteModel.initialize("UML/context-palette/Lifeline");
        return paletteModel;
    }
    
    public LifelineLineWidget getLine()
    {
        return lineWidget;
    }
    public LifelineBoxWidget getBox()
    {
        return boxWidget;
    }
    /**
     *  
     *
     * @return return true if lifeline is target of create message
     */
    public boolean isCreated()
    {
        for(Widget i:boxWidget.getChildren())
        {
            if(i instanceof MessagePinWidget)
            {
                MessagePinWidget pin=(MessagePinWidget) i;
                if(pin.getNumbetOfConnections()>0)return true;//addtional check to be sure connection isn't removed but occationally pin remains
            }
        }
        return false;
    }
    
    @Override
    public Dimension getResizingMinimumSize()
    {
        return new Dimension(40,90);//it may be good to determine based on content (i.e. pins for example, text width in label etc)
    }

    @Override
    public void remove() {
        //Lifeline deletion cause messages deletion, so some execution specs may need to be deleted, and child nodes may need to be reconnected
        IPresentationElement node=getObject();
        DesignerScene scene=(DesignerScene) getScene();
        TreeSet<MessagePinWidget> pins=new TreeSet<MessagePinWidget>();//pins to remove
        for (IPresentationElement edge : scene.findNodeEdges(node, true, true))//output edges
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
        //remove trackcar
        SequenceDiagramEngine engine=(SequenceDiagramEngine) scene.getEngine();
        SQDDiagramTopComponent tc=(SQDDiagramTopComponent) engine.getTopComponent();
        tc.getTrackBar().removePresentationElement(node);
        tc.getTrackBar().updateName(node);
        //remove node itself
        scene.removeNode (node);
    }
    
    
    
    public void setIsActorLifeline(boolean isActorLifeline)
    {
        this.isActorLifeline=isActorLifeline;
        actorWidget.setVisible(this.isActorLifeline);
    }
    public boolean isActorLifeline()
    {
        return this.isActorLifeline;
    }        
    
    @Override
    public ResizeStrategyProvider getResizeStrategyProvider()
    {
        return new LifelineResizeProvider(getResizeControlPoints());
    }
    
    @Override
    protected ResizeProvider.ControlPoint[] getResizeControlPoints()
    {
        //by default all sized are active for resize;
        return new ResizeProvider.ControlPoint[]{ResizeProvider.ControlPoint.CENTER_LEFT,ResizeProvider.ControlPoint.BOTTOM_LEFT,ResizeProvider.ControlPoint.BOTTOM_CENTER,ResizeProvider.ControlPoint.BOTTOM_RIGHT,ControlPoint.CENTER_RIGHT};
    }

    public String getWidgetID() {
        return UMLWidgetIDString.LIFELINEWIDGET.toString();
    }

    @Override
    public void refresh(boolean resizetocontent)
    {
        IPresentationElement pe = getObject();
        if (pe != null && pe.getFirstSubject() != null && !pe.getFirstSubject().isDeleted())
        {
            //need to update smth
            boxWidget.updateLabel(pe);
        } else
        {
            remove();
        }
        getScene().validate();
    }

    @Override
    protected void notifyFontChanged(Font font) {
        if(prevFont==null || prevFont.getSize()!=font.getSize())
        {
            // If all == null it means we are getting this event during the 
            // initialization of the node.  We are getting it when we call
            // setFont(null);
            if(all != null)
            {
                //need to adjust messages on this lifeline
                new AfterValidationExecutor(new AdjustAfterBoxChangeProvider(LifelineWidget.this), getScene());
                //font size was changed, need to revalidate box sizes
                boxWidget.revalidate();
                all.revalidate();
                boxWidget.updateLabel();
                revalidate();
                getScene().validate();
            }
        }
        prevFont=font;
    }
   
    @Override
    protected void saveAnchorage(NodeWriter nodeWriter) {
        //write anchor info
        DesignerScene dScene = (DesignerScene)this.getScene();
        // First get only output edges (to have proper src node anchor assigned)
        Collection outEdgeList = dScene.findNodeEdges(this.getObject(), true, false); 
        for (Iterator it = outEdgeList.iterator(); it.hasNext();) {
            IPresentationElement pE = (IPresentationElement)it.next();
            Widget widget = dScene.findWidget(pE);
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget connectionWidget = (ConnectionWidget)(widget);
                Anchor srcAnchor = connectionWidget.getSourceAnchor();
                PersistenceUtil.addAnchor(srcAnchor); // this is to cross ref the anchor ID from the edge later on..                
                nodeWriter.addAnchorEdge(srcAnchor, PersistenceUtil.getPEID(connectionWidget));
            }            
        }
        // Now get all in edges (to have proper target anchor assigned)
        Collection inEdgeList = dScene.findNodeEdges(this.getObject(), false, true); 
        for (Iterator it = inEdgeList.iterator(); it.hasNext();) {
            IPresentationElement pE = (IPresentationElement)it.next();
            Widget widget = dScene.findWidget(pE);
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget connectionWidget = (ConnectionWidget)(widget);
                Anchor targetAnchor = connectionWidget.getTargetAnchor();
                PersistenceUtil.addAnchor(targetAnchor); // this is to cross ref the anchor ID from the edge later on..                
                nodeWriter.addAnchorEdge(targetAnchor, PersistenceUtil.getPEID(connectionWidget));
            }            
        }        
        nodeWriter.writeAnchorage();
        //done writing the anchoredgemap.. now time to clear it.
        nodeWriter.clearAnchorEdgeMap();
    }

    public String getID() {
        return UMLWidget.UMLWidgetIDString.LIFELINEWIDGET.toString();
    }

    public String getDisplayName() {
        return "Default";
    }

    public void update() {
        ResourceValue.initResources(getWidgetID(), this);
    }

    public void setCustomizableResourceTypes (ResourceType[] resTypes) 
    {
        customizableResTypes = resTypes;
    }
    
    public ResourceType[] getCustomizableResourceTypes()
    {
        return customizableResTypes;
    }

    @Override
    public void setNodeBackground(Paint paint)
    {
        getResourceTable().addProperty("LIFELINE.LIFELINEBOX." + ResourceValue.BGCOLOR , paint);
    }

    @Override
    public void setNodeFont(Font f)
    {   
        getResourceTable().addProperty("LIFELINE.LIFELINEBOX." + ResourceValue.FONT , f);
    }

    @Override
    public void setNodeForeground(Color color)
    {
        
        getResourceTable().addProperty("LIFELINE.LIFELINEBOX." + ResourceValue.FGCOLOR , color);
    }
    
    
}
