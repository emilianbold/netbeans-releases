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

package org.netbeans.modules.editor.indent;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.indent.api.IndentUtils;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class FormatterImpl extends ExtFormatter {
    
    private final Document doc;
    private final Formatter defaultFormatter;
    private final IndentImpl indentImpl;
    
    private final boolean tabSizeOverriden;
    private final boolean shiftWidthOverriden;
    private final boolean expandTabsOverriden;
    
    FormatterImpl(Formatter defaultFormatter, Document doc) {
        super(defaultFormatter.getKitClass());
        
        this.defaultFormatter = defaultFormatter;
        this.tabSizeOverriden = isOverriden(defaultFormatter.getClass(), "getTabSize"); //NOI18N
        this.shiftWidthOverriden = isOverriden(defaultFormatter.getClass(), "getShiftWidth"); //NOI18N
        this.expandTabsOverriden = isOverriden(defaultFormatter.getClass(), "expandTabs"); //NOI18N

        this.doc = doc;
        assert doc != null;
        
        this.indentImpl = IndentImpl.get(doc);
        indentImpl.setDefaultFormatter(defaultFormatter);
    }
    
    @Override
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        return (defaultFormatter instanceof ExtFormatter)
                ? ((ExtFormatter)defaultFormatter).getReformatBlock(target, typedText)
                : null;
    }
    
    @Override
    public void indentLock() {
        indentImpl.indentLock();
    }
    
    @Override
    public void indentUnlock() {
        indentImpl.indentUnlock();
    }
    
    @Override
    public void reformatLock() {
        indentImpl.reformatLock();
    }
    
    @Override
    public void reformatUnlock() {
        indentImpl.reformatUnlock();
    }

    @Override
    public int getTabSize() {
        if (tabSizeOverriden) {
            return defaultFormatter.getTabSize();
        } else {
            return IndentUtils.tabSize(doc);
        }
    }

    @Override
    public int getSpacesPerTab() {
        return defaultFormatter.getSpacesPerTab();
    }

    @Override
    public int getShiftWidth() {
        if (shiftWidthOverriden) {
            return defaultFormatter.getShiftWidth();
        } else {
            return IndentUtils.indentLevelSize(doc);
        }
    }

    @Override
    public boolean expandTabs() {
        if (expandTabsOverriden) {
            return defaultFormatter.expandTabs();
        } else {
            return IndentUtils.isExpandTabs(doc);
        }
    }

    @Override
    public int indentLine(Document doc, int offset) {
        return indentLine(doc, offset, false);
    }

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    @Override
    public int indentNewLine(Document doc, int offset) {
        return indentLine(doc, offset, true);
    }

    private int indentLine(Document doc, int offset, boolean indentNewLine) {
        try {
            return indentImpl.reindent(offset, offset, offset, indentNewLine);
        } catch (GuardedException e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        return offset;
    }

    @Override
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
    boolean indentOnly) throws BadLocationException, IOException {
        // TBD delegate somehow
        return (defaultFormatter instanceof ExtFormatter)
                ? ((ExtFormatter)defaultFormatter).reformat(doc, startOffset, endOffset, indentOnly)
                : null;
    }
    
    @Override
    public int reformat(BaseDocument doc, int startOffset, int endOffset)
    throws BadLocationException {
        if (doc != indentImpl.document())
            return endOffset - startOffset; // should not happen in reality
        indentImpl.reformat(startOffset, endOffset);
        TaskHandler handler = indentImpl.reformatHandler();
        return (handler != null && handler.hasItems())
                ? Math.max(handler.endPos().getOffset() - startOffset, 0)
                : endOffset - startOffset;
    }

    @Override
    public Writer createWriter(Document doc, int offset, Writer writer) {
        if (doc != indentImpl.document()) {
            throw new IllegalStateException("Unexpected document doc=" + doc); //NOI18N
        }
        return new FormatterWriterImpl(indentImpl, offset, writer);
    }

    private static boolean isOverriden(Class clazz, String methodName) {
        while (clazz != Formatter.class && clazz != ExtFormatter.class) {
            for(Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(methodName) && m.getParameterTypes().length == 0) {
                    return true;
                }
            }

            clazz = clazz.getSuperclass();
        }

        return false;
    }
}
