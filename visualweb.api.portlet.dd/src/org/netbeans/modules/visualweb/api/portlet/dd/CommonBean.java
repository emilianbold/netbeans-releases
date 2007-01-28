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
 * This interface is the intersection of all generated methods.
 *
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public interface CommonBean {
	public void changePropertyByName(String name, Object value);

	public org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[] childBeans(boolean recursive);

	public void childBeans(boolean recursive, java.util.List beans);

	public boolean equals(Object o);

	public Object fetchPropertyByName(String name);

	public int hashCode();

	public String nameChild(Object childObj);

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName);

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName);

	public String nameSelf();

	public void readNode(org.w3c.dom.Node node);

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes);

	public String toString();

	public void validate() throws org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException;

	public void writeNode(java.io.Writer out) throws java.io.IOException;

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException;

}
