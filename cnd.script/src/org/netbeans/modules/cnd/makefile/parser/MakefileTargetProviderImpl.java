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

package org.netbeans.modules.cnd.makefile.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.builds.MakefileTargetProvider;
import org.netbeans.modules.cnd.makefile.model.AbstractMakefileElement;
import org.netbeans.modules.cnd.makefile.model.MakefileRule;
import org.netbeans.modules.cnd.makefile.model.MakefileUtils;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 * @author Alexey Vladykin
 */
public class MakefileTargetProviderImpl implements MakefileTargetProvider {

    private final FileObject fileObject;

    public MakefileTargetProviderImpl(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public Set<String> getPreferredTargets() throws IOException {
        return runRetrieverTask().getPreferredTargets();
    }

    @Override
    public Set<String> getRunnableTargets() throws IOException {
        return runRetrieverTask().getRunnableTargets();
    }

    private TargetRetrieverTask runRetrieverTask() throws IOException {
        TargetRetrieverTask task = new TargetRetrieverTask();
        try {
            ParserManager.parse(Collections.singletonList(Source.create(fileObject)), task);
            return task;
        } catch (ParseException ex) {
            throw new IOException(ex);
        }
    }

    private static class TargetRetrieverTask extends UserTask {

        private final Set<String> preferredTargets;
        private final Set<String> runnableTargets;

        public TargetRetrieverTask() {
            preferredTargets = new HashSet<String>();
            runnableTargets = new HashSet<String>();
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Parser.Result result = resultIterator.getParserResult();
            if (result instanceof MakefileParseResult) {
                MakefileParseResult makefileResult = (MakefileParseResult) result;
                for (AbstractMakefileElement element : makefileResult.getElements()) {
                    if (element.getKind() == ElementKind.RULE) {
                        MakefileRule rule = (MakefileRule) element;
                        for (String target : rule.getTargets()) {
                            if (MakefileUtils.isPreferredTarget(target)) {
                                preferredTargets.add(target);
                            }
                        }
                    }
                }
            }
        }

        private Set<String> getPreferredTargets() {
            return preferredTargets;
        }

        private Set<String> getRunnableTargets() {
            return runnableTargets;
        }
    }
}
