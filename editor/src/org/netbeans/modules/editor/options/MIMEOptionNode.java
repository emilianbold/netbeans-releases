/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.beans.IntrospectionException;
import java.io.IOException;

import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.TopManager;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.BeanNode;
import org.openide.util.NbBundle;
import org.netbeans.editor.BaseKit;
import java.util.StringTokenizer;
import org.openide.loaders.DataObjectExistsException;


/** MIME Option Node Representation.
 *  Each subClass of BaseOptions is represented via MIMEOptionNode.
 *  MIMEOptionNode creates MIME specific settings folder if it doesn't exists.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class MIMEOptionNode extends BeanNode {
    
    private String name;
    private BaseOptions base;
    private MIMEOptionFolder settingsFolder;
    
    /** Creates new OptionNode */
    public MIMEOptionNode(BaseOptions beanObject) throws IntrospectionException {
        super(beanObject);
        base = beanObject;
        name = BaseKit.getKit(base.getKitClass()).getContentType();
        if (name == null) name = base.getTypeName();
    }
    
    /** Gets display name of all options node from bundle */
    public String getDisplayName(){
        return base.getName();
    }
    
    /** Lazy initialization of the MIME specific settings folder. The folder should be created
     *  via XML layers, if not, it will be created.
     *  Instances of all XML file in this folder will be created.
     */
    public synchronized MIMEOptionFolder getMIMEFolder(){
        // return already initialized folder
        if (settingsFolder!=null) return settingsFolder;
        
        if (BaseKit.getKit(base.getKitClass()).getContentType() == null) return null;
        
        FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(AllOptionsFolder.FOLDER+"/"+name); //NOI18N
        
        // MIME folder doesn't exist, let's create it
        if (f==null){
            FileObject fo = TopManager.getDefault().getRepository().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER);
            
            if (fo != null){
                try{
                    StringTokenizer stok = new StringTokenizer(name,"/");
                    while (stok.hasMoreElements()) {
                        String newFolder = stok.nextToken();
                        if (fo.getFileObject(newFolder) == null){
                            fo = fo.createFolder(newFolder);
                        }
                        else
                            fo = fo.getFileObject(newFolder);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
                
                f = TopManager.getDefault().getRepository().getDefaultFileSystem().
                findResource(AllOptionsFolder.FOLDER+"/"+name);
            }
        }
        
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
                if (df != null) {
                    settingsFolder = new MIMEOptionFolder(df, base);
                    return settingsFolder;
                }
            } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        
        return null;
    }
    
}
