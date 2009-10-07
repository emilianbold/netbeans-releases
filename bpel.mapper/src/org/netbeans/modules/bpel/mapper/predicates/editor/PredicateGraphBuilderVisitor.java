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

import org.netbeans.modules.bpel.mapper.model.GraphBuilderVisitor;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.FinderListBuilder;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * Populates the Graph object with a complex content by an XPath expression.
 * 
 * @author nk160297
 */
public class PredicateGraphBuilderVisitor extends GraphBuilderVisitor {
    private XPathSchemaContext mSContext;
    
    public PredicateGraphBuilderVisitor(XPathSchemaContext sContext, 
        Graph graph, MapperSwingTreeModel leftTreeModel, 
        boolean connectToTargetTree, BpelDesignContext context) {
        super(graph, leftTreeModel, connectToTargetTree, context);
        mSContext = sContext;
    }

    @Override
    public void visit(XPathLocationPath locationPath) {
        connectToLeftTree(locationPath);
    }

    //--------------------------------------------------------------------------
    // Auxiliary methods
    //--------------------------------------------------------------------------

    private void connectToLeftTree(XPathLocationPath path) {
        LocationStep[] steps = path.getSteps();
        LocationStep lastLocationStep = steps[steps.length - 1];
        XPathSchemaContext sContext = lastLocationStep.getSchemaContext();
        connectToLeftTree(FinderListBuilder.build(sContext));
    }
    
}
    
