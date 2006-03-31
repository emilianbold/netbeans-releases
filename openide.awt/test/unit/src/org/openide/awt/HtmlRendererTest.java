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

package org.openide.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import junit.framework.TestCase;

/**
 *
 * @author mkleint
 */
public class HtmlRendererTest extends TestCase {
    
    private Graphics graphic;
    public HtmlRendererTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        BufferedImage waitingForPaintDummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        graphic = waitingForPaintDummyImage.getGraphics();
        
    }
    
    /**
     * Test of renderHTML method, of class org.openide.awt.HtmlRenderer.
     */
    public void testRenderHTML() throws Exception {
        doTestRender("<html>text</html>");
        doTestRender("<html>text</html");
        doTestRender("<html>text</h");
        doTestRender("<html>text</");
        doTestRender("<html>text<");
        doTestRender("<html>text");
        doTestRender("<html>text</html<html/>");
        doTestRender("<html>text</h</html>");
        doTestRender("<html>text</</html>");
        doTestRender("<html>text<</html>");
        doTestRender55310();
    }
    
    private void doTestRender(String text) {
        try {
            HtmlRenderer.renderHTML(text, graphic, 0, 0, 1000, 1000,
                    Font.getFont("Dialog"), Color.RED, HtmlRenderer.STYLE_TRUNCATE, true);
        } catch (IllegalArgumentException arg) {
            // argument exception is ok..
            if (arg.getMessage().startsWith("HTML rendering failed on string")) {
                System.err.println("throwing illegal argument for " + text);
            } else {
                throw arg;
            }
        }
    }
    
    /**
     * Test issue #55310: AIOOBE from HtmlRenderer.
     *
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=55310
     */
    private void doTestRender55310() {
        doTestRender("<html><b>a </b></html> ");
    }
}
