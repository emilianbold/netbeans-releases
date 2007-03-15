/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.xml.wsdl.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.ErrorManager;



/**
 *
 * @author Sonali Kochar
 */
public class WSDLWhereUsedRefactoringPlugin extends WSDLRefactoringPlugin {
    
   
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    private WhereUsedQuery query;
    
    
    /**
     * Creates a new instance of WSDLWhereUsedRefactoringPlugin
     */
    public WSDLWhereUsedRefactoringPlugin(WhereUsedQuery refactoring) {
        this.query = refactoring;
    }
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
        Problem problem = null;
       
        return problem;
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
      //  System.out.println("WSDLWhereUsedRefactoring:: prepare called");
        Referenceable ref = query.getRefactoringSource().lookup(Referenceable.class);
        if (ref == null)
            return null;
        
        session = refactoringElements.getSession();
        fireProgressListenerStart(ProgressEvent.START, -1);
        Component searchRoot = query.getContext().lookup(Component.class);
        this.findErrors = new ArrayList<ErrorItem>();
        Set<Component> searchRoots = new HashSet<Component>();
        if(searchRoot == null )
            searchRoots = getSearchRoots(ref);
        else
            searchRoots.add(searchRoot);
        
        for (Component root : searchRoots) {
            List<WSDLRefactoringElement> elements = find(ref, root);
                if (elements != null) {
                    for (WSDLRefactoringElement  element : elements) {
                        //System.out.println("WSDLWhereusedRefactoring::adding element");
                        refactoringElements.add(query, element);
                        fireProgressListenerStep();
                    }
                }
            }
        
        fireProgressListenerStop();
        //System.out.println("done");
        
      return null;
    }
    
      
    
}

