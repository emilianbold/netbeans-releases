/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.validator.cf.impl;

import java.util.List;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyMethod;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyOnDoneAnnotation;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyOnErrorAnnotation;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyOnFaultAnnotation;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyOnReplyAnnotation;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyOperationAnnotation;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Parameter;

/**
 *
 * @author gpatil
 */
public class WrapperMethod implements ProxyMethod{
    private Method m = null;
    private static String ANNO_Operation = "Lorg/glassfish/openesb/pojose/api/annotation/Operation;" ;  // No I18N
    private static String ANNO_OnReply = "Lorg/glassfish/openesb/pojose/api/annotation/OnReply;" ;  // No I18N
    private static String ANNO_OnDone = "Lorg/glassfish/openesb/pojose/api/annotation/OnDone;" ;  // No I18N
    private static String ANNO_OnFault = "Lorg/glassfish/openesb/pojose/api/annotation/OnFault;" ;  // No I18N    
    private static String ANNO_OnError = "Lorg/glassfish/openesb/pojose/api/annotation/OnError;" ;  // No I18N        
    
    public WrapperMethod(Method m){
        this.m = m;
    }
    
    public String getName() {
        return this.m.getName();
    }

    public ParamType getFirstParameterType() {
        ParamType ret = null;
        List<Parameter> ps = m.getParameters();
        if ((ps != null) && (ps.size() > 0)){
            Parameter p = ps.get(0);
            if (ClassFileHelper.CLS_BYTE_ARRAY.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.ByteArray;
            } else if (ClassFileHelper.CLS_STRING.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.String;
            } else if (ClassFileHelper.CLS_NODE.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.Node;
            } else if (ClassFileHelper.CLS_SOURCE.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.Source;                
            } else if (ClassFileHelper.CLS_OBJECT.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.Object;
            } else if (ClassFileHelper.CLS_NM.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.NormalizedMessage;
            } else if (ClassFileHelper.CLS_ME.equals(p.getDescriptor())) {
                ret = ProxyMethod.ParamType.MessageExchange;
            } 
        } else {
            ret = ProxyMethod.ParamType.Void;
        }
        

        return ret;
    }

    public ParamType getReturnParameterType() {
        ParamType ret = null;
        String rt  = m.getReturnType();
        if (ClassFileHelper.CLS_BYTE_ARRAY.equals(rt)) {
            ret = ProxyMethod.ParamType.ByteArray;
        } else if (ClassFileHelper.CLS_STRING.equals(rt)) {
            ret = ProxyMethod.ParamType.String;
        } else if (ClassFileHelper.CLS_NODE.equals(rt)) {
            ret = ProxyMethod.ParamType.Node;
        } else if (ClassFileHelper.CLS_SOURCE.equals(rt)) {
            ret = ProxyMethod.ParamType.Source;                
        } else if (ClassFileHelper.CLS_OBJECT.equals(rt)) {
            ret = ProxyMethod.ParamType.Object;
        } else if (ClassFileHelper.CLS_NM.equals(rt)) {
            ret = ProxyMethod.ParamType.NormalizedMessage;
        } else if (ClassFileHelper.CLS_ME.equals(rt)) {
            ret = ProxyMethod.ParamType.MessageExchange;
        } else if (ClassFileHelper.CLS_VOID.equals(rt)) {
            ret = ProxyMethod.ParamType.Void;
        }
        
        return ret;
    }

    public ProxyOnReplyAnnotation getOnReplyAnnotation() {
        Annotation anno = m.getAnnotation(ClassName.getClassName(ANNO_OnReply));
        if (anno != null){
            return new WrapperOnReply(anno);
        }
        return null;
    }

    public ProxyOperationAnnotation getOperationAnnotation() {
        Annotation anno = m.getAnnotation(ClassName.getClassName(ANNO_Operation));
        if (anno != null){
            return new WrapperOperation(anno);
        }
        return null;
    }        

    public ProxyOnDoneAnnotation getOnDoneAnnotation() {
        Annotation anno = m.getAnnotation(ClassName.getClassName(ANNO_OnDone));
        if (anno != null){
            return new WrapperOnDone(anno);
        }
        return null;
    }

    public ProxyOnFaultAnnotation getOnFaultAnnotation() {
        Annotation anno = m.getAnnotation(ClassName.getClassName(ANNO_OnFault));
        if (anno != null){
            return new WrapperOnFault(anno);
        }
        return null;
    }

    public ProxyOnErrorAnnotation getOnErrorAnnotation() {
        Annotation anno = m.getAnnotation(ClassName.getClassName(ANNO_OnError));
        if (anno != null){
            return new WrapperOnError(anno);
        }
        return null;
    }
    
    public boolean isPublic() {
        return this.m.isPublic();
    }

    public boolean isStatic() {
        return this.m.isStatic();
    }
}
