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
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
//import javax.portlet.PortletContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.openide.util.Enumerations;

/**
 * <p>Provides a JSF <code>ExternalContext</code> object for design-time use</p>
 *
 * @author Robert Brewin
 * @version 1.0
 */
public class RaveExternalContext extends ExternalContext {

    /**
     * Holds the ServletContext or PortletContext
     */
    private Object context;

    /**
     * Provides a session map for this mock external context
     */
    private HashMap sessionMap = new HashMap();

    /**
     * Provides a request map if needed for this mock external context
     */
    private HashMap requestMap = new HashMap();

    /**
     * Provides a application map if needed for this mock external context
     */
    private HashMap applicationMap = new HashMap();

    /**
     * Provides a parameter map if needed for this mock external context
     */
    private HashMap requestParameterMap = new HashMap();

    /**
     * Provides an init parameter map if needed for this mock external context.
     */
    private HashMap initParameterMap = new HashMap();

    /**
     * empty map for stubbed out returns
     * XXX Should I make it readonly, since it's shared for multiple types?
     * (in case code tries to insert and remove items from these maps)
     */
    private HashMap emptyMap = new HashMap();

    /**
     * Create a mock "external" context for the JSF container (example: a mock servlet context).
     * This particular constructor initializes the servlet context with the passed in parameter
     *
     * @param context -- the context to bind to
     */
    public RaveExternalContext(Object context) {
        this.context = context;
        if (context instanceof ServletContext) {
            ServletContext servletContext = (ServletContext)context;
            Enumeration enumer = servletContext.getInitParameterNames();
            while (enumer.hasMoreElements()) {
                String name = (String) enumer.nextElement();
                initParameterMap.put(name, servletContext.getInitParameter(name));
            }
        } 
//       else if (context instanceof PortletContext) {
//            PortletContext portletContext = (PortletContext) context;
//            Enumeration enumer = portletContext.getInitParameterNames();
//            while (enumer.hasMoreElements()) {
//                String name = (String) enumer.nextElement();
//                initParameterMap.put(name, portletContext.getInitParameter(name));
//            }
//        }
    }

    /**
     * <p>Dispatch a request to the apropriate context. In the case of servlets, this is done via
     * "forward", but for portlets, it must use "include".</p>
     */
    public void dispatch(String path) throws IOException {
        // TODO: Make this functional if needed
    }

    /**
     *
     */
    public String encodeActionURL(String url) {
        // TODO: Make this functional if needed
        return url;
    }

    /**
     *
     */
    public String encodeNamespace(String name) {
        // TODO: Make this functional if needed
        return name;
    }

    public String encodeResourceURL(String url) {
        return encodeActionURL(url);
    }

    /**
     *
     */
    public Map getApplicationMap() {
        return applicationMap;
    }

    /**
     * @return
     */
    public String getAuthType() {
        return null;
    }

    /**
     *
     */
    public Object getContext() {
        return context;
    }

    /**
     *
     */
    public String getInitParameter(String name) {
        return (String) initParameterMap.get(name);
    }

    /**
     *
     */
    public Map getInitParameterMap() {
        return initParameterMap;
    }

    /**
     *
     */
    public String getRemoteUser() {
        return null;
    }

    /**
     *
     */
    public Object getRequest() {
        if (request == null) {
            request = new DesigntimeServletRequest();
        }
        return request;
    }
    
    DesigntimeServletRequest request;

    /** Designtime ServletRequest object, returns dummy data */
    private class DesigntimeServletRequest implements HttpServletRequest {
        public Object getAttribute(String str) {
            return null;
        }

        public java.util.Enumeration getAttributeNames() {
            return Enumerations.empty();
        }

        public String getAuthType() {
            return HttpServletRequest.BASIC_AUTH;
        }

        public String getCharacterEncoding() {
            // From javadoc:  "This method returns null if the request does not specify a character encoding"
            return null;
        }

        public int getContentLength() {
            return -1; // not known
        }

        public String getContentType() {
            return null; // content type not known
        }

        public String getContextPath() {
            return "/";
        }

        public javax.servlet.http.Cookie[] getCookies() {
            return new javax.servlet.http.Cookie[0];
        }

        public long getDateHeader(String str) {
            return System.currentTimeMillis();
        }

        public String getHeader(String str) {
            return ""; // NOI18N
        }

        public java.util.Enumeration getHeaderNames() {
            return Enumerations.empty();
        }

        public java.util.Enumeration getHeaders(String str) {
            return Enumerations.empty();
        }

        public javax.servlet.ServletInputStream getInputStream() throws IOException {
            return null;
        }

        public int getIntHeader(String str) {
            return 0;
        }

        public String getLocalAddr() {
            return ""; // NOI18N
        }

        public String getLocalName() {
            return "127.0.0.1"; // NOI18N
        }

        public int getLocalPort() {
            return 8080; // if they try connecting back we shouldn't do this...
        }

        public Locale getLocale() {
            return Locale.getDefault();
        }

        public java.util.Enumeration getLocales() {
            return Enumerations.array(Locale.getAvailableLocales());
        }

        public String getMethod() {
            return "GET"; // NOI18N
        }

        public String getParameter(String str) {
            return null;
        }

        public Map getParameterMap() {
            return emptyMap;
        }

        public java.util.Enumeration getParameterNames() {
            return Enumerations.empty();
        }

        public String[] getParameterValues(String str) {
            return null;
        }

        public String getPathInfo() {
            return null;
        }

        public String getPathTranslated() {
            return null;
        }

        public String getProtocol() {
            return "HTTP/1.1"; // NOI18N
        }

        public String getQueryString() {
            return null;
        }

        public java.io.BufferedReader getReader() throws IOException {
            return null;
        }

        public String getRealPath(String str) {
            return null; // deprecated anyway
        }

        public String getRemoteAddr() {
            return "127.0.0.1"; // NOI18N
        }

        public String getRemoteHost() {
            return "localhost"; // NOI18N
        }

        public int getRemotePort() {
            return 8080;
        }

        public String getRemoteUser() {
            return "Creator"; // NOI18N
        }

        public javax.servlet.RequestDispatcher getRequestDispatcher(String str) {
            return null;
        }

        public String getRequestURI() {
            return "/"; // XXX what should we return here?
        }

        public StringBuffer getRequestURL() {
            return new StringBuffer(); // XXX what should we return here?
        }

        public String getRequestedSessionId() {
            return null;
        }

        public String getScheme() {
            return "http"; // XXX or https?
        }

        public String getServerName() {
            return "localhost"; // NOI18N
        }

        public int getServerPort() {
            return 8080;
        }

        public String getServletPath() {
            return ""; // NOI18N
        }

        public javax.servlet.http.HttpSession getSession() {
            return getSession(true);
        }

        public HttpSession getSession(boolean create) {
            return (HttpSession)RaveExternalContext.this.getSession(create);
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public boolean isRequestedSessionIdFromCookie() {
            return true;
        }

        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        public boolean isRequestedSessionIdFromUrl() {
            return true;
        }

        public boolean isRequestedSessionIdValid() {
            return false;
        }

        public boolean isSecure() {
            return false;
        }

        public boolean isUserInRole(String str) {
            return false;
        }

        public void removeAttribute(String str) {
        }

        public void setAttribute(String str, Object obj) {
        }

        public void setCharacterEncoding(String str) throws java.io.UnsupportedEncodingException {
        }
    };
    
    /**
     *
     */
    public String getRequestContextPath() {
        // TODO: Make this functional if needed
        return "";
    }

    /**
     *
     */
    public Map getRequestCookieMap() {
        // TODO: Make this functional if needed
        return emptyMap;
    }

    /**
     *
     */
    public Map getRequestHeaderMap() {
        // Provide fake user agent string
        if (headerMap == null) {
            headerMap = new HashMap();
            String userAgent = System.getProperty("rave.userAgent");
            if (userAgent == null) {
                //userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4) Gecko/20030624 Netscape/7.1 (ax)";
                userAgent = "Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.0.1) Gecko/20020920 Netscape/7.0";
            }
            headerMap.put("USER-AGENT", userAgent);
        }
        return headerMap;
    }
    private HashMap headerMap;

    /**
     *
     */
    public Map getRequestHeaderValuesMap() {
        // TODO: Make this functional if needed
        return emptyMap;
    }

    /**
     *
     */
    public Locale getRequestLocale() {
        // TODO: Make this functional if needed
        return null;
    }

    static class EmptyIterator implements Iterator {
        public boolean hasNext() { return false; }
        public Object next() { return null; }
        public void remove() {}
    }

    /**
     *
     */
    public Iterator getRequestLocales() {
        // TODO: Make this functional if needed
        return new EmptyIterator();
    }

    /**
     *
     */
    public Map getRequestMap() {
        return requestMap;
    }

    /**
     *
     */
    public Map getRequestParameterMap() {
        return requestParameterMap;
    }

    /**
     *
     */
    public Iterator getRequestParameterNames() {
        return requestParameterMap.keySet().iterator();
    }

    /**
     *
     */
    public Map getRequestParameterValuesMap() {
        // TODO: Make this functional if needed
        return emptyMap;
    }

    /**
     *
     */
    public String getRequestPathInfo() {
        // TODO: Make this functional if needed
        return "";
    }

    /**
     *
     */
    public String getRequestServletPath() {
        // TODO: Make this functional if needed
        return "";
    }

    /**
     *
     */
    public URL getResource(String path) throws MalformedURLException {
        // TODO: Make this functional if needed
        return null;
    }

    /**
     *
     */
    public InputStream getResourceAsStream(String path) {
        // TODO: Make this functional if needed
        return null;
    }

    /**
     *
     */
    public Set getResourcePaths(String path) {
        // TODO: Make this functional if needed
        return null;
    }

    /**
     *
     */
    public Object getResponse() {
        // TODO: Make this functional if needed
        return null;
    }

    /**
     *
     */
    public Object getSession(boolean create) {
        // TODO: Make this functional if needed
        return null;
    }

    /**
     * @return a <code>Map</code> that wraps the HttpSession's attribute set.
     */
    public Map getSessionMap() {
        return sessionMap;
    }

    /**
     * @return
     */
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * @return
     */
    public boolean isUserInRole(String role) {
        return true;
    }

    /**
     *
     */
    public void log(String s) {
    }

    /**
     *
     */
    public void log(String s, Throwable t) {
    }

    /**
     *
     */
    public void redirect(String url) throws IOException {
    }
}
