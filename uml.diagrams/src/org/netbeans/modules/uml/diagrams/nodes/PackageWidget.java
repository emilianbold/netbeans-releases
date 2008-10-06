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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint; 
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.widgets.PolygonConstraints;
import org.netbeans.modules.uml.widgets.PolygonWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Jyothi
 */
public class PackageWidget extends ContainerNode
        implements PropertyChangeListener
{
    private final static double TAB_HEIGHT = .2;
    private final static double TAB_WIDTH = .3;
    
    private final static PolygonConstraints TAB_NAME_CONSTRAINTS =
                            new PolygonConstraints(0, 1, 2, 3,
                                                   PolygonConstraints.VertexWeight.PREFERRED,
                                                   PolygonConstraints.VertexWeight.PREFERRED);
    private final static PolygonConstraints EMPTY_TAB_NAME_CONSTRAINTS =
                            new PolygonConstraints(0, 1, 2, 3,
                                                   PolygonConstraints.VertexWeight.NONE,
                                                   PolygonConstraints.VertexWeight.PREFERRED);
    
    private final static PolygonConstraints BODY_NAME_CONTRAINTS = 
                            new PolygonConstraints(0, 3, 5, 6,
                                                   PolygonConstraints.VertexWeight.NONE,
                                                   PolygonConstraints.VertexWeight.NONE);

    private final static Border BODY_NAME_BORDER = BorderFactory.createOpaqueBorder(UMLNameWidget.BORDER_SIZE,
                                                                             UMLNameWidget.BORDER_SIZE,
                                                                             0,
                                                                             UMLNameWidget.BORDER_SIZE);

    private final static Border TAB_NAME_BORDER = BorderFactory.createOpaqueBorder(UMLNameWidget.BORDER_SIZE,
                                                                             UMLNameWidget.BORDER_SIZE,
                                                                             UMLNameWidget.BORDER_SIZE,
                                                                             UMLNameWidget.BORDER_SIZE);
    
    
    private UMLLabelWidget stereotypeWidget = null;
    private MultiLineTaggedValueWidget taggedValuesWidget = null;
    private EditableCompartmentWidget nameWidget = null;

    private ContainerWidget container = null;
    private IPresentationElement pe;
    public static String BodyNameContainerID = "PackageBody";
    private Widget namePlaceholder = null;
    private Widget bodyNameHolder = null;
    private NameSizeDependency nameDependency = new NameSizeDependency();
    
    public PackageWidget(Scene scene)
    {
        super(scene);
        
        addToLookup(initializeContextPalette());
    }
    
     public PackageWidget(Scene scene, IPresentationElement pe)
    {
        this(scene);
        this.pe = pe;
        initializeNode(pe);
        
        addToLookup(initializeContextPalette());
        
    }
    
    @Override
    public ContainerWidget getContainer()
    {
        return container;
    }
    
    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        Scene scene = getScene();
        IPackage data = (IPackage) presentation.getFirstSubject();
        
        // The polygon widget will specify the shape of the package.
        Point2D[] points = new Point2D[7];
        points[0] = new Point2D.Double(0, 1);
        points[1] = new Point2D.Double(0, 0);
        points[2] = new Point2D.Double(TAB_WIDTH, 0);
        points[3] = new Point2D.Double(TAB_WIDTH, TAB_HEIGHT);
        points[4] = new Point2D.Double(0, TAB_HEIGHT);
        points[5] = new Point2D.Double(1, TAB_HEIGHT);
        points[6] = new Point2D.Double(1, 1);

        final PolygonWidget polygon = new PolygonWidget(scene, points);
        polygon.setOpaque(true);
        setCurrentView(polygon);

        container = new ContainerWidget(scene);
        container.setCheckClipping(true);

        stereotypeWidget = new UMLLabelWidget(getScene(),
                                              getWidgetID() + "." + UMLNameWidget.stereotypeID,
                                              NbBundle.getMessage(PackageWidget.class, "LBL_stereotype"));
        stereotypeWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
        stereotypeWidget.setBorder(BODY_NAME_BORDER);
        updateStereotypes(data.getAppliedStereotypesAsString());

        taggedValuesWidget = new MultiLineTaggedValueWidget(getScene(),
                                                getWidgetID() + "." + UMLNameWidget.taggedValueID,
                                                NbBundle.getMessage(PackageWidget.class, "LBL_taggedValue"));
        //taggedValuesWidget.setBorder(BODY_NAME_BORDER);
        taggedValuesWidget.updateTaggedValues(data.getTaggedValuesAsList());

        nameWidget = new EditableCompartmentWidget(getScene(),
                                                   getWidgetID() + "." + UMLNameWidget.nameCompartmentWidgetID,
                                                   NbBundle.getMessage(PackageWidget.class, "LBL_name"));

        nameWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
        nameWidget.setBorder(BODY_NAME_BORDER);
        nameWidget.setLabel(data.getNameWithAlias());
        setFont(getCurrentView().getFont());
        
//        String id = getResourcePath();//getWidgetID() + "." + BodyNameContainerID; // NOI18N
        Widget bodyNameContainer = new CustomizableWidget(scene, getResourcePath(), 
                NbBundle.getMessage(PackageWidget.class, "LBL_BodyNameContainer"))
        {
            @Override
            protected void paintBackground()
            {
                Paint bg = getBackground();

                // TODO: Need to test if gradient paint preference is set.
                if((bg instanceof Color) && (useGradient == true))
                {
                    Rectangle bounds = getClientArea();
                    float midX = bounds.width / 2;

                    Color bgColor = (Color)bg;
                    GradientPaint paint = new GradientPaint(midX, 0, Color.WHITE,
                                                            midX, getBounds().height, 
                                                            bgColor);
                    Graphics2D g = getGraphics();
                    g.setPaint(paint);
                    g.fillRect(0, 0, bounds.width, bounds.height);
                }
                else
                {
                    super.paintBackground();
                }
            }
        };
        
        bodyNameContainer.setOpaque(true);
        bodyNameContainer.setLayout(LayoutFactory.createOverlayLayout());
        
        bodyNameHolder = new Widget(getScene());
        bodyNameHolder.setBackground((Paint) null);
        bodyNameHolder.setForeground((Color) null);

        bodyNameHolder.setLayout(new CenterLayout());
        bodyNameHolder.setCheckClipping(true);
//        bodyNameHolder.addChild(nameWidget);
        bodyNameHolder.addChild(stereotypeWidget);
        bodyNameHolder.addChild(nameWidget);
        bodyNameHolder.addChild(taggedValuesWidget);
        bodyNameContainer.addChild(bodyNameHolder);
        
        final Widget body = new Widget(scene);
        body.setForeground((Color)null);
        body.setBackground((Paint)null);
        
        body.setBorder(BorderFactory.createCompositeBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                                           BorderFactory.createLineBorder()));
        body.setLayout(LayoutFactory.createOverlayLayout());
        body.addChild(bodyNameContainer);
        body.addChild(container);
        
        polygon.addChild(body, BODY_NAME_CONTRAINTS);
        
        namePlaceholder = new Widget(scene);
        nameWidget.addDependency(nameDependency);
        polygon.addChild(namePlaceholder, EMPTY_TAB_NAME_CONSTRAINTS);
        
        setMinimumSize(new Dimension(150, 100));//resizing/selection works better if minim initial size is set
        //setPreferredSize(new Dimension(150, 100));
        
        container.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event)
            {
                String prop = event.getPropertyName();
                if(prop.equals(ContainerWidget.CHILDREN_CHANGED) == true)
                {
                    if(container.getChildren().size() > 0)
                    {   
                        namePlaceholder.removeFromParent();
                        nameWidget.removeDependency(nameDependency);
                        
                        nameWidget.getParentWidget().removeChild(nameWidget);
                        nameWidget.setBorder(TAB_NAME_BORDER);
                        polygon.addChild(nameWidget, TAB_NAME_CONSTRAINTS);
                        bodyNameHolder.setLayout(LayoutFactory.createVerticalFlowLayout());
                    }
                    else
                    {
                        nameWidget.getParentWidget().removeChild(nameWidget);
                        bodyNameHolder.addChild(1,nameWidget);
                        bodyNameHolder.setChildConstraint(nameWidget, 100);
                        
                        namePlaceholder.removeFromParent();

                        bodyNameHolder.setLayout(new CenterLayout());
                        nameWidget.setBorder(BODY_NAME_BORDER);
                        polygon.addChild(namePlaceholder, EMPTY_TAB_NAME_CONSTRAINTS);
                        nameWidget.addDependency(nameDependency);
                    }
                }
            }
        });
        
        super.initializeNode(presentation);
    }

    @Override
    protected void notifyElementDeleted()
    {
        getContainer().firePropertyChange(ContainerWidget.CHILDREN_CHANGED, null, null);
    }

    protected void updateStereotypes(List<String> stereotypes)
    {
        String stereotypeStr = "";
        for (String stereotype : stereotypes)
        {
            if (stereotypeStr.length() > 0)
            {
                stereotypeStr += ", ";
            }
            stereotypeStr += stereotype;
        }

        if (stereotypeStr.length() > 0)
        {
            stereotypeWidget.setLabel("<<" + stereotypeStr + ">>");
            stereotypeWidget.setVisible(true);
        } else
        {
            stereotypeWidget.setLabel("");
            stereotypeWidget.setVisible(false);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
//        nameWidget.propertyChange(event);

        super.propertyChange(event);
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        if (element instanceof IPackage)
        {
            IPackage data = (IPackage) element;

            if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()) ||
                propName.equals(ModelElementChangedKind.ALIAS_MODIFIED.toString()) )
            {
                if (getScene() instanceof ObjectScene)
                {
                    nameWidget.setLabel(data.getNameWithAlias());
                }
            } else if (propName.equals(ModelElementChangedKind.STEREOTYPE.toString()))
            {
                nameWidget.setLabel(data.getNameWithAlias());
                updateStereotypes(data.getAppliedStereotypesAsString());
            } else if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
            {
                // There is a specific tagged value event.  Therefore we have to
                // check everytime the element is modified.
                taggedValuesWidget.updateTaggedValues(element.getTaggedValuesAsList());
            }
        }
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.PACKAGEWIDGET.toString();
    }

    @Override
    public void refresh(boolean resizetocontent) {
        //default logic with init do not work because of possible children nodes existence
        IPresentationElement nodePe = getObject();
        if (nodePe != null && nodePe.getFirstSubject() != null && !nodePe.getFirstSubject().isDeleted())
        {
            INamedElement packEl=(INamedElement) nodePe.getFirstSubject();//widget should be used for packages only,so not check for type
            //need to update name
            nameWidget.setLabel(packEl.getNameWithAlias());
            //need to update owned elements but keep it the same as in 6.1 where update do not work if model was changed
            //i.e. if child node was moved out of package in model it rmains graphically contained and even dragged with the container
            //issue #78705
            //..
        } else
        {
            remove();
        }
        
        if(resizetocontent)Util.resizeNodeToContents(this);
        getScene().validate();
    }

    @Override
    protected void notifyFontChanged(Font font) {
        if(nameWidget!=null && font!=null)
        {
            nameWidget.setFont(font.deriveFont(Font.BOLD));
        }
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Package");
        return paletteModel;
    }
    
    private class NameSizeDependency implements Widget.Dependency
    {

        public void revalidateDependency()
        {
            try
            {
                namePlaceholder.setPreferredSize(nameWidget.getPreferredBounds().getSize());
            }
            catch(NullPointerException e)
            {
                // Ignore because the visual library will throw this exception
                // before somethings are setup.  By time it displays on the
                // screen everything will be set up correctly.
            }
        }
        
    }
    
    /**
     * The center layout is used to layout out the contents.  I was not able to 
     * use the FlowLayhout and specify the constraint of 100 because it would 
     * not resize correctly when the packag node when from being bigger then 
     * back down to being smaller.
     */
    private class CenterLayout implements Layout
    {

        public void layout(Widget widget)
        {
            List < Widget > children = widget.getChildren();
            
            int y = 0;
            for(Widget child : children)
            {
                Rectangle childBounds = child.getPreferredBounds();
                child.resolveBounds(new Point(-childBounds.x, y), childBounds);
                y += childBounds.height;
            }
        }

        public boolean requiresJustification(Widget widget)
        {
            return true;
        }

        public void justify(Widget widget)
        {
            List < Widget > children = widget.getChildren();
            
            int totalHeight = 0;
            for(Widget child : children)
            {
                Rectangle childBounds = child.getPreferredBounds();
                totalHeight += childBounds.height;
            }

//            System.out.println("Total Height = " + totalHeight);
            Rectangle bounds = widget.getClientArea();
            
            int y = (bounds.height / 2) - (totalHeight / 2);
            for(Widget child : children)
            {
                Rectangle childBounds = child.getPreferredBounds();
                Rectangle newBounds = new Rectangle(childBounds);
                newBounds.width = bounds.width;
                
                child.resolveBounds(new Point(-childBounds.x, y), newBounds);
                y += childBounds.height;
            }
        }
        
    }
}