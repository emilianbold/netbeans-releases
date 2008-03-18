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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.text.syntax;

import java.util.*;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.xml.text.indent.XMLIndentEngine;


/**
 * Options for the xml editor kit
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class XMLOptions extends AbstractBaseOptions {
    /** Serial Version UID */
    private static final long serialVersionUID = 2347735706857337892L;

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N
    
    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N                
    
    static final String[] XML_PROP_NAMES = new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                            };
    
    //
    // init
    //

    /** */
    public XMLOptions () {
        super (XMLKit.class, "xml"); // NOI18N
    }

    protected @Override Class getDefaultIndentEngineClass () {
        return XMLIndentEngine.class;
    }
    
    public boolean getCompletionAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP);
    }

    public void setCompletionAutoPopup(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP, v, COMPLETION_AUTO_POPUP_PROP);
    }

    public int getCompletionAutoPopupDelay() {
        return getSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY);
    }

    public void setCompletionAutoPopupDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY, delay,
            COMPLETION_AUTO_POPUP_DELAY_PROP);
    }

    public boolean getCompletionInstantSubstitution() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION);
    }
    
    public void setCompletionInstantSubstitution(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION, v,
            COMPLETION_INSTANT_SUBSTITUTION_PROP);
    }        
    
    // remap old XMLTokenContext to new XMLDefaultTokenContext
    // commented out match by name
    private static final String[][] TRANSLATE_COLORS = {
//        { "xml-comment", "xml-comment" },
//        { "xml-ref", "xml-ref" },
        { "xml-string", "xml-value" },
//        { "xml-attribute", "xml-attribute" },
        { "xml-symbol", "xml-operator" },
//        { "xml-tag", "xml-tag" },
        { "xml-keyword", "xml-doctype" },
        { "xml-plain", "xml-text"},
    };
    
    /**
     * Get coloring, possibly remap setting from previous versions
     * to new one.
     */
    public @Override Map getColoringMap() {
        Map colors = super.getColoringMap();
        
        synchronized (this) {
            // get old customized colors and map them to new token IDs
            // the map will contain only such old colors that was customized AFAIK
            // because current initializer does not create them
            
            for (int i = 0; i<TRANSLATE_COLORS.length; i++) {
                String oldKey = TRANSLATE_COLORS[i][0];
                Object color = colors.get(oldKey);
                if (color != null) {
                    colors.remove(oldKey);
                    String newKey = TRANSLATE_COLORS[i][1];
                    colors.put(newKey, color);
                }
            }
            
            // do not save it explicitly if the user will do a customization
            // it get saved automatically (i.e.old keys removal will apply)
            
            return colors;
        }
    }
    
    protected @Override String getContentType() {
        return XMLKit.MIME_TYPE;
    }
    
}
