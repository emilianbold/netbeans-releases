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
package com.sun.rave.web.ui.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.IndexItem;
import javax.help.IndexView;
import javax.help.InvalidHelpSetContextException;
import javax.help.Map;
import javax.help.Map.ID;
import javax.help.Merge;
import javax.help.NavigatorView;
import javax.help.SearchHit;
import javax.help.SearchTOCItem;
import javax.help.SearchView;
import javax.help.ServletHelpBroker;
import javax.help.SortMerge;
import javax.help.TOCItem;
import javax.help.TOCView;
import javax.help.TreeItem;
import javax.help.search.MergingSearchEngine;
import javax.help.search.SearchEvent;
import javax.help.search.SearchItem;
import javax.help.search.SearchListener;
import javax.help.search.SearchQuery;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import javax.faces.context.FacesContext;

/**
 * This is a set of utilities used for accessing JavaHelp content.
 *
 * @author  Sun Microsystems, Inc.
 */
public class HelpUtils implements SearchListener {
    public static final String URL_SEPARATOR = "/";
    public static final String TOC_VIEW_NAME = "TOC";
    public static final String INDEX_VIEW_NAME = "Index";
    public static final String SEARCH_VIEW_NAME = "Search";

    private ServletHelpBroker helpBroker;

    // TOC variables.
    private TOCView tocView;
    private Enumeration tocTreeEnum;
    private ArrayList tocTreeList;
    private DefaultMutableTreeNode tocTopNode;    

    // Index (tab) variables.
    private IndexView indexView;
    private Enumeration indexTreeEnum;
    private ArrayList indexTreeList;
    private DefaultMutableTreeNode indexTopNode;    

    // Search variables.
    private SearchView searchView;
    private MergingSearchEngine helpSearch;
    private SearchQuery searchQuery;
    private Vector searchNodes;
    private Enumeration searchEnum;
    private boolean searchFinished;

    // The application name (context name).
    private String appName;

    // Help path prefix
    private static String pathPrefix;
    
    // unsecure http port to use for help requests
    int httpPort = -1;

    // Locale object for the tags.
    private Locale currentLocale;

    /** Node ID of the root treenode. */
    public static final String BASE_ID = "root";

    /** Tips on searching path. */
    protected static final String REQUEST_SCHEME = "http";
    protected static final String HTML_DIR = "html";
    protected static final String HELP_DIR = "help";
    protected static final String DEFAULT_HELPSET_NAME = "app.hs";
    protected static final String TIPS_ON_SEARCHING_FILE =
	"tips_on_searching.html";

    /** Constructor. */
    public HelpUtils(HttpServletRequest request, String appName, int httpPort) {
	// Debug.initTrace();
	this.appName = appName;
        
        this.httpPort = httpPort;

	// Set up the currentLocale object.
	currentLocale = getLocale();

	// Initialize the helpBroker and create/validate the helpset.
	initHelp(request);
    }

    /** Constructor. */
    public HelpUtils(HttpServletRequest request, String appName,
	    String pathPrefix) {
	// Debug.initTrace();
	this.appName = appName;

	if ((pathPrefix != null)  && (pathPrefix.length() != 0)) {
	    if (pathPrefix.trim().length() != 0) {
	        this.pathPrefix = pathPrefix.trim();
	    }
	}

	// Set up the currentLocale object.
	currentLocale = getLocale();

	// Initialize the helpBroker and create/validate the helpset.
	initHelp(request);
    }

    private Locale getLocale() {
	FacesContext context = FacesContext.getCurrentInstance();
        return context.getViewRoot().getLocale();
    }

    /**
     * Get the path to the localized tips_on_searching help file.
     */
    public String getTipsOnSearchingPath(ServletContext context) {

	// first check if the file is in the app help directory, if 
	// not look for it in the resource context path.

	StringBuffer buf = new StringBuffer(128);
        
        buf.append(getHelpPath(appName))
            .append(URL_SEPARATOR)
            .append(HTML_DIR)
            .append(URL_SEPARATOR)
            .append(currentLocale.toString())
            .append(URL_SEPARATOR) 
            .append(HELP_DIR)
            .append(URL_SEPARATOR)
            .append(TIPS_ON_SEARCHING_FILE);
        
        return buf.toString();
        
        /*            
        // file is in the resource context path
        buf = new StringBuffer(CCSystem.getResourceContextPath() + 
            URL_SEPARATOR + HTML_DIR
            + URL_SEPARATOR + currentLocale.toString()
            + URL_SEPARATOR + HELP_DIR
            + URL_SEPARATOR + TIPS_ON_SEARCHING_FILE);
        return buf.toString();
        */
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Initialization methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Initialize the Help helpset.
     */
    private void initHelp(HttpServletRequest request) {        
        
	instantiateHelpBroker(request);
        
	// Grab the default HelpSet path: /<appName>/html/<locale>/help/app.hs
	String hsPath = getDefaultHelpSetPath();
	// Debug.trace3("hsPath: " + hsPath);

	// Validate the HelpSets and the help IDs (use null to get current ID).
	validateHelpSet(request, hsPath, false);
	validateID(null);

	initNavigatorViews();
    }

    /** 
     * Instantiate the ServletHelpBroker bean.
     */
    private void instantiateHelpBroker(HttpServletRequest request) {
	try {
	    helpBroker = (ServletHelpBroker) java.beans.Beans.instantiate(
		this.getClass().getClassLoader(), 
		"javax.help.ServletHelpBroker");
	} catch (ClassNotFoundException exc) {
	    // XXX: Is this serious??
	    System.out.println("Cannot instantiate ServletHelpBroker."
		+ exc.getMessage());
	} catch (Exception exc) {
	    // XXX: Is this serious??
	    System.out.println("Cannot create bean of class ServletHelpBroker."
		+ exc.getMessage());
	}
    }
    
    /**
     * Return a handle to the ServletHelpBroker.
     */
    public ServletHelpBroker getHelpBroker() {
	return helpBroker;
    }
    
    public String getLocalizedHelpPath() {
        StringBuffer buffer = new StringBuffer(1024);
        
        buffer.append(appName);
        
        if (!appName.endsWith("/"))  {
            // add the slash if it's not there already
            buffer.append(URL_SEPARATOR);
        }
        
        buffer.append(HTML_DIR)
	    .append(URL_SEPARATOR)
	    .append(currentLocale.toString())
	    .append(URL_SEPARATOR)
	    .append(HELP_DIR)
	    .append(URL_SEPARATOR);
        
        return buffer.toString();
    }

    /**
     * Return the path to the default helpset file. The path will be formatted
     * as follows:
     * <p>
     * <code>/<appName>/html/<locale>/help/app.hs</code>
     * </p>
     */
    public String getDefaultHelpSetPath() {        
        /*
	if (appName == null) {
	    appName = request.getContextPath();
	    if (appName == null) {
		// Debug("Unable to obtain app name from request.");
		appName = "";
	    } else {
		if (appName.startsWith(URL_SEPARATOR)) {
		    appName = appName.substring(1);
		}
	    }
	}
        */
        
	StringBuffer buffer = new StringBuffer(1024);
        
        buffer.append(getLocalizedHelpPath())
	    .append(DEFAULT_HELPSET_NAME);
        
        // System.out.println("default hs path = " + buffer.toString());
        
        return buffer.toString();
    }

    /**
     * Get help installation directory with reference to the app
     * base. If the application's pathPrefix is set to a given value
     * then this method will return a string of the form pathPrefix/html.
     * If the appName is null or empty or no path prefix has been set then 
     * "html" will be returned.
     */
    private static String getHelpPath(String appName) {

	if ((appName == null) || (appName.length() == 0)) {
	    return HTML_DIR;
	} else {
	    if (pathPrefix == null) {
	        return appName;
	    } else {
	        return (appName + URL_SEPARATOR + 
		    pathPrefix);
	    }
	}
    }

    /**
     * Initialize navigator views.
     */
    private void initNavigatorViews() {
	// Initialize TOC.
	//
	HelpSet hs = helpBroker.getHelpSet();
	Locale locale = hs.getLocale();

	tocView = (TOCView) hs.getNavigatorView(TOC_VIEW_NAME);
	if (tocView != null) {
	    // Grab the tree data from the navigator view.
	    tocTopNode = tocView.getDataAsTree();

	    // Sort the helpsets
	    String mergeType = tocView.getMergeType();
	    if (mergeType != null) {
		SortMerge.sortNode(tocTopNode, locale);
	    }

	    // Add all the sub-helpsets to the master merged helpset.
	    addSubHelpSets(hs);

	    // Set the tocTreeEnum and tocTreeList objects for future use.
	    tocTreeEnum = tocTopNode.preorderEnumeration();
	    setTOCTreeList();
	}

	// Initialize Index.
	//

	// Get the Index navigator view and index tree data.
	indexView = (IndexView) hs.getNavigatorView(INDEX_VIEW_NAME);
	if (indexView != null) {
	    indexTopNode = indexView.getDataAsTree();

	    // Set the indexTreeEnum and indexTreeList objects for future use.
	    indexTreeEnum = indexTopNode.preorderEnumeration();
	    setIndexTreeList();
	}

	// Initialize Search.
	//

	// Get the Search navigator view and index tree data.
	searchView = (SearchView) hs.getNavigatorView(SEARCH_VIEW_NAME);        
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Validation methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * This method validates the helpset. 
     *
     * @param request The request for this page.
     * @param hsName The helpset name.
     * @param merge Indicates whether the helpset hsName should be merged.
     */
    public void validateHelpSet(HttpServletRequest request, String hsName,
	    boolean merge) {
	HelpSet hs = helpBroker.getHelpSet();

	// The HelpSet exists.
	if (hs != null) {
	    // If the hsName is null, return since there is nothing to do.
	    // Otherwise, if merging is turned on, add the HelpSet.
	    if (hsName == null) {
		return;
	    }

	    HelpSet newHS = createHelpSet(request, hsName);
	    if (merge && !hs.contains(newHS)) {
		hs.add(newHS);
	    } else {
		helpBroker.setHelpSet(newHS);
	    }

	// The HelpSet does not exist.
	} else {
	    // If the hsName is null, forward to the invalid page. Otherwise,
	    // create a HelpSet from hsName and set HelpBroker's HelpSet value.
	    if (hsName == null) {
		// Debug.trace1("Invalid URL path: " + hsName);
		// XXX: Forward to invalid url page.
		return;
	    }
	    helpBroker.setHelpSet(createHelpSet(request, hsName));
	}
    }

    /**
     * Validate the given help id. If none is specified, set the current id to
     * the home id.
     *
     * @param helpID the current ID.
     */
    public void validateID(String helpID) {
	if (helpID != null) {
	    helpBroker.setCurrentID(helpID);
	} else if (helpBroker.getCurrentID() == null
		&& helpBroker.getCurrentURL() == null) {
	    try {
		helpBroker.setCurrentID(helpBroker.getHelpSet().getHomeID());
	    } catch (InvalidHelpSetContextException e) {
		// Ignore
	    }
	}
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // HelpSet methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Set the path to the "current" page.
     */
    public void setCurrentHelpPage(URL url) {
	helpBroker.setCurrentURL(url);
    }

    /**
     * Creates a helpset.
     *
     * @param request The request for this page
     * @param hsName the HelpSet name
     * @return the HelpSet created
     */
    private HelpSet createHelpSet(HttpServletRequest request, String hsName) {
	if (!hsName.startsWith(REQUEST_SCHEME)
		&& !hsName.startsWith(URL_SEPARATOR)) {
	    hsName = URL_SEPARATOR + hsName;
	}

	HelpSet hs = null;
	
        int port = httpPort;
        
        if (port == -1) {
            port = request.getServerPort();
        }

	try {
	    URL url = (hsName.startsWith(REQUEST_SCHEME))
		? new URL(hsName)
		: new URL(REQUEST_SCHEME, "127.0.0.1", port, hsName);
                    
	    hs = new HelpSet(null, url);
	} catch (MalformedURLException e) {
	    // ignore
	} catch (HelpSetException hse) {
	    // If the currentLocale is not "en", try to create the HelpSet using
	    // the "en" (default) locale.
	    if (!currentLocale.toString().equals(Locale.ENGLISH)) {
		currentLocale = Locale.ENGLISH;
		hs = createHelpSet(request,
		    getDefaultHelpSetPath());
	    // XXX: This is a serious error. The currentLocale is not "en", and a
	    // helpset still cannot be found. Throw a runtime exception for now.
	    } else {
		throw new RuntimeException(hse.getMessage());
	    }
	}
	return hs;
    }

    /** 
     * Adds sub-helpsets to the master merged helpset.
     *
     * @param hs The HelpSet to which subhelpsets will be added
     */
    private void addSubHelpSets(HelpSet hs) {
	for (Enumeration e = hs.getHelpSets(); e.hasMoreElements(); ) {
	    HelpSet ehs = (HelpSet) e.nextElement();
	    if (ehs == null) {
		continue;
	    }

	    // Merge views
	    NavigatorView[] views = ehs.getNavigatorViews();
	    for (int i = 0; i < views.length; i++) {
		if (views[i] instanceof TOCView) {
		    Merge mergeObject = Merge.DefaultMergeFactory.getMerge(
			tocView, views[i]);
		    if (mergeObject != null) {
			mergeObject.processMerge(tocTopNode);
		    }
		}
	    }
	    addSubHelpSets(ehs);
	}
    }    

    /**
     * Return the ID of the given node.
     */
    public String getID(TreeNode node) {
	if (node == tocTopNode) {
	    return BASE_ID;
	}

	TreeNode parent = node.getParent();
	if (parent == null) {
	    return "";
	}

	String id = getID(parent);
	return id.concat("_" + Integer.toString(parent.getIndex(node)));
    }

    /**
     * Return the content URL in String form for a given TreeItem, or an empty
     * String if no content exists.
     */
    public String getContentURL(TreeItem item) {
	URL url = null;
	ID id = item.getID();
	if (id != null) {
	    HelpSet hs = id.hs;
	    Map map = hs.getLocalMap();
	    try {
		url = map.getURLFromID(id);
	    } catch (MalformedURLException e) {
		// Ignore
	    }
	}
	return (url != null) ? url.toExternalForm() : "";
    }

    /**
     * Set the TOC tree list object using the tocTreeEnum object.
     */
    private void setTOCTreeList() {
	tocTreeList = new ArrayList();
	while (tocTreeEnum.hasMoreElements()) {
	    tocTreeList.add(tocTreeEnum.nextElement());
	}
    }

    /**
     * Return the TOC tree enumeration as an ArrayList object.
     */
    public ArrayList getTOCTreeList() {
	if (tocTreeList == null) {
	    setTOCTreeList();
	}
	return tocTreeList;
    }

    /**
     * Set the Index tree list object using the indexTreeEnum object.
     */
    private void setIndexTreeList() {
	indexTreeList = new ArrayList();
	while (indexTreeEnum.hasMoreElements()) {
	    indexTreeList.add(indexTreeEnum.nextElement());
	}
    }

    /**
     * Return the Index tree enumeration as an ArrayList object.
     */
    public ArrayList getIndexTreeList() {
	if (indexTreeList == null) {
	    setIndexTreeList();
	}
	return indexTreeList;
    }

    /**
     * Do a search on the query passed in.
     */
    public synchronized Enumeration doSearch(String query) {
	if (query == null) {
	    return null;
	}

	if (helpSearch == null) {
	    if (searchView == null) {
		searchView = (SearchView) helpBroker.getHelpSet()
		    .getNavigatorView(SEARCH_VIEW_NAME);
	    }

	    if (searchView == null) {                
		return null;
	    }

	    helpSearch = new MergingSearchEngine(searchView);
	    searchQuery = helpSearch.createQuery();
	    searchQuery.addSearchListener(this);
	}

	if (searchQuery.isActive()) {
	    searchQuery.stop();
	}
        
	searchFinished = false;
	searchQuery.start(query, currentLocale);

	// Wait for search to finish.
	if (!searchFinished) {
	    try {
		wait();
	    } catch (InterruptedException e) {
		// ignore
	    }
	}

	// searchEnum is set in searchFinished method.
	return searchEnum;
    }

    /**
     * Tells the listener that the search has started.
     */
    public synchronized void searchStarted(SearchEvent e) {
	searchNodes = new Vector();
	searchFinished = false;
    }

    /**
     * Tells the listener that the search has finished.
     */
    public synchronized void searchFinished(SearchEvent e) {
	searchFinished = true;
	searchEnum = searchNodes.elements();
	notifyAll();
    }

    /**
     * Tells the listener that matching SearchItems have been found.
     */
    public synchronized void itemsFound(SearchEvent e) {
	SearchTOCItem tocitem;
	Enumeration itemEnum = e.getSearchItems();

	// Iterate through each search item in the searchEvent
	while (itemEnum.hasMoreElements()) {
	    SearchItem item = (SearchItem) itemEnum.nextElement();
	    URL url;
	    try {
		url = new URL(item.getBase(), item.getFilename());
	    } catch (MalformedURLException me) {
		/* Debug.trace3("Could not create URL from: "
		    + item.getBase() + "|" + item.getFilename()); */
		continue;
	    }
	    boolean foundNode = false;

	    // See if this search item matches that of one we currently have
	    // if so just do an update
	    Enumeration nodesEnum = searchNodes.elements();
	    while (nodesEnum.hasMoreElements()) {
		tocitem = (SearchTOCItem) nodesEnum.nextElement();
		URL testURL = tocitem.getURL();
		if (testURL != null && url != null && url.sameFile(testURL)) {
		    tocitem.addSearchHit(new SearchHit(item.getConfidence(),
			item.getBegin(), item.getEnd()));
		    foundNode = true;
		    break;
		}
	    }

	    // No match. 
	    // OK then add a new one.
	    if (!foundNode) {
		tocitem = new SearchTOCItem(item);
		searchNodes.addElement(tocitem);
	    }
	}
    }

    /**
     * For debug - print the attributes of each node in the toc and index trees.
     */
    public void printDebug() {
	// Print TOC tree.
	ArrayList tocTreeList = getTOCTreeList();
	if (tocTreeList == null) {
	    // Debug.trace1("tocTreeList null.");
	    return;
	}

	StringBuffer buf =
	    new StringBuffer("tocTreeList dump:\n");
	DefaultMutableTreeNode node = null;
	int nTreeNodes = tocTreeList.size();
	for (int i = 0; i < nTreeNodes; i++) {
	    node = (DefaultMutableTreeNode) tocTreeList.get(i);
	    buf.append(tocTreeToString(node)).append("\n");
	}

	// Print Index tree.
	ArrayList indexTreeList = getIndexTreeList();
	if (indexTreeList == null) {
	    // Debug.trace1("indexTreeList null.");
	    return;
	}

	buf = new StringBuffer("indexTreeList dump:\n");
	nTreeNodes = indexTreeList.size();
	for (int i = 0; i < nTreeNodes; i++) {
	    node = (DefaultMutableTreeNode) indexTreeList.get(i);
	    buf.append(indexTreeToString(node)).append("\n");
	}
    }

    /**
     * Return a string containing the contents of the given TOC tree node.
     */
    public String tocTreeToString(DefaultMutableTreeNode node) {
	// Add TOC tree to buffer.
	if (node == null) {
	    return ("\n\tTOC tree node is null.");
	}

	TOCItem item = (TOCItem) node.getUserObject();
	if (item == null) {
	    return ("\n\tTOCItem is null.");
	}

	DefaultMutableTreeNode parent =
	    (DefaultMutableTreeNode) node.getParent();
	StringBuffer buf = new StringBuffer();
	buf.append("\n\tname:          " + item.getName());
	buf.append("\n\thelpID:        " + ((item.getID() != null)
	    ? item.getID().id : ""));
	buf.append("\n\tparentID:        " + ((parent != null)
	    ? Integer.toHexString(parent.hashCode()) : ""));
	buf.append("\n\tparentID 2:      " + getID(parent));
	buf.append("\n\tnode:          "
	    + Integer.toHexString(node.hashCode()));
	buf.append("\n\tnodeID:        " + getID(node));
	buf.append("\n\tcontentURL:    " + getContentURL(item));
	buf.append("\n\texpansionType: " +
	    Integer.toString(item.getExpansionType()));

	return buf.toString();
    }

    /**
     * Return a string containing the contents of the given Index tree node.
     */
    public String indexTreeToString(DefaultMutableTreeNode node) {
	// Add Index tree to buffer.
	if (node == null) {
	    return ("\n\tTree node is null.");
	}

	IndexItem item = (IndexItem) node.getUserObject();
	if (item == null) {
	    return ("\n\tIndexItem is null.");
	}

	DefaultMutableTreeNode parent =
	    (DefaultMutableTreeNode) node.getParent();
	StringBuffer buf = new StringBuffer();
	buf.append("\n\tname:          " + item.getName());
	buf.append("\n\thelpID:        " + ((item.getID() != null)
	    ? item.getID().id : ""));
	buf.append("\n\tparentID:        " + ((parent != null)
	    ? Integer.toHexString(parent.hashCode()) : ""));
	buf.append("\n\tparentID 2:      " + getID(parent));
	buf.append("\n\tnode:          "
	    + Integer.toHexString(node.hashCode()));
	buf.append("\n\tnodeID:        " + getID(node));
	buf.append("\n\texpansionType: " +
	    Integer.toString(item.getExpansionType()));

	return buf.toString();
    }
}
