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

package org.netbeans.modules.groovy.editor.completion;

import groovy.lang.MetaMethod;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.groovy.editor.elements.KeywordElement;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.groovy.editor.NbUtilities;
import org.netbeans.modules.groovy.editor.elements.AstMethodElement;
import org.netbeans.modules.groovy.editor.elements.ElementHandleSupport;
import org.netbeans.modules.groovy.editor.elements.GroovyElement;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.gsf.spi.DefaultCompletionProposal;


/**
 *
 * @author schmidtm
 */
    abstract class GroovyCompletionItem extends DefaultCompletionProposal {

        protected CodeCompleter.CompletionRequest request;
        protected GroovyElement element;
        final Logger LOG = Logger.getLogger(GroovyCompletionItem.class.getName());
        
        static ImageIcon groovyIcon;
        static ImageIcon javaIcon;
        static ImageIcon newConstructorIcon;

        GroovyCompletionItem(GroovyElement element, int anchorOffset, CodeCompleter.CompletionRequest request) {
            this.element = element;
            this.anchorOffset = anchorOffset;
            this.request = request;
            
            LOG.setLevel(Level.OFF);
        }

        public static Collection<javax.lang.model.element.Modifier> toModel(int modifiers) {
            Set<javax.lang.model.element.Modifier> ret = new HashSet<javax.lang.model.element.Modifier>();
            
            if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.ABSTRACT);
            }
            if (java.lang.reflect.Modifier.isFinal(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.FINAL);
            }
            if (java.lang.reflect.Modifier.isNative(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.NATIVE);
            }
            if (java.lang.reflect.Modifier.isStatic(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.STATIC);
            }
            if (java.lang.reflect.Modifier.isStrict(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.STRICTFP);
            }
            if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.SYNCHRONIZED);
            }
//            if (java.lang.reflect.Modifier.isTransient(modifiers)) {
//                ret.add(javax.lang.model.element.Modifier.TRANSIENT);
//            }
//            if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
//                ret.add(javax.lang.model.element.Modifier.VOLATILE);
//            }
            
            if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.PRIVATE);
            } else if (java.lang.reflect.Modifier.isProtected(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.PROTECTED);
            } else if (java.lang.reflect.Modifier.isPublic(modifiers)) {
                ret.add(javax.lang.model.element.Modifier.PUBLIC);
            }

            return ret;
        }
        
    @Override
        public String getName() {
            return element.getName();
        }

        public ElementHandle getElement() {
            LOG.log(Level.FINEST, "getElement() request.info : {0}", request.info);
            LOG.log(Level.FINEST, "getElement() element : {0}", element);

            return null;
        }

        @Override
        public ElementKind getKind() {
            return element.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }
    }

    




/**
     * 
     */
    class JavaMethodItem extends GroovyCompletionItem {

        private final String simpleName;
        private final String parameterString;
        private final String returnType;
        

        JavaMethodItem(String simpleName, String parameterString, String returnType, int anchorOffset, CodeCompleter.CompletionRequest request) {
            super(null, anchorOffset, request);
            this.simpleName = simpleName;
            this.parameterString = parameterString;
            this.returnType = returnType;
        }

        @Override
        public String getName() {
            return simpleName + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return simpleName + "(" + parameterString + ")";
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // FIXME
            String retType = NbUtilities.stripPackage(returnType);

            formatter.appendText(retType);

            return formatter.getText();
        }
        
        
        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.METHOD, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            return null;
        }


    }
    class MethodItem extends GroovyCompletionItem {

        MetaMethod method;
        boolean isGDK;
        AstMethodElement methodElement;

        MethodItem(Class clz, MetaMethod method, int anchorOffset, CodeCompleter.CompletionRequest request, boolean isGDK) {
            super(null, anchorOffset, request);
            this.method = method;
            this.isGDK = isGDK;

            // This is an artificial, new ElementHandle which has no real
            // equivalent in the AST. It's used to match the one passed to super.document()
            methodElement = new AstMethodElement(new ASTNode(), clz, method, isGDK);
        }

        public MetaMethod getMethod() {
            return method;
        }
        
        @Override
        public String getName() {
            return method.getName() + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {

            ElementKind kind = getKind();

            formatter.name(kind, true);

            if (isGDK) {
                formatter.appendText(method.getName());

                // construct signature by removing package names.

                String signature = method.getSignature();
                int start = signature.indexOf("(");
                int end = signature.indexOf(")");

                String sig = signature.substring(start + 1, end);

                StringBuffer buf = new StringBuffer();

                for (String param : sig.split(",")) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(NbUtilities.stripPackage(param));
                }

                String simpleSig = buf.toString();
                formatter.appendText("(" + simpleSig + ")");
            } else {
                formatter.appendText(CodeCompleter.getMethodSignature(method, false, isGDK));
            }


            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = method.getReturnType().toString();
            retType = NbUtilities.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (!isGDK) {
                return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.METHOD,
                        GroovyCompletionItem.toModel(method.getModifiers()));
            }

            if (groovyIcon == null) {
                groovyIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {

            // to display the documentation box for each element, the completion-
            // element needs to implement this method. Otherwise document(...)
            // won't even be called at all.

            return methodElement;
        }
    }

    class KeywordItem extends GroovyCompletionItem {

        private static final String JAVA_KEYWORD   = "org/netbeans/modules/groovy/editor/resources/duke.png"; //NOI18N
        private final String keyword;
        private final String description;
        private final boolean isGroovy;

        KeywordItem(String keyword, String description, int anchorOffset, CodeCompleter.CompletionRequest request, boolean isGroovy) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
            this.description = description;
            this.isGroovy = isGroovy;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            
            if (isGroovy) {
                if (groovyIcon == null) {
                    groovyIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
                }
                return groovyIcon;
            } else {
                if (javaIcon == null) {
                    javaIcon = new ImageIcon(org.openide.util.Utilities.loadImage(JAVA_KEYWORD));
                }
                return javaIcon;
            }
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(keyword));
        }
    }

    /**
     * 
     */
    class PackageItem extends GroovyCompletionItem {

        private final String keyword;

        PackageItem(String keyword, int anchorOffset, CodeCompleter.CompletionRequest request) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.PACKAGE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(keyword));
        }
    }

    /**
     * 
     */
    class TypeItem extends GroovyCompletionItem {

        private final String name;
        private final javax.lang.model.element.ElementKind ek;

        TypeItem(String name, int anchorOffset, CodeCompleter.CompletionRequest request, javax.lang.model.element.ElementKind ek) {
            super(null, anchorOffset, request);
            this.name = name;
            this.ek = ek;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            // return ElementHandleSupport.createHandle(request.info, new ClassElement(name));
            return null;
        }
    }

    class ConstructorItem extends GroovyCompletionItem {

        private final String name;
        private static final String NEW_CSTR   = "org/netbeans/modules/groovy/editor/resources/new_constructor_16.png"; //NOI18N
        private boolean expand; // should this item expand to a constructor body?
        private final String paramListString;
        private final List<CodeCompleter.ParamDesc> paramList;

        ConstructorItem(String name, String paramListString, List<CodeCompleter.ParamDesc> paramList, int anchorOffset, CodeCompleter.CompletionRequest request, boolean expand) {
            super(null, anchorOffset, request);
            this.name = name;
            this.expand = expand;
            this.paramListString = paramListString;
            this.paramList = paramList;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if(expand){
                return name + " - generate"; // NOI18N
            } else {
                return name + "(" + paramListString +  ")";
            }            
        }

        @Override
        public String getName() {
            if(expand){
                return name  + "()\n{\n}";
            } else {
                return name;
            }
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {

            if (newConstructorIcon == null) {
                newConstructorIcon = new ImageIcon(org.openide.util.Utilities.loadImage(NEW_CSTR));
            }
            return newConstructorIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            // return ElementHandleSupport.createHandle(request.info, new ClassElement(name));
            return null;
        }
        
        // Constructors are smart by definition (have to be place above others)
        @Override
        public boolean isSmart() {
            return true;
        }

        // See IDE help-topic: "Creating and Customizing Ruby Code Templates" or
        // RubyCodeCompleter.MethodItem.getCustomInsertTemplate() for syntax.
        @Override
        public String getCustomInsertTemplate() {

            StringBuilder sb = new StringBuilder();
            
            sb.append(getInsertPrefix());
            sb.append("(");
            
            int id = 1;
            
            // sb.append("${cursor}"); // NOI18N

            for (CodeCompleter.ParamDesc paramDesc : paramList) {
                
                LOG.log(Level.FINEST, "-------------------------------------------------------------------");
                LOG.log(Level.FINEST, "paramDesc.fullTypeName : {0}", paramDesc.fullTypeName);
                LOG.log(Level.FINEST, "paramDesc.typeName     : {0}", paramDesc.typeName);
                LOG.log(Level.FINEST, "paramDesc.name         : {0}", paramDesc.name);
                
                sb.append("${"); //NOI18N

                sb.append("groovy-cc-"); // NOI18N
                sb.append(Integer.toString(id));
                
                sb.append(" default=\""); // NOI18N
                sb.append(paramDesc.name);
                sb.append("\""); // NOI18N

                sb.append("}"); //NOI18N

                // simply hardcoded values. For testing purposes.
                // sb.append(paramDesc.name);


                if (id < paramList.size()) {
                    sb.append(", "); //NOI18N
                }

                id++;
            }
            
            sb.append(")");
            
            LOG.log(Level.FINEST, "Template returned : {0}", sb.toString());
            return sb.toString();

        }
    }


    /**
     * 
     */
    class FieldItem extends GroovyCompletionItem {

        private final String name;
        private final javax.lang.model.element.ElementKind ek;
        private final String typeName;

        FieldItem(String name, int anchorOffset, CodeCompleter.CompletionRequest request, javax.lang.model.element.ElementKind ek, String typeName) {
            super(null, anchorOffset, request);
            this.name = name;
            this.ek = ek;
            this.typeName = typeName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return typeName;
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(name));
        }
    }

    /**
     * 
     */
    class LocalVarItem extends GroovyCompletionItem {

        private final Variable var;

        LocalVarItem(Variable var, int anchorOffset, CodeCompleter.CompletionRequest request) {
            super(null, anchorOffset, request);
            this.var = var;
        }

        @Override
        public String getName() {
            return var.getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return var.getType().getNameWithoutPackage();
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            return null;
        }
    }

    /**
     *
     */
    class NewVarItem extends GroovyCompletionItem {

        private final String var;

        NewVarItem(String var, int anchorOffset, CodeCompleter.CompletionRequest request) {
            super(null, anchorOffset, request);
            this.var = var;
        }

        @Override
        public String getName() {
            return var;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            return null;
        }
    }
