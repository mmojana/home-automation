#!/usr/bin/env bash
set -e

echo "Setting AES key to config..."
AES_KEY=`head -n 1 "${AES_KEY_FILE}"`

dd if=/dev/zero bs=1024 count=1 | tr "\000" "\377" > /etc/mysensors.eeprom
echo -n -e `echo ${AES_KEY} | sed 's/\([0-9A-F]\{2\}\)/\\\\x\1/g'` | dd of=/etc/mysensors.eeprom bs=1 seek=396 count=16 conv=notrunc

sed -i "s/^aes_key=.*$/aes_key=${AES_KEY}/g" /etc/mysensors.conf

echo "Enabling coredump..."
ulimit -c unlimited
ulimit -a
echo "Setting coredump format..."
sysctl -w kernel.core_pattern=/tmp/core.%e.%p

echo "Starting MySensors gateway..."
cat /tmp/version.txt
exec /usr/bin/gdb -batch -x /tmp/gdbcmds --args $@
