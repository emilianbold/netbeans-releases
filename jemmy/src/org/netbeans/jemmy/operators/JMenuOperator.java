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
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.drivers.MenuDriver;
import org.netbeans.jemmy.drivers.DescriptablePathChooser;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.PathChooser;

import java.awt.Component;
import java.awt.Container;

import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.event.MenuListener;

/**
 * <BR><BR>Timeouts used: <BR>
 * JMenuOperator.WaitBeforePopupTimeout - time to sleep before popup expanding <BR>
 * JMenuOperator.WaitPopupTimeout - time to wait popup displayed <BR>
 * JMenuOperator.PushMenuTimeout - time for the whole menu operation<BR>
 * JMenuItemOperator.PushMenuTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JMenuOperator extends JMenuItemOperator 
implements Outputable, Timeoutable{

    /**
     * Identifier for a "submenu" properties.
     * @see #getDump
     */
    public static final String SUBMENU_PREFIX_DPROP = "Submenu";

    private final static long WAIT_POPUP_TIMEOUT = 60000;
    private final static long WAIT_BEFORE_POPUP_TIMEOUT = 0;
    private final static long PUSH_MENU_TIMEOUT = 60000;

    private Timeouts timeouts;
    private TestOut output;
    private MenuDriver driver;

    /**
     * Constructor.
     * @param menu a component
     */
    public JMenuOperator(JMenu menu) {
	super(menu);
	driver = DriverManager.getMenuDriver(this);
    }

    /**
     * Constructs a JMenuOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JMenuOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JMenu)cont.
             waitSubComponent(new JMenuFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JMenuOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JMenuOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JMenuOperator(ContainerOperator cont, String text, int index) {
	this((JMenu)waitComponent(cont, 
				  new JMenuByLabelFinder(text, 
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
    public JMenuOperator(ContainerOperator cont, String text) {
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
    public JMenuOperator(ContainerOperator cont, int index) {
	this((JMenu)
	     waitComponent(cont, 
			   new JMenuFinder(),
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
    public JMenuOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JMenu in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JMenu instance or null if component was not found.
     */
    public static JMenu findJMenu(Container cont, ComponentChooser chooser, int index) {
	return((JMenu)findComponent(cont, new JMenuFinder(chooser), index));
    }

    /**
     * Searches 0'th JMenu in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JMenu instance or null if component was not found.
     */
    public static JMenu findJMenu(Container cont, ComponentChooser chooser) {
	return(findJMenu(cont, chooser, 0));
    }

    /**
     * Searches JMenu by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JMenu instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JMenu findJMenu(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJMenu(cont, 
			 new JMenuByLabelFinder(text, 
						new DefaultStringComparator(ce, ccs)), 
			 index));
    }

    /**
     * Searches JMenu by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JMenu instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JMenu findJMenu(Container cont, String text, boolean ce, boolean ccs) {
	return(findJMenu(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JMenu in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JMenu instance.
     * @throws TimeoutExpiredException
     */
    public static JMenu waitJMenu(final Container cont, final ComponentChooser chooser, final int index) {
	return((JMenu)waitComponent(cont, new JMenuFinder(chooser), index));
    }

    /**
     * Waits 0'th JMenu in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JMenu instance.
     * @throws TimeoutExpiredException
     */
    public static JMenu waitJMenu(Container cont, ComponentChooser chooser) {
	return(waitJMenu(cont, chooser, 0));
    }

    /**
     * Waits JMenu by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JMenu instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JMenu waitJMenu(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJMenu(cont, 
			 new JMenuByLabelFinder(text, 
						new DefaultStringComparator(ce, ccs)), 
			 index));
    }

    /**
     * Waits JMenu by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JMenu instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JMenu waitJMenu(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJMenu(cont, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("JMenuOperator.WaitBeforePopupTimeout", WAIT_BEFORE_POPUP_TIMEOUT);
	Timeouts.initDefault("JMenuOperator.WaitPopupTimeout", WAIT_POPUP_TIMEOUT);
	Timeouts.initDefault("JMenuOperator.PushMenuTimeout", PUSH_MENU_TIMEOUT);
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

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = DriverManager.getMenuDriver(this);
    }

    /**
     * Pushes menu.
     * @param choosers Array of choosers to find menuItems to push.
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(final ComponentChooser[] choosers) {
	return((JMenuItem)produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
                    //TDB 1.5 menu workaround
                    getQueueTool().waitEmpty();
                    Object result = driver.pushMenu(JMenuOperator.this, converChoosers(choosers));
                    getQueueTool().waitEmpty();
		    return(result);
		}
		public String getDescription() {
		    return(createDescription(choosers));
		}
	    }, getTimeouts().getTimeout("JMenuOperator.PushMenuTimeout")));
    }

    /**
     * Executes <code>pushMenu(choosers)</code> in a separate thread.
     * @param choosers Array of choosers to find menuItems to push.
     * @see #pushMenu(ComponentChooser[])
     */
    public void pushMenuNoBlock(final ComponentChooser[] choosers) {
	produceNoBlocking(new NoBlockingAction("Menu pushing") {
		public Object doAction(Object param) {
                    //TDB 1.5 menu workaround
                    getQueueTool().waitEmpty();
                    Object result = driver.pushMenu(JMenuOperator.this, converChoosers(choosers));
                    getQueueTool().waitEmpty();
		    return(result);
		}
	    });
    }

    /**
     * Pushes menu.
     * @param names an array of menu texts.
     * @param comparator a string comparision algorithm
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String[] names, StringComparator comparator) {
	return(pushMenu(JMenuItemOperator.createChoosers(names, comparator)));
    }

    /**
     * Pushes menu.
     * @param names Menu items texts.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     * @return Last pushed JMenuItem.
     * @deprecated Use pushMenu(String[]) or pushMenu(String[], StringComparator)
     */
    public JMenuItem pushMenu(String[] names, boolean ce, boolean ccs) {
	return(pushMenu(names, new DefaultStringComparator(ce, ccs)));
    }

    /**
     * Executes <code>pushMenu(names, ce, ccs)</code> in a separate thread.
     * @param names an array of menu texts.
     * @param comparator a string comparision algorithm
     */
    public void pushMenuNoBlock(String[] names, StringComparator comparator) {
	pushMenuNoBlock(JMenuItemOperator.createChoosers(names, comparator));
    }

    /**
     * Executes <code>pushMenu(names, ce, ccs)</code> in a separate thread.
     * @param names Menu items texts.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see #pushMenu(String[], boolean,boolean)
     * @deprecated Use pushMenuNoBlock(String[]) or pushMenuNoBlock(String[], StringComparator)
     */
    public void pushMenuNoBlock(String[] names, boolean ce, boolean ccs) {
	pushMenuNoBlock(names, new DefaultStringComparator(ce, ccs));
    }

    /**
     * Pushes menu.
     * Uses StringComparator assigned to this object,
     * @param names Menu items texts.
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String[] names) {
	return(pushMenu(names, getComparator()));
    }

    /**
     * Executes <code>pushMenu(names)</code> in a separate thread.
     * @param names Menu items texts.
     * @see #pushMenu(String[])
     */
    public void pushMenuNoBlock(String[] names) {
	pushMenuNoBlock(names, getComparator());
    }

    /**
     * Pushes menu.
     * @param path a menu path.
     * @param delim a path delimiter.
     * @param comparator a string comparision algorithm
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, String delim, StringComparator comparator) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	return(pushMenu(parseString(path, delim), comparator));
    }

    /**
     * Pushes menu. Uses PathParser assigned to this operator.
     * @param path a menu path.
     * @param comparator a string comparision algorithm
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, StringComparator comparator) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	return(pushMenu(parseString(path), comparator));
    }

    /**
     * Pushes menu.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     * @deprecated Use pushMenuNoBlock(String) or pushMenuNoBlock(String, StringComparator)
     */
    public JMenuItem pushMenu(String path, String delim, boolean ce, boolean ccs) {
	return(pushMenu(path, delim, new DefaultStringComparator(ce, ccs)));
    }

    /**
     * Executes <code>pushMenu(names, delim, comparator)</code> in a separate thread.
     * @param path a menu path.
     * @param delim a path delimiter.
     * @param comparator a string comparision algorithm
     */
    public void pushMenuNoBlock(String path, String delim, StringComparator comparator) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	pushMenuNoBlock(parseString(path, delim), comparator);
    }

    /**
     * Executes <code>pushMenu(names, comparator)</code> in a separate thread.
     * Uses PathParser assigned to this operator.
     * @param path a menu path.
     * @param comparator a string comparision algorithm
     */
    public void pushMenuNoBlock(String path, StringComparator comparator) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	pushMenuNoBlock(parseString(path), comparator);
    }

    /**
     * Executes <code>pushMenu(path, delim, ce, ccs)</code> in a separate thread.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see #pushMenu
     * @deprecated Use pushMenuNoBlock(String, String) or pushMenuNoBlock(String, String, StringComparator)
     */
    public void pushMenuNoBlock(String path, String delim, boolean ce, boolean ccs) {
	pushMenuNoBlock(parseString(path, delim), new DefaultStringComparator(ce, ccs));
    }

    /**
     * Pushes menu.
     * Uses StringComparator assigned to this object,
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, String delim) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	return(pushMenu(parseString(path, delim)));
    }

    /**
     * Pushes menu. Uses PathParser assigned to this operator.
     * @param path String menupath representation ("File/New", for example).
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	return(pushMenu(parseString(path)));
    }

    /**
     * Executes <code>pushMenu(path, delim)</code> in a separate thread.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     */
    public void pushMenuNoBlock(String path, String delim) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	pushMenuNoBlock(parseString(path, delim));
    }

    /**
     * Executes <code>pushMenu(path)</code> in a separate thread.
     * @param path String menupath representation ("File/New", for example).
     */
    public void pushMenuNoBlock(String path) {
	output.printLine("Pushing " + path + " menu in \n    " + toStringSource());
	output.printGolden("Pushing " + path + " menu in \n    " + toStringSource());
	pushMenuNoBlock(parseString(path));
    }

    public JMenuItemOperator[] showMenuItems(ComponentChooser[] choosers) {
        return(JMenuItemOperator.getMenuItems((JMenu)pushMenu(choosers), this));
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * @param path an array of menu texts.
     * @param comparator a string comparision algorithm
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String[] path, StringComparator comparator) {
        return(showMenuItems(JMenuItemOperator.createChoosers(path, comparator)));
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * Uses StringComparator assigned to the operator.
     * @param path an array of menu texts.
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String[] path) {
        return(showMenuItems(path, getComparator()));
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * @param path a string identifying the menu path.
     * @param delim a path delimiter.
     * @param comparator a string comparision algorithm
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String path, String delim, StringComparator comparator ) {
        return(showMenuItems(parseString(path, delim), comparator));
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * Uses StringComparator assigned to the operator.
     * @param path a string identifying the menu path.
     * @param delim a path delimiter.
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String path, String delim) {
        return(showMenuItems(path, delim, getComparator()));
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * Uses PathParser assigned to this operator.
     * @param path a string identifying the menu path.
     * @param comparator a string comparision algorithm
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String path, StringComparator comparator ) {
        return(showMenuItems(parseString(path), comparator));
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * Uses PathParser assigned to this operator.
     * Uses StringComparator assigned to the operator.
     * @param path a string identifying the menu path.
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String path) {
        return(showMenuItems(path, getComparator()));
    }

    public JMenuItemOperator showMenuItem(ComponentChooser[] choosers) {
        ComponentChooser[] parentPath = getParentPath(choosers);
        JMenu menu;
        if(parentPath.length > 0) {
            menu = (JMenu)pushMenu(parentPath);
        } else {
            push();
            menu = (JMenu)getSource();
        }
        JPopupMenuOperator popup = new JPopupMenuOperator(menu.getPopupMenu());
        popup.copyEnvironment(this);
        JMenuItemOperator result = new JMenuItemOperator(popup, choosers[choosers.length - 1]);
        result.copyEnvironment(this);
        return(result);
    }

    /**
     * Expends all menus to show menu item specified by a <code>path</code> parameter.
     * @param path an array of menu texts.
     * @param comparator a string comparision algorithm
     * @return an operator for the last menu item in path.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String[] path, StringComparator comparator) {
        String[] parentPath = getParentPath(path);
        JMenu menu;
        if(parentPath.length > 0) {
            menu = (JMenu)pushMenu(parentPath, comparator);
        } else {
            push();
            menu = (JMenu)getSource();
        }
        JPopupMenuOperator popup = new JPopupMenuOperator(menu.getPopupMenu());
        popup.copyEnvironment(this);
        JMenuItemOperator result = new JMenuItemOperator(popup, path[path.length - 1]);
        result.copyEnvironment(this);
        return(result);
    }

    /**
     * Expands all menus to show menu item specified by a <code>path</code> parameter.
     * @param path an array of menu texts.
     * @return an operator for the last menu item in path.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String[] path) {
        return(showMenuItem(path, getComparator()));
    }

    /**
     * Expands all menus to show menu item specified by a <code>path</code> parameter.
     * @param path a string identifying the menu path.
     * @param delim a path delimiter.
     * @param comparator a string comparision algorithm
     * @return an operator for the last menu item in path.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String path, String delim, StringComparator comparator ) {
        return(showMenuItem(parseString(path, delim), comparator));
    }

    /**
     * Expands all menus to show menu item specified by a <code>path</code> parameter.
     * Uses StringComparator assigned to the operator.
     * @param path a string identifying the menu path.
     * @param delim a path delimiter.
     * @return an operator for the last menu item in path.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String path, String delim) {
        return(showMenuItem(path, delim, getComparator()));
    }

    /**
     * Expands all menus to show menu item specified by a <code>path</code> parameter.
     * Uses PathParser assigned to this operator.
     * @param path a string identifying the menu path.
     * @param comparator a string comparision algorithm
     * @return an operator for the last menu item in path.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String path, StringComparator comparator ) {
        return(showMenuItem(parseString(path), comparator));
    }

    /**
     * Expands all menus to show menu item specified by a <code>path</code> parameter.
     * Uses PathParser assigned to this operator.
     * Uses StringComparator assigned to the operator.
     * @param path a string identifying the menu path.
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String path) {
        return(showMenuItem(path, getComparator()));
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	String[] items = new String[((JMenu)getSource()).getItemCount()];
	for(int i = 0; i < ((JMenu)getSource()).getItemCount(); i++) {
	    if(((JMenu)getSource()).getItem(i) != null &&
	       ((JMenu)getSource()).getItem(i).getText() != null) {
		items[i] = ((JMenu)getSource()).getItem(i).getText();
	    } else {
		items[i] = "null";
	    }
	}
	addToDump(result, SUBMENU_PREFIX_DPROP, items);
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JMenu.add(String)</code> through queue*/
    public JMenuItem add(final String string) {
	return((JMenuItem)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JMenu)getSource()).add(string));
		}}));}

    /**Maps <code>JMenu.add(Action)</code> through queue*/
    public JMenuItem add(final javax.swing.Action action) {
	return((JMenuItem)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JMenu)getSource()).add(action));
		}}));}

    /**Maps <code>JMenu.add(JMenuItem)</code> through queue*/
    public JMenuItem add(final JMenuItem jMenuItem) {
	return((JMenuItem)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JMenu)getSource()).add(jMenuItem));
		}}));}

    /**Maps <code>JMenu.addMenuListener(MenuListener)</code> through queue*/
    public void addMenuListener(final MenuListener menuListener) {
	runMapping(new MapVoidAction("addMenuListener") {
		public void map() {
		    ((JMenu)getSource()).addMenuListener(menuListener);
		}});}

    /**Maps <code>JMenu.addSeparator()</code> through queue*/
    public void addSeparator() {
	runMapping(new MapVoidAction("addSeparator") {
		public void map() {
		    ((JMenu)getSource()).addSeparator();
		}});}

    /**Maps <code>JMenu.getDelay()</code> through queue*/
    public int getDelay() {
	return(runMapping(new MapIntegerAction("getDelay") {
		public int map() {
		    return(((JMenu)getSource()).getDelay());
		}}));}

    /**Maps <code>JMenu.getItem(int)</code> through queue*/
    public JMenuItem getItem(final int i) {
	return((JMenuItem)runMapping(new MapAction("getItem") {
		public Object map() {
		    return(((JMenu)getSource()).getItem(i));
		}}));}

    /**Maps <code>JMenu.getItemCount()</code> through queue*/
    public int getItemCount() {
	return(runMapping(new MapIntegerAction("getItemCount") {
		public int map() {
		    return(((JMenu)getSource()).getItemCount());
		}}));}

    /**Maps <code>JMenu.getMenuComponent(int)</code> through queue*/
    public Component getMenuComponent(final int i) {
	return((Component)runMapping(new MapAction("getMenuComponent") {
		public Object map() {
		    return(((JMenu)getSource()).getMenuComponent(i));
		}}));}

    /**Maps <code>JMenu.getMenuComponentCount()</code> through queue*/
    public int getMenuComponentCount() {
	return(runMapping(new MapIntegerAction("getMenuComponentCount") {
		public int map() {
		    return(((JMenu)getSource()).getMenuComponentCount());
		}}));}

    /**Maps <code>JMenu.getMenuComponents()</code> through queue*/
    public Component[] getMenuComponents() {
	return((Component[])runMapping(new MapAction("getMenuComponents") {
		public Object map() {
		    return(((JMenu)getSource()).getMenuComponents());
		}}));}

    /**Maps <code>JMenu.getPopupMenu()</code> through queue*/
    public JPopupMenu getPopupMenu() {
	return((JPopupMenu)runMapping(new MapAction("getPopupMenu") {
		public Object map() {
		    return(((JMenu)getSource()).getPopupMenu());
		}}));}

    /**Maps <code>JMenu.insert(String, int)</code> through queue*/
    public void insert(final String string, final int i) {
	runMapping(new MapVoidAction("insert") {
		public void map() {
		    ((JMenu)getSource()).insert(string, i);
		}});}

    /**Maps <code>JMenu.insert(Action, int)</code> through queue*/
    public JMenuItem insert(final javax.swing.Action action, final int i) {
	return((JMenuItem)runMapping(new MapAction("insert") {
		public Object map() {
		    return(((JMenu)getSource()).insert(action, i));
		}}));}

    /**Maps <code>JMenu.insert(JMenuItem, int)</code> through queue*/
    public JMenuItem insert(final JMenuItem jMenuItem, final int i) {
	return((JMenuItem)runMapping(new MapAction("insert") {
		public Object map() {
		    return(((JMenu)getSource()).insert(jMenuItem, i));
		}}));}

    /**Maps <code>JMenu.insertSeparator(int)</code> through queue*/
    public void insertSeparator(final int i) {
	runMapping(new MapVoidAction("insertSeparator") {
		public void map() {
		    ((JMenu)getSource()).insertSeparator(i);
		}});}

    /**Maps <code>JMenu.isMenuComponent(Component)</code> through queue*/
    public boolean isMenuComponent(final Component component) {
	return(runMapping(new MapBooleanAction("isMenuComponent") {
		public boolean map() {
		    return(((JMenu)getSource()).isMenuComponent(component));
		}}));}

    /**Maps <code>JMenu.isPopupMenuVisible()</code> through queue*/
    public boolean isPopupMenuVisible() {
	return(runMapping(new MapBooleanAction("isPopupMenuVisible") {
		public boolean map() {
		    return(((JMenu)getSource()).isPopupMenuVisible());
		}}));}

    /**Maps <code>JMenu.isTearOff()</code> through queue*/
    public boolean isTearOff() {
	return(runMapping(new MapBooleanAction("isTearOff") {
		public boolean map() {
		    return(((JMenu)getSource()).isTearOff());
		}}));}

    /**Maps <code>JMenu.isTopLevelMenu()</code> through queue*/
    public boolean isTopLevelMenu() {
	return(runMapping(new MapBooleanAction("isTopLevelMenu") {
		public boolean map() {
		    return(((JMenu)getSource()).isTopLevelMenu());
		}}));}

    /**Maps <code>JMenu.remove(JMenuItem)</code> through queue*/
    public void remove(final JMenuItem jMenuItem) {
	runMapping(new MapVoidAction("remove") {
		public void map() {
		    ((JMenu)getSource()).remove(jMenuItem);
		}});}

    /**Maps <code>JMenu.removeMenuListener(MenuListener)</code> through queue*/
    public void removeMenuListener(final MenuListener menuListener) {
	runMapping(new MapVoidAction("removeMenuListener") {
		public void map() {
		    ((JMenu)getSource()).removeMenuListener(menuListener);
		}});}

    /**Maps <code>JMenu.setDelay(int)</code> through queue*/
    public void setDelay(final int i) {
	runMapping(new MapVoidAction("setDelay") {
		public void map() {
		    ((JMenu)getSource()).setDelay(i);
		}});}

    /**Maps <code>JMenu.setMenuLocation(int, int)</code> through queue*/
    public void setMenuLocation(final int i, final int i1) {
	runMapping(new MapVoidAction("setMenuLocation") {
		public void map() {
		    ((JMenu)getSource()).setMenuLocation(i, i1);
		}});}

    /**Maps <code>JMenu.setPopupMenuVisible(boolean)</code> through queue*/
    public void setPopupMenuVisible(final boolean b) {
	runMapping(new MapVoidAction("setPopupMenuVisible") {
		public void map() {
		    ((JMenu)getSource()).setPopupMenuVisible(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    static String createDescription(ComponentChooser[] choosers) {
        String description="(";
        for(int i = 0; i < choosers.length; i++) {
            if(i > 0)
                description = description + ", ";
            description = description + choosers[i].getDescription();
        }
        description = description + ")";
        return("Menu pushing: " + description);
    }

    static DescriptablePathChooser converChoosers(final ComponentChooser[] choosers) {
	return(new DescriptablePathChooser() {
		public boolean checkPathComponent(int depth, Object component) {
		    return(choosers[depth].checkComponent((Component)component));
		}
		public int getDepth() {
		    return(choosers.length);
		}
                public String getDescription() {
                    return(createDescription(choosers));
                }
	    });
    }

    /**
     * Allows to find component by text.
     */
    public static class JMenuByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs JMenuByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JMenuByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JMenuByLabelFinder.
         * @param lb a text pattern
         */
	public JMenuByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JMenu) {
		if(((JMenu)comp).getText() != null) {
		    return(comparator.equals(((JMenu)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JMenu with text \"" + label + "\"");
	}
    }
    
    /**
     * Checks component type.
     */
    public static class JMenuFinder extends Finder {
        /**
         * Constructs JMenuFinder.
         * @param sf other searching criteria.
         */
	public JMenuFinder(ComponentChooser sf) {
            super(JMenu.class, sf);
	}
        /**
         * Constructs JMenuFinder.
         */
	public JMenuFinder() {
            super(JMenu.class);
	}
    }
}
