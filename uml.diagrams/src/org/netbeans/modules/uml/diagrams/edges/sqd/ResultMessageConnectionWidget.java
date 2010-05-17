/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.diagrams.edges.sqd;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.diagrams.engines.SequenceDiagramEngine;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 * message connection with perfomance optimization for extra small zooms
 * @author sp153251
 */
public class ResultMessageConnectionWidget extends MessageWidget {
    //
    public ResultMessageConnectionWidget(Scene scene) {
        super(scene);
        setTargetAnchorShape(AnchorShapeFactory.createArrowAnchorShape(45, 12));
        setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,1,new float[] { 5.0f, 5.0f },0));
        SequenceDiagramEngine engine=(SequenceDiagramEngine) ((DesignerScene) getScene()).getEngine();
        setVisible(engine.getSettingValue(SequenceDiagramEngine.SHOW_RETURN_MESSAGES)==Boolean.TRUE);
    }
    /**
     * Paints the connection widget (the path, the anchor shapes, the control points, the end points).
     */
    @Override
    protected void paintWidget () {
        Graphics2D gr = getGraphics ();
        AffineTransform transform=gr.getTransform();
        double zoom=Math.sqrt(transform.getScaleX()*transform.getScaleX()+transform.getShearY()*transform.getShearY());
        if(zoom>0.2)
        {
            //just draw what is expected to draw, line, dashed line or any complex line
            super.paintWidget();
        }
        else
        {
            //small zoom, just  draw line without dashes wich may be drawn by parent widget
            //TBD check if it's have sense to make color lighter (far distance dashes effect) but only if originaly there are dashes
            gr.setColor (getForeground ());
            Point firstControlPoint = getFirstControlPoint ();
            Point lastControlPoint = getLastControlPoint ();
            //TBD redefine most from original ConnectionWidget (see issue 108754 which block complete redefinition), now is useful for direct message only
            gr.drawLine(firstControlPoint.x, firstControlPoint.y, lastControlPoint.x, lastControlPoint.y);
        }
    }
    @Override
    protected LabelManager createLabelManager() {
        return new MessageLabelManager(this);
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.RESULTMESSAGECONNECTIONWIDGET.toString();
    }

    @Override
    public void load(EdgeInfo edgeReader) {
        super.load(edgeReader);
        setTargetAnchorShape(AnchorShapeFactory.createArrowAnchorShape(45, 12));
        setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,1,new float[] { 5.0f, 5.0f },0));
        SequenceDiagramEngine engine=(SequenceDiagramEngine) ((DesignerScene) getScene()).getEngine();
        setVisible(engine.getSettingValue(SequenceDiagramEngine.SHOW_RETURN_MESSAGES)==Boolean.TRUE);
    }
    
    

}
