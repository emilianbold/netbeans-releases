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
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;

import javax.swing.plaf.MenuItemUI;
import org.netbeans.jemmy.util.EmptyVisualizer;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * JMenuItemOperator.PushMenuTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JMenuItemOperator extends AbstractButtonOperator 
implements Timeoutable, Outputable{

    private final static long PUSH_MENU_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Constructor.
     * @param item a component
     */
    public JMenuItemOperator(JMenuItem item) {
	super(item);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    /**
     * Constructs a JMenuItemOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JMenuItemOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JMenuItem)cont.
             waitSubComponent(new JMenuItemFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JMenuItemOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JMenuItemOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JMenuItemOperator(ContainerOperator cont, String text, int index) {
	this((JMenuItem)waitComponent(cont, 
				      new JMenuItemByLabelFinder(text, 
								 cont.getComparator()),
				      index));
	setTimeouts(cont.getTimeouts());
	setOutput(cont.getOutput());
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
    public JMenuItemOperator(ContainerOperator cont, String text) {
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
    public JMenuItemOperator(ContainerOperator cont, int index) {
	this((JMenuItem)
	     waitComponent(cont, 
			   new JMenuItemFinder(),
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
    public JMenuItemOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JMenuItem in container.
     * @param menu Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JMenuItem instance or null if component was not found.
     */
    public static JMenuItem findJMenuItem(Container menu, ComponentChooser chooser, int index) {
	return((JMenuItem)findComponent(menu, new JMenuItemFinder(chooser), index));
    }

    /**
     * Searches 0'th JMenuItem in container.
     * @param menu Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JMenuItem instance or null if component was not found.
     */
    public static JMenuItem findJMenuItem(Container menu, ComponentChooser chooser) {
	return(findJMenuItem(menu, chooser, 0));
    }

    /**
     * Searches JMenuItem by text.
     * @param menu Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JMenuItem instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JMenuItem findJMenuItem(Container menu, String text, boolean ce, boolean ccs, int index) {
	return(findJMenuItem(menu, 
			     new JMenuItemByLabelFinder(text, 
							new DefaultStringComparator(ce, 
										      ccs)), 
			     index));
    }

    /**
     * Searches JMenuItem by text.
     * @param menu Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JMenuItem instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JMenuItem findJMenuItem(Container menu, String text, boolean ce, boolean ccs) {
	return(findJMenuItem(menu, text, ce, ccs, 0));
    }

    /**
     * Waits JMenuItem in container.
     * @param menu Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JMenuItem instance.
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, ComponentChooser chooser, int index) {
	return((JMenuItem)waitComponent(menu, new JMenuItemFinder(chooser), index));
    }

    /**
     * Waits 0'th JMenuItem in container.
     * @param menu Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JMenuItem instance.
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, ComponentChooser chooser) {
	return(waitJMenuItem(menu, chooser, 0));
    }

    /**
     * Waits JMenuItem by text.
     * @param menu Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JMenuItem instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, String text, boolean ce, boolean ccs, int index) {
	return(waitJMenuItem(menu, 
			     new JMenuItemByLabelFinder(text, 
							new DefaultStringComparator(ce, ccs)), 
			     index));
    }

    /**
     * Waits JMenuItem by text.
     * @param menu Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JMenuItem instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, String text, boolean ce, boolean ccs) {
	return(waitJMenuItem(menu, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("JMenuItemOperator.PushMenuTimeout", PUSH_MENU_TIMEOUT);
    }

    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut out) {
	super.setOutput(out);
	output = out;
    }

    public TestOut getOutput() {
	return(output);
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.remove(AbstractButtonOperator.IS_SELECTED_DPROP);
	return(result);
    }
    
    /** Push this menu item. */
    public void push() {
        setVisualizer(new EmptyVisualizer());
        super.push();
    }

    /** Push this menu item and no block further execution. */
    public void pushNoBlock() {
        setVisualizer(new EmptyVisualizer());
        super.pushNoBlock();
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JMenuItem.addMenuDragMouseListener(MenuDragMouseListener)</code> through queue*/
    public void addMenuDragMouseListener(final MenuDragMouseListener menuDragMouseListener) {
	runMapping(new MapVoidAction("addMenuDragMouseListener") {
		public void map() {
		    ((JMenuItem)getSource()).addMenuDragMouseListener(menuDragMouseListener);
		}});}

    /**Maps <code>JMenuItem.addMenuKeyListener(MenuKeyListener)</code> through queue*/
    public void addMenuKeyListener(final MenuKeyListener menuKeyListener) {
	runMapping(new MapVoidAction("addMenuKeyListener") {
		public void map() {
		    ((JMenuItem)getSource()).addMenuKeyListener(menuKeyListener);
		}});}

    /**Maps <code>JMenuItem.getAccelerator()</code> through queue*/
    public KeyStroke getAccelerator() {
	return((KeyStroke)runMapping(new MapAction("getAccelerator") {
		public Object map() {
		    return(((JMenuItem)getSource()).getAccelerator());
		}}));}

    /**Maps <code>JMenuItem.getComponent()</code> through queue*/
    public Component getComponent() {
	return((Component)runMapping(new MapAction("getComponent") {
		public Object map() {
		    return(((JMenuItem)getSource()).getComponent());
		}}));}

    /**Maps <code>JMenuItem.getSubElements()</code> through queue*/
    public MenuElement[] getSubElements() {
	return((MenuElement[])runMapping(new MapAction("getSubElements") {
		public Object map() {
		    return(((JMenuItem)getSource()).getSubElements());
		}}));}

    /**Maps <code>JMenuItem.isArmed()</code> through queue*/
    public boolean isArmed() {
	return(runMapping(new MapBooleanAction("isArmed") {
		public boolean map() {
		    return(((JMenuItem)getSource()).isArmed());
		}}));}

    /**Maps <code>JMenuItem.menuSelectionChanged(boolean)</code> through queue*/
    public void menuSelectionChanged(final boolean b) {
	runMapping(new MapVoidAction("menuSelectionChanged") {
		public void map() {
		    ((JMenuItem)getSource()).menuSelectionChanged(b);
		}});}

    /**Maps <code>JMenuItem.processKeyEvent(KeyEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processKeyEvent(final KeyEvent keyEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processKeyEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processKeyEvent(keyEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuItem.processMenuDragMouseEvent(MenuDragMouseEvent)</code> through queue*/
    public void processMenuDragMouseEvent(final MenuDragMouseEvent menuDragMouseEvent) {
	runMapping(new MapVoidAction("processMenuDragMouseEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processMenuDragMouseEvent(menuDragMouseEvent);
		}});}

    /**Maps <code>JMenuItem.processMenuKeyEvent(MenuKeyEvent)</code> through queue*/
    public void processMenuKeyEvent(final MenuKeyEvent menuKeyEvent) {
	runMapping(new MapVoidAction("processMenuKeyEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processMenuKeyEvent(menuKeyEvent);
		}});}

    /**Maps <code>JMenuItem.processMouseEvent(MouseEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processMouseEvent(final MouseEvent mouseEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processMouseEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processMouseEvent(mouseEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuItem.removeMenuDragMouseListener(MenuDragMouseListener)</code> through queue*/
    public void removeMenuDragMouseListener(final MenuDragMouseListener menuDragMouseListener) {
	runMapping(new MapVoidAction("removeMenuDragMouseListener") {
		public void map() {
		    ((JMenuItem)getSource()).removeMenuDragMouseListener(menuDragMouseListener);
		}});}

    /**Maps <code>JMenuItem.removeMenuKeyListener(MenuKeyListener)</code> through queue*/
    public void removeMenuKeyListener(final MenuKeyListener menuKeyListener) {
	runMapping(new MapVoidAction("removeMenuKeyListener") {
		public void map() {
		    ((JMenuItem)getSource()).removeMenuKeyListener(menuKeyListener);
		}});}

    /**Maps <code>JMenuItem.setAccelerator(KeyStroke)</code> through queue*/
    public void setAccelerator(final KeyStroke keyStroke) {
	runMapping(new MapVoidAction("setAccelerator") {
		public void map() {
		    ((JMenuItem)getSource()).setAccelerator(keyStroke);
		}});}

    /**Maps <code>JMenuItem.setArmed(boolean)</code> through queue*/
    public void setArmed(final boolean b) {
	runMapping(new MapVoidAction("setArmed") {
		public void map() {
		    ((JMenuItem)getSource()).setArmed(b);
		}});}

    /**Maps <code>JMenuItem.setUI(MenuItemUI)</code> through queue*/
    public void setUI(final MenuItemUI menuItemUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JMenuItem)getSource()).setUI(menuItemUI);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Prepares the button to click.
     */
    protected void prepareToClick() {
	output.printLine("Push menu item\n    :" + toStringSource());
	output.printGolden("Push menu item");
	Timeouts times = timeouts.cloneThis();
	times.setTimeout("AbstractButtonOperator.PushButtonTimeout", 
			 timeouts.getTimeout("JMenuItemOperator.PushMenuTimeout"));
	super.setTimeouts(times);
	super.setOutput(output.createErrorOutput());
    }

    static JMenuItemOperator[] getMenuItems(Object[] elements, Operator env) {
        int size = 0;
        for(int i = 0; i < elements.length; i++) {
            if(elements[i] instanceof JMenuItem) {
                size++;
            }
        }
        JMenuItemOperator[] result = new JMenuItemOperator[size];
        int index = 0;
        for(int i = 0; i < elements.length; i++) {
            if(elements[i] instanceof JMenuItem) {
                result[index] = new JMenuItemOperator((JMenuItem)elements[i]);
                result[index].copyEnvironment(env);
                index++;
            }
        }
        return(result);
    }

    static JMenuItemOperator[] getMenuItems(MenuElement parent, Operator env) {
        return(getMenuItems(parent.getSubElements(), env));
    }

    static JMenuItemOperator[] getMenuItems(JMenu parent, Operator env) {
        return(getMenuItems(parent.getMenuComponents(), env));
    }

    static ComponentChooser[] createChoosers(String[] names, StringComparator comparator) {
	ComponentChooser[] choosers = new ComponentChooser[names.length];
	for(int i = 0; i < choosers.length; i++) {
	    choosers[i] = new JMenuItemOperator.JMenuItemByLabelFinder(names[i], comparator);
	}
	return(choosers);
    }

    /**
     * Allows to find component by text.
     */
    public static class JMenuItemByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs JMenuItemByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JMenuItemByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JMenuItemByLabelFinder.
         * @param lb a text pattern
         */
	public JMenuItemByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JMenuItem) {
		if(((JMenuItem)comp).getText() != null) {
		    return(comparator.equals(((JMenuItem)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JMenuItem with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class JMenuItemFinder extends Finder {
        /**
         * Constructs JMenuItemFinder.
         * @param sf other searching criteria.
         */
	public JMenuItemFinder(ComponentChooser sf) {
            super(JMenuItem.class, sf);
	}
        /**
         * Constructs JMenuItemFinder.
         */
	public JMenuItemFinder() {
            super(JMenuItem.class);
	}
    }
}
