/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import javax.swing.SwingUtilities;

/** 
 * Provides low level functions for reproducing user actions.
 * One dispatch model uses the managed component's event queue to dispatch
 * events.  The other dispatch model uses <code>java.awt.Robot</code> to
 * generate native events.  It is an option in the Robot dispatch model
 * to wait for the managed component's event queue to empty before dispatching
 * events.
 *
 * Timeouts used: <BR>
 * EventDispatcher.WaitQueueEmptyTimeout - to wait event queue empty. <BR>
 * EventDispatcher.RobotAutoDelay - param for java.awt.Robot.setAutoDelay method. <BR>
 * EventDispatcher.WaitComponentUnderMouseTimeout - time to wait component under mouse. <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */
public class EventDispatcher implements Outputable, Timeoutable {

    private final static long WAIT_QUEUE_EMPTY_TIMEOUT = 180000;
    private final static long ROBOT_AUTO_DELAY = 10;
    private final static long WAIT_COMPONENT_UNDER_MOUSE_TIMEOUT = 60000;

    private static Field[] keyFields;
    private static MotionListener motionListener = null;

    /**
     * Component to dispatch events to.
     */
    protected Component component;
    private TestOut output;
    private Timeouts timeouts;
    private ClassReference reference;
    private int model;
    private ClassReference robotReference = null;
    private boolean outsider = false;
    private QueueTool queueTool;

    /**
     * Constructor.
     * @param comp Component to operate with.
     */
    public EventDispatcher(Component comp) {
	super();
	component = comp;
	reference = new ClassReference(comp);
	queueTool = new QueueTool();
	setOutput(JemmyProperties.getProperties().getOutput());
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setDispatchingModel(JemmyProperties.getProperties().getDispatchingModel());
    }

    /**
     * Waits for the managed component's <code>java.awt.EventQueue</code> to empty.
     * The timeout for this wait is EventDispatcher.WaitQueueEmptyTimeout.
     * @param output Output to print exception into.
     * @param timeouts A collection of timeout assignments.
     * @throws TimeoutExpiredException
     * @see org.netbeans.jemmy.QueueTool
     */
    public static void waitQueueEmpty(TestOut output, Timeouts timeouts) {
	QueueTool qt = new QueueTool();
	qt.setTimeouts(timeouts.cloneThis());
	qt.getTimeouts().
	    setTimeout("QueueTool.WaitQueueEmptyTimeout",
		       JemmyProperties.
		       getCurrentTimeout("EventDispatcher.WaitQueueEmptyTimeout"));
	qt.setOutput(output);
	qt.waitEmpty();
    }

    /**
     * Waits for the managed component's <code>java.awt.EventQueue</code> to empty.
     * Uses default output and timeouts.  The timeout for this wait is
     * EventDispatcher.WaitQueueEmptyTimeout.
     * @see QueueTool
     * @throws TimeoutExpiredException
     */
    public static void waitQueueEmpty() {
	waitQueueEmpty(JemmyProperties.getCurrentOutput(),
		       JemmyProperties.getCurrentTimeouts());
    }

    /**
     * Waits for the managed component's <code>java.awt.EventQueue</code> to stay empty.
     * The timeout for this wait is EventDispatcher.WaitQueueEmptyTimeout.
     * @param emptyTime The time that the event queue has to stay empty to avoid
     * a TimeoutExpiredException.
     * @param output Output to print exception into
     * @param timeouts A collection of timeout assignments.
     * @throws TimeoutExpiredException
     * @see org.netbeans.jemmy.QueueTool
     */
    public static void waitQueueEmpty(long emptyTime, TestOut output, Timeouts timeouts) {
	QueueTool qt = new QueueTool();
	qt.setTimeouts(timeouts.cloneThis());
	qt.getTimeouts().
	    setTimeout("QueueTool.WaitQueueEmptyTimeout",
		       JemmyProperties.
		       getCurrentTimeout("EventDispatcher.WaitQueueEmptyTimeout"));
	qt.setOutput(output);
	qt.waitEmpty(emptyTime);
    }

    /**
     * Waits for the managed component's <code>java.awt.EventQueue</code> to stay empty.
     * Uses default output and timeouts.  The timeout for this wait is
     * EventDispatcher.WaitQueueEmptyTimeout.
     * @param emptyTime The time that the event queue has to stay empty to avoid
     * a TimeoutExpiredException.
     * @throws TimeoutExpiredException
     * @see org.netbeans.jemmy.QueueTool
     */
    public static void waitQueueEmpty(long emptyTime) {
	waitQueueEmpty(emptyTime,
		       JemmyProperties.getCurrentOutput(),
		       JemmyProperties.getCurrentTimeouts());
    }

    /**
     * Get a string representation for key modifiers.
     * Used to print trace.
     * @param modifiers Bit mask of keyboard event modifiers.
     * @return a string representation for the keyboard event modifiers.
     */
    public static String getModifiersString(int modifiers) {
	String result = "";
	if((modifiers & InputEvent.CTRL_MASK) != 0) {
	    result = result + "CTRL_MASK | ";
	}
	if((modifiers & InputEvent.META_MASK) != 0) {
	    result = result + "META_MASK | ";
	}
	if((modifiers & InputEvent.ALT_MASK) != 0) {
	    result = result + "ALT_MASK | ";
	}
	if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
	    result = result + "ALT_GRAPH_MASK | ";
	}
	if((modifiers & InputEvent.SHIFT_MASK) != 0) {
	    result = result + "SHIFT_MASK | ";
	}
	if(result.length() > 0) {
	    return(result.substring(0, result.length() - 3));
	}
	return(result);
    }

    /**
     * Returns a string representation for a keyboard event.
     * Used to print trace.
     * @param keyCode Key code (<code>KeyEvent.VK_*</code> value)
     * @return the KeyEvent field name.
     */
    public static String getKeyDescription(int keyCode) {
	for(int i = 0; i < keyFields.length; i++) {
	    try {
		if(keyFields[i].getName().startsWith("VK_") && 
		   keyFields[i].getInt(null) == keyCode) {
		    return(keyFields[i].getName());
		}
	    } catch(IllegalAccessException e) {
		JemmyProperties.getCurrentOutput().printStackTrace(e);
	    }
	}
	return("VK_UNKNOWN");
    }

    /**
     * Returns a mouse button string representation.
     * Used to print trace.
     * @param button Mouse button (<code>InputEvent.BUTTON1/2/3_MASK</code> value).
     * @return InputEvent field name.
     */
    public static String getMouseButtonDescription(int button) {
	String result;
	if       ((button & InputEvent.BUTTON1_MASK) != 0) {
	    result = "BUTTON1";
	} else if((button & InputEvent.BUTTON2_MASK) != 0) {
	    result = "BUTTON2";
	} else if((button & InputEvent.BUTTON3_MASK) != 0) {
	    result = "BUTTON3";
	} else {
	    result = "UNKNOWN_BUTTON";
	}
	return(result);
    }

    static {
	Timeouts.initDefault("EventDispatcher.WaitQueueEmptyTimeout", WAIT_QUEUE_EMPTY_TIMEOUT);
	Timeouts.initDefault("EventDispatcher.RobotAutoDelay", ROBOT_AUTO_DELAY);
	Timeouts.initDefault("EventDispatcher.WaitComponentUnderMouseTimeout", WAIT_COMPONENT_UNDER_MOUSE_TIMEOUT);
	try {
	    keyFields = Class.forName("java.awt.event.KeyEvent").getFields();
	} catch(ClassNotFoundException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	}
    }

    /**
     * Wait (or not) for the mouse to move over a Java component before pressing.
     * This option is relevant when using <code>java.awt.Robot</code> to generate
     * mouse events.  If a mouse press occurs at a position not occupied by a
     * known Java component then a <code>NoComponentUnderMouseException</code>
     * will be thrown.
     * @param yesOrNo if <code>true</code> then the test system will wait for
     * the mouse to move over a Java component before pressing.
     * therwise, mouse presses can take place anywhere on the screen.
     */
    public void checkComponentUnderMouse(boolean yesOrNo) {
	outsider = !yesOrNo;
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #getOutput
     */
    public void setOutput(TestOut out) {
	output = out;
	queueTool.setOutput(out);
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #setOutput
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Defines current timeouts.
     * @param timeouts A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	queueTool.setTimeouts(timeouts);
	queueTool.getTimeouts().
	    setTimeout("QueueTool.WaitQueueEmptyTimeout",
		       timeouts.
		       getTimeout("EventDispatcher.WaitQueueEmptyTimeout"));
	if(robotReference != null) {
	    try {
		Object[] params = {new Integer((int)timeouts.getTimeout("EventDispatcher.RobotAutoDelay"))};
		Class[] paramClasses = {Integer.TYPE};
		robotReference.invokeMethod("setAutoDelay", params, paramClasses);
	    } catch(InvocationTargetException e) {
		output.printStackTrace(e);
	    } catch(IllegalStateException e) {
		output.printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		output.printStackTrace(e);
	    } catch(IllegalAccessException e) {
		output.printStackTrace(e);
	    }
	}
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     * @see #setTimeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Defines dispatching model.
     * @param m New model value.
     * @see #getDispatchingModel()
     * @see org.netbeans.jemmy.JemmyProperties#QUEUE_MODEL_MASK
     * @see org.netbeans.jemmy.JemmyProperties#ROBOT_MODEL_MASK
     * @see org.netbeans.jemmy.JemmyProperties#getCurrentDispatchingModel()
     * @see org.netbeans.jemmy.JemmyProperties#setCurrentDispatchingModel(int)
     * @see org.netbeans.jemmy.JemmyProperties#initDispatchingModel(boolean, boolean)
     * @see org.netbeans.jemmy.JemmyProperties#initDispatchingModel()
     */
    public void setDispatchingModel(int m) {
	model = m;
	if((model & JemmyProperties.ROBOT_MODEL_MASK) != 0) {
	    createRobot();
	    try {
		Object[] params = { (model & JemmyProperties.QUEUE_MODEL_MASK) != 0 ? Boolean.TRUE : Boolean.FALSE };
		Class[] paramClasses = {Boolean.TYPE};
		robotReference.invokeMethod("setAutoWaitForIdle", params, paramClasses);
	    } catch(InvocationTargetException e) {
		output.printStackTrace(e);
	    } catch(IllegalStateException e) {
		output.printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		output.printStackTrace(e);
	    } catch(IllegalAccessException e) {
		output.printStackTrace(e);
	    }
	}
    }

    /**
     * Gets the dispatching model value.
     * @return the model value.
     * @see #setDispatchingModel(int)
     * @see org.netbeans.jemmy.JemmyProperties#QUEUE_MODEL_MASK
     * @see org.netbeans.jemmy.JemmyProperties#ROBOT_MODEL_MASK
     * @see org.netbeans.jemmy.JemmyProperties#getCurrentDispatchingModel()
     * @see org.netbeans.jemmy.JemmyProperties#setCurrentDispatchingModel(int)
     * @see org.netbeans.jemmy.JemmyProperties#initDispatchingModel(boolean, boolean)
     * @see org.netbeans.jemmy.JemmyProperties#initDispatchingModel()
     */
    public int getDispatchingModel() {
	return(model);
    }

    /**
     * Dispatches <code>AWTEvent</code> to component passed in constructor.
     * If <code>(getDispatchingModel & JemmyProperties.QUEUE_MODEL_MASK) == 0</code>
     * dispatched event directly, otherwise uses
     * <code>javax.swing.SwingUtilities.invokeAndWait(Runnable)</code><BR>
     * @param event AWTEvent instance to be dispatched.
     * @throws ComponentIsNotVisibleException
     * @throws ComponentIsNotFocusedException
     */
    public void dispatchEvent(final AWTEvent event) {
        // run in dispatch thread
        String eventToString = (String)queueTool.invokeSmoothly(
            new QueueTool.QueueAction("event.toString()") {
                public Object launch() {
                    return event.toString();
                }
            }
        );
	output.printLine("Dispatch event " + eventToString);
	output.printGolden("Dispatch event " + event.getClass().toString());
	Dispatcher disp = new Dispatcher(event);
	queueTool.invokeAndWait(disp);
    }

    /**
     * Dispatches a MouseEvent.
     * @see #dispatchEvent(AWTEvent)
     * @param id <code>MouseEvent.MOUSE_*</code> value
     * @param mods <code>InputEvent.MOUSE1/2/3_BUTTON</code> | (modiviers value)
     * @param clickCount Click count
     * @param x Horizontal click point coordinate.
     * @param y vertical click point coordinate.
     * @param popup Difines if mouse event is popup event.
     */
    public void dispatchMouseEvent(int id, int mods, int clickCount, int x, int y, 
				   boolean popup) {
	MouseEvent event = new MouseEvent(component, id, System.currentTimeMillis(), 
					  mods, x, y, clickCount, popup);
	dispatchEvent(event);
    }

    /**
     * Dispatches MouseEvent at the center of component.
     * @see #dispatchEvent(AWTEvent)
     * @param id <code>MouseEvent.MOUSE_*</code> value
     * @param mods <code>InputEvent.MOUSE1/2/3_BUTTON</code> | (modiviers value)
     * @param clickCount Click count
     * @param popup Difines if mouse event is popup event.
     */
    public void dispatchMouseEvent(int id, int mods, int clickCount, 
				   boolean popup) {
	int x = component.getWidth() /2;
	int y = component.getHeight()/2;
	dispatchMouseEvent(id, mods, clickCount, x, y, popup);
    }

    /**
     * Dispatches WindowEvent.
     * @see #dispatchEvent(AWTEvent)
     * @param id <code>WindowEvent.WINDOW_*</code> value
     */
    public void dispatchWindowEvent(int id) {
	WindowEvent event = new WindowEvent((Window)component, id);
	dispatchEvent(event);
    }

    /**
     * Dispatches KeyEvent.
     * @see #dispatchEvent(AWTEvent)
     * @param id <code>KeyEvent.KEY_PRESSED</code> or <code>KeyEvent.KEY_RELEASED</code> value.
     * @param mods Modifiers.
     * @param keyCode Key code,
     */
    public void dispatchKeyEvent(int id, int mods, int keyCode) {
	KeyEvent event = new KeyEvent(component, id, System.currentTimeMillis()
				      , mods, keyCode);
	dispatchEvent(event);
    }

    /**
     * Dispatches KeyEvent.
     * @see #dispatchEvent(AWTEvent)
     * @param id <code>KeyEvent.KEY_TYPED</code> value.
     * @param mods Modifiers.
     * @param keyCode Key code,
     * @param keyChar Char to be tiped
     */
    public void dispatchKeyEvent(int id, int mods, int keyCode, char keyChar) {
	KeyEvent event = new KeyEvent(component, id, System.currentTimeMillis(), 
				      mods, keyCode, keyChar);
	dispatchEvent(event);
    }

    /**
     * Waits until all events currently on the event queue have been processed.
     */
    public void waitForIdle() {
	makeRobotOperation("waitForIdle", null, null);
    }

    /**
     * Bind horizontal relative cursor coordinate to screen coordinate.
     * @param x Relative coordinate
     * @return Absolute coordinate
     */
    protected int getAbsoluteX(int x) {
	return((int)component.getLocationOnScreen().getX() + x);
    }

    /**
     * Bind vertical relative cursor coordinate to screen coordinate.
     * @param y Relative coordinate
     * @return Absolute coordinate
     */
    protected int getAbsoluteY(int y) {
	return((int)component.getLocationOnScreen().getY() + y);
    }

    /**
     * Delays robot.
     * @param time Time to dalay robot for.
     */
    public void delayRobot(long time) {
	Object[] params = {new Integer((int)time)};
	Class[] paramClasses = {Integer.TYPE};
	makeRobotOperation("delay", params, paramClasses);
    }

    /**
     * Moves mouse by robot.
     * @param x Component relative horizontal coordinate.
     * @param y Component relative vertical coordinate.
     * @throws ComponentIsNotVisibleException
     */
    public void robotMoveMouse(int x, int y) {
	if(motionListener == null) {
	    initMotionListener();
	}
	output.printLine("Move mouse to (" + Integer.toString(x) + "," +
			 Integer.toString(y) + ")");
	Object[] params = {new Integer(getAbsoluteX(x)), new Integer(getAbsoluteY(y))};
	Class[] paramClasses = {Integer.TYPE, Integer.TYPE};
	makeRobotOperation("mouseMove", params, paramClasses);
    }

    /**
     * Press mouse button by robot.
     * @param button Mouse button (InputEvent.MOUSE1/2/3_BUTTON value)
     * @param modifiers Modifiers
     * @throws ComponentIsNotVisibleException
     */
    public void robotPressMouse(int button, int modifiers) {
	if(!outsider) {
	    waitMouseOver();
	}
	robotPressModifiers(modifiers);
	output.printLine("Press " + getMouseButtonDescription(button) + " mouse button");
	Object[] params = {new Integer(button)};
	Class[] paramClasses = {Integer.TYPE};
	makeRobotOperation("mousePress", params, paramClasses);
    }

    /**
     * Press mouse button with 0 modifiers.
     * @param button Mouse button (<code>InputEvent.MOUSE1/2/3_BUTTON</code> value)
     * @see #robotPressMouse(int, int)
     */
    public void robotPressMouse(int button) {
	robotPressMouse(button, 0);
    }

    /**
     * Releases mouse button by robot.
     * @param button Mouse button (<code>InputEvent.MOUSE1/2/3_BUTTON</code> value)
     * @param modifiers Modifiers
     * @throws ComponentIsNotVisibleException
     */
    public void robotReleaseMouse(int button, int modifiers) {
	output.printLine("Release " + getMouseButtonDescription(button) + " mouse button");
	Object[] params = {new Integer(button)};
	Class[] paramClasses = {Integer.TYPE};
	makeRobotOperation("mouseRelease", params, paramClasses);
	robotReleaseModifiers(modifiers);
    }

    /**
     * Releases mouse button with 0 modifiers.
     * @param button Mouse button (<code>InputEvent.MOUSE1/2/3_BUTTON</code> value)
     * @see #robotReleaseMouse(int, int)
     */
    public void robotReleaseMouse(int button) {
	robotReleaseMouse(button, 0);
    }

    /**
     * Press a key using <code>java.awt.Robot</code>.
     * @param keyCode Key (<code>KeyEvent.VK_*</code> value)
     * @param modifiers Mask of KeyEvent modifiers.
     * @throws ComponentIsNotVisibleException
     * @throws ComponentIsNotFocusedException
     */
    public void robotPressKey(int keyCode, int modifiers) {
	robotPressModifiers(modifiers);
	output.printLine("Press " + getKeyDescription(keyCode) + " key");
	Object[] params = {new Integer(keyCode)};
	Class[] paramClasses = {Integer.TYPE};
	makeRobotOperation("keyPress", params, paramClasses);
    }

    /**
     * Press key with no modifiers using <code>java.awt.Robot</code>.
     * @param keyCode Key (<code>KeyEvent.VK_*</code> value)
     * @see #robotPressKey(int, int)
     */
    public void robotPressKey(int keyCode) {
	robotPressKey(keyCode, 0);
    }

    /**
     * Releases key by robot.
     * @param keyCode Key (<code>KeyEvent.VK_*</code> value)
     * @param modifiers Mask of KeyEvent modifiers.
     * @throws ComponentIsNotVisibleException
     * @throws ComponentIsNotFocusedException
     */
    public void robotReleaseKey(int keyCode, int modifiers) {
	output.printLine("Release " + getKeyDescription(keyCode) + " key");
	Object[] params = {new Integer(keyCode)};
	Class[] paramClasses = {Integer.TYPE};
	makeRobotOperation("keyRelease", params, paramClasses);
	robotReleaseModifiers(modifiers);
    }

    /**
     * Releases key with 0 modifiers.
     * @param keyCode Key (<code>KeyEvent.VK_*</code> value)
     * @see #robotPressKey(int, int)
     */
    public void robotReleaseKey(int keyCode) {
	robotReleaseKey(keyCode, 0);
    } 

    /**
     * Invokes component method through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * 
     * @param	method_name Name of a method to be invoked
     * @param	params Method params
     * @param	params_classes Method params' classes
     * @return an Object - methods result.
     * @see	org.netbeans.jemmy.ClassReference
     * @exception	IllegalAccessException
     * @exception	NoSuchMethodException
     * @exception	IllegalStateException
     * @exception	InvocationTargetException
     */
    public Object invokeMethod(String method_name, Object[] params, Class[] params_classes)
	throws InvocationTargetException, IllegalStateException, NoSuchMethodException, IllegalAccessException {
	Invoker invk = new Invoker(method_name, params, params_classes);
	try {
	    return(queueTool.invokeAndWait(invk));
	} catch(JemmyException e) {
	    if(invk.getException() != null) {
		if(invk.getException() instanceof InvocationTargetException) {
		    throw((InvocationTargetException)invk.getException());
		} else if(invk.getException() instanceof IllegalStateException) {
		    throw((IllegalStateException)invk.getException());
		} else if(invk.getException() instanceof NoSuchMethodException) {
		    throw((NoSuchMethodException)invk.getException());
		} else if(invk.getException() instanceof IllegalAccessException) {
		    throw((IllegalAccessException)invk.getException());
		}
	    }
	    throw(e);
	}
    }

    /**
     * Gets component field value through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * 
     * @param	field_name Name of a field
     * @see	#setField(String, Object)
     * @see	org.netbeans.jemmy.ClassReference
     * @return an Object - field value
     * @exception	IllegalAccessException
     * @exception	IllegalStateException
     * @exception	InvocationTargetException
     * @exception	NoSuchFieldException
     */
    public Object getField(String field_name)
	throws InvocationTargetException, IllegalStateException, NoSuchFieldException, IllegalAccessException {
	Getter gtr = new Getter(field_name);
	try {
	    return(queueTool.invokeAndWait(gtr));
	} catch(JemmyException e) {
	    if(gtr.getException() != null) {
		if(gtr.getException() instanceof InvocationTargetException) {
		    throw((InvocationTargetException)gtr.getException());
		} else if(gtr.getException() instanceof IllegalStateException) {
		    throw((IllegalStateException)gtr.getException());
		} else if(gtr.getException() instanceof NoSuchFieldException) {
		    throw((NoSuchFieldException)gtr.getException());
		} else if(gtr.getException() instanceof IllegalAccessException) {
		    throw((IllegalAccessException)gtr.getException());
		}
	    }
	    throw(e);
	}
    }

    /**
     * Sets component field value through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * 
     * @param	field_name Name of a field
     * @param	newValue New field value
     * @see	#getField(String)
     * @see	org.netbeans.jemmy.ClassReference
     * @exception	IllegalAccessException
     * @exception	IllegalStateException
     * @exception	InvocationTargetException
     * @exception	NoSuchFieldException
     */
    public void setField(String field_name, Object newValue)
	throws InvocationTargetException, IllegalStateException, NoSuchFieldException, IllegalAccessException {
	Setter str = new Setter(field_name, newValue);
	try {
	    queueTool.invokeAndWait(str);
	} catch(JemmyException e) {
	    if(str.getException() != null) {
		if(str.getException() instanceof InvocationTargetException) {
		    throw((InvocationTargetException)str.getException());
		} else if(str.getException() instanceof IllegalStateException) {
		    throw((IllegalStateException)str.getException());
		} else if(str.getException() instanceof NoSuchFieldException) {
		    throw((NoSuchFieldException)str.getException());
		} else if(str.getException() instanceof IllegalAccessException) {
		    throw((IllegalAccessException)str.getException());
		}
	    }
	    throw(e);
	}
    }

    /**
     * Invokes component method through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * and catch all exceptions.
     * @param method_name Name of a method to be invoked
     * @param params Method params
     * @param params_classes Method params' classes
     * @param out TestOut instance to print exceptions stack trace to.
     * @return an Object - method result
     * @see #invokeMethod(String, Object[], Class[])
     * @see org.netbeans.jemmy.ClassReference
     */
    public Object invokeExistingMethod(String method_name, Object[] params, Class[] params_classes, 
				       TestOut out) {
	try {
	    return(invokeMethod(method_name, params, params_classes));
	} catch(InvocationTargetException e) {
	    out.printStackTrace(e);
	} catch(IllegalStateException e) {
	    out.printStackTrace(e);
	} catch(NoSuchMethodException e) {
	    out.printStackTrace(e);
	} catch(IllegalAccessException e) {
	    out.printStackTrace(e);
	}
	return(null);
    }

    /**
     * Gets component field value through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * and catch all exceptions.
     * @param field_name Name of a field
     * @param out TestOut instance to print exceptions stack trace to.
     * @return an Object - fields value
     * @see #getField(String)
     * @see #setExistingField(String, Object, TestOut)
     * @see org.netbeans.jemmy.ClassReference
     */
    public Object getExistingField(String field_name,
				   TestOut out) {
	try {
	    return(getField(field_name));
	} catch(InvocationTargetException e) {
	    out.printStackTrace(e);
	} catch(IllegalStateException e) {
	    out.printStackTrace(e);
	} catch(NoSuchFieldException e) {
	    out.printStackTrace(e);
	} catch(IllegalAccessException e) {
	    out.printStackTrace(e);
	}
	return(null);
    }

    /**
     * Sets component field value through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * and catch all exceptions.
     * @param field_name Name of a field
     * @param newValue New field value
     * @param out TestOut instance to print exceptions stack trace to.
     * @see #setField(String, Object)
     * @see #getExistingField(String, TestOut)
     * @see org.netbeans.jemmy.ClassReference
     */
    public void setExistingField(String field_name, Object newValue,
				   TestOut out) {
	try {
	    setField(field_name, newValue);
	} catch(InvocationTargetException e) {
	    out.printStackTrace(e);
	} catch(IllegalStateException e) {
	    out.printStackTrace(e);
	} catch(NoSuchFieldException e) {
	    out.printStackTrace(e);
	} catch(IllegalAccessException e) {
	    out.printStackTrace(e);
	}
    }

    /**
     * Invokes component method through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * and catch all exceptions.
     * Exceptions are printed into TestOut object defined 
     * by setOutput(TestOut) method.
     * @param method_name Name of a method to be invoked
     * @param params Method params
     * @param params_classes Method params' classes
     * @return an Object - method result
     * @see #invokeExistingMethod(String, Object[], Class[], TestOut)
     * @see org.netbeans.jemmy.ClassReference
     */
    public Object invokeExistingMethod(String method_name, Object[] params, Class[] params_classes) {
	return(invokeExistingMethod(method_name, params, params_classes, output));
    }

    /**
     * Gets component field value through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * and catch all exceptions.
     * Exceptions are printed into TestOut object defined 
     * by setOutput(TestOut) method.
     * @param field_name Name of a field
     * @return an Object - fields value
     * @see #getExistingField(String, TestOut)
     * @see #setExistingField(String, Object)
     * @see org.netbeans.jemmy.ClassReference
     */
    public Object getExistingField(String field_name) {
	return(getExistingField(field_name, output));
    }

    /**
     * Sets component field value through <code>SwingUtilities.invokeAndWait(Runnable)</code>.
     * and catch all exceptions.
     * Exceptions are printed into TestOut object defined 
     * by setOutput(TestOut) method.
     * @param field_name Name of a field
     * @param newValue New field value
     * @see #setExistingField(String, Object, TestOut)
     * @see #getExistingField(String)
     * @see org.netbeans.jemmy.ClassReference
     */
    public void setExistingField(String field_name, Object newValue) {
	setExistingField(field_name, output);
    }

    //recursivelly releases all modifiers keys
    private void robotReleaseModifiers(int modifiers) {
	if       ((modifiers & InputEvent.SHIFT_MASK) != 0) {
	    robotReleaseKey(KeyEvent.VK_SHIFT,     modifiers - (InputEvent.SHIFT_MASK     & modifiers));
	} else if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
	    robotReleaseKey(KeyEvent.VK_ALT_GRAPH, modifiers - (InputEvent.ALT_GRAPH_MASK & modifiers));
	} else if((modifiers & InputEvent.ALT_MASK) != 0) {
	    robotReleaseKey(KeyEvent.VK_ALT,       modifiers - (InputEvent.ALT_MASK       & modifiers));
	} else if((modifiers & InputEvent.META_MASK) != 0) {
	    robotReleaseKey(KeyEvent.VK_META,      modifiers - (InputEvent.META_MASK      & modifiers));
	} else if((modifiers & InputEvent.CTRL_MASK) != 0) {
	    robotReleaseKey(KeyEvent.VK_CONTROL,   modifiers - (InputEvent.CTRL_MASK      & modifiers));
	}
    }

    //throws ComponentIsNotVisibleException if component is not visible
    private void checkVisibility() {
	if(!component.isVisible()) {
	    throw(new ComponentIsNotVisibleException(component));
	}
    }

    //throws ComponentIsNotFocusedException if component has not focus
    private void checkFocus() {
	if(!component.hasFocus()) {
	    throw(new ComponentIsNotFocusedException(component));
	}
    }

    //creates java.awt.Robot instance
    private void createRobot() {
	try {
	    ClassReference robotClassReverence = new ClassReference("java.awt.Robot");
	    robotReference = new ClassReference(robotClassReverence.newInstance(null, null));
	} catch(ClassNotFoundException e) {
	    output.printStackTrace(e);
	} catch(InstantiationException e) {
	    output.printStackTrace(e);
	} catch(InvocationTargetException e) {
	    output.printStackTrace(e);
	} catch(IllegalStateException e) {
	    output.printStackTrace(e);
	} catch(NoSuchMethodException e) {
	    output.printStackTrace(e);
	} catch(IllegalAccessException e) {
	    output.printStackTrace(e);
	}
    }

    private void waitMouseOver() {
	try {
	    Waiter wt = new Waiter(new Waitable() {
		public Object actionProduced(Object obj) {
		    if(motionListener.getComponent() != null) {
			return("");
		    } else {
			return(null);
		    }
		}
		public String getDescription() {
		    return("Mouse over component");
		}
	    });
	    wt.setTimeouts(timeouts.cloneThis());
	    wt.getTimeouts().setTimeout("Waiter.WaitingTime", 
					timeouts.getTimeout("EventDispatcher.WaitComponentUnderMouseTimeout"));
	    wt.setOutput(output.createErrorOutput());
	    wt.waitAction(component);
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	} catch(TimeoutExpiredException e) {
	    throw(new NoComponentUnderMouseException());
	}
    }

    //produce a robot operations through reflection
    private void makeRobotOperation(String method, Object[] params, Class[] paramClasses) {
	try {
	    robotReference.invokeMethod(method, params, paramClasses);
	} catch(InvocationTargetException e) {
	    output.printStackTrace(e);
	} catch(IllegalStateException e) {
	    output.printStackTrace(e);
	} catch(NoSuchMethodException e) {
	    output.printStackTrace(e);
	} catch(IllegalAccessException e) {
	    output.printStackTrace(e);
	}
	if ((model & JemmyProperties.QUEUE_MODEL_MASK) != 0) {
	    try {
		waitQueueEmpty(output.createErrorOutput(), timeouts);
	    } catch(TimeoutExpiredException e) {
		output.printStackTrace(e);
	    }
	}
    }

    //recursivelly presses all modifiers keys
    private void robotPressModifiers(int modifiers) {
	if       ((modifiers & InputEvent.SHIFT_MASK) != 0) {
	    robotPressKey(KeyEvent.VK_SHIFT,     modifiers & ~InputEvent.SHIFT_MASK);
	} else if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
	    robotPressKey(KeyEvent.VK_ALT_GRAPH, modifiers & ~InputEvent.ALT_GRAPH_MASK);
	} else if((modifiers & InputEvent.ALT_MASK) != 0) {
	    robotPressKey(KeyEvent.VK_ALT,       modifiers & ~InputEvent.ALT_MASK);
	} else if((modifiers & InputEvent.META_MASK) != 0) {
	    robotPressKey(KeyEvent.VK_META,      modifiers & ~InputEvent.META_MASK);
	} else if((modifiers & InputEvent.CTRL_MASK) != 0) {
	    robotPressKey(KeyEvent.VK_CONTROL,   modifiers & ~InputEvent.CTRL_MASK);
	}
    }

    private void initMotionListener() {
	if(motionListener == null) {
 	    motionListener = new MotionListener();
 	    Toolkit.getDefaultToolkit().addAWTEventListener(motionListener, AWTEvent.MOUSE_EVENT_MASK);
	    Object[] params = new Object[2];
	    Class[] paramClasses = {Integer.TYPE, Integer.TYPE};
	    params[0] = new Integer(getAbsoluteX(-1));
	    params[1] = new Integer(getAbsoluteX(-1));
	    makeRobotOperation("mouseMove", params, paramClasses);
	    params[0] = new Integer(getAbsoluteX(0));
	    params[1] = new Integer(getAbsoluteX(0));
	    makeRobotOperation("mouseMove", params, paramClasses);
	}
    }


    private class Dispatcher extends QueueTool.QueueAction {
	AWTEvent event;
	public Dispatcher(AWTEvent e) {
	    super(e.getClass().getName() + " event dispatching");
	    event = e;
	}
	public Object launch() {
	    if(event instanceof MouseEvent || event instanceof KeyEvent) {
		checkVisibility();
	    }
	    component.dispatchEvent(event);
	    return(null);
	}
    }

    private class Invoker extends QueueTool.QueueAction {
	protected String methodName;
	protected Object[] params;
	protected Class[] paramClasses;
	public Invoker(String mn, Object[] p, Class[] pc) {
	    super(mn + " method invocation");
	    methodName = mn;
	    params = p;
	    paramClasses = pc;
	}
	public Object launch()
	    throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
	    checkVisibility();
	    if(methodName.equals("keyPress") || methodName.equals("keyRelease")) {
		checkFocus();
	    }
	    return(reference.invokeMethod(methodName, params, paramClasses));
	}
    }

    private class Getter extends QueueTool.QueueAction {
	String fieldName;
	public Getter(String fn) {
	    super(fn + " field receiving");
	    fieldName = fn;
	}
	public Object launch()
	    throws InvocationTargetException, NoSuchFieldException, IllegalAccessException {
	    return(reference.getField(fieldName));
	}
    }

    private class Setter extends QueueTool.QueueAction {
	String fieldName;
	Object newValue;
	public Setter(String fn, Object nv) {
	    super(fn + " field changing");
	    fieldName = fn;
	    newValue = nv;
	}
	public Object launch()
	    throws InvocationTargetException, NoSuchFieldException, IllegalAccessException {
	    reference.setField(fieldName, newValue);
	    return(null);
	}
    }

    private static class MotionListener implements AWTEventListener {
	private Component mouseComponent;
	public void eventDispatched(AWTEvent event) {
	    if(event instanceof MouseEvent) {
		MouseEvent e = (MouseEvent)event;
		if       (e.getID() == MouseEvent.MOUSE_ENTERED) {
		    mouseComponent = e.getComponent();
		} else if(e.getID() == MouseEvent.MOUSE_EXITED) {
		    mouseComponent = null;
		}
	    }
	}
	public Component getComponent() {
	    return(mouseComponent);
	}
    }
}
