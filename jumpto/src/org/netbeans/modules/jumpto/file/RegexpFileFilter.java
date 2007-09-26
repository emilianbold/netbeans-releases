package org.netbeans.modules.jumpto.file;
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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
            filterText = removeRegexpEscapes(filterText);
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

    private static String removeRegexpEscapes(String text) {
        StringBuilder sb = new StringBuilder();
       
        for( int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           switch(c) {
           case '\\':
//           case '(':
//           case ')':
//           case '{':
//           case '}':
//           case '[':
//           case ']':
                continue;
           default:
               sb.append(c);
           }                      
        }
        return sb.toString();
    }
    
}
