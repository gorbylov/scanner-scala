# Dockerfile to build the application with all needed technologies stack
FROM java:8

MAINTAINER Yurii Gorbylov <yuriygorbylov@gmail.com>

ENV SBT_VERSION     0.13.15
ENV SBT_TAR         sbt-$SBT_VERSION.tgz
ENV SBT_TAR_URL     https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/$SBT_TAR
ENV SBT_HOME        /opt/sbt
ENV APP_HOME        /opt/app

RUN mkdir $SBT_HOME
RUN mkdir $APP_HOME

# Getting sbt
RUN wget $SBT_TAR_URL
RUN tar -xvf $SBT_TAR
RUN mv /sbt /opt
RUN bash $SBT_HOME/bin/sbt

VOLUME $APP_HOME
WORKDIR $APP_HOME

# The command which is needed to prevent container shutdown
CMD tail -f /dev/null

# To build an image:
# docker build -t ${image_name} .
#
# To create and run a container
# docker run --name ${container_name} -d -v ${host_path}:/opt/app ${image_name}