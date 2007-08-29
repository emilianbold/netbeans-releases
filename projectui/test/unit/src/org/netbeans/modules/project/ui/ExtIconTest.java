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

package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ExtIconTest extends TestCase {
    
    public ExtIconTest(String testName) {
        super(testName);
    }
    
    public void testByteConversions() {
        ExtIcon ext = new ExtIcon();
        URL res = getClass().getClassLoader().getResource("org/netbeans/modules/project/ui/module.gif");
        assertNotNull(res);
        ImageIcon icon = new ImageIcon(res);
        ext.setIcon(icon);
        try {
            byte[] bytes1 = ext.getBytes();
            ExtIcon ext2 = new ExtIcon(bytes1);
            byte[] bytes2 = ext2.getBytes();
            ExtIcon ext3 = new ExtIcon(bytes2);
            byte[] bytes3 = ext3.getBytes();
            
            assertEquals(bytes1.length, bytes2.length);
            assertEquals(bytes3.length, bytes3.length);
            for (int i = 0; i < bytes1.length; i++) {
                assertEquals("Non equals at position " + i,bytes1[i], bytes2[i]);
                assertEquals("Non equals at position " + i,bytes1[i], bytes3[i]);
            }
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        }
        
    }
    
}
