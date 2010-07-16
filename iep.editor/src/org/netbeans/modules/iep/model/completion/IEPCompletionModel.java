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
package org.netbeans.modules.iep.model.completion;

import javax.xml.XMLConstants;

import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;


public class IEPCompletionModel extends CompletionModelProvider.CompletionModel {

    private final CompletionContext context;
    private final SchemaModel schemaModel;
    private String suggestedPrefix;

    public IEPCompletionModel(CompletionContext context, SchemaModel schemaModel, String suggestedPrefix) {
        this.context = context;
        this.schemaModel = schemaModel;
        this.suggestedPrefix = suggestedPrefix;
        
    }
    
    @Override
    public String getSuggestedPrefix() {
        if(suggestedPrefix == null) {
            //Generate a new prefix by looking at the document.
            this.suggestedPrefix = getSuggestedPrefix("ns", getTargetNamespace()); //NOI18N
        }
        return suggestedPrefix;
    }
    
    @Override
    public SchemaModel getSchemaModel() {
        return schemaModel;
    }
    
    @Override
    public String getTargetNamespace() {
        return schemaModel.getSchema().getTargetNamespace();
    }
    
    private String getSuggestedPrefix(String prefix, String tns) {
        String newPrefix = prefix;
        String nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":"+newPrefix;
        int i = 0;
        while(context.getDeclaredNamespaces().get(nsDecl) != null) {
            String ns = context.getDeclaredNamespaces().get(nsDecl);
            if(ns.equals(tns))
                return null;
            newPrefix = newPrefix + i;  //NOI18N
            nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":"+newPrefix;
            i++;
        }        
        return newPrefix;
    }

}
