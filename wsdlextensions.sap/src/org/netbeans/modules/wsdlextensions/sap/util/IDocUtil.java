/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.sap.util;

import java.util.ArrayList;
import com.sap.conn.jco.*;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.wsdlextensions.sap.model.IDocType;

/**
 *
 * @author dradhakrishnan
 * @author jqian
 */
public class IDocUtil {

    private JCoTable IDOC_EXTN_LIST = null;
    private JCoTable IDOC_LIST = null;
    private JCoTable IDOC_MSG_LIST = null;
    private Logger mLogger = Logger.getAnonymousLogger();
   
    /**
     * Gets the list of all released Idocs and their extensions.
     */
    public List<IDocType> getIDocTypes(
            JCoDestination destination,
            String internalSystemRelease)
            throws Exception {

        if (destination == null) {
            mLogger.warning(" destination is NULL. Cannot get the IDoc List");
            return null;
        }

        try {
            // Create a new repository
            JCoRepository repository = destination.getRepository();
            // Get a function template from the repository
            JCoFunctionTemplate funcTemplate =
                    repository.getFunctionTemplate("IDOCTYPES_LIST_WITH_MESSAGES");
            // if the function definition was found in backend system
            if (funcTemplate != null) {
                // Create a function from the template
                JCoFunction jcoFunction = funcTemplate.getFunction();
                if (jcoFunction != null) {
                    jcoFunction.getImportParameterList().setValue("PI_RELEASE", internalSystemRelease);
                    // Call the function
                    jcoFunction.execute(destination);
                    if (jcoFunction.getTableParameterList().getTable(1).getNumRows() != 0) {
                        IDOC_EXTN_LIST = jcoFunction.getTableParameterList().getTable(0);
                        IDOC_LIST = jcoFunction.getTableParameterList().getTable(1);
                        IDOC_MSG_LIST = jcoFunction.getTableParameterList().getTable(2);
                    } else {
                        System.err.println("No IDoc Types found.");
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Failed to get the IDoc List for the connected SAP system. Please enter the IDoc type manually.",
                        "Warning!",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            throw new Exception("Error is : " + e.getMessage());
        }

        // Put the contents into an HashMap
        return getIDocList();
    }

    private List<IDocType> getIDocList() {
        List<IDocType> ret = new ArrayList<IDocType>();

        if (IDOC_LIST != null && IDOC_LIST.getNumRows() > 0) {
            do {
                JCoFieldIterator e = IDOC_LIST.getFieldIterator();
                JCoField field = e.nextField();
                String idocName = field.getString();
                field = e.nextField();
                String idocDesc = field.getString();
                field = e.nextField();  // skipping DEVCLASS
                field = e.nextField();  // retrieving RELEASED
                String releaseIn = field.getString();
                ret.add(new IDocType(idocName, idocDesc, releaseIn));
            } while (IDOC_LIST.nextRow());
        }

        return ret;
    }

    /**
     * Gets an array of extensions for the given IDoc type.
     *
     * @param idocType  IDoc type in String
     * @return an array of IDoc type extensions
     */
    public String[] getIDocExtensionList(String idocType) {
        List<String> ret = new ArrayList<String>();

        if (IDOC_EXTN_LIST != null && IDOC_EXTN_LIST.getNumRows() > 0) {
            IDOC_EXTN_LIST.setRow(0);
            do {
                JCoFieldIterator e = IDOC_EXTN_LIST.getFieldIterator();
                JCoField field0 = e.nextField();
                e.nextField();
                e.nextField();
                //JCO3 fld points to field at 3rd positon
                JCoField field3 = e.nextField();
                if (field3.getString().compareTo(idocType) == 0) {
                    ret.add(field0.getString());
                }
            } while (IDOC_EXTN_LIST.nextRow());
        }

        return ret.toArray(new String[0]);
    }

    /**
     * Gets the message type for the given IDoc type and segment release.
     *
     * @param idocType      IDoc type in String
     * @param segRelease    segment release in String
     * @return Message type in String
     */
    public String getMessageType(String idocType, String segRelease) {
        String msgType = null;

        if (IDOC_MSG_LIST != null && IDOC_MSG_LIST.getNumRows() > 0) {
            for (int i = 0; i < IDOC_MSG_LIST.getNumRows(); i++) {
                IDOC_MSG_LIST.setRow(i);
                boolean sameIdocType = IDOC_MSG_LIST.getString("IDOCTYP").compareTo(idocType) == 0;
                boolean released = IDOC_MSG_LIST.getString("RELEASED").compareTo(segRelease) <= 0;
                if (sameIdocType && released) {
                    msgType = IDOC_MSG_LIST.getString("MESTYP");
                    break;
                }
            }
        }
        
        return msgType;
    }
}


