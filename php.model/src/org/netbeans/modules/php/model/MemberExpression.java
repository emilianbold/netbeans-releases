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
 * Represent access to member .
 * It could be Array member access, Class member
 * ( method or attribute ).  See subinterfaces.
 * 

 * 
 * @author ads
 *
 */
public interface MemberExpression extends IdentifierExpression {

    /**
     * <pre>
     * This method is mutually exclusive with {@link #getOwnerIdentifier()}.
     * If this method returns not null the later method returns null.
     * MemberExpression class can contain either Call expression
     * or Member expression , but not both.
     * 
     * Returns next member in chain ( if this chain exists ) :
     * $a   ----------->  op() ---------->  op2 ();
     * ^^^^^^^^^^^^^^^^^^^^^^^             ^^^^^  
     *      method returns               Expression
     * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
     *            this member expr                       
     * </pre>
     * 
     * @return call expression if it exist
     */
    CallExpression getCallExpression();
    
    /**
     * <pre>
     * This method is mutually exclusive with {@link #getCallExpression()}.
     * If this method returns not null the later method returns null.
     * MemberExpression class can contain either Call expression
     * or identifier expression ( identifier expression can be 
     * simple expression like variable and can be again member expression), 
     * but not both.
     * 
     * Returns next member in chain ( if this chain exists ) :
     * 
     * $a      [$j++]      [ $i ++ ];
     * ^^^^^^^^^^^^^^        ^^^^^^
     * $a ----> $var       [   0   ];
     * ^^^^^^^^^^^^^^         ^^^^     
     * method returns       Expression
     * 
     *       $a            [   0   ];
     * ^^^^^^^^^^^^^^         ^^^^     
     * method returns       Expression
     * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
     *      this member exp
     * </pre>
     * @return member expression if it exist
     */
    IdentifierExpression getOwnerIdentifier();
}
