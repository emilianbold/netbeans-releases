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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.css.model.CssModel;
import org.netbeans.modules.css.model.CssRule;
import org.netbeans.modules.css.visual.api.CssRuleContext;
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
    
    public static CharSequence getPreviewCode(CssRuleContext content) {
        StringBuilder preview = new StringBuilder();
        preview.append(HTML_PREFIX);
        
        //generate the <script> ... </script> tag content
        CssModel model = content.model();
        StringBuilder sb = new StringBuilder();
        for(CssRule rule : model.rules()) {
            //pseudo classes hack ( A:link { color: red; }
            //we need to generate an artificial element so we can spot various states of the element (a:visited, active etc.)
            //which depends on the state of the browser.
            sb.append(rule.name().replace(':', 'X'));
            
            sb.append(" {\n");
            sb.append(rule.ruleContent().getFormattedString().replace('"', '\''));
            sb.append("\n }\n");
        }
        preview.append((CharSequence)sb);
        preview.append(HTML_MIDDLE);
        CssRule selected = content.selectedRule();
        String ruleName = selected.name();
        
        //We do not support preview of multiple elements in selector e.g.
        //h1 h2 h3 { color: red; }
        //
        //so for now just cut whatever is after space or comma in selector - use the first one for preview :-(
        int commaIndex = ruleName.indexOf(',');
        if(commaIndex  > 0) {
            ruleName = ruleName.substring(0, commaIndex);
        }
        
        //pseudo classes ( A:link { color: red; }
        int colonIndex = ruleName.indexOf(':');
        if(colonIndex  > 0) {
            //the colon in the styles has been replaced by 'X' character
            //so to properly render the style we need to inherit the style form the selector
            //to do this - put the element generated from the pseudo class element by
            //the element without the pseudo class.
            //for example   a:visited { color: gray; }
            //will generate
            // <a>
            //    <aXvisited>SampleText</aXvisited>
            // </a>
            //which will show the same result as if the link is visited in the browser
            
            String selector = ruleName.substring(0, colonIndex);
            String pseudoclass = ruleName.substring(colonIndex + 1, ruleName.length());
            
            preview.append("<");
            preview.append(selector);
            preview.append(">");
            
            preview.append("<");
            preview.append(selector);
            preview.append('X');
            preview.append(pseudoclass);
            preview.append(">");
            
            preview.append(SAMPLE_TEXT);
            
            preview.append("</");
            preview.append(selector);
            preview.append('X');
            preview.append(pseudoclass);
            preview.append(">");
            
            preview.append("</");
            preview.append(selector);
            preview.append(">");
            
        } else {
            
            //check if the ruleName contains space delimited selectors
            StringTokenizer st = new StringTokenizer(ruleName, " "); //NOI18N
            List<String> selectors = new ArrayList<String>();
            if(st.countTokens() > 1) {
                while(st.hasMoreTokens()) {
                    //check if the selector contains just characters or numbers (what else is allowed in selector name?)
                    String selector = st.nextToken();
                    if(!isPureSelector(selector)) {
                        selectors = null;
                        break;
                    } else {
                        selectors.add(selector);
                    }
                }
            }
            
            if(selectors != null && selectors.size() > 0) {
                /**
                 * found space delimited selectors e.g.:
                 * P EM { color: red; }
                 *
                 * in this case generate following html code:
                 * <P>
                 *    <EM>Sample Text</EM>
                 * </P>
                 */
                for(int i = 0; i < selectors.size() - 1; i++) {
                    String selector = selectors.get(i);
                    preview.append("<");
                    preview.append(selector);
                    preview.append(">");
                }
                
                //add the deepest element
                {
                    preview.append("<");
                    String selector = selectors.get(selectors.size() - 1);
                    preview.append(selector);
                    preview.append(">");
                    preview.append(SAMPLE_TEXT);
                    preview.append("</");
                    preview.append(selector);
                    preview.append(">");
                }
                
                for(int i = selectors.size() - 2; i >= 0;  i--) {
                    String selector = selectors.get(i);
                    preview.append("</");
                    preview.append(selector);
                    preview.append(">");
                }
                
            } else if(ruleName.contains(".")) {
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
        }
        
        preview.append(HTML_POSTFIX);
        
        return preview;
    }
    
    private static boolean isPureSelector(String selectorName) {
        for(int i = 0; i < selectorName.length(); i++) {
            char ch = selectorName.charAt(i);
            if(!(Character.isLetter(ch) || Character.isDigit(ch))) {
                return false;
            }
        }
        return true;
    }
    
}
