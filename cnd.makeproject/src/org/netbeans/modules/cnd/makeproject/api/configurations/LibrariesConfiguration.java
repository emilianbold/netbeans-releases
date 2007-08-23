/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.Vector;

public class LibrariesConfiguration extends VectorConfiguration {
    public LibrariesConfiguration() {
	super(null);
    }

    public LibraryItem[] getLibraryItemsAsArray() {
        return (LibraryItem[])getValue().toArray(new LibraryItem[getValue().size()]);
    }

    public String getOptions(MakeConfiguration conf) {
	StringBuilder options = new StringBuilder();
	
	LibraryItem[] items = getLibraryItemsAsArray();
	for (int i = 0; i < items.length; i++) {
	    options.append(items[i].getOption(conf));
            options.append(" "); // NOI18N
	}

	return options.toString();
    }
    // Clone and Assign
    @Override
    public void assign(VectorConfiguration conf) {
	// From VectorConfiguration
	super.assign(conf);
	// From LibrariesConfiguration
    }

    @Override
    public Object clone() {
	LibrariesConfiguration clone = new LibrariesConfiguration();
	// From VectorConfiguration
	clone.setValue((Vector)getValue().clone());
	// From LibrariesConfiguration
	return clone;
    }
}
