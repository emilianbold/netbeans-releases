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

package org.netbeans.modules.web.core.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.indent.Indent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Libor Kotouc
 */
public final class JSPPaletteUtilities {

    public static void insert(String s, JTextComponent target) throws BadLocationException {
        insert(s, target, true);
    }

    public static void insert(String s, JTextComponent target, boolean reformat) throws BadLocationException {
        Document _doc = target.getDocument();
        if (_doc == null || !(_doc instanceof BaseDocument)) {
            return;
        }

        //check whether we are not in a scriptlet
//        JspSyntaxSupport sup = (JspSyntaxSupport)(doc.getSyntaxSupport().get(JspSyntaxSupport.class));
//        int start = target.getCaret().getDot();
//        TokenItem token = sup.getTokenChain(start, start + 1);
//        if (token != null && token.getTokenContextPath().contains(JavaTokenContext.contextPath)) // we are in a scriptlet
//            return false;
        if (s == null) {
            s = "";
        }
        BaseDocument doc = (BaseDocument) _doc;
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.atomicLock();
            try {
                int start = insert(s, target, _doc);
                if (reformat && start >= 0 && _doc instanceof BaseDocument) {
                    // format the inserted text
                    int end = start + s.length();
                    indent.reindent(start, end);
                }
            } finally {
                doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }
    }

    private static FileObject getFileObject(JTextComponent target) {
        BaseDocument doc = (BaseDocument) target.getDocument();
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile() : null;
        return fobj;
    }

    private static int insert(String s, JTextComponent target, Document doc) throws BadLocationException {

        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);

            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return start;
    }

    public static PageInfo.BeanData[] getAllBeans(JTextComponent target) {
        PageInfo.BeanData[] res = null;
        FileObject fobj = getFileObject(target);
        if (fobj != null) {
            JspParserAPI.ParseResult result = JspContextInfo.getContextInfo(fobj).getCachedParseResult(target.getDocument(), fobj, false, true);
            if (result != null) {
                res = result.getPageInfo().getBeans();
            }
        }

        return res;
    }

    public static boolean idExists(String id, PageInfo.BeanData[] beanData) {
        boolean res = false;
        if (id != null && beanData != null) {
            for (int i = 0; i < beanData.length; i++) {
                PageInfo.BeanData beanData1 = beanData[i];
                if (beanData1.getId().equals(id)) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    public static boolean typeExists(JTextComponent target, final String fqcn) {
        final boolean[] result = {false};
        if (fqcn != null) {
            runUserActionTask(target, new Task<CompilationController>() {

                public void run(CompilationController parameter) throws Exception {
                    result[0] = parameter.getElements().getTypeElement(fqcn) != null;
                }
            });
        }
        return result[0];
    }

    private static void runUserActionTask(JTextComponent target, Task<CompilationController> aTask) {
        FileObject fobj = getFileObject(target);
        if (fobj == null) {
            return;
        }
        try {
            JavaSource.forFileObject(fobj).runUserActionTask(aTask, false);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static List<String> getTypeProperties(JTextComponent target, final String fqcn, final String[] prefix) {
        final List<String> result = new ArrayList<String>();
        if (prefix != null) {
            runUserActionTask(target, new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    TypeElement te = parameter.getElements().getTypeElement(fqcn);
                    if (te != null) {
                        List<ExecutableElement> list = ElementFilter.methodsIn(te.getEnclosedElements());
                        for (Iterator<ExecutableElement> it = list.iterator(); it.hasNext();) {
                            ExecutableElement executableElement = it.next();
                            String methodName = executableElement.getSimpleName().toString();
                            if (executableElement.getModifiers().contains(Modifier.PUBLIC) && match(methodName, prefix)) {
                                String propName = extractPropName(methodName, prefix);
                                if (!propName.equals("")) {
                                    result.add(propName);
                                }
                            }
                        }
                    }
                }

                private String extractPropName(String methodName, String[] prefix) {
                    String res = "";
                    for (int i = 0; i < prefix.length; i++) {
                        String string = prefix[i];
                        if (methodName.startsWith(string)) {
                            res = methodName.substring(string.length()).toLowerCase();
                            break;
                        }
                    }
                    return res;
                }

                private boolean match(String methodName, String[] prefix) {
                    boolean res = false;
                    for (int i = 0; i < prefix.length; i++) {
                        String string = prefix[i];
                        if (methodName.startsWith(string)) {
                            res = true;
                            break;
                        }
                    }
                    return res;
                }
            });
        }
        return result;
    }
}