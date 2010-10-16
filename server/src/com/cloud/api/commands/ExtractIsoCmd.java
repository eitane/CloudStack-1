/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 * 
 * You should have received a copy of the GNU General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd.Manager;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ExtractResponse;
import com.cloud.event.EventTypes;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;

@Implementation(method="extract", manager=Manager.TemplateManager)
public class ExtractIsoCmd extends BaseAsyncCmd {
	public static final Logger s_logger = Logger.getLogger(ExtractIsoCmd.class.getName());

    private static final String s_name = "extractisoresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name="id", type=CommandType.LONG, required=true, description="the ID of the ISO file")
    private Long id;

    //FIXME - add description
    @Parameter(name="url", type=CommandType.STRING, required=true)
    private String url;

    @Parameter(name="zoneid", type=CommandType.LONG, required=true, description="the ID of the zone where the ISO is originally located")
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ISO_EXTRACT;
    }

    @Override
    public long getAccountId() {
        VMTemplateVO iso = ApiDBUtils.findTemplateById(getId());
        if (iso != null) {
            return iso.getId();
        }

        // invalid id, parent this command to SYSTEM so ERROR events are tracked
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventDescription() {
        return  "Extraction job";
    }

    @Override @SuppressWarnings("unchecked")
    public ExtractResponse getResponse() {
        ExtractResponse response = (ExtractResponse)getResponseObject();
        response.setResponseName(getName());
        return response;
    }

    public static String getStaticName() {
        return "ExtractIso";
    }
}