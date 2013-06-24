/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.testrunner.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.CommonTestsCfgOfCreate;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

public final class TestCreatorPanelDisplayer {

    private static final TestCreatorPanelDisplayer INSTANCE = new TestCreatorPanelDisplayer();
    private static final RequestProcessor RP = new RequestProcessor(TestCreatorPanelDisplayer.class);

    private TestCreatorPanelDisplayer() {}
    /**
     * Get the default <code>TestCreatorPanelDisplayer</code>
     * @return the default instance
     */
    public static TestCreatorPanelDisplayer getDefault() {
        return INSTANCE;
    }

    public void displayPanel(Node[] activatedNodes, Object location, String testingFramework) {
	final DataObject[] modified = DataObject.getRegistry().getModified();
	CommonTestsCfgOfCreate cfg = new CommonTestsCfgOfCreate(activatedNodes);
	cfg.createCfgPanel(modified.length == 0 ? false : true);

	ArrayList<String> testingFrameworks = new ArrayList<String>();
	Collection<? extends Lookup.Item<TestCreatorProvider>> providers = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
	for (Lookup.Item<TestCreatorProvider> provider : providers) {
	    testingFrameworks.add(provider.getDisplayName());
	}
	cfg.addTestingFrameworks(testingFrameworks);
	cfg.setPreselectedLocation(location);
	cfg.setPreselectedFramework(testingFramework);
	if (!cfg.configure()) {
	    return;
	}
	saveAll(modified); // #149048
	String selected = cfg.getSelectedTestingFramework();

	for (final Lookup.Item<TestCreatorProvider> provider : providers) {
	    if (provider.getDisplayName().equals(selected)) {
		final TestCreatorProvider.Context context = new TestCreatorProvider.Context(activatedNodes);
		context.setSingleClass(cfg.isSingleClass());
		context.setTargetFolder(cfg.getTargetFolder());
		context.setTestClassName(cfg.getTestClassName());
                final Collection<? extends SourceGroup> createdSourceRoots = cfg.getCreatedSourceRoots();
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        //Todo: display some progress
                        for (SourceGroup sg : createdSourceRoots) {
                            IndexingManager.getDefault().refreshIndexAndWait(sg.getRootFolder().toURL(), null);
                        }
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                provider.getInstance().createTests(context);
                            }
                        });
                    }
                });
		cfg = null;
		break;
	    }
	}
    }

    private void saveAll(DataObject[] dataObjects) {
	for (DataObject dataObject : dataObjects) {
	    SaveCookie saveCookie = dataObject.getLookup().lookup(SaveCookie.class);
	    if (saveCookie != null) {
		try {
		    saveCookie.save();
		} catch (IOException ex) {
		    Exceptions.printStackTrace(ex);
		}
	    }
	}
    }
}
