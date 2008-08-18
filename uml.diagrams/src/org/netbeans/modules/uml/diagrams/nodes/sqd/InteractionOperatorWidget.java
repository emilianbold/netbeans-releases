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
package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * It's not really pentagon because upper and left border isn't drawn
 * @author sp153251
 */
public class InteractionOperatorWidget extends Widget {
    UMLLabelWidget operator;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    private final int DW=5;//shift from boounds to form 5th edge of pentagon
    public static final String ID = "interactionOp";
    
    public InteractionOperatorWidget(Scene scene)
    {
        this(scene,"");
    }
    public InteractionOperatorWidget(Scene scene,String text)
    {
        super(scene);
        operator=new UMLLabelWidget(scene,text, ID, 
                                    NbBundle.getMessage(InteractionOperatorWidget.class, "LBL_Interaction_Operator"));
        operator.setMinimumSize(new Dimension(30,5));
        setLayout(LayoutFactory.createHorizontalFlowLayout());
        addChild(operator);
        setBorder(new PentagonBorder());
        setLabel(text);
        //
        lookupContent.add(new DefaultWidgetContext("Operator"));
    }
    
    public void setLabel(String text)
    {
        operator.setLabel(text);
    }
    
    
    private class PentagonBorder implements Border
    {

        public Insets getInsets() {
            return new Insets(0,1,0,5);
        }

        public void paint(Graphics2D gr, Rectangle bnds) {
            Rectangle bounds = bnds;
            Color fc=getForeground();
            gr.setColor (fc);
            //gr.drawLine(bounds.x, bounds.y, bounds.x+bounds.width+DW, bounds.y);
            gr.drawLine(bounds.x+bounds.width+DW, bounds.y, bounds.x+bounds.width+DW, bounds.y+bounds.height-DW);
            gr.drawLine(bounds.x+bounds.width+DW, bounds.y+bounds.height-DW, bounds.x+bounds.width, bounds.y+bounds.height);
            gr.drawLine(bounds.x+bounds.width, bounds.y+bounds.height, bounds.x, bounds.y+bounds.height);
            //gr.drawLine(bounds.x, bounds.y+bounds.height, bounds.x, bounds.y);
        }

        public boolean isOpaque() {
            return true;
        }
        
    }
}
