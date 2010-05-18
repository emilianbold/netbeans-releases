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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IAssociationEnd extends IStructuralFeature
{
	/**
	 * property Association
	*/
	public IAssociation getAssociation();

	/**
	 * property Association
	*/
	public void setAssociation( IAssociation value );

	/**
	 * method AddQualifier
	*/
	public void addQualifier( IAttribute qual );

	/**
	 * method RemoveQualifier
	*/
	public void removeQualifier( IAttribute qual );

	/**
	 * Creates an Qualifier. The new qualifier is not added to this AssociationEnd.
	*/
	public IAttribute createQualifier( String Type, String Name );

	/**
	 * Creates an Qualifier. The new qualifier is not added to this AssociationEnd.
	*/
	public IAttribute createQualifier2( IClassifier Type, String Name );

	/**
	 * Creates an Qualifier with a default name and type, dependent on the current language settings. The new attribute is not added to this Classifier.
	*/
	public IAttribute createQualifier3();

	/**
	 * property Qualifiers
	*/
	public ETList<IAttribute> getQualifiers();

	/**
	 * Designates the Classifier participating in the Association at the given end.
	*/
	public IClassifier getParticipant();

	/**
	 * Designates the Classifier participating in the Association at the given end.
	*/
	public void setParticipant( IClassifier value );

	/**
	 * Retrieves the other ends of the Association this end is a part of.
	*/
	public ETList<IAssociationEnd> getOtherEnd();

	/**
	 * Turns this end into a NavigableEnd.
	*/
	public INavigableEnd makeNavigable();

	/**
	 * Determines whether or not this end is navigable.
	*/
	public boolean getIsNavigable();

	/**
	 * Retrieves the first end found in the OtherEnd collection. This is usually sufficient in every association other than a ternary.
	*/
	public IAssociationEnd getOtherEnd2();

	/**
	 * Determines whether or not the participant encapsulates the same data as the passed in element
	*/
	public boolean isSameParticipant( IVersionableElement element );

}
