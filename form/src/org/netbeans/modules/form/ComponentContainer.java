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

/**
 *
 * @author  Ian Formanek
 */
public interface ComponentContainer {

  public RADComponent[] getSubBeans ();

  public void initSubComponents (RADComponent[] initComponents);

  public void reorderSubComponents (int[] perm);

  public void add (RADComponent comp);

  public void remove (RADComponent comp);

  public int getIndexOf (RADComponent comp);

}

/*
 * Log
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/5/99   Ian Formanek    methods 
 *       add/remove/getIndexOf added
 *  4    Gandalf   1.3         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
