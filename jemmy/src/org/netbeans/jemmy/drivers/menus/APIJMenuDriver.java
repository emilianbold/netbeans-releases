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

import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

public class APIJMenuDriver extends DefaultJMenuDriver implements MenuDriver {
    public APIJMenuDriver() {
	super();
    }
    protected Object push(ComponentOperator oper, JMenuBar menuBar, 
                          PathChooser chooser, int depth, boolean pressMouse) {
        try {
            oper.waitComponentVisible(true);
            oper.waitComponentEnabled();
        } catch(InterruptedException e) {
            throw(new JemmyException("Interrupted!", e));
        }
	if(depth > chooser.getDepth() - 1) {
            if(oper instanceof JMenuOperator) {
                if(((JMenuOperator)oper).isPopupMenuVisible()) {
                    ((JMenuOperator)oper).setPopupMenuVisible(false);
                }
                ((JMenuOperator)oper).setPopupMenuVisible(true);
                waitPopupMenu(oper);
            }
            ((AbstractButtonOperator)oper).doClick();
	    return(oper.getSource());
	} else {
            if(((JMenuOperator)oper).isPopupMenuVisible()) {
                ((JMenuOperator)oper).setPopupMenuVisible(false);
            }
            ((JMenuOperator)oper).setPopupMenuVisible(true);
            waitPopupMenu(oper);
        }
	oper.getTimeouts().sleep("JMenuOperator.WaitBeforePopupTimeout");
	JMenuItem item = waitItem(oper, waitPopupMenu(oper), chooser, depth);
	if(item instanceof JMenu) {
	    JMenuOperator mo = new JMenuOperator((JMenu)item);
	    mo.copyEnvironment(oper);
	    Object result = push(mo, null, chooser, depth + 1, false);
            if(result instanceof JMenu) {
                org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("IN HERE" + ((JMenu)result).getText());
                org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("IN HERE" + Boolean.toString(((JMenu)result).isPopupMenuVisible()));
                if(!((JMenu)result).isPopupMenuVisible()) {
                    ((JMenuOperator)oper).setPopupMenuVisible(false);
                }
            } else {
                ((JMenuOperator)oper).setPopupMenuVisible(false);
                waitNoPopupMenu(oper);
            }
            return(result);
	} else {
	    JMenuItemOperator mio = new JMenuItemOperator(item);
	    mio.copyEnvironment(oper);
            try {
                mio.waitComponentEnabled();
            } catch(InterruptedException e) {
                throw(new JemmyException("Interrupted!", e));
            }
            ((AbstractButtonOperator)mio).doClick();
            ((JMenuOperator)oper).setPopupMenuVisible(false);
            waitNoPopupMenu(oper);
	    return(item);
	}
    }

    protected void waitNoPopupMenu(final ComponentOperator oper) {
        oper.waitState(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(!((JMenuOperator)oper).isPopupMenuVisible());
                }
                public String getDescription() {
                    return(((JMenuOperator)oper).getText() + "'s popup");
                }
            });
    }

}
