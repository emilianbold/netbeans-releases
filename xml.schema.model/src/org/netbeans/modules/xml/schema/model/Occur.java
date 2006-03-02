/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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


package org.netbeans.modules.xml.schema.model;

/**
 *
 * @author nn136682
 */
public interface Occur {
    
    public static enum ZeroOne implements Occur {
        ZERO, ONE;
    
        public static ZeroOne valueOfNumeric(String owner, String s) {
            int v = Integer.valueOf(s).intValue();
            if (v < 0 || v > 1) {
                throw new IllegalArgumentException("'" + owner + "' can only has value 0 or 1");
            }
            return v == 0 ? ZeroOne.ZERO : ZeroOne.ONE;
        }
        
        public String toString() {
            return this == ZeroOne.ZERO ? "0" : "1";
        }
    }
}
