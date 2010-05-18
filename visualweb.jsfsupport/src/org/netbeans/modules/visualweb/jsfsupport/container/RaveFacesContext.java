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
package org.netbeans.modules.visualweb.jsfsupport.container;

import com.sun.faces.RIConstants;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.el.PropertyResolver;
import javax.faces.render.RenderKit;

//import com.sun.rave.web.ui.faces.DataProviderPropertyResolver;
import com.sun.rave.designtime.DesignContext;

//import com.sun.jsfcl.data.ResultSetPropertyResolver;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.config.ConfigureListener;
import com.sun.faces.el.ELContextImpl;
import javax.el.ELContext;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletContext;
import org.openide.ErrorManager;

/**
 * RaveFacesContext provides a JSF context for design-time use
 *
 * @author Robert Brewin
 * @author Winston Prakash - Modifications to support JSF 1.2
 * @version 1.0
 */
public class RaveFacesContext extends FacesContext {

    private static final String FACESCONTEXT_IMPL_ATTR_NAME = RIConstants.FACES_PREFIX + "FacesContextImpl";

    private ELContext elContext = null;

    /**
     * The view root used for a given context
     */
    private UIViewRoot viewRoot;

    /**
     * The stream to use for emitting renderered output
     */
    private ResponseStream responseStream;

    /**
     * The writer used for emitting output
     */
    private ResponseWriter responseWriter;

    /**
     * The "external" context for this faces container
     */
    private ExternalContext externalContext;

    /**
     * The live context for this faces container
     */
    private DesignContext liveContext;

    /**
     * The application for this faces container, cached from factory
     */
    private Application application;

    /**
     * A hash of ArrayLists of Messages
     * key: clientId
     * value: an arraylist of messages assoc. with clientId
     */
    private HashMap messageHash;

    /**
     * maxServerity
     * the highest serverity of any message in messageHash
     */
    private Severity maxSeverity;

    private boolean released;
    private RenderKitFactory rkFactory;
    private RenderKit lastRk;
    private String lastRkId;

    private ServletContext servletContext;

    /**
     * <p>Parameter signature for the constructor of a PropertyResolver
     * that takes a PropertyResolver argument.</p>
     */
    private Class signature[] = new Class[] { PropertyResolver.class };

    /**
     * Constructor for this faces context, initialized with an external context
     *
     * @param context -- the context to use
     */
    public RaveFacesContext(ExternalContext context) {
        setCurrentInstance(this);
        externalContext = context;
        messageHash = new HashMap();
        maxSeverity = null;
        // Store this in request scope so jsf-api can access it.

        this.externalContext.getRequestMap().put(FACESCONTEXT_IMPL_ATTR_NAME, this);
        rkFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    }

    public void unsetCurrentInstance() {
        setCurrentInstance(null);
    }

    public void setCurrentInstance() {
        setCurrentInstance(this);
    }    // ---------------------------------------------------------------------------------- Properties
    
    public void setServletContext(ServletContext context){
        servletContext = context;
    }
    
    /**
     * <p>Reset the <code>Application</code> instance returned by
     * <code>ApplicationFactory</code> to a pristine instance, and
     * release our cached reference so that the next call to
     * <code>getApplication()</code> will get a new one.</p>
     */
    public void resetApplication() {
        try {
            
            setCurrentInstance(this);
                       
            
            // This hack is to fix the bug  NPE from com.sun.faces.spi.InjectionProviderFactory.findProviderClass()
            // Once we set the ThreadLocal<ExternalContext>, it is being looked for instance of WebConfiguration
            // So get the one from Servlet context and set to it.
            getExternalContext().getApplicationMap().put("com.sun.faces.config.WebConfiguration", servletContext.getAttribute("com.sun.faces.config.WebConfiguration"));
               
            
            // Also nuke the old Application Associate from the map or it will complain
            ApplicationAssociate.clearInstance(getExternalContext());
            
            // Reset the instance to be returned by ApplicationFactory
            ApplicationFactory appFactory =
                    (ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            
            if (appFactory == null) {
                throw new IllegalStateException("ApplicationFactory"); // NOI18N
            }
            
            appFactory.setApplication(new ApplicationImpl());
            
            // Clear our cached reference (if any)
            application = null;
        } catch (Throwable t) {
            org.openide.ErrorManager.getDefault().notify(t);
        }
    }
    
    private static ThreadLocal getConfigureListenerThreadLocalExternalContext() {
        try {
            Field field = ConfigureListener.class.getDeclaredField("tlsExternalContext"); // NOI18N
            field.setAccessible(true);
            Object result;
            result = field.get(null);
            return result instanceof ThreadLocal ? (ThreadLocal)result : null;
        } catch (SecurityException se) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, se);
        } catch (NoSuchFieldException nfe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
        } catch (IllegalArgumentException iae) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
        } catch (IllegalAccessException iace) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iace);
        }
        
        return null;
    }
    
    
    /**
     * <p>Return the {@link javax.faces.application.Application} instance associated with this
     * web application.</p>
     */
    public Application getApplication() {
        
        // Return any previously cached application instance
        if (application != null) {
            return application;
        }
        
        // Acquire a reference to the Application instance for this application
        ApplicationFactory appFactory = (ApplicationFactory)
        FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        if (appFactory == null) {
            throw new IllegalStateException("ApplicationFactory"); // NOI18N
        }
        application = appFactory.getApplication();
        if (application == null) {
            throw new IllegalStateException("Application"); // NOI18N
        }
        
        // Return our configured instance
        return application;
    }
    
    public ELContext getELContext() {
        assertNotReleased();
        if (elContext == null) {
            elContext = new ELContextImpl(getApplication().getELResolver());
            elContext.putContext(FacesContext.class, this);
            UIViewRoot root = this.getViewRoot();
            if (null != root) {
                elContext.setLocale(root.getLocale());
            }
        }
        return elContext;
    }
    
    private void assertNotReleased() {
        if (released) {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Return the external context
     */
    public Iterator getClientIdsWithMessages() {
        return messageHash.keySet().iterator();
    }
    
    /**
     * Return the external context
     * @return the <code>ExternalContext</code> associated with this faces context
     */
    public ExternalContext getExternalContext() {
        return externalContext;
    }
    
    /**
     * Return the current DesignContext
     * @return the <code>DesignContext</code> associated with this faces context
     */
    public DesignContext getDesignContext() {
        return liveContext;
    }
    
    /**
     * Set the current DesignContext
     * @param liveContext
     */
    public void setDesignContext(DesignContext liveContext) {
        this.liveContext = liveContext;
    }
    
    /**
     *
     */
    public Severity getMaximumSeverity() {
        return maxSeverity;
    }
    
    /**
     *
     */
    public Iterator getMessages() {
        ArrayList messages = new ArrayList();
        messages.add(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "msg-summary", "msg-detail"));
        return messages.iterator();
        /*
        ArrayList allMessages = new ArrayList();
        Iterator iter = messageHash.values().iterator();
        while (iter.hasNext()) {
            ArrayList messages = (ArrayList)iter.next();
            Iterator messageIter = messages.iterator();
            while (messageIter.hasNext()) {
                allMessages.add(messageIter.next());
            }
        }
        return allMessages.iterator();
         */
    }
    
    /**
     *
     */
    public Iterator getMessages(String clientId) {
        ArrayList messages = new ArrayList();
        messages.add(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "msg-summary", "msg-detail"));
        return messages.iterator();
        /*
        ArrayList messages = (ArrayList)messageHash.get(clientId);
        if (messages == null) {
            messages = new ArrayList();
        }
        return messages.iterator();
         */
    }
    
    /**
     * <p>Return the {@link RenderKit} instance for the render kit identifier
     * specified on our {@link UIViewRoot}, if there is one.
     */
    public RenderKit getRenderKit() {
        assertNotReleased();
        UIViewRoot vr = getViewRoot();
        if (vr == null) {
            return (null);
        }
        String renderKitId = vr.getRenderKitId();
        
        if (renderKitId.equals(lastRkId)) {
            return lastRk;
        } else {
            lastRk = rkFactory.getRenderKit(this, renderKitId);
            lastRkId = renderKitId;
            return lastRk;
        }
    }
    
    /**
     * <p>Return <code>true</code> if the <code>renderResponse()</code>
     * method has been called for the current request.</p>
     */
    public boolean getRenderResponse() {
        // TODO: Make this functional if needed
        return false;
    }
    
    /**
     * <p>Return <code>true</code> if the <code>responseComplete()</code>
     * method has been called for the current request.</p>
     */
    public boolean getResponseComplete() {
        // TODO: Make this functional if needed
        return false;
    }
    
    /**
     *
     */
    public ResponseStream getResponseStream() {
        return responseStream;
    }
    
    /**
     *
     */
    public void setResponseStream(ResponseStream responseStream) {
        this.responseStream = responseStream;
    }
    
    /**
     *
     */
    public ResponseWriter getResponseWriter() {
        return responseWriter;
    }
    
    /**
     *
     */
    public void setResponseWriter(ResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }
    
    /**
     * <p>Return the component view that is associated with the this request.
     * </p>
     */
    public UIViewRoot getViewRoot() {
        return this.viewRoot;
    }
    
    /**
     *
     */
    public void setViewRoot(UIViewRoot viewRoot) {
        if (viewRoot == null) {
            // TODO: Make this functional if needed
        }
        this.viewRoot = viewRoot;
    }
    
// ------------------------------------------------------------------------------ Public Methods
    
    /**
     *
     */
    public void addMessage(String clientId, FacesMessage message) {
        ArrayList messages = (ArrayList)messageHash.get(clientId);
        if (messages == null) {
            messages = new ArrayList();
            messageHash.put(clientId, messages);
        }
        messages.add(message);
        if (maxSeverity == null) {
            maxSeverity = message.getSeverity();
        } else if (message.getSeverity().getOrdinal() > maxSeverity.getOrdinal()) {
            maxSeverity = message.getSeverity();
        }
    }
    
    /**
     *
     */
    public void release() {
        // TODO: other stuff?
        messageHash = new HashMap();
        maxSeverity = null;
        
        this.externalContext.getRequestMap().remove(FACESCONTEXT_IMPL_ATTR_NAME);
        
        released = true;
        externalContext = null;
        responseStream = null;
        responseWriter = null;
        //componentMessageLists = null;
        //renderResponse = false;
        //responseComplete = false;
        viewRoot = null;
        
        // PENDING(edburns): write testcase that verifies that release
        // actually works.  This will be important to keep working as
        // ivars are added and removed on this class over time.
        
        // Make sure to clear our ThreadLocal instance.
        setCurrentInstance(null);
    }
    
    /**
     *
     */
    public void renderResponse() {
        // TODO: Make this functional if needed
    }
    
    /**
     *
     */
    public void responseComplete() {
        // TODO: Make this functional if needed
    }
    
}
