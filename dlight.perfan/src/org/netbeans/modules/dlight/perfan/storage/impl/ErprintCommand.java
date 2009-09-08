/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.storage.impl;

/**
 *
 * @author ak119685
 */
public class ErprintCommand {

    private static ErprintCommand _functions = new ErprintCommand("functions"); // NOI18N
    private static ErprintCommand _metrics = new ErprintCommand("metrics"); // NOI18N
    private static ErprintCommand _statistics = new ErprintCommand("statistics"); // NOI18N
    private static ErprintCommand _lines = new ErprintCommand("lines"); // NOI18N
    private static ErprintCommand _threads = new ErprintCommand("threads"); // NOI18N
    private static ErprintCommand _thread_list = new ErprintCommand("thread_list"); // NOI18N
    private static ErprintCommand _object_list = new ErprintCommand("object_list"); // NOI18N
    private static ErprintCommand _leaks = new ErprintCommand("leaks"); // NOI18N
    private static ErprintCommand _rdetail_all = new ErprintCommand("rdetail all"); // NOI18N
    private static ErprintCommand _ddetail_all = new ErprintCommand("ddetail all"); // NOI18N
    private final String cmd;

    private ErprintCommand(String cmd, String... args) {
        StringBuilder sb = new StringBuilder(cmd);
        for (String arg : args) {
            sb.append(" \"").append(arg).append("\""); // NOI18N
        }
        this.cmd = sb.toString();
    }

    public String getCmd() {
        return cmd;
    }

    public static ErprintCommand limit(int limit) {
        return new ErprintCommand("limit", Integer.toString(limit)); // NOI18N
    }

    public static ErprintCommand object_select(String obj) {
        return new ErprintCommand("object_select", obj); // NOI18N
    }

    public static ErprintCommand fsingle(String fname, int choice) {
        return new ErprintCommand("fsingle", fname, Integer.toString(choice)); // NOI18N
    }

    public static ErprintCommand filter(String filter_spec) {
        return new ErprintCommand("filter", filter_spec); // NOI18N
    }

    public static ErprintCommand fsingle(String fname) {
        return new ErprintCommand("fsingle", fname); // NOI18N
    }

    public static ErprintCommand rdetail_all() {
        return _rdetail_all;
    }

    public static ErprintCommand ddetail_all() {
        return _ddetail_all;
    }

    public static ErprintCommand functions() {
        return _functions;
    }

    public static ErprintCommand lines() {
        return _lines;
    }

    public static ErprintCommand leaks() {
        return _leaks;
    }

    public static ErprintCommand metrics() {
        return _metrics;
    }

    public static ErprintCommand metrics(String mspec) {
        return new ErprintCommand("metrics", mspec); // NOI18N
    }

    public static ErprintCommand sort(String msort) {
        return new ErprintCommand("sort", msort); // NOI18N
    }

    public static ErprintCommand statistics() {
        return _statistics;
    }

    public static ErprintCommand threads() {
        return _threads;
    }

    public static ErprintCommand thread_list() {
        return _thread_list;
    }

    public static ErprintCommand object_list() {
        return _object_list;
    }
}
