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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableTopComponent;

/**
 * Formerly HtmlBrowser.BrowserComponent.
 */
class HtmlBrowserComponent extends CloneableTopComponent implements PropertyChangeListener {
    /** generated Serialized Version UID */
    static final long                   serialVersionUID = 2912844785502987960L;

    // variables .........................................................................................
    
    /** Delegating component */
    private HtmlBrowser browserComponent;
    

    // initialization ....................................................................................

    /**
    * Creates new html browser with toolbar and status line.
    */
    public HtmlBrowserComponent() {
        this (true, true);
    }

    /**
    * Creates new html browser with toolbar and status line.
    */
    public HtmlBrowserComponent(boolean toolbar, boolean statusLine) {
        this (null, toolbar, statusLine);
    }

    /**
    * Creates new html browser.
    */
    public HtmlBrowserComponent(HtmlBrowser.Factory fact, boolean toolbar, boolean statusLine) {
        setName (""); // NOI18N
        setLayout (new BorderLayout ());
        add (browserComponent = new HtmlBrowser (fact, toolbar, statusLine), "Center"); // NOI18N

        browserComponent.getBrowserImpl().addPropertyChangeListener (this);

        // Ensure closed browsers are not stored:
        if (browserComponent.getBrowserComponent() != null) {
            putClientProperty("InternalBrowser", Boolean.TRUE); // NOI18N
        }
        setToolTipText(NbBundle.getBundle(HtmlBrowser.class).getString("HINT_WebBrowser"));
    }
    
    public int getPersistenceType() {
        return PERSISTENCE_ONLY_OPENED;
    }
    
    public void propertyChange (PropertyChangeEvent e) {
        if (!HtmlBrowser.Impl.PROP_TITLE.equals (e.getPropertyName ())) return;
        String title = browserComponent.getBrowserImpl().getTitle ();
        if ((title == null) || (title.length () < 1)) return;
        HtmlBrowserComponent.this.setName (title);
        HtmlBrowserComponent.this.setDisplayName(title);
    }    
    
    /** always open this top component in our special mode, if
    * no mode for this component is specified yet */
    public void open() {
        // do not open this component if this is dummy browser
        if (browserComponent.getBrowserComponent() == null)
            return;
        
        // behave like superclass
        super.open();
    }
    
    /** Serializes browser component -> writes Replacer object which
    * holds browser content and look. */
    protected Object writeReplace ()
    throws java.io.ObjectStreamException {
        return new BrowserReplacer (this);
    }
     
    /* Deserialize this top component. Now it is here for backward compatibility
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal (in);
        setStatusLineVisible (in.readBoolean ());
        setToolbarVisible (in.readBoolean ());
        browserComponent.setURL ((URL) in.readObject ());
    }

    // TopComponent support ...................................................................

    protected CloneableTopComponent createClonedObject () {
        HtmlBrowserComponent bc = new HtmlBrowserComponent();  // PENDING: this should pass all three params to create the same browser
        bc.setURL (getDocumentURL ());
        return bc;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(HtmlBrowserComponent.class);
    }

    protected void componentActivated () {
        browserComponent.getBrowserImpl().getComponent ().requestFocusInWindow ();
        super.componentActivated ();
    }

    public java.awt.Image getIcon () {
        return new ImageIcon (HtmlBrowser.class.getResource ("/org/openide/resources/html/htmlView.gif")).getImage ();   // NOI18N
    }
    

    // public methods ....................................................................................

    /**
    * Sets new URL.
    *
    * @param str URL to show in this browser.
    */
    public void setURL (String str) {
        browserComponent.setURL (str);
    }

    /**
    * Sets new URL.
    *
    * @param url URL to show in this browser.
    */
    public void setURL (final URL url) {
        browserComponent.setURL (url);
    }

    /**
    * Gets current document url.
    */
    public final URL getDocumentURL () {
        return browserComponent.getDocumentURL ();
    }

    /**
    * Enables/disables Home button.
    */
    public final void setEnableHome (boolean b) {
        browserComponent.setEnableHome (b);
    }

    /**
    * Enables/disables location.
    */
    public final void setEnableLocation (boolean b) {
        browserComponent.setEnableLocation (b);
    }

    /**
    * Gets status line state.
    */
    public boolean isStatusLineVisible () {
        return browserComponent.isStatusLineVisible ();
    }

    /**
    * Shows/hides status line.
    */
    public void setStatusLineVisible (boolean v) {
        browserComponent.setStatusLineVisible (v);
    }

    /**
    * Gets status toolbar.
    */
    public boolean isToolbarVisible () {
        return browserComponent.isToolbarVisible ();
    }

    /**
    * Shows/hides toolbar.
    */
    public void setToolbarVisible (boolean v) {
        browserComponent.setToolbarVisible (v);
    }

    protected java.lang.String preferredID() {
        return "HtmlBrowserComponent"; //NOI18N
    }

public static final class BrowserReplacer implements java.io.Externalizable {
    
    /** serial version UID */
    static final long serialVersionUID = 5915713034827048413L;

    
    /** browser window to be serialized */
    private transient HtmlBrowserComponent bComp = null;
    transient boolean statLine;
    transient boolean toolbar;
    transient URL url;
    
    public BrowserReplacer () {
    }
    
    public BrowserReplacer (HtmlBrowserComponent comp) {
        bComp = comp;
    }
    

    /* Serialize this top component.
    * @param out the stream to serialize to
    */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        out.writeBoolean (bComp.isStatusLineVisible ());
        out.writeBoolean (bComp.isToolbarVisible ());
        out.writeObject (bComp.getDocumentURL ());
    }
     
    /* Deserialize this top component.
      * @param in the stream to deserialize from
      */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        statLine = in.readBoolean ();
        toolbar = in.readBoolean ();
        url = (URL) in.readObject ();
        
    }


    private Object readResolve ()
    throws java.io.ObjectStreamException {
        // return singleton instance
        try {
            if ("http".equals(url.getProtocol())    // NOI18N
            &&  InetAddress.getByName (url.getHost ()).equals (InetAddress.getLocalHost ())) {
                url.openStream ();
            }
        }
        // ignore exceptions thrown during our test of accessibility and restore browser
        catch (java.net.UnknownHostException exc) {}
        catch (java.lang.SecurityException exc) {}
        catch (java.lang.NullPointerException exc) {}
        
        catch (java.io.IOException exc) {
            // do not restore JSP/servlet pages - covers FileNotFoundException, ConnectException
            return null;
        }
        catch (java.lang.Exception exc) {
            // unknown exception - write log message & restore browser
            Logger.getLogger(HtmlBrowserComponent.class.getName()).log(Level.WARNING, null, exc);
        }
        
        bComp = new HtmlBrowserComponent(statLine, toolbar);
        bComp.setURL (url);
        return bComp;
    }

} // end of BrowserReplacer inner class

}
