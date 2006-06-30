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

package org.netbeans.jemmy.operators;

import java.util.Dictionary;
import java.util.Hashtable;

import java.awt.Component;
import java.awt.Container;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JSlider;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.SliderUI;

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.ScrollDriver;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

/**
 * Covers <code>javax.swing.JSlider</code> component.
 *
 * <BR><BR>Timeouts used: <BR>
 * JSliderOperator.WholeScrollTimeout - time for the whole scrolling. <BR>
 * JSliderOperator.ScrollingDelta - time delta to verify result durong scrolling. <BR>
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JSliderOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    private final static long ONE_SCROLL_CLICK_TIMEOUT = 0;
    private final static long WHOLE_SCROLL_TIMEOUT = 60000;
    private final static long SCROLLING_DELTA = 0;

    private ScrollDriver driver;

    /**
     * Identifier for a "minimum" property.
     * @see #getDump
     */
    public static final String MINIMUM_DPROP = "Minimum";

    /**
     * Identifier for a "maximum" property.
     * @see #getDump
     */
    public static final String MAXIMUM_DPROP = "Maximum";

    /**
     * Identifier for a "value" property.
     * @see #getDump
     */
    public static final String VALUE_DPROP = "Value";

    /**
     * Identifier for a "orientation" property.
     * @see #getDump
     */
    public static final String ORIENTATION_DPROP = "Orientation";

    /**
     * Identifier for a "HORIZONTAL" value of "orientation" property.
     * @see #getDump
     */
    public static final String HORIZONTAL_ORIENTATION_DPROP_VALUE = "HORIZONTAL";

    /**
     * Identifier for a "VERTICAL" value of "orientation" property.
     * @see #getDump
     */
    public static final String VERTICAL_ORIENTATION_DPROP_VALUE = "VERTICAL";

    /**
     * Identifier for a "inverted" property.
     */
    public static final String IS_INVERTED_DPROP = "Inverted";

    /**
     * Scrolling model.
     * @see #setScrollModel(int)
     * @deprecated All actions are prformed throw a <code>ScrollDriver</code> registered
     * for this component, So this field is useless.
     */
    public static final int CLICK_SCROLL_MODEL = 1;

    /**
     * Push and wait scroll model. Mouse is pressed, and released after necessary position reached.
     * @see #setScrollModel(int)
     * @deprecated All actions are prformed throw a <code>ScrollDriver</code> registered
     * for this component, So this field is useless.
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
	driver = DriverManager.getScrollDriver(getClass());
    }

    /**
     * Constructs a JSliderOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JSliderOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JSlider)cont.
             waitSubComponent(new JSliderFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JSliderOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JSliderOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
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
				    new JSliderFinder(), 
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
	Timeouts.initDefault("JSliderOperator.ScrollingDelta", SCROLLING_DELTA);
    }

    /**
     * Defines scroll model. Default model value - CLICK_SCROLL_MODEL.
     * @param model New scroll model value.
     * @see #CLICK_SCROLL_MODEL
     * @see #PUSH_AND_WAIT_SCROLL_MODEL
     * @see #getScrollModel()
     * @see #scrollToValue(int)
     * @deprecated All actions are prformed throw a <code>ScrollDriver</code> registered
     * for this component, so value set by this method is ignored.
     */
    public void setScrollModel(int model) {
        scrollModel = model;
    }

    /**
     * Specifies the scroll model.
     * @return Current scroll model value.
     * @see #setScrollModel(int)
     * @deprecated All actions are prformed throw a <code>ScrollDriver</code> registered
     * for this component, so value returned by this method is ignored.
     */
    public int getScrollModel() {
	return(scrollModel);
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    public TestOut getOutput() {
	return(output);
    }

    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	super.setTimeouts(timeouts);
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Scrolls slider to the position defined by a ScrollAdjuster implementation.
     * @param adj defines scrolling direction, and so on.
     * @throws TimeoutExpiredException
     */
    public void scrollTo(final ScrollAdjuster adj) {
        makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scroll(JSliderOperator.this, adj);
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JSliderOperator.WholeScrollTimeout"));
    }

    /**
     * Moves slider to the necessary value.
     * @param value Value to move slider to.
     * @throws TimeoutExpiredException
     */
    public void scrollToValue(int value) {
	output.printTrace("Move JSlider to " + Integer.toString(value) +
			  " value\n" + toStringSource());
	output.printGolden("Move JSlider to " + Integer.toString(value) + " value");
        scrollTo(new ValueScrollAdjuster(value));
    }

    /**
     * Moves slider to the maximal value.
     * @throws TimeoutExpiredException
     */
    public void scrollToMaximum() {
	output.printTrace("Move JSlider to maximum value\n" +
			  toStringSource());
	output.printGolden("Move JSlider to maximum value");
	scrollToValue(getMaximum());
    }

    /**
     * Moves slider to the minimal value.
     * @throws TimeoutExpiredException
     */
    public void scrollToMinimum() {
	output.printTrace("Move JSlider to minimum value\n" +
			  toStringSource());
	output.printGolden("Move JSlider to minimum value");
	scrollToValue(getMinimum());
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(MINIMUM_DPROP, Integer.toString(((JSlider)getSource()).getMinimum()));
	result.put(MAXIMUM_DPROP, Integer.toString(((JSlider)getSource()).getMaximum()));
	result.put(ORIENTATION_DPROP, (((JSlider)getSource()).getOrientation() == JSlider.HORIZONTAL) ? 
		   HORIZONTAL_ORIENTATION_DPROP_VALUE : 
		   VERTICAL_ORIENTATION_DPROP_VALUE);
	result.put(IS_INVERTED_DPROP, ((JSlider)getSource()).getInverted() ? "true" : "false");
	result.put(VALUE_DPROP, Integer.toString(((JSlider)getSource()).getValue()));
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

    /**
     * Checks component type.
     */
    public static class JSliderFinder extends Finder {
        /**
         * Constructs JSliderFinder.
         * @param sf other searching criteria.
         */
	public JSliderFinder(ComponentChooser sf) {
            super(JSlider.class, sf);
	}
        /**
         * Constructs JSliderFinder.
         */
	public JSliderFinder() {
            super(JSlider.class);
	}
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
}
