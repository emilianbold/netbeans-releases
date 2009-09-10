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
package org.netbeans.modules.dlight.api.dataprovider;

import org.netbeans.modules.dlight.api.impl.DataModelSchemeAccessor;

/**
 * <p>
 * This is marker class  which declares scheme which
 * will be used by {@link org.netbeans.modules.dlight.spi.visualizer.Visualizer}
 * to find {@link org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory} which
 * will create  {@link org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider} used by
 *  {@link org.netbeans.modules.dlight.spi.visualizer.Visualizer} to get data from.
 * <p>
 * Use {@link org.netbeans.modules.dlight.api.support.DataModelSchemeProvider} to get
 * instance of DataModelScheme for the particular id.
 * <p>
 * As an example let's pretend you need to view data in Table View and
 * creates your own TableVisualizer configuration which implements
 * {@link org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration} and
 *  it will announce that <code>TableScheme</code> is supported (id is equals to "model:table") and
 *  When Table Visualizer (created along with TableVisuallizer configuration) needs
 *  to be opened infrastructure should find the proper {@link org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider}
 *  which will be used by Visualizer and *the proper*  means that DataProvider should
 *  provide </code>TableScheme</code> in the
 * {@link org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory#getProvidedDataModelScheme() }
 *
 */

public final class DataModelScheme {

    static {
        DataModelSchemeAccessor.setDefault(new DataModelSchemeAccessorImpl());
    }
    private String id;

    DataModelScheme(String id) {
        this.id = id;
    }

    /**
     * Id used to identify model
     * @return unqiue id
     */
    public String getID() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataModelScheme other = (DataModelScheme) obj;
        return other.getID().equals(this.id);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return id;
    }

    private static final class DataModelSchemeAccessorImpl extends DataModelSchemeAccessor {

        @Override
        public DataModelScheme createNew(String id) {
            return new DataModelScheme(id);
        }
    }
}
