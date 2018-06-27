import sys
import json
import argparse
import unittest
import requests
from requests.auth import HTTPBasicAuth

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-i', help='path to input file', dest='inputfile')
    namespace, args = parser.parse_known_args()
    print(namespace, args)
    return parser.parse_known_args()

def getCardById(id, url, user, passw):
    return requests.get(url=url+str(id),
                        auth=HTTPBasicAuth(user, passw),
                        verify=False)

def getCards(params, url, user, passw):
    return requests.get(url=url,
                        auth=HTTPBasicAuth(user, passw),
                        params=params,
                        verify=False)

def postCard(body, url, user, passw):
    return requests.post(url=url,
                        auth=HTTPBasicAuth(user, passw),
                        json=body,
                        verify=False)

def putCard(id, body, url, user, passw):
    return requests.put(url=url+str(id),
                        auth=HTTPBasicAuth(user, passw),
                        json=body,
                        verify=False)

def deleteCard(id, url, user, passw):
    return requests.delete(url=url+str(id),
                           auth=HTTPBasicAuth(user, passw),
                           verify=False)
