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
import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.drivers.DescriptablePathChooser;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MenuDriver;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.PathChooser;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/**
 * 
 * 100% stable menu driver.
 * Tries to do next steps during one action executed through EventQueue:<br>
 * find showing window containing popup<br>
 * find showing popup<br>
 * find showing menuitem<br>
 * enter mouse into it<br>
 *
 * Repeats this action as many times as "JMenuOperator.WaitPopupTimeout" timeout allows.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */
public class QueueJMenuDriver extends LightSupportiveDriver implements MenuDriver {
    QueueTool queueTool;
    public QueueJMenuDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JMenuOperator", 
                            "org.netbeans.jemmy.operators.JMenuBarOperator", 
                            "org.netbeans.jemmy.operators.JPopupMenuOperator"});
        queueTool = new QueueTool();
    }
    public Object pushMenu(final ComponentOperator oper, PathChooser chooser) {
        queueTool.setOutput(oper.getOutput().createErrorOutput());
	checkSupported(oper);
        JMenuItem result;
        OneReleaseAction action;
	if(oper instanceof JMenuBarOperator) {
            action = new OneReleaseAction(chooser, 0, oper, false) {
                    protected void pushAlone(JMenuItemOperator subMenuOper) {
                        if(subMenuOper.getSource() instanceof JMenu &&
                           isMenuBarSelected((JMenuBar)oper.getSource())) {
                            DriverManager.getMouseDriver(subMenuOper).enterMouse(subMenuOper);
                        } else {
                            DriverManager.getButtonDriver(subMenuOper).push(subMenuOper);
                        }
                    }
                    protected boolean inTheMiddle(JMenuOperator subMenuOper, boolean mousePressed) {
                        if(isMenuBarSelected((JMenuBar)oper.getSource())) {
                            DriverManager.getMouseDriver(subMenuOper).enterMouse(subMenuOper);
                            return(false);
                        } else {
                            return(super.inTheMiddle(subMenuOper, mousePressed));
                        }
                    }
                    protected void process(MenuElement element) {
                        super.process(element);
                    }
                    public MenuElement getMenuElement() {
                        return((MenuElement)oper.getSource());
                    }
                };
        } else if(oper instanceof JPopupMenuOperator) {
            action = new OneReleaseAction(chooser, 0, oper, false) {
                    public MenuElement getMenuElement() {
                        return((MenuElement)oper.getSource());
                    }
                };
	} else {
            DriverManager.getButtonDriver(oper).press(oper);
            action = new OneReleaseAction(chooser, 0, oper, false) {
                    public Object launch() {
                        process((MenuElement)oper.getSource());
                        return((MenuElement)oper.getSource());
                    }
                    public MenuElement getMenuElement() {
                        return(null);
                    }
                };
	}
        //1.5 workaround
        if(System.getProperty("java.specification.version").compareTo("1.4") > 0) {
            queueTool.setOutput(oper.getOutput().createErrorOutput());
            queueTool.waitEmpty(10);
            queueTool.waitEmpty(10);
            queueTool.waitEmpty(10);
        }
        //end of 1.5 workaround
        result = runAction(action, oper, 
                           oper.getTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout"),
                           (chooser instanceof DescriptablePathChooser) ? 
                           ((DescriptablePathChooser)chooser).getDescription() : 
                           "Menu pushing");
        if(result instanceof JMenu) {
            for(int i = 1; i < chooser.getDepth(); i++) {
                final JMenu menu = (JMenu)result;
                final ComponentChooser popupChooser = new PopupMenuChooser(menu);
                action = new OneReleaseAction(chooser, i, oper, action.mousePressed) {
                        public MenuElement getMenuElement() {
                            Window win = JPopupMenuOperator.findJPopupWindow(popupChooser);
                            if(win != null && win.isShowing()) {
                                return(JPopupMenuOperator.findJPopupMenu(win, popupChooser));
                            } else {
                                return(null);
                            }
                        }
                    };
                result = (JMenuItem)runAction(action, oper, 
                                              oper.getTimeouts().getTimeout("JMenuOperator.WaitPopupTimeout"),
                                              (chooser instanceof DescriptablePathChooser) ? 
                                              ((DescriptablePathChooser)chooser).getDescription() : 
                                              "Menu pushing");
            }
        }
        return(result);
    }

    private JMenuItem runAction(final OneReleaseAction action, ComponentOperator env, long waitingTime, final String description) {
        Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    return(queueTool.invokeSmoothly(action));
                }
                public String getDescription() {
                    return(description);
                }
            });
        waiter.setOutput(env.getOutput().createErrorOutput());
        waiter.setTimeouts(env.getTimeouts().cloneThis());
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", 
                                        waitingTime);
        waiter.getTimeouts().setTimeout("Waiter.TimeDelta", 100);
        //1.5 workaround
        if(System.getProperty("java.specification.version").compareTo("1.4") > 0) {
            queueTool.setOutput(env.getOutput().createErrorOutput());
            queueTool.waitEmpty(10);
            queueTool.waitEmpty(10);
            queueTool.waitEmpty(10);
        }
        //end of 1.5 workaround
        try {
            return((JMenuItem)waiter.waitAction(null));
        } catch(InterruptedException e) {
            action.stop();
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

    private abstract class OneReleaseAction extends QueueTool.QueueAction {
        PathChooser chooser;
        int depth;
        ComponentOperator env;
        boolean mousePressed = false;
        private boolean stopped = false;
        public OneReleaseAction(PathChooser chooser, int depth, ComponentOperator env, boolean mousePressed) {
            super("Menu pushing");
            this.chooser = chooser;
            this.depth = depth;
            this.env = env;
            this.mousePressed = mousePressed;
        }
        protected void pushAlone(JMenuItemOperator subMenuOper) {
            DriverManager.getButtonDriver(subMenuOper).push(subMenuOper);
        }
        protected void pushLast(JMenuItemOperator subMenuOper, boolean mousePressed) {
            DriverManager.getMouseDriver(subMenuOper).enterMouse(subMenuOper);
            DriverManager.getButtonDriver(subMenuOper).release(subMenuOper);
        }
        protected boolean inTheMiddle(JMenuOperator subMenuOper, boolean mousePressed) {
            if(!subMenuOper.isPopupMenuVisible()) {
                if(!mousePressed) {
                    DriverManager.getMouseDriver(subMenuOper).enterMouse(subMenuOper);
                    DriverManager.getButtonDriver(subMenuOper).press(subMenuOper);
                } else {
                    DriverManager.getMouseDriver(subMenuOper).enterMouse(subMenuOper);
                }
                return(true);
            }
            return(mousePressed);
        }
        protected void process(MenuElement element) {
            if(depth == chooser.getDepth() - 1) {
                JMenuItemOperator subMenuOper = new JMenuItemOperator((JMenuItem)element);
                subMenuOper.copyEnvironment(env);
                if(depth == 0) {
                    pushAlone(subMenuOper);
                } else {
                    pushLast(subMenuOper, mousePressed);
                }
            } else {
                if(element instanceof JMenu) {
                    JMenuOperator subMenuOper = new JMenuOperator((JMenu)element);
                    subMenuOper.copyEnvironment(env);
                    mousePressed = inTheMiddle(subMenuOper, mousePressed);
                } else {
                    throw(new JemmyException("Menu path too long"));
                }
            }
        }
        public Object launch() {
            MenuElement element = getMenuElement();
            if(element != null) {
                MenuElement[] subElements = element.getSubElements();
                for(int i = 0; i < subElements.length; i++) {
                    if(((Component)subElements[i]).isShowing() && 
                       ((Component)subElements[i]).isEnabled() &&
                       chooser.checkPathComponent(depth, subElements[i])) {
                        process(subElements[i]);
                        return(subElements[i]);
                    }
                    if(stopped) {
                        return(null);
                    }
                }
            }
            return(null);
        }
        public abstract MenuElement getMenuElement();
        private void stop() {
            stopped = true;
        }
    }

    private class PopupMenuChooser implements ComponentChooser {
        JMenu menu;
        public PopupMenuChooser(JMenu menu) {
            this.menu = menu;
        }
        public boolean checkComponent(Component comp) {
            return(comp == menu.getPopupMenu() &&
                   comp.isShowing() && comp.isEnabled());
        }
        public String getDescription() {
            return(menu.getText() + "'s popup");
        }
    }
                        
}
