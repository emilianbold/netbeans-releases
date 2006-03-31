/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
