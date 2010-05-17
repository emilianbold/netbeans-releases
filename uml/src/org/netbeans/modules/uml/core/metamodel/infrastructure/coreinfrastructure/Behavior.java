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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Behavior extends Classifier implements IBehavior 
{
	public IClassifier getContext()
	{
		ElementCollector<IClassifier> coll = new ElementCollector<IClassifier>();		
		return coll.retrieveSingleElementWithAttrID(this,"context", IClassifier.class);
	}

	/**
	 * property Context
	*/
	public void setContext( IClassifier newValue )
	{
		final IClassifier classifier = newValue;
		new ElementConnector<IBehavior>().setSingleElementAndConnect
						(
							this, classifier, 
							"context",
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									obj.addBehavior(Behavior.this);
								 }
							 },
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									obj.removeBehavior(Behavior.this);
								 }
							 }										
						);		
	}

	/**
	 * property Specification
	*/
	public IBehavioralFeature getSpecification()
	{
		ElementCollector<IBehavioralFeature> coll = new ElementCollector<IBehavioralFeature>();
		return coll.retrieveSingleElementWithAttrID(this,"specification", IBehavioralFeature.class);
	}

	/**
	 * property Specification
	*/
	public void setSpecification( IBehavioralFeature feature )
	{
		final IBehavioralFeature behaveFeature = feature;
		new ElementConnector<IBehavior>().setSingleElementAndConnect
						(
							this, behaveFeature, 
							"specification",
							 new IBackPointer<IBehavioralFeature>() 
							 {
								 public void execute(IBehavioralFeature obj) 
								 {
									obj.addMethod(Behavior.this);
								 }
							 },
							 new IBackPointer<IBehavioralFeature>() 
							 {
								 public void execute(IBehavioralFeature obj) 
								 {
									obj.removeMethod(Behavior.this);
								 }
							 }										
						);		
	}

	/**
	 * property RepresentedFeature
	*/
	public IBehavioralFeature getRepresentedFeature()
	{
		ElementCollector<IBehavioralFeature> coll = new ElementCollector<IBehavioralFeature>();
		return coll.retrieveSingleElementWithAttrID(this,"representedFeature", IBehavioralFeature.class);		
	}

	/**
	 * property RepresentedFeature
	 */
	public void setRepresentedFeature( IBehavioralFeature newValue )
	{
		final IBehavioralFeature behavFeature = newValue;
		new ElementConnector<IBehavior>().addChildAndConnect(
											this, true, "representedFeature", 
											"representedFeature", behavFeature,
											 new IBackPointer<IBehavior>() 
											 {
												 public void execute(IBehavior obj) 
												 {
													behavFeature.setRepresentation(obj);
												 }
											 }										
											);
	}

	/**
	 * method AddParameter
	*/
	public void addParameter( IParameter parm )
	{
		addChild("UML:Behavior.parameter","UML:Behavior.parameter",parm);
	}

	/**
	 * method RemoveParameter
	*/
	public void removeParameter( IParameter parm )
	{
		UMLXMLManip.removeChild(m_Node,parm);		
	}

	/**
	 * property Parameters
	*/
	public ETList<IParameter> getParameters()
	{
		ElementCollector<IParameter> coll = new ElementCollector<IParameter>();
		return coll.retrieveElementCollection(m_Node,"UML:Behavior.parameter/*", IParameter.class);
	}

	/**
	 * Tells whether whether the behavior can be invoked while its still executing from a previous invocation.
	 */
	public boolean getIsReentrant()
	{
		return getBooleanAttributeValue( "isReentrant",false );
	}

	/**
	 * Tells whether whether the behavior can be invoked while its still executing from a previous invocation.
	*/
	public void setIsReentrant( boolean value )
	{
		setBooleanAttributeValue( "isReentrant",value );
	}
	
	
}


