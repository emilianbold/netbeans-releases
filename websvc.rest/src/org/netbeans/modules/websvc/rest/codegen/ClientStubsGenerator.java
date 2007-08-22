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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel.*;
import org.openide.util.NbBundle;

/**
 * Code generator for plain REST resource class.
 * The generator takes as paramenters:
 *  - target directory
 *  - REST resource bean meta model.
 *
 * @author Ayub Khan
 */
public class ClientStubsGenerator extends AbstractGenerator {
    
    public static final String RESTSTUB = "reststub"; //NOI18N
    public static final String RESTSTUB_TEMPLATE = "Templates/WebServices/reststub.html"; //NOI18N
    public static final String STUBSUPPORT_TEMPLATE = "Templates/WebServices/support.js"; //NOI18N
    public static final String PROJECTSTUB_TEMPLATE = "Templates/WebServices/projectstub.js"; //NOI18N
    public static final String CONTAINERSTUB_TEMPLATE = "Templates/WebServices/containerstub.js"; //NOI18N
    public static final String GENERICSTUB_TEMPLATE = "Templates/WebServices/genericstub.js"; //NOI18N
    public static final String COMMON = "common"; //NOI18N
    public static final String JS = "js"; //NOI18N
    public static final String HTML = "html"; //NOI18N
    public static final String SUPPORT = "support"; //NOI18N
    private FileObject root;
    private Project p;
    private boolean overwrite;
    private String projectName;
    private ClientStubModel model;
    
    public ClientStubsGenerator(FileObject root, Project p, boolean overwrite) throws IOException {
        assert root != null;
        assert p != null;
        this.root = root;
        this.p = p;
        this.overwrite = overwrite;
        this.projectName = p.getProjectDirectory().getName();
        init(p);
    }
    
    public FileObject getRootDir() {
        return root;
    }
    
    public Project getProject() {
        return p;
    }
    
    public boolean isOverwrite() {
        return overwrite;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public ClientStubModel getModel() {
        return model;
    }
    
    private void init(Project p) throws IOException {
        //create support js files
        FileObject commonDir = createFolder(getRootDir(), COMMON);
        FileObject commonJsDir = createFolder(commonDir, JS);
        if(getRootDir().getFileObject(RESTSTUB+"."+HTML) == null)
            RestUtils.createDataObjectFromTemplate(RESTSTUB_TEMPLATE, getRootDir(), RESTSTUB);
        if(commonJsDir.getFileObject(SUPPORT+"."+JS) == null)
            RestUtils.createDataObjectFromTemplate(STUBSUPPORT_TEMPLATE, commonJsDir, SUPPORT);
        
        this.model = new ClientStubModel();
        this.model.buildModel(p);
    }
    
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle, false);
        
        String prjName = ProjectUtils.getInformation(getProject()).getName();
        FileObject projStubDir = createFolder(getRootDir(), prjName);
        FileObject jsDir = createFolder(projStubDir, JS);
        if(jsDir.getFileObject(prjName+"."+JS) == null)
            RestUtils.createDataObjectFromTemplate(PROJECTSTUB_TEMPLATE, jsDir, prjName);
        updateProjectStub(jsDir.getFileObject(prjName, JS), prjName);
        
        Set<FileObject> files = new HashSet<FileObject>();
        List<Resource> resourceList = model.getResources();
        
        for (Resource r : resourceList) {
            reportProgress(NbBundle.getMessage(ClientStubsGenerator.class,
                    "MSG_GeneratingClass", r.getFileName()));
            
            files.add(new ResourceJavaScript(r, jsDir).generate());
        }
        updateRestStub(getRootDir().getFileObject(RESTSTUB, HTML), resourceList);
        return files;
    }
    
    private FileObject createFolder(FileObject parent, String folderName) throws IOException {
        FileObject folder = parent.getFileObject(folderName);
        if(folder == null)
            folder = parent.createFolder(folderName);
        return folder;
    }
    
    private void updateProjectStub(FileObject projectStub, String prjName) throws IOException {
        FileLock lock = projectStub.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(projectStub)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (line.contains("__BASE_URL__")) {
                    sb.append(line.replaceAll("__BASE_URL__", "http://localhost:8080/"+prjName+"/restbean"));
                } else if (line.contains("__PROJECT_NAME__")) {
                    sb.append(line.replaceAll("__PROJECT_NAME__", prjName));
                } else if (line.contains("__PROJECT_INIT_BODY__")) {
                    String initBody = "";
                    List<Resource> resourceList = model.getResources();
                    for(int i=0;i<resourceList.size();i++) {
                        Resource r = resourceList.get(i);
                        if(r.isContainer())
                            initBody += "      this.resources["+i+"] = new "+r.getName()+"(this.uri+'"+r.getPath()+"');\n";
                    }
                    sb.append(initBody);                    
                } else {                    
                    sb.append(line);
                }
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(projectStub.getOutputStream(lock), "UTF-8");
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private void updateRestStub(FileObject restStub, List<Resource> resourceList) throws IOException {
        String prjName = getProjectName();
        StringBuffer sb1 = new StringBuffer();
        sb1.append("\t<script type='text/javascript' src='./" + prjName + "/js/" + prjName + ".js'></script>\n");
        for (Resource r : resourceList) {
            sb1.append("\t<script type='text/javascript' src='./" + prjName + "/js/" + r.getFileNameExt() + "'></script>\n");
        }
        StringBuffer sb2 = new StringBuffer();
        sb2.append("\n\t<!-- Using JavaScript files for project " + prjName + "-->\n");
        sb2.append("\t<script language='Javascript'>\n");
        sb2.append("\t\tvar str = '';\n");
        sb2.append("\t\t//Example test code for " + prjName + "\n");
        sb2.append("\t\tstr = '<h2>Resources for " + prjName + ":</h2><br><table border=\"1\">';\n");
        sb2.append("\t\tvar app = new " + prjName + "();\n");
        sb2.append("\t\tvar resources = app.getResources();\n");
        sb2.append("\t\tfor(i=0;i<resources.length;i++) {\n");
        sb2.append("\t\t  var resource = resources[i];\n");
        sb2.append("\t\t  var uri = resource.getUri();\n");
        sb2.append("\t\t  str += '<tr><td valign=\"top\"><a href=\"'+uri+'\" target=\"_blank\">'+uri+'</a></td><td>';\n");
        sb2.append("\t\t  var items  = resource.getItems();\n");
        sb2.append("\t\t  if(items != undefined) {\n");
        sb2.append("\t\t    for(j=0;j<items.length;j++) {\n");
        sb2.append("\t\t        var item = items[j];\n");
        sb2.append("\t\t        var uri2 = item.getUri();\n");
        sb2.append("\t\t        str += '<a href=\"'+uri2+'\" target=\"_blank\">'+uri2+'</a><br/>';\n");
        sb2.append("\t\t        str += '&nbsp;&nbsp;<font size=\"-3\">'+item.toString()+'</font><br/>';\n");
        sb2.append("\t\t    }\n");
        sb2.append("\t\t  }\n");
        sb2.append("\t\t  str += '</td></tr>';\n");
        sb2.append("\t\t}\n");
        sb2.append("\t\tstr += '</table><br>';\n");
        sb2.append("\t\tvar n = document.getElementById('containerContent');\n");
        sb2.append("\t\tn.innerHTML = n.innerHTML + str;\n\n");
        sb2.append("\t</script>\n");
        FileLock lock = restStub.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(restStub)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (line.contains("JS_DECLARE_END")) {
                    sb.append(sb1.toString());
                } else if (line.contains("JS_USAGE_END")) {
                    sb.append(sb2.toString());
                }
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(restStub.getOutputStream(lock), "UTF-8");
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    public class ResourceJavaScript {
        
        private Resource r;
        private FileObject jsFolder;
        
        public ResourceJavaScript(Resource r, FileObject jsFolder) {
            this.r = r;
            this.jsFolder = jsFolder;
        }
        
        public FileObject generate() throws IOException {
            FileObject fo = jsFolder.getFileObject(r.getFileNameExt());
            if (fo != null) {
                if(isOverwrite())
                    fo.delete();
                else
                    throw new IOException("File: "+jsFolder.getPath()+"/"+r.getFileNameExt()+" already exists.");
            }
            if(r.isContainer())
                RestUtils.createDataObjectFromTemplate(CONTAINERSTUB_TEMPLATE, jsFolder, r.getFileName());
            else
                RestUtils.createDataObjectFromTemplate(GENERICSTUB_TEMPLATE, jsFolder, r.getFileName());
            fo = jsFolder.getFileObject(r.getFileNameExt());
            FileLock lock = fo.lock();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    line = replaceTokens(r, line);
                    sb.append(line);
                    sb.append("\n");
                }
                OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
                try {
                    writer.write(sb.toString());
                } finally {
                    writer.close();
                }
            } finally {
                lock.releaseLock();
            }
            return fo;
        }
        
        private String replaceTokens(Resource r, String line) {
            RepresentationNode root = r.getRepresentation().getRoot();
            String replacedLine = line;
            String[] containerStubTokens = {
                "__CONTAINER_NAME__",
                "__CONTAINER_PATH_NAME__",
                "__CONTAINER_ITEM_NAME__",
                "__CONTAINER_ITEM_PATH_NAME__",
                "__STUB_METHODS__"
            };
            String[] genericStubTokens = {
                "__GENERIC_NAME__",
                "__GENERIC_PATH_NAME__",
                "__FIELDS_DEFINITION__",
                "__GETTER_SETTER_METHODS__",
                "__FIELDS_INIT__",
                "__SUB_RESOURCE_NAME__",
                "__SUB_RESOURCE_PATH_NAME__",
                "__FIELDS_TOSTRING__",
                "__STUB_METHODS__"
            };
            if(r.isContainer()) {
                String containerName = r.getName();
                String containerRepName = root.getName();
                //TODO
                String containerItemRepName = root.getChildren().get(0).getName();
                if(containerItemRepName.indexOf("Ref") != -1)
                    containerItemRepName = containerItemRepName.substring(0, containerItemRepName.indexOf("Ref"));
                String containerItemName = containerItemRepName.substring(0, 1).toUpperCase() + containerItemRepName.substring(1);
                for(String token: containerStubTokens) {
                    if("__CONTAINER_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_NAME__", containerName);
                    else if("__CONTAINER_PATH_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_PATH_NAME__", containerRepName);
                    else if("__CONTAINER_ITEM_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_ITEM_NAME__", containerItemName);
                    else if("__CONTAINER_ITEM_PATH_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_ITEM_PATH_NAME__", containerItemRepName);
                    else if("__STUB_METHODS__".equals(token))
                        replacedLine = replacedLine.replace("__STUB_METHODS__", createStubJSMethods(r));
                }
            } else {
                String resourceName = r.getName();
                String resourceRepName = root.getName();
                for(String token: genericStubTokens) {
                    if("__GENERIC_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__GENERIC_NAME__", resourceName);
                    else if("__GENERIC_PATH_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__GENERIC_PATH_NAME__", resourceRepName);
                    else if("__FIELDS_DEFINITION__".equals(token))
                        replacedLine = replacedLine.replaceAll("__FIELDS_DEFINITION__", createFieldsDefinition(root, true));
                    else if("__GETTER_SETTER_METHODS__".equals(token))
                        replacedLine = replacedLine.replace("__GETTER_SETTER_METHODS__", createGetterSetterMethods(root, true));
                    else if("__FIELDS_INIT__".equals(token))
                        replacedLine = replacedLine.replace("__FIELDS_INIT__", createFieldsInitBody(root, true));
                    else if("__SUB_RESOURCE_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__SUB_RESOURCE_NAME__", "");
                    else if("__SUB_RESOURCE_PATH_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__SUB_RESOURCE_PATH_NAME__", "");
                    else if("__FIELDS_TOSTRING__".equals(token))
                        replacedLine = replacedLine.replace("__FIELDS_TOSTRING__", createFieldsToStringBody(root, true));
                    else if("__STUB_METHODS__".equals(token))
                        replacedLine = replacedLine.replace("__STUB_METHODS__", createStubJSMethods(r));
                }
            }
            return replacedLine;
        }
        
        protected String createStubJSMethods(Resource r) {
            StringBuffer sb = new StringBuffer();
            for (Method m : r.getMethods()) {
                sb.append(createMethod(m)+",\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3)+"\n";
            else
                return s;
        }
        
        private String createGetterSetterMethods(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            
            //create getter and setter for attributes
            sb.append(createGetterSetterMethods(root.getAttributes(), skipUri));
            
            //create getter and setter for elements
            sb.append(createGetterSetterMethods(root.getChildren(), skipUri));
            
            return sb.toString();
        }
        
        private String createGetterSetterMethods(List<RepresentationNode> nodes, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:nodes) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri"))) {
                    sb.append(createGetterMethod(child)+",\n\n");
                    sb.append(createSetterMethod(child)+",\n\n");
                }
            }
            return sb.toString();
        }
        
        private String createFieldsDefinition(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("    this."+childName+" = '';\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String name = child.getName();
                if(child.isRoot()) {
                    sb.append("    this."+name+" = new Array();\n");
                } else {
                    sb.append("    this."+name+" = '';\n");
                }
            }
            return sb.toString();
        }
        
        private String createFieldsInitBody(RepresentationNode root, boolean skipUri) {
            String repName = root.getName();
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("         this."+childName+" = "+repName+"['@"+childName+"'];\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String childName = child.getName();
                if(child.isReference() || child.isRoot()) {
                    String childRepName = findRepresentationName(childName);
                    sb.append("         this."+childName+" = new "+findResourceName(childName)+"("+repName+"['"+childName+"']['@uri']);\n");
                } else {
                    sb.append("         this."+childName+" = "+repName+"['"+childName+"']['$'];\n");
                }
            }
            return sb.toString();
        }
        
        private String createFieldsToStringBody(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("         '\"@"+childName+"\":\"'+this."+childName+"+'\",'+\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String childName = child.getName();
                if(child.isReference()) {
                    String childRepName = findRepresentationName(childName);
                    sb.append("         '\""+childName+"\":{\"@uri\":\"'+this."+childName+".getUri()+'\", \""+childRepName+"\":{\"$\":\"'+this."+childName+".get"+findResourceName(childRepName)+"()+'\"}},'+\n");
                }else if(child.isRoot()) {
                    sb.append("         this."+childName+".toString()+\n");
                }else
                    sb.append("         '\""+childName+"\":{\"$\":\"'+this."+childName+"+'\"},'+\n");
            }
            return sb.toString();
        }
        
        private String findResourceName(String repName) {
            if(repName.contains("Ref"))
                repName = repName.substring(0, repName.indexOf("Ref"));
            return repName.substring(0,1).toUpperCase()+repName.substring(1);
        }
        
        private String findRepresentationName(String repName) {
            if(repName.contains("Ref"))
                return repName.substring(0, repName.indexOf("Ref"));
            return repName;
        }        
        
        /*private String findResourceName(String repName) {
            if(repName.contains("Ref"))
                repName = repName.substring(0, repName.indexOf("Ref"));
            return repName.substring(0,1).toUpperCase()+repName.substring(1);
        }*/

        private String createGetterMethod(RepresentationNode n) {
            String mName = RestUtils.createGetterMethodName(n);
            String fieldName = n.getName();
            return "   "+mName + " : function() {\n" +
                    "      if(!this.initialized)\n" +
                    "         this.init();\n" +
                    "      return this." + fieldName + ";\n" +
                    "   }";
        }
        
        private String createSetterMethod(RepresentationNode n) {
            String mName = createSetterMethodName(n);
            String fieldName = n.getName();
            return "   " + mName + " : function(" + fieldName + "_) {\n" +
                    "      this." + fieldName + " = " + fieldName + "_;\n" +
                    "   }";
        }
        
        private String createSetterMethodName(RepresentationNode n) {
            String mName = "set";
            if(n.getLink() != null) {
                mName = RestUtils.escapeJSReserved(n.getLink().getName().toString());
                mName = "set"+mName.substring(3);
            } else {
                mName = n.getName();
                mName = "set"+mName.substring(0, 1).toUpperCase()+mName.substring(1);
            }
            return mName;
        }
        
        private String createMethod(Method m) {
            if (m.getType() == MethodType.GET) {
                return createGetMethod(m);
            } else if (m.getType() == MethodType.POST) {
                return createPostMethod(m);
            } else if (m.getType() == MethodType.PUT) {
                return createPutMethod(m);
            } else if (m.getType() == MethodType.DELETE) {
                return createDeleteMethod(m);
            } else if (m instanceof NavigationMethod) {
                return createNavigationMethod((NavigationMethod) m);
            } else {
                return "";
            }
        }
        
        private String createMethodName(Method m, String mimeType, int length) {
            if(length > 1) {
                for(Constants.MimeType mime:Constants.MimeType.values())
                    if(mime.value().equals(mimeType))
                        return m.getName() + mime.suffix();
            }
            return m.getName();
        }
        
        private String createGetMethod(Method m) {
            StringBuffer sb = new StringBuffer();
            String mimeTypes[] = m.getResponse().getRepresentation().getMime().split(",");
            int length = mimeTypes.length;
            for(String mimeType:mimeTypes) {
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function() {\n" +
                        "      return get_(this.uri, '" +mimeType+ "');\n" +
                        "   },\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
        }
        
        private String createPostMethod(Method m) {
            StringBuffer sb = new StringBuffer();
            String mimeTypes[] = m.getRequest().getRepresentation().getMime().split(",");
            int length = mimeTypes.length;
            for(String mimeType:mimeTypes) {
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function(content) {\n" +
                        "      return post_(this.uri, '" + mimeType + "', content);\n" +
                        "   },\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
        }
        
        private String createPutMethod(Method m) {
            StringBuffer sb = new StringBuffer();
            String mimeTypes[] = m.getRequest().getRepresentation().getMime().split(",");
            int length = mimeTypes.length;
            for(String mimeType:mimeTypes) {
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function(content) {\n" +
                        "      return put_(this.uri, '" + mimeType + "', content);\n" +
                        "   },\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
        }
        
        private String createDeleteMethod(Method m) {
            return "   " + RestUtils.escapeJSReserved(m.getName()) + " : function() {\n" +
                    "      return delete_(this.uri);\n" +
                    "   }";
        }
        
        private String createNavigationMethod(NavigationMethod m) {
            return "   " + m.getName() + " : function(" + m.getNavigationUri() + ") {\n" +
                    "      var link = new " + m.getLinkName() + "(this.uri+'/'+" + m.getNavigationUri() + ")()\n" +
                    "      return link;\n" +
                    "   }";
        }
        
    }
    
}
