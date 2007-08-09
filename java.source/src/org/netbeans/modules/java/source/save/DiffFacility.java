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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.save;

import java.util.ArrayList;
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
    CasualDiff gdiff;

    public DiffFacility(CasualDiff diff) {
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
    
    public List<Diff> makeListMatch(String text1, String text2) {
        List<Line> list1 = getLines(text1);
        List<Line> list2 = getLines(text2);
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        
        List diffs = new ComputeDiff<Line>(lines1, lines2).diff();
        for (Object o : diffs) {
            Difference diff     = (Difference)o; // generify
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
                gdiff.append(Diff.insert(delEnd == Difference.NONE ? 
                        delStart < lines1.length ? lines1[delStart].start : lines1[lines1.length-1].end
                        : lines1[delEnd].end,
                        builder.toString()));
                
            }

            // deletion
            else if (type == 'd') {
                gdiff.append(Diff.delete(lines1[delStart].start, lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }
                String match1 = builder.toString();
                builder = new StringBuilder();
                for (int i = addStart; i <= delEnd; i++) {
                    builder.append(lines2[i].data);
                }
                String match2 = builder.toString();
                makeTokenListMatch(match1, match2, lines1[delStart].start);
                builder = new StringBuilder();
                for(int i=delEnd+1; i<=addEnd ; i++) {
                    builder.append(lines2[i].data);
                }
                String s = builder.toString();
                if (!"".equals(s)) {
                    gdiff.append(Diff.insert(lines1[delEnd].end, s));
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
        while (seq1.moveNext()) {
            String data = seq1.token().text().toString();
            list1.add(new Line(data, seq1.offset(), seq1.offset() + data.length()));
        }
        while (seq2.moveNext()) {
            String data = seq2.token().text().toString();
            list2.add(new Line(data, seq2.offset(), seq2.offset() + data.length()));
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
                gdiff.append(Diff.insert(currentPos + (delEnd == Difference.NONE ?
                        delStart < lines1.length ? lines1[delStart].start : lines1[lines1.length-1].end
                        : lines1[delEnd].end),
                        builder.toString()));
            }
            
            // deletion
            else if (type == 'd') {
                gdiff.append(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                /*for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }*/
                gdiff.append(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
                //builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                gdiff.append(Diff.insert(currentPos + (delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end),
                        builder.toString()));
            }
                    
        }
        return null;
    }
}
