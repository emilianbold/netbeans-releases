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
import java.util.Iterator;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import javax.swing.plaf.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.awt.JMenuPlus;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.FormEventHandlers;
import org.netbeans.modules.form.EventHandler;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.ComponentEventHandlers;
import org.netbeans.modules.form.EventSet;
import org.netbeans.modules.form.Event;
import org.netbeans.modules.form.RADComponentCookie;

/**
 * Events action - subclass of NodeAction - enabled on RADComponents.
 *
 * @author   Ian Formanek
 */

public class EventsAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4498451658201207121L;

    private static ResourceBundle bundle = NbBundle.getBundle(EventsAction.class);
    
    /**
     * @return the mode of action. Possible values are disjunctions of MODE_XXX
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
    
    /**
     * human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return bundle.getString("ACT_Events");
    }

    /**
     * Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EventsAction.class);
    }

    /**
     * Icon resource.
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

    /**
     * Returns a JMenuItem that presents the Action, that implements this
     * interface, in a MenuBar.
     * @return the JMenuItem representation for the Action
     */
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents the Action, that implements this
     * interface, in a Popup Menu.
     * @return the JMenuItem representation for the Action
     */
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenuPlus(bundle.getString("ACT_Events"));
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, EventsAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu =(JMenu) e.getSource();
                generateEventsSubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void generateEventsSubmenu(JMenu menu) {
        if (menu.getMenuComponentCount() > 0)
            menu.removeAll();
        
        Node[] nodes = getActivatedNodes();
        if (nodes.length == 0)
            return;

        Node n = nodes[0]; // we suppose that only one node is activated

        RADComponentCookie radCookie =
            (RADComponentCookie) n.getCookie(RADComponentCookie.class);
        if (radCookie == null)
            return;

        RADComponent metacomp = radCookie.getRADComponent();
        if (metacomp == null)
            return;

        boolean readOnly = metacomp.isReadOnly();
        ComponentEventHandlers em = metacomp.getEventHandlers();
        EventSet[] handlerSets = em.getEventSets();

        for (int i = 0; i < handlerSets.length; i++) {
            JMenu m = null;            
            boolean eventSetHasHandlers = false;
            Event[] events = handlerSets[i].getEvents();
            
            for (int j = 0; j < events.length; j++) {
                JMenuItem jmi = null;
                int handlersCount = events[j].getHandlers().size();
                
                if (handlersCount == 0) {
                    if (!readOnly)
                        jmi = new EventMenuItem(
                            MessageFormat.format(
                                bundle.getString("FMT_CTL_EventNoHandlers"),
                                new Object[] { events[j].getName() }),
                            events[j],
                            null);
                }
                else if (handlersCount == 1) {
                    jmi = new EventMenuItem(
                        MessageFormat.format(
                            bundle.getString("FMT_CTL_EventOneHandler"),
                            new Object[] { events[j].getName(),
                                           ((EventHandler) events[j].getHandlers().get(0)).getName() }),
                        events[j],
                        null);
                }
                else if (handlersCount > 1) {
                    jmi = new JMenuPlus(MessageFormat.format(
                        bundle.getString("FMT_CTL_EventMultipleHandlers"),
                        new Object[] { events[j].getName() }));

                    Iterator iter = events[j].getHandlers().iterator();
                    while (iter.hasNext()) {
                        EventHandler handler = (EventHandler) iter.next();
                        
                        JMenuItem handlerItem = new EventMenuItem(
                            MessageFormat.format(
                                bundle.getString("FMT_CTL_HandlerFromMultiple"),
                                new Object[] { handler.getName() }),
                            events[j],
                            handler.getName());

                        setBoldFontForMenuText(handlerItem);
                        
                        HelpCtx.setHelpIDString(handlerItem, EventsAction.class.getName());
                        ((JMenu) jmi).add(handlerItem);
                        
                        handlerItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                Object source = evt.getSource();
                                if (! (source instanceof EventMenuItem))
                                    return;

                                EventMenuItem mi = (EventMenuItem) source;
                                Event event = mi.getEvent();
                                if (event == null)
                                    return;
                                
                                String handlerName = mi.getHandlerName();
                                if (handlerName == null)
                                    event.gotoEventHandler();
                                else
                                    event.gotoEventHandler(handlerName);
                            }
                        });
                    }
                }

                if (jmi != null) {
                    HelpCtx.setHelpIDString(jmi, EventsAction.class.getName());
                    if (handlersCount > 0) {
                        eventSetHasHandlers = true;
                        if (!readOnly)
                            setBoldFontForMenuText(jmi);
                    }

                    if (m == null) {
                        String name = handlerSets[i].getName();
                        m = new JMenuPlus(name.substring(0, 1).toUpperCase()
                                    + name.substring(1));
            
                        HelpCtx.setHelpIDString(m, EventsAction.class.getName());
                    }
                    m.add(jmi);

                    jmi.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            Object source = evt.getSource();
                            if (! (source instanceof EventMenuItem))
                                return;

                            EventMenuItem mi = (EventMenuItem) source;
                            Event event = mi.getEvent();
                            if (event == null)
                                return;

                            if (event.getHandlers().size() == 0)
                                event.createDefaultEventHandler();
                            else
                                event.gotoEventHandler();
                        }
                    });
                }
            }
            if (eventSetHasHandlers && !readOnly)
                setBoldFontForMenuText(m);
            if (eventSetHasHandlers || !readOnly)
                menu.add(m);
        }
    }

    private static void setBoldFontForMenuText(JMenuItem mi) {
        Font font = mi.getFont();
        mi.setFont(new Font(font.getFontName(),
                            font.getStyle() | Font.BOLD,
                            font.getSize()));
    }
    


    private static class EventMenuItem extends JMenuItem
    {
        private Event event;
        private String handlerName;
        
        EventMenuItem(String text,
                      Event event,
                      String handlerName) {
            super(text);
            this.event = event;
            this.handlerName = handlerName;
        }

        Event getEvent() {
            return event;
        }

        String getHandlerName() {
            return handlerName;
        }
    }
}
