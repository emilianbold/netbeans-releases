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
package org.netbeans.modules.bpel.mapper.model;

import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;

/**
 *
 * @author nk160297
 */
public class LocationPathBuilder {

        private BpelEntity mContextEntity;
        private SchemaComponent mSchemaContextComp;
        private String mLocationPathText;
        
        public LocationPathBuilder(BpelEntity contextEntity, Part part, Query query) {
            mContextEntity = contextEntity;
            mSchemaContextComp = EditorUtil.getPartType(part);
            mLocationPathText = query.getContent();
        }

        public LocationPathBuilder(BpelEntity contextEntity, 
                AbstractVariableDeclaration varDecl, Query query) {
            mContextEntity = contextEntity;
            mSchemaContextComp = EditorUtil.getVariableSchemaType(varDecl);
            mLocationPathText = query.getContent();
        }

        public XPathLocationPath build() {
            try {
                XPathModel model = BpelXPathModelFactory.create(mContextEntity);
                //
                XPathSchemaContext sContext = 
                        new SimpleSchemaContext(mSchemaContextComp);
                model.setSchemaContext(sContext);
                //
                XPathExpression xpath = model.parseExpression(mLocationPathText);
                if (xpath instanceof XPathLocationPath) {
                    return (XPathLocationPath)xpath;
                }
            } catch (XPathException ex) {
                // Do nothing here
            }
            //
            return null;
        }
}
