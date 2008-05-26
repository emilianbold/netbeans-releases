/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.parsing.impl;

import java.util.Collections;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TokenHierarchySchedulerEvent;


/**
 *
 * @author Jan Jancura
 */
public class CurrentDocumentTaskScheduller extends CurrentEditorTaskScheduller {
    
    private Document        currentDocument;
    private Source          source;
//    private DocumentListener 
//                            documentListener = new ADocumentListener ();
    private TokenHierarchyListener
                            tokenHierarchyListener = new ATokenHierarchyListener ();
    
    protected void setEditor (JTextComponent editor) {
        Document document = editor.getDocument ();
        if (currentDocument == document) return;
        if (currentDocument != null)
            TokenHierarchy.get (document).removeTokenHierarchyListener (tokenHierarchyListener);
//            currentDocument.removeDocumentListener (documentListener);
        currentDocument = document;
        source = Source.create (currentDocument);
        scheduleTasks (Collections.singleton (source), new SchedulerEvent (this) {});
        //document.addDocumentListener (documentListener);
        TokenHierarchy.get (document).addTokenHierarchyListener (tokenHierarchyListener);
    }
    
    private class ATokenHierarchyListener implements TokenHierarchyListener {

        public void tokenHierarchyChanged (TokenHierarchyEvent evt) {
            scheduleTasks (
                Collections.singleton (source), 
                new TokenHierarchySchedulerEvent (this, evt) {}
            );
        }
        
    }
//    private class ADocumentListener implements DocumentListener {
//
//        public void insertUpdate (DocumentEvent e) {
//            scheduleTasks (Collections.singleton (source), new SchedulerEvent (this) {});
//        }
//
//        public void removeUpdate (DocumentEvent e) {
//            scheduleTasks (Collections.singleton (source), new SchedulerEvent (this) {});
//        }
//
//        public void changedUpdate (DocumentEvent e) {
//            scheduleTasks (Collections.singleton (source), new SchedulerEvent (this) {});
//        }
//    }
}



