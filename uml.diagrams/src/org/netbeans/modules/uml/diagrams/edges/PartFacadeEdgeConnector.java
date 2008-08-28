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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashMap;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;

/**
 *
 * 
 */
public class PartFacadeEdgeConnector extends AbstractUMLConnectionWidget
{
    public PartFacadeEdgeConnector(Scene scene)
    {
        super(scene);
        
        setForeground(Color.BLACK);
        setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER, 
                                  10.0f, new float[]{ 10.0f }, 0.0f));

        setSourceAnchorShape(AnchorShape.NONE);
        setTargetAnchorShape(AnchorShape.NONE);

        setControlPointShape(PointShape.NONE);
        setEndPointShape(PointShape.NONE);

    }
    
    protected void initLabelManager() {}

    @Override
    public void initialize(IPresentationElement element)
    {
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.PARTFACADEEDGECONNECTIONWIDGET.toString();
    }

    @Override
    public void save(EdgeWriter edgeWriter) 
    {
        HashMap edgeProps = edgeWriter.getEdgeProperties();
        edgeProps.put(UMLEdgeWidget.PROXY_PRESENTATION_ELEMENT, "PART-FACADE-EDGE"); //NOI18N
        edgeWriter.setEdgeProperties(edgeProps);
        super.save(edgeWriter);
    }
    
}
    

