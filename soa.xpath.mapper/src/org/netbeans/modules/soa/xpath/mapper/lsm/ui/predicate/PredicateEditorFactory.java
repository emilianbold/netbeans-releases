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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate;

import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtensionsManagerHolder;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.spi.MapperSpi;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 *
 * @author nikita
 */
public abstract class PredicateEditorFactory {

    /**
     * Returns a factory to create mapper model for predicate editor.
     * @return
     */
    public abstract PredicateMapperModelFactory getPredicateMapperModelFactory();

    protected abstract PredicateMapperStatContext consructWrapperContext(
            MapperStaticContext ownerContext);

    /**
     * Extracts extension manager holder from owner's mapper model according
     * to the side for which the predicate is created.
     * The result has to be ReadOnly holder!
     *
     * @param ownerStaticContext
     * @param inLeftTree
     * @return
     */
    protected abstract ExtensionsManagerHolder.ReadOnly extractBxmh(
            MapperStaticContext ownerStaticContext, boolean inLeftTree);

    /**
     * Construct a new predicate editor for editing of an existing predicate.
     *
     * @param ownerContext
     * @param oldPred
     * @param inLeftTree
     * @return
     */
    public PredicateEditor constructPredEditor(
            final MapperStaticContext ownerContext, 
            final MapperPredicate oldPred,
            final boolean inLeftTree) {
        //
        PredicatedSchemaContext schContext = oldPred.getSchemaContext();
        XPathSchemaContext baseSchContext = schContext.getBaseContext();
        //
        // Create new mapper TC context
        PredicateMapperStatContext stContext = consructWrapperContext(ownerContext);
        ExtensionsManagerHolder.ReadOnly emh = extractBxmh(ownerContext, inLeftTree);
        MapperSpi mapperSpi = ownerContext.getMapperSpi();
        //
        PredicateMapperModelFactory modelFactory = getPredicateMapperModelFactory();
        XPathMapperModel predMModel = modelFactory.constructModel(
                stContext, schContext, oldPred, emh);
        //
        PredicateEditor editor = mapperSpi.constructPredicateEditor(
                baseSchContext, oldPred, predMModel, stContext);
        //
        stContext.setMapper(editor.getMapper());
        stContext.setPredicateEditor(editor);
        //
        return editor;
    }


    /**
     * Construct a new predicate editor for creation of a new predicate.
     * @param ownerContext
     * @param baseSchContext
     * @param inLeftTree
     * @return
     */
    public PredicateEditor constructPredEditor(
            final MapperStaticContext ownerContext,
            final XPathSchemaContext baseSchContext,
            final boolean inLeftTree) {
        //
        // Create new mapper TC context
        PredicateMapperStatContext stContext = consructWrapperContext(ownerContext);
        ExtensionsManagerHolder.ReadOnly emh = extractBxmh(ownerContext, inLeftTree);
        MapperSpi mapperSpi = ownerContext.getMapperSpi();
        //
        PredicateMapperModelFactory modelFactory = getPredicateMapperModelFactory();
        XPathMapperModel predMModel = 
                modelFactory.constructEmptyModel(stContext, emh);
        //
        PredicateEditor editor = mapperSpi.constructPredicateEditor(
                baseSchContext, null, predMModel, stContext);
        //
        stContext.setMapper(editor.getMapper());
        stContext.setPredicateEditor(editor);
        //
        return editor;
    }

}
