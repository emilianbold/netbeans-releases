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
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.spi.editor.highlighting.*;
import org.netbeans.spi.editor.highlighting.performance.SimplePosition;

/**
 *
 * @author vita
 */
public class PositionsBagTest extends NbTestCase {
    
    private static final AttributeSet EMPTY = SimpleAttributeSet.EMPTY;
    
    private Document doc = new DefaultStyledDocument();
    
    /** Creates a new instance of HighlightSequenceTest */
    public PositionsBagTest(String name) {
        super(name);
    }

    public void testSimple() {
        PositionsBag hs = new PositionsBag(doc);
        assertEquals("Sequence should be empty", 0, hs.getMarks().size());
        
        hs.addHighlight(pos(10), pos(20), EMPTY);
        GapList<Position> marks = hs.getMarks();
        assertEquals("Sequence should not be empty", 2, marks.size());
        assertEquals("Wrong highlight's start offset", 10, marks.get(0).getOffset());
        assertEquals("Wrong highlight's end offset", 20, marks.get(1).getOffset());
        
        hs.clear();
        assertEquals("Sequence was not cleared", 0, hs.getMarks().size());
    }

    public void testAddLeftOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(5), pos(15), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(1).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", atttributes.get(2));
    }
    
    public void testAddRightOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(15), pos(25), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));
        assertNull(  "2. highlight - wrong end", atttributes.get(2));
    }

    public void testAddCompleteOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(5), pos(25), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 25, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }
    
    public void testAddSplit() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(25), attribsA);
        hs.addHighlight(pos(15), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }

    public void testAddAligned() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(20), pos(30), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", atttributes.get(2));
        
        hs.addHighlight(pos(0), pos(10), attribsB);
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }

    public void testAddAligned2() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(40), attribsA);
        hs.addHighlight(pos(10), pos(20), attribsB);
        hs.addHighlight(pos(30), pos(40), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 30, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 40, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }

    public void testAddMiddle() {
        for(int i = 0; i < 10; i++) {
            addMiddle(i + 1);
        }
    }
    
    private void addMiddle(int middleMarks) {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(pos(10 * i + 10), pos(10 * i + 20), i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.addHighlight(pos(15), pos(middleMarks * 10 + 15), attribsC);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsC", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            atttributes.get(3));
    }
    
    public void testRemoveLeftOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(5, 15);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveLeftOverlapClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(5), pos(15), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 15, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveRightOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(15, 25);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveRightOverlapClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(15), pos(25), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveCompleteOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(5, 25);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveCompleteOverlapClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(5), pos(25), true);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplit() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(25), attribsA);
        hs.removeHighlights(15, 20);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplitClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(25), attribsA);
        hs.removeHighlights(pos(15), pos(20), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", atttributes.get(3));
    }

    public void testRemoveAlignedClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(0), pos(10), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
        
        hs.removeHighlights(pos(20), pos(30), true);
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveAligned2Clip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(40), attribsA);
        hs.removeHighlights(pos(10), pos(20), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 40, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
        
        hs.removeHighlights(pos(30), pos(40), true);
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 30, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveMiddle() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(20), pos(30), attribsB);
        hs.removeHighlights(15, 25);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }
    
    public void testRemoveMiddleEmptyHighlight() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(15), pos(15), false);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }
    
    public void testRemoveMiddleClip() {
        for(int i = 0; i < 10; i++) {
            removeMiddleClip(i + 1);
        }
    }

    private void removeMiddleClip(int middleMarks) {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(pos(10 * i + 10), pos(10 * i + 20), i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.removeHighlights(pos(15), pos(middleMarks * 10 + 15), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            atttributes.get(1));

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            atttributes.get(3));
    }
    
    public void testAddAll() {
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(pos(0), pos(30), attribsA);
        hsA.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marksA = hsA.getMarks();
        GapList<AttributeSet> atttributesA = hsA.getAttributes();
        
        hsB.addHighlight(pos(0), pos(40), attribsC);
        hsB.addAllHighlights(hsA);
        GapList<Position> marksB = hsB.getMarks();
        GapList<AttributeSet> atttributesB = hsB.getAttributes();
        
        assertEquals("Wrong number of highlights", marksA.size() + 1, marksB.size());
        for (int i = 0; i < marksA.size() - 1; i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i + 1).getOffset(), marksB.get(i + 1).getOffset());
            assertEquals(i + ". highlight - wrong attribs",
                atttributesA.get(i).getAttribute("set-name"),
                atttributesB.get(i).getAttribute("set-name"));
        }

        assertEquals("4. highlight - wrong start offset", 30, marksB.get(3).getOffset());
        assertEquals("4. highlight - wrong end offset", 40, marksB.get(4).getOffset());
        assertEquals("4. highlight - wrong attribs", "attribsC", atttributesB.get(3).getAttribute("set-name"));
    }

    public void testSet() {
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(pos(0), pos(30), attribsA);
        hsA.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marksA = hsA.getMarks();
        GapList<AttributeSet> atttributesA = hsA.getAttributes();
        
        hsB.addHighlight(pos(0), pos(40), attribsC);
        hsB.setHighlights(hsA);
        GapList<Position> marksB = hsB.getMarks();
        GapList<AttributeSet> atttributesB = hsB.getAttributes();
        
        assertEquals("Wrong number of highlights", marksA.size(), marksB.size());
        for (int i = 0; i < marksA.size(); i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            
            AttributeSet attrA = atttributesA.get(i);
            AttributeSet attrB = atttributesB.get(i);
            
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
        PositionsBag hs = new PositionsBag(doc);
        assertFalse("Sequence should be empty", hs.getHighlights(
            Integer.MIN_VALUE, Integer.MAX_VALUE).moveNext());
        
        hs.addHighlight(pos(10), pos(30), EMPTY);

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
        PositionsBag hb = new PositionsBag(doc);
        hb.addHighlight(pos(10), pos(20), SimpleAttributeSet.EMPTY);
        
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
        PositionsBag hb = new PositionsBag(doc);

        // Modify the bag
        hb.addHighlight(pos(5), pos(10), EMPTY);
        hb.addHighlight(pos(15), pos(20), EMPTY);
        hb.addHighlight(pos(25), pos(30), EMPTY);
        
        HighlightsSequence hs = hb.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertTrue("There should be some highlights", hs.moveNext());
        
        int s = hs.getStartOffset();
        int e = hs.getEndOffset();
        AttributeSet a = hs.getAttributes();
        
        // Modification after the sequence was acquired
        hb.addHighlight(pos(100), pos(110), EMPTY);
        
        assertEquals("Wrong highlight start", s, hs.getStartOffset());
        assertEquals("Wrong highlight end", e, hs.getEndOffset());
        assertEquals("Wrong highlight attributes", a, hs.getAttributes());
        assertFalse("There should be no more highlights after co-modification", hs.moveNext());
    }

    public void testDocumentChanges() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "01234567890123456789012345678901234567890123456789", SimpleAttributeSet.EMPTY);
        
        PositionsBag bag = new PositionsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        bag.addHighlight(doc.createPosition(0), doc.createPosition(30), attribsA);
        bag.addHighlight(doc.createPosition(10), doc.createPosition(20), attribsB);
        GapList<Position> marks = bag.getMarks();
        GapList<AttributeSet> atttributes = bag.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
        
        doc.insertString(12, "----", SimpleAttributeSet.EMPTY);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 24, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 24, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 34, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
        
        doc.remove(1, 5);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 5, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 5, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 19, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 19, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 29, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }
    
    private void dumpHighlights(HighlightsSequence seq) {
        System.out.println("Dumping highlights from: " + seq + "{");
        while(seq.moveNext()) {
            System.out.println("<" + seq.getStartOffset() + ", " + seq.getEndOffset() + ", " + seq.getAttributes() + ">");
        }
        System.out.println("} --- End of Dumping highlights from: " + seq + " ---------------------");
    }
    
    private Position pos(int offset) {
        return new SimplePosition(offset);
    }
}
