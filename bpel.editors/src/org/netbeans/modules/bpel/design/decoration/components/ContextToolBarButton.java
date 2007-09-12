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

package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;


public class ContextToolBarButton extends JButton 
        implements DecorationComponent
{

    private Icon icon;
    private Icon disabledIcon;

    private Color fillColor = null;
    private Color disabledFillColor = null;

    
    public ContextToolBarButton(Action action) {
        this(action, ButtonRenderer.NORMAL_FILL_COLOR, 
                ButtonRenderer.DISABLED_FILL_COLOR);
    }

    
    public ContextToolBarButton(Action action, Color fillColor) {
        this(action, fillColor, fillColor);
    }
    
    
    public ContextToolBarButton(Action action, 
            Color fillColor, 
            Color disabledFillColor) {
        super(action);

        setText(null);
        setOpaque(false);
        setBorder(null);
        setRolloverEnabled(true);
        setContentAreaFilled(false);
        setFocusable(false);
        
        this.icon = (Icon) action.getValue(Action.SMALL_ICON);
        this.disabledIcon = ButtonRenderer.createDisabledIcon(this, icon);
        
        this.fillColor = fillColor;
        this.disabledFillColor = disabledFillColor;

        setPreferredSize(new Dimension(icon.getIconWidth() + 6, 
                icon.getIconHeight() + 6));
    }
    

    public ContextToolBarButton(Icon icon) {
        setOpaque(false);
        setBorder(null);
        setRolloverEnabled(true);
        setContentAreaFilled(false);
        setFocusable(false);

        setPreferredSize(new Dimension(icon.getIconWidth() + 6, 
                icon.getIconHeight() + 6));
        
        this.icon = icon;
        this.disabledIcon = ButtonRenderer.createDisabledIcon(this, icon);
        
        this.fillColor = ButtonRenderer.NORMAL_FILL_COLOR;
        this.disabledFillColor = ButtonRenderer.DISABLED_FILL_COLOR;
    }

    
    protected void paintComponent(Graphics g) {
        ButtonModel model = getModel();
        
        if (!model.isEnabled()) {
            ButtonRenderer.paintButton(this, g, 
                    disabledFillColor, false, 
                    ButtonRenderer.DISABLED_BORDER_COLOR, 
                    ButtonRenderer.DISABLED_STROKE_WIDTH, disabledIcon);
        } else if (model.isPressed()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.PRESSED_FILL_COLOR, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, icon);
        } else if (model.isRollover()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.ROLLOVER_FILL_COLOR, true, 
                    ButtonRenderer.ROLLOVER_BORDER_COLOR, 
                    ButtonRenderer.ROLLOVER_STROKE_WIDTH, icon);
        } else {
            ButtonRenderer.paintButton(this, g, 
                    fillColor, false, 
                    ButtonRenderer.NORMAL_BORDER_COLOR, 
                    ButtonRenderer.NORMAL_STROKE_WIDTH, icon);
        }
    }
}
