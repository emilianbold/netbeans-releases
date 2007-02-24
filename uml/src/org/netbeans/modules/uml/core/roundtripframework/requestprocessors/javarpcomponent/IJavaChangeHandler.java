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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public interface IJavaChangeHandler
{
    public void handleRequest(IRequestValidator request);

    public void setSilent(boolean silent);
    public boolean getSilent();

    public void addDependency(IRequestValidator request, IElement pDependent, IElement pIndependent);
    public void addDependencies(IRequestValidator request, IClassifier pClass, ETList<IClassifier> list, boolean toClass);
    public void removeDependency (IRequestValidator request, IElement pDependent, IElement pIndependent);

    public void plug(IRequestPlug plug);

    public void setProcessor (IRequestProcessor pProcessor );
    public IRequestProcessor getProcessor ();
    
    public void setPlugManager (IPlugManager manager);
    public IPlugManager getPlugManager();
    
    public void setChangeHandlerUtilities(IJavaChangeHandlerUtilities utils);
    public IJavaChangeHandlerUtilities getChangeHandlerUtilities();

    public String getLanguageName();

    // The new queries

    public IHandlerQuery findQuery (String key);
    public boolean doQuery(String key);
    public boolean doQuery(String key, String arg1);
    public boolean doQuery(String key, String arg1, String arg2);
    public boolean doQuery(String key, String arg1, String arg2, String arg3);
    public boolean doQuery(String key, String arg1, String arg2, String arg3, String arg4);
    
    public void clearAllQueries(boolean onlyNonPersistent); // physically deletes the queries
    public void resetAllQueries(); // Just resets the queries. Use this preferably.
    public void addQuery(IHandlerQuery pQuery);

    public void enterBatch();
    public void exitBatch();
    public boolean inBatch();
}
