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

        int amountToPay = 0;
        int seatsToReserve = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            int noOfTickets = ticketTypeRequest.getNoOfTickets();

            if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.ADULT) {
                amountToPay += noOfTickets * ADULT_TICKET_PRICE;
                seatsToReserve += noOfTickets;
            }

            if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.CHILD) {
                amountToPay += noOfTickets * CHILD_TICKET_PRICE;
                seatsToReserve += noOfTickets;
            }
        }

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
    }

}
