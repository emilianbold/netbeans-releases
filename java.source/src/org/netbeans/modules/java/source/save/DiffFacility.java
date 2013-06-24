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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.source.save;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;

/**
 *
 * @author Pavel Flaska
 */
class DiffFacility {
    private final Collection<Diff> gdiff;

    public DiffFacility(Collection<Diff> diff) {
        this.gdiff = diff;
    }
    
    private static class Line {
        Line(String data, int start, int end) {
            this.start = start;
            this.end = end;
            this.data = data;
        }
        
        @Override
        public String toString() {
            return data.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Line) {
                return data.equals(((Line) o).data);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return data.hashCode();
        }
        
        String data;
        int end;
        int start;
    }
    
    private static List<Line> getLines(String text) {
        char[] chars = text.toCharArray();
        List<Line> list = new ArrayList<Line>();
        int pointer = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n') {
                list.add(new Line(new String(chars, pointer, i-pointer+1), pointer, i+1));
                pointer = i+1;
            }
        }
        if (pointer < chars.length) {
            list.add(new Line(new String(chars, pointer, chars.length-pointer), pointer, chars.length));
        }
        return list;
    }
    
    public List<Diff> makeListMatch(String text1, String text2, int offset) {
        List<Line> list1 = getLines(text1);
        List<Line> list2 = getLines(text2);
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        
        List<Difference> diffs = new ComputeDiff<Line>(lines1, lines2).diff();
        for (Difference diff : diffs) {
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                gdiff.add(Diff.insert(delEnd == Difference.NONE ?
                        delStart < lines1.length ? lines1[delStart].start + offset : (lines1.length != 0 ? lines1[lines1.length-1].end + offset : offset)
                        : lines1[delEnd].end + offset,
                        builder.toString()));
                
            }

            // deletion
            else if (type == 'd') {
                gdiff.add(Diff.delete(lines1[delStart].start + offset, lines1[delEnd].end + offset));
            }
            
            // change
            else { // type == 'c'
                if (addEnd-addStart>delEnd-delStart) {
                    //change will be performed in 2 steps:
                    //1. change lines
                    //2. add lines
                    StringBuilder builder = new StringBuilder();
                    for (int i = delStart; i <= delEnd; i++) {
                        builder.append(lines1[i].data);
                    }
                    String match1 = builder.toString();
                    builder = new StringBuilder();
                    for (int i = addStart; i <= addStart + delEnd - delStart; i++) {
                        builder.append(lines2[i].data);
                    }
                    String match2 = builder.toString();
                    makeTokenListMatch(match1, match2, lines1[delStart].start + offset);
                    builder = new StringBuilder();
                    for (int i = addStart + delEnd - delStart + 1; i <= addEnd; i++) {
                        builder.append(lines2[i].data);
                    }
                    String s = builder.toString();
                    if (!"".equals(s)) {
                        gdiff.add(Diff.insert(lines1[delEnd].end + offset, s));
                    }
                } else {
                    //one step change
                    StringBuilder builder = new StringBuilder();
                    for (int i = delStart; i <= delEnd; i++) {
                        builder.append(lines1[i].data);
                    }
                    String match1 = builder.toString();
                    builder = new StringBuilder();
                    for (int i = addStart; i <= addEnd; i++) {
                        builder.append(lines2[i].data);
                    }
                    String match2 = builder.toString();
                    makeTokenListMatch(match1, match2, lines1[delStart].start + offset);
                }
            }
        }
        return null;
    }
    
    public List<Diff> makeTokenListMatch(String text1, String text2, int currentPos) {
        TokenSequence<JavaTokenId> seq1 = TokenHierarchy.create(text1, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        TokenSequence<JavaTokenId> seq2 = TokenHierarchy.create(text2, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        List<Line> list1 = new ArrayList<Line>();
        List<Line> list2 = new ArrayList<Line>();
        JavaTokenId lastId1 = null;
        while (seq1.moveNext()) {
            String data = seq1.token().text().toString();
            lastId1 = seq1.token().id();
            list1.add(new Line(data, seq1.offset(), seq1.offset() + data.length()));
        }
        JavaTokenId lastId2 = null;
        while (seq2.moveNext()) {
            String data = seq2.token().text().toString();
            lastId2 = seq2.token().id();
            list2.add(new Line(data, seq2.offset(), seq2.offset() + data.length()));
        }
        if (lastId1 != null && lastId1 == lastId2 && (lastId1 == JavaTokenId.LINE_COMMENT || (lastId1 == JavaTokenId.WHITESPACE && !(list1.get(list1.size() - 1).data.endsWith("\n") ^ list2.get(list2.size() - 1).data.endsWith("\n"))))) {            
            Line last1 = list1.remove(list1.size() - 1);
            if (last1.data.indexOf('\n') != last1.data.lastIndexOf("\n")) {
                String stripped = last1.data.substring(0, last1.data.lastIndexOf('\n'));
                list1.add(new Line(stripped, last1.start, last1.start + stripped.length()));
            }
            Line last2 = list2.remove(list2.size() - 1);
            if (last2.data.indexOf('\n') != last2.data.lastIndexOf("\n")) {
                String stripped = last2.data.substring(0, last2.data.lastIndexOf('\n'));
                list1.add(new Line(stripped, last2.start, last2.start + stripped.length()));
            }
        }
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        List<Difference> diffs = new ComputeDiff<Line>(lines1, lines2).diff();
        for (Difference diff : diffs) {
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                gdiff.add(Diff.insert(currentPos + (delEnd == Difference.NONE ?
                        delStart < lines1.length ? lines1[delStart].start : (lines1.length > 0 ? lines1[lines1.length-1].end : 0)
                        : lines1[delEnd].end),
                        builder.toString()));
            }
            
            // deletion
            else if (type == 'd') {
                gdiff.add(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                /*for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }*/
                gdiff.add(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
                //builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                gdiff.add(Diff.insert(currentPos + (delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end),
                        builder.toString()));
            }
                    
        }
        return null;
    }
}
