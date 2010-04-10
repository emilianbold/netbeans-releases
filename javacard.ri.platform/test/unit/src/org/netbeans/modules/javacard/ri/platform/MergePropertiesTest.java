/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.ri.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.junit.Test;
import org.netbeans.modules.propdos.AntStyleResolvingProperties;
import static org.junit.Assert.*;
import org.netbeans.modules.propdos.ObservableProperties;

public class MergePropertiesTest {

    @Test
    public void testGet() {
        P a = new P();
        P b = new P();
        a.setProperty ("foo", "bar");
        b.setProperty ("foo", "foo");

        b.setProperty ("aprop", "value");

        a.setProperty ("combined","one,two");
        b.setProperty ("append.combined", "three");

        a.setProperty ("prepended", "ten,eleven");
        b.setProperty ("prepend.prepended", "nine");

        b.setProperty("both", "${combined},${prepended}");

        MergeProperties m = new MergeProperties(a, b);
        assertEquals ("value", m.getProperty("aprop"));
        assertEquals ("foo", m.getProperty("foo"));
        assertEquals ("nine,ten,eleven", m.getProperty("prepended"));
        assertEquals ("one,two,three", m.getProperty("combined"));
        assertEquals("one,two,three,nine,ten,eleven", m.getProperty("both"));
    }

    @Test
    public void testMerge() {
        String a = "foo";
        String b = "bar";
        assertEquals ("foo,bar", MergeProperties.merge(a, b, ','));
        a = null;
        assertEquals ("bar", MergeProperties.merge(a, b, ','));
        assertEquals (null, MergeProperties.merge(null, null, ','));
        a = "foo";
        b = null;
        assertEquals ("foo", MergeProperties.merge(a, b, ','));
    }

    @Test
    public void testPropertyChange() {
        P a = new P();
        P b = new P();
        MergeProperties m = new MergeProperties(a, b);
        PCL pcl = new PCL();
        m.addPropertyChangeListener(pcl);
        a.setProperty("one", "oneVal");
        pcl.assertEvent("one", "oneVal");
        b.setProperty("one", "oneOtherVal");
        pcl.assertEvent("one", "oneOtherVal");
        a.setProperty("one", "nothing");
        pcl.assertNoEvent();
    }

    @Test
    public void testReadWrite() throws Exception {
        P a = new P();
        P b = new P();
        a.setProperty ("foo", "foo");
        b.setProperty ("foo", "bar");
        a.setProperty ("aprop", "value");
        b.setProperty ("combined","one,two");
        a.setProperty ("append.combined", "three");
        b.setProperty ("prepended", "ten,eleven");
        a.setProperty ("prepend.prepended", "nine");
        a.setProperty ("hello", "greetings");
        b.setProperty ("world", "world");
        b.setProperty ("helloWorld", "${hello} ${world}");

        MergeProperties m = new MergeProperties(a, b);

        File dir = new File(System.getProperty("java.io.tmpdir"));
        assertTrue ("java.io.tmpdir " + dir + " unusable", dir.exists() && dir.isDirectory());
        File outfile = new File (dir, System.currentTimeMillis() + getClass().getName() + ".properties");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
        try {
            m.store(out, getClass().getName());
        } finally {
            out.close();
        }
        InputStream in = new BufferedInputStream(new FileInputStream(outfile));
        Properties p = new Properties();
        try {
            p.load(in);
        } finally {
            in.close();
        }
        assertTrue (outfile.delete());
        for (Object key : p.keySet()) {
            String k = (String) key;
            assertFalse ("Prepend properties should not be saved", k.startsWith(MergeProperties.PREPEND_PREFIX));
            assertFalse ("Append properties should not be saved", k.startsWith(MergeProperties.APPEND_PREFIX));
            String val = p.getProperty(k);
            assertEquals (m.getProperty(k, false), val);
        }
    }

    @Test
    public void testDereferencing() {
        AntStyleResolvingProperties a = new AntStyleResolvingProperties(true);
        AntStyleResolvingProperties b = new AntStyleResolvingProperties(true);
        a.setProperty ("_hello", "greetings");
        b.setProperty ("_world", "world");
        b.setProperty ("helloWorld", "${_hello} ${_world}");
        a.setProperty ("worldHello", "${_world} ${_hello} ");
        MergeProperties m = new MergeProperties(a, b);
        assertEquals ("world greetings", m.getProperty("worldHello"));
        assertEquals ("greetings world", m.getProperty("helloWorld"));
        assertNull (m.getProperty("wuggle"));
    }

    @Test
    public void testCrossReferencedPropertiesAreResolved() {
        P a = new P();
        P b = new P();
        MergeProperties m = new MergeProperties(a, b);
        a.setProperty("h", "${h}");
        b.setProperty("e", "${e}");
        a.setProperty("l", "${l}");
        b.setProperty("o", "${o}");
        a.setProperty("hello", "${h}${e}${l}${l}${o}");
        assertEquals ("hello", m.getProperty("hello"));
    }

    private static class P extends ObservableProperties {
        private final PropertyChangeSupport supp = new PropertyChangeSupport(this);

        @Override
        public Object put (Object key, Object val) {
            Object old = super.put (key, val);
            supp.firePropertyChange(key.toString(), old, val);
            return old;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            supp.addPropertyChangeListener(pcl);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            supp.removePropertyChangeListener(pcl);
        }
    }

    private static class PCL implements PropertyChangeListener {
        private PropertyChangeEvent evt;
        PCL () {
        }

        void assertNoEvent() {
            assertNull (evt);
        }

        void assertEvent (String name, String newValue) {
            PropertyChangeEvent evt = this.evt;
            this.evt = null;
            assertNotNull (name + " was not fired", evt);
            assertEquals (name, evt.getPropertyName());
            assertEquals (newValue, evt.getNewValue());
        }

        public void propertyChange(PropertyChangeEvent evt) {
            this.evt = evt;
        }
    }
}