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
import java.util.Locale;
import javax.swing.JButton;
import junit.framework.TestCase;
import junit.framework.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulachs
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
            "<input type='hidden' name='submitAndExit' value=\"Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        Object[] buttons = Installer.parseButtons(is, null);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertNull("Second is null", buttons[1]);
        assertEquals("There is one button", 2, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
    }
    
    public void testReadListOfSubmitButtonsWithAmpersand() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='submitAndExit' value=\"&amp;Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        Object[] buttons = Installer.parseButtons(is, null);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There is one button", 2, buttons.length);
        assertNull("Second is null", buttons[1]);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
        assertEquals("Mnemonics", 'S', b.getMnemonic());
    }
}
