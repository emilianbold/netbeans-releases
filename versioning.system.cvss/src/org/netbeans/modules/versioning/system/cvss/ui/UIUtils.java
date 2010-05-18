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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
