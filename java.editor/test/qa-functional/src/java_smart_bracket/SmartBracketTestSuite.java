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

package java_smart_bracket;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class SmartBracketTestSuite extends NbTestSuite {
      
    public SmartBracketTestSuite() {
        super("Smart Brackets");
        
        addTestSuite(JavaSmartBracketTest.class);
    }
    

    public static NbTestSuite suite() {
        return new SmartBracketTestSuite();
    }
    
}
