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
import org.apache.tools.ant.module.wizards.shortcut.ShortcutWizard;
import org.openide.util.HelpCtx;

/** A node representing an Ant build target.
 */
public class AntTargetNode extends ElementNode implements ChangeListener {
    
    public AntTargetNode (final AntProjectCookie project, final Element targetElem) {
        super(targetElem, hasChildElements(targetElem) ? new AntTargetChildren(targetElem) : Children.LEAF);
        project.addChangeListener(WeakListeners.change(this, project));
    }
    
    public void stateChanged (ChangeEvent ev) {
        firePropertyChange (null, null, null);
    }

    protected ElementCookie createElementCookie () {
        return new ElementSupport.Introspection (el, "org.apache.tools.ant.Target");
    }

    protected void initDisplay () {
        String targetName = el.getAttribute ("name"); // NOI18N
        setNameSuper (targetName);
        String desc = el.getAttribute ("description"); // NOI18N
        if (desc.length () > 0) {
            setShortDescription (desc);
            setIconBase ("org/apache/tools/ant/module/resources/EmphasizedTargetIcon");
        } else {
            setShortDescription (getDisplayName ());
            setIconBase ("org/apache/tools/ant/module/resources/TargetIcon");
        }
    }

    private final Action EXECUTE = new ExecuteAction();
    private final Action CREATE_SHORTCUT = new CreateShortcutAction();

    public Action[] getActions(boolean context) {
        return new Action[] {
            EXECUTE,
            CREATE_SHORTCUT,
            null,
            SystemAction.get (OpenLocalExplorerAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

    public Action getPreferredAction() {
        return EXECUTE;
    }
    
    private final class ExecuteAction extends AbstractAction {
        
        ExecuteAction() {
            super(NbBundle.getMessage(AntTargetNode.class, "LBL_execute_target"));
        }
        
        public void actionPerformed(ActionEvent e) {
            AntProjectCookie project = (AntProjectCookie)getCookie(AntProjectCookie.class);
            try {
                TargetExecutor te = new TargetExecutor(project, new String[] {el.getAttribute("name")}); // NOI18N
                te.execute();
            } catch (IOException ioe) {
                AntModule.err.notify(ioe);
            }
        }
        
    }
    
    /**
     * Action to invoke the target shortcut wizard.
     * Used to be a "template", but this is more natural.
     * @see "issue #37374"
     */
    private final class CreateShortcutAction extends AbstractAction {
        
        CreateShortcutAction() {
            super(NbBundle.getMessage(AntTargetNode.class, "LBL_create_shortcut"));
        }
        
        public void actionPerformed(ActionEvent e) {
            ShortcutWizard.show((AntProjectCookie)getCookie(AntProjectCookie.class), el);
        }
        
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props != null)  {
            props.setValue("helpID", "org.apache.tools.ant.module.nodes.AntTargetNode.Properties");
        }
        return sheet;
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
            NodeList nl = proj.getChildNodes();
            Set s = new HashSet();
            for (int i = 0; i < nl.getLength (); i++) {
                if (nl.item (i) instanceof Element) {
                    Element target = (Element) nl.item (i);
                    if (target.getTagName().equals("target") && ! me.equals(target.getAttribute("name"))) { // NOI18N
                        s.add (target.getAttribute ("name")); // NOI18N
                    }
                }
            }
            AntModule.err.log ("AntTargetNode.DependsProperty.available=" + s);
            return s;
        }
        public PropertyEditor getPropertyEditor () {
            return new DependsEditor ();
        }
        private class DependsEditor extends PropertyEditorSupport {
            public String getAsText () {
                return (String) this.getValue ();
            }
        }
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
            super ("buildSequence", // NOI18N
                   String.class,
                   NbBundle.getMessage (AntTargetNode.class, "PROP_target_sequence"),
                   NbBundle.getMessage (AntTargetNode.class, "HINT_target_sequence")
                  );
            this.el = el;
        }

        /** Returns the target of this BuildSequenceProperty. */
        protected Element getTarget() {
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
            Iterator it = runningList.iterator();
            while (it.hasNext()) {
                if (targetName.equals (it.next())) {
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
            while (st.hasMoreTokens() && runningList != null) {
                Element dependsTarget = getTargetElement(st.nextToken(), projectElement);
                if (dependsTarget != null) {
                    runningList = addTarget(runningList, dependsTarget, (pos + 1), projectElement);
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
