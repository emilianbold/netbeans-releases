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
package org.netbeans.modules.groovy.editor.hints;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.groovy.editor.api.GroovyCompilerErrorID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.groovy.editor.actions.FixImportsHelper;
import org.netbeans.modules.groovy.editor.actions.FixImportsHelper.ImportCandidate;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyErrorRule;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyRuleContext;
import org.netbeans.modules.groovy.editor.api.parser.GroovyError;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author schmidtm, Petr Hejl
 */
public class ClassNotFoundRule extends GroovyErrorRule {

    public static final Logger LOG = Logger.getLogger(ClassNotFoundRule.class.getName()); // NOI18N
    private final String DESC = NbBundle.getMessage(ClassNotFoundRule.class, "FixImportsHintDescription");
    private final FixImportsHelper helper = new FixImportsHelper();

    public ClassNotFoundRule() {
        super();
    }

    public Set<GroovyCompilerErrorID> getCodes() {
        LOG.log(Level.FINEST, "getCodes()");
        Set<GroovyCompilerErrorID> result = new HashSet<GroovyCompilerErrorID>();
        result.add(GroovyCompilerErrorID.CLASS_NOT_FOUND);
        return result;
    }

    public void run(GroovyRuleContext context, GroovyError error, List<Hint> result) {
        LOG.log(Level.FINEST, "run()");

        String desc = error.getDescription();

        if (desc == null) {
            LOG.log(Level.FINEST, "desc == null");
            return;
        }

        LOG.log(Level.FINEST, "Processing : {0}", desc);

        String missingClassName = FixImportsHelper.getMissingClassName(desc);

        if (missingClassName == null) {
            return;
        }

        // FIXME parsing API
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();

        List<ImportCandidate> importCandidates =
                helper.getImportCandidate(fo, missingClassName);


        if (importCandidates.isEmpty()) {
            return;
        }

        int DEFAULT_PRIORITY = 292;

        // FIXME: for CLASS_NOT_FOUND errors we mark the whole line.
        // This should be replaced with marking the indentifier only.
        // OffsetRange range = new OffsetRange(error.getStartPosition(), error.getEndPosition());
        int lineStart = 0;
        int lineEnd = 0;

        try {

            lineStart = Utilities.getRowStart(context.doc, error.getStartPosition());
            lineEnd = Utilities.getRowEnd(context.doc, error.getEndPosition());

        } catch (BadLocationException ex) {
            LOG.log(Level.FINEST, "Processing : {0}", ex);
            return;
        }

        OffsetRange range = new OffsetRange(lineStart, lineEnd);

        for (ImportCandidate candidate : importCandidates) {
            List<HintFix> fixList = new ArrayList<HintFix>(1);
            String fqn = candidate.getFqnName();
            HintFix fixToApply = new AddImportFix(fo, fqn);
            fixList.add(fixToApply);

            Hint descriptor = new Hint(this, fixToApply.getDescription(), fo, range,
                    fixList, DEFAULT_PRIORITY);

            result.add(descriptor);
        }

        return;
    }

    public boolean appliesTo(RuleContext context) {
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

    private class AddImportFix implements HintFix {

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
