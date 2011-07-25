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

package org.netbeans.modules.java.hints.jackpot.impl.refactoring;

import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.jackpot.spi.Trigger.PatternDescription;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lahvac
 */
public class Utilities {
    public static List<PositionBounds> prepareSpansFor(FileObject file, Iterable<? extends int[]> spans) {
        List<PositionBounds> result = new ArrayList<PositionBounds>();

        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
            CloneableEditorSupport ces = (CloneableEditorSupport) ec;

            result = new LinkedList<PositionBounds>();

            for (int[] span : spans) {
                PositionRef start = ces.createPositionRef(span[0], Bias.Forward);
                PositionRef end = ces.createPositionRef(span[1], Bias.Forward);

                result.add(new PositionBounds(start, end));
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    public static Collection<RefactoringElementImplementation> createRefactoringElementImplementation(FileObject file, List<PositionBounds> spans, boolean verified) {
        List<RefactoringElementImplementation> result = new LinkedList<RefactoringElementImplementation>();

        try {
            DataObject d = DataObject.find(file);
            LineCookie lc = d.getLookup().lookup(LineCookie.class);

            for (PositionBounds bound : spans) {
                PositionRef start = bound.getBegin();
                PositionRef end = bound.getEnd();
                Line l = lc.getLineSet().getCurrent(start.getLine());
                String lineText = l.getText();

                int boldStart = start.getColumn();
                int boldEnd   = end.getLine() == start.getLine() ? end.getColumn() : lineText.length();

                StringBuilder displayName = new StringBuilder();

                if (!verified) {
                    displayName.append("(not verified) ");
                }

                displayName.append(escapedSubstring(lineText, 0, boldStart).replaceAll("^[ ]*", ""));
                displayName.append("<b>");
                displayName.append(escapedSubstring(lineText, boldStart, boldEnd));
                displayName.append("</b>");
                displayName.append(escapedSubstring(lineText, boldEnd, lineText.length()));

                result.add(new RefactoringElementImpl(file, bound, displayName.toString()));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    private static String escapedSubstring(String str, int start, int end) {
        String substring = str.substring(start, end);

        try {
            return XMLUtil.toElementContent(substring);
        } catch (CharConversionException ex) {
            Exceptions.printStackTrace(ex);
            return substring;
        }
    }
    
    public static ArrayList<HintMetadata> getBatchSupportedHints() {
        HashSet hintSet = new HashSet();
        for (Map.Entry<HintMetadata, Collection<? extends HintDescription>> entry: RulesManager.getInstance().allHints.entrySet()) {
            if (entry.getKey().options.contains(Options.NO_BATCH)) continue;
            for (HintDescription hd : entry.getValue()) {
                if (!(hd.getTrigger() instanceof PatternDescription)) continue; //TODO: only pattern based hints are currently supported
                hintSet.add(entry.getKey());
            }
        }
        
        ArrayList hints = new ArrayList(hintSet); 
            
        Collections.sort(hints, new Comparator<HintMetadata>() {

            @Override
            public int compare(HintMetadata t, HintMetadata t1) {
                return t.displayName.compareTo(t1.displayName);
            }
        });
        
        return hints;
    }
    

    private static final class RefactoringElementImpl extends SimpleRefactoringElementImplementation {

        private final FileObject file;
        private final PositionBounds span;
        private final String displayName;

        private final Lookup lookup;

        public RefactoringElementImpl(FileObject file, PositionBounds span, String displayName) {
            this.file = file;
            this.span = span;
            this.lookup = Lookups.fixed(file);
            this.displayName = displayName;
        }

        public String getText() {
            return getDisplayText();
        }

        public String getDisplayText() {
            return displayName;
        }

        public void performChange() {
            //throw new IllegalStateException();
        }

        public Lookup getLookup() {
            return lookup;
        }

        public FileObject getParentFile() {
            return file;
        }

        public PositionBounds getPosition() {
            return span;
        }

    }

}
