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

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.spi.AutoTagImporterProvider;
import org.netbeans.modules.web.frameworks.facelets.parser.Parser;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsAutoTagImporter implements AutoTagImporterProvider {
    private static String LINE_SEPARATOR = System.getProperty("line.separator");
    /** Creates a new instance of FaceletsAutoTagImporter */
    public FaceletsAutoTagImporter() {
    }
    
    public void importLibrary(Document doc, String prefix, String uri) {
//        System.out.println("autoImportLibrary: " + prefix + " -> " + uri);
        FileObject fObject = NbEditorUtilities.getFileObject(doc);
        WebModule webModule = WebModule.getWebModule(fObject);
        Map <String, String> prefixes = Parser.getParser(webModule).getPrefixesInRootTag(doc);
        Formatter formatter;
        if (prefixes.get(prefix) == null){
            try {
                String text = doc.getText(0, doc.getLength());
                int offset = Parser.getParser(webModule).getOffsetOfRootTag(doc);
                int startOffset = offset;
                if (offset > -1){
                    String insertText = LINE_SEPARATOR + "xmlns:" + prefix + "=\"" + uri + "\"" ;
                    int index = offset;
                    int closeOffset = text.indexOf('>', offset);
                    while (index > -1 && index < closeOffset){
                        offset = index;
                        index = text.indexOf("xmlns", index+1);
                    }
                    index = offset;
                    if (index > -1)
                        index = text.indexOf('"', index+1);
                    if (index > -1 && index < closeOffset)
                        index = text.indexOf('"', index+1);
                    if (index > -1 && index < closeOffset)
                        offset = index;
                    doc.insertString(offset+1, insertText, null);
                    BaseDocument bdoc = (BaseDocument)doc;
                    formatter = bdoc.getFormatter();
                    formatter.reformatLock();
                    try {
                        bdoc.atomicLock();
                        try {
                             formatter.reformat(bdoc, startOffset, closeOffset + insertText.length());
                        } finally {
                            bdoc.atomicUnlock();
                        }
                    } finally {
                        formatter.reformatUnlock();
                    }
                }
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    public String getDefinedPrefix(Document doc, String uri){
        String usedPrefix = null;
        FileObject fObject = NbEditorUtilities.getFileObject(doc);
        WebModule webModule = WebModule.getWebModule(fObject);
        Map <String, String> prefixes = Parser.getParser(webModule).getPrefixesInRootTag(doc);
        if (prefixes.containsValue(uri.trim())){
            Set<String> usedPrefixs = prefixes.keySet();
            String usedUri;
            for (String prefix : usedPrefixs) {
                usedUri = prefixes.get(prefix);
                if (uri.equals(usedUri)){
                    usedPrefix = prefix;
                    continue;
                }
            }
        }
        return usedPrefix;
    }
}
