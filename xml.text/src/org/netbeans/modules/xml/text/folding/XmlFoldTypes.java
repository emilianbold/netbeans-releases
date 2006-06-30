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

package org.netbeans.modules.xml.text.folding;

import org.netbeans.api.editor.fold.FoldType;

/**
 * This class defines @see org.netbeans.api.editor.fold.FoldType
 * instancies used in XML code folding.
 *
 * @author  mf100882
 */

public class XmlFoldTypes {

    private static final String FOLD_TYPE_PREFIX = "xml-";//NOI18N

    /** XML tag fold type */
    public static final FoldType TAG = new FoldType(FOLD_TYPE_PREFIX + "tag"); // NOI18N

    /** XML comment fold type */
    public static final FoldType COMMENT = new FoldType(FOLD_TYPE_PREFIX + "comment"); // NOI18N

    /** XML processing instruction fold type */
    public static final FoldType PI = new FoldType(FOLD_TYPE_PREFIX + "pi"); // NOI18N

    /** XML doctype fold type */
    public static final FoldType DOCTYPE = new FoldType(FOLD_TYPE_PREFIX + "doctype"); // NOI18N

    /** XML cdata section fold type */
    public static final FoldType CDATA = new FoldType(FOLD_TYPE_PREFIX + "cdata"); // NOI18N

    /** XML content section fold type */
    public static final FoldType TEXT = new FoldType(FOLD_TYPE_PREFIX + "text"); // NOI18N

}
