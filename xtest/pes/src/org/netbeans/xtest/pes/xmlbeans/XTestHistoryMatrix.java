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
 * XTestHistoryMatrix.java
 *
 * Created on January 7, 2002, 3:11 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;

import java.util.*;

/**
 *
 * @author  mb115822
 */
public class XTestHistoryMatrix extends XMLBean {

    /** Creates a new instance of XTestHistoryMatrix */
    public XTestHistoryMatrix() {
    }

    public String xmlat_host;
    public String xmlat_project;
    public String xmlat_testingGroup;
    public String xmlat_testedType;

    public HMTest xmlel_HMTest[];

}
