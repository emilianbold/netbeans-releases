package org.netbeans.modules.iep.project.anttasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.xam.Model;
import org.apache.tools.ant.BuildException;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

public class ProjectValidator {

    private Map myFileNamesToFileInBuildDir = new HashMap();
    private File mSourceDir;
    private File mBuildDir;
    private boolean myIsFoundErrors = false;
    private boolean myAllowBuildWithError = false;
    private GenerateAsaArtifacts mGAsaArtifacts;
    
    public ProjectValidator(GenerateAsaArtifacts gAsaArtifacts, File sourceDir, File buildDir, boolean allowBuildWithError) {
        mGAsaArtifacts = gAsaArtifacts;
        this.mSourceDir = sourceDir;
        this.mBuildDir = buildDir;
        this.myAllowBuildWithError = allowBuildWithError;
    }
    
    public void validate() throws BuildException {
        processSourceDir(this.mSourceDir);
    }
    
    private void processBuildDir(File folder) {
        final File files[] = folder.listFiles(new Util.IEPFileFilter());
        
        if (files == null) return;
        
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            
            if (file.isFile()) {
                this.myFileNamesToFileInBuildDir.put(Util.getRelativePath(this.mBuildDir, file), file);
            } else {
                processBuildDir(file);
            }
        }
    }
    
    private void validateFile(File file) throws BuildException {
      try {
          
        Model model = mGAsaArtifacts.getIEPModel(file);

        if (new Controller(model).ideValidate(file, ValidationType.COMPLETE)) {
          myIsFoundErrors = true;
        }
      }
      catch (Exception e) {
        throw new BuildException(e);
      }
    }

    private boolean isModified(File file) {
        boolean modified = true;
        String relativePath = Util.getRelativePath(this.mSourceDir, file);
        File fileInBuildDir = (File) this.myFileNamesToFileInBuildDir.get(relativePath);

        if (fileInBuildDir != null) {
            if (fileInBuildDir.lastModified() == file.lastModified()) {
                modified = false;
            }
        }
        return modified;
    }

    private void processSourceDir(File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles(new Util.IEPFileFilter());
            
            if (children == null) return;
            
            for (int i = 0; i < children.length; i++) {
                processSourceDir(children[i]);
            }
        } else {
            if (isModified(file)) {
                validateFile(file);
            }
        }
    }
    
    public boolean IsFoundValidationErrors() {
        return myIsFoundErrors;
    }
}
