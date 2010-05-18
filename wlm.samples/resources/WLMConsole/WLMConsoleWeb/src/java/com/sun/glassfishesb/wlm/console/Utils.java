/**
 * Copyright (c) 2009, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.glassfishesb.wlm.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import sun.com.jbi.wfse.wsdl.taskcommon.SortField;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskStatus;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskType;

/**
 *
 * @author Kirill Sorokin, Kirill.Sorokin@Sun.COM
 */
public final class Utils implements Constants {
    private static final Logger LOGGER = Logger.getLogger("com.sun.glassfishesb.wlm.console");

    private static final Map<Locale, ResourceBundle> BUNDLES =
            new HashMap<Locale, ResourceBundle>();

    public static String getMessage(
            final String key,
            final Locale locale,
            final Object... arguments) {

        ResourceBundle bundle;
        synchronized(BUNDLES) {
            bundle = BUNDLES.get(locale);

            if (bundle == null) {
                final String baseName = Utils.class.getPackage().getName() +
                        CLASSPATH_SEPARATOR + BUNDLE_LOCAL_NAME;

                bundle = ResourceBundle.getBundle(baseName, locale);

                BUNDLES.put(locale, bundle);
            }
        }

        try {
            return escapeHtml(MessageFormat.format(bundle.getString(key), arguments));
        } catch (MissingResourceException e) {
            LOGGER.log(Level.FINE, "Failed to find a localized message.", e);

            if (!KEY_SYSTEM_MESSAGENOTFOUND.equals(key)) {
                return getMessage(KEY_SYSTEM_MESSAGENOTFOUND, locale);
            } else {
                return "";
            }
        }
    }

    public static String escapeHtml(
            final String unescaped) {

        return escapeHtmlForTextArea(unescaped).
                replace(NEW_LINE, NEW_LINE_ENTITY);
    }

    public static String escapeHtmlForTextArea(
            final String unescaped) {

        return unescaped.
                replace(AMPERSAND, AMPERSAND_ENTITY).
                replace(LEFT_TAG, LEFT_TAG_ENTITY).
                replace(RIGHT_TAG, RIGHT_TAG_ENTITY).
                replace(DOUBLE_QUOTE, DOUBLE_QUOTE_ENTITY).
                replace(SINGLE_QUOTE, SINGLE_QUOTE_ENTITY);
    }

    public static String getPageTitle(
            final String pagePath,
            final Locale locale,
            final Object... arguments) {

        if (HELP_PAGE_URL.equals(pagePath)) {
            return getMessage(KEY_PAGES_HELP_PAGETITLE, locale, arguments);
        }

        if (INDEX_PAGE_URL.equals(pagePath)) {
            return getMessage(KEY_PAGES_INDEX_PAGETITLE, locale, arguments);
        }

        if (LOGIN_PAGE_URL.equals(pagePath)) {
            return getMessage(KEY_PAGES_LOGIN_PAGETITLE, locale, arguments);
        }

        if (LOGIN_FAILED_PAGE_URL.equals(pagePath)) {
            return getMessage(KEY_PAGES_LOGIN_FAILED_PAGETITLE,
                    locale, arguments);
        }

        if (TASK_PAGE_URL.equals(pagePath)) {
            return getMessage(KEY_PAGES_TASK_PAGETITLE, locale, arguments);
        }

        return getMessage(KEY_SYSTEM_UNKNOWNPAGE, locale);
    }

    public static long getTaskId(
            final HttpServletRequest request) {

        final String id = request.getParameter(TASK_ID_PARAMETER);

        if (id != null) {
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.FINE,
                        "Failed to parse the supplied task ID.", e); // NOI18N
            }
        }

        return UNKNOWN_TASK_ID;
    }

    public static String marshallElement(
            final Element element,
            final Locale locale) {

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(
                    "indent-number", new Integer(4)); // NOI18N

            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(
                    OutputKeys.METHOD, "xml"); // NOI18N
            transformer.setOutputProperty(
                    OutputKeys.ENCODING, UTF8);
            transformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "yes"); // NOI18N
            transformer.setOutputProperty(
                    OutputKeys.INDENT, "yes"); // NOI18N
            
            transformer.transform(
                    new DOMSource(element),
                    new StreamResult(new OutputStreamWriter(baos, UTF8)));
            return baos.toString(UTF8);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to transform an org.w3c.dom.Element to a string.", e); // NOI18N
        } catch (TransformerFactoryConfigurationError e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to transform an org.w3c.dom.Element to a string.", e); // NOI18N
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,
                    "Unknown encoding -- " + UTF8 + ". WTF?", e); // NOI18N
        }

        return getMessage(KEY_SYSTEM_FAILEDTOFORMATELEMENT, locale);
    }

    public static Element unmarshallElement(
            final String string,
            final Locale locale) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new InputSource(new StringReader(string)));

        return document.getDocumentElement();
    }

    public static String stringOrMDash(
            final String string) {

        return (string == null) || "".equals(string) ? MDASH_ENTITY : string;
    }

    public static String getDatePattern(
            final Locale locale) {

        final DateFormat format = DateFormat.getDateInstance(
                DateFormat.SHORT, locale);
        final GregorianCalendar cal = new GregorianCalendar(
                2003, GregorianCalendar.FEBRUARY, 1);

        return format.format(cal.getTime())
                .replace("2003", "YYYY")
                .replace("03", "YY")
                .replace("3", "Y")
                .replace("02", "MM")
                .replace("2", "M")
                .replace("01", "DD")
                .replace("1", "D")
                .replace("\"", "\\\"")
                .replace("\\", "\\\\");
    }

    // Task elements formatting --------------------------------------------------------------------
    public static int formatPriority(
            final TaskType task) {

        final Integer priority = task.getPriority();
        if (priority > 7) {
            return 10;
        }

        if (priority > 3) {
            return 5;
        }

        return 1;
    }

    public static String formatAssignedTo(
            final TaskType task) {

        return stringOrMDash(task.getAssignedTo());
    }

    public static String formatClaimedBy(
            final TaskType task) {

        return stringOrMDash(task.getClaimedBy());
    }

    public static String formatTitle(
            final TaskType task) {

        return stringOrMDash(task.getTitle());
    }

    public static String formatStatus(
            final TaskStatus taskStatus,
            final Locale locale) {

        return getMessage(
                KEY_PREFIX_TASK_STATUS + taskStatus.value(),
                locale);
    }

    public static String formatStatus(
            final TaskType task,
            final Locale locale) {

        return formatStatus(task.getStatus(), locale);
    }

    public static String formatOrder(
            final SortField sortField,
            final Locale locale) {

        return getMessage(
                KEY_PREFIX_ORDER_FIELD + sortField.value(),
                locale);
    }

    public static String formatDeadline(
            final TaskType task) {

        return formatTime(task.getDeadline());
    }

    public static String formatSubmittedOn(
            final TaskType task) {

        return formatTime(task.getSubmittedDate());
    }

    private static String formatTime(
            final XMLGregorianCalendar xmlCalendar) {

        if (xmlCalendar == null) {
            return MDASH_ENTITY;
        } else {
            long utcMS = xmlCalendar.normalize().toGregorianCalendar()
                    .getTimeInMillis();

            return "<script type=\"text/javascript\">printDate(" + utcMS + ");</script>" +
                    "<noscript>" + xmlCalendar.toString() + "</noscript>";
        }
    }

    public static void pringChangePageCountHiddenFields(JspWriter out,
            int firstTask, SortField sortField) throws IOException
    {
        if (firstTask > 0) {
            out.print("<input type=\"hidden\" name=\"start\" value=\"");
            out.print(firstTask);
            out.print("\" />");
        }

        if (sortField != null && sortField != SortField.ID) {
            out.print("<input type=\"hidden\" name=\"order\" value=\"");
            out.print(sortField.value());
            out.print("\" />");
        }
    }

    public static void pringChangeOrderHiddenFields(JspWriter out,
            int firstTask, int pageSize) throws IOException
    {
        if (firstTask > 0) {
            out.print("<input type=\"hidden\" name=\"start\" value=\"");
            out.print(firstTask);
            out.print("\" />");
        }

        if (pageSize != 20) {
            out.print("<input type=\"hidden\" name=\"size\" value=\"");
            out.print(pageSize);
            out.print("\" />");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    private Utils() {
        // Does nothing.
    }
}
