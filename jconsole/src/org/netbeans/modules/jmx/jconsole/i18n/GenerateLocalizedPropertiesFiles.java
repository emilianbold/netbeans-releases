/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 */

package org.netbeans.modules.jmx.jconsole.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author jfdenise
 */
public class GenerateLocalizedPropertiesFiles {

    /** Creates a new instance of Main */
    public GenerateLocalizedPropertiesFiles() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        String dirs = System.getProperty("jmx.i18n.properties.dirs");// NOI18N
        StringTokenizer tokenizer = new StringTokenizer(dirs, ",");// NOI18N
        String prefix = System.getProperty("jmx.i18n.prefix");// NOI18N
        String locale = System.getProperty("jmx.i18n.locale");// NOI18N
      while(tokenizer.hasMoreElements()) {
            generateFile(locale, prefix, tokenizer.nextToken());
      }
    }

    private static void generateFile(String locale,
            String prefix, String dir) throws Exception {
        File file = new File("." + File.separator + "src" + // NOI18N
                File.separator + dir + File.separator + "Bundle.properties");// NOI18N
        System.out.println("Reading file : " + file.getAbsolutePath());// NOI18N
        String newDir = "." + File.separator + "build" + // NOI18N
                File.separator + "locale" + File.separator + dir;// NOI18N
        String newFileName = newDir + File.separator + "Bundle_"+ locale + // NOI18N
                ".properties";// NOI18N
        File dirs = new File(newDir);
        dirs.mkdirs();
        File f = new File(newFileName);
        f.createNewFile();
        FileOutputStream output = 
                new FileOutputStream(f);
        
        //Load properties file
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(file);
        try {
            properties.load(fis);
        } finally {
            fis.close();
        }
        Properties newProperties = new Properties();
        Set<Entry<Object, Object>> entries = properties.entrySet();
        for(Entry entry : entries) {
            String key = (String)entry.getKey();
            
            String value = (String) entry.getValue();
            if(!key.endsWith("Template")) {// NOI18N
                //We are not translating Numbers
                try {
                    Integer.valueOf(value);
                }catch(Exception e) {
                    if(!("localhost".equals(value)))// NOI18N
                        value = parseEscapes(prefix) + entry.getValue();
                }
            }
            newProperties.setProperty(key, value);
        }
        System.out.println("Localized file : " + newFileName);// NOI18N
        newProperties.store(output, "Localized in " + locale);// NOI18N
        output.close();
    }
    
    public static String parseEscapes(CharSequence text) throws ParseException
    {
        // For each character...
        int          length = text.length();
        StringBuffer sb     = new StringBuffer(length);
        for (int iChar = 0; iChar < length;) {
            char ch = text.charAt(iChar++);
 
            // Handle escapes.
            if (ch == '\\') {
                if (iChar >= length) {
                    throw new ParseException("Orphaned escape character.",iChar);// NOI18N
                }
                ch = text.charAt(iChar++);
                if (ch == 'u') {
                    // A Unicode escape sequence.
                    if (iChar > length - 4) {
                        throw new ParseException("Malformed Unicode escape.",iChar);// NOI18N
                    }
                    int value = 0;
                    for (int iDigit = 4; iDigit > 0; iDigit--) {
                        int digitValue = Character.digit(text.charAt(iChar++), 16);
                        if (digitValue < 0) {
                            throw new ParseException("Malformed Unicode escape.",iChar);// NOI18N
                        }
                        value = (value << 4) + digitValue;
                    }
                    sb.append((char)value);
                } else {
                    // A standard escape sequence.
                    switch (ch) {
                        case 't' : ch = '\t'; break;// NOI18N
                        case 'r' : ch = '\r'; break;// NOI18N
                        case 'n' : ch = '\n'; break;// NOI18N
                        case 'f' : ch = '\f'; break;// NOI18N
                    }
                    sb.append(ch);
                }
                
                continue;
            }
 
            // An unescaped character.
            sb.append(ch);
        }
        
        return sb.toString();
    }
}
