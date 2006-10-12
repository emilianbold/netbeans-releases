/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CompletionQuery.java
 *
 * Created on June 8, 2006, 9:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.xml.schema.completion.util.CatalogModelHelper;
import org.netbeans.modules.xml.schema.completion.util.CompletionQueryHelper;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionQuery extends AsyncCompletionQuery {
    
    protected JTextComponent component;
    protected CatalogModelHelper helper;
    
    /**
     * Creates a new instance of CompletionQuery
     */
    CompletionQuery(CatalogModelHelper helper) {
        this.helper = helper;
    }    
    
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }
    
    protected void query(CompletionResultSet resultSet,
            Document doc, int caretOffset) {
        
        XMLSyntaxSupport support = (XMLSyntaxSupport)Utilities.
                getDocument(component).getSyntaxSupport();
        CompletionQueryHelper cqh = new CompletionQueryHelper(helper, support, caretOffset);
        if(cqh.isSchemaBasedCompletion()) {
            List<CompletionResultItem> items = cqh.getCompletionItems();
            if(items != null) resultSet.addAllItems(items);
        }
        resultSet.finish();
    }
    
}
