/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

/** A property editor for Class.
* @author   Jan Jancura
*/
public class ClassEditor extends java.beans.PropertyEditorSupport {

    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *    current value.
     */
    public String getJavaInitializationString() {
        Class clazz = (Class)getValue();
        if (clazz == null) return "null"; // NOI18N
        return "Class.forName (\"" + clazz.getName () + "\")"; // NOI18N
    }

    //----------------------------------------------------------------------

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        Class clazz = (Class)getValue();
        if (clazz == null) return "null"; // NOI18N
        return clazz.getName ();
    }

    /** Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        try {
            setValue (org.openide.TopManager.getDefault ().systemClassLoader ().loadClass (text));
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Petr Jiricka    i18n
 *  4    Gandalf   1.3         1/10/00  Jan Jancura     Bug 1592
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/22/99  Ian Formanek    Changed loading class 
 *       and throwing exception if it fails
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
