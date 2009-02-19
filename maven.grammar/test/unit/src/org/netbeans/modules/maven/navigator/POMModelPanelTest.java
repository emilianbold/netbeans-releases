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

package org.netbeans.modules.maven.navigator;

import hidden.org.codehaus.plexus.util.StringOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class POMModelPanelTest {

    public POMModelPanelTest() {
    }


    /**
     * Test of getPropertyValues method, of class POMModelPanel.
     */
    @Test
    public void testGetPropertyValues() throws Exception {
        ModelSource source = createModelSource();
        try {
            POMModel model = POMModelFactory.getDefault().getModel(source);
            assertTrue(source.isEditable());
            assertNotNull(model.getProject());
        System.out.println("getPropertyValues");
        Properties[] models = new Properties[4];
            model.startTransaction();
        Properties one = model.getFactory().createProperties();
        Properties two = model.getFactory().createProperties();
        Properties three = model.getFactory().createProperties();
        Properties four = model.getFactory().createProperties();
        models[0] = one;
        models[1] = two;
        models[2] = three;
        models[3] = four;

        String prop = "propone";
        one.setProperty(prop, "val1");
        three.setProperty(prop, "zzz");
        Map<String, List<String>> result = POMModelPanel.getPropertyValues(models);
        List<String> lst = result.get(prop);
        assertNotNull(lst);
        assertNotNull(lst.get(0));
        assertNull(lst.get(1));
        assertNotNull(lst.get(2));
        assertNull(lst.get(3));

        prop = "proptwo";
        two.setProperty(prop, "val1");
        three.setProperty(prop, "zzz");
        result = POMModelPanel.getPropertyValues(models);
        lst = result.get(prop);
        assertNotNull(lst);
        assertNull(lst.get(0));
        assertNotNull(lst.get(1));
        assertNotNull(lst.get(2));
        assertNull(lst.get(3));

            model.endTransaction();

        } finally {
            File file = source.getLookup().lookup(File.class);
            file.deleteOnExit();
        }

    }

    private ModelSource createModelSource() throws FileNotFoundException, IOException, URISyntaxException {
        String dir = System.getProperty("java.io.tmpdir");
        File sourceFile = new File(dir, "pom.xml");
        ModelSource source = Utilities.createModelSourceForMissingFile(sourceFile, true, 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\"> </project>",
                "text/xml");
        assertTrue(source.isEditable());
        return source;
    }

}