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
package org.netbeans.modules.xsl.settings;

import java.util.*;
import java.io.*;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.xsl.utils.TransformUtil;


/**
 * Transformation history of one XML or XSLT document. Used as FileObject attribute and also as TransformPanel model.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TransformHistory implements Serializable {
    /** Serial Version UID */
    private static final long serialVersionUID = -6268945703343989727L;

    /** Last selected XMLs with associated Outputs. Can be null. */
    private ListMap xmlOutputMap; // Map<String,String>

    /** Last selected XSLs with associated Outputs. Can be null. */
    private ListMap xslOutputMap; // Map<String,String>

    /** Automatically overwrite output. */
    private boolean overwriteOutput;

    /** What to do with output file: DO_NOTHING | APPLY_DEFAULT_ACTION | OPEN_IN_BROWSER. */
    private int processOutput;

    /** Do nothing with output file. */
    public static final int DO_NOTHING = 0;
    /** Apply default action on output file. */
    public static final int APPLY_DEFAULT_ACTION = 1;
    /** Open output file int browser. */
    public static final int OPEN_IN_BROWSER = 2;

    /** FileObject's attribute name. */
    public static final String TRANSFORM_HISTORY_ATTRIBUTE =
        "org.netbeans.modules.xsl.settings.TransformHistory"; // NOI18N

    //
    // init
    //

    /** Creates new TransformHistory.
     */
    public TransformHistory () {
        xmlOutputMap = null;
        xslOutputMap = null;
        overwriteOutput = false;
        processOutput = OPEN_IN_BROWSER;
    }


    public String[] getXMLs () {
        return getXMLOutputMap().getInputs();
    }

    public String getLastXML () {
        return getXMLOutputMap().getLastInput();
    }

    public String[] getXSLs () {
        return getXSLOutputMap().getInputs();
    }

    public String getLastXSL () {
        return getXSLOutputMap().getLastInput();
    }

    public String getXMLOutput (String xml) {
        return getXMLOutputMap().getOutput (xml);
    }

    public String getLastXMLOutput () {
        return getXMLOutput (getLastXML());
    }

    public String getXSLOutput (String xsl) {
        return getXSLOutputMap().getOutput (xsl);
    }

    public String getLastXSLOutput () {
        return getXSLOutput (getLastXSL());
    }

    public void addXML (String xml, String output) {
        getXMLOutputMap().put (xml, output);
    }

    public void addXSL (String xsl, String output) {
        getXSLOutputMap().put (xsl, output);        
    }

    public boolean isOverwriteOutput () {
        return overwriteOutput;
    }

    public void setOverwriteOutput (boolean overwrite) {
        overwriteOutput = overwrite;
    }

    public int getProcessOutput () {
        return processOutput;
    }

    public void setProcessOutput (int process) {
        processOutput = process;
    }


    private ListMap getXMLOutputMap () {
        if ( xmlOutputMap == null ) {
            xmlOutputMap = new ListMap();
        }
        return xmlOutputMap;
    }

    private ListMap getXSLOutputMap () {
        if ( xslOutputMap == null ) {
            xslOutputMap = new ListMap();
        }
        return xslOutputMap;
    }

    public String toString () {
        StringBuffer sb = new StringBuffer (super.toString());
        sb.append (" [ xmlOutputMap= ").append (xmlOutputMap);
        sb.append (", xslOutputMap= ").append (xslOutputMap);
        sb.append (", overwriteOutput= ").append (overwriteOutput);
        sb.append (", processOutput= ").append (processOutput).append (" ]");
        return sb.toString();
    }
    
    public boolean equals (Object obj) {
        if ( ( obj instanceof TransformHistory ) == false ) {
            return false;
        }
        TransformHistory peer = (TransformHistory)obj;
        if ( equals (this.xmlOutputMap, peer.xmlOutputMap) == false ) {
            return false;
        }
        if ( equals (this.xslOutputMap, peer.xslOutputMap) == false ) {
            return false;
        }
        if ( this.overwriteOutput != peer.overwriteOutput ) {
            return false;
        }
        if ( this.processOutput != peer.processOutput ) {
            return false;
        }
        return true;
    }
    
    
    //
    // utils
    //
    static boolean equals (Object obj1, Object obj2) {
        if ( obj1 != null ) {
            return (obj1.equals (obj2));
        } else {
            return (obj1 == obj2);
        }
    }

    
    //
    // class ListMap
    //
    private static class ListMap implements Serializable {
        /** Serial Version UID */
        private static final long serialVersionUID = 6341102578706167575L;

        /** Max length of history. */
        public static final int MAX = 5;
        
        transient private List inputList;
        transient private Map inputOutputMap;
        /** Serializable mirror of inputList and inputOutputMap fields. */
        private Object[] inputOutputArray;



        public ListMap () {
            init();
        }

        private void init () {
            inputList = new LinkedList();
            inputOutputMap = new HashMap();

            if ( inputOutputArray == null ) {
                return;
            }
            for ( int i = 0; i < inputOutputArray.length; i+=2 ) {
                Object input = inputOutputArray[i];
                Object output = inputOutputArray[i+1];

                try { // just hacks to avoid non-String values
                    // check input
                    if ( input instanceof FileObject ) {
                        input = TransformUtil.getURLName ((FileObject) input);
                    } else if ( ( input != null ) &&
                                ( input instanceof String ) == false ) {
                        input = input.toString();
                    }
                    // check output
                    if ( output instanceof FileObject ) {
                        output = TransformUtil.getURLName ((FileObject) output);
                    } else if ( ( output != null ) &&
                                ( output instanceof String ) == false ) {
                        output = output.toString();
                    }

                    inputList.add (input);
                    inputOutputMap.put (input, output);
                } catch (IOException exc) { // TransformUtil.getURLName
                    // ignore it

                    Util.THIS.debug (exc);
                }
            }
        }

        public void put (String input, String output) {
            // remove old value
            Object old = inputOutputMap.remove (input);
            inputList.remove (input);

            // add new value at first place
            inputOutputMap.put (input, output);
            inputList.add (0, input);
            
            // keep just ${MAX} entries
            if ( inputList.size() > MAX ) {
                Object over = inputList.remove (inputList.size() - 1);
                inputOutputMap.remove (over);
            }
        }

        public String[] getInputs () {
            return (String[]) inputList.toArray (new String[0]);
        }
        
        public String getLastInput () {
            if ( inputList.isEmpty() ) {
                return null;
            }
            return (String) inputList.get (0);
        }

        public String getOutput (String input) {
            return (String) inputOutputMap.get (input);
        }

        public String[] getArray () {
            if ( inputList.size() == 0 ) {
                return null;
            }
            String[] array = new String [2 * inputList.size()];
            for ( int i = 0; i < inputList.size(); i++ ) {
                String input = (String) inputList.get (i);
                array[2*i] = input;
                array[(2*i)+1] = (String) inputOutputMap.get (input);
            }
            return array;
        }

        public String toString () {
            StringBuffer sb = new StringBuffer (super.toString());
            sb.append (" [ inputList= ").append (inputList);
            sb.append (", inputOutputMap.keySet= ").append (inputOutputMap.keySet());
            sb.append (", inputOutputMap.values= ").append (inputOutputMap.values());
            sb.append (", xmlOutputArray= ").append (inputOutputArray == null ? "null" : Arrays.asList (inputOutputArray).toString());
            sb.append (" ]");
            return sb.toString();
        }

        public boolean equals (Object obj) {
            if ( ( obj instanceof ListMap ) == false ) {
                return false;
            }
            ListMap peer = (ListMap)obj;
            if ( TransformHistory.equals (this.inputList, peer.inputList) == false ) {
                return false;
            }
            if ( TransformHistory.equals (this.inputOutputMap, peer.inputOutputMap) == false ) {
                return false;
            }
            return true;
        }
    
        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();

            init();
            inputOutputArray = null;
        }
        

        private void writeObject (ObjectOutputStream oos) throws IOException {
            inputOutputArray = getArray();
            
            oos.defaultWriteObject();
            
            inputOutputArray = null;
        }
        
    } // class ListMap

}
