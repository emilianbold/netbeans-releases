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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.xml;

import java.util.Vector;


/**
 * Utility class for constructing {@link AttrValuePair}s to be passed to
 * {@link XMLEncoderStream}.
 * <p>
 * There is no need to escape attribute values.
 * <p>
 * <pre>
 XMLEncoderStream xes;
 ...
 AttrValuePairs pairs = new AttrValuePairs();
 pairs.add("firstName", person.getFirstName());
 pairs.add("lastName", person.getLastName());
 xes.elementOpen("person", pairs.toArray());
 * </pre>
 */

public class AttrValuePairs {
    private Vector vector = new Vector();

    public void add(String name, String value) {
	AttrValuePair attr;
	// we used to escape the values here, but moved to AttrValuePair
	// constructor
	attr = new AttrValuePair(name, value);
	vector.add(attr);
    }

    public AttrValuePair[] toArray() {
	return (AttrValuePair[])
	    vector.toArray(new AttrValuePair[vector.size()]);
    }
}
