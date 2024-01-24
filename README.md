# Event-Venue-API-Demo

# Introduction

This is the Demo API version for Le Prestige Hall that allows users to schedule, read, update and cancel appointments and reservation
of its facility.
It also allows users to view, comment and like recent events that have taken place at the venue.
Users can also share their experiences by posting reviews and rating the venue.

# Technical Details

1. Technologies and Tools:
    * Java 17
    * Spring Boot
    * Spring Data JPA
    * Spring Cloud
    * Spring Security
    * Spring Web
    * Spring Validation
    * Spring Mail
    * Itext PDF
    * PostgreSQL
    * Thymeleaf
    * Azure Spring Cloud
    * Azure Blob Storage
    * Azure Key Vault
    * Keycloak
    * Twilio
2. Architecture:
    * REST

# Getting Started

* Clone the repository
* A standalone Keycloak app version 20.0.3 or higher.
   * Lunch the keycloak instance on port 8080 and log in to the admin console; visit https://www.keycloak.org/getting-started for more information.
   * Create a new realm titled: `LePrestigeHall`
   * Create a client with a name id of your choice.
   * In the web origin section enter `http://localhoast:8085`, or the port and address you will be using to run the project.
* Open the project in your IDE
   * Create a PostgreSQL database, and update the application.yml file with your database credentials
   * Create a Blob storage account, and update the application.yml file with your storage account credentials
   * Update the application.yml file with your email credentials
   * Update the application.yml file with your JWT secret key
   * Update all email information defined in the constants class in the constants package
* Run the project

# API Documentation

## Authentication

### Register

### Login

#### Request

`POST /api/auth/login`

    curl -i -H 'Accept: application/json' -d 'username=username&password=password' http://localhost:8080/api/auth/login

#### Response
    
        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

### Logout

#### Request

`POST /api/auth/logout`

    curl -i -H 'Accept: application/json' -d 'username=username&password=password' http://localhost:8080/api/auth/logout

#### Response
    
        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

## Users

### Get all users

#### Request

`GET /api/users`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/users

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

### Get user by id

#### Request

`GET /api/users/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/users/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

### Update user

#### Request

`PUT /api/users/{id}`

    curl -i -H 'Accept: application/json' -d 'firstName=firstName&lastName=lastName&email=email&username=username&password=password&role=role' http://localhost:8085/api/users/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

### Delete user

#### Request

`DELETE /api/users/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/users/1

#### Response

        HTTP/1.1 204 No Content
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

## Appointments

### Get all appointments

#### Request

`GET /api/appointments`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/appointments

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        
        [
            {
                "id": "id",
                "firstName": "firstName",
                "lastName": "lastName",
                "phone": "phone",
                "email": "email",
                "dateTime": "dateTime",
                "raison": "raison",
                "additionalInfo": "additionalInfo",
            }
        ]

### Get appointment by id

#### Request

`GET /api/appointments/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/appointments/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        
        {
            "id": "id",
            "firstName": "firstName",
            "lastName": "lastName",
            "phone": "phone",
            "email": "email",
            "dateTime": "dateTime",
            "raison": "raison",
            "additionalInfo": "additionalInfo",
        }

### Create appointment

#### Request

`POST /api/appointments`

    curl -i -H 'Accept: application/json' -d 'firstName=firstName&lastName=lastName&phone=phone&email=email&dateTime=dateTime&raison=raison&additionalInfo=additionalInfo' http://localhost:8085/api/appointments

#### Response

        HTTP/1.1 201 Created
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Update appointment

#### Request

`PUT /api/appointments/{id}`

    curl -i -H 'Accept: application/json' -d 'firstName=firstName&lastName=lastName&phone=phone&email=email&dateTime=dateTime&raison=raison&additionalInfo=additionalInfo' http://localhost:8085/api/appointments/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0

### Cancel appointment

#### Request

`DELETE /api/appointments/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/appointments/1

#### Response

        HTTP/1.1 204 No Content
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


## Reservations

### Get all reservations

#### Request

`GET /api/reservations`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/reservations

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json

        [
            {
                "id": "id",
                "startingDateTime": "startingDateTime",
                "endingDateTime": "endingDateTime",
                "effectiveEndingDateTime": "effectiveEndingDateTime",
                "eventType": "eventType",
                "numberOfSeats": 0,
                "addOns": [],
                "addOnsTotalCost": 0,
                "status": "Pending",
                "fullPackage": false,
                "securityDepositRefunded": false,
                "subTotal": 0,
                "taxRate": .7,
                "totalPrice": 0,
                "rates": 
                    {
                        cleaningRate: 0,
                        facilityRate: 0,
                        overtimeRate: 0,
                        seatRate: 0,
                    },
                "priceComputationMethod": "Auto",
                "userId": "userId",
            }
        ]


### Get reservation by user id

#### Request

`GET /api/user/{userId}/reservations

    curl -i -H 'Accept: application/json' http://localhost:8085/api/user/userId/reservations

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT

        [
            {
                "id": "id",
                "startingDateTime": "startingDateTime",
                "endingDateTime": "endingDateTime",
                "effectiveEndingDateTime": "effectiveEndingDateTime",
                "eventType": "eventType",
                "numberOfSeats": 0,
                "addOns": [],
                "addOnsTotalCost": 0,
                "status": "Pending",
                "fullPackage": false,
                "securityDepositRefunded": false,
                "subTotal": 0,
                "taxRate": .7,
                "totalPrice": 0,
                "rates": 
                    {
                        cleaningRate: 0,
                        facilityRate: 0,
                        overtimeRate: 0,
                        seatRate: 0,
                    },
                "priceComputationMethod": "Auto",
                "userId": "userId",
            }
        ]


### Create reservation

#### Request

`POST /api/reservations`

    curl -i -H 'Accept: application/json' -d 'startingDateTime=startingDateTime&endingDateTime=endingDateTime&effectiveEndingDateTime=effectiveEndingDateTime&eventType=eventType&numberOfSeats=numberOfSeats&addOns=addOns&addOnsTotalCost=addOnsTotalCost&status=status&fullPackage=fullPackage&securityDepositRefunded=securityDepositRefunded&subTotal=subTotal&taxRate=taxRate&totalPrice=totalPrice&rates=rates&priceComputationMethod=priceComputationMethod&userId=userId' http://localhost:8085/api/reservations

#### Response

        HTTP/1.1 201 Created
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Update reservation

#### Request

`PUT /api/reservations/{id}`

    curl -i -H 'Accept: application/json' -d 'startingDateTime=startingDateTime&endingDateTime=endingDateTime&effectiveEndingDateTime=effectiveEndingDateTime&eventType=eventType&numberOfSeats=numberOfSeats&addOns=addOns&addOnsTotalCost=addOnsTotalCost&status=status&fullPackage=fullPackage&securityDepositRefunded=securityDepositRefunded&subTotal=subTotal&taxRate=taxRate&totalPrice=totalPrice&rates=rates&priceComputationMethod=priceComputationMethod&userId=userId' http://localhost:8085/api/reservations/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Cancel reservation

#### Request

`DELETE /api/reservations/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/reservations/1

#### Response

        HTTP/1.1 204 No Content
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0



## Reviews

### Get all reviews

#### Request

`GET /api/reviews`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/reviews

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json

        [
            {
                "id": 1,
                "title": "title",
                "comment": "comment",
                "rating": 1,
                "postedDate": "postedDate",
                "lastEditedDate": "lastEditedDate",
                "user": {
                    "userId": "userId",
                    "firstName": "firstName",
                    "lastName": "lastName",
                    "profilePictureUrl": "profilePictureUrl",
                }
            }
        ]


### Get review by id


#### Request

`GET /api/reviews/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/reviews/1


#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json

        {
            "id": 1,
            "title": "title",
            "comment": "comment",
            "rating": 1,
            "postedDate": "postedDate",
            "lastEditedDate": "lastEditedDate",
            "user": {
                "userId": "userId",
                "firstName": "firstName",
                "lastName": "lastName",
                "profilePictureUrl": "profilePictureUrl",
            }
        }


### Create review

#### Request

`POST /api/reviews`

    curl -i -H 'Accept: application/json' -d 'title=title&comment=comment&rating=rating&user=userSumaryDTO' http://localhost:8085/api/reviews

#### Response

        HTTP/1.1 201 Created
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Update review

#### Request

`PUT /api/reviews/{id}`

    curl -i -H 'Accept: application/json' -d 'title=title&comment=comment&rating=rating&user=userSumaryDTO' http://localhost:8085/api/reviews/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Delete review

#### Request

`DELETE /api/reviews/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/reviews/1

#### Response

        HTTP/1.1 204 No Content
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0





## FAQs

### Get all FAQs

#### Request

`GET /api/faqs`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/faqs

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json

        [
            {
                "id": 1,
                "question": "question",
                "answer": "answer"
            }
        ]


### Get FAQ by id

#### Request

`GET /api/faqs/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/faqs/1


#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json

        {
            "id": 1,
            "question": "question",
            "answer": "answer"
            "moreDetails": "moreDetails"
        }


### Create FAQ

#### Request

`POST /api/faqs`

    curl -i -H 'Accept: application/json' -d 'question=question&answer=answer&moreDetails=moreDetails' http://localhost:8085/api/faqs

#### Response

        HTTP/1.1 201 Created
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Update FAQ

#### Request

`PUT /api/faqs/{id}`

    curl -i -H 'Accept: application/json' -d 'question=question&answer=answer&moreDetails=moreDetails' http://localhost:8085/api/faqs/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0


### Delete FAQ

#### Request

`DELETE /api/faqs/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/faqs/1

#### Response

        HTTP/1.1 204 No Content
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0



## Comments

### Get all comments by event id

#### Request

`GET /api/events/{id}/comments`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/events/1/comments

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT

        [
            {
                "id": 1,
                "content": "content",
                "userId": "userId",
                "postedDate": "postedDate",
                "edited": false,
                "eventId": 1,
                "basedCommentId": null,
                "commentLikesDislikes": [
                    {
                        "id": 1,
                        "like": true,
                        "userId": "userId",
                        "commentId": 1
                    }
                ] 
            }
        ]


### Get all comments by based comment id

#### Request

`GET /api/events/{eventId}/comments/{id}/replies`

        curl -i -H 'Accept: application/json' http://localhost:8085/api/events/kybd-yhjj-jnjn/comments/1/replies

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT

        [
            {
                "id": 2,
                "content": "content",
                "userId": "userId",
                "postedDate": "postedDate",
                "edited": false,
                "eventId": 1,
                "basedCommentId": 1,
                "commentLikesDislikes": [
                    {
                        "id": 1,
                        "like": true,
                        "userId": "userId",
                        "commentId": 1
                    }
                ] 
            }
        ]


### Get comment by id

#### Request

`GET /api/events/{eventId}/comments/{id}`

    curl -i -H 'Accept: application/json' http://localhost:8085/api/events/1/comments/1

#### Response

        HTTP/1.1 200 OK
        Date: Mon, 01 Jan 2021 00:00:00 GMT

        {
            "id": 1,
            "content": "content",
            "userId": "userId",
            "postedDate": "postedDate",
            "edited": false,
            "eventId": 1,
            "basedCommentId": null,
            "commentLikesDislikes": [
                {
                    "id": 1,
                    "like": true,
                    "userId": "userId",
                    "commentId": 1
                }
            ] 
        }


### Create comment

#### Request

`POST /api/events/{eventId}/comments`

    curl -i -H 'Accept: application/json' -d 'content=content&userId=userId' http://localhost:8085/api/events/1/comments

#### Response

        HTTP/1.1 201 Created
        Date: Mon, 01 Jan 2021 00:00:00 GMT
        Content-Type: application/json
        Content-Length: 0




# Notes.

This project is still in progress (mainly the tests suites).

# UIs repositories links.

* User Interface: https://github.com/Zinphraek/Event-Venue-UI-Demo
* Admin Interface: https://github.com/Zinphraek/Event-Venue-Admin-UI-Demo
