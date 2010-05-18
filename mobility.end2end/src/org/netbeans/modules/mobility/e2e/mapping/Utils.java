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

package org.netbeans.modules.mobility.e2e.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.JavonProfileProvider;

import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Michal Skvor
 */
public class Utils {

    private ClassDataRegistry registry;

    /** Creates a new instance of Utils 
     * @param registry
     */
    public Utils( ClassDataRegistry registry ) {
        this.registry = registry;
    }

    public static String parsePrimitiveType( String typeName, String variable ) {
        if( "java.lang.String".equals( typeName )) {
            return variable;
        }
        if( "int".equals( typeName )) { 
            return "Integer.parseInt( " + variable + " )";
        } else if( "java.lang.Integer".equals( typeName )) {
            return "new Integer(Integer.parseInt( " + variable + " ))";
        }
        if( "boolean".equals( typeName )) {
            return "\"true\".equals( " + variable + " )";
        } else if( "java.lang.Boolean".equals( typeName )) {
            return "new Boolean( \"true\".equals( " + variable + " ))";
        }
        if( "byte".equals( typeName )) {
            return "Byte.parseByte( " + variable + " )";
        } else if( "java.lang.Byte".equals( typeName )) {
            return "new Byte(Byte.parseByte( " + variable + " ))";
        }
        if( "long".equals( typeName )) {
            return "Long.parseLong( " + variable + " )";
        } else if( "java.lang.Long".equals( typeName )) {
            return "new Long(Long.parseLong( " + variable + " ))";
        }
        if( "short".equals( typeName )) {
            return "Short.parseShort( " + variable + " )";
        } else if( "java.lang.Short".equals( typeName )) {
            return "new Short(Short.parseShort( " + variable + " ))";
        }
        
        if( "float".equals( typeName )) {
            return "Float.parseFloat( " + variable + " )";
        } else if( "java.lang.Float".equals( typeName )) {
            return "new Float(Float.parseFloat( " + variable + " ))";
        }
        if( "double".equals( typeName )) {
            return "Double.parseDouble( " + variable + " )";
        } else if( "java.lang.Double".equals( typeName )) {
            return "new Double(Double.parseDouble( " + variable + " ))";
        }
        
        return "";
    }
}
