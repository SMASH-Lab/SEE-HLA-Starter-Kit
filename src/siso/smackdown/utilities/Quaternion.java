package siso.smackdown.utilities;


public class Quaternion {

	// The scalar, or real, part of the quaternion.
	public double scalar; 

	// The vector, or imaginary, part of the quaternion.
	public double [] vector = new double[3];

	// Construct pure real unit quaternion.
	public Quaternion ()
	{
		scalar = 1.0;
		Vector3.initialize (vector);
	}

	// Simple methods to make a pure zero quaternion.
	public void set_to_zero()
	{
		scalar = 0.0;
		Vector3.initialize (vector);
	}

	// Simple method to make an identity quaternion.
	public void make_identity()
	{
		scalar = 1.0;
		Vector3.initialize (vector);
	}

	// Purpose: (Copy a quaternion from an existing quaternion.)
	public void copy( Quaternion quat ) // In: -- Quaternion to copy.
	{
		scalar = quat.scalar;
		vector[0] = quat.vector[0];
		vector[1] = quat.vector[1];
		vector[2] = quat.vector[2];
	}

	// Purpose: (Copy a quaternion to a four vector, with the scalar part copied
	//           to arr[0] and the vector part to arr[1] to arr[3].)
	final void copy_to ( double [] arr ) // Out: -- Copy of quaternion
	{
		arr[0] = scalar;
		arr[1] = vector[0];
		arr[2] = vector[1];
		arr[3] = vector[2];
	}


	// Purpose: (Copy a quaternion from a four vector, with the scalar part of
	//        the quaternion in arr[0] and the vector part in arr[1] to arr[3].)
	void copy_from ( final double [] arr ) // In: -- Quaternion source
	{
		scalar    = arr[0];
		vector[0] = arr[1];
		vector[1] = arr[2];
		vector[2] = arr[3];
	}


	// Purpose: (Construct the quaternion corresponding to an eigen rotation.)
	void left_quat_from_eigen_rotation (
			double    eigen_angle,       // In: r  Eigen angle
			final double [] eigen_axis  )      // In: -- Eigen axis
	{
		double htheta;
		double cosht, sinht;

		htheta = 0.5*eigen_angle;
		cosht  = Math.cos (htheta);
		sinht  = Math.sin (htheta);

		scalar = cosht;
		Vector3.scale (eigen_axis, -sinht, vector);
	}


	// Purpose: (Scale the quaternion by a real.)
	void scale (
			final double fact ) // In:     -- Scale factor
	{
		scalar *= fact;
		Vector3.scale (fact, vector);
	}


	// Purpose: (Scale the quaternion by a real, leaving original intact.)
	final void scale (
			final double fact,   // In:     -- Scale factor
			Quaternion   quat )  // Out:    -- Scaled quaternion
	{
		quat.scalar = scalar * fact;
		Vector3.scale (vector, fact, quat.vector);
	}


	// Purpose: (Compute the square of the norm of the quaternion.)
	final double norm_sq () // Return: -- Square of the norm of the quaternion.
	{
		return scalar * scalar + Vector3.vmagsq (vector);
	}

	// Purpose: ( Normalize the quaternion,
	//            making the scalar part of the quaternion non-negative.)
	void normalize ()           // Return: -- Void
	{
		double qmagsq = norm_sq ();
		double diff1 = 1.0 - qmagsq;
		double fact;

		// Compute the normalization fact, nominally 1/sqrt(qmagsq).
		// Computational shortcut: Approximate as 2/(1+qmagsq)
		// To second order, the error in the approximation is diff1^2/8.
		// The approximation is valid if this error is smaller than numerical
		// precision. A double IEEE floating point number has a 53 bit mantissa plus
		// an implied 1 to the left of the binary point. The validity limit is thus
		// sqrt(8*2^(-54)) = 2.107342e-08, to the accuracy of the appoximation.
		if ((diff1 > -2.107342e-08) && (diff1 < 2.107342e-08)) {
			fact = 2.0 / (1.0 + qmagsq);
		} else {
			fact = 1.0 / Math.sqrt (qmagsq);
		}

		// Negate the scale factor if needed to make the scalar part non-negative.
		if (scalar < 0.0) {
			fact = -fact;
		}

		// Scale the quaternion by the above normalization factor.
		scale (fact);
	}


	// Purpose: (Form the normalized quaternion, leaving original intact.)
	final void normalize (
			Quaternion quat )  // Out:    -- Normalized quaternion
	{
		quat = this;
		quat.normalize ();
	}


	// Purpose: ( Normalize the quaternion,
	//            but do not make the scalar part non-negative.)
	void normalize_integ ()     // Return: -- Void
	{
		double qmagsq = norm_sq ();
		double diff1 = 1.0 - qmagsq;
		double fact;

		// Compute the normalization fact, see discussion in normalize().
		if ((diff1 > -2.107342e-08) && (diff1 < 2.107342e-08)) {
			fact = 2.0 / (1.0 + qmagsq);
		} else {
			fact = 1.0 / Math.sqrt (qmagsq);
		}

		// Scale the quaternion by the above normalization factor.
		scale (fact);
	}


	// Purpose: (Form the normalized quaternion, leaving original intact.)
	final void normalize_integ (
			Quaternion quat )   // Out:    -- Normalized quaternion
	{
		quat = this;
		quat.normalize_integ ();
	}


	// Purpose: (Replace the quaternion with its conjugate.)
	void conjugate()
	{
		Vector3.negate (vector);
	}


	// Purpose: (Form the conjugate of a quaternion, leaving original intact.)
	final void conjugate (
			Quaternion quat)  // Out:    -- Conjugated quaternion
	{
		quat.scalar = scalar;
		Vector3.negate (vector, quat.vector);
	}


	// Purpose: (Post-multiply this quaternion by another quaternion:
	//           prod = this * quat.)
	final void multiply (
			final Quaternion quat, // In:     -- Right multiplicand
			Quaternion prod) // Out:    -- Quaternion product
	{
		prod.scalar = scalar * quat.scalar - Vector3.dot (vector, quat.vector);
		Vector3.scale      (quat.vector, scalar, prod.vector);
		Vector3.scale_incr (vector, quat.scalar, prod.vector);
		Vector3.cross_incr (vector, quat.vector, prod.vector);
	}


	// Purpose: (Post-multiply this quaternion by another quaternion:
	//           this = this * quat.)
	void multiply (
			final Quaternion quat)       // In:     -- Right multiplicand
	{
		double    v_dot_qv;
		double [] v_cross_qv = new double[3];

		v_dot_qv = Vector3.dot (vector, quat.vector);
		Vector3.cross (vector, quat.vector, v_cross_qv);

		Vector3.scale      (quat.scalar,         vector);
		Vector3.scale_incr (quat.vector, scalar, vector);
		Vector3.incr       (v_cross_qv,          vector);

		scalar = scalar * quat.scalar - v_dot_qv;
	}


	// Purpose: (Post-multiply this quaternion's conjugate by another quaternion:
	//           prod = conj(this) * quat.)
	final void conjugate_multiply (
			final Quaternion quat,       // In:     -- Right multiplicand
			Quaternion prod )      // Out:    -- Quaternion product
	{
		prod.scalar = scalar * quat.scalar + Vector3.dot (vector, quat.vector);
		Vector3.scale      (quat.vector, scalar, prod.vector);
		Vector3.scale_decr (vector, quat.scalar, prod.vector);
		Vector3.cross_decr (vector, quat.vector, prod.vector);
	}


	// Purpose: (Post-multiply this quaternion by another's conjugate:
	//           prod = this * conj(quat).)
	final void multiply_conjugate (
			final Quaternion quat,       // In:     -- Right multiplicand
			Quaternion prod )      // Out:    -- Quaternion product
	{
		prod.scalar = scalar * quat.scalar + Vector3.dot (vector, quat.vector);
		Vector3.scale      (vector, quat.scalar, prod.vector);
		Vector3.scale_decr (quat.vector, scalar, prod.vector);
		Vector3.cross_decr (vector, quat.vector, prod.vector);
	}


	// Purpose: (Post-multiply this quaternion by another's conjugate:
	//           this = this * conj(quat).)
	void multiply_conjugate (
			final Quaternion quat )       // In:     -- Right multiplicand
	{
		double    v_dot_qv;
		double [] v_cross_qv = new double[3];

		v_dot_qv = Vector3.dot (vector, quat.vector);
		Vector3.cross (vector, quat.vector, v_cross_qv);

		Vector3.scale      (quat.scalar,         vector);
		Vector3.scale_decr (quat.vector, scalar, vector);
		Vector3.decr       (v_cross_qv,          vector);

		scalar = scalar * quat.scalar + v_dot_qv;
	}


	// Purpose: (Post-multiply this quaternion's conjugate by another quaternion:
	//           this = conj(this) * quat.)
	void conjugate_multiply (
			final Quaternion quat)       // In:     -- Right multiplicand
	{
		double    v_dot_qv;
		double [] v_cross_qv = new double[3];

		v_dot_qv = Vector3.dot (vector, quat.vector);
		Vector3.cross (vector, quat.vector, v_cross_qv);

		Vector3.scale      (-quat.scalar,        vector);
		Vector3.scale_incr (quat.vector, scalar, vector);
		Vector3.decr       (v_cross_qv,          vector);

		scalar = scalar * quat.scalar + v_dot_qv;
	}


	// Purpose: (Pre-multiply this quaternion by another quaternion:
	//           this = quat * this.)
	void multiply_left (
			final Quaternion quat)       // In:     -- Right multiplicand
	{
		double    qv_dot_v;
		double [] qv_cross_v = new double[3];

		qv_dot_v = Vector3.dot (quat.vector, vector);
		Vector3.cross (quat.vector, vector, qv_cross_v);

		Vector3.scale      (quat.scalar,         vector);
		Vector3.scale_incr (quat.vector, scalar, vector);
		Vector3.incr       (qv_cross_v,          vector);

		scalar = scalar * quat.scalar - qv_dot_v;
	}


	// Purpose: (Pre-multiply this quaternion by another's conjugate:
	//           this = conj(quat) * this.)
	void multiply_left_conjugate (
			final Quaternion quat)           // In:     -- Right multiplicand
	{
		double    qv_dot_v;
		double [] qv_cross_v = new double[3];

		qv_dot_v = Vector3.dot (quat.vector, vector);
		Vector3.cross (quat.vector, vector, qv_cross_v);

		Vector3.scale      (quat.scalar,         vector);
		Vector3.scale_decr (quat.vector, scalar, vector);
		Vector3.decr       (qv_cross_v,          vector);

		scalar = scalar * quat.scalar + qv_dot_v;
	}


	// Purpose: (Pre-multiply this quaternion by a pure imaginary
	//           quaternion, the latter represented by a vector:
	//           prod = [0, vec] * quat.)
	final void multiply_vector_left (
			final double [] vec,            // In:     -- Right multiplicand
			Quaternion      prod )          // Out:    -- Quaternion product
	{
		prod.scalar = - Vector3.dot (vector, vec);
		Vector3.scale      (vec, scalar, prod.vector);
		Vector3.cross_incr (vec, vector, prod.vector);
	}


	// Purpose: (Post-multiply this quaternion by a pure imaginary
	//           quaternion, the latter represented by a vector:
	//           prod = quat * [0, vec].)
	final void multiply_vector_right (
			final double [] vec,             // In:     -- Right multiplicand
			Quaternion      prod)            // Out:    -- Quaternion product
	{
		prod.scalar = - Vector3.dot (vector, vec);
		Vector3.scale      (vec, scalar, prod.vector);
		Vector3.cross_incr (vector, vec, prod.vector);
	}


	// Purpose: (Compute eigen decomposition of this*conj(quat).)
	final void eigen_compare (
			final Quaternion quat,         // In:     -- Quaternion to compare to
			double     eigen_angle,  // Out:    r  Eigen angle
			double  [] eigen_axis  ) // Out:    -- Eigen axis
	{
		Quaternion prod = new Quaternion();
		multiply_conjugate (quat, prod);
		prod.left_quat_to_eigen_rotation (eigen_angle, eigen_axis);
	}


	// Purpose: (Compute the time derivative of a left quaternion.)
	final void compute_left_quat_deriv (
			final double [] ang_vel,          // In:     r/s Angular velocity
			Quaternion      qdot     )        // Out:    --  Quaternion derivative
	{
		double [] wxmh = new double[3];       // r/s -0.5 * ang_vel
		Vector3.scale (ang_vel, -0.5, wxmh);
		multiply_vector_left (wxmh, qdot);
	}


	// Purpose: (Compute the time derivative of a left quaternion.)
	void compute_left_quat_deriv (
			final double [] quat,       // In:     --  Quaternion as 4-vector
			final double [] ang_vel,    // In:     r/s Angular velocity
			double [] qdot)       // Out:    --  Derivative as 4-vector
	{
		double [] wxmh   = new double[3];                 // r/s -0.5 * ang_vel
		double [] q_v    = new double[]{ quat[1], quat[2], quat[3] };
		double [] qdot_v = new double[3];
		Vector3.scale (ang_vel, -0.5, wxmh);
		qdot[0] = - Vector3.dot (q_v, wxmh);
		Vector3.scale      (wxmh, quat[0],  qdot_v);
		Vector3.cross_incr (wxmh, q_v,      qdot_v);
		qdot[1] = qdot_v[0];
		qdot[2] = qdot_v[1];
		qdot[3] = qdot_v[2];
	}


	// Set the left quaternion from a transformation matrix.
	public void left_quat_from_transform( final double [][] T )
	{
		double tr;       /* Trace of input transformation matrix */
		double tmax;     /* Max of tr, diagonal elements */
		double qix2;     /* sqrt(1+max(tr,t_i)) */
		double di;       /* a_kj - a_jk */
		double fact;     /* 0.5/qix2 */
		int    meth;     /* Index of tmax in t (-1 if trace dominates) */
		int ii, jj, kk;


		/* Compute the trace of the matrix. */
		tr = T[0][0] + T[1][1] + T[2][2];

		/* Find the largest of the trace (meth = -1) and the three diagonal
		 * elements of 'a' (meth = 0, 1, or 2). */
		meth = -1;
		tmax = tr;
		for (ii = 0; ii < 3; ii++) {
			if (T[ii][ii] > tmax) {
				meth = ii;
				tmax = T[ii][ii];
			}
		}

		/* Use method -1 when no diagonal element dominates the trace. */
		if (meth == -1) {

			qix2   = Math.sqrt( 1.0 + tr );
			fact   = 0.5 / qix2;
			scalar = 0.5 * qix2;
			vector[0] = fact * (T[2][1] - T[1][2]);
			vector[1] = fact * (T[0][2] - T[2][0]);
			vector[2] = fact * (T[1][0] - T[0][1]);

			/* Use method 0,1, or 2 based on the dominant diagonal element. */
		} else {

			ii = meth;
			jj = (ii+1)%3;
			kk = (jj+1)%3;

			di = T[kk][jj] - T[jj][kk];
			qix2 = Math.sqrt (1.0 + T[ii][ii] - (T[jj][jj] + T[kk][kk]));
			if (di < 0.0) {
				qix2 = -qix2;
			}
			fact = 0.5 / qix2;
			vector[ii] = 0.5 * qix2;
			vector[jj] = fact * (T[ii][jj] + T[jj][ii]);
			vector[kk] = fact * (T[ii][kk] + T[kk][ii]);
			scalar = fact * di;
		}

		return;
	}

	// Compute a transformation matrix from the quaternion.
	public final void left_quat_to_transform( double [][] T )
	{
		double cost;                     // Cosine of rotation angle
		double [] qvx2  = new double[3]; // qvx2_i = 2 qv_i
		double [] qsqv2 = new double[3]; // qsqv2_i = 2 qs qv_i
		double [] qvqv2 = new double[3]; // qvqv2_i = 2 qv_j qv_k

		// Compute the cosine of the rotation angle.
		cost = 2.0 * scalar * scalar - 1.0;

		// Form intermediate vectors qvx2, qsqv2, qvqv2.
		qvx2[0] = vector[0] + vector[0];
		qvx2[1] = vector[1] + vector[1];
		qvx2[2] = vector[2] + vector[2];
		qsqv2[0] = qvx2[0] * scalar;
		qsqv2[1] = qvx2[1] * scalar;
		qsqv2[2] = qvx2[2] * scalar;
		qvqv2[0] = vector[1] * qvx2[2];
		qvqv2[1] = vector[2] * qvx2[0];
		qvqv2[2] = vector[0] * qvx2[1];

		// Construct the transformation matrix diagonal:
		//   T_ii = cost + 2 qv_i^2 = cost + qv_i*qvx2_i
		T[0][0] = cost + vector[0] * qvx2[0];
		T[1][1] = cost + vector[1] * qvx2[1];
		T[2][2] = cost + vector[2] * qvx2[2];

		// Contruct off-diagonal transformation matrix elements:
		//   T_ij = 2 (qv_i qv_j - eps_ijk qs qv_k)
		//        = qvqv2_k - eps_ijk qsqv2_k
		T[0][1] = qvqv2[2] - qsqv2[2];
		T[1][0] = qvqv2[2] + qsqv2[2];
		T[1][2] = qvqv2[0] - qsqv2[0];
		T[2][1] = qvqv2[0] + qsqv2[0];
		T[2][0] = qvqv2[1] - qsqv2[1];
		T[0][2] = qvqv2[1] + qsqv2[1];

		return;
	}

	// Purpose: (Compute the eigen rotation corresponding to a quaternion.)
	// Assumptions and limitations: ((Quaternion is normalized.))
	final void left_quat_to_eigen_rotation ( // Return: -- Void
			double    eigen_angle,                // Out:    r  Eigen angle
			double [] eigen_axis   )              // Out:    -- Eigen axis
	{
		double    qs;
		double [] qv = new double[3];
		double sinht;

		// The left quaternion is [cos(theta/2), -sin(theta/2)*uhat],
		// where theta and uhat represent the rotation from 0-0-0 attitude.

		// Extract the canonical scalar and vector parts from the quaternion.
		if (scalar >= 0.0) {
			qs = scalar;
			Vector3.copy (vector, qv);
		} else {
			qs = -scalar;
			Vector3.negate (vector, qv);
		}

		// Compute the quaternion vector magnitude = sin(theta/2).
		sinht = Vector3.vmag (qv);

		// Compute theta and uhat.
		// The rotation angle is zero and the unit vector is ill-defined
		// when sinht is identically zero.
		if (sinht == 0.0) {
			eigen_angle = 0.0;
			Vector3.initialize (eigen_axis);
			eigen_axis[0] = 1.0;
		}

		// Otherwise (non-zero rotation), compute
		//  uhat  via -qv/sin(theta/2)
		//  theta via 2*asin(sin(theta/2)) or 2*acos(cos(theta/2)).
		else {
			Vector3.scale (qv, -1.0/sinht, eigen_axis);

			// Use the smaller of sinht and qs to determine theta.
			if (sinht < qs) {
				eigen_angle = 2.0 * Math.asin (sinht);
			} else {
				eigen_angle = 2.0 * Math.acos (qs);
			}
		}

		return;
	}

	//*************************************************************************
	// Function: Quaternion::print
	// Purpose: (Print matrix to standard error)
	//*************************************************************************
	public void print ( ) // Return: -- Void
	{
		System.out.println( scalar + " | " +
				vector[0] + ", " + vector[1] + ", " + vector[2] );

		return;
	}

	@Override
	public String toString() {
		return (scalar + " | " + vector[0] + ", " + vector[1] + ", " + vector[2] );
	}


}
