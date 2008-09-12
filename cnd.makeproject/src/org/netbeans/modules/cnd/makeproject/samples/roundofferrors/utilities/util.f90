!/*
! * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
! *
! * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
! *
! * The contents of this file are subject to the terms of either the GNU
! * General Public License Version 2 only ("GPL") or the Common
! * Development and Distribution License("CDDL") (collectively, the
! * "License"). You may not use this file except in compliance with the
! * License. You can obtain a copy of the License at
! * http://www.netbeans.org/cddl-gplv2.html
! * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
! * specific language governing permissions and limitations under the
! * License.  When distributing the software, include this License Header
! * Notice in each file and include the License file at
! * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
! * particular file as subject to the "Classpath" exception as provided
! * by Sun in the GPL Version 2 section of the License file that
! * accompanied this code. If applicable, add the following below the
! * License Header, with the fields enclosed by brackets [] replaced by
! * your own identifying information:
! * "Portions Copyrighted [year] [name of copyright owner]"
! *
! * Contributor(s):
! *
! * The Original Software is NetBeans. The Initial Developer of the Original
! * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
! * Microsystems, Inc. All Rights Reserved.
! *
! * If you wish your version of this file to be governed by only the CDDL
! * or only the GPL Version 2, indicate your decision by adding
! * "[Contributor] elects to include this software in this distribution
! * under the [CDDL or GPL Version 2] license." If you do not indicate a
! * single choice of license, a recipient has the option to distribute
! * your version of this file under either the CDDL, the GPL Version 2 or
! * to extend the choice of license to its licensees as provided above.
! * However, if you add GPL Version 2 code and therefore, elected the GPL
! * Version 2 license, then the option applies only if the new code is
! * made subject to such option by the copyright holder.
! */
module util
    interface 
        subroutine matrix_didentity(E)
            real(8), intent(out) :: E(:,:)
        end subroutine matrix_didentity

        real(8) function dnorm_3(A)
            real(8), intent(in) :: A(:,:)
        end function dnorm_3

        real(4) function snorm_3(A)
            real(4), intent(in) :: A(:,:)
        end function snorm_3
    end interface
end module util

! This routine forms the identity matrix, size n x n
!        1 0 0 0 0 0
!        0 1 0 . . 0
!        0 0 1 0  .0
!        ..........
!        0 0 0 0 ..1
! E - identity double precision matrix
! n - matrix size

subroutine matrix_didentity(E)
    real(8), intent(out) :: E(:,:)

    E = 0.0d+00
    forall (i = 1:ubound(E, 1)) E(i, i) = 1.0d+00
end subroutine matrix_didentity
    
! This function calculates the Euclidean norm of  
! the matrix A(n x n) with double precision   
real(8) function dnorm_3(A)
    implicit none
    real(8), intent(in)   :: A(:,:)
    integer(4)            :: n, i, j
    real(8)               :: s

    s = 0.d+00
    n = ubound(A, 1)

    do i = 1, n
        do j = 1, n
            s = s + A(i, j) * A(i, j)
        enddo
    enddo

    dnorm_3 = sqrt(s)
    return
end function dnorm_3
    
! This function calculates the Euclidean norm of  
! the matrix A(n x n) with single precision  
real(4) function snorm_3(A)
    implicit none
    real(4), intent(in)   :: A(:,:)
    integer(4)            :: n, i, j
    real(4)               :: s

    s = 0.e+00
    n = ubound(A, 1)  

    do i = 1, n
        do j = 1, n
            s = s + A(i, j) * A(i, j)
        enddo
    enddo

    snorm_3 = sqrt(s)
    return
end function snorm_3
