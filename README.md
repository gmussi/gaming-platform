# Backend services

## Admin service

The admin service provides tools to create account, login and obtain tickets and tokens, that are used to authenticate in other services.

### Check if username is available

**Input**

`curl --location --request GET 'http://{ADMIN_URL}/auth/available/{username}'`

**Output** (TEXT/PLAIN)
 
`true` if username is available or `false` if username has been taken

### Register a new user

**Input**

```bash
curl --location --request POST 'http://{ADMIN_URL}/auth/register' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'username={username}' \
 --data-urlencode 'password={password}'
```

**Output** (TEXT/PLAIN)

`jwt token` String containing the authorization token to use in requests

### Sign in

**Input**

```bash
curl --location --request POST 'http://{ADMIN_URL}/auth/login' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username={username}' \
--data-urlencode 'password={password}'
```

**Output** (TEXT/PLAIN)

`jwt token` Stirng containing the authorization token to use in requests

### Getting a ticket
Tickets are single-use tokens used to connect via websocket (since websockets do not accept headers).

**Input**
```bash
curl --location --request GET 'http://{ADMIN_URL}/ticket/' \
--header 'Authorization: Bearer {jwt token}'
```

**Output** (TEXT/PLAIN)

`ticketId` Ticket to be used to connect to gameplay service 

## Gameplay service

## Statistics service