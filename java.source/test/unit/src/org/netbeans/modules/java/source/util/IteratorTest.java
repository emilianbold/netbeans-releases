/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.source.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;

/** Basic class for testing iterators.
 *
 * @author Petr Hrebejk
 */
public class IteratorTest extends TestCase {
    
    private static final int TEST_SEQS = 1000;
    private static final int TEST_SEQ_SIZE = 1011;

    public IteratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(IteratorTest.class);        
        return suite;
    }
                   
    // Test methods ------------------------------------------------------------
    
    
    public void testChainedIterables () {
        final List<List<Integer>> data = new ArrayList<List<Integer>>();
        final List<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<TEST_SEQS; i++) {
            List<Integer> l = createSequentialList(TEST_SEQ_SIZE);
            data.add (l);
            result.addAll(l);
        }
        Iterable<Integer> i = Iterators.chained(data);
        assertEquals (result,i);
        //Next try, didn't work in prev version
        i = Iterators.chained(data);
        assertEquals (result,i);
    }
    
    private static void assertEquals (List<Integer> expected, Iterable<Integer> data) {
        Iterator<Integer> it = data.iterator();
        for (Integer i : expected) {
            assertTrue (it.hasNext());
            assertEquals(i, it.next());
        }
    }
    
    
    public static List<Integer> createSequentialList( int size ) {        
        List<Integer> result = new ArrayList<Integer>( size );                
        for( int i = 0; i < size; i++ ) {
            result.add( new Integer(i) );
        }
        return result;
    }
        
}
