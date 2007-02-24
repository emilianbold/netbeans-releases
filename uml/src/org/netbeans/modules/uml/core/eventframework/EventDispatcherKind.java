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


package org.netbeans.modules.uml.core.eventframework;

/**
 * @author sumitabhk
 *
 */
public class EventDispatcherKind {

	public static int EDK_WORKSPACE	= 0;
	public static int EDK_DRAWINGAREA	= EDK_WORKSPACE + 1;
	public static int EDK_PROJECTTREE	= EDK_DRAWINGAREA + 1;
	public static int EDK_ELEMENTMODIFIED	= EDK_PROJECTTREE + 1;
	public static int EDK_EDITCTRL	= EDK_ELEMENTMODIFIED + 1;
	public static int EDK_PROJECTTREEDIALOGFILTER	= EDK_EDITCTRL + 1;
	public static int EDK_ADDIN	= EDK_PROJECTTREEDIALOGFILTER + 1;
	public static int EDK_MESSAGING	= EDK_ADDIN + 1;
	public static int EDK_VBA	= EDK_MESSAGING + 1;
	public static int EDK_RELATION	= EDK_VBA + 1;
	public static int EDK_ELEMENT_LIFETIME	= EDK_RELATION + 1;
	public static int EDK_CLASSIFIER	= EDK_ELEMENT_LIFETIME + 1;
	public static int EDK_COREPRODUCT	= EDK_CLASSIFIER + 1;
	public static int EDK_PREFERENCEMANAGER	= EDK_COREPRODUCT + 1;
	public static int EDK_SCM	= EDK_PREFERENCEMANAGER + 1;
	public static int EDK_DYNAMICS	= EDK_SCM + 1;
	public static int EDK_STRUCTURE	= EDK_DYNAMICS + 1;
	public static int EDK_ACTIVITIES	= EDK_STRUCTURE + 1;
	
	/**
	 * 
	 */
	public EventDispatcherKind() {
		super();
	}

}



