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
package org.netbeans.modules.vmd.api.model;

/**
 * This presenter is designed to allow receiving notification about document changes.
 * <p>
 * When a presenter is really attached to a component, then notifyAttached method is called to notify it. Therefore
 * perform all initialization inside that method. Similarly notifyDetached is called when a presenter is dettached
 * from a component.
 * <p>
 * When a design is changed, designChanged method is called on a presenter. The getEventFilter method is called once
 * after notifyAttached method is called, for resolving the filter that is used for the design-changed listening.
 * If the presenter changes internally (based on design-changed or other way), it has to call firePresenterChanged method
 * to notify the model about it.
 * <p>
 * It is possible to use have a dependencies between presenters e.g. a presenter requires additional data
 * from other presenter for its own work. For that purpose there are addDependency and removeDependency methods that could
 * be called from notifyAttached and notifyDetached. When a presenter is no longer used, that all its dependencies are
 * automatically removed.
 * <p>
 * When at least one of the dependent presenters (those that this presenter has a dependency on), is changed, presenterChanged
 * method is called. At the time of the method call, presenterChanged methods on all the dependent presenters are called
 * (this means all dependent presenters are resolved).
 * When a presenter fires a presenter-changed event, the presenterChanged method is called automatically called too.
 *
 * @author David Kaspar
 */
public abstract class DynamicPresenter extends Presenter {

    // TODO - right now a dependency could be set only in notifyAttached method - later there could be a need for doing it in designChanged and presenterChanged methods too.

    private Listener listener; // once it is set to non-null value, it must not be set to null, because removeDependency/autoRemoveAllDependecies will not be working.
    private State state;
//    private long lastUpdateEventID;

    /**
     * Creates a new dynamic presenter
     */
    protected DynamicPresenter () {
        state = State.DISABLED;
//        lastUpdateEventID = Long.MIN_VALUE;
    }

    final PresenterListener getPresenterListener () {
        return listener;
    }

    // NOTE - if you modify this method, look at Presenter.setNotifyAttached method too
    final void setNotifyAttached (DesignComponent component) {
        super.setNotifyAttached (component);

        state = State.ADDING_DEPENDENCIES;
        notifyAttached (component);
        state = State.DISABLED;
        DesignEventFilter filter = getEventFilter ();
        if (filter != null) {
            if (listener == null)
                listener = new Listener ();
            component.getDocument ().getListenerManager ().addDesignListener (listener, filter);
        }
    }

    // NOTE - if you modify this method, look at Presenter.setNotifyDetached method too
    final void setNotifyDetached (DesignComponent component) {
        super.setNotifyDetached (component);

        if (listener != null)
            component.getDocument ().getListenerManager ().removeDesignListener (listener);
        state = State.REMOVING_DEPENDENCIES;
        notifyDetached (component);
        state = State.DISABLED;
    }

    /**
     * This method is called when the presenter is attached to a component. Do the initialization and set dependencies.
     * @param component the component
     */
    protected abstract void notifyAttached (DesignComponent component);

    /**
     * This method is called when the presenter is dettached from a component. The presenter will be no longer used.
     * Do the finalization and unset dependencies. Unsetting dependencies is optional because it is done automatically.
     * @param component the component
     */
    protected abstract void notifyDetached (DesignComponent component);

    /**
     * This method is called to obtain a event filter for calling designChanged callback method of this presenter.
     * <p/>
     * Note: This method is called after notifyAttached method is performed and it is called only once.
     * The filter class is mutable and could be changed during the presenter life-time.
     * @return the filter
     */
    protected abstract DesignEventFilter getEventFilter ();

//    /**
//     * Checks and sets a flag that a specific event was processed. Call this method in updateSelf method to check
//     * whether the presenter has to update its data from a model or whether it is already done.
//     * @param event the event
//     * @return true if the event was alreay processed
//     */
//    protected final boolean checkSetEvent (DesignEvent event) {
//        long eventID = event.getEventID ();
//        if (eventID <= lastUpdateEventID)
//            return true;
//        lastUpdateEventID = eventID;
//        return false;
//    }

    /**
     * This method is called when a design is changed according to the event filter.
     * The presenter should update its data that could be resolved from the model directly without communication with other presenters.
     * When data are change, firePresenterChanged method should be called to notify others about it
     * (this is required for correct work of presenter dependencies).
     * @param event the design-changed event
     */
    protected abstract void designChanged (DesignEvent event);

    /**
     * This method is called when at least one of dependent presenters are changed. At the time of the method call,
     * presenterChanged methods on all the dependent presenters are called
     * (this means all dependent presenters are resolved their data).
     * @param event the presenter event
     */
    protected abstract void presenterChanged (PresenterEvent event);

    /**
     * This method should be called for notifying others that the presenter changed its data.
     * <p/>
     * Note: This method could be called from designChanged and presenterChanged method only.
     */
    protected final void firePresenterChanged () {
        DesignComponent component = getComponent ();
        assert component != null;
        DesignDocument document = component.getDocument ();
        assert document != null;
        assert document.getTransactionManager ().isWriteAccess ();
        assert state == State.FIRING_PRESENTER_CHANGED;
        document.getListenerManager ().firePresenterChanged (this);
    }

    /**
     * Adds dependency of the presenter on one that is registered on a specified component under a specified presenter class.
     * The presenter instance is resolved dynamically. Therefore when a component changes its presenter instances,
     * this dependency is still working.
     * <p/>
     * Note: This method could be called from notifyAttached method only.
     * @param component      the component
     * @param presenterClass the presenter class
     */
    protected final void addDependency (DesignComponent component, Class<? extends Presenter> presenterClass) {
        assert state == State.ADDING_DEPENDENCIES;
        if (listener == null)
            listener = new Listener ();
        component.getDocument ().getListenerManager ().addPresenterListener (component, presenterClass, listener);
    }

    /**
     * Removes dependency of the presenter on one that is registered on a specified component under a specified presenter class.
     * <p/>
     * Note: This method could be called from notifyDetached method only.
     * @param component      the component
     * @param presenterClass the presenter class
     */
    protected final void removeDependency (DesignComponent component, Class<? extends Presenter> presenterClass) {
        assert state == State.REMOVING_DEPENDENCIES;
        if (listener != null)
            component.getDocument ().getListenerManager ().removePresenterListener (component, presenterClass, listener);
    }

    private class Listener implements DesignListener, PresenterListener {

        public void designChanged (DesignEvent event) {
            DynamicPresenter.this.state = State.FIRING_PRESENTER_CHANGED;
            DynamicPresenter.this.designChanged (event);
            DynamicPresenter.this.state = State.DISABLED;
        }

        public void presenterChanged (PresenterEvent event) {
            DynamicPresenter.this.state = State.FIRING_PRESENTER_CHANGED;
            DynamicPresenter.this.presenterChanged (event);
            DynamicPresenter.this.state = State.DISABLED;
        }

        public String toString () {
            return DynamicPresenter.this.toString ();
        }

    }

    private enum State {

        DISABLED, FIRING_PRESENTER_CHANGED, ADDING_DEPENDENCIES, REMOVING_DEPENDENCIES

    }

}
