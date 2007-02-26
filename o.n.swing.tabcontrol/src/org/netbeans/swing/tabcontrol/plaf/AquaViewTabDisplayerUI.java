/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;

import javax.swing.plaf.ComponentUI;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A view tabs ui for OS-X adapted from the view tabs UI for Metal.
 *
 * @author Tim Boudreau
 */
public final class AquaViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    private static final int TXT_X_PAD = 5;
    private static final int ICON_X_PAD = 2;

    /********* static fields ***********/
    
    private static Map<Integer, String[]> buttonIconPaths;
    
    /**
     * ******* instance fields *********
     */

    private Dimension prefSize;
    /**
     * Reusable Rectangle to optimize rectangle creation/garbage collection
     * during paints
     */
    private Rectangle tempRect = new Rectangle();

    /**
     * Should be constructed only from createUI method.
     */
    private AquaViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19); //XXX huh?
    }

    public static ComponentUI createUI(JComponent c) {
        return new AquaViewTabDisplayerUI((TabDisplayer) c);
    }

    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }
    
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                21 : fm.getAscent() + 2 * fm.getDescent() + 3;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    public void paint(Graphics g, JComponent c) {
        ColorUtil.setupAntialiasing(g);
        super.paint(g, c);
        paintBottomBorder(g, c);
    }
    
    protected Font getTxtFont() {
        return getDisplayer().getFont();
    }    

    /**
     * Paints bottom "activation" line
     */
    private void paintBottomBorder(Graphics g, JComponent c) {
    }

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();

        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int textW = width;

        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();

                textW = width - (buttonsSize.width + ICON_X_PAD + 2*TXT_X_PAD);
                if (index == getDataModel().size() - 1) {
                    textW -= 3;
                }
                buttons.setLocation( x + textW+2*TXT_X_PAD, y + (height-buttonsSize.height)/2 );
            }
        } else {
            textW = width - 2 * TXT_X_PAD;
        }

        if (text.length() == 0) {
            return;
        }

        int textHeight = fm.getHeight();
        int textY;
        int textX = x + TXT_X_PAD;
	if (index == 0)
	    textX = x + 5;

        if (textHeight > height) {
            textY = (-1 * ((textHeight - height) / 2)) + fm.getAscent()
                    - 1;
        } else {
            textY = (height / 2) - (textHeight / 2) + fm.getAscent();
        }

        HtmlRenderer.renderString(text, g, textX, textY, textW, height, getTxtFont(),
                          UIManager.getColor("textText"),
                          HtmlRenderer.STYLE_TRUNCATE, true);
    }
    
    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {

    }

    private static final ChicletWrapper chiclet = new ChicletWrapper();

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        boolean first = index == 0;
        boolean last = index == getDataModel().size() - 1;
        int state = 0;
        if (isActive()) {
            state |= GenericGlowingChiclet.STATE_ACTIVE;
        }
        if (isSelected(index)) {
            state |= GenericGlowingChiclet.STATE_SELECTED;
        }
        if (isAttention(index)) {
            state |= GenericGlowingChiclet.STATE_ATTENTION;
        }
        
        y+=1; //align with top of editor tabs
        
        chiclet.setState(state);
        chiclet.setBounds(x, y, width, height);
        chiclet.setArcs(first ? 0.5f : 0f, last ? 0.5f : 0f,
                         first ? 0.0f : 0f, last ? 0.0f : 0f);
        chiclet.setNotch(false, false);
        g.translate (x, y);
        chiclet.draw((Graphics2D) g);
        g.translate (-x, -y);
    }

    private boolean containsMouse = false;

    private void setContainsMouse(boolean val) {
        if (val != containsMouse) {
            containsMouse = val;
            getDisplayer().repaint();
        }
    }

    private boolean isContainsMouse() {
        return containsMouse;
    }

    private static void initIcons() {
        //TODO add icons for aqua l&f
        
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
        }
    }

    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }
    
    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {
        
        public void mouseEntered(MouseEvent me) {
            super.mouseEntered(me);
            setContainsMouse(true);
        }

        public void mouseExited(MouseEvent me) {
            super.mouseExited(me);
            setContainsMouse(false);
        }
    } // end of OwnController

}
