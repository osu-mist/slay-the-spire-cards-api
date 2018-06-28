def init(config):
    global url
    url = config["hostname"] + config["version"] + config["api"]
    global user
    user = config["username"]
    global passw
    passw = config["password"]
