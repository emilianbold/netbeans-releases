/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
