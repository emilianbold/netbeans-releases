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

import org.openide.util.Task;
import org.openide.loaders.FolderInstance;
import org.openide.cookies.InstanceCookie;
import java.lang.ClassNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.TopManager;
import org.openide.loaders.DataObject;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.lang.String;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.AnnotationType;
import java.util.Iterator;
import org.netbeans.editor.AnnotationTypes;
import org.w3c.dom.*;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import java.lang.Exception;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;

/** Representation of the "Editors/AnnotationTypes" folder. All
 * instances created through the createInstance() method are
 * stored in Map and passed to AnnotationType.setTypes(). This
 * class should only be responsible for processing of the folder, 
 * listening of the changes in folder etc. Clients should use 
 * AnnotationType.getType and other methods in AnnotationType 
 * for access to AnnotationTypes.
 *
 * @author  David Konecny
 * @since 07/2001
 */
public class AnnotationTypesFolder extends FolderInstance{
    
    /** folder for annotation type XML files */
    private static final String FOLDER = "Editors/AnnotationTypes";
    
    /** instance of this class */
    private static AnnotationTypesFolder folder;

    /** map of annotationtype_name <-> AnnotationType_instance*/
    private Map annotationTypes;

    /** FileObject which represent the folder with annotation types*/
    private FileObject fo;
    
    /** Creates new AnnotationTypesFolder */
    private AnnotationTypesFolder(FileObject fo, DataFolder fld) {
        super(fld);
        recreate();
        instanceFinished();
        
        this.fo = fo;
        
        // add listener on changes in annotation types folder
        fo.addFileChangeListener(new FileChangeAdapter() {
            public void fileDeleted(FileEvent fe) {
                AnnotationType type;
                for (Iterator it = AnnotationTypes.getTypes().getAnnotationTypeNames(); it.hasNext(); ) {
                    type = AnnotationTypes.getTypes().getType((String)it.next());
                    if ( ((FileObject)type.getProp(AnnotationType.PROP_FILE)).equals(fe.getFile()) ) {
                        AnnotationTypes.getTypes().removeType(type.getName());
                        break;
                    }
                }
            }
        });
        
    }

    /** Gets AnnotationTypesFolder singleton instance. */
    public static synchronized AnnotationTypesFolder getAnnotationTypesFolder(){
        if (folder != null) {
            return folder;
        }
        
        FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource(FOLDER);
        if (f == null) {
            return null;
        }
        
        try {
            DataObject d = DataObject.find(f);
            DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
            if (df != null)
                folder = new AnnotationTypesFolder(f, df);
        } catch (org.openide.loaders.DataObjectNotFoundException ex) {
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                ex.printStackTrace();
            return null;
        }
        return folder;
    }

    /** Called for each XML file found in FOLDER directory */
    protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        annotationTypes = new HashMap(cookies.length * 4 / 3);
        
        for (int i = 0; i < cookies.length; i++) {
            Object o = cookies[i].instanceCreate();
            if (o instanceof AnnotationType) {
                AnnotationType type = (AnnotationType)o;
                annotationTypes.put(type.getName(), type);
            }
        }
        
        // set all these types to AnnotationType static member
        AnnotationTypes.getTypes().setTypes(annotationTypes);
        
        return null;
    }

    /** Save changed AnnotationType */
    public void saveAnnotationType(AnnotationType type) {

        FileObject fo = (FileObject)type.getProp(AnnotationType.PROP_FILE);

        Document doc = XMLUtil.createDocument(AnnotationTypeProcessor.TAG_TYPE, null, AnnotationTypeProcessor.DTD_PUBLIC_ID, AnnotationTypeProcessor.DTD_SYSTEM_ID);
        Element typeElem = doc.getDocumentElement();

        typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_NAME, type.getName());
        if (type.getProp(AnnotationType.PROP_LOCALIZING_BUNDLE) != null)
            typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_LOCALIZING_BUNDLE, (String)type.getProp(AnnotationType.PROP_LOCALIZING_BUNDLE));
        if (type.getProp(AnnotationType.PROP_DESCRIPTION_KEY) != null)
            typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_DESCRIPTION_KEY, (String)type.getProp(AnnotationType.PROP_DESCRIPTION_KEY));
        typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_VISIBLE, type.isVisible() ? "true" : "false"); // NOI18N
        typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_TYPE, type.isWholeLine() ? "line" : "linepart"); // NOI18N
        if (type.getProp(AnnotationType.PROP_GLYPH_URL) != null)
            typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_GLYPH, type.getGlyph().toExternalForm());
        if (type.getProp(AnnotationType.PROP_HIGHLIGHT_COLOR) != null)
            typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_HIGHLIGHT, "0x"+Integer.toHexString(type.getHighlight().getRGB() & 0x00FFFFFF));
        if (type.getProp(AnnotationType.PROP_FOREGROUND_COLOR) != null)
            typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_FOREGROUND, "0x"+Integer.toHexString(type.getForegroundColor().getRGB() & 0x00FFFFFF));
        if (type.getProp(AnnotationType.PROP_ACTIONS_FOLDER) != null)
            typeElem.setAttribute(AnnotationTypeProcessor.ATTR_TYPE_ACTIONS, (String)type.getProp(AnnotationType.PROP_ACTIONS_FOLDER));
        
        if (type.getCombinations() != null) {
            
            Element combsElem = doc.createElement(AnnotationTypeProcessor.TAG_COMBINATION);
            combsElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINATION_TIPTEXT_KEY, (String)type.getProp(AnnotationType.PROP_COMBINATION_TOOLTIP_TEXT_KEY));
            if (type.getProp(AnnotationType.PROP_COMBINATION_ORDER) != null)
                combsElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINATION_ORDER, ""+type.getCombinationOrder());
            if (type.getProp(AnnotationType.PROP_COMBINATION_MINIMUM_OPTIONALS) != null)
                combsElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINATION_MIN_OPTIONALS, ""+type.getMinimumOptionals());

            typeElem.appendChild(combsElem);

            AnnotationType.CombinationMember[] combs = type.getCombinations();
            for (int i=0; i < combs.length; i++) {
                Element combElem = doc.createElement(AnnotationTypeProcessor.TAG_COMBINE);
                combElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINE_ANNOTATIONTYPE, combs[i].getName());
                combElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINE_ABSORBALL, combs[i].isAbsorbAll() ? "true" : "false"); // NOI18N
                combElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINE_OPTIONAL, combs[i].isOptional() ? "true" : "false"); // NOI18N
                if (combs[i].getMinimumCount() > 0)
                    combElem.setAttribute(AnnotationTypeProcessor.ATTR_COMBINE_MIN, ""+combs[i].getMinimumCount()); 
                combsElem.appendChild(combElem);
            }
        }
        
        doc.getDocumentElement().normalize();
        
        try{
            FileLock lock = fo.lock();
            try {
                XMLUtil.write(doc, fo.getOutputStream(lock), null);
            } catch (Exception ex){
                if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                    ex.printStackTrace();
            } finally {
                lock.releaseLock();
            }
        }catch (IOException ex){
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                ex.printStackTrace();
        }
        
    }
    
}
