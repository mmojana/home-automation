FROM balenalib/armv7hf-debian:buster-run

WORKDIR /tmp

RUN apt-get update && \
  apt-get install -y wget make gcc g++ libssl-dev libc6-dev && \
  wget https://mosquitto.org/files/source/mosquitto-1.6.7.tar.gz && \
  tar -xvzf mosquitto-1.6.7.tar.gz && \
  cd mosquitto-1.6.7 && \
  make install DIRS="lib src" && \
  cd .. && \
  rm -rf mosquitto-1.6.7 && \
  rm mosquitto-1.6.7.tar.gz

COPY mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY entrypoint.sh /tmp

RUN chmod u+x /tmp/entrypoint.sh

EXPOSE 1883

ENTRYPOINT ["/tmp/entrypoint.sh"]

CMD ["/usr/local/sbin/mosquitto", "-c", "/etc/mosquitto/mosquitto.conf"]
