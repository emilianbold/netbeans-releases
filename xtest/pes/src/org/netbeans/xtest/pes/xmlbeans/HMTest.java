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
