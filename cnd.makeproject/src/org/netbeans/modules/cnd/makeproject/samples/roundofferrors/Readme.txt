1. Description

The program demonstrates the influence of condition properties of the matrix A
(in the system of linear equations (Ax = b)) on the value of calculational
round-off errors during system solving.

To demonstrate this influence the system of the form (1) is solved
    AX = E                                                                   (1)

where:
    A - [n x n] matrix;
    E - identity [n x n] matrix. 

Obviously, X == Ainv - the inverse matrix n x n of A - is the solution of (1).

Ideally A x Ainv should be equal to E, but it is not so because of round-off 
error introduced during system solving.

This error can be estimated as (2):
    ||R|| = ||AX - E|| = f(cond(A))                                          (2)

Matrix A used in this sample is generated using one of a matrix generator 
depending on command-line parameters (invoke lu_decomp -l to see the full list 
of available generators)

Parameter beta is used in genereators and affects the cond(A) characteristics.

In this sample application parameter beta takes the following values:
    beta = 1.1; 1.01; 1.001; 1.0001; ... 1.00000000001

REM: /----
      | For single precision calculations using beta >= 1.000001 is reasonable 
      | only. 
      | Usage of a smaller parameter leads to matrix A degeneratation.

The following dependencies are demonstrated in the program:
     ||R|| = f(cond(A));   (A value of error as function from condition 
                            properties of matrix A)
     ||R|| = f(n); 

     ||R|| = f(double precision/singe precision).


The following PerfLib routines are used for solving (1)

     - the DGESVX PERFLIB routine for double precision
     - the SGESVX PERFLIB routine for single precision


2. Usage

lu_decomp takes several parameters. To get the full list of available params 
invoke the program with --usage argument.


3. Building from command line

Type 'make' to build the program with debug information, or 'make CONF=Release' to
build it without. Type 'make help' for make-related help.

