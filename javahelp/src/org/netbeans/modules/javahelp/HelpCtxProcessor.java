/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javahelp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.xml.sax.*;

import org.openide.awt.Actions;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.actions.Presenter;

/** XML processor for help context links.
 * The associated instance makes it suitable for
 * inclusion in a menu or toolbar.
 * @author Jesse Glick
 */
public final class HelpCtxProcessor implements XMLDataObject.Processor, InstanceCookie.Of {
    
    /** the XML file being parsed
     */
    private XMLDataObject xml;
    
    /** the cached menu/toolbar presenter
     */
    private Presenter p;
    
    /** Bind to an XML file.
     * @param xml the file to parse
     */
    public void attachTo(XMLDataObject xml) {
        this.xml = xml;
        Installer.err.log("processing help context ref: " + xml.getPrimaryFile());
    }
    
    /** Get the class produced.
     * @throws IOException doesn't
     * @throws ClassNotFoundException doesn't
     * @return the presenter class
     */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return ShortcutPresenter.class;
    }
    
    /** Get the name of the class produced.
     * @return the name of the presenter class
     */
    public String instanceName() {
        return "org.netbeans.modules.javahelp.HelpCtxProcessor$ShortcutPresenter"; // NOI18N
    }
    
    /** Test if this instance is of a suitable type.
     * @param type some superclass
     * @return true if it can be assigned to the desired superclass
     */
    public boolean instanceOf(Class type) {
        return type == Presenter.Menu.class || type == Presenter.Toolbar.class;
    }
    
    /** Create the presenter.
     * @throws IOException doesn't
     * @throws ClassNotFoundException doesn't
     * @return the new presenter
     */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        if (p != null)
            return p;
        
        Installer.err.log("creating help context presenter from " + xml.getPrimaryFile());
        
        EntityResolver resolver = new EntityResolver() {
            public InputSource resolveEntity(String pubid, String sysid) {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        };
        
        HandlerBase handler = new HandlerBase() {
            public void startElement(String name, AttributeList amap) throws SAXException {
                if ("helpctx".equals(name)) { // NOI18N
                    String id = amap.getValue("id"); // NOI18N
                    String showmaster = amap.getValue("showmaster"); // NOI18N
                    if (id != null && !"".equals(id)) { // NOI18N
                        p = new ShortcutPresenter(xml, id, Boolean.valueOf(showmaster).booleanValue());
                    }
                }
            }
        };
        
        Parser parser = xml.createParser();
        parser.setEntityResolver(resolver);
        parser.setDocumentHandler(handler);
        
        try {
            parser.parse(new InputSource(xml.getPrimaryFile().getInputStream()));
        } catch (SAXException saxe) {
            IOException ioe = new IOException(saxe.toString());
            Installer.err.annotate(ioe, saxe);
            throw ioe;
        }
        
        return p;
    }
    
    /** The presenter to be shown in a menu, e.g.
     */
    private static final class ShortcutPresenter implements Presenter.Menu, Presenter.Toolbar, ActionListener {
        
        /** associated XML file representing it
         */
        private final XMLDataObject obj;
        
        /** the cached help context
         */
        private String helpID;
        
        /** cached flag to show the master help set
         */
        private boolean showmaster;
        
        /** Create a new presenter.
         * @param obj XML file describing it
         */
        public ShortcutPresenter(XMLDataObject obj, String helpID, boolean showmaster) {
            this.obj = obj;
            this.helpID = helpID;
            this.showmaster = showmaster;
        }
        
        /** Make a menu item.
         * @return a menu item which when selected
         * will show the help
         */
        public JMenuItem getMenuPresenter() {
            if (!obj.isValid()) {
                // #16364
                return new JMenuItem("dead"); // NOI18N
            }
            JMenuItem m = new JMenuItem();
            m.addActionListener(this);
            new NodeBridge(m);
            return m;
        }
        
        /** Get a toolbar presenter.
         * @return a button which when clicked will show
         * the help
         */
        public Component getToolbarPresenter() {
            if (!obj.isValid()) {
                // #16364
                return new JLabel("dead"); // NOI18N
            }
            JButton b = new JButton();
            b.addActionListener(this);
            new NodeBridge(b);
            return b;
        }
        
        /** Show the help.
         * @param actionEvent ignored
         */
        public void actionPerformed(ActionEvent actionEvent) {
            Installer.getHelp().showHelp(new HelpCtx(helpID), showmaster);
        }
        
        /** Bridge between the data node of the XML file
         * and the presenter component, which keeps the
         * display name and icon in synch.
         */
        private class NodeBridge extends NodeAdapter implements ChangeListener {
            
            /** associated menu item or button
             */
            private AbstractButton b;
            
            /** Create a new bridge,
             * @param b the menu item or button
             * @param n the node delegate of the XML file
             */
            public NodeBridge(AbstractButton b) {
                this.b = b;
                updateText();
                updateIcon();
                updateEnabled();
                obj.getNodeDelegate().addNodeListener(this);
                Installer.getHelp().addChangeListener(WeakListener.change(this, Installer.getHelp()));
            }
            
            /** Called when the node delegate changes somehow,
             * @param ev event indicating whether the change
             * was of display name, icon, or other
             */
            public void propertyChange(PropertyChangeEvent ev) {
                String prop = ev.getPropertyName();
                if (!obj.isValid()) return;
                if (prop == null || prop.equals(Node.PROP_NAME) || prop.equals(Node.PROP_DISPLAY_NAME)) {
                    updateText();
                }
                if (prop == null || prop.equals(Node.PROP_ICON)) {
                    updateIcon();
                }
            }
            
            /** Update the text of the button according to node's
             * display name. Handle mnemonics sanely.
             */
            private void updateText() {
                String text = obj.getNodeDelegate().getDisplayName();
                Actions.setMenuText(b, text, true);
                //b.setToolTipText (Actions.cutAmpersand(text));
            }
            
            /** Update the icon of the button according to the
             * node delegate.
             */
            private void updateIcon() {
                b.setIcon(new ImageIcon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
            }
            
            private void updateEnabled() {
                Boolean valid = Installer.getHelp().isValidID(helpID);
                if (valid != null) {
                    b.setEnabled(valid.booleanValue());
                }
                Installer.err.log("enabled: xml=" + obj.getPrimaryFile() + " id=" + helpID + " enabled=" + valid);
            }
            
            /** Help sets may have changed.
             * @param changeEvent ignore
             */
            public void stateChanged(ChangeEvent changeEvent) {
                updateEnabled();
            }
            
        }
        
    }
    
}

