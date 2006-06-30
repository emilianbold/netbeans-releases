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

    public void testGetUserDir() {
        System.setProperty("netbeans.user", "${user.home}/mine");
        
        String expResult = System.getProperty("user.home") + File.separator + "mine";
        Main.fixNetBeansUser();
        String result = System.getProperty("netbeans.user");
        assertEquals(expResult, result);
    }

}
