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

package com.sun.rave.web.ui.appbase;

import com.sun.rave.web.ui.appbase.faces.ViewHandlerImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * default integration behavior found there.</p>
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
 *
 * <p>For advanced users, and as a carry forward from previous versions of
 * Sun Java Studio Creator, page bean instances are also registered
 * automatically as a JavaServer Faces <code>PhaseListener</code>.  For each
 * lifecycle phase, there is an appropriate "before" and "after" event method,
 * which you can override to provide phase-specific behavior.  For example,
 * if you wanted to provide some special logic that happened before or after
 * the Update Model Values phase, you would override one of the methods:</p>
 * <pre>
 *   public void beforeUpdateModelValues();
 *   public void afterUpdateModelValues();
 * </pre>
 *
 */
public abstract class AbstractPageBean extends FacesBean // implements PhaseListener
{


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a new instance of this bean.</p>
     */
    public AbstractPageBean() {

        /* mbohm (6450530): This work is already being done when
         * the managed bean mechanism places this page bean
         * in request scope. Placing it in request scope causes
         * LifecycleListener.attributeAdded to be called, which causes
         * LifecycleListener.fireInit to be called, which causes
         * ViewHandlerImpl.record to be called, which appends this page bean
         * to the PAGE_BEANS_CREATED list in the request.
        // Register ourselves for phase events
        Map map = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        List list = (List) map.get(ViewHandlerImpl.PAGE_BEANS_CREATED);
        if (list == null) {
            list = new ArrayList(5);
            map.put(ViewHandlerImpl.PAGE_BEANS_CREATED, list);
        }
        list.add(this);
         **/

    }


    // --------------------------------------------------- PhaseListener Methods


    /**
     * <p>Call through to the appropriate "before event" method,
     * depending upon the <code>PhaseId</code> in this event.</p>
     *
     * @param event <code>PhaseEvent</code> to be processed
     */
    public void beforePhase(PhaseEvent event) {

        PhaseId phaseId = event.getPhaseId();
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
            if (!FacesContext.getCurrentInstance().getResponseComplete()) {
                beforeRenderResponse();
            }
        }

    }


    /**
     * <p>Call through to the appropriate "after event" method,
     * depending upon the <code>PhaseId</code> in this event.</p>
     *
     * @param event <code>PhaseEvent</code> to be processed
     */
    public void afterPhase(PhaseEvent event) {

        PhaseId phaseId = event.getPhaseId();
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
            if (!FacesContext.getCurrentInstance().getResponseComplete()) {
                afterRenderResponse();
            }
        }

    }


    // ---------------------------------------------------- PhaseEvent Callbacks


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

        UIViewRoot viewRoot = getFacesContext().getViewRoot();
        if (viewRoot == null) {
            return true;
        }
        return !Boolean.TRUE.equals(viewRoot.getAttributes().get(ViewHandlerImpl.CREATED_VIEW));

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


    /**
     * <p>Skip any remaining request processing lifecycle phases for the
     * current request, including <em>Render Response</em> phase.  This is
     * appropriate if you have completed the response through some means
     * other than JavaServer Faces rendering.</p>
     */
    protected void responseComplete() {

        getFacesContext().responseComplete();

    }


    // ------------------------------------------------------- Lifecycle Methods


    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Override this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    public void init() {
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
    public void preprocess() {
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
    public void prerender() {
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
    public void destroy() {
        ;
    }


}
