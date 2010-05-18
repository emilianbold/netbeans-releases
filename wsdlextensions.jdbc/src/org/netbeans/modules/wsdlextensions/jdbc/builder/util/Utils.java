package org.netbeans.modules.wsdlextensions.jdbc.builder.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import java.util.MissingResourceException;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.openide.util.NbBundle;

import org.netbeans.modules.wsdlextensions.jdbc.builder.Procedure;
import org.netbeans.modules.wsdlextensions.jdbc.builder.Parameter;

/** 
 * Class containing some utility methods.
 *
 * @author Susan Chen
 * @version $Revision: 1.1 $
 */
public class Utils {

    private Utils() {
    }

    /** 
     * Replaces all occurrences of a single character with a new String.
     *
     * @param orig original string
     * @param oldChar character to replace
     * @param replStr string to replace character with
     * @return new string after replacement
     */
    public static String replaceAllChars(String orig, char oldChar, String replStr) {
        String newString = "";

        for (int i = 0; i < orig.length(); i++) {
            if (orig.charAt(i) == oldChar) {
                newString = newString + replStr;
            } else {
                newString = newString + orig.charAt(i);
            }
        }
        return newString;
    }

    /** 
     * Returns a key formed by concatenating catalog, schema, name, and type  
     * in the following format: <catalog>.<schema>.<name>.<type>
     *
     * @param catalog catalog
     * @param schema schema
     * @param name name
     * @param type type
     * @return key
     */
    public static String getKey(String catalog, String schema, String name, String type) {
        return catalog + "." + schema + "." + name + "." + type;
    }

    /**
     * Sets mnemonic for the given component, using the String associated with
     * the given localized resource key and the Bundle associated with this class.
     *
     * @param component Component whose mnemonic is to be set
     * @param mnemonicKey resource key name for looking up associated mnemonic char
     */
    public static void setLocalizedMnemonicFor(JComponent component,
            String mnemonicKey) {
        setLocalizedMnemonicFor(component, mnemonicKey, Utils.class);
    }

    /**
     * Sets mnemonic for the given component, using the String associated with
     * the given localized resource key and the Bundle associated with the
     * given class.
     *
     * @param component Component whose mnemonic is to be set
     * @param mnemonicKey resource key name for looking up associated mnemonic char
     * @param bundleClass Class to use in resolving resource bundle
     */
    public static void setLocalizedMnemonicFor(JComponent component,
            String mnemonicKey, Class bundleClass) {
        char mnemonic = '\0';

        try {
            String value = NbBundle.getMessage(bundleClass,
                    mnemonicKey);
            if (value != null && value.trim().length() != 0) {
                mnemonic = value.trim().charAt(0);
            }
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }

        if (mnemonic != '\0') {
            if (component instanceof JLabel) {
                ((JLabel) component).setDisplayedMnemonic(mnemonic);
            } else if (component instanceof AbstractButton) {
                ((AbstractButton) component).setMnemonic(mnemonic);
            } else {
                System.err.println("Unknown component type: not setting mnemonic for " + component);
            }
        }
    }

    /**
     * Centers given window on the current display screen.
     *
     * @param window Window (dialog, etc.) to be centered.
     */
    public static void centerWindowOnScreen(Window window) {
        window.pack();

        Rectangle rect = window.getBounds();
        Dimension scrnDim = Toolkit.getDefaultToolkit().getScreenSize();

        rect.x = Math.max(0, (scrnDim.width - rect.width) / 2);
        rect.y = Math.max(0, (scrnDim.height - rect.height) / 2);

        window.setBounds(rect);
    }

    /**
     ** This routine is for Oralce stored procedure Only.
     ** All other DB eWay should use getObjectName.
     **
     ** handle full name generation for
     ** DB object, tables, procedures, ...
     **/
    private String getSPObjectName(Procedure proc) {
        String baseName = proc.getName();
        if ((proc.getCatalog() != null) && !isEmpty(proc.getCatalog())) {
            String catalogName = proc.getCatalog();
            // Handle stored procedures in packages
            if ((proc.getSchema() != null) && !isEmpty(proc.getSchema())) {
                // return schema.package.procedure
                return new StringBuffer(proc.getSchema()).append('.').append(catalogName).append('.').append(baseName).toString();
            }
            // return package.procedure
            return new StringBuffer(catalogName).append('.').append(baseName).toString();
        } else {
            // Handle packageless stored procedures
            if (((proc.getSchema() != null) && !isEmpty(proc.getSchema()))) {
                // return schema.procedure
                return new StringBuffer(proc.getSchema()).append('.').append(baseName).toString();
            }
            // return procedure
            return baseName;
        }
    }

    /**
     ** handle full name generation for
     ** DB object, tables, procedures, ...
     **/
    private String getObjectName(String catalogName,
            String schemaName,
            String baseName,
            boolean useFullName) {
        if (useFullName) {
            int index = catalogName.indexOf("\\");
            String tmpStr = null;
            if (index > 0) {
                tmpStr = catalogName.substring(index + 1);
            } else {
                tmpStr = catalogName;
            }
            if (!isEmpty(tmpStr)) {
                if (!isEmpty(schemaName)) {
                    return tmpStr + "." + schemaName + "." + baseName;
                } else {
                    // catalog is not empty but schema is,
                    // be tolerant, use baseName;
                    return baseName;
                }
            } else {
                if (!isEmpty(schemaName)) {
                    // catalog is empty but schema is not
                    return schemaName + "." + baseName;
                } else {
                    return baseName;
                }
            }
        } else {
            return baseName;
        }
    }

    private boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private String getProcedureExecutionString(Procedure proc) {
        String list = "";
        boolean first = true;
        boolean hasReturn = false;
        Parameter[] params = proc.getParameters();
        for (int i = 0; i < params.length; i++) {

            if (params[i].getParamType().equals(Procedure.RETURN)) {
                hasReturn = true;
            } else {
                if (!first) {
                    list += ",";
                } else {
                    first = false;
                }
                list += " ?";
            }
        }
        if (hasReturn) {
            list = "{ ? = exec " + getSPObjectName(proc) + "(" + list + ")}";
        } else {
            list = "{ exec " + getSPObjectName(proc) + "(" + list + ")}";
        }
        return list;
    }
}
