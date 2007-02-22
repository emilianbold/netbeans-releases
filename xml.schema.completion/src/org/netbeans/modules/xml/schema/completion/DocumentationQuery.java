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

/*
 * DocumentationQuery.java
 *
 * Created on June 8, 2006, 9:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion;

import java.net.URL;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.axi.Element;

/**
 *
 * @author Samaresh
 */
public class DocumentationQuery extends AsyncCompletionQuery {
    
    private CompletionResultItem completionItem;
    
    DocumentationQuery(CompletionResultItem item) {
        this.completionItem = item;
    }
    
    protected void query(CompletionResultSet resultSet,
            Document doc, int caretOffset) {
        resultSet.setDocumentation(DocumentationItem.
                createDocumentationItem(completionItem));
        resultSet.finish();
    }
        
}
