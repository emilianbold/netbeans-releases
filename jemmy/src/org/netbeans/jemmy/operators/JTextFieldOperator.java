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

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JTextField;

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
public class JTextFieldOperator extends JTextComponentOperator{

    /**
     * Constructor.
     * @param b a component
     */
    public JTextFieldOperator(JTextField b) {
	super(b);
    }

    /**
     * Constructs a JTextFieldOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JTextFieldOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JTextField)cont.
             waitSubComponent(new JTextFieldFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JTextFieldOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JTextFieldOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JTextFieldOperator(ContainerOperator cont, String text, int index) {
	this((JTextField)
	     waitComponent(cont, 
			   new JTextFieldFinder(new JTextComponentOperator.
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
    public JTextFieldOperator(ContainerOperator cont, String text) {
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
    public JTextFieldOperator(ContainerOperator cont, int index) {
	this((JTextField)
	     waitComponent(cont, 
			   new JTextFieldFinder(),
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
    public JTextFieldOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JTextField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JTextField instance or null if component was not found.
     */
    public static JTextField findJTextField(Container cont, ComponentChooser chooser, int index) {
	return((JTextField)findJTextComponent(cont, new JTextFieldFinder(chooser), index));
    }

    /**
     * Searches JTextField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JTextField instance or null if component was not found.
     */
    public static JTextField findJTextField(Container cont, ComponentChooser chooser) {
	return(findJTextField(cont, chooser, 0));
    }

    /**
     * Searches JTextField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextField instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextField findJTextField(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJTextField(cont, 
			      new JTextFieldFinder(new JTextComponentOperator.
						   JTextComponentByTextFinder(text, 
									      new DefaultStringComparator(ce, ccs))), 
			      index));
    }

    /**
     * Searches JTextField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextField instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextField findJTextField(Container cont, String text, boolean ce, boolean ccs) {
	return(findJTextField(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JTextField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JTextField instance.
     * @throws TimeoutExpiredException
     */
    public static JTextField waitJTextField(Container cont, ComponentChooser chooser, int index){
	return((JTextField)waitJTextComponent(cont, new JTextFieldFinder(chooser), index));
    }

    /**
     * Waits JTextField in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JTextField instance.
     * @throws TimeoutExpiredException
     */
    public static JTextField waitJTextField(Container cont, ComponentChooser chooser) {
	return(waitJTextField(cont, chooser, 0));
    }

    /**
     * Waits JTextField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextField instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JTextField waitJTextField(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJTextField(cont,  
			      new JTextFieldFinder(new JTextComponentOperator.
						   JTextComponentByTextFinder(text, 
									      new DefaultStringComparator(ce, ccs))), 
			      index));
    }

    /**
     * Waits JTextField by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextField instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JTextField waitJTextField(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJTextField(cont, text, ce, ccs, 0));
    }

    /**
     * Wait some text to be displayed starting from certain position.
     * @param text a text to wait.
     * @param position start position.
     */
    public void waitText(String text, int position) {
	super.waitText(removeNewLines(text), position);
    }

    /**
     * Wait some text to be displayed.
     * @param text a text to wait.
     */
    public void waitText(String text) {
	super.waitText(removeNewLines(text));
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JTextField.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((JTextField)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>JTextField.getColumns()</code> through queue*/
    public int getColumns() {
	return(runMapping(new MapIntegerAction("getColumns") {
		public int map() {
		    return(((JTextField)getSource()).getColumns());
		}}));}

    /**Maps <code>JTextField.getHorizontalAlignment()</code> through queue*/
    public int getHorizontalAlignment() {
	return(runMapping(new MapIntegerAction("getHorizontalAlignment") {
		public int map() {
		    return(((JTextField)getSource()).getHorizontalAlignment());
		}}));}

    /**Maps <code>JTextField.getHorizontalVisibility()</code> through queue*/
    public BoundedRangeModel getHorizontalVisibility() {
	return((BoundedRangeModel)runMapping(new MapAction("getHorizontalVisibility") {
		public Object map() {
		    return(((JTextField)getSource()).getHorizontalVisibility());
		}}));}

    /**Maps <code>JTextField.getScrollOffset()</code> through queue*/
    public int getScrollOffset() {
	return(runMapping(new MapIntegerAction("getScrollOffset") {
		public int map() {
		    return(((JTextField)getSource()).getScrollOffset());
		}}));}

    /**Maps <code>JTextField.postActionEvent()</code> through queue*/
    public void postActionEvent() {
	runMapping(new MapVoidAction("postActionEvent") {
		public void map() {
		    ((JTextField)getSource()).postActionEvent();
		}});}

    /**Maps <code>JTextField.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((JTextField)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>JTextField.setActionCommand(String)</code> through queue*/
    public void setActionCommand(final String string) {
	runMapping(new MapVoidAction("setActionCommand") {
		public void map() {
		    ((JTextField)getSource()).setActionCommand(string);
		}});}

    /**Maps <code>JTextField.setColumns(int)</code> through queue*/
    public void setColumns(final int i) {
	runMapping(new MapVoidAction("setColumns") {
		public void map() {
		    ((JTextField)getSource()).setColumns(i);
		}});}

    /**Maps <code>JTextField.setHorizontalAlignment(int)</code> through queue*/
    public void setHorizontalAlignment(final int i) {
	runMapping(new MapVoidAction("setHorizontalAlignment") {
		public void map() {
		    ((JTextField)getSource()).setHorizontalAlignment(i);
		}});}

    /**Maps <code>JTextField.setScrollOffset(int)</code> through queue*/
    public void setScrollOffset(final int i) {
	runMapping(new MapVoidAction("setScrollOffset") {
		public void map() {
		    ((JTextField)getSource()).setScrollOffset(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private String removeNewLines(String text) {
	StringBuffer buff = new StringBuffer(text);
	int i = 0;
	while(i < buff.length()) {
	    if(buff.charAt(i) != '\n') {
		i++;
	    } else {
		buff.deleteCharAt(i);
	    }
	}
	return(buff.toString());
    }

    /**
     * Checks component type.
     */
    public static class JTextFieldFinder extends Finder {
        /**
         * Constructs JTextFieldFinder.
         * @param sf other searching criteria.
         */
	public JTextFieldFinder(ComponentChooser sf) {
            super(JTextField.class, sf);
	}
        /**
         * Constructs JTextFieldFinder.
         */
	public JTextFieldFinder() {
            super(JTextField.class);
	}
    }
}
