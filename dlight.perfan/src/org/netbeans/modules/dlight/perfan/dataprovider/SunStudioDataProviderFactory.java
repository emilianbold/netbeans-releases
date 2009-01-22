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
package org.netbeans.modules.dlight.perfan.dataprovider;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataModel;
import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.dataprovider.impl.TableDataModel;
import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.dataprovider.spi.DataProviderFactory;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;
import org.netbeans.modules.dlight.storage.spi.DataStorageTypeFactory;

/**
 *
 * @author mt154047
 */
public final class SunStudioDataProviderFactory implements DataProviderFactory {

  public DataProvider create() {
    return new SunStudioDataProvider();
  }

  @Override
  public List<? extends DataModelScheme> getProvidedDataModelScheme() {
    return Arrays.asList(TableDataModel.instance, StackDataModel.instance);
  }

  public List<DataStorageType> getSupportedDataStorageTypes() {
    return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(PerfanDataStorage.ID));
  }

  @Override
  public boolean provides(DataModelScheme dataModel) {
    return getProvidedDataModelScheme().contains(dataModel);
  }
}
