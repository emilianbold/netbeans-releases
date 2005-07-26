/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin.visualizer;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import org.openide.windows.CloneableOpenSupport;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffVisualizer;

import org.netbeans.modules.diff.builtin.DiffPresenter;

/**
 * The textual visualizer of diffs.
 *
 * @author  Martin Entlicher
 */
public class TextDiffVisualizer extends DiffVisualizer implements Serializable {
    
    private boolean contextMode = false;
    private int contextNumLines = 3;

    static final long serialVersionUID =-2481513747957146261L;
    /** Creates a new instance of TextDiffVisualizer */
    public TextDiffVisualizer() {
    }

    /**
     * Get the display name of this diff visualizer.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.displayName");
    }
    
    /**
     * Get a short description of this diff visualizer.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.shortDescription");
    }
    
    /** Getter for property contextMode.
     * @return Value of property contextMode.
     */
    public boolean isContextMode() {
        return contextMode;
    }
    
    /** Setter for property contextMode.
     * @param contextMode New value of property contextMode.
     */
    public void setContextMode(boolean contextMode) {
        this.contextMode = contextMode;
    }
    
    /** Getter for property contextNumLines.
     * @return Value of property contextNumLines.
     */
    public int getContextNumLines() {
        return contextNumLines;
    }
    
    /** Setter for property contextNumLines.
     * @param contextNumLines New value of property contextNumLines.
     */
    public void setContextNumLines(int contextNumLines) {
        this.contextNumLines = contextNumLines;
    }
    
    /**
     * Some diff visualizers may have built-in the diff calculation. In such a case
     * the visualizer does not need any diff provider.
     * @return true when it relies on differences supplied, false if not.
     *
    public boolean needsProvider() {
        return true;
    }
     */
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param diffs The list of differences (instances of {@link Difference}).
     *       may be <code>null</code> in case that it does not need diff provider.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The TopComponent representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1,
                                String name2, String title2, Reader r2, String MIMEType) throws IOException {
        /*
        TextDiffEditorSupport.DiffsListWithOpenSupport diff =
            new TextDiffEditorSupport.DiffsListWithOpenSupport(diffs, name1 + " <> " + name2, title1+" <> "+title2);
        diff.setContextMode(contextMode, contextNumLines);
        diff.setReaders(r1, r2);
        return ((TextDiffEditorSupport) diff.getOpenSupport()).createCloneableTopComponentForMe();
        //return null;
         */
        TextDiffInfo diff = new TextDiffInfo(name1, name2, title1, title2, r1, r2, diffs);
        diff.setContextMode(contextMode, contextNumLines);
        return ((TextDiffEditorSupport) diff.getOpenSupport()).createCloneableTopComponentForMe();
    }
    
    static InputStream differenceToLineDiffText(Difference[] diffs) {
        StringBuffer content = new StringBuffer();
        int n1, n2, n3, n4;
        for (int i = 0; i < diffs.length; i++) {
            Difference diff = diffs[i];
            switch (diff.getType()) {
                case Difference.ADD:
                    n3 = diff.getSecondStart();
                    n4 = diff.getSecondEnd();
                    if (n3 == n4) {
                        content.append(diff.getFirstStart()+"a"+n3+"\n");
                    } else {
                        content.append(diff.getFirstStart()+"a"+n3+","+n4+"\n");
                    }
                    appendText(content, "> ", diff.getSecondText());
                    break;
                case Difference.DELETE:
                    n1 = diff.getFirstStart();
                    n2 = diff.getFirstEnd();
                    if (n1 == n2) {
                        content.append(n1+"d"+diff.getSecondStart()+"\n");
                    } else {
                        content.append(n1+","+n2+"d"+diff.getSecondStart()+"\n");
                    }
                    appendText(content, "< ", diff.getFirstText());
                    break;
                case Difference.CHANGE:
                    n1 = diff.getFirstStart();
                    n2 = diff.getFirstEnd();
                    n3 = diff.getSecondStart();
                    n4 = diff.getSecondEnd();
                    if (n1 == n2 && n3 == n4) {
                        content.append(n1+"c"+n3+"\n");
                    } else if (n1 == n2) {
                        content.append(n1+"c"+n3+","+n4+"\n");
                    } else if (n3 == n4) {
                        content.append(n1+","+n2+"c"+n3+"\n");
                    } else {
                        content.append(n1+","+n2+"c"+n3+","+n4+"\n");
                    }
                    appendText(content, "< ", diff.getFirstText());
                    content.append("---\n");
                    appendText(content, "> ", diff.getSecondText());
                    break;
            }
        }
        return new ByteArrayInputStream(content.toString().getBytes());
    }
    
    private static void appendText(StringBuffer buff, String prefix, String text) {
        if (text == null) return ;
        int startLine = 0;
        do {
            int endLine = text.indexOf('\n', startLine);
            if (endLine < 0) endLine = text.length();
            buff.append(prefix + text.substring(startLine, endLine) + "\n");
            startLine = endLine + 1;
        } while (startLine < text.length());
    }
    
    private static final String CONTEXT_MARK1B = "*** ";
    private static final String CONTEXT_MARK1E = " ****\n";
    private static final String CONTEXT_MARK2B = "--- ";
    private static final String CONTEXT_MARK2E = " ----\n";
    private static final String CONTEXT_MARK_DELIMETER = ",";
    private static final String DIFFERENCE_DELIMETER = "***************\n";
    private static final String LINE_PREP = "  ";
    private static final String LINE_PREP_ADD = "+ ";
    private static final String LINE_PREP_REMOVE = "- ";
    private static final String LINE_PREP_CHANGE = "! ";
    
    static InputStream differenceToContextDiffText(TextDiffInfo diffInfo) throws IOException {
        StringBuffer content = new StringBuffer();
        content.append(CONTEXT_MARK1B);
        content.append(diffInfo.getName1());
        content.append("\n");
        content.append(CONTEXT_MARK2B);
        content.append(diffInfo.getName2());
        content.append("\n");
        int contextNumLines = diffInfo.getContextNumLines();
        Difference[] diffs = diffInfo.getDifferences();
        BufferedReader br1 = new BufferedReader(diffInfo.createFirstReader());
        BufferedReader br2 = new BufferedReader(diffInfo.createSecondReader());
        int line1 = 1; // Current line read from 1st file
        int line2 = 1; // Current line read from 2nd file
        for (int i = 0; i < diffs.length; i++) {

            content.append(DIFFERENCE_DELIMETER);

            int[] cr = getContextRange(diffs, i, contextNumLines);

            int begin = diffs[i].getFirstStart() - contextNumLines;
            if (diffs[i].getType() == Difference.ADD) begin++;
            if (begin < 1) begin = 1;
            StringBuffer context = new StringBuffer();
            line1 = dumpContext(0, diffs, i, cr[0], context, contextNumLines, br1, line1);
            if (line1 <= cr[1]) cr[1] = line1 - 1;
            content.append(CONTEXT_MARK1B);
            content.append(begin);
            content.append(CONTEXT_MARK_DELIMETER);
            content.append(cr[1]);
            content.append(CONTEXT_MARK1E);
            content.append(context);

            begin = diffs[i].getSecondStart() - contextNumLines;
            if (diffs[i].getType() == Difference.DELETE) begin++;
            if (begin < 1) begin = 1;
            context = new StringBuffer();
            line2 = dumpContext(1, diffs, i, cr[0], context, contextNumLines, br2, line2);
            if (line2 <= cr[2]) cr[2] = line2 - 1;
            content.append(CONTEXT_MARK2B);
            content.append(begin);
            content.append(CONTEXT_MARK_DELIMETER);
            content.append(cr[2]);
            content.append(CONTEXT_MARK2E);
            content.append(context);

            i = cr[0];
            //i = dumpContext(diffs, 
            //Difference diff = diffs[i];
            //Difference nextDiff = ((i + 1) < diffs.length) ? diffs[i + 1] : null;
            //if (isNew) {
            //    content.append(DIFFERENCE_DELIMETER);
            //}
        }
        return new ByteArrayInputStream(content.toString().getBytes());
    }
    
    private static int[] getContextRange(Difference[] diffs, int i,
                                       int contextNumLines) {
        int line1 = diffs[i].getFirstStart();
        int line2 = diffs[i].getSecondStart();
        for ( ; i < diffs.length; i++) {
            Difference diff = diffs[i];
            if (line1 + 2*contextNumLines < diff.getFirstStart() &&
                line2 + 2*contextNumLines < diff.getSecondStart()) break;
            line1 = diff.getFirstStart();
            line2 = diff.getSecondStart();
            int l1 = Math.max(0, diff.getFirstEnd() - diff.getFirstStart());
            int l2 = Math.max(0, diff.getSecondEnd() - diff.getSecondStart());
            line1 += l1;
            line2 += l2;
        }
        return new int[] { i - 1, line1 + contextNumLines, line2 + contextNumLines };
    }
    
    private static int dumpContext(int which, Difference[] diffs, int i, int j,
        StringBuffer content, int contextNumLines, BufferedReader br, int line)
        throws IOException {

        int startLine;
        if (which == 0) {
            startLine = diffs[i].getFirstStart() - contextNumLines;
            if (diffs[i].getType() == Difference.ADD) startLine++;
        } else {
            startLine = diffs[i].getSecondStart() - contextNumLines;
            if (diffs[i].getType() == Difference.DELETE) startLine++;
        }
        for ( ; line < startLine; line++) br.readLine();
        int position = content.length();
        boolean isChange = false;
        for ( ; i <= j; i++) {
            Difference diff = diffs[i];
            if (which == 0) startLine = diff.getFirstStart();
            else startLine = diff.getSecondStart();
            for ( ; line < startLine; line++) {
                content.append(LINE_PREP);
                content.append(br.readLine());
                content.append("\n");
            }
            int length = 0;
            String prep = null;
            switch (diffs[i].getType()) {
                case Difference.ADD:
                    if (which == 1) {
                        prep = LINE_PREP_ADD;
                        length = diff.getSecondEnd() - diff.getSecondStart() + 1;
                    }
                    break;
                case Difference.DELETE:
                    if (which == 0) {
                        prep = LINE_PREP_REMOVE;
                        length = diff.getFirstEnd() - diff.getFirstStart() + 1;
                    }
                    break;
                case Difference.CHANGE:
                    prep = LINE_PREP_CHANGE;
                    if (which == 0) {
                        length = diff.getFirstEnd() - diff.getFirstStart() + 1;
                    } else {
                        length = diff.getSecondEnd() - diff.getSecondStart() + 1;
                    }
                    break;
            }
            if (prep != null) {
                isChange = true;
                for (int k = 0; k < length; k++, line++) {
                    content.append(prep);
                    content.append(br.readLine());
                    content.append("\n");
                }
            }
        }
        if (!isChange) {
            content.delete(position, content.length());
        } else {
            for (int k = 0; k < contextNumLines; k++, line++) {
                String lineStr = br.readLine();
                if (lineStr == null) break;
                content.append(LINE_PREP);
                content.append(lineStr);
                content.append("\n");
            }
        }
        return line;
    }
    
    static class TextDiffInfo extends DiffPresenter.Info {
        
        private Reader r1;
        private Reader r2;
        private Difference[] diffs;
        private CloneableOpenSupport openSupport;
        private boolean contextMode;
        private int contextNumLines;
        
        public TextDiffInfo(String name1, String name2, String title1, String title2,
                            Reader r1, Reader r2, Difference[] diffs) {
            super(name1, name2, title1, title2, null, false, false);
            this.r1 = r1;
            this.r2 = r2;
            this.diffs = diffs;
        }
        
        public String getName() {
            String componentName = getName1();
            String name2 = getName2();
            if (name2 != null && name2.length() > 0)  componentName += " <> "+name2;
            return componentName;
        }
        
        public String getTitle() {
            return getTitle1() + " <> " + getTitle2();
        }
        
        public Reader createFirstReader() {
            return r1;
        }
        
        public Reader createSecondReader() {
            return r2;
        }
        
        public Difference[] getDifferences() {
            return diffs;
        }
        
        public CloneableOpenSupport getOpenSupport() {
            if (openSupport == null) {
                openSupport = new TextDiffEditorSupport(this);
            }
            return openSupport;
        }
        
        /** Setter for property contextMode.
         * @param contextMode New value of property contextMode.
         */
        public void setContextMode(boolean contextMode, int contextNumLines) {
            this.contextMode = contextMode;
            this.contextNumLines = contextNumLines;
        }
        
        /** Getter for property contextMode.
         * @return Value of property contextMode.
         */
        public boolean isContextMode() {
            return contextMode;
        }
        
        public int getContextNumLines() {
            return contextNumLines;
        }
        
    }
}
