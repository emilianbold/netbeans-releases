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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface ITypedElementEventsSink
{
	/**
	 * Fired whenever the Multiplicity object on a particular element is about to be modified.
	*/
	public void onPreMultiplicityModified( ITypedElement element, IMultiplicity proposedValue, IResultCell cell );

	/**
	 * Fired whenever the Multiplicity object on a particular element was just modified.
	*/
	public void onMultiplicityModified( ITypedElement element, IResultCell cell );

	/**
	 * Fired whenever the type on a particular element is about to be modified.
	*/
	public void onPreTypeModified( ITypedElement element, IClassifier proposedValue, IResultCell cell );

	/**
	 * Fired whenever the type flag on a particular element was just modified.
	 * @param element The element that changed
         * @param cell The event result.
         */
	public void onTypeModified( ITypedElement element, IResultCell cell );

	/**
	 * Fired when the lower property on the passed in range is about to be modified.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param proposedValue The new value
         * @param cell The event result.
	*/
	public void onPreLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell );

	/**
	 * Fired when the lower property on the passed in range was modified.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param cell The event result.
	*/
	public void onLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when the upper property on the passed in range is about to be modified.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param proposedValue the new value
         * @param cell The event result.
	*/
	public void onPreUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell );

	/**
	 * Fired when the upper property on the passed in range was modified.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param cell The event result.
	*/
	public void onUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when a new range is about to be added to the passed in multiplicity.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param cell The event result.
	*/
	public void onPreRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when a new range is added to the passed in multiplicity.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param cell The event result.
	*/
	public void onRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when an existing range is about to be removed from the passed in multiplicity.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that will be removed
         * @param cell The event result.
	*/
	public void onPreRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when an existing range is removed from the passed in multiplicity.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that was removed
         * @param cell The event result.
	*/
	public void onRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when the order property is about to be changed on the passed in mulitplicity.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param proposedValue the new value
         * @param cell The event result.
	*/
	public void onPreOrderModified( ITypedElement element, IMultiplicity mult, boolean proposedValue, IResultCell cell );

	/**
	 * Fired when the order property is changed on the passed in mulitplicity.
         * 
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param cell The event result.
	*/
	public void onOrderModified( ITypedElement element, IMultiplicity mult, IResultCell cell );
        
        /**
	 * Fired when the collection type property is changed on the passed in 
         * range.
	 * @param element The type that owned the multilicity element
         * @param mult The multiplicity 
         * @param range The multiplicity range that changed
         * @param cell The event result.
         */
	public void onCollectionTypeModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

}
