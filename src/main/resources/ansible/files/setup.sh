#!/usr/bin/env bash
# for ssds
echo deadline > /sys/block/sda/queue/scheduler
echo 0 > /sys/block/sda/queue/rotational
echo 0 > /proc/sys/vm/zone_reclaim_mode

# readadhead to 8
blockdev --setra 8 /dev/sda

# swap off
swapoff --all
sudo sed -i.bak '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# /etc/security/limits.conf
grep -q -F '* - memlock unlimited' /etc/security/limits.conf || echo '* - memlock unlimited' >> /etc/security/limits.conf
grep -q -F '* - nofile 100000' /etc/security/limits.conf || echo '* - nofile 100000' >> /etc/security/limits.conf
grep -q -F '* - nproc 32768' /etc/security/limits.conf || echo '* - nproc 32768' >> /etc/security/limits.conf
grep -q -F '* - as unlimited' /etc/security/limits.conf || echo '* - as unlimited' >> /etc/security/limits.conf
grep -q -F 'root - memlock unlimited' /etc/security/limits.conf || echo 'root - memlock unlimited' >> /etc/security/limits.conf
grep -q -F 'root - nofile 100000' /etc/security/limits.conf || echo 'root - nofile 100000' >> /etc/security/limits.conf
grep -q -F 'root - nproc 32768' /etc/security/limits.conf || echo 'root - nproc 32768' >> /etc/security/limits.conf
grep -q -F 'root - as unlimited' /etc/security/limits.conf || echo 'root - as unlimited' >> /etc/security/limits.conf


# couch only
#/etc/rc.local
grep -q -F 'for i in /sys/kernel/mm/*transparent_hugepage/enabled; do echo never > $i; done' /etc/rc.local || echo 'for i in /sys/kernel/mm/*transparent_hugepage/enabled; do echo never > $i; done' >> /etc/rc.local
grep -q -F 'for i in /sys/kernel/mm/*transparent_hugepage/defrag; do echo never > $i; done' /etc/rc.local || echo 'for i in /sys/kernel/mm/*transparent_hugepage/defrag; do echo never > $i; done' >> /etc/rc.local
sed -e 's/exit 0//g' /etc/rc.local > /tmp/rc.local
echo 'exit 0' >> /tmp/rc.local
mv /tmp/rc.local /etc/rc.local


