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
