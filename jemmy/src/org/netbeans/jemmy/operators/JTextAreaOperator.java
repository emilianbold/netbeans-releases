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

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ActionProducer;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.util.Hashtable;

import javax.swing.JTextArea;

import javax.swing.text.BadLocationException;

/**
 *	
 * Class provides basic functions to operate with JTextArea
 * (selection, typing, deleting)
 *
 * <BR><BR>Timeouts used: <BR>
 * JTextComponentOperator.PushKeyTimeout - time between key pressing and releasing during text typing <BR>
 * JTextComponentOperator.BetweenKeysTimeout - time to sleep between two chars typing <BR>
 * JTextComponentOperator.ChangeCaretPositionTimeout - maximum time to chenge caret position <BR>
 * JTextComponentOperator.TypeTextTimeout - maximum time to type text <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitFocusTimeout - time to wait component focus <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JTextAreaOperator extends JTextComponentOperator
    implements Timeoutable, Outputable {

    private Timeouts timeouts;
    private TestOut output;
    private boolean pageNavigation = false;

    /**
     * Constructor.
     */
    public JTextAreaOperator(JTextArea b) {
	super(b);
    }

    public JTextAreaOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JTextArea)cont.
             waitSubComponent(new JTextAreaFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    public JTextAreaOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JTextAreaOperator(ContainerOperator cont, String text, int index) {
	this((JTextArea)
	     waitComponent(cont, 
			   new JTextAreaFinder(new JTextComponentOperator.
					       JTextComponentByTextFinder(text, 
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
    public JTextAreaOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JTextAreaOperator(ContainerOperator cont, int index) {
	this((JTextArea)
	     waitComponent(cont, 
			   new JTextAreaFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JTextAreaOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JTextArea in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return JTextArea instance or null if component was not found.
     */
    public static JTextArea findJTextArea(Container cont, ComponentChooser chooser, int index) {
	return((JTextArea)findJTextComponent(cont, new JTextAreaFinder(chooser), index));
    }

    /**
     * Searches JTextArea in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @return JTextArea instance or null if component was not found.
     */
    public static JTextArea findJTextArea(Container cont, ComponentChooser chooser) {
	return(findJTextArea(cont, chooser, 0));
    }

    /**
     * Searches JTextArea by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextArea instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextArea findJTextArea(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJTextArea(cont, 
			     new JTextAreaFinder(new JTextComponentOperator.
						 JTextComponentByTextFinder(text, 
									    new DefaultStringComparator(ce, ccs))), 
			     index));
    }

    /**
     * Searches JTextArea by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextArea instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextArea findJTextArea(Container cont, String text, boolean ce, boolean ccs) {
	return(findJTextArea(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JTextArea in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return JTextArea instance.
     * @throws TimeoutExpiredException
     */
    public static JTextArea waitJTextArea(Container cont, ComponentChooser chooser, int index) {
	return((JTextArea)waitJTextComponent(cont, new JTextAreaFinder(chooser), index));
    }

    /**
     * Waits JTextArea in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @return JTextArea instance.
     * @throws TimeoutExpiredException
     */
    public static JTextArea waitJTextArea(Container cont, ComponentChooser chooser) {
	return(waitJTextArea(cont, chooser, 0));
    }

    /**
     * Waits JTextArea by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextArea instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JTextArea waitJTextArea(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJTextArea(cont,  
			     new JTextAreaFinder(new JTextComponentOperator.
						 JTextComponentByTextFinder(text, 
									    new DefaultStringComparator(ce, ccs))), 
			     index));
    }

    /**
     * Waits JTextArea by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextArea instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JTextArea waitJTextArea(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJTextArea(cont, text, ce, ccs, 0));
    }

    /**
     * Defines current timeouts.
     * @param times A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts times) {
	timeouts = times;
	super.setTimeouts(timeouts);
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
     * Notifies whether "PageUp" and "PageDown" should be used
     * to change caret position. If can be useful if text takes 
     * some pages.
     */
    public void usePageNavigationKeys(boolean yesOrNo) {
	pageNavigation = yesOrNo;
    }

    /**
     * Moves caret to line.
     * @param row Line to move caret to.
     * @see JTextComponentOperator#changeCaretPosition(int)
     * @see #changeCaretPosition(int)
     * @see #changeCaretPosition(int, int)
     * @throws TimeoutExpiredException
     */
    public void changeCaretRow(int row) {
	changeCaretPosition(row, getCaretPosition() - 
			    getLineStartOffset(getLineOfOffset(getCaretPosition())));
    }

    /**
     * Moves caret.
     * @param row Line to move caret to.
     * @param column Column to move caret to.
     * @see JTextComponentOperator#changeCaretPosition(int)
     * @see #changeCaretRow(int)
     * @see #changeCaretPosition(int, int)
     * @throws TimeoutExpiredException
     */
    public void changeCaretPosition(int row, int column) {
	int startOffset = getLineStartOffset(row);
	int endOffset = getLineEndOffset(row);
	super.changeCaretPosition(getLineStartOffset(row) + 
				  ((column <= (endOffset - startOffset)) ?
				   column :
				   (endOffset - startOffset)));
    }

    /**
     * Types text.
     * @param row Line to type text in.
     * @param column Column to type text from.
     * @see JTextComponentOperator#typeText(String, int)
     * @throws TimeoutExpiredException
     */
    public void typeText(String text, int row, int column) {
	if(!hasFocus()) {
	    makeComponentVisible();
	}
	changeCaretPosition(row, column);
	typeText(text);
    }

    /**
     * Select a part of text.
     * @param startRow Start position row.
     * @param startColumn Start position column.
     * @param endRow End position row.
     * @param endColumn End position column.
     * @see JTextComponentOperator#selectText(int, int)
     * @see #selectLines(int, int)
     * @throws TimeoutExpiredException
     */
    public void selectText(int startRow, int startColumn, 
			   int endRow, int endColumn) {
	int startPos = 0;
	try {
	    startPos = getLineStartOffset(startRow) + startColumn;
	} catch(JemmyException e) {
	    if(!(e.getInnerException() instanceof BadLocationException)) {
		throw(e);
	    }
	}
	int endPos = getText().length();
	try {
	    endPos = getLineStartOffset(endRow) + endColumn;
	} catch(JemmyException e) {
	    if(!(e.getInnerException() instanceof BadLocationException)) {
		throw(e);
	    }
	}
	selectText(startPos, endPos);
    }

    /**
     * Select some text lines.
     * @param startLine
     * @param endLine
     * @see JTextComponentOperator#selectText(int, int)
     * @see #selectText(int, int, int, int)
     * @throws TimeoutExpiredException
     */
    public void selectLines(int startLine, int endLine) {
	if(!hasFocus()) {
	    makeComponentVisible();
	}
	selectText(startLine, 0, endLine + 1, 0);
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Row count", Integer.toString(((JTextArea)getSource()).getRows()));
	result.put("Column count", Integer.toString(((JTextArea)getSource()).getColumns()));
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JTextArea.append(String)</code> through queue*/
    public void append(final String string) {
	runMapping(new MapVoidAction("append") {
		public void map() {
		    ((JTextArea)getSource()).append(string);
		}});}

    /**Maps <code>JTextArea.getColumns()</code> through queue*/
    public int getColumns() {
	return(runMapping(new MapIntegerAction("getColumns") {
		public int map() {
		    return(((JTextArea)getSource()).getColumns());
		}}));}

    /**Maps <code>JTextArea.getLineCount()</code> through queue*/
    public int getLineCount() {
	return(runMapping(new MapIntegerAction("getLineCount") {
		public int map() {
		    return(((JTextArea)getSource()).getLineCount());
		}}));}

    /**Maps <code>JTextArea.getLineEndOffset(int)</code> through queue*/
    public int getLineEndOffset(final int i) {
	return(runMapping(new MapIntegerAction("getLineEndOffset") {
		public int map() throws BadLocationException {
		    return(((JTextArea)getSource()).getLineEndOffset(i));
		}}));}

    /**Maps <code>JTextArea.getLineOfOffset(int)</code> through queue*/
    public int getLineOfOffset(final int i) {
	return(runMapping(new MapIntegerAction("getLineOfOffset") {
		public int map() throws BadLocationException {
		    return(((JTextArea)getSource()).getLineOfOffset(i));
		}}));}

    /**Maps <code>JTextArea.getLineStartOffset(int)</code> through queue*/
    public int getLineStartOffset(final int i) {
	return(runMapping(new MapIntegerAction("getLineStartOffset") {
		public int map() throws BadLocationException {
		    return(((JTextArea)getSource()).getLineStartOffset(i));
		}}));}

    /**Maps <code>JTextArea.getLineWrap()</code> through queue*/
    public boolean getLineWrap() {
	return(runMapping(new MapBooleanAction("getLineWrap") {
		public boolean map() {
		    return(((JTextArea)getSource()).getLineWrap());
		}}));}

    /**Maps <code>JTextArea.getRows()</code> through queue*/
    public int getRows() {
	return(runMapping(new MapIntegerAction("getRows") {
		public int map() {
		    return(((JTextArea)getSource()).getRows());
		}}));}

    /**Maps <code>JTextArea.getTabSize()</code> through queue*/
    public int getTabSize() {
	return(runMapping(new MapIntegerAction("getTabSize") {
		public int map() {
		    return(((JTextArea)getSource()).getTabSize());
		}}));}

    /**Maps <code>JTextArea.getWrapStyleWord()</code> through queue*/
    public boolean getWrapStyleWord() {
	return(runMapping(new MapBooleanAction("getWrapStyleWord") {
		public boolean map() {
		    return(((JTextArea)getSource()).getWrapStyleWord());
		}}));}

    /**Maps <code>JTextArea.insert(String, int)</code> through queue*/
    public void insert(final String string, final int i) {
	runMapping(new MapVoidAction("insert") {
		public void map() {
		    ((JTextArea)getSource()).insert(string, i);
		}});}

    /**Maps <code>JTextArea.replaceRange(String, int, int)</code> through queue*/
    public void replaceRange(final String string, final int i, final int i1) {
	runMapping(new MapVoidAction("replaceRange") {
		public void map() {
		    ((JTextArea)getSource()).replaceRange(string, i, i1);
		}});}

    /**Maps <code>JTextArea.setColumns(int)</code> through queue*/
    public void setColumns(final int i) {
	runMapping(new MapVoidAction("setColumns") {
		public void map() {
		    ((JTextArea)getSource()).setColumns(i);
		}});}

    /**Maps <code>JTextArea.setLineWrap(boolean)</code> through queue*/
    public void setLineWrap(final boolean b) {
	runMapping(new MapVoidAction("setLineWrap") {
		public void map() {
		    ((JTextArea)getSource()).setLineWrap(b);
		}});}

    /**Maps <code>JTextArea.setRows(int)</code> through queue*/
    public void setRows(final int i) {
	runMapping(new MapVoidAction("setRows") {
		public void map() {
		    ((JTextArea)getSource()).setRows(i);
		}});}

    /**Maps <code>JTextArea.setTabSize(int)</code> through queue*/
    public void setTabSize(final int i) {
	runMapping(new MapVoidAction("setTabSize") {
		public void map() {
		    ((JTextArea)getSource()).setTabSize(i);
		}});}

    /**Maps <code>JTextArea.setWrapStyleWord(boolean)</code> through queue*/
    public void setWrapStyleWord(final boolean b) {
	runMapping(new MapVoidAction("setWrapStyleWord") {
		public void map() {
		    ((JTextArea)getSource()).setWrapStyleWord(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    public static class JTextAreaFinder extends Finder {
	public JTextAreaFinder(ComponentChooser sf) {
            super(JTextArea.class, sf);
	}
	public JTextAreaFinder() {
            super(JTextArea.class);
	}
    }
}
