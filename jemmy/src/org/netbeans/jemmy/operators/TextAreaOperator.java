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
 *
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class TextAreaOperator extends TextComponentOperator
    implements Timeoutable, Outputable {

    private final static long PUSH_KEY_TIMEOUT = 0;
    private final static long BETWEEN_KEYS_TIMEOUT = 0;
    private final static long CHANGE_CARET_POSITION_TIMEOUT = 10000;
    private final static long TYPE_TEXT_TIMEOUT = 30000;

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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
     */
    public TextAreaOperator(ContainerOperator cont, int index) {
	this((TextArea)
	     waitComponent(cont, 
			   new TextAreaFinder(ComponentSearcher.
						    getTrueChooser("Any TextArea")),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textArea.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
     */
    public static TextArea waitTextArea(Container cont, ComponentChooser chooser, int index) {
	return((TextArea)waitComponent(cont, new TextAreaFinder(chooser), index));
    }

    /**
     * Waits 0'th TextArea in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return TextArea instance.
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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

    /**
     * Defines current timeouts.
     * @param timeouts A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
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

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Text", ((TextArea)getSource()).getText());
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

    protected static class TextAreaByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public TextAreaByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
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

    static class TextAreaFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public TextAreaFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof TextArea) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
