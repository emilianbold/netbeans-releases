/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el;

/**
 * Converts expression language expressions with respect to the given conversion table.
 * Allows to convert offset between the original and converted expressions.
 * Typical usage is conversion of the xml entity references inside the facelets expressions.
 *
 * @todo Make the class pluggable so the xml - jsf entities are not hardcoded.
 * 
 * @author marekfukala
 */
public class ELPreprocessor {
    
    /** 
     * Html entity references conversion table.
     */
    public static String[][] XML_ENTITY_REFS_CONVERSION_TABLE = new String[][]{ //NOI18N
        {"&amp;", "&"}, 
        {"&gt;", ">"}, 
        {"&lt;", "<"},
        {"&quot;", "\""},
        {"&apos;", "'"}
    };
    
    private final String originalExpression;
    private final String[][] conversionTable;
    
    private String preprocessedExpression;
    private final boolean[] diffs;
    
    public ELPreprocessor(String expression, String[][] conversionTable) {
        this.originalExpression = expression;
        this.conversionTable = conversionTable;
        this.diffs = new boolean[originalExpression.length()];
        init();
    }
    
    public String getOriginalExpression() {
        return originalExpression;
    }
    
    public String getPreprocessedExpression() {
        return preprocessedExpression;
    }
    
    public int getOriginalOffset(int preprocessedELoffset) {
        int diff = 0;
        for(int i = 0; i < originalExpression.length(); i++) {
            int pointer = i + diff;            
            if(pointer == preprocessedELoffset) {
                return i;
            }
            diff += diffs[i] ? -1 : 0;
        }
        //if we got here the offset points at the very end of the expression
        assert preprocessedELoffset == preprocessedExpression.length(); //or there's a bug
        return originalExpression.length(); //last offset handled here
    }
    
    public int getPreprocessedOffset(int originalOffset) {
        int diff = 0;
        for(int i = 0; i < originalOffset; i++) {
            diff += diffs[i] ? -1 : 0;
        }
        return originalOffset + diff;
    }

    //the algorithm is far from the most effective one, but for relatively small
    //set of the patterns the complexity is acceptable
    private void init() {
        String result = originalExpression;
        for(String[] patternPair : conversionTable) {
            StringBuilder resolved = new StringBuilder();
            String source = patternPair[0];
            String dest = patternPair[1];
            
            int match;
            int lastMatchEnd = 0;
            while((match = result.indexOf(source, lastMatchEnd)) != -1) {
                resolved.append(result.substring(lastMatchEnd, match));
                resolved.append(dest);
                int patternsLenDiff = source.length() - dest.length();
                for(int i = match; i < match + patternsLenDiff; i++) {
                    diffs[i] = true;
                }
                
                lastMatchEnd = match + source.length();
            }
            resolved.append(result.substring(lastMatchEnd));
            
            result = resolved.toString();
        }
        this.preprocessedExpression = result;
    }
    
}
