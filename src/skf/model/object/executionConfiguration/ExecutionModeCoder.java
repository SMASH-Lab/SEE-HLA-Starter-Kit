package skf.model.object.executionConfiguration;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger16LE;
import hla.rti1516e.exceptions.RTIinternalError;
import skf.coder.Coder;

public class ExecutionModeCoder implements Coder<ExecutionMode> {
	
	private EncoderFactory factory = null;
	private HLAinteger16LE value = null;
	
	public ExecutionModeCoder() throws RTIinternalError {
		this.factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		this.value = factory.createHLAinteger16LE();
		
	}

	@Override
	public ExecutionMode decode(byte[] arg0) throws DecoderException {
		value.decode(arg0);
		return ExecutionMode.lookup(value.getValue());
		
	}

	@Override
	public byte[] encode(ExecutionMode arg0) {
		value.setValue(arg0.getValue());
		return value.toByteArray();
	}

	@Override
	public Class<ExecutionMode> getAllowedType() {
		return ExecutionMode.class;
	}

}
