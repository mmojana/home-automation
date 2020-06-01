FROM balenalib/armv7hf-debian:buster-run

WORKDIR /tmp

RUN apt-get update && \
  apt-get install -y git make gcc g++ gdb netcat && \ 
  git clone https://github.com/mysensors/MySensors.git --branch master && \
  cd MySensors && \
  ./configure --extra-cflags="-DMY_RFM69_RST_PIN=11 -DMY_DEBUG_VERBOSE_RFM69 -g" --extra-cxxflags="-DMY_RFM69_RST_PIN=11 -DMY_DEBUG_VERBOSE_RFM69 -g" --my-transport=rfm69 --my-rfm69-frequency=433 --my-is-rfm69hw --my-rfm69-cs-pin=24 --my-rfm69-irq-pin=22 --my-rfm69-encryption-enabled --soc=BCM2837 && \
  make && \
  make all install-gateway clean && \
  git reflog --decorate -1 > /tmp/version.txt && \
#  apt-get purge --auto-remove git make gcc g++ && \
  cd .. && \
  rm -rf /tmp/MySensors

COPY mysensors.conf /etc/mysensors.conf
COPY gdbcmds /tmp
COPY entrypoint.sh /tmp

RUN chmod u+x /tmp/entrypoint.sh

EXPOSE 5003

ENTRYPOINT ["/tmp/entrypoint.sh"]

HEALTHCHECK CMD nc -z localhost 5003

CMD ["/usr/local/bin/mysgw"]
