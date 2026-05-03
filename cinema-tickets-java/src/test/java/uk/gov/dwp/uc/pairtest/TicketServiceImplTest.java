package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
