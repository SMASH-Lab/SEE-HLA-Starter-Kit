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

package org.see.skf.runtime;

public enum DeclarationStatus {
    PUBLISHED,
    SUBSCRIBED,
    PUBLISHED_SUBSCRIBED,
    UNDESIGNATED;

    public static DeclarationStatus setPublishFlag(DeclarationStatus status) {
        if (status == UNDESIGNATED) {
            return PUBLISHED;
        } else if (status == SUBSCRIBED) {
            return PUBLISHED_SUBSCRIBED;
        } else {
            return status;
        }
    }

    public static DeclarationStatus unsetPublishFlag(DeclarationStatus status) {
        if (status == PUBLISHED) {
            return UNDESIGNATED;
        } else if (status == PUBLISHED_SUBSCRIBED) {
            return SUBSCRIBED;
        } else {
            return status;
        }
    }

    public static DeclarationStatus setSubscribeFlag(DeclarationStatus status) {
        if (status == UNDESIGNATED) {
            return SUBSCRIBED;
        } else if (status == PUBLISHED) {
            return PUBLISHED_SUBSCRIBED;
        } else {
            return status;
        }
    }

    public static DeclarationStatus unsetSubscribeFlag(DeclarationStatus status) {
        if (status == SUBSCRIBED) {
            return UNDESIGNATED;
        } else if (status == PUBLISHED_SUBSCRIBED) {
            return PUBLISHED;
        } else {
            return status;
        }
    }
}
