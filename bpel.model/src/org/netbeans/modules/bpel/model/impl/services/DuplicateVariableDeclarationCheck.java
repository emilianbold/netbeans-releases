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
package org.netbeans.modules.bpel.model.impl.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.ContainerIterator;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads
 * This service checks for unique name for varaible declaration.
 * It differs from UniqueNameCheck in the way that UniqueNameCheck
 * is generic Parent-Children check. But this is special 
 * variable declaration check for various nonstandart situations.
 * One of situations here: OnEvent that implicitly declare varaibles in its child-scope.  
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class DuplicateVariableDeclarationCheck extends InnerEventDispatcherAdapter {

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        if (event instanceof PropertyUpdateEvent) {
            PropertyUpdateEvent ev = (PropertyUpdateEvent) event;
            if ( event.getParent().getBpelModel().inSync() ){
                return false;
            }
            if ( ev.getNewValue() ==  null ){
                return false;
            }
            
            return event.getParent() instanceof VariableDeclaration && 
                isApplicable( (VariableDeclaration)event.getParent() , 
                        ev.getName()); 
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void preDispatch( ChangeEvent event ) throws VetoException {
        assert event instanceof PropertyUpdateEvent;
        Object newValue = ((PropertyUpdateEvent)event).getNewValue();
        
        if ( newValue == null ){
            return;
        }
        
        VariableDeclarationChecker checker = 
            LazyInit.CHECKERS.get( event.getParent().getElementType() );
        if (!checker.check((VariableDeclaration) event.getParent(), newValue)) {
            throw new VetoException(Utils.getResourceString(checker
                    .getError((VariableDeclaration) event.getParent()), event
                    .getName()), event);
        }
    }
    
    private boolean isApplicable( VariableDeclaration decl , String name ){
        /* 
         * Check of variable name change in Variable ( or VariableDeclaration )
         * in any VariableDeclararionScope respectively of existance
         * some VariableDeclaration will be too expensive. So we just check
         * only rules for any specific VariableDeclarations ( not 
         * Variable itself ). 
         * 
         * if ( decl instanceof Variable && NamedElement.NAME.equals( name )){
            return true;
        }*/
        
        VariableDeclarationChecker checker = 
            LazyInit.CHECKERS.get( decl.getElementType() );
        if ( checker != null && checker.isApplicable( decl , name )){
            return true;
        }
        
        return false;
    }

    private static class LazyInit {
        private static final Map<Class<? extends VariableDeclaration>,
            VariableDeclarationChecker> CHECKERS = new HashMap<
                Class<? extends VariableDeclaration>,
                VariableDeclarationChecker>();
        
        static {
            CHECKERS.put( ForEach.class , new ForEachVariableChecker() );
            CHECKERS.put( OnEvent.class , new OnEventVariableChecker() );
            CHECKERS.put( FromPart.class , new FromPartVariableChecker() );
            CHECKERS.put( Variable.class , new ScopeInsideOnEventChecker() );
        }
    }
}

interface  VariableDeclarationChecker {
    boolean isApplicable( VariableDeclaration decl , String name );
    String getError( VariableDeclaration decl );
    boolean check( VariableDeclaration decl , Object value );
}

class ForEachVariableChecker implements VariableDeclarationChecker {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#isApplicable(org.netbeans.modules.bpel.debugger.spi.plugin.def.VariableDeclaration, java.lang.String)
     */
    public boolean isApplicable( VariableDeclaration decl, String name ) {
        return decl instanceof ForEach && ForEach.COUNTER_NAME.equals( name );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#getError()
     */
    public String getError(VariableDeclaration decl) {
        assert decl instanceof ForEach;
        return Utils.BAD_VARIABLE_FOR_FOR_EACH;

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#check(org.netbeans.modules.bpel.debugger.spi.plugin.def.VariableDeclaration)
     */
    public boolean check( VariableDeclaration decl,  Object newValue ) {
        if ( decl instanceof ForEach ){
            ContainerIterator<VariableDeclarationScope> iterator = 
                new ContainerIterator<VariableDeclarationScope>(decl , 
                        VariableDeclarationScope.class);
            while ( iterator.hasNext() ){
                VariableDeclarationScope next = iterator.next();
                if ( next == decl ){
                    continue;
                }
                if ( !checkVariableDeclarationScope( next , newValue ) ){
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean checkVariableDeclarationScope(VariableDeclarationScope scope,
            Object value )
    {
        if (scope instanceof VariableDeclaration
                && value.equals(((VariableDeclaration) scope).getVariableName()))
        {
            return false;
        }
        if (scope instanceof BaseScope) {
            VariableContainer container = ((BaseScope) scope)
                    .getVariableContainer();
            if (container != null) {
                Variable[] variables = container.getVariables();
                for (Variable variable : variables) {
                    if ( value.equals( variable.getName()) ){
                        return false;
                    }
                }
            }
        }
        List<VariableDeclaration> children = 
            scope.getChildren(VariableDeclaration.class);
        for (VariableDeclaration declaration : children) {
            if ( value.equals( declaration.getVariableName()) ){
                return false;
            }
        }
        return true;
    }
    
}

abstract class ImplicitVariableCheck implements VariableDeclarationChecker {
    
    protected boolean check( OnEvent onEvent , Object value ){
        Scope scope = onEvent.getScope();
        if ( scope == null ){
            return true;
        }
        VariableContainer container = scope.getVariableContainer();
        if ( container == null ){
            return true;
        }
        Variable[] variables = container.getVariables();
        for (Variable variable : variables) {
            if ( value.equals( variable.getName()) ){
                return false;
            }
        }
        return true;
    }
    
    protected boolean check (OnEvent onEvent , VariableDeclaration decl, 
            Object value )
    {
        FromPartContainer partContainer = onEvent.getFromPartContaner();
        if ( partContainer== null) {
            return true;
        }
        for (FromPart part : partContainer.getFromParts()) {
            if ( part == decl ){
                continue;
            }
            String name = part.getVariableName();
            if ( value.equals( name ) ){
                return false;
            }
        }
        
        return true;
    }
}

class OnEventVariableChecker extends ImplicitVariableCheck {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#isApplicable(org.netbeans.modules.bpel.debugger.spi.plugin.def.VariableDeclaration, java.lang.String)
     */
    public boolean isApplicable( VariableDeclaration decl, String name ) {
        return decl instanceof OnEvent && OnEvent.VARIABLE.equals( name );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#getError()
     */
    public String getError(VariableDeclaration decl) {
        assert decl instanceof OnEvent;
        return Utils.BAD_VARIABLE_FOR_ON_EVENT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#check(org.netbeans.modules.bpel.debugger.spi.plugin.def.VariableDeclaration)
     */
    public boolean check( VariableDeclaration decl,  Object newValue ) {
        assert decl instanceof OnEvent;
        return check( (OnEvent) decl, newValue );
    }
}

class FromPartVariableChecker extends ImplicitVariableCheck {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#isApplicable(org.netbeans.modules.bpel.debugger.spi.plugin.def.VariableDeclaration, java.lang.String)
     */
    public boolean isApplicable( VariableDeclaration decl, String name ) {
        return decl instanceof FromPart &&
            decl.getParent() instanceof OnEvent &&
            FromPart.TO_VARIABLE.equals( name );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#getError()
     */
    public String getError(VariableDeclaration decl) {
        assert decl instanceof FromPart;
        return Utils.BAD_VARIABLE_FOR_ON_EVENT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#check(org.netbeans.modules.bpel.debugger.spi.plugin.def.VariableDeclaration)
     */
    public boolean check( VariableDeclaration decl,  Object newValue ) {
        assert decl instanceof FromPart;
        
        BpelContainer parent = decl.getParent();
        if ( !(parent instanceof OnEvent) ){
            return true;
        }
         
        return check( (OnEvent) parent , newValue ) && 
            check( (OnEvent) parent , decl , newValue);
    }
}

class ScopeInsideOnEventChecker extends ImplicitVariableCheck {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#isApplicable(org.netbeans.modules.bpel.model.api.VariableDeclaration, java.lang.String)
     */
    public boolean isApplicable( VariableDeclaration decl, String name ) {
        return Variable.NAME.equals(name) && 
            decl instanceof Variable &&  
            decl.getParent() instanceof VariableContainer &&
            decl.getParent().getParent() instanceof Scope &&
            decl.getParent().getParent().getParent() instanceof OnEvent;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#getError(org.netbeans.modules.bpel.model.api.VariableDeclaration)
     */
    public String getError( VariableDeclaration decl ) {
        return Utils.BAD_VARIABLE_FOR_SCOPE_IN_ON_EVENT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.services.VariableDeclarationChecker#check(org.netbeans.modules.bpel.model.api.VariableDeclaration, java.lang.Object)
     */
    public boolean check( VariableDeclaration decl, Object value ) {
        assert decl instanceof Variable;
        
        BpelContainer parent = decl.getParent();
        if ( parent instanceof VariableContainer && 
                parent.getParent() instanceof Scope && 
                parent.getParent().getParent() instanceof OnEvent )
        {
            parent = parent.getParent().getParent();
            boolean ret = !value.equals( ((OnEvent) parent).getVariableName() ); 
            return ret && check( (OnEvent) parent , value ) && 
                check( (OnEvent) parent , decl , value);
        }
        return true;
    }
}
