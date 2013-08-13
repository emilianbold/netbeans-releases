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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.csl.PHPNavTestBase;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public class ModelTestBase extends PHPNavTestBase {
    public ModelTestBase(String testName) {
        super(testName);
    }

    public Model getModel(String code) throws Exception {
        final Model[] globals = new Model[1];
        super.performTest(new String[] {code}, new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parameter = (PHPParseResult) resultIterator.getParserResult();
                if (parameter != null) {
                    Model model = parameter.getModel();
                    globals[0] = model;
                }
            }
        });
        return globals[0];
    }

    public Occurence underCaret(final Model model,String code, final int offset) throws Exception {
        final List<Occurence> occurences = new ArrayList<Occurence>();
        super.performTest(new String[] {code}, new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parameter = (PHPParseResult) resultIterator.getParserResult();
                if (parameter != null) {
                    Model mod = model != null ? model : parameter.getModel();
                    OccurencesSupport occurencesSupport = mod.getOccurencesSupport(offset);
                    Occurence underCaret = occurencesSupport.getOccurence();
                    occurences.add(underCaret);
                }
            }
        });
        return occurences.get(0);
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        FileObject dataDir = FileUtil.toFileObject(getDataDir());
        try {
            return new FileObject[]{toFileObject(dataDir, "testfiles/model", true)}; //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
