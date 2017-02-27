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
package siso.smackdown.frame;


public enum FrameType {
	
	SolarSystemBarycentricInertial("SolarSystemBarycentricInertial", null),
	
	SunCentricInertial("SunCentricInertial", "SolarSystemBarycentricInertial"),
	
	EarthMoonBarycentricInertial("EarthMoonBarycentricInertial", "SolarSystemBarycentricInertial"),
	EarthMoonBarycentricRotating("EarthMoonBarycentricRotating", "EarthMoonBarycentricInertial"),
	
	EarthMoonL2Rotating("EarthMoonL2Rotating", "EarthMoonBarycentricInertial"),
	
	EarthCentricInertial("EarthCentricInertial", "EarthMoonBarycentricInertial"),
	EarthCentricFixed("EarthCentricFixed", "EarthCentricInertial"),
	
	MoonCentricInertial("MoonCentricInertial", "EarthMoonBarycentricInertial"),
	MoonCentricFixed("MoonCentricFixed", "MoonCentricInertial"),
	
	MarsCentricInertial("MarsCentricInertial", "SolarSystemBarycentricInertial"),
	MarsCentricFixed("MarsCentricFixed", "MarsCentricInertial"),
	
	AitkenBasinLocalFixed("AitkenBasinLocalFixed", "MoonCentricFixed");
	
	
	private String name;
	private String parentFrame;

	private FrameType (String name, String parentFrame) {
		this.name = name;
		this.parentFrame = parentFrame;
	}

	public String getParentFrame() {
		return ((this.parentFrame == null)?"<None>":this.parentFrame);
	}

	public static FrameType lookup(String value) {
		for(FrameType ft : values())
			if(ft.name.equals(value))
				return ft;
		return null;
	}

}
