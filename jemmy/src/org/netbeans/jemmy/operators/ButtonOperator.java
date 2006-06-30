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
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.ButtonDriver;
import org.netbeans.jemmy.drivers.DriverManager;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionListener;

import java.util.Hashtable;


/**
 *
 * <BR><BR>Timeouts used: <BR>
 * ButtonOperator.PushButtonTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class ButtonOperator extends ComponentOperator
    implements Timeoutable, Outputable {

    /**
     * Identifier for a label property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Label";

    private final static long PUSH_BUTTON_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;

    ButtonDriver driver;

    /**
     * Constructor.
     * @param b The <code>java.awt.Button</code> managed by
     * this instance.
     */
    public ButtonOperator(Button b) {
	super(b);
	driver = DriverManager.getButtonDriver(getClass());
    }

    /**
     * Constructs a ButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public ButtonOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((Button)cont.
             waitSubComponent(new ButtonFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a ButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     */
    public ButtonOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the <code>index+1</code>'th
     * <code>java.awt.Button</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for button.
     * @param text Button text. 
     * @param index Ordinal component index. The first component has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public ButtonOperator(ContainerOperator cont, String text, int index) {
	this((Button)waitComponent(cont, 
					   new ButtonByLabelFinder(text, 
									   cont.getComparator()),
					   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits for a component in a container to show. The component is
     * identified as the first
     * <code>java.awt.Button</code> that shows, lies below
     * the container in the display containment hierarchy,
     * and that has the desired text. Uses cont's timeout and output
     * for waiting and to init this operator.
     * @param cont The operator for a container containing the sought for button.
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public ButtonOperator(ContainerOperator cont, String text) {
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
    public ButtonOperator(ContainerOperator cont, int index) {
	this((Button)
	     waitComponent(cont, 
			   new ButtonFinder(),
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
    public ButtonOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches Button in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @param index Ordinal component index.  The first <code>index</code> is 0.
     * @return Button instance or null if component was not found.
     */
    public static Button findButton(Container cont, ComponentChooser chooser, int index) {
	return((Button)findComponent(cont, new ButtonFinder(chooser), index));
    }

    /**
     * Searches for the first Button in a container.
     * @param cont Container in which to search for the component.  The container
     * lies above the component in the display containment hierarchy.  The containment
     * need not be direct.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation, defining and
     * applying search criteria.
     * @return Button instance or null if component was not found.
     */
    public static Button findButton(Container cont, ComponentChooser chooser) {
	return(findButton(cont, chooser, 0));
    }

    /**
     * Searches Button by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Button instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Button findButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findButton(cont, new ButtonByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches Button by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Button instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Button findButton(Container cont, String text, boolean ce, boolean ccs) {
	return(findButton(cont, text, ce, ccs, 0));
    }

    /**
     * Waits Button in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return Button instance.
     * @throws TimeoutExpiredException
     */
    public static Button waitButton(Container cont, ComponentChooser chooser, int index) {
	return((Button)waitComponent(cont, new ButtonFinder(chooser), index));
    }

    /**
     * Waits 0'th Button in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return Button instance.
     * @throws TimeoutExpiredException
     */
    public static Button waitButton(Container cont, ComponentChooser chooser){
	return(waitButton(cont, chooser, 0));
    }

    /**
     * Waits Button by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Button instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Button waitButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitButton(cont, new ButtonByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits Button by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Button instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Button waitButton(Container cont, String text, boolean ce, boolean ccs) {
	return(waitButton(cont, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("ButtonOperator.PushButtonTimeout", PUSH_BUTTON_TIMEOUT);
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
	    (ButtonDriver)DriverManager.
	    getDriver(DriverManager.BUTTON_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Pushes the button by mouse click.
     * @throws TimeoutExpiredException
     */
    public void push() {
	output.printLine("Push button\n    :" + toStringSource());
	output.printGolden("Push button");
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
     * Press the button by mouse.
     * @throws TimeoutExpiredException
     */
    public void press() {
	output.printLine("Press button\n    :" + toStringSource());
	output.printGolden("Press button");
	driver.press(this);
    }

    /**
     * Releases the button by mouse.
     * @throws TimeoutExpiredException
     */
    public void release() {
	output.printLine("Release button\n    :" + toStringSource());
	output.printGolden("Release button");
	driver.press(this);
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
        if(((Button)getSource()).getLabel() != null) {
            result.put(TEXT_DPROP, ((Button)getSource()).getLabel());
        }
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Button.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((Button)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>Button.getActionCommand()</code> through queue*/
    public String getActionCommand() {
	return((String)runMapping(new MapAction("getActionCommand") {
		public Object map() {
		    return(((Button)getSource()).getActionCommand());
		}}));}

    /**Maps <code>Button.getLabel()</code> through queue*/
    public String getLabel() {
	return((String)runMapping(new MapAction("getLabel") {
		public Object map() {
		    return(((Button)getSource()).getLabel());
		}}));}

    /**Maps <code>Button.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((Button)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>Button.setActionCommand(String)</code> through queue*/
    public void setActionCommand(final String string) {
	runMapping(new MapVoidAction("setActionCommand") {
		public void map() {
		    ((Button)getSource()).setActionCommand(string);
		}});}

    /**Maps <code>Button.setLabel(String)</code> through queue*/
    public void setLabel(final String string) {
	runMapping(new MapVoidAction("setLabel") {
		public void map() {
		    ((Button)getSource()).setLabel(string);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by label.
     */
    public static class ButtonByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs ButtonByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public ButtonByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs ButtonByLabelFinder.
         * @param lb a text pattern
         */
	public ButtonByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Button) {
		if(((Button)comp).getLabel() != null) {
		    return(comparator.equals(((Button)comp).getLabel(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Button with label \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class ButtonFinder extends Finder {
        /**
         * Constructs AbstractButtonFinder.
         * @param sf other searching criteria.
         */
	public ButtonFinder(ComponentChooser sf) {
            super(Button.class, sf);
	}
        /**
         * Constructs AbstractButtonFinder.
         */
	public ButtonFinder() {
            super(Button.class);
	}
    }
}
