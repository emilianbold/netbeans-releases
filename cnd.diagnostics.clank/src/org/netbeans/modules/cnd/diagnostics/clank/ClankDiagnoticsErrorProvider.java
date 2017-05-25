/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.diagnostics.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.clang.frontend.InputKind;
import org.clang.frontend.LangStandard;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankDiagnosticEnhancedFix;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.clang.tools.services.ClankDiagnosticResponse;
import org.clang.tools.services.ClankDiagnosticServices;
import org.clang.tools.services.ClankRunDiagnosticsSettings;
import org.clang.tools.services.spi.ClankFileSystemProvider;
import org.clang.tools.services.support.DataBaseEntryBuilder;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author masha
 */
@ServiceProviders({
    //@ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1400),
    @ServiceProvider(service = CsmErrorProvider.class, position = 3000)
    ,   
    @ServiceProvider(service = CodeAuditProvider.class, position = 3000)
})
public class ClankDiagnoticsErrorProvider extends CsmErrorProvider implements CodeAuditProvider {
    //, AbstractCustomizerProvider { 

    private static final Logger LOG = Logger.getLogger("cnd.diagnostics.clank.support"); //NOI18N
    private Collection<CodeAudit> audits;
    public static final String NAME = "Clank Diagnostics"; //NOI18N
    private final AuditPreferences myPreferences;

    public static CsmErrorProvider getInstance() {
        for (CsmErrorProvider provider : Lookup.getDefault().lookupAll(CsmErrorProvider.class)) {
            if (NAME.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }

    public ClankDiagnoticsErrorProvider() {
        myPreferences = new AuditPreferences(AuditPreferences.AUDIT_PREFERENCES_ROOT.node(NAME));
    }

    /*package*/ ClankDiagnoticsErrorProvider(Preferences preferences) {
        try {
            if (preferences.nodeExists(NAME)) {
                preferences = preferences.node(NAME);
            }
        } catch (BackingStoreException ex) {
        }
        if (preferences.absolutePath().endsWith("/" + NAME)) { //NOI18N
            myPreferences = new AuditPreferences(preferences);
        } else {
            myPreferences = new AuditPreferences(preferences.node(NAME));
        }
    }

    @Override
    protected boolean validate(Request request) {
        CsmFile file = request.getFile();
        if (file == null) {
            return false;
        }
        //if (file.isHeaderFile()) {
        //    return false;
        //}
        for (CodeAudit audit : getAudits()) {
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
        return NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_NAME"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION"); //NOI18N
    }

    @Override
    public String getMimeType() {
        return MIMENames.SOURCES_MIME_TYPE;
    }

    @Override
    public boolean isSupportedEvent(EditorEvent kind) {
        return kind == EditorEvent.FileBased;
    }

    @Override
    protected void doGetErrors(Request request, final Response response) {
        final CsmFile file = request.getFile();
        ClankCompilationDataBase.Entry entry = createEntry(file, true);

        if (request.isCancelled()) {
            return;
        };
        ClankRunDiagnosticsSettings settings = new ClankRunDiagnosticsSettings();
        settings.response = new ClankDiagnosticResponse() {
            @Override
            public void addError(final ClankDiagnosticInfo errorInfo) {
                response.addError(new ClankCsmErrorInfo(file, errorInfo));
            }

            @Override
            public void done() {

            }
        };
        settings.showAllWarning = clankShowAllWarning.isEnabled();
        settings.turnOnAnalysis = clankStaticAnalyzer.isEnabled();
        try {
            ClankDiagnosticServices.verify(entry, settings);
        } catch (Throwable ex) {
            //catch anything

        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static ClankCompilationDataBase.Entry createEntry(CsmFile file, boolean useURL) {
        NativeFileItem nfi = CsmFileInfoQuery.getDefault().getNativeFileItem(file);
        CharSequence mainFile = useURL ? CndFileSystemProvider.toUrl(FSPath.toFSPath(nfi.getFileObject())) : nfi.getAbsolutePath();
        DataBaseEntryBuilder builder = new DataBaseEntryBuilder(mainFile, null);

        builder.setLang(getLang(nfi)).setLangStd(getLangStd(nfi));

        // -I or -F
        for (org.netbeans.modules.cnd.api.project.IncludePath incPath : nfi.getUserIncludePaths()) {
            FileObject fileObject = incPath.getFSPath().getFileObject();
            if (fileObject != null && fileObject.isFolder()) {
                CharSequence path = useURL ? incPath.getFSPath().getURL() : incPath.getFSPath().getPath();
                builder.addUserIncludePath(path, incPath.isFramework(), incPath.ignoreSysRoot());
            }
        }
        // -isystem
        for (org.netbeans.modules.cnd.api.project.IncludePath incPath : nfi.getSystemIncludePaths()) {
            FileObject fileObject = incPath.getFSPath().getFileObject();
            if (fileObject != null && fileObject.isFolder()) {
                CharSequence path = useURL ? incPath.getFSPath().getURL() : incPath.getFSPath().getPath();
                builder.addPredefinedSystemIncludePath(path, incPath.isFramework(), incPath.ignoreSysRoot());
            }
        }

        // system pre-included headers
        for (FSPath fSPath : nfi.getSystemIncludeHeaders()) {
            FileObject fileObject = fSPath.getFileObject();
            if (fileObject != null && fileObject.isData()) {
                String path = useURL ? fSPath.getURL().toString() : fSPath.getPath();
                builder.addIncFile(path);
            }
        }

        // handle -include
        for (FSPath fSPath : nfi.getIncludeFiles()) {
            FileObject fileObject = fSPath.getFileObject();
            if (fileObject != null && fileObject.isData()) {
                String path = useURL ? fSPath.getURL().toString() : fSPath.getPath();
                builder.addIncFile(path);
            }
        }

        // -D
        for (String macro : nfi.getSystemMacroDefinitions()) {
            builder.addPredefinedSystemMacroDef(macro);
        }
        for (String macro : nfi.getUserMacroDefinitions()) {
            builder.addUserMacroDef(macro);
        }
        builder.setFileSystem(ClankFileSystemProvider.getDefault().getFileSystem());
        try {
            if (CndFileSystemProvider.isRemote(file.getFileObject().getFileSystem())) {
                CharSequence prefix = CndFileSystemProvider.toUrl(file.getFileObject().getFileSystem(), "/"); //NOI18N
                builder.setAbsPathLookupPrefix(prefix);
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        return builder.createDataBaseEntry();
    }

    private static LangStandard.Kind getLangStd(NativeFileItem startEntry) throws AssertionError {
        LangStandard.Kind lang_std = LangStandard.Kind.lang_unspecified;
        switch (startEntry.getLanguageFlavor()) {
            case DEFAULT:
            case UNKNOWN:
                break;
            case C:
                break;
            case C89:
                lang_std = LangStandard.Kind.lang_gnu89;
                break;
            case C99:
                lang_std = LangStandard.Kind.lang_gnu99;
                break;
            case CPP98:
                // we don't have flavor for C++98 in APT, but C++03 is used in fact
                lang_std = LangStandard.Kind.lang_cxx03;
                break;
            case CPP11:
                lang_std = LangStandard.Kind.lang_gnucxx11;
                break;
            case C11:
                lang_std = LangStandard.Kind.lang_gnu11;
                break;
            case CPP14:
                // FIXME
                lang_std = LangStandard.Kind.lang_gnucxx14;
                break;
            case F77:
            case F90:
            case F95:
            default:
                throw new AssertionError(startEntry.getLanguageFlavor().name());
        }
        return lang_std;
    }

    private static InputKind getLang(NativeFileItem startEntry) throws AssertionError {
        InputKind lang = InputKind.IK_None;
        switch (startEntry.getLanguage()) {
            case C:
            case C_HEADER:
                lang = InputKind.IK_C;
                break;
            case CPP:
                lang = InputKind.IK_CXX;
                break;
            case FORTRAN:
            case OTHER:
            default:
                throw new AssertionError(startEntry.getLanguage().name());
        }
        return lang;
    }

    @Override
    public synchronized Collection<CodeAudit> getAudits() {
        if (audits == null || audits.isEmpty()) {
            audits = new ArrayList<>();
            audits.add(clankShowAllWarning);
            audits.add(clankStaticAnalyzer);
        }
        return audits;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private final ClankDiagnosticAudit clankShowAllWarning
            = new ClankDiagnosticAudit("clank.diagnostic.show.all.warnings", "Show All Warning", "Show All Warning");//NOI18N
    private final ClankDiagnosticAudit clankStaticAnalyzer
            = new ClankDiagnosticAudit("clank.diagnostic.static.analyzer", "Static Analyzer", "Static Analyzer");//NOI18N    

    @Override
    public AuditPreferences getPreferences() {
        return myPreferences;
    }

//    @Override
//    public JComponent createComponent(Preferences context) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    private class ClankDiagnosticAudit implements CodeAudit {

        private final String id;
        private final String name;
        private final String description;
        //private final AuditPreferences myPreferences;
        //private static final String CLANK_SHOW_ALL_WARNINGS = "clank.diagnostic.show.all.warnings";

        private ClankDiagnosticAudit(String id, String name, String description) {
            //myPreferences = new AuditPreferences()
            this.id = id;
            this.name = name;
            this.description = description;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public boolean isEnabled() {
            String defValue = getDefaultEnabled() ? "true" : "false"; //NOI18N
            return !"false".equals(getPreferences().get(getID(), "enabled", defValue)); //NOI18N
        }

        @Override
        public boolean getDefaultEnabled() {
            return false;
        }

        @Override
        public String minimalSeverity() {
            return "hint";
        }

        @Override
        public String getDefaultSeverity() {
            return "hint";
        }

        @Override
        public String getKind() {
            return "inspection";
        }

        @Override
        public AuditPreferences getPreferences() {
            return myPreferences;
        }

    }

// @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1600)
//    public static final class ClankDiagnosticFixProvider extends CsmErrorInfoHintProvider {
//
//        @Override
//        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
//            alreadyFound.addAll(createFixes(info));
//            return alreadyFound;
//        }
//    }
//
//    private static List<? extends Fix> createFixes(CsmErrorInfo info) {
//        if (info instanceof ClankDiagnosticInfo) {
//            ClankDiagnosticInfo clankInfo = (ClankDiagnosticInfo) info;
//            //List<Replacement> replacements = mei.getDiagnostics().getReplacements();
////            if (!replacements.isEmpty()) {
////                return Collections.singletonList(new ModernizeFix(replacements, mei.getId()));
////            }
//            return clankInfo.getFix() == null ? Collections.EMPTY_LIST : Collections.singletonList(clankInfo.getFix());
//        }
//        return Collections.EMPTY_LIST;
//    }  
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 9200)
    public final static class ClankHintProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof ClankCsmErrorInfo) {
                final ClankDiagnosticInfo fix = ((ClankCsmErrorInfo) info).getDelegate();
                for (ClankDiagnosticEnhancedFix nextElement : fix.fixes()) {
                    try {
                        EnhancedFixImpl fixImpl = new EnhancedFixImpl(((ClankCsmErrorInfo) info).getCsmFile(), nextElement);
                        alreadyFound.add(fixImpl);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }
            return alreadyFound;
        }
    }

    /*package*/ static class EnhancedFixImpl implements EnhancedFix {

        private final CsmFile file;
        private final Position insertStartPosition;
        private final Position insertEndPosition;
        private final Position removeStartPosition;
        private final Position removeEndPosition;
        private final String textToInsert;
        private final String text;

        EnhancedFixImpl(CsmFile csmFile, ClankDiagnosticEnhancedFix clankFix) throws Exception {
            this.file = csmFile;
            textToInsert = clankFix.getInsertionText();
            text = clankFix.getText();
            Document document = CsmUtilities.getDocument(file);
            insertStartPosition = NbDocument.createPosition(document, clankFix.getInsertStartOffset(), Position.Bias.Forward);
            insertEndPosition = NbDocument.createPosition(document, clankFix.getInsertEndOffset(), Position.Bias.Forward);
            removeStartPosition = NbDocument.createPosition(document, clankFix.getRemoveStartOffset(), Position.Bias.Forward);
            removeEndPosition = NbDocument.createPosition(document, clankFix.getRemoveEndOffset(), Position.Bias.Forward);
        }

        @Override
        public CharSequence getSortText() {
            return text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public ChangeInfo implement() throws Exception {

            try {
                Document document = CsmUtilities.getDocument(file);
                //document.remove(0, 0);
                //check insertation range first
                if (insertStartPosition.getOffset() == 0 && insertEndPosition.getOffset() == 0) {
                    final int startPos = removeStartPosition.getOffset();
                    //will do replace
                    //remove end insert
                    document.remove(startPos,
                            removeEndPosition.getOffset() - startPos + 1);
                    document.insertString(startPos, textToInsert, null);
                } else {
                    final int startPos = insertStartPosition.getOffset();
                    document.insertString(startPos, textToInsert, null);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            return null;
        }

    }

    /*package*/ class ClankCsmErrorInfo implements CsmErrorInfo {

        private final ClankDiagnosticInfo errorInfo;
        private final CsmFile csmFile;

        ClankCsmErrorInfo(CsmFile csmFile, ClankDiagnosticInfo info) {
            this.errorInfo = info;
            this.csmFile = csmFile;
        }

        CsmFile getCsmFile() {
            return csmFile;
        }

        @Override
        public String getMessage() {
            return errorInfo.getMessage();
        }

        @Override
        public CsmErrorInfo.Severity getSeverity() {
            return errorInfo.getSeverity() == ClankDiagnosticInfo.Severity.ERROR
                    ? CsmErrorInfo.Severity.ERROR : CsmErrorInfo.Severity.WARNING;
        }

        @Override
        public int getStartOffset() {
            return errorInfo.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            //return (int) CsmFileInfoQuery.getDefault().getOffset(file, errorInfo.getLine(), errorInfo.getColumn() + 1);
            return errorInfo.getEndOffset();
        }

        /*package*/ ClankDiagnosticInfo getDelegate() {
            return errorInfo;
        }
    }

}
