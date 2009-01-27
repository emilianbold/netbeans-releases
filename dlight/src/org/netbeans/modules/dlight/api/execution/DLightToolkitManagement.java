/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.execution;

import java.util.Collection;
import org.netbeans.modules.dlight.api.impl.DLightSessionHandlerAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolkitManager;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public final class DLightToolkitManagement {

  static{
    DLightSessionHandlerAccessor.setDefault(new DLightSessionHandlerAccessorImpl());
  }

  private static DLightToolkitManagement instance = null;
  private static DLightToolkitManager toolkitManager;

  private DLightToolkitManagement() {
    Collection<? extends DLightToolkitManager> result = Lookup.getDefault().lookupAll(DLightToolkitManager.class);
    toolkitManager = Lookup.getDefault().lookup(DLightToolkitManager.class);
  }

  public static final DLightToolkitManagement getInstance() {
    if (instance == null) {
      instance = new DLightToolkitManagement();
    }
    return instance;
  }

  public DLightSessionHandler createSession(DLightTarget target, String configurationName) {
    return toolkitManager.createSession(target, configurationName);
  }

  public DLightSessionHandler createSession(DLightTarget target) {
    return toolkitManager.createSession(target);
  }

  public void startSession(DLightSessionHandler reference) {
    toolkitManager.startSession(reference);
  }

  public void stopSession(DLightSessionHandler reference) {
    toolkitManager.stopSession(reference);
  }

  private  DLightSessionHandler create(DLightSessionInternalReference ref){
    return new DLightSessionHandler(ref);
  }

  public final class DLightSessionHandler {

    private DLightSessionInternalReference ref;

    private DLightSessionHandler(DLightSessionInternalReference ref) {
      this.ref = ref;
    }

    DLightSessionInternalReference getSessionReferenceImpl() {
      return ref;
    }
  }

  private static final class DLightSessionHandlerAccessorImpl extends DLightSessionHandlerAccessor {

    @Override
    public DLightSessionHandler create(DLightSessionInternalReference ref) {
      return DLightToolkitManagement.getInstance().create(ref);
    }

    @Override
    public DLightSessionInternalReference getSessionReferenceImpl(DLightSessionHandler handler) {
      return handler.getSessionReferenceImpl();
    }
  }

}
