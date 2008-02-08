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

package org.netbeans.modules.bpel.mapper.predicates.editor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.SimpleSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.VariableSchemaContext;

/**
 * The auxiliary class to convert tree path or path iterator to other forms.
 * 
 * @author nk160297
 */
public class PathConverter {

    private static enum ParsingStage {
        SCHEMA, PART, VARIABLE;
    };

    /**
     * Builds an XPathSchemaContext by a RestartableIterator. 
     * It is implied that the RestartableIterator provides a collection of 
     * tree items' data objects in order from leafs to the tree root.
     * 
     * @param pathItr
     * @return
     */
    public static XPathSchemaContext constructContext(
            RestartableIterator<Object> pathItr) {
        //
        pathItr.restart();
        //
        LinkedList<SchemaComponent> sCompList = 
                new LinkedList<SchemaComponent>();
        Part part = null;
        AbstractVariableDeclaration var = null;
        //
        // Process the path
        ParsingStage stage = null;
        while (pathItr.hasNext()) {
            Object obj = pathItr.next();
            if (obj instanceof SchemaComponent) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                sCompList.addFirst((SchemaComponent)obj);
            } else if (obj instanceof AbstractPredicate) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                sCompList.addFirst(((AbstractPredicate)obj).getSComponent());
            } else if (obj instanceof Part) {
                if (!(stage == ParsingStage.SCHEMA || stage == null)) {
                    return null;
                }
                stage = ParsingStage.PART;
                part = (Part)obj;
            } else if (obj instanceof AbstractVariableDeclaration) {
                if (!(stage == ParsingStage.SCHEMA || 
                        stage == ParsingStage.PART || 
                        stage == null)) {
                    return null;
                }
                stage = ParsingStage.VARIABLE;
                var = (AbstractVariableDeclaration)obj;
                //
                // Everything found!
                break;
            } else {
                if (stage == null) {
                    return null;
                }
                break;
            }
        }
        //
        VariableDeclaration varDecl = null;
        if (var != null) {
            if (var instanceof VariableDeclaration) {
                varDecl = (VariableDeclaration)var;
            } else if (var instanceof VariableDeclarationWrapper) {
                varDecl = ((VariableDeclarationWrapper)var).getDelegate();
            }
        }
        //
        XPathBpelVariable xPathVariable = null;
        //
        if (varDecl != null) {
            xPathVariable = new XPathBpelVariable(varDecl, part);
        }
        //
        VariableSchemaContext varContext = null;
        if (xPathVariable != null) {
            varContext = new VariableSchemaContext(xPathVariable);
        }
        //
        XPathSchemaContext xPathContext = SimpleSchemaContext.
                constructSimpleSchemaContext(varContext, sCompList);
        //
        return xPathContext;
    }

    /**
     * Constructs a new list, which contains the schema elements, predicates, 
     * part and variable from the specified iterator pathItr. 
     * The first object taken from iterator will be at the beginning of the list. 
     * If the iterator has incompatible content then the null is returned. 
     * 
     * @param pathItr
     * @return
     */
    public static List<Object> constructPredicateLocationtList(
            RestartableIterator<Object> pathItr) {
        //
        pathItr.restart();
        //
        ArrayList<Object> treeItemList = new ArrayList<Object>();
        //
        // Process the path
        ParsingStage stage = null;
        while (pathItr.hasNext()) {
            Object obj = pathItr.next();
            if (obj instanceof SchemaComponent) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                treeItemList.add(obj);
            } else if (stage != null && obj instanceof AbstractPredicate) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                treeItemList.add(obj);
            } else if (obj instanceof Part) {
                if (stage != ParsingStage.SCHEMA) {
                    return null;
                }
                stage = ParsingStage.PART;
                treeItemList.add(obj);
            } else if (obj instanceof AbstractVariableDeclaration) {
                if (!(stage == ParsingStage.SCHEMA || 
                        stage == ParsingStage.PART)) {
                    return null;
                }
                //
                AbstractVariableDeclaration var = (AbstractVariableDeclaration)obj;
                //
                VariableDeclaration varDecl = null;
                if (var instanceof VariableDeclaration) {
                    varDecl = (VariableDeclaration)var;
                } else if (var instanceof VariableDeclarationWrapper) {
                    varDecl = ((VariableDeclarationWrapper)var).getDelegate();
                }
                //
                if (varDecl == null) {
                    return null;
                }
                //
                stage = ParsingStage.VARIABLE;
                treeItemList.add(varDecl);
                //
                // Everything found!
                break;
            } else {
                if (stage == null) {
                    return null;
                }
                break;
            }
        }
        //
        return treeItemList;
    }
    
    public static String toString(RestartableIterator<Object> pathItr) {
        LinkedList<Object> list = new LinkedList<Object>();
        pathItr.restart();
        while (pathItr.hasNext()) {
            list.addFirst(pathItr.next());
        }
        //
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Object obj : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("/"); // NOI18N
            }
            sb.append(obj.toString());
        }
        //
        return sb.toString();
    }
    
}
