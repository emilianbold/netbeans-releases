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

package org.netbeans.modules.form;

import org.openide.nodes.Node;

/**
*
* @author Ian Formanek
*/
public abstract class CodeGenerator {

    public abstract void initialize (FormManager2 formManager);

    /** Alows the code generator to provide synthetic properties for specified component
    * which are specific to the code generation method.
    * E.g. a JavaCodeGenerator will return variableName property, as it generates
    * global Java variable for every component
    * @param component The RADComponent for which the properties are to be obtained
    */
    public Node.Property[] getSyntheticProperties (RADComponent component) {
        return new Node.Property[0];
    }

    // -----------------------------------------------------------------------------
    // Event handlers

    /** Generates the specified event handler, if it does not exist yet.
    * @param handlerName The name of the event handler
    * @param paramTypes the list of event handler parameter types
    * @param bodyText the body text of the event handler or null for default (empty) one
    * @return true if the event handler have not existed yet and was creaated, false otherwise
    */
    public abstract boolean generateEventHandler (String handlerName, String[] paramTypes, String bodyText);

    /** Changes the text of the specified event handler, if it already exists.
    * @param handlerName The name of the event handler
    * @param paramTypes the list of event handler parameter types
    * @param bodyText the new body text of the event handler or null for default (empty) one
    * @return true if the event handler existed and was modified, false otherwise
    */
    public abstract boolean changeEventHandler (final String handlerName, final String[] paramTypes, final String bodyText);

    /** Removes the specified event handler - removes the whole method together with the user code!
    * @param handlerName The name of the event handler
    */
    public abstract boolean deleteEventHandler (String handlerName);

    /** Renames the specified event handler to the given new name.
    * @param oldHandlerName The old name of the event handler
    * @param newHandlerName The new name of the event handler
    * @param paramTypes the list of event handler parameter types
    */
    public abstract boolean renameEventHandler (String oldHandlerName, String newHandlerName, String[] paramTypes);

    /** Focuses the specified event handler in the editor. */
    public abstract void gotoEventHandler (String handlerName);
}

/*
 * Log
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/15/99  Ian Formanek    
 *  5    Gandalf   1.4         5/12/99  Ian Formanek    
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */

