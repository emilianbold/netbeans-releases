/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.lib.cvsclient.command;

import org.netbeans.lib.cvsclient.util.SimpleStringPattern;
import org.netbeans.lib.cvsclient.ClientServices;

import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Support for <tt>.cvswrappers</tt> parsing and merging.
 */
public class WrapperUtils {

    /**
     * Reads the wrappers from the specified source and populates the specified
     * map
     *
     * @param reader The source of wrappers which is being processed
     * @param theMap The map which is being updated
     */
    private static  void parseWrappers(BufferedReader reader, Map theMap)
                 throws IOException {

        String line;
        while ((line = reader.readLine()) != null){
            StringTokenizer tokenizer = new StringTokenizer(line);

            // the first token is the pattern
            SimpleStringPattern pattern = new SimpleStringPattern(tokenizer.nextToken());

            // it is followed by option value pairs
            String option, value;

            while (tokenizer.hasMoreTokens()) {
                option = tokenizer.nextToken();
                value = tokenizer.nextToken();

                // do not bother with the -m Options now
                if (option.equals("-k")) { //NOI18N

                    // This is a keyword substitution option
                    // strip the quotes
                    int first = value.indexOf('\'');
                    int last = value.lastIndexOf('\'');
                    if (first >=0 && last >= 0) {
                        value = value.substring(first+1, last);
                    }

                    KeywordSubstitutionOptions keywordOption = KeywordSubstitutionOptions.findKeywordSubstOption(value);
                    if (!theMap.containsKey(pattern)) {
                        theMap.put(pattern, keywordOption);
                    }
                }
            }
        }
    }

    /**
     * Reads the wrappers from the specified file and populates the specified
     * map
     *
     * @param file The File object corresponding to the file which is being processed
     * @param wrapperMap The map which is being updated
     */
    public static void readWrappersFromFile(File file, Map wrapperMap) throws IOException, FileNotFoundException{
        parseWrappers(new BufferedReader(new FileReader(file)), wrapperMap);
    }

    /**
     * Reads the wrappers from the specified System property and populates the specified
     * map. The map is unchanged if the property is not set.
     *
     * @param envVar The system variable name
     * @param wrapperMap The map which is being updated
     */
    private static void readWrappersFromProperty(String envVar, Map wrapperMap) throws IOException {
        String propertyValue = System.getenv(envVar);
        if (propertyValue != null)
        {
            parseWrappers(new BufferedReader(new StringReader(propertyValue)), wrapperMap);
        }
        propertyValue = System.getProperty("Env-CVSWRAPPERS");
        if (propertyValue != null)
        {
            parseWrappers(new BufferedReader(new StringReader(propertyValue)), wrapperMap);
        }
    }

    /**
     * This method consolidates the wrapper map so that it follows CVS prioritization
     * rules for the wrappers. Both AddCommand and ImportCommand will be calling
     * this.
     */
    public static Map mergeWrapperMap(ClientServices client) throws CommandException
    {
        String wrapperSource = null;
        Map wrappersMap = new java.util.HashMap(client.getWrappersMap());
        try
        {
            File home = new File(System.getProperty("user.home"));  // NOI18N
            File wrappers = new File(home, "./cvswrappers"); //NOI18N

            wrapperSource = CommandException.getLocalMessage("WrapperUtils.clientDotWrapper.text"); //NOI18N

            if (wrappers.exists()) {
                readWrappersFromFile(wrappers, wrappersMap);
            }

            wrapperSource = CommandException.getLocalMessage("WrapperUtils.environmentWrapper.text"); //NOI18N

            //process the Environment variable CVSWRAPPERS
            readWrappersFromProperty("CVSWRAPPERS", wrappersMap);   //NOI18N
        }
        catch (FileNotFoundException fnex) {
            // should not happen as we check for file existence. Even if it does
            // it just means the .cvswrappers are not there and can be ignored
        }
        catch (Exception ioex) {
            Object [] parms = new Object[1];
            parms[0] = wrapperSource;
            String localizedMessage = CommandException.getLocalMessage("WrapperUtils.wrapperError.text", parms); //NOI18N
            throw new CommandException(ioex, localizedMessage);
        }

        return wrappersMap;
    }


}
