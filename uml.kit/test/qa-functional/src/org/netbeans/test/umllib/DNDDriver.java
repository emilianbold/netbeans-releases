/*
 * DNDDriver.java
 *
 */

package org.netbeans.test.umllib;

import java.awt.Point;

import java.awt.Toolkit;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.input.MouseRobotDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author  Sherry
 */
public class DNDDriver {
    
    MouseRobotDriver mDriver;
    Timeout beforeDragSleep, afterDragSleep;
    
    /** Creates a new instance of DNDDriver */
    public DNDDriver() {
        mDriver = new MouseRobotDriver(new Timeout("", 10));
        beforeDragSleep = new Timeout("", 100);
        afterDragSleep = new Timeout("", 10);
    }
    
    public void dnd(ComponentOperator source, Point from, ComponentOperator target, Point to, int button, int modifiers) {
        Point theDelta = new Point(
                target.getLocationOnScreen().x - source.getLocationOnScreen().x,
                target.getLocationOnScreen().y - source.getLocationOnScreen().y);
        mDriver.moveMouse(source, from.x, from.y);
        try{Thread.sleep(2000);}catch(Exception ex){}
        mDriver.pressMouse(source, from.x, from.y, button, modifiers);
        beforeDragSleep.sleep();
        /*
        mDriver.moveMouse(source, from.x, from.y);
        mDriver.moveMouse(source, from.x + 1, from.y + 1);
         */
        mDriver.moveMouse(target, - theDelta.x, - theDelta.y);
        mDriver.moveMouse(target, - theDelta.x + 1, - theDelta.y + 1);
        mDriver.moveMouse(target, to.x + 1, to.y + 1);
        mDriver.moveMouse(target, to.x, to.y);
        afterDragSleep.sleep();
        mDriver.releaseMouse(target, to.x, to.y, button, modifiers);
    }
    
    public void dnd(ComponentOperator source, Point from, ComponentOperator target, Point to) {
        dnd(source, from, target, to, Operator.getDefaultMouseButton(), 0);
    }

    public void dnd(ComponentOperator source, ComponentOperator target) {
        dnd(source, new Point(source.getCenterXForClick(),
        source.getCenterYForClick()), 
        target, new Point(target.getCenterXForClick(),
        target.getCenterYForClick()));
    }
}
