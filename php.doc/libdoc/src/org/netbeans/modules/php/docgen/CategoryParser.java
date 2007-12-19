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
class CategoryParser {
    
    public static final String INDEX                        =
                    "index";                                                // NOI18N
    
    private static final Pattern CATEGORY_PATTERN           = 
        Pattern.compile("<[ \t]*a[ \t]+name[ \t]*=[ \t]*['\"]?ref\\." , 
                Pattern.CASE_INSENSITIVE);                                  // NOI18N 
    
    private static final Pattern CATEGORY_NAME_PATTERN      =
        Pattern.compile("<[ \t]*h1[ \t]+class[^>]*>([^<]*)<\\/[ \t]*h1[ \t]*>" , 
                Pattern.CASE_INSENSITIVE);                                  // NOI18N
    
    private static final Pattern FUNCTION_ID_PATTERN        =
        Pattern.compile("<[ \t]*a[ \t]+name[ \t]*=[ \t]*['\"]?function\\." , 
                Pattern.CASE_INSENSITIVE);                                  // NOI18N
    
    private static final String   CONTENT        = "content.html";          // NOI18N
    
    private static final String   NAME           = "name";                  // NOI18N
    
    CategoryParser( String category , int index , File folder ){
        myContent = category ;
        myIndex = index;
        myFolder = folder;
    }

    void parse() throws WriteException{
        Matcher matcher = CATEGORY_PATTERN.matcher(getContent());
        if ( !matcher.find() ) {
            throw new IllegalStateException("Cannot identify category "+myIndex); // NOI18N
        }
        int index = matcher.end();
        int end = getContent().indexOf( CategoriesParser.CLOSE_BRACKET , index);
        String id = extractName(getContent(), index, end);
        File file = new File( getFolder() , id );
        file.mkdirs();
        saveContent(file);
        saveName( file , index);
        
        appendToIndex( id );
        
        parseFunctions( file , index );
    }

    private void appendToIndex( String id ) throws WriteException {
        File indexFile = new File( getFolder() , INDEX );
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new FileWriter( indexFile ,  true ));
                writer.write( id );
                writer.write('\n');
        }
        catch (IOException e) {
            throw new WriteException("Couldn't write to index file" , e);
        }
        finally {
            if ( writer != null ) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    throw new WriteException( "Couldn't close index file", e );
                }
            }
        }
    }

    private void parseFunctions( File file, int index ) throws WriteException {
        String content = getContent().substring(index);
        Matcher matcher = FUNCTION_ID_PATTERN.matcher( content );
        int previousStart = -1;
        String nameId = null;
        String methodContent = null;
        
        while ( matcher.find() ) {
            int start = matcher.end();
            int patternEnd = content.indexOf( CategoriesParser.CLOSE_BRACKET , start);
            String namePretender = extractName(content, start, patternEnd);
            if ( namePretender.contains( "." ) ) {
                continue;
            }
            if ( previousStart >0 ) {
                int end = matcher.start();
                methodContent = content.substring( previousStart , end );
                MethodParser parser = new MethodParser( nameId , methodContent , 
                        file );
                parser.parse();

            }
            nameId = namePretender;
            previousStart = patternEnd+1;
        }
        
        /*
         * Handle last function in category.
         */
        if ( previousStart > 0 ) {
            methodContent = content.substring( previousStart );
            MethodParser parser = new MethodParser( nameId , methodContent , file );
            parser.parse();
        }
    }

    private String extractName( String content, int start, int patternEnd ) {
        String name = content.substring(start, patternEnd).trim();
        if ( name.endsWith( "'") || name.endsWith( "\"")) {
            name = name.substring( 0, name.length() -1 );
        }
        return name;
    }

    private void saveName( File categoryDir , int index ) throws WriteException {
        Matcher matcher = CATEGORY_NAME_PATTERN.matcher(
                getContent().substring(index));
        if ( !matcher.find() ) {
            throw new IllegalStateException("Cannot identify category "+myIndex); // NOI18N
        }
        
        String name = matcher.group( 1 );
        
        File file = new File( categoryDir , NAME );
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file ));
            writer.write( name );
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
                    e.printStackTrace();
                }
            }
        }        
    }

    private void saveContent( File categoryDir ) throws WriteException {
        File file = new File( categoryDir , CONTENT );
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file ));
            writer.write( getContent() );
        }
        catch (IOException e) {
            throw new WriteException( "Couldn't write file "+ file  , e );
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
    
    private File getFolder() {
        return myFolder;
    }
    
    private String getContent() {
        return myContent;
    }
    
    private String myContent;
    
    private int myIndex;
    
    private File myFolder;
}
