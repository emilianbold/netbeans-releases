/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * HMTest.java
 *
 * Created on January 7, 2002, 3:13 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
/**
 *
 * @author  mb115822
 */
public class HMTest extends XMLBean {

    /** Creates a new instance of HMTest */
    public HMTest() {
    }
    
    public String xmlat_module;
    public String xmlat_class;
    public String xmlat_name;
    public String xmlat_suiteName;
    public String xmlat_testType;
    public String xmlat_testBagName;
    public String xmlat_repositoryName;
    
    public HMTestedBuild xmlel_HMTestedBuild[];

}
