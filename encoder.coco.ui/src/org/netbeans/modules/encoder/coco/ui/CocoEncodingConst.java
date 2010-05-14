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

package org.netbeans.modules.encoder.coco.ui;

/**
 * Class for holding COBOL Copybook encoding related constants.
 *
 * @author Jun Xu
 */
public abstract class CocoEncodingConst {
    
    public static final String STYLE = "cocoencoder-1.0"; //NOI18N
    
    public static final String URI = "urn:com.sun:encoder-coco-1.0"; //NOI18N
    
    public static final String NAME = "COBOL Copybook Encoding"; //NOI18N

    public static final String DEFAULT_COBOL_EXT = "cobol"; //NOI18N

    public static final String[] COBOL_EXTS = {"cpy", "cobol", "cob", "cbl", "ccc", "ccp"}; //NOI18N

    public static final String XSD_EXT = "xsd"; //NOI18N

    public static boolean isCOBOL(String filename) {
        if (filename == null) {
            return false;
        }
        filename = filename.toLowerCase();
        for (int i = 0; i < COBOL_EXTS.length; i++) {
            if (filename.endsWith("." + COBOL_EXTS[i])) {
                return true;
            }
        }
        return false;
    }
}
