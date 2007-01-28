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
/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public interface PreferenceTypeInterface {
	public void setId(java.lang.String value);

	public java.lang.String getId();

	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setValue(java.lang.String[] value);

	public void setValue(int index, java.lang.String value);

	public java.lang.String[] getValue();

	public java.util.List fetchValueList();

	public java.lang.String getValue(int index);

	public int sizeValue();

	public int addValue(java.lang.String value);

	public int removeValue(java.lang.String value);

	public void setReadOnly(java.lang.String value);

	public java.lang.String getReadOnly();

}
