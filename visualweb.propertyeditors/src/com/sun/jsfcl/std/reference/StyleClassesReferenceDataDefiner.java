/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package com.sun.jsfcl.std.reference;

import java.util.List;
import java.util.StringTokenizer;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StyleClassesReferenceDataDefiner extends ReferenceDataDefiner {

    public void addBaseItems(List list) {

        list.add(newItem(
            "", //NOI18N
            null,
            true,
            false));
    }

    public boolean canAddRemoveItems() {
        return false;
    }
    
    public boolean canOrderItems(){
        return false;
    }

    public boolean isValueAString() {

        return true;
    }

    public boolean definesDesignPropertyItems() {
        return true;
    }

    /**
     *  Get the Style Class List stored in the DesignContext Context Data
     *  The data is stored as [class1, class2, class2]. So the chars '['
     *  and ']' should be trimmed
     **/
    public void addDesignPropertyItems(DesignProperty liveProperty, List list) {
        DesignContext liveContext = liveProperty.getDesignBean().getDesignContext();
        String StyleClassList = (String)liveContext.getContextData(Constants.ContextData.
            CSS_STYLE_CLASS_DESCRIPTORS);
        if (StyleClassList != null) {
            StringTokenizer st = new StringTokenizer(StyleClassList, ","); //NOI18N
            while (st.hasMoreTokens()) {
                String className = trimChars(st.nextToken(), "[] ."); //NOI18
                list.add(newItem(
                    className,
                    className,
                    true,
                    false)
                    );
            }
        }
    }

    private String trimChars(String str, String toTrim) {
        boolean trim = false;
        char[] strChars = str.toCharArray();
        char[] trimChars = toTrim.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < strChars.length; i++) {
            for (int j = 0; j < trimChars.length; j++) {
                if (strChars[i] == trimChars[j]) {
                    trim = true;
                    break;
                }
            }
            if (!trim) {
                buffer.append(strChars[i]);
            }
            trim = false;
        }
        return buffer.toString();
    }
}
