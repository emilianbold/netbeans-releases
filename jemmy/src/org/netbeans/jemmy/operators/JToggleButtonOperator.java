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

import javax.swing.JToggleButton;

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

public class JToggleButtonOperator extends AbstractButtonOperator{

    /**
     * Constructor.
     * @param b a component
     */
    public JToggleButtonOperator(JToggleButton b) {
	super(b);
    }

    /**
     * Constructs a JToggleButtonOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JToggleButtonOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JToggleButton)cont.
             waitSubComponent(new JToggleButtonFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JToggleButtonOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JToggleButtonOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Button text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JToggleButtonOperator(ContainerOperator cont, String text, int index) {
	this((JToggleButton)
	     waitComponent(cont, 
			   new JToggleButtonFinder(new AbstractButtonOperator.
						   AbstractButtonByLabelFinder(text, 
									       cont.getComparator())),
			   index));
	copyEnvironment(cont);
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JToggleButtonOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }


    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JToggleButtonOperator(ContainerOperator cont, int index) {
	this((JToggleButton)
	     waitComponent(cont, 
			   new JToggleButtonFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @throws TimeoutExpiredException
     */
    public JToggleButtonOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JToggleButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JToggleButton instance or null if component was not found.
     */
    public static JToggleButton findJToggleButton(Container cont, ComponentChooser chooser, int index) {
	return((JToggleButton)findAbstractButton(cont, new JToggleButtonFinder(chooser), index));
    }

    /**
     * Searches 0'th JToggleButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JToggleButton instance or null if component was not found.
     */
    public static JToggleButton findJToggleButton(Container cont, ComponentChooser chooser) {
	return(findJToggleButton(cont, chooser, 0));
    }

    /**
     * Searches JToggleButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JToggleButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JToggleButton findJToggleButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJToggleButton(cont, 
				 new JToggleButtonFinder(new AbstractButtonOperator.
							 AbstractButtonByLabelFinder(text, 
										     new DefaultStringComparator(ce, ccs))), 
				 index));
    }

    /**
     * Searches JToggleButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JToggleButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JToggleButton findJToggleButton(Container cont, String text, boolean ce, boolean ccs) {
	return(findJToggleButton(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JToggleButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JToggleButton instance.
     * @throws TimeoutExpiredException
     */
    public static JToggleButton waitJToggleButton(Container cont, ComponentChooser chooser, int index) {
	return((JToggleButton)waitAbstractButton(cont, new JToggleButtonFinder(chooser), index));
    }

    /**
     * Waits 0'th JToggleButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JToggleButton instance.
     * @throws TimeoutExpiredException
     */
    public static JToggleButton waitJToggleButton(Container cont, ComponentChooser chooser) {
	return(waitJToggleButton(cont, chooser, 0));
    }

    /**
     * Waits JToggleButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JToggleButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JToggleButton waitJToggleButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJToggleButton(cont, 
				 new JToggleButtonFinder(new AbstractButtonOperator.
							 AbstractButtonByLabelFinder(text, 
										     new DefaultStringComparator(ce, ccs))), 
				 index));
    }

    /**
     * Waits JToggleButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JToggleButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JToggleButton waitJToggleButton(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJToggleButton(cont, text, ce, ccs, 0));
    }

    /**
     * Prepares the button to click.
     */
    protected void prepareToClick() {
	makeComponentVisible();
    }

    /**
     * Checks component type.
     */
    public static class JToggleButtonFinder extends Finder {
        /**
         * Constructs JToggleButtonFinder.
         * @param sf other searching criteria.
         */
	public JToggleButtonFinder(ComponentChooser sf) {
            super(JToggleButton.class, sf);
	}
        /**
         * Constructs JToggleButtonFinder.
         */
	public JToggleButtonFinder() {
            super(JToggleButton.class);
	}
    }
}
