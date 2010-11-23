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
package com.cloud.dc.dao;

import java.util.List;

import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.utils.db.GenericDao;

public interface DataCenterIpAddressDao extends GenericDao<DataCenterIpAddressVO, Long> {
    
    boolean mark(long dcId, long podId, String ip);
    List<DataCenterIpAddressVO> listByPodIdDcIdIpAddress(long podId, long dcId, String ipAddress);
    List<DataCenterIpAddressVO> listByPodIdDcId(long podId, long dcId);
    int countIPs(long podId, long dcId, boolean onlyCountAllocated);
    boolean deleteIpAddressByPod(long podId);

}