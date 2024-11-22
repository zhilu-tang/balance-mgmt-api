FROM ubuntu:latest
LABEL authors="markus"

ENTRYPOINT ["top", "-b"]