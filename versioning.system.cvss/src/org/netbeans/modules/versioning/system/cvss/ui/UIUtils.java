/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;

/**
 * Swing toolkit utility methods.
 *
 * @author Petr Kuzel
 */
public class UIUtils {

    /**
     * Computes and sets label preferred size based
     * on <b>preferred width</b>. Compare to standard preferred
     * size algorithm that takes width from longest line
     * (it does not count with fact that HTML label wraps).
     *
     * <p>You can rollback by <code>label.setPreferredSize(null)</code>;
     *
     * @param label HTML enabled imageless label to be statically patched
     * @param ex preferred width in number of <tt>x</tt> characters in current label font
     */
    public static void computePreferredSize(JLabel label, int ex) {
        assert ex > 0;
        FontMetrics fm = label.getFontMetrics(label.getFont());
        StringBuffer sb = new StringBuffer(ex);
        for (int i = 0; i<ex; i++) {
            sb.append("x");  // NOI18N
        }
        int exWidth = SwingUtilities.computeStringWidth(fm, sb.toString());
        View v = BasicHTML.createHTMLView(label, label.getText());
        v.setSize(exWidth, 0);
        int width = (int) v.getPreferredSpan(View.X_AXIS);
        int height = (int) v.getPreferredSpan(View.Y_AXIS);

        Insets insets = label.getInsets();
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;

        label.setPreferredSize(new Dimension(width+dx, height+dy));
    }
}
