/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
