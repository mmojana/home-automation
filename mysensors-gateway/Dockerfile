FROM balenalib/armv7hf-debian:buster-run

WORKDIR /tmp

RUN apt-get update && \
  apt-get install -y git make gcc g++ netcat && \ 
  git clone https://github.com/mysensors/MySensors.git --branch master && \
  cd MySensors && \
  ./configure --extra-cflags="-DMY_RFM69_RST_PIN=11" --extra-cxxflags="-DMY_RFM69_RST_PIN=11" --my-transport=rfm69 --my-rfm69-frequency=433 --my-is-rfm69hw --my-rfm69-cs-pin=24 --my-rfm69-irq-pin=22 --my-rfm69-encryption-enabled --soc=BCM2837 && \
  make && \
  make all install-gateway clean && \
  apt-get purge --auto-remove git make gcc g++ && \
  cd .. && \
  rm -rf /tmp/MySensors

COPY mysensors.conf /etc/mysensors.conf

EXPOSE 5003

HEALTHCHECK CMD nc -z localhost 5003

ENTRYPOINT ["/usr/local/bin/mysgw"]
