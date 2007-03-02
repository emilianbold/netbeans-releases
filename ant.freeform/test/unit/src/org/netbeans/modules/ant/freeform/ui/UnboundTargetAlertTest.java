/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.w3c.dom.Element;

/**
 * Test non-GUI functionality of the unbound target alert: binding creation etc.
 * @author Jesse Glick
 */
public class UnboundTargetAlertTest extends TestBase {
    
    public UnboundTargetAlertTest(String name) {
        super(name);
    }

    private FreeformProject prj;
    private UnboundTargetAlert uta;
    
    protected void setUp() throws Exception {
        super.setUp();
        prj = copyProject(simple);
        uta = new UnboundTargetAlert(prj, "debug");
    }
    
    public void testGenerateBindingAndAddContextMenuItem() throws Exception {
        uta.simulateTargetSelection("twiddle-this");
        uta.generateBindingAndAddContextMenuItem();
        List<FreeformProjectGenerator.TargetMapping> mappings = FreeformProjectGenerator.getTargetMappings(prj.helper());
        // Will add it to the end, so just look there.
        FreeformProjectGenerator.TargetMapping lastMapping = mappings.get(mappings.size() - 1);
        assertEquals("debug", lastMapping.name);
        assertEquals(null, lastMapping.script);
        assertEquals(Collections.singletonList("twiddle-this"), lastMapping.targets);
        assertEquals(null, lastMapping.properties);
        assertEquals(null, lastMapping.context);
        // Check also making a binding for multiple targets, which is permitted.
        mappings.remove(lastMapping);
        FreeformProjectGenerator.putTargetMappings(prj.helper(), mappings);
        uta.simulateTargetSelection("  twiddle-this extra-step ");
        uta.generateBindingAndAddContextMenuItem();
        mappings = FreeformProjectGenerator.getTargetMappings(prj.helper());
        lastMapping = mappings.get(mappings.size() - 1);
        assertEquals("debug", lastMapping.name);
        assertEquals(null, lastMapping.script);
        assertEquals(Arrays.asList("twiddle-this", "extra-step"), lastMapping.targets);
        assertEquals(null, lastMapping.properties);
        assertEquals(null, lastMapping.context);
        // Also check the context menu.
        Element data = prj.getPrimaryConfigurationData();
        Element view = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull(view);
        Element contextMenu = Util.findElement(view, "context-menu", FreeformProjectType.NS_GENERAL);
        assertNotNull(contextMenu);
        Set<String> actionNames = new TreeSet<String>();
        for (Element action : Util.findSubElements(contextMenu)) {
            if (action.getLocalName().equals("ide-action")) {
                actionNames.add(action.getAttribute("name"));
            }
        }
        assertEquals("Correct context menu IDE actions",
            new TreeSet<String>(Arrays.asList("build", "clean", "rebuild", "run", "javadoc", /*added*/ "debug")),
            actionNames);
    }
    
}
