/*
 * StrutsConfigUtilities.java
 *
 * Created on August 5, 2005, 10:43 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.struts;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.struts.config.model.Action;
import org.netbeans.modules.web.struts.config.model.ActionMappings;
import org.netbeans.modules.web.struts.config.model.MessageResources;
import org.netbeans.modules.web.struts.config.model.StrutsConfig;
import org.netbeans.modules.web.struts.config.model.FormBeans;
import org.netbeans.modules.web.struts.config.model.FormBean;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author petr
 */
public class StrutsConfigUtilities {
    
    public static String DEFAULT_MODULE_NAME = "config"; //NOI18N
    private static final int TYPE_ACTION=0;
    private static final int TYPE_FORM_BEAN=1;
    private static final int TYPE_MESSAGE_RESOURCES=2;
    
    public static List getAllActionsInModule(StrutsConfigDataObject data){
        return createConfigElements(TYPE_ACTION,data);
    }
    
    public static List getAllFormBeansInModule(StrutsConfigDataObject data){
        return createConfigElements(TYPE_FORM_BEAN,data);
    }
    
    public static List getAllMessageResourcesInModule(StrutsConfigDataObject data){
        return createConfigElements(TYPE_MESSAGE_RESOURCES,data);
    }
    
    private static List createConfigElements(int elementType, StrutsConfigDataObject data) {
        FileObject config = data.getPrimaryFile();
        ArrayList list = new ArrayList();
        WebModule wm = WebModule.getWebModule(config);
        if (wm != null){
            FileObject ddFo = wm.getDeploymentDescriptor();
            if (ddFo != null){
                String moduleName = getModuleName(config, ddFo);
                if (moduleName == null){
                    // the conf file is not in any module (is not declared in the web.xml)
                    try{
                        StrutsConfig sConfig = data.getStrutsConfig();
                        switch (elementType) {
                            case TYPE_ACTION : addActions(list, sConfig);break;
                            case TYPE_FORM_BEAN : addFormBeans(list, sConfig);break;
                            case TYPE_MESSAGE_RESOURCES : addMessageResource(list, sConfig);break;
                        }
                    } catch (java.io.IOException e){
                        // Do nothing
                    }
                } else {
                    // the config file is in a Struts module, returns all actions from the
                    // conf files in the module
                    FileObject[] configs = getConfigFiles(moduleName, ddFo);
                    DataObject dOb;
                    for (int i = 0; i < configs.length; i++){
                        try{
                            dOb = DataObject.find(configs[i]);
                        } catch (DataObjectNotFoundException e){
                            dOb = null;
                        }
                        if (dOb !=null && dOb instanceof StrutsConfigDataObject){
                            StrutsConfigDataObject con = (StrutsConfigDataObject)dOb;
                            // the conf file is not in any module (is not declared in the web.xml)
                            try{
                                StrutsConfig sConfig = con.getStrutsConfig();
                                switch (elementType) {
                                    case TYPE_ACTION : addActions(list, sConfig);break;
                                    case TYPE_FORM_BEAN : addFormBeans(list, sConfig);break;
                                    case TYPE_MESSAGE_RESOURCES : addMessageResource(list, sConfig);break;
                                }
                            } catch (java.io.IOException e){
                                // Do nothing
                            }
                        }
                    }
                }
            }
        } 
        return list;
    }

    private static void addActions(List list, StrutsConfig sConfig) {
        ActionMappings mappings = sConfig.getActionMappings();
        if (mappings==null) return;
        Action [] actions = mappings.getAction();
        for (int j = 0; j < actions.length; j++)
            list.add(actions[j]);
    }
    
    private static void addFormBeans(List list, StrutsConfig sConfig) {
        FormBeans formBeans = sConfig.getFormBeans();
        if (formBeans==null) return;
        FormBean [] beans = formBeans.getFormBean();
        for (int j = 0; j < beans.length; j++)
            list.add(beans[j]);
    }
    
    private static void addMessageResource(List list, StrutsConfig sConfig) {
        MessageResources[] rosources = sConfig.getMessageResources();
        for (int j = 0; j < rosources.length; j++)
            list.add(rosources[j]);
    }
    
    
    /** Returns all configuration files for the module
     **/
    public static FileObject[] getConfigFiles(String module, FileObject dd){
        FileObject docBase = WebModule.getWebModule(dd).getDocumentBase();
        if (docBase == null)
            return null;
        Servlet servlet = getActionServlet(dd);
        InitParam param = null;
        if (module.equals(DEFAULT_MODULE_NAME))
            param = (InitParam)servlet.findBeanByName("InitParam", "ParamName", DEFAULT_MODULE_NAME);
        else
            param = (InitParam)servlet.findBeanByName("InitParam", "ParamName", DEFAULT_MODULE_NAME+"/"+module);
        FileObject[] configs = null;
        if (param != null){
            StringTokenizer st = new StringTokenizer(param.getParamValue(), ",");
            configs = new FileObject[st.countTokens()];
            int index = 0;
            while (st.hasMoreTokens()){
                String name = st.nextToken().trim();
                configs[index] = docBase.getFileObject(name);
                index++;
            }
        }
        return configs;
    }
    
    
    
    /** Returns name of Struts module, which contains the configuration file.
     */
    public static String getModuleName(FileObject config, FileObject dd){
        Servlet servlet = getActionServlet(dd);
        InitParam [] param = servlet.getInitParam();
        StringTokenizer st = null;
        int index = 0;
        String moduleName = null;
        while (moduleName == null && index < param.length){
            if(param[index].getParamName().trim().startsWith(DEFAULT_MODULE_NAME)){
                String[] files = param[index].getParamValue().split(","); //NOI18N
                for (int i = 0; i < files.length; i++){
                    String file = files[i];
                    if (config.getPath().endsWith(file)){
                        if (!param[index].getParamName().trim().equals(DEFAULT_MODULE_NAME)){
                            moduleName = param[index].getParamName().trim()
                            .substring(DEFAULT_MODULE_NAME.length()+1);
                        } else{
                            moduleName = DEFAULT_MODULE_NAME;
                        }
                        break;
                    }
                }
                
            }
            index++;
        }
        return moduleName;
    }
    
    public static Servlet getActionServlet(FileObject dd){
        // PENDING - must be more declarative.
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            return (Servlet)webApp.findBeanByName("Servlet","ServletClass","org.apache.struts.action.ActionServlet"); //NOI18N
        } catch (java.io.IOException e) {
            return null;
        }
    }
    
    /** Returns the mapping for the Struts Action Servlet.
     */
    public static String getActionServletMapping(FileObject dd){
        Servlet servlet = getActionServlet(dd);
        if (servlet != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                ServletMapping[] mappings = webApp.getServletMapping();
                for (int i = 0; i < mappings.length; i++){
                    if (mappings[i].getServletName().equals(servlet.getServletName()))
                        return mappings[i].getUrlPattern();
                }
            } catch (java.io.IOException e) {
                
            }
        }
        return null;
    }
    
    /** Returns relative path for all struts configuration files in the web module
     */
    public static String[] getConfigFiles(FileObject dd){
        Servlet servlet = getActionServlet(dd);
        if (servlet!=null) {
            InitParam[] params = servlet.getInitParam();
            List list = new ArrayList();
            for (int i=0;i<params.length;i++) {
                String paramName=params[i].getParamName();
                if (paramName!=null) {
                    if (paramName.startsWith(DEFAULT_MODULE_NAME)){
                        String[] files = params[i].getParamValue().split(","); //NOI18N
                        for (int j = 0; j < files.length; j++)
                            list.add(files[j]);
                    }
                }
            }
            String[] result = new String[list.size()];
            list.toArray(result);
            return result;
        }
        return new String[]{};
    }
    
    /** Returns all configuration files in the web module
     */
    public static FileObject[] getConfigFilesFO(FileObject dd){
        FileObject docBase = WebModule.getWebModule(dd).getDocumentBase();
        if (docBase == null)
            return null;
        Servlet servlet = getActionServlet(dd);
        if (servlet!=null) {
            InitParam[] params = servlet.getInitParam();
            List list = new ArrayList();
            FileObject file;
            for (int i=0;i<params.length;i++) {
                String paramName=params[i].getParamName();
                if (paramName!=null) {
                    if (paramName.startsWith(DEFAULT_MODULE_NAME)){ //NOI18N
                        String[] files = params[i].getParamValue().split(","); //NOI18N
                        for (int j = 0; j < files.length; j++){
                            file = docBase.getFileObject(files[j]);
                            if (file != null)
                                list.add(file);
                        }
                    }
                }
            }
            FileObject[] result = new FileObject[list.size()];
            list.toArray(result);
            return result;
        }
        return new FileObject[]{};
    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fo) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = (Sources)proj.getLookup().lookup(Sources.class);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fo)) {
                String relativePath = FileUtil.getRelativePath(root,fo);
                if (relativePath!=null) {
                    if (separator!='/') relativePath = relativePath.replace('/',separator);
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int)'.');
                        if (index>0) relativePath = relativePath.substring(0,index);
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }
}
