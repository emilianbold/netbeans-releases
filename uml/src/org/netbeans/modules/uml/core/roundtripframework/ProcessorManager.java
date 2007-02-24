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

/*
 * File       : ProcessorManager.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import java.util.HashMap;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;

/**
 * @author Aztec
 */
public class ProcessorManager implements IProcessorManager
{
    private static final String ROUND_TRIP_CONTEXT = "RoundTrip";
    
    private HashMap<String, IRequestProcessor> m_ProcessorCache = new HashMap<String, IRequestProcessor>();

       IRoundTripController m_Controller = null; // This is raw to prevent circular refs
       ILanguageManager m_LanguageManager = null;
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#createProcessor(java.lang.String)
     */
    
    public ProcessorManager()
    {        
    }
    
    public ProcessorManager(IRoundTripController controller)
    {
        m_Controller = controller;
    }
    
    public IRequestProcessor createProcessor(String procID)
    {
        IRequestProcessor req = null;
        if (m_Controller != null)
        {
            IRoundTripEventDispatcher disp = m_Controller.getRoundTripDispatcher();

            if (disp != null)
            {
                IEventPayload load = disp.createPayload("PreInitialized");

                if (disp.firePreInitialized(procID, load))
                {
                    try
                    {
                        req = (IRequestProcessor)(Class.forName(procID).newInstance());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
    
                    // Must initialize the processor because it might be an event
                    // listener itself
                    if(req == null) return null;
                    
                    req.initialize(m_Controller);

                    // We can now tell the addin (most likely an integration) that
                    // the request processors are ready.

                    m_ProcessorCache.put(procID, req);
                    
                    load = disp.createPayload("Initialized");
                    disp.fireInitialized(req, load);
                }
            }
        }
        return req;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishCreateProcessors(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, boolean)
     */
    public boolean establishCreateProcessors(
        INamedElement element,
        boolean overrideCheck)
    {
        boolean established = false;
        if (element != null)
        {
            IRTStateTester rt = new RTStateTester();

            if(rt.isElementRoundTripable(element))
            {
                INameModifyPreRequest tempRequest = new NameModifyPreRequest();
                if (tempRequest.inCreateState(element))
                {
                    // Determine what mode the project that this element is in.
                    // If we are in implementation mode, find out what language

                    String topID = element.getTopLevelId();

                    if(topID != null)
                    {
                        ICoreProduct prod = ProductRetriever.retrieveProduct();
                        if (prod != null)
                        {
                            IApplication app = prod.getApplication();                            
                            if(app != null)
                            {
                                IProject proj = app.getProjectByID(topID);
                                
                                if (proj != null)
                                    established = establishProcessorsForProject(proj, 
                                            overrideCheck)
                                                .getParamTwo().booleanValue();
                            }
                        }
                    }
                }
                else
                {
                    established = establishProcessors(element, overrideCheck);
                }
            }
        }
        return established;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessor(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage)
     */
    public IRequestProcessor establishProcessor(ILanguage pLang)
    {
        IRequestProcessor pProc = null;
        if(pLang != null)
        {
            String procID = pLang.getContextCLSID(ROUND_TRIP_CONTEXT);
            pProc = establishProcessorWithID(procID);
        }
        return pProc;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessor(java.lang.String)
     */
    public IRequestProcessor establishProcessor(String procID)
    {
        if (procID == null) return null;
        
        IRequestProcessor proc = m_ProcessorCache.get(procID);
        
        if(proc == null)
        {
            proc = createProcessor(procID);
        }
        return proc;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessors()
     */
    public boolean establishProcessors(ETList<ILanguage> langs)
    {
        boolean established = false;
        if (langs == null) return established;
        
        int count = langs.size();
        for (int i = 0 ; i < count ; ++i)
        {
            if (establishProcessor(langs.get(i)) != null)
                established = true;
        }
        
        return established;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessors(boolean, boolean, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean establishProcessors(
        boolean create,
        boolean override,
        IElement element)
    {
        boolean established = false;
        
        if (create) 
        { 
            INamedElement pNElement  = null;
            try
            {
                pNElement = (INamedElement)element;
            }
            catch(Exception e){}
           
            if (pNElement == null) 
            { 
                established = establishProcessors(element, override); 
            } 
            else 
            { 
                established = establishCreateProcessors(pNElement, override); 
            } 
        } 
        else 
        { 
            established = establishProcessors(element, override); 
        } 
        return established;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessors(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, boolean)
     */
    public boolean establishProcessors(IElement element, boolean overrideCheck)
    {
        boolean established = false;
        if(element != null)
        {
            IRTStateTester rt = new RTStateTester();

            if (rt.isElementRoundTripable(element))
            {
                if (overrideCheck || rt.isAppInRoundTripState(element))
                {
                    ETList< ILanguage > langs = element.getLanguages();

                    if (langs != null)
                    {
                        established = establishProcessors (langs);
                    }
                }
            }
        }
        return established;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessorsForProject(org.netbeans.modules.uml.core.metamodel.structure.IProject, boolean, java.lang.String)
     */
    public ETPairT<String,Boolean> establishProcessorsForProject(
            IProject project, boolean overrideCheck)
    {
        boolean established = false;
        
        if(project == null) return new ETPairT<String,Boolean>(null, new Boolean(false));
        String mode = project.getMode();
        String language = null;
        IRTStateTester rt = new RTStateTester();

        if (overrideCheck || rt.isProjectInRoundTripState(project))
        {
            language = project.getDefaultLanguage();   
            if(language != null)
            {
                String procID = retrieveProcessorIDByLang(language);
                established = establishProcessorWithID(procID) != null;
            }
        }
        return new ETPairT<String,Boolean>( language, new Boolean(established) );
    }
  

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessorsForProject(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
     */
    public void establishProcessorsForProject(IWSProject project)
    {
        if(project == null) return;
        
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        if (prod != null)
        {
            IApplication app = prod.getApplication();
            if (app != null)
            {
                IProject proj = app.getProjectByName(project.getName());

                if (proj != null)
                {                    
                    establishProcessorsForProject(proj, false);                 
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#establishProcessorWithID(java.lang.String)
     */
    public IRequestProcessor establishProcessorWithID(String procID)
    {
        return establishProcessor(procID);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#getLanguageManager()
     */
    public ILanguageManager getLanguageManager()
    {
        if (m_LanguageManager == null)
        {
            ICoreProduct prod = ProductRetriever.retrieveProduct();
            if (prod != null)
            {
                m_LanguageManager = prod.getLanguageManager();
            }
        }
        return m_LanguageManager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IProcessorManager#retrieveProcessorIDByLang(java.lang.String)
     */
    public String retrieveProcessorIDByLang(String lang)
    {
        return getLanguageManager().retrieveContextForLanguage(lang, ROUND_TRIP_CONTEXT);
    }

}
