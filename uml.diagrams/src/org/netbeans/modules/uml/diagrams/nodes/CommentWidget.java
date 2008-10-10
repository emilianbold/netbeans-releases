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
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.widgets.MultilineLabelWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.Comment;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author sp153251
 */
public class CommentWidget extends UMLNodeWidget implements PropertyChangeListener
{
    private MultilineLabelWidget bodyLabel = null;
    public static final int TAB_WIDTH = 11;

    public CommentWidget(Scene scene)
    {
        super(scene);
        addToLookup(initializeContextPalette());
        addToLookup(new DefaultWidgetContext("Comment"));
        setMinimumSize(new Dimension(100, 60));
    }

    public CommentWidget(Scene scene, IPresentationElement element)
    {
        this(scene);
        initializeNode(element);
    }

    @Override
    public void initializeNode(IPresentationElement element)
    {
        bodyLabel = new MultilineEditableCompartmentWidget(getScene(),
                getResourcePath(),
                NbBundle.getMessage(CommentWidget.class, "LBL_body"));
        bodyLabel.setAlignment(Alignment.LEFT);

        Border border = BorderFactory.createEmptyBorder(TAB_WIDTH + 2);
        bodyLabel.setBorder(border);

        ResourceValue.initResources(getResourcePath(), bodyLabel);
        setCurrentView(bodyLabel);
        setOpaque(true);
        
        //populate the comment text
        IElement elt = element.getFirstSubject();
        if (elt != null && elt instanceof IComment)
        {
            bodyLabel.setLabel(((IComment) elt).getBody());
        }
        super.initializeNode(element);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        DesignerScene scene = (DesignerScene) getScene();
        PresentationElement pe = (PresentationElement) scene.findObject(this);
        Comment comment = (Comment) pe.getFirstSubject();

        bodyLabel.setLabel(comment.getBody());
    }

    @Override
    public Dimension getDefaultMinimumSize()
    {
        return new Dimension(100, 60);
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Comment");
        return paletteModel;
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.COMMENTWIDGET.toString();
    }

    @Override
    public void load(NodeInfo nodeReader)
    {
        super.load(nodeReader);
        DesignerScene scene = (DesignerScene) getScene();
        PresentationElement pe = (PresentationElement) scene.findObject(this);
        Comment comment = (Comment) pe.getFirstSubject();
        bodyLabel.setLabel(comment.getBody());
    }

    @Override
    public void paintBackground()
    {
        Graphics2D graphics = getGraphics();
        Rectangle bounds = getBounds();
        assert bounds != null : "Error: bounds of RoundedRectangleWidget is null!";   // NOI18N

        Paint previousPaint = graphics.getPaint();

        Paint bgColor = getBackground();

        if (UMLNodeWidget.useGradient())
        {
            Color bg = (Color) bgColor;
            bgColor = new GradientPaint(
                    0, 0, Color.WHITE,
                    0, bounds.height, bg);
        }
        graphics.setPaint(bgColor);

        graphics.fillPolygon(getPolygon());

        if (previousPaint != graphics.getPaint())
        {
            graphics.setPaint(previousPaint);
        }

    }

    @Override
    public void paintWidget()
    {
        Graphics2D graphics = getGraphics();
        graphics.setColor(Color.BLACK);
        Rectangle bounds = getBounds();
        Insets insets = getBorder().getInsets();
        int leftBorder = bounds.x + insets.left;
        int top = bounds.y + insets.top;
        int rightBorder = bounds.x + bounds.width - insets.right - 1;
        int tabStartX = rightBorder - TAB_WIDTH;
        int bottom = bounds.y + bounds.height - insets.bottom - 1;
        int tabBottom = top + TAB_WIDTH;
        
        graphics.drawLine(leftBorder, top, tabStartX, top);
        graphics.drawLine(tabStartX, top, rightBorder, tabBottom);
        graphics.drawLine(rightBorder, tabBottom, tabStartX, tabBottom);
        graphics.drawLine(tabStartX, tabBottom, tabStartX, top);
        graphics.drawLine(rightBorder, tabBottom, rightBorder, bottom);
        graphics.drawLine(rightBorder, bottom, leftBorder, bottom);
        graphics.drawLine(leftBorder, bottom, leftBorder, top);        
    }

    private Polygon getPolygon()
    {
        Rectangle bounds = getBounds();
        Insets insets = getBorder().getInsets();
        int leftBorder = bounds.x + insets.left;
        int top = bounds.y + insets.top;
        int rightBorder = bounds.x + bounds.width - insets.right - 1;
        int tabStartX = rightBorder - TAB_WIDTH;
        int bottom = bounds.y + bounds.height - insets.bottom - 1;
        int tabBottom = top + TAB_WIDTH;

        int[] xPoints =
        {
            leftBorder, tabStartX, rightBorder,rightBorder, leftBorder, 
        };
        int[] yPoints =
        {
            top, top, tabBottom, bottom, bottom
        };

        return new Polygon(xPoints, yPoints, xPoints.length);
    }
}
