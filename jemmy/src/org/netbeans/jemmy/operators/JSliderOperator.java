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
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import java.util.Dictionary;
import java.util.Hashtable;

import java.awt.Component;
import java.awt.Container;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JSlider;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.SliderUI;

/**
 *
 * Operator is supposed to be used to operate with an instance of
 * javax.swing.JSlider class.
 *
 * <BR><BR>Timeouts used: <BR>
 * JSliderOperator.OneScrollClickTimeout - timeout for one scroll click <BR>
 * JSliderOperator.WholeScrollTimeout - timeout for the whole scrolling <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JSliderOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    private final static long ONE_SCROLL_CLICK_TIMEOUT = 0;
    private final static long WHOLE_SCROLL_TIMEOUT = 60000;

    /**
     * Click scroll model. Mouse is clicked till necessary position reached.
     * @see #setScrollModel(int)
     */
    public static final int CLICK_SCROLL_MODEL = 1;

    /**
     * Push and wait scroll model. Mouse is pressed, and released after necessary position reached.
     * @see #setScrollModel(int)
     */
    public static final int PUSH_AND_WAIT_SCROLL_MODEL = 2;

    private Timeouts timeouts;
    private TestOut output;
    private JButtonOperator minButtOperator;
    private JButtonOperator maxButtOperator;
    private int scrollModel = CLICK_SCROLL_MODEL;

    /**
     * Constructor.
     * @param b JSlider component.
     */
    public JSliderOperator(JSlider b) {
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
    public JSliderOperator(ContainerOperator cont, int index) {
	this((JSlider)waitComponent(cont, 
				    new JSliderFinder(ComponentSearcher.getTrueChooser("Any container")), 
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
    public JSliderOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JSlider in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JSlider instance or null if component was not found.
     */
    public static JSlider findJSlider(Container cont, ComponentChooser chooser, int index) {
	return((JSlider)findComponent(cont, new JSliderFinder(chooser), index));
    }

    /**
     * Searches 0'th JSlider in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSlider instance or null if component was not found.
     */
    public static JSlider findJSlider(Container cont, ComponentChooser chooser) {
	return(findJSlider(cont, chooser, 0));
    }

    /**
     * Searches JSlider in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JSlider instance or null if component was not found.
     */
    public static JSlider findJSlider(Container cont, int index) {
	return(findJSlider(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JSlider instance"), index));
    }

    /**
     * Searches 0'th JSlider in container.
     * @param cont Container to search component in.
     * @return JSlider instance or null if component was not found.
     */
    public static JSlider findJSlider(Container cont) {
	return(findJSlider(cont, 0));
    }

    /**
     * Waits JSlider in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JSlider instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSlider waitJSlider(Container cont, ComponentChooser chooser, int index)  {
	return((JSlider)waitComponent(cont, new JSliderFinder(chooser), index));
    }

    /**
     * Waits 0'th JSlider in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSlider instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSlider waitJSlider(Container cont, ComponentChooser chooser) {
	return(waitJSlider(cont, chooser, 0));
    }

    /**
     * Waits JSlider in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JSlider instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSlider waitJSlider(Container cont, int index)  {
	return(waitJSlider(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JSlider instance"), index));
    }

    /**
     * Waits 0'th JSlider in container.
     * @param cont Container to search component in.
     * @return JSlider instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSlider waitJSlider(Container cont) {
	return(waitJSlider(cont, 0));
    }

    static {
	Timeouts.initDefault("JSliderOperator.OneScrollClickTimeout", ONE_SCROLL_CLICK_TIMEOUT);
	Timeouts.initDefault("JSliderOperator.WholeScrollTimeout", WHOLE_SCROLL_TIMEOUT);
    }

    /**
     * Defines scroll model. Default model value - CLICK_SCROLL_MODEL.
     * @param model New scroll model value.
     * @see #CLICK_SCROLL_MODEL
     * @see #PUSH_AND_WAIT_SCROLL_MODEL
     * @see #getScrollModel()
     * @see #scrollToValue(int)
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

    /**
     * Moves slider to the necessary value.
     * @param value Value to move slider to.
     * @throws TimeoutExpiredException
     */
    public void scrollToValue(int value) {
	output.printTrace("Move JSlider to " + Integer.toString(value) +
			  " value\n" + getSource().toString());
	output.printGolden("Move JSlider to " + Integer.toString(value) + " value");
	scrollTo(value);
    }

    /**
     * Moves slider to the maximal value.
     * @param value Value to move slider to.
     * @throws TimeoutExpiredException
     */
    public void scrollToMaximum() {
	output.printTrace("Move JSlider to maximum value\n" +
			  getSource().toString());
	output.printGolden("Move JSlider to maximum value");
	scrollTo(getMaximum());
    }

    /**
     * Moves slider to the minimal value.
     * @param value Value to move slider to.
     * @throws TimeoutExpiredException
     */
    public void scrollToMinimum() {
	output.printTrace("Move JSlider to minimum value\n" +
			  getSource().toString());
	output.printGolden("Move JSlider to minimum value");
	scrollTo(getMinimum());
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Minimum", Integer.toString(((JSlider)getSource()).getMinimum()));
	result.put("Maximum", Integer.toString(((JSlider)getSource()).getMaximum()));
	result.put("Orientation", (((JSlider)getSource()).getOrientation() == JSlider.HORIZONTAL) ? 
		   "HORIZONTAL" : 
		   "VERTICAL");
	result.put("Inverted", new Boolean(((JSlider)getSource()).getInverted()).toString());
	result.put("Value", Integer.toString(((JSlider)getSource()).getValue()));
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JSlider.addChangeListener(ChangeListener)</code> through queue*/
    public void addChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("addChangeListener") {
		public void map() {
		    ((JSlider)getSource()).addChangeListener(changeListener);
		}});}

    /**Maps <code>JSlider.createStandardLabels(int)</code> through queue*/
    public Hashtable createStandardLabels(final int i) {
	return((Hashtable)runMapping(new MapAction("createStandardLabels") {
		public Object map() {
		    return(((JSlider)getSource()).createStandardLabels(i));
		}}));}

    /**Maps <code>JSlider.createStandardLabels(int, int)</code> through queue*/
    public Hashtable createStandardLabels(final int i, final int i1) {
	return((Hashtable)runMapping(new MapAction("createStandardLabels") {
		public Object map() {
		    return(((JSlider)getSource()).createStandardLabels(i, i1));
		}}));}

    /**Maps <code>JSlider.getExtent()</code> through queue*/
    public int getExtent() {
	return(runMapping(new MapIntegerAction("getExtent") {
		public int map() {
		    return(((JSlider)getSource()).getExtent());
		}}));}

    /**Maps <code>JSlider.getInverted()</code> through queue*/
    public boolean getInverted() {
	return(runMapping(new MapBooleanAction("getInverted") {
		public boolean map() {
		    return(((JSlider)getSource()).getInverted());
		}}));}

    /**Maps <code>JSlider.getLabelTable()</code> through queue*/
    public Dictionary getLabelTable() {
	return((Dictionary)runMapping(new MapAction("getLabelTable") {
		public Object map() {
		    return(((JSlider)getSource()).getLabelTable());
		}}));}

    /**Maps <code>JSlider.getMajorTickSpacing()</code> through queue*/
    public int getMajorTickSpacing() {
	return(runMapping(new MapIntegerAction("getMajorTickSpacing") {
		public int map() {
		    return(((JSlider)getSource()).getMajorTickSpacing());
		}}));}

    /**Maps <code>JSlider.getMaximum()</code> through queue*/
    public int getMaximum() {
	return(runMapping(new MapIntegerAction("getMaximum") {
		public int map() {
		    return(((JSlider)getSource()).getMaximum());
		}}));}

    /**Maps <code>JSlider.getMinimum()</code> through queue*/
    public int getMinimum() {
	return(runMapping(new MapIntegerAction("getMinimum") {
		public int map() {
		    return(((JSlider)getSource()).getMinimum());
		}}));}

    /**Maps <code>JSlider.getMinorTickSpacing()</code> through queue*/
    public int getMinorTickSpacing() {
	return(runMapping(new MapIntegerAction("getMinorTickSpacing") {
		public int map() {
		    return(((JSlider)getSource()).getMinorTickSpacing());
		}}));}

    /**Maps <code>JSlider.getModel()</code> through queue*/
    public BoundedRangeModel getModel() {
	return((BoundedRangeModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((JSlider)getSource()).getModel());
		}}));}

    /**Maps <code>JSlider.getOrientation()</code> through queue*/
    public int getOrientation() {
	return(runMapping(new MapIntegerAction("getOrientation") {
		public int map() {
		    return(((JSlider)getSource()).getOrientation());
		}}));}

    /**Maps <code>JSlider.getPaintLabels()</code> through queue*/
    public boolean getPaintLabels() {
	return(runMapping(new MapBooleanAction("getPaintLabels") {
		public boolean map() {
		    return(((JSlider)getSource()).getPaintLabels());
		}}));}

    /**Maps <code>JSlider.getPaintTicks()</code> through queue*/
    public boolean getPaintTicks() {
	return(runMapping(new MapBooleanAction("getPaintTicks") {
		public boolean map() {
		    return(((JSlider)getSource()).getPaintTicks());
		}}));}

    /**Maps <code>JSlider.getPaintTrack()</code> through queue*/
    public boolean getPaintTrack() {
	return(runMapping(new MapBooleanAction("getPaintTrack") {
		public boolean map() {
		    return(((JSlider)getSource()).getPaintTrack());
		}}));}

    /**Maps <code>JSlider.getSnapToTicks()</code> through queue*/
    public boolean getSnapToTicks() {
	return(runMapping(new MapBooleanAction("getSnapToTicks") {
		public boolean map() {
		    return(((JSlider)getSource()).getSnapToTicks());
		}}));}

    /**Maps <code>JSlider.getUI()</code> through queue*/
    public SliderUI getUI() {
	return((SliderUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JSlider)getSource()).getUI());
		}}));}

    /**Maps <code>JSlider.getValue()</code> through queue*/
    public int getValue() {
	return(runMapping(new MapIntegerAction("getValue") {
		public int map() {
		    return(((JSlider)getSource()).getValue());
		}}));}

    /**Maps <code>JSlider.getValueIsAdjusting()</code> through queue*/
    public boolean getValueIsAdjusting() {
	return(runMapping(new MapBooleanAction("getValueIsAdjusting") {
		public boolean map() {
		    return(((JSlider)getSource()).getValueIsAdjusting());
		}}));}

    /**Maps <code>JSlider.removeChangeListener(ChangeListener)</code> through queue*/
    public void removeChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("removeChangeListener") {
		public void map() {
		    ((JSlider)getSource()).removeChangeListener(changeListener);
		}});}

    /**Maps <code>JSlider.setExtent(int)</code> through queue*/
    public void setExtent(final int i) {
	runMapping(new MapVoidAction("setExtent") {
		public void map() {
		    ((JSlider)getSource()).setExtent(i);
		}});}

    /**Maps <code>JSlider.setInverted(boolean)</code> through queue*/
    public void setInverted(final boolean b) {
	runMapping(new MapVoidAction("setInverted") {
		public void map() {
		    ((JSlider)getSource()).setInverted(b);
		}});}

    /**Maps <code>JSlider.setLabelTable(Dictionary)</code> through queue*/
    public void setLabelTable(final Dictionary dictionary) {
	runMapping(new MapVoidAction("setLabelTable") {
		public void map() {
		    ((JSlider)getSource()).setLabelTable(dictionary);
		}});}

    /**Maps <code>JSlider.setMajorTickSpacing(int)</code> through queue*/
    public void setMajorTickSpacing(final int i) {
	runMapping(new MapVoidAction("setMajorTickSpacing") {
		public void map() {
		    ((JSlider)getSource()).setMajorTickSpacing(i);
		}});}

    /**Maps <code>JSlider.setMaximum(int)</code> through queue*/
    public void setMaximum(final int i) {
	runMapping(new MapVoidAction("setMaximum") {
		public void map() {
		    ((JSlider)getSource()).setMaximum(i);
		}});}

    /**Maps <code>JSlider.setMinimum(int)</code> through queue*/
    public void setMinimum(final int i) {
	runMapping(new MapVoidAction("setMinimum") {
		public void map() {
		    ((JSlider)getSource()).setMinimum(i);
		}});}

    /**Maps <code>JSlider.setMinorTickSpacing(int)</code> through queue*/
    public void setMinorTickSpacing(final int i) {
	runMapping(new MapVoidAction("setMinorTickSpacing") {
		public void map() {
		    ((JSlider)getSource()).setMinorTickSpacing(i);
		}});}

    /**Maps <code>JSlider.setModel(BoundedRangeModel)</code> through queue*/
    public void setModel(final BoundedRangeModel boundedRangeModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((JSlider)getSource()).setModel(boundedRangeModel);
		}});}

    /**Maps <code>JSlider.setOrientation(int)</code> through queue*/
    public void setOrientation(final int i) {
	runMapping(new MapVoidAction("setOrientation") {
		public void map() {
		    ((JSlider)getSource()).setOrientation(i);
		}});}

    /**Maps <code>JSlider.setPaintLabels(boolean)</code> through queue*/
    public void setPaintLabels(final boolean b) {
	runMapping(new MapVoidAction("setPaintLabels") {
		public void map() {
		    ((JSlider)getSource()).setPaintLabels(b);
		}});}

    /**Maps <code>JSlider.setPaintTicks(boolean)</code> through queue*/
    public void setPaintTicks(final boolean b) {
	runMapping(new MapVoidAction("setPaintTicks") {
		public void map() {
		    ((JSlider)getSource()).setPaintTicks(b);
		}});}

    /**Maps <code>JSlider.setPaintTrack(boolean)</code> through queue*/
    public void setPaintTrack(final boolean b) {
	runMapping(new MapVoidAction("setPaintTrack") {
		public void map() {
		    ((JSlider)getSource()).setPaintTrack(b);
		}});}

    /**Maps <code>JSlider.setSnapToTicks(boolean)</code> through queue*/
    public void setSnapToTicks(final boolean b) {
	runMapping(new MapVoidAction("setSnapToTicks") {
		public void map() {
		    ((JSlider)getSource()).setSnapToTicks(b);
		}});}

    /**Maps <code>JSlider.setUI(SliderUI)</code> through queue*/
    public void setUI(final SliderUI sliderUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JSlider)getSource()).setUI(sliderUI);
		}});}

    /**Maps <code>JSlider.setValue(int)</code> through queue*/
    public void setValue(final int i) {
	runMapping(new MapVoidAction("setValue") {
		public void map() {
		    ((JSlider)getSource()).setValue(i);
		}});}

    /**Maps <code>JSlider.setValueIsAdjusting(boolean)</code> through queue*/
    public void setValueIsAdjusting(final boolean b) {
	runMapping(new MapVoidAction("setValueIsAdjusting") {
		public void map() {
		    ((JSlider)getSource()).setValueIsAdjusting(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private void scrollTo(int value) {
	if(value == getValue()) {
	    return;
	}
	makeComponentVisible();
	boolean increase = (value > getValue());
	int xClick, yClick;
	if(getOrientation() == JSlider.HORIZONTAL) {
	    yClick = getHeight() / 2;
	    Timeouts times = timeouts.cloneThis();
	    times.setTimeout("ComponentOperator.MouseClickTimeout",
			     times.getTimeout("JSliderOperator.OneScrollClickTimeout"));
	    super.setTimeouts(times);
	    if(!getInverted() &&  increase ||
	        getInverted() && !increase) {
		xClick = getWidth() - 1;
	    } else {
		xClick = 0;
	    }
	} else {
	    xClick = getWidth() / 2;
	    if(!getInverted() &&  increase ||
	        getInverted() && !increase) {
		yClick = 0;
	    } else {
		yClick = getHeight() - 1;
	    }
	}
	if(getScrollModel() == CLICK_SCROLL_MODEL) {
	    long startTime = System.currentTimeMillis();
	    while(getValue() < value &&  increase ||
		  getValue() > value && !increase) {
		if(System.currentTimeMillis() - startTime > 
		   timeouts.getTimeout("JSliderOperator.WholeScrollTimeout")) {
		    throw(new TimeoutExpiredException("JSlider scrolling"));
		}
		clickMouse(xClick, yClick, 1);
	    }
	} else {
	    Waiter valueWaiter = new Waiter(new Waitable() {
		public Object actionProduced(Object obj) {
		    int vl = ((Integer)((Object[])obj)[0]).intValue();
		    boolean inc = ((Boolean)((Object[])obj)[1]).booleanValue();
		    if(((JSlider)getSource()).getValue() < vl &&  inc ||
		       ((JSlider)getSource()).getValue() > vl && !inc) {
			return(null);
		    } else {
			return("");
		    }
		}
		public String getDescription() {
		    return("JSlider has been scrolled");
		}
	    });
	    Timeouts times = timeouts.cloneThis();
	    times.setTimeout("Waiter.TimeDelta", 1);
	    times.setTimeout("Waiter.WaitingTime",
			     times.getTimeout("JSliderOperator.WholeScrollTimeout"));
	    times.setTimeout("Waiter.AfterWaitingTime", 0);
	    valueWaiter.setOutput(output.createErrorOutput());
	    valueWaiter.setTimeouts(times);
	    moveMouse(xClick, yClick);
	    pressMouse(xClick, yClick);
	    try {
		Object[] param = {new Integer(value), new Boolean(increase)};
		valueWaiter.waitAction(param);
	    } catch(InterruptedException e) {
		output.printStackTrace(e);
	    }
	    releaseMouse(xClick, yClick);
	}
    }

    private static class JSliderFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JSliderFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JSlider) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }

}
