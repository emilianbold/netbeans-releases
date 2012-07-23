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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBeanInfo;
import org.netbeans.modules.javafx2.editor.completion.beans.PropertyInfo;
import org.netbeans.modules.javafx2.editor.completion.impl.ErrorMark;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.completion.model.Bundle.*;

/**
 * Matches properties found in the source with the BeanInfos.
 * 
 * @author sdedic
 */
public class PropertyResolver extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    private BuildEnvironment    env;
    private FxInstance  currentInstance;
    private FxBeanInfo  beanInfo;
    private ImportProcessor importer;
    
    public PropertyResolver() {
    }

    public PropertyResolver(BuildEnvironment env) {
        this.env = env;
    }
    
    private ImportProcessor getImporter() {
        if (importer != null) {
            return importer;
        }
        ImportProcessor proc = new ImportProcessor(env.getCompilationInfo(), env.getHierarchy(), env);
        env.getModel().accept(proc);
        
        importer = proc;
        return importer;
    }

    @Override
    public void visitInstance(FxNewInstance decl) {
        visitBaseInstance(decl);
        super.visitInstance(decl);
    }

    @Override
    public void visitCopy(FxInstanceCopy copy) {
        visitBaseInstance(copy);
        super.visitCopy(copy);
    }
    
    public void visitBaseInstance(FxInstance decl) {
        FxInstance save = this.currentInstance;
        FxBeanInfo saveInfo = this.beanInfo;
        
        currentInstance = decl;
        beanInfo = env.getBeanInfo(decl.getClassName());
        this.beanInfo = saveInfo;
        this.currentInstance = save;
    }
    
    @Override
    public void visitStaticProperty(StaticProperty p) {
        if (doVisitStaticProperty(p)) {
            super.visitStaticProperty(p);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - classname as appears in the source",
        "ERR_undefinedSourceAttachClass=The attached proprty source class ''{0}'' does not exist",
        "# {0} - classname as appears in the source",
        "# {1} - 1st alternative",
        "# {2} - 2nd alternative",
        "ERR_sourceAttachClassAmbiguous=The attached property source class name ''{0}'' is ambigous. Could be {1} or {2}",
        "# {0} - resolved classname - FQN",
        "ERR_unableAnalyseClass=Unable to analyse class ''{0}''",
        "# {1} - attached property name",
        "# {0} - source class name",
        "ERR_attachedPropertyNotExist=The class ''{0}'' does not provide attached property ''{1}''",
    })
    private boolean doVisitStaticProperty(StaticProperty p) {
        // check whether 
        if (beanInfo == null) {
            return true;
        }
        String sourceClassName = p.getSourceClassName();
        // try to resolve the classname, using Importer
        Set<String> names = getImporter().resolveName(sourceClassName);
        
        int offs = (int)p.i().getStart();
        if (names == null) {
            // error - unresolvable thing
            env.addError(new ErrorMark(
                    offs,
                    p.getTagName().length(),
                    "undefined-attached-source-class",
                    ERR_undefinedSourceAttachClass(sourceClassName),
                    sourceClassName
            ));
            return true;
        } else if (names.size() > 1) {
            // error - ambiguous name
            Iterator<String> it = names.iterator();
            env.addError(new ErrorMark(
                    offs,
                    p.getTagName().length(),
                    "attached-source-class-ambiguous",
                    ERR_sourceAttachClassAmbiguous(sourceClassName, it.next(), it.next()),
                    sourceClassName
            ));
            return true;
        } 
        String resolvedName = names.iterator().next();
        // try to convert to ElementHandle:
        TypeElement resolvedEl = env.getCompilationInfo().getElements().getTypeElement(resolvedName);
        if (resolvedEl != null) {
            p.setSourceType(ElementHandle.create(resolvedEl));
        }
        FxBeanInfo sourceInfo = env.getBeanInfo(resolvedName);
        
        if (sourceInfo == null) {
            env.addError(new ErrorMark(
                    offs,
                    p.getTagName().length(),
                    "unable-analyse-class",
                    ERR_unableAnalyseClass(resolvedName),
                    resolvedName
            ));
            return true;
        }
        
        String propName = p.getName();
        PropertyInfo pi = sourceInfo.getAttachedProperty(propName);
        if (pi == null) {
            // report error, the attached property does not exist
            env.addError(new ErrorMark(
                    offs,
                    p.getTagName().length(),
                    "attached-property-not-exist",
                    ERR_attachedPropertyNotExist(resolvedName, propName),
                    resolvedName, propName
            ));
        }
        p.setTypeHandle(pi.getType());
        p.setPropertyInfo(pi);
        
        return true;
    }

    @Override
    public void visitPropertySetter(PropertySetter p) {
        if (beanInfo != null) {
            if (p.isImplicit()) {
                processDefaultProperty(p);
            } else {
                processInstanceProperty(p);
            }
        }
        super.visitPropertySetter(p);
    }
    
    @NbBundle.Messages({
        "# {0} - class name",
        "ERR_noDefaultProperty=Class {0} has no default properties. Place content in property element."
    })
    private void processDefaultProperty(PropertySetter p) {
        PropertyInfo pi = beanInfo.getDefaultProperty();
        if (pi == null) {
            int start = (int)p.i().getStart();
            int len = 1;
            TokenSequence seq = env.getHierarchy().tokenSequence();
            seq.move((int)p.i().getStart());
            if (seq.moveNext()) {
                Token<XMLTokenId>   t = seq.token();
                if (t.id() == XMLTokenId.TEXT) {
                    String tokenText = t.text().toString();
                    String trimmed = tokenText.trim();
                    int indexOfTrimmed = tokenText.indexOf(trimmed);
                    int indexOfNl = trimmed.indexOf('\n');
                    
                    start = seq.offset() + indexOfTrimmed;
                    if (indexOfNl > -1) {
                        len = indexOfNl;
                    }
                } else {
                    start = seq.offset();
                    len = t.length();
                }
            }
            env.addError(new ErrorMark(
                    start,
                    len,
                    "no-default-property",
                    ERR_noDefaultProperty(beanInfo.getClassName()),
                    beanInfo.getClassName()
            ));
        } else {
            p.setTypeHandle(pi.getType());
            p.setPropertyInfo(pi);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - full class name",
        "# {1} - property name",
        "ERR_propertyNotExist=Class ''{0}'' does not support property ''{1}''"
    })
    private void processInstanceProperty(PropertyValue p) {
        String propName = p.getName();
        // handle default property:
        
        PropertyInfo pi = beanInfo.getProperty(propName);
        int offs = (int)p.i().getStart();

        if (pi == null) {
            env.addError(new ErrorMark(
                    offs,
                    p.getTagName().length(),
                    "unable-analyse-class",
                    ERR_propertyNotExist(beanInfo.getClassName(), propName),
                    beanInfo.getClassName(), propName
            ));
        } else {
            p.setTypeHandle(pi.getType());
            p.setPropertyInfo(pi);
        }
    }

    @Override
    public void visitMapProperty(MapProperty p) {
        if (beanInfo != null) {
            processInstanceProperty(p);
        }
        super.visitMapProperty(p);
    }

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new PropertyResolver(env);
    }
}
