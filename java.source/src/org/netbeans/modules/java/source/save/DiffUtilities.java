/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.save;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;

/**
 *
 * @author lahvac
 */
public class DiffUtilities {
    private static final Logger LOG = Logger.getLogger(DiffUtilities.class.getName());
    
    public static List<ModificationResult.Difference> diff2ModificationResultDifference(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo, String originalCode, String newCode) throws IOException, BadLocationException {
        return diff2ModificationResultDifference(fo, converter, userInfo, originalCode, diff(originalCode, newCode, 0));
    }

    public static List<Diff> diff(String origContent, String newContent, int offset) {
        List<Diff> diffs = new ArrayList<Diff>();
        new DiffFacility(diffs).makeListMatch(origContent, newContent, offset);
        return diffs;
    }
    
    public static List<ModificationResult.Difference> diff2ModificationResultDifference(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo, String content, List<Diff> diffs) throws IOException, BadLocationException {
        Collections.sort(diffs, new Comparator<Diff>() {
            public int compare(Diff o1, Diff o2) {
                return o1.getPos() - o2.getPos();
            }
        });

        Rewriter out = new Rewriter(fo, converter, userInfo);
        char[] buf = content.toCharArray();

        // Copy any leading comments.
        for (Diff d : diffs) {
            switch (d.type) {
                case INSERT:
                    out.copyTo(d.getPos());
                    out.writeTo(d.getText());
                    break;
                case DELETE:
                    out.copyTo(d.getPos());
                    out.skipThrough(buf, d.getEnd());
                    break;
                default:
                    throw new AssertionError("unknown CasualDiff type: " + d.type);
            }
        }

        return out.diffs;
    }


    // Innerclasses ------------------------------------------------------------
    private static class Rewriter {

        private int offset = 0;
        private CloneableEditorSupport ces;
        private PositionConverter converter;
        public List<ModificationResult.Difference> diffs = new LinkedList<ModificationResult.Difference>();
        private Map<Integer, String> userInfo;

        public Rewriter(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo) throws IOException {
            this.converter = converter;
            this.userInfo = userInfo;
            if (fo != null) {
                DataObject dObj = DataObject.find(fo);
                ces = dObj != null ? (CloneableEditorSupport)dObj.getCookie(EditorCookie.class) : null;
            }
            if (ces == null)
                throw new IOException("Could not find CloneableEditorSupport for " + FileUtil.getFileDisplayName (fo)); //NOI18N
        }

        public void writeTo(String s) throws IOException, BadLocationException {
            ModificationResult.Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
            if (diff != null && diff.getKind() == ModificationResult.Difference.Kind.REMOVE && diff.getEndPosition().getOffset() == offset) {
                diffs.remove(diffs.size() - 1);
                diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.CHANGE, diff.getStartPosition(), diff.getEndPosition(), diff.getOldText(), s, diff.getDescription()));
            } else {
                int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                if (off >= 0) {
                    Document d = ces.getDocument();
                    PositionRef endRef = ces.createPositionRef(off, Bias.Backward);
                    int l;
                    if (d != null && endRef.getOffset() > (l = d.getLength())) {
                        LOG.log(Level.WARNING, 
                                "Invalid diff position: {0}, doc.length: {1}", new Object[] {
                                    off, l
                                });
                    }
                    diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.INSERT, ces.createPositionRef(off, Bias.Forward), endRef, null, s, userInfo.get(offset)));
                }
            }
        }

        public void skipThrough(char[] in, int pos) throws IOException, BadLocationException {
            String origText = new String(in, offset, pos - offset);
            org.netbeans.api.java.source.ModificationResult.Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
            if (diff != null && diff.getKind() == org.netbeans.api.java.source.ModificationResult.Difference.Kind.INSERT && diff.getStartPosition().getOffset() == offset) {
                diffs.remove(diffs.size() - 1);
                diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.CHANGE, diff.getStartPosition(), diff.getEndPosition(), origText, diff.getNewText(), diff.getDescription()));
            } else {
                int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                if (off >= 0) {
                    Document d = ces.getDocument();
                    PositionRef endRef = ces.createPositionRef(off + origText.length(), Bias.Backward);
                    int l;
                    if (d != null && endRef.getOffset() > (l = d.getLength())) {
                        LOG.log(Level.WARNING, 
                                "Invalid diff position: {0}, doc.length: {1}", new Object[] {
                                    off, l
                                });
                    }
                    diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.REMOVE, ces.createPositionRef(off, Bias.Forward), endRef, origText, null, userInfo.get(offset)));
                }
            }
            offset = pos;
        }

        public void copyTo(int pos) throws IOException {
            offset = pos;
        }
    }
}
