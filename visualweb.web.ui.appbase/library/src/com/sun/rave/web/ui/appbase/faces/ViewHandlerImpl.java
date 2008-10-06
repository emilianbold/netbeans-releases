/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package com.sun.rave.web.ui.appbase.faces;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.appbase.AbstractRequestBean;
import com.sun.rave.web.ui.appbase.ApplicationException;
import com.sun.rave.web.ui.appbase.servlet.LifecycleListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;

/**
 * <p>ViewHandler implementation that allows events to be triggered upon the
 * occurrence of specific ViewHandler method calls.  This implementation also
 * posts relevant lifecycle events to initialized page beans, so it also
 * implements <code>PhaseListener</code>.</p>
 */

public class ViewHandlerImpl extends ViewHandler implements PhaseListener {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new {@link ViewHandlerImpl} that delegates to the
     * specified <code>ViewHandler</code> instance.</p>
     *
     * @param handler The ViewHandler instance to which we will delegate
     */
    public ViewHandlerImpl(ViewHandler handler) {

	this.handler = handler;

    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The ViewHandler instance to which we delegate operations.</p>
     */
    private ViewHandler handler = null;


    /**
     * <p>The cached <code>Lifecycle</code> instance for this application.</p>
     */
    private Lifecycle lifecycle = null;


    /**
     * <p>The {@link PageBeanMapper} used to identify the page bean that
     * corresponds to a view identifier.  This instance is lazily instantiated,
     * so use <code>pageBeanMapper()</code> to acquire a reference.</p>
     */
    private PageBeanMapper mapper = null;


    /**
     * <p>Flag indicating whether we have been registered as a phase
     * listener with the application <code>Lifecycle</code> instance
     * yet.  This registration needs to be performed lazily, rather
     * than in our constructor, in case the <code>Lifecycle</code>
     * instance is replaced by a customized version (as will occur when
     * using the JSF-Portlet Bridge).</p>
     */
    private boolean registered = false;


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>Request attribute key under which a <code>List</code> of any
     * <code>Exception</code>s thrown by a page bean event handler,
     * and then logged and swallowed, will be cached.  Application
     * logic can check for such exceptions (perhaps during the
     * <code>destroy()</code> method), to invoke application specific
     * error processing.</p>
     */
    public static final String CACHED_EXCEPTIONS =
      "com.sun.rave.web.ui.appbase.CACHED_EXCEPTIONS";


    /**
     * <p>The <code>UIViewRoot</code> attribute under which we store
     * <code>Boolean.TRUE</code> when <code>createView()</code> is
     * called.  This can be used by the <code>isPostBack()</code>
     * method to determine whether this view was restored (no such
     * attribute present) and a postback is happening, or whether
     * this view was created (no postback is happening).</p>
     */
    public static final String CREATED_VIEW =
      "com.sun.rave.web.ui.appbase.CREATED_VIEW"; //NOI18N


    /**
     * <p>Request attribute key under which a <code>List</code> of the
     * {@link AbstractPageBean}s that have been created for the current
     * request are stored.  Typically, there will be either one or two
     * page beans on this list, depending on whether page navigation has
     * taken place or not, but will be more if/when static or dynamic
     * includes are used.</p>
     */
    public static final String PAGE_BEANS_CREATED =
      "com.sun.rave.web.ui.appbase.PAGE_BEANS_CREATED"; //NOI18N


    // ---------------------------------------------------------- Public Methods


    // ----------------------------------------------------- ViewHandler Methods


    /**
     * <p>Return an appropriate <code>Locale</code> to use for this
     * and subsequent requests for the current client.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     *
     * @exception NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    public Locale calculateLocale(FacesContext context) {

	Locale locale = handler.calculateLocale(context);
	return locale;

    }


    /**
     * <p>Return an appropriate <code>RenderKit</code> identifier
     * for this and subsequent requests from the current
     * client.
     *
     * @param context <code>FacesContext</code> for the current request
     *
     * @exception NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    public String calculateRenderKitId(FacesContext context) {

	String renderKitId = handler.calculateRenderKitId(context);
	return renderKitId;

    }


    /**
     * <p>Create and return a new <code>UIViewRoot</code> instance
     * initialized with information from this <code>FacesContext</code>
     * for the specified <code>viewId</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param viewId View identifier of the view to be created
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>viewId</code> is <code>null</code>
     */
    public UIViewRoot createView(FacesContext context, String viewId) {

        register();
	UIViewRoot viewRoot = handler.createView(context, viewId);
        viewRoot.getAttributes().put(CREATED_VIEW, Boolean.TRUE);
	return viewRoot;

    }


    /**
     * <p>Return a URL suitable for rendering that selects the
     * specified view identifier.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param viewId View identifier of the desired view
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>viewId</code> is <code>null</code>
     */
    public String getActionURL(FacesContext context, String viewId) {

	String url = handler.getActionURL(context, viewId);
	return url;

    }


    /**
     * <p>Return a URL suitable for rendering that selects the
     * specified resource.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param path Context-relative resource path to reference
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>path</code> is <code>null</code>
     */
    public String getResourceURL(FacesContext context, String path) {

	String url = handler.getResourceURL(context, path);
	return url;

    }


    /**
     * <p>Perform the necessary actions to render the specified view
     * as part of the current response.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param viewRoot View to be rendered
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>viewRoot</code> is <code>null</code>
     */
    public void renderView(FacesContext context, UIViewRoot viewRoot)
      throws IOException, FacesException {

        // Set up our page bean, if this has not yet been done
        register();
        int count = recordedCount(context);
        AbstractPageBean pageBean = pageBean(context);
        if (pageBean != null) {

            // If our page bean was just now created, that means we were
            // called from Render Response phase.  Therefore, we'll
            // fake a "before Render Response" event for symmetry with the
            // fact that an "after Render Response" event is going to get
            // fired later on
            if (recordedCount(context) > count) {
                try {
                    pageBean.beforePhase
                      (new PhaseEvent(context, PhaseId.RENDER_RESPONSE, lifecycle()));
                } catch (RuntimeException e) {
                    context.getExternalContext().log(e.getMessage(), e);
                    cache(context, e);
                }
            }

            // Fire the prerender() callback event
            prerender(context, pageBean);

        }

        // If we have cached any exceptions already, call cleanup()
        // (which will also cause an ApplicationException wrapping them
        // to be thrown).
        if (cached(context) != null) {
            cleanup(context);
            return;
        }

        // Render the specified view, calling cleanup() if any exception
        // is thrown (which will also cause an ApplicationException
        // wrapping it to be thrown).
        try {
            if (!context.getResponseComplete()) {
                handler.renderView(context, viewRoot);
            }
        } catch (RuntimeException e) {
            context.getExternalContext().log(e.getMessage(),e);
            cache(context, e);
            cleanup(context);
        }

    }


    /**
     * <p>Perform necessary actions to restore the specified view
     * and return a corresponding <code>UIViewRoot</code>.  If there
     * is no view information to be restored, return <code>null</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param viewId View identifier of the view to be restored
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>viewId</code> is <code>null</code>
     */
    public UIViewRoot restoreView(FacesContext context, String viewId) {

        register();
	UIViewRoot viewRoot = handler.restoreView(context, viewId);
        
         /* mbohm (6451472): when the view root is
         * restored in the RESTORE_VIEW phase, its attributes from the 
         * previous request are preserved. This will include the CREATED_VIEW
         * view root attribute. That is causing isPostBack always to think
         * that the view root was just created (so isPostBack always returns
         * false). So be sure to clean out the CREATED_VIEW view root attribute
         * here.
        */
        if (viewRoot != null) {
            viewRoot.getAttributes().remove(CREATED_VIEW);
        }
	return viewRoot;

    }


    /**
     * <p>Take appropriate action to save the current state information.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    public void writeState(FacesContext context) throws IOException {

	handler.writeState(context);

    }


    // -------------------------------------------------- PhaseListener Methods


    /**
     * <p>Return <code>PhaseId.ANY_PHASE</code> because we are interested
     * in all phase events.</p>
     */
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }


    /**
     * <p>Process the specified <em>before phase</em> event.</p>
     *
     * @param event <code>PhaseEvent</code> to be processed
     */
    public void beforePhase(PhaseEvent event) {

        PhaseId phaseId = event.getPhaseId();
        FacesContext context = event.getFacesContext();

        // Ripple this event through to all the page beans that have been
        // initialized for this request and call beforePhase()
        List list = (List) context.getExternalContext().getRequestMap().get(PAGE_BEANS_CREATED);
        if (list != null) {
            Iterator pageBeans = list.iterator();
            while (pageBeans.hasNext()) {
                AbstractPageBean pageBean = (AbstractPageBean) pageBeans.next();
                try {
                    pageBean.beforePhase(event);
                } catch (RuntimeException e) {
                    context.getExternalContext().log(e.getMessage(), e);
                    cache(context, e);
                }
            }
        }

        // Broadcast application level events as required
        if (PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)) {
            // This is the page that will be processing the form submit,
            // so tell the page bean by calling preprocess() on it
            preprocess(context);
        }

    }


    /**
     * <p>Process the specified <em>after phase</em> event.</p>
     *
     * @param event <code>PhaseEvent</code> to be processed
     */
    public void afterPhase(PhaseEvent event) {

        PhaseId phaseId = event.getPhaseId();
        FacesContext context = event.getFacesContext();

        // Ripple this event through to all the page beans that have been
        // initialized for this request and call afterPhase()
        List list = (List) context.getExternalContext().getRequestMap().get(PAGE_BEANS_CREATED);
        if (list != null) {
            Iterator pageBeans = list.iterator();
            while (pageBeans.hasNext()) {
                AbstractPageBean pageBean = (AbstractPageBean) pageBeans.next();
                try {
                    pageBean.afterPhase(event);
                } catch (RuntimeException e) {
                    context.getExternalContext().log(e.getMessage(), e);
                    cache(context, e);
                }
            }
        }

        // In a portlet environment, the "action" and "render"
        // parts of the lifecycle appear as two different requests.
        // Therefore, clean up the page that processed the current
        // form submit (if any)
        if (!(context.getExternalContext().getContext() instanceof ServletContext)) {
            if (PhaseId.INVOKE_APPLICATION.equals(phaseId) ||
                context.getRenderResponse() ||
                context.getResponseComplete()) {
                cleanup(context);
                return;
            }
        }

        // Broadcast application level events as required
        if (PhaseId.RENDER_RESPONSE.equals(phaseId) ||
                   context.getResponseComplete()) {
            // Unconditionally clean up after rendering is completed
            cleanup(context);
        }

    }


    // -------------------------------------------------------- Package Methods


    /**
     * <p>Cache the specified exception in a request scope attribute
     * that application logic can use to invoke error processing.
     * All such cached exceptions will be available in the <code>List</code>
     * used to maintain the cache.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param exception Exception to be cached
     */
    public static void cache(FacesContext context, Exception exception) {

        // Is there an active FacesContext?  There will not be if a lifecycle
        // event was fired on a non-Faces request
        if (context == null) {
            return;
        }

        // Add this exception to the list of exceptions for this request
        Map map = context.getExternalContext().getRequestMap();
        List list = (List) map.get(CACHED_EXCEPTIONS);
        if (list == null) {
            list = new LinkedList();
            map.put(CACHED_EXCEPTIONS, list);
        }
        list.add(exception);

    }
    

    /**
     * <p>Record the specified {@link AbstractPageBean} on the list of
     * page beans that have been , and therefore need to have their
     * beforePhase() and afterPhase() methods called at appropriate times.</p>
     *
     * @param context <code>FacesContext</code> for this request
     * @param bean Page bean to be added to the list
     */
    public static void record(FacesContext context, AbstractPageBean bean) {

        if (context == null) {
            return;
        }

        Map map = context.getExternalContext().getRequestMap();
        List list = (List) map.get(PAGE_BEANS_CREATED);
        if (list == null) {
            list = new LinkedList();
            map.put(PAGE_BEANS_CREATED, list);
        }
        list.add(bean);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * <p>Return a <code>List</code> of cached exceptions associated with
     * this request, if any.  If there were no such exceptions, return
     * <code>null</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private List cached(FacesContext context) {

        Map map = context.getExternalContext().getRequestMap();
        return (List) map.get(CACHED_EXCEPTIONS);

    }


    /**
     * <p>Cause any application model request scope beans (instances of
     * {@link AbstractFragmentBean}, {@link AbstractPageBean}, and
     * {@link AbstractRequestBean}) to be removed from request scope.
     * A side effect of this will be to cause {@link LifecycleListener}
     * to fire <code>destroy()</code> methods on them.</p>
     *
     * <p>Then, if we have cached any exceptions associated with this request,
     * throw an {@link ApplicationException} that wraps the list.  If this occurs,
     * the first cached exception will be considered the root cause.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private void cleanup(FacesContext context) {

        // Acquire a list of request scope attribute keys to be processed
        List list = new ArrayList();
        Map map = context.getExternalContext().getRequestMap();
        map.remove(PAGE_BEANS_CREATED);
        Iterator entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object value = entry.getValue();
            if (value != null) {
                if ((value instanceof AbstractFragmentBean) ||
                    (value instanceof AbstractPageBean) ||
                    (value instanceof AbstractRequestBean)) {
                    list.add(entry.getKey());
                }
            }
        }

        // Cause the selected attributes to be removed from request scope,
        // which will trigger calls to their destroy() methods
        Iterator keys = list.iterator();
        while (keys.hasNext()) {
            map.remove(keys.next());
        }
        
        // If we cached any exceptions, wrap them in an ApplicationException
        // and throw it
        List exceptions = cached(context);
        if ((exceptions != null) && (exceptions.size() > 0)) {
            /* mbohm (144650): after throwing this ApplicationException
             * and forwarding to a resource specified in the <error-page>
             * element of web.xml, we will arrive here again and throw another
             * ApplicationException during the attempt to present that resource
             * (and thus fail to present it) if the CACHED_EXCEPTIONS entry
             * still exists. So remove the CACHED_EXCEPTIONS entry before 
             * throwing this ApplicationException.
             */
            map.remove(CACHED_EXCEPTIONS);
            throw new ApplicationException
                        ((Exception) exceptions.get(0), exceptions);
        }

    }


    /**
     * <p>Return the <code>Lifecycle</code> instance for this application,
     * caching it the first time it is retrieved.</p>
     */
    private Lifecycle lifecycle() {

        if (lifecycle == null) {
            String lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE; // FIXME - override?
            LifecycleFactory factory = (LifecycleFactory)
              FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            lifecycle = factory.getLifecycle(lifecycleId);
        }
        return lifecycle;

    }


    /**
     * <p>Log the specified message via <code>FacesContext</code> if it is
     * not null, or directly to the container otherwise.</p>
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.getExternalContext().log(message);
        } else {
            System.out.println(message);
        }

    }


    /**
     * <p>Log the specified message and exception via <code>FacesContext</code>
     * if it is not null, or directly to the container otherwise.</p>
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    private void log(String message, Throwable throwable) {

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.getExternalContext().log(message);
        } else {
            System.out.println(message);
        }

    }


    /**
     * <p>Return the {@link AbstractPageBean} instance related to the
     * current request (if any).  Otherwise, return <code>null</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private AbstractPageBean pageBean(FacesContext context) {

        // Identify the current view (if any)
        UIViewRoot view = context.getViewRoot();
        if (view == null) {
            return null;
        }

        // Map the view identifier to the corresponding page bean name
        String viewId = view.getViewId();
        if (view == null) {
            return null;
        }

        // Return the relevant page bean (if any)
        return pageBean(context, viewId);

    }


    /**
     * <p>Return the {@link AbstractPageBean} instance related to the
     * current request (if any).  Otherwise, return <code>null</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param viewId View identifier used to select the page bean
     */
    private AbstractPageBean pageBean(FacesContext context, String viewId) {

        // Identify the managed bean name of the corresponding page bean
        String viewName = pageBeanMapper().mapViewId(viewId);
        if (viewName == null) {
            return null;
        }

        // Retrieve or create a corresponding page bean instance
        ValueBinding vb =
          context.getApplication().createValueBinding("#{" + viewName + "}"); //NOI18N
        AbstractPageBean pageBean = null;
        try {
            pageBean = (AbstractPageBean) vb.getValue(context);
        } catch (ClassCastException e) {
            // System.out.println("  WARNING: Bean for " + viewId + " is not a page bean");
        }
        return pageBean;

    }


    /**
     * <p>Return the {@link PageBeanMapper} we will use to map view identifiers
     * to managed bean names of the corresponding page beans, instantiating a
     * new instance if necessary.  <strong>FIXME</strong> - make the actual
     * implementation class to be used configurable.</p>
     */
    private PageBeanMapper pageBeanMapper() {

        if (mapper == null) {
            mapper = new PageBeanMapperImpl();
        }
        return mapper;

    }


    /**
     * <p>Call the <code>preprocess()</code> method on the page bean
     * associated with this request (if any).</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private void preprocess(FacesContext context) {

        preprocess(context, pageBean(context));

    }


    /**
     * <p>Call the <code>preprocess()</code> method on the page bean
     * that is associated with the specified view identifier (if any).</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param viewId View identifier of the selected view
     */
    private void preprocess(FacesContext context, String viewId) {

        preprocess(context, pageBean(context, viewId));

    }


    /**
     * <p>Call the <code>preprocess()</code> method on the specified
     * page bean associated with this request.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param pageBean {@link AbstractPageBean{ for the current view
     */
    private void preprocess(FacesContext context, AbstractPageBean pageBean) {

        if (pageBean == null) {
            return;
        }
        
        // CR 6255669 - Log and swallow any thrown RuntimeException
        try {
            pageBean.preprocess();
        } catch (RuntimeException e) {
            context.getExternalContext().log(e.getMessage(), e);
            cache(context, e);
        }

    }


    /**
     * <p>Call the <code>prerender()</code> method on the page bean
     * associated with this request (if any).</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private void prerender(FacesContext context) {

        prerender(context, pageBean(context));

    }


    /**
     * <p>Call the <code>prerender()</code> method on the specified
     * page bean associated with this request.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param pageBean {@link AbstractPageBean{ for the current view
     */
    private void prerender(FacesContext context, AbstractPageBean pageBean) {

        if (pageBean == null) {
            return;
        }
        
        // CR 6255669 - Log and swallow any thrown RuntimeException
        try {
            if (!context.getResponseComplete()) {
                pageBean.prerender();
            }
        } catch (RuntimeException e) {
            context.getExternalContext().log(e.getMessage(), e);
            cache(context, e);
        }

    }


    /**
     * <p>Return a <code>List</code> of cached page beans that have
     * been created, and therefore need to have their beforePhase()
     * and afterPhase() methods called at appropriate times.  If there were
     * no such beans, return <code>null</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private List recorded(FacesContext context) {

        if (context != null) {
            Map map = context.getExternalContext().getRequestMap();
            return (List) map.get(PAGE_BEANS_CREATED);
        } else {
            return null;
        }

    }


    /**
     * <p>Return the number of page beans that have been created for
     * this request.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private int recordedCount(FacesContext context) {

        if (context != null) {
            Map map = context.getExternalContext().getRequestMap();
            List list = (List) map.get(PAGE_BEANS_CREATED);
            if (list != null) {
                return list.size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }

    }


    /**
     * <p>Register this instance as a <code>PhaseListener</code> with the
     * <code>Lifecycle</code> instance for this web application, if we
     * have not already done so.</p>
     */
    private void register() {

        if (registered) {
            return;
        }
        lifecycle().addPhaseListener(this);
        registered = true;

    }


}
