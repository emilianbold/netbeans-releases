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

import org.netbeans.jemmy.ActionProducer;
import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextField;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.TextListener;

import java.util.Hashtable;

/**
 *
 * This operator type covers java.awt.TextField component.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */
public class TextFieldOperator extends TextComponentOperator
    implements Timeoutable, Outputable {

    /**
     * Identifier for a "text" property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Text";

    private final static long PUSH_KEY_TIMEOUT = 0;
    private final static long BETWEEN_KEYS_TIMEOUT = 0;
    private final static long CHANGE_CARET_POSITION_TIMEOUT = 60000;
    private final static long TYPE_TEXT_TIMEOUT = 60000;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Constructor.
     * @param b The <code>java.awt.TextField</code> managed by
     * this instance.
     */
    public TextFieldOperator(TextField b) {
	super(b);
    }

    /**
     * Constructs a TextFieldOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public TextFieldOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((TextField)cont.
             waitSubComponent(new TextFieldFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a TextFieldOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public TextFieldOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the <code>index+1</code>'th
     * <code>java.awt.TextField</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for textField.
     * @param text TextField text. 
     * @param index Ordinal component index. The first component has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextFieldOperator(ContainerOperator cont, String text, int index) {
	this((TextField)waitComponent(cont, 
					   new TextFieldByTextFinder(text, 
									   cont.getComparator()),
					   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the first
     * <code>java.awt.TextField</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for textField.
     * @param text TextField text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextFieldOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textField.
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextFieldOperator(ContainerOperator cont, int index) {
	this((TextField)
	     waitComponent(cont, 
			   new TextFieldFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textField.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextFieldOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches TextField in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @param index Ordinal component index.  The first <code>index</code> is 0.
     * @return TextField instance or null if component was not found.
     */
    public static TextField findTextField(Container cont, ComponentChooser chooser, int index) {
	return((TextField)findComponent(cont, new TextFieldFinder(chooser), index));
    }

    /**
     * Searches for the first TextField in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @return TextField instance or null if component was not found.
     */
    public static TextField findTextField(Container cont, ComponentChooser chooser) {
	return(findTextField(cont, chooser, 0));
    }

    /**
     * Searches TextField by text.
     * @param cont Container to search component in.
     * @param text TextField text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return TextField instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextField findTextField(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findTextField(cont, new TextFieldByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches TextField by text.
     * @param cont Container to search component in.
     * @param text TextField text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return TextField instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextField findTextField(Container cont, String text, boolean ce, boolean ccs) {
	return(findTextField(cont, text, ce, ccs, 0));
    }

    /**
     * Waits TextField in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return TextField instance.
     */
    public static TextField waitTextField(Container cont, ComponentChooser chooser, int index) {
	return((TextField)waitComponent(cont, new TextFieldFinder(chooser), index));
    }

    /**
     * Waits 0'th TextField in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return TextField instance.
     */
    public static TextField waitTextField(Container cont, ComponentChooser chooser){
	return(waitTextField(cont, chooser, 0));
    }

    /**
     * Waits TextField by text.
     * @param cont Container to search component in.
     * @param text TextField text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return TextField instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextField waitTextField(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitTextField(cont, new TextFieldByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits TextField by text.
     * @param cont Container to search component in.
     * @param text TextField text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return TextField instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextField waitTextField(Container cont, String text, boolean ce, boolean ccs) {
	return(waitTextField(cont, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("TextFieldOperator.PushKeyTimeout", PUSH_KEY_TIMEOUT);
	Timeouts.initDefault("TextFieldOperator.BetweenKeysTimeout", BETWEEN_KEYS_TIMEOUT);
	Timeouts.initDefault("TextFieldOperator.ChangeCaretPositionTimeout", CHANGE_CARET_POSITION_TIMEOUT);
	Timeouts.initDefault("TextFieldOperator.TypeTextTimeout", TYPE_TEXT_TIMEOUT);
    }

    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
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

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(TEXT_DPROP, ((TextField)getSource()).getText());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>TextField.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((TextField)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>TextField.echoCharIsSet()</code> through queue*/
    public boolean echoCharIsSet() {
	return(runMapping(new MapBooleanAction("echoCharIsSet") {
		public boolean map() {
		    return(((TextField)getSource()).echoCharIsSet());
		}}));}

    /**Maps <code>TextField.getColumns()</code> through queue*/
    public int getColumns() {
	return(runMapping(new MapIntegerAction("getColumns") {
		public int map() {
		    return(((TextField)getSource()).getColumns());
		}}));}

    /**Maps <code>TextField.getEchoChar()</code> through queue*/
    public char getEchoChar() {
	return(runMapping(new MapCharacterAction("getEchoChar") {
		public char map() {
		    return(((TextField)getSource()).getEchoChar());
		}}));}

    /**Maps <code>TextField.getMinimumSize(int)</code> through queue*/
    public Dimension getMinimumSize(final int i) {
	return((Dimension)runMapping(new MapAction("getMinimumSize") {
		public Object map() {
		    return(((TextField)getSource()).getMinimumSize(i));
		}}));}

    /**Maps <code>TextField.getPreferredSize(int)</code> through queue*/
    public Dimension getPreferredSize(final int i) {
	return((Dimension)runMapping(new MapAction("getPreferredSize") {
		public Object map() {
		    return(((TextField)getSource()).getPreferredSize(i));
		}}));}

    /**Maps <code>TextField.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((TextField)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>TextField.setColumns(int)</code> through queue*/
    public void setColumns(final int i) {
	runMapping(new MapVoidAction("setColumns") {
		public void map() {
		    ((TextField)getSource()).setColumns(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by text.
     */
    public static class TextFieldByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs TextFieldByTextFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public TextFieldByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs TextFieldByTextFinder.
         * @param lb a text pattern
         */
	public TextFieldByTextFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof TextField) {
		if(((TextField)comp).getText() != null) {
		    return(comparator.equals(((TextField)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("TextField with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class TextFieldFinder extends Finder {
        /**
         * Constructs TextFieldFinder.
         * @param sf other searching criteria.
         */
	public TextFieldFinder(ComponentChooser sf) {
            super(TextField.class, sf);
	}
        /**
         * Constructs TextFieldFinder.
         */
	public TextFieldFinder() {
            super(TextField.class);
	}
    }
}
