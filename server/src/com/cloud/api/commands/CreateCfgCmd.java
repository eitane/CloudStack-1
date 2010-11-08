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
package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.ApiResponseHelper;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ConfigurationResponse;
import com.cloud.configuration.Configuration;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ConfigurationVO;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;

@Implementation(method="addConfig", manager=ConfigurationManager.class)
public class CreateCfgCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(CreateCfgCmd.class.getName());
    private static final String s_name = "addconfigresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.CATEGORY, type=CommandType.STRING, required=true, description="component's category")
    private String category;

    @Parameter(name=ApiConstants.COMPONENT, type=CommandType.STRING, required=true, description="the component of the configuration")
    private String component;

    @Parameter(name=ApiConstants.DESCRIPTION, type=CommandType.STRING, description="the description of the configuration")
    private String description;

    //FIXME - add description
    @Parameter(name=ApiConstants.INSTANCE, type=CommandType.STRING, required=true)
    private String instance;

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required=true, description="the name of the configuration")
    private String name;

    @Parameter(name=ApiConstants.VALUE, type=CommandType.STRING, description="the value of the configuration")
    private String value;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getCategory() {
        return category;
    }

    public String getComponent() {
        return component;
    }

    public String getDescription() {
        return description;
    }

    public String getInstance() {
        return instance;
    }

    public String getConfigPropName() {
        return name;
    }

    public String getValue() {
        return value;
    }


    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getName() {
        return s_name;
    }
    
    @Override @SuppressWarnings("unchecked")
    public ConfigurationResponse getResponse() {
        ConfigurationVO cfg = (ConfigurationVO)getResponseObject();
        if (cfg != null) {
            ConfigurationResponse response = ApiResponseHelper.createConfigurationResponse(cfg);
            response.setResponseName(getName());
            return response;
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to add config");
        }
    }
    
    @Override
    public Object execute() throws ServerApiException, InvalidParameterValueException, PermissionDeniedException, InsufficientAddressCapacityException, InsufficientCapacityException, ConcurrentOperationException{
        Configuration result = _configService.addConfig(this);
        return result;
    }
}