/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.actions;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.JButton;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Test general aspects of system actions.
 * Currently, just the icon.
 * @author Jesse Glick
 */
public class AsynchronousTest extends NbTestCase {
    
    public AsynchronousTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        System.setProperty("org.openide.util.Lookup", "org.openide.util.actions.AsynchronousTest$Lkp");
        assertNotNull("ErrManager has to be in lookup", Lookup.getDefault().lookup(ErrManager.class));
        ErrManager.messages.delete(0, ErrManager.messages.length());
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testExecutionOfActionsThatDoesNotOverrideAsynchronousIsAsynchronousButWarningIsPrinted() throws Exception {
        DoesNotOverride action = (DoesNotOverride)DoesNotOverride.get(DoesNotOverride.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Not yet finished", action.finished);
            action.wait();
            assertTrue("The asynchronous action is finished", action.finished);
        }
        
        if (ErrManager.messages.toString().indexOf(DoesNotOverride.class.getName() + " should override") < 0) {
            fail("There should be warning about not overriding asynchronous: " + ErrManager.messages);
        }
    }
    
    public void testExecutionCanBeAsynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Not yet finished", action.finished);
            action.wait();
            assertTrue("The asynchronous action is finished", action.finished);
        }
        
        if (ErrManager.messages.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + ErrManager.messages);
        }
    }
    
    public void testExecutionCanBeSynchronous() throws Exception {
        DoesOverrideAndReturnsFalse action = (DoesOverrideAndReturnsFalse)DoesOverrideAndReturnsFalse.get(DoesOverrideAndReturnsFalse.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", action.finished);
        }
        
        if (ErrManager.messages.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + ErrManager.messages);
        }
    }
    
    public void testExecutionCanBeForcedToBeSynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            assertTrue("When asked for synchronous the action is finished immediatelly", action.finished);
        }
        
        if (ErrManager.messages.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + ErrManager.messages);
        }
    }
    
    public static class DoesNotOverride extends CallableSystemAction {
        boolean finished;
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        public String getName() {
            return "Should warn action";
        }
        
        public synchronized void performAction() {
            notifyAll();
            finished = true;
        }
        
    }
    
    public static class DoesOverrideAndReturnsTrue extends DoesNotOverride {
        public boolean asynchronous() {
            return true;
        }
    }
    
    public static final class DoesOverrideAndReturnsFalse extends DoesOverrideAndReturnsTrue {
        public boolean asynchronous() {
            return false;
        }
    }
    
    
    public static final class Lkp extends AbstractLookup {
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new ErrManager());
        }
    }
    
    private static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer();
        
        public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations(Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance(String name) {
            return this;
        }
        
        public void log(int severity, String s) {
            messages.append(s);
            messages.append('\n');
        }
        
        public void notify(int severity, Throwable t) {
        }
        
    }
}
