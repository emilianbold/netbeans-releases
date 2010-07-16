/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.iep.editor.xsd;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author radval
 */
public class CheckBoxPanel extends JPanel {

    private JCheckBox mCheckBox = new JCheckBox();
    private JLabel mLabel = new JLabel();
    public CheckBoxPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(mCheckBox);
        add(mLabel);
        
        mCheckBox.setBackground(UIManager.getColor("Tree.textBackground"));
       
        mCheckBox.setBorder(null);
        mLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        mLabel.addMouseListener(new LabelMouseAdapter());
    }
    
    public Dimension getPreferredSize() {
    Dimension d_check = mCheckBox.getPreferredSize();
    Dimension d_label = mLabel.getPreferredSize();
    return new Dimension(d_check.width  + d_label.width,
      (d_check.height < d_label.height ?
       d_label.height : d_check.height));
  }
        
    public void doLayout() {
        Dimension d_check = mCheckBox.getPreferredSize();
        Dimension d_label = mLabel.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height) {
          y_check = (d_label.height - d_check.height)/2;
        } else {
          y_label = (d_check.height - d_label.height)/2;
        }
//        check.setLocation(0,y_check);
        mCheckBox.setBounds(0,y_check,d_check.width,d_check.height);
//        label.setLocation(d_check.width,y_label);    
        mLabel.setBounds(d_check.width,y_label,d_label.width,d_label.height);    
  }
    
    public void setText(String text) {
        mLabel.setText(text);
    }
    
    public void setIcon(Icon icon) {
        mLabel.setIcon(icon);
    }
    
    public void setSelected(boolean selected) {
        mCheckBox.setSelected(selected);
    }
    
    public void setFocusPainted(boolean focusPainted) {
        mCheckBox.setFocusPainted(focusPainted);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setBackground(java.awt.Color)
     */
    @Override
    public void setBackground(Color bg) {
	super.setBackground(bg);
	if (mLabel != null) {
	    mLabel.setBackground(bg);
	}
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setForeground(java.awt.Color)
     */
    @Override
    public void setForeground(Color fg) {
	super.setForeground(fg);
	if (mLabel != null) {
	    mLabel.setForeground(fg);
	}
    }
    
    @Override
    public void paint(Graphics g) {
      boolean isSelected = mCheckBox.isSelected();
      if (isSelected) {
        g.setColor(UIManager.getColor("Tree.selectionBackground"));
      } else {
        g.setColor(UIManager.getColor("Tree.textBackground"));
      }

//      String str;
//      if ((str = getText()) != null) {
//        if (0 < str.length()) {
//          if (isSelected) {
//            g.setColor(UIManager.getColor("Tree.selectionBackground"));
//          } else {
//            g.setColor(UIManager.getColor("Tree.textBackground"));
//          }
//          Dimension d = getPreferredSize();
//          int imageOffset = 0;
//          Icon currentI = getIcon();
//          if (currentI != null) {
//            imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
//          }
//          g.fillRect(imageOffset, 0, d.width -1 - imageOffset, d.height);
//          if (hasFocus) {
//            g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
//            g.drawRect(imageOffset, 0, d.width -1 - imageOffset, d.height -1);     
//         }
//        }
//      }
      super.paint(g);
    }
    
    public JCheckBox getCheckBox(){
        return this.mCheckBox;
    }
    
    class LabelMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            mCheckBox.setSelected(!mCheckBox.isSelected());
        }
        
        
    }
    
    
}
