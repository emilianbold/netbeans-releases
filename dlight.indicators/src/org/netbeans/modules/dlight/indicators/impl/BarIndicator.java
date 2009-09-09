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

package org.netbeans.modules.dlight.indicators.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.indicators.BarIndicatorConfiguration;
import org.netbeans.modules.dlight.spi.indicator.Indicator;



public class BarIndicator extends Indicator<BarIndicatorConfiguration> {
  private final NumberFormat nf = NumberFormat.getNumberInstance();
//  private BarIndicatorConfig config;
  private BarIndicatorPanel panel;
  private Number[] barData;
  private int count = 0;
  private final BarIndicatorConfiguration configuration;

  public BarIndicator(BarIndicatorConfiguration config) {
    super(config);
    this.configuration = config;
    for (Column col : getMetadata().getColumns()) {
      if (col.getColumnClass().getSuperclass() != Number.class) {
        throw new IllegalArgumentException("BarIndicator could be based on Number data only!"); //NOI18N
      }
    }
    
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    
    //this.config = config;
    this.panel = new BarIndicatorPanel(getMetadata());
    barData = new Number[getMetadata().getColumnsCount()];
  }
 

  public JComponent getComponent() {
    return panel;
  }

  public void updated( List<DataRow> data) {
    if (data.isEmpty()) {
      return;
    }

    int newCount = count + data.size();
    
    DataRow lastRow = data.get(data.size() - 1);

    int idx = 0;

    for (Column col : getMetadataColumns()) {
      String value = lastRow.getStringValue(col.getColumnName());
      
      if (count == 0) {
        barData[idx] = Double.valueOf(value);
      } else {
//        String aggregationType = (String)(BarIndicatorConfigurationAccessor.getDefault().getConfigurationData(configuration).getNode(col.getColumnName()).get("aggregation"));
//        if (aggregationType != null && aggregationType.equals("avrg")) {
//          double total = barData[idx].doubleValue() * count + Double.valueOf(value);
//          barData[idx] = total / newCount;
    //    } else {
          barData[idx] = Double.valueOf(value);
 //       }
      }
      idx++;
    }

    count = newCount;
    panel.update();
  }

  protected void tick() {}

  @Override
  protected void repairNeeded(boolean needed) {}

  public void reset() {
    //reset indicator
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  private class BarIndicatorPanel extends JPanel {
    List<JLabel> barLabels = new ArrayList<JLabel>();

    public BarIndicatorPanel(IndicatorMetadata metadata) {
      setLayout(new GridBagLayout());

//      setBackground(config.getNode(TOOL_TIP_TEXT_KEY));
      GridBagConstraints gridBagConstraints;

      int idx = 0;
      for (Column c : metadata.getColumns()) {
        JLabel l = new JLabel(c.getColumnUName() + ": "); //NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = idx;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(l, gridBagConstraints);
        
        JLabel v = new JLabel("--"); //NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = idx;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(v, gridBagConstraints);
        barLabels.add(v);

        idx++;
      }
    }

    private void update() {
      int idx = 0;
      for (JLabel l : barLabels) {
        l.setText(nf.format(barData[idx++]));
      }
    }

  }}
