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

package org.netbeans.modules.javadoc.search;

import java.util.StringTokenizer;
import java.io.*;

import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.filesystems.FileObject;

/** Abstract class for thread which searches for documentation
 *
 *  @author Petr Hrebejk, Petr Suchomel
 */

public abstract class IndexSearchThread extends Thread  {

    // PENDING: Add some abstract methods

    //protected String                toFind;
    protected FileObject            fo;
    private   DocIndexItemConsumer  ddiConsumer;
    RequestProcessor.Task           rpTask = null;
    protected boolean caseSensitive;
    
    protected String lastField="";     //NOI18N
    protected String middleField="";   //NOI18N    
    protected String reminder="";   //NOI18N
    private int tokens=0;

    private String lastAdd ="";   //NOI18N
    private String lastDeclaring="";   //NOI18N
    /** This method must terminate the process of searching */
    abstract void stopSearch();

    public IndexSearchThread( String toFind, FileObject fo, DocIndexItemConsumer ddiConsumer, boolean caseSensitive ) {
        this.ddiConsumer = ddiConsumer;
        this.fo = fo;
        this.caseSensitive = caseSensitive;
        
        //this.toFind = toFind;
        //rpTask = RequestProcessor.createRequest( this );

        StringTokenizer st = new StringTokenizer(toFind, ".");     //NOI18N
        tokens = st.countTokens();
        //System.out.println(tokens);
        
        if( tokens > 1 ){
            if( tokens == 2 ){
                middleField = st.nextToken();
                lastField   = st.nextToken();
            }
            else{
                for( int i = 0; i < tokens-2; i++){
                    reminder += st.nextToken();
                    if( i+1 < tokens-2 )
                        reminder += '.';
                }            
                middleField = st.nextToken();
                lastField   = st.nextToken();
            }            
        }
        else{
            lastField = toFind;            
        }
        if( !caseSensitive ){
            reminder    = reminder.toUpperCase();
            middleField = middleField.toUpperCase();
            lastField   = lastField.toUpperCase();
        }
        //System.out.println("lastField" + lastField);
    }

    protected synchronized void insertDocIndexItem( DocIndexItem dii ) {
        //no '.', can add directly
        //System.out.println("Inserting");
        /*
        try{
            PrintWriter pw = new PrintWriter( new FileWriter( "c:/javadoc.dump", true ));
            pw.println("\"" + dii.getField() +"\""+ " " + "\""+dii.getDeclaringClass()+ "\"" + " " + "\""+ dii.getPackage()+ "\"");
            pw.println("\"" + lastField + "\"" + " " + "\"" + middleField + "\"" + " " + "\"" + reminder + "\"");
            pw.flush();
            pw.close();
        }
        catch(IOException ioEx){ioEx.printStackTrace();}
        */
        String diiField = dii.getField();
        String diiDeclaringClass = dii.getDeclaringClass();
        String diiPackage = dii.getPackage();
        if( !caseSensitive ){
            diiField = diiField.toUpperCase();
            diiDeclaringClass = diiDeclaringClass.toUpperCase();
            diiPackage = diiPackage.toUpperCase();
        }
        
        if( tokens < 2 ){
            if( diiField.startsWith( lastField ) ){
                //System.out.println("------");
                //System.out.println("Field: " + diiField + " last field: " + lastAdd + " declaring " + diiDeclaringClass + " package " + diiPackage);
                if( !lastAdd.equals( diiField ) || !lastDeclaring.equals( diiDeclaringClass )){
                    //System.out.println("ADDED");
                    ddiConsumer.addDocIndexItem ( dii );
                    lastAdd = diiField;
                    lastDeclaring = diiDeclaringClass;
                }
                //System.out.println("------");                
            }
            else if( diiDeclaringClass.startsWith( lastField ) && dii.getIconIndex() == DocSearchIcons.ICON_CLASS ) {
                if( !lastAdd.equals( diiDeclaringClass ) ){
                    ddiConsumer.addDocIndexItem ( dii );//System.out.println("Declaring class " + diiDeclaringClass + " icon " + dii.getIconIndex() + " remark " + dii.getRemark());
                    lastAdd = diiDeclaringClass;
                }
            }
            else if( diiPackage.startsWith( lastField + '.' ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ) {
                if( !lastAdd.equals( diiPackage ) ){
                    ddiConsumer.addDocIndexItem ( dii );//System.out.println("Package " + diiPackage + " icon " + dii.getIconIndex() + " remark " + dii.getRemark());
                    lastAdd = diiPackage;
                }
            }
        }
        else{            
            if( tokens == 2 ){
                //class and field (method etc. are equals)
                //System.out.println(dii.getField() + "   " + lastField + "   " + dii.getDeclaringClass() + "   " + middleField);
                if( diiField.startsWith(lastField) && diiDeclaringClass.equals(middleField) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( middleField.toUpperCase() ) && diiDeclaringClass.equals( lastField ) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( (middleField + '.' + lastField).toUpperCase() ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
            }
            else{            
                //class and field (method etc. are equals)
                if( diiField.startsWith(lastField) && diiDeclaringClass.equals(middleField) && diiPackage.startsWith( reminder.toUpperCase() ) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                //else if( diiDeclaringClass.equals(lastField) && diiPackage.startsWith( (reminder + '.' + middleField).toUpperCase()) ){
                else if( diiDeclaringClass.startsWith(lastField) && diiPackage.equals( (reminder + '.' + middleField + '.').toUpperCase()) ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
                else if( diiPackage.startsWith( (reminder + '.' + middleField + '.' + lastField).toUpperCase() ) && dii.getIconIndex() == DocSearchIcons.ICON_PACKAGE ){
                    ddiConsumer.addDocIndexItem ( dii );
                }
            }
        }
    }

    public void go() {
        rpTask = RequestProcessor.postRequest( this, 0, NORM_PRIORITY );
    }

    public void finish() {
        if ( !rpTask.isFinished() && !rpTask.cancel() )
            stopSearch();
        taskFinished();
    }

    public void taskFinished() {
        ddiConsumer.indexSearchThreadFinished( this );
    }

    /** Class for callback. Used to feed some container with found
     * index items;
     */

    public static interface DocIndexItemConsumer {

        /** Called when an item is found */
        public void addDocIndexItem ( DocIndexItem dii );

        /** Called when a task finished. May be called more than once */
        public void indexSearchThreadFinished( IndexSearchThread ist );


    }

}

/*
 * Log
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/26/99  Petr Hrebejk    
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/16/99  Petr Hrebejk    
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 