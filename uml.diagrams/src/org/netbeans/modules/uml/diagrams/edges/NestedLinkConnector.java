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
package org.netbeans.modules.uml.diagrams.edges;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;

/**
 * Creates a nested link connection.  The nested link connection represents 
 * an ownership.  For example a pacage and own a class.  The connection does 
 * does not represent a true relationship, but instead a property.  
 * 
 * @author treyspiva
 */
public class NestedLinkConnector extends AbstractUMLConnectionWidget
{
    
    public NestedLinkConnector(Scene scene)
    {
        super(scene);
        
        initializeWidget();
        
        setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
    }

    protected void initializeWidget()
    {
        setForeground(Color.BLACK);
        setSourceAnchorShape(new NestedLinkSourceShape());
        
        
    }
    
    /**
     * Since a nested link does not represent a true relationship it should not
     * have any labels.
     * 
     * @return null will be returned.
     */
    protected LabelManager createLabelManager()
    {
        return null;
    }
    
    private class NestedLinkSourceShape implements AnchorShape
    {

        public NestedLinkSourceShape()
        {
            super();
        }
        
        public boolean isLineOriented()
        {
            return true;
        }

        public int getRadius()
        {
            return 10;
        }

        public double getCutDistance()
        {
            return 20;
        }

        public void paint(Graphics2D graphics, boolean source)
        {
            graphics.drawOval(0, -10, 20, 20);
            graphics.drawLine(10, -5, 10, 5);
            graphics.drawLine(5, 0, 15, 0);
        }
        
    }
    
    @Override
    public void initialize(IPresentationElement element)
    {
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.NESTEDLINKCONNECTIONWIDGET.toString();
    }

    @Override
    public void save(EdgeWriter edgeWriter) 
    {
        HashMap edgeProps = edgeWriter.getEdgeProperties();
        edgeProps.put(UMLEdgeWidget.PROXY_PRESENTATION_ELEMENT, "NESTEDLINK");
        edgeWriter.setEdgeProperties(edgeProps);
        super.save(edgeWriter);
    }
      
}
