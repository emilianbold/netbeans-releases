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


/*
 * Created on Jun 6, 2003
 *
 *
 */
/**
 * This class is a common resource for icons.  If you call GetIconFor... then you need to manage
 * the destruction of the HICON.  If you only want icons loaded once per type of object you need
 * to get the details and load them yourself using the GetIconDetailsFor...  The project tree uses
 * the details calls cause it puts the icons into an image list and only needs one icon per, for
 * example, "Class".  The we report uses the other interface cause it requires a unique HICON per
 * entry in its tree.
 */

package org.netbeans.modules.uml.ui.support.commonresources;

import javax.swing.Icon;

/**
 * @author sumitabhk
 *
 * 
 */
public interface ICommonResourceManager
{
//	 Returns an HICON (as a long) for the name.  sKeyname may be an element type ie Class.  The caller manages the HICON destruction. 
//  HRESULT GetIconForElementType([in]BSTR sKeyname, 
//								[out,retval]long* pIcon);

	public Icon getIconForElementType(String sKeyname);

//	 Returns an HICON (as a long) for the IDispatch.  The pDisp can be an IElement, IDiagram or IProxyDiagram.  The caller manages the HICON destruction. 
//  HRESULT GetIconForDisp([in]IDispatch* pDisp,  
//						 [out,retval]long* pIcon);
	public Icon getIconForDisp(Object pDisp);

//	 Returns an HICON (as a long) for the diagram kind (of type DiagramKind).  The caller manages the HICON destruction. 
//  HRESULT GetIconForDiagramKind([in]long nDiagramKind,  
//								[out,retval]long* pIcon);
	public Icon getIconForDiagramKind(int nDiagramKind);

//	 Returns the information about a specific icon.  Use to load the icon yourself - for instance if you need to put into an image list. 
//  HRESULT GetIconDetailsForElementType([in]BSTR sKeyname, 
//									   [out]BSTR* sIconLibrary, 
//											 [out,retval]long* nIconID);
	public String getIconDetailsForElementType(String sKeyname);

//	 Returns the information about a specific icon for the IDispatch.  The pDisp can be an IElement, IDiagram or IProxyDiagram..  Use to load the icon yourself - for instance if you need to put into an image list. 
//  HRESULT GetIconDetailsForDisp([in]IDispatch* pDisp, 
//								[out]BSTR* sIconLibrary, 
//									  [out,retval]long* nIconID);
	public String getIconDetailsForDisp(Object pDisp);

//	 Returns the information about a specific icon for the diagram kind (of type DiagramKind).  Use to load the icon yourself - for instance if you need to put into an image list. 
//  HRESULT GetIconDetailsDiagramKind([in]long nDiagramKind, 
//									[out]BSTR* sIconLibrary, 
//										  [out,retval]long* nIconID);
	public String getIconDetailsDiagrmaKind(int nDiagramKind);

}


