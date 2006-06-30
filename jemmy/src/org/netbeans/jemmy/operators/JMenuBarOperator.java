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
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.MenuDriver;
import org.netbeans.jemmy.drivers.DriverManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.Hashtable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SingleSelectionModel;

import javax.swing.plaf.MenuBarUI;

/**
 * <BR><BR>Timeouts used: <BR>
 * JMenuOperator.WaitBeforePopupTimeout - time to sleep before popup expanding <BR>
 * JMenuOperator.WaitPopupTimeout - time to wait popup displayed <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JMenuBarOperator extends JComponentOperator
    implements Outputable, Timeoutable {

    /**
     * Identifier for a "submenu" properties.
     * @see #getDump
     */
    public static final String SUBMENU_PREFIX_DPROP = "Submenu";

    private TestOut output;
    private Timeouts timeouts;
    private MenuDriver driver;

    /**
     * Constructor.
     * @param b a component
     */
    public JMenuBarOperator(JMenuBar b) {
	super(b);
	driver = DriverManager.getMenuDriver(getClass());
    }

    /**
     * Constructs a JMenuBarOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JMenuBarOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JMenuBar)cont.
             waitSubComponent(new JMenuBarFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JMenuBarOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JMenuBarOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public JMenuBarOperator(ContainerOperator cont) {
	this((JMenuBar)waitComponent(cont, 
				     new JMenuBarFinder(),
				     0));
	copyEnvironment(cont);
    }

    /**
     * Searches JMenuBar in frame.
     * @param frame a container
     * @return found JMenuBar
     */    
    public static JMenuBar findJMenuBar(JFrame frame) {
	return(findJMenuBar((Container)frame));
    }

    /**
     * Searches JMenuBar in dialog.
     * @param dialog a container
     * @return found JMenuBar
     */    
    public static JMenuBar findJMenuBar(JDialog dialog) {
	return(findJMenuBar((Container)dialog));
    }

    /**
     * Searches JMenuBar in container.
     * @param cont a container
     * @return found JMenuBar
     * @throws TimeoutExpiredException
     */    
    public static JMenuBar waitJMenuBar(Container cont) {
	return((JMenuBar)waitComponent(cont, new JMenuBarFinder()));
    }

    /**
     * Waits JMenuBar in frame.
     * @param frame a container
     * @return found JMenuBar
     * @throws TimeoutExpiredException
     */    
    public static JMenuBar waitJMenuBar(JFrame frame) {
	return(waitJMenuBar((Container)frame));
    }

    /**
     * Waits JMenuBar in dialog.
     * @param dialog a container
     * @return found JMenuBar
     * @throws TimeoutExpiredException
     */    
    public static JMenuBar waitJMenuBar(JDialog dialog) {
	return(waitJMenuBar((Container)dialog));
    }

    /**
     * Waits JMenuBar in container.
     * @param cont a container
     * @return found JMenuBar
     */    
    public static JMenuBar findJMenuBar(Container cont) {
	return((JMenuBar)findComponent(cont, new JMenuBarFinder()));
    }

    static {
        //necessary to init timeouts
        JMenuOperator.class.getName();
    }

    public void setOutput(TestOut out) {
	super.setOutput(out);
	output = out;
    }

    public TestOut getOutput() {
	return(output);
    }

    public void setTimeouts(Timeouts times) {
	super.setTimeouts(times);
	timeouts = times;
    }

    public Timeouts getTimeouts() {
	return(timeouts);
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
        makeComponentVisible();
	return((JMenuItem)produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
                    //TDB 1.5 menu workaround
                    getQueueTool().waitEmpty();
                    Object result = driver.pushMenu(JMenuBarOperator.this, 
                                                    JMenuOperator.converChoosers(choosers));
                    getQueueTool().waitEmpty();
		    return(result);
		}
		public String getDescription() {
		    return(JMenuOperator.createDescription(choosers));
		}
	    }, getTimeouts().getTimeout("JMenuOperator.PushMenuTimeout")));
    }

    /**
     * Executes <code>pushMenu(choosers)</code> in a separate thread.
     * @param choosers Array of choosers to find menuItems to push.
     * @see #pushMenu(ComponentChooser[])
     */
    public void pushMenuNoBlock(final ComponentChooser[] choosers) {
        makeComponentVisible();
	produceNoBlocking(new NoBlockingAction("Menu pushing") {
		public Object doAction(Object param) {
                    //TDB 1.5 menu workaround
                    getQueueTool().waitEmpty();
                    Object result = driver.pushMenu(JMenuBarOperator.this, 
                                                    JMenuOperator.converChoosers(choosers));
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
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
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
     * @deprecated Use pushMenu(String, String) or pushMenu(String, String, StringComparator)
     */
    public JMenuItem pushMenu(String path, String delim, boolean ce, boolean ccs) {
	return(pushMenu(parseString(path, delim), ce, ccs));
    }

    /**
     * Executes <code>pushMenu(names, delim, comparator)</code> in a separate thread.
     * @param path a menu path.
     * @param delim a path delimiter.
     * @param comparator a string comparision algorithm
     */
    public void pushMenuNoBlock(String path, String delim, StringComparator comparator) {
 	pushMenuNoBlock(parseString(path, delim), comparator);
    }
 
    /**
     * Executes <code>pushMenu(names, comparator)</code> in a separate thread.
     * Uses PathParser assigned to this operator.
     * @param path a menu path.
     * @param comparator a string comparision algorithm
     */
    public void pushMenuNoBlock(String path, StringComparator comparator) {
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
	pushMenuNoBlock(parseString(path, delim), ce, ccs);
    }

    /**
     * Pushes menu.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, String delim) {
	return(pushMenu(parseString(path, delim)));
    }

    /**
     * Pushes menu. Uses PathParser assigned to this operator.
     * @param path String menupath representation ("File/New", for example).
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path) {
	return(pushMenu(parseString(path)));
    }

    /**
     * Executes <code>pushMenu(path, delim)</code> in a separate thread.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     */
    public void pushMenuNoBlock(String path, String delim) {
	pushMenuNoBlock(parseString(path, delim));
    }

    /**
     * Executes <code>pushMenu(path)</code> in a separate thread.
     * @param path String menupath representation ("File/New", for example).
     */
    public void pushMenuNoBlock(String path) {
	pushMenuNoBlock(parseString(path));
    }

    public JMenuItemOperator[] showMenuItems(ComponentChooser[] choosers) {
        if(choosers == null || choosers.length == 0) {
            return(JMenuItemOperator.getMenuItems((MenuElement)getSource(), this));
        } else {
            return(JMenuItemOperator.getMenuItems((JMenu)pushMenu(choosers), this));
        }
    }

    /**
     * Shows submenu of menu specified by a <code>path</code> parameter.
     * @param path an array of menu texts.
     * @param comparator a string comparision algorithm
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator[] showMenuItems(String[] path, StringComparator comparator) {
        if(path == null || path.length == 0) {
            return(JMenuItemOperator.getMenuItems((MenuElement)getSource(), this));
        } else {
            return(JMenuItemOperator.getMenuItems((JMenu)pushMenu(path, comparator), this));
        }
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
        ContainerOperator menuCont;
        if(parentPath.length > 0) {
            menu = (JMenu)pushMenu(getParentPath(choosers));
            menuCont = new ContainerOperator(menu.getPopupMenu());
            menuCont.copyEnvironment(this);
        } else {
            menuCont = this;
        }
        JMenuItemOperator result = new JMenuItemOperator(menuCont, choosers[choosers.length - 1]);
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
    public JMenuItemOperator showMenuItem(String[] path, StringComparator comparator ) {
        String[] parentPath = getParentPath(path);
        JMenu menu;
        ContainerOperator menuCont;
        if(parentPath.length > 0) {
            menu = (JMenu)pushMenu(getParentPath(path), comparator);
            menuCont = new ContainerOperator(menu.getPopupMenu());
            menuCont.copyEnvironment(this);
        } else {
            menuCont = this;
        }
        JMenuItemOperator result = new JMenuItemOperator(menuCont, path[path.length - 1]);
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
     * Uses StringComparator assigned to the operator.
     * @param path a string identifying the menu path.
     * @return an array of operators created tor items from the submenu.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator showMenuItem(String path) {
        return(showMenuItem(path, getComparator()));
    }

    /**
     * Closes all expanded submenus.
     */
    public void closeSubmenus() {
        JMenu menu = (JMenu)findSubComponent(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(comp instanceof JMenu &&
                           ((JMenu)comp).isPopupMenuVisible());
                }
                public String getDescription() {
                    return("Expanded JMenu");
                }
            });
        if(menu != null) {
            JMenuOperator oper = new JMenuOperator(menu);
            oper.copyEnvironment(this);
            oper.push();
        }
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	String[] items = new String[((JMenuBar)getSource()).getMenuCount()];
	for(int i = 0; i < ((JMenuBar)getSource()).getMenuCount(); i++) {
	    if(((JMenuBar)getSource()).getMenu(i) != null) {
		items[i] = ((JMenuBar)getSource()).getMenu(i).getText();
	    } else {
		items[i] = "null";
	    }
	}
	addToDump(result, SUBMENU_PREFIX_DPROP, items);
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JMenuBar.add(JMenu)</code> through queue*/
    public JMenu add(final JMenu jMenu) {
	return((JMenu)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JMenuBar)getSource()).add(jMenu));
		}}));}

    /**Maps <code>JMenuBar.getComponentIndex(Component)</code> through queue*/
    public int getComponentIndex(final Component component) {
	return(runMapping(new MapIntegerAction("getComponentIndex") {
		public int map() {
		    return(((JMenuBar)getSource()).getComponentIndex(component));
		}}));}

    /**Maps <code>JMenuBar.getHelpMenu()</code> through queue*/
    public JMenu getHelpMenu() {
	return((JMenu)runMapping(new MapAction("getHelpMenu") {
		public Object map() {
		    return(((JMenuBar)getSource()).getHelpMenu());
		}}));}

    /**Maps <code>JMenuBar.getMargin()</code> through queue*/
    public Insets getMargin() {
	return((Insets)runMapping(new MapAction("getMargin") {
		public Object map() {
		    return(((JMenuBar)getSource()).getMargin());
		}}));}

    /**Maps <code>JMenuBar.getMenu(int)</code> through queue*/
    public JMenu getMenu(final int i) {
	return((JMenu)runMapping(new MapAction("getMenu") {
		public Object map() {
		    return(((JMenuBar)getSource()).getMenu(i));
		}}));}

    /**Maps <code>JMenuBar.getMenuCount()</code> through queue*/
    public int getMenuCount() {
	return(runMapping(new MapIntegerAction("getMenuCount") {
		public int map() {
		    return(((JMenuBar)getSource()).getMenuCount());
		}}));}

    /**Maps <code>JMenuBar.getSelectionModel()</code> through queue*/
    public SingleSelectionModel getSelectionModel() {
	return((SingleSelectionModel)runMapping(new MapAction("getSelectionModel") {
		public Object map() {
		    return(((JMenuBar)getSource()).getSelectionModel());
		}}));}

    /**Maps <code>JMenuBar.getSubElements()</code> through queue*/
    public MenuElement[] getSubElements() {
	return((MenuElement[])runMapping(new MapAction("getSubElements") {
		public Object map() {
		    return(((JMenuBar)getSource()).getSubElements());
		}}));}

    /**Maps <code>JMenuBar.getUI()</code> through queue*/
    public MenuBarUI getUI() {
	return((MenuBarUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JMenuBar)getSource()).getUI());
		}}));}

    /**Maps <code>JMenuBar.isBorderPainted()</code> through queue*/
    public boolean isBorderPainted() {
	return(runMapping(new MapBooleanAction("isBorderPainted") {
		public boolean map() {
		    return(((JMenuBar)getSource()).isBorderPainted());
		}}));}

    /**Maps <code>JMenuBar.isSelected()</code> through queue*/
    public boolean isSelected() {
	return(runMapping(new MapBooleanAction("isSelected") {
		public boolean map() {
		    return(((JMenuBar)getSource()).isSelected());
		}}));}

    /**Maps <code>JMenuBar.menuSelectionChanged(boolean)</code> through queue*/
    public void menuSelectionChanged(final boolean b) {
	runMapping(new MapVoidAction("menuSelectionChanged") {
		public void map() {
		    ((JMenuBar)getSource()).menuSelectionChanged(b);
		}});}

    /**Maps <code>JMenuBar.processKeyEvent(KeyEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processKeyEvent(final KeyEvent keyEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processKeyEvent") {
		public void map() {
		    ((JMenuBar)getSource()).processKeyEvent(keyEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuBar.processMouseEvent(MouseEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processMouseEvent(final MouseEvent mouseEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processMouseEvent") {
		public void map() {
		    ((JMenuBar)getSource()).processMouseEvent(mouseEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuBar.setBorderPainted(boolean)</code> through queue*/
    public void setBorderPainted(final boolean b) {
	runMapping(new MapVoidAction("setBorderPainted") {
		public void map() {
		    ((JMenuBar)getSource()).setBorderPainted(b);
		}});}

    /**Maps <code>JMenuBar.setHelpMenu(JMenu)</code> through queue*/
    public void setHelpMenu(final JMenu jMenu) {
	runMapping(new MapVoidAction("setHelpMenu") {
		public void map() {
		    ((JMenuBar)getSource()).setHelpMenu(jMenu);
		}});}

    /**Maps <code>JMenuBar.setMargin(Insets)</code> through queue*/
    public void setMargin(final Insets insets) {
	runMapping(new MapVoidAction("setMargin") {
		public void map() {
		    ((JMenuBar)getSource()).setMargin(insets);
		}});}

    /**Maps <code>JMenuBar.setSelected(Component)</code> through queue*/
    public void setSelected(final Component component) {
	runMapping(new MapVoidAction("setSelected") {
		public void map() {
		    ((JMenuBar)getSource()).setSelected(component);
		}});}

    /**Maps <code>JMenuBar.setSelectionModel(SingleSelectionModel)</code> through queue*/
    public void setSelectionModel(final SingleSelectionModel singleSelectionModel) {
	runMapping(new MapVoidAction("setSelectionModel") {
		public void map() {
		    ((JMenuBar)getSource()).setSelectionModel(singleSelectionModel);
		}});}

    /**Maps <code>JMenuBar.setUI(MenuBarUI)</code> through queue*/
    public void setUI(final MenuBarUI menuBarUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JMenuBar)getSource()).setUI(menuBarUI);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Checks component type.
     */
    public static class JMenuBarFinder extends Finder {
        /**
         * Constructs JMenuBarFinder.
         * @param sf other searching criteria.
         */
	public JMenuBarFinder(ComponentChooser sf) {
            super(JMenuBar.class, sf);
	}
        /**
         * Constructs JMenuBarFinder.
         */
	public JMenuBarFinder() {
            super(JMenuBar.class);
	}
    }

}
