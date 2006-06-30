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

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.ButtonDriver;
import org.netbeans.jemmy.drivers.DriverManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.ButtonUI;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * AbstractButtonOperator.PushButtonTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for text <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class AbstractButtonOperator extends JComponentOperator
    implements Timeoutable, Outputable{

    /**
     * Identifier for a text property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Text";

    /**
     * Identifier for a selected text property.
     * @see #getDump
     */
    public static final String IS_SELECTED_DPROP = "Selected";

    /**
     * Default value for AbstractButtonOperator.PushButtonTimeout timeout.
     */
    private final static long PUSH_BUTTON_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;

    ButtonDriver driver;

    /**
     * Constructor.
     * @param b The <code>java.awt.AbstractButton</code> managed by
     * this instance.
     */
    public AbstractButtonOperator(AbstractButton b) {
	super(b);
	driver = DriverManager.getButtonDriver(getClass());
    }

    /**
     * Constructs an AbstractButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public AbstractButtonOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((AbstractButton)cont.
             waitSubComponent(new AbstractButtonFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs an AbstractButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     */
    public AbstractButtonOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the <code>index+1</code>'th
     * <code>javax.swing.AbstractButton</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for button.
     * @param text Button text. 
     * @param index Ordinal component index. The first component has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public AbstractButtonOperator(ContainerOperator cont, String text, int index) {
	this((AbstractButton)waitComponent(cont, 
					   new AbstractButtonByLabelFinder(text, 
									   cont.getComparator()),
					   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the first
     * <code>javax.swing.AbstractButton</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for button.
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public AbstractButtonOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for button.
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public AbstractButtonOperator(ContainerOperator cont, int index) {
	this((AbstractButton)
	     waitComponent(cont, 
			   new AbstractButtonFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont The operator for a container containing the sought for button.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public AbstractButtonOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches AbstractButton in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @param index Ordinal component index.  The first <code>index</code> is 0.
     * @return AbstractButton instance or null if component was not found.
     */
    public static AbstractButton findAbstractButton(Container cont, ComponentChooser chooser, int index) {
	return((AbstractButton)findComponent(cont, new AbstractButtonFinder(chooser), index));
    }

    /**
     * Searches for the first AbstractButton in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @return AbstractButton instance or null if component was not found.
     */
    public static AbstractButton findAbstractButton(Container cont, ComponentChooser chooser) {
	return(findAbstractButton(cont, chooser, 0));
    }

    /**
     * Searches AbstractButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return AbstractButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static AbstractButton findAbstractButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findAbstractButton(cont, new AbstractButtonByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches AbstractButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return AbstractButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static AbstractButton findAbstractButton(Container cont, String text, boolean ce, boolean ccs) {
	return(findAbstractButton(cont, text, ce, ccs, 0));
    }

    /**
     * Waits AbstractButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return AbstractButton instance.
     * @throws TimeoutExpiredException
     */
    public static AbstractButton waitAbstractButton(Container cont, ComponentChooser chooser, int index) {
	return((AbstractButton)waitComponent(cont, new AbstractButtonFinder(chooser), index));
    }

    /**
     * Waits 0'th AbstractButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return AbstractButton instance.
     * @throws TimeoutExpiredException
     */
    public static AbstractButton waitAbstractButton(Container cont, ComponentChooser chooser){
	return(waitAbstractButton(cont, chooser, 0));
    }

    /**
     * Waits AbstractButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return AbstractButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static AbstractButton waitAbstractButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitAbstractButton(cont, new AbstractButtonByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits AbstractButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return AbstractButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static AbstractButton waitAbstractButton(Container cont, String text, boolean ce, boolean ccs) {
	return(waitAbstractButton(cont, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("AbstractButtonOperator.PushButtonTimeout", PUSH_BUTTON_TIMEOUT);
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
	driver = DriverManager.getButtonDriver(this);
    }

    /**
     * Pushs the button using a ButtonDriver registered for this operator.
     */
    public void push() {
	output.printLine("Push button\n    :" + toStringSource());
	output.printGolden("Push button");
	makeComponentVisible();
        try {
            waitComponentEnabled();
        } catch(InterruptedException e) {
            throw(new JemmyException("Interrupted", e));
        }
	driver.push(this);
    }

    /**
     * Runs <code>push()</code> method in a separate thread.
     */
    public void pushNoBlock() {
	produceNoBlocking(new NoBlockingAction("Button pushing") {
		public Object doAction(Object param) {
		    push();
		    return(null);
		}
	    });
    }

    /**
     * Changes selection if necessary.
     * Uses <code>push()</code> method in order to do so.
     * @param selected a button selection.
     */
    public void changeSelection(boolean selected) {
	if(isSelected() != selected) {
	    push();
	}
	if(getVerification()) {
            waitSelected(selected);
        }
    }

    /**
     * Runs <code>changeSelection(boolean)</code> method in a separate thread.
     * @param selected a button selection.
     */
    public void changeSelectionNoBlock(boolean selected) {
	produceNoBlocking(new NoBlockingAction("Button selection changing") {
		public Object doAction(Object param) {
		    changeSelection(((Boolean)param).booleanValue());
		    return(null);
		}
	    }, selected ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Press the button by mouse.
     * @throws TimeoutExpiredException
     */
    public void press() {
	output.printLine("Press button\n    :" + toStringSource());
	output.printGolden("Press button");
	makeComponentVisible();
        try {
            waitComponentEnabled();
        } catch(InterruptedException e) {
            throw(new JemmyException("Interrupted", e));
        }
	driver.press(this);
    }

    /**
     * Releases the button by mouse.
     * @throws TimeoutExpiredException
     */
    public void release() {
	output.printLine("Release button\n    :" + toStringSource());
	output.printGolden("Release button");
        try {
            waitComponentEnabled();
        } catch(InterruptedException e) {
            throw(new JemmyException("Interrupted", e));
        }
	driver.release(this);
    }

    /**
     * Waits for button to be selected.
     * @param selected a button selection.
     */
    public void waitSelected(final boolean selected) {
	getOutput().printLine("Wait button to be selected \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait button to be selected");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
                    return(isSelected() == selected);
		}
		public String getDescription() {
		    return("Items has been " + 
			   (selected ? "" : "un") + "selected");
		}
	    });
    }

    /**
     * Waits for text. Uses getComparator() comparator.
     * @param text Text to wait for.
     */
    public void waitText(String text) {
	getOutput().printLine("Wait \"" + text + "\" text in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait \"" + text + "\" text");
	waitState(new AbstractButtonByLabelFinder(text, getComparator()));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
        if(((AbstractButton)getSource()).getText() != null) {
            result.put(TEXT_DPROP, ((AbstractButton)getSource()).getText());
        }
	result.put(IS_SELECTED_DPROP, ((AbstractButton)getSource()).isSelected() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>AbstractButton.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((AbstractButton)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>AbstractButton.addChangeListener(ChangeListener)</code> through queue*/
    public void addChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("addChangeListener") {
		public void map() {
		    ((AbstractButton)getSource()).addChangeListener(changeListener);
		}});}

    /**Maps <code>AbstractButton.addItemListener(ItemListener)</code> through queue*/
    public void addItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("addItemListener") {
		public void map() {
		    ((AbstractButton)getSource()).addItemListener(itemListener);
		}});}

    /**Maps <code>AbstractButton.doClick()</code> through queue*/
    public void doClick() {
	runMapping(new MapVoidAction("doClick") {
		public void map() {
		    ((AbstractButton)getSource()).doClick();
		}});}

    /**Maps <code>AbstractButton.doClick(int)</code> through queue*/
    public void doClick(final int i) {
	runMapping(new MapVoidAction("doClick") {
		public void map() {
		    ((AbstractButton)getSource()).doClick(i);
		}});}

    /**Maps <code>AbstractButton.getActionCommand()</code> through queue*/
    public String getActionCommand() {
	return((String)runMapping(new MapAction("getActionCommand") {
		public Object map() {
		    return(((AbstractButton)getSource()).getActionCommand());
		}}));}

    /**Maps <code>AbstractButton.getDisabledIcon()</code> through queue*/
    public Icon getDisabledIcon() {
	return((Icon)runMapping(new MapAction("getDisabledIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getDisabledIcon());
		}}));}

    /**Maps <code>AbstractButton.getDisabledSelectedIcon()</code> through queue*/
    public Icon getDisabledSelectedIcon() {
	return((Icon)runMapping(new MapAction("getDisabledSelectedIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getDisabledSelectedIcon());
		}}));}

    /**Maps <code>AbstractButton.getHorizontalAlignment()</code> through queue*/
    public int getHorizontalAlignment() {
	return(runMapping(new MapIntegerAction("getHorizontalAlignment") {
		public int map() {
		    return(((AbstractButton)getSource()).getHorizontalAlignment());
		}}));}

    /**Maps <code>AbstractButton.getHorizontalTextPosition()</code> through queue*/
    public int getHorizontalTextPosition() {
	return(runMapping(new MapIntegerAction("getHorizontalTextPosition") {
		public int map() {
		    return(((AbstractButton)getSource()).getHorizontalTextPosition());
		}}));}

    /**Maps <code>AbstractButton.getIcon()</code> through queue*/
    public Icon getIcon() {
	return((Icon)runMapping(new MapAction("getIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getIcon());
		}}));}

    /**Maps <code>AbstractButton.getMargin()</code> through queue*/
    public Insets getMargin() {
	return((Insets)runMapping(new MapAction("getMargin") {
		public Object map() {
		    return(((AbstractButton)getSource()).getMargin());
		}}));}

    /**Maps <code>AbstractButton.getMnemonic()</code> through queue*/
    public int getMnemonic() {
	return(runMapping(new MapIntegerAction("getMnemonic") {
		public int map() {
		    return(((AbstractButton)getSource()).getMnemonic());
		}}));}

    /**Maps <code>AbstractButton.getModel()</code> through queue*/
    public ButtonModel getModel() {
	return((ButtonModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((AbstractButton)getSource()).getModel());
		}}));}

    /**Maps <code>AbstractButton.getPressedIcon()</code> through queue*/
    public Icon getPressedIcon() {
	return((Icon)runMapping(new MapAction("getPressedIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getPressedIcon());
		}}));}

    /**Maps <code>AbstractButton.getRolloverIcon()</code> through queue*/
    public Icon getRolloverIcon() {
	return((Icon)runMapping(new MapAction("getRolloverIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getRolloverIcon());
		}}));}

    /**Maps <code>AbstractButton.getRolloverSelectedIcon()</code> through queue*/
    public Icon getRolloverSelectedIcon() {
	return((Icon)runMapping(new MapAction("getRolloverSelectedIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getRolloverSelectedIcon());
		}}));}

    /**Maps <code>AbstractButton.getSelectedIcon()</code> through queue*/
    public Icon getSelectedIcon() {
	return((Icon)runMapping(new MapAction("getSelectedIcon") {
		public Object map() {
		    return(((AbstractButton)getSource()).getSelectedIcon());
		}}));}

    /**Maps <code>AbstractButton.getSelectedObjects()</code> through queue*/
    public Object[] getSelectedObjects() {
	return((Object[])runMapping(new MapAction("getSelectedObjects") {
		public Object map() {
		    return(((AbstractButton)getSource()).getSelectedObjects());
		}}));}

    /**Maps <code>AbstractButton.getText()</code> through queue*/
    public String getText() {
	return((String)runMapping(new MapAction("getText") {
		public Object map() {
		    return(((AbstractButton)getSource()).getText());
		}}));}

    /**Maps <code>AbstractButton.getUI()</code> through queue*/
    public ButtonUI getUI() {
	return((ButtonUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((AbstractButton)getSource()).getUI());
		}}));}

    /**Maps <code>AbstractButton.getVerticalAlignment()</code> through queue*/
    public int getVerticalAlignment() {
	return(runMapping(new MapIntegerAction("getVerticalAlignment") {
		public int map() {
		    return(((AbstractButton)getSource()).getVerticalAlignment());
		}}));}

    /**Maps <code>AbstractButton.getVerticalTextPosition()</code> through queue*/
    public int getVerticalTextPosition() {
	return(runMapping(new MapIntegerAction("getVerticalTextPosition") {
		public int map() {
		    return(((AbstractButton)getSource()).getVerticalTextPosition());
		}}));}

    /**Maps <code>AbstractButton.isBorderPainted()</code> through queue*/
    public boolean isBorderPainted() {
	return(runMapping(new MapBooleanAction("isBorderPainted") {
		public boolean map() {
		    return(((AbstractButton)getSource()).isBorderPainted());
		}}));}

    /**Maps <code>AbstractButton.isContentAreaFilled()</code> through queue*/
    public boolean isContentAreaFilled() {
	return(runMapping(new MapBooleanAction("isContentAreaFilled") {
		public boolean map() {
		    return(((AbstractButton)getSource()).isContentAreaFilled());
		}}));}

    /**Maps <code>AbstractButton.isFocusPainted()</code> through queue*/
    public boolean isFocusPainted() {
	return(runMapping(new MapBooleanAction("isFocusPainted") {
		public boolean map() {
		    return(((AbstractButton)getSource()).isFocusPainted());
		}}));}

    /**Maps <code>AbstractButton.isRolloverEnabled()</code> through queue*/
    public boolean isRolloverEnabled() {
	return(runMapping(new MapBooleanAction("isRolloverEnabled") {
		public boolean map() {
		    return(((AbstractButton)getSource()).isRolloverEnabled());
		}}));}

    /**Maps <code>AbstractButton.isSelected()</code> through queue*/
    public boolean isSelected() {
	return(runMapping(new MapBooleanAction("isSelected") {
		public boolean map() {
		    return(((AbstractButton)getSource()).isSelected());
		}}));}

    /**Maps <code>AbstractButton.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((AbstractButton)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>AbstractButton.removeChangeListener(ChangeListener)</code> through queue*/
    public void removeChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("removeChangeListener") {
		public void map() {
		    ((AbstractButton)getSource()).removeChangeListener(changeListener);
		}});}

    /**Maps <code>AbstractButton.removeItemListener(ItemListener)</code> through queue*/
    public void removeItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("removeItemListener") {
		public void map() {
		    ((AbstractButton)getSource()).removeItemListener(itemListener);
		}});}

    /**Maps <code>AbstractButton.setActionCommand(String)</code> through queue*/
    public void setActionCommand(final String string) {
	runMapping(new MapVoidAction("setActionCommand") {
		public void map() {
		    ((AbstractButton)getSource()).setActionCommand(string);
		}});}

    /**Maps <code>AbstractButton.setBorderPainted(boolean)</code> through queue*/
    public void setBorderPainted(final boolean b) {
	runMapping(new MapVoidAction("setBorderPainted") {
		public void map() {
		    ((AbstractButton)getSource()).setBorderPainted(b);
		}});}

    /**Maps <code>AbstractButton.setContentAreaFilled(boolean)</code> through queue*/
    public void setContentAreaFilled(final boolean b) {
	runMapping(new MapVoidAction("setContentAreaFilled") {
		public void map() {
		    ((AbstractButton)getSource()).setContentAreaFilled(b);
		}});}

    /**Maps <code>AbstractButton.setDisabledIcon(Icon)</code> through queue*/
    public void setDisabledIcon(final Icon icon) {
	runMapping(new MapVoidAction("setDisabledIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setDisabledIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setDisabledSelectedIcon(Icon)</code> through queue*/
    public void setDisabledSelectedIcon(final Icon icon) {
	runMapping(new MapVoidAction("setDisabledSelectedIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setDisabledSelectedIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setFocusPainted(boolean)</code> through queue*/
    public void setFocusPainted(final boolean b) {
	runMapping(new MapVoidAction("setFocusPainted") {
		public void map() {
		    ((AbstractButton)getSource()).setFocusPainted(b);
		}});}

    /**Maps <code>AbstractButton.setHorizontalAlignment(int)</code> through queue*/
    public void setHorizontalAlignment(final int i) {
	runMapping(new MapVoidAction("setHorizontalAlignment") {
		public void map() {
		    ((AbstractButton)getSource()).setHorizontalAlignment(i);
		}});}

    /**Maps <code>AbstractButton.setHorizontalTextPosition(int)</code> through queue*/
    public void setHorizontalTextPosition(final int i) {
	runMapping(new MapVoidAction("setHorizontalTextPosition") {
		public void map() {
		    ((AbstractButton)getSource()).setHorizontalTextPosition(i);
		}});}

    /**Maps <code>AbstractButton.setIcon(Icon)</code> through queue*/
    public void setIcon(final Icon icon) {
	runMapping(new MapVoidAction("setIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setMargin(Insets)</code> through queue*/
    public void setMargin(final Insets insets) {
	runMapping(new MapVoidAction("setMargin") {
		public void map() {
		    ((AbstractButton)getSource()).setMargin(insets);
		}});}

    /**Maps <code>AbstractButton.setMnemonic(char)</code> through queue*/
    public void setMnemonic(final char c) {
	runMapping(new MapVoidAction("setMnemonic") {
		public void map() {
		    ((AbstractButton)getSource()).setMnemonic(c);
		}});}

    /**Maps <code>AbstractButton.setMnemonic(int)</code> through queue*/
    public void setMnemonic(final int i) {
	runMapping(new MapVoidAction("setMnemonic") {
		public void map() {
		    ((AbstractButton)getSource()).setMnemonic(i);
		}});}

    /**Maps <code>AbstractButton.setModel(ButtonModel)</code> through queue*/
    public void setModel(final ButtonModel buttonModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((AbstractButton)getSource()).setModel(buttonModel);
		}});}

    /**Maps <code>AbstractButton.setPressedIcon(Icon)</code> through queue*/
    public void setPressedIcon(final Icon icon) {
	runMapping(new MapVoidAction("setPressedIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setPressedIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setRolloverEnabled(boolean)</code> through queue*/
    public void setRolloverEnabled(final boolean b) {
	runMapping(new MapVoidAction("setRolloverEnabled") {
		public void map() {
		    ((AbstractButton)getSource()).setRolloverEnabled(b);
		}});}

    /**Maps <code>AbstractButton.setRolloverIcon(Icon)</code> through queue*/
    public void setRolloverIcon(final Icon icon) {
	runMapping(new MapVoidAction("setRolloverIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setRolloverIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setRolloverSelectedIcon(Icon)</code> through queue*/
    public void setRolloverSelectedIcon(final Icon icon) {
	runMapping(new MapVoidAction("setRolloverSelectedIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setRolloverSelectedIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setSelected(boolean)</code> through queue*/
    public void setSelected(final boolean b) {
	runMapping(new MapVoidAction("setSelected") {
		public void map() {
		    ((AbstractButton)getSource()).setSelected(b);
		}});}

    /**Maps <code>AbstractButton.setSelectedIcon(Icon)</code> through queue*/
    public void setSelectedIcon(final Icon icon) {
	runMapping(new MapVoidAction("setSelectedIcon") {
		public void map() {
		    ((AbstractButton)getSource()).setSelectedIcon(icon);
		}});}

    /**Maps <code>AbstractButton.setText(String)</code> through queue*/
    public void setText(final String string) {
	runMapping(new MapVoidAction("setText") {
		public void map() {
		    ((AbstractButton)getSource()).setText(string);
		}});}

    /**Maps <code>AbstractButton.setUI(ButtonUI)</code> through queue*/
    public void setUI(final ButtonUI buttonUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((AbstractButton)getSource()).setUI(buttonUI);
		}});}

    /**Maps <code>AbstractButton.setVerticalAlignment(int)</code> through queue*/
    public void setVerticalAlignment(final int i) {
	runMapping(new MapVoidAction("setVerticalAlignment") {
		public void map() {
		    ((AbstractButton)getSource()).setVerticalAlignment(i);
		}});}

    /**Maps <code>AbstractButton.setVerticalTextPosition(int)</code> through queue*/
    public void setVerticalTextPosition(final int i) {
	runMapping(new MapVoidAction("setVerticalTextPosition") {
		public void map() {
		    ((AbstractButton)getSource()).setVerticalTextPosition(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by text.
     */
    public static class AbstractButtonByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;

        /**
         * Constructs AbstractButtonByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public AbstractButtonByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}

        /**
         * Constructs AbstractButtonByLabelFinder.
         * @param lb a text pattern
         */
	public AbstractButtonByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}

	public boolean checkComponent(Component comp) {
	    if(comp instanceof AbstractButton) {
		if(((AbstractButton)comp).getText() != null) {
		    return(comparator.equals(((AbstractButton)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}

	public String getDescription() {
	    return("AbstractButton with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class AbstractButtonFinder extends Finder {
        /**
         * Constructs AbstractButtonFinder.
         * @param sf other searching criteria.
         */
	public AbstractButtonFinder(ComponentChooser sf) {
            super(AbstractButton.class, sf);
	}
        /**
         * Constructs AbstractButtonFinder.
         */
	public AbstractButtonFinder() {
            super(AbstractButton.class);
	}
    }
}
