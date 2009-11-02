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
package org.netbeans.modules.dlight.indicators;

import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesIndicatorConfigurationAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author mt154047
 */
public class TimeSeriesIndicatorConfigurationTest {

    private FileObject folder;

    public TimeSeriesIndicatorConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        folder = FileUtil.getConfigFile("DLight/Fops.Configuration");
        assertNotNull("testing layer is loaded: ", folder);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreate() {
        System.out.println("createIndicatorMetadata");
        TimeSeriesIndicatorConfiguration result = null;
        try {
            FileObject fo = folder.getFileObject("TimeSeriesIndicatorConfiguration.instance");
            assertNotNull("file " + "DLight/Fops.Configuration/TimeSeriesIndicatorConfiguration.instance", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            if (!(obj instanceof TimeSeriesIndicatorConfiguration)) {
                fail("Object needs to be a IndicatorMetadata: " + obj);
            }
            result = (TimeSeriesIndicatorConfiguration) obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("TimeSeriesIndicatorConfiguration should not be null", result);
        TimeSeriesIndicatorConfigurationAccessor accessor = TimeSeriesIndicatorConfigurationAccessor.getDefault();
        System.out.println("aggregation=" + accessor.getAggregation(result));
        System.out.println("granurality=" + accessor.getGranularity(result));
        System.out.println("label.formatter=" + accessor.getLabelRenderer(result));
        System.out.println("title=" + accessor.getTitle(result));
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testTimeSeriesDescriptorsList() {
        System.out.println("TimeSeriesDescriptors.List");
        Collection<TimeSeriesDescriptor> result = null;
        try {
            FileObject fo = folder.getFileObject("TimeSeriesDescriptors.List");
            assertNotNull("file " + "DLight/Fops.Configuration/TimeSeriesDescriptors.List", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            result = (Collection<TimeSeriesDescriptor>) obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("IndicatorMetadata should not be null", result);
        System.out.println("size=" + result.size());
    }
}
