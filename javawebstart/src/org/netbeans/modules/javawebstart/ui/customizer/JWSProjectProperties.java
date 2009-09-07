/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 */
public class JWSProjectProperties /*implements TableModelListener*/ {
    
    public static final String JNLP_ENABLED      = "jnlp.enabled";
    public static final String JNLP_ICON         = "jnlp.icon";
    public static final String JNLP_OFFLINE      = "jnlp.offline-allowed";
    public static final String JNLP_CBASE_TYPE   = "jnlp.codebase.type";
    public static final String JNLP_CBASE_USER   = "jnlp.codebase.user";
    public static final String JNLP_CBASE_URL    = "jnlp.codebase.url";
    public static final String JNLP_DESCRIPTOR   = "jnlp.descriptor";
    public static final String JNLP_APPLET       = "jnlp.applet.class";
    
    public static final String JNLP_SPEC         = "jnlp.spec";
    public static final String JNLP_INIT_HEAP    = "jnlp.initial-heap-size";
    public static final String JNLP_MAX_HEAP     = "jnlp.max-heap-size";
    
    public static final String JNLP_SIGNED = "jnlp.signed";
    
    public static final String CB_TYPE_LOCAL = "local";
    public static final String CB_TYPE_WEB = "web";
    public static final String CB_TYPE_USER = "user";
    
    public static final String DEFAULT_APPLET_WIDTH = "300";
    public static final String DEFAULT_APPLET_HEIGHT = "300";
    
    public enum DescType {
        application, applet, component;
    }
    
    public static final String CB_URL_WEB = "$$codebase";
    
    public static final String JNLP_EXT_RES_PREFIX = "jnlp.ext.resource.";
    public static final String JNLP_APPLET_PARAMS_PREFIX = "jnlp.applet.param.";
    public static final String JNLP_APPLET_WIDTH = "jnlp.applet.width";
    public static final String JNLP_APPLET_HEIGHT = "jnlp.applet.height";
    
    // property to be set when enabling javawebstart to disable Compile on Save feature
    // javawebstart project needs to be built completly before it could be run
    public static final String COS_UNSUPPORTED_PROPNAME = "compile.on.save.unsupported.javawebstart";

    // special value to persist Ant script handling
    public static final String CB_URL_WEB_PROP_VALUE = "$$$$codebase";
    
    private StoreGroup jnlpPropGroup = new StoreGroup();
    
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project j2seProject;
    
    private List<Map<String,String>> extResProperties;
    private List<Map<String,String>> appletParamsProperties;
    
    public static final String extResSuffixes[] = new String[] { "href", "name", "version" };
    public static final String appletParamsSuffixes[] = new String[] { "name", "value" };

    public static final String CONFIG_LABEL_PROPNAME = "$label";
    public static final String CONFIG_TARGET_RUN_PROPNAME = "$target.run";
    public static final String CONFIG_TARGET_DEBUG_PROPNAME = "$target.debug";

    public static final String CONFIG_TARGET_RUN = "jws-run";
    public static final String CONFIG_TARGET_DEBUG = "jws-debug";

    private DescType selectedDescType = null;

    boolean isJnlpImplPreviousVersion = false;

    // Models 
    JToggleButton.ToggleButtonModel enabledModel;
    JToggleButton.ToggleButtonModel allowOfflineModel;
    JToggleButton.ToggleButtonModel signedModel;
    
    ComboBoxModel codebaseModel;
    ComboBoxModel appletClassModel;
    
    ButtonModel applicationDescButtonModel;
    ButtonModel appletDescButtonModel;
    ButtonModel compDescButtonModel;
    private ButtonGroup bg;
    
    PropertiesTableModel extResTableModel;
    PropertiesTableModel appletParamsTableModel;
    
    // and Documents
    Document iconDocument;
    Document codebaseURLDocument;
    Document appletWidthDocument;
    Document appletHeightDocument;
    
    /** Creates a new instance of JWSProjectProperties */
    public JWSProjectProperties(Lookup context) {
        
        j2seProject = context.lookup(Project.class);
        
        if (j2seProject != null) {
            
            j2sePropEval = j2seProject.getLookup().lookup(J2SEPropertyEvaluator.class);
            
            evaluator = j2sePropEval.evaluator();
        
            enabledModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_ENABLED);
            allowOfflineModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_OFFLINE);
            signedModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_SIGNED);
            iconDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_ICON);
            appletWidthDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_APPLET_WIDTH);
            appletHeightDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_APPLET_HEIGHT);
            
            codebaseModel = new CodebaseComboBoxModel();
            codebaseURLDocument = createCBTextFieldDocument();
        
            appletClassModel = new AppletClassComboBoxModel(j2seProject);
            initRadioButtons();
            
            extResProperties = readProperties(evaluator, JNLP_EXT_RES_PREFIX, extResSuffixes);
            appletParamsProperties = readProperties(evaluator, JNLP_APPLET_PARAMS_PREFIX, appletParamsSuffixes);

            // check if the jnlp-impl.xml script is of previous version -> should be upgraded
            FileObject jnlpImlpFO = j2seProject.getProjectDirectory().getFileObject("nbproject/jnlp-impl.xml");
            if (jnlpImlpFO != null) {
                try {
                    String crc = JWSCompositeCategoryProvider.computeCrc32(jnlpImlpFO.getInputStream());
                    isJnlpImplPreviousVersion = JWSCompositeCategoryProvider.isJnlpImplPreviousVer(crc);
                } catch (IOException ex) {
                    // nothing to do really
                }
            }

        } 
        
    }
    
    boolean isJWSEnabled() {
        return enabledModel.isSelected();
    }
    
    public DescType getDescTypeProp() {
        DescType toReturn;
        if (selectedDescType != null) {
            return selectedDescType;
        }
        String desc = evaluator.getProperty(JNLP_DESCRIPTOR);
        if (desc != null) {
            toReturn = DescType.valueOf(desc);
        } else {
            toReturn = DescType.application;
        }
        return toReturn;
    }
    
    public void updateDescType() {
        selectedDescType = getSelectedDescType();
    }
    
    public List<Map<String,String>> getExtResProperties() {
        return extResProperties;
    }
    
    public void setExtResProperties(List<Map<String,String>> props) {
        extResProperties = props;
    }
    
    public List<Map<String,String>> getAppletParamsProperties() {
        return appletParamsProperties;
    }
    
    public void setAppletParamsProperties(List<Map<String,String>> props) {
        appletParamsProperties = props;
    }
    
    private void initRadioButtons() {
        
        applicationDescButtonModel = new ToggleButtonModel();
        appletDescButtonModel = new ToggleButtonModel();
        compDescButtonModel = new ToggleButtonModel();
        bg = new ButtonGroup();
        applicationDescButtonModel.setGroup(bg);
        appletDescButtonModel.setGroup(bg);
        compDescButtonModel.setGroup(bg);
        
        String desc = evaluator.getProperty(JNLP_DESCRIPTOR);
        if (desc != null) {
            if (desc.equals(DescType.application.toString())) {
                applicationDescButtonModel.setSelected(true);
            } else if (desc.equals(DescType.applet.toString())) {
                appletDescButtonModel.setSelected(true);
            } else if (desc.equals(DescType.component.toString())) {
                compDescButtonModel.setSelected(true);
            }
        } else {
            applicationDescButtonModel.setSelected(true);
        }
        
    }
    
    private void storeRest(EditableProperties editableProps) {
        // store codebase type
        String selItem = ((CodebaseComboBoxModel) codebaseModel).getSelectedCodebaseItem();
        String propName = null;
        String propValue = null;
        if (CB_TYPE_USER.equals(selItem)) {
            propName = JNLP_CBASE_USER;
            try {
                propValue = codebaseURLDocument.getText(0, codebaseURLDocument.getLength());
            } catch (BadLocationException ex) {
                // do not store anything
                // XXX log the exc
                return;
            }
        } else if (CB_TYPE_LOCAL.equals(selItem)) {
            // #161919: local codebase will be computed
            //propName = JNLP_CBASE_URL;
            //propValue = getProjectDistDir();
        } else if (CB_TYPE_WEB.equals(selItem))  {
            propName = JNLP_CBASE_URL;
            propValue = CB_URL_WEB_PROP_VALUE;
        }
        editableProps.setProperty(JNLP_CBASE_TYPE, selItem);
        if (propName != null && propValue != null) {
            editableProps.setProperty(propName, propValue);
        }
        // store applet class name and default applet size
        String appletClassName = (String) appletClassModel.getSelectedItem();
        if (appletClassName != null && !appletClassName.equals("")) {
            editableProps.setProperty(JNLP_APPLET, appletClassName);
            String appletWidth = null;
            try {
                appletWidth = appletWidthDocument.getText(0, appletWidthDocument.getLength());
            } catch (BadLocationException ex) {
                // appletWidth will be null
            }
            if (appletWidth == null || "".equals(appletWidth)) {
                editableProps.setProperty(JNLP_APPLET_WIDTH, DEFAULT_APPLET_WIDTH);
            }
            String appletHeight = null;
            try {
                appletHeight = appletHeightDocument.getText(0, appletHeightDocument.getLength());
            } catch (BadLocationException ex) {
                // appletHeight will be null
            }
            if (appletHeight == null || "".equals(appletHeight)) {
                editableProps.setProperty(JNLP_APPLET_HEIGHT, DEFAULT_APPLET_HEIGHT);
            }
        }
        // store descriptor type
        DescType descType = getSelectedDescType();
        if (descType != null) {
            editableProps.setProperty(JNLP_DESCRIPTOR, descType.toString());
        }
        // store properties
        storeProperties(editableProps, extResProperties, JNLP_EXT_RES_PREFIX);
        storeProperties(editableProps, appletParamsProperties, JNLP_APPLET_PARAMS_PREFIX);
    }
    
    public void store() throws IOException {
        
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = j2seProject.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        try {
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    jnlpPropGroup.store(ep);
                    storeRest(ep);
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        } 
        
    }
    
    private DescType getSelectedDescType() {
        DescType toReturn = null;
        if (applicationDescButtonModel.isSelected()) {
            toReturn = DescType.application;
        } else if (appletDescButtonModel.isSelected()) {
            toReturn = DescType.applet;
        } else if (compDescButtonModel.isSelected()) {
            toReturn = DescType.component;
        }
        return toReturn;
    }
    
    private Document createCBTextFieldDocument() {
        Document doc = new PlainDocument();
        String valueType = evaluator.getProperty(JNLP_CBASE_TYPE);
        String docString = "";
        if (CB_TYPE_LOCAL.equals(valueType)) {
            docString = getProjectDistDir();
        } else if (CB_TYPE_WEB.equals(valueType)) {
            docString = CB_URL_WEB;
        } else if (CB_TYPE_USER.equals(valueType)) {
            docString = getCodebaseLocation();
        }
        try {
            doc.insertString(0, docString, null);
        } catch (BadLocationException ex) {
            // do nothing, just return PlainDocument
            // XXX log the exc
        }
        return doc;
    }
    
    public String getCodebaseLocation() {
        return evaluator.getProperty(JNLP_CBASE_USER);
    }
        
    public String getProjectDistDir() {
        File distDir = new File(FileUtil.toFile(j2seProject.getProjectDirectory()), evaluator.getProperty("dist.dir"));
        return distDir.toURI().toString();
    }
    
    // only should return JNLP properties
    public String getProperty(String propName) {
        return evaluator.getProperty(propName);
    }
    
    // ----------
    
    public class CodebaseComboBoxModel extends DefaultComboBoxModel {
        
        String localLabel = NbBundle.getBundle(JWSProjectProperties.class).getString("LBL_CB_Combo_Local");
        String webLabel = NbBundle.getBundle(JWSProjectProperties.class).getString("LBL_CB_Combo_Web");
        String userLabel = NbBundle.getBundle(JWSProjectProperties.class).getString("LBL_CB_Combo_User");
        Object visItems[] = new Object[] { localLabel, webLabel, userLabel };
        String cbItems[] = new String[] { CB_TYPE_LOCAL, CB_TYPE_WEB, CB_TYPE_USER };
        
        public CodebaseComboBoxModel() {
            super();
            addElement(visItems[0]);
            addElement(visItems[1]);
            addElement(visItems[2]);
            String propValue = evaluator.getProperty(JNLP_CBASE_TYPE);
            if (cbItems[2].equals(propValue)) {
                setSelectedItem(visItems[2]);
            } else if (cbItems[1].equals(propValue)) {
                setSelectedItem(visItems[1]);
            } else {
                setSelectedItem(visItems[0]);
            }
        }
        
        public String getSelectedCodebaseItem() {
            return cbItems[getIndexOf(getSelectedItem())];
        }
        
    }
    
    public class AppletClassComboBoxModel extends DefaultComboBoxModel {
        
        Set<SearchKind> kinds = new HashSet<SearchKind>(Arrays.asList(SearchKind.IMPLEMENTORS));
        Set<SearchScope> scopes = new HashSet<SearchScope>(Arrays.asList(SearchScope.SOURCE));
        
        public AppletClassComboBoxModel(final Project proj) {
            
            Sources sources = ProjectUtils.getSources(proj);
            SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            final Map<FileObject,List<ClassPath>> classpathMap = new HashMap<FileObject,List<ClassPath>>();
            
            for (SourceGroup srcGroup : srcGroups) {
                FileObject srcRoot = srcGroup.getRootFolder();
                ClassPath bootCP = ClassPath.getClassPath(srcRoot, ClassPath.BOOT);
                ClassPath executeCP = ClassPath.getClassPath(srcRoot, ClassPath.EXECUTE);
                ClassPath sourceCP = ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
                List<ClassPath> cpList = new ArrayList<ClassPath>();
                if (bootCP != null) {
                    cpList.add(bootCP);
                }
                if (executeCP != null) {
                    cpList.add(executeCP);
                }
                if (sourceCP != null) {
                    cpList.add(sourceCP);
                }
                if (cpList.size() == 3) {
                    classpathMap.put(srcRoot, cpList);
                }
            }
            
            final Set<String> appletNames = new HashSet<String>();
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    for (FileObject fo : classpathMap.keySet()) {
                        List<ClassPath> paths = classpathMap.get(fo);
                        ClasspathInfo cpInfo = ClasspathInfo.create(paths.get(0), paths.get(1), paths.get(2));
                        final ClassIndex classIndex = cpInfo.getClassIndex();
                        final JavaSource js = JavaSource.create(cpInfo);
                        try {
                            js.runUserActionTask(new CancellableTask<CompilationController>() {
                                public void run(CompilationController controller) throws Exception {
                                    Elements elems = controller.getElements();
                                    TypeElement appletElement = elems.getTypeElement("java.applet.Applet");
                                    ElementHandle<TypeElement> appletHandle = ElementHandle.create(appletElement);
                                    TypeElement jappletElement = elems.getTypeElement("javax.swing.JApplet");
                                    ElementHandle<TypeElement> jappletHandle = ElementHandle.create(jappletElement);
                                    Set<ElementHandle<TypeElement>> appletHandles = classIndex.getElements(appletHandle, kinds, scopes);
                                    for (ElementHandle<TypeElement> elemHandle : appletHandles) {
                                        appletNames.add(elemHandle.getQualifiedName());
                                    }
                                    Set<ElementHandle<TypeElement>> jappletElemHandles = classIndex.getElements(jappletHandle, kinds, scopes);
                                    for (ElementHandle<TypeElement> elemHandle : jappletElemHandles) {
                                        appletNames.add(elemHandle.getQualifiedName());
                                    }
                                }
                                public void cancel() {
                                    
                                }
                            }, true);
                        } catch (Exception e) {
                            
                        }

                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            addElements(appletNames);
                            String appletClassName = evaluator.getProperty(JNLP_APPLET);
                            if (appletClassName != null && appletNames.contains(appletClassName)) {
                                setSelectedItem(appletClassName);
                            }
                        }
                    });
                }
            });
        }
        
        private void addElements(Set<String> elems) {
            for (String elem : elems) {
                addElement(elem);
            }
        }
        
    }
    
    public static class PropertiesTableModel extends AbstractTableModel {
        
        private List<Map<String,String>> properties;
        private String propSuffixes[];
        private String columnNames[];
        
        public PropertiesTableModel(List<Map<String,String>> props, String sfxs[], String clmns[]) {
            if (sfxs.length != clmns.length) {
                throw new IllegalArgumentException();
            }
            properties = props;
            propSuffixes = sfxs;
            columnNames = clmns;
        }
        
        public int getRowCount() {
            return properties.size();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            properties.get(rowIndex).put(propSuffixes[columnIndex], (String) aValue);
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return properties.get(rowIndex).get(propSuffixes[columnIndex]);
        }
        
        public void addRow() {
            Map<String,String> emptyMap = new HashMap<String,String>();
            for (String  suffix : propSuffixes) {
                emptyMap.put(suffix, "");
            }
            properties.add(emptyMap);
        }
        
        public void removeRow(int index) {
            properties.remove(index);
        }

    }
    
    // ----------
    
    private static List<Map<String,String>> readProperties(PropertyEvaluator evaluator, String propPrefix, String[] propSuffixes) {
        
        ArrayList<Map<String,String>> listToReturn = new ArrayList<Map<String,String>>();
        int index = 0;
        while (true) {
            HashMap<String,String> map = new HashMap<String,String>();
            int numProps = 0;
            for (String propSuffix : propSuffixes) {
                String propValue = evaluator.getProperty(propPrefix + index + "." + propSuffix);
                if (propValue != null) {
                    map.put(propSuffix, propValue);
                    numProps++;
                }
            }
            if (numProps == 0) {
                break;
            }
            listToReturn.add(map);
            index++;
        }
        return listToReturn;
        
    }
    
    private static void storeProperties(EditableProperties editableProps, List<Map<String,String>> newProps, String prefix) {
        
        int propGroupIndex = 0;
        // find all properties with the prefix
        Set<String> keys = editableProps.keySet();
        Set<String> keys2Remove = new HashSet<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                keys2Remove.add(key);
            }
        }
        // remove all props with the prefix first
        for (String key2Remove : keys2Remove) {
            editableProps.remove(key2Remove);
        }
        // and now save passed list
        for (Map<String,String> map : newProps) {
            // if all values in the map are empty do not store
            boolean allEmpty = true;
            for (String val : map.values()) {
                if (val != null && !val.equals("")) {
                    allEmpty = false;
                    break;
                }
            }
            if (!allEmpty) {
                for (String key : map.keySet()) {
                    String value = map.get(key);
                    String propName = prefix + propGroupIndex + "." + key;
                    editableProps.setProperty(propName, value);
                }
            }
            propGroupIndex++;
        }
        
    }
    
}
