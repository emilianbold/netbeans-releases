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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.lib.cvsclient.request;

/**
 * The request for a command.
 * Always a response is expected.
 *
 * @author  Thomas Singer
 */
public class CommandRequest extends Request {

    public static final CommandRequest ADD = new CommandRequest("add\n"); //NOI18N
    public static final CommandRequest ANNOTATE = new CommandRequest("annotate\n"); //NOI18N
    public static final CommandRequest CHECKOUT = new CommandRequest("co\n"); //NOI18N
    public static final CommandRequest COMMIT = new CommandRequest("ci\n"); //NOI18N
    public static final CommandRequest DIFF = new CommandRequest("diff\n"); //NOI18N
    public static final CommandRequest EDITORS = new CommandRequest("editors\n"); //NOI18N
    public static final CommandRequest EXPORT = new CommandRequest("export\n"); //NOI18N
    public static final CommandRequest HISTORY = new CommandRequest("history\n"); //NOI18N
    public static final CommandRequest IMPORT = new CommandRequest("import\n"); //NOI18N
    public static final CommandRequest LOG = new CommandRequest("log\n"); //NOI18N
    public static final CommandRequest NOOP = new CommandRequest("noop\n"); //NOI18N
    public static final CommandRequest RANNOTATE = new CommandRequest("rannotate\n"); //NOI18N
    public static final CommandRequest REMOVE = new CommandRequest("remove\n"); //NOI18N
    public static final CommandRequest RLOG = new CommandRequest("rlog\n"); //NOI18N
    public static final CommandRequest RTAG = new CommandRequest("rtag\n"); //NOI18N
    public static final CommandRequest STATUS = new CommandRequest("status\n"); //NOI18N
    public static final CommandRequest TAG = new CommandRequest("tag\n"); //NOI18N
    public static final CommandRequest UPDATE = new CommandRequest("update\n"); //NOI18N
    public static final CommandRequest WATCH_ADD = new CommandRequest("watch-add\n"); //NOI18N
    public static final CommandRequest WATCH_ON = new CommandRequest("watch-on\n"); //NOI18N
    public static final CommandRequest WATCH_OFF = new CommandRequest("watch-off\n"); //NOI18N
    public static final CommandRequest WATCH_REMOVE = new CommandRequest("watch-remove\n"); //NOI18N
    public static final CommandRequest WATCHERS = new CommandRequest("watchers\n"); //NOI18N

    private final String request;

    private CommandRequest(String request) {
        this.request = request;
    }

    /**
     * Get the request String that will be passed to the server.
     */
    public String getRequestString() {
        return request;
    }

    /**
     * Returns true if a response from the server is expected.
     */
    public boolean isResponseExpected() {
        return true;
    }
}
