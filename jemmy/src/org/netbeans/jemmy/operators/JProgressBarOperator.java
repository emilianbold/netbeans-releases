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
 * JProgressBarOperator.WaitValueTimeout - used from waitValue() method <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JProgressBarOperator extends JComponentOperator
    implements Timeoutable, Outputable {

    private static long WAIT_VALUE_TIMEOUT = 10000;

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
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JProgressBarOperator(ContainerOperator cont, int index) {
	this((JProgressBar)waitComponent(cont, 
				    new JProgressBarFinder(ComponentSearcher.getTrueChooser("Any container")), 
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
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
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
     * Waits for criteria defined by <code>chooser</code> to be reached.
     * @see #waitValue(int)
     */
    public void waitValue(final ValueChooser chooser) {
	output.printLine("Wait \"" + chooser.getDescription() + 
			 "\" value in progressbar\n    : " +
			 getSource().toString());
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
	wt.setTimeouts(timeouts);
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
     * @see #waitValue(JProgressBarOperator.ValueChooser)
     */
    public void waitValue(final int value) {
	waitValue(new ValueChooser() {
		public boolean checkValue(int val) {
		    return(value <= val);
		}
		public String getDescription() {
		    return("greater then " + Integer.toString(value));
		}
	    });
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Minimum", Integer.toString(((JProgressBar)getSource()).getMinimum()));
	result.put("Maximum", Integer.toString(((JProgressBar)getSource()).getMaximum()));
	result.put("Value", Integer.toString(((JProgressBar)getSource()).getValue()));
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
     */
    public interface ValueChooser {
	public boolean checkValue(int value);
	public String getDescription();
    }

    private static class JProgressBarFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JProgressBarFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JProgressBar) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }

}
