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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;

/**
 *
 * @author mkleint
 */
class ProjectHIP extends HudsonInstanceProperties {

    private final Set<Project> providers = new HashSet<Project>();
    public ProjectHIP() {
        super("", "");
    }

    public void addProvider(Project prov) {
        synchronized (providers) {
            providers.add(prov);
        }
        ProjectHudsonProvider p = prov.getLookup().lookup(ProjectHudsonProvider.class);
        if (p != null) {
            setProperty(HUDSON_INSTANCE_URL, p.getServerUrl());
            setProperty(HUDSON_INSTANCE_NAME, p.getName());
        }
    }

    public void removeProvider(Project prov) {
        synchronized (providers) {
            providers.remove(prov);
        }
    }

    public Set<Project> getProviders() {
        synchronized (providers) {
            return new HashSet<Project>(providers);
        }
    }


    @Override
    public synchronized void load(InputStream inStream) throws IOException {
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
    }

    @Override
    public synchronized void save(OutputStream out, String comments) {
    }

    @Override
    public synchronized void store(OutputStream out, String comments) throws IOException {
    }

    @Override
    public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
    }

    @Override
    public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
    }


}
