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

import org.dom4j.Node;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.support.umlsupport.PreventReEntrance;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * @author sumitabhk
 */
public class BehavioralFeature extends Feature 
      implements IBehavioralFeature, INamespace  
{
	private INamespace m_NamespaceAggregate = new Namespace();
	private int PDK_RESULT = 3;
	private static int entry = 0;
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_NamespaceAggregate.setNode(n);
		m_NamespaceAggregate.setAggregator(this);
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#addParameter(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter)
	 */
	public void addParameter(IParameter parm)
	{
		EventContextManager mgr = new EventContextManager();
		mgr.revokeEventContext(parm, null);
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("PreParameterAdded");	   
		   proceed = disp.firePreParameterAdded(this,parm,payload);
		}
		if (proceed)
		{
			if (parm instanceof INamedElement)
			{
				addOwnedElement((INamedElement)parm);				
			}
			if( disp != null )
			{
			   payload = disp.createPayload("ParameterAdded");	   
			   disp.fireParameterAdded(this,parm,payload);
			}
		}
		else
		{
			//throw exception		
		}				
	}

	/**
	 *
	 * Removes the passed-in Parameter from this feature. Results in the
	 * firing of the PreParameterRemoved and ParameterRemoved events.
	 *
	 * @param parm[in] The parameter to remove.
	 */
	public void removeParameter(IParameter parm)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("PreParameterRemoved");	   
		   proceed = disp.firePreParameterRemoved(this,parm,payload);
		}
		if (proceed)
		{
			if (parm instanceof INamedElement)
			{
				removeOwnedElement((INamedElement)parm);				
			}
			if( disp != null )
			{
			   payload = disp.createPayload("ParameterRemoved");	   
			   disp.fireParameterRemoved(this,parm,payload);
			}
		}
		else
		{
			//throw exception		
		}		
	}

	/**
	 *
	 * Inserts newParm before existingParm in this feature's list of parameters. If existingParm is 0,
	 * then newParm is appeneded to the end of the Parameters list.
	 *
	 * @param existingParm[in] A parameter that this feature currenly owns. Can be 0. See above.
	 * @param newParm[in]      The new parameter to insert.
	 */
	public void insertParameter(IParameter existingParm, IParameter newParm)
	{
		EventContextManager manager = new EventContextManager();
		manager.revokeEventContext(newParm, null);
		
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("PreParameterAdded");	   
		   proceed = disp.firePreParameterAdded(this,newParm,payload);
		}
		if (proceed)
		{
			super.insertNode(null,existingParm,newParm);
			if( disp != null )
			{
			   payload = disp.createPayload("ParameterAdded");	   
			   disp.fireParameterAdded(this,newParm,payload);
			}
		}
		else
		{
			//throw exception		
		}	
	}

	/**
	 *
	 * Adds the collection of parameters to this feature.  It will first remove
	 * all existing parameters.
	 *
	 * @param pVal [in] The collection of paramerters to add
	 */
	public ETList<IParameter> getParameters()
	{
		ElementCollector< IParameter > col = new ElementCollector< IParameter >();
		return col.retrieveElementCollection(getNode(), "UML:Element.ownedElement/UML:Parameter", IParameter.class);
	}

	/**
	 *
	 * Adds the collection of parameters to this feature. It will first remove all 
	 * existing parameters.
	 *
	 * @param params[in] The collection of parameters to add.
	 */
	public void setParameters(ETList<IParameter> params)
	{
		ETList<IParameter> pars = getParameters();
		Log.out("Changing parameters from " + pars + " to " + params);
		mergeParameters(params, pars);
	}
	
	private void mergeParameters(ETList<IParameter> newP,
								ETList<IParameter> oldP)
	{
		// If it's not a simple parameters added or parameters removed case,
		// do it the easy way
		if (!paramsAdded(newP, oldP) && !paramsRemoved(newP, oldP)
				&& !paramsChanged(newP, oldP))
		{
			removeAllParameters();
			if (newP != null)
			{
				int noOfParams = newP.size();
				for (int i = 0; i < noOfParams; i++)
				{
					IParameter parm = newP.get(i);
					if (parm != null)
					{
						addParameter(parm);
					}
				}
			}
		}				
	}

	private boolean paramsChanged(ETList<IParameter> nl, ETList<IParameter> old)
        {
            if (nl.size() == old.size())
            {
                try
                {
                    for (int i = 0, count = old.size(); i < count; ++i)
                    {
                        IParameter oldp = old.get(i),
                              newp = nl.get(i);
                        if (oldp.getTypeName().equals(newp.getTypeName())
                              && (oldp.getName().equals(newp.getName())
                              || (
                              (oldp.getDirection() == BaseElement.PDK_OUT
                              || oldp.getDirection() == BaseElement.PDK_RESULT)
                              && oldp.getDirection() == newp.getDirection())))
                            continue;
                        oldp.setName(newp.getName());
                        oldp.setType(newp.getType());
                        oldp.setDirection(newp.getDirection());
                    }
                    return true;
                }
                catch (Exception e)
                {
                    Log.stackTrace(e);
                }
            }
            return false;
        }
    
	private boolean paramsAdded(ETList<IParameter> nl, ETList<IParameter> old)
        {
            if (nl.size() > old.size())
            {
                try
                {
                    for (int i = 0, count = old.size(); i < count; ++i)
                    {
                        IParameter oldp = old.get(i),
                              newp = nl.get(i);
                        
                        if (!oldp.getTypeName().equals(newp.getTypeName())
                              || (!oldp.getName().equals(newp.getName())
                              && (
                              (oldp.getDirection() != BaseElement.PDK_OUT
                              && oldp.getDirection() != BaseElement.PDK_RESULT)
                              || oldp.getDirection() != newp.getDirection())))
                            return false;
                        
                        
                    }
                    for (int i = old.size(), count = nl.size(); i < count; ++i)
                        addParameter(nl.get(i));
                    return true;
                }
                catch (Exception e)
                {
                    Log.stackTrace(e);
                }
            }
            return false;
        }
	
	private boolean paramsRemoved(ETList<IParameter> nl, ETList<IParameter> old)
        {
            if (old.size() > nl.size())
            {
                try
                {
                    for (int i = 0, count = nl.size(); i < count; ++i)
                    {
                        IParameter oldp = old.get(i),
                              newp = nl.get(i);
                        if (!oldp.getTypeName().equals(newp.getTypeName())
                              || (!oldp.getName().equals(newp.getName())
                              && (
                              (oldp.getDirection() != BaseElement.PDK_OUT
                              && oldp.getDirection() != BaseElement.PDK_RESULT)
                              || oldp.getDirection() != newp.getDirection())))
                            return false;
                    }
                    for (int i = nl.size(), count = old.size(); i < count; ++i)
                        removeParameter(old.get(i));
                    return true;
                }
                catch (Exception e)
                {
                    Log.stackTrace(e);
                }
            }
            return false;
        }

	/**
	 *
	 * Removes all the parameters this feature currently owns.
	 */
	public void removeAllParameters()
	{
		ETList<IParameter> parameters = getParameters();
		if (parameters != null)
		{
			int numParams = parameters.size();
			for (int i=0;i<numParams;i++)
			{
				IParameter param = parameters.get(i);
				if (param != null)
				{
					removeParameter(param);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#addRaisedSignal(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal)
	 */
	public void addRaisedSignal(ISignal sig)
        {
            final ISignal signal = sig;
            new ElementConnector<IBehavioralFeature>().addChildAndConnect(
                  this, true, "raisedSignal",
                  "raisedSignal", signal,
                  new IBackPointer<IBehavioralFeature>()
            {
                public void execute(IBehavioralFeature obj)
                {
                    signal.addContext(obj);
                }
            }
            );
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#removeRaisedSignal(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal)
	 */
	public void removeRaisedSignal(ISignal sig)
        {
            final ISignal signal = sig;
            new ElementConnector<IBehavioralFeature>().removeByID
                  (
                  this,signal,"raisedSignal",
                  new IBackPointer<IBehavioralFeature>()
            {
                public void execute(IBehavioralFeature obj)
                {
                    signal.removeContext(obj);
                }
            }
            );
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#getRaisedSignals()
	 */
	public ETList<ISignal> getRaisedSignals()
        {
            ElementCollector<ISignal> collector = new ElementCollector<ISignal>();
            return collector.retrieveElementCollectionWithAttrIDs(
                  this,"raisedSignal", ISignal.class);
        }

	/**
	 *
	 * Adds the ISignal to the list of handled signals. Results in the firing
	 * of the PreHandledSignalAdded and HandledSignalAdded events.
	 *
	 * @param sig[in] The signal to add.
	 */
	public void addHandledSignal(ISignal sig)
        {
            EventDispatchRetriever ret = EventDispatchRetriever.instance();
            IClassifierEventDispatcher disp =
                  (IClassifierEventDispatcher) ret.getDispatcher(
                  EventDispatchNameKeeper.classifier());
            
            boolean proceed = true;
            IEventPayload payload = null;
            
            if( disp != null )
            {
                payload = disp.createPayload("PreHandledSignalAdded");
                proceed = disp.firePreHandledSignalAdded(this,sig,payload);
            }
            if (proceed)
            {
                // NOW create the semaphore, because the train is already on the
                // track, and the AddChildAndConnect routine is the routine that is going
                // to cause re-entrancy. Notice we have to wrap this in a scope to make
                // sure the semaphore destructs before the firing of the event.
                PreventReEntrance reEnt = new PreventReEntrance();
                entry = reEnt.startBlocking(entry);
                
                try
                {
                    if (!reEnt.isBlocking())
                    {
                        final ISignal signal = sig;
                        new ElementConnector<IBehavioralFeature>().addChildAndConnect(
                              this, true, "handledSignal",
                              "handledSignal", signal,
                              new IBackPointer<IBehavioralFeature>()
                        {
                            public void execute(IBehavioralFeature obj)
                            {
                                signal.addHandler(obj);
                            }
                        }
                        );
                    }
                }
                finally
                {
                    entry = reEnt.releaseBlock();
                }
                
                if( disp != null )
                {
                    payload = disp.createPayload("HandledSignalAdded");
                    disp.fireHandledSignalAdded(this,payload);
                }
            }
            else
            {
                //throw exception
            }
        }


	/**
	 *
	 * Removes the passed-in signal from the list of handled signals. Results
	 * in the firing of the PreHandledSignalRemoved and HandledSignalRemoved
	 * events.
	 *
	 * @param sig[in] The signal to remove
	 */
	public void removeHandledSignal(ISignal sig)
	{
		PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try {
    		if (!reEnt.isBlocking())
    		{
    			EventDispatchRetriever ret = EventDispatchRetriever.instance();
    			IClassifierEventDispatcher disp =
    						(IClassifierEventDispatcher) ret.getDispatcher(
    									EventDispatchNameKeeper.classifier());
    			
    			boolean proceed = true;
    			int entry = 0;		
    			IEventPayload payload = null;
    			
    			if( disp != null )
    			{
    			   payload = disp.createPayload("PreHandledSignalRemoved");	   
    			   proceed = disp.firePreHandledSignalRemoved(this,sig,payload);
    			}
    			if (proceed)
    			{	
    				final ISignal signal = sig;
    				new ElementConnector<IBehavioralFeature>().removeByID
    									   (
    										 this,signal,"handledSignal",
    										 new IBackPointer<IBehavioralFeature>() 
    										 {
    											public void execute(IBehavioralFeature obj) 
    											{
    											   signal.removeContext(obj);
    											}
    										 }										
    										);
    				
    				if( disp != null )
    				{
    				   payload = disp.createPayload("HandledSignalRemoved");	   
    				   disp.fireHandledSignalRemoved(this,payload);
    				}
    			}
    			else
    			{
    				//throw exception		
    			}
    		}
        }
        finally
        {
            reEnt.releaseBlock();
        }	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#getHandledSignals()
	 */
	public ETList<ISignal> getHandledSignals()
        {
            ElementCollector<ISignal> collector = new ElementCollector<ISignal>();
            return collector.retrieveElementCollectionWithAttrIDs(
                  this,"handledSignal", ISignal.class);
        }

	/**
	 *
	 * Gets the concurrency flag.  Results in the firing of the ConcurrencyPreModified
	 * and ConcurrencyModified events.
	 */
	public int getConcurrency()
	{
		return super.getCallConcurrencyKindValue( "concurrency" );
	}

	/**
	 *
	 * Sets the concurrenty flag. Results in the firing of the ConcurrencyPreModified
	 * and ConcurrencyModifed events.
	 *
	 * @param newVal[in] The new value.
	 */
	public void setConcurrency(int newValue)
	{
		int concur = getConcurrency();
		// If the values aren't different, don't set
		if (newValue != concur)		
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp =
						(IClassifierEventDispatcher) ret.getDispatcher(
									EventDispatchNameKeeper.classifier());
		
			boolean proceed = true;
			IEventPayload payload = null;
			if( disp != null )
			{
			   payload = disp.createPayload("ConcurrencyPreModified");	   
			   proceed = disp.fireConcurrencyPreModified(this,newValue,payload);
			}
			if (proceed)
			{
				super.setCallConcurrencyKindValue( "concurrency", newValue);
				if( disp != null )
				{
				   payload = disp.createPayload("ConcurrencyModified");	   
				   disp.fireConcurrencyModified(this,payload);
				}
			}
			else
			{
				//throw exception		
			}		
		}
	}

	/**
	 *
	 * Gets the feature to abstract.
	 *
	 * @param newVal [in]	True to make this feature abstract, resulting in the
							featuring classifier also becoming abstract, or False
							to make this feature NOT abstract.
	 */
	public boolean getIsAbstract()
	{
		return super.getBooleanAttributeValue("isAbstract",false);
	}

	/**
	 *
	 * Sets this feature to abstract.
	 *
	 * @param newValue[in] True to make this feature abstract, resulting
	 *                   in the featuring classifier also becoming abstract, or
	 *                   False to make this feature NOT abstract
	 */
	public void setIsAbstract(boolean newValue)
	{
		boolean isAbstract = getIsAbstract();
		// No need to set if the value coming in is not different than the one
		// currently set		
		if (newValue != isAbstract)
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp =
						(IClassifierEventDispatcher) ret.getDispatcher(
									EventDispatchNameKeeper.classifier());
		
			boolean proceed = true;
			IEventPayload payload = null;
			if( disp != null )
			{
			   payload = disp.createPayload("PreAbstractModified");	   
			   proceed = disp.firePreAbstractModified(this,newValue,payload);
			}
			if (proceed)
			{
				super.setBooleanAttributeValue("isAbstract",newValue);
				if( disp != null )
				{
				   payload = disp.createPayload("AbstractModified");	   
				   disp.fireAbstractModified(this,payload);
				}
				
				if (newValue)
				{
					// Now be sure to set the featuring classifier to Abstract, but 
					// only if this feature is NOT a transition element. In reality,
					// we should do this regardless, but we need to establish a more
					// robust filtering mechanism for events. Currently, when a new 
					// operation is being created, a VersionableElementFilter is getting
					// pushed onto an EventContext. This filter, however, is specific
					// to one element. So in this case, this feature is being filtered
					// ( if it is a transition ), but the owning classifier isn't. This is
					// a problem for round trip as the current logic is copying operations from
					// an interface down to a concrete class. This used to work 'cause
					// get_FeatureClassifier() never returned anything when this feature
					// was a transition element. That has been corrected, thus causing a problem
					// when making a concrete class implement an interface in Java.
					
					IBehavioralFeature curObj = this;
					if (! (curObj instanceof ITransitionElement))
					{
						IClassifier classifier = super.getFeaturingClassifier();
						if (classifier != null)
						{
							classifier.setIsAbstract(newValue);
						}
					}					
				}
			}
			else
			{
				//throw exception		
			}		
		}
	}

	/**
	 *
	 * Creates a new parameter with the passed-in information.
	 *
	 * @param type[in] The name of the type to use for the new Parameter
	 * @param name[in] The parameter name
	 */
	public IParameter createParameter(String newType, String newName)
	{
		String type = newType;
		String name = newName;
		if (type == null || type.length() == 0)
		{
			type = "int";
		}
		if (name == null || name.length() == 0)
		{
			name = retrieveDefaultName();
		}
		INamedElement nElement = resolveSingleTypeFromString(type);
		IParameter param = null;
		if (nElement != null && nElement instanceof IClassifier)
		{
			IClassifier classifier = (IClassifier)nElement;
			if (classifier != null)
			{
				param = createParameter2(classifier,name);
			}
		}
		return param;
	}

	/**
	 *
	 * Creates a new parameter with the passed-in information.
	 *
	 * @param type[in] The type to use for the new Parameter
	 * @param name[in] The parameter name
	 */
	public IParameter createParameter2(IClassifier type, String name)
	{
		if(type == null) return null;
		FactoryRetriever retriever = FactoryRetriever.instance();
		IParameter parameter = null;
		if (retriever != null)
		{
			Object obj = retriever.createType("Parameter",this);
			if (obj instanceof IParameter)	
			{
				parameter = (IParameter)obj;
				if (parameter != null)
				{
					EventContextManager manager = new EventContextManager();
					manager.establishVersionableElementContext(this,parameter,null);
					parameter.setType(type);
					if (parameter instanceof INamedElement)
					{
						INamedElement namedElement = (INamedElement)parameter;
						if (namedElement != null)
						{
							namedElement.setName(name);
						}
					}
					// Let's see if this type supports the ITransitionElement interface.
					// If it does, we can put ourselves on that interface to help resolve
					// types
					if (parameter instanceof ITransitionElement)
					{
						ITransitionElement trans = (ITransitionElement)parameter;
						trans.setFutureOwner(this);
					}
				}
			}
		}
		return parameter;
	}

        
        private IParameter cloneFormalParameter(IParameter cloningParam)
	{
            IParameter newParam = null;
            if (cloningParam != null )
            {
                newParam = createParameter(cloningParam.getTypeName(), cloningParam.getName());
                if (newParam != null)
                {
                    newParam.setXMIID(cloningParam.getXMIID()); 
                    newParam.setDirection(cloningParam.getDirection());
                    newParam.setParameterKind(cloningParam.getParameterKind());
                    newParam.setMultiplicity(cloningParam.getMultiplicity());
                    newParam.setOwner(cloningParam.getOwner());
                }
            }
            return newParam;
        }
        
	/**
	 *
	 * Retrieves the Parameter that is this feature's return type.
	 */
	public IParameter getReturnType()
	{
		IParameter retType = null;
		ETList<IParameter> params = getParameters();
        int numParams;
		if (params != null && (numParams = params.size()) > 0)
		{
			for (int i=0; i<numParams; i++)
			{
				IParameter param = params.get(i);
				if (param != null)
				{
					int kind = param.getDirection();
					if (kind == PDK_RESULT)
					{
						retType = param;
						break;
					}
				}
			}
		}
		return retType;
	}

	/**
	 *
	 * Adds the parameter to this behaviors collection of parameters,
	 * making sure the kind of that parameter is set to PDK_RETURN.
	 *
	 * @param retType[in] The parameter that will be this feature's 
	 *							 return type.
	 */
	public void setReturnType(IParameter value)
	{
		if (value != null)
		{
			// Make sure we don't already have a return type. If we do,
			// remove it
			IParameter param = getReturnType();
			if (param != null)
			{
				removeParameter(param);		
			}
			//Make sure the kind is PDK_RESULT
			value.setDirection(PDK_RESULT);
			addParameter(value);
		}
	}

	/**
	 *
	 * Adds a new parameter to this feature whose type is specified
	 * by newTypeName and whose kind is PDK_RETURN.
	 *
	 * @param newTypeName [in] The name of the type to be the return type
	 */
	public void setReturnType2(String newTypeName)
	{
		IParameter param = getReturnType();
		if (param != null)
		{
			String currentTypeName = param.getTypeName();
			// Only set the type if the type is actually changing
			if (currentTypeName != null && !currentTypeName.equals(newTypeName))
			{
				// Resolve the type
				String typeName = newTypeName;
				if (typeName.length() == 0)
				{
					// Default is hard coded for now
					typeName = "void";
				}
				INamedElement namedElement = resolveSingleTypeFromString(typeName);
				if (namedElement != null && namedElement instanceof IClassifier)				
				{
					IClassifier classifier = (IClassifier)namedElement;
					if (classifier != null)
					{
						param.setType(classifier);
					}
				}
			}
		}
		else
		{
			IParameter parameter = createParameter(newTypeName,"");
			if (parameter != null)
			{
				parameter.setDirection(PDK_RESULT);
				addParameter(parameter);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#addMethod(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
	 */
	public void addMethod(IBehavior behavior)
        {
            final IBehavior behav = behavior;
            new ElementConnector<IBehavioralFeature>().addChildAndConnect (
                  this, true, "method", "method", behav,
                  new IBackPointer<IBehavioralFeature>()
            {
                public void execute(IBehavioralFeature obj)
                {
                    behav.setSpecification(obj);
                }
            }
            );
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#removeMethod(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
	 */
	public void removeMethod(IBehavior behavior)
	{
		final IBehavior behav = behavior;
		new ElementConnector<IBehavioralFeature>().removeByID
							   (
								this,  behav, "method",
								 new IBackPointer<IBehavioralFeature>() 
								 {
									public void execute(IBehavioralFeature obj) 
									{
									   behav.setSpecification(obj);
									}
								 }										
								);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#getMethods()
	 */
	public ETList<IBehavior> getMethods()
        {
            ElementCollector<IBehavior> collector = new ElementCollector<IBehavior>();
            return collector.retrieveElementCollectionWithAttrIDs(
                  this,"method", IBehavior.class);
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#getRepresentation()
	 */
	public IBehavior getRepresentation()
        {
            ElementCollector<IBehavior> collector =
                  new ElementCollector<IBehavior>();
            return collector.retrieveSingleElementWithAttrID(this,"representation", IBehavior.class);
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#setRepresentation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
	 */
	public void setRepresentation(IBehavior behavior)
        {
            final IBehavior behav = behavior;
            new ElementConnector<IBehavioralFeature>().addChildAndConnect(
                  this, true, "representation",
                  "representation", behav,
                  new IBackPointer<IBehavioralFeature>()
            {
                public void execute(IBehavioralFeature obj)
                {
                    behav.setRepresentedFeature(obj);
                }
            }
            );
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature#getFormalParameters()
	 */
	public ETList<IParameter> getFormalParameters()
	{
		ETList<IParameter> parameters = getParameters();
		ETList<IParameter> resultParams = new ETArrayList<IParameter>();
		if (parameters != null)
		{
			int numParams = parameters.size();						
			for (int i=0;i<numParams;i++)
			{
				IParameter param = parameters.get(i);
				if (param != null)
				{
					int kind = param.getDirection();
					if (kind != PDK_RESULT)
					{
						resultParams.add(param);
					}
				}
			}
		}
		return resultParams;
	}

	/**
	 *
	 * Retrieves the name of the return type.
	 */
	public String getReturnType2()
	{
		String retTypeName = null;
		IParameter retParam = getReturnType();
		if (retParam != null)
		{
			IClassifier type = retParam.getType();
			if (type != null)
			{
				retTypeName = type.getName();
			}
		}
		return retTypeName;
	}

	/**
	 *
	 * Determines whether or not the parameters (including return type) 
	 * and name of the feature all match, in name and order.
	 *
	 * @param feature[in] The feature to match against this feature
	 */
	public boolean isSignatureSame(IBehavioralFeature feature)
	{
		return compareSignatures(feature,false);
	}

	/**
	 *
	 * Compares the signatures of this feature and the passed in feature.
	 *
	 * @param pFeature[in] The feature to match against
	 * @param isFormalCompare[in] - true if only NON result parameters are a part of the comparison
	 */
	protected boolean compareSignatures(IBehavioralFeature feature, boolean isFormalCompare)
	{
		boolean isSame = false;
		if (feature != null)
		{
			boolean sameName = isNameSame(feature);
			if (sameName)
			{
				ETList<IParameter> params1 = getParameters();
				ETList<IParameter> params2 = feature.getParameters();
				int count = 0;
				if (params1 != null)
				{
					count = params1.size();
				}
				boolean proceed = true;
				if (!isFormalCompare)
				{
					// If we're not doing a formal compare, i.e., we're checking every parameter,
					// let's do a quick check. Same number of parameters?
					int count2 = 0;
					if (params2 != null)
					{
						count2 = params2.size();
					}
					if (count != count2)
					{
						proceed = false;
					}
				}
				if (proceed)
				{
					// Ok, check the types of the parameters. Thankfully, this 
					// is not an n-squared check since the order must be the same
					int idx = 0;
					boolean sameParams = true;
					while (idx < count && sameParams)
					{
						IParameter pItem1 = params1.get(idx);
						IParameter pItem2 = params2.get(idx++);
						if (pItem1 != null && pItem2 != null)
						{
							sameParams = compareTypes(pItem1,pItem2,isFormalCompare);
						}
						else
						{
							// what if only one of the parameters is NULL? I guess we
							// should return false here.
							if ( (pItem1 == null && pItem2 != null) ||
								 (pItem2 == null && pItem1 != null) )
							{
								sameParams = false;
							}
						}
					}
					isSame = sameParams ? true:false;
				}
			}
		}
		return isSame;
	}
	
	/**
	 *
	 * Checks the types of all the parameters except for return types to see if they have the same types.
	 *
	 * @param feature[in] The feature to match against
	 */
	public boolean isFormalSignatureSame(IBehavioralFeature feature)
	{
		return compareSignatures(feature,true);
	}

	/**
	 *
	 * Compare the types of 2 typed elements are return true if they are the same.
	 * This compares the identity of the types, which are elements. It does NOT
	 * compare the names of the types. So, if the types are different, but actually
	 * have the same name, the result will be false, not true. This compares DOES
	 * include a check against parameter direction. If the direction does not match,
	 * then the parameters are different.
	 *
	 * @param pItem1[in] The first element
	 * @param pItem2[in] The second element
	 * @param isFormalCompare[in] True if we are just checking parameters that do not
	 *                            have their Direction property set to result.
	 *                            False if we are comparing all parameters
	 *
	 * @return true if the types are the same, else false
	 *
	 */
	protected boolean compareTypes(IParameter pItem1, IParameter pItem2,boolean isFormalCompare)
	{
		boolean same = false;
		int kindOne = pItem1.getDirection();
		int kindTwo = pItem2.getDirection();
		if( kindOne == kindTwo && ( kindOne == PDK_RESULT ) && isFormalCompare )
		{
			same = true;
		}
		else if (kindOne == kindTwo)
		{
			IClassifier type1 = pItem1.getType();
			IClassifier type2 = pItem2.getType();
			if (type1 != null && type2 != null)
			{
				same = type1.isSame(type2);
			}
		}
		return same;
	}
	
	/**
	 *
	 * Creates a Parameter with a default type and name.
	 */
	public IParameter createParameter3()
	{
		return createParameter(null,null);
	}

	/**
	 *
	 * Sets all the parameters who do not have a direction value of PDK_RESULT.
	 *
	 * @param pParms[in] The parameter collection
	 */
        public void setFormalParameters(ETList<IParameter> inputParams)
	{
		// get the formal parameters that are already on the element
		ETList<IParameter> formalParams = getFormalParameters();
		ETList<IParameter> newParameters = new ETArrayList<IParameter>();
		int numParams = 0;
		int numInputParams = 0;
		if (formalParams != null && inputParams != null)
		{
			numParams = formalParams.size();
			numInputParams = inputParams.size();
			// Check to see if we have any new parameters to add based on what is passed
			// in (the new list) vs what is already on the element
			for (int i=0; i<numInputParams; i++)
			{
				// looping through the new list
				IParameter inputParam = inputParams.get(i);
				if (inputParam != null)
				{
					boolean add = true;
					// looping through the existing list
					for (int j=0; j<numParams; j++)
					{
						boolean isSame = false;
						IParameter oldParam = formalParams.get(j);
						if (oldParam != null)
						{
							isSame = oldParam.isSame(inputParam);
							if (isSame)
							{
								// found it in the existing list, so do not need to add it
								add = false;
								break;
							}
						}
					}
					if (add)
					{
						newParameters.add(inputParam);
					}
				}
			}
			
			if (numParams > 0)
			{
				// Now we are looking to see if any of the existing items need to be
				// removed.
				for (int x=0; x<numParams; x++)
				{
					IParameter param = formalParams.get(x);
					if (param != null)
					{
						boolean exists = false;
						for (int y=0; y<numInputParams; y++)
						{
							IParameter inputParam = inputParams.get(y);
							if (inputParam != null)
							{
								exists = inputParam.isSame(param);
								if (exists)
									break;
							}
						}
						// if it doesn't exist on the new list, remove it from the existing list
						// events will fire
						if (!exists)
						{
							removeParameter(param);
						}
					}
				}
				
				// Now we will process the list one more time because the information in the list
				// may have been reordered.  So we must first remove everything from the existing
				formalParams = getFormalParameters();
				if (formalParams != null)
				{
//					set the events to be off
					boolean success = EventBlocker.startBlocking();
					try 
					{
						int numFormals = formalParams.size();
						for (int k=0; k<numFormals; k++)
						{
							IParameter parm = formalParams.get(k);
							if (parm != null)
							{
								removeParameter(parm);
							}							
						}
					}
					finally 
					{
						EventBlocker.stopBlocking(success);
					}

				}
			}
		}
		
		// Now our existing list is empty, so we need to put what was passed in into the
		// list.  We only want to fire an event on those items that are truly new to the
		// list.
		for (int k=0; k<numInputParams; k++)
		{
			IParameter inputParam = inputParams.get(k);
			if 	(inputParam != null)
			{
				boolean trulyNew = false;
				for(int i = 0 ; i < newParameters.size(); i++) 
				{
					trulyNew = false;
					//If it is a truly new parameter add without blocking events.
					if(inputParam.isSame(newParameters.get(i)))
					{					
						addParameter(inputParam);
						trulyNew = true;
						continue;
					}
					
					//Existing parameter, block events.
					boolean success = false;
					try 
					{
						success = EventBlocker.startBlocking();
						addParameter(inputParam);
					}
					finally
					{
						EventBlocker.stopBlocking(success);
					}											
				}
			}
		}
	}
        
       
        public  void setFormalParameters2(ETList<IParameter> inputParams)
	{   
            ETList<IParameter> formalParams = getFormalParameters();
            ETList<IParameter> newParameters = new ETArrayList<IParameter>();
            IParameter copiedParam = null;
            
            if (inputParams != null)
            {
                // We have to clone the imputParams because inputParams and 
                // fomalParams store the same instances of parameter objects but
                // the parameters could be in different order. Therefore, removing
                // parameters in formalParams will also affect those in inputParams
                for (IParameter param : inputParams)
                {
                    copiedParam = cloneFormalParameter(param);
                    if (copiedParam != null)
                    {
                        newParameters.add(copiedParam);
                    }
                }
                
                // remove all existing formal parameters
                if (formalParams != null)
                {
                    // block events.
                    boolean success = EventBlocker.startBlocking();
                    try
                    {
                        for (IParameter param : formalParams)
                        {
                            removeParameter(param);
                        }
                    }
                    finally
                    {
                        EventBlocker.stopBlocking(success);
                    }
                }
                
                // add new params
                if (newParameters != null && newParameters.size() > 0)
                {
                    for (IParameter param : newParameters)
                    {
                        if (param != null)
                        {
                            addParameter(param);
                        }
                    }
                }
            }
	}
        

	/**
	 *
	 * Retrieves the current value of the Native setting of this feature.
	 * This is specific to the Java language.
	 */
	public boolean getIsNative()
	{
		return super.getBooleanAttributeValue("isNative", false);
	}

	/**
	 *
	 * Sets the value of the Native keyword. Specific to Java.
	 *
	 * @param newVal[in] The new value.
	 */
	public void setIsNative(boolean value)
	{
		boolean isNative = getIsNative();
		// No need to set if values aren't different
		if (isNative != value)
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp =
						(IClassifierEventDispatcher) ret.getDispatcher(
									EventDispatchNameKeeper.classifier());
		
			boolean proceed = true;
			IEventPayload payload = null;
			if( disp != null )
			{
			   payload = disp.createPayload("PreNativeModified");	   
			   proceed = disp.firePreNativeModified(this,value,payload);
			}
			if (proceed)
			{
				super.setBooleanAttributeValue("isNative",value);
				if( disp != null )
				{
				   payload = disp.createPayload("NativeModified");	   
				   disp.fireNativeModified(this,payload);
				}
			}
			else
			{
				//throw exception		
			}			
		}
	}

	/**
	 *
	 * Retrieves whether of not this feature has the strictfp modifier associated with it.
	 */
	public boolean getIsStrictFP()
	{
		return super.getBooleanAttributeValue("isStrictFP",false);
	}

	/**
	 *
	 * Sets the value of the strictfp modifier on this feature
	 *
	 * @param newVal[in] The new value
	 */
	public void setIsStrictFP(boolean value)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("PreStrictFPModified");	   
		   proceed = disp.firePreStrictFPModified(this,value,payload);
		}
		if (proceed)
		{
			super.setBooleanAttributeValue("isStrictFP",value);
			if( disp != null )
			{
			   payload = disp.createPayload("StrictFPModified");	   
			   disp.fireStrictFPModified(this,payload);
			}
		}
		else
		{
			//throw exception		
		}		
	}

	/**
	 *
	 * Creates a Parameter that is the return type
	 */
	public IParameter createReturnType()
	{
		IParameter pTempParam = createParameter(null,null);
		if (pTempParam != null)
		{
			pTempParam.setDirection(PDK_RESULT);
		}
		return pTempParam;
	}

	//INamespace methods
	
	public boolean addOwnedElement(INamedElement elem)
	{
		return m_NamespaceAggregate.addOwnedElement(elem);
	}
	
	public void removeOwnedElement(INamedElement elem)
	{
		m_NamespaceAggregate.removeOwnedElement(elem);
	}
	
	public ETList<INamedElement> getOwnedElements()
	{
		return m_NamespaceAggregate.getOwnedElements();
	}
	
	public void addVisibleMember(INamedElement elem)
	{
		m_NamespaceAggregate.addVisibleMember(elem);
	}
	
	public void removeVisibleMember(INamedElement elem)
	{
		m_NamespaceAggregate.removeVisibleMember(elem);
	}
	
	public ETList<INamedElement> getVisibleMembers()
	{
		return m_NamespaceAggregate.getVisibleMembers();
	}
	
	public ETList<INamedElement> getOwnedElementsByName(String name)
	{
		return m_NamespaceAggregate.getOwnedElementsByName(name);
	}
	
	public long getOwnedElementCount()
	{
		return m_NamespaceAggregate.getOwnedElementCount();
	}
	
	public long getVisibleMemberCount()
	{
		return m_NamespaceAggregate.getVisibleMemberCount();
	}

	public IPackage createPackageStructure(String packageStructure)
	{
		return m_NamespaceAggregate.createPackageStructure(packageStructure);
	}
}
