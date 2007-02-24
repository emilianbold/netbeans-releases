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
 * Created on Jun 11, 2003
 *
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

/**
 * @author sumitabhk
 *
 *
 */
public interface IPropertyContainer
{
//	 The provider that this container contains drawing properties for.
//	 HRESULT PropertyProvider( [out,retval] IDrawingPropertyProvider* *pProvider );


//	 The provider that this container contains drawing properties for.
//	 HRESULT PropertyProvider( [in] IDrawingPropertyProvider* pProvider );


//	 Returns the drawing properties for the container.
//	HRESULT DrawingProperties([out,retval]IDrawingProperties* *pProperties);


//	 Sets the drawing properties for the container.
//	HRESULT DrawingProperties([in]IDrawingProperties* pProperties);

      
//	 Returns just the font drawing properties for the container.
//	HRESULT FontProperties([out,retval]IDrawingProperties* *pProperties);


//	 Sets the font drawing properties for the container.
//	HRESULT FontProperties([in]IDrawingProperties* pProperties);

      
//	 Returns just the color drawing properties for the container.
//	HRESULT ColorProperties([out,retval]IDrawingProperties* *pProperties);


//	 Sets the color drawing properties for the container.
//	HRESULT ColorProperties([in]IDrawingProperties* pProperties);

      
//	 Returns the child drawing property containers for the container.
//	HRESULT PropertyContainers([out,retval]IPropertyContainers* *pContainers);


//	 Sets the child drawing property containers for the container.
//	HRESULT PropertyContainers([in]IPropertyContainers* pContainers);


//	 Updates any changed drawing properties in this and child property containers.
	public void save();


//	   The display name of the collection of properties ie. 'Attributes'. 
	public String getDisplayName();


//	   The display name of the collection of properties ie. 'Attributes'. 
	public void setDisplayName(String newVal);


//	 Updates or adds drawing property. The property isn't saved until Save() is invoked.
//	HRESULT SetDrawingProperty( [in] IDrawingProperty *newVal);


//	 Retrieves a drawing property by its kind.
//	HRESULT GetDrawingProperty( [in] ResourceIDKind nKind, [out,retval] IDrawingProperty* *pVal);


//	 Propogates a property down through all child providers by inheritance.
//	HRESULT SetPropertiesToInheritFromProperty( [in] IDrawingProperty* pInheritedProperty );

}


