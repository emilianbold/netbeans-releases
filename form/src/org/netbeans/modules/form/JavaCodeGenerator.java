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

package com.netbeans.developer.modules.loaders.form.formeditor;

import com.netbeans.ide.nodes.*;

/** 
*
* @author Ian Formanek
*/
public class JavaCodeGenerator extends CodeGenerator {

  private static final String AUX_VARIABLE_NAME = "JavaCodeGenerator::VariableName";

  private FormManager formManager;

  private JCGFormListener listener;

  /** Creates new JavaCodeGenerator */
  public JavaCodeGenerator () {
  }

  public void initialize (FormManager formManager) {
    listener = new JCGFormListener ();
    this.formManager = formManager;
    formManager.addFormListener (listener);
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
  }

  private void regenerateVariables () {
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
    public void componentChanged (FormEvent evt) {
//      RADComponent component = evt.getComponent ();
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any bean property of a component on the form is changed
    * @param evt the event object describing the event
    */
    public void propertyChanged (FormEvent evt) {
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
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */


