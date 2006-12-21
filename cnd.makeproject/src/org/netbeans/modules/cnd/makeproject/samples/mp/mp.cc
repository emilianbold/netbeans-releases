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

#include <sys/types.h>
#include <unistd.h>
#include <iostream.h>

int main(int argc, char**argv) {
    pid_t pid;
    pid_t f_res;

    // Prints welcome message...
    cout << "Welcome ...\n";
    pid=getpid();

    // Prints arguments...
    if (argc > 1) {
        f_res = fork();
        if (0 == f_res) {
            /* Child */
            pid=getpid();
            cout << "\nPID child = " << pid << "\n";
            _exit(0);
        }
        cout << "PID parent = " << pid  << "  PID child = " << f_res << "\n";
        cout << "\nArguments:\n";
        for (int i = 1; i < argc; i++) {
            cout << i << ": " << argv[i] << "\n";
        }
    }
    return 0;
}
