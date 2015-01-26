/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.core;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.test.CMBaseTestCase;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author petrk
 */
abstract class CoreTestBase<T, D> extends CMBaseTestCase {
    
    protected CoreTestBase(String testName) {
        super(testName);
    }
        
    protected abstract T[] provide(CMTranslationUnit tu, URI sourceFile, final D data);
    
    protected abstract void dump(T object, StringBuilder out);   

    protected void performTest(String source, D data) throws Exception {
        performTest(source, data, getName() + ".ref");// NOI18N
    }

    protected void performTest(final String source, final D data, final String goldenFileName) throws Exception {

        performTest(new TestPerformer() {

            @Override
            public void perform(File... sourceFiles) throws Exception {
                URI uri = Utilities.toURI(sourceFiles[0]);
                
                Collection<CMTranslationUnit> units = SPIUtilities.getTranslationUnits(uri);
                
                if (!units.isEmpty()) {
                    CMTranslationUnit tu = units.iterator().next();
                
                    T[] objects = provide(tu, uri, data);

                    if (objects != null && objects.length > 0) {
                        for (T object : objects) {
                            if (object != null) {
                                StringBuilder builder = new StringBuilder();
                                dump(object, builder);
                                ref(builder.toString());
                            } else {
                                ref("NULL!");
                            }
                        }
                    }
                }
                
                compareReferenceFiles();
            }
        }, source);
    }
}
