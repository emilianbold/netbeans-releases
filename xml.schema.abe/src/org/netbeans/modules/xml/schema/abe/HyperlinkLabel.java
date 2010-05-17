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

/*
 * HyperlinkLabel.java
 *
 * Created on September 18, 2006, 7:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

/**
 *
 * @author girix
 */
public class HyperlinkLabel extends JLabel{
    private static final long serialVersionUID = -483941387931729295L;
    /** Creates a new instance of HyperlinkLabel */
    public HyperlinkLabel() {
        super();
        initialize();
    }
    
    private void initialize(){
        initMouseListener();
    }
    
    boolean mouseIn = false;
    private void initMouseListener() {
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if(hyperlinkClickHandler != null)
                    hyperlinkClickHandler.handleClick();
            }
            public void mouseEntered(MouseEvent e) {
                if(hyperlinkClickHandler != null){
                    mouseIn = true;
                    HyperlinkLabel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if(hyperlinkClickHandler != null){
                    mouseIn = false;
                    HyperlinkLabel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
        });
    }
    
    protected void paintComponent(Graphics g) {
        if(mouseIn){
            //draw diff color and underline
            Color origC = getForeground();
            Font origF = getFont();
            Rectangle bounds = g.getClipBounds();
            Color bak = getForeground();
            setForeground(Color.BLUE);
            Color gbak = g.getColor();
            g.setColor(Color.blue);
            super.paintComponent(g);
            int width = bounds.width;
            g.drawLine(bounds.x+5, bounds.y + bounds.height -1,
                    bounds.x+5 + width - 5,  bounds.y + bounds.height -1);
            setForeground(bak);
            g.setColor(gbak);
        }else{
            super.paintComponent(g);
        }
    }
    
    
    HyperlinkClickHandler hyperlinkClickHandler;
    public void setHyperlinkClickHandler(HyperlinkClickHandler hyperlinkClickHandler){
        this.hyperlinkClickHandler = hyperlinkClickHandler;
        
    }
    
    public interface HyperlinkClickHandler{
        public void handleClick();
    }
    
}
