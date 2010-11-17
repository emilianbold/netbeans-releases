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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;

/**
 * 
 * @author Vladimir Voskresensky
 */
public class ModelImplTest extends ModelImplBaseTestCase {
    public ModelImplTest(String testName) {
        super(testName);
    }
    
    public void testModelProvider() {
        CsmModel csmModel = CsmModelAccessor.getModel();
        assertNotNull("Null model", csmModel);
        assertTrue("Unknown model provider " + csmModel.getClass().getName(), csmModel instanceof ModelImpl);
    }
    
    public static void dumpProjectContainers(ProjectBase project) {
        PrintStream printStream = System.err;
        dumpProjectContainers(project.getClassifierSorage(), printStream);
        dumpProjectContainers(project.getDeclarationsSorage(), printStream);
    }

    private static void dumpProjectContainers(ClassifierContainer container, PrintStream printStream) {
        printStream.println("\n========== Dumping Dump Project Classifiers");
        for (Map.Entry<CharSequence, CsmClassifier> entry : container.getClassifiers().entrySet()) {
            printStream.print("\t" + entry.getKey().toString() + " ");
            if (entry.getValue() == null) {
                printStream.println("null");
            } else {
                printStream.println(entry.getValue().getUniqueName());
            }
        }
        printStream.println("\n========== Dumping Dump Project Typedefs");
        for (Map.Entry<CharSequence, CsmClassifier> entry : container.getTypedefs().entrySet()) {
            printStream.print("\t" + entry.getKey().toString() + " ");
            if (entry.getValue() == null) {
                printStream.println("null");
            } else {
                printStream.println(entry.getValue().getUniqueName());
            }
        }
    }
    
    private static void dumpProjectContainers(DeclarationContainerProject container, PrintStream printStream) {
        printStream.println("\n========== Dumping Project declarations");
        for (Map.Entry<CharSequence, Object> entry : container.testDeclarations().entrySet()) {
            printStream.println("\t" + entry.getKey().toString());
            TreeMap<CharSequence, CsmDeclaration> set = new TreeMap<CharSequence, CsmDeclaration>();
            Object o = entry.getValue();
            if (o instanceof CsmUID<?>[]) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                CsmUID<CsmDeclaration>[] uids = (CsmUID<CsmDeclaration>[]) o;
                for (CsmUID<CsmDeclaration> uidt : uids) {
                    set.put(((CsmOffsetableDeclaration) uidt.getObject()).getContainingFile().getAbsolutePath(), uidt.getObject());
                }
            } else if (o instanceof CsmUID<?>) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                CsmUID<CsmDeclaration> uidt = (CsmUID<CsmDeclaration>) o;
                set.put(((CsmOffsetableDeclaration) uidt.getObject()).getContainingFile().getAbsolutePath(), uidt.getObject());
            }
            for (Map.Entry<CharSequence, CsmDeclaration> f : set.entrySet()) {
                printStream.print("\t\t" + f.getValue());
            }
        }
        printStream.println("\n========== Dumping Project friends");
        for (Map.Entry<CharSequence, Set<CsmUID<CsmFriend>>> entry : container.testFriends().entrySet()) {
            printStream.print("\t" + entry.getKey().toString() + " ");
            TreeMap<CharSequence, CsmFriend> set = new TreeMap<CharSequence, CsmFriend>();
            for (CsmUID<? extends CsmFriend> uid : entry.getValue()) {
                CsmFriend f = uid.getObject();
                set.put(f.getQualifiedName(), f);
            }
            for (Map.Entry<CharSequence, CsmFriend> f : set.entrySet()) {
                printStream.print("\t\t" + f.getKey().toString() + " " + f.getValue());
            }
        }
    }    
}
