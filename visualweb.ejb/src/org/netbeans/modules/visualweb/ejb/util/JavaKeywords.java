/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
/*
 * JavaKeywords.java
 *
 * Created on February 9, 2005, 1:05 PM
 */

package org.netbeans.modules.visualweb.ejb.util;

import java.util.ArrayList;

/**
 * List all the Java keywords and reserved words
 * @author  cao
 */
public class JavaKeywords {
    
    private static ArrayList keywords;
    
    static {
        // NOI18N
        keywords = new ArrayList();
        keywords.add( "abstract" );
        keywords.add( "assert" );
        keywords.add( "boolean" );
        keywords.add( "byte" );
        keywords.add( "case" );
        keywords.add( "catch" );
        keywords.add( "char" );
        keywords.add( "class" );
        keywords.add( "const" );
        keywords.add( "continue" );
        keywords.add( "default" );
        keywords.add( "do" );
        keywords.add( "double" );
        keywords.add( "else" );
        keywords.add( "enum" );
        keywords.add( "extends" );
        keywords.add( "final" );
        keywords.add( "float" );
        keywords.add( "for" );
        keywords.add( "goto" );
        keywords.add( "if" );
        keywords.add( "implements" );
        keywords.add( "import" );
        keywords.add( "instanceof" );
        keywords.add( "int" );
        keywords.add( "interface" );
        keywords.add( "long" );
        keywords.add( "native" );
        keywords.add( "new" );
        keywords.add( "package" );
        keywords.add( "private" );
        keywords.add( "protected" );
        keywords.add( "public" );
        keywords.add( "return" );
        keywords.add( "short" );
        keywords.add( "static" );
        keywords.add( "strictfp" );
        keywords.add( "super" );
        keywords.add( "switch" );
        keywords.add( "synchronized" );
        keywords.add( "this" );
        keywords.add( "throw" );
        keywords.add( "throws" );
        keywords.add( "transient" );
        keywords.add( "try" );
        keywords.add( "void" );
        keywords.add( "volatile" );
        keywords.add( "while" );
        // reserved words
        keywords.add( "true" );
        keywords.add( "false" );
        keywords.add( "null" );
    }
    
    public static boolean isKeyword( String name )
    {
        if( keywords.contains( name ) )
            return true;
        else
            return false;
    }
    
}
