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

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;

import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JSplitPane;

import javax.swing.plaf.SplitPaneUI;

import javax.swing.plaf.basic.BasicSplitPaneDivider;

/**
 * <BR><BR>Timeouts used: <BR>
 * JSplitPaneOperator.ScrollClickTimeout - time for simple scroll click <BR>
 * JSplitPaneOperator.BetweenClickTimeout - time to sleep between scroll clicks <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 * Class to operate with javax.swing.JSplitPane component
 *	
 */

public class JSplitPaneOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    private final static long SCROLL_CLICK_TIMEOUT = 0;
    private final static long BETWEEN_CLICK_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;
    
    /**
     * Constructor.
     * @param b JSplitPane component.
     */
    public JSplitPaneOperator(JSplitPane b) {
	super(b);
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JSplitPaneOperator(ContainerOperator cont, int index) {
	this((JSplitPane)waitComponent(cont, 
				       new JSplitPaneFinder(ComponentSearcher.getTrueChooser("Any container")), 
				       index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public JSplitPaneOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JSplitPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JSplitPane instance or null if component was not found.
     */
    public static JSplitPane findJSplitPane(Container cont, ComponentChooser chooser, int index) {
	return((JSplitPane)findComponent(cont, new JSplitPaneFinder(chooser), index));
    }
    
    /**
     * Searches 0'th JSplitPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSplitPane instance or null if component was not found.
     */
    public static JSplitPane findJSplitPane(Container cont, ComponentChooser chooser) {
	return(findJSplitPane(cont, chooser, 0));
    }
    
    /**
     * Searches JSplitPane in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JSplitPane instance or null if component was not found.
     */
    public static JSplitPane findJSplitPane(Container cont, int index) {
	return(findJSplitPane(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JSplitPane instance"), index));
    }
    
    /**
     * Searches 0'th JSplitPane in container.
     * @param cont Container to search component in.
     * @return JSplitPane instance or null if component was not found.
     */
    public static JSplitPane findJSplitPane(Container cont) {
	return(findJSplitPane(cont, 0));
    }

    /**
     * Searches JSplitPane object which component lies on.
     * @param comp Component to find JSplitPane under.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSplitPane instance or null if component was not found.
     */
    public static JSplitPane findJSplitPaneUnder(Component comp, ComponentChooser chooser) {
	return((JSplitPane)findContainerUnder(comp, new JSplitPaneFinder(chooser)));
    }
    
    /**
     * Searches JSplitPane object which component lies on.
     * @param comp Component to find JSplitPane under.
     * @return JSplitPane instance or null if component was not found.
     */
    public static JSplitPane findJSplitPaneUnder(Component comp) {
	return(findJSplitPaneUnder(comp, new JSplitPaneFinder(ComponentSearcher.
								getTrueChooser("JSplitPane component"))));
    }
    
    /**
     * Waits JSplitPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JSplitPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSplitPane waitJSplitPane(Container cont, ComponentChooser chooser, int index) {
	return((JSplitPane)waitComponent(cont, new JSplitPaneFinder(chooser), index));
    }
    
    /**
     * Waits 0'th JSplitPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSplitPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSplitPane waitJSplitPane(Container cont, ComponentChooser chooser) {
	return(waitJSplitPane(cont, chooser, 0));
    }
    
    /**
     * Waits JSplitPane in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JSplitPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSplitPane waitJSplitPane(Container cont, int index) {
	return(waitJSplitPane(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JSplitPane instance"), index));
    }
    
    /**
     * Waits 0'th JSplitPane in container.
     * @param cont Container to search component in.
     * @return JSplitPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSplitPane waitJSplitPane(Container cont) {
	return(waitJSplitPane(cont, 0));
    }
    
    static {
	Timeouts.initDefault("JSplitPaneOperator.ScrollClickTimeout", SCROLL_CLICK_TIMEOUT);
	Timeouts.initDefault("JSplitPaneOperator.BetweenClickTimeout", BETWEEN_CLICK_TIMEOUT);
    }

    /**
     * Sets operator's timeouts.
     * @param timeouts org.netbeans.jemmy.Timeouts instance.
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	Timeouts times = timeouts;
	times.setTimeout("ComponentOperator.BeforeDragTimeout",
			 0);
	times.setTimeout("ComponentOperator.AfterDragTimeout",
			 times.getTimeout("JSplitPaneOperator.ScrollClickTimeout"));
	super.setTimeouts(times);
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Sets operator's output.
     * @param out org.netbeans.jemmy.TestOut instance.
     */
    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Searches divider inside split pane.
     */
    public BasicSplitPaneDivider findDivider() {
	ComponentSearcher cs = new ComponentSearcher((Container)getSource());
	cs.setOutput(output.createErrorOutput());
	return((BasicSplitPaneDivider)cs.findComponent(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(comp instanceof BasicSplitPaneDivider);
		}
		public String getDescription() {
		    return("");
		}
	    }));
    }

    /**
     * Changes divider location.
     * @param dividerLocation location to move divider to.
     * @see org.netbeans.jemmy.operators.JSplitPaneOperator#getMaximumDividerLocation()
     * @see org.netbeans.jemmy.operators.JSplitPaneOperator#getMinimumDividerLocation()
     */
    public void moveDivider(int dividerLocation) {
	output.printTrace("Move JSplitPane divider to " + Integer.toString(dividerLocation) +
			  " location. JSplitPane :    \n" + getSource().toString());
	output.printGolden("Move JSplitPane divider to " + Integer.toString(dividerLocation) +
			  " location");
	moveDividerTo(dividerLocation);
    }

    /**
     * Changes divider location.
     * @param proportionalLocation Proportional location. 
     * Should be great then 0 and less then 1.
     */
    public void moveDivider(double proportionalLocation) {
	output.printTrace("Move JSplitPane divider to " + Double.toString(proportionalLocation) +
			  " proportional location. JSplitPane :    \n" + getSource().toString());
	output.printGolden("Move JSplitPane divider to " + Double.toString(proportionalLocation) +
			  " proportional location");
	moveDividerTo(getMinimumDividerLocation() + 
		      (int)(proportionalLocation * 
			    (getMaximumDividerLocation() - getMinimumDividerLocation())));
    }

    /**
     * Pushes one time right(bottom) expand button.
     * @throws TimeoutExpiredException
     */
    public void expandRight() {
	String mess = "Expand ";
	if(getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    mess = mess + "right";
	} else {
	    mess = mess + "bottom";
	}
	output.printTrace(mess + " JSplitPane side. JSplitPane :    \n" + getSource().toString());
	output.printGolden(mess + " JSplitPane side.");
	expandTo(0);
    }

    /**
     * Pushes one time left(top) expand button.
     * @throws TimeoutExpiredException
     */
    public void expandLeft() {
	String mess = "Expand ";
	if(getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    mess = mess + "left";
	} else {
	    mess = mess + "top";
	}
	output.printTrace(mess + " JSplitPane side. JSplitPane :    \n" + getSource().toString());
	output.printGolden(mess + " JSplitPane side.");
	expandTo(1);
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Minimum", Integer.toString(((JSplitPane)getSource()).getMinimumDividerLocation()));
	result.put("Maximum", Integer.toString(((JSplitPane)getSource()).getMaximumDividerLocation()));
	result.put("Orientation", (((JSplitPane)getSource()).getOrientation() == JSplitPane.HORIZONTAL_SPLIT) ? 
		   "HORIZONTAL" : 
		   "VERTICAL");
	result.put("Value", Integer.toString(((JSplitPane)getSource()).getDividerLocation()));
	result.put("One touch expandable", new Boolean(((JSplitPane)getSource()).isOneTouchExpandable()).toString());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JSplitPane.getBottomComponent()</code> through queue*/
    public Component getBottomComponent() {
	return((Component)runMapping(new MapAction("getBottomComponent") {
		public Object map() {
		    return(((JSplitPane)getSource()).getBottomComponent());
		}}));}

    /**Maps <code>JSplitPane.getDividerLocation()</code> through queue*/
    public int getDividerLocation() {
	return(runMapping(new MapIntegerAction("getDividerLocation") {
		public int map() {
		    return(((JSplitPane)getSource()).getDividerLocation());
		}}));}

    /**Maps <code>JSplitPane.getDividerSize()</code> through queue*/
    public int getDividerSize() {
	return(runMapping(new MapIntegerAction("getDividerSize") {
		public int map() {
		    return(((JSplitPane)getSource()).getDividerSize());
		}}));}

    /**Maps <code>JSplitPane.getLastDividerLocation()</code> through queue*/
    public int getLastDividerLocation() {
	return(runMapping(new MapIntegerAction("getLastDividerLocation") {
		public int map() {
		    return(((JSplitPane)getSource()).getLastDividerLocation());
		}}));}

    /**Maps <code>JSplitPane.getLeftComponent()</code> through queue*/
    public Component getLeftComponent() {
	return((Component)runMapping(new MapAction("getLeftComponent") {
		public Object map() {
		    return(((JSplitPane)getSource()).getLeftComponent());
		}}));}

    /**Maps <code>JSplitPane.getMaximumDividerLocation()</code> through queue*/
    public int getMaximumDividerLocation() {
	return(runMapping(new MapIntegerAction("getMaximumDividerLocation") {
		public int map() {
		    return(((JSplitPane)getSource()).getMaximumDividerLocation());
		}}));}

    /**Maps <code>JSplitPane.getMinimumDividerLocation()</code> through queue*/
    public int getMinimumDividerLocation() {
	return(runMapping(new MapIntegerAction("getMinimumDividerLocation") {
		public int map() {
		    return(((JSplitPane)getSource()).getMinimumDividerLocation());
		}}));}

    /**Maps <code>JSplitPane.getOrientation()</code> through queue*/
    public int getOrientation() {
	return(runMapping(new MapIntegerAction("getOrientation") {
		public int map() {
		    return(((JSplitPane)getSource()).getOrientation());
		}}));}

    /**Maps <code>JSplitPane.getRightComponent()</code> through queue*/
    public Component getRightComponent() {
	return((Component)runMapping(new MapAction("getRightComponent") {
		public Object map() {
		    return(((JSplitPane)getSource()).getRightComponent());
		}}));}

    /**Maps <code>JSplitPane.getTopComponent()</code> through queue*/
    public Component getTopComponent() {
	return((Component)runMapping(new MapAction("getTopComponent") {
		public Object map() {
		    return(((JSplitPane)getSource()).getTopComponent());
		}}));}

    /**Maps <code>JSplitPane.getUI()</code> through queue*/
    public SplitPaneUI getUI() {
	return((SplitPaneUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JSplitPane)getSource()).getUI());
		}}));}

    /**Maps <code>JSplitPane.isContinuousLayout()</code> through queue*/
    public boolean isContinuousLayout() {
	return(runMapping(new MapBooleanAction("isContinuousLayout") {
		public boolean map() {
		    return(((JSplitPane)getSource()).isContinuousLayout());
		}}));}

    /**Maps <code>JSplitPane.isOneTouchExpandable()</code> through queue*/
    public boolean isOneTouchExpandable() {
	return(runMapping(new MapBooleanAction("isOneTouchExpandable") {
		public boolean map() {
		    return(((JSplitPane)getSource()).isOneTouchExpandable());
		}}));}

    /**Maps <code>JSplitPane.resetToPreferredSizes()</code> through queue*/
    public void resetToPreferredSizes() {
	runMapping(new MapVoidAction("resetToPreferredSizes") {
		public void map() {
		    ((JSplitPane)getSource()).resetToPreferredSizes();
		}});}

    /**Maps <code>JSplitPane.setBottomComponent(Component)</code> through queue*/
    public void setBottomComponent(final Component component) {
	runMapping(new MapVoidAction("setBottomComponent") {
		public void map() {
		    ((JSplitPane)getSource()).setBottomComponent(component);
		}});}

    /**Maps <code>JSplitPane.setContinuousLayout(boolean)</code> through queue*/
    public void setContinuousLayout(final boolean b) {
	runMapping(new MapVoidAction("setContinuousLayout") {
		public void map() {
		    ((JSplitPane)getSource()).setContinuousLayout(b);
		}});}

    /**Maps <code>JSplitPane.setDividerLocation(double)</code> through queue*/
    public void setDividerLocation(final double d) {
	runMapping(new MapVoidAction("setDividerLocation") {
		public void map() {
		    ((JSplitPane)getSource()).setDividerLocation(d);
		}});}

    /**Maps <code>JSplitPane.setDividerLocation(int)</code> through queue*/
    public void setDividerLocation(final int i) {
	runMapping(new MapVoidAction("setDividerLocation") {
		public void map() {
		    ((JSplitPane)getSource()).setDividerLocation(i);
		}});}

    /**Maps <code>JSplitPane.setDividerSize(int)</code> through queue*/
    public void setDividerSize(final int i) {
	runMapping(new MapVoidAction("setDividerSize") {
		public void map() {
		    ((JSplitPane)getSource()).setDividerSize(i);
		}});}

    /**Maps <code>JSplitPane.setLastDividerLocation(int)</code> through queue*/
    public void setLastDividerLocation(final int i) {
	runMapping(new MapVoidAction("setLastDividerLocation") {
		public void map() {
		    ((JSplitPane)getSource()).setLastDividerLocation(i);
		}});}

    /**Maps <code>JSplitPane.setLeftComponent(Component)</code> through queue*/
    public void setLeftComponent(final Component component) {
	runMapping(new MapVoidAction("setLeftComponent") {
		public void map() {
		    ((JSplitPane)getSource()).setLeftComponent(component);
		}});}

    /**Maps <code>JSplitPane.setOneTouchExpandable(boolean)</code> through queue*/
    public void setOneTouchExpandable(final boolean b) {
	runMapping(new MapVoidAction("setOneTouchExpandable") {
		public void map() {
		    ((JSplitPane)getSource()).setOneTouchExpandable(b);
		}});}

    /**Maps <code>JSplitPane.setOrientation(int)</code> through queue*/
    public void setOrientation(final int i) {
	runMapping(new MapVoidAction("setOrientation") {
		public void map() {
		    ((JSplitPane)getSource()).setOrientation(i);
		}});}

    /**Maps <code>JSplitPane.setRightComponent(Component)</code> through queue*/
    public void setRightComponent(final Component component) {
	runMapping(new MapVoidAction("setRightComponent") {
		public void map() {
		    ((JSplitPane)getSource()).setRightComponent(component);
		}});}

    /**Maps <code>JSplitPane.setTopComponent(Component)</code> through queue*/
    public void setTopComponent(final Component component) {
	runMapping(new MapVoidAction("setTopComponent") {
		public void map() {
		    ((JSplitPane)getSource()).setTopComponent(component);
		}});}

    /**Maps <code>JSplitPane.setUI(SplitPaneUI)</code> through queue*/
    public void setUI(final SplitPaneUI splitPaneUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JSplitPane)getSource()).setUI(splitPaneUI);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private void moveTo(ComponentOperator divOper, int x, int y) {
	divOper.dragNDrop(divOper.getCenterX(), divOper.getCenterY(), x, y);
    }

    private void moveToPosition(ComponentOperator divOper, int nextPosition) {
	if(getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    moveTo(divOper, divOper.getCenterX() + nextPosition, divOper.getCenterY());
	} else {
	    moveTo(divOper, divOper.getCenterX(), divOper.getCenterY() + nextPosition);
	}
    }

    private void moveOnce(ComponentOperator divOper, 
			  int dividerLocation, 
			  int leftPosition, 
			  int rightPosition) {
	int currentLocation = getDividerLocation();
	int currentPosition = 0;
	if(getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    currentPosition = (int)(divOper.getSource().getLocationOnScreen().getX() -
				    getSource().getLocationOnScreen().getX());
	} else {
	    currentPosition = (int)(divOper.getSource().getLocationOnScreen().getY() -
				    getSource().getLocationOnScreen().getY());
	}
	int nextPosition = 0;
	if       (currentLocation > dividerLocation) {
	    nextPosition = (int)((currentPosition + leftPosition) / 2);
	    moveToPosition(divOper, nextPosition - currentPosition);
	    if(currentPosition == (int)(divOper.getSource().getLocationOnScreen().getY() -
					getSource().getLocationOnScreen().getY())) {
		return;
	    }
	    moveOnce(divOper, dividerLocation, leftPosition, currentPosition);
	} else if(currentLocation < dividerLocation) {
	    nextPosition = (int)((currentPosition + rightPosition) / 2);
	    moveToPosition(divOper, nextPosition - currentPosition);
	    if(currentPosition == (int)(divOper.getSource().getLocationOnScreen().getY() -
					getSource().getLocationOnScreen().getY())) {
		return;
	    }
	    moveOnce(divOper, dividerLocation, currentPosition, rightPosition);
	} else { // (currentLocation == dividerLocation) - stop point
	    return;
	}
    }

    private void moveDividerTo(int dividerLocation) {
	makeComponentVisible();
	if(System.getProperty("java.version").startsWith("1.2")) {
	    setDividerLocation(dividerLocation);
	} else {
	    ComponentOperator divOper = new ComponentOperator(findDivider());
	    divOper.copyEnvironment(this);
	    /* workaround */
	    if(getDividerLocation() == -1) {
		moveTo(divOper, divOper.getCenterX() - 1, divOper.getCenterY() - 1);
		if(getDividerLocation() == -1) {
		    moveTo(divOper, divOper.getCenterX() + 1, divOper.getCenterY() + 1);
		}
	    }
	    
	    if(getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
		moveOnce(divOper, dividerLocation, 0, getSource().getWidth());
	    } else {
		moveOnce(divOper, dividerLocation, 0, getSource().getHeight());
	    }
	}
    }

    private void expandTo(int index) {
	ComponentSearcher cs = new ComponentSearcher(findDivider());
	cs.setOutput(output.createErrorOutput());
	JButtonOperator bo = 
	    new JButtonOperator((JButton)cs.findComponent(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(comp instanceof JButton);
		}
		public String getDescription() {
		    return("");
		}
	    }, index));
	bo.copyEnvironment(this);
	bo.push();
    }

    private static class JSplitPaneFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JSplitPaneFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JSplitPane) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
