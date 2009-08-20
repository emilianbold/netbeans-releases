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

package org.netbeans.modules.cnd.highlight.error;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.class, position=20)
public class IncludeErrorProvider extends CsmErrorProvider {

    public String getName() {
        return "include-errors"; //NOI18N
    }

    private static abstract class OffsetableErrorInfo implements CsmErrorInfo {

        private int start;
        private int end;
        private CsmErrorInfo.Severity severity;
        
        public OffsetableErrorInfo(CsmOffsetable offsetable, CsmErrorInfo.Severity severity) {
            start = offsetable.getStartOffset();
            end = offsetable.getEndOffset();
            this.severity = severity;
        }

        public int getEndOffset() {
            return end;
        }

        public int getStartOffset() {
            return start;
        }

        public Severity getSeverity() {
            return severity;
        }
    }
    
    private static class IncludeErrorInfo extends OffsetableErrorInfo implements CsmErrorInfo {

        private String message;
        
        public IncludeErrorInfo(CsmInclude incl) {
            super(incl, Severity.ERROR);
            this.message = NbBundle.getMessage(IncludeErrorProvider.class, "HighlightProvider_IncludeMissed", getIncludeText(incl));
        }

        public String getMessage() {
            return message;
        }
        
        private static String getIncludeText(CsmInclude incl){
            if (incl.isSystem()){
                return "<"+incl.getIncludeName()+">"; // NOI18N
            }
            return "\""+incl.getIncludeName()+"\""; // NOI18N
        }
        
    }

    private static class IncludeWarningInfo extends OffsetableErrorInfo implements CsmErrorInfo {

        private String message;

        public IncludeWarningInfo(CsmInclude incl) {
            super(incl, Severity.WARNING);
            this.message = NbBundle.getMessage(IncludeErrorProvider.class, "HighlightProvider_IncludeMissedWarning", getIncludeText(incl));
        }

        public String getMessage() {
            return message;
        }

        private static String getIncludeText(CsmInclude incl){
            if (incl.isSystem()){
                return "<"+incl.getIncludeName()+">"; // NOI18N
            }
            return "\""+incl.getIncludeName()+"\""; // NOI18N
        }

    }

    private static class ErrorDirectiveInfo extends OffsetableErrorInfo implements CsmErrorInfo {

        private String message;

        public ErrorDirectiveInfo(CsmErrorDirective error) {
            super(error, Severity.ERROR);
            this.message = NbBundle.getMessage(IncludeErrorProvider.class, "HighlightProvider_ErrorDirective");
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    protected void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        for(CsmInclude incl : CsmFileInfoQuery.getDefault().getBrokenIncludes(file)) {
            if (request.isCancelled()) {
                break;
            }
            if (incl.getIncludeFile() == null) {
                response.addError(new IncludeErrorInfo(incl));
            }
        }
        for (CsmErrorDirective error : file.getErrors()) {
            response.addError(new ErrorDirectiveInfo(error));
        }
        Collection<CsmFile> visited = new HashSet<CsmFile>();
        for (CsmInclude incl : file.getIncludes()) {
            CsmFile newFile = incl.getIncludeFile();
            if (newFile != null && hasBrokenIncludes(newFile, visited)) {
                response.addError(new IncludeWarningInfo(incl));
            }
        }
    }

    private boolean hasBrokenIncludes(CsmFile file, Collection<CsmFile> visited) {
        if (visited.contains(file)) {
            return false;
        }
        visited.add(file);
        if (!CsmFileInfoQuery.getDefault().getBrokenIncludes(file).isEmpty()) {
            return true;
        }
        for (CsmInclude incl : file.getIncludes()) {
            CsmFile newFile = incl.getIncludeFile();
            if (newFile != null && hasBrokenIncludes(newFile, visited)) {
                return true;
            }
        }
        return false;
    }

}
