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

import javax.swing.JCheckBox;

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

public class JCheckBoxOperator extends JToggleButtonOperator{

    /**
     * Constructor.
     */
    public JCheckBoxOperator(JCheckBox b) {
	super(b);
    }

    public JCheckBoxOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JCheckBox)cont.
             waitSubComponent(new JCheckBoxFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    public JCheckBoxOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
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
    public JCheckBoxOperator(ContainerOperator cont, String text, int index) {
	this((JCheckBox)
	     waitComponent(cont, 
			   new JCheckBoxFinder(new AbstractButtonOperator.
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
    public JCheckBoxOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JCheckBoxOperator(ContainerOperator cont, int index) {
	this((JCheckBox)
	     waitComponent(cont, 
			   new JCheckBoxFinder(),
			   index));
	copyEnvironment(cont);
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JCheckBoxOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JCheckBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JCheckBox instance or null if component was not found.
     */
    public static JCheckBox findJCheckBox(Container cont, ComponentChooser chooser, int index) {
	return((JCheckBox)findJToggleButton(cont, new JCheckBoxFinder(chooser), index));
    }

    /**
     * Searches 0'th JCheckBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JCheckBox instance or null if component was not found.
     */
    public static JCheckBox findJCheckBox(Container cont, ComponentChooser chooser) {
	return(findJCheckBox(cont, chooser, 0));
    }

    /**
     * Searches JCheckBox by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JCheckBox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JCheckBox findJCheckBox(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJCheckBox(cont, 
			     new JCheckBoxFinder(new AbstractButtonOperator.
						 AbstractButtonByLabelFinder(text, 
									     new DefaultStringComparator(ce, ccs))), 
			     index));
    }

    /**
     * Searches JCheckBox by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JCheckBox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JCheckBox findJCheckBox(Container cont, String text, boolean ce, boolean ccs) {
	return(findJCheckBox(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JCheckBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JCheckBox instance.
     * @throws TimeoutExpiredException
     */
    public static JCheckBox waitJCheckBox(Container cont, ComponentChooser chooser, int index) {
	return((JCheckBox)waitJToggleButton(cont, new JCheckBoxFinder(chooser), index));
    }

    /**
     * Waits 0'th JCheckBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JCheckBox instance.
     * @throws TimeoutExpiredException
     */
    public static JCheckBox waitJCheckBox(Container cont, ComponentChooser chooser) {
	return(waitJCheckBox(cont, chooser, 0));
    }

    /**
     * Waits JCheckBox by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JCheckBox instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JCheckBox waitJCheckBox(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJCheckBox(cont,  
			     new JCheckBoxFinder(new AbstractButtonOperator.
						 AbstractButtonByLabelFinder(text, 
									     new DefaultStringComparator(ce, ccs))), 
			     index));
    }

    /**
     * Waits JCheckBox by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JCheckBox instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JCheckBox waitJCheckBox(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJCheckBox(cont, text, ce, ccs, 0));
    }

    public static class JCheckBoxFinder extends Finder {
	public JCheckBoxFinder(ComponentChooser sf) {
            super(JCheckBox.class, sf);
	}
	public JCheckBoxFinder() {
            super(JCheckBox.class);
	}
    }
}
