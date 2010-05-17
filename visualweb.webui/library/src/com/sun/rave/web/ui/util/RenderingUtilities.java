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
package com.sun.rave.web.ui.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Body;
import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import java.util.ArrayList;
import javax.faces.component.UIParameter;

/**
 *
 * @author avk
 */
public class RenderingUtilities {

    /** Creates a new instance of RenderingUtilities. */
    public RenderingUtilities() {
    }

    /**
     * Render a component.
     * @param component The component to render
     * @param context The FacesContext of the request
     *
     */
    public static void renderComponent(UIComponent component,
            FacesContext context)
            throws IOException {

        if (!component.isRendered()) {
            return;
        }

        // this is a workaround a jsf bug in tables where it caches the
        // client id.  We are forcing the caching not to happen.
        // this could be a potential source of performance issues if
        // it turns out the jsf folks really wanted this

        String id = component.getId();
        if (id != null) {
            component.setId(id);
        }
        component.encodeBegin(context);
        if (component.getRendersChildren()) {
            component.encodeChildren(context);
        } else {
            Iterator kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                renderComponent(kid, context);
            }
        }
        component.encodeEnd(context);
    }


    /**
     * This method goes through an array of possible attribute names,
     * evaluates if they have been set on the component, and writes
     * them out using the specified writer.
     * @param component The component being rendered
     * @param writer The writer to use to write the attributes
     * @param possibleAttributes String attributes that are treated as
     * passthrough for this component
     */
    public static void writeStringAttributes(UIComponent component,
            ResponseWriter writer,
            String[] possibleAttributes)
            throws IOException {

        // Get the rest of the component attributes and display them
        Map attributes = component.getAttributes();


        int numNames = possibleAttributes.length;
        String attributeName = null;
        Object attributeValue;

        for (int counter = 0; counter < numNames; counter++) {
            attributeName = possibleAttributes[counter];
            attributeValue = attributes.get(attributeName);
            if (attributeValue != null) {
                writer.writeAttribute(attributeName.toLowerCase(),
                        String.valueOf(attributeValue),
                        attributeName);
            }
        }
    }

    /**
     * Add any attributes on the specified list directly to the specified
     * ResponseWriter for which the specified UIComponent has a non-null String
     * value. This method may be used to "pass through" commonly used attribute
     * name/value pairs with a minimum of code. Attribute names are converted to
     * lower case in the rendered output. Any name/value pairs in the extraHtml
     * String shall take precedence over attribute values.
     *
     * @param context FacesContext for the current request.
     * @param component EditableValueHolder component whose submitted value is 
     * to be stored.
     * @param writer ResponseWriter to which the element start should be rendered.
     * @param names List of attribute names to be passed through.
     * @param extraHtml Extra name/value pairs to be rendered.
     *
     * @exception IOException if an input/output error occurs
     */
    public static void writeStringAttributes(UIComponent component,
        ResponseWriter writer, String names[], String extraHtml)
            throws IOException {
        if (component == null || names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        Object value;
        for (int i = 0; i < names.length; i++) {
            // Special case for names matching "valign" instead of "align".
            if (extraHtml == null 
                    || extraHtml.indexOf(names[i] + "=") != 0
                    && extraHtml.indexOf(" " + names[i] + "=") == -1) {
                value = attributes.get(names[i]);
                if (value != null) {
                    if (value instanceof String) {
                        writer.writeAttribute(names[i].toLowerCase(),
                            (String) value, names[i]);
                    } else {
                        writer.writeAttribute(names[i].toLowerCase(),
                            value.toString(), names[i]);
                    }
                }
            }
        }
        // Render extra HTML attributes.
        renderExtraHtmlAttributes(writer, extraHtml);
    }

    /**
     * This method will output a hidden field for use with Params and components
     * that need to submit a value through a hidden field.
     * Note: The name of the hidden field will be written as is.  For Params
     * no encoding inside the form is done.  This is intentional.
     * @param writer The writer to use to write the attributes
     * @param id The identifier of the hidden field.
     * passthrough for this component
     */
    public static void renderHiddenField(UIComponent component,
                                         ResponseWriter writer,
                                         String id,
                                         String value)
            throws IOException {
        if (id == null) {
            // TODO: when we figure out our exception string strategy, fix this
            throw new IllegalArgumentException(
		    "An f:param tag had a null name attribute");
        }

        writer.startElement("input", component);  //NOI18N
        writer.writeAttribute("id", id, null); //NOI18N
        writer.writeAttribute("name", id, null); //NOI18N
        if (value != null) {
            writer.writeAttribute("value", value, null); //NOI18N
        }
        writer.writeAttribute("type", "hidden", null); //NOI18N
        writer.endElement("input"); //NOI18N
    }

    /**
     * <p>Return a space-separated list of CSS style classes to render for
     * this component, or <code>null</code> for none.</p>
     *
     * @param component <code>UIComponent</code> for which to calculate classes
     * @param styles Additional styles specified by the renderer
     */
    public static String getStyleClasses(FacesContext context,
            UIComponent component, String styles) {
        String styleClass = (String)
	    component.getAttributes().get("styleClass");

        boolean componentNotVisible = !isVisible(component);

        if (componentNotVisible) {
            String hiddenStyleClass = ThemeUtilities.getTheme(context)
            .getStyleClass(ThemeStyles.HIDDEN);
            if (styleClass != null) {
                styleClass += " "  + hiddenStyleClass;
            } else {
                styleClass = hiddenStyleClass;
            }
        }

        if (styleClass != null) {
            if (styles != null) {
                return styleClass + " " + styles;
            } else {
                return styleClass;
            }
        } else {
            if (styles != null) {
                return styles;
            } else {
                return null;
            }
        }
    }

    /**
     *
     */
    public static void renderStyleClass(FacesContext context,
            ResponseWriter writer,
            UIComponent component,
            String extraStyles)
            throws IOException {
        String classes = getStyleClasses(context, component, extraStyles);
        if (classes != null) {
            writer.writeAttribute("class", classes, "styleClass");
        }
    }

    /**
     * Helper method to render style classes when name/value pairs are given
     * via an extraHtml String. This method will append the given style to the
     * class name/value pair found in the extraHtml String. The class name/value
     * is removed from the returned extraHtml String so that developers may
     * invoke the writeStringAttributes method without rendering the style
     * class, again.
     *
     * @param context FacesContext for the current request.
     * @param component The UIComponent component to be rendered.
     * @param writer ResponseWriter to which the element start should be rendered.
     * @param style The style to append to the component's styleClass property.
     * @param extraHtml Extra name/value pairs to be rendered.
     */
    public static String renderStyleClass(FacesContext context, 
        ResponseWriter writer, UIComponent component, String styleClass, 
            String extraHtml) throws IOException {
        if (styleClass != null) {
            int first = -1;
            if (extraHtml != null 
                    && (first = extraHtml.indexOf("class=")) != -1) {
                try {
                    // Concat given class value with styleClass attribute.
                    int quote = first + 6; // Quote char index.
                    char ch = extraHtml.charAt(quote); // Get quote char.
                    int last = extraHtml.indexOf(ch, quote + 1); // Last index.
                    String s = extraHtml.substring(first, last + 1); // Get name/value pair
                    extraHtml = extraHtml.replaceAll(s, ""); // Remove substring.
                    s = s.substring(7, s.length() - 1); // Remove quote chars.
                    styleClass =  s + " " + styleClass; // Append styleClass.
                } catch (IndexOutOfBoundsException e) {}
            }
            renderStyleClass(context, writer, component, styleClass);
        }
        return extraHtml;
    }

    /**
     *
     */
    public static String getJavascriptId(FacesContext context,
            UIComponent component) {
        String client = component.getClientId(context);
        return client.replace(':', '_');
    }

    /**
     *
     */
    public static boolean isPortlet(FacesContext context) {
        if (context.getExternalContext().getContext()
		instanceof ServletContext) {
            return false;
        }
        return true;
    }

    /**
     * Get the client ID of the last component to have focus.
     */
    public static String getLastClientID(FacesContext context) {
        return (String) context.getExternalContext().getRequestMap().get(
            Body.FOCUS_PARAM);
    }

    /**
     * Set the client ID of the last component to have focus.
     */
    public static void setLastClientID(FacesContext context,
            String clientId) {
        context.getExternalContext().getRequestMap().put(Body.FOCUS_PARAM,
                clientId);
    }


    
   /**
     * Helper function to render a transparent spacer image.
     *
     *
     * @param writer The current ResponseWriter
     * @param component The uicomponent
     * @param height The value to use for the image height attribute
     * @param width The value to use for the image width attribute
     */
    public static void renderSpacer(ResponseWriter writer, 
                                    UIComponent component, 
                                    String dotSrc,
                                    int height, int width) throws IOException {
        
        if (height == 0 && width == 0) {
            return;
        }
        writer.startElement("img", component);
        writer.writeAttribute("src", dotSrc, null); // NOI18N
        writer.writeAttribute("alt", "", null); // NOI18N
        writer.writeAttribute("border", "0", null); // NOI18N
        writer.writeAttribute("height", new Integer(height), null); // NOI18N
        writer.writeAttribute("width", new Integer(width), null); // NOI18N
        writer.endElement("img"); // NOI18N

    }    

   /**
     * Helper function to render a transparent spacer image.
     *
     *
     * @param writer The current ResponseWriter
     * @param component The uicomponent
     * @param height The value to use for the image height attribute
     * @param width The value to use for the image width attribute
     */
    public static void renderSpacer(FacesContext context, 
                                    ResponseWriter writer, 
                                    UIComponent component, 
                                    int height, int width) throws IOException {
        
        if (height == 0 && width == 0) {
            return;
        }
        Theme theme = ThemeUtilities.getTheme(context);
        String dotSrc = theme.getIcon(ThemeImages.DOT).getUrl();
        renderSpacer(writer, component, dotSrc, height, width);
    }   
    
 

   /**
     * Helper function to render theme scripts
     *
     *
     * @param context containing theme
     * @param writer The current ResponseWriter
     * @param component The uicomponent
     */
    public static void renderJavaScript(UIComponent component, 
                                        Theme theme, 
                                        FacesContext context, 
                                        ResponseWriter writer) 
                                        
        throws IOException {
        
        
        String javascripts[] = theme.getGlobalJSFiles();
        for (int i = 0; i < javascripts.length; i++) {
            writer.startElement("script", component); // NOI18N
            writer.writeAttribute("type", "text/javascript", null); // NOI18N
            writer.writeURIAttribute
                    ("src", javascripts[i], null); // NOI18N
            writer.endElement("script"); // NOI18N
            writer.write("\n"); // NOI18N
        }
    }
     
    
     /**
     * Helper function to render theme stylesheet link(s)
     *
     *
     * @param context containing theme
     * @param writer The current ResponseWriter
     * @param component The uicomponent
     */
    public static void renderStyleSheetLink(UIComponent component, 
                                            Theme theme, 
                                            FacesContext context, 
                                            ResponseWriter writer) 
        throws IOException {
        
       

        //Master.
        String master = theme.getPathToMasterStylesheet(); 
        
        if (master != null) {
            writer.startElement("link", component); //NOI18N
            writer.writeAttribute("rel", "stylesheet", null); //NOI18N
            writer.writeAttribute("type", "text/css", null); //NOI18N
            writer.writeURIAttribute("href", master, null); //NOI18N
            writer.endElement("link"); //NOI18N
            writer.write("\n"); //NOI18N
        }

        //browser specific stuff.
        String browserSS = theme.getPathToStylesheet(context);
        if (browserSS != null) {
            writer.startElement("link", component); //NOI18N
            writer.writeAttribute("rel", "stylesheet", null); //NOI18N
            writer.writeAttribute("type", "text/css", null); //NOI18N
            writer.writeURIAttribute("href", browserSS, null); //NOI18N
            writer.endElement("link"); //NOI18N
            writer.write("\n"); //NOI18N
        }

        String stylesheets[] = theme.getGlobalStylesheets();
        for (int i = 0; i < stylesheets.length; i++) {
            writer.startElement("link", component); //NOI18N
            writer.writeAttribute("rel", "stylesheet", null); //NOI18N
            writer.writeAttribute("type", "text/css", null); //NOI18N
            writer.writeURIAttribute("href", stylesheets[i], null); //NOI18N
            writer.endElement("link"); //NOI18N
            writer.write("\n"); //NOI18N
        }
    }
     
    
   /**
     * Helper function to render theme stylesheet definitions inline
     *
     *
     * @param context containing theme
     * @param writer The current ResponseWriter
     * @param component The uicomponent
     */
    public static void renderStyleSheetInline(UIComponent component, 
                                              Theme theme, 
                                              FacesContext context,
                                              ResponseWriter writer) 
        throws IOException { 
        
        writer.startElement("style", component); //NOI18N
        writer.writeAttribute("type", "text/css", null); //NOI18N
        writer.write("\n"); //NOI18N
        String master = theme.getPathToMasterStylesheet();
        
        if (master != null) {
            writer.write("@import(\""); //NOI18N
            writer.write(master); //NOI18N
            writer.write("\");");
            writer.write("\n"); //NOI18N
        }
        
        //browser specific stuff.
        String browserSS = theme.getPathToStylesheet(context);
        if (browserSS != null) {
           writer.write("@import(\""); //NOI18N
            writer.write(browserSS); //NOI18N
            writer.write("\");");
            writer.write("\n"); //NOI18N
        }

        String stylesheets[] = theme.getGlobalStylesheets();
        for (int i = 0; i < stylesheets.length; i++) {
          writer.write("@import(\""); //NOI18N
            writer.write(stylesheets[i]); //NOI18N
            writer.write("\");");
            writer.write("\n"); //NOI18N
        }
         writer.endElement("style"); //NOI18N
    }
     
    
    /**
     * Perform a <code>RequestDispatcher.include</code> of the specified URI
     * <code>jspURI</code>.
     * <p>
     * The path identifed by <code>jspURI</code> must begin with
     * a <code>&lt;f:subview&gt;</code> tag. The URI must not have 
     * as part of its path the FacesServlet mapping. For example if the
     * FacesServlet mapping maps to <code>/faces/*</code> then 
     * <code>jspURI</code> must not have <code>/faces/</code> as part of 
     * its path.
     * </p>
     * <p>
     * If <code>jspUIR</code> is a relative path then the  
     * request context path is prepended to it.
     * </p>
     * @param context the <code>FacesContext</code> for this request
     * @param writer the <code>ResponseWrite</code> destination for the
     * rendered output
     * @param jspURI the URI identifying a JSP page to be included.
     * @throws IOException if response can't be written or <code>jspURI</code>
     * cannot be included. Real cause is chained.
     */
    public static void includeJsp(FacesContext context, ResponseWriter writer,
	String jspURI) throws IOException {

	class ResponseWrapper extends HttpServletResponseWrapper {
	    private PrintWriter printWriter;
	    public ResponseWrapper(HttpServletResponse response,
		    Writer writer) {
		super((HttpServletResponse)response);
		this.printWriter = new PrintWriter(writer);
	    }
	    public PrintWriter getWriter() {
		return printWriter;
	    }
	    public ServletOutputStream getOutputStream() throws IOException {
		throw new IllegalStateException();
	    }
	    public void resetBuffer() {
	    }
	}

	if (jspURI == null) {
	    return;
	}

	// prepend the request path if there is one in this path is not
	// a relative path. It appears that the servlet context algorithm
	// differs from the JspRuntime algorithm that allowed a relative
	// path in the lockhart wizard.
	//
	try {
	    if (!jspURI.startsWith("/")) { //NOI18N
		String contextPath = 
		    context.getExternalContext().getRequestContextPath();
		jspURI = contextPath.concat("/").concat(jspURI); //NOI18N
	    }
	       
	    ServletRequest request = 
		    (ServletRequest)context.getExternalContext().getRequest();
	    ServletResponse response =
		    (ServletResponse)context.getExternalContext().getResponse();

	    RequestDispatcher rd = request.getRequestDispatcher(jspURI);

	    // JSF is already buffering and suppressing output.
	    // 
	    rd.include(request,
		new ResponseWrapper((HttpServletResponse)response, writer));

	} catch (Exception e) {
	    throw (IOException)new IOException().initCause(e);
	}
    }

    public static final String NL = "/n"; //NOI18N
    public static final String SCRIPT = "script"; //NOI18N
    public static final String TYPE = "type"; //NOI18N
    public static final String SRC = "src"; //NOI18N
    public static final String MEDIA = "text/javascript"; //NOI18N
    // SJWUIC - Sun Java Web User Interface Components
    public static final String SJWUIC_JSFILE = "sjwuic_jsfile"; //NOI18N
    public static final String USCORE = "_"; //NOI18N
    public static final String COLON = ":"; //NOI18N
    public static final char USCORE_CHAR = '_'; //NOI18N
    public static final char COLON_CHAR = ':'; //NOI18N

    /**
     * Return true if the javascript file identified by 
     * <code>includeFile</code> is included.
     * Return false if <code>includeFile</code> is null
     * or there is no javascript file identified by
     * <code>includeFile</code>.
     * <p>
     * renderJsInclude maintains a RequestMap attribute 
     * "sjwuic_jsfile" that resolves to a Map containing
     * files that have previously been included.
     * </p>
     * @param context The current FacesContext
     * @param component The current component being rendered
     * @param theme The Theme to use to locate the Js file
     * @param writer The current ResponseWriter
     * @param includeFile The Js file to include
     */
    public static boolean renderJsInclude(FacesContext context,
	    UIComponent component,
	    Theme theme, ResponseWriter writer, String includeFile) 
	    throws  IOException {

	if (includeFile == null) {
	    return false;
	}

	Map requestMap = context.getExternalContext().getRequestMap();
	Map jsFileMap = (Map)requestMap.get(SJWUIC_JSFILE);
	if (jsFileMap == null) {
	    jsFileMap = new HashMap();
	    requestMap.put(SJWUIC_JSFILE, jsFileMap);
	}

        String jsFile = (String)jsFileMap.get(includeFile);
	if (jsFile != null) {
	    return true;
	}

	jsFile = theme.getPathToJSFile(includeFile);
	if (jsFile == null) {
	    return false;
	}
	jsFileMap.put(includeFile, jsFile);

        writer.startElement(SCRIPT, component);
        writer.writeAttribute(TYPE, MEDIA, null);
        writer.writeURIAttribute(SRC, jsFile, null);
        writer.endElement(SCRIPT);

	return true;
    }

    /**
     * Return true if markup is rendered that creates a javascript
     * object instance.
     * Return false if markup is not rendered.
     * <p>
     * Look for an attribute on the component identified by the
     * <code>attribute</code> parameter. If it exists and matches
     * the component's clientId after ":" has been replaced with "_"
     * return true. Otherwise render markup that creates a
     * javascript object named by the clientId where ":" is replaced
     * with "_" and then add this value to the component's 
     * attribute map as the value of an attribute identified by
     * the <code>attribute</code> parameter.
     * </p>
     * @param context The current FacesContext
     * @param component The current component being rendered
     * @param theme The Theme to use to locate the Js file
     * @param writer The current ResponseWriter
     * @param includeFile The Js file to include
     */

    private static final String newJsId =
	"var {0} = new {1}(''{2}'');"; //NOI18N
    private static final String newJsIdAndArgs =
	"var {0} = new {1}(''{2}'',{3});"; //NOI18N

    public static boolean renderJsObject(FacesContext context,
	    UIComponent component, ResponseWriter writer,
	    String objectName, String jsObjectClass, String arguments) 
	    throws IOException {

	if (context == null || component == null || writer == null ||
		jsObjectClass == null) {
	    return false;
	}
	if (objectName == null) {
	    objectName = jsObjectClass;
	}
        String clientId = component.getClientId(context);
	if (clientId == null) {
	    return false;
	}
	String jsObject = getJsObjectName(clientId, objectName);

	String format = arguments != null ? newJsIdAndArgs : newJsId;
	Object[] args =  new Object[] {
		jsObject, jsObjectClass, clientId, arguments };

        writer.startElement(SCRIPT, component);
        writer.writeAttribute(TYPE, MEDIA, null);
        writer.writeText(MessageFormat.format(format, args), null);
        writer.endElement(SCRIPT);

	return true;
    }

    public static String getJsObjectName(FacesContext context, 
	    UIComponent component, String objectPrefix) {
	return getJsObjectName(component.getClientId(context), objectPrefix);
    }

    public static String getJsObjectName(String clientId, String objectPrefix) {
	return objectPrefix.concat(USCORE).
		concat(clientId.replace(COLON_CHAR, USCORE_CHAR));
    }

    /**
     * Helper method to render extra attributes.
     *
     * @param writer <code>ResponseWriter</code> to which the element
     *  end should be rendered
     * @param extraHtml Extra HTML appended to the tag enclosing the header
     *
     * @exception IOException if an input/output error occurs
     */
    public static void renderExtraHtmlAttributes(ResponseWriter writer, 
            String extraHtml) throws IOException {
        if (extraHtml == null) {
            return;
        }

        int n = extraHtml.length();
        int i = 0;
        while (i < n) {
            StringBuffer name = new StringBuffer();
            StringBuffer value = new StringBuffer();

            // Skip extra space characters.
            while (i < n && Character.isWhitespace(extraHtml.charAt(i))) {
                i++;
            }

            // Find name.
            for (; i < n; i++) {
                char c = extraHtml.charAt(i);
                if (c == '\'' || c == '"') {
                    return; // Not well formed.
                } else if (c == '=') {
                    break;
                } else {
                    name.append(c);
                }
            }
            i++; // Skip =

            // Process quote character.
            char quote = (i < n) ? extraHtml.charAt(i) : '\0';
            if (!(quote == '\'' || quote == '"')) {
                return; // Not well formed.
            }
            i++; // Skip quote character.

            // Find value.
            for (; i < n; i++) {
                char c = extraHtml.charAt(i);
                if (c == quote) {
                    break;
                } else {
                    value.append(c);
                }
            }
            i++; // Skip quote character.

            writer.writeAttribute(name.toString(), value.toString(), null); //NOI18N
        }
    }

   /**
    * Helper function to render a typical URL
    *
    *
    * @param writer The current ResponseWriter
    * @param component The uicomponent
    * @param name The attribute name of the url to write out
    * @param url The value passed in by the developer for the url
    * @param compPropName The property name of the component's property that 
    * specifies this property.  Should be null if same as name.
    *
    */
    public static void renderURLAttribute(FacesContext context, 
                                    ResponseWriter writer, 
                                    UIComponent component,
                                    String name,
                                    String url,
                                    String compPropName) throws IOException {
        if (url == null) {
            return;
        }
        
        Param paramList[] = getParamList(context, component);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = paramList.length;
        sb = new StringBuffer();
        
        // Don't append context path here as themed images already include it.
        sb.append(url);
        if (0 < len) {
            sb.append("?");
        }
        for (i = 0; i < len; i++) {
            if (0 != i) {
                sb.append("&");
            }
            sb.append(paramList[i].getName());
            sb.append("=");
            sb.append(paramList[i].getValue());
        }
        
        
        String newName = null;
        if (compPropName != null) {
            newName = (compPropName.equals(name)) 
                                    ? null
                                    : compPropName;          
        }

        //<RAVE>
        //writer.writeURIAttribute(name, context.getExternalContext()
        //    .encodeResourceURL(sb.toString()), newName);
        
        if( url.trim().length() != 0 )
            writer.writeURIAttribute(name, context.getExternalContext()
                .encodeResourceURL(sb.toString()), newName);
        else
            writer.writeURIAttribute(name, "", newName);
        //<RAVE>
    }        
        
    static protected Param[] getParamList(FacesContext context, UIComponent command) {
        ArrayList parameterList = new ArrayList();

        Iterator kids = command.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();

            if (kid instanceof UIParameter) {
                UIParameter uiParam = (UIParameter) kid;
                Object value = uiParam.getValue();
                Param param = new Param(uiParam.getName(),
                                        (value == null ? null :
                                         value.toString()));
                parameterList.add(param);
            }
        }

        return (Param[]) parameterList.toArray(new Param[parameterList.size()]);
    }


    //inner class to store parameter name and value pairs
    static protected class Param {

        public Param(String name, String value) {
            set(name, value);
        }


        private String name;
        private String value;


        public void set(String name, String value) {
            this.name = name;
            this.value = value;
        }


        public String getName() {
            return name;
        }


        public String getValue() {
            return value;
        }
    }
    
    // This method is written in such a way that you can use it without
    // using the component.
    
    static public void renderSkipLink(String anchorName, String styleClass, String style, 
                                      String toolTip, Integer tabIndex,   
                                      UIComponent component, FacesContext context)
            throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        
        String id = component.getClientId(context); 
        writer.startElement("div", component); //NOI18N
        if(styleClass != null) {
            writer.writeAttribute("class", styleClass, null);
        }
        if(style != null) {
            writer.writeAttribute("style", styleClass, null);
        }   
        writer.write("\n"); //NOI18N
        
        StringBuffer buffer = new StringBuffer(128); 
        buffer.append("#"); 
        buffer.append(component.getClientId(context)); 
        buffer.append("_"); 
        buffer.append(anchorName); 
        
        writer.startElement("a", component); //NOI18N
        writer.writeAttribute("href", buffer.toString(), null);
        if(toolTip != null) {
            writer.writeAttribute("alt", toolTip, null);
        }
        if(tabIndex != null) {
            writer.writeAttribute("tabindex", tabIndex.toString(), null);
        }
        writer.write("\n"); //NOI18N

        // <RAVE>
        // Rendering 1x1 images in empty links causes layout problems in IE
        //Icon icon = ThemeUtilities.getTheme(context).getIcon(ThemeImages.DOT);
        //icon.setParent(component); 
        //icon.setWidth(1);
        //icon.setHeight(1);
        //icon.setBorder(0);
        //icon.setId("icon");
        //RenderingUtilities.renderComponent(icon, context);
        //writer.write("\n"); //NOI18N
        // </RAVE>
        
        writer.endElement("a"); //NOI18N
        writer.write("\n"); //NOI18N
        
        writer.endElement("div"); //NOI18N
    }
    
    // This method is written in such a way that you can use it without
    // using the component.
    
    static public void renderAnchor(String anchorName, UIComponent component, 
                                    FacesContext context)
    throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        
        StringBuffer buffer = new StringBuffer(128); 
        buffer.append(component.getClientId(context)); 
        buffer.append("_"); 
        buffer.append(anchorName);
        
        writer.startElement("div", component); //NOI18N
        writer.write("\n"); //NOI18N
        writer.startElement("a", component); //NOI18N
        writer.writeAttribute("name", buffer.toString(), null);
        writer.endElement("a"); //NOI18N
        writer.write("\n"); //NOI18N
        writer.endElement("div"); //NOI18N
    }
    
    /**
     *	<p> Return whether the given <code>UIComponent</code> is "visible".
     *	    If the property is null, it will return true.  Otherwise the value
     *	    of the property is returned.</p>
     *
     * @param	component   The <code>UIComponent</code> to check
     *
     * @return	True if the property is null or true, false otherwise.
     */
    public static boolean isVisible(UIComponent component) {
        Object visible = component.getAttributes().get("visible"); //NOI18N
        if (visible == null)
            return true;
        else
            return ((Boolean) visible).booleanValue();
    }
}
