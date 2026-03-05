FROM ubuntu:latest
LABEL authors="mrs"

ENTRYPOINT ["top", "-b"]