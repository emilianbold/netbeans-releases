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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.ButtonDriver;
import org.netbeans.jemmy.drivers.DriverManager;

import java.awt.Component;
import java.awt.Container;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.event.ItemListener;

import java.util.Hashtable;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * ButtonOperator.PushButtonTimeout - time between checkbox pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait checkbox displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait checkbox enabled <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class CheckboxOperator extends ComponentOperator implements Outputable {

    private TestOut output;
    ButtonDriver driver;

    /**
     * Constructor.
     */
    public CheckboxOperator(Checkbox b) {
	super(b);
	driver = DriverManager.getButtonDriver(getClass());
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Checkbox text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public CheckboxOperator(ContainerOperator cont, String text, int index) {
	this((Checkbox)
	     waitComponent(cont, 
			   new CheckboxByLabelFinder(text,
                                              cont.getComparator()),
			   index));
	copyEnvironment(cont);
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Checkbox text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public CheckboxOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public CheckboxOperator(ContainerOperator cont, int index) {
	this((Checkbox)
	     waitComponent(cont, 
			   new CheckboxFinder(ComponentSearcher.
					       getTrueChooser("Any Checkbox")),
			   index));
	copyEnvironment(cont);
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public CheckboxOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches Checkbox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return Checkbox instance or null if component was not found.
     */
    public static Checkbox findCheckbox(Container cont, ComponentChooser chooser, int index) {
	return((Checkbox)findComponent(cont, new CheckboxFinder(chooser), index));
    }

    /**
     * Searches 0'th Checkbox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return Checkbox instance or null if component was not found.
     */
    public static Checkbox findCheckbox(Container cont, ComponentChooser chooser) {
	return(findCheckbox(cont, chooser, 0));
    }

    /**
     * Searches Checkbox by text.
     * @param cont Container to search component in.
     * @param text Checkbox text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Checkbox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Checkbox findCheckbox(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findCheckbox(cont, 
			     new CheckboxFinder(new CheckboxOperator.
					CheckboxByLabelFinder(text, 
					new DefaultStringComparator(ce, ccs))), 
			     index));
    }

    /**
     * Searches Checkbox by text.
     * @param cont Container to search component in.
     * @param text Checkbox text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Checkbox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Checkbox findCheckbox(Container cont, String text, boolean ce, boolean ccs) {
	return(findCheckbox(cont, text, ce, ccs, 0));
    }

    /**
     * Waits Checkbox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return Checkbox instance.
     * @throws TimeoutExpiredException
     */
    public static Checkbox waitCheckbox(Container cont, ComponentChooser chooser, int index) {
	return((Checkbox)waitComponent(cont, new CheckboxFinder(chooser), index));
    }

    /**
     * Waits 0'th Checkbox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return Checkbox instance.
     * @throws TimeoutExpiredException
     */
    public static Checkbox waitCheckbox(Container cont, ComponentChooser chooser) {
	return(waitCheckbox(cont, chooser, 0));
    }

    /**
     * Waits Checkbox by text.
     * @param cont Container to search component in.
     * @param text Checkbox text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Checkbox instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Checkbox waitCheckbox(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitCheckbox(cont,  
			     new CheckboxFinder(new CheckboxOperator.
						 CheckboxByLabelFinder(text, 
	                                         new DefaultStringComparator(ce, ccs))), 
			     index));
    }

    /**
     * Waits Checkbox by text.
     * @param cont Container to search component in.
     * @param text Checkbox text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Checkbox instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Checkbox waitCheckbox(Container cont, String text, boolean ce, boolean ccs) {
	return(waitCheckbox(cont, text, ce, ccs, 0));
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


    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (ButtonDriver)DriverManager.
	    getDriver(DriverManager.BUTTON_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    public void changeSelection(boolean newValue) {
	makeComponentVisible();
	if(getState() != newValue) {
	    try {
		waitComponentEnabled();
	    } catch(InterruptedException e) {
		throw(new JemmyException("Interrupted!", e));
	    }
	    output.printLine("Change checkbox selection to " + new Boolean(newValue).toString() +
			     "\n    :" + getSource().toString());
	    output.printGolden("Change checkbox selection to " + new Boolean(newValue).toString());
	    driver.push(this);
	}
    }

    public void changeSelectionNoBlock(boolean selected) {
	produceNoBlocking(new NoBlockingAction("Button selection changing") {
		public Object doAction(Object param) {
		    changeSelection(((Boolean)param).booleanValue());
		    return(null);
		}
	    }, new Boolean(selected));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Label", ((Checkbox)getSource()).getLabel());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Checkbox.addItemListener(ItemListener)</code> through queue*/
    public void addItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("addItemListener") {
		public void map() {
		    ((Checkbox)getSource()).addItemListener(itemListener);
		}});}

    /**Maps <code>Checkbox.getCheckboxGroup()</code> through queue*/
    public CheckboxGroup getCheckboxGroup() {
	return((CheckboxGroup)runMapping(new MapAction("getCheckboxGroup") {
		public Object map() {
		    return(((Checkbox)getSource()).getCheckboxGroup());
		}}));}

    /**Maps <code>Checkbox.getLabel()</code> through queue*/
    public String getLabel() {
	return((String)runMapping(new MapAction("getLabel") {
		public Object map() {
		    return(((Checkbox)getSource()).getLabel());
		}}));}

    /**Maps <code>Checkbox.getState()</code> through queue*/
    public boolean getState() {
	return(runMapping(new MapBooleanAction("getState") {
		public boolean map() {
		    return(((Checkbox)getSource()).getState());
		}}));}

    /**Maps <code>Checkbox.removeItemListener(ItemListener)</code> through queue*/
    public void removeItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("removeItemListener") {
		public void map() {
		    ((Checkbox)getSource()).removeItemListener(itemListener);
		}});}

    /**Maps <code>Checkbox.setCheckboxGroup(CheckboxGroup)</code> through queue*/
    public void setCheckboxGroup(final CheckboxGroup grp) {
	runMapping(new MapVoidAction("setCheckboxGroup") {
		public void map() {
		    ((Checkbox)getSource()).setCheckboxGroup(grp);
		}});}

    /**Maps <code>Checkbox.setLabel(String)</code> through queue*/
    public void setLabel(final String string) {
	runMapping(new MapVoidAction("setLabel") {
		public void map() {
		    ((Checkbox)getSource()).setLabel(string);
		}});}

    /**Maps <code>Checkbox.setState(boolean)</code> through queue*/
    public void setState(final boolean state) {
	runMapping(new MapVoidAction("setState") {
		public void map() {
		    ((Checkbox)getSource()).setState(state);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    protected static class CheckboxByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public CheckboxByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Checkbox) {
		if(((Checkbox)comp).getLabel() != null) {
		    return(comparator.equals(((Checkbox)comp).getLabel(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Checkbox with label \"" + label + "\"");
	}
    }

    private static class CheckboxFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public CheckboxFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Checkbox) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
