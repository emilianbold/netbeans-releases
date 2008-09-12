! Copyright 2007 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.

module matrix_generator
interface 
    subroutine generateMatrix(generatorID, A, param1)
        integer(4), intent(in)  :: generatorID          ! id of generator to use
        real(8), intent(out)    :: A(:,:)               ! matrix to fill-up
        real(8), optional, intent(in) :: param1         ! parameters for matr
    end subroutine generateMatrix
end interface
end module matrix_generator

subroutine generateMatrix(generatorID, A, param1)
    interface
        subroutine generate_1(A, x)
            real(8) :: A(:,:), x
        end subroutine generate_1
        subroutine generate_2(A, x)
            real(8) :: A(:,:), x
        end subroutine generate_2
        subroutine generate_3(A, x)
            real(8) :: A(:,:), x
        end subroutine generate_3
        subroutine generate_4(A, x)
            real(8) :: A(:,:), x
        end subroutine generate_4
    end interface
    
    integer(4), intent(in) :: generatorID        ! id of generator to use
    real(8), intent(out)   :: A(:,:)             ! matrix to fill-up
    real(8), optional, intent(in) :: param1      ! parameters for matr

    select case (generatorID)
        case (1)
            call generate_1(A, param1)
        case (2)
            call generate_2(A, param1)
        case (3)
            call generate_3(A, param1)
        case (4)
            call generate_4(A, param1)
        case default
            print *, "Wrong matrix generator ID."
            stop
    end select
end subroutine generateMatrix

subroutine out_generators(id)
    if (id == 0 .or. id == 1) then
        call out_generate_1()
    end if
    if (id == 0 .or. id == 2) then
        call out_generate_2()
    end if
    if (id == 0 .or. id == 3) then
        call out_generate_3()
    end if
    if (id == 0 .or. id == 4) then
        call out_generate_4()
    end if
end subroutine

subroutine out_generate_1()
    print 100
100 format ( /&
    "-- ( 1 ) ---------------------------------  " /&
    "generates the real(8) matrix A[n x n]       " /&
    "with the following coefficients:            " /&
    "  for i <> j:   aij = j,  i = 1, 2, ... n,  j = 1, 2, ... n" /&
    "  for i = j:    a11 = 1,  aii = x + 1       " /&
    "  where x is a parameter that influence on the condition properties of A" //&
    "           1    2     3    4    . . .    n  " /&
    "           1  (x+1)   3    4    . . .    n  " /&
    "    A:     1    2   (x+1)  4    . . .    n  " /&
    "           .    .     .    .    . . .    .  " /&
    "           .    .     .    .    . . .    .  " /&
    "           1    2     3    4           (x+1)" /&
    "------------------------------------------ " //)
end subroutine out_generate_1
        
subroutine generate_1(A, param)
implicit none
    real(8), intent(out)  :: A(:,:)
    real(8), intent(in)   :: param
    integer(4) :: n, i, j

    n = ubound(A, 1)        ! Get array size

    ! filling-up the matrix
    A(1, 1) = 1.0
    forall (i = 1:n, j = 1:n, i <> j) A(i, j) = dfloat(j)
    forall (i = 2:n) A(i, i) = param + 1.0
end subroutine generate_1

subroutine out_generate_2()
    print 100
100 format ( /&
    "-- ( 2 ) --------------------------------- " /&
    " generates a real(8) matrix A[n x n]       " /&
    " with the following coefficients:          " /&
    "  aij = 1,  for i = 1, ... n,  j <= i      " /&
    "  aij = bi, otherwise,                     " /&
    "  where B=[b1, b2, ... b(n-1)]**T          " //&
    "     1    b1   b1   b1   . . .     b1      " /&
    "     1    1    b2   b2   . . .     b2      " /&
    "     1    1    1    b3   . . .     b3      " /&
    "     .    .    .    .    . . .      .      " /&
    "     .    .    .    .    . . .      .      " /&
    "     1    1    1    1    . . .  b(n - 1)   " /&
    "     1    1    1    1    . . .      1      " //&
    "  Let bi = n - i; i = 1, .. (n - 2)        " /&
    "  b (n - 1) = param = var:                 " /&
    "  Recommended values of b(n-1):            " /&
    "     1.1, 1.01, 1.001, 1.0001, 1.00001, ..." /&
    "------------------------------------------ " //)
end subroutine

subroutine generate_2(A, param)
implicit none
    real(8), intent(out)  :: A(:,:)
    real(8), intent(in)   :: param
    integer(4)            :: i, j, n

    n = ubound(A, 1)        ! Get array size

    ! filling-up the matrix
    forall (i = 1:n, j = 1:n, i >= j) A(i, j) = 1.0d00
    forall (i = 1:n, j = 1:n, i < j)  A(i, j) = dfloat(n - i)
    A(n - 1, n) = param

end subroutine generate_2

subroutine out_generate_3()
    print 100
100 format ( /&
    "-- ( 3 ) --------------------------------- " /&
    " generates a real(8) matrix A[n x n]       " /&
    " with the following coefficients:          " /&
    "  aij = 1,  for i, j = 1, ... n,  i <> j   " /&
    "  aii = x,  for i = 1, ... n               " //&
    "     x    1    1    . . .    1             " /&
    "     1    x    1    . . .    1             " /&
    "     1    1    x    . . .    1             " /&
    "     .    .    .    .    . . .             " /&
    "     .    .    .    .    . . .             " /&
    "     1    1    1    1    . . .             " /&
    "     1    1    1    1    . . x             " //&
    " Recommended values of x:                  " /&
    "   1.1, 1.01, 1.001, 1.0001, 1.00001, ...  " /&
    "------------------------------------------ " //)
end subroutine

subroutine generate_3(A, param)
implicit none
    real(8), intent(out)  :: A(:,:)
    real(8), intent(in)   :: param
    integer(4)            :: i, j, n

    n = ubound(A, 1)        ! Get array size

    ! filling-up the matrix
    A = 1.0d+00
    !forall (i = 1:n, j = 1:n, i <> j) A(i, j) = 1.0d+00
    forall (i = 1:n)  A(i, i) = param
end subroutine generate_3

subroutine out_generate_4()
    print 100
100 format ( /&
    "-- ( 4 ) ---------------------------------         " /&
    " generates a real(8) matrix A[n x n]               " /&
    " with the following coefficients:                  " //&
    "     1/(z1+y1)    1/(z1+y2)  . . .  1/(z1+yn)      " /&
    "     1/(z2+y1)    1/(z2+y2)  . . .  1/(z2+yn)      " /&
    "     1/(z3+y1)    1/(z3+y2)  . . .  1/(z3+yn)      " /&
    "        .            .       . . .      .          " /&
    "     1/(zn+y1)    1/(zn+y2)  . . .  1/(zn+yn)      " //&
    "   zi = 1/(1 + i)                                  " /&
    "   yj = cos(j)                                     " /&
    "   zn = 1/(x - 1) = var                            " /&
    "   Recommended values of x = 1.1, 1.01, 1.001, ...." /&
    "------------------------------------------         " //)
end subroutine

subroutine generate_4(A, param)
implicit none
    real(8), intent(out)  :: A(:,:)
    real(8), intent(in)   :: param
    integer(4)            :: i, j, n
    real(8), allocatable  :: Y(:), Z(:)

    n = ubound(A, 1)        ! Get array size

    allocate(Y(n), Z(n))

    do i = 1, n
        Z(i) = 1.0d+00 / dfloat(1 + i)
        Y(i) = cos(dfloat(i))
    enddo

    Z(n) = 1.0d+00 / (param - 1.0d+00)

    forall (i = 1:n, j = 1:n) A(i, j) = 1.0d+00 / (Z(i) + Y(j)) 
    
    deallocate(Y, Z)
end subroutine generate_4
