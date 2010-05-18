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
package org.netbeans.modules.visualweb.api.insync;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import java.net.URL;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * This abstract class exposes services the InSync Source modeller can perform.
 * The InSync implementation will register an implementation of this interface
 * in the system file system, so using org.openide.util.Lookup on
 * InSyncService.class you'll get an instance you can call these
 * methods on.
 * </p>
 *
 * @author Eric Arseneau
 */
public abstract class InSyncService {

//    /** Insync extention of MutationEventImpl type (see insyc/MarkupUnit and xerces/MuationEventImpl). */
//    public static final String DOM_DOCUMENT_REPLACED = "DOMDocumentReplaced"; // NOI18N

    /** Obtain a default provider of the InSyncService */
    public static InSyncService getProvider() {
        return (InSyncService)Lookup.getDefault().lookup(InSyncService.class);
    }

    public InSyncService () {
    }

    public abstract String getBeanNameForJsp(FileObject fileObject);

    /**
     * This is a hack until we get things done correctly with integration of Retouche.
     * Should only be called from JsfJspDataObject to notify us that toDataObject was created by copying fromDataObject.
     * Go through and rename the EL expression references to the original page name to the new page name.
     *
     * This is NOT the way to fix the problem of copy, however there are too many other issues that get in the way
     * that prevent us from creating a simple fix that will yield the same results.  Since ALL of this code will change
     * when we properly integrate insync into platform, this is throw away code.
     *
     *  TODO: Remove
     *
     * @param fromDataObject
     * @param toDataObject
     */
    public abstract void copied(JsfJspDataObjectMarker fromDataObject, JsfJspDataObjectMarker toDataObject);

    /**
     * Execute the runnable outside of a refactoring Session, this allows most of the refactoring processing
     * to have occured by the time this runnable will get run.
     *
     * @param runnable
     */
    public abstract void doOutsideOfRefactoringSession(Runnable runnable);

    // XXX Hacks, to the top component events. Get rid of them, when
    // there will be able to parse the files on demand. In any case
    // the SourceMonitor should be in insync.
    /** Called when jsp data object TopComponent was activated. */
    public abstract void jspDataObjectTopComponentActivated(DataObject dobj);
    /** Called when jsp data object TopComponent was hidden. */
    public abstract void jspDataObjectTopComponentHidden(DataObject dobj);
    /** Called when jsp data object TopComponent was shown. */
    public abstract void jspDataObjectTopComponentShown(DataObject dobj);


// <missing_designtime_api> These methods point out missing design time api,
// XXX Be aware that not exactly in this form the api is needed, the methods
// can by factored out to diff api, important is to satisfy the funcitonality need!
// For example there might be provided bean for document, and then the rest for the bean.

    // <markup_separation> XXX Suspicious API which needs to be revisited,
    // it comes from the original impl of RaveDocument and etc. markup packages.
    // TODO Get rid of it, these methods point out the architecural flaws.
    public abstract void appendParsedString(Document doc, Node node, String xhtml, MarkupDesignBean bean);
    public abstract FileObject getFileObject(Document doc);
    public abstract int computeLine(Document document, Element element);
    public abstract URL getDocumentUrl(Document doc);
    // </markup_separation>

    public abstract void setUrl(Document doc, URL url);
    public abstract URL getUrl(Document doc);
    
    // JSF Rendering
//    public abstract DocumentFragment renderHtml(FileObject markupFile, MarkupDesignBean bean);
//    public abstract Exception getRenderFailure(FileObject markupFile);
//    public abstract DesignBean getRenderFailureComponent(FileObject markupFile);
    
// <separation of models>
//    public abstract Document getJspDomForMarkupFile(FileObject markupFile);
//    public abstract Document getHtmlDomForMarkupFile(FileObject markupFile);
//    public abstract DocumentFragment getHtmlDomFragmentForMarkupFile(FileObject markupFile);
    public abstract Element getHtmlBodyForMarkupFile(FileObject markupFile);
    // XXX Refresh antipatern.
//    public abstract void clearHtmlForMarkupFile(FileObject markupFile);
    
//    // XXX FIXME It is used only in dnd handling (outside of insync) which shouldn't be there.
//    public abstract boolean isBraveheartPage(Document dom);    
//    public abstract boolean isWoodstockPage(Document dom);
    
// </separation of models>
    public abstract DocumentFragment getHtmlDomFragmentForDocument(Document document);
    
// </missing_designtime_api>

// <error_handling>
    // XXX It is not clear whether this should be here or at other more suitable place,
    // anyway, it is good to have these methods together represented by one interface.
    public abstract RaveErrorHandler getRaveErrorHandler();
    
    public interface RaveErrorHandler {
        /** Clears document related errors. 
         * @param delayed When set, don't actually clear the errors right now;
         * it clears the errors next time another error is added. */
        public void clearErrors(boolean delayed);
        /** Cause the panel/window within which errors are displayed to come to the front if possible. */
        public void selectErrors();
        public void displayError(String message);
        public void displayErrorForLocation(String message, Object location, int line, int column);
        public void displayErrorForFileObject(String message, FileObject fileObject, int line, int column);
    } // End of RaveErrorHandler.
// </error_handling>

// <service methods>
    /** Convert the given URL to a path: decode spaces from %20's, etc.
     * If the url does not begin with "file:" it will not do anything.
     * @todo Find a better home for this method */
    public abstract String fromURL(String url);
//    public abstract Element getCorrespondingSourceElement(Element elem);
    /** Given a general location object provided from the CSS parser,
     * compute the correct file name to use. */
    public abstract String computeFileName(Object location);
    public abstract int computeLineNumber(Object location, int line);
// </service methods>
    
    public abstract void copyMarkupMouseRegionForElement(Element fromElement, Element toElement);
    public abstract MarkupMouseRegion getMarkupMouseRegionForElement(Element element);
    
    public abstract void copyMarkupDesignBeanForElement(Element fromElement, Element toElement);
    public abstract MarkupDesignBean getMarkupDesignBeanForElement(Element element);
    public abstract void setMarkupDesignBeanForElement(Element element, MarkupDesignBean markupDesignBean);
    
    /** Generate the html string from the given node. This will return
     * an empty string unless the Node is an Element or a DocumentFragment
     * or a Document. */
    public abstract String getHtmlStream(Node node);
    /** Generate the html string from the given element */
    public abstract String getHtmlStream(Element element);
    /** Generate the html string from the given document. Does formatting. */
    public abstract String getHtmlStream(Document document);
    /** Generate the html string from the given document fragment */
    public abstract String getHtmlStream(DocumentFragment df);

// <from designer-service>
//    /**
//     * Return true iff the given file object represents a webform primary file
//     * (e.g. jsp, etc.)
//     */
//    public abstract boolean isWebPage(FileObject fo);
    /**
     * Return a List of web pages in the project
     * @param includePages Iff true, include non-fragment pages in the list
     * @param includeFragments Iff true, include page fragments in the list
     * @return A List containing FileObject entries for WebForms in the project
     */
    public abstract List getWebPages(Project project, boolean includePages, boolean includeFragments);
//    /**
//     * Return an array of String mime types for mime types considered to be webforms
//     * the designer will edit (and insync will provide models for etc.)
//     */
//    public abstract String[] getMimeTypes();
    
    /**
     * <p>
     * Resolve the given url (which can be relative, context relative or
     * absolute) to an absolute file URL. For example, let's say you have
     * a document "/tmp/foo.jsp" which references a stylesheet in "/tmp/css/bar.css"
     * and in this stylesheet you have a url "baz.png".
     * In this case the parameters to this method would have "base" pointing to the
     * css file, the url string would be the png filename, and the document reference
     * would point to the including jsp document.
     * </p>
     * The algorithm used by this method is as follows:
     * <ul>
     *   <li> If the url string represents its own URL (e.g. starts with a protocol)
     *      then the URL returned is the resulting full URL. </li>
     *   <li> Otherwise, if the url string does NOT start with "/", then a URL is
     *      formed by appending it to the base URL passed in
     *   <li> Otherwise, this is a context relative URL (because it begins with "/")
     *      and the base URL is computed by finding the project associated with
     *      the document parameter, and from the document the WEB root is located.
     *      This is taken as the base and the URL is computed as above.
     * </ul>
     *
     * @param base The URL of the referrer, which the url string will be taken
     *   to be relative to, unless it is an "absolute" (context relative) string,
     *   such as "/resources". In that case it will look up the project associated
     *   with the given document and find its context root from there.
     * @param document A document related (more distantly than the base) to the
     *   url reference.
     * @param url A string which represents a relative URL, or a context url, or
     *   even a complete url on its own (http://www.sun.com/jscreator).
     */
    public abstract URL resolveUrl(URL base, Document document, String url);
// <from designer-service>

    
// XXX used in xhtml
    // xhtml/TableDesignInfo
    /** Just a marker interface representing the write lock. */
    public interface WriteLock {}
    public abstract WriteLock writeLockContext(DesignContext designContext, String message);
    public abstract void writeUnlockContext(DesignContext designContext, WriteLock lock);
    public abstract void addLocalStyleValueForElement(Element element, int style, String value);
    public abstract void removeLocalStyleValueForElement(Element element, int style);
    // xhtml/FragmentPanel
    public abstract Project getProjectForDesignProject(DesignProject designProject);
    public abstract FileObject getMarkupFileObjectForDesignContext(DesignContext designContext);
    /** XXX Very suspicious, try to get rid of it. */
    public abstract void initModelsForWebformFile(Project project, FileObject webformFile);
    
    // XXX html entities
    public abstract String expandHtmlEntities(String html, boolean warn, Node node);
    public abstract int getExpandedOffset(String unexpanded, int unexpandedOffset);
    public abstract int getUnexpandedOffset(String unexpanded, int expandedOffset);
    
    // Thread.currentThread().getContextClassLoader() stuff
    public abstract ClassLoader getContextClassLoader(DesignContext designContext);
    public abstract ClassLoader getContextClassLoader(DesignBean designBean);
    public abstract ClassLoader getContextClassLoader(DesignProperty designProperty);
    public abstract ClassLoader getContextClassLoader(DesignEvent designEvent);
}
