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


package org.netbeans.modules.tbls.editor.ps;

import org.netbeans.modules.tbls.model.ArrayUtil;

import java.util.logging.Logger;

/** 
 * EnumPropertyEditor
 *
 * @author Bing Lu
 */
public class EnumPropertyEditor extends SingleTcgComponentNodePropertyEditor {
    private static final Logger mLogger = Logger.getLogger(EnumPropertyEditor.class.getName());

    private String[] mTags;
    private Object[] mValues;
    
    public EnumPropertyEditor(String[] tags, Object[] values) {
        mTags = ArrayUtil.duplicate(tags);
        mValues = ArrayUtil.duplicate(values);
    }

    public String[] getTags () {
        return ArrayUtil.duplicate(mTags);
    }

    public String getAsText () {
        Object obj = getValue ();
        for (int i = 0; i < mValues.length; i++) {
            if (obj.equals(mValues[i])) {
                return mTags[i];
            }
        }
        return null;
    }

    public void setAsText (String str) {
        for (int i = 0; i < mTags.length; i++) {
            if (str.equals (mTags[i])) {
                setValue (mValues[i]);
                return;
            }
        }
    }

}
