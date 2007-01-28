/*
 * Class.java
 *
 * Created on August 16, 2004, 7:07 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import javax.swing.DefaultListModel;

/**
 * The data model for the client jar list in the ejb group panel
 *
 * @author  cao
 */
public class ClientJarFileListModel extends DefaultListModel {
    
    public ClientJarFileListModel() {
    }
    
    public void addJarFile( String jarFile )
    {
        // Check duplication
        for( int i = 0; i < super.getSize(); i ++ )
        {
            String fileName = (String)super.getElementAt( i );
            if( fileName.equals( jarFile ) )
                return;
        }
        
        super.addElement( jarFile );
    }
}
