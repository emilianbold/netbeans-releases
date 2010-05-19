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

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.xpath.WlmXPathModelFactory;
import org.netbeans.modules.worklist.editor.mapper.lsm.MapperLsmProcessor.MapperLsmContainer;
import org.netbeans.modules.worklist.editor.mapper.tree.search.FinderListBuilder;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;

/**
 * Processes the Copy-->To object to find a node in the target tree to 
 * which a link has to be connected.
 * The To WLM entity can have different forms.
 * The specific processing is required for different forms.
 * 
 * @author nk160297
 */
public class CopyToProcessor {
    
    public static List<TreeItemFinder> constructFindersList(
            WLMComponent contextEntity, TTo copyTo,
            XPathExpression toExpr, 
            MapperLsmContainer lsmCont,
            MapperModelFactory modelFactory) {
        //
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        if (toExpr == null) {
            toExpr = constructExpression(contextEntity, copyTo,
                    lsmCont, modelFactory);
        }
        //
        if (toExpr != null) {
            if (toExpr instanceof AbstractLocationPath) {
                finderList.addAll(FinderListBuilder.build(
                        (AbstractLocationPath)toExpr));
            } else if (toExpr instanceof XPathVariableReference) {
                finderList.addAll(FinderListBuilder.build(
                        (XPathVariableReference)toExpr));
            }
        }
        //
        //
        return finderList;
    }
    
    public static XPathExpression constructExpression(
            WLMComponent contextEntity, TTo copyTo,
            MapperLsmContainer lsmCont, MapperModelFactory modelFactory) {
        //
        String exprLang = copyTo.getExpressionLanguage();
        String exprText = copyTo.getContent();
        boolean isXPathExpr = (exprLang == null || exprLang.length() == 0 ||
                WlmXPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang));
        //
        // we can handle only xpath expressions.
        if (isXPathExpr && exprText != null && exprText.length() != 0) {
            try {
                XPathCastResolver castResolver = null;
//                if (lsmCont != null) {
//                    castResolver = new XPathCastResolverImpl(lsmCont, false);
//                }
                XPathModel newXPathModel = WlmXPathModelFactory.create(
                        contextEntity, castResolver);
                //
                // NOT NEED to specify schema context because of an 
                // expression with variable is implied here. 
                //
                XPathExpression expr = newXPathModel.parseExpression(exprText);
                return expr;
            } catch (XPathException ex) {
                // Do nothing
            }
        }
        //
        return null;
    }

}
