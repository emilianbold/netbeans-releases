/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.jsfcl.app;

import java.util.Iterator;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

/**
 * <p><strong>FacesBean</strong> is the abstract base class for all page beans,
 * session scope data beans, and application scope data beans that wish to
 * participate in the request processing lifecycle.  Concrete subclasses of
 * this class will typically be registered as managed beans, so that they get
 * created on demand (and added to the relevant scope's attributes).</p>
 */
public abstract class FacesBean implements PhaseListener {

    // ------------------------------------------------------------- Constructor

    public FacesBean() {
    }

    // --------------------------------------------------- Convenience Accessors

    /**
     * <p>Return the <code>Application</code> instance for the current
     * web application.</p>
     */
    protected Application getApplication() {
        return FacesContext.getCurrentInstance().getApplication();
    }

    /**
     * <p>Return a <code>Map</code> of the application scope attributes
     * for this web application.</p>
     */
    protected Map getApplicationMap() {
        return getExternalContext().getApplicationMap();
    }

    /**
     * <p>Return the <code>FacesContext</code> instance for the current
     * request.  This method has been restored for backwards compatibilty.</p>
     */
    protected FacesContext getContext() {
        return getFacesContext();
    }

    /**
     * <p>Return the <code>ExternalContext</code> instance for the
     * current request.</p>
     */
    protected ExternalContext getExternalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    /**
     * <p>Return the <code>FacesContext</code> instance for the current
     * request.</p>
     */
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * <p>Return the configured <code>Lifecycle</code> instance for the
     * current web application.</p>
     */
    protected Lifecycle getLifecycle() {
        String lifecycleId =
            getExternalContext().getInitParameter("javax.faces.LIFECYCLE_ID");  //NOI18N
        if (lifecycleId == null || lifecycleId.length() == 0) {
            lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
        }
        LifecycleFactory lifecycleFactory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        return lifecycleFactory.getLifecycle(lifecycleId);
    }

    /**
     * <p>Return a <code>Map</code> of the request scope attributes for
     * the current request.</p>
     */
    protected Map getRequestMap() {
        return getExternalContext().getRequestMap();
    }

    /**
     * <p>Return a <code>Map</code> of the session scope attributes for the
     * current user's session.  Note that calling this method will cause a
     * session to be created if there is not already one associated with
     * this request.</p>
     */
    protected Map getSessionMap() {
        return getExternalContext().getSessionMap();
    }

    // ------------------------------------------------------- Bean Manipulation

    /**
     * <p>Return any attribute stored in request scope, session scope, or
     * application scope under the specified name.  If no such
     * attribute is found, and if this name is the registered name of a
     * managed bean, cause a new instance of this managed bean to be created
     * (and stored in an appropriate scope, if necessary) and returned.
     * If no attribute exists, and no managed bean was created, return
     * <code>null</code>.</p>
     *
     * @param name Name of the attribute to be retrieved
     */
    protected Object getBean(String name) {
        return getApplication().getVariableResolver().resolveVariable(getFacesContext(), name);
    }

    /**
     * <p>Replace the value of any attribute stored in request scope,
     * session scope, or application scope under the specified name.  If there
     * is no such attribute, create a new request scope attribute under this
     * name, and store the value there.</p>
     */
    protected void setBean(String name, Object value) {
        setValue("#{" + name + "}", value); //NOI18N
    }

    // ------------------------------------------------------ Value Manipulation

    /**
     * <p>Evaluate the specified value binding expression, and return
     * the value that it points at.</p>
     *
     * @param expr Value binding expression (including delimiters)
     */
    protected Object getValue(String expr) {
        ValueBinding vb = getApplication().createValueBinding(expr);
        return (vb.getValue(getFacesContext()));
    }

    /**
     * <p>Evaluate the specified value binding expression, and update
     * the value that it points at.</p>
     *
     * @param expr Value binding expression (including delimiters) that
     *  must point at a writeable property
     * @param value New value for the property pointed at by <code>expr</code>
     */
    protected void setValue(String expr, Object value) {
        ValueBinding vb = getApplication().createValueBinding(expr);
        vb.setValue(getFacesContext(), value);
    }

    // ----------------------------------------------------------- PhaseListener

    /**
     * <p>Call through to the "before" lifecycle callback method
     * for the current phase.</p>
     *
     * @param phaseEvent <code>PhaseEvent</code> to be processed
     */
    public void beforePhase(PhaseEvent phaseEvent) {
        PhaseId phaseId = phaseEvent.getPhaseId();
        if (PhaseId.RESTORE_VIEW.equals(phaseId)) {
            beforeRestoreView();
        } else if (PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)) {
            beforeApplyRequestValues();
        } else if (PhaseId.PROCESS_VALIDATIONS.equals(phaseId)) {
            beforeProcessValidations();
        } else if (PhaseId.UPDATE_MODEL_VALUES.equals(phaseId)) {
            beforeUpdateModelValues();
        } else if (PhaseId.INVOKE_APPLICATION.equals(phaseId)) {
            beforeInvokeApplication();
        } else if (PhaseId.RENDER_RESPONSE.equals(phaseId)) {
            beforeRenderResponse();
        }
    }

    /**
     * <p>Call through to the "after" lifecycle callback method
     * for the current phase.</p>
     *
     * @param phaseEvent <code>PhaseEvent</code> to be processed
     */
    public void afterPhase(PhaseEvent phaseEvent) {
        PhaseId phaseId = phaseEvent.getPhaseId();
        if (PhaseId.RESTORE_VIEW.equals(phaseId)) {
            afterRestoreView();
        } else if (PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)) {
            afterApplyRequestValues();
        } else if (PhaseId.PROCESS_VALIDATIONS.equals(phaseId)) {
            afterProcessValidations();
        } else if (PhaseId.UPDATE_MODEL_VALUES.equals(phaseId)) {
            afterUpdateModelValues();
        } else if (PhaseId.INVOKE_APPLICATION.equals(phaseId)) {
            afterInvokeApplication();
        } else if (PhaseId.RENDER_RESPONSE.equals(phaseId)) {
            afterRenderResponse();
        }
    }

    /**
     * <p>Return <code>PhaseId.ANY_PHASE</code> to indicate that we are
     * interested in all phases.</p>
     */
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    // ----------------------------------------------------- Lifecycle Callbacks

    // These methods are called by beforePhase() and afterPhase() as appropriate
    // and allow subclasses to perform additional tasks at the corresponding
    // moment in the request processing lifecycle for each request.  The default
    // implementations do nothing.

    protected void beforeRestoreView() {}
    protected void afterRestoreView() {}
    protected void beforeApplyRequestValues() {}
    protected void afterApplyRequestValues() {}
    protected void beforeProcessValidations() {}
    protected void afterProcessValidations() {}
    protected void beforeUpdateModelValues() {}
    protected void afterUpdateModelValues() {}
    protected void beforeInvokeApplication() {}
    protected void afterInvokeApplication() {}
    protected void beforeRenderResponse() {}
    protected void afterRenderResponse() {}

    // ------------------------------------------------------- Phase Processing

    /**
     * <p>Return <code>true</code> if the current request was a post back
     * for an existing view, rather than the creation of a new view.  The
     * result of this method may be used to conditionally execute one time
     * setup that is only required when a page is first displayed.</p>
     */
    protected boolean isPostBack() {

	return getExternalContext().getRequestMap().get(ViewHandlerImpl.CREATED_VIEW) == null;

    }


    /**
     * <p>Skip any remaining request processing lifecycle phases for the
     * current request, and go immediately to <em>Render Response</em>
     * phase.  This method is typically invoked when you want to throw
     * away input values provided by the user, instead of processing them.</p>
     */
    protected void renderResponse() {
        getFacesContext().renderResponse();
    }


    // -------------------------------------------------- Component Manipulation


    /**
     * <p>Erase previously submitted values for all input components on this
     * page.  This method <strong>MUST</strong> be called if you have bound
     * input components to database columns, and then arbitrarily navigate
     * the underlying <code>RowSet</code> to a different row in an event
     * handler method.</p>
     */
    protected void erase() {
        erase(getFacesContext().getViewRoot());
    }


    /**
     * <p>Private helper method for <code>erase()</code> that recursively
     * descends the component tree and performs the required processing.</p>
     *
     * @param component The component to be erased
     */
    private void erase(UIComponent component) {
        // Erase the component itself (if needed)
        if (component instanceof EditableValueHolder) {
            ((EditableValueHolder) component).setSubmittedValue(null);
        }
        // Process the facets and children of this component
        Iterator kids = component.getFacetsAndChildren();
        while (kids.hasNext()) {
            erase((UIComponent) kids.next());
        }
    }


    // ------------------------------------------------------------- Log Methods

    /**
     * <p>Log the specified message to the container's log file.</p>
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        getExternalContext().log(message);
    }

    /**
     * <p>Log the specified message and exception to the container's
     * log file.</p>
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    protected void log(String message, Throwable throwable) {
        getExternalContext().log(message, throwable);
    }

    // --------------------------------------------------------- Message Methods


    /**
     * <p>Enqueue a global <code>FacesMessage</code> (not associated
     * with any particular componen) containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_INFO</code>.
     * </p>
     *
     * @param summary Summary text for this message
     */
    protected void info(String summary) {
        getFacesContext().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    /**
     * <p>Enqueue a <code>FacesMessage</code> associated with the
     * specified component, containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_INFO</code>.
     * </p>
     *
     * @param component Component with which this message is associated
     * @param summary Summary text for this message
     */
    protected void info(UIComponent component, String summary) {
        getFacesContext().addMessage(component.getClientId(getFacesContext()),
          new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    /**
     * <p>Enqueue a global <code>FacesMessage</code> (not associated
     * with any particular componen) containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_WARN</code>.
     * </p>
     *
     * @param summary Summary text for this message
     */
    protected void warn(String summary) {
        getFacesContext().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_WARN, summary, null));
    }

    /**
     * <p>Enqueue a <code>FacesMessage</code> associated with the
     * specified component, containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_WARN</code>.
     * </p>
     *
     * @param component Component with which this message is associated
     * @param summary Summary text for this message
     */
    protected void warn(UIComponent component, String summary) {
        getFacesContext().addMessage(component.getClientId(getFacesContext()),
          new FacesMessage(FacesMessage.SEVERITY_WARN, summary, null));
    }

    /**
     * <p>Enqueue a global <code>FacesMessage</code> (not associated
     * with any particular componen) containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_ERROR</code>.
     * </p>
     *
     * @param summary Summary text for this message
     */
    protected void error(String summary) {
        getFacesContext().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, null));
    }

    /**
     * <p>Enqueue a <code>FacesMessage</code> associated with the
     * specified component, containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_ERROR</code>.
     * </p>
     *
     * @param component Component with which this message is associated
     * @param summary Summary text for this message
     */
    protected void error(UIComponent component, String summary) {
        getFacesContext().addMessage(component.getClientId(getFacesContext()),
          new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, null));
    }

    /**
     * <p>Enqueue a global <code>FacesMessage</code> (not associated
     * with any particular componen) containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_FATAL</code>.
     * </p>
     *
     * @param summary Summary text for this message
     */
    protected void fatal(String summary) {
        getFacesContext().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_FATAL, summary, null));
    }

    /**
     * <p>Enqueue a <code>FacesMessage</code> associated with the
     * specified component, containing the specified summary text
     * and a message severity level of <code>FacesMessage.SEVERITY_FATAL</code>.
     * </p>
     *
     * @param component Component with which this message is associated
     * @param summary Summary text for this message
     */
    protected void fatal(UIComponent component, String summary) {
        getFacesContext().addMessage(component.getClientId(getFacesContext()),
          new FacesMessage(FacesMessage.SEVERITY_FATAL, summary, null));
    }
}
