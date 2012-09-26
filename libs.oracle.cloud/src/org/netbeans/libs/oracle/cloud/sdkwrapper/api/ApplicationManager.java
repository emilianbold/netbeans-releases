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
package org.netbeans.libs.oracle.cloud.sdkwrapper.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.Application;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.ApplicationType;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.Job;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.Log;

public interface ApplicationManager extends JobManager {

    public Job deployApplication(String s, String s1, String s2, ApplicationType applicationtype, InputStream inputstream);

    public Job redeployApplication(String s, String s1, String s2, InputStream inputstream);

    public Job undeployApplication(String s, String s1, String s2);

    public List<Application> listApplications(String s, String s1);

    public Application describeApplication(String s, String s1, String s2);

    public Job startApplication(String s, String s1, String s2);

    public Job stopApplication(String s, String s1, String s2);

    public InputStream queryServiceInstanceLogs(String s, String s1);
    
}
