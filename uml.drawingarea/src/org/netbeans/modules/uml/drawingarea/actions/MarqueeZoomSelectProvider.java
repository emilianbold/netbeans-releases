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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author Sheryl Su
 */
public class MarqueeZoomSelectProvider implements RectangularSelectProvider
{

    private ObjectScene scene;

    public MarqueeZoomSelectProvider(ObjectScene scene)
    {
        this.scene = scene;
    }

    public void performSelection(Rectangle sceneSelection)
    {

        int w = Math.abs(sceneSelection.width) ;
        int h = Math.abs(sceneSelection.height);

        if (w < 1 || h < 1)
        {
            return;
        }
        
        Rectangle rect = new Rectangle (0, 0, w, h);
        rect.translate (sceneSelection.x, sceneSelection.y);
        rect = scene.convertSceneToView(rect);
        
        Rectangle visible = scene.getView().getVisibleRect();
        double scale = Math.min(visible.width / rect.width, visible.height / rect.height);
        
        JComponent view = scene.getView();
        if (view != null)
        {
            Point center = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
            center = scene.convertViewToScene(center);

            scene.setZoomFactor(scale * scene.getZoomFactor());
            scene.validate(); // HINT - forcing to change preferred size of the JComponent view
            center = scene.convertSceneToView(center);

            view.scrollRectToVisible(new Rectangle(center.x - view.getVisibleRect().width/2, 
                                                   center.y - view.getVisibleRect().height / 2, 
                                                   view.getVisibleRect().width, 
                                                   view.getVisibleRect().height));
        } else
        {
            scene.setZoomFactor(scale * scene.getZoomFactor());
        }
        if(scene instanceof DesignerScene)
        {
            DesignerScene ds=(DesignerScene) scene;
            if(ds.getTopComponent() instanceof SQDDiagramTopComponent)
            {
                SQDDiagramTopComponent tc=(SQDDiagramTopComponent) ds.getTopComponent();
                tc.getTrackBar().onPostScrollZoom();
            }
        }
        scene.validate();
    }
}