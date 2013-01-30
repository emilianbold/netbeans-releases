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
package org.netbeans.libs.oracle.cloud.ext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import oracle.cloud.paas.internal.ServiceInstanceLogCriteriaImpl;
import org.netbeans.libs.oracle.cloud.sdkwrapper.api.ApplicationManager;
import org.netbeans.libs.oracle.cloud.sdkwrapper.exception.*;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.*;

public class ApplicationManagerImpl implements ApplicationManager {

    private final oracle.cloud.paas.api.ApplicationManager realApplicationManager;

    ApplicationManagerImpl(oracle.cloud.paas.api.ApplicationManager realApplicationManager) {
        this.realApplicationManager = realApplicationManager;
        assert this.realApplicationManager != null;
    }
    
    @Override
    public Job deployApplication(String s, String s1, String s2, ApplicationType applicationtype, InputStream inputstream) {
        try {
            return convertJob(realApplicationManager.deployApplication(s, s1, s2, convertApplicationType(applicationtype), inputstream));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public Job redeployApplication(String s, String s1, String s2, InputStream inputstream) {
        try {
            return convertJob(realApplicationManager.redeployApplication(s, s1, s2, inputstream));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public Job undeployApplication(String s, String s1, String s2) {
        try {
            return convertJob(realApplicationManager.undeployApplication(s, s1, s2));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public List<Application> listApplications(String s, String s1) {
        try {
            return convertApplicationList(realApplicationManager.listApplications(s, s1));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public Application describeApplication(String s, String s1, String s2) {
        try {
            return convertApplication(realApplicationManager.describeApplication(s, s1, s2));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public Job startApplication(String s, String s1, String s2) {
        try {
            return convertJob(realApplicationManager.startApplication(s, s1, s2));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public Job stopApplication(String s, String s1, String s2) {
        try {
            return convertJob(realApplicationManager.stopApplication(s, s1, s2));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public List<Job> listJobs() {
        try {
            return convertJobList(realApplicationManager.listJobs());
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public List<Job> listJobs(String s, String s1) {
        try {
            return convertJobList(realApplicationManager.listJobs(s, s1));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public Job describeJob(String s) {
        try {
            return convertJob(realApplicationManager.describeJob(s));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public List<Log> listJobLogs(String s) {
        try {
            return convertLogList(realApplicationManager.listJobLogs(s));
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

    @Override
    public void fetchJobLog(String s, String s1, OutputStream outputstream) {
        try {
            realApplicationManager.fetchJobLog(s, s1, outputstream);
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }


    private static Job convertJob(oracle.cloud.paas.model.Job j) {
        return new Job(j.getJobId(), convertJobStatus(j.getStatus()), j.getStartTime(), j.getEndTime(), j.getOperation());
    }
    
    private static List<Job> convertJobList(List<oracle.cloud.paas.model.Job> jobs) {
        List<Job> nj = new ArrayList<Job>();
        for (oracle.cloud.paas.model.Job j : jobs) {
            nj.add(convertJob(j));
        }
        return nj;
    }

    private static Application convertApplication(oracle.cloud.paas.model.Application j) {
        return new Application(j.getGroupName(), j.getInstanceName(), j.getApplicationName(), 
                convertApplicationType(j.getType()), convertApplicationState(j.getState()), j.getApplicationUrls());
    }
    
    private static List<Application> convertApplicationList(List<oracle.cloud.paas.model.Application> js) {
        List<Application> nj = new ArrayList<Application>();
        for (oracle.cloud.paas.model.Application j : js) {
            nj.add(convertApplication(j));
        }
        return nj;
    }
    
    private static Log convertLog(oracle.cloud.paas.model.Log j) {
        return new Log(j.getName(), j.getContentType());
    }
    
    private static List<Log> convertLogList(List<oracle.cloud.paas.model.Log> js) {
        List<Log> nj = new ArrayList<Log>();
        for (oracle.cloud.paas.model.Log j : js) {
            nj.add(convertLog(j));
        }
        return nj;
    }
    
    private static oracle.cloud.paas.model.ApplicationType convertApplicationType(ApplicationType at) {
        switch (at) {
            case EAR: return oracle.cloud.paas.model.ApplicationType.EAR;
            case WAR: return oracle.cloud.paas.model.ApplicationType.WAR;
        }
        return null;
    }
    
    private static ApplicationType convertApplicationType(oracle.cloud.paas.model.ApplicationType at) {
        switch (at) {
            case EAR: return ApplicationType.EAR;
            case WAR: return ApplicationType.WAR;
        }
        return null;
    }
    
    private static ApplicationState convertApplicationState(oracle.cloud.paas.model.ApplicationState at) {
        switch (at) {
            case STATE_ACTIVE: return ApplicationState.STATE_ACTIVE;
            case STATE_ADMIN: return ApplicationState.STATE_ADMIN;
            case STATE_FAILED: return ApplicationState.STATE_FAILED;
            case STATE_NEW: return ApplicationState.STATE_NEW;
            case STATE_PREPARED: return ApplicationState.STATE_PREPARED;
            case STATE_RETIRED: return ApplicationState.STATE_RETIRED;
            case STATE_UPDATE_PENDING: return ApplicationState.STATE_UPDATE_PENDING;
        }
        return null;
    }
    
    private static JobStatus convertJobStatus(oracle.cloud.paas.model.JobStatus at) {
        switch (at) {
            case CANCELLED: return JobStatus.CANCELLED;
            case COMPLETE: return JobStatus.COMPLETE;
            case FAILED: return JobStatus.FAILED;
            case NEW: return JobStatus.NEW;
            case PAUSED: return JobStatus.PAUSED;
            case RETRY_WAIT: return JobStatus.RETRY_WAIT;
            case RUNNING: return JobStatus.RUNNING;
            case SUBMITTED: return JobStatus.SUBMITTED;
        }
        return null;
    }

    static ManagerException wrapException(oracle.cloud.paas.exception.ManagerException ex) {
        if (ex instanceof oracle.cloud.paas.exception.ResourceBusyException) {
            return new ResourceBusyException(ex);
        }
        if (ex instanceof oracle.cloud.paas.exception.UnknownResourceException) {
            return new UnknownResourceException(ex);
        }
        return new ManagerException(ex);
    }

    @Override
    public InputStream queryServiceInstanceLogs(String s, String s1) {
        try {
            return realApplicationManager.queryServiceInstanceLogs(s, s1, new ServiceInstanceLogCriteriaImpl());
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw wrapException(ex);
        }
    }

}
