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

package org.netbeans.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.BeanNode;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Global IDE settings.
 *
 * @author Ian Formanek
 */
public class IDESettings  {    
    private static final IDESettings INSTANCE = new IDESettings();
    
    /** showToolTipsInIDE property name */
    public static final String PROP_SHOW_TOOLTIPS_IN_IDE = "showToolTipsInIDE"; // NOI18N
    /** confirmDelete property name */
    public static final String PROP_CONFIRM_DELETE = "confirmDelete"; // NOI18N
    /** home page property name */
    public static final String PROP_HOME_PAGE = "homePage"; // NOI18N
    /** show file extensions property name */
    public static final String PROP_SHOW_FILE_EXTENSIONS = "showFileExtensions"; // NOI18N
    /** Web Browser prefered by user */
    public static final String PROP_WWWBROWSER = "WWWBrowser"; // NOI18N
    
    /** files that should be ignored
     *
     * DO NOT CHANGE THIS PROPERTY NAME without checking that
     * this property name was changed also in GlobalVisibilityQueryImpl
     * in module org.netbeans.modules.masterfs.
     *
     */
    public static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N    
    
   
    // ------------------------------------------
    // properties
            
    public static IDESettings getInstance() {
        return INSTANCE;
    }
    
    static Preferences getPreferences() {
        return NbPreferences.forModule(IDESettings.class);
    } 
    
    // ------------------------------------------
    // property access methods
    
    /** A utility method to avoid unnecessary creation of second URL */
    public static URL getRealHomeURL () {
        try {
            return new URL (NbBundle.getMessage (IDESettings.class, "URL_default_home_page"));
        } catch (MalformedURLException e) {
            throw new AssertionError (e);
        }
    }
            
    /** Getter for ShowToolTipsInIDE
     * @return true if dialog will be shown*/
    public boolean getShowToolTipsInIDE () {
        return getPreferences().getBoolean(PROP_SHOW_TOOLTIPS_IN_IDE, true);
    }
    
    /** Setter for ShowToolTipsInIDE
     * @param value true if on the next start of corona the dialog will be shown
     *              false otherwise */
    public void setShowToolTipsInIDE (boolean value) {
        getPreferences().putBoolean(PROP_SHOW_TOOLTIPS_IN_IDE, value);
    }
    
    /** Getter for ConfirmDelete
     * @param true if the user should asked for confirmation of object delete, false otherwise */
    public boolean getConfirmDelete () {
        return getPreferences().getBoolean(PROP_CONFIRM_DELETE, true);
    }
    
    /** Setter for ConfirmDelete
     * @param value if true the user is asked for confirmation of object delete, not if false */
    public void setConfirmDelete (boolean value) {
        getPreferences().putBoolean(PROP_CONFIRM_DELETE, value);
    }
    
    /** This method must be overriden. It returns display name of this options.
     */
    public String displayName () {
        return NbBundle.getBundle (IDESettings.class).getString ("CTL_IDESettings");
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (IDESettings.class);
    }
    
    /** Getter for home page used in html viewer.
     */
    public String getHomePage () {
        return HtmlBrowser.getHomePage ();
    }
    
    /** Setter for home page used in html viewer.
     */
    public void setHomePage (String homePage) {
        HtmlBrowser.setHomePage (homePage);
    }
    
    /** Getter for showing file extensions.
     * @return whether to show them
     */
    public boolean getShowFileExtensions () {
        return DataNode.getShowFileExtensions ();
    }
    
    /** Setter for showing file extensions.
     * @param s whether to show them
     */
    public void setShowFileExtensions (boolean s) {
        boolean old = getShowFileExtensions ();
        DataNode.setShowFileExtensions (s);
    }
    
    /** Getter for preffered web browser.
     *
     * First time when this function is called Lookup is used
     * to find prefered browser factory in a browser registry.
     *
     * @return prefered browser,
     * may return null if it is not possible to get the browser
     */
    public static HtmlBrowser.Factory getWWWBrowser () {
        try {
            Object obj = getPreferences().get(PROP_WWWBROWSER, null);
            
            if (obj instanceof String && !"".equals (obj)) {
                // use new style
                Lookup.Item<HtmlBrowser.Factory> item = Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, (String)obj, null));
                return item == null ? null : item.getInstance ();
            }
            
            // the browser is not set yet - find the first one
            if (obj == null || "".equals (obj)) {
                Lookup.Result<HtmlBrowser.Factory> res = Lookup.getDefault ().lookupResult (HtmlBrowser.Factory.class);
                java.util.Iterator<? extends HtmlBrowser.Factory> it = res.allInstances ().iterator ();
                while (it.hasNext ()) {
                    HtmlBrowser.Factory brow = it.next ();
                    
                    // check if it is not set to be hidden
                    FileObject fo = Repository.getDefault ()
                    .getDefaultFileSystem ().findResource ("Services/Browsers");   // NOI18N
                    
                    DataFolder folder = DataFolder.findFolder (fo);
                    DataObject [] dobjs = folder.getChildren ();
                    for (int i = 0; i < dobjs.length; i++) {
                        Object o = null;
                        
                        try {
                            if (Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute ("hidden")))
                                continue;
                            InstanceCookie cookie = (InstanceCookie) dobjs[i].getCookie (InstanceCookie.class);
                            
                            if (cookie == null)
                                continue;
                            o = cookie.instanceCreate ();
                            if (o != null && o.equals (brow)) {
                                return brow;
                            }
                        }
                        // exceptions are thrown if module is uninstalled
                        catch (java.io.IOException ex) {
                            Logger.getLogger (IDESettings.class.getName ()).log (Level.WARNING, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger (IDESettings.class.getName ()).log (Level.WARNING, null, ex);
                        }
                    }
                    
                }
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace (ex);
        }
        return null;
    }
    
    /** Setter for preffered browser.
     *
     *  Actually Node.Handle of node that represent browser in lookup folder is stored.
     *
     * @param brow prefered browser capable of providing implementation
     */
    public static void setWWWBrowser (HtmlBrowser.Factory brow) {
        try {
            if (brow == null) {
                getPreferences().put(PROP_WWWBROWSER, "");//NOI18N
                return;
            }
            
            Lookup.Item<HtmlBrowser.Factory> item =
                    Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, null, brow));
            if (item != null) {
                getPreferences().put(PROP_WWWBROWSER, item.getId ());
            } else {
                // strange
                Logger.getLogger (IDESettings.class.getName ()).warning ("IDESettings: Cannot find browser in lookup");// NOI18N
                getPreferences().put(PROP_WWWBROWSER, "");//NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace (ex);
        }
    }
    
    // PRIVATE METHODS
    
    public String getIgnoredFiles () {
        return getPreferences().get(PROP_IGNORED_FILES, "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|~$|^\\..*$"); //NOI18N
    }
    
    public void setIgnoredFiles(String ignoredFiles) throws IllegalArgumentException {
        try {
            Pattern.compile(ignoredFiles);
            getPreferences().put(PROP_IGNORED_FILES, ignoredFiles); //NOI18N
        } catch (PatternSyntaxException e) {
            IllegalArgumentException iae = new IllegalArgumentException();
            iae.initCause( e );
            UIExceptions.annotateUser(iae, e.getMessage(),
                    e.getLocalizedMessage(), null, null);
            throw iae;
        }
    }    

    private static BeanNode createViewNode() throws java.beans.IntrospectionException {
        return new BeanNode(IDESettings.getInstance());
    }         

    private static org.netbeans.beaninfo.editors.HtmlBrowser.FactoryEditor createHtmlBrowserFactoryEditor()  {
        return new org.netbeans.beaninfo.editors.HtmlBrowser.FactoryEditor(){
            public void setValue(Object value) {
                setWWWBrowser((HtmlBrowser.Factory)value);
            }
            
            public Object getValue() {
                return getWWWBrowser();
            }            
        };
    }
}
