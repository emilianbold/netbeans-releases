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
package org.netbeans.modules.mashup.db.ui.model;

import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.openide.util.NbBundle;

import com.sun.sql.framework.utils.StringUtil;

/**
 * Concrete bean wrapper for instances of FlatfileDatabaseModel, exposing read-only
 * properties for display in a Flatfile Database definition property sheet. <br>
 * <br>
 * TODO Extend to a mutable class (adding setters as required) to allow editing of Flat
 * file database definition properties.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileDatabase {

    private FlatfileDatabaseModel mDelegate;

    public FlatfileDatabase(FlatfileDatabaseModel dbModel) {
        mDelegate = dbModel;
    }

    public String getName() {
        return mDelegate.getModelName();
    }

    public String getDescription() {
        String desc = mDelegate.getModelDescription();
        return (StringUtil.isNullString(desc)) ? NbBundle.getMessage(FlatfileDatabase.class, "LBL_none_placeholder") : desc;
    }

    public FlatfileDatabaseModel getDeligate() {
        return mDelegate;
    }
}

