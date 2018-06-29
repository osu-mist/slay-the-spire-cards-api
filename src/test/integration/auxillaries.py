import local_vars
import argparse
import requests
from requests.auth import HTTPBasicAuth


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-i', help='path to input file', dest='inputfile')
    namespace, args = parser.parse_known_args()
    return parser.parse_known_args()


def get_card_by_id(id):
    return requests.get(url=local_vars.url+str(id),
                        auth=HTTPBasicAuth(local_vars.user, local_vars.passw),
                        verify=False)


def get_cards(params):
    return requests.get(url=local_vars.url,
                        auth=HTTPBasicAuth(local_vars.user, local_vars.passw),
                        params=params,
                        verify=False)


def post_card(body):
    return requests.post(url=local_vars.url,
                         auth=HTTPBasicAuth(local_vars.user, local_vars.passw),
                         json=body,
                         verify=False)


def put_card(id, body):
    return requests.put(url=local_vars.url+str(id),
                        auth=HTTPBasicAuth(local_vars.user, local_vars.passw),
                        json=body,
                        verify=False)


def delete_card(id):
    return requests.delete(url=local_vars.url+str(id),
                           auth=HTTPBasicAuth(local_vars.user,
                                              local_vars.passw),
                           verify=False)
