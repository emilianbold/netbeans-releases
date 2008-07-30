/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject;

/**
 *
 * @author schmidtm
 * @author Martin Adamek
 */
public enum SourceCategory {

    NONE("", null), // NOI18N
    CONFIGURATION("grails-app/conf", null),
    CONTROLLERS("grails-app/controllers", "create-controller"),
    DOMAIN("grails-app/domain", "create-domain-class"),
    MESSAGES("grails-app/i18n", null),
    SERVICES("grails-app/services", "create-service"),
    TAGLIB("grails-app/taglib", "create-tag-lib"),
    UTIL("grails-app/utils", null),
    VIEWS("grails-app/views", "generate-views"),
    INTEGRATION_TESTS("test/integration", "create-integration-test"),
    UNIT_TESTS("test/unit", "create-unit-test"),
    SCRIPTS("scripts", "create-script"),
    SRC_JAVA("src/java", null),
    SRC_GROOVY("src/groovy", null),
    WEBAPP("web-app", null),
    LIB("lib", null);

    private final String directory;
    private final String cmd;

    SourceCategory(String directory, String command) {
        this.directory = directory;
        this.cmd = command;
    }

    public String dir() {
        return directory;
    }

    public String command() {
        return cmd;
    }

}
