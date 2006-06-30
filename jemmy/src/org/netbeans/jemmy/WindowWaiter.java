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

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;

/**
 * A WindowWaiter is a utility class used to look or wait for Windows.
 * It contains methods to search for a Window among the currently
 * showing Windows as well as methods that wait for a Window to show
 * within an allotted time period.
 *
 * Searches and waits always involve search criteria applied by a
 * ComponentChooser instance.  Searches and waits can both be restricted
 * to windows owned by a given window.
 *
 * <BR>Timeouts used: <BR>
 * WindowWaiter.WaitWindowTimeout - time to wait window displayed <BR>
 * WindowWaiter.AfterWindowTimeout - time to sleep after window has been dispayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class WindowWaiter extends Waiter implements Timeoutable {

    private final static long WAIT_TIME = 60000;
    private final static long AFTER_WAIT_TIME = 0;

    private ComponentChooser chooser;
    private Window owner = null;
    private int index = 0;
    private Timeouts timeouts;

    /**
     * Constructor.
     */
    public WindowWaiter() {
	super();
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
    }

    /**
     * Searches for a window.
     * The search proceeds among the currently showing windows 
     * for the <code>index+1</code>'th window that is both owned by the
     * <code>java.awt.Window</code> <code>owner</code> and that meets the
     * criteria defined and applied by the <code>ComponentChooser</code> parameter.
     * @param owner The owner window of all the windows to be searched.
     * @param cc A component chooser used to define and apply the search criteria.
     * @param index The ordinal index of the window in the set of currently displayed
     * windows with the proper window ownership and a suitable title.  The first
     * index is 0.
     * @return a reference to the <code>index+1</code>'th window that is showing,
     * has the proper window ownership, and that meets the search criteria.
     * If there are fewer than <code>index+1</code> windows, a <code>null</code>
     * reference is returned.
     */
    public static Window getWindow(Window owner, ComponentChooser cc, int index) {
	return(getAWindow(owner, new IndexChooser(cc, index)));
    }

    /**
     * Searches for a window.
     * Search among the currently showing windows for the first that is both
     * owned by the <code>java.awt.Window</code> <code>owner</code> and that
     * meets the search criteria applied by the <code>ComponentChooser</code>
     * parameter.
     * @param owner The owner window of the windows to be searched.
     * @param cc A component chooser used to define and apply the search criteria.
     * @return a reference to the first window that is showing, has a proper
     * owner window, and that meets the search criteria.  If no such window
     * can be found, a <code>null</code> reference is returned.
     */
    public static Window getWindow(Window owner, ComponentChooser cc) {
	return(getWindow(owner, cc, 0));
    }

    /**
     * Searches for a window.
     * The search proceeds among the currently showing windows for the
     * <code>index+1</code>'th window that meets the criteria defined and
     * applied by the <code>ComonentChooser</code> parameter.
     * @param cc A component chooser used to define and apply the search criteria.
     * @param index The ordinal index of the window in the set of currently displayed
     * windows.  The first index is 0.
     * @return a reference to the <code>index+1</code>'th window that is showing
     * and that meets the search criteria.  If there are fewer than
     * <code>index+1</code> windows, a <code>null</code> reference is returned.
     */
    public static Window getWindow(ComponentChooser cc, int index) {
	return(getAWindow(new IndexChooser(cc, index)));
    }

    /**
     * Searches for a window.
     * Search among the currently showing windows for one that meets the search
     * criteria applied by the <code>ComponentChooser</code> parameter.
     * @param cc A component chooser used to define and apply the search criteria.
     * @return a reference to the first window that is showing and that
     * meets the search criteria.  If no such window can be found, a
     * <code>null</code> reference is returned.
     */
    public static Window getWindow(ComponentChooser cc) {
	return(getWindow(cc, 0));
    }

    static {
	Timeouts.initDefault("WindowWaiter.WaitWindowTimeout", WAIT_TIME);
	Timeouts.initDefault("WindowWaiter.AfterWindowTimeout", AFTER_WAIT_TIME);
    }

    /**
     * Defines current timeouts.
     * @param	timeouts A collection of timeout assignments.
     * @see	org.netbeans.jemmy.Timeoutable
     * @see	org.netbeans.jemmy.Timeouts
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	Timeouts times = timeouts.cloneThis();
	times.setTimeout("Waiter.WaitingTime", 
			 timeouts.getTimeout("WindowWaiter.WaitWindowTimeout"));
	times.setTimeout("Waiter.AfterWaitingTime", 
			 timeouts.getTimeout("WindowWaiter.AfterWindowTimeout"));
	super.setTimeouts(times);
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
     * Action producer--get a window.
     * Get a window.  The search uses constraints on window ownership,
     * ordinal index, and search criteria defined by an instance of
     * <code>org.netbeans.jemmy.ComponentChooser</code>.
     * @param obj Not used.
     * @return the window waited upon.  If a window cannot be found
     * then a <code>null</code> reference is returned.
     * @see org.netbeans.jemmy.Action
     */
    public Object actionProduced(Object obj) {
	return(WindowWaiter.getWindow(owner, chooser, index));
    }

    /**
     * Waits for a window to show.
     * Wait for the <code>index+1</code>'th window that meets the criteria
     * defined and applied by the <code>ComonentChooser</code> parameter to
     * show up.
     * 
     * @param	ch A component chooser used to define and apply the search criteria.
     * @param	index The ordinal index of the window in the set of currently displayed
     * windows.  The first index is 0.
     * @return	a reference to the <code>index+1</code>'th window that shows
     * and that meets the search criteria.  If fewer than
     * <code>index+1</code> windows show up in the allotted time period then
     * a <code>null</code> reference is returned.
     * @throws	TimeoutExpiredException
     * @see	#actionProduced(Object)
     * @exception	InterruptedException
     */
    public Window waitWindow(ComponentChooser ch, int index)
	throws InterruptedException {
	chooser = ch;
	owner = null;
	this.index = index;
	return(waitWindow());
    }

    /**
     * Waits for a window to show.
     * Wait for a window that meets the search criteria applied by the
     * <code>ComponentChooser</code> parameter to show up.
     * 
     * @param	ch A component chooser used to define and apply the search criteria.
     * @return	a reference to the first window that shows and that
     * meets the search criteria.  If no such window can be found within the
     * time period allotted, a <code>null</code> reference is returned.
     * @throws	TimeoutExpiredException
     * @see	#actionProduced(Object)
     * @exception	InterruptedException
     */
    public Window waitWindow(ComponentChooser ch)
	throws InterruptedException {
	return(waitWindow(ch, 0));
    }

    /**
     * Waits for a window to show.
     * Wait for the <code>index+1</code>'th window to show that is both owned by the
     * <code>java.awt.Window</code> <code>o</code> and that meets the
     * criteria defined and applied by the <code>ComponentChooser</code> parameter.
     * 
     * @param	o The owner window of all the windows to be searched.
     * @param	ch A component chooser used to define and apply the search criteria.
     * @param	index The ordinal index of the window in the set of currently displayed
     * windows with the proper window ownership and a suitable title.  The first
     * index is 0.
     * @return	a reference to the <code>index+1</code>'th window to show that
     * has the proper window ownership, and that meets the search criteria.
     * If there are fewer than <code>index+1</code> windows, a <code>null</code>
     * reference is returned.
     * @throws	TimeoutExpiredException
     * @see	#actionProduced(Object)
     * @exception	InterruptedException
     */
    public Window waitWindow(Window o, ComponentChooser ch, int index)
	throws InterruptedException {
	owner = o;
	chooser = ch;
	this.index = index;
	return((Window)waitAction(null));
    }

    /**
     * Waits for a window to show.
     * Wait for the first window to show that is both owned by the
     * <code>java.awt.Window</code> <code>o</code> and that meets the
     * criteria defined and applied by the <code>ComponentChooser</code> parameter.
     * 
     * @param	o The owner window of all the windows to be searched.
     * @param	ch A component chooser used to define and apply the search criteria.
     * @return	a reference to the first window to show that
     * has the proper window ownership, and that meets the search criteria.
     * If there is no such window, a <code>null</code> reference is returned.
     * @throws	TimeoutExpiredException
     * @see	#actionProduced(Object)
     * @exception	InterruptedException
     */
    public Window waitWindow(Window o, ComponentChooser ch)
	throws InterruptedException {
	return(waitWindow(o, ch, 0));
    }

    public String getDescription() {
	return(chooser.getDescription());
    }

    /**
     * Method can be used by a subclass to define chooser.
     * @param	ch a chooser specifying searching criteria.
     * @see #getComponentChooser
     */
    protected void setComponentChooser(ComponentChooser ch) {
	chooser = ch;
    }

    /**
     * Method can be used by a subclass to define chooser.
     * @return a chooser specifying searching criteria.
     * @see #setComponentChooser
     */
    protected ComponentChooser getComponentChooser() {
	return(chooser);
    }

    /**
     * Method can be used by a subclass to define window owner.
     * @param	owner Window-owner of the set of windows.
     * @see #getOwner
     */
    protected void setOwner(Window owner) {
	this.owner = owner;
    }

    /**
     * Method can be used by a subclass to define window owner.
     * @return Window-owner of the set of windows.
     * @see #setOwner
     */
    protected Window getOwner() {
	return(owner);
    }

    /**
     * @see org.netbeans.jemmy.Waiter#getWaitingStartedMessage()
     */
    protected String getWaitingStartedMessage() {
	return("Start to wait window \"" + chooser.getDescription() + "\" opened");
    }

    /**
     * Overrides Waiter.getTimeoutExpiredMessage.
     * @see	org.netbeans.jemmy.Waiter#getTimeoutExpiredMessage(long)
     * @param timeSpent time from waiting start (milliseconds)
     * @return a message.
     */
    protected String getTimeoutExpiredMessage(long timeSpent) {
	return("Window \"" + chooser.getDescription() + "\" has not been opened in " +
	       (new Long(timeSpent)).toString() + " milliseconds");
    }

    /**
     * Overrides Waiter.getActionProducedMessage.
     * @see	org.netbeans.jemmy.Waiter#getActionProducedMessage(long, Object)
     * @param timeSpent time from waiting start (milliseconds)
     * @param result result of Waitable.actionproduced method.
     * @return a message.
     */
    protected String getActionProducedMessage(long timeSpent, final Object result) {
        String resultToString;
        if(result instanceof Component) {
            // run toString in dispatch thread
            resultToString = (String)new QueueTool().invokeSmoothly(
                new QueueTool.QueueAction("result.toString()") {
                    public Object launch() {
                        return result.toString();
                    }
                }
            );
        } else {
            resultToString = result.toString();
        }
	return("Window \"" + chooser.getDescription() + "\" has been opened in " +
	       (new Long(timeSpent)).toString() + " milliseconds" +
	       "\n    " + resultToString);
    }

    /**
     * @return a message.
     * @see org.netbeans.jemmy.Waiter#getGoldenWaitingStartedMessage()
     */
    protected String getGoldenWaitingStartedMessage() {
	return("Start to wait window \"" + chooser.getDescription() + "\" opened");
    }

    /**
     * @return a message.
     * @see org.netbeans.jemmy.Waiter#getGoldenTimeoutExpiredMessage()
     */
    protected String getGoldenTimeoutExpiredMessage() {
	return("Window \"" + chooser.getDescription() + "\" has not been opened");
    }

    /**
     * @return a message.
     * @see org.netbeans.jemmy.Waiter#getGoldenActionProducedMessage()
     */
    protected String getGoldenActionProducedMessage() {
	return("Window \"" + chooser.getDescription() + "\" has been opened");
    }

    private static Window getAWindow(Window owner, ComponentChooser cc) {
	if(owner == null) {
	    return(WindowWaiter.getAWindow(cc));
	} else {
	    Window result = null;
	    Window[] windows = owner.getOwnedWindows();
	    for(int i = 0; i < windows.length; i++) {
		if(cc.checkComponent(windows[i])) {
		    return(windows[i]);
		}
		if((result = WindowWaiter.getWindow(windows[i], cc)) != null) {
		    return(result);
		}
	    }
	    return(null);
	}
    }

    private static Window getAWindow(ComponentChooser cc) {
	Window result = null;
	Frame[] frames = Frame.getFrames();
	for(int i = 0; i < frames.length; i++) {
	    if(cc.checkComponent(frames[i])) {
		return(frames[i]);
	    }
	    if((result = WindowWaiter.getWindow(frames[i], cc)) != null) {
		return(result);
	    }
	}
	return(null);
    }

    private Window waitWindow()
	throws InterruptedException {
	return((Window)waitAction(null));
    }

    private Window waitWindow(Window o)
	throws InterruptedException {
	owner = o;
	return((Window)waitAction(null));
    }

    private static class IndexChooser implements ComponentChooser {
	private int curIndex = 0;
	private int index;
	private ComponentChooser chooser;
	public IndexChooser(ComponentChooser ch, int i) {
	    index = i;
	    chooser = ch;
	    curIndex = 0;
	}
	public boolean checkComponent(Component comp) {
	    if(comp.isShowing() && comp.isVisible() && chooser.checkComponent(comp)) {
		if(curIndex == index) {
		    return(true);
		}
		curIndex++;
	    }
	    return(false);
	}
	public String getDescription() {
	    return(chooser.getDescription());
	}
    }
}
