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
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JButton;
import junit.textui.TestRunner;
import org.netbeans.junit.Log;
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

    private CharSequence err;
    
    public AsynchronousTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() {
        err = Log.enable("", Level.ALL);
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
        
        if (err.toString().indexOf(DoesNotOverride.class.getName() + " should override") < 0) {
            fail("There should be warning about not overriding asynchronous: " + err);
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
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeSynchronous() throws Exception {
        DoesOverrideAndReturnsFalse action = (DoesOverrideAndReturnsFalse)DoesOverrideAndReturnsFalse.get(DoesOverrideAndReturnsFalse.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeForcedToBeSynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            assertTrue("When asked for synchronous the action is finished immediatelly", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
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
}
