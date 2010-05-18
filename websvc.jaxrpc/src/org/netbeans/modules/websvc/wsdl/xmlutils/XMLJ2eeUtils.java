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

package org.netbeans.modules.websvc.wsdl.xmlutils;

import java.util.*;


/** Static methods useful for XMLJ2eeDataObject.
 *
 * @author  mkuchtiak
 */

public class XMLJ2eeUtils {

    /** This method updates document in editor with newDoc, but leaves the text before prefixMark.
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
    public static void updateDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();        
        String origDoc = doc.getText(0, origLen);;
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origLen-=prefixInd;
                origDoc=doc.getText(prefixInd,origLen);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
            newDoc=newDoc.substring(prefixIndNewDoc);
        }
        //newDoc=filterEndLines(newDoc);
        int newLen = newDoc.length();
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }

        final int granularity = 20;
        
        int prefix = -1;
        int postfix = -1;
        String toInsert = newDoc;
        
        if ((origLen > granularity) && (newLen > granularity)) {
            int pos1 = 0;
            int len = Math.min(origLen, newLen);
            // find the prefix which both Strings begin with
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos1, granularity)) {
                    pos1 += granularity;
                    if (pos1 + granularity >= len) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            if (pos1 > 0)
                prefix = pos1;
            
            pos1 = origLen - granularity;
            int pos2 = newLen - granularity;
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos2, granularity)) {
                    pos1 -= granularity;
                    pos2 -= granularity;
                    if (pos1 < 0) {
                        pos1 += granularity;
                        break;
                    }
                    if (pos2 < 0) {
                        pos2 += granularity;
                        break;
                    }
                }
                else {
                    pos1 += granularity;
                    pos2 += granularity;
                    break;
                }
            }
            if (pos1 < origLen - granularity) {
                postfix = pos1;
            }
        }

        if ((prefix != -1) && (postfix != -1)) {
            if (postfix < prefix) {
                postfix = prefix;
            }
            
            int delta = (prefix + (origLen - postfix) - newLen);
            if (delta > 0) {
                postfix += delta;
            }
        }
        
        int removeBeginIndex = (prefix == -1) ? 0 : prefix;
        int removeEndIndex = (postfix == -1) ? origLen - 1 : postfix;
        
        doc.remove(prefixInd+removeBeginIndex, removeEndIndex - removeBeginIndex);

        if (toInsert.length() > 0) {
            int p1 = (prefix == -1) ? 0 : prefix;
            int p2 = toInsert.length();
            if (postfix != -1)
                p2 = p2 - (origLen - postfix);

            if (p2 > p1) {
                toInsert = toInsert.substring(p1, p2);
                doc.insertString(prefixInd+removeBeginIndex, toInsert, null);
            }
        }        
    }
    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     * 
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();        
        String origDoc = doc.getText(0, origLen);
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origLen-=prefixInd;
                origDoc=doc.getText(prefixInd,origLen);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
            newDoc=newDoc.substring(prefixIndNewDoc);
        }
        newDoc=filterEndLines(newDoc);
        int newLen = newDoc.length();
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }

        final int granularity = 20;
        
        int prefix = -1;
        int postfix = -1;
        String toInsert = newDoc;
        
        if ((origLen > granularity) && (newLen > granularity)) {
            int pos1 = 0;
            int len = Math.min(origLen, newLen);
            // find the prefix which both Strings begin with
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos1, granularity)) {
                    pos1 += granularity;
                    if (pos1 + granularity >= len) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            if (pos1 > 0)
                prefix = pos1;
            
            pos1 = origLen - granularity;
            int pos2 = newLen - granularity;
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos2, granularity)) {
                    pos1 -= granularity;
                    pos2 -= granularity;
                    if (pos1 < 0) {
                        pos1 += granularity;
                        break;
                    }
                    if (pos2 < 0) {
                        pos2 += granularity;
                        break;
                    }
                }
                else {
                    pos1 += granularity;
                    pos2 += granularity;
                    break;
                }
            }
            if (pos1 < origLen - granularity) {
                postfix = pos1;
            }
        }

        if ((prefix != -1) && (postfix != -1)) {
            if (postfix < prefix) {
                postfix = prefix;
            }
            
            int delta = (prefix + (origLen - postfix) - newLen);
            if (delta > 0) {
                postfix += delta;
            }
        }
        
        int removeBeginIndex = (prefix == -1) ? 0 : prefix;
        int removeEndIndex;
        if (postfix == -1){
            if(doc.getText(0, doc.getLength()).charAt(doc.getLength()-1) == '>'){
                removeEndIndex = origLen;
            }
            else
                removeEndIndex = origLen-1;
        }
        else 
            removeEndIndex = postfix;
        
        doc.remove(prefixInd+removeBeginIndex, removeEndIndex - removeBeginIndex);
        
        if (toInsert.length() > 0) {
            int p1 = (prefix == -1) ? 0 : prefix;
            int p2 = toInsert.length();
            if (postfix != -1)
                p2 = p2 - (origLen - postfix);

            if (p2 > p1) {
                toInsert = toInsert.substring(p1, p2);
                doc.insertString(prefixInd+removeBeginIndex, toInsert, null);
            }
        }
    }
    
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc) throws javax.swing.text.BadLocationException {
        replaceDocument(doc,newDoc,null);
    }
    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    public static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i)
                    text[pos] = c;
                pos++;
            }
        }
        return new String(text, 0, pos - 1);
    } 
     
}
