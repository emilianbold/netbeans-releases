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
package org.netbeans.modules.ruby.railsprojects.plugins;


/**
 * A descriptor of a Ruby On Rails Plugin.
 *
 * @author Tor Norbye
 */
public class Plugin implements Comparable<Plugin> {
    private String name;
    private String repository;
    
    public Plugin(String name, String repository) {
        this.name = name;
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public String getRepository() {
        return repository;
    }

    @Override
    public String toString() {
        // Shown in ListCellRenderer etc.
        StringBuilder sb = new StringBuilder(100);
        sb.append("<html><b>"); // NOI18N
        sb.append(name);
        sb.append("</b>"); // NOI18N

        if (repository != null) {
            sb.append(": "); // NOI18N
            sb.append(repository);
        }

        sb.append("</html>"); // NOI18N

        return sb.toString();
    }

    public int compareTo(Plugin other) {
        return name.compareTo(other.name);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
}
