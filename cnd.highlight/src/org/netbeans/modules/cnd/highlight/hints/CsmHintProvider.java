/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Alexander Simon
 */
@ServiceProviders({
    //@ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1400),
    @ServiceProvider(service = CsmErrorProvider.class, position = 1000),
    @ServiceProvider(service = CodeAuditProvider.class, position = 1000)
})
public final class CsmHintProvider extends CsmErrorProvider implements CodeAuditProvider {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.highlight.hints"); //NOI18N
    private Collection<CodeAudit> audits;
    public static final String NAME = "Model"; //NOI18N
    private final AuditPreferences myPreferences;

    public static CsmErrorProvider getInstance() {
        for(CsmErrorProvider provider : Lookup.getDefault().lookupAll(CsmErrorProvider.class)) {
            if (NAME.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }
    
    public CsmHintProvider() {
         myPreferences = new AuditPreferences(AuditPreferences.AUDIT_PREFERENCES_ROOT.node(NAME));
    }
    
    CsmHintProvider(Preferences preferences) {        
        try {
            if (preferences.nodeExists(NAME)) {
                preferences = preferences.node(NAME);
            }
        } catch (BackingStoreException ex) {
        }   
        if (preferences.absolutePath().endsWith("/"+NAME)) { //NOI18N
            myPreferences = new AuditPreferences(preferences);
        } else {
            myPreferences = new AuditPreferences(preferences.node(NAME));
        }
    }

    @Override
    protected boolean validate(Request request) {
        CsmFile file = request.getFile();
        if (file == null){
            return false;
        }
        for(CodeAudit audit : getAudits()) {
            if (audit.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasHintControlPanel() {
        return true;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CsmHintProvider.class, "General_NAME"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(CsmHintProvider.class, "General_DESCRIPTION"); //NOI18N
    }

    @Override
    public Set<EditorEvent> supportedEvents() {
        return EnumSet.<EditorEvent>of(EditorEvent.FileBased);
    }
    
    @Override
    protected void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            CsmCacheManager.enter();
            try {
                for(CodeAudit audit : getAudits()) {
                    if (request.isCancelled()) {
                        return;
                    }
                    ((AbstractCodeAudit)audit).doGetErrors(request, response);
                }
            } finally {
                CsmCacheManager.leave();
            }
        }
    }

    @Override
    public synchronized Collection<CodeAudit> getAudits() {
        if (audits == null) {
            List<CodeAudit> res = new ArrayList<CodeAudit>();
            for(CodeAuditFactory factory : Lookups.forPath(CodeAuditFactory.REGISTRATION_PATH).lookupAll(CodeAuditFactory.class)) {
                res.add(factory.create(myPreferences));
            }
            audits = res;
        }
        return audits;
    }

    @Override
    public AuditPreferences getPreferences() {
        return myPreferences;
    }

    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 9100)
    public final static class FixProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof ErrorInfoImpl) {
                alreadyFound.add(new DisableHintFix());
            }
            return alreadyFound;
        }
    }
    
    
    private static class DisableHintFix implements EnhancedFix {

        DisableHintFix() {
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(CsmHintProvider.class, "DisableHint"); // NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {
            OptionsDisplayer.getDefault().open("Editor/Hints/text/x-cnd+sourcefile"); // NOI18N
            return null;
        }

        @Override
        public CharSequence getSortText() {
            //Hint opening options dialog should always be the lastest in offered list
            return Integer.toString(Integer.MAX_VALUE);
        }
    }
}
