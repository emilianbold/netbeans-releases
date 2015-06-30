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
package org.netbeans.modules.cnd.highlight.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditListFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Danila Sergeyev
 */
public class UsingUnsafeFunctions extends AbstractCodeAudit {
    private final String functionName;
    private final String header;
    
    private UsingUnsafeFunctions(String functionName, String header, String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
        this.functionName = functionName;
        this.header = header;
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }
    
    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            
            for (CsmReference ref : CsmReferenceResolver.getDefault().getReferences(file)) {
                if (CsmKindUtilities.isFunction(ref.getReferencedObject())) {
                    CsmFunction function = (CsmFunction) ref.getReferencedObject();
                    if (function.getName().toString().equals(functionName)) {
                        CsmFile srcFile = function.getContainingFile();
                        for (CsmInclude include : CsmFileInfoQuery.getDefault().getIncludeStack(srcFile)) {
                            if (include.getIncludeName().toString().equals(header)) {
                                CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                                if (response instanceof AnalyzerResponse) {
                                    ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                                        new ErrorInfoImpl(SecurityCheckProvider.NAME, getID(), getName()+"\n"+getDescription(), severity, ref.getStartOffset(), ref.getEndOffset()));  // NOI18N
                                } else {
                                    response.addError(new ErrorInfoImpl(SecurityCheckProvider.NAME, getID(), getDescription(), severity, ref.getStartOffset(), ref.getEndOffset()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @ServiceProvider(path = CodeAuditListFactory.REGISTRATION_PATH+SecurityCheckProvider.NAME, service = CodeAuditListFactory.class, position = 1300)
    public static final class SecurityAuditFactory implements CodeAuditListFactory {
        @Override
        public Collection<AbstractCodeAudit> create(AuditPreferences preferences) {
            Checks avoid = Checks.getInstance(Checks.Level.AVOID);
            Checks unsafe = Checks.getInstance(Checks.Level.UNSAFE);
            Map<String, String> avoidFunctions = avoid.getFunctions();
            Map<String, String> unsafeFunctions = unsafe.getFunctions();
            ArrayList<AbstractCodeAudit> result = new ArrayList<>(avoidFunctions.size()+unsafeFunctions.size());
            for (String key : avoidFunctions.keySet()) {
                String id = NbBundle.getMessage(UsingUnsafeFunctions.class, "UsingUnsafeFunctions.name", key); // NOI18N
                String description = NbBundle.getMessage(UsingUnsafeFunctions.class, "UsingUnsafeFunctions."+key+".description"); // NOI18N
                result.add(new UsingUnsafeFunctions(key, avoidFunctions.get(key), id, id, description, "warning", true, preferences)); // NOI18N
            }
            for (String key : unsafeFunctions.keySet()) {
                String id = NbBundle.getMessage(UsingUnsafeFunctions.class, "UsingUnsafeFunctions.name", key); // NOI18N
                String description = NbBundle.getMessage(UsingUnsafeFunctions.class, "UsingUnsafeFunctions."+key+".description"); // NOI18N
                result.add(new UsingUnsafeFunctions(key, unsafeFunctions.get(key), id, id, description, "error", true, preferences)); // NOI18N
            }
            return result;
        }
    }
    
}
