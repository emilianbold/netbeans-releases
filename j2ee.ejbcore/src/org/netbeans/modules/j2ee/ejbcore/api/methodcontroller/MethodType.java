/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 * Provide simple instance of the visitor pattern to use for code generation.
 * @author Chris Webster
 */
public abstract class MethodType {

    public static final int METHOD_TYPE_BUSINESS = 1;
    public static final int METHOD_TYPE_SELECT = 2;
    public static final int METHOD_TYPE_CREATE = 3;
    public static final int METHOD_TYPE_FINDER = 4;
    public static final int METHOD_TYPE_HOME = 5;

    private final ElementHandle<ExecutableElement> methodHandle;
    
    public MethodType(ElementHandle<ExecutableElement> methodHandle) {
        this.methodHandle = methodHandle;
    }
    
    public abstract void accept(MethodTypeVisitor visitor);
    
    public final ElementHandle<ExecutableElement> getMethodElement() {
        return methodHandle;
    }
    
    public interface MethodTypeVisitor {
        void visit(BusinessMethodType bmt);
        void visit(CreateMethodType cmt);
        void visit(HomeMethodType hmt);
        void visit(FinderMethodType fmt);
    }
    
    public static class BusinessMethodType extends MethodType {
        public BusinessMethodType(ElementHandle<ExecutableElement> methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    public static class SelectMethodType extends MethodType {
        public SelectMethodType(ElementHandle<ExecutableElement> methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            assert false:"select methods are not intended to be visited";
        }
        
    }
    
    public static class CreateMethodType extends MethodType {
        public CreateMethodType(ElementHandle<ExecutableElement> methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    public static class HomeMethodType extends MethodType {
        public HomeMethodType(ElementHandle<ExecutableElement> methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    public static class FinderMethodType extends MethodType {
        public FinderMethodType(ElementHandle<ExecutableElement> methodHandle) {
            super(methodHandle);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
