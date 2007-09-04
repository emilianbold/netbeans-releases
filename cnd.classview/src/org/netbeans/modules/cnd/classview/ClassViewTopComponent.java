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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.classview.resources.I18n;
import java.awt.*;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.classview.actions.ShowHideClassViewAction;
import org.openide.ErrorManager;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Vladimir Kvashin
 */
public class ClassViewTopComponent extends TopComponent implements CsmModelListener {

    static final long serialVersionUID = 420172427347975689L;

    private static final String PREFERRED_ID = "classview"; //NOI18N

    public static transient ClassViewTopComponent DEFAULT;

    private transient ClassView view;
    
    public ClassViewTopComponent() {
        //if( Diagnostic.DEBUG ) Diagnostic.traceStack("ClassViewTopComponent .ctor #" + (++cnt));
    }

    /** Return preferred ID */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    // VK: code is copied from org.netbeans.modules.favorites.Tab class
    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized ClassViewTopComponent findDefault() {
        if (DEFAULT == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID); // NOI18N
            //if( ! (tc instanceof ClassViewTopComponent) ) {
            if (DEFAULT == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Cannot find ClassView component. It will not be located properly in the window system."); // NOI18N
//                DEFAULT = new ClassViewTopComponent();
//                // XXX Look into getDefault method.
//                DEFAULT.scheduleValidation();
                getDefault();
            }
        }

        return DEFAULT;
    }

    /** Gets default instance. Don't use directly, it reserved for deserialization routines only,
     * e.g. '.settings' file in xml layer, otherwise you can get non-deserialized instance. */
    public static synchronized ClassViewTopComponent getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ClassViewTopComponent();
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            //$ DEFAULT.scheduleValidation();
        }

        return DEFAULT;
    }

    public Object readResolve() throws java.io.ObjectStreamException {
        //return getDefault();
        if (DEFAULT == null) {
            DEFAULT = this;
            //$ DEFAULT.scheduleValidation();
        }
        return this;
    }

    /** Overriden to explicitely set persistence type of ProjectsTab
     * to PERSISTENCE_ALWAYS */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public static final String ICON_PATH = "org/netbeans/modules/cnd/classview/resources/class_view.png"; // NOI18N

    @Override
    protected void componentOpened() {
        if (view == null) {
            view = new ClassView();
            setLayout(new BorderLayout());
            setToolTipText(I18n.getMessage("ClassViewTitle")); // NOI18N
            setName(I18n.getMessage("ClassViewTooltip")); // NOI18N
            setIcon(Utilities.loadImage(ICON_PATH));
        }
        view.startup();
        addRemoveModelListeners(true);
        if (CsmModelAccessor.getModel().projects().isEmpty()) {
            add(createEmptyContent(), BorderLayout.CENTER);
        } else {
            add(view, BorderLayout.CENTER);
        }
    }

    private boolean isAutoMode = false;
    public void closeImplicit(){
        isAutoMode = true;
        close();
    }
    
    public void selectInClasses(CsmOffsetableDeclaration decl){
        if (view != null) {
            view.selectInClasses(decl);
        }
    }
    
    @Override
    protected void componentClosed() {
        if (!isAutoMode) {
            Preferences ps = NbPreferences.forModule(ShowHideClassViewAction.class);
            ps.putBoolean("ClassViewWasOpened", true);
        }
        isAutoMode = false;
        addRemoveModelListeners(false);
        if (view != null) {
            // paranoia
            view.shutdown();
            // clearing of view doesn't work. Opening component do not see mouse actions.
            //view = null;
        }
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        view.requestFocus();
    }

    private void addRemoveModelListeners(boolean add) {
        if (add) {
            if (Diagnostic.DEBUG) {
                Diagnostic.trace(">>> adding model listeners"); // NOI18N
            }
            CsmModelAccessor.getModel().addModelListener(this);
        } else {
            if (Diagnostic.DEBUG) {
                Diagnostic.trace(">>> removing model listeners"); // NOI18N
            }
            CsmModelAccessor.getModel().removeModelListener(this);
        }
    }

    public void projectOpened(CsmProject project) {
        if (view != null) {
            view.projectOpened(project);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    removeAll();
                    add(view, BorderLayout.CENTER);
                    validate();
                }
            });
        }
    }

    public void projectClosed(CsmProject project) {
        if (view != null) {
            view.projectClosed(project);
        }
        if (CsmModelAccessor.getModel().projects().isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    removeAll();
                    add(createEmptyContent(), BorderLayout.CENTER);
                    validate();
                }
            });
        }
    }

    private JComponent createEmptyContent() {
        JButton res = new JButton(I18n.getMessage("NoProjectOpen")); // NOI18N
        res.setEnabled(false);
        res.setBorder(BorderFactory.createEmptyBorder());
        res.setBackground(new JTextArea().getBackground());
        return res;
    }

    public void modelChanged(CsmChangeEvent e) {
        if (view != null) {
            view.modelChanged(e);
        }
    }
}