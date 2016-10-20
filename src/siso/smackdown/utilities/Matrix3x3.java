package siso.smackdown.utilities;

public class Matrix3x3 {

   //*************************************************************************
   // Function: Matrix3x3::initialize
   // Purpose: (Zero-fill matrix: mat[i][j] = 0.0)
   //*************************************************************************
   public static final void initialize ( // Return: -- Void
      double [][] mat       )            // Out:    -- Zero-filled matrix
   {
      mat[0][0] = mat[1][1] = mat[2][2] = 0.0;

      mat[0][1] = mat[1][0] = 0.0;
      mat[1][2] = mat[2][1] = 0.0;
      mat[2][0] = mat[0][2] = 0.0;

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::identity
   // Purpose: (Construct identity matrix: mat[i][j] = delta_ij)
   //*************************************************************************
   public static final void identity ( // Return: -- Void
      double [][] mat )                // Out:    -- Identity matrix
   {

      mat[0][0] = mat[1][1] = mat[2][2] = 1.0;

      mat[0][1] = mat[1][0] = 0.0;
      mat[1][2] = mat[2][1] = 0.0;
      mat[2][0] = mat[0][2] = 0.0;

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::cross_matrix
   // Purpose: (Construct the skew symmetric cross product matrix:
   //    mat[i][k] = epsilon_ijk vec[j], epsilon_ijk is the Levi-Cevita symbol)
   //*************************************************************************
   public static final void cross_matrix ( // Return: -- Void
      final double [] vec,                 // In:     -- Vector
      double [][]     cross_mat )          // Out:    -- Cross product matrix
   {
      cross_mat[0][0] = cross_mat[1][1] = cross_mat[2][2] = 0.0;

      // ijk = 012, 210
      cross_mat[0][2] =  vec[1];
      cross_mat[2][0] = -vec[1];

      // ijk = 120, 021
      cross_mat[1][0] =  vec[2];
      cross_mat[0][1] = -vec[2];

      // ijk = 201, 102
      cross_mat[2][1] =  vec[0];
      cross_mat[1][2] = -vec[0];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::outer_product
   // Purpose: (Construct the outer product of two vectors:
   //        mat[i][j] = vec_left[i] * vec_right[j])
   //*************************************************************************
   public static final void outer_product ( // Return: -- Void
      final double [] vec_left,             // In:     -- Vector
      final double [] vec_right,            // In:     -- Vector
      double [][]     prod       )          // Out:    -- Outer product matrix
   {
      prod[0][0] = vec_left[0] * vec_right[0];
      prod[0][1] = vec_left[0] * vec_right[1];
      prod[0][2] = vec_left[0] * vec_right[2];
      prod[1][0] = vec_left[1] * vec_right[0];
      prod[1][1] = vec_left[1] * vec_right[1];
      prod[1][2] = vec_left[1] * vec_right[2];
      prod[2][0] = vec_left[2] * vec_right[0];
      prod[2][1] = vec_left[2] * vec_right[1];
      prod[2][2] = vec_left[2] * vec_right[2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::negate
   // Purpose: (Negated matrix in-place: mat[i][j] = -mat[i][j])
   //*************************************************************************
   public static final void negate ( // Return: -- Void
      double [][] mat )              // Inout:  -- Negated matrix
   {

      mat[0][0] = -mat[0][0];
      mat[0][1] = -mat[0][1];
      mat[0][2] = -mat[0][2];

      mat[1][0] = -mat[1][0];
      mat[1][1] = -mat[1][1];
      mat[1][2] = -mat[1][2];

      mat[2][0] = -mat[2][0];
      mat[2][1] = -mat[2][1];
      mat[2][2] = -mat[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::transpose
   // Purpose: (Transpose matrix in-place: mat[i][j] = mat[j][i])
   //*************************************************************************
   public static final void transpose (  // Return: -- Void
      double [][] mat )                  // Inout:  -- Transposed matrix
   {
      double temp;

      temp      = mat[0][1];
      mat[0][1] = mat[1][0];
      mat[1][0] = temp;

      temp      = mat[0][2];
      mat[0][2] = mat[2][0];
      mat[2][0] = temp;

      temp      = mat[1][2];
      mat[1][2] = mat[2][1];
      mat[2][1] = temp;

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::scale
   // Purpose: (Scale matrix in-place, mat[i][j] = scalar * mat[i][j])
   //*************************************************************************
   public static final void scale ( // Return: -- Void
      final double scalar,          // In:     -- Scalar
      double [][]  mat     )        // Inout:  -- Scaled matrix
   {

      mat[0][0] *= scalar;
      mat[0][1] *= scalar;
      mat[0][2] *= scalar;
      mat[1][0] *= scalar;
      mat[1][1] *= scalar;
      mat[1][2] *= scalar;
      mat[2][0] *= scalar;
      mat[2][1] *= scalar;
      mat[2][2] *= scalar;

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::incr
   // Purpose: (Increment matrix in-place: mat[i][j] = mat[i][j] + addend[i][j])
   //*************************************************************************
   public static final void incr ( // Return: -- Void
      final double [][] addend,    // In:     -- Increment
            double [][] mat     )  // Inout:  -- Incremented matrix
   {

      mat[0][0] += addend[0][0];
      mat[0][1] += addend[0][1];
      mat[0][2] += addend[0][2];
      mat[1][0] += addend[1][0];
      mat[1][1] += addend[1][1];
      mat[1][2] += addend[1][2];
      mat[2][0] += addend[2][0];
      mat[2][1] += addend[2][1];
      mat[2][2] += addend[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::decr
   // Purpose: (Decrement matrix in-place: mat[i][j] = mat[i][j] - subtrahend[i][j])
   //*************************************************************************
   public static final void decr (    // Return: -- Void
      final double [][] subtrahend,   // In:     -- Decrement
            double [][] mat         ) // Inout:  -- Decremented matrix
   {

      mat[0][0] -= subtrahend[0][0];
      mat[0][1] -= subtrahend[0][1];
      mat[0][2] -= subtrahend[0][2];
      mat[1][0] -= subtrahend[1][0];
      mat[1][1] -= subtrahend[1][1];
      mat[1][2] -= subtrahend[1][2];
      mat[2][0] -= subtrahend[2][0];
      mat[2][1] -= subtrahend[2][1];
      mat[2][2] -= subtrahend[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::copy
   // Purpose: (Copy matrix: copy[i][j] = mat[i][j])
   //*************************************************************************
   public static final void copy (   // Return: -- Void
      final double [][] input_mat,   // In:     -- Source matrix
            double [][] copy       ) // Out:    -- Matrix copy
   {

      copy[0][0] = input_mat[0][0];
      copy[0][1] = input_mat[0][1];
      copy[0][2] = input_mat[0][2];

      copy[1][0] = input_mat[1][0];
      copy[1][1] = input_mat[1][1];
      copy[1][2] = input_mat[1][2];

      copy[2][0] = input_mat[2][0];
      copy[2][1] = input_mat[2][1];
      copy[2][2] = input_mat[2][2];
      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::negate
   // Purpose: (Negate matrix: copy[i][j] = -mat[i][j])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void negate (  // Return: -- Void
      final double [][] input_mat,    // In:     -- Source matrix
            double [][] copy       )  // Out:    -- Negated matrix
   {

      copy[0][0] = -input_mat[0][0];
      copy[0][1] = -input_mat[0][1];
      copy[0][2] = -input_mat[0][2];

      copy[1][0] = -input_mat[1][0];
      copy[1][1] = -input_mat[1][1];
      copy[1][2] = -input_mat[1][2];

      copy[2][0] = -input_mat[2][0];
      copy[2][1] = -input_mat[2][1];
      copy[2][2] = -input_mat[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::transpose
   // Purpose: (Transpose matrix: copy[i][j] = mat[j][i])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void transpose ( // Return: -- Void
      final double [][] input_mat,      // In:     -- Source matrix
            double [][] trans      )    // Out:    -- Matrix transpose
   {

      trans[0][0] = input_mat[0][0];
      trans[0][1] = input_mat[1][0];
      trans[0][2] = input_mat[2][0];

      trans[1][0] = input_mat[0][1];
      trans[1][1] = input_mat[1][1];
      trans[1][2] = input_mat[2][1];

      trans[2][0] = input_mat[0][2];
      trans[2][1] = input_mat[1][2];
      trans[2][2] = input_mat[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::scale
   // Purpose: (Scale matrix: copy[i][j] = scalar * mat[i][j])
   //*************************************************************************
   public static final void scale ( // Return: -- Void
      final double [][] mat,        // In:     -- Matrix
      final double      scalar,     // In:     -- Scalar
            double [][] prod    )   // Out:    -- Product
   {

      prod[0][0] = mat[0][0] * scalar;
      prod[0][1] = mat[0][1] * scalar;
      prod[0][2] = mat[0][2] * scalar;
      prod[1][0] = mat[1][0] * scalar;
      prod[1][1] = mat[1][1] * scalar;
      prod[1][2] = mat[1][2] * scalar;
      prod[2][0] = mat[2][0] * scalar;
      prod[2][1] = mat[2][1] * scalar;
      prod[2][2] = mat[2][2] * scalar;

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::add
   // Purpose: (Add matrices: sum[i][j] = augend[i][j] + addend[i][j])
   //*************************************************************************
   public static final void add ( // Return: -- Void
      final double [][] augend,   // In:     -- Matrix
      final double [][] addend,   // In:     -- Matrix
            double [][] sum    )  // Out:    -- Sum
   {

      sum[0][0] = augend[0][0] + addend[0][0];
      sum[0][1] = augend[0][1] + addend[0][1];
      sum[0][2] = augend[0][2] + addend[0][2];
      sum[1][0] = augend[1][0] + addend[1][0];
      sum[1][1] = augend[1][1] + addend[1][1];
      sum[1][2] = augend[1][2] + addend[1][2];
      sum[2][0] = augend[2][0] + addend[2][0];
      sum[2][1] = augend[2][1] + addend[2][1];
      sum[2][2] = augend[2][2] + addend[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::subtract
   // Purpose: (Subtract matrices: diff[i][j] = minuend[i][j] - subtrahend[i][j])
   //*************************************************************************
   public static final void subtract ( // Return: -- Void
      final double [][] minuend,       // In:     -- Matrix
      final double [][] subtrahend,    // In:     -- Matrix
            double [][] diff        )  // Out:    -- Difference
   {

      diff[0][0] = minuend[0][0] - subtrahend[0][0];
      diff[0][1] = minuend[0][1] - subtrahend[0][1];
      diff[0][2] = minuend[0][2] - subtrahend[0][2];
      diff[1][0] = minuend[1][0] - subtrahend[1][0];
      diff[1][1] = minuend[1][1] - subtrahend[1][1];
      diff[1][2] = minuend[1][2] - subtrahend[1][2];
      diff[2][0] = minuend[2][0] - subtrahend[2][0];
      diff[2][1] = minuend[2][1] - subtrahend[2][1];
      diff[2][2] = minuend[2][2] - subtrahend[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::product
   // Purpose: (Compute the matrix product mat_left * mat_right:
   //           prod[i][j] = mat_left[i][k] * mat_right[k][j])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void product ( // Return: -- Void
      final double [][] mat_left,     // In:     -- Multiplier
      final double [][] mat_right,    // In:     -- Multiplicand
            double [][] prod        ) // Out:    -- Product
   {

      prod[0][0] = mat_left[0][0] * mat_right[0][0] +
                   mat_left[0][1] * mat_right[1][0] +
                   mat_left[0][2] * mat_right[2][0];

      prod[0][1] = mat_left[0][0] * mat_right[0][1] +
                   mat_left[0][1] * mat_right[1][1] +
                   mat_left[0][2] * mat_right[2][1];

      prod[0][2] = mat_left[0][0] * mat_right[0][2] +
                   mat_left[0][1] * mat_right[1][2] +
                   mat_left[0][2] * mat_right[2][2];

      prod[1][0] = mat_left[1][0] * mat_right[0][0] +
                   mat_left[1][1] * mat_right[1][0] +
                   mat_left[1][2] * mat_right[2][0];

      prod[1][1] = mat_left[1][0] * mat_right[0][1] +
                   mat_left[1][1] * mat_right[1][1] +
                   mat_left[1][2] * mat_right[2][1];

      prod[1][2] = mat_left[1][0] * mat_right[0][2] +
                   mat_left[1][1] * mat_right[1][2] +
                   mat_left[1][2] * mat_right[2][2];

      prod[2][0] = mat_left[2][0] * mat_right[0][0] +
                   mat_left[2][1] * mat_right[1][0] +
                   mat_left[2][2] * mat_right[2][0];

      prod[2][1] = mat_left[2][0] * mat_right[0][1] +
                   mat_left[2][1] * mat_right[1][1] +
                   mat_left[2][2] * mat_right[2][1];

      prod[2][2] = mat_left[2][0] * mat_right[0][2] +
                   mat_left[2][1] * mat_right[1][2] +
                   mat_left[2][2] * mat_right[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::product_left_transpose
   // Purpose: (Compute the matrix product mat_left^T * mat_right:
   //        prod[i][j] = mat_left[k][i] * mat_right[k][j])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void product_left_transpose ( // Return: -- Void
      final double [][] mat_left,       // In:     -- Multiplier
      final double [][] mat_right,      // In:     -- Multiplicand
            double [][] prod      )     // Out:    -- Product
   {

      prod[0][0] = mat_left[0][0] * mat_right[0][0] +
                   mat_left[1][0] * mat_right[1][0] +
                   mat_left[2][0] * mat_right[2][0];

      prod[0][1] = mat_left[0][0] * mat_right[0][1] +
                   mat_left[1][0] * mat_right[1][1] +
                   mat_left[2][0] * mat_right[2][1];

      prod[0][2] = mat_left[0][0] * mat_right[0][2] +
                   mat_left[1][0] * mat_right[1][2] +
                   mat_left[2][0] * mat_right[2][2];

      prod[1][0] = mat_left[0][1] * mat_right[0][0] +
                   mat_left[1][1] * mat_right[1][0] +
                   mat_left[2][1] * mat_right[2][0];

      prod[1][1] = mat_left[0][1] * mat_right[0][1] +
                   mat_left[1][1] * mat_right[1][1] +
                   mat_left[2][1] * mat_right[2][1];

      prod[1][2] = mat_left[0][1] * mat_right[0][2] +
                   mat_left[1][1] * mat_right[1][2] +
                   mat_left[2][1] * mat_right[2][2];

      prod[2][0] = mat_left[0][2] * mat_right[0][0] +
                   mat_left[1][2] * mat_right[1][0] +
                   mat_left[2][2] * mat_right[2][0];

      prod[2][1] = mat_left[0][2] * mat_right[0][1] +
                   mat_left[1][2] * mat_right[1][1] +
                   mat_left[2][2] * mat_right[2][1];

      prod[2][2] = mat_left[0][2] * mat_right[0][2] +
                   mat_left[1][2] * mat_right[1][2] +
                   mat_left[2][2] * mat_right[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::product_right_transpose
   // Purpose: (Compute the matrix product mat_left * mat_right^T:
   //           prod[i][j] = sum_k mat_left[i][k] * mat_right[j][k])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void product_right_transpose (  // Return: -- Void
      final double [][] mat_left,       // In:     -- Multiplier
      final double [][] mat_right,      // In:     -- Multiplicand
            double [][] prod      )     // Out:    -- Product
   {

      prod[0][0] = mat_left[0][0] * mat_right[0][0] +
                   mat_left[0][1] * mat_right[0][1] +
                   mat_left[0][2] * mat_right[0][2];

      prod[0][1] = mat_left[0][0] * mat_right[1][0] +
                   mat_left[0][1] * mat_right[1][1] +
                   mat_left[0][2] * mat_right[1][2];

      prod[0][2] = mat_left[0][0] * mat_right[2][0] +
                   mat_left[0][1] * mat_right[2][1] +
                   mat_left[0][2] * mat_right[2][2];

      prod[1][0] = mat_left[1][0] * mat_right[0][0] +
                   mat_left[1][1] * mat_right[0][1] +
                   mat_left[1][2] * mat_right[0][2];

      prod[1][1] = mat_left[1][0] * mat_right[1][0] +
                   mat_left[1][1] * mat_right[1][1] +
                   mat_left[1][2] * mat_right[1][2];

      prod[1][2] = mat_left[1][0] * mat_right[2][0] +
                   mat_left[1][1] * mat_right[2][1] +
                   mat_left[1][2] * mat_right[2][2];

      prod[2][0] = mat_left[2][0] * mat_right[0][0] +
                   mat_left[2][1] * mat_right[0][1] +
                   mat_left[2][2] * mat_right[0][2];

      prod[2][1] = mat_left[2][0] * mat_right[1][0] +
                   mat_left[2][1] * mat_right[1][1] +
                   mat_left[2][2] * mat_right[1][2];

      prod[2][2] = mat_left[2][0] * mat_right[2][0] +
                   mat_left[2][1] * mat_right[2][1] +
                   mat_left[2][2] * mat_right[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::product_transpose_transpose
   // Purpose: (Compute the matrix product mat_left^T * mat_right^T:
   //           prod[i][j] = sum_k mat_left[k][i] * mat_right[j][k])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void product_transpose_transpose ( // Return: -- Void
      final double [][] mat_left,       // In:     -- Multiplier
      final double [][] mat_right,      // In:     -- Multiplicand
            double [][] prod       )    // Out:    -- Product
   {

      prod[0][0] = mat_left[0][0] * mat_right[0][0] +
                   mat_left[1][0] * mat_right[0][1] +
                   mat_left[2][0] * mat_right[0][2];

      prod[0][1] = mat_left[0][0] * mat_right[1][0] +
                   mat_left[1][0] * mat_right[1][1] +
                   mat_left[2][0] * mat_right[1][2];

      prod[0][2] = mat_left[0][0] * mat_right[2][0] +
                   mat_left[1][0] * mat_right[2][1] +
                   mat_left[2][0] * mat_right[2][2];

      prod[1][0] = mat_left[0][1] * mat_right[0][0] +
                   mat_left[1][1] * mat_right[0][1] +
                   mat_left[2][1] * mat_right[0][2];

      prod[1][1] = mat_left[0][1] * mat_right[1][0] +
                   mat_left[1][1] * mat_right[1][1] +
                   mat_left[2][1] * mat_right[1][2];

      prod[1][2] = mat_left[0][1] * mat_right[2][0] +
                   mat_left[1][1] * mat_right[2][1] +
                   mat_left[2][1] * mat_right[2][2];

      prod[2][0] = mat_left[0][2] * mat_right[0][0] +
                   mat_left[1][2] * mat_right[0][1] +
                   mat_left[2][2] * mat_right[0][2];

      prod[2][1] = mat_left[0][2] * mat_right[1][0] +
                   mat_left[1][2] * mat_right[1][1] +
                   mat_left[2][2] * mat_right[1][2];

      prod[2][2] = mat_left[0][2] * mat_right[2][0] +
                   mat_left[1][2] * mat_right[2][1] +
                   mat_left[2][2] * mat_right[2][2];

      return;
   }


   //*************************************************************************
   // Function: Matrix3x3::transform_matrix
   // Purpose: (Compute the matrix product trans * mat * trans^T
   //           prod[i][j] = trans[i][k] * mat[k][l] * trans[j][l])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void transform_matrix ( // Return: -- Void
      final double [][] trans,          // In:     -- Transformation matrix
      final double [][] mat,            // In:     -- Matrix to transform
            double [][] prod)           // Out:    -- Product
   {
      double [][] temp = new double[3][3];

      product (trans, mat, temp);
      product_right_transpose (temp, trans, prod);
   }


   //*************************************************************************
   // Function: Matrix3x3::transpose_transform_matrix
   // Purpose: (Compute the matrix product trans^T * mat * trans
   //           prod[i][j] = trans[k][i] * mat[k][l] * trans[l][j])
   // Assumptions: ((Input and output matrices are distinct.))
   //*************************************************************************
   public static final void transpose_transform_matrix ( // Return: -- Void
      final double [][] trans,       // In:     -- Transformation matrix
      final double [][] mat,         // In:     -- Matrix to transform
            double [][] prod  )      // Out:    -- Product
   {
      double [][] temp = new double[3][3];

      product_left_transpose (trans, mat, temp);
      product (temp, trans, prod);
   }


   //*************************************************************************
   // Function: Matrix3x3::print
   // Purpose: (Print matrix to standard error)
   //*************************************************************************
   public static final void print ( // Return: -- Void
      final double [][] mat )       // In:     -- Matrix to print
   {
      String row0 = mat[0][0] + ", " + mat[0][1] + ", " + mat[0][2];
      String row1 = mat[1][0] + ", " + mat[1][1] + ", " + mat[1][2];
      String row2 = mat[2][0] + ", " + mat[2][1] + ", " + mat[2][2];
      System.out.println( row0 );
      System.out.println( row1 );
      System.out.println( row2 );

      return;
   }

}
