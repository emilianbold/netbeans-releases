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

package org.netbeans.modules.form.editors2;

import javax.swing.AbstractListModel;

/** A property editor for ListModel.
* @author  Ian Formanek
*/
public class ListModelEditor extends Object {

  public static class NbListModel extends AbstractListModel implements java.io.Serializable {
    static final long serialVersionUID =-1806930448622666787L;
    public NbListModel (String[] data) {
      this.data = data;
    }
    
    public int getSize() { return data.length; }
    public Object getElementAt(int i) { return data[i]; }

    private String[] data;
  }
  
}

/*
 * Log
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         4/12/99  Ian Formanek    
 * $
 */




