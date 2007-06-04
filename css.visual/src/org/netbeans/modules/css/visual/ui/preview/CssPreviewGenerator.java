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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.css.visual.ui.preview;

import org.netbeans.modules.css.model.CssModel;
import org.netbeans.modules.css.model.CssRule;
import org.openide.util.NbBundle;

/**
 * Css html preview code generator.
 *
 * @author Marek Fukala
 */
public class CssPreviewGenerator {
    
    private static final String HTML_PREFIX =
            "<html>\n"
            + "\t<head>\n"
            + "\t\t<style type=\"text/css\">\n"; //NOI18N
    
    private static final String HTML_MIDDLE =
            "\t\t</style>\n"
            +"\t</head>\n"
            +"\t<body>\n\t\t"; //NOI18N
    
    private static final String HTML_POSTFIX =
            "\t</body>\n"
            +"</html>\n"; //NOI18N
    
    private static final String SAMPLE_TEXT =
            NbBundle.getMessage(CssPreviewTopComponent.class, "Sample_Text"); //NOI18N
    
    public static CharSequence getPreviewCode(CssPreviewable.Content content) {
        StringBuilder preview = new StringBuilder();
        preview.append(HTML_PREFIX);
        
        //generate the <script> ... </script> tag content
        CssModel model = CssModel.get(content.document());
        StringBuilder sb = new StringBuilder();
        for(CssRule rule : model.rules()) {
            sb.append(rule.name());
            sb.append(" {\n");
            sb.append(rule.ruleContent().getFormattedString().replace('"', '\''));
            sb.append("\n }\n");
        }
        preview.append((CharSequence)sb);
        preview.append(HTML_MIDDLE);
        CssRule selected = content.selectedRule();
        String ruleName = selected.name();
        
        //Hack - we do not support preview of multiple elements in selector e.g.
        //h1 h2 h3 { color: red; }
        //
        //so for now just cut whatever is after space or comma in selector - use the first one for preview :-(
        int delimIndex = ruleName.indexOf(' ') > 0 ? ruleName.indexOf(' ') : ruleName.indexOf(',');
        if(delimIndex > 0) {
            ruleName = ruleName.substring(0, delimIndex);
        }
        
        if(ruleName.contains(".")) {
            //class selector
            String elementName, className;
            if(ruleName.charAt(0) == '.') {
                //anonymous class selector
                elementName = "div"; //NOI18N
                className = ruleName.substring(1);
            } else {
                //element class selector
                int index = ruleName.indexOf('.');
                elementName = ruleName.substring(0, index);
                className = ruleName.substring(index + 1);
            }
            preview.append("<");
            preview.append(elementName);
            preview.append(" class=\"");
            preview.append(className);
            preview.append("\">");
            preview.append(SAMPLE_TEXT);
            preview.append("</");
            preview.append(elementName);
            preview.append(">\n");
            
        } else if(ruleName.contains("#")){
            //id selector
            String elementName, id;
            if(ruleName.charAt(0) == '#') {
                //anonymous id selector
                elementName = "div"; //NOI18N
                id = ruleName.substring(1);
            } else {
                //element class selector
                int index = ruleName.indexOf('#');
                elementName = ruleName.substring(0, index);
                id = ruleName.substring(index + 1);
            }
            
            preview.append("\n<");
            preview.append(elementName);
            preview.append(" id=\"");
            preview.append(id);
            preview.append("\">");
            preview.append(SAMPLE_TEXT);
            preview.append("</");
            preview.append(elementName);
            preview.append(">\n");
            
        } else {
            //'normal' element selector
            preview.append("\n<");
            preview.append(ruleName);
            preview.append(">");
            preview.append(SAMPLE_TEXT);
            preview.append("</");
            preview.append(ruleName);
            preview.append(">\n");
        }
        
        preview.append(HTML_POSTFIX);
        
        return preview;
    }
    
}
