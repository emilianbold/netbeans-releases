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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * PageFlowController.java
 *
 * Created on March 1, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities.Scope;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbPreferences;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditCookie;

/**
 *
 * @author joelle lam
 */
public class PageFlowController {

    private PageFlowView view;
    private JSFConfigModel configModel;
    private Collection<FileObject> webFiles;
    private DataObject configDataObj;

    private final HashMap<NavigationCase, NavigationCaseEdge> navCase2NavCaseEdge = new HashMap<NavigationCase, NavigationCaseEdge>();
    private HashMap<NavigationRule, String> navRule2String = new HashMap<NavigationRule, String>();
    private final HashMap<String, Page> pageName2Page = new HashMap<String, Page>(); //Should this be synchronized.
    //    public static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18NF
    private static final String NO_WEB_FOLDER_WARNING = NbBundle.getMessage(PageFlowController.class, "MSG_NoWebFolder");
    private static final String NO_WEB_FOLDER_TITLE = NbBundle.getMessage(PageFlowController.class, "TLE_NoWebFolder");

    /** Creates a new instance of PageFlowController
     * @param context
     * @param view
     */
    public PageFlowController(JSFConfigEditorContext context, PageFlowView view) {
        this.view = view;
        FileObject configFile = context.getFacesConfigFile();

        try {
            configDataObj = DataObject.find(configFile);
        } catch (DataObjectNotFoundException donfe) {
            donfe.printStackTrace();
        }
        configModel = ConfigurationUtils.getConfigModel(configFile, true);
        Project project = FileOwnerQuery.getOwner(configFile);
        //        webFolder = project.getProjectDirectory().getFileObject(DEFAULT_DOC_BASE_FOLDER);
        webFolder = PageFlowView.getWebFolder(configFile);
        if (webFolder == null) {
            //            DialogDescriptor desc = new DialogDescriptor(
            //                    NbBundle.getMessage(PageFlowController.class, "NotWebFolder"),
            //                    NbBundle.getMessage(PageFlowController.class, "TLE_NoWebFolder"),
            //                    false,
            //                    DialogDescriptor.WARNING_MESSAGE,
            //                    DialogDescriptor.NO_OPTION,
            //                    null);
            //
            //            Dialog d = DialogDisplayer.getDefault().createDialog(desc);
            //            d.setVisible(true);
            if (isShowNoWebFolderDialog()) {

                final NotWebFolder panel = new NotWebFolder(NO_WEB_FOLDER_WARNING);
                DialogDescriptor descriptor = new DialogDescriptor(panel, NO_WEB_FOLDER_TITLE, true, NotifyDescriptor.PLAIN_MESSAGE, NotifyDescriptor.YES_OPTION, null);
                descriptor.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
                descriptor.setClosingOptions(new Object[]{NotifyDescriptor.OK_OPTION});
                descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
                final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
                d.setSize(380, 180);
                d.setVisible(true);

                setShowNoWebFolderDialog(panel.getShowDialog());
            }


            webFiles = new LinkedList<FileObject>();
        } else {
            webFiles = getAllProjectRelevantFilesObjects();
        }
    }
    private static final String PROP_SHOW_NO_WEB_FOLDER = "showNoWebFolder"; // NOI18N

    public void setShowNoWebFolderDialog(boolean show) {
        getPreferences().putBoolean(PROP_SHOW_NO_WEB_FOLDER, show);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(PageFlowController.class);
    }

    public boolean isShowNoWebFolderDialog() {
        return getPreferences().getBoolean(PROP_SHOW_NO_WEB_FOLDER, true);
    }

    private PropertyChangeListener pcl;
    private FileChangeListener fcl;

    public void registerListeners() {
        if (pcl == null) {
            pcl = new FacesModelPropertyChangeListener(this);
            if (configModel != null) {
                configModel.addPropertyChangeListener(pcl);
            }
        }
        FileObject webFolder = getWebFolder();
        if (fcl == null) {
            fcl = new WebFolderListener(this);
            if (webFolder != null) {
                try {
                    webFolder.getFileSystem().addFileChangeListener(fcl);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Unregister any listeners.
     */
    public void unregisterListeners() {
        if (pcl != null && configModel != null) {
            configModel.removePropertyChangeListener(pcl);
            pcl = null;
        }

        FileObject webFolder = getWebFolder();
        if (fcl != null && webFolder != null) {
            try {
                webFolder.getFileSystem().removeFileChangeListener(fcl);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean isCurrentScope(Scope scope) {
        return PageFlowToolbarUtilities.getInstance(view).getCurrentScope().equals(scope);
    }


    /**
     * Set From outcome by default.
     * @param source
     * @param target
     * @param pinNode if null then it was not conntect to a pin.
     * @return
     */
    public NavigationCase createLink(Page source, Page target, Pin pinNode) {

        String sourceName = source.getDisplayName();
        int caseNum = 1;

        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        NavigationRule navRule = getRuleWithFromViewID(facesConfig, source.getDisplayName());
        NavigationCase navCase = configModel.getFactory().createNavigationCase();
        if (navRule == null) {
            navRule = configModel.getFactory().createNavigationRule();
            FacesModelUtility.setFromViewId(navRule, source.getDisplayName());
            facesConfig.addNavigationRule(navRule);
            navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
        } else {
            caseNum = getNewCaseNumber(navRule);
        }
        String caseName = CASE_STRING + Integer.toString(caseNum);

        if (pinNode != null) {
            pinNode.setFromOutcome(caseName);
        }
        navCase.setFromOutcome(caseName);

        FacesModelUtility.setToViewId(navCase, target.getDisplayName());
        navRule.addNavigationCase(navCase);


        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return navCase;
    }

    public void updatePageItems(Page pageNode) {
        view.resetNodeWidget(pageNode, true);
        view.validateGraph();
    }

    private static final String CASE_STRING = "case";

    private int getNewCaseNumber(NavigationRule navRule) {
        Collection<String> caseOutcomes = new HashSet<String>();
        List<NavigationCase> navCases = navRule.getNavigationCases();
        for (NavigationCase navCase : navCases) {
            caseOutcomes.add(navCase.getFromOutcome());
            //            caseOutcomes.add(navCase.getFromAction());
        }

        int caseNum = 1;
        while (true) {
            if (!caseOutcomes.contains(CASE_STRING + Integer.toString(caseNum))) {
                return caseNum;
            }
            caseNum++;
        }
    }

    /**
     * @return the navigation rule.  This will be null if none was found
     **/
    private NavigationRule getRuleWithFromViewID(FacesConfig facesConfig, String fromViewId) {

        for (NavigationRule navRule : facesConfig.getNavigationRules()) {
            String rulefromViewId = FacesModelUtility.getFromViewIdFiltered(navRule);
            if (rulefromViewId != null && rulefromViewId.equals(fromViewId)) {
                //  Match Found
                return navRule;
            }
        }

        return null;
    }


    private Collection<FileObject> getAllProjectRelevantFilesObjects() {
        return getProjectKnownFileOjbects(getWebFolder());
    }


    private Collection<FileObject> getProjectKnownFileOjbects(FileObject folder) {
        Collection<FileObject> projectKnownFiles = new LinkedList<FileObject>();

        FileObject[] childrenFiles = new FileObject[]{};
        if (folder != null) {
            childrenFiles = folder.getChildren();
        }
        for (FileObject file : childrenFiles) {
            if (!file.isFolder()) {
                if (isKnownFile(file)) {
                    projectKnownFiles.add(file);
                }
            } else if (isKnownFolder(file)) {
                projectKnownFiles.addAll(getProjectKnownFileOjbects(file));
            }
        }

        return projectKnownFiles;
    }

    public final boolean isKnownFile(FileObject file) {
        if (file.getMIMEType().equals("text/x-jsp") && !file.getExt().equals("jspf")) {
            return true;
        } else if (file.getMIMEType().equals("text/html")) {
            return true;
        }
        return false;
    }

    public final boolean isKnownFolder(FileObject folder) {
        /* If it is not a folder return false*/
        if (!folder.isFolder()) {
            return false;
        }
        /* If it does not exist within WebFolder return false */
        if (!folder.getPath().contains(getWebFolder().getPath())) {
            return false;
        }
        /* If it exists withing WEB-INF or META-INF return false */
        if (folder.getPath().contains("WEB-INF") || folder.getPath().contains("META-INF")) {
            return false;
        }
        return true;
    }

    /**
     * Setup The Graph
     * Should only be called by init();
     *
     **/
    public boolean setupGraph() {
        view.saveLocations();
        return setupGraphNoSaveData();
    }

    PropertyChangeListener otherFacesConfigListener = null;

    private PropertyChangeListener getOtherFacesConfigListener() {
        if (otherFacesConfigListener == null) {
            return new OtherFacesModelListener();
        }
        return otherFacesConfigListener;
    }

    private void removeOtherFacesConfigListener() {
        WebModule webModule = WebModule.getWebModule(getWebFolder());
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        for (FileObject aConfigFile : configFiles) {
            JSFConfigModel aConfigModel = ConfigurationUtils.getConfigModel(aConfigFile, true);
            aConfigModel.removePropertyChangeListener(otherFacesConfigListener);
        }
    }

    public int getPageCount() {
        return webFiles.size();
    }

    public boolean setupGraphNoSaveData() {
        LOGGER.entering(PageFlowController.class.toString(), "setupGraphNoSaveData()");
        assert configModel != null;
        //        assert webFolder != null;
        assert webFiles != null;

        /* This listener is only created when it was a All_FACES scope */
        if (otherFacesConfigListener != null) {
            removeOtherFacesConfigListener();
        }

        view.clearGraph();
        clearPageName2Page();
        navCase2NavCaseEdge.clear();
        navRule2String.clear();

        FacesConfig facesConfig = configModel.getRootComponent();

        if (facesConfig == null) {
            return false;
        }

        List<NavigationRule> rules = null;
        if (isCurrentScope(Scope.SCOPE_FACESCONFIG)) {
            rules = facesConfig.getNavigationRules();
            for (NavigationRule navRule : rules) {
                navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
            }
            Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
            createFacesConfigPages(pagesInConfig);
        } else if (isCurrentScope(Scope.SCOPE_PROJECT)) {
            rules = facesConfig.getNavigationRules();
            for (NavigationRule navRule : rules) {
                navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
            }
            Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
            createAllProjectPages(pagesInConfig);
        } else if (isCurrentScope(Scope.SCOPE_ALL_FACESCONFIG)) {
            List<NavigationRule> allRules = new ArrayList<NavigationRule>();
            FileObject webFolder = getWebFolder();
            if (webFolder != null) {
                WebModule webModule = WebModule.getWebModule(webFolder);
                FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
                for (FileObject aConfigFile : configFiles) {
                    JSFConfigModel aConfigModel = ConfigurationUtils.getConfigModel(aConfigFile, true);
                    allRules.addAll(aConfigModel.getRootComponent().getNavigationRules());
                    if (!configModel.equals(aConfigModel)) {
                        aConfigModel.addPropertyChangeListener(getOtherFacesConfigListener());
                    }
                }
                for (NavigationRule navRule : allRules) {
                    navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
                }
                Collection<String> pagesInConfig = getFacesConfigPageNames(allRules);
                createFacesConfigPages(pagesInConfig);
                rules = allRules;
            } else {
                /* If no web module exists don't worry about other faces-config files */
                rules = facesConfig.getNavigationRules();
                for (NavigationRule navRule : rules) {
                    navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
                }
                Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
                createAllProjectPages(pagesInConfig);
            }
        }
        createAllEdges(rules);
        view.validateGraph();
        LOGGER.log(new LogRecord(Level.INFO, "PageFlowEditor # Rules: " + rules.size() + "\n" + "               # WebPages: " + webFiles.size() + "\n" + "               # TotalPages: " + pageName2Page.size()));
        LOGGER.exiting(PageFlowController.class.toString(), "setupGraphNoSaveData()");

        return true;
    }

    private class OtherFacesModelListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {

            EventQueue.invokeLater(new Runnable() {

                public void run() {

                    setupGraph();
                }
            });
        }
    }


    private void createAllEdges(List<NavigationRule> rules) {

        List<NavigationRule> editableRules = configModel.getRootComponent().getNavigationRules();
        for (NavigationRule rule : rules) {
            List<NavigationCase> navCases = rule.getNavigationCases();

            /* this is for ALL_FACES_CONFIG scope*/
            boolean isModifableEdge = editableRules.contains(rule) ? true : false;

            for (NavigationCase navCase : navCases) {

                NavigationCaseEdge navEdge = new NavigationCaseEdge(this, navCase);
                navCase2NavCaseEdge.put(navCase, navEdge);
                navEdge.setModifiable(isModifableEdge);
                if (navEdge.getFromViewId() != null && navEdge.getToViewId() != null) {
                    createEdge(navEdge);
                }
            }
        }
    }

    public void createEdge(NavigationCaseEdge caseNode) {
        String fromPage = caseNode.getFromViewId();
        String toPage = caseNode.getToViewId();
        if (getPageName2Page(fromPage) == null || getPageName2Page(toPage) == null) {
            System.err.println("Why is this node null? CaseNode: " + caseNode);
            System.err.println("FromPage: " + fromPage);
            System.err.println("ToPage: " + toPage);
            Thread.dumpStack();
        } else {
            view.createEdge(caseNode, getPageName2Page(fromPage), getPageName2Page(toPage));
        }
    }


    private Collection<String> getFacesConfigPageNames(List<NavigationRule> navRules) {
        // Get all the pages in the faces config.  But don't list them twice.
        Collection<String> pages = new HashSet<String>();
        for (NavigationRule navRule : navRules) {
            String pageName = FacesModelUtility.getFromViewIdFiltered(navRule);
            pages.add(pageName);
            List<NavigationCase> navCases = navRule.getNavigationCases();
            for (NavigationCase navCase : navCases) {
                //                String toPage = navCase.getToViewId();
                String toPage = FacesModelUtility.getToViewIdFiltered(navCase);
                if (toPage != null) {
                    pages.add(toPage);
                }
            }
        }
        return pages;
    }

    public java.util.Stack<String> PageFlowCreationStack = new java.util.Stack<String>();
    private int PageFlowCreationCount = 0;

    public Page createPageFlowNode(Node node) {
        Page pageNode = new Page(this, node);
        Calendar rightNow = Calendar.getInstance();
        PageFlowCreationStack.push("\n" + PageFlowCreationCount + ". " + rightNow.get(Calendar.MINUTE) + ":" + rightNow.get(Calendar.SECOND) + " -  " + pageNode);
        PageFlowCreationCount++;
        return pageNode;
    }

    /*
     * Create PageFlowNode with no backing page.
     */
    public Page createPage(String pageName) {
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(pageName);
        Page node = createPageFlowNode(tmpNode);
        return node;
    }

    public java.util.Stack<String> PageFlowDestroyStack = new java.util.Stack<String>();
    private int PageFlowDestroyCount = 0;

    public void destroyPageFlowNode(Page pageNode) {
        pageNode.destroy2();
        Calendar rightNow = Calendar.getInstance();
        PageFlowDestroyStack.push("\n" + PageFlowDestroyCount + ". " + rightNow.get(Calendar.MINUTE) + ":" + rightNow.get(Calendar.SECOND) + " -  " + pageNode);
        PageFlowDestroyCount++;
    }

    private void createAllProjectPages(Collection<String> pagesInConfig) {

        Collection<String> pages = new HashSet<String>(pagesInConfig);

        //Create all pages in the project...
        for (FileObject webFile : webFiles) {
            try {
                //DISPLAYNAME:
                String webFileName = Page.getFolderDisplayName(getWebFolder(), webFile);
                Page node = null;
                node = createPageFlowNode((DataObject.find(webFile)).getNodeDelegate());
                view.createNode(node, null, null);
                //Do not remove the webFile page until it has been created with a data Node.  If the dataNode throws and exception, then it can be created with an Abstract node.
                pages.remove(webFileName);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (ClassCastException cce) {
                cce.printStackTrace();
            }
        }

        //Create any pages that don't actually exist but are defined specified by the config file.
        for (String pageName : pages) {
            if (pageName != null) {
                Node tmpNode = new AbstractNode(Children.LEAF);
                tmpNode.setName(pageName);
                Page node = createPageFlowNode(tmpNode);
                view.createNode(node, null, null);
            }
        }
    }

    /**
     * Givena pageName, look through the list of predefined webFiles and return the matching fileObject
     * @return FileObject for which the match was found or null of none was found.
     **/
    private FileObject getFileObject(String pageName) {
        for (FileObject webFile : webFiles) {
            //DISPLAYNAME:
            String webFileName = Page.getFolderDisplayName(getWebFolder(), webFile);
            //            String webFileName = webFile.getNameExt();
            if (webFileName.equals(pageName)) {
                return webFile;
            }
        }
        return null;
    }

    private void createFacesConfigPages(Collection<String> pagesInConfig) {
        Collection<String> pages = new HashSet<String>(pagesInConfig);

        for (String pageName : pages) {
            if (pageName != null) {
                FileObject file = getFileObject(pageName);
                Node wrapNode = null;
                if (file == null) {
                    wrapNode = new AbstractNode(Children.LEAF);
                    wrapNode.setName(pageName);
                } else {
                    try {
                        wrapNode = (DataObject.find(file)).getNodeDelegate();
                    } catch (DataObjectNotFoundException donfe) {
                        donfe.printStackTrace();
                    }
                }
                Page node = createPageFlowNode(wrapNode);
                view.createNode(node, null, null);
            }
        }
    }

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    public Page removePageName2Page(Page pageNode, boolean destroy) {
        return removePageName2Page(pageNode.getDisplayName(), destroy);
    }

    public Page removePageName2Page(String displayName, boolean destroy) {
        LOGGER.finest("PageName2Page: remove " + displayName);
        printThreadInfo();
        synchronized (pageName2Page) {
            Page node = pageName2Page.remove(displayName);
            if (destroy) {
                destroyPageFlowNode(node);
            }
            return node;
        }
    }

    /**
     * Replace page name in PageName2Node HasMap
     * @param Page node, String newName, String oldName
     **/
    public void replacePageName2Page(Page node, String newName, String oldName) {

        LOGGER.finest("PageName2Page: replace " + oldName + " to " + newName);
        printThreadInfo();
        synchronized (pageName2Page) {
            Page node2 = pageName2Page.remove(oldName);
            if (node == null || node2 == null) {
                System.err.println("PageFlowEditor: Trying to add Page [" + oldName + "] but it is null.");
            }
            pageName2Page.put(newName, node);
        }
    }

    public void clearPageName2Page() {
        LOGGER.finest("PageName2Page: clear");

        //        printThreadInfo();
        Set<String> keys;
        synchronized (pageName2Page) {
            keys = new HashSet<String>(pageName2Page.keySet());
        }
        for (String key : keys) {
            Page node = removePageName2Page(key, true);
        }
        //            pageName2Node.clear();
        //        }
    }

    public void putPageName2Page(String displayName, Page pageNode) {

        LOGGER.finest("PageName2Page: put " + displayName);
        printThreadInfo();
        if (pageNode == null) {
            throw new RuntimeException("PageFlowEditor: Trying to add Page [" + displayName + "] but it is null.");
        }
        synchronized (pageName2Page) {
            pageName2Page.put(displayName, pageNode);
        }
    }

    public Page getPageName2Page(String displayName) {
        printThreadInfo();
        synchronized (pageName2Page) {
            /*
             * Begin Test
             */
            /* Page pageNode = pageName2Page.remove(displayName);
            if (pageNode != null) {
            Page pageNode2 = pageName2Page.get(displayName);
            if (pageNode2 != null) {
            throw new RuntimeException("Why are there two of the same page?: " + displayName + "\n PageNode1: " + pageNode + "\n PageNode2:" + pageNode2);
            }
            putPageName2Page(displayName, pageNode);
            } */
            /*
             * End Test
             */
            return pageName2Page.get(displayName);
        }
    }

    private Thread t = null;

    public void printThreadInfo() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Thread.dumpStack();
            throw new RuntimeException("Not a Dispatched Thread");
        }
    }


    public void renamePageInModel(String oldDisplayName, String newDisplayName) {
        FacesModelUtility.renamePageInModel(configModel, oldDisplayName, newDisplayName);
    }


    public void removeSceneNodeEdges(Page pageNode) {

        Collection<NavigationCaseEdge> navCaseNodes = view.getNodeEdges(pageNode);
        for (NavigationCaseEdge navCaseNode : navCaseNodes) {
            try {
                navCaseNode.destroy();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            //            view.removeEdge(navCaseNode);
        }
    }


    /**
     * Remove all rules and cases with this pagename.
     * @param displayName
     */
    public void removePageInModel(String displayName) {
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for (NavigationRule navRule : navRules) {
            String fromViewId = FacesModelUtility.getFromViewIdFiltered(navRule);
            if (fromViewId != null && fromViewId.equals(displayName)) {
                //if the rule is removed, don't check the cases.
                facesConfig.removeNavigationRule(navRule);
            } else {
                List<NavigationCase> navCases = navRule.getNavigationCases();
                for (NavigationCase navCase : navCases) {
                    //                    String toViewId = navCase.getToViewId();
                    String toViewId = FacesModelUtility.getToViewIdFiltered(navCase);
                    if (toViewId != null && toViewId.equals(displayName)) {
                        navRule.removeNavigationCase(navCase);
                    }
                }
            }
        }

        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }



    private FileObject webFolder = null;

    /**
     * Gets the WebFolder which contains the jsp pages.
     * @return FileObject webfolder
     */
    public FileObject getWebFolder() {
        //        assert webFolder.isValid();
        return webFolder;
    }


    public boolean isPageInAnyFacesConfig(String name) {
        WebModule webModule = WebModule.getWebModule(getWebFolder());
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        for (FileObject aConfigFile : configFiles) {
            JSFConfigModel aConfigModel = ConfigurationUtils.getConfigModel(aConfigFile, true);
            List<NavigationRule> rules = aConfigModel.getRootComponent().getNavigationRules();
            Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
            if (pagesInConfig.contains(name)) {
                return true; /* Return as soon as you find one. */
            }
        }
        return false;
    }

    public boolean isNavCaseInFacesConfig(NavigationCaseEdge navEdge) {
        NavigationCase navCase = getNavCase2NavCaseEdge(navEdge);
        JSFConfigComponent navRule = navCase.getParent();
        if (configModel.getRootComponent().getNavigationRules().contains(navRule)) {
            return true;
        }
        return false;
    }

    public void changeToAbstractNode(Page oldNode, String displayName) {
        //1. Make Old Node an abstract node
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(displayName);
        oldNode.replaceWrappedNode(tmpNode); //Does this take care of pageName2Node?
        view.resetNodeWidget(oldNode, true);
    }


    public DataObject getConfigDataObject() {
        return configDataObj;
    }

    public void saveLocation(String oldDisplayName, String newDisplayName) {
        view.saveLocation(oldDisplayName, newDisplayName);
    }

    // WebFiles Wrappers
    public final boolean removeWebFile(FileObject fileObj) {
        return webFiles.remove(fileObj);
    }

    public final boolean addWebFile(FileObject fileObj) {
        return webFiles.add(fileObj);
    }

    public final boolean containsWebFile(FileObject fileObj) {
        return webFiles.contains(fileObj);
    }


    public final void putNavCase2NavCaseEdge(NavigationCase navCase, NavigationCaseEdge navCaseEdge) {
        navCase2NavCaseEdge.put(navCase, navCaseEdge);
    }

    public final NavigationCaseEdge getNavCase2NavCaseEdge(NavigationCase navCase) {
        return navCase2NavCaseEdge.get(navCase);
    }

    private final NavigationCase getNavCase2NavCaseEdge(NavigationCaseEdge navEdge) {
        Set<Entry<NavigationCase, NavigationCaseEdge>> entries = navCase2NavCaseEdge.entrySet();
        for (Entry entry : entries) {
            if (entry.getValue().equals(navEdge)) {
                return (NavigationCase) entry.getKey();
            }
        }
        return null;
    }

    public final NavigationCaseEdge removeNavCase2NavCaseEdge(NavigationCase navCase) {
        return navCase2NavCaseEdge.remove(navCase);
    }

    //NavRule2String wrappers
    public final String removeNavRule2String(NavigationRule navRule) {
        return navRule2String.remove(navRule);
    }

    public final String putNavRule2String(NavigationRule navRule, String navRuleName) {
        return navRule2String.put(navRule, navRuleName);
    }

    public PageFlowView getView() {
        return view;
    }


    public void setModelNavigationCaseName(NavigationCase navCase, String newName) {
        configModel.startTransaction();

        //By default check from outcome first.  Maybe this should be the expectation.
        if (navCase.getFromOutcome() != null) {
            navCase.setFromOutcome(newName);
        }
        if (navCase.getFromAction() != null) {
            navCase.setFromAction(newName);
        }
        configModel.endTransaction();

        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void removeModelNavigationCase(NavigationCase navCase) throws IOException {
        configModel.startTransaction();
        NavigationRule navRule = (NavigationRule) navCase.getParent();
        if (navRule != null && navRule.getNavigationCases().contains(navCase)) {
            //Only delete if it is still valid.
            navRule.removeNavigationCase(navCase);
            if (navRule.getNavigationCases().size() < 1) {
                configModel.removeChildComponent(navRule); //put this back once you remove hack
            }
        }
        configModel.endTransaction();
        configModel.sync();
    }

    public void serializeNodeLocations() {
        view.serializeNodeLocations(PageFlowView.getStorageFile(configDataObj.getPrimaryFile()));
    }

    public void openNavigationCase(NavigationCaseEdge navCaseEdge) {

        final NavigationCase navCase = getNavCase2NavCaseEdge(navCaseEdge);
        //FileObject fobj = NbEditorUtilities.getFileObject(navCase.getModel().getDocument());
        //DataObject dobj = DataObject.find(fobj);
        DataObject dobj = getConfigDataObject();        
        if (dobj != null) {
            final EditCookie ec2 = dobj.getCookie(EditCookie.class);
            if (ec2 != null) {

                final EditorCookie.Observable ec = dobj.getCookie(EditorCookie.Observable.class);
                if (ec != null) {
                    StatusDisplayer.getDefault().setStatusText("otvirani"); // NOI18N
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {

                            ec2.edit();
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes != null && panes.length > 0) {
                                openPane(panes[0], navCase);
                                //ec.open();
                            } else {
                                ec.addPropertyChangeListener(new PropertyChangeListener() {

                                    public void propertyChange(PropertyChangeEvent evt) {
                                        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                            final JEditorPane[] panes = ec.getOpenedPanes();
                                            if (panes != null && panes.length > 0) {
                                                openPane(panes[0], navCase);
                                            }
                                            ec.removePropertyChangeListener(this);
                                        }
                                    }
                                });
                                ec.open();
                            }
                        }
                    });
                }
            }
        }
    }

    private void openPane(JEditorPane pane, NavigationCase navCase) {
        final Cursor editCursor = pane.getCursor();
        pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
        pane.setCaretPosition(navCase.findPosition() + 2);
        pane.setCursor(editCursor);
        StatusDisplayer.getDefault().setStatusText(""); //NOI18N
    }
}
