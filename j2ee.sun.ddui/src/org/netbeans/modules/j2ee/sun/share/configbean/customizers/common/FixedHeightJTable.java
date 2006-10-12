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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JTable;

/** JTables do not automatically scale their row height with font size, so when
 *  running under, for example --fontsize 16 (common for Chinese), the rows of our
 *  tables are too narrow and only marginally usable.  For --fontsize 24, the text
 *  overlap makes the rows illegible.
 * 
 *  This code is take from a posting Tim Boudreau made on javalobby and automatically
 *  scales the row height with the font size or if the font is changed.  It does not 
 *  take icon height into account  but we don't use icons anywhere.
 *
 * @author Peter Williams
 */
public class FixedHeightJTable extends JTable {

    private boolean firstPaint = true;

    public void setFont (Font f) {
        firstPaint = true;
        super.setFont(f);
    }

    private void calcFixedHeight (Graphics g) {
        g.setFont (getFont());
        setRowHeight(g.getFontMetrics().getHeight());
        firstPaint = false;
    }

    public void paint (Graphics g) {
        if (firstPaint) {
            calcFixedHeight(g);
            //Setting the fixed height will generate another paint request,
            //no need to complete this one
            return;
        }
        super.paint (g);
    }
}
