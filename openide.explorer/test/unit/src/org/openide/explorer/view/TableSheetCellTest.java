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

package org.openide.explorer.view;

import junit.framework.TestCase;

/**
 * TableSheetCell tests.
 *
 * @author Martin Krauskopf
 */
public class TableSheetCellTest extends TestCase {

    public TableSheetCellTest(String testName) {
        super(testName);
    }

    public void testHtmlTooltipCreation() {

        String text = ">\"main\" is not a known variable in current context<"; // NOI18N
        assertEquals("<html>&gt;\"main\" is not a known variable in current context&lt;</html>", // NOI18N
                TableSheetCell.createHtmlTooltip(text, null));
        
        // non-html should be escaped
        String noHtml = "\"<html><b>ahoj</b></html>\""; // NOI18N
        assertEquals("<html>\"&lt;html&gt;&lt;b&gt;ahoj&lt;/b&gt;&lt;/html&gt;\"</html>", // NOI18N
                TableSheetCell.createHtmlTooltip(noHtml, null));
        
        // html should be returned as html
        String html = "<html><b>ahoj</b></html>"; // NOI18N
        assertEquals("<html>&lt;html&gt;&lt;b&gt;ahoj&lt;/b&gt;&lt;/html&gt;</html>", // NOI18N
                TableSheetCell.createHtmlTooltip(html, null));
        
        // should return "null" for null values
        assertEquals("null", TableSheetCell.createHtmlTooltip(null, null)); // NOI18N
    }
}
