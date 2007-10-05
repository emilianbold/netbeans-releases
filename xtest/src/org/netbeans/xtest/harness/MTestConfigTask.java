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
package org.netbeans.xtest.harness;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.xtest.xmlserializer.XMLSerializeException;

/**
 *
 * @author  lm97939
 */
public class MTestConfigTask extends Task {

    private String config;
    private String testtype;
    private static Vector singletest = new Vector();
    private HashSet passed_attribset = new HashSet();
    private HashSet passed_patternattribs = new HashSet();
    private HashSet passed_executors = new HashSet();
    private HashSet passed_testbags = new HashSet();
    
    private Vector additionalPatterns = new Vector();
    
    private static MTestConfig mconfig;
    
    /** Creates a new instance of MTestConfigTask */
    public MTestConfigTask() {
    }
    
        // xml element
    public void setTesttype (String s) {
        testtype = s;
    }       
    
    // xml element
    public void setTestattributes (String s) {        
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_attribset.add( stt.nextToken() );
        }
    }
    
    // xml element
    public void setPatternsetattribs (String s) {        
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_patternattribs.add( stt.nextToken() );
        }
    }
    
    // xml element
    public void setTestexecutors (String s) {
        if (s == null || s.equals("")) return;
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_executors.add( stt.nextToken() );
        }
    } 
    
    // xml element
    public void setTestbags (String s) {
        if (s == null || s.equals("")) return;
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_testbags.add( stt.nextToken() );
        }
    }
    
    // xml element
    public void setTestconfig (String config) {
        this.config = config;
    }
    
    // xml element
    public void setSingleTest (String s) {
        if (s == null || s.equals("")) return;
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            singletest.add( stt.nextToken() );
        }
    }
    
    public PatternSet createPatternSet() {
        PatternSet patterns = new PatternSet();
        additionalPatterns.addElement(patterns);
        return patterns;
    }
    
    public static MTestConfig getMTestConfig() {
        return mconfig;
    }
    
    public static Testbag[] getTestbags() throws BuildException {
        if (!singletest.isEmpty()) {
            try {
                return new Testbag[] { mconfig.createSingleTestbag(mconfig.getTesttype()+"/src", singletest) };
            }
            catch (XMLSerializeException e) {
                throw new BuildException(e.getMessage(), e);
            }
        } else {
            return mconfig.getFilteredTestbags();
        }
    }
    
    
    public void execute() throws BuildException {
        File configfile;
        if (testtype == null)
            throw new BuildException("Attribute testtype is required.");
        if (passed_attribset.isEmpty() && passed_testbags.isEmpty())
            throw new BuildException("Either attribute testattributes or testbags is required.");
        if (config != null && !config.equals("")) {
            configfile = getProject().resolveFile(config);
            if (!configfile.exists() || !configfile.isFile())
                throw new BuildException("Config file "+configfile.getAbsolutePath()+" doesn't exist or is not a file.");
        } else {
            configfile = new File(getProject().getBaseDir(), "cfg-" + testtype + ".xml");
        }
        try {
            mconfig = MTestConfig.loadConfig(configfile, passed_patternattribs, passed_attribset, passed_executors, passed_testbags);
            mconfig.setTesttype(testtype);
            ArrayList includes_list = new ArrayList();
            ArrayList excludes_list = new ArrayList();
            Enumeration en = additionalPatterns.elements();
            while (en.hasMoreElements()) {
                PatternSet pattern = (PatternSet)en.nextElement();
                String include[] = pattern.getIncludePatterns(getProject());
                String exclude[] = pattern.getExcludePatterns(getProject());
                if (include != null)
                    includes_list.addAll(Arrays.asList(include));
                if (exclude != null)
                    excludes_list.addAll(Arrays.asList(exclude));
            }
            mconfig.setAdditionalIncludes((String[])includes_list.toArray(new String[0]));
            mconfig.setAdditionalExcludes((String[])excludes_list.toArray(new String[0]));
        }
        catch (XMLSerializeException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
}
