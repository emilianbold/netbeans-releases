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

package org.netbeans.modules.groovy.support.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** 
 * Support for parsing Groovy output.
 *
 * @author Petr Hamernik
 */
public class GroovyErrorOutputSupport {

    private static final String EXT = ".groovy"; // NOI18N
    private static final String CAUGHT = "Caught: "; // NOI18N
    
    private static int findNumberBetween( String str, String left, String right ) {
        int indexL = str.indexOf( left );
        if ( indexL != -1 ) {
            int x0 = indexL + left.length();
            int indexR = str.indexOf( right, x0 );
            if ( indexR != -1 ) {
                String numberSubstring = str.substring( indexL + left.length(), indexR ).trim();
                try {
                    int number = Integer.parseInt( numberSubstring );
                    return number;
                }
                catch ( NumberFormatException exc ) {
                }
            }
        }
        return -1;
    }
    
    /** Try to parse the line from groovy output. If exception is found,
     * it should parse the filename and the position of problem
     * 
     * Groovy exceptions differ in the printed messages. The following formats
     * are currently supported:
     * <pre>
     * <ul>
     * <li>C:\\temp\\MyGroovy.groovy:13:7: unexpected char: '\\'
     * <li>Caught: C:\\temp\\MyGroovy.groovy: 43: Unknown type: SCOPE_ESCAPE at line: 43 column: 19. File: C:\\temp\\MyGroovy.groovy @ line 43, column 19.
     * <li>Node: org.codehaus.groovy.ast.expr.BinaryExpression. At [3:9] C:\\temp\\MyGroovy.groovy
     * <li>/tmp/MyGroovy.groovy:13:7: unexpected char: '\\'
     * <li>Caught: /tmp/MyGroovy.groovy: 43: Unknown type: SCOPE_ESCAPE at line: 43 column: 19. File: /tmp/MyGroovy.groovy @ line 43, column 19.
     * <li>Node: org.codehaus.groovy.ast.expr.BinaryExpression. At [3:9] /tmp/MyGroovy.groovy
     * <li>org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed, src/foo.groovy: 2: expecting EOF, found 's' @ line 2, column 28.
     * <li>org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed, /tmp/foo.groovy: 2: expecting EOF, found 's' @ line 2, column 28.
     * </ul>
     */
    public static HyperlinkData checkErrorLine(String line, FileObject fileObject) {
        
        String fileName = null;
        String message = line;
        int line1 = -1;
        int col1 = -1;
        
        if (line.startsWith("org.codehaus.groovy.control.MultipleCompilationErrorsException")) {
            
            StringTokenizer tokenizer = new StringTokenizer(line, ":", false);
            List<String> parts = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                parts.add(tokenizer.nextToken());
            }
            
            String fileNameToken = parts.get(1);
            fileName = fileNameToken.substring(fileNameToken.indexOf(",") + 2);
            
            StringBuffer messageToken = new StringBuffer(parts.get(3));
            for (int i = 4; i < parts.size(); i++) {
                messageToken.append(':');
                messageToken.append(parts.get(i));
            }
            message = messageToken.toString();
            
            line1 = findNumberBetween(message, "line", ",");
            col1 = findNumberBetween(message, "column", ".");

        } else {

            if (line.startsWith(CAUGHT)) {
                line = line.substring(CAUGHT.length());
            }

            int extIndex = line.indexOf( EXT );
            if ( extIndex == -1 )
                return null;

            if ( line.startsWith( "Node: " ) ) {
                int beforeCoords = line.indexOf( " At [" );
                if ( beforeCoords == -1 )
                    return null; 
                line1 = findNumberBetween( line, " At [", ":" );
                col1 = findNumberBetween( line.substring(beforeCoords), ":", "]" );
                int beforeFileName = line.indexOf("] ", beforeCoords);
                if ( beforeFileName == -1 )
                    return null;
                fileName = line.substring ( beforeFileName + 2 );
            }
            else {
                fileName = line.substring (0, extIndex + EXT.length());
                line = line.substring( fileName.length() );
                line1 = findNumberBetween( line, ":", ":" );
                col1 = findNumberBetween( line.substring(1), ":", ":" );
                if ( col1 == -1 ) {
                    col1 = findNumberBetween( line, "column: ", "." );
                }
                else {
                    line = line.substring( line.indexOf(Integer.toString(line1) ) );
                }
                int colon2 = line.indexOf(":",1);
                message = line.substring(colon2 + 1);
                if (message.length () == 0) {
                    message = null;
                }
            }
        }
        
        File file = FileUtil.normalizeFile(new File(fileName));
        if (!file.exists()) {
            FileObject currentDir = FileOwnerQuery.getOwner(fileObject).getProjectDirectory();
            FileObject executedFile = currentDir.getFileObject(fileName);
            if (executedFile != null) {
                file = FileUtil.normalizeFile(FileUtil.toFile(executedFile));
            }
            if (file != null && !file.exists()) {
                return null;
            }
        }

//        System.out.println("Message:"+message);
//        System.out.println(" file ="+fileName);
//        System.out.println(" line1="+line1);
//        System.out.println(" col1 ="+col1);
        
        return new HyperlinkData( file, message, line1, col1, line1, col1);
    }

    /** Grouping of information about hyperlink. Used in checkErrorLine() method */
    public static class HyperlinkData {
        public File file;
        public String message;
        public int line1;
        public int column1;
        public int line2;
        public int column2;
        
        private HyperlinkData( File file, String message, int line1, int column1, int line2, int column2 ) {
            this.file = file;
            this.message = message;
            this.line1 = line1;
            this.column1 = column1;
            this.line2 = line2;
            this.column2 = column2;
        }
    }

}
