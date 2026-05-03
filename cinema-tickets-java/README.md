# Cinema Tickets

Implementation of the DWP cinema ticket exercise.

## What It Does

`TicketServiceImpl` validates a ticket purchase request, calculates the payment amount, and reserves the correct number of seats through the supplied third-party services.

Ticket prices:

- Adult: £25
- Child: £15
- Infant: £0

Seat allocation:

- Adult and child tickets reserve seats.
- Infant tickets do not reserve seats because infants sit on an adult's lap.

## Validation Rules

A purchase is rejected when:

- The account id is null, zero, or negative.
- No ticket requests are supplied.
- Any ticket request is null.
- Any ticket request has a null type.
- Any ticket request has zero or negative tickets.
- More than 25 tickets are requested in one purchase.
- Child or infant tickets are requested without at least one adult ticket.

Rejected purchases throw `InvalidPurchaseException` before calling either external service.

## Tests

The test suite covers successful payment and seat reservation scenarios, plus invalid purchase requests.

Run the tests with:

```bash
mvn test
```
