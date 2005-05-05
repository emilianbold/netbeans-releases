package enroller;
/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;


public class EnrollerClient {
    public static void main(String[] args) {
        try {
            
            
            
            
            Context initial = new InitialContext();
            //Context initial = new InitialContext();
            Object objref = initial.lookup("ejb/SimpleStudent");
            StudentRemoteHome sHome =
                (StudentRemoteHome) PortableRemoteObject.narrow(objref,
                    StudentRemoteHome.class);

            StudentRemote denise = sHome.create("823", "Denise Smith");

            objref = initial.lookup("ejb/SimpleCourse");

            CourseRemoteHome cHome =
                (CourseRemoteHome) PortableRemoteObject.narrow(objref,
                    CourseRemoteHome.class);

            CourseRemote power = cHome.create("220", "Power J2EE Programming");

            objref = initial.lookup("ejb/SimpleEnroller");

            EnrollerRemoteHome eHome =
                (EnrollerRemoteHome) PortableRemoteObject.narrow(objref,
                    EnrollerRemoteHome.class);

            EnrollerRemote enroller = eHome.create();

            enroller.enroll("823", "220");
            enroller.enroll("823", "333");
            enroller.enroll("823", "777");
            enroller.enroll("456", "777");
            enroller.enroll("388", "777");

            System.out.println(denise.getName() + ":");

            ArrayList courses = denise.getCourseIds();
            Iterator i = courses.iterator();

            while (i.hasNext()) {
                String courseId = (String) i.next();
                CourseRemote course = cHome.findByPrimaryKey(courseId);

                System.out.println(courseId + " " + course.getName());
            }

            System.out.println();

            CourseRemote intro = cHome.findByPrimaryKey("777");

            System.out.println(intro.getName() + ":");
            courses = intro.getStudentIds();
            i = courses.iterator();

            while (i.hasNext()) {
                String studentId = (String) i.next();
                StudentRemote student = sHome.findByPrimaryKey(studentId);

                System.out.println(studentId + " " + student.getName());
            }

            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }
}
