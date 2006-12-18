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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.plaf.ComponentUI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import org.openide.awt.HtmlRenderer;

/**
 * GTK user interface of view type tabs. It uses native engine to paint tab
 * background.
 *
 * @author Dafe Simonek
 */
public final class GtkViewTabDisplayerUI extends AbstractViewTabDisplayerUI {
    
    /**
     * ******** constants ************
     */

    private static final int BUMP_X_PAD = 0;
    private static final int BUMP_WIDTH = 0;
    private static final int TXT_X_PAD = 3;
    private static final int TXT_Y_PAD = 6;

    private static final int ICON_X_PAD = 2;
    
    private static Map<Integer, String[]> buttonIconPaths;

    private static JTabbedPane dummyTab;
    
    /**
     * ******** instance fields ********
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
    private GtkViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19);
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new GtkViewTabDisplayerUI((TabDisplayer) c);
    }

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                19 : fm.getAscent() + 2 * fm.getDescent() + 3;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * adds painting of overall border
     */
    public void paint(Graphics g, JComponent c) {

        ColorUtil.setupAntialiasing(g);

        Color col = c.getBackground();
        if (col != null) {
            g.setColor (col);
            g.fillRect (0, 0, c.getWidth(), c.getHeight());
        }
        paintOverallBorder(g, c);
        super.paint(g, c);
    }

    /**
     * Paints lower border, bottom line, separating tabs from content
     */
    protected void paintOverallBorder(Graphics g, JComponent c) {
        return;
    }
    
    protected Font getTxtFont() {
        Font result = UIManager.getFont("controlFont");
        if (result != null) {
            return result;
        }
        return super.getTxtFont();
    }     

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        // substract lower border
        height--;
        y -= 2; //align to center
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();
                txtWidth = width - (buttonsSize.width + ICON_X_PAD + 2*TXT_X_PAD);
                buttons.setLocation(x + txtWidth + 2 * TXT_X_PAD, y + (height - buttonsSize.height)/2 + (TXT_Y_PAD / 2) + 1);
            }
        } else {
            txtWidth = width - 2 * TXT_X_PAD;
        }
        // draw bump (dragger)
        drawBump(g, index, x + 4, y + 6, BUMP_WIDTH, height - 8);
        
        // draw text in right color
        Color txtC = UIManager.getColor("TabbedPane.foreground"); //NOI18N
        
        HtmlRenderer.renderString(text, g, x + TXT_X_PAD, y + fm.getAscent()
            + TXT_Y_PAD,
            txtWidth, height, getTxtFont(),
            txtC,
            HtmlRenderer.STYLE_TRUNCATE, true);
    }

    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {

        return;
    }
    
    private static void paintTabBackgroundNative (Graphics g, int index, int state,
    int x, int y, int w, int h) {
        if (dummyTab == null) {
            dummyTab = new JTabbedPane();
        }
        Region region = Region.TABBED_PANE_TAB;
        SynthLookAndFeel laf = (SynthLookAndFeel) UIManager.getLookAndFeel();
        SynthStyleFactory sf = laf.getStyleFactory();
        SynthStyle style = sf.getStyle(dummyTab, region);
        SynthContext context =
            new SynthContext(dummyTab, region, style, state);
        SynthPainter painter = style.getPainter(context);
        painter.paintTabbedPaneTabBackground(context, g, x, y, w, h, index);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        if (isSelected(index)) {
            paintTabBackgroundNative(g, 0, SynthConstants.SELECTED,
            x, y, width, height);
        } else {
            paintTabBackgroundNative(g, 0, 0,
            x, y + 1, width, height - 1);
        }        
    }

    /**
     * Paints dragger in given rectangle
     */
    private void drawBump(Graphics g, int index, int x, int y, int width,
                          int height) {
            //This look and feel is also used as the default UI on non-JDS
        return;
    }
    
    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_bigclose_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/win_bigclose_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_bigclose_enabled.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win-pin-normal-east.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-east.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-east.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win-pin-normal-west.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-west.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-west.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win-pin-normal-south.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-south.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-south.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win-pin-normal-center.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-center.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-center.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            //TODO add more icons
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_titlebar_maximize_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_titlebar_maximize_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_titlebar_maximize_normal.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_titlebar_restore_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_titlebar_restore_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_titlebar_restore_normal.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
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
    
}
