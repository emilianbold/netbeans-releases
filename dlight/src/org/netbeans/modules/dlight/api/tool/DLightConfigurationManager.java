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

package org.netbeans.modules.dlight.api.tool;

import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class is manager for DLight Configuration
 */
public final class DLightConfigurationManager {
  private static DLightConfigurationManager instance = null;
  private String selectedConfigurationName = null;


  private  DLightConfigurationManager() {
  }
  
  private final FileObject getToolsFSRoot() {
    FileObject fsRoot  = FileUtil.getConfigRoot();
    return fsRoot.getFileObject(getToolsFSRootPath());
  }

  private final String getToolsFSRootPath() {
    return "DLight/Configurations"; // NOI18N
  }

  void selectConfiguration(String configurationName){
    this.selectedConfigurationName = configurationName;
  }

  /**
   * Returns DLightConfiguration by name if exists, <code>null</code> otherwise
   * @param configurationName configuration name
   * @return DLightConfiguration by name if exists, <code>null</code> otherwise
   */
  public DLightConfiguration getConfigurationByName(String configurationName){
    List<DLightConfiguration> toolConfigurations = getDLightConfigurations();
    for (DLightConfiguration conf : toolConfigurations){
      if (conf.getConfigurationName().equals(configurationName)){
        return conf;
      }
    }
    return null;
  }
  
  public List<DLightConfiguration> getDLightConfigurations(){
    List<DLightConfiguration> result = new ArrayList<DLightConfiguration>();
    FileObject configurationsFolder = getToolsFSRoot();

    if (configurationsFolder == null) {
      return result;
    }

    FileObject[] configurations = configurationsFolder.getChildren();

    if (configurations == null || configurations.length == 0) {
      return result;
    }

    for (FileObject conf : configurations) {
      result.add(DLightConfiguration.create(conf));
    }
    return result;
  }

  DLightConfiguration getSelectedDLightConfiguration(){
    if (selectedConfigurationName != null){
      return getConfigurationByName(selectedConfigurationName);
    }
    List<DLightConfiguration> tools = getDLightConfigurations();
    if (tools == null || tools.size() == 0){
      return DLightConfiguration.createDefault();
    }
    return tools.get(0);
  }

  /**
   * This method returns the default configuration (all tools)
   */
  public final DLightConfiguration getDefaultConfiguration() {
      return DLightConfiguration.createDefault();
  }

  /**
   * 
   * @return
   */
  public static synchronized  final DLightConfigurationManager getInstance(){
    if (instance == null){
      instance = new DLightConfigurationManager();
    }
    return instance;
  }

  

}
