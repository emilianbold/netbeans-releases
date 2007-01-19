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

package org.netbeans.spi.editor.highlighting.performance;

import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class PositionsBagFindHighlightTest extends NbTestCase {

    public static TestSuite suite() {
        return NbTestSuite.speedSuite(PositionsBagFindHighlightTest.class, 2, 3);
    }

    private int cnt = 0;
    private PositionsBag bag = null;
    private int startOffset;
    private int endOffset;
    
    /** Creates a new instance of PerfTest */
    public PositionsBagFindHighlightTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        cnt = this.getTestNumber();
        bag = new PositionsBag(new PlainDocument(), false);
        
        for(int i = 0; i < cnt; i++) {
            bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i * 10 + 5), SimpleAttributeSet.EMPTY);
        }

        startOffset = 10 * cnt / 5 - 1;
        endOffset = 10 * (cnt/ 5 + 1) - 1;
        
        System.out.println("cnt = " + cnt + " : startOffset = " + startOffset + " : endOffset = " + endOffset);
    }
    
    public void testFindHighlight10() {
        HighlightsSequence seq = bag.getHighlights(startOffset, endOffset);
    }
    
    public void testFindHighlight10000() {
        HighlightsSequence seq = bag.getHighlights(startOffset, endOffset);
    }
}
