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

package org.netbeans.modules.encoder.ui.basic;

/**
 * Class for holding encoding framework related constants.
 *
 * @author Jun Xu
 */
public abstract class EncodingConst {

    /**
     * Constant used to indicate the source of appinfo deemed to be encoding
     * related.  Also used as the namespace that governs the elements or
     * attributes that are used to describe generic encoding properties, e.g.,
     * the "top" flag.
     */
    public static final String URI = "urn:com.sun:encoder"; //NOI18N
    
    /**
     * Local name of the element that holds the value of the "top" flag
     */
    public static final String TOP_FLAG = "top"; //NOI18N

    public static final String PRE_DECODE_CHAR_CODING = "preDecodeCharCoding"; //NOI18N

    public static final String POST_ENCODE_CHAR_CODING = "postEncodeCharCoding"; //NOI18N
}
