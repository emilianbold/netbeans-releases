/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankPreprocessorCallback;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankTokenStreamProducer extends TokenStreamProducer {

    private ClankTokenStreamProducer(FileImpl file, FileContent newFileContent) {
        super(file, newFileContent);
    }
    
    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean index) {
        return new ClankTokenStreamProducer(file, newFileContent);
    }

    @Override
    public TokenStream getTokenStream(boolean triggerParsingActivity, Interrupter interrupter) {
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        ClankPreprocessorCallback callback = new MyClankPreprocessorCallback();
        FileImpl fileImpl = getMainFile();
        TokenStream tsFromClank = ClankDriver.getTokenStream(fileImpl.getBuffer(), ppHandler, callback, interrupter);
        APTLanguageFilter languageFilter = fileImpl.getLanguageFilter(ppHandler.getState());
        TokenStream filteredTokenStream = languageFilter.getFilteredStream(tsFromClank);
        return filteredTokenStream;
    }

    @Override
    public FilePreprocessorConditionState release() {
        return CsmCorePackageAccessor.get().createPCState(getMainFile().getAbsolutePath(), new int[] {0, 10});
    }
    
    private static final class MyClankPreprocessorCallback implements ClankPreprocessorCallback {
        
    }
}
