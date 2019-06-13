/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

#include "ClassA.h" // in test

void go();
void go(int a);
void go(int a, double b);

void go() {
    
}

void go(int a) {
    
}

void go(int a, double b) {
    friendFoo();    
}

int main(int argc, char** argv) {
    ClassA a;
    int in = argc;
    void* ptr = argv;
    go();
    go(1);
    go(in, 1.0);

    // Prints welcome message...
    cout << "Welcome ...\n";
    
    // Prints arguments...
    if (argc > 1) {
        cout << "\nArguments:\n";
        for (int i = 1; i < argc; i++) { 
            cout << i << ": " << argv[i] << "\n";
        }
    }
    // hello;

    return 0;
}
 
void castChecks() {
    void* a;
    ((ClassB)*a).myPtr;
    ((ClassB*)a)->myPtr;
    ((ClassB)*a).myVal;
    ((ClassB*)a)->myVal;
}

void sameValue(int sameValue) {
    if (sameValue > 0) {
        sameValue(sameValue - 1);
    }
}

typedef unsigned int uint32_t;
typedef	struct ehci_itd {
    uint32_t itd_state;
} ehci_itd_t;

typedef struct ehci_state {
    ehci_itd_t *ehci_itd_pool_addr;
} ehci_state_t;

void iz136894(ehci_state* state, int i){
    state->ehci_itd_pool_addr->itd_state;
    state->ehci_itd_pool_addr[i].itd_state;
    ehci_itd_t *pool_addr;
    pool_addr[i].itd_state;
    state->ehci_itd_pool_addr[0].itd_state;
    pool_addr[0].itd_state;
}

void iz137483(int param_postfix, int param){
    int i = param;
    int j = param_postfix;
    ehci_state* state;
    state->ehci_itd_pool_addr[sizeof(param)/sizeof(char) - 1].itd_state;
}

struct entryplus3_info {
    int attr;
    int fh;
    int res;
};
typedef struct entryplus3_info entryplus3_info;

int* iz145828(entryplus3_info *infop, int i)
{
    int* j = &infop[i].attr; //
    &infop[i].fh; //
    return &infop[i].res; // 
}