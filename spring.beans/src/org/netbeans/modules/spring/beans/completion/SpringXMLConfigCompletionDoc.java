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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.completion;

import java.net.URL;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.Action;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class SpringXMLConfigCompletionDoc implements CompletionDocumentation {

    public static SpringXMLConfigCompletionDoc getAttribValueDoc(String text) {
        return new AttribValueDoc(text);
    }
    
    public static SpringXMLConfigCompletionDoc createJavaDoc(CompilationController cc, Element element) {
        return new JavaElementDoc(ElementJavadoc.create(cc, element));
    }
    
    public static SpringXMLConfigCompletionDoc getBeanRefDoc(String beanId, List<String> beanNames, 
            String beanClassName, String filePath) {
        return new BeanRefDoc(beanId, beanNames, beanClassName, filePath);
    }

    public URL getURL() {
        return null;
    }

    public CompletionDocumentation resolveLink(String link) {
        return null;
    }

    public Action getGotoSourceAction() {
        return null;
    }
    
    private static class JavaElementDoc extends SpringXMLConfigCompletionDoc {

        private ElementJavadoc elementJavadoc;

        public JavaElementDoc(ElementJavadoc elementJavadoc) {
            this.elementJavadoc = elementJavadoc;
        }

        @Override
        public JavaElementDoc resolveLink(String link) {
            ElementJavadoc doc = elementJavadoc.resolveLink(link);
            return doc != null ? new JavaElementDoc(doc) : null;
        }

        @Override
        public URL getURL() {
            return elementJavadoc.getURL();
        }

        public String getText() {
            return elementJavadoc.getText();
        }

        @Override
        public Action getGotoSourceAction() {
            return elementJavadoc.getGotoSourceAction();
        }
    }

    private static class BeanRefDoc extends SpringXMLConfigCompletionDoc {
        private String filePath;
        private String beanClassName;
        private String displayText;

        private static final String BOLD_START = "<b>"; // NOI18N
        private static final String BOLD_END = "</b>"; // NOI18N
        private static final String BR = "<br>"; // NOI18N
        private static final String COMMA = ", "; // NOI18N
        private List<String> beanNames;
        private String beanId;

        public BeanRefDoc(String beanId, List<String> beanNames, String beanClassName, String filePath) {
            this.beanClassName = beanClassName;
            this.filePath = filePath;
            this.beanId = beanId;
            this.beanNames = beanNames;
        }
        
        public String getText() {
            if(displayText == null) {
                StringBuilder sb = new StringBuilder();
                addLabel(sb, NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_Bean_Id")); // NOI18N
                sb.append(beanId);
                sb.append(BR); 
                if(beanNames.size() > 0) {
                    addLabel(sb, NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_Bean_Names")); // NOI18N
                    addBeanNames(sb);
                    sb.append(BR); 
                }
                addLabel(sb, NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_Bean_Implementation_Class")); // NOI18N
                sb.append(beanClassName);
                sb.append(BR); 
                addLabel(sb, NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_Bean_File_Path")); // NOI18N
                sb.append(filePath);
                displayText = sb.toString();
            }
            
            return displayText;
        }
        
        private void addBeanNames(StringBuilder sb) {
            for(int i = 0; i < beanNames.size(); i++) {
                sb.append(beanNames.get(i));
                if(i != beanNames.size() - 1) {
                    sb.append(COMMA);
                }
            }
        }
        
        private void addLabel(StringBuilder sb, String lbl) {
            sb.append(BOLD_START);
            sb.append(lbl);
            sb.append(": "); // NOI18N
            sb.append(BOLD_END);
        }
    }
    
    private static class AttribValueDoc extends SpringXMLConfigCompletionDoc {

        private String text;

        public AttribValueDoc(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
