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
