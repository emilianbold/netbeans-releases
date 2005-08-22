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

package org.netbeans.modules.apisupport.jnlplauncher;

import junit.framework.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.security.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

/** Can we specify ${user.home} in name of userdirectory?
 *
 * @author Jaroslav Tulach
 */
public class ReplaceUserDirTest extends TestCase {
    
    public ReplaceUserDirTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testGetUserDir() {
        System.setProperty("netbeans.user", "${user.home}/mine");
        
        String expResult = System.getProperty("user.home") + File.separator + "mine";
        Main.fixNetBeansUser();
        String result = System.getProperty("netbeans.user");
        assertEquals(expResult, result);
    }

}
