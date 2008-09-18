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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Maintain the same text in the selected regions of the text document.
 *
 * @author Miloslav Metelka
 */
public final class TextRegionManager {
    
    // -J-Dorg.netbeans.lib.editor.codetemplates.textsync.TextRegionManager.level=FINE
    static final Logger LOG = Logger.getLogger(TextRegionManager.class.getName());
    
    private TextRegion<?> rootRegion;
    
    private List<TextSyncGroup> groups;
    
    /**
     * Text sync for which the document modifications are being replicated
     * across the respective regions.
     */
    private TextSync activeTextSync;
    
    /**
     * Runnable executed upon user's modification of active text sync.
     */
    private EditingNotify editingNotify;

    /**
     * Bounds of the activeTextSync prior last modification (during insert/removeUpdate()).
     */
    private int masterRegionStartOffset;
    private int masterRegionEndOffset;
    
    private int ignoreDocModifications;
    
    public static synchronized TextRegionManager get(Document doc) {
        if (doc == null)
            throw new IllegalArgumentException("document cannot be null"); // NOI18N
        TextRegionManager manager = (TextRegionManager)doc.getProperty(TextRegionManager.class);
        if (manager == null) {
            manager = new TextRegionManager(doc);
            doc.putProperty(TextRegionManager.class, manager);
        }
        return manager;
    }

    private Document doc;
    
    TextRegionManager(Document doc) {
        this.doc = doc;
        this.rootRegion = new TextRegion<Void>();
        this.groups = new GapList<TextSyncGroup>(2);
        if (doc instanceof BaseDocument) {
            // Add the listener to allow doc syncing modifications
            // The listener is never removed (since this object is a property of the document)
            ((BaseDocument)doc).addPostModificationDocumentListener(DocListener.INSTANCE);
        }
    }

    public Collection<TextSyncGroup> textSyncGroups() {
        return Collections.unmodifiableList(groups);
    }
    
    /**
     * Add a sync group to the manager of text regions which will cause
     * the regions to be updated by the changes performed in the document.
     * <br/>
     * This method should be called under document's readlock to ensure
     * that the document will not be modified during execution of this method.
     * 
     * @param textSyncGroup non-null text sync group.
     * @param offsetShift shift added to the offsets contained in the text regions
     *  in the group being added. The resulting offsets will be turned into positions.
     * @throws javax.swing.text.BadLocationException
     */
    public void addTextSyncGroup(TextSyncGroup textSyncGroup, int offsetShift) throws BadLocationException {
        if (textSyncGroup.textRegionManager() != null)
            throw new IllegalArgumentException("TextSyncGroup=" + textSyncGroup // NOI18N
                    + " already assigned to " + textSyncGroup.textRegionManager()); // NOI18N

        TextRegion<?> lastAdded = null;
        try {
            for (TextSync textSync : textSyncGroup.textSyncsModifiable()) {
                for (TextRegion<?> textRegion : textSync.regions()) {
                    Position startPos = doc.createPosition(textRegion.startOffset() + offsetShift);
                    Position endPos = doc.createPosition(textRegion.endOffset() + offsetShift);
                    textRegion.setStartPos(startPos);
                    textRegion.setEndPos(endPos);
                    addRegion(rootRegion, textRegion);
                    lastAdded = textRegion;
                }
            }
            lastAdded = null; // All were added
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("ADD textSyncGroup: " + textSyncGroup + '\n');
            }
        } finally {
            removeAddedSoFar(textSyncGroup, lastAdded);
        }
        textSyncGroup.setTextRegionManager(this);
    }
    
    private void removeAddedSoFar(TextSyncGroup textSyncGroup, TextRegion<?> lastAdded) {
        // Created as a method (instead of a cycle in finally { } due to a crashing javac during compilation
        while (lastAdded != null) { // Remove what was added so far
            for (TextSync textSync : textSyncGroup.textSyncsModifiable()) {
                for (TextRegion<?> textRegion : textSync.regions()) {
                    removeRegionFromParent(textRegion);
                    if (textRegion == lastAdded) {
                        return;
                    }
                }
            }
        }
    }

    public void removeTextSyncGroup(TextSyncGroup textSyncGroup) {
        textSyncGroup.setTextRegionManager(null);
        for (TextSync textSync : textSyncGroup.textSyncsModifiable()) {
            for (TextRegion<?> textRegion : textSync.regions()) {
                removeRegionFromParent(textRegion);
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("REMOVE textSyncGroup: " + textSyncGroup + '\n');
        }
    }
    
    void setActiveTextSync(TextSync textSync, EditingNotify editingNotify) {
        if (textSync.masterRegion() == null)
            throw new IllegalArgumentException("masterRegion expected to be non-null"); // NOI18N
        this.activeTextSync = textSync;
        this.editingNotify = editingNotify;
        updateMasterRegionBounds();
    }
    
    List<TextRegion<?>> regions() { // For tests only
        return rootRegion.regions();
    }

    void insertUpdate(DocumentEvent evt) {
        if (ignoreDocModifications == 0) {
            if (activeTextSync != null) {
                ignoreDocModifications++;
                try {
                    int offset = evt.getOffset();
                    int insertLength = evt.getLength();
                    String insertText = DocumentUtilities.getModificationText(evt);
                    if (insertText == null) {
                        try {
                            insertText = doc.getText(offset, insertLength);
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e); // Should never happen
                        }
                    }
                    boolean syncSuccess = false;
                    if (offset > masterRegionStartOffset) {
                        if (offset <= masterRegionEndOffset) { // Within master region
                            TextRegion<?> master = activeTextSync.validMasterRegion();
                            int relOffset = offset - master.startOffset();
                            if (relOffset <= 0) {
                                // See #146105 - the undo will cause the master's start position
                                // to be above the insertion point => offset < 0
                                outsideModified(evt);
                                return;
                            }
                            beforeDocumentModification();
                            try {
                                for (TextRegion<?> region : activeTextSync.regions()) {
                                    if (region != master) {
                                        doc.insertString(region.startOffset() + relOffset, insertText, null);
                                    }
                                }
                            } finally {
                                afterDocumentModification();
                            }
                            syncSuccess = true;
                        }

                    } else if (offset == masterRegionStartOffset) { // Insert at begining of master region
                        // This will require fixing of regions' start positions.
                        // In adition adjacent regions may need to be fixed too if they were
                        // ending at the begining of the region which start position was fixed.
                        TextRegion<?> master = activeTextSync.validMasterRegion();
                        fixRegionStartOffset(master, offset);
                        beforeDocumentModification();
                        try {
                            for (TextRegion<?> region : activeTextSync.regions()) {
                                if (region != master) {
                                    int startOffset = region.startOffset();
                                    doc.insertString(startOffset, insertText, null);
                                    fixRegionStartOffset(region, startOffset);
                                }
                            }
                        } finally {
                            afterDocumentModification();
                        }
                        syncSuccess = true;

                    } // otherwise below master region

                    if (syncSuccess) {
                        editingNotify.modified(evt);
                    } else { // Not synced successfully
                        // In case this was typing modification the synced editing should end
                        if (DocumentUtilities.isTypingModification(doc)) {
                            outsideModified(evt);
                        }
                    }
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, "Unexpected exception during synchronization", e); // NOI18N
                } finally {
                    assert (ignoreDocModifications > 0);
                    ignoreDocModifications--;
                }
            } else { // activeTextSync == null
                if (DocumentUtilities.isTypingModification(doc)) {
                    outsideModified(evt);
                }
            }
        } // else controlled doc modification to be ignored
        updateMasterRegionBounds();
    }
    
    void removeUpdate(DocumentEvent evt) {
        if (ignoreDocModifications == 0) {
            if (activeTextSync != null) {
                ignoreDocModifications++;
                try {
                    int offset = evt.getOffset();
                    int removeLength = evt.getLength();
                    if (offset >= masterRegionStartOffset && offset + removeLength <= masterRegionEndOffset) {
                        TextRegion<?> master = activeTextSync.validMasterRegion();
                        int relOffset = offset - master.startOffset();
                        beforeDocumentModification();
                        try {
                            for (TextRegion<?> region : activeTextSync.regions()) {
                                if (region != master) {
                                    doc.remove(region.startOffset() + relOffset, removeLength);
                                }
                            }
                            editingNotify.modified(evt);
                        } catch (BadLocationException e) {
                            outsideModified(evt);
                            LOG.log(Level.WARNING, "Unexpected exception during synchronization", e); // NOI18N
                        } finally {
                            afterDocumentModification();
                        }
                    } else { // Not synced successfully
                        if (DocumentUtilities.isTypingModification(doc)) {
                            outsideModified(evt);
                        }
                    }
                } finally {
                    assert (ignoreDocModifications > 0);
                    ignoreDocModifications--;
                }
            } else {
                if (DocumentUtilities.isTypingModification(doc)) {
                    outsideModified(evt);
                }
            }
        } // else controlled doc modification to be ignored
        updateMasterRegionBounds();
    }
    
    private void beforeDocumentModification() {
        doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
    }
    
    private void afterDocumentModification() {
        doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
    }
    
    
    
    private void outsideModified(DocumentEvent evt) {
        EditingNotify notify = editingNotify; // assign before clearing
        clearActiveTextSync();
        if (notify != null)
            notify.outsideModified(evt);
    }

    void clearActiveTextSync() {
        activeTextSync = null;
        editingNotify = null;
    }
    
    void syncByMaster(TextSync textSync) {
        beforeDocumentModification();
        ignoreDocModifications++;
        try {
            TextRegion<?> masterRegion = textSync.validMasterRegion();
            CharSequence docText = DocumentUtilities.getText(doc);
            CharSequence masterRegionText = docText.subSequence(
                    masterRegion.startOffset(), masterRegion.endOffset());
            String masterRegionString = null;
            for (TextRegion<?> region : textSync.regionsModifiable()) {
                if (region == masterRegion)
                    continue;
                int regionStartOffset = region.startOffset();
                int regionEndOffset = region.endOffset();
                CharSequence regionText = docText.subSequence(regionStartOffset, regionEndOffset);
                if (!CharSequenceUtilities.textEquals(masterRegionText, regionText)) {
                    // Must re-insert
                    if (masterRegionString == null)
                        masterRegionString = masterRegionText.toString();
                    doc.remove(regionStartOffset, regionEndOffset - regionStartOffset);
                    doc.insertString(regionStartOffset, masterRegionString, null);
                    fixRegionStartOffset(region, regionStartOffset);
                }
            }
        } catch (BadLocationException e) {
           LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N 
        } finally {
            assert (ignoreDocModifications > 0);
            ignoreDocModifications--;
            afterDocumentModification();
        }
        updateMasterRegionBounds();
    }
    
    void setText(TextSync textSync, String text) {
        beforeDocumentModification();
        ignoreDocModifications++;
        try {
            CharSequence docText = DocumentUtilities.getText(doc);
            for (TextRegion<?> region : textSync.regionsModifiable()) {
                int regionStartOffset = region.startOffset();
                int regionEndOffset = region.endOffset();
                CharSequence regionText = docText.subSequence(regionStartOffset, regionEndOffset);
                if (!CharSequenceUtilities.textEquals(text, regionText)) {
                    // Must re-insert
                    doc.remove(regionStartOffset, regionEndOffset - regionStartOffset);
                    doc.insertString(regionStartOffset, text, null);
                    fixRegionStartOffset(region, regionStartOffset);
                }
            }
        } catch (BadLocationException e) {
           LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N 
        } finally {
            assert (ignoreDocModifications > 0);
            ignoreDocModifications--;
            afterDocumentModification();
        }
        updateMasterRegionBounds();
    }
    
    private void fixRegionStartOffset(TextRegion<?> region, int offset) throws BadLocationException {
        Position pos = doc.createPosition(offset);
        region.setStartPos(pos);
        TextRegion<?> parent = region.parent();
        List<TextRegion<?>> regions = parent.regions();
        int index = findRegionIndex(regions, region) - 1;
        while (index >= 0) {
            region = regions.get(index);
            if (region.endOffset() > offset)
                region.setEndPos(pos);
            else
                break;
            if (region.startOffset() > offset)
                region.setStartPos(pos);
            else
                break;
        }
        if (index < 0) { // Fixed first region -> check parent
            if (!isRoot(parent) && parent.startOffset() > offset) {
                parent.setStartPos(pos);
                fixRegionStartOffset(parent, offset);
            }
        }
    }
    
    private boolean isRoot(TextRegion region) {
        return (region == rootRegion);
    }
    
    private void updateMasterRegionBounds() {
        if (activeTextSync != null) {
            masterRegionStartOffset = activeTextSync.masterRegion().startOffset();
            masterRegionEndOffset = activeTextSync.masterRegion().endOffset();
        }
    }
    
    /**
     * Get index at which the region's start offset is greater than the given offset.
     * 
     * @param regions regions in which to search.
     * @param offset >=0 offset 
     * @return index >=0 index at which the region's start offset is greater than the given offset.
     */
    static int findRegionInsertIndex(List<TextRegion<?>> regions, int offset) {
        int low = 0;
        int high = regions.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            TextRegion<?> midRegion = regions.get(mid);
            int midStartOffset = midRegion.startOffset();

            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Region starting exactly at startOffset found -> get index above it
                low = mid + 1;
                while (low < regions.size() && regions.get(low).startOffset() == offset) {
                    low++;
                }
                break;
            }
        }
        return low;
    }
    
    static int findRegionIndex(List<TextRegion<?>> regions, TextRegion<?> region) {
        int low = 0;
        int high = regions.size() - 1;
        int offset = region.startOffset();
        while (low <= high) {
            int mid = (low + high) / 2;
            TextRegion<?> midRegion = regions.get(mid);
            int midStartOffset = midRegion.startOffset();

            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Region starting exactly at startOffset found
                if (midRegion == region)
                    return mid;
                // Search backward in adjacent regions
                low = mid - 1;
                while (low >= 0) {
                    midRegion = regions.get(low);
                    if (midRegion == region)
                        return low;
                    if (midRegion.startOffset() != offset)
                        break;
                    low--;
                }
                // Search forward in adjacent regions
                low = mid + 1;
                while (low < regions.size()) {
                    midRegion = regions.get(low);
                    if (midRegion == region)
                        return low;
                    if (midRegion.startOffset() != offset)
                        throw new IllegalStateException("Region: " + region + " not found."); // NOI18N
                    low++;
                }
                break;
            }
        }
        throw new IllegalStateException("Region: " + region + " not found."); // NOI18N
    }
    
    static void addRegion(TextRegion<?> parent, TextRegion<?> region) {
        if (region.parent() != null)
            throw new IllegalArgumentException("Region:" + region + " already added."); // NOI18N
        List<TextRegion<?>> regions = parent.validRegions();
        int regionStartOffset = region.startOffset();
        int regionEndOffset = region.endOffset();
        int insertIndex = findRegionInsertIndex(regions, regionStartOffset);
        // Check the regions containment and overlapping
        // Prefer containment of existing regions into the one being inserted
        // since the inserted one's positions will likely be produced later
        // so possible undo of removals would retain containment.
        int endConsumeIndex = insertIndex; // >0 if region-param consumes existing regions
        while (endConsumeIndex < regions.size()) {
            TextRegion<?> consumeCandidate = regions.get(endConsumeIndex);
            if (regionEndOffset < consumeCandidate.endOffset()) { // region-param does not fully contain consumeCandidate
                if (regionEndOffset <= consumeCandidate.startOffset()) { // Region and consumeCandidate do not overlap
                    break;
                } else {
                    throw new IllegalArgumentException("Inserted region " + region + // NOI18N
                            " overlaps with region " + consumeCandidate + // NOI18N
                            " at index=" + endConsumeIndex // NOI18N
                        );
                }
            } // otherwise Region fully contains consumeCandidate
            endConsumeIndex++;
        }
        while (insertIndex > 0) {
            TextRegion<?> prev = regions.get(insertIndex - 1);
            int prevEndOffset;
            if (regionStartOffset == prev.startOffset()) { // region-param eats prev?
                if (regionEndOffset < (prevEndOffset = prev.endOffset())) { // region-param inside prev
                    if (regionStartOffset != regionEndOffset) { // region-param is non-empty 
                        addRegion(prev, region);
                        return;
                    } else { // Region will be inserted right before this region
                        insertIndex--;
                        endConsumeIndex = insertIndex;
                        break;
                    }
                } // Region consumes prev - continue
            } else { // startOffset > prevStartOffset
                if (regionStartOffset >= (prevEndOffset = prev.endOffset())) { // Region does not overlap prev
                    break;
                } else if (regionEndOffset <= prevEndOffset) { // Region nests into prev
                    addRegion(prev, region);
                    return;
                } else {
                    throw new IllegalArgumentException("Inserted region " + region + // NOI18N
                            " overlaps with region " + prev + // NOI18N
                            " at index=" + (insertIndex - 1)); // NOI18N
                }
            }
            insertIndex--;
        }
        if (endConsumeIndex - insertIndex > 0) { // Do consume
            GapList<TextRegion<?>> regionsGL = (GapList<TextRegion<?>>)regions;
            TextRegion<?>[] consumedRegions = new TextRegion<?>[endConsumeIndex - insertIndex];
            regionsGL.copyElements(insertIndex, endConsumeIndex, consumedRegions, 0);
            regionsGL.remove(insertIndex, consumedRegions.length);
            region.initRegions(consumedRegions);
            for (TextRegion<?> r : consumedRegions)
                r.setParent(region);
        }
        regions.add(insertIndex, region);
        region.setParent(parent);
    }
    
    static void removeRegionFromParent(TextRegion<?> region) {
        TextRegion<?> parent = region.parent();
        List<TextRegion<?>> regions = parent.regions();
        int index = findRegionIndex(regions, region);
        regions.remove(index);
        region.setParent(null);
        // Move possible children to the regions
        List<TextRegion<?>> children = region.regions();
        if (children != null) {
            for (TextRegion<?> child : children) {
                regions.add(index++, child);
                child.setParent(parent);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Managed regions:\n");
        dumpRegions(sb, rootRegion.regions(), 4);
        if (activeTextSync != null) {
            sb.append("  Active textSync: ").append(activeTextSync);
        }
        return sb.toString();
    }
    
    private void dumpRegions(StringBuilder sb, List<TextRegion<?>> regions, int indent) {
        if (regions == null)
            return;
        for (TextRegion<?> region : regions) {
            ArrayUtilities.appendSpaces(sb, indent);
            sb.append(region).append('\n');
            dumpRegions(sb, region.regions(), indent + 4);
        }
    }
    
    static interface EditingNotify {
        
        void modified(DocumentEvent evt);
        
        void outsideModified(DocumentEvent evt);

    }

    private static final class DocListener implements DocumentListener {
        
        static final DocListener INSTANCE = new DocListener();

        public void insertUpdate(DocumentEvent e) {
            TextRegionManager.get(e.getDocument()).insertUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            TextRegionManager.get(e.getDocument()).removeUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
        }

    }

}
