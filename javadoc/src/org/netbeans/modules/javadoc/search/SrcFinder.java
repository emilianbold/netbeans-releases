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

package org.netbeans.modules.javadoc.search;

import java.net.URL;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.cookies.SourceCookie;
import org.openide.src.*;


/** This class finds the source to show it instead of documentation.
 *
 * @author  Administrator
 * @version 
 */
public final class SrcFinder extends Object {

    private static final ElementFormat FORMATOR = new ElementFormat( "{C}" ); // NOI18N

    /** SrcFinder is a singleton */
    private  SrcFinder() {
    }

    static Element findSource( String aPackage, URL url  ) {

        aPackage = aPackage.replace( '.', '/' ); // NOI18N
        String thePackage = null;
        String member = url.getRef(); 
        String clazz = url.getFile();
        String filename = null;

        int pIndex;
        
        if ( ( pIndex = clazz.toLowerCase().indexOf( aPackage.trim().toLowerCase() ) ) != -1 ) {
            thePackage = clazz.substring(pIndex, pIndex + aPackage.trim().length()  - 1 );
            clazz = clazz.substring( pIndex + aPackage.trim().length(), clazz.length() - 5 );

            int ei;
            if ( ( ei = clazz.indexOf('.')) != -1 ) {
                filename = clazz.substring(0, ei );
            }
            else
                filename = clazz;

        }
        
//        System.out.println("================================");
//        System.out.println("URL     :" + url   );
//        System.out.println("aPCKG   :" + aPackage );
//        System.out.println("--------------------------------");
//        System.out.println("MEMBER  :" + member ); // NOI18N
//        System.out.println("CLASS   :" + clazz ); // NOI18N
//        System.out.println("PACKAGE :" + thePackage ); // NOI18N
//        System.out.println("FILENAME:" + filename ); // NOI18N

        String resourceName = thePackage + "/" + filename + ".java"; // NOI18N
        FileObject fo = searchResource(url, resourceName);
        
        if ( fo != null ) {
            try {        
                DataObject dobj = DataObject.find( fo );

                SourceCookie sc = (SourceCookie)dobj.getCookie( SourceCookie.class );
                ClassElement classes[] = sc.getSource().getAllClasses();

                // Search all classes
                for ( int i = 0; i < classes.length; i++ ) {
                    String outerName = FORMATOR.format( classes[i] );
                    if ( clazz.equals( outerName ) ) {
                        if ( member == null )
                            // We are looking for class
                            return classes[i];
                        else {

                            int pi = member.indexOf( '(' );
                            if ( pi == -1 ) {
                                // we are looking for fiels
                                FieldElement fe = classes[i].getField( Identifier.create( member ) );
                                return fe;
                            }
                            else {
                                // We are looking for method or constructor
                                return getMethod( classes[i], member );
                            }

                        }
                    }
                }


                return null;
            }
            catch ( org.openide.loaders.DataObjectNotFoundException e ) {
                System.out.println( e );
            }
        }
        return null;
    }

    /**
     * searches the file corresponding to javadoc url on all source path.
     * {@link GlobalPathRegistry#findResource}
     * is insufficient due to returning just the first occurrence of the file. So having
     * two platforms installed would bring troubles.
     * @param url javadoc
     * @param respath resource in form java/lang/Character.java
     * @return the file
     */ 
    private static FileObject searchResource(URL url, String respath) {
        FileObject res = searchBinaryPath(ClassPath.BOOT, respath, url);
        
        if (res == null) {
            res = searchBinaryPath(ClassPath.COMPILE, respath, url);
        }
        
        if (res == null) {
            res = searchSourcePath(respath, url);
        }
        
        return res;
        
    }

    private static FileObject searchBinaryPath(String classPathID, String respath, URL url) {
        Set/*<ClassPath>*/ cpaths = GlobalPathRegistry.getDefault().getPaths(classPathID);
        for (Iterator it = cpaths.iterator(); it.hasNext();) {
            ClassPath cpath = (ClassPath) it.next();
            FileObject[] cpRoots = cpath.getRoots();
            for (int i = 0; i < cpRoots.length; i++) {
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(URLMapper.findURL(cpRoots[i], URLMapper.EXTERNAL));
                FileObject[] srcRoots = result.getRoots();
                for (int j = 0; j < srcRoots.length; j++) {
                    FileObject fo = srcRoots[j].getFileObject(respath);
                    if (fo != null && isJavadocAssigned(cpath, url)) {
                        return fo; 
                    }
                }
            }
        }
        return null;
    }

    private static FileObject searchSourcePath(String respath, URL url) {
        Set/*<ClassPath>*/ cpaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        for (Iterator it = cpaths.iterator(); it.hasNext();) {
            ClassPath cpath = (ClassPath) it.next();
            FileObject fo = cpath.findResource(respath);
            if (fo != null && isJavadocAssigned(cpath, url)) {
                return fo;
            }
        }
        
        return null;
    }
    
    /**
     * checks if the javadoc url is assigned to a given classpath 
     * @param cpath classpath
     * @param url javadoc
     * @return is assigned?
     */ 
    private static boolean isJavadocAssigned(ClassPath cpath, URL url) {
        FileObject[] cpRoots = cpath.getRoots();
        String urlPath = url.toExternalForm();
        for (int i = 0; i < cpRoots.length; i++) {
            JavadocForBinaryQuery.Result result = JavadocForBinaryQuery.findJavadoc(URLMapper.findURL(cpRoots[i], URLMapper.EXTERNAL));
            URL[] jdRoots = result.getRoots();
            for (int j = 0; j < jdRoots.length; j++) {
                String jdRootPath = jdRoots[j].toExternalForm();
                if (urlPath.indexOf(jdRootPath) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Gets the method we are looking for
     */
    private static ConstructorElement getMethod( ClassElement ce, String member) {


        int pi = member.indexOf( '(' );
        String name = member.substring( 0, pi );

        StringTokenizer tokenizer = new StringTokenizer( member.substring( pi ), " ,()" ); // NOI18N
        ArrayList paramList = new ArrayList();

        while( tokenizer.hasMoreTokens() ) {
            paramList.add( Type.parse( tokenizer.nextToken() ) );
        }

        Type[] params = new Type[ paramList.size() ];
        paramList.toArray( params );

        ConstructorElement result = ce.getConstructor( params );

        if ( result == null )
            result = ce.getMethod( Identifier.create( name ), params );


        /*
        if ( result == null ) {
          // We didn't found the method yet let's try other way
          
          findInArray( ce.getConstructors() );
          findInArray( ce.getMethods() );
          
    }
        */
        return result;
    }


    /*
    private static void findInArray( ConstructorElement[] cs ) {
      
      for ( int i = 0; i < cs.length; i++ ) {
        
        MethodParameter[] ps = cs[i].getParameters();
        
        for( int j = 0; j < ps.length; j++ ) {
          //System.out.println( ps[j].getType().toString() );
        }
        
      }
      
}
    */

}