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

package org.netbeans.modules.php.editor.formatting;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.DocumentUtilities;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.structure.formatting.TransferData;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class GenericTopLevelLanguageFormatter implements Formatter{    
    private static final Logger logger = Logger.getLogger(GenericTopLevelLanguageFormatter.class.getName()); 

    public void reformat(Document document, int startOffset, int endOffset, ParserResult result) {
        BaseDocument doc = (BaseDocument) document;
        doc.atomicLock();
        
        try {
            TransferData transferData = null;
           

            // store data for compatible formatters that will be called later
            transferData = new TransferData();
            transferData.init(doc);

            int firstRefBlockLine = Utilities.getLineOffset(doc, startOffset);
            int lastRefBlockLine = Utilities.getLineOffset(doc, endOffset);

          
            int[] newIndents = new int[transferData.getNumberOfLines()];

            int lineBeforeSelectionBias = 0;
            
            if (firstRefBlockLine > 0){
                lineBeforeSelectionBias = transferData.getOriginalIndent(firstRefBlockLine - 1) - newIndents[firstRefBlockLine - 1];
            }
            
            org.netbeans.editor.Formatter formatter = doc.getFormatter();
            
            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line++) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
                newIndents[line] = lineBeforeSelectionBias;
                
                formatter.changeRowIndent(doc, lineStart, lineBeforeSelectionBias);
            }
            
            transferData.setTransformedOffsets(newIndents);

            if (logger.isLoggable(Level.FINE)) {
                StringBuilder buff = new StringBuilder();

                for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                    int lineStart = Utilities.getRowStartFromLineOffset(doc, i);
                    
                    char formattingRange = (i >= firstRefBlockLine && i <= lastRefBlockLine) 
                            ? '*' : ' ';

                    buff.append(i + ":" + formattingRange + ":" + doc.getText(lineStart, Utilities.getRowEnd(doc, lineStart) - lineStart) + ".\n"); //NOI18N
                }

                buff.append("\n-------------\n"); //NOI18N
                logger.fine(getClass().getName() + ":\n" + buff);
            }

        } catch (BadLocationException e){
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            doc.atomicUnlock();
        }
    }

    public void reindent(Document document, int startOffset, int endOffset, ParserResult result) {
        BaseDocument doc = (BaseDocument) document;
        DataObject dataObject = NbEditorUtilities.getDataObject(doc);

        if (dataObject != null) {
            EditorCookie editor = dataObject.getCookie(EditorCookie.class);

            if (editor != null) {
                int caretPos = editor.getOpenedPanes()[0].getCaretPosition();
                doc.putProperty(TransferData.ORG_CARET_OFFSET_DOCPROPERTY, new Integer(caretPos));
            }
        } else {
            logger.warning("Failed to obtain a DataObject for document");
        }
    }

    public int indentSize() {
        return 0;
    }

    public int hangingIndentSize() {
        return 0;
    }
}
