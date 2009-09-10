/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.css.visual.ui.preview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.core.browser.api.EmbeddedBrowserFactory;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Css preview top component.
 *
 * @auhor Marek Fukala
 */
public final class CssPreviewTopComponent extends TopComponent {
    
    private static CssPreviewTopComponent instance;
    
    private static final Logger LOGGER = Logger.getLogger(org.netbeans.modules.css.Utilities.VISUAL_EDITOR_LOGGER);
    
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/css/resources/style_sheet_16.png";//NOI18N
    
    private static final String PREFERRED_ID = "CssPreviewTC";//NOI18N
    
    private CssPreviewComponent previewPanel;
    
    private CssPreviewable lastSelectedPreviewable;
    
//    private PropertyChangeListener WINDOW_REGISTRY_LISTENER = new PropertyChangeListener() {
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
//                checkPreview((TopComponent)evt.getNewValue());
//            }
//        }
//    };
    
//    private CssPreviewable.Listener PREVIEWABLE_LISTENER = new CssPreviewable.Listener() {
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
                    setNoSelectedRule();
                    LOGGER.log(Level.FINE, "Preview deactivated");//NOI18N
                }
            });
            
        }
//    };
    
    private static final String DEFAULT_TC_NAME = NbBundle.getMessage(CssPreviewTopComponent.class, "CTL_CssPreviewTopComponent");
    
    private JPanel NO_PREVIEW_PANEL, PREVIEW_ERROR_PANEL;
    private boolean previewing, error;
    
    private SAXParser parser = null;
    
    private CssPreviewTopComponent() {
        initComponents();
        setToolTipText(NbBundle.getMessage(CssPreviewTopComponent.class, "HINT_CssPreviewTopComponent")); //NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        if (EmbeddedBrowserFactory.getDefault().isEnabled()) {
            previewPanel = new CssWebPreviewPanel();
        } else {
            previewPanel = new CssPreviewPanel();
        }

        EmbeddedBrowserFactory.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                previewPanel.dispose();
                remove(previewPanel.getComponent());
                if (EmbeddedBrowserFactory.getDefault().isEnabled()) {
                    previewPanel = new CssWebPreviewPanel();
                } else {
                    previewPanel = new CssPreviewPanel();
                }
                add(previewPanel.getComponent(), BorderLayout.CENTER);
                revalidate();
                repaint();
            }
        });

        NO_PREVIEW_PANEL = makeMsgPanel(NbBundle.getBundle("org/netbeans/modules/css/visual/ui/preview/Bundle").getString("No_Preview"));
        add(NO_PREVIEW_PANEL, BorderLayout.CENTER);
        previewing = false;
        error = false;

        PREVIEW_ERROR_PANEL = makeMsgPanel(NbBundle.getBundle("org/netbeans/modules/css/visual/ui/preview/Bundle").getString("Preview_Error"));

        setName(DEFAULT_TC_NAME); //set default TC name
        
        //init SAX parser
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
//    private void checkPreview(TopComponent tc) {
//        if(tc != null) {
//            Node[] activatedNodes = tc.getActivatedNodes();
//            if(activatedNodes != null) {
//                for(Node n : activatedNodes) {
//                    CssPreviewable previewable = n.getCookie(CssPreviewable.class);
//                    if(previewable != null) {
//                        LOGGER.log(Level.FINE, "Previewable selected " + previewable);//NOI18N
//                        previewableSelected(previewable);
//                        break; //use the first selected previewable
//                    }
//                }
//            }
//        }
//    }

    
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
    private void setNoSelectedRule() {
        if(previewing || error) {
            setName(DEFAULT_TC_NAME); //set default TC name
            LOGGER.log(Level.FINE, "Previewable deactivated");//NOI18N
            removeAll();
            add(NO_PREVIEW_PANEL, BorderLayout.CENTER);
            previewing = false;
            error = false;
            
            revalidate();
            repaint();
        }
    }
    
    //run in AWT!
    void setError() {
        if(!error) {
            setName(DEFAULT_TC_NAME); //set default TC name
            LOGGER.log(Level.FINE, "Previewable error occured.");//NOI18N
            removeAll();
            add(PREVIEW_ERROR_PANEL, BorderLayout.CENTER);
            previewing = false;
            error = true;
            
            revalidate();
            repaint();

        }
    }
    
    //run in AWT!
    private void setPreviewing(String title) {
        setName(title);
        removeAll();
        add(previewPanel.getComponent(), BorderLayout.CENTER);
        previewing = true;
        error = false;
        
        revalidate();
        repaint();
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
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private String getTitle(CssRuleContext content) {
        if(content != null && content.selectedRuleContent() != null) {
            return content.selectedRuleContent().rule().name() + " - " + DEFAULT_TC_NAME; //NOI18N
        }
        return DEFAULT_TC_NAME;
    }
    
    private void preview(CssRuleContext content) {

        assert SwingUtilities.isEventDispatchThread() : "Must be run in event dispatch thread!";

        //get the generated sample XHTML code
        CharSequence htmlCode = CssPreviewGenerator.getPreviewCode(content);

        //parse it first to find potential errors in the code
        if (parser != null) {
            try {
                DefaultHandler handler = new DefaultHandler();
                parser.parse(new ByteArrayInputStream(htmlCode.toString().getBytes()), handler);
            } catch (SAXException ex) {
                LOGGER.log(Level.INFO, "There is an error in the generated sample document.", ex);
                LOGGER.log(Level.INFO, "Errorneous preview sample code:\n---------------------------------\n" + htmlCode); //NOI18N
                setError();
                return;
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex); //NOI18N
            }
        }

        //set the UI to the previewing state
        setPreviewing(getTitle(content));

        try {
            //resolve relative URL base for the preview component
            String relativeURL = null;
            File source = content.base();
            if (source != null) {
                relativeURL = source.toURL().toExternalForm();
            }

            LOGGER.log(Level.FINE, "preview - setting content " + htmlCode); //NOI18N
            //set XHTML preview panel content - the generated sample
            previewPanel.setDocument(new ByteArrayInputStream(htmlCode.toString().getBytes()), relativeURL);
        } catch (Throwable e) {
            //an error - show message into the preview
            setError();
            LOGGER.log(Level.INFO, "An error occured in the preview component.", e); //NOI18N
            LOGGER.log(Level.INFO, "Errorneous preview sample code:\n---------------------------------\n" + htmlCode); //NOI18N
        }
    }
    
//    private void previewableSelected(final CssPreviewable previewable) {
//        if(lastSelectedPreviewable != null) {
//            if(lastSelectedPreviewable.equals(previewable)) {
//                return ; //ignore
//            } else {
//                LOGGER.log(Level.FINE, "removed listener from " + lastSelectedPreviewable);//NOI18N
//                lastSelectedPreviewable.removeListener(PREVIEWABLE_LISTENER);
//            }
//        }
//        lastSelectedPreviewable = previewable;
//        LOGGER.log(Level.FINE, "added listener to " + previewable);//NOI18N
//        lastSelectedPreviewable.addListener(PREVIEWABLE_LISTENER);
//        
//        //preview the content is available
//        final CssRuleContext context = previewable.content();
//        if(context != null) {
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    preview(context);
//                }
//            });
//        } else {
//            //no content to preview
//            setNoSelectedRule();
//        }
//        
//    }
    
//    @Override
//    public void componentOpened() {
//        WindowManager.getDefault().getRegistry().addPropertyChangeListener(WINDOW_REGISTRY_LISTENER);
//        checkPreview(org.openide.windows.WindowManager.getDefault().getRegistry().getActivated());
//
//    }
//    
//    @Override
//    protected void componentActivated() {
//        super.componentActivated();
//    }
//    

    @Override
    public void componentClosed() {
        previewPanel.dispose();
//        WindowManager.getDefault().getRegistry().removePropertyChangeListener(WINDOW_REGISTRY_LISTENER);
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
