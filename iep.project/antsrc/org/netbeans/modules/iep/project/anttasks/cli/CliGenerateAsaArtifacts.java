package org.netbeans.modules.iep.project.anttasks.cli;

import java.io.File;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.netbeans.modules.iep.project.anttasks.GenerateAsaArtifacts;
import org.netbeans.modules.xml.xam.ModelSource;

public class CliGenerateAsaArtifacts extends GenerateAsaArtifacts {

    CliIEPCatalogModel mCatalogModel;
    
    
    public CliGenerateAsaArtifacts() {
        mCatalogModel = CliIEPCatalogModel.getDefault();
    }
    
    @Override
    protected ModelSource createModelSource(File f) throws Exception {
        return mCatalogModel.createModelSource(f, false);
    }   
    
   
    
    
    
}
