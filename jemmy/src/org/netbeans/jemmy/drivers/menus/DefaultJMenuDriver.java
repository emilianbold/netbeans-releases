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

package org.netbeans.jemmy.drivers.menus;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MenuDriver;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.PathChooser;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

public class DefaultJMenuDriver extends LightSupportiveDriver implements MenuDriver {
    public DefaultJMenuDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JMenuOperator", 
                            "org.netbeans.jemmy.operators.JMenuBarOperator", 
                            "org.netbeans.jemmy.operators.JPopupMenuOperator"});
    }
    public Object pushMenu(ComponentOperator oper, PathChooser chooser) {
	checkSupported(oper);
	if(oper instanceof JMenuBarOperator ||
	   oper instanceof JPopupMenuOperator) {
	    JMenuItem item;
	    if(oper instanceof JMenuBarOperator) {
		item = waitItem(oper, 
				(JMenuBar)oper.getSource(), 
				chooser, 0);
	    } else {
		item = waitItem(oper, 
				(JPopupMenu)oper.getSource(), 
				chooser, 0);
	    }
	    JMenuItemOperator itemOper;
	    if(item instanceof JMenu) {
		itemOper = new JMenuOperator((JMenu)item);
	    } else if(item instanceof JMenuItem) {
		itemOper = new JMenuItemOperator(item);
	    } else {
		return(null);
	    }
	    itemOper.copyEnvironment(oper);
	    return(push(itemOper, (oper instanceof JMenuBarOperator) ? ((JMenuBar)oper.getSource()) : null, 
                        chooser, 1, true));
	} else {
	    return(push(oper, null, chooser, 0, true));
	}
    }
    protected Object push(ComponentOperator oper, JMenuBar menuBar, 
                          PathChooser chooser, int depth, boolean pressMouse) {
        try {
            oper.waitComponentVisible(true);
            oper.waitComponentEnabled();
        } catch(InterruptedException e) {
            throw(new JemmyException("Interrupted!", e));
        }
	MouseDriver mDriver = DriverManager.getMouseDriver(oper);
        mDriver.enterMouse(oper);
	if(depth > chooser.getDepth() - 1) {
            if(oper instanceof JMenuOperator &&
               menuBar != null && isMenuBarSelected(menuBar)) {
                //mDriver.enterMouse(oper);
            } else {
                DriverManager.getButtonDriver(oper).push(oper);
            }
	    return(oper.getSource());
	}
	if(pressMouse && !((JMenuOperator)oper).isPopupMenuVisible() &&
           !(menuBar != null && isMenuBarSelected(menuBar))) {
	    DriverManager.getButtonDriver(oper).push(oper);
	}
	oper.getTimeouts().sleep("JMenuOperator.WaitBeforePopupTimeout");
	JMenuItem item = waitItem(oper, waitPopupMenu(oper), chooser, depth);
        mDriver.exitMouse(oper);
	if(item instanceof JMenu) {
	    JMenuOperator mo = new JMenuOperator((JMenu)item);
	    mo.copyEnvironment(oper);
	    return(push(mo, null, chooser, depth + 1, false));
	} else {
	    JMenuItemOperator mio = new JMenuItemOperator(item);
	    mio.copyEnvironment(oper);
            try {
                mio.waitComponentEnabled();
            } catch(InterruptedException e) {
                throw(new JemmyException("Interrupted!", e));
            }
	    DriverManager.getButtonDriver(oper).push(mio);
	    return(item);
	}
    }
    protected JPopupMenu waitPopupMenu(final ComponentOperator oper) {
        return((JPopupMenu)JPopupMenuOperator.waitJPopupMenu(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(comp == ((JMenuOperator)oper).getPopupMenu() &&
                           comp.isShowing());
                }
                public String getDescription() {
                    return(((JMenuOperator)oper).getText() + "'s popup");
                }
            }).getSource());
    }

    protected JMenuItem waitItem(ComponentOperator oper, MenuElement element, PathChooser chooser, int depth) {
	Waiter waiter = new Waiter(new JMenuItemWaiter(element, chooser, depth));
	waiter.setOutput(oper.getOutput().createErrorOutput());
	waiter.setTimeouts(oper.getTimeouts());
	try {
	    return((JMenuItem)waiter.waitAction(null));
	} catch(InterruptedException e) {
	    throw(new JemmyException("Waiting has been interrupted", e));
	}
    }

    private boolean isMenuBarSelected(JMenuBar bar) {
        MenuElement[] subElements = bar.getSubElements();
        for(int i = 0; i < subElements.length; i++) {
            if(subElements[i] instanceof JMenu &&
               ((JMenu)subElements[i]).isPopupMenuVisible()) {
                return(true);
            }
        }
        return(false);
    }

    private class JMenuItemWaiter implements Waitable {
	MenuElement cont;
	PathChooser chooser;
	int depth;
	public JMenuItemWaiter(MenuElement cont, PathChooser chooser, int depth) {
	    this.cont = cont;
	    this.chooser = chooser;
	    this.depth = depth;
	}
	public Object actionProduced(Object obj) {
            if(!((Component)cont).isShowing()) {
                return(null);
            }
	    MenuElement[] subElements = cont.getSubElements();
	    for(int i = 0; i < subElements.length; i++) {
                if(!((Component)subElements[i]).isShowing()) {
                    //not all items above have already been displayed - return
                    return(null);
                }
		if(chooser.checkPathComponent(depth, subElements[i])) {
		    return(subElements[i]);
		}
	    }
	    return(null);
	}
	public String getDescription() {
	    return("");
	}
    }
}
