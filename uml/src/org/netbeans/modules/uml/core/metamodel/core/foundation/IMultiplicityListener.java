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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

public interface IMultiplicityListener
{
	/**
	 * Fired when the lower property on the passed in range is about to be modified.
	*/
	public boolean onPreLowerModified( IMultiplicity mult, IMultiplicityRange range, String proposedValue );

	/**
	 * Fired when the lower property on the passed in range was modified.
	*/
	public void onLowerModified( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when the upper property on the passed in range is about to be modified.
	*/
	public boolean onPreUpperModified( IMultiplicity mult, IMultiplicityRange range, String proposedValue );

	/**
	 * Fired when the upper property on the passed in range was modified.
	*/
	public void onUpperModified( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when a new range is about to be added to the passed in multiplicity.
	*/
	public boolean onPreRangeAdded( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when a new range is added to the passed in multiplicity.
	*/
	public void onRangeAdded( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when an existing range is about to be removed from the passed in multiplicity.
	*/
	public boolean onPreRangeRemoved( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when an existing range is removed from the passed in multiplicity.
	*/
	public void onRangeRemoved( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when the order property is about to be changed on the passed in mulitplicity.
	*/
	public boolean onPreOrderModified( IMultiplicity mult, boolean proposedValue );

	/**
	 * Fired when the order property is changed on the passed in mulitplicity.
	*/
	public void onOrderModified( IMultiplicity mult );
        
        /**
         * Fired when the collection type property is changed on the passed in 
         * range.
         */
        public void onCollectionTypeModified(IMultiplicity mult, IMultiplicityRange range);

}
