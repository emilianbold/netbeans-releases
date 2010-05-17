/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package com.sun.jsfcl.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import javax.faces.lifecycle.LifecycleFactory;

/**
 * <p>ViewHandler implementation that allows events to be triggered upon the
 * occurrence of specific ViewHandler method calls.  This implementation also
 * posts relevant lifecycle events to initialized page beans, so it also
 * implements <code>PhaseListener</code>.</p>
 *
 * <p>FIXME - may need to provide an implementation of Lifecycle as well,
 * in order to ensure that afterRenderResponse works even in the face of
 * exceptions thrown by application code.</p>
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
        register();

    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The ViewHandler instance to which we delegate operations.</p>
     */
    private ViewHandler handler = null;


    /**
     * <p>The {@link PageBeanMapper} used to identify the page bean that
     * corresponds to a view identifier.  This instance is lazily instantiated,
     * so use <code>getPageBeanMapper()</code> to acquire a reference.</p>
     */
    private PageBeanMapper mapper = null;


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The request scope attribute under which we store the view id
     * when <code>createView()</code> is called.</p>
     */
    public static final String CREATED_VIEW =
      "com.sun.jsfcl.app.CREATED_VIEW"; //NOI18N


    /**
     * <p>Request attribute key under which the {@link AbstractPageBean}
     * for the view that will actually be rendered (if any) is stored.</p>
     */
    private static final String PAGE_BEAN_RENDERED =
      "com.sun.jsfcl.app.PAGE_BEAN_RENDERED"; //NOI18N


    /**
     * <p>Request attribute key under which a <code>List</code> of the
     * {@link AbstractPageBean}s that have been initialized for the current
     * request are stored.  Typically, there will be either one or two
     * page beans on this list, depending on whether page navigation has
     * taken place or not.</p>
     */
    private static final String PAGE_BEANS_INITIALIZED =
      "com.sun.jsfcl.app.PAGE_BEANS_INITIALIZED"; //NO18N


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

	UIViewRoot viewRoot = handler.createView(context, viewId);
	context.getExternalContext().getRequestMap().put(CREATED_VIEW, viewId);
        setupPageBean(context, viewRoot, false);
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

	handler.renderView(context, viewRoot);

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

	UIViewRoot viewRoot = handler.restoreView(context, viewId);
        setupPageBean(context, viewRoot, true);
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
        // System.out.println("beforePhase(" + phaseId + "," + context + ")");
        if (PhaseId.RENDER_RESPONSE.equals(phaseId)) {
            beforeRenderResponse(context);
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
        // System.out.println("afterPhase(" + phaseId + "," + context + ")");
        if (PhaseId.RESTORE_VIEW.equals(phaseId)) {
            afterRestoreView(context);
        } else if (PhaseId.RENDER_RESPONSE.equals(phaseId) ||
                   context.getResponseComplete()) {
            destroy(context);
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * <p>Call the <code>preprocess()</code> method on any page bean that
     * will be performing post back processing.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private void afterRestoreView(FacesContext context) {

        Map map = context.getExternalContext().getRequestMap();
        List list = (List) map.get(PAGE_BEANS_INITIALIZED);
        if (list == null) {
            return;
        }
        Iterator beans = list.iterator();
        while (beans.hasNext()) {
            AbstractPageBean bean = (AbstractPageBean) beans.next();
            if (bean.isPostBack()) {
                // CR 6255669 - Log and swallow thrown RuntimeException
                try {
                    bean.preprocess();
                } catch (RuntimeException e) {
                    context.getExternalContext().log(e.getMessage(), e);
                }
            }
        }

    }


    /**
     * <p>Call the <code>prerender()</code> method on any page bean that
     * will be performing rendering.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private void beforeRenderResponse(FacesContext context) {

        Map map = context.getExternalContext().getRequestMap();
        AbstractPageBean pageBean =
          (AbstractPageBean) map.get(PAGE_BEAN_RENDERED);
        if (pageBean == null) {
            return;
        }
        // CR 6255669 - Log and swallow any thrown RuntimeException
        try {
            pageBean.prerender();
        } catch (RuntimeException e) {
            context.getExternalContext().log(e.getMessage(), e);
        }
        map.remove(PAGE_BEAN_RENDERED);

    }


    /**
     * <p>Call the <code>destroy()</code> method for any page bean that we
     * have called <code>init()</code> for.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private void destroy(FacesContext context) {

        Map map = context.getExternalContext().getRequestMap();
        List list = (List) map.get(PAGE_BEANS_INITIALIZED);
        if (list == null) {
            return;
        }
        Iterator beans = list.iterator();
        while (beans.hasNext()) {
            AbstractPageBean bean = (AbstractPageBean) beans.next();
            // CR 6255669 - Log and swallow any thrown RuntimeException
            try {
                bean.destroy();
            } catch (RuntimeException e) {
                context.getExternalContext().log(e.getMessage(), e);
            }
        }
        map.remove(PAGE_BEANS_INITIALIZED);

    }



    /**
     * <p>Return the {@link PageBeanMapper} we will use to map view identifiers
     * to managed bean names of the corresponding page beans, instantiating a
     * new instance if necessary.  <strong>FIXME</strong> - make the actual
     * implementation class to be used configurable.</p>
     */
    private PageBeanMapper getPageBeanMapper() {

        if (mapper == null) {
            mapper = new PageBeanMapperImpl();
        }
        return mapper;

    }


    /**
     * <p>Register this instance as a <code>PhaseListener</code> with the
     * <code>Lifecycle</code> instance for this web application.</p>
     */
    private void register() {

        String lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE; // FIXME - override?
        LifecycleFactory factory = (LifecycleFactory)
          FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        factory.getLifecycle(lifecycleId).addPhaseListener(this);

    }


    /**
     * <p>Create and initialize an appropriate {@link AbstractPageBean}
     * associated with the specified view, which was just created or restored.
     *
     * @param context <code>FacesContext</code> for the current request
     * @param view <code>UIViewRoot</code> just created or restored
     *  (or <code>null</code> if there was no such view)
     * @param postBack <code>true</code> if this is a post back to
     *  an existing view
     */
    private void setupPageBean(FacesContext context, UIViewRoot view,
                               boolean postBack) {

        // Is there actually a view for us to process?
        if (view == null) {
            return;
        }

        // Map the view identifier to the corresponding page bean name
        String viewId = view.getViewId();
        // System.out.println("setupPageBean(" + viewId + "," + postBack + ")");
        String viewName = getPageBeanMapper().mapViewId(viewId);
        if (viewName == null) {
            // System.out.println("  WARNING: no page bean for " + viewId);
            return;
        }
        // System.out.println("  Mapped to page bean " + viewName);

        // Retrieve or create a corresponding page bean instance
        ValueBinding vb =
          context.getApplication().createValueBinding("#{" + viewName + "}"); //NOI18N
        AbstractPageBean pageBean = null;
        try {
            pageBean = (AbstractPageBean) vb.getValue(context);
        } catch (ClassCastException e) {
            // System.out.println("  WARNING: Bean for " + viewId + " is not a page bean");
            return;
        }
        if (pageBean == null) {
            // System.out.println("  WARNING: No page bean for " + viewId);
            return;
        }

        // Configure this instance, and schedule it for later event processing
        // CR 6255669 - Log and swallow any thrown RuntimeException
        try {
            pageBean.init();
        } catch (RuntimeException e) {
            context.getExternalContext().log(e.getMessage(), e);
        }
        Map map = context.getExternalContext().getRequestMap();
        map.put(PAGE_BEAN_RENDERED, pageBean);
        List list = (List) map.get(PAGE_BEANS_INITIALIZED);
        if (list == null) {
            list = new ArrayList(2);
            map.put(PAGE_BEANS_INITIALIZED, list);
        }
        list.add(pageBean);

    }


}
