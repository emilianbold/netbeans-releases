/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * SchemaDocumentationFinderVisitor.java
 *
 * Created on April 17, 2006, 8:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;

/**
 *
 * @author radval
 */
public class SchemaDocumentationFinderVisitor extends AbstractXSDVisitor {
    
    private StringBuffer mDocumentationBuf = new StringBuffer(40);
    
    private static StringAttribute xmlLangAttribute =  new StringAttribute("xml:lang");
    
    private String xmlLangValue = "";
    
    /** Creates a new instance of SchemaDocumentationFinderVisitor */
    public SchemaDocumentationFinderVisitor() {
        xmlLangValue = convertLocaleToXmlLangValue();
    }
    
    public String getDocumentation() {
        return this.mDocumentationBuf.toString();
    }
    
    public void visit(LocalAttribute la) {
        Annotation ann = la.getAnnotation();
        if(ann != null) {
            visit(ann);
        } else {
            if (la.getType() != null ) {
                visit(la.getType().get());
            } else if (la.getInlineType() != null) {
                visit(la.getInlineType());
            }
        }
        
    }
    
    public void visit(GlobalAttribute ga) {
        Annotation ann = ga.getAnnotation();
        if(ann != null) {
            visit(ann);
        } else {
            if (ga.getType() != null ) {
                visit(ga.getType().get());
            } else if (ga.getInlineType() != null) {
                visit(ga.getInlineType());
            }
        }
    }
    
    public void visit(GlobalElement ge) {
        Annotation ann = ge.getAnnotation();
        if(ann != null) {
            visit(ann);
        } else {
            if (ge.getType() != null ) {
                visit(ge.getType().get());
            } else if (ge.getInlineType() != null) {
                visit(ge.getInlineType());
            }
        }
    }
    
    public void visit(LocalElement le) {
        Annotation ann = le.getAnnotation();
        if(ann != null) {
            visit(ann);
        } else {
            if (le.getType() != null ) {
                visit(le.getType().get());
            } else if (le.getInlineType() != null) {
                visit(le.getInlineType());
            }
        }
    }
    
    
    
    
    @Override
    public void visit(GlobalComplexType gct) {
        Annotation ann = gct.getAnnotation();
        if(ann != null) {
            visit(ann);
        }
    }

    @Override
    public void visit(GlobalSimpleType gst) {
        if(gst != null) {
        Annotation ann = gst.getAnnotation();
        if(ann != null) {
            visit(ann);
            }
        }
    }

    @Override
    public void visit(LocalComplexType type) {
        Annotation ann = type.getAnnotation();
        if(ann != null) {
            visit(ann);
        }
    }

    @Override
    public void visit(LocalSimpleType type) {
        Annotation ann = type.getAnnotation();
        if(ann != null) {
            visit(ann);
        }
    }

    public void visit(Annotation ann) {
    	visitLocalizedDocumentation(ann);
    }
    
    public void visit(Documentation doc) {
        String content = doc.getContent();
        if(content != null) {
            content = content.trim();
            mDocumentationBuf.append(content);
        }
    }
    
    private void visitLocalizedDocumentation(Annotation ann) {
        boolean foundLocalizedDocumentation = false;
        
        Collection<Documentation> docs = ann.getDocumentationElements();
        if (docs != null) {
            Iterator<Documentation> iter = docs.iterator();
            while(iter.hasNext()) {
                Documentation doc = iter.next();
                String xmlLang = doc.getAttribute(xmlLangAttribute);
                //find the first documentation matching current locale
                if(xmlLang != null && xmlLang.equals(xmlLangValue)) {
                    visit(doc);
                    foundLocalizedDocumentation = true;
                    break;
                }
            }
            
            //no matching documentation found, so looks like xml:lang
            //is not specified
            if(!foundLocalizedDocumentation) {
                //get the first documentation and use is as default
                if(docs.size() > 0) {
                    visitDefaultDocumentation(docs);
                }
            }
        }
    }
    
    private String convertLocaleToXmlLangValue() {
         StringBuffer xmlLangValue = new StringBuffer(15);
        
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        String country = l.getCountry();
        String variant = l.getVariant();
        
        if(language != null && !language.trim().equals("")) {
            xmlLangValue.append(language);
        }
        
        if(country != null && !country.trim().equals("")) {
            xmlLangValue.append("-");
            xmlLangValue.append(country);
        }
        
        if(variant != null && !variant.trim().equals("")) {
            xmlLangValue.append("-");
            xmlLangValue.append(variant);
        }
        
        return xmlLangValue.toString();
    }
    
    private void visitDefaultDocumentation( Collection<Documentation> docs) {
        //find documentation which does not have xml:lang
        if (docs != null) {
            Iterator<Documentation> iter = docs.iterator();
            while(iter.hasNext()) {
                Documentation doc = iter.next();
                String xmlLang = doc.getAttribute(xmlLangAttribute);
                if(xmlLang == null || xmlLang.trim().equals("")) {
                    visit(doc);
                    break;
                }
            }
        }
    }
}

