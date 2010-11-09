/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.support;

import java.sql.SQLException;

/**
 * 
 * @author ak119685
 */
public interface SQLRequestsProcessor {

    /**
     * Put SQLRequest into requests queue.
     * Note that in general successful execution of the method doesn't necessary
     * mean that request will ever be executed. If DB is closed before the
     * request is dequeued, it will be disregarded.
     * 
     * @param request Request to queue
     * @return true if request was successfully queued, false if no place in
     * queue available
     */
    public boolean queueRequest(SQLRequest request);

    /**
     * Executes request in the calling thread without puting it into queue
     * 
     * @param request Request to execute
     * @throws SQLException
     */
    public void processRequest(SQLRequest request) throws SQLException;

    /**
     * Waits for requests queue to become empty. It is guaranteed that flush()
     * returns not earlier than queued (in the same execution thread) requests
     * are processed.
     */
    public void flush();
}
