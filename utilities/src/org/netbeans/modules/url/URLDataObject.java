/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.url;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.awt.Actions;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;


/** Data object that represents one bookmark, one .url file containing url.
 *
 * @author Ian Formanek
 * @see org.openide.Places.Folders#bookmarks
 */
public class URLDataObject extends MultiDataObject implements EditCookie, OpenCookie, URLNodeCookie, InstanceCookie {

    /** Name for url property. */
    private static final String PROP_URL = "url"; // NOI18N
    
    /** Generated serial version UID. */
    static final long serialVersionUID =6829522922370124627L;

    
    /** Constructor.
     * @param pf primary file object for this data object */
    public URLDataObject(final FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);

        getCookieSet().add(this);
    }
    

    // PENDING: it would be neat to have get/setURL methods 
    // but, there is a problem(at least at jdk1.3 for linux) with URL.equals (too much time consuming
    // in underlying native method).
    /** Gets <code>URL</code> string from uderlying .url file. Notifies user
     * if error occures.
     * @return <code>URL</code> string stored in the file or empty string if file is empty
     * or <code>null</code> if error occured. Even there are multiple lines of text in the
     *  file, only the first one is returned */
    String getURLString() {
        FileObject urlFile = getPrimaryFile();
        if(!urlFile.isValid())
            return null;
        
        String urlString = ""; // NOI18N
        InputStream is = null;
        
        try {
            urlString = new BufferedReader (new InputStreamReader(is = urlFile.getInputStream ())).readLine ();
        } catch (FileNotFoundException fne) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Message (
                    MessageFormat.format (
                        NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_FileNotFoundError"),
                        new Object[] { urlFile.getPackageNameExt (File.separatorChar, '.') }
                    ),
                    NotifyDescriptor.ERROR_MESSAGE
                )
            );
                    
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                fne.printStackTrace ();
                    
            return null;
        } catch (IOException ioe) {
            TopManager.getDefault().notify (
                new NotifyDescriptor.Message (
                    MessageFormat.format (
                        NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_IOError"),
                        new Object[] { urlFile.getPackageNameExt (File.separatorChar, '.'), ioe.getMessage () }
                    ),
                    NotifyDescriptor.ERROR_MESSAGE
                )
            );
                    
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ioe.printStackTrace ();
                    
            return null;
        } finally {
            if(is != null)
                try {
                    is.close ();
                } catch (IOException e) {
                }
        }
        
        if (urlString == null)
            // if the file is empty, return empty string, as null is reserved for notifying failure 
            urlString = ""; // NOI18N

        return urlString;
    }

    /** Stores specified String into the URL file.
     * @param newUrlString the URL String to be stored in the file. */
    void setURLString(String newUrlString) {
        FileObject urlFile = getPrimaryFile();
        if(!urlFile.isValid())
            return;
        
        FileLock lock = null;
        try {
            lock = urlFile.lock ();
            OutputStream os = urlFile.getOutputStream (lock);
            os.write (newUrlString.getBytes ());
            os.close ();
        } catch(IOException ioe) {
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ioe.printStackTrace ();
        } finally {
            if (lock != null)
                lock.releaseLock ();
        }
    }

    /** Help context for this object. Overrides superclass method.
     * @return help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (URLDataObject.class);
    }

    /** Creates node delagte for this data object.
     * @return <code>URLNode</code> representating this data object instance
     */
    protected Node createNodeDelegate () {
        return new URLNode(this);
    }


    /** Invokes the open action. Implements <code>OpenCookie</code> interface. */
    public void open() {
        String urlString = getURLString ();
        if(urlString == null) 
            return;

        URL url = getURLFromString(urlString);
        
        if(url == null)
            return;

        TopManager.getDefault().showUrl(url);
    }

    /** Implements <code>URLNodeCookie</code> interface.
     * @see URLNodeCookie */
    public void openInNewWindow () {
        String urlString = getURLString ();
        if(urlString == null)
            return;

        URL url = getURLFromString(urlString);
        
        if(url == null)
            return;

        // hack for finding default browser set in global IDE settings
        HtmlBrowser.Factory fact = null;
        try {
            FileObject fo = TopManager.getDefault ().getRepository ()
                .getDefaultFileSystem ().findResource ("Services/Browsers");   // NOI18N
            DataFolder folder = DataFolder.findFolder (fo);
            DataObject [] dobjs = folder.getChildren ();
            for (int i = 0; i<dobjs.length; i++) {
                Boolean flag = (Boolean)dobjs[i].getPrimaryFile ().getAttribute ("DEFAULT_BROWSER");    // NOI18N
                if ((flag != null) && flag.booleanValue ()) {
                    Object o = ((InstanceCookie)dobjs[i].getCookie (InstanceCookie.class)).instanceCreate ();
                    if (o instanceof HtmlBrowser.Factory)
                        fact = (HtmlBrowser.Factory)o;
                    break;
                }
            }
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) {
                // not a big problem: HtmlBrowser will create some browser
                ex.printStackTrace ();
            }
        }

        HtmlBrowser.BrowserComponent htmlViewer = new HtmlBrowser.BrowserComponent(fact, true, true);
        htmlViewer.setURL(url);
        htmlViewer.open();
        htmlViewer.requestFocus ();
    }

    /** Gets URL from string. Notifies user about error if it's not possible. Utility method.
     * @param urlString string from to construct <code>URL</code>
     * @return <code>URL</code> or null if it's not possible to construct from <code>urlString</code> */
    private static URL getURLFromString(String urlString) {
        URL url = null;
        
        try {
            url = new URL(urlString);
        } catch (MalformedURLException mue1) {
            try {
                // Try to prepend http protocol.
                url = new URL ("http://" + urlString); // NOI18N
            } catch (MalformedURLException mue2) {
                if (urlString.length () > 50) { // too long URL
                    TopManager.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            NbBundle.getBundle (URLDataObject.class).getString("MSG_MalformedURLError"),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                } else {
                    TopManager.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            MessageFormat.format (
                                NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_MalformedURLError"),
                                new Object[] { urlString }
                            ),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                }
            }
        }
        
        return url;
    }

    /** Implements <code>EditCookie</code> interface.
     * Instructs an editor to be opened. The operation can
     * return immediately and the editor be opened later.
     * There can be more than one editor open, so one of them is
     * arbitrarily chosen and opened. */
    public void edit() {
        String urlString = getURLString ();
        if (urlString == null) return;
        NotifyDescriptor.InputLine urlLine = new NotifyDescriptor.InputLine (
            NbBundle.getBundle (URLDataObject.class).getString("CTL_URL") ,
            NbBundle.getBundle (URLDataObject.class).getString("CTL_EditURL"));
        
        urlLine.setInputText(urlString);
        
        TopManager.getDefault ().notify (urlLine);
        if(urlLine.getValue () == NotifyDescriptor.OK_OPTION)
            setURLString (urlLine.getInputText ());
    }

    /** Gets name of instance. Implements <code>InstanceCookie</code> interface method. */
    public String instanceName () {
        return getName();
    }

    /** Gets class of instance. Implements <code>InstanceCookie</code> interface method. 
     * @return <code>URLPresenter</code> class
     * @see URLPresenter */
    public Class instanceClass () throws IOException, ClassNotFoundException {
        return URLPresenter.class;
    }

    /** Creates new instance. Implements <code>InstanceCookie</code> interface method. 
     * @return <code>URLPresenter</code> instance 
     * @see URLPresenter */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        return createURLPresenter();
    }
    
    /** Creates <code>URLPresenter</code> for this object. */
    URLPresenter createURLPresenter() {
        return new URLPresenter(getNodeDelegate());
    }
    

    /** Presenter which creates actual components on demand. */
    private static class URLPresenter extends Object implements Presenter.Menu, Presenter.Toolbar, Presenter.Popup {
        
        /** Node to present. */
        private Node n;
        
        /** Constructor. */        
        public URLPresenter (Node n) {
            this.n = n;
        }

        /** Implements <code>Presenter.Menu</code> interface. */
        public JMenuItem getMenuPresenter () {
            return new URLMenuItem(n);
        }
        
        /** Implements <code>Presenter.Popup</code> interface. */
        public JMenuItem getPopupPresenter () {
            return new URLMenuItem(n);
        }
        
        /** Implements <code>Presenter.Toolbar</code> interface. */
        public Component getToolbarPresenter () {
            return new URLToolbarButton(n);
        }
    } // End of URLPresenter nested class.

    
    /** Menu item representing the bookmark.
     * Takes display name and icon from associated node;
     * when invoked, opens the URL in the browser. */
    private static class URLMenuItem extends JMenuItem {
        public URLMenuItem(Node n) {
            new SimpleNodeButtonBridge(n, this);
        }
    } // End of URLMenuItem nested class.

    
    /** Toolbar button representing the bookmark. */
    private static class URLToolbarButton extends JButton {
        public URLToolbarButton(Node n) {
            new SimpleNodeButtonBridge(n, this);
        }
    } // End of URLToolbarButton nested class.

    
    /** Bridge which binds a URLNode to a menu item or toolbar button. */
    private static class SimpleNodeButtonBridge extends Object implements ActionListener, NodeListener {
        /** Node to bind with. */
        private final Node node;
        
        /** Abstract button to bind with. */
        private final AbstractButton button;

        
        /** Constructor. */
        public SimpleNodeButtonBridge (Node node, AbstractButton button) {
            this.node = node;
            this.button = button;
            
            updateText();
            updateIcon();
            
            HelpCtx.setHelpIDString(button, node.getHelpCtx().getHelpID());
            
            button.addActionListener(this);
            node.addNodeListener(WeakListener.node(this, node));
        }
        
        /** Implements <code>ActionListener</code> interface. Gets node's <code>OpenCookie</code>
         * and perfoms it if node does have it. */
        public void actionPerformed(ActionEvent ev) {
            OpenCookie open = (OpenCookie)node.getCookie (OpenCookie.class);
            if(open != null)
                open.open();
        }
        
        /** Dummy implementation of <code>NodeListener</code> method. */
        public void childrenAdded (NodeMemberEvent ev) {}
        /** Dummy implementation of <code>NodeListener</code> method. */
        public void childrenRemoved (NodeMemberEvent ev) {}
        /** Dummy implementation of <code>NodeListener</code> method. */
        public void childrenReordered (NodeReorderEvent ev) {}
        /** Dummy implementation of <code>NodeListener</code> method. */
        public void nodeDestroyed (NodeEvent ev) {}

        /** Implements <code>NodeListener</code> interface method.
         * Listens on node's name, display name and icon changes and updates
         * associated button accordingly. */
        public void propertyChange (PropertyChangeEvent ev) {
            String propName = ev.getPropertyName ();
            if(Node.PROP_DISPLAY_NAME.equals(propName)
                || Node.PROP_NAME.equals(propName)) {
                updateText();
            } else if(Node.PROP_ICON.equals(propName)) {
                updateIcon();
            }
        }
        
        /** Updates display text and tooltip of associated button. Node's
         * name or display name has changed. Helper method. */
        private void updateText() {
            String text = node.getName();
            
            Actions.setMenuText(button, text, true);
            button.setToolTipText(Actions.cutAmpersand(text));
        }
        
        /** Updates icon of associated button. Node's icon has changed. Helper method. */
        private void updateIcon() {
            button.setIcon(new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16)));
        }
        
    } // End of SimpleNodeButtonBridge nested class.


    /** <code>URL</code> node representing <code>URLDataObject</code>.
     * Leaf node, default action opens editor or instantiates template.
     * Icons redefined.
     */
    public static final class URLNode extends DataNode {

        /** Default constructor, constructs node */
        public URLNode (final DataObject dataObject) {
            super(dataObject, Children.LEAF);
            setIconBase("org/netbeans/modules/url/urlObject"); // NOI18N

            // Trick: To have a node name localized but remain the object name 
            // the same until renamed explictly. -> due to be able show
            // node names without ampersands for shortcuts and be able to show
            // shortcuts in bookmarks menu.
            // Note: see getDisplayName method bellow.
            setName(super.getDisplayName(), false);
        }


        /** Gets display name. Overrides superclass method. 
         * Cuts ampersand from the original display name. */
        public String getDisplayName() {
            return Actions.cutAmpersand(super.getDisplayName());
        }
        
        /** Gets sheet of properties. Overrides superclass method. */
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set sheetSet = sheet.get(Sheet.PROPERTIES);
            
            // Name property is replaced by so we could show the localized name
            // of node instead of the non-localized name of file on the disk 
            // which could differ at the start.
            sheetSet.remove(DataObject.PROP_NAME);
            sheetSet.put(createNameProperty());
                        
            sheetSet.put(createURLStringProperty());
            
            return sheet;
        }

        /** Creates a name property. */
        private Node.Property createNameProperty() {
            return new PropertySupport.ReadWrite (
                DataObject.PROP_NAME,
                String.class,
                NbBundle.getBundle(URLDataObject.class).getString("PROP_Name"),
                NbBundle.getBundle(URLDataObject.class).getString("PROP_NameShortDescription")) {

                public Object getValue () {
                  return URLNode.this.getName();
                }

                public void setValue (Object val) throws IllegalAccessException,
                  IllegalArgumentException, InvocationTargetException {
                  if (!canWrite())
                      throw new IllegalAccessException();
                  if (!(val instanceof String))
                      throw new IllegalArgumentException();

                  try {
                      getDataObject().rename ((String)val);
                  } catch (IOException ex) {
                      throw new InvocationTargetException (ex);
                  }
                }

                public boolean canWrite () {
                  return getDataObject().isRenameAllowed();
                }
            };
        }
        
        /** Creates property for editing.
         * @return property for URL String or null */
        private Node.Property createURLStringProperty() {
            Node.Property urlStringProperty = new PropertySupport.ReadWrite (
                URLDataObject.PROP_URL,
                String.class,
                NbBundle.getBundle(URLDataObject.class).getString("PROP_URLDisplayName"),
                NbBundle.getBundle(URLDataObject.class).getString("PROP_URLShortDescription")) {

                public Object getValue() {
                    return ((URLDataObject)getDataObject()).getURLString();
                }

                public void setValue(Object val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
                    if(!canWrite())
                        throw new IllegalAccessException();
                    if(!(val instanceof String))
                        throw new IllegalArgumentException();

                    ((URLDataObject)getDataObject()).setURLString((String)val);
                }
            };
            
            urlStringProperty.setPreferred(true);

            return urlStringProperty;
        }
        
    } // End of URLNode nested class.
    
}
