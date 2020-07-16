import socket
import time

verbs = {
    "RECV": {
        "syntax": "RECV [from]",
        "description": "Fetches messages from the server",
        "explanation": ""
    },
    "SEND": {
        "syntax": "SEND [recipient] [message]",
        "description": "Posts a message to the server",
        "explanation": ""
    },
    "DELETE": {
        "syntax": "DELETE [from]",
        "description": "Deletes the conversation with another user from your account",
        "explanation": ""
    },
    "LOGIN": {
        "syntax": "LOGIN [username] [password]",
        "description": "Attempts login with the given credentials",
        "explanation": ""
    },
    "LOGOUT": {
        "syntax": "LOGOUT",
        "description": "Logs the current user out",
        "explanation": ""
    },
    "HELP": {
        "syntax": "HELP | HELP [verb]",
        "description": "Fetches general help or help specific to a verb",
        "explanation": ""
    },
    "DIR": {
        "syntax": "DIR",
        "description": "Fetches a list of all registered users",
        "explanation": ""
    },
    "CONN": {
        "syntax": "CONN | CONN [host] [port]",
        "description": "Connects to the given server",
        "explanation": ""
    },
    "REG": {
        "syntax": "REG [username] [password]",
        "description": "Register a new user",
        "explanation": ""
    },
    "SERVER": {
        "syntax": "SERVER [request]",
        "description": "Sends a custom request to the server",
        "explanation": ""
    }
}
maxConnAttempts = 5
timeout = 5

def transfer(data):
    data += "\r\n"
    status = 0
    response = ""
    
    s = socket.socket()
    s.connect((host, port))
    s.send(data.encode())
    
    prevChar = ""
    char = s.recv(1).decode()
    while prevChar != "\r" or char != "\n":
        if char != "\r":
            response += char
        prevChar = char
        char = s.recv(1).decode()
        
    s.close()
    
    response = response.split(" ")
    status = int(response[0])
    response = " ".join(response[1:])
    
    return status, response

def handleLogin(parts):
    global username
    global password
    if len(parts) == 3:
        if connected:
            request = "AUTH " + parts[1] + " " + parts[2]
            status, response = transfer(request)
            if status == 200:
                username = parts[1]
                password = parts[2]
            return status, response
        else:
            return 404, "Not connected"
    else:
        return 400, "Syntax: " + verbs["LOGIN"]["syntax"]

def handleLogout(parts):
    global username
    global password
    if len(parts) == 1:
        if loggedIn():
            username = ""
            password = ""
            return 200, "Logged out"
        else:
            return 200, "Not logged in"
    else:
        return 400, "Syntax: " + verbs["LOGOUT"]["syntax"]

def handleRecv(parts):
    if len(parts) == 2:
        if connected:
            userFrom = parts[1]
            if loggedIn():
                return transfer("GET " + username + " " + password + " " + userFrom)
            else:
                return 401, "Log in first"
        else:
            return 404, "Not connected"
    else:
        return 400, "Syntax: " + verbs["RECV"]["syntax"]

def handleSend(parts):
    if len(parts) >= 3:
        if connected:
            recipient = parts[1]
            message = " ".join(parts[2:])
            if loggedIn():
                return transfer("POST " + username + " " + password + " " + recipient + " " + message)
            else:
                return 401, "Log in first"
        else:
            return 404, "Not connected"
    else:
        return 400, "Syntax: " + verbs["SEND"]["syntax"]

def handleDelete(parts):
    if len(parts) == 2:
        if connected:
            userFrom = parts[1]
            if loggedIn():
                confirm = input("[ $ ] Are you sure you wish to delete your conversation with " + userFrom + "? [y/N] ").lower()
                if confirm in ["y", "yes"]:
                    return transfer("DELETE " + username + " " + password + " " + userFrom)
                else:
                    return 304, "Deletion aborted"
            else:
                return 401, "Log in first"
        else:
            return 404, "Not connected"
    else:
        return 400, "Syntax: " + verbs["DELETE"]["syntax"]

def handleHelp(parts):
    if len(parts) == 1:
        helpText = ""
        for verb, data in verbs.items():
            helpText += verb + "\t" + data["syntax"] + "\n"
        return 200, helpText.rstrip("\n")
    elif len(parts) == 2:
        verb = parts[1].upper()
        try:
            return 200, "Syntax: " + verbs[verb]["syntax"] + "\nDescription: " + verbs[verb]["description"] + "\nExplanation: " + verbs[verb]["explanation"]
        except Exception:
            return 404, "No help available for " + verb
    else:
        return 400, "Syntax: " + verbs["HELP"]["syntax"]

def handleConn(parts):
    global host
    global port
    if len(parts) == 3:
        host = parts[1]
        try:
            port = int(parts[2])
            if (port < 1 or port > 65535):
                raise Exception
        except Exception:
            return 400, "Port must be a positive integer from 1 through 65535"
        return connect(host, port)
    elif len(parts) == 1:
        return connect(host, port)
    else:
        return 400, "Syntax: " + verbs["CONN"]["syntax"]

def connect(host, port):
    global connected
    for i in range(maxConnAttempts):
        try:
            status, splash = transfer("SPLASH")
            print(addStatus(status, splash) + "\n[   ]")
            status, ping = transfer("PING")
            print(addStatus(status, ping) + "\n[   ]")
            status, info = transfer("INFO *")
            print(addStatus(status, info) + "\n[   ]")
            connected = True
            return 200, "Connected"
        except:
            print(addStatus(404, "Server not found\nRetrying in " + str(timeout) + " seconds"))
            time.sleep(timeout)
    connected = False
    return 404, "Server not found\nConnection failed"

def loggedIn():
    return username != "" and password != ""

def addStatus(status, response):
    return "[" + str(status) + "] " + response.replace("\n", "\n[" + str(status) + "] ")

host = "127.0.0.1"
port = 5001
connected = False

username = ""
password = ""

while True:
    data = input("[ $ ] ")
    parts = data.split(" ")
    verb = parts[0].upper()
    status = 0
    response = ""
    if verb == "CONN":
        status, response = handleConn(parts)
    elif verb == "LOGIN":
        status, response = handleLogin(parts)
    elif verb == "LOGOUT":
        status, response = handleLogout(parts)
    elif verb == "RECV":
        status, response = handleRecv(parts)
    elif verb == "SEND":
        status, response = handleSend(parts)
    elif verb == "DELETE":
        status, response = handleDelete(parts)
    elif verb == "REG" or verb == "REGISTER":
        if connected:
            status, response = transfer(data)
        else:
            status, response = 404, "Not connected"
    elif verb == "DIR":
        if connected:
            status, response = transfer(data)
        else:
            status, response = 404, "Not connected"
    elif verb == "HELP":
        status, response = handleHelp(parts)
    elif verb == "SERVER":
        if connected:
            status, response = transfer(" ".join(parts[1:]))
        else:
            status, response = 404, "Not connected"
    else:
        status, response = 400, "Unknown verb"
        
    print(addStatus(status, response))
