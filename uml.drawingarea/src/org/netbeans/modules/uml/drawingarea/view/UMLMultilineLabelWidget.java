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
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.widgets.MultilineLabelWidget;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;

/**
 *
 * @author jyothi
 */
public class UMLMultilineLabelWidget extends MultilineLabelWidget 
        implements DiagramEdgeWriter, DiagramEdgeReader, UMLWidget, Customizable {
  
    private String propId ;
    private String propDisplayName;
    private ResourceType[] customizableResTypes = new ResourceType[] {
        ResourceType.FONT,
        ResourceType.FOREGROUND, 
        ResourceType.BACKGROUND }; 
    
    public UMLMultilineLabelWidget(Scene scene,
            String propertyID, String propDisplayName) {
        super(scene);
        init(propertyID, propDisplayName);
    }

    public UMLMultilineLabelWidget(Scene scene, String label,
            String propertyID, String propDisplayName) {
        super(scene, label);
         init(propertyID, propDisplayName);
    }

    private void init(String propertyID, String displayName) {
        setForeground(null); 
        setBackground(null);
        propId = propertyID;
        ResourceValue.initResources(propertyID, this);
        propDisplayName = displayName;
    }
    
    public void save(EdgeWriter edgeWriter) {
        edgeWriter.setPEID(PersistenceUtil.getPEID(this));
        edgeWriter.setVisible(this.isVisible());
        edgeWriter.setLocation(this.getLocation());
        edgeWriter.setSize(this.getBounds().getSize());
        edgeWriter.setPresentation("");

        edgeWriter.beginGraphNode();
        edgeWriter.endGraphNode();
    }

    public void load(EdgeInfo edgeReader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWidgetID() {
        return UMLWidgetIDString.LABELWIDGET.toString();
    }

    public void remove()
    {
        super.removeFromParent();
    }

    public void refresh(boolean resizetocontent) {}


    public String getDisplayName()
    {
        return propDisplayName;
    }
    
    public String getID()
    {
        return propId;
    }

    public void update()
    {
        ResourceValue.initResources(propId, this);
    }

    public ResourceType[] getCustomizableResourceTypes()
    {
        return customizableResTypes;
    }

    public void setCustomizableResourceTypes(ResourceType[] resTypes)
    {
        customizableResTypes = resTypes;
    }

    @Override
    protected Rectangle calculateClientArea()
    {
        if (getLabel() == null)
        {
            return super.calculateClientArea();
        }

        Rectangle rectangle;
//        if (isUseGlyphVector() == true)
//        {
//            assureGlyphVector();
//            rectangle = GeomUtil.roundRectangle(cacheGlyphVector.getVisualBounds());
//            rectangle.grow(1, 1); // WORKAROUND - even text antialiasing is included into the boundary
//        }
//        else
        {
            Graphics2D gr = getGraphics();
            FontMetrics fontMetrics = gr.getFontMetrics(getFont());
            Dimension parentSize = getParentPrefSize();

            Rectangle2D union = new Rectangle2D.Double();
            double x = 0;
            double y = 0;
            double width = 0;
            double height = 0;

            String[] lines = getLabel().split("\n");
            for(int index = 0; index < lines.length; index++)
            {
                String line = lines[index];
                Rectangle2D stringBounds = fontMetrics.getStringBounds(line, gr);

                if(index == 0)
                {
                    x = stringBounds.getX();
                    y = stringBounds.getY();
                    width = stringBounds.getWidth();
                    if (parentSize != null && (parentSize.width < width))
                    {
                        width = parentSize.width;
                    }
                }
                else
                {
                    if(stringBounds.getX() < x)
                    {
                        x = stringBounds.getX();
                    }

                    if(stringBounds.getY() < y)
                    {
                        y = stringBounds.getY();
                    }

                    if(stringBounds.getWidth() > width)
                    {
                        width = stringBounds.getWidth();
                    }
                    if (parentSize != null && (parentSize.width < width))
                    {
                        width = parentSize.width;
                    }
                }

                height += stringBounds.getHeight();
                if (parentSize != null && (parentSize.height < height))
                {
                    height = parentSize.height;
                }

            }
            rectangle = roundRectangle(new Rectangle2D.Double(x, y, width, height));
            
        }

        switch (getOrientation())
        {
            case NORMAL:
                return rectangle;
            case ROTATE_90:
                return new Rectangle(rectangle.y, -rectangle.x - rectangle.width, rectangle.height, rectangle.width);
            default:
                throw new IllegalStateException();
        }
    }

    private Dimension getParentPrefSize()
    {
       UMLNodeWidget widget = getParent();
        if (widget != null && widget.getPreferredSize() != null)
        {
            return widget.getPreferredSize();
        }
        return null;
    }

    private UMLNodeWidget getParent()
    {
         UMLNodeWidget widget = PersistenceUtil.getParentUMLNodeWidget(this);
         return widget;
    }
}
