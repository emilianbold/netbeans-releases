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

import java.awt.AWTEvent;
import java.awt.Component;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.ComponentIsNotVisibleException;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.KeyDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;

public class EventDriver extends SupportiveDriver {
    QueueTool queueTool;
    public EventDriver(Class[] supported) {
	super(supported);
	queueTool = new QueueTool();
    }
    public EventDriver() {
	this(new Class[] {ComponentOperator.class});
    }
    public void dispatchEvent(Component comp, AWTEvent event) {
        queueTool.invokeSmoothly(new Dispatcher(comp, event));
    }
    protected void checkVisibility(Component component) {
	if(!component.isVisible()) {
	    throw(new ComponentIsNotVisibleException(component));
	}
    }
    protected class Dispatcher extends QueueTool.QueueAction {
	AWTEvent event;
	Component component;
	public Dispatcher(Component component, AWTEvent e) {
	    super(e.getClass().getName() + " event dispatching");
	    this.component = component;
	    event = e;
	}
	public Object launch() {
	    checkVisibility(component);
	    component.dispatchEvent(event);
	    return(null);
	}
    }
}
