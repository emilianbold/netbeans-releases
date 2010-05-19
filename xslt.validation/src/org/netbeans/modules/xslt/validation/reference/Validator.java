/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xslt.validation.reference;

import java.util.List;

import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.CallTemplate;
import org.netbeans.modules.xslt.model.CharacterMap;
import org.netbeans.modules.xslt.model.TypeSpec;
import org.netbeans.modules.xslt.model.UseAttributesSetsSpec;
import org.netbeans.modules.xslt.model.UseCharacterMapsSpec;
import org.netbeans.modules.xslt.model.WithParam;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.XslVisitorAdapter;

import org.netbeans.modules.xslt.validation.core.XsltValidator;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends XsltValidator {

    public XslVisitor getVisitor() {
        return new XslVisitorAdapter() {

            @Override
            public void visit(CallTemplate callTemplate) {
//out();
//out("callTemplate: " + callTemplate);
                checkReference(callTemplate, callTemplate.getName());
            }

            @Override
            public void visit(TypeSpec typeSpec) {
//out();
//out("typeSpec: " + typeSpec);
                checkReference(typeSpec, typeSpec.getType());
            }

            @Override
            public void visit(UseAttributesSetsSpec useAttributesSetsSpec) {
//out();
//out("useAttributesSetsSpec: " + useAttributesSetsSpec);
                List<XslReference<AttributeSet>> sets = useAttributesSetsSpec.getUseAttributeSets();

                for (XslReference<AttributeSet> set : sets) {
                    checkReference(useAttributesSetsSpec, set);
                }
            }

            @Override
            public void visit(UseCharacterMapsSpec useCharacterMapsSpec) {
//out();
//out("useCharacterMapsSpec: " + useCharacterMapsSpec);
                List<XslReference<CharacterMap>> sets = useCharacterMapsSpec.getUseCharacterMaps();

                for (XslReference<CharacterMap> set : sets) {
                    checkReference(useCharacterMapsSpec, set);
                }
            }

            @Override
            public void visit(WithParam withParam) {
//out();
//out("withParam: " + withParam);
                checkReference(withParam, withParam.getName());
            }
        };
    }
}
