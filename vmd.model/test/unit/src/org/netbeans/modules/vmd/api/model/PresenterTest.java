/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.vmd.api.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.vmd.api.model.common.TypesSupport;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter1;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class PresenterTest extends TestCase {
    
    private DesignDocument document;
    
    public PresenterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PresenterTest.class);
        
        return suite;
    }
    
    /**
     * Test of getEventFilter method, of class org.netbeans.modules.vmd.api.model.Presenter.
     */
    public void testGetEventFilter() {
        System.out.println("getEventFilter"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(0);
                
                assertNotNull(comp1.getPresenter(TestPresenter1.class).getEventFilter());
                assertTrue(comp1.getPresenter(TestPresenter1.class).getEventFilter() instanceof DesignEventFilter);
            }
        });
    }
    
    /**
     * Test of designChanged method, of class org.netbeans.modules.vmd.api.model.Presenter.
     */
    public void testDesignChanged() {
        System.out.println("designChanged"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(0);
                TestPresenter presenter = (TestPresenter) comp1.getPresenter(TestPresenter1.class);
                
                assertTrue(presenter.isDesignChangedFlag());
            }
        });
    }
    
    /**
     * Test of presenterChanged method, of class org.netbeans.modules.vmd.api.model.Presenter.
     */
    public void testPresenterChanged() {
        System.out.println("presenterChanged"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(0);
                TestPresenter presenter = comp1.getPresenter(TestPresenter1.class);
                
                assertTrue(presenter.isPresenterChangedFlag());
            }
        });
    }
}
