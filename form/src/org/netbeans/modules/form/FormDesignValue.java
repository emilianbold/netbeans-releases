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

/** FormDesignValue is an interface for property values which
* should provide different value during design time.
* NetBeans form editor allows to use property editors which
* do not provide objects of the real property type during design-time
* I.e. a property editor for String does not have to provide
* instances of String, if it wants to store additional information
* about the property value, which cannot be encoded into the value itself
* (e.g. if the property type is a final class which cannot be subclassed to
* allow extended info to be stored with it).  Such property editors can
* provide an Object which implements FormDesignValue interface.
* This object must return a correct property value (of the same type
* as the property type) from the getDesignValue () method.
* The value returned from getDesignValue () will be used on the real instance
* of the JavaBean component during design-time, while the object implementing
* FormDesignValue will be used for persistence and for code generation 
* (i.e. the property editor must accept it in its setValue () method and
* generate correct Java code fro it from the getJavaInitializationString () method).
* @author Ian Formanek
*/
public interface FormDesignValue extends java.io.Serializable {
  /** A special value which is not used during design-time if returned from the getDesignValue
  * method call.
  * @see #getDesignValue
  */
  public static final Object IGNORED_VALUE = new Object ();

  /** Provides a value which should be used during design-time
  * as the real property value on the bean instance.
  * E.g. the ResourceBundle String would provide the real value
  * of the String from the resource bundle, so that the design-time
  * representation reflects the real code being generated.
  * @param radComponent the radComponent in which this property is used
  * @return the real property value to be used during design-time
  */
  public Object getDesignValue (RADComponent radComponent);
}

/*
 * Log
 *  3    Gandalf   1.2         6/27/99  Ian Formanek    Added constant 
 *       IGNORED_VALUE
 *  2    Gandalf   1.1         6/27/99  Ian Formanek    implements serializable,
 *       getDesignValue has a RADComponent parameter
 *  1    Gandalf   1.0         5/23/99  Ian Formanek    
 * $
 */
