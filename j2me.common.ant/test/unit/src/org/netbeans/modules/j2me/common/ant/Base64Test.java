/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * Base64Test.java
 * JUnit based test
 *
 * Created on April 5, 2005, 1:21 PM
 */
package org.netbeans.modules.j2me.common.ant;

import java.util.Arrays;
import java.util.Random;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author misk
 */
public class Base64Test extends NbTestCase {
    
    final byte[] test0 = new byte[] { };
    final byte[] test1 = new byte[] { 0x55 };
    final byte[] test2 = new byte[] { (byte)0xff, 0x0f };
    final byte[] test3 = new byte[] { (byte)0xaa, 0x00, 0x55 };
    final byte[] test4 = new byte[] { (byte)0xff, (byte)0xf0, 0x0f, 0x5a };
    
    
    public Base64Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    static Test suite() {
        TestSuite suite = new TestSuite(Base64Test.class);
        
        return suite;
    }

    /**
     * Test of encode method, of class org.netbeans.mobility.antext.Base64.
     */
    public void testEncode() {
        
        assertEquals( "Correct pattern", "", Base64.encode( test0 ));       // NOI18N
        assertEquals( "Correct pattern", "VQ==", Base64.encode( test1 ));   // NOI18N
        assertEquals( "Correct pattern", "/w8=", Base64.encode( test2 ));   // NOI18N
        assertEquals( "Correct pattern", "qgBV", Base64.encode( test3 ));   // NOI18N
        assertEquals( "Correct pattern", "//APWg==", Base64.encode( test4 ));   // NOI18N
    }

    /**
     * Test of decode method, of class org.netbeans.mobility.antext.Base64.
     */
    public void testDecode() {
        
        assertTrue( "Correct pattern", Arrays.equals( new byte[] {}, Base64.decode( "" )));
        
        assertTrue( "Correct pattern", Arrays.equals( test0, Base64.decode( "====" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test1, Base64.decode( "VQ==" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test2, Base64.decode( "/w8=" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test3, Base64.decode( "qgBV" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test4, Base64.decode( "//APWg==" ))); // NOI18N
    }
    
    /**
     * Test encode and decode method on random generated data
     */
    public void testEncodeDecode() {
        byte[] test = new byte[1024];
        long seed = System.currentTimeMillis();
        Random random = new Random( seed );
        
        for( int i = 0; i < 1024; i++ ) {
            test[i] = (byte)random.nextInt( 256 );
        }
        String testResult = Base64.encode( test );
        assertTrue( "Correct pattern on seed " + seed, 
                Arrays.equals( test, Base64.decode( testResult ))); 
    }
}
