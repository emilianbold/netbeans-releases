/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

import java.util.ArrayList; 
import java.util.Iterator;
import java.util.StringTokenizer; 

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;

// PENDING - it would be better to have a FilterData which extends
// ServletData, and keep the filter specific code in that class. 

/** 
 * Deployment data validator object for servlets. 
 * @author ana.von.klopp@sun.com
 */
class ServletData extends DeployData { 

    private String errorMessage = null; 
    private String name = null; 
    // These are URL mappings - they're used by both Servlets and Filters
    private String[] urlMappings = null; 
    // These are mappings to servlet names - used by Filters only
    private ArrayList filterMappings = null; 
    private String[][] initparams = null; 
    private boolean paramOK = true; 
    private FileType fileType = null; 
    private static final boolean debug = false;
    private boolean addToDD=true;

    ServletData(FileType fileType) {
	this.fileType = fileType; 
    } 

    String getName() { 
	if(name == null) return ""; 
	return name; 
    } 

    void setName(String name) { 
	if(name != this.name) { 
	    if(fileType == FileType.FILTER)
		updateFilterMappings(getName(), name); 
	    this.name = name; 
	} 
    }

    String[] getServletNames() { 
	if(webApp == null) return new String[0]; 
	Servlet[] ss = webApp.getServlet();
	String[] names = new String[ss.length]; 
	for (int i=0; i<ss.length; i++) {
	    try { 
		names[i] = ss[i].getServletName(); 
	    }
	    catch(Exception e) { 
		names[i] = ""; 
	    } 
	}
	return names; 
    } 

    java.util.List getUrlPatterns () {
	if(webApp == null) { 
	    if(debug) log("\tNo web app, return null"); //NOI18N
	    return new ArrayList(); 
	}
        ServletMapping[] maps = webApp.getServletMapping();
        java.util.List l = new ArrayList();
        for (int i=0;i<maps.length;i++) {
            l.add(maps[i].getUrlPattern());
        }
        return l;
    }
    
    ArrayList getFilterMappings() { 

	if(debug) log("::getFilterMappings()"); //NOI18N
	if(webApp == null) { 
	    if(debug) log("\tNo web app, return null"); //NOI18N
	    return new ArrayList(); 
	} 
	if(filterMappings != null) { 
	    if(debug) log("\tFilter mappings already exist"); //NOI18N
	    return filterMappings; 
	}

	if(debug) log("\tCreating the filter mapping list"); //NOI18N
	FilterMapping[] fm = webApp.getFilterMapping();
	if(debug) { 
	    log("\tOrder of mappings according to DD APIs"); //NOI18N
	    for(int i=0; i<fm.length; ++i) 
		log("\tServlet name: " + fm[i].getFilterName()); //NOI18N
	}

	filterMappings = new ArrayList(); 
	filterMappings.add(new FilterMappingData(getName())); 

	String string; 
	String[] d = null; 
	FilterMappingData fmd; 
	FilterMappingData.Dispatcher[] dispatchList; 

	for (int i=0; i<fm.length; i++) {
	    fmd = new FilterMappingData(); 
	    fmd.setName(fm[i].getFilterName()); 
	    
	    string = fm[i].getUrlPattern();
	    if(string == null || string.length() == 0) { 
		fmd.setType(FilterMappingData.Type.SERVLET); 
		fmd.setPattern(fm[i].getServletName()); 
	    } 
	    else {
		fmd.setType(FilterMappingData.Type.URL); 
		fmd.setPattern(string); 
	    } 
	    
	    try { 
		if(fm[i].sizeDispatcher() == 0) { 
		    filterMappings.add(fmd); 
		    continue; 
		}
	    }
	    catch(Exception ex) { 
		// Not supported
		filterMappings.add(fmd); 
		continue; 
	    } 

	    try { 
		d = fm[i].getDispatcher(); 
	    }
	    catch(Exception ex) { 
		if(debug) { 
		    log(ex.toString()); 
		    ex.printStackTrace(); 
		}
		// PENDING ... 
		// Servlet 2.3
	    } 
	    if(d == null) 
		
		dispatchList = new FilterMappingData.Dispatcher[0];
	    else { 
		dispatchList = new FilterMappingData.Dispatcher[d.length];
		for(int j=0; j<d.length; ++j) { 
		    dispatchList[j] = FilterMappingData.Dispatcher.findDispatcher(d[j]); 
		    if(debug) log("\tDispatch: " + dispatchList[j]);//NOI18N
		}
	    } 
	    fmd.setDispatcher(dispatchList); 
	    filterMappings.add(fmd); 
	} 
	return filterMappings; 
    } 

    void setFilterMappings(ArrayList fmds) { 
	if(debug) log("::setFilterMappings()"); 
	filterMappings = fmds; 
    }
	
    void updateFilterMappings(String oldName, String newName) { 
	if(debug) 
	    log("::updateFilterMappings("+oldName+", " + newName + "),"); //NOI18N
	Iterator i = getFilterMappings().iterator(); 
	// No web app
	if(i == null) return; 
	FilterMappingData fmd; 
	while(i.hasNext()) { 
	    fmd = (FilterMappingData)i.next();
	    if(fmd.getName().equals(oldName)) fmd.setName(newName); 
	}
    } 

    boolean isNameUnique() { 
	if(webApp == null) return true; 
	Servlet[] ss = webApp.getServlet();
	for (int i=0; i<ss.length; i++) {
	    if(name.equals(ss[i].getServletName())) { 
		return false; 
	    }
	}

	Filter[] ff = webApp.getFilter();
	for (int i=0; i<ff.length; i++) {
	    if(name.equals(ff[i].getFilterName())) { 
		return false; 
	    }
	}
	return true; 
    } 

    String[] getUrlMappings() { 
	if(urlMappings == null) return new String[0]; 
	return urlMappings; 
    } 

    String getUrlMappingsAsString() { 

	if(urlMappings == null || urlMappings.length == 0 ) return ""; //NOI18N
	StringBuffer buf = new StringBuffer(); 
	int index = 0; 
	while(index<urlMappings.length-1) { 
	    buf.append(urlMappings[index]); 
	    buf.append(", "); //NOI18N
	    index++; 
	} 

	buf.append(urlMappings[index]); 
	return buf.toString(); 
    } 

    void parseUrlMappingString(String raw) { 

	urlMappings = null; 
	StringTokenizer st = new StringTokenizer(raw, ",");
	ArrayList list = new ArrayList(); 
	String mapping; 
	String[] names = getServletNames(); 

	while(st.hasMoreTokens()) { 
	    mapping = st.nextToken().trim(); 
	    if(mapping.length() == 0) 
		continue; 
	    list.add(mapping); 
	} 

	urlMappings = new String[list.size()]; 
	Iterator it = list.iterator(); 
	int index = 0; 
	while(it.hasNext()) { 
	    urlMappings[index] = (String)it.next(); 
	    ++index; 
	} 
    } 

    String[][] getInitParams() { 
	if(initparams == null) return new String[0][2]; 
	return initparams; 
    } 

    void setInitParams(String[][] initparams, boolean paramOK) { 
	this.initparams = initparams; 
	this.paramOK = paramOK; 
    } 

    int getNumInitParams() { 
	if(initparams == null) return 0; 
	return initparams.length; 
    } 

    boolean isParamOK() { 
	return paramOK; 
    } 

    void setParamOK(boolean paramOK) { 
	this.paramOK = paramOK; 
    } 


    boolean isValid() {
	if(debug) log("::isValid()"); //NOI18N
	errorMessage = new String(); 
	if(webApp == null) return true;
        if (!isAddToDD()) return true;

	if(getName().length() == 0) {
	    errorMessage = NbBundle.getMessage(ServletData.class, 
					       "MSG_no_name"); 
	    return false; 
	} 

	if(!isNameUnique()) { 
	    errorMessage = NbBundle.getMessage(ServletData.class, 
					       "MSG_name_unique"); 
	    return false; 
	} 

	if(debug) log("\tname is fine"); //NOI18N

	if(fileType == FileType.SERVLET) { 
	    if(!checkMappingsForServlet()) return false; 
            if(!checkServletDuplicitMappings()) return false; 
	} 
	else if(fileType == FileType.FILTER) { 
	    if(!checkMappingsForFilter()) return false; 
	}
	
	if(!isParamOK()) { 
	    errorMessage = NbBundle.getMessage(ServletData.class, 
					       "MSG_invalid_param"); 
	    return false; 
	}

	if(debug) log("\tparams are fine"); //NOI18N
	return true; 
    }


    boolean checkMappingsForServlet() { 

	errorMessage = new String();
        String[] mappings = getUrlMappings();
	if(mappings == null || mappings.length == 0) { 

	    if(debug) log("\tNo URL mappings"); //NOI18N
	    errorMessage = NbBundle.getMessage(ServletData.class, 
					       "MSG_no_mapping"); 
	    return false; 
	}
        for (int i=0;i<mappings.length;i++) {
            String errMessage = checkServletMappig(mappings[i]);
            if (errMessage!=null) {
                errorMessage = errMessage; 
                return false; 
            }
        }
	if(debug) log("\tmappings are fine"); //NOI18N
	return true; 
    }
    
    boolean checkServletDuplicitMappings() { 
	errorMessage = new String(); 
        String[] newMappings = getUrlMappings();
        java.util.List urlPatterns = getUrlPatterns();
        for (int i=0;i<newMappings.length;i++) {
            Iterator it = urlPatterns.iterator();
            while(it.hasNext()) {
                String urlPattern = (String)it.next();
                if (newMappings[i].equals(urlPattern)) {
                    if(debug) log("\tDuplicit URL mappings"); //NOI18N
                    errorMessage = NbBundle.getMessage(ServletData.class, 
                                                       "MSG_url_pattern_unique"); 
                    return false;
                }
            }
            // new Url Patterns need to be compare to each other 
            urlPatterns.add(newMappings[i]);
        }
	if(debug) log("\tmappings- duplicity - is fine"); //NOI18N
	return true; 
    }

    boolean checkMappingsForFilter() { 
	errorMessage = new String(); 
	if(filterMappings == null || filterMappings.size() == 0) { 
 	    if(debug) log("\tNo mappings"); //NOI18N
	    errorMessage =  NbBundle.getMessage(ServletData.class, 
						"MSG_no_mapping"); 
	    return false; 
	}
	Iterator i = getFilterMappings().iterator(); 
	boolean found = false; 
	FilterMappingData fmd; 
	while(i.hasNext()) { 
	    fmd = (FilterMappingData)(i.next()); 
	    if(fmd.getName().equals(getName())) { 
		found = true; 
		break;
	    }
	}
	if(!found) {
	    errorMessage = NbBundle.getMessage(ServletData.class, 
					       "MSG_no_mapping"); 
	    return false; 
	}
	return true; 
    } 

    void createDDEntries() { 
	if(debug) log("::createDDEntries()");
	if(webApp == null) return; 
	if(debug) log("\t...adding"); 

	if(fileType == FileType.SERVLET) { 
	    boolean added = addServlet(); 
	    if(added) { 
		addUrlMappings(); 
		if(debug) log("\t...writing changes"); 
		try {
		    writeChanges();
		} catch (java.io.IOException ex) {
                    if (debug) ex.printStackTrace();
		}
	    }
	} 
	else if(fileType == FileType.FILTER) { 
	    boolean added = addFilter(); 
	    if(added) { 
		addFilterMappings(); 
		if(debug) log("\t...writing changes"); 
		try {
		    writeChanges();
                } catch (java.io.IOException ex) {
                    if (debug) ex.printStackTrace();
		}
	    }
	} 
    }


    private boolean addServlet() { 

	if(debug) log("::addServlet()"); //NOI18N
	if(webApp == null) return false; 
	Servlet s; 
	try { 
	    s = (Servlet)webApp.createBean("Servlet"); //NOI18N
	    if(debug) log("\tCreated servlet"); //NOI18N
	} 
	
	catch(ClassNotFoundException cnfe) {
	    if(debug) cnfe.printStackTrace(); 
	    return false; 
	} 

	s.setServletName(name); 
	s.setServletClass(className); 

	int numInitParams = getInitParams().length; 

	if(debug) 
	    log("\tnum params " + String.valueOf(numInitParams));//NOI18N


	for(int i=0; i<numInitParams; ++i) { 
	    InitParam param; 
	    try { 
		param = (InitParam)s.createBean("InitParam"); //NOI18N
		if(debug) log("\tCreated initparam"); //NOI18N
	    } 

	    catch(ClassNotFoundException cnfe) { 
		if(debug) cnfe.printStackTrace(); 
		continue; 
	    } 

	    param.setParamName(initparams[i][0]); 
	    param.setParamValue(initparams[i][1]); 
	    s.addInitParam(param); 
	} 

	if(debug) 
	    log("\tnum params " + String.valueOf(s.sizeInitParam())); //NOI18N

	webApp.addServlet(s);
	return true; 
    }


    private boolean addFilter() { 

	if(debug) log("::addFilter()"); //NOI18N
	if(webApp == null) return false; 
	Filter f; 
	try { 
	    f = (Filter)webApp.createBean("Filter"); //NOI18N
	    if(debug) log("\tCreated filter"); //NOI18N
	} 

	catch(ClassNotFoundException cnfe) {
	    if(debug) cnfe.printStackTrace(); 
	    return false; 
	} 

	f.setFilterName(name); 
	f.setFilterClass(className); 

	int numInitParams = getInitParams().length; 

	if(debug) 
	    log("\tnum params " + String.valueOf(numInitParams));//NOI18N


	for(int i=0; i<numInitParams; ++i) { 
	    InitParam param; 
	    try { 
		param = (InitParam)f.createBean("InitParam"); //NOI18N
		if(debug) log("\tCreated initparam"); //NOI18N
	    } 

	    catch(ClassNotFoundException cnfe) { 
		if(debug) cnfe.printStackTrace(); 
		continue; 
	    } 

	    param.setParamName(initparams[i][0]); 
	    param.setParamValue(initparams[i][1]); 
	    f.addInitParam(param); 
	} 

	if(debug) 
	    log("\tnum params " + String.valueOf(f.sizeInitParam())); //NOI18N

	webApp.addFilter(f);
	return true; 
    }


    private void addUrlMappings() { 

	if(webApp == null) return; 
	int numMappings = getUrlMappings().length; 
	for(int i=0; i<numMappings; ++i) { 
	    ServletMapping m; 
	    try { 
		m = (ServletMapping)webApp.createBean("ServletMapping"); //NOI18N
	    } 
	    catch(ClassNotFoundException cnfe) { 
		return; 
	    } 
	    m.setServletName(name); 
	    m.setUrlPattern(urlMappings[i]);
	    webApp.addServletMapping(m);
	}
    }

    private void addFilterMappings() { 

	if(debug) log("::addFilterMappings()"); 
	if(webApp == null) return; 

	// filterMappings cannot be null, or of size zero
	int numFilterMappings = filterMappings.size(); 
	Iterator iterator = filterMappings.iterator();

	FilterMapping[] fm = new FilterMapping[numFilterMappings]; 

	FilterMappingData fmd; 
	for(int i=0; i<numFilterMappings; ++i) { 

	    fmd = (FilterMappingData)(iterator.next()); 

	    try { 
		fm[i] = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
		if(debug) log("\tCreated filter mapping"); //NOI18N
	    } 
	    catch(ClassNotFoundException cnfe) { 
		return; 
	    } 

	    fm[i].setFilterName(fmd.getName()); 
	    if(debug) log("\tFilter name: " + fmd.getName()); //NOI18N
	    if(fmd.getType() == FilterMappingData.Type.URL) { 
		fm[i].setUrlPattern(fmd.getPattern()); 
		if(debug) log("URL pattern " + fmd.getPattern()); //NOI18N
	    } 
	    else {
		fm[i].setServletName(fmd.getPattern()); 
		if(debug) log("Servlet pattern " + fmd.getPattern()); //NOI18N
	    }

	    int length = fmd.getDispatcher().length; 
	    if(length == 0) { 
		if(debug) log("\tNo dispatcher, continue"); //NOI18N
		continue; 
	    }

	    String[] s = new String[length]; 
	    FilterMappingData.Dispatcher[] d = fmd.getDispatcher(); 
	    for(int j=0; j<length; ++j) { 
		if(debug) log("\tDispatcher: " + d[j].toString()); //NOI18N
		s[j] = d[j].toString(); 
	    }
	    try { 
		fm[i].setDispatcher(s); 
	    }
	    catch(Exception e) {
		if(debug) log("\tFailed to set dispatcher"); //NOI18N
		// do nothing, wrong version
	    }
	    if(debug) log(fm[i].toString()); 
	}
	webApp.setFilterMapping(fm);
    }
    
    
    void setAddToDD(boolean addToDD){
        this.addToDD=addToDD;
    }
    
    boolean isAddToDD() {
        return addToDD;
    }

    String getErrorMessage() {
	return errorMessage; 
    } 

    void log(String s) { 
	System.out.println("ServletData" + s); 
    }
    
    private String checkServletMappig(String uri) {
        if (!uri.matches("[\\*/].*")) { //NOI18N
            return NbBundle.getMessage(ServletData.class,"MSG_WrongUriStart");
        } else if (uri.length()>1  && uri.endsWith("/")) {
            return NbBundle.getMessage(ServletData.class,"MSG_WrongUriEnd");
        } else if (uri.matches(".*\\*.*\\*.*")) { //NOI18N
            return NbBundle.getMessage(ServletData.class,"MSG_TwoAsterisks");
        }
        return null;
    }
}

