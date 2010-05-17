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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;


/**
 * <p><strong>FacesBean</strong> is the abstract base class for all page beans,
 * request scope data beans, session scope data beans, and application scope
 * data beans that wish to participate in the provided JavaServer Faces
 * integration support.  Concrete subclasses of this class will typically
 * be registered as managed beans, so that they get created on demand
 * (and added to the relevant scope's attributes).</p>
 *
 * <p><strong>NOTE</strong> - These integration methods will operate
 * successfully <em>only</em> within the lifetime of a JavaServer Faces
 * request.</p>
 */
public abstract class FacesBean {


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
     * <p>Return a <code>List</code> of the exceptions that have been
     * logged, swallowed, and cached during the processing of this request
     * so far.  If there are no such cached exceptions, return <code>null</code>
     * instead.</p>
     *
     * <p>The application runtime framework causes exceptions thrown from
     * any of the following sources to be logged (to the application server's
     * log), swallowed, and added to this list:</p>
     * <ul>
     * <li>Application lifecycle methods (<code>init()</code>, <code>preprocess()</code>,
     *     <code>prerender()</code>, and <code>destroy()</code>) on an
     *     {@link AbstractPageBean}.</li>
     * <li>Phase listener methods (<code>beforeXxx()</code>, <code>afterXxx()</code>)
     *     on an {@link AbstractPageBean}.</li>
     * <li>Exception thrown during the rendering of the selected view during
     *     <em>Render Response</em> phase of the request processing lifecycle.</li>
     * </ul>
     */
    protected List getCachedExceptions() {

        return (List) getFacesContext().getExternalContext().getRequestMap().
                      get(ViewHandlerImpl.CACHED_EXCEPTIONS);

    }


    /**
     * <p>Return the <code>FacesContext</code> instance for the current
     * request.  This method has been restored for backwards compatibilty.</p>
     *
     * @deprecated Use <code>getFacesContext()</code> instead
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


    // ----------------------------------------------- Save/Restore Data Methods


    /**
     * <p>The attribute name under which saved data will be stored on the
     * view root component.</p>
     */
    private static final String DATA_KEY = "com.sun.rave.web.ui.appbase.DATA";



    /**
     * <p>Return the data object stored (typically when the component tree
     * was previously rendered) under the specified key, if any; otherwise,
     * return <code>null</code>.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE:</strong> Data objects will become
     * available only after the <em>Restore View</em> phase of the request
     * processing lifecycle has been completed.  A common place to reinitialize
     * state information, then, would be in the <code>preprocess()</code>
     * event handler of a page bean.</p>
     *
     * @param key Key under which to retrieve the requested data
     */
    public Object retrieveData(String key) {

        FacesContext context = getFacesContext();
        if (context == null) {
            return null;
        }
        UIViewRoot view = context.getViewRoot();
        if (view == null) {
            return null;
        }
        Map map = (Map) view.getAttributes().get(DATA_KEY);
        if (map != null) {
            return map.get(key);
        } else {
            return null;
        }

    }


    /**
     * <p>Save the specified data object (which <strong>MUST</strong> be
     * <code>Serializable</code>) under the specified key, such that it can
     * be retrieved (via <code>getData()</code>) on a s subsequent request
     * immediately after the component tree has been restored.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE:</strong> In order to successfully save
     * data objects, this method must be called before the <em>Render Response</em>
     * phase of the request processing lifecycle is executed.  A common scenario
     * is to save state information in the <code>prerender()</code> event handler
     * of a page bean.</p>
     *
     * @param key Key under which to store the requested data
     * @param data Data object to be stored
     */
    public void saveData(String key, Object data) {

        Map map = (Map)
           getFacesContext().getViewRoot().getAttributes().get(DATA_KEY);
        if (map == null) {
            map = new HashMap();
            getFacesContext().getViewRoot().getAttributes().put(DATA_KEY, map);
        }
        map.put(key, data);

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

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            getExternalContext().log(message);
        } else {
            System.out.println(message);
        }

    }


    /**
     * <p>Log the specified message and exception to the container's
     * log file.</p>
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    protected void log(String message, Throwable throwable) {

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            getExternalContext().log(message, throwable);
        } else {
            System.out.println(message);
            throwable.printStackTrace(System.out);
        }

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
