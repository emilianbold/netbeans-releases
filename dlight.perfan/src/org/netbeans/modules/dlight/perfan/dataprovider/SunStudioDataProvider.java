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
package org.netbeans.modules.dlight.perfan.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataModel;
import org.netbeans.modules.dlight.core.stack.model.FunctionCall;
import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.dataprovider.impl.TableDataModel;
import org.netbeans.modules.dlight.dataprovider.spi.support.TableDataProvider;
import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.util.DLightLogger;



/**
 *
 * @author mt154047
 */
final class SunStudioDataProvider extends SSStackDataProvider implements TableDataProvider {

  private static final Logger log = DLightLogger.getLogger(SSStackDataProvider.class);

  SunStudioDataProvider() {
  }


  public List<DataRow> queryData(DataTableMetadata tableMetadata) {

    String table = tableMetadata.getName();
    List<Column> columns = tableMetadata.getColumns();

    List<DataRow> result = new ArrayList<DataRow>();
    List<String> columnNames = new ArrayList<String>();
    for (Column c : columns){
      columnNames.add(c.getColumnName());
    }
    List<FunctionCallTreeTableNode> nodes= super.getTableView(columns, null, Integer.MAX_VALUE);
    for (FunctionCallTreeTableNode node : nodes){
      FunctionCall call = node.getDeligator();
      List<Object> data = new ArrayList<Object>();
      for (Column c : columns){
       if (c.getColumnName().equals("name")){
         data.add(call.getFunction().getName());
         continue;
        }
        data.add(call.getMetricValue(c.getColumnName()));
      }
      DataRow row = new DataRow(columnNames, data);
      result.add(row);

    }
    return result;
  }
}
