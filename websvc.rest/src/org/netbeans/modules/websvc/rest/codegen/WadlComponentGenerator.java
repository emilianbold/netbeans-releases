/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.model.WADLBasedResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.JaxwsOperationInfo;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class WadlComponentGenerator extends RestComponentGenerator {

    public WadlComponentGenerator(FileObject targetFile, RestComponentData data) throws IOException {
        this(targetFile, new WADLBasedResourceBean(data));
    }
    
    public WadlComponentGenerator(FileObject targetFile, WADLBasedResourceBean bean) {
        super(targetFile, bean);
        this.targetFile = targetFile;
        this.destDir = targetFile.getParent();
        project = FileOwnerQuery.getOwner(targetFile);
        if (project == null) {
            throw new IllegalArgumentException(targetFile.getPath() + " is not part of a project.");
        }
        targetResourceJS = JavaSource.forFileObject(targetFile);
        String packageName = JavaSourceHelper.getPackageName(targetResourceJS);
        bean.setPackageName(packageName);
        bean.setPrivateFieldForQueryParam(true);
        this.bean = bean;
        wrapperResourceFile = SourceGroupSupport.findJavaSourceFile(project, bean.getName());

        getSubResourceMethodName = "get" + Util.upperFirstChar(bean.getShortName());
    }
    
    public Map<String,String> getInputParameterTypes() {
        Map<String,String> ret = new HashMap<String,String>();
        //ret.put(names[i], types[i]);  //TODO duplicate param names b/w operations
        return ret;
    }
    
    public void setConstantInputValues(Map<String,Object> constantParamValues) {
        ((WADLBasedResourceBean)bean).setConstantParams(constantParamValues);
    }
    
    public boolean needsInputs() {
        return this.bean.getQueryParams().length > 0;
    }
    
    /**
     *  Return target and generated file objects
     */
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);
        
        RestUtils.createRestConnection(this.destDir, bean.getPackageName());
        FileObject outputWrapperFO = generateJaxbOutputWrapper();
        jaxbOutputWrapperJS = JavaSource.forFileObject(outputWrapperFO);
        generateComponentResourceClass();
        addSubResourceMethod();
        FileObject refConverterFO = getOrCreateGenericRefConverter().getFileObjects().iterator().next();
        modifyTargetConverter();
        putFocusOnTargetFile();
        FileObject[] result = new FileObject[] { targetFile, wrapperResourceFile, refConverterFO, outputWrapperFO };
        JavaSourceHelper.saveSource(result);
      
        finishProgressReporting();
        
        return new HashSet<FileObject>(Arrays.asList(result));
    }
        
    public FileObject generateJaxbOutputWrapper() throws IOException {
        FileObject converterFolder = getConverterFolder();
        String packageName = SourceGroupSupport.packageForFolder(converterFolder);
        ((WADLBasedResourceBean)bean).setOutputWrapperPackageName(packageName);
        String[] returnTypeNames = ((WADLBasedResourceBean)bean).getOutputTypes();
        XmlOutputWrapperGenerator gen = new XmlOutputWrapperGenerator(
                converterFolder, ((WADLBasedResourceBean)bean).getOutputWrapperName(), packageName, returnTypeNames);
        return gen.generate();
    }
    
    public void modifyGETMethod() throws IOException {
        ModificationResult result = wrapperResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                String converterType = JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
                JavaSourceHelper.addImports(copy, new String[] { converterType });

                String converterName = converterType.substring(converterType.lastIndexOf('.')+1);
                String methodBody = "{" + getOverridingStatements(); //NOI18N
                
                String paramStr;
                StringBuffer sb1 = new StringBuffer();
                String[] params = bean.getQueryParams();
                String[] paramTypes = bean.getQueryParamTypes();
                for (int i = 0; i<params.length; i++) {
                    String param = params[i];
                    if(!paramTypes[i].equals("String"))
                        sb1.append("{\""+param+"\", "+param+".toString()},");
                    else
                        sb1.append("{\""+param+"\", "+param+"},");
                }
                paramStr = sb1.toString();
                if(bean.getQueryParams().length > 0)
                    paramStr = paramStr.substring(0, paramStr.length()-1);
                methodBody += "String url = \""+((WADLBasedResourceBean)bean).getUrl()+"\";\n";
                methodBody += "        "+converterName+" converter = new "+converterName+"();\n";
                methodBody += "        try {\n";
                methodBody += "             RestConnection cl = new RestConnection();\n";
                methodBody += "             String[][] params = new String[][]{\n";
                methodBody += "                 "+paramStr+"\n";
                methodBody += "             };\n";
                methodBody += "             String result = cl.connect(url, params);\n";
                methodBody += "             converter.setString(result);\n";
                methodBody += "             return converter;\n";
                methodBody += "        } catch (java.io.IOException ex) {\n";
                methodBody += "             java.util.logging.Logger.getLogger("+converterName+".class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n";
                methodBody += "        }\n";
                methodBody += "        return converter; }";
                MethodTree methodTree = JavaSourceHelper.getMethodByName(copy, "getXml"); //NOI18
                JavaSourceHelper.replaceMethodBody(copy, methodTree, methodBody);
            }
        });
        result.commit();
    }    
    
    public void generateComponentResourceClass() throws IOException {
        
        //setupWebServiceClient();
        if (wrapperResourceFile == null) {
            GenericResourceGenerator delegate = new GenericResourceGenerator(destDir, bean);
            delegate.setTemplate(RESOURCE_TEMPLATE);
            Set<FileObject> files = delegate.generate(getProgressHandle());
        
            if (files == null || files.size() == 0) {
                return;
            }
            wrapperResourceFile = files.iterator().next();
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            modifyGETMethod();
        } else {
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            ((WADLBasedResourceBean)bean).initConstantParams(wrapperResourceJS);
        }
    }    
}
