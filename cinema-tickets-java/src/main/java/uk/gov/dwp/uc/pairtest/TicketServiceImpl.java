package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccount(accountId);
        validateTicketRequests(ticketTypeRequests);

        int amountToPay = calculateAmountToPay(ticketTypeRequests);
        int seatsToReserve = calculateSeatsToReserve(ticketTypeRequests);

        paymentService.makePayment(accountId, amountToPay);
        reservationService.reserveSeat(accountId, seatsToReserve);
    }

    private void validateAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateTicketRequests(TicketTypeRequest[] ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            validateTicketRequest(ticketTypeRequest);
        }

        validateTicketLimit(ticketTypeRequests);
        validateAdultRequired(ticketTypeRequests);
    }

    private void validateTicketRequest(TicketTypeRequest ticketTypeRequest) {
        if (ticketTypeRequest == null
                || ticketTypeRequest.getTicketType() == null
                || ticketTypeRequest.getNoOfTickets() <= 0) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateTicketLimit(TicketTypeRequest[] ticketTypeRequests) {
        if (countTickets(ticketTypeRequests) > MAX_TICKETS_PER_PURCHASE) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateAdultRequired(TicketTypeRequest[] ticketTypeRequests) {
        boolean hasAdultTicket = false;
        boolean hasChildOrInfantTicket = false;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.ADULT) {
                hasAdultTicket = true;
            }

            if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.CHILD
                    || ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.INFANT) {
                hasChildOrInfantTicket = true;
            }
        }

        if (hasChildOrInfantTicket && !hasAdultTicket) {
            throw new InvalidPurchaseException();
        }
    }

    private int calculateAmountToPay(TicketTypeRequest[] ticketTypeRequests) {
        int amountToPay = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            amountToPay += ticketTypeRequest.getNoOfTickets() * priceFor(ticketTypeRequest.getTicketType());
        }

        return amountToPay;
    }

    private int calculateSeatsToReserve(TicketTypeRequest[] ticketTypeRequests) {
        int seatsToReserve = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            if (requiresSeat(ticketTypeRequest.getTicketType())) {
                seatsToReserve += ticketTypeRequest.getNoOfTickets();
            }
        }

        return seatsToReserve;
    }

    private int countTickets(TicketTypeRequest[] ticketTypeRequests) {
        int totalTickets = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            totalTickets += ticketTypeRequest.getNoOfTickets();
        }

        return totalTickets;
    }

    private int priceFor(TicketTypeRequest.Type ticketType) {
        return switch (ticketType) {
            case ADULT -> ADULT_TICKET_PRICE;
            case CHILD -> CHILD_TICKET_PRICE;
            case INFANT -> 0;
        };
    }

    private boolean requiresSeat(TicketTypeRequest.Type ticketType) {
        return ticketType != TicketTypeRequest.Type.INFANT;
    }

}
