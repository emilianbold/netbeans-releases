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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.css.visual.ui.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import org.netbeans.modules.css.editor.model.CssModel;
import org.netbeans.modules.css.editor.model.CssRule;
import org.netbeans.modules.css.editor.model.CssRuleContent;
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
        StringBuilder sb = new StringBuilder();

        //extract all rules from all included models
        Collection<CssModel> models = new ArrayList<CssModel>();
        models.add(content.model());
        models.addAll(content.model().getImportedFileModels());

        for (CssModel model : models) {
            for (CssRule rule : model.rules()) {
                //pseudo classes hack ( A:link { color: red; }
                //we need to generate an artificial element so we can spot various states of the element (a:visited, active etc.)
                //which depends on the state of the browser.
                String ruleName = rule.name();
                while (ruleName.indexOf('[') > 0) {// remove [ ] blocks !!!FIX ME in future
                    int startBracket = ruleName.indexOf('[');
                    int endBracket = ruleName.indexOf(']', startBracket);
                    ruleName = ruleName.substring(0, startBracket) + ruleName.substring(endBracket + 1);
                }

                sb.append(ruleName.replaceAll(":", "X"));//#118277 a:visited:hover
                sb.append(" {\n");
                sb.append(CssRuleContent.create(rule).getFormattedString().replace('"', '\''));
                sb.append("\n }\n");
            }
        }
        preview.append((CharSequence)sb);
        preview.append(HTML_MIDDLE);
        CssRule selected = content.selectedRuleContent().rule();
        String ruleName = selected.name();
        ruleName = ruleName.replace('\n', ' '); //hotfix #117690 (selectors on multiple lines break the css previrew)
        
        //We do not support preview of multiple elements in selector e.g.
        //h1 h2 h3 { color: red; }
        //
        //so for now just cut whatever is after space or comma in selector - use the first one for preview :-(
        int commaIndex = ruleName.indexOf(',');
        if(commaIndex  > 0) {
            ruleName = ruleName.substring(0, commaIndex);
        }
        while (ruleName.indexOf('[') > 0){// remove [ ] blocks
            int startBracket = ruleName.indexOf('[');
            int endBracket = ruleName.indexOf(']', startBracket);
            ruleName = ruleName.substring(0, startBracket) + ruleName.substring(endBracket+1);
        }
        //parse selectors by  space and ">" char
        StringTokenizer st = new StringTokenizer(ruleName, " >"); //NOI18N
        List<String> selectors = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            //check if the selector contains just characters or numbers (what else is allowed in selector name?)
            String selector = st.nextToken();
            selectors.add("*".equals(selector.trim()) ? "div" : selector); //NOI18N
        }
        if (selectors.size() == 0){
            selectors.add("div"); //NOI18N
        } else if(selectors.size() == 1) {
            //#135823 fix
            String single = selectors.get(0);
            if("table".equalsIgnoreCase(single)) {
                selectors.add("tr"); //NOI18N
                selectors.add("td"); //NOI18N
                selectors.add("div"); //NOI18N
            } else if("tr".equalsIgnoreCase(single)) {
                selectors.add(0, "table");
                selectors.add("td"); //NOI18N
                selectors.add("div"); //NOI18N
            } else if("td".equalsIgnoreCase(single)) {
                selectors.add(0, "table");
                selectors.add(1, "tr"); //NOI18N
                selectors.add("div"); //NOI18N
            }
        }

        int previewFocus = preview.length();
        for (String selectorItem : selectors) {
            StringBuilder opening = new StringBuilder();
            StringBuilder closing = new StringBuilder();

            //pseudo classes ( A:link { color: red; }
            int firstColonIndex = selectorItem.indexOf(':');
            if (firstColonIndex > 0) {
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

                String selector = selectorItem.substring(0, firstColonIndex);
                List<String> pseudoclass = new ArrayList<String>(2);
                int colonIndex = selectorItem.indexOf(':', firstColonIndex + 1);
                String realName;
                while (colonIndex > 0) {
                    realName = selectorItem.substring(firstColonIndex + 1, colonIndex);
                    pseudoclass.add(realName.replaceAll(":", "X"));
                    colonIndex = selectorItem.indexOf(':', colonIndex + 1);
                }
                realName = selectorItem.substring(firstColonIndex + 1, selectorItem.length());
                pseudoclass.add(realName.replaceAll(":", "X"));

                opening.append("<");
                opening.append(selector);
                opening.append(">");
                for (String pseudoclassName : pseudoclass) {
                    opening.append("<");
                    opening.append(selector);
                    opening.append('X');
                    opening.append(pseudoclassName);
                    opening.append(">");
                }
                ListIterator<String> iter = pseudoclass.listIterator(pseudoclass.size());
                while (iter.hasPrevious()) {// reverse order
                    String pseudoclassName = iter.previous();
                    closing.append("</");
                    closing.append(selector);
                    closing.append('X');
                    closing.append(pseudoclassName);
                    closing.append(">");
                }
                closing.append("</");
                closing.append(selector);
                closing.append(">");
            } else if (isPureSelector(selectorItem)) {
                opening.append("<");
                opening.append(selectorItem);
                opening.append(">");
                closing.append("</");
                closing.append(selectorItem);
                closing.append(">");
            } else{

                SelectorInfo info = parseSelector(selectorItem);

                opening.append("<");
                opening.append(info.elementName);
                if (info.className != null) {
                    opening.append(" class=\"");
                    opening.append(info.className);
                    opening.append("\"");
                }
                if (info.id != null) {
                    opening.append(" id=\"");
                    opening.append(info.id);
                    opening.append("\"");
                }
                opening.append(">");
                closing.append("</");
                closing.append(info.elementName);
                closing.append(">\n");
            }
            preview.insert(previewFocus, opening);
            previewFocus = previewFocus + opening.length();
            preview.insert(previewFocus, closing);
        }
        
        preview.insert(previewFocus, SAMPLE_TEXT);
        preview.append(HTML_POSTFIX);
        
        return preview;
    }
    
    private static boolean isPureSelector(String selectorName) {
        for(int i = 0; i < selectorName.length(); i++) {
            char ch = selectorName.charAt(i);
            if(!(Character.isLetter(ch) || Character.isDigit(ch) || ch == '*')||(ch == '.')) {
                return false;
            }
        }
        return true;
    }
    
    private static class SelectorInfo{
        String elementName, className, id;
    }
    
    private static SelectorInfo parseSelector(String selector){
        SelectorInfo result = new SelectorInfo();
        int dotIndex = selector.indexOf('.');
        int hashIndex = selector.indexOf('#', dotIndex);
        
        if (dotIndex > 0){//text.class
            result.elementName = selector.substring(0, dotIndex);
            if (hashIndex > 0){
                result.className = selector.substring(dotIndex + 1, hashIndex);
                result.id = selector.substring(hashIndex + 1);
            }else{
                result.className =  selector.substring(dotIndex + 1);
            }
        }else if(dotIndex == 0){//.class
            result.elementName = "div";
            if (hashIndex > 0){
                result.className = selector.substring(1, hashIndex);
                result.id = selector.substring(hashIndex + 1);
            }else{
                result.className = selector.substring(1);
            }
        }else if (hashIndex > 0){// dotIndex < 0
            result.elementName = selector.substring(0, hashIndex);
            result.id = selector.substring(hashIndex + 1);
        }else if (hashIndex == 0){
            result.elementName = "div";
            result.id = selector.substring(1);
        }else if ("*".equals(selector.trim())){ // no class and no id
            result.elementName = "div";
        }else{
            result.elementName = selector;
        }
        return result;
    }
    
    
}
