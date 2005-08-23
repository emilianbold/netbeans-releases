/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;

/**
 * Parser of the parametrized text.
 *
 * @author Miloslav Metelka
 */
public final class ParametrizedTextParser {
    
    private final CodeTemplateInsertHandler handler;
    
    private final String parametrizedText;
    
    private List paramImpls; // filled only when (handler == null)
    
    /**
     * Fragments of the parametrized text between the parameters.
     */
    private List/*<String>*/ parametrizedTextFragments;

    public ParametrizedTextParser(CodeTemplateInsertHandler handler, String parametrizedText) {
        this.handler = handler; // may be null for parsing for completion doc item
        this.parametrizedText = parametrizedText;
        if (handler == null) { // will build doc for completion item
            paramImpls = new ArrayList();
        }
    }
    
    public void parse() {
        parametrizedTextFragments = new ArrayList();

        StringBuffer textFrag = new StringBuffer();
        int copyStartIndex = 0;
        int index = 0; // actual index in parametrizedText
        boolean atEOT = false;
        while (!atEOT) {
            // Search for '${...}'
            // '$$' interpreted as '$'
            int dollarIndex = parametrizedText.indexOf('$', index);
            if (dollarIndex != -1) { // found
                switch (parametrizedText.charAt(dollarIndex + 1)) { // test char after '$'
                    case '{': // parameter parsing
                        // Store preceding part into fragments
                        textFrag.append(parametrizedText.substring(copyStartIndex, dollarIndex));
                        copyStartIndex = dollarIndex;
                        parametrizedTextFragments.add(textFrag.toString());
                        textFrag.setLength(0);

                        // Create parameter found at the dollarIndex
                        CodeTemplateParameterImpl paramImpl = new CodeTemplateParameterImpl(
                                handler, parametrizedText, dollarIndex);

                        int afterClosingBraceIndex = paramImpl.getParametrizedTextEndOffset();
                        if (afterClosingBraceIndex <= parametrizedText.length()) { // successfully recognized
                            if (handler != null) {
                                handler.notifyParameterParsed(paramImpl);
                            } else { // store params locally
                                paramImpls.add(paramImpl);
                            }
                            index = afterClosingBraceIndex;
                            copyStartIndex = index;

                        } else { // parameter's parsing hit EOT
                            atEOT = true;
                            break;
                        }
                        break;
                        
                    case '$': // shrink to single '$'
                        textFrag.append(parametrizedText.substring(copyStartIndex, dollarIndex + 1));
                        index = dollarIndex + 2;
                        copyStartIndex = index;
                        break;
                        
                    default: // something else => '$'
                        index = dollarIndex + 1;
                        break;
                }

            } else { // '$' not found till the end of parametrizedText
                textFrag.append(parametrizedText.substring(copyStartIndex));
                parametrizedTextFragments.add(textFrag.toString());
                atEOT = true;
            }
        }
    }
    
    public String buildInsertText(List/*<CodeTemplateParameter>*/ allParameters) {
        StringBuffer insertTextBuffer = new StringBuffer(parametrizedText.length());
        insertTextBuffer.append(parametrizedTextFragments.get(0));
        int fragIndex = 1;
        for (Iterator it = allParameters.iterator(); it.hasNext();) {
            CodeTemplateParameter parameter = (CodeTemplateParameter)it.next();
            CodeTemplateParameterImpl.get(parameter).setInsertTextOffset(insertTextBuffer.length());
            insertTextBuffer.append(parameter.getValue());
            insertTextBuffer.append(parametrizedTextFragments.get(fragIndex));
            fragIndex++;
        }
        return insertTextBuffer.toString();
    }
    
    private static String toHtmlText(String text) {
        return CodeTemplateCompletionItem.toHtmlText(text);
    }
    
    public void appendHtmlText(StringBuffer htmlTextBuffer) {
        htmlTextBuffer.append(toHtmlText((String)parametrizedTextFragments.get(0)));
        
        int fragIndex = 1;
        for (Iterator it = paramImpls.iterator(); it.hasNext();) {
            CodeTemplateParameterImpl paramImpl = (CodeTemplateParameterImpl)it.next();
            htmlTextBuffer.append("<b>");
            if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(paramImpl.getName())) {
                htmlTextBuffer.append("|");
            } else {
                htmlTextBuffer.append(toHtmlText(paramImpl.getValue()));
            }
            htmlTextBuffer.append("</b>");
            htmlTextBuffer.append(toHtmlText((String)parametrizedTextFragments.get(fragIndex)));
            fragIndex++;
        }
    }

}
