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
import com.netbeans.ide.util.Utilities;
import com.netbeans.developer.modules.loaders.java.JavaEditor;
import com.netbeans.developerx.loaders.form.formeditor.layouts.DesignLayout;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Iterator;

/* TODO
  x generation of form's properties/events
  - generation of container's add code
  - adding event handlers  (connection to JavaEditor's interior sections)
  - removing event handlers  (connection to JavaEditor's interior sections)
  - renaming event handlers  (connection to JavaEditor's interior sections)
  
  - connection to indentation engine
  - Indent AWT Hierarchy
  - Exception handling in guarded blocks - from FormSettings???, or as a property of formManager

  - BeanContext support
  - External Event Handlers
*/

/** JavaCodeGenerator is the default code generator which produces a Java source for the form.
*
* @author Ian Formanek
*/
public class JavaCodeGenerator extends CodeGenerator {
  private static Object GEN_LOCK = new Object ();

  protected static final String SECTION_INIT_COMPONENTS = "initComponents";
  protected static final String SECTION_VARIABLES = "variables";
  protected static final String SECTION_EVENT_PREFIX = "event_";
    
  private FormManager formManager;

  private JCGFormListener listener;

  private boolean errorInitializing = false;

  private JavaEditor.SimpleSection initComponentsSection;
  private JavaEditor.SimpleSection variablesSection;

  private static final String INIT_COMPONENTS_HEADER = "  private void initComponents () {\n";
  private static final String INIT_COMPONENTS_FOOTER = "  }\n";
  private static final String VARIABLES_HEADER = FormEditor.getFormBundle ().getString ("MSG_VariablesBegin");
  private static final String VARIABLES_FOOTER = FormEditor.getFormBundle ().getString ("MSG_VariablesEnd");

  private static final String oneIndent = "  "; // [PENDING - indentation engine]

  /** The prefix for event handler sections */
  private static final String EVT_SECTION_PREFIX = "event_";

  // FINALIZE DEBUG METHOD
  public void finalize () throws Throwable {
    super.finalize ();
    if (System.getProperty ("netbeans.debug.form.finalize") != null) {
      System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this);
    }
  } // FINALIZE DEBUG METHOD
  
  /** Creates new JavaCodeGenerator */
  public JavaCodeGenerator () {
  }

  public void initialize (FormManager formManager) {
    listener = new JCGFormListener ();
    this.formManager = formManager;
    formManager.addFormListener (listener);
    FormEditorSupport s = formManager.getFormEditorSupport ();
    
    initComponentsSection = s.findSimpleSection (SECTION_INIT_COMPONENTS); // [PENDING]
    variablesSection = s.findSimpleSection (SECTION_VARIABLES); // [PENDING]

    if ((initComponentsSection == null) || (variablesSection == null)) {
      System.out.println("ERROR: Cannot initialize guarded sections... code generation will not work.");
      errorInitializing = true;
    }

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
        }

        public Object getValue () {
          return component.getName ();
        }
      },
    };
  }

  private void regenerateInitializer () {
    if (errorInitializing) return;
    StringBuffer text = new StringBuffer (); 
    text.append (INIT_COMPONENTS_HEADER);
    RADForm form = formManager.getRADForm ();
    RADComponent top = form.getTopLevelComponent ();
    addInitCode (top, text, "    "); // [PENDING - indentation engine]
    text.append (INIT_COMPONENTS_FOOTER);
    synchronized (GEN_LOCK) {
      initComponentsSection.setText (text.toString ());
    }
  }

  private void regenerateVariables () {
    if (errorInitializing) return;
    StringBuffer text = new StringBuffer (); 
    text.append (VARIABLES_HEADER);
    text.append ("\n");
    RADForm form = formManager.getRADForm ();
    ComponentContainer top = (ComponentContainer)form.getTopLevelComponent (); // [PENDING - illegal cast]
    addVariables (top, text);
    text.append (VARIABLES_FOOTER);
    text.append ("\n");
    synchronized (GEN_LOCK) {
      variablesSection.setText (text.toString ());
    }
  }

  private void addInitCode (RADComponent comp, StringBuffer text, String indent) {
    System.out.println("Adding init code for: "+comp.getName ());
    if (!(comp instanceof FormContainer)) {
      generateComponentCreate (comp, text, indent);
    }
    generateComponentInit (comp, text, indent);
    generateComponentEvents (comp, text, indent);
    if (comp instanceof ComponentContainer) {
      RADComponent[] children = ((ComponentContainer)comp).getSubBeans ();
      for (int i = 0; i < children.length; i++) {
        text.append ("\n"); // [PENDING - indentation engine]
        if (comp instanceof FormContainer) {
          // do not indent for top-level children
          addInitCode (children[i], text, indent);
        } else {
          addInitCode (children[i], text, indent + oneIndent);
        }
        if (comp instanceof RADVisualContainer) {
          if (comp instanceof RADVisualFormContainer) {
            // no indent for top-level container
            text.append ("\n");
            generateComponentAddCode (children[i], (RADVisualContainer)comp, text, indent);
          } else {
            generateComponentAddCode (children[i], (RADVisualContainer)comp, text, indent + oneIndent);
          }
        } // [PENDING - adding to non-visual containers
      }
    }
  }
  
  private void generateComponentCreate (RADComponent comp, StringBuffer text, String indent) {
    text.append (indent); // [PENDING - will be done by indentation engine]
    text.append (comp.getName ());
    text.append (" = new ");
    text.append (comp.getComponentClass ().getName ());
    text.append (" ();\n");
  }
  
  private void generateComponentInit (RADComponent comp, StringBuffer text, String indent) {
    if (comp instanceof RADVisualContainer) {
      // generate layout init code
      DesignLayout dl = ((RADVisualContainer)comp).getDesignLayout ();
      text.append (dl.generateInitCode (indent, (RADVisualContainer)comp));
    }
    Map changedProps = comp.getChangedProperties ();
    for (Iterator it = changedProps.keySet ().iterator (); it.hasNext ();) {
      PropertyDescriptor desc = (PropertyDescriptor) it.next ();
      Object value = changedProps.get (desc);
      if (desc instanceof IndexedPropertyDescriptor) {
        generateIndexedPropertySetter (comp, desc, value, text, indent);
      } else {
        generatePropertySetter (comp, desc, value, text, indent);
      }
    }
  }

  private void generateComponentAddCode (RADComponent comp, RADVisualContainer container, StringBuffer text, String indent) {
    DesignLayout dl = container.getDesignLayout ();
    text.append (dl.generateComponentCode (indent, container, (RADVisualComponent)comp)); // [PENDING incorrect cast]
  }
  
  private void generateIndexedPropertySetter (RADComponent comp, PropertyDescriptor desc, Object value, StringBuffer text, String indent) {
    System.out.println("generateIndexedPropertySetter: NotImplemented... (Property: "+desc.getName ()+", Value: "+value+")"); // [PENDING]
  }

  
  private void generatePropertySetter (RADComponent comp, PropertyDescriptor desc, Object value, StringBuffer text, String indent) {
    Method writeMethod = desc.getWriteMethod ();
    String indentToUse = indent;
    
    // if the setter throws checked exceptions, we must generate try/catch block around it.
    Class[] exceptions = writeMethod.getExceptionTypes ();
    if (exceptions.length > 0) {
      indentToUse = indent + oneIndent; // [PENDING indentation engine]
      text.append (indent);
      text.append ("try {\n");
    }
          
    text.append (indentToUse); // [PENDING - will be done by indentation engine]
    text.append (getVariableGenString (comp, false));
    text.append (writeMethod.getName ());
    text.append (" (");

    // null values are generated separately, as most property editors cannot cope with nulls
    if (value != null) {
      PropertyEditor ed = BeanSupport.getPropertyEditor (desc);
      ed.setValue (value);
      text.append (ed.getJavaInitializationString ());
    } else {
      text.append ("null");
    }
    
    text.append (");\n");

    int varCount = 1;
    // add the catch for all checked exceptions
    for (int j = 0; j < exceptions.length; j++) {
      text.append (indent);
      text.append ("} catch (");
      text.append (exceptions[j].getName ());
      text.append (" ");
      String excName = "e"+varCount;
      varCount++;
      while (formManager.getVariablesPool ().isReserved (excName)) {
        excName = "e"+varCount;
        varCount++;
      }
      text.append (excName);
      text.append (") {\n");
      text.append (indent);
      text.append (oneIndent);
      text.append (excName);
      text.append (".printStackTrace ();\n");
      if (j == exceptions.length - 1) {
        text.append (indent);
        text.append ("}\n");
      }
    }
  }
          
  
  private void generateComponentEvents (RADComponent comp, StringBuffer text, String indent) {
    String variablePrefix = getVariableGenString (comp, false);

    EventsList.EventSet[] eventSets = comp.getEventsList ().getEventSets ();

    // go through the event sets - we generate the innerclass for whole
    // EventSet at once
    for (int i = 0; i < eventSets.length; i++) {
      EventsList.Event events[] = eventSets[i].getEvents ();
      EventSetDescriptor eventSetDesc = eventSets[i].getEventSetDescriptor ();

      // try to find adpater to use instead of the listener
      Class classToGenerate = FormUtils.getAdapterForListener(
        eventSetDesc.getListenerType());
      boolean adapterUsed = true;
      if (classToGenerate == null) { // if not found, we must use the listener
        classToGenerate = eventSetDesc.getListenerType();
        adapterUsed = false;
      }

      // test if we should generate the addListener for this eventSet
      boolean shouldGenerate = false;
      boolean[] shouldGenerateEvent = new boolean[events.length];
      for (int j = 0; j < events.length; j++) {
        if (events[j].getHandler () != null) {
          shouldGenerate = true;
          shouldGenerateEvent[j] = true;
          continue;
        }
        else shouldGenerateEvent[j] = false;
      }
      // if we should generate inner class for this listener and we do not
      // use adapter, we must generate all methods!!!
      if (shouldGenerate && !adapterUsed)
        for (int j = 0; j < events.length; j++)
          shouldGenerateEvent[j] = true;

      if (shouldGenerate) {
        Method eventAddMethod = eventSetDesc.getAddListenerMethod ();
        String indentToUse = indent;
        
        boolean unicastEvent = false;
        if ((eventAddMethod.getExceptionTypes ().length == 1) && 
            (java.util.TooManyListenersException.class.equals (eventAddMethod.getExceptionTypes()[0]))) 
          unicastEvent = true;
        
        if (unicastEvent) {
          text.append (indent);
          text.append ("try {\n");
          indentToUse = indent + oneIndent;
        }
        
        // beginning of the addXXXListener
        text.append(indentToUse);
        text.append(variablePrefix);
        text.append(eventSetDesc.getAddListenerMethod().getName());
        text.append(" (new ");
        text.append(classToGenerate.getName() + " () {\n");

        // listener innerclass' methods - indented one more indent to the right
        for (int j = 0; j < events.length; j++) {
          if (!shouldGenerateEvent[j])
            continue;

          Method evtMethod = events[j].getListenerMethod ();
          Class[] evtParams = evtMethod.getParameterTypes();
          String[] varNames;

          if ((evtParams.length == 1) &&
              (java.util.EventObject.class.isAssignableFrom (evtParams[0])))
            varNames = new String[] {
              FormEditor.getFormSettings ().getEventVariableName()
            };
          else {
            varNames = new String[evtParams.length];
            for (int k = 0; k < evtParams.length; k ++)
              varNames[k] = "param" + k;
          }

          // generate the listener's method
          text.append (FormUtils.getMethodHeaderText (
              evtMethod, indentToUse + oneIndent + oneIndent, varNames)
          );
          text.append (" {\n");

          if (events[j].getHandler () != null) {
            // generate the call to the handler
            text.append (indentToUse);
            text.append (oneIndent);
            text.append (oneIndent);
            text.append (oneIndent);
            text.append (events[j].getHandler ().getName ());
            text.append (" (");
            for (int k = 0; k < varNames.length; k++) {
              text.append (varNames[k]);
              if (k != varNames.length - 1)
               text.append (", ");
            }
            text.append (");");
          }
          text.append ("\n");
          text.append (indentToUse);
          text.append (oneIndent);
          text.append (oneIndent);
          text.append ("}\n");
        }

        // end of the innerclass
        text.append (indentToUse);
        text.append (oneIndent);
        text.append ("}\n");
        text.append (indentToUse);
        text.append (");\n");


        // if the event is unicast, generate the catch for TooManyListenersException
        if (unicastEvent) {
          text.append (indent);
          text.append ("} catch (java.util.TooManyListenersException ");
          String varName = "e";
          if (formManager.getVariablesPool ().isReserved (varName)) {
            int varCount = 1;
            varName = "e1";
            while (true) {
              if (!(formManager.getVariablesPool ().isReserved (varName)))
                break;             
              varName = "e"+varCount;
            }
          }
           
          text.append (varName);
          text.append (") {\n");
          text.append (indent);
          text.append (oneIndent);
          text.append (varName);
          text.append (".printStackTrace ();\n");
          text.append (indent);
          text.append ("}\n");          
        }
      }
    }
  }

  private void addVariables (ComponentContainer cont, StringBuffer text) {
    RADComponent[] children = cont.getSubBeans ();
    for (int i = 0; i < children.length; i++) {
      text.append (oneIndent); // [PENDING - will be done by indentation engine]
      text.append ("private ");
      text.append (children[i].getComponentClass ().getName ());
      text.append (" ");
      text.append (children[i].getName ());
      text.append (";\n");
      if (children[i] instanceof ComponentContainer) {
        addVariables ((ComponentContainer)children[i], text);
      }
    }
  }

  private static String getVariableGenString (RADComponent comp, boolean containerCode) {
    if (comp instanceof FormContainer) {
      if (containerCode) {
        return (((FormContainer)comp).getFormInfo ().getContainerGenName ());
      } else {
        return "";
      }
    } else {
      return comp.getName () + ".";
    }
  }
    
  
// -----------------------------------------------------------------------------
// Event handlers
  /** Generates the specified event handler, if it does not exist yet.
  * @param handlerName The name of the event handler
  * @param paramList the list of event handler parameter types
  * @param bodyText the body text of the event handler or null for default (empty) one
  * @return true if the event handler have not existed yet and was creaated, false otherwise
  */
  public boolean generateEventHandler (String handlerName, String[] paramTypes, String bodyText) {
    if (errorInitializing) return false;
    if (getEventHandlerSection (handlerName) != null)
      return false;

    synchronized (GEN_LOCK) {
      FormEditorSupport s = formManager.getFormEditorSupport ();
    
      try {
        JavaEditor.InteriorSection sec = s.createInteriorSectionAfter (initComponentsSection, getEventSectionName (handlerName));
        sec.setHeader (getEventHandlerHeader (handlerName, paramTypes));
        sec.setBody (getEventHandlerBody (handlerName, paramTypes, bodyText));
        sec.setBottom (getEventHandlerFooter (handlerName, paramTypes));
      } catch (javax.swing.text.BadLocationException e) {
        e.printStackTrace (); // [PENDING]
      }
//      clearUndo ();
    }

    return true;
  }

  /** Changes the text of the specified event handler, if it already exists.
  * @param handlerName The name of the event handler
  * @param paramList the list of event handler parameter types
  * @param bodyText the new body text of the event handler or null for default (empty) one
  * @return true if the event handler existed and was modified, false otherwise
  */
  public boolean changeEventHandler (final String handlerName, final String[] paramTypes, final String bodyText) {
    JavaEditor.InteriorSection sec = getEventHandlerSection (handlerName);
    if (sec == null)
      return false;

    synchronized (GEN_LOCK) {
      System.out.println("Change event handler: "+handlerName+" text: "+bodyText);
      sec.setHeader (getEventHandlerHeader (handlerName, paramTypes));
      sec.setBody (getEventHandlerBody (handlerName, paramTypes, bodyText));
      sec.setBottom (getEventHandlerFooter (handlerName, paramTypes));
    }
    return true;
  }

  /** Removes the specified event handler - removes the whole method together with the user code!
  * @param handlerName The name of the event handler
  */
  public boolean deleteEventHandler (String handlerName) {
    synchronized (GEN_LOCK) {
      JavaEditor.InteriorSection section = getEventHandlerSection (handlerName);
      if (section == null)
        return false;    
      section.deleteSection ();
    }
    
    return true;
  }

  private String getEventHandlerHeader (String handlerName, String[] paramTypes) {
    StringBuffer buf = new StringBuffer ();
    buf.append (oneIndent);
    buf.append ("private void ");
    buf.append (handlerName);
    buf.append (" (");

    // create variable names
    String[] varNames = new String [paramTypes.length];

    if (paramTypes.length == 1)
      varNames [0] = paramTypes [0] + " " + new FormLoaderSettings ().getEventVariableName();
    else
      for (int i = 0; i < paramTypes.length; i ++)
        varNames [i] = paramTypes [0] + " param" + i;

    for (int i = 0; i < paramTypes.length; i++) {
      buf.append (varNames[i]);
      if (i != paramTypes.length - 1)
        buf.append (", ");
      else
        buf.append (") {\n");
    }
    return buf.toString ();
  }

  private String getEventHandlerBody (String handlerName, String[] paramTypes, String bodyText) {
    if (bodyText == null) {
      bodyText = getDefaultEventBody ();
    } else {
      bodyText = Utilities.replaceString (bodyText, "\n", "\n"+oneIndent);
      bodyText = Utilities.replaceString (bodyText, "\t", oneIndent);
      bodyText = oneIndent + oneIndent + bodyText;
    }
    return bodyText;
  }

  private String getEventHandlerFooter (String handlerName, String[] paramTypes) {
    return "  }\n"; // [PENDING]
  }
  
  private String getDefaultEventBody () {
    StringBuffer evtCode = new StringBuffer();
    evtCode.append (oneIndent);
    evtCode.append (oneIndent);
    evtCode.append (FormEditor.getFormBundle ().getString ("MSG_EventHandlerBody"));
    evtCode.append ("\n");
    evtCode.append (oneIndent);
    evtCode.append ("\n");
    return evtCode.toString();
  }

  /** Renames the specified event handler to the given new name.
  * @param oldHandlerName The old name of the event handler
  * @param newHandlerName The new name of the event handler
  */
  public boolean renameEventHandler (String oldHandlerName, String newHandlerName, String[] paramTypes) {
    JavaEditor.InteriorSection sec = getEventHandlerSection (oldHandlerName);
    if (sec == null) {
      return false;
    }

    synchronized (GEN_LOCK) {
      System.out.println("Rename event handler: "+newHandlerName);
      sec.setHeader (getEventHandlerHeader (newHandlerName, paramTypes));
      sec.setBottom (getEventHandlerFooter (newHandlerName, paramTypes));
      try {
        sec.setName(getEventSectionName(newHandlerName));
//        clearUndo ();
      } catch (java.beans.PropertyVetoException e) {
        return false;
      }
    }

    return true;
  }

  /** Focuses the specified event handler in the editor. */
  public void gotoEventHandler (String handlerName) {
    JavaEditor.InteriorSection sec = getEventHandlerSection (handlerName);
    if (sec != null) {
      sec.openAt ();
    } 
  }

// ------------------------------------------------------------------------------------------
// Private methods

  // sections acquirement

  private JavaEditor.InteriorSection getEventHandlerSection (String eventName) {
    FormEditorSupport s = formManager.getFormEditorSupport ();
    return s.findInteriorSection (getEventSectionName (eventName));
  }

  // other

  private String getEventSectionName (String handlerName) {
    return EVT_SECTION_PREFIX + handlerName;
  }


// ------------------------------------------------------------------------------------------
// Innerclasses

  private class JCGFormListener implements FormListener {
    /** Called when a new component is added to the form
    * @param evt the event object describing the event
    */
    public void componentAdded (FormEvent evt) {
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any component is removed from the form
    * @param evt the event object describing the event
    */
    public void componentRemoved (FormEvent evt) {
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any synthetic property of a component on the form is changed
    * The synthetic properties include: variableName, serialize, serializeName, generateGlobalVariable
    * @param evt the event object describing the event
    */
    public void componentChanged (FormPropertyEvent evt) {
      regenerateVariables ();
      regenerateInitializer ();
    }

    /** Called when any bean property of a component on the form is changed
    * @param evt the event object describing the event
    */
    public void propertyChanged (FormPropertyEvent evt) {
      regenerateInitializer ();
    }

    /** Called when an event handler is added to a component on the form
    * @param evt the event object describing the event
    */
    public void eventAdded (FormEventEvent evt) {
      // 1. add event handler method according to handler type

      // 2. regenerate initializer
      regenerateInitializer ();
    }

    /** Called when an event handler is added to a component on the form
    * @param evt the event object describing the event
    */
    public void eventRemoved (FormEventEvent evt) {
      // 1. remove event handler method according to handler type

      // 2. regenerate initializer
      regenerateInitializer ();
    }

    /** Called when an event handler is renamed on a component on the form
    * @param evt the event object describing the event
    */
    public void eventRenamed (FormEventEvent evt) {
      // 1. rename event handler method according to handler type

      // 2. regenerate initializer
      regenerateInitializer ();
    }
  }
}

/*
 * Log
 *  9    Gandalf   1.8         5/14/99  Ian Formanek    
 *  8    Gandalf   1.7         5/12/99  Ian Formanek    
 *  7    Gandalf   1.6         5/11/99  Ian Formanek    Build 318 version
 *  6    Gandalf   1.5         5/10/99  Ian Formanek    
 *  5    Gandalf   1.4         5/6/99   Ian Formanek    Generates code into 
 *       guarded sections
 *  4    Gandalf   1.3         5/5/99   Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */


