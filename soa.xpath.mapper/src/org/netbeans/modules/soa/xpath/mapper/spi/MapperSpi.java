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

package org.netbeans.modules.soa.xpath.mapper.spi;

import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmProcessor;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditor;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate.PredicateEditorFactory;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;

/**
 * Represents an SPI. It contains a set of general methods which the XPath mapper
 * expects the specific mapper implementation has to provide.
 *
 * @author Nikita Krjukov
 */
public interface MapperSpi {

    /**
     * Calculates XPath type of a source pin
     * @param sourcePin
     * @return
     */
    XPathType calculateXPathSourcePinType(SourcePin sourcePin);

    /**
     * Returns project dependant external model resolver.
     * @return
     */
    ExternalModelResolver getExternalModelResolver(
            MapperStaticContext staticContext);

    /**
     * Constructs a tree model for the Type Choose dialog.
     * @return
     */
    SoaTreeModel constructTypeChooserTreeModel(
            MapperStaticContext staticContext, boolean isAttribute);

    /**
     * Calculates a schema component associated to the specified tree item. 
     * @param treeItem
     * @return
     */
    SchemaComponent getAssociatedSchemaComp(Object treeItem);

    /**
     * Returns a factory to create a predicate editor.
     * @return
     */
    PredicateEditorFactory getPredicateEditorFactory();

    PredicateEditor constructPredicateEditor(
            XPathSchemaContext schContext, MapperPredicate pred,
             XPathMapperModel mapperModel, MapperStaticContext stContext);

    MapperLsmProcessor getMapperLsmProcessor();

    /**
     * This delimiter is used for keeping several XPath expressions in a single string.
     * @return
     */
    String getXPathExprDelimiter();

    /**
     * Register new namespace prefix for the specified namespace URI.
     * It is usually necessary when a new XPath extension function is added
     * fo an XPath expression (of mapper).
     * 
     * @param nsUri
     * @param stContext
     * @return
     */
    String registerNewNsPrefix(String nsUri, MapperStaticContext stContext);

}
