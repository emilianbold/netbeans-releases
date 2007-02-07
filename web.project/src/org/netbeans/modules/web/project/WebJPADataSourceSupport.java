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

package org.netbeans.modules.web.project;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;

/**
 * Provides support for dealing with data sources in the JSR 220 support module. 
 * 
 * @author Erno Mononen
 */
public class WebJPADataSourceSupport implements JPADataSourcePopulator, JPADataSourceProvider{
    
    private final WebProject project;
    
    /** Creates a new instance of WebJPADataSourceSupport */
    public WebJPADataSourceSupport(WebProject project) {
        this.project = project;
    }
    
    public void connect(JComboBox comboBox) {
        DatasourceUIHelper.connect(project.getWebModule(), comboBox);
        // see the comment in EjbJarJPASupport regarding the code below
        int size = comboBox.getItemCount();
        for (int i = 0; i < size; i++){
            Object item = comboBox.getItemAt(i);
            if (item instanceof Datasource){
                comboBox.insertItemAt(new DatasourceWrapper((Datasource)item), i);
            }
        }
    }

    public List<JPADataSource> getDataSources() {
        List<Datasource> datasources = new ArrayList<Datasource>();
        datasources.addAll(project.getWebModule().getModuleDatasources());
        datasources.addAll(project.getWebModule().getServerDatasources());

        List<JPADataSource> result = new ArrayList<JPADataSource>(datasources.size());
        for(Datasource each : datasources){
            result.add(new DatasourceWrapper(each));
        }
        return result;
    }

/**
 * Provides <code>JPADataSource</code> interface for <code>Datasource</code>s.
 */ 
// TODO: this class is duplicated in the EjbJarJPASupport
private static class DatasourceWrapper implements Datasource, JPADataSource{
    
    private Datasource delegate;
    
    DatasourceWrapper(Datasource datasource){
        this.delegate = datasource;
    }
    
    public String getJndiName() {
        return delegate.getJndiName();
    }
    
    public String getUrl() {
        return delegate.getUrl();
    }
    
    public String getUsername() {
        return delegate.getUsername();
    }
    
    public String getPassword() {
        return delegate.getPassword();
    }
    
    public String getDriverClassName() {
        return delegate.getDriverClassName();
    }
    
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    
    public String toString(){
        return delegate.toString();
    }
}

}
