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
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.WindowWaiter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SingleSelectionModel;

import javax.swing.event.PopupMenuListener;

import javax.swing.plaf.PopupMenuUI;

/**
 * <BR><BR>Timeouts used: <BR>
 * JMenuOperator.WaitBeforePopupTimeout - time to sleep before popup expanding <BR>
 * JMenuOperator.WaitPopupTimeout - time to wait popup displayed <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * WindowWaiter.WaitWindowTimeout - time to wait popup window displayed <BR>
 * WindowWaiter.AfterWindowTimeout - time to sleep after popup window has been dispayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JPopupMenuOperator extends JComponentOperator
implements Outputable, Timeoutable {

    private TestOut output;
    private Timeouts timeouts;

    /**
     * Constructor.
     */
    public JPopupMenuOperator(JPopupMenu popup) {
	super(popup);
    }

    /**
     * Constructor.
     * Waits component first.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param timeouts
     * @param output
     * @throws TimeoutExpiredException
     */
    public JPopupMenuOperator(Operator env) {
	this((JPopupMenu)
	     waitComponent(WindowOperator.
			   waitWindow(new JPopupWindowFinder(ComponentSearcher.
							     getTrueChooser("Popup window")),
				      0,
				      env.getTimeouts(),
				      env.getOutput()),
			   new JPopupMenuFinder(ComponentSearcher.
						getTrueChooser("Popup")),
			   0,
			   env.getTimeouts(),
			   env.getOutput()));
	copyEnvironment(env);
    }
    
    /**
     * Constructor.
     * Waits component first.
     * @throws TimeoutExpiredException
     */
    public JPopupMenuOperator() {
	this(Operator.getEnvironmentOperator());
    }

    /**
     * Searches JPopupMenu in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return JPopupMenu instance or null if component was not found.
     */
    public static JPopupMenu findJPopupMenu(Container cont, ComponentChooser chooser, int index) {
	return((JPopupMenu)findComponent(cont, new JPopupMenuFinder(chooser), index));
    }

    /**
     * Searches JPopupMenu in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @return JPopupMenu instance or null if component was not found.
     */
    public static JPopupMenu findJPopupMenu(Container cont, ComponentChooser chooser) {
	return(findJPopupMenu(cont, chooser, 0));
    }

    /**
     * Waits JPopupMenu in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return JPopupMenu instance.
     * @throws TimeoutExpiredException
     */
    public static JPopupMenu waitJPopupMenu(Container cont, ComponentChooser chooser, int index) {
	return((JPopupMenu)waitComponent(cont, new JPopupMenuFinder(chooser), index));
    }

    /**
     * Waits JPopupMenu in container.
     * @param cont Container to search component in.
     * @param chooser 
     * @param index Ordinal component index.
     * @return JPopupMenu instance.
     * @throws TimeoutExpiredException
     */
    public static JPopupMenu waitJPopupMenu(Container cont, ComponentChooser chooser) {
	return(waitJPopupMenu(cont, chooser, 0));
    }

    /**
     * Searches for a window which contains JPopupMenu.
     */
    public static Window findJPopupWindow(ComponentChooser chooser) {
	return((new WindowWaiter()).getWindow(new JPopupWindowFinder(chooser)));
    }

    /**
     * Waits for a window which contains JPopupMenu.
     * @throws TimeoutExpiredException
     */
    public static Window waitJPopupWindow(ComponentChooser chooser) {
	try {
	    return((new WindowWaiter()).waitWindow(new JPopupWindowFinder(chooser)));
	} catch(InterruptedException e) {
	    return(null);
	}
    }

    /**
     * Calls popup by clicking on (x, y) point in component.
     * @param comp Component to call popup on.
     * @param mouseButton Mouse button mask to call popup.
     * @throws TimeoutExpiredException
     */
    public static JPopupMenu callPopup(Component comp, int x, int y, int mouseButton) {
	ComponentOperator co = new ComponentOperator(comp);
	co.makeComponentVisible();
	co.clickForPopup(x, y, mouseButton);
	return(findJPopupMenu(waitJPopupWindow(ComponentSearcher.getTrueChooser("Popup menu window")), 
			      ComponentSearcher.getTrueChooser("Popup menu")));
    }

    /**
     * Calls popup by clicking on (x, y) point in component.
     * @param comp Component to call popup on.
     * @see ComponentOperator#getPopupMouseButton()
     * @throws TimeoutExpiredException
     */
    public static JPopupMenu callPopup(Component comp, int x, int y) {
	return(callPopup(comp, x, y, getPopupMouseButton()));
    }

    /**
     * Calls popup by clicking component center.
     * @param comp Component to call popup on.
     * @param mouseButton Mouse button mask to call popup.
     * @throws TimeoutExpiredException
     */
    public static JPopupMenu callPopup(Component comp, int mouseButton) {
	ComponentOperator co = new ComponentOperator(comp);
	co.makeComponentVisible();
	co.clickForPopup(mouseButton);
	return(findJPopupMenu(waitJPopupWindow(ComponentSearcher.getTrueChooser("Popup menu window")), 
			      ComponentSearcher.getTrueChooser("Popup menu")));
    }

    /**
     * Calls popup by clicking component center.
     * @param comp Component to call popup on.
     * @see ComponentOperator#getPopupMouseButton()
     * @throws TimeoutExpiredException
     */
    public static JPopupMenu callPopup(Component comp) {
	return(callPopup(comp, getPopupMouseButton()));
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
     * Defines current timeouts.
     * @param times A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts times) {
	super.setTimeouts(times);
	timeouts = times;
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
     * Pushes menu.
     * @param choosers Array of choosers to find menuItems to push.
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(ComponentChooser[] choosers) {
	return(pushMenu(choosers, true));
    }

    /**
     * Executes <code>pushMenu(choosers)</code> in a separate thread.
     * @see #pushMenu(ComponentChooser[])
     */
    public JMenuItem pushMenuNoBlock(ComponentChooser[] choosers) {
	return(pushMenu(choosers, false));
    }

    /**
     * Pushes menu.
     * @param names Menu items texts.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
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
     * @param names Menu items texts.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
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
	return(pushMenu(parseString(path, delim), ce, ccs));
    }

    /**
     * Executes <code>pushMenu(path, delim, ce, ccs)</code> in a separate thread.
     * @see #pushMenu(String, String, boolean, boolean)
     */
    public JMenuItem pushMenuNoBlock(String path, String delim, boolean ce, boolean ccs) {
	return(pushMenuNoBlock(parseString(path, delim), ce, ccs));
    }

    /**
     * Pushes menu.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, String delim) {
	return(pushMenu(parseString(path, delim)));
    }

    /**
     * Executes <code>pushMenu(path, delim)</code> in a separate thread.
     * @see #pushMenu(String, String)
     */
    public JMenuItem pushMenuNoBlock(String path, String delim) {
	return(pushMenuNoBlock(parseString(path, delim)));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	if(((JPopupMenu)getSource()).getLabel() != null) {
	    result.put("Label", ((JPopupMenu)getSource()).getLabel());
	}
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JPopupMenu.add(String)</code> through queue*/
    public JMenuItem add(final String string) {
	return((JMenuItem)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JPopupMenu)getSource()).add(string));
		}}));}

    /**Maps <code>JPopupMenu.add(Action)</code> through queue*/
    public JMenuItem add(final Action action) {
	return((JMenuItem)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JPopupMenu)getSource()).add(action));
		}}));}

    /**Maps <code>JPopupMenu.add(JMenuItem)</code> through queue*/
    public JMenuItem add(final JMenuItem jMenuItem) {
	return((JMenuItem)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JPopupMenu)getSource()).add(jMenuItem));
		}}));}

    /**Maps <code>JPopupMenu.addPopupMenuListener(PopupMenuListener)</code> through queue*/
    public void addPopupMenuListener(final PopupMenuListener popupMenuListener) {
	runMapping(new MapVoidAction("addPopupMenuListener") {
		public void map() {
		    ((JPopupMenu)getSource()).addPopupMenuListener(popupMenuListener);
		}});}

    /**Maps <code>JPopupMenu.addSeparator()</code> through queue*/
    public void addSeparator() {
	runMapping(new MapVoidAction("addSeparator") {
		public void map() {
		    ((JPopupMenu)getSource()).addSeparator();
		}});}

    /**Maps <code>JPopupMenu.getComponentIndex(Component)</code> through queue*/
    public int getComponentIndex(final Component component) {
	return(runMapping(new MapIntegerAction("getComponentIndex") {
		public int map() {
		    return(((JPopupMenu)getSource()).getComponentIndex(component));
		}}));}

    /**Maps <code>JPopupMenu.getInvoker()</code> through queue*/
    public Component getInvoker() {
	return((Component)runMapping(new MapAction("getInvoker") {
		public Object map() {
		    return(((JPopupMenu)getSource()).getInvoker());
		}}));}

    /**Maps <code>JPopupMenu.getLabel()</code> through queue*/
    public String getLabel() {
	return((String)runMapping(new MapAction("getLabel") {
		public Object map() {
		    return(((JPopupMenu)getSource()).getLabel());
		}}));}

    /**Maps <code>JPopupMenu.getMargin()</code> through queue*/
    public Insets getMargin() {
	return((Insets)runMapping(new MapAction("getMargin") {
		public Object map() {
		    return(((JPopupMenu)getSource()).getMargin());
		}}));}

    /**Maps <code>JPopupMenu.getSelectionModel()</code> through queue*/
    public SingleSelectionModel getSelectionModel() {
	return((SingleSelectionModel)runMapping(new MapAction("getSelectionModel") {
		public Object map() {
		    return(((JPopupMenu)getSource()).getSelectionModel());
		}}));}

    /**Maps <code>JPopupMenu.getSubElements()</code> through queue*/
    public MenuElement[] getSubElements() {
	return((MenuElement[])runMapping(new MapAction("getSubElements") {
		public Object map() {
		    return(((JPopupMenu)getSource()).getSubElements());
		}}));}

    /**Maps <code>JPopupMenu.getUI()</code> through queue*/
    public PopupMenuUI getUI() {
	return((PopupMenuUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JPopupMenu)getSource()).getUI());
		}}));}

    /**Maps <code>JPopupMenu.insert(Component, int)</code> through queue*/
    public void insert(final Component component, final int i) {
	runMapping(new MapVoidAction("insert") {
		public void map() {
		    ((JPopupMenu)getSource()).insert(component, i);
		}});}

    /**Maps <code>JPopupMenu.insert(Action, int)</code> through queue*/
    public void insert(final Action action, final int i) {
	runMapping(new MapVoidAction("insert") {
		public void map() {
		    ((JPopupMenu)getSource()).insert(action, i);
		}});}

    /**Maps <code>JPopupMenu.isBorderPainted()</code> through queue*/
    public boolean isBorderPainted() {
	return(runMapping(new MapBooleanAction("isBorderPainted") {
		public boolean map() {
		    return(((JPopupMenu)getSource()).isBorderPainted());
		}}));}

    /**Maps <code>JPopupMenu.isLightWeightPopupEnabled()</code> through queue*/
    public boolean isLightWeightPopupEnabled() {
	return(runMapping(new MapBooleanAction("isLightWeightPopupEnabled") {
		public boolean map() {
		    return(((JPopupMenu)getSource()).isLightWeightPopupEnabled());
		}}));}

    /**Maps <code>JPopupMenu.menuSelectionChanged(boolean)</code> through queue*/
    public void menuSelectionChanged(final boolean b) {
	runMapping(new MapVoidAction("menuSelectionChanged") {
		public void map() {
		    ((JPopupMenu)getSource()).menuSelectionChanged(b);
		}});}

    /**Maps <code>JPopupMenu.pack()</code> through queue*/
    public void pack() {
	runMapping(new MapVoidAction("pack") {
		public void map() {
		    ((JPopupMenu)getSource()).pack();
		}});}

    /**Maps <code>JPopupMenu.processKeyEvent(KeyEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processKeyEvent(final KeyEvent keyEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processKeyEvent") {
		public void map() {
		    ((JPopupMenu)getSource()).processKeyEvent(keyEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JPopupMenu.processMouseEvent(MouseEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processMouseEvent(final MouseEvent mouseEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processMouseEvent") {
		public void map() {
		    ((JPopupMenu)getSource()).processMouseEvent(mouseEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JPopupMenu.removePopupMenuListener(PopupMenuListener)</code> through queue*/
    public void removePopupMenuListener(final PopupMenuListener popupMenuListener) {
	runMapping(new MapVoidAction("removePopupMenuListener") {
		public void map() {
		    ((JPopupMenu)getSource()).removePopupMenuListener(popupMenuListener);
		}});}

    /**Maps <code>JPopupMenu.setBorderPainted(boolean)</code> through queue*/
    public void setBorderPainted(final boolean b) {
	runMapping(new MapVoidAction("setBorderPainted") {
		public void map() {
		    ((JPopupMenu)getSource()).setBorderPainted(b);
		}});}

    /**Maps <code>JPopupMenu.setInvoker(Component)</code> through queue*/
    public void setInvoker(final Component component) {
	runMapping(new MapVoidAction("setInvoker") {
		public void map() {
		    ((JPopupMenu)getSource()).setInvoker(component);
		}});}

    /**Maps <code>JPopupMenu.setLabel(String)</code> through queue*/
    public void setLabel(final String string) {
	runMapping(new MapVoidAction("setLabel") {
		public void map() {
		    ((JPopupMenu)getSource()).setLabel(string);
		}});}

    /**Maps <code>JPopupMenu.setLightWeightPopupEnabled(boolean)</code> through queue*/
    public void setLightWeightPopupEnabled(final boolean b) {
	runMapping(new MapVoidAction("setLightWeightPopupEnabled") {
		public void map() {
		    ((JPopupMenu)getSource()).setLightWeightPopupEnabled(b);
		}});}

    /**Maps <code>JPopupMenu.setPopupSize(int, int)</code> through queue*/
    public void setPopupSize(final int i, final int i1) {
	runMapping(new MapVoidAction("setPopupSize") {
		public void map() {
		    ((JPopupMenu)getSource()).setPopupSize(i, i1);
		}});}

    /**Maps <code>JPopupMenu.setPopupSize(Dimension)</code> through queue*/
    public void setPopupSize(final Dimension dimension) {
	runMapping(new MapVoidAction("setPopupSize") {
		public void map() {
		    ((JPopupMenu)getSource()).setPopupSize(dimension);
		}});}

    /**Maps <code>JPopupMenu.setSelected(Component)</code> through queue*/
    public void setSelected(final Component component) {
	runMapping(new MapVoidAction("setSelected") {
		public void map() {
		    ((JPopupMenu)getSource()).setSelected(component);
		}});}

    /**Maps <code>JPopupMenu.setSelectionModel(SingleSelectionModel)</code> through queue*/
    public void setSelectionModel(final SingleSelectionModel singleSelectionModel) {
	runMapping(new MapVoidAction("setSelectionModel") {
		public void map() {
		    ((JPopupMenu)getSource()).setSelectionModel(singleSelectionModel);
		}});}

    /**Maps <code>JPopupMenu.setUI(PopupMenuUI)</code> through queue*/
    public void setUI(final PopupMenuUI popupMenuUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JPopupMenu)getSource()).setUI(popupMenuUI);
		}});}

    /**Maps <code>JPopupMenu.show(Component, int, int)</code> through queue*/
    public void show(final Component component, final int i, final int i1) {
	runMapping(new MapVoidAction("show") {
		public void map() {
		    ((JPopupMenu)getSource()).show(component, i, i1);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private JMenuItem pushMenu(ComponentChooser[] choosers, boolean blocking)  {
	JMenuItem menuitem = JMenuItemOperator.waitJMenuItem((JPopupMenu)getSource(), choosers[0]);
	if(menuitem instanceof JMenu) {
	    ComponentChooser[] nextChoosers = new ComponentChooser[choosers.length - 1];
	    for(int i = 0; i < choosers.length - 1; i++) {
		nextChoosers[i] = choosers[i+1];
	    }
	    JMenuOperator mo = new JMenuOperator((JMenu)menuitem);
	    mo.copyEnvironment(this);
	    if(blocking) {
		return(mo.pushMenu(nextChoosers));
	    } else {
		return(mo.pushMenuNoBlock(nextChoosers));
	    }
	} else {
	    output.printLine("Pushing menu item " + menuitem.getText());
	    output.printGolden("Pushing menu item " + menuitem.getText());
	    JMenuItemOperator mio = new JMenuItemOperator(menuitem);
	    mio.copyEnvironment(this);
	    if(blocking) {
		mio.push();
	    } else {
		mio.pushNoBlock();
	    }
	    return(menuitem);
	}
    }

    private JMenuItem pushMenu(String[] names, StringComparator comparator) {
	return(pushMenu(JMenuItemOperator.createChoosers(names, comparator)));
    }

    private JMenuItem pushMenuNoBlock(String[] names, StringComparator comparator) {
	return(pushMenuNoBlock(JMenuItemOperator.createChoosers(names, comparator)));
    }

    private static class JPopupMenuFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JPopupMenuFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    //workaroud for the 4394642 java bug
	    if(comp instanceof JPopupMenu && comp.isShowing() && comp.isVisible()) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }

    private static class JPopupWindowFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JPopupWindowFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp.isShowing() && comp instanceof Window) {
		ComponentSearcher cs = new ComponentSearcher((Container)comp);
		cs.setOutput(JemmyProperties.getCurrentOutput().createErrorOutput());
		return(cs.findComponent(new JPopupMenuFinder(subFinder))
		       != null);
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }

}
