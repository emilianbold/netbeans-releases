/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.customizer;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.ErrorManager;


/**
 * Customizer data support keeps models for all the customizer components, 
 * initializes them, tracks model changes and performs save.
 *
 * @author sherold
 */
public class CustomizerDataSupport {
    
    // models    
    private DefaultComboBoxModel    jvmModel;
    private Document                javaOptsModel;
    private ButtonModel             secManagerModel;        
    private Document                catalinaHomeModel;
    private Document                catalinaBaseModel;    
    private Document                usernameModel;
    private Document                passwordModel;    
    private Document                scriptPathModel;
    private ButtonModel             customScriptModel;
    private ButtonModel             forceStopModel;    
    private ButtonModel             sharedMemModel;
    private ButtonModel             socketModel;    
    private ButtonModel             monitorModel;    
    private Document                sharedMemNameModel;    
    private PathModel               sourceModel;
    private PathModel               classModel;
    private PathModel               javadocModel;
    private SpinnerNumberModel      serverPortModel;
    private SpinnerNumberModel      shutdownPortModel;
    private SpinnerNumberModel      debugPortModel;
    
    // model dirty flags    
    private boolean jvmModelFlag;
    private boolean javaOptsModelFlag;
    private boolean secManagerModelFlag;
    private boolean usernameModelFlag;
    private boolean passwordModelFlag;
    private boolean scriptPathModelFlag;
    private boolean customScriptModelFlag;
    private boolean forceStopModelFlag;
    private boolean sharedMemModelFlag;
    private boolean socketModelFlag;
    private boolean monitorModelFlag;
    private boolean sharedMemNameModelFlag;
    private boolean sourceModelFlag;
    private boolean javadocModelFlag;
    private boolean serverPortModelFlag;
    private boolean shutdownPortModelFlag;
    private boolean debugPortModelFlag;
    
    private TomcatProperties tp;
    private TomcatManager tm;
    
    private FocusListener saveOnFocusLostListener = new FocusAdapter() {
        public void focusLost(FocusEvent evt) {
            store();
        }
    };
    
    /**
     * Creates a new instance of CustomizerDataSupport 
     */
    public CustomizerDataSupport(TomcatManager tm) {
        this.tm = tm;
        tp = tm.getTomcatProperties();
        init();
    }
    
    /** Initialize the customizer models. */
    private void init() {
        
        // jvmModel
        jvmModel = new DefaultComboBoxModel();
        loadJvmModel();
        jvmModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                jvmModelFlag = true;
            }
        });
        
        // javaOptions
        javaOptsModel = createDocument(tp.getJavaOpts());
        javaOptsModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javaOptsModelFlag = true;
            }
        });
        
        // catalinaHomeModel
        catalinaHomeModel = createDocument(tp.getCatalinaHome().toString());
        
        // catalinaBaseModel
        catalinaBaseModel = createDocument(tp.getCatalinaDir().toString());
        
        // usernameModel
        usernameModel = createDocument(tp.getUsername());
        usernameModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                usernameModelFlag = true;
            }
        });
        
        // passwordModel
        passwordModel = createDocument(tp.getPassword());
        passwordModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                passwordModelFlag = true;
            }
        });
        
        // sharedMemNameModel
        sharedMemNameModel = createDocument(tp.getSharedMem());
        sharedMemNameModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                sharedMemNameModelFlag = true;
            }
        });

        // scriptPathModel
        scriptPathModel = createDocument(tp.getScriptPath());
        scriptPathModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                scriptPathModelFlag = true;
            }
        });
        
        // secManagerModel
        secManagerModel = createToggleButtonModel(tp.getSecManager());
        secManagerModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                secManagerModelFlag = true;
            }
        });
        
        // customScriptModel
        customScriptModel = createToggleButtonModel(tp.getCustomScript());
        customScriptModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                customScriptModelFlag = true;
            }
        });
        
        // forceStopModel
        forceStopModel = createToggleButtonModel(tp.getForceStop());
        forceStopModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                forceStopModelFlag = true;
            }
        });
        
        // monitorModel
        monitorModel = createToggleButtonModel(tp.getMonitor());
        monitorModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                monitorModelFlag = true;
            }
        });

        // classModel
        classModel = new PathModel(tp.getClasses());
        
        // sourceModel
        sourceModel = new PathModel(tp.getSources());
        sourceModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                sourceModelFlag = true;
            }
        });
        
        // javadocModel
        javadocModel = new PathModel(tp.getJavadocs());
        javadocModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javadocModelFlag = true;
            }
        });
        
        // serverPortModel
        serverPortModel = new SpinnerNumberModel(tm.getServerPort(), 0, 65535, 1);
        serverPortModel.addChangeListener(new ModelChangeAdapter() {
            public void modelChanged() {
                serverPortModelFlag = true;
            }
        });
        
        // shutdownPortModel
        shutdownPortModel = new SpinnerNumberModel(tm.getShutdownPort(), 0, 65535, 1);
        shutdownPortModel.addChangeListener(new ModelChangeAdapter() {
            public void modelChanged() {
                shutdownPortModelFlag = true;
            }
        });
        
        // debugPortModel
        debugPortModel = new SpinnerNumberModel(tp.getDebugPort(), 0, 65535, 1);
        debugPortModel.addChangeListener(new ModelChangeAdapter() {
            public void modelChanged() {
                debugPortModelFlag = true;
            }
        });
        
        ButtonGroup debugButtonGroup = new ButtonGroup();
        
        // socketModel
        socketModel = new JToggleButton.ToggleButtonModel();
        socketModel.setGroup(debugButtonGroup);
        socketModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                socketModelFlag = true;
            }
        });
        
        // sharedMemModel
        sharedMemModel = new JToggleButton.ToggleButtonModel();
        sharedMemModel.setGroup(debugButtonGroup);
        sharedMemModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                sharedMemModelFlag = true;
            }
        });
        
        boolean socketEnabled = TomcatProperties.DEBUG_TYPE_SOCKET.equalsIgnoreCase(tp.getDebugType());
        debugButtonGroup.setSelected(socketEnabled ? socketModel : sharedMemModel, true);
    }
    
    /** Update the jvm model */
    public void loadJvmModel() {
        String curJvmName = (String)jvmModel.getSelectedItem();
        if (curJvmName == null) {
            curJvmName = tp.getJavaPlatform();
        }
        jvmModel.removeAllElements();
        
        // feed the combo with sorted platform list
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] j2sePlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        String[] platformDisplayNames = new String[j2sePlatforms.length];
        for (int i = 0; i < j2sePlatforms.length; i++) {
            platformDisplayNames[i] = j2sePlatforms[i].getDisplayName();
        }
        Arrays.sort(platformDisplayNames);
        for (int i = 0; i < platformDisplayNames.length; i++) {
            jvmModel.addElement(platformDisplayNames[i]);
        }
        
        // set selected
        JavaPlatform[] curJvms = jpm.getPlatforms(curJvmName, new Specification("J2SE", null)); // NOI18N
        if (curJvms.length == 0) {
            jvmModel.setSelectedItem(jpm.getDefaultPlatform().getDisplayName());
        } else {
            jvmModel.setSelectedItem(curJvms[0].getDisplayName());
        }
    }
    
    // model getters ----------------------------------------------------------
        
    public DefaultComboBoxModel getJvmModel() {
        return jvmModel;
    }
    
    public Document getJavaOptsModel() {
        return javaOptsModel;
    }
    
    public Document getCatalinaHomeModel() {
        return catalinaHomeModel;
    }
    
    public Document getCatalinaBaseModel() {
        return catalinaBaseModel;
    }
    
    public Document getUsernameModel() {
        return usernameModel;
    }    
    
    public Document getPasswordModel() {
        return passwordModel;
    }
    
    public ButtonModel getCustomScriptModel() {
        return customScriptModel;
    }
    
    public ButtonModel getForceStopModel() {
        return forceStopModel;
    }
    
    public Document getScriptPathModel() {
        return scriptPathModel;
    }
    
    public ButtonModel getSharedMemModel() {
        return sharedMemModel;
    }
    
    public ButtonModel getSocketModel() {
        return socketModel;
    }
    
    public ButtonModel getMonitorModel() {
        return monitorModel;
    }
    
    public ButtonModel getSecManagerModel() {
        return secManagerModel;
    }
    
    public Document getSharedMemNameModel() {
        return sharedMemNameModel;
    }
    
    public PathModel getClassModel() {
        return classModel;
    }
    
    public PathModel getSourceModel() {
        return sourceModel;
    }
    
    public PathModel getJavadocsModel() {
        return javadocModel;
    }
    
    public SpinnerNumberModel getServerPortModel() {
        return serverPortModel;
    }
    
    public SpinnerNumberModel getShutdownPortModel() {
        return shutdownPortModel;
    }
    
    public SpinnerNumberModel getDebugPortModel() {
        return debugPortModel;
    }
    
    public FocusListener getSaveOnFocusLostListener() {
        return saveOnFocusLostListener;
    }
    
    // private helper methods -------------------------------------------------
    
    /** Save all changes */
    private void store() {
        
        if (jvmModelFlag) {
            tp.setJavaPlatform((String)jvmModel.getSelectedItem());
            jvmModelFlag = false;
        }
        
        if (javaOptsModelFlag) {
            tp.setJavaOpts(getText(javaOptsModel));
            javaOptsModelFlag = false;
        }
        
        if (secManagerModelFlag) {
            tp.setSecManager(secManagerModel.isSelected());
            secManagerModelFlag = false;
        }
        
        if (usernameModelFlag) {
            tp.setUsername(getText(usernameModel));
            usernameModelFlag = false;
        }
        
        if (passwordModelFlag) {
            tp.setPassword(getText(passwordModel));
            passwordModelFlag = false;
        }
        
        if (scriptPathModelFlag) {
            tp.setScriptPath(getText(scriptPathModel));
            scriptPathModelFlag = false;
        }
        
        if (customScriptModelFlag) {
            tp.setCustomScript(customScriptModel.isSelected());
            customScriptModelFlag = false;
        }
        
        if (forceStopModelFlag) {
            tp.setForceStop(forceStopModel.isSelected());
            forceStopModelFlag = false;
        }
        
        if (sharedMemModelFlag || socketModelFlag) {
            tp.setDebugType(sharedMemModelFlag ? TomcatProperties.DEBUG_TYPE_SHARED 
                                               : TomcatProperties.DEBUG_TYPE_SOCKET);
            sharedMemModelFlag = false;
            socketModelFlag = false;
        }
        
        if (monitorModelFlag) {
            tp.setMonitor(monitorModel.isSelected());
            monitorModelFlag = false;
        }
        
        if (sharedMemNameModelFlag) {
            tp.setSharedMem(getText(sharedMemNameModel));
            sharedMemNameModelFlag = false;
        }
        
        if (sourceModelFlag) {
            tp.setSources(sourceModel.getData());
            sourceModelFlag = false;
        }
        
        if (javadocModelFlag) {
            tp.setJavadocs(javadocModel.getData());
            javadocModelFlag = false;
        }
        
        if (serverPortModelFlag) {
            tm.setServerPort(((Integer)serverPortModel.getValue()).intValue());
            serverPortModelFlag = false;
        }
        
        if (shutdownPortModelFlag) {
            tm.setShutdownPort(((Integer)shutdownPortModel.getValue()).intValue());
            shutdownPortModelFlag = false;
        }
        
        if (debugPortModelFlag) {
            tp.setDebugPort(((Integer)debugPortModel.getValue()).intValue());
            debugPortModelFlag = false;
        }
    }
    
    /** Create a Document initialized by the specified text parameter, which may be null */
    private Document createDocument(String text) {
        PlainDocument doc = new PlainDocument();
        if (text != null) {
            try {
                doc.insertString(0, text, null);
            } catch(BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return doc;
    }
    
    /** Create a ToggleButtonModel inilialized by the specified selected parameter. */
    private JToggleButton.ToggleButtonModel createToggleButtonModel(boolean selected) {
        JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel();
        model.setSelected(selected);
        return model;
    }
    
    /** Get the text value from the document */
    private String getText(Document doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch(BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /** 
     * Adapter that implements several listeners, which is useful for dirty model
     * monitoring.
     */
    private abstract class ModelChangeAdapter implements ListDataListener, 
            DocumentListener, ItemListener, ChangeListener {
        
        public abstract void modelChanged();
        
        public void contentsChanged(ListDataEvent e) {
            modelChanged();
        }

        public void intervalAdded(ListDataEvent e) {
            modelChanged();
        }

        public void intervalRemoved(ListDataEvent e) {
            modelChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void itemStateChanged(ItemEvent e) {
            modelChanged();
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            modelChanged();
        }
    }
}
