import socket
import os
import threading
import uuid
import hashlib
import time
import datetime

splash = """      ______________________________________________________
     / ___  __________________  _________  __  _____  ___  /
    / / _ )/  _/ __/_  __/ __ \/ ___/ __ \/  |/  /  |/  / /
   / / _  |/ /_\ \  / / / /_/ / /__/ /_/ / /|_/ / /|_/ / /
  / /____/___/___/ /_/  \____/\___/\____/_/  /_/_/  /_/ /
 /_____________________________________________________/
"""
directory = "data"
gcWait = 0.5
logLevel = 3
port = 5001

maxMessageLength = 256
maxUsernameLength = 64
maxConnections = 5
forbiddenUsernames = ["admin", "root"]
forbiddenPasswords = ["password", "123", "1234", "12345", "abc", "qwerty"]

properties = {
    "maxMessageLength": maxMessageLength,
    "maxUsernameLength": maxUsernameLength,
    "maxConnections": maxConnections,
    "forbiddenUsernames": ", ".join(forbiddenUsernames),
    "forbiddenPasswords": ", ".join(forbiddenPasswords),
}

codes = {
    200: {
        "status": "OK",
        "description": "The request completed successfully"            
    }
}

verbs = {
    "GET": {
        "syntax": "GET [username] [password] [from]",
        "description": "Fetches messages from the server",
        "explanation": "Authenticates as the user identified by [username] and [password], and returns a list of messages from the user specified by [from]"
    },
    "POST": {
        "syntax": "POST [username] [password] [recipient] [message]",
        "description": "Posts a message to the server",
        "explanation": "Authenticates as the user identified by [username] and [password], and sends [message] to [recipient]"
    },
    "DELETE": {
        "syntax": "DELETE [username] [password] [from]",
        "description": "Deletes a conversation",
        "explanation": "Deletes a conversation with the user specified by [from] from the account identified by [username] and [password]"
    },
    "REG": {
        "syntax": "REG [username] [password]",
        "description": "Registers a new account on the server",
        "explanation": "Registers a new user identified by [username] and [password]"
    },
    "DIR": {
        "syntax": "DIR",
        "description": "Fetches a list of all registered users",
        "explanation": "Returns a list containing the usernames of all registered users on separate lines"
    },
    "HELP": {
        "syntax": "HELP | HELP [verb]",
        "description": "Fetches general help or help specific to a verb",
        "explanation": "Returns a brief synopsis of all available verbs or shows all help for [verb]"
    },
    "SPLASH": {
        "syntax": "SPLASH",
        "description": "Fetches the server splash screen",
        "explanation": "Returns the server's welcome screen"
    },
    "AUTH": {
        "syntax": "AUTH [username] [password]",
        "description": "Determines whether a given username and password combination is valid or not",
        "explanation": "Attempts to authenticate as [username] with the password [password] and returns the result of the attempt"
    },
    "PING": {
        "syntax": "PING",
        "description": "Pings the server",
        "explanation": "No-op verb, used simply to test if the server is still online"
    },
    "INFO": {
        "syntax": "INFO | INFO [property] | INFO *",
        "description": "Fetches server information",
        "explanation": "Either returns a list of all public properties of the server, or returns the value of a specified property, or returns a human-readable list of all properties and values on separate lines"
    }
}

clients = {}

def log(level, message, error = False):
    marker = "[-]" if error else "[+]"
    if logLevel >= level:
        print(marker, message)

if not os.path.isdir(directory):
    log(1, directory + " does not exist", True)
    exit()

if not os.path.isfile(directory + "/users"):
    log(1, directory + "/users does not exist", True)
    exit()

log(1, splash)

s = socket.socket()
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(("", port))
s.listen()

ping = "BistoComm listening on port " + str(port)
log(1, ping)

def handleGet(parts):
    if len(parts) == 4:
        username = parts[1]
        password = parts[2]
        fromUser = parts[3]
        if authenticate(username, password):
            if usernameTaken(fromUser):
                try:
                    f = open(directory + "/" + username + "/" + fromUser)
                    messages = f.read()
                    f.close()
                    return 200, messages
                except:
                    return 200, "No messages from " + fromUser + "."
            else:
                return 404, "User " + fromUser + " not found" + "."
        else:
            return 401, "Authentication failed for " + username + "."
    else:
        return 400, "Syntax: " + verbs["GET"]["syntax"]

def handlePost(parts):
    if len(parts) >= 5:
        username = parts[1]
        password = parts[2]
        recipient = parts[3]
        message = " ".join(parts[4:])
        if len(message) <= maxMessageLength:
            if authenticate(username, password):
                if (usernameTaken(recipient)):
                    usernameSpace = max(len(username), len(recipient)) + 5
                    
                    timestamp = str(datetime.datetime.now().strftime("%d/%m/%y %H:%M:%S"))
                    
                    messageLine = timestamp.ljust(len(timestamp) + 4, " ") + (username + ":").ljust(usernameSpace, " ") + message + "\n"
                    
                    recipientFile = open(directory + "/" + recipient + "/" + username, "a+")
                    recipientFile.write(messageLine)
                    recipientFile.close()
                    
                    senderFile = open(directory + "/" + username + "/" + recipient, "a+")
                    senderFile.write(messageLine)
                    senderFile.close()
                    
                    return 201, "Message sent to " + recipient + "."
                else:
                    return 404, "User " + recipient + " not found."
            else:
                return 401, "Authentication failed for " + username + "."
        else:
            return 413, "Maximum message length is " + str(maxMessageLength) + "."
    else:
        return 400, "Syntax: " + verbs["POST"]["syntax"]

def handleDelete(parts):
    if len(parts) == 4:
        username = parts[1]
        password = parts[2]
        fromUser = parts[3]
        if authenticate(username, password):
            if usernameTaken(fromUser):
                path = directory + "/" + username + "/" + fromUser
                if os.path.exists(path):
                    os.remove(path)
                    return 200, "Conversation with " + fromUser + " deleted."
                else:
                    return 404, "No conversation with " + fromUser + "."
            else:
                return 404, "User " + fromUser + " not found."
        else:
            return 401, "Authentication failed for " + username + "."
    else:
        return 400, "Syntax: " + verbs["DELETE"]["syntax"]

def handleDir(parts):
    if len(parts) == 1:
        f = open(directory + "/users")
        credentials = f.readlines()
        users = ""
        for cred in credentials:
            if cred != "\n":
                users += cred.split(" ")[0].rstrip("\r\n") + "\n"
        if users == "":
            users = "There are no users currently registered."
        return 200, users
    else:
        return 400, "Syntax: " + verbs["DIR"]["syntax"]

def handleReg(parts):
    if len(parts) == 3:
        username = parts[1]
        password = parts[2]
        if len(username) <= maxUsernameLength:
            if not username in forbiddenUsernames and username.isalnum() and not password in forbiddenPasswords:
                if not usernameTaken(username):
                    f = open(directory + "/users", "a+")
                    f.write(username + " " + hashPassword(password) + "\n")
                    f.close()
                    os.mkdir(directory + "/" + username)
                    return 201, "Successfully registered as " + username + "."
                else:
                    return 403, "Username " + username + " already taken."
            else:
                return 403, "Cannot register with given credentials."
        else:
            return 413, "Username " + username + " too long"
    else:
        return 400, "Syntax: " + verbs["REG"]["syntax"]

def handleHelp(parts):
    if len(parts) == 1:
        helpText = ""
        for verb, data in verbs.items():
            helpText += verb + "\t" + data["syntax"] + "\n"
        return 200, helpText
    elif len(parts) == 2:
        verb = parts[1].upper()
        try:
            return 200, "Syntax: " + verbs[verb]["syntax"] + "\nDescription: " + verbs[verb]["description"] + "\nExplanation: " + verbs[verb]["explanation"]
        except Exception:
            return 404, "No help available for " + verb + "."
    else:
        return 400, "Syntax: " + verbs["HELP"]["syntax"]

def handleSplash(parts):
    if len(parts) == 1:
        return 200, splash
    else:
        return 400, "Syntax: " + verbs["SPLASH"]["syntax"]

def handleAuth(parts):
    if len(parts) == 3:
        username = parts[1]
        password = parts[2]
        authenticated = authenticate(username, password)
        if authenticated:
            return 200, "Authentication success."
        else:
            return 401, "Authentication failure."
    else:
        return 400, "Syntax: " + verbs["AUTH"]["syntax"]

def handlePing(parts):
    if len(parts) == 1:
        return 200, ping
    else:
        return 400, "Syntax: " + verbs["PING"]["syntax"]
        
def handleInfo(parts):
    if len(parts) == 1:
        info = ""
        for prop in properties.keys():
            info += prop + "\n"
        return 200, info
    elif len(parts) == 2:
        if parts[1] == "*":
            info = ""
            for prop in properties.keys():
                info += prop + " = " + str(properties[prop]) + "\n"
            return 200, info
        else:
            try:
                prop = parts[1]
                return 200, str(properties[prop])
            except Exception:
                return 404, "No such property: " + prop + "."
    else:
        return 400, "Syntax: " + verbs["INFO"]["syntax"]

def usernameTaken(username):
    f = open(directory + "/users", "r+")
    credentials = f.readlines()
    for cred in credentials:
        parts = cred.split(" ")
        if parts[0] == username:
            f.close()
            return True
    f.close()
    return False

def authenticate(username, password):
    f = open(directory + "/users", "r+")
    credentials = f.readlines()
    for cred in credentials:
        parts = cred.split(" ")
        if parts[0] == username and checkPassword(parts[1].rstrip("\n"), password):
            f.close()
            return True
    f.close()
    return False

def hashPassword(password):
    # uuid is used to generate a random number
    salt = uuid.uuid4().hex
    return hashlib.sha256(salt.encode() + password.encode()).hexdigest() + ':' + salt

def checkPassword(hashed, plaintext):
    password, salt = hashed.split(':')
    return password == hashlib.sha256(salt.encode() + plaintext.encode()).hexdigest()

def getResponse(addr, data):
    ip = addr[0]
    parts = data.split(" ")
    verb = parts[0].upper()
    status = 0
    response = ""
    activeConnections = len([c for c in clients[ip] if c.is_alive()])
    if activeConnections > maxConnections:
        status, response = 429, "Connection limit reached. Please wait a moment before retrying."
    elif verb == "GET":
        status, response = handleGet(parts)
    elif verb == "POST":
        status, response = handlePost(parts)
    elif verb == "DELETE":
        status, response = handleDelete(parts)
    elif verb == "DIR":
        status, response = handleDir(parts)
    elif verb == "REG":
        status, response = handleReg(parts)
    elif verb == "HELP":
        status, response = handleHelp(parts)
    elif verb == "SPLASH":
        status, response = handleSplash(parts)
    elif verb == "AUTH":
        status, response = handleAuth(parts)
    elif verb == "PING":
        status, response = handlePing(parts)
    elif verb == "INFO":
        status, response = handleInfo(parts)
    else:
        status, response = handleHelp([""])
    return str(status) + " " + response.rstrip("\n") + "\r\n"

def handleConnection(cid, c, addr):
    ip = addr[0]
    try:
        log(1, "Accepted connection from " + ip)
        request = ""
        prevChar = ""
        char = c.recv(1).decode()
        while prevChar != "\r" or char != "\n":
            log(4, "Received char: " + char)
            if char != "\r":
                request += char
            prevChar = char
            char = c.recv(1).decode()
        log(2, "Received " + request)
        response = getResponse(addr, request)
        c.send(response.encode())
        log(3, "Sent " + response)
        c.close()
        log(1, "Closed connection to " + ip)
    except Exception as msg:
        log(1, str(msg), True)
        c.close()

def garbageCollection():
    while True:
        time.sleep(gcWait)
        for key in clients:
            client = clients[key]
            if len(client) == 0:
                del clients[key]
                break
            else:
                for connection in client:
                    if not connection.is_alive():
                        client.remove(connection)
                        break

gcThread = threading.Thread(target = garbageCollection, daemon = True)
gcThread.start()

while True:
    c, addr = s.accept()
    ip = addr[0]
    
    if not ip in clients:
        clients[ip] = []
        
    cid = len(clients[ip])
    thread = threading.Thread(target = handleConnection, args = (cid, c, addr), daemon = True)
    
    clients[ip].append(thread)
    thread.start()
