/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonListener;

/**
 *
 * @author Alexander Simon (adapted for CND)
 */
public class UIUtils {

    private static final float ALTERNATE_ROW_DARKER_FACTOR = 0.96f;
    private static final float BRDER_LINE_DARKER_FACTOR = 0.65f;
    private static Color unfocusedSelBg;
    private static Color unfocusedSelFg;
    /** Color used to draw vertical gridlines in JTables */
    public static final Color TABLE_VERTICAL_GRID_COLOR = new Color(214, 223, 247); //Color.LIGHT_GRAY;
    /** Color used for painting selected cell background in JTables */
    public static final Color TABLE_SELECTION_BACKGROUND_COLOR = new Color(193, 210, 238); //(253, 249, 237)
    /** Color used for painting selected cell foreground in JTables */
    public static final Color TABLE_SELECTION_FOREGROUND_COLOR = Color.BLACK;

    private UIUtils() {
    }

    static Color getProfilerResultsBackground() {
        return Color.WHITE;
    }

    public static Color getDarker(Color c) {
        if (c.equals(Color.WHITE)) {
            return new Color(244, 244, 244);
        }
        return getDarker(c, ALTERNATE_ROW_DARKER_FACTOR);
    }

    public static Color getDarker(Color c, float ratio) {
        return getSafeColor((int) (c.getRed() * ratio),
                (int) (c.getGreen() * ratio),
                (int) (c.getBlue() * ratio));
    }

    public static Color getDarkerLine(Color c) {
        return getSafeColor((int) (c.getRed() * BRDER_LINE_DARKER_FACTOR),
                (int) (c.getGreen() * BRDER_LINE_DARKER_FACTOR),
                (int) (c.getBlue() * BRDER_LINE_DARKER_FACTOR));
    }

    // Copied from org.openide.awt.HtmlLabelUI
    /** Get the system-wide unfocused selection background color */
    public static Color getUnfocusedSelectionBackground() {
        if (unfocusedSelBg == null) {
            //allow theme/ui custom definition
            unfocusedSelBg = UIManager.getColor("nb.explorer.unfocusedSelBg"); //NOI18N

            if (unfocusedSelBg == null) {
                //try to get standard shadow color
                unfocusedSelBg = UIManager.getColor("controlShadow"); //NOI18N

                if (unfocusedSelBg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelBg = Color.lightGray;
                }

                //Lighten it a bit because disabled text will use controlShadow/
                //gray
                if (!Color.WHITE.equals(unfocusedSelBg.brighter())) {
                    unfocusedSelBg = unfocusedSelBg.brighter();
                }
            }
        }

        return unfocusedSelBg;
    }

    // Copied from org.openide.awt.HtmlLabelUI
    /** Get the system-wide unfocused selection foreground color */
    public static Color getUnfocusedSelectionForeground() {
        if (unfocusedSelFg == null) {
            //allow theme/ui custom definition
            unfocusedSelFg = UIManager.getColor("nb.explorer.unfocusedSelFg"); //NOI18N

            if (unfocusedSelFg == null) {
                //try to get standard shadow color
                unfocusedSelFg = UIManager.getColor("textText"); //NOI18N

                if (unfocusedSelFg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelFg = Color.BLACK;
                }
            }
        }

        return unfocusedSelFg;
    }

    public static Color getSafeColor(int red, int green, int blue) {
        red = Math.max(red, 0);
        red = Math.min(red, 255);
        green = Math.max(green, 0);
        green = Math.min(green, 255);
        blue = Math.max(blue, 0);
        blue = Math.min(blue, 255);

        return new Color(red, green, blue);
    }

    public static void runInEventDispatchThread(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public static void runInEventDispatchThreadAndWait(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Classic Windows LaF doesn't draw dotted focus rectangle inside JButton if parent is JToolBar,
    // XP Windows LaF doesn't draw dotted focus rectangle inside JButton at all
    // This method installs customized Windows LaF that draws dotted focus rectangle inside JButton always
    // On JDK 1.5 the XP Windows LaF enforces special border to all buttons, overriding any custom border
    // set by setBorder(). Class responsible for this is WindowsButtonListener. See Issue 71546.
    // Also fixes buttons size in JToolbar.
    /** Ensures that focus will be really painted if button is focused
     * and fixes using custom border for JDK 1.5 & XP LaF
     */
    public static void fixButtonUI(AbstractButton button) {
        // JButton
        if (button.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsButtonUI) {
            button.setUI(new com.sun.java.swing.plaf.windows.WindowsButtonUI() {

                @Override
                protected BasicButtonListener createButtonListener(AbstractButton b) {
                    return new BasicButtonListener(b); // Fix for  Issue 71546
                    }

                @Override
                protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect,
                        Rectangle iconRect) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    g.setColor(getFocusColor());
                    javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
                            width - dashedRectGapWidth,
                            height - dashedRectGapHeight);
                }
            });
        } else if (button.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsToggleButtonUI) {
            // JToggleButton
            button.setUI(new com.sun.java.swing.plaf.windows.WindowsToggleButtonUI() {

                @Override
                protected BasicButtonListener createButtonListener(AbstractButton b) {
                    return new BasicButtonListener(b); // Fix for  Issue 71546
                    }

                @Override
                protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect,
                        Rectangle iconRect) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    g.setColor(getFocusColor());
                    javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
                            width - dashedRectGapWidth,
                            height - dashedRectGapHeight);
                }
            });
        }
    }

    /** Determines if current L&F is GTKLookAndFeel */
    public static boolean isGTKLookAndFeel() {
        // is current L&F some kind of GTKLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("GTK"); //NOI18N
    }

    /** Determines if current L&F is Nimbus */
    public static boolean isNimbusLookAndFeel() {
        // is current L&F Nimbus?
        return UIManager.getLookAndFeel().getID().equals("Nimbus"); //NOI18N
    }

    public static boolean isMotifLookAndFeel() {
        // is current L&F some kind of GTKLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("Motif"); //NOI18N
    }
}
