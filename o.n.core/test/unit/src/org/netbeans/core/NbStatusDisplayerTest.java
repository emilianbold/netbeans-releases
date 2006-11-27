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
package org.netbeans.core;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.StatusDisplayer;

/** Basic tests on NbClipboard
 *
 * @author Jaroslav Tulach
 */
public class NbStatusDisplayerTest extends NbTestCase {

    public NbStatusDisplayerTest(String testName) {
        super(testName);
    }

    public void testFiringWhenSameValueIsSet() {
       StatusDisplayer d = new NbTopManager.NbStatusDisplayer();
       d.setStatusText("ahoj");
       CountingListener lsnr = new CountingListener();
       d.addChangeListener(lsnr);
       d.setStatusText("ahoj");
       assertEquals("event is fired even when setting the same text again", 1, lsnr.getCount());
    }
    
    public static class CountingListener implements ChangeListener {
        private int count;
        
        public void stateChanged(ChangeEvent arg0) {
            count++;
        }
        public int getCount() {
            return count;
        }
    }
}
