/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.management.api;

import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * Represents D-Light Configuration. Register your D-Light in D-Light filesystem.
 * Use the following example:
 * <pre>
 * &lt;filesystem&gt;
  &lt;folder name="DLight"&gt;
    &lt;folder name="Configurations"&gt;
      &lt;folder name="MyFavoriteConfiguration"&gt;
        &lt;folder name="KnownToolsConfigurationProviders"&gt;
          &lt;file name="MyDLightToolConfigurationProvider.shadow"&gt;
            &lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/MyDLightToolConfigurationProvider.instance"/&gt;
          &lt;/file&gt;
          &lt;file name="MemoryToolConfigurationProvider.shadow"&gt;
            &lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/MemoryToolConfigurationProvider.instance"/&gt;
          &lt;/file&gt;
          &lt;file name="SyncToolConfigurationProvider.shadow"&gt;
            &lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/SyncToolConfigurationProvider.instance"/&gt;
          &lt;/file&gt;
        &lt;/folder&gt;
      &lt;/folder&gt;
    &lt;/folder&gt;
  &lt;/folder&gt;
&lt;/filesystem&gt;
</pre>
 */
public final class DLightConfiguration {
  private final FileObject rootFolder;
  private final ToolsConfiguration toolsConfiguration;


 static DLightConfiguration create(FileObject configurationRoot){
   return new DLightConfiguration(configurationRoot);
 }

 static DLightConfiguration createDefault(){
    FileSystem fs = Repository.getDefault().getDefaultFileSystem();
    FileObject toolConfigurations =  fs.getRoot().getFileObject("DLight/ToolConfigurationProviders");//NOI18N
    return new DLightConfiguration(fs.getRoot().getFileObject("DLight"),ToolsConfiguration.createDefault(toolConfigurations));//NOI18N
 }
  private DLightConfiguration(FileObject configurationRoot) {
    this(configurationRoot, ToolsConfiguration.create(configurationRoot));
  }

  private DLightConfiguration(FileObject configurationRoot, ToolsConfiguration  toolsConfiguration){
    this.toolsConfiguration = toolsConfiguration;
    this.rootFolder = configurationRoot;
  }

   List<DLightTool> getToolsSet(){
    return toolsConfiguration.getToolsSet();
  }

  String getConfigurationName(){
    return rootFolder.getName();
  }


}
