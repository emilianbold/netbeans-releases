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

package com.netbeans.developer.modules.text.options;

import org.openide.options.ContextSystemOption;
import org.openide.util.NbBundle;
import org.openide.text.PrintSettings;

/**
* Root node for all available editor options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AllOptions extends ContextSystemOption {

  public AllOptions() {
    addOption(new PlainOptions());
    addOption(new JavaOptions());
    addOption(new HTMLOptions());

    PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
    ps.addOption(new PrintOptions());
    ps.addOption(new JavaPrintOptions());
    ps.addOption(new HTMLPrintOptions());
  }

  public String displayName() {
    return NbBundle.getBundle(PlainOptions.class).getString("OPTIONS_all");
  }

}

/*
 * Log
 *  3    Gandalf   1.2         7/9/99   Ales Novak      print options change
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */
