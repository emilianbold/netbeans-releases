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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.DesignView;

/**
 * @author aa160298
 */
public class AbstractGlassPaneButton extends JToggleButton implements ActionListener, HierarchyListener, DecorationComponent {
    
    public AbstractGlassPaneButton(Icon icon) {
      this(icon, null, false, null);
    }

    public AbstractGlassPaneButton(Icon icon, String text, boolean editable, ActionListener actionListener) {
        super(icon);
        myIcon = icon;
        myGlassPane = new GlassPane(text, actionListener, editable);
        myActionListener = actionListener;
        addActionListener(this);
        addHierarchyListener(this);
        
        setOpaque(false);
        setBorder(null);
        setRolloverEnabled(true);
        setContentAreaFilled(false);
        setFocusable(false);
        
        updatePreferredSize();
        myGlassPane.addHierarchyListener(this);
    }

    protected void updatePreferredSize() {
        setPreferredSize(new Dimension(myIcon.getIconWidth() + 6, myIcon.getIconHeight() + 6));
    }

    public void actionPerformed(ActionEvent e) {
        if ( !isGlassPaneShown()) {
            showGlassPane();
        } else {
            hideGlassPane();
        }
    }

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        
        if (isGlassPaneShown()) {
            updateGlassPaneBounds();
        }
    }
    
    private void showGlassPane() {
        getDesignView().add(myGlassPane);
        updateGlassPaneBounds();
        myGlassPane.scrollRectToVisible(new Rectangle(0, 0, myGlassPane.getWidth(), myGlassPane.getHeight()));
    }
    
    // vlv
    protected void addTitle(Icon icon, String text, Color color) {
        myGlassPane.addHeader(icon, text, color);
    }

    private void hideGlassPane() {
        DesignView designView = (DesignView) myGlassPane.getParent();
        designView.remove(myGlassPane);
        designView.revalidate();
        designView.repaint();

        // vlv
        if (myActionListener != null) {
          myActionListener.actionPerformed(
            new ActionEvent(myGlassPane.getText(), 0, null));
        }
    }
    
    private DesignView getDesignView() {
        return (DesignView) SwingUtilities.getAncestorOfClass(DesignView.class, this);
    }
    
    private boolean isGlassPaneShown() {
        return myGlassPane.getParent() != null;
    }
    
    private void updateGlassPaneBounds() {
        DesignView designView = getDesignView();
        Point coords = SwingUtilities.convertPoint(this, getWidth() + 1, getHeight() / 2, designView);
        Dimension size = myGlassPane.getPreferredSize();
        myGlassPane.setBounds(coords.x, coords.y, size.width, size.height);
        designView.revalidate();
        designView.repaint();
    }
    
    public void hierarchyChanged(HierarchyEvent e) {
        if (e.getSource() == this) {
            if ((getParent() == null) && isGlassPaneShown()) {
                hideGlassPane();
            }
        } else if (e.getSource() == myGlassPane) {
            if (myGlassPane.getParent() == null) {
                setSelected(false);
            } else {
                setSelected(true);
            }
        }
    }
    
    protected void paintComponent(Graphics g) {
        ButtonModel model = getModel();
        
        if (model.isPressed()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.PRESSED_FILL_COLOR, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, myIcon);
        } else if (model.isRollover()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.ROLLOVER_FILL_COLOR, true, 
                    ButtonRenderer.ROLLOVER_BORDER_COLOR, 
                    ButtonRenderer.ROLLOVER_STROKE_WIDTH, myIcon);
            
        } else if (model.isSelected()) {
            ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, myIcon);
        } else {
            ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                    null, ButtonRenderer.NORMAL_STROKE_WIDTH, myIcon);
        }
    }

    protected void setMyIcon(Icon icon) {
      myIcon = icon;
    }

    protected Icon getMyIcon() {
      return myIcon;
    }

    protected GlassPane getGlassPane() {
      return myGlassPane;
    }

    private Icon myIcon; 
    private GlassPane myGlassPane;
    private ActionListener myActionListener;
    private static final Color BACKGROUND = new Color(0xCCFFFFFF, true);
}
