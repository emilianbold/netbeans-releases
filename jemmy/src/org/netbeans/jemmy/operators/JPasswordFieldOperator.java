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

import javax.swing.JPasswordField;

/**
 * <BR><BR>Timeouts used: <BR>
 * JTextComponentOperator.PushKeyTimeout - time between key pressing and releasing during text typing <BR>
 * JTextComponentOperator.BetweenKeysTimeout - time to sleep between two chars typing <BR>
 * JTextComponentOperator.ChangeCaretPositionTimeout - maximum time to chenge caret position <BR>
 * JTextComponentOperator.TypeTextTimeout - maximum time to type text <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitFocusTimeout - time to wait component focus <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JPasswordFieldOperator extends JTextFieldOperator{

    /**
     * Identifier for a "echo char" property.
     * @see #getDump
     */
    public static final String ECHO_CHAR_DPROP = "Echo char";

    /**
     * Constructor.
     * @param b a component
     */
    public JPasswordFieldOperator(JPasswordField b) {
	super(b);
    }

    /**
     * Constructs a JPasswordFieldOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JPasswordFieldOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JPasswordField)cont.
             waitSubComponent(new JPasswordFieldFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JPasswordFieldOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JPasswordFieldOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JPasswordFieldOperator(ContainerOperator cont, String text, int index) {
	this((JPasswordField)
	     waitComponent(cont, 
			   new JPasswordFieldFinder(new JTextComponentOperator.
                                                    JTextComponentByTextFinder(text, 
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
    public JPasswordFieldOperator(ContainerOperator cont, String text) {
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
    public JPasswordFieldOperator(ContainerOperator cont, int index) {
	this((JPasswordField)
	     waitComponent(cont, 
			   new JPasswordFieldFinder(),
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
    public JPasswordFieldOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JPasswordField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JPasswordField instance or null if component was not found.
     */
    public static JPasswordField findJPasswordField(Container cont, ComponentChooser chooser, int index) {
	return((JPasswordField)findJTextComponent(cont, new JPasswordFieldFinder(chooser), index));
    }

    /**
     * Searches JPasswordField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JPasswordField instance or null if component was not found.
     */
    public static JPasswordField findJPasswordField(Container cont, ComponentChooser chooser) {
	return(findJPasswordField(cont, chooser, 0));
    }

    /**
     * Searches JPasswordField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JPasswordField instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JPasswordField findJPasswordField(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJPasswordField(cont, 
			      new JPasswordFieldFinder(new JTextComponentOperator.
                                                       JTextComponentByTextFinder(text, 
                                                                                  new DefaultStringComparator(ce, ccs))), 
			      index));
    }

    /**
     * Searches JPasswordField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JPasswordField instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JPasswordField findJPasswordField(Container cont, String text, boolean ce, boolean ccs) {
	return(findJPasswordField(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JPasswordField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JPasswordField instance.
     * @throws TimeoutExpiredException
     */
    public static JPasswordField waitJPasswordField(Container cont, ComponentChooser chooser, int index){
	return((JPasswordField)waitJTextComponent(cont, new JPasswordFieldFinder(chooser), index));
    }

    /**
     * Waits JPasswordField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JPasswordField instance.
     * @throws TimeoutExpiredException
     */
    public static JPasswordField waitJPasswordField(Container cont, ComponentChooser chooser) {
	return(waitJPasswordField(cont, chooser, 0));
    }

    /**
     * Waits JPasswordField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JPasswordField instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JPasswordField waitJPasswordField(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJPasswordField(cont,  
			      new JPasswordFieldFinder(new JTextComponentOperator.
                                                       JTextComponentByTextFinder(text, 
                                                                                  new DefaultStringComparator(ce, ccs))), 
			      index));
    }

    /**
     * Waits JPasswordField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JPasswordField instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JPasswordField waitJPasswordField(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJPasswordField(cont, text, ce, ccs, 0));
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(ECHO_CHAR_DPROP, 
		   new Character(((JPasswordField)getSource()).getEchoChar()).toString());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JPasswordField.echoCharIsSet()</code> through queue*/
    public boolean echoCharIsSet() {
	return(runMapping(new MapBooleanAction("echoCharIsSet") {
		public boolean map() {
		    return(((JPasswordField)getSource()).echoCharIsSet());
		}}));}

    /**Maps <code>JPasswordField.getEchoChar()</code> through queue*/
    public char getEchoChar() {
	return(runMapping(new MapCharacterAction("getEchoChar") {
		public char map() {
		    return(((JPasswordField)getSource()).getEchoChar());
		}}));}

    /**Maps <code>JPasswordField.getPassword()</code> through queue*/
    public char[] getPassword() {
	return((char[])runMapping(new MapAction("getPassword") {
		public Object map() {
		    return(((JPasswordField)getSource()).getPassword());
		}}));}

    /**Maps <code>JPasswordField.setEchoChar(char)</code> through queue*/
    public void setEchoChar(final char c) {
	runMapping(new MapVoidAction("setEchoChar") {
		public void map() {
		    ((JPasswordField)getSource()).setEchoChar(c);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Checks component type.
     */
    public static class JPasswordFieldFinder extends Finder {
        /**
         * Constructs JPasswordFieldFinder.
         * @param sf other searching criteria.
         */
	public JPasswordFieldFinder(ComponentChooser sf) {
            super(JPasswordField.class, sf);
	}
        /**
         * Constructs JPasswordFieldFinder.
         */
	public JPasswordFieldFinder() {
            super(JPasswordField.class);
	}
    }
}
