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

package org.netbeans.installer;

/** Auxiliary class used just to pass JDK info (home and version) between
  JDKSearchAction and JDKSelectionPanel in separate from to avoid trouble
  with trimming version from concatenated string.
 */
public class JDKInfoAux {
    
    private String jdkHome;
    private String jdkVersion;
            
    public JDKInfoAux (String jdkHome, String jdkVersion) {
        this.jdkHome = jdkHome;
        this.jdkVersion = jdkVersion;
    }
    
    public String getHome() {
        return jdkHome;
    }
    
    public String getVersion() {
        return jdkVersion;
    }
    
}
