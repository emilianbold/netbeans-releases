/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import com.sun.imageio.plugins.common.ImageUtil;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Simple class completion: just insert the (right now fully qualified) classname
 * 
 * @author sdedic
 */
public class SimpleClassItem extends AbstractCompletionItem {
    private static final Logger LOG = Logger.getLogger(SimpleClassItem.class.getName()); // NOI18N
    
    private static final String ICON_CLASS = "org/netbeans/modules/javafx2/editor/resources/class.png"; // NOI18N
    
    private String  className;
    private  String  fullClassName;
    private String  leftText;
    private boolean deprecated;
    private int     priority;
    private static ImageIcon ICON;
    private boolean closeTag;

    public SimpleClassItem(CompletionContext ctx, String text) {
        super(ctx, text);
    }

    void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }
    
    void setClassName(String n) {
        this.className = n;
    }
    
    void setFullClassName(String n) {
        fullClassName = n;
    }

    public String getClassName() {
        return className;
    }

    public String getFullClassName() {
        return fullClassName;
    }
    
    @Override
    public int getSortPriority() {
        return priority;
    }

    @Override
    public CharSequence getSortText() {
        return className;
    }

    public void setCloseTag(boolean closeTag) {
        this.closeTag = closeTag;
    }
    
    @Override
    protected String getLeftHtmlText() {
        if (leftText != null) {
            return leftText;
        }
        String s;
        
        if (deprecated) {
            s = NbBundle.getMessage(SimpleClassItem.class, "FMT_Deprecated", className);
        } else {
            s = className;
        }
        
        s = NbBundle.getMessage(SimpleClassItem.class, "FMT_ClassName", s);
        
        if (!fullClassName.equals(className)) {
            s = NbBundle.getMessage(SimpleClassItem.class, "FMT_AddPackage", s, 
                    fullClassName.substring(0, fullClassName.length() - className.length() - 1));
        }
        
        return this.leftText = s;
    }

    @Override
    protected ImageIcon getIcon() {
        if (ICON == null) {
            ICON = ImageUtilities.loadImageIcon(ICON_CLASS, false);
        }
        return ICON;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return className;
    }

    @Override
    protected String getSubstituteText() {
        // the opening < is a part of the replacement area
        return "<" + fullClassName + ">"; // NOI18N
    }

    @MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=ClassItemFactory.class)
    public static class ItemFactory implements ClassItemFactory {

        @Override
        public CompletionItem convert(TypeElement elem, CompletionContext ctx, int priorityHint) {
            Collection<? extends ExecutableElement> execs = ElementFilter.constructorsIn(elem.getEnclosedElements());
            for (ExecutableElement e : execs) {
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    // ignore non-public ctors
                    continue;
                }
                if (e.getParameters().isEmpty()) {
                    // non-public, no-arg ctor -> provide an item
                    
                    return setup(new SimpleClassItem(ctx, 
                            elem.getQualifiedName().toString()),
                            elem, ctx, priorityHint);
                }
            }
            return null;
        }
    }

    static SimpleClassItem setup(
            SimpleClassItem item, TypeElement elem, CompletionContext ctx, int priority) {
        item.setFullClassName(elem.getQualifiedName().toString());
        item.setClassName(elem.getSimpleName().toString());
        item.setDeprecated(ctx.isBlackListed(elem));
        item.setPriority(priority);
        
//        item.setCloseTag(
//                shouldCloseTag(elem, ctx.getCompilationInfo())
//        );
        
        return item;
    }


    private static final String SETTER_PREFIX = "set"; // NOI18N
    
    /**
     * Guesses whether it is appropriate to generate a closed tag.
     * If the bean has no properties, or has only 'primitive' properties, we'll
     * close it. Primitive properties have types from j.l. package,
     * have valueOf() method, or are arrays of the above
     * 
     * @param elem
     * @return 
     */
    static boolean shouldCloseTag(TypeElement elem, CompilationInfo info) {
        
        for (ExecutableElement m : ElementFilter.methodsIn(elem.getEnclosedElements())) {
            if (!m.getSimpleName().toString().startsWith(SETTER_PREFIX)) {
                continue;
            }

            // not real setters
            if (m.getParameters().size() != 1) {
                continue;
            }

            // inaccessible or non-instance method
            if (m.getModifiers().contains(Modifier.STATIC) || 
                !m.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            
            if (!isPrimitive(m.getParameters().get(0).asType(), info)) {
                return false;
            }
        }
        return true;
    }
    
    static boolean isPrimitive(TypeMirror m, CompilationInfo ci) {
        class TV extends SimpleTypeVisitor7<Void, Void> {
            int arrayDepth;
            TypeKind primitive;
            TypeElement bottomType;
            List<TypeElement> alternatives;
            
            public Void visitDeclared(DeclaredType t, Void p){
                TypeElement e = ((TypeElement)t.asElement());
                if (alternatives != null) {
                    alternatives.add(e);
                } else if (bottomType != null) {
                    alternatives = new LinkedList<TypeElement>();
                    alternatives.add(bottomType);
                    alternatives.add(e);
                } else {
                    bottomType = e;
                }
                return null;
            }
            
            @Override
            public Void visitUnion(UnionType t, Void p) {
                // FIXME
                return super.visitUnion(t, p);
            }

            @Override
            public Void visitPrimitive(PrimitiveType t, Void p) {
                primitive = t.getKind();
                return super.visitPrimitive(t, p);
            }

            @Override
            public Void visitNull(NullType t, Void p) {
                return super.visitNull(t, p);
            }

            @Override
            public Void visitArray(ArrayType t, Void p) {
                arrayDepth++;
                return super.visitArray(t, p);
            }

            @Override
            public Void visitTypeVariable(TypeVariable t, Void p) {
                return super.visitTypeVariable(t, p);
            }

            @Override
            public Void visitWildcard(WildcardType t, Void p) {
                TypeMirror tm = t.getExtendsBound();
                if (tm != null) {
                    tm.accept(this, p);
                }
                return super.visitWildcard(t, p);
            }
        }
        
        TV tv = new TV();
        
        if (tv.primitive != null) {
            return true;
        }
        
        if (tv.alternatives == null) {
            tv.alternatives = Collections.singletonList(tv.bottomType);
        }
        
        for (TypeElement te : tv.alternatives) {
            if (!te.getQualifiedName().toString().equals("java.lang.String") &&
                (findValueOf(te, ci) == null)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Name of the value-of factory method, as per FXML guide
     */
    private static final String NAME_VALUE_OF = "valueOf"; // NOI18N

    static ExecutableElement findValueOf(TypeElement te, CompilationInfo ci) {
        TypeElement stringType = ci.getElements().getTypeElement("java.lang.String"); // NOI18N        
         List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
        for (ExecutableElement e : methods) {
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                //LOG.log(Level.FINE, "rejecting method: {0}; is not static", e);
                continue;
            }
            if (!e.getSimpleName().toString().equals(NAME_VALUE_OF)) {
                //LOG.log(Level.FINE, "rejecting method: {0}; different name", e);
                continue;
            }
            if (!ci.getTypes().isSameType(e.getReturnType(), te.asType())) {
                //LOG.log(Level.FINE, "rejecting method: {0}; does not return the type itself", e);
                continue;
            }

            List<? extends VariableElement> params = e.getParameters();
            if (params.size() != 1) {
                //LOG.log(Level.FINE, "rejecting method: {0}; has incorrect number of params ({1})",
                  //      new Object[]{e, params.size()});
                continue;
            }
            VariableElement v = params.get(0);
            TypeMirror vType = v.asType();
            if (!ci.getTypes().isSameType(vType, stringType.asType())) {
                //LOG.log(Level.FINE, "rejecting method: {0}; does not take String parameter ({1})",
                  //      new Object[]{e, vType});
                continue;
            }
            //LOG.fine("Found value-of class");
            
            return e;
        }

        return null;
   }
    
    public String toString() {
        return "SCI[" + getFullClassName() + "]";
    }
}
