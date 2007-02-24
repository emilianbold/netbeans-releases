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
 * File       : IProcessorManager.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;

/**
 * @author Aztec
 */
public interface IProcessorManager
{
    public boolean establishProcessors(boolean create, boolean override, 
                                        IElement element);
    
    public boolean establishProcessors(IElement element, boolean overrideCheck);
    
    public boolean establishProcessors(ETList<ILanguage> langs);
    
    public IRequestProcessor establishProcessor(ILanguage pLang);
    
    public IRequestProcessor establishProcessorWithID(String procID);
    
    public boolean establishCreateProcessors(INamedElement element, 
                                                boolean overrideCheck);
    
    public IRequestProcessor establishProcessor(String procID);
    
    public IRequestProcessor createProcessor(String procID);
    
    public ETPairT<String,Boolean> establishProcessorsForProject(
            IProject project, boolean overrideCheck);
                                                    
    public void establishProcessorsForProject(IWSProject project);
    
    public String retrieveProcessorIDByLang(String lang);
    
    public ILanguageManager getLanguageManager();
}
