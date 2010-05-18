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

package org.netbeans.modules.websvc.registry.jaxrpc;

import java.io.*;
import java.util.*;

import org.openide.util.NbBundle;

public class WSCompileArguments {
    protected String additionalClasspath;
    private List features = new ArrayList();    // List<String>

    /** Holds value of property classpath. */
    private String classpath;

    /** Holds value of property outputDirectory. */
    private String outputDirectory;

    /** What sort of thing to generate: "client", "server", "both". */
    private String gen;

    /** Holds value of property keep. */
    private boolean keep = true;
    
    /** Holds value of property nonclassOutputDirectory. */
    private String nonclassOutputDirectory;
    
    /** Holds value of property sourceOutputDirectory. */
    private String sourceOutputDirectory;
    
    /** Holds value of property define. */
    private boolean define;
    
    /** Holds value of property importGen. */
    private boolean importGen;
    
    /** Holds value of property verbose. */
    private boolean verbose;

    private String mappingFile;
    
    /** Holds value of property configuration. */
    private Configuration configuration;

    private List extraArguments = new LinkedList();

    // The argument type needs no parameters.
    public final static int TYPE_NEED_NO_PARAM = 0;
    // The argument type needs parameters.
    public final static int TYPE_NEED_PARAM = 1;
    // The argument type is uncommon.
    public final static int TYPE_INTERNAL =    0x010;
    // The argument type will cause things to not compile.
    public final static int TYPE_NON_COMPILE = 0x100;

    /**
     * The direct calling of this constructor is discouraged.  Use
     * JAXRPCFactory.getWSCompileArguments instead.
     */
    public WSCompileArguments() {
    }

    /**
     * The direct calling of this constructor is discouraged.  Use
     * JAXRPCFactory.getWSCompileArguments instead.
     */
    public WSCompileArguments(String additionalClasspath) {
        this.additionalClasspath = additionalClasspath;
    }

    public void addArgument(String arg) {
        addArgument(arg, null);
    }

    /**
     * Add an argument to the command line.
     * @param arg the argument
     * @param param any additional parameter to arg.
     */
    public void addArgument(String arg, String param) {
        arg = arg.intern();
        if (arg == "-gen")
            setGen("");
        else if (arg.startsWith("-gen:"))
            setGen(arg.substring(5, arg.length()));
        else if (arg == "-define")
            setDefine(true);
        else if (arg == "-import")
            setImportGen(true);
        else if (arg == "-classpath")
            setClasspath(param);
        else if (arg == "-d")
            setOutputDirectory(param);
        else if (arg == "-nd")
            setNonclassOutputDirectory(param);
        else if (arg == "-s")
            setSourceOutputDirectory(param);
        else if (arg == "-keep")
            setKeep(true);
        else if (arg == "-verbose")
            setVerbose(true);
        else if (arg == "-mapping")
            setMapping(param);
        else {
            if (param == null)
                extraArguments.add(arg);
            else
                extraArguments.add(arg+" "+param);
        }
    }
    
    public void addFeature(String feature) {
        // Deal with mutually exclusive features by removing the old one.
        if ("documentliteral".equals(feature)) {
            features.remove("rpcliteral");
        } else if ("rpcliteral".equals(feature)) {
            features.remove("documentliteral");
        } else if ("unwrap".equals(feature)) {
            features.remove("donotunwrap");
        } else if ("donotunwrap".equals(feature)) {
            features.remove("unwrap");
        }
        features.add(feature);
    }

    public void removeFeature(String feature) {
        features.remove(feature);
    }

    public boolean hasFeature(String feature) {
        return features.contains(feature);
    }
    
    public void setSearchSchemaForSubtypes() {
        features.add("searchschema");
    }
    
    public void setUseDataHandlerOnly() {
        features.add("datahandleronly");
    }
    
    public String[] toArgs() {
        List args = new LinkedList();
        
        if (gen != null) {
            if (define || importGen)
                throw new IllegalStateException(NbBundle.getMessage(WSCompileArguments.class, "MSG_MutuallyExclusiveGenDefineImport"));
            if (gen.equals(""))
                args.add("-gen");
            else {
                args.add("-gen:"+gen);
            }
        }
        if (define) {
            if (gen != null || importGen)
                throw new IllegalStateException(NbBundle.getMessage(WSCompileArguments.class, "MSG_MutuallyExclusiveGenDefineImport"));
            args.add("-define");
        }
        if (importGen) {
            if (gen != null || define)
                throw new IllegalStateException(NbBundle.getMessage(WSCompileArguments.class, "MSG_MutuallyExclusiveGenDefineImport"));
            args.add("-import");
        }
        if (classpath != null) {
            args.add("-classpath");
            args.add(classpath);
        }
        if (mappingFile != null) {
            args.add("-mapping");
            args.add(mappingFile);
        }
        if (outputDirectory != null) {
            args.add("-d");
            args.add(outputDirectory);
        }
        if (nonclassOutputDirectory != null) {
            args.add("-nd");
            args.add(nonclassOutputDirectory);
        }
        if (sourceOutputDirectory != null) {
            args.add("-s");
            args.add(sourceOutputDirectory);
        }
        if (keep)
            args.add("-keep");
        if (verbose)
            args.add("-verbose");
        if (features.size() > 0) {
            String featureArg = "-f:";
            boolean first = true;
            for (Iterator it = features.iterator(); it.hasNext(); ) {
                if (first) 
                    first = false; 
                else 
                    featureArg += ",";
                featureArg += (String) it.next();
            }
            args.add(featureArg);
        }
        
        if (configuration != null) {
            try {
                File cf = File.createTempFile("jaxrpcconfigfile", ".xml");
                cf.deleteOnExit();
                OutputStream out = new FileOutputStream(cf);
                configuration.write(out);
                out.close();
                args.add(cf.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        String[] result = new String[args.size()];
        return (String[]) args.toArray(result);
    }
    
    /** Getter for property classpath.
     * @return Value of property classpath.
     *
     */
    public String getClasspath() {
        return this.classpath;
    }
    
    /** Setter for property classpath.
     * @param classpath New value of property classpath.
     *
     */
    public void setClasspath(String classpath) {
        if (additionalClasspath == null)
            this.classpath = classpath;
        else
            this.classpath = classpath + File.pathSeparator + additionalClasspath;
    }

    public String getMapping() {
        return mappingFile;
    }

    public void setMapping(String m) {
        mappingFile = m;
    }
    
    /** Getter for property outputDirectory.
     * @return Value of property outputDirectory.
     *
     */
    public String getOutputDirectory() {
        return this.outputDirectory;
    }
    
    /** Setter for property outputDirectory.
     * @param outputDirectory New value of property outputDirectory.
     *
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory.getAbsolutePath();
    }
    
    /** Getter for property gen.
     * @return Value of property gen.
     *
     */
    public String getGen() {
        return this.gen;
    }
    
    /** Setter for property gen.
     * @param gen New value of property gen.
     *
     */
    public void setGen(String gen) {
        this.gen = gen;
    }
    
    /** Getter for property keep.
     * @return Value of property keep.
     *
     */
    public boolean isKeep() {
        return this.keep;
    }
    
    /** Setter for property keep.
     * @param keep New value of property keep.
     *
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }
    
    /** Getter for property nonclassOutputDirectory.
     * @return Value of property nonclassOutputDirectory.
     *
     */
    public String getNonclassOutputDirectory() {
        return this.nonclassOutputDirectory;
    }
    
    /** Setter for property nonclassOutputDirectory.
     * @param nonclassOutputDirectory New value of property nonclassOutputDirectory.
     *
     */
    public void setNonclassOutputDirectory(String nonclassOutputDirectory) {
        this.nonclassOutputDirectory = nonclassOutputDirectory;
    }
    
    /** Getter for property sourceOutputDirectory.
     * @return Value of property sourceOutputDirectory.
     *
     */
    public String getSourceOutputDirectory() {
        return this.sourceOutputDirectory;
    }
    
    /** Setter for property sourceOutputDirectory.
     * @param sourceOutputDirectory New value of property sourceOutputDirectory.
     *
     */
    public void setSourceOutputDirectory(String sourceOutputDirectory) {
        this.sourceOutputDirectory = sourceOutputDirectory;
    }
    
    /** Getter for property define.
     * @return Value of property define.
     *
     */
    public boolean isDefine() {
        return this.define;
    }
    
    /** Setter for property define.
     * @param define New value of property define.
     *
     */
    public void setDefine(boolean define) {
        this.define = define;
    }
    
    /** Getter for property importGen.
     * @return Value of property importGen.
     *
     */
    public boolean isImportGen() {
        return this.importGen;
    }
    
    /** Setter for property importGen.
     * @param importGen New value of property importGen.
     *
     */
    public void setImportGen(boolean importGen) {
        this.importGen = importGen;
    }
    
    /** Getter for property verbose.
     * @return Value of property verbose.
     *
     */
    public boolean isVerbose() {
        return this.verbose;
    }
    
    /** Setter for property verbose.
     * @param verbose New value of property verbose.
     *
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public String toString() {
        String[] args = toArgs();
        String result = "";
        for (int i = 0; i < args.length; ++i) {
            if (i > 0)
                result += " ";
            result += args[i];
        }
        return result;
    }
    
    /** Getter for property configuration.
     * @return Value of property configuration.
     *
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    /** Setter for property configuration.
     * @param configuration New value of property configuration.
     *
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a Configuration object (if not there already) and set the WSDL
     * property.
     */
    public void prepConfigurationForWSDL(java.net.URL location, java.lang.String packageName) {
        if (configuration == null)
            configuration = new Configuration();
        if (configuration.getWsdl() == null)
            configuration.setWsdl(new WsdlType(location, packageName));
        else {
            configuration.getWsdl().setLocation(location);
            configuration.getWsdl().setPackageName(packageName);
        }
    }
}
