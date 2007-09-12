/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
