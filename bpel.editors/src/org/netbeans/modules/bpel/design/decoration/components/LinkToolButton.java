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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.FlowlinkTool;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author aa160298
 */
public class LinkToolButton extends JLabel
        implements DecorationComponent, MouseListener {
    

    private UniqueId omReference;
    private DesignView designView;
    
    //private Pattern pattern;
 
    public LinkToolButton(Pattern pattern) {
        super(ICON_GRAY);

        omReference = pattern.getOMReference().getUID();
        designView = pattern.getModel().getView();
        
        setOpaque(false);
        setBorder(null);
        setBackground(null);
        setFocusable(false);
        
        setPreferredSize(new Dimension(16, 16));
        
        addMouseListener(this);
    }

    
    public Pattern getPattern(){
        BpelEntity be = omReference.getModel().getEntity(omReference);
        return (be == null) ? null : designView.getModel().getPattern(be);
    }

    
    public void setPosition(Point p) {
        Dimension size = getPreferredSize();
        setBounds(p.x - size.width / 2, p.y - size.height / 2, size.width, size.height);
    }

    
    protected void paintComponent(Graphics g) {
        Point point = getMousePosition();
        
        FlowlinkTool flowLinkTool = getPattern().getModel().getView()
                .getFlowLinkTool();
        
        if (flowLinkTool.isActive()) {
            if (flowLinkTool.isValidLocation()) {
                ICON.paintIcon(this, g, 0, 0);
            } else {
                ICON_RED.paintIcon(this, g, 0, 0);
            }
        } else {
            boolean rollover = (point != null) && (0 <= point.x) 
                    && (point.x < getWidth()) && (0 <= point.y) 
                    && (point.y < getHeight());

            if (rollover) {
                ICON.paintIcon(this, g, 0, 0);
            } else {
                ICON_GRAY.paintIcon(this, g, 0, 0);
            }
        }
    }
    

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    
    public void mouseEntered(MouseEvent e) { 
        repaint(); 
    }
    
    
    public void mouseExited(MouseEvent e) { 
        repaint(); 
    }    
    
    
    private static final String ICON_PATH = "resources/envelope_small.png"; // NOI18N
    private static final String ICON_GRAY_PATH = "resources/envelope_small_gray.png"; // NOI18N
    private static final String ICON_RED_PATH = "resources/envelope_small_red.png"; // NOI18N
    
    private static final Icon ICON;
    private static final Icon ICON_GRAY;
    private static final Icon ICON_RED;
    
    static {
        ICON = new ImageIcon(Decoration.class.getResource(ICON_PATH));
        ICON_GRAY = new ImageIcon(Decoration.class.getResource(ICON_GRAY_PATH));
        ICON_RED = new ImageIcon(Decoration.class.getResource(ICON_RED_PATH));
    }
}
