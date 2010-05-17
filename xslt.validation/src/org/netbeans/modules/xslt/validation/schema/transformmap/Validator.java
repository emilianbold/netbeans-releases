/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xslt.validation.schema.transformmap;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.07.30
 */
public final class Validator extends XsdBasedValidator {

    @Override
    public ValidationResult validate(Model model, Validation validation, Validation.ValidationType validationType) {
        if (!(model instanceof TMapModel)) {
            return null;
        }
        startTime();
        ValidationResult result = Validator.super.validate((TMapModel) model, validation, validationType);
        endTime("Validator " + getName() + "    "); // NOI18N

        return result;
    }

    public String getName() {
        return getClass().getName();
    }

    protected Schema getSchema(Model model) {
        if (!(model instanceof TMapModel)) {
            return null;
        }
        return getTransformMapSchema(SoaUtil.isAllowBetaFeatures(
                SoaUtil.getProject(SoaUtil.getFileObjectByModel(model))));
    }

    private Schema getTransformMapSchema(boolean isExtSchema) {
        if (!isExtSchema && ourTransformMapSchema == null) {
            ourTransformMapSchema = getCompiledSchema(new InputStream[]{Validator.class.getResourceAsStream(isExtSchema ? TMAP_EXT_URL : TMAP_URL)}, new Resolver(isExtSchema));
        }
        if (isExtSchema && ourTransformMapExtSchema == null) {
            ourTransformMapSchema = getCompiledSchema(new InputStream[]{Validator.class.getResourceAsStream(isExtSchema ? TMAP_EXT_URL : TMAP_URL)}, new Resolver(isExtSchema));
        }
        return isExtSchema ? ourTransformMapExtSchema : ourTransformMapSchema;
    }

    private class Resolver implements LSResourceResolver {

        private boolean isExtSchema;
        
        public Resolver(boolean isExtSchema) {
            this.isExtSchema = isExtSchema;
        }
        
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            InputStream stream = null;

            if (systemId.equals(TMAP_ID)) {
                stream = Validator.class.getResourceAsStream(isExtSchema ? TMAP_EXT_URL : TMAP_URL);
            }
            if (stream == null) {
                return null;
            }
            DOMImplementation dom = null;

            try {
                dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            } catch (ParserConfigurationException ex) {
                return null;
            }
            DOMImplementationLS dols = (DOMImplementationLS) dom.getFeature("LS", "3.0"); // NOI18N
            LSInput input = dols.createLSInput();
            input.setByteStream(stream);

            return input;
        }
    }

    private static Schema ourTransformMapSchema;
    private static Schema ourTransformMapExtSchema;
    private static final String TMAP_ID = "http://www.sun.com/jbi/xsltse/transformmap/"; // NOI18N
    private static final String TMAP_URL = "/org/netbeans/modules/xslt/tmap/resources/transformmap.xsd"; // NOI18N
    private static final String TMAP_EXT_URL = "/org/netbeans/modules/xslt/tmap/resources/transformmap_ext.xsd"; // NOI18N
}
