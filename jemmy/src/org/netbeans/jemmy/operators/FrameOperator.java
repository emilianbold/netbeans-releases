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
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Image;
import java.awt.Frame;
import java.awt.MenuBar;

import java.util.Hashtable;

/**
 * <BR><BR>Timeouts used: <BR>
 * FrameWaiter.WaitFrameTimeout - time to wait frame displayed <BR>
 * FrameWaiter.AfterFrameTimeout - time to sleep after frame has been dispayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class FrameOperator extends WindowOperator {
    public FrameOperator(Frame w) {
	super(w);
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
	this(waitFrame(new FrameByTitleChooser(title, 
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
	     waitFrame(new FrameSubChooser(ComponentSearcher.
					   getTrueChooser("Any Frame")),
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
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Title", ((Frame)getSource()).getTitle());
	result.put("State", 
		   (((Frame)getSource()).getState() == Frame.ICONIFIED) ?
		   "ICONIFIED" : "NORMAL");
	result.put("Resizable", new Boolean(((Frame)getSource()).isResizable()).toString());
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
	    return((Frame)waiter.waitFrame(new FrameSubChooser(chooser), index));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    private static class FrameSubChooser implements ComponentChooser {
	private ComponentChooser chooser;
	public FrameSubChooser(ComponentChooser c) {
	    super();
	    chooser = c;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Frame) {
		return(chooser.checkComponent(comp));
	    } else {
		return(false);
	    }
	}
	public String getDescription() {
	    return(chooser.getDescription());
	}
    }

    protected static class FrameByTitleChooser implements ComponentChooser {
	String title;
	StringComparator comparator;
	public FrameByTitleChooser(String t, StringComparator comparator) {
	    super();
	    title = t;
	    this.comparator = comparator;
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

