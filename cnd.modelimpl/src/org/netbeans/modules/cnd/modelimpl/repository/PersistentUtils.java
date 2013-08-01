/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.modelimpl.csm.NoType;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFunPtrImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.AbstractFileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.csm.deep.EmptyCompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LazyCompoundStatementImpl;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.modelimpl.csm.DeclTypeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NestedType;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterEllipsisImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterListImpl;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateDescriptor;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateParameterTypeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypeBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariadicSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LazyTryCatchStatementImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.DummyParameterImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.DummyParametersListImpl;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Voskresensky
 */
public class PersistentUtils {

    public static FileSystem readFileSystem(RepositoryDataInput input) throws IOException {
        CharSequence rootUrl = PersistentUtils.readUTF(input, FilePathCache.getManager());
        FileObject rootFileObject = CndFileUtils.urlToFileObject(rootUrl);
        assert (rootFileObject != null) : "Restored null file object for URL " + rootUrl;
        return rootFileObject.getFileSystem();
    }
    
    public static void writeFileSystem(FileSystem fs, RepositoryDataOutput output) throws IOException {
        CharSequence rootUrl = CharSequences.create(CndFileUtils.fileObjectToUrl(fs.getRoot()));
        PersistentUtils.writeUTF(rootUrl, output);        
    }

    public static void readErrorDirectives(Set<ErrorDirectiveImpl> errors, RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            ErrorDirectiveImpl offs = new ErrorDirectiveImpl(input);
            errors.add(offs);
        }
    }

    public static void writeErrorDirectives(Set<ErrorDirectiveImpl> errors, RepositoryDataOutput output) throws IOException {
        int size = errors.size();
        output.writeInt(size);
        for (ErrorDirectiveImpl error : errors) {
            error.write(output);
        }
    }

    private PersistentUtils() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // support for parameters
    public static void writeParameterList(CsmParameterList<?> paramList, RepositoryDataOutput output) throws IOException {
        if (paramList == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else if (paramList instanceof ParameterListImpl<?, ?>) {
            int handler = PARAM_LIST_IMPL;
            if (paramList instanceof FunctionParameterListImpl) {
                handler = FUN_PARAM_LIST_IMPL;
                if (paramList instanceof FunctionParameterListImpl.FunctionKnRParameterListImpl) {
                    handler = FUN_KR_PARAM_LIST_IMPL;
                }
            }
            if (paramList instanceof DummyParametersListImpl) {
                handler = DUMMY_PARAMS_LIST_IMPL;
            }
            output.writeInt(handler);
            ((ParameterListImpl<?, ?>)paramList).write(output);
        }
    }

    public static CsmParameterList<?> readParameterList(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        CsmParameterList<?> paramList;
        switch (handler) {
            case AbstractObjectFactory.NULL_POINTER:
                paramList = null;
                break;
            case PARAM_LIST_IMPL:
                paramList = new ParameterListImpl<CsmParameterList<CsmNamedElement>, CsmNamedElement>(input);
                break;
            case FUN_PARAM_LIST_IMPL:
                paramList = new FunctionParameterListImpl(input);
                break;
            case FUN_KR_PARAM_LIST_IMPL:
                paramList = new FunctionParameterListImpl.FunctionKnRParameterListImpl(input);
                break;
            case DUMMY_PARAMS_LIST_IMPL:
                paramList = new DummyParametersListImpl(input);
                break;
            default:
                assert false : "unexpected param list implementation " + handler;
                paramList = null;
        }
        return paramList;
    }

    public static void writeParameters(Collection<CsmParameter> params, RepositoryDataOutput output) throws IOException {
        if (params == null || params.isEmpty()) {
            output.writeInt(0);
        } else {
            output.writeInt(params.size());
            for (CsmParameter param : params) {
                writeParameter(param, output);
            }
        }
    }

    public static Collection<CsmParameter> readParameters(RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        if (size == 0) {
            return null;
        }
        ArrayList<CsmParameter> list = new ArrayList<CsmParameter>(size);
        for (int i = 0; i < size; i++) {
            list.add(readParameter(input));
        }
        list.trimToSize();
        return list;
    }

    private static CsmParameter readParameter(RepositoryDataInput input) throws IOException {
        byte handler = input.readByte();
        CsmParameter obj = null;
        switch (handler) {
            case DUMMY_PARAMETER_IMPL:
                obj = new ParameterEllipsisImpl(input);
                break;
            case PARAMETER_ELLIPSIS_IMPL:
                obj = new ParameterEllipsisImpl(input);
                break;
            case PARAMETER_IMPL:
                obj = new ParameterImpl(input);
                break;
            default:
                assert false : "unexpected handler" + handler;
        }
        return obj;
    }

    private static void writeParameter(CsmParameter param, RepositoryDataOutput output) throws IOException {
        assert param != null;
        if (param instanceof DummyParameterImpl) {
            output.writeByte(DUMMY_PARAMETER_IMPL);
            ((DummyParameterImpl)param).write(output);
        } else if (param instanceof ParameterEllipsisImpl) { // have to be before ParameterImpl check
            output.writeByte(PARAMETER_ELLIPSIS_IMPL);
            ((ParameterEllipsisImpl)param).write(output);
        } else if (param instanceof ParameterImpl) {
            output.writeByte(PARAMETER_IMPL);
            ((ParameterImpl)param).write(output);
        } else {
            throw new IllegalArgumentException("instance of unknown FileBuffer " + param.getClass() + param);  //NOI18N
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // support file buffers
    public static void writeBuffer(FileBuffer buffer, RepositoryDataOutput output, int unitId) throws IOException {
        assert buffer != null;
        if (buffer instanceof AbstractFileBuffer) {
            // always write as file buffer file
            output.writeInt(FILE_BUFFER_FILE);
            ((AbstractFileBuffer) buffer).write(output, unitId);
        } else {
            throw new IllegalArgumentException("instance of unknown FileBuffer " + buffer);  //NOI18N
        }
    }

    public static FileBuffer readBuffer(RepositoryDataInput input, int unitId) throws IOException {
        FileBuffer buffer;
        int handler = input.readInt();
        assert handler == FILE_BUFFER_FILE;
        buffer = new FileBufferFile(input, unitId);
        return buffer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // support string (arrays)
    public static void writeStrings(CharSequence[] arr, RepositoryDataOutput output) throws IOException {
        if (arr == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            int len = arr.length;
            output.writeInt(len);
            for (int i = 0; i < len; i++) {
                assert arr[i] != null;
                PersistentUtils.writeUTF(arr[i], output);
            }
        }
    }

    public static void writeCollectionStrings(Collection<CharSequence> arr, RepositoryDataOutput output) throws IOException {
        if (arr == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            int len = arr.size();
            output.writeInt(len);
            for (CharSequence s : arr) {
                assert s != null;
                PersistentUtils.writeUTF(s, output);
            }
        }
    }

    public static CharSequence[] readStrings(RepositoryDataInput input, APTStringManager manager) throws IOException {
        CharSequence[] arr = null;
        int len = input.readInt();
        if (len != AbstractObjectFactory.NULL_POINTER) {
            arr = new CharSequence[len];
            for (int i = 0; i < len; i++) {
                arr[i] = manager.getString(PersistentUtils.readUTF(input, manager));
            }
        }
        return arr;
    }

    public static Collection<CharSequence> readCollectionStrings(RepositoryDataInput input, APTStringManager manager) throws IOException {
        List<CharSequence> arr = null;
        int len = input.readInt();
        if (len != AbstractObjectFactory.NULL_POINTER) {
            arr = new ArrayList<CharSequence>(len);
            for (int i = 0; i < len; i++) {
                arr.add(PersistentUtils.readUTF(input, manager));
            }
        }
        return arr;
    }
    private static final int UTF_LIMIT = 65535;

    private static final String NULL_STRING = new String(new char[]{0});

    public static void writeFileNameIndex(CharSequence st, RepositoryDataOutput aStream, int unitId) throws IOException {
        int id = KeyUtilities.getFileIdByName(unitId, st);
        aStream.writeInt(id);
    }

    public static CharSequence readFileNameIndex(RepositoryDataInput aStream, APTStringManager manager, int unitId) throws IOException {
        int id = aStream.readInt();
        CharSequence path = KeyUtilities.getFileNameById(unitId, id);
        CharSequence res = manager.getString(path);
        assert CharSequences.isCompact(res);
        return res;
    }

    public static void writeUTF(CharSequence st, RepositoryDataOutput aStream) throws IOException {
        if (st == null) {
            aStream.writeUTF(NULL_STRING);
        } else {
            assert CharSequences.isCompact(st);
            aStream.writeCharSequenceUTF(st);
        }
    }

    public static CharSequence readUTF(RepositoryDataInput aStream, APTStringManager manager) throws IOException {
        CharSequence s = aStream.readCharSequenceUTF();
        if (s.length()==1 && s.charAt(0)==0) {
            return null;
        }
        CharSequence res = manager.getString(s);
        assert CharSequences.isCompact(res);
        return res;
    }

    ////////////////////////////////////////////////////////////////////////////
    // support CsmExpression
    public static void writeExpression(CsmExpression expr, RepositoryDataOutput output) throws IOException {
        if (expr == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            if (expr instanceof ExpressionBase) {
                output.writeInt(EXPRESSION_BASE);
                ((ExpressionBase) expr).write(output);
            } else {
                throw new IllegalArgumentException("instance of unknown CsmExpression " + expr);  //NOI18N
            }
        }
    }

    public static CsmExpression readExpression(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        CsmExpression expr;
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            expr = null;
        } else {
            assert handler == EXPRESSION_BASE;
            expr = new ExpressionBase(input);
        }
        return expr;
    }

    public static void writeExpressions(Collection<CsmExpression> exprs, RepositoryDataOutput output) throws IOException {
        if (exprs == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            int collSize = exprs.size();
            output.writeInt(collSize);

            for (CsmExpression expr : exprs) {
                assert expr != null;
                writeExpression(expr, output);
            }
        }
    }

    public static <T extends Collection<CsmExpression>> T readExpressions(T collection, RepositoryDataInput input) throws IOException {
        int collSize = input.readInt();
        if (collSize == AbstractObjectFactory.NULL_POINTER) {
            collection = null;
        } else {
            for (int i = 0; i < collSize; ++i) {
                CsmExpression expr = readExpression(input);
                assert expr != null;
                collection.add(expr);
            }
            return collection;
        }
        return collection;
    }

    public static void writeExpressionKind(CsmExpression.Kind kind, RepositoryDataOutput output) throws IOException {
        if (kind == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            throw new UnsupportedOperationException("Not yet implemented"); //NOI18N
        }
    }

    public static CsmExpression.Kind readExpressionKind(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        CsmExpression.Kind kind;
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            kind = null;
        } else {
            throw new UnsupportedOperationException("Not yet implemented"); //NOI18N
        }
        return kind;
    }

    ////////////////////////////////////////////////////////////////////////////
    // support types
    public static CsmType readType(RepositoryDataInput stream) throws IOException {
        CsmType obj;
        int handler = stream.readInt();
        switch (handler) {
            case AbstractObjectFactory.NULL_POINTER:
                obj = null;
                break;

            case NO_TYPE:
                obj = NoType.instance();
                break;

            case TYPE_IMPL:
                obj = new TypeImpl(stream);
                break;

            case NESTED_TYPE:
                obj = new NestedType(stream);
                break;

            case TYPE_FUN_PTR_IMPL:
                obj = new TypeFunPtrImpl(stream);
                break;
                
            case TYPE_DECLTYPE_IMPL:
                obj = new DeclTypeImpl(stream);
                break;

            case TEMPLATE_PARAM_TYPE:
                obj = new TemplateParameterTypeImpl(stream);
                break;

            default:
                throw new IllegalArgumentException("unknown type handler" + handler);  //NOI18N
        }
        return obj;
    }

    public static void writeType(CsmType type, RepositoryDataOutput stream) throws IOException {
        try {
            if (type == null) {
                stream.writeInt(AbstractObjectFactory.NULL_POINTER);
            } else if (type instanceof NoType) {
                stream.writeInt(NO_TYPE);
            } else if (type instanceof TypeImpl) {
                if (type instanceof DeclTypeImpl) {
                    stream.writeInt(TYPE_FUN_PTR_IMPL);
                    ((DeclTypeImpl) type).write(stream);
                } else if (type instanceof TypeFunPtrImpl) {
                    stream.writeInt(TYPE_FUN_PTR_IMPL);
                    ((TypeFunPtrImpl) type).write(stream);
                } else if (type instanceof NestedType) {
                    stream.writeInt(NESTED_TYPE);
                    ((NestedType) type).write(stream);
                } else {
                    stream.writeInt(TYPE_IMPL);
                    ((TypeImpl) type).write(stream);
                }
            } else if (type instanceof TemplateParameterTypeImpl) {
                stream.writeInt(TEMPLATE_PARAM_TYPE);
                ((TemplateParameterTypeImpl) type).write(stream);
            } else {
                throw new IllegalArgumentException("instance of unknown class " + type.getClass().getName());  //NOI18N
            }
        } catch (UTFDataFormatException e) {
            CndUtils.assertTrueInConsole(false, "type with too long name ", type); // NOI18N
            throw e;
        }
    }
    
    public static boolean isPersistable(CsmType type) {
        if (type == null) {
            return true;
        } else if (type instanceof NoType) {
            return true;
        } else if (type instanceof TypeImpl) {
            return true;
        } else if (type instanceof TemplateParameterTypeImpl) {
            return true;
        } else {
            return false;
        }        
    }

    public static <T extends Collection<CsmType>> void readTypes(T collection, RepositoryDataInput input) throws IOException {
        int collSize = input.readInt();
        assert collSize >= 0;
        for (int i = 0; i < collSize; ++i) {
            CsmType type = readType(input);
            assert type != null;
            collection.add(type);
        }
    }

    public static void writeTypes(Collection<? extends CsmType> types, RepositoryDataOutput output) throws IOException {
        assert types != null;
        int collSize = types.size();
        output.writeInt(collSize);

        for (CsmType elem : types) {
            assert elem != null;
            writeType(elem, output);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // support Template Descriptors
    public static TemplateDescriptor readTemplateDescriptor(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            return null;
        }
        assert handler == TEMPLATE_DESCRIPTOR_IMPL;
        return new TemplateDescriptor(input);
    }

    public static void writeTemplateDescriptor(TemplateDescriptor templateDescriptor, RepositoryDataOutput output) throws IOException {
        if (templateDescriptor == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            output.writeInt(TEMPLATE_DESCRIPTOR_IMPL);
            templateDescriptor.write(output);
        }
    }

    public static SpecializationDescriptor readSpecializationDescriptor(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            return null;
        }
        assert handler == SPECIALIZATION_DESCRIPTOR_IMPL;
        return new SpecializationDescriptor(input);
    }

    public static void writeSpecializationDescriptor(SpecializationDescriptor specializationDescriptor, RepositoryDataOutput output) throws IOException {
        if (specializationDescriptor == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            output.writeInt(SPECIALIZATION_DESCRIPTOR_IMPL);
            specializationDescriptor.write(output);
        }
    }

    public static void writeSpecializationParameters(List<CsmSpecializationParameter> params, RepositoryDataOutput output) throws IOException {
        if (params == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            output.writeInt(SPECIALIZATION_PARAMETERS_LIST);
            output.writeInt(params.size());
            for (CsmSpecializationParameter p : params) {
                writeSpecializationParameter(p, output);
            }
        }
    }

    public static void writeSpecializationParameter(CsmSpecializationParameter param, RepositoryDataOutput output) throws IOException {
        if (param == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {        
            if (param instanceof TypeBasedSpecializationParameterImpl) {
                output.writeInt(TYPE_BASED_SPECIALIZATION_PARAMETER_IMPL);
                ((TypeBasedSpecializationParameterImpl) param).write(output);
            } else if (param instanceof ExpressionBasedSpecializationParameterImpl) {
                output.writeInt(EXPRESSION_BASED_SPECIALIZATION_PARAMETER_IMPL);
                ((ExpressionBasedSpecializationParameterImpl) param).write(output);
            } else if (param instanceof VariadicSpecializationParameterImpl) {
                output.writeInt(VARIADIC_SPECIALIZATION_PARAMETER_IMPL);
                ((VariadicSpecializationParameterImpl) param).write(output);
            } else {
                assert false : "unexpected instance of specialization parameter ";
            }
        }
    }    

    public static List<CsmSpecializationParameter> readSpecializationParameters(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            return null;
        }
        assert handler == SPECIALIZATION_PARAMETERS_LIST;
        List<CsmSpecializationParameter> params = new ArrayList<CsmSpecializationParameter>();
        readSpecializationParametersList(params, input);
        return params;
    }

    public static void readSpecializationParameters(List<CsmSpecializationParameter> params, RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            return;
        }
        assert handler == SPECIALIZATION_PARAMETERS_LIST : "unexpected handler " + handler;
        readSpecializationParametersList(params, input);
    }

    private static void readSpecializationParametersList(List<CsmSpecializationParameter> params, RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            params.add(readSpecializationParameter(input));
        }
    }

    public static CsmSpecializationParameter readSpecializationParameter(RepositoryDataInput input) throws IOException {
        int type = input.readInt();
        if (type == AbstractObjectFactory.NULL_POINTER) {
            return null;
        }
        
        if(type == TYPE_BASED_SPECIALIZATION_PARAMETER_IMPL) {
            return new TypeBasedSpecializationParameterImpl(input);
        } else if (type == EXPRESSION_BASED_SPECIALIZATION_PARAMETER_IMPL) {
            return new ExpressionBasedSpecializationParameterImpl(input);
        } else if (type == VARIADIC_SPECIALIZATION_PARAMETER_IMPL) {
            return new VariadicSpecializationParameterImpl(input);
        } else {
            assert false : "unexpected instance of specialization parameter ";
        }
        return null;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // support visibility
    public static void writeVisibility(CsmVisibility visibility, RepositoryDataOutput output) throws IOException {
        assert visibility != null;
        int handler = -1;
        if (visibility == CsmVisibility.PUBLIC) {
            handler = VISIBILITY_PUBLIC;
        } else if (visibility == CsmVisibility.PROTECTED) {
            handler = VISIBILITY_PROTECTED;
        } else if (visibility == CsmVisibility.PRIVATE) {
            handler = VISIBILITY_PRIVATE;
        } else if (visibility == CsmVisibility.NONE) {
            handler = VISIBILITY_NONE;
        } else {
            throw new IllegalArgumentException("instance of unknown visibility " + visibility);  //NOI18N
        }
        output.writeInt(handler);
    }

    public static CsmVisibility readVisibility(RepositoryDataInput input) throws IOException {
        CsmVisibility visibility = null;
        int handler = input.readInt();
        switch (handler) {
            case VISIBILITY_PUBLIC:
                visibility = CsmVisibility.PUBLIC;
                break;

            case VISIBILITY_PROTECTED:
                visibility = CsmVisibility.PROTECTED;
                break;

            case VISIBILITY_PRIVATE:
                visibility = CsmVisibility.PRIVATE;
                break;

            case VISIBILITY_NONE:
                visibility = CsmVisibility.NONE;
                break;
            default:
                throw new IllegalArgumentException("unknown handler" + handler);  //NOI18N
        }
        return visibility;
    }

    ////////////////////////////////////////////////////////////////////////////
    // compound statements
    public static void writeCompoundStatement(CsmCompoundStatement body, RepositoryDataOutput output) throws IOException {
        assert body != null;
        if (body instanceof LazyCompoundStatementImpl) {
            output.writeInt(LAZY_COMPOUND_STATEMENT_IMPL);
            ((LazyCompoundStatementImpl) body).write(output);
        } else if (body instanceof LazyTryCatchStatementImpl) {
            output.writeInt(LAZY_TRY_CATCH_STATEMENT_IMPL);
            ((LazyTryCatchStatementImpl) body).write(output);
        } else if (body instanceof EmptyCompoundStatementImpl) {
            output.writeInt(EMPTY_COMPOUND_STATEMENT_IMPL);
            ((EmptyCompoundStatementImpl) body).write(output);
        } else if (body instanceof CompoundStatementImpl) {
            // will be deserialized as lazy compound statement
            output.writeInt(LAZY_COMPOUND_STATEMENT_IMPL);
            ((CompoundStatementImpl) body).write(output);
        } else {
            throw new IllegalArgumentException("unknown compound statement " + body);  //NOI18N
        }
    }

    public static CsmCompoundStatement readCompoundStatement(RepositoryDataInput input) throws IOException {
        int handler = input.readInt();
        CsmCompoundStatement body;
        switch (handler) {
            case LAZY_COMPOUND_STATEMENT_IMPL:
                body = new LazyCompoundStatementImpl(input);
                break;
            case LAZY_TRY_CATCH_STATEMENT_IMPL:
                body = new LazyTryCatchStatementImpl(input);
                break;
            case EMPTY_COMPOUND_STATEMENT_IMPL:
                body = new EmptyCompoundStatementImpl(input);
                break;
            default:
                throw new IllegalArgumentException("unknown handler" + handler);  //NOI18N
        }
        return body;
    }

    ////////////////////////////////////////////////////////////////////////////
    // support preprocessor states
// Unused for the time being
//    public static void writeStringToStateMap(Map<String, APTPreprocHandler.State> filesHandlers, DataOutput output) throws IOException {
//        assert filesHandlers != null;
//        int collSize = filesHandlers.size();
//        output.writeInt(collSize);
//
//        for (Entry<String, APTPreprocHandler.State> entry: filesHandlers.entrySet()) {
//            assert entry != null;
//            String key = entry.getKey();
//            output.writeUTF(key);
//            assert key != null;
//            APTPreprocHandler.State state = entry.getValue();
//            writePreprocState(state, output);
//        }
//    }

// Unused for the time being
//    public static void readStringToStateMap(Map<CharSequence, APTPreprocHandler.State> filesHandlers, DataInput input) throws IOException {
//        assert filesHandlers != null;
//        int collSize = input.readInt();
//
//        for (int i = 0; i < collSize; i++) {
//            CharSequence key = FilePathCache.getString(input.readUTF());
//            assert key != null;
//            APTPreprocHandler.State state = readPreprocState(input);
//            assert state != null;
//            filesHandlers.put(key, state);
//        }
//    }
    public static void writePreprocState(APTPreprocHandler.State state, RepositoryDataOutput output, int unitIndex) throws IOException {
        APTPreprocHandler.State cleanedState = APTHandlersSupport.createCleanPreprocState(state);
        APTSerializeUtils.writePreprocState(cleanedState, output, unitIndex);
    }

    public static APTPreprocHandler.State readPreprocState(FileSystem fs, RepositoryDataInput input, int unitIndex) throws IOException {
        APTPreprocHandler.State state = APTSerializeUtils.readPreprocState(fs, input, unitIndex);
        assert state.isCleaned();
        return state;
    }

    ////////////////////////////////////////////////////////////////////////////
    // indices
    private static final int FIRST_INDEX = CsmObjectFactory.LAST_INDEX + 1;
    private static final int VISIBILITY_PUBLIC = FIRST_INDEX;
    private static final int VISIBILITY_PROTECTED = VISIBILITY_PUBLIC + 1;
    private static final int VISIBILITY_PRIVATE = VISIBILITY_PROTECTED + 1;
    private static final int VISIBILITY_NONE = VISIBILITY_PRIVATE + 1;
    private static final int EXPRESSION_BASE = VISIBILITY_NONE + 1;
    private static final int FILE_BUFFER_FILE = EXPRESSION_BASE + 1;
    // types
    private static final int NO_TYPE = FILE_BUFFER_FILE + 1;
    private static final int TYPE_IMPL = NO_TYPE + 1;
    private static final int NESTED_TYPE = TYPE_IMPL + 1;
    private static final int TYPE_FUN_PTR_IMPL = NESTED_TYPE + 1;
    private static final int TYPE_DECLTYPE_IMPL = TYPE_FUN_PTR_IMPL + 1;
    private static final int TEMPLATE_PARAM_TYPE = TYPE_DECLTYPE_IMPL + 1;

    // state
    private static final int PREPROC_STATE_STATE_IMPL = TEMPLATE_PARAM_TYPE + 1;

    // compound statements
    private static final int LAZY_COMPOUND_STATEMENT_IMPL = PREPROC_STATE_STATE_IMPL + 1;
    private static final int LAZY_TRY_CATCH_STATEMENT_IMPL = LAZY_COMPOUND_STATEMENT_IMPL + 1;
    private static final int EMPTY_COMPOUND_STATEMENT_IMPL = LAZY_TRY_CATCH_STATEMENT_IMPL + 1;
    private static final int COMPOUND_STATEMENT_IMPL = EMPTY_COMPOUND_STATEMENT_IMPL + 1;

    // param lists
    private static final int PARAM_LIST_IMPL = COMPOUND_STATEMENT_IMPL + 1;
    private static final int FUN_PARAM_LIST_IMPL = PARAM_LIST_IMPL + 1;
    private static final int FUN_KR_PARAM_LIST_IMPL = FUN_PARAM_LIST_IMPL + 1;
    private static final int DUMMY_PARAMS_LIST_IMPL = FUN_KR_PARAM_LIST_IMPL + 1;
    // params
    private static final int DUMMY_PARAMETER_IMPL = DUMMY_PARAMS_LIST_IMPL + 1;
    private static final int PARAMETER_ELLIPSIS_IMPL = DUMMY_PARAMETER_IMPL + 1;
    private static final int PARAMETER_IMPL = PARAMETER_ELLIPSIS_IMPL + 1;

    // tempalte descriptor
    private static final int TEMPLATE_DESCRIPTOR_IMPL = PARAMETER_IMPL + 1;
    // specialization descriptor
    private static final int SPECIALIZATION_DESCRIPTOR_IMPL = TEMPLATE_DESCRIPTOR_IMPL + 1;
    // specialization parameters
    private static final int SPECIALIZATION_PARAMETERS_LIST = SPECIALIZATION_DESCRIPTOR_IMPL + 1;
    private static final int TYPE_BASED_SPECIALIZATION_PARAMETER_IMPL = SPECIALIZATION_PARAMETERS_LIST + 1;
    private static final int EXPRESSION_BASED_SPECIALIZATION_PARAMETER_IMPL = TYPE_BASED_SPECIALIZATION_PARAMETER_IMPL + 1;
    private static final int VARIADIC_SPECIALIZATION_PARAMETER_IMPL = EXPRESSION_BASED_SPECIALIZATION_PARAMETER_IMPL + 1;

    // index to be used in another factory (but only in one)
    // to start own indeces from the next after LAST_INDEX
    public static final int LAST_INDEX = VARIADIC_SPECIALIZATION_PARAMETER_IMPL;
}
