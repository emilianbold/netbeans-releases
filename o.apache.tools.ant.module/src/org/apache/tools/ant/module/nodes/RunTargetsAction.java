/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */
 
package org.apache.tools.ant.module.nodes;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.openide.ErrorManager;

// XXX nicer to show a submenu for other targets
// XXX rewrite to use a plain JMenu and listen to button activation events to populate popup menu
// (or just impl ContextAwareAction and put the getPopupPresenter method into that)

/** Submenu to run certain targets in a project.
 */
public final class RunTargetsAction extends CookieAction implements Presenter.Popup {

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

        private List/*<String>*/ targets = null;
        private AntProjectCookie project = null;

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

        public void performActionAt (final int index) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    String target = (String) targets.get (index);
                    try {
                        TargetExecutor te = new TargetExecutor(project, new String[] {target});
                        te.execute();
                    } catch (IOException ioe) {
                        AntModule.err.notify (ioe);
                    }
                }
            });
        }

        void addNotify () {
            project = null;
            targets = Collections.singletonList(null);
            Node[] nodes = action.getActivatedNodes ();
            if (nodes.length != 1) return;
            project = (AntProjectCookie) nodes[0].getCookie (AntProjectCookie.class);
            if (project == null) return;
            if (project.getParseException () != null) return;
            Set/*<TargetLister.Target>*/ allTargets;
            try {
                allTargets = TargetLister.getTargets(project);
            } catch (IOException e) {
                // XXX how to notify properly?
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
                return;
            }
            String defaultTarget = null;
            SortedSet/*<String>*/ describedTargets = new TreeSet(Collator.getInstance());
            SortedSet/*<String>*/ otherTargets = new TreeSet(Collator.getInstance());
            Iterator it = allTargets.iterator();
            while (it.hasNext()) {
                TargetLister.Target t = (TargetLister.Target) it.next();
                if (t.isOverridden()) {
                    // Cannot be called.
                    continue;
                }
                if (t.isInternal()) {
                    // Don't present in GUI.
                    continue;
                }
                String name = t.getName();
                if (t.isDefault()) {
                    defaultTarget = name;
                } else if (t.isDescribed()) {
                    describedTargets.add(name);
                } else {
                    otherTargets.add(name);
                }
            }
            targets = new ArrayList();
            if (defaultTarget != null) {
                targets.add(defaultTarget);
            }
            if (!describedTargets.isEmpty()) {
                if (!targets.isEmpty()) {
                    // XXX it seems the separators are not really displayed, at least on GTK...
                    targets.add(null);
                }
                targets.addAll(describedTargets);
            }
            if (!otherTargets.isEmpty()) {
                if (!targets.isEmpty()) {
                    targets.add(null);
                }
                targets.addAll(otherTargets);
            }
            // Ensure there are >1 items (workaround for
            // undesired behavior of Actions.SubMenu):
            while (targets.size() < 2) {
                // The extra separator will not actually be displayed:
                targets.add (null);
            }
        }

        public synchronized void addChangeListener (ChangeListener l) {
        }

        public synchronized void removeChangeListener (ChangeListener l) {
        }

    }

}
