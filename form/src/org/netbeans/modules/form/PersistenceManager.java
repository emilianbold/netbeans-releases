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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
*
* @author Ian Formanek
*/
public abstract class PersistenceManager {

    private static ArrayList managers = new ArrayList (5);

    // -----------------------------------------------------------------------------
    // Static accessors to registered PersistenceManagers

    public static void registerManager (PersistenceManager manager) {
        managers.add (manager);
    }

    public static Iterator getManagers () {
        return managers.iterator ();
    }

    // -----------------------------------------------------------------------------
    // abstract interface

    /** A method which allows the persistence manager to provide infotrmation on whether
    * is is capable to store info about advanced features provided from Developer 3.0 
    * - all persistence managers except the one providing backward compatibility with 
    * Developer 2.X should return true from this method.
    * @return true if this PersistenceManager is capable to store advanced form features, false otherwise
    */
    public abstract boolean supportsAdvancedFeatures ();

    /** A method which allows the persistence manager to check whether it can read
    * given form format.
    * @return true if this PersistenceManager can load form stored in the specified form, false otherwise
    * @exception IOException if any problem occured when accessing the form
    */
    public abstract boolean canLoadForm (FormDataObject formObject) throws IOException;

    /** Called to actually load the form stored in specified formObject.
    * @param formObject the FormDataObject which represents the form files
    * @return the FormManager2 representing the loaded form or null if some problem occured
    * @exception IOException if any problem occured when loading the form
    */
    public abstract FormManager2 loadForm (FormDataObject formObject) throws IOException;

    /** Called to actually save the form represented by specified FormManager2 into specified formObject.
    * @param formObject the FormDataObject which represents the form files
    * @param manager the FormManager2 representing the form to be saved
    * @exception IOException if any problem occured when saving the form
    */
    public abstract void saveForm (FormDataObject formObject, FormManager2 manager) throws IOException;
}

/*
 * Log
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/11/99  Ian Formanek    supportsAdvancedFeatures
 *       added
 *  4    Gandalf   1.3         5/30/99  Ian Formanek    Removed obsoleted field
 *  3    Gandalf   1.2         5/15/99  Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
