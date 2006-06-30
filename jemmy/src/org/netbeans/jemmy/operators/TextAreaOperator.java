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
import java.awt.TextArea;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.TextListener;

import java.util.Hashtable;

/**
 * This operator type covers java.awt.textArea component.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class TextAreaOperator extends TextComponentOperator
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
     * @param b The <code>java.awt.TextArea</code> managed by
     * this instance.
     */
    public TextAreaOperator(TextArea b) {
	super(b);
    }

    /**
     * Constructs a TextAreaOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public TextAreaOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((TextArea)cont.
             waitSubComponent(new TextAreaFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a TextAreaOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public TextAreaOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the <code>index+1</code>'th
     * <code>java.awt.TextArea</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for textArea.
     * @param text TextArea text. 
     * @param index Ordinal component index. The first component has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextAreaOperator(ContainerOperator cont, String text, int index) {
	this((TextArea)waitComponent(cont, 
					   new TextAreaByTextFinder(text, 
									   cont.getComparator()),
					   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the first
     * <code>java.awt.TextArea</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for textArea.
     * @param text TextArea text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextAreaOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textArea.
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextAreaOperator(ContainerOperator cont, int index) {
	this((TextArea)
	     waitComponent(cont, 
			   new TextAreaFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textArea.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextAreaOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches TextArea in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @param index Ordinal component index.  The first <code>index</code> is 0.
     * @return TextArea instance or null if component was not found.
     */
    public static TextArea findTextArea(Container cont, ComponentChooser chooser, int index) {
	return((TextArea)findComponent(cont, new TextAreaFinder(chooser), index));
    }

    /**
     * Searches for the first TextArea in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @return TextArea instance or null if component was not found.
     */
    public static TextArea findTextArea(Container cont, ComponentChooser chooser) {
	return(findTextArea(cont, chooser, 0));
    }

    /**
     * Searches TextArea by text.
     * @param cont Container to search component in.
     * @param text TextArea text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return TextArea instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextArea findTextArea(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findTextArea(cont, new TextAreaByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches TextArea by text.
     * @param cont Container to search component in.
     * @param text TextArea text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return TextArea instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextArea findTextArea(Container cont, String text, boolean ce, boolean ccs) {
	return(findTextArea(cont, text, ce, ccs, 0));
    }

    /**
     * Waits TextArea in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return TextArea instance.
     */
    public static TextArea waitTextArea(Container cont, ComponentChooser chooser, int index) {
	return((TextArea)waitComponent(cont, new TextAreaFinder(chooser), index));
    }

    /**
     * Waits 0'th TextArea in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return TextArea instance.
     */
    public static TextArea waitTextArea(Container cont, ComponentChooser chooser){
	return(waitTextArea(cont, chooser, 0));
    }

    /**
     * Waits TextArea by text.
     * @param cont Container to search component in.
     * @param text TextArea text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return TextArea instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextArea waitTextArea(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitTextArea(cont, new TextAreaByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits TextArea by text.
     * @param cont Container to search component in.
     * @param text TextArea text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return TextArea instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextArea waitTextArea(Container cont, String text, boolean ce, boolean ccs) {
	return(waitTextArea(cont, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("TextAreaOperator.PushKeyTimeout", PUSH_KEY_TIMEOUT);
	Timeouts.initDefault("TextAreaOperator.BetweenKeysTimeout", BETWEEN_KEYS_TIMEOUT);
	Timeouts.initDefault("TextAreaOperator.ChangeCaretPositionTimeout", CHANGE_CARET_POSITION_TIMEOUT);
	Timeouts.initDefault("TextAreaOperator.TypeTextTimeout", TYPE_TEXT_TIMEOUT);
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
	result.put(TEXT_DPROP, ((TextArea)getSource()).getText());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>TextArea.getColumns()</code> through queue*/
    public int getColumns() {
	return(runMapping(new MapIntegerAction("getColumns") {
		public int map() {
		    return(((TextArea)getSource()).getColumns());
		}}));}

    /**Maps <code>TextArea.getMinimumSize(int, int)</code> through queue*/
    public Dimension getMinimumSize(final int i, final int i1) {
	return((Dimension)runMapping(new MapAction("getMinimumSize") {
		public Object map() {
		    return(((TextArea)getSource()).getMinimumSize(i, i1));
		}}));}

    /**Maps <code>TextArea.getPreferredSize(int, int)</code> through queue*/
    public Dimension getPreferredSize(final int i, final int i1) {
	return((Dimension)runMapping(new MapAction("getPreferredSize") {
		public Object map() {
		    return(((TextArea)getSource()).getPreferredSize(i, i1));
		}}));}

    /**Maps <code>TextArea.getRows()</code> through queue*/
    public int getRows() {
	return(runMapping(new MapIntegerAction("getRows") {
		public int map() {
		    return(((TextArea)getSource()).getRows());
		}}));}

    /**Maps <code>TextArea.getScrollbarVisibility()</code> through queue*/
    public int getScrollbarVisibility() {
	return(runMapping(new MapIntegerAction("getScrollbarVisibility") {
		public int map() {
		    return(((TextArea)getSource()).getScrollbarVisibility());
		}}));}

    /**Maps <code>TextArea.replaceRange(String, int, int)</code> through queue*/
    public void replaceRange(final String string, final int i, final int i1) {
	runMapping(new MapVoidAction("replaceRange") {
		public void map() {
		    ((TextArea)getSource()).replaceRange(string, i, i1);
		}});}

    /**Maps <code>TextArea.setColumns(int)</code> through queue*/
    public void setColumns(final int i) {
	runMapping(new MapVoidAction("setColumns") {
		public void map() {
		    ((TextArea)getSource()).setColumns(i);
		}});}

    /**Maps <code>TextArea.setRows(int)</code> through queue*/
    public void setRows(final int i) {
	runMapping(new MapVoidAction("setRows") {
		public void map() {
		    ((TextArea)getSource()).setRows(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by text.
     */
    public static class TextAreaByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs TextAreaByTextFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public TextAreaByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs TextAreaByTextFinder.
         * @param lb a text pattern
         */
	public TextAreaByTextFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof TextArea) {
		if(((TextArea)comp).getText() != null) {
		    return(comparator.equals(((TextArea)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("TextArea with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class TextAreaFinder extends Finder {
        /**
         * Constructs TextAreaFinder.
         * @param sf other searching criteria.
         */
	public TextAreaFinder(ComponentChooser sf) {
            super(TextArea.class, sf);
	}
        /**
         * Constructs TextAreaFinder.
         */
	public TextAreaFinder() {
            super(TextArea.class);
	}
    }
}
