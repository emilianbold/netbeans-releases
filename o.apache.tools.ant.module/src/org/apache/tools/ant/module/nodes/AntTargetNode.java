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
 * Contributor(s): Jayme C. Edwards, Jesse Glick.
 */

package org.apache.tools.ant.module.nodes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.w3c.dom.*;

import org.apache.tools.ant.Target;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.util.datatransfer.*;
import org.openide.util.actions.SystemAction;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.ElementCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.xml.ElementSupport;

/** A node representing an Ant build target.
 */
public class AntTargetNode extends ElementNode {

    public AntTargetNode (final AntProjectCookie project, final Element targetElem) {
        super (targetElem, new AntTargetChildren (targetElem));
        /*
        AntTargetCookie targetCookie = new AntTargetSupport (project, targetElem);
        getCookieSet().add(targetCookie);
         */
        if (project.getFile () != null) {
            getCookieSet ().add (new ExecCookie () {
                    public void start () {
                        try {
                            new TargetExecutor (project, new String[] { targetElem.getAttribute ("name") }).execute (); // NOI18N
                        } catch (IOException ioe) {
                            TopManager.getDefault ().notifyException (ioe);
                        }
                    }
                });
        }
    }

    protected ElementCookie createElementCookie () {
        return new ElementSupport.Introspection (el, Target.class.getName ());
    }

    protected void initDisplay () {
        String targetName = el.getAttribute ("name"); // NOI18N
        setNameSuper (targetName);
        String desc = el.getAttribute ("description"); // NOI18N
        if (desc.length () > 0) {
            setShortDescription (desc);
            setIconBase ("/org/apache/tools/ant/module/resources/EmphasizedTargetIcon");
        } else {
            setShortDescription (getDisplayName ());
            setIconBase ("/org/apache/tools/ant/module/resources/TargetIcon");
        }
    }

    protected SystemAction[] createActions () {
        return new SystemAction[] {
            SystemAction.get (ExecuteAction.class),
            null,
            SystemAction.get (OpenLocalExplorerAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            SystemAction.get (RenameAction.class),
            null,
            SystemAction.get (NewAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

    public SystemAction getDefaultAction () {
        return SystemAction.get (ExecuteAction.class);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
    }

    protected void addProperties (Sheet.Set props) {
        String[] attrs = new String[] { "name", "description", "if", "unless", /*"id"*/ }; // NOI18N
        for (int i = 0; i < attrs.length; i++) {
            org.openide.nodes.Node.Property prop = new AntProperty (el, attrs[i]);
            prop.setDisplayName (NbBundle.getMessage (AntTargetNode.class, "PROP_target_" + attrs[i]));
            prop.setShortDescription (NbBundle.getMessage (AntTargetNode.class, "HINT_target_" + attrs[i]));
            props.put (prop);
        }
        props.put (new DependsProperty ());
    }

    private class DependsProperty extends AntProperty {
        public DependsProperty () {
            super (el, "depends"); // NOI18N
            this.setDisplayName (NbBundle.getMessage (AntTargetNode.class, "PROP_target_depends"));
            this.setShortDescription (NbBundle.getMessage (AntTargetNode.class, "HINT_target_depends"));
        }
        private Set/*<String>*/ getAvailable () {
            Element proj = (Element) el.getParentNode ();
            if (proj == null) return Collections.EMPTY_SET;
            String me = el.getAttribute ("name"); // NOI18N
            NodeList nl = proj.getElementsByTagName ("target"); // NOI18N
            Set s = new HashSet (Math.max (1, nl.getLength () - 1));
            for (int i = 0; i < nl.getLength (); i++) {
                Element target = (Element) nl.item (i);
                String name = target.getAttribute ("name"); // NOI18N
                if (! me.equals (name)) {
                    s.add (name);
                }
            }
            AntModule.err.log ("AntTargetNode.DependsProperty.available=" + s);
            return s;
        }
        public void setValue (Object o) throws IllegalArgumentException, InvocationTargetException {
            if (! (o instanceof String)) throw new IllegalArgumentException ();
            Set av = getAvailable ();
            StringTokenizer tok = new StringTokenizer ((String) o, ", "); // NOI18N
            while (tok.hasMoreTokens ()) {
                String target = tok.nextToken ();
                if (! av.contains (target)) {
                    IllegalArgumentException iae = new IllegalArgumentException ("no such target: " + target); // NOI18N
                    //TopManager.getDefault ().getErrorManager ().annotate (iae, NbBundle.getMessage (AntTargetNode.class, "EXC_target_not_exist", target));
                    AntModule.err.annotate (iae, NbBundle.getMessage (AntTargetNode.class, "EXC_target_not_exist", target));
                    throw iae;
                }
            }
            super.setValue (o);
        }
        public Object getValue () {
            return super.getValue ();
        }
        public PropertyEditor getPropertyEditor () {
            return new DependsEditor ();
        }
        /** Note: treats list of dependencies as an _unordered set_.
         * Ant does not currently officially specify that the order
         * of items in a depends clause means anything, so this GUI
         * faithfully provides no interface to reorder them.
         * Cf. Peter Donald's message "RE: Order of Depends" to
         * ant-dev on Feb 21 2001.
         */
        private class DependsEditor extends PropertyEditorSupport {
            public String getAsText () {
                return (String) this.getValue ();
            }
            public void setAsText (String v) {
                this.setValue (v);
            }
            public boolean supportsCustomEditor () {
                return true;
            }
            public Component getCustomEditor () {
                return new DependsPanel ();
            }
            private class DependsPanel extends Box implements ActionListener {
                private final Set on = new HashSet ();
                public DependsPanel () {
                    super (BoxLayout.Y_AXIS);
                    String depends = (String) DependsEditor.this.getValue ();
                    StringTokenizer tok = new StringTokenizer (depends, ", "); // NOI18N
                    while (tok.hasMoreTokens ()) {
                        on.add (tok.nextToken ());
                    }
                    Iterator it = getAvailable ().iterator ();
                    while (it.hasNext ()) {
                        String target = (String) it.next ();
                        AbstractButton check = new JCheckBox (target, on.contains (target));
                        check.addActionListener (this);
                        add (check);
                    }
                    add (createGlue ());
                }
                public void actionPerformed (ActionEvent ev) {
                    JCheckBox box = (JCheckBox) ev.getSource ();
                    String target = box.getText ();
                    if (box.isSelected ()) {
                        on.add (target);
                    } else {
                        on.remove (target);
                    }
                    StringBuffer buf = new StringBuffer ();
                    Iterator it = on.iterator ();
                    while (it.hasNext ()) {
                        target = (String) it.next ();
                        if (buf.length () > 0) buf.append (',');
                        buf.append (target);
                    }
                    DependsEditor.this.setValue (buf.toString ());
                }
            }
        }
    }

    public NewType[] getNewTypes () {
        List names = new ArrayList ();
        names.addAll (IntrospectedInfo.getDefaults ().getTaskdefs ().keySet ());
        names.addAll (AntSettings.getDefault ().getCustomDefs ().getTaskdefs ().keySet ());
        Collections.sort (names);
        return new NewType[] { new TaskNewType (names) };
    }

    private class TaskNewType extends NewType {
        private List names;
        public TaskNewType (List names) {
            this.names = names;
        }
        public String getName () {
            return NbBundle.getMessage (AntTargetNode.class, "LBL_task_new_type");
        }
        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.apache.tools.ant.module.node-manip");
        }
        public void create () throws IOException {
            // Ask the user which to choose.
            JPanel pane = new JPanel ();
            pane.setLayout (new BorderLayout ());
            final JComboBox combo = new JComboBox (names.toArray ());
            // Not for now; helplistener does not update when the text is changed:
            //combo.setEditable (true);
            pane.add (combo, BorderLayout.CENTER);
            pane.add (new JLabel (NbBundle.getMessage (AntTargetNode.class, "LBL_choose_task")), BorderLayout.WEST);
            final JButton help = new JButton ();
            ActionListener helplistener = new ActionListener () {
                    public void actionPerformed (ActionEvent ignore) {
                        help.setText (NbBundle.getMessage (AntTargetNode.class, "LBL_help_on_task", combo.getSelectedItem ()));
                        help.setEnabled (IntrospectedInfo.getDefaults ().getTaskdefs ().containsKey (combo.getSelectedItem ()));
                    }
                };
            helplistener.actionPerformed (null);
            combo.addActionListener (helplistener);
            help.addActionListener (new ActionListener () {
                    public void actionPerformed (ActionEvent ev) {
                        TopManager.getDefault ().showHelp (new HelpCtx ("org.apache.tools.ant.module.tasks." + combo.getSelectedItem ()));
                    }
                });
            pane.add (help, BorderLayout.EAST);
            DialogDescriptor dlg = new DialogDescriptor (pane, NbBundle.getMessage (AntTargetNode.class, "TITLE_select_task"));
            dlg.setHelpCtx (getHelpCtx ());
            dlg.setModal (true);
            TopManager.getDefault ().createDialog (dlg).show ();
            if (dlg.getValue () != NotifyDescriptor.OK_OPTION) return;
            String name = (String) combo.getSelectedItem ();
            try {
                Element el2 = el.getOwnerDocument ().createElement (name);
                ElementNode.appendWithIndent (el, el2);
            } catch (DOMException dome) {
                IOException ioe = new IOException ();
                AntModule.err.annotate (ioe, dome);
                throw ioe;
            }
        }
    }

    protected boolean canPasteElement (Element el2) {
        String name = el2.getNodeName ();
        if (IntrospectedInfo.getDefaults ().getTaskdefs ().containsKey (name)) {
            return true;
        }
        if (AntSettings.getDefault ().getCustomDefs ().getTaskdefs ().containsKey (name)) {
            return true;
        }
        return false;
    }

}
