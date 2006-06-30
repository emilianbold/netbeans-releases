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
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class JButtonOperator extends AbstractButtonOperator{

    /**
     * Identifier for a "default button" property.
     * @see #getDump
     */
    public static final String IS_DEFAULT_DPROP = "Default button";

    /**
     * Constructor.
     * @param b a component
     */
    public JButtonOperator(JButton b) {
	super(b);
    }

    /**
     * Constructs a JButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JButtonOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JButton)cont.
             waitSubComponent(new JButtonFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JButtonOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont container
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
     * @param cont container
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
     * @param cont container
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont, int index) {
	this((JButton)
	     waitComponent(cont, 
			   new JButtonFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont container
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
	result.remove(AbstractButtonOperator.IS_SELECTED_DPROP);
	result.put(IS_DEFAULT_DPROP, ((JButton)getSource()).isDefaultButton() ? "true" : "false");
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

    /**
     * Prepares the button to click.
     */
    protected void prepareToClick() {
	makeComponentVisible();
    }

    /**
     * Checks component type.
     */
    public static class JButtonFinder extends Finder {
        /**
         * Constructs JButtonFinder.
         * @param sf other searching criteria.
         */
	public JButtonFinder(ComponentChooser sf) {
            super(JButton.class, sf);
	}
        /**
         * Constructs JButtonFinder.
         */
	public JButtonFinder() {
            super(JButton.class);
	}
    }
}
