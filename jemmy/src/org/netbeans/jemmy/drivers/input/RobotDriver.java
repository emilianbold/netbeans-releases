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
    
    private boolean haveOldPos;
    private boolean smooth = false;
    private double oldX;
    private double oldY;
    private static final double CONSTANT1 = 0.75;
    private static final double CONSTANT2 = 12.0;
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
    
    public RobotDriver(Timeout autoDelay, String[] supported, boolean smooth) {
        this(autoDelay, supported);
        this.smooth = smooth;
    }

        /**
     * Constructs a RobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public RobotDriver(Timeout autoDelay) {
        this(autoDelay, new String[] {"org.netbeans.jemmy.operators.ComponentOperator"});
    }
    
    public RobotDriver(Timeout autoDelay, boolean smooth) {
        this(autoDelay);
        this.smooth = smooth;
    }
    
    public void pressMouse(int mouseButton, int modifiers) {
        pressModifiers(modifiers);
        makeAnOperation("mousePress",
                new Object[] {new Integer(mouseButton)},
                new Class[] {Integer.TYPE});
    }
    
    public void releaseMouse(int mouseButton, int modifiers) {
        makeAnOperation("mouseRelease",
                new Object[] {new Integer(mouseButton)},
                new Class[] {Integer.TYPE});
                releaseModifiers(modifiers);
    }
    
    public void moveMouse(int x, int y) {
        if(!smooth) {
            makeAnOperation("mouseMove",
                    new Object[] {new Integer(x), new Integer(y)},
                    new Class[] {Integer.TYPE, Integer.TYPE});
        } else {
            double targetX = x;
            double targetY = y;
            if (haveOldPos) {
                double currX = oldX;
                double currY = oldY;
                double vx = 0.0;
                double vy = 0.0;
                while (Math.round(currX)!=Math.round(targetX) ||
                        Math.round(currY)!=Math.round(targetY)) {
                    vx = vx*CONSTANT1+(targetX-currX)/CONSTANT2*(1.0-CONSTANT1);
                    vy = vy*CONSTANT1+(targetY-currY)/CONSTANT2*(1.0-CONSTANT1);
                    currX += vx;
                    currY += vy;
                    makeAnOperation("mouseMove", new Object[]{
                        new Integer((int)Math.round(currX)),
                                new Integer((int) Math.round(currY))},
                            new Class[]{Integer.TYPE, Integer.TYPE});
                }
            } else {
                makeAnOperation("mouseMove", new Object[]{
                    new Integer((int) Math.round(targetX)),
                            new Integer((int) Math.round(targetY))},
                        new Class[]{Integer.TYPE, Integer.TYPE});
            }
            haveOldPos = true;
            oldX = targetX;
            oldY = targetY;
        }
    }
    
    public void clickMouse(int x, int y, int clickCount, int mouseButton,
            int modifiers, Timeout mouseClick) {
        pressModifiers(modifiers);
        moveMouse(x, y);
        makeAnOperation("mousePress", new Object[] {new Integer(mouseButton)}, new Class[] {Integer.TYPE});
        for(int i = 1; i < clickCount; i++) {
            makeAnOperation("mouseRelease", new Object[] {new Integer(mouseButton)}, new Class[] {Integer.TYPE});
            makeAnOperation("mousePress", new Object[] {new Integer(mouseButton)}, new Class[] {Integer.TYPE});
        }
        mouseClick.sleep();
        makeAnOperation("mouseRelease", new Object[] {new Integer(mouseButton)}, new Class[] {Integer.TYPE});
        releaseModifiers(modifiers);
    }
    
    public void dragMouse(int x, int y, int mouseButton, int modifiers) {
        moveMouse(x, y);
    }
    
    public void dragNDrop(int start_x, int start_y, int end_x, int end_y,
            int mouseButton, int modifiers, Timeout before, Timeout after) {
        moveMouse(start_x, start_y);
        pressMouse(mouseButton, modifiers);
        before.sleep();
        moveMouse(end_x, end_y);
        after.sleep();
        releaseMouse(mouseButton, modifiers);
    }
    
    /**
     * Presses a key.
     * @param oper Operator to press a key on.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void pressKey(int keyCode, int modifiers) {
        pressModifiers(modifiers);
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
    public void releaseKey(int keyCode, int modifiers) {
        releaseModifiers(modifiers);
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
    protected void pressModifiers(int modifiers) {
        if       ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            pressKey(KeyEvent.VK_SHIFT,     modifiers & ~InputEvent.SHIFT_MASK);
        } else if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            pressKey(KeyEvent.VK_ALT_GRAPH, modifiers & ~InputEvent.ALT_GRAPH_MASK);
        } else if((modifiers & InputEvent.ALT_MASK) != 0) {
            pressKey(KeyEvent.VK_ALT,       modifiers & ~InputEvent.ALT_MASK);
        } else if((modifiers & InputEvent.META_MASK) != 0) {
            pressKey(KeyEvent.VK_META,      modifiers & ~InputEvent.META_MASK);
        } else if((modifiers & InputEvent.CTRL_MASK) != 0) {
            pressKey(KeyEvent.VK_CONTROL,   modifiers & ~InputEvent.CTRL_MASK);
        }
    }
    /*
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
     */
    /**
     * Releases modifiers keys by robot.
     * @param oper an operator for a component to release keys on.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    protected void releaseModifiers(int modifiers) {
        if       ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            releaseKey(KeyEvent.VK_SHIFT,     modifiers & ~InputEvent.SHIFT_MASK);
        } else if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            releaseKey(KeyEvent.VK_ALT_GRAPH, modifiers & ~InputEvent.ALT_GRAPH_MASK);
        } else if((modifiers & InputEvent.ALT_MASK) != 0) {
            releaseKey(KeyEvent.VK_ALT,       modifiers & ~InputEvent.ALT_MASK);
        } else if((modifiers & InputEvent.META_MASK) != 0) {
            releaseKey(KeyEvent.VK_META,      modifiers & ~InputEvent.META_MASK);
        } else if((modifiers & InputEvent.CTRL_MASK) != 0) {
            releaseKey(KeyEvent.VK_CONTROL,   modifiers & ~InputEvent.CTRL_MASK);
        }
    }
/*
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
 */
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
