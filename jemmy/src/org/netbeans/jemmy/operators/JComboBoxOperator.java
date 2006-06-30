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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.WindowWaiter;
import org.netbeans.jemmy.util.EmptyVisualizer;
import org.netbeans.jemmy.drivers.ListDriver;
import org.netbeans.jemmy.drivers.DriverManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import java.util.Hashtable;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import javax.swing.event.ListDataEvent;

import javax.swing.plaf.ComboBoxUI;

import javax.swing.plaf.basic.ComboPopup;

/**
 * <BR><BR>Timeouts used: <BR>
 * JComboBoxOperator.BeforeSelectingTimeout - time to sleep after list opened and before item selected <BR>
 * JComboBoxOperator.WaitListTimeout - time to wait list opened <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait component enabled <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for item to be selected <BR>
 * AbstractButtonOperator.PushButtonTimeout - time between combo button pressing and releasing<BR>
 * ComponentOperator.MouseClickTimeout - time between mouse pressing and releasing during item selecting<BR>
 * JTextComponentOperator.PushKeyTimeout - time between key pressing and releasing during text typing <BR>
 * JTextComponentOperator.BetweenKeysTimeout - time to sleep between two chars typing <BR>
 * JTextComponentOperator.ChangeCaretPositionTimeout - maximum time to chenge caret position <BR>
 * JTextComponentOperator.TypeTextTimeout - maximum time to type text <BR>.
 *
 * @see Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JComboBoxOperator extends JComponentOperator
implements Timeoutable, Outputable {

    /**
     * Identifier for a "text" property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Text";

    /**
     * Identifier for a "item" property values.
     * @see #getDump
     */
    public static final String ITEM_PREFIX_DPROP = "Item";

    private final static long BEFORE_SELECTING_TIMEOUT = 0;
    private final static long WAIT_LIST_TIMEOUT = 60000;

    private TestOut output;
    private Timeouts timeouts;
    private ComponentSearcher searcher;

    private JButtonOperator button;
    private JTextFieldOperator text;

    ListDriver driver;

    /**
     * Constructs a JComboBoxOperator object.
     * @param b a component
     */
    public JComboBoxOperator(JComboBox b) {
	super(b);
	driver = DriverManager.getListDriver(getClass());
    }

    /**
     * Constructs a JComboBoxOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JComboBoxOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JComboBox)cont.
             waitSubComponent(new JComboBoxFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JComboBoxOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JComboBoxOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Text of item which is currently selected. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JComboBoxOperator(ContainerOperator cont, String text, int index) {
	this((JComboBox)waitComponent(cont, 
				      new JComboBoxByItemFinder(text, -1,
								cont.getComparator()),
				      index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Text of item which is currently selected. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JComboBoxOperator(ContainerOperator cont, String text) {
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
    public JComboBoxOperator(ContainerOperator cont, int index) {
	this((JComboBox)
	     waitComponent(cont, 
			   new JComboBoxFinder(),
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
    public JComboBoxOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JComboBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JComboBox instance or null if component was not found.
     */
    public static JComboBox findJComboBox(Container cont, ComponentChooser chooser, int index) {
	return((JComboBox)findComponent(cont, new JComboBoxFinder(chooser), index));
    }

    /**
     * Searches 0'th JComboBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JComboBox instance or null if component was not found.
     */
    public static JComboBox findJComboBox(Container cont, ComponentChooser chooser) {
	return(findJComboBox(cont, chooser, 0));
    }

    /**
     * Searches JComboBox by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @param index Ordinal component index.
     * @return JComboBox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JComboBox findJComboBox(Container cont, String text, boolean ce, boolean ccs, int itemIndex, int index) {
	return(findJComboBox(cont, 
			     new JComboBoxByItemFinder(text, 
						       itemIndex, 
						       new DefaultStringComparator(ce, ccs)), 
			     index));
    }

    /**
     * Searches JComboBox by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @return JComboBox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JComboBox findJComboBox(Container cont, String text, boolean ce, boolean ccs, int itemIndex) {
	return(findJComboBox(cont, text, ce, ccs, itemIndex, 0));
    }

    /**
     * Waits JComboBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JComboBox instance or null if component was not found.
     * @throws TimeoutExpiredException
     */
    public static JComboBox waitJComboBox(Container cont, ComponentChooser chooser, int index)  {
	return((JComboBox)waitComponent(cont, new JComboBoxFinder(chooser), index));
    }

    /**
     * Waits 0'th JComboBox in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JComboBox instance or null if component was not found.
     * @throws TimeoutExpiredException
     */
    public static JComboBox waitJComboBox(Container cont, ComponentChooser chooser) {
	return(waitJComboBox(cont, chooser, 0));
    }

    /**
     * Waits JComboBox by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @param index Ordinal component index.
     * @return JComboBox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JComboBox waitJComboBox(Container cont, String text, boolean ce, boolean ccs, int itemIndex, int index) {
	return(waitJComboBox(cont, 
			     new JComboBoxByItemFinder(text, 
						       itemIndex, 
						       new DefaultStringComparator(ce, ccs)), 
			     index));
    }

    /**
     * Waits JComboBox by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @return JComboBox instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JComboBox waitJComboBox(Container cont, String text, boolean ce, boolean ccs, int itemIndex) {
	return(waitJComboBox(cont, text, ce, ccs, itemIndex, 0));
    }

    static {
	Timeouts.initDefault("JComboBoxOperator.BeforeSelectingTimeout", BEFORE_SELECTING_TIMEOUT);
	Timeouts.initDefault("JComboBoxOperator.WaitListTimeout", WAIT_LIST_TIMEOUT);
    }

    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut output) {
	super.setOutput(output);
	this.output = output;
    }

    public TestOut getOutput() {
	return(output);
    }

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (ListDriver)DriverManager.
	    getDriver(DriverManager.LIST_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Searches JButton inside component.
     * @return JButton which is used to expand this JComboBox.
     */
    public JButton findJButton() {
	return((JButton)waitSubComponent(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(comp instanceof JButton);
		}
		public String getDescription() {
		    return("Button for combobox popup menu opening");
		}
	    }));
    }

    /**
     * Searches JTextField inside component.
     * @return JTextField if JComboBox is editable, null otherwise.
     */
    public JTextField findJTextField() {
	return((JTextField)waitSubComponent(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(comp instanceof JTextField);
		}
		public String getDescription() {
		    return("ComboBox's text field");
		}
	    }));
    }

    /**
     * Creates an operator for button returned by
     * <code>findJButton()</code> method.
     * @return new JButtonOperator instance.
     */
    public JButtonOperator getButton() {
	if(button == null) {
	    button = new JButtonOperator(findJButton());
	    button.copyEnvironment(this);
	    button.setOutput(getOutput().createErrorOutput());
	}
	return(button);
    }

    /**
     * Creates an operator for button returned by
     * <code>findJTextField()</code> method.
     * @return new JTextField instance.
     */
    public JTextFieldOperator getTextField() {
        if(((JComboBox)getSource()).isEditable()) {
            text = new JTextFieldOperator(findJTextField());
            text.copyEnvironment(this);
            text.setOutput(getOutput().createErrorOutput());
        }
	return(text);
    }

    /**
     * Waits combobox's list to be displayed.
     * @return JList object if it was displayed in JComboBoxOperator.WaitListTimeout millisecont,
     * null otherwise.
     * @throws TimeoutExpiredException
     */
    public JList waitList() {
	Waiter pw = new ListWater();
	pw.setOutput(output.createErrorOutput());
	Timeouts times = timeouts.cloneThis();
	times.setTimeout("Waiter.WaitingTime", 
			 times.getTimeout("JComboBoxOperator.WaitListTimeout"));
	pw.setTimeouts(times);
	try {
	    return((JList)pw.waitAction(null));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	}
	return(null);
    }

    /**
     * Push combobox's button to expand or collapse combobox.
     * @throws TimeoutExpiredException
     */
    public void pushComboButton() {
	makeComponentVisible();
	getButton().push();
    }

    /**
     * Finds an item between list items.
     * @param item a text pattern.
     * @param comparator a searching criteria.
     * @return an item index.
     */
    public int findItemIndex(String item, StringComparator comparator) {
	ComboBoxModel model = getModel();
	for(int i = 0; i < model.getSize(); i++) {
	    if(comparator.equals(model.getElementAt(i).toString(), item)) {
		return(i);
	    }
	}
	return(-1);
    }
    
    /**
     * Waits for an item available between list items.
     * @param item a text pattern.
     * @param comparator a searching criteria.
     * @return an item index or throws TimeoutExpiredException if item not found.
     */
    public int waitItem(final String item, final StringComparator comparator) {
	getOutput().printLine("Wait item \"" + item + "\" available in combo box \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait item \"" + item + "\" available in combo box.");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
                    return findItemIndex(item, comparator) > -1;
		}
		public String getDescription() {
                    return "Item \"" + item + "\" available in combo box.";
		}
	    });
       return findItemIndex(item, comparator);
    }
    
    /**
     * Waits for an item of given index available between list items.
     * @param itemIndex index of desired item
     * @return an item index or throws TimeoutExpiredException if item not found.
     */
    public int waitItem(final int itemIndex) {
	getOutput().printLine("Wait item of index \"" + itemIndex + "\" available in combo box \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait item of index \"" + itemIndex + "\" available in combo box.");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
                    // given itemIndex is within size of combo box
                    return getModel().getSize() > itemIndex;
		}
		public String getDescription() {
                    return "Item \"" + itemIndex + "\" available in combo box.";
		}
	    });
       return itemIndex;
    }

    /**
     * Selects an item by text.
     * @param item a text pattern.
     * @param comparator a searching criteria.
     */
    public void selectItem(String item, StringComparator comparator) {
	output.printLine("Select \"" + item + "\" item in combobox\n    : " +
			 toStringSource());
	output.printGolden("Select \"" + item + "\" item in combobox");
        selectItem(waitItem(item, comparator));
    }

    /**
     * Selects combobox item.
     * @param item Item text.
     * @param ce Compare exactly.
     * @param cc Compare case sensitivelly.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     * @deprecated Use selectItem(String) or selectItem(String, StringComparator)
     */
    public void selectItem(String item, boolean ce, boolean cc) {
	selectItem(item, new DefaultStringComparator(ce, cc));
    }

    /**
     * Selects combobox item.
     * Uses StringComparator assigned to this object.
     * @param item Item text.
     * @throws TimeoutExpiredException
     */
    public void selectItem(String item) {
	selectItem(item, getComparator());
    }

    /**
     * Selects combobox item.
     * If verification mode is on, checks that right item has been selected. 
     * @param index Item index.
     * @throws TimeoutExpiredException
     */
    public void selectItem(int index) {
	output.printLine("Select " + Integer.toString(index) + "\'th item in combobox\n    : " +
			 toStringSource());
	output.printGolden("Select " + Integer.toString(index) + "\'th item in combobox");
        try {
            waitComponentEnabled();
        } catch(InterruptedException e) {
            throw new JemmyException("Interrupted", e);
        }

	driver.selectItem(this, waitItem(index));

	if(getVerification()) {
	    waitItemSelected(index);
	}
    }

    /**
     * Types text in the editable combobox.
     * If combobox has no focus, does simple mouse click on it first.
     * @param text text to type.
     * @throws TimeoutExpiredException
     */
    public void typeText(String text) {
	makeComponentVisible();
	JTextFieldOperator tfo = getTextField();
	tfo.copyEnvironment(this);
	tfo.setVisualizer(new EmptyVisualizer());
	tfo.typeText(text);
    }

    /**
     * Clears text in the editable combobox using left-arrow and delete keys.
     * If combobox has no focus, does simple mouse click on it first.
     * @throws TimeoutExpiredException
     */
    public void clearText() {
	makeComponentVisible();
	JTextFieldOperator tfo = getTextField();
	tfo.copyEnvironment(this);
	tfo.setVisualizer(new EmptyVisualizer());
	tfo.clearText();
    }

    /**
     * Requests a focus, clears text, types new one and pushes Enter.
     * @param text New text value. Shouln't include final '\n'.
     * @throws TimeoutExpiredException
     */
    public void enterText(String text) {
	makeComponentVisible();
	JTextFieldOperator tfo = getTextField();
	tfo.copyEnvironment(this);
	tfo.setVisualizer(new EmptyVisualizer());
	tfo.enterText(text);
    }

    /**
     * Waits for item to be selected.
     * @param index Item index.
     */
    public void waitItemSelected(final int index) {
	getOutput().printLine("Wait " + Integer.toString(index) + 
			      "'th item to be selected in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait " + Integer.toString(index) + 
				"'th item to be selected");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(getSelectedIndex() == index);
		}
		public String getDescription() {
		    return("Has " + Integer.toString(index) + "'th item selected");
		}
	    });
    }

    /**
     * Waits for item to be selected. Uses getComparator() comparator.
     * @param item wait an item to be selected.
     */
    public void waitItemSelected(final String item) {
	getOutput().printLine("Wait \"" + item + 
			      "\" item to be selected in component \n    : "+
			      toStringSource());
	getOutput().printGolden("WaitWait \"" + item + 
				"\" item to be selected");
	waitState(new JComboBoxByItemFinder(item, -1, getComparator()));

   }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	if(((JComboBox)getSource()).getSelectedItem() != null &&
	   ((JComboBox)getSource()).getSelectedItem().toString() != null) {
	    result.put(TEXT_DPROP, ((JComboBox)getSource()).getSelectedItem().toString());
	}
	String[] items = new String[((JComboBox)getSource()).getItemCount()];
	for(int i = 0; i < ((JComboBox)getSource()).getItemCount(); i++) {
	    if(((JComboBox)getSource()).getItemAt(i) != null &&
	       ((JComboBox)getSource()).getItemAt(i).toString() != null) {
		items[i] = ((JComboBox)getSource()).getItemAt(i).toString();
	    }
	}
	addToDump(result, ITEM_PREFIX_DPROP, items);
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JComboBox.actionPerformed(ActionEvent)</code> through queue*/
    public void actionPerformed(final ActionEvent actionEvent) {
	runMapping(new MapVoidAction("actionPerformed") {
		public void map() {
		    ((JComboBox)getSource()).actionPerformed(actionEvent);
		}});}

    /**Maps <code>JComboBox.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((JComboBox)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>JComboBox.addItem(Object)</code> through queue*/
    public void addItem(final Object object) {
	runMapping(new MapVoidAction("addItem") {
		public void map() {
		    ((JComboBox)getSource()).addItem(object);
		}});}

    /**Maps <code>JComboBox.addItemListener(ItemListener)</code> through queue*/
    public void addItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("addItemListener") {
		public void map() {
		    ((JComboBox)getSource()).addItemListener(itemListener);
		}});}

    /**Maps <code>JComboBox.configureEditor(ComboBoxEditor, Object)</code> through queue*/
    public void configureEditor(final ComboBoxEditor comboBoxEditor, final Object object) {
	runMapping(new MapVoidAction("configureEditor") {
		public void map() {
		    ((JComboBox)getSource()).configureEditor(comboBoxEditor, object);
		}});}

    /**Maps <code>JComboBox.contentsChanged(ListDataEvent)</code> through queue*/
    public void contentsChanged(final ListDataEvent listDataEvent) {
	runMapping(new MapVoidAction("contentsChanged") {
		public void map() {
		    ((JComboBox)getSource()).contentsChanged(listDataEvent);
		}});}

    /**Maps <code>JComboBox.getActionCommand()</code> through queue*/
    public String getActionCommand() {
	return((String)runMapping(new MapAction("getActionCommand") {
		public Object map() {
		    return(((JComboBox)getSource()).getActionCommand());
		}}));}

    /**Maps <code>JComboBox.getEditor()</code> through queue*/
    public ComboBoxEditor getEditor() {
	return((ComboBoxEditor)runMapping(new MapAction("getEditor") {
		public Object map() {
		    return(((JComboBox)getSource()).getEditor());
		}}));}

    /**Maps <code>JComboBox.getItemAt(int)</code> through queue*/
    public Object getItemAt(final int i) {
	return((Object)runMapping(new MapAction("getItemAt") {
		public Object map() {
		    return(((JComboBox)getSource()).getItemAt(i));
		}}));}

    /**Maps <code>JComboBox.getItemCount()</code> through queue*/
    public int getItemCount() {
	return(runMapping(new MapIntegerAction("getItemCount") {
		public int map() {
		    return(((JComboBox)getSource()).getItemCount());
		}}));}

    /**Maps <code>JComboBox.getKeySelectionManager()</code> through queue*/
    public KeySelectionManager getKeySelectionManager() {
	return((KeySelectionManager)runMapping(new MapAction("getKeySelectionManager") {
		public Object map() {
		    return(((JComboBox)getSource()).getKeySelectionManager());
		}}));}

    /**Maps <code>JComboBox.getMaximumRowCount()</code> through queue*/
    public int getMaximumRowCount() {
	return(runMapping(new MapIntegerAction("getMaximumRowCount") {
		public int map() {
		    return(((JComboBox)getSource()).getMaximumRowCount());
		}}));}

    /**Maps <code>JComboBox.getModel()</code> through queue*/
    public ComboBoxModel getModel() {
	return((ComboBoxModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((JComboBox)getSource()).getModel());
		}}));}

    /**Maps <code>JComboBox.getRenderer()</code> through queue*/
    public ListCellRenderer getRenderer() {
	return((ListCellRenderer)runMapping(new MapAction("getRenderer") {
		public Object map() {
		    return(((JComboBox)getSource()).getRenderer());
		}}));}

    /**Maps <code>JComboBox.getSelectedIndex()</code> through queue*/
    public int getSelectedIndex() {
	return(runMapping(new MapIntegerAction("getSelectedIndex") {
		public int map() {
		    return(((JComboBox)getSource()).getSelectedIndex());
		}}));}

    /**Maps <code>JComboBox.getSelectedItem()</code> through queue*/
    public Object getSelectedItem() {
	return((Object)runMapping(new MapAction("getSelectedItem") {
		public Object map() {
		    return(((JComboBox)getSource()).getSelectedItem());
		}}));}

    /**Maps <code>JComboBox.getSelectedObjects()</code> through queue*/
    public Object[] getSelectedObjects() {
	return((Object[])runMapping(new MapAction("getSelectedObjects") {
		public Object map() {
		    return(((JComboBox)getSource()).getSelectedObjects());
		}}));}

    /**Maps <code>JComboBox.getUI()</code> through queue*/
    public ComboBoxUI getUI() {
	return((ComboBoxUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JComboBox)getSource()).getUI());
		}}));}

    /**Maps <code>JComboBox.hidePopup()</code> through queue*/
    public void hidePopup() {
	runMapping(new MapVoidAction("hidePopup") {
		public void map() {
		    ((JComboBox)getSource()).hidePopup();
		}});}

    /**Maps <code>JComboBox.insertItemAt(Object, int)</code> through queue*/
    public void insertItemAt(final Object object, final int i) {
	runMapping(new MapVoidAction("insertItemAt") {
		public void map() {
		    ((JComboBox)getSource()).insertItemAt(object, i);
		}});}

    /**Maps <code>JComboBox.intervalAdded(ListDataEvent)</code> through queue*/
    public void intervalAdded(final ListDataEvent listDataEvent) {
	runMapping(new MapVoidAction("intervalAdded") {
		public void map() {
		    ((JComboBox)getSource()).intervalAdded(listDataEvent);
		}});}

    /**Maps <code>JComboBox.intervalRemoved(ListDataEvent)</code> through queue*/
    public void intervalRemoved(final ListDataEvent listDataEvent) {
	runMapping(new MapVoidAction("intervalRemoved") {
		public void map() {
		    ((JComboBox)getSource()).intervalRemoved(listDataEvent);
		}});}

    /**Maps <code>JComboBox.isEditable()</code> through queue*/
    public boolean isEditable() {
	return(runMapping(new MapBooleanAction("isEditable") {
		public boolean map() {
		    return(((JComboBox)getSource()).isEditable());
		}}));}

    /**Maps <code>JComboBox.isLightWeightPopupEnabled()</code> through queue*/
    public boolean isLightWeightPopupEnabled() {
	return(runMapping(new MapBooleanAction("isLightWeightPopupEnabled") {
		public boolean map() {
		    return(((JComboBox)getSource()).isLightWeightPopupEnabled());
		}}));}

    /**Maps <code>JComboBox.isPopupVisible()</code> through queue*/
    public boolean isPopupVisible() {
	return(runMapping(new MapBooleanAction("isPopupVisible") {
		public boolean map() {
		    return(((JComboBox)getSource()).isPopupVisible());
		}}));}

    /**Maps <code>JComboBox.processKeyEvent(KeyEvent)</code> through queue*/
    public void processKeyEvent(final KeyEvent keyEvent) {
	runMapping(new MapVoidAction("processKeyEvent") {
		public void map() {
		    ((JComboBox)getSource()).processKeyEvent(keyEvent);
		}});}

    /**Maps <code>JComboBox.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((JComboBox)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>JComboBox.removeAllItems()</code> through queue*/
    public void removeAllItems() {
	runMapping(new MapVoidAction("removeAllItems") {
		public void map() {
		    ((JComboBox)getSource()).removeAllItems();
		}});}

    /**Maps <code>JComboBox.removeItem(Object)</code> through queue*/
    public void removeItem(final Object object) {
	runMapping(new MapVoidAction("removeItem") {
		public void map() {
		    ((JComboBox)getSource()).removeItem(object);
		}});}

    /**Maps <code>JComboBox.removeItemAt(int)</code> through queue*/
    public void removeItemAt(final int i) {
	runMapping(new MapVoidAction("removeItemAt") {
		public void map() {
		    ((JComboBox)getSource()).removeItemAt(i);
		}});}

    /**Maps <code>JComboBox.removeItemListener(ItemListener)</code> through queue*/
    public void removeItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("removeItemListener") {
		public void map() {
		    ((JComboBox)getSource()).removeItemListener(itemListener);
		}});}

    /**Maps <code>JComboBox.selectWithKeyChar(char)</code> through queue*/
    public boolean selectWithKeyChar(final char c) {
	return(runMapping(new MapBooleanAction("selectWithKeyChar") {
		public boolean map() {
		    return(((JComboBox)getSource()).selectWithKeyChar(c));
		}}));}

    /**Maps <code>JComboBox.setActionCommand(String)</code> through queue*/
    public void setActionCommand(final String string) {
	runMapping(new MapVoidAction("setActionCommand") {
		public void map() {
		    ((JComboBox)getSource()).setActionCommand(string);
		}});}

    /**Maps <code>JComboBox.setEditable(boolean)</code> through queue*/
    public void setEditable(final boolean b) {
	runMapping(new MapVoidAction("setEditable") {
		public void map() {
		    ((JComboBox)getSource()).setEditable(b);
		}});}

    /**Maps <code>JComboBox.setEditor(ComboBoxEditor)</code> through queue*/
    public void setEditor(final ComboBoxEditor comboBoxEditor) {
	runMapping(new MapVoidAction("setEditor") {
		public void map() {
		    ((JComboBox)getSource()).setEditor(comboBoxEditor);
		}});}

    /**Maps <code>JComboBox.setKeySelectionManager(KeySelectionManager)</code> through queue*/
    public void setKeySelectionManager(final KeySelectionManager keySelectionManager) {
	runMapping(new MapVoidAction("setKeySelectionManager") {
		public void map() {
		    ((JComboBox)getSource()).setKeySelectionManager(keySelectionManager);
		}});}

    /**Maps <code>JComboBox.setLightWeightPopupEnabled(boolean)</code> through queue*/
    public void setLightWeightPopupEnabled(final boolean b) {
	runMapping(new MapVoidAction("setLightWeightPopupEnabled") {
		public void map() {
		    ((JComboBox)getSource()).setLightWeightPopupEnabled(b);
		}});}

    /**Maps <code>JComboBox.setMaximumRowCount(int)</code> through queue*/
    public void setMaximumRowCount(final int i) {
	runMapping(new MapVoidAction("setMaximumRowCount") {
		public void map() {
		    ((JComboBox)getSource()).setMaximumRowCount(i);
		}});}

    /**Maps <code>JComboBox.setModel(ComboBoxModel)</code> through queue*/
    public void setModel(final ComboBoxModel comboBoxModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((JComboBox)getSource()).setModel(comboBoxModel);
		}});}

    /**Maps <code>JComboBox.setPopupVisible(boolean)</code> through queue*/
    public void setPopupVisible(final boolean b) {
	runMapping(new MapVoidAction("setPopupVisible") {
		public void map() {
		    ((JComboBox)getSource()).setPopupVisible(b);
		}});}

    /**Maps <code>JComboBox.setRenderer(ListCellRenderer)</code> through queue*/
    public void setRenderer(final ListCellRenderer listCellRenderer) {
	runMapping(new MapVoidAction("setRenderer") {
		public void map() {
		    ((JComboBox)getSource()).setRenderer(listCellRenderer);
		}});}

    /**Maps <code>JComboBox.setSelectedIndex(int)</code> through queue*/
    public void setSelectedIndex(final int i) {
	runMapping(new MapVoidAction("setSelectedIndex") {
		public void map() {
		    ((JComboBox)getSource()).setSelectedIndex(i);
		}});}

    /**Maps <code>JComboBox.setSelectedItem(Object)</code> through queue*/
    public void setSelectedItem(final Object object) {
	runMapping(new MapVoidAction("setSelectedItem") {
		public void map() {
		    ((JComboBox)getSource()).setSelectedItem(object);
		}});}

    /**Maps <code>JComboBox.setUI(ComboBoxUI)</code> through queue*/
    public void setUI(final ComboBoxUI comboBoxUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JComboBox)getSource()).setUI(comboBoxUI);
		}});}

    /**Maps <code>JComboBox.showPopup()</code> through queue*/
    public void showPopup() {
	runMapping(new MapVoidAction("showPopup") {
		public void map() {
		    ((JComboBox)getSource()).showPopup();
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private void init() {
	if(button == null) {
	    button = new JButtonOperator(findJButton());
	    button.copyEnvironment(this);
	    button.setOutput(getOutput().createErrorOutput());
	    if(((JComboBox)getSource()).isEditable()) {
		text = new JTextFieldOperator(findJTextField());
		text.copyEnvironment(this);
		text.setOutput(getOutput().createErrorOutput());
	    }
	}
    }

    /**
     * Allows to find component by an item.
     */
    public static class JComboBoxByItemFinder implements ComponentChooser {
	String label;
	int itemIndex;
	StringComparator comparator;
        /**
         * Constructs JComboBoxByItemFinder.
         * @param lb a text pattern
         * @param ii item index to check. If equal to -1, selected item is checked.
         * @param comparator specifies string comparision algorithm.
         */
	public JComboBoxByItemFinder(String lb, int ii, StringComparator comparator) {
	    label = lb;
	    itemIndex = ii;
	    this.comparator = comparator;
	}
        /**
         * Constructs JComboBoxByItemFinder.
         * @param lb a text pattern
         * @param ii item index to check. If equal to -1, selected item is checked.
         */
	public JComboBoxByItemFinder(String lb, int ii) {
            this(lb, ii, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JComboBox) {
                if(label == null) {
                    return(true);
                }
		if(((JComboBox)comp).getModel().getSize() > itemIndex) {
		    int ii = itemIndex;
		    if(ii == -1) {
                        ii = ((JComboBox)comp).getSelectedIndex();
                        if(ii == -1) {
                            return(false);
                        }
		    }
		    return(comparator.equals(((JComboBox)comp).getModel().getElementAt(ii).toString(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JComboBox with text \"" + label + "\" in " + 
		   (new Integer(itemIndex)).toString() + "'th item");
	}
    }

    /**
     * Checks component type.
     */
    public static class JComboBoxFinder extends Finder {
        /**
         * Constructs JComboBoxFinder.
         * @param sf other searching criteria.
         */
	public JComboBoxFinder(ComponentChooser sf) {
            super(JComboBox.class, sf);
	}
        /**
         * Constructs JComboBoxFinder.
         */
	public JComboBoxFinder() {
            super(JComboBox.class);
	}
    }

    private class PopupWindowChooser implements ComponentChooser {
	ComponentChooser pChooser;
	public PopupWindowChooser(ComponentChooser pChooser) {
	    this.pChooser = pChooser;
	}
	public boolean checkComponent(Component comp) {
	    ComponentSearcher cs = new ComponentSearcher((Container)comp);
	    cs.setOutput(TestOut.getNullOutput());
	    return(cs.findComponent(pChooser) != null);
	}
	public String getDescription() {
	    return("Popup window");
	}
    }

    private class ListWater extends Waiter {
	ComponentChooser cChooser;
	ComponentChooser pChooser;
	public ListWater() {
	    super();
	    cChooser = new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    if(comp instanceof JList) {
			Container cont = (Container)comp;
			while((cont = cont.getParent()) != null) {
			    if(cont instanceof ComboPopup) {
				return(true);
			    }
			}
		    }
		    return(false);
		}
		public String getDescription() {
		    return("Popup menu");
		}
	    };
	    pChooser = new PopupWindowChooser(cChooser);
	}
	public Object actionProduced(Object obj) {
	    Window popupWindow = null;
	    if(pChooser.checkComponent(getWindow())) {
		popupWindow = getWindow();
	    } else {
		popupWindow = WindowWaiter.getWindow(getWindow(), pChooser);
	    }
	    if(popupWindow != null) {
		ComponentSearcher sc = new ComponentSearcher(popupWindow);
		sc.setOutput(TestOut.getNullOutput());
		return(sc.findComponent(cChooser));
	    } else {
		return(null);
	    }
	}
	public String getDescription() {
	    return("Wait popup expanded");
	}
    }
}
