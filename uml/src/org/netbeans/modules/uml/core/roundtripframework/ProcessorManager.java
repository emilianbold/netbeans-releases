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
