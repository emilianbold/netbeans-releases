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
package org.netbeans.modules.php.docgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * @author ads
 *
 */
class CategoriesParser {
    
    private static final String DIV                 = "div";                // NOI18N
    
    private static final String CLASS               = "class";              // NOI18N
    
    private static final String REFERENCE           = "reference";           // NOI18N
    
    public static final String OPEN_BRACKET        = "<";                  // NOI18N
    
    public static final String CLOSE_BRACKET       = ">";                  // NOI18N
    
    private static final String EQUAL               = "=";                  // NOI18N
    
    private static final String PART                = "part";               // NOI18N

    CategoriesParser( File file , File folder ) throws IOException{
        myFolder = folder;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader( new FileReader( file ));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append( line );
                builder.append(" ");
            }
        }
        finally {
            reader.close();
        }
        myContent = builder.toString();
        builder.delete( 0, builder.length());
        builder = null;
    }

    void parse() throws WriteException {
        int index = getContent().indexOf( OPEN_BRACKET );
        int start = -1;
        int count = 0;
        while ( index != -1 ) {
            int end = getContent().indexOf( CLOSE_BRACKET , index );
            if ( end != -1 ) {
                if ( findCategory( getContent().substring( index , end ),
                        REFERENCE) ) 
                {
                    parseCategory( start , index , count );
                    count ++;
                    start = index;
                }
            }
            index = getContent().indexOf( OPEN_BRACKET, end);
        }
        
        /* 
         * Flag for last reference section. Its end differs from end of
         * refrences in the middle, so we need special hanlder.  
         */
        boolean found = false;
        index = getContent().indexOf( CLOSE_BRACKET , start );
        while ( index != -1 ) {
            int end = getContent().indexOf( CLOSE_BRACKET , index );
            if ( end != -1 ) {
                if ( findCategory( getContent().substring( index , end ), PART )) 
                {
                    found = true;
                    parseCategory( start , index , count );
                    break;
                }
            }
            index = getContent().indexOf( OPEN_BRACKET, end);
        }
        
        myContent = null;
        if ( !found ) {
            throw new IllegalStateException( "Couldn't recognize last category"); // NOI18N
        }
    }

    private void parseCategory( int start, int end , int indx ) 
        throws WriteException 
    {
        if ( start > 0 ) {
            CategoryParser parser = new CategoryParser( 
                    getContent().substring( start , end ) , indx , getFolder() );
            parser.parse();
        }
    }

    private boolean findCategory( String tag , String classType ) {
        if ( tag.length() <=2 ) {
            return false ;
        }
        String tagContent = tag.substring( 1, tag.length() -1 ).trim().toLowerCase();
        if ( tagContent.startsWith( DIV )) {
            tagContent = tagContent.substring( DIV.length() ).trim();
            return findReference( tagContent , classType );
        }
        else {
            return false;
        }
    }
    private boolean findReference( String tagContent , String classType ) {
        if ( tagContent.startsWith( CLASS )) {
            String str = tagContent.substring( CLASS.length() ).trim();
            if ( !str.startsWith( EQUAL )) {
                return false;
            }
            str = str.substring( 1 );
            return str.equals( classType )|| str.equals( "'" + classType + "'")
                    || str.equals( '"' + classType +'"');
        }
        return false;
    }

    File getFolder() {
        return myFolder;
    }
    
    private String getContent() {
        return myContent;
    }
    
    private File myFolder; 
    
    private String myContent; 
}
