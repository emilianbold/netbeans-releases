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
package org.netbeans.modules.cnd.highlight.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Danila Sergeyev
 */
public class InvalidFormatString extends AbstractCodeAudit {    
    private InvalidFormatString(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }
    
    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        final CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            
            Document doc_ = request.getDocument();
            if (doc_ == null) {
                CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
                doc_ = CsmUtilities.openDocument(ces);
            }
            final Document doc = doc_;
            final AtomicReference<List<FormatError>> result = new AtomicReference<>();
            Runnable runnable = new Runnable () {
                @Override
                public void run() {
                    TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                    if (docTokenSequence == null) {
                        return;
                    }
                    docTokenSequence.moveStart();
                    
                    final CsmReferenceResolver rr = CsmReferenceResolver.getDefault();
                    State state = State.DEFAULT;
                    boolean formatFlag = false;  // detect was format string already processed
                    List<FormatError> errorsList = new LinkedList<>();
                    StringBuilder paramBuf = new StringBuilder();
                    ArrayList<FormatInfo> info = new ArrayList<>();
                    ArrayList<Parameter> params = new ArrayList<>();
                    int bracketsCounter = 0;
                    while (docTokenSequence.moveNext()) {
                        if (docTokenSequence.token().id() instanceof CppTokenId) {
                            CppTokenId tokenId = (CppTokenId) docTokenSequence.token().id();
                            if (tokenId.equals(CppTokenId.IDENTIFIER) && state == State.DEFAULT) {
                                CsmReference reference = rr.findReference(file, doc, docTokenSequence.offset());
                                CsmObject object = reference.getReferencedObject();
                                if (isFormattedPrintFunction(object)) {
                                    state = State.START;
                                }
                            } else if (tokenId.equals(CppTokenId.LPAREN) && state == State.START) {
                                state = State.IN_PARAM;
                            } else if (tokenId.equals(CppTokenId.LPAREN) && state == State.IN_PARAM) {
                                state = State.IN_PARAM_BRACKET;
                                bracketsCounter++;
                                if (formatFlag) {
                                    paramBuf.append(docTokenSequence.token().text());
                                }
                            } else if (tokenId.equals(CppTokenId.LPAREN) && state == State.IN_PARAM_BRACKET) {
                                bracketsCounter++;
                                if (formatFlag) {
                                    paramBuf.append(docTokenSequence.token().text());
                                }
                            } else if (tokenId.equals(CppTokenId.RPAREN) && state == State.IN_PARAM_BRACKET) {
                                bracketsCounter--;
                                if (bracketsCounter == 0) {
                                    state = State.IN_PARAM;
                                }
                                if (formatFlag) {
                                    paramBuf.append(docTokenSequence.token().text());
                                }
                            } else if (tokenId.equals(CppTokenId.RPAREN) && state == State.IN_PARAM) {
                                if (paramBuf.length() > 0) {
                                    params.add(new Parameter(paramBuf.toString(), docTokenSequence.offset()));
                                }
                                paramBuf = new StringBuilder();
                                int line = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, docTokenSequence.offset())[0];
                                errorsList.addAll(validateParameters(file, info, params, line));
                                state = State.DEFAULT;
                                formatFlag = false;
                            } else if (state == State.IN_PARAM && tokenId.equals(CppTokenId.STRING_LITERAL) && !formatFlag) {
                                formatFlag = true;
                                if (!params.isEmpty()) { 
                                    params = new ArrayList<>();
                                }
                                info = processFormatString(docTokenSequence.token().text().toString());
                                int line = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, docTokenSequence.offset())[0];
                            } else if (state == State.IN_PARAM && formatFlag && tokenId.equals(CppTokenId.COMMA)) {
                                if (paramBuf.length() > 0) {
                                    params.add(new Parameter(paramBuf.toString(), docTokenSequence.offset()));
                                }
                                paramBuf = new StringBuilder();
                            } else if ((state == State.IN_PARAM || state == State.IN_PARAM_BRACKET) 
                                    && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)
                                    && formatFlag) {
                                paramBuf.append(docTokenSequence.token().text());
                            }
                        }
                    }
                    result.set(errorsList);
                }
            };
            
            FutureTask<AtomicReference<List<FormatError>>> task = new FutureTask<>(runnable, result);
            doc.render(task);
            
            try {
                List<FormatError> errors = task.get().get();
                for (FormatError error : errors) {
                    int startOffset = (int) CsmFileInfoQuery.getDefault().getOffset(file, error.getLine(), 1);
                    int endOffset = (int) CsmFileInfoQuery.getDefault().getOffset(file, error.getLine()+1, 1) - 1;
                    CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                            new ErrorInfoImpl(CsmHintProvider.NAME, getID(), getMessageForError(error), severity, startOffset, endOffset));  // NOI18N
                    } else {
                        response.addError(new ErrorInfoImpl(CsmHintProvider.NAME, getID(), getMessageForError(error), severity, startOffset, endOffset));  // NOI18N
                    }
                }
            } catch (InterruptedException | CancellationException | ExecutionException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    private List<FormatError> validateParameters(CsmFile file, ArrayList<FormatInfo> formatInfoList, ArrayList<Parameter> parameters, int line) {
        List<FormatError> result = new LinkedList<>();
        if (getParametersFromFormat(formatInfoList) != parameters.size()) {
            result.add(new FormatError(FormatErrorType.ARGS, null, null, line));
        }
        for (int i = 0, limit = formatInfoList.size(), pIndex = 0; i < limit; i++) {
            FormatInfo info = formatInfoList.get(i);
            List<FormatError> list = info.validateFormat(line);
            if (list.isEmpty() && !info.specifier().equals("%")) {  // NOI18N
                String wType = null;
                String pType = null;
                String type = null;
                if (pIndex < parameters.size() && info.hasWidthWildcard()) {
                    wType = getParameterType(parameters.get(pIndex).getValue(), parameters.get(pIndex).getOffset(), file);
                    if (wType != null && !wType.equals("int")) {  // NOI18N
                        result.add(new FormatError(FormatErrorType.TYPE_WILDCARD, "Width", null, line));  // NOI18N
                    }
                    pIndex++;
                }
                if (pIndex < parameters.size() && info.hasPrecisionWildcard()) {
                    pType = getParameterType(parameters.get(pIndex).getValue(), parameters.get(pIndex).getOffset(), file);
                    if (pType != null && !pType.equals("int")) {  // NOI18N
                        result.add(new FormatError(FormatErrorType.TYPE_WILDCARD, "Precision", null, line));  // NOI18N
                    }
                    pIndex++;
                }
                if (pIndex < parameters.size()) {
                    type = getParameterType(parameters.get(pIndex).getValue(), parameters.get(pIndex).getOffset(), file);
                    if (type != null) {
                        String fType = info.getFullType();
                        List<String> validFlags = typeToFormat(type);
                        if (!validFlags.contains(fType)) {
                            result.add(new FormatError(FormatErrorType.TYPE_MISMATCH, type, fType, line));
                        }
                    }
                }
                pIndex++;
            } else {
                result.addAll(list);
                pIndex++;
            }
        }
        return result;
    }
    
    private String getParameterType(String value, int offset, CsmFile file) {
        DummyResolvedTypeHandler handler = new DummyResolvedTypeHandler();
        CsmExpressionResolver.resolveType(value
                                         ,file
                                         ,offset
                                         ,null
                                         ,handler);
        
        if (handler.type != null) {
            return handler.type.getCanonicalText().toString().replace("const", "");  // NOI18N
        }
        return null;
    }
    
    // take const modifier into account
    private List<String> typeToFormat(String type) {
        if (type.contains("*")) {                           // NOI18N
            if (type.contains("void")) {                    // NOI18N
                return Collections.singletonList("p");      // NOI18N
            } else if (type.contains("char")) {             // NOI18N
                return Arrays.asList("p", "hhn", "s");      // NOI18N
            } else if (type.contains("wchar_t")) {          // NOI18N
                return Arrays.asList("p", "s", "ls", "S");  // NOI18N
            } else if (type.contains("short")) {            // NOI18N
                return Arrays.asList("p", "hn");            // NOI18N
            } else if (type.contains("int")) {              // NOI18N
                return Arrays.asList("p", "n");             // NOI18N
            } else if (type.contains("long long")) {        // NOI18N
                return Arrays.asList("p", "lln");           // NOI18N
            } else if (type.contains("long")) {             // NOI18N
                return Arrays.asList("p", "ln");            // NOI18N
            } else if (type.contains("intmax_t")) {         // NOI18N
                return Arrays.asList("p", "jn");            // NOI18N
            } else if (type.contains("size_t")) {           // NOI18N
                return Arrays.asList("p", "zn");            // NOI18N
            } else if (type.contains("ptrdiff_t")) {        // NOI18N
                return Arrays.asList("p", "tn");            // NOI18N
            }
        } else if (type.startsWith("unsigned")) {                  // NOI18N
            if (type.contains("char")) {                           // NOI18N
                return Arrays.asList("hho", "hhu", "hhx", "hhX");  // NOI18N
            } else if (type.contains("short")) {                   // NOI18N
                return Arrays.asList("ho", "hu", "hx", "hX");      // NOI18N
            } else if (type.contains("long long")) {               // NOI18N
                return Arrays.asList("llo", "llu", "llx", "llX");  // NOI18N
            } else if (type.contains("long")) {                    // NOI18N
                return Arrays.asList("lo", "lu", "lx", "lX");      // NOI18N
            } else if (type.contains("int")) {                     // NOI18N
                return Arrays.asList("o", "u", "x", "X");          // NOI18N
            }
        } else {
            if (type.contains("signed char")) {                           // NOI18N
                return Arrays.asList("hhd", "hhi");                       // NOI18N
            } else if (type.contains("short")) {                          // NOI18N
                return Arrays.asList("hd", "hi");                         // NOI18N
            } else if (type.contains("long long")) {                      // NOI18N
                return Arrays.asList("lld", "lli");                       // NOI18N
            } else if (type.contains("long")) {                           // NOI18N
                return Arrays.asList("ld", "li");                         // NOI18N
            } else if (type.equals("int")) {                              // NOI18N
                return Arrays.asList("d", "i", "c");                      // NOI18N
            } else if (type.equals("intmax_t")) {                         // NOI18N
                return Arrays.asList("jd", "ji");                         // NOI18N
            } else if (type.equals("uintmax_t")) {                        // NOI18N
                return Arrays.asList("jo", "ju", "jx", "jX");             // NOI18N
            } else if (type.equals("size_t")) {                           // NOI18N
                return Arrays.asList("zd", "zi","zo", "zu", "zx", "zX");  // NOI18N
            } else if (type.equals("ptrdiff_t")) {                        // NOI18N
                return Arrays.asList("td", "ti","to", "tu", "tx", "tX");  // NOI18N
            } else if (type.equals("wint_t")) {                           // NOI18N
                return Arrays.asList("c", "lc", "C");                     // NOI18N
            } else if (type.equals("float")) {  // NOI18N
                return Collections.EMPTY_LIST;
            } else if (type.equals("double")) {  // NOI18N
                return Arrays.asList("f", "lf", "llf", "F", "lF", "llF",   // NOI18N
                                     "e", "le", "lle", "E", "lE", "llE",   // NOI18N
                                     "g", "lg", "llg", "G", "lG", "llG",   // NOI18N
                                     "a", "la", "lla", "A", "lA", "llA");  // NOI18N
            } else if (type.equals("long double")) {                                   // NOI18N
                return Arrays.asList("f", "lf", "llf", "Lf", "F", "lF", "llF", "LF",   // NOI18N
                                     "e", "le", "lle", "Le", "E", "lE", "llE", "LE",   // NOI18N
                                     "g", "lg", "llg", "Lg", "G", "lG", "llG", "LG",   // NOI18N
                                     "a", "la", "lla", "La", "A", "lA", "llA", "LA");  // NOI18N
            }
        }
        return Collections.EMPTY_LIST;
    }
    
    private int getParametersFromFormat(Collection<FormatInfo> info) {
        int result = 0;
        for (FormatInfo i : info) {
            if (!i.specifier().equals("%")) {  // NOI18N
                result++;
            }
            if (i.hasPrecisionWildcard()) {
                result++;
            }
            if (i.hasWidthWildcard()) {
                result++;
            }
        }
        return result;
    }
    
    private String getMessageForError(FormatError error) {
        switch (error.getType()) {
            case FLAG:
                return NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.message.incompatibleFlag", error.getFlag(), error.getSpecifier()); // NOI18N
            case LENGTH:
                return NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.message.incompatibleLength", error.getFlag(), error.getSpecifier()); // NOI18N
            case TYPE_MISMATCH:
                return NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.message.type", error.getFlag(), error.getSpecifier()); // NOI18N
            case TYPE_NOTEXIST:
                return NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.message.notexist", error.getSpecifier()); // NOI18N
            case TYPE_WILDCARD:
                return NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.message.wildcard", error.getFlag()); // NOI18N
            case ARGS:
                return NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.message.argnum"); // NOI18N
        }
        return null;
    }
    
    private ArrayList<FormatInfo> processFormatString(String format) {
        ArrayList<FormatInfo> result = new ArrayList<>();
        FormatInfo info = new FormatInfo();
        ConversionState state = ConversionState.DEFAULT;
        for (int i = 0, limit = format.length(); i < limit; i++) {
            char __current_char__ = format.charAt(i);
            if (format.charAt(i) == '%' && state == ConversionState.DEFAULT) {  // NOI18N
                state = ConversionState.START;
                info = new FormatInfo();
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.charAt(i) == FormatFlag.APOSTROPHE.character()) {
                state = ConversionState.FLAGS;
                info.addFormatFlag(FormatFlag.APOSTROPHE);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.charAt(i) == FormatFlag.HASH.character()) {
                state = ConversionState.FLAGS;
                info.addFormatFlag(FormatFlag.HASH);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.charAt(i) == FormatFlag.ZERO.character()) {
                state = ConversionState.FLAGS;
                info.addFormatFlag(FormatFlag.ZERO);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.substring(i, i+1).matches("-|\\+|\\s")) { // NOI18N
                state = ConversionState.FLAGS;
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.substring(i, i+1).matches("[0-9]|\\*")) { // NOI18N
                state = ConversionState.WIDTH;
                if (format.charAt(i) == '*') { // NOI18N
                    info.setWidthWildcardFlag(true);
                }
            }  else if (state == ConversionState.WIDTH && Character.isDigit(format.charAt(i))) {
                continue;
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS || state == ConversionState.WIDTH) && format.charAt(i) == '.') {
                state = ConversionState.PRECISION;
            } else if (state == ConversionState.PRECISION && format.substring(i, i+1).matches("[0-9]|\\*")) { // NOI18N
                if (format.charAt(i) == '*') { // NOI18N
                    info.setPrecisionWildcardFlag(true);
                }
            } else if (state != ConversionState.DEFAULT) {
                if (format.substring(i, i+2).equals("hh")) { // NOI18N
                    info.setLengthFlag(LengthFlag.hh);
                    i++;
                } else if (format.charAt(i) == 'h') { // NOI18N
                    info.setLengthFlag(LengthFlag.h);
                } else if (format.charAt(i) == 'j') { // NOI18N
                    info.setLengthFlag(LengthFlag.j);
                } else if (format.charAt(i) == 'z') { // NOI18N
                    info.setLengthFlag(LengthFlag.z);
                } else if (format.charAt(i) == 't') { // NOI18N
                    info.setLengthFlag(LengthFlag.t);
                } else if (format.substring(i, i+2).equals("ll")) { // NOI18N
                    info.setLengthFlag(LengthFlag.ll);
                    i++;
                } else if (format.charAt(i) == 'l') { // NOI18N
                    info.setLengthFlag(LengthFlag.l);
                } else if (format.charAt(i) == 'L') { // NOI18N
                    info.setLengthFlag(LengthFlag.L);
                } else {
                    info.setSpecifier(String.valueOf(format.charAt(i)));
                    result.add(info);
                    state = ConversionState.DEFAULT;
                }
            }
        }
        return result;
    }
    
    // check if object is a function which accepted format string
    private static boolean isFormattedPrintFunction(CsmObject object) {
        if (CsmKindUtilities.isFunction(object)) {
            CsmFunction function = (CsmFunction) object;
            if (function.getName().toString().endsWith("printf")) {  // NOI18N
                CsmFile srcFile = function.getContainingFile();
                for (CsmInclude include : CsmFileInfoQuery.getDefault().getIncludeStack(srcFile)) {
                    if (include.getIncludeName().toString().equals("stdio.h")) {  // NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 4000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.name");  // NOI18N
            String description = NbBundle.getMessage(InvalidFormatString.class, "InvalidFormatString.description");  // NOI18N
            return new InvalidFormatString(id, id, description, "error", true, preferences);  // NOI18N
        }
    }
    
    private static enum State {
        DEFAULT,
        START,
        IN_PARAM,
        IN_PARAM_BRACKET
    }
    
    /*
     * Format specification fields:
     *     %[flags][min field width][precision][length]conversion specifier
     * where:
     *     flags: #,0,-,+, ,'
     *     length: h,hh,l,ll,j,z,t,L
     *     conversion specifier: d,i,o,u,x,X,f,F,e,E,g,G,a,A,c,s,p,n,C,S,%
     */
    private static enum ConversionState {
        DEFAULT,
        START,
        FLAGS,
        WIDTH,
        PRECISION,
        CONVERSION
    }
    
    private static enum FormatFlag {
        APOSTROPHE('\'', 0b110100110011110000000),  // NOI18N
        MINUS('-', 0b111111111111111111111),        // NOI18N
        PLUS('+', 0b111111111111111111111),         // NOI18N
        SPACE(' ', 0b111111111111111111111),        // NOI18N
        HASH('#', 0b001011111111110000000),         // NOI18N
        ZERO('0', 0b111111111111110000000);         // NOI18N
        
        private final char flag;
        private final int mask;
        
        FormatFlag(char flag, int mask) {
            this.flag = flag;
            this.mask = mask;
        }
        
        public char character() {
            return flag;
        }
        
        public int getMask() {
            return mask;
        }
    }
    
    private static enum LengthFlag {
        h("h", 0b111111000000000001000),    // NOI18N
        hh("hh", 0b111111000000000001000),  // NOI18N
        l("l", 0b111111111111111101000),    // NOI18N
        ll("ll", 0b111111111111110001000),  // NOI18N
        j("j", 0b111111000000000001000),    // NOI18N
        z("z", 0b111111000000000001000),    // NOI18N
        t("t", 0b111111000000000001000),    // NOI18N
        L("L", 0b000000111111110000000);    // NOI18N
        
        private final String flag;
        private final int mask;
        
        LengthFlag(String flag, int mask) {
            this.flag = flag;
            this.mask = mask;
        }
        
        @Override
        public String toString() {
            return flag;
        }
        
        public int getMask() {
            return mask;
        }
    }
    
    private static enum FormatErrorType {
        FLAG,           // usage of a flag character that is incompatible with the conversion specifier
        LENGTH,         // usage of a length modifier that is incompatible with the conversion specifier
        TYPE_MISMATCH,  // mismatching the argument type and conversion specifier
        TYPE_WILDCARD,  // type of width or pressision not int
        TYPE_NOTEXIST,  // wrong conversion specifier
        ARGS            // incorrect number of arguments for the format string
    }
    
    private static class FormatError {
        private final FormatErrorType type;
        private final String flag;
        private final String specifier;
        private final int line;
        
        public FormatError(FormatErrorType type, String flag, String specifier, int line) {
            this.type = type;
            this.flag = flag;
            this.specifier = specifier;
            this.line = line;
        }

        public FormatErrorType getType() {
            return type;
        }

        public int getLine() {
            return line;
        }

        public String getFlag() {
            return flag;
        }

        public String getSpecifier() {
            return specifier;
        }
    }
    
    private static class FormatInfo {
        private static final List<String> conversionCharacters = Arrays.asList("d","i","o","u","x","X","f","F","e","E","g","G","a","A","c","s","p","n","C","S","%"); // NOI18N
        private final List<FormatFlag> formatFlags;
        private LengthFlag lengthFlag;
        private String specifier;
        private boolean hasWidthWildcard = false;
        private boolean hasPrecisionWildcard = false;

        public boolean hasWidthWildcard() {
            return hasWidthWildcard;
        }

        public boolean hasPrecisionWildcard() {
            return hasPrecisionWildcard;
        }
        
        public FormatInfo() {
            formatFlags = new LinkedList<>();
        }

        public void setSpecifier(String specifier) {
            this.specifier = specifier;
        }

        public void setLengthFlag(LengthFlag lengthFlag) {
            this.lengthFlag = lengthFlag;
        }
        
        public void addFormatFlag(FormatFlag flag) {
            formatFlags.add(flag);
        }

        public void setWidthWildcardFlag(boolean flag) {
            hasWidthWildcard = flag;
        }

        public void setPrecisionWildcardFlag(boolean flag) {
            hasPrecisionWildcard = flag;
        }
        
        public String specifier() {
            return specifier;
        }
        
        public String getFullType() {
            StringBuilder result = new StringBuilder();
            if (lengthFlag != null) {
                result.append(lengthFlag);
            }
            result.append(specifier);
            return result.toString();
        }
        
        public List<FormatError> validateFormat(int line) {
            if (!conversionCharacters.contains(specifier)) {
                return Collections.singletonList(new FormatError(FormatErrorType.TYPE_NOTEXIST, null, specifier, line));
            }
            List<FormatError> result = new LinkedList<>();
            
            // validate format flags
            for (FormatFlag flag : formatFlags) {
                int filter = 0b100000000000000000000;
                for (int i = 0, limit = conversionCharacters.size(); i < limit; i++) {
                    if ((flag.getMask() & filter) == 0 && specifier.equals(conversionCharacters.get(i))) {
                        result.add(new FormatError(FormatErrorType.FLAG, String.valueOf(flag.character()), specifier, line));
                        break;
                    }
                    filter >>= 1;
                }
            }
            
            // validate length flags
            if (lengthFlag != null) {
                int filter = 0b100000000000000000000;
                for (int i = 0, limit = conversionCharacters.size(); i < limit; i++) {
                    if ((lengthFlag.getMask() & filter) == 0 && specifier.equals(conversionCharacters.get(i))) {
                        result.add(new FormatError(FormatErrorType.LENGTH, lengthFlag.toString(), specifier, line));
                        break;
                    }
                    filter >>= 1;
                }
            }
            return result;
        }
    }
    
    private class Parameter {
        private final String value;
        private final int offset;
        
        Parameter(String value, int offset) {
            this.value = value;
            this.offset = offset;
        }

        public String getValue() {
            return value;
        }

        public int getOffset() {
            return offset;
        }
    }
    
    private class DummyResolvedTypeHandler implements CsmExpressionResolver.ResolvedTypeHandler {
        
        public CsmType type;

        @Override
        public void process(CsmType resolvedType) {
            type = resolvedType;
        }        
    }
}
