/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;

/**
 * @author Tor Norbye
 */
public abstract class SharedRubyProjectProperties {
    
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    public static final String INCLUDE_JAVA = "ruby.includejava"; // NOI18N
    public static final String RAKE_ARGS = "rake.args"; // NOI18N
    public static final String JRUBY_PROPS = "jruby.props"; // NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N

    // External Java integration
    public DefaultListModel JAVAC_CLASSPATH_MODEL;
    public ButtonModel INCLUDE_JAVA_MODEL;
    public ListCellRenderer CLASS_PATH_LIST_RENDERER;

    //public abstract DefaultListModel getListModel(String propertyName);
    //public abstract ListCellRenderer getListRenderer(String propertyName);
    
    public abstract void save();
}
