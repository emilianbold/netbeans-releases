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

package org.netbeans.modules.bpel.model.api;

import java.util.concurrent.Callable;

import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.xml.xam.Model;

/**
 * This interface is start point for BPEL model access. It contain root element -
 * process that correspond to process in BPEL file. One should keep reference to
 * this interface instead of keeping reference to process element, because
 * reference to process element could mutate ( it is up to implementation ).
 * 
 * @author ads
 */
public interface BpelModel extends Model<BpelEntity> {
    
    /**
     * This is pseudo property for event that fired when state of OM is changed.
     */
    String STATE = "<state>";           // NOI18N

    /**
     * @return reference to root of BPEL - process.
     */
    Process getProcess();

    /**
     * @return builder for BPEL elements.
     */
    BPELElementsBuilder getBuilder();

    /**
     * Returns entity by its unique id.
     * 
     * @param id
     *            id of entity.
     * @return entity by its id.
     */
    BpelEntity getEntity( UniqueId id );
    
    /**
     * Accessor to root bpel process element that has different
     * version from currently supported.
     * Please note that this is only read-only element.
     * You should not try to change it in any way.
     * This method will return each time new instance of such 
     * root element. This method will return null if 
     * bpel process has correct namespace or not well formed ( in terms of XML ).
     * 
     * @return OM root element that correspond to BPEL process with 
     * different version if any. 
     */
    AnotherVersionBpelProcess getAnotherVersionProcess();

    /**
     * 
     */
    boolean canExtend(ExtensibleElements extensible,
            Class<? extends ExtensionEntity> extensionType);
    
    /**
     * Add change listener which will receive events for any element in the
     * underlying model.
     * 
     * Listener adds to model as weak reference.
     * So one needs to care about keeping reference to listener somehere
     * till it used and need to get events from model.
     * If one will use anonymous class for adding then it will never get
     * events.
     * You should use method removePropertyChangeListener when listener is already not 
     * needed. For using this method you should keep reference to created listener.
     * If you cannot use method removePropertyChangeListener then probably 
     * you will never get events from model because you don't keep 
     * reference to listener and it could be collected by GC in any time.  
     * 
     * @param listener
     *            listener for add.
     */
    void addEntityChangeListener( ChangeEventListener listener );

    /**
     * Removes change listener from model.
     * 
     * @param listener
     *            listener for remove.
     */
    void removeEntityChangeListener( ChangeEventListener listener );

    /**
     * This method should be used for executing group of calls to model as
     * atomic action. Placing <code>action</code> in this method guarantee
     * that model will not be affected via another threads
     * in process of execution this action.
     * 
     * Changes in model that represented by <code>action</code>
     * will be executed synchronously. It means method will end
     * only after all calls to model inside <code>action</code>
     * would be executed. 
     * 
     * All model methods getXXX, setXX, addXX, etc. also atomic.
     * If you need just get value or set new value in model you don't
     * need to call this method. Each this action will be synchronized.
     * You need to use this method when you need to perform many actions
     * one depends from another. In this case value that you get in one action
     * could be not valid for next action with model. 
     * 
     * @param <V> type for return value.
     * @param action group of calls to model.
     * @param source this is object that will be set as source
     * for events that will be fired by model as result of this action.
     * It could be used for distinguishing various consumers of model.
     * Could be equall to null. If it equals to null then source of
     * event will be set to Thread.currentThread(). 
     * 
     * One should not put "sync" method inside <code>invoke()</code>
     * or in transaction that started with <code>startTransaction()</code>.
     * Otherwise "sync" will no have any effect. OM consider
     * starting transaction as starting mutation and
     * in this case "sync" doesn't have sense.
     *  
     * @return return value from action.
     * @throws Exception {@link Exception} exception that could be thrown by <code>action</code>
     */
    <V> V invoke( Callable<V> action, Object source ) throws Exception;
    
    /**
     * This method execute command <code>run</code> inside read lock on OM 
     * obtained. You should never try mutate OM inside this command <code>run</code>.
     * Otherwise you will get InvalidaStateException.
     * @param run command for execution
     */
    void invoke ( Runnable run );
    
    /**
     * Finds Bpel element on the specified position.
     * @param i Position in NbDocument.
     * @return Entity on the position if any.
     */
    BpelEntity findElement( int i );
    
    boolean isSupportedExpension(String uri);
    void rollbackTransaction();
}
