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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint; 
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.util.NbBundle;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;

/**
 *
 * @author treyspiva
 */
public class DesignPatternWidget extends UMLNodeWidget 
    implements PropertyChangeListener
{
    protected final static int MIN_NODE_WIDTH  = 80;
    protected final static int MIN_NODE_HEIGHT = 40;
    protected final static int MIN_TEMPLATE_HEIGHT = 30;
    protected final static Color LIGHT_FILL_COLOR = new Color(103,237,213);

    private UMLNameWidget nameWidget = null;
    private TemplateWidget parameterWidget = null;
    private TemplateContainerWidget parameterContainerWidget = null;
    private DesignPatternBodyWidget bodyWidget = null;
    private String bodyWidgetID = "DesignPatternWidgetBody";
    private String parameterContainerWidgetID = "DesignPatternParameterContainerWidget";

    public DesignPatternWidget(Scene scene)
    {
        super(scene,true);        
        addToLookup(initializeContextPalette());
    }
    
    public DesignPatternWidget(Scene scene, IPresentationElement presentation)
    {
        this(scene);
        initializeNode(presentation);
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        ICollaboration c = (ICollaboration) presentation.getFirstSubject();
        setCurrentView(createView(c));
        setFont(getCurrentView().getFont());
        super.initializeNode(presentation);
        getScene().validate();
    }

    @Override
    public void initializeNode(IPresentationElement presentation, boolean show) {
        //isShowWidget=show;
        if(show)
        {
            ICollaboration c = (ICollaboration) presentation.getFirstSubject();
            IParameterableElement parameter = (IParameterableElement) FactoryRetriever.instance()
                .createType("ParameterableElement", null); // NOI18N
            c.addTemplateParameter(parameter);
        }
        initializeNode(presentation);
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/DesignPattern");
        return paletteModel;
    }

    public Widget createView(ICollaboration element)
    {
        setBackground((Paint)null);
        parameterWidget = null;

        String id = getWidgetID() + "." + bodyWidgetID; // NOI18N
        bodyWidget = new DesignPatternBodyWidget(getScene(), id, 
            NbBundle.getMessage(DesignPatternWidget.class, "LBL_DesignPatternWidgetBody"));
       
        ResourceValue.initResources(getWidgetID() + "." + DEFAULT, bodyWidget);

        bodyWidget.setLayout(new CenteredWidgetLayout());
        bodyWidget.setOpaque(true);

        nameWidget = new UMLNameWidget(getScene(), false, getWidgetID());
        nameWidget.initialize(element);
        ResourceValue.initResources(getWidgetID() + "." + DEFAULT, nameWidget);

        bodyWidget.addChild(nameWidget);
        
        Widget retVal = new Widget(getScene());
        retVal.addChild(bodyWidget);
        
        if (element instanceof IClassifier) 
        {
            initializeParameterWidget((IClassifier)element);
        
            if(parameterWidget != null) 
            {
                parameterWidget.setBorder(BorderFactory.createEmptyBorder());
                parameterWidget.setBackground((Paint)null);
                parameterWidget.setOpaque(false);

                String tcId = getWidgetID() + "." + parameterContainerWidgetID; // NOI18N
                parameterContainerWidget = new TemplateContainerWidget(getScene(), tcId, 
                    NbBundle.getMessage(DesignPatternWidget.class, 
                                        "LBL_DesignPatternParameterContainerWidget"));
                ResourceValue.initResources(getWidgetID() + "." + DEFAULT, parameterWidget);
                ResourceValue.initResources(getWidgetID() + "." + DEFAULT, parameterContainerWidget);
                parameterContainerWidget.setOpaque(true);
                parameterContainerWidget.setLayout(LayoutFactory.createVerticalFlowLayout());


                retVal.setLayout(new TemplateWidgetLayout());
                retVal.setBackground((Paint)null);

                retVal.addChild(parameterContainerWidget);
                parameterContainerWidget.addChild(parameterWidget);

            }
        }
        setMinSize();
        return retVal;
    }

    protected boolean initializeParameterWidget(IClassifier element)
    {
        boolean retVal = false;
                
        List < IParameterableElement > params = element.getTemplateParameters();
        if((params != null) && (params.size() > 0))
        {
            if(parameterWidget == null)
            {
                parameterWidget = new TemplateParameterWidget(getScene());
                retVal = true;
            }
            parameterWidget.updateUI(element);
        }
        else if((parameterWidget != null) && (parameterWidget.getParentWidget() != null))
        {
            parameterWidget.getParentWidget().removeChild(parameterWidget);
            parameterWidget = null;
            retVal = true;
        }
        
        return retVal;
    }

    public class TemplateWidgetLayout implements Layout
    {
        private static final int TEMPLATE_EXTENDS = 10;
        public void layout(Widget widget)
        {
            Rectangle bounds = bodyWidget.getPreferredBounds();
            int bodyY = 0;
            if(bounds != null)
            {
                int shift = TEMPLATE_EXTENDS;
                Rectangle paramBounds = parameterContainerWidget.getPreferredBounds();
                bodyY = paramBounds.height / 3 * 2;

                if(paramBounds.width < (bounds.width))
                {
                    paramBounds.width = bounds.width;
                }

                parameterContainerWidget.resolveBounds(new Point(shift, 0), 
                                              new Rectangle(new Point(0, 0), 
                                                            paramBounds.getSize()));
    
                Point bodyLocation = new Point(0, bodyY);
                bodyWidget.resolveBounds(bodyLocation, 
                                         new Rectangle(new Point(0, 0), bounds.getSize()));
                widget.resolveBounds(new Point(0,0), 
                    new Rectangle(new Point(0, 0), 
                                  new Dimension(paramBounds.width + TEMPLATE_EXTENDS,
                                                bodyY + bounds.height)));
            }
        }

        public boolean requiresJustification(Widget widget)
        {
            return true;
        }

        public void justify(Widget widget)
        {
            Rectangle clientArea = widget.getClientArea();
            
            int bodyWidth = clientArea.width - TEMPLATE_EXTENDS;
            int shift = TEMPLATE_EXTENDS;
            
            Rectangle paramBounds = parameterContainerWidget.getPreferredBounds();
            Rectangle bounds = bodyWidget.getPreferredBounds();
            Dimension paramSize = new Dimension(clientArea.width - shift, paramBounds.height );
            
            int bodyY = paramSize.height - (paramSize.height / 3);
            Dimension bodySize = new Dimension(bodyWidth, clientArea.height - bodyY);

            Point paramLocation = new Point(shift, 0);
            Point bodyLocation = new Point(0, bodyY);
            
            parameterContainerWidget.resolveBounds(paramLocation, 
                                                   new Rectangle(new Point(0, 0), paramSize));
            bodyWidget.resolveBounds(bodyLocation, new Rectangle(new Point(0, 0), bodySize));
        }
        
    }

    public class CenteredWidgetLayout implements Layout
    {
        public void layout(Widget widget)
        {
            List<Widget> children = widget.getChildren();
            if (children != null && children.size() > 0) 
            {
                Widget child = children.get(0);
                Rectangle bounds = child.getPreferredBounds();
                if(bounds != null)
                {
                    child.resolveBounds(new Point(0, 0), 
                                        new Rectangle(new Point(0, 0), 
                                                      bounds.getSize()));    
                }
            }
        }

        public boolean requiresJustification(Widget widget)
        {
            return true;
        }

        public void justify(Widget widget)
        {
            Rectangle clientArea = widget.getClientArea();
            List<Widget> children = widget.getChildren();
            if (children != null && children.size() > 0) 
            {
                Widget child = children.get(0);
                Rectangle bounds = child.getPreferredBounds();
                int bodyY = 0;
                int bodyX = 0;
                if(bounds != null)
                {
                    bodyY = (clientArea.height - bounds.height) / 2;
                    bodyX = (clientArea.width - bounds.width) / 2;

                    child.resolveBounds(new Point(bodyX, bodyY), 
                                        new Rectangle(new Point(0, 0), 
                                                      bounds.getSize()));    
                }
            }
        }
        
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        
        if((event.getSource() instanceof IParameterableElement) 
           && (! (event.getSource() instanceof ICollaboration)) 
           && (parameterWidget != null))
        {
            parameterWidget.propertyChange(event);
            return;
        }

        String propName = event.getPropertyName();
        nameWidget.propertyChange(event);
        if(propName.equals(ModelElementChangedKind.TEMPLATE_PARAMETER.toString()))
        {
            if (event.getSource() instanceof ICollaboration) 
            {
                setCurrentView(createView((ICollaboration)event.getSource()));
            }
        }
    }

    
    public String getWidgetID() {
        return UMLWidgetIDString.DESIGNPATTERNWIDGET.toString();
    }

    private void setMinSize() 
    {
        Dimension bd = bodyWidget.getMinimumSize();
        int w = (bd == null) ? MIN_NODE_WIDTH : Math.max(MIN_NODE_WIDTH, bd.width);
        int h = (bd == null) ? MIN_NODE_HEIGHT : Math.max(MIN_NODE_HEIGHT, bd.width);
        bodyWidget.setMinimumSize(new Dimension(w, h));    
    
        if (parameterContainerWidget != null) 
        {
            Dimension par = parameterContainerWidget.getMinimumSize();
            w = (par == null) ? MIN_NODE_WIDTH : Math.max(MIN_NODE_WIDTH, par.width);
            h = (par == null) ? MIN_TEMPLATE_HEIGHT : Math.max(MIN_TEMPLATE_HEIGHT, par.width);
            parameterContainerWidget.setMinimumSize(new Dimension(w, h));
        }        
    }


    public class TemplateParameterWidget extends TemplateWidget 
    {
        public TemplateParameterWidget(Scene scene)
        {
            super(scene);
        }

        public String getID()
        {
            return UMLWidgetIDString.DESIGNPATTERNWIDGET.toString() + ".TemplateParameters"; 
        }
    }

    public class DesignPatternBodyWidget extends Widget {

        public DesignPatternBodyWidget(Scene scene, String id, String name)
        {
            super(scene);
        }
        
        protected void paintBorder()
        {
            Graphics2D g = getGraphics();
            Paint previousPaint = g.getPaint();
            g.setColor(Color.BLACK);
            Rectangle bounds = getClientArea();
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 10.0f, new float[]{ 10.0f }, 0.0f);
            Stroke prevStroke = g.getStroke();
            g.setStroke(stroke);
            g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setStroke(prevStroke);
            g.setPaint(previousPaint);
        }
        
        protected void paintBackground()
        {
            Graphics2D g = getGraphics();
            Paint previousPaint = g.getPaint();
            
            Rectangle bounds = getClientArea();
            Paint bg = getBackground();               
            if((bg instanceof Color) && useGradient)
            {
                float midX = bounds.width / 2;
                
                Color bgColor = (Color)bg;
                GradientPaint paint = new GradientPaint(midX, 0, Color.WHITE,
                                                        midX, bounds.height, 
                                                        bgColor);
                g.setPaint(paint);
            }
            else
            {
                g.setPaint(bg);
            }
            g.fillOval(0, 0, bounds.width, bounds.height);
            g.setPaint(previousPaint);
        }

    }


    public class TemplateContainerWidget extends CustomizableWidget {

        public TemplateContainerWidget(Scene scene, String id, String name)
        {
            super(scene, id, name);
        }

        protected void paintBorder()
        {
            Graphics2D g = getGraphics();
            Paint previousPaint = g.getPaint();
            g.setColor(Color.BLACK);
            Rectangle bounds = getClientArea();
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 10.0f, new float[]{ 10.0f }, 0.0f);
            Stroke prevStroke = g.getStroke();
            g.setStroke(stroke);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setStroke(prevStroke);
            g.setPaint(previousPaint);
        }

        protected void paintBackground()
        {
            Graphics2D g = getGraphics();
            Paint previousPaint = g.getPaint();
            
            Rectangle bounds = getClientArea();
            Paint bg = getBackground();               
            if((bg instanceof Color) && useGradient)
            {
                float midX = bounds.width / 2;
                
                Color bgColor = (Color)bg;
                GradientPaint paint = new GradientPaint(midX, 0, LIGHT_FILL_COLOR,
                                                        midX, bounds.height, 
                                                        bgColor);
                g.setPaint(paint);
            }
            else
            {
                g.setPaint(bg);
            }
            g.fillRect(0, 0, bounds.width, bounds.height);
            g.setPaint(previousPaint);
        }
        
    }

    
}
