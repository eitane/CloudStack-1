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

package com.cloud.network.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.network.FirewallRuleVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

@Local(value = { FirewallRulesDao.class })
public class FirewallRulesDaoImpl extends GenericDaoBase<FirewallRuleVO, Long> implements FirewallRulesDao {
    private static final Logger s_logger = Logger.getLogger(FirewallRulesDaoImpl.class);

    public static String SELECT_IP_FORWARDINGS_BY_USERID_SQL   = null;
    public static String SELECT_IP_FORWARDINGS_BY_USERID_AND_DCID_SQL = null;
    public static String SELECT_LB_FORWARDINGS_BY_USERID_AND_DCID_SQL = null;


    public static final String           DELETE_IP_FORWARDING_BY_IPADDRESS_SQL = "DELETE FROM ip_forwarding WHERE public_ip_address = ?";
    public static final String           DELETE_IP_FORWARDING_BY_IP_PORT_SQL = "DELETE FROM ip_forwarding WHERE public_ip_address = ? and public_port = ?";

    public static final String           DISABLE_IP_FORWARDING_BY_IPADDRESS_SQL = "UPDATE  ip_forwarding set enabled=0 WHERE public_ip_address = ?";


    protected SearchBuilder<FirewallRuleVO> FWByIPSearch;
    protected SearchBuilder<FirewallRuleVO> FWByIPAndForwardingSearch;
    protected SearchBuilder<FirewallRuleVO> FWByIPPortAndForwardingSearch;
    protected SearchBuilder<FirewallRuleVO> FWByIPPortProtoSearch;
    protected SearchBuilder<FirewallRuleVO> FWByIPPortAlgoSearch;
    protected SearchBuilder<FirewallRuleVO> FWByPrivateIPSearch;
    protected SearchBuilder<FirewallRuleVO> RulesExcludingPubIpPort;
    protected SearchBuilder<FirewallRuleVO> FWByGroupId;
    protected SearchBuilder<FirewallRuleVO> FWByIpForLB;

    protected SearchBuilder<FirewallRuleVO> FWByGroupAndPrivateIp;
    protected SearchBuilder<FirewallRuleVO> FWByPrivateIpPrivatePortPublicIpPublicPortSearch;
    protected SearchBuilder<FirewallRuleVO> OneToOneNATSearch;


    protected FirewallRulesDaoImpl() {
    }
    
    @Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if (!super.configure(name, params)) {
            return false;
        }
        
        SELECT_IP_FORWARDINGS_BY_USERID_SQL = buildSelectByUserIdSql();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(SELECT_IP_FORWARDINGS_BY_USERID_SQL);
        }
        
        SELECT_IP_FORWARDINGS_BY_USERID_AND_DCID_SQL = buildSelectByUserIdAndDatacenterIdSql();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(SELECT_IP_FORWARDINGS_BY_USERID_AND_DCID_SQL);
        }
        
        SELECT_LB_FORWARDINGS_BY_USERID_AND_DCID_SQL = buildSelectByUserIdAndDatacenterIdForLBSql();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(SELECT_LB_FORWARDINGS_BY_USERID_AND_DCID_SQL);
        }
        

        FWByIPSearch = createSearchBuilder();
        FWByIPSearch.and("publicIpAddress", FWByIPSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByIPSearch.done();
        
        FWByIPAndForwardingSearch = createSearchBuilder();
        FWByIPAndForwardingSearch.and("publicIpAddress", FWByIPAndForwardingSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByIPAndForwardingSearch.and("forwarding", FWByIPAndForwardingSearch.entity().isForwarding(), SearchCriteria.Op.EQ);
        FWByIPAndForwardingSearch.done();
        
        FWByIPPortAndForwardingSearch = createSearchBuilder();
        FWByIPPortAndForwardingSearch.and("publicIpAddress", FWByIPPortAndForwardingSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByIPPortAndForwardingSearch.and("publicPort", FWByIPPortAndForwardingSearch.entity().getPublicPort(), SearchCriteria.Op.EQ);
        FWByIPPortAndForwardingSearch.and("forwarding", FWByIPPortAndForwardingSearch.entity().isForwarding(), SearchCriteria.Op.EQ);
        FWByIPPortAndForwardingSearch.done();
        
        FWByIPPortProtoSearch = createSearchBuilder();
        FWByIPPortProtoSearch.and("publicIpAddress", FWByIPPortProtoSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByIPPortProtoSearch.and("publicPort", FWByIPPortProtoSearch.entity().getPublicPort(), SearchCriteria.Op.EQ);
        FWByIPPortProtoSearch.and("protocol", FWByIPPortProtoSearch.entity().getProtocol(), SearchCriteria.Op.EQ);
        FWByIPPortProtoSearch.done();
        
        FWByIPPortAlgoSearch = createSearchBuilder();
        FWByIPPortAlgoSearch.and("publicIpAddress", FWByIPPortAlgoSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByIPPortAlgoSearch.and("publicPort", FWByIPPortAlgoSearch.entity().getPublicPort(), SearchCriteria.Op.EQ);
        FWByIPPortAlgoSearch.and("algorithm", FWByIPPortAlgoSearch.entity().getAlgorithm(), SearchCriteria.Op.EQ);
        FWByIPPortAlgoSearch.done();

        FWByPrivateIPSearch = createSearchBuilder();
        FWByPrivateIPSearch.and("privateIpAddress", FWByPrivateIPSearch.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        FWByPrivateIPSearch.done();

        RulesExcludingPubIpPort = createSearchBuilder();
        RulesExcludingPubIpPort.and("publicIpAddress", RulesExcludingPubIpPort.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        RulesExcludingPubIpPort.and("groupId", RulesExcludingPubIpPort.entity().getGroupId(), SearchCriteria.Op.NEQ);
        RulesExcludingPubIpPort.and("forwarding", RulesExcludingPubIpPort.entity().isForwarding(), SearchCriteria.Op.EQ);
        RulesExcludingPubIpPort.done();

        FWByGroupId = createSearchBuilder();
        FWByGroupId.and("groupId", FWByGroupId.entity().getGroupId(), SearchCriteria.Op.EQ);
        FWByGroupId.and("forwarding", FWByGroupId.entity().isForwarding(), SearchCriteria.Op.EQ);
        FWByGroupId.done();

        FWByGroupAndPrivateIp = createSearchBuilder();
        FWByGroupAndPrivateIp.and("groupId", FWByGroupAndPrivateIp.entity().getGroupId(), SearchCriteria.Op.EQ);
        FWByGroupAndPrivateIp.and("privateIpAddress", FWByGroupAndPrivateIp.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        FWByGroupAndPrivateIp.and("forwarding", FWByGroupAndPrivateIp.entity().isForwarding(), SearchCriteria.Op.EQ);
        FWByGroupAndPrivateIp.done();

        FWByPrivateIpPrivatePortPublicIpPublicPortSearch = createSearchBuilder();
        FWByPrivateIpPrivatePortPublicIpPublicPortSearch.and("publicIpAddress", FWByPrivateIpPrivatePortPublicIpPublicPortSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByPrivateIpPrivatePortPublicIpPublicPortSearch.and("privateIpAddress", FWByPrivateIpPrivatePortPublicIpPublicPortSearch.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        FWByPrivateIpPrivatePortPublicIpPublicPortSearch.and("privatePort", FWByPrivateIpPrivatePortPublicIpPublicPortSearch.entity().getPrivatePort(), SearchCriteria.Op.NULL);
        FWByPrivateIpPrivatePortPublicIpPublicPortSearch.and("publicPort", FWByPrivateIpPrivatePortPublicIpPublicPortSearch.entity().getPublicPort(), SearchCriteria.Op.NULL);
        FWByPrivateIpPrivatePortPublicIpPublicPortSearch.done();
        
        OneToOneNATSearch = createSearchBuilder();
        OneToOneNATSearch.and("publicIpAddress", OneToOneNATSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        OneToOneNATSearch.and("protocol", OneToOneNATSearch.entity().getProtocol(), SearchCriteria.Op.EQ);
        OneToOneNATSearch.done();
        
        FWByIpForLB = createSearchBuilder();
        FWByIpForLB.and("publicIpAddress", FWByIpForLB.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        FWByIpForLB.and("groupId", FWByIpForLB.entity().getGroupId(), SearchCriteria.Op.NNULL);
        FWByIpForLB.and("forwarding", FWByIpForLB.entity().isForwarding(), SearchCriteria.Op.EQ);
        FWByIpForLB.done();
        
        return true;
    }

    protected String buildSelectByUserIdSql() {
        StringBuilder sql = createPartialSelectSql(null, true);
        sql.insert(sql.length() - 6, ", user_ip_address ");
        sql.append("ip_forwarding.public_ip_address = user_ip_address.public_ip_address AND user_ip_address.account_id = ?");

        return sql.toString();
    }
    
    protected String buildSelectByUserIdAndDatacenterIdSql() {
    	return "SELECT i.id, i.group_id, i.public_ip_address, i.public_port, i.private_ip_address, i.private_port, i.enabled, i.protocol, i.forwarding, i.algorithm FROM ip_forwarding i, user_ip_address u WHERE i.public_ip_address=u.public_ip_address AND u.account_id=? AND u.data_center_id=?";
    }
    
    protected String buildSelectByUserIdAndDatacenterIdForLBSql() {
    	return "SELECT i.id, i.group_id, i.public_ip_address, i.public_port, i.private_ip_address, i.private_port, i.enabled, i.protocol, i.forwarding, i.algorithm FROM ip_forwarding i, user_ip_address u WHERE i.public_ip_address=u.public_ip_address AND u.account_id=? AND u.data_center_id=? AND i.group_id is not NULL";
    }

    public List<FirewallRuleVO> listIPForwarding(String publicIPAddress, boolean forwarding) {
        SearchCriteria<FirewallRuleVO> sc = FWByIPAndForwardingSearch.create();
        sc.setParameters("publicIpAddress", publicIPAddress);
        sc.setParameters("forwarding", forwarding);
        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listIPForwarding(long userId) {
        Transaction txn = Transaction.currentTxn();
        List<FirewallRuleVO> forwardings = new ArrayList<FirewallRuleVO>();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(SELECT_IP_FORWARDINGS_BY_USERID_SQL);
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                forwardings.add(toEntityBean(rs, false));
            }
        } catch (Exception e) {
        	s_logger.warn(e);
        }
        return forwardings;
    }
    
    public List<FirewallRuleVO> listIPForwarding(long userId, long dcId) {
    	Transaction txn = Transaction.currentTxn();
        List<FirewallRuleVO> forwardings = new ArrayList<FirewallRuleVO>();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(SELECT_IP_FORWARDINGS_BY_USERID_AND_DCID_SQL);
            pstmt.setLong(1, userId);
            pstmt.setLong(2, dcId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                forwardings.add(toEntityBean(rs, false));
            }
        } catch (Exception e) {
        	s_logger.warn(e);
        }
        return forwardings;
    }

    @Override
    public void deleteIPForwardingByPublicIpAddress(String ipAddress) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(DELETE_IP_FORWARDING_BY_IPADDRESS_SQL);
            pstmt.setString(1, ipAddress);
            pstmt.executeUpdate();
        } catch (Exception e) {
        	s_logger.warn(e);
        }
    }

    @Override
    public void deleteIPForwardingByPublicIpAndPort(String ipAddress, String port) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(DELETE_IP_FORWARDING_BY_IP_PORT_SQL);
            pstmt.setString(1, ipAddress);
            pstmt.setString(2, port);

            pstmt.executeUpdate();
        } catch (Exception e) {
        	s_logger.warn(e);
        }
    }
    
    @Override
    public List<FirewallRuleVO> listIPForwarding(String publicIPAddress) {
        SearchCriteria<FirewallRuleVO> sc = FWByIPSearch.create();
        sc.setParameters("publicIpAddress", publicIPAddress);
        return listBy(sc);
    }

	@Override
	public List<FirewallRuleVO> listIPForwardingForUpdate(String publicIPAddress) {
		SearchCriteria<FirewallRuleVO> sc = FWByIPSearch.create();
        sc.setParameters("publicIpAddress", publicIPAddress);
        return listBy(sc, null);
	}

	@Override
	public List<FirewallRuleVO> listIPForwardingForUpdate(String publicIp, boolean fwding) {
        SearchCriteria<FirewallRuleVO> sc = FWByIPAndForwardingSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        sc.setParameters("forwarding", fwding);
        return search(sc, null);
	}

	@Override
	public List<FirewallRuleVO> listIPForwardingForUpdate(String publicIp,
			String publicPort, String proto) {
		SearchCriteria<FirewallRuleVO> sc = FWByIPPortProtoSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        sc.setParameters("publicPort", publicPort);
        sc.setParameters("protocol", proto);
        return search(sc, null);
	}
	
	@Override
	public List<FirewallRuleVO> listLoadBalanceRulesForUpdate(String publicIp,
			String publicPort, String algo) {
		SearchCriteria<FirewallRuleVO> sc = FWByIPPortAlgoSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        sc.setParameters("publicPort", publicPort);
        sc.setParameters("algorithm", algo);
        return listBy(sc, null);
	}

	@Override
	public List<FirewallRuleVO> listIPForwarding(String publicIPAddress,
			String port, boolean forwarding) {
		SearchCriteria<FirewallRuleVO> sc = FWByIPPortAndForwardingSearch.create();
        sc.setParameters("publicIpAddress", publicIPAddress);
        sc.setParameters("publicPort", port);
        sc.setParameters("forwarding", forwarding);

        return listBy(sc);
	}

	@Override
	public void disableIPForwarding(String publicIPAddress) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
        	txn.start();
            pstmt = txn.prepareAutoCloseStatement(DISABLE_IP_FORWARDING_BY_IPADDRESS_SQL);
            pstmt.setString(1, publicIPAddress);
            pstmt.executeUpdate();
            txn.commit();
        } catch (Exception e) {
            txn.rollback();
            throw new CloudRuntimeException("DB Exception ", e);
        }
	}

	@Override
    public List<FirewallRuleVO> listRulesExcludingPubIpPort(String publicIpAddress, long securityGroupId) {
        SearchCriteria<FirewallRuleVO> sc = RulesExcludingPubIpPort.create();
        sc.setParameters("publicIpAddress", publicIpAddress);
        sc.setParameters("groupId", securityGroupId);
        sc.setParameters("forwarding", false);
        return listBy(sc);
    }

	@Override
    public List<FirewallRuleVO> listBySecurityGroupId(long securityGroupId) {
	    SearchCriteria<FirewallRuleVO> sc = FWByGroupId.create();
	    sc.setParameters("groupId", securityGroupId);
        sc.setParameters("forwarding", Boolean.TRUE);
	    return listBy(sc);
	}

    @Override
    public List<FirewallRuleVO> listForwardingByPubAndPrivIp(boolean forwarding, String publicIPAddress, String privateIp) {
        SearchCriteria<FirewallRuleVO> sc = FWByIPAndForwardingSearch.create();
        sc.setParameters("publicIpAddress", publicIPAddress);
        sc.setParameters("forwarding", forwarding);
        sc.addAnd("privateIpAddress", SearchCriteria.Op.EQ, privateIp);
        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByLoadBalancerId(long loadBalancerId) {
        SearchCriteria<FirewallRuleVO> sc = FWByGroupId.create();
        sc.setParameters("groupId", loadBalancerId);
        sc.setParameters("forwarding", Boolean.FALSE);
        return listBy(sc);
    }

    @Override
    public FirewallRuleVO findByGroupAndPrivateIp(long groupId, String privateIp, boolean forwarding) {
        SearchCriteria<FirewallRuleVO> sc = FWByGroupAndPrivateIp.create();
        sc.setParameters("groupId", groupId);
        sc.setParameters("privateIpAddress", privateIp);
        sc.setParameters("forwarding", forwarding);
        return findOneBy(sc);
        
    }
    
    @Override
    public List<FirewallRuleVO> findByPublicIpPrivateIpForNatRule(String publicIp, String privateIp){
    	SearchCriteria<FirewallRuleVO> sc = FWByPrivateIpPrivatePortPublicIpPublicPortSearch.create();
    	sc.setParameters("publicIpAddress", publicIp);
    	sc.setParameters("privateIpAddress", privateIp);
    	return listBy(sc);
    }
    
	@Override
	public List<FirewallRuleVO> listByPrivateIp(String privateIp) {
		SearchCriteria<FirewallRuleVO> sc = FWByPrivateIPSearch.create();
        sc.setParameters("privateIpAddress", privateIp);
        return listBy(sc);
	}

	@Override
	public List<FirewallRuleVO> listIPForwardingByPortAndProto(String publicIp,
			String publicPort, String proto) {
		SearchCriteria<FirewallRuleVO> sc = FWByIPPortProtoSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        sc.setParameters("publicPort", publicPort);
        sc.setParameters("protocol", proto);
        return search(sc, null);
	}

	@Override
	public boolean isPublicIpOneToOneNATted(String publicIp) {
		SearchCriteria<FirewallRuleVO> sc = OneToOneNATSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        sc.setParameters("protocol", NetUtils.NAT_PROTO);
        List<FirewallRuleVO> rules = search(sc, null);
        if (rules.size() != 1)
        	return false;
        return rules.get(1).getProtocol().equalsIgnoreCase(NetUtils.NAT_PROTO);
	}

	@Override
	public List<FirewallRuleVO> listIpForwardingRulesForLoadBalancers(
			String publicIp) {
		SearchCriteria<FirewallRuleVO> sc = FWByIpForLB.create();
        sc.setParameters("publicIpAddress", publicIp);
        sc.setParameters("forwarding", false);
        return search(sc, null);
	}
	
	@Override
    public List<FirewallRuleVO> listIPForwardingForLB(long userId, long dcId) {
    	Transaction txn = Transaction.currentTxn();
        List<FirewallRuleVO> forwardings = new ArrayList<FirewallRuleVO>();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(SELECT_LB_FORWARDINGS_BY_USERID_AND_DCID_SQL);
            pstmt.setLong(1, userId);
            pstmt.setLong(2, dcId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                forwardings.add(toEntityBean(rs, false));
            }
        } catch (Exception e) {
        	s_logger.warn(e);
        }
        return forwardings;
    }
}