/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;

/**
 * Interface describing an abstract model. The model is based on a
 * document representation that represents the persistent form.
 * @author Chris Webster
 * @author Nam Nguyen
 * @author Rico Cruz
 */
public interface Model<T extends Component<T>> {
    
    /**
     * Add coarse-grained change listener for events on model components.
     */
    public void removeComponentListener(ComponentListener cl);

    /**
     * Remove component event listener.
     */
    public void addComponentListener(ComponentListener cl);

    /**
     * Add fine-grained property change listener for events on model components.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /**
     * Remove property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl);

    void removeUndoableEditListener(UndoableEditListener uel);
    void addUndoableEditListener(UndoableEditListener uel);

    /**
     * make the current memory model consistent with the underlying
     * representation, typically a swing document. 
     */
    void sync() throws java.io.IOException;
    
    /**
     * return true if sync is being performed. 
     */
    boolean inSync();
    
    enum State {
        VALID, NOT_WELL_FORMED
    }
    /**
     * @return the last known state of the document. This method is affected
     * by invocations of #sync().
     */
    State getState();
    
    /**
     * @return true if model is in middle of transformation tranasction.
     */
    boolean isIntransaction();
    
    /** 
     * This method will block until a transaction can be started. A transaction
     * in this context will fire events (such as property change) when 
     * #endTransaction() has been invoked. A transaction must be 
     * be acquired during a mutation, reading can be performed without
     * a transaction. Only a single transaction at a time is supported. Mutations
     * which occur based on events will not be reflected until the transaction
     * has completed.
     */
    void startTransaction();
    
    /**
     * This method stops the transaction and causes all events to be fired. 
     * After all events have been fired, the document representation will be 
     * modified to reflect the current value of the model (flush). 
     */
    void endTransaction() throws IOException;
    
    /**
     * Returns model root component.
     */
    T getRootComponent();
    
    /**
     * Add child component at specified index.
     * @param target the parent component.
     * @param child the child component to be added.
     * @param index position among same type of child components, or -1 if not relevant.
     */
    void addChildComponent(Component target, Component child, int index);
    
    /**
     * Remove specified component from model.
     */
    void removeChildComponent(Component child);
}
