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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Tor Norbye
 */
public abstract class SharedRubyProjectProperties {
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    public static final String INCLUDE_JAVA = "ruby.includejava"; // NOI18N
    public static final String RAKE_ARGS = "rake.args"; // NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N

    // External Java integration
    public DefaultListModel JAVAC_CLASSPATH_MODEL;
    public ButtonModel INCLUDE_JAVA_MODEL;
    public ListCellRenderer CLASS_PATH_LIST_RENDERER;

    //public abstract DefaultListModel getListModel(String propertyName);
    //public abstract ListCellRenderer getListRenderer(String propertyName);
    
    public abstract void save();
}
