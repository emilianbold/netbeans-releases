/*
 * ImportEjbDataSourcesHelper.java
 *
 * Created on September 10, 2004, 1:51 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourcesImportor;
import java.io.File;
import java.util.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * A helper for importing ejb datasources
 *
 * @author  cao
 */
public class ImportEjbDataSourcesHelper {
    
    /** Creates a new instance of ImportEjbDataSourcesHelper */
    public ImportEjbDataSourcesHelper() {
    }
    
    public static PortableEjbDataSource[] readDataSourceImports( String filePath )
    {
        // First, check file existence
        if( !(new File(filePath)).exists() )
        {
            String msg = NbBundle.getMessage(ImportEjbDataSourcesHelper.class, "IMPORT_FILE_NOT_FOUND", filePath );
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return null;
        }
        
        EjbDataSourcesImportor importor = new EjbDataSourcesImportor( filePath );
        Collection ejbGrps = importor.getEjbGroups();
        
        if( ejbGrps != null && !ejbGrps.isEmpty() )
        {
            PortableEjbDataSource[] ejbDataSources = new PortableEjbDataSource[ ejbGrps.size() ];
            int i = 0;
            for( Iterator iter = ejbGrps.iterator(); iter.hasNext(); ) {
                EjbGroup grp = (EjbGroup)iter.next();
                ejbDataSources[i++] = new PortableEjbDataSource( grp );
            }
            return ejbDataSources;
        }
        else
            return new PortableEjbDataSource[0];
    }
}
