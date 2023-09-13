package com.utilxl.test;

import mpi.*;

public class MPI_HelloWorld
{

     public static void main(String[] args) 
     {
          MPI.Init(args);
          int rank = MPI.COMM_WORLD.Rank();
          System.out.println("HelloWorld. I am rank "+rank);
          MPI.Finalize();
     }

}
