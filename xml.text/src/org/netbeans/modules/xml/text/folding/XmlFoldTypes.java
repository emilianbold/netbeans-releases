/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

}
