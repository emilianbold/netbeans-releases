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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

/**
 * @author josephg
 *
 */
public class QuadrantKind {
	private String m_name = null;
	private QuadrantKind(String name) {
		m_name = name;
	}
	
	public String toString() {
		return m_name;
	}
	
	public final static QuadrantKind TOP = new QuadrantKind("Top");
	public final static QuadrantKind BOTTOM = new QuadrantKind("Bottom");
	public final static QuadrantKind LEFT = new QuadrantKind("Left");
	public final static QuadrantKind RIGHT = new QuadrantKind("Right");
	public final static QuadrantKind ERROR = new QuadrantKind("Error");
}


