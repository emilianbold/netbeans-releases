/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.openide.ErrorManager;

/**
 * Maintain the same text in the selected regions of the text document.
 *
 * @author Miloslav Metelka
 */
public final class SyncDocumentRegion {
    
    private Document doc;
    
    private List/*<MutablePositionRegion>*/ regions;
    
    private List/*<MutablePositionRegion>*/ sortedRegions;
    
    private boolean regionsSortPerformed;
    
    /**
     * Construct synchronized document regions.
     *
     * @param doc document on which to operate.
     * @param regions regions that should be kept synchronized.
     *  The first region is the master. All the regions need to have
     *  the initial position to have the backward bias.
     */
    public SyncDocumentRegion(Document doc, List/*<MutablePositionRegion>*/ regions) {
        this.doc = doc;
        this.regions = regions;
        // Check bounds correctness and whether they are sorted
        regionsSortPerformed = PositionRegion.isRegionsSorted(regions);
        if (regionsSortPerformed) {
            sortedRegions = regions;
        } else {
            sortedRegions = new ArrayList(regions);
            Collections.sort(sortedRegions, PositionRegion.getComparator());
        }
    }
    
    public int getRegionCount() {
        return regions.size();
    }
    
    public MutablePositionRegion getRegion(int regionIndex) {
        return (MutablePositionRegion)regions.get(regionIndex);
    }

    public int getFirstRegionStartOffset() {
        return getRegion(0).getStartOffset();
    }
    
    public int getFirstRegionEndOffset() {
        return getRegion(0).getEndOffset();
    }
    
    public int getFirstRegionLength() {
        return getFirstRegionEndOffset() - getFirstRegionStartOffset();
    }
    
    /**
     * Get region in a sorted list of the regions.
     *
     * @param regionIndex of the region.
     * @return region in a sorted list of the regions.
     */
    public MutablePositionRegion getSortedRegion(int regionIndex) {
         return (MutablePositionRegion)sortedRegions.get(regionIndex);
    }

    /**
     * Propagate text of the first region into all other regions.
     *
     * @param moveStartDownLength how much to move starting position
     *  down. It may be 0 to signal that the startng position should
     *  stay as is.
     */
    public void sync(int moveStartDownLength) {
        if (moveStartDownLength != 0) {
            // Move first region's start offset down
            MutablePositionRegion firstRegion = getRegion(0);
            try {
                Position newStartPos = doc.createPosition(
                        firstRegion.getStartOffset() - moveStartDownLength);
                firstRegion.setStartPosition(newStartPos);
                
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
        }
        
        String firstRegionText = getFirstRegionText();
        if (firstRegionText != null) {
            int regionCount = getRegionCount();
            for (int i = 1; i < regionCount; i++) {
                MutablePositionRegion region = getRegion(i);
                int offset = region.getStartOffset();
                int length = region.getEndOffset() - offset;
                try {
                    if (!firstRegionText.equals(doc.getText(offset, length))) {
                        doc.remove(offset, length);
                        if (firstRegionText.length() > 0) {
                            doc.insertString(offset, firstRegionText, null);
                        }
                    }
                    // Recreate the start position as the position are put together
                    Position newStartPos = doc.createPosition(offset);
                    region.setStartPosition(newStartPos);
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }

            }
        }
    }

    private String getFirstRegionText() {
        return getRegionText(0);
    }
    
    private String getRegionText(int regionIndex) {
        try {
            MutablePositionRegion region = getRegion(regionIndex);
            int offset = region.getStartOffset();
            int length = region.getEndOffset() - offset;
            return doc.getText(offset, length);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
}
