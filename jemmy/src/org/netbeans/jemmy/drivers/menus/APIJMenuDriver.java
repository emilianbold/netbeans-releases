/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
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
