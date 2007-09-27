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

package org.netbeans.modules.iep.editor.tcg.ps;


import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.exception.I18nException;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.model.TcgPropertyType;
import org.netbeans.modules.iep.editor.tcg.util.Bundle;
import org.netbeans.modules.iep.editor.tcg.util.Configuration;

public class TcgPsI18n implements SharedConstants {
    private static Bundle mAppBundle = Bundle.getInstance(Configuration.getVarByName("psBundle"));

    public static String getDisplayName(TcgComponentType type) {
        String titleKey = type.getTitle();
        String title = mAppBundle.getString(titleKey, titleKey);
        return title;
    }
    
    public static String getToolTip(TcgComponentType type) {
        String descKey = type.getDescription();
        String desc = mAppBundle.getString(descKey, descKey);
        return desc;
    }        

        
    public static String getDisplayName(TcgComponent comp) {
        if (comp.hasProperty(NAME_KEY)) {
            try {
                return comp.getProperty(NAME_KEY).getStringValue();
            } catch (I18nException e) {
                return comp.getTitle();
            }
        }
        return comp.getTitle();
    }
    
    public static String getToolTip(TcgComponent comp) {
        return getToolTip(comp.getType());
    }

    
    public static String getDisplayName(TcgPropertyType pt) {
        String titleKey = pt.getTitle();
        String title = mAppBundle.getString(titleKey, titleKey);
        return title;
    }
    
    public static String getToolTip(TcgPropertyType pt) {
        String descKey = pt.getDescription();
        String desc = mAppBundle.getString(descKey, descKey);
        return desc;
    }
    
    public static String getCatetoryDisplayName(TcgPropertyType pt) {
        String key = pt.getCategory();
        String display = mAppBundle.getString(key, key);
        return display;
    }
    
    public static String getI18nString(String key) {
        return mAppBundle.getString(key, key);
    }
    
}
