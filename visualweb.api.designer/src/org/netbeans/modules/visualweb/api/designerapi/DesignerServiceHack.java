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
package org.netbeans.modules.visualweb.api.designerapi;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;


/**
 * <b><font color="red"><em>
 * <p>
 * XXX Important note: Avoid using this service (module).
 * It hides cyclic dependency issue, i.e. it is an architectural flaw.
 * If you depend on this module, it indicates the problem, and it needs to be solved other way!
 * </p>
 * <p>
 * (E.g. There will be created appropriate api from designer module directly for some cases.
 * The other cases should use different approach)
 * </p>
 * </em></font></b>
 * <p>
 * This interface exposes services the page designer can perform.
 * The designer will register an implementation of this interface
 * in the system file system, so using org.openide.util.Lookup on
 * DesignerService.class you'll get an instance you can call these
 * methods on.
 * </p>
 *
 * @todo CustomizerDisplayer has some batching methods that
 *   could well be integrated here.
 * @todo Add the "Add To Form" code here which dataconnectivity and others
 *   depend on
 *
 * @author Tor Norbye
 */
public abstract class DesignerServiceHack {
    private static DesignerServiceHack designer; // TODO Use weak reference?

    public DesignerServiceHack() {
    }


    /**
     * Return the currently showing designer's FileObject.
     * This will return the FileObject for the JSP or HTML file being edited,
     * not the corresponding Page Bean's Java file.
     * You can for example find out the current project by passing this
     * file to org.netbeans.api.project.FileOwnerQuery.
     * @return The current jsp/html file being edited, or null if there is no
     *  currently open AND showing designer.
     */
    public abstract FileObject getCurrentFile();

//    /**
//     * During a drag operation the designer needs access to the actual transferable
//     * being dragged. This isn't available via the DND apis - you can only get to the
//     * DataFlavor, which is not enough. Thus, for certain kinds of drops, like
//     * designtime component drops, drag initiators (like the Server Navigator, etc.)
//     * should register the transferable as soon as drag is initiated.
//     */
//    public abstract void registerTransferable(Transferable transferable);

    /** Return true iff we can drop the given transferable on the current
     * designer. If no current page is showing, this will return null regardless
     * of the given transferable. Note also that this method returning true
     * is not a <b>guarantee</b> that a transferable of the given flavor will
     * be accepted; the actual transferable needs to be investigated since
     * for example some container components refuse children except for certain
     * subtypes, and this can't be inferred from all DataFlavors.
     * <p>
     * Note that flavor may be null if you only want to check if there's
     * a currently showing canvas that could -potentially- receive a drop.
     * @param a flavor to check if can be dropped on the canvas. Can be null to
     *  check if there's a visible designer accepting drops.
     */
    public abstract boolean canDrop(DataFlavor flavor);

    /**
     * Drop the given Transferable somewhere on the form (it will use the
     * topmost form)
     * @param transferable The transferable to be dropped somewhere on the designer
     */
    public abstract void drop(Transferable transferable);

    /**
     * Compute a preview image of the requested size for the given DesignBean,
     * applying the given cssStyle string.
     * <b>cssStyleClasses: THIS HAS NOT YET BEEN IMPLEMENTED!</b>
     */
    public abstract Image getCssPreviewImage(String cssStyle, String[] cssStyleClasses,
        MarkupDesignBean bean, int width, int height);
    
    /**
     * Compute a preview image of the requested size for the given set
     * of CSS properties. The visual elements included in the preview depends
     * on the CSS properties present. For example, if the property map
     * includes only a background color, the preview will simply be a rectangle
     * showing that color. If the property map has a property related to
     * text (such as color, or font size, or even a text transform), some
     * sample text will be included, and so on.
     *
     * @param properties A map containing String keys and String values, where
     *    the keys represent CSS property names (these <b>must</b> be lower case)
     *    and the values represent values for the CSS properties, using valid
     *    CSS syntax. <b>NOTE</b>: Shorthand properties (see the CSS spec)
     *    should NOT be passed in as keys; instead you must pass in the individual
     *    CSS properties for the shorthand property. Use the
     *    {@link DesignContext.convertCssStyleToMap} method to split a Style
     *    string if necessary.
     * @param base A base url to resolve relative urls against. You typically pass
     *    in the URL of the stylesheet here
     * @param width The requested width of the preview image
     * @param height The requested height of the preview image
     * @todo Transfer the convertCssStyleToMap method into DesignerService instead!
     */
    public abstract Image getCssPreviewImage(Map<String, String> properties, URL base, int width, int height);

    /** Computes a preview image of the specified size for given <code>DataObject</code>.
     * @return the image or <code>null</code> if the specified DataObject is not a webform one. */
    public abstract Image getPageBoxPreviewImage(DataObject dobj, int width, int height);

//    /** For the given CSS property (for example, "background-repeat"),
//     * return the list of valid property names.
//     * NOTE: Some properties also allow other value types. For example,
//     * "background-position" has the following value names:
//     * "top", "center", "bottom", "left", "right". But it also accepts
//     * percentages and absolute numbers. Therefore, this method does not
//     * return all possible CSS values you could apply, but it does represent
//     * all valid IDENTIFIER values for the property.
//     * @param propertyName The Css property name you want to get the
//     *  set of identifier values. Must be all lower-case.
//     * @return An alphabetically sorted array of property value names, such
//     *  as "left" or "baseline".
//     *  The return value will be null for property names that are either
//     *  not recognized or for properties that do not support identifier
//     *  values.
//     */
//    public abstract String[] getCssIdentifiers(String propertyName);

    /**
     * Similar to {@link getCssIdentifiers}, but instead of just returning
     * the name of the value, it returns a Batik Value object. This has
     * many advantages. For example, if it's a RGBColorValue, you can easily
     * get the java.awt.Color to paint. This is handy when the String name
     * is "indigo" for example...
     * <p>
     * <b>NOTE</b>: As a temporary hack to avoid adding a Batik dependency
     * on this API I've just made the return type Object. Clients can
     * cast to org.apache.batik.css.engine.value.Value - but will probably
     * want to do even more specific casts anywhere, depending on
     * the value of Value.getCssValueType().
     */

    // Not used, so commented out for now because the implementation would
    // need to be modified to return the Objects in the alphabetized
    // identifier name order!
    //public abstract Object[] getCssIdentifierValues(String propertyName);

//    /**
//     * Get the length units available in this version of CSS:
//     * "px", "cm", ... etc.  It only returns length units, not
//     * for example audio-related units like "deg", "rad" and "kHz".
//     * @return An array of length unit names.
//     */
//    public abstract String[] getCssLengthUnits();

//    /**
//     * Return a list of list of all CSS properties (the property names)
//     * @return an array containing all the CSS property names
//     */
//    public abstract String[] getCssProperties();

//    /**
//     * Get the CSS Value for the given CSS property in the given
//     * DesignBean. The return value will be of type
//     * org.apache.batik.css.engine.value.Value. Clients must cast
//     * to that type (and must depend on the Batik module). This is
//     * not part of this interface since I don't want Batik to be a
//     * required portion of the interface, since modules like dataconnectivity
//     * and websvc shouldn't be exposed to it.
//     * @param bean The design bean for which you want to look up a CSS property
//     * @param property The name of the CSS property to look up. NOTE: It should
//     *   NOT be a shorthand property!
//     * @return A org.apache.batik.css.engine.value.Value object representing
//     *   the value, or null if it could not be computed.
//     */
//    public abstract Object getCssValue(MarkupDesignBean bean, String property);

//    /**
//     * Parse the given document, using the given ErrorHandler.
//     * @param document The document to be parsed
//     * @param handler The error handler which will be called when the
//     *   parse is proceeding. This needs to be an instance of
//     *   org.w3c.css.sac.ErrorHandler. I have not put that in the
//     *   interface because most clients of this API because I would
//     *   have to add the whole Batik module as a prerequisite for this
//     *   API and most designer clients do not need it. This method
//     *   will throw an IllegalArgumentException if you pass in any
//     *   other type of handler.
//     */
//    public abstract void parseCss(javax.swing.text.Document document, Object handler);

    /**
     * Converts a CSS inline style string into a Map of style elements
     *
     * @param cssStyle The CSS inline style string to convert
     * @return A Map containing the parsed CSS styles
     */
//    /**
//     * Converts a CSS inline style string into a Map of style elements
//     *
//     * @param cssStyle The CSS inline style string to convert
//     * @return A Map containing the parsed CSS styles
//     */
//    public abstract Map convertCssStyleToMap(DesignContext context, String cssStyle);
//
//    /**
//     * Converts a Map of CSS styles into an inline CSS style string
//     *
//     * @param cssStyleMap The Map of CSS styles to convert
//     * @return An inline CSS style string
//     */
//    public abstract String convertMapToCssStyle(DesignContext context, Map cssStyleMap);

//    /**
//     * Return an array of all known HTML tag names.
//     */
//    public abstract String[] getHtmlTags();

    /**
     * Notify the designer that the given CSS file has been edited. This should
     * be called for every edit and is intended to be a lightweight call.
     * (In particular, this should not only be called when the file becomes modified
     * since the designer is always reflecting the current buffer contents, not the
     * last saved version, so even when an already modified file is modified again
     * we need to be notified in order to update the designer view if ncessary
     * (e.g. if visible)
     */
    public abstract void notifyCssEdited(DataObject dobj);

//    /**
//     * Show the given line in a particular file.
//     *
//     * @param filename The full path to the file, or null. Exactly one of filename or fileObject
//     *            should be non null.
//     * @param fileObject The FileObject for the file or null. Exactly one of filename or fileObject
//     *            should be non null.
//     * @param lineno The line number
//     * @param openFirst Usually you'll want to pass false. When set to true, this will first open
//     *            the file, then request the given line number; this works around certain bugs for
//     *            some editor types like CSS files.
//     */
//    public abstract void show(String filename, FileObject fileObject, int lineno, int column,
//        boolean openFirst);

    /** Obtain a default instance of the DesignerService */
    public static DesignerServiceHack getDefault() {
        // The service has no state so doesn't need to be a singleton. Therefore,
        // I won't bother with synchronization since getDefault may be called a lot.
        if (designer == null) {
            // Add the import items to the menu
            Lookup l = Lookup.getDefault();
            designer = (DesignerServiceHack)l.lookup(DesignerServiceHack.class);
        }

        return designer;
    }

    /**
     * Compute table information for a given bean. NOTE: The bean
     * must be a visible bean in the designer.
     * The return value is an opaque object that clients should
     * not attempt to interpret; however it can be passed to multiple
     * other methods in this interface to compute specific information
     * about the table. This allows the expensive table computation
     * to be performed only once and then various pieces extracted from it
     * at low cost.
     */
    public abstract Object getTableInfo(MarkupDesignBean bean);

    /**
     * Return the number of rows in the given table.
     * @param tableInfo See {@link getTableInfo}
     * @return The number of actual rows shown in the table
     */
    public abstract int getRowCount(Object tableInfo);

    /**
     * Return the number of columns in the given table.
     * @param tableInfo See {@link getTableInfo}
     * @return The number of actual columns shown in the table
     */
    public abstract int getColumnCount(Object tableInfo);

    /**
     * Return the element for the cell at the given row or column
     * @param tableInfo See {@link getTableInfo}
     * @param row The row number (virtual, not in the model.)
     * @param col The column number (virtual, not in the model.)
     * @return The element for the given cell, or null if this
     *  cell is not a cell origin (e.g. it is a continued cell via
     *  a colspan or rowspan.)
     */
    public abstract Element getCellElement(Object tableInfo, int row, int column);

    /**
     * Return the bean for the cell at the given row or column
     * @param tableInfo See {@link getTableInfo}
     * @param row The row number (virtual, not in the model.)
     * @param col The column number (virtual, not in the model.)
     * @return The bean for the given cell, or null if this
     *  cell is not a cell origin (e.g. it is a continued cell via
     *  a colspan or rowspan.)
     */
    public abstract MarkupDesignBean getCellBean(Object tableInfo, int row, int column);

    /**
     * Return the rowspan for the cell at the given row or column
     * @param tableInfo See {@link getTableInfo}
     * @param row The row number (virtual, not in the model.)
     * @param col The column number (virtual, not in the model.)
     * @return The rowspan for the given cell.
     */
    public abstract int getRowSpan(Object tableInfo, int row, int column);

    /**
     * Return the colspan for the cell at the given row or column
     * @param tableInfo See {@link getTableInfo}
     * @param row The row number (virtual, not in the model.)
     * @param col The column number (virtual, not in the model.)
     * @return The colspan for the given cell.
     */
    public abstract int getColSpan(Object tableInfo, int row, int column);

//    /**
//     * Set the CSS property for the given bean to the given value.
//     * @param bean The bean to apply the CSS property value to
//     * @param property The CSS property name (for example "background-color". NOTE:
//     *   This should NOT be a CSS shorthand property such as "border" or "background" !
//     * @param The value to apply
//     */
//    public abstract void setCssProperty(MarkupDesignBean bean, String property, String value);
//
//    /**
//     * Clear the CSS property for the given bean.
//     * @param bean The bean to clear the CSS property value for
//     * @param property The CSS property name (for example "background-color". NOTE:
//     *   This should NOT be a CSS shorthand property such as "border" or "background" !
//     */
//    public abstract void removeCssProperty(MarkupDesignBean bean, String property);

//    /**
//     * <p>
//     * Resolve the given url (which can be relative, context relative or
//     * absolute) to an absolute file URL. For example, let's say you have
//     * a document "/tmp/foo.jsp" which references a stylesheet in "/tmp/css/bar.css"
//     * and in this stylesheet you have a url "baz.png".
//     * In this case the parameters to this method would have "base" pointing to the
//     * css file, the url string would be the png filename, and the document reference
//     * would point to the including jsp document.
//     * </p>
//     * The algorithm used by this method is as follows:
//     * <ul>
//     *   <li> If the url string represents its own URL (e.g. starts with a protocol)
//     *      then the URL returned is the resulting full URL. </li>
//     *   <li> Otherwise, if the url string does NOT start with "/", then a URL is
//     *      formed by appending it to the base URL passed in
//     *   <li> Otherwise, this is a context relative URL (because it begins with "/")
//     *      and the base URL is computed by finding the project associated with
//     *      the document parameter, and from the document the WEB root is located.
//     *      This is taken as the base and the URL is computed as above.
//     * </ul>
//     *
//     * @param base The URL of the referrer, which the url string will be taken
//     *   to be relative to, unless it is an "absolute" (context relative) string,
//     *   such as "/resources". In that case it will look up the project associated
//     *   with the given document and find its context root from there.
//     * @param document A document related (more distantly than the base) to the
//     *   url reference.
//     * @param url A string which represents a relative URL, or a context url, or
//     *   even a complete url on its own (http://www.sun.com/jscreator).
//     */
//    public abstract URL resolveUrl(URL base, Document document, String url);

//    /**
//     * Return the <body> element of the given source document, if any.
//     * @return A <body> element, or a <frameset>.
//     */
//    public abstract Element getBody(Document document);

//    /**
//     * Return true iff the given file object represents a webform primary file
//     * (e.g. jsp, etc.)
//     */
//    public abstract boolean isWebPage(FileObject fo);
//
//    /**
//     * Return a List of web pages in the project
//     * @param includePages Iff true, include non-fragment pages in the list
//     * @param includeFragments Iff true, include page fragments in the list
//     * @return A List containing FileObject entries for WebForms in the project
//     */
//    public abstract List getWebPages(Project project, boolean includePages, boolean includeFragments);
//
//    /**
//     * Return an array of String mime types for mime types considered to be webforms
//     * the designer will edit (and insync will provide models for etc.)
//     */
//    public abstract String[] getMimeTypes();

// Moved to insync service.
//    /**
//     * Return true iff the given document represents a Braveheart page. A braveheart
//     * page is one using Braveheart components
//     * @param document The document to be checked
//     * @return True iff the page is a braveheart page
//     */
//    public abstract boolean isBraveheartPage(Document document);
//
//    /**
//     * Return true iff the given FileObject represents a Braveheart page. A braveheart
//     * page is one using Braveheart components.
//     * <b>NOTE: This method only returns true for braveheart pages that are currently open
//     * in the designer.</b>
//     *
//     * @param fobj The FileObject to be checked
//     * @return True iff the page is a braveheart page
//     */
//    public abstract boolean isBraveheartPage(FileObject fobj);

    /**
     * Refresh a given webform, or all webforms in the project.
     * If dobj is not null, it should reference a webform DataObject which
     * will be refreshed. Otherwise, all webforms in the project will be
     * refreshed.
     * For things like picking up new stylesheets or themes, a normal
     * refresh will do. If you want the insync units to rebuild themselves
     * from source, set the deep parameter to true.
//     * @param project The project for which you want a full refresh. You cannot
//     *  pass null to imply all projects; you must call this method on each project.
     * @param dobj The DataObject of the WebForm to be refreshed, or null if you
     *  want all webforms in the project to be refreshed
     * @param deep If true, perform a full sync from source of the buffer rather
     *  than just discarding style and html render trees. This is normally overkill.
     * @todo Rename the deep parameter to full?
     */
//    public abstract void refresh(Project project, DataObject dobj, boolean deep);
//    public abstract void refreshDataObject(DataObject dobj, boolean deep);
//    public abstract void refreshProject(Project project, boolean deep);
    
//    /** Destroys webform for specified fileobject, if the webform exists. */
//    public abstract void destroyWebFormForFileObject(FileObject fo);
    

//    public abstract void detachTopComponentForDataObject(DataObject dobj);
                
//    public abstract MultiViewElement getMultiViewElementForDataObject(DataObject jsfJspDataObject);


    // Moved via designer/cssengine/CssBlockSizeProvider
//    /** Gets the width of the block which directly contains the
//     * given element. */
//    public abstract float getBlockWidth(Element element);
//    
//    /** Gets the height of the block which directly contains the
//     * given element. */
//    public abstract float getBlockHeight(Element element);
    
    
        /** XXX Do not use, this is only temporary, until the markup impl is moved into designer,
         or prepared better solution. */
    public abstract void copyBoxForElement(Element fromElement, Element toElement);
    
// <missing designtime api> see InSyncService
    
// <separation of models>
    public abstract FileObject getContextFileForFragmentFile(FileObject fragmentFile);
    public abstract FileObject getExternalFormFileForElement(Element element);
// </separation of models>
    
// </missing designtime api>
}
