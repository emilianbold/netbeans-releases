/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.file.validator.Utils;
import org.netbeans.modules.xml.wsdl.bindingsupport.appconfig.spi.CompositeDataEditorPanel;

/**
 *
 * @author jfu
 */
public class FileBCApplicationConfigurationEditorPanel
        extends CompositeDataEditorPanel implements DocumentListener, ActionListener {
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.file.configeditor.Bundle");

    private static final String APP_CFG_NAME = "configurationName";
    public static final String APPLICATION_CONFIG_PROPERTY_FILEDIR = "fileDirectory";
    public static final String APPLICATION_CONFIG_PROPERTY_RELATIVEPATH = "relativePath";
    public static final String APPLICATION_CONFIG_PROPERTY_PATHRELATIVETO = "pathRelativeTo";
    public static final String APPLICATION_CONFIG_PROPERTY_LOCKNAME = "lockName";
    public static final String APPLICATION_CONFIG_PROPERTY_WORKAREA = "workArea";
    public static final String APPLICATION_CONFIG_PROPERTY_SEQNAME = "seqName";
    public static final String APPLICATION_CONFIG_PROPERTY_PERSIST_BASE = "persistenceBaseLoc";
    public static final String APPLICATION_CONFIG_PROPERTY_RECURSIVE = "recursive";
    public static final String APPLICATION_CONFIG_PROPERTY_EXCLUDE_REGEX = "recursiveExclude";

    public static final String FILEDIR_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_FILEDIR);
    public static final String RELATIVEPATH_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_RELATIVEPATH);
    public static final String PATHRELATIVETO_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_PATHRELATIVETO);
    public static final String LOCKNAME_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_LOCKNAME);
    public static final String WORKAREA_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_WORKAREA);
    public static final String SEQNAME_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_SEQNAME);
    public static final String PERSIST_BASE_LOC_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_PERSIST_BASE);
    public static final String RECURSIVE_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_RECURSIVE);
    public static final String RECURSIVE_REGEX_DESC = mMessages.getString(APPLICATION_CONFIG_PROPERTY_EXCLUDE_REGEX);

    private static final String[] AppConfigRowAttrNames =
    {
    	APP_CFG_NAME,
    	APPLICATION_CONFIG_PROPERTY_FILEDIR,
    	APPLICATION_CONFIG_PROPERTY_RELATIVEPATH,
    	APPLICATION_CONFIG_PROPERTY_PATHRELATIVETO,
    	APPLICATION_CONFIG_PROPERTY_LOCKNAME,
    	APPLICATION_CONFIG_PROPERTY_WORKAREA,
    	APPLICATION_CONFIG_PROPERTY_SEQNAME,
        APPLICATION_CONFIG_PROPERTY_PERSIST_BASE,
        APPLICATION_CONFIG_PROPERTY_RECURSIVE,
        APPLICATION_CONFIG_PROPERTY_EXCLUDE_REGEX
    };
    private static final String[] AppConfigAttrDesc =
    {
    	"Application Configuration Name",
    	FILEDIR_DESC,
    	RELATIVEPATH_DESC,
    	PATHRELATIVETO_DESC,
    	LOCKNAME_DESC,
    	WORKAREA_DESC,
    	SEQNAME_DESC,
        PERSIST_BASE_LOC_DESC,
        RECURSIVE_DESC,
        RECURSIVE_REGEX_DESC
    };
    private static final OpenType[] AppConfigAttrTypes =
    {
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.BOOLEAN,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.STRING,
    	SimpleType.BOOLEAN,
    	SimpleType.STRING
    };

    private static final String PARAM_NOT_SPECIFIED = "PARAM_NOT_SPECIFIED";
    
    private JTextField fileDir;
    private JTextField lockName;
    private JCheckBox relativePath;
    private JTextField seqName;
    private JComboBox pathRelativeTo;
    private JTextField workArea;
    private JTextField persistBaseLoc;
    private JCheckBox recursive;
    private JTextField excludeRegex;
    
    private String appConfigName;
    
    public FileBCApplicationConfigurationEditorPanel(String appConfigName,
            CompositeData compositeData) {
        this.appConfigName = appConfigName;
        init(compositeData);
    }

    private String getDisplayName(String key) {
        return mMessages.getString(key);
    }
    
    private String getLocalizedMessage(String key, Object[] params) {
        String msg = null;
        String fmt = mMessages.getString(key);
        if ( fmt != null ) {
            if ( params != null ) {
                msg = MessageFormat.format(fmt, params);
            } else {
                msg = fmt;
            }
        }
        else {
            msg = key;
        }
        return msg;
    }
    
    private void init(CompositeData compositeData) {
        setLayout(new java.awt.GridBagLayout());
        //setLayout(new GridLayout(9, 2));
        GridBagConstraints gridBagConstraints = null;

        persistBaseLoc = new JTextField();
        persistBaseLoc.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_PERSIST_BASE));
        
        fileDir = new JTextField();
        fileDir.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_FILEDIR));

        lockName = new JTextField();
        lockName.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_LOCKNAME));

        seqName = new JTextField();
        seqName.setText(compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_SEQNAME));

        pathRelativeTo = new JComboBox(
                new Object[] {
                    "",
                    "User Home",
                    "Current Working Dir",
                    "Default System Temp Dir"
        });
        
        String relative2 = compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_PATHRELATIVETO);

        pathRelativeTo.setSelectedItem(relative2);

        String workarea = compositeData == null ? "" : (String)compositeData.get(APPLICATION_CONFIG_PROPERTY_WORKAREA);

        workArea = new JTextField();
        workArea.setText(workarea);
        
        relativePath = new JCheckBox();
        Boolean dirIsRelative = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_RELATIVEPATH));
        relativePath.setSelected(dirIsRelative.booleanValue());
        
        recursive = new JCheckBox();
        Boolean isRecursive = (compositeData == null ? new Boolean(false) : (Boolean)compositeData.get(APPLICATION_CONFIG_PROPERTY_RECURSIVE));
        recursive.setSelected(isRecursive.booleanValue());

        excludeRegex = new JTextField();
        excludeRegex.setText(compositeData == null ? "" : (String) compositeData.get(APPLICATION_CONFIG_PROPERTY_EXCLUDE_REGEX));

        int y_co = 0;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_FILEDIR)), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(fileDir, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_RELATIVEPATH)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(relativePath, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_PATHRELATIVETO)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(pathRelativeTo, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_LOCKNAME)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(lockName, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_SEQNAME)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(seqName, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_WORKAREA)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(workArea, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_PERSIST_BASE)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(persistBaseLoc, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_RECURSIVE)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(recursive, gridBagConstraints);

        y_co++;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(new JLabel(getDisplayName(APPLICATION_CONFIG_PROPERTY_EXCLUDE_REGEX)), gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y_co;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(excludeRegex, gridBagConstraints);

        fileDir.getDocument().addDocumentListener(this);
        lockName.getDocument().addDocumentListener(this);
        seqName.getDocument().addDocumentListener(this);
        workArea.getDocument().addDocumentListener(this);
        persistBaseLoc.getDocument().addDocumentListener(this);
        excludeRegex.getDocument().addDocumentListener(this);
        
        pathRelativeTo.addActionListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                validateContent();
            }
        });
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    }

    public CompositeData getCompositeData() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(APP_CFG_NAME, appConfigName);
        map.put(APPLICATION_CONFIG_PROPERTY_FILEDIR, fileDir.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_RELATIVEPATH, new Boolean(relativePath.isSelected()));
        map.put(APPLICATION_CONFIG_PROPERTY_PATHRELATIVETO, pathRelativeTo.getSelectedItem() != null ?  pathRelativeTo.getSelectedItem().toString() : "");
        map.put(APPLICATION_CONFIG_PROPERTY_LOCKNAME, lockName.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_SEQNAME, seqName.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_WORKAREA, workArea.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_PERSIST_BASE, persistBaseLoc.getText());
        map.put(APPLICATION_CONFIG_PROPERTY_RECURSIVE, new Boolean(recursive.isSelected()));
        map.put(APPLICATION_CONFIG_PROPERTY_EXCLUDE_REGEX, excludeRegex.getText());

        try {
            CompositeType compositeType = new CompositeType("AppliationConfigurationObject",
                    "FILEBC Application Configuration CompositeType",
                    AppConfigRowAttrNames,
                    AppConfigAttrDesc,
                    AppConfigAttrTypes);
            return new CompositeDataSupport(compositeType, map);
        } catch (OpenDataException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void validateContent() {
        // fire property change event at first error
        if (Utils.isEmpty(fileDir.getText())) {
            firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, 
                    getLocalizedMessage(PARAM_NOT_SPECIFIED, new Object[] {APPLICATION_CONFIG_PROPERTY_FILEDIR}));
            return;
        }

        firePropertyChange(CompositeDataEditorPanel.PROPERTY_VALIDATION_RESULT, null, null);
    }

    public void insertUpdate(DocumentEvent e) {
        validateContent();
    }

    public void removeUpdate(DocumentEvent e) {
        validateContent();
    }

    public void changedUpdate(DocumentEvent e) {
        validateContent();
    }

    public void actionPerformed(ActionEvent e) {
        Object select = e.getSource();
    }
}
