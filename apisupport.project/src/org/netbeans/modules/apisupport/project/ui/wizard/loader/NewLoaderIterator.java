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

package org.netbeans.modules.apisupport.project.ui.wizard.loader;

import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Wizard for creating new DataLoaders
 *
 * @author Milos Kleint
 */
final class NewLoaderIterator extends BasicWizardIterator {
    
    private NewLoaderIterator.DataModel data;
    
    private NewLoaderIterator() { /* Use factory method. */ };
    
    public static NewLoaderIterator createIterator() {
        return new NewLoaderIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewLoaderIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new FileRecognitionPanel(wiz, data),
            new NameAndLocationPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private String prefix;
        private String iconPath;
        private String mimeType;
        private boolean extensionBased = true;
        private String extension;
        private String namespace;
        
        private CreatedModifiedFiles files;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
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
        final String mime = model.getMimeType();
        Map<String, String> replaceTokens = new HashMap<String, String>();
        replaceTokens.put("@@PREFIX@@", namePrefix);//NOI18N
        replaceTokens.put("@@PACKAGENAME@@", packageName);//NOI18N
        replaceTokens.put("@@MIMETYPE@@", mime);//NOI18N
        replaceTokens.put("@@EXTENSIONS@@", formatExtensions(model.isExtensionBased(), model.getExtension(), mime));//NOI18N
        replaceTokens.put("@@NAMESPACES@@", formatNameSpace(model.isExtensionBased(), model.getNamespace(), mime));//NOI18N
        
        // Copy action icon
        String origIconPath = model.getIconPath();
        if (origIconPath != null && new File(origIconPath).exists()) {
            String relativeIconPath = model.addCreateIconOperation(fileChanges, origIconPath);
            replaceTokens.put("@@IMAGESNIPPET@@", formatImageSnippet(relativeIconPath));//NOI18N
            replaceTokens.put("@@ICONPATH@@", relativeIconPath);//NOI18N
            replaceTokens.put("@@COMMENTICON@@", "");//NOI18N
            
        } else {
            replaceTokens.put("@@IMAGESNIPPET@@", formatImageSnippet(null)); //NOI18N
            replaceTokens.put("@@ICONPATH@@", "SET/PATH/TO/ICON/HERE"); //NOI18N
            replaceTokens.put("@@COMMENTICON@@", "//");//NOI18N
        }
        
        // 1. create dataloader file
        String loaderName = model.getDefaultPackagePath(namePrefix + "DataLoader.java", false); // NOI18N
        // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        URL template = NewLoaderIterator.class.getResource("templateDataLoader.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(loaderName, template, replaceTokens));
        String loaderInfoName = model.getDefaultPackagePath(namePrefix + "DataLoaderBeanInfo.java", false); // NOI18N
        template = NewLoaderIterator.class.getResource("templateDataLoaderBeanInfo.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(loaderInfoName, template, replaceTokens));
        
        // 2. dataobject file
        final boolean isEditable = Pattern.matches("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.+-])*)", //NOI18N
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
        
        String doName = model.getDefaultPackagePath(namePrefix + "DataObject.java", false); // NOI18N
        template = null;
        try {
            SpecificationVersion current = model.getModuleInfo().getDependencyVersion("org.openide.loaders");
            SpecificationVersion desired = new SpecificationVersion("6.0"); // NOI18N
            if (current.compareTo(desired) >= 0) {
                template = NewLoaderIterator.class.getResource("templateDataObjectWithLookup.javx");//NOI18N
            }
        } catch (IOException ex) {
            Logger.getLogger(NewLoaderIterator.class.getName()).log(Level.INFO, null, ex);
        }
        if (template == null) {
            template = NewLoaderIterator.class.getResource("templateDataObject.javx");//NOI18N
        }
        fileChanges.add(fileChanges.createFileWithSubstitutions(doName, template, replaceTokens));
        
        // 3. node file
        String nodeName = model.getDefaultPackagePath(namePrefix + "DataNode.java", false); // NOI18N
        template = NewLoaderIterator.class.getResource("templateDataNode.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(nodeName, template, replaceTokens));
        
        // 4. mimetyperesolver file
        template = NewLoaderIterator.class.getResource("templateresolver.xml");//NOI18N
        fileChanges.add(fileChanges.createLayerEntry("Services/MIMEResolver/" + namePrefix + "Resolver.xml", //NOI18N
                template,
                replaceTokens,
                NbBundle.getMessage(NewLoaderIterator.class, "LBL_LoaderName", namePrefix),//NOI18N
                null));
        
        //5. update project.xml with dependencies
        fileChanges.add(fileChanges.addModuleDependency("org.openide.filesystems")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.loaders")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.nodes")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.util")); //NOI18N
        if (isEditable) {
            fileChanges.add(fileChanges.addModuleDependency("org.openide.text")); //NOI18N
        }
        if (isEditable) {
            fileChanges.add(fileChanges.addModuleDependency("org.openide.windows")); //NOI18N
        }
        
        // 6. update/create bundle file
        String bundlePath = model.getDefaultPackagePath("Bundle.properties", true); // NOI18N
        fileChanges.add(fileChanges.bundleKey(bundlePath, "LBL_" + namePrefix + "_loader_name",  // NOI18N
                NbBundle.getMessage(NewLoaderIterator.class, "LBL_LoaderName", namePrefix))); //NOI18N
        
        // 7. register manifest entry
        boolean isXml = Pattern.matches("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.-])*\\+xml)", //NOI18N
                mime);
        String installBefore = null;
        if (isXml) {
            installBefore = "org.openide.loaders.XMLDataObject, org.netbeans.modules.xml.core.XMLDataObject"; //NOI18N
        }
        
        fileChanges.add(fileChanges.addLoaderSection(packageName.replace('.', '/')  + "/" + namePrefix + "DataLoader", installBefore)); // NOI18N

        // 7a. create matching test registration for convenience (#73202)
        fileChanges.add(fileChanges.addLookupRegistration("org.openide.loaders.DataLoader", packageName + '.' + namePrefix + "DataLoader", true)); // NOI18N
        
        //8. create layerfile actions subsection
        
        fileChanges.add(fileChanges.layerModifications(new CreatedModifiedFiles.LayerOperation() {
            public void run(FileSystem layer) throws IOException {
                List<String> actions = new ArrayList<String>();
                if (isEditable) {
                    actions.add("org.openide.actions.OpenAction"); // NOI18N
                }
                actions.addAll(Arrays.asList(new String[] {
                    "org.openide.actions.FileSystemAction", // NOI18N
                    null,
                    "org.openide.actions.CutAction", // NOI18N
                    "org.openide.actions.CopyAction", // NOI18N
                    null,
                    "org.openide.actions.DeleteAction", // NOI18N
                    "org.openide.actions.RenameAction", // NOI18N
                    null,
                    "org.openide.actions.SaveAsTemplateAction", // NOI18N
                    null,
                    "org.openide.actions.ToolsAction", // NOI18N
                    "org.openide.actions.PropertiesAction", // NOI18N
                }));
                FileObject folder = FileUtil.createFolder(layer.getRoot(), "Loaders/" + mime + "/Actions"); // NOI18N
                List<DataObject> kids = new ArrayList<DataObject>();
                Iterator it = actions.iterator();
                int i = 0;
                while (it.hasNext()) {
                    String name = (String) it.next();
                    FileObject kid;
                    if (name != null) {
                        kid = folder.createData(name.replace('.', '-') + ".instance"); // NOI18N
                    } else {
                        kid = folder.createData("sep-" + (++i) + ".instance"); // NOI18N
                        kid.setAttribute("instanceClass", "javax.swing.JSeparator"); // NOI18N
                    }
                    kids.add(DataObject.find(kid));
                }
                DataFolder.findFolder(folder).setOrder(kids.toArray(new DataObject[kids.size()]));
            }
        }, Collections.<String>emptySet()));
        
        //9. create sample template
        String suffix = null;
        if (model.isExtensionBased()) {
            suffix = "Template." + getFirstExtension(model.getExtension()); // NOI18N
            template = NewLoaderIterator.class.getResource("templateNew1");//NOI18N
        } else {
            template = NewLoaderIterator.class.getResource("templateNew2");//NOI18N
            suffix = "Template.xml"; // NOI18N
            try {
                replaceTokens.put("@@NAMESPACE@@", XMLUtil.toElementContent(model.getNamespace())); // NOI18N
            } catch (CharConversionException ex) {
                assert false: ex;
            }
        }
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("template", Boolean.TRUE); // NOI18N
        fileChanges.add(fileChanges.createLayerEntry("Templates/Other/" + namePrefix + suffix, //NOI18N
                template,
                replaceTokens,
                NbBundle.getMessage(NewLoaderIterator.class, "LBL_fileTemplateName", namePrefix),
                attrs)); //NOI18N
        model.setCreatedModifiedFiles(fileChanges);
    }
    
    private static String formatExtensions(boolean isExtensionBased, String ext, String mime) {
        if (!isExtensionBased) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(ext, " ,"); // NOI18N
        while (tokens.hasMoreTokens()) {
            String element = tokens.nextToken().trim();
            if (element.startsWith(".")) { // NOI18N
                element = element.substring(1);
            }
            buff.append("        <ext name=\"").append(element).append("\"/>\n"); //NOI18N
        }
        buff.append("        <resolver mime=\"").append(mime).append("\"/>"); //NOI18N
        return buff.toString();
    }
    
    private static String getFirstExtension(String ext) {
        StringTokenizer tokens = new StringTokenizer(ext," ,"); // NOI18N
        String element = "someextension"; // NOI18N
        if (tokens.hasMoreTokens()) {
            element = tokens.nextToken().trim();
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
        buff.append("            <xml-rule>\n"); // NOI18N
        try {
            buff.append("                <element ns=\"").append(XMLUtil.toElementContent(namespace)).append("\"/>\n"); //NOI18N
        } catch (CharConversionException ex) {
            assert false : ex;
        }
        buff.append("            </xml-rule>\n"); //NOI18N
        buff.append("        </resolver>"); //NOI18N
        return buff.toString();
    }
    
    private static String formatImageSnippet(String path) {
        if (path == null) {
            // XXX Utilities is unconditionally imported
            return "return super.getIcon(type); // TODO add a custom icon here\n"; //NOI18N
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

