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

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import java.awt.Component;
import java.awt.Container;

import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.event.MenuListener;

/**
 * <BR><BR>Timeouts used: <BR>
 * JMenuOperator.WaitBeforePopupTimeout - time to sleep before popup expanding <BR>
 * JMenuOperator.WaitPopupTimeout - time to wait popup displayed <BR>
 * JMenuItemOperator.PushMenuTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JMenuOperator extends JMenuItemOperator 
implements Outputable, Timeoutable{

    private final static long WAIT_POPUP_TIMEOUT = 10000;
    private final static long WAIT_BEFORE_POPUP_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Constructor.
     */
    public JMenuOperator(JMenu menu) {
	super(menu);
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
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JMenuOperator(ContainerOperator cont, int index) {
	this((JMenu)
	     waitComponent(cont, 
			   new JMenuFinder(ComponentSearcher.
					   getTrueChooser("Any JMenu")),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
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
	super.setOutput(out);
	output = out;
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
     * Pushes menu.
     * @param choosers Array of choosers to find menuItems to push.
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(ComponentChooser[] choosers) {
	return(pushMenu(converChoosers(choosers), true, true));
    }

    /**
     * Executes <code>pushMenu(choosers)</code> in a separate thread.
     * @see #pushMenu(ComponentChooser[])
     */
    public JMenuItem pushMenuNoBlock(ComponentChooser[] choosers) {
	return(pushMenu(converChoosers(choosers), true, false));
    }

    /**
     * Pushes menu.
     * @param names Menu items texts.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     * @return Last pushed JMenuItem.
     */
    public JMenuItem pushMenu(String[] names, boolean ce, boolean ccs) {
	return(pushMenu(names, new DefaultStringComparator(ce, ccs)));
    }

    /**
     * Executes <code>pushMenu(names, ce, ccs)</code> in a separate thread.
     * @see #pushMenu(String[], boolean, boolean)
     */
    public JMenuItem pushMenuNoBlock(String[] names, boolean ce, boolean ccs) {
	return(pushMenuNoBlock(names, new DefaultStringComparator(ce, ccs)));
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
     * @see #pushMenu(String[])
     */
    public JMenuItem pushMenuNoBlock(String[] names) {
	return(pushMenuNoBlock(names, getComparator()));
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
     */
    public JMenuItem pushMenu(String path, String delim, boolean ce, boolean ccs) {
	output.printLine("Pushing " + path + " menu in \n    " + getSource().toString());
	output.printGolden("Pushing " + path + " menu in \n    " + getSource().toString());
	return(pushMenu(parseString(path, delim), new DefaultStringComparator(ce, ccs)));
    }

    /**
     * Executes <code>pushMenu(path, delim, ce, ccs)</code> in a separate thread.
     * @see #pushMenu(String, String, boolean, boolean)
     */
    public JMenuItem pushMenuNoBlock(String path, String delim, boolean ce, boolean ccs) {
	output.printLine("Pushing " + path + " menu in \n    " + getSource().toString());
	output.printGolden("Pushing " + path + " menu in \n    " + getSource().toString());
	return(pushMenuNoBlock(parseString(path, delim), new DefaultStringComparator(ce, ccs)));
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
	output.printLine("Pushing " + path + " menu in \n    " + getSource().toString());
	output.printGolden("Pushing " + path + " menu in \n    " + getSource().toString());
	return(pushMenu(parseString(path, delim)));
    }

    /**
     * Executes <code>pushMenu(path, delim)</code> in a separate thread.
     * @see #pushMenu(String, String)
     */
    public JMenuItem pushMenuNoBlock(String path, String delim) {
	output.printLine("Pushing " + path + " menu in \n    " + getSource().toString());
	output.printGolden("Pushing " + path + " menu in \n    " + getSource().toString());
	return(pushMenuNoBlock(parseString(path, delim)));
    }

    /**
     * Returns information about component.
     */
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
	addToDump(result, "Submenu", items);
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
    public JMenuItem add(final Action action) {
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
    public JMenuItem insert(final Action action, final int i) {
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

    JMenuItem pushMenu(JMenuItemFinder[] choosers, boolean pressMouse, boolean blocking) {
	class JMenuItemWaiter implements Waitable {
	    ComponentSearcher searcher;
	    ComponentChooser chooser;
	    public JMenuItemWaiter(JPopupMenu cont, ComponentChooser chooser) {
		searcher = new ComponentSearcher(cont);
		searcher.setOutput(output.createErrorOutput());
		this.chooser = chooser;
	    }
	    public Object actionProduced(Object obj) {
		return(searcher.findComponent(chooser));
	    }
	    public String getDescription() {
		return("");
	    }
	}
	if(choosers.length == 0) {
	    output.printLine("Pushing menu " + ((JMenu)getSource()).getText());
	    output.printGolden("Pushing menu " + ((JMenu)getSource()).getText());
            if(blocking) {
                push();
            } else {
                pushNoBlock();
            }
	    return((JMenuItem)getSource());
	}
	getEventDispatcher().setOutput(output.createErrorOutput());
	enterMouse();
	if(pressMouse && !isPopupMenuVisible()) {
	    pressMouse();
	}
	timeouts.sleep("JMenuOperator.WaitBeforePopupTimeout");
	output.printLine("Expanding menu " + ((JMenu)getSource()).getText());
	output.printGolden("Expanding menu " + ((JMenu)getSource()).getText());
	JPopupMenu popup = waitPopupMenu();
	Waiter waiter = new Waiter(new JMenuItemWaiter(popup, choosers[0]));
	waiter.setOutput(output.createErrorOutput());
	waiter.setTimeouts(timeouts);
	JMenuItem item = null;
	try {
	    item = (JMenuItem)waiter.waitAction(null);
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	}
	if(item instanceof JMenu) {
	    exitMouse();
	    JMenuItemFinder[] nextChoosers = new JMenuItemFinder[choosers.length - 1];
	    for(int i = 0; i < choosers.length - 1; i++) {
		nextChoosers[i] = choosers[i+1];
	    }
	    JMenuOperator mo = new JMenuOperator((JMenu)item);
	    mo.copyEnvironment(this);
	    return(mo.pushMenu(nextChoosers, false, blocking));
	} else {
	    releaseMouse();
	    exitMouse();
	    JMenuItemOperator mio = new JMenuItemOperator(item);
	    mio.copyEnvironment(this);
	    if(blocking) {
		mio.push();
	    } else {
		mio.pushNoBlock();
	    }
	    return(item);
	}
    }

    private JMenuItemFinder[] converChoosers(ComponentChooser[] choosers) {
	JMenuItemFinder[] realChoosers = new JMenuItemFinder[choosers.length];
	for(int i = 0; i < choosers.length; i++) {
	    realChoosers[i] = new JMenuItemFinder(choosers[i]);
	}
	return(realChoosers);
    }

    private JMenuItem pushMenu(String[] names, StringComparator comparator) {
	return(pushMenu(JMenuItemOperator.createChoosers(names, comparator)));
    }

    private JMenuItem pushMenuNoBlock(String names[], StringComparator comparator) {
	return(pushMenuNoBlock(JMenuItemOperator.createChoosers(names, comparator)));
    }

    private JPopupMenu waitPopupMenu() {
	try {
	    Waiter waiter = (new Waiter(new Waitable() {
		public Object actionProduced(Object obj) {
		    JPopupMenu popup = getPopupMenu();
		    if(popup != null && popup.isShowing()) {
			return(popup);
		    } else {
			return(null);
		    }
		}
		public String getDescription() {
		    return("Popup menu under " + ((JMenu)getSource()).getText() + " menu");
		}
	    }));
	    Timeouts times = timeouts.cloneThis();
	    times.setTimeout("Waiter.WaitingTime",
			     times.getTimeout("JMenuOperator.WaitPopupTimeout"));
	    waiter.setTimeouts(times);
	    return((JPopupMenu)waiter.waitAction(null));
	} catch (InterruptedException e) {
	    return(null);
	}
    }

    private static class JMenuItemFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JMenuItemFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JMenuItem) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }

    private static class JMenuByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public JMenuByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
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
    
    private static class JMenuFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JMenuFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JMenu) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
