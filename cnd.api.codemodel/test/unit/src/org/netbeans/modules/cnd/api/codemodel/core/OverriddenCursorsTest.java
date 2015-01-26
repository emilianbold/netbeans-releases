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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;

/**
 *
 * @author petrk
 */
public class OverriddenCursorsTest extends CoreTestBase<CMCursor, CoreUtils.PositionData[]> {
    
    public OverriddenCursorsTest(String testName) {
        super(testName);
    }

    @Override
    protected CMCursor[] provide(CMTranslationUnit tu, URI sourceFile, CoreUtils.PositionData[] datas) {
        CMFile file = tu.getFile(sourceFile);
        
        List<CMCursor> cursors = new ArrayList<>();
        
        for (CoreUtils.PositionData data : datas) {
            CMSourceLocation sourceLocation = tu.getLocation(file, data.line, data.column);
            CMCursor cursor = tu.getCursor(sourceLocation);
            Iterator<CMCursor> iter = cursor.getDirectlyOverridden().iterator();
            if (iter.hasNext()) {
                while (iter.hasNext()) {
                    cursors.add(iter.next());
                }
            } else {
                cursors.add(null);
            }                
        }
        
        return cursors.toArray(new CMCursor[cursors.size()]);
    }

    @Override
    protected void dump(CMCursor object, StringBuilder out) {
        out.append(CoreUtils.dumpCursor(object));
    }

    public void testOverriddenCursors_1() throws Exception {
        performTest("overridden_cursors_1.cpp", asArray(make(6, 8), make(10, 10), make(21, 8), make(25, 19), make(33, 10), make(37, 10)));
    }
    
    
    private CoreUtils.PositionData[] asArray(CoreUtils.PositionData ... datas) {
        return datas;
    }

    private CoreUtils.PositionData make(int line, int column) {
        return new CoreUtils.PositionData(line, column);
    }
    
}