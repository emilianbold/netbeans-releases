/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.editor.fortran.options;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;

/** 
 * 
 * @author Alexander Simon
 */
public final class FortranCodeStyle {
    
    private Preferences preferences;
    private static boolean autoFormatDetection = true;
    
    private FortranCodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    /** For testing purposes only */
    public static FortranCodeStyle get(Preferences prefs) {
        return new FortranCodeStyle(prefs);
    }

    /** For testing purposes only */
    public static void setAutoFormatDetection(boolean autoFormatDetection) {
        FortranCodeStyle.autoFormatDetection = autoFormatDetection;
    }

    public InputAttributes setupLexerAttributes(Document doc){
        InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (lexerAttrs == null) {
            lexerAttrs = new InputAttributes();
            doc.putProperty(InputAttributes.class, lexerAttrs);
        }
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, getRrightMargin(), true);
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_FREE_FORMAT, isFreeFormatFortran(), true);
        return lexerAttrs;
    }

    public static FortranCodeStyle get(Document doc) {
        Preferences pref = CodeStylePreferences.get(doc).getPreferences();
        boolean freeFormat;
        if (autoFormatDetection) {
            freeFormat = CndLexerUtilities.detectFortranFormat(doc);
        } else {
            freeFormat = pref.getBoolean(FmtOptions.freeFormat, FmtOptions.getDefaultAsBoolean(FmtOptions.freeFormat));
        }
        Preferences delegate = new MyPreferences(pref, freeFormat);
        return new FortranCodeStyle(delegate);
    }

    public boolean absoluteLabelIndent() {
        return true;
    }

    // General tabs and indents ------------------------------------------------
    
    public boolean expandTabToSpaces() {
        return preferences.getBoolean(FmtOptions.expandTabToSpaces, FmtOptions.getDefaultAsBoolean(FmtOptions.expandTabToSpaces));
    }

    public int getTabSize() {
        return preferences.getInt(FmtOptions.tabSize, FmtOptions.getDefaultAsInt(FmtOptions.tabSize));
    }

    public int getRrightMargin() {
        return preferences.getInt(FmtOptions.rightMargin, FmtOptions.getDefaultAsInt(FmtOptions.rightMargin));
    }

    public boolean indentCasesFromSwitch() {
        return true;
    }

    public CodeStyle.PreprocessorIndent indentPreprocessorDirectives() {
        return CodeStyle.PreprocessorIndent.START_LINE;
    }

    public int indentSize() {
        return preferences.getInt(FmtOptions.indentSize, FmtOptions.getDefaultAsInt(FmtOptions.indentSize));
    }

    public boolean isFreeFormatFortran() {
        return preferences.getBoolean(FmtOptions.freeFormat, FmtOptions.getDefaultAsBoolean(FmtOptions.freeFormat));
    }

    /** For testing purposes only */
    public void setFreeFormatFortran(boolean freeFormat) {
        preferences.putBoolean(FmtOptions.freeFormat, freeFormat);
    }

    public boolean sharpAtStartLine() {
        return true;
    }

    public boolean spaceAfterComma() {
        return true;
    }

    public boolean spaceAroundAssignOps() {
        return true;
    }

    public boolean spaceAroundBinaryOps() {
        return true;
    }

    public boolean spaceAroundUnaryOps() {
        return false;
    }

    public boolean spaceBeforeComma() {
        return false;
    }

    public boolean spaceBeforeForParen() {
        return true;
    }

    public boolean spaceBeforeIfParen() {
        return true;
    }

    public boolean spaceBeforeKeywordParen() {
        return true;
    }

    public boolean spaceBeforeMethodCallParen() {
        return false;
    }

    public boolean spaceBeforeMethodDeclParen() {
        return false;
    }

    public boolean spaceBeforeSwitchParen() {
        return true;
    }

    public boolean spaceBeforeWhile() {
        return true;
    }

    public boolean spaceBeforeWhileParen() {
        return true;
    }

    public boolean spaceWithinForParens() {
        return false;
    }

    public boolean spaceWithinIfParens() {
        return false;
    }

    public boolean spaceWithinMethodCallParens() {
        return false;
    }

    public boolean spaceWithinMethodDeclParens() {
        return false;
    }

    public boolean spaceWithinParens() {
        return false;
    }

    public boolean spaceWithinSwitchParens() {
        return false;
    }

    public boolean spaceWithinWhileParens() {
        return false;
    }

    private static final class MyPreferences extends Preferences {
        private final Preferences delegate;
        private boolean freeFormat;

        MyPreferences(Preferences delegate, boolean freeFormat) {
            this.delegate = delegate;
            this.freeFormat = freeFormat;
        }

        @Override
        public void put(String key, String value) {
            delegate.put(key, value);
        }

        @Override
        public String get(String key, String def) {
            return delegate.get(key, def);
        }

        @Override
        public void remove(String key) {
            delegate.remove(key);
        }

        @Override
        public void clear() throws BackingStoreException {
            delegate.clear();
        }

        @Override
        public void putInt(String key, int value) {
            delegate.putInt(key, value);
        }

        @Override
        public int getInt(String key, int def) {
            return delegate.getInt(key, def);
        }

        @Override
        public void putLong(String key, long value) {
            delegate.putLong(key, value);
        }

        @Override
        public long getLong(String key, long def) {
            return delegate.getLong(key, def);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            if (FortranCodeStyle.autoFormatDetection && FmtOptions.freeFormat.equals(key)){
                freeFormat = value;
            }
            delegate.putBoolean(key, value);
        }

        @Override
        public boolean getBoolean(String key, boolean def) {
            if (FortranCodeStyle.autoFormatDetection && FmtOptions.freeFormat.equals(key)){
                return freeFormat;
            }
            return delegate.getBoolean(key, def);
        }

        @Override
        public void putFloat(String key, float value) {
            delegate.putFloat(key, value);
        }

        @Override
        public float getFloat(String key, float def) {
            return delegate.getFloat(key, def);
        }

        @Override
        public void putDouble(String key, double value) {
            delegate.putDouble(key, value);
        }

        @Override
        public double getDouble(String key, double def) {
            return delegate.getDouble(key, def);
        }

        @Override
        public void putByteArray(String key, byte[] value) {
            delegate.putByteArray(key, value);
        }

        @Override
        public byte[] getByteArray(String key, byte[] def) {
            return delegate.getByteArray(key, def);
        }

        @Override
        public String[] keys() throws BackingStoreException {
            return keys();
        }

        @Override
        public String[] childrenNames() throws BackingStoreException {
            return delegate.childrenNames();
        }

        @Override
        public Preferences parent() {
            return parent();
        }

        @Override
        public Preferences node(String pathName) {
            return delegate.node(pathName);
        }

        @Override
        public boolean nodeExists(String pathName) throws BackingStoreException {
            return delegate.nodeExists(pathName);
        }

        @Override
        public void removeNode() throws BackingStoreException {
            removeNode();
        }

        @Override
        public String name() {
            return name();
        }

        @Override
        public String absolutePath() {
            return delegate.absolutePath();
        }

        @Override
        public boolean isUserNode() {
            return delegate.isUserNode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void flush() throws BackingStoreException {
            delegate.flush();
        }

        @Override
        public void sync() throws BackingStoreException {
            delegate.sync();
        }

        @Override
        public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
            delegate.addPreferenceChangeListener(pcl);
        }

        @Override
        public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
            delegate.removePreferenceChangeListener(pcl);
        }

        @Override
        public void addNodeChangeListener(NodeChangeListener ncl) {
            delegate.addNodeChangeListener(ncl);
        }

        @Override
        public void removeNodeChangeListener(NodeChangeListener ncl) {
            delegate.removeNodeChangeListener(ncl);
        }

        @Override
        public void exportNode(OutputStream os) throws IOException, BackingStoreException {
            delegate.exportNode(os);
        }

        @Override
        public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
            delegate.exportSubtree(os);
        }
    }
}
