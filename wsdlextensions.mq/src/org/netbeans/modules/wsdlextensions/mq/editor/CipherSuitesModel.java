/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * accompanied this code.
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 * Represents WebSphere MQ-compatible JSSE CipherSuites.
 * 
 * @author Noel.Ang@sun.com
 */
public class CipherSuitesModel {
    public CipherSuitesModel() {
    }
    
    public ComboBoxModel getAsComboBoxModel() {
        return new DefaultComboBoxModel(cipherSuites);
    }
    
    private final static String[] cipherSuites = new String[] {
            "",
            "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
            "SSL_RSA_WITH_DES_CBC_SHA",
            "SSL_RSA_WITH_NULL_MD5",
            "SSL_RSA_WITH_NULL_SHA",
            "SSL_RSA_WITH_RC4_128_MD5",
            "SSL_RSA_WITH_RC4_128_SHA",
    };
}
