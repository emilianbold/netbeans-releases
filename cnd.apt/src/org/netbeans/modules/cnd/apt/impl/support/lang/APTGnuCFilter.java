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
        filter("__alignof__", APTTokenTypes.LITERAL_alignof);
        filter("__asm", APTTokenTypes.LITERAL_asm);
        filter("__asm__", APTTokenTypes.LITERAL_asm);
        filter("__attribute__", APTTokenTypes.LITERAL___attribute__);
        filter("__complex__", APTTokenTypes.LITERAL___complex);
        filter("__const", APTTokenTypes.LITERAL_const);
        filter("__const__", APTTokenTypes.LITERAL_const);
        filter("__imag__", APTTokenTypes.LITERAL___imag);
        filter("__inline", APTTokenTypes.LITERAL_inline);
        filter("__inline__", APTTokenTypes.LITERAL_inline);
        filter("__real__", APTTokenTypes.LITERAL___real);
        filter("__signed", APTTokenTypes.LITERAL_signed);
        filter("__signed__", APTTokenTypes.LITERAL_signed);
        filter("__typeof", APTTokenTypes.LITERAL_typeof);
        filter("__typeof__", APTTokenTypes.LITERAL_typeof);
        filter("__volatile", APTTokenTypes.LITERAL_volatile);
        filter("__volatile__", APTTokenTypes.LITERAL_volatile);        
    }
}
