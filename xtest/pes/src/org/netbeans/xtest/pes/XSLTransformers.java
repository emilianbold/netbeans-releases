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
 * XSLTTransformers.java
 *
 * Created on June 4, 2002, 4:04 PM
 */

package org.netbeans.xtest.pes;

import java.util.logging.Level;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.netbeans.xtest.util.XSLUtils;
import java.io.*;
import org.netbeans.xtest.pes.xmlbeans.*;
import org.netbeans.xtest.pe.*;

/**
 *
 * @author  mb115822
 */
public class XSLTransformers {

    private static final String PROJECTS_SUMMARY_XSL = "server/projects-summary.xsl";
    private static final String BUILDS_SUMMARY_XSL = "server/builds-summary.xsl";
    private static final String BUILD_SUMMARY_XSL = "server/build-group-type-summary.xsl";
    private static final String BUILDS_HISTORY_MATRIX_XSL = "server/history-matrix.xsl";
    // server stuff
    private static final String MAIN_NAVIGATOR_SERVER_XSL = "server/main-navigator-server.xsl";
    
    private static Transformer projectsSummaryTransformer;
    private static Transformer buildsSummaryTransformer;
    private static Transformer buildSummaryTransformer;
    private static Transformer buildsHistoryMatrixTransformer;
    private static Transformer mainNavigatorServerTransformer;
    
    // xalan bug indicator
    private static boolean XALAN_BUG=true;
    
    // this object has only static methods
    private XSLTransformers() {
    }
    
    // my method, because I don't want to use XTest's transformers (Classloaders issue)
    public static Transformer getTransformer(File xslFile) throws TransformerConfigurationException {
        TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
        StreamSource xslSource = new StreamSource(xslFile);
        return tFactory.newTransformer(xslSource);
    }
    
    private static Transformer getTransformer(Transformer transformer, String transformerFilename) throws IOException, TransformerConfigurationException {
        if ((transformer == null)|(XALAN_BUG)) {
            File xslFile = XSLUtils.getXSLFile(PEServer.getXTestHome(),transformerFilename);
            return getTransformer(xslFile);
        } else {
            transformer.clearParameters();
            return transformer;
        }
    }
    
    public static Transformer getProjectsSummaryTransformer() throws IOException,TransformerConfigurationException {
        projectsSummaryTransformer = getTransformer(projectsSummaryTransformer,PROJECTS_SUMMARY_XSL);
        return projectsSummaryTransformer;
    }
    
    public static Transformer getBuildsSummaryTransformer() throws IOException,TransformerConfigurationException {
        buildsSummaryTransformer = getTransformer(buildsSummaryTransformer,BUILDS_SUMMARY_XSL);
        return buildsSummaryTransformer;
    }
    
    public static Transformer getBuildSummaryTransformer() throws IOException,TransformerConfigurationException {
        buildSummaryTransformer = getTransformer(buildSummaryTransformer,BUILD_SUMMARY_XSL);
        return buildSummaryTransformer;
    }
    
    public static Transformer getBuildsHistoryMatrixTransformer() throws IOException,TransformerConfigurationException {
        buildsHistoryMatrixTransformer = getTransformer(buildsHistoryMatrixTransformer,BUILDS_HISTORY_MATRIX_XSL);
        return buildsHistoryMatrixTransformer;
    }
    
    public static Transformer getMainNavigatorServerTransformer() throws IOException,TransformerConfigurationException {
        mainNavigatorServerTransformer = getTransformer(mainNavigatorServerTransformer,MAIN_NAVIGATOR_SERVER_XSL);
        return mainNavigatorServerTransformer; 
    }
    
    
    public static void transformResultsForServer(File reportRoot, boolean truncated, boolean includeIDELog, boolean includeExceptions, 
        String mappedHostname, PESProjectGroup reportGroup, boolean retransform)
                throws IOException, TransformerConfigurationException, TransformerException {
            
        if (retransform) {
            
            TransformXMLTask.setTruncated(truncated);
            TransformXMLTask.setMappedHostname(mappedHostname);
            TransformXMLTask.setIncludeIDELog(includeIDELog);
            TransformXMLTask.setIncludeExceptions(includeExceptions);
            TransformXMLTask.transformResults(reportRoot,reportRoot);
            
            File testruns[] = reportRoot.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(PEConstants.TESTRUN_DIR_PREFIX);
                }
            });
            for (int i=0; i<testruns.length; i++) {
                
                TransformXMLTask.transformResults(testruns[i],testruns[i]);
                
                File testbags[] = testruns[i].listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith(PEConstants.TESTBAG_DIR_PREFIX);
                    }
                });
                for (int j=0; j<testbags.length; j++) {
                    
                    TransformXMLTask.transformResults(testbags[j],testbags[j]);                    
                    
                }
            }
            
        } else {
            if (mappedHostname != null) {
                TransformXMLTask.setTruncated(truncated);
                TransformXMLTask.setMappedHostname(mappedHostname);
                TransformXMLTask.transformResults(reportRoot,reportRoot);                
            }
        }
        // now try to transform the control frame of the processed report, so
        // it will include links to histories and back to parent pages
        File mnInputDir = ResultsUtils.getXMLResultDir(reportRoot);
        File mnInputFile = new File(mnInputDir,PEConstants.TESTREPORT_XML_FILE);
       
        File mnOutputDir = ResultsUtils.getHTMLResultDir(reportRoot);
        File mnOutputFile = new File(mnOutputDir,PEConstants.MAIN_NAVIGATOR_HTML_FILE);       
              
        Transformer mnTransformer = getMainNavigatorServerTransformer();
        mnTransformer.setParameter("webGroupName",reportGroup.getName());
        mnTransformer.setParameter("mainGroup",""+reportGroup.isMain());
        mnTransformer.setParameter("includeExceptions", ""+includeExceptions);
        mnTransformer.setParameter("includeIDELog",""+includeIDELog);        
        if (mappedHostname != null) {
            mnTransformer.setParameter("mappedHostname", mappedHostname);
        }
        try {
            XSLUtils.transform(mnTransformer,mnInputFile,mnOutputFile);
        } catch (RuntimeException re) {
            PESLogger.logger.log(Level.FINE, "Transformation of "+mnInputFile+" to "+mnOutputFile+" failed!", re);
        }
    }
    
}
