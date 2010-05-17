/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.editor.highlights;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.text.Document;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.MarkFactory;
import org.netbeans.modules.editor.highlights.spi.*;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Lahoda
 */
final class HighlightLayer extends DrawLayer.AbstractLayer {

    static final String NAME = "highlight-layer";
    
    /** Creates a new instance of HighlightLayer */
    public HighlightLayer() {
        super(NAME); // NOI18N
        this.initialized = false;
        type2Highlights = new HashMap/*<String, SortedSet<Highlight>>*/();
        fakeHighlight = new FakeHighlight();
    }
    
    private Map/*<String, SortedSet<Highlight>>*/ type2Highlights;
    
    private Highlight fakeHighlight;
    private int       fakePosition;
    
    public static final int VISIBILITY = 3000;
    
    private boolean initialized = false;
    
    private void checkDocument(Document doc) {
    }
    
    public boolean extendsEOL() {
        return true;
    }
    
    public synchronized void init(final DrawContext ctx) {
        if (!initialized) {
            Document doc = ctx.getEditorUI().getDocument();
            
            initialized = true;
            checkDocument(doc);
        }
        
        if (isActive())
            setNextActivityChangeOffset(0);
    }
    
    private boolean isActive() {
        return true;
    }

    public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
        if (isActive()) {
            int currentOffset = ctx.getFragmentOffset();
            List/*<Highlight>*/ highlights = getHighlightsForOffset(currentOffset);
            int nextActivity = Integer.MAX_VALUE;

//        for (Highlight h : highlights) {
            for (Iterator i = highlights.iterator(); i.hasNext(); ) {
                Highlight h = (Highlight) i.next();
                
                if (h.getStart() <= currentOffset) {
                    if (nextActivity > h.getEnd()) {
                        nextActivity = h.getEnd();
                    }
                } else {
                    if (nextActivity > h.getStart()) {
                        nextActivity = h.getStart();
                    }
                }
            }

            if (nextActivity == currentOffset) {
                nextActivity++;
            }

            setNextActivityChangeOffset(nextActivity);
            return true;
        }

        return false;
    }
    
    public void updateContext(DrawContext ctx) {
        if (!isActive())
            return ;
        
        int currentOffset = /*ctx.getTokenOffset();/*/ctx.getFragmentOffset();
        List/*<Highlight>*/ highlights = getHighlightsForOffset(currentOffset);
        int nextActivity = Integer.MAX_VALUE;
        int starts = Integer.MAX_VALUE;
        boolean applied = false;

//        for (Highlight h : highlights) {
        for (Iterator i = highlights.iterator(); i.hasNext(); ) {
            Highlight h = (Highlight) i.next();
            
            if (h.getStart() <= currentOffset && currentOffset < h.getEnd()) {
                h.getColoring().apply(ctx);
                applied = true;
            }

            if (nextActivity > h.getEnd()) {
                nextActivity = h.getEnd();
            }

            if (starts > h.getStart()) {
                starts = h.getStart();
            }
        }

        if (!applied && starts < nextActivity)
            nextActivity = starts;

        setNextActivityChangeOffset(nextActivity);
    }

    public synchronized void setHighlights(String type, Collection/*<Highlight>*/ highlights) {
        SortedSet/*<Highlight>*/ target = (SortedSet) type2Highlights.get(type);
        
        if (target == null) {
            type2Highlights.put(type, target = new TreeSet/*<Highlight>*/(new HighlightComparator()));
        } else {
            target.clear();
        }
        
//        for (Highlight h : highlights) {
        for (Iterator i = highlights.iterator(); i.hasNext(); ) {
            Highlight h = (Highlight) i.next();
            
            if (h != null) {
                target.add(h);
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "Got a null highlight, type: " + type); // NOI18N
            }
        }
    }
    
    private synchronized List/*<Highlight>*/ getHighlightsForOffset(int offset) {
        List/*<Highlight>*/ highlights = new ArrayList/*<Highlight>*/();
        
        fakePosition = offset;
        
//        for (String type : type2Highlights.keySet()) {
        for (Iterator i = type2Highlights.keySet().iterator(); i.hasNext(); ) {
            String type = (String) i.next();
            
            SortedSet/*<Highlight>*/ tail = ((SortedSet) type2Highlights.get(type)).tailSet(fakeHighlight);
            
            if (!tail.isEmpty()) {
                Highlight h = (Highlight) tail.first();
                
                highlights.add(h);
            }
        }
        
        return highlights;
    }
    
    private final class FakeHighlight implements Highlight {
        
        public int getStart() {
            return -1;
        }
        
        public int getEnd() {
            return fakePosition;
        }
        
        public Coloring getColoring() {
            return null;
        }
        
    }
}
