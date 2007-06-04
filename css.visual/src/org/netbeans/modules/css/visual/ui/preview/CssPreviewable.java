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

import javax.swing.text.Document;
import org.netbeans.modules.css.model.CssRule;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * Allows to listen on underlaying CSS preview source changes.
 * New property value is the HTML code to be used in the preview.
 *
 * @author Marek Fukala
 */
public interface CssPreviewable extends Node.Cookie {

    public void addListener(Listener l);
    
    public void removeListener(Listener l);
    
    public Content content();
    
    public interface Listener {
        
        /** called when the css model or selected rule has changed. */
        public void activate(Content content);
        
        /** called when the css model is broken or no rule selected. */
        public void deactivate();
    }
    
    public final class Content {
        
        private FileObject source;
        private Document doc;
        private CssRule selectedRule;
        
        public Content(CssRule selectedRule, Document doc, FileObject source) {
            this.selectedRule = selectedRule;
            this.source = source;
            this.doc = doc;
        }
        
        public CssRule selectedRule() {
            return selectedRule;
        }
        
        public FileObject fileObject() {
            return source;
        }
        
        public Document document() {
            return doc;
        }
        
        public boolean equals(Object o) {
            if(!(o instanceof Content)) {
                return false;
            } else {
                Content c = (Content)o;
                return c.document() == document() 
                        && c.fileObject() == fileObject()
                        && c.selectedRule() == selectedRule();
            }
        }
    }
}
