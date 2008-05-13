/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.hints;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.groovy.editor.GroovyCompilerErrorID;
import org.netbeans.modules.groovy.editor.hints.spi.Description;
import org.netbeans.modules.groovy.editor.hints.spi.ErrorRule;
import org.netbeans.modules.groovy.editor.hints.spi.HintSeverity;
import org.netbeans.modules.groovy.editor.hints.spi.RuleContext;
import org.netbeans.modules.gsf.api.CompilationInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.editor.actions.FixImportsHelper;
import org.netbeans.modules.groovy.editor.actions.FixImportsHelper.ImportCandidate;
import org.netbeans.modules.groovy.editor.hints.spi.Fix;
import org.openide.util.NbBundle;
import org.netbeans.modules.groovy.editor.parser.GroovyParser.GroovyError;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 *
 * @author schmidtm
 */
public class ClassNotFoundRule implements ErrorRule {

    public static final Logger LOG = Logger.getLogger(ClassNotFoundRule.class.getName()); // NOI18N
    String DESC = NbBundle.getMessage(ClassNotFoundRule.class, "FixImportsHintDescription");
    
    FixImportsHelper helper = new FixImportsHelper();
    
    public ClassNotFoundRule() {
        // LOG.setLevel(Level.FINEST);
    }
    
    public Set<GroovyCompilerErrorID> getCodes() {
        LOG.log(Level.FINEST, "getCodes()");
        Set<GroovyCompilerErrorID> result = new HashSet<GroovyCompilerErrorID>();
        result.add(GroovyCompilerErrorID.CLASS_NOT_FOUND);
        return result;
    }

    public void run(RuleContext context, GroovyError error, List<Description> result) {
        LOG.log(Level.FINEST, "run()");
//        LOG.log(Level.FINEST, "context : " + context);
//        LOG.log(Level.FINEST, "error : " + error);

        String desc = error.getDescription();

        if (desc == null) {
            LOG.log(Level.FINEST, "desc == null");
            return;
        }

        LOG.log(Level.FINEST, "Processing : " + desc);

        String missingClassName = helper.getMissingClassName(desc);

        if (missingClassName == null) {
            return;
        }

        FileObject fo = context.compilationInfo.getFileObject();

        List<ImportCandidate> importCandidates = 
                helper.getImportCandidate(fo, missingClassName);
        

        if (importCandidates.isEmpty()) {
            return;
        }
        
        int DEFAULT_PRIORITY = 292;
        
        OffsetRange range = new OffsetRange(error.getStartPosition(), error.getEndPosition());
        
        for (ImportCandidate candidate : importCandidates) {
            List<Fix> fixList = new ArrayList<Fix>(1);
            String fqn = candidate.getFqnName();
            Fix fixToApply = new AddImportFix(fo, fqn);
            fixList.add(fixToApply);
            
            Description descriptor = new Description(this, fixToApply.getDescription(), fo, range,
            fixList, DEFAULT_PRIORITY);

            result.add(descriptor);
        }

        return;
    }

    public boolean appliesTo(CompilationInfo compilationInfo) {
        return true;
    }

    public String getDisplayName() {
        return DESC;
    }

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
    
    private class AddImportFix implements Fix {

        String HINT_PREFIX = NbBundle.getMessage(ClassNotFoundRule.class, "ClassNotFoundRuleHintDescription");
        FileObject fo;
        String fqn;
        
        public AddImportFix(FileObject fo, String fqn) {
            this.fo = fo;
            this.fqn = fqn;
        }

        public String getDescription() {
            return HINT_PREFIX + " " + fqn;
        }

        public void implement() throws Exception {
            helper.doImport(fo, fqn);
            return;
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}
