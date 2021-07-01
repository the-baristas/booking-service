# Booking Service

A microservice that handles requests for performing persistent storage
operations on a booking, a passenger, a discount, a payment, or a payment
intent.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)

## Installation

- Install Java 8 JDK
- Install Maven
- Clone project
- Change into project directory:
  ```
  cd booking-service
  ```
- Package project
  ```
  mvn package
  ```
- Run the application
  ```
  java -jar target/[SNAPSHOT NAME].jar
  ```

## Usage

### Endpoints:

- [GET /bookings](#get-bookings)

  Find all bookings.
- [POST /bookings](#post-bookings)

  Create a booking.
- [PUT /bookings{id}](#put-bookings)

  Update a booking.
- [DELETE /bookings{id}](#delete-bookings)

  Delete a booking.
- [GET /passengers](#get-passengers)
- [POST /passengers](#post-passengers)
- [PUT /passengers{id}](#put-passengers)
- [DELETE /passengers{id}](#delete-passengers)
- [GET /payments/{stripe_id}](#get-payments-stripe-id)

## GET /bookings

Under construction.

### Parameters:

confirmation_code (string)

### Response:

```
{
  "id": 0,
  "active": true,
  "confirmationCode": "string",
  "layoverCount": 0,
  "totalPrice": 0,
  "username": "string",
  "email": "string",
  "phone": "string",
  "flights": [
    {
      "id": 0,
      "active": true,
      "departureTime": "2021-07-01T00:01:32.787Z",
      "arrivalTime": "2021-07-01T00:01:32.787Z",
      "routeId": 0,
      "routeActive": true,
      "originAirportCode": "string",
      "originAirportCity": "string",
      "originAirportActive": true,
      "destinationAirportCode": "string",
      "destinationAirportCity": "string",
      "destinationAirportActive": true,
      "airplaneModel": "string",
      "passengers": [
        {
          "id": 0,
          "bookingId": 0,
          "bookingActive": true,
          "bookingConfirmationCode": "string",
          "layoverCount": 0,
          "bookingTotalPrice": 0,
          "flightId": 0,
          "flightActive": true,
          "departureTime": "2021-07-01T00:01:32.787Z",
          "arrivalTime": "2021-07-01T00:01:32.787Z",
          "routeId": 0,
          "routeActive": true,
          "originAirportCode": "string",
          "originAirportActive": true,
          "originAirportCity": "string",
          "destinationAirportCode": "string",
          "destinationAirportActive": true,
          "destinationAirportCity": "string",
          "discountType": "string",
          "discountRate": 0,
          "givenName": "string",
          "familyName": "string",
          "dateOfBirth": "2021-07-01",
          "gender": "string",
          "address": "string",
          "seatClass": "string",
          "seatNumber": 0,
          "checkInGroup": 0,
          "username": "string",
          "email": "string",
          "phone": "string"
        }
      ]
    }
  ],
  "passengers": [
    {
      "id": 0,
      "bookingId": 0,
      "bookingActive": true,
      "bookingConfirmationCode": "string",
      "layoverCount": 0,
      "bookingTotalPrice": 0,
      "flightId": 0,
      "flightActive": true,
      "departureTime": "2021-07-01T00:01:32.787Z",
      "arrivalTime": "2021-07-01T00:01:32.787Z",
      "routeId": 0,
      "routeActive": true,
      "originAirportCode": "string",
      "originAirportActive": true,
      "originAirportCity": "string",
      "destinationAirportCode": "string",
      "destinationAirportActive": true,
      "destinationAirportCity": "string",
      "discountType": "string",
      "discountRate": 0,
      "givenName": "string",
      "familyName": "string",
      "dateOfBirth": "2021-07-01",
      "gender": "string",
      "address": "string",
      "seatClass": "string",
      "seatNumber": 0,
      "checkInGroup": 0,
      "username": "string",
      "email": "string",
      "phone": "string"
    }
  ]
}
```

## POST /bookings

### Request body:

```
{
  "confirmationCode": "string",
  "layoverCount": 0,
  "username": "string"
}
```

### Response:

```
{
  "id": 0,
  "active": true,
  "confirmationCode": "string",
  "layoverCount": 0,
  "totalPrice": 0,
  "username": "string",
  "email": "string",
  "phone": "string",
  "flights": [
    {
      "id": 0,
      "active": true,
      "departureTime": "2021-06-30T21:10:20.250Z",
      "arrivalTime": "2021-06-30T21:10:20.250Z",
      "routeId": 0,
      "routeActive": true,
      "originAirportCode": "string",
      "originAirportCity": "string",
      "originAirportActive": true,
      "destinationAirportCode": "string",
      "destinationAirportCity": "string",
      "destinationAirportActive": true,
      "airplaneModel": "string",
      "passengers": [
        {
          "id": 0,
          "bookingId": 0,
          "bookingActive": true,
          "bookingConfirmationCode": "string",
          "layoverCount": 0,
          "bookingTotalPrice": 0,
          "flightId": 0,
          "flightActive": true,
          "departureTime": "2021-06-30T21:10:20.250Z",
          "arrivalTime": "2021-06-30T21:10:20.250Z",
          "routeId": 0,
          "routeActive": true,
          "originAirportCode": "string",
          "originAirportActive": true,
          "originAirportCity": "string",
          "destinationAirportCode": "string",
          "destinationAirportActive": true,
          "destinationAirportCity": "string",
          "discountType": "string",
          "discountRate": 0,
          "givenName": "string",
          "familyName": "string",
          "dateOfBirth": "2021-06-30",
          "gender": "string",
          "address": "string",
          "seatClass": "string",
          "seatNumber": 0,
          "checkInGroup": 0,
          "username": "string",
          "email": "string",
          "phone": "string"
        }
      ]
    }
  ],
  "passengers": [
    {
      "id": 0,
      "bookingId": 0,
      "bookingActive": true,
      "bookingConfirmationCode": "string",
      "layoverCount": 0,
      "bookingTotalPrice": 0,
      "flightId": 0,
      "flightActive": true,
      "departureTime": "2021-06-30T21:10:20.250Z",
      "arrivalTime": "2021-06-30T21:10:20.250Z",
      "routeId": 0,
      "routeActive": true,
      "originAirportCode": "string",
      "originAirportActive": true,
      "originAirportCity": "string",
      "destinationAirportCode": "string",
      "destinationAirportActive": true,
      "destinationAirportCity": "string",
      "discountType": "string",
      "discountRate": 0,
      "givenName": "string",
      "familyName": "string",
      "dateOfBirth": "2021-06-30",
      "gender": "string",
      "address": "string",
      "seatClass": "string",
      "seatNumber": 0,
      "checkInGroup": 0,
      "username": "string",
      "email": "string",
      "phone": "string"
    }
  ]
}
```

## PUT /bookings/{id}

Under construction.

### Parameters:

id (integer)

### Request body:

```
{
  "confirmationCode": "string",
  "layoverCount": 0,
  "totalPrice": 0,
  "username": "string"
}
```

### Responses:

## DELETE /bookings/{id}

### Parameters:

id (integer)

## GET /passengers

### Parameters:

index (integer)

size (integer)

### Response:

```
{
  "totalPages": 0,
  "totalElements": 0,
  "last": true,
  "size": 0,
  "content": [
    {
      "id": 0,
      "bookingId": 0,
      "bookingActive": true,
      "bookingConfirmationCode": "string",
      "layoverCount": 0,
      "bookingTotalPrice": 0,
      "flightId": 0,
      "flightActive": true,
      "departureTime": "2021-06-30T18:52:56.373Z",
      "arrivalTime": "2021-06-30T18:52:56.373Z",
      "routeId": 0,
      "routeActive": true,
      "originAirportCode": "string",
      "originAirportActive": true,
      "originAirportCity": "string",
      "destinationAirportCode": "string",
      "destinationAirportActive": true,
      "destinationAirportCity": "string",
      "discountType": "string",
      "discountRate": 0,
      "givenName": "string",
      "familyName": "string",
      "dateOfBirth": "2021-06-30",
      "gender": "string",
      "address": "string",
      "seatClass": "string",
      "seatNumber": 0,
      "checkInGroup": 0,
      "username": "string",
      "email": "string",
      "phone": "string"
    }
  ],
  "number": 0,
  "sort": {
    "unsorted": true,
    "sorted": true,
    "empty": true
  },
  "numberOfElements": 0,
  "first": true,
  "pageable": {
    "paged": true,
    "unpaged": true,
    "offset": 0,
    "sort": {
      "unsorted": true,
      "sorted": true,
      "empty": true
    },
    "pageNumber": 0,
    "pageSize": 0
  },
  "empty": true
}
```

## POST /passengers

### Request body:

```
{
  "bookingConfirmationCode": "string",
  "originAirportCode": "string",
  "destinationAirportCode": "string",
  "airplaneModel": "string",
  "departureTime": "2021-06-30T18:55:51.642Z",
  "arrivalTime": "2021-06-30T18:55:51.642Z",
  "givenName": "string",
  "familyName": "string",
  "dateOfBirth": "2021-06-30",
  "gender": "string",
  "address": "string",
  "seatClass": "string",
  "seatNumber": 0,
  "checkInGroup": 0
}
```

### Response:

```
{
  "id": 0,
  "bookingId": 0,
  "bookingActive": true,
  "bookingConfirmationCode": "string",
  "layoverCount": 0,
  "bookingTotalPrice": 0,
  "flightId": 0,
  "flightActive": true,
  "departureTime": "2021-06-30T18:55:51.673Z",
  "arrivalTime": "2021-06-30T18:55:51.673Z",
  "routeId": 0,
  "routeActive": true,
  "originAirportCode": "string",
  "originAirportActive": true,
  "originAirportCity": "string",
  "destinationAirportCode": "string",
  "destinationAirportActive": true,
  "destinationAirportCity": "string",
  "discountType": "string",
  "discountRate": 0,
  "givenName": "string",
  "familyName": "string",
  "dateOfBirth": "2021-06-30",
  "gender": "string",
  "address": "string",
  "seatClass": "string",
  "seatNumber": 0,
  "checkInGroup": 0,
  "username": "string",
  "email": "string",
  "phone": "string"
}
```

## PUT /passengers/{id}

### Parameters:

id (integer)

### Request body:

```
{
  "id": 0,
  "givenName": "string",
  "familyName": "string",
  "dateOfBirth": "2021-07-01",
  "gender": "string",
  "address": "string",
  "seatClass": "string",
  "seatNumber": 0,
  "checkInGroup": 0
}
```

### Response:

```
{
  "id": 0,
  "bookingId": 0,
  "bookingActive": true,
  "bookingConfirmationCode": "string",
  "layoverCount": 0,
  "bookingTotalPrice": 0,
  "flightId": 0,
  "flightActive": true,
  "departureTime": "2021-07-01T00:10:29.693Z",
  "arrivalTime": "2021-07-01T00:10:29.693Z",
  "routeId": 0,
  "routeActive": true,
  "originAirportCode": "string",
  "originAirportActive": true,
  "originAirportCity": "string",
  "destinationAirportCode": "string",
  "destinationAirportActive": true,
  "destinationAirportCity": "string",
  "discountType": "string",
  "discountRate": 0,
  "givenName": "string",
  "familyName": "string",
  "dateOfBirth": "2021-07-01",
  "gender": "string",
  "address": "string",
  "seatClass": "string",
  "seatNumber": 0,
  "checkInGroup": 0,
  "username": "string",
  "email": "string",
  "phone": "string"
}
```

## DELETE /passengers/{id}

### Parameters:

id (integer)

## GET /payments/{stripe_id}

Under construction.

## POST /payments

Under construction.

## DELETE /payments/{stripe_id}

### Parameters:

stripe_id (string)

## POST /payments/payment-intent

Under construction.

## Required environment variables

    SERVER_PORT

    JWT_SECRET_KEY

    STRIPE_SECRET_KEY

### Development

    LOCAL_MYSQL_URL

    LOCAL_MYSQL_USERNAME

    LOCAL_MYSQL_PASSWORD

### Production

    MYSQL_URL

    MYSQL_USERNAME

    MYSQL_PASSWORD
