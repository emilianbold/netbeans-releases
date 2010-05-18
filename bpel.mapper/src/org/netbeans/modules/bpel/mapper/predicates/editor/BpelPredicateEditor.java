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

import javax.swing.JPanel;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.palette.BpelPalette;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditor;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 *
 * @author  nk160297
 */
public class BpelPredicateEditor extends PredicateEditor {
    
    public BpelPredicateEditor(XPathSchemaContext sContext, MapperPredicate pred,
             XPathMapperModel mapperModel, MapperStaticContext stContext) {
        //
        super(sContext, pred, mapperModel, stContext);
        //
        createContent();
        initControls();
    }
    
    protected String calculatePredContextStr() {
        MapperTcContext tcContext = MapperTcContext.class.cast(mStContext);
        NamespaceContext nsContext = tcContext.getDesignContextController().
                getContext().getSelectedEntity().getNamespaceContext();
        String predContext = null;
        if (mSchContext instanceof PredicatedSchemaContext) {
            predContext = ((PredicatedSchemaContext) mSchContext).
                    getBaseContext().getExpressionString(nsContext, null);
        } else {
            predContext = mSchContext.getExpressionString(nsContext, null);
        }
        return predContext;
    }

    protected JPanel createPalette() {
        return new BpelPalette(mStContext).getPanel();
    }

    public Mapper createMapper(MapperModel model) {
        return new PredicatesMapperFactory().createMapper(mMapperModel);
    }

    public PredicateModelUpdater createModelUpdater() {
        MapperTcContext tcContext = MapperTcContext.class.cast(mStContext);
        BpelMapperModel bpelMModel = BpelMapperModel.class.cast(mMapperModel);
        //
        return new PredicateModelUpdater(tcContext, bpelMModel, mSchContext);
    }

    public MapperPredicate calculatePredicate() {
        return createModelUpdater().createNewPredicate();
    }

    public XPathPredicateExpression[] calculatePredicateExperArr() {
        return createModelUpdater().recalculatePredicates();
    }

    
}
