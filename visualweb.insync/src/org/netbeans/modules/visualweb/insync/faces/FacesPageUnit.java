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
package org.netbeans.modules.visualweb.insync.faces;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.java.Statement;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.netbeans.modules.visualweb.jsfsupport.container.JsfTagSupportException;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignInfo;
import com.sun.rave.designtime.markup.MarkupPosition;
import com.sun.rave.designtime.markup.MarkupRenderContext;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.Property;
import org.netbeans.modules.visualweb.insync.live.BeansDesignBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.jsfsupport.container.FacesContainer;
import org.w3c.dom.Text;

/**
 * An extended FacesUnit that adds in the ability to have a JSF bean being designed have source in
 * JSPX in addition to the regular Java source.
 *
 * @author cquinn
 */
public class FacesPageUnit extends FacesUnit implements PropertyChangeListener {

    public static final String URI_JSP_PAGE = "http://java.sun.com/JSP/Page";
    public static final String URI_JSF_CORE = "http://java.sun.com/jsf/core";
    public static final String URI_JSF_HTML = "http://java.sun.com/jsf/html";

    final UIViewRoot viewRoot;  // our reusable Faces view tree root instance

    protected MarkupUnit pgunit;  // the page definition markup unit
    Document document;        // the current DOM document from the unit
    String defaultSrcEncoding = "UTF-8";  // the default source encoding
    String defaultEncoding = "UTF-8";  // the default (response) encoding
    String defaultLanguage;  // the default (response) language

    private MarkupBean defaultParent;   // the current default parent for new faces beans
    private DesignBean preRendered; // A design bean we've pre-rendered
    private DocumentFragment preRenderedFragment; // For the preRendered bean, use this fragment
    private Exception renderFailure; // Most recent render-failure exception
    private DesignBean renderFailureComponent; // Most recent render-failure component
    protected DataObject pageUnitDataObject;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct an FacesPageUnit from existing java and markup units
     *
     * @param junit
     * @param cl
     * @param pkgname
     * @param rootPackage
     * @param container
     * @param pgunit
     */
    public FacesPageUnit(JavaUnit junit, ClassLoader cl, String pkgname, Model model, String rootPackage,
                         FacesContainer container, MarkupUnit pgunit) {
        super(junit, cl, pkgname, model, rootPackage, container);
        this.pgunit = pgunit;
        viewRoot = container.newViewRoot();
        //Trace.enableTraceCategory("insync.faces");
        // Storing the dataObject such that we can remove the property change listener later in destroy
        // order of destroy may mean that the page unit no longer can get access to its data object
        pageUnitDataObject = pgunit.getDataObject();
        pageUnitDataObject.addPropertyChangeListener(this);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#destroy()
     */
    public void destroy() {
        if (pageUnitDataObject != null) {
            pageUnitDataObject.removePropertyChangeListener(this);
        }
        pgunit = null;
        pageUnitDataObject = null;
        document = null;
        super.destroy();
    }

    //----------------------------------------------------------------------------------------- Unit

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeLock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public void writeLock(UndoEvent event) {
        pgunit.writeLock(event);
        try {
            super.writeLock(event);
        }
        catch (IllegalStateException e) {
            // if the second lock bombed, the undo our first lock
            pgunit.writeUnlock(event);
            throw e;
        }
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeUnlock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public boolean writeUnlock(UndoEvent event) {
        boolean unlocked = super.writeUnlock(event);
        unlocked |= pgunit.writeUnlock(event);
        return unlocked;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#isWriteLocked()
     */
    public boolean isWriteLocked() {
        return pgunit.isWriteLocked() || super.isWriteLocked();
    }

    /**
     * @return the combined state of this unit. A dirty state overrules clean. The only illegal
     *         state is one DirtySource and one DirtyModel
     * @see org.netbeans.modules.visualweb.insync.Unit#getState()
     */
    public State getState() {
        State beanstate = super.getState();
        State pgstate = pgunit.getState();
        if (pgstate == beanstate)
            return beanstate;
        if (beanstate == State.BUSTED || pgstate == State.BUSTED)
            return State.BUSTED;
        if (beanstate == State.CLEAN)
            return pgstate;
        if (pgstate == State.CLEAN)
            return beanstate;
        if(pgstate == State.MODELDIRTY)
            return State.MODELDIRTY;
        // this is a bad mix and should never happen
        throw new IllegalStateException("Illegal state mix " + this + "(" + beanstate + ") and " + pgunit + "(" + pgstate + ")");
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getErrors()
     */
    public ParserAnnotation[] getErrors() {
        ParserAnnotation[] pgErrors = pgunit.getErrors();
        ParserAnnotation[] beansErrors = super.getErrors();
        int errorCount = pgErrors.length + beansErrors.length;
        if (errorCount == 0)
            return beansErrors;

        // Must combine
        ParserAnnotation[] errors = new ParserAnnotation[errorCount];
        int index = 0;
        System.arraycopy(pgErrors, 0, errors, index, pgErrors.length);
        index += pgErrors.length;
        System.arraycopy(beansErrors, 0, errors, index, beansErrors.length);
        index += beansErrors.length;
        return errors;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readLock()
     */
    public void readLock() {
        pgunit.readLock();
        super.readLock();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readUnlock()
     */
    public void readUnlock() {
        super.readUnlock();
        pgunit.readUnlock();
    }

    protected boolean syncSubUnits() {
        // read supporting dom document first, then our bean definitions since that will call us
        // back during binding
        boolean synced = pgunit.sync();
        // call the internal justSync method so that we can manage scan & bind ourselves
        synced |= super.syncSubUnits();
        return synced;
    }

    //------------------------------------------------------------------------------------ BeansUnit

    /**
     * Scan our document to find or create our outer tracked elements
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#scan()
     */
    protected void scan() {
        super.scan();  // make sure class & other base java stuff are retrieved first
        // Now go through and find or create our JSP elements
        document = pgunit.getSourceDom();
//        if (isPage())
//            ensurePageStructure();
//        else
//            ensureFragmentStructure();
    }

    /**
     * Determine if the current document is a complete page (true) or a fragment (false)
     *
     * @return true iff the current document is a complete page
     */
    public boolean isPage() {
        return document.getDocumentElement().getTagName().equals("jsp:root");
    }

    /**
     * For complete pages, make sure that the outer JSPX structure is correct.
     */
    private void ensurePageStructure() {
        // Get or create the JSP root element. All other elements are children of this
        Element root = MarkupUnit.ensureRoot(document, "jsp:root");

        // Make sure the NS URIs are present, supplying default prefixes in case they aren't
        String jspPre = pgunit.getNamespacePrefix(URI_JSP_PAGE, "jsp");
        String facesPre = pgunit.getNamespacePrefix(URI_JSF_CORE, "f");
        pgunit.getNamespacePrefix(URI_JSF_HTML, "h");

        // We require JSP 1.2
        String projectVersion = JsfProjectUtils.getProjectVersion(model.getProject());
        if(projectVersion.equals(J2eeModule.J2EE_13) ||
                projectVersion.equals(J2eeModule.J2EE_14)) {
            pgunit.ensureAttributeValue(root, "version", "1.2");            
        } else if(projectVersion.equals(J2eeModule.JAVA_EE_5)) {
            pgunit.ensureAttributeValue(root, "version", "2.1");
        }

        // Get or create (as first child) a JSP page directive element to control page type and
        // source charset encoding. Use the page' xml encoding if available, else use our default
        // for both.
        Element pgdirective = MarkupUnit.ensureElement(root, jspPre + ":directive.page", null);
        String srcEncoding = pgunit.getEncoding();
        if (srcEncoding == null || srcEncoding.length() == 0)
            srcEncoding = pgdirective.getAttribute("pageEncoding");
        if (srcEncoding == null || srcEncoding.length() == 0)
            srcEncoding = defaultSrcEncoding;        
        pgunit.ensureAttributeValue(pgdirective, "pageEncoding", srcEncoding);

        // set the page response content type and charset (encoding)
        String encoding = defaultEncoding;
        pgunit.ensureAttributeExists(pgdirective, "contentType", "text/html;charset=" + encoding);

        // Get or create the Faces view element, which is the root of all other faces UIComponent
        // elements
        Element view = MarkupUnit.ensureElement(root, facesPre+":view", pgdirective);

        // The following code was used in Creator 1.0: it goes and ENFORCES
        // a particular page structure:  <html><body><h:form> (with various
        // attributes on these tags.
        // This is no longer a required page structure - and in fact our
        // new default pages, which use the Sun WEB UI components (braveheart)
        // do not follow it.
        // ensureHtmlPageStructure(view);
    }

    private void ensureHtmlPageStructure(Element view) {
        // Get or create the basic xhtml elements
        Element html = MarkupUnit.ensureElement(view, HtmlTag.HTML.name, null);
        pgunit.ensureAttributeExists(html, HtmlAttribute.LANG, defaultLanguage);
        pgunit.ensureAttributeExists(html, "xml:lang", defaultLanguage);

        Element head = MarkupUnit.ensureElement(html, HtmlTag.HEAD.name, null);

        // Add this meta/http-equiv tag as a hint to browsers...
        //!CQ now relying on jsp:directive.page
        /*
        Element ctmeta = MarkupUnit.getDescendantElementByAttr(head, HtmlTag.META.name, "http-equiv", "Content-type");
        if (ctmeta == null) {
            ctmeta = pgunit.addElement(head, null, null, null, HtmlTag.META.name);
            pgunit.ensureAttributeValue(ctmeta, "http-equiv", "Content-type");
            pgunit.ensureAttributeValue(ctmeta, "content", "text/html;charset=" + encoding);
        }*/

        Element title = MarkupUnit.ensureElement(head, HtmlTag.TITLE.name, null);
        if (MarkupUnit.getElementText(title).equals("__TITLE__"))
            MarkupUnit.setElementText(title, getBeanName() + " Title");

        // If there is no body, add a default one in grid layout mode
        if (MarkupUnit.getFirstDescendantElement(html, HtmlTag.BODY.name) == null &&
            MarkupUnit.getFirstDescendantElement(html, HtmlTag.FRAMESET.name) == null) {
            Element body = MarkupUnit.ensureElement(html, HtmlTag.BODY.name, head);
            body.setAttribute(HtmlAttribute.STYLE, "-rave-layout: grid"); // NOI18N
        }
    }

    /**
     * For page fragments, make sure that there is an outer div.
     */
    private void ensureFragmentStructure() {
        // Get or create the JSP root element. All other elements are children of this
        Element root = MarkupUnit.ensureRoot(document, HtmlTag.DIV.name);
    }

    /**
     * Bind all markup beans to their element and take care of parenting. Creates the HtmlBean
     * instances for HTML and other fake beans on the fly
     */
    protected void bindMarkupBeans(Element e) {
        MarkupBean mbean = getMarkupBean(e);
        if (mbean == null) {
            String type = null;
            String tagName = e.getLocalName();
            String name = e.getAttribute(FacesBean.ID_ATTR);
            String tagLibUri = pgunit.findTaglibUri(e.getPrefix());
            try {
                type = container.findComponentClass(tagName, tagLibUri);
                assert Trace.trace("insync.faces", "FU.bindMarkupBeans type:" + type +
                        " tag:" + tagName + " tagLibUri:" + tagLibUri);
            } catch (JsfTagSupportException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }

            if (type != null) {
                BeanInfo bi = getBeanInfo(type);
                if (bi == null) {
                    // XXX #139640 Avoiding possible NPE.
                    debugLog("There was no BeanInfo found for type=" + type); // NOI18N
                } else {
                    mbean = new FacesBean(this, bi, name, e);
                    // snag the form bean as it goes by for later use as the default parent
                    if (defaultParent == null && tagName.equals(HtmlTag.FORM.name)) {
                        defaultParent = mbean;
                    }
                }
            } else {
                type = HtmlBean.getBeanClassname(e);
                if (type != null) {
                    BeanInfo bi = getBeanInfo(type);
                    if (bi != null) {
                        mbean = new HtmlBean(this, bi, e.getTagName(), e);
                    }
                }
            }
        }
        if (mbean != null) {
            beans.add(mbean);
            Bean parent = mbean.bindParent();
            if (parent != null)
                parent.addChild(mbean, null);  // append is right since we are walking the DOM here
        }

        NodeList ekids = e.getChildNodes();
        int ekidcount = ekids.getLength();
        for (int i = 0; i < ekidcount; i++) {
            Node n = ekids.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE)
                bindMarkupBeans((Element)n);
        }
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#bind()
     */
    protected void bind() {
        // with a new document, we'll need to recompute our defaultParent
        defaultParent = null;

        // scan our markup document to wire up the bean tree to match the DOM
        bindMarkupBeans(document.getDocumentElement());
        
        super.bind();

        // Find a suitable default parent for null-parented creations
        // first try finding the first form-compatible bean
        if (defaultParent == null) {
            Bean bean = firstBeanOfType(getBeans(), UIForm.class);
            if (bean instanceof MarkupBean)
                defaultParent = (MarkupBean)bean;
        }

        // if not found and this is a complete page, then we need to create a new default form bean
        if (defaultParent == null) {
            if (isPage()) {
                // Use the braveheart form component on Braveheart pages
//                if (DesignerServiceHack.getDefault().isBraveheartPage(pgunit.getSourceDom())) {
                if (InSyncServiceProvider.get().isWoodstockPage(pgunit.getSourceDom())) {
                    defaultParent = (MarkupBean)addBean(getBeanInfo(com.sun.webui.jsf.component.Form.class.getName()), null, "form", null, null);
                } else if (InSyncServiceProvider.get().isBraveheartPage(pgunit.getSourceDom())) {
                    defaultParent = (MarkupBean)addBean(getBeanInfo(com.sun.rave.web.ui.component.Form.class.getName()), null, "form", null, null);
                } else {
                    defaultParent = (MarkupBean)addBean(getBeanInfo("javax.faces.component.html.HtmlForm"), null, "form", null, null);
                }
            }
            else {
                Bean[] beans = getBeans();
                if (!isPage()) {
                    Element root = document.getDocumentElement();
                    Element subview = Util.findChild("f:subview", root, false);
                    if (subview != null) {
                        defaultParent = (MarkupBean)firstBeanOfType(beans, "f:subview");
                        // This might be the wrong place to set it...

                        // The id must begin with a Character.isLetter or _, and the rest of
                        // the characters must be isLetter(), isDigit(), or _.
                        // (Otherwise UIComponentBase.validateId() will barf at runtime)                        
                        // Generating this is pretty easy for us because we already know
                        // that the markup file must be named a valid Java classname,
                        // and will therefore follow the right conventions! Thus, all we
                        // need to do is strip out the extension and we're done!
                        String id = pgunit.getFileObject().getName(); // no extension
                        pgunit.ensureAttributeValue(subview, "id", id); // NOI18N
                    }
                    if (defaultParent == null) {
                        defaultParent = firstBeanOfType(beans, root.getTagName());
                    }
                } else {
                    // then try <body>, <div> and finally an outer <html>
                    defaultParent = (MarkupBean)firstBeanOfType(beans, HtmlTag.BODY.name);
                }
                if (defaultParent == null)
                    defaultParent = firstBeanOfType(beans, HtmlTag.FRAMESET.name);
                if (defaultParent == null)
                    defaultParent = firstBeanOfType(beans, HtmlTag.DIV.name);
                if (defaultParent == null)
                    defaultParent = firstBeanOfType(beans, HtmlTag.HTML.name);
                // worst case, create an outer <div>
                if (defaultParent == null) {
//                    defaultParent = (MarkupBean)addBean(getBeanInfo("org.netbeans.modules.visualweb.xhtml.Div"), null, "div", null, null);
                    defaultParent = (MarkupBean)addBean(getBeanInfo(org.netbeans.modules.visualweb.xhtml.Div.class.getName()), null, "div", null, null);
                }
            }
        }
    }

    /*
     * We override as a no-op here since parenting for a FacesPageUnit is defined by the JSP XML elements hierarchy.
     * In a regular bean the parentage is flat.
     */
    protected void bindBeanParents() {
    }

    /**
     * Find the first occurance of a child bean with a given tag.
     *
     * @param bean the parent bean whose children will be searched.
     * @param tagName the tag name to search for.
     * @return the MarkupBean found, or null of none.
     */
    protected static MarkupBean firstBeanOfType(Bean bean, String tagName) {
        if (bean instanceof MarkupBean) {
            MarkupBean mbean = (MarkupBean)bean;
            Element element = mbean.getElement();
            if (element.getTagName().equals(tagName))
                return mbean;
        }
        Bean[] kids = bean.getChildren();
        return kids != null ? firstBeanOfType(kids, tagName) : null;
    }

    /**
     * Find the first occurance of a bean from a list, with a given tag.
     *
     * @param beans the list of beans to be searched.
     * @param tagName the tag name to search for.
     * @return the MarkupBean found, or null of none.
     */
    protected static MarkupBean firstBeanOfType(Bean[] beans, String tagName) {
        for (int i = 0; i < beans.length; i++) {
            MarkupBean kb = firstBeanOfType(beans[i], tagName);
            if (kb != null)
                return kb;
        }
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#newBoundBean(java.beans.BeanInfo, java.lang.String, org.netbeans.modules.visualweb.insync.java.Field, org.netbeans.modules.visualweb.insync.java.Method, org.netbeans.modules.visualweb.insync.java.Method)
     */
    protected Bean newBoundBean(BeanInfo bi, String name, List<String> typeNames) {
        String tag = getBeanTagName(bi);
        // Determine the source tag for this bean and if not a faces bean to 
        // bind, delegate it to super class
        if (tag == null) {
            return super.newBoundBean(bi, name, typeNames);
        }
        return null;
    }

    /**
     * We will create any unparented beans, or faces beans with faces parents
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#canCreateBean(java.beans.BeanInfo, org.netbeans.modules.visualweb.insync.beans.Bean)
     */
    public boolean canCreateBean(BeanInfo bi, Bean parent) {
        boolean can = bi != null &&
                      (parent == null ||
                              parent instanceof MarkupBean && parent.isParentCapable() && (isFacesBean(bi) || isHtmlBean(bi)));
        assert Trace.trace("insync.faces", "FU.canCreateBean" +
                           " type:" + bi.getBeanDescriptor().getBeanClass().getName() +
                           " parent:" + parent + " can:" + can);
        return can;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#newCreatedBean(java.beans.BeanInfo, org.netbeans.modules.visualweb.insync.beans.Bean, java.lang.String, java.lang.String)
     */
    protected Bean newCreatedBean(BeanInfo bi, Bean parent, String name, String facet, Position pos) {
        // Determine the source tag for this bean and thus if it is faces
        String tag = getBeanTagName(bi);
        assert Trace.trace("insync.faces", "FU.newCreatedBean " +
                           " type:" + bi.getBeanDescriptor().getBeanClass().getName() +
                           " parent:" + parent +
                           " name:" + name +" tag:" + tag);

        // If not a faces bean, return new regular bean
        if (tag == null)
            return super.newCreatedBean(bi, parent instanceof FacesBean ? null : parent, name, facet, pos);

        // if bean parent supplied, use the corresponding markup & DOM parent
        MarkupBean mparent = parent instanceof MarkupBean ? (MarkupBean)parent : null;
        Element eparent = mparent != null ? mparent.getElement() : null;

        // If the beaninfo tells us where to default the section, then use that element parent,
        // and faces parent if none was given, or if the given parent is not within the section.
        String section = getBeanMarkupSection(bi);
        if (section != null) {
            Element se = findMarkupSectionElement(section);
            if (se != null && !MarkupUnit.isDescendent(se, eparent)) {
                eparent = se;
                mparent = getMarkupBean(eparent);
				// Bug fix # 6471512 - Script tag shows in outline head1, but in jsp under form1
				// reset the position
                pos = new MarkupPosition(-1);
            }
        }

        // last chance, default all un-parented beans to the form
        if (mparent == null && defaultParent != null) {
            mparent = defaultParent;
            eparent = mparent.getElement();
        }

        // Insert new faces element into page dom
        String tlUri = getBeanTaglibUri(bi);
        String tlPre = getBeanTaglibPrefix(bi);
        Element element = addCompElement(eparent, tlUri, tlPre, tag, facet, pos);

        // Return new HtmlBean that represents the html element only
        Bean bean;
        if (HtmlBean.isHtmlBean(bi)) {
            HtmlBean hbean = new HtmlBean(this, bi, tag, mparent, element);
            bean = hbean;
        }
        // or, return new FacesBean that encorporates the element and the java side
        else {
            FacesBean fbean = new FacesBean(this, bi, name, mparent, element);
            //fbean.insertEntry(null);  // this bean's Java source position doesn't matter
            
            //Do not insert the binding for Faces Beans to the java source
            //beansToAdd.add(fbean);
            // Also do not set the binding 
            //fbean.setBindingProperties();
            
            bean = fbean;
        }

        // now add to main list and to parent by position
        beans.add(bean);
        if (mparent != null)        // add this child to parent
            mparent.addChild(bean, pos);

        return bean;
    }
    
    /**
     * Find the JSF element matching a given markup section name.
     * For example, if the section is "head", we should return
     * <head>. However, it's not that simple - we have to return
     * the JSF element which -renders- <head>, which in a typical
     * Braveheart page will be <ui:head>.
     */
    private Element findMarkupSectionElement(String section) {
        assert section != null;
        
// <removing set/getRoot from RaveDocument>
//        if (document instanceof RaveDocument) {
//            RaveDocument doc = (RaveDocument)document;
//            if (doc.getRoot() != doc.getDocumentElement()) {
//                RaveElement e = (RaveElement)MarkupUnit.getFirstDescendantElement(doc.getRoot(), section);
// ====
        if (model instanceof FacesModel) {
            FacesModel facesModel = (FacesModel)model;
            DocumentFragment html = facesModel.getHtmlDomFragment();
            Element effectiveRoot = null;
            NodeList nl = html.getChildNodes();
            for (int i = 0, n = nl.getLength(); i < n; i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    effectiveRoot = (Element)node;
                    break;
                }
            }
            if (effectiveRoot != document.getDocumentElement()) {
//                RaveElement e = (RaveElement)MarkupUnit.getFirstDescendantElement(effectiveRoot, section);
                Element e = MarkupUnit.getFirstDescendantElement(effectiveRoot, section);
// <removing set/getRoot from RaveDocument>
                if (e != null) {
//                    MarkupDesignBean bean = e.getDesignBean();
                    MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(e);
                    if (bean != null) {
                        return bean.getElement();
                    }
                }
            }
        }

        // Old way
        return MarkupUnit.getFirstDescendantElement(document.getDocumentElement(), section);
    }

    /**
     * Add the appropriate element in the correct position for a new component.
     *
     * @param eparent The potential element parent. May be null to use default.
     * @param taglibUri The taglib URI for the new element tag.
     * @param tagPrefix The taglib prefix for the new element tag.
     * @param tag The element tag name.
     * @param facet The facet element that should wrap the new element.
     * @param pos The Position or MarkupPosition that more presicely defines where the element
     *            should be placed.
     * @return The newly created element.
     */
    private Element addCompElement(Element eparent, String taglibUri, String tagPrefix, String tag,
                                   String facet, Position pos) {

        MarkupPosition mpos = pos instanceof MarkupPosition ? (MarkupPosition)pos : null;
        Node before = mpos != null ? mpos.getBeforeSibling() : null;

        // if no parent was provided pick the best one: specified one, default if available, body if
        // found, otherwise root

        if (mpos != null) {
            if (mpos.getUnderParent() instanceof Element)
                eparent = (Element)mpos.getUnderParent();
            else if (before != null && before.getParentNode() != eparent)
                eparent = (Element)before.getParentNode();
        }
        if (eparent == null) {
            if (defaultParent != null) {
                eparent = defaultParent.getElement();
            }
            else {
                eparent = MarkupUnit.getFirstDescendantElement(document.getDocumentElement(), HtmlTag.BODY.name);
                // framesets can't contain anything other than frame or
                // other nested framesets, so don't worry about that here
                if (eparent == null)
                    eparent = document.getDocumentElement();  // need something at least
            }
        }

        // if before-sibling is missing, figure out from parent+index
        if (before == null) {
            if (pos != null && pos.getIndex() >= 0)
                before = eparent.getChildNodes().item(pos.getIndex());
        }

        // if this component is a facet, then inject the intermediate <f:facet> tag
        if (facet != null) {
            eparent = pgunit.addElement(eparent, before, URI_JSF_CORE, null, "facet");
            eparent.setAttribute("name", facet);
        }

        return pgunit.addElement(eparent, before, taglibUri, tagPrefix, tag);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#moveBean(org.netbeans.modules.visualweb.insync.beans.Bean, org.netbeans.modules.visualweb.insync.beans.Bean, com.sun.rave.designtime.Position)
     */
    public void moveBean(Bean bean, Bean newparent, Position pos) {
        // change the Bean parentage
        super.moveBean(bean, newparent, pos);

        // for markup beans, fixup the markup DOM element parentage
        if (bean instanceof MarkupBean && newparent instanceof MarkupBean) {
            MarkupPosition mpos = pos instanceof MarkupPosition ? (MarkupPosition)pos : null;
            Node before = mpos != null ? mpos.getBeforeSibling() : null;

            // start out presuming parent element is the parent bean's element, then see if mpos
            // has a better one
            Element eparent = ((MarkupBean)newparent).element;
            if (mpos != null) {
                if (mpos.getUnderParent() instanceof Element)
                    eparent = (Element)mpos.getUnderParent();
                else if (before != null && before.getParentNode() != eparent)
                    eparent = (Element)before.getParentNode();
            }

            // if before-sibling is missing, figure out from parent+index
			if (before == null && pos.getIndex() >= 0) {
                 Bean[] children = newparent.getChildren();
                 for (int i = 0; i < children.length; i++) {
                        Bean childBean = children[i];
                        if (childBean == bean && i < (children.length - 1)) {
                                before = ((MarkupBean)children[i+1]).element;
                        }
                 }
             }

            eparent.insertBefore(((MarkupBean)bean).element, before);
        }
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#newBoundProperty(org.netbeans.modules.visualweb.insync.java.Statement)
     */
    protected Property newBoundProperty(Statement stmt) {
        return MarkupProperty.newBoundInstance(this, stmt);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#bindEventSets()
     */
    protected void bindEventSets(List<Statement> stmts) {
        // bind statement-based event wiring
        super.bindEventSets(stmts);

        // bind markup-based event wiring
        for (Iterator i = beans.iterator(); i.hasNext(); ) {
            Bean bean = (Bean)i.next();
            if (bean instanceof FacesBean)
                ((FacesBean)bean).bindEventSets();
        }
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Set the default language for this unit.
     *
     * @param defaultLanguage
     */
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
    
    /** 
     * Return the default language to be used in this page 
     *
     * @return The default language that should be used for this page if not otherwise specified
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Set the default (response) encoding for this unit.
     *
     * @param encoding The default response encoding.
     */
    public void setDefaultEncoding(String encoding) {
        this.defaultEncoding = MarkupUnit.getIanaEncoding(encoding);
    }

    /**
     * Get the effective response encoding for this unit.
     *
     * @return The current response encoding.
     */
    public String getEncoding() {
        if (isPage()) {
            Element root = document.getDocumentElement();
            Element jspd = MarkupUnit.ensureElement(root, "jsp:directive.page", null);
            String ct = jspd.getAttribute("contentType");
            if (ct != null) {
                int cs = ct.indexOf("charset=");
                if (cs > 0)
                    return ct.substring(cs + 8);
            }
        }
        return null;
    }

    /**
     * Set the effective response encoding for this unit.
     *
     * @param encoding The response encoding, or null to use default.
     */
    public void setEncoding(String encoding) {
        if (encoding == null)
            encoding = defaultEncoding;
        else
            encoding = MarkupUnit.getIanaEncoding(encoding);
        if (isPage()) {
            Element root = document.getDocumentElement();
            Element jspd = MarkupUnit.ensureElement(root, "jsp:directive.page", null);
            pgunit.ensureAttributeExists(jspd, "pageEncoding", encoding);  // don't whack src encoding
            pgunit.ensureAttributeValue(jspd, "contentType", "text/html;charset=" + encoding);
        }
    }

    /**
     * Set the default encoding for the jsp source
     *
     * @param encoding
     */
    public void setDefaultSrcEncoding(String encoding) {
        this.defaultSrcEncoding = encoding;
    }

    /**
     * Listen for JSP encoding changes from the editor so that our layer can mirror the changes.
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("encoding") && pgunit.getState().isModelAvailable()) {
            try {
                writeLock(null);
                setSrcEncoding(pgunit.getEncoding());
            }
            finally {
                writeUnlock(null);
            }
        }
    }

    /**
     * Set the page source encoding to follow a dobj encoding property change
     *
     * @param encoding
     */
    private void setSrcEncoding(String encoding) {
        if (encoding == null)
            encoding = defaultSrcEncoding;
        if (isPage()) {
            Element root = document.getDocumentElement();
            Element jspd = MarkupUnit.ensureElement(root, "jsp:directive.page", null);
            pgunit.ensureAttributeValue(jspd, "pageEncoding", encoding);
        }
    }

    /**
     * Override super's to make sure our viewRoot is active
     *
     * @see org.netbeans.modules.visualweb.insync.faces.FacesUnit#getFacesContext()
     */
    public FacesContext getFacesContext() {
        FacesContext facesContext = super.getFacesContext();
        facesContext.setViewRoot(viewRoot);  // the view root for the component tree to be rendered
        return facesContext;
    }

    /**
     * Get the JSF page view root for this unit.
     *
     * @return The view root for this JSF page.
     */
    public UIViewRoot getViewRoot() {
        return viewRoot;
    }

    /**
     * Return the form bean associated with the unit.
     *
     * @return the form bean associated with the unit.
     */
    public MarkupBean getDefaultParent() {
        return defaultParent;
    }

    /**
     * Set the default parent associated with this unit.
     * The passed in bean must be a valid bean in this faces unit.
     * Avoid this method, this is primarily used by clients that
     * really know what they're doing and don't want any of the
     * default behavior - such as the Page Import feature.
     */
    public void setDefaultParent(MarkupBean defaultParent) {
        this.defaultParent = defaultParent;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#getRootElement()
     */
    public org.w3c.dom.Element getRootElement() {
        return document.getDocumentElement();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansUnit#getRootInstance()
     */
    public Object getRootInstance() {
        return getViewRoot();  //container.getFacesContext().getViewRoot();
    }

    /**
     * Get the component binding string for a given component bean name.
     *
     * @param bname The component bean name.
     * @return The component binding string.
     */
    public final String getCompBinding(String bname) {
        String jbname = getBeanName();
        return "#{" + jbname + "." + bname + "}";
    }

    /**
     * @return
     */
    public MarkupUnit getPageUnit() {
        return pgunit;
    }

    /**
     * Return the child element with a given id attr within the page
     *
     * @param id The id attrribute string.
     * @return The matching component element if found.
     */
    public Element findCompElement(String id) {
        return MarkupUnit.getDescendantElementByAttr(document.getDocumentElement(), "*",
                                                     FacesBean.ID_ATTR, id);
    }

    /**
     * Get the markup bean for a given faces tag element.
     *
     * @param e the tag element.
     * @return the markup bean for the given faces tag element, null if element has no associated
     *         bean //!CQ TODO: could hash element=>bean instead
     */
    public MarkupBean getMarkupBean(Element e) {
        Bean[] beans = getBeans();
        for (int i = 0; i < beans.length; i++) {
            if (beans[i] instanceof MarkupBean) {
                MarkupBean mb = (MarkupBean)beans[i];
                if (mb.element == e) {
                    assert Trace.trace("insync.faces", "FU.getMarkupBean:" + e + " => " + mb);
                    return mb;
                }
            }
        }
        assert Trace.trace("insync.faces", "FU.getMarkupBean:" + e + " => null");
        return null;
    }

    /**
     * Get the faces bean for a given faces tag element.
     *
     * @param e the tag element.
     * @return the faces bean for the given faces tag element, null if not a faces element.
     */
    public FacesBean getFacesBean(Element e) {
        MarkupBean mb = getMarkupBean(e);
        return mb instanceof FacesBean ? (FacesBean)mb : null;
    }

    /**
     * @return the faces bean for a given faces tag element or any of its ancestors. Null if not a
     * faces element.
     */
    public FacesBean getFacesAncestorBean(Element e) {
        Element p = e;
        do {
            FacesBean fbean = getFacesBean(p);
            if (fbean != null)
                return fbean;
            p = (Element)p.getParentNode();
        }
        while (p != null);
        return null;
    }

    /**
     * Debug method for dumping a faces tree to stderr
     *
     * @param uic The parent of the tree to dump.
     * @param indent The indent level, 0 to start.
     */
    public void dumpFacesComp(UIComponent uic, int indent) {
        for (int i = 0; i < indent; i++)
            System.err.print("  ");
        System.err.println(uic);
        List kids = uic.getChildren();
        for (Iterator i = kids.iterator(); i.hasNext(); ) {
            UIComponent kid = (UIComponent)i.next();
            dumpFacesComp(kid, indent+1);
        }
    }

    /**
     * Get a complete render tree as a document fragment for a given component bean.
     *
     * @param lbean The bean to render in DesignBean form.
     * @param lu The LiveUnit that hosts the given bean.
     * @return A complete DocumentFragment that contains the bean's rendered XHTML.
     * //!TODO: could easily extract lu from lbean here.
     */
    public DocumentFragment getFacesRenderTree(DesignBean lbean, LiveUnit lu) {
        assert Trace.trace("insync.faces", "FU.getFacesRenderTree bean:" + lbean);

        renderFailure = null;
        renderFailureComponent = null;
        
        // If we're looking for the pre-rendered fragment we're done
        if (preRendered == lbean) {
            // We need to make a copy since clients are allowed to (and do) mutate the
            // returned DocumentFragment from this method.
//            DocumentFragment df = (DocumentFragment)getPageUnit().getSourceDom().importNode(preRenderedFragment, true);
//            return df;
            return (DocumentFragment)getPageUnit().getRenderedDom().importNode(preRenderedFragment, true);
        }

        // create an empty doc fragment, & then render into it.
//        DocumentFragment df = getPageUnit().getSourceDom().createDocumentFragment();
        DocumentFragment df = getPageUnit().getRenderedDom().createDocumentFragment();

		ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(getClassLoader());
//        DocFragmentJspWriter rw = container.beginRender(lu, viewRoot, df);
            DocFragmentJspWriter rw = new DocFragmentJspWriter(container, df);
            container.beginRender(lu, viewRoot, rw);
            
            if (preRendered != null) {
                rw.setPreRendered((UIComponent)preRendered.getInstance(), preRenderedFragment);
            }
            
            try {
                if (getFacesBean(lbean) == null) {
                    Element element;
                    if(lbean instanceof MarkupDesignBean) {
                        element = ((MarkupDesignBean)lbean).getElement();
                    } else {
                        element = null;
                    }
                    
                    HashMap map = new HashMap();
                    addFirstFacesBeans(lbean, map);
                    renderNode(element, map, rw, lu);
                } else {
                    renderBean(lbean, rw, lu);
                }
            } catch (RenderError re) {
                // We've already handled these at the throw point
                ;
            }
            container.endRender(rw);
    
            //System.err.println("rendered:" + b + " as:");
            //getPageUnit().dump(df, new java.io.PrintWriter(System.err, true), 0);
    
            //System.err.println("ViewRoot");
            //dumpFacesComp(viewRoot, 1);
    
            return df;
    	} finally {
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    /**
     * Search down the hierarchy from the given design bean and add the
     * first (element, DesignBean) pair found in each bean subtree that corresponds
     * to a FacesBean.
     */
    private void addFirstFacesBeans(DesignBean curr, HashMap map) {
        FacesBean fb = getFacesBean(curr);
        if (fb != null) {
            map.put(((MarkupDesignBean)curr).getElement(), curr);
            // Stop searching this subtree
            return;
        }
        for (int i = 0, n = curr.getChildBeanCount(); i < n; i++) {
            addFirstFacesBeans(curr.getChildBean(i), map);
        }
    }
    
    
    /**
     * Set the "pre rendered" DocumentFragment for a particular bean.
     * Note: Only ONE bean can be pre-rendered at a time; this is not
     * a per-bean assignment. When set, this will cause the given
     * DocumentFragment to be inserted into the output fragment
     * rather than calling the bean's renderer.
     *
     * The bean must represent a UIComponent.
     *
     * This is intended to be used for for example having the ability
     * to "inline edit" a particular component's value; in that case
     * since we're not updating the value attribute during editing,
     * we want to suppress the normal rendered portion from the component
     * and instead substitute the inline-edited document fragment
     * corresponding to the parsed text output of the component.
     */
    public void setPreRendered(DesignBean bean, DocumentFragment df) {
        preRendered = bean;
        assert bean == null || bean.getInstance() instanceof UIComponent;
        preRenderedFragment = df;
    }
    
    /** 
     * Return the exception associated with the most recent getFacesRenderTree call.
     * You should call this method immediately after a {@link #getFacesRenderTree} call;
     * it will be overwritten by other requests. If there were no failures, returns null. */
    public Exception getRenderFailure() {
        return renderFailure;
    }

    /** 
     * Return the failing component associated with the most recent getFacesRenderTree call.
     * You should call this method immediately after a {@link #getFacesRenderTree} call;
     * it will be overwritten by other requests.  If there were no failures, returns null. */
    public DesignBean getRenderFailureComponent() {
        return renderFailureComponent;
    }

    /**
     * Called when component rendering aborts with an exception. Inserts a representative
     * icon and scrapes away any output already produced by the component.
     */
    private void renderError(Exception e, DesignBean bean, DocFragmentJspWriter rw, Node currentPos) {
        renderFailure = e;
        renderFailureComponent = bean;

        UIComponent uic = (UIComponent)bean.getInstance();

        // Log as "informational" to suppress footer messages and such
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

        Node parent = rw.getCurrent();

        if (currentPos != null) {
            parent = currentPos;
        }

        if (parent != null) {
            // Nuke everything related to this component
            ArrayList nodes = new ArrayList();
            Node curr = parent.getFirstChild();

            while (curr != null) {
//                if (curr instanceof RaveElement && (((RaveElement)curr).getDesignBean() == bean)) {
                if (curr instanceof Element
                && MarkupUnit.getMarkupDesignBeanForElement((Element)curr) == bean) {
                    nodes.add(curr);
                }

                curr = curr.getNextSibling();
            }

            Iterator it = nodes.iterator();

            while (it.hasNext()) {
                Node n = (Node)it.next();
                parent.removeChild(n);
            }
        }

        Document document = parent.getOwnerDocument();
        Element span = document.createElement(HtmlTag.SPAN.name);
        parent.appendChild(span); // TODO -- insert before instead?

        String style = "";
        DesignProperty property = bean.getProperty("style"); // NOI18N

        if (property != null) {
            Object o = property.getValueSource();

            if (o instanceof String) {
                style = (String)o;
            }
        }

        if (style.length() > 0) {
            span.setAttribute(HtmlAttribute.STYLE, style); // NOI18N
        }

        Element img = document.createElement(HtmlTag.IMG.name);
        span.appendChild(img);

        String url =
            org.netbeans.modules.visualweb.insync.SourceUnit.class.getResource("error-glyph.gif").toExternalForm();
        img.setAttribute(HtmlAttribute.SRC, url);
        String text = bean.getInstanceName();
        String msg = e.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            // Put the tag name inside a <b>
            Element b = document.createElement(HtmlTag.B.name);
            span.appendChild(b);
            b.appendChild(document.createTextNode(text));
            span.appendChild(document.createTextNode(": " + msg));
        } else {
            span.appendChild(document.createTextNode(text));
        }

        if (uic != null) {
//            ((RaveElement)span).setDesignBean((MarkupDesignBean)bean);
//            ((RaveElement)img).setDesignBean((MarkupDesignBean)bean);
            MarkupUnit.setMarkupDesignBeanForElement(span, (MarkupDesignBean)bean);
            MarkupUnit.setMarkupDesignBeanForElement(img, (MarkupDesignBean)bean);
        }
        
        throw new RenderError(e);
    }

    /**
     * Render a component into a doc fragment writer
     *
     * @param lbean The bean to render in DesignBean form.
     * @param rw The JSP response writer to write the rendering into.
     * @param lu The LiveUnit that hosts the given bean.
     */
    protected void renderBean(DesignBean lbean, DocFragmentJspWriter rw, LiveUnit lu) {
        FacesContext facesContext = container.getFacesContext();
        UIComponent uic = (UIComponent)lbean.getInstance();

        Node currentPos = rw.getCurrent();
        int depth = rw.getDepth();
        int startIndex = currentPos != null ? currentPos.getChildNodes().getLength() : -1;
        try {
            try {
                Trace.trace("insync.faces", "renderView encodeBegin...");
                uic.encodeBegin(facesContext);
            }
            catch (Exception e) {
                Trace.trace("jsfsupport.container", "error in tag encode: resetting");
                renderError(e, lbean, rw, currentPos);
                // Abort tag and its children entirely
                return;
            }

            //int childrenStart = rw.getPosition();
            try {
                Trace.trace("insync.faces", "renderView encodeChildren...");

                // If the component renders its own children, then let it take care of them
                if (uic.getRendersChildren()) {
                    uic.encodeChildren(facesContext);
                }
                // Otherwise, we need to render the children like a JSP page does, including the markup
                else {
                    // prepare a map of elements => facesbeans
                    HashMap bkidmap = new HashMap();
                    for (int i = lbean.getChildBeanCount()-1; i >= 0; i--) {
                        DesignBean lbkid = lbean.getChildBean(i);
                        FacesBean bkid = getFacesBean(lbkid);
                        if (bkid != null) {
                            bkidmap.put(bkid.getElement(), lbkid);
                        } else {
                            addFirstFacesBeans(lbkid, bkidmap);                            
                        }
                    }
                    Element e = getFacesBean(lbean).getElement();
                    NodeList ekids = e.getChildNodes();
                    int ekidcount = ekids.getLength();

                    for (int ei = 0; ei < ekidcount; ei++) {
                        Node child = ekids.item(ei);

                        // Facets should not be designtime rendered - component
                        // parents should handle that themselves (we don't know where
                        // they go!)
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element)child;
                            if (element.getLocalName().equals("facet") &&  // NOI18N
                                    element.getNamespaceURI().equals(URI_JSF_CORE)) {
                                continue;
                            }
                        }

                        renderNode(child, bkidmap, rw, lu);
                    }
               }
            }
            catch (Exception e) {
                Trace.trace("insync.faces", "error in encodeChildren: resetting");
                
                renderError(e, lbean, rw, currentPos);
                return; // abort tag
            }

            try {
                Trace.trace("insync.faces", "renderView encodeEnd...");
                uic.encodeEnd(facesContext);
            }
            catch (Exception e) {
                Trace.trace("insync.faces", "error in encodeEnd: resetting");
                renderError(e, lbean, rw, currentPos);
            }
        }
        finally {
            if (currentPos != null) {
                NodeList nl = currentPos.getChildNodes();
                int endIndex = nl.getLength();
                DocumentFragment df = rw.getFragment();
                annotateRender(lbean, df, currentPos, startIndex, endIndex);
                if (uic.getRendersChildren()) {
                    // The component may have rendered other JSF components as its children.
                    // We should try to annotate these too. We can't know accurately
                    // which nodes they emitted (and if they didn't emit anything, we have
                    // no way to know where they would have gone, so we can't call
                    // annotateRender in that case.)
                    Node curr = nl.item(startIndex);
                    int index = startIndex;
                    while (curr != null) {
//                      if (curr instanceof RaveElement) {
//                          annotateRenderTree(lbean, df, (RaveElement)curr, index);
//                      }
                        if (curr instanceof Element) {
                            annotateRenderTree(lbean, df, (Element)curr, index);
                        }
                        index++;
                        curr = curr.getNextSibling();
                    }
                }
            }

            // Ensure that children aborting during encode doesn't
            // leave the current node pointing somewhere in the subtree
            rw.setCurrent(currentPos, depth);
        }
    }

    /**
     * Return the FacesBean for the live bean. May be null, for non faces live beans.
     *
     * @param lb The live bean to get the faces bean for. May be null.
     * @return the FacesBean corresponding to the live bean, or null.
     */
    public static FacesBean getFacesBean(DesignBean lb) {
        if (!(lb instanceof BeansDesignBean)) {
            return null;
        }
        Bean b = ((BeansDesignBean) lb).getBean();
        if (b instanceof FacesBean) {
            return (FacesBean) b;
        }
        return null;
    }


    /**
     * Call annotateRender on a constructed component (if it has an annotateRender)
     *
     * @param bean
     * @param df
     * @param parent
     * @param start
     * @param end
     */
    private void annotateRender(DesignBean bean, DocumentFragment df, Node parent, int start, int end) {
        DesignInfo lbi = bean.getDesignInfo();
        if (lbi instanceof MarkupDesignInfo && bean instanceof MarkupDesignBean) {
            MarkupDesignInfo mlbi = (MarkupDesignInfo)lbi;
            int length = parent.getChildNodes().getLength();
            Node startBefore = start >= 0 && start < length
                    ? parent.getChildNodes().item(start)
                    : null;
            Node endBefore = end >= 0 && end < length ? parent.getChildNodes().item(end) : null;
            MarkupPosition startPos = new MarkupPosition(parent, startBefore);
            MarkupPosition endPos = new MarkupPosition(parent, endBefore);
            renderContext.fragment = df;
            renderContext.begin = startPos;
            renderContext.end = endPos;
            mlbi.customizeRender((MarkupDesignBean)bean, renderContext);
        }
    }

    private RenderContext renderContext = new RenderContext();

    /**
     *
     */
    private class RenderContext implements MarkupRenderContext {
        private DocumentFragment fragment;
        private MarkupPosition begin;
        private MarkupPosition end;
        public DocumentFragment getDocumentFragment() {
            return fragment;
        }
        public MarkupPosition getBeginPosition() {
            return begin;
        }
        public MarkupPosition getEndPosition() {
            return end;
        }
        public void associateMouseRegion(Element element, MarkupMouseRegion region) {
//            if (element instanceof RaveElement) {
//                ((RaveElement)element).setMarkupMouseRegion(region);
//            }
            setMarkupMouseRegionForElement(element, region);
        }
    }

    
//    private static final Map element2region = new WeakHashMap(200);
//    private static final String KEY_MARKUP_MOUSE_REGION = "vwpMarkupMouseRegion"; // NOI18N
    
    private static final Map<Element, MarkupMouseRegion> element2region = new WeakHashMap<Element, MarkupMouseRegion>(200);
    
    public static void setMarkupMouseRegionForElement(Element element, MarkupMouseRegion region) {
//        synchronized (element2region) {
//            element2region.put(element, region);
//        }
        if (element == null) {
            return;
        }
//        element.setUserData(KEY_MARKUP_MOUSE_REGION, region, MarkupMouseRegionDataHandler.getDefault());
        element2region.put(element, region);
    }
    
    public static MarkupMouseRegion getMarkupMouseRegionForElement(Element element) {
//        synchronized (element2region) {
//            return (MarkupMouseRegion)element2region.get(element);
//        }
        if (element == null) {
            return null;
        }
//        return (MarkupMouseRegion)element.getUserData(KEY_MARKUP_MOUSE_REGION);
        return element2region.get(element);
    }
    
    
//    private static class MarkupMouseRegionDataHandler implements UserDataHandler {
//        private static final MarkupMouseRegionDataHandler INSTANCE = new MarkupMouseRegionDataHandler();
//        
//        public static MarkupMouseRegionDataHandler getDefault() {
//            return INSTANCE;
//        }
//        
//        public void handle(short operation, String key, Object data, Node src, Node dst) {
//        }
//    } // End of MarkupMouseRegionDataHandler.
    
    
    /**
     * Recursively annotateRender on any components found in the node tree (not counting the node
     * itself); e.g. only process its children.
     */
    private void annotateRenderTree(DesignBean parentBean, DocumentFragment df, Element element,
                                    int index) {
//        DesignBean lb = element.getDesignBean();
        DesignBean lb = MarkupUnit.getMarkupDesignBeanForElement(element);

        if (lb != null) {
            // Do it bottom up
            NodeList nl = element.getChildNodes();
            int n = nl.getLength();
            for (int i = 0; i < n; i++) {
                Node child = nl.item(i);
//                if (child instanceof RaveElement) {
//                    RaveElement ce = (RaveElement)child;
//                    annotateRenderTree(lb, df, ce, i);
//                }
                if (child instanceof Element) {
                    annotateRenderTree(lb, df, (Element)child, i);
                }
            }
        }
        
        if (lb != null && lb != parentBean) {
            annotateRender(lb, df, element.getParentNode(), index, index+1);
        }
    }

    /**
     * Write a source node's view representation into a doc fragment writer
     * @param node
     * @param beanmap
     * @param rw
     * @param lu
     */
    private void renderNode(Node node, Map beanmap, DocFragmentJspWriter rw, LiveUnit lu) {
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                return; // ignore

            case Node.ELEMENT_NODE:
                // if the node is an element that belongs to a facesbean, then render the bean
                DesignBean bkid = (DesignBean)beanmap.get(node);
                if (bkid != null) {
                    try {
                        renderBean(bkid, rw, lu);
                    }
                    catch (Exception e) {
                        Trace.trace("insync.faces", "error in child encode: resetting");
                        UIComponent kid = (UIComponent)bkid.getInstance();
                        renderError(e, bkid, rw, null);
                    }
                    beanmap.remove(node);
                    return;
                }

                // recurse when replicating elements in case we have a child burried under one
                rw.importNode(node, false);
                for (Node nkid = node.getFirstChild(); nkid != null; nkid = nkid.getNextSibling())
                    renderNode(nkid, beanmap, rw, lu);
                rw.popNode();
                return;

            // Text nodes need to be "rendered" from JSPX to HTML.
            // For example, let's say the JSPX is this: "Hello&amp;nbsp;World" -
            // e.g. "Hello World" where the space is a nonbreaking space.
            // The XML parser will already have processed the &amp; so the
            // text node we're given for this is "Hello&nbsp;World". Since
            // we're creating a text node directly we need to process the entities
            // ourselves (otherwise the text node would say "Hell&nbsp;World"
            // literally).  The utility method Entities.expand will find
            // and replace any of the 253 HTML entities in a string with their
            // corresponding characters, but only do this if we know that the
            // string contains an entity (e.g. there is a & in the string).
            case Node.TEXT_NODE: {
                String s = node.getNodeValue();
//                if (((RaveRenderNode)node).isJspx() && (s.indexOf('&') != -1)) {
                if (MarkupService.isJspxNode(node) && (s.indexOf('&') != -1)) {
                    // <markup_separation>
//                    s = MarkupServiceProvider.getDefault().expandHtmlEntities(s, false);
                    // ====
                    s = Entities.expandHtmlEntities(s, false);
                    // </markup_separation>
                }
                Node newnode = rw.appendTextNode(s);
                // Think of a better name than "jsp" and "html" here; I'm
                // realy dealing with rendered vs source
                if (newnode != null) {
//                    ((RaveText)newnode).setSource((RaveText)node);
                    MarkupService.setSourceTextForText((Text)newnode, (Text)node);
                }
                return;
            }
                
            // other text node type can be simply deep copied
            case Node.CDATA_SECTION_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.COMMENT_NODE: // because of <script> nodes and <style> nodes
                Node newnode = rw.importNode(node, true);
            	return;
        }
    }

    //--------------------------------------------------------------------------------------- Object

    /*
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(30);
        sb.append("[FacesPageUnit pkg:" + getThisPackageName() + " cls:" + getThisClassName()
                + " name:" + junit.getName());
        sb.append("]");
        return sb.toString();
    }
    
    public class RenderError extends Error {
        private RenderError(Exception cause) {
            super(cause);
        }
    }

// <copied from designer/FacesSupport>
    public static DocumentFragment renderHtml(FacesModel model, MarkupDesignBean bean) {
        return renderHtml(model, bean, true);
    }
    
    public static DocumentFragment renderHtml(FacesModel model, MarkupDesignBean bean, boolean markRendered) {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
            FacesModelSet facesModelSet = model.getFacesModelSet();
            if (facesModelSet == null) {
                // XXX Possible NPE, after the model was invalidated.
                return null;
            }
    		Thread.currentThread().setContextClassLoader(model.getFacesModelSet().getProjectClassLoader());
            if (bean == null) {
                // First discover where to start rendering...
                // Look from the topmost render
                DesignBean r = model.getRootBean();
    
                if (r == null) {
                    return null;
                }
    
                // XXX This isn't right. What if the user adds a property to the page
                // of type UIComponent? I've gotta get an insync API to get -the- view
                // hierarchy.  Should I just go looking for f:view ?
                for (int i = 0, n = r.getChildBeanCount(); i < n; i++) {
                    DesignBean b = (DesignBean)r.getChildBean(i);
    
                    if (b instanceof MarkupDesignBean) {
                        bean = (MarkupDesignBean)b;
    
                        // Look for a bean that has a deep hierarchy - so we skip
                        // things like f:loadBundle and such in case they occur
                        // at the top level below f:view
                        if ((bean.getChildBeanCount() > 0) &&
                                (bean.getChildBean(0).getChildBeanCount() > 0)) {
                            break; // has a grandchild - probably the main parent we're looking for.
                        }
                    }
                }
    
                if (bean == null) {
                    return null;
                }
            }
    
            FacesPageUnit facesunit = model.getFacesUnit();
            
            DocumentFragment df = facesunit.getFacesRenderTree(bean, model.getLiveUnit());
    
            if(DEBUG) {
                debugLog("Rendered bean=" + bean + "\n" + InSyncServiceProvider.get().getHtmlStream(df)); // NOI18N
            }
            
//        // TODO: Rather than check for the box persistence side-effect flag, should I
//        // be smarter here and only mark rendered nodes if the target is the DomSynchronizer's
//        // DOM?
        if (markRendered) {
//            markRenderedNodes(null, df);
                MarkupService.markRenderedNodes(df);
        }
    
            return df;
    	} finally {    		
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }
    
    /** Debugging flag. */
    private static final boolean DEBUG = ErrorManager.getDefault()
            .getInstance(FacesPageUnit.class.getName()).isLoggable(ErrorManager.INFORMATIONAL);
    
    /** Logs debug message. Use only after checking <code>DEBUG</code> flag. */
    private static void debugLog(String message) {
        ErrorManager.getDefault().getInstance(FacesPageUnit.class.getName()).log(message);
    }
    
//    /** Mark all nodes in a node tree as rendered HTML nodes, and point back to the
//     * source nodes in the JSP DOM.  For nodes that all point to the same source
//     * node I want only the topmost nodes to point to the source.
//     */
//    private static void markRenderedNodes(Element parent, Node node) {
//        RaveElement element;
//        if (node instanceof RaveElement) {
//            element = (RaveElement)node;
//        } else {
//            element = null;
//        }
//
//        // We work our way right to left, bottom to top, to ensure that
//        // the last setJsp call made for a particular jsp node will be the
//        // leftmost, topmost rendered node for that jsp element.
//        NodeList nl = node.getChildNodes();
//
//        for (int n = nl.getLength(), i = n - 1; i >= 0; i--) {
//            markRenderedNodes(element, nl.item(i));
//        }
//
//        if (node instanceof RenderNode) {
//            RenderNode rn = (RenderNode)node;
//
//            if (element != null) {
////                if ((parent != null) && (parent.getDesignBean() == element.getDesignBean())) {
//                if (parent != null
//                && MarkupUnit.getMarkupDesignBeanForElement(parent) == MarkupUnit.getMarkupDesignBeanForElement(element)) {
//                    element.setSource(null);
////                } else if (element.getDesignBean() != null) {
////                    element.setSource((RaveElement)element.getDesignBean().getElement());
//                } else if (MarkupUnit.getMarkupDesignBeanForElement(element) != null) {
//                    element.setSource((RaveElement)MarkupUnit.getMarkupDesignBeanForElement(element).getElement());
//                } else {
//                    rn.markRendered();
//                }
//            } else {
//                rn.markRendered();
//            }
//        }
//    }
// </copied from designer/FacesSupport>
    
    
}
