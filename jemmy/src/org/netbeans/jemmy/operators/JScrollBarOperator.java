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
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.util.EmptyVisualizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;

import java.awt.event.AdjustmentListener;

import java.util.Hashtable;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JScrollBar;

import javax.swing.plaf.ScrollBarUI;

/**
 *
 * Operator is supposed to be used to operate with an instance of
 * javax.swing.JScrollBar class. <BR><BR>
 *
 * Operator can use some or one of next scroll modes:<BR>
 * CLICK_SCROLL_MODEL - Click by increase/decrease button to reach the point.<BR>
 * PUSH_AND_WAIT_SCROLL_MODEL - Push one of the buttons and wait for the point reached. <BR>
 * Since point can be missed during this type scrolling, CLICK_SCROLL_MODEL scrolling can be invoked
 * after if necessary.<BR>
 * DRAG_AND_DROP_SCROLL_MODEL - Two different types of scrolling are really under this type<BR>
 * 1. If stop criteria was set by integer value.<BR>
 *  Dragger is taken and dropped once. 
 * Drop point is calculated pretty aproximately, so second type of drag and drop is executed after.
 * JScrollBarOperator.BeforeDropTimeout timeout is used to sleep before drop. <BR>
 * 2. If stop criteria was set by either ScrollChecker or Waitable implementation
 *  Dragger is tanen, then mouse is moved step by step for number of pixels
 * defined by setDragAndDropStepLength(int) method. Scrolling is stopped if either 
 * ScrollChecker.getScrollDirection returned DO_NOT_TOUCH_SCROLL_DIRECTION or 
 * Waitable.actionProduced returned not null. JScrollBarOperator.DragAndDropScrollingDelta timeout
 * is used to sleep between steps. JScrollBarOperator.BeforeDropTimeout is stell used.
 * Since this scrolling type can be inaccurate too, CLICK SCROLL MODEL scrolling can be invoked
 * if necessary.<BR><BR>
 *
 * If no scroll model was defined by setScrollModel method, defines scroll model dynamically by next algorithm: <BR>
 * If getUnitIncrement(-1) > 1 or getUnitIncrement( 1) > 1 <BR>
 *     use CLICK_SCROLL_MODEL<BR>
 * else <BR>
 *     Scroll model to be used is DRAG_AND_DROP_MODEL.<BR>
 *
 *
 * <BR><BR>Timeouts used: <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>
 * JScrollBarOperator.BeforeDropTimeout - to sleep before drop
 * JScrollBarOperator.DragAndDropScrollingDelta - to sleep before drag steps
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * @see #setScrollModel(int)
 * @see #getUnitIncrement(int)
 * @see #setDragAndDropStepLength(int)
 * @see #CLICK_SCROLL_MODEL
 * @see #PUSH_AND_WAIT_SCROLL_MODEL
 * @see #DRAG_AND_DROP_SCROLL_MODEL
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JScrollBarOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    /**
     * Click scroll model. Respective button is pushed till necessary position reached.
     * @see #setScrollModel(int)
     */
    public static final int CLICK_SCROLL_MODEL = 1;

    /**
     * Push and wait scroll model. Respective button is pressed, and released after necessary position reached. Default model.
     * @see #setScrollModel(int)
     */
    public static final int PUSH_AND_WAIT_SCROLL_MODEL = 2;

    /**
     * Drag'n'drop scroll model. If drag'n'drop is not available (i.e., for example scroll pad is not visible) default model is used.
     * @see #setScrollModel(int)
     */
    public static final int DRAG_AND_DROP_SCROLL_MODEL = 3;

    /**
     * Possible value of the ScrollChecker.getScrollDirection(JScrollBarOperator) method.
     * Scroll down (right) value
     */
    public static final int INCREASE_SCROLL_DIRECTION = 1;

    /**
     * Possible value of the ScrollChecker.getScrollDirection(JScrollBarOperator) method.
     * Scroll up (left) value
     */
    public static final int DECREASE_SCROLL_DIRECTION = -1;

    /**
     * Possible value of the ScrollChecker.getScrollDirection(JScrollBarOperator) method.
     * This value means scrolling has been finished.
     */
    public static final int DO_NOT_TOUCH_SCROLL_DIRECTION = 0;

    private final static long ONE_SCROLL_CLICK_TIMEOUT = 0;
    private final static long WHOLE_SCROLL_TIMEOUT = 60000;
    private final static long BEFORE_DROP_TIMEOUT = 0;
    private final static long DRAG_AND_DROP_SCROLLING_DELTA = 0;

    private static final int UNDEFINED_SCROLL_MODEL = 0;
    private static final int MINIMAL_PAD_SIZE = 10;

    private static final int IS_NOT_BEING_SCROLLED = 0;
    private static final int HAS_BEEN_SCROLLED_SUCCESFULLY = 1;
    private static final int SCROLLING_HAS_BEEN_TIMEOUTED = -1;

    private static final int MINIMAL_DRAGGER_SIZE = 5;

    private Timeouts timeouts;
    private TestOut output;
    private JButtonOperator minButtOperator;
    private JButtonOperator maxButtOperator;
    private int scrollModel = UNDEFINED_SCROLL_MODEL;
    private int dragAndDropStepLength = 100;

    /**
     * Constructor.
     * @param b JScrollBar component.
     */
    public JScrollBarOperator(JScrollBar b) {
	super(b);
	setDragAndDropStepLength(JemmyProperties.getProperties().getCurrentDragAndDropStepLength());
	initOperators();
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JScrollBarOperator(ContainerOperator cont, int index) {
	this((JScrollBar)waitComponent(cont, 
				       new JScrollBarFinder(ComponentSearcher.getTrueChooser("Any container")), 
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
    public JScrollBarOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JScrollBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JScrollBar instance or null if component was not found.
     */
    public static JScrollBar findJScrollBar(Container cont, ComponentChooser chooser, int index) {
	return((JScrollBar)findComponent(cont, new JScrollBarFinder(chooser), index));
    }

    /**
     * Searches 0'th JScrollBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JScrollBar instance or null if component was not found.
     */
    public static JScrollBar findJScrollBar(Container cont, ComponentChooser chooser) {
	return(findJScrollBar(cont, chooser, 0));
    }

    /**
     * Searches JScrollBar in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JScrollBar instance or null if component was not found.
     */
    public static JScrollBar findJScrollBar(Container cont, int index) {
	return(findJScrollBar(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JScrollBar instance"), index));
    }

    /**
     * Searches 0'th JScrollBar in container.
     * @param cont Container to search component in.
     * @return JScrollBar instance or null if component was not found.
     */
    public static JScrollBar findJScrollBar(Container cont) {
	return(findJScrollBar(cont, 0));
    }

    /**
     * Waits JScrollBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JScrollBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollBar waitJScrollBar(Container cont, ComponentChooser chooser, int index)  {
	return((JScrollBar)waitComponent(cont, new JScrollBarFinder(chooser), index));
    }

    /**
     * Waits 0'th JScrollBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JScrollBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollBar waitJScrollBar(Container cont, ComponentChooser chooser) {
	return(waitJScrollBar(cont, chooser, 0));
    }

    /**
     * Waits JScrollBar in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JScrollBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollBar waitJScrollBar(Container cont, int index)  {
	return(waitJScrollBar(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JScrollBar instance"), index));
    }

    /**
     * Waits 0'th JScrollBar in container.
     * @param cont Container to search component in.
     * @return JScrollBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollBar waitJScrollBar(Container cont) {
	return(waitJScrollBar(cont, 0));
    }

    static {
	Timeouts.initDefault("JScrollBarOperator.OneScrollClickTimeout", ONE_SCROLL_CLICK_TIMEOUT);
	Timeouts.initDefault("JScrollBarOperator.WholeScrollTimeout", WHOLE_SCROLL_TIMEOUT);
	Timeouts.initDefault("JScrollBarOperator.BeforeDropTimeout", BEFORE_DROP_TIMEOUT);
	Timeouts.initDefault("JScrollBarOperator.DragAndDropScrollingDelta", DRAG_AND_DROP_SCROLLING_DELTA);
    }

    /**
     * Step length value defines pixel count which mouse is moved for (during one scroll step).
     * Default step length - 1. It can be increased to make operator faster.
     * At the same time point can be missed easily if step length has been increased.
     * @see #getDragAndDropStepLength()
     */
    public void setDragAndDropStepLength(int dragAndDropStepLength) {
	this.dragAndDropStepLength = dragAndDropStepLength;
    }

    /**
     * @see #setDragAndDropStepLength(int)
     */
    public int getDragAndDropStepLength() {
	return(dragAndDropStepLength);
    }

    /**
     * Defines scroll model.
     * @param model New scroll model value.
     * @see #CLICK_SCROLL_MODEL
     * @see #PUSH_AND_WAIT_SCROLL_MODEL
     * @see #DRAG_AND_DROP_SCROLL_MODEL
     * @see #getScrollModel()
     * @see #scrollTo(Waitable, Object, boolean)
     */
    public void setScrollModel(int model) {
	scrollModel = model;
    }

    /**
     * @param model New scroll model value.
     * @return Current scroll model value.
     * @see #setScrollModel(int)
     */
    public int getScrollModel() {
	return(scrollModel);
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
     * Sets operator's timeouts.
     * @param timeouts org.netbeans.jemmy.Timeouts instance.
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
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
     * Does simple scroll click.
     * @param increase 
     * @throws TimeoutExpiredException
     */
    public void scroll(boolean increase) {
	makeComponentVisible();
	initOperators();
	if(increase) {
	    output.printTrace("Increase JScrollBar value\n" +
			      getSource().toString());
	    output.printGolden("Increase JScrollBar value");
	    maxButtOperator.push();
	} else {
	    output.printTrace("Decrease JScrollBar value\n" +
			      getSource().toString());
	    output.printGolden("Decrease JScrollBar value");
	    minButtOperator.push();
	}
    }

    /**
     * Scroll scrollbar to the position defined by w parameter.
     * Do not use this method together with DRAG_AND_DROP acroll model, since that model
     * can miss the point and this method can not do accurate scrolling in the
     * back direction
     * @param w Scrolling is stopped when w.actionProduced(waiterParam) != null
     * @param waiterParam
     * @param increase
     * @see #scrollTo(JScrollBarOperator.ScrollChecker)
     * @see #setScrollModel(int)
     * @throws TimeoutExpiredException
     */
    public void scrollTo(Waitable w, Object waiterParam, boolean increase) {
	scrollTo(new WaitableChecker(w, waiterParam, increase));
    }

    /**
     * Scroll scrollbar to the position defined by w ScrollChecker implementation.
     * @param checker ScrollChecker implementation defining scrolling direction, and so on.
     * @see ScrollChecker
     * @see #setScrollModel(int)
     * @throws TimeoutExpiredException
     */
    public void scrollTo(ScrollChecker checker) {
	makeComponentVisible();
	initOperators();
	int scrModel = getActualScrollModel();
	try {
	    if(scrModel == CLICK_SCROLL_MODEL) {
		clickScroll(checker);
	    } else if(scrModel == DRAG_AND_DROP_SCROLL_MODEL) {
		dragAndDrop(checker);
	    } else {
		pushAndWait(checker);
	    }
	} catch(NullPointerException e) {
	    if(getSource().isVisible()) {
		throw(e);
	    }
	}
    }

    /**
     * Scrolls scroll bar to necessary value.
     * @param value Scroll bar value to scroll to.
     * @throws TimeoutExpiredException
     */
    public void scrollToValue(int value) {
	output.printTrace("Scroll JScrollBar to " + Integer.toString(value) +
			  " value\n" + getSource().toString());
	output.printGolden("Scroll JScrollBar to " + Integer.toString(value) + " value");
	scrollTo(value);
    }

    /**
     * Scrolls scroll bar to necessary proportional value.
     * @param proportionalValue Proportional scroll to. Must be >= 0 and <= 1.
     * @throws TimeoutExpiredException
     */
    public void scrollToValue(double proportionalValue) {
	output.printTrace("Scroll JScrollBar to " + Double.toString(proportionalValue) +
			  " proportional value\n" + getSource().toString());
	output.printGolden("Scroll JScrollBar to " + Double.toString(proportionalValue) + " proportional value");
	scrollTo(getMinimum() + 
		 (int)(proportionalValue * 
		       (getMaximum() - getVisibleAmount() - getMinimum())));
    }

    /**
     * Scrolls to minimum value.
     * @throws TimeoutExpiredException
     */
    public void scrollToMinimum() {
	output.printTrace("Scroll JScrollBar to minimum value\n" +
			  getSource().toString());
	output.printGolden("Scroll JScrollBar to minimum value");
	scrollTo(getMinimum() - MINIMAL_DRAGGER_SIZE);
    }

    /**
     * Scrolls to maximum value.
     * @throws TimeoutExpiredException
     */
    public void scrollToMaximum() {
	output.printTrace("Scroll JScrollBar to maximum value\n" +
			  getSource().toString());
	output.printGolden("Scroll JScrollBar to maximum value");
	scrollTo(getMaximum() - getVisibleAmount() + MINIMAL_DRAGGER_SIZE);
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Minimum", Integer.toString(((JScrollBar)getSource()).getMinimum()));
	result.put("Maximum", Integer.toString(((JScrollBar)getSource()).getMaximum()));
	result.put("Orientation", (((JScrollBar)getSource()).getOrientation() == JScrollBar.HORIZONTAL) ? 
		   "HORIZONTAL" : 
		   "VERTICAL");
	result.put("Value", Integer.toString(((JScrollBar)getSource()).getValue()));
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JScrollBar.addAdjustmentListener(AdjustmentListener)</code> through queue*/
    public void addAdjustmentListener(final AdjustmentListener adjustmentListener) {
	runMapping(new MapVoidAction("addAdjustmentListener") {
		public void map() {
		    ((JScrollBar)getSource()).addAdjustmentListener(adjustmentListener);
		}});}

    /**Maps <code>JScrollBar.getBlockIncrement()</code> through queue*/
    public int getBlockIncrement() {
	return(runMapping(new MapIntegerAction("getBlockIncrement") {
		public int map() {
		    return(((JScrollBar)getSource()).getBlockIncrement());
		}}));}

    /**Maps <code>JScrollBar.getBlockIncrement(int)</code> through queue*/
    public int getBlockIncrement(final int i) {
	return(runMapping(new MapIntegerAction("getBlockIncrement") {
		public int map() {
		    return(((JScrollBar)getSource()).getBlockIncrement(i));
		}}));}

    /**Maps <code>JScrollBar.getMaximum()</code> through queue*/
    public int getMaximum() {
	return(runMapping(new MapIntegerAction("getMaximum") {
		public int map() {
		    return(((JScrollBar)getSource()).getMaximum());
		}}));}

    /**Maps <code>JScrollBar.getMinimum()</code> through queue*/
    public int getMinimum() {
	return(runMapping(new MapIntegerAction("getMinimum") {
		public int map() {
		    return(((JScrollBar)getSource()).getMinimum());
		}}));}

    /**Maps <code>JScrollBar.getModel()</code> through queue*/
    public BoundedRangeModel getModel() {
	return((BoundedRangeModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((JScrollBar)getSource()).getModel());
		}}));}

    /**Maps <code>JScrollBar.getOrientation()</code> through queue*/
    public int getOrientation() {
	return(runMapping(new MapIntegerAction("getOrientation") {
		public int map() {
		    return(((JScrollBar)getSource()).getOrientation());
		}}));}

    /**Maps <code>JScrollBar.getUI()</code> through queue*/
    public ScrollBarUI getUI() {
	return((ScrollBarUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JScrollBar)getSource()).getUI());
		}}));}

    /**Maps <code>JScrollBar.getUnitIncrement()</code> through queue*/
    public int getUnitIncrement() {
	return(runMapping(new MapIntegerAction("getUnitIncrement") {
		public int map() {
		    return(((JScrollBar)getSource()).getUnitIncrement());
		}}));}

    /**Maps <code>JScrollBar.getUnitIncrement(int)</code> through queue*/
    public int getUnitIncrement(final int i) {
	return(runMapping(new MapIntegerAction("getUnitIncrement") {
		public int map() {
		    return(((JScrollBar)getSource()).getUnitIncrement(i));
		}}));}

    /**Maps <code>JScrollBar.getValue()</code> through queue*/
    public int getValue() {
	return(runMapping(new MapIntegerAction("getValue") {
		public int map() {
		    return(((JScrollBar)getSource()).getValue());
		}}));}

    /**Maps <code>JScrollBar.getValueIsAdjusting()</code> through queue*/
    public boolean getValueIsAdjusting() {
	return(runMapping(new MapBooleanAction("getValueIsAdjusting") {
		public boolean map() {
		    return(((JScrollBar)getSource()).getValueIsAdjusting());
		}}));}

    /**Maps <code>JScrollBar.getVisibleAmount()</code> through queue*/
    public int getVisibleAmount() {
	return(runMapping(new MapIntegerAction("getVisibleAmount") {
		public int map() {
		    return(((JScrollBar)getSource()).getVisibleAmount());
		}}));}

    /**Maps <code>JScrollBar.removeAdjustmentListener(AdjustmentListener)</code> through queue*/
    public void removeAdjustmentListener(final AdjustmentListener adjustmentListener) {
	runMapping(new MapVoidAction("removeAdjustmentListener") {
		public void map() {
		    ((JScrollBar)getSource()).removeAdjustmentListener(adjustmentListener);
		}});}

    /**Maps <code>JScrollBar.setBlockIncrement(int)</code> through queue*/
    public void setBlockIncrement(final int i) {
	runMapping(new MapVoidAction("setBlockIncrement") {
		public void map() {
		    ((JScrollBar)getSource()).setBlockIncrement(i);
		}});}

    /**Maps <code>JScrollBar.setMaximum(int)</code> through queue*/
    public void setMaximum(final int i) {
	runMapping(new MapVoidAction("setMaximum") {
		public void map() {
		    ((JScrollBar)getSource()).setMaximum(i);
		}});}

    /**Maps <code>JScrollBar.setMinimum(int)</code> through queue*/
    public void setMinimum(final int i) {
	runMapping(new MapVoidAction("setMinimum") {
		public void map() {
		    ((JScrollBar)getSource()).setMinimum(i);
		}});}

    /**Maps <code>JScrollBar.setModel(BoundedRangeModel)</code> through queue*/
    public void setModel(final BoundedRangeModel boundedRangeModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((JScrollBar)getSource()).setModel(boundedRangeModel);
		}});}

    /**Maps <code>JScrollBar.setOrientation(int)</code> through queue*/
    public void setOrientation(final int i) {
	runMapping(new MapVoidAction("setOrientation") {
		public void map() {
		    ((JScrollBar)getSource()).setOrientation(i);
		}});}

    /**Maps <code>JScrollBar.setUnitIncrement(int)</code> through queue*/
    public void setUnitIncrement(final int i) {
	runMapping(new MapVoidAction("setUnitIncrement") {
		public void map() {
		    ((JScrollBar)getSource()).setUnitIncrement(i);
		}});}

    /**Maps <code>JScrollBar.setValue(int)</code> through queue*/
    public void setValue(final int i) {
	runMapping(new MapVoidAction("setValue") {
		public void map() {
		    ((JScrollBar)getSource()).setValue(i);
		}});}

    /**Maps <code>JScrollBar.setValueIsAdjusting(boolean)</code> through queue*/
    public void setValueIsAdjusting(final boolean b) {
	runMapping(new MapVoidAction("setValueIsAdjusting") {
		public void map() {
		    ((JScrollBar)getSource()).setValueIsAdjusting(b);
		}});}

    /**Maps <code>JScrollBar.setValues(int, int, int, int)</code> through queue*/
    public void setValues(final int i, final int i1, final int i2, final int i3) {
	runMapping(new MapVoidAction("setValues") {
		public void map() {
		    ((JScrollBar)getSource()).setValues(i, i1, i2, i3);
		}});}

    /**Maps <code>JScrollBar.setVisibleAmount(int)</code> through queue*/
    public void setVisibleAmount(final int i) {
	runMapping(new MapVoidAction("setVisibleAmount") {
		public void map() {
		    ((JScrollBar)getSource()).setVisibleAmount(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Silente method scrolls to the value. (Does not provide any output)
     */
    protected void scrollTo(int value) {
	int scrModel = getActualScrollModel();
	makeComponentVisible();
	initOperators();
	ScrollChecker w = new ValueScrollChecker(value);
	boolean increase = value > getValue();
	if(scrModel == CLICK_SCROLL_MODEL) {
	    clickScroll(w);
	} else if(scrModel == DRAG_AND_DROP_SCROLL_MODEL) {
	    dragAndDrop(value);
	} else {
	    pushAndWait(w);
	}
    }

    /**
     * Drag scrollbar to the necessary value.
     */
    protected void dragAndDrop(int value) {
	long startTime = System.currentTimeMillis();
	if(value >= getMaximum() - getVisibleAmount()||
	   value <= getMinimum()) {
	    try {
		dragToValue(value);
	    } catch(NullPointerException e) {
		if(getSource().isVisible()) {
		    throw(e);
		}
	    }
	} else {
	    dragAndDrop(new ValueScrollChecker(value));
	}
    }

    /**
     * Scroll by clicking on buttons.
     */
    protected void clickScroll(ScrollChecker checker)  {
	long startTime = System.currentTimeMillis();
	int direction = checker.getScrollDirection(this);
	if(direction == DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    return;
	}
	while(checker.getScrollDirection(this) == direction) {
	    if(System.currentTimeMillis() - startTime > 
	       timeouts.getTimeout("JScrollBarOperator.WholeScrollTimeout")) {
		throw(new TimeoutExpiredException(checker.getDescription()));
	    }
	    if(direction == INCREASE_SCROLL_DIRECTION) {
		maxButtOperator.push();
		if(getValue() == getMaximum() - getVisibleAmount()) {
		    break;
		}
	    } else {
		minButtOperator.push();
		if(getValue() == getMinimum()) {
		    break;
		}
	    }
	}
    }

    /**
     * Scroll by pushing on button and wait good position.
     */
    protected void pushAndWait(ScrollChecker checker) {
	int direction = checker.getScrollDirection(this);
	if(direction == DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    return;
	}
	Waiter valueWaiter = new Waiter(new WaitingNotifier(this, checker));
	valueWaiter.setTimeouts(timeouts.cloneThis());
	valueWaiter.getTimeouts().setTimeout("Waiter.TimeDelta", 1);
	valueWaiter.getTimeouts().setTimeout("Waiter.WaitingTime",
					     timeouts.
					     getTimeout("JScrollBarOperator.WholeScrollTimeout"));
	valueWaiter.getTimeouts().setTimeout("Waiter.AfterWaitingTime", 0);
	valueWaiter.setOutput(output.createErrorOutput());
	try {
	    if(checker.getScrollDirection(this) == INCREASE_SCROLL_DIRECTION) {
		maxButtOperator.press();
		valueWaiter.waitAction(null);
		maxButtOperator.release();
	    } else {
		minButtOperator.press();
		valueWaiter.waitAction(null);
		minButtOperator.release();
	    }
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	}
	//tiny
	clickScroll(checker);
    }

    /**
     * Scroll by drag'n'drop action.
     */
    protected void dragAndDrop(ScrollChecker checker) {
	int direction = checker.getScrollDirection(this);
	if(direction == DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    return;
	}
	long startTime = System.currentTimeMillis();
	Point pnt = getClickPoint(getValue());
	moveMouse(pnt.x, pnt.y);
	pressMouse(pnt.x, pnt.y);
	while(checker.getScrollDirection(this) == direction) {
	    if(System.currentTimeMillis() - startTime > 
	       timeouts.getTimeout("JScrollBarOperator.WholeScrollTimeout")) {
		throw(new TimeoutExpiredException(checker.getDescription()));
	    }
	    pnt = increasePoint(pnt, (direction == INCREASE_SCROLL_DIRECTION));
	    dragMouse(pnt.x, pnt.y, getDefaultMouseButton(), 0);
	    if(getValue() == getMinimum() &&
	       direction == DECREASE_SCROLL_DIRECTION ||
	       getValue() == getMaximum() - getVisibleAmount() &&
	       direction == INCREASE_SCROLL_DIRECTION) {
		break;
	    }
	    timeouts.sleep("JScrollBarOperator.DragAndDropScrollingDelta");
	}	    
	timeouts.sleep("JScrollBarOperator.BeforeDropTimeout");
	releaseMouse(pnt.x, pnt.y);
	//tiny
	clickScroll(checker);
    }

    private int getActualScrollModel() {
	int scrModel = getScrollModel();
	if(scrModel == UNDEFINED_SCROLL_MODEL) {
	    if(getUnitIncrement(-1) > 1 ||
	       getUnitIncrement( 1) > 1) {
		scrModel = CLICK_SCROLL_MODEL;
	    } else {
		scrModel = DRAG_AND_DROP_SCROLL_MODEL;
	    }
	}
	if(scrModel == DRAG_AND_DROP_SCROLL_MODEL) {
	    if(!isPadVisible()) {
		scrModel = PUSH_AND_WAIT_SCROLL_MODEL;
	    }
	}
	return(scrModel);
    }

    private void dragToValue(int value) {
	Point pnt = getClickPoint(getValue());
	moveMouse(pnt.x, pnt.y);
	pressMouse(pnt.x, pnt.y);
	pnt = getClickPoint(value);
	dragMouse(pnt.x, pnt.y, getDefaultMouseButton(), 0);
	timeouts.sleep("JScrollBarOperator.BeforeDropTimeout");
	releaseMouse(pnt.x, pnt.y);
    }

    private void initOperators() {
	ComponentChooser chooser = new ComponentChooser() {
	    public boolean checkComponent(Component comp) {
		return(comp instanceof JButton);
	    }
	    public String getDescription() {
		return("");
	    }
	};
	ComponentSearcher searcher = new ComponentSearcher((Container)getSource());
	searcher.setOutput(output.createErrorOutput());
	JButton butt0 = (JButton)searcher.findComponent(chooser, 0);
	JButton butt1 = (JButton)searcher.findComponent(chooser, 1);

	if(butt0 == null || butt1 == null) {
	    minButtOperator = null;
	    maxButtOperator = null;
	    return;
	}

	JButton minButt, maxButt;

	if(((JScrollBar)getSource()).getOrientation() == JScrollBar.HORIZONTAL) {
	    if(butt0.getX() < butt1.getX()) {
		minButt = butt0;
		maxButt = butt1;
	    } else {
		minButt = butt1;
		maxButt = butt0;
	    }
	} else {
	    if(butt0.getY() < butt1.getY()) {
		minButt = butt0;
		maxButt = butt1;
	    } else {
		minButt = butt1;
		maxButt = butt0;
	    }
	}
	minButtOperator = new JButtonOperator(minButt);
	maxButtOperator = new JButtonOperator(maxButt);

	minButtOperator.copyEnvironment(this);
	maxButtOperator.copyEnvironment(this);

	minButtOperator.setOutput(output.createErrorOutput());
	maxButtOperator.setOutput(output.createErrorOutput());

	Timeouts times = timeouts.cloneThis();
	times.setTimeout("AbstractButtonOperator.PushButtonTimeout",
			 times.getTimeout("JScrollBarOperator.OneScrollClickTimeout"));
	
	minButtOperator.setTimeouts(times);		 
	maxButtOperator.setTimeouts(times);		 

	minButtOperator.setVisualizer(new EmptyVisualizer());		 
	maxButtOperator.setVisualizer(new EmptyVisualizer());
    }

    private boolean isPadVisible() {
	boolean result = false;
	Point pnt = getClickPoint(getValue());
	moveMouse(pnt.x, pnt.y);
	pressMouse(pnt.x, pnt.y);
	result = getValueIsAdjusting();
	releaseMouse(pnt.x, pnt.y);
	return(result);
    }

    private Point getClickPoint(int value) {
	initOperators();
	int lenght = (getOrientation() == JScrollBar.HORIZONTAL) ?
	    getWidth()  - minButtOperator.getWidth()  - maxButtOperator.getWidth() :
	    getHeight() - minButtOperator.getHeight() - maxButtOperator.getHeight();
	int subpos = (int)(((float)lenght / (getMaximum() - getMinimum())) * value);
	if(((JScrollBar)getSource()).getOrientation() == JScrollBar.HORIZONTAL) {
	    subpos = subpos + minButtOperator.getWidth();
	} else {
	    subpos = subpos + minButtOperator.getHeight();
	}
	subpos = subpos + MINIMAL_DRAGGER_SIZE / 2 + 1;
	return((getOrientation() == JScrollBar.HORIZONTAL) ?
	       new Point(subpos, getHeight() / 2) :
	       new Point(getWidth() / 2, subpos));
    }

    private Point increasePoint(Point pnt, boolean increase) {
	return((getOrientation() == JScrollBar.HORIZONTAL) ?
	       new Point(pnt.x + (increase ? 1 : -1) * getDragAndDropStepLength(), pnt.y) :
	       new Point(pnt.x, pnt.y + (increase ? 1 : -1) * getDragAndDropStepLength()));
    }

    /**
     * Interface can be used to define some kind of complicated
     * scrolling rules.
     */
    public interface ScrollChecker {
	/**
	 * Should return one of the following values:<BR>
	 * INCREASE_SCROLL_DIRECTION<BR>
	 * DECREASE_SCROLL_DIRECTION<BR>
	 * DO_NOT_TOUCH_SCROLL_DIRECTION<BR>
	 * @see #INCREASE_SCROLL_DIRECTION
	 * @see #DECREASE_SCROLL_DIRECTION
	 * @see #DO_NOT_TOUCH_SCROLL_DIRECTION
	 */
	public int getScrollDirection(JScrollBarOperator oper);

	/**
	 * Scrolling rules decription.
	 */
	public String getDescription();
    }

    private class WaitableChecker implements ScrollChecker {
	Waitable w;
	Object waitParam;
	boolean increase;
	boolean reached = false;
	public WaitableChecker(Waitable w, Object waitParam, boolean increase) {
	    this.w = w;
	    this.waitParam = waitParam;
	    this.increase = increase;
	}
	public int getScrollDirection(JScrollBarOperator oper) {
	    if(!reached && w.actionProduced(waitParam) != null) {
		reached = true;
	    }
	    if(reached) {
		return(DO_NOT_TOUCH_SCROLL_DIRECTION);
	    } else {
		return(increase ? 
		       INCREASE_SCROLL_DIRECTION :
		       DECREASE_SCROLL_DIRECTION);
	    }
	}
	public String getDescription() {
	    return(w.getDescription());
	}
    }

    private static class JScrollBarFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JScrollBarFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JScrollBar) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }

    private class WaitingNotifier implements Waitable {
	JScrollBarOperator oper;
	ScrollChecker checker;
	int startValue;
	public WaitingNotifier(JScrollBarOperator oper, ScrollChecker checker) {
	    this.oper = oper;
	    this.checker = checker;
	    startValue = checker.getScrollDirection(oper);
	}
	public Object actionProduced(Object obj) {
	    if(checker.getScrollDirection(oper) != startValue) {
		return(new Integer(checker.getScrollDirection(oper)));
	    } else {
		return(null);
	    }
	}
	public String getDescription() {
	    return(checker.getDescription());
	}
    }

    private class ValueScrollChecker implements ScrollChecker {
	private int value;
	public ValueScrollChecker(int value) {
	    this.value = value;
	}
	public int getScrollDirection(JScrollBarOperator oper) {
	    if(oper.getValue() == value) {
		return(DO_NOT_TOUCH_SCROLL_DIRECTION);
	    } else {
		return((oper.getValue() < value) ?
		       INCREASE_SCROLL_DIRECTION :
		       DECREASE_SCROLL_DIRECTION);
	    }
	}
	public String getDescription() {
	    return("Scroll to " + Integer.toString(value) + " value");
	}
    }
}
