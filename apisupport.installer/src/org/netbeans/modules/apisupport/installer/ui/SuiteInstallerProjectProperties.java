package org.netbeans.modules.apisupport.installer.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JToggleButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
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

/**
 *
 * @author avm
 */
public class SuiteInstallerProjectProperties {

    public static final String GENERATE_FOR_WINDOWS = "installer.os.windows";
    public static final String GENERATE_FOR_LINUX = "installer.os.linux";
    public static final String GENERATE_FOR_SOLARIS = "installer.os.solaris";
    public static final String GENERATE_FOR_MAC = "installer.os.macosx";
    public static final String USE_PACK200_COMPRESSION = "installer.pack200.enabled";

    public static final String LICENSE_TYPE = "installer.license.type";
    public static final String LICENSE_FILE = "installer.license.file";
    public static final String LICENSE_TYPE_NO = "no";
    public static final String LICENSE_TYPE_FILE = "file";
    public static final String LICENSE_TYPE_CUSTOM = "custom";
    
    private StoreGroup installerPropGroup = new StoreGroup();
    private Project suiteProject;
    private PropertyEvaluator propEval;
    // JToggleButton.ToggleButtonModel pack200Model;
    JToggleButton.ToggleButtonModel windowsModel;
    JToggleButton.ToggleButtonModel linuxModel;
    JToggleButton.ToggleButtonModel solarisModel;
    JToggleButton.ToggleButtonModel macModel;
    JToggleButton.ToggleButtonModel pack200Model;
    LicenseComboBoxModel licenseModel;

    public SuiteInstallerProjectProperties(Lookup context) {

        suiteProject = context.lookup(Project.class);
        if (suiteProject != null) {
            //   suiteProject.g
            //propEval = AntProjectHelper.getStandardPropertyEvaluator();
            /***********************/
            //UGLIEST HACK to get helper
            AntProjectHelper helper = null;
            try {
                Class<?> suiteClass = suiteProject.getClass();
                Method getHelperMethod = suiteClass.getDeclaredMethod("getHelper");
                helper = (AntProjectHelper) getHelperMethod.invoke(suiteProject);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            //End of hack
            /***********************/
            //Collection c = suiteProject.getLookup().lookupAll(Object.class);//.lookup(AntProjectHelper.class);
            //for (Object i : c) {
            //    System.out.println("###" + i.getClass());
            //}
            propEval = helper.getStandardPropertyEvaluator();
            
            windowsModel = installerPropGroup.createToggleButtonModel(propEval, GENERATE_FOR_WINDOWS);
            linuxModel = installerPropGroup.createToggleButtonModel(propEval, GENERATE_FOR_LINUX);
            solarisModel = installerPropGroup.createToggleButtonModel(propEval, GENERATE_FOR_SOLARIS);
            macModel = installerPropGroup.createToggleButtonModel(propEval, GENERATE_FOR_MAC);

            String osName = System.getProperty("os.name");
            if(osName.startsWith("Windows") && propEval.getProperty(GENERATE_FOR_WINDOWS) == null) {
                windowsModel.setSelected(true);
            }
            if(osName.equals("Linux") && propEval.getProperty(GENERATE_FOR_LINUX) == null) {
                linuxModel.setSelected(true);                
            }
            if((osName.equals("Solaris") || osName.equals("SunOS")) && propEval.getProperty(GENERATE_FOR_SOLARIS) == null) {
                solarisModel.setSelected(true);                
            }
            if((osName.equals("Mac OS X") || osName.equals("Darwin")) && propEval.getProperty(GENERATE_FOR_MAC) == null) {
                macModel.setSelected(true);
            }

            pack200Model = installerPropGroup.createToggleButtonModel(propEval, USE_PACK200_COMPRESSION);

            createLicenseModel();

        }



    }

    private void createLicenseModel() {
        ResourceBundle rb = NbBundle.getBundle(SuiteInstallerProjectProperties.class);
        Enumeration<String> keys = rb.getKeys();
        String prefix = "SuiteInstallerProjectProperties.license.type.";
        List<String> names = new ArrayList<String>();
        List<String> types = new ArrayList<String>();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(prefix)) {
                String type = key.substring(prefix.length());
                String value = NbBundle.getMessage(SuiteInstallerProjectProperties.class, key);
                if (type.equals(LICENSE_TYPE_NO)) {
                    //No License is the first option
                    names.add(0, value);
                    types.add(0, type);
                } else {
                    names.add(value);
                    types.add(type);
                }
            }
        }
        licenseModel = new LicenseComboBoxModel(suiteProject, names, types);

        if (propEval.getProperty(LICENSE_FILE) != null) {
            
            File licenseFile = new File(propEval.getProperty(LICENSE_FILE));
            if(!licenseFile.equals(licenseFile.getAbsoluteFile())) {
                licenseFile = new File(FileUtil.toFile(suiteProject.getProjectDirectory()),
                        propEval.getProperty(LICENSE_FILE));
            }
            
            licenseModel.getNames().add(licenseFile.getAbsolutePath());
            licenseModel.getTypes().add(LICENSE_TYPE_FILE);
            String name = licenseModel.getNames().get(licenseModel.getNames().size() - 1);
            licenseModel.setSelectedItem(name);             
        }
        
        else if (propEval.getProperty(LICENSE_TYPE) != null) {
            int index = licenseModel.getTypes().indexOf(propEval.getProperty(LICENSE_TYPE));
            if (index != -1) {
                licenseModel.setSelectedItem(licenseModel.getNames().get(index));
            }
        }
    }

    public void store() throws IOException {

        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = suiteProject.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        try {
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    installerPropGroup.store(ep);
                    Object lName = licenseModel.getSelectedItem();
                    String licenseName = (lName == null) ? null : (String) lName;
                    if (licenseName != null) {
                        int index = licenseModel.getNames().indexOf(licenseName);
                        if (index != -1) {
                            String type = licenseModel.getTypes().get(index);
                            if(type.equals(LICENSE_TYPE_FILE)) {
                                ep.setProperty(LICENSE_FILE, licenseName.replace("\\", "/"));
                                File f = new File(licenseName);
                                File suiteLocation = FileUtil.toFile(suiteProject.getProjectDirectory());
                                if(!f.equals(f.getAbsoluteFile())) {
                                    f = new File(suiteLocation, licenseName);
                                }

                                File parent = f.getAbsoluteFile().getParentFile();
                                while(parent!=null) {
                                    if(parent.equals(suiteLocation)) {
                                        String parentPath = parent.getAbsolutePath();
                                        String filePath = f.getAbsolutePath();
                                        if(filePath.startsWith(parentPath)) {
                                            ep.setProperty(LICENSE_FILE,
                                                    filePath.substring(parentPath.length() + 1).replace("\\", "/"));
                                        }
                                        break;
                                    }
                                    parent = parent.getParentFile();
                                }
                                ep.remove(LICENSE_TYPE);
                            } else {
                                ep.setProperty(LICENSE_TYPE, type);
                                ep.remove(LICENSE_FILE);
                            }
                        }
                    }
                    //storeRest(ep);
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
}
