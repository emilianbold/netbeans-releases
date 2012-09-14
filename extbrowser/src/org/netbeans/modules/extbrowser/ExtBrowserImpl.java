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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.extbrowser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.extbrowser.plugins.*;
import org.netbeans.modules.extbrowser.plugins.ExternalBrowserPlugin.BrowserTabDescriptor;
import org.netbeans.modules.extbrowser.plugins.chrome.WebKitDebuggingTransport;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * The ExtBrowserImpl is generalized external browser.
 *
 * @author Radim Kubacki
 */
public abstract class ExtBrowserImpl extends HtmlBrowser.Impl
    implements EnhancedBrowser 
{
    /** Lookup of this {@code HtmlBrowser.Impl}.  */
    private Lookup lookup;

    /** standart helper variable */
    protected PropertyChangeSupport pcs;

    /** requested URL */
    private URL url;
    protected String title = "";      // NOI18N

    /** reference to a factory to get settings */
    protected ExtWebBrowser extBrowserFactory;
    
    private BrowserTabDescriptor browserTabDescriptor = null;
    
    private boolean enhancedMode;
    
    private boolean disablePageInspector = false;
    private boolean liveHTMLEnabled = false;
    
    private boolean running = false;
    
    private Lookup projectContext;
    
    /** Default constructor. 
      * <p>Builds PropertyChangeSupport. 
      */
    public ExtBrowserImpl () {
        pcs = new PropertyChangeSupport (this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.browser.api.EnhancedBrowser#hasEnhancedMode()
     */
    @Override
    public boolean hasEnhancedMode() {
        return enhancedMode;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.browser.api.EnhancedBrowser#setEnhancedMode(boolean)
     */
    @Override
    public void setEnhancedMode( boolean mode ) {
        enhancedMode = mode;
    }
    
    @Override
    public void disablePageInspector() {
        this.disablePageInspector = true;
    }

    @Override
    public void enableLiveHTML() {
        this.liveHTMLEnabled = true;
    }
    
    public boolean isDisablePageInspector() {
        return disablePageInspector;
    }

    public boolean isLiveHTMLEnabled() {
        return liveHTMLEnabled;
    }
    
    private Lookup createLookup() {
        List<Lookup> lookups = new ArrayList<Lookup>();
        lookups.add(Lookups.fixed(
                    new MessageDispatcherImpl(),
                    new RemoteScriptExecutor(this),
                    new PageInspectionHandleImpl(this)
                ));
        if (extBrowserFactory.getBrowserFamilyId() == BrowserFamilyId.CHROME || 
                extBrowserFactory.getBrowserFamilyId() == BrowserFamilyId.CHROMIUM) {
            WebKitDebuggingTransport transport = new WebKitDebuggingTransport(this);
            lookups.add(Lookups.fixed(transport, Factory.createWebKitDebugging(transport)));
        }
        
        return new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
    }
    
    protected BrowserFamilyId getDefaultBrowserFamilyId(){
        return BrowserFamilyId.UNKNOWN;
    }
    
    
    /** Dummy implementations */
    @Override
    public boolean isBackward() { return false; }
    @Override
    public boolean isForward() { return false; }
    @Override
    public void backward() { }
    @Override
    public void forward() { }
    @Override
    public boolean isHistory() { return false; }
    @Override
    public void showHistory() {}
    @Override
    public void stopLoading() { }
    
    protected void setTitle (String title) {
        return;
    }
    
    @Override
    public String getTitle() {
        return "";
    }

    
    /** Returns status message representing status of html browser.
     *
     * @return status message.
     */
    @Override
    public String getStatusMessage() {
        return "";
    }
        
    /** Call setURL again to force reloading.
     * Browser must be set to reload document and do not cache them.
     */
    @Override
    public void reloadDocument() {
        if (url == null) {
            return;
        }
        if ( hasEnhancedMode() ){
            BrowserTabDescriptor tab = getBrowserTabDescriptor();
            if (tab != null) {
                ExternalBrowserPlugin.getInstance().showURLInTab(tab, url);
            }
            /*
             * Do nothing in case there is no possibility to reload browser in the opened tab.
             * Otherwise each call of this method opens new tab in the browser and 
             * this has no relation to "reload".
             * 
            }*/
        }
        else {
            setURL(url);
        }
    }
        
    
    /** Returns current URL.
     *
     * @return current URL.
     */
    @Override
    public URL getURL() {
        return url;
    }

    public void close(boolean closeTab) {
        if (hasEnhancedMode()) {
            BrowserTabDescriptor tab = getBrowserTabDescriptor();
            if (tab != null) {
                ExternalBrowserPlugin.getInstance().close(tab, closeTab);
            }
        }
    }

    @Override
    public void setProjectContext(Lookup projectContext) {
        this.projectContext = projectContext;
    }

    public Lookup getProjectContext() {
        return projectContext;
    }
    
    /** 
     *  Sets current URL. Descendants of this class will implement it and they can call this
     *  to display internal resources.
     *
     * @param url URL to show in the browser.
     */
    @Override
    final public void setURL(URL url) {
        if (hasEnhancedMode()) {
            BrowserTabDescriptor tab = getBrowserTabDescriptor();
            if (tab == null) {
                BrowserFamilyId pluginId = extBrowserFactory.getBrowserFamilyId();
                ExtensionManager.ExtensitionStatus status = ExtensionManager
                        .isInstalled(pluginId);
                boolean browserPluginAvailable = true;
                if (status == ExtensionManager.ExtensitionStatus.DISABLED) {
                    browserPluginAvailable = false;
                } else if (status == ExtensionManager.ExtensitionStatus.MISSING || 
                        status == ExtensionManager.ExtensitionStatus.NEEDS_UPGRADE) {
                    browserPluginAvailable = ExtensionManager.installExtension(pluginId,
                            new PluginLoader() {

                                @Override
                                public void requestPluginLoad( URL url ) {
                                    loadURLInBrowser(url);
                                }
                            }, status);
                }
                if (browserPluginAvailable) {
                    // instead of using real URL to open a new tab in the browser
                    // (and possible start browser process itself) I'm going to use
                    // a temp file which I will refresh with the real URL once the
                    // link between browser and IDE was established. The reason is
                    // that I would like to be able to set breakpoints to the browser
                    // tab before the URL is loaded so that breakpoints get hit
                    // even when the URL is loaded for the first time.
                    URL tempUrl = createBlankHTMLPage();
                    assert tempUrl != null;
                    ExternalBrowserPlugin.getInstance().register(tempUrl, url, this);
                    loadURLInBrowser(tempUrl);
                }
            }
            else {
                ExternalBrowserPlugin.getInstance().showURLInTab(tab, url);
            }
        }
        else {
            loadURLInBrowser(url);
        }
        this.url = url;
    }
    
    private URL createBlankHTMLPage() {
        try {
            File f = File.createTempFile("blank", ".html");
            FileWriter fw = new FileWriter(f);
            fw.write("<html :netbeans_temporary=\"true\"></html>");
            fw.close();
            return f.toURI().toURL();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    abstract protected void loadURLInBrowser(URL url);
    
    /** Returns visual component of html browser.
     *
     * @return visual component of html browser.
     */
    @Override
    public final java.awt.Component getComponent() {
        return null;
    }

    /** Adds PropertyChangeListener to this browser.
     *
     * @param l Listener to add.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /** Removes PropertyChangeListener from this browser.
     *
     * @param l Listener to remove.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    public final Lookup getLookup() {
        if (lookup == null) {
            lookup = createLookup();
        }
        return lookup;
    }

    public void wasClosed() {
        setBrowserTabDescriptor(null);
        url = null;
        pcs.firePropertyChange(HtmlBrowser.Impl.PROP_BROWSER_WAS_CLOSED, null, null);
    }

    public synchronized BrowserTabDescriptor getBrowserTabDescriptor() {
        return browserTabDescriptor;
    }

    public synchronized void setBrowserTabDescriptor(BrowserTabDescriptor browserTabDescriptor) {
        this.browserTabDescriptor = browserTabDescriptor;
    }

    public void urlHasChanged() {
        pcs.firePropertyChange(HtmlBrowser.Impl.PROP_URL, null, null);
    }

}
