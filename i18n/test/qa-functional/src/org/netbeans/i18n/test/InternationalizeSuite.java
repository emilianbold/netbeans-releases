/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File CustomizingPropertiesFile.java
 *
 * Created on 19. brezen 2002, 14:50
 *
 * Description :
 *
 * This class is userd for starting test suite in IDE. Test have been
 * writed in Jelly2 ( see testtools.netbeans.org )
 *
 */

package org.netbeans.i18n.test;

import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  Petr Felenda - QA Engineer ( petr.felenda@sun.com )
 */
public class InternationalizeSuite {
    
    /** Creates a new instance of EditingFileSuite class */
    public InternationalizeSuite() {
    }
    
    /** Return test suite */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.setName("Internationalize");
        suite.addTest(new org.netbeans.i18n.test.internationalize.Internationalize());
        return suite;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
}
