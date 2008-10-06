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
package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Font;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;

/**
 *
 * @author Sheryl Su
 */
public class ShallowHistoryStateWidget extends InitialStateWidget
{
    public ShallowHistoryStateWidget(Scene scene, String path)
    {
        super(scene, path);
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        super.initializeNode(presentation);
        CircleWidget circleWidget = new CircleWidget(getScene(),
                getRadius(),
                getResourcePath(),
                bundle.getString("LBL_body"));
   
        circleWidget.setOpaque(true);

        LabelWidget labelWidget = new UMLLabelWidget(getScene(), getSymbol());
        labelWidget.setFont(Font.decode("SansSerif-plain-10")); // NOI18N

        labelWidget.setForeground(null);
        circleWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(SerialAlignment.CENTER, 0));
        Widget layer = new Widget(getScene());
        layer.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, 0));
        layer.setForeground(null);
        layer.setBackground(null);
        layer.addChild(labelWidget);
        circleWidget.addChild(layer, 1);
        setCurrentView(circleWidget);        
    }

    @Override
    public String getWidgetID()
    {
        return UMLWidgetIDString.SHALLOWHISTORYSTATEWIDGET.toString();
    }
    
    public String getSymbol()
    {
        return "H";
    }
            
}
