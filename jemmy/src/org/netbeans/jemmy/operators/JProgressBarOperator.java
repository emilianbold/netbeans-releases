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

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
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
import javax.swing.JProgressBar;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.ProgressBarUI;

/**
 *
 * Operator is supposed to be used to operate with an instance of
 * javax.swing.JProgressBar class.
 *
 * <BR><BR>Timeouts used: <BR>
 * JProgressBarOperator.WaitValueTimeout - used from waitValue() method <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JProgressBarOperator extends JComponentOperator
    implements Timeoutable, Outputable {

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

    private static long WAIT_VALUE_TIMEOUT = 60000;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Constructor.
     * @param b JProgressBar component.
     */
    public JProgressBarOperator(JProgressBar b) {
	super(b);
    }

    /**
     * Constructs a JProgressBarOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JProgressBarOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JProgressBar)cont.
             waitSubComponent(new JProgressBarFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JProgressBarOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JProgressBarOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JProgressBarOperator(ContainerOperator cont, int index) {
	this((JProgressBar)waitComponent(cont, 
				    new JProgressBarFinder(), 
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
    public JProgressBarOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JProgressBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JProgressBar instance or null if component was not found.
     */
    public static JProgressBar findJProgressBar(Container cont, ComponentChooser chooser, int index) {
	return((JProgressBar)findComponent(cont, new JProgressBarFinder(chooser), index));
    }

    /**
     * Searches 0'th JProgressBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JProgressBar instance or null if component was not found.
     */
    public static JProgressBar findJProgressBar(Container cont, ComponentChooser chooser) {
	return(findJProgressBar(cont, chooser, 0));
    }

    /**
     * Searches JProgressBar in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JProgressBar instance or null if component was not found.
     */
    public static JProgressBar findJProgressBar(Container cont, int index) {
	return(findJProgressBar(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JProgressBar instance"), index));
    }

    /**
     * Searches 0'th JProgressBar in container.
     * @param cont Container to search component in.
     * @return JProgressBar instance or null if component was not found.
     */
    public static JProgressBar findJProgressBar(Container cont) {
	return(findJProgressBar(cont, 0));
    }

    /**
     * Waits JProgressBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JProgressBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JProgressBar waitJProgressBar(Container cont, ComponentChooser chooser, int index)  {
	return((JProgressBar)waitComponent(cont, new JProgressBarFinder(chooser), index));
    }

    /**
     * Waits 0'th JProgressBar in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JProgressBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JProgressBar waitJProgressBar(Container cont, ComponentChooser chooser) {
	return(waitJProgressBar(cont, chooser, 0));
    }

    /**
     * Waits JProgressBar in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JProgressBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JProgressBar waitJProgressBar(Container cont, int index)  {
	return(waitJProgressBar(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JProgressBar instance"), index));
    }

    /**
     * Waits 0'th JProgressBar in container.
     * @param cont Container to search component in.
     * @return JProgressBar instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JProgressBar waitJProgressBar(Container cont) {
	return(waitJProgressBar(cont, 0));
    }

    static {
	Timeouts.initDefault("JProgressBarOperator.WaitValueTimeout", WAIT_VALUE_TIMEOUT);
    }

    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	super.setTimeouts(timeouts);
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    public TestOut getOutput() {
	return(output);
    }

    /**
     * Waits for criteria defined by <code>chooser</code> to be reached.
     * @param chooser an object specifying waiting criteria.
     * @see #waitValue(int)
     * @deprecated Use waitState(ComponentChooser) instead.
     */
    public void waitValue(final ValueChooser chooser) {
	output.printLine("Wait \"" + chooser.getDescription() + 
			 "\" value in progressbar\n    : " +
			 toStringSource());
	output.printGolden("Wait \"" + chooser.getDescription() + 
			 "\" value in progressbar");
	Waiter wt = new Waiter(new Waitable() {
		public Object actionProduced(Object obj) {
		    return(chooser.checkValue(((JProgressBar)getSource()).getValue()) ?
			   "" : null);
		}
		public String getDescription() {
		    return("\"" + chooser.getDescription() + "\" value");
		}
	    });
	wt.setTimeouts(timeouts.cloneThis());
	wt.getTimeouts().setTimeout("Waiter.WaitingTime",
				    getTimeouts().getTimeout("JProgressBarOperator.WaitValueTimeout"));
	wt.setOutput(output.createErrorOutput());
	try {
	    wt.waitAction(null);
	} catch (InterruptedException e) {
	    throw(new JemmyException("Exception during progressbar value waiting", e));
	}
    }

    /**
     * Waits progress bar value to be less or equal to <code>value</code> parameter.
     * Can be used for typical progress bar (when value is increasing).
     * @param value a value to reach.
     * @see Operator.waitState(ComponentChooser)
     */
    public void waitValue(final int value) {
	output.printLine("Wait \"" + value + 
			 "\" value in progressbar\n    : " +
			 toStringSource());
	output.printGolden("Wait \"" + value +
			 "\" value in progressbar");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(((JProgressBar)comp).getValue() >= value);
		}
		public String getDescription() {
		    return("greater then " + Integer.toString(value));
		}
	    });
    }

    /**
     * Waits progress bar string to match <code>value</code> parameter.
     * @param value a string value.
     * @see Operator.waitState(ComponentChooser)
     */
    public void waitValue(final String value) {
	output.printLine("Wait \"" + value + 
			 "\" string in progressbar\n    : " +
			 toStringSource());
	output.printGolden("Wait \"" + value +
			 "\" string in progressbar");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(getComparator().equals(((JProgressBar)comp).getString(), value));
		}
		public String getDescription() {
		    return("'" + value + "' string");
		}
	    });
    }


    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(MINIMUM_DPROP, Integer.toString(((JProgressBar)getSource()).getMinimum()));
	result.put(MAXIMUM_DPROP, Integer.toString(((JProgressBar)getSource()).getMaximum()));
	result.put(VALUE_DPROP, Integer.toString(((JProgressBar)getSource()).getValue()));
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JProgressBar.addChangeListener(ChangeListener)</code> through queue*/
    public void addChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("addChangeListener") {
		public void map() {
		    ((JProgressBar)getSource()).addChangeListener(changeListener);
		}});}

    /**Maps <code>JProgressBar.getMaximum()</code> through queue*/
    public int getMaximum() {
	return(runMapping(new MapIntegerAction("getMaximum") {
		public int map() {
		    return(((JProgressBar)getSource()).getMaximum());
		}}));}

    /**Maps <code>JProgressBar.getMinimum()</code> through queue*/
    public int getMinimum() {
	return(runMapping(new MapIntegerAction("getMinimum") {
		public int map() {
		    return(((JProgressBar)getSource()).getMinimum());
		}}));}

    /**Maps <code>JProgressBar.getModel()</code> through queue*/
    public BoundedRangeModel getModel() {
	return((BoundedRangeModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((JProgressBar)getSource()).getModel());
		}}));}

    /**Maps <code>JProgressBar.getOrientation()</code> through queue*/
    public int getOrientation() {
	return(runMapping(new MapIntegerAction("getOrientation") {
		public int map() {
		    return(((JProgressBar)getSource()).getOrientation());
		}}));}

    /**Maps <code>JProgressBar.getPercentComplete()</code> through queue*/
    public double getPercentComplete() {
	return(runMapping(new MapDoubleAction("getPercentComplete") {
		public double map() {
		    return(((JProgressBar)getSource()).getPercentComplete());
		}}));}

    /**Maps <code>JProgressBar.getString()</code> through queue*/
    public String getString() {
	return((String)runMapping(new MapAction("getString") {
		public Object map() {
		    return(((JProgressBar)getSource()).getString());
		}}));}

    /**Maps <code>JProgressBar.getUI()</code> through queue*/
    public ProgressBarUI getUI() {
	return((ProgressBarUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JProgressBar)getSource()).getUI());
		}}));}

    /**Maps <code>JProgressBar.getValue()</code> through queue*/
    public int getValue() {
	return(runMapping(new MapIntegerAction("getValue") {
		public int map() {
		    return(((JProgressBar)getSource()).getValue());
		}}));}

    /**Maps <code>JProgressBar.isBorderPainted()</code> through queue*/
    public boolean isBorderPainted() {
	return(runMapping(new MapBooleanAction("isBorderPainted") {
		public boolean map() {
		    return(((JProgressBar)getSource()).isBorderPainted());
		}}));}

    /**Maps <code>JProgressBar.isStringPainted()</code> through queue*/
    public boolean isStringPainted() {
	return(runMapping(new MapBooleanAction("isStringPainted") {
		public boolean map() {
		    return(((JProgressBar)getSource()).isStringPainted());
		}}));}

    /**Maps <code>JProgressBar.removeChangeListener(ChangeListener)</code> through queue*/
    public void removeChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("removeChangeListener") {
		public void map() {
		    ((JProgressBar)getSource()).removeChangeListener(changeListener);
		}});}

    /**Maps <code>JProgressBar.setBorderPainted(boolean)</code> through queue*/
    public void setBorderPainted(final boolean b) {
	runMapping(new MapVoidAction("setBorderPainted") {
		public void map() {
		    ((JProgressBar)getSource()).setBorderPainted(b);
		}});}

    /**Maps <code>JProgressBar.setMaximum(int)</code> through queue*/
    public void setMaximum(final int i) {
	runMapping(new MapVoidAction("setMaximum") {
		public void map() {
		    ((JProgressBar)getSource()).setMaximum(i);
		}});}

    /**Maps <code>JProgressBar.setMinimum(int)</code> through queue*/
    public void setMinimum(final int i) {
	runMapping(new MapVoidAction("setMinimum") {
		public void map() {
		    ((JProgressBar)getSource()).setMinimum(i);
		}});}

    /**Maps <code>JProgressBar.setModel(BoundedRangeModel)</code> through queue*/
    public void setModel(final BoundedRangeModel boundedRangeModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((JProgressBar)getSource()).setModel(boundedRangeModel);
		}});}

    /**Maps <code>JProgressBar.setOrientation(int)</code> through queue*/
    public void setOrientation(final int i) {
	runMapping(new MapVoidAction("setOrientation") {
		public void map() {
		    ((JProgressBar)getSource()).setOrientation(i);
		}});}

    /**Maps <code>JProgressBar.setString(String)</code> through queue*/
    public void setString(final String string) {
	runMapping(new MapVoidAction("setString") {
		public void map() {
		    ((JProgressBar)getSource()).setString(string);
		}});}

    /**Maps <code>JProgressBar.setStringPainted(boolean)</code> through queue*/
    public void setStringPainted(final boolean b) {
	runMapping(new MapVoidAction("setStringPainted") {
		public void map() {
		    ((JProgressBar)getSource()).setStringPainted(b);
		}});}

    /**Maps <code>JProgressBar.setUI(ProgressBarUI)</code> through queue*/
    public void setUI(final ProgressBarUI progressBarUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JProgressBar)getSource()).setUI(progressBarUI);
		}});}

    /**Maps <code>JProgressBar.setValue(int)</code> through queue*/
    public void setValue(final int i) {
	runMapping(new MapVoidAction("setValue") {
		public void map() {
		    ((JProgressBar)getSource()).setValue(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Interface to define criteria for <code>waitValue(ValueChooser)</code>
     * method.
     * @see #waitValue(int)
     * @deprecated Use waitState(ComponentChooser) instead.
     */
    public interface ValueChooser {
        /**
         * Check if criteria jave been reached.
         * @param value current value.
         * @return true if criteria reached.
         */
	public boolean checkValue(int value);
        /**
         * A description.
         * @return a description.
         */
	public String getDescription();
    }

    /**
     * Checks component type.
     */
    public static class JProgressBarFinder extends Finder {
        /**
         * Constructs JProgressBarFinder.
         * @param sf other searching criteria.
         */
	public JProgressBarFinder(ComponentChooser sf) {
            super(JProgressBar.class, sf);
	}
        /**
         * Constructs JProgressBarFinder.
         */
	public JProgressBarFinder() {
            super(JProgressBar.class);
	}
    }

}
