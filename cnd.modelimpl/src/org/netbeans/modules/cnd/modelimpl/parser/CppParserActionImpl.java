/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.*;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.ClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl.EnumBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl.EnumeratorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl.NamespaceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl.UsingDirectiveBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.SimpleDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase.NameBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.parser.symtab.*;
import org.openide.util.CharSequences;

/**
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public class CppParserActionImpl implements CppParserActionEx {

    private enum CppAttributes implements SymTabEntryKey {
        SYM_TAB, DEFINITION, TYPE
    }
    
    private final CppParserBuilderContext builderContext;
    private final SymTabStack globalSymTab;
    private Pair currentContext;
    private final Deque<Pair> contexts;
    
    private static final class Pair {
        final Map<Integer, CsmObject> objects = new HashMap<Integer, CsmObject>();
        final FileImpl file;

        public Pair(CsmFile file) {
            this.file = (FileImpl)file;
            
            if(this.file == null || this.file.getParsingFileContent() == null) {
                assert false;
            }
        }
        
    }

    public CppParserActionImpl(CsmParserProvider.CsmParserParameters params) {
        this.contexts = new ArrayDeque<Pair>();
        currentContext = new Pair(params.getMainFile());
        this.contexts.push(currentContext);
        this.globalSymTab = createGlobal();
        this.builderContext = new CppParserBuilderContext();
    }
    
    @Override
    public boolean type_specifier_already_present(TokenStream input) {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null && builderContext.getSimpleDeclarationBuilderIfExist().hasTypeSpecifier()) {
            return false;
        }
        int index = input.index();
        int scopeLevel = 0;
        SymTabEntry entry = null;
        
        while (true) {
            APTToken aToken = (APTToken) CXXParserActionImpl.convertToken(input.LT(1));
            if (aToken.getType() == APTTokenTypes.IDENT) {
                final CharSequence name = aToken.getTextID();
                entry = globalSymTab.lookup(name);
                if (entry == null || entry.getAttribute(CppAttributes.TYPE) == null) {
                    break;
                }
                input.consume();
                aToken = (APTToken) CXXParserActionImpl.convertToken(input.LT(1));
                if (aToken.getType() == APTTokenTypes.LESSTHAN) {
                    input.consume();
                    aToken = (APTToken) CXXParserActionImpl.convertToken(input.LT(1));
                    int templateLevel = 0;
                    while (templateLevel != 0 || aToken.getType() != APTTokenTypes.GREATERTHAN) {
                        if(aToken.getType() == APTTokenTypes.GREATERTHAN) {
                            templateLevel--;
                        } else if(aToken.getType() == APTTokenTypes.LESSTHAN) {
                            templateLevel++;
                        }
                        input.consume();
                        aToken = (APTToken) CXXParserActionImpl.convertToken(input.LT(1));
                    }
                    input.consume();
                    aToken = (APTToken) CXXParserActionImpl.convertToken(input.LT(1));
                }
                if (aToken.getType() == APTTokenTypes.SCOPE) {
                    if (entry.getAttribute(CppAttributes.SYM_TAB) == null) {
                        entry = null;
                        break;
                    }
                    scopeLevel++;
                    globalSymTab.push((SymTab) entry.getAttribute(CppAttributes.SYM_TAB));
                } else {
                    break;
                }
            } else if (aToken.getType() == APTTokenTypes.STAR) {
                return true;
            } else { 
                entry = null;
                break;
            }
            input.consume();
        }
        for (int i = 0; i < scopeLevel; i++) {
            globalSymTab.pop();
        }
        input.rewind(index);
        if(entry != null && entry.getAttribute(CppAttributes.TYPE) != null) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean identifier_is(int kind, Token token) {
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry entry = globalSymTab.lookup(name);
        if (entry != null) {
            return entry.getAttribute(CppAttributes.TYPE) != null;
        }
        return false;
//        return true;
    }
    
    @Override
    public boolean top_level_of_template_arguments() {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null && builderContext.getSimpleDeclarationBuilderIfExist().isInDeclSpecifiers()) {
            return true;
        }        
        return templateLevel != 0;
    }
    
    @Override
    public void parameter_declaration_list() {
    }

    @Override
    public void end_parameter_declaration_list() {
    }
    
    @Override
    public void decl_specifiers() {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
            builderContext.getSimpleDeclarationBuilderIfExist().declSpecifiers();
        }
    }

    @Override
    public void end_decl_specifiers() {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
            builderContext.getSimpleDeclarationBuilderIfExist().endDeclSpecifiers();
        }
    }
    
    
    @Override
    public void enum_declaration(Token token) {        
        //System.out.println("enum_declaration " + ((APTToken)token).getOffset());
        
        EnumBuilder enumBuilder = new EnumBuilder(currentContext.file.getParsingFileContent());
        enumBuilder.setParent(builderContext.top(1));
        enumBuilder.setFile(currentContext.file);
        if(token instanceof APTToken) {
            enumBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(enumBuilder);
    }

    @Override
    public void enum_strongly_typed(Token token) {
        //System.out.println("enum_strongly_typed " + ((APTToken)token).getOffset());

        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();
            enumBuilder.setStronglyTyped();
        }
    }

    @Override
    public void enum_name(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();

            APTToken aToken = (APTToken) token;
            final CharSequence name = aToken.getTextID();
            SymTabEntry enumEntry = globalSymTab.lookupLocal(name);
            if (enumEntry == null) {
                enumEntry = globalSymTab.enterLocal(name);
                enumEntry.setAttribute(CppAttributes.TYPE, true);
            } else {
                // error
            }
            enumBuilder.setName(name);
        }
    }

    @Override
    public void enum_body(Token token) {
        globalSymTab.push();
    }
    
    @Override
    public void enumerator(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();

            APTToken aToken = (APTToken) token;
            final CharSequence name = aToken.getTextID();
            SymTabEntry enumeratorEntry = globalSymTab.lookupLocal(name);
            if (enumeratorEntry == null) {
                enumeratorEntry = globalSymTab.enterLocal(name);
    //            enumeratorEntry.setAttribute(CppAttributes.SYM_TAB, globalSymTab.getLocal());
            } else {
                // ERROR redifinition
            }
            if(enumBuilder != null) {
                EnumeratorBuilder builder2 = new EnumeratorBuilder(currentContext.file.getParsingFileContent());
                builder2.setName(name);
                builder2.setFile(currentContext.file);
                builder2.setStartOffset(aToken.getOffset());
                builder2.setEndOffset(aToken.getEndOffset());
                enumBuilder.addEnumerator(builder2);
            }
        }
    }
    
    @Override
    public void end_enum_body(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();

            if(token instanceof APTToken) {
                enumBuilder.setEndOffset(((APTToken)token).getEndOffset());
            }
        }
        SymTab enumerators = globalSymTab.pop();
        globalSymTab.importToLocal(enumerators);
    }

    @Override
    public void end_enum_declaration(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof EnumBuilder) {
            EnumBuilder enumBuilder = builderContext.getEnumBuilder();
        
//            EnumImpl e = enumBuilder.create(true);
//            if(e != null) {
//                currentContext.objects.put(e.getStartOffset(), e);
//                SymTabEntry enumEntry = globalSymTab.lookupLocal(e.getName());
//                enumEntry.setAttribute(CppAttributes.DEFINITION, e);
//                for (CsmEnumerator csmEnumerator : e.getEnumerators()) {
//                    SymTabEntry enumeratorEntry = globalSymTab.lookupLocal(csmEnumerator.getName());
//                    assert enumeratorEntry != null;
//                    enumeratorEntry.setAttribute(CppAttributes.DEFINITION, csmEnumerator);
//                }
//            }

            builderContext.pop();
        }
    }
    
    @Override
    public void class_declaration(Token token) {
        ClassBuilder classBuilder = new ClassBuilder(currentContext.file.getParsingFileContent());
        classBuilder.setParent(builderContext.top(1));
        classBuilder.setFile(currentContext.file);
        if(token instanceof APTToken) {
            classBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(classBuilder);
    }

    @Override
    public void class_kind(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            Kind kind = Kind.CLASS;
            switch (token.getType()) {
                case CPPTokenTypes.LITERAL_class:
                    kind = Kind.CLASS;
                    break;
                case CPPTokenTypes.LITERAL_union:
                    kind = Kind.UNION;
                    break;
                case CPPTokenTypes.LITERAL_struct:
                    kind = Kind.STRUCT;
                    break;
            }
            classBuilder.setKind(kind);
        }
    }    
    
    @Override
    public void class_name(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            APTToken aToken = (APTToken) token;
            final CharSequence name = aToken.getTextID();
            SymTabEntry classEntry = globalSymTab.lookupLocal(name);
            if (classEntry == null) {
                classEntry = globalSymTab.enterLocal(name);
                classEntry.setAttribute(CppAttributes.TYPE, true);
            } else {
                // error
            }

            classBuilder.setName(name, aToken.getOffset(), aToken.getEndOffset());
        }
    }
    
    @Override
    public void class_body(Token token) {
        SymTab st = globalSymTab.push();
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            CharSequence name = classBuilder.getName();
            if(name != null) {
                SymTabEntry classEntry = globalSymTab.lookup(name);
                if (classEntry != null) {
                    classEntry.setAttribute(CppAttributes.SYM_TAB, st);
                }                 
            }
        }
    }

    @Override
    public void end_class_body(Token token) {
        globalSymTab.pop();
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;
            if(token instanceof APTToken) {
                classBuilder.setEndOffset(((APTToken)token).getEndOffset());
            }
        }
    }
    
    @Override
    public void end_class_declaration(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof ClassBuilder) {
            ClassBuilder classBuilder = (ClassBuilder) top;

            ClassImpl cls = classBuilder.create();
            if(cls != null) {
                currentContext.objects.put(cls.getStartOffset(), cls);
                SymTabEntry classEntry = globalSymTab.lookupLocal(cls.getName());
                if(classEntry != null) {
                    classEntry.setAttribute(CppAttributes.DEFINITION, cls);
                } else {
//                    System.out.println("classEntry is empty " + cls);
                }
            }
            builderContext.pop();
        }
    }    

    @Override
    public void namespace_declaration(Token token) {
        NamespaceBuilder nsBuilder = new NamespaceBuilder();
        nsBuilder.setParentNamespace(builderContext.getNamespaceBuilderIfExist());
        nsBuilder.setFile(currentContext.file);
        if(token instanceof APTToken) {
            nsBuilder.setStartOffset(((APTToken)token).getOffset());
        }
        builderContext.push(nsBuilder);
    }

    @Override
    public void namespace_name(Token token) {
        NamespaceBuilder nsBuilder = builderContext.getNamespaceBuilder();
        
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry enumEntry = globalSymTab.lookupLocal(name);
        if (enumEntry == null) {
            enumEntry = globalSymTab.enterLocal(name);
            enumEntry.setAttribute(CppAttributes.TYPE, true);
        } else {
            // error
        }
        nsBuilder.setName(name);
    }
    
    @Override
    public void namespace_body(Token token) {
        NamespaceBuilder nsBuilder = builderContext.getNamespaceBuilder();
        if(token instanceof APTToken) {
            nsBuilder.setBodyStartOffset(((APTToken)token).getOffset());
        }
        SymTabEntry classEntry = globalSymTab.lookupLocal(nsBuilder.getName());
        SymTab st = null;
        if (classEntry != null) {
            st = (SymTab)classEntry.getAttribute(CppAttributes.SYM_TAB);
        }
        if(st != null) {
            globalSymTab.push(st);
        } else {
            st = globalSymTab.push();
            if(classEntry != null) {
                classEntry.setAttribute(CppAttributes.SYM_TAB, st);
            }
        }
    }

    @Override
    public void end_namespace_body(Token token) {
        globalSymTab.pop();
    }

    @Override
    public void end_namespace_declaration(Token token) {
        NamespaceBuilder nsBuilder = builderContext.getNamespaceBuilder();
        if(token instanceof APTToken) {
            nsBuilder.setEndOffset(((APTToken)token).getEndOffset());
        }
        builderContext.pop();
        nsBuilder.create();
    }
    
    @Override
    public void simple_declaration(Token token) {
        SimpleDeclarationBuilder builder = new SimpleDeclarationBuilder();
        builderContext.push(builder);        
    }

    @Override
    public void end_simple_declaration(Token token) {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
            builderContext.pop();
        }
    }
    
    @Override
    public void compound_statement(Token token) {
        globalSymTab.push();
    }

    @Override
    public void end_compound_statement(Token token) {
        globalSymTab.pop();
    }

    @Override
    public void decl_specifier(int kind, Token token) {
        if(kind == DECL_SPECIFIER__LITERAL_TYPEDEF) {
            if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
                builderContext.getSimpleDeclarationBuilderIfExist().setTypedefSpecifier();
            }
        }
        if(kind == DECL_SPECIFIER__TYPE_SPECIFIER) {
        }
    }

    @Override
    public void simple_type_specifier(Token token) {
        if(builderContext.getSimpleDeclarationBuilderIfExist() != null) {
            builderContext.getSimpleDeclarationBuilderIfExist().setTypeSpecifier();
        }        
    }

    @Override
    public void nested_name_specifier(Token token) {
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
    }

    @Override
    public void simple_template_id(Token token) {
    }    
    
    @Override
    public void id(Token token) {
//        APTToken aToken = (APTToken) token;
//        final CharSequence name = aToken.getTextID();
//        SymTabEntry entry = globalSymTab.lookup(name);
//        if (entry != null) {
//            addReference(token, (CsmObject) entry.getAttribute(CppAttributes.DEFINITION), CsmReferenceKind.DIRECT_USAGE);
//        }
    }
    
    @Override
    public void simple_type_id(Token token) {
//        APTToken aToken = (APTToken) token;
//        final CharSequence name = aToken.getTextID();
//        SymTabEntry entry = globalSymTab.lookup(name);
//        if (entry != null) {
//            CsmObject def = (CsmObject) entry.getAttribute(CppAttributes.DEFINITION);
//            addReference(token, def, CsmReferenceKind.DIRECT_USAGE);
//            
////            if(token instanceof APTToken && CsmKindUtilities.isClassifier(def)) {
////                CsmType type = TypeFactory.createSimpleType((CsmClassifier)def, currentContext.file, ((APTToken)token).getOffset(), ((APTToken)token).getEndOffset());
////                currentContext.objects.put(type.getStartOffset(), type);
////            }
//            
//        }
        
    }
    
    @Override
    public void simple_template_id_or_ident(Token token) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof SimpleDeclarationBuilder && ((SimpleDeclarationBuilder)top).hasTypedefSpecifier() && !((SimpleDeclarationBuilder)top).isInDeclSpecifiers()) {
            APTToken aToken = (APTToken) token;
            final CharSequence name = aToken.getTextID();
            SymTabEntry classEntry = globalSymTab.lookup(name);
            if (classEntry == null) {
                classEntry = globalSymTab.enterLocal(name);
                classEntry.setAttribute(CppAttributes.TYPE, true);
            }
        }
        if(top instanceof NameBuilder) {
            NameBuilder nameBuilder = (NameBuilder) top;
            APTToken aToken = (APTToken) token;
            CharSequence part = aToken.getTextID();
            nameBuilder.addNamePart(part);
        }
    }

    int templateLevel = 0;
    
    @Override
    public void simple_template_id_or_ident(int kind, Token token) {
        if(kind == SIMPLE_TEMPLATE_ID_OR_IDENT__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == SIMPLE_TEMPLATE_ID_OR_IDENT__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }
    
    @Override
    public void simple_template_id(int kind, Token token) {
        if(kind == SIMPLE_TEMPLATE_ID__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == SIMPLE_TEMPLATE_ID__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }

    @Override
    public void simple_template_id_nocheck(int kind, Token token) {
        if(kind == SIMPLE_TEMPLATE_ID_NOCHECK__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == SIMPLE_TEMPLATE_ID_NOCHECK__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }
    
    @Override
    public void template_declaration(int kind, Token token) {
        if(kind == TEMPLATE_DECLARATION__TEMPLATE_ARGUMENT_LIST) {
            templateLevel++;
        }        
        if(kind == TEMPLATE_DECLARATION__END_TEMPLATE_ARGUMENT_LIST) {
            templateLevel--;
        }        
    }
    
    
    @Override
    public void elaborated_type_specifier(Token token) {
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry classEntry = globalSymTab.lookup(name);
        if (classEntry == null) {
            classEntry = globalSymTab.enterLocal(name);
            classEntry.setAttribute(CppAttributes.TYPE, true);
        }
    }
    
    @Override
    public void using_declaration(Token token) {
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        SymTabEntry classEntry = globalSymTab.lookupLocal(name);
        if (classEntry == null) {
            classEntry = globalSymTab.enterLocal(name);
            classEntry.setAttribute(CppAttributes.TYPE, true);
        }
    }
    

    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3) {
        if(kind == TYPE_PARAMETER__CLASS ||
                kind == TYPE_PARAMETER__TYPENAME) {
            if(token3 != null) {
                APTToken aToken = (APTToken) token3;
                final CharSequence name = aToken.getTextID();
                SymTabEntry classEntry = globalSymTab.lookupLocal(name);
                if (classEntry == null) {
                    classEntry = globalSymTab.enterLocal(name);
                    classEntry.setAttribute(CppAttributes.TYPE, true);
                }
            }
        } else if(kind == TYPE_PARAMETER__CLASS_ASSIGNEQUAL ||
                kind == TYPE_PARAMETER__TYPENAME_ASSIGNEQUAL) {
            if(token2 != null) {
                APTToken aToken = (APTToken) token2;
                final CharSequence name = aToken.getTextID();
                SymTabEntry classEntry = globalSymTab.lookupLocal(name);
                if (classEntry == null) {
                    classEntry = globalSymTab.enterLocal(name);
                    classEntry.setAttribute(CppAttributes.TYPE, true);
                }
            }
        }

    }
    
    
    @Override
    public void using_directive(Token usingToken, Token namespaceToken) {
        UsingDirectiveBuilder usingBuilder = new UsingDirectiveBuilder(currentContext.file.getParsingFileContent());
        usingBuilder.setParent(builderContext.top());
        usingBuilder.setFile(currentContext.file);
        if(usingToken instanceof APTToken) {
            usingBuilder.setStartOffset(((APTToken)usingToken).getOffset());
        }
        builderContext.push(usingBuilder);
        
        builderContext.push(new NameBuilder());
    }

    @Override
    public void using_directive(int kind, Token token) {
        if(kind == USING_DIRECTIVE__IDENT) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof NameBuilder) {
                NameBuilder nameBuilder = (NameBuilder) top;
                APTToken aToken = (APTToken) token;
                CharSequence part = aToken.getTextID();
                nameBuilder.addNamePart(part);

                CharSequence name = nameBuilder.getName();

                builderContext.pop();
                top = builderContext.top();
                if(top instanceof UsingDirectiveBuilder) {
                    UsingDirectiveBuilder usingBuilder = (UsingDirectiveBuilder) top;
                    usingBuilder.setName(name, aToken.getOffset(), aToken.getEndOffset());
                }
            }
        } else if(kind == USING_DIRECTIVE__SCOPE) {
            CsmObjectBuilder top = builderContext.top();
            if(top instanceof NameBuilder) {
                NameBuilder nameBuilder = (NameBuilder) top;
                nameBuilder.setGlobal();
            }
        }
    }

    @Override
    public void end_using_directive(Token semicolonToken) {
        CsmObjectBuilder top = builderContext.top();
        if(top instanceof UsingDirectiveBuilder) {
            UsingDirectiveBuilder usingBuilder = (UsingDirectiveBuilder) top;
            if(semicolonToken instanceof APTToken) {
                usingBuilder.setEndOffset(((APTToken)semicolonToken).getEndOffset());
            }
            usingBuilder.create();
            builderContext.pop();
            
            
            SymTabEntry nsEntry = globalSymTab.lookup(usingBuilder.getName());
            SymTab st = null;
            if (nsEntry != null) {
                st = (SymTab)nsEntry.getAttribute(CppAttributes.SYM_TAB);
            }
            if(st != null) {
                globalSymTab.importToLocal(st);
            }            
        }        
    }    
    
    @Override
    public boolean isType(String name) {
        SymTabEntry entry = globalSymTab.lookup(CharSequences.create(name));
        if (entry != null) {
            return entry.getAttribute(CppAttributes.TYPE) != null;
        }
        return false;
    }
     
    @Override
    public void pushFile(CsmFile file) {
        this.contexts.push(currentContext);
        currentContext = new Pair(file);
    }

    @Override
    public CsmFile popFile() {
        assert !contexts.isEmpty();
        CsmFile out = currentContext.file;
        currentContext = contexts.pop();
        return out;
    }

    Map<Integer, CsmObject> getObjectsMap() {
        return currentContext.objects;
    }
    
    private SymTabStack createGlobal() {
        SymTabStack out = SymTabStack.create();
        // TODO: need to push symtab for predefined types
        
        // create global level 
        out.push();
        return out;
    }

    private static final boolean TRACE = false;
    private void addReference(Token token, final CsmObject definition, final CsmReferenceKind kind) {
        if (definition == null) {
//            assert false;
            if (TRACE) System.err.println("no definition for " + token + " in " + currentContext.file);
            return;
        }
        assert token instanceof APTToken : "token is incorrect " + token;
        if (APTUtils.isMacroExpandedToken(token)) {
            if (TRACE) System.err.println("skip registering macro expanded " + token + " in " + currentContext.file);
            return;
        }
        APTToken aToken = (APTToken) token;
        final CharSequence name = aToken.getTextID();
        final int startOffset = aToken.getOffset();
        final int endOffset = aToken.getEndOffset();
        CsmReference ref = new CsmReference() {

            @Override
            public CsmReferenceKind getKind() {
                return kind;
            }

            @Override
            public CsmObject getReferencedObject() {
                return definition;
            }

            @Override
            public CsmObject getOwner() {
                return null;
            }

            @Override
            public CsmFile getContainingFile() {
                return currentContext.file;
            }

            @Override
            public int getStartOffset() {
                return startOffset;
            }

            @Override
            public int getEndOffset() {
                return endOffset;
            }

            @Override
            public CsmOffsetable.Position getStartPosition() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public CsmOffsetable.Position getEndPosition() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public CharSequence getText() {
                return name;
            }

            @Override
            public CsmObject getClosestTopLevelObject() {
                return null;
            }
        };
        currentContext.file.addReference(ref, definition);
    }   
}
