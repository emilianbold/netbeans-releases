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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class AlwaysEnabledActionTest extends NbTestCase implements PropertyChangeListener {
    private FileObject folder;
    private int changeCounter;
    
    public AlwaysEnabledActionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        folder = fs.findResource("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);

        myIconResourceCounter = 0;
        myListenerCalled = 0;
        myListenerCounter = 0;
        MyAction.last = null;
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testIconIsCorrect() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testIconIsCorrect.instance");
        
        assertNotNull("Action created", a);
        assertEquals("No myListener called", 0, myListenerCounter);
        assertEquals("No myIconURL called", 0, myIconResourceCounter);
        
        Object name = a.getValue(a.NAME);
        Object mnem = a.getValue(a.MNEMONIC_KEY);
        Object smallIcon = a.getValue(a.SMALL_ICON);
        //Object icon = a.getValue(a.ICON)
            
        assertEquals("Right localized name", "Icon &Name Action", name);
        assertEquals("Mnemonic is N", Character.valueOf('N'), mnem);
        assertNotNull("small icon present", smallIcon);

        assertEquals("once icon called", 1, myIconResourceCounter);

        
        Object base = a.getValue("iconBase"); 
        assertEquals("iconBase attribute is delegated", 2, myIconResourceCounter);
     
        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());

        a.actionPerformed(new ActionEvent(this, 0, "kuk"));

        assertEquals("Listener invoked", 1, myListenerCounter);
        
        
        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));

        assertContextAware(a);
    }

    private void assertContextAware(Action a) {
        assertTrue("We want context aware actions", a instanceof ContextAwareAction);
    }


    public void testDelegatesToPreviousInstanceWhenCreated() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testDelegate.instance");

        assertNotNull("Action created", a);
        assertEquals("No myListener called", 0, myListenerCounter);
        assertEquals("No myIconURL called", 0, myIconResourceCounter);

        Object name = a.getValue(a.NAME);
        Object mnem = a.getValue(a.MNEMONIC_KEY);
        Object smallIcon = a.getValue(a.SMALL_ICON);
        //Object icon = a.getValue(a.ICON)

        assertEquals("Right localized name", "Icon &Name Action", name);
        assertEquals("Mnemonic is N", Character.valueOf('N'), mnem);
        assertNotNull("small icon present", smallIcon);

        assertEquals("once icon called", 1, myIconResourceCounter);


        Object base = a.getValue("iconBase");
        assertEquals("iconBase attribute is delegated", 2, myIconResourceCounter);

        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());

        assertNull("No real action created yet", MyAction.last);
        a.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action not invoked as it is disabled", 0, myListenerCalled);
        assertNotNull("real action created", MyAction.last);
        a.addPropertyChangeListener(this);
        assertFalse("Disabled", a.isEnabled());
        MyAction.last.setEnabled(true);
        assertEquals("Change in a property delivered", 1, changeCounter);
        assertTrue("enabled now", a.isEnabled());
        a.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action invoked as no longer disabled", 1, myListenerCalled);

        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));

        assertEquals("Right localized name", "Icon &Name Action", a.getValue(Action.NAME));
        MyAction.last.putValue(MyAction.NAME, "Ahoj");
        assertEquals("Next Change in a property delivered", 2, changeCounter);
        assertEquals("Value taken from delegate", "Ahoj", a.getValue(Action.NAME));


        assertContextAware(a);
    }

    public void testContextAwareDelegate() throws Exception {
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction("testContextDelegate.instance");

        assertNotNull("Action created", a);
        assertEquals("No myListener called", 0, myListenerCounter);
        assertEquals("No myIconURL called", 0, myIconResourceCounter);

        Object name = a.getValue(a.NAME);
        Object mnem = a.getValue(a.MNEMONIC_KEY);
        Object smallIcon = a.getValue(a.SMALL_ICON);
        //Object icon = a.getValue(a.ICON)

        assertEquals("Right localized name", "Icon &Name Action", name);
        assertEquals("Mnemonic is N", Character.valueOf('N'), mnem);
        assertNotNull("small icon present", smallIcon);

        assertEquals("once icon called", 1, myIconResourceCounter);


        Object base = a.getValue("iconBase");
        assertEquals("iconBase attribute is delegated", 2, myIconResourceCounter);

        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());

        assertNull("No real action created yet", MyAction.last);

        InstanceContent ic = new InstanceContent();
        Lookup lkp = new AbstractLookup(ic);
        if (a instanceof ContextAwareAction) {
            a = ((ContextAwareAction)a).createContextAwareInstance(lkp);
        } else {
            fail("Should be context sensitive: " + a);
        }
        assertEquals("No clone created yet", 0, MyContextAction.clones);

        a.actionPerformed(new ActionEvent(this, 0, "kuk"));

        assertEquals("Clone created", 1, MyContextAction.clones);
        assertSame("Lookup used", lkp, MyContextAction.lkp);

        assertEquals("Action not invoked as it is disabled", 0, myListenerCalled);
        assertNotNull("real action created", MyAction.last);
        a.addPropertyChangeListener(this);
        assertFalse("Disabled", a.isEnabled());
        MyAction.last.setEnabled(true);
        assertEquals("Change in a property delivered", 1, changeCounter);
        assertTrue("enabled now", a.isEnabled());
        a.actionPerformed(new ActionEvent(this, 0, "kuk"));
        assertEquals("Action invoked as no longer disabled", 1, myListenerCalled);

        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));

        assertEquals("Right localized name", "Icon &Name Action", a.getValue(Action.NAME));
        MyAction.last.putValue(MyAction.NAME, "Ahoj");
        assertEquals("Next Change in a property delivered", 2, changeCounter);
        assertEquals("Value taken from delegate", "Ahoj", a.getValue(Action.NAME));

    }
    
    private static int myListenerCounter;
    private static int myListenerCalled;
    private static ActionListener myListener() {
        myListenerCounter++;
        return new MyListener();
    }
    private static ActionListener myAction() {
        myListenerCounter++;
        return new MyAction();
    }
    private static ActionListener myContextAction() {
        myListenerCounter++;
        return new MyContextAction();
    }
    private static int myIconResourceCounter;
    private static String myIconResource() {
        myIconResourceCounter++;
        return "/org/openide/awt/TestIcon.png";
    }
    
    
    private Action readAction(String fileName) throws Exception {
        FileObject fo = this.folder.getFileObject(fileName);
        assertNotNull("file " + fileName, fo);
        
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        
        if (!(obj instanceof Action)) {
            fail("Object needs to be action: " + obj);
        }
        
        return (Action)obj;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        changeCounter++;
    }

    private static class MyListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            myListenerCalled++;
        }
    }
    private static class MyAction extends AbstractAction {
        static MyAction last;

        MyAction() {
            last = this;
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            myListenerCalled++;
        }
    }
    private static class MyContextAction extends MyAction
    implements ContextAwareAction {
        static int clones;
        static Lookup lkp;

        public Action createContextAwareInstance(Lookup actionContext) {
            clones++;
            lkp = actionContext;
            return new MyContextAction();
        }
    }

}
