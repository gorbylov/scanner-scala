FROM java:8

ENV SBT_VERSION     0.13.13
ENV SBT_JAR         https://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$SBT_VERSION/sbt-launch.jar
ENV SBT_HOME        /opt/sbt
ENV APP_HOME        /opt/app

RUN mkdir $SBT_HOME
RUN mkdir $APP_HOME

ADD $SBT_JAR $SBT_HOME
COPY . $APP_HOME