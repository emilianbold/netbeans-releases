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
import org.netbeans.jemmy.FrameWaiter;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.FrameDriver;
import org.netbeans.jemmy.drivers.DriverManager;

import java.awt.Component;
import java.awt.Image;
import java.awt.Frame;
import java.awt.MenuBar;

import java.util.Hashtable;

/**
 * <BR><BR>Timeouts used: <BR>
 * FrameWaiter.WaitFrameTimeout - time to wait frame displayed <BR>
 * FrameWaiter.AfterFrameTimeout - time to sleep after frame has been dispayed <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for text <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class FrameOperator extends WindowOperator implements Outputable {

    public static final String TITLE_DPROP = "Title";
    public static final String STATE_DPROP = "State";
    public static final String STATE_NORMAL_DPROP_VALUE = "NORMAL";
    public static final String STATE_ICONIFIED_DPROP_VALUE = "ICONIFIED";
    public static final String IS_RESIZABLE_DPROP = "Resizable";

    TestOut output;
    FrameDriver driver;

    public FrameOperator(Frame w) {
	super(w);
	driver = DriverManager.getFrameDriver(getClass());
    }

    public FrameOperator(ComponentChooser chooser, int index, Operator env) {
	this(waitFrame(new FrameFinder(chooser),
                       index, 
                       env.getTimeouts(),
                       env.getOutput()));
	copyEnvironment(env);
    }

    public FrameOperator(ComponentChooser chooser, int index) {
	this(chooser, index, Operator.getEnvironmentOperator());
    }

    public FrameOperator(ComponentChooser chooser) {
	this(chooser, 0);
    }

    /**
     * Constructor.
     * Waits for the frame with "title" subtitle.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param title
     * @param index
     * @param timeouts 
     * @param output
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public FrameOperator(String title, int index, Operator env) {
	this(waitFrame(new FrameByTitleFinder(title, 
						 env.getComparator()),
			index,
			env.getTimeouts(),
			env.getOutput()));
	copyEnvironment(env);
    }

    /**
     * Constructor.
     * Waits for the frame with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title
     * @param index
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public FrameOperator(String title, int index) {
	this(title, index,
	     ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the frame with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public FrameOperator(String title) {
	this(title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th frame.
     * Uses current timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public FrameOperator(int index) {
	this((Frame)
	     waitFrame(new FrameFinder(),
		       index,
		       ComponentOperator.getEnvironmentOperator().getTimeouts(),
		       ComponentOperator.getEnvironmentOperator().getOutput()));
	copyEnvironment(ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the first frame.
     * Uses current timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public FrameOperator() {
	this(0);
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

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (FrameDriver)DriverManager.
	    getDriver(DriverManager.FRAME_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Waits for title. Uses getComparator() comparator.
     * @param title Title to wait for.
     */
    public void waitTitle(final String title) {
	getOutput().printLine("Wait \"" + title + "\" title of frame \n    : "+
			      getSource().toString());
	getOutput().printGolden("Wait \"" + title + "\" title");
	waitState(new FrameByTitleFinder(title, getComparator()));
    }

    public void iconify() {
 	output.printLine("Iconifying frame\n    " + getSource().toString());
 	output.printGolden("Iconifying frame");
	driver.iconify(this);
	if(getVerification()) {
            waitState(Frame.ICONIFIED);
        }
    }
    public void deiconify() {
 	output.printLine("Deiconifying frame\n    " + getSource().toString());
 	output.printGolden("Deiconifying frame");
	driver.deiconify(this);
	if(getVerification()) {
            waitState(Frame.NORMAL);
        }
    }
    public void maximize() {
 	output.printLine("Maximizing frame\n    " + getSource().toString());
 	output.printGolden("Maximizing frame");
	driver.maximize(this);
	if(getVerification()) {
            waitState(Frame.NORMAL);
        }
    }
    public void demaximize() {
 	output.printLine("Demaximizing frame\n    " + getSource().toString());
 	output.printGolden("Demaximizing frame");
	driver.demaximize(this);
	if(getVerification()) {
            waitState(Frame.NORMAL);
        }
    }

    public void waitState(final int state) {
	getOutput().printLine("Wait frame to have " +
                              Integer.toString(state) +
			      " state \n    : "+
			      getSource().toString());
	getOutput().printGolden("Wait frame to have " +
                                Integer.toString(state) +
                                " state");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
                    return(((Frame)comp).getState() == state);
		}
		public String getDescription() {
		    return(Integer.toString(state) + " state");
		}
	    });
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(TITLE_DPROP, ((Frame)getSource()).getTitle());
	result.put(STATE_DPROP, 
		   (((Frame)getSource()).getState() == Frame.ICONIFIED) ?
		   STATE_ICONIFIED_DPROP_VALUE : STATE_NORMAL_DPROP_VALUE);
	result.put(IS_RESIZABLE_DPROP, ((Frame)getSource()).isResizable() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Frame.getIconImage()</code> through queue*/
    public Image getIconImage() {
	return((Image)runMapping(new MapAction("getIconImage") {
		public Object map() {
		    return(((Frame)getSource()).getIconImage());
		}}));}

    /**Maps <code>Frame.getMenuBar()</code> through queue*/
    public MenuBar getMenuBar() {
	return((MenuBar)runMapping(new MapAction("getMenuBar") {
		public Object map() {
		    return(((Frame)getSource()).getMenuBar());
		}}));}

    /**Maps <code>Frame.getState()</code> through queue*/
    public int getState() {
	return(runMapping(new MapIntegerAction("getState") {
		public int map() {
		    return(((Frame)getSource()).getState());
		}}));}

    /**Maps <code>Frame.getTitle()</code> through queue*/
    public String getTitle() {
	return((String)runMapping(new MapAction("getTitle") {
		public Object map() {
		    return(((Frame)getSource()).getTitle());
		}}));}

    /**Maps <code>Frame.isResizable()</code> through queue*/
    public boolean isResizable() {
	return(runMapping(new MapBooleanAction("isResizable") {
		public boolean map() {
		    return(((Frame)getSource()).isResizable());
		}}));}

    /**Maps <code>Frame.setIconImage(Image)</code> through queue*/
    public void setIconImage(final Image image) {
	runMapping(new MapVoidAction("setIconImage") {
		public void map() {
		    ((Frame)getSource()).setIconImage(image);
		}});}

    /**Maps <code>Frame.setMenuBar(MenuBar)</code> through queue*/
    public void setMenuBar(final MenuBar menuBar) {
	runMapping(new MapVoidAction("setMenuBar") {
		public void map() {
		    ((Frame)getSource()).setMenuBar(menuBar);
		}});}

    /**Maps <code>Frame.setResizable(boolean)</code> through queue*/
    public void setResizable(final boolean b) {
	runMapping(new MapVoidAction("setResizable") {
		public void map() {
		    ((Frame)getSource()).setResizable(b);
		}});}

    /**Maps <code>Frame.setState(int)</code> through queue*/
    public void setState(final int i) {
	runMapping(new MapVoidAction("setState") {
		public void map() {
		    ((Frame)getSource()).setState(i);
		}});}

    /**Maps <code>Frame.setTitle(String)</code> through queue*/
    public void setTitle(final String string) {
	runMapping(new MapVoidAction("setTitle") {
		public void map() {
		    ((Frame)getSource()).setTitle(string);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    protected static Frame waitFrame(ComponentChooser chooser, int index,
				       Timeouts timeouts, TestOut output) {
	try {
	    FrameWaiter waiter = new FrameWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((Frame)waiter.waitFrame(new FrameFinder(chooser), index));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    public static class FrameFinder extends Finder {
	public FrameFinder(ComponentChooser sf) {
            super(Frame.class, sf);
	}
	public FrameFinder() {
            super(Frame.class);
	}
    }

    public static class FrameByTitleFinder implements ComponentChooser {
	String title;
	StringComparator comparator;
	public FrameByTitleFinder(String t, StringComparator comparator) {
	    title = t;
	    this.comparator = comparator;
	}
	public FrameByTitleFinder(String t) {
            this(t, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Frame) {
		if(((Frame)comp).isShowing() && ((Frame)comp).getTitle() != null) {
		    return(comparator.equals(((Frame)comp).getTitle(), title));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Frame with title \"" + title + "\"");
	}
    }
}

