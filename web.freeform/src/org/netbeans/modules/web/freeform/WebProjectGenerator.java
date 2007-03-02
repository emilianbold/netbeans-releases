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
 */

package org.netbeans.modules.web.freeform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Reads/writes project.xml.
 *
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class WebProjectGenerator {

//    /** Keep root elements in the order specified by project's XML schema. */
    private static final String[] rootElementsOrder = new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}; // NOI18N
    private static final String[] viewElementsOrder = new String[]{"items", "context-menu"}; // NOI18N

//    // this order is not required by schema, but follow it to minimize randomness a bit
    private static final String[] folderElementsOrder = new String[]{"source-folder", "build-folder"}; // NOI18N
    private static final String[] viewItemElementsOrder = new String[]{"source-folder", "source-file"}; // NOI18N
    
    private WebProjectGenerator() {}

    /**
     * @param soruces list of pairs[relative path, display name]
     */
    public static void putWebSourceFolder(AntProjectHelper helper, List/*<String>*/ sources) {
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element foldersEl = Util.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(Util.NAMESPACE, "folders"); // NOI18N
            Util.appendChildElement(data, foldersEl, rootElementsOrder);
        } else {
            List l = Util.findSubElements(foldersEl);
            for (int i = 0; i < l.size(); i++) {
                Element e = (Element) l.get(i);
                Element te = Util.findElement(e, "type", Util.NAMESPACE);
                if (te != null && Util.findText(te).equals("doc_root")) {
                    foldersEl.removeChild(e);
                    break;
                }
            }        
        }
        Element viewEl = Util.findElement(data, "view", Util.NAMESPACE); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(Util.NAMESPACE, "view"); // NOI18N
            Util.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element itemsEl = Util.findElement(viewEl, "items", Util.NAMESPACE); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(Util.NAMESPACE, "items"); // NOI18N
            Util.appendChildElement(viewEl, itemsEl, viewElementsOrder);
        } else {
            List l = Util.findSubElements(itemsEl);
            for (int i = 0; i < l.size(); i++) {
                Element e = (Element) l.get(i);
                if (e.hasAttribute("style")) {
                    if (e.getAttribute("style").equals("tree")) {
                        itemsEl.removeChild(e);
                        break;
                    }
                }
            }
        }
        
        Iterator it1 = sources.iterator();
        while (it1.hasNext()) {
            String path = (String)it1.next();
            assert it1.hasNext();
            String dispname = (String)it1.next();
            Element sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            Element el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
            el.appendChild(doc.createTextNode(dispname));
            sourceFolderEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "type"); // NOI18N
            el.appendChild(doc.createTextNode(WebProjectConstants.TYPE_DOC_ROOT));
            sourceFolderEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            el.appendChild(doc.createTextNode(path));
            sourceFolderEl.appendChild(el);
            Util.appendChildElement(foldersEl, sourceFolderEl, folderElementsOrder);
            
            sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            sourceFolderEl.setAttribute("style", "tree"); // NOI18N
            el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
            el.appendChild(doc.createTextNode(NbBundle.getMessage(WebProjectGenerator.class, "LBL_WebPages")));
            sourceFolderEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            el.appendChild(doc.createTextNode(path)); // NOI18N
            sourceFolderEl.appendChild(el);
            Node firstNode = itemsEl.getFirstChild();
            if (firstNode != null) {
                itemsEl.insertBefore(sourceFolderEl, firstNode);
            } else {
                Util.appendChildElement(itemsEl, sourceFolderEl, viewItemElementsOrder);
            }
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    
    /**
     * Read web modules from the project.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @return list of WebModule instances
     */
    public static List/*<WebModule>*/ getWebmodules (
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("web-data", WebProjectNature.NS_WEB, true); // NOI18N
        List/*<Element>*/ wms = Util.findSubElements(data);
        Iterator it = wms.iterator();
        while (it.hasNext()) {
            Element wmEl = (Element)it.next();
            WebModule wm = new WebModule();
            Iterator it2 = Util.findSubElements(wmEl).iterator();
            while (it2.hasNext()) {
                Element el = (Element)it2.next();
                if (el.getLocalName().equals("doc-root")) { // NOI18N
                    wm.docRoot = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("classpath")) { // NOI18N
                    wm.classpath = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("context-path")) { // NOI18N
                    wm.contextPath = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("j2ee-spec-level")) { // NOI18N
                    wm.j2eeSpecLevel = Util.findText(el);
                }
            }
            list.add(wm);
        }
        return list;
    }

    /**
     * Update web modules of the project. Project is left modified
     * and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @param webModules list of WebModule instances
     */
    public static void putWebModules(AntProjectHelper helper, 
            AuxiliaryConfiguration aux, List/*<WebModule>*/ webModules) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = aux.getConfigurationFragment("web-data", WebProjectNature.NS_WEB, true); // NOI18N
        if (data == null) {
            data = Util.getPrimaryConfigurationData(helper).getOwnerDocument().
                createElementNS(WebProjectNature.NS_WEB, "web-data"); // NOI18N
        }
        Document doc = data.getOwnerDocument();
        List wms = Util.findSubElements(data);
        Iterator it = wms.iterator();
        while (it.hasNext()) {
            Element wmEl = (Element)it.next();
            data.removeChild(wmEl);
        }
        Iterator it2 = webModules.iterator();
        while (it2.hasNext()) {
            Element wmEl = doc.createElementNS(WebProjectNature.NS_WEB, "web-module"); // NOI18N
            data.appendChild(wmEl);
            WebModule wm = (WebModule)it2.next();
            Element el;
            if (wm.docRoot != null) {
                el = doc.createElementNS(WebProjectNature.NS_WEB, "doc-root"); // NOI18N
                el.appendChild(doc.createTextNode(wm.docRoot));
                wmEl.appendChild(el);
            }
            if (wm.classpath != null) {
                el = doc.createElementNS(WebProjectNature.NS_WEB, "classpath"); // NOI18N
                el.appendChild(doc.createTextNode(wm.classpath));
                wmEl.appendChild(el);
            }
            if (wm.contextPath != null) {
                el = doc.createElementNS(WebProjectNature.NS_WEB, "context-path"); // NOI18N
                el.appendChild(doc.createTextNode(wm.contextPath));
                wmEl.appendChild(el);
            }
            if (wm.j2eeSpecLevel != null) {
                el = doc.createElementNS(WebProjectNature.NS_WEB, "j2ee-spec-level"); // NOI18N
                el.appendChild(doc.createTextNode(wm.j2eeSpecLevel));
                wmEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
    }
    
    /**
     * Structure describing web module.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class WebModule {
        public String docRoot;
        public String classpath;
        public String contextPath;
        public String j2eeSpecLevel;
    }

}
