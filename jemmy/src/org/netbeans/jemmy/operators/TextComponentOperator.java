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

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.TextDriver;

import java.awt.Component;
import java.awt.Container;
import java.awt.TextComponent;

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

public class TextComponentOperator extends ComponentOperator
    implements Timeoutable, Outputable {

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

    public TextComponentOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((TextComponent)cont.
             waitSubComponent(new TextComponentFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
     */
    public static TextComponent waitTextComponent(Container cont, ComponentChooser chooser, int index) {
	return((TextComponent)waitComponent(cont, new TextComponentFinder(chooser), index));
    }

    /**
     * Waits 0'th TextComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return TextComponent instance.
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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
     * @throws TimeoutExpiredException
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

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (TextDriver)DriverManager.
	    getDriver(DriverManager.TEXT_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

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

    public int getPositionByText(String text) {
	return(getPositionByText(text, 0));
    }

    public void clearText() {
	output.printLine("Clearing text in text component\n    : " +
			 getSource().toString());
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

    public void typeText(final String text, final int caretPosition) {
	output.printLine("Typing text \"" + text + "\" from " +
			 Integer.toString(caretPosition) + " position " +
			 "in text component\n    : " +
			 getSource().toString());
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

    public void typeText(String text) {
	typeText(text, getCaretPosition());
    }
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
    /**
     * Returns information about component.
     */
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

    protected TextDriver getTextDriver() {
	return(driver);
    }

    public static class TextComponentByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public TextComponentByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
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

    public static class TextComponentFinder extends Finder {
	public TextComponentFinder(ComponentChooser sf) {
            super(TextComponent.class, sf);
	}
	public TextComponentFinder() {
            super(TextComponent.class);
	}
    }
}
