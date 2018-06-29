FROM python:3

WORKDIR /usr/src/app

COPY . .
RUN pip3 install -r requirements.txt

USER nobody:nogroup
ENTRYPOINT [ "python3" ]
CMD [ "integration-tests.py", "-v", "-i", "configuration.json" ]
