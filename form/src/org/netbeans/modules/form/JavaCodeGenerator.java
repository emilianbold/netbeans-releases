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

package com.netbeans.developer.modules.loaders.form;

import com.netbeans.ide.nodes.*;
import com.netbeans.developer.modules.loaders.java.JavaEditor;

import java.beans.*;
import java.util.Map;
import java.util.Iterator;

/** 
*
* @author Ian Formanek
*/
public class JavaCodeGenerator extends CodeGenerator {

  protected static final String SECTION_INIT_COMPONENTS = "initComponents";
  protected static final String SECTION_VARIABLES = "variables";
  protected static final String SECTION_EVENT_PREFIX = "event_";
    
  private static final String AUX_VARIABLE_NAME = "JavaCodeGenerator::VariableName";

  private FormManager formManager;

  private JCGFormListener listener;

  private JavaEditor.SimpleSection initComponentsSection;
  private JavaEditor.SimpleSection variablesSection;

  /** Creates new JavaCodeGenerator */
  public JavaCodeGenerator () {
  }

  public void initialize (FormManager formManager) {
    listener = new JCGFormListener ();
    this.formManager = formManager;
    formManager.addFormListener (listener);
    FormEditorSupport s = formManager.getFormEditorSupport ();
    initComponentsSection = (JavaEditor.SimpleSection) s.findSection (SECTION_INIT_COMPONENTS); // [PENDING - incorrect cast]
    variablesSection = (JavaEditor.SimpleSection) s.findSection (SECTION_VARIABLES); // [PENDING - incorrect cast]

    Thread.dumpStack();
    // regenerate on init
    regenerateInitializer ();
    regenerateVariables ();
  }

  /** Alows the code generator to provide synthetic properties for specified component
  * which are specific to the code generation method.
  * E.g. a JavaCodeGenerator will return variableName property, as it generates
  * global Java variable for every component
  * @param component The RADComponent for which the properties are to be obtained
  */
  public Node.Property[] getSyntheticProperties (final RADComponent component) {
    return new Node.Property[] {
      new PropertySupport.ReadWrite ("variableName", String.class, "Variable Name",
                                     "The name of the global variable generated for this component") {
        public void setValue (Object value) {
          if (!(value instanceof String)) {
            throw new IllegalArgumentException ();
          }

          component.setName ((String)value);
          component.setAuxiliaryValue (AUX_VARIABLE_NAME,  value);
        }

        public Object getValue () {
          return component.getName ();
          //return component.getAuxiliaryValue (AUX_VARIABLE_NAME);
        }
      },
    };
  }

  private void regenerateInitializer () {
    StringBuffer text = new StringBuffer (); 
    RADForm form = formManager.getRADForm ();
    ComponentContainer top = form.getTopLevelComponent ();
    addInitCode (top, text);
    //initComponentsSection.setText (text);
    System.out.println("-----------regenerateInitializer:--------------");
    System.out.println(text.toString ());
    System.out.println("-----------------------------------------------");
  }

  private void regenerateVariables () {
    Thread.dumpStack();
    StringBuffer text = new StringBuffer (); 
    RADForm form = formManager.getRADForm ();
    ComponentContainer top = form.getTopLevelComponent ();
    addVariables (top, text);
//    variablesSection.setText (text);
    System.out.println("-----------regenerateVariables:--------------");
    System.out.println(text.toString ());
    System.out.println("---------------------------------------------");
  }

  private void addInitCode (ComponentContainer cont, StringBuffer text) {
    RADComponent[] children = cont.getSubComponents ();
    for (int i = 0; i < children.length; i++) {
      generateComponentCreate (children[i], text);
      generateComponentInit (children[i], text);
      generateComponentEvents (children[i], text);
      if (children[i] instanceof ComponentContainer) {
        addInitCode ((ComponentContainer)children[i], text);
      }
    }
  }
  
  private void generateComponentCreate (RADComponent comp, StringBuffer text) {
    text.append (comp.getName ());
    text.append (" = new ");
    text.append (comp.getComponentClass ().getName ());
    text.append (" ();\n");
  }
  
  private void generateComponentInit (RADComponent comp, StringBuffer text) {
    Map changedProps = comp.getChangedProperties ();
    for (Iterator it = changedProps.keySet ().iterator (); it.hasNext ();) {
      PropertyDescriptor desc = (PropertyDescriptor) it.next ();
      text.append (comp.getName ());
      text.append (".");
      text.append (desc.getWriteMethod ().getName ());
      text.append (" (");
      PropertyEditor ed = BeanSupport.getPropertyEditor (desc);
      ed.setValue (changedProps.get (desc));
      text.append (ed.getJavaInitializationString ());
      text.append (");\n");
    }
  }
  
  private void generateComponentEvents (RADComponent comp, StringBuffer text) {
  }

  private void addVariables (ComponentContainer cont, StringBuffer text) {
    RADComponent[] children = cont.getSubComponents ();
    for (int i = 0; i < children.length; i++) {
      text.append ("private ");
      text.append (children[i].getComponentClass ().getName ());
      text.append (" ");
      text.append (children[i].getName ());
      text.append ("\n");
      if (children[i] instanceof ComponentContainer) {
        addVariables ((ComponentContainer)children[i], text);
      }
    }
  }
  
  private class JCGFormListener implements FormListener {
    /** Called when a new component is added to the form
    * @param evt the event object describing the event
    */
    public void componentAdded (FormEvent evt) {
//      RADComponent component = evt.getComponent ();
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any component is removed from the form
    * @param evt the event object describing the event
    */
    public void componentRemoved (FormEvent evt) {
//      RADComponent component = evt.getComponent ();
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any synthetic property of a component on the form is changed
    * The synthetic properties include: variableName, serialize, serializeName, generateGlobalVariable
    * @param evt the event object describing the event
    */
    public void componentChanged (FormPropertyEvent evt) {
      //RADComponent component = evt.getRADComponent ();
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any bean property of a component on the form is changed
    * @param evt the event object describing the event
    */
    public void propertyChanged (FormPropertyEvent evt) {
      //RADComponent component = evt.getRADComponent ();
      regenerateInitializer ();
    }

    /** Called when an event handler is added to a component on the form
    * @param evt the event object describing the event
    */
    public void eventAdded (FormEvent evt) {
      // 1. add event handler method according to handler type

      // 2. regenerate initializer
      regenerateInitializer ();
    }

    /** Called when an event handler is added to a component on the form
    * @param evt the event object describing the event
    */
    public void eventRemoved (FormEvent evt) {
      // 1. remove event handler method according to handler type

      // 2. regenerate initializer
      regenerateInitializer ();
    }

    /** Called when an event handler is renamed on a component on the form
    * @param evt the event object describing the event
    */
    public void eventRenamed (FormEvent evt) {
      // 1. rename event handler method according to handler type

      // 2. regenerate initializer
      regenerateInitializer ();
    }
  }
}

/*
 * Log
 *  4    Gandalf   1.3         5/5/99   Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */


