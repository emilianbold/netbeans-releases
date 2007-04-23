package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.netbeans.installer.infra.build.ant.utils.Utils;

public class NativeUnzip extends Expand {
    private File dest; //req
    private File source; // req
    
    @Override
    public void setDest(File d) {
        this.dest = d;
        
        super.setDest(d);
    }
    
    @Override
    public void setSrc(File s) {
        this.source = s;
        
        super.setSrc(s);
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            Utils.setProject(getProject());
            log("trying native unzip");
            
            Utils.nativeUnzip(source, dest);
        } catch (IOException e) {
            log("native unzip failed, falling back to java implementation");
            
            Utils.delete(dest);
            super.execute();
        }
    }
}
