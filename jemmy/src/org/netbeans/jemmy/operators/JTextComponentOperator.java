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
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.util.EmptyVisualizer;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.TextDriver;

import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.util.Hashtable;

import javax.swing.JScrollPane;

import javax.swing.event.CaretListener;

import javax.swing.plaf.TextUI;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

/**
 *	
 * Class provides basic functions to operate with JTextComponent
 * (selection, typing, deleting)
 *
 * <BR><BR>Timeouts used: <BR>
 * JTextComponentOperator.PushKeyTimeout - time between key pressing and releasing during text typing <BR>
 * JTextComponentOperator.BetweenKeysTimeout - time to sleep between two chars typing <BR>
 * JTextComponentOperator.ChangeCaretPositionTimeout - maximum time to chenge caret position <BR>
 * JTextComponentOperator.TypeTextTimeout - maximum time to type text <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitFocusTimeout - time to wait component focus <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for text <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JTextComponentOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    /**
     * Identifier for a "text" property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Text";

    /**
     * Identifier for a "selected text" property.
     * @see #getDump
     */
    public static final String SELECTED_TEXT_DPROP = "Selected text";

    /**
     * Identifier for a "editable" property.
     * @see #getDump
     */
    public static final String IS_EDITABLE_DPROP = "Editable";

    private final static long PUSH_KEY_TIMEOUT = 0;
    private final static long BETWEEN_KEYS_TIMEOUT = 0;
    private final static long CHANGE_CARET_POSITION_TIMEOUT = 60000;
    private final static long TYPE_TEXT_TIMEOUT = 60000;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Notifies what modifiers are pressed.
     * @deprecated All text operations are performed by TextDriver regitered for this operator type.
     */
    protected int modifiersPressed = 0;

    private TextDriver driver;

    /**
     * Constructor.
     * @param b Component to operate with.
     */
    public JTextComponentOperator(JTextComponent b) {
	super(b);
	driver = DriverManager.getTextDriver(getClass());
    }

    /**
     * Constructs a JTextComponentOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JTextComponentOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JTextComponent)cont.
             waitSubComponent(new JTextComponentFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JTextComponentOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JTextComponentOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JTextComponentOperator(ContainerOperator cont, String text, int index) {
	this((JTextComponent)waitComponent(cont, 
					   new JTextComponentByTextFinder(text, 
									  cont.getComparator()),
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
    public JTextComponentOperator(ContainerOperator cont, String text) {
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
    public JTextComponentOperator(ContainerOperator cont, int index) {
	this((JTextComponent)
	     waitComponent(cont, 
			   new JTextComponentFinder(),
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
    public JTextComponentOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    static {
	Timeouts.initDefault("JTextComponentOperator.PushKeyTimeout", PUSH_KEY_TIMEOUT);
	Timeouts.initDefault("JTextComponentOperator.BetweenKeysTimeout", BETWEEN_KEYS_TIMEOUT);
	Timeouts.initDefault("JTextComponentOperator.ChangeCaretPositionTimeout", CHANGE_CARET_POSITION_TIMEOUT);
	Timeouts.initDefault("JTextComponentOperator.TypeTextTimeout", TYPE_TEXT_TIMEOUT);
    }

    /**
     * Searches JTextComponent in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JTextComponent instance or null if component was not found.
     */
    public static JTextComponent findJTextComponent(Container cont, ComponentChooser chooser, int index) {
	return((JTextComponent)findComponent(cont, new JTextComponentFinder(chooser), index));
    }

    /**
     * Searches JTextComponent in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JTextComponent instance or null if component was not found.
     */
    public static JTextComponent findJTextComponent(Container cont, ComponentChooser chooser) {
	return(findJTextComponent(cont, chooser, 0));
    }

    /**
     * Searches JTextComponent by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextComponent findJTextComponent(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJTextComponent(cont, new JTextComponentByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches JTextComponent by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextComponent findJTextComponent(Container cont, String text, boolean ce, boolean ccs) {
	return(findJTextComponent(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JTextComponent in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JTextComponent instance.
     * @throws TimeoutExpiredException
     */
    public static JTextComponent waitJTextComponent(final Container cont, final ComponentChooser chooser, final int index) {
	return((JTextComponent)waitComponent(cont, new JTextComponentFinder(chooser), index));
    }

    /**
     * Waits JTextComponent in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JTextComponent instance.
     * @throws TimeoutExpiredException
     */
    public static JTextComponent waitJTextComponent(Container cont, ComponentChooser chooser) {
	return(waitJTextComponent(cont, chooser, 0));
    }

    /**
     * Waits JTextComponent by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextComponent instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JTextComponent waitJTextComponent(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJTextComponent(cont, new JTextComponentByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits JTextComponent by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextComponent instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JTextComponent waitJTextComponent(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJTextComponent(cont, text, ce, ccs, 0));
    }

    public void setTimeouts(Timeouts times) {
	timeouts = times;
	timeouts.setTimeout("ComponentOperator.PushKeyTimeout", 
			    timeouts.getTimeout("JTextComponentOperator.PushKeyTimeout"));
	super.setTimeouts(timeouts);
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

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (TextDriver)DriverManager.
	    getDriver(DriverManager.TEXT_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Finds start text position.
     * @param text Text to be searched.
     * @param tChooser Additional search criteria.
     * @param index Index of text instance (first instance has index 0)
     * @return Caret position correspondent to text start.
     * @see JTextComponentOperator.TextChooser
     */
    public int getPositionByText(String text, TextChooser tChooser, int index) {
	output.printLine("Find " + tChooser.getDescription() + "\"" + text + 
			 "\" text in text component\n    : " +
			 toStringSource());
	output.printGolden("Find " + tChooser.getDescription() + "\"" + text + 
			   "\" text in text component");
	String allText = getDisplayedText();
	Document doc = getDocument();
	int position = 0;
	int ind = 0;
	while((position = allText.indexOf(text, position)) >= 0) {
	    if(tChooser.checkPosition(doc, position)) {
		if(ind == index) {
		    return(position);
		} else {
		    ind++;
		}
	    }
	    position = position + text.length();
	}
	return(-1);
    }

    /**
     * Finds start text position.
     * @param text Text to be searched.
     * @param tChooser Additional search criteria.
     * @return Caret position correspondent to text start.
     */
    public int getPositionByText(String text, TextChooser tChooser) {
	return(getPositionByText(text, tChooser, 0));
    }

    /**
     * Finds start text position.
     * @param text Text to be searched.
     * @param index Index of text instance (first instance has index 0)
     * @return Caret position correspondent to text start.
     */
    public int getPositionByText(String text, int index) {
	return(getPositionByText(text, new TextChooser() {
		public boolean checkPosition(Document doc, int offset) {
		    return(true);
		}
		public String getDescription() {
		    return("any");
		}
	    }, index));
    }

    /**
     * Finds start text position.
     * @param text Text to be searched.
     * @return Caret position correspondent to text start.
     */
    public int getPositionByText(String text) {
	return(getPositionByText(text, 0));
    }

    /**
     * Requests a focus, clears text, types new one and pushes Enter.
     * @param text New text value. Shouln't include final '\n'.
     * @throws TimeoutExpiredException
     */
    public void enterText(final String text) {
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.enterText(JTextComponentOperator.this, text);
		    return(null);
		}
		public String getDescription() {
		    return("Text entering");
		}
	    }, getTimeouts().getTimeout("JTextComponentOperator.TypeTextTimeout"));
    }

    /**
     * Changes caret position.
     * @param position Position to move caret to.
     * @see #changeCaretPosition(String, int, boolean)
     * @throws TimeoutExpiredException
     */
    public void changeCaretPosition(final int position) {
	output.printLine("Change caret position to " + Integer.toString(position));
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.changeCaretPosition(JTextComponentOperator.this, position);
		    return(null);
		}
		public String getDescription() {
		    return("Caret moving");
		}
	    }, getTimeouts().getTimeout("JTextComponentOperator.ChangeCaretPositionTimeout"));
	if(getVerification()) {
	    waitCaretPosition(position);
	}
    }

    /**
     * Puts caret before or after text.
     * @param text Text to be searched.
     * @param index Index of text instance (first instance has index 0)
     * @param before If true put caret before text, otherwise after.
     * @see #changeCaretPosition(int)
     * @see #getPositionByText(String, int)
     * @throws TimeoutExpiredException
     * @throws NoSuchTextException
     */
    public void changeCaretPosition(String text, int index, boolean before) {
	output.printLine("Put caret " + 
			 (before ? "before" : "after") + " " +
			 Integer.toString(index) + "'th instance of \"" +
			 text + "\" text");
	makeComponentVisible();
	int offset = getPositionByText(text, index);
	if(offset == -1) {
	    throw(new NoSuchTextException(text));
	}
	offset = before ? offset : offset + text.length();
	changeCaretPosition(offset);
    }

    /**
     * Puts caret before or after text.
     * @param text Text to be searched.
     * @param before If true put caret before text, otherwise after.
     * @see #changeCaretPosition(int)
     * @see #getPositionByText(String, int)
     * @throws TimeoutExpiredException
     * @throws NoSuchTextException
     */
    public void changeCaretPosition(String text, boolean before) {
	changeCaretPosition(text, 0, before);
    }

    /**
     * Types text starting from known position.
     * If verification mode is on, checks that right text has been typed
     * and caret has been moved to right position.
     * @param text Text to be typed.
     * @param caretPosition Position to start type text
     * @see #typeText(String)
     * @throws TimeoutExpiredException
     * @throws NoSuchTextException
     */
    public void typeText(final String text, final int caretPosition) {
	output.printLine("Typing text \"" + text + "\" from " +
			 Integer.toString(caretPosition) + " position " +
			 "in text component\n    : " +
			 toStringSource());
	output.printGolden("Typing text \"" + text + "\" in text component");
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.typeText(JTextComponentOperator.this, text, caretPosition);
		    return(null);
		}
		public String getDescription() {
		    return("Text typing");
		}
	    }, getTimeouts().getTimeout("JTextComponentOperator.TypeTextTimeout"));
	if(getVerification()) {
	    waitText(text, -1);
	}
    }

    /**
     * Types text starting from the current position.
     * @param text Text to be typed.
     * @see #typeText(String, int)
     * @throws TimeoutExpiredException
     */
    public void typeText(String text) {
	typeText(text, getCaretPosition());
    }

    /**
     * Selects a part of text.
     * @param startPosition Start caret position
     * @param finalPosition Final caret position
     * @see #selectText(String, int)
     * @see #selectText(String)
     * @throws TimeoutExpiredException
     */
    public void selectText(final int startPosition, final int finalPosition) {
	output.printLine("Select text from " +
			 Integer.toString(startPosition) + " to " +
			 Integer.toString(finalPosition) + 
			 " in text component\n    : " +
			 toStringSource());
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.selectText(JTextComponentOperator.this, startPosition, finalPosition);
		    return(null);
		}
		public String getDescription() {
		    return("Text selecting");
		}
	    }, getTimeouts().getTimeout("JTextComponentOperator.TypeTextTimeout"));
    }

    /**
     * Selects a part of text.
     * @param text Text to be selected
     * @param index Index of text instance (first instance has index 0)
     * @see #selectText(int, int)
     * @see #selectText(String)
     * @see #getPositionByText(String, int)
     * @throws TimeoutExpiredException
     * @throws NoSuchTextException
     */
    public void selectText(String text, int index) {
	output.printLine("Select " +
			 Integer.toString(index) + "'th instance of \"" +
			 text + "\" text in component\n    : " +
			 toStringSource());
	makeComponentVisible();
	int start = getPositionByText(text, index);
	if(start == -1) {
	    throw(new NoSuchTextException(text));
	}
	selectText(start, start + text.length());
    }

    /**
     * Selects a part of text.
     * @param text Text to be selected
     * @see #selectText(String, int)
     * @see #selectText(int, int)
     * @throws TimeoutExpiredException
     * @throws NoSuchTextException
     */
    public void selectText(String text) {
	selectText(text, 0);
    }

    /**
     * Clears text.
     * @throws TimeoutExpiredException
     */
    public void clearText() {
	output.printLine("Clearing text in text component\n    : " +
			 toStringSource());
	output.printGolden("Clearing text in text component");
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.clearText(JTextComponentOperator.this);
		    return(null);
		}
		public String getDescription() {
		    return("Text clearing");
		}
	    }, getTimeouts().getTimeout("JTextComponentOperator.TypeTextTimeout"));
    }

    /**
     * Scrolls to a text poistion.
     * @param position a position to scroll.
     * @throws TimeoutExpiredException
     */
    public void scrollToPosition(int position) {
	output.printTrace("Scroll JTextComponent to " + Integer.toString(position) + " position\n    : " +
			  toStringSource());
	output.printGolden("Scroll JTextComponent to " + Integer.toString(position) + " position");
	makeComponentVisible();
	//try to find JScrollPane under.
	JScrollPane scroll = (JScrollPane)getContainer(new JScrollPaneOperator.
						       JScrollPaneFinder(ComponentSearcher.
									 getTrueChooser("JScrollPane")));
	if(scroll == null) {
	    return;
	}
	JScrollPaneOperator scroller = new JScrollPaneOperator(scroll);
	scroller.copyEnvironment(this);
	scroller.setVisualizer(new EmptyVisualizer());
	Rectangle rect = modelToView(position);
	scroller.scrollToComponentRectangle(getSource(), 
					    (int)rect.getX(),
					    (int)rect.getY(),
					    (int)rect.getWidth(),
					    (int)rect.getHeight());
    }

    /**
     * Returns text which is really displayed.
     * Results returned by <code>getText()</code> and <code>getDisplayedText()</code>
     * are different if text component is used to display <code>javax.swing.text.StyledDocument</code>
     * @return the text which is displayed.
     */
    public String getDisplayedText() {
	try {
	    Document doc = getDocument();
	    return(doc.getText(0, doc.getLength()));
	} catch(BadLocationException e) {
	    throw(new JemmyException("Exception during text operation with\n    : " +
				     toStringSource(), e));
	}
    }

    /**
     * Wait for text to be displayed starting from certain position.
     * @param text text to wait.
     * @param position starting text position.
     */
    public void waitText(final String text, final int position) {
	getOutput().printLine("Wait \"" + text + "\" text starting from " +
			      Integer.toString(position) + " position in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait \"" + text + "\" text starting from " +
				Integer.toString(position) + " position");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    String alltext = getDisplayedText();
		    if(position >= 0) {
			if(position + text.length() <= alltext.length()) {
			    return(alltext.substring(position, position + text.length()).
				   equals(text));
			} else {
			    return(false);
			}
		    } else {
			return(alltext.indexOf(text) >= 0);
		    }
		}
		public String getDescription() {
		    return("Has \"" + text + "\" text starting from " +
			   Integer.toString(position) + " position");
		}
	    });
    }

    /**
     * Waits for certain text.
     * @param text Text to be compared by getComparator() comparator.
     */
    public void waitText(String text) {
	getOutput().printLine("Wait \"" + text + "\" text in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait \"" + text + "\" text");
	waitState(new JTextComponentByTextFinder(text, getComparator()));
    }

    /**
     * Wait for caret to be moved to certain position.
     * @param position a position which caret supposed to be moved to.
     */
    public void waitCaretPosition(final int position) {
	getOutput().printLine("Wait caret to be at \"" + Integer.toString(position) + 
			      " position in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait caret to be at \"" + Integer.toString(position) + 
				" position");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(getCaretPosition() == position);
		}
		public String getDescription() {
		    return("Has caret at " + Integer.toString(position) + " position");
		}
	    });
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(TEXT_DPROP, ((JTextComponent)getSource()).getText());
	if(((JTextComponent)getSource()).getSelectedText() != null &&
	   !((JTextComponent)getSource()).getSelectedText().equals("")) {
	    result.put(SELECTED_TEXT_DPROP, ((JTextComponent)getSource()).getSelectedText());
	}
	result.put(IS_EDITABLE_DPROP, ((JTextComponent)getSource()).isEditable() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JTextComponent.addCaretListener(CaretListener)</code> through queue*/
    public void addCaretListener(final CaretListener caretListener) {
	runMapping(new MapVoidAction("addCaretListener") {
		public void map() {
		    ((JTextComponent)getSource()).addCaretListener(caretListener);
		}});}

    /**Maps <code>JTextComponent.copy()</code> through queue*/
    public void copy() {
	runMapping(new MapVoidAction("copy") {
		public void map() {
		    ((JTextComponent)getSource()).copy();
		}});}

    /**Maps <code>JTextComponent.cut()</code> through queue*/
    public void cut() {
	runMapping(new MapVoidAction("cut") {
		public void map() {
		    ((JTextComponent)getSource()).cut();
		}});}

    /**Maps <code>JTextComponent.getActions()</code> through queue*/
    public javax.swing.Action[] getActions() {
	return((javax.swing.Action[])runMapping(new MapAction("getActions") {
		public Object map() {
		    return(((JTextComponent)getSource()).getActions());
		}}));}

    /**Maps <code>JTextComponent.getCaret()</code> through queue*/
    public Caret getCaret() {
	return((Caret)runMapping(new MapAction("getCaret") {
		public Object map() {
		    return(((JTextComponent)getSource()).getCaret());
		}}));}

    /**Maps <code>JTextComponent.getCaretColor()</code> through queue*/
    public Color getCaretColor() {
	return((Color)runMapping(new MapAction("getCaretColor") {
		public Object map() {
		    return(((JTextComponent)getSource()).getCaretColor());
		}}));}

    /**Maps <code>JTextComponent.getCaretPosition()</code> through queue*/
    public int getCaretPosition() {
	return(runMapping(new MapIntegerAction("getCaretPosition") {
		public int map() {
		    return(((JTextComponent)getSource()).getCaretPosition());
		}}));}

    /**Maps <code>JTextComponent.getDisabledTextColor()</code> through queue*/
    public Color getDisabledTextColor() {
	return((Color)runMapping(new MapAction("getDisabledTextColor") {
		public Object map() {
		    return(((JTextComponent)getSource()).getDisabledTextColor());
		}}));}

    /**Maps <code>JTextComponent.getDocument()</code> through queue*/
    public Document getDocument() {
	return((Document)runMapping(new MapAction("getDocument") {
		public Object map() {
		    return(((JTextComponent)getSource()).getDocument());
		}}));}

    /**Maps <code>JTextComponent.getFocusAccelerator()</code> through queue*/
    public char getFocusAccelerator() {
	return(runMapping(new MapCharacterAction("getFocusAccelerator") {
		public char map() {
		    return(((JTextComponent)getSource()).getFocusAccelerator());
		}}));}

    /**Maps <code>JTextComponent.getHighlighter()</code> through queue*/
    public Highlighter getHighlighter() {
	return((Highlighter)runMapping(new MapAction("getHighlighter") {
		public Object map() {
		    return(((JTextComponent)getSource()).getHighlighter());
		}}));}

    /**Maps <code>JTextComponent.getKeymap()</code> through queue*/
    public Keymap getKeymap() {
	return((Keymap)runMapping(new MapAction("getKeymap") {
		public Object map() {
		    return(((JTextComponent)getSource()).getKeymap());
		}}));}

    /**Maps <code>JTextComponent.getMargin()</code> through queue*/
    public Insets getMargin() {
	return((Insets)runMapping(new MapAction("getMargin") {
		public Object map() {
		    return(((JTextComponent)getSource()).getMargin());
		}}));}

    /**Maps <code>JTextComponent.getPreferredScrollableViewportSize()</code> through queue*/
    public Dimension getPreferredScrollableViewportSize() {
	return((Dimension)runMapping(new MapAction("getPreferredScrollableViewportSize") {
		public Object map() {
		    return(((JTextComponent)getSource()).getPreferredScrollableViewportSize());
		}}));}

    /**Maps <code>JTextComponent.getScrollableBlockIncrement(Rectangle, int, int)</code> through queue*/
    public int getScrollableBlockIncrement(final Rectangle rectangle, final int i, final int i1) {
	return(runMapping(new MapIntegerAction("getScrollableBlockIncrement") {
		public int map() {
		    return(((JTextComponent)getSource()).getScrollableBlockIncrement(rectangle, i, i1));
		}}));}

    /**Maps <code>JTextComponent.getScrollableTracksViewportHeight()</code> through queue*/
    public boolean getScrollableTracksViewportHeight() {
	return(runMapping(new MapBooleanAction("getScrollableTracksViewportHeight") {
		public boolean map() {
		    return(((JTextComponent)getSource()).getScrollableTracksViewportHeight());
		}}));}

    /**Maps <code>JTextComponent.getScrollableTracksViewportWidth()</code> through queue*/
    public boolean getScrollableTracksViewportWidth() {
	return(runMapping(new MapBooleanAction("getScrollableTracksViewportWidth") {
		public boolean map() {
		    return(((JTextComponent)getSource()).getScrollableTracksViewportWidth());
		}}));}

    /**Maps <code>JTextComponent.getScrollableUnitIncrement(Rectangle, int, int)</code> through queue*/
    public int getScrollableUnitIncrement(final Rectangle rectangle, final int i, final int i1) {
	return(runMapping(new MapIntegerAction("getScrollableUnitIncrement") {
		public int map() {
		    return(((JTextComponent)getSource()).getScrollableUnitIncrement(rectangle, i, i1));
		}}));}

    /**Maps <code>JTextComponent.getSelectedText()</code> through queue*/
    public String getSelectedText() {
	return((String)runMapping(new MapAction("getSelectedText") {
		public Object map() {
		    return(((JTextComponent)getSource()).getSelectedText());
		}}));}

    /**Maps <code>JTextComponent.getSelectedTextColor()</code> through queue*/
    public Color getSelectedTextColor() {
	return((Color)runMapping(new MapAction("getSelectedTextColor") {
		public Object map() {
		    return(((JTextComponent)getSource()).getSelectedTextColor());
		}}));}

    /**Maps <code>JTextComponent.getSelectionColor()</code> through queue*/
    public Color getSelectionColor() {
	return((Color)runMapping(new MapAction("getSelectionColor") {
		public Object map() {
		    return(((JTextComponent)getSource()).getSelectionColor());
		}}));}

    /**Maps <code>JTextComponent.getSelectionEnd()</code> through queue*/
    public int getSelectionEnd() {
	return(runMapping(new MapIntegerAction("getSelectionEnd") {
		public int map() {
		    return(((JTextComponent)getSource()).getSelectionEnd());
		}}));}

    /**Maps <code>JTextComponent.getSelectionStart()</code> through queue*/
    public int getSelectionStart() {
	return(runMapping(new MapIntegerAction("getSelectionStart") {
		public int map() {
		    return(((JTextComponent)getSource()).getSelectionStart());
		}}));}

    /**Maps <code>JTextComponent.getText()</code> through queue*/
    public String getText() {
	return((String)runMapping(new MapAction("getText") {
		public Object map() {
		    return(((JTextComponent)getSource()).getText());
		}}));}

    /**Maps <code>JTextComponent.getText(int, int)</code> through queue*/
    public String getText(final int i, final int i1) {
	return((String)runMapping(new MapAction("getText") {
		public Object map() throws BadLocationException {
		    return(((JTextComponent)getSource()).getText(i, i1));
		}}));}

    /**Maps <code>JTextComponent.getUI()</code> through queue*/
    public TextUI getUI() {
	return((TextUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JTextComponent)getSource()).getUI());
		}}));}

    /**Maps <code>JTextComponent.isEditable()</code> through queue*/
    public boolean isEditable() {
	return(runMapping(new MapBooleanAction("isEditable") {
		public boolean map() {
		    return(((JTextComponent)getSource()).isEditable());
		}}));}

    /**Maps <code>JTextComponent.modelToView(int)</code> through queue*/
    public Rectangle modelToView(final int i) {
	return((Rectangle)runMapping(new MapAction("modelToView") {
		public Object map() throws BadLocationException {
		    return(((JTextComponent)getSource()).modelToView(i));
		}}));}

    /**Maps <code>JTextComponent.moveCaretPosition(int)</code> through queue*/
    public void moveCaretPosition(final int i) {
	runMapping(new MapVoidAction("moveCaretPosition") {
		public void map() {
		    ((JTextComponent)getSource()).moveCaretPosition(i);
		}});}

    /**Maps <code>JTextComponent.paste()</code> through queue*/
    public void paste() {
	runMapping(new MapVoidAction("paste") {
		public void map() {
		    ((JTextComponent)getSource()).paste();
		}});}

    /**Maps <code>JTextComponent.read(Reader, Object)</code> through queue*/
    public void read(final Reader reader, final Object object) {
	runMapping(new MapVoidAction("read") {
		public void map() throws IOException {
		    ((JTextComponent)getSource()).read(reader, object);
		}});}

    /**Maps <code>JTextComponent.removeCaretListener(CaretListener)</code> through queue*/
    public void removeCaretListener(final CaretListener caretListener) {
	runMapping(new MapVoidAction("removeCaretListener") {
		public void map() {
		    ((JTextComponent)getSource()).removeCaretListener(caretListener);
		}});}

    /**Maps <code>JTextComponent.replaceSelection(String)</code> through queue*/
    public void replaceSelection(final String string) {
	runMapping(new MapVoidAction("replaceSelection") {
		public void map() {
		    ((JTextComponent)getSource()).replaceSelection(string);
		}});}

    /**Maps <code>JTextComponent.select(int, int)</code> through queue*/
    public void select(final int i, final int i1) {
	runMapping(new MapVoidAction("select") {
		public void map() {
		    ((JTextComponent)getSource()).select(i, i1);
		}});}

    /**Maps <code>JTextComponent.selectAll()</code> through queue*/
    public void selectAll() {
	runMapping(new MapVoidAction("selectAll") {
		public void map() {
		    ((JTextComponent)getSource()).selectAll();
		}});}

    /**Maps <code>JTextComponent.setCaret(Caret)</code> through queue*/
    public void setCaret(final Caret caret) {
	runMapping(new MapVoidAction("setCaret") {
		public void map() {
		    ((JTextComponent)getSource()).setCaret(caret);
		}});}

    /**Maps <code>JTextComponent.setCaretColor(Color)</code> through queue*/
    public void setCaretColor(final Color color) {
	runMapping(new MapVoidAction("setCaretColor") {
		public void map() {
		    ((JTextComponent)getSource()).setCaretColor(color);
		}});}

    /**Maps <code>JTextComponent.setCaretPosition(int)</code> through queue*/
    public void setCaretPosition(final int i) {
	runMapping(new MapVoidAction("setCaretPosition") {
		public void map() {
		    ((JTextComponent)getSource()).setCaretPosition(i);
		}});}

    /**Maps <code>JTextComponent.setDisabledTextColor(Color)</code> through queue*/
    public void setDisabledTextColor(final Color color) {
	runMapping(new MapVoidAction("setDisabledTextColor") {
		public void map() {
		    ((JTextComponent)getSource()).setDisabledTextColor(color);
		}});}

    /**Maps <code>JTextComponent.setDocument(Document)</code> through queue*/
    public void setDocument(final Document document) {
	runMapping(new MapVoidAction("setDocument") {
		public void map() {
		    ((JTextComponent)getSource()).setDocument(document);
		}});}

    /**Maps <code>JTextComponent.setEditable(boolean)</code> through queue*/
    public void setEditable(final boolean b) {
	runMapping(new MapVoidAction("setEditable") {
		public void map() {
		    ((JTextComponent)getSource()).setEditable(b);
		}});}

    /**Maps <code>JTextComponent.setFocusAccelerator(char)</code> through queue*/
    public void setFocusAccelerator(final char c) {
	runMapping(new MapVoidAction("setFocusAccelerator") {
		public void map() {
		    ((JTextComponent)getSource()).setFocusAccelerator(c);
		}});}

    /**Maps <code>JTextComponent.setHighlighter(Highlighter)</code> through queue*/
    public void setHighlighter(final Highlighter highlighter) {
	runMapping(new MapVoidAction("setHighlighter") {
		public void map() {
		    ((JTextComponent)getSource()).setHighlighter(highlighter);
		}});}

    /**Maps <code>JTextComponent.setKeymap(Keymap)</code> through queue*/
    public void setKeymap(final Keymap keymap) {
	runMapping(new MapVoidAction("setKeymap") {
		public void map() {
		    ((JTextComponent)getSource()).setKeymap(keymap);
		}});}

    /**Maps <code>JTextComponent.setMargin(Insets)</code> through queue*/
    public void setMargin(final Insets insets) {
	runMapping(new MapVoidAction("setMargin") {
		public void map() {
		    ((JTextComponent)getSource()).setMargin(insets);
		}});}

    /**Maps <code>JTextComponent.setSelectedTextColor(Color)</code> through queue*/
    public void setSelectedTextColor(final Color color) {
	runMapping(new MapVoidAction("setSelectedTextColor") {
		public void map() {
		    ((JTextComponent)getSource()).setSelectedTextColor(color);
		}});}

    /**Maps <code>JTextComponent.setSelectionColor(Color)</code> through queue*/
    public void setSelectionColor(final Color color) {
	runMapping(new MapVoidAction("setSelectionColor") {
		public void map() {
		    ((JTextComponent)getSource()).setSelectionColor(color);
		}});}

    /**Maps <code>JTextComponent.setSelectionEnd(int)</code> through queue*/
    public void setSelectionEnd(final int i) {
	runMapping(new MapVoidAction("setSelectionEnd") {
		public void map() {
		    ((JTextComponent)getSource()).setSelectionEnd(i);
		}});}

    /**Maps <code>JTextComponent.setSelectionStart(int)</code> through queue*/
    public void setSelectionStart(final int i) {
	runMapping(new MapVoidAction("setSelectionStart") {
		public void map() {
		    ((JTextComponent)getSource()).setSelectionStart(i);
		}});}

    /**Maps <code>JTextComponent.setText(String)</code> through queue*/
    public void setText(final String string) {
	runMapping(new MapVoidAction("setText") {
		public void map() {
		    ((JTextComponent)getSource()).setText(string);
		}});}

    /**Maps <code>JTextComponent.setUI(TextUI)</code> through queue*/
    public void setUI(final TextUI textUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JTextComponent)getSource()).setUI(textUI);
		}});}

    /**Maps <code>JTextComponent.viewToModel(Point)</code> through queue*/
    public int viewToModel(final Point point) {
	return(runMapping(new MapIntegerAction("viewToModel") {
		public int map() {
		    return(((JTextComponent)getSource()).viewToModel(point));
		}}));}

    /**Maps <code>JTextComponent.write(Writer)</code> through queue*/
    public void write(final Writer writer) {
	runMapping(new MapVoidAction("write") {
		public void map() throws IOException {
		    ((JTextComponent)getSource()).write(writer);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Can be throught during a text operation 
     * if text has not been found in the component.
     */
    public class NoSuchTextException extends JemmyInputException {
	/**
	 * Constructor.
         * @param text a nonexistent text.
	 */
	public NoSuchTextException(String text) {
	    super("No such text as \"" + text + "\"", getSource());
	}
    }

    /**
     * Interface defining additional text cearch criteria.
     * @see #getPositionByText(java.lang.String, JTextComponentOperator.TextChooser)
     */
    public interface TextChooser {
        /**
         * Checkes if position fits the criteria.
         * @param document a document to be checked.
         * @param offset a checked position
         * @return true if the position fits the criteria.
         */
	public boolean checkPosition(Document document, int offset);
        /**
         * Returns a printable description of the criteria.
         * @return a description of this chooser.
         */
	public String getDescription();
    }

    /**
     * Allows to find component by text.
     */
    public static class JTextComponentByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs JTextComponentByTextFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JTextComponentByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JTextComponentByTextFinder.
         * @param lb a text pattern
         */
	public JTextComponentByTextFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JTextComponent) {
		if(((JTextComponent)comp).getText() != null) {
		    return(comparator.equals(((JTextComponent)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JTextComponent with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class JTextComponentFinder extends Finder {
        /**
         * Constructs JTextComponentFinder.
         * @param sf other searching criteria.
         */
	public JTextComponentFinder(ComponentChooser sf) {
            super(JTextComponent.class, sf);
	}
        /**
         * Constructs JTextComponentFinder.
         */
	public JTextComponentFinder() {
            super(JTextComponent.class);
	}
    }
}
