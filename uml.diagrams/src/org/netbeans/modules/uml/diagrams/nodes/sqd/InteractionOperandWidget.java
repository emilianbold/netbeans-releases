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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.nodes.LabeledWidget;
import org.netbeans.modules.uml.diagrams.nodes.MovableLabelWidget;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.SubWidget;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author sp153251
 */
public class InteractionOperandWidget extends Widget implements DiagramNodeWriter, DiagramNodeReader,LabeledWidget,SubWidget {

    private final int HITAREAWIDTH=5;//really width is 2*HITAREAWIDTH, HITAREAWIDTH itself is one side hit border
    
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    IInteractionOperand operand;
    InteractionOperandConstraintWidget constraint;

    public InteractionOperandWidget(Scene scene) {
        super(scene);
    }

    public void initialize(IElement attr) {
        IInteractionOperand oper=(IInteractionOperand) attr;
        this.operand = oper;
        if(constraint!=null && constraint.getParentWidget()!=null)
        {
            removeChild(constraint);
        }
        constraint=new InteractionOperandConstraintWidget(getScene(),operand.createGuard());
        setCheckClipping(true);
        constraint.setMinimumSize(new Dimension(30,10));
        constraint.setVisible(false);
        addChild(constraint);
        constraint.initialize(operand.createGuard());
        constraint.revalidate(); 
        addPresentation(attr);
        setLayout(new Layout(){
        
            public void layout(Widget widget) {
               LayoutFactory.createAbsoluteLayout().layout(widget);
            }

            public boolean requiresJustification(Widget widget) {
                return widget.getFont()!=null;
            }

            public void justify(Widget widget) {
                LayoutFactory.createAbsoluteLayout().justify(widget);
                 Rectangle rec=getClientArea();
                 for(Widget i:widget.getChildren())
                 {
                     if(i instanceof InteractionOperandConstraintWidget && i.isVisible())
                     {
                         Rectangle rec2=i.getBounds();
                         if(rec2==null)rec2=new Rectangle();
                         i.resolveBounds(new Point(rec.x+rec.width/2-rec2.width/2, -rec2.y),null);
                     }
                 }
            }
            
        });
    }

    public IInteractionOperand getOperand() {
        return operand;
    }

    @Override
    public boolean isHitAt(Point localLocation) {
        return super.isHitAt(localLocation);
    }

    @Override
    public Lookup getLookup()
    {
        return lookup;
    }

    
    /**
     * Paints the line widget.
     */
    @Override
    protected void paintWidget() {
        if(getLocation().y>0)//only non-first are required to draw
        {
            Graphics2D gr = getGraphics();
            int x;
            int y;
            //check graphics
            AffineTransform transform = gr.getTransform();
            double zoom = Math.sqrt(transform.getScaleX() * transform.getScaleX() + transform.getShearY() * transform.getShearY());
            //
            Rectangle rec = getClientArea();
            Paint foreground = getForeground();
            Color color = (Color) foreground;
            gr.setColor(color);
            Stroke oldStroke = null;
            if (zoom > 0.1) {
                BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{15.0f, 5.0f}, 0);
                oldStroke = gr.getStroke();
                gr.setStroke(stroke);
            }
            gr.drawLine(rec.x, rec.y, rec.x + rec.width-2, rec.y);//2 pixels for uncounted 1px border width
            if (zoom > 0.1)
                gr.setStroke(oldStroke);
        }
        else super.paintWidget();
    }

    public TYPE[] getAllTypes() {
        return new TYPE[]{TYPE.BODY};
    }

    public boolean isShown(TYPE type) {
        switch(type)
        {
        case BODY:
            return constraint.isLabelVisible();
        default:
            return false;
        }
    }
    
    public MovableLabelWidget getLabel()
    {
        return constraint.getLabel();
    }
    
    public void show(TYPE type) {
        //TBD swicth to usage of label manager
        switch(type)
        {
        case BODY:
            //constraint.setVisible(true);sur
            constraint.showLabel();
            //constraint.switchToEditMode();
            break;
        default:
        }
    }

    public void hide(TYPE type) {
        switch(type)
        {
        case BODY:
            //constraint.setVisible(false);
            constraint.hideLabel();
            break;
        default:
        }
    }
    private void addPresentation(IElement element)
    {
        Scene scene = getScene();
        if (scene instanceof ObjectScene)
        {
            IPresentationElement presentation = createPresentationElement();
            presentation.addSubject(element);
            
            ObjectScene objectScene = (ObjectScene)scene;
            objectScene.addObject(presentation, this);
        }

    }
    private IPresentationElement createPresentationElement()
    {
        IPresentationElement retVal = null;
        
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if(factory != null)
        {
           Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;    
           }
        }
        
        return retVal;
    }

    public void save(NodeWriter nodeWriter) {
        setNodeWriterValues(nodeWriter, this);
        nodeWriter.beginGraphNodeWithModelBridge();
        nodeWriter.beginContained();
        //write contained
        saveChildren(this, nodeWriter);
        nodeWriter.endContained();     
        //write dependencies for this node
        if(this.getDependencies().size() > 0) 
        {
            PersistenceUtil.saveDependencies(this, nodeWriter);
        }
        nodeWriter.endGraphNode();
    }

    public void saveChildren(Widget widget, NodeWriter nodeWriter) {
        //we are not interested in its children.. we just want dependencies (movablelabelwidgets)
//        if (widget == null || nodeWriter == null)
//            return;
//        
//        List<Widget> widList = widget.getChildren();
//        for (Widget child : widList) {
//            if (child instanceof DiagramNodeWriter) {
//                ((DiagramNodeWriter) child).save(nodeWriter);
//            } else {
//                saveChildren(child, nodeWriter);
//            }
//        }
    }
    
    public void addContainedChild(Widget widget) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void load(NodeInfo nodeReader) {
        //get all the properties
        Hashtable<String, String> props = nodeReader.getProperties();        //
        if(nodeReader.getPosition()!=null)
            setPreferredLocation(nodeReader.getPosition());
        if (nodeReader.getSize() != null)
        {
            setPreferredSize(nodeReader.getSize());
        }
    }

    public void loadDependencies(NodeInfo nodeReader)
    {
        Collection nodeLabels = nodeReader.getLabels();
        for (Iterator it = nodeLabels.iterator(); it.hasNext();)
        {
            NodeInfo.NodeLabel nodeLabel = (NodeInfo.NodeLabel) it.next();            
            this.show(LabeledWidget.TYPE.BODY);
            MovableLabelWidget label = this.getLabel();
            if (label != null)
            {
                if (nodeLabel.getPosition() != null)
                {
                    ((UMLLabelWidget)label).addPersistenceProperty(UMLNodeWidget.LOCATION, nodeLabel.getPosition());
                    UMLNodeWidget cf = PersistenceUtil.getParentUMLNodeWidget(this);
                    if (cf != null)
                    {
                        label.addPersistenceProperty(UMLNodeWidget.GRANDPARENTLOCATION, cf.getPreferredLocation());
                    }
                    label.setPreferredLocation(nodeLabel.getPosition());
                }
                else if (nodeLabel.getPosition() == null) //we need default location for TSLoading
                {
                    label.addPersistenceProperty(UMLNodeWidget.GRANDPARENTLOCATION, UMLNodeWidget.DEFAULT);
                }
                label.refresh(false);
            }
        }
//        System.out.println(" NodeLabels = " + nodeLabels.toString());
    }
            
    protected void setNodeWriterValues(NodeWriter nodeWriter, Widget widget) {
        nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, widget);
        nodeWriter.setHasPositionSize(true);
        PersistenceUtil.populateProperties(nodeWriter, widget);
    }
    
    
    @Override
    protected void notifyAdded () 
    {
        if(getLabel()==null || !getLabel().isVisible())return;//invisible/unadded labels don't need to be adjusted
        final Widget cf=Util.getParentByClass(this, CombinedFragmentWidget.class);
        if(cf==null || cf.getParentWidget()==null || getParentWidget()==null)return;//if cf absent or not yet added
        if(cf.getParentWidget()==getLabel().getParentWidget())return;//if label already on correct layer
        // this is invoked when this widget or its parent gets added, only need to
        // process the case when this widget is changed, same for notifyRemoved to 
        // avoid concurrent modification to children list
                getLabel().removeFromParent();
                int index=cf.getParentWidget().getChildren().indexOf(cf);
                cf.getParentWidget().addChild(index + 1, getLabel());
    }
    @Override
    protected void notifyRemoved()
    {
        MovableLabelWidget labelWidget=getLabel();
        if (labelWidget != null)
        {
            if(getParentWidget() == null)
            {           
                labelWidget.removeFromParent();
            }
            else
            {
                Widget cf=Util.getParentByClass(this, CombinedFragmentWidget.class);
                if(cf==null || cf.getParentWidget()==null)
                {
                    labelWidget.removeFromParent();
                }
            }
        }
    }
}
