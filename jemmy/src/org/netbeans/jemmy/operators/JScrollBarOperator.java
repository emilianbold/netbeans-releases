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

import org.netbeans.jemmy.Action;
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

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.ScrollDriver;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

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
 *
 * <BR><BR>Timeouts used: <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>
 * JScrollBarOperator.BeforeDropTimeout - to sleep before drop
 * JScrollBarOperator.DragAndDropScrollingDelta - to sleep before drag steps
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * @see #getUnitIncrement(int)
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JScrollBarOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    private final static long ONE_SCROLL_CLICK_TIMEOUT = 0;
    private final static long WHOLE_SCROLL_TIMEOUT = 60000;
    private final static long BEFORE_DROP_TIMEOUT = 0;
    private final static long DRAG_AND_DROP_SCROLLING_DELTA = 0;

    private static final int MINIMAL_PAD_SIZE = 10;

    private static final int MINIMAL_DRAGGER_SIZE = 5;

    private Timeouts timeouts;
    private TestOut output;
    private JButtonOperator minButtOperator;
    private JButtonOperator maxButtOperator;

    private ScrollDriver driver;

    /**
     * Constructor.
     * @param b JScrollBar component.
     */
    public JScrollBarOperator(JScrollBar b) {
	super(b);
	driver = DriverManager.getScrollDriver(getClass());
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
	super.setTimeouts(timeouts);
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

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (ScrollDriver)DriverManager.
	    getDriver(DriverManager.SCROLL_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Does simple scroll click.
     * @param increase 
     * @throws TimeoutExpiredException
     * deprecated
     */
    public void scroll(boolean increase) {
	scrollToValue(getValue() + (increase ? 1 : -1));
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
     * @throws TimeoutExpiredException
     */
    public void scrollTo(Waitable w, Object waiterParam, boolean increase) {
	scrollTo(new WaitableChecker(w, waiterParam, increase, this));
    }

    /**
     * Scroll scrollbar to the position defined by w ScrollChecker implementation.
     * @param checker ScrollChecker implementation defining scrolling direction, and so on.
     * @see ScrollChecker
     * @throws TimeoutExpiredException
     */
    public void scrollTo(ScrollChecker checker) {
	scrollTo(new CheckerAdjustable(checker, this));
    }

    public void scrollTo(final ScrollAdjuster adj) {
	initOperators();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scroll(JScrollBarOperator.this, adj);
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JScrollBarOperator.WholeScrollTimeout"));
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
	scrollTo(new ValueScrollAdjuster(value));
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
	scrollTo(new ValueScrollAdjuster((int)(getMinimum() + 
					       (getMaximum() - 
						getVisibleAmount() - 
						getMinimum()) * proportionalValue)));
    }

    /**
     * Scrolls to minimum value.
     * @throws TimeoutExpiredException
     */
    public void scrollToMinimum() {
	output.printTrace("Scroll JScrollBar to minimum value\n" +
			  getSource().toString());
	output.printGolden("Scroll JScrollBar to minimum value");
	initOperators();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scrollToMinimum(JScrollBarOperator.this, getOrientation());
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JScrollBarOperator.WholeScrollTimeout"));
    }

    /**
     * Scrolls to maximum value.
     * @throws TimeoutExpiredException
     */
    public void scrollToMaximum() {
	output.printTrace("Scroll JScrollBar to maximum value\n" +
			  getSource().toString());
	output.printGolden("Scroll JScrollBar to maximum value");
	initOperators();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scrollToMaximum(JScrollBarOperator.this, getOrientation());
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JScrollBarOperator.WholeScrollTimeout"));
    }

    public JButtonOperator getDecreaseButton() {
	initOperators();
	return(minButtOperator);
    }

    public JButtonOperator getIncreaseButton() {
	initOperators();
	return(maxButtOperator);
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

    private void initOperators() {
	if(minButtOperator != null && 
	   maxButtOperator != null) {
	    return;
	}
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

    /**
     * Interface can be used to define some kind of complicated
     * scrolling rules.
     */
    public interface ScrollChecker {
	/**
	 * Should return one of the following values:<BR>
	 * ScrollAdjuster.INCREASE_SCROLL_DIRECTION<BR>
	 * ScrollAdjuster.DECREASE_SCROLL_DIRECTION<BR>
	 * ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION<BR>
	 * @see org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster#INCREASE_SCROLL_DIRECTION
	 * @see org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster#DECREASE_SCROLL_DIRECTION
	 * @see org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster#DO_NOT_TOUCH_SCROLL_DIRECTION
	 */
	public int getScrollDirection(JScrollBarOperator oper);

	/**
	 * Scrolling rules decription.
	 */
	public String getDescription();
    }

    private class ValueScrollAdjuster implements ScrollAdjuster {
	int value;
	public ValueScrollAdjuster(int value) {
	    this.value = value;
	}
	public int getScrollDirection() {
	    if(getValue() == value) {
		return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
	    } else {
		return((getValue() < value) ?
		       ScrollAdjuster.INCREASE_SCROLL_DIRECTION :
		       ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
	    }
	}
	public int getScrollOrientation() {
	    return(getOrientation());
	}
	public String getDescription() {
	    return("Scroll to " + Integer.toString(value) + " value");
	}
    }
    private class WaitableChecker implements ScrollAdjuster {
	Waitable w;
	Object waitParam;
	boolean increase;
	boolean reached = false;
	JScrollBarOperator oper;
	public WaitableChecker(Waitable w, Object waitParam, boolean increase, JScrollBarOperator oper) {
	    this.w = w;
	    this.waitParam = waitParam;
	    this.increase = increase;
	    this.oper = oper;
	}
	public int getScrollDirection() {
	    if(!reached && w.actionProduced(waitParam) != null) {
		reached = true;
	    }
	    if(reached) {
		return(this.DO_NOT_TOUCH_SCROLL_DIRECTION);
	    } else {
		return(increase ? 
		       this.INCREASE_SCROLL_DIRECTION :
		       this.DECREASE_SCROLL_DIRECTION);
	    }
	}
	public int getScrollOrientation() {
	    return(getOrientation());
	}
	public String getDescription() {
	    return(w.getDescription());
	}
    }

    private class CheckerAdjustable implements ScrollAdjuster {
	ScrollChecker checker;
	JScrollBarOperator oper;
	public CheckerAdjustable(ScrollChecker checker, JScrollBarOperator oper) {
	    this.checker = checker;
	    this.oper = oper;
	}
	public int getScrollDirection() {
	    return(checker.getScrollDirection(oper));
	}
	public int getScrollOrientation() {
	    return(getOrientation());
	}
	public String getDescription() {
	    return(checker.getDescription());
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
}
