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

package org.netbeans.modules.soa.mapper.common.basicmapper.literal;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;


/**
 * The real interaction with editors is placed into this type.
 * The resulting value of the editor, when it closes, is
 * available via this interface.
 *
 * @author Josh Sandusky
 */
public interface ILiteralUpdater {

    /**
     * Returns the literal editor associated with this updater.
     */
    public ILiteralEditor getEditor(IBasicMapper basicMapper, IFieldNode field);
    
    /**
     * Returns whether an editor is associated with this updater.
     */
    public boolean hasEditor();
    
    /**
     * Called by the editor when the editor closes with
     * a valid value. This method is not called if the
     * user entered invalid data into the editor or
     * cancelled the edit.
     * 
     * @return the new value (possibly modified by literalSet)
     */
    public String literalSet(IFieldNode fieldNode, String newValue);
    
    /**
     * Called when a field node with an in-place
     * literal needs to remove the in-place literal.
     */
    public void literalUnset(IFieldNode fieldNode);
    
    /**
     * Used by certain editors for determining what to do with
     * text that the user may have entered.
     */
    public LiteralSubTypeInfo getLiteralSubType(String freeTextValue);
    
    /**
     * Return the modified-version of the specified text that
     * should be presented to the user. An example of this is
     * where literalText may represent a string, but this string
     * should be wrapped in double quotes before being displayed.
     */
    public String getLiteralDisplayText(String literalText);
    
    
    /**
     * Used by certain editors for determining what to do with
     * text that the user may have entered.
     */
    public class LiteralSubTypeInfo {
        private String mValue;
        private String mType;
        public LiteralSubTypeInfo(String type, String value) {
            mType = type;
            mValue = value;
        }
        public String getValue() {
            return mValue;
        }
        public String getType() {
            return mType;
        }
    }
}
