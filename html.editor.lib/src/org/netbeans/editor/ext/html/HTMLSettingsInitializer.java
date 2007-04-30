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
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            HTML_IDENTIFIER_ACCEPTOR);

            settingsMap.put(HTMLSettingsNames.COMPLETION_LOWER_CASE,
                            HTMLSettingsDefaults.defaultCompletionLowerCase);
            
        }
    }
}
