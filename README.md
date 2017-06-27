# lagom-jwt-authentication

Example project to integrate Lagom framework with JWT authentication.

Actions:
- Create a client with initial user
- Login with created user
- Create another user
- Get current state
- Obtain a new authorization token

Validation:
- Validates input fields
- Validates that username or email are not already used

Error handling:
- Custom error handling to show fields which failed a validation (can be used to show errors on frontend easily)

# Health 
[![Build Status](https://travis-ci.org/dpalinic/public-transportation-services.svg?branch=master)](https://travis-ci.org/dpalinic/public-transportation-services)

# How To

## Start services

1. Install sbt and jdk 8
2. Checkout this project using `git checkout`
3. Run `sbt` from the project root
4. Run `runAll` to start the application

## Create a database schema

Execute cql statements from `identity-impl/src/main/resources/V1.0__db_schema.cql` to create needed Cassandra database schema.

1. Connect to Cassandra with `cqlsh localhost 4000`
2. Import schema with `source '/absolute/path/lagom-jwt-authentication/identity-impl/src/main/resources/db/migration/cassandra/V1.0__db_schema.cql'`. Please replace `/absolute/path/` with your absolute path to application.
 
## Use services
 
Postman collections can be found here: https://www.getpostman.com/collections/ee0f1331422a982bc7f0

### Create a client and initial user

**URL**

`POST http://localhost:9000/api/client/registration`

**Request Headers**

None

**Request Body**
```json
{
    "company": "Digital Cat",
    "firstName": "Damir",
    "lastName": "Palinic",
    "email": "damir@palinic.com",
    "username": "dpalinic",
    "password": "test12345"
 }
```

**Response**
```json
{
    "id": "9bdfe4d3-ef23-4cd8-8ab7-e70a5ffe1722"
}
```

### Login with created user

**URL**

`POST http://localhost:9000/api/user/login`

**Request Headers**

None

**Request Body**
```json
{
	"username": "dpalinic",
	"password": "test12345"
}
```

**Response**
```json
{
    "authToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0OTgzMDQyMjMsImlhdCI6MTQ5ODMwMzkyMywiY2xpZW50SWQiOiI5YmRmZTRkMy1lZjIzLTRjZDgtOGFiNy1lNzBhNWZmZTE3MjIiLCJ1c2VySWQiOiJhZWM0NjU0NS1jNDQxLTRjYTItODEzNC1hMTcyN2NiOGVkODYiLCJ1c2VybmFtZSI6ImRwYWxpbmljIiwiaXNSZWZyZXNoVG9rZW4iOmZhbHNlfQ.nVmQKB_94JPEL9-SmyAZ1u3HB-Z8UcOxSLR1Wrgqa8jFdyP-jORkBrWVMBkkeH7i8ypnHNw5Duxc5hwlpWIaZQ",
    "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0OTgzOTAzMjMsImlhdCI6MTQ5ODMwMzkyMywiY2xpZW50SWQiOiI5YmRmZTRkMy1lZjIzLTRjZDgtOGFiNy1lNzBhNWZmZTE3MjIiLCJ1c2VySWQiOiJhZWM0NjU0NS1jNDQxLTRjYTItODEzNC1hMTcyN2NiOGVkODYiLCJ1c2VybmFtZSI6ImRwYWxpbmljIiwiaXNSZWZyZXNoVG9rZW4iOnRydWV9.I-Asm-ewQysboUDkaNRD6SK-yTSx2uza4szlC8ZLw8gZgnuSaXMKu86Ab-swMSnOjU_ubm6-73D55ydwM28e1w"
}
```

`authToken` is used for authorization and it lives for 5 minutes. Refresh token is used to generate a new authorization token and it lives for 24 hours.

### Create another user

**URL**

`POST http://localhost:9000/api/user`

**Request Headers**

| Key           | Value        |
| --------------|:-------------|
| Authorization | Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0OTgzMDQyMjMsImlhdCI6MTQ5ODMwMzkyMywiY2xpZW50SWQiOiI5YmRmZTRkMy1lZjIzLTRjZDgtOGFiNy1lNzBhNWZmZTE3MjIiLCJ1c2VySWQiOiJhZWM0NjU0NS1jNDQxLTRjYTItODEzNC1hMTcyN2NiOGVkODYiLCJ1c2VybmFtZSI6ImRwYWxpbmljIiwiaXNSZWZyZXNoVG9rZW4iOmZhbHNlfQ.nVmQKB_94JPEL9-SmyAZ1u3HB-Z8UcOxSLR1Wrgqa8jFdyP-jORkBrWVMBkkeH7i8ypnHNw5Duxc5hwlpWIaZQ |

**Request Body**
```json
{
	"firstName": "Jelena",
	"lastName": "Palinic",
	"email": "jelena@palinic.com",
	"username": "jpalinic",
	"password": "test12345"
}
```

**Response**
```json
{
    "id": "dcedde37-a336-4377-9935-a600357a5fef"
}
```

### Get current state

**URL**

`GET http://localhost:9000/api/state/identity`

**Request Headers**

| Key           | Value        |
| --------------|:-------------|
| Authorization | Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0OTgzMDQyMjMsImlhdCI6MTQ5ODMwMzkyMywiY2xpZW50SWQiOiI5YmRmZTRkMy1lZjIzLTRjZDgtOGFiNy1lNzBhNWZmZTE3MjIiLCJ1c2VySWQiOiJhZWM0NjU0NS1jNDQxLTRjYTItODEzNC1hMTcyN2NiOGVkODYiLCJ1c2VybmFtZSI6ImRwYWxpbmljIiwiaXNSZWZyZXNoVG9rZW4iOmZhbHNlfQ.nVmQKB_94JPEL9-SmyAZ1u3HB-Z8UcOxSLR1Wrgqa8jFdyP-jORkBrWVMBkkeH7i8ypnHNw5Duxc5hwlpWIaZQ |

**Response**

```json
{
    "id": "9bdfe4d3-ef23-4cd8-8ab7-e70a5ffe1722",
    "company": "Digital Cat",
    "users": [
        {
            "id": "aec46545-c441-4ca2-8134-a1727cb8ed86",
            "firstName": "Damir",
            "lastName": "Palinic",
            "email": "damir@palinic.com",
            "username": "dpalinic"
        },
        {
            "id": "dcedde37-a336-4377-9935-a600357a5fef",
            "firstName": "Jelena",
            "lastName": "Palinic",
            "email": "jelena@palinic.com",
            "username": "jpalinic"
        }
    ]
}
```

### Obtain a new authorization token

**URL**

`PUT http://localhost:9000/api/user/token`

**Request Headers**

| Key           | Value        |
| --------------|:-------------|
| Authorization | Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0OTgzOTAzMjMsImlhdCI6MTQ5ODMwMzkyMywiY2xpZW50SWQiOiI5YmRmZTRkMy1lZjIzLTRjZDgtOGFiNy1lNzBhNWZmZTE3MjIiLCJ1c2VySWQiOiJhZWM0NjU0NS1jNDQxLTRjYTItODEzNC1hMTcyN2NiOGVkODYiLCJ1c2VybmFtZSI6ImRwYWxpbmljIiwiaXNSZWZyZXNoVG9rZW4iOnRydWV9.I-Asm-ewQysboUDkaNRD6SK-yTSx2uza4szlC8ZLw8gZgnuSaXMKu86Ab-swMSnOjU_ubm6-73D55ydwM28e1w |


Please note that you need to use refresh token in Authorization header now!

**Response**

```json
{
    "authToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0OTgzMDQ2NDAsImlhdCI6MTQ5ODMwNDM0MCwiY2xpZW50SWQiOiI5YmRmZTRkMy1lZjIzLTRjZDgtOGFiNy1lNzBhNWZmZTE3MjIiLCJ1c2VySWQiOiJhZWM0NjU0NS1jNDQxLTRjYTItODEzNC1hMTcyN2NiOGVkODYiLCJ1c2VybmFtZSI6ImRwYWxpbmljIiwiaXNSZWZyZXNoVG9rZW4iOnRydWV9.U4z-oJUmKISbUxDN8SVxzPEKzhHbqsMt1gbjJYUVtNLxdlXhl18czTfaz0ET7yDAuaHIU7aPRo_ivS3LNcWMIg"
}
```