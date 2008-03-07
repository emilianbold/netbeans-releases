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

package org.netbeans.modules.bpel.model.api.support;

import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.impl.references.SchemaReferenceBuilder;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author nk160297
 */
public final class XPathModelFactory {

    /**
     * This delimiter is used to separate multiple XPath expressions. 
     * The delimiter is used when BPEL mapper graph has a few roots. 
     * The expression with such delimiter will not be processed correctly 
     * by BPEL engine. 
     * It is intended to be used temporary to save unlinked content of a graph. 
     */
    public static final String XPATH_EXPR_DELIMITER = ";";
    public static final char XPATH_EXPR_DELIMITER_CHAR = ';';
    
    public static final String DEFAULT_EXPR_LANGUAGE =
            "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"; // NOI18N
    
    /**
     * Try to create a new XPath model for BPEL. 
     * The BpelEntity is used as the context.
     * 
     * @param expression text to parse
     * @param contextEntity BPEL entity which is used as context
     * @return the new XPath model.
     */
    public static XPathModel create(final BpelEntity contextEntity) {
        assert contextEntity != null : 
            "Trying to create a new BPEL XPath model without a context entity"; // NOI18N
        XPathModelHelper helper= XPathModelHelper.getInstance();
        XPathModel model = helper.newXPathModel();
        //
        ExNamespaceContext nsContext = contextEntity.getNamespaceContext();
        model.setNamespaceContext(new BpelXPathNamespaceContext(nsContext));
        //
        model.setVariableResolver(new BpelVariableResolver(null, contextEntity));
        model.setExtensionFunctionResolver(new BpelXpathExtFunctionResolver());
        //
        model.setExternalModelResolver(new ExternalModelResolver() {
            public Collection<SchemaModel> getModels(String modelNsUri) {
                BpelModel bpelModel = contextEntity.getBpelModel();
                return SchemaReferenceBuilder.getSchemaModels(bpelModel, modelNsUri);
            }

            public Collection<SchemaModel> getVisibleModels() {
                return null;
            }

            public boolean isSchemaVisible(String schemaNamespaceUri) {
                return false;
            }
        });
        //
        return model;
    }
    
    public static String[] split(String str) {
        if (str == null || str.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        
        boolean quotOpen = false;
        boolean aposOpen = false;
        
        int beginIndex = 0;
        
        List<String> result = new ArrayList<String>();
        
        int stringLength = str.length();
        
        for (int i = 0; i < stringLength; i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                if (aposOpen) {
                    aposOpen = false;
                } else if (!quotOpen) {
                    aposOpen = true;
                }
            } else if (c == '\"') {
                if (quotOpen) {
                    quotOpen = false;
                } else if (!aposOpen) {
                    quotOpen = true;
                }
            } else if (c == XPATH_EXPR_DELIMITER_CHAR) {
                if (!quotOpen && !aposOpen) {
                    // substring found
                    if (beginIndex < i) {
                        result.add(new String(str.substring(beginIndex, i)));
                    }
                    beginIndex = i + 1;
                }
            }
        }
        
        if (beginIndex < stringLength) {
            result.add(new String(str.substring(beginIndex)));
        }
        
        return (result.isEmpty()) ? EMPTY_STRING_ARRAY
                : result.toArray(new String[result.size()]);
    }
    
    public static boolean isSplitable(String str) {
        boolean quotOpen = false;
        boolean aposOpen = false;

        int stringLength = str.length();
        
        for (int i = 0; i < stringLength; i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                if (aposOpen) {
                    aposOpen = false;
                } else if (!quotOpen) {
                    aposOpen = true;
                }
            } else if (c == '\"') {
                if (quotOpen) {
                    quotOpen = false;
                } else if (!aposOpen) {
                    quotOpen = true;
                }
            } else if (c == XPATH_EXPR_DELIMITER_CHAR) {
                if (!quotOpen && !aposOpen) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
}
