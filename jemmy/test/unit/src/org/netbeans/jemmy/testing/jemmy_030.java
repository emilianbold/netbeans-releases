package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class jemmy_030 extends JemmyTest {

    JFrameOperator frmo;
    AWTEvent event;
    boolean finished;

    public int runIt(Object obj) {

	try {

	    JemmyProperties.push();

	    EventTool cleaner = new EventTool();
	    cleaner.getTimeouts().setTimeout("EventTool.WaitNoEventTimeout", 9000);

	    EventTool.addListeners(AWTEvent.CONTAINER_EVENT_MASK);

	    TestFrame frm = new TestFrame("Application_030");
	    frm.show();
	    frm.setSize(100, 100);
	    frmo = new JFrameOperator(frm);

	    event = cleaner.getLastEvent();
	    if(event != null ||
	       EventTool.getCurrentEventMask() != AWTEvent.CONTAINER_EVENT_MASK) {
		printEvent("First event found:", 0);
		if(!(event instanceof ContainerEvent)) {
		    printEvent("Wrong event catched (should be ContainerEvent):", 
			       AWTEvent.CONTAINER_EVENT_MASK);
		}
	    }

	    cleaner.getTimeouts().setTimeout("EventTool.WaitEventTimeout", 3000);
	    if((event = EventTool.getLastEvent(AWTEvent.WINDOW_EVENT_MASK)) != null ||
	       EventTool.getLastEventTime(AWTEvent.WINDOW_EVENT_MASK) > 0) {
		printEvent("Window event was somehow catched:", AWTEvent.WINDOW_EVENT_MASK);
		finalize();
		return(1);
	    }

	    EventTool.addListeners();

	    new MouseMover(1000).produceAction(null);

	    event = cleaner.waitEvent(AWTEvent.MOUSE_EVENT_MASK);
	    if(event != null) {
		printEvent("Catched:", AWTEvent.MOUSE_EVENT_MASK);
	    }

	    try {
		event = cleaner.waitEvent(AWTEvent.KEY_EVENT_MASK);
		printEvent("Key event was somehow catched:", AWTEvent.KEY_EVENT_MASK);
		finalize();
		return(1);
	    } catch(TimeoutExpiredException e) {
	    }

	    event = EventTool.getLastEvent(AWTEvent.MOUSE_EVENT_MASK);
	    if(event != null &&
	       EventTool.getLastEventTime(AWTEvent.MOUSE_EVENT_MASK) > 0) {
		printEvent("Last mouse event:", AWTEvent.MOUSE_EVENT_MASK);
	    } else {
		getOutput().printErrLine("No mouse event found");
		finalize();
		return(1);
	    }

	    new FinishedWaiter().waitAction(null);

	    EventTool.removeListeners();

	    new MouseMover(1000).produceAction(null);

	    try {
		event = cleaner.waitEvent();
		printEvent("Some event was somehow catched:", 0);
		finalize();
		return(1);
	    } catch(TimeoutExpiredException e) {
	    }

	    new FinishedWaiter().waitAction(null);

	    EventTool.addListeners();

	    new MouseMover(1000).produceAction(null);

	    event = cleaner.waitEvent();
	    if(event != null) {
		printEvent("First event catched:", 0);
	    }

	    new FinishedWaiter().waitAction(null);

	    new MouseMover(1000).produceAction(null);

	    if(!cleaner.checkNoEvent(AWTEvent.MOUSE_EVENT_MASK, 500)) {
		getOutput().printErrLine("Mouse event occured in 500 milliseconds");
		finalize();
		return(1);
	    }

	    if(cleaner.checkNoEvent(AWTEvent.MOUSE_EVENT_MASK, 1500)) {
		getOutput().printErrLine("Mouse event was not occured in 1500 milliseconds");
		finalize();
		return(1);
	    }
		
	    new FinishedWaiter().waitAction(null);

	    if(!cleaner.checkNoEvent(500)) {
		getOutput().printErrLine("Some event occured in 500 milliseconds");
		finalize();
		return(1);
	    }

	    new MouseMover(1000, 10).produceAction(null);

	    cleaner.waitNoEvent(AWTEvent.MOUSE_EVENT_MASK, 500);

	    new FinishedWaiter().waitAction(null);

	    new MouseMover(1000, 10).produceAction(null);

	    try {
		cleaner.waitNoEvent(AWTEvent.MOUSE_EVENT_MASK, 1500);
		getOutput().printErrLine("There somehow was 1500 millisecone quiet time");
		finalize();
		return(1);
	    } catch(TimeoutExpiredException e) {
	    }

	    new FinishedWaiter().waitAction(null);

	} catch(TimeoutExpiredException e) {
	    return(1);
	} catch(InterruptedException e) {
	    return(1);
	} finally {
	    finalize();
	    JemmyProperties.pop();
	}

	return(0);
    }

    void setFinished(boolean finished) {
	this.finished = finished;
    }

    void printEvent(String pre, long eMask) {
	getOutput().printLine(pre);
	getOutput().printLine(event.toString());
	getOutput().printLine("Mask     " + eMask);
    }

    class MouseMover extends ActionProducer {
	long timeToSleep;
	int count;
	public MouseMover(long timeToSleep, int count) {
	    super(false);
	    this.count = count;
	    this.timeToSleep = timeToSleep;
	}
	public MouseMover(long timeToSleep) {
	    this(timeToSleep, 1);
	}
	public Object launch(Object obj) {
	    try {
		setFinished(false);
		for(int i = 0; i < count; i++) {
		    sleep(timeToSleep);
		    frmo.enterMouse(); 
		    frmo.exitMouse(); 
		}
		setFinished(true);
	    } catch(InterruptedException e) {
		getOutput().printStackTrace(e);
	    }
	    return(null);
	}
	public String getDescription() {
	    return("Actions");
	}
    }

    class FinishedWaiter extends Waiter {
	public FinishedWaiter() {
	    super();
	    getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
	}
	public Object actionProduced(Object obj) {
	    return(finished && QueueTool.checkEmpty() ? "" : null);
	}
	public String getDescription() {
	    return("Quiet state");
	}
    }
}
