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

package com.cloud.async.executor;

import org.apache.log4j.Logger;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.vm.UserVmVO;

public class VMOperationListener implements Listener {
    private static final Logger s_logger = Logger.getLogger(VMOperationListener.class);
	
	private final VMOperationExecutor _executor;
	private final VMOperationParam _param;
	private final UserVmVO _vm;
	private int _cookie;
	
	public VMOperationListener(VMOperationExecutor executor, VMOperationParam param,
		UserVmVO vm, int cookie) {
		
    	if(s_logger.isDebugEnabled())
    		s_logger.debug("VM operation listener is created");
		
		_executor = executor;
		_param = param;
		_vm = vm;
		_cookie = cookie;
	}
	
    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
    	Answer answer = null;
    	if(answers != null)
    		answer = answers[0];
    	
    	if(s_logger.isDebugEnabled())
    		s_logger.debug("Process command answer for " + agentId + "-" + seq + " " + answer);
    	_executor.processAnswer(this, agentId, seq, answer);
    	return true;
    }
    
    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
    	return true;
    }
    
    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
    	return null;
    }
    
    @Override
    public void processConnect(HostVO agent, StartupCommand cmd) {
//    	return true;
    }
    
    @Override
    public boolean processDisconnect(long agentId, Status state) {
    	if(_vm.getHostId() == agentId)
    		_executor.processDisconnect(this, agentId);
    	return true;
    }
    
    @Override
    public boolean isRecurring() {
    	return false;
    }
    
    @Override
    public int getTimeout() {
    	// TODO : no time out support for now as underlying support does not work as expected
    	return -1;
    }
    
    @Override
    public boolean processTimeout(long agentId, long seq) {
    	if(s_logger.isDebugEnabled())
    		s_logger.debug("Process time out for " + agentId + "-" + seq);
    	
    	_executor.processTimeout(this, agentId, seq);
    	return true;
    }
    
	public int getCookie() {
		return _cookie;
	}

	public void setCookie(int cookie) {
		_cookie = cookie;
	}

	public VMOperationExecutor getExecutor() {
		return _executor;
	}

	public VMOperationParam getParam() {
		return _param;
	}

	public UserVmVO getVm() {
		return _vm;
	}
}
