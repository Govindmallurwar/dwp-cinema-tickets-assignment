package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService reservationService;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        paymentService = mock(TicketPaymentService.class);
        reservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, reservationService);
    }

    @Test
    void purchasesAdultTickets() {
        ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));

        verify(paymentService).makePayment(1L, 50);
        verify(reservationService).reserveSeat(1L, 2);
    }

    @Test
    void chargesForAdultAndChildTicketsAndReservesASeatForEach() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)
        );

        verify(paymentService).makePayment(1L, 55);
        verify(reservationService).reserveSeat(1L, 3);
    }

    @Test
    void doesNotChargeOrReserveASeatForInfants() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        verify(paymentService).makePayment(1L, 50);
        verify(reservationService).reserveSeat(1L, 2);
    }

    @Test
    void handlesAdultChildAndInfantTicketsTogether() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        verify(paymentService).makePayment(1L, 95);
        verify(reservationService).reserveSeat(1L, 5);
    }

    @Test
    void allowsExactlyTwentyFiveTickets() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5)
        );

        verify(paymentService).makePayment(1L, 400);
        verify(reservationService).reserveSeat(1L, 20);
    }

    @Test
    void handlesRepeatedTicketTypesInOnePurchase() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        verify(paymentService).makePayment(1L, 90);
        verify(reservationService).reserveSeat(1L, 4);
    }

    @Test
    void rejectsNullAccountId() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
    }

    @Test
    void rejectsZeroAccountId() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
    }

    @Test
    void rejectsNegativeAccountId() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
    }

    @Test
    void rejectsNullTicketRequests() {
        assertInvalidPurchase(() -> ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null));
    }

    @Test
    void rejectsEmptyTicketRequests() {
        assertInvalidPurchase(() -> ticketService.purchaseTickets(1L));
    }

    @Test
    void rejectsNullTicketRequest() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1), null)
        );
    }

    @Test
    void rejectsTicketRequestWithNullType() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(null, 1))
        );
    }

    @Test
    void rejectsZeroTickets() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0))
        );
    }

    @Test
    void rejectsNegativeTickets() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1))
        );
    }

    @Test
    void rejectsMoreThanTwentyFiveTickets() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6)
                )
        );
    }

    @Test
    void rejectsChildTicketsWithoutAnAdultTicket() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2))
        );
    }

    @Test
    void rejectsInfantTicketsWithoutAnAdultTicket() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1))
        );
    }

    @Test
    void rejectsChildAndInfantTicketsWithoutAnAdultTicket() {
        assertInvalidPurchase(() ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
                )
        );
    }

    private void assertInvalidPurchase(Executable purchase) {
        assertThrows(InvalidPurchaseException.class, purchase);
        verifyNoInteractions(paymentService, reservationService);
    }
}
