/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form.actions;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import javax.swing.plaf.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.EventsManager;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.EventsList;
import org.netbeans.modules.form.RADComponentCookie;

/** Events action - subclass of NodeAction - enabled on RADComponents.
 *
 * @author   Ian Formanek
 */
public class EventsAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4498451658201207121L;

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
     * constants. */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Creates new set of classes that are tested by the cookie.
     *
     * @return list of classes the that the cookie tests
     */
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }
    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(EventsAction.class).getString("ACT_Events");
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EventsAction.class);
    }

    /** Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource() {
        return "/org/netbeans/modules/form/resources/events.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
    }

    /** Returns a JMenuItem that presents the Action, that implements this
     * interface, in a MenuBar.
     * @return the JMenuItem representation for the Action
     */
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /** Returns a JMenuItem that presents the Action, that implements this
     * interface, in a Popup Menu.
     * @return the JMenuItem representation for the Action
     */
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new org.openide.awt.JMenuPlus(org.openide.util.NbBundle.getBundle(EventsAction.class).getString("ACT_Events"));
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, EventsAction.class.getName());
        popupMenu.addMenuListener(new MenuListener() {
            Hashtable mapping = new Hashtable();  // events by menu
            Hashtable mapping2 = new Hashtable(); // handlers by menu
            public void menuSelected(MenuEvent e) {
                JMenu menu =(JMenu)e.getSource();
                if (menu.getMenuComponentCount() > 0) // [IAN - Patch for Swing 1.1, which throws NullPointerException if removeAll is called on empty uninitialized JMenu]
                    menu.removeAll();
                Node[] nodes = getActivatedNodes();
                if (nodes.length == 0) return;
                Node n = nodes[0]; // we suppose that one node is activated

                RADComponentCookie radCookie =
                    (RADComponentCookie) n.getCookie(RADComponentCookie.class);
                if (radCookie == null)
                    return;

                RADComponent radComp = radCookie.getRADComponent();
                if (radComp == null)
                    return;
                boolean readOnly = radComp.readOnly();

                EventsList em = radComp.getEventsList();
                EventsList.EventSet[] setHandlers = em.getEventSets();

                for (int i = 0; i < setHandlers.length; i++) {
                    String name = setHandlers[i].getName();
                    JMenu m = new org.openide.awt.JMenuPlus(name.substring(0, 1).toUpperCase() + name.substring(1));
                    HelpCtx.setHelpIDString(m, EventsAction.class.getName());
                    boolean eventSetHasHandlers = false;
                    EventsList.Event[] events = setHandlers[i].getEvents();
                    for (int j = 0; j < events.length; j++) {
                        JMenuItem jmi=null;
                        if (events[j].getHandlers().size() == 0) {
                            if (!readOnly) {
                                String menuText = java.text.MessageFormat.format(org.openide.util.NbBundle.getBundle(EventsAction.class).getString("FMT_CTL_EventNoHandlers"),
                                                                             new Object[] { events[j].getName() });
                                jmi = new JMenuItem(menuText);
                            }
                        }
                        if (events[j].getHandlers().size() == 1) {
                            String menuText = java.text.MessageFormat.format(org.openide.util.NbBundle.getBundle(EventsAction.class).getString("FMT_CTL_EventOneHandler"),
                                                                             new Object[] { events[j].getName(),((EventsManager.EventHandler) events[j].getHandlers().get(0)).getName() });
                            jmi = new JMenuItem(menuText);
                        }
                        if (events[j].getHandlers().size() > 1) {
                            String menuText = java.text.MessageFormat.format(org.openide.util.NbBundle.getBundle(EventsAction.class).getString("FMT_CTL_EventMultipleHandlers"),
                                                                             new Object[] { events[j].getName() });
                            jmi = new org.openide.awt.JMenuPlus(menuText);
                            for (java.util.Iterator it = events[j].getHandlers().iterator(); it.hasNext();) {
                                EventsManager.EventHandler handler =(EventsManager.EventHandler) it.next();
                                String handlerText = java.text.MessageFormat.format(org.openide.util.NbBundle.getBundle(EventsAction.class).getString("FMT_CTL_HandlerFromMultiple"),
                                                                                    new Object[] { handler.getName() });
                                JMenuItem hItem = new JMenuItem(handlerText);
                                hItem.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent evt) {
                                        EventsList.Event event =(EventsList.Event)mapping.get(evt.getSource());
                                        String handlerName =(String) mapping2.get(evt.getSource());
                                        if (event != null) {
                                            if (handlerName == null)
                                                event.gotoEventHandler();
                                            else
                                                event.gotoEventHandler(handlerName);
                                        }
                                    }
                                }
                                                        );
                                Font hFont = hItem.getFont();
                                hItem.setFont(new Font(hFont.getFontName(), hFont.getStyle() + Font.BOLD, hFont.getSize()));
                                HelpCtx.setHelpIDString(hItem, EventsAction.class.getName());
                                ((JMenu) jmi).add(hItem);
                                mapping.put(hItem, events[j]);
                                mapping2.put(hItem, handler.getName());
                            }
                        }
                        if (jmi != null) {
                            HelpCtx.setHelpIDString(jmi, EventsAction.class.getName());
                            if (events[j].getHandlers().size() > 0) {
                                eventSetHasHandlers = true;
                                if (!readOnly) {
                                    Font jmiFont = jmi.getFont();
                                    jmi.setFont(new Font(jmiFont.getFontName(),
                                                         jmiFont.getStyle() + Font.BOLD,
                                                         jmiFont.getSize()));
                                }
                            }
                            m.add(jmi);
                            mapping.put(jmi, events[j]);
                            jmi.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    EventsList.Event event =(EventsList.Event)mapping.get(evt.getSource());
                                    if (event != null) {
                                        if (event.getHandlers().size() == 0)
                                            event.createDefaultEventHandler();
                                        event.gotoEventHandler();
                                    }
                                }
                            }
                                                  );
                        }
                    }
                    if (eventSetHasHandlers && !readOnly) {
                        Font mFont = m.getFont();
                        m.setFont(new Font(mFont.getFontName(), mFont.getStyle() + Font.BOLD, mFont.getSize()));
                    }
                    if (eventSetHasHandlers || !readOnly) {
                        menu.add(m);
                    }
                }
            }
            public void menuDeselected(MenuEvent e) {
            }
            public void menuCanceled(MenuEvent e) {
            }
        }
                                  );
        return popupMenu;
    }
}
