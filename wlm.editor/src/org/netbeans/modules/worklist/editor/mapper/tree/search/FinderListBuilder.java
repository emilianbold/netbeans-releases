/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.tree.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.xpath.XPathWlmVariable;
import org.netbeans.modules.worklist.editor.mapper.model.PathConverter;
import org.netbeans.modules.worklist.editor.mapper.model.PathConverter.DirectedList;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class FinderListBuilder {

    public static List<TreeItemFinder> build(XPathSchemaContext schemaContext) {
        if (schemaContext == null) {
            return Collections.EMPTY_LIST;
        }
        //
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        LinkedList result = new LinkedList();
        //
        DirectedList<Object> pathList = PathConverter.
                constructObjectLocationList(schemaContext, false, false);
        //
        if (pathList != null && !pathList.isEmpty()) {
            Iterator itr = pathList.backwardIterator();
            while (itr.hasNext()) {
                Object obj = itr.next();
                //
                if (obj instanceof VariableDeclaration) {
                    finderList.add(new VariableFinder((VariableDeclaration)obj));
                } else if (obj instanceof Part) {
                    finderList.add(new PartFinder((Part)obj));
//                } else if (obj instanceof AbstractTypeCast) {
//                    AbstractTypeCast typeCast = (AbstractTypeCast)obj;
//                    Object castedObj = typeCast.getCastedObject();
//                    if (castedObj instanceof SchemaComponent) {
//                        result.addLast(typeCast);
//                    } else if (castedObj instanceof AbstractVariableDeclaration) {
//                        finderList.add(new CastedVariableFinder(typeCast));
//                    } else if (castedObj instanceof Part) {
//                        finderList.add(new CastedPartFinder(typeCast));
//                    } else {
//                        return Collections.EMPTY_LIST;
//                    }
                } else {
                    result.addLast(obj);
                }
            }
            //
            if (!result.isEmpty()) {
                PathFinder pathFinder = new PathFinder(result);
                finderList.add(pathFinder);
            }
        }
        //
        return finderList;
    }

    /**
     * Builds a set of finders for looking a variable with or without a part. 
     * If a type cast parameter is specified then it is intended for looking 
     * the casted variable or casted part. 
     * @param varRef
     * @param typeCast
     * @return
     */
    public static List<TreeItemFinder> build(XPathVariableReference varRef) {
            // TypeCast typeCast) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        XPathVariable var = varRef.getVariable();
        // Variable could be deleted but the reference no.
        // issue 128684
        if (var == null) {
            Logger.getLogger(FinderListBuilder.class.getName()).log(Level.INFO, 
                    NbBundle.getMessage(FinderListBuilder.class, "LOG_MSG_VAR_NULL", varRef)); //NOI18N
            return finderList;
        }
        assert var instanceof XPathWlmVariable;
        XPathWlmVariable wlmVar = (XPathWlmVariable)var;
        VariableDeclaration varDecl = wlmVar.getVarDecl();
        Part part = wlmVar.getPart();
        //
        Object castedObj = null;
//        if (typeCast != null) {
//            castedObj = typeCast.getCastedObject();
//        }
        //
//        if (castedObj != null && castedObj == varDecl) {
//            CastedVariableFinder varCastFinder = new CastedVariableFinder(typeCast);
//            finderList.add(varCastFinder);
//        } else {
            VariableFinder varFinder = new VariableFinder(varDecl);
            finderList.add(varFinder);
//        }
        //
        if (part != null) {
//            if (castedObj != null && castedObj == part) {
//                CastedPartFinder partCastFinder = new CastedPartFinder(typeCast);
//                finderList.add(partCastFinder);
//            } else {
                PartFinder partFinder = new PartFinder(part);
                finderList.add(partFinder);
//            }
        }
        //
        return finderList;
    }

    public static List<TreeItemFinder> build(AbstractLocationPath locationPath) {
        return build(locationPath.getSchemaContext());
    }

}
