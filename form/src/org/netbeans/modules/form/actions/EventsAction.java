/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form.actions;

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
import com.netbeans.developer.modules.loaders.form.EventsManager;
import com.netbeans.developer.modules.loaders.form.FormEditor;
import com.netbeans.developer.modules.loaders.form.RADComponent;
import com.netbeans.developer.modules.loaders.form.EventsList;
import com.netbeans.developer.modules.loaders.form.RADComponentCookie;

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
  protected Class[] cookieClasses () {
    return new Class[] { RADComponentCookie.class };
  }
  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return org.openide.util.NbBundle.getBundle (EventsAction.class).getString ("ACT_Events");
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
  protected String iconResource () {
    return "/com/netbeans/developer/modules/loaders/form/resources/events.gif"; // NOI18N
  }

  /**
  * Standard perform action extended by actually activated nodes.
  *
  * @param activatedNodes gives array of actually activated nodes.
  */
  protected void performAction (Node[] activatedNodes) {
  }

  /** Returns a JMenuItem that presents the Action, that implements this
  * interface, in a MenuBar.
  * @return the JMenuItem representation for the Action
  */
  public JMenuItem getMenuPresenter() {
    return getPopupPresenter ();
  }

  /** Returns a JMenuItem that presents the Action, that implements this
  * interface, in a Popup Menu.
  * @return the JMenuItem representation for the Action
  */
  public JMenuItem getPopupPresenter() {
    JMenu popupMenu = new org.openide.awt.JMenuPlus (org.openide.util.NbBundle.getBundle (EventsAction.class).getString ("ACT_Events"));
    popupMenu.setEnabled (isEnabled ());
    HelpCtx.setHelpIDString (popupMenu, EventsAction.class.getName ());
    popupMenu.addMenuListener(new MenuListener() {
        Hashtable mapping = new Hashtable ();  // events by menu
        Hashtable mapping2 = new Hashtable (); // handlers by menu
        public void menuSelected(MenuEvent e) {
          JMenu menu = (JMenu)e.getSource ();
          if (menu.getMenuComponentCount () > 0) // [IAN - Patch for Swing 1.1, which throws NullPointerException if removeAll is called on empty uninitialized JMenu]
            menu.removeAll ();
          Node[] nodes = getActivatedNodes ();
          if (nodes.length == 0) return;
          Node n = nodes[0]; // we suppose that one node is activated

          RADComponent radComp = ((RADComponentCookie) n.getCookie(RADComponentCookie.class)).getRADComponent ();
          if (radComp == null) {
            return;
          }            

          EventsList em = radComp.getEventsList ();
          EventsList.EventSet[] setHandlers = em.getEventSets ();
         
          for (int i = 0; i < setHandlers.length; i++) {
            String name = setHandlers[i].getName ();
            JMenu m = new org.openide.awt.JMenuPlus (name.substring (0, 1).toUpperCase () + name.substring (1));
            HelpCtx.setHelpIDString (m, EventsAction.class.getName ());
            menu.add (m);
            boolean eventSetHasHandlers = false;
            EventsList.Event[] events = setHandlers[i].getEvents();
            for (int j = 0; j < events.length; j++) {
              JMenuItem jmi=null;
              if (events[j].getHandlers ().size () == 0) {
                String menuText = java.text.MessageFormat.format (org.openide.util.NbBundle.getBundle (EventsAction.class).getString ("FMT_CTL_EventNoHandlers"),
                                                        new Object[] { events[j].getName () });
                jmi = new JMenuItem (menuText);
              }
              if (events[j].getHandlers ().size () == 1) {
                String menuText = java.text.MessageFormat.format (org.openide.util.NbBundle.getBundle (EventsAction.class).getString ("FMT_CTL_EventOneHandler"),
                                                        new Object[] { events[j].getName (), ((EventsManager.EventHandler) events[j].getHandlers ().get (0)).getName () });
                jmi = new JMenuItem (menuText);
              }
              if (events[j].getHandlers ().size () > 1) {
                String menuText = java.text.MessageFormat.format (org.openide.util.NbBundle.getBundle (EventsAction.class).getString ("FMT_CTL_EventMultipleHandlers"),
                                                        new Object[] { events[j].getName () });
                jmi = new org.openide.awt.JMenuPlus (menuText);
                for (java.util.Iterator it = events[j].getHandlers ().iterator(); it.hasNext(); ) {
                  EventsManager.EventHandler handler = (EventsManager.EventHandler) it.next ();
                  String handlerText = java.text.MessageFormat.format (org.openide.util.NbBundle.getBundle (EventsAction.class).getString ("FMT_CTL_HandlerFromMultiple"),
                                                          new Object[] { handler.getName () });
                  JMenuItem hItem = new JMenuItem (handlerText);
                  hItem.addActionListener (new ActionListener () {
                      public void actionPerformed (ActionEvent evt) {
                        EventsList.Event event = (EventsList.Event)mapping.get (evt.getSource ());
                        String handlerName = (String) mapping2.get(evt.getSource ());
                        if (event != null) {
                          if (handlerName == null)
                            event.gotoEventHandler ();
                          else
                            event.gotoEventHandler (handlerName);
                        }
                      }
                    }
                  );
                  Font hFont = hItem.getFont();
                  hItem.setFont(new Font (hFont.getFontName(), hFont.getStyle() + Font.BOLD, hFont.getSize()));
                  HelpCtx.setHelpIDString (hItem, EventsAction.class.getName ());
                  ((JMenu) jmi).add (hItem);
                  mapping.put(hItem, events[j]);
                  mapping2.put(hItem, handler.getName ());
                }
              }
              HelpCtx.setHelpIDString (jmi, EventsAction.class.getName ());
              if (events[j].getHandlers ().size () > 0 ) {
                eventSetHasHandlers = true;
                Font jmiFont = jmi.getFont();
                jmi.setFont(new Font (jmiFont.getFontName(), jmiFont.getStyle() + Font.BOLD, jmiFont.getSize()));
              }
              m.add (jmi);
              mapping.put (jmi, events[j]);
              jmi.addActionListener (new ActionListener () {
                  public void actionPerformed (ActionEvent evt) {
                    EventsList.Event event = (EventsList.Event)mapping.get (evt.getSource ());
                    if (event != null) {
                      if (event.getHandlers ().size () == 0)
                        event.createDefaultEventHandler ();
                      event.gotoEventHandler ();
                    }
                  }
                }
              );
            }
            if (eventSetHasHandlers) {
              Font mFont = m.getFont();
              m.setFont(new Font (mFont.getFontName(), mFont.getStyle() + Font.BOLD, mFont.getSize()));
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
/*
 * Log
 *  13   Gandalf   1.12        1/7/00   Ian Formanek    I18N
 *  12   Gandalf   1.11        1/5/00   Ian Formanek    NOI18N
 *  11   Gandalf   1.10        11/25/99 Pavel Buzek     support for multiple 
 *       handlers for one event
 *  10   Gandalf   1.9         11/1/99  Pavel Buzek     displying events and 
 *       event groups that have some handler assigned in bold font
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         7/20/99  Jesse Glick     Context help.
 *  7    Gandalf   1.6         6/28/99  Ian Formanek    Fixed positioning 
 *       problems with popup menu
 *  6    Gandalf   1.5         6/27/99  Ian Formanek    FUlly implemented for 
 *       Gandalf
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/20/99  Ian Formanek    FormNodeCookie->RADComponentCookie
 *       
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    Introduced changes done 
 *       in X2 after this class was copied to Gandalf
 *  1    Gandalf   1.0         3/26/99  Ian Formanek    
 * $
 */
