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

import java.io.IOException;

import com.netbeans.ide.filesystems.FileObject;

/** 
*
* @author Ian Formanek
*/
public class GandalfPersistenceManager extends PersistenceManager {
  
  /** A method which allows the persistence manager to check whether it can read
  * given form format.
  * @return true if this PersistenceManager can load form stored in the specified form, false otherwise
  * @exception IOException if any problem occured when accessing the form
  */
  public boolean canLoadForm (FormDataObject formObject) throws IOException {
    /* try {
      InputStream is = formObject.getFormEntry ().getFile ().getInputStream();
      byte[] bytes = new byte[4];
      int len = is.read (bytes);
      return ((len == 4) && (bytes[0] == MAGIC_0) && (bytes[1] == MAGIC_1) && (bytes[2] == MAGIC_2) && (bytes[3] == MAGIC_3));
    } catch (Throwable t) {
      if (t instanceof ThreadDeath) {
        throw (ThreadDeath)t;
      }
      
      return false;
    }
*/
    return false;
  }

  /** Called to actually load the form stored in specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @return the FormManager2 representing the loaded form or null if some problem occured
  * @exception IOException if any problem occured when loading the form
  */
  public FormManager2 loadForm (FormDataObject formObject) throws IOException {
    FileObject formFile = formObject.getFormEntry ().getFile ();
    try {
      org.w3c.dom.Document doc = com.netbeans.ide.loaders.XMLDataObject.parse (
          formFile.getURL (),
          new java.util.Hashtable (),
          false,
          false
      );
    } catch (org.xml.sax.SAXException e) {
      throw new IOException (e.getMessage ());
    }
    return null;
  }

  /** Called to actually save the form represented by specified FormManager2 into specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @param manager the FormManager2 representing the form to be saved
  * @exception IOException if any problem occured when saving the form
  */
  public void saveForm (FormDataObject formObject, FormManager2 manager) throws IOException {
  }

}

/*
 * Log
 *  1    Gandalf   1.0         5/30/99  Ian Formanek    
 * $
 */
