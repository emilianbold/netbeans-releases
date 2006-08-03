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

package general;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import org.netbeans.junit.NbTestCase;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Vita Stejskal
 */
public class EditorKitsRegistryTest extends NbTestCase {
    
    /** Creates a new instance of EditorKitsRegistryTest */
    public EditorKitsRegistryTest(String name) {
        super(name);
    }
    
    public void testHTMLEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "text/html");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/html", kitFromJdk);
        assertTrue("Wrong JDK kit for text/html", kitFromJdk instanceof HTMLEditorKit);
        
        // Test Netbeans kit
        EditorKit kitFromNb = CloneableEditorSupport.getEditorKit("text/html");
        assertNotNull("Can't find Nb kit for text/html", kitFromNb);
        assertEquals("Wrong Nb kit for text/html", 
            "org.netbeans.modules.editor.html.HTMLKit", kitFromNb.getClass().getName());
    }

    public void testPlainEditorKits() {
        // VIS: JEditorPane when constructed contains javax.swing.JEditorPane$PlainEditorKit
        // and calling JEP.setContenetType("text/plain") has no effect. IMO this is probably
        // a defect in JDK, becuase JEP should always honour its EditorKit registry.
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(new DefaultEditorKit() {
            public String getContentType() {
                return "text/whatever";
            }
        });
        setContentTypeInAwt(pane, "text/plain");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/plain", kitFromJdk);
        assertEquals("The kit for text/plain should not be from JDK", 
            "org.netbeans.modules.editor.plain.PlainKit", kitFromJdk.getClass().getName());

        // Test Netbeans kit
        EditorKit kitFromNb = CloneableEditorSupport.getEditorKit("text/plain");
        assertNotNull("Can't find Nb kit for text/plain", kitFromNb);
        assertEquals("Wrong Nb kit for text/plain", 
            "org.netbeans.modules.editor.plain.PlainKit", kitFromNb.getClass().getName());
    }

    public void testTextRtfEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "text/rtf");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/rtf", kitFromJdk);
        assertTrue("Wrong JDK kit for application/rtf", kitFromJdk instanceof RTFEditorKit);
    }

    public void testApplicationRtfEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "application/rtf");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for application/rtf", kitFromJdk);
        assertTrue("Wrong JDK kit for application/rtf", kitFromJdk instanceof RTFEditorKit);
    }
    
    private void setContentTypeInAwt(final JEditorPane pane, final String mimeType) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    pane.setContentType(mimeType);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't set content type in AWT: " + e.getMessage());
        }
    }
}
