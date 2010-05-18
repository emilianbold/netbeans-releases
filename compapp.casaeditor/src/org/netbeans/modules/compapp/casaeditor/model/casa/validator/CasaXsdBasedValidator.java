/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.model.casa.validator;

import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class CasaXsdBasedValidator extends XsdBasedValidator {

    private static Schema schema;

    private static final String CASA_XSD_URL =
            "nbres:/org/netbeans/modules/compapp/casaeditor/resources/casa.xsd"; // NOI18N

    protected Schema getSchema(Model model) {
        if (!(model instanceof CasaWrapperModel)) {
            return null;
        }

        // This will not be used as validate(.....) method is being overridden here.
        // So just return a schema returned by newSchema().
        if (schema == null) {
            try {
                schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
                        new URL(CASA_XSD_URL));
            } catch (Exception ex) {
                assert false : "Error while creating compiled schema for CASA."; // NOI18N
            }
        }
        return schema;
    }

    public String getName() {
        return NbBundle.getMessage(CasaXsdBasedValidator.class, "LBL_CASA_Schema_Validator"); // NOI18N
    }
}
