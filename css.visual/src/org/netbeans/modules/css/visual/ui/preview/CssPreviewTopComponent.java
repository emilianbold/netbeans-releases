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
package org.netbeans.modules.css.visual.ui.preview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Utilities;

/**
 * Css preview top component.
 *
 * @auhor Marek Fukala
 */
public final class CssPreviewTopComponent extends TopComponent {
    
    private static CssPreviewTopComponent instance;
    
    private static final Logger LOGGER = Logger.getLogger(org.netbeans.modules.css.Utilities.VISUAL_EDITOR_LOGGER);
    
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/css/resources/css.gif";//NOI18N
    
    private static final String PREFERRED_ID = "CssPreviewTC";//NOI18N
    
    CssPreviewPanel previewPanel = new CssPreviewPanel();
    
    private CssPreviewable lastSelectedPreviewable;
    
    private PropertyChangeListener WINDOW_REGISTRY_LISTENER = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                checkPreview((TopComponent)evt.getNewValue());
            }
        }
    };
    
    private CssPreviewable.Listener PREVIEWABLE_LISTENER = new CssPreviewable.Listener() {
        public void activate(final CssRuleContext content) {
            LOGGER.log(Level.FINE, "Previewable activated - POSTING activate task " + content);//NOI18N
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    preview(content);
                    LOGGER.log(Level.FINE, "Previewable activated - " + content);//NOI18N
                }
            });
        }
        
        public void deactivate() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showNoSelectedRulePanel();
                    LOGGER.log(Level.FINE, "Preview deactivated");//NOI18N
                }
            });
            
        }
    };
    
    private static final String DEFAULT_TC_NAME = NbBundle.getMessage(CssPreviewTopComponent.class, "CTL_CssPreviewTopComponent");
    
    private JPanel NO_PREVIEW_PANEL;
    private boolean previewing;
    
    private CssPreviewTopComponent() {
        initComponents();
        setToolTipText(NbBundle.getMessage(CssPreviewTopComponent.class, "HINT_CssPreviewTopComponent"));//NOI18N
        setIcon(Utilities.loadImage(ICON_PATH, true));
        
        NO_PREVIEW_PANEL = makeMsgPanel(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/preview/Bundle").getString("No_Preview"));
        
        add(NO_PREVIEW_PANEL, BorderLayout.CENTER);
        previewing = false;
    }
    
    private void checkPreview(TopComponent tc) {
        if(tc != null) {
            Node[] activatedNodes = tc.getActivatedNodes();
            if(activatedNodes != null) {
                for(Node n : activatedNodes) {
                    CssPreviewable previewable = n.getCookie(CssPreviewable.class);
                    if(previewable != null) {
                        LOGGER.log(Level.FINE, "Previewable selected " + previewable);//NOI18N
                        previewableSelected(previewable);
                        break; //use the first selected previewable
                    }
                }
            }
        }
    }

    
    private JPanel makeMsgPanel(String message) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BorderLayout());
        JLabel msgLabel = new JLabel(message);
        p.add(msgLabel, BorderLayout.CENTER);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return p;
    }
    
    //run in AWT!
    private void showNoSelectedRulePanel() {
        if(previewing) {
            setName(DEFAULT_TC_NAME); //set default TC name
            LOGGER.log(Level.FINE, "Previewable deactivated");//NOI18N
            remove(previewPanel);
            add(NO_PREVIEW_PANEL, BorderLayout.CENTER);
            previewing = false;
            revalidate();
            repaint();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized CssPreviewTopComponent getDefault() {
        if (instance == null) {
            instance = new CssPreviewTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the CssPreviewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized CssPreviewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CssPreviewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); //NOI18N
            return getDefault();
        }
        if (win instanceof CssPreviewTopComponent) {
            return (CssPreviewTopComponent)win;
        }
        Logger.getLogger(CssPreviewTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");//NOI18N
        return getDefault();
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private String getTitle(CssRuleContext content) {
        if(content != null && content.selectedRule() != null) {
            return content.selectedRule().name() + " - " + DEFAULT_TC_NAME; //NOI18N
        }
        return DEFAULT_TC_NAME;
    }
    
    private void preview(CssRuleContext content) {
        //set window title according to the selected rule
        setName(getTitle(content));
        
        //show the preview panel if hasn't been shown before
        if(!previewing) {
            remove(NO_PREVIEW_PANEL);
            add(previewPanel, BorderLayout.CENTER);
            previewing = true;
        }
        
        CharSequence htmlCode = CssPreviewGenerator.getPreviewCode(content);
//        System.out.println("\n---------------------" + htmlCode + "\n---------------------");
        try {
            String relativeURL = null;
            FileObject source = content.fileObject();
            if(source != null) {
                if(!source.isFolder()) {
                    source = source.getParent();
                }
                relativeURL = source.getURL().toExternalForm();
            }
            setPreviewContent(htmlCode, relativeURL);
        }catch(Exception e) {
            //an error - show message into the preview
            //TODO change this ugly thing
            try {
                String errorMessage = "<html> <body> <div style=\"color: red\">" + e.getMessage() + "</div> </body> </html>"; //NOI18N
                setPreviewContent(errorMessage, null);
            }catch(Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        revalidate();
        repaint();
    }
    
    private void setPreviewContent(CharSequence content, String relativeURL) throws Exception {
        LOGGER.log(Level.FINE, "preview - setting content " + content);//NOI18N
        previewPanel.panel().setDocument(new ByteArrayInputStream(content.toString().getBytes()), relativeURL);
    }
    
    private void previewableSelected(final CssPreviewable previewable) {
        if(lastSelectedPreviewable != null) {
            if(lastSelectedPreviewable.equals(previewable)) {
                return ; //ignore
            } else {
                LOGGER.log(Level.FINE, "removed listener from " + lastSelectedPreviewable);//NOI18N
                lastSelectedPreviewable.removeListener(PREVIEWABLE_LISTENER);
            }
        }
        lastSelectedPreviewable = previewable;
        LOGGER.log(Level.FINE, "added listener to " + previewable);//NOI18N
        lastSelectedPreviewable.addListener(PREVIEWABLE_LISTENER);
        
        //preview the content is available
        if(previewable.content() != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    preview(previewable.content());
                }
            });
        } else {
            //no content to preview
            showNoSelectedRulePanel();
        }
        
    }
    
    @Override
    public void componentOpened() {
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(WINDOW_REGISTRY_LISTENER);
        checkPreview(org.openide.windows.WindowManager.getDefault().getRegistry().getActivated());

    }
    
    @Override
    protected void componentActivated() {
        super.componentActivated();
    }
    
    @Override
    public void componentClosed() {
        WindowManager.getDefault().getRegistry().removePropertyChangeListener(WINDOW_REGISTRY_LISTENER);
    }
    
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return CssPreviewTopComponent.getDefault();
        }
    }
    
}
