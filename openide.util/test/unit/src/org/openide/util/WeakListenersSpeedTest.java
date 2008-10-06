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

package org.openide.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

@RandomlyFails // NB-Core-Build #1279
public class WeakListenersSpeedTest extends NbTestCase
implements java.lang.reflect.InvocationHandler {
    private static java.util.HashMap times = new java.util.HashMap ();
    private long time;

    private static final int COUNT = 100000;

    public WeakListenersSpeedTest (java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp () throws Exception {
        for (int i = 0; i < 10; i++) {
            try {
                super.runTest ();
            } catch (Throwable t) {
            }
        }
        
        time = System.currentTimeMillis ();
    }
    
    protected void tearDown () throws Exception {
        long now = System.currentTimeMillis ();
        times.put (getName (), new Long (now - time));
        
        assertNumbersAreSane ();
    }
    
    public void testThisIsTheBasicBenchmark () throws Exception {
        // RequestProcessor is a class with string argument that does
        // nearly nothing in its constructor, so it should be good reference
        // point
        Constructor c = org.openide.util.RequestProcessor.class.getConstructor (new Class[] { String.class });
        String  orig = "Ahoj";
        for (int i = 0; i < COUNT; i++) {
            // this is slow
            //java.lang.reflect.Proxy.newProxyInstance (getClass().getClassLoader(), new Class[] { Runnable.class }, this);
            c.newInstance (new Object[] { orig });
        }
    }

    
    public void testCreateListeners () {
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener () {
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
            }
        };
        
        for (int i = 0; i < COUNT; i++) {
            org.openide.util.WeakListeners.create (java.beans.PropertyChangeListener.class, l, this);
        }
    }
    public void testMoreTypesVariousListeners () {
        class X implements java.beans.PropertyChangeListener, java.beans.VetoableChangeListener {
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
            }
            public void vetoableChange (java.beans.PropertyChangeEvent ev) {
            }
        };
        
        X x = new X ();
        for (int i = 0; i < COUNT / 2; i++) {
            org.openide.util.WeakListeners.create (java.beans.PropertyChangeListener.class, x, this);
            org.openide.util.WeakListeners.create (java.beans.VetoableChangeListener.class, x, this);
        }
    }
    
    
    /** Compares that the numbers are in sane bounds */
    private void assertNumbersAreSane () {
        StringBuffer error = new StringBuffer ();
        {
            java.util.Iterator it = times.entrySet ().iterator ();
            while (it.hasNext ()) {
                java.util.Map.Entry en = (java.util.Map.Entry)it.next ();
                error.append ("Test "); error.append (en.getKey ());
                error.append (" took "); error.append (en.getValue ());
                error.append (" ms\n");
            }
        }
        
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        
        {
            java.util.Iterator it = times.values ().iterator ();
            while (it.hasNext ()) {
                Long l = (Long)it.next ();
                if (l.longValue () > max) max = l.longValue ();
                if (l.longValue () < min) min = l.longValue ();
            }
        }
        
        if (min * 5 < max) {
            fail ("Too big differences when various number of shadows is used:\n" + error.toString ());
        }
        
        System.err.println(error.toString ());
    }
    
    public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
        throw new Throwable ();
    }
    
}
