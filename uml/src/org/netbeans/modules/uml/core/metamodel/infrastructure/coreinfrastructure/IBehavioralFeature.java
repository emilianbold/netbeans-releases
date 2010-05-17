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

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IBehavioralFeature extends IFeature, INamespace
{
	/**
	 * property AddOwnedElement
	*/
	public void addParameter( IParameter parm );

	/**
	 * property RemoveParameter
	*/
	public void removeParameter( IParameter parm );

	/**
	 * Inserts a new parameter into this feature's collection of parameters before the existingParm.
	*/
	public void insertParameter( IParameter existingParm, IParameter newParm );

	/**
	 * The collection of Parameters that makes up this features parameter list.
	*/
	public ETList<IParameter> getParameters();

	/**
	 * The collection of Parameters that makes up this features parameter list.
	*/
	public void setParameters( ETList<IParameter> value );

	/**
	 * Removes all the parameters from this feature.
	*/
	public void removeAllParameters();

	/**
	 * property AddRaisedSignal
	*/
	public void addRaisedSignal( ISignal sig );

	/**
	 * property RemoveRaisedSignal
	*/
	public void removeRaisedSignal( ISignal sig );

	/**
	 * property RaisedSignals
	*/
	public ETList<ISignal> getRaisedSignals();

	/**
	 * property AddHandledSignal
	*/
	public void addHandledSignal( ISignal sig );

	/**
	 * property RemoveHandledSignal
	*/
	public void removeHandledSignal( ISignal sig );

	/**
	 * property HandledSignals
	*/
	public ETList<ISignal> getHandledSignals();

	/**
	 * property Concurrency
	*/
	public int getConcurrency();

	/**
	 * property Concurrency
	*/
	public void setConcurrency( /* CallConcurrencyKind */ int value );

	/**
	 * property IsAbstract
	*/
	public boolean getIsAbstract();

	/**
	 * property IsAbstract
	*/
	public void setIsAbstract( boolean value );

	/**
	 * Creates a parameter given the type and name. Does not add the parameter to this feature.
	*/
	public IParameter createParameter( String Type, String Name );

	/**
	 * Creates a parameter given the type and name. Does not add the parameter to this feature.
	*/
	public IParameter createParameter2( IClassifier Type, String Name );

	/**
	 * Sets / Gets the return type of this feature.
	*/
	public IParameter getReturnType();

	/**
	 * Sets / Gets the return type of this feature.
	*/
	public void setReturnType( IParameter value );

	/**
	 * Sets / Gets the return type of this feature.
	*/
	public void setReturnType2( String value );

	/**
	 * property AddMethod
	*/
	public void addMethod( IBehavior Behavior );

	/**
	 * property RemoveMethod
	*/
	public void removeMethod( IBehavior Behavior );

	/**
	 * property Methods
	*/
	public ETList<IBehavior> getMethods();

	/**
	 * property Representation
	*/
	public IBehavior getRepresentation();

	/**
	 * property Representation
	*/
	public void setRepresentation( IBehavior value );

	/**
	 * All parameters other than those with a direction kind of PDK_RESULT.
	*/
	public ETList<IParameter> getFormalParameters();

	/**
	 * Sets / Gets the return type of this feature.
	*/
	public String getReturnType2();

	/**
	 * Determines whether or not the signature of the passed in feature is the same as this one.
	*/
	public boolean isSignatureSame( IBehavioralFeature pFeature );

	/**
	 * Determines whether or not the signature of the passed in feature is the same as this one. This does not include the result parameters, i.e., return types.
	*/
	public boolean isFormalSignatureSame( IBehavioralFeature pFeature );

	/**
	 * Creates a parameter with default name and type.
	*/
	public IParameter createParameter3();

	/**
	 * All parameters other than those with a direction kind of PDK_RESULT.
	*/
	public void setFormalParameters( ETList<IParameter> value );
        
        
        /**
         * Simply removes all the existing parameters and sets new parameters
         * from a given parameter list.
         * 
         * @param parameterList 
         */
        public void setFormalParameters2( ETList<IParameter> parameterList );

	/**
	 * Specific to the Java language.
	*/
	public boolean getIsNative();

	/**
	 * Specific to the Java language.
	*/
	public void setIsNative( boolean value );

	/**
	 * Specific to the Java language.
	*/
	public boolean getIsStrictFP();

	/**
	 * Specific to the Java language.
	*/
	public void setIsStrictFP( boolean value );

	/**
	 * Creates a return type parameter
	*/
	public IParameter createReturnType();

}
