/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
