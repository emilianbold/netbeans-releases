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
package org.netbeans.modules.php.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.netbeans.modules.php.doc.resources.DocCategoriesMarker;
import org.openide.ErrorManager;


/**
 * @author ads
 *
 */
public class FunctionDoc {

    /*private static final Pattern FUNCTION_NAME_PATTERN =
        Pattern.compile("<[ \t]*h[^>]+>([^<]*)<\\/[ \t]*h[^>]+>" ,      // NOI18N 
                Pattern.CASE_INSENSITIVE);*/

    private static final String COLON                       = ",";         // NOI18N

    private static final String POINTER_ENTITY_SEPARATOR    = "-&#62;";    // NOI18N 

    private static final String POINTER_OWNER_SEPARATOR     = "->";        // NOI18N 

    private static final String COLON_OWNER_SEPARATOR       = ":";         // NOI18N 
    
    private static final String RIGHT_PARENS                = ")";         // NOI18N

    private static final String LEFT_PARENS                 = "(";         // NOI18N

    private static final String OPTIONAL_PARAM_PREFIX = "[";      // NOI18N

    /*
     * This is border quantity of this documentation usage.
     * When usage bacomes more than this value string will 
     * be cached in memeory.    
     */
    private static final int BORDER_USAGE_QUANTITY          = 5;

    private static final String METHOD_POSTFIX              = ".method";  // NOI18N                                  

    FunctionDoc( String categoryId, String file , String id) {
        myFileName = file ;
        myId = id;
        myCategory = categoryId;
    }

    /**
     * @return internal id ( file name that contains documentation )
     */
    public String getId() {
        return myId;
    }
    
    /**
     * @return full function name ( the same string as in documentation header ).
     */
    public String getFullName() {
        if (myFullName != null) {
            return myFullName;
        }
        String filePath = DocCategoriesMarker.getCategoryPath(getCategory())
                + getId() + METHOD_POSTFIX;
        InputStream inputStream = FunctionDoc.class
                .getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line;
        
        byte counter = 0;
        try {
            while ( (line = reader.readLine()) != null ) {
                switch ( counter ) {
                    case 0: 
                        myFullName = line.trim();
                        break;
                    case 1: 
                        myReturnType = line.trim();
                        break;
                    case 2: 
                        myArguments = line.trim();
                        break;
                    default :
                        return myFullName;
                }
                counter++;
            }
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify( e );
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify( e );
            }
        }
        return myFullName;
    }
    
    public String getCategory() {
        return myCategory;
    }
    
    /**
     * @return short name ( only function name without class mention if any )
     */
    public String getName() {
        readName();
        return myMethodName;
    }
    
    public String getOwnerName(){
        readName();
        return myOwnerName;
    }
    
    public String getDocumentation() {
        return getContent();
    }
    
    public String getReturnType(){
        readName();
        return myReturnType;
    }
    
    public String getArgumentString(){
        readName();
        return myArguments;
    }
    
    public List<String> getArguments(){
        String args = getArgumentString();
        int indx = args.indexOf( LEFT_PARENS );
        if ( indx >= 0 && indx < args.length()-1 ) {
            args = args.substring( indx +1 );
        }
        indx = args.lastIndexOf( RIGHT_PARENS );
        if ( indx >= 0 ) {
            args = args.substring( 0, indx );
        }
        String requiredArgs = null;
        String optionalArgs = null;
        indx = args.indexOf(OPTIONAL_PARAM_PREFIX);
        if ( indx >= 0 ) {
            requiredArgs = args.substring( 0, indx );
            optionalArgs = args.substring( indx );
        }
        else {
            requiredArgs = args;
        }
        List<String> result = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer( requiredArgs , COLON );
        while ( tokenizer.hasMoreTokens() ) {
            String next = tokenizer.nextToken();
            result.add( next.trim() );
        }
        if(optionalArgs != null) {
            // It adds all rest optional args as one complex argument
            result.add( optionalArgs );
        }
        return result;
    }
    
    private String getContent() {
        if ( myContent != null ) {
            return myContent;
        }
        myUsageCount ++;
        
        String filePath = DocCategoriesMarker.getCategoryPath( getCategory() )
            + getFileName();
        InputStream inputStream = FunctionDoc.class.getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader( 
                new InputStreamReader( inputStream) );
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ( (line = reader.readLine()) != null ) {
                builder.append( line );
            }
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify( e );
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify( e );
            }
        }
        
        /*
         * TODO : myContent should be atomic reference
         * if ( myUsageCount > BORDER_USAGE_QUANTITY ) {
            myContent = builder.toString();
        }*/
        return builder.toString();
    }
    
    private void readName(){
        if ( isNameParsed ){
            return;
        }
        isNameParsed = true;
        String name = getFullName();
        myMethodName = name;
        if ( name.contains( COLON_OWNER_SEPARATOR )){
            int start = name.indexOf( COLON_OWNER_SEPARATOR );
            int end = name.lastIndexOf( COLON_OWNER_SEPARATOR );
            assert end +1<name.length();
            myMethodName = name.substring( end +1 );
            myOwnerName = name.substring( 0, start );
        }
        else if ( !initPointerSep( name , POINTER_ENTITY_SEPARATOR ) ) {
            initPointerSep( name, POINTER_OWNER_SEPARATOR);
        }
    }
    
    private boolean initPointerSep( String name, String sep ) {
        int start = name.indexOf(sep);
        if (start != -1) {
            myOwnerName = name.substring(0, start);
            start += sep.length();
            assert start  < name.length();
            myMethodName = name.substring(start) ;
            return true;
        }
        else {
            return false;
        }
    }
    
    private String getFileName() {
        return myFileName;
    }
    
    private String myId;
    
    private String myFullName;
    
    private String myFileName;
    
    private String myCategory; 
    
    /*
     * This counter is increased each time when #getContent()
     * method is called .
     * When quantity of usage belongs more then some special border value.
     * One need to cache documentation in class internal value. 
     */
    private int myUsageCount;
    
    private String myContent;
    
    private String myMethodName;
    
    private String myOwnerName;
    
    private boolean isNameParsed;
    
    private String myReturnType;
    
    private String myArguments;
}
