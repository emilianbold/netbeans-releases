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

package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.CardLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.common.Properties;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.unit.JdbcListCellRenderer;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Adamek
 */
public class PersistenceUnitPanel extends SectionInnerPanel {
    
    private final PersistenceUnit persistenceUnit;
    private PUDataObject dObj;
    private Project project;
    private boolean isContainerManaged;
    private boolean jpa20=false;

    //jpa2.0 specific
    private final java.lang.String[] validationModes = {"AUTO", "CALLBACK", "NONE"};//NOI18N
    private final java.lang.String[] cachingTypes = {"ALL", "NONE", "ENABLE_SELECTIVE", "DISABLE_SELECTIVE"};//NOI18N
    
    public PersistenceUnitPanel(SectionView view, final PUDataObject dObj,  final PersistenceUnit persistenceUnit) {
        super(view);
        this.dObj=dObj;
        this.jpa20=Persistence.VERSION_2_0.equals(dObj.getPersistence().getVersion());
        this.persistenceUnit=persistenceUnit;
        this.project = FileOwnerQuery.getOwner(this.dObj.getPrimaryFile());
        
        assert project != null : "Could not resolve project for " + dObj.getPrimaryFile(); //NOI18N
        
        if (PersistenceLibrarySupport.getLibrary(persistenceUnit) != null && ProviderUtil.getConnection(persistenceUnit) != null) {
            isContainerManaged = false;
        } else if (persistenceUnit.getJtaDataSource() != null || persistenceUnit.getNonJtaDataSource() != null) {
            isContainerManaged = true;
        } else {
            isContainerManaged = Util.isSupportedJavaEEVersion(project);
        }
        
        initComponents();
        
        PersistenceProviderComboboxHelper comboHelper = new PersistenceProviderComboboxHelper(project);
        if (isContainerManaged){
            comboHelper.connect(providerCombo);
            Provider provider = ProviderUtil.getProvider(persistenceUnit);
            providerCombo.setSelectedItem(provider);
        } else {
            comboHelper.connect(libraryComboBox);
            setSelectedLibrary();
        }
        
        setVisiblePanel();
        initIncludeAllEntities();
        initEntityList();
        
        initDataSource();
        if(jpa20)
        {
            initCache();
            initValidation();
        }
        
        nameTextField.setText(persistenceUnit.getName());
        setTableGeneration();
        handleCmAmSelection();
        
        registerModifiers();
        
    }
    
    /**
     * Registers the components that modify the model. Should be called after
     * these components have been initialized (otherwise the underlying file will
     * be marked  as modified immediately upon opening it).
     */
    private void registerModifiers(){
        if (isContainerManaged){
            addImmediateModifier(dsCombo);
            if(dsCombo.isEditable()) {
                addImmediateModifier((JTextComponent)dsCombo.getEditor().getEditorComponent());
            }
            addImmediateModifier(providerCombo);
            addImmediateModifier(jtaCheckBox);
            
        } else {
            addImmediateModifier(jdbcComboBox);
            addImmediateModifier(libraryComboBox);
        }
        addImmediateModifier(nameTextField);
        addImmediateModifier(ddDropCreate);
        addImmediateModifier(ddCreate);
        addImmediateModifier(ddUnknown);
        addImmediateModifier(includeAllEntities);
        if(jpa20)
        {
            addImmediateModifier(ddAll);
            addImmediateModifier(ddNone);
            addImmediateModifier(ddEnableSelective);
            addImmediateModifier(ddDisableSelective);
            addImmediateModifier(ddDefault);
            addImmediateModifier(ddAuto);
            addImmediateModifier(ddNoValidation);
            addImmediateModifier(ddCallBack);
        }
    }
    
    
    /**
     * Sets which panel (container/application) is visible.
     */
    private void setVisiblePanel(){
        String panelName = isContainerManaged ? "container" : "application";//NOI18N
        ((CardLayout)providerPanel.getLayout()).show(providerPanel, panelName);
        //
        validationStrategyPanel.setVisible(jpa20);
        cachingStrategyPanel.setVisible(jpa20);
    }
    private void initCache(){
        org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
        String caching=pu2.getCaching();
        if(cachingTypes[0].equals(caching))
        {
            ddAll.setSelected(true);
        }
        else if(cachingTypes[1].equals(caching))
        {
            ddNone.setSelected(true);
        }
        else if(cachingTypes[2].equals(caching))
        {
            ddEnableSelective.setSelected(true);
        }
        else if(cachingTypes[3].equals(caching))
        {
            ddDisableSelective.setSelected(true);
        }
        else
        {
            ddDefault.setSelected(true);
        }

    }
    
    private void initValidation(){
        org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
        String validation=pu2.getValidationMode();
        if(validationModes[1].equals(validation))
        {
            ddCallBack.setSelected(true);
        }
        else if(validationModes[2].equals(validation))
        {
            ddNoValidation.setSelected(true);
        }
        else//according to specification auto is default
        {
            ddAuto.setSelected(true);
        }
    }
    
    private void initDataSource(){
        // Fixed enable/disable JTA checkbox based on isContainerManaged, 
        // instead of project environment (SE or not). See issue 147628
        jtaCheckBox.setEnabled(isContainerManaged);
        
        if (isContainerManaged && ProviderUtil.isValidServerInstanceOrNone(project)) {
            String jtaDataSource = persistenceUnit.getJtaDataSource();
            String nonJtaDataSource = persistenceUnit.getNonJtaDataSource();
            
            JPADataSourcePopulator dsPopulator = project.getLookup().lookup(JPADataSourcePopulator.class);
            if (dsPopulator != null){
                dsPopulator.connect(dsCombo);
                addModifier((JTextComponent)dsCombo.getEditor().getEditorComponent(), false);
            }
            
            String jndiName = (jtaDataSource != null ? jtaDataSource : nonJtaDataSource);
            selectDatasource(jndiName);
            
            jtaCheckBox.setSelected(jtaDataSource != null);
            
            String provider = persistenceUnit.getProvider();
            for (int i = 0; i < providerCombo.getItemCount(); i++) {
                Object item = providerCombo.getItemAt(i);
                if (item instanceof Provider){
                    if (((Provider) item).getProviderClass().equals(provider)) {
                        providerCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else if (!isContainerManaged){
            initJdbcComboBox();
            setSelectedLibrary();
            jtaCheckBox.setSelected(false);
        }
    }
    
    private void initJdbcComboBox(){
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < connections.length; i++) {
            jdbcComboBox.addItem(connections[i]);
        }
        setSelectedConnection();
    }
    
    private void initIncludeAllEntities(){
        boolean javaSE = Util.isJavaSE(project);
        includeAllEntities.setEnabled(!javaSE);
        includeAllEntities.setSelected(!javaSE && !persistenceUnit.isExcludeUnlistedClasses());
        includeAllEntities.setText(NbBundle.getMessage(PersistenceUnitPanel.class,
                "LBL_IncludeAllEntities",//NOI18N
                new Object[]{ProjectUtils.getInformation(project).getDisplayName()}));
    }
    
    private void initEntityList(){
        initEntityListControls();
        DefaultListModel listedClassesModel = new DefaultListModel();
        for (String elem : persistenceUnit.getClass2()) {
            listedClassesModel.addElement(elem);
        }
        entityList.setModel(listedClassesModel);
    }

    private void initEntityListControls(){
        boolean enable = !includeAllEntities.isSelected();
        entityList.setEnabled(enable);
        addClassButton.setEnabled(enable);
        removeClassButton.setEnabled(enable);    
    }
    /**
     * Sets selected item in library combo box.
     */
    private void setSelectedLibrary(){
        Provider selected = ProviderUtil.getProvider(persistenceUnit);
        if (selected == null){
            return;
        }
        for(int i = 0; i < libraryComboBox.getItemCount(); i++){
            Object item = libraryComboBox.getItemAt(i);
            Provider provider = (Provider) (item instanceof Provider ? item : null);
            if (provider!= null && provider.equals(selected)){
                libraryComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private Provider getSelectedProvider(){
        if (isContainerManaged){
            return (Provider) providerCombo.getSelectedItem();
        }
        Object selectedItem = libraryComboBox.getSelectedItem();
        Provider provider = (Provider) (selectedItem instanceof Provider ? selectedItem : null);
        if (provider != null) {
            return  provider;
        }
        return ProviderUtil.getProvider(persistenceUnit.getProvider(), project);
    }
    
    /**
     * Selects appropriate table generation radio button.
     */
    private void setTableGeneration(){
        Provider provider = getSelectedProvider();
        // issue 123224. The user can have a persistence.xml in J2SE project without provider specified
        Property tableGeneration = (provider == null) ? null : ProviderUtil.getProperty(persistenceUnit, provider.getTableGenerationPropertyName());
        if (tableGeneration != null){
            if (provider.getTableGenerationCreateValue().equals(tableGeneration.getValue())){
                ddCreate.setSelected(true);
            } else if (provider.getTableGenerationDropCreateValue().equals(tableGeneration.getValue())){
                ddDropCreate.setSelected(true);
            }
        } else {
            ddUnknown.setSelected(true);
        }
        boolean toggle = (provider == null) ? false : provider.supportsTableGeneration();
        
        ddCreate.setEnabled(toggle);
        ddDropCreate.setEnabled(toggle);
        ddUnknown.setEnabled(toggle);
    }
    
    /**
     * Sets the selected item in connection combo box.
     */
    private void setSelectedConnection(){
        DatabaseConnection connection = ProviderUtil.getConnection(persistenceUnit);
        if (connection != null){
            jdbcComboBox.setSelectedItem(connection);
        } else {
            // custom connection (i.e. connection not registered in netbeans)
            Properties props = persistenceUnit.getProperties();
            if (props != null){
                Property[] properties = props.getProperty2();
                String url = null;
                Provider provider = ProviderUtil.getProvider(persistenceUnit);
                for (int i = 0; i < properties.length; i++) {
                    String key = properties[i].getName();
                    if (provider.getJdbcUrl().equals(key)) {
                        url = properties[i].getValue();
                        break;
                    }
                }
                if (url == null) {
                    url = NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_CustomConnection");//NOI18N
                }
                jdbcComboBox.addItem(url);
                jdbcComboBox.setSelectedItem(url);
            }
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source == nameTextField) {
            persistenceUnit.setName((String) value);
        } else if (source == dsCombo){
            setDataSource();
        } else if (source == dsCombo.getEditor().getEditorComponent()) {
            setDataSource((String)value);
        } else if (source == jdbcComboBox){
            if (value instanceof DatabaseConnection){
                ProviderUtil.setDatabaseConnection(persistenceUnit, (DatabaseConnection) value);
            }
        } else if (source == libraryComboBox){
            setProvider();
            setTableGeneration();
        } else if (providerCombo == source){
            String prevProvider = persistenceUnit.getProvider();
            setProvider();
            setDataSource();
            String curProvider = persistenceUnit.getProvider();
            if(prevProvider != null && curProvider != null) {
                ProviderUtil.migrateProperties(prevProvider, curProvider, persistenceUnit);
            }
        } else if (source == ddCreate || source == ddDropCreate || source == ddUnknown){
            ProviderUtil.setTableGeneration(persistenceUnit, getTableGeneration(), ProviderUtil.getProvider(persistenceUnit.getProvider(), project));
        } else if (source == includeAllEntities){
            persistenceUnit.setExcludeUnlistedClasses(!includeAllEntities.isSelected());
        } else if (source == jtaCheckBox){
            setDataSource();
        }
        else if(jpa20)
        {
            if(source==ddAll)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setCaching(cachingTypes[0]);
            }
            else if(source==ddNone)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setCaching(cachingTypes[1]);
            }
            else if(source==ddEnableSelective)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setCaching(cachingTypes[2]);
            }
            else if(source==ddDisableSelective)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setCaching(cachingTypes[3]);
            }
            else if(source==ddDefault)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setCaching(null);
            }
            else if(source==ddAuto)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setValidationMode(null);//can be either cleared or set to AUTO
            }
            else if(source==ddCallBack)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setValidationMode(validationModes[1]);
            }
            else if(source==ddNoValidation)
            {
                org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit pu2=(org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) persistenceUnit;
                pu2.setValidationMode(validationModes[2]);
            }
        }
        performValidation();
    }
    
    private void performValidation(){
        PersistenceValidator validator = new PersistenceValidator(dObj);
        List<Error> result = validator.validate();
        if (!result.isEmpty()){
            getSectionView().getErrorPanel().setError(result.get(0));
        } else {
            getSectionView().getErrorPanel().clearError();
        }
        
    }
    
    private void setDataSource() {
        setDataSource(null);
    }
    
    private void setProvider(){
        
        if (isContainerManaged && providerCombo.getSelectedItem() instanceof Provider) {
            Provider provider = (Provider) providerCombo.getSelectedItem();
            ProviderUtil.removeProviderProperties(persistenceUnit);
            
            if (!(provider instanceof DefaultProvider)) {
                persistenceUnit.setProvider(provider.getProviderClass());
                setTableGeneration();
            }
            ProviderUtil.makePortableIfPossible(project, persistenceUnit);
            
        } else if (libraryComboBox.getSelectedItem() instanceof Provider){
            Provider provider = (Provider) libraryComboBox.getSelectedItem();
            ProviderUtil.removeProviderProperties(persistenceUnit);
            if (!(provider instanceof DefaultProvider)) {
                ProviderUtil.setProvider(persistenceUnit, provider, getSelectedConnection(), getTableGeneration());
            }
        }
    }
    
    private void setDataSource(String name) {
        
        String jndiName = name;
        
        if (jndiName == null) {
            int itemIndex = dsCombo.getSelectedIndex();
            Object item = dsCombo.getSelectedItem();
            JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
            JPADataSource jpaDS = dsProvider != null ? dsProvider.toJPADataSource(item) : null;
            if (jpaDS != null){
                jndiName = jpaDS.getJndiName();
            } else if (itemIndex == -1 && item != null){ // user input
                jndiName = item.toString();
            }
        }
        
        if (jndiName == null){
            return;
        }
        
        if (isJta()){
            persistenceUnit.setJtaDataSource(jndiName);
            persistenceUnit.setNonJtaDataSource(null);
        } else {
            persistenceUnit.setJtaDataSource(null);
            persistenceUnit.setNonJtaDataSource(jndiName);
        }
    }
    
    private boolean isJta(){
        return jtaCheckBox.isEnabled() && jtaCheckBox.isSelected();
    }
    
    /**
     *@return selected table generation strategy.
     */
    private String getTableGeneration(){
        if (ddCreate.isSelected()){
            return Provider.TABLE_GENERATION_CREATE;
        } else if (ddDropCreate.isSelected()){
            return Provider.TABLE_GENERATION_DROPCREATE;
        } else {
            return Provider.TABLE_GENERATTION_UNKOWN;
        }
    }
    
    private DatabaseConnection getSelectedConnection(){
        DatabaseConnection connection = null;
        if (jdbcComboBox.getSelectedItem() instanceof DatabaseConnection){
            connection = (DatabaseConnection) jdbcComboBox.getSelectedItem();
        }
        return connection;
        
    }
    
    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameTextField == source) {
            nameTextField.setText(persistenceUnit.getName());
        } else if (dsCombo.getEditor().getEditorComponent() == source){
            String jndiName = (isJta() ? persistenceUnit.getJtaDataSource() : persistenceUnit.getNonJtaDataSource());
            selectDatasource(jndiName);
        }
    }
    
    private void selectDatasource(String jndiName) {
        
        Object item = findDatasource(jndiName);
        dsCombo.setSelectedItem(item);
        if (dsCombo.getEditor() != null) { // item must be set in the editor
            dsCombo.configureEditor(dsCombo.getEditor(), item);
        }
    }
    
    private Object findDatasource(String jndiName) {
        
        if (jndiName != null) {
            int nItems = dsCombo.getItemCount();
            for (int i = 0; i < nItems; i++) {
                Object item = dsCombo.getItemAt(i);
                if (item instanceof JPADataSource && jndiName.equals(((JPADataSource)item).getJndiName())) {
                    return (JPADataSource)item;
                }
            }
        }
        
        return jndiName;
    }
    
    @Override
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("name".equals(errorId)) {//NOI18N
            return nameTextField;
        }
        return null;
    }
    
    private void handleCmAmSelection() {
        boolean isCm = isContainerManaged;
        datasourceLabel.setEnabled(isCm);
        dsCombo.setEnabled(isCm);
        jtaCheckBox.setEnabled(isCm);
        libraryLabel.setEnabled(!isCm);
        libraryComboBox.setEnabled(!isCm);
        jdbcLabel.setEnabled(!isCm);
        jdbcComboBox.setEnabled(!isCm);
        setTableGeneration();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        providerPanel = new javax.swing.JPanel();
        providerDataSourcePanel = new javax.swing.JPanel();
        datasourceLabel = new javax.swing.JLabel();
        jtaCheckBox = new javax.swing.JCheckBox();
        dsCombo = new javax.swing.JComboBox();
        providerCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        providerJdbcPanel = new javax.swing.JPanel();
        libraryLabel = new javax.swing.JLabel();
        jdbcLabel = new javax.swing.JLabel();
        libraryComboBox = new javax.swing.JComboBox();
        jdbcComboBox = new javax.swing.JComboBox();
        tableGenerationPanel = new javax.swing.JPanel();
        tableGenerationLabel = new javax.swing.JLabel();
        ddCreate = new javax.swing.JRadioButton();
        ddDropCreate = new javax.swing.JRadioButton();
        ddUnknown = new javax.swing.JRadioButton();
        includeAllPanel = new javax.swing.JPanel();
        includeAllEntities = new javax.swing.JCheckBox();
        entityClassesPanel = new javax.swing.JPanel();
        entityScrollPane = new javax.swing.JScrollPane();
        entityList = new javax.swing.JList();
        addClassButton = new javax.swing.JButton();
        removeClassButton = new javax.swing.JButton();
        includeEntitiesLabel = new javax.swing.JLabel();
        cachingStrategyPanel = new javax.swing.JPanel();
        cachingStrategyLabel = new javax.swing.JLabel();
        ddAll = new javax.swing.JRadioButton();
        ddNone = new javax.swing.JRadioButton();
        ddEnableSelective = new javax.swing.JRadioButton();
        ddDisableSelective = new javax.swing.JRadioButton();
        ddDefault = new javax.swing.JRadioButton();
        validationStrategyPanel = new javax.swing.JPanel();
        validationStrategyLabel = new javax.swing.JLabel();
        ddAuto = new javax.swing.JRadioButton();
        ddCallBack = new javax.swing.JRadioButton();
        ddNoValidation = new javax.swing.JRadioButton();

        namePanel.setOpaque(false);

        nameLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_UnitName")); // NOI18N

        org.jdesktop.layout.GroupLayout namePanelLayout = new org.jdesktop.layout.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(nameLabel)
                .add(32, 32, 32)
                .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 440, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(203, Short.MAX_VALUE))
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, namePanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        providerPanel.setOpaque(false);
        providerPanel.setLayout(new java.awt.CardLayout());

        providerDataSourcePanel.setOpaque(false);

        datasourceLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_DatasourceName")); // NOI18N

        jtaCheckBox.setSelected(true);
        jtaCheckBox.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_JTA")); // NOI18N
        jtaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dsCombo.setEditable(true);

        providerCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle"); // NOI18N
        jLabel3.setText(bundle.getString("LBL_Provider")); // NOI18N

        org.jdesktop.layout.GroupLayout providerDataSourcePanelLayout = new org.jdesktop.layout.GroupLayout(providerDataSourcePanel);
        providerDataSourcePanel.setLayout(providerDataSourcePanelLayout);
        providerDataSourcePanelLayout.setHorizontalGroup(
            providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(providerDataSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(providerDataSourcePanelLayout.createSequentialGroup()
                        .add(providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(datasourceLabel))
                        .add(43, 43, 43)
                        .add(providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(providerCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(dsCombo, 0, 436, Short.MAX_VALUE)))
                    .add(jtaCheckBox))
                .add(79, 79, 79))
        );
        providerDataSourcePanelLayout.setVerticalGroup(
            providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(providerDataSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(providerCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providerDataSourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(datasourceLabel)
                    .add(dsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtaCheckBox)
                .add(48, 48, 48))
        );

        providerPanel.add(providerDataSourcePanel, "container");

        providerJdbcPanel.setOpaque(false);

        libraryLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_PersistenceLibrary")); // NOI18N

        jdbcLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_JdbcConnection")); // NOI18N

        libraryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryComboBoxActionPerformed(evt);
            }
        });

        jdbcComboBox.setRenderer(new JdbcListCellRenderer());

        org.jdesktop.layout.GroupLayout providerJdbcPanelLayout = new org.jdesktop.layout.GroupLayout(providerJdbcPanel);
        providerJdbcPanel.setLayout(providerJdbcPanelLayout);
        providerJdbcPanelLayout.setHorizontalGroup(
            providerJdbcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(providerJdbcPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(providerJdbcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(libraryLabel)
                    .add(jdbcLabel))
                .add(47, 47, 47)
                .add(providerJdbcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jdbcComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(libraryComboBox, 0, 442, Short.MAX_VALUE))
                .addContainerGap(212, Short.MAX_VALUE))
        );
        providerJdbcPanelLayout.setVerticalGroup(
            providerJdbcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(providerJdbcPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(providerJdbcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(libraryLabel)
                    .add(libraryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providerJdbcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jdbcLabel)
                    .add(jdbcComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        providerPanel.add(providerJdbcPanel, "application");

        tableGenerationPanel.setOpaque(false);

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle"); // NOI18N
        tableGenerationLabel.setText(bundle1.getString("LBL_TableGeneration")); // NOI18N

        buttonGroup2.add(ddCreate);
        ddCreate.setText(bundle1.getString("LBL_Create")); // NOI18N
        ddCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup2.add(ddDropCreate);
        ddDropCreate.setText(bundle1.getString("LBL_DropCreate")); // NOI18N
        ddDropCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup2.add(ddUnknown);
        ddUnknown.setText(bundle1.getString("LBL_None")); // NOI18N
        ddUnknown.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout tableGenerationPanelLayout = new org.jdesktop.layout.GroupLayout(tableGenerationPanel);
        tableGenerationPanel.setLayout(tableGenerationPanelLayout);
        tableGenerationPanelLayout.setHorizontalGroup(
            tableGenerationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableGenerationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(tableGenerationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddCreate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddDropCreate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddUnknown)
                .addContainerGap(381, Short.MAX_VALUE))
        );
        tableGenerationPanelLayout.setVerticalGroup(
            tableGenerationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableGenerationPanelLayout.createSequentialGroup()
                .add(tableGenerationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddCreate, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(ddDropCreate)
                    .add(ddUnknown)
                    .add(tableGenerationLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        includeAllPanel.setOpaque(false);

        includeAllEntities.setText(bundle1.getString("LBL_IncludeAllEntities")); // NOI18N
        includeAllEntities.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeAllEntities.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeAllEntitiesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout includeAllPanelLayout = new org.jdesktop.layout.GroupLayout(includeAllPanel);
        includeAllPanel.setLayout(includeAllPanelLayout);
        includeAllPanelLayout.setHorizontalGroup(
            includeAllPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(includeAllPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(includeAllEntities)
                .addContainerGap(544, Short.MAX_VALUE))
        );
        includeAllPanelLayout.setVerticalGroup(
            includeAllPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(includeAllPanelLayout.createSequentialGroup()
                .add(includeAllEntities)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        entityClassesPanel.setOpaque(false);

        entityScrollPane.setViewportView(entityList);

        addClassButton.setText(bundle1.getString("LBL_AddClasses")); // NOI18N
        addClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClassButtonActionPerformed(evt);
            }
        });

        removeClassButton.setText(bundle1.getString("LBL_RemoveClass")); // NOI18N
        removeClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClassButtonActionPerformed(evt);
            }
        });

        includeEntitiesLabel.setText(bundle1.getString("LBL_IncludeEntityClasses")); // NOI18N

        org.jdesktop.layout.GroupLayout entityClassesPanelLayout = new org.jdesktop.layout.GroupLayout(entityClassesPanel);
        entityClassesPanel.setLayout(entityClassesPanelLayout);
        entityClassesPanelLayout.setHorizontalGroup(
            entityClassesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(entityClassesPanelLayout.createSequentialGroup()
                .add(entityClassesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(entityClassesPanelLayout.createSequentialGroup()
                        .add(entityScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 449, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(entityClassesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(removeClassButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .add(addClassButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                        .add(20, 20, 20))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, includeEntitiesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE))
                .addContainerGap())
        );
        entityClassesPanelLayout.setVerticalGroup(
            entityClassesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(entityClassesPanelLayout.createSequentialGroup()
                .add(includeEntitiesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(entityClassesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(entityClassesPanelLayout.createSequentialGroup()
                        .add(addClassButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeClassButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE)
                        .add(44, 44, 44))
                    .add(entityScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cachingStrategyPanel.setOpaque(false);

        cachingStrategyLabel.setText(bundle1.getString("LBL_CachingStrategy")); // NOI18N

        buttonGroup1.add(ddAll);
        ddAll.setText(bundle1.getString("LBL_All")); // NOI18N
        ddAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup1.add(ddNone);
        ddNone.setText(bundle1.getString("LBL_None")); // NOI18N
        ddNone.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup1.add(ddEnableSelective);
        ddEnableSelective.setText(bundle1.getString("LBL_EnableSelective")); // NOI18N
        ddEnableSelective.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup1.add(ddDisableSelective);
        ddDisableSelective.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_DisableSelective")); // NOI18N

        buttonGroup1.add(ddDefault);
        ddDefault.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_Default")); // NOI18N

        org.jdesktop.layout.GroupLayout cachingStrategyPanelLayout = new org.jdesktop.layout.GroupLayout(cachingStrategyPanel);
        cachingStrategyPanel.setLayout(cachingStrategyPanelLayout);
        cachingStrategyPanelLayout.setHorizontalGroup(
            cachingStrategyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cachingStrategyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(cachingStrategyLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddAll)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddNone)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddEnableSelective)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddDisableSelective)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddDefault)
                .addContainerGap(278, Short.MAX_VALUE))
        );
        cachingStrategyPanelLayout.setVerticalGroup(
            cachingStrategyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cachingStrategyPanelLayout.createSequentialGroup()
                .add(cachingStrategyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(ddNone)
                    .add(ddEnableSelective)
                    .add(cachingStrategyLabel)
                    .add(ddDisableSelective)
                    .add(ddDefault))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        validationStrategyPanel.setOpaque(false);

        validationStrategyLabel.setText(bundle1.getString("LBL_ValidationStrategy")); // NOI18N

        buttonGroup3.add(ddAuto);
        ddAuto.setSelected(true);
        ddAuto.setText(bundle1.getString("LBL_Auto")); // NOI18N
        ddAuto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup3.add(ddCallBack);
        ddCallBack.setText(bundle1.getString("LBL_CallBack")); // NOI18N
        ddCallBack.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup3.add(ddNoValidation);
        ddNoValidation.setText(bundle1.getString("LBL_None")); // NOI18N
        ddNoValidation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout validationStrategyPanelLayout = new org.jdesktop.layout.GroupLayout(validationStrategyPanel);
        validationStrategyPanel.setLayout(validationStrategyPanelLayout);
        validationStrategyPanelLayout.setHorizontalGroup(
            validationStrategyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationStrategyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(validationStrategyLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddAuto)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddCallBack)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddNoValidation)
                .addContainerGap(493, Short.MAX_VALUE))
        );
        validationStrategyPanelLayout.setVerticalGroup(
            validationStrategyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationStrategyPanelLayout.createSequentialGroup()
                .add(validationStrategyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddAuto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(ddCallBack)
                    .add(ddNoValidation)
                    .add(validationStrategyLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, providerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 842, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, includeAllPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tableGenerationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, namePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(entityClassesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, cachingStrategyPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, validationStrategyPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(namePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableGenerationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationStrategyPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cachingStrategyPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(includeAllPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(entityClassesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    private void removeClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClassButtonActionPerformed
        Object[] values = entityList.getSelectedValues();
        for (Object value : values) {
            dObj.removeClass(persistenceUnit, (String)value);
            ((DefaultListModel)entityList.getModel()).removeElement(value);
        }
    }//GEN-LAST:event_removeClassButtonActionPerformed
    
    private void addClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClassButtonActionPerformed
        EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(dObj.getPrimaryFile());
        if (entityClassScope == null) {
            return;
        }
        String[] existingClassNames = persistenceUnit.getClass2();
        Set<String> ignoreClassNames = new HashSet<String>(Arrays.asList(existingClassNames));
        List<String> addedClassNames = AddEntityDialog.open(entityClassScope, ignoreClassNames);
        for (String entityClass : addedClassNames) {
            if (dObj.addClass(persistenceUnit, entityClass)){
                ((DefaultListModel)entityList.getModel()).addElement(entityClass);
            }
        }
    }//GEN-LAST:event_addClassButtonActionPerformed
    
    private void libraryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryComboBoxActionPerformed
        //        setProvider();
        ////        setSelectedLibrary();
        //        setTableGeneration();
    }//GEN-LAST:event_libraryComboBoxActionPerformed

    private void includeAllEntitiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeAllEntitiesActionPerformed
        initEntityListControls();
    }//GEN-LAST:event_includeAllEntitiesActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClassButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JLabel cachingStrategyLabel;
    private javax.swing.JPanel cachingStrategyPanel;
    private javax.swing.JLabel datasourceLabel;
    private javax.swing.JRadioButton ddAll;
    private javax.swing.JRadioButton ddAuto;
    private javax.swing.JRadioButton ddCallBack;
    private javax.swing.JRadioButton ddCreate;
    private javax.swing.JRadioButton ddDefault;
    private javax.swing.JRadioButton ddDisableSelective;
    private javax.swing.JRadioButton ddDropCreate;
    private javax.swing.JRadioButton ddEnableSelective;
    private javax.swing.JRadioButton ddNoValidation;
    private javax.swing.JRadioButton ddNone;
    private javax.swing.JRadioButton ddUnknown;
    private javax.swing.JComboBox dsCombo;
    private javax.swing.JPanel entityClassesPanel;
    private javax.swing.JList entityList;
    private javax.swing.JScrollPane entityScrollPane;
    private javax.swing.JCheckBox includeAllEntities;
    private javax.swing.JPanel includeAllPanel;
    private javax.swing.JLabel includeEntitiesLabel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox jdbcComboBox;
    private javax.swing.JLabel jdbcLabel;
    private javax.swing.JCheckBox jtaCheckBox;
    private javax.swing.JComboBox libraryComboBox;
    private javax.swing.JLabel libraryLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JComboBox providerCombo;
    private javax.swing.JPanel providerDataSourcePanel;
    private javax.swing.JPanel providerJdbcPanel;
    private javax.swing.JPanel providerPanel;
    private javax.swing.JButton removeClassButton;
    private javax.swing.JLabel tableGenerationLabel;
    private javax.swing.JPanel tableGenerationPanel;
    private javax.swing.JLabel validationStrategyLabel;
    private javax.swing.JPanel validationStrategyPanel;
    // End of variables declaration//GEN-END:variables
    
}

