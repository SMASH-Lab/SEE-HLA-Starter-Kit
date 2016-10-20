/*****************************************************************
SEE HLA Starter Kit -  A Java framework to develop HLA Federates
in the context of the SEE (Simulation Exploration Experience) 
project.
Copyright (c) 2014, SMASH Lab - University of Calabria (Italy), 
All rights reserved.

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
package skf.coder;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.exceptions.RTIinternalError;



public class HLAbooleanCoder implements Coder<Boolean>{

	private HLAboolean coder = null;
	private EncoderFactory factory = null;

	public HLAbooleanCoder() throws RTIinternalError {
		this.factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		this.coder = factory.createHLAboolean();
	}

	@Override
	public Boolean decode(byte[] code) throws DecoderException {
		coder.decode(code);
		return coder.getValue();
	}

	@Override
	public byte[] encode(Boolean element) {
		coder.setValue(element);
		return coder.toByteArray();
	}
	
	@Override
	public Class<Boolean> getAllowedType() {
		return Boolean.class;
	}
}