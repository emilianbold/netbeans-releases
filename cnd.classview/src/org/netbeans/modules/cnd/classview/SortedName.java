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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

/**
 *
 * @author Alexander Simon
 */
public class SortedName implements Comparable<SortedName> {
    private byte prefix;
    private String name;
    private byte suffix;
    public SortedName(int prefix, String name, int suffix){
        this.prefix = (byte)prefix;
        this.name = name;
        this.suffix = (byte)suffix;
    }
    
    public byte getPrefix(){
        return prefix;
    }
    
    public int compareTo(SortedName o) {
        int i = prefix - o.prefix;
        if (i == 0){
            i = name.compareTo(o.name);
            if (i == 0){
                i = suffix - o.suffix;
            }
        }
        return i;
    }
}

