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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.util.Iterator;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.DependencyChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

/**
 * @author Aztec
 */
public class JavaChangeHandler implements IJavaChangeHandler
{
    private boolean m_Silent;
    private boolean m_Batch;

    private IRequestProcessor m_Processor = null;
   
    private ETList<IHandlerQuery>   m_Queries = 
    					new ETArrayList<IHandlerQuery>();
    private ETList<IHandlerQuery> m_NonPersistentQueries = 
    					new ETArrayList<IHandlerQuery>();
    private IPlugManager m_PlugManager;    
    protected IJavaChangeHandlerUtilities m_Utilities = null; 

    
    public JavaChangeHandler()
    {
        m_Silent = false;
        m_Batch = false;

        m_Utilities = null;
        m_Processor = null;        
    }
    
    public JavaChangeHandler(IJavaChangeHandlerUtilities utilities)
    {
        m_Silent = false;
        m_Batch = false;

        m_Processor = null;
        setChangeHandlerUtilities(utilities);        
    }
    
    public JavaChangeHandler(IJavaChangeHandler copy)
    {
    	if (copy != null)
    	{
			m_Silent = copy.getSilent();
			m_Batch = copy.inBatch();

			m_Processor = copy.getProcessor();
			setChangeHandlerUtilities(copy.getChangeHandlerUtilities());
    	}
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#addDependencies(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void addDependencies(IRequestValidator request, IClassifier pClass, 
    							ETList<IClassifier> list, boolean toClass)
    {
        if (pClass != null && list != null)
        {
        	Iterator<IClassifier> iter = list.iterator();
        	if (iter != null)
        	{
        		while (iter.hasNext())
        		{
        			IClassifier item = iter.next();
					// AddDependency checks to see if the two classifiers
					// are now in the same package.
        			if (toClass)
        				addDependency(request, pClass, item);
        			else
						addDependency(request, item, pClass); 	
        		}
        	}
        }
    }

    public void addDependency(  IRequestValidator request,
						        IElement dependent,
						        IElement independent)
    {
		if ( dependent != null && independent != null && m_Utilities != null)
		{
		   // We only want to do this if the two elements are in different packages.
		   IPackage dependentPackage = m_Utilities.getPackage(dependent);
		   IPackage independentPackage = m_Utilities.getPackage(independent);
		   
		   boolean isSame = false;
		   if ( dependentPackage == null && independentPackage == null )
		   {
			  // Two null package are considered the same here.
			  isSame = true;
		   }
		   else if ( dependentPackage != null && independentPackage != null )
		   {
			  isSame = dependentPackage.isSame(independentPackage);
		   }
		   else if ( independentPackage == null )
		   {
			  // We know that they are not both null, but, the independent package is
			  // null, so they are by default not the same. However, we don't want
			  // to create a dependency on a null package, so just pretend they are
			  // the same to bypass the creation of the dependency
			  isSame = true;
		   }
		   else
		   {
			  // They are not both null, they are not both not-null, and the 
			  // independent package is not null. The dependent package must be
			  // null. But we do want to create a dependency in this case.
			  isSame = false;
		   }
		   
		   if (!isSame)
		   {
				// Now, don't create a dependency if one already exists.
				if ( !m_Utilities.isDependent(dependent, independentPackage) )
				{
				   // Must create a dependency between the dependent element, and the 
				   // independent package.			   
				   IChangeRequest newRequest = m_Utilities.createChangeRequest(
				            DependencyChangeRequest.class,
				   		    ChangeKind.CT_MODIFY,
							RequestDetailKind.RDT_DEPENDENCY_ADDED,
							dependent,
							dependent,
							dependent);
	
				   if ( newRequest != null) 
				   {
					  IDependencyChangeRequest depReq = (IDependencyChangeRequest)
					  									  newRequest;
					  if ( depReq != null )
					  {
						 // FOR NOW, we always generate PACKAGE DEPENDENCIES only.
						 // This means we don't have to worry about class name changes.	
						 depReq.setIndependentElement((IElement)independentPackage);
	
						 // now just add the new request to the passed in request.	
						 request.addRequest( depReq );
					  }
				   }
				}
		   }
		}
    }

	/**
	 *
	 * Add a new query to the list.
	 *
	 * @param query[in]
	 */
    public void addQuery(IHandlerQuery query)
    {
       if (query != null)
       {
       	   if (query.getPersist())
       	   	   m_Queries.add(query);
       	   else
			   m_NonPersistentQueries.add(query);	  	
       }
    }


	/**
	 * Physically deletes the queries
	 *
	 * @param onlyNonPersistent[in] If true, only the non-persistent queries are deleted.
	 */
    public void clearAllQueries(boolean onlyNonPersistent)
    {
		if ( !onlyNonPersistent )		
			m_Queries.clear();
		m_NonPersistentQueries.clear();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#doQuery(java.lang.String, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean doQuery(
					        String key,
					        String arg1,
					        String arg2,
					        String arg3,
					        String arg4)
    {
		IHandlerQuery query = preDoQuery(key);
		boolean queryResult = false;
		
		// Now do the query. The query knows whether to actually bring up 
		// the dialog or not.
		if (query != null)
		{
			queryResult = query.doQuery(arg1,arg2,arg3,arg4);
		}
		return queryResult;

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#doQuery(java.lang.String, long, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean doQuery(
					        String key,
					        String arg1,
					        String arg2,
					        String arg3)
    {
		IHandlerQuery query = preDoQuery(key);
		boolean queryResult = false;
		
		// Now do the query. The query knows whether to actually bring up 
		// the dialog or not.
		if (query != null)
		{
			queryResult = query.doQuery(arg1, arg2, arg3);
		}
		return queryResult;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#doQuery(java.lang.String, long, java.lang.String, java.lang.String)
     */
    public boolean doQuery(String key, String arg1, String arg2)
    {
		IHandlerQuery query = preDoQuery(key);
		boolean queryResult = false;
		
		// Now do the query. The query knows whether to actually bring up 
		// the dialog or not.
		if (query != null)
		{
			queryResult = query.doQuery(arg1,arg2);
		}
		return queryResult;
    }

	/**
	 * Do the specified Query. 
	 * If the query does not currently exist, it will be built at this time.
	 *
	 * @param key[in]
	 * @param parent[in]
	 * 
	 * @return queryResult
	 */
    public boolean doQuery(String key, String arg1)
    {
		IHandlerQuery query = preDoQuery(key);
		boolean queryResult = false;
		
		// Now do the query. The query knows whether to actually bring up 
		// the dialog or not.
		if (query != null)
		{
			queryResult = query.doQuery(arg1);
		}
		return queryResult;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#doQuery(java.lang.String, long)
     */
    public boolean doQuery(String key)
    {
		IHandlerQuery query = preDoQuery(key);
		boolean queryResult = false;
		
		// Now do the query. The query knows whether to actually bring up 
		// the dialog or not.
		if (query != null)
		{
			queryResult = query.doQuery();
		}
		return queryResult;
    }

	protected IHandlerQuery preDoQuery (String key)
	{
		IHandlerQuery query = findQuery(key);
		if (query == null)
		{
			// This query is not built yet. Use the virtual BuildQuery to 
			// build the right one and add it to the list.
			query = buildQuery(key);
			if (query != null)
				addQuery(query);
		}
		return query;
	}


	protected IHandlerQuery buildQuery(String key)
	{
		return new HandlerQuery(key,
								"DummyText",
								"DummyTitle",
								false,
								ErrorDialogIconKind.EDIK_ICONERROR,
								false,
								true);
	}

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#enterBatch()
     */
    public void enterBatch()
    {
		m_Batch = true;
		
		// We must reset all queries when we enter or exit batch,
		// since the next round of processing might or might not
		// be in batch mode. This is basically an "ensure state"
		// mechanism. Should not be a performance problem. If the 
		// batch flag becomes a refcount, we MIGHT have to rethink
		// this, but even then, if we reset when we transition from
		// 0 to 1 and from 1 to 0, we should cover all cases.
		resetAllQueries();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#ExitBatch()
     */
    public void exitBatch()
    {
		 m_Batch = false;

		 // since the next round of processing might or might not
		 // be in batch mode. This is basically an "ensure state"
		 // mechanism. Should not be a performance problem.		
		 resetAllQueries ();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#findQuery(java.lang.String, long)
     */
    public IHandlerQuery findQuery(String key)
    {
		boolean found = false;
		IHandlerQuery retQuery = null;
		if (key != null)
		{
			Iterator<IHandlerQuery> iter = null;
			if (m_Queries != null)
			{
				iter = m_Queries.iterator();
				if (iter != null)
				{
					while (iter.hasNext())
					{
						IHandlerQuery query = iter.next();
						if (query != null && key.equals(query.getKey()))
						{
							found = true;
							retQuery = query;
							break;
						}
					}
				}
			}
			iter = null;
			// look in the non-persistent list if we did not find it yet.
			if (!found)
			{
				if (m_NonPersistentQueries != null)
				{
					iter = m_NonPersistentQueries.iterator();
					if (iter != null)
					{
						while (iter.hasNext())
						{
							IHandlerQuery query = iter.next();
							if (query != null && key.equals(query.getKey()))
							{
								found = true;
								retQuery = query;
								break;
							}
						}
					}
				}	
			}
		}
		return retQuery;
    }

    public IJavaChangeHandlerUtilities getChangeHandlerUtilities()
    {
        return m_Utilities;
    }

    /*
     * Gets the Langugage Name
     */
    public String getLanguageName()
    {
    	String retLangName = null;
		IRequestProcessor pProc = getProcessor();
		if (pProc != null)
		{
			ILanguage lang = pProc.getLanguage2();
			if (lang != null)
			{
				retLangName = lang.getName();
			}
		}
		return retLangName;
    }

	/**
	 * Retrieves the plug manager used to control the change handler.
	 *
	 * @param manager [out] The plug manager.
	 */
    public IPlugManager getPlugManager()
    {
		return m_PlugManager;
    }


	/**
	 * Retrieve the request processor that owns the change handler.
	 *
	 * @reutrn pProcessor The request processor.
	 */
    public IRequestProcessor getProcessor()
    {
		return m_Processor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#getSilent()
     */
    public boolean getSilent()
    {
		boolean retval = m_Silent;
		if ( !retval )
		{
		   retval = m_Utilities.isSilent();
		}
		return retval;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#handleRequest(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator)
     */
    public void handleRequest(IRequestValidator request)
    {
        //Its a pure virtual funtion in C++. So the sub class of it should implement it.
    } 

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#inBatch()
     */
    public boolean inBatch()
    {
		return m_Batch;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#plug(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestPlug)
     */
    public void plug(IRequestPlug requestPlug)
    {        
		requestPlug.plug(getPlugManager());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#removeDependency(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void removeDependency(
						        IRequestValidator request,
						        IElement dependent,
						        IElement independent)
    {
        //C++ method is empty
    }

	/**
	 *
	 * Does not delete the queries, just reinitialize them.
	 * This is preferable to ClearAllQueries().
	 */
    public void resetAllQueries()
    {
		Iterator<IHandlerQuery> iter = null;
		if (m_Queries != null)
		{
			iter = m_Queries.iterator();
			if (iter != null)
			{
				while (iter.hasNext())
				{
					IHandlerQuery query = iter.next();
					query.reset();
				}
			}
		}
		// always clear the non-persistent queries
		clearAllQueries (true);
    }

	/**
	 * Sets the change handler utilities use to perform change handler processing.
	 *
	 * @param utils [out] The change handler utilities.
	 */
    public void setChangeHandlerUtilities(IJavaChangeHandlerUtilities utils)
    {
		m_Utilities = utils;
    }

	/**
	 * Sets the plug manager used to control the change handler.
	 *
	 * @param manager [out] The plug manager.
	 */
    public void setPlugManager(IPlugManager manager)
    {
		m_PlugManager = manager;
    }

	/**
	 * Sets the request processor that owns the change handler.
	 *
	 * @param pProcessor The request processor.
	 */
    public void setProcessor(IRequestProcessor pProcessor)
    {
		m_Processor = pProcessor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandler#setSilent(boolean)
     */
    public void setSilent(boolean silent)
    {
		m_Silent = silent;
    }
    
    protected boolean displayYesNoMessage (int textID, int titleID, boolean defaultAnswer)
    {
    	//Aztec: TODO:
    	//needs to get the text strings corresponding to the ids from 
    	//a ResourceBundle
    	
        return false;
    }
    
    protected boolean displayYesNoMessage (String text, String title,
                                                           boolean  defaultAnswer)
    {
        boolean retval = defaultAnswer;
        if(!getSilent())
        {
            IQuestionDialog dlg = new SwingQuestionDialogImpl();
            
            QuestionResponse dlgAnswer = null;
            
            int dType = SimpleQuestionDialogKind.SQDK_YESNO;
            
            int iconType = ErrorDialogIconKind.EDIK_ICONQUESTION;
            int defaultDlgAnswer = SimpleQuestionDialogResultKind.SQDRK_RESULT_NO; 
            if(defaultAnswer)
            {
                defaultDlgAnswer = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
            }
            
            dlgAnswer = dlg.displaySimpleQuestionDialogWithCheckbox(dType, iconType, text, null, title, defaultDlgAnswer, false);

            if(dlgAnswer != null && dlgAnswer.getResult() ==  SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
            {
                retval = true;
            }
        }
        return retval;
    }
    
	protected void displayMessage ( String textID, String titleID )
	{
		//Aztec: TODO: Error dialog
	}
	
	protected void displayMessage ( int textID, int titleID )
	{
		//Aztec: TODO: Error dialog
	}
	
	/**
	 *
	 * Turns RoundTrip on or off depending on the passed in flag
	 *
	 * @param flag[in] true to turn it on, else false
	 */
	protected void turnRoundTripOn(boolean flag)
	{
		ICoreProduct pProd = ProductRetriever.retrieveProduct();
		if (pProd != null)
		{
			IRoundTripController rt = pProd.getRoundTripController();
			if (rt != null)
				rt.setMode(flag ? RTMode.RTM_LIVE : RTMode.RTM_OFF);
		}
	}
}
