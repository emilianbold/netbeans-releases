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

package org.netbeans.modules.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ActionProcessorTest extends NbTestCase {

    public ActionProcessorTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @ActionRegistration(
        category="Tools",
        displayName="#AlwaysOn"
    )
    public static final class Always implements ActionListener {
        static int cnt;
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testAlwaysEnabledAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/" + Always.class.getName().replace('.', '-') + ".instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is an action", obj instanceof Action);
        Action a = (Action)obj;

        assertEquals("I am always on!", a.getValue(Action.NAME));
        a.actionPerformed(new ActionEvent(this, 300, ""));

        assertEquals("Action called", 300, Always.cnt);
    }

    @ActionRegistration(
        category="Tools",
        displayName="#Key",
        id="my-action",
        key="klic"
    )
    public static final class Callback implements ActionListener {
        static int cnt;
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testCallbackAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/my-action.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        class MyAction extends AbstractAction {
            int cnt;
            public void actionPerformed(ActionEvent e) {
                cnt += e.getID();
            }
        }
        MyAction my = new MyAction();
        ActionMap m = new ActionMap();
        m.put("klic", my);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(m);

        assertEquals("I am context", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Local Action called", 300, my.cnt);
        assertEquals("Global Action not called", 0, Callback.cnt);

        ic.remove(m);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Local Action stays", 300, my.cnt);
        assertEquals("Global Action called", 200, Callback.cnt);
    }

    @ActionRegistration(
        category="Tools",
        displayName="#OnInt",
        id="on-int"
    )
    public static final class Context implements ActionListener {
        private final int context;
        
        public Context(Integer context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            cnt += context;
        }

    }

    public void testContextAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/on-int.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 10, Context.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Global Action stays same", 10, Context.cnt);
    }

    @ActionRegistration(
        category="Tools",
        displayName="#OnInt",
        id="on-numbers"
    )
    public static final class MultiContext implements ActionListener {
        private final Collection<Number> context;

        public MultiContext(Collection<Number> context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            for (Number n : context) {
                cnt += n.intValue();
            }
        }

    }

    public void testMultiContextAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/on-numbers.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);
        ic.add(3L);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 13, MultiContext.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Adds 3", 16, MultiContext.cnt);

        ic.remove(3L);
        assertFalse("It is disabled", clone.isEnabled());
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("No change", 16, MultiContext.cnt);
    }

}