/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.tasks;

import java.util.logging.Logger;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.query.C2CQuery;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.openide.util.RequestProcessor;

/**
 *
 * @author tomas
 */
public class C2C {
    
    private static C2C instance;
    public final static Logger LOG = Logger.getLogger("org.netbeans.modules.c2c.tasks"); // NOI18N
    
    private AbstractRepositoryConnector cfcrc;
    private TaskRepositoryLocationFactory trlf;
    
    private RequestProcessor rp;
    
    public static C2C getInstance() {
        if(instance == null) {
            instance = new C2C();
            instance.init();
        }
        return instance;
    }
    private C2CIssueProvider c2cip;
    private C2CQueryProvider c2cqp;
    private C2CRepositoryProvider c2crp;
    private BugtrackingFactory<C2CRepository, C2CQuery, C2CIssue> bf;

    private void init() {
        AbstractRepositoryConnector rc = C2CExtender.create();
        trlf = new TaskRepositoryLocationFactory();
        C2CExtender.assignTaskRepositoryLocationFactory(rc, trlf);
        cfcrc = rc;
    }
    
    public C2CData getClientData(C2CRepository repository) {
        return C2CExtender.getData(cfcrc, repository.getTaskRepository());
    }
    
    public AbstractRepositoryConnector getRepositoryConnector() {
        return cfcrc;
    }
    
    public BugtrackingFactory<C2CRepository, C2CQuery, C2CIssue> getBugtrackingFactory() {
        if(bf == null) {
            bf = new BugtrackingFactory<C2CRepository, C2CQuery, C2CIssue>();
        }    
        return bf;
    }
    
    public C2CIssueProvider getIssueProvider() {
        if(c2cip == null) {
            c2cip = new C2CIssueProvider();
        }
        return c2cip; 
    }
    public C2CQueryProvider getQueryProvider() {
        if(c2cqp == null) {
            c2cqp = new C2CQueryProvider();
        }
        return c2cqp; 
    }
    public C2CRepositoryProvider getRepositoryProvider() {
        if(c2crp == null) {
            c2crp = new C2CRepositoryProvider();
        }
        return c2crp; 
    }    

    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("C2C", 1, true); // NOI18N
        }
        return rp;
    }
    
}
