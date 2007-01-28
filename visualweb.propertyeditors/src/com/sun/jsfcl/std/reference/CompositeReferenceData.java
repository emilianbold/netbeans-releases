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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.jsfcl.std.reference;

import java.util.List;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CompositeReferenceData extends ReferenceData {

    protected BaseReferenceData baseReferenceData;
    protected List itemsSorted;
    protected ProjectAttachedReferenceData projectReferenceData;
    protected Object projectVersionMarker;
    protected DesignPropertyAttachedReferenceData livePropertyReferenceData;

    /**
     * @param manager
     * @param definer
     */
    public CompositeReferenceData(
        ReferenceDataManager manager,
        String name,
        ReferenceDataDefiner definer,
        BaseReferenceData baseReferenceData,
        ProjectAttachedReferenceData projectReferenceData,
        DesignPropertyAttachedReferenceData livePropertyReferenceData) {

        super(manager, definer, name);
        this.name = name;
        this.baseReferenceData = baseReferenceData;
        this.projectReferenceData = projectReferenceData;
        if (projectReferenceData != null) {
            definer = projectReferenceData.getDefiner();
        }
        this.livePropertyReferenceData = livePropertyReferenceData;
    }

    public void add(ReferenceDataItem item) {

        if (projectReferenceData != null) {
            projectReferenceData.add(item);
            invalidateItemsCache();
        }
    }

    public boolean canAddRemoveItems() {

        if (projectReferenceData != null && projectReferenceData.canAddRemoveItems()) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.jsfcl.std.reference.ReferenceData#addToItems(java.util.List)
     */
    protected void defineItems() {

        if (baseReferenceData != null) {
            items.addAll(baseReferenceData.getItems());
        }
        if (projectReferenceData != null) {
            items.addAll(projectReferenceData.getItems());
        }
        if (livePropertyReferenceData != null) {
            items.addAll(livePropertyReferenceData.getItems());
        }
    }

    public List getItems() {

        if (items != null && projectReferenceData != null &&
            projectReferenceData.getVersionMarker() != projectVersionMarker) {
            invalidateItemsCache();
            projectVersionMarker = projectReferenceData.getVersionMarker();
        }
        return super.getItems();
    }

    public List getItemsSorted() {

        if (itemsSorted != null && projectReferenceData != null &&
            projectReferenceData.getVersionMarker() != projectVersionMarker) {
            invalidateItemsCache();
        }
        if (itemsSorted == null) {
            itemsSorted = ReferenceDataItem.sorted(getItems());
        }
        return itemsSorted;
    }

    public void invalidateItemsCache() {

        super.invalidateItemsCache();
        itemsSorted = null;
    }

    public void invalidateDesignContextRelatedCaches() {

        if (livePropertyReferenceData != null) {
            livePropertyReferenceData.invalidateItemsCache();
            invalidateItemsCache();
        }
    }

    public void remove(ReferenceDataItem item) {

        if (projectReferenceData != null) {
            projectReferenceData.remove(item);
            invalidateItemsCache();
        }
    }

}
