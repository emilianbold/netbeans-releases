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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.picklist;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * DefaultPicklistElement
 */
public class DefaultPicklistElement implements PicklistElement, Serializable {
    private static final long serialVersionUID = -8893325364784938693L;
    private String elem;

    public DefaultPicklistElement(String elem) {
	this.elem = elem;
    }

    public String getString() {
	return elem;
    }

    /**
     * Compares two PicklistElement for equality. Returns true if equeal,
     * othervise false.
     */
    public boolean equals(PicklistElement elem) {
	return ((DefaultPicklistElement)elem).getString().equals(this.elem);
    }

    /**
     * Returns a String representation of this element to be used
     * for displaying the element.
     */
    public String displayName() {
	return elem;
    }

    /**
     * Return a clone (copy) of this element
     */
    public PicklistElement cloneElement() {
	return new DefaultPicklistElement(elem);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	try {
	    out.writeObject(elem);
	}
	catch (IOException ioe) {
	    System.err.println("ExecutePicklistElement - writeObject - ioe " + ioe); // NOI18N
	    throw(ioe);
	}
    }
	                                                                                                         
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	try {
	    elem = (String)in.readObject();   
	}
	catch (IOException e) {
	    System.err.println("ExecutePicklistElement - readObject - e " + e); // NOI18N
	    throw(e);
	}
	catch (ClassNotFoundException e) {
	    System.err.println("ExecutePicklistElement - readObject - e " + e); // NOI18N
	    throw(e);
	}
    }
}
