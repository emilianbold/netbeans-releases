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

/**
 *
 */
package org.netbeans.modules.bpel.model.xam.spi;

import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * This interface could be implemented by various inner services for model. F.e.
 * it could be used for setting UIDs for entities after creation, copying, cut.
 * May be used for saving information from one entity ( with children ) for
 * setting it to another entity ( after cut f.e. ). Any such service should
 * implement this interface and put as service in META-INF. It will be loaded
 * and each OM modification will be handled by those services.
 * 
 * Please note that each class that implements this interface will be one
 * for IDE, not one for each BPEL model. So you should care about race conditions
 * in implementation. Because any model when it call methods of this interface
 * will be locked exclusively. But this lock is per model. Not global lock.
 * So there is possibility to change something from other thread from other model
 * impl in the same instance of impl.
 * This means that you should care about saving "context" information 
 * when method is called. You need either not hold any context at all or
 * hold this context in ThreadLocal variables.    
 * 
 * @author ads
 */
public interface InnerEventDispatcher {

    /**
     * This method called for checking either we need to call this visitor for
     * <code>event</code>.
     * 
     * @param event
     *            Fired event.
     * @return Is applicable this visitor for <code>event</code>.
     */
    boolean isApplicable( ChangeEvent event );
    
    /**
     * This method will be called before action on model will be performed. This
     * method could throw VetoException. It should not throw any exception on
     * events about accessing to children ( setting/adding/removing child in
     * parent ). This is because OM doesn't have methods that could be
     * incorrectly used. Only setting incorrect attribute value could throw such
     * exception.
     * 
     * @param event
     *            Event that will be fired after OM will be changed. It is not
     *            yet happened.
     * @throws VetoException {@link VetoException}
     *             Could be thrown if event is rejected by visitor.
     */
    void preDispatch( ChangeEvent event ) throws VetoException;

    /**
     * This method will be called after action on model was performed. It could
     * perform additional changes in OM based on event information. F.e. it
     * could change name of attribute that reference to some entity by name and
     * this name was changed.
     * 
     * 
     * @param event
     *            Event that fired by OM after change was performed.
     */
    void postDispatch( ChangeEvent event );
    
    /**
     * This method is called when some exception is detected in 
     * one of dispatchers. Then all dispatchers that collect 
     * some information on preDispatch stage can clear this information. 
     * 
     * Suggested use - when one of inner dispatchers throws VetoException
     * then all diaspatchers need to clean internal state, because
     * postDispatch will never be called. 
     * @param event Event that fired by OM after change was performed.
     */
    void reset( ChangeEvent event );
}
