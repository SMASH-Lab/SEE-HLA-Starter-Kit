/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London. All rights reserved.

 GNU Lesser General Public License (GNU LGPL).

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library.
 If not, see http://http://www.gnu.org/licenses/
 *****************************************************************/

package org.see.skf.core;

import hla.rti1516_2025.encoding.DecoderException;

/**
 * The base interface for all coders in the SEE HLA Starter Kit. A coder serves the purpose of encoding a native Java
 * data type a suitable equivalent in the HLA federation object model (FOM) and vice versa. For instance, the String
 * type can be mapped to an HLAunicodeString or HLAASCIIchar. The encoded representations are byte arrays and thus need
 * to be decoded prior to any read/write operation.
 *
 * @param <T> The type that will be encoded and decoded by this coder.
 * @since 1.5
 */
public interface Coder<T> {
    T decode(byte[] buffer) throws DecoderException;
    byte[] encode(T element);
    Class<T> getAllowedType();
}
