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
package org.netbeans.modules.ruby.rhtml;

import java.util.Map;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;


/**
 * The classes in here no one should ever implement, as I would think all this
 * information could be defined in a more declarative way: either via a simple interface
 * implementation where returning specific flags enables a set of settings
 * or a table or some external xml-like file.
 * Maybe that is all there already in NetBeans but I could not find it.
 * This is called from the ModuleInstall class and it's key for the editor to work.
 */
public class RhtmlEditorSettings extends Settings.AbstractInitializer {
    private static final String SETTINGS_NAME = "rhtml-editor-settings-initializer"; // NOI18N

    public RhtmlEditorSettings() {
        super(SETTINGS_NAME);
    }
    
    public static final Acceptor defaultAbbrevResetAcceptor = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isJavaIdentifierPart(ch) && ch != ':';
          }
      };

    @SuppressWarnings("unchecked") // The old API isn't generified yet
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == null) {
            return;
        }

        if (kitClass == RhtmlKit.class) {
            settingsMap.put(SettingsNames.SPACES_PER_TAB, Integer.valueOf(2));
            settingsMap.put(SettingsNames.INDENT_SHIFT_WIDTH, Integer.valueOf(2));
            settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE, Boolean.FALSE);
            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, Boolean.TRUE);
            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE, Boolean.TRUE);
            settingsMap.put(ExtSettingsNames.REINDENT_WITH_TEXT_BEFORE, Boolean.FALSE);
            settingsMap.put(ExtSettingsNames.COMPLETION_AUTO_POPUP, Boolean.TRUE);
            settingsMap.put(SettingsNames.PAIR_CHARACTERS_COMPLETION, Boolean.TRUE);
            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR, defaultAbbrevResetAcceptor);

            //ExtSettingsNames.SHOW_DEPRECATED_MEMBERS
            //ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION
            //ExtSettingsNames.COMPLETION_CASE_SENSITIVE
        }
    }
}
