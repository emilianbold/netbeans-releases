package org.apache.jmeter.module;

import java.io.IOException;
import org.apache.jmeter.module.cookies.JMeterEditable;
import org.apache.jmeter.module.exceptions.InitializationException;
import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

public class JMXTypeDataObject extends MultiDataObject {
  public JMXTypeDataObject(FileObject pf, JMXTypeDataLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    CookieSet cookies = getCookieSet();
    
    cookies.add(new JMeterEditable());
  }
  
  protected Node createNodeDelegate() {
    return new JMXTypeDataNode(this);
  }
  
//  protected DataObject handleCreateFromTemplate(DataFolder dataFolder, String string) throws IOException {
//    try {
//      DataObject retValue;
//      
//      retValue = super.handleCreateFromTemplate(dataFolder, string);
//      String path = FileUtil.toFile(retValue.getPrimaryFile()).getCanonicalPath();
//      JMeterIntegrationEngine.getDefault().externalEdit(path);
//      
//      return retValue;
//    } catch (InitializationException e) {
//      throw new IOException(e.getMessage());
//    }
//  }
}
