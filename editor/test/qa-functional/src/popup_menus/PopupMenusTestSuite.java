/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package popup_menus;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Test behavior of Editor Popup Menus
 *
 * @author Martin Roskanin
 */
  public class PopupMenusTestSuite extends NbTestSuite {
      
    public PopupMenusTestSuite() {
        super("Popup Menus");
        
        addTestSuite(MainMenuTest.class);
    }
    

    public static NbTestSuite suite() {
        return new PopupMenusTestSuite();
    }
    
}
