/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.hypervisor;

public class Hypervisor {

    public static enum HypervisorType {
    	None, //for storage hosts
    	Xen,
    	XenServer,
    	KVM,
    	VmWare,
    	VirtualBox,
    	Parralels,
    	Any; /*If you don't care about the hypervisor type*/

    	public static HypervisorType getType(String hypervisor) {
    		if (hypervisor == null) {
    			return HypervisorType.None;
    		}

    		if (hypervisor.equalsIgnoreCase("Xen")) {
    			return HypervisorType.Xen;
    		} else if (hypervisor.equalsIgnoreCase("XenServer")) {
    			return HypervisorType.XenServer;
    		} else if (hypervisor.equalsIgnoreCase("KVM")) {
    			return HypervisorType.KVM;
    		} else if (hypervisor.equalsIgnoreCase("VmWare")) {
    			return HypervisorType.VmWare;
    		} else if (hypervisor.equalsIgnoreCase("VirtualBox")) {
    			return HypervisorType.VirtualBox;
    		} else if (hypervisor.equalsIgnoreCase("Parralels")) {
    			return HypervisorType.Parralels;
    		} else if (hypervisor.equalsIgnoreCase("Any")) {
    			return HypervisorType.Any;
    		} else {
    			return HypervisorType.None;
    		}
    	}
    }
}
