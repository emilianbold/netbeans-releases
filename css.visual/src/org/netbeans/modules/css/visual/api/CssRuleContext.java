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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.api;

import javax.swing.text.Document;
import org.netbeans.modules.css.model.CssModel;
import org.netbeans.modules.css.model.CssRule;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marek
 */
public class CssRuleContext {

    private FileObject source;
        private Document doc;
        private CssRule selectedRule;
        private CssModel model;
        
        public CssRuleContext(CssRule selectedRule, CssModel model, Document doc, FileObject source) {
            this.selectedRule = selectedRule;
            this.model = model;
            this.source = source;
            this.doc = doc;
        }
        
        public CssRule selectedRule() {
            return selectedRule;
        }
        
        public CssModel model() {
            return model;
        }
        
        public FileObject fileObject() {
            return source;
        }
        
        public Document document() {
            return doc;
        }
        
        public boolean equals(Object o) {
            if(!(o instanceof CssRuleContext)) {
                return false;
            } else {
                CssRuleContext c = (CssRuleContext)o;
                return c.document() == document() 
                        && c.fileObject() == fileObject()
                        && c.selectedRule() == selectedRule();
            }
        }
    }

