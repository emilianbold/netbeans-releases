/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.exceptions;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsSettingsTest extends NbTestCase {
    
    public ExceptionsSettingsTest(String testName) {
        super(testName);
    }

    public void testEmpty(){
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings.getPasswd());
    }

    public void testUserName() {
        String str = "Moje_Jmeno";
        String previous;
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        previous = settings.getUserName();
        settings.setUserName(str);
        assertEquals(str, settings.getUserName());
        settings.setUserName(previous);
        assertEquals(previous, settings.getUserName());
    }

    public void testPasswd() {
        char[] str = "MY_PASSWD".toCharArray();
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        settings.setPasswd(str);
        assertArraysEquals("MY_PASSWD".toCharArray(), settings.getPasswd());
    }

    public void testIsGuest() {
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        boolean previous = settings.isGuest();
        settings.setGuest(true);
        assertTrue(settings.isGuest());
        settings.setGuest(false);
        assertFalse(settings.isGuest());
        settings.setGuest(previous);
        assertEquals(previous, settings.isGuest());
    }

    public void testSaveUserData(){
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        settings.setGuest(false);
        settings.setPasswd("HALLO".toCharArray());
        settings.setRememberPasswd(false);
        ReportPanel panel = new ReportPanel();
        assertArraysEquals("correctly loaded", "HALLO".toCharArray(), panel.getPasswdChars());
        assertEquals("correctly loaded", false, panel.asAGuest());
        panel.saveUserData();
        panel = new ReportPanel();
        assertArraysEquals("should not save passwd", new char[0], panel.getPasswdChars());
        assertEquals("correctly loaded", false, panel.asAGuest());
    }

    public void assertArraysEquals(String message, char[] x, char[] y){
        assertEquals(message, x.length, y.length);
        for (int i = 0; i < y.length; i++) {
            assertEquals(message, x[i], y[i]);
        }

    }
    public void assertArraysEquals(char[] x, char[] y){
        assertArraysEquals(null, x, y);
    }
}
