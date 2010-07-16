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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.mapper.cast.BpelTypeChooserTreeModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.predicates.editor.BpelPredicateEditor;
import org.netbeans.modules.bpel.mapper.predicates.editor.BpelPredicateEditorFactory;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.BpelExternalModelResolver;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmProcessor;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditor;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditorFactory;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.spi.MapperSpi;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.openide.ErrorManager;

/**
 *
 * TODO: write comments
 *
 * @author Nikita Krjukov
 */
public class BpelMapperSpiImpl implements MapperSpi {

    private static BpelMapperSpiImpl singleton = new BpelMapperSpiImpl();

    public static MapperSpi singleton() {
        return singleton;
    }

    public XPathType calculateXPathSourcePinType(SourcePin sourcePin) {
        return BpelMapperUtils.calculateXPathSourcePinType(sourcePin);
    }

    public MapperLsmProcessor getMapperLsmProcessor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ExternalModelResolver getExternalModelResolver(
            MapperStaticContext staticContext) {
        MapperTcContext tcContext = (MapperTcContext)staticContext;
        BpelModel bpelModel = tcContext.getDesignContextController().
                getContext().getBpelModel();
        //
        BpelExternalModelResolver result = new BpelExternalModelResolver(bpelModel);
        return result;
    }

    public SoaTreeModel constructTypeChooserTreeModel(
            MapperStaticContext staticContext, boolean isAttribute) {
        //
        MapperTcContext tcContext = (MapperTcContext)staticContext;
        BpelModel bpelModel = tcContext.getDesignContextController().
                getContext().getBpelModel();
        //
        SoaTreeModel result = new BpelTypeChooserTreeModel(bpelModel, isAttribute);
        return result;
    }

    public SchemaComponent getAssociatedSchemaComp(Object treeItem) {
        return BpelMapperUtils.getAssociatedSchemaComp(treeItem);
    }

    public PredicateEditor constructPredicateEditor(
            XPathSchemaContext schContext, MapperPredicate pred,
            XPathMapperModel mapperModel, MapperStaticContext stContext) {
        return new BpelPredicateEditor(schContext, pred, mapperModel, stContext);
    }

    public String getXPathExprDelimiter() {
        return BpelXPathModelFactory.XPATH_EXPR_DELIMITER;
    }

    public String registerNewNsPrefix(String nsUri, MapperStaticContext stContext) {
        if (nsUri != null && nsUri.length() != 0) {
            MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
            BpelDesignContext dContext =
                    tcContext.getDesignContextController().getContext();
            BpelModel bpelModel = dContext.getBpelModel();
            Process process = bpelModel.getProcess();
            if (process != null) {
                ExNamespaceContext nsContext = process.getNamespaceContext();
                try {
                    return nsContext.addNamespace(nsUri);
                } catch (InvalidNamespaceException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        return null;
    }

    public PredicateEditorFactory getPredicateEditorFactory() {
        return new BpelPredicateEditorFactory();
    }

}
