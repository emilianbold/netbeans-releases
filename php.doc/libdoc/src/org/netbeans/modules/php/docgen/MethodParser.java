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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author ads
 *
 */
class MethodParser {

    private static final String HTML_POSTFIX    = ".html";          // NOI18N
        
    private static final String METHOD_POSTFIX    = ".method";      // NOI18N

    /*
     * Head html tag.
     */
    private static final String H               = "h";              // NOI18N

    private static final Pattern FUNCTION_NAME_PATTERN =
        Pattern.compile("</[ \t]*a[ \t]*>([^<]*)<\\/[ \t]*h([^>]*)>" , 
                Pattern.CASE_INSENSITIVE);                          // NOI18N
    
    private static final String TYPE_PATTERN     = 
                "[\\s>]*([^\\s>]*)\\s*<b\\s+class\\s*=\\s*" +
                "\"methodname\""+"\\s*>\\s*";                       // NOI18N
    
    private static final String ARGS_PATTERN    = 
                "\\s*</b\\s*>\\s*(\\([^\\)]*\\))";                  // NOI18N
    
    
    MethodParser( String methodNameId , String content, File folder ){
        myNameId = methodNameId;
        myContent = content.trim();
        myFolder = folder;
        
        initContent();
    }
    
    void parse() throws WriteException {
        File folder = getFolder();
        File file = new File( folder , getFileName() );
        
        addToIndex();
        
        writeMethod();
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file ));
            writer.write( getContent() );
        }
        catch (IOException e) {
            throw new WriteException( "Couldn't write file "+ file , e );
        }
        finally {
            if ( writer != null ) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    throw new WriteException( "Couldn't close file " +file , e);
                }
            }
        }    
    }


    private void writeMethod() throws WriteException {
        writeMethodField( myDocName );
        writeMethodField( myType );
        writeMethodField( myArguments );
    }
    
    private void writeMethodField( String field) throws WriteException {
        File file = new File(getFolder(), getMethodName() );
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file ,true ) );
            if ( field != null ){
                writer.write( field );
            }
            writer.newLine();
        }
        catch (IOException e) {
            throw new WriteException("Couldn't write method structure file for " + // NOI18N
                    myNameId +" method ", e);
        }
        finally {
            try {
                writer.close();
            }
            catch (IOException e) {
                throw new WriteException("Couldn't close method name file for " + // NOI18N
                        myNameId, e);
            }
        }
    }  

    private void addToIndex() throws WriteException {
        File file = new File( getFolder() , CategoryParser.INDEX );
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file , true) );
            writer.write( getFileName() );
            writer.write( '\n' );
        }
        catch (IOException e) {
            throw new WriteException( "Couldn't write index file for " +  // NOI18N
                    getFolder().getName() , e );
        }
        finally {
            try {
                writer.close();
            }
            catch (IOException e) {
                throw new WriteException( "Couldn't close index file for "+// NOI18N 
                        getFolder().getName() , e  );
            }
        }
    }

    private void initContent() {
        Matcher matcher = FUNCTION_NAME_PATTERN.matcher(getContent());
        if ( !matcher.find() || matcher.start()!= 0) {
            throw new IllegalStateException("Unrecognized method format found, " +
                    "method " +getNameId());
        }
        int end = matcher.end();
        StringBuilder builder = new StringBuilder();
        String name = matcher.group( 1 ).trim();
        String headNumber = matcher.group( 2 ).trim();
        builder.append( CategoriesParser.OPEN_BRACKET );
        builder.append( H );
        builder.append( headNumber );
        builder.append(  CategoriesParser.CLOSE_BRACKET );
        builder.append( name );
        builder.append( CategoriesParser.OPEN_BRACKET );
        builder.append( '/' );
        builder.append( H );
        builder.append( headNumber );
        builder.append(  CategoriesParser.CLOSE_BRACKET );
        builder.append( getContent().substring( end  ) );
        myContent = builder.toString();
        
        Pattern pattern = Pattern.compile( TYPE_PATTERN + name +
                ARGS_PATTERN , Pattern.CASE_INSENSITIVE); 
        matcher = pattern.matcher( getContent() );
        if( matcher.find() ){
            myType = matcher.group( 1 ).trim();
            myArguments = matcher.group( 2 ).trim(); 
        }
        
        myDocName = name;
    }

    private String getFileName() {
        return getNameId()+HTML_POSTFIX;
    }
    
    private String getMethodName() {
        return getNameId()+METHOD_POSTFIX;
    }
    
    private String getNameId() {
        return myNameId;
    }
    
    private String getContent() {
        return myContent;
    }
    
    private File getFolder() {
        return myFolder;
    }
    
    private String myNameId;
    
    private String myContent;
    
    private File myFolder;
    
    private String myDocName;

    private String myType;
    
    private String myArguments;
}
