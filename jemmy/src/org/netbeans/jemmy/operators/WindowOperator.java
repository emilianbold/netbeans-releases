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

package org.netbeans.jemmy.operators;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.WindowWaiter;

import java.awt.Component;
import java.awt.Window;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.ResourceBundle;

/**
 * <BR><BR>Timeouts used: <BR>
 * WindowWaiter.WaitWindowTimeout - time to wait window displayed <BR>
 * WindowWaiter.AfterWindowTimeout - time to sleep after window has been dispayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class WindowOperator extends ContainerOperator 
implements Outputable{

    TestOut output;

    /**
     * Constructor.
     */
    public WindowOperator(Window w) {
	super(w);
    }

    /**
     * Constructor.
     * Waits for the index'th displayed owner's child.
     * Uses owner's timeout and output for waiting and to init operator.
     * @param owner Operator pointing on a window owner.
     * @param index
     * @throws TimeoutExpiredException
     */
    public WindowOperator(WindowOperator owner, int index) {
	this(waitWindow(owner, 
			ComponentSearcher.getTrueChooser("Any Window"),
			index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the first displayed owner's child.
     * Uses owner's timeout and output for waiting and to init operator.
     * @param owner Operator pointing on a window owner.
     * @throws TimeoutExpiredException
     */
    public WindowOperator(WindowOperator owner) {
	this(owner, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th displayed window.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param index
     * @param timeouts 
     * @param output
     * @throws TimeoutExpiredException
     */
    public WindowOperator(int index, Operator env) {
	this(waitWindow(ComponentSearcher.getTrueChooser("Any Window"),
			index, env.getTimeouts(), env.getOutput()));
	copyEnvironment(env);
    }

    /**
     * Constructor.
     * Waits for the index'th displayed window.
     * Uses current timeouts and output values.
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * @param index
     * @throws TimeoutExpiredException
     */
    public WindowOperator(int index) {
	this(index,
	     getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the first displayed window.
     * Uses current timeouts and output values.
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * @throws TimeoutExpiredException
     */
    public WindowOperator() {
	this(0);
    }

    /**
     * Searches an index'th window.
     */
    public static Window findWindow(ComponentChooser chooser, int index) {
	return(WindowWaiter.getWindow(chooser, index));
    }

    /**
     * Searches a window.
     */
    public static Window findWindow(ComponentChooser chooser) {
	return(findWindow(chooser, 0));
    }

    /**
     * Searches an index'th window.
     * @param owner Window - owner.
     */
    public static Window findWindow(Window owner, ComponentChooser chooser, int index) {
	return(WindowWaiter.getWindow(owner, chooser, index));
    }

    /**
     * Searches a window.
     * @param owner Window - owner.
     */
    public static Window findWindow(Window owner, ComponentChooser chooser) {
	return(findWindow(owner, chooser, 0));
    }

    /**
     * Waits an index'th window.
     * @throws TimeoutExpiredException
     */
    public static Window waitWindow(ComponentChooser chooser, int index) {
	return(waitWindow(chooser, index,
			  JemmyProperties.getCurrentTimeouts(),
			  JemmyProperties.getCurrentOutput()));
    }

    /**
     * Waits a window.
     * @throws TimeoutExpiredException
     */
    public static Window waitWindow(ComponentChooser chooser) {
	return(waitWindow(chooser, 0));
    }

    /**
     * Waits an index'th window.
     * @param owner Window - owner.
     * @throws TimeoutExpiredException
     */
    public static Window waitWindow(Window owner, ComponentChooser chooser, int index) {
	return(waitWindow(owner, chooser, index,
			  JemmyProperties.getCurrentTimeouts(),
			  JemmyProperties.getCurrentOutput()));
    }

    /**
     * Waits a window.
     * @param owner Window - owner.
     * @throws TimeoutExpiredException
     */
    public static Window waitWindow(Window owner, ComponentChooser chooser) {
	return(waitWindow(owner, chooser, 0));
    }

    protected static Window waitWindow(ComponentChooser chooser, int index,
				       Timeouts timeouts, TestOut output) {
	try {
	    WindowWaiter waiter = new WindowWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return(waiter.waitWindow(chooser, index));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    protected static Window waitWindow(WindowOperator owner, ComponentChooser chooser, int index) {
	return(waitWindow((Window)owner.getSource(), 
			  chooser, index, 
			  owner.getTimeouts(), owner.getOutput()));
    }

    protected static Window waitWindow(Window owner, ComponentChooser chooser, int index,
				       Timeouts timeouts, TestOut output) {
	try {
	    WindowWaiter waiter = new WindowWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return(waiter.waitWindow(owner, chooser, index));
	} catch(InterruptedException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    return(null);
	}
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setOutput(TestOut out) {
	super.setOutput(out);
	output = out;
    }
    
    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public TestOut getOutput() {
	return(output);
    }

    public void activate() {
 	output.printLine("Activate window\n    " + getSource().toString());
 	output.printGolden("Activate window");
 	if(getFocusOwner() == null) {
 	    getEventDispatcher().invokeExistingMethod("toFront", null, null);
 	}
 	getEventDispatcher().dispatchWindowEvent(WindowEvent.WINDOW_ACTIVATED);
    }

    /**
     * Notifies the window that it is being closed.
     */    
    public void close() {
 	output.printLine("Close window\n    " + getSource().toString());
 	output.printGolden("Close window");
 	getEventDispatcher().dispatchWindowEvent(WindowEvent.WINDOW_CLOSING);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Window.addWindowListener(WindowListener)</code> through queue*/
    public void addWindowListener(final WindowListener windowListener) {
	runMapping(new MapVoidAction("addWindowListener") {
		public void map() {
		    ((Window)getSource()).addWindowListener(windowListener);
		}});}

    /**Maps <code>Window.applyResourceBundle(String)</code> through queue*/
    public void applyResourceBundle(final String string) {
	runMapping(new MapVoidAction("applyResourceBundle") {
		public void map() {
		    ((Window)getSource()).applyResourceBundle(string);
		}});}

    /**Maps <code>Window.applyResourceBundle(ResourceBundle)</code> through queue*/
    public void applyResourceBundle(final ResourceBundle resourceBundle) {
	runMapping(new MapVoidAction("applyResourceBundle") {
		public void map() {
		    ((Window)getSource()).applyResourceBundle(resourceBundle);
		}});}

    /**Maps <code>Window.dispose()</code> through queue*/
    public void dispose() {
	runMapping(new MapVoidAction("dispose") {
		public void map() {
		    ((Window)getSource()).dispose();
		}});}

    /**Maps <code>Window.getFocusOwner()</code> through queue*/
    public Component getFocusOwner() {
	return((Component)runMapping(new MapAction("getFocusOwner") {
		public Object map() {
		    return(((Window)getSource()).getFocusOwner());
		}}));}

    /**Maps <code>Window.getOwnedWindows()</code> through queue*/
    public Window[] getOwnedWindows() {
	return((Window[])runMapping(new MapAction("getOwnedWindows") {
		public Object map() {
		    return(((Window)getSource()).getOwnedWindows());
		}}));}

    /**Maps <code>Window.getOwner()</code> through queue*/
    public Window getOwner() {
	return((Window)runMapping(new MapAction("getOwner") {
		public Object map() {
		    return(((Window)getSource()).getOwner());
		}}));}

    /**Maps <code>Window.getWarningString()</code> through queue*/
    public String getWarningString() {
	return((String)runMapping(new MapAction("getWarningString") {
		public Object map() {
		    return(((Window)getSource()).getWarningString());
		}}));}

    /**Maps <code>Window.pack()</code> through queue*/
    public void pack() {
	runMapping(new MapVoidAction("pack") {
		public void map() {
		    ((Window)getSource()).pack();
		}});}

    /**Maps <code>Window.removeWindowListener(WindowListener)</code> through queue*/
    public void removeWindowListener(final WindowListener windowListener) {
	runMapping(new MapVoidAction("removeWindowListener") {
		public void map() {
		    ((Window)getSource()).removeWindowListener(windowListener);
		}});}

    /**Maps <code>Window.toBack()</code> through queue*/
    public void toBack() {
	runMapping(new MapVoidAction("toBack") {
		public void map() {
		    ((Window)getSource()).toBack();
		}});}

    /**Maps <code>Window.toFront()</code> through queue*/
    public void toFront() {
	runMapping(new MapVoidAction("toFront") {
		public void map() {
		    ((Window)getSource()).toFront();
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////
}
