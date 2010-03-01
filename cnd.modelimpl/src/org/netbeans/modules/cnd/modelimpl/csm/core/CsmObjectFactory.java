/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.modelimpl.csm.ClassForwardDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.FriendClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceAliasImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterEllipsisImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterListImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypedefImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.ModuleImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.ProgramImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.SubroutineImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * objects factory
 * @author Vladimir Voskresensky
 */
/* XXX typo in interface name?
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.spi.PersistentObjectFactory.class)
*/
public final class CsmObjectFactory extends AbstractObjectFactory implements PersistentFactory {
    
    private static final CsmObjectFactory instance = new CsmObjectFactory();

    private CsmObjectFactory() {
    }

    public static CsmObjectFactory instance() {
        return instance;
    }

    public boolean canWrite(Persistent obj) {
        if (obj instanceof FileImpl) {
            return ((FileImpl) obj).getBuffer().isFileBased();
        } else {
            return true;
        }
    }

    protected int getHandler(Object object) {
        assert object != null;
        int aHandler;
        if (object instanceof LibProjectImpl) {
            aHandler = LIB_PROJECT_IMPL;
        } else if (object instanceof ProjectImpl) {
            aHandler = PROJECT_IMPL;
        } else if (object instanceof FileContainer) {
            aHandler = FILES_CONTAINER;
        } else if (object instanceof GraphContainer) {
            aHandler = GRAPH_CONTAINER;
        } else if (object instanceof FileImpl) {
            aHandler = FILE_IMPL;
//        } else if (object instanceof Unresolved.UnresolvedFile) {
//            aHandler = UNRESOLVED_FILE;
//        } else if (object instanceof Unresolved.UnresolvedClass) {
//            aHandler = UNRESOLVED_CLASS;
        } else if (object instanceof EnumImpl) {
            aHandler = ENUM_IMPL;
        } else if (object instanceof ClassImpl) {
            if (object instanceof ClassImplSpecialization) {
                aHandler = CLASS_IMPL_SPECIALIZATION;
            } else if (object instanceof ForwardClass) {
                aHandler = FORWARD_CLASS;
            } else {
                aHandler = CLASS_IMPL;
            }
        } else if (object instanceof TypedefImpl) {
            if (object instanceof ClassImpl.MemberTypedef) {
                aHandler = MEMBER_TYPEDEF;
            } else {
                aHandler = TYPEDEF_IMPL;
            }
        } else if (object instanceof NamespaceImpl) {
            aHandler = NAMESPACE_IMPL;
        } else if (object instanceof NamespaceDefinitionImpl) {
            aHandler = NAMESPACE_DEF_IMPL;
        } else if (object instanceof NamespaceAliasImpl) {
            aHandler = NAMESPACE_ALIAS_IMPL;
        } else if (object instanceof UsingDeclarationImpl) {
            aHandler = USING_DECLARATION_IMPL;
        } else if (object instanceof UsingDirectiveImpl) {
            aHandler = USING_DIRECTIVE_IMPL;
        } else if (object instanceof ClassForwardDeclarationImpl) {
            if (object instanceof ClassImpl.ClassMemberForwardDeclaration) {
                aHandler = CLASS_MEMBER_FORWARD_DECLARATION;
            } else {
                aHandler = CLASS_FORWARD_DECLARATION_IMPL;
            }
        } else if (object instanceof FunctionImpl) {
            // we have several FunctionImpl subclasses
            if (object instanceof FunctionImplEx) {
                if (object instanceof FunctionDefinitionImpl) {
                    // we have several FunctionDefinitionImpl subclasses
                    if (object instanceof DestructorDefinitionImpl) {
                        aHandler = DESTRUCTOR_DEF_IMPL;
                    } else if (object instanceof ConstructorDefinitionImpl) {
                        aHandler = CONSTRUCTOR_DEF_IMPL;
                    } else {
                        if (object instanceof CsmFriendFunction) {
                            aHandler = FRIEND_FUNCTION_DEF_IMPL;
                        } else {
                            aHandler = FUNCTION_DEF_IMPL;
                        }
                    }
                } else {
                    if (object instanceof CsmFriendFunction) {
                        aHandler = FRIEND_FUNCTION_IMPL_EX;
                    } else {
                        aHandler = FUNCTION_IMPL_EX;
                    }
                }
            } else if (object instanceof MethodImpl) {
                // we have several MethodImpl subclusses
                if (object instanceof MethodDDImpl) {
                    // we have two MethodDDImpl classses:
                    if (object instanceof DestructorDDImpl) {
                        aHandler = DESTRUCTOR_DEF_DECL_IMPL;
                    } else if (object instanceof ConstructorDDImpl) {
                        aHandler = CONSTRUCTOR_DEF_DECL_IMPL;
                    } else {
                        aHandler = METHOD_DEF_DECL_IMPL;
                    }
                } else if (object instanceof ConstructorImpl) {
                    aHandler = CONSTRUCTOR_IMPL;
                } else if (object instanceof DestructorImpl) {
                    aHandler = DESTRUCTOR_IMPL;
                } else {
                    aHandler = METHOD_IMPL;
                }
            } else if (object instanceof FunctionDDImpl) {
                if (object instanceof CsmFriendFunction) {
                    aHandler = FRIEND_FUNCTION_DEF_DECL_IMPL;
                } else {
                    aHandler = FUNCTION_DEF_DECL_IMPL;
                }
            } else {
                if (object instanceof CsmFriendFunction) {
                    aHandler = FRIEND_FUNCTION_IMPL;
                } else {
                    aHandler = FUNCTION_IMPL;
                }
            }
        } else if (object instanceof VariableImpl) {
            // we have several VariableImpl subclasses
            if (object instanceof VariableDefinitionImpl) {
                aHandler = VARIABLE_DEF_IMPL;
            } else if (object instanceof FieldImpl) {
                aHandler = FIELD_IMPL;
            } else if (object instanceof ParameterEllipsisImpl) {
                aHandler = PARAMETER_ELLIPSIS_IMPL;
            } else if (object instanceof ParameterImpl) {
                aHandler = PARAMETER_IMPL;
            } else {
                aHandler = VARIABLE_IMPL;
            }
        } else if (object instanceof EnumeratorImpl) {
            aHandler = ENUMERATOR_IMPL;
        } else if (object instanceof IncludeImpl) {
            aHandler = INCLUDE_IMPL;
        } else if (object instanceof ParameterListImpl) {
            aHandler = PARAM_LIST_IMPL;
            if (object instanceof FunctionParameterListImpl) {
                aHandler = FUNCTION_PARAM_LIST_IMPL;
                if (object instanceof FunctionParameterListImpl.FunctionKnRParameterListImpl) {
                    aHandler = FUNCTION_KR_PARAM_LIST_IMPL;
                }
            }
        } else if (object instanceof MacroImpl) {
            aHandler = MACRO_IMPL;
        } else if (object instanceof FriendClassImpl) {
            aHandler = FRIEND_CLASS_IMPL;
        } else if (object instanceof DeclarationContainer) {
            aHandler = DECLARATION_CONTAINER;
        } else if (object instanceof ClassifierContainer) {
            aHandler = CLASSIFIER_CONTAINER;
        } else if (object instanceof TemplateParameterImpl) {
            aHandler = TEMPLATE_PARAMETER_IMPL;
        } else if (object instanceof ProgramImpl) {
            aHandler = PROGRAM_IMPL;
        } else if (object instanceof SubroutineImpl) {
            aHandler = SUBROUTINE_IMPL;
        } else if (object instanceof ModuleImpl) {
            aHandler = MODULE_IMPL;
        } else {
            throw new IllegalArgumentException("instance of unknown class " + object.getClass().getName());  //NOI18N
        }
        return aHandler;
    }

    protected SelfPersistent createObject(int handler, DataInput stream) throws IOException {
        SelfPersistent obj;

        switch (handler) {
            case PROJECT_IMPL:
                obj = new ProjectImpl(stream);
                break;

            case LIB_PROJECT_IMPL:
                obj = new LibProjectImpl(stream);
                break;

            case FILES_CONTAINER:
                obj = new FileContainer(stream);
                break;

            case GRAPH_CONTAINER:
                obj = new GraphContainer(stream);
                break;

            case FILE_IMPL:
                obj = new FileImpl(stream);
                break;

//            case UNRESOLVED_FILE:
//                obj = new Unresolved.UnresolvedFile(stream);
//                break;
//                
//            case UNRESOLVED_CLASS:
//                obj = new Unresolved.UnresolvedClass(stream);
//                break;

            case ENUM_IMPL:
                obj = new EnumImpl(stream);
                break;

            case CLASS_IMPL:
                obj = new ClassImpl(stream);
                break;

            case CLASS_IMPL_SPECIALIZATION:
                obj = new ClassImplSpecialization(stream);
                break;

            case FORWARD_CLASS:
                obj = new ForwardClass(stream);
                break;

            case TYPEDEF_IMPL:
                obj = new TypedefImpl(stream);
                break;

            case MEMBER_TYPEDEF:
                obj = new ClassImpl.MemberTypedef(stream);
                break;

            case NAMESPACE_IMPL:
                obj = new NamespaceImpl(stream);
                break;

            case NAMESPACE_DEF_IMPL:
                obj = new NamespaceDefinitionImpl(stream);
                break;

            case NAMESPACE_ALIAS_IMPL:
                obj = new NamespaceAliasImpl(stream);
                break;

            case USING_DECLARATION_IMPL:
                obj = new UsingDeclarationImpl(stream);
                break;

            case USING_DIRECTIVE_IMPL:
                obj = new UsingDirectiveImpl(stream);
                break;

            case CLASS_FORWARD_DECLARATION_IMPL:
                obj = new ClassForwardDeclarationImpl(stream);
                break;

            case CLASS_MEMBER_FORWARD_DECLARATION:
                obj = new ClassImpl.ClassMemberForwardDeclaration(stream);
                break;

            case FUNCTION_IMPL:
                obj = new FunctionImpl(stream);
                break;

            case FUNCTION_IMPL_EX:
                obj = new FunctionImplEx(stream);
                break;

            case DESTRUCTOR_DEF_IMPL:
                obj = new DestructorDefinitionImpl(stream);
                break;

            case CONSTRUCTOR_DEF_IMPL:
                obj = new ConstructorDefinitionImpl(stream);
                break;

            case CONSTRUCTOR_DEF_DECL_IMPL:
                obj = new ConstructorDDImpl(stream);
                break;

            case FUNCTION_DEF_IMPL:
                obj = new FunctionDefinitionImpl(stream);
                break;

            case DESTRUCTOR_DEF_DECL_IMPL:
                obj = new DestructorDDImpl(stream);
                break;

            case METHOD_DEF_DECL_IMPL:
                obj = new MethodDDImpl(stream);
                break;

            case CONSTRUCTOR_IMPL:
                obj = new ConstructorImpl(stream);
                break;

            case DESTRUCTOR_IMPL:
                obj = new DestructorImpl(stream);
                break;

            case METHOD_IMPL:
                obj = new MethodImpl(stream);
                break;

            case FUNCTION_DEF_DECL_IMPL:
                obj = new FunctionDDImpl(stream);
                break;

            case VARIABLE_DEF_IMPL:
                obj = new VariableDefinitionImpl(stream);
                break;

            case FIELD_IMPL:
                obj = new FieldImpl(stream);
                break;

            case PARAMETER_IMPL:
                obj = new ParameterImpl(stream);
                break;

            case PARAMETER_ELLIPSIS_IMPL:
                obj = new ParameterEllipsisImpl(stream);
                break;

            case VARIABLE_IMPL:
                obj = new VariableImpl(stream);
                break;

            case ENUMERATOR_IMPL:
                obj = new EnumeratorImpl(stream);
                break;

            case INCLUDE_IMPL:
                obj = new IncludeImpl(stream);
                break;

            case PARAM_LIST_IMPL:
                obj = new ParameterListImpl(stream);
                break;

            case FUNCTION_PARAM_LIST_IMPL:
                obj = new FunctionParameterListImpl(stream);
                break;

            case FUNCTION_KR_PARAM_LIST_IMPL:
                obj = new FunctionParameterListImpl.FunctionKnRParameterListImpl(stream);
                break;

            case MACRO_IMPL:
                obj = new MacroImpl(stream);
                break;

            case FRIEND_CLASS_IMPL:
                obj = new FriendClassImpl(stream);
                break;

            case FRIEND_FUNCTION_IMPL:
                obj = new FriendFunctionImpl(stream);
                break;

            case FRIEND_FUNCTION_IMPL_EX:
                obj = new FriendFunctionImplEx(stream);
                break;

            case FRIEND_FUNCTION_DEF_IMPL:
                obj = new FriendFunctionDefinitionImpl(stream);
                break;

            case FRIEND_FUNCTION_DEF_DECL_IMPL:
                obj = new FriendFunctionDDImpl(stream);
                break;

            case DECLARATION_CONTAINER:
                obj = new DeclarationContainer(stream);
                break;

            case CLASSIFIER_CONTAINER:
                obj = new ClassifierContainer(stream);
                break;

            case TEMPLATE_PARAMETER_IMPL:
                obj = new TemplateParameterImpl(stream);
                break;

            case PROGRAM_IMPL:
                obj = new ProgramImpl(stream);
                break;

            case SUBROUTINE_IMPL:
                obj = new SubroutineImpl(stream);
                break;

            case MODULE_IMPL:
                obj = new ModuleImpl(stream);
                break;

            default:
                throw new IllegalArgumentException("unknown handler" + handler);  //NOI18N
        }
        return obj;
    }

    public void write(DataOutput out, Persistent obj) throws IOException {
        SelfPersistent persistentObj = (SelfPersistent) obj;
        super.writeSelfPersistent(persistentObj, out);
    }

    public Persistent read(DataInput in) throws IOException {
        SelfPersistent persistentObj = super.readSelfPersistent(in);
        assert persistentObj == null || persistentObj instanceof Persistent;
        return (Persistent) persistentObj;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////
    // handlers to identify different classes of projects
    
    private static final int FIRST_INDEX                    = UIDObjectFactory.LAST_INDEX + 1;
    
    private static final int PROJECT_IMPL                   = FIRST_INDEX;
    private static final int LIB_PROJECT_IMPL               = PROJECT_IMPL + 1;    
    private static final int FILES_CONTAINER                = LIB_PROJECT_IMPL + 1;
    private static final int GRAPH_CONTAINER                = FILES_CONTAINER + 1;
    private static final int DECLARATION_CONTAINER	        = GRAPH_CONTAINER + 1;
    private static final int CLASSIFIER_CONTAINER           = DECLARATION_CONTAINER + 1;
    private static final int FILE_IMPL                      = CLASSIFIER_CONTAINER + 1;
    private static final int ENUM_IMPL                      = FILE_IMPL + 1;
    private static final int CLASS_IMPL_SPECIALIZATION      = ENUM_IMPL + 1;
    private static final int FORWARD_CLASS                  = CLASS_IMPL_SPECIALIZATION + 1;
    private static final int CLASS_IMPL                     = FORWARD_CLASS + 1;
//    private static final int UNRESOLVED_FILE                = CLASS_IMPL + 1;
//    private static final int UNRESOLVED_CLASS               = UNRESOLVED_FILE + 1;
//    private static final int TYPEDEF_IMPL                   = UNRESOLVED_CLASS + 1;
    private static final int TYPEDEF_IMPL                   = CLASS_IMPL + 1;
    private static final int MEMBER_TYPEDEF                 = TYPEDEF_IMPL + 1;
    private static final int NAMESPACE_IMPL                 = MEMBER_TYPEDEF + 1;
    private static final int NAMESPACE_DEF_IMPL             = NAMESPACE_IMPL + 1;
    private static final int NAMESPACE_ALIAS_IMPL           = NAMESPACE_DEF_IMPL + 1;
    private static final int USING_DECLARATION_IMPL         = NAMESPACE_ALIAS_IMPL + 1;
    private static final int USING_DIRECTIVE_IMPL           = USING_DECLARATION_IMPL + 1;
    private static final int CLASS_FORWARD_DECLARATION_IMPL = USING_DIRECTIVE_IMPL + 1;   
    private static final int CLASS_MEMBER_FORWARD_DECLARATION = CLASS_FORWARD_DECLARATION_IMPL + 1;   
    private static final int FRIEND_CLASS_IMPL              = CLASS_MEMBER_FORWARD_DECLARATION + 1;   
                
    // functions
    private static final int FUNCTION_IMPL                  = FRIEND_CLASS_IMPL + 1;
    private static final int FUNCTION_IMPL_EX               = FUNCTION_IMPL + 1;

    //// function definitons 
    private static final int DESTRUCTOR_DEF_IMPL            = FUNCTION_IMPL_EX + 1;
    private static final int CONSTRUCTOR_DEF_IMPL           = DESTRUCTOR_DEF_IMPL + 1;
    private static final int FUNCTION_DEF_IMPL              = CONSTRUCTOR_DEF_IMPL + 1;

    //// friends
    private static final int FRIEND_FUNCTION_IMPL           = FUNCTION_DEF_IMPL + 1;
    private static final int FRIEND_FUNCTION_IMPL_EX        = FRIEND_FUNCTION_IMPL + 1;
    private static final int FRIEND_FUNCTION_DEF_IMPL       = FRIEND_FUNCTION_IMPL_EX + 1;
    private static final int FRIEND_FUNCTION_DEF_DECL_IMPL  = FRIEND_FUNCTION_DEF_IMPL + 1;
    
    //// methods
    private static final int CONSTRUCTOR_DEF_DECL_IMPL      = FRIEND_FUNCTION_DEF_DECL_IMPL + 1;
    private static final int DESTRUCTOR_DEF_DECL_IMPL       = CONSTRUCTOR_DEF_DECL_IMPL + 1;
    private static final int METHOD_DEF_DECL_IMPL           = DESTRUCTOR_DEF_DECL_IMPL + 1;
    private static final int CONSTRUCTOR_IMPL               = METHOD_DEF_DECL_IMPL + 1;
    private static final int DESTRUCTOR_IMPL                = CONSTRUCTOR_IMPL + 1;
    private static final int METHOD_IMPL                    = DESTRUCTOR_IMPL + 1;
    
    private static final int FUNCTION_DEF_DECL_IMPL         = METHOD_IMPL + 1;
    // end of functions
    
    // variables
    private static final int VARIABLE_IMPL                  = FUNCTION_DEF_DECL_IMPL + 1;
    private static final int VARIABLE_DEF_IMPL              = VARIABLE_IMPL + 1;
    private static final int FIELD_IMPL                     = VARIABLE_DEF_IMPL + 1;
    private static final int PARAMETER_IMPL                 = FIELD_IMPL + 1;
    private static final int PARAMETER_ELLIPSIS_IMPL        = PARAMETER_IMPL + 1;
    
    private static final int ENUMERATOR_IMPL                = PARAMETER_ELLIPSIS_IMPL + 1;

    private static final int INCLUDE_IMPL                   = ENUMERATOR_IMPL + 1;
    private static final int PARAM_LIST_IMPL                = INCLUDE_IMPL + 1;
    private static final int FUNCTION_PARAM_LIST_IMPL       = PARAM_LIST_IMPL + 1;
    private static final int FUNCTION_KR_PARAM_LIST_IMPL    = FUNCTION_PARAM_LIST_IMPL + 1;
    private static final int MACRO_IMPL                     = FUNCTION_KR_PARAM_LIST_IMPL + 1;
    private static final int TEMPLATE_PARAMETER_IMPL        = MACRO_IMPL + 1;


    // fortran

    private static final int PROGRAM_IMPL                  = TEMPLATE_PARAMETER_IMPL + 1;
    private static final int SUBROUTINE_IMPL               = PROGRAM_IMPL + 1;
    private static final int MODULE_IMPL                   = SUBROUTINE_IMPL + 1;

    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX        
    public static final int LAST_INDEX = MODULE_IMPL;
}
