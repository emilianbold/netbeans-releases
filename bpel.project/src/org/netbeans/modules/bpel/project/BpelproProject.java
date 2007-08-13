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
package org.netbeans.modules.bpel.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileLock;
import org.netbeans.modules.compapp.projects.base.spi.JbiArtifactProvider;
import org.netbeans.modules.compapp.projects.base.ui.IcanproCustomizerProvider;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.bpel.project.ui.IcanproLogicalViewProvider;
import org.netbeans.modules.compapp.projects.base.queries.IcanproProjectEncodingQueryImpl;
import org.netbeans.modules.compapp.projects.base.ui.IcanproXmlCustomizerProvider;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author Chris Webster
 */
public final class BpelproProject implements Project, AntProjectListener, ProjectPropertyProvider {
    private static final Icon PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/bpel/project/resources/bpelProject.png")); // NOI18N
    public static final String SOURCES_TYPE_BPELPRO = "BIZPRO";
    public static final String ARTIFACT_TYPE_JBI_ASA = "CAPS.asa";
    
    public static final String MODULE_INSTALL_NAME = "modules/org-netbeans-modules-bpel-project.jar";
    public static final String MODULE_INSTALL_CBN = "org.netbeans.modules.bpel.project";
    public static final String MODULE_INSTALL_DIR = "module.install.dir";
    
    private static final Logger LOG = Logger.getLogger(BpelproProject.class.getName());

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final BpelSourcesRegistryHelper sourcesRegistryHelper;

    private ProjectCloseSupport projectCloseSupport;
    
    public BpelproProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);

        sourcesRegistryHelper = new BpelSourcesRegistryHelper(this);
        projectCloseSupport = new ProjectCloseSupport();
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    public String toString() {
        return "BpelproProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        return helper.getStandardPropertyEvaluator();
    }
    
    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(helper.getStandardPropertyEvaluator(),
                new String[] {"${src.dir}/*.java"}, // NOI18N
                new String[] {"${build.classes.dir}/*.class"} // NOI18N
        );
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(IcanproCustomizerProvider.class, "LBL_Node_EJBModule"); //NOI18N
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(IcanproCustomizerProvider.class, "LBL_Node_Sources"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+IcanproProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", srcJavaLabel, /*XXX*/null, null);
        
        sourcesHelper.addTypedSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", SOURCES_TYPE_BPELPRO, srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}",
                org.netbeans.modules.xml.catalogsupport.ProjectConstants.SOURCES_TYPE_XML,
                srcJavaLabel, null, null);
        
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            helper,
            spp,
            new BpelproActionProvider( this, helper, refHelper ),
            new IcanproLogicalViewProvider(this, helper, evaluator(), spp, refHelper),
//            new BpelProjectCustomizerProvider(this),
//            new IcanproCustomizerProvider(this, helper, refHelper, 
//                    BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE),
            new IcanproXmlCustomizerProvider(this, helper, refHelper,
                    BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE),
            
            
            new JbiArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(this),
            new BpelProjectOperations(this),
            fileBuilt,
            new RecommendedTemplatesImpl(),
            refHelper,
            new IcanproProjectEncodingQueryImpl(evaluator()),
            sourcesHelper.createSources(),
            helper.createSharabilityQuery(evaluator(),
                    new String[] {"${"+IcanproProjectProperties.SOURCE_ROOT+"}"},
                    new String[] {
                "${"+IcanproProjectProperties.BUILD_DIR+"}",
                "${"+IcanproProjectProperties.DIST_DIR+"}"}
            ),
            new DefaultProjectCatalogSupport(this, helper, refHelper),
        });
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }
    
    public void propertiesChanged(AntProjectEvent ev) {}
    
    String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator().getProperty("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    private static long brokenAlertLastTime = 0;
    private static boolean brokenAlertShown = false;
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    @SuppressWarnings("unchecked") // NOI18N
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    public void addProjectCloseListener(ProjectCloseListener listener) {
        projectCloseSupport.addProjectCloseListener(listener);
    } 
    
    public void removeProjectCloseListener(ProjectCloseListener listener) {
        projectCloseSupport.removeProjectCloseListener(listener);
    } 

    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return BpelproProject.this.getName();
        }
        
        public String getDisplayName() {
            return BpelproProject.this.getName();
        }
        
        public Icon getIcon() {
            return PROJECT_ICON;
        }
        
        public Project getProject() {
            return BpelproProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    BpelproProject.class.getResource("resources/build-impl.xsl"),
                    false);
            genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    BpelproProject.class.getResource("resources/build.xsl"),
                    false);
        }
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
      ProjectOpenedHookImpl(Project project) {}
      
      @SuppressWarnings("unchecked") // NOI18N
      protected void projectOpened() {
          try {
              genFilesHelper.refreshBuildScript(
                      GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                      BpelproProject.class.getResource("resources/build-impl.xsl"),
                      true);
              genFilesHelper.refreshBuildScript(
                      getBuildXmlName(),
                      BpelproProject.class.getResource("resources/build.xsl"),
                      true);
          } catch (IOException e) {
              ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
          }
          ProjectManager.mutex().writeAccess(new Mutex.Action() {
              public Object run() {
                  EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                  ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                  File f = InstalledFileLocator.getDefault().locate(MODULE_INSTALL_NAME, MODULE_INSTALL_CBN, false);

                  if (f != null) {
                      ep.setProperty(MODULE_INSTALL_DIR, f.getParentFile().getPath());
                  }
                  helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

                    
                  // Add project encoding for old projects
                  EditableProperties projectEP = helper.getProperties(
                                  AntProjectHelper.PROJECT_PROPERTIES_PATH);
                  if (projectEP.getProperty(IcanproProjectProperties.SOURCE_ENCODING) == null) {
                      projectEP.setProperty(IcanproProjectProperties.SOURCE_ENCODING,
                              // FIXME: maybe we should use Charset.defaultCharset() instead?
                              // See comments in IcanproProjectEncodingQueryImpl.java
                              FileEncodingQuery.getDefaultEncoding().name());
                  }            
                  helper.putProperties(
                          AntProjectHelper.PROJECT_PROPERTIES_PATH, projectEP);

                  try {
                      ProjectManager.getDefault().saveProject(BpelproProject.this);
                  } catch (IOException e) {
                      ErrorManager.getDefault().notify(e);
                  }
                  return null;
              }
          });
          if (IcanproLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
              BrokenReferencesSupport.showAlert();
          }
            
          checkEncoding();

          sourcesRegistryHelper.register();
          addListenerOnCatalog();
      }
      
      private void checkEncoding() {
        // TODO m
        // Should we show ErrorManager dialog to inform user in case wrong encoding parameter ?
        String prop = eval.getProperty(IcanproProjectProperties.SOURCE_ENCODING);
        if (prop != null) {
            try {
                Charset c = Charset.forName(prop);
            } catch (IllegalCharsetNameException e) {
                //Broken property, log & ignore
                LOG.warning("Illegal charset: " + prop+ " in project: " + // NOI18N
                        getProjectDirectory()); 
            } catch (UnsupportedCharsetException e) {
                //todo: Needs UI notification like broken references.
                LOG.warning("Unsupported charset: " + prop+ " in project: " + // NOI18N
                        getProjectDirectory()); 
            }
        }            
      }
      
      // vlv # 96026
      private void addListenerOnCatalog() {
//System.out.println();
//System.out.println();
//System.out.println("ADD LISTENER");
        if (myCatalogListener != null) {
          return;
        }
        CatalogWriteModel catalog = getCatalog();

        if (catalog == null) {
          return;
        }
        myCatalogListener = new CatalogListener(getProjectDirectory(), catalog);
        catalog.getCatalogFileObject().addFileChangeListener(myCatalogListener);
/* vlv # 111020
//System.out.println("== SAVE ==");
        // save catalog.xml
        FileObject fileObject = catalog.getCatalogFileObject();

        if (fileObject == null) {
          return;
        }
        try {
          DataObject dataObject = DataObject.find(fileObject);
    
          if (dataObject == null) {
            return;
          }
          SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);

          if (saveCookie == null) {
            return;
          }
          saveCookie.save();
        }
        catch (DataObjectNotFoundException e) {
//          e.printStackTrace();
        }
        catch (IOException e) {
//          e.printStackTrace();
        }
        */
      }

      // vlv
      private CatalogWriteModel getCatalog() {
        try {
          return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(getProjectDirectory());
        }
        catch (CatalogModelException e) {
          return null;
        }
      }

      private CatalogListener myCatalogListener;

      protected void projectClosed() {
        if (myCatalogListener != null) {
          CatalogWriteModel catalog = getCatalog();

          if (catalog != null) {
            catalog.getCatalogFileObject().removeFileChangeListener(myCatalogListener);
          }
          myCatalogListener = null;
        }
        try {
          ProjectManager.getDefault().saveProject(BpelproProject.this);
        }
        catch (IOException e) {
          ErrorManager.getDefault().notify(e);
        }
        sourcesRegistryHelper.unregister();
        projectCloseSupport.fireProjectClosed();
      }
    }

    private final class JbiArtifactProviderImpl implements JbiArtifactProvider {
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(BpelproProject.ARTIFACT_TYPE_JBI_ASA + ":" +
                        helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SE_TYPE),
                        IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                        helper.getStandardPropertyEvaluator(), "dist_se", "clean"), // NOI18N
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR,
                        IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                        helper.getStandardPropertyEvaluator(), "dist_se", "clean"), // NOI18N
            };
        }
        
        public String getJbiServiceAssemblyType() {
            return helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SE_TYPE);
        }
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        private static final String[] TYPES = new String[] {
            "SOA",         // NOI18N
            "XML",         // NOI18N
            "simple-files" // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/SOA/Process.bpel",        // NOI18N
            "Templates/XML/retrieveXMLResource", // NOI18N
            "Templates/XML/WSDL.wsdl",           // NOI18N
        };
        
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }
    
    public IcanproProjectProperties getProjectProperties() {
      return new IcanproProjectProperties(this, helper, refHelper, BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE );
    }

    // vlv
    private static class CatalogListener implements FileChangeListener {
      public CatalogListener(FileObject project, CatalogWriteModel catalog) {
//System.out.println();
//System.out.println();
//System.out.println("NEW CATALOG LISTENER " + project);
        myCatalog = catalog;
        myProject = project;
        myEntries = null;
        // vlv # 111020
        updateEntries();
      }

      public void fileChanged(FileEvent event) {
        updateEntries();
      }

      public void fileAttributeChanged(FileAttributeEvent event) {
      }

      public void fileDataCreated(FileEvent event) {
      }

      public void fileDeleted(FileEvent event) {
      }

      public void fileFolderCreated(FileEvent event) {
      }

      public void fileRenamed(FileRenameEvent event) {
      }

      private void updateEntries() {
        removeEntries();

        myEntries = new HashMap<FileObject,FileListener>();
        Collection<CatalogEntry> entries = myCatalog.getCatalogEntries();
//System.out.println();
//System.out.println();
//System.out.println("UPDATE CATALOG");

        for (CatalogEntry entry : entries) {
//System.out.println("see");
          String name = getFileName(entry.getSource());
//System.out.println("        name: " + name);
//System.out.println("        file: " + new File(name));
          FileObject source = FileUtil.toFileObject(FileUtil.normalizeFile(new File(name)));

          if (source == null) {
            continue;
          }
//System.out.println("  source: " + source.getNameExt());
          FileObject target = myProject.getFileObject(entry.getTarget());

          if (target == null) {
            continue;
          }
//System.out.println("  target: " + target.getNameExt());
          FileListener listener = new FileListener(target);
          source.addFileChangeListener(listener);

          myEntries.put(source, listener);
        }
      }

      private void removeEntries() {
        if (myEntries == null) {
          return;
        }
//System.out.println();
//System.out.println();
//System.out.println("CLEAR CATALOG");
        Set<FileObject> files = myEntries.keySet();

        for (FileObject file : files) {
          file.removeFileChangeListener(myEntries.get(file));
//System.out.println("  file: " + file.getNameExt());
        }
        myEntries.clear();
      }

      private String getFileName(String file) {
        file = file.replaceAll("%20", " ");

        if (file.startsWith("file:")) { // NOI18N
          file = file.substring(5);
        }
        return file.replace("\\", "/"); // NOI18N
      }

      private FileObject myProject;
      private CatalogWriteModel myCatalog;
      private HashMap<FileObject,FileListener> myEntries;
    }

    // vlv
    private static class FileListener implements FileChangeListener {
      public FileListener(FileObject target) {
        myTarget = target;
        myIsValid = true;
      }

      public void fileChanged(FileEvent event) {
        FileObject source = event.getFile();
//System.out.println();
//System.out.println();
//System.out.println("FILE CHANGED: " + myIsValid + " " + myTarget.isValid() + " " + source.getNameExt());
//System.out.println();
//System.out.println();
        if ( !myIsValid) {
          return;
        }
        if ( !myTarget.isValid()) {
          return;
        }
        InputStream input = null;
        OutputStream output = null;
        FileLock lock = null;
        
        try {
          input = source.getInputStream();
          lock = myTarget.lock();
          output = myTarget.getOutputStream(lock);
          FileUtil.copy(input, output);
        }
        catch (FileNotFoundException e) {
//          e.printStackTrace();
        }
        catch (IOException e) {
//          e.printStackTrace();
        }
        finally {
          if (lock != null) {
            lock.releaseLock();
          }
          try {
            if (output != null) {
              output.close();
            }
            if (input != null) {
              input.close();
            }
          }
          catch (IOException e) {
//            e.printStackTrace();
          }
        }
      }

      public void fileAttributeChanged(FileAttributeEvent event) {
      }

      public void fileDataCreated(FileEvent event) {
      }

      public void fileDeleted(FileEvent event) {
        myIsValid = false;
      }

      public void fileFolderCreated(FileEvent event) {
      }

      public void fileRenamed(FileRenameEvent event) {
        myIsValid = false;
      }

      private boolean myIsValid;
      private FileObject myTarget;
    }
}
