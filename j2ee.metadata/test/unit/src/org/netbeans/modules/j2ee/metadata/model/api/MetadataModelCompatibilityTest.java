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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import java.sql.SQLException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;

/**
 *
 * @author Andrei Badea
 */
public class MetadataModelCompatibilityTest extends NbTestCase {

    public MetadataModelCompatibilityTest(String name) {
        super(name);
    }

    protected MetadataModel<?> createModel() {
        return MetadataModelFactory.createMetadataModel(new SimpleMetadataModelImpl<Void>());
    }

    public void testExceptions() throws Exception {
        MetadataModel<?> model = createModel();
        doTestRuntimeExceptionsArePropagated(model);
        doTestCheckedExceptionsAreWrapped(model);
        doTestIOExceptionsAreWrapped(model);
    }

    private <T> void doTestRuntimeExceptionsArePropagated(MetadataModel<T> model) throws IOException {
        try {
            model.runReadAction(new MetadataModelAction<T, Void>() {
                public Void run(T test) {
                    throw new RuntimeException("foo");
                }
            });
            fail();
        } catch (RuntimeException re) {
            assertEquals("foo", re.getMessage());
        }
    }

    private <T> void doTestCheckedExceptionsAreWrapped(MetadataModel<T> model) throws IOException {
        try {
            model.runReadAction(new MetadataModelAction<T, Void>() {
                public Void run(T test) throws SQLException {
                    throw new SQLException("foo");
                }
            });
            fail();
        } catch (MetadataModelException mme) {
            SQLException cause = (SQLException)mme.getCause();
            assertEquals("foo", cause.getMessage());
        }
    }

    private <T> void doTestIOExceptionsAreWrapped(MetadataModel<T> model) throws IOException {
        try {
            model.runReadAction(new MetadataModelAction<T, Void>() {
                public Void run(T test) throws IOException {
                    throw new IOException("foo");
                }
            });
            fail();
        } catch (MetadataModelException mme) {
            IOException cause = (IOException)mme.getCause();
            assertEquals("foo", cause.getMessage());
        }
    }
}
