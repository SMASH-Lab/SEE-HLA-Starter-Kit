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
package skf.model.object;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.encoding.DecoderException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import skf.coder.Coder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;
import skf.model.parser.AbstractObjectModelParser;

public class ObjectClassModelParser extends AbstractObjectModelParser {

	private static final Logger logger = LogManager.getLogger(ObjectClassModelParser.class);

	protected Class<? extends ObjectClass> objectClassModel = null;

	public ObjectClassModelParser(Class<? extends ObjectClass> objectClassModel) {
		super();
		this.objectClassModel = objectClassModel;
		retrieveClassModelStructure();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void retrieveClassModelStructure() {

		this.classHandleName = objectClassModel.getAnnotation(ObjectClass.class).name();

		fields = objectClassModel.getDeclaredFields();
		Map<Class<? extends ObjectClass>, Coder> tmpMapCoder = new HashMap<Class<? extends ObjectClass>, Coder>();
		Coder coderTmp = null;

		try {
			for(Field f : fields){
				if(f.isAnnotationPresent(Attribute.class)){
					coderTmp = tmpMapCoder.get(f.getAnnotation(Attribute.class).coder());
					if(coderTmp == null){
						coderTmp = f.getAnnotation(Attribute.class).coder().newInstance();
						tmpMapCoder.put((Class<? extends ObjectClass>) f.getAnnotation(Attribute.class).coder(), coderTmp);
					}
					matchingObjectCoderIsValid(f, coderTmp);
					mapFieldCoder.put(f.getAnnotation(Attribute.class).name(), coderTmp);
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Error in retreving the annotations of the fields");
			e.printStackTrace();
		} finally {
			tmpMapCoder = null;
			coderTmp = null;
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, byte[]> encode(Object element) {

		if(element == null){
			logger.error("The argument is null");
			throw new IllegalArgumentException("The argument is null");
		}

		logger.debug("Encoding: "+element.toString());

		PropertyDescriptor pd = null;
		Object fieldValue = null;
		Coder fieldCoder = null;

		try {
			for(Field f : fields)
				if(f.isAnnotationPresent(Attribute.class)){
					pd = new PropertyDescriptor(f.getName(), element.getClass());
					fieldValue = pd.getReadMethod().invoke(element);
					if(fieldValue != null){
						fieldCoder = mapFieldCoder.get(f.getAnnotation(Attribute.class).name());
						logger.trace(element.getClass().getName()+ " Update [ Field "+f.getName()+", value: "+fieldValue+" ]");
						encoderMap.put(f.getAnnotation(Attribute.class).name(), fieldCoder.encode(fieldValue));
					}
					else{
						logger.error("The field ' "+f.getName()+" ' is null");
						throw new NullPointerException("The field ' "+f.getName()+" ' is null");
					}
				}
		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error in retreving the value of the fields");
			e.printStackTrace();
		}

		return encoderMap;
	}

	@SuppressWarnings("unchecked")
	public void decode(Object element, Map<String, AttributeHandle> mapFieldNameAttributeHandle, AttributeHandleValueMap arg1) {

		if(element == null || arg1 == null || mapFieldNameAttributeHandle == null){
			logger.error("Some arguments are null");
			throw new IllegalArgumentException("Some arguments are null");
		}

		PropertyDescriptor pd = null;
		Coder<Object> fieldCoder = null;
		byte[] currValue = null;
		try {
			for(Field f : fields)
				if(f.isAnnotationPresent(Attribute.class)){
					pd = new PropertyDescriptor(f.getName(), element.getClass());
					fieldCoder = this.mapFieldCoder.get(f.getAnnotation(Attribute.class).name());
					currValue = arg1.get(mapFieldNameAttributeHandle.get(f.getAnnotation(Attribute.class).name()));
					if(currValue != null){
						pd.getWriteMethod().invoke(element, fieldCoder.decode(currValue));
						logger.debug("Attribute decoded [ Field "+f.getName()+", value: "+fieldCoder.decode(currValue)+" ]");
						}
					else
						logger.debug("Attribute decoded [ Field "+f.getName()+", value: null ]");
				}
		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | DecoderException e) {
			e.printStackTrace();
		}

	}

}
