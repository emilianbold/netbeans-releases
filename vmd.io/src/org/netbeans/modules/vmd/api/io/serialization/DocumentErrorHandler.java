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
 */package org.netbeans.modules.vmd.api.io.serialization;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public final class DocumentErrorHandler {
    
    private List<String> errors;
    private List<String> warnings;
   
    public DocumentErrorHandler() {
        errors = new ArrayList<String>();
        warnings = new ArrayList<String>();
    }
     /**
     * This method adds critical issues to the deserializing document rapport and
     * stops loading of document. All reported issues are shown in report
     * dialog window right after loading of document is stooped.  
     * @param description of issue, non null String 
     */ 
    public DocumentErrorHandler addError(String error) {
        if (error == null)
            throw new IllegalArgumentException();
        errors.add(error);
        return this;
    }
    
    /**
     * This method adds critical issues to the deserializing document rapport  and
     * stops loading of document. All reported issues are shown in report dialog window when document is loaded.
     * @param strings - parts of the warning message
     * @param pattern - string warning pattern
     */ 
    public DocumentErrorHandler addError(String pattern, Object... strings) {
         if (pattern == null || strings == null)
            throw new IllegalArgumentException();
        errors.add(MessageFormat.format(pattern, strings));
        return this;
    }
    /**
     * This method adds NON critical issues to the deserializing document rapport.
     * All reported issues are shown in report dialog window when document is loaded.
     * @param description of issue, non null String 
     */ 
    public DocumentErrorHandler addWarning(String warning) {
         if (warning == null)
            throw new IllegalArgumentException();
        warnings.add(warning);
        return this;
    }
    
    /**
     * This method adds NON critical issues to the deserializing document rapport.
     * All reported issues are shown in report dialog window when document is loaded.
     * @param strings - parts of the warning message
     * @param pattern - string warning pattern
     */ 
    public DocumentErrorHandler addWarning(String pattern, Object... strings) {
         if (pattern == null || strings == null)
            throw new IllegalArgumentException();
        warnings.add(MessageFormat.format(pattern, strings));
        return this;
    }
    /**
     * Returns reported critical issues;
     * @return critical issues
     */ 
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    /**
     * Returns reported non criticla issues. 
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
}
