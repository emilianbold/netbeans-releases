/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.ContainerNode;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Thuy
 */
public class PartitionWidget extends ContainerNode 
{
    private Scene scene;
    private IActivityPartition partition;
    private ContainerWidget containerWidget;
    //private CompartmentSeparatorWidget separatorWidget;
    private UMLNameWidget nameWidget;
    private static ResourceBundle bundle = NbBundle.getBundle(ContainerNode.class);
    
    public PartitionWidget(Scene scene)
    {
        super(scene);
        this.scene = scene;
    }
    
    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if ( presentation != null)
        {
            IActivityPartition element = (IActivityPartition) presentation.getFirstSubject();
            setCurrentView(createActivityParttionView(element) );
        }
    }
     
    private Widget createActivityParttionView(IActivityPartition element)
    {
         //create main view 
        MainViewWidget mainView = new MainViewWidget(scene, 
                getWidgetID(), bundle.getString("LBL_body"));
         
        mainView.setLayout(
                LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY , 2));
        
        mainView.setBorder(BorderFactory.createLineBorder());
        //mainView.setMinimumSize(new Dimension(MIN_NODE_WIDTH, MIN_NODE_HEIGHT));
        mainView.setUseGradient(useGradient);
        mainView.setCustomizableResourceTypes(
                    new ResourceType [] {ResourceType.BACKGROUND} );
        mainView.setOpaque(true);
        mainView.setCheckClipping(true);

        // element name widget (including stereotype and tagged value)
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        nameWidget.initialize(element);
       // nameWidget.setBorder(BorderFactory.createLineBorder(1, Color.RED));
        mainView.addChild(nameWidget);
        
        SeparatorWidget hSeparator = new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL);
        //hSeparator.setForeground(Color.BLACK);
        mainView.addChild(hSeparator);
        
        containerWidget = new ContainerWidget(scene);
        //containerWidget.setMinimumSize(new Dimension(30,30));
        containerWidget.setPreferredBounds(new Rectangle(100,60));
        //containerWidget.setBorder(BorderFactory.createLineBorder(5, Color.BLUE));

        mainView.addChild(containerWidget);
        
        return mainView;
    }
    
    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.PARTITIONWIDGET.toString();
    }
    
    
//    public void showSeparator(boolean show)
//    {
//        if (separatorWidget != null)
//            separatorWidget.setVisible(show);
//    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        nameWidget.propertyChange(event);
//        if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
//            nameWidget.propertyChange(evt);
//        else if (evt.getPropertyName().equals(ModelElementChangedKind.DELETE.toString()) || 
//                evt.getPropertyName().equals(ModelElementChangedKind.PRE_DELETE.toString()))
//        {
//
//        }
    }

    @Override
    public  ContainerWidget getContainer()
    {
        return containerWidget;
    }
    
    private class MainViewWidget extends CustomizableWidget
    {
        public MainViewWidget(Scene scene, String propID, String propDisplayName)
        {
            super(scene, propID, propDisplayName);
        }

        @Override
        public void paintBackground()
        {
            Rectangle bounds = getBounds();
            Paint bgColor = getBackground();
            
            if (isGradient())
            {
                Color primeBgColor = (Color) bgColor;
                bgColor = new GradientPaint(
                        0, 0, Color.WHITE,
                        0, bounds.height, primeBgColor);
            } 
            
            Graphics2D graphics = getGraphics();
            Paint previousPaint = graphics.getPaint();
            graphics.setPaint (bgColor);
            graphics.fillRect (bounds.x, bounds.y, bounds.width, bounds.height);
            
            // reset to presious paint
            graphics.setPaint(previousPaint);
        }
    }
}
