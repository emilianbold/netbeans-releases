package org.netbeans.modules.jumpto.file;
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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * Contributor(s): Petr Hrebejk
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Nice filter which based on the text finds out how to search.
 *
 * @author phrebejk
 */
public class RegexpFileFilter implements FileFilter, FilenameFilter {
    
    private String filterText;
    private Pattern pattern;
    private boolean caseSensitive;
    
    public RegexpFileFilter(String filterText, boolean caseSensitive) {
        
        // XXX Camel case
        
        this.caseSensitive = caseSensitive;
        
        if ( !filterText.contains("?") && !filterText.contains("*") ) {
            this.filterText = caseSensitive ? filterText : filterText.toLowerCase();
        }
        else {            
            filterText = filterText.replace(".", "\\."); // NOI18N
            filterText = filterText.replace("?", "."); // NOI18N
            filterText = filterText.replace("*", ".*"); // NOI18N       
            this.filterText = filterText;
            this.pattern = Pattern.compile(this.filterText, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
        }
    }

    public boolean accept(File file) {
        return accept( null, file.getName() );        
    }

    public boolean accept(File dir, String name) {
        
        if ( pattern == null ) {
           return caseSensitive ? 
                        name.startsWith(filterText) :
                        name.toLowerCase().startsWith(filterText); 
        }
        else {
            Matcher m = pattern.matcher(name);
            return m.matches();
        }
        
    }
    
    public static boolean onlyContainsWildcards(String s) {
       for( int i = 0; i < s.length(); i++ ) {
           char ch =  s.charAt(i);
           switch( ch ) {
               case '*':  // NOI18N
               case '?':  // NOI18N
               case '.':  // NOI18N
                   continue;
               default:
                   return false;
           }
       }
       return true;
    }

}
