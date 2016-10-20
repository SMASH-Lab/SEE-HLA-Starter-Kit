package siso.smackdown.utilities;

public class Vector3 {

	//*************************************************************************
	// Function: Vector3::initialize
	// Purpose: (Zero-fill vector, vec[i] = 0.0)
	//*************************************************************************
	public static final double [] initialize ( // Return: -- Zero-filled vector
			double [] vec )           // Out:    -- Zero-filled vector
	{
		vec[0] = vec[1] = vec[2] = 0.0;
		return vec;
	}


	//*************************************************************************
	// Function: Vector3::unit
	// Purpose: (Construct unit vector,
	//           vec[i] = delta_ij (delta_ij is the Kronecker delta))
	//*************************************************************************
	public static final double [] unit ( // Return: -- Unit vector
			int       index,                  // In:     -- Unit index: 0,1,2=x,y,z hat
			double [] vec )                   // Out:    -- Unit vector
	{
		vec[0]     = vec[1] = vec[2] = 0.0;
		vec[index] = 1.0;
		return vec;
	}


	//*************************************************************************
	// Function: Vector3::fill
	// Purpose: (Construct a vector from scalar,  vec[i] = scalar)
	//*************************************************************************
	public static final double [] fill ( // Return: -- Filled vector
			final double    scalar,           // In:     -- Scalar
			double [] vec     )         // Out:    -- Filled vector
	{
		vec[0] = vec[1] = vec[2] = scalar;
		return vec;
	}


	//*************************************************************************
	// Function: Vector3::zero_small
	// Purpose: (Zero-out small components of a vector,
	//           vec[i] = 0 if abs(vec[i]) < limit)
	//*************************************************************************
	public static final double [] zero_small ( // Return: -- Truncated vector
			final double    limit,                  // In:     -- Limit
			double [] vec   )                 // Inout:  -- Truncated vector
	{

		if (Math.abs (vec[0]) < limit) {
			vec[0] = 0.0;
		}

		if (Math.abs (vec[1]) < limit) {
			vec[1] = 0.0;
		}

		if (Math.abs (vec[2]) < limit) {
			vec[2] = 0.0;
		}

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::copy
	// Purpose: (Copy vector contents,
	//        copy[i] = vec[i])
	//*************************************************************************
	public static final double [] copy ( // Return: -- Copied vector
			final double [] vec,              // In:     -- Source vector
			double [] copy )            // Out:    -- Copied vector
	{
		copy[0] = vec[0];
		copy[1] = vec[1];
		copy[2] = vec[2];

		return copy;
	}


	//*************************************************************************
	// Function: Vector3::dot
	// Purpose: (Compute vector inner product, result = sum_i vec1[i] * vec2[i])
	//*************************************************************************
	public static final double dot ( // Return: -- Inner product
			final double [] vec2,         // In:     -- Vector 2
			final double [] vec1  )       // In:     -- Vector 1
	{
		return vec1[0] * vec2[0] +
				vec1[1] * vec2[1] +
				vec1[2] * vec2[2];
	}


	//*************************************************************************
	// Function: Vector3::vmagsq
	// Purpose: (Compute square of vector magnitude,
	//           result = dot(vec,vec), but protects against underflow)
	//*************************************************************************
	public static final double vmagsq ( // Return: -- Inner product
			final double [] vec)             // In:     -- Vector
	{
		double magsq = 0.0;

		magsq += vec[0] * vec[0];
		magsq += vec[1] * vec[1];
		magsq += vec[2] * vec[2];

		return magsq;
	}


	//*************************************************************************
	// Function: Vector3::vmag
	// Purpose: (Compute vector magnitude, result = sqrt(vmagsq(vec)))
	//*************************************************************************
	public static final double vmag ( // Return: -- Vector magnitude
			final double [] vec)           // In:     -- Vector
	{
		return Math.sqrt(vmagsq (vec));
	}


	//*************************************************************************
	// Function: Vector3::normalize
	// Purpose: (Make vector a unit vector in-place, vec = vec * 1/vmag(vec))
	//*************************************************************************
	public static final double [] normalize ( // Return: -- Normalized vector
			double [] vec )                        // Inout:  -- Vector
	{
		double mag = vmag (vec);

		if (mag > 0.0) {
			scale (1.0 / mag, vec);
		}
		else {
			initialize (vec);
		}

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::normalize
	// Purpose: (Construct unit vector, unit_vec = vec * 1/vmag(vec))
	//*************************************************************************
	public static final double [] normalize ( // Return: -- Unit vector
			final double [] vec,                   // In:     -- Vector
			double [] unit_vec )             // Out:    -- Unit vector
	{
		normalize (copy (vec, unit_vec));

		return unit_vec;
	}


	//*************************************************************************
	// Function: Vector3::scale
	// Purpose: (Scale a vector in-place, vec[i] = scalar)
	//*************************************************************************
	public static final double [] scale ( // Return: -- Scaled vector
			final double    scalar,            // In:     -- Scalar
			double [] vec     )          // Inout:  -- Scaled vector
	{
		vec[0] *= scalar;
		vec[1] *= scalar;
		vec[2] *= scalar;

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::scale
	// Purpose: (Scale a vector, prod[i] = vec[i] * scalar)
	//*************************************************************************
	public static final double [] scale ( // Return: -- Scaled vector
			final double [] vec,               // In:     -- Source vector
			final double    scalar,            // In:     -- Scalar
			double [] prod    )          // Out:    -- Scaled vector
	{
		prod[0] = vec[0] * scalar;
		prod[1] = vec[1] * scalar;
		prod[2] = vec[2] * scalar;

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::negate
	// Purpose: (Negate vector in-place, vec[i] = -vec[i])
	//*************************************************************************
	public static final double [] negate ( // Return: -- Negated vector
			double [] vec )                     // Inout:  -- Vector
	{
		vec[0] = -vec[0];
		vec[1] = -vec[1];
		vec[2] = -vec[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::negate
	// Purpose: (Negate vector, copy[i] = -vec[i])
	//*************************************************************************
	public static final double [] negate ( // Return: -- Negated vector
			final double [] vec,                // In:     -- Source vector
			double [] copy )              // Out:    -- Negated vector
	{
		copy[0] = -vec[0];
		copy[1] = -vec[1];
		copy[2] = -vec[2];

		return copy;
	}


	//*************************************************************************
	// Function: Vector3::transform
	// Purpose: (Transform a column vector, prod[i] = tmat[i][j]*vec[j])
	//*************************************************************************
	public static final double [] transform ( // Return: -- Transformed vector
			final double [][] tmat,                // In:     -- Transformation matrix
			final double   [] vec,                 // In:     -- Source vector
			double   [] prod )               // Out:    -- Transformed vector
	{

		prod[0] = tmat[0][0] * vec[0] +
				tmat[0][1] * vec[1] +
				tmat[0][2] * vec[2];

		prod[1] = tmat[1][0] * vec[0] +
				tmat[1][1] * vec[1] +
				tmat[1][2] * vec[2];

		prod[2] = tmat[2][0] * vec[0] +
				tmat[2][1] * vec[1] +
				tmat[2][2] * vec[2];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::transform
	// Purpose: (Transform a column vector in-place, vec[i] <- tmat[i][j]*vec[j])
	//*************************************************************************
	public static final double [] transform ( // Return: -- Transformed vector
			final double [][] tmat,                // In:     -- Transformation matrix
			double   [] vec   )              // Inout:  -- Transformed vector
	{
		double [] temp = new double[3];

		transform (tmat, vec, temp);
		copy (temp, vec);

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::transform_transpose
	// Purpose: (Transform a column vector with the transpose,
	//           prod[i] = tmat[j][i]*vec[j])
	//*************************************************************************
	public static final double [] transform_transpose ( // Return: -- Transformed vector
			final double [][] tmat,           // In:     -- Transformation matrix
			final double   [] vec,            // In:     -- Source vector
			double   [] prod  )         // Out:    -- Transformed vector
	{

		prod[0] = tmat[0][0] * vec[0] +
				tmat[1][0] * vec[1] +
				tmat[2][0] * vec[2];

		prod[1] = tmat[0][1] * vec[0] +
				tmat[1][1] * vec[1] +
				tmat[2][1] * vec[2];

		prod[2] = tmat[0][2] * vec[0] +
				tmat[1][2] * vec[1] +
				tmat[2][2] * vec[2];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::transform_transpose
	// Purpose: (Transform a column vector in-place with the transpose,
	//          vec[i] <- tmat[j][i]*vec[j])
	//*************************************************************************
	public static final double [] transform_transpose ( // Return: -- Transformed vector
			final double [][] tmat,           // In:     -- Transformation matrix
			double   [] vec   )         // Inout:  -- Transformed vector
	{
		double [] temp = new double[3];

		transform_transpose (tmat, vec, temp);
		copy (temp, vec);

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::incr
	// Purpose: (Increment a vector, vec[i] += addend[i])
	//*************************************************************************
	public static final double [] incr ( // Return: -- Incremented vector
			final double [] addend,           // In:     -- Increment
			double [] vec    )          // Inout:  -- Vector
	{
		vec[0] += addend[0];
		vec[1] += addend[1];
		vec[2] += addend[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::incr
	// Purpose: (Increment a vector, vec[i] += addend1[i] + addend2[i])
	//*************************************************************************
	public static final double [] incr ( // Return: -- Incremented vector
			final double [] addend1,          // In:     -- Increment
			final double [] addend2,          // In:     -- Increment
			double [] vec     )         // Inout:  -- Vector
	{
		vec[0] += addend1[0] + addend2[0];
		vec[1] += addend1[1] + addend2[1];
		vec[2] += addend1[2] + addend2[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::decr
	// Purpose: (Decrement a vector, vec[i] -= subtrahend[i])
	//*************************************************************************
	public static final double [] decr ( // Return: -- Decremented vector
			final double [] subtrahend,       // In:     -- Decrement
			double [] vec        )      // Inout:  -- Vector
	{
		vec[0] -= subtrahend[0];
		vec[1] -= subtrahend[1];
		vec[2] -= subtrahend[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::decr
	// Purpose: (Decrement a vector, vec[i] -= subtrahend1[i] + subtrahend2[i])
	//*************************************************************************
	public static final double [] decr ( // Return: -- Decremented vector
			final double [] subtrahend1,      // In:     -- Decrement
			final double [] subtrahend2,      // In:     -- Decrement
			double [] vec         )     // Inout:  -- Vector
	{
		vec[0] -= subtrahend1[0] + subtrahend2[0];
		vec[1] -= subtrahend1[1] + subtrahend2[1];
		vec[2] -= subtrahend1[2] + subtrahend2[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::sum
	// Purpose: (Compute the sum of two vectors,
	//           vec[i] = addend1[i] + addend2[i])
	//*************************************************************************
	public static final double [] sum ( // Return: -- Sum vector
			final double [] addend1,         // In:     -- Addend
			final double [] addend2,         // In:     -- Addend
			double [] vec      )       // Out:    -- Sum vector
	{
		vec[0] = addend1[0] + addend2[0];
		vec[1] = addend1[1] + addend2[1];
		vec[2] = addend1[2] + addend2[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::sum
	// Purpose: (Compute the sum of three vectors,
	//        vec[i] = addend1[i] + addend2[i] + addend3[i])
	//*************************************************************************
	public static final double [] sum ( // Return: -- Sum vector
			final double [] addend1,         // In:     -- Addend
			final double [] addend2,         // In:     -- Addend
			final double [] addend3,         // In:     -- Addend
			double [] vec     )        // Out:    -- Sum vector
	{
		vec[0] = addend1[0] + addend2[0] + addend3[0];
		vec[1] = addend1[1] + addend2[1] + addend3[1];
		vec[2] = addend1[2] + addend2[2] + addend3[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::diff
	// Purpose: (Compute the difference between two vectors,
	//       diff[i] = minuend[i] - subtrehend[i])
	//*************************************************************************
	public static final double [] diff ( // Return: -- Difference vector
			final double [] minuend,          // In:     -- Minuend
			final double [] subtrahend,       // In:     -- Subtrahend
			double [] vec        )      // Out:    -- Difference vector
	{
		vec[0] = minuend[0] - subtrahend[0];
		vec[1] = minuend[1] - subtrahend[1];
		vec[2] = minuend[2] - subtrahend[2];

		return vec;
	}


	//*************************************************************************
	// Function: Vector3::cross
	// Purpose: (Compute the cross product between two vectors,
	//           prod[i] = epsilon_ijk * vec_left[j] * vec_right[k])
	//*************************************************************************
	public static final double [] cross ( // Return: -- Cross product vector
			final double [] vec_left,          // In:     -- Left vector
			final double [] vec_right,         // In:     -- Right vector
			double [] prod      )        // Out:    -- Cross product vector
	{
		prod[0] = vec_left[1] * vec_right[2] - vec_left[2] * vec_right[1];
		prod[1] = vec_left[2] * vec_right[0] - vec_left[0] * vec_right[2];
		prod[2] = vec_left[0] * vec_right[1] - vec_left[1] * vec_right[0];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::scale_incr
	// Purpose: (Increment a vector with a scaled vector,
	//           prod[i] += scalar*vec[i])
	//*************************************************************************
	public static final double [] scale_incr ( // Return: -- Incremented vector
			final double [] vec,                    // In:     -- Source vector
			final double    scalar,                 // In:     -- Scalar
			double [] prod   )                // Inout:  -- Incremented vector
	{
		prod[0] += vec[0] * scalar;
		prod[1] += vec[1] * scalar;
		prod[2] += vec[2] * scalar;

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::scale_decr
	// Purpose: (Decrement a vector with a scaled vector,
	//           prod[i] += scalar*vec[i])
	//*************************************************************************
	public static final double [] scale_decr ( // Return: -- Decremented vector
			final double [] vec,                    // In:     -- Source vector
			final double    scalar,                 // In:     -- Scalar
			double [] prod   )                // Inout:  -- Decremented vector
	{
		prod[0] -= vec[0] * scalar;
		prod[1] -= vec[1] * scalar;
		prod[2] -= vec[2] * scalar;

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::cross_incr
	// Purpose: (Increment a vector with the the cross product between two vectors,
	//        prod[i] += epsilon_ijk * vec_left[j] * vec_right[k])
	//*************************************************************************
	public static final double [] cross_incr ( // Return: -- Cross product vector
			final double [] vec_left,               // In:     -- Left vector
			final double [] vec_right,              // In:     -- Right vector
			double [] prod      )             // Inout:  -- Cross product vector
	{
		prod[0] += vec_left[1] * vec_right[2] - vec_left[2] * vec_right[1];
		prod[1] += vec_left[2] * vec_right[0] - vec_left[0] * vec_right[2];
		prod[2] += vec_left[0] * vec_right[1] - vec_left[1] * vec_right[0];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::cross_decr
	// Purpose: (Decrement a vector with the the cross product between two vectors,
	//           prod[i] -= epsilon_ijk * vec_left[j] * vec_right[k])
	//*************************************************************************
	public static final double [] cross_decr ( // Return: -- Decremented vector
			final double [] vec_left,               // In:     -- Left vector
			final double [] vec_right,              // In:     -- Right vector
			double [] prod      )             // Inout:  -- Decremented vector
	{
		prod[0] -= vec_left[1] * vec_right[2] - vec_left[2] * vec_right[1];
		prod[1] -= vec_left[2] * vec_right[0] - vec_left[0] * vec_right[2];
		prod[2] -= vec_left[0] * vec_right[1] - vec_left[1] * vec_right[0];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::transform_incr
	// Purpose: (Increment a vector with a transformed column vector,
	//           prod[i] += tmat[i][j]*vec[j])
	//*************************************************************************
	public static final double [] transform_incr ( // Return: -- Incremented vector
			final double [][] tmat,              // In:     -- Transformation matrix
			final double   [] vec,               // In:     -- Source vector
			double   [] prod )             // Inout:  -- Incremented vector
	{

		prod[0] += tmat[0][0] * vec[0] +
				tmat[0][1] * vec[1] +
				tmat[0][2] * vec[2];

		prod[1] += tmat[1][0] * vec[0] +
				tmat[1][1] * vec[1] +
				tmat[1][2] * vec[2];

		prod[2] += tmat[2][0] * vec[0] +
				tmat[2][1] * vec[1] +
				tmat[2][2] * vec[2];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::transform_decr
	// Purpose: (Decrement a vector with a transformed column vector,
	//           prod[i] += tmat[i][j]*vec[j])
	//*************************************************************************
	public static final double [] transform_decr ( // Return: -- Decremented vector
			final double [][] tmat,           // In:     -- Transformation matrix
			final double   [] vec,            // In:     -- Source vector
			double   [] prod )          // Inout:  -- Decremented vector
	{

		prod[0] -= tmat[0][0] * vec[0] +
				tmat[0][1] * vec[1] +
				tmat[0][2] * vec[2];

		prod[1] -= tmat[1][0] * vec[0] +
				tmat[1][1] * vec[1] +
				tmat[1][2] * vec[2];

		prod[2] -= tmat[2][0] * vec[0] +
				tmat[2][1] * vec[1] +
				tmat[2][2] * vec[2];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::transform_transpose_incr
	// Purpose: (Increment a vector with a transpose-transformed column vector,
	//           prod[i] += tmat[j][i]*vec[j])
	//*************************************************************************
	public static final double [] transform_transpose_incr ( // Return: -- Incremented vector
			final double [][] tmat,           // In:     -- Transformation matrix
			final double   [] vec,            // In:     -- Source vector
			double   [] prod )          // Inout:  -- Incremented vector
	{

		prod[0] += tmat[0][0] * vec[0] +
				tmat[1][0] * vec[1] +
				tmat[2][0] * vec[2];

		prod[1] += tmat[0][1] * vec[0] +
				tmat[1][1] * vec[1] +
				tmat[2][1] * vec[2];

		prod[2] += tmat[0][2] * vec[0] +
				tmat[1][2] * vec[1] +
				tmat[2][2] * vec[2];

		return prod;
	}


	//*************************************************************************
	// Function: Vector3::transform_transpose_decr
	// Purpose: (decrement a vector with a transpose-transformed column vector,
	//           prod[i] -= tmat[j][i]*vec[j])
	//*************************************************************************
	public static final double [] transform_transpose_decr ( // Return: -- Decremented vector
			final double [][] tmat,           // In:     -- Transformation matrix
			final double   [] vec,            // In:     -- Source vector
			double   [] prod )          // Inout:  -- Decremented vector
	{

		prod[0] -= tmat[0][0] * vec[0] +
				tmat[1][0] * vec[1] +
				tmat[2][0] * vec[2];

		prod[1] -= tmat[0][1] * vec[0] +
				tmat[1][1] * vec[1] +
				tmat[2][1] * vec[2];

		prod[2] -= tmat[0][2] * vec[0] +
				tmat[1][2] * vec[1] +
				tmat[2][2] * vec[2];

		return prod;
	}

	//*************************************************************************
	// Function: Vector3::print
	// Purpose: (Print vector to standard error)
	//*************************************************************************
	public static final void print ( // Return: -- Void
			final double [] vec )       // In:     -- Matrix to print
	{
		String row = vec[0] + ", " + vec[1] + ", " + vec[2];
		System.out.println( row );

		return;
	}


	public static String getString(double[] vec) {
		return vec[0] + ", " + vec[1] + ", " + vec[2];
	}


}
