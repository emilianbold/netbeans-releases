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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalNBMsProvider implements UpdateProvider {
    private String name;
    private File [] nbms;
    
    /** Creates a new instance of LocalNBMsProvider */
    public LocalNBMsProvider (String name, File... files) {
        this.nbms = files;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return getName ();
    }

    public Map<String, UpdateItem> getUpdateItems() {
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        for (int i = 0; i < nbms.length; i++) {
            Map<String, UpdateItem> items = AutoupdateInfoParser.getUpdateItems (nbms [i]);
            assert items != null && ! items.isEmpty ();
            String id = items.keySet ().iterator ().next ();
            assert items.size () == 1 : "AutoupdateInfoParser returns " + items.get (id) + " for file " + nbms [i];
            res.put (id, items.get (id));
        }
        return res;
    }

    public boolean refresh (boolean force) {
        assert false : "Not supported yet.";
        return false;
    }

}
