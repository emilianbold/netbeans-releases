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

package org.netbeans.modules.bpel.mapper.testutils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.BpelTestUtils;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Tests if the projects in test resources folder are correct.
 *
 * @author Nikita Krjukov
 */
public class TestProjectConsistency {

    @Test
    public void testTestProjectsConsistency() throws Exception {
        TestProjects[] projects = TestProjects.values();
        for (int index = 0; index < projects.length; index++) {
            BpelModel bpelModel = projects[index].getBpelModel();
            assertNotNull(bpelModel);
            //
            checkImportedFilesAccessibility(bpelModel);
            checkVariableTypesAccessibility(bpelModel);
        }
    }

    /**
     * Checks that all variables has a type, which is accessible.
     * @throws java.lang.Exception
     */
    private void checkVariableTypesAccessibility(BpelModel bpelModel) {
        // Check types of variables. 
        SimpleBpelModelVisitor visitor = new SimpleBpelModelVisitorAdaptor() {
            @Override
            public void visit(Variable variable) {
                VariableStereotype vs = EditorUtil.getVariableStereotype(variable);
                assertNotNull(vs);
                switch (vs) {
                    case GLOBAL_COMPLEX_TYPE:
                    case GLOBAL_SIMPLE_TYPE:
                    case GLOBAL_TYPE:
                    case PRIMITIVE_TYPE:
                    case GLOBAL_ELEMENT:
                        SchemaComponent sc = EditorUtil.getVariableSchemaType(variable);
                        assertNotNull(sc);
                        break;
                    case MESSAGE:
                        Reference ref = EditorUtil.getVariableType(variable);
                        assertNotNull(ref);
                        Object refrenceable = ref.get();
                        assertNotNull(refrenceable);
                        assert refrenceable instanceof Message;
                        Message msg = Message.class.cast(refrenceable);
                        assertNotNull(msg);
                        //
                        Collection<Part> parts = msg.getParts();
                        for (Part part : parts) {
                            NamedComponentReference partTypeRef = part.getElement();
                            if (partTypeRef == null) {
                                partTypeRef = part.getType();
                            }
                            assertNotNull(partTypeRef);
                            //
                            Referenceable partType = partTypeRef.get();
                            assertNotNull(partType);
                        }
                        break;
                }
            }
        };
        bpelModel.getProcess().accept(visitor);
    }

    /**
     * Checks that all variables has a type, which is accessible.
     * @throws java.lang.Exception
     */
    public void checkImportedFilesAccessibility(BpelModel bpelModel) throws Exception {
        // Checks BPEL imports
        BpelTestUtils.checkImports(bpelModel);
    }

}
