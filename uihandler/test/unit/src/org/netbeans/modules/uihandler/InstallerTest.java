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

package org.netbeans.modules.uihandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.swing.JButton;
import junit.framework.TestCase;
import junit.framework.*;
import java.net.URL;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerTest extends TestCase {
    
    public InstallerTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testReadListOfSubmitButtons() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='submit' value=\"Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = Installer.parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("Second is default", def, buttons[1]);
        assertEquals("There is one button", 2, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
    }
    
    public void testReadListOfSubmitButtonsWithAmpersand() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = Installer.parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There is one button", 2, buttons.length);
        assertEquals("Second is default", def, buttons[1]);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
        assertEquals("Mnemonics", 'S', b.getMnemonic());
    }

    public void testCanDefineExitButton() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "\n" +
            "<input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "\n" +
            "<input type='hidden' name='exit' value=\"&amp;Cancel\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = Installer.parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are two buttons", 2, buttons.length);
        if (def == buttons[1]) {
            fail("Second is default: " + buttons[1]);
        }
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
        assertEquals("Mnemonics", 'S', b.getMnemonic());

        assertEquals("It is a button", JButton.class, buttons[1].getClass());
        b = (JButton)buttons[1];
        assertEquals("It is named", "Cancel", b.getText());
        assertNull("No url", b.getClientProperty("url"));
        assertEquals("Mnemonics", 'C', b.getMnemonic());
    }

    public void testReadAllButtons() throws Exception {
        String page = 
            "<html><body><form action='http://xyz.cz' method='POST'>" +
            "  <input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "  <input type='hidden' name='never-again' value=\"&amp;No and do not Bother Again\"/>" +
            "\n" +
            "  <input type='hidden' name='view-data' value=\"&amp;View Data\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        Object[] buttons = Installer.parseButtons(is, null);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are 3 button", 3, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        assertEquals("It is a button2", JButton.class, buttons[1].getClass());
        assertEquals("It is a button3", JButton.class, buttons[2].getClass());
        
        {
            JButton b = (JButton)buttons[0];
            assertEquals("It is named", "Send Feedback", b.getText());
            assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
            assertEquals("Mnemonics", 'S', b.getMnemonic());
            assertEquals("submit", b.getActionCommand());
            URL[] url = new URL[1];
            String r = Installer.decodeButtons(b, url);
            assertEquals("action is ", "submit", r);
            assertEquals("no url", new URL("http://xyz.cz"), url[0]);
        }
        {
            JButton b = (JButton)buttons[1];
            assertEquals("It is named", "No and do not Bother Again", b.getText());
            assertEquals("It url attribute is not set", null, b.getClientProperty("url"));
            assertEquals("Mnemonics", 'N', b.getMnemonic());
            assertEquals("never-again", b.getActionCommand());
        }
        {
            JButton b = (JButton)buttons[2];
            assertEquals("It is named", "View Data", b.getText());
            assertEquals("It url attribute is not set", null, b.getClientProperty("url"));
            assertEquals("Mnemonics", 'V', b.getMnemonic());
            assertEquals("view-data", b.getActionCommand());
            
            URL[] url = new URL[1];
            String r = Installer.decodeButtons(b, url);
            assertEquals("action is ", "view-data", r);
            assertEquals("no url", null, url[0]);
        }
    }
}
