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

package org.netbeans.modules.asm.core.assistance;

import java.awt.Color;
import java.util.Iterator;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

import org.netbeans.modules.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.asm.model.AsmModelAccessor;
import org.netbeans.modules.asm.model.AsmState;
import org.netbeans.modules.asm.model.lang.OperandElement;
import org.netbeans.modules.asm.model.util.IntervalSet;
import org.netbeans.modules.asm.core.assistance.RegisterHighlightAction.HighlightEntry;
import org.netbeans.modules.asm.core.ui.top.RegisterUsagesPanel;


public class RegisterHighlightAssistance extends AbstractHighlightsContainer  
                        implements CaretListener, AsmModelAccessor.ParseListener {

    public static final String LAYER_NAME = "reg-highlight-layer";
    
    public static final Color READ_COLORING = RegisterUsagesPanel.READ_COLOR;
    public static final Color WRITE_COLORING = RegisterUsagesPanel.WRITE_COLOR;

    private JTextComponent pane;
  
    private IntervalSet<HighlightEntry> lastHighlight;
    
    private RegisterHighlightAction act;    

    public RegisterHighlightAssistance(JTextComponent pane, Document doc) {                     
        this.pane = pane;
       
        lastHighlight = new IntervalSet<HighlightEntry>();
        act = new RegisterHighlightAction();
        
        pane.addCaretListener(this);                                               
        
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(doc);
        if (acc == null) {
            return;
        }        
        acc.addParseListener(this); 
    }

    private void update() {
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(pane);
        if (acc == null)  {
            return;
        }  
        
        AsmState state = acc.getState();
                
        int pos = pane.getCaretPosition();
                      
        IntervalSet<HighlightEntry> newHighlight = act.getHighlight(state, pos);
        
        int start = 0, end = 0;
        if (newHighlight.isEmpty() && lastHighlight.isEmpty()) {
            return;
        }
        
        if (newHighlight.isEmpty()) {
            start = lastHighlight.getLowerBound();
            end = lastHighlight.getUpperBound();
        } else if (lastHighlight.isEmpty()) {
            start = newHighlight.getLowerBound();
            end = newHighlight.getUpperBound();
        } else {
            start = Math.min(lastHighlight.getLowerBound(), 
                             newHighlight.getLowerBound());
            end = Math.max(lastHighlight.getUpperBound(), 
                             newHighlight.getUpperBound());
        }
          
        lastHighlight = newHighlight;
        
        super.fireHighlightsChange(start, end);        
    }
      
    public void caretUpdate(CaretEvent e) {
        update();
    }

    public void notifyParsed() {
        update();
    }
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {                
        return new RegHighlightsSequence(
                    lastHighlight.getFromBounds(startOffset, endOffset)
                                        );
    }
       
    private static class RegHighlightsSequence implements HighlightsSequence {

        private HighlightEntry cur;
        private Iterator<HighlightEntry> it;
                
        public RegHighlightsSequence(IntervalSet<HighlightEntry> acc) {
            it = acc.iterator();           
        }
        
        public boolean moveNext() {            
            if (!it.hasNext()) {
                return false;
            }
            
            cur = it.next();
            
            return true;
        }

        public int getStartOffset() {
            return cur.getStartOffset();
        }

        public int getEndOffset() {
            return cur.getEndOffset();
        }

        public AttributeSet getAttributes() {
            Color res = cur.getUsage().isContain(OperandElement.Usage.OP_USE_WRITE) 
                        ? WRITE_COLORING : READ_COLORING;
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(StyleConstants.Background, res);
            
            return attrs;
        }        
    }      
}
