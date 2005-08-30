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
        if(System.getProperty("java.version").startsWith("1.5")) {
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
        if(System.getProperty("java.version").startsWith("1.5")) {
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
