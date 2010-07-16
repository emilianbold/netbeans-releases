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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>
<%@include file="WEB-INF/includes/common.jsp" %>

<%@include file="WEB-INF/includes/header.jsp" %>

<div id="helpContent">
<a name="gjmms"></a>
<h3>Using the Default Worklist Manager Console</h3>
<p>
    This topic provides instructions for using the default Worklist Manager Console provided with
    the WLM SE. This console is provided primarily for testing and development purposes,
    but you can customize it to fit your needs by installing the Worklist Manager Console sample
    files. You can also create a custom console for handling the Worklist Manager tasks using the
    client API.
</p>
<p>
    The default Worklist Manager Console displays a search box below which any tasks
    that match a search are displayed. By default, tasks are ordered by their
    ID number (which is also the order in which the tasks were submitted).
    The worklist shows the task ID, title, status, submission date, users and groups
    to which it is assigned, the user who has claimed the task, and
    the deadline for completion.
</p>
<p>
    Task IDs are highlighted based on the task priority.
</p>
<ul>
    <li>
        <p>
            Red indicates a high priority (8&#8211;10).
        </p>
    </li>
    <li>
        <p>
            Yellow indicates a normal priority (4&#8211;7).
        </p>
    </li>
    <li>
        <p>
            Blue indicates a low priority (1&#8211;3).
        </p>
    </li>
</ul>
<p>
    Typical steps for completing a task are searching for the task, claiming the
    task, and then marking it as complete. The last two steps can be
    combined into one. Perform any of the following procedures to manage worklist tasks:
</p>
<ul>
    <li>
        <p>
            <a href="#ug_wlmse-search_t">Searching for Tasks</a>
        </p>
    </li>
    <li>
        <p>
            <a href="#ug_wlmse-claim_t">Claiming a Task</a>
        </p>
    </li>
    <li>
        <p>
            <a href="#ug_wlmse-complete_t">Completing a Claimed Task</a>
        </p>
    </li>
    <li>
        <p>
            <a href="#ug_wlmse-reassign_t">Reassigning a Task</a>
        </p>
    </li>
</ul>

<a name="ug_wlmse-search_t"></a>
<h3>Searching for Tasks</h3>
<p>
    The WLM SE supports full-text and keyword searches or Worklist Manager tasks.
</p>

<a name="gjkut"></a>
<h4>To Search for Tasks</h4>
<ol>
    <li>
        <b>To perform a basic search, enter a search term into the Search field
            and then click Search.</b>
        <p>
            Any tasks that contain the search criteria and that do not have a
            status of Completed appear in the task list.
        </p>
    </li>
    <li>
        <b>To perform an advanced search, do the following:</b>
        <ol style="list-style-type: lower-alpha">
            <li>
                <b>In the Search box, click Advanced Search.</b>
                <p>
                    The Advanced Search fields appear.
                </p>
            </li>
            <li>
                <b>Enter any of the following information for the search:</b>
                <ul>
                    <li>
                        <p>
                            <b>Task Statuses</b>: Select one or more of the following: Assigned,
                            Claimed, Escalated, Completed, Expired, and Failed. Use the Ctrl and
                            Shift keys to select multiple statuses.
                        </p>
                    </li>
                    <li>
                        <p>
                            <b>Task Owners</b>: Specify the names of users and groups who own the
                            tasks you want to find. If you specify more than one user or group,
                            separate the names with a space.
                        </p>
                    </li>
                    <li>
                        <p>
                            <b>Text Search</b>: Enter a word or words by which to search.
                        </p>
                    </li>
                </ul>
            </li>
            <li>
                <b>When you have entered all of your criteria, click Search.</b><p>
                    Any tasks matching the criteria appear in the task list.
                </p>
            </li>
        </ol>
    </li>
    <li>
        <b>Once you perform a search, you can do any of the following:</b>
        <ul>
            <li>
                <b>To repeat the previous query, click Last Query above the Search box.</b>
            </li>
            <li>
                <b>To view all tasks, click All Tasks above the Search box.</b>
            </li>
            <li>
                <b>To view only the tasks you have claimed, click My Tasks above the
                    Search box.</b>
            </li>
            <li>
                <b>To re-sort the results of a search, click in the Sort By field
                    and then select the column by which you want to sort.</b>
            </li>
            <li>
                <b>To navigate back and forth through the worklist, use the Previous and Next
                    buttons or the button corresponding to the page you want to view.</b>
            </li>
            <li>
                <b>To change the number of tasks display on each page, select a new
                    value from the Tasks per Page field.</b>
            </li>
        </ul>
    </li>
</ol>

<a name="ug_wlmse-claim_t"></a>
<h3>Claiming a Task</h3>
<p>
    Once a task you want to complete appears in the task list,
    you can claim the task. You can only complete tasks you have claimed, and
    you can claim and complete the task at the same time that
    you claim it.
</p>

<a name="gjkts"></a>
<h4>To Claim a Task</h4><ol>
    <li>
        <b>In the task list, select the task you want to claim.</b>
        <p>
            The page changes to show the task information.
        </p>
    </li>
    <li>
        <b>Click Claim.</b>
        <p>
            Additional information for the task appears on the page, the Claimed By property
            changes to your user name, and the task status changes to Claimed.
        </p>
    </li>
    <li>
        <b>Do one of the following:</b>
        <ul>
            <li>
                <b>To modify the task output without completing the task, make the necessary
                    changes and then click Save.</b>
                <p>
                    Any changes you made to the output are saved.
                </p>
            </li>
            <li>
                <b>To complete the task, make the necessary changes to the task output and
                    then click Save and Complete.</b>
                <p>
                    The status for the task changed the Completed.
                </p>
            </li>
            <li>
                <b>If you do not want to claim the task after all, click Revoke.</b>
                <p>
                    The Claimed By property is cleared and the task status changes back to
                    Assigned.
                </p>
            </li>
        </ul>
    </li>
</ol>

<a name="ug_wlmse-complete_t"></a>
<h3>Completing a Claimed Task</h3>
<p>
    If you did not complete a task at the time you claimed
    it, you can return to the task and update the required information to
    complete the task.
</p>

<a name="gjkth"></a>
<h4>To Complete a Task</h4>
<ol>
    <li>
        <b>In the task worklist, click on a task you have claimed (the task
            status will be Claimed).</b>
    </li>
    <li>
        <b>In the response section of the window, modify any necessary information in the
            message.</b>
    </li>
    <li>
        <b>Click Save and Complete.</b>
        <p>
            The status for the task changes to Complete.
        </p>
        <hr/>
        <p class="Note">
            <b>Note - </b>The task remains in the Worklist Manager database, and can be viewed by
            searching for tasks with a status of Complete.
        </p>
        <hr/>
    </li>
</ol>

<a name="ug_wlmse-reassign_t"></a>
<h3>Reassigning a Task</h3>
<p>
    At times it might be necessary to reassign a task to someone other
    than yourself. This can only be done before a task is claimed.
</p>

<a name="gjkxy"></a>
<h4>To Reassign a Task</h4>
<ol>
    <li>
        <b>Perform a search for the task to reassign.</b>
    </li>
    <li>
        <b>In the worklist, select the task to reassign.</b>
    </li>
    <li>
        <b>Click Reassign.</b>
        <p>
            Two fields appear so you can reassign the task to a user or
            a group.
        </p>
    </li>
    <li>
        <b>Enter the user or group name to which the task should be reassigned.</b>
    </li>
    <li>
        <b>Click Reassign again.</b>
        <p>
            The task's Assigned To properties change to user or group names you entered,
            and the task worklist reappears.
        </p>
    </li>
</ol>
</div>

<%@include file="WEB-INF/includes/footer.jsp"%>
