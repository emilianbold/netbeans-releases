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

package org.netbeans.modules.bpel.mapper.tree.models;

import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;

/**
 * The special artificial object which is intended to represent the variable 
 * data object in case when a BPEL model doesn't provide such one. 
 * It is suitable when a BPEL entity implements the VariableDeclaration and the
 * VariableDeclarationScope both. An example is Cache or ForEach entities. 
 * 
 * @author nk160297
 */
public class VariableDeclarationWrapper implements AbstractVariableDeclaration {

    private VariableDeclaration mVarDeclDelegate;

    public VariableDeclarationWrapper(VariableDeclaration varDecl) {
        assert varDecl != null;
        mVarDeclDelegate = varDecl;
    }

    public VariableDeclaration getDelegate() {
        return mVarDeclDelegate;
    }

    public WSDLReference<Message> getMessageType() {
        return mVarDeclDelegate.getMessageType();
    }

    public SchemaReference<GlobalElement> getElement() {
        return mVarDeclDelegate.getElement();
    }

    public SchemaReference<GlobalType> getType() {
        return mVarDeclDelegate.getType();
    }

    public String getVariableName() {
        return mVarDeclDelegate.getVariableName();
    }
}
