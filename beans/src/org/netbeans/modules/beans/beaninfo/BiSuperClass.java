/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.lang.reflect.Modifier;
import org.netbeans.modules.beans.PatternAnalyser;

import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.Type;
import org.openide.ErrorManager;

/** Singleton - utility class


 @author Petr Hrebejk
*/
class BiSuperClass extends Object {


    /** Creates a ClassElement containing all methods from classElement and it's superclasses */

    static ClassElement createForClassElement( ClassElement classElement ) {
        ClassElement result = new ClassElement();

        try {
            result.setName( classElement.getName() );
        }
        catch ( org.openide.src.SourceException e ) {
            ErrorManager.getDefault().notify(e);
        }

        ClassElement ce = classElement;
        int methodsAdded = 0;           // Workaround for getMethd


        while ( ce != null && !ce.getName().getFullName().equals("java.lang.Object")) { // NOI18N
            MethodElement[] methods = ce.getMethods();

            for( int i = 0; i < methods.length; i++ )  {

                if ( ( methods[i].getModifiers() & Modifier.PUBLIC ) == 0 )
                    continue;

                if ( methodsAdded == 0 || result.getMethod( methods[i].getName(), getParameterTypes( methods[i] ) ) == null ) {
                    try {
                        result.addMethod( methods[i] );
                        methodsAdded ++;
                    }
                    catch ( org.openide.src.SourceException e ) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
            
            ce = ce.getSuperclass() == null ? null : ClassElement.forName( ce.getSuperclass().getFullName(), PatternAnalyser.fileObjectForElement( classElement ) );
        }

        /*
        MethodElement[] methods = result.getMethods();
        for( int i = 0; i < methods.length; i++ ) 
          System.out.println ( methods[i].getName() );
        */
        return result;
    }

    /** Returns array of parameter types  */
    static Type[] getParameterTypes( MethodElement method ) {
        MethodParameter[] params = method.getParameters();

        Type[] result = new Type[ params.length ];

        for( int i = 0; i < params.length; i++ ) {
            result[i] = params[i].getType();
        }

        return result;
    }

}
