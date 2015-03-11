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
package org.netbeans.modules.cnd.modelimpl.parser.spi;

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTTokenStreamProducer;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankTokenStreamProducer;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class TokenStreamProducer {
    private PreprocHandler curPreprocHandler;
    private String language = APTLanguageSupport.GNU_CPP;
    private String languageFlavor = APTLanguageSupport.FLAVOR_UNKNOWN;
    private final FileImpl fileImpl;
    private final FileContent fileContent;

    protected TokenStreamProducer(FileImpl fileImpl, FileContent newFileContent) {
        assert fileImpl != null : "null file is not allowed";        
        assert newFileContent != null : "null file content is not allowed";        
        this.fileImpl = fileImpl;
        this.fileContent = newFileContent;
    }        
    
    public static TokenStreamProducer create(FileImpl file, boolean emptyFileContent, boolean index) {
        FileContent newFileContent = FileContent.getHardReferenceBasedCopy(file.getCurrentFileContent(), emptyFileContent);
        if (APTTraceFlags.USE_CLANK) {
            return ClankTokenStreamProducer.createImpl(file, newFileContent, index);
        } else {
            return APTTokenStreamProducer.createImpl(file, newFileContent, index);
        }
    }
    
    public abstract TokenStream getTokenStream(boolean triggerParsingActivity);
    
    /** must be called when TS was completely consumed */
    public abstract FilePreprocessorConditionState release();

    public void prepare(PreprocHandler handler, String language, String languageFlavor) {
        assert handler != null : "null preprocHandler is not allowed";
        curPreprocHandler = handler;
        assert language != null : "null language is not allowed";
        this.language = language;
        assert languageFlavor != null : "null language flavor is not allowed";
        this.languageFlavor = languageFlavor;
    }
    
    public PreprocHandler getCurrentPreprocHandler() {
        return curPreprocHandler;
    }
    
    public String getLanguage() {
        return language;
    }        

    public String getLanguageFlavor() {
        return languageFlavor;
    }

    public FileImpl getMainFile() {
        return fileImpl;
    }    

    public FileContent getFileContent() {
        assert fileContent != null;
        return fileContent;
    }
}
