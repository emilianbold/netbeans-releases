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
package org.netbeans.modules.bpel.model.impl;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.support.ExpressionUpdater;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;

/**
 * This service is responsible for updating references
 * in expression that represented by String.
 * It's like ReferenceIntegrityService but 
 * have absolutely other implementation because of nature of
 * dependent objects. In ReferenceIntegrity service dependent
 * objects have referecenes . Here no references exist.
 * So we need to update expression in other way. 
 * @author ads
 *
 */
public class ExpressionConsistencyService extends InnerEventDispatcherAdapter {

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        BpelEntity entity = event.getParent();
        if ( entity instanceof BpelEntityImpl
                && event instanceof PropertyUpdateEvent
                 )
        {
            if ( !(( BpelEntityImpl )entity).isInTree()) {
                // do not perform refactoring for element that is not in tree.
                return false;
            }
            return (!entity.getBpelModel().inSync())
                    && entity instanceof BpelReferenceable  
                    && NamedElement.NAME.equals(event.getName());
        }
        return false;
    }
    
    public void preDispatch( ChangeEvent event ) {
        // need to use preDispatch because AFTER change component will have new name.   
        BpelEntity entity = ((PropertyUpdateEvent)event).getParent();
        String newName = (String)((PropertyUpdateEvent)event).getNewValue();
        if ( ! (entity instanceof NamedElement ) || newName == null ) {
            return;
        }
        
        Process process = entity.getBpelModel().getProcess();
        Map<ContentElement,String> map = 
            new IdentityHashMap<ContentElement,String>();
        myMap.get().put( event , map );
        collectExpressions( process , (NamedElement)entity , newName , map ) ;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        BpelEntity entity = ((PropertyUpdateEvent)event).getParent();
        
        Process process = entity.getBpelModel().getProcess();
        update( process , myMap.get().get( event) ) ;
        
        reset( event );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#reset(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void reset( ChangeEvent event ) {
        Map<ChangeEvent,Map<ContentElement,String>> map = myMap.get();
        map.remove(event);
    }
    
    private void collectExpressions( BpelEntity target , NamedElement changed , 
            String newName , Map<ContentElement, String> map )
    {
        if ( target instanceof ContentElement ){
            String expr = ((ContentElement)target).getContent();
            String essentialExpr = expr.trim();

            String newExpr = ExpressionUpdater.getInstance().update( essentialExpr , changed , newName );
            if ( map != null  && newExpr!= null ){
                int index = expr.indexOf( essentialExpr );
                String leadingWhitespaces = expr.substring( 0 , index );
                String trailingWhitespaces = expr.substring( index + 
                        essentialExpr.length() );
                newExpr = leadingWhitespaces + newExpr +trailingWhitespaces;
                map.put( (ContentElement)target, newExpr );
            }
        }
        List<BpelEntity> list = target.getChildren();
        for (BpelEntity child : list) {
            collectExpressions( child , changed , newName , map );
        }
    }
    
    private void update( BpelEntity target, Map<ContentElement, String > map ){
        if ( map == null ){
            return;
        }
        String newExpression = map.remove( target );
        if ( newExpression != null ){
            try {
                ((ContentElement)target).setContent( newExpression );
            }
            catch (VetoException e) {
                // if inner dispatchers allow to set new name for referenceable component then
                // other inner dispatchers should allow set new expression here
                // otherwise first inner dispatchers works incorrectly.
                assert false;
            }
        }
        
        List<BpelEntity> list = target.getChildren();
        for (BpelEntity entity : list) {
            update( entity , map );
        }
    }

    private ThreadLocal<Map<ChangeEvent,Map<ContentElement,String>>> myMap =
        new ThreadLocal<Map<ChangeEvent,Map<ContentElement,String>>>(){
        
        // Fix for #80104
        @Override
        protected Map<ChangeEvent,Map<ContentElement,String>> initialValue(){
            return new WeakHashMap<ChangeEvent,Map<ContentElement,String>>();
        }
    };
}
