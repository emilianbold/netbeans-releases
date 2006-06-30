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

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.TextDriver;

import java.awt.Component;
import java.awt.Container;
import java.awt.TextComponent;

import java.awt.event.KeyEvent;
import java.awt.event.TextListener;

import java.util.Hashtable;

/**
 * This operator type covers java.awt.TextArea component.
 *
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */
public class TextComponentOperator extends ComponentOperator
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

    private TextDriver driver;

    /**
     * Constructor.
     * @param b The <code>java.awt.TextComponent</code> managed by
     * this instance.
     */
    public TextComponentOperator(TextComponent b) {
	super(b);
	driver = DriverManager.getTextDriver(getClass());
    }

    /**
     * Constructs a TextComponentOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public TextComponentOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((TextComponent)cont.
             waitSubComponent(new TextComponentFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a TextComponentOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public TextComponentOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the <code>index+1</code>'th
     * <code>java.awt.TextComponent</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for textComponent.
     * @param text TextComponent text. 
     * @param index Ordinal component index. The first component has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextComponentOperator(ContainerOperator cont, String text, int index) {
	this((TextComponent)waitComponent(cont, 
					   new TextComponentByTextFinder(text, 
									   cont.getComparator()),
					   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the first
     * <code>java.awt.TextComponent</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for textComponent.
     * @param text TextComponent text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextComponentOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textComponent.
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextComponentOperator(ContainerOperator cont, int index) {
	this((TextComponent)
	     waitComponent(cont, 
			   new TextComponentFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for textComponent.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public TextComponentOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches TextComponent in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @param index Ordinal component index.  The first <code>index</code> is 0.
     * @return TextComponent instance or null if component was not found.
     */
    public static TextComponent findTextComponent(Container cont, ComponentChooser chooser, int index) {
	return((TextComponent)findComponent(cont, new TextComponentFinder(chooser), index));
    }

    /**
     * Searches for the first TextComponent in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @return TextComponent instance or null if component was not found.
     */
    public static TextComponent findTextComponent(Container cont, ComponentChooser chooser) {
	return(findTextComponent(cont, chooser, 0));
    }

    /**
     * Searches TextComponent by text.
     * @param cont Container to search component in.
     * @param text TextComponent text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return TextComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextComponent findTextComponent(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findTextComponent(cont, new TextComponentByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches TextComponent by text.
     * @param cont Container to search component in.
     * @param text TextComponent text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return TextComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextComponent findTextComponent(Container cont, String text, boolean ce, boolean ccs) {
	return(findTextComponent(cont, text, ce, ccs, 0));
    }

    /**
     * Waits TextComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return TextComponent instance.
     */
    public static TextComponent waitTextComponent(Container cont, ComponentChooser chooser, int index) {
	return((TextComponent)waitComponent(cont, new TextComponentFinder(chooser), index));
    }

    /**
     * Waits 0'th TextComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return TextComponent instance.
     */
    public static TextComponent waitTextComponent(Container cont, ComponentChooser chooser){
	return(waitTextComponent(cont, chooser, 0));
    }

    /**
     * Waits TextComponent by text.
     * @param cont Container to search component in.
     * @param text TextComponent text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return TextComponent instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextComponent waitTextComponent(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitTextComponent(cont, new TextComponentByTextFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits TextComponent by text.
     * @param cont Container to search component in.
     * @param text TextComponent text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return TextComponent instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static TextComponent waitTextComponent(Container cont, String text, boolean ce, boolean ccs) {
	return(waitTextComponent(cont, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("TextComponentOperator.PushKeyTimeout", PUSH_KEY_TIMEOUT);
	Timeouts.initDefault("TextComponentOperator.BetweenKeysTimeout", BETWEEN_KEYS_TIMEOUT);
	Timeouts.initDefault("TextComponentOperator.ChangeCaretPositionTimeout", CHANGE_CARET_POSITION_TIMEOUT);
	Timeouts.initDefault("TextComponentOperator.TypeTextTimeout", TYPE_TEXT_TIMEOUT);
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

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (TextDriver)DriverManager.
	    getDriver(DriverManager.TEXT_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Changes caret position.
     * @param position Position to move caret to.
     * 
     */
    public void changeCaretPosition(final int position) {
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.changeCaretPosition(TextComponentOperator.this, position);
		    return(null);
		}
		public String getDescription() {
		    return("Caret moving");
		}
	    }, getTimeouts().getTimeout("TextComponentOperator.ChangeCaretPositionTimeout"));
    }

    /**
     * Selects a part of text.
     * @param startPosition Start caret position
     * @param finalPosition Final caret position
     * 
     */
    public void selectText(final int startPosition, final int finalPosition) {
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.selectText(TextComponentOperator.this, startPosition, finalPosition);
		    return(null);
		}
		public String getDescription() {
		    return("Text selecting");
		}
	    }, getTimeouts().getTimeout("TextComponentOperator.TypeTextTimeout"));
    }

    /**
     * Finds start text position.
     * @param text Text to be searched.
     * @param index Index of text instance (first instance has index 0)
     * @return Caret position correspondent to text start.
     */
    public int getPositionByText(String text, int index) {
	String allText = getText();
	int position = 0;
	int ind = 0;
	while((position = allText.indexOf(text, position)) >= 0) {
	    if(ind == index) {
		return(position);
	    } else {
		ind++;
	    }
	    position = position + text.length();
	}
	return(-1);
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
     * Clears text.
     * 
     */
    public void clearText() {
	output.printLine("Clearing text in text component\n    : " +
			 toStringSource());
	output.printGolden("Clearing text in text component");
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.clearText(TextComponentOperator.this);
		    return(null);
		}
		public String getDescription() {
		    return("Text clearing");
		}
	    }, getTimeouts().getTimeout("TextComponentOperator.TypeTextTimeout"));
    }

    /**
     * Types text starting from known position.
     * @param text Text to be typed.
     * @param caretPosition Position to start type text
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
		    driver.typeText(TextComponentOperator.this, text, caretPosition);
		    return(null);
		}
		public String getDescription() {
		    return("Text typing");
		}
	    }, getTimeouts().getTimeout("TextComponentOperator.TypeTextTimeout"));
    }

    /**
     * Types text starting from known position.
     * @param text Text to be typed.
     */
    public void typeText(String text) {
	typeText(text, getCaretPosition());
    }

    /**
     * Requests a focus, clears text, types new one and pushes Enter.
     * @param text New text value. Shouln't include final '\n'.
     * 
     */
    public void enterText(final String text) {
	makeComponentVisible();
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.enterText(TextComponentOperator.this, text);
		    return(null);
		}
		public String getDescription() {
		    return("Text entering");
		}
	    }, getTimeouts().getTimeout("TextComponentOperator.TypeTextTimeout"));
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(TEXT_DPROP, ((TextComponent)getSource()).getText());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //
    /**Maps <code>TextComponent.addTextListener(TextListener)</code> through queue*/
    public void addTextListener(final TextListener textListener) {
	runMapping(new MapVoidAction("addTextListener") {
		public void map() {
		    ((TextComponent)getSource()).addTextListener(textListener);
		}});}

    /**Maps <code>TextComponent.getCaretPosition()</code> through queue*/
    public int getCaretPosition() {
	return(runMapping(new MapIntegerAction("getCaretPosition") {
		public int map() {
		    return(((TextComponent)getSource()).getCaretPosition());
		}}));}

    /**Maps <code>TextComponent.getSelectedText()</code> through queue*/
    public String getSelectedText() {
	return((String)runMapping(new MapAction("getSelectedText") {
		public Object map() {
		    return(((TextComponent)getSource()).getSelectedText());
		}}));}

    /**Maps <code>TextComponent.getSelectionEnd()</code> through queue*/
    public int getSelectionEnd() {
	return(runMapping(new MapIntegerAction("getSelectionEnd") {
		public int map() {
		    return(((TextComponent)getSource()).getSelectionEnd());
		}}));}

    /**Maps <code>TextComponent.getSelectionStart()</code> through queue*/
    public int getSelectionStart() {
	return(runMapping(new MapIntegerAction("getSelectionStart") {
		public int map() {
		    return(((TextComponent)getSource()).getSelectionStart());
		}}));}

    /**Maps <code>TextComponent.getText()</code> through queue*/
    public String getText() {
	return((String)runMapping(new MapAction("getText") {
		public Object map() {
		    return(((TextComponent)getSource()).getText());
		}}));}

    /**Maps <code>TextComponent.isEditable()</code> through queue*/
    public boolean isEditable() {
	return(runMapping(new MapBooleanAction("isEditable") {
		public boolean map() {
		    return(((TextComponent)getSource()).isEditable());
		}}));}

    /**Maps <code>TextComponent.removeTextListener(TextListener)</code> through queue*/
    public void removeTextListener(final TextListener textListener) {
	runMapping(new MapVoidAction("removeTextListener") {
		public void map() {
		    ((TextComponent)getSource()).removeTextListener(textListener);
		}});}

    /**Maps <code>TextComponent.select(int, int)</code> through queue*/
    public void select(final int i, final int i1) {
	runMapping(new MapVoidAction("select") {
		public void map() {
		    ((TextComponent)getSource()).select(i, i1);
		}});}

    /**Maps <code>TextComponent.selectAll()</code> through queue*/
    public void selectAll() {
	runMapping(new MapVoidAction("selectAll") {
		public void map() {
		    ((TextComponent)getSource()).selectAll();
		}});}

    /**Maps <code>TextComponent.setCaretPosition(int)</code> through queue*/
    public void setCaretPosition(final int i) {
	runMapping(new MapVoidAction("setCaretPosition") {
		public void map() {
		    ((TextComponent)getSource()).setCaretPosition(i);
		}});}

    /**Maps <code>TextComponent.setEditable(boolean)</code> through queue*/
    public void setEditable(final boolean b) {
	runMapping(new MapVoidAction("setEditable") {
		public void map() {
		    ((TextComponent)getSource()).setEditable(b);
		}});}

    /**Maps <code>TextComponent.setSelectionEnd(int)</code> through queue*/
    public void setSelectionEnd(final int i) {
	runMapping(new MapVoidAction("setSelectionEnd") {
		public void map() {
		    ((TextComponent)getSource()).setSelectionEnd(i);
		}});}

    /**Maps <code>TextComponent.setSelectionStart(int)</code> through queue*/
    public void setSelectionStart(final int i) {
	runMapping(new MapVoidAction("setSelectionStart") {
		public void map() {
		    ((TextComponent)getSource()).setSelectionStart(i);
		}});}

    /**Maps <code>TextComponent.setText(String)</code> through queue*/
    public void setText(final String string) {
	runMapping(new MapVoidAction("setText") {
		public void map() {
		    ((TextComponent)getSource()).setText(string);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Return a TextDriver  used by this component.
     * @return a driver got by the operator during creation.
     */
    protected TextDriver getTextDriver() {
	return(driver);
    }

    /**
     * Allows to find component by text.
     */
    public static class TextComponentByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs TextComponentByTextFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public TextComponentByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs TextComponentByTextFinder.
         * @param lb a text pattern
         */
	public TextComponentByTextFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof TextComponent) {
		if(((TextComponent)comp).getText() != null) {
		    return(comparator.equals(((TextComponent)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("TextComponent with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class TextComponentFinder extends Finder {
        /**
         * Constructs TextComponentFinder.
         * @param sf other searching criteria.
         */
	public TextComponentFinder(ComponentChooser sf) {
            super(TextComponent.class, sf);
	}
        /**
         * Constructs TextComponentFinder.
         */
	public TextComponentFinder() {
            super(TextComponent.class);
	}
    }
}
