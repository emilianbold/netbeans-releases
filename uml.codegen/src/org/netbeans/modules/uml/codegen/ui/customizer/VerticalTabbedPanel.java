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

package org.netbeans.modules.uml.codegen.ui.customizer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;


/**
 * @author Jan Jancura
 */
public class VerticalTabbedPanel extends TabbedPanel 
{
    public VerticalTabbedPanel(TabbedPanelModel model, int expansionPolicy) 
    {
        super(model, expansionPolicy, false);
        setBackground(Color.white);
    }
    
    protected JComponent createTitleComponent(
        String name,
        String toolTip,
        final int index) 
    {
        final JLabel label = new JLabel(
            name,
            isExpanded(index) 
                ? (Icon)UIManager.get("Tree.expandedIcon") // NOI18N
                : (Icon)UIManager.get("Tree.collapsedIcon"), // NOI18N
            JLabel.LEFT
        );
        
        label.setToolTipText(toolTip);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBackground(Color.white);
        label.setForeground(Color.black);
        
        label.setOpaque(true);
        label.addMouseListener(new Listener1());
        label.putClientProperty("index", Integer.valueOf(index)); // NOI18N
        label.setFocusable(true);
        label.setFocusTraversalKeysEnabled(true);
        
        label.getActionMap().put(
            "SPACE",  // NOI18N
            new AbstractAction() 
            {
                public void actionPerformed(ActionEvent e) 
                {
                    if (getSelectedIndex() != index)
                        setSelectedIndex(index);
                
                    else
                        setSelectedIndex(-1);
                }
            }
        );
        
        label.getInputMap().put(
            KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0), 
            "SPACE");
        
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setBorder(new EmptyBorder(1, 3, 1, 1));
        
        label.addFocusListener (new FocusListener()
        {
            public void focusGained(FocusEvent e) 
            {
                label.setBorder(new CompoundBorder(
                    new DashedBorder(),
                    new EmptyBorder(0, 2, 0, 0)));
            }

            public void focusLost(FocusEvent e) 
            {
                label.setBorder(new EmptyBorder(1, 3, 1, 1));
            }
        });
        
        return label;
    }
    
    private class Listener1 implements MouseListener
    {
        public void mouseClicked(MouseEvent e)
        {
            if (!(e.getSource() instanceof JLabel)) 
                return;
            
            JLabel l = (JLabel)e.getSource();
            int i = ((Integer)l.getClientProperty("index")).intValue(); // NOI18N
            
            if (i == getSelectedIndex())
            {
                if (getExpansionPolicy() == EXPAND_SOME)
                    setSelectedIndex(-1);
            }
            
            else
                setSelectedIndex(i);
        }
        
        public void mousePressed(MouseEvent e)
        {}
        public void mouseReleased(MouseEvent e)
        {}
        public void mouseEntered(MouseEvent e)
        {}
        public void mouseExited(MouseEvent e)
        {}
    };
}
