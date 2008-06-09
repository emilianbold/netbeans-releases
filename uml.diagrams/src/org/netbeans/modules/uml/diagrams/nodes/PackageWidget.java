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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint; 
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.widgets.PolygonConstraints;
import org.netbeans.modules.uml.widgets.PolygonWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
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
    
    private final static PolygonConstraints BODY_NAME_CONTRAINTS = 
                            new PolygonConstraints(0, 3, 5, 6,
                                                   PolygonConstraints.VertexWeight.NONE,
                                                   PolygonConstraints.VertexWeight.NONE);
    
    
    private UMLNameWidget nameWidget = null;
    private ContainerWidget container = null;
    private IPresentationElement pe;
    public static String BodyNameContainerID = "PackageBody";
    
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
        
        nameWidget= new UMLNameWidget(scene, false,  getWidgetID());
        nameWidget.initialize(data); 
        
        String id = getWidgetID() + "." + BodyNameContainerID; // NOI18N
        Widget bodyNameContainer = new CustomizableWidget(scene, id, 
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
        
        final Widget centerHack = new Widget(getScene());
        centerHack.setBackground((Paint) null);
        centerHack.setForeground((Color) null);

        centerHack.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        centerHack.addChild(nameWidget);
        centerHack.setChildConstraint(nameWidget, 100);
        bodyNameContainer.addChild(centerHack);
//        bodyNameContainer.addChild(nameWidget);
        
        final Widget body = new Widget(scene);
        body.setForeground((Color)null);
        body.setBackground((Paint)null);
        
        body.setBorder(BorderFactory.createCompositeBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                                           BorderFactory.createLineBorder()));
        body.setLayout(LayoutFactory.createOverlayLayout());
        body.addChild(bodyNameContainer);
        body.addChild(container);
        
        polygon.addChild(body, BODY_NAME_CONTRAINTS);
        
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
                        nameWidget.getParentWidget().removeChild(nameWidget);
                        polygon.addChild(nameWidget, TAB_NAME_CONSTRAINTS);
                    }
                    else
                    {
                        nameWidget.getParentWidget().removeChild(nameWidget);
                        centerHack.addChild(nameWidget);
                        centerHack.setChildConstraint(nameWidget, 100);
                    }
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        nameWidget.propertyChange(event);
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.PACKAGEWIDGET.toString();
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Package");
        return paletteModel;
    }
}