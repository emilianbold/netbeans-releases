/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */
 
package org.apache.tools.ant.module.nodes;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.run.TargetExecutor;

/** Submenu to run certain targets in a project.
 */
public class RunTargetsAction extends CookieAction implements Presenter.Popup {

    public String getName () {
        return NbBundle.getMessage (RunTargetsAction.class, "LBL_run_targets_action");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.executing-target"); // NOI18N
    }

    public JMenuItem getPopupPresenter() {
        return new SpecialSubMenu (this, new ActSubMenuModel (this), true);
    }

    protected int mode () {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses () {
        return new Class[] { AntProjectCookie.class };
    }
    
    protected void performAction (Node[] activatedNodes) {
        // do nothing, should not happen
    }
    
    /** Special submenu which notifies model when it is added as a component.
    */
    private static final class SpecialSubMenu extends Actions.SubMenu {

        private final ActSubMenuModel model;

        SpecialSubMenu (SystemAction action, ActSubMenuModel model, boolean popup) {
            super (action, model, popup);
            this.model = model;
        }

        public void addNotify () {
            model.addNotify ();
            super.addNotify ();
        }

        // removeNotify not useful--might be called before action is invoked

    }

    /** Model to use for the submenu.
    */
    private static final class ActSubMenuModel implements Actions.SubMenuModel {

        private List targets = null; // List<String>
        private AntProjectCookie project = null;

        //private Set listeners = new HashSet (); // Set<ChangeListener>
        
        private final NodeAction action;
        
        ActSubMenuModel (NodeAction action) {
            this.action = action;
        }

        public int getCount () {
            // Apparently when >1 Ant script is selected and you right-click,
            // it gets here though targets==null (as it should since the action
            // should not be enabled!). Not clear why this happens.
            if (targets == null) return 0;
            return targets.size ();
        }

        public String getLabel (int index) {
            return (String) targets.get (index);
        }

        public HelpCtx getHelpCtx (int index) {
            return new HelpCtx ("org.apache.tools.ant.module.executing-target"); // NOI18N
        }

        public void performActionAt (int index) {
            String target = (String) targets.get (index);
            try {
                new TargetExecutor (project, new String[] { target }).execute ();
            } catch (IOException ioe) {
                AntModule.err.notify (ioe);
            }
        }

        void addNotify () {
            project = null;
            targets = null;
            Node[] nodes = action.getActivatedNodes ();
            if (nodes.length != 1) return;
            project = (AntProjectCookie) nodes[0].getCookie (AntProjectCookie.class);
            if (project == null) return;
            targets = Collections.EMPTY_LIST;
            if (project.getParseException () != null) return;
            Element pel = project.getProjectElement ();
            String deftarget = pel.getAttribute ("default"); // NOI18N
            List targetsWithHint = new ArrayList (25); // List<String>
            List targetsWithoutHint = new ArrayList (25); // List<String>
            NodeList nl = pel.getChildNodes ();
            for (int i = 0; i < nl.getLength (); i++) {
                if (nl.item (i) instanceof Element) {
                    Element targ = (Element) nl.item (i);
                    if (! targ.getNodeName ().equals ("target")) continue; // NOI18N
                    String targname = targ.getAttribute ("name"); // NOI18N
                    String descr = targ.getAttribute ("description"); // NOI18N
                    if (descr.length () == 0) {
                        targetsWithoutHint.add (targname);
                    } else {
                        targetsWithHint.add (targname);
                    }
                }
            }
            if (targetsWithHint.isEmpty ()) {
                // User does not target descriptions; so show them all.
                // If default is present, show it at the top above sep.
                if (targetsWithoutHint.remove (deftarget)) {
                    targetsWithoutHint.add (0, deftarget);
                    targetsWithoutHint.add (1, null);
                }
                targets = targetsWithoutHint;
            } else {
                // Show just the documented ones. If default is present,
                // show it at the top above sep. If not, show it anyway
                // (provided it was in the undocumented list).
                if (targetsWithHint.remove (deftarget) ||
                        targetsWithoutHint.contains (deftarget)) {
                    targetsWithHint.add (0, deftarget);
                    targetsWithHint.add (1, null);
                }
                targets = targetsWithHint;
            }
            // If the default was the only target, we ought to kill
            // the extra sep. But leave it in to force the submenu
            // to appear.
            /*
            if (targets.size () == 2 && targets.get (1) == null) {
                targets.remove (1);
            }
             */
            // In fact we should ensure there are >1 items (workaround for
            // undesired behavior of Actions.SubMenu):
            if (targets.size () == 1) {
                // The extra separator will not actually be displayed:
                targets.add (null);
            }
        }

        public synchronized void addChangeListener (ChangeListener l) {
            //listeners.add (l);
        }

        public synchronized void removeChangeListener (ChangeListener l) {
            //listeners.remove (l);
        }

        /** You may use this is you have attached other listeners to things that will affect displayNames, for example. * /
        private synchronized void fireStateChanged () {
            if (listeners.size () == 0) return;
            ChangeEvent ev = new ChangeEvent (this);
            Iterator it = listeners.iterator ();
            while (it.hasNext ())
                ((ChangeListener) it.next ()).stateChanged (ev);
        }*/

    }

}
