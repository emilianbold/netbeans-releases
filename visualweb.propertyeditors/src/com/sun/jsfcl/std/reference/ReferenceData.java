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

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ReferenceData {

    protected ReferenceDataDefiner definer;
    protected List items;
    protected ReferenceDataManager manager;
    protected String name;

    public ReferenceData(ReferenceDataManager manager, ReferenceDataDefiner definer, String name) {

        this.name = name;
        this.manager = manager;
        this.definer = definer;
    }

    public boolean canAddRemoveItems() {
        return getDefiner().canAddRemoveItems();
    }
    
    public boolean canOrderItems() {
        return getDefiner().canOrderItems();
    }

    protected abstract void defineItems();

    public ReferenceDataDefiner getDefiner() {

        return definer;
    }

    public String getDisplayName() {

        return BundleHolder.bundle.getMessage(getName());
    }

    public List getItems() {

        if (items == null) {
            items = new ArrayList(16);
            defineItems();
        }
        return items;
    }

    public String getChooseOneTitle() {

        return BundleHolder.bundle.getMessage("chooseA", getName()); // NOI18N
    }

    public String getChooseManyTitle() {

        return BundleHolder.bundle.getMessage("ChooseMany_" + getName()); // NOI18N
    }

    public String getChooseManyOfManyTitle() {

        return BundleHolder.bundle.getMessage("ChooseManyOfMany_" + getName()); //NOI18N
    }

    public String getName() {

        return name;
    }

    public void invalidateItemsCache() {

        items = null;
    }

    public boolean isValueAString() {

        return getDefiner().isValueAString();
    }
}
