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
import org.netbeans.jemmy.TimeoutExpiredException;

import java.awt.Component;
import java.awt.Container;
import java.awt.Label;

import java.util.Hashtable;

/**
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class LabelOperator extends ComponentOperator {

    /**
     * Constructor.
     */
    public LabelOperator(Label b) {
	super(b);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Button text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public LabelOperator(ContainerOperator cont, String text, int index) {
	this((Label)waitComponent(cont, 
				   new LabelByLabelFinder(text, 
							   cont.getComparator()),
				   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public LabelOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public LabelOperator(ContainerOperator cont, int index) {
	this((Label)
	     waitComponent(cont, 
			   new LabelFinder(ComponentSearcher.
					    getTrueChooser("Any Label")),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public LabelOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches Label in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return Label instance or null if component was not found.
     */
    public static Label findLabel(Container cont, ComponentChooser chooser, int index) {
	return((Label)findComponent(cont, new LabelFinder(chooser), index));
    }

    /**
     * Searches Label in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @return Label instance or null if component was not found.
     */
    public static Label findLabel(Container cont, ComponentChooser chooser) {
	return(findLabel(cont, chooser, 0));
    }

    /**
     * Searches Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Label instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Label findLabel(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findLabel(cont, new LabelByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Label instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Label findLabel(Container cont, String text, boolean ce, boolean ccs) {
	return(findLabel(cont, text, ce, ccs, 0));
    }

    /**
     * Waits Label in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return Label instance.
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(final Container cont, final ComponentChooser chooser, final int index) {
	return((Label)waitComponent(cont, new LabelFinder(chooser), index));
    }

    /**
     * Waits Label in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @return Label instance.
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(Container cont, ComponentChooser chooser) {
	return(waitLabel(cont, chooser, 0));
    }

    /**
     * Waits Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Label instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitLabel(cont, new LabelByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Label instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(Container cont, String text, boolean ce, boolean ccs) {
	return(waitLabel(cont, text, ce, ccs, 0));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	if(((Label)getSource()).getText() != null) {
	    result.put("Text", ((Label)getSource()).getText());
	} else {
	    result.put("Text", "null");
	}
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Label.getAlignment()</code> through queue*/
    public int getAlignment() {
	return(runMapping(new MapIntegerAction("getAlignment") {
		public int map() {
		    return(((Label)getSource()).getAlignment());
		}}));}

    /**Maps <code>Label.getText()</code> through queue*/
    public String getText() {
	return((String)runMapping(new MapAction("getText") {
		public Object map() {
		    return(((Label)getSource()).getText());
		}}));}

    /**Maps <code>Label.setAlignment(int)</code> through queue*/
    public void setAlignment(final int i) {
	runMapping(new MapVoidAction("setAlignment") {
		public void map() {
		    ((Label)getSource()).setAlignment(i);
		}});}

    /**Maps <code>Label.setText(String)</code> through queue*/
    public void setText(final String string) {
	runMapping(new MapVoidAction("setText") {
		public void map() {
		    ((Label)getSource()).setText(string);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private static class LabelByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public LabelByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Label) {
		if(((Label)comp).getText() != null) {
		    return(comparator.equals(((Label)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Label with text \"" + label + "\"");
	}
    }

    private static class LabelFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public LabelFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Label) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
