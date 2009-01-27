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
package org.netbeans.modules.dlight.api.indicator;

import org.netbeans.modules.dlight.api.impl.IndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;

/**
 * Configuration to create {@link org.netbeans.modules.dlight.spi.indicator.Indicator}
 * on the base of. Use it to register in
 * {@link org.netbeans.modules.dlight.api.tool.DLightToolConfiguration#addIndicatorConfiguration(org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration)}
 *  
 */
public abstract class IndicatorConfiguration {

  private IndicatorMetadata metadata;
  private VisualizerConfiguration visualizerConfiguration;
  private ConfigurationData configurationData;


  static {
    IndicatorConfigurationAccessor.setDefault(new IndicatorConfigurationAccessorImpl());
  }

  /**
   * Created new Indicator Configuration on the base of {@link org.netbeans.modules.dlight.api.indicator.IndicatorMetadata}
   * @param metadata metadata to create Indicator configuration for
   */
  public IndicatorConfiguration(IndicatorMetadata metadata) {
    this.metadata = metadata;
  }

  /**
   * Unique id which will be used to find the proper factory
   * @return id
   */
  public abstract String getID();

  /**
   * Sets configuration data if needed
   * @param configurationData configuration to be used by {@link org.netbeans.modules.dlight.spi.indicator.Indicator}
   */
  public final void setConfigurationData(ConfigurationData configurationData) {
    this.configurationData = configurationData;
  }

  /**
   * Sets {@link org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration}
   * which means Detailed View which will be opened when double or single mouse click on 
   * {@link org.netbeans.modules.dlight.spi.indicator.Indicator}
   * @param configuration configuration to create {@link org.netbeans.modules.dlight.spi.visualizer.Visualizer}
   * which will represent Detailed View
   */
  public final void setVisualizerConfiguration(VisualizerConfiguration configuration) {
    this.visualizerConfiguration = configuration;
  }

  /**
   * Returns {@link org.netbeans.modules.dlight.api.indicator.ConfigurationData} if it is set
   * @return configuration data
   */
  protected final ConfigurationData getConfigurationData() {
    return configurationData;
  }

  /**
   * Returns indicator metadata, see {@link org.netbeans.modules.dlight.api.indicator.IndicatorMetadata}
   * @return indicator metadata
   */
  protected final IndicatorMetadata getIndicatorMetadata() {
    return metadata;
  }

  /**
   * Returns {@link org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration} used
   * by the indicator as a configuration of Detailed View which will be opened by clicking on the
   * indicator created on the base of this configuration
   * @return visualizer configuraion
   */
  protected final VisualizerConfiguration getVisualizerConfiguration() {
    return visualizerConfiguration;
  }

  private static final class IndicatorConfigurationAccessorImpl extends IndicatorConfigurationAccessor {

    @Override
    public ConfigurationData getConfigurationData(IndicatorConfiguration configuration) {
      return configuration.getConfigurationData();
    }

    @Override
    public IndicatorMetadata getIndicatorMetadata(IndicatorConfiguration configuration) {
      return configuration.getIndicatorMetadata();
    }

    @Override
    public VisualizerConfiguration getVisualizerConfiguration(IndicatorConfiguration configuration) {
      return configuration.getVisualizerConfiguration();
    }
  }

}
