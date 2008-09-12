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
subroutine calculate(n, n_beta, beta, dcondA, dnormR, snormR, generatorID)
use matrix_generator
use util
    
implicit none
    integer (4), intent(in)   :: n, n_beta, generatorID
    real(8), intent(out)      :: beta(:), dcondA(:), dnormR(:)
    real(4), intent(out)      :: snormR(:)
    real(8), allocatable      :: A(:,:), Ainv(:,:), E(:,:), R(:,:)
    real(4), allocatable      :: AS(:,:), AinvS(:,:), ES(:,:), RS(:,:), betaS(:)
    integer (4)               :: k, m, i, j
    real(8)                   :: p
    
    ! DGESVX parameters:
    integer(4)                :: info,i,j
    integer(4), allocatable   :: IPIVOT(:), WORK2(:)
    real(8), allocatable      :: RD(:), CD(:), FERR(:), BERR(:), WORK(:), B(:,:), AF(:,:)
    real(4), allocatable      :: RDS(:), CDS(:), FERRS(:), BERRS(:), WORKS(:), BS(:,:), AFS(:,:)
    character(1)              :: EQUED
    real(8)                   :: drcond
    real(4)                   :: srcond
    
    ! A(n,n), AS(n,n)           : input/output matrix double (A) or single (AS) precision
    ! Ainv(n,n), AinvS(n,n)     : inverse matrix double (A) or single (AS) precision
    ! E(n,n), ES(n,n)           : identity matrix double (E) or single (ES) precision
    ! R(n,n), RS(n,n)           : R = A*Ainv - E should be close to the zero matrix
    !                             R - double precision, RS - sigle precision
    ! dnormR(n_beta)            : ||R|| - vector (for different beta values), double precision
    ! snormR(n_beta)            : ||RS|| - vector (for different beta values), single precision
    ! dcondA(n_beta)            : condition number - vector (for different beta values)
    ! beta(n_beta)              : parameters for matrix_generator
    ! B(n,n)                    : work array - copy of the input matrix E
    
    EQUED = 'N'
    
    ! Generating vector of parameters beta (single/double precision)

    allocate(betaS(n_beta))
    p = 0.1d00
    
    do m = 1, n_beta
        beta(m) = 1.d00 + p
        betaS(m) = float(m)
        p = 0.1d00 * p
    enddo
    
    write(6,"(' Test for the matrix generator ', i2, ' :')") generatorID

    ! Allocate double-precision arrays ...
    allocate(A(n, n), AF(n, n), Ainv(n, n), E(n, n), R(n, n), B(n, n))
    allocate(IPIVOT(n), RD(n), CD(n), FERR(n), BERR(n), WORK(4*n), WORK2(n))
    
    ! Allocate single-precision arrays ...
    allocate(AS(n, n), AFS(n, n), AinvS(n, n), ES(n, n), RS(n, n), BS(n, n))
    allocate(RDS(n), CDS(n), FERRS(n), BERRS(n), WORKS(4*n))
    
    ! Generate identity n by n matrix
    call matrix_didentity(E)
    
    B = E
    do m = 1, n_beta
        ! Generate matrix A (with parameter beta) with double precision

        call generateMatrix(generatorID, A, beta(m))
        
        ! The same matrixes, but with single precision: A->AS, E->ES, B->BS.
        do i = 1, n
            do j = 1, n
                AS(i, j)=float(A(i, j))
                ES(i, j)=float(E(i, j))
            enddo
        enddo
        
        BS = ES
        
        ! Solve the system of linear algebraic equations using the DGESVX
        ! PERFLIB routine
        ! SUBROUTINE DGESVX(FACT, TRANSA, N, NRHS, A, LDA, AF, LDAF, IPIVOT, EQUED,
        ! R, C, B, LDB, X, LDX, RCOND, FERR, BERR, WORK, WORK2, INFO)
        
        call dgesvx('N', 'N', n, n, A, n, AF, n, IPIVOT, EQUED, RD, CD, B, n, Ainv, n, drcond, &
                    FERR, BERR, WORK, WORK2, info)
                    
        if (abs(drcond) <= epsilon(1d0)) then
            dcondA(m) = huge(1d0)
        else 
            dcondA(m) = 1.0d00 / drcond
        end if
        
        call sgesvx('N', 'N', n, n, AS, n, AFS, n, IPIVOT, EQUED, RDS, CDS, BS, n, AinvS, n, srcond, &
                    FERRS, BERRS, WORKS, WORK2, info)
        
        ! estimate calculation errors...
        R = matmul(A, Ainv) - E         !  ... for double precision
        RS = matmul(AS, AinvS) - ES     !  ... for single precision
        
        ! calculate Euclidean norm
        dnormR(m) = dnorm_3(R)       !  ... for double precision
        snormR(m) = snorm_3(RS)      !  ... for single precision
    enddo
    
    ! Free used memory
    deallocate(A, AF, Ainv, E, R, B,IPIVOT, RD, CD, FERR, BERR, WORK, WORK2)
    deallocate(AS, AFS, AinvS, ES, RS, BS,RDS, CDS, FERRS, BERRS, WORKS)
    deallocate(betaS)
end subroutine calculate

subroutine outResults(n, n_beta, beta, dcondA, dnormR, snormR)
implicit none
    integer(4), intent(in)        :: n, n_beta
    real(8), intent(in)           :: beta(:), dcondA(:), dnormR(:)
    real(4), intent(in)           :: snormR(:)
    integer(4)                    :: m

    write(6, " (' matrix size n=', i3)")n
    write(6, "(3x, '-------------------------------------------------------------------------------')")
    write(6, "(3x, '| m |         beta       |     condA     | ||A*inv(A)-E||  |  ||A*inv(A)-E||  |')")
    write(6, "(3x, '|   |                    |               |double precision | single precision |')")
    write(6, "(3x, '-------------------------------------------------------------------------------')")
    
    do m = 1, n_beta
        write(6, "(3x,'|', i2, ' | ', f15.10,'    | ', e13.5, ' |  ', e13.5  '  |  ', e13.5, '   |')")  &
        m, beta(m), dcondA(m), dnormR(m), snormR(m)
    enddo
    
    write( 6, "(3x, '-------------------------------------------------------------------------------')")
    write (6, "('   ')")
end subroutine outResults

subroutine out_help()
    print *, ""
    print *, "For usage issue: "
    print *, "   lu_decomp --usage"
    print *, ""
    print *, "For help read README file :)"
    print *, ""
end subroutine

subroutine out_usage()
    write(6, "('')")
    write(6, "('Usage: lu_decomp (-h|--help|--usage) | -l [genID] | [-g genID] [-n matrix_size] [-ne number_of_experiments]')")
    write(6, "('Where:')")
    write(6, "('')")
    write(6, "('       -h | --help: Get information about this program')")
    write(6, "('')")
    write(6, "('       --usage: Display this message')")
    write(6, "('')")
    write(6, "('       -l: list all available matrix generators')")
    write(6, "('')")
    write(6, "('       -l generatorID: list matrix generator generatorID')")
    write(6, "('')")
    write(6, "('       -g generatorID: ID of matrix generator to use')")
    write(6, "('')")
    write(6, "('       -n matrix_size: Matrix A [matrix_size x matrix_size] is used ')")
    write(6, "('')")
    write(6, "('       -ne number_of_experiments: Amount of experiments ')")
    write(6, "('')")
end subroutine

!
! parseCommandLine parces command-line arguments and set out parameters to
! appropriate values.
! Returns:
!  0 - in case when all parameters are valid and program can proceed
!  1 - if program was invoked with --help or --usege or -h parameter. This means 
!      that there no need to proceed calculations
! -1 - invalid parameters passed
!
integer function parseCommandLine(n, n_beta, generatorID) 
    integer, intent(out)  :: n, n_beta, generatorID
    integer :: argc, argl, status, res, argiv, iostat
    character (len = 10) :: arg, argv
    
    argc = command_argument_count()
    
    res = 0
    
    ! Set defaults
    n = 10
    n_beta = 10
    generatorID = 1

    if (argc > 0) then 
        if (argc == 1) then
            call get_command_argument(1, arg)
            select case (arg)
                case('-h')
                    call out_help()
                    res = 1
                case('--help')
                    call out_help()
                    res = 1
                case('--usage')
                    call out_usage()
                    res = 1
            end select
        end if

        i = 1

        do while (i <= argc .and. res == 0)
            call get_command_argument(i, arg, argl, status)
            argiv = 0

            if (arg .eq. '-g' .or. arg .eq. '-n' .or. arg .eq. '-ne' .or. arg .eq. '-l') then ! params that require integer argument
                i = i + 1

                call get_command_argument(i, argv, argl, status)

                if (status <> 0 .and. arg .ne. '-l') then
                    write(6, "('Parameter ', A, ' requires an argument')") trim(arg)
                    res = -1
                else 
                    read(argv, *, iostat=iostat) argiv
                    if (iostat <> 0) then
                        if (arg .eq. '-l') then 
                            i = i - 1
                        else 
                            write(6, "('Parameter ', A, ' requires an integer argument')") trim(arg)
                            res = -1
                        end if
                    end if
                end if
            end if
            
            if (res == 0) then
                select case (arg)
                    case('-g')
                        generatorID = argiv
                    case('-n')
                        n = argiv
                    case('-ne')
                        n_beta = argiv
                    case('-l')
                        call out_generators(argiv)
                        res = 1
                    case default
                        write(6, "('Wrong parameter: ', A)") arg
                        res = 1
                end select
            end if
            i = i + 1
        end do
    end if

    if (res == -1) then 
        call out_usage()
    end if

    parseCommandLine = res
    return
end function parseCommandLine
    
program test_LU_algorithm
use sunperf

interface 
    subroutine calculate(n, n_beta, beta, dcondA, dnormR, snormR, generatorID)
        integer(4) :: n, n_beta, generatorID
        real(8)    :: beta(:), dcondA(:), dnormR(:)
        real(4)    :: snormR(:)
    end subroutine

    subroutine outResults(n, n_beta, beta, dcondA, dnormR, snormR)
        integer(4) :: n, n_beta
        real(8)    :: beta(:), dcondA(:), dnormR(:)
        real(4)    :: snormR(:)
    end subroutine
        
    integer function parseCommandLine(n, n_beta, generatorID) 
        integer, intent(out)  :: n, n_beta, generatorID
    end function

end interface

    integer (4)             :: n, n_beta, generatorID
    integer                 :: res
    real(8), allocatable    :: beta(:), dcondA(:), dnormR(:)
    real(4), allocatable    :: snormR(:)

    res = parseCommandLine(n, n_beta, generatorID)
    
    if (res == 0) then
        allocate(beta(n_beta), dcondA(n_beta), dnormR(n_beta), snormR(n_beta))

        ! Perform calculations
        call calculate(n, n_beta, beta, dcondA, dnormR, snormR, generatorID)

        ! Out results
        call outResults(n, n_beta, beta, dcondA, dnormR, snormR)

        ! Free memory
        deallocate(beta, dcondA, dnormR, snormR)
    end if
end program test_LU_algorithm
