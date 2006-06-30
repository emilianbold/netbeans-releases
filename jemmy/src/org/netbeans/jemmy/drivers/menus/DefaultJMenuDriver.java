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
	    return(push(itemOper, null, (oper instanceof JMenuBarOperator) ? ((JMenuBar)oper.getSource()) : null, 
                        chooser, 1, true));
	} else {
	    return(push(oper, null, null, chooser, 0, true));
	}
    }
    protected Object push(ComponentOperator oper, ComponentOperator lastItem,
                          JMenuBar menuBar, 
                          PathChooser chooser, int depth, boolean pressMouse) {
        try {
            oper.waitComponentVisible(true);
            oper.waitComponentEnabled();
        } catch(InterruptedException e) {
            throw(new JemmyException("Interrupted!", e));
        }
	MouseDriver mDriver = DriverManager.getMouseDriver(oper);
        //mDriver.enterMouse(oper);
        //use enhanced algorithm instead
        smartMove(lastItem, oper);
	if(depth > chooser.getDepth() - 1) {
            if(oper instanceof JMenuOperator &&
               menuBar != null && getSelectedElement(menuBar) != null) {
                //mDriver.enterMouse(oper);
            } else {
                DriverManager.getButtonDriver(oper).push(oper);
            }
	    return(oper.getSource());
	}
	if(pressMouse && !((JMenuOperator)oper).isPopupMenuVisible() &&
           !(menuBar != null && getSelectedElement(menuBar) != null)) {
	    DriverManager.getButtonDriver(oper).push(oper);
	}
	oper.getTimeouts().sleep("JMenuOperator.WaitBeforePopupTimeout");
	JMenuItem item = waitItem(oper, waitPopupMenu(oper), chooser, depth);
        mDriver.exitMouse(oper);
	if(item instanceof JMenu) {
	    JMenuOperator mo = new JMenuOperator((JMenu)item);
	    mo.copyEnvironment(oper);
	    return(push(mo, oper, null, chooser, depth + 1, false));
	} else {
	    JMenuItemOperator mio = new JMenuItemOperator(item);
	    mio.copyEnvironment(oper);
            try {
                mio.waitComponentEnabled();
            } catch(InterruptedException e) {
                throw(new JemmyException("Interrupted!", e));
            }
            //move here first
            smartMove(oper, mio);
	    DriverManager.getButtonDriver(oper).push(mio);
	    return(item);
	}
    }
    private void smartMove(ComponentOperator last, ComponentOperator oper) {
        if(last == null) {
            oper.enterMouse();
            return;
        }
        //get all the coordinates first
        //previous item
        long lastXl, lastXr, lastYl, lastYr;
        lastXl = (long)last.getSource().getLocationOnScreen().getX();
        lastXr = lastXl + last.getSource().getWidth();
        lastYl = (long)last.getSource().getLocationOnScreen().getY();
        lastYr = lastYl + last.getSource().getHeight();
        //this item
        long operXl, operXr, operYl, operYr;
        operXl = (long)oper.getSource().getLocationOnScreen().getX();
        operXr = operXl + oper.getSource().getWidth();
        operYl = (long)oper.getSource().getLocationOnScreen().getY();
        operYr = operYl + oper.getSource().getHeight();
        //get the overlap borders
        long overXl, overXr, overYl, overYr;
        overXl = (lastXl > operXl) ? lastXl : operXl;
        overXr = (lastXr < operXr) ? lastXr : operXr;
        overYl = (lastYl > operYl) ? lastYl : operYl;
        overYr = (lastYr < operYr) ? lastYr : operYr;
        //now, let's see ...
        //what if it overlaps by x?
        if(overXl < overXr) {
            //good - move mose to the center of the overlap
            last.moveMouse((int)((overXr - overXl) / 2 - lastXl),
                           last.getCenterY());
            //move mouse inside
            oper.moveMouse((int)((overXr - overXl) / 2 - operXl),
                           oper.getCenterY());
            //done - now move to the center
            oper.enterMouse();
            return;
        }
        //ok, what if it overlaps by y?
        if(overYl < overYr) {
            //good - move mose to the center of the overlap
            last.moveMouse(last.getCenterX(),
                           (int)((overYr - overYl) / 2 - lastYl));
            //move mouse inside
            oper.moveMouse(last.getCenterX(),
                           (int)((overYr - overYl) / 2 - operYl));
            //done - now move to the center
            oper.enterMouse();
            return;
        }
        //well - can't help it
        oper.enterMouse();
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

    public static Object getSelectedElement(JMenuBar bar) {
        MenuElement[] subElements = bar.getSubElements();
        for(int i = 0; i < subElements.length; i++) {
            if(subElements[i] instanceof JMenu &&
               ((JMenu)subElements[i]).isPopupMenuVisible()) {
                return(subElements[i]);
            }
        }
        return(null);
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
                if(((Component)subElements[i]).isShowing() && ((Component)subElements[i]).isEnabled() &&
                        chooser.checkPathComponent(depth, subElements[i])) {
                    return subElements[i];
                }
	    }
	    return(null);
	}
	public String getDescription() {
	    return("");
	}
    }
}
