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

import java.util.Hashtable;

import javax.swing.JButton;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * AbstractButtonOperator.PushButtonTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class JButtonOperator extends AbstractButtonOperator{

    /**
     * Constructor.
     */
    public JButtonOperator(JButton b) {
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
    public JButtonOperator(ContainerOperator cont, String text, int index) {
	this((JButton)
	     waitComponent(cont, 
			   new JButtonFinder(new AbstractButtonOperator.
					     AbstractButtonByLabelFinder(text, 
									 cont.getComparator())),
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
    public JButtonOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont, int index) {
	this((JButton)
	     waitComponent(cont, 
			   new JButtonFinder(ComponentSearcher.
					     getTrueChooser("Any JButton")),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JButton instance or null if component was not found.
     */
    public static JButton findJButton(Container cont, ComponentChooser chooser, int index) {
	return((JButton)findAbstractButton(cont, new JButtonFinder(chooser), index));
    }

    /**
     * Searches 0'th JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JButton instance or null if component was not found.
     */
    public static JButton findJButton(Container cont, ComponentChooser chooser) {
	return(findJButton(cont, chooser, 0));
    }

    /**
     * Searches JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JButton findJButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJButton(cont, 
			   new JButtonFinder(new AbstractButtonOperator.
					     AbstractButtonByLabelFinder(text, 
									 new DefaultStringComparator(ce, ccs))), 
			   index));
    }

    /**
     * Searches JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JButton findJButton(Container cont, String text, boolean ce, boolean ccs) {
	return(findJButton(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JButton instance.
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, ComponentChooser chooser, int index) {
	return((JButton)waitAbstractButton(cont, new JButtonFinder(chooser), index));
    }

    /**
     * Waits 0'th JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JButton instance.
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, ComponentChooser chooser) {
	return(waitJButton(cont, chooser, 0));
    }

    /**
     * Waits JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJButton(cont,  
			   new JButtonFinder(new AbstractButtonOperator.
					     AbstractButtonByLabelFinder(text, 
									 new DefaultStringComparator(ce, ccs))), 
			   index));
    }

    /**
     * Waits JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJButton(cont, text, ce, ccs, 0));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.remove("Selected");
	result.put("Default button", new Boolean(((JButton)getSource()).isDefaultButton()).toString());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JButton.isDefaultButton()</code> through queue*/
    public boolean isDefaultButton() {
	return(runMapping(new MapBooleanAction("isDefaultButton") {
		public boolean map() {
		    return(((JButton)getSource()).isDefaultButton());
		}}));}

    /**Maps <code>JButton.isDefaultCapable()</code> through queue*/
    public boolean isDefaultCapable() {
	return(runMapping(new MapBooleanAction("isDefaultCapable") {
		public boolean map() {
		    return(((JButton)getSource()).isDefaultCapable());
		}}));}

    /**Maps <code>JButton.setDefaultCapable(boolean)</code> through queue*/
    public void setDefaultCapable(final boolean b) {
	runMapping(new MapVoidAction("setDefaultCapable") {
		public void map() {
		    ((JButton)getSource()).setDefaultCapable(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    protected void prepareToClick() {
	makeComponentVisible();
    }

    private static class JButtonFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JButtonFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JButton) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
