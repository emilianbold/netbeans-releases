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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.editor.highlighting.support;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.lib2.highlighting.OffsetGapList;
import org.netbeans.spi.editor.highlighting.*;

/**
 *
 * @author vita
 */
public class OffsetsBagTest extends NbTestCase {
    
    private static final AttributeSet EMPTY = SimpleAttributeSet.EMPTY;
    
    private Document doc = new DefaultStyledDocument();
    
    /** Creates a new instance of HighlightSequenceTest */
    public OffsetsBagTest(String name) {
        super(name);
    }

    public void testSimple() {
        OffsetsBag hs = new OffsetsBag(doc);
        assertEquals("Sequence should be empty", 0, hs.getMarks().size());
        
        hs.addHighlight(10, 20, EMPTY);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        assertEquals("Sequence should not be empty", 2, marks.size());
        assertEquals("Wrong highlight's start offset", 10, marks.get(0).getOffset());
        assertEquals("Wrong highlight's end offset", 20, marks.get(1).getOffset());
        
        hs.clear();
        assertEquals("Sequence was not cleared", 0, hs.getMarks().size());
    }

    public void testAddLeftOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(5, 15, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(1).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", marks.get(2).getAttributes());
    }
    
    public void testAddRightOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(15, 25, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));
        assertNull(  "2. highlight - wrong end", marks.get(2).getAttributes());
    }

    public void testAddCompleteOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(5, 25, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 25, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }
    
    public void testAddSplit() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 25, attribsA);
        hs.addHighlight(15, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testAddAligned() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(20, 30, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", marks.get(2).getAttributes());
        
        hs.addHighlight(0, 10, attribsB);
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testAddAligned2() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 40, attribsA);
        hs.addHighlight(10, 20, attribsB);
        hs.addHighlight(30, 40, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 30, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 40, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testAddMiddle() {
        for(int i = 0; i < 10; i++) {
            addMiddle(i + 1);
        }
    }
    
    private void addMiddle(int middleMarks) {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(10 * i + 10, 10 * i + 20, i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.addHighlight(15, middleMarks * 10 + 15, attribsC);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsC", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            marks.get(3).getAttributes());
    }
    
    public void testRemoveLeftOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 15, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveLeftOverlapClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 15, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 15, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveRightOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(15, 25, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveRightOverlapClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(15, 25, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveCompleteOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 25, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveCompleteOverlapClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 25, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplit() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 25, attribsA);
        hs.removeHighlights(15, 20, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplitClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 25, attribsA);
        hs.removeHighlights(15, 20, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());

        assertEquals("2. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testRemoveAlignedClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(0, 10, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
        
        hs.removeHighlights(20, 30, true);
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveAligned2Clip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 40, attribsA);
        hs.removeHighlights(10, 20, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 40, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
        
        hs.removeHighlights(30, 40, true);
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 30, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveMiddle() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(20, 30, attribsB);
        hs.removeHighlights(15, 25, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveMiddleEmptyHighlight() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(15, 15, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }
    
    public void testRemoveMiddleClip() {
        for(int i = 0; i < 10; i++) {
            removeMiddleClip(i + 1);
        }
    }

    private void removeMiddleClip(int middleMarks) {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(10 * i + 10, 10 * i + 20, i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.removeHighlights(15, middleMarks * 10 + 15, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            marks.get(1).getAttributes());

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            marks.get(3).getAttributes());
    }
    
    public void testAddAll() {
        OffsetsBag hsA = new OffsetsBag(doc);
        OffsetsBag hsB = new OffsetsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(0, 30, attribsA);
        hsA.addHighlight(10, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marksA = hsA.getMarks();
        
        hsB.addHighlight(0, 40, attribsC);
        hsB.addAllHighlights(hsA.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE));
        OffsetGapList<OffsetsBag.Mark> marksB = hsB.getMarks();
        
        assertEquals("Wrong number of highlights", marksA.size() + 1, marksB.size());
        for (int i = 0; i < marksA.size() - 1; i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i + 1).getOffset(), marksB.get(i + 1).getOffset());
            assertEquals(i + ". highlight - wrong attribs",
                marksA.get(i).getAttributes().getAttribute("set-name"),
                marksB.get(i).getAttributes().getAttribute("set-name"));
        }

        assertEquals("4. highlight - wrong start offset", 30, marksB.get(3).getOffset());
        assertEquals("4. highlight - wrong end offset", 40, marksB.get(4).getOffset());
        assertEquals("4. highlight - wrong attribs", "attribsC", marksB.get(3).getAttributes().getAttribute("set-name"));
    }

    public void testSet() {
        OffsetsBag hsA = new OffsetsBag(doc);
        OffsetsBag hsB = new OffsetsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(0, 30, attribsA);
        hsA.addHighlight(10, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marksA = hsA.getMarks();
        
        hsB.addHighlight(0, 40, attribsC);
        hsB.setHighlights(hsA.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE));
        OffsetGapList<OffsetsBag.Mark> marksB = hsB.getMarks();
        
        assertEquals("Wrong number of highlights", marksA.size(), marksB.size());
        for (int i = 0; i < marksA.size(); i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            
            AttributeSet attrA = marksA.get(i).getAttributes();
            AttributeSet attrB = marksB.get(i).getAttributes();
            
            if (attrA != null && attrB != null) {
                assertEquals(i + ". highlight - wrong attribs",
                    attrA.getAttribute("set-name"), 
                    attrB.getAttribute("set-name"));
            } else {
                assertTrue(i + ". highlight - wrong attribs", attrA == null && attrB == null);
            }
        }
    }
    
    public void testGetHighlights() {
        OffsetsBag hs = new OffsetsBag(doc);
        assertFalse("Sequence should be empty", hs.getHighlights(
            Integer.MIN_VALUE, Integer.MAX_VALUE).moveNext());
        
        hs.addHighlight(10, 30, EMPTY);

        {
            // Do not clip the highlights
            HighlightsSequence highlights = hs.getHighlights(20, 25);
            assertTrue("Sequence should not be empty", highlights.moveNext());
            assertEquals("Wrong highlight's start offset", 20, highlights.getStartOffset());
            assertEquals("Wrong highlight's end offset", 25, highlights.getEndOffset());
            assertFalse("There should be no more highlights", highlights.moveNext());
        }
        
        hs.clear();
        assertFalse("Sequence was not cleared", hs.getHighlights(
            Integer.MIN_VALUE, Integer.MAX_VALUE).moveNext());
    }

    public void testGetHighlights2() {
        OffsetsBag hb = new OffsetsBag(doc);
        hb.addHighlight(10, 20, SimpleAttributeSet.EMPTY);
        
        HighlightsSequence hs = hb.getHighlights(0, 5);
        assertFalse("HighlightsSequence should be empty", hs.moveNext());
        
        hs = hb.getHighlights(25, 30);
        assertFalse("HighlightsSequence should be empty", hs.moveNext());
        
        hs = hb.getHighlights(0, 15);
        assertTrue("HighlightsSequence should not be empty", hs.moveNext());
        assertFalse("Too many highlights in the sequence", hs.moveNext());

        hs = hb.getHighlights(12, 22);
        assertTrue("HighlightsSequence should not be empty", hs.moveNext());
        assertFalse("Too many highlights in the sequence", hs.moveNext());
        
        hs = hb.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertTrue("HighlightsSequence should not be empty", hs.moveNext());
        assertFalse("Too many highlights in the sequence", hs.moveNext());
    }
    
    public void testConcurrentModification() {
        OffsetsBag hb = new OffsetsBag(doc);

        // Modify the bag
        hb.addHighlight(5, 10, EMPTY);
        hb.addHighlight(15, 20, EMPTY);
        hb.addHighlight(25, 30, EMPTY);
        
        HighlightsSequence hs = hb.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertTrue("There should be some highlights", hs.moveNext());
        
        int s = hs.getStartOffset();
        int e = hs.getEndOffset();
        AttributeSet a = hs.getAttributes();
        
        // Modification after the sequence was acquired
        hb.addHighlight(100, 110, EMPTY);
        
        assertEquals("Wrong highlight start", s, hs.getStartOffset());
        assertEquals("Wrong highlight end", e, hs.getEndOffset());
        assertEquals("Wrong highlight attributes", a, hs.getAttributes());
        assertFalse("There should be no more highlights after co-modification", hs.moveNext());
    }

    public void testDocumentChanges() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "01234567890123456789012345678901234567890123456789", SimpleAttributeSet.EMPTY);
        
        OffsetsBag bag = new OffsetsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        bag.addHighlight(0, 30, attribsA);
        bag.addHighlight(10, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = bag.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
        
        doc.insertString(12, "----", SimpleAttributeSet.EMPTY);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 24, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 24, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 34, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
        
        doc.remove(1, 5);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 5, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 5, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 19, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 19, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 29, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }
    
    private void dumpHighlights(HighlightsSequence seq) {
        System.out.println("Dumping highlights from: " + seq + "{");
        while(seq.moveNext()) {
            System.out.println("<" + seq.getStartOffset() + ", " + seq.getEndOffset() + ", " + seq.getAttributes() + ">");
        }
        System.out.println("} --- End of Dumping highlights from: " + seq + " ---------------------");
    }
}
