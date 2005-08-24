/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import org.netbeans.jmi.javamodel.Method;

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
    
    private Method me;
    public MethodType(Method me) {
        this.me = me;
    }
    
    public abstract void accept(MethodTypeVisitor visitor);
    public final Method getMethodElement() {
        return me;
    }
    
    public interface MethodTypeVisitor {
        void visit(BusinessMethodType bmt);
        void visit(CreateMethodType cmt);
        void visit(HomeMethodType hmt);
        void visit(FinderMethodType fmt);
    }
    
    public static class BusinessMethodType extends MethodType {
        public BusinessMethodType(Method me) {
            super(me);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    public static class SelectMethodType extends MethodType {
        public SelectMethodType(Method me) {
            super(me);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            assert false:"select methods are not intended to be visited";
        }
        
    }
    
    public static class CreateMethodType extends MethodType {
        public CreateMethodType(Method me) {
            super(me);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    public static class HomeMethodType extends MethodType {
        public HomeMethodType(Method me) {
            super(me);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    public static class FinderMethodType extends MethodType {
        public FinderMethodType(Method me) {
            super(me);
        }
        
        public void accept(MethodTypeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
