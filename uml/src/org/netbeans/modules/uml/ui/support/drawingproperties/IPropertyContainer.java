/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


