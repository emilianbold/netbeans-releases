/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.reporting.dataobjects.DataObjectFactory;
import org.netbeans.modules.uml.reporting.dataobjects.ElementDataObject;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.reporting.dataobjects.DiagramData;
import org.netbeans.modules.uml.reporting.wizard.ReportWizardSettings;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.controls.projecttree.DefaultNodeFactory;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeBuilderImpl;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


public class ReportTask extends Thread implements Cancellable
{
    
    private DisplayNameComparator comparator = new DisplayNameComparator();
    private boolean cancelled = false;
    private long start;
    private IProject m_CurrentProject;
    private IElement startingPoint;
    private ProjectTreeBuilderImpl m_Builder;
    private boolean success = true;
    
    private static InputOutput inputOutput = IOProvider.getDefault().getIO(
        NbBundle.getMessage(ReportTask.class, "TITLE_ReportOutput"),false);
    
    private File reportDir;
    private ArrayList diagrams = new ArrayList();
    private ArrayList packages = new ArrayList();
    private HashMap < String, ITreeItem > elements = new HashMap < String, ITreeItem > (500);
    private HashMap < String, String > elementFileMap = new HashMap < String, String > (500);
    private HashMap < String, String > diagramFileMap = new HashMap < String, String > ();
    
    public static String FRAME = "-frame";
    public static String HTML_EXT = ".html";
    public static String OVERVIEW_FILE = "overview.html";
    public static String ELEMENT_INDEX_FILE = "element-index.html";
    public static String HELP_FILE = "help.html";
    public static String JPG_EXT = ".jpg";
    public static String IMAGE_EXT = ".png";
    
    private static ArrayList<String> opened = new ArrayList < String > ();

    private static String[] files = {
        "org/netbeans/modules/uml/reporting/templates/zoom.js",
        "org/netbeans/modules/uml/reporting/templates/behaviour.js",
        "org/netbeans/modules/uml/reporting/templates/stylesheet.css",
        "org/netbeans/modules/uml/reporting/templates/index.html",
        "org/netbeans/modules/uml/reporting/templates/help.html"
        // "org/netbeans/modules/uml/reporting/templates/element-index.html",
    };
    
    // image files used by the report being generated
    private static List<String> imageFilenames = new ArrayList<String>();
    
    // add image files that are always used in every report
    static
    {
        imageFilenames.add("zoom-in.png"); // NOI18N
        imageFilenames.add("zoom-out.png");  // NOI18N
        imageFilenames.add("fit-to-window.png"); // NOI18N
        imageFilenames.add("diagrams-root-node.png"); // NOI18N
        imageFilenames.add("package.png"); // NOI18N
        imageFilenames.add("checked.png"); // NOI18N
        imageFilenames.add("unchecked.png"); // NOI18N
        imageFilenames.add("inherit.gif"); // NOI18N
    }
    
    
    public ReportTask(ReportWizardSettings settings)
    {
        Project p = settings.getProject();
        startingPoint = settings.getElement();
        UMLProjectHelper help = (UMLProjectHelper)p.getLookup().lookup(UMLProjectHelper.class);
        this.m_CurrentProject = help.getProject();
        m_Builder = new ProjectTreeBuilderImpl(new DefaultNodeFactory());
        this.reportDir = settings.getReportFolder();
        
        initialize();
    }
    
    
    public String getReportDir()
    {
        return reportDir.getAbsolutePath();
    }
    
    
    public void run()
    {
        String name = NbBundle.getMessage(GenerateModelReportAction.class,
                "MSG_GenerateModelReportAction_Generate", m_CurrentProject.getName());
        ProgressHandle ph = ProgressHandleFactory.createHandle(name, this);
        try
        {
            ph.start();
            start = System.currentTimeMillis();
            generateReport();
        }
        catch(Exception e)
        {
            ErrorManager.getDefault().notify(e);
        }
        finally
        {
            finish();
            ph.finish();
        }
        
    }
    
    
    public void generateReport()
    {
        ITreeItem item = m_Builder.createChild(null, startingPoint);
        
        loadDataElement(item);
        
        for (int i=0; i<diagrams.size(); i++)
        {
            ITreeDiagram treeDiagram = (ITreeDiagram)diagrams.get(i);
            if (treeDiagram != null)
            {
                IDiagram diagram= treeDiagram.getData().getDiagram().getDiagram();
                if (diagram==null)
                    diagram = loadDiagram(treeDiagram.getData().getDiagram().getFilename());
                DiagramData diagramData = new DiagramData(treeDiagram);
                
                if (diagramData.toReport(new File(getDirectoryPath(treeDiagram))))
                {
                    diagramFileMap.put(treeDiagram.getDiagram().getDiagram().getXMIID(),
                            getLinkPathToDiagram(treeDiagram.getData().getDiagram()));
                }
                else
                {
                    success=false;
                    log(NbBundle.getMessage(ReportTask.class, "Log_error_generating",
                            treeDiagram.getDiagram().getDiagram().getName()));
                }
            }
        }
        if (!cancelled)
        {
            processSummaryPages();
            launchReport();
        }
    }
    
    
    private void finish()
    {
        for (int i=0; i<opened.size(); i++)
        {
            ProductHelper.getProductDiagramManager().closeDiagram(opened.get(i));
        }
        long total = (System.currentTimeMillis() - start)/1000;
        int minutes = (int)total/60;
        int seconds = (int)(total-minutes*60);
        
        log(NbBundle.getMessage(ReportTask.class, "Log_processed",
                elements.size(), diagrams.size()));
        String time = minutes==0?seconds + " " + NbBundle.getMessage(ReportTask.class, "Log_seconds"):
            minutes + " " + NbBundle.getMessage(ReportTask.class, "Log_minutes") +
                " "+ seconds + " " + NbBundle.getMessage(ReportTask.class, "Log_seconds");
        time = NbBundle.getMessage(ReportTask.class, "Log_totaltime", time);
        
        if (cancelled)
            log(NbBundle.getMessage(ReportTask.class, "Log_Report_Cancelled") + " " + time);
        else if (success)
            log(NbBundle.getMessage(ReportTask.class, "Log_Report_Successful") + " " + time);
        else
            log(NbBundle.getMessage(ReportTask.class, "Log_Report_Failed") + " " + time);
        
        inputOutput.getOut().flush();
        inputOutput.getOut().close();
    }
    
    
    public boolean cancel()
    {
        cancelled = true;
        return true;
    }
    
    
    private void initialize()
    {
        opened.clear();
        // copyImages();
        File images = new File(getReportDir() + File.separator + "images");
        images.mkdirs();
        images = null;

        for (int i=0; i<files.length; i++)
        {
            copyFiles(files[i]);
        }
        initLog();
        log(NbBundle.getMessage(ReportTask.class, "Log_generating_report_for",
                m_CurrentProject.getName(), getReportDir()));
    }
    
    
    public void loadDataElement(ITreeItem pItem)
    {
        if (cancelled)
        {
            return;
        }
        
        if (pItem instanceof ITreeDiagram)
        {
            ITreeDiagram pTreeDiag = (ITreeDiagram) pItem;
            if (pTreeDiag != null)
            {
                IProxyDiagram pProxyDiagram = pTreeDiag.getDiagram();
                if (pProxyDiagram != null)
                {
                    // process diagrams at last
                    diagrams.add(pTreeDiag);
                }
            }
        }
        else
        {
            createFileForElement(pItem);
            processChildren(pItem);
        }
        
    }
    
    
    public void processChildren(ITreeItem pItem)
    {
        if (pItem != null && !elements.containsKey(pItem.getData().getModelElement().getXMIID()))
            elements.put(pItem.getData().getModelElement().getXMIID(), pItem);
        
        if (pItem != null)
        {
            // ask the ProjectTreeBuilder for the children of this tree item
            ITreeItem[] pTreeItems = m_Builder.retrieveChildItemsSorted(pItem);
            
            if (pTreeItems != null && pTreeItems.length >0)
            {
                int count = pTreeItems.length;
                for (int x = 0; x < count; x++)
                {
                    ITreeItem pTreeItem = pTreeItems[x];
                    if (pTreeItem != null)
                    {
                        if (pItem.getData().getModelElement() instanceof IPackage &&
//								!(pTreeItem.getData().getModelElement() instanceof IPackage) &&
                                !containsPackage(pItem))
                        {
                            packages.add(pItem);
                        }
                        // process each child separately
                        loadDataElement(pTreeItem);
                    }
                }
            }
            else
            {
                if (pItem.getData().getModelElement() instanceof IPackage &&
                        !containsPackage(pItem))
                {
                    packages.add(pItem);
                }
            }
        }
        
    }
    
    
    private boolean containsPackage(ITreeItem pkg)
    {
        for (int i=0; i<packages.size(); i++)
        {
            if (((ITreeItem)packages.get(i)).getData().getModelElementXMIID().
                    equals(pkg.getData().getModelElementXMIID()))
                return true;
        }
        return false;
    }
    
    
    public void createFileForElement(ITreeItem pItem)
    {
        // skip the element that has already been processed
        if (elementFileMap.get(pItem.getData().getModelElement().getXMIID())!=null)
            return;
        
//		if (pItem.getParentItem()!=null)
//		{
//			if (pItem.getParentItem().getData().isSameModelElement(pItem.getData().getModelElement()))
//				return;
//		}
        
        ElementDataObject dataObject = DataObjectFactory.getDataObject(pItem.getData().getModelElement());
        if (dataObject==null)
            return;
        
        String dir = getDirectoryPath(pItem);
        String fileName = convertID(pItem.getData().getModelElement().getXMIID()) + HTML_EXT;
        String file = dir + File.separator + fileName;
        
        if (dataObject.toReport(new File(file)))
        {
            elementFileMap.put(pItem.getData().getModelElement().getXMIID(),
                    getLinkTo(pItem.getData().getModelElement()));
        }
        else
        {
            success = false;
            log(NbBundle.getMessage(ReportTask.class, "Log_error_creating_file",
                    pItem.getData().getModelElement().getElementType()));
        }
    }
    
    
    public void makePage(String fileName, String content)
    {
        File f = new File(fileName);
        try
        {
            FileOutputStream fo = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(fo);
            writer.write(content);
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            success = false;
            ErrorManager.getDefault().notify(e);
        }
    }
    
    
    public static IDiagram loadDiagram(String filename)
    {
        IDiagram diagram = ProductHelper.getProductDiagramManager().openDiagram(filename, false, null);
        opened.add(diagram.getFilename());
        return diagram;
    }
    
    
    private void copyFiles(String name)
    {
        int BUFFER = 2048;
        String localized=name;
        InputStream is=null;
                
        ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        if (loader!=null)
        {
            if (name.indexOf(".html") > -1)
            {
                localized = name.substring(0, name.indexOf(".html")) + "_" +
                        Locale.getDefault() + ".html";
            
                is = loader.getResourceAsStream(localized);
                if (is==null)
                {
                    localized = name.substring(0, name.indexOf(".html")) + "_" +
                            Locale.getDefault().getLanguage() + ".html";
                    is = loader.getResourceAsStream(localized);
                }
            }
            if (is==null)
                is = loader.getResourceAsStream(name);

            String fileName = name.substring(name.lastIndexOf("/")+1);
            byte[] b = new byte[BUFFER];
            int c=0;
            
            try
            {
                String rptDir = getReportDir();
                File script = new File(rptDir, fileName);
                FileOutputStream out = new FileOutputStream(script);
                while ((c=is.read(b,0,BUFFER))!=-1)
                {
                    out.write(b,0,c);
                }
                out.flush();
                out.close();
            }
            catch (IOException e)
            {
                success = false;
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    
    private void copyImages()
    {
        File images = new File(getReportDir() + File.separator + "images");
        String toFolder = images.getAbsolutePath() + File.separatorChar;
        images = null;
        
        IConfigManager configMgr = ProductHelper.getConfigManager();
        
        if (configMgr != null)
        {
            String fromDir = configMgr.getDefaultConfigLocation();
            
            fromDir = fromDir + File.separator + "WebReportSupport" + 
                File.separator + "images" + File.separatorChar;
            
            for (String filename: imageFilenames)
            {
                File imageFile = new File(fromDir +
                    File.separatorChar + filename);
                
                if (imageFile != null)
                {
                    UMLXMLManip.copyFile(
                        imageFile.getAbsolutePath(),
                        toFolder + imageFile.getName());
                }
            }
        }
    }
    
    
    public static void addToImageList(String imageFilename)
    {
        if (!imageFilenames.contains(imageFilename))
            imageFilenames.add(imageFilename);
    }
    
    public String getDirectoryPath(ITreeItem pItem)
    {
        String pkg = "";
        IPackage p=null;
        IDiagram diagram;
        if (pItem.getData().isDiagram())
        {
            diagram= pItem.getData().getDiagram().getDiagram();
            if (diagram==null)
                diagram = loadDiagram(pItem.getData().getDiagram().getFilename());
            p = diagram.getOwningPackage();
        }
        else
        {
            IElement element = pItem.getData().getModelElement();
            if (element != null)
                p = element.getOwningPackage();
        }
        if (p!=null)
            pkg = p.getFullyQualifiedName(true);
        
        String root = getReportDir();
        
        if (pkg.indexOf("::")>0)
            pkg = pkg.replace("::", File.separator);
        if (pkg.trim().length()>0)
            pkg = root + File.separator + pkg;
        else
            pkg = root;
        File dir = new File(pkg);
        if (!dir.exists())
            dir.mkdirs();
        return pkg;
    }
    
    
    public static String getLinkPathToDiagram(IProxyDiagram pItem)
    {
        String pkg = "";
        IPackage p=null;
        
        IDiagram pDiagram = pItem.getDiagram();
        if (pDiagram == null)
            pDiagram = loadDiagram(pItem.getFilename());
        
        if (pDiagram != null)
            p = pDiagram.getOwningPackage();
        
        if (p!=null)
            pkg = p.getFullyQualifiedName(true);
        
        if (pkg.indexOf("::")>0)
            pkg = pkg.replace("::", "/");
        
        return pkg + "/" + StringUtilities.getFileName(
                pItem.getFilename()) + HTML_EXT;
    }
    
    
    public static String getLinkTo(IElement element)
    {
        String pkg = "";
        IPackage p=null;
        if (element==null)
            return "";
        
        p = element.getOwningPackage();
        if (p==null) // Project element
            return convertID(element.getXMIID()) + HTML_EXT;
        if (p!=null)
            pkg = p.getFullyQualifiedName(true);
        
        if (pkg.indexOf("::")>0)
            pkg = pkg.replace("::", "/");
        
        return pkg + "/" + convertID(element.getXMIID()) + HTML_EXT;
    }
    
    
    public static String convertID(String id)
    {
        if (id != null && id.length() > 0)
        {
            String s1 = StringUtilities.replaceAllSubstrings(id, ".", "");
            return StringUtilities.replaceAllSubstrings(s1, "-", "");
        }
        return "";
    }
    
    
    public String getJavaScriptPath(IDiagram diagram)
    {
        String path ="..";
        IPackage pkg = diagram.getOwningPackage();
        while (!pkg.equals(pkg.getProject()))
        {
            path = path + "/..";
            pkg=pkg.getOwningPackage();
        }
        return path;
    }
    
    
    public static String getPathToReportRoot(IElement element)
    {
        // workaround for 100686, but association end should have its owner set to association object
        if (element instanceof IAssociationEnd)
            return getPathToReportRoot(((IAssociationEnd)element).getAssociation());
        
        String path ="..";
        IPackage pkg = element.getOwningPackage();
        if (pkg == null)
            return "";
        while (!pkg.equals(pkg.getProject()))
        {
            path = path + "/..";
            pkg=pkg.getOwningPackage();
        }
        return path+"/";
    }
    
   
    public void processSummaryPages()
    {
        String projectName = m_CurrentProject.getName();
        
        String projectDoc = m_CurrentProject.getDocumentation();
        
        String link;
        String pname;
        IPackage pkg;
        String doc;
        
        StringBuilder buffer = new StringBuilder();
        String filename = "";
        String template;
        
        log(NbBundle.getMessage(ReportTask.class, "Log_generating_summary_files")); // NOI18N
        
        // 1. generate alldiagrams.html
        
        buffer = readTemplate("org/netbeans/modules/uml/reporting/templates/alldiagrams.html"); // NOI18N
        template = buffer.toString();
        template = template.replaceAll("%ALL_DIAGRAMS%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Category_AllDiagrams")); // NOI18N
        buffer = new StringBuilder();
        
        ITreeDiagram[] treeDiagrams = new ITreeDiagram[diagrams.size()];
        diagrams.toArray(treeDiagrams);
        Arrays.sort(treeDiagrams, comparator);
        
        for (int i=0; i<treeDiagrams.length; i++)
        {
            
            ITreeDiagram diagram = (ITreeDiagram)treeDiagrams[i];
            pname = diagram.getDiagram().getDiagram().getName();
            link = diagramFileMap.get(diagram.getDiagram().getXMIID());
            /*
                <A HREF="diagram1.html" title="class diagram in javax.swing" target="elementFrame">Class Diagram</A>
                <BR>
             */
//            buffer.append("<img src=\"images/" + DiagramTypesManager.instance().
//                    getOpenIcon(diagram.getDiagram().getDiagram()) +
//                    ".png" + "\" border=n>&nbsp;<A HREF=\"" + link + 
//                    "\" target=\"elementframe\">" + pname + "</A>\n<BR>\n"); // NOI18N
            
            String imageName = ImageUtil.instance().getDiagramTypeImageName(
                diagram.getDiagram().getDiagram().getDiagramKind());
            
            ReportTask.addToImageList(imageName);
            
            buffer.append("<img src=\"images/" + imageName + 
                "\" border=n>&nbsp;<A HREF=\"" + link +  // NOI18N
                "\" target=\"elementframe\">" + pname + "</A>\n<BR>\n"); // NOI18N
        }
        
        String content = buffer.toString();
        template = template.replace("%DIAGRAM_CONTENT%", content); // NOI18N
        template = template.replaceAll("%CHARSET%", // NOI18N
            System.getProperty("file.encoding")); // NOI18N
        filename = getReportDir() + File.separator + "alldiagrams.html"; // NOI18N
        makePage(filename, template.toString());
        
        
        // 2. generate allelements.html
        buffer = readTemplate("org/netbeans/modules/uml/reporting/templates/allelements.html"); // NOI18N
        template = buffer.toString();
        template = template.replaceAll("%ALL_ELEMENTS%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Category_AllElements")); // NOI18N
        template = template.replaceAll("%CHARSET%", // NOI18N
                System.getProperty("file.encoding")); // NOI18N
                
        buffer = new StringBuilder();
        
        ITreeItem[] items = new ITreeItem[elements.size()];
        elements.values().toArray(items);
        Arrays.sort(items, comparator);
        
        for (int i=0; i<items.length; i++)
        {
            if (((ITreeItem)items[i]) instanceof ITreeFolder)
                continue;
            
            IElement element = ((ITreeItem)items[i]).getData().getModelElement();
            /*
            <A HREF="javax/swing/AbstractAction.html" title="class in javax.swing" target="classFrame">AbstractAction</A>
            <BR>
             */
            
            pname = ((ITreeItem)items[i]).getName();
            if (elementFileMap.containsKey(element.getXMIID()))
            {
                link = elementFileMap.get(element.getXMIID());
                buffer.append("<A HREF=\"" + link + "\" target=\"elementframe\">" + pname + "</A>\n<BR>\n"); // NOI18N
            }
            else
            {
//              buffer.append(pname + "\n<BR>\n"); // NOI18N
            }
        }
        template = template.replace("%ELEMENT_CONTENT%", buffer.toString()); // NOI18N
        filename = getReportDir() + File.separator + "allelements.html"; // NOI18N
        makePage(filename, template);
        
        
        // 3. generate overview.html
        
        buffer = readTemplate("org/netbeans/modules/uml/reporting/templates/overview.html"); // NOI18N
        template = buffer.toString();
        template = template.replace("%PROJECT_NAME%", m_CurrentProject.getName()); // NOI18N
        template = template.replaceAll("%OVERVIEW%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Overview")); // NOI18N
        template = template.replaceAll("%ALL_ELEMENTS%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Category_AllElements")); // NOI18N
        template = template.replaceAll("%ALL_DIAGRAMS%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Category_AllDiagrams")); // NOI18N
        template = template.replaceAll("%PACKAGES%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Category_Package")); // NOI18N
        template = template.replaceAll("%CHARSET%",  // NOI18N
                System.getProperty("file.encoding")); // NOI18N
        StringBuilder buff = new StringBuilder();
        
        items = new ITreeItem[packages.size()];
        packages.toArray(items);
        Arrays.sort(items, comparator);
        
        for (int i=0; i<items.length; i++)
        {
            link = (String)elementFileMap.get((items[i].getData().getModelElement()).getXMIID());
            
            if (link!=null)
            {
                pname = ((IPackage)(items[i].getData().getModelElement())).getFullyQualifiedName(false);
                if (items[i].getData().getModelElement() instanceof IProject)
                    pname = "&lt;" + NbBundle.getMessage(ReportTask.class,"default_package") + ">"; // NOI18N
                link = link.substring(0, link.indexOf(HTML_EXT)) + FRAME + HTML_EXT;
                /* sample html section
                <FONT CLASS="FrameItemFont"><A HREF="java/applet/package-frame.html" target="packageFrame">java.applet</A></FONT>
                <BR>
                 */
                buff.append("<FONT CLASS=\"FrameItemFont\"><A HREF=\"" + link + "\"target=\"packageFrame\">" + pname + "</A></FONT><BR>\n"); // NOI18N
            }
        }
        template = template.replace("%PACKAGE_CONTENT%", buff.toString());
        filename = getReportDir() + File.separator + OVERVIEW_FILE;
        makePage(filename, template);
        
        
        // 4. generate overview-summary.html
        
        buffer = readTemplate("org/netbeans/modules/uml/reporting/templates/overview-summary.html"); // NOI18N
        template = buffer.toString();
        template = template.replace("%PROJECT_NAME%", projectName); // NOI18N
        template = template.replace("%PROJECT_DESCRIPTION%", projectDoc); // NOI18N
        template = template.replaceAll("%OVERVIEW%",  // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Overview")); // NOI18N
        template = template.replaceAll("%PACKAGE%",  // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Package")); // NOI18N
        template = template.replaceAll("%ELEMENT%",  // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Element")); // NOI18N
        template = template.replaceAll("%DIAGRAM%",  // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Diagram")); // NOI18N
        template = template.replaceAll("%HELP%",  // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Help")); // NOI18N
        template = template.replaceAll("%CHARSET%",  // NOI18N
                System.getProperty("file.encoding")); // NOI18N
        
        buff = new StringBuilder();
        
        /*
            <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
            <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
            <TH ALIGN="left" COLSPAN="2"><FONT SIZE="+2">
            <B>UML_PROJECT_NAME Packages</B></FONT></TH>
            </TR>
            <TR BGCOLOR="white" CLASS="TableRowColor">
            <TD WIDTH="20%"><B><A HREF="java/applet/package-summary.html">java.applet</A></B></TD>
            <TD>Provides the classes necessary to create an applet and the classes an applet
            uses to communicate with its applet context.</TD>
            </TR>
            </TABLE>
         */
        
        if (packages.size()>0)
        {
            buff.append("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\n"); // NOI18N
            buff.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n"); // NOI18N
            buff.append("<TH ALIGN=\"left\" COLSPAN=\"2\"><FONT SIZE=\"+2\">\n"); // NOI18N
            buff.append("<B>" + projectName + " " + // NOI18N
                    NbBundle.getMessage(ReportTask.class,"packages") + "</B></FONT></TH>\n"); // NOI18N
            buff.append("</TR>\n"); // NOI18N
            
            
            for (int i=0; i<items.length; i++)
            {
                link = (String)elementFileMap.get(items[i].getData().getModelElement().getXMIID());
                
                if (link==null)
                    continue;
                
                pkg = (IPackage)items[i].getData().getModelElement();
                pname = pkg.getFullyQualifiedName(false);
                
                if (pkg instanceof IProject)
                    pname = "&lt;" + NbBundle.getMessage(ReportTask.class,"default_package") + ">"; // NOI18N
                
                link = (String)elementFileMap.get(pkg.getXMIID());
                doc = pkg.getDocumentation();
                
                if (doc==null || doc.trim().equals(""))
                    doc = "&nbsp;"; // NOI18N
                
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\n"); // NOI18N
                buff.append("<TD WIDTH=\"20%\"><B><A HREF=\"" + link + "\">" + pname + "</A></B></TD>\n"); // NOI18N
                buff.append("<TD>" + doc + "</TD>\n"); // NOI18N
                buff.append("</TR>\n"); // NOI18N
            }
            buff.append("</TABLE>\n"); // NOI18N
        }
        template = template.replace("%PACKAGE_TABLE%", buff.toString()); // NOI18N
        filename = getReportDir() + File.separator + "overview-summary.html"; // NOI18N
        makePage(filename, template);
    }
    
    
    public static StringBuilder readTemplate(String resource)
    {
        StringBuilder buffer=new StringBuilder();
        String localized = resource;
        ClassLoader loader = ReportTask.class.getClassLoader();
        InputStream is=null;
        
        if (loader!=null)
        {
            if (resource.indexOf(".html") > -1) // NOI18N
            {
                localized = resource.substring(0, resource.indexOf(".html")) + "_" + // NOI18N
                        Locale.getDefault() + ".html"; // NOI18N
                is = loader.getResourceAsStream(localized);
                if (is==null)
                {
                    localized = resource.substring(0, resource.indexOf(".html")) + "_" + // NOI18N
                            Locale.getDefault().getLanguage() + ".html"; // NOI18N
                    is = loader.getResourceAsStream(localized);
                }
            }
            
            if (is==null)
                is = loader.getResourceAsStream(resource);
            try
            {
                BufferedReader reader=new BufferedReader(new InputStreamReader(is));
                
                int length = 1000;
                char[] cbuff = new char[length];
                
                String line=null;
                
                
                while (reader.read(cbuff,0,length)!=-1)
                {
                    buffer.append(cbuff);
                    cbuff = new char[length];
                }
            }
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
        return buffer;
    }
    
    
    public static void log(String msg)
    {
        PrintWriter out = inputOutput.getOut();
        out.println(msg);
        out.flush();
        
    }
    
    private void initLog()
    {
        TopComponent tc = WindowManager.getDefault().findTopComponent("output"); // NOI18N
        tc.open();
        tc.requestActive();
        tc.toFront();
        
        try
        {
            inputOutput.getOut().reset();
        }
        catch(IOException e)
        {
            // ignore, just append the log message to the output pane
        }
        inputOutput.select();
        inputOutput.setOutputVisible(true);
    }
    
    
    public void launchReport()
    {
        copyImages();
        String rptDir = getReportDir();
        String fileLaunch = rptDir + File.separator + "index.html"; // NOI18N
        
        File locationFile = new File(fileLaunch);
        
        // launch the index html file in that directory
        try
        {
            IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
            if((ui != null) && (locationFile.exists() == true))
            {
                ui.displayInBrowser(locationFile.toURI().toURL());
            }
        }
        catch(IOException e)
        {
            ErrorManager.getDefault().log(e.getMessage());
        }
    }
    
    
    private static class DisplayNameComparator implements Comparator
    {
        
        private Comparator COLLATOR = Collator.getInstance();
        
        public int compare(Object o1, Object o2)
        {
            
            if ( !( o1 instanceof ITreeItem ) )
            {
                return 1;
            }
            if ( !( o2 instanceof ITreeItem ) )
            {
                return -1;
            }
            
            ITreeItem e1 = (ITreeItem)o1;
            ITreeItem e2 = (ITreeItem)o2;
            
            if (e1.getData().getModelElement() instanceof IProject)
                return -1;
            
            if (e1.getData().getModelElement() instanceof IPackage &&
                    e2.getData().getModelElement() instanceof IPackage)
            {
                return COLLATOR.compare(((IPackage)e1.getData().getModelElement()).getFullyQualifiedName(false),
                        ((IPackage)e2.getData().getModelElement()).getFullyQualifiedName(false));
            }
            
            return COLLATOR.compare(e1.getName(), e2.getName());
        }
    }
    
}
