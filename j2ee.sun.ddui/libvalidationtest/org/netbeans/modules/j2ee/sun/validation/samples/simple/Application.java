/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.validation.samples.simple;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;

import org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.*;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;
import org.netbeans.modules.j2ee.sun.validation.util.Display;
import org.netbeans.modules.j2ee.sun.validation.util.Utils;
import org.netbeans.modules.j2ee.sun.validation.ValidationManager;
import org.netbeans.modules.j2ee.sun.validation.ValidationManagerFactory;



/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class Application {
    
    /** Creates a new instance of sample */
    public Application() {
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RootElement rootElement =  null;
        String fileBeingValidated = "org/netbeans/modules/" +           //NOI18N
            "j2ee/sun/validation/samples/simple/simple.xml";            //NOI18N

        String validationFile = "org/netbeans/modules/" +               //NOI18N
            "j2ee/sun/validation/samples/simple/validation.xml";        //NOI18N

///    String validationFile = "C:/testframe/tests/org/netbeans/" +     //NOI18N
///        "modules/j2ee/sun/validation/samples/simple/validation.xml"; //NOI18N


//      You should set impl.file to fully qualified file name of the 
//      impl(implementation) file
///        String implFile = "com.sun.enterprise.tools." +              //NOI18N
///            "common.XYZImpl";                                        //NOI18N
///     System.setProperty("impl.file", implFile);                      //NOI18N


//      You can set constraints.file to either absolute or relative path of the
//      Constraints file
///     String cosntriantsFile = "com/sun/enterprise/tools/" +          //NOI18N
///         "common/testXYZ.xml";                                       //NOI18N
///     String cosntriantsFile = "C:/testframe/src/java/com/sun/" +     //NOI18N
///         "enterprise/tools/XYZ/testXYZ.xml";                         //NOI18N
///     System.setProperty("constraints.file", cosntriantsFile);        //NOI18N


        //Create an InpurtStream object
        Utils utils = new Utils();
        InputStream inputStream = utils.getInputStream(fileBeingValidated);

        //Create graph
        if(inputStream != null) {
            try {
                rootElement = RootElement.createGraph(inputStream);
            } catch(Exception e) {
               System.out.println(e.getMessage());
            }
        } else {
            String format = 
                BundleReader.getValue("MSG_Unable_to_use_file");        //NOI18N
            Object[] arguments = new Object[]{fileBeingValidated};
            System.out.println(MessageFormat.format(format, arguments));
        }

        if(rootElement != null){
            ValidationManagerFactory validationManagerFactory = 
                new ValidationManagerFactory();

            //you can pass either absolute or relative path of the validation
            //file to getValidationManager()
            ValidationManager validationManager = 
                validationManagerFactory.getValidationManager(validationFile);

            Collection failures =
                validationManager.validate(rootElement);

            Display display = new Display();
            display.text(failures);
            display.gui(failures);
        }
    }
}
