/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.libs.djnsswt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;


/**
 *
 * @author stan
 */
class ModuleHandler {

  private boolean isRestart = false;
  private OperationContainer<OperationSupport> oc;
  private Restarter restarter;
  private final boolean isDirectMode;

  public ModuleHandler() {
    this (false);
  }

  public ModuleHandler(boolean isDirectMode) {
    this.isDirectMode = isDirectMode;
  }

  public List<String> getModules(String startFilter, boolean includeDisabled) {
    List<String> activatedModules = new ArrayList<String>();
    Collection<? extends ModuleInfo> lookupAll = Lookup.getDefault().lookupAll(ModuleInfo.class);
    for (ModuleInfo moduleInfo : lookupAll) {
      if (includeDisabled || moduleInfo.isEnabled()) {
        if (startFilter == null || moduleInfo.getCodeNameBase().startsWith(startFilter)) {
          activatedModules.add(moduleInfo.getCodeNameBase());
        }
      }
    }
    Collections.sort(activatedModules);
    return activatedModules;
  }

  public void doRestart(boolean isForced) {
    if (isForced || isRestart) {
      if (oc != null && restarter != null) {
        try {
          oc.getSupport().doRestart(restarter, null);
        } catch (OperationException ex) {
          Exceptions.printStackTrace(ex);
        }
      } else {
        LifecycleManager.getDefault().markForRestart();
        LifecycleManager.getDefault().exit();
      }
    }
  }

  /**
* Activate/deactivate a list of modules
* @param codeNames The names of the modules.
* @param isEnabled True to enable the modules, false otherwise.
* @return true if a restart is mandatory.
*/
  public boolean setModulesState(Set<String> codeNames, boolean isEnabled) {
    boolean restartFlag;
    if (isEnabled) {
      restartFlag = setModulesEnabled(codeNames);
    } else {
      restartFlag = setModulesDisabled(codeNames);
    }
    return isRestart = isRestart || restartFlag;
  }

  private boolean setModulesDisabled(Set<String> codeNames) {
    Collection<UpdateElement> toDisable = new HashSet<UpdateElement>();
    List<UpdateUnit> allUpdateUnits =
      UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
    for (UpdateUnit unit : allUpdateUnits) {
      if (unit.getInstalled() != null) {
        UpdateElement el = unit.getInstalled();
        if (el.isEnabled()) {
          if (codeNames.contains(el.getCodeName())) {
            toDisable.add(el);
          }
        }
      }
    }
    if (!toDisable.isEmpty()) {
      oc = isDirectMode ? OperationContainer.createForDirectDisable() : OperationContainer.createForDisable();
      for (UpdateElement module : toDisable) {
        if (oc.canBeAdded(module.getUpdateUnit(), module)) {
          OperationInfo<OperationSupport> operationInfo = oc.add(module);
          if (operationInfo == null) {
            continue;
          }
          // get all module depending on this module
          Set<UpdateElement> requiredElements = operationInfo.getRequiredElements();
          // add all of them between modules for disable
          oc.add(requiredElements);
        }
      }
      try {
        // get operation support for complete the disable operation
        OperationSupport support = oc.getSupport();
        // If support is null, no element can be disabled.
        if ( support != null ) {
          restarter = support.doOperation(null);
        }
      } catch (OperationException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return restarter != null;
  }

  private boolean setModulesEnabled(Set<String> codeNames) {
    Collection<UpdateElement> toEnable = new HashSet<UpdateElement>();
    List<UpdateUnit> allUpdateUnits = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
    for (UpdateUnit unit : allUpdateUnits) {
      if (unit.getInstalled() != null) {
        UpdateElement el = unit.getInstalled();
        if (!el.isEnabled()) {
          if (codeNames.contains(el.getCodeName())) {
            toEnable.add(el);
          }
        }
      }
    }
    if (!toEnable.isEmpty()) {
      oc = OperationContainer.createForEnable();
      for (UpdateElement module : toEnable) {
        if (oc.canBeAdded(module.getUpdateUnit(), module)) {
          OperationInfo<OperationSupport> operationInfo = oc.add(module);
          if (operationInfo == null) {
            continue;
          }
          // get all module depending on this module
          Set<UpdateElement> requiredElements = operationInfo.getRequiredElements();
          // add all of them between modules for disable
          oc.add(requiredElements);
        }
      }
      try {
        // get operation support for complete the enable operation
        OperationSupport support = oc.getSupport();
        if (support != null) {
          restarter = support.doOperation(null);
        }
        return true;
      } catch (OperationException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return false;
  }

}