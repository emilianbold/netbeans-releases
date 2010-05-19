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

package org.netbeans.modules.bpel.mapper.tree.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.model.BpelPathConverter;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.xpath.mapper.tree.FinderListBuilder;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
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
public class BpelFinderListBuilder implements FinderListBuilder {

    private static BpelFinderListBuilder singleton = new BpelFinderListBuilder();

    public static BpelFinderListBuilder singl() {
        return singleton;
    }

    public List<TreeItemFinder> build(XPathSchemaContext schemaContext) {
        if (schemaContext == null) {
            return Collections.emptyList();
        }
        //
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        LinkedList<Object> result = new LinkedList<Object>();
        //
        DirectedList<Object> pathList = BpelPathConverter.singleton().
                constructObjectLocationList(schemaContext, false, false);
        //
        if (pathList != null && !pathList.isEmpty()) {
            Iterator itr = pathList.backwardIterator();
            while (itr.hasNext()) {
                Object obj = itr.next();
                //
                if (obj instanceof AbstractVariableDeclaration) {
                    finderList.add(new VariableFinder(
                            (AbstractVariableDeclaration)obj));
                } else if (obj instanceof Part) {
                    finderList.add(new PartFinder((Part)obj));
                } else if (obj instanceof BpelMapperTypeCast) {
                    BpelMapperTypeCast typeCast = (BpelMapperTypeCast)obj;
                    Object castedObj = typeCast.getCastedObject();
                    if (castedObj instanceof SchemaComponent) {
                        result.addLast(typeCast);
                    } else if (castedObj instanceof AbstractVariableDeclaration) {
                        finderList.add(new CastedVariableFinder(typeCast));
                    } else if (castedObj instanceof Part) {
                        finderList.add(new CastedPartFinder(typeCast));
                    } else {
                        return Collections.emptyList();
                    }
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
    public List<TreeItemFinder> build(XPathVariableReference varRef, 
            BpelMapperTypeCast typeCast) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        XPathVariable var = varRef.getVariable();
        // Variable could be deleted but the reference no.
        // issue 128684
        if (var == null) {
            Logger.getLogger(BpelFinderListBuilder.class.getName()).log(Level.INFO,
                    NbBundle.getMessage(BpelFinderListBuilder.class, "LOG_MSG_VAR_NULL", varRef)); //NOI18N
            return finderList;
        }
        assert var instanceof XPathBpelVariable;
        XPathBpelVariable bpelVar = (XPathBpelVariable)var;
        AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
        Part part = bpelVar.getPart();
        //
        Object castedObj = null;
        if (typeCast != null) {
            castedObj = typeCast.getCastedObject();
        }
        //
        if (castedObj != null && castedObj == varDecl) {
            CastedVariableFinder varCastFinder = new CastedVariableFinder(typeCast);
            finderList.add(varCastFinder);
        } else {
            VariableFinder varFinder = new VariableFinder(varDecl);
            finderList.add(varFinder);
        }
        //
        if (part != null) {
            if (castedObj != null && castedObj == part) {
                CastedPartFinder partCastFinder = new CastedPartFinder(typeCast);
                finderList.add(partCastFinder);
            } else {
                PartFinder partFinder = new PartFinder(part);
                finderList.add(partFinder);
            }
        }
        //
        return finderList;
    }

    public List<TreeItemFinder> build(AbstractLocationPath locationPath) {
        return build(locationPath.getSchemaContext());
    }

}
