%define __os_install_post %{nil}
%global debug_package %{nil}

# DISABLE the post-percentinstall java repacking and line number stripping
# we need to find a way to just disable the java repacking and line number stripping, but not the autodeps

%define _ver 2.1.97
%define _rel 1

Name:      cloud
Summary:   Cloud.com Stack
Version:   %{_ver}
#http://fedoraproject.org/wiki/PackageNamingGuidelines#Pre-Release_packages
%if "%{?_prerelease}" != ""
Release:   0.%{_build_number}%{_prerelease}
%else
Release:   %{_rel}
%endif
License:   GPLv3+ with exceptions or CSL 1.1
Vendor:    Cloud.com, Inc. <sqa@cloud.com>
Packager:  Manuel Amador (Rudd-O) <manuel@cloud.com>
Group:     System Environment/Libraries
# FIXME do groups for every single one of the subpackages
Source0:   %{name}-%{_ver}.tar.bz2
BuildRoot: %{_tmppath}/%{name}-%{_ver}-%{release}-build

BuildRequires: java-1.6.0-openjdk-devel
BuildRequires: tomcat6
BuildRequires: ws-commons-util
#BuildRequires: commons-codec
BuildRequires: commons-dbcp
BuildRequires: commons-collections
BuildRequires: commons-httpclient
BuildRequires: jpackage-utils
BuildRequires: gcc
BuildRequires: glibc-devel
BuildRequires: /usr/bin/mkisofs
BuildRequires: MySQL-python

%global _premium %(tar jtvmf %{SOURCE0} '*/cloudstack-proprietary/' --occurrence=1 2>/dev/null | wc -l)

%description
This is the Cloud.com Stack, a highly-scalable elastic, open source,
intelligent cloud implementation.

%package utils
Summary:   Cloud.com utility library
Requires: java >= 1.6.0
Requires: python
Group:     System Environment/Libraries
Obsoletes: vmops-utils < %{version}-%{release}
%description utils
The Cloud.com utility libraries provide a set of Java classes used
in the Cloud.com Stack.

%package client-ui
Summary:   Cloud.com management server UI
Requires: %{name}-client
Group:     System Environment/Libraries
Obsoletes: vmops-client-ui < %{version}-%{release}
%description client-ui
The Cloud.com management server is the central point of coordination,
management, and intelligence in the Cloud.com Stack.  This package
is a requirement of the %{name}-client package, which installs the
Cloud.com management server.

%package server
Summary:   Cloud.com server library
Requires: java >= 1.6.0
Obsoletes: vmops-server < %{version}-%{release}
Requires: %{name}-utils = %{version}-%{release}, %{name}-core = %{version}-%{release}, %{name}-deps = %{version}-%{release}, tomcat6-servlet-2.5-api
Group:     System Environment/Libraries
%description server
The Cloud.com server libraries provide a set of Java classes used
in the Cloud.com Stack.

%package agent-scripts
Summary:   Cloud.com agent scripts
# FIXME nuke the archdependency
Requires: python
Requires: bash
Requires: bzip2
Requires: gzip
Requires: unzip
Requires: /sbin/mount.nfs
Requires: openssh-clients
Requires: nfs-utils
Obsoletes: vmops-agent-scripts < %{version}-%{release}
Group:     System Environment/Libraries
%description agent-scripts
The Cloud.com agent is in charge of managing shared computing resources in
a Cloud.com Stack-powered cloud.  Install this package if this computer
will participate in your cloud -- this is a requirement for the Cloud.com
agent.

%package python
Summary:   Cloud.com Python library
# FIXME nuke the archdependency
Requires: python
Group:     System Environment/Libraries
%description python
The Cloud.com Python library contains a few Python modules that the
CloudStack uses.

%package deps
Summary:   Cloud.com library dependencies
Requires: java >= 1.6.0
Obsoletes: vmops-deps < %{version}-%{release}
Group:     System Environment/Libraries
%description deps
This package contains a number of third-party dependencies
not shipped by distributions, required to run the Cloud.com
Stack.

%package daemonize
Summary:   Cloud.com daemonization utility
Group:     System Environment/Libraries
Obsoletes: vmops-daemonize < %{version}-%{release}
%description daemonize
This package contains a program that daemonizes the specified
process.  The Cloud.com Cloud Stack uses this to start the agent
as a service.

%package core
Summary:   Cloud.com core library
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}, %{name}-deps = %{version}-%{release}
Group:     System Environment/Libraries
Obsoletes: vmops-core < %{version}-%{release}
%description core
The Cloud.com core libraries provide a set of Java classes used
in the Cloud.com Stack.

%package client
Summary:   Cloud.com management server
# If GCJ is present, a setPerformanceSomething method fails to load Catalina
Conflicts: java-1.5.0-gcj-devel
Obsoletes: vmops-client < %{version}-%{release}
Requires: java >= 1.6.0
Requires: %{name}-deps = %{version}-%{release}, %{name}-utils = %{version}-%{release}, %{name}-server = %{version}-%{release}
Requires: %{name}-client-ui = %{version}-%{release}
Requires: %{name}-setup = %{version}-%{release}
# reqs the agent-scripts package because of xenserver within the management server
Requires: %{name}-agent-scripts = %{version}-%{release}
Requires: %{name}-python = %{version}-%{release}
# for consoleproxy
# Requires: %{name}-agent
Requires: tomcat6
Requires: ws-commons-util
#Requires: commons-codec
Requires: commons-dbcp
Requires: commons-collections
Requires: commons-httpclient
Requires: jpackage-utils
Requires: sudo
Requires: /sbin/service
Requires: /sbin/chkconfig
Requires: /usr/bin/ssh-keygen
Requires: MySQL-python
Requires: python-paramiko
Requires: augeas >= 0.7.1
Group:     System Environment/Libraries
%description client
The Cloud.com management server is the central point of coordination,
management, and intelligence in the Cloud.com Stack.  This package
installs the management server..

%package setup
Summary:   Cloud.com setup tools
Obsoletes: vmops-setup < %{version}-%{release}
Requires: java >= 1.6.0
Requires: python
Requires: MySQL-python
Requires: %{name}-utils = %{version}-%{release}
Requires: %{name}-server = %{version}-%{release}
Requires: %{name}-deps = %{version}-%{release}
Requires: %{name}-python = %{version}-%{release}
Group:     System Environment/Libraries
%description setup
The Cloud.com setup tools let you set up your Management Server and Usage Server.

%package agent-libs
Summary:   Cloud.com agent libraries
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}, %{name}-core = %{version}-%{release}, %{name}-deps = %{version}-%{release}
Requires: commons-httpclient
#Requires: commons-codec
Requires: commons-collections
Requires: commons-pool
Requires: commons-dbcp
Requires: jakarta-commons-logging
Requires: jpackage-utils
Group:     System Environment/Libraries
%description agent-libs
The Cloud.com agent libraries are used by the Cloud Agent and the Cloud
Console Proxy.

%package agent
Summary:   Cloud.com agent
Obsoletes: vmops-agent < %{version}-%{release}
Obsoletes: vmops-console < %{version}-%{release}
Obsoletes: cloud-console < %{version}-%{release}
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}, %{name}-core = %{version}-%{release}, %{name}-deps = %{version}-%{release}
Requires: %{name}-agent-libs = %{version}-%{release}
Requires: %{name}-agent-scripts = %{version}-%{release}
Requires: python
Requires: %{name}-python = %{version}-%{release}
Requires: commons-httpclient
#Requires: commons-codec
Requires: commons-collections
Requires: commons-pool
Requires: commons-dbcp
Requires: jakarta-commons-logging
Requires: libvirt
Requires: /usr/sbin/libvirtd
Requires: jpackage-utils
Requires: %{name}-daemonize
Requires: /sbin/service
Requires: /sbin/chkconfig
Requires: kvm
%if 0%{?fedora} >= 12
Requires: qemu-cloud-system-x86
Requires: qemu-cloud-img
%endif
Requires: libcgroup
Requires: /usr/bin/uuidgen
Requires: augeas >= 0.7.1
Requires: rsync
Requires: /bin/egrep
Requires: /sbin/ip
Requires: vconfig
Group:     System Environment/Libraries
%description agent
The Cloud.com agent is in charge of managing shared computing resources in
a Cloud.com Stack-powered cloud.  Install this package if this computer
will participate in your cloud.

%package console-proxy
Summary:   Cloud.com console proxy
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}, %{name}-core = %{version}-%{release}, %{name}-deps = %{version}-%{release}, %{name}-agent-libs = %{version}-%{release}
Requires: python
Requires: %{name}-python = %{version}-%{release}
Requires: commons-httpclient
#Requires: commons-codec
Requires: commons-collections
Requires: commons-pool
Requires: commons-dbcp
Requires: jakarta-commons-logging
Requires: jpackage-utils
Requires: %{name}-daemonize
Requires: /sbin/service
Requires: /sbin/chkconfig
Requires: /usr/bin/uuidgen
Requires: augeas >= 0.7.1
Requires: /bin/egrep
Requires: /sbin/ip
Group:     System Environment/Libraries
%description console-proxy
The Cloud.com console proxy is the service in charge of granting console
access into virtual machines managed by the Cloud.com CloudStack.

%package cli
Summary:   Cloud.com command line tools
Requires: python
Group:     System Environment/Libraries
%description cli
The Cloud.com command line tools contain a few Python modules that can call cloudStack APIs.


%if %{_premium}

%package test
Summary:   Cloud.com test suite
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}, %{name}-deps = %{version}-%{release}, wget
Group:     System Environment/Libraries
Obsoletes: vmops-test < %{version}-%{release}
%description test
The Cloud.com test package contains a suite of automated tests
that the very much appreciated QA team at Cloud.com constantly
uses to help increase the quality of the Cloud.com Stack.

%package premium-deps
Summary:   Cloud.com premium library dependencies
Requires: java >= 1.6.0
Provides: %{name}-deps = %{version}-%{release}
Group:     System Environment/Libraries
Obsoletes: vmops-premium-deps < %{version}-%{release}
%description premium-deps
This package contains the certified software components required to run
the premium edition of the Cloud.com Stack.

%package premium
Summary:   Cloud.com premium components
Obsoletes: vmops-premium < %{version}-%{release}
Provides: %{name}-premium-plugin-zynga = %{version}-%{release}
Obsoletes: %{name}-premium-plugin-zynga < %{version}-%{release}
Provides: %{name}-premium-vendor-zynga = %{version}-%{release}
Obsoletes: %{name}-premium-vendor-zynga < %{version}-%{release}
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}
Requires: %{name}-premium-deps
License:   CSL 1.1
Group:     System Environment/Libraries
%description premium
The Cloud.com premium components expand the range of features on your Cloud.com Stack.

%package usage
Summary:   Cloud.com usage monitor
Obsoletes: vmops-usage < %{version}-%{release}
Requires: java >= 1.6.0
Requires: %{name}-utils = %{version}-%{release}, %{name}-core = %{version}-%{release}, %{name}-deps = %{version}-%{release}, %{name}-server = %{version}-%{release}, %{name}-premium = %{version}-%{release}, %{name}-daemonize = %{version}-%{release}
Requires: %{name}-setup = %{version}-%{release}
Requires: %{name}-client = %{version}-%{release}
License:   CSL 1.1
Group:     System Environment/Libraries
%description usage
The Cloud.com usage monitor provides usage accounting across the entire cloud for
cloud operators to charge based on usage parameters.

%endif

%prep

%if %{_premium}
echo Doing premium build
%else
echo Doing open source build
%endif

%setup -q -n %{name}-%{_ver}

%build

# this fixes the /usr/com bug on centos5
%define _localstatedir /var
%define _sharedstatedir /var/lib
./waf configure --prefix=%{_prefix} --libdir=%{_libdir} --bindir=%{_bindir} --javadir=%{_javadir} --sharedstatedir=%{_sharedstatedir} --localstatedir=%{_localstatedir} --sysconfdir=%{_sysconfdir} --mandir=%{_mandir} --docdir=%{_docdir}/%{name}-%{version} --with-tomcat=%{_datadir}/tomcat6 --tomcat-user=%{name} --fast --build-number=%{_ver}-%{release}
./waf build --build-number=%{?_build_number}

%install
[ ${RPM_BUILD_ROOT} != "/" ] && rm -rf ${RPM_BUILD_ROOT}
# we put the build number again here, otherwise state checking will cause an almost-full recompile
./waf install --destdir=$RPM_BUILD_ROOT --nochown --build-number=%{?_build_number}

%clean

#[ ${RPM_BUILD_ROOT} != "/" ] && rm -rf ${RPM_BUILD_ROOT}


%preun client
/sbin/service %{name}-management stop || true
if [ "$1" == "0" ] ; then
    /sbin/chkconfig --del %{name}-management  > /dev/null 2>&1 || true
    /sbin/service %{name}-management stop > /dev/null 2>&1 || true
fi

%pre client
id %{name} > /dev/null 2>&1 || /usr/sbin/useradd -M -c "Cloud.com unprivileged user" \
     -r -s /bin/sh -d %{_sharedstatedir}/%{name}/management %{name}|| true
rm -rf %{_localstatedir}/cache/%{name}
# user harcoded here, also hardcoded on wscript

%post client
if [ "$1" == "1" ] ; then
    /sbin/chkconfig --add %{name}-management > /dev/null 2>&1 || true
    /sbin/chkconfig --level 345 %{name}-management on > /dev/null 2>&1 || true
fi



%if %{_premium}

%preun usage
if [ "$1" == "0" ] ; then
    /sbin/chkconfig --del %{name}-usage  > /dev/null 2>&1 || true
    /sbin/service %{name}-usage stop > /dev/null 2>&1 || true
fi

%pre usage
id %{name} > /dev/null 2>&1 || /usr/sbin/useradd -M -c "Cloud.com unprivileged user" \
     -r -s /bin/sh -d %{_sharedstatedir}/%{name}/management %{name}|| true
# user harcoded here, also hardcoded on wscript

%post usage
if [ "$1" == "1" ] ; then
    /sbin/chkconfig --add %{name}-usage > /dev/null 2>&1 || true
    /sbin/chkconfig --level 345 %{name}-usage on > /dev/null 2>&1 || true
else
    /sbin/service %{name}-usage condrestart >/dev/null 2>&1 || true
fi

%endif

%pre agent-scripts
id %{name} > /dev/null 2>&1 || /usr/sbin/useradd -M -c "Cloud.com unprivileged user" \
     -r -s /bin/sh -d %{_sharedstatedir}/%{name}/management %{name}|| true


%preun agent
if [ "$1" == "0" ] ; then
    /sbin/chkconfig --del %{name}-agent  > /dev/null 2>&1 || true
    /sbin/service %{name}-agent stop > /dev/null 2>&1 || true
fi

%post agent
if [ "$1" == "1" ] ; then
    /sbin/chkconfig --add %{name}-agent > /dev/null 2>&1 || true
    /sbin/chkconfig --level 345 %{name}-agent on > /dev/null 2>&1 || true
else
    /sbin/service %{name}-agent condrestart >/dev/null 2>&1 || true
fi

%preun console-proxy
if [ "$1" == "0" ] ; then
    /sbin/chkconfig --del %{name}-console-proxy  > /dev/null 2>&1 || true
    /sbin/service %{name}-console-proxy stop > /dev/null 2>&1 || true
fi

%post console-proxy
if [ "$1" == "1" ] ; then
    /sbin/chkconfig --add %{name}-console-proxy > /dev/null 2>&1 || true
    /sbin/chkconfig --level 345 %{name}-console-proxy on > /dev/null 2>&1 || true
else
    /sbin/service %{name}-console-proxy condrestart >/dev/null 2>&1 || true
fi

%files utils
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-utils.jar
%{_javadir}/%{name}-api.jar
%attr(755,root,root) %{_bindir}/cloud-sccs
%attr(755,root,root) %{_bindir}/cloud-gitrevs
%doc %{_docdir}/%{name}-%{version}/sccs-info
%doc %{_docdir}/%{name}-%{version}/version-info
%doc %{_docdir}/%{name}-%{version}/configure-info
%doc README.html
%doc debian/copyright

%files client-ui
%defattr(0644,root,root,0755)
%{_datadir}/%{name}/management/webapps/client/*

%files server
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-server.jar
%{_sysconfdir}/%{name}/server/*

%files agent-scripts
%defattr(-,root,root,-)
%{_libdir}/%{name}/agent/scripts/*
# maintain the following list in sync with files agent-scripts
%{_libdir}/%{name}/agent/vms/systemvm.zip
%{_libdir}/%{name}/agent/vms/systemvm.iso

%files daemonize
%defattr(-,root,root,-)
%attr(755,root,root) %{_bindir}/%{name}-daemonize

%files deps
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-commons-codec-1.4.jar
%{_javadir}/%{name}-apache-log4j-extras-1.0.jar
%{_javadir}/%{name}-backport-util-concurrent-3.0.jar
%{_javadir}/%{name}-ehcache.jar
%{_javadir}/%{name}-email.jar
%{_javadir}/%{name}-gson.jar
%{_javadir}/%{name}-httpcore-4.0.jar
%{_javadir}/%{name}-jna.jar
%{_javadir}/%{name}-junit-4.8.1.jar
%{_javadir}/%{name}-libvirt-0.4.5.jar
%{_javadir}/%{name}-log4j.jar
%{_javadir}/%{name}-trilead-ssh2-build213.jar
%{_javadir}/%{name}-cglib.jar
%{_javadir}/%{name}-mysql-connector-java-5.1.7-bin.jar
%{_javadir}/%{name}-xenserver-5.6.0-1.jar
%{_javadir}/%{name}-xmlrpc-common-3.*.jar
%{_javadir}/%{name}-xmlrpc-client-3.*.jar
%{_javadir}/%{name}-jstl-1.2.jar

%{_javadir}/%{name}-axis.jar
%{_javadir}/%{name}-commons-discovery.jar
%{_javadir}/%{name}-iControl.jar
%{_javadir}/%{name}-wsdl4j.jar
%{_javadir}/%{name}-bcprov-jdk16-1.45.jar
%{_javadir}/%{name}-jsch-0.1.42.jar


%files core
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-core.jar

%files python
%defattr(0644,root,root,0755)
%{_prefix}/lib*/python*/site-packages/%{name}*
%attr(0755,root,root) %{_bindir}/cloud-external-ipallocator.py
%attr(0755,root,root) %{_initrddir}/cloud-ipallocator
%dir %attr(770,root,root) %{_localstatedir}/log/%{name}/ipallocator

%files setup
%attr(0755,root,root) %{_bindir}/%{name}-setup-databases
%attr(0755,root,root) %{_bindir}/%{name}-migrate-databases
%dir %{_datadir}/%{name}/setup
%{_datadir}/%{name}/setup/*.sql
%{_datadir}/%{name}/setup/deploy-db-dev.sh
%{_datadir}/%{name}/setup/server-setup.xml

%files client
%defattr(0644,root,root,0755)
%{_sysconfdir}/%{name}/management/*
%if %{_premium}
%exclude %{_sysconfdir}/%{name}/management/*premium*
%endif
%config(noreplace) %attr(640,root,%{name}) %{_sysconfdir}/%{name}/management/db.properties
%config(noreplace) %{_sysconfdir}/%{name}/management/log4j-%{name}.xml
%config(noreplace) %{_sysconfdir}/%{name}/management/tomcat6.conf
%dir %attr(770,root,%{name}) %{_sysconfdir}/%{name}/management/Catalina
%dir %attr(770,root,%{name}) %{_sysconfdir}/%{name}/management/Catalina/localhost
%dir %attr(770,root,%{name}) %{_sysconfdir}/%{name}/management/Catalina/localhost/client
%config %{_sysconfdir}/sysconfig/%{name}-management
%attr(0755,root,root) %{_initrddir}/%{name}-management
%dir %{_datadir}/%{name}/management
%{_datadir}/%{name}/management/*
%attr(755,root,root) %{_bindir}/%{name}-setup-management
%attr(755,root,root) %{_bindir}/%{name}-update-xenserver-licenses
%dir %attr(770,root,%{name}) %{_sharedstatedir}/%{name}/mnt
%dir %attr(770,%{name},%{name}) %{_sharedstatedir}/%{name}/management
%dir %attr(770,root,%{name}) %{_localstatedir}/cache/%{name}/management
%dir %attr(770,root,%{name}) %{_localstatedir}/cache/%{name}/management/work
%dir %attr(770,root,%{name}) %{_localstatedir}/cache/%{name}/management/temp
%dir %attr(770,root,%{name}) %{_localstatedir}/log/%{name}/management
%dir %attr(770,root,%{name}) %{_localstatedir}/log/%{name}/agent

%files agent-libs
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-agent.jar

%files agent
%defattr(0644,root,root,0755)
%config(noreplace) %{_sysconfdir}/%{name}/agent/agent.properties
%config %{_sysconfdir}/%{name}/agent/developer.properties.template
%config %{_sysconfdir}/%{name}/agent/environment.properties
%config(noreplace) %{_sysconfdir}/%{name}/agent/log4j-%{name}.xml
%attr(0755,root,root) %{_initrddir}/%{name}-agent
%attr(0755,root,root) %{_libexecdir}/agent-runner
%{_libdir}/%{name}/agent/css
%{_libdir}/%{name}/agent/ui
%{_libdir}/%{name}/agent/js
%{_libdir}/%{name}/agent/images
%attr(0755,root,root) %{_bindir}/%{name}-setup-agent
%dir %attr(770,root,root) %{_localstatedir}/log/%{name}/agent


%files console-proxy
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-console*.jar
%config(noreplace) %{_sysconfdir}/%{name}/console-proxy/*
%attr(0755,root,root) %{_initrddir}/%{name}-console-proxy
%attr(0755,root,root) %{_libexecdir}/console-proxy-runner
%{_libdir}/%{name}/console-proxy/*
%attr(0755,root,root) %{_bindir}/%{name}-setup-console-proxy
%dir %attr(770,root,root) %{_localstatedir}/log/%{name}/console-proxy

%files cli
%{_bindir}/%{name}-tool
%{_sysconfdir}/%{name}/cli/commands.xml
%dir %{_prefix}/lib*/python*/site-packages/%{name}tool
%{_prefix}/lib*/python*/site-packages/%{name}tool/*
%{_prefix}/lib*/python*/site-packages/%{name}apis.py

%if %{_premium}

%files test
%defattr(0644,root,root,0755)
%attr(755,root,root) %{_bindir}/%{name}-run-test
%{_javadir}/%{name}-test.jar
%{_sharedstatedir}/%{name}/test/*
%{_libdir}/%{name}/test/*
%{_sysconfdir}/%{name}/test/*

%files premium-deps
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-premium/*.jar

%files premium
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-core-extras.jar
%{_javadir}/%{name}-server-extras.jar
%{_sysconfdir}/%{name}/management/commands-ext.properties
%{_sysconfdir}/%{name}/management/components-premium.xml
%{_libdir}/%{name}/agent/vms/systemvm-premium.iso
%{_datadir}/%{name}/setup/create-database-premium.sql
%{_datadir}/%{name}/setup/create-schema-premium.sql
# maintain the following list in sync with files agent-scripts
%{_libdir}/%{name}/agent/premium-scripts/*

%files usage
%defattr(0644,root,root,0755)
%{_javadir}/%{name}-usage.jar
%attr(0755,root,root) %{_initrddir}/%{name}-usage
%attr(0755,root,root) %{_libexecdir}/usage-runner
%dir %attr(770,root,%{name}) %{_localstatedir}/log/%{name}/usage
%{_sysconfdir}/%{name}/usage/usage-components.xml
%config(noreplace) %{_sysconfdir}/%{name}/usage/log4j-%{name}_usage.xml
%config(noreplace) %attr(640,root,%{name}) %{_sysconfdir}/%{name}/usage/db.properties

%endif

%changelog
* Mon May 3 2010 Manuel Amador (Rudd-O) <manuel@vmops.com> 1.9.12
- Bump version for RC4 release

%changelog
* Fri Apr 30 2010 Manuel Amador (Rudd-O) <manuel@vmops.com> 1.9.11
- Rename to Cloud.com everywhere

* Wed Apr 28 2010 Manuel Amador (Rudd-O) <manuel@vmops.com> 1.9.10
- FOSS release

%changelog
* Mon Apr 05 2010 Manuel Amador (Rudd-O) <manuel@vmops.com> 1.9.8
- RC3 branched

* Wed Feb 17 2010 Manuel Amador (Rudd-O) <manuel@vmops.com> 1.9.7
- First initial broken-up release

