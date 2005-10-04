/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
