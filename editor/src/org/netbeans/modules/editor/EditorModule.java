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

package org.netbeans.modules.editor;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.DialogSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.editor.java.JCStorage;
import org.netbeans.modules.editor.java.JCUpdateAction;
import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.options.JavaOptions;
import org.netbeans.modules.editor.options.HTMLOptions;
import org.netbeans.modules.editor.options.PlainOptions;
import org.netbeans.modules.editor.options.JavaPrintOptions;
import org.netbeans.modules.editor.options.HTMLPrintOptions;
import org.netbeans.modules.editor.options.PlainPrintOptions;

import org.openide.modules.ModuleInstall;
import org.openide.TopManager;
import org.openide.filesystems.FileSystem;
import org.openide.util.SharedClassObject;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListener;
import org.openide.filesystems.RepositoryListener;
import org.openide.filesystems.Repository;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import java.beans.IntrospectionException;
import org.openide.nodes.Children;
import java.util.Iterator;
import org.openide.nodes.BeanNode;
import org.netbeans.modules.editor.options.AnnotationTypeOptions;
import org.netbeans.editor.AnnotationType;


/**
* Module installation class for editor
*
* @author Miloslav Metelka
*/
public class EditorModule extends ModuleInstall implements Runnable {

    /** Kit replacements that will be installed into JEditorPane */
    KitInfo[] replacements = new KitInfo[] {
        new KitInfo(PlainKit.PLAIN_MIME_TYPE, PlainKit.class.getName(),
            PlainOptions.class, PlainPrintOptions.class),
        new KitInfo(JavaKit.JAVA_MIME_TYPE, JavaKit.class.getName(),
            JavaOptions.class, JavaPrintOptions.class),
        new KitInfo(HTMLKit.HTML_MIME_TYPE, HTMLKit.class.getName(),
            HTMLOptions.class, HTMLPrintOptions.class)
    };

    static final long serialVersionUID =-929863607593944237L;
    
    private DORegistryListener rl;
    
    private RepositListener repoListen;

    public void installed () {
        restored ();
    }

    /** Module installed again. */
    public void restored () {
        LocaleSupport.addLocalizer(new NbLocalizer(AllOptions.class));
        LocaleSupport.addLocalizer(new NbLocalizer(BaseKit.class));
        LocaleSupport.addLocalizer(new NbLocalizer(org.netbeans.editor.ext.java.JavaSettingsNames.class));

        // Initializations
        DialogSupport.setDialogFactory( new NbDialogSupport() );
        
        // register loader for annotation types
        AnnotationType.registerLoader( new AnnotationType.Loader() {
                public void load() {
                    AnnotationTypesFolder.getAnnotationTypesFolder();
                }
            } );

        // Settings
        NbEditorSettingsInitializer.init();

	// defer the rest of initialization, but enable a bit of paralelism
        org.openide.util.RequestProcessor.postRequest (this, 0, Thread.MIN_PRIORITY);

        // Options
        AllOptions ao = (AllOptions) SharedClassObject.findObject(AllOptions.class, true);
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        
        // Start listening on addition/removal of options
        ao.init();

        for (int i = 0; i < replacements.length; i++) {
            ao.addOption((SystemOption)SharedClassObject.findObject(replacements[i].optionsClass, true));
            ps.addOption((SystemOption)SharedClassObject.findObject(replacements[i].printOptionsClass, true));
        }


        // Registration of the editor kits to JEditorPane
        for (int i = 0; i < replacements.length; i++) {
            JEditorPane.registerEditorKitForContentType(
                replacements[i].contentType,
                replacements[i].newKitClassName,
                getClass().getClassLoader()
            );
        }

        // Start listening on DataObject.Registry
        if (rl == null) {
            rl = new DORegistryListener();
            DataObject.getRegistry().addChangeListener((ChangeListener)(WeakListener.change(rl, DataObject.getRegistry())));
        }

        /*
        if (repoListen==null){
            repoListen=new RepositListener();
            Repository repo = TopManager.getDefault().getRepository();
            if (repo!=null){
                repo.addRepositoryListener((RepositoryListener)(WeakListener.repository(repoListen, repo)));
            }
        }
         */
    }

    public void run () {
        // Java completion storage init
        FileSystem rfs = TopManager.getDefault().getRepository().getDefaultFileSystem();
        JCStorage.init(rfs.getRoot());

        // Preloading of some classes for faster editor opening
        BaseKit.getKit(JavaKit.class).createDefaultDocument();
    }

    public void uninstalled() {

        // Options
        AllOptions ao = (AllOptions) SharedClassObject.findObject(AllOptions.class, true);
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);

        for (int i = 0; i < replacements.length; i++) {
            ao.removeOption((SystemOption)SharedClassObject.findObject(replacements[i].optionsClass, true));
            ps.removeOption((SystemOption)SharedClassObject.findObject(replacements[i].printOptionsClass, true));
        }

        if (Boolean.getBoolean("netbeans.module.test")) { // NOI18N
            /* Reset the hashtable holding the editor kits, so the editor kit
            * can be refreshed. As the JEditorPane.kitRegistryKey is private
            * it must be accessed through the reflection.
            */
            try {
                java.lang.reflect.Field kitRegistryKeyField = JEditorPane.class.getDeclaredField("kitRegistryKey");  // NOI18N
                if (kitRegistryKeyField != null) {
                    kitRegistryKeyField.setAccessible(true);
                    Object kitRegistryKey = kitRegistryKeyField.get(JEditorPane.class);
                    if (kitRegistryKey != null) {
                        // Set a fresh hashtable. It can't be null as there is a hashtable in AppContext
                        sun.awt.AppContext.getAppContext().put(kitRegistryKey, new java.util.Hashtable());
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }


    static class KitInfo {

        /** Content type for which the kits will be switched */
        String contentType;

        /** Class name of the kit that will be registered */
        String newKitClassName;
        
        /** Class holding the options for the kit */
        Class optionsClass;
        
        /** Class holding the print options for the kit */
        Class printOptionsClass;

        KitInfo(String contentType, String newKitClassName, Class optionsClass, Class printOptionsClass) {
            this.contentType = contentType;
            this.newKitClassName = newKitClassName;
            this.optionsClass = optionsClass;
            this.printOptionsClass = printOptionsClass;
        }

    }
}
