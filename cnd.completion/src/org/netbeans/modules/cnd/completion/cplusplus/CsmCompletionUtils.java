/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.cplusplus;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 * Misc static utility functions
 * @author Vladimir Kvashin
 */
public class CsmCompletionUtils {
    public static final String CPP_AUTO_COMPLETION_TRIGGERS = "cppAutoCompletionTriggers"; //NOI18N
    public static final String PREPRPOC_AUTO_COMPLETION_TRIGGERS = "autoCompletionTriggersPreproc"; //NOI18N
    public static final String CPP_AUTO_INSERT_INCLUDE_DIRECTIVES = "autoInsertIncludeDirectives"; //NOI18N

    private CsmCompletionUtils() {
    }

    /**
     * Gets the mime type of a document. If the mime type can't be determined
     * this method will return <code>null</code>. This method should work reliably
     * for Netbeans documents that have their mime type stored in a special
     * property. For any other documents it will probably just return <code>null</code>.
     *
     * @param doc The document to get the mime type for.
     *
     * @return The mime type of the document or <code>null</code>.
     * @see NbEditorDocument#MIME_TYPE_PROP
     */
    public  static String getMimeType(Document doc) {
        return (String)doc.getProperty(BaseDocument.MIME_TYPE_PROP); //NOI18N
    }

    /**
     * Gets the mime type of a document in <code>JTextComponent</code>. If
     * the mime type can't be determined this method will return <code>null</code>.
     * It tries to determine the document's mime type first and if that does not
     * work it uses mime type from the <code>EditorKit</code> attached to the
     * component.
     *
     * @param component The component to get the mime type for.
     *
     * @return The mime type of a document opened in the component or <code>null</code>.
     */
    public static String getMimeType(JTextComponent component) {
        if (component == null) {
            return "";
        }
        Document doc = component.getDocument();
        String mimeType = getMimeType(doc);
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    public static boolean isCaseSensitive(String mimeType) {
        if (mimeType == null || mimeType.length() == 0) {
            return false;
        }
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
    }

    public static boolean isNaturalSort(String mimeType) {
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_NATURAL_SORT, false);
    }

    public static boolean isAutoInsertIncludeDirectives() {
        String mimeType = MIMENames.CPLUSPLUS_MIME_TYPE; // now all settings are from C++
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.getBoolean(CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, true);
    }
}
