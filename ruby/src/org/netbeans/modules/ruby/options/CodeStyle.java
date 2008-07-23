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

package org.netbeans.modules.ruby.options;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;

import static org.netbeans.modules.ruby.options.FmtOptions.*;

/** 
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 * 
 * @author Dusan Balek
 */
public final class CodeStyle {
    
    static {
        FmtOptions.codeStyleProducer = new Producer();
    }
    
    private Preferences preferences;
    
    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    /** For testing purposes only */
    public static CodeStyle getTestStyle(Preferences prefs) {
        return new CodeStyle(prefs);
    }
    
    static {
        FmtOptions.codeStyleProducer = new Producer();
    }
    
    public synchronized static CodeStyle getDefault(Project project) {
        return FmtOptions.codeStyleProducer.create(FmtOptions.getPreferences(project));
    }
    
    // General tabs and indents ------------------------------------------------
    
    public boolean expandTabToSpaces() {
        return preferences.getBoolean(expandTabToSpaces, getGlobalExpandTabToSpaces());
    }

    public int getTabSize() {
        return preferences.getInt(tabSize, getGlobalTabSize());
    }

    public int getIndentSize() {
        int indentLevel = preferences.getInt(indentSize, getGlobalIndentSize());

        if (indentLevel <= 0) {
            boolean expandTabs = preferences.getBoolean(expandTabToSpaces, getGlobalExpandTabToSpaces());
            if (expandTabs) {
                indentLevel = preferences.getInt(spacesPerTab, getGlobalSpacesPerTab());
            } else {
                indentLevel = preferences.getInt(tabSize, getGlobalTabSize());
            }
        }
        
        return indentLevel;
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(continuationIndentSize, getDefaultAsInt(continuationIndentSize));
    }

    public boolean reformatComments() {
        return preferences.getBoolean(reformatComments, getDefaultAsBoolean(reformatComments));
    }

    public boolean indentHtml() {
        return preferences.getBoolean(indentHtml, getDefaultAsBoolean(indentHtml));
    }
    
    public int getRightMargin() {
        return preferences.getInt(rightMargin, getGlobalRightMargin());
    }

    // Communication with non public packages ----------------------------------
    
    private static class Producer implements FmtOptions.CodeStyleProducer {

        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
        
    } 
}
