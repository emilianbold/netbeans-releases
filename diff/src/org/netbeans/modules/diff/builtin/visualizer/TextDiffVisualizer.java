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
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.List;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.DiffVisualizer;
import org.netbeans.api.diff.Difference;

/**
 * The textual visualizer of diffs.
 *
 * @author  Martin Entlicher
 */
public class TextDiffVisualizer extends DiffVisualizer {

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
    
    /**
     * Some diff visualizers may have built-in the diff calculation. In such a case
     * the visualizer does not need any diff provider.
     * @return true when it relies on differences supplied, false if not.
     */
    public boolean needsProvider() {
        return true;
    }
    
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
    public Component createDiff(List diffs, String name1, String title1, Reader r1,
                                String name2, String title2, Reader r2, String MIMEType) throws IOException {
        TextDiffEditorSupport.DiffsListWithOpenSupport diff =
            new TextDiffEditorSupport.DiffsListWithOpenSupport(diffs, name1 + " <> " + name2, title1+" <> "+title2);
        return ((TextDiffEditorSupport) diff.getOpenSupport()).createCloneableTopComponentForMe();
        //return null;
    }
    
    static InputStream differenceToLineDiffText(List diffs) {
        StringBuffer content = new StringBuffer();
        int n1, n2, n3, n4;
        for (Iterator it = diffs.iterator(); it.hasNext(); ) {
            Difference diff = (Difference) it.next();
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
        return new StringBufferInputStream(content.toString());
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
    
}
