/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.fold;

import org.netbeans.api.editor.fold.Fold;
import org.netbeans.modules.editor.fold.ApiPackageAccessor;
import org.netbeans.lib.editor.util.GapList;

//import org.netbeans.spi.lexer.util.GapObjectArray;

/**
 * Manager of the children of a fold.
 * <br>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldChildren extends GapList {
    
    /**
     * Initial size of the index gap.
     */
    private static final int INITIAL_INDEX_GAP_LENGTH
        = Integer.MAX_VALUE >> 1;

    /**
     * Parent fold for the folds contained in this children instance.
     */
    Fold parent;

   /**
     * Index where the index gap resides.
     */
    private int indexGapIndex;

    /**
     * Length of the index gap in managed folds.
     * <br>
     * The initial gap length is chosen big enough
     * so that it's never reached.
     */
    private int indexGapLength;

    public FoldChildren(Fold parent) {
        this.parent = parent;
        indexGapLength = INITIAL_INDEX_GAP_LENGTH;
    }
    
    /**
     * Get total count of subfolds contained in this fold.
     *
     * @return count of subfolds contained in this fold.
     *  Zero means there are no subfolds under this fold.
     */
    public int getFoldCount() {
        return size();
    }

    /**
     * Get fold with the given index.
     *
     * @param index &gt;=0 &amp;&amp; &lt;{@link #getFoldCount()}
     *  index of the fold.
     */
    public Fold getFold(int index) {
        return (Fold)get(index);
    }
    
    public int getFoldIndex(Fold child) {
        int index = getTranslatedFoldIndex(ApiPackageAccessor.get().foldGetRawIndex(child));
        if (index < 0 || index >= getFoldCount() || getFold(index) != child) {
            index = -1;
        }
        return index;
    }
    
    public Fold[] foldsToArray(int index, int length) {
        Fold[] folds = new Fold[length];
        copyItems(index, index + length, folds, 0);
        return folds;
    }
    
    /**
     * Insert the given fold at the requested index.
     */
    public void insert(int index, Fold fold) {
        moveIndexGap(index);
        //ensureCapacity(1);
        insertImpl(index, fold);
    }
   
    /**
     * Insert the given folds at the requested index.
     */
    public void insert(int index, Fold[] folds) {
        moveIndexGap(index);
        insertImpl(index, folds);
    }

    public void remove(int index, int length) {
        moveIndexGap(index + length);
        for (int i = index + length - 1; i >= index; i--) {
            ApiPackageAccessor.get().foldSetParent(getFold(i), null);
        }
        super.remove(index, length);
        indexGapLength += length;
        indexGapIndex -= length;
    }
    
    /**
     * Extract given area of folds into new FoldChildren instance
     * parented by the given fold.
     *
     * @param index start of the area of folds to be extracted.
     * @param length length of the area of folds to be extracted.
     * @param fold fold that will own the newly created fold children.
     */
    public FoldChildren extractToChildren(int index, int length, Fold fold) {
        FoldChildren foldChildren = new FoldChildren(fold);
        if (length == 1) {
            Fold insertFold = getFold(index);
            remove(index, length); // removal prior insertion to set children parents properly
            foldChildren.insert(0, insertFold);
        } else {
            Fold[] insertFolds = foldsToArray(index, length);
            remove(index, length); // removal prior insertion to set children parents properly
            foldChildren.insert(0, insertFolds);
        }

        // Insert the fold into list of current children
        insertImpl(index, fold);
        
        return foldChildren;
    }

    public void replaceByChildren(int index, FoldChildren children) {
        remove(index, 1);
        
        if (children != null) {
            // Index gap already moved by preceding remove()
            int childCount = children.getFoldCount();
            //ensureCapacity(childCount);
            insertImpl(index, children, 0, childCount);
        }
    }    

    private void insertImpl(int index, FoldChildren children,
    int childIndex, int childCount) {

        switch (childCount) {
            case 0: // nothing to do
                break;

            case 1: // single item insert
                insertImpl(index, children.getFold(childIndex));
                break;
                
            default: // multiple items insert
                Fold[] folds = children.foldsToArray(childIndex, childCount);
                insertImpl(index, folds);
                break;
        }
    }

    private void insertImpl(int index, Fold fold) {
        indexGapLength--;
        indexGapIndex++;
        ApiPackageAccessor api = ApiPackageAccessor.get();
        api.foldSetRawIndex(fold, index);
        api.foldSetParent(fold, parent);
        add(index, fold);
    }
    
    private void insertImpl(int index, Fold[] folds) {
        ApiPackageAccessor api = ApiPackageAccessor.get();
        int foldsLength = folds.length;
        indexGapLength -= foldsLength;
        indexGapIndex += foldsLength;
        for (int i = foldsLength - 1; i >= 0; i--) {
            Fold fold = folds[i];
            api.foldSetRawIndex(fold, index + i);
            api.foldSetParent(fold, parent);
        }
        addArray(index, folds);
    }
    
    private int getTranslatedFoldIndex(int rawIndex) {
        if (rawIndex >= indexGapLength) {
            rawIndex -= indexGapLength;
        }
        return rawIndex;
    }

    private void moveIndexGap(int index) {
        if (index != indexGapIndex) {
            ApiPackageAccessor api = ApiPackageAccessor.get();
            int gapLen = indexGapLength; // cache to local var
            if (index < indexGapIndex) { // fix back from indexGapIndex till index
                for (int i = indexGapIndex - 1; i >= index; i--) {
                    api.foldUpdateRawIndex(getFold(i), +gapLen);
                }

            } else { // index > indexGapIndex => fix up from indexGapIndex till index
                for (int i = indexGapIndex; i < index; i++) {
                    api.foldUpdateRawIndex(getFold(i), -gapLen);
                }
            }
            indexGapIndex = index;
        }
    }

}
