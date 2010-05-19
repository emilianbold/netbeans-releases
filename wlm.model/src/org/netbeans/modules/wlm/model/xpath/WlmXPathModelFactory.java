/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.xpath;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelConstructor;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;

/**
 * @author nk160297
 */
public final class WlmXPathModelFactory implements XPathModelConstructor {

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
     * The WLMComponent is used as the context.
     * 
     * ATTENTION! If you use this method, be sure you specify a 
     * Type Cast Resolver. See the method setXPathCastResolver().
     * 
     * @param expression text to parse
     * @param contextEntity BPEL entity which is used as context
     * @return the new XPath model.
     */
    public static XPathModel create(final WLMComponent contextEntity) {
        return create(contextEntity, contextEntity, null, null);
    }
    
    /**
     * Create a model which knows about type casts and pseudo components.
     * @param contextEntity
     * @param castResolver
     * @return
     */
    public static XPathModel create(final WLMComponent contextEntity,
            XPathCastResolver castResolver) {
        //
        return create(contextEntity, contextEntity, castResolver, null);
    }

    public static XPathModel create(WLMComponent contextEntity,
            WLMComponent varContextEntity,
            XPathCastResolver castResolver) {
        return create(contextEntity, varContextEntity, castResolver, null);
    }

    /**
     * Create a model which knows about type casts and pseudo components.
     * @param contextEntity
     * @param varContextEntity points to a BPEL entity, which is used
     * to calculate a set of visible variables. 
     * @param castResolver
     * @return
     */
    public static XPathModel create(final WLMComponent contextEntity,
            final WLMComponent varContextEntity,
            XPathCastResolver castResolver,
            XPathValidationContext context) {
        //
        assert contextEntity != null : 
            "Trying to create a new BPEL XPath model without a context entity"; // NOI18N
        XPathModelHelper helper= XPathModelHelper.getInstance();
        XPathModel model = helper.newXPathModel();
        //
        if (context != null) {
            context.setXPathModel(model);
            model.setValidationContext(context);
        }
        //
        model.setNamespaceContext(new WlmXPathNamespaceContext(contextEntity));
        //
        model.setVariableResolver(new WlmVariableResolver(context, varContextEntity));
        model.setExtensionFunctionResolver(new WlmXpathExtFunctionResolver());
        //
        model.setExternalModelResolver(
                new WlmExternalModelResolver(contextEntity.getModel()));
        //
        if (castResolver != null) {
            model.setXPathCastResolver(castResolver);
        }
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

    //------------------------------------------------------------------
    
    private WLMComponent mContextEntity;
    private XPathCastResolver mCastResolver;
    private XPathModel mXPathModel;
    
    public WlmXPathModelFactory(WLMComponent contextEntity,
            XPathCastResolver castResolver) {
        mContextEntity = contextEntity;
        mCastResolver = castResolver;
    }

    public XPathModel constructNewModel() {
        return create(mContextEntity, mCastResolver);
    }
    
    public XPathModel getModel() {
        if (mXPathModel == null) {
            mXPathModel = constructNewModel();
        }
        return mXPathModel;
    }
    
}
