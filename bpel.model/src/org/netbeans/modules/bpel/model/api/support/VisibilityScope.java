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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.openide.util.Lookup;

/**
 * Describes a visibility scope for a bpel element. 
 * There are 2 different kind of visibility scope: for activities and for variables. 
 * Activities are visible inside of Scope elements. 
 * Variables are visible inside of VariableDeclarationScope elements. 
 * The visibility scope is the chain of such elements. This chain is calculated 
 * relative the certain target BPEL element and defines the containers 
 * with elements visible to the target element.  
 * <p>
 * The Scopes has particular order in the chain.
 * The first element of visibility zone is the Process.
 * The most nested scope is at the end of the chain.
 *
 * ATTENTION!
 * TODO: It necessary to check if using of this class is safe. 
 * The matter is that the scope list is cached here and can be chenged after it. 
 * So the cached value can be obsolete.
 *
 * @author nk160297
 */
public class VisibilityScope {
    
    /**
     * This model element has been used to construct the current instance
     */
    private BpelEntity baseModelElement;
    
    private List<BaseScope> myScopeList;
    private List<VariableDeclarationScope> myVarScopeList;
    
    private VisibleVariables mVisibleVariables;
    
    public VisibilityScope(BpelEntity modelElement, Lookup lookup) {
        baseModelElement = modelElement;
    }

    public VisibilityScope(BpelEntity modelElement) {
        baseModelElement = modelElement;
    }

    private void calculateScopeList(final BpelEntity modelElement) {
        myScopeList = new ArrayList<BaseScope>();
        FindHelper helper =
                (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
        Iterator<BaseScope> itr = helper.scopeIterator(modelElement);
        while (itr.hasNext()) {
            BaseScope baseScope = itr.next();
            // Put new element to the top of list to provide the sorting contract.
            myScopeList.add(0, baseScope);
        }
        //
        if (modelElement instanceof Process && myScopeList.isEmpty()) {
            myScopeList.add((BaseScope)modelElement);
        }
    }
    
    private void calculateVarScopeList(final BpelEntity modelElement) {
        myVarScopeList = new ArrayList<VariableDeclarationScope>();
        FindHelper helper =
                (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
        Iterator<VariableDeclarationScope> itr =
                helper.varaibleDeclarationScopes(modelElement);
        while (itr.hasNext()) {
            VariableDeclarationScope vdScope = itr.next();
            // Put new element to the top of list to provide the sorting contract.
            myVarScopeList.add(0, vdScope);
        }
        //
        if (modelElement instanceof Process && myVarScopeList.isEmpty()) {
            myVarScopeList.add((BaseScope)modelElement);
        }
    }
    
    /**
     * Returns the Bpel element relative to which the visibility scope was constructed.
     */
    public BpelEntity getBaseModelElement() {
        return baseModelElement;
    }
    
    public List<BaseScope> getScopeChain() {
        if (myScopeList == null) {
            calculateScopeList(baseModelElement);
        }
        return myScopeList;
    }
    
    public BaseScope getClosestScope() {
        List<BaseScope> scopeList = getScopeChain();
        assert !scopeList.isEmpty() : "The VisibilityScope should never be empty"; 
        return scopeList.get(scopeList.size() - 1);
    }
    
    public List<VariableDeclarationScope> getVarScopeChain() {
        if (myVarScopeList == null) {
            calculateVarScopeList(baseModelElement);
        }
        return myVarScopeList;
    }
    
    public VariableDeclarationScope getClosestVarScope() {
        List<VariableDeclarationScope> varScopeList = getVarScopeChain();
        assert !varScopeList.isEmpty() : "The VisibilityScope should never be empty";
        return varScopeList.get(varScopeList.size() - 1);
    }
    
    /**
     * Looks for a variable by name in the visibility scope 
     * @param varName
     * @return variable or null
     */
    public VariableDeclaration lookForVariable(String varName) {
        VariableDeclaration result = null;
        //
        List<VariableDeclarationScope> scopeList = getVarScopeChain();
        ListIterator<VariableDeclarationScope> itr = 
                scopeList.listIterator(scopeList.size());
        while (itr.hasPrevious()) {
            VariableDeclarationScope varScope = itr.previous();
            List<VariableDeclaration> varDeclList = Utils.getVarDeclInScope(varScope);
            if (varDeclList != null) {
                for (VariableDeclaration varDecl : varDeclList) {
                    if (varDecl.getVariableName().equals(varName)) {
                        return varDecl;
                    }
                }
            }
        }
        //
        return null;
    }
    
    public synchronized VisibleVariables getVisibleVariables() {
        if (mVisibleVariables == null) {
            mVisibleVariables = calculateVisibleVariables();
        }
        return mVisibleVariables;
    }
    
    /**
     * Traverses the specified visibility scope and collects all 
     * overridden variables.
     * @param visScope to travrse over
     * @return the set of overridden variables
     */
    private VisibleVariables calculateVisibleVariables() {
        VisibleVariables result = new VisibleVariables();
        //
        HashSet<String> visibleNames = new HashSet<String>();
        //
        List<VariableDeclarationScope> vdScopeList = getVarScopeChain();
        for (int index = vdScopeList.size() - 1; index >=0; index--) {
            VariableDeclarationScope vdScope = vdScopeList.get(index);
            List<VariableDeclaration> varList = Utils.getVarDeclInScope(vdScope);
            for (VariableDeclaration varDecl : varList) {
                String varName = varDecl.getVariableName();
                //
                if (visibleNames.contains(varName)) {
                    result.mAllOverridenVariables.add(varDecl);
                } else {
                    visibleNames.add(varName);
                    result.mAllVisibleVariables.add(varDecl);
                }
            }
        }
        //
        return result;
    }
    
    public static class Utils {
        
        public static List<Scope> getNestedScopes(BpelEntity startFrom) {
            List<Scope> scopes = new ArrayList<Scope>();
            addNestedScopes(startFrom, scopes);
            return scopes;
        }
        
        public static List<VariableDeclarationScope> getNestedVarScopes(
                BpelEntity startFrom) {
            List<VariableDeclarationScope> scopes = 
                    new ArrayList<VariableDeclarationScope>();
            addNestedVarScopes(startFrom, scopes);
            return scopes;
        }
        
        /**
         * Iterate recursively over the BPEL Object model and looking for
         * nested Scope elements. Searching goes on only to the nearest Scopes.
         */
        private static void addNestedScopes(
                BpelEntity startFrom, List<Scope> scopeList) {
            if (startFrom == null || scopeList == null) {
                return;
            } else if (startFrom instanceof Scope) {
                // Check to Scope should be the first to catch only the nearest Scopes!!!
                scopeList.add((Scope)startFrom);
            } else if (startFrom instanceof ActivityHolder) {
                addNestedScopes(
                        ((ActivityHolder)startFrom).getActivity(), scopeList);
            } else if (startFrom instanceof CompositeActivity) {
                ExtendableActivity[] actArr =
                        ((CompositeActivity)startFrom).getActivities();
                for (ExtendableActivity act : actArr) {
                    addNestedScopes(act, scopeList);
                }
            }
        }
        
        /**
         * Iterate recursively over the BPEL Object model and looking for
         * nested VariableDeclarationScope elements. 
         * Searching goes on only to the nearest VariableDeclarationScopes.
         */
        private static void addNestedVarScopes(
                BpelEntity startFrom, List<VariableDeclarationScope> scopeList) {
            if (startFrom == null || scopeList == null) {
                return;
            } else if (startFrom instanceof VariableDeclarationScope) {
                // Check to VariableDeclarationScope should be the first 
                // in order to catch only the nearest Scopes!!!
                scopeList.add((VariableDeclarationScope)startFrom);
            } else if (startFrom instanceof ActivityHolder) {
                addNestedVarScopes(
                        ((ActivityHolder)startFrom).getActivity(), scopeList);
            } else if (startFrom instanceof CompositeActivity) {
                ExtendableActivity[] actArr =
                        ((CompositeActivity)startFrom).getActivities();
                for (ExtendableActivity act : actArr) {
                    addNestedVarScopes(act, scopeList);
                }
            }
        }
        
        /**
         * Obtains the list of variables accessible from the specifid 
         * variable declaration scope. 
         * 
         * @param vdScope
         * @return
         */
        public static List<VariableDeclaration> getVarDeclInScope(
                VariableDeclarationScope vdScope) {
            List<VariableDeclaration> result = new ArrayList<VariableDeclaration>();
            //
            // Create variable nodes
            if (vdScope instanceof BaseScope) {
                VariableContainer vc = ((BaseScope)vdScope).getVariableContainer();
                if (vc != null) {
                    Variable[] varArr = vc.getVariables();
                    for (Variable var : varArr) {
                        result.add(var);
                    }
                }
            } else if (vdScope instanceof VariableDeclaration) {
                VariableDeclaration varDecl = (VariableDeclaration)vdScope;
                String varName = varDecl.getVariableName();
                if (varName != null && varName.length() != 0) {
                    Object varTypeRef = null;
                    varTypeRef = varDecl.getMessageType();
                    if (varTypeRef == null) {
                        varTypeRef = varDecl.getType();
                        if (varTypeRef == null) {
                            varTypeRef = varDecl.getElement();
                        }
                    }
                    if (varTypeRef != null) {
                        // Ignore variable declaration without a name or type
                        result.add(varDecl);
                    }
                }
            } else if (vdScope instanceof VariableContainer) {
                VariableContainer vc = (VariableContainer)vdScope;
                Variable[] varArr = vc.getVariables();
                for (Variable var : varArr) {
                    result.add(var);
                }
            }
            //
            return result;
        }
        
        /**
         * Traverses the specified visibility scope and collects all 
         * overridden variables.
         * @param visScope to travrse over
         * @return the set of overridden variables
         */
        public static Set<VariableDeclaration> getAllOverridenVariables(
                VisibilityScope visScope) {
            HashSet<VariableDeclaration> result = new HashSet<VariableDeclaration>();
            HashSet<String> visibleNames = new HashSet<String>();
            //
            List<VariableDeclarationScope> vdScopeList = visScope.getVarScopeChain();
            for (int index = vdScopeList.size() - 1; index >=0; index--) {
                VariableDeclarationScope vdScope = vdScopeList.get(index);
                List<VariableDeclaration> varList = getVarDeclInScope(vdScope);
                for (VariableDeclaration varDecl : varList) {
                    String varName = varDecl.getVariableName();
                    //
                    if (visibleNames.contains(varName)) {
                        result.add(varDecl);
                    } else {
                        visibleNames.add(varName);
                    }
                }
            }
            //
            return result;
        }

    }
    
    /**
     * This class is intended for temporary holding variable search results.
     */
    public static final class VisibleVariables {
        private HashSet<VariableDeclaration> mAllVisibleVariables;
        private HashSet<VariableDeclaration> mAllOverridenVariables;

        public VisibleVariables() {
            mAllVisibleVariables = new HashSet<VariableDeclaration>();
            mAllOverridenVariables = new HashSet<VariableDeclaration>();
        }
        
        public Set<VariableDeclaration> getAllVisibleVariables() {
            return mAllVisibleVariables;
        }

        public Set<VariableDeclaration> getAllOverridenVariables() {
            return mAllOverridenVariables;
        }
    }


}
