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
package com.cloud.network.vpn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.commands.ListRemoteAccessVpnsCmd;
import com.cloud.api.commands.ListVpnUsersCmd;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.EventTypes;
import com.cloud.event.EventUtils;
import com.cloud.exception.AccountLimitException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.NetworkManager;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.RemoteAccessVpnVO;
import com.cloud.network.VpnUser;
import com.cloud.network.VpnUser.State;
import com.cloud.network.VpnUserVO;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.RemoteAccessVpnDao;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.router.VirtualNetworkApplianceManager;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.RulesManager;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.UserContext;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.PasswordGenerator;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;

@Local(value = RemoteAccessVpnService.class)
public class RemoteAccessVpnManagerImpl implements RemoteAccessVpnService, Manager {
    private final static Logger s_logger = Logger.getLogger(RemoteAccessVpnManagerImpl.class);
    String _name;
    
    @Inject AccountDao _accountDao;
    @Inject VpnUserDao _vpnUsersDao;
    @Inject RemoteAccessVpnDao _remoteAccessVpnDao;
    @Inject IPAddressDao _ipAddressDao;
    @Inject VirtualNetworkApplianceManager _routerMgr;
    @Inject AccountManager _accountMgr;
    @Inject NetworkManager _networkMgr;
    @Inject RulesManager _rulesMgr;
    @Inject DomainDao _domainDao;
    @Inject FirewallRulesDao _rulesDao;
    
    int _userLimit;
    int _pskLength;
    String _clientIpRange;
    SearchBuilder<RemoteAccessVpnVO> VpnSearch;

    @Override
    public RemoteAccessVpn createRemoteAccessVpn(Ip publicIp, String ipRange) throws NetworkRuleConflictException {
        UserContext ctx = UserContext.current();
        Account caller = ctx.getCaller();

        // make sure ip address exists
        PublicIpAddress ipAddr = _networkMgr.getPublicIpAddress(publicIp);
        if (ipAddr == null) {
            throw new InvalidParameterValueException("Unable to create remote access vpn, invalid public IP address " + publicIp);
        }

        _accountMgr.checkAccess(caller, ipAddr);

        if (!ipAddr.readyToUse() || ipAddr.getAssociatedWithNetworkId() == null) {
            throw new InvalidParameterValueException("The Ip address is not ready to be used yet: " + ipAddr.getAddress());
        }

        RemoteAccessVpnVO vpnVO = _remoteAccessVpnDao.findByPublicIpAddress(publicIp.toString());
       
        if (vpnVO != null) {
            //if vpn is in Added state, return it to the api
            if (vpnVO.getState() == RemoteAccessVpn.State.Added) {
                return vpnVO;
            }
            throw new InvalidParameterValueException("A Remote Access VPN already exists for this public Ip address");
        }

        // TODO: assumes one virtual network / domr per account per zone
        vpnVO = _remoteAccessVpnDao.findByAccountAndNetwork(ipAddr.getAllocatedToAccountId(), ipAddr.getAssociatedWithNetworkId());
        if (vpnVO != null) {
            //if vpn is in Added state, return it to the api
            if (vpnVO.getState() == RemoteAccessVpn.State.Added) {
                return vpnVO;
            }
            throw new InvalidParameterValueException("A Remote Access VPN already exists for this account");
        }

        if (ipRange == null) {
            ipRange = _clientIpRange;
        }
        String[] range = ipRange.split("-");
        if (range.length != 2) {
            throw new InvalidParameterValueException("Invalid ip range");
        }
        if (!NetUtils.isValidIp(range[0]) || !NetUtils.isValidIp(range[1])) {
            throw new InvalidParameterValueException("Invalid ip in range specification " + ipRange);
        }
        if (!NetUtils.validIpRange(range[0], range[1])) {
            throw new InvalidParameterValueException("Invalid ip range " + ipRange);
        }

        Network network = _networkMgr.getNetwork(ipAddr.getAssociatedWithNetworkId());
        Pair<String, Integer> cidr = NetUtils.getCidr(network.getCidr());

        // FIXME: This check won't work for the case where the guest ip range
        // changes depending on the vlan allocated.
        String[] guestIpRange = NetUtils.getIpRangeFromCidr(cidr.first(), cidr.second());
        if (NetUtils.ipRangesOverlap(range[0], range[1], guestIpRange[0], guestIpRange[1])) {
            throw new InvalidParameterValueException("Invalid ip range: " + ipRange + " overlaps with guest ip range " + guestIpRange[0] + "-"
                    + guestIpRange[1]);
        }
        // TODO: check sufficient range
        // TODO: check overlap with private and public ip ranges in datacenter

        long startIp = NetUtils.ip2Long(range[0]);
        String newIpRange = NetUtils.long2Ip(++startIp) + "-" + range[1];
        String sharedSecret = PasswordGenerator.generatePresharedKey(_pskLength);
        _rulesMgr.reservePorts(ipAddr, NetUtils.UDP_PROTO, Purpose.Vpn, NetUtils.VPN_PORT, NetUtils.VPN_L2TP_PORT, NetUtils.VPN_NATT_PORT);
        vpnVO = new RemoteAccessVpnVO(ipAddr.getAllocatedToAccountId(), ipAddr.getAllocatedInDomainId(), ipAddr.getAssociatedWithNetworkId(),
                publicIp, range[0], newIpRange, sharedSecret);
        return _remoteAccessVpnDao.persist(vpnVO);
    }

    private void validateRemoteAccessVpnConfiguration() throws ConfigurationException {
        String ipRange = _clientIpRange;
        if (ipRange == null) {
            s_logger.warn("Remote Access VPN configuration missing client ip range -- ignoring");
            return;
        }
        Integer pskLength = _pskLength;
        if (pskLength != null && (pskLength < 8 || pskLength > 256)) {
            throw new ConfigurationException("Remote Access VPN: IPSec preshared key length should be between 8 and 256");
        } else if (pskLength == null) {
            s_logger.warn("Remote Access VPN configuration missing Preshared Key Length -- ignoring");
            return;
        }

        String[] range = ipRange.split("-");
        if (range.length != 2) {
            throw new ConfigurationException("Remote Access VPN: Invalid ip range " + ipRange);
        }
        if (!NetUtils.isValidIp(range[0]) || !NetUtils.isValidIp(range[1])) {
            throw new ConfigurationException("Remote Access VPN: Invalid ip in range specification " + ipRange);
        }
        if (!NetUtils.validIpRange(range[0], range[1])) {
            throw new ConfigurationException("Remote Access VPN: Invalid ip range " + ipRange);
        }
    }

    @Override @DB
    public void destroyRemoteAccessVpn(Ip ip, long startEventId) throws ResourceUnavailableException {
        Account caller = UserContext.current().getCaller();
        
        RemoteAccessVpnVO vpn = _remoteAccessVpnDao.findById(ip);
        if (vpn == null) {
            s_logger.debug("vpn does not exists " + ip);
            return;
        }
        
        _accountMgr.checkAccess(caller, vpn);
        
        Network network = _networkMgr.getNetwork(vpn.getNetworkId());
        
        vpn.setState(RemoteAccessVpn.State.Removed);
        _remoteAccessVpnDao.update(vpn.getServerAddress(), vpn);
        
        
        List<? extends RemoteAccessVpnElement> elements = _networkMgr.getRemoteAccessVpnElements();
        boolean success = false;
        try {
            for (RemoteAccessVpnElement element : elements) {
                if (element.stop(network, vpn)) {
                    success = true;
                    break;
                }
            }
        } finally {        
            if (success) {
                Transaction txn = Transaction.currentTxn();
                try {
                    txn.start();
                    _remoteAccessVpnDao.remove(ip);
                    
                    //Cleanup corresponding ports
                    List<FirewallRuleVO> ports = _rulesDao.listByIpAndPurpose(ip, Purpose.Vpn);
                    if (ports != null) {
                        for (FirewallRuleVO port : ports) {
                            _rulesDao.remove(port.getId());
                            s_logger.debug("Successfully removed firewall rule with ip " + port.getSourceIpAddress() + " and port " + port.getSourcePortStart() + " as a part of vpn cleanup");
                        }
                    }
                    txn.commit();   
                } catch (Exception ex) {
                    txn.rollback();
                    s_logger.warn("Unable to release the three vpn ports from the firewall rules", ex);
                }
            }
        }
    }

    @Override
    @DB
    public VpnUser addVpnUser(long vpnOwnerId, String username, String password) {
        long callerId = UserContext.current().getCallerUserId();
        Account caller = UserContext.current().getCaller();

        if (!username.matches("^[a-zA-Z0-9][a-zA-Z0-9@._-]{2,63}$")) {
            throw new InvalidParameterValueException(
                    "Username has to be begin with an alphabet have 3-64 characters including alphabets, numbers and the set '@.-_'");
        }
        if (!password.matches("^[a-zA-Z0-9][a-zA-Z0-9@#+=._-]{2,31}$")) {
            throw new InvalidParameterValueException("Password has to be 3-32 characters including alphabets, numbers and the set '@#+=.-_'");
        }
        Transaction txn = Transaction.currentTxn();
        txn.start();
        Account owner = _accountDao.lockRow(vpnOwnerId, true);
        if (owner == null) {
            throw new InvalidParameterValueException("Unable to add vpn user: Another operation active");
        }
        _accountMgr.checkAccess(caller, owner);

        long userCount = _vpnUsersDao.getVpnUserCount(owner.getId());
        if (userCount >= _userLimit) {
            throw new AccountLimitException("Cannot add more than " + _userLimit + " remote access vpn users");
        }

        VpnUser user = _vpnUsersDao.persist(new VpnUserVO(vpnOwnerId, owner.getDomainId(), username, password));
        EventUtils.saveEvent(callerId, owner.getId(), EventTypes.EVENT_VPN_USER_ADD, "Added a VPN user for account: " + owner.getAccountName()
                + " username= " + username);
        txn.commit();
        return user;
    }

    @Override
    public boolean removeVpnUser(long vpnOwnerId, String username) {
        long callerId = UserContext.current().getCallerUserId();
        Account caller = UserContext.current().getCaller();

        VpnUserVO user = _vpnUsersDao.findByAccountAndUsername(vpnOwnerId, username);
        if (user == null) {
            throw new InvalidParameterValueException("Could not find vpn user " + username);
        }
        _accountMgr.checkAccess(caller, user);

        user.setState(State.Revoke);
        _vpnUsersDao.update(user.getId(), user);
        EventUtils.saveEvent(callerId, vpnOwnerId, EventTypes.EVENT_VPN_USER_REMOVE, "Removed a VPN user username= " + username);
        return true;
    }

    @Override
    public List<? extends VpnUser> listVpnUsers(long vpnOwnerId, String userName) {
        Account caller = UserContext.current().getCaller();
        Account owner = _accountDao.findById(vpnOwnerId);
        _accountMgr.checkAccess(caller, owner);
        return _vpnUsersDao.listByAccount(vpnOwnerId);
    }

    @Override
    public RemoteAccessVpnVO startRemoteAccessVpn(Ip vpnId) throws ResourceUnavailableException {
        Account caller = UserContext.current().getCaller();

        RemoteAccessVpnVO vpn = _remoteAccessVpnDao.findById(vpnId);
        if (vpn == null) {
            throw new InvalidParameterValueException("Unable to find your vpn: " + vpnId);
        }

        _accountMgr.checkAccess(caller, vpn);

        Network network = _networkMgr.getNetwork(vpn.getNetworkId());

        List<? extends RemoteAccessVpnElement> elements = _networkMgr.getRemoteAccessVpnElements();
        boolean started = false;
        try {
            for (RemoteAccessVpnElement element : elements) {
                if (element.start(network, vpn)) {
                    started = true;
                    break;
                }
            }
            return vpn;
        } finally {
            if (started) {
                vpn.setState(RemoteAccessVpn.State.Running);
                _remoteAccessVpnDao.update(vpn.getServerAddress(), vpn);
            } 
        }
    }

    @DB
    @Override
    public boolean applyVpnUsers(long vpnOwnerId) {
        Account caller = UserContext.current().getCaller();
        Account owner = _accountDao.findById(vpnOwnerId);
        _accountMgr.checkAccess(caller, owner);

        s_logger.debug("Applying vpn users for " + owner);
        List<RemoteAccessVpnVO> vpns = _remoteAccessVpnDao.findByAccount(vpnOwnerId);

        List<VpnUserVO> users = _vpnUsersDao.listByAccount(vpnOwnerId);
        
        //If user is in Active state, we still have to resend them therefore their status has to be Add
        for (VpnUserVO user : users) {
            if (user.getState() == State.Active) {
                user.setState(State.Add);
                _vpnUsersDao.update(user.getId(), user);
            }
        }
        
        List<? extends RemoteAccessVpnElement> elements = _networkMgr.getRemoteAccessVpnElements();

        boolean success = true;

        boolean[] finals = new boolean[users.size()];
        for (RemoteAccessVpnElement element : elements) {
            s_logger.debug("Applying vpn access to " + element.getName());
            for (RemoteAccessVpnVO vpn : vpns) {
                try {
                    String[] results = element.applyVpnUsers(vpn, users);
                    if (results != null) {
                        for (int i = 0; i < results.length; i++) {
                            s_logger.debug("VPN User " + users.get(i)
                                    + (results[i] == null ? " is set on " : (" couldn't be set due to " + results[i]) + " on ") + vpn);
                            if (results[i] == null) {
                                if (!finals[i]) {
                                    finals[i] = true;
                                }
                            } else {
                                finals[i] = false;
                                success = false;
                            }
                        }
                    }
                } catch (ResourceUnavailableException e) {
                    s_logger.warn("Unable to apply vpn users ", e);
                    success= false;
                    
                    for (int i = 0; i < finals.length; i++) {
                        finals[i] = false;
                    }
                }
            }
        }

        for (int i = 0; i < finals.length; i++) {
            VpnUserVO user = users.get(i);
            if (finals[i]) {     
                if (user.getState() == State.Add) {
                    user.setState(State.Active);
                    _vpnUsersDao.update(user.getId(), user);
                } else if (user.getState() == State.Revoke) {
                    _vpnUsersDao.remove(user.getId());
                }
            } else {
                s_logger.warn("Failed to apply vpn for user " + user.getUsername() + ", accountId=" + user.getAccountId());
            }
        }

        return success;
    }

    @Override
    public List<VpnUserVO> searchForVpnUsers(ListVpnUsersCmd cmd) {
        Account account = UserContext.current().getCaller();
        String accountName = cmd.getAccountName();
        Long domainId = cmd.getDomainId();
        Long accountId = null;
        String username = cmd.getUsername();
        
        //Verify account information
        if (account.getType() == Account.ACCOUNT_TYPE_ADMIN) {
            if (domainId != null) {
                if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                    throw new PermissionDeniedException("Invalid domain id (" + domainId + ") given, unable to list virtual machines.");
                }

                if (accountName != null) {
                    account = _accountDao.findActiveAccount(accountName, domainId);
                    if (account == null) {
                        throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
                    }
                    accountId = account.getId();
                }
            } 
            if (account.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) {
                DomainVO domain = _domainDao.findById(account.getDomainId());
                if (domain != null) {
                   domainId = domain.getId();
                }
            }
        } else {
            accountId = account.getId();
        }

        Filter searchFilter = new Filter(VpnUserVO.class, "username", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        Object id = cmd.getId();

        SearchBuilder<VpnUserVO> sb = _vpnUsersDao.createSearchBuilder();
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("username", sb.entity().getUsername(), SearchCriteria.Op.EQ);
        sb.and("accountId", sb.entity().getAccountId(), SearchCriteria.Op.EQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);

        if ((accountId == null) && (domainId != null)) {
            // if accountId isn't specified, we can do a domain match for the
            // admin case
            SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
            sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        }

        SearchCriteria<VpnUserVO> sc = sb.create();
        
        //list only active users
        sc.setParameters("state", State.Active);

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (username != null) {
            sc.setParameters("username", username);
        }

        if (accountId != null) {
            sc.setParameters("accountId", accountId);
        } else if (domainId != null) {
            DomainVO domain = _domainDao.findById(domainId);
            sc.setJoinParameters("domainSearch", "path", domain.getPath() + "%");
        }

        return _vpnUsersDao.search(sc, searchFilter);
    }

    @Override
    public List<RemoteAccessVpnVO> searchForRemoteAccessVpns(ListRemoteAccessVpnsCmd cmd) {
        // do some parameter validation
        Account caller = UserContext.current().getCaller();
        String accountName = cmd.getAccountName();
        Long domainId = cmd.getDomainId();

        Ip ipAddress = cmd.getPublicIp();
        if (ipAddress != null) {
            PublicIpAddress publicIp = _networkMgr.getPublicIpAddress(ipAddress);
            if (publicIp == null) {
                throw new InvalidParameterValueException("Unable to list remote access vpns, IP address " + ipAddress + " not found.");
            } else {
                Long ipAddrAcctId = publicIp.getAllocatedToAccountId();
                if (ipAddrAcctId == null) {
                    throw new InvalidParameterValueException("Unable to list remote access vpns, IP address " + ipAddress
                            + " is not associated with an account.");
                }
            }
            _accountMgr.checkAccess(caller, publicIp);

            List<RemoteAccessVpnVO> vpns = new ArrayList<RemoteAccessVpnVO>(1);
            RemoteAccessVpnVO remoteVpn = _remoteAccessVpnDao.findById(ipAddress);
            if (remoteVpn != null) {
            	vpns.add(remoteVpn);
            }
            return vpns;
        }

        Account owner = null;
        if (accountName != null) {
            owner = _accountDao.findAccount(accountName, domainId);
        }
        _accountMgr.checkAccess(caller, owner);

        Filter searchFilter = new Filter(RemoteAccessVpnVO.class, "serverAddress", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        SearchCriteria<RemoteAccessVpnVO> sc = VpnSearch.create();

        sc.setParameters("accountId", owner.getId());
        sc.setParameters("state", RemoteAccessVpn.State.Running);
        DomainVO domain = _domainDao.findById(domainId);
        sc.setJoinParameters("domainSearch", "path", domain.getPath() + "%");

        return _remoteAccessVpnDao.search(sc, searchFilter);
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        Map<String, String> configs = configDao.getConfiguration(params);

        _userLimit = NumbersUtil.parseInt(configs.get(Config.RemoteAccessVpnUserLimit.key()), 8);

        _clientIpRange = configs.get(Config.RemoteAccessVpnClientIpRange.key());

        _pskLength = NumbersUtil.parseInt(configs.get(Config.RemoteAccessVpnPskLength.key()), 24);

        validateRemoteAccessVpnConfiguration();

        VpnSearch = _remoteAccessVpnDao.createSearchBuilder();
        VpnSearch.and("accountId", VpnSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
        domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
        VpnSearch.join("domainSearch", domainSearch, VpnSearch.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        VpnSearch.done();

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

}
