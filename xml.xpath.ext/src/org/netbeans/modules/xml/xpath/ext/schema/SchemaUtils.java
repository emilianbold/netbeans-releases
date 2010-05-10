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

package org.netbeans.modules.xml.xpath.ext.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nikita Krjukov
 */
public class SchemaUtils {

    private static SchemaModel mXsiModel;
    private static GlobalAttribute mXsiTypeAttr;

    public static synchronized SchemaModel getXsiModel() {
        if (mXsiModel == null) {
            mXsiModel = createXsiModel();
        }
        return mXsiModel;
    }

    public static synchronized GlobalAttribute getXsiTypeAttr() {
        if (mXsiTypeAttr == null) {
            SchemaModel sModel = getXsiModel();
            Collection<GlobalAttribute> gAttrs = sModel.getSchema().getAttributes();
            for (GlobalAttribute gAttr : gAttrs) {
                String name = gAttr.getName();
                if ("type".equals(name)) { // NOI18N
                    mXsiTypeAttr = gAttr;
                    break;
                }
            }
            //
            assert mXsiTypeAttr != null : "It has to be in the schema!"; // NOI18N
        }
        //
        return mXsiTypeAttr;
    }

    private static SchemaModel createXsiModel() {
        javax.swing.text.Document document;
        SchemaModel sModel;
        try {
            InputStream inStream = SchemaUtils.class.
                    getResourceAsStream("XMLSchema-instance.xsd"); //NOI18N
            document = AbstractDocumentModel.getAccessProvider().
                    loadSwingDocument(inStream);
            ModelSource ms = new ModelSource(Lookups.singleton(document), false);
            sModel = SchemaModelFactory.getDefault().createFreshModel(ms);
            sModel.sync();
        } catch (BadLocationException ex) {
            throw new RuntimeException("schema should be correct",ex); //NOI18N
        } catch (IOException ex) {
            throw new RuntimeException("schema should be correct",ex); //NOI18N
        }
        //
        assert sModel != null : "schema should be correct";
        //
        return sModel;
    }

    /**
     * Checks if the expression ends with the @xsi:type attribute.
     * @param toExpr
     * @return
     */
    public static boolean checkIfXsiType(XPathExpression toExpr) {
        if (toExpr instanceof XPathSchemaContextHolder) {
            XPathSchemaContext toSContext = XPathSchemaContextHolder.class.
                    cast(toExpr).getSchemaContext();
            if (toSContext != null) {
                SchemaComponent lastSComp = XPathSchemaContext.Utilities.
                        getSchemaComp(toSContext);
                if (lastSComp == getXsiTypeAttr()) {
                    return true;
                }
            }
        }
        return false;
    }

}
