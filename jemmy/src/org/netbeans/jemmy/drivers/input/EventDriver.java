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

import java.awt.AWTEvent;
import java.awt.Component;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.ComponentIsNotVisibleException;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.KeyDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
/**
 * Superclass for all drivers using event dispatching.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class EventDriver extends LightSupportiveDriver {
    QueueTool queueTool;

    /**
     * Constructs an EventDriver object.
     * @param supported an array of supported class names
     */
    public EventDriver(String[] supported) {
	super(supported);
	queueTool = new QueueTool();
    }
    /**
     * Constructs an EventDriver object suporting ComponentOperator.
     */
    public EventDriver() {
	this(new String[] {"org.netbeans.jemmy.operators.ComponentOperator"});
    }

    /**
     * Dispatches an event to the component.
     * @param comp Component to dispatch events to.
     * @param event an event to dispatch.
     */
    public void dispatchEvent(Component comp, AWTEvent event) {
        checkVisibility(comp);
        QueueTool.processEvent(event);
    }

    /**
     * Checks component visibility.
     * @param component a component.
     */
    protected void checkVisibility(Component component) {
	if(!component.isVisible()) {
	    throw(new ComponentIsNotVisibleException(component));
	}
    }

    /**
     * Class used fot execution of an event through the dispatching thread.
     */
    protected class Dispatcher extends QueueTool.QueueAction {
	AWTEvent event;
	Component component;

        /**
         * Constructs an EventDriver$Dispatcher object.
         * @param component a component to dispatch event to.
         * @param e an event to dispatch.
         */
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
