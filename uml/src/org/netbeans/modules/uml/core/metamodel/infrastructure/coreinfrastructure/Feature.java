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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;

public class Feature extends RedefinableElement implements IFeature 
{
	/**
	 * Specifies if the Feature is defined for the Classifier (true) or for the Instances of the Classifier (false). The default value is false.
	*/
	public boolean getIsStatic()
	{
		return getBooleanAttributeValue("isStatic",false);
	}

	/**
	 * Specifies if the Feature is defined for the Classifier (true) or for the Instances of the Classifier (false). The default value is false.
	*/
	public void setIsStatic( boolean newVal )
	{
		boolean isStatic = getIsStatic();
		
		//No need to set if values aren't different
	    if( isStatic != newVal )
	    {
		  EventDispatchRetriever ret = EventDispatchRetriever.instance();
		  IClassifierEventDispatcher disp = (IClassifierEventDispatcher) 
		  					ret.getDispatcher(EventDispatchNameKeeper.classifier());
		  boolean proceed = true; 
		  if( disp != null )
		  {
			 IEventPayload payload = disp.createPayload("PreStaticModified" );
			 proceed = disp.firePreStaticModified((IFeature) getAggregator(),newVal,payload);			 
		  }

		  if( proceed )
		  {
			 setBooleanAttributeValue( "isStatic", newVal );
			 if( disp != null )
			 {
				IEventPayload payload = disp.createPayload("StaticModified");
				disp.fireStaticModified((IFeature) getAggregator(),payload);
			 }
		  }
		  else
		  {
			 //Cancel Event (throw Exception)
		  }
	   }
		
	}

	/**
 	 *
     * Retrieves the Classifier that owns this feature.
     * 
     */
	public IClassifier getFeaturingClassifier()
	{
		IElement owner = getOwner();
		if (owner != null)
		{
			return owner instanceof IClassifier? (IClassifier)owner : null;
		}
		return null;
	}

	/**
 	 *
 	 * Sets the Classifier that owns this feature.
 	 *
 	 * @param newVal The owning Classifier
 	 */
	public void setFeaturingClassifier( IClassifier classi )
	{
		final IClassifier classifier = classi;
		new ElementConnector<IFeature>().setSingleElementAndConnect
						(
							(IFeature) getAggregator(), classifier, 
							"featuringClassifier",
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									obj.addFeature((IFeature) getAggregator());
								 }
							 },
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									obj.removeFeature((IFeature) getAggregator());
								 }
							 }										
						);				
	}

	/**
	 * Moves this Feature from the Featuring Classifier it currently is in
 	 * to the Classifier passed in.
 	 * 
 	 * @param destination. The destination classifier
 	 * 
	 */
	public void moveToClassifier( IClassifier destination )
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp = (IClassifierEventDispatcher) 
						ret.getDispatcher(EventDispatchNameKeeper.classifier());
		boolean proceed = true;
		IClassifier featClassifier =  getFeaturingClassifier();
		//An Actor is also a Feature, but this routine is really only designed
	   // for Attributes and Operations. If this feature doesn't have a featuring
	   // classifier, we won't do anything.

        IFeature feat = (IFeature) getAggregator();
        
	   if( featClassifier != null)
	   {
		  if( disp != null )
		  {
			 IEventPayload payload = disp.createPayload("FeaturePreMoved");
			 proceed = disp.fireFeaturePreMoved(destination, feat, payload);			 
		  }
		  if( proceed )
		  {
			 {
			    boolean status = EventBlocker.startBlocking();
                try
                {
    				// Remove this feature from the Featuring Classifier that it is currently in,
    				// then add to the destination classifier
    				featClassifier.removeFeature( feat );
    				destination.addFeature( feat );
                }
                finally
                {
                    EventBlocker.stopBlocking(status);
                }
			 }
			 if( disp != null )
			 {
				IEventPayload payload = disp.createPayload("FeatureMoved");
				disp.fireFeatureMoved(featClassifier, feat, payload);				
			 }
		  }
		  else
		  {
			 //Cancel Event (throw Exception)
		  }
	   }		
	}

    
    /**
     * Duplicates this Feature, then adds it to the passed in Classifier. 
     * The duplicated feature is passed back.
     * 
     *  @param destination. The classifier to add the duplicated feature to
     */
    public IFeature duplicateToClassifier( IClassifier destination)
    {
		IFeature dupFeat = null;
		
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
	    IClassifierEventDispatcher disp = (IClassifierEventDispatcher) 
					   ret.getDispatcher(EventDispatchNameKeeper.classifier());
	    boolean proceed = true;		

		IClassifier featClassifier =  getFeaturingClassifier();
		if( disp != null )
		{
			 IEventPayload payload = disp.createPayload("FeaturePreDuplicatedToClassifier");
			 proceed = disp.fireFeaturePreDuplicatedToClassifier(destination,(IFeature) getAggregator(),payload);			 
		}
		if( proceed )
		{			
			{
                boolean status = EventBlocker.startBlocking();
                try
                {
                    // Duplicate this feature then add to the destination classifier
                    IVersionableElement ver = this.duplicate();			   
                    dupFeat = (IFeature) ver;
                    destination.addFeature( dupFeat );
                }
                finally
                {
                    EventBlocker.stopBlocking(status);
                }
			}

			if( disp != null )
			{
			   IEventPayload payload = disp.createPayload("FeatureDuplicatedToClassifier");
			   proceed = disp.fireFeatureDuplicatedToClassifier(featClassifier,(IFeature) getAggregator(),destination,dupFeat,payload);		   	
			}
		 }
		 else
		 {
			//Cancel Event (throw Exception)
		 }
		 
		 return dupFeat;
    }
}
