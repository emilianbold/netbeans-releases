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
package org.netbeans.modules.xslt.validation.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xml.validation.core.Validator;
import org.netbeans.modules.xslt.tmap.TMapConstants;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.validation.ValidatorUtil;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.05.07
 */
public abstract class TMapValidator extends Validator {

    public abstract TMapVisitor getVisitor();

    public synchronized ValidationResult validate(Model model, Validation validation, ValidationType type) {
        ValidationResult validationResult = checkTransformMapNamespace(model);

        if ((!(model instanceof TMapModel)) || (validationResult != null)) {
            return validationResult;
        }
        //out();
        //out("TMAP VALIDATOR");
        //out();
        TMapModel tmapModel = (TMapModel) model;

        if (tmapModel.getState() == Model.State.NOT_WELL_FORMED) {
            return null;
        }
        init(validation, type);
        TransformMap transformMap = tmapModel.getTransformMap();

        if (transformMap == null) {
            return null;
        }
        startTime();
        transformMap.accept(getVisitor());
        endTime(getDisplayName());

        return createValidationResult(tmapModel);
    }

    private ValidationResult checkTransformMapNamespace(Model model) {
        String transformmapNamespace = ValidatorUtil.getXmlTagAttributeValue(model, TMapConstants.TRANSFORMMAP_TAG_NAME, TMapConstants.TRANSFORMMAP_ATTRIBUTE_NAMESPACE_PREFIX);

        if (transformmapNamespace == null) {
            return null;
        }
        String errMsg = "";
        int lineNumber = 0;
        int columnNumber = 0;
        Set<ResultItem> setResultItems = new HashSet<ResultItem>();

        if (TMapConstants.OLD_TRANSFORM_MAP_NS_URI.equals(transformmapNamespace)) {
            errMsg = i18n(TMapValidator.class, "FIX_Deprecated_TMap"); // NOI18N
            setResultItems.add(new ResultItem(this, ResultType.ERROR, errMsg, lineNumber, columnNumber, model));
        }
        if (!TMapComponent.TRANSFORM_MAP_NS_URI.equals(transformmapNamespace)) {
            errMsg = i18n(TMapValidator.class, "FIX_Incorrect_Namespace", transformmapNamespace); // NOI18N
            setResultItems.add(new ResultItem(this, ResultType.ERROR, errMsg, lineNumber, columnNumber, model));
        }
        return setResultItems.isEmpty() ? null : new ValidationResult(setResultItems, Collections.singleton(model));
    }
}
