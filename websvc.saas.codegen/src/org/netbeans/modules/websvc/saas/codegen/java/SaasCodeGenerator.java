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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.saas.codegen.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
abstract public class SaasCodeGenerator extends AbstractGenerator {

    private FileObject targetFile; // resource file target of the drop
    private JavaSource targetSource;
    private FileObject destDir;
    private Project project;
    protected SaasBean bean;
    private JTextComponent targetComponent;

    public SaasCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, SaasBean bean) {
        this.targetComponent = targetComponent;
        this.targetFile = targetFile;
        this.destDir = targetFile.getParent();
        project = FileOwnerQuery.getOwner(targetFile);

        if (project == null) {
            throw new IllegalArgumentException(targetFile.getPath() + " is not part of a project.");
        }

        targetSource = JavaSource.forFileObject(targetFile);
        String packageName = JavaSourceHelper.getPackageName(targetSource);
        this.bean = bean;
        getBean().setPackageName(packageName);
    }

    protected JTextComponent getTargetComponent() {
        return this.targetComponent;
    }

    protected FileObject getTargetFile() {
        return this.targetFile;
    }
    
    protected JavaSource getTargetSource() {
        return this.targetSource;
    }
    
    protected FileObject getTargetFolder() {
        return this.destDir;
    }
   
    protected Project getProject() {
        return this.project;
    }
    
    protected void preGenerate() throws IOException {
    }
    
    /*
     * Copy File only
     */    
    public void copyFile(String resourceName, File destFile) throws IOException {
        String path = resourceName;
        if(!destFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = this.getClass().getResourceAsStream(path);
                os = new FileOutputStream(destFile);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if(os != null) {
                    os.flush();
                    os.close();
                }
                if(is != null)
                    is.close();            
            }
        }
    }

    abstract protected String getCustomMethodBody() throws IOException;
    
    public SaasBean getBean() {
        return bean;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();
        FileObject[] result = new FileObject[]{targetFile};
        JavaSourceHelper.saveSource(result);

        finishProgressReporting();

        return new HashSet<FileObject>(Arrays.asList(result));
    }

    private String getParamList() {
        List<ParameterInfo> inputParams = bean.filterParametersByAuth
                (bean.filterParameters(new ParamFilter[]{ParamFilter.FIXED}));
        String text = ""; //NOI18N
        for (int i = 0; i < inputParams.size(); i++) {
            ParameterInfo param = inputParams.get(i);

            if (i == 0) {
                text += getParameterName(param, true, true, true);
            } else {
                text += ", " + getParameterName(param, true, true, true); //NOI18N
            }
        }

        return text;
    }

  
    protected static void insert(String s, JTextComponent target, boolean reformat)
    throws BadLocationException {
        Document doc = target.getDocument();
        if (doc == null)
            return;
        
        if (s == null)
            return;
        
        //We dont need to lock the document, since there is no way user can
        //update document during Dnd with a modal dialog
//        if (doc instanceof BaseDocument)
//            ((BaseDocument)doc).atomicLock();
        
        int start = insert(s, target, doc);
        
//        if (reformat && start >= 0 && doc instanceof BaseDocument) {  // format the inserted text
//            BaseDocument d = (BaseDocument) doc;
//            int end = start + s.length();
//            Formatter f = d.getFormatter();
//            
//            //f.reformat(d, start, end);
//            f.reformat(d, 0,d.getLength());
//        }
        
//        if (select && start >= 0) { // select the inserted text
//            Caret caret = target.getCaret();
//            int current = caret.getDot();
//            caret.setDot(start);
//            caret.moveDot(current);
//            caret.setSelectionVisible(true);
//        }
        
        //We dont need to lock the document, since there is no way user can
        //update document during Dnd with a modal dialog
//        if (doc instanceof BaseDocument)
//            ((BaseDocument)doc).atomicUnlock();
    }
    
    protected static int insert(String s, JTextComponent target, Document doc)
    throws BadLocationException {
        
        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {}
        
        return start;
    }
    
    protected boolean isInBlock(JTextComponent target) {
        //TODO - FIX return true if the caret position where code is
        //going to be inserted is within some block other Class block.
        Caret caret = target.getCaret();
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        return true;
    }
    
    public static void createRestConnectionFile(Project project) throws IOException {
        SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(project);
        String pkg = REST_CONNECTION_PACKAGE;
        FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],pkg , true);
        JavaSourceHelper.createJavaSource(REST_CONNECTION_TEMPLATE, targetFolder, pkg, REST_CONNECTION);
        String restResponseTemplate = REST_RESPONSE_TEMPLATE;
        JavaSource restResponseJS = JavaSourceHelper.createJavaSource(restResponseTemplate, targetFolder, pkg, REST_RESPONSE);
    }
    
   
    protected String[] getGetParamNames(List<ParameterInfo> queryParams) {
        ArrayList<String> params = new ArrayList<String>();
        params.addAll(Arrays.asList(getParamNames(queryParams)));
        return params.toArray(new String[params.size()]);
    }
    
    protected String[] getGetParamTypes(List<ParameterInfo> queryParams) {
        ArrayList<String> types = new ArrayList<String>();
        types.addAll(Arrays.asList(getParamTypeNames(queryParams)));
        return types.toArray(new String[types.size()]);
    }
    
    
    protected String[] getParamNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();
        
        for (ParameterInfo param : params) {
            results.add(getParameterName(param, true, true, true));
        }
        
        return results.toArray(new String[results.size()]);
    }
    
    protected String[] getParamTypeNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();
        
        for (ParameterInfo param : params) {
            results.add(param.getTypeName());
        }
        
        return results.toArray(new String[results.size()]);
    }
    
    protected String getParameterName(ParameterInfo param) {
        return Util.getParameterName(param);
    }
    
    protected String getParameterName(ParameterInfo param, 
            boolean camelize, boolean normalize) {
        return Util.getParameterName(param, camelize, normalize, false);
    }
    
    protected String getParameterName(ParameterInfo param, 
            boolean camelize, boolean normalize, boolean trimBraces) {
        return Util.getParameterName(param, camelize, normalize, trimBraces);
    }
    
    protected String getVariableName(String name) {
        return Util.getVariableName(name, true, true, true);
    }
    
    protected String getVariableName(final String name, 
            boolean camelize, boolean normalize, boolean trimBraces) {
        return Util.getVariableName(name, camelize, normalize, trimBraces);
    }
    
    protected Object[] getParamValues(List<ParameterInfo> params) {
        List<Object> results = new ArrayList<Object>();
        
        for (ParameterInfo param : params) {
            Object defaultValue = null;
            
            if (param.getStyle() != ParamStyle.QUERY) {
                defaultValue = param.getDefaultValue();
            }
            
            results.add(defaultValue);
        }
        
        return results.toArray(new Object[results.size()]);
    }
}
