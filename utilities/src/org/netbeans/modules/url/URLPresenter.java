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

package org.netbeans.modules.url;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.openide.awt.Mnemonics;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Presenter which creates actual components on demand.
 *
 * @author  Ian Formanek
 * @author  Marian Petras
 */
final class URLPresenter implements Presenter.Menu,
                                    Presenter.Toolbar,
                                    Presenter.Popup,
                                    ActionListener {

    /** <code>URLDataObject</code> this presenter presents */
    private final URLDataObject dataObject;
    
    /**
     * Creates a new presenter for a specified <code>URLDataObject</code>.
     *
     * @param  dataObject  <code>URLDataObject</code> to represent
     */
    URLPresenter(URLDataObject dataObject) {
        this.dataObject = dataObject;
    }

    /* implements interface Presenter.Menu */
    public JMenuItem getMenuPresenter() {
        JMenuItem menuItem = new JMenuItem();
        initialize(menuItem, false);
        return menuItem;
    }

    /* implements interface Presenter.Popup */
    public JMenuItem getPopupPresenter() {
        JMenuItem menuItem = new JMenuItem();
        initialize(menuItem, false);
        return menuItem;
    }

    /* implements interface Presenter.Toolbar */
    public Component getToolbarPresenter() {
        JButton toolbarButton = new JButton();
        initialize(toolbarButton, true);
        return toolbarButton;
    }

    /**
     * Initializes a specified presenter.
     *
     * @param  presenter  presenter to initialize
     */
    private void initialize(AbstractButton presenter, boolean useIcons) {

        if (useIcons) {
            // set the presenter's icon:
            Image icon = Utilities.loadImage(
                    "org/netbeans/modules/url/urlObject.gif");              //NOI18N
            try {
                FileObject file = dataObject.getPrimaryFile();
                FileSystem.Status fsStatus = file.getFileSystem().getStatus();
                icon = fsStatus.annotateIcon(icon,
                                             BeanInfo.ICON_COLOR_16x16,
                                             dataObject.files());
            } catch (FileStateInvalidException fsie) {
                // OK, so we use the default icon
            }
            presenter.setIcon(new ImageIcon(icon));
        }

        /* set the presenter's text and ensure it is maintained up-to-date: */
        NameChangeListener listener = new NameChangeListener(presenter);
        presenter.addPropertyChangeListener(
                WeakListeners.propertyChange(listener, dataObject));
        updateName(presenter);
        /*
         * The above code works with the assumption that it is called
         * from the AWT event dispatching thread (it manipulates
         * the presenter's display name). The same applies to
         * NameChangeListener's method propertyChange(...).
         *
         * At least, both mentioned parts of code should be called from
         * the same thread since method updateText(...) is not thread-safe.
         */

        presenter.addActionListener(this);
        HelpCtx.setHelpIDString(presenter,
                                dataObject.getHelpCtx().getHelpID());
    }

    /**
     * Updates display text and tooltip of a specified presenter.
     *
     * @param  presenter  presenter whose name is to be updated
     */
    private void updateName(AbstractButton presenter) {
        String name = dataObject.getName();

        try {
            FileObject file = dataObject.getPrimaryFile();
            FileSystem.Status fsStatus = file.getFileSystem().getStatus();
            name = fsStatus.annotateName(name, dataObject.files());
        } catch (FileStateInvalidException fsie) {
            /* OK, so we use the default name */
        }

        Mnemonics.setLocalizedText(presenter, name);
    }

    /* implements interface ActionListener */
    /**
     * Performs operation <em>open</em> of the <code>DataObject</code>.
     */
    public void actionPerformed(ActionEvent evt) {
        Node.Cookie open = dataObject.getCookie(OpenCookie.class);
        if (open != null) {
            ((OpenCookie) open).open();
        }
    }

    /**
     */
    private class NameChangeListener implements PropertyChangeListener {

        /** */
        private final AbstractButton presenter;

        /**
         */
        NameChangeListener(AbstractButton presenter) {
            this.presenter = presenter;
        }

        /* Implements interface PropertyChangeListener. */
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                URLPresenter.this.updateName(presenter);
            }
        }

    }

}