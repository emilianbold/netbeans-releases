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

import org.netbeans.jemmy.drivers.LightSupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ScrollPaneOperator;
/**
 * Superclass for all drivers using robot.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */

public class RobotDriver extends LightSupportiveDriver {

    /**
     * A reference to the robot instance.
     */
    protected ClassReference robotReference = null;

    /**
     * A QueueTool instance.
     */
    protected QueueTool qtool;

    protected Timeout autoDelay;

    /**
     * Constructs a RobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     * @param supported an array of supported class names
     */
    public RobotDriver(Timeout autoDelay, String[] supported) {
	super(supported);
	qtool = new QueueTool();
	qtool.setOutput(TestOut.getNullOutput());
        this.autoDelay = autoDelay;
    }

    /**
     * Constructs a RobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public RobotDriver(Timeout autoDelay) {
	this(autoDelay, new String[] {"org.netbeans.jemmy.operators.ComponentOperator"});
    }

    /**
     * Presses a key.
     * @param oper Operator to press a key on.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void pressKey(ComponentOperator oper, int keyCode, int modifiers) {
	pressModifiers(oper, modifiers);
	makeAnOperation("keyPress", 
			new Object[] {new Integer(keyCode)}, 
			new Class[] {Integer.TYPE});
    }

    /**
     * Releases a key.
     * @param oper Operator to release a key on.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void releaseKey(ComponentOperator oper, int keyCode, int modifiers) {
	releaseModifiers(oper, modifiers);
	makeAnOperation("keyRelease", 
			new Object[] {new Integer(keyCode)}, 
			new Class[] {Integer.TYPE});
    }

    /**
     * Performs a single operation.
     * @param method a name of <code>java.awt.Robot</code> method.
     * @param params method parameters
     * @param paramClasses method parameters classes
     */
    protected void makeAnOperation(final String method, final Object[] params, final Class[] paramClasses) {
        if(robotReference == null) {
            initRobot();
        }
        try {
            robotReference.invokeMethod(method, params, paramClasses);
            synchronizeRobot();
	} catch(InvocationTargetException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(IllegalStateException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(NoSuchMethodException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	} catch(IllegalAccessException e) {
	    throw(new JemmyException("Exception during java.awt.Robot accessing", e));
	}
    }
    /**
     * Calls <code>java.awt.Robot.waitForIdle()</code> method.
     */
    protected void synchronizeRobot() {
        if(!qtool.isDispatchThread()) {
            if ((JemmyProperties.getCurrentDispatchingModel() & JemmyProperties.QUEUE_MODEL_MASK) != 0) {
                if(robotReference == null) {
                    initRobot();
                }
                try {
                    robotReference.invokeMethod("waitForIdle", null, null);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * Presses modifiers keys by robot.
     * @param oper an operator for a component to press keys on.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
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
    /**
     * Releases modifiers keys by robot.
     * @param oper an operator for a component to release keys on.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
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

    private void initRobot() {
        // need to init Robot in dispatch thread because it hangs on Linux 
        // (see http://www.netbeans.org/issues/show_bug.cgi?id=37476)
        if(qtool.isDispatchThread()) {
            doInitRobot();
        } else {
            qtool.invokeAndWait(new Runnable() {
                public void run() {
                    doInitRobot();
                }
            });
        }
    }
    
    private void doInitRobot() {
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

}
