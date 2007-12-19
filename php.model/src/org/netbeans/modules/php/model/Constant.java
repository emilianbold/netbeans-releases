/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.model;


/**
 * <pre>
 * Represent reference actually to some constant that was defined
 * before.
 *
 * It could be constant that was introduced via "define", 
 * could be reference to class constant : Clazz::CONST.
 * It could be function name ( defined by user or php function ).
 * 
 * The methods below are mutually exclusive. If one is not null,
 * then all other return null. So methods describes different 
 * cases constant appearance.
 * There is one case that is absent in represented methods :
 * when constant is method name in some class ( f.e. in expression $clazz -> method() ).
 * In this case Constant is used only for getting method name via
 * getText() without any other information. One can implement 
 * logic for access to definition of this method in some class 
 * manually .
 * ( It could be too complex problem actually. Even in simple case :
 * $clazz->method();
 * one need to find class definition for $clazz. If there was expression :
 * $clazz = new Class();
 * somewhere before then it is possible.
 * But there can be such ( actually simple code ):
 * $a = getA( $arg );
 * $a->method();
 * 
 * So there can be very complex logic for finding type for $a ( PHP mostly doesn't
 * have types  ! ) ).
 * </pre> 
 * @author ads
 *
 */
public interface Constant extends IdentifierExpression {

    /**
     * <pre>
     * Accessor to static object ( class or interface ) member .
     * It return not null reference only for expression in form :
     * Clazz::member
     * Clazz here is name of some class or interface , 
     * member here is constant name or ( static ) method name. 
     * </pre> 
     * @return reference to static member of class or interface determined from expression 
     */
    ClassReference<SourceElement> getClassConstant();
    
    /**
     * <pre>
     * Accessor to constant definition ( call expression ) or function 
     * definition.
     * It return not null referene only in cases :
     * 1) when <code>this</code> constant is reference to constant that was somehere 
     * defined in file.
     * Constant is defined with 'define' built-in php function via 
     * define(CONST , $value); 
     * so this is call expression.
     * 2) when constant represent user defined function.   
     * </pre>     
     * @return reference to call expression or function definition
     */
    Reference<SourceElement> getSourceElement();
    
}
