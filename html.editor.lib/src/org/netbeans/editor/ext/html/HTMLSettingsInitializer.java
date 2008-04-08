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

package org.netbeans.editor.ext.html;

import java.util.Map;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;

/**
* Extended settings provide the settings for the extended editor features
* supported by the various classes of this package.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class HTMLSettingsInitializer extends Settings.AbstractInitializer {

    private final Class htmlKitClass;

    /** Name assigned to initializer */
    public static final String NAME = "html-settings-initializer"; // NOI18N
    
    public static final Acceptor HTML_IDENTIFIER_ACCEPTOR = 
        new Acceptor() {
            public final boolean accept(char ch) {
                return (ch == ':') || AcceptorFactory.JAVA_IDENTIFIER.accept(ch);
            }
        };


    /** Construct HTML Settings initializer
    * @param htmlKitClass the real kit class for which the settings are created.
    *   It's unknown here so it must be passed to this constructor.
    */ 
    public HTMLSettingsInitializer(Class htmlKitClass) {
        super(NAME);
        this.htmlKitClass = htmlKitClass;
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == htmlKitClass) {

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    HTMLTokenContext.context
                }
            );
            
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, new Boolean(true));
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            HTML_IDENTIFIER_ACCEPTOR);

            settingsMap.put(HTMLSettingsNames.COMPLETION_LOWER_CASE,
                            HTMLSettingsDefaults.defaultCompletionLowerCase);
            
        }
    }
}
