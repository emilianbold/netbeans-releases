/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.gsfret.editor.semantic;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.editor.Coloring;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.gsf.Language;
import org.openide.text.NbDocument;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
final class HighlightImpl implements Mark, Highlight {
    
    private Document doc;
    private Position start;
    private Position start2;
    private Position end;
    private Position end2;
    private Collection<ColoringAttributes> colorings;
    private Color    esColor;
    private Language language;
    
    public HighlightImpl(Document doc, Position start, Position end, Collection<ColoringAttributes> colorings) {
        this(doc, start, end, colorings, null);
    }
    
    /** Creates a new instance of Highlight */
    public HighlightImpl(Document doc, Position start, Position end, Collection<ColoringAttributes> colorings, Color esColor) {
        this.doc = doc;
        this.start = start;
        this.end = end;
        this.colorings = colorings;
        this.esColor = esColor;
    }

    public HighlightImpl(Language language, Document doc, int start, int end, Collection<ColoringAttributes> colorings, Color esColor) throws BadLocationException {
        this.language = language;
        this.doc = doc;
        this.start = NbDocument.createPosition(doc, start, Bias.Forward);
        this.start = NbDocument.createPosition(doc, start, Bias.Backward);
        this.end = NbDocument.createPosition(doc, end, Bias.Backward);
        this.end2 = NbDocument.createPosition(doc, end, Bias.Forward);
        this.colorings = colorings;
        this.esColor = esColor;
    }
    
    public int getEnd() {
        int endPos = end.getOffset();
        
        if (end2 == null)
            return endPos;
        
        int endPos2 = end2.getOffset();
        
        if (endPos == endPos2)
            return endPos;
        
        try {
            String added = doc.getText(endPos, endPos2 - endPos);
            int newEndPos = endPos;
            
            for (char c : added.toCharArray()) {
                if (Character.isJavaIdentifierPart(c))
                    newEndPos++;
            }
            
            if (newEndPos != endPos) {
                end = NbDocument.createPosition(doc, newEndPos, Bias.Backward);
                end2 = NbDocument.createPosition(doc, newEndPos, Bias.Forward);
            }
            
            return newEndPos;
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.FINE, e.getMessage(), e);
            return endPos;
        }
    }

    public int getStart() {
        int startPos = start.getOffset();
        
        if (start2 == null)
            return startPos;
        
        int startPos2 = start2.getOffset();
        
        if (startPos == startPos2)
            return startPos;
        
        try {
            String added = doc.getText(startPos, startPos2 - startPos);
            int newStartPos = startPos;
            
            for (char c : added.toCharArray()) {
                if (Character.isJavaIdentifierPart(c))
                    newStartPos++;
            }
            
            if (newStartPos != startPos) {
                start = NbDocument.createPosition(doc, newStartPos, Bias.Forward);
                start2 = NbDocument.createPosition(doc, newStartPos, Bias.Backward);
            }
            
            return newStartPos;
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.FINE, e.getMessage(), e);
            return startPos;
        }
    }

    public Coloring getColoring() {
        return language.getColoringManager().getColoring(colorings);
    }
    
    public String toString() {
        return "Highlight: [" + colorings + ", " + start.getOffset() + "-" + end.getOffset() + "]";
    }
    
    public int getType() {
        return TYPE_ERROR_LIKE;
    }
    
    public Status getStatus() {
        return Status.STATUS_OK;
    }
    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    public Color getEnhancedColor() {
        return esColor;
    }
    
    public int[] getAssignedLines() {
        int line = NbDocument.findLineNumber((StyledDocument) doc, start.getOffset());
        
        return new int[] {line, line};
    }
    
    public String getShortDescription() {
        return "...";
    }
    
    public String getHighlightTestData() {
        int lineStart = NbDocument.findLineNumber((StyledDocument) doc, start.getOffset());
        int columnStart = NbDocument.findLineColumn((StyledDocument) doc, start.getOffset());
        int lineEnd = NbDocument.findLineNumber((StyledDocument) doc, end.getOffset());
        int columnEnd = NbDocument.findLineColumn((StyledDocument) doc, end.getOffset());
        
        return coloringsToString() + ", " + lineStart + ":" + columnStart + "-" + lineEnd + ":" + columnEnd;
    }
    
    private String coloringsToString() {
        StringBuffer result = new StringBuffer();
        boolean first = true;
        
        result.append("[");
        
        for (ColoringAttributes attribute : coloringsAttributesOrder) {
            if (colorings.contains(attribute)) {
                if (!first) {
                    result.append(", ");
                }
                
                first = false;
                result.append(attribute.name());
            }
        }
        
        result.append("]");
        
        return result.toString();
    }
    
    Collection<ColoringAttributes> coloringsAttributesOrder = Arrays.asList(new ColoringAttributes[] {
        ColoringAttributes.STATIC, 
        ColoringAttributes.ABSTRACT,
        
        ColoringAttributes.PUBLIC,
        ColoringAttributes.PROTECTED,
        ColoringAttributes.PACKAGE_PRIVATE,
        ColoringAttributes.PRIVATE,
        
        ColoringAttributes.DEPRECATED,
        
        ColoringAttributes.FIELD,
        ColoringAttributes.LOCAL_VARIABLE,
        ColoringAttributes.PARAMETER,
        ColoringAttributes.METHOD,
        ColoringAttributes.CONSTRUCTOR,
        ColoringAttributes.CLASS,
                
        ColoringAttributes.UNUSED,

        ColoringAttributes.TYPE_PARAMETER_DECLARATION,
        ColoringAttributes.TYPE_PARAMETER_USE,

        ColoringAttributes.UNDEFINED,

        ColoringAttributes.MARK_OCCURRENCES,
    });
 
//    public static HighlightImpl parse(StyledDocument doc, String line) throws ParseException, BadLocationException {
//        MessageFormat f = new MessageFormat("[{0}], {1,number,integer}:{2,number,integer}-{3,number,integer}:{4,number,integer}");
//        Object[] args = f.parse(line);
//        
//        String attributesString = (String) args[0];
//        int    lineStart  = ((Long) args[1]).intValue();
//        int    columnStart  = ((Long) args[2]).intValue();
//        int    lineEnd  = ((Long) args[3]).intValue();
//        int    columnEnd  = ((Long) args[4]).intValue();
//        
//        String[] attrElements = attributesString.split(",");
//        List<ColoringAttributes> attributes = new ArrayList<ColoringAttributes>();
//        
//        for (String a : attrElements) {
//            a = a.trim();
//            
//            attributes.add(ColoringAttributes.valueOf(a));
//        }
//        
//        if (attributes.contains(null))
//            throw new NullPointerException();
//        
//        int offsetStart = NbDocument.findLineOffset(doc, lineStart) + columnStart;
//        int offsetEnd = NbDocument.findLineOffset(doc, lineEnd) + columnEnd;
//        
//        return new HighlightImpl(doc, doc.createPosition(offsetStart), doc.createPosition(offsetEnd), attributes, null);
//    }
    
}
