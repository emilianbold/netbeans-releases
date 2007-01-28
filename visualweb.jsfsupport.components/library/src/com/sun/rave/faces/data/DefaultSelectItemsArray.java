/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.rave.faces.data;

import java.util.ArrayList;
import javax.faces.model.SelectItem;
import com.sun.rave.faces.util.ComponentBundle;

public class DefaultSelectItemsArray extends ArrayList {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(DefaultSelectItemsArray.class);

    public DefaultSelectItemsArray() {
        super();
        add(new SelectItem(bundle.getMessage("item1"))); //NOI18N
        add(new SelectItem(bundle.getMessage("item2"))); //NOI18N
        add(new SelectItem(bundle.getMessage("item3"))); //NOI18N
    }

    public void setItems(String[] items) {
        clear();
        for (int i = 0; items != null && i < items.length; i++) {
            add(new SelectItem(items[i]));
        }
    }

    public String[] getItems() {
        ArrayList items = new ArrayList();
        for (int i = 0; i < size(); i++) {
            items.add(((SelectItem)get(i)).getValue());
        }
        return (String[])items.toArray(new String[items.size()]);
    }
}
