/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * XTestResultsReportTask.java
 *
 * Created on December 3, 2001, 2:43 PM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.io.*;
import org.w3c.dom.*;
import java.util.*;
import org.netbeans.xtest.util.SerializeDOM;

/**
 *
 * @author  mb115822
 * @version
 */
public class XTestResultsReportTask extends Task{

    /** Creates new XTestResultsReportTask */
    public XTestResultsReportTask() {
    }

    private File outfile;
    private String project;
    private String build;
    private String testingGroup;
    private String testedType;
    private String host;
    private String project_id;
    private String team;
    
    private String scannedPropertiesName = null;
    
    
    public void setTestingGroup(String testingGroup) {
        this.testingGroup = testingGroup;
    }
    
    public void setTestedType(String testedType) {
        this.testedType = testedType;
    }

    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    public void setProject(String project) {
        this.project = project;
    }    
    
    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }
    
    public void setTeam(String team) {
        if ((team != null) & (team.length() > 0)) {
            this.team = team;
        } else {
            this.team = null;
        }
    }
    
    public void setBuild(String build) {
        this.build = build;
    }
    
    public void setHost(String host) {
        if (host.startsWith("${")|host.equals("")) {
            this.host = SystemInfo.getCurrentHost();
        } else {
            this.host = host;
        }
    }
    
    public XTestResultsReport getReport() {
        XTestResultsReport report = new XTestResultsReport();
        report.xmlat_project = project;
        report.xmlat_project_id = project_id;
        report.xmlat_build = build;
        report.xmlat_testingGroup = testingGroup;
        report.xmlat_testedType = testedType;
        report.xmlat_host = host;
        report.xmlat_team = team;
        report.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        return report;
    }
    
    public void execute () throws BuildException {
        log("Generating test report info xml");
        XTestResultsReport report;
        try {
            Document doc = SerializeDOM.parseFile(this.outfile);
            report = (XTestResultsReport)XMLBean.getXMLBean(doc);
            log("Test report info xml already exists - skipping");
            return;
        } catch (Exception e) {            
            report = getReport();
        }
        //System.err.println("TR:"+tr);
        try {
            FileOutputStream outStream = new FileOutputStream(this.outfile);
            SerializeDOM.serializeToStream(report.toDocument(),outStream);
            outStream.close();
        } catch (IOException ioe) {
            log("Cannot save test report:"+ioe);
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            log("XMLBean exception?:"+e);
            e.printStackTrace(System.err);           
        }
    }

}
