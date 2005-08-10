/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.loader;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Wizard for creating new DataLoaders
 *
 * @author Milos Kleint
 */
public class NewLoaderIterator extends BasicWizardIterator {
    private static final long serialVersionUID = 1L;
    NewLoaderIterator.DataModel data = null;    
    
    public static NewLoaderIterator createIterator() {
        return new NewLoaderIterator();
    }
    
    public Set instantiate() throws IOException {
        assert data != null;
        CreatedModifiedFiles fileOperations = data.getCreatedModifiedFiles();
        if (fileOperations != null) {   
            fileOperations.run();
        }
        String[] paths = fileOperations.getCreatedPaths();
        HashSet set = new HashSet();
        for (int i =0; i < paths.length; i++) {
            FileObject fo = data.getProject().getProjectDirectory().getFileObject(paths[i]);
            if (fo != null) {
                set.add(fo);
            }
        }
        return set;
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewLoaderIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new FileRecognitionPanel(wiz, data),
            new NameAndLocationPanel(wiz, data)
        };
    }

    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }

    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        private String packageName = null;
        private String prefix = null;
        private String iconPath = null;
        private String mimeType = null;
        private boolean extensionBased = true;
        private String extension = null;
        private String namespace = null;
        
        private CreatedModifiedFiles files;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public CreatedModifiedFiles getCreatedModifiedFiles() {            
            return files;
        }

        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getIconPath() {
            return iconPath;
        }

        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public boolean isExtensionBased() {
            return extensionBased;
        }

        public void setExtensionBased(boolean extensionBased) {
            this.extensionBased = extensionBased;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

    }
    
    public static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles fileChanges = new CreatedModifiedFiles(model.getProject());

        String namePrefix = model.getPrefix();
        String packageName = model.getPackageName();
        String mime = model.getMimeType();
        HashMap replaceTokens = new HashMap();
        replaceTokens.put("@@PREFIX@@", namePrefix);//NOI18N
        replaceTokens.put("@@PACKAGENAME@@", packageName);//NOI18N
        replaceTokens.put("@@MIMETYPE@@", mime);//NOI18N
        replaceTokens.put("@@EXTENSIONS@@", formatExtensions(model.isExtensionBased(), model.getExtension(), mime));//NOI18N
        replaceTokens.put("@@NAMESPACES@@", formatNameSpace(model.isExtensionBased(), model.getNamespace(), mime));//NOI18N

        // 0. move icon file if necessary
        String icon = model.getIconPath();
        if (icon != null) {
            FileObject fo = FileUtil.toFileObject(new File(icon));
            String relativeIconPath = null;
            if (!FileUtil.isParentOf(model.getProject().getSourceDirectory(), fo)) {
                String iconPath = getRelativePath(model.getProject(), packageName, 
                                                "", fo.getNameExt()); //NOI18N
                try {
                    fileChanges.add(fileChanges.createFile(iconPath, fo.getURL()));
                    relativeIconPath = packageName.replace('.', '/') + "/" + fo.getNameExt();
                } catch (FileStateInvalidException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            } else {
                relativeIconPath = FileUtil.getRelativePath(model.getProject().getSourceDirectory(), fo);
            }
            replaceTokens.put("@@IMAGESNIPPET@@", formatImageSnippet(relativeIconPath));//NOI18N
            replaceTokens.put("@@ICONPATH@@", relativeIconPath);//NOI18N
        } else {
            replaceTokens.put("@@IMAGESNIPPET@@", formatImageSnippet(null)); //NOI18N
            replaceTokens.put("@@ICONPATH@@", "SET/PATH/TO/ICON/HERE"); //NOI18N
        }
        
        // 1. create dataloader file
        String loaderName = getRelativePath(model.getProject(), model.getPackageName(), 
                                            namePrefix, "DataLoader.java"); //NOI18N
        // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        URL template = NewLoaderIterator.class.getResource("templateDataLoader.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(loaderName, template, replaceTokens));
        String loaderInfoName = getRelativePath(model.getProject(), model.getPackageName(), 
                                            namePrefix, "DataLoaderBeanInfo.java"); //NOI18N
        template = NewLoaderIterator.class.getResource("templateDataLoaderBeanInfo.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(loaderInfoName, template, replaceTokens));
        
        // 2. dataobject file
        boolean isEditable = Pattern.matches("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.+-])*)", //NOI18N
                                               mime); 
        if (isEditable) {
            StringBuffer editorBuf = new StringBuffer();
            editorBuf.append("        CookieSet cookies = getCookieSet();\n");//NOI18N
            editorBuf.append("        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));"); // NOI18N
            replaceTokens.put("@@EDITOR_SUPPORT_SNIPPET@@", editorBuf.toString());//NOI18N
            replaceTokens.put("@@EDITOR_SUPPORT_IMPORT@@", "import org.openide.text.DataEditorSupport;");//NOI18N
        } else {
            // ignore the editor support snippet
            replaceTokens.put("@@EDITOR_SUPPORT_SNIPPET@@", "");//NOI18N
            replaceTokens.put("@@EDITOR_SUPPORT_IMPORT@@", "");//NOI18N
        }
        
        String doName = getRelativePath(model.getProject(), model.getPackageName(), 
                                            namePrefix, "DataObject.java"); //NOI18N
        template = NewLoaderIterator.class.getResource("templateDataObject.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(doName, template, replaceTokens));
        
        // 3. node file
        String nodeName = getRelativePath(model.getProject(), model.getPackageName(), 
                                            namePrefix, "DataNode.java"); //NOI18N
        template = NewLoaderIterator.class.getResource("templateDataNode.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(nodeName, template, replaceTokens));
        
        // 4. mimetyperesolver file
        String resolverName = getRelativePath(model.getProject(), model.getPackageName(), 
                                            namePrefix, "Resolver.xml"); //NOI18N
        File resFile = new File(FileUtil.toFile(model.getProject().getProjectDirectory()), resolverName);
        template = NewLoaderIterator.class.getResource("templateresolver.xml");//NOI18N
        fileChanges.add(fileChanges.createLayerEntry("Services/MIMEResolver/" + namePrefix + "resolver.xml", //NOI18N
                                                     null, template, resolverName, 
                                                     replaceTokens, 
                                                     NbBundle.getMessage(NewLoaderIterator.class, "LBL_LoaderName", namePrefix),//NOI18N
                                                     null)); 
        
        //5. update project.xml with dependencies
        ProjectXMLManager manager = new ProjectXMLManager(model.getProject().getHelper());
        try {
            SortedSet set = manager.getDirectDependencies(model.getProject().getPlatform());
            if (set != null) {
                Iterator it = set.iterator();
                boolean filesystems = false;
                boolean loaders = false;
                boolean nodes = false;
                boolean util = false;
                boolean windows = false;
                boolean text = false;
                while (it.hasNext()) {
                    ModuleDependency dep = (ModuleDependency)it.next();
                    if ("org.openide.filesystems".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        filesystems = true;
                    }
                    if ("org.openide.loaders".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        loaders = true;
                    }
                    if ("org.openide.nodes".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        nodes = true;
                    }
                    if ("org.openide.util".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        util = true;
                    }
                    if ("org.openide.windows".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        windows = true;
                    }
                    if ("org.openide.text".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        text = true;
                    }
                }
                if (!filesystems) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.filesystems", -1, null, true)); //NOI18N
                }
                if (!loaders) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.loaders", -1, null, true)); //NOI18N
                }
                if (!nodes) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.nodes", -1, null, true)); //NOI18N
                }
                if (!util) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.util", -1, null, true)); //NOI18N
                }
                if (!text && isEditable) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.text", -1, null, true)); //NOI18N
                }
                if (!windows && isEditable) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.windows", -1, null, true)); //NOI18N
                }
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        // 6. update/create bundle file
        String bundlePath = getRelativePath(model.getProject(), model.getPackageName(), "", "Bundle.properties"); //NOI18N
        fileChanges.add(fileChanges.bundleKey(bundlePath, "LBL_" + namePrefix + "_loader_name",  // NOI18N
                                NbBundle.getMessage(NewLoaderIterator.class, "LBL_LoaderName", namePrefix))); //NOI18N
        
        // 7. register manifest entry
        boolean isXml = Pattern.matches("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.-])*\\+xml)", //NOI18N
                                               mime); 
        String installBefore = null;
        if (isXml) {
            installBefore = "org.openide.loaders.XMLDataObject, org.netbeans.modules.xml.core.XMLDataObject"; //NOI18N
        }
        
        fileChanges.add(fileChanges.addLoaderSection(packageName.replace('.', '/')  + "/" + namePrefix + "DataLoader", installBefore));
        
        StringBuffer buf = new StringBuffer();
        buf.append("<invisibleRoot>");//NOI18N
        if (isEditable) {
            buf.append("<file name=\"org-openide-actions-OpenAction.instance\"/>");//NOI18N
            buf.append("<attr name=\"org-openide-actions-OpenAction.instance/org-openide-actions-FileSystemAction.instance\" boolvalue=\"true\"/>");//NOI18N
        }
        buf.append("<file name=\"org-openide-actions-FileSystemAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-FileSystemAction.instance/sep-1.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"sep-1.instance\">");//NOI18N
        buf.append("<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>");//NOI18N
        buf.append("</file>");//NOI18N
        buf.append("<attr name=\"sep-1.instance/org-openide-actions-CutAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-CutAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-CutAction.instance/org-openide-actions-CopyAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-CopyAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-CopyAction.instance/sep-2.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"sep-2.instance\">");//NOI18N
        buf.append("<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>");//NOI18N
        buf.append("</file>");//NOI18N
        buf.append("<attr name=\"sep-2.instance/org-openide-actions-DeleteAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-DeleteAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-DeleteAction.instance/org-openide-actions-RenameAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-RenameAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-RenameAction.instance/sep-3.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"sep-3.instance\">");//NOI18N
        buf.append("<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>");//NOI18N
        buf.append("</file>");//NOI18N
        buf.append("<attr name=\"sep-3.instance/org-openide-actions-SaveAsTemplateAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-SaveAsTemplateAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-SaveAsTemplateAction.instance/sep-4.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"sep-4.instance\">");//NOI18N
        buf.append("<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>");//NOI18N
        buf.append("</file>");//NOI18N
        buf.append("<attr name=\"sep-4.instance/org-openide-actions-ToolsAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-ToolsAction.instance\"/>");//NOI18N
        buf.append("<attr name=\"org-openide-actions-ToolsAction.instance/org-openide-actions-PropertiesAction.instance\" boolvalue=\"true\"/>");//NOI18N
        buf.append("<file name=\"org-openide-actions-PropertiesAction.instance\"/>");//NOI18N
        buf.append("</invisibleRoot>");
        
        //8. create layerfile actions subsection
        fileChanges.add(fileChanges.createLayerSubtree("Loaders/" + mime + "/Actions",//NOI18N
                        buf.toString(), false));
        //9. create sample template
        String suffix = null;
        if (model.isExtensionBased()) {
            suffix = "Template." + getFirstExtension(model.getExtension());
            template = NewLoaderIterator.class.getResource("templateNew1");//NOI18N
        } else {
            template = NewLoaderIterator.class.getResource("templateNew2");//NOI18N
            suffix = "Template.xml";
            try {
                replaceTokens.put("@@NAMESPACE@@", XMLUtil.toElementContent(model.getNamespace()));
            } catch (CharConversionException ex) {
                assert false: ex;
            }
        }
        String templateName = getRelativePath(model.getProject(), model.getPackageName(), 
                                            namePrefix, suffix); //NOI18N
        File templateFile = new File(FileUtil.toFile(model.getProject().getProjectDirectory()), templateName);
        buf = new StringBuffer();
        Map attrs = new HashMap();
        attrs.put("template", Boolean.TRUE);
        fileChanges.add(fileChanges.createLayerEntry("Templates/Other/" + templateFile.getName(), //NOI18N
                                                     null, template, templateName, 
                                                     replaceTokens, 
                                                     NbBundle.getMessage(NewLoaderIterator.class, "LBL_fileTemplateName", namePrefix), 
                                                     attrs)); //NOI18N
        model.setCreatedModifiedFiles(fileChanges);
    }
    
    private static String getRelativePath(NbModuleProject project, String fullyQualifiedPackageName, 
                                          String prefix, String postfix) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(project.getSourceDirectoryPath()).append("/").append(fullyQualifiedPackageName.replace('.','/')) //NOI18N
                    .append("/").append(prefix).append(postfix);//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    private static String formatExtensions(boolean isExtensionBased, String ext, String mime) {
        if (!isExtensionBased) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(ext, " ,"); // NOI18N
        while (tokens.hasMoreTokens()) {
            String element = tokens.nextToken();
            if (element.startsWith(".")) {
                element = element.substring(1);
            }
            buff.append("        <ext name=\"").append(element).append("\"/>\n"); //NOI18N
        }
        buff.append("        <resolver mime=\"").append(mime).append("\"/>\n"); //NOI18N
        return buff.toString();
    }
    
    private static String getFirstExtension(String ext) {
        StringBuffer buff = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(ext," ,");
        String element = "someextension"; // NOI18N
        if (tokens.hasMoreTokens()) {
            element = tokens.nextToken();
            if (element.startsWith(".")) { //NOI18N
                element = element.substring(1);
            }
        }
        return element;
    }
    
    private static String formatNameSpace(boolean isExtensionBased, String namespace, String mime) {
        if (isExtensionBased) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        buff.append("        <ext name=\"xml\"/>\n"); //NOI18N
        buff.append("        <resolver mime=\"").append(mime).append("\">\n"); //NOI18N
        buff.append("            <xml-rule>\n");
        try {
            buff.append("                <element ns=\"").append(XMLUtil.toElementContent(namespace)).append("\"/>\n"); //NOI18N
        } catch (CharConversionException ex) {
            assert false : ex;
        }
        buff.append("            </xml-rule>\n"); //NOI18N
        buff.append("        </resolver>\n"); //NOI18N
        return buff.toString();
    }
    
    private static String formatImageSnippet(String path) {
        if (path == null) {
        // XXX Utilities is unconditionally imported
            return "return null;\n"; //NOI18N
        }
        StringBuffer buff = new StringBuffer(); 
        buff.append("        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {\n"); //NOI18N
        buff.append("            return Utilities.loadImage(\""); //NOI18N
        buff.append(path).append("\");\n"); //NOI18N
        buff.append("        } else {\n"); //NOI18N
        buff.append("            return null;\n        }\n"); //NOI18N
        return buff.toString();
    }
    
}
