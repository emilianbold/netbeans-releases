/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.drivers.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ScrollPaneOperator;

public class RobotDriver extends SupportiveDriver {

    protected ClassReference robotReference = null;
    protected QueueTool qtool;

    public RobotDriver(Timeout autoDelay, Class[] supported) {
	super(supported);
	qtool = new QueueTool();
	qtool.setOutput(TestOut.getNullOutput());
	try {
	    ClassReference robotClassReverence = new ClassReference("java.awt.Robot");
	    robotReference = new ClassReference(robotClassReverence.newInstance(null, null));
	    robotReference.invokeMethod("setAutoDelay", 
					new Object[] {new Integer((int)((autoDelay != null) ?
									autoDelay.getValue() :
									0))}, 
					new Class[] {Integer.TYPE});
	} catch(InvocationTargetException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(IllegalStateException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(NoSuchMethodException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(IllegalAccessException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(ClassNotFoundException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(InstantiationException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	}
    }

    public RobotDriver(Timeout autoDelay) {
	this(autoDelay, new Class[] {ComponentOperator.class});
    }

    /**
     * Press key.
     * @param keyCode Key code (KeyEvent.VK_* value)
     * @param modifiers Modifiers (combination of InputEvent.*_MASK fields)
     */
    public void pressKey(ComponentOperator oper, int keyCode, int modifiers) {
	pressModifiers(oper, modifiers);
	makeAnOperation("keyPress", 
			new Object[] {new Integer(keyCode)}, 
			new Class[] {Integer.TYPE});
    }

    /**
     * Releases key.
     * @param keyCode Key code (KeyEvent.VK_* value)
     * @param modifiers Modifiers (combination of InputEvent.*_MASK fields)
     */
    public void releaseKey(ComponentOperator oper, int keyCode, int modifiers) {
	releaseModifiers(oper, modifiers);
	makeAnOperation("keyRelease", 
			new Object[] {new Integer(keyCode)}, 
			new Class[] {Integer.TYPE});
    }

    protected void makeAnOperation(String method, Object[] params, Class[] paramClasses) {
	try {
	    robotReference.invokeMethod(method, params, paramClasses);
	} catch(InvocationTargetException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(IllegalStateException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(NoSuchMethodException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(IllegalAccessException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	}
        if(!qtool.isDispatchThread()) {
            try {
                robotReference.invokeMethod("waitForIdle", null, null);
            } catch(InvocationTargetException e) {
                throw(new JemmyException("Exception during java.awt.Robot accessing", e));
            } catch(IllegalStateException e) {
                throw(new JemmyException("Exception during java.awt.Robot accessing", e));
            } catch(NoSuchMethodException e) {
                throw(new JemmyException("Exception during java.awt.Robot accessing", e));
            } catch(IllegalAccessException e) {
                throw(new JemmyException("Exception during java.awt.Robot accessing", e));
            }
            if ((JemmyProperties.getCurrentDispatchingModel() & JemmyProperties.QUEUE_MODEL_MASK) != 0) {
                qtool.waitEmpty();
            }
        }
    }
    protected void pressModifiers(ComponentOperator oper, int modifiers) {
	if       ((modifiers & InputEvent.SHIFT_MASK) != 0) {
	    pressKey(oper, KeyEvent.VK_SHIFT,     modifiers & ~InputEvent.SHIFT_MASK);
	} else if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
	    pressKey(oper, KeyEvent.VK_ALT_GRAPH, modifiers & ~InputEvent.ALT_GRAPH_MASK);
	} else if((modifiers & InputEvent.ALT_MASK) != 0) {
	    pressKey(oper, KeyEvent.VK_ALT,       modifiers & ~InputEvent.ALT_MASK);
	} else if((modifiers & InputEvent.META_MASK) != 0) {
	    pressKey(oper, KeyEvent.VK_META,      modifiers & ~InputEvent.META_MASK);
	} else if((modifiers & InputEvent.CTRL_MASK) != 0) {
	    pressKey(oper, KeyEvent.VK_CONTROL,   modifiers & ~InputEvent.CTRL_MASK);
	}
    }
    protected void releaseModifiers(ComponentOperator oper, int modifiers) {
	if       ((modifiers & InputEvent.SHIFT_MASK) != 0) {
	    releaseKey(oper, KeyEvent.VK_SHIFT,     modifiers & ~InputEvent.SHIFT_MASK);
	} else if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
	    releaseKey(oper, KeyEvent.VK_ALT_GRAPH, modifiers & ~InputEvent.ALT_GRAPH_MASK);
	} else if((modifiers & InputEvent.ALT_MASK) != 0) {
	    releaseKey(oper, KeyEvent.VK_ALT,       modifiers & ~InputEvent.ALT_MASK);
	} else if((modifiers & InputEvent.META_MASK) != 0) {
	    releaseKey(oper, KeyEvent.VK_META,      modifiers & ~InputEvent.META_MASK);
	} else if((modifiers & InputEvent.CTRL_MASK) != 0) {
	    releaseKey(oper, KeyEvent.VK_CONTROL,   modifiers & ~InputEvent.CTRL_MASK);
	}
    }
}
