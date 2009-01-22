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

package org.netbeans.modules.dlight.dataprovider.spi;

import org.netbeans.modules.dlight.storage.spi.DataStorage;


/**
 * Provides the data to the {@link org.netbeans.modules.dlight.core.visualizer.model.Visualizer}.
 * Use Global Lookup to regitser your  own DataProvider.
 * DataProvider represents also a factory to create instances using
 * {@link  #newInstance()} method
 */

public interface DataProvider {
//  /**
//   * Unique provider id
//   * @return unique provider id
//   */
//  public String getID();
//  /**
//   * Returns the list of {@link org.netbeans.modules.dlight.core.dataprovider.model.DataModelScheme}
//   * this data provider can serve.
//   * @return the list of data model this DataProvider can serve
//   */
//  public List<? extends DataModelScheme> getProvidedDataModelScheme();
//
//  /**
//   * Checks if DataProvider can provider information according to
//   * te <param>dataModel</param>
//   * @param dataModel
//   * @return <code>true</code> if DataProvider provides information required
//   * by <param>dataModel</param>
//   */
//  public boolean provides(DataModelScheme dataModel);
//
//  /**
//   * The types of {@link org.netbeans.modules.dlight.core.storage.model.DataStorage} this
//   *  DataProvider can get data from
//   * @return the list of {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
//   *  supported by this DataProvider
//   */
//  public List<DataStorageType> getSupportedDataStorageTypes();

  /**
   * Attaches DataProvider to the>storage.
   * All data requested by {@link org.netbeans.modules.dlight.core.visualizer.model.Visualizer} will
   * be extracted from this storage. This method is invoked at the time Visualizer
   * need to be displayed. See {@link org.netbeans.modules.dlight.core.model.DLightManager#openVisualizer(org.netbeans.modules.dlight.core.model.DLightTool, java.lang.String, org.netbeans.modules.dlight.core.visualizer.model.VisualizerConfiguration) } for more detailes
   * @param storage {@link org.netbeans.modules.dlight.core.storage.model.DataStorage}.
   * Please do not call this method directly.
   * It will be invoked automatically when needed.
   */
  public void attachTo(DataStorage storage);

//  /**
//   * Creates new instance of DataProvider, factory method
//   * @return new instance of DataProvider
//   */
//  public DataProvider newInstance();

}
