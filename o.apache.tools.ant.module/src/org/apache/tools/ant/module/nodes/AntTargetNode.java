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
        AntProjectCookie proj = (AntProjectCookie) getCookie(AntProjectCookie.class);
        for (int i = 0; i < attrs.length; i++) {
            org.openide.nodes.Node.Property prop = new AntProperty (el, attrs[i], proj);
            prop.setDisplayName (NbBundle.getMessage (AntTargetNode.class, "PROP_target_" + attrs[i]));
            prop.setShortDescription (NbBundle.getMessage (AntTargetNode.class, "HINT_target_" + attrs[i]));
            props.put (prop);
        }
        props.put (new DependsProperty (proj));
        props.put (new BuildSequenceProperty(el));
    }

    private class DependsProperty extends AntProperty {
        public DependsProperty (AntProjectCookie proj) {
            super (el, "depends", proj); // NOI18N
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
                private final Set on = new HashSet (); // Set<String>
                public DependsPanel () {
                    super (BoxLayout.Y_AXIS);
                    String depends = (String) DependsEditor.this.getValue ();
                    StringTokenizer tok = new StringTokenizer (depends, ", "); // NOI18N
                    Set available = getAvailable ();
                    Set bogus = new HashSet (); // Set<String>
                    while (tok.hasMoreTokens ()) {
                        String dep = tok.nextToken ();
                        if (available.contains (dep)) {
                            on.add (dep);
                        } else {
                            bogus.add (dep);
                        }
                    }
                    if (! bogus.isEmpty ()) {
                        // #12681: if there are bad dependencies, just skip them.
                        List bogusList = new ArrayList (bogus); // List<String>
                        Collections.sort (bogusList);
                        StringBuffer bogusListString = new StringBuffer (100);
                        Iterator it = bogusList.iterator ();
                        bogusListString.append ((String) it.next ());
                        while (it.hasNext ()) {
                            bogusListString.append (' '); // NOI18N
                            bogusListString.append ((String) it.next ());
                        }
                        add (new JLabel (NbBundle.getMessage (AntTargetNode.class,
                            "MSG_suppressing_bad_deps", bogusListString.toString ()))); // NOI18N
                    }
                    List availableList = new ArrayList (available); // List<String>
                    Collections.sort (availableList);
                    Iterator it = availableList.iterator ();
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
                    List onList = new ArrayList (on); // List<String>
                    Collections.sort (onList);
                    Iterator it = onList.iterator ();
                    while (it.hasNext ()) {
                        target = (String) it.next ();
                        if (buf.length () > 0) buf.append (','); // NOI18N
                        buf.append (target);
                    }
                    DependsEditor.this.setValue (buf.toString ());
                }
            }
        }
    }

    public NewType[] getNewTypes () {
        if (! AntProjectNode.isScriptReadOnly ((AntProjectCookie) getCookie (AntProjectCookie.class))) {
            List names = new ArrayList ();
            names.addAll (IntrospectedInfo.getDefaults ().getTaskdefs ().keySet ());
            names.addAll (AntSettings.getDefault ().getCustomDefs ().getTaskdefs ().keySet ());
            Collections.sort (names);
            return new NewType[] { new TaskNewType (names) };
        } else {
            return new NewType[0];
        }
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

    /**
     * Node displaying the sequence of all called targets when executing.
     */
    public static class BuildSequenceProperty extends org.openide.nodes.PropertySupport.ReadOnly {
        
        /** Target Element of the build. */
        protected org.w3c.dom.Element el;
                
        /** Creates new BuildSequenceProperty.
         * @param el Element representing the target.
         * @param name the name of the target.
         * @param proj AntProjectCookie of the Ant file
         */
        public BuildSequenceProperty (Element el) {
            super ("sequenceproperty",
                   String.class,
                   NbBundle.getMessage (AntTargetNode.class, "PROP_target_sequence"),
                   NbBundle.getMessage (AntTargetNode.class, "HINT_target_sequence")
                  );
            this.el = el;
        }

        /** Returns the target of this BuildSequenceProperty. */
        public Element getTarget() {
            return el;
        }
        
        /** Computes the dependencies of all called targets and returns an ordered
         * sequence String.
         * @param target the target that gets executed
         */
        protected String computeTargetDependencies(org.w3c.dom.Element target) {
            if (target == null) {
                return "";
            }
            
            // get ProjectElement
            Element proj = (Element) target.getParentNode ();
            if (proj == null) {
                // just return current target name
                return target.getAttribute ("name"); // NOI18N
            } else {
                // List with all called targets. the last called target is the first
                // in the list
                List callingList = new LinkedList(); 
                // add this target.
                callingList = addTarget (callingList, target, 0, proj);
                if (callingList != null) {
                    return getReverseString (callingList);
                } else {
                    return NbBundle.getMessage (AntProjectNode.class, "MSG_target_sequence_illegaldepends");
                }
            }
        }

        /** Adds a target to the List. Calls depends-on targets recursively.
         * @param runningList List containing the ordered targets.
         * @param target the target that should be added
         * @param pos position where this target should be inserted
         * @projectElement the Element of the Ant project.
         *
         * @return list with all targets or null if a target was not found.
         */
        protected List addTarget(List runningList, Element target, int pos, Element projectElement) {
            String targetName = target.getAttribute ("name"); // NOI18N
            if (targetName == null) return runningList;
            
            // search target, skip it if found
            for (int x=0; x < runningList.size (); x++) {
                if (targetName.equals (runningList.get(x))) {
                    return runningList;
                }
            }
            //add target at the given position...
            runningList.add(pos, targetName);
            
            // check dependenciesList
            String dependsString = target.getAttribute ("depends"); // NOI18N
            if (dependsString == null) return runningList;
            
            // add each target of the dependencies List
            StringTokenizer st = new StringTokenizer(dependsString, ", "); // NOI18N
            while (st.hasMoreTokens()) {
                Element dependsTarget = getTargetElement(st.nextToken(), projectElement);
                if (dependsTarget != null) {
                    addTarget(runningList, dependsTarget, (pos + 1), projectElement);
                } else {
                    // target is missing, we return null to indicate that something is wrong
                    return null;
                }
            }
            
            return runningList;
        }
        
        /** Returns the Element of a target given by its name. */
        protected Element getTargetElement(String targetName, Element projectElement) {
            NodeList nl = projectElement.getChildNodes();
            for (int i = 0; i < nl.getLength (); i++) {
                if (nl.item (i) instanceof Element) {
                    Element el = (Element) nl.item (i);
                    if (el.getTagName().equals("target") && el.getAttribute("name").equals(targetName)) { // NOI18N
                        return el;
                    }
                }
            }
            return null;
        }
 
        /** Returns a String of all Elements in the List in reverse order. */
        protected String getReverseString (List l) {
            StringBuffer sb = new StringBuffer ();
            for (int x= (l.size() - 1); x > -1; x--) {
                sb.append (l.get(x));
                if (x > 0) sb.append (", "); // NOI18N
            }
            return sb.toString ();
        }
        
        /** Returns the value of this property. */
        public Object getValue () {
            return computeTargetDependencies(getTarget());
        }
    }
}
