/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.jsfcl.app;

import java.util.Iterator;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.Lifecycle;

/**
 * <p><strong>AbstractPageBean</strong> is the abstract base class for every
 * page bean associated with a JSP page containing JavaServer Faces
 * components.  It extends {@link FacesBean}, so it inherits all of the
 * default behavior found there.</p>
 *
 * <p>In addition to event handler methods that you create while building
 * your application, the runtime environment will also call the following
 * <em>lifecycle</em> related methods at appropriate points during the execution
 * of your application:</p>
 * <ul>
 * <li><strong>init()</strong> - Called whenever you navigate to the
 *     corresponding JSP page, either directly (via a URL) or indirectly
 *     via page navigation from a different page.  You can override this
 *     method to acquire any resources that will always be needed by this
 *     page.</li>
 * <li><strong>preprocess()</strong> - If this request is a postback (i.e.
 *     your page is about to process an incoming form submit, this method
 *     will be called after the original component tree has been restored,
 *     but before any standard JavaServer Faces event processing is done
 *     (i.e. this is called before <em>Apply Request Values</em> phase of
 *     the request processing lifecycle).  Override this method to acquire
 *     any resources that will be needed by event handlers (such as action
 *     methods, validator methods, or value change listener methods) that
 *     will be executed while executing the request processing lifecycle.</li>
 * <li><strong>prerender()</strong> - If this page is the one that will be
 *     rendering the response, this method is called <em>after</em> any event
 *     processing, but before the actual rendering.  Override this method to
 *     acquire resources that are only needed when a page is being rendered.
 *     </li>
 * <li><strong>destroy()</strong> - Called unconditionally if
 *     <code>init()</code> was called, after completion of rendering by
 *     whichever page was actually rendered.  Override this method to release
 *     any resources allocated in the <code>init()</code>,
 *     <code>preprocess()</code>, or <code>prerender()</code>
 *     methods (or in an event handler).</li>
 * </ul>
 */
public abstract class AbstractPageBean extends FacesBean {


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The <code>FacesContext</code> instance for the request this
     * <code>AbstractPageBean</code> is associated with.</p>
     */
    private FacesContext context = null;


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Register this bean as a <code>PhaseListener</code> so that it can
     * participate in the request processing lifecycle of each request.</p>
     */
    public AbstractPageBean() {
        
        // Register to receive phase events ourselves
        Lifecycle lifecycle = getLifecycle();
        lifecycle.addPhaseListener(this);

        // Remember which FacesContext we are processing so that
        // we can filter out irrelevant events later
        context = FacesContext.getCurrentInstance();

    }


    // --------------------------------------------------- PhaseListener Methods


    /**
     * <p>If this event is for the request associated with this
     * page bean, call through to the appropriate "before" lifecycle
     * method for this page bean, and notify interested session bean
     * and application bean instances as well.</p>
     *
     * @param event <code>PhaseEvent</code> to be processed
     */
    public void beforePhase(PhaseEvent event) {

        // Does this event apply to this page bean instance?
        if (context != FacesContext.getCurrentInstance()) {
            return; // This is not our request
        }

        // Call through to the appropriate handler on this page bean
        super.beforePhase(event);

        // Call through to any session beans in our associated session
        Map map = event.getFacesContext().getExternalContext().getSessionMap();
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object value = map.get(key);
            if (value instanceof AbstractSessionBean) {
                ((AbstractSessionBean) value).beforePhase(event);
            }
        }

        // Call through to any application bean
        map = event.getFacesContext().getExternalContext().getApplicationMap();
        keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object value = map.get(key);
            if (value instanceof AbstractApplicationBean) {
                ((AbstractApplicationBean) value).beforePhase(event);
            }
        }

    }


    /**
     * <p>If this event is for the request associated with this
     * page bean, call through to the appropriate "after" lifecycle
     * method for this page bean, and notify interested session bean
     * and application bean instances as well.  Then, if this is
     * Render Response phase, deregister ourselves as a listener.</p>
     *
     * @param event <code>PhaseEvent</code> to be processed
     */
    public void afterPhase(PhaseEvent event) {

        // Does this event apply to this page bean instance?
        if (context != FacesContext.getCurrentInstance()) {
            return; // This is not our request
        }

        // Call through to the appropriate handler on this page bean
        super.afterPhase(event);

        // Call through to any session beans in our associated session
        Map map = event.getFacesContext().getExternalContext().getSessionMap();
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object value = map.get(key);
            if (value instanceof AbstractSessionBean) {
                ((AbstractSessionBean) value).afterPhase(event);
            }
        }

        // Call through to any application bean
        map = event.getFacesContext().getExternalContext().getApplicationMap();
        keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object value = map.get(key);
            if (value instanceof AbstractApplicationBean) {
                ((AbstractApplicationBean) value).afterPhase(event);
            }
        }

        // AFter render response phase, deregister ourselves as a listener
        if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
            getLifecycle().removePhaseListener(this);
            context = null; // We do not need our reference any longer
        }

    }


    // ------------------------------------------------------- Lifecycle Methods


    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Override this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.  Note that this method
     * is called <strong>before</strong> the component tree has been
     * restored, so you do not have access to any information from the
     * JavaServer Faces components on this page.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    protected void init() {
        ;
    }


    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a "post back" request that
     * is processing a form submit.  Override this method to allocate
     * resources that will be required in your event handlers.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    protected void preprocess() {
        ;
    }


    /**
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a post back and then navigated to a different page).  Override
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    protected void prerender() {
        ;
    }


    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called, regardless of whether
     * or not this was the page that was actually rendered.  Override this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    protected void destroy() {
        ;
    }


    // --------------------------------------------------------- Private Methods

}
