<%--
 - Copyright (c) 2010, Oracle. All rights reserved.
 -
 - Redistribution and use in source and binary forms, with or without
 - modification, are permitted provided that the following conditions are met:
 -
 - * Redistributions of source code must retain the above copyright notice,
 -   this list of conditions and the following disclaimer.
 -
 - * Redistributions in binary form must reproduce the above copyright notice,
 -   this list of conditions and the following disclaimer in the documentation
 -   and/or other materials provided with the distribution.
 -
 - * Neither the name of Oracle nor the names of its contributors
 -   may be used to endorse or promote products derived from this software without
 -   specific prior written permission.
 -
 - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 - AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 - IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 - ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 - LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 - CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 - SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 - INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 - CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 - ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 - THE POSSIBILITY OF SUCH DAMAGE.
--%>

<%--
    @author Kirill Sorokin, Kirill.Sorokin@Sun.COM
--%>
<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="java.text.*" %>
<%@page import="java.util.*" %>
<%@page import="org.w3c.dom.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>

<%
    final String APPROVED = "Approved";
    final String REJECTED = "Rejected";

    final String SELECTED = "selected=\"selected\"";

    final Locale locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
    final long taskId = (Long) request.getAttribute(TASK_ID_ATTRIBUTE);
    final String userId = (String) request.getAttribute(USER_ID_ATTRIBUTE);
    final TaskType task = (TaskType) request.getAttribute(TASK_ATTRIBUTE);

    final String operationMode = (String) request.getAttribute(TASK_OUTPUT_HANDLER_MODE_ATTRIBUTE);

    if (OUTPUT_MODE.equals(operationMode)) {
        String approveResult = "";
        String description = "";

        // It might happen that the user submitted the updated output data, which was invalid,
        // in this case we need to display the data that was submitted, instead of working with
        // the one provided by the WLM SE. The error message has already been displayed for us.
        if (request.getAttribute(TASK_OUTPUT_DATA_EXCEPTION_ATTRIBUTE) == null) {
            final Element outputData = (Element) request.getAttribute(TASK_OUTPUT_DATA_ATTRIBUTE);

            final NodeList nodes = outputData.getChildNodes();
            final int nodesLength = nodes.getLength();
            for (int i = 0; i < nodesLength; i++) {
                final Node node = nodes.item(i);
                final String localName = node.getLocalName();

                if ("approveResult".equals(localName)) {
                    approveResult = node.getTextContent();
                }

                if ("description".equals(localName)) {
                    description = node.getTextContent();
                }
            }
        } else {
            approveResult = request.getParameter("posOutputDataApproveResult");
            description = request.getParameter("posOutputDataDescription");
        }

        final Boolean readOnly = (Boolean)
                request.getAttribute(TASK_OUTPUT_DATA_READ_ONLY_ATTRIBUTE);

        if (readOnly) {
        %>
            <div id="posOutputData">
                <div id="posOutputDataApproveResult">
                    <%--<%= getMessage(KEY_HANDLERS_POS_RESULT, locale) %>--%>
                    <% if (APPROVED.equals(approveResult)) { %>
                        <%= getMessage(KEY_HANDLERS_POS_APPROVED, locale) %>
                    <% } %>
                    <% if (REJECTED.equals(approveResult)) { %>
                        <%= getMessage(KEY_HANDLERS_POS_REJECTED, locale) %>
                    <% } %>
                </div>
                <div id="posOutputDataDescription">
                    <%= getMessage(KEY_HANDLERS_POS_DESCRIPTION, locale) %><br/>
                    <%= escapeHtml(description) %>
                </div>
            </div>
        <%
        } else {
        %>
            <div id="posOutputData">
                <div id="posOutputDataApproveResult">
                    <%= getMessage(KEY_HANDLERS_POS_RESULT, locale) %>
                    <select name="posOutputDataApproveResult">
                        <option value="<%= APPROVED %>"
                                <%= APPROVED.equals(approveResult) ? SELECTED : "" %>>
                            <%= getMessage(KEY_HANDLERS_POS_APPROVED, locale) %>
                        </option>
                        <option value="<%= REJECTED %>"
                                <%= REJECTED.equals(approveResult) ? SELECTED : "" %>>
                            <%= getMessage(KEY_HANDLERS_POS_REJECTED, locale) %>
                        </option>
                    </select>
                </div>
                <div id="posOutputDataDescription">
                    <%= getMessage(KEY_HANDLERS_POS_DESCRIPTION, locale) %><br/>
                    <textarea name="posOutputDataDescription"><%= escapeHtml(description) %></textarea>
                </div>
            </div>
        <%
        }
    } else {
        final String approveResult = request.getParameter("posOutputDataApproveResult");
        final String description = request.getParameter("posOutputDataDescription");
        
        final Element outputData = (Element) request.getAttribute(TASK_OUTPUT_DATA_ATTRIBUTE);
        final Element inputData = (Element) request.getAttribute(TASK_INPUT_DATA_ATTRIBUTE);

        String orderId = "";
        final NodeList inputNodes = inputData.getChildNodes();
        final int inputNodesLength = inputNodes.getLength();
        for (int i = 0; i < inputNodesLength; i++) {
            final Node node = inputNodes.item(i);
            final String localName = node.getLocalName();

            if ("orderId".equals(localName)) {
                orderId = node.getTextContent();
            }
        }

        final NodeList outputNodes = outputData.getChildNodes();
        final int outputNodesLength = outputNodes.getLength();
        for (int i = 0; i < outputNodesLength; i++) {
            final Node node = outputNodes.item(i);
            final String localName = node.getLocalName();

            if ("orderId".equals(localName)) {
                node.setTextContent(orderId);
            }

            if ("replyDate".equals(localName)) {
                node.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            }

            if ("approveResult".equals(localName)) {
                node.setTextContent(approveResult);
            }

            if ("description".equals(localName)) {
                node.setTextContent(description);
            }
        }

        request.setAttribute(TASK_OUTPUT_DATA_ATTRIBUTE, outputData);
    }
%>
