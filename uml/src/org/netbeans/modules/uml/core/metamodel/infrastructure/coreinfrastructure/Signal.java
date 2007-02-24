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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Signal extends Classifier implements ISignal 
{
	public Signal()
	{
		super();
	}
	/**
	 * method AddContext
	*/
	public void addContext( IBehavioralFeature feature )
	{
		final IBehavioralFeature behavFeature = feature;
		new ElementConnector<ISignal>().addChildAndConnect(this, true, 
							"context", 
							"context", behavFeature,
							 new IBackPointer<ISignal>() 
							 {
								 public void execute(ISignal obj) 
								 {
									behavFeature.addHandledSignal(obj);
								 }
							 }										
							);		
	}

	/**
	 * method RemoveContext
	*/
	public void removeContext( IBehavioralFeature feature )
	{
		final IBehavioralFeature feat = feature;
		new ElementConnector<ISignal>().removeByID
							   (
								this, feat,"context",
								 new IBackPointer<ISignal>() 
								 {
									public void execute(ISignal obj) 
									{
									   feat.removeHandledSignal(obj);
									}
								 }										
								);		
	}

	/**
	 * property Contexts
	*/
	public ETList<IBehavioralFeature> getContexts()
	{
		ElementCollector<IBehavioralFeature> collector = new ElementCollector<IBehavioralFeature>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"context", IBehavioralFeature.class);
	}

	/**
	 * method AddHandler
	*/
	public void addHandler( IBehavioralFeature feature )
	{
		final IBehavioralFeature behavFeature = feature;
		new ElementConnector<ISignal>().addChildAndConnect(this, true, 
							"handler", 
							"handler", behavFeature,
							 new IBackPointer<ISignal>() 
							 {
								 public void execute(ISignal obj) 
								 {
									behavFeature.addRaisedSignal(obj);
								 }
							 }										
							);
	}

	/**
	 * method RemoveHandler
	*/
	public void removeHandler( IBehavioralFeature feature )
	{
		final IBehavioralFeature feat = feature;
		new ElementConnector<ISignal>().removeByID
							   (
								 this,feat,"handler",
								 new IBackPointer<ISignal>() 
								 {
									public void execute(ISignal obj) 
									{
									   feat.removeRaisedSignal(obj);
									}
								 }										
								);
	}

	/**
	 * property Handlers
	*/
	public ETList<IBehavioralFeature> getHandlers()
	{
		ElementCollector<IBehavioralFeature> collector = new ElementCollector<IBehavioralFeature>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"handler", IBehavioralFeature.class);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Signal",doc,parent);
	}	
}


