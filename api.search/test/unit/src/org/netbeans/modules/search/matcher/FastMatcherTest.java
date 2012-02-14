/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.matcher;

import java.io.File;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.MatchingObject.Def;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class FastMatcherTest extends NbTestCase {

    public FastMatcherTest(String name) {
        super(name);
    }

    public void testFastMatcher() {
        FastMatcher fm = new FastMatcher(SearchPattern.create("y",
                false, false, false));
        File f = new File("C:\\Users\\jhavlin\\Documents\\NetBeansProjects\\JavaApplication\\files\\testSearch\\textFiles\\text_30000_dos.txt");
        FileObject fo = FileUtil.toFileObject(f);
        Def def = fm.check(fo, new SearchListener() {

            @Override
            public void generalError(Throwable t) {
                Exceptions.printStackTrace(t);
            }
        });
        assertNotNull(def);
        assertEquals(9, def.getTextDetails().get(0).getLine());
    }
}
