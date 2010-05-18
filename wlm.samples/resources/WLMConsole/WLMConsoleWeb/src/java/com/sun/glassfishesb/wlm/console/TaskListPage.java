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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import sun.com.jbi.wfse.wsdl.taskcommon.Direction;
import sun.com.jbi.wfse.wsdl.taskcommon.GroupsType;
import sun.com.jbi.wfse.wsdl.taskcommon.QueryType;
import sun.com.jbi.wfse.wsdl.taskcommon.SortField;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskCommonPortType;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskCommonService;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskListType;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskStatus;
import sun.com.jbi.wfse.wsdl.taskcommon.TaskType;
import sun.com.jbi.wfse.wsdl.taskcommon.UsersType;

/**
 *
 * @author Alexey Anjeleevich, Alexey.Anjeleevich@Sun.COM
 */
public class TaskListPage {
    private static final Logger LOGGER = Logger.getLogger("com.sun.glassfishesb.wlm.console");

    private String userName;

    private String[] users;
    private String usersString;

    private String[] groups;
    private String groupsString;

    private Set<TaskStatus> statuses;

    private String searchString;

    private int firstTask;
    private int pageSize;
    private SortField sortField;

    private int totalTaskCount = -1;
    private int returnedTaskCount = -1;

    private TaskListPage(HttpServletRequest request) {
        userName = request.getUserPrincipal().getName();

        pageSize = extractPageSize(request);
        firstTask = extractFirstTask(request, pageSize);
        sortField = extractSortField(request);

        statuses = extractTaskListStatuses(request);
        users = extractTaskListUsers(request);
        groups = extractTaskListGroups(request);

        searchString = extractSearchString(request);

        usersString = merge(users);
        groupsString = merge(groups);
    }

    public String createPageHRef(int pageNumber) {
        return createPageHRefImpl((pageNumber - 1) * pageSize);
    }

    public String createRedirect() {
        return createPageHRefImpl(firstTask);
    }

    private String createPageHRefImpl(int newStartTask) {
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        
        if (newStartTask > 0) {
            first = appendHrefAmp(builder, first);
            builder.append("start=");
            builder.append(newStartTask);
        }

        if (!isDefaultPageSize()) {
            first = appendHrefAmp(builder, first);
            builder.append("size=");
            builder.append(pageSize);
        }

        if (!isDefaultSortField()) {
            first = appendHrefAmp(builder, first);
            builder.append("order=");
            builder.append(sortField.value());
        }

        if (hasSearchString()) {
            first = appendHrefAmp(builder, first);
            builder.append("search=");
            appendHrefValue(builder, searchString);
        }

        if (hasUsers()) {
            first = appendHrefAmp(builder, first);
            builder.append("users=");
            appendHrefValue(builder, usersString);
        }

        if (hasGroups()) {
            first = appendHrefAmp(builder, first);
            builder.append("groups=");
            appendHrefValue(builder, groupsString);
        }

        if (hasStatuses()) {
            for (TaskStatus status : statuses) {
                first = appendHrefAmp(builder, first);
                builder.append("status=");
                builder.append(status.value().toLowerCase());
            }
        }

        return builder.toString();
    }

    private static boolean appendHrefAmp(StringBuilder builder, boolean first) {
        builder.append((first) ? '?' : '&');
        return false;
    }

    private static void appendHrefValue(StringBuilder builder, String value) {
        try {
            builder.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            // never happens
            ex.printStackTrace();
        }
    }

    public boolean hasUsers() {
        return (users != null) && (users.length > 0);
    }

    public boolean hasGroups() {
        return (groups != null) && (groups.length > 0);
    }

    public boolean hasSearchString() {
        return (searchString != null) && (searchString.length() > 0);
    }

    public boolean hasStatuses() {
        return (statuses != null) && !(statuses.isEmpty());
    }

    public boolean isDefaultFirstTask() {
        return (firstTask == 0);
    }

    public boolean isDefaultPageSize() {
        return (pageSize == 20);
    }

    public boolean isDefaultSortField() {
        return sortField == SortField.ID;
    }

    public int getFirstTask() {
        return firstTask;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalTaskCount() {
        return totalTaskCount;
    }

    public int getReturnedTaskCount() {
        return returnedTaskCount;
    }

    public SortField getSortField() {
        return sortField;
    }

    public Set<TaskStatus> getStatuses() {
        return statuses;
    }

    public String getUsersString(boolean escape) {
        return (escape) ? Utils.escapeHtml(usersString) : usersString;
    }

    public String getGroupsString(boolean escape) {
        return (escape) ? Utils.escapeHtml(groupsString) : groupsString;
    }

    public String getSearchString(boolean escape) {
        return (escape) ? Utils.escapeHtml(searchString) : searchString;
    }

    public boolean isAdvancedSearchMode() {
        return hasStatuses() || hasUsers() || hasGroups();
    }

    public boolean isBasicSearchMode() {
        return !isAdvancedSearchMode() && hasSearchString();
    }

    public boolean isViewMode() {
        return !isAdvancedSearchMode() && !isBasicSearchMode();
    }

    public List<TaskType> fetchTasks() {
        TaskCommonService service = new TaskCommonService();
        TaskCommonPortType port = service.getTaskCommonPort();

        QueryType query = new QueryType();
        query.setSort(sortField);
        query.setDir(getDefaultOrderDirection(sortField));

        // configure search params
        if (isAdvancedSearchMode()) {
            query.setType("FILTERED"); // NOI18N

            if (hasStatuses()) {
                for (TaskStatus status : statuses) {
                    query.getTaskStatus().add(status);
                }
            } else {
                query.getTaskStatus().add(TaskStatus.ASSIGNED);
                query.getTaskStatus().add(TaskStatus.CLAIMED);
            }

            if (hasUsers()) {
                UsersType queryUsers = new UsersType();
                for (String user : users) {
                    queryUsers.getUser().add(user);
                }
                query.setUsers(queryUsers);
            }

            if (hasGroups()) {
                GroupsType queryGroups = new GroupsType();
                for (String group : groups) {
                    queryGroups.getGroup().add(group);
                }
                query.setGroups(queryGroups);
            }

            if (hasSearchString()) {
                query.setSearchString(searchString);
            }
        } else if (isBasicSearchMode()) {
            query.setType("TEXTSEARCH");  // NOI18N
            query.setSearchString(searchString);
        } else {
            query.setType("DEFAULT"); // NOI18N
        }

        TaskListType taskList = null;
        try {
            taskList = port.getTaskList(query, userName,
                    firstTask, pageSize);
            while ((taskList.getTotalRecords() != 0)
                    && (taskList.getReturnedRecords() == 0)) {

                int newFirstTask = Math.max(0, taskList.getTotalRecords() - 1)
                        / pageSize * pageSize;
                if (newFirstTask == firstTask) {
                    break;
                }
                firstTask = newFirstTask;
                taskList = port.getTaskList(query, userName, firstTask, pageSize);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        List<TaskType> result = null;

        if (taskList != null) {
            totalTaskCount = taskList.getTotalRecords();
            returnedTaskCount = taskList.getReturnedRecords();
            result = taskList.getTask();
        } else {
            firstTask = 0;
            totalTaskCount = 0;
            returnedTaskCount = 0;
        }

        return (result != null) ? result : new ArrayList<TaskType>(0);
    }

    private static int extractPageSize(HttpServletRequest request) {

        int pageSize = 20; // default value

        try {
            pageSize = Integer.parseInt(request.getParameter("size")); // NOI18N

            if (pageSize < 15) {
                pageSize = 10;
            } else if (pageSize < 25) {
                pageSize = 20;
            } else if (pageSize < 40) {
                pageSize = 30;
            } else if (pageSize < 75) {
                pageSize = 50;
            } else {
                pageSize = 100;
            }
        } catch (Exception ex) {
            // Do nothing.
        }

        return pageSize;
    }

    private static int extractFirstTask(HttpServletRequest request, 
            int pageSize)
    {
        int firstTask = 0;
        try {
            firstTask = Integer.parseInt(request.getParameter("start")); // NOI18N

            if (firstTask < 0) {
                firstTask = 0;
            }
        } catch (Exception ex) {
            // Do nothing.
        }

        return (firstTask / pageSize) * pageSize;
    }

    private static SortField extractSortField(HttpServletRequest request) {
        SortField sortField = SortField.ID;
        try {
            sortField = SortField.fromValue(request.getParameter("order")); // NOI18N
        } catch (Exception ex) {
            // Do nothing.
        }

        return sortField;
    }

    private static Set<TaskStatus> extractTaskListStatuses(
            HttpServletRequest request)
    {
        Set<TaskStatus> result = new HashSet<TaskStatus>();

        String[] statuses = request.getParameterValues("status"); // NOI18N

        if (statuses != null) {
            for (String status : statuses) {
                if (status != null) {
                    status = status.trim().toUpperCase();

                    TaskStatus taskStatus = TaskStatus.fromValue(status);
                    if (taskStatus != null) {
                        result.add(taskStatus);
                    }
                }
            }
        }

        return result;
    }

    private static String[] extractTaskListUsers(
            HttpServletRequest request)
    {
        String users = request.getParameter("users"); // NOI18N
        if (users == null) {
            return null;
        }

        users = users.trim();
        if (users.length() == 0) {
            return null;
        }

        String[] result = users.split("\\s+"); // NOI18N
        return result;
    }

    private static String[] extractTaskListGroups(
            HttpServletRequest request)
    {
        String groups = request.getParameter("groups"); // NOI18N
        if (groups == null) {
            return null;
        }

        groups = groups.trim();
        if (groups.length() == 0) {
            return null;
        }

        String[] result = groups.split("\\s+");
        return result;
    }

    private static String extractSearchString(HttpServletRequest request) {
        String result = request.getParameter("search"); // NOI18N
        return (result == null) ? "" : result.trim();
    }

    private static Direction getDefaultOrderDirection(
            final SortField sortField) {

        switch (sortField) {
            case ID:
            case PRIORITY:
            case CREATE_DATE:
                return Direction.DESC;
            //case TITLE:
            //case ASSIGNED_TO:
            //case OWNER:
            //case STATUS:
            //case DEADLINE:
            default:
                return Direction.ASC;
        }
    }

    public static TaskListPage get(HttpServletRequest request) {
        TaskListPage result = (TaskListPage) request.getAttribute(REQUEST_ATTRIBUTE);
        if (result == null) {
            result = new TaskListPage(request);
            request.setAttribute(REQUEST_ATTRIBUTE, result);
        }
        return result;
    }

    public static final String SESSION_ATTRIBUTE
            = "com.sun.glassfishesb.wlm.console.last-query"; // NOI18N

    private static final String REQUEST_ATTRIBUTE 
            = "com.sun.glassfishesb.wlm.console.TaskListPage"; // NOI18N

    private static final String merge(String[] words) {
        if (words == null || words.length == 0) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            builder.append(words[i]);
            if (i + 1 < words.length) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}