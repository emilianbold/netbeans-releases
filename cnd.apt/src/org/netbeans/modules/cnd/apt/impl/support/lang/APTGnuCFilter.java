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

package org.netbeans.modules.cnd.apt.impl.support.lang;

import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * filter for GNU C language
 * @author Vladimir Voskresensky
 */
public class APTGnuCFilter extends APTStdCFilter {
    
    /** Creates a new instance of APTGnuCFilter */
    public APTGnuCFilter() {
        initialize();
    }
    
    private void initialize() {
        // GNU C extensions 
        filter("__alignof__", APTTokenTypes.LITERAL_alignof); // NOI18N
        filter("__asm", APTTokenTypes.LITERAL_asm); // NOI18N
        filter("__asm__", APTTokenTypes.LITERAL_asm); // NOI18N
        filter("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        filter("__complex__", APTTokenTypes.LITERAL___complex); // NOI18N
        filter("__const", APTTokenTypes.LITERAL_const); // NOI18N
        filter("__const__", APTTokenTypes.LITERAL_const); // NOI18N
        filter("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        filter("__inline", APTTokenTypes.LITERAL_inline); // NOI18N
        filter("__inline__", APTTokenTypes.LITERAL_inline); // NOI18N
        filter("__real__", APTTokenTypes.LITERAL___real); // NOI18N
        filter("__signed", APTTokenTypes.LITERAL_signed); // NOI18N
        filter("__signed__", APTTokenTypes.LITERAL_signed); // NOI18N
        filter("__typeof", APTTokenTypes.LITERAL_typeof); // NOI18N
        filter("__typeof__", APTTokenTypes.LITERAL_typeof); // NOI18N
        filter("__volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        filter("__volatile__", APTTokenTypes.LITERAL_volatile); // NOI18N        
    }
}
