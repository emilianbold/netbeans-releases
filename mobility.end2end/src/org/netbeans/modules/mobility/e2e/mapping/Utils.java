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
            return "Boolean.parseBoolean( " + variable + " )";
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
